package com.nikolas.odteste.aparelhos;

/**
 * Números e contas do Analisador de Sinal (Java puro, testado em {@code RegrasAnalisadorTest}).
 * Proposta para o play test: muda aqui e só aqui.
 */
public final class RegrasAnalisador {
    /** Bateria interna. */
    public static final int CAPACIDADE = 100_000;
    /** Quanto aceita por tick de cabo de qualquer mod. */
    public static final int ENTRADA_MAXIMA = 1_000;
    /** Gasto ligado, só com jogador perto (20 FE/t, como a ficha do rádio em docs/tech-paranormal.md). */
    public static final int CONSUMO_POR_TICK = 20;
    /** De quanto em quanto tempo (ticks) o aparelho olha em volta. Nada roda a cada tick. */
    public static final int INTERVALO = 40;
    /** Gasto de cada checagem (cobre os ticks do intervalo). */
    public static final int GASTO_CHECAGEM = CONSUMO_POR_TICK * INTERVALO;
    /** Gasto de cada leitura (decifrar). */
    public static final int CUSTO_LEITURA = 20_000;
    /** Até onde ouve um sinal, em blocos. */
    public static final int RAIO_SINAL = 8;
    /** Até onde capta uma fonte fixa (só a direção e a força, sem decifrar). */
    public static final int ALCANCE_LONGE = 160;
    /** Gasto de cada leitura de longe (apontar a direção). */
    public static final int CUSTO_DIRECAO = 5_000;
    /** O chiado de longe toca sozinho a cada tantas checagens (3 = a cada 6 s). */
    public static final int CHECAGENS_POR_CHIADO = 3;
    /** Só trabalha com jogador a até essa distância. */
    public static final int RAIO_JOGADOR = 16;
    /** Espera entre duas leituras do mesmo aparelho (ticks), contra clique repetido. */
    public static final int ESPERA_LEITURA = 40;
    /** No máximo quantos sinais aparecem numa leitura. */
    public static final int MAX_SINAIS = 3;

    private RegrasAnalisador() {}

    /** Cada aparelho checa num tick diferente (semente = posição), para não pesar tudo no mesmo tick. */
    public static boolean vezDeChecar(long tempoDoMundo, long semente) {
        return Math.floorMod(tempoDoMundo + semente, INTERVALO) == 0;
    }

    /** Tem energia para ficar ligado mais um intervalo. */
    public static boolean ficaLigado(int energia) {
        return energia >= GASTO_CHECAGEM;
    }

    /** Tem energia para decifrar sinal perto. */
    public static boolean podeLer(int energia) {
        return podePagar(energia, CUSTO_LEITURA);
    }

    /** Tem energia para apontar a direção de uma fonte longe (mais barato que decifrar). */
    public static boolean podeApontar(int energia) {
        return podePagar(energia, CUSTO_DIRECAO);
    }

    /** Cada leitura exige só o próprio custo, nunca o da leitura mais cara. */
    public static boolean podePagar(int energia, int custo) {
        return energia >= Math.max(0, custo);
    }

    public static boolean esperouLeitura(long agora, long ultimaLeitura) {
        return agora - ultimaLeitura >= ESPERA_LEITURA;
    }

    /** Energia depois de gastar, nunca abaixo de zero. */
    public static int gastar(int energia, int custo) {
        return Math.max(0, energia - Math.max(0, custo));
    }
}
