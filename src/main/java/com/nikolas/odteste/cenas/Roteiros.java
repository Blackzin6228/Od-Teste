package com.nikolas.odteste.cenas;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.vilacalada.RegrasFxSangue;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * As cenas em JSON do servidor: {@code data/<mod>/cenas/<id>.json} (as do mod vêm no jar; um datapack do mundo pode
 * trocar ou somar, e o {@code /reload} relê). Cena com erro é pulada com aviso no log (o resto carrega).
 * Id = nome do arquivo (as de outro namespace ficam {@code <namespace>.<nome>}).
 */
public final class Roteiros {
    private static final Gson GSON = new Gson();

    /** Cena lida + o texto que vai no pacote (o cliente lê o mesmo texto). */
    public record Carregado(Roteiro roteiro, String json) {}

    private static volatile Map<String, Carregado> TODAS = Map.of();

    private Roteiros() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent e) -> e.addListener(new Leitor()));
    }

    public static Optional<Carregado> de(String id) {
        return Optional.ofNullable(TODAS.get(id));
    }

    public static Set<String> ids() {
        return TODAS.keySet();
    }

    /** Lê e confere uma cena. Erro: exceção com a frase do problema. Usado também no teste. */
    public static Carregado carregar(String id, String json) {
        Roteiro r = LeitorRoteiro.ler(id, json);
        List<String> problemas = RegrasRoteiro.problemas(r, Set.copyOf(RegrasFxSangue.EFEITOS));
        if (!problemas.isEmpty()) throw new IllegalArgumentException(String.join("; ", problemas));
        return new Carregado(r, json);
    }

    static String idDe(ResourceLocation rl) {
        return rl.getNamespace().equals(OdMod.MODID) ? rl.getPath() : rl.getNamespace() + "." + rl.getPath();
    }

    private static final class Leitor extends SimpleJsonResourceReloadListener {
        Leitor() {
            super(GSON, IdsCena.PASTA);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> arquivos, ResourceManager rm, ProfilerFiller perfil) {
            Map<String, Carregado> lidas = new TreeMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> en : arquivos.entrySet()) {
                String id = idDe(en.getKey());
                if (!id.matches("[a-z0-9_.]+")) {
                    OdMod.LOGGER.warn("Cena {}: nome inválido (só a-z, 0-9 e _), pulada", en.getKey());
                    continue;
                }
                try {
                    lidas.put(id, carregar(id, GSON.toJson(en.getValue())));
                } catch (IllegalArgumentException erro) {
                    OdMod.LOGGER.warn("Cena {} com problema, pulada: {}", en.getKey(), erro.getMessage());
                }
            }
            TODAS = Map.copyOf(lidas);
            OdMod.LOGGER.info("Cenas em JSON carregadas: {}", lidas.keySet());
        }
    }
}
