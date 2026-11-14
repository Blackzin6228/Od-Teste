package com.nikolas.odteste.historia.vilacalada;

import java.util.List;

/**
 * Números e curvas dos quatro efeitos de tela do Sangue na Vila Calada (decisão de 14/11/2026, camada de horror).
 * Java puro (testado em RegrasFxSangueTest); o cliente (cliente/FxSangue) e a prévia fora do jogo
 * (ferramentas/fx/previa_sangue.py) usam as mesmas curvas.
 */
public final class RegrasFxSangue {
    /** Nomes dos efeitos no comando {@code /odfx} e no pacote {@code odteste:efeito_fx}. Não renomear. */
    public static final String CARMESIM = "carmesim";
    public static final String SANGUE = "sangue";
    public static final String MARCADO = "marcado";
    public static final String CORACAO = "coracao";
    public static final List<String> EFEITOS = List.of(CARMESIM, SANGUE, MARCADO, CORACAO);
    /** Efeitos que o mestre pode soltar num jogador (os outros dois são do lugar e da Marca). */
    public static final List<String> DO_MESTRE = List.of(SANGUE, CORACAO);

    // ---- sangue nas paredes e no chão (segundos)
    public static final float SANGUE_SOBE = 6f;
    public static final float SANGUE_VIVE = 6f;
    public static final float SANGUE_SECA = 5f;
    public static final float SANGUE_SOME = 3f;
    /** Até onde a poça chega a partir de onde a coisa vem (blocos). */
    public static final float SANGUE_ALCANCE = 12f;
    /** Uma criatura de Sangue a esta distância (blocos) solta o sangue... */
    public static final double SANGUE_DISTANCIA = 18;
    /** ...no máximo uma vez a cada 3 min (sustos raros: decisão de 14/11). */
    public static final int SANGUE_ESPERA_TICKS = 3 * 60 * 20;

    // ---- pulso do coração
    /** Começa a bater com a criatura a esta distância (blocos)... */
    public static final double CORACAO_LONGE = 16;
    /** ...e está no máximo a esta. */
    public static final double CORACAO_PERTO = 4;
    /** Intervalo entre batidas (s): longe e colado. */
    public static final float BATIDA_LONGE = 1.15f;
    public static final float BATIDA_PERTO = 0.55f;

    // ---- visão do Marcado
    /** Intervalo da batida própria do Marcado (s), quando não há coração acelerado. */
    public static final float BATIDA_MARCADO = 1.0f;

    // ---- teste e gravação
    /** Quanto dura um efeito solto pelo comando (s), quando ninguém diz. */
    public static final int TESTE_PADRAO = 12;
    public static final int TESTE_MAXIMO = 120;
    /** Gravação: no máximo isto de quadros (um por tick). */
    public static final int GRAVAR_MAXIMO = 400;

    private RegrasFxSangue() {}

    public static boolean conhecido(String efeito) {
        return EFEITOS.contains(efeito);
    }

    public static float suave(float x) {
        float u = Math.max(0f, Math.min(1f, x));
        return u * u * (3f - 2f * u);
    }

    /** Duração total do sangue no chão (s). */
    public static float sangueTotal() {
        return SANGUE_SOBE + SANGUE_VIVE + SANGUE_SECA + SANGUE_SOME;
    }

    /** Força (0..1) do sangue em {@code t} segundos: cheio até começar a sumir, some em SANGUE_SOME. */
    public static float sangueForca(float t) {
        if (t < 0) return 0f;
        float total = sangueTotal();
        return 1f - suave((t - total + SANGUE_SOME) / SANGUE_SOME);
    }

    /** Quanto o sangue já secou (0 = fresco, 1 = seco). */
    public static float sangueSeca(float t) {
        return suave((t - SANGUE_SOBE - SANGUE_VIVE) / SANGUE_SECA);
    }

    /**
     * Forma do batimento (0..1) {@code f} segundos depois do começo da batida: um "tum" forte e um mais fraco
     * 0,2 s depois. Igual a {@code batida()} da prévia.
     */
    public static float batida(float f) {
        if (f < 0) return 0f;
        double a = Math.exp(-Math.pow((f - 0.03) / 0.055, 2));
        double b = 0.6 * Math.exp(-Math.pow((f - 0.23) / 0.06, 2));
        return (float) Math.min(1.0, a + b);
    }

    /** Quão perto (0..1) pela distância da criatura: 0 em CORACAO_LONGE ou mais, 1 em CORACAO_PERTO ou menos. */
    public static float perto(double distancia) {
        double x = (CORACAO_LONGE - distancia) / (CORACAO_LONGE - CORACAO_PERTO);
        return (float) Math.max(0, Math.min(1, x));
    }

    /** Intervalo entre batidas (s) para {@code perto} (0..1): mais perto, mais rápido. */
    public static float intervalo(float perto) {
        float p = Math.max(0f, Math.min(1f, perto));
        return BATIDA_LONGE + (BATIDA_PERTO - BATIDA_LONGE) * p;
    }

    /** Estágio da Marca (1..4) para o uniform do shader (0..1). Sem Marca de Sangue: 0. */
    public static float estagio(int estagio) {
        if (estagio <= 0) return 0f;
        return Math.min(4, estagio) / 4f;
    }

    /**
     * Força do shader pela opção {@code efeitos_fortes} (tremor 1 / 0,5 / 0): cor, névoa, sangue e veias
     * diminuem mas não somem (decisão de 14/11: diminuir, não desligar).
     */
    public static float forcaVisual(float tremor) {
        return 0.5f + 0.5f * Math.max(0f, Math.min(1f, tremor));
    }

    /** Segundos do teste pelo comando, presos entre 1 e TESTE_MAXIMO. */
    public static int segundosTeste(int pedido) {
        if (pedido <= 0) return TESTE_PADRAO;
        return Math.min(TESTE_MAXIMO, pedido);
    }
}
