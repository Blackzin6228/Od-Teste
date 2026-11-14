package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.horror.IdsHorror;
import com.nikolas.odteste.horror.RegrasCamadaHorror;
import com.nikolas.odteste.horror.RegrasCamadaHorror.Momento;
import com.nikolas.odteste.rede.AreaHorror;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

/**
 * Camada de horror, lado do cliente (decisão de 14/11/2026). Vale à noite (overworld, perto do céu) e nas áreas da
 * história (o servidor diz qual: {@link AreaHorror}); de dia, numa área, fica leve.
 * <ul>
 * <li><b>Noite escura, brilho travado:</b> pelo mapa de luz ({@link LuzDaNoite}): onde não há luz de bloco, a luz cai
 * e o brilho (gamma) do jogador é desfeito. Tocha, lanterna e vela continuam clareando. A opção de brilho do
 * jogador nunca é mexida (nada fica salvo errado no options.txt). Se outro mod trocou o céu do overworld, entra um
 * véu escuro na tela no lugar (menos perto de luz).</li>
 * <li><b>Névoa:</b> de noite chega mais perto e escurece de cor.</li>
 * <li><b>Música:</b> a do jogo e a dos mods param e não tocam enquanto a camada estiver ligada. Volta depois.
 * No lugar: silêncio e, de vez em quando, um som vanilla grave e longe (fora das áreas, que têm os sons delas).</li>
 * </ul>
 * <b>Gancho para o shader</b> (mesa de shaders de Sangue): {@link #forca()}, {@link #forcaNoite()}, {@link #area()}
 * dizem quanto a camada está ligada; {@link #shaderCuidaDoVeu} = true faz o véu de reserva sair (o shader escurece).
 *
 * Sem lag: por tick, só contas simples; o mapa de luz recalcula 256 cores quando o jogo pede (uma vez por tick).
 */
final class CamadaHorrorCliente {
    private static String area = "";
    private static Momento momento = Momento.NADA;
    /** 0 a 1, suave (~5 s): a camada inteira. */
    private static float forca;
    /** 0 a 1, suave: só a parte da noite (névoa, escuro forte). */
    private static float forcaNoite;
    /** 0 a 1: "a luz falha" (susto LUZ): até a luz de bloco escurece. Quem manda é o SustoCliente. */
    static float apagar;
    /** Brilho do jogador lido uma vez por tick (o mapa de luz chama 256 vezes). */
    private static float gamma;
    private static boolean cortandoMusica;
    private static boolean luzNossa;
    private static boolean avisouLuz;
    private static long tick;
    private static long proximoSom = -1;
    /** A mesa de shaders liga isto quando o shader dela já escurece a tela. */
    static volatile boolean shaderCuidaDoVeu;

    private CamadaHorrorCliente() {}

    static void iniciar(IEventBus barramento) {
        barramento.addListener((RegisterDimensionSpecialEffectsEvent e) ->
                e.register(BuiltinDimensionTypes.OVERWORLD_EFFECTS, new LuzDaNoite()));
        barramento.addListener((RegisterGuiLayersEvent e) -> e.registerBelowAll(
                ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsHorror.CAMADA_ESCURO), CamadaHorrorCliente::desenhar));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> aoTick());
        // Por último e mesmo se outro mod já escolheu: a música fica vazia.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, CamadaHorrorCliente::aoEscolherMusica);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CamadaHorrorCliente::aoTocarSom);
        NeoForge.EVENT_BUS.addListener(CamadaHorrorCliente::aoCorDaNevoa);
        // Depois da névoa da vila (que cancela o evento): só encurta mais, nunca alarga.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, CamadaHorrorCliente::aoNevoa);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> limpar());
        AreaHorror.definirReceptor(m -> area = m.area() == null ? "" : m.area());
        SustoCliente.iniciar();
        FalhaMinimapa.iniciar();
    }

    // ---------- gancho (shader e outras partes do cliente) ----------

    static float forca() {
        return forca;
    }

    static float forcaNoite() {
        return forcaNoite;
    }

    static String area() {
        return area;
    }

    static boolean ligada() {
        return RegrasCamadaHorror.ligada(momento);
    }

    static void limpar() {
        area = "";
        momento = Momento.NADA;
        forca = forcaNoite = apagar = 0f;
        cortandoMusica = false;
        proximoSom = -1;
        SustoCliente.limpar();
        FalhaMinimapa.limpar();
    }

    // ---------- tick ----------

    private static void aoTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ClientLevel nivel = mc.level;
        if (p == null || nivel == null) {
            if (forca > 0 || !area.isEmpty()) limpar();
            return;
        }
        if (mc.isPaused()) return;
        tick++;
        BlockPos olhos = BlockPos.containing(p.getEyePosition());
        momento = RegrasCamadaHorror.momento(nivel.dimension() == Level.OVERWORLD, nivel.getDayTime(),
                nivel.getBrightness(LightLayer.SKY, olhos), !area.isEmpty());
        forca = RegrasCamadaHorror.aproximar(forca, RegrasCamadaHorror.alvo(momento), RegrasCamadaHorror.PASSO);
        forcaNoite = RegrasCamadaHorror.aproximar(forcaNoite, momento == Momento.NOITE ? 1f : 0f, RegrasCamadaHorror.PASSO);
        gamma = (float) Math.max(0.0, Math.min(1.0, mc.options.gamma().get()));

        luzNossa = nivel.effects() instanceof LuzDaNoite;
        if (!luzNossa && !avisouLuz && forca > 0) {
            avisouLuz = true;
            OdMod.LOGGER.info("{}: outro mod troca o céu do overworld; a noite escura usa o véu de reserva.", OdMod.MODID);
        }

        boolean cortar = RegrasCamadaHorror.cortaMusica(momento);
        if (cortar && !cortandoMusica) mc.getSoundManager().stop(null, SoundSource.MUSIC); // o que já tocava para
        cortandoMusica = cortar;

        if (tick % 20 == 0) segundo(p);
    }

    /** Uma vez por segundo: de noite, fora das áreas, um som grave e longe de vez em quando. */
    private static void segundo(LocalPlayer p) {
        if (momento != Momento.NOITE || !area.isEmpty() || CenaCliente.tocando() || SustoCliente.tocando()) {
            proximoSom = -1;
            return;
        }
        RandomSource r = p.getRandom();
        if (proximoSom < 0) {
            proximoSom = tick + 20 * (40 + r.nextInt(60));
            return;
        }
        if (tick < proximoSom) return;
        proximoSom = tick + 20 * (60 + r.nextInt(90));
        double ang = r.nextDouble() * Math.PI * 2;
        double d = 14 + r.nextDouble() * 10;
        Vec3 onde = p.position().add(Math.cos(ang) * d, 1 + r.nextDouble() * 2, Math.sin(ang) * d);
        float tom = 0.5f + r.nextFloat() * 0.15f;
        switch (r.nextInt(3)) {
            case 0 -> Sons.em(onde, SoundEvents.AMBIENT_CAVE, 0.25f, tom);
            case 1 -> Sons.em(onde, SoundEvents.GRASS_STEP, 0.3f, tom);
            default -> Sons.em(onde, SoundEvents.WOOD_STEP, 0.3f, tom);
        }
    }

    // ---------- música ----------

    private static void aoEscolherMusica(SelectMusicEvent e) {
        if (!cortandoMusica) return;
        e.setMusic(null);
        e.setCanceled(true);
    }

    /** Música que algum mod toca direto (sem passar pela escolha de música do jogo): também não toca. */
    private static void aoTocarSom(PlaySoundEvent e) {
        if (e.getSound() == null) return;
        if (cortandoMusica && e.getSound().getSource() == SoundSource.MUSIC) {
            e.setSound(null);
            return;
        }
        SustoCliente.filtrar(e);
    }

    // ---------- luz ----------

    /** Chamado pelo mapa de luz (256 vezes por atualização): {@code luzBloco} 0-15, cor [r, g, b] de 0 a 1. */
    static void ajustarLuz(int luzBloco, Vector3f cor) {
        float f = forca;
        float a = apagar;
        if (f <= 0.001f && a <= 0.001f) return;
        float g = gamma;
        cor.set(RegrasCamadaHorror.canal(cor.x(), f, luzBloco, a, g),
                RegrasCamadaHorror.canal(cor.y(), f, luzBloco, a, g),
                RegrasCamadaHorror.canal(cor.z(), f, luzBloco, a, g));
    }

    /** Véu de reserva: só quando a luz não é nossa (ou nenhum shader cuida). Embaixo da HUD (a barra fica visível). */
    private static void desenhar(GuiGraphics g, DeltaTracker delta) {
        if (luzNossa || shaderCuidaDoVeu) return;
        float f = Math.max(forca, apagar);
        if (f <= 0.01f) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        int luz = mc.level.getBrightness(LightLayer.BLOCK, BlockPos.containing(p.getEyePosition()));
        luz = Math.max(luz, Math.max(luzNaMao(p.getMainHandItem()), luzNaMao(p.getOffhandItem())));
        if (apagar > 0.01f) luz = Math.round(luz * (1 - apagar));
        float k = RegrasCamadaHorror.veu(f, luz);
        if (k <= 0.01f) return;
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), ((int) (Math.min(1f, k) * 255) << 24));
    }

    private static int luzNaMao(ItemStack s) {
        return s.getItem() instanceof BlockItem bi ? bi.getBlock().defaultBlockState().getLightEmission() : 0;
    }

    // ---------- névoa ----------

    private static void aoCorDaNevoa(ViewportEvent.ComputeFogColor e) {
        if (forcaNoite <= 0.001f || e.getCamera().getFluidInCamera() != FogType.NONE) return;
        float k = RegrasCamadaHorror.corNevoa(forcaNoite);
        e.setRed(e.getRed() * k);
        e.setGreen(e.getGreen() * k);
        e.setBlue(e.getBlue() * k);
    }

    private static void aoNevoa(ViewportEvent.RenderFog e) {
        if (forcaNoite <= 0.001f || e.getType() != FogType.NONE) return;
        float normal = e.getFarPlaneDistance();
        float longe = RegrasCamadaHorror.nevoa(normal, forcaNoite);
        if (longe >= normal) return;
        e.setFarPlaneDistance(longe);
        e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(), longe * 0.2f));
        e.setCanceled(true); // no NeoForge, a névoa nova só vale com o evento cancelado
    }
}
