package com.nikolas.odteste.cenas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.historia.vilacalada.RegrasFxSangue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class RegrasRoteiroTest {
    private static final double E = 1e-6;
    private static final Set<String> FX = Set.copyOf(RegrasFxSangue.EFEITOS);
    private static final Path PASTA = Path.of("src/main/resources/data/odteste/cenas");
    private static final RegrasRoteiro.Ancora A0 = new RegrasRoteiro.Ancora(0, 0, 0, 0);
    private static final RegrasRoteiro.Olhos O0 = new RegrasRoteiro.Olhos(0, 0, 0, 0, 0);

    private static Roteiro ler(String json) {
        return LeitorRoteiro.ler("teste", json);
    }

    /** Três chaves (x local para a direita = -x do mundo com giro 0), só a posição muda. */
    private static List<RegrasRoteiro.ChaveMundo> tres(String curva) {
        Roteiro r = ler("{\"duracao\":100,\"chaves\":["
                + "{\"t\":0,\"pos\":[0,0,0],\"curva\":\"" + curva + "\"},"
                + "{\"t\":40,\"pos\":[4,2,0],\"curva\":\"" + curva + "\"},"
                + "{\"t\":100,\"pos\":[10,0,6]}]}");
        return RegrasRoteiro.resolver(r, A0, O0, 70);
    }

    // ------------------------------------------------------------------ arquivos do mod

    @Test
    void cenasDoModAbremSemProblema() throws Exception {
        String pt = Files.readString(Path.of("src/main/resources/assets/odteste/lang/pt_br.json"));
        String en = Files.readString(Path.of("src/main/resources/assets/odteste/lang/en_us.json"));
        String ids = Files.readString(Path.of("docs/ids.md"));
        int n = 0;
        try (Stream<Path> arquivos = Files.list(PASTA)) {
            for (Path f : arquivos.filter(p -> p.toString().endsWith(".json")).toList()) {
                String id = f.getFileName().toString().replace(".json", "");
                Roteiro r = LeitorRoteiro.ler(id, Files.readString(f));
                assertEquals(List.of(), RegrasRoteiro.problemas(r, FX), "cena " + id);
                assertTrue(ids.contains("`" + id + "`"), "cena " + id + " em docs/ids.md");
                assertTrue(Set.of(IdsCena.LUGAR_JOGADOR, IdsCena.LUGAR_SINO_RACHADO).contains(r.ancora()),
                        "lugar conhecido em " + id);
                for (Roteiro.Evento e : r.eventos()) {
                    if (e instanceof Roteiro.Texto t) {
                        assertTrue(pt.contains("\"" + t.chave() + "\""), "pt_br: " + t.chave());
                        assertTrue(en.contains("\"" + t.chave() + "\""), "en_us: " + t.chave());
                    }
                }
                n++;
            }
        }
        assertTrue(n >= 1, "pelo menos a cena de exemplo");
    }

    @Test
    void chegadaTerminaNosOlhosDoJogador() throws Exception {
        Roteiro r = LeitorRoteiro.ler(IdsCena.CHEGADA_VILA_CALADA,
                Files.readString(PASTA.resolve(IdsCena.CHEGADA_VILA_CALADA + ".json")));
        assertEquals(IdsCena.LUGAR_SINO_RACHADO, r.ancora());
        assertTrue(r.saidaDireta(), "volta direto para a visão do jogador");
        assertTrue(r.mostrarJogador());
        RegrasRoteiro.Olhos o = new RegrasRoteiro.Olhos(10, 70, -5, 135, 12);
        var a = new RegrasRoteiro.Ancora(40, 90, 30, 20);
        var k = RegrasRoteiro.resolver(r, a, o, 70);
        RegrasRoteiro.Pose fim = RegrasRoteiro.pose(k, r.duracao());
        assertEquals(10, fim.x(), E);
        assertEquals(70, fim.y(), E);
        assertEquals(-5, fim.z(), E);
        assertEquals(12, fim.inclinacao(), 1e-4);
        assertEquals(0, ((fim.giro() - 135) % 360 + 360) % 360, 1e-3);
        assertEquals(70, fim.fov(), 1e-4, "termina com o campo de visão do jogador");
        // No meio passa perto do sino (a âncora).
        RegrasRoteiro.Pose sino = RegrasRoteiro.pose(k, 319);
        assertTrue(Math.abs(sino.x() - a.x()) < 8 && Math.abs(sino.z() - a.z()) < 8, "perto do sino em 319");
    }

    // ------------------------------------------------------------------ curvas

    @Test
    void catmullPassaPelasChaves() {
        var k = tres("catmull");
        assertEquals(0, RegrasRoteiro.pose(k, 0).x(), E);
        assertEquals(-4, RegrasRoteiro.pose(k, 40).x(), E);
        assertEquals(2, RegrasRoteiro.pose(k, 40).y(), E);
        assertEquals(-10, RegrasRoteiro.pose(k, 100).x(), E);
        // Antes e depois das pontas: fica na ponta.
        assertEquals(0, RegrasRoteiro.pose(k, -5).x(), E);
        assertEquals(6, RegrasRoteiro.pose(k, 500).z(), E);
    }

    @Test
    void catmullSemTrancoNaChaveDoMeio() {
        var k = tres("catmull");
        double h = 1e-3;
        for (int d = 0; d < 3; d++) {
            double antes = (val(k, 40, d) - val(k, 40 - h, d)) / h;
            double depois = (val(k, 40 + h, d) - val(k, 40, d)) / h;
            assertEquals(antes, depois, 1e-2, "velocidade igual dos dois lados (eixo " + d + ")");
        }
    }

    private static double val(List<RegrasRoteiro.ChaveMundo> k, double t, int d) {
        RegrasRoteiro.Pose p = RegrasRoteiro.pose(k, t);
        return d == 0 ? p.x() : d == 1 ? p.y() : p.z();
    }

    @Test
    void linearNoMeio() {
        var k = tres("linear");
        assertEquals(-2, RegrasRoteiro.pose(k, 20).x(), E);
        assertEquals(1, RegrasRoteiro.pose(k, 20).y(), E);
    }

    @Test
    void corteSeguraEPula() {
        var k = tres("corte");
        assertEquals(0, RegrasRoteiro.pose(k, 39.9).x(), E);
        assertEquals(-4, RegrasRoteiro.pose(k, 40).x(), E);
        assertEquals(-4, RegrasRoteiro.pose(k, 99).x(), E);
    }

    @Test
    void ritmos() {
        for (Roteiro.Ritmo r : Roteiro.Ritmo.values()) {
            assertEquals(0, RegrasRoteiro.ritmo(r, 0), E);
            assertEquals(1, RegrasRoteiro.ritmo(r, 1), E);
            assertEquals(1, RegrasRoteiro.ritmo(r, 3), E);
        }
        assertEquals(0.5, RegrasRoteiro.ritmo(Roteiro.Ritmo.SUAVE, 0.5), E);
        assertTrue(RegrasRoteiro.ritmo(Roteiro.Ritmo.ENTRA, 0.3) < 0.3);
        assertTrue(RegrasRoteiro.ritmo(Roteiro.Ritmo.SAI, 0.3) > 0.3);
    }

    @Test
    void giroPeloLadoMaisCurto() {
        assertEquals(370, RegrasRoteiro.desenrolar(350, 10), E);
        assertEquals(-10, RegrasRoteiro.desenrolar(10, 350), E);
        assertEquals(90, RegrasRoteiro.desenrolar(90, 90), E);
        Roteiro r = ler("{\"duracao\":20,\"chaves\":[{\"t\":0,\"pos\":[0,0,0],\"giro\":170,\"curva\":\"linear\"},"
                + "{\"t\":20,\"pos\":[0,0,0],\"giro\":-170}]}");
        var k = RegrasRoteiro.resolver(r, A0, O0, 70);
        assertEquals(180, RegrasRoteiro.pose(k, 10).giro(), 1e-3, "passa por 180, não por 0");
    }

    @Test
    void olhosEhAVisaoDoJogador() {
        Roteiro r = ler("{\"duracao\":10,\"chaves\":[{\"t\":0,\"em\":\"olhos\",\"pos\":[0,0,0],\"giro\":0}]}");
        var o = new RegrasRoteiro.Olhos(3, 64, 7, 45, -20);
        RegrasRoteiro.Pose p = RegrasRoteiro.pose(RegrasRoteiro.resolver(r, A0, o, 85), 5);
        assertEquals(3, p.x(), E);
        assertEquals(64, p.y(), E);
        assertEquals(45, p.giro(), 1e-4);
        assertEquals(-20, p.inclinacao(), 1e-4);
        assertEquals(85, p.fov(), 1e-4, "sem fov na chave: o do jogador");
    }

    @Test
    void olharParaOJogador() {
        // Câmera 2 blocos na frente dos olhos (z+ = sul, giro 0): para olhar o jogador, vira para o norte (180).
        Roteiro r = ler("{\"duracao\":10,\"chaves\":[{\"t\":0,\"em\":\"olhos\",\"pos\":[0,0,2],\"olhar\":\"jogador\"}]}");
        RegrasRoteiro.Pose p = RegrasRoteiro.pose(RegrasRoteiro.resolver(r, A0, O0, 70), 0);
        assertEquals(2, p.z(), E);
        assertEquals(180, Math.abs(p.giro()), 1e-3);
        assertEquals(0, p.inclinacao(), 1e-3);
    }

    @Test
    void entreFicaNoMeio() {
        Roteiro r = ler("{\"duracao\":10,\"chaves\":[{\"t\":0,\"em\":\"entre\",\"entre\":0.5,\"pos\":[0,0,0]}]}");
        var a = new RegrasRoteiro.Ancora(10, 20, 30, 0);
        RegrasRoteiro.Pose p = RegrasRoteiro.pose(RegrasRoteiro.resolver(r, a, O0, 70), 0);
        assertEquals(5, p.x(), E);
        assertEquals(10, p.y(), E);
        assertEquals(15, p.z(), E);
    }

    @Test
    void inclinacaoEFovNoLimite() {
        Roteiro r = ler("{\"duracao\":10,\"chaves\":[{\"t\":0,\"pos\":[0,0,0],\"inclinacao\":300,\"fov\":149}]}");
        RegrasRoteiro.Pose p = RegrasRoteiro.pose(RegrasRoteiro.resolver(r, A0, O0, 70), 0);
        assertTrue(p.inclinacao() < 90);
        assertTrue(p.fov() <= 150);
    }

    // ------------------------------------------------------------------ eventos

    @Test
    void veuSobeDesceETrocaDeCor() {
        Roteiro r = ler("{\"duracao\":100,\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}],\"eventos\":["
                + "{\"t\":0,\"tipo\":\"fade\",\"de\":1,\"ate\":0,\"duracao\":20},"
                + "{\"t\":50,\"tipo\":\"fade\",\"cor\":\"vermelho\",\"ate\":0.6,\"duracao\":10},"
                + "{\"t\":55,\"tipo\":\"fade\",\"ate\":0,\"duracao\":10}]}");
        var ev = r.eventos();
        assertEquals(1, RegrasRoteiro.veu(ev, 0).alfa(), E);
        assertEquals(0.5, RegrasRoteiro.veu(ev, 10).alfa(), E);
        assertEquals(0, RegrasRoteiro.veu(ev, 30).alfa(), E);
        assertEquals(LeitorRoteiro.VERMELHO, RegrasRoteiro.veu(ev, 52).cor());
        assertEquals(0.3, RegrasRoteiro.veu(ev, 55).alfa(), 1e-5, "parte de onde estava");
        assertEquals(0, RegrasRoteiro.veu(ev, 70).alfa(), E);
        assertEquals(0, RegrasRoteiro.veu(List.of(), 10).alfa(), E);
    }

    @Test
    void tremorSomeEDiminui() {
        List<Roteiro.Evento> ev = List.of(new Roteiro.Tremor(10, 1f, 20));
        float[] fora = RegrasRoteiro.tremor(ev, 5, 1f);
        assertEquals(0, fora[0] + fora[1] + fora[2], E);
        float[] zero = RegrasRoteiro.tremor(ev, 15, 0f);
        assertEquals(0, Math.abs(zero[0]) + Math.abs(zero[1]), E);
        for (double t = 10; t < 30; t += 0.25) {
            float[] v = RegrasRoteiro.tremor(ev, t, 1f);
            double env = 1 - (t - 10) / 20;
            assertTrue(Math.abs(v[0]) <= 2.0 * env + E && Math.abs(v[1]) <= 1.6 * env + E && Math.abs(v[2]) <= 1.2 * env + E);
            float[] metade = RegrasRoteiro.tremor(ev, t, 0.25f);
            assertEquals(v[0] * 0.25, metade[0], 1e-4, "efeitos fortes diminui");
        }
    }

    @Test
    void barrasEntramESaem() {
        Roteiro direta = ler("{\"duracao\":100,\"barras\":0.1,\"saida\":\"direta\",\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}]}");
        Roteiro preto = ler("{\"duracao\":100,\"barras\":0.1,\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}]}");
        assertEquals(0, RegrasRoteiro.barras(direta, 0), E);
        assertEquals(0.1, RegrasRoteiro.barras(direta, 50), 1e-6);
        assertEquals(0, RegrasRoteiro.barras(direta, 100), E);
        assertEquals(0.1, RegrasRoteiro.barras(preto, 100), 1e-6);
        Roteiro sem = ler("{\"duracao\":100,\"barras\":0,\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}]}");
        assertEquals(0, RegrasRoteiro.barras(sem, 50), E);
    }

    @Test
    void textoEntraESai() {
        var x = new Roteiro.Texto(100, "odteste.x", 0, 60);
        assertEquals(0, RegrasRoteiro.texto(x, 99), E);
        assertEquals(1, RegrasRoteiro.texto(x, 130), E);
        assertEquals(0, RegrasRoteiro.texto(x, 160), E);
    }

    // ------------------------------------------------------------------ fases e restauração

    @Test
    void fasesSaidaComPreto() {
        Roteiro r = ler("{\"duracao\":100,\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}]}");
        RegrasCena.Quadro q = RegrasRoteiro.fase(r, -RegrasCena.ENTRADA, RegrasCena.SEM_PULO);
        assertFalse(q.camera());
        assertEquals(0, q.preto(), E);
        q = RegrasRoteiro.fase(r, 0, RegrasCena.SEM_PULO);
        assertTrue(q.camera());
        assertEquals(1, q.preto(), E);
        assertEquals(0, RegrasRoteiro.fase(r, 50, RegrasCena.SEM_PULO).preto(), E);
        assertTrue(RegrasRoteiro.fase(r, 99, RegrasCena.SEM_PULO).preto() > 0.85);
        q = RegrasRoteiro.fase(r, 100, RegrasCena.SEM_PULO);
        assertFalse(q.camera());
        assertFalse(q.acabou());
        assertTrue(RegrasRoteiro.fase(r, 100 + RegrasRoteiro.SAIDA, RegrasCena.SEM_PULO).acabou());
    }

    @Test
    void fasesSaidaDireta() {
        Roteiro r = ler("{\"duracao\":100,\"saida\":\"direta\",\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}]}");
        assertEquals(0, RegrasRoteiro.fase(r, 99, RegrasCena.SEM_PULO).preto(), E);
        assertTrue(RegrasRoteiro.fase(r, 100, RegrasCena.SEM_PULO).acabou());
    }

    @Test
    void sempreTerminaAntesDoServidorSoltar() {
        // Pule a qualquer hora (ou nunca): a cena acaba no cliente antes do tempo que o servidor segura.
        for (String saida : List.of("preto", "direta")) {
            Roteiro r = ler("{\"duracao\":300,\"saida\":\"" + saida + "\",\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}]}");
            int limite = RegrasRoteiro.ticksProtegido(r.duracao()) - RegrasRoteiro.FOLGA_SERVIDOR;
            for (int pulo = -RegrasCena.ENTRADA - 1; pulo <= 301; pulo += 7) {
                double p = pulo < -RegrasCena.ENTRADA ? RegrasCena.SEM_PULO : pulo;
                boolean acabou = false;
                for (int t = -RegrasCena.ENTRADA; t <= limite - RegrasCena.ENTRADA; t++) {
                    if (RegrasRoteiro.fase(r, t, p).acabou()) {
                        acabou = true;
                        break;
                    }
                }
                assertTrue(acabou, "saída " + saida + ", pulo em " + pulo);
            }
        }
    }

    @Test
    void puloEscureceEVolta() {
        Roteiro r = ler("{\"duracao\":200,\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}]}");
        RegrasCena.Quadro q = RegrasRoteiro.fase(r, 50 + RegrasCena.ESCURECER_PULO - 1, 50);
        assertTrue(q.camera());
        assertTrue(q.preto() > 0.8);
        q = RegrasRoteiro.fase(r, 50 + RegrasCena.ESCURECER_PULO, 50);
        assertFalse(q.camera(), "depois do preto, a câmera já é do jogador");
        assertTrue(RegrasRoteiro.fase(r, 50 + RegrasCena.ESCURECER_PULO + RegrasRoteiro.SAIDA, 50).acabou());
    }

    @Test
    void pularPrecisaDaCenaEDoMestre() {
        assertTrue(RegrasRoteiro.podePular(true, true, false));
        assertFalse(RegrasRoteiro.podePular(false, true, false));
        assertFalse(RegrasRoteiro.podePular(true, false, false));
        assertTrue(RegrasRoteiro.podePular(false, false, true), "mestre assistindo sempre pode");
    }

    // ------------------------------------------------------------------ leitura e conferência

    @Test
    void leitorRecusaFormatoRuim() {
        assertThrows(IllegalArgumentException.class, () -> ler("[1,2]"));
        assertThrows(IllegalArgumentException.class, () -> ler("{\"duracao\":10,\"chaves\":[{\"t\":0}]}"));
        assertThrows(IllegalArgumentException.class,
                () -> ler("{\"duracao\":10,\"chaves\":[{\"t\":0,\"pos\":[0,0,0],\"curva\":\"torta\"}]}"));
        assertThrows(IllegalArgumentException.class,
                () -> ler("{\"duracao\":10,\"chaves\":[{\"t\":0,\"pos\":[0,0,0]}],\"eventos\":[{\"t\":0,\"tipo\":\"explodir\"}]}"));
        assertThrows(IllegalArgumentException.class, () -> LeitorRoteiro.cor("azulzinho"));
        assertEquals(0x123ABC, LeitorRoteiro.cor("#123abc"));
    }

    @Test
    void conferenciaAchaErros() {
        Roteiro r = ler("{\"duracao\":100,\"chaves\":[{\"t\":5,\"pos\":[0,0,0]},{\"t\":3,\"pos\":[0,0,0]}],\"eventos\":["
                + "{\"t\":0,\"tipo\":\"som\",\"som\":\"sem_namespace\"},"
                + "{\"t\":0,\"tipo\":\"fx\",\"efeito\":\"nada\"},"
                + "{\"t\":150,\"tipo\":\"parar_sons\"}]}");
        List<String> p = RegrasRoteiro.problemas(r, FX);
        assertTrue(p.stream().anyMatch(s -> s.contains("tick 0")), p.toString());
        assertTrue(p.stream().anyMatch(s -> s.contains("fora de ordem")), p.toString());
        assertTrue(p.stream().anyMatch(s -> s.contains("namespace")), p.toString());
        assertTrue(p.stream().anyMatch(s -> s.contains("efeito desconhecido")), p.toString());
        assertTrue(p.stream().anyMatch(s -> s.contains("fora da cena")), p.toString());
    }

    @Test
    void idsNovosDocumentados() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (String id : List.of(IdsCena.PACOTE_CINEMA, IdsCena.PACOTE_PARAR, IdsCena.PACOTE_FIM, IdsCena.CAMADA_CINEMA)) {
            assertTrue(ids.contains("`odteste:" + id + "`"), "falta odteste:" + id + " em docs/ids.md");
        }
        assertTrue(ids.contains("`" + IdsCena.LUGAR_SINO_RACHADO + "`"));
    }
}
