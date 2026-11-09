package com.nikolas.odteste.historia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasAto1Test {

    @Test
    void exposicaoMediaDeixaOSinoMaisLento() {
        assertTrue(RegrasAto1.unidade(true) > RegrasAto1.unidade(false));
        int normal = RegrasAto1.duracaoComPausa(RegrasAto1.PALAVRA, RegrasAto1.unidade(false));
        int lenta = RegrasAto1.duracaoComPausa(RegrasAto1.PALAVRA, RegrasAto1.unidade(true));
        assertTrue(lenta > normal);
    }

    @Test
    void cadaToqueDoSinoTemRespostaDoBlocoDeNotas() {
        List<RegrasAto1.Som> sons = RegrasAto1.sons("FERRO", RegrasAto1.UNIDADE_NORMAL);
        // FERRO tem 14 toques (4 + 1 + 3 + 3 + 3): cada um vira sino + nota.
        assertEquals(14 * 2, sons.size());
        for (int i = 1; i < sons.size(); i++) assertTrue(sons.get(i).tick() >= sons.get(i - 1).tick(), "em ordem");
        long sinos = sons.stream().filter(s -> s.fonte() == RegrasAto1.Fonte.SINO).count();
        assertEquals(14, sinos);
        // A nota vem logo depois do sino, com o mesmo traço/ponto.
        RegrasAto1.Som primeiroSino = sons.get(0);
        RegrasAto1.Som primeiraNota = sons.stream().filter(s -> s.fonte() == RegrasAto1.Fonte.NOTA).findFirst().orElseThrow();
        assertEquals(RegrasAto1.Fonte.SINO, primeiroSino.fonte());
        assertEquals(primeiroSino.tick() + RegrasAto1.ATRASO_NOTA, primeiraNota.tick());
        assertEquals(primeiroSino.traco(), primeiraNota.traco());
        // O atraso da nota nunca passa o próximo toque (fica claro qual nota é de qual toque).
        assertTrue(RegrasAto1.ATRASO_NOTA < RegrasAto1.UNIDADE_NORMAL * 2);
    }

    @Test
    void sonsDentroDaRegraDeVolumeETom() {
        for (float v : new float[] {RegrasAto1.VOLUME_PERTO, RegrasAto1.VOLUME_LONGE}) {
            assertTrue(v >= 0.5f && v <= 0.7f, "volume entre 0,5 e 0,7");
        }
        for (float t : new float[] {RegrasAto1.TOM_PONTO, RegrasAto1.TOM_TRACO}) {
            assertTrue(t >= 0.5f && t <= 0.7f, "tom entre 0,5 e 0,7");
        }
        assertTrue(RegrasAto1.TOM_TRACO < RegrasAto1.TOM_PONTO, "traço grave, ponto agudo");
    }

    @Test
    void placaAceitaFerroDeVariosJeitos() {
        assertTrue(RegrasAto1.placaCerta(List.of("ferro", "", "", ""), "FERRO"));
        assertTrue(RegrasAto1.placaCerta(List.of("F E R", "R O.", "", ""), "FERRO"));
        assertTrue(RegrasAto1.placaCerta(List.of("", " Ferro! ", "", ""), "FERRO"));
        assertFalse(RegrasAto1.placaCerta(List.of("ferros", "", "", ""), "FERRO"));
        assertFalse(RegrasAto1.placaCerta(List.of("", "", "", ""), "FERRO"));
        assertFalse(RegrasAto1.placaCerta(null, "FERRO"));
        assertEquals("", RegrasAto1.juntar(Arrays.asList("", " ", ".", "")));
    }

    @Test
    void placaValeNaFrenteOuNoVerso() {
        List<String> vazio = List.of("", "", "", "");
        assertTrue(RegrasAto1.placaCerta(List.of("ferro", "", "", ""), vazio, "FERRO"), "só na frente");
        assertTrue(RegrasAto1.placaCerta(vazio, List.of("", "FERRO", "", ""), "FERRO"), "só no verso");
        assertTrue(RegrasAto1.placaCerta(List.of("oi", "", "", ""), List.of("ferro", "", "", ""), "FERRO"),
                "verso certo vale mesmo com a frente rabiscada");
        assertFalse(RegrasAto1.placaCerta(List.of("FER", "", "", ""), List.of("RO", "", "", ""), "FERRO"),
                "metade em cada lado não vale");
        assertFalse(RegrasAto1.placaCerta(vazio, vazio, "FERRO"));
    }

    @Test
    void lucidezSoCaiOuvindoTudoDePertoEComEspera() {
        long espera = RegrasAto1.ESPERA_PERDA_TICKS;
        assertFalse(RegrasAto1.perdeLucidez(false, 1000, -1), "de longe não tira");
        assertTrue(RegrasAto1.perdeLucidez(true, 1000, -1), "primeira vez tira");
        assertFalse(RegrasAto1.perdeLucidez(true, 1000 + espera - 1, 1000), "dentro da espera não tira");
        assertTrue(RegrasAto1.perdeLucidez(true, 1000 + espera, 1000));
        assertTrue(RegrasAto1.perdeLucidez(true, 10, 5000), "relógio voltou: não confia na marca");
    }

    @Test
    void fraseDaTrocaNuncaRepeteSeguida() {
        for (int ultima = 0; ultima <= RegrasAto1.FRASES_TROCA; ultima++) {
            for (int s = 0; s < 50; s++) {
                int f = RegrasAto1.fraseTroca(ultima, s);
                assertTrue(f >= 1 && f <= RegrasAto1.FRASES_TROCA);
                if (ultima >= 1) assertNotEquals(ultima, f);
            }
        }
    }

    @Test
    void frasesDaTrocaExistemNosDoisIdiomas() throws Exception {
        for (String lang : new String[] {"pt_br", "en_us"}) {
            String json = Files.readString(Path.of("src/main/resources/assets/odteste/lang/" + lang + ".json"));
            for (int i = 1; i <= RegrasAto1.FRASES_TROCA; i++) {
                assertTrue(json.contains("\"odteste.ato1.troca." + i + "\""), "falta troca." + i + " em " + lang);
            }
        }
    }

    @Test
    void idsDaHistoriaEstaoNoDocumento() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (String id : IdsHistoria.MARCOS) assertTrue(ids.contains("`" + id + "`"), "falta marco " + id + " em docs/ids.md");
        for (String id : IdsHistoria.LUGARES) assertTrue(ids.contains("`" + id + "`"), "falta lugar " + id + " em docs/ids.md");
        assertTrue(ids.contains("`" + IdsHistoria.CHAVE_PECA + "`"));
        assertTrue(ids.contains("`" + com.nikolas.odteste.dados.IdsDados.ARQUIVO_HISTORIA + "`"));
        assertTrue(ids.contains("`odteste:" + IdsHistoria.MODIFICADOR_ALDEAO_PARADO + "`"));
    }

    @Test
    void chavesDoArquivoSalvoEstaoNoDocumento() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (List<String> l : List.of(IdsHistoria.CHAVES_VILA, IdsHistoria.CHAVES_COLOCACAO, IdsHistoria.CHAVES_BACKUP)) {
            for (String k : l) assertTrue(ids.contains("`" + k + "`"), "falta a chave " + k + " em docs/ids.md");
        }
    }

    @Test
    void chavesDaVilaSaoAsQueOCodigoUsa() throws Exception {
        // Se alguém renomear uma chave no código, a lista (e o docs/ids.md) deixam de bater: o teste avisa.
        String codigo = Files.readString(Path.of("src/main/java/com/nikolas/odteste/historia/VilaMarcada.java"));
        for (String k : IdsHistoria.CHAVES_VILA) assertTrue(codigo.contains("\"" + k + "\""), "VilaMarcada não usa " + k);
        String backup = Files.readString(Path.of("src/main/java/com/nikolas/odteste/estruturas/Backup.java"));
        for (String k : IdsHistoria.CHAVES_BACKUP) assertTrue(backup.contains("\"" + k + "\""), "Backup não usa " + k);
        String col = Files.readString(Path.of("src/main/java/com/nikolas/odteste/estruturas/Colocacao.java"));
        for (String k : IdsHistoria.CHAVES_COLOCACAO) assertTrue(col.contains("\"" + k + "\""), "Colocacao não usa " + k);
    }
}
