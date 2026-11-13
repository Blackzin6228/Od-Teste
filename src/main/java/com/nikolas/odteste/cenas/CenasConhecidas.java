package com.nikolas.odteste.cenas;

import com.nikolas.odteste.cenas.Cena.Legenda;
import com.nikolas.odteste.cenas.Cena.Local;
import com.nikolas.odteste.cenas.Cena.Plano;
import com.nikolas.odteste.cenas.Cena.Ponto;
import com.nikolas.odteste.cenas.Cena.Som;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * As cenas do mod, escritas em código (Java puro). Para pôr uma cena nova (ex.: a do Diabo): escrever aqui, pôr o id
 * em {@link IdsCena} e em docs/ids.md, e mandar tocar com {@code Cenas.tocar} no servidor.
 */
public final class CenasConhecidas {

    /**
     * Chegada à Vila Calada (lore/cenas/entrada-vila-calada.md, rascunho da Lore). Âncora: o centro do portão, no
     * chão; {@code z} positivo = para dentro da vila. Os números são da vila de 150x150 do rascunho: se a construção
     * mudar, mudam só eles. 18 s de câmera + 0,8 s do preto sumindo.
     */
    public static final Cena ENTRADA_VILA_CALADA = new Cena(
            IdsCena.ENTRADA_VILA_CALADA,
            360,
            16,
            List.of(
                    // 1. Na estrada, olhando o portão e a vila ao fundo. Quase parada.
                    new Plano(0, 60, new Local(0, 1.7, -30), new Local(0, 1.7, -27),
                            new Local(0, 3, 40), new Local(0, 3, 40), 0, 0),
                    // 2. Rasteira, ao lado da carroça tombada (esquerda); vira para o portão. Balança como passo.
                    new Plano(60, 140, new Local(-4, 1.2, -22), new Local(-2, 1.2, -8),
                            new Local(-8, 0.6, -15), new Local(0, 2, 0), 0.06, 0),
                    // 3. Atravessa o portão, de frente para a praça.
                    new Plano(140, 200, new Local(0, 2, -4), new Local(0, 2, 8),
                            new Local(0, 2, 40), new Local(0, 2, 40), 0, 0),
                    // 4. Por cima do muro: a vila toda, girando devagar até a torre do sino.
                    new Plano(200, 280, new Local(-25, 14, -12), new Local(-10, 15.5, 4),
                            new Local(5, 0, 40), new Local(30, 22, 40), 0, 0),
                    // 5. De baixo, perto da torre, olhando a abertura do sino. Parada, zoom lento.
                    new Plano(280, 320, new Local(16, 6, 24), new Local(16, 6, 24),
                            new Local(30, 22, 40), new Local(30, 22, 40), 0, 10f)),
            // Preto: sai em 0,6 s no começo; volta em 0,4 s no fim do plano 5 e fica (plano 6, o texto).
            List.of(new Ponto(0, 1f), new Ponto(12, 0f), new Ponto(312, 0f), new Ponto(320, 1f)),
            List.of(
                    new Som(0, "minecraft:ambient.cave", 0.5f, 0.6f),
                    new Som(70, "minecraft:block.wooden_trapdoor.open", 0.35f, 0.6f),
                    new Som(105, "minecraft:block.chain.hit", 0.25f, 0.5f),
                    // Ao cruzar o muro (z = 0 no meio do plano 3).
                    new Som(160, "minecraft:block.bell.use", 0.45f, 0.5f),
                    new Som(205, "minecraft:ambient.soul_sand_valley.mood", 0.3f, 0.6f),
                    new Som(286, "minecraft:block.chain.hit", 0.3f, 0.5f),
                    new Som(302, "minecraft:block.chain.hit", 0.3f, 0.5f),
                    // Depois de 1 s de silêncio total, o sino quase sem volume no último segundo do texto.
                    new Som(340, "minecraft:block.bell.resonate", 0.12f, 0.5f)),
            List.of(
                    new Legenda("odteste.cena." + IdsCena.ENTRADA_VILA_CALADA + ".1", 0, 322, 358),
                    new Legenda("odteste.cena." + IdsCena.ENTRADA_VILA_CALADA + ".2", 1, 334, 358)),
            320);

    private static final Map<String, Cena> TODAS = Map.of(IdsCena.ENTRADA_VILA_CALADA, ENTRADA_VILA_CALADA);

    private CenasConhecidas() {}

    public static Optional<Cena> de(String id) {
        return Optional.ofNullable(TODAS.get(id));
    }

    public static Map<String, Cena> todas() {
        return TODAS;
    }
}
