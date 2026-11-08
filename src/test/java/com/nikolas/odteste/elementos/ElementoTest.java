package com.nikolas.odteste.elementos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class ElementoTest {

    @Test
    void lerIdIgnoraMaiusculaEEspaco() {
        assertEquals(Elemento.SANGUE, Elemento.deId(" Sangue ").orElseThrow());
        assertTrue(Elemento.deId("medo").isEmpty());
        assertTrue(Elemento.deId(null).isEmpty());
    }

    @Test
    void idDesconhecidoNoDiscoViraNenhum() {
        assertEquals(Elemento.NENHUM, Elemento.deIdOuNenhum("coisa_velha"));
        assertEquals(Elemento.NENHUM, Elemento.deIdOuNenhum(null));
    }

    @Test
    void idsSaoUnicosEVoltamIguais() {
        var vistos = new HashSet<String>();
        for (Elemento e : Elemento.values()) {
            assertTrue(vistos.add(e.id()), "id repetido: " + e.id());
            assertEquals(e, Elemento.deIdOuNenhum(e.id()));
        }
    }

    @Test
    void cicloDeVantagemFechaEmQuatro() {
        assertTrue(Elemento.SANGUE.venceDe(Elemento.CONHECIMENTO));
        assertTrue(Elemento.CONHECIMENTO.venceDe(Elemento.ENERGIA));
        assertTrue(Elemento.ENERGIA.venceDe(Elemento.MORTE));
        assertTrue(Elemento.MORTE.venceDe(Elemento.SANGUE));
        assertFalse(Elemento.CONHECIMENTO.venceDe(Elemento.SANGUE));
        assertFalse(Elemento.NENHUM.venceDe(Elemento.NENHUM));
        for (Elemento e : Elemento.values()) {
            if (e == Elemento.NENHUM) continue;
            assertEquals(e, e.vence().vence().vence().vence(), "o ciclo de " + e + " não fecha");
        }
    }

    @Test
    void todoElementoTemNomeTraduzido() throws Exception {
        String pt = Files.readString(Path.of("src/main/resources/assets/odteste/lang/pt_br.json"));
        for (Elemento e : Elemento.values()) {
            assertTrue(pt.contains("\"" + e.chaveTraducao() + "\""), "falta tradução de " + e.id());
        }
    }
}
