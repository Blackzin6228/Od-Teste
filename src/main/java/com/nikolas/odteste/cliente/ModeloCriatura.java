package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import com.nikolas.odteste.OdMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * Modelo GeckoLib PROVISÓRIO de uma criatura: geo e animação em assets/odteste/geo|animations/entity/,
 * e a textura gerada em código (carne com costuras), uma vez só, na primeira vez que a criatura aparece.
 * A mesa Modelador troca tudo pelo definitivo (aí a textura vira PNG e esta geração sai).
 */
final class ModeloCriatura<T extends GeoAnimatable> extends GeoModel<T> {
    /** Cores ARGB: base, sombra, brilho, costura. */
    record Paleta(int base, int sombra, int brilho, int costura, int semente) {}

    static final Paleta RETALHADO = new Paleta(0xFF6E1414, 0xFF2A0000, 0xFF9E2A22, 0xFF1A0E0A, 11);
    static final Paleta SANGUESSUGA = new Paleta(0xFF4A1410, 0xFF1E0504, 0xFF8B3A2E, 0xFFD8C8B0, 23);

    private static final int LADO = 256;

    private final ResourceLocation modelo;
    private final ResourceLocation animacoes;
    private final ResourceLocation textura;
    private final Paleta paleta;
    private boolean gerada;

    ModeloCriatura(String nome, Paleta paleta) {
        this.modelo = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, "geo/entity/" + nome + ".geo.json");
        this.animacoes = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, "animations/entity/" + nome + ".animation.json");
        this.textura = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, "textures/entity/" + nome + ".png");
        this.paleta = paleta;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return modelo;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return animacoes;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        if (!gerada) {
            gerada = true;
            Minecraft.getInstance().getTextureManager().register(textura, new DynamicTexture(gerar(paleta)));
        }
        return textura;
    }

    /** Carne em blocos de 2x2 px com variação, veias escuras e costuras grossas a cada 24 px. */
    private static NativeImage gerar(Paleta p) {
        NativeImage img = new NativeImage(LADO, LADO, true);
        for (int y = 0; y < LADO; y++) {
            for (int x = 0; x < LADO; x++) {
                int h = ruido(x >> 1, y >> 1, p.semente());
                int cor = h % 7 == 0 ? p.sombra() : h % 5 == 0 ? p.brilho() : p.base();
                // Veia: linha diagonal que serpenteia.
                if (Math.floorMod(x + (y >> 2) * 3 + (ruido(x >> 3, y >> 3, p.semente()) & 3), 37) == 0) cor = p.sombra();
                // Costura: linha horizontal com pontos verticais.
                int fy = y % 24;
                if (fy == 12 || (fy >= 10 && fy <= 14 && x % 6 == 0)) cor = p.costura();
                img.setPixelRGBA(x, y, paraAbgr(cor));
            }
        }
        return img;
    }

    private static int ruido(int x, int y, int semente) {
        int h = x * 374761393 + y * 668265263 + semente * 982451653;
        h = (h ^ (h >>> 13)) * 1274126177;
        return (h ^ (h >>> 16)) & 0x7FFFFFFF;
    }

    /** NativeImage guarda ABGR. */
    private static int paraAbgr(int argb) {
        return (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
    }
}
