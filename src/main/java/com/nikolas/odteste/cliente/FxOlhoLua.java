package com.nikolas.odteste.cliente;

import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.compat.enhancedcelestials.LuaDeSangue;
import com.nikolas.odteste.historia.vilacalada.RegrasFxSangue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * Efeito de tela {@code olho_lua}: a lua vira um olho vermelho (Sangue). Vale com Lua de Sangue do Enhanced
 * Celestials (compat, sem dependência) ou na Vila Calada à noite; o {@code /odfx olho_lua} testa em qualquer lugar.
 *
 * <p>A pupila quase sempre vagueia (troca de rumo a cada 5 a 11 s, escorregando devagar). Raramente (no mínimo
 * 45 s entre uma e outra, mais fácil quando o jogador olha para a lua) ela encara quem olha e segue a câmera
 * devagar por 5 a 9 s. Sem lag: por tick, só umas contas; o Enhanced Celestials é perguntado uma vez por segundo.
 * Roda pela base {@link FxTela} (com e sem Iris).
 */
final class FxOlhoLua {
    static final String SHADER = "odfx_olho_lua";

    private static float forca;
    private static float luaDeSangue;
    /** Para onde a pupila aponta (desvio em relação a "encarar"), agora e o alvo. */
    private static final Vector3f DESVIO = new Vector3f();
    private static final Vector3f ALVO = new Vector3f();
    private static long proximoRumo;
    private static long encaraAte = -1;
    private static long podeEncarar;
    private static long piscaInicio = -1000;
    private static long proximaPiscada;
    private static float abertura = 1f;
    private static float encara;

    private FxOlhoLua() {}

    static boolean ativo() {
        return forca > 0.002f;
    }

    static void aoTick(long relogio, ClientLevel nivel, LocalPlayer p) {
        boolean overworld = nivel.dimension() == Level.OVERWORLD;
        if (relogio % 20 == 0) {
            luaDeSangue = overworld && Compat.Integracao.ENHANCED_CELESTIALS.ligada() ? LuaDeSangue.forca(nivel) : 0f;
        }
        float alvo = overworld ? RegrasFxSangue.olhoAlvo(luaDeSangue, ClimaVilaCliente.fatorVila(), ClimaVilaCliente.noiteVila()) : 0f;
        if (FxSangue.testando(RegrasFxSangue.OLHO_LUA)) alvo = 1f;
        forca = forca < alvo ? Math.min(alvo, forca + 1f / 80f) : Math.max(alvo, forca - 1f / 80f);
        if (forca <= 0.002f) {
            encaraAte = -1;
            return;
        }
        RandomSource r = p.getRandom();
        Vector3f lua = luaDir(nivel, 1f);
        boolean encarando = relogio < encaraAte;
        if (!encarando && relogio >= podeEncarar && relogio % 200 == 0) {
            // olhar para a lua deixa mais fácil ele notar
            Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vector3f frente = cam.getLookVector();
            float chance = frente.dot(lua) > 0.85f ? 0.35f : 0.08f;
            if (r.nextFloat() < chance) {
                encaraAte = relogio + RegrasFxSangue.OLHO_ENCARA_MIN
                        + r.nextInt(RegrasFxSangue.OLHO_ENCARA_MAX - RegrasFxSangue.OLHO_ENCARA_MIN);
                podeEncarar = encaraAte + RegrasFxSangue.OLHO_ENCARA_ESPERA;
                Sons.em(p, SoundEvents.WARDEN_HEARTBEAT, 0.18f * forca, 0.42f);
                encarando = true;
            }
        }
        if (encarando) {
            // encara e segue a câmera devagar: um pouco de desvio para onde o jogador está olhando
            Vector3f frente = Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector();
            ALVO.set(frente).sub(lua).mul(0.25f);
        } else if (relogio >= proximoRumo) {
            proximoRumo = relogio + RegrasFxSangue.OLHO_VAGUEIA_MIN
                    + r.nextInt(RegrasFxSangue.OLHO_VAGUEIA_MAX - RegrasFxSangue.OLHO_VAGUEIA_MIN);
            ALVO.set(r.nextFloat() * 2f - 1f, r.nextFloat() * 1.4f - 0.7f, r.nextFloat() * 2f - 1f).mul(0.9f);
        }
        DESVIO.lerp(ALVO, RegrasFxSangue.OLHO_PASSO);
        encara = Mth.approach(encara, encarando ? 1f : 0f, 0.03f);
        // piscada (só a variante 1 usa de verdade; a 2 abre a fenda quando encara)
        if (relogio >= proximaPiscada) {
            piscaInicio = relogio;
            proximaPiscada = relogio + 160 + r.nextInt(240);
        }
        float pisca = RegrasFxSangue.piscada(relogio - piscaInicio);
        abertura = RegrasFxSangue.OLHO_VARIANTE == 2 ? 0.15f + 0.85f * encara : pisca;
    }

    static void uniforms(ShaderInstance sh, FxTela.Quadro q) {
        ClientLevel nivel = Minecraft.getInstance().level;
        Vector3f lua = luaDir(nivel, q.parcial());
        sh.safeGetUniform("Forca").set(forca * RegrasFxSangue.forcaVisual(ConfigCliente.tremor()));
        sh.safeGetUniform("LuaDir").set(lua.x, lua.y, lua.z);
        // o shader só usa a parte do Olhar que fica de lado na lua: "encarar" é desvio zero
        sh.safeGetUniform("Olhar").set(DESVIO.x, DESVIO.y, DESVIO.z);
        sh.safeGetUniform("Abertura").set(abertura);
        sh.safeGetUniform("Variante").set((float) RegrasFxSangue.OLHO_VARIANTE);
    }

    /** Igual ao FxSangue: o sol gira no plano x-y; a lua fica do lado oposto. */
    private static Vector3f luaDir(ClientLevel nivel, float parcial) {
        float a = nivel == null ? 0f : nivel.getSunAngle(parcial);
        return new Vector3f(Mth.sin(a), -Mth.cos(a), 0.05f).normalize();
    }

    static void limpar() {
        forca = luaDeSangue = encara = 0f;
        abertura = 1f;
        DESVIO.zero();
        ALVO.zero();
        encaraAte = -1;
        podeEncarar = proximoRumo = proximaPiscada = 0;
        piscaInicio = -1000;
    }
}
