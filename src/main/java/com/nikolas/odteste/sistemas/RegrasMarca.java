package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.elementos.Elemento;
import java.util.Map;
import java.util.Optional;

/**
 * Quando a Marca aparece e quando ela troca sozinha (docs/sistemas.md, seção 3).
 * O jogador não escolhe: decide o contato com cada elemento, contado em segredo.
 * Java puro, testado em RegrasTest.
 */
public final class RegrasMarca {
    /**
     * Para trocar sozinha, o outro elemento precisa de pelo menos isto de contato (centésimos),
     * além do dobro do atual. Protege a Marca dada pelo narrador (que começa com contato 0)
     * de trocar no primeiro toque de outro elemento.
     */
    public static final int CONTATO_MINIMO_TROCA = 300;

    private RegrasMarca() {}

    private static int contato(Map<Elemento, Integer> contato, Elemento e) {
        Integer v = contato.get(e);
        return v == null ? 0 : Math.max(0, v);
    }

    /** O elemento com mais contato; vazio se ninguém tem contato ou se há empate no topo. */
    public static Optional<Elemento> lider(Map<Elemento, Integer> contato, Elemento ignorar) {
        Elemento melhor = null;
        int maior = 0;
        boolean empate = false;
        for (Elemento e : Elemento.values()) {
            if (e == Elemento.NENHUM || e == ignorar) continue;
            int v = contato(contato, e);
            if (v > maior) {
                maior = v;
                melhor = e;
                empate = false;
            } else if (v == maior && v > 0) {
                empate = true;
            }
        }
        return empate ? Optional.empty() : Optional.ofNullable(melhor);
    }

    /** Sem Marca e já Tocado: o elemento com mais contato marca (estágio 1). Empate espera o próximo contato. */
    public static Optional<Elemento> marcaQueAparece(int exposicao, Map<Elemento, Integer> contato) {
        return marcaQueAparece(exposicao, contato, Elemento.NENHUM, false);
    }

    /**
     * Com o elemento do jogador definido pelo admin, a Marca só aparece nesse elemento, e só depois de revelado
     * (antes disso o corpo entregaria o segredo). O contato continua contando escondido.
     * Sem elemento: a regra de sempre (o contato decide).
     */
    public static Optional<Elemento> marcaQueAparece(int exposicao, Map<Elemento, Integer> contato,
                                                     Elemento doJogador, boolean revelado) {
        if (!Degrau.de(exposicao).peloMenos(Degrau.TOCADO)) return Optional.empty();
        if (doJogador == null || doJogador == Elemento.NENHUM) return lider(contato, null);
        return revelado ? Optional.of(doJogador) : Optional.empty();
    }

    /**
     * Com o elemento do jogador definido, a Marca nunca troca para outro elemento. Uma Marca antiga de outro elemento
     * (de antes do admin definir) volta para o elemento dele, se ainda estiver no estágio 1 e ele já foi revelado.
     */
    public static Optional<Elemento> trocaSozinha(Elemento atual, int estagio, Map<Elemento, Integer> contato,
                                                  Elemento doJogador, boolean revelado) {
        if (doJogador == null || doJogador == Elemento.NENHUM) return trocaSozinha(atual, estagio, contato);
        if (atual == null || atual == Elemento.NENHUM || atual == doJogador) return Optional.empty();
        return revelado && estagio == 1 ? Optional.of(doJogador) : Optional.empty();
    }

    /**
     * Antes do Vínculo (só estágio 1), a Marca troca sozinha se outro elemento tiver o dobro do contato
     * do atual (e pelo menos {@link #CONTATO_MINIMO_TROCA}).
     */
    public static Optional<Elemento> trocaSozinha(Elemento atual, int estagio, Map<Elemento, Integer> contato) {
        if (atual == null || atual == Elemento.NENHUM || estagio != 1) return Optional.empty();
        int doAtual = contato(contato, atual);
        return lider(contato, atual).filter(outro -> {
            int v = contato(contato, outro);
            return v >= CONTATO_MINIMO_TROCA && v >= 2L * doAtual;
        });
    }
}
