package com.nikolas.odteste.efeitos;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.registro.Registro;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Sangramento: a ferida aberta que a Rubra, o Dedal Dentado e as criaturas de Sangue deixam.
 * Uma vez por segundo tira um pouco de vida (mais, quanto maior o nível). O dano é do tipo
 * {@code odteste:sangramento}: atravessa armadura e conta como paranormal na Lucidez (tags em data/).
 * O ícone e o tempo não aparecem na tela (regra: nenhum número); quem sangra vê as gotas.
 */
public final class Sangramento extends MobEffect {
    public static final ResourceKey<DamageType> DANO = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.SANGRAMENTO));

    public static final DeferredHolder<MobEffect, Sangramento> EFEITO =
            Registro.EFEITOS.register(IdsDados.SANGRAMENTO, Sangramento::new);

    /** Gota vermelho-escura (0x8B0000). */
    public static final DustParticleOptions GOTA = new DustParticleOptions(new Vector3f(0.55f, 0.0f, 0.0f), 1.0f);

    private Sangramento() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    /** Força o carregamento da classe antes do registro. */
    public static void iniciar() {}

    @Override
    public boolean shouldApplyEffectTickThisTick(int duracao, int nivel) {
        return duracao % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity alvo, int nivel) {
        if (alvo.level() instanceof ServerLevel sl) {
            alvo.hurt(fonte(sl, null), RegrasSangramento.danoPorSegundo(nivel));
            sl.sendParticles(GOTA, alvo.getX(), alvo.getY() + alvo.getBbHeight() * 0.5, alvo.getZ(),
                    2 + nivel, alvo.getBbWidth() * 0.3, alvo.getBbHeight() * 0.25, alvo.getBbWidth() * 0.3, 0.0);
        }
        return true;
    }

    /** Fonte de dano de sangramento; com {@code causador}, a morte conta para ele. */
    public static DamageSource fonte(Level level, @Nullable Entity causador) {
        Holder<DamageType> tipo = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DANO);
        return causador == null ? new DamageSource(tipo) : new DamageSource(tipo, causador);
    }

    /** Põe (ou aumenta) o sangramento. Efeito novo reinicia o tempo; nunca passa do nível máximo. */
    public static void aplicar(LivingEntity alvo, int nivel, int duracaoTicks, @Nullable Entity causador) {
        int n = Math.max(0, Math.min(RegrasSangramento.NIVEL_MAX, nivel));
        alvo.addEffect(new MobEffectInstance(EFEITO, duracaoTicks, n, false, true, false), causador);
    }

    /** Nível atual (−1 = não sangra). */
    public static int nivel(LivingEntity alvo) {
        MobEffectInstance e = alvo.getEffect(EFEITO);
        return e == null ? -1 : e.getAmplifier();
    }
}
