package com.nikolas.odteste.sistemas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.criaturas.RegrasCriaturas;
import com.nikolas.odteste.sistemas.RegrasCombate.Raridade;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Balanceamento tech (decisões de 13/11/2026): armas, criaturas, magia, traje, luz, tocha e "algo segura você aqui". */
class RegrasCombateTest {

    @Test
    void armaNuncaPassaDe20() {
        assertEquals(7f, RegrasCombate.golpeDeArma(7f));
        assertEquals(20f, RegrasCombate.golpeDeArma(20f));
        assertEquals(20f, RegrasCombate.golpeDeArma(35f));
        assertEquals(0f, RegrasCombate.golpeDeArma(-3f));
        assertEquals(0f, RegrasCombate.golpeDeArma(Float.NaN));
        // Fica abaixo da Meka-Tool do pack (24).
        assertTrue(RegrasCombate.TETO_ARMA < 24f);
    }

    @Test
    void criaturaNossa() {
        // Paranormal dobra, em qualquer raridade (mesmo se também for magia).
        assertEquals(20f, RegrasCombate.contraCriatura(10f, true, false, Raridade.CHEFE));
        assertEquals(20f, RegrasCombate.contraCriatura(10f, true, true, Raridade.CHEFE));
        // Comum perde a resistência: 0 / 30 / 60%.
        assertEquals(10f, RegrasCombate.contraCriatura(10f, false, false, Raridade.COMUM), 1e-4);
        assertEquals(7f, RegrasCombate.contraCriatura(10f, false, false, Raridade.RARA), 1e-4);
        assertEquals(4f, RegrasCombate.contraCriatura(10f, false, false, Raridade.CHEFE), 1e-4);
        assertEquals(10f, RegrasCombate.contraCriatura(10f, false, false, null), 1e-4);
        assertEquals(0f, RegrasCombate.contraCriatura(Float.NaN, true, false, Raridade.RARA));
        // Arma nossa no teto contra criatura nossa: 20 x 2 = 40.
        assertEquals(40f, RegrasCombate.contraCriatura(RegrasCombate.golpeDeArma(30f), true, false, Raridade.COMUM));
    }

    @Test
    void magiaPassaPorCimaDaResistencia() {
        // Resposta do Nikolas (13/11/2026): o técnico precisa do mago. Magia entra inteira, sem o x2 do paranormal.
        for (Raridade r : Raridade.values()) {
            assertEquals(10f, RegrasCombate.contraCriatura(10f, false, true, r), 1e-4, "magia contra " + r);
        }
        assertEquals(0f, RegrasCombate.contraCriatura(-2f, false, true, Raridade.CHEFE));
    }

    @Test
    void magiaNaTagSoComEntradasOpcionais() throws Exception {
        String magia = Files.readString(Path.of("src/main/resources/data/odteste/tags/damage_type/magia.json"));
        assertTrue(magia.matches("(?s).*\"replace\"\\s*:\\s*false.*"));
        assertTrue(magia.contains("\"#irons_spellbooks:fire_magic\""));
        assertTrue(magia.contains("\"ars_nouveau:spell\""));
        // Toda entrada é de outro mod: precisa ser opcional, senão a tag quebra sem o mod no pack.
        int entradas = magia.split("\"id\"", -1).length - 1;
        int opcionais = magia.split("\"required\"\\s*:\\s*false", -1).length - 1;
        assertEquals(entradas, opcionais, "toda entrada da tag magia precisa de \"required\": false");
        // neoforge:is_magic traz espinhos, veneno, wither e poção de dano: não é magia de mod.
        assertFalse(magia.contains("neoforge:is_magic"));
        assertFalse(magia.contains("\"minecraft:"));
    }

    @Test
    void traje() {
        // Abaixo de A4, nada aqui (a armadura de pontos cuida).
        assertEquals(0, RegrasCombate.absorcaoTraje(3, true));
        assertEquals(0, RegrasCombate.absorcaoTraje(0, false));
        // A4 e A5 (resposta do Nikolas de 13/11/2026): paranormal 50/70, comum 15/25.
        assertEquals(50, RegrasCombate.absorcaoTraje(4, true));
        assertEquals(70, RegrasCombate.absorcaoTraje(5, true));
        assertEquals(15, RegrasCombate.absorcaoTraje(4, false));
        assertEquals(25, RegrasCombate.absorcaoTraje(5, false));
        assertEquals(3f, RegrasCombate.depoisDoTraje(10f, 70), 1e-4);
        assertEquals(0f, RegrasCombate.depoisDoTraje(10f, 150), 1e-4);
        assertEquals(10f, RegrasCombate.depoisDoTraje(10f, -5), 1e-4);
    }

    @Test
    void luzSozinhaParaEmInquieto() {
        assertTrue(RegrasLucidez.luzDevolve(10, 100));
        assertTrue(RegrasLucidez.luzDevolve(69, 100));
        assertFalse(RegrasLucidez.luzDevolve(70, 100));
        assertFalse(RegrasLucidez.luzDevolve(95, 100));
        // O teto da luz nunca chega em Firme.
        for (int max = 10; max <= 100; max++) {
            int l = 0;
            while (RegrasLucidez.luzDevolve(l, max)) l++;
            assertTrue(FaixaLucidez.de(l, max) == FaixaLucidez.INQUIETO || l == 0, "luz parou fora de Inquieto com máxima " + max);
            assertTrue(FaixaLucidez.de(l, max) != FaixaLucidez.FIRME, "luz passou de Inquieto com máxima " + max);
        }
    }

    @Test
    void tochaSoProtegeAteExposicaoAlta() {
        // Só a regra: a ligação com o Torchmaster está pendente (docs/progresso.md).
        assertTrue(RegrasCriaturas.tochaProtege(0));
        assertTrue(RegrasCriaturas.tochaProtege(59));
        assertFalse(RegrasCriaturas.tochaProtege(60));
        assertEquals(Degrau.FUNDO.minimo, RegrasCriaturas.EXPOSICAO_TOCHA);
    }

    @Test
    void algoSeguraVoceAqui() {
        assertTrue(RegrasContencao.nossaDimensao("odteste"));
        assertFalse(RegrasContencao.nossaDimensao("minecraft"));
        assertFalse(RegrasContencao.nossaDimensao("twilightforest"));
        // Mundo normal, sem perseguição: liberado.
        assertFalse(RegrasContencao.preso(false, false, false));
        assertTrue(RegrasContencao.preso(true, false, false));
        assertTrue(RegrasContencao.preso(false, true, false));
        // Criativo e espectador nunca ficam presos.
        assertFalse(RegrasContencao.preso(true, true, true));
    }

    @Test
    void marcasDeTempo() {
        assertFalse(RegrasContencao.vale(100, null, 50));
        assertTrue(RegrasContencao.vale(100, 120L, 50));
        assertFalse(RegrasContencao.vale(120, 120L, 50));
        // Relógio voltou para antes do começo: não confia.
        assertFalse(RegrasContencao.vale(10, 120L, 50));
        assertTrue(RegrasContencao.podeAvisar(0, null));
        assertFalse(RegrasContencao.podeAvisar(30, 0L));
        assertTrue(RegrasContencao.podeAvisar(RegrasContencao.AVISO_TICKS, 0L));
        assertTrue(RegrasContencao.podeAvisar(5, 100L));
    }

    @Test
    void danoParanormalNaTagEArmaNaTag() throws Exception {
        String dano = Files.readString(Path.of("src/main/resources/data/odteste/tags/damage_type/paranormal.json"));
        assertTrue(dano.contains("\"odteste:paranormal\""), "o tipo odteste:paranormal precisa estar na tag paranormal");
        assertTrue(dano.matches("(?s).*\"replace\"\\s*:\\s*false.*"));
        String tipo = Files.readString(Path.of("src/main/resources/data/odteste/damage_type/paranormal.json"));
        assertTrue(tipo.contains("\"odteste.paranormal\""));
        // Não pode ignorar armadura: a armadura comum e a MekaSuit seguram (a MekaSuit menos, pelo data map).
        String ignora = Files.readString(Path.of("src/main/resources/data/minecraft/tags/damage_type/bypasses_armor.json"));
        assertFalse(ignora.contains("odteste:paranormal"));
        String arma = Files.readString(Path.of("src/main/resources/data/odteste/tags/item/arma_paranormal.json"));
        assertTrue(arma.contains("\"odteste:rubra\""));
    }
}
