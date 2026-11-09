package com.nikolas.odteste.aparelhos;

import com.nikolas.odteste.registro.Registro;
import com.nikolas.odteste.sinais.SinaisNoMundo;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Nossos aparelhos (decisão "máquinas = misto", 2026-11-08). Só usam NeoForge: energia FE pela capacidade
 * padrão, então ligam em cabo, gerador ou bateria de qualquer mod. IDs em docs/ids.md.
 */
public final class Aparelhos {
    public static final DeferredBlock<AnalisadorSinalBloco> ANALISADOR = Registro.BLOCOS.register(
            "analisador_sinal",
            () -> new AnalisadorSinalBloco(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.5f, 6f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .noOcclusion()
                    .lightLevel(AnalisadorSinalBloco::luz)));

    public static final DeferredItem<BlockItem> ANALISADOR_ITEM = Registro.ITENS.register(
            "analisador_sinal",
            () -> new BlockItem(ANALISADOR.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack item, TooltipContext contexto, List<Component> linhas,
                                            TooltipFlag flag) {
                    linhas.add(Component.translatable("odteste.analisador.dica")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                }
            });

    @SuppressWarnings("DataFlowIssue") // o tipo de dado do bloco não usa "data fixer"
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnalisadorSinalEntidade>>
            ANALISADOR_ENTIDADE = Registro.BLOCOS_COM_DADOS.register("analisador_sinal",
                    () -> BlockEntityType.Builder.of(AnalisadorSinalEntidade::new, ANALISADOR.get()).build(null));

    private Aparelhos() {}

    public static void iniciar(IEventBus barramento) {
        barramento.addListener(Aparelhos::capacidades);
        barramento.addListener(Aparelhos::abaCriativo);
        SinaisNoMundo.iniciar();
    }

    /** Energia FE: aceita de qualquer lado, de qualquer mod. */
    private static void capacidades(RegisterCapabilitiesEvent evento) {
        evento.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ANALISADOR_ENTIDADE.get(),
                (be, lado) -> be.bateria());
    }

    /** Sem aba nossa por enquanto: entra na aba vanilla de blocos funcionais. */
    private static void abaCriativo(BuildCreativeModeTabContentsEvent evento) {
        if (evento.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) evento.accept(ANALISADOR_ITEM.get());
    }
}
