package com.nikolas.odteste.estruturas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EspacoTest {

    /** Mundo de mentira: chão de pedra em y=63, ar em cima, e blocos marcados à mão. */
    private static final class MundoFalso implements Espaco.Mundo, Espaco.Chao {
        final Map<String, Espaco.Tipo> blocos = new HashMap<>();
        int chunkProtegidoX = Integer.MIN_VALUE;
        int limiteCarregado = Integer.MAX_VALUE;

        void por(int x, int y, int z, Espaco.Tipo t) {
            blocos.put(x + "," + y + "," + z, t);
        }

        @Override
        public Espaco.Tipo tipo(int x, int y, int z) {
            if (Math.abs(x) > limiteCarregado || Math.abs(z) > limiteCarregado) return Espaco.Tipo.NAO_CARREGADO;
            if ((x >> 4) == chunkProtegidoX) return Espaco.Tipo.PROTEGIDO;
            Espaco.Tipo t = blocos.get(x + "," + y + "," + z);
            if (t != null) return t;
            return y <= 63 ? Espaco.Tipo.OCUPADO : Espaco.Tipo.AR;
        }

        @Override
        public int y(int x, int z) {
            return 64;
        }
    }

    private static Espaco.Caixa caixa3x3(int x, int y, int z) {
        return Espaco.Caixa.deTamanho(x, y, z, 3, 3, 3);
    }

    @Test
    void cabeNoArComChao() {
        MundoFalso m = new MundoFalso();
        assertTrue(Espaco.checarComChao(caixa3x3(0, 64, 0), m).cabe());
    }

    @Test
    void plantaPodeSerOcupada() {
        MundoFalso m = new MundoFalso();
        m.por(1, 64, 1, Espaco.Tipo.SUBSTITUIVEL);
        assertTrue(Espaco.checarComChao(caixa3x3(0, 64, 0), m).cabe());
    }

    @Test
    void nuncaPassaPorCimaDeConstrucao() {
        MundoFalso m = new MundoFalso();
        m.por(2, 65, 1, Espaco.Tipo.OCUPADO); // um bloco de jogador no meio da caixa
        Espaco.Resultado r = Espaco.checar(caixa3x3(0, 64, 0), m);
        assertFalse(r.cabe());
        assertEquals(Espaco.Tipo.OCUPADO, r.motivo());
        assertArrayEquals(new int[] {2, 65, 1}, new int[] {r.x(), r.y(), r.z()});
    }

    @Test
    void bauEMaquinaBarram() {
        MundoFalso m = new MundoFalso();
        m.por(0, 64, 0, Espaco.Tipo.COM_DADOS);
        assertEquals(Espaco.Tipo.COM_DADOS, Espaco.checar(caixa3x3(0, 64, 0), m).motivo());
    }

    @Test
    void aguaNaoEhChaoNemEspaco() {
        MundoFalso m = new MundoFalso();
        m.por(1, 64, 1, Espaco.Tipo.LIQUIDO);
        assertFalse(Espaco.checar(caixa3x3(0, 64, 0), m).cabe());
        assertFalse(Espaco.Tipo.LIQUIDO.chao);
    }

    @Test
    void claimBarra() {
        MundoFalso m = new MundoFalso();
        m.chunkProtegidoX = 0;
        Espaco.Resultado r = Espaco.checar(caixa3x3(0, 64, 0), m);
        assertFalse(r.cabe());
        assertEquals(Espaco.Tipo.PROTEGIDO, r.motivo());
    }

    @Test
    void chunkNaoCarregadoBarra() {
        MundoFalso m = new MundoFalso();
        m.limiteCarregado = 1;
        assertEquals(Espaco.Tipo.NAO_CARREGADO, Espaco.checar(caixa3x3(0, 64, 0), m).motivo());
    }

    @Test
    void naoFicaVoando() {
        MundoFalso m = new MundoFalso();
        // Caixa no ar, dois blocos acima do chão: não tem chão firme embaixo.
        assertFalse(Espaco.checarComChao(caixa3x3(0, 66, 0), m).cabe());
        // Com só um buraco no piso (8 de 9 firmes), ainda serve.
        m.por(1, 63, 1, Espaco.Tipo.AR);
        assertTrue(Espaco.checarComChao(caixa3x3(0, 64, 0), m).cabe());
    }

    @Test
    void chaoEmClaimBarraMesmoComCaixaLivre() {
        MundoFalso m = new MundoFalso();
        m.por(0, 63, 0, Espaco.Tipo.PROTEGIDO);
        assertEquals(Espaco.Tipo.PROTEGIDO, Espaco.checarComChao(caixa3x3(0, 64, 0), m).motivo());
    }

    @Test
    void procuraOutroPontoQuandoNaoCabe() {
        MundoFalso m = new MundoFalso();
        // Uma casa de jogador (bloco ocupado) bem no ponto pedido.
        for (int x = -2; x <= 4; x++) for (int z = -2; z <= 4; z++) m.por(x, 65, z, Espaco.Tipo.OCUPADO);
        Optional<int[]> p = Espaco.procurar(0, 0, 3, 3, 3, 16, 2, 400, m, m);
        assertTrue(p.isPresent());
        int[] c = p.get();
        assertTrue(Espaco.checarComChao(caixa3x3(c[0], c[1], c[2]), m).cabe());
        assertEquals(64, c[1]);
        // E o ponto original continua barrado.
        assertFalse(Espaco.checar(caixa3x3(0, 64, 0), m).cabe());
    }

    @Test
    void semLugarDevolveVazio() {
        MundoFalso m = new MundoFalso();
        m.limiteCarregado = 0; // quase nada carregado
        assertTrue(Espaco.procurar(5, 5, 3, 3, 3, 8, 2, 100, m, m).isEmpty());
    }

    /** Tudo ar (como em cima de uma ravina funda): a caixa sempre "cabe", mas nunca tem chão. Conta as leituras. */
    private static final class SemChao implements Espaco.Mundo, Espaco.Chao {
        long lidos;

        @Override
        public Espaco.Tipo tipo(int x, int y, int z) {
            lidos++;
            return Espaco.Tipo.AR;
        }

        @Override
        public int y(int x, int z) {
            return 64;
        }
    }

    @Test
    void semChaoNemLeACaixa() {
        SemChao m = new SemChao();
        // Caixa grande (16.384 blocos, piso 32x32): sem chão, para depois de ~30% do piso, sem ler a caixa.
        Espaco.Caixa c = Espaco.Caixa.deTamanho(0, 64, 0, 32, 16, 32);
        assertFalse(Espaco.checarComChao(c, m).cabe());
        assertTrue(m.lidos <= 32 * 32 * 0.3 + 2, "leu " + m.lidos);
    }

    @Test
    void buscaRespeitaOOrcamentoDeLeituras() {
        SemChao m = new SemChao();
        long orcamento = 5_000;
        assertTrue(Espaco.procurar(0, 0, 32, 16, 32, 32, 3, 400, orcamento, m, m).isEmpty());
        // Pode passar no máximo uma checagem (chão + caixa) além do orçamento.
        assertTrue(m.lidos <= orcamento + 32 * 32 + 32 * 16 * 32, "leu " + m.lidos);
    }

    @Test
    void caixaSabeOQueTemDentro() {
        Espaco.Caixa c = Espaco.Caixa.deTamanho(0, 0, 0, 2, 3, 4);
        assertTrue(c.contem(1, 2, 3));
        assertFalse(c.contem(1, 3, 3), "a metade de cima de uma planta logo acima fica fora");
        assertEquals(2, c.largura());
        assertEquals(3, c.altura());
        assertEquals(4, c.fundo());
    }

    @Test
    void candidatosDoMaisPertoParaOMaisLongeComLimite() {
        List<int[]> c = Espaco.candidatos(10, 10, 9, 4, 1000);
        assertArrayEquals(new int[] {10, 10}, c.get(0), "começa no próprio ponto");
        for (int i = 1; i < c.size(); i++) {
            int a = (c.get(i - 1)[0] - 10) * (c.get(i - 1)[0] - 10) + (c.get(i - 1)[1] - 10) * (c.get(i - 1)[1] - 10);
            int b = (c.get(i)[0] - 10) * (c.get(i)[0] - 10) + (c.get(i)[1] - 10) * (c.get(i)[1] - 10);
            assertTrue(a <= b);
        }
        assertEquals(5, Espaco.candidatos(0, 0, 32, 1, 5).size());
    }

    @Test
    void caixa() {
        Espaco.Caixa c = Espaco.Caixa.deTamanho(1, 2, 3, 2, 3, 4);
        assertEquals(24, c.volume());
        assertEquals(new Espaco.Caixa(2, 2, 3, 3, 4, 6), c.mover(1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Espaco.Caixa(1, 1, 1, 0, 1, 1));
    }
}
