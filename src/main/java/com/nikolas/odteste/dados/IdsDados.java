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

    /** Último uso de cada Artefato Paranormal (id do item → tempo do mundo). Recarga que sobrevive a relogar. */
    public static final String ARTEFATO_RECARGA = "artefato_recarga";
    /** Noite (dia do relógio) em que a última criatura de Sangue nasceu sozinha perto do jogador. */
    public static final String CRIATURA_NOITE = "criatura_noite";

    /** Elemento do jogador, definido pelo admin (nenhum = sem elemento). Nunca vai para o cliente. */
    public static final String ELEMENTO = "elemento";
    /** Se a história já revelou o elemento ao jogador. Nunca vai para o cliente. */
    public static final String ELEMENTO_REVELADO = "elemento_revelado";

    /** Cansaço do elemento: dia de jogo e matérias brutas ganhas nele (metade depois de 8, um quarto depois de 16). */
    public static final String CANSACO_ELEMENTO = "cansaco_elemento";

    public static final List<String> TODOS = List.of(
            EXPOSICAO, LUCIDEZ, LUCIDEZ_MAX, MARCA_ELEMENTO, MARCA_ESTAGIO, TRANSCENDER_NIVEL,
            EXPOSICAO_RESTO, CONTATO_ELEMENTO, EXPOSICAO_REPETICAO, ARTEFATO_RECARGA, CRIATURA_NOITE,
            ELEMENTO, ELEMENTO_REVELADO, CANSACO_ELEMENTO);

    // ---------- Play test de Sangue, parte 2: conteúdo ----------

    /** Espada de Sangue (sem receita). */
    public static final String ITEM_RUBRA = "rubra";
    /** Artefatos Paranormais de Sangue (sem receita). */
    public static final String ITEM_AGULHA = "agulha_de_cerzir_carne";
    public static final String ITEM_DEDAL = "dedal_dentado";
    public static final String ITEM_SINO = "sino_de_matadouro";
    /** Matéria paranormal de Sangue, bruta (cai das criaturas). */
    public static final String ITEM_MATERIA_SANGUE = "materia_sangue_bruta";
    public static final List<String> ITENS = List.of(ITEM_RUBRA, ITEM_AGULHA, ITEM_DEDAL, ITEM_SINO, ITEM_MATERIA_SANGUE);
    /** Artefatos de Sangue, do mais comum ao mais raro (mesma ordem de RegrasAchados.PESOS). */
    public static final List<String> ARTEFATOS = List.of(ITEM_AGULHA, ITEM_DEDAL, ITEM_SINO);

    /** Chave NBT que marca, no template de uma estrutura, o baú que ganha recompensa de enigma. */
    public static final String RECOMPENSA_CHAVE = "odteste_recompensa";
    /** Valor da marca: baú com um artefato de Sangue (tabela abaixo). */
    public static final String RECOMPENSA_ARTEFATO_SANGUE = "artefato_sangue";
    /** Tabela de loot (data/odteste/loot_table/chests/) aplicada ao baú marcado quando a estrutura é colocada. */
    public static final String LOOT_RECOMPENSA_ARTEFATO_SANGUE = "chests/recompensa_artefato_sangue";

    /** Criaturas de Sangue (GeckoLib). Mesmo nome no modificador de bioma e na tabela de drop. */
    public static final String CRIATURA_RETALHADO = "retalhado";
    public static final String CRIATURA_SANGUESSUGA = "sanguessuga_mae";
    public static final List<String> CRIATURAS = List.of(CRIATURA_RETALHADO, CRIATURA_SANGUESSUGA);
    /**
     * "O Que Segue" (nome provisório; o ID não muda). Fora de {@link #CRIATURAS}: não nasce por bioma, não tem drop
     * e não é de Sangue. Nunca é salva no mundo.
     */
    public static final String CRIATURA_O_QUE_SEGUE = "o_que_segue";

    /** Efeito "Sangramento" e o tipo de dano com o mesmo nome (data/odteste/damage_type/). */
    public static final String SANGRAMENTO = "sangramento";
    /** Aba do modo criativo. */
    public static final String ABA = "principal";
    /** Modificador de vida máxima do Dedal Dentado (−1 coração até dormir; fica salvo no jogador). */
    public static final String MODIFICADOR_DEDAL = "dedal_coracao";
    /** Ritual do Occultism (fábrica de ritual); a receita é odteste:ritual/chamado_de_sangue. */
    public static final String RITUAL_CHAMADO = "chamado_de_sangue";
    /** Etiqueta (scoreboard tag) da criatura que veio de spawner: não solta matéria. Fica salva na criatura. */
    public static final String ETIQUETA_SEM_MATERIA = "odteste_sem_materia";
    /** Chave salva na criatura: jogadores que já a viram (Exposição e Lucidez só uma vez por jogador). */
    public static final String CHAVE_SENTIRAM = "odteste_sentiram";
    /** Plugin do JEI que esconde os itens e o ritual sem receita. */
    public static final String PLUGIN_JEI = "esconder_jei";

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
    /** Pasta das texturas da Marca de Sangue v2 (estagio_N[_brilho][_alex].png). Só cliente, não é dado salvo. */
    public static final String TEXTURA_MARCA_SANGUE = "textures/entity/marca_sangue";
    /** Modificador de velocidade da Lucidez baixa (temporário: não fica salvo no jogador). */
    public static final String MODIFICADOR_LENTIDAO = "lucidez_lentidao";

    /** Tag de bloco: o que a história pode ocupar além do ar (data/odteste/tags/block/substituivel.json). */
    public static final String TAG_SUBSTITUIVEL = "substituivel";
    /** Peças de história (item vanilla com custom_data {odteste_peca: "odteste:..."}). */
    public static final String PECA_LIVRO_CARNICEIRO_1 = "livro_carniceiro_1";
    public static final String PECA_MAPA_ESQUERDA = "mapa_rasgado_esquerda";
    /** Arquivo de dados do mundo com grupos, progresso e eventos (data/odteste_historia.dat). Sem "odteste:". */
    public static final String ARQUIVO_HISTORIA = "odteste_historia";

    // ---------- Balanceamento tech (13/11/2026) ----------

    /** Tipo de dano paranormal (data/odteste/damage_type/paranormal.json): golpe de criatura nossa. */
    public static final String DANO_PARANORMAL = "paranormal";
    /** Tag de item: arma paranormal (teto 20; x2 contra criatura nossa). */
    public static final String TAG_ARMA_PARANORMAL = "arma_paranormal";
    /** Tag de tipo de dano: magia de outros mods (passa por cima da resistência das criaturas nossas). */
    public static final String TAG_MAGIA = "magia";
    /** Modificador do voo criativo (NeoForge) nas nossas dimensões e na perseguição (temporário). */
    public static final String MODIFICADOR_VOO = "contencao_voo";

    /** Todos os IDs que não são dado salvo, mas também precisam estar em docs/ids.md. */
    public static final List<String> OUTROS = List.of(
            DANO_PARANORMAL, TAG_ARMA_PARANORMAL, TAG_MAGIA, MODIFICADOR_VOO,
            PACOTE_SINCRONIZAR, PACOTE_MOMENTO, MODIFICADOR_SANGUE, MODIFICADOR_LENTIDAO, CAMADA_TELA,
            ANIM_MARCA_APARECE, ANIM_MARCA_SOBE, ANIM_SURTO, CAMADA_ANIMACAO,
            PACOTE_MARCA_VISIVEL, TAG_PARANORMAL, TAG_COMIDA_QUENTE, TEXTURA_MARCA_SANGUE,
            ITEM_RUBRA, ITEM_AGULHA, ITEM_DEDAL, ITEM_SINO, ITEM_MATERIA_SANGUE,
            CRIATURA_RETALHADO, CRIATURA_SANGUESSUGA, SANGRAMENTO, ABA, MODIFICADOR_DEDAL, RITUAL_CHAMADO,
            "ritual/" + RITUAL_CHAMADO, PLUGIN_JEI,
            TAG_SUBSTITUIVEL, PECA_LIVRO_CARNICEIRO_1, PECA_MAPA_ESQUERDA);

    private IdsDados() {}
}
