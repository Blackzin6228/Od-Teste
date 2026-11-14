package com.nikolas.odteste.magias;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.BiFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Comandos de admin (permissão 2) para testar as magias sem pergaminho:
 * - {@code /odteste magia testar <magia>}: solta de graça, em quem digitou;
 * - {@code /odteste magia afinidade <magia>}: faz o mesmo que a tecla de Afinidade fará (paga Lucidez, tem recarga).
 */
final class ComandosMagia {
    private ComandosMagia() {}

    static void iniciar() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
    }

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("magia").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("testar")
                                .then(Commands.argument("magia", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(IdsMagia.MAGIAS, b))
                                        .executes(c -> rodar(c, Magias::testar))))
                        .then(Commands.literal("afinidade")
                                .then(Commands.argument("magia", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(IdsMagia.MAGIAS, b))
                                        .executes(c -> rodar(c, Magias::conjurarPelaAfinidade))))));
    }

    private static int rodar(CommandContext<CommandSourceStack> c,
            BiFunction<ServerPlayer, String, Magias.Resultado> acao) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String magia = StringArgumentType.getString(c, "magia");
        Magias.Resultado r = acao.apply(p, magia);
        String chave = "odteste.comando.magia." + r.name().toLowerCase(java.util.Locale.ROOT);
        if (r == Magias.Resultado.CONJUROU) {
            c.getSource().sendSuccess(() -> Component.translatable(chave, magia), false);
            return 1;
        }
        c.getSource().sendFailure(Component.translatable(chave, magia));
        return 0;
    }
}
