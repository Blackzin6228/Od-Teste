package com.nikolas.odteste.sistemas;

import java.util.Locale;
import java.util.Optional;

/**
 * O que derruba a Lucidez além do dano (decisão do Nikolas, 08/11/2026). O conteúdo chama
 * {@link Lucidez#abalar} quando existir. Valor em pontos de Lucidez com intensidade 1.
 * O dano já está ligado sozinho (Vigia), com pouco peso para dano comum e mais para dano paranormal.
 */
public enum Abalo {
    /** Cada golpe numa batalha com relíquia (usar ou enfrentar uma). */
    BATALHA_RELIQUIA("batalha_reliquia", 3),
    /** Uma aparição. */
    APARICAO("aparicao", 10),
    /** Enfrentar um mob de Sangue (ver de perto ou começar a luta). */
    MOB_SANGUE("mob_sangue", 4);

    private final String id;
    public final int base;

    Abalo(String id, int base) {
        this.id = id;
        this.base = base;
    }

    public String id() {
        return id;
    }

    /** Pontos perdidos com a intensidade (0 a 5; fora disso é preso). */
    public int perda(float intensidade) {
        if (!(intensidade > 0)) return 0;
        return Math.max(1, Math.round(base * Math.min(5f, intensidade)));
    }

    public static Optional<Abalo> deId(String id) {
        if (id == null) return Optional.empty();
        String limpo = id.trim().toLowerCase(Locale.ROOT);
        for (Abalo a : values()) {
            if (a.id.equals(limpo)) return Optional.of(a);
        }
        return Optional.empty();
    }
}
