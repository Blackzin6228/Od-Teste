package com.nikolas.odteste.compat.enhancedcelestials;

import com.nikolas.odteste.OdMod;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Pergunta ao Enhanced Celestials se esta noite é de Lua de Sangue. **Só cliente**, chamado de cliente/ atrás de
 * {@code Compat.Integracao.ENHANCED_CELESTIALS.ligada()}. Tudo por reflexão (o jar dele não entra na compilação):
 * {@code EnhancedCelestials.lunarForecastWorldData(level)} → {@code LunarForecast.currentLunarEventHolder()} e
 * {@code getBlend()}. Vale qualquer evento lunar cujo id tenha "blood" (o pack usa
 * {@code enhancedcelestials2defaultlunarevents:blood_moon}). Se a API mudar ou falhar, responde 0 e avisa uma vez.
 */
public final class LuaDeSangue {
    private static final String CLASSE = "dev.corgitaco.enhancedcelestials2core.EnhancedCelestials";

    private static boolean procurou;
    private static boolean avisou;
    private static Method previsaoDe;
    private static Method eventoAgora;
    private static Method mistura;

    private LuaDeSangue() {}

    /** 0..1: quanto a Lua de Sangue está valendo agora neste mundo (sobe e desce junto com a do mod). */
    public static float forca(Level nivel) {
        if (nivel == null) return 0f;
        if (!procurou) procurar();
        if (previsaoDe == null) return 0f;
        try {
            Object opcional = previsaoDe.invoke(null, nivel);
            if (!(opcional instanceof Optional<?> o) || o.isEmpty()) return 0f;
            Object previsao = o.get();
            if (eventoAgora == null) {
                eventoAgora = previsao.getClass().getMethod("currentLunarEventHolder");
                mistura = previsao.getClass().getMethod("getBlend");
            }
            Object h = eventoAgora.invoke(previsao);
            if (!(h instanceof Holder<?> holder) || !ehSangue(holder)) return 0f;
            Object b = mistura.invoke(previsao);
            float f = b instanceof Float x ? x : 1f;
            if (Float.isNaN(f)) return 0f;
            return Math.max(0f, Math.min(1f, f));
        } catch (Throwable falhou) {
            previsaoDe = null;
            if (!avisou) {
                avisou = true;
                OdMod.LOGGER.warn("{}: não deu para perguntar ao Enhanced Celestials pela Lua de Sangue; o olho na lua"
                        + " fica só na Vila Calada.", OdMod.MODID, falhou);
            }
            return 0f;
        }
    }

    private static boolean ehSangue(Holder<?> h) {
        Optional<? extends ResourceKey<?>> chave = h.unwrapKey();
        return chave.isPresent() && chave.get().location().getPath().contains("blood");
    }

    private static void procurar() {
        procurou = true;
        try {
            Class<?> tipo = Class.forName(CLASSE, false, LuaDeSangue.class.getClassLoader());
            previsaoDe = tipo.getMethod("lunarForecastWorldData", Level.class);
            OdMod.LOGGER.info("{}: Enhanced Celestials achado; Lua de Sangue mostra o olho na lua.", OdMod.MODID);
        } catch (Throwable falhou) {
            previsaoDe = null;
            OdMod.LOGGER.warn("{}: Enhanced Celestials está no pack, mas a API dele não respondeu; olho na lua só na"
                    + " Vila Calada.", OdMod.MODID);
        }
    }
}
