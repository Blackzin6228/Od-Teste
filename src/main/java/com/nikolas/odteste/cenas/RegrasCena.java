package com.nikolas.odteste.cenas;

import java.util.ArrayList;
import java.util.List;

/**
 * Contas das cenas (Java puro, testado em RegrasCenaTest): onde a câmera está em cada instante, para onde olha,
 * quanto a tela está preta e em que fase a cena está. O cliente só pergunta daqui e aplica.
 */
public final class RegrasCena {
    /** Antes da câmera trocar: a tela escurece por cima da visão do jogador (ticks). */
    public static final int ENTRADA = 10;
    /** Segurar a tecla de pular por tanto tempo (1 s) corta a cena. */
    public static final int SEGURAR_PARA_PULAR = 20;
    /** Pulou: a tela escurece em 0,4 s antes de voltar ao jogador. */
    public static final int ESCURECER_PULO = 8;
    /** Legenda: aparece em 0,5 s e some em 0,4 s. */
    public static final int LEGENDA_ENTRA = 10;
    public static final int LEGENDA_SAI = 8;
    /** Período do balanço "de passo" (ticks). */
    public static final double PASSO = 16;
    /** Valor de "não pulou" para {@link #quadro}. */
    public static final double SEM_PULO = Double.NaN;

    private RegrasCena() {}

    /** Onde a câmera está e para onde olha, em coordenadas locais. */
    public record Pose(Cena.Local camera, Cena.Local alvo, float zoom) {}

    /**
     * Fase da cena num instante.
     *
     * @param camera {@code true} = a câmera é a da cena; {@code false} = a do jogador
     * @param preto  opacidade da tela preta (0-1)
     * @param acabou a cena terminou: devolver tudo
     */
    public record Quadro(boolean camera, float preto, boolean acabou) {}

    /** 0 a 1 com começo e fim suaves (sem tranco no começo nem no fim do plano). */
    public static double suave(double f) {
        double x = limitar(f);
        return x * x * (3 - 2 * x);
    }

    public static double limitar(double v) {
        return Math.max(0, Math.min(1, v));
    }

    static double misturar(double a, double b, double f) {
        return a + (b - a) * f;
    }

    static Cena.Local misturar(Cena.Local a, Cena.Local b, double f) {
        return new Cena.Local(misturar(a.x(), b.x(), f), misturar(a.y(), b.y(), f), misturar(a.z(), b.z(), f));
    }

    /** O plano que vale no tick {@code t}: o último que já começou (antes do primeiro, o primeiro). */
    public static Cena.Plano plano(Cena c, double t) {
        Cena.Plano atual = c.planos().get(0);
        for (Cena.Plano p : c.planos()) {
            if (p.inicio() <= t) atual = p;
        }
        return atual;
    }

    /** Pose da câmera no tick {@code t} (pode ter fração: o cliente pede a cada quadro desenhado). */
    public static Pose pose(Cena c, double t) {
        Cena.Plano p = plano(c, t);
        double dur = Math.max(1, p.fimPlano() - p.inicio());
        double f = suave((t - p.inicio()) / dur);
        Cena.Local cam = misturar(p.de(), p.ate(), f);
        if (p.balanco() > 0) {
            double fase = (t - p.inicio()) * 2 * Math.PI / PASSO;
            cam = cam.mais(Math.sin(fase / 2) * p.balanco() * 0.5, Math.abs(Math.sin(fase)) * p.balanco(), 0);
        }
        Cena.Local alvo = misturar(p.alvoDe(), p.alvoAte(), f);
        return new Pose(cam, alvo, (float) (p.zoom() * f));
    }

    /**
     * Ponto local → mundo. O giro é o da âncora em graus, no jeito do Minecraft (0 = olhando para +z/sul,
     * 90 = para -x/oeste). Volta {x, y, z}.
     */
    public static double[] paraMundo(double ax, double ay, double az, float giro, Cena.Local l) {
        double r = Math.toRadians(giro);
        double fx = -Math.sin(r), fz = Math.cos(r); // frente
        double dx = -Math.cos(r), dz = -Math.sin(r); // direita
        return new double[] {ax + dx * l.x() + fx * l.z(), ay + l.y(), az + dz * l.x() + fz * l.z()};
    }

    /** Giro e inclinação (graus, jeito do Minecraft) para olhar na direção (dx, dy, dz). Volta {giro, inclinação}. */
    public static float[] olhar(double dx, double dy, double dz) {
        double h = Math.sqrt(dx * dx + dz * dz);
        float giro = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float incl = (float) -Math.toDegrees(Math.atan2(dy, h));
        return new float[] {giro, incl};
    }

    /** Giro (graus) de quem olha na direção horizontal (dx, dz). Direção nula: 0 (sul). */
    public static float giroDe(double dx, double dz) {
        if (dx * dx + dz * dz < 1e-9) return 0f;
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    /** Opacidade do preto definido na cena (reta entre os pontos; fora deles, o valor da ponta). */
    public static float preto(Cena c, double t) {
        List<Cena.Ponto> pts = c.preto();
        if (pts.isEmpty()) return 0f;
        if (t <= pts.get(0).tick()) return pts.get(0).valor();
        for (int i = 1; i < pts.size(); i++) {
            Cena.Ponto a = pts.get(i - 1), b = pts.get(i);
            if (t <= b.tick()) {
                double dur = Math.max(1, b.tick() - a.tick());
                return (float) misturar(a.valor(), b.valor(), limitar((t - a.tick()) / dur));
            }
        }
        return pts.get(pts.size() - 1).valor();
    }

    /** Opacidade de uma legenda no tick {@code t} (0 fora da janela). */
    public static float legenda(Cena.Legenda l, double t) {
        if (t < l.entra() || t >= l.sai()) return 0f;
        double sobe = limitar((t - l.entra()) / LEGENDA_ENTRA);
        double desce = limitar((l.sai() - t) / LEGENDA_SAI);
        return (float) Math.min(sobe, desce);
    }

    /**
     * Em que fase a cena está.
     *
     * @param t       ticks desde o começo da câmera (negativo = ainda escurecendo a visão do jogador, de -ENTRADA a 0)
     * @param pulouEm tick em que o jogador pulou ({@link #SEM_PULO} = não pulou)
     */
    public static Quadro quadro(Cena c, double t, double pulouEm) {
        if (!Double.isNaN(pulouEm) && t >= pulouEm) {
            boolean comCamera = pulouEm >= 0 && pulouEm < c.fim();
            float base = pulouEm < 0 ? (float) limitar(1 + pulouEm / ENTRADA) : comCamera ? preto(c, pulouEm) : 1f;
            double d = t - pulouEm;
            if (d < ESCURECER_PULO) {
                return new Quadro(comCamera, (float) misturar(base, 1, d / ESCURECER_PULO), false);
            }
            double d2 = d - ESCURECER_PULO;
            if (d2 < c.saida()) return new Quadro(false, (float) (1 - d2 / c.saida()), false);
            return new Quadro(false, 0f, true);
        }
        if (t < 0) return new Quadro(false, (float) limitar(1 + t / ENTRADA), false);
        if (t < c.fim()) return new Quadro(true, preto(c, t), false);
        double d = t - c.fim();
        if (d < c.saida()) return new Quadro(false, (float) (preto(c, c.fim()) * (1 - d / c.saida())), false);
        return new Quadro(false, 0f, true);
    }

    /** Teto de cenas guardadas por jogador (protege contra arquivo editado à mão). */
    public static final int MAX_VISTAS = 64;

    /** Lista de cenas vistas lida do disco: sem vazio, sem repetido, no máximo {@link #MAX_VISTAS}. */
    public static List<String> limparVistas(List<String> lido) {
        List<String> r = new ArrayList<>();
        for (String s : lido) {
            if (s != null && !s.isBlank() && !r.contains(s) && r.size() < MAX_VISTAS) r.add(s);
        }
        return List.copyOf(r);
    }

    /** Problemas na definição de uma cena (lista vazia = boa). Usado no teste. */
    public static List<String> problemas(Cena c) {
        List<String> r = new ArrayList<>();
        if (c.planos().isEmpty()) r.add("sem planos");
        if (c.fim() <= 0) r.add("fim <= 0");
        if (c.saida() <= 0) r.add("saida <= 0");
        int antes = -1;
        for (Cena.Plano p : c.planos()) {
            if (p.inicio() < antes) r.add("plano fora de ordem em " + p.inicio());
            if (p.fimPlano() <= p.inicio()) r.add("plano sem duração em " + p.inicio());
            if (p.fimPlano() > c.fim()) r.add("plano passa do fim em " + p.inicio());
            antes = p.inicio();
        }
        int ultimo = -1;
        for (Cena.Ponto pt : c.preto()) {
            if (pt.tick() < ultimo) r.add("preto fora de ordem em " + pt.tick());
            if (pt.valor() < 0 || pt.valor() > 1) r.add("preto fora de 0-1 em " + pt.tick());
            ultimo = pt.tick();
        }
        for (Cena.Som s : c.sons()) {
            if (s.tick() < 0 || s.tick() >= c.fim()) r.add("som fora da cena: " + s.id());
            if (!s.id().contains(":")) r.add("som sem namespace: " + s.id());
            if (s.volume() <= 0 || s.volume() > 1) r.add("volume fora de 0-1: " + s.id());
            if (s.tom() < 0.5f || s.tom() > 2f) r.add("tom fora de 0,5-2: " + s.id());
        }
        for (Cena.Legenda l : c.legendas()) {
            if (l.sai() <= l.entra()) r.add("legenda sem duração: " + l.chave());
            if (l.sai() > c.fim()) r.add("legenda passa do fim: " + l.chave());
        }
        return r;
    }
}
