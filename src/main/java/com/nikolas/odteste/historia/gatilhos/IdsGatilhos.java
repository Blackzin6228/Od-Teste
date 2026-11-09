package com.nikolas.odteste.historia.gatilhos;

import java.util.List;

/**
 * Textos dos gatilhos que ficam salvos no mundo ou viram nome de arquivo e por isso nunca mudam
 * (entram em docs/ids.md). Java puro.
 */
public final class IdsGatilhos {
    /** Arquivo próprio ({@code data/odteste_gatilhos.dat} do overworld): o que cada grupo já passou nos gatilhos. */
    public static final String ARQUIVO = "odteste_gatilhos";

    /** Chaves de fora do arquivo: versão do formato e grupo → estado. */
    public static final String CHAVE_VERSAO = "versao";
    public static final String CHAVE_GRUPOS = "grupos";
    /** Chaves do estado de cada grupo (EstadoGrupo). */
    public static final List<String> CHAVES_GRUPO =
            List.of("ja_teve", "armado", "livro_lido", "noites", "ultima_noite", "avisado", "falha_avisada");

    /** Arquivo de config do servidor ({@code serverconfig/} do mundo). */
    public static final String ARQUIVO_CONFIG = "odteste-gatilhos-server.toml";
    /** Seção e chaves da config. */
    public static final String SECAO_CONFIG = "gatilhos";
    public static final List<String> CHAVES_CONFIG =
            List.of("primeiro_sinal_automatico", "neblina_precisa_chuva", "aviso_sino_ligado", "noites_aviso_sino");

    /** Id do Livro do Sineiro (já existe em docs/ids.md; aqui só é lido). */
    public static final String LIVRO_SINEIRO = "sineiro";

    private IdsGatilhos() {}
}
