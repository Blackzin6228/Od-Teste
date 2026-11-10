package com.nikolas.odteste.blocos.capela;

import com.mojang.serialization.MapCodec;
import com.nikolas.odteste.sistemas.Lucidez;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A vela da capela: a única luz que acende sozinha na Vila Calada. Quem fica perto dela recupera Lucidez.
 * Sem lag: cada vela checa uma vez a cada 2 s (em tick próprio) e só olha os jogadores da mesma dimensão
 * que estão no raio. Só roda com o chunk carregado, ou seja, só quando tem alguém por perto.
 */
public final class VelaDaCapela extends Block implements EntityBlock, LiquidBlockContainer {
    public static final MapCodec<VelaDaCapela> CODEC = simpleCodec(VelaDaCapela::new);
    /** Mesmo formato de uma vela do jogo. */
    private static final VoxelShape FORMATO = Block.box(7, 0, 7, 9, 6, 9);

    public VelaDaCapela(Properties propriedades) {
        super(propriedades);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState estado, BlockGetter nivel, BlockPos pos, CollisionContext contexto) {
        return FORMATO;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState estado) {
        return new VelaDaCapelaDados(pos, estado);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level nivel, BlockState estado,
                                                                  BlockEntityType<T> tipo) {
        if (nivel.isClientSide || tipo != BlocosCapela.VELA_DADOS.get()) return null;
        return (n, pos, s, dados) -> checar(n, pos);
    }

    /** Água e lava não entram nem apagam a vela (peça de estrutura). */
    @Override
    public boolean canPlaceLiquid(Player jogador, BlockGetter nivel, BlockPos pos, BlockState estado,
                                  Fluid liquido) {
        return false;
    }

    /** O balde pergunta isto antes: o padrão aceita bloco não sólido, e aí a lava apagaria a vela. */
    @Override
    protected boolean canBeReplaced(BlockState estado, Fluid liquido) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor nivel, BlockPos pos, BlockState estado, FluidState liquido) {
        return false;
    }

    /** Uma vez a cada 2 s: quem está perto da chama recupera um pouco de Lucidez. */
    private static void checar(Level nivel, BlockPos pos) {
        if (!(nivel instanceof ServerLevel servidor)) return;
        if (!RegrasCapela.vezDeChecar(servidor.getGameTime(), pos.asLong())) return;
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        for (ServerPlayer p : servidor.players()) {
            if (!p.isAlive() || p.isSpectator() || p.isCreative()) continue;
            // Várias velas não somam: o freio dá um ganho só por jogador a cada 2 s.
            if (RegrasCapela.pertoDaVela(p.distanceToSqr(x, y, z)) && BlocosCapela.FREIO_VELA.liberar(p)) {
                Lucidez.recuperar(p, RegrasCapela.VELA_GANHO);
            }
        }
    }

    /** Chama e fumaça como numa vela acesa do jogo (só no cliente, chamado pelo próprio jogo). */
    @Override
    public void animateTick(BlockState estado, Level nivel, BlockPos pos, RandomSource sorte) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        if (sorte.nextFloat() < 0.3F) nivel.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        nivel.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0, 0, 0);
    }
}
