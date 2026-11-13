package com.nikolas.odteste.achados;

/**
 * Regras (Java puro, testadas em RegrasAchadosTest) de como o jogador acha Artefatos Paranormais e de quando as
 * criaturas de Sangue rondam a Vila Calada. Decisão: docs/decisoes/2026-11-12-artefatos-e-ronda.md.
 * Números são proposta para o play test.
 */
public final class RegrasAchados {
    /** Chance de uma criatura de Sangue derrotada soltar um artefato (um sorteio só por criatura). */
    public static final double CHANCE_DROP = 0.03;
    /** Quem já tem o artefato no inventário tem a chance multiplicada por isto. */
    public static final double FATOR_JA_TEM = 0.25;
    /** Pesos do sorteio, na ordem de {@code IdsDados.ARTEFATOS}: comum 6, incomum 3, raro 1. */
    public static final int[] PESOS = {6, 3, 1};

    /** Ronda: raio (horizontal) em volta da âncora da Vila Calada. */
    public static final int RAIO_RONDA = 128;
    /** Ronda: de quantos em quantos ticks o servidor olha (30 s). */
    public static final int INTERVALO_TICKS = 600;
    /** Ronda: chance por jogador (perto da vila) a cada olhada. */
    public static final double CHANCE_RONDA = 0.06;
    /** Ronda: no máximo tantas criaturas de Sangue vivas em volta da âncora. */
    public static final int MAX_PERTO_ANCORA = 2;
    /** Ronda: no máximo tantas por chunk. */
    public static final int MAX_POR_CHUNK = 1;
    /** Ronda: quantos jogadores a olhada considera por vez (limite de custo). */
    public static final int MAX_JOGADORES = 4;
    /** Ronda: distância do jogador em que a criatura aparece. */
    public static final int DIST_MIN = 24;
    public static final int DIST_MAX = 40;
    /** Ronda: chance de ser a Sanguessuga-Mãe (a outra é o Retalhado). */
    public static final double CHANCE_SANGUESSUGA = 0.3;

    private RegrasAchados() {}

    /** Chance final do drop. */
    public static double chanceDrop(boolean jaTem) {
        return jaTem ? CHANCE_DROP * FATOR_JA_TEM : CHANCE_DROP;
    }

    /** @param sorte número de 0 (inclusive) a 1 (exclusive) */
    public static boolean sorteou(double chance, double sorte) {
        return sorte < chance;
    }

    /**
     * Qual artefato cai. Quem já tem algum deles prefere os que não tem; se tem todos, vale o sorteio normal.
     * @param tem para cada artefato, se o jogador já o tem (mesmo tamanho de {@link #PESOS})
     * @param sorteio número de 0 a (soma dos pesos usados − 1)
     * @return posição do artefato escolhido
     */
    public static int escolher(boolean[] tem, int sorteio) {
        int[] pesos = new int[PESOS.length];
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            pesos[i] = tem[i] ? 0 : PESOS[i];
            soma += pesos[i];
        }
        if (soma == 0) {
            pesos = PESOS.clone();
            soma = 0;
            for (int p : pesos) soma += p;
        }
        int s = Math.floorMod(sorteio, soma);
        for (int i = 0; i < pesos.length; i++) {
            if (s < pesos[i]) return i;
            s -= pesos[i];
        }
        return pesos.length - 1;
    }

    /** Soma dos pesos que {@link #escolher} usa para este caso (para o chamador sortear dentro do limite). */
    public static int somaPesos(boolean[] tem) {
        int soma = 0;
        for (int i = 0; i < PESOS.length; i++) if (!tem[i]) soma += PESOS[i];
        if (soma == 0) for (int p : PESOS) soma += p;
        return soma;
    }

    /** Noite do Minecraft (do pôr do sol ao nascer do sol). */
    public static boolean ehNoite(long horaDoDia) {
        long t = Math.floorMod(horaDoDia, 24000L);
        return t >= 13000 && t <= 23000;
    }

    public static boolean dentroDoRaio(double dx, double dz) {
        return dx * dx + dz * dz <= (double) RAIO_RONDA * RAIO_RONDA;
    }

    /**
     * Se a ronda pode fazer nascer mais uma criatura agora. As criaturas só vêm depois dos primeiros dias do mundo
     * (como no nascimento natural), de noite, e nunca passam dos limites.
     * @param tempoDoMundo ticks de jogo desde que o mundo foi criado
     * @param jaPertoDaAncora criaturas de Sangue vivas em volta da âncora
     * @param jaNoChunk criaturas de Sangue vivas no chunk escolhido
     */
    public static boolean podeRondar(long tempoDoMundo, boolean noite, int jaPertoDaAncora, int jaNoChunk,
                                     long diasMinimos) {
        if (!noite) return false;
        if (tempoDoMundo / 24000L < diasMinimos) return false;
        return jaPertoDaAncora < MAX_PERTO_ANCORA && jaNoChunk < MAX_POR_CHUNK;
    }
}
