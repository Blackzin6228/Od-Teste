package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.compat.lodestone.BrilhoLodestone;
import com.nikolas.odteste.compat.playeranimator.AnimacaoJogador;
import com.nikolas.odteste.magias.IdsMagia;
import com.nikolas.odteste.magias.RegrasTalhoRubro;
import com.nikolas.odteste.rede.FaseMagia;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Visual das magias no cliente (Talho Rubro). O servidor manda só "começou a preparação" e "golpe";
 * o resto nasce aqui e some sozinho. Nada entra no mundo de verdade: as manchas no chão são quadrados
 * desenhados por evento de render (nenhum bloco, nenhuma entidade), e somem em 3 s.
 *
 * Fases do Talho Rubro (docs/magias.md, nível Apex Arcana proporcional ao Básico):
 * 1. preparação (0,3 s): o sangue se junta na mão, batida grave;
 * 2. golpe: meia-lua vermelha na diagonal, corte grave e som molhado; borda da tela pulsa de leve (só quem conjura);
 * 3. rastro: gotas grossas voam do corte, caem e mancham o chão por 3 s, com pingos graves.
 * Sons vanilla com tom baixo (0,5 a 0,7). Com Lodestone no pack, o corte ganha brilho; sem ele, pó vanilla.
 *
 * Limites (sem lag): no máximo {@link #MAX_GOTAS} gotas e {@link #MAX_MANCHAS} manchas no total; sem nada, cada
 * evento sai na primeira linha. Se o desenho der erro, a parte que falhou se desliga e a magia continua.
 */
final class MagiaCliente {
    private static final boolean ANIMADOR = Compat.Integracao.PLAYER_ANIMATOR.ligada();
    /** Desliga sozinho se o Lodestone der erro (versão diferente, por exemplo): volta para o pó vanilla. */
    private static boolean lodestone = Compat.Integracao.LODESTONE.ligada();
    /** Desliga sozinho se o desenho das manchas der erro. */
    private static boolean manchasLigadas = true;
    /** Desliga sozinho se o efeito de tela der erro. */
    private static boolean telaLigada = true;

    static final int MAX_GOTAS = 48;
    static final int MAX_MANCHAS = 48;
    /** Pingos no chão por golpe (o resto cai em silêncio). */
    private static final int MAX_PINGOS_SOM = 3;
    private static final int GOTA_VIDA_MAX = 40;

    /** Vermelho vivo do corte, vermelho escuro das gotas e cor da mancha (sangue velho). */
    private static final int COR_CORTE = 0xB3121A;
    private static final int COR_BORDA_TELA = 0x7A0A10;
    private static final DustParticleOptions PO_CORTE = new DustParticleOptions(new Vector3f(0.70f, 0.05f, 0.07f), 1.6f);
    private static final DustParticleOptions PO_ESCURO = new DustParticleOptions(new Vector3f(0.36f, 0.01f, 0.02f), 1.3f);
    private static final DustParticleOptions PO_GOTA = new DustParticleOptions(new Vector3f(0.45f, 0.02f, 0.03f), 1.5f);
    private static final float MANCHA_R = 0.30f, MANCHA_G = 0.02f, MANCHA_B = 0.03f;

    private static final class Gota {
        Vec3 pos;
        Vec3 vel;
        int idade;
        boolean comSom;
    }

    private record Mancha(double x, double y, double z, float raio, float[] borda, float giro, long nasceu, float luz) {}

    private static final List<Gota> GOTAS = new ArrayList<>();
    private static final List<Mancha> MANCHAS = new ArrayList<>();
    private static long tick;

    private MagiaCliente() {}

    static void iniciar() {
        FaseMagia.definirReceptor(MagiaCliente::receber);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> aoTick());
        NeoForge.EVENT_BUS.addListener(MagiaCliente::desenhar);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> limpar());
    }

    static void limpar() {
        GOTAS.clear();
        MANCHAS.clear();
    }

    // ---------- pacote ----------

    static void receber(FaseMagia f) {
        if (!IdsMagia.TALHO_RUBRO.equals(f.magia())) return; // magia que este cliente não conhece
        Minecraft mc = Minecraft.getInstance();
        ClientLevel nivel = mc.level;
        if (nivel == null) return;
        Entity quem = nivel.getEntity(f.entidade());
        boolean souEu = mc.player != null && quem == mc.player;
        Vec3 pes = new Vec3(f.x(), f.y(), f.z());
        double giro = Math.toRadians(f.giro());
        Vec3 frente = new Vec3(-Math.sin(giro), 0, Math.cos(giro));
        Vec3 direita = new Vec3(-Math.cos(giro), 0, -Math.sin(giro));
        RandomSource r = RandomSource.create(f.semente());
        if (f.fase() == FaseMagia.PREPARACAO) {
            preparacao(nivel, pes, frente, direita, r, souEu);
            // Gancho do Player Animator: a animação (golpe de mão aberta em diagonal) vem do Modelador.
            // Enquanto ela não existir, tocar() não faz nada.
            if (ANIMADOR && quem instanceof AbstractClientPlayer jogador) AnimacaoJogador.tocar(jogador, IdsMagia.TALHO_RUBRO);
        } else if (f.fase() == FaseMagia.GOLPE) {
            golpe(nivel, pes, frente, direita, r, souEu);
        }
    }

    // ---------- fase 1: preparação ----------

    private static void preparacao(ClientLevel nivel, Vec3 pes, Vec3 frente, Vec3 direita, RandomSource r, boolean souEu) {
        Vec3 mao = pes.add(0, 1.3, 0).add(direita.scale(0.45)).add(frente.scale(0.35));
        // O sangue se junta na mão: pontos num anel que correm para o centro.
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2 * i / 12 + r.nextDouble() * 0.4;
            Vec3 de = mao.add(Math.cos(a) * 0.7, (r.nextDouble() - 0.5) * 0.6, Math.sin(a) * 0.7);
            Vec3 v = mao.subtract(de).scale(0.25);
            if (!brilho(nivel, de, v, 0.18f, 0.7f, COR_CORTE, 8)) {
                nivel.addParticle(PO_ESCURO, de.x, de.y, de.z, v.x * 10, v.y * 10, v.z * 10);
            }
        }
        som(nivel, mao, SoundEvents.WARDEN_HEARTBEAT, 0.7f, 0.6f);
        som(nivel, mao, SoundEvents.HONEY_BLOCK_SLIDE, 0.5f, 0.55f);
        if (souEu) tela(() -> Tela.vinheta(2, 3, 4, 0.12f * escalaTela()));
    }

    // ---------- fase 2: golpe ----------

    private static void golpe(ClientLevel nivel, Vec3 pes, Vec3 frente, Vec3 direita, RandomSource r, boolean souEu) {
        double meio = Math.toRadians(RegrasTalhoRubro.MEIO_ANGULO_GRAUS);
        double alcance = RegrasTalhoRubro.ALCANCE;
        int passos = 26;
        int pingos = 0;
        for (int i = 0; i < passos; i++) {
            double t = i / (double) (passos - 1);
            double ang = meio - 2 * meio * t; // da direita para a esquerda
            Vec3 dir = frente.scale(Math.cos(ang)).add(direita.scale(Math.sin(ang)));
            double altura = 1.75 - 1.0 * t; // diagonal: começa alto à direita, termina baixo à esquerda
            double grossura = Math.sin(Math.PI * t); // meia-lua: grossa no meio, fina nas pontas
            for (int camada = 0; camada < 3; camada++) {
                double raio = alcance * (0.55 + 0.15 * camada) + grossura * 0.25 * (camada - 1);
                Vec3 p = pes.add(dir.scale(raio)).add(0, altura + (r.nextDouble() - 0.5) * 0.1, 0);
                boolean borda = camada == 2;
                if (!brilho(nivel, p, dir.scale(0.02), borda ? 0.32f : 0.22f, borda ? 0.85f : 0.6f, COR_CORTE, 10 + camada * 3)) {
                    nivel.addParticle(borda ? PO_CORTE : PO_ESCURO, p.x, p.y, p.z, 0, 0, 0);
                }
            }
            // Rastro: gotas grossas saem do corte (uma a cada dois passos, até o limite).
            if (i % 2 == 0 && GOTAS.size() < MAX_GOTAS) {
                Gota g = new Gota();
                g.pos = pes.add(dir.scale(alcance * (0.6 + 0.3 * r.nextDouble()))).add(0, altura, 0);
                g.vel = dir.scale(0.06 + r.nextDouble() * 0.06).add(0, 0.04 + r.nextDouble() * 0.08, 0);
                g.comSom = pingos++ < MAX_PINGOS_SOM && r.nextBoolean();
                GOTAS.add(g);
            }
        }
        Vec3 centro = pes.add(frente.scale(alcance * 0.6)).add(0, 1.2, 0);
        nivel.addParticle(ParticleTypes.SWEEP_ATTACK, centro.x, centro.y, centro.z, 0, 0, 0);
        som(nivel, centro, SoundEvents.PLAYER_ATTACK_SWEEP, 0.9f, 0.6f);
        som(nivel, centro, SoundEvents.HONEY_BLOCK_BREAK, 0.8f, 0.5f);
        som(nivel, centro, SoundEvents.SLIME_SQUISH, 0.5f, 0.65f);
        // Tela: pulso vermelho fraco na borda, uma batida só (sem flash; DESLIGADO não mostra; SUAVE pela metade).
        if (souEu) tela(() -> Tela.pulso(COR_BORDA_TELA, 12, 1, 0.22f * escalaTela() * ConfigCliente.tremor()));
    }

    // ---------- fase 3: rastro (gotas e manchas) ----------

    private static void aoTick() {
        tick++;
        if (GOTAS.isEmpty() && MANCHAS.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel nivel = mc.level;
        if (nivel == null || mc.isPaused()) return;
        for (Iterator<Gota> it = GOTAS.iterator(); it.hasNext(); ) {
            Gota g = it.next();
            Vec3 antes = g.pos;
            g.vel = new Vec3(g.vel.x * 0.96, g.vel.y * 0.98 - 0.045, g.vel.z * 0.96);
            Vec3 depois = antes.add(g.vel);
            BlockHitResult bate = nivel.clip(new ClipContext(antes, depois, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY, CollisionContext.empty()));
            if (bate.getType() == HitResult.Type.BLOCK) {
                it.remove();
                Vec3 p = bate.getLocation();
                if (bate.getDirection() == Direction.UP) {
                    manchar(nivel, p, g.pos.hashCode() ^ g.idade);
                    if (g.comSom) som(nivel, p, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER, 0.6f, 0.5f + nivel.random.nextFloat() * 0.2f);
                }
                for (int i = 0; i < 3; i++) nivel.addParticle(PO_GOTA, p.x, p.y + 0.05, p.z, 0, 0, 0);
                continue;
            }
            g.pos = depois;
            if (++g.idade > GOTA_VIDA_MAX) {
                it.remove();
                continue;
            }
            nivel.addParticle(PO_GOTA, depois.x, depois.y, depois.z, 0, 0, 0);
        }
        MANCHAS.removeIf(m -> tick - m.nasceu() > RegrasTalhoRubro.RASTRO_TICKS || tick < m.nasceu());
    }

    private static void manchar(ClientLevel nivel, Vec3 p, int semente) {
        if (MANCHAS.size() >= MAX_MANCHAS) MANCHAS.remove(0);
        RandomSource r = RandomSource.create(semente);
        float[] borda = new float[8];
        for (int i = 0; i < borda.length; i++) borda[i] = 0.7f + r.nextFloat() * 0.5f;
        // A mancha não brilha no escuro: a cor acompanha a luz do lugar (lida uma vez, ao nascer).
        float luz = 0.25f + 0.75f * nivel.getMaxLocalRawBrightness(BlockPos.containing(p.x, p.y + 0.1, p.z)) / 15f;
        MANCHAS.add(new Mancha(p.x, p.y + 0.012, p.z, 0.16f + r.nextFloat() * 0.22f, borda,
                r.nextFloat() * (float) Math.PI * 2, tick, luz));
    }

    private static void desenhar(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || MANCHAS.isEmpty() || !manchasLigadas) return;
        try {
            Minecraft mc = Minecraft.getInstance();
            Vec3 cam = e.getCamera().getPosition();
            float agora = tick + e.getPartialTick().getGameTimeDeltaPartialTick(false);
            PoseStack pose = e.getPoseStack();
            pose.pushPose();
            pose.translate(-cam.x, -cam.y, -cam.z);
            Matrix4f m = pose.last().pose();
            MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
            VertexConsumer vc = buf.getBuffer(RenderType.debugQuads());
            for (Mancha mancha : MANCHAS) desenharMancha(vc, m, mancha, agora);
            buf.endBatch(RenderType.debugQuads());
            pose.popPose();
        } catch (RuntimeException erro) {
            manchasLigadas = false;
            OdMod.LOGGER.warn("{}: desenho das manchas desligado (erro: {}). A magia continua.", OdMod.MODID, erro.toString());
        }
    }

    /** Mancha = octógono torto (centro + 8 pontos), em 4 quadriláteros. Espalha nos primeiros ticks e some no fim. */
    private static void desenharMancha(VertexConsumer vc, Matrix4f m, Mancha mancha, float agora) {
        float idade = agora - mancha.nasceu();
        float vida = RegrasTalhoRubro.RASTRO_TICKS;
        float espalha = Math.min(1f, 0.45f + idade / 5f);
        float some = idade > vida - 20 ? Math.max(0f, (vida - idade) / 20f) : 1f;
        float a = 0.82f * some;
        if (a <= 0.01f) return;
        float r = MANCHA_R * mancha.luz(), g = MANCHA_G * mancha.luz(), b = MANCHA_B * mancha.luz();
        float[] xs = new float[8];
        float[] zs = new float[8];
        for (int i = 0; i < 8; i++) {
            double ang = mancha.giro() + Math.PI * 2 * i / 8;
            float raio = mancha.raio() * mancha.borda()[i] * espalha;
            xs[i] = (float) (mancha.x() + Math.cos(ang) * raio);
            zs[i] = (float) (mancha.z() + Math.sin(ang) * raio);
        }
        float cx = (float) mancha.x(), cy = (float) mancha.y(), cz = (float) mancha.z();
        for (int q = 0; q < 8; q += 2) {
            int i1 = (q + 1) % 8, i2 = (q + 2) % 8;
            vc.addVertex(m, cx, cy, cz).setColor(r, g, b, a);
            vc.addVertex(m, xs[q], cy, zs[q]).setColor(r, g, b, a);
            vc.addVertex(m, xs[i1], cy, zs[i1]).setColor(r, g, b, a);
            vc.addVertex(m, xs[i2], cy, zs[i2]).setColor(r, g, b, a);
        }
    }

    // ---------- ajudas ----------

    /** Brilho do Lodestone, se tiver. Devolve false para o chamador usar o pó vanilla. */
    private static boolean brilho(ClientLevel nivel, Vec3 p, Vec3 v, float tamanho, float opacidade, int cor, int vida) {
        if (!lodestone) return false;
        try {
            BrilhoLodestone.brilho(nivel, p.x, p.y, p.z, v.x, v.y, v.z, tamanho, opacidade, cor, vida);
            return true;
        } catch (Throwable erro) { // LinkageError também: API do Lodestone diferente da esperada
            lodestone = false;
            OdMod.LOGGER.warn("{}: brilho do Lodestone desligado (erro: {}). Usando partículas vanilla.", OdMod.MODID, erro.toString());
            return false;
        }
    }

    private static void som(ClientLevel nivel, Vec3 p, SoundEvent som, float volume, float tom) {
        nivel.playLocalSound(p.x, p.y, p.z, som, SoundSource.PLAYERS, volume, tom, false);
    }

    /** Opção vanilla "Efeitos de distorção" (acessibilidade): 0 a 1. */
    private static float escalaTela() {
        return Minecraft.getInstance().options.screenEffectScale().get().floatValue();
    }

    private static void tela(Runnable efeito) {
        if (!telaLigada) return;
        try {
            efeito.run();
        } catch (RuntimeException erro) {
            telaLigada = false;
            OdMod.LOGGER.warn("{}: efeito de tela das magias desligado (erro: {}).", OdMod.MODID, erro.toString());
        }
    }
}
