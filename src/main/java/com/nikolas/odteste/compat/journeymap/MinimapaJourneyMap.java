package com.nikolas.odteste.compat.journeymap;

import java.util.List;

/**
 * JourneyMap (o minimapa do Conguitos ATM10, {@code journeymap-neoforge-1.21.1-6.0.9}). Nenhuma classe do JourneyMap
 * é tocada: a falha do minimapa ({@code cliente/FalhaMinimapa}) esconde as camadas da HUD com o nome do mod (se ele
 * usar camadas) e cobre o canto do minimapa com chiado. Só é lido se {@code Compat.Integracao.JOURNEYMAP.ligada()}.
 */
public final class MinimapaJourneyMap {
    /** Nome das camadas da HUD do mod. */
    public static final List<String> CAMADAS = List.of("journeymap");
    /** Canto padrão do minimapa no JourneyMap: em cima, à direita. */
    public static final boolean EM_CIMA = true;
    public static final boolean DIREITA = true;

    private MinimapaJourneyMap() {}
}
