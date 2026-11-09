package com.nikolas.odteste.blocos.capela;

import com.mojang.serialization.MapCodec;
import com.nikolas.odteste.sistemas.Lucidez;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * Poça de sangue que "pulsa" (praça e curral da Vila Calada). Quem pisa ou fica nela perde um pouco de Lucidez.
 * Sem lag: o jogo já avisa quando alguém está dentro do bloco; aqui só se tira Lucidez uma vez a cada 2 s por
 * jogador (Freio), uma vez só mesmo que ele esteja em cima de várias poças ao mesmo tempo.
 */
public final class PocaDeSangue extends Block implements LiquidBlockContainer {
    public static final MapCodec<PocaDeSangue> CODEC = simpleCodec(PocaDeSangue::new);
    /** Fina como um tapete. */
    private static final VoxelShape FORMATO = Block.box(0, 0, 0, 16, 1, 16);
    private static final DustParticleOptions PULSO = new DustParticleOptions(new Vector3f(0.45F, 0.02F, 0.02F), 0.8F);

    public PocaDeSangue(Properties propriedades) {
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
    protected void entityInside(BlockState estado, Level nivel, BlockPos pos, Entity entidade) {
        if (nivel.isClientSide || !(entidade instanceof ServerPlayer p)) return;
        if (!p.isAlive() || p.isSpectator() || p.isCreative()) return;
        // O jogo só chama isto quando o jogador se mexe (pacote de movimento), então não dá para esperar um tick
        // exato: perde se já passaram 2 s desde a última perda dele, somando todas as poças.
        if (BlocosCapela.FREIO_POCA.liberar(p)) Lucidez.perder(p, RegrasCapela.POCA_PERDA);
    }

    /** Água e lava não entram nem levam a poça embora (peça de estrutura). */
    @Override
    public boolean canPlaceLiquid(Player jogador, BlockGetter nivel, BlockPos pos, BlockState estado, Fluid liquido) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor nivel, BlockPos pos, BlockState estado, FluidState liquido) {
        return false;
    }

    /** Partícula rala vermelha subindo de vez em quando (só no cliente, chamado pelo próprio jogo). */
    @Override
    public void animateTick(BlockState estado, Level nivel, BlockPos pos, RandomSource sorte) {
        if (sorte.nextInt(4) != 0) return;
        nivel.addParticle(PULSO, pos.getX() + sorte.nextDouble(), pos.getY() + 0.1, pos.getZ() + sorte.nextDouble(),
                0, 0.02, 0);
    }
}
