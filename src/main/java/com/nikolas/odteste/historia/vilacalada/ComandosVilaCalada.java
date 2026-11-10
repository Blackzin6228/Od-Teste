package com.nikolas.odteste.historia.vilacalada;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import com.nikolas.odteste.historia.IdsHistoria;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Comandos de admin (permissão 2) das pistas da Vila Calada. Ficam em {@code /odteste vila_calada}:
 *
 * ver · ligar · desligar · ancorar <ponto> · desancorar · tocar <jogadores> (toca o sino rachado agora, para testar)
 * · chegou <grupo> (marca que o grupo chegou: pistas de longe param) · reabrir <grupo> (tira o marco: pistas voltam)
 *
 * Para o admin, número aparece. Para o jogador, nunca.
 */
public final class ComandosVilaCalada {
    private static final String GRUPO = "grupo";
    private static final String PONTO = "ponto";
    private static final String JOGADORES = "jogadores";

    private ComandosVilaCalada() {}

    static void aoRegistrarComandos(RegisterCommandsEvent e) {
        registrar(e.getDispatcher());
    }

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        // Junta no mesmo /odteste dos outros comandos (o brigadier soma os filhos do mesmo nome).
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("vila_calada").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("ver").executes(ComandosVilaCalada::ver))
                        .then(Commands.literal("ligar").executes(c -> ligar(c, true)))
                        .then(Commands.literal("desligar").executes(c -> ligar(c, false)))
                        .then(Commands.literal("ancorar").then(Commands.argument(PONTO, BlockPosArgument.blockPos())
                                .executes(ComandosVilaCalada::ancorar)))
                        .then(Commands.literal("desancorar").executes(ComandosVilaCalada::desancorar))
                        .then(Commands.literal("tocar").then(Commands.argument(JOGADORES, EntityArgument.players())
                                .executes(ComandosVilaCalada::tocar)))
                        .then(Commands.literal("chegou").then(argGrupo().executes(c -> chegou(c, true))))
                        .then(Commands.literal("reabrir").then(argGrupo().executes(c -> chegou(c, false))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> argGrupo() {
        return Commands.argument(GRUPO, StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                        DadosHistoria.de(c.getSource().getServer()).grupos.todos().stream().map(Grupos.Grupo::nome), b));
    }

    private static int ok(CommandContext<CommandSourceStack> c, String chave, Object... args) {
        c.getSource().sendSuccess(() -> Component.translatable(chave, args), true);
        return 1;
    }

    private static int erro(CommandContext<CommandSourceStack> c, String chave, Object... args) {
        c.getSource().sendFailure(Component.translatable(chave, args));
        return 0;
    }

    private static int ver(CommandContext<CommandSourceStack> c) {
        DadosHistoria dh = DadosHistoria.de(c.getSource().getServer());
        DadosVilaCalada dv = PistasVilaCalada.dados(c.getSource().getServer());
        GlobalPos a = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        Component estado = Component.translatable(dv.ligado ? "odteste.vila_calada.ligado" : "odteste.vila_calada.desligado");
        Component ancora = a == null ? Component.translatable("odteste.vila_calada.sem_ancora")
                : Component.literal(a.pos().getX() + " " + a.pos().getY() + " " + a.pos().getZ() + " (" + a.dimension().location() + ")");
        String origem = dv.colocacao.isEmpty() ? "-" : dv.colocacao;
        c.getSource().sendSuccess(() -> Component.translatable("odteste.vila_calada.ver", estado, ancora, origem,
                PistasVilaCalada.viradosAgora()), false);
        for (Grupos.Grupo g : dh.grupos.todos()) {
            String chave = !g.tem(IdsHistoria.MARCO_PRIMEIRO_SINAL) ? "odteste.vila_calada.grupo.antes"
                    : g.tem(IdsVilaCalada.MARCO_CHEGOU) ? "odteste.vila_calada.grupo.chegou" : "odteste.vila_calada.grupo.seguindo";
            c.getSource().sendSuccess(() -> Component.translatable(chave, g.nome()).withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private static int ligar(CommandContext<CommandSourceStack> c, boolean ligar) {
        DadosVilaCalada dv = PistasVilaCalada.dados(c.getSource().getServer());
        dv.ligado = ligar;
        dv.setDirty();
        if (!ligar) PistasVilaCalada.aoDesligar();
        return ok(c, ligar ? "odteste.vila_calada.ligou" : "odteste.vila_calada.desligou");
    }

    private static int ancorar(CommandContext<CommandSourceStack> c) {
        BlockPos p = BlockPosArgument.getBlockPos(c, PONTO);
        DadosHistoria dh = DadosHistoria.de(c.getSource().getServer());
        dh.ancoras.put(IdsVilaCalada.LUGAR_VILA_CALADA, GlobalPos.of(c.getSource().getLevel().dimension(), p));
        dh.setDirty();
        return ok(c, "odteste.evento.ancorado", IdsVilaCalada.LUGAR_VILA_CALADA, p.getX(), p.getY(), p.getZ());
    }

    private static int desancorar(CommandContext<CommandSourceStack> c) {
        DadosHistoria dh = DadosHistoria.de(c.getSource().getServer());
        if (dh.ancoras.remove(IdsVilaCalada.LUGAR_VILA_CALADA) == null) {
            return erro(c, "odteste.evento.erro.lugar", IdsVilaCalada.LUGAR_VILA_CALADA);
        }
        dh.setDirty();
        PistasVilaCalada.aoDesligar();
        return ok(c, "odteste.evento.desancorado", IdsVilaCalada.LUGAR_VILA_CALADA);
    }

    private static int tocar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        DadosHistoria dh = DadosHistoria.de(c.getSource().getServer());
        GlobalPos a = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        if (a == null) return erro(c, "odteste.evento.erro.lugar", IdsVilaCalada.LUGAR_VILA_CALADA);
        int n = 0;
        for (ServerPlayer p : EntityArgument.getPlayers(c, JOGADORES)) {
            if (!(p.level() instanceof ServerLevel nivel) || nivel.dimension() != a.dimension()) continue;
            PistasVilaCalada.tocar(nivel, p, a.pos());
            n++;
        }
        return ok(c, "odteste.vila_calada.tocou", n);
    }

    private static int chegou(CommandContext<CommandSourceStack> c, boolean chegou) {
        String nome = StringArgumentType.getString(c, GRUPO);
        DadosHistoria dh = DadosHistoria.de(c.getSource().getServer());
        Optional<Grupos.Grupo> g = dh.grupos.grupo(nome);
        if (g.isEmpty()) return erro(c, "odteste.evento.erro.sem_grupo", nome);
        if (chegou) dh.grupos.marcar(g.get().nome(), IdsVilaCalada.MARCO_CHEGOU);
        else dh.grupos.desmarcar(g.get().nome(), IdsVilaCalada.MARCO_CHEGOU);
        dh.setDirty();
        return ok(c, chegou ? "odteste.vila_calada.marcou_chegada" : "odteste.vila_calada.reabriu", g.get().nome());
    }
}
