package com.nikolas.odteste.itens;

import java.util.HashMap;
import java.util.Map;

/**
 * Regras dos Artefatos Paranormais (Java puro, testado em RegrasConteudoTest), tiradas de
 * lore/artefatos-paranormais.md ("Regras anti-abuso"):
 * - recarga própria por raridade (comum 60 s, incomum 3 min, rara 10 min);
 * - recarga compartilhada de 10 s entre todos;
 * - usar o mesmo de novo em menos de 5 min cobra o dobro de Lucidez ou Exposição;
 * - Lucidez em zero bloqueia o uso.
 * Tempos em ticks do mundo (20 por segundo). Números são proposta para o play test.
 */
public final class RegrasArtefato {
    public enum Raridade {
        COMUM(20 * 60), INCOMUM(20 * 60 * 3), RARA(20 * 60 * 10);

        public final int recarga;

        Raridade(int recarga) {
            this.recarga = recarga;
        }
    }

    public enum Bloqueio { LIVRE, RECARGA, COMPARTILHADA, SEM_LUCIDEZ }

    public static final int RECARGA_COMPARTILHADA = 20 * 10;
    public static final long JANELA_DOBRO = 20L * 60 * 5;
    /** Uso mais velho que isso não importa para nenhuma regra: sai do dado salvo. */
    public static final long GUARDAR = Math.max(JANELA_DOBRO, Raridade.RARA.recarga);

    // Agulha de Cerzir Carne
    public static final int AGULHA_EXPOSICAO = 10;
    /** "+1 de progresso da Marca de Sangue": 1 ponto de contato com o Sangue (100 centésimos). */
    public static final int AGULHA_CONTATO_MARCA = 100;
    /** Regeneração II por 10 s ≈ 4 corações. */
    public static final int AGULHA_REGEN_TICKS = 200;
    public static final int AGULHA_REGEN_NIVEL = 1;

    // Dedal Dentado
    public static final int DEDAL_LUCIDEZ = 5;
    public static final int DEDAL_TICKS = 20 * 20;
    /** Parte do dano que volta em quem bateu. */
    public static final float DEDAL_REFLEXO = 0.3f;
    /** −1 coração máximo até dormir. */
    public static final double DEDAL_VIDA_MAX = -2.0;

    // Sino de Matadouro
    public static final int SINO_EXPOSICAO = 25;
    /** 3 corações. */
    public static final float SINO_VIDA = 6f;
    public static final int SINO_TICKS = 20 * 15;
    public static final double SINO_RAIO = 8.0;

    private RegrasArtefato() {}

    /**
     * Pode usar agora?
     * @param ultimoDoItem último uso deste artefato (null = nunca)
     * @param ultimoDeTodos último uso de qualquer artefato (null = nunca)
     */
    public static Bloqueio bloqueio(long agora, Long ultimoDoItem, Long ultimoDeTodos, Raridade r, int lucidez) {
        if (lucidez <= 0) return Bloqueio.SEM_LUCIDEZ;
        if (dentro(agora, ultimoDoItem, r.recarga)) return Bloqueio.RECARGA;
        if (dentro(agora, ultimoDeTodos, RECARGA_COMPARTILHADA)) return Bloqueio.COMPARTILHADA;
        return Bloqueio.LIVRE;
    }

    /** Usar de novo em menos de 5 min cobra o dobro. */
    public static int custo(int base, long agora, Long ultimoDoItem) {
        return dentro(agora, ultimoDoItem, JANELA_DOBRO) ? base * 2 : base;
    }

    /** O uso de {@code ultimo} ainda está dentro de {@code janela}? Relógio que voltou (mundo trocado) não conta. */
    static boolean dentro(long agora, Long ultimo, long janela) {
        if (ultimo == null || ultimo > agora) return false;
        return agora - ultimo < janela;
    }

    /** O mais recente de todos os usos (null se não houver). */
    public static Long ultimoDeTodos(Map<String, Long> usos) {
        Long max = null;
        for (Long v : usos.values()) {
            if (v != null && (max == null || v > max)) max = v;
        }
        return max;
    }

    /** Registra um uso e tira os velhos (e os do "futuro", de mundo trocado), para o dado não crescer. */
    public static Map<String, Long> registrar(Map<String, Long> usos, String item, long agora) {
        Map<String, Long> m = new HashMap<>();
        usos.forEach((k, v) -> {
            if (k != null && v != null && v <= agora && agora - v < GUARDAR) m.put(k, v);
        });
        m.put(item, agora);
        return m;
    }
}
