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

    /**
     * Modificador de velocidade dos aldeões parados olhando o sino ({@code odteste:aldeao_parado}).
     * Temporário: o jogo nunca salva no disco, então o aldeão nunca fica preso se o mod sair do pack.
     */
    public static final String MODIFICADOR_ALDEAO_PARADO = "aldeao_parado";

    /** Chaves dentro de cada vila salva (VilaMarcada). Não mudam: o mundo já tem vilas salvas com elas. */
    public static final List<String> CHAVES_VILA = List.of("grupo", "dim", "sino", "nota", "bau", "placa",
            "estandarte", "cama", "aldeao", "blocos", "congelados", "resolvido", "marcos",
            "apoio_placa", "apoio_placa_bloco", "apoio_estandarte", "apoio_estandarte_bloco");
    /** Chaves de cada estrutura colocada (Colocacao) e do backup de blocos (Backup). */
    public static final List<String> CHAVES_COLOCACAO = List.of("id", "template", "dim", "canto", "backup");
    public static final List<String> CHAVES_BACKUP = List.of("paleta", "pos", "original", "colocado");

    private IdsHistoria() {}
}
