package com.nikolas.odteste.estruturas;

import com.nikolas.odteste.OdMod;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    /** Maior estrutura colocada de uma vez (blocos da caixa). Acima disso vai em partes, uma fatia por tick ({@link EmPartes}). */
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

    /** {@link Tentativa#erro} quando deu erro no meio: colocou parte, e o que entrou está no backup. */
    public static final String ERRO_INTERNO = "interno";

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

    /**
     * Estrutura grande (acima de {@link #VOLUME_MAXIMO}) até esse volume vai em partes ({@link EmPartes}).
     * 524.288 = 128 x 32 x 128; a Vila Calada final (~100 x 30 x 100) cabe com folga.
     * O {@code prever} só lê o chão (até 128 x 128); o resto é conferido em partes antes de pôr qualquer bloco.
     */
    public static final long VOLUME_MAXIMO_PARTES = 524_288;

    /** Grande demais até para colocar em partes. */
    public static boolean grandeDemaisAtePartes(StructureTemplate t, Rotation rotacao) {
        var tamanho = t.getSize(rotacao);
        return (long) tamanho.getX() * tamanho.getY() * tamanho.getZ() > VOLUME_MAXIMO_PARTES;
    }

    /** Só o chão embaixo (estrutura grande: o resto é conferido em partes por {@link EmPartes}). */
    public static Espaco.Resultado checarChao(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao) {
        return Espaco.checarChao(caixa(t, ponto, rotacao), new MundoReal(nivel));
    }

    /**
     * Separa os blocos do template por camada das fatias ({@link Fatias#camadaDoY}), uma vez só por colocação:
     * cada tick carrega só a camada da vez, em vez de o jogo passar o template inteiro por fatia.
     * Cada pedaço é um template com o mesmo tamanho e a mesma paleta, então cai exatamente no mesmo lugar.
     * O custo (salvar o template uma vez) é de uma vez só, no começo (ou ao retomar depois de reiniciar).
     */
    static CompoundTag[] porCamada(StructureTemplate t, BlockPos ponto, Rotation rotacao, Espaco.Caixa c, int limite) {
        CompoundTag raiz = t.save(new CompoundTag());
        int n = Fatias.camadas(c, limite);
        ListTag[] blocos = new ListTag[n];
        for (int k = 0; k < n; k++) blocos[k] = new ListTag();
        StructurePlaceSettings ajustes = ajustes(rotacao);
        ListTag todos = raiz.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < todos.size(); i++) {
            CompoundTag b = todos.getCompound(i);
            ListTag pos = b.getList("pos", Tag.TAG_INT);
            BlockPos local = new BlockPos(pos.getInt(0), pos.getInt(1), pos.getInt(2));
            int y = StructureTemplate.calculateRelativePosition(ajustes, local).offset(ponto).getY();
            blocos[Fatias.camadaDoY(c, limite, y)].add(b);
        }
        CompoundTag[] saida = new CompoundTag[n];
        for (int k = 0; k < n; k++) {
            CompoundTag s = new CompoundTag();
            for (String chave : new String[] {"size", "palette", "palettes", "DataVersion"}) {
                if (raiz.contains(chave)) s.put(chave, raiz.get(chave));
            }
            s.put("blocks", blocos[k]);
            s.put("entities", new ListTag());
            saida[k] = s;
        }
        return saida;
    }

    /** Monta o template de uma camada (de {@link #porCamada}). */
    static StructureTemplate carregarCamada(CompoundTag camada) {
        StructureTemplate t = new StructureTemplate();
        t.load(BuiltInRegistries.BLOCK.asLookup(), camada);
        return t;
    }

    /** Coloca no ponto, só se a checagem passar. Nunca procura outro lugar. */
    public static Tentativa colocar(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao) {
        if (grandeDemais(t, rotacao)) return new Tentativa(false, ponto, null, Optional.empty(), "grande");
        MundoReal mundo = new MundoReal(nivel);
        Espaco.Caixa c = caixa(t, ponto, rotacao);
        Espaco.Resultado r = Espaco.checarComChao(c, mundo);
        if (!r.cabe()) return new Tentativa(false, ponto, r, Optional.empty(), "nao_cabe");
        Backup b = new Backup();
        try {
            colocarParte(nivel, t, ponto, rotacao, c, b, false);
        } catch (RuntimeException ex) {
            // O que entrou antes do erro já está no backup: devolve mesmo assim, para a colocação ficar salva
            // e o admin poder desfazer. erro = "interno" avisa quem chamou.
            OdMod.LOGGER.error("Estrutura em {} parou com erro no meio", ponto, ex);
            return new Tentativa(true, ponto, r, Optional.of(b), ERRO_INTERNO);
        }
        return new Tentativa(true, ponto, r, Optional.of(b), "");
    }

    /**
     * Põe só o pedaço do template que cai dentro de {@code parte} e anota no backup. Não confere espaço: quem
     * chama confere antes. Com {@code emPartes}, as bordas entre fatias não se ajustam (o template já guarda o
     * formato certo de cada bloco; ajustar contra a fatia ainda vazia derrubaria tocha e placa).
     */
    static void colocarParte(ServerLevel nivel, StructureTemplate t, BlockPos ponto, Rotation rotacao,
                             Espaco.Caixa parte, Backup b, boolean emPartes) {
        Espaco.Caixa c = parte;
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
        StructurePlaceSettings ajustes = ajustes(rotacao);
        if (emPartes) {
            // Mesmo recurso da geração do jogo: só sai bloco dentro da caixa (o template já vem só com a camada).
            ajustes.setBoundingBox(new BoundingBox(c.x0(), c.y0(), c.z0(), c.x1(), c.y1(), c.z1()));
            ajustes.setKnownShape(true);
        }
        try {
            t.placeInWorld(nivel, ponto, ponto, ajustes, nivel.getRandom(), 2);
            // Baús marcados no template ganham a recompensa de enigma (tabela de loot nossa).
            com.nikolas.odteste.achados.Achados.aplicarRecompensas(nivel, t, ponto, ajustes);
        } finally {
            // Mesmo se der erro no meio (bloco de outro mod quebrado, por exemplo): tudo que já mudou entra no
            // backup, senão ficaria no mundo sem como desfazer. O erro segue para quem chamou (pausa e avisa).
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
        }
    }
}
