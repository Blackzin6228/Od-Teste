package com.nikolas.odteste.horror;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.horror.RegrasCamadaHorror.Momento;
import org.junit.jupiter.api.Test;

/** Quando a camada de horror liga e como a luz escurece (decisão de 14/11/2026). */
class RegrasCamadaHorrorTest {
    private static final long MEIO_DIA = 6_000;
    private static final long MEIA_NOITE = 18_000;

    @Test
    void ligaDeNoitePertoDoCeuENaArea() {
        assertEquals(Momento.NOITE, RegrasCamadaHorror.momento(true, MEIA_NOITE, 15, false));
        assertEquals(Momento.NOITE, RegrasCamadaHorror.momento(true, MEIA_NOITE, 0, true)); // adega da vila conta
        assertEquals(Momento.AREA_DIA, RegrasCamadaHorror.momento(true, MEIO_DIA, 15, true));
        assertEquals(Momento.MUNDO, RegrasCamadaHorror.momento(true, MEIO_DIA, 15, false));
    }

    @Test
    void minaFundaFicaSoComOFundoEOutraDimensaoFora() {
        assertEquals(Momento.MUNDO, RegrasCamadaHorror.momento(true, MEIA_NOITE, 0, false));
        assertEquals(Momento.NADA, RegrasCamadaHorror.momento(false, MEIA_NOITE, 15, false));
        assertEquals(Momento.NADA, RegrasCamadaHorror.momento(false, MEIO_DIA, 15, false));
    }

    @Test
    void fundoValeNoOverworldInteiroDeDiaTambem() {
        for (long hora : new long[] {0, MEIO_DIA, 11_000, MEIA_NOITE, 23_500}) {
            for (int ceu : new int[] {0, 15}) {
                for (boolean area : new boolean[] {false, true}) {
                    Momento m = RegrasCamadaHorror.momento(true, hora, ceu, area);
                    assertEquals(1f, RegrasCamadaHorror.alvoDia(m), "hora=" + hora + " ceu=" + ceu + " area=" + area);
                    assertTrue(RegrasCamadaHorror.cortaMusica(m), "música cortada de dia também");
                    assertEquals(0f, RegrasCamadaHorror.alvoDia(RegrasCamadaHorror.momento(false, hora, ceu, area)));
                }
            }
        }
    }

    @Test
    void forcaSobeDeDiaParaAreaEParaNoite() {
        float dia = RegrasCamadaHorror.total(1f, RegrasCamadaHorror.alvo(RegrasCamadaHorror.momento(true, MEIO_DIA, 15, false)));
        float areaDia = RegrasCamadaHorror.total(1f, RegrasCamadaHorror.alvo(RegrasCamadaHorror.momento(true, MEIO_DIA, 15, true)));
        float noite = RegrasCamadaHorror.total(1f, RegrasCamadaHorror.alvo(RegrasCamadaHorror.momento(true, MEIA_NOITE, 15, false)));
        assertTrue(dia > 0f, "de dia não é zero");
        assertTrue(dia <= areaDia && areaDia < noite, dia + " " + areaDia + " " + noite);
        assertEquals(1f, noite);
        assertEquals(0f, RegrasCamadaHorror.total(0f, 0f));
    }

    @Test
    void sustoContinuaSoDeNoite() {
        assertFalse(RegrasCamadaHorror.podeSusto(RegrasCamadaHorror.momento(true, MEIO_DIA, 15, false)));
        assertFalse(RegrasCamadaHorror.podeSusto(RegrasCamadaHorror.momento(true, MEIA_NOITE, 0, false)));
        assertTrue(RegrasCamadaHorror.podeSusto(RegrasCamadaHorror.momento(true, MEIA_NOITE, 15, false)));
    }

    @Test
    void luzDoDiaCaiPoucoESoOndeChegaOCeu() {
        float c = 0.9f;
        float sol = RegrasCamadaHorror.canalDia(c, 1f, 0, 15);
        assertEquals(c * (1 - RegrasCamadaHorror.ESCURO_DIA), sol, 0.0001f);
        assertTrue(sol >= c * 0.65f, "moderado: dá para trabalhar");
        assertEquals(c, RegrasCamadaHorror.canalDia(c, 1f, 0, 0), 0.0001f); // mina sem céu: igual
        assertEquals(c, RegrasCamadaHorror.canalDia(c, 1f, 15, 15), 0.0001f); // perto da tocha: igual
        assertEquals(c, RegrasCamadaHorror.canalDia(c, 0f, 0, 15), 0.0001f); // fundo desligado: igual
    }

    @Test
    void nevoaDoDiaMaisPertoQueOJogoEMaisLongeQueANoite() {
        assertEquals(RegrasCamadaHorror.NEVOA_DIA, RegrasCamadaHorror.nevoaDia(256f, 1f), 0.001f);
        assertEquals(64f, RegrasCamadaHorror.nevoaDia(64f, 1f)); // nunca alarga
        assertTrue(RegrasCamadaHorror.NEVOA_DIA > RegrasCamadaHorror.NEVOA_NOITE);
    }

    @Test
    void ceuFechadoDeDiaESoltaDeNoite() {
        assertEquals(RegrasCamadaHorror.CEU_NEVOA, RegrasCamadaHorror.nevoaCeu(192f, 1f, 0f), 0.001f);
        assertEquals(192f, RegrasCamadaHorror.nevoaCeu(192f, 1f, 1f), 0.001f);
        assertEquals(192f, RegrasCamadaHorror.nevoaCeu(192f, 0f, 0f), 0.001f);
        assertTrue(RegrasCamadaHorror.pularCeu(1f, 0f));
        assertFalse(RegrasCamadaHorror.pularCeu(1f, 1f)); // noite: lua e estrelas
        assertFalse(RegrasCamadaHorror.pularCeu(0.5f, 0f)); // entrando no overworld: ainda não
    }

    @Test
    void corDoCeuViraCinza() {
        float r = 0.47f, g = 0.65f, b = 1f; // azul do céu limpo
        float l = RegrasCamadaHorror.luma(r, g, b);
        float cr = RegrasCamadaHorror.cinza(r, l, 1f);
        float cb = RegrasCamadaHorror.cinza(b, l, 1f);
        assertTrue(Math.abs(cb - cr) < Math.abs(b - r) * 0.3f, "quase sem cor");
        assertTrue(cb < b, "mais escuro que o céu limpo");
        assertEquals(r, RegrasCamadaHorror.cinza(r, l, 0f), 0.0001f);
    }

    @Test
    void intensidadeDiminuiMasNaoDesliga() {
        assertEquals(1f, RegrasCamadaHorror.intensidade(100));
        assertEquals(0.5f, RegrasCamadaHorror.intensidade(50));
        assertEquals(0.5f, RegrasCamadaHorror.intensidade(0)); // arquivo editado à mão: não zera
        assertEquals(1f, RegrasCamadaHorror.intensidade(500));
    }

    @Test
    void sonsDeDiaMaisRarosQueDeNoite() {
        Momento dia = RegrasCamadaHorror.momento(true, MEIO_DIA, 15, false);
        Momento noite = RegrasCamadaHorror.momento(true, MEIA_NOITE, 15, false);
        assertTrue(RegrasCamadaHorror.intervaloSom(dia, false, 0) > RegrasCamadaHorror.intervaloSom(noite, false, 1));
        assertEquals(-1, RegrasCamadaHorror.intervaloSom(noite, true, 0.5)); // áreas têm sons próprios
        assertEquals(-1, RegrasCamadaHorror.intervaloSom(Momento.NADA, false, 0.5));
    }

    @Test
    void veuDeDiaFracoESoNoCeu() {
        assertTrue(RegrasCamadaHorror.veuDia(1f, 15, 0) <= RegrasCamadaHorror.VEU_DIA_MAX);
        assertEquals(0f, RegrasCamadaHorror.veuDia(1f, 0, 0));
        assertEquals(0f, RegrasCamadaHorror.veuDia(1f, 15, 15));
    }

    @Test
    void musicaCortadaESustoSoDeNoite() {
        assertTrue(RegrasCamadaHorror.cortaMusica(Momento.NOITE));
        assertTrue(RegrasCamadaHorror.cortaMusica(Momento.AREA_DIA));
        assertFalse(RegrasCamadaHorror.cortaMusica(Momento.NADA));
        assertTrue(RegrasCamadaHorror.cortaMusica(Momento.MUNDO));
        assertTrue(RegrasCamadaHorror.podeSusto(Momento.NOITE));
        assertFalse(RegrasCamadaHorror.podeSusto(Momento.AREA_DIA));
        assertTrue(RegrasCamadaHorror.alvo(Momento.AREA_DIA) < RegrasCamadaHorror.alvo(Momento.NOITE));
        assertEquals(0f, RegrasCamadaHorror.alvo(Momento.NADA));
        assertEquals(0f, RegrasCamadaHorror.alvo(Momento.MUNDO)); // o dia é a camada de fundo (alvoDia)
    }

    @Test
    void gammaDesfeitoVoltaAoMesmoValor() {
        for (float g : new float[] {0f, 0.5f, 1f}) {
            for (float c : new float[] {0.02f, 0.1f, 0.3f, 0.7f}) {
                float cru = RegrasCamadaHorror.desfazerGamma(c, g);
                assertEquals(c, RegrasCamadaHorror.aplicarGamma(cru, g), 0.002f, "c=" + c + " g=" + g);
            }
        }
    }

    @Test
    void brilhoAltoNaoClareiaOEscuro() {
        float c = 0.15f; // luz da lua, sem tocha
        float comBrilhoMaximo = RegrasCamadaHorror.aplicarGamma(RegrasCamadaHorror.canal(c, 1f, 0, 0f, 1f), 1f);
        float semBrilho = RegrasCamadaHorror.aplicarGamma(RegrasCamadaHorror.canal(c, 1f, 0, 0f, 0f), 0f);
        assertEquals(semBrilho, comBrilhoMaximo, 0.003f);
        assertTrue(comBrilhoMaximo < c, "fica mais escuro que o normal sem brilho");
    }

    @Test
    void tochaContinuaClareando() {
        float c = 0.9f;
        assertEquals(c, RegrasCamadaHorror.canal(c, 1f, 15, 0f, 1f), 0.0001f); // luz de bloco cheia: nada muda
        assertTrue(RegrasCamadaHorror.peso(1f, 14, 0f) < 0.01f);
        assertTrue(RegrasCamadaHorror.peso(1f, 0, 0f) > 0.99f);
    }

    @Test
    void luzQueFalhaEscureceAteATocha() {
        assertEquals(0.9f, RegrasCamadaHorror.peso(0f, 15, 0.9f), 0.0001f);
        float c = 0.9f;
        assertTrue(RegrasCamadaHorror.aplicarGamma(RegrasCamadaHorror.canal(c, 0f, 15, 0.9f, 0.5f), 0.5f) < 0.5f);
    }

    @Test
    void desligadaNaoMudaNada() {
        assertEquals(0.4f, RegrasCamadaHorror.canal(0.4f, 0f, 0, 0f, 1f), 0.0001f);
        assertEquals(0f, RegrasCamadaHorror.veu(0f, 0));
    }

    @Test
    void nevoaSoChegaMaisPerto() {
        assertEquals(256f, RegrasCamadaHorror.nevoa(256f, 0f));
        assertEquals(RegrasCamadaHorror.NEVOA_NOITE, RegrasCamadaHorror.nevoa(256f, 1f), 0.001f);
        assertEquals(40f, RegrasCamadaHorror.nevoa(40f, 1f)); // já estava perto (vila, cegueira): fica
    }

    @Test
    void aproximarParaNoAlvo() {
        assertEquals(1f, RegrasCamadaHorror.aproximar(0.995f, 1f, 0.01f));
        assertEquals(0f, RegrasCamadaHorror.aproximar(0.005f, 0f, 0.01f));
    }
}
