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
    /** Desde 14/11/2026: os jogadores podem pular as cenas de câmera (segurando o pulo). */
    public static final String CHAVE_PULAR_CENAS = "pular_cenas";
    public static final List<String> CHAVES_CONFIG = List.of(CHAVE_MESTRES, CHAVE_OUVIR_PADRAO, CHAVE_PULAR_CENAS);

    private IdsMestre() {}
}
