package com.nikolas.odteste.achados;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.dados.IdsDados;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class RegrasAchadosTest {

    @Test
    void dropEBaixoEDiminuiQuemJaTem() {
        assertEquals(0.03, RegrasAchados.chanceDrop(false), 1e-9);
        assertEquals(0.0075, RegrasAchados.chanceDrop(true), 1e-9);
        assertTrue(RegrasAchados.sorteou(RegrasAchados.chanceDrop(false), 0.029));
        assertFalse(RegrasAchados.sorteou(RegrasAchados.chanceDrop(false), 0.03));
        assertFalse(RegrasAchados.sorteou(RegrasAchados.chanceDrop(true), 0.01));
    }

    @Test
    void escolhaRespeitaPesosEPrefereOQueFalta() {
        boolean[] nenhum = {false, false, false};
        assertEquals(0, RegrasAchados.escolher(nenhum, 0));
        assertEquals(0, RegrasAchados.escolher(nenhum, 5));
        assertEquals(1, RegrasAchados.escolher(nenhum, 6));
        assertEquals(1, RegrasAchados.escolher(nenhum, 8));
        assertEquals(2, RegrasAchados.escolher(nenhum, 9));
        // Já tem a agulha: só sobram dedal (3) e sino (1).
        boolean[] temAgulha = {true, false, false};
        assertEquals(4, RegrasAchados.somaPesos(temAgulha));
        assertEquals(1, RegrasAchados.escolher(temAgulha, 0));
        assertEquals(2, RegrasAchados.escolher(temAgulha, 3));
        // Tem todos: volta ao sorteio normal.
        boolean[] todos = {true, true, true};
        assertEquals(10, RegrasAchados.somaPesos(todos));
        assertEquals(2, RegrasAchados.escolher(todos, 9));
    }

    @Test
    void pesosAcompanhamOsTresArtefatos() {
        assertEquals(IdsDados.ARTEFATOS.size(), RegrasAchados.PESOS.length);
    }

    @Test
    void rondaSoDeNoiteDepoisDosPrimeirosDiasEDentroDosLimites() {
        long dia = 24000;
        assertTrue(RegrasAchados.ehNoite(15000));
        assertFalse(RegrasAchados.ehNoite(6000));
        assertTrue(RegrasAchados.ehNoite(5 * dia + 18000));
        assertFalse(RegrasAchados.ehNoite(5 * dia + 2000));
        assertTrue(RegrasAchados.podeRondar(5 * dia, true, 0, 0, 3));
        assertFalse(RegrasAchados.podeRondar(5 * dia, false, 0, 0, 3));
        assertFalse(RegrasAchados.podeRondar(2 * dia, true, 0, 0, 3));
        assertFalse(RegrasAchados.podeRondar(5 * dia, true, RegrasAchados.MAX_PERTO_ANCORA, 0, 3));
        assertFalse(RegrasAchados.podeRondar(5 * dia, true, 0, RegrasAchados.MAX_POR_CHUNK, 3));
    }

    @Test
    void rondaTemRaio() {
        assertTrue(RegrasAchados.dentroDoRaio(100, 0));
        assertTrue(RegrasAchados.dentroDoRaio(128, 0));
        assertFalse(RegrasAchados.dentroDoRaio(129, 0));
        assertFalse(RegrasAchados.dentroDoRaio(100, 100));
    }

    /** Artefato só aparece na tabela do baú de recompensa: nada em receita, tag, drop de criatura ou tabela comum. */
    @Test
    void artefatoSoApareceNaTabelaDeRecompensa() throws Exception {
        Path data = Path.of("src/main/resources/data");
        List<String> achados = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(data)) {
            for (Path f : (Iterable<Path>) arquivos.filter(Files::isRegularFile)::iterator) {
                String texto = Files.readString(f);
                for (String id : IdsDados.ARTEFATOS) {
                    if (texto.contains("odteste:" + id)) achados.add(data.relativize(f).toString().replace('\\', '/'));
                }
            }
        }
        for (String a : achados) {
            assertEquals("odteste/loot_table/chests/recompensa_artefato_sangue.json", a,
                    "artefato fora da tabela de recompensa: " + a);
        }
        assertFalse(achados.isEmpty(), "a tabela de recompensa precisa listar os artefatos");
    }

    @Test
    void tabelaDeRecompensaTemOsTresArtefatos() throws Exception {
        String t = Files.readString(Path.of("src/main/resources/data/odteste/loot_table/"
                + IdsDados.LOOT_RECOMPENSA_ARTEFATO_SANGUE + ".json"));
        for (String id : IdsDados.ARTEFATOS) assertTrue(t.contains("odteste:" + id), id);
    }

    @Test
    void artefatosEstaoEscondidosENosIds() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (String id : List.of(IdsDados.RECOMPENSA_CHAVE, IdsDados.RECOMPENSA_ARTEFATO_SANGUE,
                IdsDados.LOOT_RECOMPENSA_ARTEFATO_SANGUE)) {
            assertTrue(ids.contains("`" + id + "`"), id + " precisa estar em docs/ids.md");
        }
    }
}
