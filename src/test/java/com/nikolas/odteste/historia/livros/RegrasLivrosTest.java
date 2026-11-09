package com.nikolas.odteste.historia.livros;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.sistemas.Degrau;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class RegrasLivrosTest {
    private static final int MEDIA = Degrau.MARCADO.minimo;
    private static final int ALTA = Degrau.FUNDO.minimo;

    private static Livro exemplo() {
        return new Livro("x", "T", "A", 3, List.of(
                new Livro.Pagina("um", 0, List.of()),
                new Livro.Pagina("dois", 0, List.of(new Livro.Trecho("vermelho", MEDIA))),
                new Livro.Pagina("tres", ALTA, List.of()),
                new Livro.Pagina("", 0, List.of(new Livro.Trecho("a", MEDIA), new Livro.Trecho("b", ALTA)))));
    }

    @Test
    void semExposicaoSoOTextoComum() {
        var p = RegrasLivros.montar(exemplo(), 0);
        assertEquals(3, p.size(), "a página com condição some");
        assertEquals(List.of(new RegrasLivros.Pedaco("um", false)), p.get(0));
        assertEquals(List.of(new RegrasLivros.Pedaco("dois", false)), p.get(1));
        assertTrue(p.get(2).isEmpty(), "página em branco continua existindo");
    }

    @Test
    void exposicaoMediaMostraOVermelho() {
        var p = RegrasLivros.montar(exemplo(), MEDIA);
        assertEquals(3, p.size());
        assertEquals(new RegrasLivros.Pedaco("vermelho", true), p.get(1).get(1));
        assertEquals(List.of(new RegrasLivros.Pedaco("a", true)), p.get(2));
    }

    @Test
    void exposicaoAltaMostraTudo() {
        var p = RegrasLivros.montar(exemplo(), ALTA);
        assertEquals(4, p.size());
        assertEquals("tres", p.get(2).get(0).texto());
        assertEquals(List.of(new RegrasLivros.Pedaco("a", true), new RegrasLivros.Pedaco("b", true)), p.get(3));
    }

    @Test
    void limiarAceitaNumeroEDegrau() {
        assertEquals(0, RegrasLivros.limiar(null));
        assertEquals(0, RegrasLivros.limiar(""));
        assertEquals(35, RegrasLivros.limiar("35"));
        assertEquals(100, RegrasLivros.limiar("500"));
        assertEquals(Degrau.MARCADO.minimo, RegrasLivros.limiar("Marcado"));
        assertEquals(Degrau.A_BEIRA.minimo, RegrasLivros.limiar("a_beira"));
        assertThrows(IllegalArgumentException.class, () -> RegrasLivros.limiar("media"));
    }

    @Test
    void marcadores() {
        Map<String, String> m = Map.of("jogador", "Ana", "grupo", "Ana e Bia", "marcado", "Bia");
        assertEquals("Ana, Ana e Bia, Bia.", RegrasLivros.substituir("{jogador}, {grupo}, {marcado}.", m));
        assertEquals("{parceiro} fica", RegrasLivros.substituir("{parceiro} fica", m), "desconhecido não some");
        assertEquals("sem nada", RegrasLivros.substituir("sem nada", m));
        assertEquals("{ aberto", RegrasLivros.substituir("{ aberto", m));
        // O valor trocado não é trocado de novo.
        assertEquals("{grupo}", RegrasLivros.substituir("{jogador}", Map.of("jogador", "{grupo}", "grupo", "X")));
    }

    @Test
    void juntarNomes() {
        assertEquals("", RegrasLivros.juntarNomes(List.of()));
        assertEquals("Ana", RegrasLivros.juntarNomes(List.of("Ana")));
        assertEquals("Ana e Bia", RegrasLivros.juntarNomes(List.of("Ana", "Bia")));
        assertEquals("Ana, Bia e Caio", RegrasLivros.juntarNomes(List.of("Ana", "Bia", "Caio")));
    }

    @Test
    void marcadoEhQuemTemMaisExposicao() {
        Map<String, Integer> e = new LinkedHashMap<>();
        assertEquals("eu", RegrasLivros.marcado(e, "eu"));
        e.put("Ana", 10);
        e.put("Bia", 50);
        e.put("Caio", 50);
        assertEquals("Bia", RegrasLivros.marcado(e, "eu"), "empate fica com o primeiro");
    }

    /**
     * O arquivo do Sineiro sem Gson (os testes rodam sem Minecraft): 14 páginas, só degraus que existem,
     * nenhum texto grande demais para uma página do livro.
     */
    @Test
    void arquivoDoSineiro() throws Exception {
        String json = Files.readString(Path.of("src/main/resources/data/odteste/odteste_livros/sineiro.json"));
        Matcher limiares = Pattern.compile("\"exposicao_minima\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        int n = 0;
        while (limiares.find()) {
            RegrasLivros.limiar(limiares.group(1));
            n++;
        }
        assertEquals(6, n, "5 trechos de Exposição média e 1 de alta");
        // No Sineiro, só os trechos escondidos têm condição: o resto dos "texto" são as páginas.
        long textos = Pattern.compile("\"texto\"\\s*:").matcher(json).results().count();
        assertEquals(14, textos - n);
        Matcher conteudo = Pattern.compile("\"texto\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        while (conteudo.find()) assertTrue(conteudo.group(1).length() <= 320, "texto grande demais: " + conteudo.group(1));
    }
}
