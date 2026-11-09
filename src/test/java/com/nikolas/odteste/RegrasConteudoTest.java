package com.nikolas.odteste;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.criaturas.RegrasCriaturas;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.efeitos.RegrasSangramento;
import com.nikolas.odteste.itens.RegrasArtefato;
import com.nikolas.odteste.itens.RegrasArtefato.Bloqueio;
import com.nikolas.odteste.itens.RegrasArtefato.Raridade;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Play test de Sangue, parte 2: contas da Rubra, dos artefatos e do nascimento das criaturas (Java puro). */
class RegrasConteudoTest {

    // ---------- Sangramento e Rubra ----------

    @Test
    void sangramentoCresceComONivelEParaNoMaximo() {
        assertEquals(0.5f, RegrasSangramento.danoPorSegundo(0));
        assertEquals(2.5f, RegrasSangramento.danoPorSegundo(4));
        assertEquals(2.5f, RegrasSangramento.danoPorSegundo(99));
        assertEquals(0.5f, RegrasSangramento.danoPorSegundo(-3));
    }

    @Test
    void rubraSoFicaForteAbaixoDe70PorCento() {
        assertFalse(RegrasSangramento.rubraForte(20, 20));
        assertFalse(RegrasSangramento.rubraForte(14, 20)); // 70% exatos: ainda não
        assertTrue(RegrasSangramento.rubraForte(13.9f, 20));
        assertFalse(RegrasSangramento.rubraForte(5, 0)); // vida máxima estranha não liga
    }

    @Test
    void rubraAcumulaAteCincoEAbreAFerida() {
        assertEquals(0, RegrasSangramento.proximoAcumulo(-1));
        assertEquals(4, RegrasSangramento.proximoAcumulo(3));
        assertEquals(4, RegrasSangramento.proximoAcumulo(4));
        assertFalse(RegrasSangramento.abreFerida(3));
        assertTrue(RegrasSangramento.abreFerida(4));
    }

    // ---------- Artefatos ----------

    @Test
    void recargaPorRaridade() {
        assertEquals(1200, Raridade.COMUM.recarga);
        assertEquals(3600, Raridade.INCOMUM.recarga);
        assertEquals(12000, Raridade.RARA.recarga);
    }

    @Test
    void bloqueioDoArtefato() {
        long t = 100_000;
        assertEquals(Bloqueio.LIVRE, RegrasArtefato.bloqueio(t, null, null, Raridade.COMUM, 50));
        // Recarga própria
        assertEquals(Bloqueio.RECARGA, RegrasArtefato.bloqueio(t, t - 1199, t - 1199, Raridade.COMUM, 50));
        assertEquals(Bloqueio.LIVRE, RegrasArtefato.bloqueio(t, t - 1200, t - 1200, Raridade.COMUM, 50));
        // Compartilhada: outro artefato usado há menos de 10 s
        assertEquals(Bloqueio.COMPARTILHADA, RegrasArtefato.bloqueio(t, null, t - 199, Raridade.RARA, 50));
        assertEquals(Bloqueio.LIVRE, RegrasArtefato.bloqueio(t, null, t - 200, Raridade.RARA, 50));
        // Lucidez zero bloqueia antes de tudo
        assertEquals(Bloqueio.SEM_LUCIDEZ, RegrasArtefato.bloqueio(t, null, null, Raridade.COMUM, 0));
        // Relógio que voltou (mundo trocado) não trava o jogador
        assertEquals(Bloqueio.LIVRE, RegrasArtefato.bloqueio(t, t + 500, t + 500, Raridade.RARA, 50));
    }

    @Test
    void custoDobraSeUsarDeNovoEmMenosDeCincoMinutos() {
        long t = 100_000;
        assertEquals(10, RegrasArtefato.custo(10, t, null));
        assertEquals(20, RegrasArtefato.custo(10, t, t - 5999));
        assertEquals(10, RegrasArtefato.custo(10, t, t - 6000));
    }

    @Test
    void registrarGuardaSoOsUsosRecentes() {
        long t = 100_000;
        Map<String, Long> usos = new HashMap<>();
        usos.put("velho", t - RegrasArtefato.GUARDAR);
        usos.put("recente", t - 100);
        usos.put("futuro", t + 10);
        Map<String, Long> m = RegrasArtefato.registrar(usos, "novo", t);
        assertEquals(Map.of("recente", t - 100, "novo", t), m);
        assertEquals(t, RegrasArtefato.ultimoDeTodos(m));
        assertNull(RegrasArtefato.ultimoDeTodos(Map.of()));
    }

    @Test
    void custosDosArtefatosComoNoLore() {
        assertEquals(10, RegrasArtefato.AGULHA_EXPOSICAO);
        assertEquals(200, RegrasArtefato.AGULHA_REGEN_TICKS);
        assertEquals(5, RegrasArtefato.DEDAL_LUCIDEZ);
        assertEquals(400, RegrasArtefato.DEDAL_TICKS);
        assertEquals(-2.0, RegrasArtefato.DEDAL_VIDA_MAX);
        assertEquals(25, RegrasArtefato.SINO_EXPOSICAO);
        assertEquals(6f, RegrasArtefato.SINO_VIDA);
        assertEquals(300, RegrasArtefato.SINO_TICKS);
    }

    // ---------- Criaturas ----------

    @Test
    void naoNascemNasPrimeirasNoitesNemPertoDoSpawn() {
        long dia = 24000;
        // Tudo certo: 3º dia, longe, jogador Tocado perto.
        assertTrue(RegrasCriaturas.podeNascer(3 * dia, 300, 0, 20));
        // Primeiros dias: nunca.
        assertFalse(RegrasCriaturas.podeNascer(3 * dia - 1, 1000, 1000, 100));
        // Perto do spawn do mundo: nunca.
        assertFalse(RegrasCriaturas.podeNascer(100 * dia, 150, 150, 100));
        assertTrue(RegrasCriaturas.podeNascer(100 * dia, 256, 0, 100));
        // Sem jogador exposto perto: o paranormal ainda não veio atrás.
        assertFalse(RegrasCriaturas.podeNascer(100 * dia, 1000, 0, 19));
        assertFalse(RegrasCriaturas.podeNascer(100 * dia, 1000, 0, 0));
    }

    @Test
    void umaPorJogadorTocadoPorNoite() {
        long noite = RegrasCriaturas.noite(5 * 24000L + 18000);
        assertEquals(5, noite);
        // Dormir passa o relógio para a manhã seguinte: noite nova.
        assertEquals(6, RegrasCriaturas.noite(6 * 24000L + 100));
        assertTrue(RegrasCriaturas.livreNestaNoite(RegrasCriaturas.NUNCA, noite));
        assertFalse(RegrasCriaturas.livreNestaNoite(noite, noite));
        assertTrue(RegrasCriaturas.livreNestaNoite(noite - 1, noite));
    }

    @Test
    void naoNascemEmBiomaSemMonstro() throws Exception {
        for (String id : IdsDados.CRIATURAS) {
            String spawn = Files.readString(DATA.resolve("neoforge/biome_modifier/" + id + ".json"));
            assertTrue(spawn.contains("\"neoforge:not\"") && spawn.contains("minecraft:mushroom_fields")
                    && spawn.contains("#c:no_default_monsters"), id + " não pode nascer em Mushroom Fields");
        }
    }

    @Test
    void retalhadoCegoNasCostas() {
        assertEquals(1.5f, RegrasCriaturas.multiplicadorCostas(-1));
        assertEquals(1.5f, RegrasCriaturas.multiplicadorCostas(-0.6));
        assertEquals(1f, RegrasCriaturas.multiplicadorCostas(-0.5));
        assertEquals(1f, RegrasCriaturas.multiplicadorCostas(1));
    }

    // ---------- Arquivos ----------

    private static final Path ASSETS = Path.of("src/main/resources/assets/odteste");
    private static final Path DATA = Path.of("src/main/resources/data/odteste");

    @Test
    void cadaItemTemModeloENomeNasDuasLinguas() throws Exception {
        String pt = Files.readString(ASSETS.resolve("lang/pt_br.json"));
        for (String id : IdsDados.ITENS) {
            assertTrue(Files.exists(ASSETS.resolve("models/item/" + id + ".json")), "falta o modelo do item " + id);
            assertTrue(pt.contains("\"item.odteste." + id + "\""), "falta o nome do item " + id);
        }
    }

    @Test
    void cadaCriaturaTemModeloAnimacaoDropSpawnENome() throws Exception {
        String pt = Files.readString(ASSETS.resolve("lang/pt_br.json"));
        String tag = Files.readString(DATA.resolve("tags/entity_type/paranormal.json"));
        for (String id : IdsDados.CRIATURAS) {
            assertTrue(Files.exists(ASSETS.resolve("geo/entity/" + id + ".geo.json")), "falta o geo de " + id);
            assertTrue(Files.exists(ASSETS.resolve("animations/entity/" + id + ".animation.json")), "falta a animação de " + id);
            assertTrue(Files.exists(DATA.resolve("loot_table/entities/" + id + ".json")), "falta o drop de " + id);
            assertTrue(Files.exists(DATA.resolve("neoforge/biome_modifier/" + id + ".json")), "falta o spawn de " + id);
            assertTrue(pt.contains("\"entity.odteste." + id + "\""), "falta o nome de " + id);
            assertTrue(tag.contains("\"odteste:" + id + "\""), id + " precisa estar na tag paranormal");
        }
    }

    @Test
    void ritualSoExisteComOccultism() throws Exception {
        String receita = Files.readString(DATA.resolve("recipe/ritual/" + IdsDados.RITUAL_CHAMADO + ".json"));
        assertTrue(receita.contains("\"neoforge:mod_loaded\"") && receita.contains("\"occultism\""),
                "a receita do ritual precisa da condição mod_loaded do Occultism");
        assertTrue(receita.contains("\"odteste:" + IdsDados.RITUAL_CHAMADO + "\""));
    }

    @Test
    void etiquetaSemMateriaEstaNosIds() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        assertTrue(ids.contains("`" + IdsDados.ETIQUETA_SEM_MATERIA + "`"));
    }
}
