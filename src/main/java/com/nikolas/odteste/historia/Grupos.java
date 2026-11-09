package com.nikolas.odteste.historia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Os grupos de jogadores e o progresso da história de cada um. O progresso é do **grupo**, não do jogador:
 * quem entra no grupo depois já chega com o que o grupo descobriu. Um jogador fica em um grupo só.
 * Java puro (testado em GruposTest); quem salva no mundo é {@link DadosHistoria}.
 */
public final class Grupos {
    private static final Pattern NOME = Pattern.compile("[a-z0-9_]{1,24}");

    public static final class Grupo {
        private final String nome;
        /** UUID → último nome conhecido (para o {grupo} dos textos, mesmo com o jogador fora). */
        private final Map<UUID, String> membros = new LinkedHashMap<>();
        /** Marcos da história já cumpridos, na ordem em que aconteceram. */
        private final Set<String> marcos = new LinkedHashSet<>();

        Grupo(String nome) {
            this.nome = nome;
        }

        public String nome() {
            return nome;
        }

        public Map<UUID, String> membros() {
            return Collections.unmodifiableMap(membros);
        }

        public List<String> nomes() {
            return new ArrayList<>(membros.values());
        }

        public Set<String> marcos() {
            return Collections.unmodifiableSet(marcos);
        }

        public boolean tem(String marco) {
            return marcos.contains(marco);
        }
    }

    private final Map<String, Grupo> grupos = new LinkedHashMap<>();

    /** Nome de grupo: letras minúsculas, números e "_", até 24. Maiúsculas viram minúsculas. */
    public static Optional<String> normalizar(String nome) {
        if (nome == null) return Optional.empty();
        String n = nome.trim().toLowerCase(Locale.ROOT);
        return NOME.matcher(n).matches() ? Optional.of(n) : Optional.empty();
    }

    public boolean criar(String nome) {
        Optional<String> n = normalizar(nome);
        if (n.isEmpty() || grupos.containsKey(n.get())) return false;
        grupos.put(n.get(), new Grupo(n.get()));
        return true;
    }

    public boolean apagar(String nome) {
        return normalizar(nome).map(grupos::remove).isPresent();
    }

    public Optional<Grupo> grupo(String nome) {
        return normalizar(nome).map(grupos::get);
    }

    public Collection<Grupo> todos() {
        return Collections.unmodifiableCollection(grupos.values());
    }

    public Optional<Grupo> grupoDe(UUID jogador) {
        for (Grupo g : grupos.values()) {
            if (g.membros.containsKey(jogador)) return Optional.of(g);
        }
        return Optional.empty();
    }

    /**
     * Põe o jogador no grupo. Se ele estava em outro, sai de lá (um grupo só).
     * @return o nome do grupo antigo, se ele trocou; vazio se não estava em nenhum ou o grupo não existe
     */
    public Optional<String> adicionar(String nome, UUID jogador, String nomeJogador) {
        Optional<Grupo> g = grupo(nome);
        if (g.isEmpty() || jogador == null) return Optional.empty();
        Optional<String> antigo = Optional.empty();
        for (Grupo outro : grupos.values()) {
            if (outro != g.get() && outro.membros.remove(jogador) != null) antigo = Optional.of(outro.nome);
        }
        g.get().membros.put(jogador, nomeJogador == null ? "?" : nomeJogador);
        return antigo;
    }

    public boolean tirar(String nome, UUID jogador) {
        return grupo(nome).map(g -> g.membros.remove(jogador) != null).orElse(false);
    }

    /** Atualiza o nome mostrado do jogador (ao entrar no servidor). */
    public void atualizarNome(UUID jogador, String nomeJogador) {
        grupoDe(jogador).ifPresent(g -> g.membros.put(jogador, nomeJogador));
    }

    /** @return true se o marco é novo para o grupo */
    public boolean marcar(String nome, String marco) {
        return grupo(nome).map(g -> g.marcos.add(marco)).orElse(false);
    }

    public boolean desmarcar(String nome, String marco) {
        return grupo(nome).map(g -> g.marcos.remove(marco)).orElse(false);
    }

    public boolean tem(String nome, String marco) {
        return grupo(nome).map(g -> g.tem(marco)).orElse(false);
    }

    /** Usado só ao carregar do mundo: devolve o grupo sem checar regras de nome de jogador. */
    Grupo carregar(String nome) {
        return grupos.computeIfAbsent(nome, Grupo::new);
    }

    static void carregarMembro(Grupo g, UUID jogador, String nomeJogador) {
        g.membros.put(jogador, nomeJogador);
    }

    static void carregarMarco(Grupo g, String marco) {
        g.marcos.add(marco);
    }
}
