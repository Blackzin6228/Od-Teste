package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.historia.vilacalada.RegrasFxSangue;
import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;

/**
 * Números do efeito de tela {@code cor_horror} (decisão de 14/11/2026, camada de horror: forte no mundo inteiro,
 * de dia também). Java puro, testado em RegrasCorHorrorTest; o cliente (cliente/FxCorHorror) e a prévia fora do
 * jogo (ferramentas/fx/previa_cor_horror.py) usam as mesmas contas.
 *
 * <ul>
 * <li>Vale no overworld inteiro, de dia (forte) e de noite (mais pesado). Nether, End e outras: nada.</li>
 * <li>Na Vila Calada, à noite, dá lugar ao carmesim (que já lava a cor do jeito dele).</li>
 * <li>{@code efeitos_fortes} diminui (metade no DESLIGADO), não desliga.</li>
 * </ul>
 */
public final class RegrasCorHorror {
    /** Entrar e sair da noite leva uns 5 s (fração por tick), igual à camada de horror. */
    public static final float PASSO = 1f / 100f;

    private RegrasCorHorror() {}

    /** Alvo da parte "noite" pela hora do mundo (0 ou 1; o cliente suaviza). */
    public static float noiteAlvo(boolean overworld, long horaDoDia) {
        return overworld && RegrasVilaCalada.noite(horaDoDia) ? 1f : 0f;
    }

    /** Força-padrão de dia (sem a camada de horror do cliente): 1 no overworld, 0 fora. */
    public static float diaPadrao(boolean overworld) {
        return overworld ? 1f : 0f;
    }

    /**
     * Força que vai para o shader (uniform {@code Forca}).
     *
     * @param dia      0..1, quanto a camada vale aqui (1 no overworld)
     * @param carmesim 0..1, quanto o carmesim da Vila está ligado (o cor_horror sai na mesma medida)
     * @param tremor   multiplicador do {@code efeitos_fortes} (1 / 0,5 / 0)
     */
    public static float forca(float dia, float carmesim, float tremor) {
        float d = Math.max(0f, Math.min(1f, dia));
        float c = Math.max(0f, Math.min(1f, carmesim));
        return d * (1f - c) * RegrasFxSangue.forcaVisual(tremor);
    }

    public static float aproximar(float atual, float alvo, float passo) {
        if (atual < alvo) return Math.min(alvo, atual + passo);
        return Math.max(alvo, atual - passo);
    }
}
