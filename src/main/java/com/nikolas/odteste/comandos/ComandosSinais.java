package com.nikolas.odteste.comandos;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nikolas.odteste.sinais.FontesFixas;
import com.nikolas.odteste.sinais.RegrasSinal;
import com.nikolas.odteste.sinais.Sinais;
import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Fontes de sinal fixas (mesa Integrador), só admin (a permissão vem do {@code /odteste} em ComandosHistoria):
 *
 * /odteste sinal fonte adicionar <id> <tipo> [ponto] · remover <id> · lista
 *
 * {@code tipo} escolhe os textos ({@code odteste.sinal.<tipo>.*}, ex.: {@code sino_vila}). Sem ponto: onde o admin está.
 */
public final class ComandosSinais {
    private static final String ID = "id";
    private static final String TIPO = "tipo";
    private static final String PONTO = "ponto";

    private ComandosSinais() {}

    public static LiteralArgumentBuilder<CommandSourceStack> sinal() {
        return Commands.literal("sinal").then(Commands.literal("fonte")
                .then(Commands.literal("adicionar")
                        .then(Commands.argument(ID, StringArgumentType.word())
                                .then(Commands.argument(TIPO, StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                RegrasSinal.tipos(), b))
                                        .executes(c -> adicionar(c, BlockPos.containing(c.getSource().getPosition())))
                                        .then(Commands.argument(PONTO, BlockPosArgument.blockPos())
                                                .executes(c -> adicionar(c,
                                                        BlockPosArgument.getBlockPos(c, PONTO)))))))
                .then(Commands.literal("remover")
                        .then(Commands.argument(ID, StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Sinais.fontesFixas(c.getSource().getServer()).stream()
                                                .map(FontesFixas.Fonte::id), b))
                                .executes(ComandosSinais::remover)))
                .then(Commands.literal("lista").executes(ComandosSinais::lista)));
    }

    private static int adicionar(CommandContext<CommandSourceStack> c, BlockPos pos) throws CommandSyntaxException {
        String id = StringArgumentType.getString(c, ID);
        String tipo = StringArgumentType.getString(c, TIPO);
        CommandSourceStack s = c.getSource();
        Sinais.Resultado r = Sinais.registrarFonteFixa(s.getLevel(), id, pos, tipo);
        switch (r) {
            case NOME_INVALIDO -> {
                s.sendFailure(Component.translatable("odteste.sinal.fonte.erro.nome",
                        RegrasSinal.nomeValido(id) ? tipo : id));
                return 0;
            }
            case TIPO_DESCONHECIDO -> {
                s.sendFailure(Component.translatableWithFallback("odteste.sinal.fonte.erro.tipo",
                        "Tipo de sinal desconhecido: %s. Tipos: %s", tipo, String.join(", ", RegrasSinal.tipos())));
                return 0;
            }
            case CHEIO -> {
                s.sendFailure(Component.translatable("odteste.sinal.fonte.erro.cheio", RegrasSinal.MAX_FONTES));
                return 0;
            }
            default -> { }
        }
        s.sendSuccess(() -> Component.translatable("odteste.sinal.fonte.adicionada",
                id, tipo, pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    private static int remover(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, ID);
        if (!Sinais.removerFonteFixa(c.getSource().getServer(), id)) {
            c.getSource().sendFailure(Component.translatable("odteste.sinal.fonte.erro.nao_existe", id));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.translatable("odteste.sinal.fonte.removida", id), true);
        return 1;
    }

    private static int lista(CommandContext<CommandSourceStack> c) {
        Collection<FontesFixas.Fonte> todas = Sinais.fontesFixas(c.getSource().getServer());
        if (todas.isEmpty()) {
            c.getSource().sendSuccess(() -> Component.translatable("odteste.sinal.fonte.nenhuma")
                    .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }
        for (FontesFixas.Fonte f : todas) {
            c.getSource().sendSuccess(() -> Component.translatable("odteste.sinal.fonte.item", f.id(), f.tipo(),
                    f.pos().getX(), f.pos().getY(), f.pos().getZ(), f.dimensao().location().toString()), false);
        }
        return todas.size();
    }
}
