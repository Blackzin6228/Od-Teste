package com.nikolas.odteste.sinais;

/**
 * Regras da fonte de sinal da Vila Calada (Java puro, testado em {@code RegrasFonteVilaTest}).
 * A fonte não é salva: nasce, a cada leitura, da âncora {@code vila_calada} que a história já guarda.
 */
public final class RegrasFonteVila {
    /** Nome da fonte (aparece só em log; não é salvo). */
    public static final String ID_FONTE = "vila_calada";
    /** Tipo de texto: {@code odteste.sinal.vila_calada.decifrado} e {@code .camada} no lang. */
    public static final String TIPO = "vila_calada";
    /** O sinal da vila tem segunda camada (o que só o aparelho separa). */
    public static final boolean COM_CAMADA = true;

    private RegrasFonteVila() {}

    /**
     * A vila emite? Só com a pista ligada pelo admin, a âncora gravada e o aparelho na mesma dimensão dela.
     * Mundo sem âncora (o caso de quem ainda não chegou no Ato 1): não emite, sem erro.
     */
    public static boolean emite(boolean pistasLigadas, boolean temAncora, boolean mesmaDimensao) {
        return pistasLigadas && temAncora && mesmaDimensao;
    }

    /** Distância ao quadrado até {@code raio}, contando também a altura (igual às fontes salvas). */
    public static boolean dentro(long dx, long dy, long dz, int raio) {
        if (raio < 0) return false;
        long r2 = (long) raio * raio;
        return dx * dx + dy * dy + dz * dz <= r2;
    }

    /**
     * Entre a fonte salva mais perto e a vila, qual o aparelho aponta. {@code true} = a vila.
     * Empate fica com a salva (o admin a pôs ali de propósito).
     */
    public static boolean vilaGanha(boolean temSalva, double d2Salva, double d2Vila) {
        return !temSalva || d2Vila < d2Salva;
    }
}
