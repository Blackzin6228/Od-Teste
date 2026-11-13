package com.nikolas.odteste;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.dados.IdsDados;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Balanceamento tech x paranormal (decisão 2026-11-13, estudo seção 3.4): as travas que impedem
 * máquina de outro mod de facilitar o nosso mod. Tudo aqui é dado; o teste só lê os JSON.
 * (Leitor de JSON próprio, pequeno, para o teste não depender das bibliotecas do Minecraft.)
 */
class BalanceamentoTechTest {

    private static final Path DATA = Path.of("src/main/resources/data");

    /** Tags de criatura que barram captura, clone, teleporte, "sem IA", polimorfia, Drygmy e spawner de outros mods. */
    static final List<String> TAGS_CRIATURA = List.of(
            "c/tags/entity_type/capturing_not_supported.json",
            "c/tags/entity_type/teleporting_not_supported.json",
            "industrialforegoing/tags/entity_type/mob_imprisonment_tool_blacklist.json",
            "industrialforegoing/tags/entity_type/mob_duplicator_blacklist.json",
            "industrialforegoing/tags/entity_type/mob_crusher_blacklist.json",
            "justdirethings/tags/entity_type/paradox_deny.json",
            "justdirethings/tags/entity_type/paradox_absorb_deny.json",
            "justdirethings/tags/entity_type/polymorphic_target_deny.json",
            "justdirethings/tags/entity_type/no_ai_deny.json",
            "justdirethings/tags/entity_type/creature_catcher_deny.json",
            "ars_nouveau/tags/entity_type/drygmy_blacklist.json",
            "enderio/tags/entity_type/spawner_blacklist.json");

    /** Tags de bloco "não acelerável" (Time in a Bottle, Varinha do Tempo do JDT, Soul Surge do IF Souls). */
    static final List<String> TAGS_BLOCO_SEM_ACELERAR = List.of(
            "tiab/tags/block/un_acceleratable.json",
            "justdirethings/tags/block/tick_speed_deny.json",
            "industrialforegoingsouls/tags/block/cant_accelerate.json");

    /** Tags que fazem Mekanism, Create, moedores e o Almost Unified multiplicarem ou trocarem o item. */
    static final Set<String> TAGS_DE_MINERIO = Set.of(
            "ores", "raw_materials", "raw_blocks", "dusts", "ingots", "gems", "nuggets", "storage_blocks",
            "ores_in_ground", "ore_rates", "ore_bearing_ground", "crops", "seeds", "clusters");

    /** Pastas de dados que dariam fonte infinita da nossa matéria (modelo do HNN, abelha, árvore, semente). */
    static final Set<String> PASTAS_PROIBIDAS = Set.of(
            "hostilenetworks", "productivebees", "productivetrees", "mysticalagriculture");

    // ---------- Criaturas e blocos nas tags de bloqueio ----------

    @Test
    void todaCriaturaNossaEstaEmTodasAsTagsDeBloqueio() throws IOException {
        for (String arquivo : TAGS_CRIATURA) {
            Map<String, Object> tag = lerObjeto(DATA.resolve(arquivo));
            assertNaoApaga(tag, arquivo);
            List<String> valores = valoresDaTag(tag);
            for (String id : IdsDados.CRIATURAS) {
                assertTrue(valores.contains("odteste:" + id), arquivo + " precisa ter odteste:" + id);
            }
        }
    }

    @Test
    void todoBlocoNossoNaoPodeSerAcelerado() throws IOException {
        List<String> blocos = blocosDoMod();
        assertFalse(blocos.isEmpty(), "não achei nenhum bloco em assets/odteste/blockstates");
        for (String arquivo : TAGS_BLOCO_SEM_ACELERAR) {
            Map<String, Object> tag = lerObjeto(DATA.resolve(arquivo));
            assertNaoApaga(tag, arquivo);
            List<String> valores = valoresDaTag(tag);
            for (String bloco : blocos) {
                assertTrue(valores.contains(bloco), arquivo + " precisa ter " + bloco);
            }
        }
    }

    // ---------- MekaSuit x dano paranormal ----------

    /** A MekaSuit não segura o dano paranormal (estudo 2.3 e 2.4): fica abaixo da A4 (50%). */
    @Test
    void mekaSuitAbsorveMenosQueNossaArmaduraContraDanoParanormal() throws IOException {
        String arquivo = "mekanism/data_maps/damage_type/mekasuit_absorption.json";
        Map<String, Object> mapa = lerObjeto(DATA.resolve(arquivo));
        assertNaoApaga(mapa, arquivo);
        Map<String, Object> valores = objeto(mapa.get("values"));
        assertTrue(valores.containsKey("odteste:paranormal"), "falta o dano odteste:paranormal no data map");
        double absorcao = ((Number) objeto(valores.get("odteste:paranormal")).get("absorption")).doubleValue();
        assertTrue(absorcao >= 0 && absorcao < 0.5, "MekaSuit contra dano paranormal tem de ficar abaixo de 0.5 (A4)");
    }

    // ---------- Receitas 1:1 ----------

    /**
     * Nenhuma receita nossa devolve mais itens do que consome, nenhuma tem saída extra por chance, e nenhuma
     * devolve mais de um item nosso do que entrou desse mesmo item (matéria nunca se multiplica).
     */
    @Test
    void receitasNuncaMultiplicam() throws IOException {
        List<Path> receitas = receitas();
        assertFalse(receitas.isEmpty(), "não achei nenhuma receita (o teste ficaria vazio)");
        for (Path arquivo : receitas) {
            conferirReceita(lerObjeto(arquivo), arquivo.toString());
        }
    }

    /** O conferidor pega multiplicação e chance (garante que o teste acima não passa sempre). */
    @Test
    void conferidorDeReceitaPegaMultiplicacaoEChance() {
        Map<String, Object> dobra = objeto(new Leitor("""
                {"type":"x:moer","ingredients":[{"item":"odteste:materia_sangue_bruta"}],
                 "results":[{"id":"odteste:materia_sangue_bruta","count":2}]}""").valor());
        assertThrows(AssertionError.class, () -> conferirReceita(dobra, "dobra"));
        Map<String, Object> chance = objeto(new Leitor("""
                {"type":"x:moer","ingredient":{"item":"minecraft:bone"},
                 "results":[{"id":"minecraft:bone_meal"},{"id":"odteste:materia_sangue_bruta","chance":0.1}]}""").valor());
        assertThrows(AssertionError.class, () -> conferirReceita(chance, "chance"));
        Map<String, Object> mesa = objeto(new Leitor("""
                {"type":"minecraft:crafting_shaped","pattern":["AA","A "],"key":{"A":{"item":"odteste:materia_sangue_bruta"}},
                 "result":{"id":"odteste:analisador_sinal","count":1}}""").valor());
        conferirReceita(mesa, "mesa"); // 3 → 1: passa
        Map<String, Integer> entrada = new HashMap<>();
        assertEquals(3, contarEntrada(mesa, entrada));
        assertEquals(3, entrada.get("odteste:materia_sangue_bruta"));
    }

    // ---------- Sem tag de minério ----------

    @Test
    void nenhumItemNossoComTagDeMinerio() throws IOException {
        for (String ns : List.of("c", "forge")) {
            for (String tipo : List.of("item", "block")) {
                Path raiz = DATA.resolve(ns + "/tags/" + tipo);
                if (!Files.isDirectory(raiz)) continue;
                try (Stream<Path> s = Files.walk(raiz)) {
                    for (Path p : s.filter(f -> f.toString().endsWith(".json")).toList()) {
                        String primeira = raiz.relativize(p).getName(0).toString().replace(".json", "");
                        if (!TAGS_DE_MINERIO.contains(primeira)) continue;
                        assertFalse(Files.readString(p).contains("odteste:"),
                                p + ": item ou bloco nosso não entra em tag de minério (multiplicação por máquina)");
                    }
                }
            }
        }
    }

    // ---------- Sem modelo do HNN, abelha, árvore ou semente ----------

    @Test
    void semModeloDoHnnNemFonteInfinita() throws IOException {
        try (Stream<Path> s = Files.walk(DATA)) {
            for (Path p : s.toList()) {
                for (Path parte : DATA.relativize(p)) {
                    assertFalse(PASTAS_PROIBIDAS.contains(parte.toString()),
                            p + ": não criamos modelo do HNN, abelha, árvore nem semente para coisa nossa");
                }
                if (p.toString().endsWith(".json")) {
                    assertFalse(Files.readString(p).contains("hostilenetworks:"),
                            p + ": nada nosso pode citar o Hostile Neural Networks");
                }
            }
        }
    }

    // ---------- Contas da receita ----------

    private static void conferirReceita(Map<String, Object> r, String nome) {
        Map<String, Integer> entrada = new HashMap<>();
        int totalEntrada = contarEntrada(r, entrada);
        Map<String, Integer> saida = new HashMap<>();
        int totalSaida = contarSaida(r, saida, nome);
        if (totalEntrada > 0) {
            assertTrue(totalSaida <= totalEntrada, nome + ": sai " + totalSaida + " e entra " + totalEntrada + " (multiplica)");
        }
        for (var e : saida.entrySet()) {
            if (!e.getKey().startsWith("odteste:")) continue;
            int entrou = entrada.getOrDefault(e.getKey(), 0);
            if (entrou > 0) {
                assertTrue(e.getValue() <= entrou, nome + ": " + e.getKey() + " sai mais do que entra");
            }
        }
    }

    private static int contarEntrada(Map<String, Object> r, Map<String, Integer> porItem) {
        int total = 0;
        if (r.get("pattern") instanceof List<?> linhas && r.get("key") instanceof Map<?, ?> chave) {
            for (Object linha : linhas) {
                for (char c : ((String) linha).toCharArray()) {
                    if (c == ' ') continue;
                    total++;
                    somar(porItem, chave.get(String.valueOf(c)), 1);
                }
            }
        }
        for (String campo : List.of("ingredients", "inputs")) {
            if (r.get(campo) instanceof List<?> lista) {
                for (Object e : lista) {
                    int n = quantidade(e);
                    total += n;
                    somar(porItem, e, n);
                }
            }
        }
        for (String campo : List.of("ingredient", "input", "item_input", "activation_item")) {
            if (r.get(campo) instanceof Map<?, ?> e) {
                int n = quantidade(e);
                total += n;
                somar(porItem, e, n);
            }
        }
        return total;
    }

    private static int contarSaida(Map<String, Object> r, Map<String, Integer> porItem, String nome) {
        List<Object> saidas = new ArrayList<>();
        for (String campo : List.of("result", "results", "output", "outputs", "item_output")) {
            Object e = r.get(campo);
            if (e instanceof List<?> lista) saidas.addAll(lista);
            else if (e != null) saidas.add(e);
        }
        int total = 0;
        for (Object e : saidas) {
            if (!(e instanceof Map<?, ?> o)) continue;
            assertFalse(o.containsKey("chance"), nome + ": saída extra por chance não pode (estudo 3.2)");
            String id = idDe(o);
            if (id != null && id.startsWith("occultism:jei_dummy/")) continue; // ritual: só ícone, não sai item
            int n = quantidade(o);
            total += n;
            if (id != null) porItem.merge(id, n, Integer::sum);
        }
        return total;
    }

    private static int quantidade(Object e) {
        if (e instanceof Map<?, ?> o) {
            for (String campo : List.of("count", "amount")) {
                if (o.get(campo) instanceof Number n) return n.intValue();
            }
        }
        return 1;
    }

    private static void somar(Map<String, Integer> porItem, Object ingrediente, int n) {
        if (ingrediente instanceof Map<?, ?> o) {
            String id = idDe(o);
            if (id != null) porItem.merge(id, n, Integer::sum);
        }
    }

    private static String idDe(Map<?, ?> o) {
        for (String campo : List.of("id", "item")) {
            if (o.get(campo) instanceof String s) return s;
        }
        return null;
    }

    // ---------- Arquivos ----------

    private static List<Path> receitas() throws IOException {
        List<Path> lista = new ArrayList<>();
        try (Stream<Path> namespaces = Files.list(DATA)) {
            for (Path ns : namespaces.toList()) {
                Path pasta = ns.resolve("recipe");
                if (!Files.isDirectory(pasta)) continue;
                try (Stream<Path> s = Files.walk(pasta)) {
                    s.filter(p -> p.toString().endsWith(".json")).forEach(lista::add);
                }
            }
        }
        return lista;
    }

    private static List<String> blocosDoMod() throws IOException {
        Path pasta = Path.of("src/main/resources/assets/odteste/blockstates");
        try (Stream<Path> s = Files.list(pasta)) {
            return s.map(p -> "odteste:" + p.getFileName().toString().replace(".json", "")).sorted().toList();
        }
    }

    private static void assertNaoApaga(Map<String, Object> json, String arquivo) {
        assertFalse(Boolean.TRUE.equals(json.get("replace")),
                arquivo + " não pode ter \"replace\": true (apagaria a lista do outro mod)");
    }

    private static List<String> valoresDaTag(Map<String, Object> tag) {
        List<String> lista = new ArrayList<>();
        for (Object v : (List<?>) tag.get("values")) {
            lista.add(v instanceof Map<?, ?> o ? (String) o.get("id") : (String) v);
        }
        return lista;
    }

    private static Map<String, Object> lerObjeto(Path p) throws IOException {
        assertTrue(Files.exists(p), "falta o arquivo " + p);
        return objeto(new Leitor(Files.readString(p)).valor());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objeto(Object o) {
        assertTrue(o instanceof Map, "esperava um objeto JSON");
        return (Map<String, Object>) o;
    }

    /** Leitor de JSON mínimo: objeto → Map, lista → List, texto → String, número → Double, true/false/null. */
    static final class Leitor {
        private final String s;
        private int i;

        Leitor(String s) {
            this.s = s;
        }

        Object valor() {
            espacos();
            char c = s.charAt(i);
            if (c == '{') return objetoJson();
            if (c == '[') return listaJson();
            if (c == '"') return texto();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            int inicio = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (inicio == i) throw new IllegalArgumentException("JSON inválido na posição " + i);
            return Double.parseDouble(s.substring(inicio, i));
        }

        private Map<String, Object> objetoJson() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++;
            espacos();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                espacos();
                String chave = texto();
                espacos();
                esperar(':');
                m.put(chave, valor());
                espacos();
                if (s.charAt(i) == ',') { i++; continue; }
                esperar('}');
                return m;
            }
        }

        private List<Object> listaJson() {
            List<Object> l = new ArrayList<>();
            i++;
            espacos();
            if (s.charAt(i) == ']') { i++; return l; }
            while (true) {
                l.add(valor());
                espacos();
                if (s.charAt(i) == ',') { i++; continue; }
                esperar(']');
                return l;
            }
        }

        private String texto() {
            esperar('"');
            StringBuilder b = new StringBuilder();
            while (s.charAt(i) != '"') {
                char c = s.charAt(i++);
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n' -> b.append('\n');
                        case 't' -> b.append('\t');
                        case 'u' -> { b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }
                        default -> b.append(e);
                    }
                } else {
                    b.append(c);
                }
            }
            i++;
            return b.toString();
        }

        private void espacos() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        private void esperar(char c) {
            if (s.charAt(i) != c) throw new IllegalArgumentException("esperava '" + c + "' na posição " + i);
            i++;
        }
    }
}
