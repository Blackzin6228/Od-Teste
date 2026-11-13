package com.nikolas.odteste.compat.torchmaster;

import com.nikolas.odteste.criaturas.CriaturaDeSangue;
import com.nikolas.odteste.criaturas.RegrasCriaturas;
import com.nikolas.odteste.dados.DadosJogador;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Mega Torch (Torchmaster) x nossas criaturas (decisão de 13/11/2026):
 * - spawn natural continua respeitando a tocha (justo com quem constrói);
 * - evento e aparição nascem como {@code EVENT}, e a tocha não barra esse tipo (nada a fazer aqui);
 * - com Exposição Alta (Fundo, 60+), a tocha deixa de proteger esse jogador: o spawn natural da criatura nossa
 *   perto dele, que a tocha cancelou, volta a valer.
 * Não toca em nenhuma classe do Torchmaster: só olha o evento de spawn do NeoForge, por último (LOWEST), e só para
 * criatura nossa que já passou pelas nossas regras de nascimento (raridade, 1 por noite, 1 por área).
 * (A confirmar no jar: o Torchmaster 1.21.1 cancela com {@code setSpawnCancelled} neste mesmo evento.)
 * Só é ligado com o Torchmaster no pack (Compat).
 */
public final class TochaNaoProtege {
    private TochaNaoProtege() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, TochaNaoProtege::aoFinalizarSpawn);
    }

    private static void aoFinalizarSpawn(FinalizeSpawnEvent e) {
        // Contas baratas primeiro: quase todo spawn do mundo sai aqui.
        if (!e.isSpawnCancelled() || !(e.getEntity() instanceof CriaturaDeSangue)) return;
        if (e.getSpawnType() != MobSpawnType.NATURAL) return;
        ServerLevel nivel = e.getLevel().getLevel();
        Player dono = nivel.getNearestPlayer(e.getX(), e.getY(), e.getZ(), RegrasCriaturas.RAIO_JOGADOR,
                o -> o instanceof Player j && !j.isSpectator() && !j.isCreative()
                        && !RegrasCriaturas.tochaProtege(DadosJogador.exposicao(j)));
        if (!(dono instanceof ServerPlayer p)) return;
        e.setSpawnCancelled(false);
        p.displayClientMessage(Component.translatable("odteste.contencao.tocha")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
    }
}
