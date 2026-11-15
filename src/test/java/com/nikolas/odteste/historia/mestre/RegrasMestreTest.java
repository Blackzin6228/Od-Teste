package com.nikolas.odteste.historia.mestre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasMestreTest {
    @Test
    void listaDaConfig() {
        assertEquals(List.of("Blackzin6228", "Outro"), RegrasMestre.nomes(" Blackzin6228, Outro ,"));
        assertEquals(List.of("a", "b"), RegrasMestre.nomes("a b"));
        assertTrue(RegrasMestre.nomes("").isEmpty());
        assertTrue(RegrasMestre.nomes(null).isEmpty());
    }

    @Test
    void soOpDaListaEhMestre() {
        assertFalse(RegrasMestre.ehMestre(false, "Blackzin6228", List.of()), "sem permissão nunca");
        assertTrue(RegrasMestre.ehMestre(true, "QualquerOp", List.of()), "lista vazia: todo op");
        assertTrue(RegrasMestre.ehMestre(true, "blackzin6228", List.of("Blackzin6228")), "maiúscula não importa");
        assertFalse(RegrasMestre.ehMestre(true, "OpQueJoga", List.of("Blackzin6228")), "op fora da lista não recebe aviso");
    }

    @Test
    void ouvirSoEmEspectador() {
        assertTrue(RegrasMestre.ouvindo(true, true, null, true), "padrão ligado");
        assertFalse(RegrasMestre.ouvindo(true, true, false, true), "desligou");
        assertTrue(RegrasMestre.ouvindo(true, true, true, false), "ligou com padrão desligado");
        assertFalse(RegrasMestre.ouvindo(true, false, true, true), "fora do espectador joga normal");
        assertFalse(RegrasMestre.ouvindo(false, true, true, true), "não é mestre");
    }

    @Test
    void tempoAteMudar() {
        assertEquals(1_000, RegrasMestre.ateMudar(12_000), "dia: escurece às 13.000");
        assertEquals(5_000, RegrasMestre.ateMudar(18_000), "noite: amanhece às 23.000");
        assertEquals(14_000, RegrasMestre.ateMudar(23_000), "de manhã cedo: até a noite seguinte");
        assertEquals(5_000, RegrasMestre.ateMudar(24_000L * 3 + 18_000), "outro dia");
        assertEquals(0, RegrasMestre.minutos(0));
        assertEquals(1, RegrasMestre.minutos(1));
        assertEquals(2, RegrasMestre.minutos(1_201));
        assertEquals(0, RegrasMestre.minutos(-5));
    }

    @Test
    void numerosDoPainel() {
        assertEquals("4.480", RegrasMestre.milhar(4_480));
        assertEquals("32", RegrasMestre.milhar(32));
        assertEquals("1.000.000", RegrasMestre.milhar(1_000_000));
        assertEquals(5, RegrasMestre.distancia(3, 4));
    }

    @Test
    void configDocumentadaEmIds() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        assertTrue(ids.contains(IdsMestre.ARQUIVO_CONFIG), "arquivo de config do mestre em docs/ids.md");
        for (String k : IdsMestre.CHAVES_CONFIG) {
            assertTrue(ids.contains("`" + IdsMestre.SECAO_CONFIG + "." + k + "`"), "falta " + k + " em docs/ids.md");
        }
    }
}
