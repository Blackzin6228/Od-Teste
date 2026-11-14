package com.nikolas.odteste.horror;

import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;

/**
 * Quando a camada de horror liga e quanto ela escurece (decisão de 14/11/2026). Java puro, testado em
 * RegrasCamadaHorrorTest; usado pelo servidor (quando pode haver susto) e pelo cliente (luz, névoa, música).
 *
 * <ul>
 * <li><b>Mundo inteiro (overworld), de dia também</b> (mudança do Nikolas, 14/11/2026): céu fechado e cinza, névoa
 * mais perto que a do jogo, luz do dia um pouco mais baixa (dá para trabalhar e minerar) e música cortada. É a
 * "camada de fundo" ({@link #alvoDia}). Nether, End e outras dimensões ficam fora.</li>
 * <li><b>Noite:</b> por cima do fundo, mais pesada, perto da superfície (alguma luz do céu chega ali). Mina funda de
 * noite fica só com o fundo: o ATM10 tem muita mineração e a mina já é escura.</li>
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

    // ---------- camada de fundo (mundo inteiro, de dia também) ----------
    /** Luz do dia: quanto cai onde só há luz do céu, com o fundo inteiro (0,3 = fica com 70%; moderado). */
    public static final float ESCURO_DIA = 0.3f;
    /** Névoa de dia: até onde se enxerga, com o fundo inteiro (blocos). Mais perto que a do jogo, mais longe que a noite. */
    public static final float NEVOA_DIA = 112f;
    /** Névoa do céu com o fundo inteiro: o céu vira todo cor de névoa (fechado). */
    public static final float CEU_NEVOA = 2f;
    /** Quanto a cor da névoa/céu perde de cor (0,8 = quase cinza). */
    public static final float CINZA = 0.8f;
    /** O cinza também é um pouco mais escuro que o céu limpo. */
    public static final float CINZA_ESCURECE = 0.2f;
    /** Véu de reserva de dia (shader pack ou céu de outro mod): tela no máximo assim de cinza escuro. */
    public static final float VEU_DIA_MAX = 0.18f;
    /** Quanto a camada de fundo pesa no "total" que o shader lê (a noite e as áreas chegam a 1). */
    public static final float FORCA_DIA_TOTAL = 0.5f;
    /** Opção {@code intensidade} do cliente (%): dá para diminuir até aqui, não desligar. */
    public static final int INTENSIDADE_MIN = 50;

    public enum Momento {
        /** Nada: fora do overworld (Nether, End, outras dimensões). */
        NADA,
        /** Overworld, só a camada de fundo: de dia, ou de noite na mina funda / longe do céu. */
        MUNDO,
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
        if (!overworld) return Momento.NADA; // Nether, End e outras: fora, mesmo numa área
        boolean noite = RegrasVilaCalada.noite(horaDoDia);
        if (noite && (emArea || luzDoCeu >= CEU_MINIMO)) return Momento.NOITE;
        if (emArea) return Momento.AREA_DIA;
        return Momento.MUNDO;
    }

    public static boolean ligada(Momento m) {
        return m != null && m != Momento.NADA;
    }

    /** A música (do jogo e dos mods) para quando a camada está ligada: no overworld inteiro, de dia também. */
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
            case NADA, MUNDO -> 0f;
            case AREA_DIA -> FORCA_AREA_DIA;
            case NOITE -> 1f;
        };
    }

    /** Força-alvo da camada de fundo (0 a 1): inteira em todo o overworld, nada fora dele. */
    public static float alvoDia(Momento m) {
        return ligada(m) ? 1f : 0f;
    }

    /** O que o shader lê: o maior entre o fundo (até {@link #FORCA_DIA_TOTAL}) e a parte pesada (noite/área). */
    public static float total(float forcaDia, float forca) {
        return limitar(Math.max(FORCA_DIA_TOTAL * limitar(forcaDia), limitar(forca)));
    }

    /** Opção {@code intensidade} (%) vira multiplicador: entre 0,5 e 1, nunca zero. */
    public static float intensidade(int porcento) {
        return Math.max(INTENSIDADE_MIN, Math.min(100, porcento)) / 100f;
    }

    /**
     * Intervalo até o próximo som grave e longe (em ticks), ou -1 = nenhum agora. De noite fora das áreas: 1 a 2,5 min.
     * Só com o fundo (de dia, mina): bem mais raro, 3 a 7 min. Nas áreas (que têm sons próprios) e fora do
     * overworld: nenhum. {@code sorte} de 0 a 1.
     */
    public static int intervaloSom(Momento m, boolean emArea, double sorte) {
        double s = Math.max(0, Math.min(1, sorte));
        if (m == null || emArea) return -1;
        return switch (m) {
            case NOITE -> 20 * (60 + (int) (s * 90));
            case MUNDO -> 20 * (180 + (int) (s * 240));
            default -> -1;
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

    /**
     * Luz do dia mais baixa (camada de fundo), num ponto do mapa de luz: só a parte que vem do céu cai, e pouco
     * ({@link #ESCURO_DIA}). Mina sem luz do céu e perto de tocha: nada muda. Sem mexer no brilho do jogador.
     */
    public static float canalDia(float c, float forcaDia, int luzBloco, int luzCeu) {
        float f = limitar(forcaDia);
        if (f <= 0.001f) return c;
        float b = Math.max(0, Math.min(15, luzBloco)) / 15f;
        float s = Math.max(0, Math.min(15, luzCeu)) / 15f;
        return limitar(c) * (1 - ESCURO_DIA * f * s * (1 - b) * (1 - b));
    }

    /** Véu de reserva de dia (cinza escuro): só onde chega luz do céu e longe de tocha. */
    public static float veuDia(float forcaDia, int luzCeu, int luzBloco) {
        float b = Math.max(0, Math.min(15, luzBloco)) / 15f;
        float s = Math.max(0, Math.min(15, luzCeu)) / 15f;
        return VEU_DIA_MAX * limitar(forcaDia) * s * (1 - b);
    }

    /** Névoa de dia: só chega mais perto, nunca empurra para longe. */
    public static float nevoaDia(float normal, float forcaDia) {
        float f = limitar(forcaDia);
        if (normal <= NEVOA_DIA || f <= 0f) return normal;
        return normal + (NEVOA_DIA - normal) * f;
    }

    /**
     * Névoa do céu: com o fundo inteiro o céu fica todo cor de névoa (fechado e cinza). De noite o fundo do céu solta
     * (fica a lua das áreas e a noite como já era): {@code k = forcaDia × (1 − forcaNoite)}.
     */
    public static float nevoaCeu(float normal, float forcaDia, float forcaNoite) {
        float k = limitar(forcaDia) * (1 - limitar(forcaNoite));
        if (normal <= CEU_NEVOA || k <= 0f) return normal;
        return normal + (CEU_NEVOA - normal) * k;
    }

    /** Pula o céu do jogo (sol, nuvem de pôr do sol): só de dia com o fundo quase inteiro. O fundo da tela já é a cor da névoa. */
    public static boolean pularCeu(float forcaDia, float forcaNoite) {
        return forcaDia >= 0.9f && forcaNoite <= 0.1f;
    }

    /** Um canal da cor da névoa/céu puxado para o cinza ({@code luma} = brilho da cor original). */
    public static float cinza(float c, float luma, float forcaDia) {
        float f = limitar(forcaDia);
        float m = CINZA * f;
        float misturado = c + (luma - c) * m;
        return limitar(misturado * (1 - CINZA_ESCURECE * f));
    }

    /** Brilho de uma cor (pesos comuns de tela). */
    public static float luma(float r, float g, float b) {
        return 0.299f * r + 0.587f * g + 0.114f * b;
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
