package com.nikolas.odteste.cliente;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * O céu do overworld igual ao do jogo, com dois ajustes da camada de horror:
 * <ul>
 * <li>o mapa de luz (a tabela de 16×16 cores que o jogo usa para clarear cada bloco) passa por
 * {@link CamadaHorrorCliente#ajustarLuz} antes do brilho do jogador: luz do dia um pouco mais baixa, noite escura
 * de verdade, brilho alto não clareia, tocha e lanterna continuam clareando;</li>
 * <li>de dia, com a camada de fundo inteira e sem shader pack, o céu do jogo (sol, cor do pôr do sol) não é desenhado:
 * o fundo da tela já é a cor da névoa cinza, então o céu fica fechado. Nuvens e chuva continuam.</li>
 * </ul>
 * Nada muda quando a camada está desligada.
 *
 * Se outro mod trocar o céu do overworld depois de nós, esta classe não é usada e a camada cai no véu de reserva.
 */
final class LuzDaNoite extends DimensionSpecialEffects.OverworldEffects {
    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker,
                                     float skyLight, int pixelX, int pixelY, Vector3f colors) {
        super.adjustLightmapColors(level, partialTicks, skyDarken, blockLightRedFlicker, skyLight, pixelX, pixelY, colors);
        CamadaHorrorCliente.ajustarLuz(pixelX, pixelY, colors);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera,
                             Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return CamadaHorrorCliente.pularCeu();
    }
}
