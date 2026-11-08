package com.nikolas.odteste.sistemas;

/**
 * Degraus da Exposição (docs/sistemas.md, seção 1). O jogador nunca vê o número, só sente a subida.
 * Java puro, testado em RegrasTest.
 */
public enum Degrau {
    COMUM(0),
    TOCADO(20),
    MARCADO(40),
    FUNDO(60),
    A_BEIRA(80),
    ATRAVESSADO(100);

    /** Exposição mínima para estar neste degrau. */
    public final int minimo;

    Degrau(int minimo) {
        this.minimo = minimo;
    }

    public static Degrau de(int exposicao) {
        Degrau d = COMUM;
        for (Degrau x : values()) {
            if (exposicao >= x.minimo) d = x;
        }
        return d;
    }

    public boolean peloMenos(Degrau outro) {
        return ordinal() >= outro.ordinal();
    }
}
