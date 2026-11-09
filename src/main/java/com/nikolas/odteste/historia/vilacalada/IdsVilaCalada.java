package com.nikolas.odteste.historia.vilacalada;

import java.util.List;

/**
 * Textos da Vila Calada que ficam salvos no mundo e por isso nunca mudam (entram em docs/ids.md).
 * Java puro.
 */
public final class IdsVilaCalada {
    /** Lugar ancorado (dentro de {@code ancoras} do arquivo {@code odteste_historia}): para onde as pistas apontam. */
    public static final String LUGAR_VILA_CALADA = "vila_calada";

    /** Marco do grupo: alguém do grupo chegou à Vila Calada. As pistas de longe param para esse grupo. */
    public static final String MARCO_CHEGOU = "vila_calada";

    /** Arquivo próprio ({@code data/odteste_vila_calada.dat} do overworld): liga/desliga e a colocação que ancorou. */
    public static final String ARQUIVO = "odteste_vila_calada";

    /** Chaves dentro do arquivo. */
    public static final List<String> CHAVES = List.of("ligado", "colocacao", "ancora_auto", "ancora_auto_dim", "vistas");

    /**
     * Modificador de velocidade dos aldeões virados para a Vila Calada ({@code odteste:aldeao_virado}).
     * Temporário: o jogo nunca salva no disco, então nenhum aldeão fica preso se o mod sair do pack.
     */
    public static final String MODIFICADOR_ALDEAO_VIRADO = "aldeao_virado";

    /** Template da Vila Calada que ancora sozinho ao terminar de ser colocado (nome exato). */
    public static final String TEMPLATE = "odteste:vila_calada";

    private IdsVilaCalada() {}
}
