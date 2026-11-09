package com.nikolas.odteste.historia.livros;

import com.nikolas.odteste.sistemas.Degrau;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Regras dos livros de história, sem Minecraft (testadas em RegrasLivrosTest): que páginas e trechos quem lê
 * enxerga, e a troca dos marcadores ({jogador}, {grupo}, {marcado}...).
 */
public final class RegrasLivros {
    /** Um pedaço de texto de uma página já montada. {@code escondido} = vai em vermelho. */
    public record Pedaco(String texto, boolean escondido) {}

    private RegrasLivros() {}

    /**
     * Lê a Exposição mínima do arquivo: um número (0 a 100) ou o nome de um degrau
     * ("comum", "tocado", "marcado", "fundo", "a_beira", "atravessado"). Vazio = 0.
     *
     * @throws IllegalArgumentException se não for nenhum dos dois
     */
    public static int limiar(String valor) {
        if (valor == null || valor.isBlank()) return 0;
        String v = valor.trim();
        try {
            return Math.max(0, Math.min(100, Integer.parseInt(v)));
        } catch (NumberFormatException naoEhNumero) {
            try {
                return Degrau.valueOf(v.toUpperCase(Locale.ROOT)).minimo;
            } catch (IllegalArgumentException semDegrau) {
                throw new IllegalArgumentException("Exposição mínima inválida: " + valor);
            }
        }
    }

    /**
     * Monta o livro para quem tem esta Exposição. Páginas com condição não cumprida somem; trechos escondidos
     * não cumpridos somem. Uma página sem nenhum pedaço fica em branco (lista vazia).
     */
    public static List<List<Pedaco>> montar(Livro livro, int exposicao) {
        List<List<Pedaco>> paginas = new ArrayList<>();
        for (Livro.Pagina p : livro.paginas()) {
            if (exposicao < p.exposicaoMinima()) continue;
            List<Pedaco> pedacos = new ArrayList<>();
            if (!p.texto().isEmpty()) pedacos.add(new Pedaco(p.texto(), false));
            for (Livro.Trecho t : p.escondidos()) {
                if (exposicao >= t.exposicaoMinima() && !t.texto().isEmpty()) pedacos.add(new Pedaco(t.texto(), true));
            }
            paginas.add(pedacos);
        }
        return paginas;
    }

    /**
     * Monta, troca os marcadores e só DEPOIS corta cada página em {@code max} letras (um nome comprido no lugar
     * de {grupo} pode passar do limite mesmo com o arquivo dentro dele).
     */
    public static List<List<Pedaco>> montar(Livro livro, int exposicao, Map<String, String> marcadores, int max) {
        List<List<Pedaco>> paginas = new ArrayList<>();
        for (List<Pedaco> pedacos : montar(livro, exposicao)) {
            List<Pedaco> trocados = new ArrayList<>(pedacos.size());
            for (Pedaco p : pedacos) trocados.add(new Pedaco(substituir(p.texto(), marcadores), p.escondido()));
            paginas.add(cortar(trocados, max));
        }
        return paginas;
    }

    /**
     * Corta a página em {@code max} letras, contando as 2 letras da linha em branco entre um pedaço e outro
     * (é assim que a página é escrita no livro). O que não cabe some, na ordem.
     */
    public static List<Pedaco> cortar(List<Pedaco> pedacos, int max) {
        List<Pedaco> saida = new ArrayList<>(pedacos.size());
        int sobra = max;
        for (Pedaco p : pedacos) {
            int cabe = sobra - (saida.isEmpty() ? 0 : 2);
            if (cabe <= 0) break;
            String t = p.texto().length() > cabe ? p.texto().substring(0, cabe) : p.texto();
            saida.add(new Pedaco(t, p.escondido()));
            sobra = cabe - t.length();
        }
        return saida;
    }

    /**
     * Troca {chave} pelo valor do mapa, numa passada só (o valor trocado nunca é trocado de novo).
     * Marcador desconhecido fica como está, para quem revisa perceber.
     */
    public static String substituir(String texto, Map<String, String> valores) {
        if (texto == null || texto.indexOf('{') < 0) return texto;
        StringBuilder sb = new StringBuilder(texto.length() + 16);
        int i = 0;
        while (i < texto.length()) {
            char c = texto.charAt(i);
            if (c == '{') {
                int fim = texto.indexOf('}', i + 1);
                if (fim > i) {
                    String chave = texto.substring(i + 1, fim);
                    String valor = valores.get(chave);
                    if (valor != null) {
                        sb.append(valor);
                        i = fim + 1;
                        continue;
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    /** "Ana", "Ana e Bia", "Ana, Bia e Caio". Lista vazia = "". */
    public static String juntarNomes(List<String> nomes) {
        if (nomes.isEmpty()) return "";
        if (nomes.size() == 1) return nomes.get(0);
        return String.join(", ", nomes.subList(0, nomes.size() - 1)) + " e " + nomes.get(nomes.size() - 1);
    }

    /**
     * O {marcado}: quem tem a maior Exposição. Empate fica com o primeiro da lista (ordem do grupo).
     * Lista vazia = {@code reserva}.
     */
    public static String marcado(Map<String, Integer> exposicaoPorNome, String reserva) {
        String melhor = null;
        int maior = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> e : exposicaoPorNome.entrySet()) {
            if (e.getValue() > maior) {
                maior = e.getValue();
                melhor = e.getKey();
            }
        }
        return melhor == null ? reserva : melhor;
    }
}
