package com.nikolas.odteste.cliente;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Sons tocados só neste cliente (ninguém mais ouve). Sempre sons vanilla.
 * O Minecraft prende o pitch entre 0,5 e 2,0: por isso usamos 0,5–0,7 (docs/animacoes.md).
 */
final class Sons {
    private Sons() {}

    static void em(Vec3 pos, SoundEvent som, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || som == null) return;
        mc.level.playLocalSound(pos.x, pos.y, pos.z, som, SoundSource.AMBIENT, volume, pitch, false);
    }

    static void em(Vec3 pos, Holder<SoundEvent> som, float volume, float pitch) {
        em(pos, som.value(), volume, pitch);
    }

    /** Na posição da entidade (para os outros: o som "sai" do jogador). */
    static void em(Entity e, SoundEvent som, float volume, float pitch) {
        em(e.position().add(0, e.getBbHeight() * 0.6, 0), som, volume, pitch);
    }

    static void em(Entity e, Holder<SoundEvent> som, float volume, float pitch) {
        em(e, som.value(), volume, pitch);
    }

    /** Atrás do jogador, a {@code distancia} blocos (sussurro, passos). */
    static Vec3 atras(Entity e, double distancia) {
        Vec3 olhar = e.getLookAngle();
        Vec3 plano = new Vec3(olhar.x, 0, olhar.z);
        if (plano.lengthSqr() < 1e-4) plano = new Vec3(0, 0, 1);
        return e.getEyePosition().subtract(plano.normalize().scale(distancia));
    }
}
