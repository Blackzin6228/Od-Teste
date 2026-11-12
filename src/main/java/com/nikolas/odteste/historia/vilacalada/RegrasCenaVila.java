package com.nikolas.odteste.historia.vilacalada;

/**
 * Números da cena de entrada da Vila Calada (Java puro, testado em RegrasCenaVilaTest).
 *
 * "Dentro do muro" usa a mesma conta do clima da vila (PR #53, {@code RegrasClimaVila.dentro}): um círculo de 48
 * blocos em volta do centro ancorado. Quando o clima entrar no main, os dois passam a usar uma conta só.
 */
public final class RegrasCenaVila {
    public static final double RAIO_MURO = 48;
    public static final double ACIMA = 48;
    public static final double ABAIXO = 16;
    /**
     * Do centro da vila até o portão (blocos). No modelo da vila o muro vai de z 3 a 88 e o centro fica em z 50:
     * o portão sul fica 38 blocos ao sul do centro, antes de girar a estrutura.
     */
    public static final double PORTAO_DO_CENTRO = 38;
    /** Teto de jogadores olhados por segundo (proteção contra lag). */
    public static final int MAX_JOGADORES = 128;

    private RegrasCenaVila() {}

    /** Está dentro do muro? (dx, dy, dz) = jogador menos o ponto da vila. */
    public static boolean dentro(double dx, double dy, double dz) {
        return dx * dx + dz * dz <= RAIO_MURO * RAIO_MURO && dy <= ACIMA && dy >= -ABAIXO;
    }

    /**
     * Direção (unitária, {x, z}) do centro para o portão. Com a rotação da estrutura conhecida, é o "sul" do modelo
     * girado ({@code sulX, sulZ}); sem ela (vila ancorada à mão), é a direção do centro para o jogador, que é por onde
     * ele chegou. Tudo nulo: sul.
     */
    public static double[] direcaoPortao(Integer sulX, Integer sulZ, double jogadorDx, double jogadorDz) {
        if (sulX != null && sulZ != null && (sulX != 0 || sulZ != 0)) {
            double n = Math.sqrt(sulX * sulX + sulZ * sulZ);
            return new double[] {sulX / n, sulZ / n};
        }
        double n = Math.sqrt(jogadorDx * jogadorDx + jogadorDz * jogadorDz);
        if (n < 1e-6) return new double[] {0, 1};
        return new double[] {jogadorDx / n, jogadorDz / n};
    }
}
