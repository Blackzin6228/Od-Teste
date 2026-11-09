package com.nikolas.odteste.historia.gatilhos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nikolas.odteste.historia.gatilhos.RegrasGatilhos.Acao;
import com.nikolas.odteste.historia.gatilhos.RegrasGatilhos.Grupo;
import com.nikolas.odteste.historia.gatilhos.RegrasGatilhos.Momento;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegrasGatilhosTest {
    private static final long DIA = 6_000;
    private static final long NOITE = 18_000;

    private static Momento m(long hora, boolean chuva) {
        return new Momento(hora, chuva, true, true, true, 3);
    }

    /** Grupo sem evento, alguém online, pode tentar. */
    private static Grupo novo() {
        return new Grupo(false, false, true, true, true, true);
    }

    /** Grupo que já teve o evento, ainda não chegou à Vila Calada. */
    private static Grupo teve(boolean online, boolean admin) {
        return new Grupo(true, false, true, online, true, admin);
    }

    @Test
    void neblinaENoiteComChuva() {
        assertTrue(RegrasGatilhos.noiteDeNeblina(NOITE, true, true));
        assertFalse(RegrasGatilhos.noiteDeNeblina(NOITE, false, true), "noite limpa");
        assertFalse(RegrasGatilhos.noiteDeNeblina(DIA, true, true), "chuva de dia");
        assertTrue(RegrasGatilhos.noiteDeNeblina(NOITE, false, false), "sem exigir chuva, toda noite vale");
        assertEquals(3, RegrasGatilhos.dia(24_000L * 3 + 18_000));
        assertEquals(-1, RegrasGatilhos.dia(-5), "hora negativa não quebra");
    }

    @Test
    void semLivroSoltaNaPrimeiraNoiteDeNeblinaDepoisDeArmado() {
        EstadoGrupo st = new EstadoGrupo();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(DIA, false), novo()));
        assertTrue(st.armado, "visto fora da neblina: armado");
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE, false), novo()), "noite limpa não solta");
        assertEquals(Acao.SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos.passo(st, m(NOITE, true), novo()));
        st.soltou();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE + 24_000, true), novo()), "nunca solta duas vezes");
    }

    @Test
    void comLivroLidoSoltaIgual() {
        EstadoGrupo st = new EstadoGrupo();
        st.leuLivro();
        RegrasGatilhos.passo(st, m(DIA, false), novo());
        assertEquals(Acao.SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos.passo(st, m(NOITE, true), novo()));
    }

    @Test
    void grupoCriadoNaNeblinaEsperaAProxima() {
        EstadoGrupo st = new EstadoGrupo();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE, true), novo()), "não é \"depois\" de ativado");
        assertFalse(st.armado);
        RegrasGatilhos.passo(st, m(24_000 + DIA, false), novo());
        assertEquals(Acao.SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos.passo(st, m(24_000 + NOITE, true), novo()));
    }

    @Test
    void mundoQueJaExiste_grupoQueTeveNaoSolta_nemDepoisDoDesfazer() {
        EstadoGrupo st = new EstadoGrupo();
        st.armado = true;
        Grupo comMarco = new Grupo(true, false, false, true, true, true);
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE, true), comMarco));
        assertTrue(st.jaTeve);
        // O admin desfez: o marco saiu, mas o gatilho lembra.
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE, true), novo()));
    }

    @Test
    void desligadoPorConfigNaoSolta() {
        EstadoGrupo st = new EstadoGrupo();
        st.armado = true;
        Momento desligado = new Momento(NOITE, true, true, false, true, 3);
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, desligado, novo()));
        assertFalse(st.jaTeve, "ligar de novo depois ainda solta");
        assertEquals(Acao.SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos.passo(st, m(NOITE, true), novo()));
    }

    @Test
    void semNinguemOnlineOuSemVezNaoTenta_eUmaTentativaPorNoite() {
        EstadoGrupo st = new EstadoGrupo();
        st.armado = true;
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE, true), new Grupo(false, false, true, false, true, true)));
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE, true), new Grupo(false, false, true, true, false, true)));
        assertEquals(Acao.SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos.passo(st, m(NOITE, true), novo()));
        // Falhou (sem vila perto): não tenta de novo na mesma noite, tenta na próxima.
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE + 100, true), novo()));
        assertEquals(Acao.SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos.passo(st, m(24_000 + NOITE, true), novo()));
    }

    @Test
    void avisoDepoisDeNoitesComAlguemOnline() {
        EstadoGrupo st = new EstadoGrupo();
        // Noite 1 e 2 contam; a mesma noite não conta duas vezes; dia não conta; noite sem ninguém não conta.
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE, false), teve(true, true)));
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(NOITE + 2_000, false), teve(true, true)));
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(24_000 + DIA, false), teve(true, true)));
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(24_000 + NOITE, false), teve(false, true)));
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(48_000 + NOITE, false), teve(true, true)));
        assertEquals(2, st.noites);
        // Terceira noite: avisa (limite 3), mas só com admin online.
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(72_000 + NOITE, false), teve(true, false)));
        assertEquals(3, st.noites);
        assertEquals(Acao.AVISAR_ADMIN, RegrasGatilhos.passo(st, m(72_000 + DIA, false), teve(false, true)), "admin entrou de dia");
        st.avisou();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(st, m(96_000 + NOITE, false), teve(true, true)), "uma vez por grupo");
    }

    @Test
    void semAvisoSeChegouLeuOLivroOuNaoHaVila() {
        Momento noite1 = new Momento(NOITE, false, true, true, true, 1);
        EstadoGrupo chegou = new EstadoGrupo();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(chegou, noite1, new Grupo(true, true, true, true, true, true)));
        EstadoGrupo leu = new EstadoGrupo();
        leu.leuLivro();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(leu, noite1, teve(true, true)));
        EstadoGrupo semVila = new EstadoGrupo();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(semVila, noite1, new Grupo(true, false, false, true, true, true)));
        assertEquals(0, semVila.noites, "sem Vila Calada colocada, as noites não contam");
        EstadoGrupo desligado = new EstadoGrupo();
        assertEquals(Acao.NADA, RegrasGatilhos.passo(desligado, new Momento(NOITE, false, true, true, false, 1), teve(true, true)));
        EstadoGrupo normal = new EstadoGrupo();
        assertEquals(Acao.AVISAR_ADMIN, RegrasGatilhos.passo(normal, noite1, teve(true, true)));
    }

    @Test
    void primeiraCriacaoDoArquivo_gruposQueJaExistemNuncaSoltam_soOsNovos() {
        java.util.Map<String, EstadoGrupo> estados = new java.util.LinkedHashMap<>();
        RegrasGatilhos.primeiraCriacao(estados, List.of("antigo_a", "antigo_b"));
        assertTrue(estados.get("antigo_a").jaTeve);
        assertTrue(estados.get("antigo_b").jaTeve);
        // Antigo, sem o marco, numa noite de neblina com o automático ligado: nada.
        EstadoGrupo antigo = estados.get("antigo_a");
        RegrasGatilhos.passo(antigo, m(DIA, false), novo());
        assertEquals(Acao.NADA, RegrasGatilhos.passo(antigo, m(NOITE, true), novo()));
        // Grupo criado depois: automático.
        EstadoGrupo depois = estados.computeIfAbsent("novo", k -> new EstadoGrupo());
        RegrasGatilhos.passo(depois, m(DIA, false), novo());
        assertEquals(Acao.SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos.passo(depois, m(NOITE, true), novo()));
    }

    @Test
    void automaticoVemDesligadoPorPadrao() {
        assertFalse(ConfigGatilhos.PADRAO_AUTOMATICO, "neblina = chuva ainda não foi aprovada");
    }

    @Test
    void falhaAvisaOAdminUmaVezSo() {
        EstadoGrupo st = new EstadoGrupo();
        assertTrue(st.falhou());
        assertFalse(st.falhou(), "segunda noite de chuva sem vila: só no log");
        assertTrue(st.falhaAvisada);
    }

    @Test
    void limiteDaConfigFicaNaFaixa() {
        assertEquals(RegrasGatilhos.NOITES_AVISO_MIN, RegrasGatilhos.noitesAviso(0));
        assertEquals(RegrasGatilhos.NOITES_AVISO_MAX, RegrasGatilhos.noitesAviso(9_999));
        assertEquals(7, RegrasGatilhos.noitesAviso(7));
    }

    @Test
    void sujoSoQuandoMuda() {
        EstadoGrupo st = new EstadoGrupo();
        RegrasGatilhos.passo(st, m(DIA, false), novo());
        assertTrue(st.limparSujo());
        RegrasGatilhos.passo(st, m(DIA + 100, false), novo());
        assertFalse(st.limparSujo(), "nada mudou: não grava o arquivo à toa");
    }

    @Test
    void chavesSalvasSemRepetir() {
        List<String> todas = new java.util.ArrayList<>(IdsGatilhos.CHAVES_GRUPO);
        assertEquals(todas.size(), Set.copyOf(todas).size());
        assertEquals(IdsGatilhos.CHAVES_CONFIG.size(), Set.copyOf(IdsGatilhos.CHAVES_CONFIG).size());
    }
}
