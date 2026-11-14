package com.nikolas.odteste.horror;

import java.util.ArrayList;
import java.util.List;

/**
 * Os sustos grandes. O id vai no pacote e no comando {@code /odteste susto <tipo> <jogador>}: não muda.
 * Cada um começa com tensão (silêncio, um som pequeno) e termina num momento forte. Tudo só no cliente de quem leva.
 */
public enum TipoSusto {
    /** Passos atrás que acompanham você e param quando você vira. Depois, algo pesado bem nas suas costas. */
    PASSOS("passos"),
    /** Uma porta perto range devagar... e bate com força, várias vezes. */
    PORTA("porta"),
    /** A luz falha: tocha e vela parecem apagar (só na sua tela) e algo respira no escuro. */
    LUZ("luz"),
    /** Um sussurro de um lado só, colado no ouvido. Depois, o seu nome. */
    SUSSURRO("sussurro"),
    /** Tudo fica mudo de repente. Muito tempo depois, um sino grave. */
    SILENCIO("silencio");

    public final String id;

    TipoSusto(String id) {
        this.id = id;
    }

    /** Pelo id; null se não existe. */
    public static TipoSusto de(String id) {
        for (TipoSusto t : values()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    public static List<String> ids() {
        List<String> r = new ArrayList<>();
        for (TipoSusto t : values()) r.add(t.id);
        return r;
    }
}
