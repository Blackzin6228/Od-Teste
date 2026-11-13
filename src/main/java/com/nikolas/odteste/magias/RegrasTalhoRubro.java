package com.nikolas.odteste.magias;

/**
 * Números do Talho Rubro (Sangue, Básico; docs/magias.md seções 4 e 6.1). Java puro: o teste confere sem o jogo.
 * Fases: preparação curta → golpe (corte em meia-lua à frente) → rastro (gotas que mancham o chão, só visual).
 */
public final class RegrasTalhoRubro {
    /** Preparação: o tempo entre apertar e o corte sair (0,3 s). Curta: é magia Básica. */
    public static final int PREPARACAO_TICKS = 6;
    /** Rastro: quanto tempo as manchas ficam no chão (3 s, decisão do pedido). Só no cliente. */
    public static final int RASTRO_TICKS = 60;
    /** Alcance do corte, em blocos, a partir do centro de quem conjura. */
    public static final double ALCANCE = 4.0;
    /** Meia abertura da meia-lua, em graus (130° no total). */
    public static final double MEIO_ANGULO_GRAUS = 65.0;
    /** Diferença de altura máxima entre o alvo e quem conjura (o corte é na altura do peito). */
    public static final double ALTURA_MAX = 2.0;
    /** No máximo tantos alvos por golpe (os mais perto primeiro). */
    public static final int MAX_ALVOS = 6;

    /** Dano no nível 1 (sem Iron's é sempre este). No Iron's sobe 1 por nível e multiplica pelo poder da escola. */
    public static final float DANO_BASE = 4.0f;
    public static final float DANO_POR_NIVEL = 1.0f;
    public static final int NIVEL_MAX = 3;
    /** Sangramento curto: nível 0 (o mais fraco) por 3 s. */
    public static final int SANGRAMENTO_NIVEL = 0;
    public static final int SANGRAMENTO_TICKS = 60;

    /** Iron's: mana (tabela Básico 15–25) e recarga (1–3 s). */
    public static final int MANA_BASE = 20;
    public static final int MANA_POR_NIVEL = 2;
    public static final int RECARGA_SEGUNDOS = 2;

    /**
     * Sem Iron's (tecla de Afinidade): paga só Lucidez. A tabela dá 0 Lucidez no Básico (o dobro de 0 é 0),
     * então usamos um custo pequeno para a magia não sair de graça. [DECIDIR com o Nikolas]
     */
    public static final int LUCIDEZ_SEM_IRONS = 2;
    public static final int RECARGA_SEM_IRONS_TICKS = RECARGA_SEGUNDOS * 20;

    private RegrasTalhoRubro() {}

    /**
     * O alvo está dentro da meia-lua? {@code dx, dy, dz}: posição do alvo menos a de quem conjura;
     * {@code frenteX, frenteZ}: direção do olhar no chão (não precisa ser unitária).
     */
    public static boolean dentroDoArco(double dx, double dy, double dz, double frenteX, double frenteZ) {
        if (Math.abs(dy) > ALTURA_MAX) return false;
        double dist2 = dx * dx + dz * dz;
        if (dist2 > ALCANCE * ALCANCE) return false;
        if (dist2 < 0.25) return true; // colado em quem conjura: pega sempre
        double f = Math.sqrt(frenteX * frenteX + frenteZ * frenteZ);
        if (f < 1e-6) return false;
        double cos = (dx * frenteX + dz * frenteZ) / (Math.sqrt(dist2) * f);
        return cos >= Math.cos(Math.toRadians(MEIO_ANGULO_GRAUS));
    }

    /** Dano do golpe. {@code poder} = multiplicador do Iron's (1 = sem bônus); nível fora da faixa é preso. */
    public static float dano(int nivel, float poder) {
        int n = Math.max(1, Math.min(NIVEL_MAX, nivel));
        float p = Float.isFinite(poder) ? Math.max(0f, poder) : 1f;
        return (DANO_BASE + DANO_POR_NIVEL * (n - 1)) * p;
    }

    /** Pode pagar com Lucidez? Nunca deixa a magia levar a Lucidez a 0 (o surto vem do paranormal, não do botão). */
    public static boolean podePagarLucidez(int lucidez, int custo) {
        return custo <= 0 || lucidez - custo >= 1;
    }

    /** Recarga sem Iron's: já pode de novo? {@code ultimo} &lt; 0 = nunca usou. Relógio que voltou libera. */
    public static boolean recargaPronta(long agora, long ultimo) {
        return ultimo < 0 || agora < ultimo || agora - ultimo >= RECARGA_SEM_IRONS_TICKS;
    }
}
