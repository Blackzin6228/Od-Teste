package com.nikolas.odteste.cliente;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Uma lufada de vento só neste cliente: um som vanilla longo, sem posição (vem de todo lado), que cresce, fica e
 * some sozinho em {@code dura} ticks. Assim um som de loop vira um sopro curto, sem corte seco.
 */
final class SomVento extends AbstractTickableSoundInstance {
    private final float volumeMax;
    private final int dura;
    private int idade;

    SomVento(SoundEvent som, float volume, float tom, int dura, RandomSource r) {
        super(som, SoundSource.AMBIENT, r);
        this.volumeMax = volume;
        this.dura = Math.max(20, dura);
        this.volume = 0.001f;
        this.pitch = tom;
        this.looping = false;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    @Override
    public boolean canStartSilent() {
        return true; // começa em volume quase zero e cresce
    }

    @Override
    public void tick() {
        idade++;
        if (idade >= dura) {
            stop();
            return;
        }
        float t = idade / (float) dura;
        float envelope = t < 0.3f ? t / 0.3f : t > 0.7f ? (1f - t) / 0.3f : 1f;
        volume = Math.max(0.001f, volumeMax * envelope);
    }
}
