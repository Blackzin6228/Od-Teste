package com.nikolas.odteste.blocos.capela;

import com.nikolas.odteste.registro.Registro;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Blocos da Vila Calada: a vela da capela (refúgio) e a poça de sangue (perigo).
 * IDs para sempre (docs/ids.md). Os dois são peças de estrutura: não quebram no sobrevivência, não caem como item,
 * não são empurrados por pistão. O admin coloca por /setblock, /give (item escondido) ou pela schematic.
 */
public final class BlocosCapela {
    private static final String ID_VELA = BlocosCapelaIds.VELA;
    private static final String ID_POCA = BlocosCapelaIds.POCA;

    /** Força de "inquebrável" do jogo (igual à rocha-mãe). */
    private static final float INQUEBRAVEL = -1.0F;
    private static final float RESISTENCIA = 3_600_000.0F;

    public static final DeferredBlock<VelaDaCapela> VELA = Registro.BLOCOS.registerBlock(ID_VELA, VelaDaCapela::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(INQUEBRAVEL, RESISTENCIA)
                    .noLootTable()
                    .noOcclusion()
                    .sound(SoundType.CANDLE)
                    .lightLevel(estado -> RegrasCapela.VELA_LUZ)
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<PocaDeSangue> POCA = Registro.BLOCOS.registerBlock(ID_POCA, PocaDeSangue::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.CRIMSON_NYLIUM)
                    .strength(INQUEBRAVEL, RESISTENCIA)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()
                    .sound(SoundType.HONEY_BLOCK)
                    .pushReaction(PushReaction.BLOCK));

    /** Só existe para a vela checar a cada 2 s. Não guarda nada. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VelaDaCapelaDados>> VELA_DADOS =
            Registro.BLOCOS_COM_DADOS.register(ID_VELA,
                    () -> BlockEntityType.Builder.of(VelaDaCapelaDados::new, VELA.get()).build(null));

    public static final DeferredItem<BlockItem> ITEM_VELA = Registro.ITENS.registerSimpleBlockItem(VELA);
    public static final DeferredItem<BlockItem> ITEM_POCA = Registro.ITENS.registerSimpleBlockItem(POCA);

    private BlocosCapela() {}

    /** Força o carregamento da classe (registros). Chamado antes de Registro.registrar. */
    public static void iniciar() {}

    /** Sem receita: fora do criativo e escondidos no JEI/EMI. */
    public static List<Item> escondidos() {
        return List.of(ITEM_VELA.get(), ITEM_POCA.get());
    }
}
