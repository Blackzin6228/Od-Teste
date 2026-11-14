package com.nikolas.odteste.cenas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Lê uma cena em JSON ({@code docs/cenas.md}). Java puro (só Gson). O mesmo leitor roda no servidor (ao carregar os
 * arquivos) e no cliente (ao receber o pacote): os dois veem a mesma cena. Erro de formato vira
 * {@link IllegalArgumentException} com a frase do problema; regras de tempo e valores ficam em
 * {@link RegrasRoteiro#problemas}.
 */
public final class LeitorRoteiro {
    /** Tamanho máximo do texto da cena (o pacote leva o texto inteiro). */
    public static final int MAX_TEXTO = 60_000;

    public static final int PRETO = 0x000000;
    public static final int VERMELHO = 0x5A0006;

    private LeitorRoteiro() {}

    public static Roteiro ler(String id, String json) {
        if (json == null || json.length() > MAX_TEXTO) throw new IllegalArgumentException("texto vazio ou grande demais");
        JsonElement e;
        try {
            e = JsonParser.parseString(json);
        } catch (RuntimeException erro) {
            throw new IllegalArgumentException("JSON quebrado: " + erro.getMessage());
        }
        if (!e.isJsonObject()) throw new IllegalArgumentException("a cena precisa ser um objeto { }");
        return ler(id, e.getAsJsonObject());
    }

    public static Roteiro ler(String id, JsonObject o) {
        int duracao = inteiro(o, "duracao", -1);
        String ancora = texto(o, "ancora", "jogador");
        boolean pular = booleano(o, "pular", true);
        float barras = (float) numero(o, "barras", 0.11);
        String saida = texto(o, "saida", "preto");
        if (!saida.equals("preto") && !saida.equals("direta")) throw new IllegalArgumentException("saida: preto ou direta");
        boolean mostrar = booleano(o, "mostrar_jogador", false);

        List<Roteiro.Chave> chaves = new ArrayList<>();
        JsonArray ca = o.has("chaves") && o.get("chaves").isJsonArray() ? o.getAsJsonArray("chaves") : new JsonArray();
        for (JsonElement ce : ca) {
            if (!ce.isJsonObject()) throw new IllegalArgumentException("chave que não é objeto");
            chaves.add(chave(ce.getAsJsonObject()));
        }

        List<Roteiro.Evento> eventos = new ArrayList<>();
        JsonArray ea = o.has("eventos") && o.get("eventos").isJsonArray() ? o.getAsJsonArray("eventos") : new JsonArray();
        for (JsonElement ee : ea) {
            if (!ee.isJsonObject()) throw new IllegalArgumentException("evento que não é objeto");
            eventos.add(evento(ee.getAsJsonObject()));
        }
        // Ordem estável por tempo: dois eventos no mesmo tick ficam na ordem do arquivo.
        eventos.sort(Comparator.comparingInt(Roteiro.Evento::t));
        return new Roteiro(id, duracao, ancora, pular, barras, saida.equals("direta"), mostrar, chaves, eventos);
    }

    private static Roteiro.Chave chave(JsonObject o) {
        int t = inteiro(o, "t", -1);
        Roteiro.Referencia ref = referencia(texto(o, "em", "ancora"));
        double entre = numero(o, "entre", 0.5);
        Cena.Local pos = local(o, "pos");
        if (pos == null) throw new IllegalArgumentException("chave " + t + " sem pos [x, y, z]");

        Roteiro.Olhar olhar;
        if (o.has("olhar") && o.get("olhar").isJsonPrimitive()) {
            if (!"jogador".equals(o.get("olhar").getAsString())) {
                throw new IllegalArgumentException("chave " + t + ": olhar é [x, y, z] ou \"jogador\"");
            }
            olhar = new Roteiro.Olhar(null, null, 0, true, 0, 0);
        } else if (o.has("olhar")) {
            Cena.Local alvo = local(o, "olhar");
            if (alvo == null) throw new IllegalArgumentException("chave " + t + ": olhar é [x, y, z] ou \"jogador\"");
            Roteiro.Referencia refOlhar = referencia(texto(o, "olhar_em", ref.nome));
            olhar = new Roteiro.Olhar(alvo, refOlhar, numero(o, "olhar_entre", entre), false, 0, 0);
        } else {
            olhar = Roteiro.Olhar.angulos((float) numero(o, "giro", 0), (float) numero(o, "inclinacao", 0));
        }

        float fov = o.has("fov") ? (float) numero(o, "fov", 70) : Float.NaN;
        float roll = (float) numero(o, "roll", 0);
        Roteiro.Curva curva = Roteiro.Curva.de(texto(o, "curva", "catmull"));
        if (curva == null) throw new IllegalArgumentException("chave " + t + ": curva é catmull, linear ou corte");
        Roteiro.Ritmo ritmo = Roteiro.Ritmo.de(texto(o, "ritmo", "linear"));
        if (ritmo == null) throw new IllegalArgumentException("chave " + t + ": ritmo é linear, suave, entra ou sai");
        return new Roteiro.Chave(t, ref, entre, pos, olhar, fov, roll, curva, ritmo);
    }

    private static Roteiro.Evento evento(JsonObject o) {
        int t = inteiro(o, "t", -1);
        String tipo = texto(o, "tipo", "");
        return switch (tipo) {
            case "som" -> new Roteiro.Som(t, texto(o, "som", ""), (float) numero(o, "volume", 1), (float) numero(o, "tom", 1));
            case "fade" -> new Roteiro.Fade(t, o.has("cor") ? cor(texto(o, "cor", "preto")) : -1, (float) numero(o, "de", -1),
                    (float) numero(o, "ate", 0), inteiro(o, "duracao", 10));
            case "texto" -> new Roteiro.Texto(t, texto(o, "chave", ""), inteiro(o, "linha", 0), inteiro(o, "duracao", 60));
            case "tremor" -> new Roteiro.Tremor(t, (float) numero(o, "forca", 0.5), inteiro(o, "duracao", 20));
            case "fx" -> new Roteiro.Fx(t, texto(o, "efeito", ""), inteiro(o, "segundos", 0));
            case "parar_sons" -> new Roteiro.PararSons(t);
            default -> throw new IllegalArgumentException("evento no tick " + t + " com tipo desconhecido: " + tipo);
        };
    }

    /** {@code preto}, {@code vermelho} ou {@code #RRGGBB}. */
    public static int cor(String s) {
        String v = s.toLowerCase(Locale.ROOT).trim();
        if (v.equals("preto")) return PRETO;
        if (v.equals("vermelho")) return VERMELHO;
        if (v.matches("#[0-9a-f]{6}")) return Integer.parseInt(v.substring(1), 16);
        throw new IllegalArgumentException("cor: preto, vermelho ou #RRGGBB (veio " + s + ")");
    }

    private static Roteiro.Referencia referencia(String s) {
        Roteiro.Referencia r = Roteiro.Referencia.de(s);
        if (r == null) throw new IllegalArgumentException("em: ancora, jogador, entre ou olhos (veio " + s + ")");
        return r;
    }

    private static Cena.Local local(JsonObject o, String nome) {
        if (!o.has(nome) || !o.get(nome).isJsonArray()) return null;
        JsonArray a = o.getAsJsonArray(nome);
        if (a.size() != 3) return null;
        try {
            return new Cena.Local(a.get(0).getAsDouble(), a.get(1).getAsDouble(), a.get(2).getAsDouble());
        } catch (RuntimeException erro) {
            return null;
        }
    }

    private static String texto(JsonObject o, String nome, String padrao) {
        if (!o.has(nome)) return padrao;
        try {
            return o.get(nome).getAsString();
        } catch (RuntimeException erro) {
            throw new IllegalArgumentException(nome + " precisa ser texto");
        }
    }

    private static double numero(JsonObject o, String nome, double padrao) {
        if (!o.has(nome)) return padrao;
        try {
            double v = o.get(nome).getAsDouble();
            if (!Double.isFinite(v)) throw new IllegalArgumentException(nome + " não é número");
            return v;
        } catch (IllegalArgumentException erro) {
            throw erro;
        } catch (RuntimeException erro) {
            throw new IllegalArgumentException(nome + " precisa ser número");
        }
    }

    private static int inteiro(JsonObject o, String nome, int padrao) {
        return (int) Math.round(numero(o, nome, padrao));
    }

    private static boolean booleano(JsonObject o, String nome, boolean padrao) {
        if (!o.has(nome)) return padrao;
        try {
            return o.get(nome).getAsBoolean();
        } catch (RuntimeException erro) {
            throw new IllegalArgumentException(nome + " precisa ser true ou false");
        }
    }
}
