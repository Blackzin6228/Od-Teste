package com.nikolas.odteste.sinais;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.sinais.RegrasSinal.Direcao;
import com.nikolas.odteste.sinais.RegrasSinal.Intensidade;
import org.junit.jupiter.api.Test;

/** Direção e força das fontes de sinal fixas (Java puro). */
class RegrasSinalTest {
    // yaw do Minecraft: 0 = sul (+z), 90 = oeste (-x), 180 = norte (-z), -90 = leste (+x)
    private static final float SUL = 0, OESTE = 90, NORTE = 180, LESTE = -90;

    @Test
    void olhandoParaONorte() {
        assertEquals(Direcao.FRENTE, RegrasSinal.direcao(NORTE, 0, -100));
        assertEquals(Direcao.DIREITA, RegrasSinal.direcao(NORTE, 100, 0), "leste fica à direita de quem olha o norte");
        assertEquals(Direcao.ESQUERDA, RegrasSinal.direcao(NORTE, -100, 0));
        assertEquals(Direcao.TRAS, RegrasSinal.direcao(NORTE, 0, 100));
        assertEquals(Direcao.FRENTE_DIREITA, RegrasSinal.direcao(NORTE, 50, -50));
        assertEquals(Direcao.TRAS_ESQUERDA, RegrasSinal.direcao(NORTE, -50, 50));
    }

    @Test
    void giraJuntoComOJogador() {
        // sinal fixo a leste (+x): muda de lado conforme o jogador vira
        assertEquals(Direcao.FRENTE, RegrasSinal.direcao(LESTE, 100, 0));
        assertEquals(Direcao.ESQUERDA, RegrasSinal.direcao(SUL, 100, 0), "olhando o sul, o leste fica à esquerda");
        assertEquals(Direcao.TRAS, RegrasSinal.direcao(OESTE, 100, 0));
        assertEquals(Direcao.DIREITA, RegrasSinal.direcao(NORTE, 100, 0));
    }

    @Test
    void yawForaDaFaixaDaMesmaResposta() {
        // o Minecraft deixa o yaw passar de 360 ao girar muito
        assertEquals(RegrasSinal.direcao(NORTE, 30, -70), RegrasSinal.direcao(NORTE + 720, 30, -70));
        assertEquals(RegrasSinal.direcao(LESTE, 30, -70), RegrasSinal.direcao(LESTE + 360, 30, -70));
    }

    @Test
    void cadaSetorTem45Graus() {
        // 20 graus para a direita ainda é frente; 25 já é frente-direita
        double r20 = Math.toRadians(20), r25 = Math.toRadians(25);
        assertEquals(Direcao.FRENTE, RegrasSinal.direcao(NORTE, Math.sin(r20) * 100, -Math.cos(r20) * 100));
        assertEquals(Direcao.FRENTE_DIREITA, RegrasSinal.direcao(NORTE, Math.sin(r25) * 100, -Math.cos(r25) * 100));
    }

    @Test
    void forcaCaiComADistancia() {
        assertEquals(Intensidade.FORTE, RegrasSinal.intensidade(0, 160));
        assertEquals(Intensidade.FORTE, RegrasSinal.intensidade(40, 160));
        assertEquals(Intensidade.MEDIA, RegrasSinal.intensidade(41, 160));
        assertEquals(Intensidade.MEDIA, RegrasSinal.intensidade(96, 160));
        assertEquals(Intensidade.FRACA, RegrasSinal.intensidade(97, 160));
        assertEquals(Intensidade.FRACA, RegrasSinal.intensidade(160, 160));
        assertEquals(Intensidade.NENHUMA, RegrasSinal.intensidade(161, 160), "fora do alcance não capta");
        assertEquals(Intensidade.NENHUMA, RegrasSinal.intensidade(10, 0));
    }

    @Test
    void volumeSobeComAForca() {
        assertEquals(0f, RegrasSinal.volume(Intensidade.NENHUMA));
        assertTrue(RegrasSinal.volume(Intensidade.FRACA) < RegrasSinal.volume(Intensidade.MEDIA));
        assertTrue(RegrasSinal.volume(Intensidade.MEDIA) < RegrasSinal.volume(Intensidade.FORTE));
        assertTrue(RegrasSinal.volume(Intensidade.FORTE) <= 1f);
    }

    @Test
    void nomesDeFonte() {
        assertTrue(RegrasSinal.nomeValido("sino_vila_calada"));
        assertTrue(RegrasSinal.nomeValido("e2"));
        assertFalse(RegrasSinal.nomeValido(""));
        assertFalse(RegrasSinal.nomeValido(null));
        assertFalse(RegrasSinal.nomeValido("Sino"));
        assertFalse(RegrasSinal.nomeValido("sino-vila"));
        assertFalse(RegrasSinal.nomeValido("a".repeat(33)));
    }

    @Test
    void chavesDeTexto() {
        assertEquals("odteste.analisador.direcao.frente_direita", Direcao.FRENTE_DIREITA.chave());
        assertEquals("odteste.analisador.longe.forte", Intensidade.FORTE.chave());
    }
}
