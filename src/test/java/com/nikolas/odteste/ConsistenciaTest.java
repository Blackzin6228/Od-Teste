package com.nikolas.odteste;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/** Travas baratas que pegam erro antes de virar crash no servidor. */
class ConsistenciaTest {

    private static Properties props() throws Exception {
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(Path.of("gradle.properties"))) {
            p.load(r);
        }
        return p;
    }

    @Test
    void modidIgualNoCodigoENoGradle() throws Exception {
        assertEquals(props().getProperty("mod_id"), OdMod.MODID);
    }

    @Test
    void pastaDeAssetsBateComOModid() throws Exception {
        String modid = props().getProperty("mod_id");
        assertTrue(Files.isDirectory(Path.of("src/main/resources/assets", modid)),
                "a pasta assets/" + modid + " precisa existir");
    }

    @Test
    void portuguesEInglesTemAsMesmasChaves() throws Exception {
        String modid = props().getProperty("mod_id");
        Path lang = Path.of("src/main/resources/assets", modid, "lang");
        var pt = chaves(Files.readString(lang.resolve("pt_br.json")));
        var en = chaves(Files.readString(lang.resolve("en_us.json")));
        assertEquals(pt, en, "toda tradução em pt_br precisa existir em en_us (e vice-versa)");
    }

    /** Vela e poça não podem ser quebradas pelo Wither nem pelo Dragão (e a tag não pode apagar a de outros mods). */
    @Test
    void velaEPocaImunesAoWitherEAoDragao() throws Exception {
        Path tags = Path.of("src/main/resources/data/minecraft/tags/block");
        for (String tag : java.util.List.of("wither_immune.json", "dragon_immune.json")) {
            String json = Files.readString(tags.resolve(tag));
            assertTrue(json.matches("(?s).*\"replace\"\\s*:\\s*false.*"), tag + " precisa de \"replace\": false");
            for (String id : java.util.List.of("odteste:vela_da_capela", "odteste:poca_de_sangue")) {
                assertTrue(json.contains("\"" + id + "\""), tag + " precisa ter " + id);
            }
        }
    }

    private static java.util.Set<String> chaves(String json) {
        var m = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:").matcher(json);
        var s = new java.util.TreeSet<String>();
        while (m.find()) s.add(m.group(1));
        return s;
    }
}
