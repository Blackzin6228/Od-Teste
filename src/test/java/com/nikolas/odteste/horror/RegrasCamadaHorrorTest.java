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
        assertEquals(Momento.NADA, RegrasCamadaHorror.momento(true, MEIO_DIA, 15, false));
    }

    @Test
    void minaFundaEOutraDimensaoFicamComoSempre() {
        assertEquals(Momento.NADA, RegrasCamadaHorror.momento(true, MEIA_NOITE, 0, false));
        assertEquals(Momento.NADA, RegrasCamadaHorror.momento(false, MEIA_NOITE, 15, false));
    }

    @Test
    void musicaCortadaESustoSoDeNoite() {
        assertTrue(RegrasCamadaHorror.cortaMusica(Momento.NOITE));
        assertTrue(RegrasCamadaHorror.cortaMusica(Momento.AREA_DIA));
        assertFalse(RegrasCamadaHorror.cortaMusica(Momento.NADA));
        assertTrue(RegrasCamadaHorror.podeSusto(Momento.NOITE));
        assertFalse(RegrasCamadaHorror.podeSusto(Momento.AREA_DIA));
        assertTrue(RegrasCamadaHorror.alvo(Momento.AREA_DIA) < RegrasCamadaHorror.alvo(Momento.NOITE));
        assertEquals(0f, RegrasCamadaHorror.alvo(Momento.NADA));
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
