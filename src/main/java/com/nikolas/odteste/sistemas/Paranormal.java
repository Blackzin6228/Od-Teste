package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * O que conta como paranormal, por tag (data/odteste/tags/...). O conteúdo (criaturas, danos de relíquia) e o
 * Integrador (mobs de outros mods) acrescentam lá, sem mexer no código.
 */
public final class Paranormal {
    public static final TagKey<DamageType> DANO = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_PARANORMAL));
    public static final TagKey<EntityType<?>> CRIATURA = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_PARANORMAL));
    /** Item paranormal (artefatos, relíquias): segurar parado faz a mão tremer com Exposição Média+. Hoje vazia. */
    public static final TagKey<Item> ITEM = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_PARANORMAL));

    /**
     * Arma paranormal (relíquias e espadas nossas): golpe com teto de 20 e que conta como paranormal contra
     * criatura nossa (x2). Tag {@code odteste:arma_paranormal}; hoje só a Rubra.
     */
    public static final TagKey<Item> ARMA = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_ARMA_PARANORMAL));
    /**
     * Dano de magia de outros mods (Iron's, Ars Nouveau e afins): passa por cima da resistência das criaturas nossas.
     * Tag {@code odteste:magia}, só com entradas opcionais ({@code required: false}): sem o mod no pack, a entrada
     * some sozinha e nenhuma classe de outro mod é tocada. Para outro mod de magia, basta acrescentar na tag.
     */
    public static final TagKey<DamageType> MAGIA = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_MAGIA));
    /** Tipo de dano paranormal genérico (golpe de criatura nossa; depois, relíquias e eventos). Está na tag. */
    public static final ResourceKey<DamageType> TIPO = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.DANO_PARANORMAL));

    private Paranormal() {}

    /** Dano paranormal: o tipo do dano está na tag, ou quem bateu é uma criatura paranormal. */
    public static boolean dano(DamageSource fonte) {
        if (fonte == null) return false;
        if (fonte.is(DANO)) return true;
        Entity quem = fonte.getEntity();
        return quem != null && quem.getType().is(CRIATURA);
    }

    /** Golpe direto (corpo a corpo) dado com uma arma paranormal na mão. */
    public static boolean golpeDeArma(DamageSource fonte) {
        if (fonte == null) return false;
        // Direto: quem causou é quem bateu (flecha, magia e afins não contam).
        return fonte.getEntity() instanceof LivingEntity quem && fonte.getDirectEntity() == quem
                && quem.getMainHandItem().is(ARMA);
    }

    /** Fonte de dano paranormal; com {@code causador}, a morte conta para ele. */
    public static DamageSource fonte(Level level, @Nullable Entity causador) {
        Holder<DamageType> tipo = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TIPO);
        return causador == null ? new DamageSource(tipo) : new DamageSource(tipo, causador);
    }
}
