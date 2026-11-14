package com.nikolas.odteste.horror;

/**
 * Frequência dos sustos grandes (decisão de 14/11/2026: "uma ou duas coisas grandes por noite por jogador; o resto é
 * tensão e silêncio"). Java puro, testado em RegrasSustoTest. Só o servidor decide; o cliente só toca.
 *
 * <ul>
 * <li>No máximo {@link #maxPorNoite}: 1 por noite; 2 com Exposição Alta ou Crítica (Fundo para cima).</li>
 * <li>Pelo menos {@link #ESPERA_ENTRE} entre dois, e {@link #ESPERA_LIGOU} depois que a noite começa para ele
 * (primeiro vem a tensão).</li>
 * <li>Uma tentativa a cada {@link #CHECAR_A_CADA}; a chance sobe com a Exposição e cai pela metade com outro jogador
 * a menos de 8 blocos (o grupo ajuda, como nas alucinações).</li>
 * <li>O mestre pode disparar quando quiser ({@code /odteste susto}): conta para a noite, mas não é barrado.</li>
 * </ul>
 */
public final class RegrasSusto {
    /** Uma tentativa por jogador a cada 30 s. */
    public static final int CHECAR_A_CADA = 20 * 30;
    /** Entre dois sustos grandes do mesmo jogador: 5 min. */
    public static final int ESPERA_ENTRE = 20 * 60 * 5;
    /** Depois que a camada liga para ele: 1 min de nada. */
    public static final int ESPERA_LIGOU = 20 * 60;
    /** Um dia do Minecraft, em ticks. */
    public static final long DIA = 24_000L;

    /**
     * Chance por tentativa, por faixa de Exposição (0 Comum ... 4 Crítica). Numa noite (~8 min, ~16 tentativas,
     * menos a espera inicial) dá mais ou menos: Comum 35%, Tocado 50%, Marcado 65%, Fundo 80%, Crítica 90%
     * de ter pelo menos um.
     */
    private static final double[] CHANCE = {0.03, 0.05, 0.075, 0.11, 0.15};

    private RegrasSusto() {}

    /** Gerador: de 0 até {@code limite - 1}. */
    @FunctionalInterface
    public interface Sorteio {
        int ate(int limite);
    }

    private static int faixa(int f) {
        return Math.max(0, Math.min(CHANCE.length - 1, f));
    }

    public static int maxPorNoite(int faixaExposicao) {
        return faixa(faixaExposicao) >= 3 ? 2 : 1;
    }

    public static double chance(int faixaExposicao, boolean grupoPerto) {
        double c = CHANCE[faixa(faixaExposicao)];
        return grupoPerto ? c * 0.5 : c;
    }

    /** Número da noite: igual do anoitecer ao amanhecer (a noite vai de 13000 a 23000 dentro do mesmo dia). */
    public static long noite(long horaDoDia) {
        return Math.floorDiv(horaDoDia, DIA);
    }

    /**
     * Chamado uma vez por segundo: é a vez deste jogador? Uma tentativa a cada 30 s, espalhada entre os jogadores
     * pelo {@code espalhar} (ex.: hash do UUID), para não cair todo mundo no mesmo segundo.
     */
    public static boolean vezDeTentar(long segundo, int espalhar) {
        return Math.floorMod(segundo + espalhar, CHECAR_A_CADA / 20) == 0;
    }

    /** Diário de um jogador: só memória (reiniciar zera; no pior caso, um susto a mais naquela noite). */
    public static final class Diario {
        private long noite = Long.MIN_VALUE;
        private int feitos;
        private long ultimo = Long.MIN_VALUE / 2;
        private long ligouEm = -1;

        /** A camada ligou ou desligou para ele (só o começo importa). */
        public void camada(boolean ligada, long agora) {
            if (!ligada) ligouEm = -1;
            else if (ligouEm < 0) ligouEm = agora;
        }

        public int feitos(long noiteAtual) {
            return noiteAtual == noite ? feitos : 0;
        }

        /** Ainda cabe um susto sozinho agora? */
        public boolean cabe(long noiteAtual, long agora, int faixaExposicao) {
            if (ligouEm < 0 || agora - ligouEm < ESPERA_LIGOU) return false;
            if (agora - ultimo < ESPERA_ENTRE) return false;
            return feitos(noiteAtual) < maxPorNoite(faixaExposicao);
        }

        public void marcar(long noiteAtual, long agora) {
            if (noiteAtual != noite) {
                noite = noiteAtual;
                feitos = 0;
            }
            feitos++;
            ultimo = agora;
        }
    }

    /** Tentativa: cabe e o sorteio (0 a 1) caiu dentro da chance. */
    public static boolean tentar(Diario d, long noiteAtual, long agora, int faixaExposicao, boolean grupoPerto,
                                 double sorteio) {
        return d.cabe(noiteAtual, agora, faixaExposicao) && sorteio < chance(faixaExposicao, grupoPerto);
    }

    /** Peso de cada tipo por faixa de Exposição: luz e passos pesam mais lá no fundo. */
    public static int peso(TipoSusto t, int faixaExposicao) {
        int f = faixa(faixaExposicao);
        return switch (t) {
            case PASSOS -> 3 + (f >= 2 ? 1 : 0);
            case PORTA -> 3;
            case LUZ -> 2 + (f >= 3 ? 2 : 0);
            case SUSSURRO -> 3;
            case SILENCIO -> 2;
        };
    }

    /** Sorteia o tipo, nunca o mesmo do último (se houver outro). */
    public static TipoSusto escolher(int faixaExposicao, TipoSusto ultimo, Sorteio s) {
        int total = 0;
        for (TipoSusto t : TipoSusto.values()) {
            if (t != ultimo) total += peso(t, faixaExposicao);
        }
        int x = s.ate(Math.max(1, total));
        for (TipoSusto t : TipoSusto.values()) {
            if (t == ultimo) continue;
            x -= peso(t, faixaExposicao);
            if (x < 0) return t;
        }
        return TipoSusto.PASSOS;
    }
}
