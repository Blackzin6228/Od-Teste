package com.nikolas.odteste.historia.gatilhos;

/**
 * O que os gatilhos sabem de um grupo. Java puro: quem salva é {@link DadosGatilhos}, quem decide é
 * {@link RegrasGatilhos}. Só {@link #diaTentado} fica em memória.
 */
public final class EstadoGrupo {
    /** Sem noite contada ainda. */
    public static final long NENHUMA = Long.MIN_VALUE;

    /**
     * O grupo já teve o Primeiro Sinal (pelo admin ou sozinho). Uma vez true, nunca volta: assim o desfazer do admin
     * não faz o evento soltar sozinho de novo.
     */
    public boolean jaTeve;
    /** O mod já viu o grupo fora de uma noite de neblina: a próxima noite de neblina é "a primeira depois". */
    public boolean armado;
    /** Alguém do grupo abriu o Livro do Sineiro (então o grupo seguiu o sino: sem aviso ao admin). */
    public boolean livroLido;
    /** Noites (com alguém do grupo online) desde o Primeiro Sinal sem chegar à Vila Calada. */
    public int noites;
    /** Dia da última noite contada (para não contar a mesma noite duas vezes). */
    public long ultimaNoite = NENHUMA;
    /** O admin já recebeu "{grupo} não seguiu o sino". Uma vez por grupo. */
    public boolean avisado;

    /** Dia da última tentativa de soltar o evento (uma por noite). Só memória. */
    long diaTentado = NENHUMA;
    /** Mudou algo que precisa ir para o disco. Só memória. */
    boolean sujo;

    /** O evento soltou: nunca mais solta sozinho para este grupo. */
    public void soltou() {
        if (!jaTeve) sujo = true;
        jaTeve = true;
    }

    public void leuLivro() {
        if (!livroLido) sujo = true;
        livroLido = true;
    }

    public void avisou() {
        if (!avisado) sujo = true;
        avisado = true;
    }

    /** Lê e limpa o "mudou". */
    public boolean limparSujo() {
        boolean s = sujo;
        sujo = false;
        return s;
    }
}
