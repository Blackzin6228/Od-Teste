package com.nikolas.odteste.historia;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Código Morse internacional de verdade (o enigma do sino se resolve fora do jogo, com papel e caneta).
 * Java puro, testado em MorseTest.
 *
 * Tempo padrão do Morse, em "unidades": ponto 1, traço 3, espaço dentro da letra 1, entre letras 3,
 * entre palavras 7.
 */
public final class Morse {
    private static final Map<Character, String> TABELA = Map.ofEntries(
            Map.entry('A', ".-"), Map.entry('B', "-..."), Map.entry('C', "-.-."), Map.entry('D', "-.."),
            Map.entry('E', "."), Map.entry('F', "..-."), Map.entry('G', "--."), Map.entry('H', "...."),
            Map.entry('I', ".."), Map.entry('J', ".---"), Map.entry('K', "-.-"), Map.entry('L', ".-.."),
            Map.entry('M', "--"), Map.entry('N', "-."), Map.entry('O', "---"), Map.entry('P', ".--."),
            Map.entry('Q', "--.-"), Map.entry('R', ".-."), Map.entry('S', "..."), Map.entry('T', "-"),
            Map.entry('U', "..-"), Map.entry('V', "...-"), Map.entry('W', ".--"), Map.entry('X', "-..-"),
            Map.entry('Y', "-.--"), Map.entry('Z', "--.."),
            Map.entry('0', "-----"), Map.entry('1', ".----"), Map.entry('2', "..---"), Map.entry('3', "...--"),
            Map.entry('4', "....-"), Map.entry('5', "....."), Map.entry('6', "-...."), Map.entry('7', "--..."),
            Map.entry('8', "---.."), Map.entry('9', "----."));

    public static final int PONTO = 1;
    public static final int TRACO = 3;
    public static final int ESPACO_SINAL = 1;
    public static final int ESPACO_LETRA = 3;
    public static final int ESPACO_PALAVRA = 7;

    /** Um toque: começa na unidade {@code inicio} e é traço ou ponto. */
    public record Toque(int inicio, boolean traco) {}

    /** A sequência inteira de uma frase, em unidades. {@code duracao} vai até o fim do último sinal. */
    public record Sequencia(List<Toque> toques, int duracao) {}

    private Morse() {}

    /** Tira acento e deixa só letras, números e espaço, em maiúsculas ("Não" vira "NAO"). */
    public static String limpar(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9 ]", " ").trim().replaceAll(" +", " ");
    }

    /** Frase em pontos e traços: letras separadas por espaço, palavras por " / ". */
    public static String codificar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (String palavra : limpar(texto).split(" ")) {
            if (palavra.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" / ");
            StringBuilder letras = new StringBuilder();
            for (char c : palavra.toCharArray()) {
                if (letras.length() > 0) letras.append(' ');
                letras.append(TABELA.get(c));
            }
            sb.append(letras);
        }
        return sb.toString();
    }

    /** O contrário de {@link #codificar}. Código que não existe vira "?". */
    public static String decodificar(String codigo) {
        if (codigo == null || codigo.isBlank()) return "";
        StringBuilder sb = new StringBuilder();
        for (String palavra : codigo.trim().split("\\s*/\\s*")) {
            if (sb.length() > 0) sb.append(' ');
            for (String letra : palavra.trim().split("\\s+")) {
                sb.append(TABELA.entrySet().stream().filter(e -> e.getValue().equals(letra))
                        .map(e -> String.valueOf(e.getKey())).findFirst().orElse("?"));
            }
        }
        return sb.toString();
    }

    /** Quando cada toque começa, em unidades de tempo do Morse. */
    public static Sequencia sequencia(String texto) {
        List<Toque> toques = new ArrayList<>();
        int t = 0;
        boolean primeiraPalavra = true;
        for (String palavra : limpar(texto).split(" ")) {
            if (palavra.isEmpty()) continue;
            if (!primeiraPalavra) t += ESPACO_PALAVRA - ESPACO_LETRA; // o espaço de letra já foi somado
            primeiraPalavra = false;
            for (char c : palavra.toCharArray()) {
                String codigo = TABELA.get(c);
                for (int i = 0; i < codigo.length(); i++) {
                    boolean traco = codigo.charAt(i) == '-';
                    toques.add(new Toque(t, traco));
                    t += traco ? TRACO : PONTO;
                    if (i < codigo.length() - 1) t += ESPACO_SINAL;
                }
                t += ESPACO_LETRA;
            }
        }
        int duracao = toques.isEmpty() ? 0 : t - ESPACO_LETRA;
        return new Sequencia(List.copyOf(toques), duracao);
    }
}
