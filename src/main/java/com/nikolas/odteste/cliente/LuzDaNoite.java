package com.nikolas.odteste.cliente;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.joml.Vector3f;

/**
 * O céu do overworld igual ao do jogo, com um ajuste só: o mapa de luz (a tabela de 16×16 cores que o jogo usa
 * para clarear cada bloco) passa por {@link CamadaHorrorCliente#ajustarLuz} antes do brilho do jogador.
 * Assim a noite fica escura de verdade e o brilho alto não clareia, mas tocha e lanterna continuam
 * clareando. Funciona com shader pack (Iris lê o mesmo mapa de luz). Nada muda quando a camada está desligada.
 *
 * Se outro mod trocar o céu do overworld depois de nós, esta classe não é usada e a camada cai no véu de reserva.
 */
final class LuzDaNoite extends DimensionSpecialEffects.OverworldEffects {
    @Override
    public void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken, float blockLightRedFlicker,
                                     float skyLight, int pixelX, int pixelY, Vector3f colors) {
        super.adjustLightmapColors(level, partialTicks, skyDarken, blockLightRedFlicker, skyLight, pixelX, pixelY, colors);
        CamadaHorrorCliente.ajustarLuz(pixelX, colors);
    }
}
