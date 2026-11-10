package com.nikolas.odteste.aparelhos;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.nikolas.odteste.sinais.Sinais;

/**
 * Analisador de Sinal: aparelho de bancada que ouve o que o ouvido não pega.
 * Estados (nomes salvos no mundo, ver docs/ids.md): {@code facing}, {@code ligado} (tem energia e alguém perto)
 * e {@code sinal} (achou sinal a até 8 blocos). Modelo provisório: o Modelador faz o definitivo.
 */
public class AnalisadorSinalBloco extends BaseEntityBlock {
    public static final MapCodec<AnalisadorSinalBloco> CODEC = simpleCodec(AnalisadorSinalBloco::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIGADO = BooleanProperty.create("ligado");
    public static final BooleanProperty SINAL = BooleanProperty.create("sinal");

    private static final VoxelShape FORMA = Block.box(1, 0, 1, 15, 11, 15);

    public AnalisadorSinalBloco(Properties propriedades) {
        super(propriedades);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIGADO, false)
                .setValue(SINAL, false));
    }

    /** Luz do bloco: apagado, luz fraca ligado, mais forte com sinal. */
    public static int luz(BlockState estado) {
        if (estado.getValue(SINAL)) return 7;
        return estado.getValue(LIGADO) ? 3 : 0;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> construtor) {
        construtor.add(FACING, LIGADO, SINAL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext contexto) {
        return defaultBlockState().setValue(FACING, contexto.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState estado, Rotation rotacao) {
        return estado.setValue(FACING, rotacao.rotate(estado.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState estado, Mirror espelho) {
        return estado.rotate(espelho.getRotation(estado.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState estado, BlockGetter nivel, BlockPos pos, CollisionContext contexto) {
        return FORMA;
    }

    @Override
    protected RenderShape getRenderShape(BlockState estado) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState estado) {
        return new AnalisadorSinalEntidade(pos, estado);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level nivel, BlockState estado,
                                                                  BlockEntityType<T> tipo) {
        if (nivel.isClientSide) return null;
        return createTickerHelper(tipo, Aparelhos.ANALISADOR_ENTIDADE.get(), AnalisadorSinalEntidade::tickServidor);
    }

    /** Com um item que tem sinal (livro marcado etc.): lê o item. Outro item: cai para a leitura em volta. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack item, BlockState estado, Level nivel, BlockPos pos,
                                              Player jogador, InteractionHand mao, BlockHitResult acerto) {
        if (nivel.isClientSide) return ItemInteractionResult.SUCCESS;
        if (Sinais.doItem(item).isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (nivel.getBlockEntity(pos) instanceof AnalisadorSinalEntidade be && nivel instanceof ServerLevel s) {
            be.ler(s, jogador, item);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState estado, Level nivel, BlockPos pos, Player jogador,
                                               BlockHitResult acerto) {
        if (nivel.isClientSide) return InteractionResult.SUCCESS;
        if (nivel.getBlockEntity(pos) instanceof AnalisadorSinalEntidade be && nivel instanceof ServerLevel s) {
            nivel.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, 1.4f);
            be.ler(s, jogador, ItemStack.EMPTY);
        }
        return InteractionResult.CONSUME;
    }
}
