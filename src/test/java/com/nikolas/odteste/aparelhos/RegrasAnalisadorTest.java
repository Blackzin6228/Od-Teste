package com.nikolas.odteste.aparelhos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Contas do Analisador de Sinal (Java puro). */
class RegrasAnalisadorTest {

    @Test
    void checaUmaVezPorIntervalo() {
        long semente = 123_456_789L;
        int vezes = 0;
        for (long t = 0; t < RegrasAnalisador.INTERVALO * 10L; t++) {
            if (RegrasAnalisador.vezDeChecar(t, semente)) vezes++;
        }
        assertEquals(10, vezes, "uma checagem a cada intervalo, nunca a cada tick");
    }

    @Test
    void sementeNegativaNaoQuebra() {
        int vezes = 0;
        for (long t = 0; t < RegrasAnalisador.INTERVALO; t++) {
            if (RegrasAnalisador.vezDeChecar(t, -987_654_321L)) vezes++;
        }
        assertEquals(1, vezes);
    }

    @Test
    void aparelhosDiferentesChecamEmTicksDiferentes() {
        long tickA = -1, tickB = -1;
        for (long t = 0; t < RegrasAnalisador.INTERVALO; t++) {
            if (RegrasAnalisador.vezDeChecar(t, 0)) tickA = t;
            if (RegrasAnalisador.vezDeChecar(t, 7)) tickB = t;
        }
        assertTrue(tickA != tickB);
    }

    @Test
    void ligadoSoComEnergiaParaUmIntervalo() {
        assertFalse(RegrasAnalisador.ficaLigado(0));
        assertFalse(RegrasAnalisador.ficaLigado(RegrasAnalisador.GASTO_CHECAGEM - 1));
        assertTrue(RegrasAnalisador.ficaLigado(RegrasAnalisador.GASTO_CHECAGEM));
        assertEquals(RegrasAnalisador.CONSUMO_POR_TICK * RegrasAnalisador.INTERVALO, RegrasAnalisador.GASTO_CHECAGEM);
    }

    @Test
    void leituraPedeEnergiaECabeNaBateria() {
        assertFalse(RegrasAnalisador.podeLer(RegrasAnalisador.CUSTO_LEITURA - 1));
        assertTrue(RegrasAnalisador.podeLer(RegrasAnalisador.CUSTO_LEITURA));
        assertTrue(RegrasAnalisador.CUSTO_LEITURA <= RegrasAnalisador.CAPACIDADE, "bateria cheia tem que dar ao menos 1 leitura");
        assertTrue(RegrasAnalisador.ENTRADA_MAXIMA > RegrasAnalisador.CONSUMO_POR_TICK, "um cabo tem que vencer o gasto");
    }

    @Test
    void gastarNuncaFicaNegativo() {
        assertEquals(0, RegrasAnalisador.gastar(10, 50));
        assertEquals(40, RegrasAnalisador.gastar(50, 10));
        assertEquals(50, RegrasAnalisador.gastar(50, -10), "custo negativo não cria energia");
    }

    @Test
    void esperaEntreLeituras() {
        assertFalse(RegrasAnalisador.esperouLeitura(100, 100));
        assertFalse(RegrasAnalisador.esperouLeitura(100 + RegrasAnalisador.ESPERA_LEITURA - 1, 100));
        assertTrue(RegrasAnalisador.esperouLeitura(100 + RegrasAnalisador.ESPERA_LEITURA, 100));
        assertTrue(RegrasAnalisador.esperouLeitura(0, Long.MIN_VALUE / 2), "primeira leitura sempre passa");
    }

    @Test
    void raiosRazoaveis() {
        assertTrue(RegrasAnalisador.RAIO_SINAL > 0 && RegrasAnalisador.RAIO_SINAL <= RegrasAnalisador.RAIO_JOGADOR);
        assertTrue(RegrasAnalisador.RAIO_JOGADOR <= 32, "não pode ficar ligado com jogador longe");
    }
}
