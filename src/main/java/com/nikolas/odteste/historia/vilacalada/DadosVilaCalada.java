package com.nikolas.odteste.historia.vilacalada;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * O que a Vila Calada guarda no mundo (arquivo {@code data/odteste_vila_calada.dat} do overworld).
 * O ponto da vila fica junto das outras âncoras, em {@code odteste_historia}. Mundo sem este arquivo: pistas
 * ligadas e nada ancorado (então nada acontece até o admin colocar a vila ou ancorar).
 */
public final class DadosVilaCalada extends SavedData {
    public static final SavedData.Factory<DadosVilaCalada> FABRICA =
            new SavedData.Factory<>(DadosVilaCalada::new, DadosVilaCalada::carregar, null);

    /** O admin pode desligar as três pistas. */
    public boolean ligado = true;
    /** Id da colocação de estrutura que ancorou a vila ("" = nenhuma, ou o admin ancorou à mão). */
    public String colocacao = "";
    /** O ponto que a colocação ancorou. Se o admin mudou a âncora, o desfazer da estrutura não mexe nela. */
    public Long ancoraAuto;
    /** Dimensão desse ponto (posição igual em outra dimensão não é a mesma âncora). */
    public String ancoraAutoDim = "";
    /** Colocações da vila já vistas: cada uma ancora uma vez só (o admin pode mudar a âncora depois). */
    public final Set<String> vistas = new LinkedHashSet<>();

    public static DadosVilaCalada de(MinecraftServer s) {
        return s.overworld().getDataStorage().computeIfAbsent(FABRICA, IdsVilaCalada.ARQUIVO);
    }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider registros) {
        t.putBoolean("ligado", ligado);
        t.putString("colocacao", colocacao);
        if (ancoraAuto != null) t.putLong("ancora_auto", ancoraAuto);
        t.putString("ancora_auto_dim", ancoraAutoDim);
        ListTag l = new ListTag();
        for (String v : vistas) l.add(StringTag.valueOf(v));
        t.put("vistas", l);
        return t;
    }

    static DadosVilaCalada carregar(CompoundTag t, HolderLookup.Provider registros) {
        DadosVilaCalada d = new DadosVilaCalada();
        d.ligado = !t.contains("ligado") || t.getBoolean("ligado");
        d.colocacao = t.getString("colocacao");
        if (t.contains("ancora_auto", Tag.TAG_LONG)) d.ancoraAuto = t.getLong("ancora_auto");
        d.ancoraAutoDim = t.getString("ancora_auto_dim");
        ListTag l = t.getList("vistas", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) d.vistas.add(l.getString(i));
        return d;
    }
}
