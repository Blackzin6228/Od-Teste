package com.nikolas.odteste.sinais;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Quem emite sinal no mundo (sino marcado, bloco de notas, estrutura...). Registre com
 * {@link Sinais#registrarFonte(FonteDeSinal)} uma vez, no início do mod.
 *
 * Chamado só no servidor, no máximo uma vez a cada 2 s por Analisador com jogador perto, e quando alguém
 * usa o Analisador. Tem que ser barato: olhe só a sua lista de pontos, nunca varra blocos do mundo.
 */
@FunctionalInterface
public interface FonteDeSinal {
    /** Sinais a até {@code raio} blocos de {@code centro}. Lista vazia se nada. */
    List<Sinal> perto(ServerLevel nivel, BlockPos centro, int raio);
}
