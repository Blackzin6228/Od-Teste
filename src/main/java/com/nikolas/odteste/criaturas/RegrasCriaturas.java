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
 * Limites, para continuar raro:
 * - no máximo 1 por jogador Tocado por noite (dia do relógio; salvo no jogador em {@code odteste:criatura_noite});
 * - nunca se já existe outra criatura de Sangue a menos de {@link #RAIO_AREA} blocos (cobre o chunk e os vizinhos).
 * O peso no bioma também é baixo e fica fora dos biomas sem monstro (ver data/odteste/neoforge/biome_modifier/).
 */
public final class RegrasCriaturas {
    public static final long DIAS_MINIMOS = 3;
    public static final int DISTANCIA_SPAWN = 256;
    public static final int RAIO_JOGADOR = 48;
    public static final int EXPOSICAO_MINIMA = 20;
    /** Raio (horizontal) em que só pode haver uma criatura de Sangue nascida sozinha. */
    public static final int RAIO_AREA = 48;
    /** Altura (para cima e para baixo) da mesma busca. */
    public static final int ALTURA_AREA = 32;
    /** Valor de "nunca nasceu nenhuma perto deste jogador". */
    public static final long NUNCA = -1;

    /** Raio em que a criatura "é vista" (Exposição e Lucidez, uma vez por jogador por criatura). */
    public static final double RAIO_VISTA = 12;
    /** Golpe pelas costas no Retalhado (cego nas costas): dano × 1,5. */
    public static final float MULT_COSTAS = 1.5f;
    /** Luz em que os filhotes da Sanguessuga-Mãe morrem. */
    public static final int LUZ_FORTE = 12;

    /** Exposição Alta (Fundo): a partir daqui a Mega Torch não protege mais o jogador (compat/torchmaster). */
    public static final int EXPOSICAO_TOCHA = 60;

    private RegrasCriaturas() {}

    /** A Mega Torch ainda protege quem tem esta Exposição? */
    public static boolean tochaProtege(int exposicao) {
        return exposicao < EXPOSICAO_TOCHA;
    }

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

    /** Número da noite (um por dia do relógio do mundo; dormir passa para a próxima). */
    public static long noite(long horaDoDia) {
        return Math.floorDiv(horaDoDia, 24000L);
    }

    /** No máximo uma por jogador Tocado por noite. */
    public static boolean livreNestaNoite(long ultimaNoite, long noiteAtual) {
        return ultimaNoite != noiteAtual;
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
