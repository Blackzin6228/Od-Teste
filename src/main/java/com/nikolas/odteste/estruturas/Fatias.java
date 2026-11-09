package com.nikolas.odteste.estruturas;

/**
 * Divide a caixa de uma estrutura grande em fatias de no máximo {@code limite} blocos, para colocar uma por tick.
 * Java puro, testado em FatiasTest.
 *
 * Ordem: de baixo para cima (camada por camada), depois em X, depois em Z. Se uma camada inteira cabe no limite,
 * a fatia junta várias camadas; se não cabe, a camada vira faixas em X (e, no caso extremo, pedaços em Z).
 * A fatia {@code i} é calculada na hora: nada de lista guardada, e o mesmo {@code limite} dá sempre as mesmas fatias
 * (por isso a colocação salva o limite que usou, e mudar a config no meio não bagunça a retomada).
 */
public final class Fatias {
    private Fatias() {}

    /** Tamanho de cada fatia em y, x e z (a última de cada eixo pode ser menor). */
    static int[] passo(Espaco.Caixa c, int limite) {
        long l = Math.max(1, limite);
        long area = (long) c.largura() * c.fundo();
        if (area <= l) return new int[] {(int) Math.max(1, Math.min(c.altura(), l / area)), c.largura(), c.fundo()};
        if (c.fundo() <= l) return new int[] {1, (int) Math.max(1, l / c.fundo()), c.fundo()};
        return new int[] {1, 1, (int) l};
    }

    private static int partes(int tamanho, int passo) {
        return (tamanho + passo - 1) / passo;
    }

    /** Quantas fatias a caixa tem com esse limite. */
    public static int total(Espaco.Caixa c, int limite) {
        int[] p = passo(c, limite);
        long n = (long) partes(c.altura(), p[0]) * partes(c.largura(), p[1]) * partes(c.fundo(), p[2]);
        if (n > Integer.MAX_VALUE) throw new IllegalArgumentException("fatias demais");
        return (int) n;
    }

    /** Quantas camadas (grupos de altura) as fatias usam. Cada fatia fica inteira dentro de uma camada. */
    public static int camadas(Espaco.Caixa c, int limite) {
        return partes(c.altura(), passo(c, limite)[0]);
    }

    /** Em que camada fica a fatia {@code i}. As fatias andam camada por camada, de baixo para cima. */
    public static int camadaDaFatia(Espaco.Caixa c, int limite, int i) {
        int[] p = passo(c, limite);
        return i / (partes(c.largura(), p[1]) * partes(c.fundo(), p[2]));
    }

    /** Em que camada fica o bloco de altura {@code y} do mundo (fora da caixa vai para a primeira ou a última). */
    public static int camadaDoY(Espaco.Caixa c, int limite, int y) {
        int k = (y - c.y0()) / passo(c, limite)[0];
        return Math.max(0, Math.min(camadas(c, limite) - 1, k));
    }

    /** A fatia de número {@code i} (de 0 a {@code total - 1}). */
    public static Espaco.Caixa fatia(Espaco.Caixa c, int limite, int i) {
        int[] p = passo(c, limite);
        int nx = partes(c.largura(), p[1]);
        int nz = partes(c.fundo(), p[2]);
        int ny = partes(c.altura(), p[0]);
        if (i < 0 || i >= (long) ny * nx * nz) throw new IndexOutOfBoundsException("fatia " + i);
        int iy = i / (nx * nz);
        int resto = i % (nx * nz);
        int ix = resto / nz;
        int iz = resto % nz;
        int y0 = c.y0() + iy * p[0];
        int x0 = c.x0() + ix * p[1];
        int z0 = c.z0() + iz * p[2];
        return new Espaco.Caixa(x0, y0, z0,
                Math.min(c.x1(), x0 + p[1] - 1), Math.min(c.y1(), y0 + p[0] - 1), Math.min(c.z1(), z0 + p[2] - 1));
    }
}
