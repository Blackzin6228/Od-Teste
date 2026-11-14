package com.nikolas.odteste.sinais;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** A Vila Calada como fonte de sinal (Java puro). */
class RegrasFonteVilaTest {

    @Test
    void semAncoraNaoEmite() {
        // mundo que já existe e ainda não tem a vila: nada, sem erro
        assertFalse(RegrasFonteVila.emite(true, false, false));
        assertFalse(RegrasFonteVila.emite(true, false, true));
    }

    @Test
    void soEmiteLigadaENaMesmaDimensao() {
        assertTrue(RegrasFonteVila.emite(true, true, true));
        assertFalse(RegrasFonteVila.emite(false, true, true), "admin desligou as pistas");
        assertFalse(RegrasFonteVila.emite(true, true, false), "aparelho no Nether não capta a vila do overworld");
    }

    @Test
    void alcanceContaAAltura() {
        assertTrue(RegrasFonteVila.dentro(0, 0, 0, 0));
        assertTrue(RegrasFonteVila.dentro(3, 0, 4, 5));
        assertFalse(RegrasFonteVila.dentro(3, 1, 4, 5));
        assertTrue(RegrasFonteVila.dentro(-160, 0, 0, 160));
        assertFalse(RegrasFonteVila.dentro(-161, 0, 0, 160));
        assertFalse(RegrasFonteVila.dentro(0, 0, 0, -1));
    }

    @Test
    void alcanceGrandeNaoEstoura() {
        // coordenadas da borda do mundo: a conta é em long
        assertFalse(RegrasFonteVila.dentro(60_000_000L, 0, 0, 160));
    }

    @Test
    void apontaParaAMaisPerto() {
        assertTrue(RegrasFonteVila.vilaGanha(false, 0, 1e9), "sem fonte salva, a vila");
        assertTrue(RegrasFonteVila.vilaGanha(true, 400, 100));
        assertFalse(RegrasFonteVila.vilaGanha(true, 100, 400));
        assertFalse(RegrasFonteVila.vilaGanha(true, 100, 100), "empate fica com a fonte do admin");
    }

    @Test
    void tipoDaVilaTemTexto() throws IOException {
        assertTrue(RegrasSinal.tipoConhecido(RegrasFonteVila.TIPO));
        assertTrue(RegrasSinal.nomeValido(RegrasFonteVila.ID_FONTE));
        for (String lang : List.of("pt_br", "en_us")) {
            String json = Files.readString(Path.of("src/main/resources/assets/odteste/lang/" + lang + ".json"));
            for (String parte : List.of("decifrado", "camada")) {
                String chave = "odteste.sinal." + RegrasFonteVila.TIPO + "." + parte;
                assertTrue(json.contains("\"" + chave + "\""), lang + " sem " + chave);
            }
        }
    }

    @Test
    void soOSinalDaAncoraFicaCalado() {
        assertTrue(RegrasFonteVila.silencioso(RegrasFonteVila.ID_SINAL));
        assertFalse(RegrasFonteVila.silencioso("sino_vila"), "fonte salva pelo admin segue com o chiado de sempre");
        assertFalse(RegrasFonteVila.silencioso(RegrasFonteVila.TIPO), "fonte salva do tipo vila_calada também");
        assertFalse(RegrasFonteVila.silencioso(null));
    }

    @Test
    void fonteSalvaDoSinoPertoDaAncoraGanha() {
        int r = RegrasFonteVila.RAIO_DUPLICADA;
        assertTrue(RegrasFonteVila.duplicada("sino_vila", 0, 0, 0));
        assertTrue(RegrasFonteVila.duplicada("sino_vila", r, 0, 0));
        assertTrue(RegrasFonteVila.duplicada(RegrasFonteVila.TIPO, 10, 5, -10));
        assertFalse(RegrasFonteVila.duplicada("sino_vila", r + 1, 0, 0), "longe da vila: são dois sinais");
        assertFalse(RegrasFonteVila.duplicada("outro_tipo", 0, 0, 0), "outro sinal no mesmo lugar não esconde a vila");
    }
}
