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
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.nikolas.odteste.sistemas.Abalo;
import com.nikolas.odteste.sistemas.Exposicao;
import com.nikolas.odteste.sistemas.Fonte;
import com.nikolas.odteste.sistemas.Lucidez;
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

    private static final DynamicCommandExceptionType ESTAGIO_FALTANDO = new DynamicCommandExceptionType(
            nome -> Component.translatable("odteste.comando.erro.estagio", nome));

    private static final DynamicCommandExceptionType FONTE_INVALIDA = new DynamicCommandExceptionType(
            nome -> Component.translatable("odteste.comando.erro.fonte", nome));

    private static final DynamicCommandExceptionType ABALO_INVALIDO = new DynamicCommandExceptionType(
            nome -> Component.translatable("odteste.comando.erro.abalo", nome));

    private static final String INTENSIDADE = "intensidade";

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
                        }))))
                // Simula um contato com o paranormal (criatura, relíquia...) pelo mesmo caminho do conteúdo.
                // Com elemento, soma contato: é assim que se testa a Marca aparecendo sozinha.
                .then(Commands.literal("contato").then(jogador()
                        .then(Commands.argument("fonte", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Fonte.values()).map(Fonte::id), b))
                                .executes(c -> contato(c, false, false))
                                .then(Commands.argument("elemento", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(Elemento.values()).map(Elemento::id), b))
                                        .executes(c -> contato(c, true, false))
                                        .then(Commands.argument(INTENSIDADE, FloatArgumentType.floatArg(0f, 5f))
                                                .executes(c -> contato(c, true, true))))))));

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
                        }))))
                // Simula um abalo (batalha com relíquia, aparição, mob de Sangue) pelo caminho do conteúdo.
                .then(Commands.literal("abalar").then(jogador()
                        .then(Commands.argument("abalo", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(Abalo.values()).map(Abalo::id), b))
                                .executes(c -> abalar(c, 1f))
                                .then(Commands.argument(INTENSIDADE, FloatArgumentType.floatArg(0f, 5f))
                                        .executes(c -> abalar(c, FloatArgumentType.getFloat(c, INTENSIDADE))))))));

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
                                // Sem estágio: só "nenhum" (tira a Marca). Com elemento, o estágio é obrigatório
                                // e dá erro claro, para o admin nunca salvar um estágio que não pediu.
                                .executes(c -> definirMarca(c, null))
                                .then(Commands.argument("estagio", IntegerArgumentType.integer(Limites.ESTAGIO_MIN, Limites.ESTAGIO_MAX))
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

    /**
     * @param estagio null quando o comando veio sem estágio. Só vale para "nenhum";
     *                com elemento, dá erro pedindo o estágio (1 a 4).
     *                Com "nenhum", um estágio digitado é ignorado (sem Marca, o estágio é sempre 0).
     */
    private static int definirMarca(CommandContext<CommandSourceStack> c, Integer estagio) throws CommandSyntaxException {
        ServerPlayer p = alvo(c);
        String nome = StringArgumentType.getString(c, "elemento");
        Elemento e = Elemento.deId(nome).orElseThrow(() -> ELEMENTO_INVALIDO.create(nome));
        if (estagio == null && e != Elemento.NENHUM) {
            throw ESTAGIO_FALTANDO.create(e.id());
        }
        DadosJogador.definirMarca(p, e, estagio == null ? 0 : estagio);
        Elemento salvo = DadosJogador.marcaElemento(p);
        int estagioSalvo = DadosJogador.marcaEstagio(p);
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.marca.definir",
                p.getDisplayName(), Component.translatable(salvo.chaveTraducao()), estagioSalvo), true);
        return estagioSalvo;
    }

    private static int contato(CommandContext<CommandSourceStack> c, boolean comElemento, boolean comIntensidade)
            throws CommandSyntaxException {
        ServerPlayer p = alvo(c);
        String nome = StringArgumentType.getString(c, "fonte");
        Fonte f = Fonte.deId(nome).orElseThrow(() -> FONTE_INVALIDA.create(nome));
        Elemento e = Elemento.NENHUM;
        if (comElemento) {
            String el = StringArgumentType.getString(c, "elemento");
            e = Elemento.deId(el).orElseThrow(() -> ELEMENTO_INVALIDO.create(el));
        }
        float intensidade = comIntensidade ? FloatArgumentType.getFloat(c, INTENSIDADE) : 1f;
        int ganho = Exposicao.contato(p, f, e, intensidade);
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.exposicao.contato",
                p.getDisplayName(), f.id(), ganho, DadosJogador.exposicao(p)), true);
        return ganho;
    }

    private static int abalar(CommandContext<CommandSourceStack> c, float intensidade) throws CommandSyntaxException {
        ServerPlayer p = alvo(c);
        String nome = StringArgumentType.getString(c, "abalo");
        Abalo a = Abalo.deId(nome).orElseThrow(() -> ABALO_INVALIDO.create(nome));
        Lucidez.abalar(p, a, intensidade);
        return ok(c, "odteste.comando.lucidez.abalar", DadosJogador.lucidez(p), p);
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
