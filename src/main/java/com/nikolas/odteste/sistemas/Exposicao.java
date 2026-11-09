package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import net.minecraft.server.level.ServerPlayer;

/**
 * Ponto de entrada da Exposição: **contato com o paranormal**. Criaturas, relíquias, rituais, dimensões e
 * aparições chamam {@link #contato} quando tocam o jogador. Exemplos:
 *
 * <pre>
 * Exposicao.contato(jogador, Fonte.CRIATURA_VISTA, Elemento.SANGUE, 1f);   // viu uma criatura de Sangue
 * Exposicao.contato(jogador, Fonte.CRIATURA_MORTA, Elemento.SANGUE, 2.5f); // matou uma criatura forte
 * Exposicao.contato(jogador, Fonte.APARICAO, Elemento.NENHUM, 1f);         // aparição sem elemento
 * </pre>
 *
 * Com elemento, o ganho também soma no contato com aquele elemento (decide qual Marca aparece).
 * Só no servidor.
 */
public final class Exposicao {
    private Exposicao() {}

    public static int contato(ServerPlayer p, Fonte fonte, Elemento elemento) {
        return contato(p, fonte, elemento, 1f);
    }

    /**
     * @param intensidade 1 = normal; 0 a 5 (criatura forte, relíquia maior...). Fora da faixa é preso.
     * @return centésimos ganhos de fato (0 se a repetição já zerou o ganho).
     */
    public static int contato(ServerPlayer p, Fonte fonte, Elemento elemento, float intensidade) {
        if (p == null || fonte == null) return 0;
        Elemento e = elemento == null ? Elemento.NENHUM : elemento;
        // Tempo do mundo (salvo com o mundo): sair e voltar, ou reiniciar o servidor, não zera o freio.
        long agora = p.server.overworld().getGameTime();
        RegrasExposicao.Repeticao rep = RegrasExposicao.Repeticao.de(p.getData(DadosJogador.EXPOSICAO_REPETICAO));
        int antes = rep.registrar(fonte.id() + "/" + e.id(), agora);
        p.setData(DadosJogador.EXPOSICAO_REPETICAO, rep.paraSalvar(agora));
        int ganho = RegrasExposicao.ganhoCentesimos(fonte.base, intensidade, antes);
        if (ganho <= 0) return 0;
        aplicar(p, e, ganho);
        return ganho;
    }

    /**
     * Custo de um Artefato Paranormal: pontos inteiros de Exposição, **sem** o freio de repetição (o artefato já
     * tem recarga e cobra o dobro se for usado de novo cedo). Também para em 99. Com elemento, soma no contato.
     */
    public static void custo(ServerPlayer p, Elemento elemento, int pontos) {
        if (p == null || pontos <= 0) return;
        aplicar(p, elemento == null ? Elemento.NENHUM : elemento, pontos * 100);
    }

    private static void aplicar(ServerPlayer p, Elemento e, int ganho) {
        DadosJogador.somarContato(p, e, ganho);
        int atual = DadosJogador.exposicao(p);
        RegrasExposicao.Resultado r = RegrasExposicao.somar(atual, DadosJogador.exposicaoResto(p), ganho);
        DadosJogador.definirExposicaoComResto(p, r.exposicao(), r.resto());
        // Mesmo sem mudar de ponto, o contato novo pode fazer a Marca aparecer ou trocar.
        if (r.exposicao() == atual) Reacoes.conferirMarca(p);
    }
}
