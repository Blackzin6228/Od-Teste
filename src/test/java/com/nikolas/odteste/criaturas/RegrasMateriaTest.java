package com.nikolas.odteste.criaturas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        // Só com jogador no killed_by_player. O FakePlayer passa nessa condição: quem tira a matéria dele é o
        // CansacoDoElemento (testes maquinaNaoLevaMateria e morteIndiretaContaParaQuemBateu).
        assertTrue(t.contains("minecraft:killed_by_player"));
    }

    @Test
    void maquinaNaoLevaMateria() {
        // Jogador de verdade: passa pelo cansaço.
        assertEquals(RegrasCansaco.Destino.CONTA, RegrasCansaco.destino(true, false));
        // FakePlayer (máquina, Create, Mekanism...): a matéria sai do drop inteira, sem sorteio e sem contar.
        assertEquals(RegrasCansaco.Destino.TIRA, RegrasCansaco.destino(true, true));
        // Ninguém bateu (ou o golpe já expirou): também sai.
        assertEquals(RegrasCansaco.Destino.TIRA, RegrasCansaco.destino(false, false));
        assertEquals(RegrasCansaco.Destino.TIRA, RegrasCansaco.destino(false, true));
    }

    @Test
    void morteIndiretaContaParaQuemBateu() {
        // Ana bate e a criatura morre queimando/sangrando/caindo 3 s depois (o golpe vale 100 ticks; faltam 40):
        // o crédito é da Ana, igual ao killed_by_player do loot, mesmo sem ela no golpe final.
        assertEquals("ana", RegrasCansaco.jogadorDoDrop("ana", 40));
        assertEquals(RegrasCansaco.Destino.CONTA, RegrasCansaco.destino(RegrasCansaco.jogadorDoDrop("ana", 40) != null, false));
        // Máquina bate e a criatura morre na lava: o último que bateu é o FakePlayer, então a matéria sai.
        assertEquals(RegrasCansaco.Destino.TIRA, RegrasCansaco.destino(RegrasCansaco.jogadorDoDrop("maquina", 40) != null, true));
        // Golpe velho (passou dos 5 s): o loot também não vê jogador; nada conta e a matéria sai.
        assertNull(RegrasCansaco.jogadorDoDrop("ana", 0));
        assertNull(RegrasCansaco.jogadorDoDrop(null, 100));
    }

    @Test
    void cansacoUsaOMesmoJogadorDoLoot() throws Exception {
        String c = Files.readString(Path.of("src/main/java/com/nikolas/odteste/criaturas/CansacoDoElemento.java"));
        // Não pode achar o jogador pelo golpe final (fogo, queda e sangramento não têm jogador na fonte).
        assertFalse(c.contains("getSource().getEntity()"), "o cansaço não pode olhar só o golpe final");
        assertTrue(c.contains("jogadorDoDrop()"));
        // FakePlayer cai no TIRA, que remove a matéria bruta (o furo antigo era só dar return).
        assertTrue(c.contains("removeIf(solto -> ehMateriaBruta"));
        String criatura = Files.readString(Path.of("src/main/java/com/nikolas/odteste/criaturas/CriaturaDeSangue.java"));
        assertTrue(criatura.contains("jogadorDoDrop(lastHurtByPlayer, lastHurtByPlayerTime)"),
                "o crédito precisa vir do mesmo campo que o killed_by_player do loot usa");
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
