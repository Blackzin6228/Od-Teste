package com.nikolas.odteste.sistemas;

/**
 * O que a Marca de Sangue faz por estágio (proposta para o play test, a partir de docs/sistemas.md:
 * "fome que não para; mais força quando ferido"). Java puro, testado em RegrasTest.
 *
 * | Estágio | Ganha (com menos de metade da vida) | Paga (a cada 2 s) |
 * |---|---|---|
 * | 1 Sinal    | +0,5 de dano | nada |
 * | 2 Vínculo  | +1 de dano   | nada |
 * | 3 Cicatriz | +1,5 de dano | fome um pouco mais rápida |
 * | 4 Entrega  | +2,5 de dano | fome bem mais rápida |
 */
public final class RegrasSangue {
    private static final double[] BONUS_FERIDO = {0, 0.5, 1.0, 1.5, 2.5};
    /** Exaustão extra por checagem de 2 s. 4,0 de exaustão = 1 ponto de fome (ou saturação). */
    private static final float[] EXAUSTAO = {0f, 0f, 0f, 0.1f, 0.2f};

    private RegrasSangue() {}

    private static int estagio(int e) {
        return Math.max(0, Math.min(4, e));
    }

    /** Ferido = menos da metade da vida máxima. */
    public static boolean ferido(float vida, float vidaMaxima) {
        return vidaMaxima > 0 && vida < vidaMaxima / 2f;
    }

    /** Dano extra (somado ao dano de ataque) para quem tem Marca de Sangue e está ferido. */
    public static double bonusDano(int estagio, boolean ferido) {
        return ferido ? BONUS_FERIDO[estagio(estagio)] : 0;
    }

    public static float exaustaoExtra(int estagio) {
        return EXAUSTAO[estagio(estagio)];
    }

    /** Partículas vermelhas por checagem de 2 s, vistas por quem está perto. Discreto: 1 a 4. */
    public static int particulas(int estagio) {
        return estagio(estagio);
    }
}
