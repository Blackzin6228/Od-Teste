package com.nikolas.odteste.itens;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * Martelo do Ferreiro (Vila Calada, Dobre). Sozinho não faz nada: o que conta é bater no sino rachado com ele
 * ({@code historia/vilacalada/Dobre}). Fica no campanário, ao lado do atril ({@code /odteste vila_calada preparar}).
 *
 * <p>Modelo provisório: {@code assets/odteste/models/item/martelo_do_ferreiro.json} com a textura
 * {@code textures/item/martelo_do_ferreiro.png}. O Modelador troca só esses dois arquivos (pode virar modelo 3D do
 * Blockbench no mesmo caminho), sem mexer em código.
 */
public final class MarteloDoFerreiro extends Item {
    public MarteloDoFerreiro() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack item, Item.TooltipContext contexto, List<Component> linhas, TooltipFlag flag) {
        linhas.add(Component.translatable("item.odteste.martelo_do_ferreiro.objeto")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
