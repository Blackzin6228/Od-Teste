package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.dados.Limites;
import java.util.HashMap;
import java.util.Map;

/**
 * Contas da Exposição. Java puro (sem Minecraft), testado em RegrasTest.
 *
 * - A Exposição é inteira (0–100), mas os ganhos são pequenos: o "resto" (0–99 centésimos) fica guardado
 *   até completar um ponto.
 * - Repetir a mesma fonte dentro de uma janela rende menos: 1ª vez inteira, 2ª metade, 3ª um terço...
 * - O ganho automático para em 99: chegar a 100 ("Atravessado") é só por evento do narrador ou comando.
 */
public final class RegrasExposicao {
    /** Teto do ganho automático. */
    public static final int TETO_AUTOMATICO = Limites.EXPOSICAO_MAX - 1;
    /** Janela da repetição: 10 minutos de jogo (em ticks). Depois dela, a fonte volta a render inteira. */
    public static final long JANELA_REPETICAO = 20L * 60 * 10;

    private RegrasExposicao() {}

    /** Intensidade aceita no contato: de 0 (nada) a 5 (uma criatura forte, uma relíquia maior). */
    public static final float INTENSIDADE_MAX = 5f;

    /** Ganho em centésimos com intensidade 1. */
    public static int ganhoCentesimos(int base, int repeticoesAntes) {
        return ganhoCentesimos(base, 1f, repeticoesAntes);
    }

    /**
     * Ganho em centésimos: base × intensidade, dividido por quantas vezes a mesma fonte já rendeu na janela.
     * Intensidade fora de 0–5 (ou NaN) é presa na faixa.
     */
    public static int ganhoCentesimos(int base, float intensidade, int repeticoesAntes) {
        if (base <= 0 || !(intensidade > 0)) return 0;
        float i = Math.min(INTENSIDADE_MAX, intensidade);
        return Math.round(base * i) / (1 + Math.max(0, repeticoesAntes));
    }

    public record Resultado(int exposicao, int resto) {}

    /**
     * Soma um ganho (centésimos) na Exposição atual. Nunca desce, nunca passa de 99 por aqui.
     * Já em 99 (ou acima, por comando), o resto zera e nada muda.
     */
    public static Resultado somar(int exposicao, int resto, int ganhoCentesimos) {
        int e = Limites.exposicao(exposicao);
        int r = Limites.prender(resto, 0, 99);
        if (e >= TETO_AUTOMATICO || ganhoCentesimos <= 0) {
            return new Resultado(e, e >= TETO_AUTOMATICO ? 0 : r);
        }
        long total = (long) r + ganhoCentesimos;
        long novo = e + total / 100;
        int novoResto = (int) (total % 100);
        if (novo >= TETO_AUTOMATICO) return new Resultado(TETO_AUTOMATICO, 0);
        return new Resultado((int) novo, novoResto);
    }

    /**
     * Conta quantas vezes cada fonte rendeu dentro da janela. É salvo no jogador (dado exposicao_repeticao),
     * com o tempo do mundo: sair e voltar não zera o freio.
     */
    public static final class Repeticao {
        private final Map<String, long[]> porChave = new HashMap<>(); // [inicio da janela, vezes]

        public Repeticao() {}

        /** Monta a partir do que estava salvo: chave → [inicio da janela, vezes]. Entrada estragada é ignorada. */
        public static Repeticao de(Map<String, java.util.List<Long>> salvo) {
            Repeticao r = new Repeticao();
            if (salvo == null) return r;
            salvo.forEach((chave, v) -> {
                if (chave == null || v == null || v.size() != 2 || v.get(0) == null || v.get(1) == null) return;
                long vezes = v.get(1);
                if (vezes <= 0) return;
                r.porChave.put(chave, new long[] {v.get(0), Math.min(vezes, 1000)});
            });
            return r;
        }

        /** Para salvar: só as janelas ainda abertas em {@code agora} (as velhas saem, o dado não cresce sem fim). */
        public Map<String, java.util.List<Long>> paraSalvar(long agora) {
            Map<String, java.util.List<Long>> m = new HashMap<>();
            porChave.forEach((chave, j) -> {
                if (agora - j[0] < JANELA_REPETICAO && agora >= j[0]) m.put(chave, java.util.List.of(j[0], j[1]));
            });
            return Map.copyOf(m);
        }

        /** Registra uma vez e devolve quantas vezes a chave já tinha rendido nesta janela. */
        public int registrar(String chave, long agora) {
            long[] j = porChave.get(chave);
            if (j == null || agora - j[0] >= JANELA_REPETICAO || agora < j[0]) {
                porChave.put(chave, new long[] {agora, 1});
                return 0;
            }
            int antes = (int) j[1];
            j[1]++;
            return antes;
        }

        public int tamanho() {
            return porChave.size();
        }
    }
}
