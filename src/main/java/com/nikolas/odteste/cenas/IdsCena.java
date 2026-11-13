package com.nikolas.odteste.cenas;

import java.util.List;

/** Nomes das cenas que ficam salvos ou registrados (todos em docs/ids.md). Java puro. Nunca mudam. */
public final class IdsCena {
    /** Dado do jogador ({@code odteste:cenas_vistas}): lista de cenas que ele já viu. */
    public static final String DADO_VISTAS = "cenas_vistas";
    /** Pacote servidor → cliente ({@code odteste:tocar_cena}). */
    public static final String PACOTE_TOCAR = "tocar_cena";
    /** Camada da HUD da cena (preto, legenda, aviso de pular). */
    public static final String CAMADA = "cena";

    /** Cena de chegada à Vila Calada. Fica salva em {@code cenas_vistas}. */
    public static final String ENTRADA_VILA_CALADA = "entrada_vila_calada";

    /** Todas as cenas (ids salvos). */
    public static final List<String> CENAS = List.of(ENTRADA_VILA_CALADA);

    private IdsCena() {}
}
