package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.horror.IdsHorror;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * O minimapa falha quando algo paranormal está perto (decisão de 14/11/2026): chiado por cima, e às vezes some
 * de vez por um instante. Fora disso, normal.
 *
 * Sem tocar em classe de outro mod: as camadas da HUD com o nome do minimapa são escondidas nos instantes em que
 * "some" (se o mod usar camadas), e o canto do minimapa leva um chiado desenhado por cima de tudo. O canto vem do
 * padrão de cada mod (compat/journeymap, compat/xaero) ou da config {@code minimapa_canto}/{@code minimapa_tamanho}
 * (para quem mudou o minimapa de lugar). Sem nenhum minimapa no pack, nada disto liga.
 *
 * "Paranormal perto": criatura da tag {@code odteste:assombra_mapa} a até 24 blocos, alucinação ou Pendurado na tela,
 * ou um susto grande tocando; mais fraco dentro de uma área da história à noite.
 * Sem lag: a procura roda uma vez por segundo; o chiado só é desenhado enquanto falha.
 */
final class FalhaMinimapa {
    private static final TagKey<EntityType<?>> ASSOMBRA = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsHorror.TAG_ASSOMBRA_MAPA));
    private static final double RAIO = 24;

    private record Mapa(List<String> camadas, boolean emCima, boolean direita) {}

    private static final List<Mapa> MAPAS = new ArrayList<>();
    private static float alvo;
    private static float forca;
    private static long tick;

    private FalhaMinimapa() {}

    static void iniciar() {
        if (Compat.Integracao.JOURNEYMAP.ligada()) {
            MAPAS.add(new Mapa(com.nikolas.odteste.compat.journeymap.MinimapaJourneyMap.CAMADAS,
                    com.nikolas.odteste.compat.journeymap.MinimapaJourneyMap.EM_CIMA,
                    com.nikolas.odteste.compat.journeymap.MinimapaJourneyMap.DIREITA));
        }
        if (Compat.Integracao.XAERO_MINIMAPA.ligada() || Compat.Integracao.XAERO_MINIMAPA_JUSTO.ligada()) {
            MAPAS.add(new Mapa(com.nikolas.odteste.compat.xaero.MinimapaXaero.CAMADAS,
                    com.nikolas.odteste.compat.xaero.MinimapaXaero.EM_CIMA,
                    com.nikolas.odteste.compat.xaero.MinimapaXaero.DIREITA));
        }
        if (MAPAS.isEmpty()) return;
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> aoTick());
        NeoForge.EVENT_BUS.addListener(FalhaMinimapa::aoCamada);
        // Por último: o chiado fica por cima do minimapa, seja ele desenhado como for.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, FalhaMinimapa::aoDesenhar);
    }

    static void limpar() {
        alvo = forca = 0f;
    }

    private static void aoTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) {
            limpar();
            return;
        }
        if (mc.isPaused()) return;
        tick++;
        if (tick % 20 == 0) alvo = procurar(mc, p);
        forca += (alvo - forca) * 0.08f;
        if (forca < 0.005f) forca = 0f;
    }

    private static float procurar(Minecraft mc, LocalPlayer p) {
        if (SustoCliente.tocando() || Pendurado.presente() || Alucinacoes.ocupado()) return 1f;
        AABB caixa = p.getBoundingBox().inflate(RAIO);
        if (!mc.level.getEntities(p, caixa, e -> e.getType().is(ASSOMBRA)).isEmpty()) return 1f;
        if (!CamadaHorrorCliente.area().isEmpty() && CamadaHorrorCliente.ehNoite()) return 0.35f;
        return 0f;
    }

    /** Neste instante o minimapa "some" de vez? Pedaços de 4 ticks, sorteados pelo relógio. */
    private static boolean sumido() {
        if (forca < 0.5f) return false;
        long bloco = tick / 4;
        int h = (int) ((bloco * 0x9E3779B97F4A7C15L) >>> 40) & 0xFF;
        return h < 90 * forca;
    }

    private static void aoCamada(RenderGuiLayerEvent.Pre e) {
        if (forca <= 0f || !sumido()) return;
        String ns = e.getName().getNamespace();
        for (Mapa m : MAPAS) {
            if (m.camadas().contains(ns)) {
                e.setCanceled(true);
                return;
            }
        }
    }

    private static void aoDesenhar(RenderGuiEvent.Post e) {
        if (forca <= 0.02f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        GuiGraphics g = e.getGuiGraphics();
        int w = g.guiWidth();
        int h = g.guiHeight();
        int pct = ConfigCliente.minimapaTamanho();
        int lado = pct > 0 ? h * pct / 100 : Math.max(60, Math.min(140, (int) (h * 0.32f)));
        ConfigCliente.Canto canto = ConfigCliente.minimapaCanto();
        if (canto == ConfigCliente.Canto.NENHUM) return;
        if (canto != ConfigCliente.Canto.AUTOMATICO) {
            boolean cima = canto == ConfigCliente.Canto.CIMA_DIREITA || canto == ConfigCliente.Canto.CIMA_ESQUERDA;
            boolean dir = canto == ConfigCliente.Canto.CIMA_DIREITA || canto == ConfigCliente.Canto.BAIXO_DIREITA;
            chiado(g, w, h, lado, cima, dir);
            return;
        }
        boolean[] feito = new boolean[4];
        for (Mapa m : MAPAS) {
            int i = (m.emCima() ? 0 : 2) + (m.direita() ? 1 : 0);
            if (feito[i]) continue;
            feito[i] = true;
            chiado(g, w, h, lado, m.emCima(), m.direita());
        }
    }

    /** Chiado de TV fora do ar num quadrado do canto: cinzas sorteados a cada quadro, em blocos de 7 px (~400 no máximo). */
    private static void chiado(GuiGraphics g, int w, int h, int lado, boolean cima, boolean direita) {
        int margem = 2;
        int x0 = direita ? w - lado - margem : margem;
        int y0 = cima ? margem : h - lado - margem;
        boolean some = sumido();
        float f = Math.min(1f, forca);
        long semente = System.nanoTime() / 50_000_000L; // troca ~20 vezes por segundo
        g.drawManaged(() -> {
            if (some) g.fill(x0, y0, x0 + lado, y0 + lado, 0xF0080808);
            int cel = 7;
            long r = semente * 6364136223846793005L + 1442695040888963407L;
            for (int y = y0; y < y0 + lado; y += cel) {
                for (int x = x0; x < x0 + lado; x += cel) {
                    r = r * 6364136223846793005L + 1442695040888963407L;
                    int v = (int) (r >>> 56) & 0xFF;
                    int alfa = (int) ((some ? 200 : 60 + (v & 0x7F)) * f);
                    int cinza = 30 + (v * 170 >> 8);
                    g.fill(x, y, Math.min(x + cel, x0 + lado), Math.min(y + cel, y0 + lado),
                            (alfa << 24) | (cinza << 16) | (cinza << 8) | cinza);
                }
            }
            // Uma faixa clara que rola, como imagem fora de sintonia.
            int faixa = (int) ((semente * 7) % Math.max(1, lado));
            g.fill(x0, y0 + faixa, x0 + lado, Math.min(y0 + lado, y0 + faixa + 2), ((int) (120 * f) << 24) | 0xC8C8C8);
        });
    }
}
