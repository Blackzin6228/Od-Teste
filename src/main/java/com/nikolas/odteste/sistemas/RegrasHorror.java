package com.nikolas.odteste.sistemas;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * Decisões das alucinações (o que o cliente toca sozinho, ninguém mais vê), seguindo docs/horror.md.
 * Java puro, testado em RegrasHorrorTest.
 *
 * - Cada faixa tem uma espera mínima entre duas alucinações (horror.md, tabelas 1.1 e 2.1). Vale a da pior faixa.
 * - O intervalo é sorteado entre a espera mínima e o maior entre o dobro dela e 4 min.
 * - Com outro jogador a menos de 8 blocos, a frequência cai pela metade (o grupo ajuda).
 * - Uma por vez; a mesma não se repete antes de 2 min.
 * - Sem "efeitos fortes", só sobra o que é som ou texto.
 */
public final class RegrasHorror {
    /** Nível máximo da intensidade (0 a 4), usado só para dosar efeitos contínuos. */
    public static final int NIVEL_MAX = 4;
    /** A menor espera mínima de todas (Lucidez Quebrando): 75 s. Nunca duas alucinações mais perto que isso. */
    public static final int INTERVALO_MINIMO = 20 * 75;
    /** O teto do sorteio nunca fica abaixo de 4 min. */
    public static final int TETO_SORTEIO = 20 * 240;
    /** A mesma alucinação só volta depois de 2 min. */
    public static final int COOLDOWN_MESMA = 20 * 120;
    /** "Grupo por perto": outro jogador a menos de 8 blocos. */
    public static final double GRUPO_RAIO_SQ = 8 * 8;

    /** Gerador de sorteio: devolve de 0 até {@code limite - 1}. No jogo é o RandomSource; no teste, java.util.Random. */
    @FunctionalInterface
    public interface Sorteio {
        int ate(int limite);
    }

    // ---------- faixas do horror.md ----------

    /** Índice da faixa de Exposição no horror.md: 0 Comum, 1 Baixa, 2 Média, 3 Alta, 4 Crítica. */
    public static int faixa(Degrau d) {
        if (d == null) return 0;
        return switch (d) {
            case COMUM -> 0;
            case TOCADO -> 1;
            case MARCADO -> 2;
            case FUNDO -> 3;
            case A_BEIRA, ATRAVESSADO -> 4;
        };
    }

    /** Índice da faixa de Lucidez no horror.md: 0 Firme, 1 Alta, 2 Média, 3 Baixa (o Surto conta como Baixa). */
    public static int faixa(FaixaLucidez f) {
        if (f == null) return 0;
        return Math.min(3, f.gravidade());
    }

    /** Espera mínima de Exposição, em s, por índice de faixa (0 = nada). */
    private static final int[] ESPERA_EXPOSICAO_S = {0, 240, 180, 120, 90};
    /** Espera mínima de Lucidez, em s, por índice de faixa (0 = nada). */
    private static final int[] ESPERA_LUCIDEZ_S = {0, 180, 120, 75};

    /**
     * Espera mínima entre duas alucinações, em ticks: a menor das duas faixas que estão ativas. 0 = nenhuma alucinação.
     */
    public static int esperaMinima(FaixaLucidez f, Degrau d) {
        int e = ESPERA_EXPOSICAO_S[faixa(d)];
        int l = ESPERA_LUCIDEZ_S[faixa(f)];
        int s;
        if (e == 0) s = l;
        else if (l == 0) s = e;
        else s = Math.min(e, l);
        return s * 20;
    }

    /** Peso da Exposição na intensidade: Comum 0, Baixa/Média 1, Alta 2, Crítica 3. */
    private static final int[] PESO_EXPOSICAO = {0, 1, 1, 2, 3};

    /** Ticks até a próxima: entre a espera e max(2 × espera, 4 min). 0 = nenhuma. */
    public static int intervalo(int espera, Sorteio s) {
        if (espera <= 0) return 0;
        int teto = Math.max(espera * 2, TETO_SORTEIO);
        return espera + s.ate(teto - espera + 1);
    }

    /**
     * Intensidade de 0 a 4 (o pior dos dois; se os dois estão pesados, sobe mais um). Só dosa efeitos contínuos
     * (visão que respira, inclinação, batidas do coração); a frequência vem de {@link #esperaMinima}.
     */
    public static int nivel(FaixaLucidez lucidez, Degrau exposicao) {
        int l = faixa(lucidez);
        int e = PESO_EXPOSICAO[faixa(exposicao)];
        int n = Math.max(l, e) + (Math.min(l, e) >= 2 ? 1 : 0);
        return Math.min(NIVEL_MAX, n);
    }

    // ---------- quando ----------

    /**
     * Agenda das alucinações de um jogador (só memória, no cliente). Chamar uma vez por tick:
     * devolve true quando é hora de tocar uma.
     */
    public static final class Agenda {
        private long proxima = -1;
        private long ultima = Long.MIN_VALUE / 2;
        private int esperaAgendada;
        private boolean adiadaPeloGrupo;

        /**
         * @param espera espera mínima da faixa atual ({@link #esperaMinima}); 0 = nada
         * @param grupo  só é consultado na hora de tocar: com grupo perto, a vez é pulada uma vez (frequência pela metade)
         */
        public boolean deveTocar(long agora, int espera, BooleanSupplier grupo, Sorteio s) {
            if (espera <= 0) {
                proxima = -1;
                esperaAgendada = 0;
                return false;
            }
            if (proxima < 0 || espera != esperaAgendada) {
                // Mudou de faixa (ou começou): reagenda, nunca antes da espera desde a última.
                // Piorando (espera menor), pode adiantar; melhorando, nunca adianta.
                long novo = agora + intervalo(espera, s);
                if (proxima >= 0) novo = espera < esperaAgendada ? Math.min(novo, proxima) : Math.max(novo, proxima);
                proxima = Math.max(novo, ultima + espera);
                esperaAgendada = espera;
            }
            if (agora < proxima) return false;
            if (!adiadaPeloGrupo && grupo != null && grupo.getAsBoolean()) {
                // O grupo ajuda: esta vez passa em branco e espera um intervalo inteiro de novo.
                adiadaPeloGrupo = true;
                proxima = agora + intervalo(espera, s);
                return false;
            }
            adiadaPeloGrupo = false;
            ultima = agora;
            proxima = agora + intervalo(espera, s);
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
            esperaAgendada = 0;
            adiadaPeloGrupo = false;
            ultima = Long.MIN_VALUE / 2;
        }
    }

    // ---------- qual ----------

    /**
     * Cada alucinação e a partir de que faixa ela aparece em cada sistema (índice do horror.md; -1 = nunca por esse
     * sistema). "visual" = some quando "efeitos fortes" está DESLIGADO. Os nomes não são salvos: podem mudar.
     */
    public enum Alucinacao {
        /** Sussurro grave, com eco. Exposição Baixa, Lucidez Alta. */
        SUSSURRO(1, 1, false),
        /** Um passo ou estalo, uma vez. Exposição Baixa (longe), Lucidez Alta (logo atrás). */
        ESTALO(1, 1, false),
        /** Passos atrás de você que param quando você vira. Exposição Média, Lucidez Média. */
        PASSOS(2, 2, false),
        /** Porta que abre sozinha (só na sua tela). Exposição Média, Lucidez Média. */
        PORTA(2, 2, true),
        /** Sombras se mexendo na borda (partículas só suas). Lucidez Média. */
        PARTICULAS(-1, 2, true),
        /** Cisco de sombra passando no canto. Exposição Alta, Lucidez Média. */
        CISCO(3, 2, true),
        /** Vulto no canto da visão, some quando você olha. Exposição Alta, Lucidez Baixa. */
        VULTO(3, 3, true),
        /** Seu nome sussurrado no chat, sem remetente. Exposição Alta, Lucidez Baixa (com a sua voz). Texto: fica no DESLIGADO. */
        NOME(3, 3, false),
        /** Batimento lento. Exposição Alta, Lucidez Baixa. */
        CORACAO(3, 3, false),
        /** A câmera pende um instante. Exposição Alta, Lucidez Baixa. */
        INCLINAR(3, 3, true),
        /** Som de criatura que não existe. Exposição Crítica, Lucidez Baixa. */
        SOM_CRIATURA(4, 3, false),
        /** Escuridão curta (sem efeitos fortes, vira vinheta). Lucidez Baixa. */
        ESCURECER(-1, 3, false),
        /** Criatura falsa. Exposição Crítica (de costas), Lucidez Baixa (vem até você). */
        MOB_FALSO(4, 3, true);

        public final int faixaExposicao;
        public final int faixaLucidez;
        public final boolean visual;

        Alucinacao(int faixaExposicao, int faixaLucidez, boolean visual) {
            this.faixaExposicao = faixaExposicao;
            this.faixaLucidez = faixaLucidez;
            this.visual = visual;
        }

        public boolean permitida(FaixaLucidez f, Degrau d) {
            return (faixaExposicao > 0 && faixa(d) >= faixaExposicao)
                    || (faixaLucidez > 0 && faixa(f) >= faixaLucidez);
        }

        /** Peso no sorteio: as da própria faixa saem mais (3), as de uma faixa abaixo 2, as mais leves 1. */
        public int peso(FaixaLucidez f, Degrau d) {
            int p = 0;
            if (faixaExposicao > 0 && faixa(d) >= faixaExposicao) p = Math.max(p, pesoDistancia(faixa(d) - faixaExposicao));
            if (faixaLucidez > 0 && faixa(f) >= faixaLucidez) p = Math.max(p, pesoDistancia(faixa(f) - faixaLucidez));
            return p;
        }

        private static int pesoDistancia(int distancia) {
            return distancia <= 0 ? 3 : (distancia == 1 ? 2 : 1);
        }
    }

    private RegrasHorror() {}

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
    public static List<Alucinacao> possiveis(FaixaLucidez f, Degrau d, boolean efeitosFortes, Recentes recentes,
                                             long agora) {
        List<Alucinacao> l = new ArrayList<>();
        for (Alucinacao a : Alucinacao.values()) {
            if (!a.permitida(f, d)) continue;
            if (a.visual && !efeitosFortes) continue;
            if (recentes != null && !recentes.livre(a, agora)) continue;
            l.add(a);
        }
        return l;
    }

    /** Sorteia uma (ou nenhuma, se nada estiver livre). Não marca: quem toca chama {@link Recentes#marcar}. */
    public static Optional<Alucinacao> escolher(FaixaLucidez f, Degrau d, boolean efeitosFortes, Recentes recentes,
                                                long agora, Sorteio s) {
        List<Alucinacao> l = possiveis(f, d, efeitosFortes, recentes, agora);
        if (l.isEmpty()) return Optional.empty();
        int total = 0;
        for (Alucinacao a : l) total += a.peso(f, d);
        int x = s.ate(total);
        for (Alucinacao a : l) {
            x -= a.peso(f, d);
            if (x < 0) return Optional.of(a);
        }
        return Optional.of(l.get(l.size() - 1));
    }

    // ---------- vulto e criatura falsa ----------

    /** O vulto some se você olhar direto (menos de 18° do centro da tela). */
    public static final double COS_OLHOU = Math.cos(Math.toRadians(18));
    /** ...se chegar a 4 blocos... */
    public static final double VULTO_PERTO_SQ = 4 * 4;
    /** ...ou depois de 12 s (SUAVE: 1 s). */
    public static final int VULTO_VIDA = 20 * 12;
    public static final int VIDA_SUAVE = 20;

    /**
     * @param cosOlhar cosseno do ângulo entre para onde você olha e a direção do vulto (1 = bem no centro)
     */
    public static boolean vultoSome(double cosOlhar, double distanciaSq, int idade, int vida) {
        return cosOlhar >= COS_OLHOU || distanciaSq <= VULTO_PERTO_SQ || idade >= vida;
    }

    /** Ângulo do vulto em relação ao olhar, em graus: 35° a 50°, para a esquerda (negativo) ou direita. */
    public static int vultoAngulo(Sorteio s) {
        int graus = 35 + s.ate(16);
        return s.ate(2) == 0 ? -graus : graus;
    }

    /** Distância do vulto: 12 a 16 blocos (horror.md: 12+). */
    public static int vultoDistancia(Sorteio s) {
        return 12 + s.ate(5);
    }

    /** A criatura falsa some se você chegar a 3 blocos... */
    public static final double MOB_PERTO_SQ = 3 * 3;
    /** ...ou depois de 30 s (SUAVE: 1 s). */
    public static final int MOB_VIDA = 20 * 30;
    /** Na Lucidez Baixa ela vem até você, devagar (blocos por tick). */
    public static final double MOB_PASSO = 0.06;
    /** Na Exposição Crítica, ao ser olhada ela some e reaparece uma vez, 5 blocos mais perto (nunca a menos de 8). */
    public static final int MOB_REAPARECE_MAIS_PERTO = 5;
    public static final int MOB_REAPARECE_MINIMO = 8;

    public static boolean mobSome(double distanciaSq, int idade, int vida) {
        return distanciaSq <= MOB_PERTO_SQ || idade >= vida;
    }

    /** Distância da criatura falsa: 14 a 20 blocos. */
    public static int mobDistancia(Sorteio s) {
        return 14 + s.ate(7);
    }

    /** Distância ao reaparecer mais perto. */
    public static int mobReaparece(double distanciaAtual) {
        return (int) Math.max(MOB_REAPARECE_MINIMO, Math.floor(distanciaAtual) - MOB_REAPARECE_MAIS_PERTO);
    }

    /** Lentidão sutil com o vulto perto (Exposição Crítica): −5%, nunca mais. Só no cliente, no movimento. */
    public static final float LENTIDAO_VULTO = 0.05f;

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

    // ---------- tela e corpo ----------

    private static final float[] FOV = {0f, 0f, 0f, 0.012f, 0.025f};

    private static int idx(int nivel) {
        return Math.max(0, Math.min(NIVEL_MAX, nivel));
    }

    /** Distorção leve: a visão "respira" (fração do campo de visão). */
    public static float distorcao(int nivel) {
        return FOV[idx(nivel)];
    }

    /**
     * Quanto a cor some (0 a 1), horror.md 1.2 e 2.2: Exposição Alta 20%, Crítica 45%; Lucidez Média 15%,
     * Baixa 40%. Vale o maior. (SUAVE corta pela metade; DESLIGADO zera: isso é feito na Tela.)
     */
    public static float dessaturar(FaixaLucidez f, Degrau d) {
        float e = switch (faixa(d)) {
            case 3 -> 0.20f;
            case 4 -> 0.45f;
            default -> 0f;
        };
        float l = switch (faixa(f)) {
            case 2 -> 0.15f;
            case 3 -> 0.40f;
            default -> 0f;
        };
        return Math.max(e, l);
    }

    /**
     * Mão tremendo (deslocamento do modelo da mão, em blocos), horror.md 1.3 e 2.3:
     * Exposição Média+ ao segurar item paranormal parado (de leve); Lucidez Média+ ao mirar ou usar item.
     */
    public static float tremorMao(FaixaLucidez f, Degrau d, boolean itemParanormal, boolean parado, boolean usandoItem) {
        float m = 0f;
        if (usandoItem) {
            if (faixa(f) == 2) m = 0.006f;
            else if (faixa(f) >= 3) m = 0.012f;
        }
        if (itemParanormal && parado && faixa(d) >= 2) {
            m = Math.max(m, faixa(d) >= 3 ? 0.006f : 0.004f);
        }
        return m;
    }

    /**
     * Visão balançando (graus), horror.md 1.3 e 2.3: Exposição Alta+ bem pouco ao parar; Lucidez Baixa de leve ao andar.
     */
    public static float balanco(FaixaLucidez f, Degrau d, boolean parado) {
        float b = 0f;
        if (parado && faixa(d) >= 3) b = faixa(d) >= 4 ? 0.5f : 0.35f;
        if (!parado && faixa(f) >= 3) b = Math.max(b, 0.7f);
        return b;
    }

    // ---------- som ----------

    /** Pitch dos sons de horror: o pedido é 0,4–0,7, mas o Minecraft não toca abaixo de 0,5. */
    public static final float PITCH_MIN = 0.5f;
    public static final float PITCH_MAX = 0.7f;
    /** Nenhum som de horror passa de volume 0,6. */
    public static final float VOLUME_MAX = 0.6f;

    public static float pitch(Sorteio s) {
        return PITCH_MIN + (PITCH_MAX - PITCH_MIN) * s.ate(101) / 100f;
    }

    /** Base dos sons com eco: 0,6–0,7, para os ecos (−0,05 cada) ainda tocarem acima de 0,5. */
    public static float pitchComEco(Sorteio s) {
        return 0.6f + 0.1f * s.ate(101) / 100f;
    }

    /** Eco (horror.md "Sobre o som"): o mesmo som de novo 6 e 14 ticks depois. */
    public static final int[] ECO_TICKS = {0, 6, 14};

    /** Volume da batida {@code i} do eco (0 = original): 100%, 50%, 25%. */
    public static float ecoVolume(float volume, int i) {
        float v = Math.min(VOLUME_MAX, volume);
        return i <= 0 ? v : (i == 1 ? v * 0.5f : v * 0.25f);
    }

    /** Pitch da batida {@code i} do eco: 0,05 mais grave a cada repetição, nunca abaixo de 0,5. */
    public static float ecoPitch(float pitch, int i) {
        return Math.max(PITCH_MIN, pitch - 0.05f * Math.max(0, i));
    }
}
