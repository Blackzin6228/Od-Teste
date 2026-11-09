package com.nikolas.odteste.blocos.capela;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RegrasCapelaTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/odteste");

    @Test
    void checaUmaVezACadaDoisSegundos() {
        int vezes = 0;
        for (long t = 0; t < 400; t++) {
            if (RegrasCapela.vezDeChecar(t, 7)) vezes++;
        }
        assertEquals(10, vezes);
    }

    @Test
    void deslocamentoNegativoNaoQuebra() {
        // pos.asLong() pode ser negativo: o floorMod mantém uma checagem a cada 40 ticks.
        int vezes = 0;
        for (long t = 0; t < 40; t++) {
            if (RegrasCapela.vezDeChecar(t, -123_456_789L)) vezes++;
        }
        assertEquals(1, vezes);
    }

    @Test
    void freioLiberaUmaVezACadaDoisSegundos() {
        assertTrue(RegrasCapela.passouIntervalo(null, 1000));
        assertFalse(RegrasCapela.passouIntervalo(1000L, 1000));
        assertFalse(RegrasCapela.passouIntervalo(1000L, 1039));
        assertTrue(RegrasCapela.passouIntervalo(1000L, 1040));
        // relógio voltou (mundo trocado): libera em vez de travar para sempre
        assertTrue(RegrasCapela.passouIntervalo(1000L, 10));
    }

    @Test
    void velaSoAcalmaQuemEstaPerto() {
        assertTrue(RegrasCapela.pertoDaVela(0));
        assertTrue(RegrasCapela.pertoDaVela(25));
        assertFalse(RegrasCapela.pertoDaVela(25.01));
        assertFalse(RegrasCapela.pertoDaVela(-1));
    }

    @Test
    void velaNaoViraLuzForteDoGrupo() {
        // Luz forte (Vigia) é 12: a vela não pode somar ao "grupo na luz" por conta própria.
        assertTrue(RegrasCapela.VELA_LUZ > 0 && RegrasCapela.VELA_LUZ < 12);
    }

    @Test
    void cadaBlocoTemEstadoModeloENomeNasDuasLinguas() throws Exception {
        String pt = Files.readString(ASSETS.resolve("lang/pt_br.json"));
        String en = Files.readString(ASSETS.resolve("lang/en_us.json"));
        for (String id : BlocosCapelaIds.IDS) {
            assertTrue(Files.exists(ASSETS.resolve("blockstates/" + id + ".json")), "falta o blockstate de " + id);
            assertTrue(Files.exists(ASSETS.resolve("models/block/" + id + ".json")), "falta o modelo do bloco " + id);
            assertTrue(Files.exists(ASSETS.resolve("models/item/" + id + ".json")), "falta o modelo do item " + id);
            assertTrue(pt.contains("\"block.odteste." + id + "\""), "falta o nome pt_br de " + id);
            assertTrue(en.contains("\"block.odteste." + id + "\""), "falta o nome en_us de " + id);
        }
    }
}
