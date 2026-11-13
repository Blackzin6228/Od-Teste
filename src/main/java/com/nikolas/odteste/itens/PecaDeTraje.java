package com.nikolas.odteste.itens;

/**
 * Peça de armadura nossa com degrau (A1 a A5, docs/progressao.md). Os trajes ainda não existem: quando entrarem,
 * cada peça implementa isto e o combate (sistemas/Combate) aplica a absorção do degrau sozinho.
 */
public interface PecaDeTraje {
    /** Degrau da peça (1 a 5). */
    int grau();
}
