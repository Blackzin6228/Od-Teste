package com.nikolas.odteste.historia.vilacalada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RegrasVilaCaladaTest {
    @Test
    void soTocaDeNoite() {
        assertFalse(RegrasVilaCalada.noite(6_000), "meio-dia");
        assertTrue(RegrasVilaCalada.noite(18_000), "meia-noite");
        assertTrue(RegrasVilaCalada.noite(24_000L * 5 + 13_000), "noite de outro dia");
        assertFalse(RegrasVilaCalada.noite(23_000));
        assertFalse(RegrasVilaCalada.noite(-1_000), "hora negativa não quebra");
    }

    @Test
    void intervalosFicamNosLimites() {
        assertEquals(RegrasVilaCalada.TOQUE_MIN, RegrasVilaCalada.proximoToque(0));
        assertEquals(RegrasVilaCalada.TOQUE_MAX, RegrasVilaCalada.proximoToque(1));
        assertEquals(RegrasVilaCalada.TOQUE_MAX, RegrasVilaCalada.proximoToque(7), "sorteio fora de 0..1 é cortado");
        assertEquals(RegrasVilaCalada.OLHADA_MIN, RegrasVilaCalada.proximaOlhada(-3));
        assertTrue(RegrasVilaCalada.primeiroToque(0.5) < RegrasVilaCalada.TOQUE_MIN);
    }

    @Test
    void volumeSobeQuandoSeAndaParaLa() {
        float longe = RegrasVilaCalada.volumeLonge(1_200);
        float meio = RegrasVilaCalada.volumeLonge(600);
        float perto = RegrasVilaCalada.volumeLonge(60);
        assertTrue(longe < meio && meio < perto);
        assertEquals(RegrasVilaCalada.VOLUME_MINIMO, RegrasVilaCalada.volumeLonge(3_000), 1e-6);
        assertEquals(0f, RegrasVilaCalada.volumeLonge(RegrasVilaCalada.ALCANCE_SINO + 1), 1e-6);
    }

    @Test
    void depoisDeChegarSoOuveNaVila() {
        assertTrue(RegrasVilaCalada.ouve(1_000, false));
        assertFalse(RegrasVilaCalada.ouve(1_000, true));
        assertTrue(RegrasVilaCalada.ouve(50, true));
    }

    @Test
    void direcaoUnitariaESemDivisaoPorZero() {
        double[] d = RegrasVilaCalada.direcao(30, 40);
        assertEquals(0.6, d[0], 1e-9);
        assertEquals(0.8, d[1], 1e-9);
        double[] zero = RegrasVilaCalada.direcao(0, 0);
        assertEquals(0, zero[0]);
        assertEquals(0, zero[1]);
    }

    @Test
    void reconheceOTemplateDaVila() {
        assertTrue(RegrasVilaCalada.templateDaVila("odteste:vila_calada"));
        assertTrue(RegrasVilaCalada.templateDaVila("odteste:estruturas/vila_calada_decor"));
        assertTrue(RegrasVilaCalada.templateDaVila("ODTESTE:VILA_CALADA"));
        assertFalse(RegrasVilaCalada.templateDaVila("odteste:frigorifico"));
        assertFalse(RegrasVilaCalada.templateDaVila("odteste:nao_vila_calada"));
        assertFalse(RegrasVilaCalada.templateDaVila(null));
    }

    @Test
    void chavesSalvasBatemComOCodigo() throws Exception {
        // Se alguém renomear uma chave do arquivo, a lista (e o docs/ids.md) deixam de bater: o teste avisa.
        String codigo = Files.readString(Path.of("src/main/java/com/nikolas/odteste/historia/vilacalada/DadosVilaCalada.java"));
        for (String k : IdsVilaCalada.CHAVES) assertTrue(codigo.contains("\"" + k + "\""), "DadosVilaCalada não usa " + k);
    }
}
