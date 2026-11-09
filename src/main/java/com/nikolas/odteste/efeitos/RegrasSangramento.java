package com.nikolas.odteste.efeitos;

/**
 * Contas do sangramento e da Rubra (Java puro, testado em RegrasConteudoTest).
 * Números são proposta para o play test.
 */
public final class RegrasSangramento {
    /** Nível máximo do sangramento (0 a 4 = 5 acúmulos). */
    public static final int NIVEL_MAX = 4;
    /** Duração de cada acúmulo da Rubra: 5 s. */
    public static final int DURACAO_RUBRA = 100;

    /** A Rubra só fica forte com a vida do dono abaixo de 70%. */
    public static final float RUBRA_LIMIAR_VIDA = 0.7f;
    /** Ferida aberta: raio e dano em área. */
    public static final double FERIDA_RAIO = 3.0;
    public static final float FERIDA_DANO = 5f;

    private RegrasSangramento() {}

    /** Dano por segundo: 0,5 no nível 0, mais 0,5 por nível (2,5 no nível 4). */
    public static float danoPorSegundo(int nivel) {
        int n = Math.max(0, Math.min(NIVEL_MAX, nivel));
        return 0.5f * (n + 1);
    }

    /** Vida abaixo de 70% da máxima? (máxima inválida conta como "não"). */
    public static boolean rubraForte(float vida, float vidaMax) {
        return rubraForte(vida, vidaMax, RUBRA_LIMIAR_VIDA);
    }

    /** Igual, com outro limiar (o dono de Sangue acorda a Rubra mais cedo: RegrasElemento.limiarVida). */
    public static boolean rubraForte(float vida, float vidaMax, float limiar) {
        if (!(vidaMax > 0)) return false;
        return vida < vidaMax * limiar;
    }

    /** O golpe da Rubra abre a ferida quando o alvo já está no acúmulo máximo. */
    public static boolean abreFerida(int nivelAtual) {
        return nivelAtual >= NIVEL_MAX;
    }

    /** Próximo acúmulo do golpe (−1 = sem sangramento → 0). */
    public static int proximoAcumulo(int nivelAtual) {
        return Math.max(0, Math.min(NIVEL_MAX, nivelAtual + 1));
    }
}
