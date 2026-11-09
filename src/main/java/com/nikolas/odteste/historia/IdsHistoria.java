package com.nikolas.odteste.historia;

import java.util.List;

/**
 * Textos que ficam salvos no mundo (dentro de {@code odteste_historia}) e por isso nunca mudam.
 * Java puro: o teste confere que todos estão em docs/ids.md.
 */
public final class IdsHistoria {
    /** Marcos do progresso do grupo. */
    public static final String MARCO_PRIMEIRO_SINAL = "primeiro_sinal";
    public static final String MARCO_ENIGMA_SINO = "enigma_sino";
    public static final List<String> MARCOS = List.of(MARCO_PRIMEIRO_SINAL, MARCO_ENIGMA_SINO);

    /** Lugares que o admin pode ancorar. */
    public static final String LUGAR_VILA_DO_SINO = "vila_do_sino";
    public static final List<String> LUGARES = List.of(LUGAR_VILA_DO_SINO);

    /** Eventos do comando /odteste evento. Não ficam salvos (só o marco que eles dão). */
    public static final String EVENTO_PRIMEIRO_SINAL = "primeiro_sinal";
    public static final List<String> EVENTOS = List.of(EVENTO_PRIMEIRO_SINAL);

    /** Chave do custom_data que marca um item vanilla como peça de história ({"odteste_peca": "odteste:..."}). */
    public static final String CHAVE_PECA = "odteste_peca";

    private IdsHistoria() {}
}
