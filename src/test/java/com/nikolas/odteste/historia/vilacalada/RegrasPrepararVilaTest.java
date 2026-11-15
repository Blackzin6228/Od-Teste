package com.nikolas.odteste.historia.vilacalada;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class RegrasPrepararVilaTest {
    /** As pistas do arquivo do Modelador: "x,y,z,tipo". */
    private static List<String> pistasDoArquivo() throws Exception {
        String json = Files.readString(Path.of("arte/vila-calada-150-v2/vila_calada_150_v2.pistas.json")).replaceAll("\\s", "");
        Matcher m = Pattern.compile("\"pos\":\\[(\\d+),(\\d+),(\\d+)\\],\"tipo\":\"(\\w+)\"").matcher(json);
        List<String> r = new ArrayList<>();
        while (m.find()) r.add(m.group(1) + "," + m.group(2) + "," + m.group(3) + "," + m.group(4));
        return r;
    }

    private static String chave(RegrasPrepararVila.Peca p, String tipo) {
        return p.x() + "," + p.y() + "," + p.z() + "," + tipo;
    }

    @Test
    void posicoesBatemComOArquivoDaVila() throws Exception {
        List<String> pistas = pistasDoArquivo();
        assertFalse(pistas.isEmpty());
        assertTrue(pistas.contains(chave(RegrasPrepararVila.ATRIL, "atril")));
        assertTrue(pistas.contains(chave(RegrasPrepararVila.SINO, "sino")));
        assertTrue(pistas.contains(chave(RegrasPrepararVila.VELA, "vela")));
        assertTrue(pistas.contains(chave(RegrasPrepararVila.BAU_POCO, "bau")));
        assertTrue(pistas.contains(chave(RegrasPrepararVila.PLACA_POCO, "placa")));
        // As poças da praça estão escritas na nota da primeira; a do curral é uma pista "poca".
        assertTrue(pistas.contains(chave(RegrasPrepararVila.POCAS.get(0), "poca")));
        assertTrue(pistas.contains(chave(RegrasPrepararVila.POCAS.get(3), "poca")));
    }

    @Test
    void cantoPeloSino() {
        // Canto da vila no servidor (14/11): 3928 63 3954. O sino fica em +85 +19 +57.
        assertArrayEquals(new int[] {3928, 63, 3954}, RegrasPrepararVila.canto(4013, 82, 4011, RegrasPrepararVila.SINO));
    }

    @Test
    void soEntraNoArOuNoQueOTemplatePos() {
        assertTrue(RegrasPrepararVila.pocaPodeEntrar("minecraft:air"));
        assertTrue(RegrasPrepararVila.pocaPodeEntrar("minecraft:redstone_wire"));
        assertFalse(RegrasPrepararVila.pocaPodeEntrar("minecraft:chest"), "baú de jogador fica");
        assertFalse(RegrasPrepararVila.pocaPodeEntrar("minecraft:torch"), "tocha do grupo fica");
        assertTrue(RegrasPrepararVila.velaPodeEntrar("minecraft:candle"));
        assertTrue(RegrasPrepararVila.velaPodeEntrar("minecraft:red_candle"));
        assertTrue(RegrasPrepararVila.velaPodeEntrar("minecraft:cave_air"));
        assertFalse(RegrasPrepararVila.velaPodeEntrar("minecraft:candle_cake"));
        assertFalse(RegrasPrepararVila.velaPodeEntrar("outromod:candle"));
        assertFalse(RegrasPrepararVila.velaPodeEntrar(null));
    }

    @Test
    void chaveDoQueJaFoiPreparado() {
        assertEquals("5:bau_poco", RegrasPrepararVila.chave(5, RegrasPrepararVila.BAU_POCO));
        assertNotEquals(RegrasPrepararVila.chave(5, RegrasPrepararVila.BAU_POCO), RegrasPrepararVila.chave(6, RegrasPrepararVila.BAU_POCO));
    }

    @Test
    void sinoRespondeLogoDepoisDoEnigma() {
        long agora = 100_000;
        assertEquals(agora + RegrasVilaCalada.TOQUE_DEPOIS_ENIGMA, RegrasVilaCalada.adiantar(null, agora));
        assertEquals(agora + RegrasVilaCalada.TOQUE_DEPOIS_ENIGMA, RegrasVilaCalada.adiantar(agora + 4_000, agora), "puxa para perto");
        assertEquals(agora + 50, RegrasVilaCalada.adiantar(agora + 50, agora), "já ia tocar antes: fica");
        assertEquals(agora + RegrasVilaCalada.TOQUE_DEPOIS_ENIGMA, RegrasVilaCalada.adiantar(agora - 100_000, agora), "marca velha");
    }
}
