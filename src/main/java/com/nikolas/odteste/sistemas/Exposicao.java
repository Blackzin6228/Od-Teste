package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
    /** Freio de repetição por jogador. Só memória: limpo quando o jogador sai (Vigia). */
    private static final Map<UUID, RegrasExposicao.Repeticao> REPETICAO = new HashMap<>();

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
        int antes = REPETICAO.computeIfAbsent(p.getUUID(), u -> new RegrasExposicao.Repeticao())
                .registrar(fonte.id() + "/" + e.id(), p.server.getTickCount());
        int ganho = RegrasExposicao.ganhoCentesimos(fonte.base, intensidade, antes);
        if (ganho <= 0) return 0;
        DadosJogador.somarContato(p, e, ganho);
        int atual = DadosJogador.exposicao(p);
        RegrasExposicao.Resultado r = RegrasExposicao.somar(atual, DadosJogador.exposicaoResto(p), ganho);
        DadosJogador.definirExposicaoComResto(p, r.exposicao(), r.resto());
        // Mesmo sem mudar de ponto, o contato novo pode fazer a Marca aparecer ou trocar.
        if (r.exposicao() == atual) Reacoes.conferirMarca(p);
        return ganho;
    }

    static void esquecer(UUID jogador) {
        REPETICAO.remove(jogador);
    }
}
