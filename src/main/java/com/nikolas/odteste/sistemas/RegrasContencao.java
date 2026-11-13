package com.nikolas.odteste.sistemas;

/**
 * "Algo segura você aqui" (decisão de 13/11/2026): dentro das nossas dimensões e durante uma perseguição,
 * teleporte (/home, Waystones, pérola e afins) e voo falham. No mundo normal, tudo liberado.
 * Java puro, testado em RegrasContencaoTest.
 */
public final class RegrasContencao {
    /** Dimensão nossa = dimensão com o namespace do mod (nenhuma existe ainda; as que vierem entram sozinhas). */
    public static final String NAMESPACE = "odteste";
    /** O voo é conferido a cada 5 ticks, só para quem está preso (os outros saem na primeira conta). */
    public static final int TICKS_VOO = 5;
    /** A frase aparece no máximo uma vez a cada 3 s. */
    public static final int AVISO_TICKS = 60;
    /** Teleporte do admin (/tp, /spreadplayers) libera a troca de dimensão logo depois, por 1 s. */
    public static final int PASSE_TICKS = 20;
    /** Perseguição marcada por comando: de 1 s a 30 min. */
    public static final int PERSEGUICAO_MIN_S = 1;
    public static final int PERSEGUICAO_MAX_S = 1800;
    /** Modificador do voo criativo: tira tudo que outros mods somam. */
    public static final double VOO_MODIFICADOR = -1000;

    private RegrasContencao() {}

    public static boolean nossaDimensao(String namespace) {
        return NAMESPACE.equals(namespace);
    }

    /** Preso: na nossa dimensão ou em perseguição, e não está no criativo/espectador. */
    public static boolean preso(boolean nossaDimensao, boolean emPerseguicao, boolean criativoOuEspectador) {
        return !criativoOuEspectador && (nossaDimensao || emPerseguicao);
    }

    /**
     * Marca de tempo (perseguição, passe) ainda vale? {@code ate} null = sem marca. Relógio que voltou para antes
     * do começo da marca (mundo trocado) não conta.
     */
    public static boolean vale(long agora, Long ate, long duracaoMaxima) {
        if (ate == null) return false;
        return agora < ate && agora >= ate - duracaoMaxima;
    }

    /** Pode mostrar a frase de novo? */
    public static boolean podeAvisar(long agora, Long ultimo) {
        return ultimo == null || agora - ultimo >= AVISO_TICKS || agora < ultimo;
    }
}
