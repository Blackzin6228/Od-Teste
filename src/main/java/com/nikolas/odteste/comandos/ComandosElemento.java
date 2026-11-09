package com.nikolas.odteste.comandos;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.ElementoJogador;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Elemento do jogador (só admin, debaixo de /odteste, que já pede permissão 2):
 *
 * /odteste elemento <jogador> · ver
 * /odteste elemento <jogador> <sangue|conhecimento|morte|energia|nenhum> · definir (esconde de novo se trocar)
 * /odteste elemento revelar <jogador> · a história revela (frase e som só para ele)
 *
 * As respostas vão só para quem digitou (nem para os outros admins): um jogador que é admin não descobre o
 * próprio elemento pelo aviso do chat.
 */
final class ComandosElemento {
    private static final String JOGADOR = "jogador";
    private static final String ELEMENTO = "elemento";

    private static final DynamicCommandExceptionType ELEMENTO_INVALIDO = new DynamicCommandExceptionType(
            nome -> Component.translatable("odteste.comando.erro.elemento", nome));

    private ComandosElemento() {}

    static LiteralArgumentBuilder<CommandSourceStack> elemento() {
        return Commands.literal("elemento")
                .then(Commands.literal("revelar").then(Commands.argument(JOGADOR, EntityArgument.player())
                        .executes(ComandosElemento::revelar)))
                .then(Commands.argument(JOGADOR, EntityArgument.player())
                        .executes(ComandosElemento::ver)
                        .then(Commands.argument(ELEMENTO, StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Elemento.values()).map(Elemento::id), b))
                                .executes(ComandosElemento::definir)));
    }

    private static Component estado(ServerPlayer p) {
        Elemento e = ElementoJogador.de(p);
        String chave = e == Elemento.NENHUM ? "odteste.comando.elemento.sem"
                : ElementoJogador.revelado(p) ? "odteste.comando.elemento.revelado" : "odteste.comando.elemento.escondido";
        return Component.translatable(chave, p.getDisplayName(), Component.translatable(e.chaveTraducao()));
    }

    private static int ver(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, JOGADOR);
        c.getSource().sendSuccess(() -> estado(p), false);
        return ElementoJogador.de(p).ordinal();
    }

    private static int definir(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, JOGADOR);
        String nome = StringArgumentType.getString(c, ELEMENTO);
        Elemento e = Elemento.deId(nome).orElseThrow(() -> ELEMENTO_INVALIDO.create(nome));
        ElementoJogador.definir(p, e);
        c.getSource().sendSuccess(() -> estado(p), false);
        return 1;
    }

    private static int revelar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, JOGADOR);
        if (!ElementoJogador.revelar(p)) {
            c.getSource().sendFailure(Component.translatable("odteste.comando.elemento.erro.sem", p.getDisplayName()));
            return 0;
        }
        c.getSource().sendSuccess(() -> estado(p), false);
        return 1;
    }
}
