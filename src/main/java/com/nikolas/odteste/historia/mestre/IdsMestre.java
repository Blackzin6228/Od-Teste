package com.nikolas.odteste.historia.mestre;

import java.util.List;

/** Nomes da config do mestre, que ficam no arquivo do servidor e por isso nunca mudam (docs/ids.md). Java puro. */
public final class IdsMestre {
    /** Arquivo de config do servidor ({@code serverconfig/} do mundo). */
    public static final String ARQUIVO_CONFIG = "odteste-mestre-server.toml";
    /** Seção e chaves da config. */
    public static final String SECAO_CONFIG = "mestre";
    public static final String CHAVE_MESTRES = "mestres";
    public static final String CHAVE_OUVIR_PADRAO = "ouvir_padrao";
    public static final List<String> CHAVES_CONFIG = List.of(CHAVE_MESTRES, CHAVE_OUVIR_PADRAO);

    private IdsMestre() {}
}
