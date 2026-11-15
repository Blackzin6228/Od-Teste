package com.nikolas.odteste.historia.mestre;

import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Regras do mestre (Java puro, testado em RegrasMestreTest).
 *
 * <ul>
 * <li><b>Quem é mestre:</b> quem tem permissão 2 e está na lista {@code mestres} da config. Lista vazia: todo op é
 * mestre (o padrão, para não sumir aviso de quem já usa).</li>
 * <li><b>Mestre ouvindo:</b> o mestre em espectador recebe os sons e pistas que o grupo recebe perto dele, sem contar
 * como presente para nenhuma regra (não para aldeão, não lê placa, não marca chegada, não perde Lucidez). Liga e
 * desliga com {@code /odteste mestre ouvir on|off}; sem escolha, vale o padrão da config ({@code ouvir_padrao}).</li>
 * </ul>
 */
public final class RegrasMestre {
    /** O mestre ouve o sino rachado e vê o rastro da cama se estiver a até tantos blocos de um membro que recebeu. */
    public static final double RAIO_PERTO_DO_GRUPO = 128;
    /** Ticks num minuto. */
    public static final long MINUTO = 1_200L;
    /** Um dia do Minecraft. */
    public static final long DIA = 24_000L;

    private RegrasMestre() {}

    /** Separa a lista da config ("Fulano, Beltrano" ou "Fulano Beltrano") em nomes, sem vazios. */
    public static List<String> nomes(String config) {
        List<String> r = new ArrayList<>();
        if (config == null) return r;
        for (String n : config.split("[,;\\s]+")) {
            String t = n.trim();
            if (!t.isEmpty()) r.add(t);
        }
        return r;
    }

    /** É mestre? Precisa ser op (permissão 2); lista vazia = todo op; nome sem diferença de maiúscula. */
    public static boolean ehMestre(boolean op, String nome, List<String> lista) {
        if (!op) return false;
        if (lista == null || lista.isEmpty()) return true;
        if (nome == null) return false;
        String n = nome.toLowerCase(Locale.ROOT);
        for (String m : lista) {
            if (m != null && m.toLowerCase(Locale.ROOT).equals(n)) return true;
        }
        return false;
    }

    /**
     * Está no modo "mestre ouvindo"?
     * @param escolha o que o mestre escolheu com o comando (null = não escolheu: vale o padrão)
     */
    public static boolean ouvindo(boolean mestre, boolean espectador, Boolean escolha, boolean padrao) {
        return mestre && espectador && (escolha == null ? padrao : escolha);
    }

    /** Ticks até o próximo amanhecer (se é noite) ou até escurecer (se é dia). */
    public static long ateMudar(long horaDoDia) {
        long t = Math.floorMod(horaDoDia, DIA);
        if (RegrasVilaCalada.noite(horaDoDia)) return RegrasVilaCalada.NOITE_FIM - t;
        if (t < RegrasVilaCalada.NOITE_INICIO) return RegrasVilaCalada.NOITE_INICIO - t;
        return DIA - t + RegrasVilaCalada.NOITE_INICIO;
    }

    /** Minutos de relógio, arredondados para cima (0 vira 0; 1 tick vira 1). Negativo vira 0. */
    public static long minutos(long ticks) {
        if (ticks <= 0) return 0;
        return (ticks + MINUTO - 1) / MINUTO;
    }

    /** Distância horizontal arredondada, para o painel. */
    public static long distancia(double dx, double dz) {
        return Math.round(Math.sqrt(dx * dx + dz * dz));
    }

    /** Número com ponto de milhar (4480 → "4.480"), como o Nikolas lê. */
    public static String milhar(long n) {
        String s = Long.toString(Math.abs(n));
        StringBuilder b = new StringBuilder();
        int c = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            b.append(s.charAt(i));
            if (++c % 3 == 0 && i > 0) b.append('.');
        }
        if (n < 0) b.append('-');
        return b.reverse().toString();
    }
}
