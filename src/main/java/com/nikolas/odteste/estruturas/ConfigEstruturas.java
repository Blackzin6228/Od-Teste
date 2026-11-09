package com.nikolas.odteste.estruturas;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Opções do servidor para estruturas (arquivo {@code serverconfig/odteste-server.toml} do mundo).
 * {@code estruturas.blocos_por_tick}: tamanho de cada fatia de uma estrutura grande. Menor = mais suave e mais lento.
 */
public final class ConfigEstruturas {
    public static final int PADRAO = 8192;
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue BLOCOS_POR_TICK;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("estruturas");
        BLOCOS_POR_TICK = b.comment("Blocos por tick ao colocar ou desfazer estrutura grande (em partes).",
                        "Menor = o servidor sente menos, mas demora mais. Mudar no meio só vale para a próxima.")
                .defineInRange("blocos_por_tick", PADRAO, 256, 65536);
        b.pop();
        SPEC = b.build();
    }

    private ConfigEstruturas() {}

    /** Chame no construtor do mod (ali o container do mod está ativo). */
    public static void registrar() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    /** Seguro de chamar antes da config carregar. */
    public static int blocosPorTick() {
        try {
            return BLOCOS_POR_TICK.get();
        } catch (IllegalStateException carregando) {
            return PADRAO;
        }
    }
}
