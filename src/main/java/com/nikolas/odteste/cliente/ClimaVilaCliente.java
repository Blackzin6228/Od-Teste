package com.nikolas.odteste.cliente;

import com.nikolas.odteste.historia.vilacalada.RegrasClimaVila;
import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;
import com.nikolas.odteste.rede.ClimaVila;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Clima da Vila Calada, lado do cliente (lore/fichas-estruturas.md, "Clima"). Só para quem o servidor disse que está
 * dentro do muro ({@link ClimaVila}):
 * <ul>
 * <li><b>Névoa avermelhada leve:</b> a névoa do jogo chega mais perto e puxa para o vermelho do Sangue (#7A0A10),
 * mais forte de noite. Entra e sai em ~3 s.</li>
 * <li><b>Cinza caindo:</b> uma partícula vanilla a cada 4 ticks em volta do jogador, só a céu aberto.</li>
 * <li><b>Sons:</b> vento, madeira rangendo, arrasto ao longe. Vanilla, tom 0,5-0,7, um a cada 25-70 s.</li>
 * <li><b>O Pendurado:</b> só para o jogador de maior Exposição do grupo ({@link Pendurado}), a cada 3-6 min.</li>
 * </ul>
 * {@code efeitos_fortes}: SUAVE = névoa e cinza pela metade, Pendurado some em 1 s. DESLIGADO = sons e névoa
 * (no nível do SUAVE; sem cinza nem Pendurado), igual às outras alucinações que se veem. Nenhum número na tela.
 *
 * Sem lag: por tick, só contas simples e no máximo uma partícula; sons e Pendurado são decididos uma vez por segundo.
 */
final class ClimaVilaCliente {
    /** O que o servidor disse por último. */
    private static boolean dentro;
    private static boolean pendurado;
    /** 0 = fora, 1 = dentro (suave). */
    private static float fator;
    /** 0 = dia, 1 = noite (suave, ~10 s). */
    private static float noite;
    private static long tick;
    /** Depois de uma cena, sons e Pendurado esperam pelo menos isto (ticks). */
    private static final int DEPOIS_DA_CENA = 100;
    private static long proximoSom = -1;
    private static long proximoPendurado = -1;

    private ClimaVilaCliente() {}

    static void receber(ClimaVila m) {
        dentro = m.dentro();
        pendurado = m.dentro() && m.pendurado();
        if (!dentro) proximoSom = -1;
        if (!pendurado) {
            proximoPendurado = -1;
            Pendurado.limpar();
        }
    }

    static void limpar() {
        dentro = pendurado = false;
        fator = noite = 0f;
        proximoSom = proximoPendurado = -1;
        Pendurado.limpar();
    }

    /** Força dos efeitos que se veem: 1 (LIGADO), 0,5 (SUAVE), 0 (DESLIGADO). */
    private static float forca() {
        return ConfigCliente.tremor();
    }

    static void aoTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ClientLevel nivel = mc.level;
        if (p == null || nivel == null) {
            if (dentro || fator > 0) limpar();
            return;
        }
        if (mc.isPaused()) return;
        if (!dentro && fator <= 0f && !Pendurado.presente()) return; // fora da vila: nada a fazer
        tick++;
        fator = RegrasClimaVila.aproximar(fator, dentro ? 1f : 0f, RegrasClimaVila.PASSO_FATOR);
        boolean deNoite = !nivel.dimensionType().hasFixedTime() && RegrasVilaCalada.noite(nivel.getDayTime());
        noite = RegrasClimaVila.aproximar(noite, deNoite ? 1f : 0f, 1f / 200f);

        float f = forca();
        // Trocou para DESLIGADO, ou começou uma cena: some na hora.
        if ((f <= 0f || CenaCliente.tocando()) && Pendurado.presente()) Pendurado.limpar();
        Pendurado.aoTick(p);

        int aCada = f >= 1f ? RegrasClimaVila.CINZA_A_CADA : RegrasClimaVila.CINZA_A_CADA * 2;
        if (f > 0f && fator > 0.3f && tick % aCada == 0) cinza(p, nivel);
        if (tick % 20 == 0 && dentro) segundo(p, f);
    }

    /** Uma vez por segundo, dentro da vila: sons e Pendurado. */
    private static void segundo(LocalPlayer p, float f) {
        if (CenaCliente.tocando()) { // a cena tem os sons dela: o clima espera e volta uns segundos depois
            if (proximoSom >= 0) proximoSom = Math.max(proximoSom, tick + DEPOIS_DA_CENA);
            if (proximoPendurado >= 0) proximoPendurado = Math.max(proximoPendurado, tick + DEPOIS_DA_CENA);
            return;
        }
        RandomSource r = p.getRandom();
        if (proximoSom < 0) {
            proximoSom = tick + RegrasClimaVila.primeiroSom(r.nextDouble());
        } else if (tick >= proximoSom) {
            proximoSom = tick + RegrasClimaVila.proximoSom(r.nextDouble());
            som(p, r);
        }
        if (!pendurado || f <= 0f) return;
        if (proximoPendurado < 0) {
            proximoPendurado = tick + RegrasClimaVila.primeiroPendurado(r.nextDouble());
            return;
        }
        if (tick < proximoPendurado || Pendurado.presente()) return;
        if (Alucinacoes.ocupado()) { // uma figura por vez
            proximoPendurado = tick + RegrasClimaVila.PENDURADO_TENTAR_DE_NOVO;
            return;
        }
        boolean apareceu = Pendurado.aparecer(p, ConfigCliente.suave());
        proximoPendurado = tick + (apareceu ? RegrasClimaVila.proximoPendurado(r.nextDouble())
                : RegrasClimaVila.PENDURADO_TENTAR_DE_NOVO);
    }

    /** Uma partícula de cinza caindo em volta, só a céu aberto (não chove cinza dentro de casa com teto). */
    private static void cinza(LocalPlayer p, ClientLevel nivel) {
        RandomSource r = p.getRandom();
        double raio = RegrasClimaVila.CINZA_RAIO;
        double x = p.getX() + (r.nextDouble() * 2 - 1) * raio;
        double z = p.getZ() + (r.nextDouble() * 2 - 1) * raio;
        double y = p.getY() + 2 + r.nextDouble() * RegrasClimaVila.CINZA_ALTURA;
        if (!nivel.canSeeSky(BlockPos.containing(x, y, z))) return;
        nivel.addParticle(ParticleTypes.ASH, x, y, z, 0, -0.02, 0);
    }

    /** Vento (de todo lado), madeira rangendo ou arrasto, longe, dentro da vila. Sempre vanilla, grave. */
    private static void som(LocalPlayer p, RandomSource r) {
        float tom = RegrasClimaVila.tom(r.nextDouble());
        switch (r.nextInt(3)) {
            case 0 -> Minecraft.getInstance().getSoundManager().play(
                    new SomVento(SoundEvents.ELYTRA_FLYING, 0.12f, tom, 80 + r.nextInt(60), r));
            case 1 -> Sons.em(longe(p, r), r.nextBoolean() ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOOD_BREAK,
                    0.3f, tom);
            default -> Sons.em(longe(p, r), r.nextBoolean() ? SoundEvents.GRINDSTONE_USE : SoundEvents.CHAIN_STEP,
                    0.25f, tom);
        }
    }

    private static Vec3 longe(LocalPlayer p, RandomSource r) {
        double ang = r.nextDouble() * Math.PI * 2;
        double d = RegrasClimaVila.SOM_PERTO + r.nextDouble() * (RegrasClimaVila.SOM_LONGE - RegrasClimaVila.SOM_PERTO);
        return p.position().add(Math.cos(ang) * d, 1 + r.nextDouble() * 3, Math.sin(ang) * d);
    }

    // ---------- para os efeitos de shader (FxSangue) ----------

    /** 0 = fora da vila, 1 = dentro (suave, ~3 s). */
    static float fatorVila() {
        return fator;
    }

    /** 0 = dia, 1 = noite (suave, ~10 s). */
    static float noiteVila() {
        return noite;
    }

    // ---------- névoa ----------

    /** Fator que vale para a tela: dentro × força do efeitos_fortes. */
    private static float fatorVisivel() {
        return fator * forca();
    }

    /** A névoa não dá tontura: fica sempre, no mínimo como no SUAVE (decisão de 13/11/2026). */
    private static float fatorNevoa() {
        return fator * Math.max(forca(), 0.5f);
    }

    /** Cor da névoa puxada para o vermelho do Sangue. Só no ar (debaixo d'água ou na lava, o jogo manda). */
    static void aoCorDaNevoa(ViewportEvent.ComputeFogColor e) {
        float f = fatorNevoa();
        if (f <= 0.001f || e.getCamera().getFluidInCamera() != FogType.NONE) return;
        float m = RegrasClimaVila.mistura(f, noite);
        e.setRed(RegrasClimaVila.misturarCanal(e.getRed(), 16, m));
        e.setGreen(RegrasClimaVila.misturarCanal(e.getGreen(), 8, m));
        e.setBlue(RegrasClimaVila.misturarCanal(e.getBlue(), 0, m));
    }

    /** A névoa chega mais perto. Nunca mais longe do que o jogo já ia pôr (cegueira, escuridão continuam valendo). */
    static void aoNevoa(ViewportEvent.RenderFog e) {
        float f = fatorNevoa();
        if (f <= 0.001f || e.getType() != FogType.NONE) return;
        float normal = e.getFarPlaneDistance();
        float longe = RegrasClimaVila.alcance(normal, f, noite);
        if (longe >= normal) return;
        e.setFarPlaneDistance(longe);
        e.setNearPlaneDistance(Math.min(e.getNearPlaneDistance(), longe * RegrasClimaVila.INICIO));
        e.setCanceled(true); // no NeoForge, a névoa nova só vale com o evento cancelado
    }
}
