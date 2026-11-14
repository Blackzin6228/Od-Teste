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
 * · preparar [canto] (põe livro, comida do poço, placa, vela, poças, martelo e peitoril na vila colocada à mão; ver PrepararVilaCalada)
 * · dobre resolver|reabrir|pendurado <grupo> · dobre empurrao <grupo> cliques|bigorna|corrente (ver {@link Dobre})
 *
 * Para o admin, número aparece. Para o jogador, nunca.
 */
public final class ComandosVilaCalada {
    private static final String GRUPO = "grupo";
    private static final String PONTO = "ponto";
    private static final String JOGADORES = "jogadores";
    private static final String CANTO = "canto";

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
                        .then(Commands.literal("reabrir").then(argGrupo().executes(c -> chegou(c, false))))
                        .then(Commands.literal("preparar").executes(c -> preparar(c, false))
                                .then(Commands.argument(CANTO, BlockPosArgument.blockPos()).executes(c -> preparar(c, true))))
                        .then(Commands.literal("dobre")
                                .then(Commands.literal("resolver").then(argGrupo().executes(ComandosVilaCalada::dobreResolver)))
                                .then(Commands.literal("reabrir").then(argGrupo().executes(ComandosVilaCalada::dobreReabrir)))
                                .then(Commands.literal("pendurado").then(argGrupo().executes(ComandosVilaCalada::dobrePendurado)))
                                .then(Commands.literal("empurrao").then(argGrupo()
                                        .then(Commands.argument("qual", StringArgumentType.word())
                                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                        java.util.List.of("cliques", "bigorna", "corrente"), b))
                                                .executes(ComandosVilaCalada::dobreEmpurrao)))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> argGrupo() {
        return Commands.argument(GRUPO, StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                        DadosHistoria.de(c.getSource().getServer()).grupos.todos().stream().map(Grupos.Grupo::nome), b));
    }

    private static int ok(CommandContext<CommandSourceStack> c, String chave, Object... args) {
        c.getSource().sendSuccess(() -> Component.translatable(chave, args), false);
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

    private static int preparar(CommandContext<CommandSourceStack> c, boolean comCanto) {
        DadosHistoria dh = DadosHistoria.de(c.getSource().getServer());
        GlobalPos a = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        ServerLevel nivel;
        BlockPos canto;
        if (comCanto) {
            nivel = c.getSource().getLevel();
            canto = BlockPosArgument.getBlockPos(c, CANTO);
        } else {
            if (a == null) return erro(c, "odteste.evento.erro.lugar", IdsVilaCalada.LUGAR_VILA_CALADA);
            nivel = c.getSource().getServer().getLevel(a.dimension());
            if (nivel == null) return erro(c, "odteste.evento.erro.dimensao");
            Optional<BlockPos> achou = PrepararVilaCalada.acharCanto(nivel, a.pos());
            if (achou.isEmpty()) return erro(c, "odteste.vila_calada.preparar.sem_canto");
            canto = achou.get();
        }
        BlockPos k = canto;
        c.getSource().sendSuccess(() -> Component.translatable("odteste.vila_calada.preparar.canto",
                k.getX(), k.getY(), k.getZ()).withStyle(ChatFormatting.GOLD), false);
        for (Component l : PrepararVilaCalada.preparar(nivel, canto, PistasVilaCalada.dados(c.getSource().getServer()))) {
            c.getSource().sendSuccess(() -> l, false);
        }
        return 1;
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

    // ------------------------------------------------------------------ dobre

    private static Optional<Grupos.Grupo> grupo(CommandContext<CommandSourceStack> c) {
        return DadosHistoria.de(c.getSource().getServer()).grupos.grupo(StringArgumentType.getString(c, GRUPO));
    }

    /** Resolver à força (teste e emergência): o mesmo fim do dobre certo, no sino rachado. */
    private static int dobreResolver(CommandContext<CommandSourceStack> c) {
        Optional<Grupos.Grupo> g = grupo(c);
        if (g.isEmpty()) return erro(c, "odteste.evento.erro.sem_grupo", StringArgumentType.getString(c, GRUPO));
        var s = c.getSource().getServer();
        ServerLevel nivel = Dobre.nivel(s);
        Optional<BlockPos> canto = nivel == null ? Optional.empty() : Dobre.canto(s, true);
        if (canto.isEmpty()) return erro(c, "odteste.vila_calada.preparar.sem_canto");
        BlockPos sino = PrepararVilaCalada.em(canto.get(), RegrasPrepararVila.SINO);
        Dobre.feito(nivel, DadosHistoria.de(s), PistasVilaCalada.dados(s), g.get().nome(), sino, canto.get(), true);
        return ok(c, "odteste.dobre.cmd.resolvido", g.get().nome());
    }

    private static int dobreReabrir(CommandContext<CommandSourceStack> c) {
        Optional<Grupos.Grupo> g = grupo(c);
        if (g.isEmpty()) return erro(c, "odteste.evento.erro.sem_grupo", StringArgumentType.getString(c, GRUPO));
        Dobre.reabrir(c.getSource().getServer(), g.get().nome());
        return ok(c, "odteste.dobre.cmd.reaberto", g.get().nome());
    }

    private static int dobrePendurado(CommandContext<CommandSourceStack> c) {
        Optional<Grupos.Grupo> g = grupo(c);
        if (g.isEmpty()) return erro(c, "odteste.evento.erro.sem_grupo", StringArgumentType.getString(c, GRUPO));
        var s = c.getSource().getServer();
        Optional<BlockPos> canto = Dobre.nivel(s) == null ? Optional.empty() : Dobre.canto(s, true);
        if (canto.isEmpty()) return erro(c, "odteste.vila_calada.preparar.sem_canto");
        int n = Dobre.penduradoNoSino(s, g.get(), PrepararVilaCalada.em(canto.get(), RegrasPrepararVila.SINO));
        return ok(c, "odteste.dobre.cmd.pendurado", n);
    }

    private static int dobreEmpurrao(CommandContext<CommandSourceStack> c) {
        Optional<Grupos.Grupo> g = grupo(c);
        if (g.isEmpty()) return erro(c, "odteste.evento.erro.sem_grupo", StringArgumentType.getString(c, GRUPO));
        String qual = StringArgumentType.getString(c, "qual");
        if (!Dobre.empurrar(c.getSource().getServer(), g.get(), qual)) return erro(c, "odteste.dobre.cmd.empurrao_falhou", qual);
        return ok(c, "odteste.dobre.cmd.empurrao", qual, g.get().nome());
    }
}
