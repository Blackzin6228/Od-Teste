package com.nikolas.odteste.sistemas;

/**
 * Faixas da Lucidez, em fração da máxima (docs/sistemas.md, seção 2), para funcionar igual para quem transcendeu.
 * Acima de 70% Firme, 40–70% Inquieto, 15–40% Abalado, 1–15% Quebrando, 0 Surto.
 * Java puro, testado em RegrasTest.
 */
public enum FaixaLucidez {
    FIRME,
    INQUIETO,
    ABALADO,
    QUEBRANDO,
    SURTO;

    public static FaixaLucidez de(int lucidez, int maxima) {
        if (lucidez <= 0) return SURTO;
        int max = Math.max(1, maxima);
        // Conta em inteiros (lucidez*100 contra max*limite) para não errar na borda por arredondamento.
        long l = lucidez * 100L;
        if (l > max * 70L) return FIRME;
        if (l >= max * 40L) return INQUIETO;
        if (l >= max * 15L) return ABALADO;
        return QUEBRANDO;
    }

    /** Quanto maior, pior. Firme = 0. */
    public int gravidade() {
        return ordinal();
    }
}
