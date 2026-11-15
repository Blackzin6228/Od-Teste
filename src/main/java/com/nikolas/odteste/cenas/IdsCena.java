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

    /** Pacote servidor → alvo (e mestre assistindo) ({@code odteste:cinema}): cena em JSON com âncora e olhos. */
    public static final String PACOTE_CINEMA = "cinema";
    /** Pacote servidor → jogador ({@code odteste:cena_parar}): para a cena que estiver tocando. */
    public static final String PACOTE_PARAR = "cena_parar";
    /** Pacote jogador → servidor ({@code odteste:cena_fim}): a cena acabou (ou pulou); o servidor solta o jogador. */
    public static final String PACOTE_FIM = "cena_fim";
    /** Camada da HUD das cenas em JSON (barras, véu, texto, aviso de pular). */
    public static final String CAMADA_CINEMA = "cinema";
    /** Pasta das cenas em JSON dentro de {@code data/<mod>/} (datapack pode trocar ou somar). */
    public static final String PASTA = "cenas";
    /** Âncora padrão das cenas em JSON: onde o alvo está, olhando para onde ele olha. */
    public static final String LUGAR_JOGADOR = "jogador";
    /** Lugar: o sino rachado da Vila Calada (achado pelo servidor). */
    public static final String LUGAR_SINO_RACHADO = "sino_rachado";
    /** Cena em JSON de exemplo: a chegada do grupo na Vila Calada (só por comando, até o Nikolas ver). */
    public static final String CHEGADA_VILA_CALADA = "chegada_vila_calada";

    /** Cena de chegada à Vila Calada. Fica salva em {@code cenas_vistas}. */
    public static final String ENTRADA_VILA_CALADA = "entrada_vila_calada";

    /** Todas as cenas (ids salvos). */
    public static final List<String> CENAS = List.of(ENTRADA_VILA_CALADA);

    private IdsCena() {}
}
