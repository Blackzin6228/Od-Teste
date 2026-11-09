package com.nikolas.odteste.criaturas;

/**
 * Regras das criaturas de Sangue (Java puro, testado em RegrasConteudoTest). Números são proposta para o play test.
 *
 * Nascimento natural (raro e só em condição paranormal):
 * - nunca nos primeiros {@link #DIAS_MINIMOS} dias de jogo do mundo (contados pelo relógio do mundo, que
 *   {@code /time set} não mexe);
 * - nunca a menos de {@link #DISTANCIA_SPAWN} blocos do spawn do mundo;
 * - só perto (até {@link #RAIO_JOGADOR} blocos) de um jogador já "Tocado" (Exposição 20+): o paranormal vem atrás
 *   de quem ele já percebeu;
 * - e só onde um monstro comum também nasceria (escuro, dificuldade não pacífica).
 * O peso no bioma também é baixo (ver data/odteste/neoforge/biome_modifier/).
 */
public final class RegrasCriaturas {
    public static final long DIAS_MINIMOS = 3;
    public static final int DISTANCIA_SPAWN = 256;
    public static final int RAIO_JOGADOR = 48;
    public static final int EXPOSICAO_MINIMA = 20;

    /** Raio em que a criatura "é vista" (Exposição e Lucidez, uma vez por jogador por criatura). */
    public static final double RAIO_VISTA = 12;
    /** Golpe pelas costas no Retalhado (cego nas costas): dano × 1,5. */
    public static final float MULT_COSTAS = 1.5f;
    /** Luz em que os filhotes da Sanguessuga-Mãe morrem. */
    public static final int LUZ_FORTE = 12;

    private RegrasCriaturas() {}

    /**
     * @param tempoDoMundo ticks de jogo desde que o mundo foi criado
     * @param dx distância em X até o spawn do mundo
     * @param dz distância em Z até o spawn do mundo
     * @param exposicaoPerto Exposição do jogador mais perto (0 se não houver jogador no raio)
     */
    public static boolean podeNascer(long tempoDoMundo, double dx, double dz, int exposicaoPerto) {
        if (tempoDoMundo / 24000L < DIAS_MINIMOS) return false;
        if (dx * dx + dz * dz < (double) DISTANCIA_SPAWN * DISTANCIA_SPAWN) return false;
        return exposicaoPerto >= EXPOSICAO_MINIMA;
    }

    /**
     * Multiplicador do dano conforme de onde veio o golpe.
     * @param produto produto escalar (horizontal, normalizado) entre para onde a criatura olha e a direção do atacante:
     *                1 = de frente, −1 = bem nas costas
     */
    public static float multiplicadorCostas(double produto) {
        return produto < -0.5 ? MULT_COSTAS : 1f;
    }
}
