package com.nikolas.odteste.dados;

import java.util.List;

/**
 * Nomes dos dados salvos no jogador. Ficam aqui (Java puro) para o teste conferir
 * que todos estão em docs/ids.md. Depois do lançamento, nunca mudam.
 */
public final class IdsDados {
    public static final String EXPOSICAO = "exposicao";
    public static final String LUCIDEZ = "lucidez";
    public static final String LUCIDEZ_MAX = "lucidez_max";
    public static final String MARCA_ELEMENTO = "marca_elemento";
    public static final String MARCA_ESTAGIO = "marca_estagio";
    public static final String TRANSCENDER_NIVEL = "transcender_nivel";

    public static final List<String> TODOS = List.of(
            EXPOSICAO, LUCIDEZ, LUCIDEZ_MAX, MARCA_ELEMENTO, MARCA_ESTAGIO, TRANSCENDER_NIVEL);

    /** Canal de rede que manda os dados para o cliente do próprio jogador. */
    public static final String PACOTE_SINCRONIZAR = "sincronizar_jogador";

    private IdsDados() {}
}
