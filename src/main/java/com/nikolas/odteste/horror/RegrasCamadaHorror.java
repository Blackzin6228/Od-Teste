package com.nikolas.odteste.horror;

import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;

/**
 * Quando a camada de horror liga e quanto ela escurece (decisão de 14/11/2026). Java puro, testado em
 * RegrasCamadaHorrorTest; usado pelo servidor (quando pode haver susto) e pelo cliente (luz, névoa, música).
 *
 * <ul>
 * <li><b>Noite:</b> só no overworld e perto da superfície (alguma luz do céu chega ali). Mina funda de noite fica
 * como sempre foi: o ATM10 tem muita mineração e a mina já é escura.</li>
 * <li><b>Área da história</b> (hoje a Vila Calada): vale de dia também, mas leve.</li>
 * <li><b>Luz:</b> o escuro pesa onde a luz de bloco é fraca. Tocha, lanterna e vela continuam clareando.
 * O brilho (gamma) do jogador é desfeito nesses pontos: brilho no máximo não clareia o escuro.</li>
 * </ul>
 */
public final class RegrasCamadaHorror {
    /** Luz do céu bruta (0-15, não depende da hora) a partir da qual o lugar conta como "perto do céu". */
    public static final int CEU_MINIMO = 1;
    /** Entrar e sair leva uns 5 s (fração por tick). */
    public static final float PASSO = 1f / 100f;
    /** Força da camada de dia, dentro de uma área (a noite é 1). */
    public static final float FORCA_AREA_DIA = 0.35f;
    /** Quanto a luz cai onde só há luz do céu, com a camada inteira (0,6 = fica com 40%). */
    public static final float ESCURO = 0.6f;
    /** Névoa da noite: até onde se enxerga, com a camada inteira (blocos). */
    public static final float NEVOA_NOITE = 72f;
    /** Cor da névoa e do céu de noite: quanto escurece (0,55 = fica com 45%). */
    public static final float NEVOA_ESCURECE = 0.55f;
    /** Véu de reserva (sem a luz nossa): escuridão máxima da tela, onde não há luz de bloco. */
    public static final float VEU_MAX = 0.45f;

    public enum Momento {
        /** Nada: dia no mundo comum, mina funda, outra dimensão. */
        NADA,
        /** Área da história de dia: leve (névoa, música cortada, sem trava forte). */
        AREA_DIA,
        /** Noite (perto do céu ou dentro de uma área): escuro, luz travada, música cortada, sustos. */
        NOITE
    }

    private RegrasCamadaHorror() {}

    /**
     * @param overworld   o jogador está no overworld (as outras dimensões têm clima próprio)
     * @param horaDoDia   hora do mundo ({@code getDayTime})
     * @param luzDoCeu    luz do céu bruta nos olhos (0-15)
     * @param emArea      dentro de uma área da história
     */
    public static Momento momento(boolean overworld, long horaDoDia, int luzDoCeu, boolean emArea) {
        boolean noite = overworld && RegrasVilaCalada.noite(horaDoDia);
        if (noite && (emArea || luzDoCeu >= CEU_MINIMO)) return Momento.NOITE;
        if (emArea) return Momento.AREA_DIA;
        return Momento.NADA;
    }

    public static boolean ligada(Momento m) {
        return m != null && m != Momento.NADA;
    }

    /** A música (do jogo e dos mods) para quando a camada está ligada. */
    public static boolean cortaMusica(Momento m) {
        return ligada(m);
    }

    /** Sustos grandes sozinhos: só de noite. */
    public static boolean podeSusto(Momento m) {
        return m == Momento.NOITE;
    }

    /** Força-alvo da camada (0 a 1). */
    public static float alvo(Momento m) {
        if (m == null) return 0f;
        return switch (m) {
            case NADA -> 0f;
            case AREA_DIA -> FORCA_AREA_DIA;
            case NOITE -> 1f;
        };
    }

    public static float aproximar(float atual, float alvo, float passo) {
        if (atual < alvo) return Math.min(alvo, atual + passo);
        if (atual > alvo) return Math.max(alvo, atual - passo);
        return atual;
    }

    // ---------- luz (um ponto do mapa de luz do jogo: luz de bloco 0-15 × cor) ----------

    /**
     * Quanto a camada pesa num ponto com esta luz de bloco: inteiro sem luz de bloco, quase nada perto da tocha.
     * {@code apagar} (0 a 1) é a "luz que falha" do susto: aí até a luz de bloco escurece.
     */
    public static float peso(float forca, int luzBloco, float apagar) {
        float b = Math.max(0, Math.min(15, luzBloco)) / 15f;
        float semTocha = (1 - b) * (1 - b);
        float w = forca * semTocha;
        return Math.max(w, Math.max(0f, Math.min(1f, apagar)));
    }

    /** O jogo aplica o brilho assim: mistura a cor com 1-(1-c)^4 pelo valor do brilho (0 a 1). */
    public static float aplicarGamma(float c, float gamma) {
        float x = limitar(c);
        float g = limitar(gamma);
        float levantado = 1 - (float) Math.pow(1 - x, 4);
        return x + (levantado - x) * g;
    }

    /** O contrário: que cor dar ao jogo para que, depois do brilho, apareça {@code mostrado}. */
    public static float desfazerGamma(float mostrado, float gamma) {
        float alvo = limitar(mostrado);
        float g = limitar(gamma);
        if (g <= 0.0001f) return alvo;
        float lo = 0f;
        float hi = alvo; // com brilho ≥ 0 a cor só sobe: a resposta é no máximo o próprio alvo
        for (int i = 0; i < 24; i++) {
            float meio = (lo + hi) * 0.5f;
            if (aplicarGamma(meio, g) < alvo) lo = meio;
            else hi = meio;
        }
        return (lo + hi) * 0.5f;
    }

    /**
     * Novo valor de um canal de cor do mapa de luz. O que aparece na tela fica entre o normal (com o brilho do
     * jogador) e o escuro sem brilho nenhum, pelo peso do ponto.
     */
    public static float canal(float c, float forca, int luzBloco, float apagar, float gamma) {
        float w = peso(forca, luzBloco, apagar);
        if (w <= 0.001f) return c;
        float normal = aplicarGamma(c, gamma);
        float escuro = limitar(c) * (1 - ESCURO * w);
        float mostrar = normal + (escuro - normal) * w;
        return desfazerGamma(mostrar, gamma);
    }

    /** Véu de reserva (sem a luz nossa): quão escura fica a tela, menos perto de luz de bloco. */
    public static float veu(float forca, int luzBloco) {
        return VEU_MAX * peso(forca, luzBloco, 0f);
    }

    /** Alcance da névoa da noite: só chega mais perto, nunca empurra para longe. */
    public static float nevoa(float normal, float forcaNoite) {
        float f = limitar(forcaNoite);
        if (normal <= NEVOA_NOITE || f <= 0f) return normal;
        return normal + (NEVOA_NOITE - normal) * f;
    }

    /** Fator da cor da névoa/céu de noite (multiplica cada canal). */
    public static float corNevoa(float forcaNoite) {
        return 1f - NEVOA_ESCURECE * limitar(forcaNoite);
    }

    private static float limitar(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
