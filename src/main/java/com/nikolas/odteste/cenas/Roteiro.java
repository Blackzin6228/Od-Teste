package com.nikolas.odteste.cenas;

import java.util.List;

/**
 * Uma cena de câmera escrita em JSON ({@code data/<mod>/cenas/<id>.json}, lida por {@link LeitorRoteiro}). Java puro:
 * só números e nomes. O servidor lê o arquivo, confere e manda o texto inteiro no pacote; o cliente lê de novo e toca.
 * Formato completo em {@code docs/cenas.md}.
 *
 * Tempos em ticks (20 por segundo), contados do começo da câmera.
 *
 * @param id             nome da cena (o nome do arquivo; fica salvo em {@code odteste:cenas_vistas})
 * @param duracao        tick em que a câmera volta ao jogador
 * @param ancora         onde a cena acontece: {@code jogador} (onde o alvo está, olhando para onde ele olha) ou o
 *                       nome de um lugar que o servidor sabe achar (ex.: {@code sino_rachado})
 * @param pular          a cena deixa pular (segurando o pulo); também precisa do {@code mestre.pular_cenas}
 * @param barras         altura de cada barra de cinema (fração da tela, 0 = sem barras)
 * @param saidaDireta    {@code true} = no fim a câmera volta direto aos olhos do jogador, sem preto (a última chave
 *                       deve ser os próprios olhos); {@code false} = escurece e volta
 * @param mostrarJogador o boneco do próprio jogador aparece na cena (para a câmera poder olhar para ele)
 * @param chaves         pontos de câmera, em ordem de tempo; o primeiro no tick 0
 * @param eventos        sons, preto/vermelho, textos, tremor e efeitos de tela, por tempo
 */
public record Roteiro(String id, int duracao, String ancora, boolean pular, float barras, boolean saidaDireta,
        boolean mostrarJogador, List<Chave> chaves, List<Evento> eventos) {

    public Roteiro {
        chaves = List.copyOf(chaves);
        eventos = List.copyOf(eventos);
    }

    /** Em relação a quê a posição (e o olhar) de uma chave é medida. */
    public enum Referencia {
        /** A âncora da cena (x direita, y cima, z frente, pelo giro da âncora). */
        ANCORA("ancora"),
        /** Os olhos do alvo, com os eixos da âncora (o caminho fica igual para qualquer alvo). */
        JOGADOR("jogador"),
        /** Ponto entre os olhos do alvo (0) e a âncora (1), com os eixos da âncora. Usa {@link Chave#entre}. */
        ENTRE("entre"),
        /** Os olhos do alvo e para onde ele olha: {@code [0,0,0]} com giro 0 e inclinação 0 = a visão dele. */
        OLHOS("olhos");

        public final String nome;

        Referencia(String nome) {
            this.nome = nome;
        }

        public static Referencia de(String s) {
            for (Referencia r : values()) {
                if (r.nome.equals(s)) return r;
            }
            return null;
        }
    }

    /** Forma do caminho entre esta chave e a próxima. */
    public enum Curva {
        /** Curva suave que passa por todos os pontos (Catmull-Rom), sem tranco entre as chaves. */
        CATMULL("catmull"),
        /** Reta. */
        LINEAR("linear"),
        /** Fica parada nesta chave até a próxima e então pula (corte seco). */
        CORTE("corte");

        public final String nome;

        Curva(String nome) {
            this.nome = nome;
        }

        public static Curva de(String s) {
            for (Curva c : values()) {
                if (c.nome.equals(s)) return c;
            }
            return null;
        }
    }

    /** Ritmo do tempo entre esta chave e a próxima (o caminho é o mesmo; muda a velocidade). */
    public enum Ritmo {
        LINEAR("linear"),
        /** Começa e termina devagar. */
        SUAVE("suave"),
        /** Começa devagar. */
        ENTRA("entra"),
        /** Termina devagar. */
        SAI("sai");

        public final String nome;

        Ritmo(String nome) {
            this.nome = nome;
        }

        public static Ritmo de(String s) {
            for (Ritmo r : values()) {
                if (r.nome.equals(s)) return r;
            }
            return null;
        }
    }

    /**
     * Um ponto de câmera.
     *
     * @param t          tick
     * @param ref        referência da posição
     * @param entre      só em {@link Referencia#ENTRE}: 0 = olhos do alvo, 1 = âncora
     * @param pos        posição na referência
     * @param olhar      para onde olha: {@link Olhar}
     * @param fov        campo de visão em graus ({@code NaN} = o do jogador)
     * @param roll       inclinação lateral da câmera (graus; positivo = gira para a direita)
     * @param curva      caminho até a próxima chave
     * @param ritmo      ritmo até a próxima chave
     */
    public record Chave(int t, Referencia ref, double entre, Cena.Local pos, Olhar olhar, float fov, float roll,
            Curva curva, Ritmo ritmo) {}

    /**
     * Para onde a câmera olha numa chave. Um dos três jeitos:
     * <ul>
     * <li>{@code ponto != null}: olha para um ponto (na referência {@code refPonto});</li>
     * <li>{@code paraJogador}: olha para os olhos do alvo;</li>
     * <li>senão: giro e inclinação em graus. Giro somado ao da âncora (ou ao do alvo, em {@code olhos});
     * inclinação somada à do alvo só em {@code olhos}.</li>
     * </ul>
     */
    public record Olhar(Cena.Local ponto, Referencia refPonto, double entre, boolean paraJogador, float giro,
            float inclinacao) {
        public static Olhar angulos(float giro, float inclinacao) {
            return new Olhar(null, null, 0, false, giro, inclinacao);
        }
    }

    /** Algo que acontece num tick. */
    public sealed interface Evento permits Som, Fade, Texto, Tremor, Fx, PararSons {
        int t();
    }

    /** Som vanilla (ou de mod), tocado "na cabeça" (sem origem). */
    public record Som(int t, String som, float volume, float tom) implements Evento {}

    /**
     * A tela vai para {@code ate} (0 = some, 1 = cobre tudo) em {@code duracao} ticks, na cor {@code cor} (RGB;
     * {@code -1} = a cor do fade anterior, preto se for o primeiro). {@code de < 0} = parte de onde estava.
     */
    public record Fade(int t, int cor, float de, float ate, int duracao) implements Evento {}

    /** Texto curto no meio da tela (chave de tradução). {@code linha} 0 = grande, 1 = menor, embaixo. */
    public record Texto(int t, String chave, int linha, int duracao) implements Evento {}

    /** A câmera treme ({@code forca} 1 = forte), sumindo até o fim da duração. Diminui com "efeitos fortes". */
    public record Tremor(int t, float forca, int duracao) implements Evento {}

    /** Liga um efeito de tela do mod ({@code /odfx}: sangue, coracao, carmesim, marcado, cor_horror). */
    public record Fx(int t, String efeito, int segundos) implements Evento {}

    /** Para todos os sons da cena que ainda tocam. */
    public record PararSons(int t) implements Evento {}
}
