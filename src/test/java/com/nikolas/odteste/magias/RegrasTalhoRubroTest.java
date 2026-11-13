package com.nikolas.odteste.magias;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RegrasTalhoRubroTest {

    @Test
    void meiaLuaPegaSoNaFrenteDentroDoAlcance() {
        // Olhando para +Z.
        assertTrue(RegrasTalhoRubro.dentroDoArco(0, 0, 3, 0, 1), "bem na frente");
        assertTrue(RegrasTalhoRubro.dentroDoArco(2, 0, 2, 0, 1), "45° para o lado");
        assertFalse(RegrasTalhoRubro.dentroDoArco(3, 0, 0.5, 0, 1), "quase do lado (80°) fica fora");
        assertFalse(RegrasTalhoRubro.dentroDoArco(0, 0, -3, 0, 1), "atrás");
        assertFalse(RegrasTalhoRubro.dentroDoArco(0, 0, 4.5, 0, 1), "longe demais");
        assertFalse(RegrasTalhoRubro.dentroDoArco(0, 3, 2, 0, 1), "alto demais");
        assertTrue(RegrasTalhoRubro.dentroDoArco(0.2, 0, -0.2, 0, 1), "colado pega sempre");
        assertFalse(RegrasTalhoRubro.dentroDoArco(0, 0, 3, 0, 0), "sem direção não pega nada");
    }

    @Test
    void danoSobeComNivelEPoderSemQuebrar() {
        assertEquals(4f, RegrasTalhoRubro.dano(1, 1f), 1e-6);
        assertEquals(6f, RegrasTalhoRubro.dano(3, 1f), 1e-6);
        assertEquals(6f, RegrasTalhoRubro.dano(99, 1f), 1e-6, "nível acima do máximo é preso");
        assertEquals(4f, RegrasTalhoRubro.dano(0, 1f), 1e-6, "nível abaixo de 1 é preso");
        assertEquals(6f, RegrasTalhoRubro.dano(1, 1.5f), 1e-6);
        assertEquals(0f, RegrasTalhoRubro.dano(1, -2f), 1e-6, "poder negativo não cura");
        assertEquals(4f, RegrasTalhoRubro.dano(1, Float.NaN), 1e-6, "poder inválido vira 1");
    }

    @Test
    void numerosDentroDaTabelaDoBasico() {
        // docs/magias.md seção 4: Básico = mana 15–25, recarga 1–3 s.
        for (int n = 1; n <= RegrasTalhoRubro.NIVEL_MAX; n++) {
            int mana = RegrasTalhoRubro.MANA_BASE + RegrasTalhoRubro.MANA_POR_NIVEL * (n - 1);
            assertTrue(mana >= 15 && mana <= 25, "mana do nível " + n + " fora da tabela: " + mana);
        }
        assertTrue(RegrasTalhoRubro.RECARGA_SEGUNDOS >= 1 && RegrasTalhoRubro.RECARGA_SEGUNDOS <= 3);
        assertEquals(60, RegrasTalhoRubro.RASTRO_TICKS, "as manchas ficam 3 s");
        assertTrue(RegrasTalhoRubro.PREPARACAO_TICKS > 0 && RegrasTalhoRubro.PREPARACAO_TICKS <= 10, "preparação curta");
    }

    @Test
    void lucidezNuncaVaiAZeroPelaMagia() {
        assertTrue(RegrasTalhoRubro.podePagarLucidez(3, 2));
        assertFalse(RegrasTalhoRubro.podePagarLucidez(2, 2));
        assertFalse(RegrasTalhoRubro.podePagarLucidez(0, 2));
        assertTrue(RegrasTalhoRubro.podePagarLucidez(0, 0), "custo zero (criativo) sempre pode");
    }

    @Test
    void recargaSemIrons() {
        assertTrue(RegrasTalhoRubro.recargaPronta(100, -1), "nunca usou");
        assertFalse(RegrasTalhoRubro.recargaPronta(110, 100));
        assertTrue(RegrasTalhoRubro.recargaPronta(100 + RegrasTalhoRubro.RECARGA_SEM_IRONS_TICKS, 100));
        assertTrue(RegrasTalhoRubro.recargaPronta(50, 100), "relógio voltou: libera");
    }

    @Test
    void idsNoDocumento() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (String id : IdsMagia.TODOS) {
            assertTrue(ids.contains("`odteste:" + id + "`"), "falta odteste:" + id + " em docs/ids.md");
        }
    }

    @Test
    void textosDaMagiaNosDoisIdiomas() throws Exception {
        for (String arquivo : new String[] {"pt_br.json", "en_us.json"}) {
            String lang = Files.readString(Path.of("src/main/resources/assets/odteste/lang", arquivo));
            for (String chave : new String[] {"school.odteste." + IdsMagia.ESCOLA_SANGUE,
                    "spell.odteste." + IdsMagia.TALHO_RUBRO, "spell.odteste." + IdsMagia.TALHO_RUBRO + ".guide",
                    "attribute.odteste." + IdsMagia.PODER_SANGUE, "attribute.odteste." + IdsMagia.RESISTENCIA_SANGUE,
                    "death.attack.odteste." + IdsMagia.DANO_MAGIA_SANGUE}) {
                assertTrue(lang.contains("\"" + chave + "\""), arquivo + " precisa de " + chave);
            }
            for (Magias.Resultado r : Magias.Resultado.values()) {
                String chave = "odteste.comando.magia." + r.name().toLowerCase(java.util.Locale.ROOT);
                assertTrue(lang.contains("\"" + chave + "\""), arquivo + " precisa de " + chave);
            }
        }
    }
}
