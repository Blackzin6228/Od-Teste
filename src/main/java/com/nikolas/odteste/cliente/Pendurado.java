package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nikolas.odteste.historia.vilacalada.RegrasClimaVila;
import com.nikolas.odteste.sistemas.RegrasHorror;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * O Pendurado (lore/roteiro.md, Aparição 2): uma silhueta de aldeão pendurada por uma corrente numa viga, galho ou
 * telhado, no canto da visão. Some quando se olha direto, quando se chega perto ou depois de uns segundos.
 *
 * Igual às alucinações ({@link Alucinacoes}): é só desenho. O aldeão-molde e a corrente **nunca entram no mundo do
 * cliente**: minimapa, radar e Jade não veem, ninguém mais vê, não dá para bater. Desenhado escuro (luz zero), para
 * ser silhueta.
 */
final class Pendurado {
    /** Altura do aldeão: a cabeça encosta na corrente. */
    private static final float ALTURA = 1.95f;
    /** Luz zero: silhueta. */
    private static final int ESCURO = LightTexture.pack(0, 0);
    /** Chegou a 4 blocos: some. */
    private static final double PERTO_SQ = 4 * 4;
    /** Cabeça caída para a frente. */
    private static final float CABECA = 35f;

    private static Villager corpo;
    private static ClientLevel nivel;
    private static BlockPos corrente;
    private static float giroBase;
    private static int idade;
    private static int vida;

    private Pendurado() {}

    static boolean presente() {
        return corpo != null;
    }

    static void limpar() {
        corpo = null;
        nivel = null;
        corrente = null;
    }

    /**
     * Tenta pôr o Pendurado no canto da visão. Até 3 tentativas, uma vez só (nunca por tick).
     * @param suave SUAVE: some em 1 s
     * @return false se não achou viga com 4 blocos livres embaixo, à vista
     */
    static boolean aparecer(LocalPlayer p, boolean suave) {
        if (corpo != null) return false;
        RandomSource r = p.getRandom();
        for (int i = 0; i < 3; i++) {
            float yaw = p.getYRot() + RegrasClimaVila.anguloPendurado(r.nextDouble(), r.nextBoolean());
            BlockPos teto = teto(p, yaw, RegrasClimaVila.distanciaPendurado(r.nextDouble()));
            if (teto == null) continue;
            Vec3 pes = new Vec3(teto.getX() + 0.5, teto.getY() - 1 - ALTURA, teto.getZ() + 0.5);
            if (!aVista(p, pes.add(0, ALTURA * 0.6, 0))) continue;
            Villager v = new Villager(EntityType.VILLAGER, p.level());
            v.setId(-0x0D7F0000); // id negativo: o servidor só usa positivos (o molde nem entra no mundo)
            giroBase = r.nextFloat() * 360f;
            v.moveTo(pes.x, pes.y, pes.z, giroBase, CABECA);
            v.xRotO = CABECA;
            v.yRotO = giroBase;
            v.setYHeadRot(giroBase);
            v.setYBodyRot(giroBase);
            v.yHeadRotO = giroBase;
            v.yBodyRotO = giroBase;
            v.setSilent(true);
            v.setNoGravity(true);
            corpo = v;
            nivel = (ClientLevel) p.level();
            corrente = teto.below();
            idade = 0;
            vida = suave ? RegrasHorror.VIDA_SUAVE : RegrasClimaVila.PENDURADO_VIDA;
            Sons.em(Vec3.atCenterOf(corrente), SoundEvents.CHAIN_STEP, 0.3f, 0.5f);
            return true;
        }
        return false;
    }

    /** Uma vez por tick do cliente. Sem Pendurado, sai na primeira linha. */
    static void aoTick(LocalPlayer p) {
        if (corpo == null) return;
        if (nivel != Minecraft.getInstance().level) {
            limpar();
            return;
        }
        idade++;
        if (idade >= vida || p.distanceToSqr(corpo) <= PERTO_SQ || cosOlhar(p) >= RegrasHorror.COS_OLHOU) {
            limpar(); // some sem barulho
            return;
        }
        // Gira devagar na corrente.
        float giro = giroBase + Mth.sin(idade * 0.06f) * 10f;
        corpo.yRotO = corpo.getYRot();
        corpo.yHeadRotO = corpo.getYHeadRot();
        corpo.yBodyRotO = corpo.yBodyRot;
        corpo.setYRot(giro);
        corpo.setYHeadRot(giro);
        corpo.setYBodyRot(giro);
    }

    private static double cosOlhar(LocalPlayer p) {
        Vec3 olhar = p.getViewVector(1f);
        Vec3 ate = corpo.position().add(0, ALTURA * 0.6, 0).subtract(p.getEyePosition());
        return ate.lengthSqr() < 1e-6 ? 1 : olhar.dot(ate.normalize());
    }

    /**
     * Um bloco sólido (viga, galho, telhado) com 4 blocos livres embaixo: corrente + corpo + pés soltos.
     * Olha no máximo 8 alturas acima do jogador.
     */
    private static BlockPos teto(LocalPlayer p, float yaw, double dist) {
        Level l = p.level();
        double rad = Math.toRadians(yaw);
        int x = Mth.floor(p.getX() - Math.sin(rad) * dist);
        int z = Mth.floor(p.getZ() + Math.cos(rad) * dist);
        int y0 = Mth.floor(p.getY());
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dy = 8; dy >= 3; dy--) {
            m.set(x, y0 + dy, z);
            if (!l.isLoaded(m)) return null;
            if (livre(l, m)) continue;
            if (livre(l, m.below()) && livre(l, m.below(2)) && livre(l, m.below(3)) && livre(l, m.below(4))) {
                return m.immutable();
            }
        }
        return null;
    }

    private static boolean livre(Level l, BlockPos b) {
        return l.getBlockState(b).getCollisionShape(l, b).isEmpty();
    }

    /** Dá para ver daqui (nada sólido no meio)? Um raio só, na hora de aparecer. */
    private static boolean aVista(LocalPlayer p, Vec3 alvo) {
        return p.level().clip(new ClipContext(p.getEyePosition(), alvo, ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE, p)).getType() == HitResult.Type.MISS;
    }

    /** Desenha a corrente e a silhueta, depois das entidades de verdade. */
    static void desenhar(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || corpo == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level != nivel) return;
        Vec3 cam = e.getCamera().getPosition();
        float pt = e.getPartialTick().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        PoseStack pose = e.getPoseStack();

        pose.pushPose();
        pose.translate(corrente.getX() - cam.x, corrente.getY() - cam.y, corrente.getZ() - cam.z);
        mc.getBlockRenderer().renderSingleBlock(Blocks.CHAIN.defaultBlockState(), pose, buf, ESCURO,
                OverlayTexture.NO_OVERLAY);
        pose.popPose();

        EntityRenderDispatcher d = mc.getEntityRenderDispatcher();
        double x = corpo.getX() - cam.x;
        double y = corpo.getY() - cam.y;
        double z = corpo.getZ() - cam.z;
        float yaw = Mth.rotLerp(pt, corpo.yRotO, corpo.getYRot());
        d.render(corpo, x, y, z, yaw, pt, pose, buf, ESCURO);
        buf.endBatch();
    }
}
