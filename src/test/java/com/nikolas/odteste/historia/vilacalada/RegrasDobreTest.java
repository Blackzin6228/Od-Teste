package com.nikolas.odteste.historia.vilacalada;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.historia.vilacalada.RegrasDobre.Andamento;
import com.nikolas.odteste.historia.vilacalada.RegrasDobre.Fim;
import com.nikolas.odteste.historia.vilacalada.RegrasDobre.Toque;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegrasDobreTest {
    private static final UUID ANA = new UUID(0, 1);
    private static final UUID BIA = new UUID(0, 2);
    /** Meia-noite do dia 10. */
    private static final long MEIA = 10 * 24_000L + RegrasDobre.MEIA_NOITE;

    /** Bate e aplica; devolve o julgamento. */
    private static Toque bater(Andamento a, long agora, UUID quem, boolean botao) {
        Toque t = RegrasDobre.julgar(a, agora, agora, quem, botao, false);
        RegrasDobre.aplicar(a, t, agora, quem);
        return t;
    }

    /** O dobre certo: alternando a mão, lento, pausa depois do quinto. */
    private static long dobreCerto(Andamento a, boolean botao) {
        long t = MEIA;
        for (int i = 1; i <= RegrasDobre.TOQUES; i++) {
            if (i > 1) t += RegrasDobre.espera(i);
            Toque r = bater(a, t, i % 2 == 1 ? ANA : BIA, botao);
            if (i == RegrasDobre.TOQUES && !botao) {
                assertEquals(Toque.PESADO, r);
                return t;
            }
            assertEquals(Toque.CONTOU, r, "toque " + i);
        }
        return t;
    }

    @Test
    void seteToquesUmPorMortoMaisOSineiro() {
        assertEquals(7, RegrasDobre.TOQUES);
        assertEquals(RegrasDobre.TOQUES, RegrasDobre.peitoril().length);
        assertEquals(91, RegrasDobre.ESPACO_SINEIRO.z(), "o vazio da ponta do peitoril (v1 z 66 + 25)");
    }

    @Test
    void dobreCertoFechaComFeito() {
        Andamento a = new Andamento();
        long fim = dobreCerto(a, true);
        assertEquals(7, a.toques);
        assertFalse(RegrasDobre.fechou(a, fim + RegrasDobre.FECHA - 1), "o som ainda não morreu");
        assertTrue(RegrasDobre.fechou(a, fim + RegrasDobre.FECHA));
        assertEquals(Fim.FEITO, RegrasDobre.fim(a, true));
    }

    @Test
    void naoDaParaChutarBatendoAteDarCerto() {
        Andamento a = new Andamento();
        long t = dobreCerto(a, true);
        // Um oitavo antes de o som morrer: toque a mais.
        assertEquals(Toque.A_MAIS, RegrasDobre.julgar(a, t + RegrasDobre.INTERVALO_MIN, t + RegrasDobre.INTERVALO_MIN, BIA, true, false));
        assertTrue(Toque.A_MAIS.bate);
        assertTrue(RegrasDobre.gastaNoite(Toque.A_MAIS));
    }

    @Test
    void semBotaoDoSineiroOUltimoNaoSai() {
        Andamento a = new Andamento();
        dobreCerto(a, false);
        assertEquals(6, a.toques);
        assertFalse(Toque.PESADO.bate, "martelo pesado: não fere nem soa");
        assertEquals(Fim.FALTOU_SINEIRO, RegrasDobre.fim(a, false));
    }

    @Test
    void contagemErrada() {
        Andamento a = new Andamento();
        bater(a, MEIA, ANA, true);
        bater(a, MEIA + 60, BIA, true);
        assertEquals(Fim.CONTA_ERRADA, RegrasDobre.fim(a, true));
        Andamento b = new Andamento();
        for (int i = 0; i < 6; i++) bater(b, MEIA + i * 200L, i % 2 == 0 ? ANA : BIA, true);
        assertEquals(Fim.CONTA_ERRADA, RegrasDobre.fim(b, true), "seis com o botão posto: faltou tocar pelo Sineiro");
    }

    @Test
    void mesmaMaoNaoBateDuasSeguidas() {
        Andamento a = new Andamento();
        bater(a, MEIA, ANA, true);
        assertEquals(Toque.MESMA_MAO, RegrasDobre.julgar(a, MEIA + 200, MEIA + 200, ANA, true, false));
        assertFalse(Toque.MESMA_MAO.bate);
        assertEquals(Toque.CONTOU, RegrasDobre.julgar(a, MEIA + 200, MEIA + 200, BIA, true, false));
    }

    @Test
    void rapidoDemaisChamaEGastaANoite() {
        Andamento a = new Andamento();
        bater(a, MEIA, ANA, true);
        assertEquals(Toque.RAPIDO, RegrasDobre.julgar(a, MEIA + RegrasDobre.INTERVALO_MIN - 1, MEIA, BIA, true, false));
        assertTrue(RegrasDobre.gastaNoite(Toque.RAPIDO));
        assertEquals(Toque.CONTOU, RegrasDobre.julgar(a, MEIA + RegrasDobre.INTERVALO_MIN, MEIA, BIA, true, false));
    }

    @Test
    void pausaDeCincoEmCinco() {
        Andamento a = new Andamento();
        long t = MEIA;
        for (int i = 1; i <= 5; i++) {
            bater(a, t, i % 2 == 1 ? ANA : BIA, true);
            t += RegrasDobre.INTERVALO_MIN;
        }
        long depois = t - RegrasDobre.INTERVALO_MIN;
        assertEquals(Toque.RAPIDO, RegrasDobre.julgar(a, depois + RegrasDobre.INTERVALO_MIN, MEIA, BIA, true, false),
                "o sexto sem pausa chama");
        assertEquals(Toque.CONTOU, RegrasDobre.julgar(a, depois + RegrasDobre.PAUSA_CINCO, MEIA, BIA, true, false));
        assertTrue(RegrasDobre.PAUSA_CINCO > RegrasDobre.INTERVALO_MIN && RegrasDobre.PAUSA_CINCO < RegrasDobre.FECHA);
    }

    @Test
    void soAMeiaNoiteComeca() {
        Andamento a = new Andamento();
        assertEquals(Toque.FORA_DE_HORA, RegrasDobre.julgar(a, 0, 10 * 24_000L + 14_000, ANA, true, false));
        assertFalse(RegrasDobre.gastaNoite(Toque.FORA_DE_HORA), "fora de hora chama, mas ainda dá para esperar a meia-noite");
        assertEquals(Toque.CONTOU, RegrasDobre.julgar(a, 0, MEIA - RegrasDobre.JANELA, ANA, true, false));
        assertEquals(Toque.CONTOU, RegrasDobre.julgar(a, 0, MEIA + RegrasDobre.JANELA, ANA, true, false));
        assertEquals(Toque.FORA_DE_HORA, RegrasDobre.julgar(a, 0, MEIA + RegrasDobre.JANELA + 1, ANA, true, false));
        // Depois de começar, o dobre segue mesmo que a janela passe.
        bater(a, 0, MEIA + RegrasDobre.JANELA, ANA, true);
        assertEquals(Toque.CONTOU, RegrasDobre.julgar(a, 100, MEIA + RegrasDobre.JANELA + 100, BIA, true, false));
    }

    private static void bater(Andamento a, long agora, long hora, UUID quem, boolean botao) {
        Toque t = RegrasDobre.julgar(a, agora, hora, quem, botao, false);
        RegrasDobre.aplicar(a, t, agora, quem);
    }

    @Test
    void noiteGastaNaoResponde() {
        Andamento a = new Andamento();
        assertEquals(Toque.NOITE_GASTA, RegrasDobre.julgar(a, MEIA, MEIA, ANA, true, true));
        assertFalse(Toque.NOITE_GASTA.bate);
        assertEquals(10, RegrasDobre.dia(MEIA));
        assertEquals(RegrasDobre.dia(10 * 24_000L), RegrasDobre.dia(MEIA), "a noite pertence ao dia em que começou");
    }

    @Test
    void danoNuncaMata() {
        assertEquals(RegrasDobre.DANO_TOQUE, RegrasDobre.dano(20f));
        assertEquals(1f, RegrasDobre.dano(2f), 1e-6);
        assertEquals(0f, RegrasDobre.dano(1f), 1e-6);
        assertEquals(0f, RegrasDobre.dano(0.5f), 1e-6);
    }

    @Test
    void ecoAndaPelaRua() {
        assertEquals(0, RegrasDobre.eco(0));
        assertEquals(40, RegrasDobre.eco(40));
        assertTrue(RegrasDobre.eco(80) > RegrasDobre.eco(10), "quem está longe se ajoelha depois");
    }

    @Test
    void empurroes() {
        assertFalse(RegrasDobre.empurraoCliques(1));
        assertTrue(RegrasDobre.empurraoCliques(2));
        assertFalse(RegrasDobre.empurraoCorrente(1));
        assertTrue(RegrasDobre.empurraoCorrente(2));
        assertTrue(RegrasDobre.manha(5 * 24_000L + 100));
        assertFalse(RegrasDobre.manha(5 * 24_000L + 6_000));
        int c = RegrasDobre.proximosCliques(0);
        assertEquals(RegrasDobre.CLIQUES_MIN, c);
        assertTrue(RegrasDobre.proximosCliques(1) < RegrasDobre.CLIQUES_MAX);
        assertTrue(RegrasDobre.botaoDePedra("minecraft:stone_button"));
        assertFalse(RegrasDobre.botaoDePedra("minecraft:oak_button"), "só o botão de pedra é o do Sineiro");
    }

    @Test
    void posicoesBatemComOArquivoDaVila() throws Exception {
        String json = Files.readString(Path.of("arte/vila-calada-150-v2/vila_calada_150_v2.pistas.json")).replaceAll("\\s", "");
        assertTrue(json.contains("\"pos\":[86,18,56],\"tipo\":\"martelo\""), "martelo ao lado do atril");
        assertTrue(json.contains("\"pos\":[99,1,72],\"tipo\":\"bigorna\""));
        assertTrue(json.contains("\"pos\":[76,3,33],\"tipo\":\"lapide\""));
        assertTrue(json.contains("\"pos\":[54,2,85],\"tipo\":\"botoes\""), "primeiro botão do peitoril");
        assertEquals(RegrasDobre.MARTELO.x(), 86);
        assertEquals(RegrasDobre.BIGORNA.y(), 1);
        assertEquals(RegrasDobre.LAPIDE.z(), 33);
    }

    @Test
    void corDaVilaCalada() {
        float vermelho = RegrasClimaVila.misturarCanal(0f, 16, 1f, 1f);
        float cinza = RegrasClimaVila.misturarCanal(0f, 16, 1f, 0f);
        assertEquals(RegrasClimaVila.misturarCanal(0f, 16, 1f), vermelho, 1e-6, "com fome, igual a antes");
        assertEquals(((RegrasClimaVila.COR_CALADA >> 16) & 0xFF) / 255f, cinza, 1e-6);
        float azulCalada = RegrasClimaVila.misturarCanal(0f, 0, 1f, 0f);
        assertTrue(azulCalada > RegrasClimaVila.misturarCanal(0f, 0, 1f, 1f), "calada perde o vermelho: o azul sobe");
    }

    @Test
    void idsNoDocsEChavesNoCodigo() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        String codigo = Files.readString(Path.of("src/main/java/com/nikolas/odteste/historia/vilacalada/DadosVilaCalada.java"));
        for (String k : IdsVilaCalada.CHAVES_DOBRE) {
            assertTrue(ids.contains("`" + k + "`"), "falta a chave " + k + " em docs/ids.md");
            assertTrue(codigo.contains("\"" + k + "\""), "DadosVilaCalada não usa " + k);
        }
        assertTrue(ids.contains("`" + IdsVilaCalada.MARCO_DOBRE + "`"));
        assertTrue(ids.contains("`" + IdsVilaCalada.LUGAR_MOINHO + "`"));
        assertTrue(ids.contains("`odteste:" + IdsVilaCalada.TAG_OUVE_DOBRE + "`"));
        assertTrue(ids.contains("`odteste:" + IdsVilaCalada.MODIFICADOR_DOBRE + "`"));
        assertTrue(ids.contains("`odteste:" + IdsVilaCalada.PACOTE_PENDURADO_SINO + "`"));
        assertTrue(Files.exists(Path.of("src/main/resources/data/odteste/tags/entity_type/" + IdsVilaCalada.TAG_OUVE_DOBRE + ".json")));
    }
}
