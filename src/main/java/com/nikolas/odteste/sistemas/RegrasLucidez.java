package com.nikolas.odteste.sistemas;

/**
 * Ritmo da Lucidez (cai rápido, volta devagar). Java puro, testado em RegrasTest.
 * Tudo em "checagens" de 2 segundos, que é o passo do Vigia no servidor.
 */
public final class RegrasLucidez {
    /** Passo do Vigia: uma checagem por jogador a cada 40 ticks (2 s). */
    public static final int TICKS_POR_CHECAGEM = 40;

    /** Escuro total sozinho: começa a pesar depois de 30 s (15 checagens)... */
    public static final int ESCURO_CARENCIA = 15;
    /** ...e daí tira 1 de Lucidez a cada 6 s (3 checagens). De 100 a 0 em uns 10 minutos no escuro. */
    public static final int ESCURO_INTERVALO = 3;
    /** A cada 60 s seguidos no escuro (30 checagens), um pouco de Exposição. */
    public static final int ESCURO_EXPOSICAO = 30;

    /** Luz forte e outro jogador perto: +1 a cada 10 s (5 checagens). */
    public static final int GRUPO_INTERVALO = 5;
    /** Mob hostil à noite rende Exposição no máximo a cada 30 s (15 checagens). */
    public static final int MOB_INTERVALO = 15;

    /** Comer ensopado ou sopa. */
    public static final int GANHO_COMIDA_QUENTE = 8;

    /** Depois do surto, a Lucidez volta para 15% da máxima. */
    public static final int SURTO_VOLTA_PERCENTO = 15;
    /** Duração do desmaio (Cegueira + Lentidão forte): 5 s. */
    public static final int SURTO_TICKS = 100;

    private RegrasLucidez() {}

    /** Quanto a Lucidez perde nesta checagem, dado há quantas checagens seguidas o jogador está no escuro sozinho. */
    public static int perdaNoEscuro(int checagensSeguidas) {
        if (checagensSeguidas <= ESCURO_CARENCIA) return 0;
        return (checagensSeguidas - ESCURO_CARENCIA) % ESCURO_INTERVALO == 0 ? 1 : 0;
    }

    public static boolean exposicaoNoEscuro(int checagensSeguidas) {
        return checagensSeguidas > 0 && checagensSeguidas % ESCURO_EXPOSICAO == 0;
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
