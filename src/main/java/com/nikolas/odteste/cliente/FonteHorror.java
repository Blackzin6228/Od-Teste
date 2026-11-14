package com.nikolas.odteste.cliente;

/**
 * De onde o efeito {@code cor_horror} tira a força (0 a 1, já suave). Só cliente.
 *
 * <p>Enquanto a camada de horror (CamadaHorrorCliente, PR #77) não estiver no mesmo código, vale a fonte-padrão
 * do {@link FxCorHorror}: dia = 1 no overworld, noite pela hora do mundo, área = dentro da Vila Calada.
 * Quando os dois PRs entrarem, a camada liga a dela no {@code iniciar} com uma linha:
 * <pre>
 * FxCorHorror.usarFonte(new FonteHorror() {
 *     public float dia() { return forcaDia(); }
 *     public float noite() { return forcaNoite(); }
 *     public float area() { return area().isEmpty() ? 0f : 1f; }
 * });
 * </pre>
 */
interface FonteHorror {
    /** Quanto a camada vale aqui, de dia (overworld inteiro: forte). 0 = desligada (outra dimensão). */
    float dia();

    /** A parte da noite (mais pesada). */
    float noite();

    /** Dentro de uma área da história (mais pesada também de dia). */
    float area();
}
