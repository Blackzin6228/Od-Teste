package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.nikolas.odteste.sistemas.RegrasHorror;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Efeitos de tela (só cliente, só para o próprio jogador): vinheta escura, borda colorida, escurecer e tremor.
 * Sem shader: usa a textura de vinheta do próprio Minecraft. Tudo tem duração e limite;
 * desenhar custa duas texturas por quadro no máximo.
 */
final class Tela {
    private static final ResourceLocation VINHETA = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    /** No máximo três bordas coloridas ao mesmo tempo (as mais velhas saem). */
    private static final int MAX_PULSOS = 3;

    /** Relógio do cliente, em ticks (avançado em EfeitosLucidez). */
    static long tick;

    /** Vinheta da faixa de Lucidez (0 a 1). O alvo vem de EfeitosLucidez; aqui só suaviza. */
    static float vinhetaAlvo;
    private static float vinhetaAtual;
    /**
     * Tela "lavada" (0 a 1): cor puxada para um cinza escuro. Aproximação de dessaturação sem shader
     * (não briga com shaders do pack). Só com efeitos fortes.
     */
    static float dessaturarAlvo;
    private static float dessaturarAtual;
    /** Cinza escuro e frio da tela lavada: tira cor sem clarear a noite. */
    private static final int CINZA_LAVADO = 0x2E2F34;

    private record Pulso(int cor, long inicio, int duracao, int batidas, float forca) {
        float valor(float t) {
            float x = (t - inicio) / duracao;
            if (x < 0 || x > 1) return 0;
            float forma = batidas <= 1 ? (float) Math.sin(Math.PI * x)
                    : (float) Math.abs(Math.sin(Math.PI * batidas * x));
            return forca * forma;
        }
    }

    /** Envelope: sobe em {@code entrada}, segura, desce em {@code saida}. */
    private record Envelope(long inicio, int entrada, int segura, int saida, float forca) {
        float valor(float t) {
            float d = t - inicio;
            if (d < 0) return 0;
            if (d < entrada) return forca * d / Math.max(1, entrada);
            d -= entrada;
            if (d < segura) return forca;
            d -= segura;
            if (d < saida) return forca * (1 - d / Math.max(1, saida));
            return 0;
        }

        boolean acabou(float t) {
            return t - inicio > entrada + segura + saida;
        }
    }

    private static final List<Pulso> PULSOS = new ArrayList<>();
    /** Escurecer pela vinheta (bordas para dentro). */
    private static Envelope vinhetaMomento;
    /** Escurecer a tela inteira (surto, Escuridão curta). */
    private static Envelope escuro;
    private static Envelope tremor;
    private static Envelope inclinar;
    private static float inclinarLado = 1;

    private Tela() {}

    // ---------- pedidos (chamados por EfeitosLucidez e MomentosCliente) ----------

    /** Borda colorida. Com "Efeitos fortes" desligado, não aparece (fica só vinheta e som). */
    static void pulso(int cor, int duracaoTicks, int batidas, float forca) {
        if (ConfigCliente.soVinhetaESom()) return;
        if (PULSOS.size() >= MAX_PULSOS) PULSOS.remove(0);
        PULSOS.add(new Pulso(cor, tick, Math.max(1, duracaoTicks), batidas, forca));
    }

    static void vinheta(int entrada, int segura, int saida, float forca) {
        vinhetaMomento = new Envelope(tick, entrada, segura, saida, forca);
    }

    /** Tela inteira escura. Com "Efeitos fortes" desligado, vira só a vinheta (bordas), no mesmo tempo. */
    static void escurecer(int entrada, int segura, int saida, float forca) {
        if (ConfigCliente.soVinhetaESom()) {
            vinheta(entrada, segura, saida, forca);
            return;
        }
        escuro = new Envelope(tick, entrada, segura, saida, forca);
    }

    /** Tremor em graus. Obedece à opção de acessibilidade. */
    static void tremer(int duracao, float graus) {
        if (ConfigCliente.tremor() <= 0) return;
        tremor = new Envelope(tick, 2, Math.max(0, duracao - 6), 4, graus);
    }

    static void inclinar(int duracao, float graus) {
        if (ConfigCliente.tremor() <= 0) return;
        inclinarLado = Math.random() < 0.5 ? -1 : 1;
        inclinar = new Envelope(tick, duracao / 3, duracao / 3, duracao / 3, graus);
    }

    static void limpar() {
        PULSOS.clear();
        vinhetaMomento = escuro = tremor = inclinar = null;
        vinhetaAtual = vinhetaAlvo = 0;
        dessaturarAtual = dessaturarAlvo = 0;
        tremorMira = 0;
    }

    /** Uma vez por tick: suaviza a vinheta e joga fora o que acabou. */
    static void aoTick() {
        tick++;
        vinhetaAtual += (vinhetaAlvo - vinhetaAtual) * 0.08f;
        float lavar = ConfigCliente.soVinhetaESom() ? 0f : dessaturarAlvo;
        dessaturarAtual += (lavar - dessaturarAtual) * 0.02f; // entra e sai devagar (uns 2 s)
        float t = tick;
        PULSOS.removeIf(p -> t - p.inicio() > p.duracao());
        if (vinhetaMomento != null && vinhetaMomento.acabou(t)) vinhetaMomento = null;
        if (escuro != null && escuro.acabou(t)) escuro = null;
        if (tremor != null && tremor.acabou(t)) tremor = null;
        if (inclinar != null && inclinar.acabou(t)) inclinar = null;
    }

    // ---------- desenho ----------

    static void desenhar(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float t = tick + delta.getGameTimeDeltaPartialTick(false);
        int w = g.guiWidth();
        int h = g.guiHeight();

        float escurecerBorda = vinhetaAtual + (vinhetaMomento == null ? 0 : vinhetaMomento.valor(t));
        boolean temPulso = false;
        for (Pulso p : PULSOS) temPulso |= p.valor(t) > 0.01f;
        float tudo = escuro == null ? 0 : escuro.valor(t);
        float lavar = dessaturarAtual;
        if (escurecerBorda < 0.01f && !temPulso && tudo < 0.01f && lavar < 0.01f) return;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        // 0) Tela lavada: um véu cinza escuro por cima de tudo (tira a cor, quase não clareia o escuro).
        if (lavar > 0.01f) {
            RenderSystem.defaultBlendFunc();
            int a = Math.min(200, (int) (lavar * 255));
            g.fill(0, 0, w, h, (a << 24) | CINZA_LAVADO);
        }

        // 1) Bordas escuras: a vinheta do jogo multiplicada (mesmo truque da vinheta vanilla).
        if (escurecerBorda > 0.01f) {
            float k = Math.min(1f, escurecerBorda);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
            g.setColor(k, k, k, 1f);
            g.blit(VINHETA, 0, 0, -90, 0f, 0f, w, h, w, h);
        }

        // 2) Bordas coloridas: escurece um pouco e soma a cor do elemento por cima.
        for (Pulso p : PULSOS) {
            float k = Math.min(1f, p.valor(t));
            if (k < 0.01f) continue;
            float r = ((p.cor() >> 16) & 0xFF) / 255f;
            float gg = ((p.cor() >> 8) & 0xFF) / 255f;
            float b = (p.cor() & 0xFF) / 255f;
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
            g.setColor(k * 0.6f, k * 0.6f, k * 0.6f, 1f);
            g.blit(VINHETA, 0, 0, -90, 0f, 0f, w, h, w, h);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            g.setColor(r * k, gg * k, b * k, 1f);
            g.blit(VINHETA, 0, 0, -90, 0f, 0f, w, h, w, h);
        }

        g.setColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();

        // 3) Tela inteira escura (surto, Escuridão curta).
        if (tudo > 0.01f) {
            int a = Math.min(255, (int) (tudo * 255));
            g.fill(0, 0, w, h, a << 24);
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    // ---------- câmera ----------

    /** Ângulo extra da mira tremendo (arco com Lucidez baixa), em graus. EfeitosLucidez preenche. */
    static float tremorMira;

    static void aoCalcularCamera(ViewportEvent.ComputeCameraAngles e) {
        float forca = ConfigCliente.tremor();
        if (forca <= 0) return;
        float t = (float) (tick + e.getPartialTick());
        float amp = (tremor == null ? 0 : tremor.valor(t)) * forca;
        if (amp > 0.001f) {
            e.setYaw(e.getYaw() + (float) Math.sin(t * 2.7) * amp);
            e.setPitch(e.getPitch() + (float) Math.cos(t * 3.3) * amp * 0.7f);
        }
        float mira = tremorMira * forca;
        if (mira > 0.001f) {
            e.setYaw(e.getYaw() + (float) (Math.sin(t * 0.9) + 0.5 * Math.sin(t * 2.3)) * mira);
            e.setPitch(e.getPitch() + (float) (Math.cos(t * 0.7) + 0.5 * Math.sin(t * 1.9)) * mira);
        }
        if (inclinar != null) {
            e.setRoll(e.getRoll() + inclinar.valor(t) * inclinarLado * forca);
        }
        // Balanço: a cabeça pende devagar, como tontura (só com o nível de horror alto).
        float balanco = RegrasHorror.balanco(EfeitosLucidez.nivel) * forca;
        if (balanco > 0.001f) {
            e.setRoll(e.getRoll() + (float) (Math.sin(t * 0.045) + 0.4 * Math.sin(t * 0.11)) * balanco);
            e.setYaw(e.getYaw() + (float) Math.sin(t * 0.031) * balanco * 0.4f);
        }
    }

    /** Distorção leve: a visão "respira" (o campo de visão abre e fecha um pouco). */
    static void aoCalcularFov(ViewportEvent.ComputeFov e) {
        float forca = ConfigCliente.tremor();
        float d = RegrasHorror.distorcao(EfeitosLucidez.nivel) * forca;
        if (d <= 0.0001f) return;
        float t = (float) (tick + e.getPartialTick());
        double onda = Math.sin(t * 0.07) * 0.7 + Math.sin(t * 0.173) * 0.3;
        e.setFOV(e.getFOV() * (1.0 + d * onda));
    }

    /** Mão tremendo: o braço e o item na mão balançam um pouco (só a sua visão em primeira pessoa). */
    static void aoDesenharMao(RenderHandEvent e) {
        if (e.getHand() != InteractionHand.MAIN_HAND) return; // a mão secundária herda o mesmo deslocamento
        float forca = ConfigCliente.tremor();
        float amp = RegrasHorror.tremorMao(EfeitosLucidez.nivel) * forca;
        if (amp <= 0.00001f) return;
        float t = (float) (tick + e.getPartialTick());
        e.getPoseStack().translate(Math.sin(t * 1.9) * amp + Math.sin(t * 5.3) * amp * 0.3,
                Math.cos(t * 2.3) * amp * 0.8, 0);
    }
}
