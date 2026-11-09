package com.nikolas.odteste.estruturas;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O que estava no mundo antes de a história pôr blocos: posição, bloco original e bloco que pusemos.
 * {@link #restaurar} só devolve o original onde ainda está o bloco que pusemos: se um jogador trocou o bloco
 * depois, o dele fica (nunca destruímos construção de jogador, nem no desfazer).
 */
public final class Backup {
    private final List<BlockState> paleta = new ArrayList<>();
    private final List<Long> posicoes = new ArrayList<>();
    private final List<Integer> originais = new ArrayList<>();
    private final List<Integer> colocados = new ArrayList<>();

    public int tamanho() {
        return posicoes.size();
    }

    public boolean contem(BlockPos p) {
        return posicoes.contains(p.asLong());
    }

    public List<BlockPos> posicoes() {
        List<BlockPos> l = new ArrayList<>(posicoes.size());
        for (long p : posicoes) l.add(BlockPos.of(p));
        return l;
    }

    private int indice(BlockState s) {
        int i = paleta.indexOf(s);
        if (i >= 0) return i;
        paleta.add(s);
        return paleta.size() - 1;
    }

    public void anotar(BlockPos p, BlockState original, BlockState colocado) {
        posicoes.add(p.asLong());
        originais.add(indice(original));
        colocados.add(indice(colocado));
    }

    /**
     * Devolve os blocos originais, do último colocado para o primeiro (placa e estandarte saem antes do apoio).
     * Esvazia baú nosso antes de tirar (para não espalhar item no chão).
     * @return quantos blocos ficaram como estão porque um jogador já tinha mexido
     */
    public int restaurar(ServerLevel nivel) {
        int mantidos = 0;
        for (int i = posicoes.size() - 1; i >= 0; i--) {
            BlockPos p = BlockPos.of(posicoes.get(i));
            BlockState agora = nivel.getBlockState(p);
            BlockState nosso = paleta.get(colocados.get(i));
            if (!agora.is(nosso.getBlock())) {
                mantidos++;
                continue;
            }
            BlockEntity be = nivel.getBlockEntity(p);
            if (be != null) Clearable.tryClear(be);
            nivel.setBlock(p, paleta.get(originais.get(i)), Block.UPDATE_ALL);
        }
        return mantidos;
    }

    public CompoundTag salvar() {
        CompoundTag t = new CompoundTag();
        ListTag pal = new ListTag();
        for (BlockState s : paleta) pal.add(NbtUtils.writeBlockState(s));
        t.put("paleta", pal);
        long[] pos = new long[posicoes.size()];
        int[] ori = new int[posicoes.size()];
        int[] col = new int[posicoes.size()];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = posicoes.get(i);
            ori[i] = originais.get(i);
            col[i] = colocados.get(i);
        }
        t.put("pos", new LongArrayTag(pos));
        t.put("original", new IntArrayTag(ori));
        t.put("colocado", new IntArrayTag(col));
        return t;
    }

    public static Backup carregar(CompoundTag t) {
        Backup b = new Backup();
        ListTag pal = t.getList("paleta", Tag.TAG_COMPOUND);
        for (int i = 0; i < pal.size(); i++) {
            b.paleta.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), pal.getCompound(i)));
        }
        long[] pos = t.getLongArray("pos");
        int[] ori = t.getIntArray("original");
        int[] col = t.getIntArray("colocado");
        int n = Math.min(pos.length, Math.min(ori.length, col.length));
        for (int i = 0; i < n; i++) {
            // Índice fora da paleta (arquivo estragado): pula a posição em vez de crashar.
            if (ori[i] < 0 || ori[i] >= b.paleta.size() || col[i] < 0 || col[i] >= b.paleta.size()) continue;
            b.posicoes.add(pos[i]);
            b.originais.add(ori[i]);
            b.colocados.add(col[i]);
        }
        return b;
    }
}
