package com.nikolas.odteste.sistemas;

import java.util.Locale;

/**
 * O que o jogador lê ao digitar /exposicao ou /lucidez sem nada: um símbolo e uma frase da faixa, nunca o número.
 * Os textos ficam no lang (chaves odteste.frase.*), com texto provisório até a Lore escrever (docs/horror.md).
 * Os símbolos também são provisórios. Java puro, testado em RegrasHorrorTest.
 */
public final class FrasesFaixa {
    private FrasesFaixa() {}

    public static String chave(Degrau d) {
        return "odteste.frase.exposicao." + d.name().toLowerCase(Locale.ROOT);
    }

    public static String chave(FaixaLucidez f) {
        return "odteste.frase.lucidez." + f.name().toLowerCase(Locale.ROOT);
    }

    /** O círculo vai enchendo conforme o paranormal toca você. */
    public static String simbolo(Degrau d) {
        return switch (d) {
            case COMUM -> "○";       // ○
            case TOCADO -> "◔";      // ◔
            case MARCADO -> "◑";     // ◑
            case FUNDO -> "◕";       // ◕
            case A_BEIRA -> "●";     // ●
            case ATRAVESSADO -> "◉"; // ◉
        };
    }

    /** O losango vai se esvaziando conforme a cabeça cede. */
    public static String simbolo(FaixaLucidez f) {
        return switch (f) {
            case FIRME -> "◆";     // ◆
            case INQUIETO -> "◈";  // ◈
            case ABALADO -> "◇";   // ◇
            case QUEBRANDO -> "⋄"; // ⋄
            case SURTO -> "✕";     // ✕
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
