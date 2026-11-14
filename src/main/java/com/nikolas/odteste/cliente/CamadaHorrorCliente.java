package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.horror.IdsHorror;
import com.nikolas.odteste.horror.RegrasCamadaHorror;
import com.nikolas.odteste.horror.RegrasCamadaHorror.Momento;
import com.nikolas.odteste.rede.AreaHorror;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
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
 * Camada de horror, lado do cliente (decisão de 14/11/2026). Duas partes:
 * <ul>
 * <li><b>Fundo, no overworld inteiro, de dia também</b> (mudança do Nikolas, 14/11): céu fechado e cinza (a cor da
 * névoa puxada para o cinza e o céu do jogo coberto por ela), névoa mais perto que a do jogo, luz do dia um pouco
 * mais baixa pelo mapa de luz (dá para trabalhar e minerar), música do jogo e dos mods cortada e, raramente, um som
 * vanilla grave e longe. Nether, End e outras dimensões ficam fora.</li>
 * <li><b>Parte pesada</b>, à noite (perto do céu) e nas áreas da história (o servidor diz qual: {@link AreaHorror}),
 * por cima do fundo:
 * <ul>
 * <li><b>Noite escura, brilho travado</b> pelo mapa de luz ({@link LuzDaNoite}): onde não há luz de bloco, a luz cai
 * e o brilho (gamma) do jogador é desfeito. Tocha, lanterna e vela continuam clareando. A opção de brilho do
 * jogador nunca é mexida (nada fica salvo errado no options.txt).</li>
 * <li><b>Névoa</b> de noite mais perto e mais escura.</li>
 * </ul></li>
 * </ul>
 * Se outro mod trocou o céu do overworld, ou há shader pack do Iris ligado (a maioria dos packs não usa o mapa de
 * luz), entra um véu na tela no lugar: cinza fraco de dia, escuro de noite (menos perto de luz).
 * A opção {@code intensidade} do cliente (50 a 100%) diminui tudo que se vê; nunca desliga.
 *
 * <b>Gancho para o shader</b> (mesa de shaders, PR #76): {@link #forcaDia()} (o fundo, 0 a 1: a "cor lavada" do
 * mundo inteiro), {@link #forca()} (parte pesada), {@link #forcaNoite()}, {@link #forcaTotal()} e {@link #area()},
 * todos já com a {@code intensidade} do jogador. {@link #shaderCuidaDoVeu} = true tira o véu escuro de reserva (o
 * shader escurece a noite); {@link #shaderCuidaDoDia} = true tira o véu cinza de reserva do dia (o shader lava a cor).
 *
 * Sem lag: por tick, só contas simples; o mapa de luz recalcula 256 cores quando o jogo pede (uma vez por tick).
 */
final class CamadaHorrorCliente {
    private static String area = "";
    private static Momento momento = Momento.NADA;
    /** 0 a 1, suave (~5 s): a parte pesada (noite/área), sem a intensidade. */
    private static float forca;
    /** 0 a 1, suave: só a parte da noite (névoa, escuro forte), sem a intensidade. */
    private static float forcaNoite;
    /** 0 a 1, suave: o fundo do overworld inteiro (céu cinza, névoa, luz do dia), sem a intensidade. */
    private static float forcaDia;
    /** 0,5 a 1: opção {@code intensidade} do jogador, lida uma vez por tick. */
    private static float intensidade = 1f;
    /** 0 a 1: "a luz falha" (susto LUZ): até a luz de bloco escurece. Quem manda é o SustoCliente. */
    static float apagar;
    /** Brilho do jogador lido uma vez por tick (o mapa de luz chama 256 vezes). */
    private static float gamma;
    private static boolean cortandoMusica;
    private static boolean luzNossa;
    private static boolean packLigado;
    private static boolean avisouLuz;
    private static long tick;
    private static long proximoSom = -1;
    /** Iris no pack: com shader pack ligado, a maioria dos packs ignora o mapa de luz (o véu entra no lugar). */
    private static final boolean IRIS = Compat.Integracao.IRIS.ligada();
    /** A mesa de shaders liga isto quando o shader dela já escurece a noite. */
    static volatile boolean shaderCuidaDoVeu;
    /** A mesa de shaders liga isto quando o shader dela já faz a cor lavada do dia. */
    static volatile boolean shaderCuidaDoDia;

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
        // O cinza vem primeiro: a cor da Vila Calada e o escuro da noite entram por cima dele.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, CamadaHorrorCliente::aoCorCinza);
        NeoForge.EVENT_BUS.addListener(CamadaHorrorCliente::aoCorDaNevoa);
        // Depois da névoa da vila (que cancela o evento): só encurta mais, nunca alarga.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, CamadaHorrorCliente::aoNevoa);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> limpar());
        AreaHorror.definirReceptor(m -> area = m.area() == null ? "" : m.area());
        SustoCliente.iniciar();
        FalhaMinimapa.iniciar();
    }

    // ---------- gancho (shader e outras partes do cliente) ----------

    /** Parte pesada (noite e áreas), 0 a 1, já com a intensidade. */
    static float forca() {
        return forca * intensidade;
    }

    /** Só a noite, 0 a 1, já com a intensidade. */
    static float forcaNoite() {
        return forcaNoite * intensidade;
    }

    /** Fundo do overworld inteiro (de dia também), 0 a 1, já com a intensidade. É a "cor lavada" do shader. */
    static float forcaDia() {
        return forcaDia * intensidade;
    }

    /** Tudo junto (0 a 1): o fundo vale até metade, noite e áreas chegam a 1. */
    static float forcaTotal() {
        return RegrasCamadaHorror.total(forcaDia(), forca());
    }

    /** Já é noite de verdade (mais da metade da entrada), sem contar a intensidade. Para regras, não para tela. */
    static boolean ehNoite() {
        return forcaNoite > 0.5f;
    }

    static String area() {
        return area;
    }

    static boolean ligada() {
        return RegrasCamadaHorror.ligada(momento);
    }

    /** O céu do jogo pode ser pulado (dia com o fundo inteiro, sem shader pack): {@link LuzDaNoite#renderSky}. */
    static boolean pularCeu() {
        return !packLigado && RegrasCamadaHorror.pularCeu(forcaDia, forcaNoite);
    }

    static void limpar() {
        area = "";
        momento = Momento.NADA;
        forca = forcaNoite = forcaDia = apagar = 0f;
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
            if (forca > 0 || forcaDia > 0 || !area.isEmpty()) limpar();
            return;
        }
        if (mc.isPaused()) return;
        tick++;
        BlockPos olhos = BlockPos.containing(p.getEyePosition());
        boolean overworld = nivel.dimension() == Level.OVERWORLD;
        momento = RegrasCamadaHorror.momento(overworld, nivel.getDayTime(),
                nivel.getBrightness(LightLayer.SKY, olhos), !area.isEmpty());
        if (!overworld) {
            // Nether, End e outras: a camada some na hora (nada de véu nem névoa durante a troca).
            forca = forcaNoite = forcaDia = 0f;
        } else {
            forca = RegrasCamadaHorror.aproximar(forca, RegrasCamadaHorror.alvo(momento), RegrasCamadaHorror.PASSO);
            forcaNoite = RegrasCamadaHorror.aproximar(forcaNoite, momento == Momento.NOITE ? 1f : 0f, RegrasCamadaHorror.PASSO);
            forcaDia = RegrasCamadaHorror.aproximar(forcaDia, RegrasCamadaHorror.alvoDia(momento), RegrasCamadaHorror.PASSO);
        }
        gamma = (float) Math.max(0.0, Math.min(1.0, mc.options.gamma().get()));
        intensidade = RegrasCamadaHorror.intensidade(ConfigCliente.intensidade());

        luzNossa = nivel.effects() instanceof LuzDaNoite;
        packLigado = IRIS && com.nikolas.odteste.compat.iris.PackIris.packLigado();
        if (overworld && !luzNossa && !avisouLuz && forcaDia > 0) {
            avisouLuz = true;
            OdMod.LOGGER.info("{}: outro mod troca o céu do overworld; a camada de horror usa o véu de reserva.", OdMod.MODID);
        }

        boolean cortar = RegrasCamadaHorror.cortaMusica(momento);
        if (cortar && !cortandoMusica) mc.getSoundManager().stop(null, SoundSource.MUSIC); // o que já tocava para
        cortandoMusica = cortar;

        if (tick % 20 == 0) segundo(p);
    }

    /** Uma vez por segundo: fora das áreas, um som grave e longe de vez em quando (de dia bem mais raro). */
    private static void segundo(LocalPlayer p) {
        RandomSource r = p.getRandom();
        int intervalo = RegrasCamadaHorror.intervaloSom(momento, !area.isEmpty(), r.nextDouble());
        if (intervalo < 0 || CenaCliente.tocando() || SustoCliente.tocando()) {
            proximoSom = -1;
            return;
        }
        if (proximoSom < 0) {
            proximoSom = tick + intervalo / 2 + r.nextInt(Math.max(1, intervalo / 2));
            return;
        }
        if (tick < proximoSom) return;
        proximoSom = tick + intervalo;
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

    /**
     * Chamado pelo mapa de luz (256 vezes por atualização): {@code luzBloco} e {@code luzCeu} 0-15, cor [r, g, b]
     * de 0 a 1. Primeiro o fundo (luz do dia um pouco mais baixa), depois a parte pesada (noite, brilho travado).
     */
    static void ajustarLuz(int luzBloco, int luzCeu, Vector3f cor) {
        float d = forcaDia();
        float f = forca();
        float a = apagar;
        if (d <= 0.001f && f <= 0.001f && a <= 0.001f) return;
        float g = gamma;
        float r = RegrasCamadaHorror.canalDia(cor.x(), d, luzBloco, luzCeu);
        float gr = RegrasCamadaHorror.canalDia(cor.y(), d, luzBloco, luzCeu);
        float b = RegrasCamadaHorror.canalDia(cor.z(), d, luzBloco, luzCeu);
        cor.set(RegrasCamadaHorror.canal(r, f, luzBloco, a, g),
                RegrasCamadaHorror.canal(gr, f, luzBloco, a, g),
                RegrasCamadaHorror.canal(b, f, luzBloco, a, g));
    }

    /**
     * Véu de reserva: quando a luz não é nossa ou há shader pack ligado (a maioria dos packs calcula a luz sozinha e
     * não usa o mapa de luz), e nenhum shader nosso cuida disso. De dia, cinza escuro fraco onde chega o céu; de noite,
     * escuro (menos perto de luz). Embaixo da HUD (a barra fica visível).
     */
    private static void desenhar(GuiGraphics g, DeltaTracker delta) {
        if (luzNossa && !packLigado) return;
        boolean dia = !shaderCuidaDoDia;
        boolean noite = !shaderCuidaDoVeu;
        if (!dia && !noite) return;
        float fd = forcaDia();
        float f = Math.max(forca(), apagar);
        if (fd <= 0.01f && f <= 0.01f) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;
        BlockPos olhos = BlockPos.containing(p.getEyePosition());
        int luz = mc.level.getBrightness(LightLayer.BLOCK, olhos);
        luz = Math.max(luz, Math.max(luzNaMao(p.getMainHandItem()), luzNaMao(p.getOffhandItem())));
        if (apagar > 0.01f) luz = Math.round(luz * (1 - apagar));
        if (dia && fd > 0.01f) {
            float kd = RegrasCamadaHorror.veuDia(fd, mc.level.getBrightness(LightLayer.SKY, olhos), luz);
            if (kd > 0.01f) g.fill(0, 0, g.guiWidth(), g.guiHeight(), ((int) (Math.min(1f, kd) * 255) << 24) | 0x1C1E20);
        }
        if (noite && f > 0.01f) {
            float k = RegrasCamadaHorror.veu(f, luz);
            if (k > 0.01f) g.fill(0, 0, g.guiWidth(), g.guiHeight(), ((int) (Math.min(1f, k) * 255) << 24));
        }
    }

    private static int luzNaMao(ItemStack s) {
        return s.getItem() instanceof BlockItem bi ? bi.getBlock().defaultBlockState().getLightEmission() : 0;
    }

    // ---------- névoa e céu ----------

    /** Céu fechado e cinza: a cor da névoa (que também pinta o céu e o fundo da tela) perde a cor. */
    private static void aoCorCinza(ViewportEvent.ComputeFogColor e) {
        float d = forcaDia();
        if (d <= 0.001f || e.getCamera().getFluidInCamera() != FogType.NONE) return;
        float l = RegrasCamadaHorror.luma(e.getRed(), e.getGreen(), e.getBlue());
        e.setRed(RegrasCamadaHorror.cinza(e.getRed(), l, d));
        e.setGreen(RegrasCamadaHorror.cinza(e.getGreen(), l, d));
        e.setBlue(RegrasCamadaHorror.cinza(e.getBlue(), l, d));
    }

    private static void aoCorDaNevoa(ViewportEvent.ComputeFogColor e) {
        float n = forcaNoite();
        if (n <= 0.001f || e.getCamera().getFluidInCamera() != FogType.NONE) return;
        float k = RegrasCamadaHorror.corNevoa(n);
        e.setRed(e.getRed() * k);
        e.setGreen(e.getGreen() * k);
        e.setBlue(e.getBlue() * k);
    }

    private static void aoNevoa(ViewportEvent.RenderFog e) {
        float d = forcaDia();
        float n = forcaNoite();
        if ((d <= 0.001f && n <= 0.001f) || e.getType() != FogType.NONE) return;
        float normal = e.getFarPlaneDistance();
        float longe;
        float inicio;
        if (e.getMode() == FogRenderer.FogMode.FOG_SKY) {
            // O céu: com o fundo inteiro vira todo cor de névoa (fechado). De noite solta (lua e estrelas como antes).
            longe = RegrasCamadaHorror.nevoaCeu(normal, d, n);
            inicio = 0f;
        } else {
            longe = Math.min(RegrasCamadaHorror.nevoaDia(normal, d), RegrasCamadaHorror.nevoa(normal, n));
            inicio = longe * (n > 0.5f ? 0.2f : 0.35f);
        }
        if (longe >= normal) return;
        e.setFarPlaneDistance(longe);
        e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(), inicio));
        e.setCanceled(true); // no NeoForge, a névoa nova só vale com o evento cancelado
    }
}
