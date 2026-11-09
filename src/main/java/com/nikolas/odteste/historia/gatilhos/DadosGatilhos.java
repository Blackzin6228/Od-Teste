package com.nikolas.odteste.historia.gatilhos;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * O que os gatilhos guardam no mundo (arquivo {@code data/odteste_gatilhos.dat} do overworld), por grupo.
 * Mundo sem este arquivo: nenhum grupo armado ainda; grupos que já têm o marco {@code primeiro_sinal} viram
 * "já teve" na primeira olhada. Grupo apagado sai do arquivo.
 */
public final class DadosGatilhos extends SavedData {
    public static final SavedData.Factory<DadosGatilhos> FABRICA =
            new SavedData.Factory<>(DadosGatilhos::criar, DadosGatilhos::carregar, null);
    /** Formato do arquivo (gravado; ainda só existe o 1). */
    static final int VERSAO = 1;

    private final Map<String, EstadoGrupo> grupos = new LinkedHashMap<>();
    /** Criado agora (não veio do disco): os grupos que já existem ainda precisam virar "já teve". Só memória. */
    private boolean novo;

    private DadosGatilhos() {}

    /** O arquivo não existia: mundo novo ou atualização num mundo que já existe. */
    private static DadosGatilhos criar() {
        DadosGatilhos d = new DadosGatilhos();
        d.novo = true;
        return d;
    }

    public static DadosGatilhos de(MinecraftServer s) {
        DadosGatilhos d = s.overworld().getDataStorage().computeIfAbsent(FABRICA, IdsGatilhos.ARQUIVO);
        if (d.novo) {
            // Todo grupo que já existe fica de fora do automático. Grava já: assim o arquivo existe na próxima
            // vez, e grupo criado depois não é confundido com grupo antigo.
            d.novo = false;
            RegrasGatilhos.primeiraCriacao(d.grupos,
                    DadosHistoria.de(s).grupos.todos().stream().map(Grupos.Grupo::nome).toList());
            d.setDirty();
            OdMod.LOGGER.info("Gatilhos: arquivo novo; {} grupo(s) que já existiam ficam fora do Primeiro Sinal automático",
                    d.grupos.size());
        }
        return d;
    }

    public EstadoGrupo estado(String grupo) {
        return grupos.computeIfAbsent(grupo, k -> {
            setDirty();
            return new EstadoGrupo();
        });
    }

    /** Tira grupos que não existem mais. */
    public void manterSo(Collection<String> existentes) {
        if (grupos.keySet().retainAll(existentes)) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider registros) {
        CompoundTag cg = new CompoundTag();
        grupos.forEach((nome, st) -> {
            CompoundTag c = new CompoundTag();
            c.putBoolean("ja_teve", st.jaTeve);
            c.putBoolean("armado", st.armado);
            c.putBoolean("livro_lido", st.livroLido);
            c.putInt("noites", st.noites);
            if (st.ultimaNoite != EstadoGrupo.NENHUMA) c.putLong("ultima_noite", st.ultimaNoite);
            c.putBoolean("avisado", st.avisado);
            c.putBoolean("falha_avisada", st.falhaAvisada);
            cg.put(nome, c);
        });
        t.putInt(IdsGatilhos.CHAVE_VERSAO, VERSAO);
        t.put(IdsGatilhos.CHAVE_GRUPOS, cg);
        return t;
    }

    static DadosGatilhos carregar(CompoundTag t, HolderLookup.Provider registros) {
        DadosGatilhos d = new DadosGatilhos();
        CompoundTag cg = t.getCompound(IdsGatilhos.CHAVE_GRUPOS);
        for (String nome : cg.getAllKeys()) {
            CompoundTag c = cg.getCompound(nome);
            EstadoGrupo st = new EstadoGrupo();
            st.jaTeve = c.getBoolean("ja_teve");
            st.armado = c.getBoolean("armado");
            st.livroLido = c.getBoolean("livro_lido");
            st.noites = Math.max(0, c.getInt("noites"));
            if (c.contains("ultima_noite", Tag.TAG_LONG)) st.ultimaNoite = c.getLong("ultima_noite");
            st.avisado = c.getBoolean("avisado");
            st.falhaAvisada = c.getBoolean("falha_avisada");
            d.grupos.put(nome, st);
        }
        return d;
    }
}
