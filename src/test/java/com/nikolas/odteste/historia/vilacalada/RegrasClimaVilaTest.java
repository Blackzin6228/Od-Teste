package com.nikolas.odteste.historia.vilacalada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegrasClimaVilaTest {
    @Test
    void dentroDoMuro() {
        assertTrue(RegrasClimaVila.dentro(0, 0, 0));
        assertTrue(RegrasClimaVila.dentro(30, 5, -30), "canto do muro");
        assertFalse(RegrasClimaVila.dentro(40, 0, 40), "fora do círculo");
        assertFalse(RegrasClimaVila.dentro(0, -40, 0), "caverna funda embaixo não conta");
        assertTrue(RegrasClimaVila.dentro(0, 30, 0), "alto do campanário conta");
    }

    @Test
    void penduradoEDoMaisExposto() {
        UUID a = new UUID(0, 1);
        UUID b = new UUID(0, 2);
        UUID c = new UUID(0, 3);
        Map<UUID, Integer> m = new HashMap<>();
        assertNull(RegrasClimaVila.maisExposto(m), "grupo sem ninguém online");
        m.put(a, 10);
        m.put(b, 40);
        m.put(c, 40);
        assertEquals(b, RegrasClimaVila.maisExposto(m), "empate fica com o menor UUID (não troca a cada segundo)");
        m.put(c, 41);
        assertEquals(c, RegrasClimaVila.maisExposto(m));
    }

    @Test
    void nevoaNuncaPassaDoNormalEMaisFechadaDeNoite() {
        assertEquals(256f, RegrasClimaVila.alcance(256f, 0f, 1f), 1e-4, "fora da vila: nada muda");
        float dia = RegrasClimaVila.alcance(256f, 1f, 0f);
        float noite = RegrasClimaVila.alcance(256f, 1f, 1f);
        assertEquals(RegrasClimaVila.ALCANCE_DIA, dia, 1e-4);
        assertEquals(RegrasClimaVila.ALCANCE_NOITE, noite, 1e-4);
        assertTrue(noite < dia);
        assertEquals(10f, RegrasClimaVila.alcance(10f, 1f, 1f), 1e-4, "cegueira/água continuam mandando");
        float meio = RegrasClimaVila.alcance(256f, 0.5f, 0f);
        assertTrue(meio < 256f && meio > dia, "SUAVE fica entre o normal e o cheio");
    }

    @Test
    void corPuxaParaOVermelhoDoSangue() {
        assertEquals(0f, RegrasClimaVila.mistura(0f, 1f), 1e-6);
        assertTrue(RegrasClimaVila.mistura(1f, 1f) > RegrasClimaVila.mistura(1f, 0f), "mais forte de noite");
        assertTrue(RegrasClimaVila.mistura(1f, 1f) < 0.5f, "leve: nunca toma a cor toda");
        assertEquals(0x7A / 255f, RegrasClimaVila.misturarCanal(0f, 16, 1f), 1e-6);
        assertEquals(0x10 / 255f, RegrasClimaVila.misturarCanal(1f, 0, 1f), 1e-6);
        assertEquals(0.5f, RegrasClimaVila.misturarCanal(0.5f, 8, 0f), 1e-6);
    }

    @Test
    void sonsGravesERaros() {
        assertEquals(0.5f, RegrasClimaVila.tom(0), 1e-6);
        assertEquals(0.7f, RegrasClimaVila.tom(1), 1e-6);
        assertEquals(0.7f, RegrasClimaVila.tom(9), 1e-6, "sorteio fora de 0..1 é cortado");
        assertTrue(RegrasClimaVila.proximoSom(0) >= 20 * 20, "nunca mais de um som a cada 20 s");
        assertEquals(RegrasClimaVila.SOM_MAX, RegrasClimaVila.proximoSom(1));
    }

    @Test
    void penduradoRaroENoCantoDaVisao() {
        assertTrue(RegrasClimaVila.proximoPendurado(0) >= 20 * 60 * 3, "no mínimo 3 min entre um e outro");
        assertTrue(RegrasClimaVila.primeiroPendurado(1) < RegrasClimaVila.PENDURADO_MIN);
        float esq = RegrasClimaVila.anguloPendurado(0, true);
        float dir = RegrasClimaVila.anguloPendurado(1, false);
        assertEquals(-RegrasClimaVila.PENDURADO_ANGULO_MIN, esq, 1e-6);
        assertEquals(RegrasClimaVila.PENDURADO_ANGULO_MAX, dir, 1e-6);
        // Fora do "olhou direto" (18°), senão some no mesmo tick em que aparece.
        assertTrue(RegrasClimaVila.PENDURADO_ANGULO_MIN > 18);
        double d = RegrasClimaVila.distanciaPendurado(0.5);
        assertTrue(d >= RegrasClimaVila.PENDURADO_PERTO && d <= RegrasClimaVila.PENDURADO_LONGE);
    }

    @Test
    void aproximarNaoPassaDoAlvo() {
        assertEquals(1f, RegrasClimaVila.aproximar(0.99f, 1f, 0.1f), 1e-6);
        assertEquals(0f, RegrasClimaVila.aproximar(0.05f, 0f, 0.1f), 1e-6);
        assertEquals(0.5f, RegrasClimaVila.aproximar(0.4f, 1f, 0.1f), 1e-6);
    }

    @Test
    void pacoteEstaEmIdsMd() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        assertTrue(ids.contains("`odteste:" + IdsVilaCalada.PACOTE_CLIMA + "`"), "falta odteste:clima_vila em docs/ids.md");
    }
}
