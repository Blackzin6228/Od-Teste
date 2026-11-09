package com.nikolas.odteste.elementos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.efeitos.RegrasSangramento;
import com.nikolas.odteste.sistemas.RegrasMarca;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RegrasElementoTest {

    @Test
    void soOProprioElementoEAfim() {
        assertTrue(RegrasElemento.afim(Elemento.SANGUE, Elemento.SANGUE));
        assertFalse(RegrasElemento.afim(Elemento.MORTE, Elemento.SANGUE));
        assertFalse(RegrasElemento.afim(Elemento.NENHUM, Elemento.NENHUM));
        assertFalse(RegrasElemento.afim(null, Elemento.SANGUE));
    }

    @Test
    void custoCai30PorCentoSoNoProprioElemento() {
        assertEquals(7, RegrasElemento.custo(10, Elemento.SANGUE, Elemento.SANGUE));
        assertEquals(18, RegrasElemento.custo(25, Elemento.SANGUE, Elemento.SANGUE)); // 17,5 arredonda para 18
        assertEquals(14, RegrasElemento.custo(20, Elemento.SANGUE, Elemento.SANGUE));
        assertEquals(10, RegrasElemento.custo(10, Elemento.ENERGIA, Elemento.SANGUE));
        assertEquals(10, RegrasElemento.custo(10, Elemento.NENHUM, Elemento.SANGUE));
    }

    @Test
    void custoQueExistiaNuncaZera() {
        assertEquals(1, RegrasElemento.custo(1, Elemento.SANGUE, Elemento.SANGUE));
        assertEquals(0, RegrasElemento.custo(0, Elemento.SANGUE, Elemento.SANGUE));
        assertEquals(-3, RegrasElemento.custo(-3, Elemento.SANGUE, Elemento.SANGUE));
    }

    @Test
    void custoDeVida() {
        assertEquals(4.2f, RegrasElemento.custoVida(6f, Elemento.SANGUE, Elemento.SANGUE), 1e-4);
        assertEquals(6f, RegrasElemento.custoVida(6f, Elemento.MORTE, Elemento.SANGUE), 1e-6);
        assertTrue(Float.isNaN(RegrasElemento.custoVida(Float.NaN, Elemento.SANGUE, Elemento.SANGUE)));
    }

    @Test
    void rubraAcordaMaisCedoNoDonoDeSangue() {
        float limiar = RegrasElemento.limiarVida(RegrasSangramento.RUBRA_LIMIAR_VIDA, Elemento.SANGUE, Elemento.SANGUE);
        assertEquals(0.79f, limiar, 1e-4);
        assertEquals(0.7f, RegrasElemento.limiarVida(0.7f, Elemento.MORTE, Elemento.SANGUE), 1e-6);
        // 15 de 20 (75%): só o dono de Sangue já acorda a Rubra.
        assertTrue(RegrasSangramento.rubraForte(15f, 20f, limiar));
        assertFalse(RegrasSangramento.rubraForte(15f, 20f));
    }

    @Test
    void marcadoresDosEventos() {
        assertEquals("escolhido_sangue", RegrasElemento.marcador(Elemento.SANGUE));
        assertEquals("escolhido_conhecimento", RegrasElemento.marcador(Elemento.CONHECIMENTO));
        assertThrows(IllegalArgumentException.class, () -> RegrasElemento.marcador(Elemento.NENHUM));
    }

    private static Map<Elemento, Integer> contato(Elemento e, int v) {
        Map<Elemento, Integer> m = new EnumMap<>(Elemento.class);
        m.put(e, v);
        return m;
    }

    @Test
    void semElementoAMarcaSegueOContato() {
        assertEquals(Optional.of(Elemento.MORTE),
                RegrasMarca.marcaQueAparece(20, contato(Elemento.MORTE, 50), Elemento.NENHUM, false));
    }

    @Test
    void comElementoAMarcaEsperaARevelacao() {
        Map<Elemento, Integer> c = contato(Elemento.MORTE, 500);
        assertEquals(Optional.empty(), RegrasMarca.marcaQueAparece(60, c, Elemento.SANGUE, false));
        assertEquals(Optional.of(Elemento.SANGUE), RegrasMarca.marcaQueAparece(20, c, Elemento.SANGUE, true));
        // Ainda não Tocado: nada, mesmo revelado.
        assertEquals(Optional.empty(), RegrasMarca.marcaQueAparece(19, c, Elemento.SANGUE, true));
    }

    @Test
    void comElementoAMarcaNuncaTrocaParaOutro() {
        Map<Elemento, Integer> muitoMorte = contato(Elemento.MORTE, 10_000);
        assertEquals(Optional.empty(),
                RegrasMarca.trocaSozinha(Elemento.SANGUE, 1, muitoMorte, Elemento.SANGUE, true));
        // Marca antiga de outro elemento volta para o do jogador (estágio 1, já revelado).
        assertEquals(Optional.of(Elemento.SANGUE),
                RegrasMarca.trocaSozinha(Elemento.MORTE, 1, muitoMorte, Elemento.SANGUE, true));
        assertEquals(Optional.empty(),
                RegrasMarca.trocaSozinha(Elemento.MORTE, 1, muitoMorte, Elemento.SANGUE, false));
        assertEquals(Optional.empty(),
                RegrasMarca.trocaSozinha(Elemento.MORTE, 2, muitoMorte, Elemento.SANGUE, true));
        // Sem elemento: a troca de sempre.
        assertEquals(Optional.of(Elemento.MORTE),
                RegrasMarca.trocaSozinha(Elemento.SANGUE, 1, muitoMorte, Elemento.NENHUM, false));
    }

    /** O elemento é segredo: nada do código manda ele para o cliente. */
    @Test
    void elementoNaoVaiParaOCliente() throws Exception {
        String rede = Files.readString(Path.of("src/main/java/com/nikolas/odteste/rede/SincronizarJogador.java"))
                + Files.readString(Path.of("src/main/java/com/nikolas/odteste/rede/MarcaVisivel.java"));
        assertFalse(rede.contains("DadosJogador.elemento(") || rede.contains("ELEMENTO_REVELADO") || rede.contains("ElementoJogador"),
                "o elemento do jogador não pode ir na rede");
        String cliente = String.join("\n", Files.walk(Path.of("src/main/java/com/nikolas/odteste/cliente"))
                .filter(Files::isRegularFile).map(p -> {
                    try {
                        return Files.readString(p);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).toList());
        assertFalse(cliente.contains("ElementoJogador") || cliente.contains("DadosJogador.elemento"),
                "o cliente não pode ler o elemento do jogador");
    }
}
