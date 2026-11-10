package com.nikolas.odteste.historia.vilacalada;

import java.util.Locale;

/**
 * Números das três pistas que levam o grupo até a Vila Calada (lore: fichas-estruturas.md, "As três pistas").
 * Java puro (testado em RegrasVilaCaladaTest). Distâncias em blocos, tempos em ticks (20 por segundo).
 * Nada disso aparece na tela do jogador.
 */
public final class RegrasVilaCalada {
    /** Noite do jogo (o sino rachado só toca de noite). */
    public static final long NOITE_INICIO = 13_000;
    public static final long NOITE_FIM = 23_000;

    /** Intervalo entre dois toques para o mesmo jogador: longo, o silêncio entre eles é a parte que assusta. */
    public static final int TOQUE_MIN = 900;
    public static final int TOQUE_MAX = 1_800;
    /** Primeiro toque da noite: um pouco depois de escurecer. */
    public static final int PRIMEIRO_TOQUE_MIN = 100;
    public static final int PRIMEIRO_TOQUE_MAX = 600;

    /** Perto assim, o som sai da própria vila. Mais longe, de um ponto perto do jogador na direção dela. */
    public static final double RAIO_SOM_DIRETO = 48;
    /** Longe: o som sai a esta distância do jogador (dá para ouvir de que lado vem). */
    public static final double DISTANCIA_FONTE = 6;
    /** Mais longe que isso, ninguém ouve. */
    public static final double ALCANCE_SINO = 4_096;
    /** Distância em que o volume chega no mínimo. */
    public static final double LONGE = 1_500;
    public static final float VOLUME_PERTO = 1.0f;
    public static final float VOLUME_MINIMO = 0.3f;
    /** Volume do toque quando o jogador já está na vila (o som sai do ponto da vila e chega longe). */
    public static final float VOLUME_NA_VILA = 3.0f;
    /** Grave e rachado: duas batidas quase iguais tocadas juntas desafinam. */
    public static final float TOM_GRAVE = 0.5f;
    public static final float TOM_RACHA = 0.56f;

    /** Chegou à vila (horizontal). */
    public static final double RAIO_CHEGADA = 40;
    /** Depois de chegar, o sino só toca para quem está na vila ou em volta. */
    public static final double RAIO_VILA = 128;

    /** Aldeões virados: de quanto em quanto tempo acontece, quanto dura, quantos e de que distância. */
    public static final int OLHADA_MIN = 2_400;
    public static final int OLHADA_MAX = 4_800;
    public static final int OLHADA_DURA = 300;
    public static final double RAIO_ALDEOES = 24;
    public static final int MAX_ALDEOES_POR_JOGADOR = 12;
    /** Para onde o aldeão olha: um ponto a esta distância dele, na direção da vila. */
    public static final double OLHAR_LONGE = 32;

    /** Cama manchada: o rastro de gotas começa quando alguém do grupo está a esta distância da cama. */
    public static final double RAIO_CAMA = 16;
    public static final int GOTAS = 5;

    /** Teto de jogadores olhados por segundo (proteção contra lag; grupos são pequenos). */
    public static final int MAX_JOGADORES = 32;

    private RegrasVilaCalada() {}

    public static boolean noite(long horaDoDia) {
        long t = Math.floorMod(horaDoDia, 24_000L);
        return t >= NOITE_INICIO && t < NOITE_FIM;
    }

    /** Sorteio de 0 a 1 → ticks até o próximo toque. */
    public static int proximoToque(double sorteio) {
        return entre(TOQUE_MIN, TOQUE_MAX, sorteio);
    }

    public static int primeiroToque(double sorteio) {
        return entre(PRIMEIRO_TOQUE_MIN, PRIMEIRO_TOQUE_MAX, sorteio);
    }

    public static int proximaOlhada(double sorteio) {
        return entre(OLHADA_MIN, OLHADA_MAX, sorteio);
    }

    private static int entre(int min, int max, double sorteio) {
        double s = Math.max(0, Math.min(1, sorteio));
        return min + (int) Math.round(s * (max - min));
    }

    /** Volume do toque ouvido de longe: mais alto quando se anda para lá. 0 = não ouve. */
    public static float volumeLonge(double distancia) {
        if (distancia > ALCANCE_SINO) return 0f;
        double t = (distancia - RAIO_SOM_DIRETO) / (LONGE - RAIO_SOM_DIRETO);
        t = Math.max(0, Math.min(1, t));
        return (float) (VOLUME_PERTO - (VOLUME_PERTO - VOLUME_MINIMO) * t);
    }

    /**
     * O jogador ouve o sino agora?
     * @param chegou o grupo já chegou à vila (aí só quem está em volta dela ouve)
     */
    public static boolean ouve(double distancia, boolean chegou) {
        return distancia <= (chegou ? RAIO_VILA : ALCANCE_SINO);
    }

    /** Direção horizontal unitária {x, z} de (dx, dz). Em cima do ponto: {0, 0}. */
    public static double[] direcao(double dx, double dz) {
        double n = Math.sqrt(dx * dx + dz * dz);
        if (n < 1e-6) return new double[] {0, 0};
        return new double[] {dx / n, dz / n};
    }

    /**
     * O template colocado é a Vila Calada principal? Só o nome exato ({@code odteste:vila_calada}): variações
     * (decoração, testes) não ancoram sozinhas. Para elas, o admin ancora à mão.
     */
    public static boolean templateDaVila(String template) {
        return template != null && template.trim().toLowerCase(Locale.ROOT).equals(IdsVilaCalada.TEMPLATE);
    }
}
