package com.nikolas.odteste.sistemas;

/**
 * Ritmo da Lucidez: cai conforme as coisas acontecem, volta devagar. Java puro, testado em RegrasTest.
 *
 * - Cai com dano (comum pesa pouco, paranormal pesa mais) e com os abalos do conteúdo (Abalo).
 * - Volta descansando (parado, sem tomar dano), com luz forte e o grupo perto, com comida quente e dormindo.
 * O Vigia do servidor checa a cada 2 s ("checagem").
 */
public final class RegrasLucidez {
    /** Passo do Vigia: uma checagem por jogador a cada 40 ticks (2 s). */
    public static final int TICKS_POR_CHECAGEM = 40;

    // ---------- dano ----------
    /** Dano comum: 0,1 de Lucidez por ponto de dano (1 Lucidez a cada 5 corações). */
    public static final int DANO_COMUM_CENTESIMOS = 10;
    /** Dano paranormal: 0,6 de Lucidez por ponto de dano. */
    public static final int DANO_PARANORMAL_CENTESIMOS = 60;
    /** Teto por golpe, em centésimos: um golpe comum tira no máximo 2; um paranormal, no máximo 15. */
    public static final int TETO_GOLPE_COMUM = 200;
    public static final int TETO_GOLPE_PARANORMAL = 1500;

    // ---------- recuperação ----------
    /** Descanso: sem tomar dano há 60 s (30 checagens) e parado... */
    public static final int DESCANSO_SEM_DANO = 30;
    /** ...ganha +1 a cada 20 s (10 checagens). Parado = andou menos que isto (blocos) desde a última checagem. */
    public static final int DESCANSO_INTERVALO = 10;
    public static final double DESCANSO_PARADO = 0.5;
    /** Luz forte e outro jogador perto: +1 a cada 10 s (5 checagens). */
    public static final int GRUPO_INTERVALO = 5;
    /** Comer ensopado ou sopa. */
    public static final int GANHO_COMIDA_QUENTE = 8;

    // ---------- surto ----------
    /** Depois do surto, a Lucidez volta para 15% da máxima. */
    public static final int SURTO_VOLTA_PERCENTO = 15;
    /** Duração do desmaio (Cegueira + Lentidão forte): 5 s. */
    public static final int SURTO_TICKS = 100;
    /** Proteção depois do surto: 20 s em que nada tira Lucidez (não surta em cadeia no meio da luta). */
    public static final int PROTECAO_SURTO_TICKS = 20 * 20;

    private RegrasLucidez() {}

    /** Centésimos de Lucidez que um golpe tira. Dano negativo, zero ou NaN não tira nada. */
    public static int perdaPorDano(float dano, boolean paranormal) {
        if (!(dano > 0)) return 0;
        int porPonto = paranormal ? DANO_PARANORMAL_CENTESIMOS : DANO_COMUM_CENTESIMOS;
        int teto = paranormal ? TETO_GOLPE_PARANORMAL : TETO_GOLPE_COMUM;
        return (int) Math.min(teto, Math.round((double) dano * porPonto));
    }

    /** Resultado de juntar centésimos: quantos pontos inteiros saem agora e o que sobra (0–99). */
    public record Acumulo(int pontos, int resto) {}

    public static Acumulo acumular(int resto, int centesimos) {
        long total = Math.max(0, resto) + (long) Math.max(0, centesimos);
        return new Acumulo((int) (total / 100), (int) (total % 100));
    }

    /** Ganha 1 descansando nesta checagem? */
    public static boolean recuperaDescansando(int checagensSemDano, int checagensParado) {
        return checagensSemDano >= DESCANSO_SEM_DANO
                && checagensParado > 0 && checagensParado % DESCANSO_INTERVALO == 0;
    }

    /** Lucidez depois do surto: 15% da máxima, nunca 0 (senão surtaria de novo). */
    public static int aposSurto(int maxima) {
        return Math.max(1, Math.round(maxima * SURTO_VOLTA_PERCENTO / 100f));
    }

    /** Dormir a noite inteira devolve metade da máxima. */
    public static int ganhoAoDormir(int maxima) {
        return Math.max(1, maxima / 2);
    }
}
