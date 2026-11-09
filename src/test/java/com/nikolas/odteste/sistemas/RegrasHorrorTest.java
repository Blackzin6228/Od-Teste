package com.nikolas.odteste.sistemas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.sistemas.RegrasHorror.Alucinacao;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Regras das alucinações e das frases de faixa (Java puro), conferidas contra docs/horror.md. */
class RegrasHorrorTest {

    private static RegrasHorror.Sorteio sorteio(long semente) {
        Random r = new Random(semente);
        return r::nextInt;
    }

    // ---------- espera mínima (horror.md 1.1 e 2.1) ----------

    @Test
    void esperaMinimaIgualAoHorrorMd() {
        assertEquals(0, RegrasHorror.esperaMinima(FaixaLucidez.FIRME, Degrau.COMUM), "Firme + Comum = nada");
        assertEquals(20 * 240, RegrasHorror.esperaMinima(FaixaLucidez.FIRME, Degrau.TOCADO));
        assertEquals(20 * 180, RegrasHorror.esperaMinima(FaixaLucidez.FIRME, Degrau.MARCADO));
        assertEquals(20 * 120, RegrasHorror.esperaMinima(FaixaLucidez.FIRME, Degrau.FUNDO));
        assertEquals(20 * 90, RegrasHorror.esperaMinima(FaixaLucidez.FIRME, Degrau.A_BEIRA));
        assertEquals(20 * 90, RegrasHorror.esperaMinima(FaixaLucidez.FIRME, Degrau.ATRAVESSADO));
        assertEquals(20 * 180, RegrasHorror.esperaMinima(FaixaLucidez.INQUIETO, Degrau.COMUM));
        assertEquals(20 * 120, RegrasHorror.esperaMinima(FaixaLucidez.ABALADO, Degrau.COMUM));
        assertEquals(20 * 75, RegrasHorror.esperaMinima(FaixaLucidez.QUEBRANDO, Degrau.COMUM));
        assertEquals(20 * 75, RegrasHorror.esperaMinima(FaixaLucidez.SURTO, Degrau.COMUM));
        // Vale a pior das duas.
        assertEquals(20 * 75, RegrasHorror.esperaMinima(FaixaLucidez.QUEBRANDO, Degrau.ATRAVESSADO));
        assertEquals(20 * 120, RegrasHorror.esperaMinima(FaixaLucidez.ABALADO, Degrau.TOCADO));
    }

    @Test
    void intervaloEntreAEsperaEQuatroMinutosNoPior() {
        assertEquals(0, RegrasHorror.intervalo(0, sorteio(1)));
        int espera = RegrasHorror.esperaMinima(FaixaLucidez.QUEBRANDO, Degrau.ATRAVESSADO);
        RegrasHorror.Sorteio s = sorteio(2);
        for (int i = 0; i < 500; i++) {
            int t = RegrasHorror.intervalo(espera, s);
            assertTrue(t >= 20 * 75 && t <= 20 * 240, "pior caso: 75 s a 4 min, saiu " + t);
        }
        // Exposição Baixa: 4 a 8 min.
        assertEquals(20 * 240, RegrasHorror.intervalo(20 * 240, limite -> 0));
        assertEquals(20 * 480, RegrasHorror.intervalo(20 * 240, limite -> limite - 1));
    }

    @Test
    void agendaNaoTocaSemFaixaENuncaAntesDaEspera() {
        RegrasHorror.Agenda a = new RegrasHorror.Agenda();
        RegrasHorror.Sorteio s = sorteio(7);
        for (long t = 0; t < 20 * 60 * 10; t++) assertFalse(a.deveTocar(t, 0, () -> false, s));

        int espera = 20 * 75;
        int tocou = 0;
        long ultima = Long.MIN_VALUE;
        for (long t = 0; t < 20 * 60 * 60; t++) { // 1 h no pior caso
            if (a.deveTocar(t, espera, () -> false, s)) {
                if (ultima != Long.MIN_VALUE) assertTrue(t - ultima >= espera);
                ultima = t;
                tocou++;
            }
        }
        // 75 s a 4 min -> de 15 a 48 em 1 h.
        assertTrue(tocou >= 15 && tocou <= 48, "tocou " + tocou);
    }

    @Test
    void grupoPertoCortaAFrequenciaPelaMetade() {
        int espera = 20 * 75;
        int sozinho = contar(espera, false, 11);
        int emGrupo = contar(espera, true, 11);
        // Com grupo, cada vez é pulada uma vez: metade (com folga para o sorteio).
        double razao = (double) emGrupo / sozinho;
        assertTrue(razao > 0.42 && razao < 0.58, "sozinho " + sozinho + ", em grupo " + emGrupo);
    }

    private static int contar(int espera, boolean grupo, long semente) {
        RegrasHorror.Agenda a = new RegrasHorror.Agenda();
        RegrasHorror.Sorteio s = sorteio(semente);
        int n = 0;
        for (long t = 0; t < 20L * 60 * 60 * 10; t++) if (a.deveTocar(t, espera, () -> grupo, s)) n++;
        return n;
    }

    @Test
    void agendaPiorarNaoQuebraAEspera() {
        RegrasHorror.Agenda a = new RegrasHorror.Agenda();
        RegrasHorror.Sorteio s = sorteio(3);
        long t = 0;
        while (!a.deveTocar(t, 20 * 240, () -> false, s)) t++;
        long tocouEm = t;
        // Logo depois, piora para Quebrando: não pode tocar antes de 75 s.
        for (long x = tocouEm + 1; x < tocouEm + 20 * 75; x++) {
            assertFalse(a.deveTocar(x, 20 * 75, () -> false, s));
        }
    }

    @Test
    void agendaAdiarEmpurraCincoSegundos() {
        RegrasHorror.Agenda a = new RegrasHorror.Agenda();
        a.deveTocar(0, 20 * 120, () -> false, sorteio(1));
        a.adiar(1000);
        assertEquals(1100, a.proxima());
        a.zerar();
        assertEquals(-1, a.proxima());
    }

    // ---------- qual (tabelas 1.1 e 2.1) ----------

    @Test
    void cadaFaixaSoTemOQueOHorrorMdPede() {
        assertTrue(RegrasHorror.possiveis(FaixaLucidez.FIRME, Degrau.COMUM, true, null, 0).isEmpty());
        // Exposição Baixa: só som, nada na tela.
        assertEquals(EnumSet.of(Alucinacao.SUSSURRO, Alucinacao.ESTALO),
                EnumSet.copyOf(RegrasHorror.possiveis(FaixaLucidez.FIRME, Degrau.TOCADO, true, null, 0)));
        List<Alucinacao> media = RegrasHorror.possiveis(FaixaLucidez.FIRME, Degrau.MARCADO, true, null, 0);
        assertTrue(media.contains(Alucinacao.PASSOS) && media.contains(Alucinacao.PORTA));
        assertFalse(media.contains(Alucinacao.VULTO));
        List<Alucinacao> alta = RegrasHorror.possiveis(FaixaLucidez.FIRME, Degrau.FUNDO, true, null, 0);
        assertTrue(alta.contains(Alucinacao.VULTO) && alta.contains(Alucinacao.NOME));
        assertFalse(alta.contains(Alucinacao.MOB_FALSO), "mob falso só na Crítica");
        assertTrue(RegrasHorror.possiveis(FaixaLucidez.FIRME, Degrau.A_BEIRA, true, null, 0).contains(Alucinacao.MOB_FALSO));
        assertTrue(RegrasHorror.possiveis(FaixaLucidez.QUEBRANDO, Degrau.COMUM, true, null, 0).contains(Alucinacao.MOB_FALSO));
        assertFalse(RegrasHorror.possiveis(FaixaLucidez.ABALADO, Degrau.COMUM, true, null, 0).contains(Alucinacao.VULTO));
    }

    @Test
    void desligadoNuncaDisparaEfeitoDeTela() {
        for (FaixaLucidez f : FaixaLucidez.values()) {
            for (Degrau d : Degrau.values()) {
                for (Alucinacao a : RegrasHorror.possiveis(f, d, false, null, 0)) {
                    assertFalse(a.visual, a + " é visual e apareceu com efeitos fortes desligado");
                }
            }
        }
        List<Alucinacao> pior = RegrasHorror.possiveis(FaixaLucidez.QUEBRANDO, Degrau.ATRAVESSADO, false, null, 0);
        assertTrue(pior.contains(Alucinacao.NOME), "o nome sussurrado é texto: fica no DESLIGADO");
        assertTrue(pior.contains(Alucinacao.SUSSURRO), "som fica no DESLIGADO");
    }

    @Test
    void mesmaAlucinacaoNaoRepeteAntesDoCooldown() {
        RegrasHorror.Recentes r = new RegrasHorror.Recentes();
        r.marcar(Alucinacao.SUSSURRO, 1000);
        assertFalse(r.livre(Alucinacao.SUSSURRO, 1000 + RegrasHorror.COOLDOWN_MESMA - 1));
        assertTrue(r.livre(Alucinacao.SUSSURRO, 1000 + RegrasHorror.COOLDOWN_MESMA));
        assertTrue(r.livre(Alucinacao.ESTALO, 1001));
        assertTrue(r.livre(Alucinacao.SUSSURRO, 10), "relógio voltou (outro mundo): não trava para sempre");

        r.marcar(Alucinacao.ESTALO, 1000);
        assertEquals(Optional.empty(), RegrasHorror.escolher(FaixaLucidez.FIRME, Degrau.TOCADO, true, r, 1001, sorteio(1)));
    }

    @Test
    void escolhaSaiDoQuePodeEPesaAFaixaPropria() {
        RegrasHorror.Sorteio s = sorteio(42);
        int[] vezes = new int[Alucinacao.values().length];
        for (int i = 0; i < 6000; i++) {
            Alucinacao a = RegrasHorror.escolher(FaixaLucidez.FIRME, Degrau.FUNDO, true, null, 0, s).orElseThrow();
            assertTrue(a.permitida(FaixaLucidez.FIRME, Degrau.FUNDO));
            vezes[a.ordinal()]++;
        }
        assertTrue(vezes[Alucinacao.VULTO.ordinal()] > vezes[Alucinacao.SUSSURRO.ordinal()],
                "as da própria faixa saem mais que as leves");
        for (Alucinacao a : RegrasHorror.possiveis(FaixaLucidez.FIRME, Degrau.FUNDO, true, null, 0)) {
            assertTrue(vezes[a.ordinal()] > 0, a + " nunca saiu");
        }
    }

    // ---------- distâncias ----------

    @Test
    void vultoNasceA12MaisNoCantoESomeAoOlhar() {
        int vida = RegrasHorror.VULTO_VIDA;
        assertFalse(RegrasHorror.vultoSome(Math.cos(Math.toRadians(40)), 14 * 14, 10, vida)); // no canto, longe
        assertTrue(RegrasHorror.vultoSome(Math.cos(Math.toRadians(10)), 14 * 14, 10, vida)); // olhou direto
        assertTrue(RegrasHorror.vultoSome(0, 4 * 4, 10, vida)); // chegou a 4 blocos
        assertTrue(RegrasHorror.vultoSome(0, 14 * 14, vida, vida));
        assertTrue(RegrasHorror.vultoSome(0, 14 * 14, RegrasHorror.VIDA_SUAVE, RegrasHorror.VIDA_SUAVE), "SUAVE: 1 s");

        RegrasHorror.Sorteio s = sorteio(5);
        Set<Boolean> lados = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            int g = RegrasHorror.vultoAngulo(s);
            assertTrue(Math.abs(g) >= 35 && Math.abs(g) <= 50, "ângulo " + g);
            assertTrue(Math.cos(Math.toRadians(g)) < RegrasHorror.COS_OLHOU, "nasce fora do cone de olhar");
            lados.add(g > 0);
            int d = RegrasHorror.vultoDistancia(s);
            assertTrue(d >= 12 && d <= 16, "horror.md: 12+ blocos, saiu " + d);
        }
        assertEquals(2, lados.size(), "aparece dos dois lados");
    }

    @Test
    void mobFalsoSomeA3Blocos() {
        int vida = RegrasHorror.MOB_VIDA;
        assertFalse(RegrasHorror.mobSome(4 * 4, 10, vida));
        assertTrue(RegrasHorror.mobSome(3 * 3, 10, vida));
        assertTrue(RegrasHorror.mobSome(15 * 15, vida, vida));
        RegrasHorror.Sorteio s = sorteio(9);
        for (int i = 0; i < 100; i++) {
            int d = RegrasHorror.mobDistancia(s);
            assertTrue(d >= 14 && d <= 20);
        }
        assertEquals(12, RegrasHorror.mobReaparece(17.4));
        assertEquals(RegrasHorror.MOB_REAPARECE_MINIMO, RegrasHorror.mobReaparece(9), "nunca a menos de 8");
    }

    @Test
    void portaFicaAbertaPoucoTempo() {
        RegrasHorror.Sorteio s = sorteio(11);
        for (int i = 0; i < 100; i++) {
            int d = RegrasHorror.portaDuracao(s);
            assertTrue(d >= 30 && d <= 60);
        }
    }

    // ---------- corpo (1.3 e 2.3) ----------

    @Test
    void maoTremeSoNasSituacoesDoHorrorMd() {
        // Exposição Média: só segurando item paranormal parado.
        assertEquals(0f, RegrasHorror.tremorMao(FaixaLucidez.FIRME, Degrau.MARCADO, false, true, false));
        assertEquals(0f, RegrasHorror.tremorMao(FaixaLucidez.FIRME, Degrau.MARCADO, true, false, false));
        assertTrue(RegrasHorror.tremorMao(FaixaLucidez.FIRME, Degrau.MARCADO, true, true, false) > 0);
        assertEquals(0f, RegrasHorror.tremorMao(FaixaLucidez.FIRME, Degrau.TOCADO, true, true, false), "Baixa: nada");
        // Lucidez Média: ao mirar ou usar item.
        assertEquals(0f, RegrasHorror.tremorMao(FaixaLucidez.ABALADO, Degrau.COMUM, false, true, false));
        assertTrue(RegrasHorror.tremorMao(FaixaLucidez.ABALADO, Degrau.COMUM, false, false, true) > 0);
        assertEquals(0f, RegrasHorror.tremorMao(FaixaLucidez.INQUIETO, Degrau.COMUM, false, false, true), "Alta: nada");
        assertTrue(RegrasHorror.tremorMao(FaixaLucidez.QUEBRANDO, Degrau.COMUM, false, false, true)
                > RegrasHorror.tremorMao(FaixaLucidez.ABALADO, Degrau.COMUM, false, false, true));
    }

    @Test
    void visaoBalancaParadoNaExposicaoEAndandoNaLucidez() {
        assertEquals(0f, RegrasHorror.balanco(FaixaLucidez.FIRME, Degrau.MARCADO, true));
        assertTrue(RegrasHorror.balanco(FaixaLucidez.FIRME, Degrau.FUNDO, true) > 0);
        assertEquals(0f, RegrasHorror.balanco(FaixaLucidez.FIRME, Degrau.FUNDO, false));
        assertTrue(RegrasHorror.balanco(FaixaLucidez.QUEBRANDO, Degrau.COMUM, false) > 0);
        assertEquals(0f, RegrasHorror.balanco(FaixaLucidez.QUEBRANDO, Degrau.COMUM, true));
        assertEquals(0f, RegrasHorror.balanco(FaixaLucidez.ABALADO, Degrau.COMUM, false));
    }

    @Test
    void dessaturacaoDoHorrorMd() {
        assertEquals(0f, RegrasHorror.dessaturar(FaixaLucidez.FIRME, Degrau.MARCADO));
        assertEquals(0.20f, RegrasHorror.dessaturar(FaixaLucidez.FIRME, Degrau.FUNDO));
        assertEquals(0.45f, RegrasHorror.dessaturar(FaixaLucidez.FIRME, Degrau.ATRAVESSADO));
        assertEquals(0.15f, RegrasHorror.dessaturar(FaixaLucidez.ABALADO, Degrau.COMUM));
        assertEquals(0.40f, RegrasHorror.dessaturar(FaixaLucidez.QUEBRANDO, Degrau.COMUM));
        assertEquals(0.45f, RegrasHorror.dessaturar(FaixaLucidez.QUEBRANDO, Degrau.A_BEIRA), "vale o maior");
    }

    // ---------- som ----------

    @Test
    void pitchEEcoNaFaixaTocavel() {
        RegrasHorror.Sorteio s = sorteio(13);
        for (int i = 0; i < 200; i++) {
            float p = RegrasHorror.pitch(s);
            assertTrue(p >= 0.5f && p <= 0.7f);
            float base = RegrasHorror.pitchComEco(s);
            for (int k = 0; k < RegrasHorror.ECO_TICKS.length; k++) {
                float e = RegrasHorror.ecoPitch(base, k);
                assertTrue(e >= 0.5f && e <= 0.7f, "eco " + k + " pitch " + e);
            }
        }
        assertEquals(6, RegrasHorror.ECO_TICKS[1]);
        assertEquals(14, RegrasHorror.ECO_TICKS[2]);
        assertEquals(0.3f, RegrasHorror.ecoVolume(0.3f, 0));
        assertEquals(0.15f, RegrasHorror.ecoVolume(0.3f, 1));
        assertEquals(0.075f, RegrasHorror.ecoVolume(0.3f, 2));
        assertEquals(RegrasHorror.VOLUME_MAX, RegrasHorror.ecoVolume(2f, 0), "nunca passa de 0,6");
        assertEquals(0.6f, RegrasHorror.ecoPitch(0.65f, 1), 1e-6);
    }

    // ---------- lentidão (servidor) ----------

    @Test
    void lentidaoSoNaLucidezBaixaComPoucaLuz() {
        for (int luz = 0; luz <= 15; luz++) {
            assertEquals(0, RegrasLucidez.lentidao(FaixaLucidez.FIRME, luz));
            assertEquals(0, RegrasLucidez.lentidao(FaixaLucidez.INQUIETO, luz));
            assertEquals(0, RegrasLucidez.lentidao(FaixaLucidez.ABALADO, luz));
        }
        assertEquals(-0.08, RegrasLucidez.lentidao(FaixaLucidez.QUEBRANDO, 0));
        assertEquals(-0.08, RegrasLucidez.lentidao(FaixaLucidez.QUEBRANDO, RegrasLucidez.LUZ_FRACA));
        assertEquals(0, RegrasLucidez.lentidao(FaixaLucidez.QUEBRANDO, RegrasLucidez.LUZ_FRACA + 1), "com luz, nada");
        assertEquals(-0.08, RegrasLucidez.lentidao(FaixaLucidez.SURTO, 3));
        assertEquals(0.05f, RegrasHorror.LENTIDAO_VULTO, "vulto perto: −5%, nunca mais");
    }

    // ---------- frases (seção 3) ----------

    @Test
    void todaFaixaTemTresFrasesNasDuasLinguas() throws Exception {
        String pt = Files.readString(Path.of("src/main/resources/assets/odteste/lang/pt_br.json"));
        String en = Files.readString(Path.of("src/main/resources/assets/odteste/lang/en_us.json"));
        Set<String> simbolos = new HashSet<>();
        for (Degrau d : Degrau.values()) {
            for (int v = 1; v <= FrasesFaixa.VARIACOES; v++) {
                assertTrue(pt.contains("\"" + FrasesFaixa.chave(d, v) + "\""), FrasesFaixa.chave(d, v));
                assertTrue(en.contains("\"" + FrasesFaixa.chave(d, v) + "\""), FrasesFaixa.chave(d, v));
            }
            simbolos.add(FrasesFaixa.simbolo(d));
        }
        assertEquals(Degrau.values().length, simbolos.size(), "um símbolo diferente por degrau");
        simbolos.clear();
        for (FaixaLucidez f : FaixaLucidez.values()) {
            for (int v = 1; v <= FrasesFaixa.VARIACOES; v++) {
                assertTrue(pt.contains("\"" + FrasesFaixa.chave(f, v) + "\""), FrasesFaixa.chave(f, v));
                assertTrue(en.contains("\"" + FrasesFaixa.chave(f, v) + "\""), FrasesFaixa.chave(f, v));
            }
            simbolos.add(FrasesFaixa.simbolo(f));
        }
        assertEquals(FaixaLucidez.values().length, simbolos.size(), "um símbolo diferente por faixa");
        for (String k : List.of("odteste.horror.sussurro.nome", "odteste.horror.sussurro.nome.2", "odteste.horror.sussurro.voz")) {
            assertTrue(pt.contains("\"" + k + "\"") && en.contains("\"" + k + "\""), k);
        }
        assertEquals("odteste.horror.exposicao.critica.2", FrasesFaixa.chave(Degrau.ATRAVESSADO, 2));
        assertEquals("odteste.horror.lucidez.baixa.1", FrasesFaixa.chave(FaixaLucidez.QUEBRANDO, 1));
    }

    @Test
    void fraseNuncaRepeteDuasVezesSeguidas() {
        RegrasHorror.Sorteio s = sorteio(17);
        Set<Integer> vistas = new HashSet<>();
        int anterior = 0;
        for (int i = 0; i < 300; i++) {
            int v = FrasesFaixa.sortear(anterior, s);
            assertTrue(v >= 1 && v <= FrasesFaixa.VARIACOES);
            assertNotEquals(anterior, v);
            vistas.add(v);
            anterior = v;
        }
        assertEquals(3, vistas.size(), "as três aparecem");
    }
}
