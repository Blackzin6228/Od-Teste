package com.nikolas.odteste.historia.gatilhos;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Opções do servidor para os gatilhos do Ato 1 (arquivo {@code serverconfig/odteste-gatilhos-server.toml} do mundo).
 * O admin muda com o servidor ligado: o NeoForge relê o arquivo sozinho.
 */
public final class ConfigGatilhos {
    /** Desligado por padrão: "neblina = chuva" ainda espera o ok do Nikolas. */
    public static final boolean PADRAO_AUTOMATICO = false;
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue AUTOMATICO;
    public static final ModConfigSpec.BooleanValue PRECISA_CHUVA;
    public static final ModConfigSpec.BooleanValue AVISO_LIGADO;
    public static final ModConfigSpec.IntValue NOITES_AVISO;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push(IdsGatilhos.SECAO_CONFIG);
        AUTOMATICO = b.comment("O Primeiro Sinal solta sozinho na primeira noite de neblina para grupo criado depois",
                        "desta versão e que ainda não o teve. false (padrão) = só o admin solta (/odteste evento primeiro_sinal).",
                        "Desligado até o Nikolas aprovar \"noite de neblina = noite com chuva\".")
                .define("primeiro_sinal_automatico", PADRAO_AUTOMATICO);
        PRECISA_CHUVA = b.comment("Noite de neblina = noite com chuva no overworld. false = qualquer noite vale.")
                .define("neblina_precisa_chuva", true);
        AVISO_LIGADO = b.comment("Avisar os admins quando um grupo não segue o sino até a Vila Calada.")
                .define("aviso_sino_ligado", true);
        NOITES_AVISO = b.comment("Noites (com alguém do grupo online) depois do Primeiro Sinal até o aviso.")
                .defineInRange("noites_aviso_sino", RegrasGatilhos.NOITES_AVISO_PADRAO,
                        RegrasGatilhos.NOITES_AVISO_MIN, RegrasGatilhos.NOITES_AVISO_MAX);
        b.pop();
        SPEC = b.build();
    }

    private ConfigGatilhos() {}

    /** Chame no construtor do mod (ali o container do mod está ativo). */
    public static void registrar() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, SPEC, IdsGatilhos.ARQUIVO_CONFIG);
    }

    // Seguros de chamar antes da config carregar: devolvem o padrão.

    public static boolean automatico() {
        return ler(AUTOMATICO, PADRAO_AUTOMATICO);
    }

    public static boolean precisaChuva() {
        return ler(PRECISA_CHUVA, true);
    }

    public static boolean avisoLigado() {
        return ler(AVISO_LIGADO, true);
    }

    public static int noitesAviso() {
        try {
            return NOITES_AVISO.get();
        } catch (IllegalStateException carregando) {
            return RegrasGatilhos.NOITES_AVISO_PADRAO;
        }
    }

    private static boolean ler(ModConfigSpec.BooleanValue v, boolean padrao) {
        try {
            return v.get();
        } catch (IllegalStateException carregando) {
            return padrao;
        }
    }
}
