package com.nikolas.odteste.blocos.capela;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Dados da vela: vazio. Existe só para o jogo chamar a checagem da vela enquanto o chunk está carregado. */
public final class VelaDaCapelaDados extends BlockEntity {
    public VelaDaCapelaDados(BlockPos pos, BlockState estado) {
        super(BlocosCapela.VELA_DADOS.get(), pos, estado);
    }
}
