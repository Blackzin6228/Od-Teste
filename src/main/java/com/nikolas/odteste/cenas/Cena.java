package com.nikolas.odteste.cenas;

import java.util.List;

/**
 * Uma cena de câmera do mod (ex.: a chegada à Vila Calada; depois, a do Diabo). Java puro: só números e nomes.
 * Quem toca é o cliente ({@code cliente/CenaCliente}); o servidor só manda "toque a cena X neste ponto".
 *
 * Coordenadas são <b>locais</b>, relativas à âncora da cena: {@code x} = para a direita, {@code y} = para cima,
 * {@code z} = para a frente (para dentro do lugar). O giro da âncora vem no pacote. Tempos em ticks (20 por segundo),
 * contados a partir do começo da câmera.
 *
 * @param id        nome da cena (fica salvo em {@code odteste:cenas_vistas}: nunca muda)
 * @param fim       tick em que a câmera volta ao jogador (depois disso só o preto sumindo)
 * @param saida     ticks do preto sumindo, já com a câmera de volta no jogador
 * @param planos    planos da câmera, em ordem; entre dois planos o corte é seco
 * @param preto     tela preta por cima de tudo: pontos (tick, opacidade 0-1), reta entre eles
 * @param sons      sons vanilla, sem origem (tocam "na cabeça"), cada um uma vez
 * @param legendas  textos curtos no meio da tela (chave de tradução)
 * @param cortaSons tick em que todos os sons da cena param (-1 = nunca)
 */
public record Cena(String id, int fim, int saida, List<Plano> planos, List<Ponto> preto, List<Som> sons,
        List<Legenda> legendas, int cortaSons) {

    public Cena {
        planos = List.copyOf(planos);
        preto = List.copyOf(preto);
        sons = List.copyOf(sons);
        legendas = List.copyOf(legendas);
    }

    /** Ponto local (x direita, y cima, z frente). */
    public record Local(double x, double y, double z) {
        public Local mais(double dx, double dy, double dz) {
            return new Local(x + dx, y + dy, z + dz);
        }
    }

    /**
     * Um plano: a câmera vai de {@code de} até {@code ate} olhando de {@code alvoDe} até {@code alvoAte}, com
     * aceleração suave no começo e no fim.
     *
     * @param balanco altura do balanço "de passo" (blocos; 0 = parado)
     * @param zoom    quanto o campo de visão cai até o fim do plano (graus; 0 = nada)
     */
    public record Plano(int inicio, int fimPlano, Local de, Local ate, Local alvoDe, Local alvoAte, double balanco,
            float zoom) {}

    public record Ponto(int tick, float valor) {}

    /** @param id nome do som vanilla (ex.: {@code minecraft:ambient.cave}) */
    public record Som(int tick, String id, float volume, float tom) {}

    /**
     * @param chave  chave de tradução
     * @param linha  0 = primeira linha, 1 = segunda (abaixo)
     * @param entra  tick em que começa a aparecer
     * @param sai    tick em que já sumiu
     */
    public record Legenda(String chave, int linha, int entra, int sai) {}
}
