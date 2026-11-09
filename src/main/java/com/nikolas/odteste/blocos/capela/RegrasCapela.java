package com.nikolas.odteste.blocos.capela;

import com.nikolas.odteste.sistemas.RegrasLucidez;

/**
 * Ritmo da vela da capela e das poças de sangue da Vila Calada (lore/fichas-estruturas.md, Perigos).
 * Java puro, testado em RegrasCapelaTest. Nada aparece em número para o jogador.
 *
 * - Vela: quem fica perto dela recupera Lucidez (o refúgio da vila).
 * - Poça: quem pisa ou fica parado nela perde um pouco de Lucidez.
 * As duas checam no mesmo passo do Vigia (2 s), nunca a cada tick.
 */
public final class RegrasCapela {
    /** Uma checagem a cada 2 s, igual ao Vigia. */
    public static final int TICKS_POR_CHECAGEM = RegrasLucidez.TICKS_POR_CHECAGEM;
    /** Distância (blocos) da vela em que a Lucidez volta: cabe a nave da capela, não a praça. */
    public static final double VELA_RAIO = 5;
    /** Lucidez que a vela devolve por checagem (+1 a cada 2 s: de vazio a cheio em uns 3 min). */
    public static final int VELA_GANHO = 1;
    /** Lucidez que a poça tira por checagem (passar rápido custa 0 ou 1; ficar um minuto custa 30). */
    public static final int POCA_PERDA = 1;
    /** Luz da vela: o bastante para não nascer monstro em volta, pouca para não virar "luz forte" (12). */
    public static final int VELA_LUZ = 9;

    private RegrasCapela() {}

    /** É a vez desta vela/jogador checar? O deslocamento espalha as checagens por ticks diferentes. */
    public static boolean vezDeChecar(long tempoDoMundo, long deslocamento) {
        return Math.floorMod(tempoDoMundo + deslocamento, TICKS_POR_CHECAGEM) == 0;
    }

    /**
     * Freio por jogador: já passaram 2 s desde a última vez (ganho da vela ou perda da poça)?
     * Sem registro = libera. Relógio que voltou (mundo trocado) também libera, para não travar.
     */
    public static boolean passouIntervalo(Long ultima, long agora) {
        if (ultima == null) return true;
        long passou = agora - ultima;
        return passou >= TICKS_POR_CHECAGEM || passou < 0;
    }

    /** O jogador está perto o bastante da vela? Recebe a distância ao quadrado (sem raiz). */
    public static boolean pertoDaVela(double distanciaAoQuadrado) {
        return distanciaAoQuadrado >= 0 && distanciaAoQuadrado <= VELA_RAIO * VELA_RAIO;
    }
}
