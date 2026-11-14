package com.nikolas.odteste.historia.vilacalada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RegrasFxSangueTest {
    @Test
    void sangueCresceSecaESome() {
        assertEquals(0f, RegrasFxSangue.sangueForca(-1f));
        assertEquals(1f, RegrasFxSangue.sangueForca(1f), 1e-6);
        assertEquals(0f, RegrasFxSangue.sangueSeca(5f), 1e-6, "fresco enquanto escorre");
        assertEquals(1f, RegrasFxSangue.sangueSeca(RegrasFxSangue.sangueTotal()), 1e-6, "seco no fim");
        assertEquals(0f, RegrasFxSangue.sangueForca(RegrasFxSangue.sangueTotal()), 1e-6, "sumiu no fim");
        float meio = RegrasFxSangue.sangueForca(RegrasFxSangue.sangueTotal() - RegrasFxSangue.SANGUE_SOME / 2);
        assertTrue(meio > 0.3f && meio < 0.7f, "some devagar, não de uma vez");
    }

    @Test
    void batidaTemDoisTunsEVoltaAZero() {
        assertTrue(RegrasFxSangue.batida(0.03f) > 0.95f, "primeiro tum");
        float segundo = RegrasFxSangue.batida(0.23f);
        assertTrue(segundo > 0.5f && segundo < 0.7f, "segundo tum mais fraco");
        assertTrue(RegrasFxSangue.batida(0.13f) < segundo, "vale entre os dois");
        assertTrue(RegrasFxSangue.batida(0.6f) < 0.01f, "descansa até a próxima");
        assertEquals(0f, RegrasFxSangue.batida(-0.1f));
    }

    @Test
    void maisPertoBateMaisRapido() {
        assertEquals(0f, RegrasFxSangue.perto(30));
        assertEquals(1f, RegrasFxSangue.perto(2));
        assertTrue(RegrasFxSangue.intervalo(1f) < RegrasFxSangue.intervalo(0f));
        assertEquals(RegrasFxSangue.BATIDA_PERTO, RegrasFxSangue.intervalo(5f), 1e-6, "preso em 1");
    }

    @Test
    void efeitosFortesDiminuiMasNaoDesliga() {
        assertEquals(1f, RegrasFxSangue.forcaVisual(1f), 1e-6);
        assertEquals(0.5f, RegrasFxSangue.forcaVisual(0f), 1e-6, "DESLIGADO ainda mostra metade");
        assertEquals(0f, RegrasFxSangue.estagio(0));
        assertEquals(1f, RegrasFxSangue.estagio(9));
        assertEquals(RegrasFxSangue.TESTE_PADRAO, RegrasFxSangue.segundosTeste(0));
        assertEquals(RegrasFxSangue.TESTE_MAXIMO, RegrasFxSangue.segundosTeste(9999));
    }

    @Test
    void cadaEfeitoTemShaderEJson() throws Exception {
        Path core = Path.of("src/main/resources/assets/odteste/shaders/core");
        for (String e : RegrasFxSangue.EFEITOS) {
            assertTrue(Files.exists(core.resolve("odfx_" + e + ".fsh")), "falta odfx_" + e + ".fsh");
            String json = Files.readString(core.resolve("odfx_" + e + ".json"));
            assertTrue(json.contains("\"odteste:odfx_" + e + "\""), "json de " + e + " aponta para outro fsh");
            String fsh = Files.readString(core.resolve("odfx_" + e + ".fsh"));
            // Todo uniform do .fsh precisa estar no .json, senão o jogo nunca manda o valor.
            for (String linha : fsh.split("\n")) {
                String l = linha.trim();
                if (!l.startsWith("uniform ") || l.contains("sampler2D")) continue;
                String nome = l.split("\\s+")[2].replace(";", "");
                assertTrue(json.contains("\"" + nome + "\""), "odfx_" + e + ".json sem o uniform " + nome);
            }
        }
    }

    @Test
    void efeitosEPacoteEstaoEmIdsMd() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        assertTrue(ids.contains("`odteste:efeito_fx`"));
        for (String e : RegrasFxSangue.EFEITOS) assertTrue(ids.contains("`" + e + "`"), "falta " + e + " em docs/ids.md");
    }
}
