package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.cenas.Cena;
import com.nikolas.odteste.cenas.CenasConhecidas;
import com.nikolas.odteste.cenas.IdsCena;
import com.nikolas.odteste.cenas.RegrasCena;
import com.nikolas.odteste.rede.TocarCena;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Toca uma cena de câmera no cliente (sistema genérico: a chegada à Vila Calada hoje, o Diabo depois).
 *
 * <ul>
 * <li>O jogador <b>não sai do lugar</b>: só a câmera do jogo é posta em outro ponto, a cada quadro, depois do jogo
 * calcular a dele. Movimento, pulo, ataque e uso ficam travados; o olhar volta ao de antes no fim.</li>
 * <li>Mão, mira, chat e toda a HUD somem; fica só a camada da cena (preto, legenda, aviso de pular).</li>
 * <li>Sons vanilla sem origem, cada um uma vez; param juntos quando a cena manda (ou no fim).</li>
 * <li>Inventário, chat e outras telas do próprio jogador não abrem (o menu de pausa abre).</li>
 * <li>"Efeitos fortes de tela": LIGADO = cena inteira; SUAVE = sem balanço de câmera; DESLIGADO = a câmera não anda,
 * tela preta só com o texto e os sons ({@link RegrasCena.Modo}).</li>
 * <li>Segurar pulo por 1 s corta para o fim. Morrer, trocar de mundo/dimensão, sair do servidor ou tomar dano
 * devolvem tudo na hora (dano: escurece rápido e volta).</li>
 * </ul>
 * Sem lag: nada roda sem cena tocando (cada evento sai na primeira linha).
 */
public final class CenaCliente {
    private static Cena cena;
    private static double ax, ay, az;
    private static float giro;
    /** Ticks desde o começo da câmera (começa negativo: a visão do jogador escurecendo). */
    private static int tick;
    private static double pulouEm = RegrasCena.SEM_PULO;
    private static int segurando;
    private static int proximoSom;
    private static final List<SoundInstance> TOCANDO = new ArrayList<>();
    private static boolean sonsCortados;
    private static RegrasCena.Modo modo = RegrasCena.Modo.COMPLETO;

    private static LocalPlayer jogador;
    private static ClientLevel nivel;
    private static boolean cameraLigada;
    private static CameraType tipoAntes;
    private static float giroAntes, inclinacaoAntes;
    private static RegrasCena.Quadro quadro = new RegrasCena.Quadro(false, 0f, true);
    private static final RandomSource SORTE = RandomSource.create();

    private CenaCliente() {}

    static void iniciar(IEventBus barramento) {
        barramento.addListener((RegisterGuiLayersEvent e) -> e.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsCena.CAMADA), CenaCliente::desenhar));
        TocarCena.definirReceptor(CenaCliente::receber);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> aoTick());
        // Por último: a câmera da cena manda sobre tremor e balanço dos outros efeitos.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CenaCliente::aoCalcularCamera);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CenaCliente::aoCalcularFov);
        NeoForge.EVENT_BUS.addListener(CenaCliente::aoDesenharCamada);
        NeoForge.EVENT_BUS.addListener((RenderHandEvent e) -> {
            if (naTela()) e.setCanceled(true);
        });
        NeoForge.EVENT_BUS.addListener((RenderHighlightEvent.Block e) -> {
            if (naTela()) e.setCanceled(true);
        });
        NeoForge.EVENT_BUS.addListener(CenaCliente::aoAbrirTela);
        NeoForge.EVENT_BUS.addListener(CenaCliente::aoMover);
        NeoForge.EVENT_BUS.addListener((InputEvent.InteractionKeyMappingTriggered e) -> {
            if (travado()) {
                e.setCanceled(true);
                e.setSwingHand(false);
            }
        });
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> encerrar());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.Clone e) -> encerrar());
    }

    /** Tem cena tocando (de qualquer fase)? */
    public static boolean tocando() {
        return cena != null;
    }

    private static boolean travado() {
        return cena != null && (quadro.camera() || cameraLigada || tick < 0);
    }

    /** A cena manda na tela agora (câmera da cena, ou o preto do modo só texto): mão, mira e HUD somem. */
    private static boolean naTela() {
        return cena != null && (cameraLigada || quadro.camera());
    }

    /** Tela que a cena deixa abrir: menu de pausa, e telas que o servidor abriu (baú etc.: fechar daria erro). */
    private static boolean telaPermitida(Screen tela) {
        return tela == null || tela instanceof PauseScreen
                || (tela instanceof AbstractContainerScreen<?> cont && cont.getMenu().containerId != 0);
    }

    private static void aoAbrirTela(ScreenEvent.Opening e) {
        if (!travado()) return;
        if (!telaPermitida(e.getNewScreen())) e.setCanceled(true);
    }

    // ------------------------------------------------------------------ começo e fim

    private static void receber(TocarCena m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        Cena c = CenasConhecidas.de(m.cena()).orElse(null);
        if (c == null) {
            OdMod.LOGGER.warn("Cena desconhecida no cliente: {}", m.cena());
            return;
        }
        if (cena != null) encerrar(); // uma por vez: a nova começa do zero
        cena = c;
        ax = m.x();
        ay = m.y();
        az = m.z();
        giro = m.giro();
        tick = -RegrasCena.ENTRADA;
        pulouEm = RegrasCena.SEM_PULO;
        segurando = 0;
        proximoSom = 0;
        sonsCortados = false;
        jogador = mc.player;
        nivel = mc.level;
        modo = RegrasCena.Modo.de(ConfigCliente.tremor());
        // Fecha a tela do próprio jogador (inventário, chat); baú aberto fecha do jeito certo, avisando o servidor.
        if (mc.screen instanceof AbstractContainerScreen<?>) {
            jogador.closeContainer();
        } else if (!telaPermitida(mc.screen)) {
            mc.setScreen(null);
        }
        giroAntes = jogador.getYRot();
        inclinacaoAntes = jogador.getXRot();
        quadro = RegrasCena.quadro(cena, tick, pulouEm, modo);
    }

    /** Devolve tudo como estava. Pode ser chamado a qualquer hora (sem cena, não faz nada). */
    static void encerrar() {
        if (cena == null) return;
        desligarCamera();
        pararSons();
        cena = null;
        jogador = null;
        nivel = null;
        quadro = new RegrasCena.Quadro(false, 0f, true);
    }

    private static void ligarCamera() {
        Minecraft mc = Minecraft.getInstance();
        tipoAntes = mc.options.getCameraType();
        // A câmera da cena é sempre "em primeira pessoa": sem o boneco do jogador na tela.
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        cameraLigada = true;
    }

    private static void desligarCamera() {
        if (!cameraLigada) return;
        cameraLigada = false;
        Minecraft mc = Minecraft.getInstance();
        if (tipoAntes != null) mc.options.setCameraType(tipoAntes);
        tipoAntes = null;
        if (jogador != null && mc.player == jogador) segurarOlhar(jogador);
    }

    private static void segurarOlhar(LocalPlayer p) {
        p.setYRot(giroAntes);
        p.setXRot(inclinacaoAntes);
        p.yRotO = giroAntes;
        p.xRotO = inclinacaoAntes;
        p.setYHeadRot(giroAntes);
        p.yHeadRotO = giroAntes;
    }

    // ------------------------------------------------------------------ tick

    private static void aoTick() {
        if (cena == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player != jogador || mc.level != nivel || jogador.isRemoved()
                || jogador.isDeadOrDying()) {
            encerrar();
            return;
        }
        if (mc.isPaused()) return;
        tick++;

        boolean emCena = quadro.camera() || tick < 0;
        if (emCena && Double.isNaN(pulouEm)) {
            if (jogador.hurtTime > 0) {
                pulouEm = tick; // tomou dano: devolve o controle já
            } else if (mc.screen == null && mc.options.keyJump.isDown()) {
                if (++segurando >= RegrasCena.SEGURAR_PARA_PULAR) pulouEm = tick;
            } else {
                segurando = 0;
            }
        }

        if (!sonsCortados && cena.cortaSons() >= 0 && tick >= cena.cortaSons()) {
            pararSons();
            sonsCortados = true;
        }
        if (Double.isNaN(pulouEm)) tocarSons(mc);

        quadro = RegrasCena.quadro(cena, tick, pulouEm, modo);
        if (quadro.acabou()) {
            encerrar();
            return;
        }
        if (quadro.camera() && !cameraLigada && modo.moveCamera()) ligarCamera();
        if (!quadro.camera() && tick >= 0 && !TOCANDO.isEmpty()) pararSons(); // a cena saiu da tela: sons param
        if (!quadro.camera() && cameraLigada) desligarCamera();
        if (naTela()) segurarOlhar(jogador); // o corpo fica parado, olhando para onde olhava
    }

    private static void tocarSons(Minecraft mc) {
        List<Cena.Som> sons = cena.sons();
        while (proximoSom < sons.size() && sons.get(proximoSom).tick() <= tick) {
            Cena.Som s = sons.get(proximoSom++);
            if (tick - s.tick() > 2) continue; // pausou ou atrasou: não toca vários juntos
            ResourceLocation id = ResourceLocation.tryParse(s.id());
            if (id == null || mc.getSoundManager().getSoundEvent(id) == null) {
                OdMod.LOGGER.warn("Cena {}: som {} não existe, pulado", cena.id(), s.id());
                continue;
            }
            // Sem origem: toca "na cabeça", igual para qualquer lugar da câmera.
            SoundInstance si = new SimpleSoundInstance(id, SoundSource.AMBIENT, s.volume(), s.tom(), SORTE, false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true);
            mc.getSoundManager().play(si);
            TOCANDO.add(si);
        }
    }

    private static void pararSons() {
        Minecraft mc = Minecraft.getInstance();
        for (SoundInstance si : TOCANDO) mc.getSoundManager().stop(si);
        TOCANDO.clear();
    }

    // ------------------------------------------------------------------ câmera

    /** Depois do jogo montar a câmera dele: põe no ponto da cena e vira para o alvo. */
    private static void aoCalcularCamera(ViewportEvent.ComputeCameraAngles e) {
        if (!cameraLigada || cena == null) return;
        double t = tick + (Minecraft.getInstance().isPaused() ? 0 : e.getPartialTick());
        RegrasCena.Pose pose = RegrasCena.pose(cena, t, modo.balanca());
        double[] cam = RegrasCena.paraMundo(ax, ay, az, giro, pose.camera());
        double[] alvo = RegrasCena.paraMundo(ax, ay, az, giro, pose.alvo());
        float[] olhar = RegrasCena.olhar(alvo[0] - cam[0], alvo[1] - cam[1], alvo[2] - cam[2]);
        e.getCamera().setPosition(new Vec3(cam[0], cam[1], cam[2]));
        e.setYaw(olhar[0]);
        e.setPitch(olhar[1]);
        e.setRoll(0f);
    }

    private static void aoCalcularFov(ViewportEvent.ComputeFov e) {
        if (!cameraLigada || cena == null || !e.usedConfiguredFov()) return;
        double t = tick + (Minecraft.getInstance().isPaused() ? 0 : e.getPartialTick());
        float zoom = RegrasCena.pose(cena, t, modo.balanca()).zoom();
        // Sempre do campo de visão escolhido pelo jogador (sem o efeito de correr ou de poção).
        double base = Minecraft.getInstance().options.fov().get();
        e.setFOV(Math.max(10, base - zoom));
    }

    private static void aoMover(MovementInputUpdateEvent e) {
        if (!travado()) return;
        Input in = e.getInput();
        in.up = false;
        in.down = false;
        in.left = false;
        in.right = false;
        in.jumping = false;
        in.shiftKeyDown = false;
        in.forwardImpulse = 0;
        in.leftImpulse = 0;
        if (jogador != null) jogador.setSprinting(false);
    }

    // ------------------------------------------------------------------ tela

    /** Esconde toda a HUD (a do jogo e a dos outros mods) enquanto a câmera é da cena; só a nossa camada fica. */
    private static void aoDesenharCamada(RenderGuiLayerEvent.Pre e) {
        if (!naTela()) return;
        if (e.getName().getNamespace().equals(OdMod.MODID) && e.getName().getPath().equals(IdsCena.CAMADA)) return;
        e.setCanceled(true);
    }

    private static void desenhar(GuiGraphics g, DeltaTracker delta) {
        if (cena == null) return;
        Minecraft mc = Minecraft.getInstance();
        double t = tick + (mc.isPaused() ? 0 : delta.getGameTimeDeltaPartialTick(false));
        RegrasCena.Quadro q = RegrasCena.quadro(cena, t, pulouEm, modo);
        int w = g.guiWidth();
        int h = g.guiHeight();
        int preto = Math.round(Math.max(0f, Math.min(1f, q.preto())) * 255f);
        if (preto > 0) g.fill(0, 0, w, h, preto << 24);

        Font font = mc.font;
        if (Double.isNaN(pulouEm)) {
            for (Cena.Legenda l : cena.legendas()) {
                float a = RegrasCena.legenda(l, t);
                if (a < 0.02f) continue;
                Component texto = Component.translatable(l.chave());
                float escala = l.linha() == 0 ? 2f : 1.25f;
                int y = h / 2 + (l.linha() == 0 ? -16 : 10);
                g.pose().pushPose();
                g.pose().translate(w / 2f, y, 0);
                g.pose().scale(escala, escala, 1f);
                // Branco desbotado, sem sombra, sem tremer.
                g.drawString(font, texto, -font.width(texto) / 2, 0, cor(0xD8D6CE, a), false);
                g.pose().popPose();
            }
            // Aviso de pular, fraco, no canto de baixo. Sem número nem contagem.
            if (q.camera()) {
                Component aviso = Component.translatable("odteste.cena.pular", mc.options.keyJump.getTranslatedKeyMessage());
                float a = segurando > 0 ? 0.75f : 0.35f;
                g.drawString(font, aviso, w - font.width(aviso) - 8, h - font.lineHeight - 6, cor(0xC8C8C8, a), false);
            }
        }
    }

    private static int cor(int rgb, float alfa) {
        int a = Math.max(4, Math.round(Math.max(0f, Math.min(1f, alfa)) * 255f));
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}
