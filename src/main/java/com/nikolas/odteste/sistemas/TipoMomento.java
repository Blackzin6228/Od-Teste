package com.nikolas.odteste.sistemas;

/**
 * Os "momentos" que o servidor avisa aos clientes por perto (docs/animacoes.md).
 * O cliente decide o que tocar (tela, som, partícula, corpo). A ordem é o número que vai na rede:
 * só acrescente no fim.
 */
public enum TipoMomento {
    /** Subiu de degrau de Exposição. valor = ordinal do Degrau. */
    DEGRAU,
    /** A Marca apareceu (estágio 1). */
    MARCA_APARECEU,
    /** A Marca trocou sozinha de elemento. */
    MARCA_TROCOU,
    /** A Marca subiu de estágio. valor = novo estágio. */
    MARCA_SUBIU,
    /** Lucidez chegou a 0. */
    SURTO;

    public static TipoMomento deNumero(int n) {
        TipoMomento[] v = values();
        return n >= 0 && n < v.length ? v[n] : null;
    }
}
