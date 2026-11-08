package com.nikolas.odteste.sistemas;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Decisões das alucinações (o que o cliente toca sozinho, ninguém mais vê). Java puro, testado em RegrasHorrorTest.
 *
 * - O "nível de horror" (0 a 4) junta a faixa de Lucidez e o degrau de Exposição.
 * - Uma alucinação a cada intervalo sorteado, que encurta com o nível; nunca menos de 20 s entre duas.
 * - A mesma alucinação não se repete antes de 2 min.
 * - Sem "efeitos fortes", só sobra o que é som ou vinheta.
 */
public final class RegrasHorror {
    /** Nível máximo. */
    public static final int NIVEL_MAX = 4;
    /** Nunca duas alucinações com menos de 20 s entre elas (em ticks). */
    public static final int INTERVALO_MINIMO = 20 * 20;
    /** A mesma alucinação só volta depois de 2 min. */
    public static final int COOLDOWN_MESMA = 20 * 120;
    /** Intervalo sorteado por nível, em segundos: [mínimo, máximo]. Índice = nível. */
    private static final int[][] INTERVALO_S = {{0, 0}, {120, 240}, {60, 140}, {35, 80}, {20, 50}};

    /** Gerador de sorteio: devolve de 0 até {@code limite - 1}. No jogo é o RandomSource; no teste, java.util.Random. */
    @FunctionalInterface
    public interface Sorteio {
        int ate(int limite);
    }

    /**
     * Cada alucinação: nível mínimo para aparecer e se é visual (some quando "efeitos fortes" está desligado).
     * Os nomes não são salvos: podem mudar.
     */
    public enum Alucinacao {
        /** Sussurro grave atrás de você, com eco. */
        SUSSURRO(1, false),
        /** Cisco de poeira passando no canto da visão. */
        CISCO(1, true),
        /** Passos atrás de você, que param quando você vira. */
        PASSOS(2, false),
        /** O próprio coração batendo alto. */
        CORACAO(2, false),
        /** Gotas, faíscas ou cinza caindo só para você. */
        PARTICULAS(2, true),
        /** A câmera pende para um lado. */
        INCLINAR(2, true),
        /** Uma porta ou alçapão perto parece abrir sozinho (só na sua tela). */
        PORTA(2, true),
        /** Som de criatura que não está lá. */
        SOM_CRIATURA(3, false),
        /** A tela escurece um instante (sem efeitos fortes, vira vinheta). */
        ESCURECER(3, false),
        /** Vulto parado no canto da visão; some quando você olha. */
        VULTO(3, true),
        /** Criatura parada olhando para você; some quando você chega perto. */
        MOB_FALSO(3, true);

        public final int nivelMinimo;
        public final boolean visual;

        Alucinacao(int nivelMinimo, boolean visual) {
            this.nivelMinimo = nivelMinimo;
            this.visual = visual;
        }
    }

    private RegrasHorror() {}

    // ---------- nível ----------

    /** Peso do degrau de Exposição no nível: Comum 0, Tocado/Marcado 1, Fundo 2, À beira/Atravessado 3. */
    public static int pesoExposicao(Degrau d) {
        if (d == null) return 0;
        return switch (d) {
            case COMUM -> 0;
            case TOCADO, MARCADO -> 1;
            case FUNDO -> 2;
            case A_BEIRA, ATRAVESSADO -> 3;
        };
    }

    /** Peso da Lucidez: a gravidade da faixa (o surto conta como Quebrando). */
    public static int pesoLucidez(FaixaLucidez f) {
        if (f == null) return 0;
        return Math.min(FaixaLucidez.QUEBRANDO.gravidade(), f.gravidade());
    }

    /**
     * Nível de horror: o pior dos dois; se os dois estão pesados (2 ou mais), sobe mais um. De 0 a 4.
     * Ex.: Lucidez Abalada + Exposição Fundo = 3; Quebrando + À beira = 4.
     */
    public static int nivel(FaixaLucidez lucidez, Degrau exposicao) {
        int l = pesoLucidez(lucidez);
        int e = pesoExposicao(exposicao);
        int n = Math.max(l, e) + (Math.min(l, e) >= 2 ? 1 : 0);
        return Math.min(NIVEL_MAX, n);
    }

    // ---------- quando ----------

    /** Ticks até a próxima alucinação no nível (0 = nenhuma). Sempre pelo menos {@link #INTERVALO_MINIMO}. */
    public static int intervalo(int nivel, Sorteio s) {
        if (nivel <= 0) return 0;
        int[] faixa = INTERVALO_S[Math.min(NIVEL_MAX, nivel)];
        int seg = faixa[0] + s.ate(faixa[1] - faixa[0] + 1);
        return Math.max(INTERVALO_MINIMO, seg * 20);
    }

    /**
     * Agenda das alucinações de um jogador (só memória, no cliente). Chamar uma vez por tick:
     * devolve true quando é hora de tocar uma.
     */
    public static final class Agenda {
        private long proxima = -1;
        private long ultima = Long.MIN_VALUE / 2;
        private int nivelAgendado;

        public boolean deveTocar(long agora, int nivel, Sorteio s) {
            if (nivel <= 0) {
                proxima = -1;
                nivelAgendado = 0;
                return false;
            }
            if (proxima < 0 || nivel != nivelAgendado) {
                // Mudou de nível (ou começou): reagenda, mas nunca antes do intervalo mínimo desde a última.
                // Subindo de nível, pode adiantar; descendo, nunca adianta.
                long novo = agora + intervalo(nivel, s);
                if (proxima >= 0) novo = nivel > nivelAgendado ? Math.min(novo, proxima) : Math.max(novo, proxima);
                proxima = Math.max(novo, ultima + INTERVALO_MINIMO);
                nivelAgendado = nivel;
            }
            if (agora < proxima) return false;
            ultima = agora;
            proxima = agora + intervalo(nivel, s);
            return true;
        }

        /** Não deu para tocar agora (outra em andamento, nada válido): tenta de novo em 5 s. */
        public void adiar(long agora) {
            proxima = agora + 100;
        }

        public long proxima() {
            return proxima;
        }

        public void zerar() {
            proxima = -1;
            nivelAgendado = 0;
            ultima = Long.MIN_VALUE / 2;
        }
    }

    // ---------- qual ----------

    /** Quando cada alucinação tocou pela última vez (ticks do cliente). */
    public static final class Recentes {
        private final Map<Alucinacao, Long> ultima = new EnumMap<>(Alucinacao.class);

        public boolean livre(Alucinacao a, long agora) {
            Long t = ultima.get(a);
            return t == null || agora - t >= COOLDOWN_MESMA || agora < t;
        }

        public void marcar(Alucinacao a, long agora) {
            ultima.put(a, agora);
        }

        public void zerar() {
            ultima.clear();
        }
    }

    /** As que podem tocar agora. */
    public static List<Alucinacao> possiveis(int nivel, boolean efeitosFortes, Recentes recentes, long agora) {
        List<Alucinacao> l = new ArrayList<>();
        for (Alucinacao a : Alucinacao.values()) {
            if (a.nivelMinimo > nivel) continue;
            if (a.visual && !efeitosFortes) continue;
            if (recentes != null && !recentes.livre(a, agora)) continue;
            l.add(a);
        }
        return l;
    }

    /** Peso no sorteio: as do próprio nível saem mais (3), as de um nível abaixo 2, as mais leves 1. */
    public static int peso(Alucinacao a, int nivel) {
        int distancia = nivel - a.nivelMinimo;
        if (distancia <= 0) return 3;
        return distancia == 1 ? 2 : 1;
    }

    /** Sorteia uma (ou nenhuma, se nada estiver livre). Não marca: quem toca chama {@link Recentes#marcar}. */
    public static Optional<Alucinacao> escolher(int nivel, boolean efeitosFortes, Recentes recentes, long agora,
                                                Sorteio s) {
        List<Alucinacao> l = possiveis(nivel, efeitosFortes, recentes, agora);
        if (l.isEmpty()) return Optional.empty();
        int total = 0;
        for (Alucinacao a : l) total += peso(a, nivel);
        int x = s.ate(total);
        for (Alucinacao a : l) {
            x -= peso(a, nivel);
            if (x < 0) return Optional.of(a);
        }
        return Optional.of(l.get(l.size() - 1));
    }

    // ---------- vulto e criatura falsa ----------

    /** O vulto some se você olhar (menos de 18° do centro da tela). */
    public static final double VULTO_COS_OLHOU = Math.cos(Math.toRadians(18));
    /** ...se chegar a 4 blocos... */
    public static final double VULTO_PERTO_SQ = 4 * 4;
    /** ...ou depois de 12 s. */
    public static final int VULTO_VIDA = 20 * 12;

    /**
     * @param cosOlhar cosseno do ângulo entre para onde você olha e a direção do vulto (1 = bem no centro)
     */
    public static boolean vultoSome(double cosOlhar, double distanciaSq, int idade) {
        return cosOlhar >= VULTO_COS_OLHOU || distanciaSq <= VULTO_PERTO_SQ || idade >= VULTO_VIDA;
    }

    /** Ângulo do vulto em relação ao olhar, em graus: 35° a 50°, para a esquerda (negativo) ou direita. */
    public static int vultoAngulo(Sorteio s) {
        int graus = 35 + s.ate(16);
        return s.ate(2) == 0 ? -graus : graus;
    }

    /** Distância do vulto: 7 a 11 blocos. */
    public static int vultoDistancia(Sorteio s) {
        return 7 + s.ate(5);
    }

    /** A criatura falsa some se você chegar a 6 blocos... */
    public static final double MOB_PERTO_SQ = 6 * 6;
    /** ...ou depois de 30 s. */
    public static final int MOB_VIDA = 20 * 30;

    public static boolean mobSome(double distanciaSq, int idade) {
        return distanciaSq <= MOB_PERTO_SQ || idade >= MOB_VIDA;
    }

    /** Distância da criatura falsa: 14 a 20 blocos. */
    public static int mobDistancia(Sorteio s) {
        return 14 + s.ate(7);
    }

    // ---------- porta ----------

    /** Busca de porta/alçapão: raio na horizontal e na vertical (pequeno: no máximo 13×7×13 blocos, uma vez). */
    public static final int PORTA_RAIO = 6;
    public static final int PORTA_RAIO_Y = 3;
    /** Nem colada no rosto: pelo menos 3 blocos. */
    public static final double PORTA_MIN_SQ = 3 * 3;

    /** Quanto tempo a porta fica "aberta": 1,5 a 3 s. */
    public static int portaDuracao(Sorteio s) {
        return 30 + s.ate(31);
    }

    // ---------- tela e corpo (contínuos) ----------

    private static final float[] DESSATURAR = {0f, 0f, 0.12f, 0.22f, 0.32f};
    private static final float[] BALANCO = {0f, 0f, 0.35f, 0.7f, 1.1f};
    private static final float[] MAO = {0f, 0f, 0.004f, 0.008f, 0.014f};
    private static final float[] FOV = {0f, 0f, 0f, 0.012f, 0.025f};

    private static int idx(int nivel) {
        return Math.max(0, Math.min(NIVEL_MAX, nivel));
    }

    /** Quanto a tela "lava" a cor (0 a 1). */
    public static float dessaturar(int nivel) {
        return DESSATURAR[idx(nivel)];
    }

    /** Balanço da câmera, em graus. */
    public static float balanco(int nivel) {
        return BALANCO[idx(nivel)];
    }

    /** Tremor da mão, em blocos de deslocamento do modelo da mão. */
    public static float tremorMao(int nivel) {
        return MAO[idx(nivel)];
    }

    /** Distorção leve: a visão "respira" (fração do campo de visão). */
    public static float distorcao(int nivel) {
        return FOV[idx(nivel)];
    }

    /** Pitch dos sons de horror: o pedido era 0,4–0,7, mas o Minecraft não toca abaixo de 0,5. */
    public static final float PITCH_MIN = 0.5f;
    public static final float PITCH_MAX = 0.7f;

    public static float pitch(Sorteio s) {
        return PITCH_MIN + (PITCH_MAX - PITCH_MIN) * s.ate(101) / 100f;
    }
}
