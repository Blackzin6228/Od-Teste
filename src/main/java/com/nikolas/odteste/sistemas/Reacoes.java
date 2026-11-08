package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import net.minecraft.server.level.ServerPlayer;

/**
 * O que acontece quando um valor do jogador muda, venha de onde vier (ganho automático, rito ou comando de admin).
 * Chamado só no servidor, por DadosJogador. Assim o comando de admin também mostra os momentos (bom para teste).
 */
public final class Reacoes {
    private Reacoes() {}

    public static void exposicaoMudou(ServerPlayer p, int antes, int depois) {
        Degrau a = Degrau.de(antes);
        Degrau d = Degrau.de(depois);
        if (d.ordinal() > a.ordinal()) {
            Momentos.enviar(p, TipoMomento.DEGRAU, DadosJogador.marcaElemento(p), Elemento.NENHUM, d.ordinal());
        }
        conferirMarca(p);
    }

    public static void lucidezMudou(ServerPlayer p, int antes, int depois) {
        int max = DadosJogador.lucidezMax(p);
        if (FaixaLucidez.de(antes, max) != FaixaLucidez.de(depois, max)) Lucidez.atualizarLentidao(p);
        if (depois == 0 && antes > 0) Lucidez.surto(p);
    }

    public static void marcaMudou(ServerPlayer p, Elemento antes, int estagioAntes, Elemento depois, int estagioDepois) {
        if (depois == Elemento.NENHUM || (antes == depois && estagioDepois <= estagioAntes)) {
            if (depois == Elemento.NENHUM) MarcaSangue.limpar(p);
            return;
        }
        if (antes == Elemento.NENHUM) {
            Momentos.enviar(p, TipoMomento.MARCA_APARECEU, depois, Elemento.NENHUM, estagioDepois);
        } else if (antes != depois) {
            Momentos.enviar(p, TipoMomento.MARCA_TROCOU, depois, antes, estagioDepois);
        } else {
            Momentos.enviar(p, TipoMomento.MARCA_SUBIU, depois, Elemento.NENHUM, estagioDepois);
        }
        if (depois != Elemento.SANGUE) MarcaSangue.limpar(p);
    }

    /** A Marca aparece ou troca sozinha, pelo contato (o jogador nunca escolhe). */
    public static void conferirMarca(ServerPlayer p) {
        Elemento atual = DadosJogador.marcaElemento(p);
        if (atual == Elemento.NENHUM) {
            RegrasMarca.marcaQueAparece(DadosJogador.exposicao(p), DadosJogador.contato(p))
                    .ifPresent(e -> DadosJogador.definirMarca(p, e, 1));
        } else {
            RegrasMarca.trocaSozinha(atual, DadosJogador.marcaEstagio(p), DadosJogador.contato(p))
                    .ifPresent(e -> DadosJogador.definirMarca(p, e, 1));
        }
    }
}
