package com.nikolas.odteste.criaturas;

import java.util.function.Supplier;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Opções do servidor para o "O Que Segue" e para "a tech chama o paranormal"
 * (arquivo {@code serverconfig/odteste-segue-server.toml} do mundo). O NeoForge relê o arquivo com o servidor ligado.
 */
public final class ConfigSegue {
    public static final String ARQUIVO = "odteste-segue-server.toml";
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue LIGADO;
    public static final ModConfigSpec.IntValue DISTANCIA_MIN;
    public static final ModConfigSpec.IntValue DISTANCIA_MAX;
    public static final ModConfigSpec.IntValue RECARGA_MIN;
    public static final ModConfigSpec.IntValue VOLTA_MIN;
    public static final ModConfigSpec.IntValue CHANCE;
    public static final ModConfigSpec.IntValue DANO;
    public static final ModConfigSpec.IntValue RAIO_SOZINHO;
    public static final ModConfigSpec.BooleanValue MAQUINAS_LIGADO;
    public static final ModConfigSpec.IntValue RAIO_MAQUINAS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("o_que_segue");
        LIGADO = b.comment("Liga a entidade que segue o jogador sozinho (à noite ou com Exposição alta).",
                        "false = só aparece pelo comando /odteste segue chamar.")
                .define("ligado", true);
        DISTANCIA_MIN = b.comment("Distância mínima (blocos) em que ela aparece.")
                .defineInRange("distancia_min", 30, 16, 64);
        DISTANCIA_MAX = b.comment("Distância máxima (blocos) em que ela aparece.")
                .defineInRange("distancia_max", 45, 16, 64);
        RECARGA_MIN = b.comment("Minutos (de jogo) até voltar para o mesmo jogador depois de pegar. Dobra se ele dormiu",
                        "ou estava num lugar claro com outros jogadores.")
                .defineInRange("recarga_minutos", 15, 1, 240);
        VOLTA_MIN = b.comment("Minutos até voltar depois de ser encarada ou atacada.")
                .defineInRange("volta_minutos", 5, 1, 120);
        CHANCE = b.comment("Chance base (em %) a cada tentativa (~10 s) de ela aparecer. Sobe com Exposição e máquinas.")
                .defineInRange("chance_por_cento", 3, 0, 50);
        DANO = b.comment("Dano do golpe (2 = 1 coração). Com a vida cheia, nunca mata.")
                .defineInRange("dano", 14, 0, 40);
        RAIO_SOZINHO = b.comment("Só aparece para quem não tem outro jogador a até esse raio (blocos).")
                .defineInRange("raio_sozinho", 24, 0, 128);
        b.pop();
        b.push("tech_chama");
        MAQUINAS_LIGADO = b.comment("Máquinas com energia perto do jogador sobem a Exposição e atraem o O Que Segue.")
                .define("ligado", true);
        RAIO_MAQUINAS = b.comment("Raio (blocos) da contagem de máquinas. Conta chunks inteiros, com teto.")
                .defineInRange("raio", 24, 8, 48);
        b.pop();
        SPEC = b.build();
    }

    private ConfigSegue() {}

    /** Chame no construtor do mod (ali o container do mod está ativo). */
    public static void registrar() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, SPEC, ARQUIVO);
    }

    // Seguros de chamar antes da config carregar: devolvem o padrão.

    public static boolean ligado() {
        return ler(LIGADO::get, true);
    }

    public static int distanciaMin() {
        return Math.min(ler(DISTANCIA_MIN::get, 30), distanciaMaxCrua());
    }

    public static int distanciaMax() {
        return Math.max(ler(DISTANCIA_MIN::get, 30), distanciaMaxCrua());
    }

    private static int distanciaMaxCrua() {
        return ler(DISTANCIA_MAX::get, 45);
    }

    public static int recargaMin() {
        return ler(RECARGA_MIN::get, 15);
    }

    public static int voltaMin() {
        return ler(VOLTA_MIN::get, 5);
    }

    public static double chance() {
        return ler(CHANCE::get, 3) / 100.0;
    }

    public static float dano() {
        return ler(DANO::get, 14);
    }

    public static int raioSozinho() {
        return ler(RAIO_SOZINHO::get, 24);
    }

    public static boolean maquinasLigado() {
        return ler(MAQUINAS_LIGADO::get, true);
    }

    public static int raioMaquinas() {
        return ler(RAIO_MAQUINAS::get, 24);
    }

    private static <T> T ler(Supplier<T> v, T padrao) {
        try {
            return v.get();
        } catch (IllegalStateException carregando) {
            return padrao;
        }
    }
}
