package com.nikolas.odteste.historia.livros;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Gancho: um jogador abriu um livro de história pela primeira vez (na mão ou no atril). Só no servidor,
 * postado no {@code NeoForge.EVENT_BUS} logo antes de o livro abrir. Uma vez por jogador por livro.
 *
 * <pre>
 * NeoForge.EVENT_BUS.addListener((LivroAbertoPrimeiraVez e) -> {
 *     if ("sineiro".equals(e.livro())) { ... tocar o sino ... }
 * });
 * </pre>
 */
public final class LivroAbertoPrimeiraVez extends Event {
    private final ServerPlayer jogador;
    private final String livro;

    public LivroAbertoPrimeiraVez(ServerPlayer jogador, String livro) {
        this.jogador = jogador;
        this.livro = livro;
    }

    public ServerPlayer jogador() {
        return jogador;
    }

    /** O id do livro (nome do arquivo em odteste_livros, ex.: "sineiro"). */
    public String livro() {
        return livro;
    }
}
