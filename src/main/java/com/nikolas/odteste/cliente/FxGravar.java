package com.nikolas.odteste.cliente;

import com.mojang.blaze3d.platform.NativeImage;
import com.nikolas.odteste.OdMod;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * {@code /odfx gravar <efeito>}: salva um quadro por tick em {@code screenshots/odteste/<efeito>_<hora>/0001.png}...
 * para virar vídeo/GIF de prévia. O quadro é tirado na camada de HUD mais alta (a imagem pronta, com shader pack
 * ou sem). Gravar pesa (uma leitura da tela por tick); o arquivo é escrito fora da linha do jogo. Só teste.
 */
final class FxGravar {
    private static Path pasta;
    private static long ultimoTick = -1;
    private static long fim;
    private static long tickAtual;
    private static int quadros;
    private static boolean gravando;

    private FxGravar() {}

    static void comecar(String efeito, long agora, int ticks) {
        Minecraft mc = Minecraft.getInstance();
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        pasta = mc.gameDirectory.toPath().resolve("screenshots").resolve(OdMod.MODID).resolve(efeito + "_" + hora);
        try {
            Files.createDirectories(pasta);
        } catch (Exception falhou) {
            OdMod.LOGGER.warn("{}: não deu para criar {}", OdMod.MODID, pasta, falhou);
            return;
        }
        fim = agora + ticks;
        quadros = 0;
        ultimoTick = -1;
        gravando = true;
    }

    static void aoTick(long relogio) {
        tickAtual = relogio;
        if (gravando && relogio > fim) parar();
    }

    static void parar() {
        if (!gravando) return;
        gravando = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && pasta != null) {
            mc.player.displayClientMessage(Component.translatable("odteste.fx.gravado", quadros,
                    mc.gameDirectory.toPath().relativize(pasta).toString()), false);
        }
    }

    /** Camada de HUD: um quadro por tick enquanto grava. */
    static void desenhar(GuiGraphics g, DeltaTracker delta) {
        if (!gravando || tickAtual == ultimoTick) return;
        ultimoTick = tickAtual;
        Minecraft mc = Minecraft.getInstance();
        try {
            g.flush();
            NativeImage img = Screenshot.takeScreenshot(mc.getMainRenderTarget());
            quadros++;
            Path arquivo = pasta.resolve(String.format("%04d.png", quadros));
            Util.ioPool().execute(() -> {
                try {
                    img.writeToFile(arquivo);
                } catch (Exception falhou) {
                    OdMod.LOGGER.warn("{}: não deu para salvar {}", OdMod.MODID, arquivo, falhou);
                } finally {
                    img.close();
                }
            });
        } catch (Throwable falhou) {
            OdMod.LOGGER.warn("{}: gravação parada", OdMod.MODID, falhou);
            parar();
        }
    }
}
