package com.nikolas.odteste.criaturas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.registro.Registro;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Criaturas do mod (IDs em docs/ids.md). Onde nascem: data/odteste/neoforge/biome_modifier/ (peso baixo, só
 * Overworld) e a regra paranormal de {@link RegrasCriaturas#podeNascer}.
 */
public final class Criaturas {
    public static final DeferredHolder<EntityType<?>, EntityType<Retalhado>> RETALHADO = Registro.CRIATURAS.register(
            IdsDados.CRIATURA_RETALHADO,
            () -> EntityType.Builder.<Retalhado>of(Retalhado::new, MobCategory.MONSTER)
                    .sized(1.9f, 3.1f)
                    .clientTrackingRange(10)
                    .build(OdMod.MODID + ":" + IdsDados.CRIATURA_RETALHADO));

    public static final DeferredHolder<EntityType<?>, EntityType<SanguessugaMae>> SANGUESSUGA = Registro.CRIATURAS.register(
            IdsDados.CRIATURA_SANGUESSUGA,
            () -> EntityType.Builder.<SanguessugaMae>of(SanguessugaMae::new, MobCategory.MONSTER)
                    .sized(2.8f, 2.1f)
                    .clientTrackingRange(10)
                    .build(OdMod.MODID + ":" + IdsDados.CRIATURA_SANGUESSUGA));

    /**
     * "O Que Segue" (nome provisório): não nasce sozinha pelo bioma; quem chama é o {@link Segue}. Nunca é salva no mundo.
     * Categoria MISC: não ocupa vaga de monstro.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<OQueSegue>> O_QUE_SEGUE = Registro.CRIATURAS.register(
            IdsDados.CRIATURA_O_QUE_SEGUE,
            () -> EntityType.Builder.<OQueSegue>of(OQueSegue::new, MobCategory.MISC)
                    .sized(0.6f, 2.8f)
                    .fireImmune()
                    .clientTrackingRange(6)
                    .build(OdMod.MODID + ":" + IdsDados.CRIATURA_O_QUE_SEGUE));

    private Criaturas() {}

    public static void iniciar(IEventBus barramento) {
        Segue.iniciar();
        barramento.addListener((EntityAttributeCreationEvent e) -> {
            e.put(RETALHADO.get(), Retalhado.atributos().build());
            e.put(SANGUESSUGA.get(), SanguessugaMae.atributos().build());
            e.put(O_QUE_SEGUE.get(), OQueSegue.atributos().build());
        });
        barramento.addListener((RegisterSpawnPlacementsEvent e) -> {
            e.register(RETALHADO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Criaturas::podeNascer, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            e.register(SANGUESSUGA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    Criaturas::podeNascer, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
    }

    /**
     * Nascimento natural: raro e só em condição paranormal (RegrasCriaturas). Spawner, ritual e comando
     * seguem só a regra de monstro comum. As contas baratas vêm primeiro; a busca de jogador olha só em volta.
     */
    static <T extends Monster> boolean podeNascer(EntityType<T> tipo, ServerLevelAccessor acesso, MobSpawnType motivo,
                                                  BlockPos pos, RandomSource sorte) {
        if (motivo == MobSpawnType.NATURAL || motivo == MobSpawnType.CHUNK_GENERATION) {
            ServerLevel nivel = acesso.getLevel();
            if (nivel.dimension() != Level.OVERWORLD) return false;
            BlockPos spawn = nivel.getSharedSpawnPos();
            Player perto = nivel.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    RegrasCriaturas.RAIO_JOGADOR, o -> !o.isSpectator());
            int exposicao = perto == null ? 0 : DadosJogador.exposicao(perto);
            if (!RegrasCriaturas.podeNascer(nivel.getGameTime(), pos.getX() - spawn.getX(), pos.getZ() - spawn.getZ(),
                    exposicao)) {
                return false;
            }
            // 1 por jogador Tocado por noite.
            if (!RegrasCriaturas.livreNestaNoite(DadosJogador.criaturaNoite(perto),
                    RegrasCriaturas.noite(nivel.getDayTime()))) {
                return false;
            }
            // Já tem uma de Sangue por perto (busca só nesta área, só quando o sorteio de spawn chega aqui).
            if (temCriaturaPerto(nivel, pos)) return false;
        }
        return Monster.checkMonsterSpawnRules(tipo, acesso, motivo, pos, sorte);
    }

    private static boolean temCriaturaPerto(ServerLevel nivel, BlockPos pos) {
        AABB area = new AABB(pos).inflate(RegrasCriaturas.RAIO_AREA, RegrasCriaturas.ALTURA_AREA, RegrasCriaturas.RAIO_AREA);
        return !nivel.getEntitiesOfClass(CriaturaDeSangue.class, area, e -> e.isAlive()).isEmpty();
    }

    /** Chamado quando uma criatura de Sangue nasce sozinha: gasta a noite do jogador Tocado mais perto. */
    static void gastarNoite(ServerLevel nivel, double x, double y, double z) {
        Player perto = nivel.getNearestPlayer(x, y, z, RegrasCriaturas.RAIO_JOGADOR,
                o -> o instanceof Player j && !j.isSpectator()
                        && DadosJogador.exposicao(j) >= RegrasCriaturas.EXPOSICAO_MINIMA);
        if (perto != null) DadosJogador.definirCriaturaNoite(perto, RegrasCriaturas.noite(nivel.getDayTime()));
    }
}
