package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.rede.Momento;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Manda um momento para o próprio jogador e para quem está a até 32 blocos (nunca para o servidor inteiro). */
public final class Momentos {
    public static final double RAIO = 32;
    /** "Em combate": tomou ou deu dano nos últimos 5 s. */
    private static final int COMBATE_TICKS = 100;

    private Momentos() {}

    public static void enviar(ServerPlayer p, TipoMomento tipo, Elemento elemento, Elemento antigo, int valor) {
        Momento m = new Momento(tipo.ordinal(), p.getId(),
                (elemento == null ? Elemento.NENHUM : elemento).id(),
                (antigo == null ? Elemento.NENHUM : antigo).id(),
                valor, emCombate(p));
        PacketDistributor.sendToPlayersNear(p.serverLevel(), null, p.getX(), p.getY(), p.getZ(), RAIO, m);
    }

    static boolean emCombate(ServerPlayer p) {
        int agora = p.tickCount;
        boolean apanhou = p.getLastHurtByMob() != null && agora - p.getLastHurtByMobTimestamp() < COMBATE_TICKS;
        boolean bateu = p.getLastHurtMob() != null && agora - p.getLastHurtMobTimestamp() < COMBATE_TICKS;
        return apanhou || bateu || p.hurtTime > 0;
    }
}
