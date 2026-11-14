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
    /**
     * Id do {@link Sinal} que nasce da âncora (não é salvo). Diferente do tipo para o aparelho saber que é a vila
     * e não tocar som sozinho por ela.
     */
    public static final String ID_SINAL = "ancora_vila_calada";
    /** Fonte salva do sino a até esta distância da âncora: a salva ganha e a da âncora some (sem texto dobrado). */
    public static final int RAIO_DUPLICADA = 64;

    private RegrasFonteVila() {}

    /**
     * O sinal é o da âncora? O som da vila sem o jogador usar o aparelho espera decisão do Nikolas:
     * o aparelho não faz chiado nem clique sozinho por ele (só luz). Usar o aparelho continua fazendo o som de sempre.
     */
    public static boolean silencioso(String idSinal) {
        return ID_SINAL.equals(idSinal);
    }

    /** Uma fonte salva deste tipo, nesta distância da âncora, já é o sino da vila. */
    public static boolean duplicada(String tipoSalvo, long dx, long dy, long dz) {
        return ("sino_vila".equals(tipoSalvo) || TIPO.equals(tipoSalvo)) && dentro(dx, dy, dz, RAIO_DUPLICADA);
    }

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
