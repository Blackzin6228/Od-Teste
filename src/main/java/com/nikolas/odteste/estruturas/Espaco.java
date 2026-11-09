package com.nikolas.odteste.estruturas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Checagem de espaço antes de pôr qualquer bloco da história no mundo (docs/sistemas.md, "Colocar estruturas").
 * Java puro, testado em EspacoTest: o mundo entra como uma função que diz o tipo de cada bloco.
 *
 * Regra: só ocupa {@link Tipo#AR} e {@link Tipo#SUBSTITUIVEL} (grama alta, neve fina...). Qualquer outro bloco
 * pode ser construção de jogador, então barra. Na dúvida, não coloca.
 */
public final class Espaco {
    /** O que tem num bloco do mundo, do ponto de vista da colocação. */
    public enum Tipo {
        AR(true, false),
        SUBSTITUIVEL(true, false),
        /** Bloco sólido ou qualquer bloco que não está na lista de substituíveis (pode ser de jogador). */
        OCUPADO(false, true),
        /** Água, lava ou bloco alagado: não ocupa e não serve de chão. */
        LIQUIDO(false, false),
        /** Baú, fornalha, máquina, placa...: nunca mexe. */
        COM_DADOS(false, true),
        /** Chunk com dono (claim de FTB Chunks ou parecido). */
        PROTEGIDO(false, false),
        /** Chunk não carregado: não dá para conferir, então recusa. */
        NAO_CARREGADO(false, false),
        /** Fora da altura do mundo. */
        FORA_DO_MUNDO(false, false);

        /** Pode ser ocupado por um bloco nosso. */
        public final boolean livre;
        /** Serve de chão (tem bloco firme). */
        public final boolean chao;

        Tipo(boolean livre, boolean chao) {
            this.livre = livre;
            this.chao = chao;
        }
    }

    /** O mundo visto pela checagem. */
    @FunctionalInterface
    public interface Mundo {
        Tipo tipo(int x, int y, int z);
    }

    /** Altura do chão (primeiro bloco livre acima do terreno) numa coluna. */
    @FunctionalInterface
    public interface Chao {
        int y(int x, int z);
    }

    /** Uma caixa de blocos, de {@code min} a {@code max} (inclusive). */
    public record Caixa(int x0, int y0, int z0, int x1, int y1, int z1) {
        public Caixa {
            if (x1 < x0 || y1 < y0 || z1 < z0) throw new IllegalArgumentException("caixa invertida");
        }

        public static Caixa deTamanho(int x, int y, int z, int largura, int altura, int fundo) {
            return new Caixa(x, y, z, x + largura - 1, y + altura - 1, z + fundo - 1);
        }

        public long volume() {
            return (long) (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1);
        }

        public Caixa mover(int dx, int dy, int dz) {
            return new Caixa(x0 + dx, y0 + dy, z0 + dz, x1 + dx, y1 + dy, z1 + dz);
        }

        public boolean contem(int x, int y, int z) {
            return x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1;
        }

        public int largura() {
            return x1 - x0 + 1;
        }

        public int altura() {
            return y1 - y0 + 1;
        }

        public int fundo() {
            return z1 - z0 + 1;
        }
    }

    /** Resultado: cabe, ou o primeiro bloqueio (posição e motivo). */
    public record Resultado(boolean cabe, int x, int y, int z, Tipo motivo) {
        public static final Resultado OK = new Resultado(true, 0, 0, 0, Tipo.AR);
    }

    /** Fração mínima do piso que precisa ter chão firme embaixo (estrutura não fica voando). */
    public static final double CHAO_MINIMO = 0.7;

    private Espaco() {}

    /** Confere bloco por bloco. Para no primeiro bloqueio. */
    public static Resultado checar(Caixa c, Mundo m) {
        for (int y = c.y0(); y <= c.y1(); y++) {
            for (int x = c.x0(); x <= c.x1(); x++) {
                for (int z = c.z0(); z <= c.z1(); z++) {
                    Tipo t = m.tipo(x, y, z);
                    if (!t.livre) return new Resultado(false, x, y, z, t);
                }
            }
        }
        return Resultado.OK;
    }

    /**
     * Cabe e tem chão firme embaixo de pelo menos {@link #CHAO_MINIMO} do piso. Confere o chão primeiro (é só
     * uma camada, e para assim que faltar chão demais): em cima de ravina ou água, nem lê a caixa.
     */
    public static Resultado checarComChao(Caixa c, Mundo m) {
        Resultado chao = checarChao(c, m);
        return chao.cabe() ? checar(c, m) : chao;
    }

    /**
     * Só o chão embaixo da caixa (uma camada; para assim que faltar chão demais). A estrutura grande confere só
     * isto no {@code prever}: o resto da caixa é conferido em partes, um pedaço por tick.
     */
    public static Resultado checarChao(Caixa c, Mundo m) {
        int area = c.largura() * c.fundo();
        int maxFalhas = area - (int) Math.ceil(area * CHAO_MINIMO);
        int falhas = 0;
        int y = c.y0() - 1;
        for (int x = c.x0(); x <= c.x1(); x++) {
            for (int z = c.z0(); z <= c.z1(); z++) {
                Tipo t = m.tipo(x, y, z);
                if (t == Tipo.NAO_CARREGADO || t == Tipo.PROTEGIDO) return new Resultado(false, x, y, z, t);
                if (!t.chao && ++falhas > maxFalhas) return new Resultado(false, c.x0(), y, c.z0(), Tipo.AR);
            }
        }
        return Resultado.OK;
    }

    /**
     * Procura um ponto livre perto de {@code (x, z)}: tenta primeiro o próprio ponto, depois anéis cada vez
     * mais longe (passo {@code passo}) até {@code raio}. Em cada coluna, a caixa fica apoiada no chão.
     * No máximo {@code maxTentativas} pontos (para não travar o servidor).
     *
     * @return o canto mínimo da caixa que coube, ou vazio
     */
    public static Optional<int[]> procurar(int x, int z, int largura, int altura, int fundo,
                                           int raio, int passo, int maxTentativas, Mundo m, Chao chao) {
        return procurar(x, z, largura, altura, fundo, raio, passo, maxTentativas, Long.MAX_VALUE, m, chao);
    }

    /**
     * Igual, mas para depois de {@code orcamento} leituras de bloco no total (o servidor não trava num tick só).
     * Uma checagem já começada termina, então o máximo é o orçamento mais uma caixa com o chão.
     */
    public static Optional<int[]> procurar(int x, int z, int largura, int altura, int fundo,
                                           int raio, int passo, int maxTentativas, long orcamento, Mundo m, Chao chao) {
        long[] lidos = {0};
        Mundo contado = (bx, by, bz) -> {
            lidos[0]++;
            return m.tipo(bx, by, bz);
        };
        for (int[] p : candidatos(x, z, raio, Math.max(1, passo), maxTentativas)) {
            if (lidos[0] >= orcamento) break;
            int y = chao.y(p[0], p[1]);
            Caixa c = Caixa.deTamanho(p[0], y, p[1], largura, altura, fundo);
            if (checarComChao(c, contado).cabe()) return Optional.of(new int[] {p[0], y, p[1]});
        }
        return Optional.empty();
    }

    /** Pontos de uma grade em volta de (x, z), do mais perto para o mais longe. */
    static List<int[]> candidatos(int x, int z, int raio, int passo, int max) {
        List<int[]> lista = new ArrayList<>();
        int n = raio / passo;
        for (int i = -n; i <= n; i++) {
            for (int j = -n; j <= n; j++) {
                int dx = i * passo;
                int dz = j * passo;
                if (dx * dx + dz * dz <= raio * raio) lista.add(new int[] {x + dx, z + dz});
            }
        }
        lista.sort(Comparator.comparingInt(p -> (p[0] - x) * (p[0] - x) + (p[1] - z) * (p[1] - z)));
        return lista.size() > max ? lista.subList(0, max) : lista;
    }
}
