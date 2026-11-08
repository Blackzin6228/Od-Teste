package com.nikolas.odteste.dados;

import com.nikolas.odteste.elementos.Elemento;

/**
 * Faixas válidas de cada valor do jogador e as regras de "prender na faixa".
 * Java puro (sem Minecraft), testado em LimitesTest. Toda escrita de dado passa por aqui,
 * assim um comando, um rito ou um arquivo velho nunca deixam um valor impossível salvo.
 * O jogador nunca vê esses números: ele sente pelos efeitos (docs/sistemas.md).
 */
public final class Limites {
    public static final int EXPOSICAO_MIN = 0;
    public static final int EXPOSICAO_MAX = 100;

    /** Lucidez máxima de quem nunca transcendeu. */
    public static final int LUCIDEZ_MAX_PADRAO = 100;
    /** Piso da Lucidez máxima: mesmo pagando tudo, sempre sobra um fio. */
    public static final int LUCIDEZ_MAX_PISO = 10;

    public static final int ESTAGIO_MIN = 1;
    public static final int ESTAGIO_MAX = 4;

    public static final int TRANSCENDER_MIN = 0;
    public static final int TRANSCENDER_MAX = 4;

    private Limites() {}

    public static int prender(int valor, int min, int max) {
        return Math.max(min, Math.min(max, valor));
    }

    public static int exposicao(int valor) {
        return prender(valor, EXPOSICAO_MIN, EXPOSICAO_MAX);
    }

    public static int lucidezMax(int valor) {
        return prender(valor, LUCIDEZ_MAX_PISO, LUCIDEZ_MAX_PADRAO);
    }

    /** A Lucidez nunca passa da Lucidez máxima atual do jogador. */
    public static int lucidez(int valor, int lucidezMax) {
        return prender(valor, 0, lucidezMax(lucidezMax));
    }

    public static int transcender(int valor) {
        return prender(valor, TRANSCENDER_MIN, TRANSCENDER_MAX);
    }

    /**
     * Sem elemento não existe estágio (0). Com elemento, o estágio fica entre 1 e 4:
     * uma Marca "de estágio 0" não existe.
     */
    public static int estagioMarca(Elemento elemento, int estagio) {
        if (elemento == null || elemento == Elemento.NENHUM) return 0;
        return prender(estagio, ESTAGIO_MIN, ESTAGIO_MAX);
    }
}
