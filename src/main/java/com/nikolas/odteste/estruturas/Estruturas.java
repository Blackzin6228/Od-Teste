package com.nikolas.odteste.estruturas;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Coloca estrutura por template NBT (arquivo {@code data/<mod>/structure/<id>.nbt}, o mesmo do Structure Block),
 * sem geração de mundo. Só ocupa ar e blocos da tag {@code odteste:substituivel}; se não couber, procura outro
 * ponto perto; chunk com dono ou não carregado barra. Tudo fica num {@link Backup} para desfazer.
 */
public final class Estruturas {
    /** Maior estrutura aceita (blocos da caixa). Acima disso trava o tick: dividir o template. */
    public static final long VOLUME_MAXIMO = 16384;
    /** Busca de outro ponto quando não cabe: raio, passo e tentativas (barato: a checagem para no 1º bloqueio). */
    public static final int RAIO_BUSCA = 32;
    public static final int PASSO_BUSCA = 3;
    public static final int MAX_TENTATIVAS = 400;

    /** Resultado de uma tentativa: onde ficou (ou onde está o bloqueio) e a colocação, se deu certo. */
    public record Tentativa(boolean ok, BlockPos ponto, Espaco.Resultado checagem, boolean mudouDeLugar,
                            Optional<Backup> backup, String erro) {}

    private Estruturas() {}

    public static Optional<StructureTemplate> template(ServerLevel nivel, ResourceLocation id) {
        return nivel.getStructureManager().get(id);
    }

    public static StructurePlaceSettings ajustes(Rotation rotacao) {
        return new StructurePlaceSettings().setRotation(rotacao).setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(JigsawReplacementProcessor.INSTANCE);
    }

    public static Espaco.Caixa caixa(StructureTemplate t, BlockPos ponto, Rotation rotacao) {
        BoundingBox b = t.getBoundingBox(ajustes(rotacao), ponto);
        return new Espaco.Caixa(b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ());
    }

    /** Confere se cabe no ponto (com chão firme embaixo). */
    public static Espaco.Resultado checar(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao) {
        return Espaco.checarComChao(caixa(t, ponto, rotacao), new MundoReal(nivel));
    }

    /**
     * Tenta no ponto; se não cabe e {@code procurar}, tenta pontos perto (apoiados no chão).
     * Só coloca se a checagem passar.
     */
    public static Tentativa colocar(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao,
                                    boolean procurar) {
        Vec3i tamanho = t.getSize(rotacao);
        if ((long) tamanho.getX() * tamanho.getY() * tamanho.getZ() > VOLUME_MAXIMO) {
            return new Tentativa(false, ponto, null, false, Optional.empty(), "grande");
        }
        MundoReal mundo = new MundoReal(nivel);
        Espaco.Caixa c = caixa(t, ponto, rotacao);
        Espaco.Resultado r = Espaco.checarComChao(c, mundo);
        BlockPos alvo = ponto;
        boolean mudou = false;
        if (!r.cabe() && procurar) {
            // A busca devolve o canto mínimo da caixa; o template é colocado pelo ponto de origem dele.
            int dx = ponto.getX() - c.x0();
            int dz = ponto.getZ() - c.z0();
            int dy = ponto.getY() - c.y0();
            Optional<int[]> achou = Espaco.procurar(c.x0(), c.z0(), tamanho.getX(), tamanho.getY(), tamanho.getZ(),
                    RAIO_BUSCA, PASSO_BUSCA, MAX_TENTATIVAS, mundo, mundo);
            if (achou.isPresent()) {
                int[] p = achou.get();
                alvo = new BlockPos(p[0] + dx, p[1] + dy, p[2] + dz);
                c = caixa(t, alvo, rotacao);
                r = Espaco.checarComChao(c, mundo);
                mudou = true;
            }
        }
        if (!r.cabe()) return new Tentativa(false, alvo, r, mudou, Optional.empty(), "nao_cabe");

        // Guarda o que estava lá (só ar e plantas, a checagem garantiu), coloca e anota o que mudou.
        BlockState[] antes = new BlockState[(int) c.volume()];
        int i = 0;
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int y = c.y0(); y <= c.y1(); y++)
            for (int x = c.x0(); x <= c.x1(); x++)
                for (int z = c.z0(); z <= c.z1(); z++) antes[i++] = nivel.getBlockState(m.set(x, y, z));
        t.placeInWorld(nivel, alvo, alvo, ajustes(rotacao), nivel.getRandom(), 2);
        Backup b = new Backup();
        i = 0;
        for (int y = c.y0(); y <= c.y1(); y++)
            for (int x = c.x0(); x <= c.x1(); x++)
                for (int z = c.z0(); z <= c.z1(); z++) {
                    BlockState depois = nivel.getBlockState(m.set(x, y, z));
                    if (depois != antes[i]) b.anotar(m.immutable(), antes[i], depois);
                    i++;
                }
        return new Tentativa(true, alvo, r, mudou, Optional.of(b), "");
    }
}
