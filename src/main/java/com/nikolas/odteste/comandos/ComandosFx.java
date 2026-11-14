package com.nikolas.odteste.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nikolas.odteste.historia.vilacalada.RegrasFxSangue;
import com.nikolas.odteste.rede.EfeitoFx;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Efeitos de tela do Sangue (permissão 2):
 * <ul>
 * <li>{@code /odfx <efeito> [segundos]}: toca em quem digitou, em qualquer lugar (teste);</li>
 * <li>{@code /odfx gravar <efeito> [segundos]}: o mesmo, e salva os quadros em {@code screenshots/odteste/};</li>
 * <li>{@code /odfx mestre <sangue|coracao> <jogadores> [segundos]}: o mestre solta num jogador (evento).</li>
 * </ul>
 * O servidor só manda um pacote curto ({@link EfeitoFx}); o desenho é todo no cliente.
 */
public final class ComandosFx {
    private static final String EFEITO = "efeito";
    private static final String SEGUNDOS = "segundos";
    private static final String JOGADORES = "jogadores";

    private ComandosFx() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
    }

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odfx").requires(s -> s.hasPermission(2))
                .then(Commands.literal("gravar")
                        .then(efeito(RegrasFxSangue.EFEITOS)
                                .executes(c -> eu(c, false, 0))
                                .then(Commands.argument(SEGUNDOS, IntegerArgumentType.integer(1, RegrasFxSangue.TESTE_MAXIMO))
                                        .executes(c -> eu(c, true, IntegerArgumentType.getInteger(c, SEGUNDOS))))))
                .then(Commands.literal("mestre")
                        .then(efeito(RegrasFxSangue.DO_MESTRE)
                                .then(Commands.argument(JOGADORES, EntityArgument.players())
                                        .executes(c -> mestre(c, 0))
                                        .then(Commands.argument(SEGUNDOS,
                                                        IntegerArgumentType.integer(1, RegrasFxSangue.TESTE_MAXIMO))
                                                .executes(c -> mestre(c, IntegerArgumentType.getInteger(c, SEGUNDOS)))))))
                .then(efeito(RegrasFxSangue.EFEITOS)
                        .executes(c -> eu(c, false, 0, false))
                        .then(Commands.argument(SEGUNDOS, IntegerArgumentType.integer(1, RegrasFxSangue.TESTE_MAXIMO))
                                .executes(c -> eu(c, false, IntegerArgumentType.getInteger(c, SEGUNDOS), false)))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> efeito(
            List<String> opcoes) {
        return Commands.argument(EFEITO, StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(opcoes, b));
    }

    /** {@code gravar}: o literal "gravar" já foi digitado. */
    private static int eu(CommandContext<CommandSourceStack> c, boolean comSegundos, int segundos)
            throws CommandSyntaxException {
        return eu(c, comSegundos, segundos, true);
    }

    private static int eu(CommandContext<CommandSourceStack> c, boolean comSegundos, int segundos, boolean gravar)
            throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String efeito = StringArgumentType.getString(c, EFEITO);
        if (!RegrasFxSangue.conhecido(efeito)) {
            c.getSource().sendFailure(Component.translatable("odteste.comando.fx.desconhecido", efeito,
                    String.join(", ", RegrasFxSangue.EFEITOS)));
            return 0;
        }
        PacketDistributor.sendToPlayer(p, new EfeitoFx(efeito, comSegundos || segundos > 0 ? segundos : 0, gravar));
        String chave = gravar ? "odteste.comando.fx.gravando" : "odteste.comando.fx.tocou";
        c.getSource().sendSuccess(() -> Component.translatable(chave, efeito), false);
        return 1;
    }

    private static int mestre(CommandContext<CommandSourceStack> c, int segundos) throws CommandSyntaxException {
        String efeito = StringArgumentType.getString(c, EFEITO);
        if (!RegrasFxSangue.DO_MESTRE.contains(efeito)) {
            c.getSource().sendFailure(Component.translatable("odteste.comando.fx.desconhecido", efeito,
                    String.join(", ", RegrasFxSangue.DO_MESTRE)));
            return 0;
        }
        Collection<ServerPlayer> alvos = EntityArgument.getPlayers(c, JOGADORES);
        for (ServerPlayer p : alvos) {
            PacketDistributor.sendToPlayer(p, new EfeitoFx(efeito, segundos, false));
        }
        int n = alvos.size();
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.fx.mestre", efeito, n), true);
        return n;
    }
}
