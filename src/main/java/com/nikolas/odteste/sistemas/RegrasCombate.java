package com.nikolas.odteste.sistemas;

/**
 * Balanceamento tech x paranormal (docs/decisoes/2026-11-13-balanceamento-tech.md, estudo seção 2).
 * Java puro, testado em RegrasCombateTest. Os números são ponto de partida: ajuste no playtest.
 *
 * - Arma nossa: no máximo {@link #TETO_ARMA} por golpe (abaixo da Meka-Tool do pack, 24).
 * - Contra criatura nossa, dano paranormal vale {@link #MULT_PARANORMAL}x; dano comum perde a resistência da
 *   criatura (comum 0%, rara 30%, chefe 60%). O técnico precisa do mago.
 * - Traje A4/A5: segura 15%/25% do dano que ignora armadura (de outros mods) e 50%/70% do dano paranormal.
 *   O resto (dano comum) fica com a armadura e a Reserva.
 */
public final class RegrasCombate {
    /** Teto do golpe das nossas armas (relíquias e espadas), contra qualquer alvo. */
    public static final float TETO_ARMA = 20f;
    /** Dano paranormal contra criatura nossa. */
    public static final float MULT_PARANORMAL = 2f;

    /** Quanto a criatura nossa resiste a dano comum (não paranormal). */
    public enum Raridade {
        COMUM(0),
        RARA(30),
        CHEFE(60);

        /** Resistência a dano comum, em %. */
        public final int resistencia;

        Raridade(int resistencia) {
            this.resistencia = resistencia;
        }
    }

    /** Traje A4: dano que ignora armadura (outros mods) e dano paranormal, em %. */
    public static final int A4_IGNORA_ARMADURA = 15;
    public static final int A4_PARANORMAL = 50;
    /** Traje A5. */
    public static final int A5_IGNORA_ARMADURA = 25;
    public static final int A5_PARANORMAL = 70;

    private RegrasCombate() {}

    /** Golpe de arma nossa: nunca passa do teto. Dano negativo ou NaN vira 0. */
    public static float golpeDeArma(float dano) {
        if (!(dano > 0)) return 0f;
        return Math.min(dano, TETO_ARMA);
    }

    /** Dano que chega na criatura nossa: paranormal dobra; comum perde a resistência dela. */
    public static float contraCriatura(float dano, boolean paranormal, Raridade raridade) {
        if (!(dano > 0)) return 0f;
        if (paranormal) return dano * MULT_PARANORMAL;
        int r = raridade == null ? 0 : raridade.resistencia;
        return dano * (100 - r) / 100f;
    }

    /**
     * Quanto (em %) o traje segura antes da conta da armadura.
     * @param grau degrau da armadura (só 4 e 5 seguram algo aqui; abaixo disso, 0)
     * @param paranormal o dano é paranormal (tag ou criatura nossa)
     * @param ignoraArmadura o dano ignora armadura (tag {@code minecraft:bypasses_armor})
     */
    public static int absorcaoTraje(int grau, boolean paranormal, boolean ignoraArmadura) {
        if (grau < 4) return 0;
        boolean a5 = grau >= 5;
        if (paranormal) return a5 ? A5_PARANORMAL : A4_PARANORMAL;
        if (ignoraArmadura) return a5 ? A5_IGNORA_ARMADURA : A4_IGNORA_ARMADURA;
        return 0;
    }

    /** Dano depois do traje. */
    public static float depoisDoTraje(float dano, int absorcao) {
        if (!(dano > 0)) return 0f;
        int a = Math.max(0, Math.min(100, absorcao));
        return dano * (100 - a) / 100f;
    }
}
