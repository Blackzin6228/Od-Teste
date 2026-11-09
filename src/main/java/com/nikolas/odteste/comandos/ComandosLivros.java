package com.nikolas.odteste.comandos;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nikolas.odteste.historia.livros.LivrosHistoria;
import com.nikolas.odteste.historia.livros.LivrosLidos;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Livros de história, só de admin (dentro de /odteste, que já pede permissão 2):
 *
 * /odteste livro dar <livro> [jogadores] · lista · esquecer <livro> <jogadores>
 *
 * "esquecer" faz o livro contar como nunca aberto (para testar o gancho de primeira leitura de novo).
 * As mensagens são só para o admin, em texto direto (sem chave de tradução).
 */
public final class ComandosLivros {
    private static final String LIVRO = "livro";
    private static final String JOGADORES = "jogadores";

    private ComandosLivros() {}

    public static LiteralArgumentBuilder<CommandSourceStack> livro() {
        return Commands.literal("livro").requires(s -> s.hasPermission(2))
                .then(Commands.literal("dar").then(argLivro()
                        .executes(c -> dar(c, List.of(c.getSource().getPlayerOrException())))
                        .then(Commands.argument(JOGADORES, EntityArgument.players())
                                .executes(c -> dar(c, EntityArgument.getPlayers(c, JOGADORES))))))
                .then(Commands.literal("lista").executes(ComandosLivros::lista))
                .then(Commands.literal("esquecer").then(argLivro()
                        .then(Commands.argument(JOGADORES, EntityArgument.players()).executes(ComandosLivros::esquecer))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> argLivro() {
        return Commands.argument(LIVRO, StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(LivrosHistoria.ids(), b));
    }

    private static int dar(CommandContext<CommandSourceStack> c, Collection<ServerPlayer> jogadores) {
        String id = StringArgumentType.getString(c, LIVRO);
        if (LivrosHistoria.livro(id).isEmpty()) {
            c.getSource().sendFailure(Component.literal("Livro desconhecido: " + id + " (veja /odteste livro lista)"));
            return 0;
        }
        int n = 0;
        for (ServerPlayer p : jogadores) {
            Optional<ItemStack> livro = LivrosHistoria.criar(id, p);
            if (livro.isEmpty()) continue;
            if (!p.getInventory().add(livro.get())) p.drop(livro.get(), false);
            n++;
        }
        int total = n;
        c.getSource().sendSuccess(() -> Component.literal("Livro " + id + " entregue a " + total + " jogador(es)."), true);
        return n;
    }

    private static int lista(CommandContext<CommandSourceStack> c) {
        List<String> ids = LivrosHistoria.ids();
        String texto = ids.isEmpty() ? "Nenhum livro de história carregado." : "Livros: " + String.join(", ", ids);
        c.getSource().sendSuccess(() -> Component.literal(texto).withStyle(ChatFormatting.GRAY), false);
        return ids.size();
    }

    private static int esquecer(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String id = StringArgumentType.getString(c, LIVRO);
        LivrosLidos lidos = LivrosLidos.de(c.getSource().getServer());
        int n = 0;
        for (ServerPlayer p : EntityArgument.getPlayers(c, JOGADORES)) {
            if (lidos.esquecer(p.getUUID(), id)) n++;
        }
        int total = n;
        c.getSource().sendSuccess(() -> Component.literal("Livro " + id + " esquecido por " + total + " jogador(es)."), true);
        return n;
    }
}
