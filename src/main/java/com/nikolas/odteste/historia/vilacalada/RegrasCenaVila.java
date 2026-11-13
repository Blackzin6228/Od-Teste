package com.nikolas.odteste.historia.vilacalada;

/**
 * Números da cena de entrada da Vila Calada (Java puro, testado em RegrasCenaVilaTest).
 *
 * "Dentro do muro" é a conta do clima da vila ({@link RegrasClimaVila#dentro}): uma conta só para os dois.
 */
public final class RegrasCenaVila {
    /**
     * Do centro da vila até o portão (blocos). No modelo da vila o muro vai de z 3 a 88 e o centro fica em z 50:
     * o portão sul fica 38 blocos ao sul do centro, antes de girar a estrutura.
     */
    public static final double PORTAO_DO_CENTRO = 38;
    /** Teto de jogadores olhados por segundo (proteção contra lag). */
    public static final int MAX_JOGADORES = 128;

    private RegrasCenaVila() {}

    /**
     * A cena só começa com o jogador parado no chão: sem montaria/barco, sem élitro. Com o movimento travado, quem
     * está caindo, voando ou montado seguiria sem controle. Se não pode, o servidor tenta de novo no segundo seguinte.
     */
    public static boolean podeComecar(boolean noChao, boolean emVeiculo, boolean planando) {
        return noChao && !emVeiculo && !planando;
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
