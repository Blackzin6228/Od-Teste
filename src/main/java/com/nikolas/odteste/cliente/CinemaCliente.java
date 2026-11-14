package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.cenas.IdsCena;
import com.nikolas.odteste.cenas.LeitorRoteiro;
import com.nikolas.odteste.cenas.RegrasCena;
import com.nikolas.odteste.cenas.RegrasRoteiro;
import com.nikolas.odteste.cenas.Roteiro;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.horror.IdsHorror;
import com.nikolas.odteste.rede.FimCena;
import com.nikolas.odteste.rede.PararCena;
import com.nikolas.odteste.rede.TocarCinema;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Toca as cenas em JSON ({@code data/<mod>/cenas/*.json}, docs/cenas.md) no cliente.
 *
 * <p><b>Como a câmera anda (e por que é estável com Sodium/Iris):</b> nada de entidade de câmera nem de mixin. O jogo
 * monta a câmera dele a cada quadro ({@code Camera.setup}); o NeoForge avisa o giro no meio da montagem
 * ({@code ComputeCameraAngles}: aqui entram giro, inclinação e roll) e, logo depois da montagem e antes de desenhar o
 * mundo, pede o campo de visão ({@code ComputeFov}): é aí que a posição é posta. Da posição em diante o jogo (e o
 * Sodium, o Iris, o culling de entidades e a névoa) só leem {@code camera.getPosition()}, então todos veem a câmera da
 * cena. O jogador não sai do lugar (o servidor segura ele) e o terreno em volta já está carregado.
 *
 * <p>Barras de cinema animadas, HUD escondida (fica só a nossa camada, o escuro da noite e o gravador do /odfx),
 * mão e mira escondidas, movimento/ataque/uso travados, telas do jogador não abrem (o menu de pausa abre).
 * "Efeitos fortes" diminui o tremor; no Mínimo a câmera continua andando, sem tremor forte.
 * Pular segurando o pulo por 1 s, só se a cena e o mestre deixarem. Tudo volta no fim, ao morrer, trocar de mundo
 * ou sair; no fim o cliente avisa o servidor ({@code odteste:cena_fim}) para soltar o jogador.
 *
 * <p>Sem lag: sem cena, cada evento sai na primeira linha.
 */
public final class CinemaCliente {
    private static final Set<String> CAMADAS_QUE_FICAM = Set.of(IdsCena.CAMADA_CINEMA, IdsHorror.CAMADA_ESCURO,
            IdsDados.CAMADA_GRAVAR_FX);

    private static Roteiro roteiro;
    private static List<RegrasRoteiro.ChaveMundo> chaves = List.of();
    private static RegrasRoteiro.Olhos olhos;
    private static int tick;
    private static double pulouEm = RegrasCena.SEM_PULO;
    private static int segurando;
    private static int proximoEvento;
    private static boolean podePular;
    private static boolean espectador;
    private static RegrasCena.Modo modo = RegrasCena.Modo.COMPLETO;
    private static final List<SoundInstance> TOCANDO = new ArrayList<>();

    private static LocalPlayer jogador;
    private static ClientLevel nivel;
    private static boolean cameraLigada;
    private static CameraType tipoAntes;
    private static float giroAntes, inclinacaoAntes;
    private static RegrasCena.Quadro quadro = new RegrasCena.Quadro(false, 0f, true);
    /** Pose do quadro que está sendo desenhado (o giro é posto antes da posição, no mesmo quadro). */
    private static RegrasRoteiro.Pose poseQuadro;
    private static final RandomSource SORTE = RandomSource.create();

    private CinemaCliente() {}

    static void iniciar(IEventBus barramento) {
        barramento.addListener((RegisterGuiLayersEvent e) -> e.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsCena.CAMADA_CINEMA), CinemaCliente::desenhar));
        TocarCinema.definirReceptor(CinemaCliente::receber);
        PararCena.definirReceptor(() -> {
            CenaCliente.encerrar();
            encerrar(false);
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> aoTick());
        // Por último: a câmera da cena manda sobre o tremor e o balanço dos outros efeitos.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CinemaCliente::aoGiro);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CinemaCliente::aoFovEPosicao);
        NeoForge.EVENT_BUS.addListener(CinemaCliente::aoDesenharCamada);
        NeoForge.EVENT_BUS.addListener(CinemaCliente::aoDesenharJogador);
        NeoForge.EVENT_BUS.addListener((RenderHandEvent e) -> {
            if (naTela()) e.setCanceled(true);
        });
        NeoForge.EVENT_BUS.addListener((RenderHighlightEvent.Block e) -> {
            if (naTela()) e.setCanceled(true);
        });
        NeoForge.EVENT_BUS.addListener(CinemaCliente::aoAbrirTela);
        NeoForge.EVENT_BUS.addListener(CinemaCliente::aoMover);
        NeoForge.EVENT_BUS.addListener((InputEvent.InteractionKeyMappingTriggered e) -> {
            if (travado()) {
                e.setCanceled(true);
                e.setSwingHand(false);
            }
        });
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> encerrar(false));
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.Clone e) -> encerrar(false));
    }

    /** Tem cena em JSON tocando (qualquer fase)? */
    static boolean tocando() {
        return roteiro != null;
    }

    private static boolean travado() {
        return roteiro != null && (quadro.camera() || cameraLigada || tick < 0);
    }

    private static boolean naTela() {
        return roteiro != null && (cameraLigada || quadro.camera());
    }

    private static boolean telaPermitida(Screen tela) {
        return tela == null || tela instanceof PauseScreen
                || (tela instanceof AbstractContainerScreen<?> cont && cont.getMenu().containerId != 0);
    }

    private static void aoAbrirTela(ScreenEvent.Opening e) {
        if (travado() && !telaPermitida(e.getNewScreen())) e.setCanceled(true);
    }

    // ------------------------------------------------------------------ começo e fim

    private static void receber(TocarCinema m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        Roteiro r;
        try {
            r = LeitorRoteiro.ler(m.cena(), m.json());
        } catch (IllegalArgumentException erro) {
            OdMod.LOGGER.warn("Cena {} não abriu no cliente: {}", m.cena(), erro.getMessage());
            if (!m.espectador()) PacketDistributor.sendToServer(new FimCena(m.cena()));
            return;
        }
        // Uma por vez: a nova começa do zero (sem avisar o fim da velha: o servidor já trocou).
        CenaCliente.encerrar();
        encerrar(false);
        roteiro = r;
        espectador = m.espectador();
        podePular = m.pular();
        olhos = new RegrasRoteiro.Olhos(m.ox(), m.oy(), m.oz(), m.oGiro(), m.oInclinacao());
        chaves = RegrasRoteiro.resolver(r, new RegrasRoteiro.Ancora(m.ax(), m.ay(), m.az(), m.giro()), olhos,
                mc.options.fov().get());
        tick = -RegrasCena.ENTRADA;
        pulouEm = RegrasCena.SEM_PULO;
        segurando = 0;
        proximoEvento = 0;
        jogador = mc.player;
        nivel = mc.level;
        modo = RegrasCena.Modo.de(ConfigCliente.tremor());
        if (mc.screen instanceof AbstractContainerScreen<?>) {
            jogador.closeContainer();
        } else if (!telaPermitida(mc.screen)) {
            mc.setScreen(null);
        }
        giroAntes = jogador.getYRot();
        inclinacaoAntes = jogador.getXRot();
        quadro = RegrasRoteiro.fase(r, tick, pulouEm);
    }

    /** Devolve tudo como estava. {@code avisar}: conta ao servidor que acabou (para soltar o jogador). */
    static void encerrar(boolean avisar) {
        if (roteiro == null) return;
        String id = roteiro.id();
        boolean eraEspectador = espectador;
        desligarCamera();
        pararSons();
        roteiro = null;
        chaves = List.of();
        jogador = null;
        nivel = null;
        poseQuadro = null;
        quadro = new RegrasCena.Quadro(false, 0f, true);
        if (avisar && !eraEspectador && Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new FimCena(id));
        }
    }

    private static void ligarCamera() {
        Minecraft mc = Minecraft.getInstance();
        tipoAntes = mc.options.getCameraType();
        // Com o boneco: terceira pessoa (o jogo desenha o próprio jogador). Sem: primeira pessoa.
        mc.options.setCameraType(roteiro.mostrarJogador() ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON);
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
        if (roteiro == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player != jogador || mc.level != nivel || jogador.isRemoved()
                || jogador.isDeadOrDying()) {
            encerrar(true);
            return;
        }
        if (mc.isPaused()) return;
        tick++;

        boolean emCena = quadro.camera() || tick < 0;
        if (emCena && podePular && Double.isNaN(pulouEm)) {
            if (mc.screen == null && mc.options.keyJump.isDown()) {
                if (++segurando >= RegrasCena.SEGURAR_PARA_PULAR) pulouEm = tick;
            } else {
                segurando = 0;
            }
        }
        if (Double.isNaN(pulouEm)) tocarEventos(mc);

        quadro = RegrasRoteiro.fase(roteiro, tick, pulouEm);
        if (quadro.acabou()) {
            encerrar(true);
            return;
        }
        if (quadro.camera() && !cameraLigada && modo.moveCamera()) ligarCamera();
        if (!quadro.camera() && tick >= 0 && !TOCANDO.isEmpty()) pararSons();
        if (!quadro.camera() && cameraLigada) desligarCamera();
        if (naTela()) segurarOlhar(jogador);
    }

    private static void tocarEventos(Minecraft mc) {
        List<Roteiro.Evento> ev = roteiro.eventos();
        while (proximoEvento < ev.size() && ev.get(proximoEvento).t() <= tick) {
            Roteiro.Evento e = ev.get(proximoEvento++);
            if (tick - e.t() > 2) continue; // pausou ou atrasou: não toca vários juntos
            switch (e) {
                case Roteiro.Som s -> tocarSom(mc, s);
                case Roteiro.Fx fx -> FxSangue.tocarNaCena(fx.efeito(), fx.segundos());
                case Roteiro.PararSons p -> pararSons();
                default -> { } // fade, texto e tremor são lidos a cada quadro
            }
        }
    }

    private static void tocarSom(Minecraft mc, Roteiro.Som s) {
        ResourceLocation id = ResourceLocation.tryParse(s.som());
        if (id == null || mc.getSoundManager().getSoundEvent(id) == null) {
            OdMod.LOGGER.warn("Cena {}: som {} não existe, pulado", roteiro.id(), s.som());
            return;
        }
        SoundInstance si = new SimpleSoundInstance(id, SoundSource.AMBIENT, s.volume(), s.tom(), SORTE, false, 0,
                SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        mc.getSoundManager().play(si);
        TOCANDO.add(si);
    }

    private static void pararSons() {
        Minecraft mc = Minecraft.getInstance();
        for (SoundInstance si : TOCANDO) mc.getSoundManager().stop(si);
        TOCANDO.clear();
    }

    // ------------------------------------------------------------------ câmera

    private static double agora(float parcial) {
        return tick + (Minecraft.getInstance().isPaused() ? 0 : parcial);
    }

    /** No meio da montagem da câmera: giro, inclinação e roll da cena (com o tremor). */
    private static void aoGiro(ViewportEvent.ComputeCameraAngles e) {
        if (!cameraLigada || roteiro == null) return;
        double t = agora((float) e.getPartialTick());
        RegrasRoteiro.Pose p = RegrasRoteiro.pose(chaves, t);
        poseQuadro = p;
        float[] tr = RegrasRoteiro.tremor(roteiro.eventos(), t, ConfigCliente.tremor());
        e.setYaw(p.giro() + tr[0]);
        e.setPitch(p.inclinacao() + tr[1]);
        e.setRoll(p.roll() + tr[2]);
    }

    /**
     * Logo depois da montagem da câmera (o jogo já pôs a posição dele) e antes do mundo ser desenhado: a posição da
     * cena entra aqui, e o campo de visão também. Só no pedido do mundo ({@code usedConfiguredFov}); o da mão não.
     */
    private static void aoFovEPosicao(ViewportEvent.ComputeFov e) {
        if (!cameraLigada || roteiro == null || !e.usedConfiguredFov()) return;
        RegrasRoteiro.Pose p = poseQuadro != null ? poseQuadro : RegrasRoteiro.pose(chaves, agora((float) e.getPartialTick()));
        e.getCamera().setPosition(new Vec3(p.x(), p.y(), p.z()));
        e.setFOV(p.fov());
    }

    /** Câmera colada nos olhos do alvo: o boneco dele some (senão a câmera fica dentro da cabeça). */
    private static void aoDesenharJogador(RenderPlayerEvent.Pre e) {
        if (!cameraLigada || roteiro == null || !roteiro.mostrarJogador() || poseQuadro == null || olhos == null) return;
        if (RegrasRoteiro.distanciaOlhos(poseQuadro, olhos) > RegrasRoteiro.PERTO_DOS_OLHOS) return;
        Vec3 o = e.getEntity().getEyePosition();
        double dx = o.x - olhos.x(), dy = o.y - olhos.y(), dz = o.z - olhos.z();
        if (dx * dx + dy * dy + dz * dz < 0.25) e.setCanceled(true);
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

    private static void aoDesenharCamada(RenderGuiLayerEvent.Pre e) {
        if (!naTela()) return;
        if (e.getName().getNamespace().equals(OdMod.MODID) && CAMADAS_QUE_FICAM.contains(e.getName().getPath())) return;
        e.setCanceled(true);
    }

    private static void desenhar(GuiGraphics g, DeltaTracker delta) {
        if (roteiro == null) return;
        Minecraft mc = Minecraft.getInstance();
        double t = agora(delta.getGameTimeDeltaPartialTick(false));
        RegrasCena.Quadro q = RegrasRoteiro.fase(roteiro, t, pulouEm);
        int w = g.guiWidth();
        int h = g.guiHeight();
        boolean soTexto = !modo.moveCamera() && q.camera();

        if (q.camera()) {
            // Véu da cena (preto ou vermelho) por baixo das barras.
            RegrasRoteiro.Veu v = RegrasRoteiro.veu(roteiro.eventos(), t);
            if (v.alfa() > 0.004f) g.fill(0, 0, w, h, cor(v.cor(), v.alfa()));
            int barra = Math.round(RegrasRoteiro.barras(roteiro, t) * h);
            if (barra > 0) {
                g.fill(0, 0, w, barra, 0xFF000000);
                g.fill(0, h - barra, w, h, 0xFF000000);
            }
        }
        float preto = soTexto ? 1f : Math.max(0f, Math.min(1f, q.preto()));
        if (preto > 0.004f) g.fill(0, 0, w, h, cor(0x000000, preto));

        if (!Double.isNaN(pulouEm) || !q.camera()) return;
        Font font = mc.font;
        for (Roteiro.Evento e : roteiro.eventos()) {
            if (!(e instanceof Roteiro.Texto x)) continue;
            float a = RegrasRoteiro.texto(x, t);
            if (a < 0.02f) continue;
            Component texto = Component.translatable(x.chave());
            float escala = x.linha() == 0 ? 2f : 1.25f;
            int y = h / 2 + (x.linha() == 0 ? -16 : 10);
            g.pose().pushPose();
            g.pose().translate(w / 2f, y, 0);
            g.pose().scale(escala, escala, 1f);
            g.drawString(font, texto, -font.width(texto) / 2, 0, cor(0xD8D6CE, a), false);
            g.pose().popPose();
        }
        if (podePular) {
            Component aviso = Component.translatable("odteste.cena.pular", mc.options.keyJump.getTranslatedKeyMessage());
            float a = segurando > 0 ? 0.75f : 0.35f;
            int barra = Math.round(RegrasRoteiro.barras(roteiro, t) * h);
            int y = barra > font.lineHeight + 4 ? h - barra / 2 - font.lineHeight / 2 : h - font.lineHeight - 6;
            g.drawString(font, aviso, w - font.width(aviso) - 8, y, cor(0xC8C8C8, a), false);
        }
    }

    private static int cor(int rgb, float alfa) {
        int a = Math.max(4, Math.round(Math.max(0f, Math.min(1f, alfa)) * 255f));
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}
