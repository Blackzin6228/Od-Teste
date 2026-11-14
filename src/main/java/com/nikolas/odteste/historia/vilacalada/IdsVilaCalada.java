package com.nikolas.odteste.historia.vilacalada;

import java.util.List;

/**
 * Textos da Vila Calada que ficam salvos no mundo e por isso nunca mudam (entram em docs/ids.md).
 * Java puro.
 */
public final class IdsVilaCalada {
    /** Lugar ancorado (dentro de {@code ancoras} do arquivo {@code odteste_historia}): para onde as pistas apontam. */
    public static final String LUGAR_VILA_CALADA = "vila_calada";

    /** Marco do grupo: alguém do grupo chegou à Vila Calada. As pistas de longe param para esse grupo. */
    public static final String MARCO_CHEGOU = "vila_calada";

    /** Arquivo próprio ({@code data/odteste_vila_calada.dat} do overworld): liga/desliga e a colocação que ancorou. */
    public static final String ARQUIVO = "odteste_vila_calada";

    /** Chaves dentro do arquivo. */
    public static final List<String> CHAVES = List.of("ligado", "colocacao", "ancora_auto", "ancora_auto_dim", "vistas", "preparados", "dobre");
    /** Chaves de cada grupo dentro de {@code dobre}. */
    public static final List<String> CHAVES_DOBRE = List.of("noite_gasta", "erros_conta", "noites_sem_tentar",
            "ultima_noite_contada", "outra_coisa", "empurroes", "feito_dia");

    /** Marco do grupo: o dobre foi feito (os Famintos se ajoelharam, a vila se calou). */
    public static final String MARCO_DOBRE = "dobre";
    /** Tag de criatura que ouve o dobre (para no toque, sobe a rua no chamado, ajoelha no fim). */
    public static final String TAG_OUVE_DOBRE = "ouve_o_dobre";
    /** Modificador de velocidade das criaturas paradas pelo dobre (temporário, nunca salvo). */
    public static final String MODIFICADOR_DOBRE = "dobre_parado";
    /** Pacote servidor → cliente: o Pendurado aparece em cima do sino (empurrão do mestre). Nada salvo. */
    public static final String PACOTE_PENDURADO_SINO = "pendurado_sino";
    /** Lugar que o admin pode ancorar: de onde vem o rangido do Moinho Parado (sem âncora: do noroeste). */
    public static final String LUGAR_MOINHO = "moinho_parado";

    /**
     * Modificador de velocidade dos aldeões virados para a Vila Calada ({@code odteste:aldeao_virado}).
     * Temporário: o jogo nunca salva no disco, então nenhum aldeão fica preso se o mod sair do pack.
     */
    public static final String MODIFICADOR_ALDEAO_VIRADO = "aldeao_virado";

    /** Template da Vila Calada que ancora sozinho ao terminar de ser colocado (nome exato). */
    public static final String TEMPLATE = "odteste:vila_calada";

    /** Pacote servidor → cliente {@code odteste:clima_vila}: está dentro do muro? vê o Pendurado? Nada salvo. */
    public static final String PACOTE_CLIMA = "clima_vila";

    private IdsVilaCalada() {}
}
