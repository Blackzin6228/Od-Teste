package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.compat.iris.PackIris;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Base dos efeitos de tela com shader (cor + profundidade + matrizes). Só cliente.
 *
 * <p><b>Sem shader pack:</b> desenha no fim do mundo ({@code AFTER_LEVEL}), antes da mão e da HUD, lendo uma cópia
 * da cor e da profundidade da tela.
 *
 * <p><b>Com shader pack (Iris):</b> o pack só termina a imagem depois do mundo. Então, no {@code AFTER_LEVEL}, só
 * guardamos as matrizes e copiamos a profundidade; o desenho vai no começo da HUD ({@code RenderGuiEvent.Pre}),
 * por cima da imagem pronta do pack. A profundidade copiada é conferida de vez em quando: se o pack não deixou
 * nada útil (tudo 0 ou tudo 1 olhando para o chão), o efeito roda na versão sem profundidade
 * ({@code ComProfundidade = 0}: borda, tela e névoa pela altura da câmera), em vez de desligar.
 *
 * <p><b>Falhou?</b> Shader que não compila ou que quebra ao desenhar é desligado só ele, até sair do jogo, e
 * fica no log. Nunca crash.
 *
 * <p>Técnica (copiar a tela, ler a profundidade, desenhar depois do pack) aprendida no Apex Arcana (MIT);
 * o código é nosso (docs/referencias.md).
 */
final class FxTela {
    /** Um efeito de tela: qual shader, se está ligado agora e os uniforms dele. */
    interface Passe {
        String shader();

        boolean ativo();

        void uniforms(ShaderInstance sh, Quadro q);
    }

    /** O que vale no quadro atual (capturado no AFTER_LEVEL). */
    record Quadro(Matrix4f vista, Matrix4f projecao, Vec3 camera, float parcial, boolean comProfundidade) {}

    private static final Map<String, ShaderInstance> SHADERS = new HashMap<>();
    private static final Set<String> QUEBRADOS = new HashSet<>();
    private static final Set<String> AVISADOS = new HashSet<>();
    private static List<Passe> passes = List.of();

    private static RenderTarget copia;
    private static final Matrix4f VISTA = new Matrix4f();
    private static final Matrix4f PROJECAO = new Matrix4f();
    private static Vec3 camera = Vec3.ZERO;
    private static float parcial;
    /** Com pack: o AFTER_LEVEL deste quadro guardou matrizes e profundidade, falta desenhar na HUD. */
    private static boolean esperandoHud;

    // ---- profundidade com shader pack
    private static boolean profundidadePack;
    private static long conferidoEm;
    private static final long CONFERIR_NS = 2_000_000_000L;
    private static final float[] AMOSTRA = new float[1];

    private static final boolean IRIS = Compat.Integracao.IRIS.ligada();

    private FxTela() {}

    static void iniciar(IEventBus barramentoDoMod, List<Passe> lista) {
        passes = List.copyOf(lista);
        barramentoDoMod.addListener(FxTela::aoRegistrarShaders);
        NeoForge.EVENT_BUS.addListener(FxTela::aoMundo);
        NeoForge.EVENT_BUS.addListener(FxTela::aoComecoDaHud);
    }

    static boolean packLigado() {
        return IRIS && PackIris.packLigado();
    }

    /** Para o log e o comando: qual caminho está valendo. */
    static String caminho() {
        if (!packLigado()) return "comum";
        return profundidadePack ? "pack+profundidade" : "pack-sem-profundidade";
    }

    static boolean quebrado(String shader) {
        return QUEBRADOS.contains(shader) || !SHADERS.containsKey(shader);
    }

    // ---------------------------------------------------------------- carregar

    private static void aoRegistrarShaders(RegisterShadersEvent e) {
        for (Passe p : passes) {
            String nome = p.shader();
            try {
                e.registerShader(new ShaderInstance(e.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(OdMod.MODID, nome), DefaultVertexFormat.POSITION_TEX),
                        sh -> {
                            SHADERS.put(nome, sh);
                            QUEBRADOS.remove(nome);
                        });
            } catch (Throwable falhou) {
                SHADERS.remove(nome);
                QUEBRADOS.add(nome);
                OdMod.LOGGER.error("{}: o efeito de tela '{}' não compilou nesta placa de vídeo e fica de fora.",
                        OdMod.MODID, nome, falhou);
            }
        }
    }

    // ---------------------------------------------------------------- quando desenhar

    private static boolean algumAtivo() {
        for (Passe p : passes) {
            if (!quebrado(p.shader()) && p.ativo()) return true;
        }
        return false;
    }

    private static void aoMundo(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        esperandoHud = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !algumAtivo()) return;
        VISTA.set(e.getModelViewMatrix()).mul(e.getPoseStack().last().pose());
        PROJECAO.set(e.getProjectionMatrix());
        camera = e.getCamera().getPosition();
        parcial = e.getPartialTick().getGameTimeDeltaPartialTick(false);
        if (packLigado()) {
            // O pack ainda vai terminar a imagem: aqui só a profundidade (se ele deixar).
            try {
                pegar(mc.getMainRenderTarget(), GL11.GL_DEPTH_BUFFER_BIT);
                conferirProfundidade(mc);
            } catch (Throwable falhou) {
                profundidadePack = false;
                avisarUmaVez("profundidade", falhou);
            }
            esperandoHud = true;
            return;
        }
        desenharTodos(mc, true, true);
    }

    private static void aoComecoDaHud(RenderGuiEvent.Pre e) {
        if (!esperandoHud) return;
        esperandoHud = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !packLigado()) return;
        desenharTodos(mc, profundidadePack, false);
    }

    private static void desenharTodos(Minecraft mc, boolean comProfundidade, boolean copiarProfundidade) {
        Quadro q = new Quadro(new Matrix4f(VISTA), new Matrix4f(PROJECAO), camera, parcial, comProfundidade);
        boolean primeira = true;
        for (Passe p : passes) {
            if (quebrado(p.shader()) || !p.ativo()) continue;
            int bits = GL11.GL_COLOR_BUFFER_BIT | (primeira && copiarProfundidade ? GL11.GL_DEPTH_BUFFER_BIT : 0);
            desenhar(mc, p, q, bits);
            primeira = false;
        }
    }

    // ---------------------------------------------------------------- cópia da tela

    private static void garantir(RenderTarget principal) {
        if (copia == null) {
            copia = new TextureTarget(principal.width, principal.height, true, Minecraft.ON_OSX);
            copia.setFilterMode(GL11.GL_LINEAR);
        } else if (copia.width != principal.width || copia.height != principal.height) {
            copia.resize(principal.width, principal.height, Minecraft.ON_OSX);
            copia.setFilterMode(GL11.GL_LINEAR);
        }
    }

    /** Copia cor e/ou profundidade da tela para a cópia (o shader lê dela e escreve na tela). */
    private static void pegar(RenderTarget principal, int bits) {
        garantir(principal);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, principal.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, copia.frameBufferId);
        if ((bits & GL11.GL_COLOR_BUFFER_BIT) != 0) {
            GL30.glBlitFramebuffer(0, 0, principal.width, principal.height, 0, 0, copia.width, copia.height,
                    GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        }
        if ((bits & GL11.GL_DEPTH_BUFFER_BIT) != 0) {
            GL30.glBlitFramebuffer(0, 0, principal.width, principal.height, 0, 0, copia.width, copia.height,
                    GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        }
        principal.bindWrite(false);
    }

    /**
     * Com pack: a profundidade copiada presta? A cada 2 s lê três pontos da metade de baixo da tela (um
     * custo pequeno). Algum valor no meio = presta. Tudo 0 ou tudo 1 olhando para baixo = o pack não deixou.
     */
    private static void conferirProfundidade(Minecraft mc) {
        long agora = System.nanoTime();
        if (agora - conferidoEm < CONFERIR_NS) return;
        conferidoEm = agora;
        RenderTarget principal = mc.getMainRenderTarget();
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, copia.frameBufferId);
        boolean algumMeio = false;
        boolean tudoVazio = true;
        int[][] pontos = {{copia.width / 2, copia.height / 5}, {copia.width / 4, copia.height / 3},
                {copia.width * 3 / 4, copia.height / 3}};
        for (int[] pt : pontos) {
            AMOSTRA[0] = 1f;
            GL11.glReadPixels(pt[0], pt[1], 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, AMOSTRA);
            float z = AMOSTRA[0];
            if (z > 0.0001f && z < 0.99999f) algumMeio = true;
            if (z > 0.0001f && z < 0.99999f) tudoVazio = false;
        }
        principal.bindWrite(false);
        boolean olhandoParaBaixo = mc.player != null && mc.player.getXRot() > 25f;
        boolean antes = profundidadePack;
        if (algumMeio) profundidadePack = true;
        else if (tudoVazio && olhandoParaBaixo) profundidadePack = false;
        if (antes != profundidadePack) {
            OdMod.LOGGER.info("{}: shader pack {} a profundidade; efeitos de tela {}.", OdMod.MODID,
                    profundidadePack ? "deixa ler" : "não deixa ler",
                    profundidadePack ? "com profundidade" : "na versão sem profundidade");
        }
    }

    // ---------------------------------------------------------------- desenhar um efeito

    private static void desenhar(Minecraft mc, Passe passe, Quadro q, int bits) {
        String nome = passe.shader();
        ShaderInstance sh = SHADERS.get(nome);
        if (sh == null) return;
        RenderTarget principal = mc.getMainRenderTarget();
        int w = principal.width;
        int h = principal.height;
        if (w <= 0 || h <= 0) return;
        Matrix4f projecaoAntes = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting ordemAntes = RenderSystem.getVertexSorting();
        Matrix4fStack vista = RenderSystem.getModelViewStack();
        boolean empilhou = false;
        try {
            pegar(principal, bits);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0f, w, h, 0f, 1000f, 3000f),
                    VertexSorting.ORTHOGRAPHIC_Z);
            vista.pushMatrix();
            empilhou = true;
            vista.identity();
            vista.translate(0f, 0f, -2000f);
            RenderSystem.applyModelViewMatrix();

            sh.safeGetUniform("InvProj").set(new Matrix4f(q.projecao()).invert());
            sh.safeGetUniform("InvView").set(new Matrix4f(q.vista()).invert());
            sh.safeGetUniform("ProjVista").set(new Matrix4f(q.projecao()).mul(q.vista()));
            sh.safeGetUniform("ScreenSize").set((float) w, (float) h);
            sh.safeGetUniform("ComProfundidade").set(q.comProfundidade() ? 1f : 0f);
            passe.uniforms(sh, q);

            RenderSystem.setShader(() -> sh);
            RenderSystem.setShaderTexture(0, copia.getColorTextureId());
            RenderSystem.setShaderTexture(1, copia.getDepthTextureId());
            BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            b.addVertex(0f, h, 0f).setUv(0f, 0f);
            b.addVertex(w, h, 0f).setUv(1f, 0f);
            b.addVertex(w, 0f, 0f).setUv(1f, 1f);
            b.addVertex(0f, 0f, 0f).setUv(0f, 1f);
            BufferUploader.drawWithShader(b.buildOrThrow());
        } catch (Throwable falhou) {
            QUEBRADOS.add(nome);
            avisarUmaVez(nome, falhou);
        } finally {
            if (empilhou) {
                vista.popMatrix();
                RenderSystem.applyModelViewMatrix();
            }
            RenderSystem.setProjectionMatrix(projecaoAntes, ordemAntes);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            principal.bindWrite(false);
            religarBlend();
        }
    }

    /** Deixa o cache de estado do jogo igual ao que está na placa (um shader vanilla aplicado e limpo). */
    private static void religarBlend() {
        ShaderInstance vanilla = GameRenderer.getPositionTexColorShader();
        if (vanilla != null) {
            vanilla.apply();
            vanilla.clear();
        }
    }

    private static void avisarUmaVez(String nome, Throwable falhou) {
        if (!AVISADOS.add(nome)) return;
        OdMod.LOGGER.warn("{}: efeito de tela '{}' desligado até sair do jogo (o resto continua).", OdMod.MODID, nome,
                falhou);
    }

    /** Ao sair do mundo: solta a cópia da tela (volta a ser criada quando precisar). */
    static void limpar() {
        esperandoHud = false;
        if (copia != null) {
            RenderTarget c = copia;
            copia = null;
            RenderSystem.recordRenderCall(c::destroyBuffers);
        }
    }
}
