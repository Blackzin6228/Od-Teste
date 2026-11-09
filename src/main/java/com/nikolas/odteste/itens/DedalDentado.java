package com.nikolas.odteste.itens;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Lucidez;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

/**
 * Dedal Dentado (incomum): por 20 s, quem te bate de perto sangra (30% do dano volta nele, como sangramento).
 * Custo: Lucidez −5 e −1 coração máximo até dormir (modificador salvo no jogador).
 */
public final class DedalDentado extends Artefato {
    public static final ResourceLocation ID_CORACAO =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.MODIFICADOR_DEDAL);

    /** Até quando (tempo do mundo) o dedal está ativo em cada jogador. Só memória: 20 s; some ao sair. */
    private static final Map<UUID, Long> ATIVO_ATE = new HashMap<>();

    public DedalDentado() {
        super(RegrasArtefato.Raridade.INCOMUM, Elemento.SANGUE);
    }

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(DedalDentado::aoTomarDano);
        NeoForge.EVENT_BUS.addListener(DedalDentado::aoAmanhecer);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> ATIVO_ATE.remove(e.getEntity().getUUID()));
    }

    @Override
    protected boolean usar(ServerPlayer p, boolean dobro) {
        long agora = p.server.overworld().getGameTime();
        ATIVO_ATE.put(p.getUUID(), agora + RegrasArtefato.DEDAL_TICKS); // não empilha: reinicia o tempo
        Lucidez.perder(p, custo(p, RegrasArtefato.DEDAL_LUCIDEZ * (dobro ? 2 : 1)));

        AttributeInstance vida = p.getAttribute(Attributes.MAX_HEALTH);
        if (vida != null && !vida.hasModifier(ID_CORACAO)) {
            // O valor é negativo: o desconto vale sobre o tamanho da perda.
            double perda = custoVida(p, (float) -RegrasArtefato.DEDAL_VIDA_MAX);
            vida.addPermanentModifier(new AttributeModifier(ID_CORACAO, -perda,
                    AttributeModifier.Operation.ADD_VALUE));
            if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        }

        p.serverLevel().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.EVOKER_FANGS_ATTACK,
                SoundSource.PLAYERS, 0.5f, 0.7f);
        p.serverLevel().sendParticles(Sangramento.GOTA, p.getX(), p.getY() + 1.0, p.getZ(), 5, 0.2, 0.3, 0.2, 0.0);
        p.displayClientMessage(Component.translatable("odteste.artefato.dedal.uso")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
        return true;
    }

    private static boolean ativo(ServerPlayer p) {
        Long ate = ATIVO_ATE.get(p.getUUID());
        if (ate == null) return false;
        long agora = p.server.overworld().getGameTime();
        if (agora >= ate || agora < ate - RegrasArtefato.DEDAL_TICKS) {
            ATIVO_ATE.remove(p.getUUID());
            return false;
        }
        return true;
    }

    /** Golpe de perto num jogador com o dedal: parte do dano volta em quem bateu, como sangramento. */
    private static void aoTomarDano(LivingDamageEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        DamageSource fonte = e.getSource();
        // Sangramento não devolve sangramento (sem pingue-pongue entre dois dedais).
        if (fonte.is(Sangramento.DANO)) return;
        if (!(fonte.getEntity() instanceof LivingEntity atacante) || atacante == p) return;
        if (fonte.getDirectEntity() != atacante) return; // só de perto (flecha e magia não contam)
        if (!ativo(p)) return;

        float volta = Math.max(1f, e.getNewDamage() * RegrasArtefato.DEDAL_REFLEXO);
        ServerLevel nivel = p.serverLevel();
        atacante.hurt(Sangramento.fonte(nivel, p), volta);
        Sangramento.aplicar(atacante, 0, 60, p);
        nivel.sendParticles(Sangramento.GOTA, atacante.getX(), atacante.getY() + atacante.getBbHeight() * 0.6,
                atacante.getZ(), 6, 0.2, 0.2, 0.2, 0.0);
        nivel.playSound(null, atacante.getX(), atacante.getY(), atacante.getZ(), SoundEvents.EVOKER_FANGS_ATTACK,
                SoundSource.PLAYERS, 0.4f, 0.6f);
    }

    /** "Até dormir": quem passou a noite na cama recupera o coração. */
    private static void aoAmanhecer(SleepFinishedTimeEvent e) {
        if (!(e.getLevel() instanceof ServerLevel nivel)) return;
        for (ServerPlayer p : nivel.players()) {
            if (!p.isSleeping()) continue;
            AttributeInstance vida = p.getAttribute(Attributes.MAX_HEALTH);
            if (vida != null) vida.removeModifier(ID_CORACAO);
        }
    }
}
