package com.nikolas.odteste.cliente;

import com.nikolas.odteste.sistemas.RegrasHorror;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * Alucinações que se veem: vulto no canto da visão, criatura parada e porta que abre sozinha.
 * Tudo só neste cliente:
 * - o vulto e a criatura são entidades criadas aqui, com id negativo (o servidor nunca usa), nunca registradas,
 *   nunca enviadas e nunca salvas; não dá para mirar nem bater nelas (o cliente nem manda pacote);
 * - a porta muda só a cópia do bloco neste cliente; o mundo de verdade (servidor) não muda, e a cópia volta
 *   sozinha em 1,5–3 s (ou na hora, se o servidor mandar o bloco de novo).
 * As regras de quando sumir estão em RegrasHorror (testadas).
 */
final class Alucinacoes {
    /** Ids negativos: o servidor só usa positivos, então nunca colide com uma entidade de verdade. */
    private static int proximoId = -0x0D7E0000;

    private static Entity vulto;
    private static Entity mob;
    private static Porta porta;

    private record Porta(ClientLevel nivel, BlockPos pos, BlockState original, BlockState falsa,
                         BlockPos outra, BlockState outraOriginal, BlockState outraFalsa,
                         boolean alcapao, long fecharEm) {}

    private Alucinacoes() {}

    /** Tem alguma visual em andamento? (uma por vez) */
    static boolean ocupado() {
        return vulto != null || mob != null || porta != null;
    }

    /** Uma vez por tick do cliente. Barato: só olha as (no máximo três) alucinações vivas. */
    static void aoTick(LocalPlayer p, long agora) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (vulto != null) {
            if (vulto.level() != nivel || vulto.isRemoved()) {
                vulto = null;
            } else {
                Vec3 olhar = p.getViewVector(1f);
                Vec3 ate = vulto.position().add(0, 1.5, 0).subtract(p.getEyePosition());
                double cos = ate.lengthSqr() < 1e-6 ? 1 : olhar.dot(ate.normalize());
                if (RegrasHorror.vultoSome(cos, p.distanceToSqr(vulto), vulto.tickCount)) {
                    sumir(vulto, nivel);
                    vulto = null;
                }
            }
        }
        if (mob != null) {
            if (mob.level() != nivel || mob.isRemoved()) {
                mob = null;
            } else if (RegrasHorror.mobSome(p.distanceToSqr(mob), mob.tickCount)) {
                sumir(mob, nivel);
                mob = null;
            }
        }
        if (porta != null && (agora >= porta.fecharEm() || nivel != porta.nivel())) {
            fecharPorta(nivel);
        }
    }

    static void limpar() {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (vulto != null && nivel != null && vulto.level() == nivel) nivel.removeEntity(vulto.getId(), Entity.RemovalReason.DISCARDED);
        if (mob != null && nivel != null && mob.level() == nivel) nivel.removeEntity(mob.getId(), Entity.RemovalReason.DISCARDED);
        vulto = mob = null;
        if (porta != null) fecharPorta(nivel);
    }

    // ---------- vulto ----------

    /** Vulto parado no canto da visão (35–50° do centro, 7–11 blocos). Devolve false se não achou chão. */
    static boolean vulto(LocalPlayer p) {
        if (vulto != null) return false;
        RandomSource r = p.getRandom();
        float yaw = p.getYRot() + RegrasHorror.vultoAngulo(r::nextInt);
        Vec3 pos = noChao(p, yaw, RegrasHorror.vultoDistancia(r::nextInt));
        if (pos == null) return false;
        vulto = criar(new VultoFalso(p.level()), pos, p);
        return vulto != null;
    }

    // ---------- criatura falsa ----------

    /** Criatura parada, longe (14–20 blocos), num lado qualquer. Some quando você chega a 6 blocos. */
    static boolean mobFalso(LocalPlayer p) {
        if (mob != null) return false;
        RandomSource r = p.getRandom();
        float yaw = p.getYRot() + 60 + r.nextInt(241); // qualquer lado, menos bem na frente
        Vec3 pos = noChao(p, yaw, RegrasHorror.mobDistancia(r::nextInt));
        if (pos == null) return false;
        mob = criar(new MobFalso(p.level()), pos, p);
        return mob != null;
    }

    private static Entity criar(LivingEntity e, Vec3 pos, LocalPlayer p) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel == null) return null;
        e.setId(proximoId--);
        if (proximoId > -0x0D000000) proximoId = -0x0D7E0000; // nunca chega perto de zero
        float yaw = olharPara(pos, p);
        e.moveTo(pos.x, pos.y, pos.z, yaw, 0f);
        e.setYHeadRot(yaw);
        e.setYBodyRot(yaw);
        e.yHeadRotO = yaw;
        e.yBodyRotO = yaw;
        e.setSilent(true);
        e.setNoGravity(true);
        nivel.addEntity(e);
        return e;
    }

    /** Some sem barulho, deixando um fiapo de fumaça. */
    private static void sumir(Entity e, ClientLevel nivel) {
        if (nivel != null) {
            for (int i = 0; i < 6; i++) {
                nivel.addParticle(ParticleTypes.SMOKE, e.getX() + (e.getRandom().nextDouble() - 0.5) * 0.5,
                        e.getY() + e.getRandom().nextDouble() * e.getBbHeight(),
                        e.getZ() + (e.getRandom().nextDouble() - 0.5) * 0.5, 0, 0.01, 0);
            }
            nivel.removeEntity(e.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    private static float olharPara(Vec3 de, LocalPlayer p) {
        double dx = p.getX() - de.x;
        double dz = p.getZ() - de.z;
        return (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
    }

    /**
     * Um lugar de pé a {@code dist} blocos na direção {@code yaw}: chão firme embaixo e dois blocos livres.
     * Olha no máximo 8 alturas, uma vez só (não é por tick).
     */
    private static Vec3 noChao(LocalPlayer p, float yaw, double dist) {
        Level nivel = p.level();
        double rad = Math.toRadians(yaw);
        double x = p.getX() - Math.sin(rad) * dist;
        double z = p.getZ() + Math.cos(rad) * dist;
        int y0 = Mth.floor(p.getY());
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = 3; dy >= -4; dy--) {
            m.set(Mth.floor(x), y0 + dy, Mth.floor(z));
            if (!nivel.isLoaded(m)) return null;
            if (livre(nivel, m) && livre(nivel, m.above()) && !livre(nivel, m.below())) {
                return new Vec3(m.getX() + 0.5, m.getY(), m.getZ() + 0.5);
            }
        }
        return null;
    }

    private static boolean livre(Level nivel, BlockPos pos) {
        return nivel.getBlockState(pos).getCollisionShape(nivel, pos).isEmpty();
    }

    // ---------- porta ----------

    /** Uma porta ou alçapão fechado perto (3 a 6 blocos) parece abrir, com o som. Devolve false se não tem. */
    static boolean porta(LocalPlayer p, long agora) {
        if (porta != null) return false;
        ClientLevel nivel = Minecraft.getInstance().level;
        if (nivel == null) return false;
        BlockPos centro = p.blockPosition();
        int r = RegrasHorror.PORTA_RAIO;
        int ry = RegrasHorror.PORTA_RAIO_Y;
        List<BlockPos> achadas = new ArrayList<>();
        for (BlockPos b : BlockPos.betweenClosed(centro.offset(-r, -ry, -r), centro.offset(r, ry, r))) {
            if (b.distToCenterSqr(p.getX(), p.getY(), p.getZ()) < RegrasHorror.PORTA_MIN_SQ) continue;
            BlockState s = nivel.getBlockState(b);
            if (!s.hasProperty(BlockStateProperties.OPEN) || s.getValue(BlockStateProperties.OPEN)) continue;
            boolean ehPorta = s.getBlock() instanceof DoorBlock
                    && s.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
            if (ehPorta || s.getBlock() instanceof TrapDoorBlock) {
                achadas.add(b.immutable());
                if (achadas.size() >= 16) break;
            }
        }
        if (achadas.isEmpty()) return false;

        BlockPos pos = achadas.get(p.getRandom().nextInt(achadas.size()));
        BlockState original = nivel.getBlockState(pos);
        BlockState falsa = original.setValue(BlockStateProperties.OPEN, true);
        boolean alcapao = original.getBlock() instanceof TrapDoorBlock;
        BlockPos outra = null;
        BlockState outraOriginal = null;
        BlockState outraFalsa = null;
        if (!alcapao) {
            outra = pos.above();
            outraOriginal = nivel.getBlockState(outra);
            if (!(outraOriginal.getBlock() instanceof DoorBlock) || !outraOriginal.hasProperty(BlockStateProperties.OPEN)) {
                return false;
            }
            outraFalsa = outraOriginal.setValue(BlockStateProperties.OPEN, true);
        }
        porta = new Porta(nivel, pos, original, falsa, outra, outraOriginal, outraFalsa, alcapao,
                agora + RegrasHorror.portaDuracao(p.getRandom()::nextInt));
        trocar(nivel, pos, falsa);
        if (outra != null) trocar(nivel, outra, outraFalsa);
        Sons.em(Vec3.atCenterOf(pos), alcapao ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN,
                0.6f, 0.6f);
        return true;
    }

    /** Fecha a porta falsa: só se a cópia do cliente ainda for a nossa (se o servidor mandou outra, fica a dele). */
    private static void fecharPorta(ClientLevel nivel) {
        Porta f = porta;
        porta = null;
        // Mundo trocado (dimensão, saiu): a cópia velha foi jogada fora junto, não há o que desfazer.
        if (f == null || nivel == null || nivel != f.nivel()) return;
        boolean voltou = false;
        if (nivel.getBlockState(f.pos()) == f.falsa()) {
            trocar(nivel, f.pos(), f.original());
            voltou = true;
        }
        if (f.outra() != null && nivel.getBlockState(f.outra()) == f.outraFalsa()) {
            trocar(nivel, f.outra(), f.outraOriginal());
        }
        if (voltou) {
            Sons.em(Vec3.atCenterOf(f.pos()), f.alcapao() ? SoundEvents.WOODEN_TRAPDOOR_CLOSE
                    : SoundEvents.WOODEN_DOOR_CLOSE, 0.5f, 0.6f);
        }
    }

    /** Troca só a cópia do cliente, sem avisar vizinhos (2 = redesenhar, 16 = não recalcular formas). */
    private static void trocar(ClientLevel nivel, BlockPos pos, BlockState s) {
        nivel.setBlock(pos, s, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    // ---------- as figuras ----------

    /** Parada, olhando para você, sem cérebro, sem som, sem colisão, sem mira. Modelo vanilla provisório. */
    private static void parada(LivingEntity e) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;
        float yaw = olharPara(e.position(), p);
        e.yHeadRotO = e.getYHeadRot();
        e.yBodyRotO = e.yBodyRot;
        e.setYHeadRot(yaw);
        e.setYBodyRot(yaw);
        e.setYRot(yaw);
    }

    /** O vulto: silhueta alta e escura (modelo do Enderman até o Modelador fazer o nosso). */
    private static final class VultoFalso extends EnderMan {
        VultoFalso(Level nivel) {
            super(EntityType.ENDERMAN, nivel);
        }

        @Override
        public void tick() {
            parada(this);
        }

        @Override
        public boolean isPickable() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }

        @Override
        public boolean isAttackable() {
            return false;
        }

        @Override
        public boolean shouldBeSaved() {
            return false;
        }
    }

    /** A criatura falsa (modelo do zumbi até termos as nossas criaturas). */
    private static final class MobFalso extends Zombie {
        MobFalso(Level nivel) {
            super(EntityType.ZOMBIE, nivel);
        }

        @Override
        public void tick() {
            parada(this);
        }

        @Override
        public boolean isPickable() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }

        @Override
        public boolean isAttackable() {
            return false;
        }

        @Override
        public boolean shouldBeSaved() {
            return false;
        }
    }
}
