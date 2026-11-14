package com.nikolas.odteste.compat.xaero;

import java.util.List;

/**
 * Xaero's Minimap (e a versão "Fair-Play"). Não está no Conguitos ATM10 hoje; fica pronto se entrar. Nenhuma classe
 * do Xaero é tocada (ver {@code cliente/FalhaMinimapa}). Só é lido se a integração estiver ligada.
 */
public final class MinimapaXaero {
    public static final List<String> CAMADAS = List.of("xaerominimap", "xaerominimapfair");
    /** Canto padrão do minimapa do Xaero: em cima, à esquerda. */
    public static final boolean EM_CIMA = true;
    public static final boolean DIREITA = false;

    private MinimapaXaero() {}
}
