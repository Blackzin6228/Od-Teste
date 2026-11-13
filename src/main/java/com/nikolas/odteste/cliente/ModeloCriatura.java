package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.criaturas.CriaturaDeSangue;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * Modelo das criaturas de Sangue com a textura variante B (decisão de 13/11/2026).
 * A variante sai do UUID da criatura: a mesma criatura tem sempre a mesma pele, no servidor inteiro e depois de
 * reiniciar, sem nenhum dado salvo novo. Conta de um bit por desenho (nada pesado).
 */
final class ModeloCriatura<T extends CriaturaDeSangue> extends DefaultedEntityGeoModel<T> {
    private final ResourceLocation texturaB;

    ModeloCriatura(String nome) {
        super(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, nome));
        this.texturaB = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, "textures/entity/" + nome + "_b.png");
    }

    /** Metade das criaturas usa a B. O {@code _glowmask} segue a textura escolhida (retalhado_b_glowmask.png). */
    static boolean varianteB(CriaturaDeSangue criatura) {
        return (criatura.getUUID().getLeastSignificantBits() & 1L) == 1L;
    }

    @Override
    public ResourceLocation getTextureResource(T criatura) {
        return varianteB(criatura) ? texturaB : super.getTextureResource(criatura);
    }
}
