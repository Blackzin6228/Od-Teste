package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.marcas.PadraoVeias;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * A Marca de Sangue no corpo: veias vermelhas por cima da skin, que crescem com o estágio (1 a 4).
 * Todos que veem o jogador veem a camada (o estágio chega pelo pacote MarcaVisivel).
 *
 * Sem lag: as 4 texturas são geradas uma vez só (na primeira vez que alguém marcado aparece) e reaproveitadas;
 * desenhar é o mesmo modelo do jogador mais uma vez, só para quem tem a Marca.
 */
final class CamadaMarca extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation[] TEXTURAS = new ResourceLocation[5];

    CamadaMarca(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pai) {
        super(pai);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int luz, AbstractClientPlayer jogador,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (jogador.isInvisible() || DadosJogador.marcaElemento(jogador) != Elemento.SANGUE) return;
        int estagio = DadosJogador.marcaEstagio(jogador);
        if (estagio <= 0) return;
        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(textura(estagio)));
        getParentModel().renderToBuffer(pose, vc, luz, LivingEntityRenderer.getOverlayCoords(jogador, 0f), 0xFFFFFFFF);
    }

    /** Gera (uma vez) e devolve a textura do estágio. Roda na thread de desenho. */
    private static ResourceLocation textura(int estagio) {
        int s = Math.max(1, Math.min(4, estagio));
        if (TEXTURAS[s] == null) {
            int[] argb = PadraoVeias.gerar(s);
            NativeImage img = new NativeImage(PadraoVeias.LADO, PadraoVeias.LADO, true);
            for (int y = 0; y < PadraoVeias.LADO; y++) {
                for (int x = 0; x < PadraoVeias.LADO; x++) {
                    int c = argb[y * PadraoVeias.LADO + x];
                    // NativeImage guarda ABGR.
                    int abgr = (c & 0xFF00FF00) | ((c >> 16) & 0xFF) | ((c & 0xFF) << 16);
                    img.setPixelRGBA(x, y, abgr);
                }
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TEXTURA_MARCA_SANGUE + "_" + s);
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(img));
            TEXTURAS[s] = id;
        }
        return TEXTURAS[s];
    }
}
