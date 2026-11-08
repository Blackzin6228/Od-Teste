package com.nikolas.odteste.dados;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.elementos.Elemento;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Nenhum valor do jogador pode sair da faixa, venha de comando, rito ou arquivo velho. */
class LimitesTest {

    @Test
    void exposicaoFicaEntre0e100() {
        assertEquals(0, Limites.exposicao(-5));
        assertEquals(0, Limites.exposicao(Integer.MIN_VALUE));
        assertEquals(42, Limites.exposicao(42));
        assertEquals(100, Limites.exposicao(100));
        assertEquals(100, Limites.exposicao(101));
        assertEquals(100, Limites.exposicao(Integer.MAX_VALUE));
    }

    @Test
    void lucidezMaximaTemPisoETeto() {
        assertEquals(Limites.LUCIDEZ_MAX_PISO, Limites.lucidezMax(0));
        assertEquals(Limites.LUCIDEZ_MAX_PISO, Limites.lucidezMax(-30));
        assertEquals(70, Limites.lucidezMax(70));
        assertEquals(Limites.LUCIDEZ_MAX_PADRAO, Limites.lucidezMax(500));
    }

    @Test
    void lucidezNuncaPassaDaMaxima() {
        assertEquals(70, Limites.lucidez(100, 70));
        assertEquals(0, Limites.lucidez(-1, 70));
        assertEquals(35, Limites.lucidez(35, 70));
        // Uma máxima inválida é corrigida antes de servir de teto.
        assertEquals(Limites.LUCIDEZ_MAX_PISO, Limites.lucidez(50, 0));
        assertEquals(Limites.LUCIDEZ_MAX_PADRAO, Limites.lucidez(150, 999));
    }

    @Test
    void transcenderVaiDe0a4() {
        assertEquals(0, Limites.transcender(-1));
        assertEquals(3, Limites.transcender(3));
        assertEquals(4, Limites.transcender(5));
    }

    @Test
    void semElementoNaoTemEstagio() {
        assertEquals(0, Limites.estagioMarca(Elemento.NENHUM, 3));
        assertEquals(0, Limites.estagioMarca(null, 3));
    }

    @Test
    void comElementoOEstagioVaiDe1a4() {
        assertEquals(1, Limites.estagioMarca(Elemento.SANGUE, 0));
        assertEquals(1, Limites.estagioMarca(Elemento.MORTE, -2));
        assertEquals(2, Limites.estagioMarca(Elemento.ENERGIA, 2));
        assertEquals(4, Limites.estagioMarca(Elemento.CONHECIMENTO, 9));
    }

    @Test
    void todoDadoSalvoEstaEmIdsMd() throws Exception {
        String ids = Files.readString(Path.of("docs/ids.md"));
        for (String id : IdsDados.TODOS) {
            assertTrue(ids.contains("`odteste:" + id + "`"), "falta odteste:" + id + " em docs/ids.md");
        }
        assertTrue(ids.contains("`odteste:" + IdsDados.PACOTE_SINCRONIZAR + "`"));
    }
}
