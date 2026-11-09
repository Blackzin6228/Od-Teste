package com.nikolas.odteste.historia.livros;

import java.util.List;

/**
 * Um livro de história lido de {@code data/<modid>/odteste_livros/<id>.json}. Java puro (testado em RegrasLivrosTest).
 *
 * @param geracao 0 original, 1 cópia, 2 cópia de cópia, 3 velho (não dá para copiar)
 */
public record Livro(String id, String titulo, String autor, int geracao, List<Pagina> paginas) {
    public Livro {
        paginas = List.copyOf(paginas);
        geracao = Math.max(0, Math.min(3, geracao));
    }

    /**
     * Uma página. Se {@code exposicaoMinima} > 0, a página inteira só existe para quem tem essa Exposição.
     * Os trechos escondidos aparecem em vermelho, depois do texto, cada um com a sua Exposição mínima.
     */
    public record Pagina(String texto, int exposicaoMinima, List<Trecho> escondidos) {
        public Pagina {
            texto = texto == null ? "" : texto;
            escondidos = List.copyOf(escondidos);
        }
    }

    /** Texto escondido (vermelho): só aparece com a Exposição mínima. */
    public record Trecho(String texto, int exposicaoMinima) {
        public Trecho {
            texto = texto == null ? "" : texto;
        }
    }
}
