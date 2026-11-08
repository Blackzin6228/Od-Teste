package com.nikolas.odteste.cliente;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Opções do cliente (arquivo config/odteste-client.toml e tela de configuração do mod).
 * "Efeitos fortes de tela" é acessibilidade (tontura): no "suave" o tremor cai pela metade,
 * no "desligado" fica só vinheta e som (docs/animacoes.md, regra 5).
 */
public final class ConfigCliente {
    public enum Forca {
        LIGADO(1f),
        SUAVE(0.5f),
        DESLIGADO(0f);

        public final float tremor;

        Forca(float tremor) {
            this.tremor = tremor;
        }
    }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<Forca> EFEITOS_FORTES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        EFEITOS_FORTES = b.comment("Efeitos fortes de tela (tremor, inclinação). LIGADO, SUAVE ou DESLIGADO.")
                .translation("odteste.configuration.efeitos_fortes")
                .defineEnum("efeitos_fortes", Forca.LIGADO);
        SPEC = b.build();
    }

    private ConfigCliente() {}

    /** Multiplicador do tremor (0 a 1). Seguro de chamar antes da config carregar. */
    public static float tremor() {
        try {
            return EFEITOS_FORTES.get().tremor;
        } catch (IllegalStateException carregando) {
            return Forca.LIGADO.tremor;
        }
    }
}
