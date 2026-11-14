package com.nikolas.odteste.blocos.capela;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/**
 * Freio por jogador: no máximo uma vez a cada 2 s, não importa quantas velas ou poças estejam em volta.
 * Só memória (um número por jogador); limpo quando ele sai do servidor (BlocosCapela.iniciar).
 * Só servidor: tudo roda na thread do servidor.
 */
final class Freio {
    private final Map<UUID, Long> ultima = new HashMap<>();

    /** Libera se já passou o intervalo desde a última vez deste jogador (e já marca agora). */
    boolean liberar(ServerPlayer p) {
        long agora = p.server.overworld().getGameTime();
        UUID id = p.getUUID();
        if (!RegrasCapela.passouIntervalo(ultima.get(id), agora)) return false;
        ultima.put(id, agora);
        return true;
    }

    void esquecer(UUID id) {
        ultima.remove(id);
    }
}
