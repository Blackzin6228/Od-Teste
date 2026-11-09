package com.nikolas.odteste.elementos;

/**
 * Regras do elemento do jogador (decisão de 2026-11-08, docs/decisoes/2026-11-08-elemento-do-jogador.md).
 * O admin define o elemento de cada jogador; ele fica escondido até a história revelar.
 * Espada e artefatos do próprio elemento custam menos. Nenhum número disso aparece na tela.
 *
 * Java puro (sem Minecraft), testado em RegrasElementoTest.
 */
public final class RegrasElemento {
    /** Quanto o custo cai quando o artefato ou a espada é do elemento do jogador (30%). Proposta para o play test. */
    public static final double DESCONTO = 0.30;
    /** Prefixo dos marcadores de texto dos eventos: {escolhido_sangue}, {escolhido_morte}... */
    public static final String PREFIXO_MARCADOR = "escolhido_";

    private RegrasElemento() {}

    /** O objeto é do elemento do jogador? Sem elemento (nenhum) nunca é. */
    public static boolean afim(Elemento doJogador, Elemento doObjeto) {
        return doJogador != null && doJogador != Elemento.NENHUM && doJogador == doObjeto;
    }

    /**
     * Custo inteiro (Lucidez, Exposição) com o desconto do próprio elemento.
     * Arredonda para o mais perto e nunca zera um custo que existia (mínimo 1).
     */
    public static int custo(int base, Elemento doJogador, Elemento doObjeto) {
        if (base <= 0 || !afim(doJogador, doObjeto)) return base;
        return Math.max(1, (int) Math.round(base * (1 - DESCONTO)));
    }

    /** Custo em vida (pode ser quebrado: meio coração, menos). */
    public static float custoVida(float base, Elemento doJogador, Elemento doObjeto) {
        if (!(base > 0) || !afim(doJogador, doObjeto)) return base;
        return (float) (base * (1 - DESCONTO));
    }

    /**
     * Limiar de vida em que uma espada "acorda" (fração da vida máxima). O custo é a vida que falta:
     * 30% a menos de vida perdida. Ex.: 0,7 (precisa perder 30%) vira 0,79 (precisa perder 21%).
     */
    public static float limiarVida(float limiar, Elemento doJogador, Elemento doObjeto) {
        if (!afim(doJogador, doObjeto)) return limiar;
        return (float) (1 - (1 - limiar) * (1 - DESCONTO));
    }

    /** Nome do marcador de texto de um elemento: "escolhido_sangue". NENHUM não tem marcador. */
    public static String marcador(Elemento e) {
        if (e == null || e == Elemento.NENHUM) throw new IllegalArgumentException("nenhum não tem marcador");
        return PREFIXO_MARCADOR + e.id();
    }
}
