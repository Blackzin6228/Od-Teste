package com.nikolas.odteste.itens;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Exposicao;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Agulha de Cerzir Carne (comum): fecha feridas. Regeneração por 10 s (≈ 4 corações) e para o sangramento.
 * Custo: Exposição +10 e +1 de progresso da Marca de Sangue (contato com o Sangue).
 */
public final class AgulhaDeCerzir extends Artefato {
    public AgulhaDeCerzir() {
        super(RegrasArtefato.Raridade.COMUM, Elemento.SANGUE);
    }

    @Override
    protected boolean usar(ServerPlayer p, boolean dobro) {
        p.removeEffect(Sangramento.EFEITO);
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, RegrasArtefato.AGULHA_REGEN_TICKS,
                RegrasArtefato.AGULHA_REGEN_NIVEL, false, true, false));

        // Primeiro a Marca (contato), depois a Exposição: assim a Marca que aparecer já é a de Sangue.
        DadosJogador.somarContato(p, Elemento.SANGUE, RegrasArtefato.AGULHA_CONTATO_MARCA);
        Exposicao.custo(p, Elemento.SANGUE, RegrasArtefato.AGULHA_EXPOSICAO * (dobro ? 2 : 1));

        p.serverLevel().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SLIME_SQUISH_SMALL,
                SoundSource.PLAYERS, 0.6f, 0.5f);
        p.serverLevel().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WOOL_BREAK,
                SoundSource.PLAYERS, 0.4f, 0.6f);
        p.serverLevel().sendParticles(Sangramento.GOTA, p.getX(), p.getY() + 1.0, p.getZ(), 8, 0.3, 0.4, 0.3, 0.0);
        p.displayClientMessage(Component.translatable("odteste.artefato.agulha.uso")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
        return true;
    }
}
