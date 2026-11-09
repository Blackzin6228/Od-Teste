package com.nikolas.odteste.itens;

import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Exposicao;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Sino de Matadouro (raro): um toque em área. Jogadores a até 8 blocos (quem toca também) ganham Força e
 * Velocidade por 15 s. Custo: 3 corações de quem toca e Exposição +25.
 * Com 3 corações ou menos, o sino não toca (não mata o dono).
 */
public final class SinoDeMatadouro extends Artefato {
    public SinoDeMatadouro() {
        super(RegrasArtefato.Raridade.RARA, Elemento.SANGUE);
    }

    @Override
    protected boolean usar(ServerPlayer p, boolean dobro) {
        float vidaCusto = custoVida(p, RegrasArtefato.SINO_VIDA);
        if (p.getHealth() <= vidaCusto) {
            p.displayClientMessage(Component.translatable("odteste.artefato.sino.sem_sangue")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
            return false;
        }
        ServerLevel nivel = p.serverLevel();
        // Custo em vida direto (não passa por armadura nem por imunidade de dano recente).
        p.setHealth(p.getHealth() - vidaCusto);
        nivel.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.8f, 0.6f);

        double r = RegrasArtefato.SINO_RAIO;
        for (Player aliado : nivel.getEntitiesOfClass(Player.class, p.getBoundingBox().inflate(r),
                o -> o.isAlive() && !o.isSpectator() && o.distanceToSqr(p) <= r * r)) {
            aliado.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, RegrasArtefato.SINO_TICKS, 0, false, true, false));
            aliado.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, RegrasArtefato.SINO_TICKS, 0, false, true, false));
            nivel.sendParticles(Sangramento.GOTA, aliado.getX(), aliado.getY() + 1.2, aliado.getZ(), 6, 0.3, 0.4, 0.3, 0.0);
        }
        Exposicao.custo(p, Elemento.SANGUE, custo(p, RegrasArtefato.SINO_EXPOSICAO * (dobro ? 2 : 1)));

        // O toque: grave e longo, para todos por perto.
        nivel.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.5f, 0.5f);
        nivel.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0f, 0.5f);
        p.displayClientMessage(Component.translatable("odteste.artefato.sino.uso")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
        return true;
    }
}
