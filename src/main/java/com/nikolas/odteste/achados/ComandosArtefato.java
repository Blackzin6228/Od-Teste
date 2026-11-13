package com.nikolas.odteste.achados;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.itens.Itens;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** {@code /odteste artefato dar <jogador> <artefato>} (admin, permissão 2): o evento do admin que entrega um artefato. */
public final class ComandosArtefato {
    private static final DynamicCommandExceptionType ARTEFATO_INVALIDO = new DynamicCommandExceptionType(
            nome -> Component.translatable("odteste.comando.erro.artefato", nome));

    private ComandosArtefato() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
    }

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        // Junta no mesmo /odteste dos outros comandos (o brigadier soma os filhos do mesmo nome).
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("artefato").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("dar")
                                .then(Commands.argument("jogador", EntityArgument.player())
                                        .then(Commands.argument("artefato", StringArgumentType.word())
                                                .suggests((c, b) -> SharedSuggestionProvider.suggest(IdsDados.ARTEFATOS, b))
                                                .executes(ComandosArtefato::dar))))));
    }

    private static int dar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "jogador");
        String nome = StringArgumentType.getString(c, "artefato");
        Item item = switch (nome) {
            case IdsDados.ITEM_AGULHA -> Itens.AGULHA.get();
            case IdsDados.ITEM_DEDAL -> Itens.DEDAL.get();
            case IdsDados.ITEM_SINO -> Itens.SINO.get();
            default -> throw ARTEFATO_INVALIDO.create(nome);
        };
        ItemStack pilha = new ItemStack(item);
        if (!p.getInventory().add(pilha)) p.drop(pilha, false);
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.artefato.dar", nome, p.getDisplayName()), true);
        return 1;
    }
}
