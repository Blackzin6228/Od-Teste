package com.nikolas.odteste.criaturas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.sistemas.EventoEnergia;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Balanceamento tech, parte 2 (decisão de 13/11/2026): matéria e eventos de Energia. */
class RegrasMateriaTest {

    @Test
    void diaPeloRelogioDoMundo() {
        assertEquals(0, RegrasCansaco.dia(0));
        assertEquals(0, RegrasCansaco.dia(23_999));
        assertEquals(1, RegrasCansaco.dia(24_000));
        // Contador de outro dia não conta.
        assertEquals(5, RegrasCansaco.brutasHoje(3, 5, 3));
        assertEquals(0, RegrasCansaco.brutasHoje(2, 5, 3));
        assertEquals(0, RegrasCansaco.brutasHoje(-1, 0, 0));
        // Arquivo editado à mão: negativo vira 0, gigante para no teto.
        assertEquals(0, RegrasCansaco.brutasHoje(3, -7, 3));
        assertEquals(RegrasCansaco.TETO_CONTADOR, RegrasCansaco.brutasHoje(3, Integer.MAX_VALUE, 3));
    }

    @Test
    void chanceCaiDepoisDe8EDe16() {
        assertEquals(4, RegrasCansaco.chanceEmQuartos(0));
        assertEquals(4, RegrasCansaco.chanceEmQuartos(7));
        assertEquals(2, RegrasCansaco.chanceEmQuartos(8));
        assertEquals(2, RegrasCansaco.chanceEmQuartos(15));
        assertEquals(1, RegrasCansaco.chanceEmQuartos(16));
        assertEquals(1, RegrasCansaco.chanceEmQuartos(500));
        // Antes do cansaço, sempre cai.
        for (int s = 0; s < 4; s++) assertTrue(RegrasCansaco.cai(0, s));
        // Metade: 2 de 4 sorteios.
        int caem = 0;
        for (int s = 0; s < 4; s++) if (RegrasCansaco.cai(10, s)) caem++;
        assertEquals(2, caem);
        // Um quarto: 1 de 4.
        caem = 0;
        for (int s = 0; s < 4; s++) if (RegrasCansaco.cai(20, s)) caem++;
        assertEquals(1, caem);
    }

    @Test
    void fraseSoQuandoPassaDoLimiar() {
        assertEquals(0, RegrasCansaco.aviso(0, 7));
        assertEquals(1, RegrasCansaco.aviso(7, 8));
        assertEquals(0, RegrasCansaco.aviso(8, 9));
        assertEquals(2, RegrasCansaco.aviso(15, 16));
        // Pulou os dois de uma vez: a frase mais forte.
        assertEquals(2, RegrasCansaco.aviso(6, 17));
        assertEquals(0, RegrasCansaco.aviso(16, 20));
    }

    @Test
    void sanguessugaDaUmaGarantida() throws Exception {
        String t = Files.readString(Path.of("src/main/resources/data/odteste/loot_table/entities/sanguessuga_mae.json"));
        assertTrue(t.replaceAll("\\s", "").contains("\"count\":{\"type\":\"minecraft:uniform\",\"min\":1,\"max\":2}"),
                "a Sanguessuga-Mãe dá 1 garantida + 0 a 1");
        // O teto da Pilhagem continua (Apotheosis).
        assertTrue(t.replaceAll("\\s", "").contains("\"limit\":2"));
        // Só jogador (o FakePlayer é barrado no código).
        assertTrue(t.contains("minecraft:killed_by_player"));
    }

    @Test
    void energiaNa02SoCasaOcupadaEApagao() {
        assertTrue(EventoEnergia.CASA_OCUPADA.podeAcontecer(40));
        assertTrue(EventoEnergia.APAGAO.podeAcontecer(100));
        assertFalse(EventoEnergia.CASA_OCUPADA.podeAcontecer(39), "Exposição Média no mínimo");
        assertFalse(EventoEnergia.APAGAO.podeAcontecer(0));
        // Fome da Linha travada na 0.2, mesmo com Exposição máxima.
        assertFalse(EventoEnergia.FOME_DA_LINHA.podeAcontecer(100));
        // Nenhum evento liberado mexe em bloco de outro mod (reatores do Draconic e do Mekanism explodem).
        for (EventoEnergia ev : EventoEnergia.values()) {
            if (ev.liberado) assertFalse(ev.tocaOutrosMods(), ev + " está liberado e mexe em outro mod");
        }
    }
}
