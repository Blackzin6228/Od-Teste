package com.nikolas.odteste.sinais;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/**
 * Quem sabe ler o sinal guardado num item (livro do carniceiro, livro-caixa, gravação de evento...).
 * O jogador clica no Analisador com o item na mão. Registre com {@link Sinais#registrarLeitor(LeitorDeItem)}.
 * Chamado só no servidor, só quando alguém usa o Analisador.
 */
@FunctionalInterface
public interface LeitorDeItem {
    /** O sinal deste item, ou vazio se o item não é seu. Não altere o item aqui. */
    Optional<Sinal> ler(ItemStack item);
}
