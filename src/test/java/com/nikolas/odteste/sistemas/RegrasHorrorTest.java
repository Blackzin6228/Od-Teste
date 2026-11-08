package com.nikolas.odteste.sistemas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Regras das alucinações e das frases de faixa (Java puro). */
class RegrasHorrorTest {

    private static RegrasHorror.Sorteio sorteio(long semente) {
        Random r = new Random(semente);
        return r::nextInt;
    }

    // ---------- nível ----------

    @Test
    void nivelJuntaLucidezEExposicao() {
        assertEquals(0, RegrasHorror.nivel(FaixaLucidez.FIRME, Degrau.COMUM));
        assertEquals(1, RegrasHorror.nivel(FaixaLucidez.INQUIETO, Degrau.COMUM));
        assertEquals(1, RegrasHorror.nivel(FaixaLucidez.FIRME, Degrau.TOCADO));
        assertEquals(3, RegrasHorror.nivel(FaixaLucidez.FIRME, Degrau.A_BEIRA)); // vultos já em À beira
        assertEquals(3, RegrasHorror.nivel(FaixaLucidez.QUEBRANDO, Degrau.COMUM));
        assertEquals(3, RegrasHorror.nivel(FaixaLucidez.ABALADO, Degrau.FUNDO)); // os dois pesados somam 1
        assertEquals(4, RegrasHorror.nivel(FaixaLucidez.QUEBRANDO, Degrau.A_BEIRA));
        assertEquals(4, RegrasHorror.nivel(FaixaLucidez.SURTO, Degrau.ATRAVESSADO));
        assertEquals(0, RegrasHorror.nivel(null, null));
    }

    @Test
    void nivelNuncaPassaDoMaximoENuncaDesceQuandoPiora() {
        for (FaixaLucidez f : FaixaLucidez.values()) {
            int antes = -1;
            for (Degrau d : Degrau.values()) {
                int n = RegrasHorror.nivel(f, d);
                assertTrue(n >= 0 && n <= RegrasHorror.NIVEL_MAX);
                assertTrue(n >= antes, "mais Exposição nunca deixa o horror mais leve");
                antes = n;
            }
        }
    }

    // ---------- quando ----------

    @Test
    void intervaloEncurtaComONivelENuncaFicaAbaixoDoMinimo() {
        assertEquals(0, RegrasHorror.intervalo(0, sorteio(1)));
        for (int nivel = 1; nivel <= RegrasHorror.NIVEL_MAX; nivel++) {
            RegrasHorror.Sorteio s = sorteio(nivel);
            for (int i = 0; i < 200; i++) {
                assertTrue(RegrasHorror.intervalo(nivel, s) >= RegrasHorror.INTERVALO_MINIMO);
            }
        }
        // O mais longo do nível 4 é menor que o mais curto do nível 1.
        assertTrue(RegrasHorror.intervalo(4, limite -> limite - 1) < RegrasHorror.intervalo(1, limite -> 0));
    }

    @Test
    void agendaNaoTocaNoNivelZeroNemEmSequencia() {
        RegrasHorror.Agenda a = new RegrasHorror.Agenda();
        RegrasHorror.Sorteio s = sorteio(7);
        for (long t = 0; t < 20 * 60 * 10; t++) assertFalse(a.deveTocar(t, 0, s));

        int tocou = 0;
        long ultima = Long.MIN_VALUE;
        for (long t = 0; t < 20 * 60 * 30; t++) { // 30 min no pior nível
            if (a.deveTocar(t, 4, s)) {
                if (ultima != Long.MIN_VALUE) assertTrue(t - ultima >= RegrasHorror.INTERVALO_MINIMO);
                ultima = t;
                tocou++;
            }
        }
        // Nível 4: entre 20 e 50 s -> de 36 a 90 em 30 min. Nunca spam.
        assertTrue(tocou >= 30 && tocou <= 95, "tocou " + tocou);
    }

    @Test
    void agendaSubirDeNivelNaoQuebraOIntervaloMinimo() {
        RegrasHorror.Agenda a = new RegrasHorror.Agenda();
        RegrasHorror.Sorteio s = sorteio(3);
        long t = 0;
        while (!a.deveTocar(t, 1, s)) t++;
        long tocouEm = t;
        // Logo depois, piora para 4: não pode tocar antes de 20 s.
        for (long x = tocouEm + 1; x < tocouEm + RegrasHorror.INTERVALO_MINIMO; x++) {
            assertFalse(a.deveTocar(x, 4, s));
        }
    }

    @Test
    void agendaAdiarEmpurraCincoSegundos() {
        RegrasHorror.Agenda a = new RegrasHorror.Agenda();
        a.deveTocar(0, 2, sorteio(1));
        a.adiar(1000);
        assertEquals(1100, a.proxima());
        a.zerar();
        assertEquals(-1, a.proxima());
    }

    // ---------- qual ----------

    @Test
    void nivelBaixoSoTemSomELeve() {
        List<Alucinacao> l = RegrasHorror.possiveis(1, true, null, 0);
        assertEquals(EnumSet.of(Alucinacao.SUSSURRO, Alucinacao.CISCO), EnumSet.copyOf(l));
        assertTrue(RegrasHorror.possiveis(0, true, null, 0).isEmpty());
        assertFalse(RegrasHorror.possiveis(2, true, null, 0).contains(Alucinacao.VULTO));
        assertTrue(RegrasHorror.possiveis(3, true, null, 0).contains(Alucinacao.VULTO));
        assertTrue(RegrasHorror.possiveis(3, true, null, 0).contains(Alucinacao.MOB_FALSO));
    }

    @Test
    void semEfeitosFortesSobraSoSomEVinheta() {
        for (int nivel = 0; nivel <= RegrasHorror.NIVEL_MAX; nivel++) {
            for (Alucinacao a : RegrasHorror.possiveis(nivel, false, null, 0)) {
                assertFalse(a.visual, a + " é visual e apareceu com efeitos fortes desligado");
            }
        }
        assertFalse(RegrasHorror.possiveis(4, false, null, 0).isEmpty(), "ainda tem som no pior nível");
    }

    @Test
    void mesmaAlucinacaoNaoRepeteAntesDoCooldown() {
        RegrasHorror.Recentes r = new RegrasHorror.Recentes();
        r.marcar(Alucinacao.SUSSURRO, 1000);
        assertFalse(r.livre(Alucinacao.SUSSURRO, 1000 + RegrasHorror.COOLDOWN_MESMA - 1));
        assertTrue(r.livre(Alucinacao.SUSSURRO, 1000 + RegrasHorror.COOLDOWN_MESMA));
        assertTrue(r.livre(Alucinacao.CISCO, 1001));
        // Relógio voltou (outro mundo): não trava para sempre.
        assertTrue(r.livre(Alucinacao.SUSSURRO, 10));

        r.marcar(Alucinacao.CISCO, 1000);
        assertEquals(Optional.empty(), RegrasHorror.escolher(1, true, r, 1001, sorteio(1)));
    }

    @Test
    void escolhaSaiDoQuePodeEPesaONivelProprio() {
        RegrasHorror.Sorteio s = sorteio(42);
        int[] vezes = new int[Alucinacao.values().length];
        for (int i = 0; i < 6000; i++) {
            Alucinacao a = RegrasHorror.escolher(3, true, null, 0, s).orElseThrow();
            assertTrue(a.nivelMinimo <= 3);
            vezes[a.ordinal()]++;
        }
        assertTrue(vezes[Alucinacao.VULTO.ordinal()] > vezes[Alucinacao.SUSSURRO.ordinal()],
                "as do próprio nível saem mais que as leves");
        for (Alucinacao a : RegrasHorror.possiveis(3, true, null, 0)) {
            assertTrue(vezes[a.ordinal()] > 0, a + " nunca saiu");
        }
    }

    // ---------- sumir ----------

    @Test
    void vultoSomeAoOlharChegarPertoOuComOTempo() {
        assertFalse(RegrasHorror.vultoSome(Math.cos(Math.toRadians(40)), 64, 10)); // no canto, longe
        assertTrue(RegrasHorror.vultoSome(Math.cos(Math.toRadians(10)), 64, 10)); // olhou
        assertTrue(RegrasHorror.vultoSome(0, 9, 10)); // chegou a 3 blocos
        assertTrue(RegrasHorror.vultoSome(0, 64, RegrasHorror.VULTO_VIDA));
    }

    @Test
    void vultoNasceNoCantoDaVisao() {
        RegrasHorror.Sorteio s = sorteio(5);
        Set<Boolean> lados = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            int g = RegrasHorror.vultoAngulo(s);
            assertTrue(Math.abs(g) >= 35 && Math.abs(g) <= 50, "ângulo " + g);
            // Nasce fora do cone de "olhou", senão sumiria na hora.
            assertTrue(Math.cos(Math.toRadians(g)) < RegrasHorror.VULTO_COS_OLHOU);
            lados.add(g > 0);
            int d = RegrasHorror.vultoDistancia(s);
            assertTrue(d >= 7 && d <= 11);
            assertTrue((double) d * d > RegrasHorror.VULTO_PERTO_SQ);
        }
        assertEquals(2, lados.size(), "aparece dos dois lados");
    }

    @Test
    void mobFalsoSomeAoChegarPerto() {
        assertFalse(RegrasHorror.mobSome(15 * 15, 10));
        assertTrue(RegrasHorror.mobSome(5 * 5, 10));
        assertTrue(RegrasHorror.mobSome(15 * 15, RegrasHorror.MOB_VIDA));
        RegrasHorror.Sorteio s = sorteio(9);
        for (int i = 0; i < 100; i++) {
            int d = RegrasHorror.mobDistancia(s);
            assertTrue((double) d * d > RegrasHorror.MOB_PERTO_SQ, "nasce longe o bastante para não sumir na hora");
        }
    }

    @Test
    void portaFicaAbertaPoucoTempo() {
        RegrasHorror.Sorteio s = sorteio(11);
        for (int i = 0; i < 100; i++) {
            int d = RegrasHorror.portaDuracao(s);
            assertTrue(d >= 30 && d <= 60);
        }
    }

    // ---------- tela e som ----------

    @Test
    void efeitosContinuosCrescemComONivel() {
        float[] antes = {-1, -1, -1, -1};
        for (int n = 0; n <= RegrasHorror.NIVEL_MAX; n++) {
            float[] agora = {RegrasHorror.dessaturar(n), RegrasHorror.balanco(n),
                    RegrasHorror.tremorMao(n), RegrasHorror.distorcao(n)};
            for (int i = 0; i < agora.length; i++) {
                assertTrue(agora[i] >= antes[i]);
                assertTrue(agora[i] >= 0 && agora[i] < 2);
            }
            antes = agora;
        }
        assertEquals(0f, RegrasHorror.dessaturar(0));
        assertEquals(0f, RegrasHorror.balanco(1));
        assertEquals(RegrasHorror.dessaturar(4), RegrasHorror.dessaturar(99)); // fora da faixa é preso
    }

    @Test
    void pitchDosSonsNaFaixaTocavel() {
        RegrasHorror.Sorteio s = sorteio(13);
        for (int i = 0; i < 200; i++) {
            float p = RegrasHorror.pitch(s);
            assertTrue(p >= 0.5f && p <= 0.7f);
        }
    }

    // ---------- lentidão (servidor) ----------

    @Test
    void lentidaoSoNaLucidezBaixaELeve() {
        assertEquals(0, RegrasLucidez.lentidao(FaixaLucidez.FIRME));
        assertEquals(0, RegrasLucidez.lentidao(FaixaLucidez.INQUIETO));
        assertTrue(RegrasLucidez.lentidao(FaixaLucidez.ABALADO) < 0);
        assertTrue(RegrasLucidez.lentidao(FaixaLucidez.QUEBRANDO) < RegrasLucidez.lentidao(FaixaLucidez.ABALADO));
        assertTrue(RegrasLucidez.lentidao(FaixaLucidez.SURTO) >= -0.1, "leve: nunca mais que 10%");
    }

    // ---------- frases ----------

    @Test
    void todaFaixaTemFraseESimboloNasDuasLinguas() throws Exception {
        String pt = Files.readString(Path.of("src/main/resources/assets/odteste/lang/pt_br.json"));
        String en = Files.readString(Path.of("src/main/resources/assets/odteste/lang/en_us.json"));
        Set<String> simbolos = new HashSet<>();
        for (Degrau d : Degrau.values()) {
            assertTrue(pt.contains("\"" + FrasesFaixa.chave(d) + "\""), FrasesFaixa.chave(d));
            assertTrue(en.contains("\"" + FrasesFaixa.chave(d) + "\""), FrasesFaixa.chave(d));
            assertFalse(FrasesFaixa.simbolo(d).isBlank());
            simbolos.add(FrasesFaixa.simbolo(d));
        }
        assertEquals(Degrau.values().length, simbolos.size(), "um símbolo diferente por degrau");
        simbolos.clear();
        for (FaixaLucidez f : FaixaLucidez.values()) {
            assertTrue(pt.contains("\"" + FrasesFaixa.chave(f) + "\""), FrasesFaixa.chave(f));
            assertTrue(en.contains("\"" + FrasesFaixa.chave(f) + "\""), FrasesFaixa.chave(f));
            simbolos.add(FrasesFaixa.simbolo(f));
        }
        assertEquals(FaixaLucidez.values().length, simbolos.size(), "um símbolo diferente por faixa");
    }
}
