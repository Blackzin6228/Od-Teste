package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A Marca de Sangue no corpo (v2, aprovada em 13/11/2026): PNG 64x64 no molde da skin, um por estágio (1 a 4),
 * Steve (braço 4 px) e Alex (braço 3 px). Em cima, a camada de brilho ({@code _brilho}): só rachaduras, carne viva
 * e coração, desenhada como emissiva e pulsando devagar. O olho injetado do estágio 4 já vem no PNG.
 * Todos que veem o jogador veem a camada (o estágio chega pelo pacote MarcaVisivel).
 *
 * Sem lag: texturas fixas do resource pack (nada gerado em código); desenhar é o modelo do jogador mais duas vezes,
 * só para quem tem a Marca.
 */
final class CamadaMarca extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    /** [estágio][0 = Steve, 1 = Alex]. Índice 0 do estágio fica vazio. */
    private static final ResourceLocation[][] BASE = new ResourceLocation[5][2];
    private static final ResourceLocation[][] BRILHO = new ResourceLocation[5][2];

    static {
        for (int s = 1; s <= 4; s++) {
            for (int alex = 0; alex < 2; alex++) {
                String sufixo = alex == 1 ? "_alex" : "";
                BASE[s][alex] = textura("estagio_" + s + sufixo);
                BRILHO[s][alex] = textura("estagio_" + s + "_brilho" + sufixo);
            }
        }
    }

    CamadaMarca(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pai) {
        super(pai);
    }

    private static ResourceLocation textura(String nome) {
        return ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TEXTURA_MARCA_SANGUE + "/" + nome + ".png");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int luz, AbstractClientPlayer jogador,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (jogador.isInvisible() || DadosJogador.marcaElemento(jogador) != Elemento.SANGUE) return;
        int estagio = DadosJogador.marcaEstagio(jogador);
        if (estagio <= 0) return;
        int s = Math.min(4, estagio);
        int alex = jogador.getSkin().model() == PlayerSkin.Model.SLIM ? 1 : 0;
        int overlay = LivingEntityRenderer.getOverlayCoords(jogador, 0f);

        getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(BASE[s][alex])),
                luz, overlay, 0xFFFFFFFF);

        // Brilho: emissivo (aparece no escuro), pulso lento de ~4 s entre 55% e 100%. A cor escurece em vez do alfa,
        // porque a camada "eyes" soma luz.
        if (s < 2) return; // o estágio 1 (arranhão) ainda não brilha
        float pulso = 0.55f + 0.45f * (0.5f + 0.5f * Mth.sin(ageInTicks * 0.08f));
        int c = Math.round(255 * pulso);
        int cor = 0xFF000000 | (c << 16) | (c << 8) | c;
        getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(BRILHO[s][alex])),
                luz, overlay, cor);
    }
}
