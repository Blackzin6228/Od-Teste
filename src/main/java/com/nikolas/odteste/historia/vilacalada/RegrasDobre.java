package com.nikolas.odteste.historia.vilacalada;

import java.util.UUID;

/**
 * Regras do Dobre (enigma grande da Vila Calada, Proposta A, decisão {@code 2026-11-14-enigma-vila-calada-dobre}).
 * Java puro, testado em RegrasDobreTest. Os números ficam aqui, num lugar só, para a Lore e o mestre ajustarem.
 *
 * <p>O grupo bate no sino rachado com o Martelo do Ferreiro, à meia-noite, um toque lento por morto: os seis botões
 * do peitoril da Moça do Terço e mais o Sineiro (o espaço vazio, que o grupo precisa preencher com um botão de pedra).
 * Cada toque fere quem bate e o martelo passa de mão (o mesmo jogador não dá dois seguidos). O dobre só é avaliado
 * quando o som morre (ninguém bate por {@link #FECHA} ticks): assim o grupo precisa <b>parar</b> no número certo,
 * não basta bater até dar certo.
 *
 * <p>Posições relativas ao canto noroeste de baixo da Vila Calada v2, sem giro (como {@link RegrasPrepararVila}).
 */
public final class RegrasDobre {
    /** Seis botões no peitoril (os mortos que a Moça contou). */
    public static final int BOTOES_NO_PEITORIL = 6;
    /** Um toque por morto: os seis e o Sineiro. */
    public static final int TOQUES = BOTOES_NO_PEITORIL + 1;

    /** Peitoril da casa da Moça do Terço: botões em x 54, y 2, z 85 a 90 (do mais velho ao mais novo). */
    public static final int PEITORIL_X = 54;
    public static final int PEITORIL_Y = 2;
    public static final int PEITORIL_Z0 = 85;
    /** O espaço vazio da ponta: o botão do Sineiro (z 91). */
    public static final RegrasPrepararVila.Peca ESPACO_SINEIRO =
            new RegrasPrepararVila.Peca("botao_sineiro", PEITORIL_X, PEITORIL_Y, PEITORIL_Z0 + BOTOES_NO_PEITORIL);
    /** Martelo do Ferreiro: ao lado do atril, no alto do campanário (pista "martelo" do arquivo da vila). */
    public static final RegrasPrepararVila.Peca MARTELO = new RegrasPrepararVila.Peca("martelo", 86, 18, 56);
    /** Bigorna rachada da casa do ferreiro (empurrão: vira para o campanário). */
    public static final RegrasPrepararVila.Peca BIGORNA = new RegrasPrepararVila.Peca("bigorna", 99, 1, 72);
    /** Lápide com a Cruz de Ferro riscada (a cova fechada do Sineiro; empurrão: a corrente cai em cima). */
    public static final RegrasPrepararVila.Peca LAPIDE = new RegrasPrepararVila.Peca("lapide", 76, 3, 33);

    /** Meia-noite (hora do jogo). O primeiro toque precisa cair na janela; os seguintes continuam o mesmo dobre. */
    public static final long MEIA_NOITE = 18_000;
    /** Meia janela: 30 s de cada lado (1 minuto de relógio no total). */
    public static final long JANELA = 600;
    /** Toque lento: esperar o som morrer (3 s) antes do próximo. */
    public static final long INTERVALO_MIN = 60;
    /** Pausa a cada cinco (pág. 13: "de cinco em cinco"): o sexto toque espera mais (5 s). */
    public static final long PAUSA_CINCO = 100;
    /** Ninguém bateu por 10 s: o som morreu e o dobre é avaliado. */
    public static final long FECHA = 200;
    /** Dano de cada toque em quem bate (um coração). Nunca mata: deixa pelo menos meio coração. */
    public static final float DANO_TOQUE = 2.0f;
    /** Contagem errada: Lucidez que cai em quem ouviu o sino tocar sozinho a mais. */
    public static final int LUCIDEZ_ERRO = 8;
    /** Quem ouviu: até essa distância do sino. */
    public static final double RAIO_OUVIR = 64;
    /** Criaturas que reagem ao toque e ao eco: até essa distância do sino (a vila é 150 x 150). */
    public static final double RAIO_CRIATURAS = 96;
    /** Teto de criaturas mexidas por vez (sem lag). */
    public static final int MAX_CRIATURAS = 32;
    /** No toque certo, as criaturas param e olham a torre por 2 s. */
    public static final int PARADA_TOQUE = 40;
    /** O eco anda um bloco a cada tick (a câmera da cutscene acompanha). */
    public static final double ECO_BLOCOS_POR_TICK = 1.0;
    /** O "chamado" (toque rápido ou fora de hora) no máximo uma vez a cada 30 s por grupo. */
    public static final long CHAMADO_ESPERA = 600;
    /** Moinho ao longe, na primeira noite de silêncio: três rangidos depois do último toque. */
    public static final int[] MOINHO_DEPOIS = {400, 700, 1_000};
    /** Cliques dos botões (pista 2): de noite, a cada 2,5 a 5 min, para quem está perto do peitoril. */
    public static final int CLIQUES_MIN = 3_000;
    public static final int CLIQUES_MAX = 6_000;
    /** Um clique a cada 0,75 s (dá para contar). */
    public static final int CLIQUE_INTERVALO = 15;
    /** Ouve os cliques a até 12 blocos do peitoril; com o empurrão, de qualquer lugar da praça. */
    public static final double CLIQUES_PERTO = 12;
    public static final float CLIQUES_VOLUME = 0.6f;
    public static final float CLIQUES_VOLUME_EMPURRAO = 2.5f;
    /** Empurrão dos cliques: duas noites na vila sem tentar. */
    public static final int NOITES_SEM_TENTAR = 2;
    /** Empurrão da corrente: duas contagens erradas. */
    public static final int ERROS_CORRENTE = 2;
    /** Manhã (para os empurrões que "aparecem de manhã"): do nascer do sol até 2000. */
    public static final long MANHA_FIM = 2_000;

    /** O que um golpe do martelo no sino rachado faz. */
    public enum Toque {
        /** Contou: mais um morto dobrado. */
        CONTOU(true),
        /** O mesmo jogador de novo: a mão não aguenta (não bate, sem dano, sem erro). */
        MESMA_MAO(false),
        /** Sétimo toque sem o botão do Sineiro: o martelo fica pesado e não bate (sem dano). */
        PESADO(false),
        /** Esta noite já foi gasta (errou ou terminou): o sino não responde (sem dano). */
        NOITE_GASTA(false),
        /** Antes de o som morrer: o sino chama. */
        RAPIDO(true),
        /** Fora da meia-noite (primeiro toque): o sino chama. */
        FORA_DE_HORA(true),
        /** Toque depois do último morto: erro de contagem na hora. */
        A_MAIS(true);

        /** O martelo bateu de verdade (soa e fere quem bate). */
        public final boolean bate;

        Toque(boolean bate) {
            this.bate = bate;
        }
    }

    /** Como o dobre fechou (som morreu). */
    public enum Fim {
        /** Todos os mortos, devagar, na hora, com o Sineiro: os Famintos se ajoelham. */
        FEITO,
        /** Seis toques e o espaço do Sineiro vazio: faltou um (sem Lucidez, a noite acaba). */
        FALTOU_SINEIRO,
        /** Número errado: o sino toca sozinho mais uma vez, Lucidez cai. */
        CONTA_ERRADA
    }

    /** Um dobre em andamento (só memória: reiniciar o servidor no meio é recomeçar). */
    public static final class Andamento {
        public int toques;
        public long ultimo = Long.MIN_VALUE;
        public UUID ultimaMao;

        public boolean comecou() {
            return toques > 0;
        }
    }

    private RegrasDobre() {}

    /** Dia do jogo (a noite de meia-noite 18000 pertence ao mesmo dia). */
    public static long dia(long horaDoMundo) {
        return Math.floorDiv(horaDoMundo, 24_000L);
    }

    /** Hora do dia (0 a 23999). */
    public static long hora(long horaDoMundo) {
        return Math.floorMod(horaDoMundo, 24_000L);
    }

    /** Está na janela da meia-noite? */
    public static boolean meiaNoite(long horaDoMundo) {
        return Math.abs(hora(horaDoMundo) - MEIA_NOITE) <= JANELA;
    }

    /** Manhã (para os empurrões de manhã). */
    public static boolean manha(long horaDoMundo) {
        return hora(horaDoMundo) < MANHA_FIM;
    }

    /** Espera mínima antes do toque número {@code proximo} (1 = o primeiro). */
    public static long espera(int proximo) {
        return proximo == BOTOES_NO_PEITORIL ? PAUSA_CINCO : INTERVALO_MIN;
    }

    /**
     * Julga um golpe. Não muda nada: quem chama aplica com {@link #aplicar}.
     *
     * @param a          o dobre do grupo (nunca null; sem começar = {@code toques == 0})
     * @param agora      tempo do mundo (ticks corridos, para os intervalos)
     * @param horaMundo  hora do mundo (getDayTime, para a meia-noite)
     * @param quem       quem bateu
     * @param botaoSineiro há um botão de pedra no espaço vazio do peitoril
     * @param noiteGasta o grupo já gastou esta noite
     */
    public static Toque julgar(Andamento a, long agora, long horaMundo, UUID quem, boolean botaoSineiro, boolean noiteGasta) {
        if (noiteGasta) return Toque.NOITE_GASTA;
        if (!a.comecou()) return meiaNoite(horaMundo) ? Toque.CONTOU : Toque.FORA_DE_HORA;
        if (quem != null && quem.equals(a.ultimaMao)) return Toque.MESMA_MAO;
        int proximo = a.toques + 1;
        if (proximo > TOQUES) return Toque.A_MAIS;
        if (agora - a.ultimo < espera(proximo)) return Toque.RAPIDO;
        if (proximo == TOQUES && !botaoSineiro) return Toque.PESADO;
        return Toque.CONTOU;
    }

    /** Conta o toque no andamento (só para {@link Toque#CONTOU}). */
    public static void aplicar(Andamento a, Toque t, long agora, UUID quem) {
        if (t != Toque.CONTOU) return;
        a.toques++;
        a.ultimo = agora;
        a.ultimaMao = quem;
    }

    /** O som morreu? (Andamento começado e ninguém bateu por {@link #FECHA}.) */
    public static boolean fechou(Andamento a, long agora) {
        return a.comecou() && agora - a.ultimo >= FECHA;
    }

    /** Como fechou. */
    public static Fim fim(Andamento a, boolean botaoSineiro) {
        if (a.toques == TOQUES && botaoSineiro) return Fim.FEITO;
        if (a.toques == BOTOES_NO_PEITORIL && !botaoSineiro) return Fim.FALTOU_SINEIRO;
        return Fim.CONTA_ERRADA;
    }

    /** O erro gasta a noite? (Chamado fora de hora não gasta: ainda dá para esperar a meia-noite.) */
    public static boolean gastaNoite(Toque t) {
        return t == Toque.RAPIDO || t == Toque.A_MAIS;
    }

    /** Dano do toque sem matar: no máximo {@link #DANO_TOQUE}, deixando meio coração. */
    public static float dano(float vida) {
        return Math.max(0f, Math.min(DANO_TOQUE, vida - 1f));
    }

    /** Em quantos ticks o eco chega a uma criatura a esta distância do sino. */
    public static int eco(double distancia) {
        return (int) Math.max(0, Math.round(distancia / ECO_BLOCOS_POR_TICK));
    }

    /** Próxima sequência de cliques (ticks a partir de agora), com {@code r} de 0 a 1. */
    public static int proximosCliques(double r) {
        return CLIQUES_MIN + (int) Math.floor(Math.max(0, Math.min(0.999_999, r)) * (CLIQUES_MAX - CLIQUES_MIN));
    }

    /** Posições do peitoril, do mais velho ao espaço do Sineiro (7 posições, relativas ao canto). */
    public static int[][] peitoril() {
        int[][] r = new int[TOQUES][];
        for (int i = 0; i < TOQUES; i++) r[i] = new int[] {PEITORIL_X, PEITORIL_Y, PEITORIL_Z0 + i};
        return r;
    }

    /** Empurrão dos cliques altos: noites na vila sem tentar. */
    public static boolean empurraoCliques(int noitesSemTentar) {
        return noitesSemTentar >= NOITES_SEM_TENTAR;
    }

    /** Empurrão da corrente na cova: contagens erradas. */
    public static boolean empurraoCorrente(int errosConta) {
        return errosConta >= ERROS_CORRENTE;
    }

    /** Botão de pedra do jogo (o único que conta como o do Sineiro). */
    public static boolean botaoDePedra(String idBloco) {
        return "minecraft:stone_button".equals(idBloco);
    }
}
