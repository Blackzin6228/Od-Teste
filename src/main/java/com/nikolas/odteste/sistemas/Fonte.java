package com.nikolas.odteste.sistemas;

import java.util.Locale;
import java.util.Optional;

/**
 * De onde vem a Exposição: só **contato com o paranormal** (decisão do Nikolas, 08/11/2026).
 * Escuro, morte comum e mob vanilla **não** contam.
 *
 * Cada fonte dá um pouco, em centésimos de ponto (100 = 1 ponto de Exposição), multiplicado pela intensidade
 * que quem chama passa. Repetir a mesma fonte rende menos (ver RegrasExposicao).
 * Ainda não há criaturas nem relíquias: o conteúdo chama {@link Exposicao#contato} quando existir.
 * Os números são proposta para o play test: mudar aqui muda o ritmo do jogo inteiro.
 */
public enum Fonte {
    /** Ver de perto uma criatura do mod. */
    CRIATURA_VISTA("criatura_vista", 150),
    /** Lutar com uma criatura do mod (bater ou apanhar). */
    CRIATURA_LUTA("criatura_luta", 40),
    /** Matar uma criatura do mod. */
    CRIATURA_MORTA("criatura_morta", 100),
    /** Pegar uma relíquia menor. */
    RELIQUIA_PEGAR("reliquia_pegar", 100),
    /** Usar uma relíquia menor. */
    RELIQUIA_USAR("reliquia_usar", 50),
    /** Fazer um ritual. */
    RITUAL("ritual", 200),
    /** Tempo numa dimensão de elemento (o conteúdo chama a cada minuto lá dentro). */
    DIMENSAO("dimensao", 50),
    /** Uma aparição (evento do paranormal ou do narrador). */
    APARICAO("aparicao", 300);

    private final String id;
    /** Ganho da primeira vez com intensidade 1, em centésimos de ponto. */
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
