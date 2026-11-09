package com.nikolas.odteste.historia;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.estruturas.Colocacao;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Tudo da história que fica salvo no mundo (arquivo {@code data/odteste_historia.dat} do overworld):
 * grupos e o progresso de cada um, pontos ancorados pelo admin, vilas marcadas, estruturas colocadas
 * e aldeões que ainda precisam voltar ao normal. Arquivo estragado não crasha: o que não dá para ler é pulado.
 */
public final class DadosHistoria extends SavedData {
    public static final SavedData.Factory<DadosHistoria> FABRICA =
            new SavedData.Factory<>(DadosHistoria::new, DadosHistoria::carregar, null);

    public final Grupos grupos = new Grupos();
    /** Lugar da história (ex.: {@code vila_do_sino}) → ponto escolhido pelo admin. */
    public final Map<String, GlobalPos> ancoras = new LinkedHashMap<>();
    /** Grupo → vila marcada (uma por grupo). */
    public final Map<String, VilaMarcada> vilas = new LinkedHashMap<>();
    /** Id → estrutura colocada por comando. */
    public final Map<String, Colocacao> colocacoes = new LinkedHashMap<>();
    /** Aldeões que o desfazer não achou carregados: voltam ao normal quando o chunk carregar. */
    public final Map<UUID, Integer> descongelarDepois = new LinkedHashMap<>();
    /** Vilas do arquivo que não deu para ler (sem sino): não rodam, mas voltam iguais para o arquivo. */
    final List<CompoundTag> vilasIgnoradas = new ArrayList<>();
    private int proximaColocacao = 1;

    public static DadosHistoria de(MinecraftServer servidor) {
        return servidor.overworld().getDataStorage().computeIfAbsent(FABRICA, IdsDados.ARQUIVO_HISTORIA);
    }

    public String novoIdColocacao() {
        setDirty();
        return "e" + (proximaColocacao++);
    }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider registros) {
        ListTag lg = new ListTag();
        for (Grupos.Grupo g : grupos.todos()) {
            CompoundTag cg = new CompoundTag();
            cg.putString("nome", g.nome());
            CompoundTag membros = new CompoundTag();
            g.membros().forEach((u, n) -> membros.putString(u.toString(), n));
            cg.put("membros", membros);
            ListTag marcos = new ListTag();
            for (String m : g.marcos()) marcos.add(net.minecraft.nbt.StringTag.valueOf(m));
            cg.put("marcos", marcos);
            lg.add(cg);
        }
        t.put("grupos", lg);

        CompoundTag anc = new CompoundTag();
        ancoras.forEach((lugar, gp) -> {
            CompoundTag c = new CompoundTag();
            c.putString("dim", gp.dimension().location().toString());
            c.putLong("pos", gp.pos().asLong());
            anc.put(lugar, c);
        });
        t.put("ancoras", anc);

        ListTag lv = new ListTag();
        for (VilaMarcada v : vilas.values()) lv.add(v.salvar());
        for (CompoundTag cv : vilasIgnoradas) lv.add(cv.copy());
        t.put("vilas", lv);

        ListTag lc = new ListTag();
        for (Colocacao c : colocacoes.values()) lc.add(c.salvar());
        t.put("colocacoes", lc);

        CompoundTag desc = new CompoundTag();
        descongelarDepois.forEach((u, f) -> desc.putInt(u.toString(), f));
        t.put("descongelar", desc);
        t.putInt("proxima_colocacao", proximaColocacao);
        return t;
    }

    static DadosHistoria carregar(CompoundTag t, HolderLookup.Provider registros) {
        DadosHistoria d = new DadosHistoria();
        ListTag lg = t.getList("grupos", Tag.TAG_COMPOUND);
        for (int i = 0; i < lg.size(); i++) {
            CompoundTag cg = lg.getCompound(i);
            var nome = Grupos.normalizar(cg.getString("nome"));
            if (nome.isEmpty()) continue;
            Grupos.Grupo g = d.grupos.carregar(nome.get());
            CompoundTag membros = cg.getCompound("membros");
            for (String k : membros.getAllKeys()) {
                uuid(k).ifPresent(u -> Grupos.carregarMembro(g, u, membros.getString(k)));
            }
            ListTag marcos = cg.getList("marcos", Tag.TAG_STRING);
            for (int j = 0; j < marcos.size(); j++) Grupos.carregarMarco(g, marcos.getString(j));
        }

        CompoundTag anc = t.getCompound("ancoras");
        for (String lugar : anc.getAllKeys()) {
            CompoundTag c = anc.getCompound(lugar);
            ResourceLocation dim = ResourceLocation.tryParse(c.getString("dim"));
            if (dim == null) continue;
            d.ancoras.put(lugar, GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dim), BlockPos.of(c.getLong("pos"))));
        }

        ListTag lv = t.getList("vilas", Tag.TAG_COMPOUND);
        for (int i = 0; i < lv.size(); i++) {
            CompoundTag cv = lv.getCompound(i);
            VilaMarcada v = VilaMarcada.carregar(cv);
            if (v == null) {
                // Sem sino: o evento fica parado (não toca nada), mas a vila crua é salva de volta igual.
                OdMod.LOGGER.warn("odteste_historia: vila do grupo '{}' sem posição do sino; evento ignorado", cv.getString("grupo"));
                d.vilasIgnoradas.add(cv.copy());
                continue;
            }
            if (!v.grupo.isEmpty()) d.vilas.put(v.grupo, v);
        }

        ListTag lc = t.getList("colocacoes", Tag.TAG_COMPOUND);
        for (int i = 0; i < lc.size(); i++) {
            Colocacao c = Colocacao.carregar(lc.getCompound(i));
            if (!c.id().isEmpty()) d.colocacoes.put(c.id(), c);
        }

        CompoundTag desc = t.getCompound("descongelar");
        for (String k : desc.getAllKeys()) uuid(k).ifPresent(u -> d.descongelarDepois.put(u, desc.getInt(k)));
        d.proximaColocacao = Math.max(1, t.getInt("proxima_colocacao"));
        return d;
    }

    private static java.util.Optional<UUID> uuid(String s) {
        try {
            return java.util.Optional.of(UUID.fromString(s));
        } catch (IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }

    /** Volta para o Level da dimensão salva (null se a dimensão não existe mais). */
    public static net.minecraft.server.level.ServerLevel nivel(MinecraftServer s, ResourceKey<Level> dim) {
        return s.getLevel(dim);
    }
}
