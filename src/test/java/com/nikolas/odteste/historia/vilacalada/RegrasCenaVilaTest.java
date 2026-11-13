package com.nikolas.odteste.historia.vilacalada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.cenas.Cena;
import com.nikolas.odteste.cenas.RegrasCena;
import org.junit.jupiter.api.Test;

class RegrasCenaVilaTest {

    @Test
    void dentroDoMuroEhAContaDoClima() {
        assertTrue(RegrasClimaVila.dentro(0, 0, 0));
        assertFalse(RegrasClimaVila.dentro(0, 60, 0)); // voando bem alto
    }

    @Test
    void soComecaNoChao() {
        assertTrue(RegrasCenaVila.podeComecar(true, false, false));
        assertFalse(RegrasCenaVila.podeComecar(false, false, false)); // caindo ou nadando
        assertFalse(RegrasCenaVila.podeComecar(true, true, false)); // montado ou de barco
        assertFalse(RegrasCenaVila.podeComecar(false, false, true)); // de élitro
    }

    @Test
    void portaoComRotacaoConhecida() {
        double[] d = RegrasCenaVila.direcaoPortao(0, 1, 99, -99);
        assertEquals(0, d[0], 1e-9);
        assertEquals(1, d[1], 1e-9); // a rotação manda, não o jogador
        double[] oeste = RegrasCenaVila.direcaoPortao(-1, 0, 0, 0);
        assertEquals(-1, oeste[0], 1e-9);
    }

    @Test
    void portaoSemRotacaoVemDoJogador() {
        double[] d = RegrasCenaVila.direcaoPortao(null, null, 30, 40);
        assertEquals(0.6, d[0], 1e-9);
        assertEquals(0.8, d[1], 1e-9);
        double[] nada = RegrasCenaVila.direcaoPortao(null, null, 0, 0);
        assertEquals(0, nada[0], 1e-9);
        assertEquals(1, nada[1], 1e-9);
    }

    @Test
    void giroDoPortaoOlhaParaOCentro() {
        // Vila sem girar: portão ao sul do centro; quem entra olha para o norte (giro 180).
        double[] d = RegrasCenaVila.direcaoPortao(0, 1, 0, 0);
        float giro = RegrasCena.giroDe(-d[0], -d[1]);
        assertEquals(180f, Math.abs(giro), 1e-4);
        // A frente local (+z) aponta do portão para o centro.
        double[] f = RegrasCena.paraMundo(0, 0, 0, giro, new Cena.Local(0, 0, RegrasCenaVila.PORTAO_DO_CENTRO));
        assertEquals(-RegrasCenaVila.PORTAO_DO_CENTRO, f[2], 1e-6);
    }
}
