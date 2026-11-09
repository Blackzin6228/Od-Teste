package com.nikolas.odteste.historia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MorseTest {

    @Test
    void ferroEhOCodigoDoRoteiro() {
        // lore/roteiro.md, Enigma 1: ..-.  .  .-.  .-.  ---
        assertEquals("..-. . .-. .-. ---", Morse.codificar("FERRO"));
        assertEquals("FERRO", Morse.decodificar("..-. . .-. .-. ---"));
    }

    @Test
    void idaEVoltaComFraseEAcento() {
        String codigo = Morse.codificar("Não entrem pela porta da frente");
        assertEquals("NAO ENTREM PELA PORTA DA FRENTE", Morse.decodificar(codigo));
        assertTrue(codigo.contains(" / "), "palavras separadas por barra");
    }

    @Test
    void limparTiraAcentoEPontuacao() {
        assertEquals("NAO E FOME", Morse.limpar("  Não é... fome!  "));
        assertEquals("", Morse.limpar(null));
    }

    @Test
    void codigoDesconhecidoViraInterrogacao() {
        assertEquals("?", Morse.decodificar("......."));
        assertEquals("", Morse.decodificar("  "));
    }

    @Test
    void tempoPadraoDoMorse() {
        // E = ponto (1). T = traço (3). "E T": 1 + 7 (palavra) + 3 = 11.
        Morse.Sequencia s = Morse.sequencia("E T");
        assertEquals(List.of(new Morse.Toque(0, false), new Morse.Toque(8, true)), s.toques());
        assertEquals(11, s.duracao());
        // "EE": ponto, espaço de letra (3), ponto = 5.
        Morse.Sequencia ee = Morse.sequencia("EE");
        assertEquals(4, ee.toques().get(1).inicio());
        assertEquals(5, ee.duracao());
    }

    @Test
    void ferroTemQuatorzeToquesE47Unidades() {
        Morse.Sequencia s = Morse.sequencia("FERRO");
        assertEquals(4 + 1 + 3 + 3 + 3, s.toques().size());
        assertEquals(47, s.duracao());
        long tracos = s.toques().stream().filter(Morse.Toque::traco).count();
        assertEquals(1 + 0 + 1 + 1 + 3, tracos);
        // Sempre em ordem e sem toque sobreposto.
        for (int i = 1; i < s.toques().size(); i++) {
            Morse.Toque a = s.toques().get(i - 1);
            Morse.Toque b = s.toques().get(i);
            assertTrue(b.inicio() >= a.inicio() + (a.traco() ? Morse.TRACO : Morse.PONTO) + Morse.ESPACO_SINAL);
        }
    }

    @Test
    void sequenciaVazia() {
        Morse.Sequencia s = Morse.sequencia("  ");
        assertTrue(s.toques().isEmpty());
        assertEquals(0, s.duracao());
        assertFalse(Morse.codificar("").contains("null"));
    }
}
