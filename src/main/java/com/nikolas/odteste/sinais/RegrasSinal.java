package com.nikolas.odteste.sinais;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

/**
 * Contas das fontes de sinal fixas (Java puro, testado em {@code RegrasSinalTest}).
 * O jogador nunca vê número: só a direção em palavras e a força do ruído.
 */
public final class RegrasSinal {
    /** No máximo quantas fontes fixas no servidor inteiro (cada leitura olha todas: tem que ser pouco). */
    public static final int MAX_FONTES = 256;
    private static final Pattern NOME = Pattern.compile("[a-z0-9_]{1,32}");

    /** De onde vem o sinal, em relação a para onde o jogador olha. Ordem = sentido horário, de 45 em 45 graus. */
    public enum Direcao {
        FRENTE, FRENTE_DIREITA, DIREITA, TRAS_DIREITA, TRAS, TRAS_ESQUERDA, ESQUERDA, FRENTE_ESQUERDA;

        public String chave() {
            return "odteste.analisador.direcao." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** Força do ruído. NENHUMA = fora do alcance. */
    public enum Intensidade {
        NENHUMA, FRACA, MEDIA, FORTE;

        public String chave() {
            return "odteste.analisador.longe." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /**
     * Tipos de sinal que têm texto no lang ({@code odteste.sinal.<tipo>.decifrado}). Conteúdo novo acrescenta o
     * seu por {@link Sinais#registrarTipo}. Fonte fixa só aceita tipo desta lista.
     */
    private static final Set<String> TIPOS = new CopyOnWriteArraySet<>(List.of("sino_vila"));

    private RegrasSinal() {}

    /** Id e tipo de fonte: letras minúsculas, números e _, até 32. */
    public static boolean nomeValido(String nome) {
        return nome != null && NOME.matcher(nome).matches();
    }

    /** O tipo existe (tem texto)? */
    public static boolean tipoConhecido(String tipo) {
        return tipo != null && TIPOS.contains(tipo);
    }

    /** Tipos conhecidos, em ordem alfabética (para sugestão e mensagem de erro). */
    public static List<String> tipos() {
        return TIPOS.stream().sorted().toList();
    }

    static boolean acrescentarTipo(String tipo) {
        return nomeValido(tipo) && TIPOS.add(tipo);
    }

    /**
     * Direção do sinal para quem olha com {@code yawGraus} (rotação do Minecraft: 0 = sul, 90 = oeste,
     * 180 = norte, -90 = leste). {@code dx}, {@code dz}: sinal menos jogador.
     */
    public static Direcao direcao(float yawGraus, double dx, double dz) {
        double rad = Math.toRadians(yawGraus);
        double lx = -Math.sin(rad);
        double lz = Math.cos(rad);
        double frente = lx * dx + lz * dz;
        double direita = lx * dz - lz * dx; // positivo = à direita (x leste, z sul)
        double angulo = Math.toDegrees(Math.atan2(direita, frente));
        int setor = Math.floorMod((int) Math.round(angulo / 45.0), 8);
        return Direcao.values()[setor];
    }

    /** Força pela distância: perto de um quarto do alcance é forte, até 60% é média, até o alcance é fraca. */
    public static Intensidade intensidade(double distancia, int alcance) {
        if (alcance <= 0 || distancia < 0 || distancia > alcance) return Intensidade.NENHUMA;
        double fracao = distancia / alcance;
        if (fracao <= 0.25) return Intensidade.FORTE;
        if (fracao <= 0.6) return Intensidade.MEDIA;
        return Intensidade.FRACA;
    }

    /** Volume do chiado que o aparelho solta sozinho, pela força. */
    public static float volume(Intensidade intensidade) {
        return switch (intensidade) {
            case FORTE -> 0.5f;
            case MEDIA -> 0.3f;
            case FRACA -> 0.15f;
            case NENHUMA -> 0f;
        };
    }
}
