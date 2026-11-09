package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * O que conta como paranormal, por tag (data/odteste/tags/...). Hoje as duas tags estão vazias: o conteúdo
 * (criaturas, danos de relíquia) e o Integrador (mobs de outros mods) acrescentam lá, sem mexer no código.
 */
public final class Paranormal {
    public static final TagKey<DamageType> DANO = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_PARANORMAL));
    public static final TagKey<EntityType<?>> CRIATURA = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_PARANORMAL));
    /** Item paranormal (artefatos, relíquias): segurar parado faz a mão tremer com Exposição Média+. Hoje vazia. */
    public static final TagKey<Item> ITEM = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_PARANORMAL));

    private Paranormal() {}

    /** Dano paranormal: o tipo do dano está na tag, ou quem bateu é uma criatura paranormal. */
    public static boolean dano(DamageSource fonte) {
        if (fonte == null) return false;
        if (fonte.is(DANO)) return true;
        Entity quem = fonte.getEntity();
        return quem != null && quem.getType().is(CRIATURA);
    }
}
