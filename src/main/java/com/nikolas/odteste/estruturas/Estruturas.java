package com.nikolas.odteste.estruturas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
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
 * sem geração de mundo. Só ocupa ar e blocos da tag {@code odteste:substituivel}; chunk com dono ou não carregado
 * barra. <b>Nunca muda de lugar sozinha:</b> se não cabe, {@link #sugerir} acha um ponto perto e o admin confere
 * com um {@code prever} lá antes de colocar. Tudo fica num {@link Backup} para desfazer.
 */
public final class Estruturas {
    /** Maior estrutura aceita (blocos da caixa). Acima disso trava o tick: dividir o template. */
    public static final long VOLUME_MAXIMO = 16384;
    /** Busca de ponto sugerido: raio, passo e tentativas. */
    public static final int RAIO_BUSCA = 32;
    public static final int PASSO_BUSCA = 3;
    public static final int MAX_TENTATIVAS = 150;
    /**
     * Máximo de blocos lidos numa busca de sugestão (num tick só). Com o chão conferido primeiro, cada ponto ruim
     * custa pouco; mesmo no pior caso a busca para aqui (algumas dezenas de milissegundos).
     */
    public static final long ORCAMENTO_LEITURAS = 200_000;

    /** Resultado de uma tentativa: onde (ou onde está o bloqueio) e a colocação, se deu certo. */
    public record Tentativa(boolean ok, BlockPos ponto, Espaco.Resultado checagem, Optional<Backup> backup, String erro) {}

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

    public static boolean grandeDemais(StructureTemplate t, Rotation rotacao) {
        var tamanho = t.getSize(rotacao);
        return (long) tamanho.getX() * tamanho.getY() * tamanho.getZ() > VOLUME_MAXIMO;
    }

    /** Confere se cabe no ponto (com chão firme embaixo). */
    public static Espaco.Resultado checar(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao) {
        return Espaco.checarComChao(caixa(t, ponto, rotacao), new MundoReal(nivel));
    }

    /**
     * Um ponto perto onde a estrutura cabe (apoiada no chão), para o admin conferir. Não coloca nada.
     * A busca tem limite de leituras ({@link #ORCAMENTO_LEITURAS}).
     * @return o ponto de origem para usar no {@code prever}/{@code colocar}, ou vazio
     */
    public static Optional<BlockPos> sugerir(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao) {
        if (grandeDemais(t, rotacao)) return Optional.empty();
        MundoReal mundo = new MundoReal(nivel);
        Espaco.Caixa c = caixa(t, ponto, rotacao);
        // A busca devolve o canto mínimo da caixa; o template é colocado pelo ponto de origem dele.
        int dx = ponto.getX() - c.x0();
        int dy = ponto.getY() - c.y0();
        int dz = ponto.getZ() - c.z0();
        return Espaco.procurar(c.x0(), c.z0(), c.largura(), c.altura(), c.fundo(),
                        RAIO_BUSCA, PASSO_BUSCA, MAX_TENTATIVAS, ORCAMENTO_LEITURAS, mundo, mundo)
                .map(p -> new BlockPos(p[0] + dx, p[1] + dy, p[2] + dz));
    }

    /** Coloca no ponto, só se a checagem passar. Nunca procura outro lugar. */
    public static Tentativa colocar(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao) {
        if (grandeDemais(t, rotacao)) return new Tentativa(false, ponto, null, Optional.empty(), "grande");
        MundoReal mundo = new MundoReal(nivel);
        Espaco.Caixa c = caixa(t, ponto, rotacao);
        Espaco.Resultado r = Espaco.checarComChao(c, mundo);
        if (!r.cabe()) return new Tentativa(false, ponto, r, Optional.empty(), "nao_cabe");

        Backup b = new Backup();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

        // Planta de duas metades com só uma metade na caixa: a outra metade sai junto e entra no backup,
        // para não quebrar nem ficar solta (no desfazer as duas voltam).
        List<BlockPos> foraTirada = new ArrayList<>();
        List<BlockPos> dentroDoPar = new ArrayList<>();
        List<BlockState> estadoFora = new ArrayList<>();
        for (int y = c.y0(); y <= c.y1(); y++)
            for (int x = c.x0(); x <= c.x1(); x++)
                for (int z = c.z0(); z <= c.z1(); z++) {
                    BlockState st = nivel.getBlockState(m.set(x, y, z));
                    BlockPos outra = Backup.outraMetade(m.immutable(), st);
                    if (outra == null || c.contem(outra.getX(), outra.getY(), outra.getZ())) continue;
                    BlockState so = nivel.getBlockState(outra);
                    BlockPos tirada = b.tirarOutraMetade(nivel, m.immutable(), st);
                    if (tirada != null) {
                        foraTirada.add(tirada);
                        dentroDoPar.add(m.immutable());
                        estadoFora.add(so);
                    }
                }

        // Guarda o que estava lá (só ar e plantas, a checagem garantiu), coloca e anota o que mudou.
        BlockState[] antes = new BlockState[(int) c.volume()];
        int i = 0;
        for (int y = c.y0(); y <= c.y1(); y++)
            for (int x = c.x0(); x <= c.x1(); x++)
                for (int z = c.z0(); z <= c.z1(); z++) antes[i++] = nivel.getBlockState(m.set(x, y, z));
        t.placeInWorld(nivel, ponto, ponto, ajustes(rotacao), nivel.getRandom(), 2);
        i = 0;
        for (int y = c.y0(); y <= c.y1(); y++)
            for (int x = c.x0(); x <= c.x1(); x++)
                for (int z = c.z0(); z <= c.z1(); z++) {
                    BlockState depois = nivel.getBlockState(m.set(x, y, z));
                    if (depois != antes[i]) b.anotar(m.immutable(), antes[i], depois);
                    i++;
                }

        // Se o template não mexeu na metade de dentro (ex.: structure void ali), a de fora volta e sai do backup.
        for (int k = 0; k < foraTirada.size(); k++) {
            BlockPos dentro = dentroDoPar.get(k);
            BlockState agora = nivel.getBlockState(dentro);
            if (agora.is(estadoFora.get(k).getBlock())) {
                nivel.setBlock(foraTirada.get(k), estadoFora.get(k), Backup.SEM_VIZINHOS);
                b.esquecer(foraTirada.get(k));
            }
        }
        return new Tentativa(true, ponto, r, Optional.of(b), "");
    }
}
