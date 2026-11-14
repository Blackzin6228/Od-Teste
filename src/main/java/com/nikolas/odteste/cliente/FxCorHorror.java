package com.nikolas.odteste.cliente;

import com.nikolas.odteste.historia.vilacalada.RegrasFxSangue;
import com.nikolas.odteste.sistemas.RegrasCorHorror;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.Level;

/**
 * Efeito de tela {@code cor_horror} (decisão de 14/11/2026: horror forte no mundo inteiro, de dia também).
 * No overworld: cor lavada e fria, sombra de contraste baixo, céu puxado para cinza, grão leve e vinheta.
 * Mais forte à noite e nas áreas da história; na Vila Calada, à noite, dá lugar ao carmesim.
 * Só cor: a luz e a música são da camada de horror (PR #77). Roda pela base {@link FxTela} (com e sem Iris).
 *
 * <p>A força vem de uma {@link FonteHorror}; sem a camada de horror, vale a fonte-padrão (pela hora do mundo).
 * Sem lag: por tick, só duas contas.
 */
final class FxCorHorror {
    static final String SHADER = "odfx_cor_horror";

    /** Fonte-padrão: dia = overworld, noite suave pela hora, área = dentro da Vila Calada. */
    private static final FonteHorror PADRAO = new FonteHorror() {
        @Override
        public float dia() {
            return diaPadrao;
        }

        @Override
        public float noite() {
            return noitePadrao;
        }

        @Override
        public float area() {
            return ClimaVilaCliente.fatorVila();
        }
    };

    private static FonteHorror fonte = PADRAO;
    private static float diaPadrao;
    private static float noitePadrao;

    private FxCorHorror() {}

    /** A camada de horror chama isto para mandar ela na força (ver {@link FonteHorror}). */
    static void usarFonte(FonteHorror f) {
        fonte = f == null ? PADRAO : f;
    }

    static void aoTick() {
        ClientLevel nivel = Minecraft.getInstance().level;
        boolean overworld = nivel != null && nivel.dimension() == Level.OVERWORLD;
        diaPadrao = RegrasCorHorror.diaPadrao(overworld);
        float alvo = nivel == null ? 0f : RegrasCorHorror.noiteAlvo(overworld, nivel.getDayTime());
        noitePadrao = overworld ? RegrasCorHorror.aproximar(noitePadrao, alvo, RegrasCorHorror.PASSO) : 0f;
    }

    private static float forcaAgora() {
        float dia = FxSangue.testando(RegrasFxSangue.COR_HORROR) ? 1f : seguro(fonte.dia());
        return RegrasCorHorror.forca(dia, FxSangue.carmesimAgora(), ConfigCliente.tremor());
    }

    static boolean ativo() {
        return forcaAgora() > 0.002f;
    }

    static void uniforms(ShaderInstance sh, FxTela.Quadro q) {
        sh.safeGetUniform("Forca").set(forcaAgora());
        sh.safeGetUniform("Noite").set(seguro(fonte.noite()));
        sh.safeGetUniform("Area").set(seguro(fonte.area()));
    }

    private static float seguro(float v) {
        if (Float.isNaN(v)) return 0f;
        return Math.max(0f, Math.min(1f, v));
    }

    static void limpar() {
        diaPadrao = noitePadrao = 0f;
    }
}
