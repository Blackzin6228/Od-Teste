package com.nikolas.odteste.historia.mestre;

import java.util.List;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config do mestre ({@code serverconfig/odteste-mestre-server.toml} do mundo). O NeoForge relê o arquivo com o
 * servidor ligado: dá para mudar a lista sem reiniciar.
 */
public final class ConfigMestre {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> MESTRES;
    public static final ModConfigSpec.BooleanValue OUVIR_PADRAO;
    public static final ModConfigSpec.BooleanValue PULAR_CENAS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push(IdsMestre.SECAO_CONFIG);
        MESTRES = b.comment("Nomes dos mestres, separados por vírgula (ex.: \"Blackzin6228\").",
                        "Só eles (e com permissão 2) recebem os avisos automáticos da história e usam o painel.",
                        "Vazio (padrão) = todo op é mestre.")
                .define(IdsMestre.CHAVE_MESTRES, "");
        OUVIR_PADRAO = b.comment("Mestre em espectador ouve e vê o que o grupo recebe perto dele (sino, pistas, clima).",
                        "Cada mestre muda o dele com /odteste mestre ouvir on|off (vale até o servidor reiniciar).")
                .define(IdsMestre.CHAVE_OUVIR_PADRAO, true);
        PULAR_CENAS = b.comment("Os jogadores podem pular as cenas de câmera segurando o pulo por 1 s.",
                        "false = a cena vai até o fim (o mestre que assiste sempre pode pular a dele).",
                        "Cada cena também pode proibir no próprio arquivo (\"pular\": false).")
                .define(IdsMestre.CHAVE_PULAR_CENAS, true);
        b.pop();
        SPEC = b.build();
    }

    private ConfigMestre() {}

    /** Chame no construtor do mod. */
    public static void registrar() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, SPEC, IdsMestre.ARQUIVO_CONFIG);
    }

    /** Seguros antes da config carregar: devolvem o padrão. */
    public static List<String> mestres() {
        try {
            return RegrasMestre.nomes(MESTRES.get());
        } catch (IllegalStateException carregando) {
            return List.of();
        }
    }

    public static boolean pularCenas() {
        try {
            return PULAR_CENAS.get();
        } catch (IllegalStateException carregando) {
            return true;
        }
    }

    public static boolean ouvirPadrao() {
        try {
            return OUVIR_PADRAO.get();
        } catch (IllegalStateException carregando) {
            return true;
        }
    }
}
