package com.nikolas.odteste.sinais;

import com.nikolas.odteste.OdMod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Fontes de sinal fixas por posição, salvas no mundo (arquivo {@code data/odteste_sinais.dat} do overworld,
 * ver docs/ids.md). Diferente de {@link SinaisNoMundo}, sobrevivem a reinício e o Analisador capta de longe.
 * Mundo que já existe: o arquivo não existe ainda e começa vazio. Entrada estragada é pulada, sem crash.
 * Use pela API de {@link Sinais} ({@code registrarFonteFixa}, {@code removerFonteFixa}).
 */
public final class FontesFixas extends SavedData {
    public static final String ARQUIVO = "odteste_sinais";
    private static final SavedData.Factory<FontesFixas> FABRICA =
            new SavedData.Factory<>(FontesFixas::new, FontesFixas::carregar, null);

    /** Uma fonte. {@code tipo} escolhe os textos: {@code odteste.sinal.<tipo>.decifrado} / {@code .camada}. */
    public record Fonte(String id, ResourceKey<Level> dimensao, BlockPos pos, String tipo, boolean comCamada) {
        public Sinal sinal() {
            return Sinal.doLang(tipo, comCamada);
        }
    }

    private final Map<String, Fonte> fontes = new LinkedHashMap<>();

    public static FontesFixas de(MinecraftServer servidor) {
        return servidor.overworld().getDataStorage().computeIfAbsent(FABRICA, ARQUIVO);
    }

    public Collection<Fonte> todas() {
        return Collections.unmodifiableCollection(fontes.values());
    }

    public Optional<Fonte> pegar(String id) {
        return Optional.ofNullable(fontes.get(id));
    }

    Sinais.Resultado colocar(Fonte f) {
        if (!RegrasSinal.nomeValido(f.id()) || !RegrasSinal.nomeValido(f.tipo())) return Sinais.Resultado.NOME_INVALIDO;
        if (!fontes.containsKey(f.id()) && fontes.size() >= RegrasSinal.MAX_FONTES) return Sinais.Resultado.CHEIO;
        fontes.put(f.id(), f);
        setDirty();
        return Sinais.Resultado.OK;
    }

    boolean tirar(String id) {
        if (fontes.remove(id) == null) return false;
        setDirty();
        return true;
    }

    /** Sinais a até {@code raio} blocos (decifráveis). */
    List<Sinal> perto(ServerLevel nivel, BlockPos centro, int raio) {
        if (fontes.isEmpty()) return List.of();
        long raio2 = (long) raio * raio;
        List<Sinal> achados = new ArrayList<>();
        for (Fonte f : fontes.values()) {
            if (f.dimensao().equals(nivel.dimension()) && f.pos().distSqr(centro) <= raio2) achados.add(f.sinal());
        }
        return achados;
    }

    /** A fonte mais perto a até {@code alcance} blocos, na mesma dimensão. */
    Optional<Fonte> maisProxima(ServerLevel nivel, BlockPos centro, int alcance) {
        Fonte melhor = null;
        double melhorD2 = (double) alcance * alcance;
        for (Fonte f : fontes.values()) {
            if (!f.dimensao().equals(nivel.dimension())) continue;
            double d2 = f.pos().distSqr(centro);
            if (d2 <= melhorD2) {
                melhor = f;
                melhorD2 = d2;
            }
        }
        return Optional.ofNullable(melhor);
    }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider registros) {
        ListTag lista = new ListTag();
        for (Fonte f : fontes.values()) {
            CompoundTag c = new CompoundTag();
            c.putString("id", f.id());
            c.putString("dim", f.dimensao().location().toString());
            c.putLong("pos", f.pos().asLong());
            c.putString("tipo", f.tipo());
            c.putBoolean("camada", f.comCamada());
            lista.add(c);
        }
        t.put("fontes", lista);
        return t;
    }

    static FontesFixas carregar(CompoundTag t, HolderLookup.Provider registros) {
        FontesFixas dados = new FontesFixas();
        ListTag lista = t.getList("fontes", Tag.TAG_COMPOUND);
        for (int i = 0; i < lista.size(); i++) {
            CompoundTag c = lista.getCompound(i);
            String id = c.getString("id");
            String tipo = c.getString("tipo");
            ResourceLocation dim = ResourceLocation.tryParse(c.getString("dim"));
            if (!RegrasSinal.nomeValido(id) || !RegrasSinal.nomeValido(tipo) || dim == null
                    || dados.fontes.size() >= RegrasSinal.MAX_FONTES) {
                OdMod.LOGGER.warn("Fonte de sinal salva ilegível ({}); pulada", c);
                continue;
            }
            boolean camada = !c.contains("camada") || c.getBoolean("camada");
            dados.fontes.put(id, new Fonte(id, ResourceKey.create(Registries.DIMENSION, dim),
                    BlockPos.of(c.getLong("pos")), tipo, camada));
        }
        return dados;
    }
}
