package com.nikolas.odteste.compat.lodestone;

import java.awt.Color;
import net.minecraft.world.level.Level;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;

/**
 * Partícula com brilho do Lodestone (vem com o Malum). **Só cliente** e só chamada de cliente/ atrás de
 * {@code Compat.Integracao.LODESTONE.ligada()}: sem o mod, esta classe nunca carrega e o cliente usa o pó vanilla.
 */
public final class BrilhoLodestone {
    private BrilhoLodestone() {}

    /**
     * Um ponto de brilho que encolhe e some.
     * @param cor   cor do começo (0xRRGGBB); o fim é a mesma cor bem mais escura
     * @param vida  em ticks
     */
    public static void brilho(Level nivel, double x, double y, double z, double vx, double vy, double vz,
            float tamanho, float opacidade, int cor, int vida) {
        Color inicio = new Color(cor);
        Color fim = new Color((cor >> 16 & 0xFF) / 4, (cor >> 8 & 0xFF) / 4, (cor & 0xFF) / 4);
        WorldParticleBuilder.create(LodestoneParticleTypes.WISP_PARTICLE)
                .setScaleData(GenericParticleData.create(tamanho, tamanho * 0.2f).build())
                .setTransparencyData(GenericParticleData.create(opacidade, 0f).build())
                .setColorData(ColorParticleData.create(inicio, fim).build())
                .setLifetime(vida)
                .setMotion(vx, vy, vz)
                .enableNoClip()
                .spawn(nivel, x, y, z);
    }
}
