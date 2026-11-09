package com.nikolas.odteste.sistemas;

/**
 * O que o jogador lê ao digitar /exposicao ou /lucidez sem nada: um símbolo e uma frase da faixa, nunca o número.
 * Frases da Lore (docs/horror.md, seção 3): chaves {@code odteste.horror.<sistema>.<faixa>.<1-3>}, sorteadas entre
 * as 3 variações, nunca a mesma duas vezes seguidas. Os símbolos são provisórios. Java puro, testado em RegrasHorrorTest.
 */
public final class FrasesFaixa {
    /** Quantas variações cada faixa tem. */
    public static final int VARIACOES = 3;

    private static final String[] GRUPO_EXPOSICAO = {"comum", "baixa", "media", "alta", "critica"};
    private static final String[] GRUPO_LUCIDEZ = {"firme", "alta", "media", "baixa", "surto"};

    private FrasesFaixa() {}

    /** Nome da faixa no horror.md: Comum, Baixa (Tocado), Média (Marcado), Alta (Fundo), Crítica (À beira/Atravessado). */
    public static String grupo(Degrau d) {
        return GRUPO_EXPOSICAO[RegrasHorror.faixa(d)];
    }

    /** Firme, Alta (Inquieto), Média (Abalado), Baixa (Quebrando), Surto. */
    public static String grupo(FaixaLucidez f) {
        return GRUPO_LUCIDEZ[f.ordinal()];
    }

    /** @param variacao de 1 a {@link #VARIACOES} */
    public static String chave(Degrau d, int variacao) {
        return "odteste.horror.exposicao." + grupo(d) + "." + variacao;
    }

    public static String chave(FaixaLucidez f, int variacao) {
        return "odteste.horror.lucidez." + grupo(f) + "." + variacao;
    }

    /** Sorteia de 1 a 3, diferente de {@code anterior} (0 = nenhuma ainda). */
    public static int sortear(int anterior, RegrasHorror.Sorteio s) {
        if (anterior < 1 || anterior > VARIACOES) return 1 + s.ate(VARIACOES);
        int v = 1 + s.ate(VARIACOES - 1); // 1 ou 2
        return v >= anterior ? v + 1 : v;
    }

    /** O círculo vai enchendo conforme o paranormal toca você. */
    public static String simbolo(Degrau d) {
        return switch (d) {
            case COMUM -> "○";
            case TOCADO -> "◔";
            case MARCADO -> "◑";
            case FUNDO -> "◕";
            case A_BEIRA -> "●";
            case ATRAVESSADO -> "◉";
        };
    }

    /** O losango vai se esvaziando conforme a cabeça cede. */
    public static String simbolo(FaixaLucidez f) {
        return switch (f) {
            case FIRME -> "◆";
            case INQUIETO -> "◈";
            case ABALADO -> "◇";
            case QUEBRANDO -> "⋄";
            case SURTO -> "✕";
        };
    }

    /** Cor do símbolo (RGB): da cinza para o vermelho-escuro. */
    public static int cor(Degrau d) {
        return switch (d) {
            case COMUM -> 0xAAAAAA;
            case TOCADO -> 0xB09090;
            case MARCADO -> 0xA86060;
            case FUNDO -> 0x9A3030;
            case A_BEIRA -> 0x8B0000;
            case ATRAVESSADO -> 0x5A0000;
        };
    }

    /** Cor do símbolo (RGB): do branco frio para o cinza apagado. */
    public static int cor(FaixaLucidez f) {
        return switch (f) {
            case FIRME -> 0xD8E4F0;
            case INQUIETO -> 0xA8B8C8;
            case ABALADO -> 0x7E8C99;
            case QUEBRANDO -> 0x5A5F66;
            case SURTO -> 0x3A3A3A;
        };
    }
}
