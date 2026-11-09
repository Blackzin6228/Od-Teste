package com.nikolas.odteste.estruturas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

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
     * Esquece a última anotação de uma posição (o bloco voltou ao que era sem o desfazer). Só a última: na colocação
     * em partes a mesma posição pode ter uma anotação mais antiga, de outra fatia, que precisa ficar.
     */
    void esquecer(BlockPos p) {
        long l = p.asLong();
        for (int i = posicoes.size() - 1; i >= 0; i--) {
            if (posicoes.get(i) == l) {
                posicoes.remove(i);
                originais.remove(i);
                colocados.remove(i);
                return;
            }
        }
    }

    /** A outra metade de um bloco de duas metades (planta alta, porta), ou null se o bloco não tem metade. */
    public static BlockPos outraMetade(BlockPos p, BlockState st) {
        if (!st.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) return null;
        return st.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? p.above() : p.below();
    }

    /**
     * Se em {@code p} há metade de um bloco de duas metades, tira a outra metade sem soltar item e anota no backup:
     * no desfazer as duas voltam inteiras. Chame antes de trocar {@code p}.
     * @return onde estava a outra metade, ou null se não havia
     */
    public BlockPos tirarOutraMetade(ServerLevel nivel, BlockPos p, BlockState st) {
        BlockPos outra = outraMetade(p, st);
        if (outra == null) return null;
        BlockState so = nivel.getBlockState(outra);
        if (!so.is(st.getBlock()) || !so.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                || so.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == st.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return null;
        }
        BlockState ar = Blocks.AIR.defaultBlockState();
        nivel.setBlock(outra, ar, SEM_VIZINHOS);
        anotar(outra, so, ar);
        return outra;
    }

    /** Põe um bloco e anota. Planta de duas metades no lugar sai inteira (e volta inteira no desfazer). */
    public void colocar(ServerLevel nivel, BlockPos p, BlockState novo) {
        BlockState antes = nivel.getBlockState(p);
        tirarOutraMetade(nivel, p, antes);
        nivel.setBlock(p, novo, Block.UPDATE_ALL);
        anotar(p, antes, nivel.getBlockState(p));
    }

    /** Troca o bloco avisando os jogadores, mas sem os vizinhos reagirem (planta e placa não caem no meio). */
    static final int SEM_VIZINHOS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    /**
     * Como foi o desfazer: blocos mantidos porque um jogador mexeu, e itens de jogador que estavam nos nossos baús
     * e foram soltos no chão (onde: a primeira posição, ou null).
     */
    public record Restauracao(int mantidos, int itensSoltos, BlockPos ondeSoltou) {}

    /**
     * Devolve os blocos originais, do último colocado para o primeiro. Os vizinhos só reagem no fim, com tudo
     * no lugar: placa, estandarte e planta de duas metades não caem no meio do caminho.
     * Baú nosso: o que for peça de história ({@code descartar}) sai; o resto é de jogador e cai no chão.
     */
    public Restauracao restaurar(ServerLevel nivel, Predicate<ItemStack> descartar) {
        List<BlockPos> trocados = new ArrayList<>();
        Restauracao r = restaurarDe(nivel, descartar, 0, trocados);
        for (BlockPos p : trocados) atualizarVizinhos(nivel, p);
        return r;
    }

    /**
     * Desfaz só as últimas {@code limite} anotações (para estrutura grande, um pedaço por tick) e tira elas do
     * backup: se o servidor cair no meio, o que sobrou no backup é exatamente o que falta desfazer.
     * Os vizinhos <b>não</b> reagem aqui: as posições trocadas vão para {@code trocados}, e quem chama roda
     * {@link #atualizarVizinhos} em todas no fim (senão uma tocha nossa cai da parede nossa antes da hora).
     */
    public Restauracao restaurarParte(ServerLevel nivel, Predicate<ItemStack> descartar, int limite, List<BlockPos> trocados) {
        int de = Math.max(0, posicoes.size() - Math.max(1, limite));
        Restauracao r = restaurarDe(nivel, descartar, de, trocados);
        for (int i = posicoes.size() - 1; i >= de; i--) {
            posicoes.remove(i);
            originais.remove(i);
            colocados.remove(i);
        }
        return r;
    }

    /** A primeira posição, entre as últimas {@code limite} anotações, num chunk não carregado (ou null). */
    public BlockPos descarregadaNoFim(ServerLevel nivel, int limite) {
        int de = Math.max(0, posicoes.size() - Math.max(1, limite));
        for (int i = posicoes.size() - 1; i >= de; i--) {
            BlockPos p = BlockPos.of(posicoes.get(i));
            if (!nivel.hasChunk(p.getX() >> 4, p.getZ() >> 4)) return p;
        }
        return null;
    }

    /** Depois de desfazer: o bloco e os vizinhos se ajustam (planta sem chão cai, cerca reconecta). */
    public static void atualizarVizinhos(ServerLevel nivel, BlockPos p) {
        if (!nivel.hasChunk(p.getX() >> 4, p.getZ() >> 4)) return;
        BlockState st = nivel.getBlockState(p);
        st.updateNeighbourShapes(nivel, p, Block.UPDATE_ALL);
        nivel.blockUpdated(p, st.getBlock());
    }

    private Restauracao restaurarDe(ServerLevel nivel, Predicate<ItemStack> descartar, int de, List<BlockPos> trocados) {
        int mantidos = 0;
        int soltos = 0;
        BlockPos onde = null;
        for (int i = posicoes.size() - 1; i >= de; i--) {
            BlockPos p = BlockPos.of(posicoes.get(i));
            BlockState agora = nivel.getBlockState(p);
            BlockState nosso = paleta.get(colocados.get(i));
            if (!agora.is(nosso.getBlock())) {
                mantidos++;
                continue;
            }
            BlockEntity be = nivel.getBlockEntity(p);
            if (be instanceof Container c) {
                int n = soltarItens(nivel, p, c, descartar);
                if (n > 0 && onde == null) onde = p;
                soltos += n;
            }
            if (be != null) Clearable.tryClear(be);
            nivel.setBlock(p, paleta.get(originais.get(i)), SEM_VIZINHOS);
            trocados.add(p);
        }
        return new Restauracao(mantidos, soltos, onde);
    }

    /**
     * Esvazia um baú: o que {@code descartar} aceita some, o resto cai no chão em cima do baú.
     * @return quantas pilhas caíram
     */
    public static int soltarItens(ServerLevel nivel, BlockPos p, Container c, Predicate<ItemStack> descartar) {
        int soltos = 0;
        for (int s = 0; s < c.getContainerSize(); s++) {
            ItemStack it = c.removeItemNoUpdate(s);
            if (it.isEmpty() || descartar.test(it)) continue;
            Containers.dropItemStack(nivel, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, it);
            soltos++;
        }
        c.setChanged();
        return soltos;
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
