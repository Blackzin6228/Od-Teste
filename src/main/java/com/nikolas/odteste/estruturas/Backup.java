package com.nikolas.odteste.estruturas;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    /** Estado -> índice na paleta (a paleta pode crescer bastante numa estrutura grande). */
    private final Object2IntOpenHashMap<BlockState> indicePaleta = new Object2IntOpenHashMap<>();
    // Listas de primitivos (fastutil): numa estrutura de 500 mil blocos, nada de um objeto por posição.
    private final LongArrayList posicoes = new LongArrayList();
    private final IntArrayList originais = new IntArrayList();
    private final IntArrayList colocados = new IntArrayList();
    /**
     * Posição -> índice da última anotação dela. Só memória, montado na primeira consulta e jogado fora quando
     * uma anotação sai (aí os índices mudam). Assim {@link #contem} e {@link #ehNosso} custam O(1) por bloco.
     */
    private Long2IntOpenHashMap ultima;

    public Backup() {
        indicePaleta.defaultReturnValue(-1);
    }

    public int tamanho() {
        return posicoes.size();
    }

    public boolean contem(BlockPos p) {
        return mapa().containsKey(p.asLong());
    }

    /**
     * A posição está no backup e ainda tem o bloco que nós pusemos por último (mesmo teste do desfazer)?
     * Serve para a fatia que parou no meio: o que já entrou é nosso, não é bloqueio.
     */
    public boolean ehNosso(BlockPos p, BlockState agora) {
        int i = mapa().get(p.asLong());
        return i >= 0 && agora.is(paleta.get(colocados.getInt(i)).getBlock());
    }

    private Long2IntOpenHashMap mapa() {
        if (ultima == null) {
            ultima = new Long2IntOpenHashMap(posicoes.size());
            ultima.defaultReturnValue(-1);
            for (int i = 0; i < posicoes.size(); i++) ultima.put(posicoes.getLong(i), i);
        }
        return ultima;
    }

    public List<BlockPos> posicoes() {
        List<BlockPos> l = new ArrayList<>(posicoes.size());
        for (int i = 0; i < posicoes.size(); i++) l.add(BlockPos.of(posicoes.getLong(i)));
        return l;
    }

    private int indice(BlockState s) {
        int i = indicePaleta.getInt(s);
        if (i >= 0) return i;
        paleta.add(s);
        indicePaleta.put(s, paleta.size() - 1);
        return paleta.size() - 1;
    }

    public void anotar(BlockPos p, BlockState original, BlockState colocado) {
        posicoes.add(p.asLong());
        originais.add(indice(original));
        colocados.add(indice(colocado));
        if (ultima != null) ultima.put(p.asLong(), posicoes.size() - 1);
    }

    /** Corta as anotações a partir de {@code de} (as últimas). */
    private void cortar(int de) {
        if (de >= posicoes.size()) return;
        posicoes.size(de);
        originais.size(de);
        colocados.size(de);
        ultima = null;
    }

    /**
     * Esquece a última anotação de uma posição (o bloco voltou ao que era sem o desfazer). Só a última: na colocação
     * em partes a mesma posição pode ter uma anotação mais antiga, de outra fatia, que precisa ficar.
     */
    void esquecer(BlockPos p) {
        long l = p.asLong();
        for (int i = posicoes.size() - 1; i >= 0; i--) {
            if (posicoes.getLong(i) == l) {
                posicoes.removeLong(i);
                originais.removeInt(i);
                colocados.removeInt(i);
                ultima = null;
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
        LongArrayList trocados = new LongArrayList();
        Restauracao r = restaurarDe(nivel, descartar, 0, trocados);
        for (int i = 0; i < trocados.size(); i++) atualizarVizinhos(nivel, BlockPos.of(trocados.getLong(i)));
        return r;
    }

    /**
     * Desfaz só as últimas {@code limite} anotações (para estrutura grande, um pedaço por tick) e tira elas do
     * backup: se o servidor cair no meio, o que sobrou no backup é exatamente o que falta desfazer.
     * Os vizinhos <b>não</b> reagem aqui: as posições trocadas vão para {@code trocados}, e quem chama roda
     * {@link #atualizarVizinhos} em todas no fim (senão uma tocha nossa cai da parede nossa antes da hora).
     */
    public Restauracao restaurarParte(ServerLevel nivel, Predicate<ItemStack> descartar, int limite, LongArrayList trocados) {
        int de = Math.max(0, posicoes.size() - Math.max(1, limite));
        Restauracao r = restaurarDe(nivel, descartar, de, trocados);
        cortar(de);
        return r;
    }

    /** A primeira posição, entre as últimas {@code limite} anotações, num chunk não carregado (ou null). */
    public BlockPos descarregadaNoFim(ServerLevel nivel, int limite) {
        int de = Math.max(0, posicoes.size() - Math.max(1, limite));
        for (int i = posicoes.size() - 1; i >= de; i--) {
            BlockPos p = BlockPos.of(posicoes.getLong(i));
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

    private Restauracao restaurarDe(ServerLevel nivel, Predicate<ItemStack> descartar, int de, LongArrayList trocados) {
        int mantidos = 0;
        int soltos = 0;
        BlockPos onde = null;
        for (int i = posicoes.size() - 1; i >= de; i--) {
            BlockPos p = BlockPos.of(posicoes.getLong(i));
            BlockState agora = nivel.getBlockState(p);
            BlockState nosso = paleta.get(colocados.getInt(i));
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
            nivel.setBlock(p, paleta.get(originais.getInt(i)), SEM_VIZINHOS);
            trocados.add(p.asLong());
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
        // Mesmo formato de antes (pos/original/colocado): o que já está salvo no mundo continua lendo igual.
        t.put("pos", new LongArrayTag(posicoes.toLongArray()));
        t.put("original", new IntArrayTag(originais.toIntArray()));
        t.put("colocado", new IntArrayTag(colocados.toIntArray()));
        return t;
    }

    public static Backup carregar(CompoundTag t) {
        Backup b = new Backup();
        ListTag pal = t.getList("paleta", Tag.TAG_COMPOUND);
        for (int i = 0; i < pal.size(); i++) {
            BlockState s = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), pal.getCompound(i));
            b.paleta.add(s);
            // Paleta salva pode repetir estado (bloco de mod que sumiu vira ar): fica o primeiro índice.
            b.indicePaleta.putIfAbsent(s, i);
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
