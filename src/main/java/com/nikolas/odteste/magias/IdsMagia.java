package com.nikolas.odteste.magias;

import java.util.List;

/**
 * IDs das magias (Java puro, para o teste conferir que todos estão em docs/ids.md).
 * Depois do lançamento, nunca mudam: o Iron's grava o ID da magia nos pergaminhos e grimórios,
 * e o ID da escola nos itens que dão bônus por escola.
 */
public final class IdsMagia {
    /** Escola nova do Iron's (odteste:sangue). Nome na tela provisório, sem confundir com a Blood do Iron's. */
    public static final String ESCOLA_SANGUE = "sangue";
    /** Primeira magia: Talho Rubro (Sangue, nível Básico). Também é o nome da animação do jogador. */
    public static final String TALHO_RUBRO = "talho_rubro";
    /** Tipo de dano das magias de Sangue (data/odteste/damage_type/magia_sangue.json). */
    public static final String DANO_MAGIA_SANGUE = "magia_sangue";
    /** Atributos da escola (só existem com o Iron's no pack). */
    public static final String PODER_SANGUE = "poder_sangue";
    public static final String RESISTENCIA_SANGUE = "resistencia_sangue";
    /** Tag de item que serve de foco da escola no Iron's (data/odteste/tags/item/foco_sangue.json; hoje vazia). */
    public static final String FOCO_SANGUE = "foco_sangue";
    /** Pacote servidor → clientes que veem quem conjura: "a magia X entrou na fase Y". */
    public static final String PACOTE_FASE = "magia_fase";

    /** Magias que o comando de teste conhece (o id é o mesmo nos dois caminhos: Iron's e Afinidade). */
    public static final List<String> MAGIAS = List.of(TALHO_RUBRO);

    public static final List<String> TODOS = List.of(
            ESCOLA_SANGUE, TALHO_RUBRO, DANO_MAGIA_SANGUE, PODER_SANGUE, RESISTENCIA_SANGUE, FOCO_SANGUE, PACOTE_FASE);

    private IdsMagia() {}
}
