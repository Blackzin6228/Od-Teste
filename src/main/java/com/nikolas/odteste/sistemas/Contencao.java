package com.nikolas.odteste.sistemas;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * "Algo segura você aqui" (RegrasContencao), só servidor. Dentro das nossas dimensões ou em perseguição:
 * - teleporte que avisa o NeoForge (pérola, coro, e mods que usam o evento de teleporte) é cancelado;
 * - trocar de dimensão (/home em outra dimensão, Waystones, portais, teleportadores) é cancelado;
 * - voo (criativo de mod e élitro) cai.
 * O admin passa: /tp e /spreadplayers liberam a troca de dimensão logo em seguida. Criativo e espectador, também.
 * O conteúdo marca a perseguição com {@link #perseguir}; o admin, com {@code /odteste perseguicao}.
 * Nada é salvo: reiniciar o servidor solta todo mundo (perseguição é coisa de minutos).
 * Sem lag: o voo é olhado a cada 5 ticks, e quem não está preso sai na primeira conta.
 */
public final class Contencao {
    private static final ResourceLocation ID_VOO = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.MODIFICADOR_VOO);
    private static final long MAX_PERSEGUICAO_TICKS = RegrasContencao.PERSEGUICAO_MAX_S * 20L;

    private static final Map<UUID, Long> PERSEGUICAO_ATE = new HashMap<>();
    private static final Map<UUID, Long> PASSE_ATE = new HashMap<>();
    private static final Map<UUID, Long> ULTIMO_AVISO = new HashMap<>();

    private Contencao() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(Contencao::aoTeleportar);
        NeoForge.EVENT_BUS.addListener(Contencao::aoTrocarDimensao);
        NeoForge.EVENT_BUS.addListener(Contencao::aoTick);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> esquecer(e.getEntity().getUUID()));
    }

    /** Começa (ou estende) a perseguição do jogador por {@code ticks}. */
    public static void perseguir(ServerPlayer p, int ticks) {
        long t = Math.max(1, Math.min(ticks, MAX_PERSEGUICAO_TICKS));
        PERSEGUICAO_ATE.put(p.getUUID(), agora(p) + t);
    }

    public static void soltar(ServerPlayer p) {
        PERSEGUICAO_ATE.remove(p.getUUID());
    }

    public static boolean emPerseguicao(ServerPlayer p) {
        Long ate = PERSEGUICAO_ATE.get(p.getUUID());
        if (ate == null) return false;
        if (RegrasContencao.vale(agora(p), ate, MAX_PERSEGUICAO_TICKS)) return true;
        PERSEGUICAO_ATE.remove(p.getUUID());
        return false;
    }

    /** Saída da história (ex.: a saída de uma dimensão nossa): libera a próxima troca de dimensão por 1 s. */
    public static void darPasse(ServerPlayer p) {
        PASSE_ATE.put(p.getUUID(), agora(p) + RegrasContencao.PASSE_TICKS);
    }

    public static boolean preso(ServerPlayer p) {
        // Contas baratas primeiro: a maioria sai no criativo ou na dimensão.
        if (p.isCreative() || p.isSpectator()) return false;
        boolean nossa = RegrasContencao.nossaDimensao(p.level().dimension().location().getNamespace());
        // A perseguição só é olhada (um mapa) se não estiver numa dimensão nossa.
        return RegrasContencao.preso(nossa, !nossa && emPerseguicao(p), false);
    }

    private static boolean temPasse(ServerPlayer p) {
        return RegrasContencao.vale(agora(p), PASSE_ATE.get(p.getUUID()), RegrasContencao.PASSE_TICKS);
    }

    private static void aoTeleportar(EntityTeleportEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        // Comando do admin: passa, e libera a troca de dimensão que vem logo depois.
        if (e instanceof EntityTeleportEvent.TeleportCommand || e instanceof EntityTeleportEvent.SpreadPlayersCommand) {
            darPasse(p);
            return;
        }
        if (!preso(p) || temPasse(p)) return;
        e.setCanceled(true);
        avisar(p);
    }

    private static void aoTrocarDimensao(EntityTravelToDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (!preso(p) || temPasse(p)) return;
        e.setCanceled(true);
        avisar(p);
    }

    private static void aoTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if ((p.tickCount + p.getId()) % RegrasContencao.TICKS_VOO != 0) return;
        boolean preso = p.isAlive() && preso(p);
        AttributeInstance voo = p.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (voo != null) {
            boolean tem = voo.getModifier(ID_VOO) != null;
            if (preso && !tem) {
                voo.addTransientModifier(new AttributeModifier(ID_VOO, RegrasContencao.VOO_MODIFICADOR,
                        AttributeModifier.Operation.ADD_VALUE));
            } else if (!preso && tem) {
                voo.removeModifier(ID_VOO);
            }
        }
        if (!preso) return;
        boolean caiu = false;
        if (p.getAbilities().flying) {
            p.getAbilities().flying = false;
            p.onUpdateAbilities();
            caiu = true;
        }
        if (p.isFallFlying()) {
            p.stopFallFlying();
            caiu = true;
        }
        if (caiu) avisar(p);
    }

    private static void avisar(ServerPlayer p) {
        long t = agora(p);
        if (!RegrasContencao.podeAvisar(t, ULTIMO_AVISO.get(p.getUUID()))) return;
        ULTIMO_AVISO.put(p.getUUID(), t);
        p.displayClientMessage(Component.translatable("odteste.contencao.preso")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
    }

    private static long agora(ServerPlayer p) {
        return p.server.overworld().getGameTime();
    }

    private static void esquecer(UUID id) {
        PERSEGUICAO_ATE.remove(id);
        PASSE_ATE.remove(id);
        ULTIMO_AVISO.remove(id);
    }

    // ---------- comando do admin ----------

    /** {@code /odteste perseguicao comecar <jogadores> <segundos>} e {@code /odteste perseguicao parar <jogadores>}. */
    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("perseguicao").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("comecar")
                                .then(Commands.argument("jogadores", EntityArgument.players())
                                        .then(Commands.argument("segundos", IntegerArgumentType.integer(
                                                        RegrasContencao.PERSEGUICAO_MIN_S, RegrasContencao.PERSEGUICAO_MAX_S))
                                                .executes(Contencao::comecar))))
                        .then(Commands.literal("parar")
                                .then(Commands.argument("jogadores", EntityArgument.players())
                                        .executes(Contencao::parar)))));
    }

    private static int comecar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Collection<ServerPlayer> alvos = EntityArgument.getPlayers(c, "jogadores");
        int segundos = IntegerArgumentType.getInteger(c, "segundos");
        for (ServerPlayer p : alvos) perseguir(p, segundos * 20);
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.perseguicao.comecar",
                alvos.size(), segundos), true);
        return alvos.size();
    }

    private static int parar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Collection<ServerPlayer> alvos = EntityArgument.getPlayers(c, "jogadores");
        for (ServerPlayer p : alvos) soltar(p);
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.perseguicao.parar", alvos.size()), true);
        return alvos.size();
    }
}
