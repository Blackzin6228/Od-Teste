package com.nikolas.odteste.historia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Números e contas do Ato 1, parte A (Cena 1 "Primeiro Sinal" e Enigma 1 "O Sino da Vila"). Java puro,
 * testado em RegrasAto1Test. Os números são proposta para o play test: mudam num lugar só.
 */
public final class RegrasAto1 {
    /** A palavra que o sino toca em Morse e que destrava o baú (lore/roteiro.md, Enigma 1). */
    public static final String PALAVRA = "FERRO";

    /** Ticks por unidade de Morse: normal e lenta (Exposição Média, "Marcado", para cima). */
    public static final int UNIDADE_NORMAL = 8;
    public static final int UNIDADE_LENTA = 14;
    /** O bloco de notas responde ao sino logo depois de cada toque. */
    public static final int ATRASO_NOTA = 3;
    /** Silêncio entre uma repetição e a próxima (ticks). */
    public static final int PAUSA_ENTRE_REPETICOES = 160;

    /** Até onde o sino se ouve (blocos). Fora disso, o jogador não recebe nada. */
    public static final double RAIO_OUVIR = 96;
    /** Abaixo disso, o som sai do próprio sino; acima, de um ponto perto do jogador, na direção do sino. */
    public static final double RAIO_SOM_DIRETO = 14;
    /** "De perto": quem ouve a sequência inteira a até essa distância perde Lucidez. */
    public static final double RAIO_PERTO = 12;
    /** Quanto a sequência inteira de perto tira de Lucidez, e de quanto em quanto tempo no máximo. */
    public static final int PERDA_LUCIDEZ_SEQUENCIA = 4;
    public static final long ESPERA_PERDA_TICKS = 20L * 60 * 5;

    /** Aldeões param olhando o sino quando há jogador a até essa distância; são procurados nesse raio. */
    public static final double RAIO_ALDEOES = 40;
    /** No máximo tantos aldeões congelados por vila (proteção contra vila enorme). */
    public static final int MAX_ALDEOES = 32;
    /** A cama manchada solta partícula só com jogador a até essa distância. */
    public static final double RAIO_CAMA = 24;
    /** A placa é lida só com jogador a até essa distância. */
    public static final double RAIO_PLACA = 16;
    /** Busca de vila (sino) em volta do grupo ou do ponto do admin. */
    public static final int RAIO_BUSCA_VILA = 192;
    public static final int RAIO_BUSCA_VILA_PONTO = 48;
    /** Busca de parede para o estandarte em volta do sino. */
    public static final int RAIO_ESTANDARTE = 24;

    /** Volumes e tons (regra: só sons vanilla, volume 0,5 a 0,7, tom 0,5 a 0,7). */
    public static final float VOLUME_PERTO = 0.7f;
    public static final float VOLUME_LONGE = 0.5f;
    public static final float TOM_PONTO = 0.7f;
    public static final float TOM_TRACO = 0.5f;

    /** Quantas frases existem para as trocas que viraram frase (chaves odteste.ato1.troca.1..N). */
    public static final int FRASES_TROCA = 4;

    /** De onde sai o som de um toque. */
    public enum Fonte { SINO, NOTA }

    /** Um som da sequência: em que tick (contado do começo), de onde sai e se é traço. */
    public record Som(int tick, Fonte fonte, boolean traco) {}

    private RegrasAto1() {}

    /** Exposição Média (Marcado, 40+) ou mais deixa o sino mais lento: ponto e traço ficam fáceis de separar. */
    public static int unidade(boolean exposicaoMediaOuMais) {
        return exposicaoMediaOuMais ? UNIDADE_LENTA : UNIDADE_NORMAL;
    }

    /** Todos os sons de uma repetição, em ordem de tick: o sino toca e o bloco de notas responde. */
    public static List<Som> sons(String palavra, int unidade) {
        Morse.Sequencia s = Morse.sequencia(palavra);
        List<Som> sons = new ArrayList<>();
        for (Morse.Toque t : s.toques()) {
            int tick = t.inicio() * unidade;
            sons.add(new Som(tick, Fonte.SINO, t.traco()));
            sons.add(new Som(tick + ATRASO_NOTA, Fonte.NOTA, t.traco()));
        }
        sons.sort(Comparator.comparingInt(Som::tick));
        return List.copyOf(sons);
    }

    /** Ticks de uma repetição inteira, com a pausa depois dela. */
    public static int duracaoComPausa(String palavra, int unidade) {
        return Morse.sequencia(palavra).duracao() * unidade + PAUSA_ENTRE_REPETICOES;
    }

    /**
     * A placa diz a palavra? Junta as linhas (frente e verso), tira acento, espaço e pontuação, ignora
     * maiúscula. "ferro", "F E R R O" e "Ferro." valem; "FERROS" não.
     */
    public static boolean placaCerta(List<String> linhas, String palavra) {
        return juntar(linhas).equals(Morse.limpar(palavra).replace(" ", ""));
    }

    /** O que está escrito na placa, só letras e números, em maiúsculas. Vazio se a placa está em branco. */
    public static String juntar(List<String> linhas) {
        StringBuilder sb = new StringBuilder();
        if (linhas != null) {
            for (String l : linhas) sb.append(Morse.limpar(l).replace(" ", ""));
        }
        return sb.toString();
    }

    /** A sequência foi ouvida inteira de perto e já passou a espera desde a última perda? */
    public static boolean perdeLucidez(boolean ouviuTudoDePerto, long agora, long ultimaPerda) {
        return ouviuTudoDePerto && (ultimaPerda < 0 || agora - ultimaPerda >= ESPERA_PERDA_TICKS || agora < ultimaPerda);
    }

    /** Qual frase de troca mostrar (1 a FRASES_TROCA), nunca a mesma duas vezes seguidas. */
    public static int fraseTroca(int ultima, int sorteio) {
        if (ultima < 1 || ultima > FRASES_TROCA) return Math.floorMod(sorteio, FRASES_TROCA) + 1;
        int n = Math.floorMod(sorteio, FRASES_TROCA - 1) + 1; // 1..N-1, pulando a última
        return n >= ultima ? n + 1 : n;
    }
}
