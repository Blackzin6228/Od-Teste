package com.nikolas.odteste.blocos.capela;

import java.util.List;

/**
 * IDs dos blocos da Vila Calada (Java puro, para o teste ler sem carregar o Minecraft). Para sempre: docs/ids.md.
 * O bloco com dados da vela usa o mesmo nome da vela.
 */
public final class BlocosCapelaIds {
    public static final String VELA = "vela_da_capela";
    public static final String POCA = "poca_de_sangue";
    public static final List<String> IDS = List.of(VELA, POCA);

    private BlocosCapelaIds() {}
}
