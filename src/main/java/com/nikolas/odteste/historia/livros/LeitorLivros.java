package com.nikolas.odteste.historia.livros;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lê o JSON de um livro. Formato (todos os campos de condição são opcionais):
 *
 * <pre>
 * {
 *   "titulo": "Livro do Sineiro", "autor": "o Sineiro", "geracao": 3,
 *   "paginas": [
 *     { "texto": "Texto que todos leem." },
 *     { "texto": "Texto.", "escondidos": [ { "texto": "Em vermelho.", "exposicao_minima": "marcado" } ] },
 *     { "texto": "Página que só existe com Exposição.", "exposicao_minima": 60 }
 *   ]
 * }
 * </pre>
 *
 * {@code exposicao_minima} aceita número (0 a 100) ou nome de degrau (ver {@link RegrasLivros#limiar}).
 * Só usa Gson (sem Minecraft), para dar para testar.
 */
public final class LeitorLivros {
    private LeitorLivros() {}

    /** Limite de letras de uma página do livro vanilla (o mesmo do livro e pena). Texto + trechos escondidos. */
    public static final int MAX_PAGINA = 1024;
    /** Limite do título do livro vanilla. */
    public static final int MAX_TITULO = 32;

    /** Sem aviso (para teste). */
    public static Livro ler(String id, JsonElement json) {
        return ler(id, json, aviso -> {});
    }

    /**
     * @param aviso recebe uma mensagem quando algo é cortado por passar do limite
     * @throws IllegalArgumentException se o arquivo estiver errado (a mensagem diz onde)
     */
    public static Livro ler(String id, JsonElement json, Consumer<String> aviso) {
        if (json == null || !json.isJsonObject()) throw new IllegalArgumentException("o livro precisa ser um objeto");
        JsonObject o = json.getAsJsonObject();
        String titulo = texto(o, "titulo", id);
        if (titulo.length() > MAX_TITULO) {
            aviso.accept("livro " + id + ": título com mais de " + MAX_TITULO + " letras, cortado");
            titulo = titulo.substring(0, MAX_TITULO);
        }
        String autor = texto(o, "autor", "?");
        int geracao = o.has("geracao") ? o.get("geracao").getAsInt() : 0;
        if (!o.has("paginas") || !o.get("paginas").isJsonArray()) throw new IllegalArgumentException("falta \"paginas\"");
        JsonArray ps = o.getAsJsonArray("paginas");
        if (ps.size() > 100) throw new IllegalArgumentException("mais de 100 páginas");
        List<Livro.Pagina> paginas = new ArrayList<>();
        for (int i = 0; i < ps.size(); i++) {
            try {
                paginas.add(cortar(pagina(ps.get(i)), "livro " + id + ", página " + (i + 1), aviso));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("página " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return new Livro(id, titulo, autor, geracao, paginas);
    }

    private static Livro.Pagina pagina(JsonElement e) {
        if (e.isJsonPrimitive()) return new Livro.Pagina(e.getAsString(), 0, List.of());
        JsonObject p = e.getAsJsonObject();
        List<Livro.Trecho> escondidos = new ArrayList<>();
        if (p.has("escondidos")) {
            for (JsonElement t : p.getAsJsonArray("escondidos")) {
                JsonObject to = t.getAsJsonObject();
                escondidos.add(new Livro.Trecho(texto(to, "texto", ""), limiar(to)));
            }
        }
        return new Livro.Pagina(texto(p, "texto", ""), limiar(p), escondidos);
    }

    /**
     * Corta a página no limite do livro vanilla: primeiro o texto, depois os trechos escondidos na ordem
     * (cada trecho conta mais 2 letras, a linha em branco antes dele). O que não cabe some, com aviso.
     */
    static Livro.Pagina cortar(Livro.Pagina p, String onde, Consumer<String> aviso) {
        int sobra = MAX_PAGINA;
        String texto = p.texto();
        boolean cortou = false;
        if (texto.length() > sobra) {
            texto = texto.substring(0, sobra);
            cortou = true;
        }
        sobra -= texto.length();
        List<Livro.Trecho> escondidos = new ArrayList<>();
        for (Livro.Trecho t : p.escondidos()) {
            int cabe = sobra - (texto.isEmpty() && escondidos.isEmpty() ? 0 : 2);
            if (cabe <= 0) {
                cortou = true;
                continue;
            }
            String tt = t.texto();
            if (tt.length() > cabe) {
                tt = tt.substring(0, cabe);
                cortou = true;
            }
            escondidos.add(new Livro.Trecho(tt, t.exposicaoMinima()));
            sobra = cabe - tt.length();
        }
        if (!cortou) return p;
        aviso.accept(onde + ": passou de " + MAX_PAGINA + " letras, o fim foi cortado");
        return new Livro.Pagina(texto, p.exposicaoMinima(), escondidos);
    }

    private static int limiar(JsonObject o) {
        return o.has("exposicao_minima") ? RegrasLivros.limiar(o.get("exposicao_minima").getAsString()) : 0;
    }

    private static String texto(JsonObject o, String chave, String padrao) {
        return o.has(chave) && !o.get(chave).isJsonNull() ? o.get(chave).getAsString() : padrao;
    }
}
