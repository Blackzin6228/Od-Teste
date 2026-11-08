package com.nikolas.odteste.elementos;

import java.util.Locale;
import java.util.Optional;

/**
 * Os elementos do Outro Lado que podem marcar um jogador.
 * O Medo fica de fora de propósito: é força de fundo, não Marca (pendente em docs/decisoes.md).
 *
 * Java puro (sem Minecraft) para poder ser testado no JUnit.
 * O {@link #id()} é salvo no jogador: depois do lançamento, nunca muda.
 */
public enum Elemento {
    NENHUM("nenhum", 0x808080),
    SANGUE("sangue", 0x8B0000),
    CONHECIMENTO("conhecimento", 0xD9C7A0),
    MORTE("morte", 0x1A1A1A),
    ENERGIA("energia", 0x6A00FF);

    private final String id;
    private final int cor;

    Elemento(String id, int cor) {
        this.id = id;
        this.cor = cor;
    }

    /** Nome salvo no disco e usado nos comandos. */
    public String id() {
        return id;
    }

    /** Cor principal (RGB) para partículas e textos. */
    public int cor() {
        return cor;
    }

    /** Chave de tradução do nome do elemento. */
    public String chaveTraducao() {
        return "odteste.elemento." + id;
    }

    /**
     * Ciclo de vantagem: Sangue vence Conhecimento, que vence Energia, que vence Morte, que vence Sangue.
     * NENHUM não vence nem perde.
     */
    public Elemento vence() {
        return switch (this) {
            case SANGUE -> CONHECIMENTO;
            case CONHECIMENTO -> ENERGIA;
            case ENERGIA -> MORTE;
            case MORTE -> SANGUE;
            case NENHUM -> NENHUM;
        };
    }

    public boolean venceDe(Elemento outro) {
        return this != NENHUM && vence() == outro;
    }

    /** Lê o id (sem diferenciar maiúscula). Vazio se não existir. */
    public static Optional<Elemento> deId(String id) {
        if (id == null) return Optional.empty();
        String limpo = id.trim().toLowerCase(Locale.ROOT);
        for (Elemento e : values()) {
            if (e.id.equals(limpo)) return Optional.of(e);
        }
        return Optional.empty();
    }

    /** Para dados salvos: um id desconhecido (arquivo velho ou corrompido) vira NENHUM em vez de crashar. */
    public static Elemento deIdOuNenhum(String id) {
        return deId(id).orElse(NENHUM);
    }
}
