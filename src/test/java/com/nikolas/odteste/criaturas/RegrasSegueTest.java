package com.nikolas.odteste.criaturas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.dados.IdsDados;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** O Que Segue (proposta de 13/11/2026). */
class RegrasSegueTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/odteste");

    @Test
    void soDeNoiteOuComExposicaoAlta() {
        assertEquals(0, RegrasSegue.chance(false, 59, 1, 0.03));
        assertTrue(RegrasSegue.chance(false, 60, 0, 0.03) > 0);
        assertTrue(RegrasSegue.chance(true, 0, 0, 0.03) > 0);
        assertEquals(0, RegrasSegue.chance(true, 90, 1, 0));
        // Máquina e Exposição aumentam, com teto.
        assertTrue(RegrasSegue.chance(true, 0, 1, 0.03) > RegrasSegue.chance(true, 0, 0, 0.03));
        assertTrue(RegrasSegue.chance(true, 80, 0, 0.03) > RegrasSegue.chance(true, 0, 0, 0.03));
        assertTrue(RegrasSegue.chance(true, 100, 1, 1) <= RegrasSegue.CHANCE_TETO);
    }

    @Test
    void maquinasPesamAosPoucos() {
        assertEquals(0, RegrasSegue.fatorMaquinas(0));
        assertEquals(0, RegrasSegue.fatorMaquinas(RegrasSegue.MAQUINAS_MIN - 1));
        assertEquals(1, RegrasSegue.fatorMaquinas(1000));
        assertEquals(0f, RegrasSegue.intensidadeMaquinas(RegrasSegue.MAQUINAS_EXPOSICAO - 1));
        assertTrue(RegrasSegue.intensidadeMaquinas(RegrasSegue.MAQUINAS_EXPOSICAO) > 0);
        assertTrue(RegrasSegue.intensidadeMaquinas(1000) <= 2f);
    }

    @Test
    void saltosMaisRapidosComMaquinas() {
        assertTrue(RegrasSegue.intervaloSalto(0.5, 1) < RegrasSegue.intervaloSalto(0.5, 0));
        assertTrue(RegrasSegue.intervaloSalto(0, 1) >= 30);
        assertTrue(RegrasSegue.passo(0, 0) >= 3);
        assertTrue(RegrasSegue.passo(1, 1) <= 8);
    }

    @Test
    void comVidaCheiaNaoMata() {
        assertEquals(19f, RegrasSegue.dano(40, 20, 20));
        assertEquals(14f, RegrasSegue.dano(14, 20, 20));
        assertEquals(14f, RegrasSegue.dano(14, 10, 20));
        assertEquals(0f, RegrasSegue.dano(0, 20, 20));
    }

    @Test
    void encaradaSome() {
        assertFalse(RegrasSegue.sumirOlhada(20, true, 30));
        assertTrue(RegrasSegue.sumirOlhada(RegrasSegue.TICKS_SUMIR_OLHADA, true, 30));
        assertTrue(RegrasSegue.sumirOlhada(10, true, 7));
        assertFalse(RegrasSegue.sumirOlhada(0, false, 7));
    }

    @Test
    void recargaDobraProtegido() {
        long normal = RegrasSegue.recarga(RegrasSegue.Motivo.PEGOU, 15, 5, false);
        assertEquals(20L * 60 * 15, normal);
        assertEquals(2 * normal, RegrasSegue.recarga(RegrasSegue.Motivo.PEGOU, 15, 5, true));
        assertEquals(2 * normal, RegrasSegue.recarga(RegrasSegue.Motivo.DORMIU, 15, 5, false));
        assertEquals(20L * 60 * 5, RegrasSegue.recarga(RegrasSegue.Motivo.VISTA, 15, 5, false));
    }

    @Test
    void temModeloAnimacaoTexturaENome() throws Exception {
        String id = IdsDados.CRIATURA_O_QUE_SEGUE;
        assertTrue(Files.exists(ASSETS.resolve("geo/entity/" + id + ".geo.json")));
        assertTrue(Files.exists(ASSETS.resolve("textures/entity/" + id + ".png")));
        String anim = Files.readString(ASSETS.resolve("animations/entity/" + id + ".animation.json"));
        assertTrue(anim.contains("animation." + id + ".parado"));
        for (String lingua : new String[] {"pt_br", "en_us"}) {
            assertTrue(Files.readString(ASSETS.resolve("lang/" + lingua + ".json")).contains("\"entity.odteste." + id + "\""));
        }
        assertTrue(Files.readString(Path.of("docs/ids.md")).contains("`odteste:" + id + "`"));
    }
}
