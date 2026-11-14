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
    void depoisDeChegarOSinoPara() {
        assertTrue(RegrasVilaCalada.ouve(1_000, false));
        assertFalse(RegrasVilaCalada.ouve(RegrasVilaCalada.ALCANCE_SINO + 1, false));
        assertFalse(RegrasVilaCalada.ouve(1_000, true));
        assertFalse(RegrasVilaCalada.ouve(10, true), "nem dentro da vila: parou de vez");
    }

    @Test
    void sinoERaro() {
        // 2 a 4 minutos entre toques: no máximo uns 4 por noite (noite = 10.000 ticks).
        assertTrue(RegrasVilaCalada.TOQUE_MIN >= 2_400);
        int noite = (int) (RegrasVilaCalada.NOITE_FIM - RegrasVilaCalada.NOITE_INICIO);
        assertTrue(noite / RegrasVilaCalada.TOQUE_MIN <= 4);
        assertTrue(RegrasVilaCalada.TOM_GRAVE >= 0.5f && RegrasVilaCalada.TOM_RACHA <= 0.7f);
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
    void soOTemplatePrincipalAncoraSozinho() {
        assertTrue(RegrasVilaCalada.templateDaVila("odteste:vila_calada"));
        assertTrue(RegrasVilaCalada.templateDaVila("ODTESTE:VILA_CALADA"));
        assertFalse(RegrasVilaCalada.templateDaVila("odteste:vila_calada_decor"), "variação não ancora sozinha");
        assertFalse(RegrasVilaCalada.templateDaVila("odteste:estruturas/vila_calada"));
        assertFalse(RegrasVilaCalada.templateDaVila("outro:vila_calada"));
        assertFalse(RegrasVilaCalada.templateDaVila("odteste:frigorifico"));
        assertFalse(RegrasVilaCalada.templateDaVila(null));
    }

    @Test
    void todoTextoSalvoEstaEmIdsMd() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        assertTrue(ids.contains("`" + IdsVilaCalada.LUGAR_VILA_CALADA + "`"), "falta o lugar vila_calada em docs/ids.md");
        assertTrue(ids.contains("`" + IdsVilaCalada.MARCO_CHEGOU + "`"), "falta o marco vila_calada em docs/ids.md");
        assertTrue(ids.contains("`" + IdsVilaCalada.ARQUIVO + "`"), "falta o arquivo odteste_vila_calada em docs/ids.md");
        assertTrue(ids.contains("`odteste:" + IdsVilaCalada.MODIFICADOR_ALDEAO_VIRADO + "`"),
                "falta odteste:aldeao_virado em docs/ids.md");
        assertTrue(ids.contains("`" + IdsVilaCalada.TEMPLATE + "`"), "falta o template odteste:vila_calada em docs/ids.md");
        for (String k : IdsVilaCalada.CHAVES) assertTrue(ids.contains("`" + k + "`"), "falta a chave " + k + " em docs/ids.md");
    }

    @Test
    void chavesSalvasBatemComOCodigo() throws Exception {
        // Se alguém renomear uma chave do arquivo, a lista (e o docs/ids.md) deixam de bater: o teste avisa.
        String codigo = Files.readString(Path.of("src/main/java/com/nikolas/odteste/historia/vilacalada/DadosVilaCalada.java"));
        for (String k : IdsVilaCalada.CHAVES) assertTrue(codigo.contains("\"" + k + "\""), "DadosVilaCalada não usa " + k);
    }
}
