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
 * Atenção: {@code "marcado"} (sem chaves) em {@code exposicao_minima} é o <b>degrau</b> de Exposição;
 * {@code {marcado}} (com chaves) dentro de {@code texto} é o <b>marcador</b> trocado pelo nome de quem tem mais
 * Exposição no grupo. Mesma palavra, coisas diferentes.
 * O limite de {@value #MAX_PAGINA} letras por página é aplicado na hora de montar, depois de trocar os marcadores
 * ({@link RegrasLivros#montar(Livro, int, java.util.Map, int)}); aqui só avisa no log se o arquivo já passa dele.
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
                Livro.Pagina p = pagina(ps.get(i));
                avisarSeGrande(p, "livro " + id + ", página " + (i + 1), aviso);
                paginas.add(p);
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
     * Avisa se a página, com tudo visível (texto + trechos escondidos + 2 letras de linha em branco entre eles),
     * já passa do limite no arquivo. Não corta: o corte é feito ao montar, depois de trocar os marcadores.
     */
    static boolean avisarSeGrande(Livro.Pagina p, String onde, Consumer<String> aviso) {
        int total = p.texto().length();
        int pedacos = p.texto().isEmpty() ? 0 : 1;
        for (Livro.Trecho t : p.escondidos()) {
            if (t.texto().isEmpty()) continue;
            total += t.texto().length() + (pedacos > 0 ? 2 : 0);
            pedacos++;
        }
        if (total <= MAX_PAGINA) return false;
        aviso.accept(onde + ": passa de " + MAX_PAGINA + " letras, o fim vai ser cortado no livro");
        return true;
    }

    private static int limiar(JsonObject o) {
        return o.has("exposicao_minima") ? RegrasLivros.limiar(o.get("exposicao_minima").getAsString()) : 0;
    }

    private static String texto(JsonObject o, String chave, String padrao) {
        return o.has(chave) && !o.get(chave).isJsonNull() ? o.get(chave).getAsString() : padrao;
    }
}
