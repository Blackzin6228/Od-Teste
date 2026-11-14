package com.nikolas.odteste.historia.vilacalada;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
    /** Peças que o {@code preparar} já pôs uma vez ("<canto>:<peça>"): rodar de novo não enche o baú de novo. */
    public final Set<String> preparados = new LinkedHashSet<>();
    /** Dobre de cada grupo (desde 14/11/2026). Mundo sem a chave: ninguém tentou ainda. */
    public final Map<String, Dobre> dobre = new LinkedHashMap<>();

    /**
     * O que fica salvo do Dobre de um grupo. Tudo começa no "nada aconteceu" (padrão seguro para o mundo que já
     * existe). O dobre feito é o marco {@code dobre} do grupo (arquivo da história), não fica aqui.
     */
    public static final class Dobre {
        /** Dia do jogo em que o grupo gastou a noite (errou ou acabou). -1 = nunca. */
        public long noiteGasta = -1;
        /** Contagens erradas (empurrão da corrente na 2ª). */
        public int errosConta;
        /** Noites com alguém do grupo dentro da vila sem tentar (empurrão dos cliques na 2ª). */
        public int noitesSemTentar;
        /** Último dia contado em {@link #noitesSemTentar} (conta uma vez por noite). */
        public long ultimaNoiteContada = -1;
        /** Bateu no sino rachado com a mão ou outra coisa (a bigorna vira na manhã seguinte). */
        public boolean outraCoisa;
        /** Empurrões já feitos ("bigorna", "corrente"): cada um uma vez. */
        public final Set<String> empurroes = new LinkedHashSet<>();
        /** Dia do jogo em que o dobre foi feito (-1 = não). Os ajoelhados viram cinza na manhã seguinte. */
        public long feitoDia = -1;

        CompoundTag salvar() {
            CompoundTag t = new CompoundTag();
            t.putLong("noite_gasta", noiteGasta);
            t.putInt("erros_conta", errosConta);
            t.putInt("noites_sem_tentar", noitesSemTentar);
            t.putLong("ultima_noite_contada", ultimaNoiteContada);
            t.putBoolean("outra_coisa", outraCoisa);
            ListTag l = new ListTag();
            for (String e : empurroes) l.add(StringTag.valueOf(e));
            t.put("empurroes", l);
            t.putLong("feito_dia", feitoDia);
            return t;
        }

        static Dobre carregar(CompoundTag t) {
            Dobre d = new Dobre();
            d.noiteGasta = t.contains("noite_gasta", Tag.TAG_LONG) ? t.getLong("noite_gasta") : -1;
            d.errosConta = Math.max(0, t.getInt("erros_conta"));
            d.noitesSemTentar = Math.max(0, t.getInt("noites_sem_tentar"));
            d.ultimaNoiteContada = t.contains("ultima_noite_contada", Tag.TAG_LONG) ? t.getLong("ultima_noite_contada") : -1;
            d.outraCoisa = t.getBoolean("outra_coisa");
            ListTag l = t.getList("empurroes", Tag.TAG_STRING);
            for (int i = 0; i < l.size(); i++) d.empurroes.add(l.getString(i));
            d.feitoDia = t.contains("feito_dia", Tag.TAG_LONG) ? t.getLong("feito_dia") : -1;
            return d;
        }
    }

    /** O dobre do grupo (cria vazio na primeira vez; quem muda chama {@link #setDirty()}). */
    public Dobre dobre(String grupo) {
        return dobre.computeIfAbsent(grupo, k -> new Dobre());
    }

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
        ListTag pr = new ListTag();
        for (String v : preparados) pr.add(StringTag.valueOf(v));
        t.put("preparados", pr);
        CompoundTag db = new CompoundTag();
        for (Map.Entry<String, Dobre> e : dobre.entrySet()) db.put(e.getKey(), e.getValue().salvar());
        t.put("dobre", db);
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
        ListTag pr = t.getList("preparados", Tag.TAG_STRING);
        for (int i = 0; i < pr.size(); i++) d.preparados.add(pr.getString(i));
        CompoundTag db = t.getCompound("dobre");
        for (String g : db.getAllKeys()) d.dobre.put(g, Dobre.carregar(db.getCompound(g)));
        return d;
    }
}
