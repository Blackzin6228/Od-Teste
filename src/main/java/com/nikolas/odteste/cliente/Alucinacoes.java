package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nikolas.odteste.sistemas.RegrasHorror;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Alucinações que se veem: vulto no canto da visão, criatura falsa e porta que abre sozinha. Tudo só neste cliente.
 *
 * Vulto e criatura: são desenhados por evento de render (RenderLevelStageEvent), usando um modelo vanilla como
 * molde. A entidade-molde **nunca entra no mundo do cliente** (nada de addEntity): minimapas (JourneyMap, Xaero,
 * FTB Chunks), radares e "o que é isso" (Jade) só enxergam o que está no mundo, então não veem nada. Também não dá
 * para mirar nem bater: não existe para a física.
 *
 * Porta: a troca do bloco passa pelo mesmo mecanismo que o jogo usa quando você coloca um bloco antes do servidor
 * confirmar ("previsão de bloco"). Enquanto a porta falsa está na tela, o cliente guarda o estado real que o servidor
 * mandar para aquele bloco (outro jogador abriu, aldeão, redstone...). No fim, ou se você mexer em algum bloco antes,
 * o jogo devolve exatamente o estado real. A porta real nunca fica errada.
 */
final class Alucinacoes {
    /** Ids negativos: o servidor só usa positivos (o molde nem entra no mundo, mas alguns mods guardam coisas por id). */
    private static int proximoId = -0x0D7E0000;

    /** Uma figura falsa: posição, idade e comportamento. O corpo é só o molde do desenho. */
    private static final class Figura {
        final LivingEntity corpo;
        final ClientLevel nivel;
        final int vida;
        /** Vem até você (Lucidez Baixa). */
        final boolean anda;
        /** Fica de costas e, ao ser olhada, some e reaparece uma vez mais perto (Exposição Crítica). */
        final boolean costas;
        boolean reapareceu;
        int idade;

        Figura(LivingEntity corpo, ClientLevel nivel, int vida, boolean anda, boolean costas) {
            this.corpo = corpo;
            this.nivel = nivel;
            this.vida = vida;
            this.anda = anda;
            this.costas = costas;
        }
    }

    private static Figura vulto;
    private static Figura mob;
    private static Porta porta;

    /**
     * @param sequencia número da previsão de bloco que guarda o estado real (-1 = só som, nada mudou na tela)
     */
    private record Porta(ClientLevel nivel, BlockPos pos, BlockState falsa, int sequencia, boolean alcapao,
                         long fecharEm) {}

    private Alucinacoes() {}

    /** Tem alguma visual em andamento? (uma por vez) */
    static boolean ocupado() {
        return vulto != null || mob != null || porta != null;
    }

    /** Tem um vulto na tela agora (para a lentidão sutil da Exposição Crítica)? */
    static boolean vultoPresente() {
        return vulto != null;
    }

    /** Uma vez por tick do cliente. Barato: só olha as (no máximo três) alucinações vivas. */
    static void aoTick(LocalPlayer p, long agora) {
        ClientLevel nivel = Minecraft.getInstance().level;
        if (vulto != null) {
            Figura f = vulto;
            f.idade++;
            if (f.nivel != nivel || RegrasHorror.vultoSome(cosOlhar(p, f.corpo), p.distanceToSqr(f.corpo), f.idade, f.vida)) {
                sumir(f, nivel);
                vulto = null;
            } else {
                virarPara(f, p);
            }
        }
        if (mob != null) {
            Figura f = mob;
            f.idade++;
            if (f.nivel != nivel || RegrasHorror.mobSome(p.distanceToSqr(f.corpo), f.idade, f.vida)) {
                sumir(f, nivel);
                mob = null;
            } else if (f.costas && cosOlhar(p, f.corpo) >= RegrasHorror.COS_OLHOU) {
                // Olhou: some. Na primeira vez, reaparece mais perto, em outro lugar.
                sumir(f, nivel);
                mob = null;
                if (!f.reapareceu) reaparecer(p, f);
            } else {
                if (f.anda) andar(f, p);
                virarPara(f, p);
            }
        }
        if (porta != null && (agora >= porta.fecharEm() || nivel != porta.nivel())) {
            fecharPorta(nivel);
        }
    }

    static void limpar() {
        vulto = mob = null;
        if (porta != null) fecharPorta(Minecraft.getInstance().level);
    }

    private static double cosOlhar(LocalPlayer p, LivingEntity e) {
        Vec3 olhar = p.getViewVector(1f);
        Vec3 ate = e.position().add(0, e.getBbHeight() * 0.8, 0).subtract(p.getEyePosition());
        return ate.lengthSqr() < 1e-6 ? 1 : olhar.dot(ate.normalize());
    }

    // ---------- vulto ----------

    /** Vulto parado no canto da visão (35–50° do centro, 12–16 blocos). Devolve false se não achou chão. */
    static boolean vulto(LocalPlayer p, boolean suave) {
        if (vulto != null) return false;
        RandomSource r = p.getRandom();
        float yaw = p.getYRot() + RegrasHorror.vultoAngulo(r::nextInt);
        Vec3 pos = noChao(p, yaw, RegrasHorror.vultoDistancia(r::nextInt));
        if (pos == null) return false;
        LivingEntity corpo = molde(new EnderMan(EntityType.ENDERMAN, p.level()), pos, p, false);
        if (corpo == null) return false;
        vulto = new Figura(corpo, (ClientLevel) p.level(), suave ? RegrasHorror.VIDA_SUAVE : RegrasHorror.VULTO_VIDA,
                false, false);
        return true;
    }

    // ---------- criatura falsa ----------

    /**
     * Criatura parada, longe (14–20 blocos), fora do centro da visão.
     * @param anda   Lucidez Baixa: vem até você e some a 3 blocos
     * @param suave  SUAVE: silhueta parada que some em 1 s
     */
    static boolean mobFalso(LocalPlayer p, boolean anda, boolean suave) {
        if (mob != null) return false;
        RandomSource r = p.getRandom();
        float yaw = p.getYRot() + 60 + r.nextInt(241); // qualquer lado, menos bem na frente
        Vec3 pos = noChao(p, yaw, RegrasHorror.mobDistancia(r::nextInt));
        if (pos == null) return false;
        boolean costas = !anda && !suave;
        LivingEntity corpo = molde(new Zombie(EntityType.ZOMBIE, p.level()), pos, p, costas);
        if (corpo == null) return false;
        mob = new Figura(corpo, (ClientLevel) p.level(), suave ? RegrasHorror.VIDA_SUAVE : RegrasHorror.MOB_VIDA,
                anda && !suave, costas);
        return true;
    }

    /** A criatura de costas reaparece uma vez, mais perto, num outro canto. */
    private static void reaparecer(LocalPlayer p, Figura antes) {
        RandomSource r = p.getRandom();
        double dist = RegrasHorror.mobReaparece(Math.sqrt(p.distanceToSqr(antes.corpo)));
        float yaw = p.getYRot() + 60 + r.nextInt(241);
        Vec3 pos = noChao(p, yaw, dist);
        if (pos == null) return;
        LivingEntity corpo = molde(new Zombie(EntityType.ZOMBIE, p.level()), pos, p, true);
        if (corpo == null) return;
        Figura f = new Figura(corpo, antes.nivel, RegrasHorror.MOB_VIDA, false, true);
        f.reapareceu = true;
        mob = f;
    }

    /** Prepara o molde: posição e rotação. Nunca é adicionado ao mundo. */
    private static LivingEntity molde(LivingEntity e, Vec3 pos, LocalPlayer p, boolean costas) {
        if (Minecraft.getInstance().level == null) return null;
        e.setId(proximoId--);
        if (proximoId > -0x0D000000) proximoId = -0x0D7E0000; // nunca chega perto de zero
        float yaw = olharPara(pos, p) + (costas ? 180f : 0f);
        e.moveTo(pos.x, pos.y, pos.z, yaw, 0f);
        e.setYHeadRot(yaw);
        e.setYBodyRot(yaw);
        e.yHeadRotO = yaw;
        e.yBodyRotO = yaw;
        e.setSilent(true);
        e.setNoGravity(true);
        return e;
    }

    private static void virarPara(Figura f, LocalPlayer p) {
        LivingEntity e = f.corpo;
        float yaw = olharPara(e.position(), p) + (f.costas ? 180f : 0f);
        e.yRotO = e.getYRot();
        e.yHeadRotO = e.getYHeadRot();
        e.yBodyRotO = e.yBodyRot;
        e.setYRot(yaw);
        e.setYHeadRot(yaw);
        e.setYBodyRot(yaw);
    }

    /** Um passinho na sua direção (sem física: o molde não está no mundo). */
    private static void andar(Figura f, LocalPlayer p) {
        LivingEntity e = f.corpo;
        Vec3 ate = new Vec3(p.getX() - e.getX(), 0, p.getZ() - e.getZ());
        if (ate.lengthSqr() < 1e-6) return;
        Vec3 passo = ate.normalize().scale(RegrasHorror.MOB_PASSO);
        e.xo = e.getX();
        e.yo = e.getY();
        e.zo = e.getZ();
        e.setPos(e.getX() + passo.x, e.getY(), e.getZ() + passo.z);
        e.walkAnimation.update((float) RegrasHorror.MOB_PASSO * 4f, 0.4f);
    }

    /** Some sem barulho, deixando um fiapo de fumaça. */
    private static void sumir(Figura f, ClientLevel nivel) {
        if (nivel == null || f.nivel != nivel) return;
        LivingEntity e = f.corpo;
        RandomSource r = e.getRandom();
        for (int i = 0; i < 6; i++) {
            nivel.addParticle(ParticleTypes.SMOKE, e.getX() + (r.nextDouble() - 0.5) * 0.5,
                    e.getY() + r.nextDouble() * e.getBbHeight(), e.getZ() + (r.nextDouble() - 0.5) * 0.5, 0, 0.01, 0);
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

    // ---------- desenho ----------

    /** Desenha as figuras, depois das entidades de verdade. Sem figura, sai na primeira linha. */
    static void desenhar(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (vulto == null && mob == null) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel nivel = mc.level;
        if (nivel == null) return;
        Vec3 cam = e.getCamera().getPosition();
        float pt = e.getPartialTick().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        EntityRenderDispatcher d = mc.getEntityRenderDispatcher();
        PoseStack pose = e.getPoseStack();
        desenharFigura(vulto, nivel, d, cam, pt, pose, buf);
        desenharFigura(mob, nivel, d, cam, pt, pose, buf);
        buf.endBatch();
    }

    private static void desenharFigura(Figura f, ClientLevel nivel, EntityRenderDispatcher d, Vec3 cam, float pt,
                                 PoseStack pose, MultiBufferSource buf) {
        if (f == null || f.nivel != nivel) return;
        LivingEntity c = f.corpo;
        double x = Mth.lerp(pt, c.xo, c.getX()) - cam.x;
        double y = Mth.lerp(pt, c.yo, c.getY()) - cam.y;
        double z = Mth.lerp(pt, c.zo, c.getZ()) - cam.z;
        float yaw = Mth.rotLerp(pt, c.yRotO, c.getYRot());
        d.render(c, x, y, z, yaw, pt, pose, buf, d.getPackedLightCoords(c, pt));
    }

    // ---------- porta ----------

    /**
     * Uma porta ou alçapão fechado perto (3 a 6 blocos) parece abrir, com o som. Devolve false se não tem.
     * @param soSom SUAVE: só o som da porta, sem mudar nada na tela
     */
    static boolean porta(LocalPlayer p, long agora, boolean soSom) {
        if (porta != null) return false;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel nivel = mc.level;
        if (nivel == null) return false;
        // Nunca no bloco que você está mirando (onde você clicaria).
        BlockPos mirado = mc.hitResult instanceof BlockHitResult alvo && alvo.getType() == HitResult.Type.BLOCK
                ? alvo.getBlockPos() : null;
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
            if (!ehPorta && !(s.getBlock() instanceof TrapDoorBlock)) continue;
            if (mirado != null && (b.equals(mirado) || (ehPorta && b.above().equals(mirado)))) continue;
            achadas.add(b.immutable());
            if (achadas.size() >= 16) break;
        }
        if (achadas.isEmpty()) return false;

        BlockPos pos = achadas.get(p.getRandom().nextInt(achadas.size()));
        BlockState original = nivel.getBlockState(pos);
        boolean alcapao = original.getBlock() instanceof TrapDoorBlock;
        BlockPos outra = alcapao ? null : pos.above();
        BlockState outraOriginal = outra == null ? null : nivel.getBlockState(outra);
        if (outra != null && (!(outraOriginal.getBlock() instanceof DoorBlock)
                || !outraOriginal.hasProperty(BlockStateProperties.OPEN))) {
            return false;
        }
        long fecharEm = agora + RegrasHorror.portaDuracao(p.getRandom()::nextInt);
        Sons.em(Vec3.atCenterOf(pos), alcapao ? SoundEvents.WOODEN_TRAPDOOR_OPEN : SoundEvents.WOODEN_DOOR_OPEN,
                0.6f, 0.6f);
        if (soSom) {
            porta = new Porta(nivel, pos, null, -1, alcapao, fecharEm);
            return true;
        }

        BlockState falsa = original.setValue(BlockStateProperties.OPEN, true);
        // Troca dentro de uma "previsão": o jogo guarda o estado real e devolve ele no fim (ou antes, se você
        // interagir com algum bloco). Atualizações do servidor para este bloco ficam guardadas, nunca se perdem.
        BlockStatePredictionHandler previsao = nivel.getBlockStatePredictionHandler();
        int sequencia;
        try (BlockStatePredictionHandler h = previsao.startPredicting()) {
            sequencia = h.currentSequence();
            trocar(nivel, pos, falsa);
            if (outra != null) trocar(nivel, outra, outraOriginal.setValue(BlockStateProperties.OPEN, true));
        }
        porta = new Porta(nivel, pos, falsa, sequencia, alcapao, fecharEm);
        return true;
    }

    /** Fim da porta falsa: o jogo devolve o estado real que o servidor mandou (porta e metade de cima). */
    private static void fecharPorta(ClientLevel nivel) {
        Porta f = porta;
        porta = null;
        // Mundo trocado (dimensão, saiu): a cópia velha foi jogada fora junto, não há o que desfazer.
        if (f == null || nivel == null || nivel != f.nivel()) return;
        boolean mudou;
        if (f.falsa() == null) {
            mudou = true; // só som: fecha no som também
        } else {
            BlockState antes = nivel.getBlockState(f.pos());
            nivel.handleBlockChangedAck(f.sequencia());
            mudou = nivel.getBlockState(f.pos()) != antes;
        }
        if (mudou) {
            Sons.em(Vec3.atCenterOf(f.pos()), f.alcapao() ? SoundEvents.WOODEN_TRAPDOOR_CLOSE
                    : SoundEvents.WOODEN_DOOR_CLOSE, 0.5f, 0.6f);
        }
    }

    /** Troca só a cópia do cliente, sem avisar vizinhos (2 = redesenhar, 16 = não recalcular formas). */
    private static void trocar(ClientLevel nivel, BlockPos pos, BlockState s) {
        nivel.setBlock(pos, s, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }
}
