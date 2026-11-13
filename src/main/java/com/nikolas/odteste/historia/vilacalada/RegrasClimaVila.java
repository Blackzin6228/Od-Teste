package com.nikolas.odteste.historia.vilacalada;

import java.util.Map;
import java.util.UUID;

/**
 * Números do clima da Vila Calada (lore/fichas-estruturas.md, "Clima"): névoa avermelhada, cinza caindo, sons de
 * ambiente e o Pendurado. Java puro (testado em RegrasClimaVilaTest), usado pelo servidor (quem está dentro) e pelo
 * cliente (o que se vê e ouve). Distâncias em blocos, tempos em ticks. Nada disso aparece na tela do jogador.
 */
public final class RegrasClimaVila {
    /** Dentro do muro: o muro da vila vai de x 4-95, z 3-88 (ferramentas/estruturas/modelos/vila_calada.py). */
    public static final double RAIO_MURO = 48;
    /** Altura em volta do ponto que ainda conta (o campanário em cima, uma adega embaixo). Caverna funda não conta. */
    public static final double ACIMA = 48;
    public static final double ABAIXO = 16;

    /** Cor do Sangue (docs/decisoes/2026-11-08-cores-dos-elementos.md). */
    public static final int COR_NEVOA = 0x7A0A10;
    /** Quanto a cor da névoa puxa para o vermelho: leve de dia, mais forte de noite. */
    public static final float MISTURA_DIA = 0.25f;
    public static final float MISTURA_NOITE = 0.45f;
    /** Até onde se enxerga com a névoa (blocos). De noite, bem mais perto. */
    public static final float ALCANCE_DIA = 96f;
    public static final float ALCANCE_NOITE = 40f;
    /** A névoa começa a esta fração do alcance. */
    public static final float INICIO = 0.15f;
    /** Entrar e sair da névoa leva ~3 s (fração por tick). */
    public static final float PASSO_FATOR = 1f / 60f;

    /** Cinza caindo: uma partícula a cada tantos ticks em volta do jogador (limite por jogador: ~5 por segundo). */
    public static final int CINZA_A_CADA = 4;
    public static final double CINZA_RAIO = 8;
    public static final double CINZA_ALTURA = 6;

    /** Sons de ambiente: um a cada 25-70 s; o primeiro, 10-30 s depois de entrar. */
    public static final int SOM_MIN = 500;
    public static final int SOM_MAX = 1_400;
    public static final int PRIMEIRO_SOM_MIN = 200;
    public static final int PRIMEIRO_SOM_MAX = 600;
    /** Os sons vêm de longe (dentro da vila, longe do jogador). */
    public static final double SOM_PERTO = 10;
    public static final double SOM_LONGE = 26;
    /** O Minecraft prende o pitch em 0,5; a decisão pede 0,5-0,7. */
    public static final float TOM_MIN = 0.5f;
    public static final float TOM_MAX = 0.7f;

    /** O Pendurado: o primeiro 40-90 s depois de entrar; depois um a cada 3-6 min. */
    public static final int PENDURADO_PRIMEIRO_MIN = 800;
    public static final int PENDURADO_PRIMEIRO_MAX = 1_800;
    public static final int PENDURADO_MIN = 3_600;
    public static final int PENDURADO_MAX = 7_200;
    /** Não achou lugar (viga, galho, telhado): tenta de novo daqui a 10 s. */
    public static final int PENDURADO_TENTAR_DE_NOVO = 200;
    /** Quanto tempo fica, se ninguém olhar direto (SUAVE usa RegrasHorror.VIDA_SUAVE). */
    public static final int PENDURADO_VIDA = 20 * 8;
    /** No canto da visão: 30-50° do centro, 9-15 blocos. */
    public static final int PENDURADO_ANGULO_MIN = 30;
    public static final int PENDURADO_ANGULO_MAX = 50;
    public static final double PENDURADO_PERTO = 9;
    public static final double PENDURADO_LONGE = 15;

    /** Teto de jogadores olhados por segundo no servidor (proteção contra lag). */
    public static final int MAX_JOGADORES = 128;

    private RegrasClimaVila() {}

    /** Está dentro do muro? (dx, dy, dz) = jogador menos o ponto da vila. */
    public static boolean dentro(double dx, double dy, double dz) {
        return dx * dx + dz * dz <= RAIO_MURO * RAIO_MURO && dy <= ACIMA && dy >= -ABAIXO;
    }

    /**
     * Quem do grupo tem a maior Exposição (só ele vê o Pendurado). Empate: o menor UUID, para não trocar de pessoa a
     * cada segundo. Lista vazia: ninguém.
     */
    public static UUID maisExposto(Map<UUID, Integer> exposicao) {
        UUID melhor = null;
        int maior = Integer.MIN_VALUE;
        for (Map.Entry<UUID, Integer> e : exposicao.entrySet()) {
            int v = e.getValue() == null ? 0 : e.getValue();
            if (melhor == null || v > maior || (v == maior && e.getKey().compareTo(melhor) < 0)) {
                melhor = e.getKey();
                maior = v;
            }
        }
        return melhor;
    }

    /** Aproxima {@code atual} de {@code alvo} em no máximo {@code passo}. */
    public static float aproximar(float atual, float alvo, float passo) {
        if (atual < alvo) return Math.min(alvo, atual + passo);
        return Math.max(alvo, atual - passo);
    }

    /**
     * Alcance da névoa (blocos): do alcance normal do jogo até o da vila.
     * @param normal alcance que o jogo já ia usar
     * @param fator  0 = fora, 1 = dentro (já com a força do efeitos_fortes)
     * @param noite  0 = dia, 1 = noite (suave entre os dois)
     * @return nunca maior que {@code normal} (cegueira, água etc. continuam mandando)
     */
    public static float alcance(float normal, float fator, float noite) {
        float alvo = ALCANCE_DIA + (ALCANCE_NOITE - ALCANCE_DIA) * limitar(noite);
        float f = limitar(fator);
        return Math.min(normal, normal + (alvo - normal) * f);
    }

    /** Quanto a cor puxa para o vermelho do Sangue (0 a 1). */
    public static float mistura(float fator, float noite) {
        return limitar(fator) * (MISTURA_DIA + (MISTURA_NOITE - MISTURA_DIA) * limitar(noite));
    }

    /** Um canal de cor (0 a 1) puxado para o mesmo canal da cor da névoa. */
    public static float misturarCanal(float canal, int deslocamento, float mistura) {
        float alvo = ((COR_NEVOA >> deslocamento) & 0xFF) / 255f;
        return canal + (alvo - canal) * limitar(mistura);
    }

    public static int proximoSom(double sorteio) {
        return entre(SOM_MIN, SOM_MAX, sorteio);
    }

    public static int primeiroSom(double sorteio) {
        return entre(PRIMEIRO_SOM_MIN, PRIMEIRO_SOM_MAX, sorteio);
    }

    public static float tom(double sorteio) {
        return TOM_MIN + (TOM_MAX - TOM_MIN) * (float) limitar((float) sorteio);
    }

    public static int proximoPendurado(double sorteio) {
        return entre(PENDURADO_MIN, PENDURADO_MAX, sorteio);
    }

    public static int primeiroPendurado(double sorteio) {
        return entre(PENDURADO_PRIMEIRO_MIN, PENDURADO_PRIMEIRO_MAX, sorteio);
    }

    /** Ângulo (graus, com lado) do Pendurado em relação ao centro da visão. */
    public static float anguloPendurado(double sorteio, boolean esquerda) {
        float a = PENDURADO_ANGULO_MIN + (PENDURADO_ANGULO_MAX - PENDURADO_ANGULO_MIN) * limitar((float) sorteio);
        return esquerda ? -a : a;
    }

    public static double distanciaPendurado(double sorteio) {
        return PENDURADO_PERTO + (PENDURADO_LONGE - PENDURADO_PERTO) * limitar((float) sorteio);
    }

    private static int entre(int min, int max, double sorteio) {
        double s = Math.max(0, Math.min(1, sorteio));
        return min + (int) Math.round(s * (max - min));
    }

    private static float limitar(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
