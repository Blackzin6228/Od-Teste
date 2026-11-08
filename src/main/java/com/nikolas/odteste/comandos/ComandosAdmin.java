package com.nikolas.odteste.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.Limites;
import com.nikolas.odteste.elementos.Elemento;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Comandos de admin (permissão 2) para ver e mexer nos valores dos jogadores.
 * São para o narrador e para teste: o jogador comum nunca vê esses números.
 *
 * /exposicao ver|definir, /lucidez ver|definir|maxima, /marca ver|definir, /transcender ver|definir
 */
public final class ComandosAdmin {
    private static final String JOGADOR = "jogador";
    private static final String VALOR = "valor";

    private static final DynamicCommandExceptionType ELEMENTO_INVALIDO = new DynamicCommandExceptionType(
            nome -> Component.translatable("odteste.comando.erro.elemento", nome));

    private ComandosAdmin() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
    }

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(raiz("exposicao")
                .then(Commands.literal("ver").then(jogador().executes(c -> {
                    ServerPlayer p = alvo(c);
                    return ok(c, "odteste.comando.exposicao.ver", DadosJogador.exposicao(p), p, false);
                })))
                .then(Commands.literal("definir").then(jogador().then(valor(Limites.EXPOSICAO_MIN, Limites.EXPOSICAO_MAX)
                        .executes(c -> {
                            ServerPlayer p = alvo(c);
                            DadosJogador.definirExposicao(p, IntegerArgumentType.getInteger(c, VALOR));
                            return ok(c, "odteste.comando.exposicao.definir", DadosJogador.exposicao(p), p);
                        })))));

        d.register(raiz("lucidez")
                .then(Commands.literal("ver").then(jogador().executes(c -> {
                    ServerPlayer p = alvo(c);
                    c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.lucidez.ver",
                            p.getDisplayName(), DadosJogador.lucidez(p), DadosJogador.lucidezMax(p)), false);
                    return DadosJogador.lucidez(p);
                })))
                // A Lucidez é presa na máxima do jogador: pedir 100 para quem tem máxima 70 dá 70.
                .then(Commands.literal("definir").then(jogador().then(valor(0, Limites.LUCIDEZ_MAX_PADRAO)
                        .executes(c -> {
                            ServerPlayer p = alvo(c);
                            DadosJogador.definirLucidez(p, IntegerArgumentType.getInteger(c, VALOR));
                            return ok(c, "odteste.comando.lucidez.definir", DadosJogador.lucidez(p), p);
                        }))))
                .then(Commands.literal("maxima").then(jogador().then(valor(Limites.LUCIDEZ_MAX_PISO, Limites.LUCIDEZ_MAX_PADRAO)
                        .executes(c -> {
                            ServerPlayer p = alvo(c);
                            DadosJogador.definirLucidezMax(p, IntegerArgumentType.getInteger(c, VALOR));
                            return ok(c, "odteste.comando.lucidez.maxima", DadosJogador.lucidezMax(p), p);
                        })))));

        d.register(raiz("marca")
                .then(Commands.literal("ver").then(jogador().executes(c -> {
                    ServerPlayer p = alvo(c);
                    c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.marca.ver",
                            p.getDisplayName(),
                            Component.translatable(DadosJogador.marcaElemento(p).chaveTraducao()),
                            DadosJogador.marcaEstagio(p)), false);
                    return DadosJogador.marcaEstagio(p);
                })))
                .then(Commands.literal("definir").then(jogador()
                        .then(Commands.argument("elemento", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Elemento.values()).map(Elemento::id), b))
                                // "nenhum" não precisa de estágio: tira a Marca.
                                .executes(c -> definirMarca(c, 0))
                                .then(Commands.argument("estagio", IntegerArgumentType.integer(0, Limites.ESTAGIO_MAX))
                                        .executes(c -> definirMarca(c, IntegerArgumentType.getInteger(c, "estagio"))))))));

        d.register(raiz("transcender")
                .then(Commands.literal("ver").then(jogador().executes(c -> {
                    ServerPlayer p = alvo(c);
                    return ok(c, "odteste.comando.transcender.ver", DadosJogador.transcender(p), p, false);
                })))
                .then(Commands.literal("definir").then(jogador().then(valor(Limites.TRANSCENDER_MIN, Limites.TRANSCENDER_MAX)
                        .executes(c -> {
                            ServerPlayer p = alvo(c);
                            DadosJogador.definirTranscender(p, IntegerArgumentType.getInteger(c, VALOR));
                            return ok(c, "odteste.comando.transcender.definir", DadosJogador.transcender(p), p);
                        })))));
    }

    private static int definirMarca(CommandContext<CommandSourceStack> c, int estagio) throws CommandSyntaxException {
        ServerPlayer p = alvo(c);
        String nome = StringArgumentType.getString(c, "elemento");
        Elemento e = Elemento.deId(nome).orElseThrow(() -> ELEMENTO_INVALIDO.create(nome));
        DadosJogador.definirMarca(p, e, estagio);
        Elemento salvo = DadosJogador.marcaElemento(p);
        int estagioSalvo = DadosJogador.marcaEstagio(p);
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.marca.definir",
                p.getDisplayName(), Component.translatable(salvo.chaveTraducao()), estagioSalvo), true);
        return estagioSalvo;
    }

    // ---------- pedaços repetidos ----------

    private static LiteralArgumentBuilder<CommandSourceStack> raiz(String nome) {
        return Commands.literal(nome).requires(s -> s.hasPermission(2));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> jogador() {
        return Commands.argument(JOGADOR, EntityArgument.player());
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> valor(int min, int max) {
        return Commands.argument(VALOR, IntegerArgumentType.integer(min, max));
    }

    private static ServerPlayer alvo(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        return EntityArgument.getPlayer(c, JOGADOR);
    }

    /** Mensagem de "definir" vai para os outros admins também (fica no log). */
    private static int ok(CommandContext<CommandSourceStack> c, String chave, int valor, ServerPlayer p) {
        return ok(c, chave, valor, p, true);
    }

    private static int ok(CommandContext<CommandSourceStack> c, String chave, int valor, ServerPlayer p, boolean avisarAdmins) {
        c.getSource().sendSuccess(() -> Component.translatable(chave, p.getDisplayName(), valor), avisarAdmins);
        return valor;
    }
}
