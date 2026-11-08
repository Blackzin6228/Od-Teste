package com.nikolas.odteste.marcas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** A camada da Marca de Sangue no corpo: cresce com o estágio e fica só onde deve. */
class PadraoVeiasTest {

    private static int pintados(int[] img) {
        int n = 0;
        for (int c : img) if (c != 0) n++;
        return n;
    }

    @Test
    void semMarcaNaoDesenhaNada() {
        assertEquals(0, pintados(PadraoVeias.gerar(0)));
        assertEquals(0, pintados(PadraoVeias.gerar(-3)));
        assertEquals(PadraoVeias.LADO * PadraoVeias.LADO, PadraoVeias.gerar(1).length);
    }

    @Test
    void cadaEstagioTemTudoDoAnteriorEMais() {
        for (int s = 1; s <= 4; s++) {
            int[] antes = PadraoVeias.gerar(s - 1);
            int[] agora = PadraoVeias.gerar(s);
            assertTrue(pintados(agora) > pintados(antes), "estágio " + s + " precisa crescer");
            for (int i = 0; i < antes.length; i++) {
                if (antes[i] != 0) assertTrue(agora[i] != 0, "estágio " + s + " apagou um pixel do anterior");
            }
        }
    }

    @Test
    void veiasSoNasRegioesLiberadas() {
        for (int s = 1; s <= 4; s++) {
            int[] img = PadraoVeias.gerar(s);
            for (int y = 0; y < PadraoVeias.LADO; y++) {
                for (int x = 0; x < PadraoVeias.LADO; x++) {
                    if (img[y * PadraoVeias.LADO + x] != 0) {
                        assertTrue(PadraoVeias.permitido(x, y, s), "pixel fora do lugar em " + x + "," + y + " (estágio " + s + ")");
                    }
                }
            }
        }
    }

    @Test
    void estagio1SoNoBracoDireito() {
        int[] img = PadraoVeias.gerar(1);
        for (int y = 0; y < PadraoVeias.LADO; y++) {
            for (int x = 0; x < PadraoVeias.LADO; x++) {
                if (img[y * PadraoVeias.LADO + x] != 0) assertTrue(x >= 40 && x < 56 && y >= 20 && y < 32);
            }
        }
    }

    @Test
    void sempreIgualParaTodoMundo() {
        assertArrayEquals(PadraoVeias.gerar(3), PadraoVeias.gerar(3));
        assertArrayEquals(PadraoVeias.gerar(9), PadraoVeias.gerar(4), "estágio fora da faixa é preso");
    }
}
