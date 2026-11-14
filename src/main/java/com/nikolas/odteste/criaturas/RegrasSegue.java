package com.nikolas.odteste.criaturas;

/**
 * Contas do "O Que Segue" (nome provisório, docs/decisoes/2026-11-13-o-que-segue.md). Java puro, testado em
 * RegrasSegueTest. Todos os números são proposta: ajustar no teste (a maioria também está na config do servidor).
 */
public final class RegrasSegue {
    /** Exposição a partir da qual ela também vem de dia (degrau Fundo). */
    public static final int EXPOSICAO_DIA = 60;
    /** Abaixo disso as máquinas não pesam. */
    public static final int MAQUINAS_MIN = 8;
    /** A partir disso o peso das máquinas é o máximo. */
    public static final int MAQUINAS_MAX = 64;
    /** A partir disso as máquinas sobem a Exposição. */
    public static final int MAQUINAS_EXPOSICAO = 16;
    /** Teto da chance por tentativa (nunca vira certeza). */
    public static final double CHANCE_TETO = 0.5;
    /** Olhar direto: o ângulo entre o olhar do jogador e ela é de até ~14 graus. */
    public static final double DOT_OLHAR_DIRETO = 0.97;
    /** No campo de visão: até ~60 graus do centro da tela. */
    public static final double DOT_CAMPO = 0.5;
    /** Olhada direto por tanto tempo (ticks), ela some. */
    public static final int TICKS_SUMIR_OLHADA = 60;
    /** Olhada direto mais perto que isso, ela some na hora. */
    public static final double DISTANCIA_SUMIR_OLHADA = 8;
    /** Chegou a essa distância sem ser vista: pega. */
    public static final double DISTANCIA_PEGAR = 3;

    /** Por que ela sumiu (decide quando pode voltar). */
    public enum Motivo { PEGOU, VISTA, ATACADA, SEM_CAMINHO, DORMIU, AFASTADA, PERDEU_ALVO }

    private RegrasSegue() {}

    /** 0 a 1: quanto as máquinas em volta pesam. */
    public static double fatorMaquinas(int maquinas) {
        if (maquinas < MAQUINAS_MIN) return 0;
        return Math.min(1.0, (maquinas - MAQUINAS_MIN) / (double) (MAQUINAS_MAX - MAQUINAS_MIN));
    }

    /** Intensidade do contato com a Exposição por causa das máquinas (0 = nada). */
    public static float intensidadeMaquinas(int maquinas) {
        if (maquinas < MAQUINAS_EXPOSICAO) return 0f;
        return (float) (0.5 + 1.5 * fatorMaquinas(maquinas));
    }

    /**
     * Chance de ela aparecer numa tentativa (a cada ~10 s). Só à noite ou com Exposição alta.
     * @param base chance base (0,03 = 3%)
     */
    public static double chance(boolean noite, int exposicao, double fatorMaquinas, double base) {
        if (base <= 0) return 0;
        if (!noite && exposicao < EXPOSICAO_DIA) return 0;
        double e = Math.max(0, Math.min(100, exposicao));
        double f = Math.max(0, Math.min(1, fatorMaquinas));
        return Math.min(CHANCE_TETO, base * (1 + e / 100.0) * (1 + 2 * f));
    }

    /** Ticks até o próximo salto. {@code sorte} de 0 a 1. Mais máquina = mais rápido. */
    public static int intervaloSalto(double sorte, double fatorMaquinas) {
        double s = Math.max(0, Math.min(1, sorte));
        double f = Math.max(0, Math.min(1, fatorMaquinas));
        return (int) Math.max(30, Math.round((60 + 40 * s) * (1 - 0.5 * f)));
    }

    /** Quantos blocos ela avança num salto (3 a 8). */
    public static double passo(double sorte, double fatorMaquinas) {
        double s = Math.max(0, Math.min(1, sorte));
        double f = Math.max(0, Math.min(1, fatorMaquinas));
        return 3 + 3 * s + 2 * f;
    }

    /** Dano do golpe: com a vida cheia, nunca mata (sobra meio coração). */
    public static float dano(float base, float vida, float vidaMax) {
        if (!(base > 0)) return 0f;
        if (vida >= vidaMax - 0.01f) return Math.max(0f, Math.min(base, vidaMax - 1f));
        return base;
    }

    /** Some quando é olhada direto por ~3 s, ou olhada direto de perto. */
    public static boolean sumirOlhada(int ticksOlhada, boolean olhandoAgora, double distancia) {
        return ticksOlhada >= TICKS_SUMIR_OLHADA || (olhandoAgora && distancia < DISTANCIA_SUMIR_OLHADA);
    }

    /**
     * Ticks até poder voltar para o mesmo jogador.
     * @param protegido o jogador estava dormindo, ou num lugar claro com outros jogadores: dobra a espera
     */
    public static long recarga(Motivo motivo, int recargaMin, int voltaMin, boolean protegido) {
        long minuto = 20L * 60;
        long t = switch (motivo) {
            case PEGOU, AFASTADA, DORMIU -> recargaMin * minuto;
            case VISTA, ATACADA -> voltaMin * minuto;
            case SEM_CAMINHO -> 2 * minuto;
            case PERDEU_ALVO -> minuto;
        };
        if (protegido || motivo == Motivo.DORMIU) t *= 2;
        return Math.max(20, t);
    }
}
