package com.nikolas.odteste.estruturas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FatiasTest {

    /** Toda fatia cabe no limite, nenhuma se sobrepõe e juntas cobrem a caixa inteira. */
    private static void confereCobertura(Espaco.Caixa c, int limite) {
        int n = Fatias.total(c, limite);
        Set<Long> vistos = new HashSet<>();
        long soma = 0;
        int yAnterior = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            Espaco.Caixa f = Fatias.fatia(c, limite, i);
            assertTrue(f.volume() <= Math.max(1, limite),"fatia " + i + " passa do limite: " + f.volume());
            assertTrue(f.y0() >= yAnterior, "fatias fora de ordem (de baixo para cima)");
            yAnterior = f.y0();
            for (int y = f.y0(); y <= f.y1(); y++)
                for (int x = f.x0(); x <= f.x1(); x++)
                    for (int z = f.z0(); z <= f.z1(); z++) {
                        assertTrue(c.contem(x, y, z), "fatia sai da caixa");
                        long k = ((long) (x - c.x0()) * 4096 + (y - c.y0())) * 4096 + (z - c.z0());
                        assertTrue(vistos.add(k), "bloco em duas fatias");
                    }
            soma += f.volume();
        }
        assertEquals(c.volume(), soma);
        assertEquals(c.volume(), vistos.size());
    }

    @Test
    void vilaDoPrototipoCobreTudo() {
        // Vila Calada (protótipo): 72 x 30 x 64, num ponto qualquer do mundo (inclusive negativo).
        Espaco.Caixa vila = Espaco.Caixa.deTamanho(-1200, 62, 845, 72, 30, 64);
        confereCobertura(vila, 8192);
        // Camada de 72 x 64 = 4608 cabe no limite: uma camada por fatia, 30 fatias.
        assertEquals(30, Fatias.total(vila, 8192));
    }

    @Test
    void camadaMaiorQueOLimiteViraFaixas() {
        // 100 x 100 (ficha final): camada de 10.000 não cabe em 4096, vira faixas de 40 em X.
        Espaco.Caixa c = Espaco.Caixa.deTamanho(0, 0, 0, 100, 5, 100);
        confereCobertura(c, 4096);
        assertEquals(5 * 3, Fatias.total(c, 4096));
        assertEquals(40, Fatias.fatia(c, 4096, 0).largura());
        assertEquals(20, Fatias.fatia(c, 4096, 2).largura());
    }

    @Test
    void variasCamadasPorFatiaQuandoCabe() {
        Espaco.Caixa c = Espaco.Caixa.deTamanho(10, 10, 10, 10, 9, 10);
        confereCobertura(c, 400);
        assertEquals(3, Fatias.total(c, 400)); // 4 + 4 + 1 camadas
        assertEquals(1, Fatias.fatia(c, 400, 2).altura());
    }

    @Test
    void limiteMuitoPequenoAindaFunciona() {
        Espaco.Caixa c = Espaco.Caixa.deTamanho(0, 0, 0, 3, 2, 7);
        confereCobertura(c, 5);
        confereCobertura(c, 1);
        confereCobertura(c, 0); // limite inválido vira 1
        assertEquals(42, Fatias.total(c, 1));
    }

    @Test
    void caixaPequenaEhUmaFatiaSo() {
        Espaco.Caixa c = Espaco.Caixa.deTamanho(0, 0, 0, 8, 8, 8);
        assertEquals(1, Fatias.total(c, 8192));
        assertEquals(c, Fatias.fatia(c, 8192, 0));
    }

    @Test
    void mesmoLimiteDaSempreAsMesmasFatias() {
        Espaco.Caixa c = Espaco.Caixa.deTamanho(5, 60, -5, 100, 30, 100);
        for (int i = 0; i < Fatias.total(c, 3000); i += 37) {
            assertEquals(Fatias.fatia(c, 3000, i), Fatias.fatia(c, 3000, i));
        }
    }

    /** O template é fatiado por camada: cada fatia tem que estar inteira na camada que o código diz. */
    @Test
    void cadaFatiaFicaNaSuaCamada() {
        for (int limite : new int[] {1, 5, 400, 4096, 8192, 100_000}) {
            for (Espaco.Caixa c : new Espaco.Caixa[] {Espaco.Caixa.deTamanho(-30, 62, 7, 72, 30, 64),
                    Espaco.Caixa.deTamanho(0, -10, 0, 100, 9, 100), Espaco.Caixa.deTamanho(3, 3, 3, 3, 2, 7)}) {
                int camadas = Fatias.camadas(c, limite);
                int anterior = 0;
                for (int i = 0; i < Fatias.total(c, limite); i++) {
                    Espaco.Caixa f = Fatias.fatia(c, limite, i);
                    int k = Fatias.camadaDaFatia(c, limite, i);
                    assertTrue(k >= anterior && k < camadas, "camada fora de ordem");
                    anterior = k;
                    assertEquals(k, Fatias.camadaDoY(c, limite, f.y0()));
                    assertEquals(k, Fatias.camadaDoY(c, limite, f.y1()));
                }
                assertEquals(camadas - 1, anterior);
                assertEquals(0, Fatias.camadaDoY(c, limite, c.y0() - 5));
                assertEquals(camadas - 1, Fatias.camadaDoY(c, limite, c.y1() + 5));
            }
        }
    }

    @Test
    void indiceForaDaFaixaFalha() {
        Espaco.Caixa c = Espaco.Caixa.deTamanho(0, 0, 0, 4, 4, 4);
        assertThrows(IndexOutOfBoundsException.class, () -> Fatias.fatia(c, 16, Fatias.total(c, 16)));
        assertThrows(IndexOutOfBoundsException.class, () -> Fatias.fatia(c, 16, -1));
    }
}
