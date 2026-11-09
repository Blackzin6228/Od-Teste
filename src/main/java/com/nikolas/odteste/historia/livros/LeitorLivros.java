package com.nikolas.odteste.historia.livros;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

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

    /** @throws IllegalArgumentException se o arquivo estiver errado (a mensagem diz onde) */
    public static Livro ler(String id, JsonElement json) {
        if (json == null || !json.isJsonObject()) throw new IllegalArgumentException("o livro precisa ser um objeto");
        JsonObject o = json.getAsJsonObject();
        String titulo = texto(o, "titulo", id);
        String autor = texto(o, "autor", "?");
        int geracao = o.has("geracao") ? o.get("geracao").getAsInt() : 0;
        if (!o.has("paginas") || !o.get("paginas").isJsonArray()) throw new IllegalArgumentException("falta \"paginas\"");
        JsonArray ps = o.getAsJsonArray("paginas");
        if (ps.size() > 100) throw new IllegalArgumentException("mais de 100 páginas");
        List<Livro.Pagina> paginas = new ArrayList<>();
        for (int i = 0; i < ps.size(); i++) {
            try {
                paginas.add(pagina(ps.get(i)));
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

    private static int limiar(JsonObject o) {
        return o.has("exposicao_minima") ? RegrasLivros.limiar(o.get("exposicao_minima").getAsString()) : 0;
    }

    private static String texto(JsonObject o, String chave, String padrao) {
        return o.has(chave) && !o.get(chave).isJsonNull() ? o.get(chave).getAsString() : padrao;
    }
}
