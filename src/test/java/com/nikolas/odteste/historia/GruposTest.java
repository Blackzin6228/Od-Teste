package com.nikolas.odteste.historia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GruposTest {
    private final UUID ana = UUID.randomUUID();
    private final UUID bia = UUID.randomUUID();

    @Test
    void nomeDeGrupo() {
        assertEquals(Optional.of("conguitos"), Grupos.normalizar(" Conguitos "));
        assertTrue(Grupos.normalizar("com espaço").isEmpty());
        assertTrue(Grupos.normalizar("").isEmpty());
        assertTrue(Grupos.normalizar("a".repeat(25)).isEmpty());
        assertTrue(Grupos.normalizar(null).isEmpty());
    }

    @Test
    void criarNaoRepete() {
        Grupos g = new Grupos();
        assertTrue(g.criar("Conguitos"));
        assertFalse(g.criar("conguitos"), "maiúscula não cria outro");
        assertFalse(g.criar("nome ruim"));
        assertEquals(1, g.todos().size());
    }

    @Test
    void progressoEhDoGrupo() {
        Grupos g = new Grupos();
        g.criar("a");
        g.adicionar("a", ana, "Ana");
        assertTrue(g.marcar("a", IdsHistoria.MARCO_PRIMEIRO_SINAL));
        assertFalse(g.marcar("a", IdsHistoria.MARCO_PRIMEIRO_SINAL), "marco repetido não conta de novo");
        // Quem entra depois já chega com o que o grupo descobriu.
        g.adicionar("a", bia, "Bia");
        assertTrue(g.grupoDe(bia).orElseThrow().tem(IdsHistoria.MARCO_PRIMEIRO_SINAL));
        assertTrue(g.tem("A", IdsHistoria.MARCO_PRIMEIRO_SINAL));
        assertTrue(g.desmarcar("a", IdsHistoria.MARCO_PRIMEIRO_SINAL));
        assertFalse(g.tem("a", IdsHistoria.MARCO_PRIMEIRO_SINAL));
    }

    @Test
    void progressoDeUmGrupoNaoVazaParaOutro() {
        Grupos g = new Grupos();
        g.criar("a");
        g.criar("b");
        g.marcar("a", IdsHistoria.MARCO_ENIGMA_SINO);
        assertFalse(g.tem("b", IdsHistoria.MARCO_ENIGMA_SINO));
        assertFalse(g.marcar("naoexiste", IdsHistoria.MARCO_ENIGMA_SINO));
    }

    @Test
    void jogadorFicaEmUmGrupoSo() {
        Grupos g = new Grupos();
        g.criar("a");
        g.criar("b");
        assertTrue(g.adicionar("a", ana, "Ana").isEmpty());
        assertEquals(Optional.of("a"), g.adicionar("b", ana, "Ana"));
        assertEquals("b", g.grupoDe(ana).orElseThrow().nome());
        assertTrue(g.grupo("a").orElseThrow().membros().isEmpty());
        assertTrue(g.adicionar("naoexiste", bia, "Bia").isEmpty());
        assertTrue(g.grupoDe(bia).isEmpty());
    }

    @Test
    void nomesNaOrdemEmQueEntraram() {
        Grupos g = new Grupos();
        g.criar("a");
        g.adicionar("a", ana, "Ana");
        g.adicionar("a", bia, "Bia");
        assertEquals(List.of("Ana", "Bia"), g.grupo("a").orElseThrow().nomes());
        g.atualizarNome(ana, "Ana2");
        assertEquals(List.of("Ana2", "Bia"), g.grupo("a").orElseThrow().nomes());
        assertTrue(g.tirar("a", ana));
        assertFalse(g.tirar("a", ana));
        assertEquals(List.of("Bia"), g.grupo("a").orElseThrow().nomes());
    }

    @Test
    void apagar() {
        Grupos g = new Grupos();
        g.criar("a");
        assertTrue(g.apagar("A"));
        assertFalse(g.apagar("a"));
        assertTrue(g.grupo("a").isEmpty());
    }
}
