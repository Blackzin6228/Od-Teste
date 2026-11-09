package com.nikolas.odteste.dados;

import java.util.List;

/**
 * Nomes dos dados salvos no jogador e dos outros IDs do mod. Ficam aqui (Java puro) para o teste conferir
 * que todos estão em docs/ids.md. Depois do lançamento, nunca mudam.
 */
public final class IdsDados {
    public static final String EXPOSICAO = "exposicao";
    public static final String LUCIDEZ = "lucidez";
    public static final String LUCIDEZ_MAX = "lucidez_max";
    public static final String MARCA_ELEMENTO = "marca_elemento";
    public static final String MARCA_ESTAGIO = "marca_estagio";
    public static final String TRANSCENDER_NIVEL = "transcender_nivel";
    /** Centésimos de Exposição guardados até completar 1 ponto (0–99). */
    public static final String EXPOSICAO_RESTO = "exposicao_resto";
    /** Contato secreto com cada elemento (decide qual Marca aparece). */
    public static final String CONTATO_ELEMENTO = "contato_elemento";
    /** Freio de repetição da Exposição (fonte → início da janela e vezes). Salvo para relogar não zerar. */
    public static final String EXPOSICAO_REPETICAO = "exposicao_repeticao";

    public static final List<String> TODOS = List.of(
            EXPOSICAO, LUCIDEZ, LUCIDEZ_MAX, MARCA_ELEMENTO, MARCA_ESTAGIO, TRANSCENDER_NIVEL,
            EXPOSICAO_RESTO, CONTATO_ELEMENTO, EXPOSICAO_REPETICAO);

    /** Canal de rede que manda os dados para o cliente do próprio jogador. */
    public static final String PACOTE_SINCRONIZAR = "sincronizar_jogador";
    /** Canal de rede dos momentos (subir degrau, Marca, surto) para quem está perto. */
    public static final String PACOTE_MOMENTO = "momento";

    /** Modificador de dano da Marca de Sangue (temporário: não fica salvo no jogador). */
    public static final String MODIFICADOR_SANGUE = "marca_sangue_forca";
    /** Camada da HUD com vinheta, borda colorida e escurecer. */
    public static final String CAMADA_TELA = "tela_paranormal";
    /** Animações do Player Animator (assets/odteste/player_animation/). */
    public static final String ANIM_MARCA_APARECE = "marca_aparece";
    public static final String ANIM_MARCA_SOBE = "marca_sobe";
    public static final String ANIM_SURTO = "surto";
    /** Camada de animação do Player Animator. */
    public static final String CAMADA_ANIMACAO = "animacao_sistemas";

    /** Canal de rede que mostra a Marca de um jogador para quem está vendo ele. */
    public static final String PACOTE_MARCA_VISIVEL = "marca_visivel";
    /** Tag de dano e tag de criatura "paranormal" (pesam mais na Lucidez). */
    public static final String TAG_PARANORMAL = "paranormal";
    /** Tag de item: comida quente que devolve Lucidez (data/odteste/tags/item/comida_quente.json). */
    public static final String TAG_COMIDA_QUENTE = "comida_quente";
    /** Textura (gerada no cliente) das veias da Marca de Sangue; uma por estágio: marca_sangue_1 a _4. */
    public static final String TEXTURA_MARCA_SANGUE = "marca_sangue";
    /** Modificador de velocidade da Lucidez baixa (temporário: não fica salvo no jogador). */
    public static final String MODIFICADOR_LENTIDAO = "lucidez_lentidao";

    /** Todos os IDs que não são dado salvo, mas também precisam estar em docs/ids.md. */
    public static final List<String> OUTROS = List.of(
            PACOTE_SINCRONIZAR, PACOTE_MOMENTO, MODIFICADOR_SANGUE, MODIFICADOR_LENTIDAO, CAMADA_TELA,
            ANIM_MARCA_APARECE, ANIM_MARCA_SOBE, ANIM_SURTO, CAMADA_ANIMACAO,
            PACOTE_MARCA_VISIVEL, TAG_PARANORMAL, TAG_COMIDA_QUENTE, TEXTURA_MARCA_SANGUE + "_1", TEXTURA_MARCA_SANGUE + "_2",
            TEXTURA_MARCA_SANGUE + "_3", TEXTURA_MARCA_SANGUE + "_4");

    private IdsDados() {}
}
