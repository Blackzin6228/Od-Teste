package com.nikolas.odteste.sistemas;

import java.util.Locale;
import java.util.Optional;

/**
 * De onde vem a Exposição. Cada fonte dá um pouco, em centésimos de ponto (100 = 1 ponto de Exposição).
 * Repetir a mesma fonte rende menos (ver RegrasExposicao).
 *
 * As quatro primeiras já disparam sozinhas no jogo. As outras são para criaturas, relíquias, ritos e
 * Páginas chamarem {@link Exposicao#ganhar} quando existirem. Os números são proposta para o play test:
 * mudar aqui muda o ritmo do jogo inteiro.
 */
public enum Fonte {
    /** 60 s seguidos no escuro total, sem outro jogador por perto. */
    ESCURO("escuro", 50),
    /** Morrer. */
    MORRER("morrer", 200),
    /** Mob hostil a poucos blocos, à noite. */
    MOB_NOTURNO("mob_noturno", 25),
    /** Lucidez chegou a 0. */
    SURTO("surto", 300),

    // ---------- para o conteúdo que vem depois ----------
    CRIATURA_VISTA("criatura_vista", 150),
    CRIATURA_MORTA("criatura_morta", 100),
    RELIQUIA("reliquia", 50),
    RITO("rito", 200),
    PAGINA("pagina", 100),
    LUGAR("lugar", 50);

    private final String id;
    /** Ganho da primeira vez, em centésimos de ponto. */
    public final int base;

    Fonte(String id, int base) {
        this.id = id;
        this.base = base;
    }

    public String id() {
        return id;
    }

    public static Optional<Fonte> deId(String id) {
        if (id == null) return Optional.empty();
        String limpo = id.trim().toLowerCase(Locale.ROOT);
        for (Fonte f : values()) {
            if (f.id.equals(limpo)) return Optional.of(f);
        }
        return Optional.empty();
    }
}
