/**
 * Camada de horror (decisão de 14/11/2026): à noite e nas áreas da história, a noite fica bem escura (luz travada),
 * a música para, e de vez em quando vem um susto grande. As contas ficam em Regras* (Java puro, testadas no JUnit);
 * o servidor diz ao cliente em que área ele está e quando um susto começa. Tudo o que se vê e ouve é do cliente
 * ({@code cliente/CamadaHorrorCliente}, {@code cliente/SustoCliente}, {@code cliente/FalhaMinimapa}).
 *
 * Para registrar uma área nova da história: {@link com.nikolas.odteste.horror.CamadaHorror#registrarArea}.
 */
package com.nikolas.odteste.horror;
