package com.nikolas.odteste.sistemas;

/**
 * Eventos de Energia na base (docs/tech-paranormal.md seção 5; estudo de balanceamento 4.3 e 4.4).
 * Decisão de 13/11/2026: **na 0.2 só Casa Ocupada e Apagão**, e só nos nossos aparelhos e na tela. A Fome da Linha
 * (dreno de baterias de outros mods) fica travada aqui até existir a lista permitida e a zona de 32 blocos longe de
 * reatores (estudo 4.1). Todo evento de Energia que entrar no código pergunta {@link #podeAcontecer} antes.
 * Java puro, testado em criaturas/RegrasMateriaTest. Nenhum evento existe ainda: esta é a trava.
 */
public enum EventoEnergia {
    /** Nível 1: nada desliga (sons, Rádio fala o nome, vultos). A partir de Exposição Média. */
    CASA_OCUPADA(1, 40, true),
    /** Nível 2: só nossos aparelhos (Analisador, Rádio, Antena) e luz piscando só na tela. A partir de Média. */
    APAGAO(2, 40, true),
    /** Nível 3: dreno de baterias de outros mods. Só com Exposição Alta, e **desligado na 0.2**. */
    FOME_DA_LINHA(3, 60, false);

    public final int nivel;
    /** Maior Exposição do grupo da base, mínima para o evento. */
    public final int exposicaoMinima;
    /** Liberado nesta versão. */
    public final boolean liberado;

    EventoEnergia(int nivel, int exposicaoMinima, boolean liberado) {
        this.nivel = nivel;
        this.exposicaoMinima = exposicaoMinima;
        this.liberado = liberado;
    }

    /** Pode acontecer numa base cujo jogador mais exposto tem esta Exposição? */
    public boolean podeAcontecer(int maiorExposicao) {
        return liberado && maiorExposicao >= exposicaoMinima;
    }

    /** Mexe em bloco de outro mod? Só a Fome da Linha (e ela está travada). */
    public boolean tocaOutrosMods() {
        return this == FOME_DA_LINHA;
    }
}
