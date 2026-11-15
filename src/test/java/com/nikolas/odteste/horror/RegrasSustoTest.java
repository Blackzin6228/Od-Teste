package com.nikolas.odteste.horror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Sustos grandes: raros (1 ou 2 por noite), com espera, regulados pela Exposição. */
class RegrasSustoTest {
    private static final long NOITE_0 = 13_500;

    @Test
    void umPorNoiteEDoisSoLaNoFundo() {
        assertEquals(1, RegrasSusto.maxPorNoite(0));
        assertEquals(1, RegrasSusto.maxPorNoite(2));
        assertEquals(2, RegrasSusto.maxPorNoite(3));
        assertEquals(2, RegrasSusto.maxPorNoite(4));
        assertEquals(2, RegrasSusto.maxPorNoite(99));
    }

    @Test
    void chanceSobeComExposicaoECaiComGrupo() {
        for (int f = 0; f < 4; f++) assertTrue(RegrasSusto.chance(f, false) < RegrasSusto.chance(f + 1, false));
        assertEquals(RegrasSusto.chance(3, false) / 2, RegrasSusto.chance(3, true), 1e-9);
    }

    @Test
    void esperaDepoisDeAnoitecerEEntreDois() {
        RegrasSusto.Diario d = new RegrasSusto.Diario();
        long noite = RegrasSusto.noite(NOITE_0);
        assertFalse(d.cabe(noite, 0, 4), "camada desligada");
        d.camada(true, 0);
        assertFalse(d.cabe(noite, RegrasSusto.ESPERA_LIGOU - 1, 4));
        assertTrue(d.cabe(noite, RegrasSusto.ESPERA_LIGOU, 4));
        d.marcar(noite, 2_000);
        assertFalse(d.cabe(noite, 2_000 + RegrasSusto.ESPERA_ENTRE - 1, 4));
        assertTrue(d.cabe(noite, 2_000 + RegrasSusto.ESPERA_ENTRE, 4)); // Crítica: cabe o segundo
        d.marcar(noite, 2_000 + RegrasSusto.ESPERA_ENTRE);
        assertFalse(d.cabe(noite, 100_000, 4), "nunca mais que dois na mesma noite");
        assertTrue(d.cabe(noite + 1, 100_000, 4), "a noite seguinte começa do zero");
    }

    @Test
    void comumSoUmPorNoite() {
        RegrasSusto.Diario d = new RegrasSusto.Diario();
        long noite = RegrasSusto.noite(NOITE_0);
        d.camada(true, 0);
        d.marcar(noite, 2_000);
        assertFalse(d.cabe(noite, 100_000, 0));
    }

    @Test
    void desligarZeraAEsperaInicial() {
        RegrasSusto.Diario d = new RegrasSusto.Diario();
        d.camada(true, 0);
        d.camada(false, 5_000);
        d.camada(true, 6_000);
        assertFalse(d.cabe(0, 6_000 + RegrasSusto.ESPERA_LIGOU - 1, 4));
    }

    @Test
    void sairEEntrarNaoZeraOLimite() {
        RegrasSusto.Diario d = new RegrasSusto.Diario();
        long noite = RegrasSusto.noite(NOITE_0);
        d.camada(true, 0);
        d.marcar(noite, 2_000);
        d.camada(false, 3_000); // saiu
        d.camada(true, 4_000); // voltou na mesma noite
        assertFalse(d.cabe(noite, 100_000, 0));
    }

    @Test
    void aNoiteInteiraTemOMesmoNumero() {
        assertEquals(RegrasSusto.noite(13_000), RegrasSusto.noite(22_999));
        assertEquals(RegrasSusto.noite(24_000 + 13_000), RegrasSusto.noite(24_000 + 22_000));
        assertNotEquals(RegrasSusto.noite(13_000), RegrasSusto.noite(24_000 + 13_000));
    }

    @Test
    void numaNoiteSimuladaNuncaPassaDoTeto() {
        Random r = new Random(7);
        for (int f = 0; f <= 4; f++) {
            for (int rodada = 0; rodada < 200; rodada++) {
                RegrasSusto.Diario d = new RegrasSusto.Diario();
                int feitos = 0;
                long noite = 0;
                for (long agora = 0; agora < 10_000; agora += 20) { // uma noite, conferindo a cada segundo
                    d.camada(true, agora);
                    if (!RegrasSusto.vezDeTentar(agora / 20, 3)) continue;
                    if (RegrasSusto.tentar(d, noite, agora, f, false, r.nextDouble())) {
                        d.marcar(noite, agora);
                        feitos++;
                    }
                }
                assertTrue(feitos <= RegrasSusto.maxPorNoite(f), "faixa " + f + ": " + feitos);
            }
        }
    }

    @Test
    void tentaUmaVezACada30Segundos() {
        int vezes = 0;
        for (long s = 0; s < 300; s++) if (RegrasSusto.vezDeTentar(s, 12345)) vezes++;
        assertEquals(10, vezes);
    }

    @Test
    void naoRepeteOMesmoTipo() {
        Random r = new Random(1);
        for (int i = 0; i < 500; i++) {
            TipoSusto t = RegrasSusto.escolher(r.nextInt(5), TipoSusto.LUZ, r::nextInt);
            assertNotNull(t);
            assertNotEquals(TipoSusto.LUZ, t);
        }
    }

    @Test
    void idsDosTiposNaoMudam() {
        assertEquals(TipoSusto.PASSOS, TipoSusto.de("passos"));
        assertEquals(TipoSusto.SILENCIO, TipoSusto.de("silencio"));
        assertEquals(null, TipoSusto.de("nada"));
        assertEquals(5, TipoSusto.ids().size());
    }

    @Test
    void idsEstaoNoDocumento() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (String id : IdsHorror.TODOS) {
            assertTrue(ids.contains("`odteste:" + id + "`"), "falta odteste:" + id + " em docs/ids.md");
        }
        for (String t : TipoSusto.ids()) assertTrue(ids.contains("`" + t + "`"), "falta o tipo " + t + " em docs/ids.md");
        assertTrue(ids.contains("`minimapa_canto`") && ids.contains("`minimapa_tamanho`"));
    }
}
