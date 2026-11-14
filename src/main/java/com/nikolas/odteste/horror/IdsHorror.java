package com.nikolas.odteste.horror;

import java.util.List;

/** IDs da camada de horror (todos em docs/ids.md). Java puro. */
public final class IdsHorror {
    /** Pacote servidor → cliente: em que área da história o jogador está ("" = nenhuma). */
    public static final String PACOTE_AREA = "area_horror";
    /** Pacote servidor → cliente: começa um susto grande (tipo pelo id de {@link TipoSusto}). */
    public static final String PACOTE_SUSTO = "susto";
    /** Camada da HUD: véu escuro de reserva (quando a luz da noite não é nossa). */
    public static final String CAMADA_ESCURO = "noite_escura";
    /** Modificador de velocidade dos animais parados na vila morta (temporário: nunca salvo). */
    public static final String MODIFICADOR_ANIMAL = "animal_parado";
    /** Tag de criatura: perto dela, o minimapa falha. */
    public static final String TAG_ASSOMBRA_MAPA = "assombra_mapa";
    /** Áreas da história registradas hoje. */
    public static final String AREA_VILA_CALADA = "vila_calada";

    public static final List<String> TODOS = List.of(PACOTE_AREA, PACOTE_SUSTO, CAMADA_ESCURO, MODIFICADOR_ANIMAL,
            TAG_ASSOMBRA_MAPA);

    private IdsHorror() {}
}
