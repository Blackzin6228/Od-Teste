package com.nikolas.odteste.itens;

import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.registro.Registro;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Itens do mod (IDs em docs/ids.md). Nenhum tem receita: espada e artefatos vêm de comando, evento ou ritual;
 * a matéria cai das criaturas.
 */
public final class Itens {
    public static final DeferredItem<Rubra> RUBRA = Registro.ITENS.register(IdsDados.ITEM_RUBRA, Rubra::new);
    public static final DeferredItem<AgulhaDeCerzir> AGULHA =
            Registro.ITENS.register(IdsDados.ITEM_AGULHA, AgulhaDeCerzir::new);
    public static final DeferredItem<DedalDentado> DEDAL = Registro.ITENS.register(IdsDados.ITEM_DEDAL, DedalDentado::new);
    public static final DeferredItem<SinoDeMatadouro> SINO =
            Registro.ITENS.register(IdsDados.ITEM_SINO, SinoDeMatadouro::new);
    public static final DeferredItem<Item> MATERIA_SANGUE = Registro.ITENS.register(IdsDados.ITEM_MATERIA_SANGUE,
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)) {
                @Override
                public void appendHoverText(ItemStack item, Item.TooltipContext contexto, List<Component> linhas,
                                            TooltipFlag flag) {
                    linhas.add(Component.translatable("item.odteste.materia_sangue_bruta.objeto")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
            });

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ABA = Registro.ABAS.register(IdsDados.ABA,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.odteste"))
                    .icon(() -> new ItemStack(RUBRA.get()))
                    .displayItems((parametros, saida) -> {
                        saida.accept(RUBRA.get());
                        saida.accept(AGULHA.get());
                        saida.accept(DEDAL.get());
                        saida.accept(SINO.get());
                        saida.accept(MATERIA_SANGUE.get());
                    })
                    .build());

    private Itens() {}

    /** Força o carregamento da classe (registros) e liga os eventos dos artefatos. */
    public static void iniciar() {
        DedalDentado.iniciar();
    }

    /** Todos os Artefatos Paranormais (para a recarga compartilhada). */
    public static List<Item> artefatos() {
        return List.of(AGULHA.get(), DEDAL.get(), SINO.get());
    }
}
