package com.nikolas.odteste.historia.gatilhos;

import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;
import java.util.Collection;
import java.util.Map;

/**
 * Regras dos gatilhos do Ato 1 (Java puro, testado em RegrasGatilhosTest):
 * <ul>
 * <li><b>2.2</b> O Primeiro Sinal solta sozinho na primeira noite de neblina para um grupo que ainda não o teve,
 * com ou sem o Livro do Sineiro lido (lore/roteiro.md, "Gatilho").</li>
 * <li><b>1.11</b> Grupo que teve o Primeiro Sinal e passa {@code noitesAviso} noites sem chegar à Vila Calada nem
 * abrir o Livro do Sineiro: aviso só para admins, "{grupo} não seguiu o sino" (lore/roteiro.md, "Atalho tech").</li>
 * </ul>
 * "Noite de neblina": o vanilla não tem neblina. Até o Nikolas decidir outra coisa, é <b>noite com chuva</b> no
 * overworld (config {@code neblina_precisa_chuva}; desligada, qualquer noite vale).
 */
public final class RegrasGatilhos {
    /** De quanto em quanto tempo os gatilhos olham os grupos (5 s). Grupos são poucos: conta leve. */
    public static final int INTERVALO_TICKS = 100;
    public static final long TICKS_DIA = 24_000L;
    /** Noites sem seguir o sino até avisar o admin (padrão da config). */
    public static final int NOITES_AVISO_PADRAO = 5;
    public static final int NOITES_AVISO_MIN = 1;
    public static final int NOITES_AVISO_MAX = 100;

    /** O que fazer com o grupo agora. */
    public enum Acao { NADA, SOLTAR_PRIMEIRO_SINAL, AVISAR_ADMIN }

    /**
     * O mundo e a config agora.
     * @param hora hora do dia do overworld ({@code getDayTime})
     */
    public record Momento(long hora, boolean chuva, boolean precisaChuva, boolean automatico,
                          boolean avisoLigado, int noitesAviso) {}

    /**
     * O grupo agora.
     * @param teveEvento tem o marco {@code primeiro_sinal} ou uma vila marcada
     * @param chegou tem o marco {@code vila_calada}
     * @param temVilaCalada a âncora {@code vila_calada} existe (sem ela, não há para onde ir)
     * @param alguemOnline algum membro online (fora do modo espectador)
     * @param vezDeTentar pode tentar soltar o evento nesta olhada (uma tentativa por olhada, para não pesar)
     * @param adminOnline algum admin online para receber o aviso
     */
    public record Grupo(boolean teveEvento, boolean chegou, boolean temVilaCalada, boolean alguemOnline,
                        boolean vezDeTentar, boolean adminOnline) {}

    private RegrasGatilhos() {}

    public static boolean noite(long hora) {
        return RegrasVilaCalada.noite(hora);
    }

    /** Número do dia (a noite de um dia vai de 13.000 a 23.000, dentro do mesmo número). */
    public static long dia(long hora) {
        return Math.floorDiv(hora, TICKS_DIA);
    }

    public static boolean noiteDeNeblina(long hora, boolean chuva, boolean precisaChuva) {
        return noite(hora) && (chuva || !precisaChuva);
    }

    /**
     * Arquivo dos gatilhos criado agora (atualização num mundo que já existe): todo grupo que já existe fica
     * "já teve" e nunca solta sozinho. Só grupos criados depois ficam automáticos.
     */
    public static void primeiraCriacao(Map<String, EstadoGrupo> estados, Collection<String> gruposExistentes) {
        for (String g : gruposExistentes) {
            EstadoGrupo st = estados.computeIfAbsent(g, k -> new EstadoGrupo());
            st.soltou();
        }
    }

    public static int noitesAviso(int config) {
        return Math.max(NOITES_AVISO_MIN, Math.min(NOITES_AVISO_MAX, config));
    }

    /**
     * Uma olhada no grupo: atualiza o estado e diz o que fazer. Quem chama marca {@link EstadoGrupo#soltou()} se o
     * evento soltou e {@link EstadoGrupo#avisou()} se o aviso foi entregue.
     */
    public static Acao passo(EstadoGrupo st, Momento m, Grupo g) {
        long dia = dia(m.hora());
        boolean noite = noite(m.hora());
        boolean neblina = noiteDeNeblina(m.hora(), m.chuva(), m.precisaChuva());

        // Mundo que já existe: grupo que passou pelo evento (pelo admin) fica marcado para sempre.
        if (g.teveEvento()) st.soltou();

        if (!st.jaTeve) {
            if (!neblina) {
                // Fora da neblina: a próxima noite de neblina é "a primeira depois" (de ativado ou do livro).
                if (!st.armado) {
                    st.armado = true;
                    st.sujo = true;
                }
                return Acao.NADA;
            }
            if (!m.automatico() || !st.armado || !g.alguemOnline() || !g.vezDeTentar() || st.diaTentado == dia) {
                return Acao.NADA;
            }
            st.diaTentado = dia; // uma tentativa por noite: sem vila perto, tenta de novo na próxima neblina
            return Acao.SOLTAR_PRIMEIRO_SINAL;
        }

        // Já teve o evento: acompanha se o grupo segue o sino.
        if (!m.avisoLigado() || st.avisado || st.livroLido || g.chegou() || !g.temVilaCalada()) return Acao.NADA;
        if (noite && g.alguemOnline() && st.ultimaNoite != dia) {
            st.ultimaNoite = dia;
            st.noites++;
            st.sujo = true;
        }
        if (st.noites >= noitesAviso(m.noitesAviso()) && g.adminOnline()) return Acao.AVISAR_ADMIN;
        return Acao.NADA;
    }
}
