package com.nikolas.odteste.estruturas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * O mundo de verdade visto pela {@link Espaco}. Nunca carrega nem gera chunk: chunk que não está carregado
 * volta como {@link Espaco.Tipo#NAO_CARREGADO} e a colocação recusa.
 * Use um por checagem (guarda o claim de cada chunk enquanto confere).
 */
public final class MundoReal implements Espaco.Mundo, Espaco.Chao {
    /** Blocos que a história pode ocupar além do ar (grama alta, samambaia, neve fina...). */
    public static final TagKey<Block> SUBSTITUIVEL = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_SUBSTITUIVEL));

    private final ServerLevel nivel;
    private final Map<Long, Boolean> donos = new HashMap<>();
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    public MundoReal(ServerLevel nivel) {
        this.nivel = nivel;
    }

    public ServerLevel nivel() {
        return nivel;
    }

    public boolean carregado(int x, int z) {
        return nivel.hasChunk(x >> 4, z >> 4);
    }

    public boolean temDono(int x, int z) {
        int cx = x >> 4;
        int cz = z >> 4;
        return donos.computeIfAbsent(ChunkPos.asLong(cx, cz), k -> Protecao.temDono(nivel, cx, cz));
    }

    @Override
    public Espaco.Tipo tipo(int x, int y, int z) {
        if (y < nivel.getMinBuildHeight() || y >= nivel.getMaxBuildHeight()) return Espaco.Tipo.FORA_DO_MUNDO;
        if (!carregado(x, z)) return Espaco.Tipo.NAO_CARREGADO;
        if (temDono(x, z)) return Espaco.Tipo.PROTEGIDO;
        BlockState s = nivel.getBlockState(cursor.set(x, y, z));
        return classificar(s);
    }

    public Espaco.Tipo tipo(BlockPos p) {
        return tipo(p.getX(), p.getY(), p.getZ());
    }

    public static Espaco.Tipo classificar(BlockState s) {
        if (s.hasBlockEntity()) return Espaco.Tipo.COM_DADOS;
        if (!s.getFluidState().isEmpty()) return Espaco.Tipo.LIQUIDO;
        if (s.isAir()) return Espaco.Tipo.AR;
        if (s.is(SUBSTITUIVEL)) return Espaco.Tipo.SUBSTITUIVEL;
        return Espaco.Tipo.OCUPADO;
    }

    /** Bloco firme por cima (dá para apoiar algo em cima dele)? */
    public boolean firme(BlockPos p) {
        if (!carregado(p.getX(), p.getZ())) return false;
        BlockState s = nivel.getBlockState(p);
        return s.getFluidState().isEmpty() && s.isFaceSturdy(nivel, p, net.minecraft.core.Direction.UP);
    }

    @Override
    public int y(int x, int z) {
        if (!carregado(x, z)) return nivel.getMinBuildHeight();
        return nivel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }
}
