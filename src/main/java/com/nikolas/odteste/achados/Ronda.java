package com.nikolas.odteste.achados;

import com.nikolas.odteste.criaturas.Criaturas;
import com.nikolas.odteste.criaturas.CriaturaDeSangue;
import com.nikolas.odteste.criaturas.RegrasCriaturas;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.vilacalada.DadosVilaCalada;
import com.nikolas.odteste.historia.vilacalada.IdsVilaCalada;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Ronda: de noite, em volta da âncora da Vila Calada, as criaturas de Sangue aparecem com mais chance que no resto
 * do mundo. Continua raro: olha só a cada 30 s, no máximo 4 jogadores por vez, 6% de chance cada, no máximo 2
 * criaturas vivas em volta da âncora e 1 por chunk. Sem âncora (vila ainda não colocada) ou com a Vila Calada
 * desligada pelo admin, não faz nada.
 */
public final class Ronda {
    private static final TagKey<Biome> SEM_MONSTROS =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "no_default_monsters"));

    private Ronda() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> aoTick(e.getServer()));
    }

    private static void aoTick(MinecraftServer servidor) {
        ServerLevel nivel = servidor.overworld();
        // Contas baratas primeiro: a maior parte dos ticks sai na primeira linha.
        if (nivel.getGameTime() % RegrasAchados.INTERVALO_TICKS != 0) return;
        if (!RegrasAchados.ehNoite(nivel.getDayTime()) || nivel.getDifficulty() == Difficulty.PEACEFUL) return;
        if (nivel.players().isEmpty()) return;
        // "/odteste vila_calada desligar" também para a Ronda.
        if (!DadosVilaCalada.de(servidor).ligado) return;
        GlobalPos ancora = DadosHistoria.de(servidor).ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        if (ancora == null || ancora.dimension() != Level.OVERWORLD) return;

        int olhados = 0;
        for (ServerPlayer p : nivel.players()) {
            if (olhados >= RegrasAchados.MAX_JOGADORES) break;
            if (p.isSpectator() || p.isCreative()) continue;
            if (!RegrasAchados.dentroDoRaio(p.getX() - ancora.pos().getX(), p.getZ() - ancora.pos().getZ())) continue;
            olhados++;
            if (nivel.getRandom().nextDouble() >= RegrasAchados.CHANCE_RONDA) continue;
            tentar(nivel, p, ancora.pos());
        }
    }

    private static void tentar(ServerLevel nivel, ServerPlayer jogador, BlockPos ancora) {
        double angulo = nivel.getRandom().nextDouble() * Math.PI * 2;
        double dist = RegrasAchados.DIST_MIN
                + nivel.getRandom().nextInt(RegrasAchados.DIST_MAX - RegrasAchados.DIST_MIN + 1);
        int x = (int) Math.floor(jogador.getX() + Math.cos(angulo) * dist);
        int z = (int) Math.floor(jogador.getZ() + Math.sin(angulo) * dist);
        if (!nivel.hasChunkAt(new BlockPos(x, 0, z))) return;
        BlockPos pos = new BlockPos(x, nivel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);

        var bioma = nivel.getBiome(pos);
        if (bioma.is(Biomes.MUSHROOM_FIELDS) || bioma.is(SEM_MONSTROS)) return;

        int perto = contar(nivel, new AABB(ancora).inflate(RegrasAchados.RAIO_RONDA, 64, RegrasAchados.RAIO_RONDA));
        ChunkPos cp = new ChunkPos(pos);
        int noChunk = contar(nivel, new AABB(cp.getMinBlockX(), nivel.getMinBuildHeight(), cp.getMinBlockZ(),
                cp.getMaxBlockX() + 1, nivel.getMaxBuildHeight(), cp.getMaxBlockZ() + 1));
        if (!RegrasAchados.podeRondar(nivel.getGameTime(), true, perto, noChunk, RegrasCriaturas.DIAS_MINIMOS)) return;

        if (nivel.getRandom().nextDouble() < RegrasAchados.CHANCE_SANGUESSUGA) {
            nascer(nivel, Criaturas.SANGUESSUGA.get(), pos);
        } else {
            nascer(nivel, Criaturas.RETALHADO.get(), pos);
        }
    }

    private static int contar(ServerLevel nivel, AABB area) {
        return nivel.getEntitiesOfClass(CriaturaDeSangue.class, area, e -> e.isAlive()).size();
    }

    private static <T extends Monster> void nascer(ServerLevel nivel, EntityType<T> tipo, BlockPos pos) {
        if (!SpawnPlacements.isSpawnPositionOk(tipo, nivel, pos)) return;
        if (!Monster.checkMonsterSpawnRules(tipo, nivel, MobSpawnType.EVENT, pos, nivel.getRandom())) return;
        if (!nivel.noCollision(tipo.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) return;
        tipo.spawn(nivel, pos, MobSpawnType.EVENT);
    }
}
