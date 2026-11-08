package com.nikolas.odteste.marcas;

import java.util.Random;

/**
 * Desenho **provisório** da Marca de Sangue no corpo: veias vermelhas numa imagem 64x64 no mesmo molde da skin.
 * Feito por nós, em código (sem arquivo de imagem): a mesa Modelador troca pela textura definitiva com prévia.
 * Java puro, testado em PadraoVeiasTest.
 *
 * Cresce com o estágio, sempre por cima do anterior (o estágio 3 tem tudo do 2, e mais):
 * 1 braço direito · 2 + peito e costas · 3 + braço esquerdo e pescoço · 4 + pernas.
 * Só usa a camada de baixo da skin (nada na camada de cima, que pode estar desligada).
 */
public final class PadraoVeias {
    public static final int LADO = 64;

    /** Veia escura e ramo um pouco mais vivo (ARGB, quase opacos). */
    static final int VEIA = 0xD8640000;
    static final int RAMO = 0xB8A01010;

    /** Retângulo [x0, y0, x1, y1) no molde da skin e a partir de que estágio ele ganha veias. */
    record Regiao(int x0, int y0, int x1, int y1, int desde) {}

    /** Lados (frente, costas, laterais) de cada parte, camada de baixo. */
    static final Regiao[] REGIOES = {
            new Regiao(40, 20, 56, 32, 1), // braço direito
            new Regiao(16, 20, 40, 32, 2), // tronco
            new Regiao(32, 52, 48, 64, 3), // braço esquerdo
            new Regiao(0, 12, 32, 16, 3),  // cabeça: só a parte de baixo (pescoço, queixo)
            new Regiao(0, 20, 16, 32, 4),  // perna direita
            new Regiao(16, 52, 32, 64, 4), // perna esquerda
    };

    private PadraoVeias() {}

    /** Imagem do estágio (0 a 4; fora disso é preso), em ARGB, linha por linha. 0 = transparente. */
    public static int[] gerar(int estagio) {
        int[] img = new int[LADO * LADO];
        int ate = Math.max(0, Math.min(4, estagio));
        for (int s = 1; s <= ate; s++) {
            for (int r = 0; r < REGIOES.length; r++) {
                Regiao reg = REGIOES[r];
                if (reg.desde() > s) continue;
                // Semente fixa: a imagem é sempre a mesma (todo mundo vê igual).
                Random rnd = new Random(s * 1000L + r);
                int veias = reg.desde() == s ? 3 : 2; // região nova ganha mais veias; as antigas engrossam
                for (int v = 0; v < veias; v++) veia(img, reg, rnd);
            }
        }
        return img;
    }

    /** Uma veia que desce torta pela região, com um ramo de vez em quando. */
    private static void veia(int[] img, Regiao reg, Random rnd) {
        int x = reg.x0() + rnd.nextInt(reg.x1() - reg.x0());
        int y = reg.y0() + rnd.nextInt(Math.max(1, (reg.y1() - reg.y0()) / 2));
        int comprimento = 4 + rnd.nextInt(Math.max(1, reg.y1() - reg.y0()));
        for (int i = 0; i < comprimento && y < reg.y1(); i++) {
            pintar(img, x, y, VEIA);
            if (rnd.nextInt(4) == 0) {
                int lado = rnd.nextBoolean() ? 1 : -1;
                if (x + lado >= reg.x0() && x + lado < reg.x1()) pintar(img, x + lado, y, RAMO);
            }
            y++;
            int passo = rnd.nextInt(3) - 1;
            if (x + passo >= reg.x0() && x + passo < reg.x1()) x += passo;
        }
    }

    private static void pintar(int[] img, int x, int y, int cor) {
        // Uma veia nunca é apagada por um ramo: só pinta ramo onde está vazio.
        int i = y * LADO + x;
        if (cor == RAMO && img[i] != 0) return;
        img[i] = cor;
    }

    /** O pixel (x, y) pode ter veia? (está numa região liberada até este estágio) */
    static boolean permitido(int x, int y, int estagio) {
        for (Regiao r : REGIOES) {
            if (r.desde() <= estagio && x >= r.x0() && x < r.x1() && y >= r.y0() && y < r.y1()) return true;
        }
        return false;
    }
}
