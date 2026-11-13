package com.nikolas.odteste.cenas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasCenaTest {
    private static final Cena C = CenasConhecidas.ENTRADA_VILA_CALADA;
    private static final double E = 1e-6;

    @Test
    void todasAsCenasSaoValidas() {
        for (Cena c : CenasConhecidas.todas().values()) {
            assertEquals(List.of(), RegrasCena.problemas(c), "cena " + c.id());
        }
        assertEquals(IdsCena.CENAS.size(), CenasConhecidas.todas().size(), "toda cena precisa de id em IdsCena.CENAS");
        for (String id : IdsCena.CENAS) assertTrue(CenasConhecidas.de(id).isPresent(), "falta a cena " + id);
    }

    @Test
    void entradaDuraDezoitoSegundos() {
        assertEquals(18 * 20, C.fim());
        assertEquals(5, C.planos().size(), "5 planos de câmera; o sexto é o preto do texto");
    }

    @Test
    void suaveNasPontas() {
        assertEquals(0, RegrasCena.suave(0), E);
        assertEquals(1, RegrasCena.suave(1), E);
        assertEquals(0.5, RegrasCena.suave(0.5), E);
        assertEquals(0, RegrasCena.suave(-3), E);
        assertEquals(1, RegrasCena.suave(7), E);
        // Começa devagar: no primeiro décimo andou bem menos que um décimo.
        assertTrue(RegrasCena.suave(0.1) < 0.05);
    }

    @Test
    void poseSegueOsPlanos() {
        RegrasCena.Pose p0 = RegrasCena.pose(C, 0);
        assertEquals(-30, p0.camera().z(), E);
        assertEquals(1.7, p0.camera().y(), E);
        RegrasCena.Pose p1 = RegrasCena.pose(C, 60);
        assertEquals(-22, p1.camera().z(), E); // corte seco: o plano 2 começa do ponto dele
        RegrasCena.Pose fimTres = RegrasCena.pose(C, 199.999);
        assertEquals(8, fimTres.camera().z(), 1e-3);
        // Depois do último plano, a câmera fica parada onde parou (preto por cima).
        RegrasCena.Pose depois = RegrasCena.pose(C, 350);
        assertEquals(16, depois.camera().x(), E);
        assertEquals(10f, depois.zoom(), 1e-4);
        assertEquals(0f, RegrasCena.pose(C, 0).zoom(), 1e-4);
    }

    @Test
    void localParaMundoRespeitaOGiro() {
        Cena.Local frente = new Cena.Local(0, 0, 10);
        Cena.Local direita = new Cena.Local(10, 0, 0);
        // Giro 0: frente = sul (+z); direita de quem olha para o sul = oeste (-x).
        assertArrayEquals(new double[] {0, 0, 10}, RegrasCena.paraMundo(0, 0, 0, 0f, frente));
        assertArrayEquals(new double[] {-10, 0, 0}, RegrasCena.paraMundo(0, 0, 0, 0f, direita));
        // Giro 180: frente = norte (-z); direita = leste (+x).
        assertArrayEquals(new double[] {0, 0, -10}, RegrasCena.paraMundo(0, 0, 0, 180f, frente));
        assertArrayEquals(new double[] {10, 0, 0}, RegrasCena.paraMundo(0, 0, 0, 180f, direita));
        // Altura e âncora somam direto.
        assertArrayEquals(new double[] {5, 71.7, 3}, RegrasCena.paraMundo(5, 70, 3, 90f, new Cena.Local(0, 1.7, 0)));
    }

    @Test
    void olharEGiroBatemComOJogo() {
        float[] sul = RegrasCena.olhar(0, 0, 1);
        assertEquals(0f, sul[0], 1e-4);
        assertEquals(0f, sul[1], 1e-4);
        assertEquals(90f, RegrasCena.olhar(-1, 0, 0)[0], 1e-4); // oeste
        assertEquals(-90f, RegrasCena.olhar(1, 0, 0)[0], 1e-4); // leste
        assertEquals(-45f, RegrasCena.olhar(0, 1, 1)[1], 1e-4); // para cima = inclinação negativa
        assertEquals(RegrasCena.olhar(3, 0, -2)[0], RegrasCena.giroDe(3, -2), 1e-4);
        assertEquals(0f, RegrasCena.giroDe(0, 0), 1e-4);
        // Volta: o giro do portão leva "frente" para a direção pedida.
        float g = RegrasCena.giroDe(0.6, -0.8);
        double[] f = RegrasCena.paraMundo(0, 0, 0, g, new Cena.Local(0, 0, 1));
        assertEquals(0.6, f[0], 1e-6);
        assertEquals(-0.8, f[2], 1e-6);
    }

    @Test
    void pretoNaEntradaENoFim() {
        assertEquals(1f, RegrasCena.preto(C, 0), 1e-4);
        assertEquals(0f, RegrasCena.preto(C, 12), 1e-4);
        assertEquals(0f, RegrasCena.preto(C, 200), 1e-4);
        assertEquals(1f, RegrasCena.preto(C, 320), 1e-4);
        assertEquals(1f, RegrasCena.preto(C, 359), 1e-4);
    }

    @Test
    void fasesSemPular() {
        double sem = RegrasCena.SEM_PULO;
        RegrasCena.Quadro antes = RegrasCena.quadro(C, -RegrasCena.ENTRADA, sem);
        assertFalse(antes.camera());
        assertEquals(0f, antes.preto(), 1e-4);
        RegrasCena.Quadro troca = RegrasCena.quadro(C, 0, sem);
        assertTrue(troca.camera());
        assertEquals(1f, troca.preto(), 1e-4); // a troca de câmera acontece no preto
        assertTrue(RegrasCena.quadro(C, 100, sem).camera());
        RegrasCena.Quadro volta = RegrasCena.quadro(C, C.fim(), sem);
        assertFalse(volta.camera());
        assertEquals(1f, volta.preto(), 1e-4); // volta ao jogador ainda no preto
        assertEquals(0.5f, RegrasCena.quadro(C, C.fim() + C.saida() / 2.0, sem).preto(), 1e-4);
        assertTrue(RegrasCena.quadro(C, C.fim() + C.saida(), sem).acabou());
    }

    @Test
    void pularEscureceEVolta() {
        double pulou = 100;
        RegrasCena.Quadro q0 = RegrasCena.quadro(C, 100, pulou);
        assertTrue(q0.camera());
        assertEquals(0f, q0.preto(), 1e-4);
        RegrasCena.Quadro q1 = RegrasCena.quadro(C, 100 + RegrasCena.ESCURECER_PULO, pulou);
        assertFalse(q1.camera());
        assertEquals(1f, q1.preto(), 1e-4);
        assertTrue(RegrasCena.quadro(C, 100 + RegrasCena.ESCURECER_PULO + C.saida(), pulou).acabou());
        // Antes de pular, tudo igual a sem pulo.
        assertEquals(RegrasCena.quadro(C, 50, RegrasCena.SEM_PULO), RegrasCena.quadro(C, 50, pulou));
        // Pular no preto do fim não acende a tela.
        assertEquals(1f, RegrasCena.quadro(C, 330, 330).preto(), 1e-4);
    }

    @Test
    void legendaApareceEsome() {
        Cena.Legenda l = C.legendas().get(0);
        assertEquals(0f, RegrasCena.legenda(l, l.entra() - 1), 1e-4);
        assertEquals(0f, RegrasCena.legenda(l, l.entra()), 1e-4);
        assertEquals(1f, RegrasCena.legenda(l, l.entra() + RegrasCena.LEGENDA_ENTRA), 1e-4);
        assertEquals(0f, RegrasCena.legenda(l, l.sai()), 1e-4);
        // A segunda linha entra 0,6 s depois da primeira.
        assertEquals(12, C.legendas().get(1).entra() - l.entra());
    }

    @Test
    void sonsEmOrdemEDaTabelaDaLore() {
        int antes = -1;
        for (Cena.Som s : C.sons()) {
            assertTrue(s.tick() >= antes, "sons fora de ordem: " + s.id());
            assertTrue(s.id().startsWith("minecraft:"), "só som vanilla: " + s.id());
            assertTrue(s.tom() >= 0.5f && s.tom() <= 0.7f, "tom entre 0,5 e 0,7: " + s.id());
            antes = s.tick();
        }
        // Tudo corta no preto do texto; só o sino quase mudo toca depois.
        long depois = C.sons().stream().filter(s -> s.tick() >= C.cortaSons()).count();
        assertEquals(1, depois);
    }

    @Test
    void problemasAcusaCenaQuebrada() {
        Cena ruim = new Cena("x", 10, 0, List.of(new Cena.Plano(5, 5, new Cena.Local(0, 0, 0), new Cena.Local(0, 0, 0),
                new Cena.Local(0, 0, 1), new Cena.Local(0, 0, 1), 0, 0)), List.of(new Cena.Ponto(0, 2f)),
                List.of(new Cena.Som(20, "ambient.cave", 2f, 0.1f)), List.of(), -1);
        List<String> p = RegrasCena.problemas(ruim);
        assertTrue(p.size() >= 5, p.toString());
    }

    @Test
    void vistasLimpaRepetidoEVazio() {
        assertEquals(List.of("a", "b"), RegrasCena.limparVistas(java.util.Arrays.asList("a", "", "a", null, "b", " ")));
        List<String> muitas = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) muitas.add("c" + i);
        assertEquals(RegrasCena.MAX_VISTAS, RegrasCena.limparVistas(muitas).size());
    }

    @Test
    void idsNoDocumento() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        assertTrue(ids.contains("`odteste:" + IdsCena.DADO_VISTAS + "`"), "falta odteste:cenas_vistas em docs/ids.md");
        assertTrue(ids.contains("`odteste:" + IdsCena.PACOTE_TOCAR + "`"), "falta odteste:tocar_cena em docs/ids.md");
        assertTrue(ids.contains("`odteste:" + IdsCena.CAMADA + "`"), "falta odteste:cena em docs/ids.md");
        for (String c : IdsCena.CENAS) assertTrue(ids.contains("`" + c + "`"), "falta a cena " + c + " em docs/ids.md");
    }

    @Test
    void textosDasCenasNasDuasLinguas() throws Exception {
        Path lang = Path.of("src/main/resources/assets/odteste/lang");
        String pt = Files.readString(lang.resolve("pt_br.json"));
        String en = Files.readString(lang.resolve("en_us.json"));
        for (Cena c : CenasConhecidas.todas().values()) {
            for (Cena.Legenda l : c.legendas()) {
                assertTrue(pt.contains("\"" + l.chave() + "\""), "falta " + l.chave() + " em pt_br");
                assertTrue(en.contains("\"" + l.chave() + "\""), "falta " + l.chave() + " em en_us");
            }
        }
        assertTrue(pt.contains("\"odteste.cena.pular\""));
    }

    private static void assertArrayEquals(double[] esperado, double[] obtido) {
        assertEquals(esperado.length, obtido.length);
        for (int i = 0; i < esperado.length; i++) assertEquals(esperado[i], obtido[i], 1e-6, "posição " + i);
    }

    @Test
    void modoPelaForcaDosEfeitos() {
        assertEquals(RegrasCena.Modo.COMPLETO, RegrasCena.Modo.de(1f));
        assertEquals(RegrasCena.Modo.SEM_BALANCO, RegrasCena.Modo.de(0.5f));
        assertEquals(RegrasCena.Modo.SO_TEXTO, RegrasCena.Modo.de(0f));
    }

    @Test
    void suaveNaoBalanca() {
        Cena c = CenasConhecidas.ENTRADA_VILA_CALADA;
        // Plano 2 balança; sem balanço, a câmera fica na reta entre os pontos.
        boolean diferente = false;
        for (int t = 60; t < 140; t += 3) {
            Cena.Local com = RegrasCena.pose(c, t, true).camera();
            Cena.Local sem = RegrasCena.pose(c, t, false).camera();
            if (Math.abs(com.y() - sem.y()) > 1e-6) diferente = true;
        }
        assertTrue(diferente, "o plano 2 devia balançar no modo completo");
        // Fora do plano que balança, os dois são iguais.
        assertEquals(RegrasCena.pose(c, 170, true).camera(), RegrasCena.pose(c, 170, false).camera());
    }

    @Test
    void desligadoEhSoPretoComTexto() {
        Cena c = CenasConhecidas.ENTRADA_VILA_CALADA;
        for (int t = 0; t < c.fim(); t += 7) {
            RegrasCena.Quadro q = RegrasCena.quadro(c, t, RegrasCena.SEM_PULO, RegrasCena.Modo.SO_TEXTO);
            assertTrue(q.camera());
            assertEquals(1f, q.preto(), 1e-6);
        }
        // Escurece igual no começo e some igual no fim.
        assertEquals(RegrasCena.quadro(c, -5, RegrasCena.SEM_PULO).preto(),
                RegrasCena.quadro(c, -5, RegrasCena.SEM_PULO, RegrasCena.Modo.SO_TEXTO).preto(), 1e-6);
        assertTrue(RegrasCena.quadro(c, c.fim() + c.saida(), RegrasCena.SEM_PULO, RegrasCena.Modo.SO_TEXTO).acabou());
        // Pular continua valendo: escurece e devolve o jogador.
        RegrasCena.Quadro pulou = RegrasCena.quadro(c, 100 + RegrasCena.ESCURECER_PULO + c.saida(), 100,
                RegrasCena.Modo.SO_TEXTO);
        assertTrue(pulou.acabou());
    }
}
