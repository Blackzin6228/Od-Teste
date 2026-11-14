package com.nikolas.odteste.historia.vilacalada;

import java.util.List;
import java.util.Locale;

/**
 * Onde ficam as peças da Vila Calada v2 dentro da estrutura (cópia de
 * {@code arte/vila-calada-150-v2/vila_calada_150_v2.pistas.json}; o teste confere que bate). Posições relativas ao
 * canto noroeste de baixo, sem giro. Java puro, testado em RegrasPrepararVilaTest.
 *
 * Usado pelo {@code /odteste vila_calada preparar}: a vila colocada à mão ({@code /place template}) sai sem o livro,
 * a comida do poço, a vela e as poças, porque o template não guarda conteúdo de bloco nem bloco do mod.
 */
public final class RegrasPrepararVila {
    /** Uma peça: nome curto (aparece para o mestre) e posição relativa ao canto. */
    public record Peca(String nome, int x, int y, int z) {}

    /** Livro do Sineiro: atril no alto do campanário. */
    public static final Peca ATRIL = new Peca("atril", 87, 18, 56);
    /** Sino rachado (serve para achar o canto: o sino é um ponto de encontro dos aldeões, o jogo o indexa). */
    public static final Peca SINO = new Peca("sino", 85, 19, 57);
    /** A única vela acesa, na mesa da capela: vira {@code odteste:vela_da_capela} (refúgio). */
    public static final Peca VELA = new Peca("vela", 74, 4, 46);
    /** Poças que pulsam: três na praça e uma no curral (ficha: "na praça e no curral"). */
    public static final List<Peca> POCAS = List.of(
            new Peca("poca_praca_1", 68, 1, 74), new Peca("poca_praca_2", 56, 1, 62),
            new Peca("poca_praca_3", 41, 1, 60), new Peca("poca_curral", 45, 1, 43));
    /** Nicho no fundo do poço: baú de mantimentos (pão, maçã). */
    public static final Peca BAU_POCO = new Peca("bau_poco", 74, 0, 78);
    /** Placa na beira do poço: "Aqui ele ouvia." */
    public static final Peca PLACA_POCO = new Peca("placa_poco", 72, 3, 83);

    /** O que vai no baú do poço (um fôlego, não um estoque). */
    public static final int PAES = 4;
    public static final int MACAS = 3;
    /** Espaços do baú (meio da fileira do meio). */
    public static final int ESPACO_PAO = 12;
    public static final int ESPACO_MACA = 14;

    /** Procura o sino rachado até essa distância da âncora (a âncora fica perto da praça). */
    public static final int RAIO_PROCURA_SINO = 112;

    private RegrasPrepararVila() {}

    /** Canto da estrutura a partir de onde um ponto conhecido está no mundo: {@code mundo - relativo}. */
    public static int[] canto(int mx, int my, int mz, Peca conhecida) {
        return new int[] {mx - conhecida.x(), my - conhecida.y(), mz - conhecida.z()};
    }

    /** Poça pode entrar onde há ar ou o pó de redstone que o template pôs de enfeite (nunca em outra coisa). */
    public static boolean pocaPodeEntrar(String idBloco) {
        return ar(idBloco) || "minecraft:redstone_wire".equals(idBloco);
    }

    /** A vela do mod pode entrar onde há ar ou uma vela comum do jogo (a do template). */
    public static boolean velaPodeEntrar(String idBloco) {
        if (ar(idBloco)) return true;
        if (idBloco == null) return false;
        String s = idBloco.toLowerCase(Locale.ROOT);
        return s.startsWith("minecraft:") && s.endsWith("candle");
    }

    /** Ar de verdade (os três ares do jogo). */
    public static boolean ar(String idBloco) {
        return "minecraft:air".equals(idBloco) || "minecraft:cave_air".equals(idBloco) || "minecraft:void_air".equals(idBloco);
    }

    /** Chave do que já foi preparado uma vez (fica salvo): o baú do poço não enche de novo se o grupo esvaziou. */
    public static String chave(long canto, Peca p) {
        return canto + ":" + p.nome();
    }
}
