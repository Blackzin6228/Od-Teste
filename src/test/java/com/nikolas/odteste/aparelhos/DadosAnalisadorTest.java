package com.nikolas.odteste.aparelhos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Travas dos arquivos de dados do Analisador: receitas condicionais, modelo, drop e IDs documentados.
 * Pega erro que só apareceria no servidor (receita que some, bloco sem drop, modelo roxo-e-preto).
 */
class DadosAnalisadorTest {
    private static final Path RES = Path.of("src/main/resources");
    private static final Path RECEITAS = RES.resolve("data/odteste/recipe");
    private static final Pattern ID_ITEM = Pattern.compile("\"(?:item|tag)\"\\s*:\\s*\"([a-z0-9_.-]+):");
    private static final Pattern MOD_LOADED =
            Pattern.compile("\"neoforge:mod_loaded\"\\s*,\\s*\"modid\"\\s*:\\s*\"([a-z0-9_]+)\"");
    private static final Set<String> SEM_CONDICAO = Set.of("minecraft", "c", "odteste", "neoforge");

    private static List<Path> receitasCompat() throws IOException {
        List<Path> lista = new ArrayList<>();
        try (Stream<Path> s = Files.walk(RECEITAS.resolve("compat"))) {
            s.filter(p -> p.toString().endsWith(".json")).forEach(lista::add);
        }
        return lista;
    }

    private static Set<String> modsCondicionados(String json) {
        Set<String> mods = new TreeSet<>();
        Matcher m = MOD_LOADED.matcher(json);
        while (m.find()) mods.add(m.group(1));
        return mods;
    }

    @Test
    void receitaCompatSoCarregaComOMod() throws IOException {
        List<Path> receitas = receitasCompat();
        assertFalse(receitas.isEmpty());
        for (Path p : receitas) {
            String json = Files.readString(p);
            String pastaDoMod = RECEITAS.resolve("compat").relativize(p).getName(0).toString();
            Set<String> condicoes = modsCondicionados(json);
            assertTrue(condicoes.contains(pastaDoMod), p + " precisa de neoforge:mod_loaded \"" + pastaDoMod + "\"");
            Matcher m = ID_ITEM.matcher(json);
            while (m.find()) {
                String ns = m.group(1);
                if (!SEM_CONDICAO.contains(ns)) {
                    assertTrue(condicoes.contains(ns), p + " usa item de " + ns + " sem condição mod_loaded");
                }
            }
        }
    }

    @Test
    void reservaSoVanillaENegaTodosOsModsDasOutrasReceitas() throws IOException {
        String reserva = Files.readString(RECEITAS.resolve("reserva/analisador_sinal.json"));
        assertTrue(reserva.contains("\"neoforge:not\"") && reserva.contains("\"neoforge:or\""));
        Matcher m = ID_ITEM.matcher(reserva);
        while (m.find()) assertEquals("minecraft", m.group(1), "a reserva não pode depender de outro mod");

        // Para qualquer combinação de mods presentes, sempre existe ao menos uma receita: a reserva nega
        // exatamente os mods das receitas compat do mesmo item.
        Set<String> modsCompat = new TreeSet<>();
        for (Path p : receitasCompat()) {
            String json = Files.readString(p);
            if (json.contains("\"odteste:analisador_sinal\"")) modsCompat.addAll(modsCondicionados(json));
        }
        assertEquals(modsCompat, modsCondicionados(reserva));
    }

    @Test
    void reservaMaisCaraQueAsOutras() throws IOException {
        String reserva = Files.readString(RECEITAS.resolve("reserva/analisador_sinal.json"));
        assertTrue(reserva.contains("minecraft:copper_block") && reserva.contains("minecraft:redstone_block"),
                "a reserva sem tech usa blocos inteiros (mais cara)");
    }

    @Test
    void blockstateCobreTodasAsCombinacoes() throws IOException {
        String json = Files.readString(RES.resolve("assets/odteste/blockstates/analisador_sinal.json"));
        for (String lado : List.of("north", "east", "south", "west")) {
            for (String ligado : List.of("false", "true")) {
                for (String sinal : List.of("false", "true")) {
                    String chave = "\"facing=" + lado + ",ligado=" + ligado + ",sinal=" + sinal + "\"";
                    assertTrue(json.contains(chave), "falta a variante " + chave);
                }
            }
        }
        Matcher m = Pattern.compile("\"model\"\\s*:\\s*\"odteste:block/([a-z_]+)\"").matcher(json);
        while (m.find()) {
            Path modelo = RES.resolve("assets/odteste/models/block/" + m.group(1) + ".json");
            assertTrue(Files.exists(modelo), "falta o modelo " + modelo);
        }
        assertTrue(Files.exists(RES.resolve("assets/odteste/models/item/analisador_sinal.json")));
    }

    @Test
    void blocoTemDropEFerramenta() throws IOException {
        assertTrue(Files.readString(RES.resolve("data/odteste/loot_table/blocks/analisador_sinal.json"))
                .contains("\"odteste:analisador_sinal\""));
        String picareta = Files.readString(RES.resolve("data/minecraft/tags/block/mineable/pickaxe.json"));
        assertTrue(picareta.contains("\"replace\": false"), "tag vanilla nunca substitui a dos outros mods");
        assertTrue(picareta.contains("odteste:analisador_sinal"));
    }

    @Test
    void idsNovosEstaoDocumentados() throws IOException {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (String id : List.of("`odteste:analisador_sinal`", "`energia`", "`ligado`", "`sinal`")) {
            assertTrue(ids.contains(id), "docs/ids.md precisa listar " + id);
        }
    }

    @Test
    void textosDoAparelhoNosDoisIdiomas() throws IOException {
        for (String lang : List.of("pt_br", "en_us")) {
            String json = Files.readString(RES.resolve("assets/odteste/lang/" + lang + ".json"));
            for (String chave : List.of("block.odteste.analisador_sinal", "odteste.analisador.dica",
                    "odteste.analisador.sem_energia", "odteste.analisador.estatica", "odteste.analisador.cabecalho",
                    "odteste.analisador.camada", "odteste.sinal.sino_vila.decifrado", "odteste.sinal.sino_vila.camada")) {
                assertTrue(json.contains("\"" + chave + "\""), lang + " sem " + chave);
            }
        }
    }
}
