package com.nikolas.odteste.criaturas;

/**
 * "Cansaço do elemento" (decisão de 13/11/2026, estudo 3.3): contra grind de matéria.
 * Cada jogador conta as matérias brutas que ganhou no **dia de jogo** (20 min reais, pelo relógio do mundo
 * {@code gameTime}, que dormir e {@code /time set} não mexem; nem acelerador de bloco).
 * Até {@link #LIMIAR_1} no dia: cada unidade cai sempre. Depois: metade. Depois de {@link #LIMIAR_2}: um quarto.
 * Sem número na tela: só uma frase quando o outro lado "fica quieto". Java puro, testado em RegrasMateriaTest.
 */
public final class RegrasCansaco {
    public static final long TICKS_DIA = 24000L;
    public static final int LIMIAR_1 = 8;
    public static final int LIMIAR_2 = 16;
    /** Teto do contador salvo (nunca cresce sem limite). */
    public static final int TETO_CONTADOR = 10_000;

    private RegrasCansaco() {}

    public static long dia(long tempoDoMundo) {
        return Math.floorDiv(tempoDoMundo, TICKS_DIA);
    }

    /** Brutas que contam hoje: o contador salvo só vale se for do mesmo dia. */
    public static int brutasHoje(long diaSalvo, int brutasSalvas, long diaAgora) {
        return diaSalvo == diaAgora ? Math.max(0, Math.min(TETO_CONTADOR, brutasSalvas)) : 0;
    }

    /** Chance de uma unidade cair, em quartos (4 = sempre, 2 = metade, 1 = um quarto). */
    public static int chanceEmQuartos(int brutasHoje) {
        if (brutasHoje < LIMIAR_1) return 4;
        if (brutasHoje < LIMIAR_2) return 2;
        return 1;
    }

    /** A unidade cai? {@code sorteio} de 0 a 3. */
    public static boolean cai(int brutasHoje, int sorteio) {
        return sorteio < chanceEmQuartos(brutasHoje);
    }

    /** O que fazer com a matéria bruta do drop. */
    public enum Destino {
        /** Jogador de verdade: passa pelo sorteio do dia e soma no cansaço dele. */
        CONTA,
        /** Máquina (FakePlayer) ou ninguém: a matéria bruta sai do drop. */
        TIRA
    }

    /**
     * Quem leva o crédito do drop: o mesmo jogador que o loot usa no {@code killed_by_player} (o último que bateu,
     * enquanto o golpe ainda vale, {@code lastHurtByPlayerTime > 0}). Não é quem deu o golpe final: se a criatura
     * morre de fogo, sangramento, lava ou queda depois do golpe, a matéria cai e o cansaço é de quem bateu.
     */
    public static <J> J jogadorDoDrop(J ultimoQueBateu, int tempoDoGolpe) {
        return tempoDoGolpe > 0 ? ultimoQueBateu : null;
    }

    /** Matéria só para jogador de verdade (decisão de 13/11/2026). */
    public static Destino destino(boolean temJogador, boolean jogadorFalso) {
        return temJogador && !jogadorFalso ? Destino.CONTA : Destino.TIRA;
    }

    /** Frase a mostrar quando o contador passa de um limiar: 0 nenhuma, 1 primeira, 2 segunda. */
    public static int aviso(int antes, int depois) {
        if (antes < LIMIAR_2 && depois >= LIMIAR_2) return 2;
        if (antes < LIMIAR_1 && depois >= LIMIAR_1) return 1;
        return 0;
    }
}
