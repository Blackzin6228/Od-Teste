package com.nikolas.odteste.cenas;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Contas das cenas em JSON (Java puro, testado em RegrasRoteiroTest): onde a câmera está em cada instante (curva
 * Catmull-Rom pelo tempo, ritmo, corte), tremor, preto/vermelho, barras de cinema, textos, fases (entrada, cena,
 * pulo, saída) e quanto tempo o servidor segura o jogador. O cliente só pergunta daqui e aplica.
 */
public final class RegrasRoteiro {
    /** No começo da câmera, o preto da entrada some em tanto tempo (ticks). */
    public static final int CLAREAR = 10;
    /** Saída com preto: escurece nos últimos ticks da câmera e some depois de voltar ao jogador. */
    public static final int ESCURECER_FIM = 10;
    public static final int SAIDA = 16;
    /** Barras de cinema entram (e saem, na saída direta) em 1 s. */
    public static final int BARRAS = 20;
    /** Folga do servidor além do tempo da cena (pausa, atraso da rede): 5 s. */
    public static final int FOLGA_SERVIDOR = 100;
    /** Cena mais longa aceita: 2 min. */
    public static final int MAX_DURACAO = 20 * 120;
    public static final int MAX_CHAVES = 64;
    public static final int MAX_EVENTOS = 128;
    /** Câmera mais perto que isso dos olhos do alvo: o boneco dele some (senão a câmera fica dentro da cabeça). */
    public static final double PERTO_DOS_OLHOS = 0.35;

    private RegrasRoteiro() {}

    /** Onde o alvo está e para onde olha, no começo da cena (o servidor manda; ele fica parado). */
    public record Olhos(double x, double y, double z, float giro, float inclinacao) {}

    /** Âncora da cena no mundo. */
    public record Ancora(double x, double y, double z, float giro) {}

    /** Uma chave já no mundo. */
    public record ChaveMundo(int t, double[] v, Roteiro.Curva curva, Roteiro.Ritmo ritmo) {}

    /** Câmera num instante: posição, giro, inclinação, campo de visão e roll. */
    public record Pose(double x, double y, double z, float giro, float inclinacao, float fov, float roll) {}

    /** Cor e opacidade do véu (preto ou vermelho) da cena. */
    public record Veu(int cor, float alfa) {}

    // ------------------------------------------------------------------ chaves no mundo

    /**
     * Passa todas as chaves para o mundo. Giro "desenrolado": de uma chave para a outra a câmera gira pelo lado mais
     * curto. {@code fovJogador} entra onde a chave não diz o campo de visão.
     */
    public static List<ChaveMundo> resolver(Roteiro r, Ancora a, Olhos o, float fovJogador) {
        List<ChaveMundo> lista = new ArrayList<>();
        double giroAntes = Double.NaN;
        for (Roteiro.Chave c : r.chaves()) {
            double[] p = ponto(c.ref(), c.entre(), c.pos(), a, o);
            float[] ang = angulos(c, p, a, o);
            double giro = ang[0];
            if (!Double.isNaN(giroAntes)) giro = desenrolar(giroAntes, giro);
            giroAntes = giro;
            float fov = Float.isNaN(c.fov()) ? fovJogador : c.fov();
            lista.add(new ChaveMundo(c.t(), new double[] {p[0], p[1], p[2], giro, ang[1], fov, c.roll()}, c.curva(),
                    c.ritmo()));
        }
        return List.copyOf(lista);
    }

    /** Ponto de uma referência no mundo. */
    static double[] ponto(Roteiro.Referencia ref, double entre, Cena.Local l, Ancora a, Olhos o) {
        return switch (ref) {
            case ANCORA -> RegrasCena.paraMundo(a.x(), a.y(), a.z(), a.giro(), l);
            case JOGADOR -> RegrasCena.paraMundo(o.x(), o.y(), o.z(), a.giro(), l);
            case ENTRE -> RegrasCena.paraMundo(misturar(o.x(), a.x(), entre), misturar(o.y(), a.y(), entre),
                    misturar(o.z(), a.z(), entre), a.giro(), l);
            case OLHOS -> RegrasCena.paraMundo(o.x(), o.y(), o.z(), o.giro(), l);
        };
    }

    private static float[] angulos(Roteiro.Chave c, double[] p, Ancora a, Olhos o) {
        Roteiro.Olhar ol = c.olhar();
        double[] alvo = null;
        if (ol.paraJogador()) alvo = new double[] {o.x(), o.y(), o.z()};
        else if (ol.ponto() != null) alvo = ponto(ol.refPonto(), ol.entre(), ol.ponto(), a, o);
        if (alvo != null) {
            double dx = alvo[0] - p[0], dy = alvo[1] - p[1], dz = alvo[2] - p[2];
            if (dx * dx + dy * dy + dz * dz > 1e-6) return RegrasCena.olhar(dx, dy, dz);
            return new float[] {o.giro(), o.inclinacao()}; // em cima do alvo: a visão do jogador
        }
        if (c.ref() == Roteiro.Referencia.OLHOS) {
            return new float[] {o.giro() + ol.giro(), limitarInclinacao(o.inclinacao() + ol.inclinacao())};
        }
        return new float[] {a.giro() + ol.giro(), limitarInclinacao(ol.inclinacao())};
    }

    /** {@code depois} trocado pelo ângulo igual (±360) mais perto de {@code antes}. */
    public static double desenrolar(double antes, double depois) {
        double d = depois - antes;
        d = ((d % 360) + 540) % 360 - 180;
        return antes + d;
    }

    static float limitarInclinacao(double v) {
        return (float) Math.max(-89.9, Math.min(89.9, v));
    }

    // ------------------------------------------------------------------ câmera num instante

    /** Ritmo de 0 a 1. */
    public static double ritmo(Roteiro.Ritmo r, double u) {
        double x = RegrasCena.limitar(u);
        return switch (r) {
            case LINEAR -> x;
            case SUAVE -> x * x * (3 - 2 * x);
            case ENTRA -> x * x;
            case SAI -> 1 - (1 - x) * (1 - x);
        };
    }

    /** Pose da câmera no tick {@code t} (com fração). Antes da primeira chave, a primeira; depois da última, a última. */
    public static Pose pose(List<ChaveMundo> k, double t) {
        int n = k.size();
        if (t <= k.get(0).t() || n == 1) return pose(k.get(0).v());
        if (t >= k.get(n - 1).t()) return pose(k.get(n - 1).v());
        int i = 0;
        while (i < n - 2 && t >= k.get(i + 1).t()) i++;
        ChaveMundo a = k.get(i), b = k.get(i + 1);
        if (a.curva() == Roteiro.Curva.CORTE) return pose(a.v());
        double dt = Math.max(1e-6, b.t() - a.t());
        double u = ritmo(a.ritmo(), (t - a.t()) / dt);
        double[] v = new double[7];
        if (a.curva() == Roteiro.Curva.LINEAR) {
            for (int d = 0; d < 7; d++) v[d] = misturar(a.v()[d], b.v()[d], u);
        } else {
            double[] ma = tangente(k, i), mb = tangente(k, i + 1);
            double u2 = u * u, u3 = u2 * u;
            double h00 = 2 * u3 - 3 * u2 + 1, h10 = u3 - 2 * u2 + u, h01 = -2 * u3 + 3 * u2, h11 = u3 - u2;
            for (int d = 0; d < 7; d++) {
                v[d] = h00 * a.v()[d] + h10 * dt * ma[d] + h01 * b.v()[d] + h11 * dt * mb[d];
            }
        }
        return pose(v);
    }

    /**
     * Inclinação da curva na chave {@code i} (por tick). Catmull-Rom com o tempo de verdade: a diferença entre a
     * chave de antes e a de depois, dividida pelo tempo entre elas. Corte separa: depois de um corte a chave não olha
     * para trás, e uma chave com corte não olha para frente.
     */
    static double[] tangente(List<ChaveMundo> k, int i) {
        int n = k.size();
        boolean temAntes = i > 0 && k.get(i - 1).curva() != Roteiro.Curva.CORTE;
        boolean temDepois = i < n - 1 && k.get(i).curva() != Roteiro.Curva.CORTE;
        ChaveMundo de = temAntes ? k.get(i - 1) : k.get(i);
        ChaveMundo ate = temDepois ? k.get(i + 1) : k.get(i);
        double[] m = new double[7];
        double dt = ate.t() - de.t();
        if (dt <= 0) return m;
        for (int d = 0; d < 7; d++) m[d] = (ate.v()[d] - de.v()[d]) / dt;
        return m;
    }

    private static Pose pose(double[] v) {
        return new Pose(v[0], v[1], v[2], (float) v[3], limitarInclinacao(v[4]),
                (float) Math.max(5, Math.min(150, v[5])), (float) v[6]);
    }

    /** Distância da câmera aos olhos do alvo. */
    public static double distanciaOlhos(Pose p, Olhos o) {
        double dx = p.x() - o.x(), dy = p.y() - o.y(), dz = p.z() - o.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // ------------------------------------------------------------------ eventos

    /**
     * Tremor no tick {@code t}: {giro, inclinação, roll} a somar (graus). Some até o fim de cada tremor; vários se
     * somam. {@code escala} = força do "efeitos fortes" do jogador (0 a 1).
     */
    public static float[] tremor(List<Roteiro.Evento> eventos, double t, float escala) {
        float[] r = new float[3];
        if (escala <= 0) return r;
        for (Roteiro.Evento e : eventos) {
            if (!(e instanceof Roteiro.Tremor tr) || t < tr.t() || t >= tr.t() + tr.duracao()) continue;
            double env = 1 - (t - tr.t()) / Math.max(1, tr.duracao());
            double amp = tr.forca() * env * escala;
            r[0] += (float) (amp * 2.0 * (Math.sin(t * 1.9 + tr.t()) * 0.6 + Math.sin(t * 4.7 + 1.1) * 0.4));
            r[1] += (float) (amp * 1.6 * (Math.sin(t * 2.3 + 0.7) * 0.6 + Math.sin(t * 5.9) * 0.4));
            r[2] += (float) (amp * 1.2 * Math.sin(t * 3.1 + 2.0));
        }
        return r;
    }

    /** Véu da cena (eventos de fade) no tick {@code t}. Sem fade: transparente. */
    public static Veu veu(List<Roteiro.Evento> eventos, double t) {
        int cor = LeitorRoteiro.PRETO;
        double de = 0, ate = 0, inicio = 0, dur = 1;
        for (Roteiro.Evento e : eventos) {
            if (!(e instanceof Roteiro.Fade f)) continue;
            if (f.t() > t) break;
            double agora = misturar(de, ate, RegrasCena.limitar((f.t() - inicio) / dur));
            de = f.de() >= 0 ? f.de() : agora;
            ate = f.ate();
            inicio = f.t();
            dur = Math.max(1e-6, f.duracao());
            if (f.cor() >= 0) cor = f.cor();
        }
        return new Veu(cor, (float) RegrasCena.limitar(misturar(de, ate, RegrasCena.limitar((t - inicio) / dur))));
    }

    /** Opacidade de um texto no tick {@code t} (entra em 0,5 s, sai em 0,4 s; 0 fora da janela). */
    public static float texto(Roteiro.Texto x, double t) {
        return RegrasCena.legenda(new Cena.Legenda(x.chave(), x.linha(), x.t(), x.t() + x.duracao()), t);
    }

    /** Altura de cada barra de cinema (fração da tela) no tick {@code t}. */
    public static float barras(Roteiro r, double t) {
        if (r.barras() <= 0) return 0f;
        double f = RegrasCena.suave(t / BARRAS);
        if (r.saidaDireta()) f *= RegrasCena.suave((r.duracao() - t) / BARRAS);
        return (float) (r.barras() * f);
    }

    // ------------------------------------------------------------------ fases

    /**
     * Em que fase a cena está.
     *
     * @param t       ticks desde o começo da câmera (de -{@link RegrasCena#ENTRADA} a 0: a visão do jogador escurecendo)
     * @param pulouEm tick em que pulou ({@link RegrasCena#SEM_PULO} = não pulou)
     */
    public static RegrasCena.Quadro fase(Roteiro r, double t, double pulouEm) {
        if (!Double.isNaN(pulouEm) && t >= pulouEm) {
            boolean comCamera = pulouEm >= 0 && pulouEm < r.duracao();
            float base = fase(r, pulouEm, RegrasCena.SEM_PULO).preto();
            double d = t - pulouEm;
            if (d < RegrasCena.ESCURECER_PULO) {
                return new RegrasCena.Quadro(comCamera, (float) misturar(base, 1, d / RegrasCena.ESCURECER_PULO), false);
            }
            double d2 = d - RegrasCena.ESCURECER_PULO;
            if (d2 < SAIDA) return new RegrasCena.Quadro(false, (float) (1 - d2 / SAIDA), false);
            return new RegrasCena.Quadro(false, 0f, true);
        }
        if (t < 0) return new RegrasCena.Quadro(false, (float) RegrasCena.limitar(1 + t / RegrasCena.ENTRADA), false);
        int dur = r.duracao();
        if (t < dur) {
            double entra = RegrasCena.limitar(1 - t / CLAREAR);
            double sai = r.saidaDireta() ? 0 : RegrasCena.limitar((t - (dur - ESCURECER_FIM)) / ESCURECER_FIM);
            return new RegrasCena.Quadro(true, (float) Math.max(entra, sai), false);
        }
        if (r.saidaDireta()) return new RegrasCena.Quadro(false, 0f, true);
        double d = t - dur;
        if (d < SAIDA) return new RegrasCena.Quadro(false, (float) (1 - d / SAIDA), false);
        return new RegrasCena.Quadro(false, 0f, true);
    }

    /** Ticks que o servidor segura o jogador parado e protegido (a cena inteira + folga). */
    public static int ticksProtegido(int duracaoCamera) {
        return RegrasCena.ENTRADA + duracaoCamera + SAIDA + RegrasCena.ESCURECER_PULO + FOLGA_SERVIDOR;
    }

    /** Pode pular? Precisa da cena e do mestre deixarem. Mestre assistindo sempre pode. */
    public static boolean podePular(boolean cenaDeixa, boolean mestreDeixa, boolean espectador) {
        return espectador || (cenaDeixa && mestreDeixa);
    }

    // ------------------------------------------------------------------ conferência

    /** Problemas na cena (lista vazia = boa). {@code efeitos}: nomes de efeito de tela que existem. */
    public static List<String> problemas(Roteiro r, Set<String> efeitos) {
        List<String> p = new ArrayList<>();
        if (r.duracao() <= 0 || r.duracao() > MAX_DURACAO) p.add("duracao fora de 1 a " + MAX_DURACAO);
        if (r.ancora() == null || !r.ancora().matches("[a-z0-9_]+")) p.add("ancora inválida");
        if (r.barras() < 0 || r.barras() > 0.25f) p.add("barras fora de 0 a 0,25");
        if (r.chaves().isEmpty()) p.add("sem chaves");
        if (r.chaves().size() > MAX_CHAVES) p.add("chaves demais");
        if (r.eventos().size() > MAX_EVENTOS) p.add("eventos demais");
        if (!r.chaves().isEmpty() && r.chaves().get(0).t() != 0) p.add("a primeira chave precisa ser no tick 0");
        int antes = -1;
        for (Roteiro.Chave c : r.chaves()) {
            if (c.t() <= antes) p.add("chave fora de ordem (ou repetida) em " + c.t());
            if (c.t() > r.duracao()) p.add("chave depois do fim em " + c.t());
            if (c.ref() == Roteiro.Referencia.ENTRE && (c.entre() < 0 || c.entre() > 1)) p.add("entre fora de 0-1 em " + c.t());
            if (!Float.isNaN(c.fov()) && (c.fov() < 5 || c.fov() > 150)) p.add("fov fora de 5-150 em " + c.t());
            if (Math.abs(c.roll()) > 90) p.add("roll fora de ±90 em " + c.t());
            if (Math.abs(c.pos().x()) > 256 || Math.abs(c.pos().y()) > 256 || Math.abs(c.pos().z()) > 256) {
                p.add("ponto longe demais (mais de 256 blocos) em " + c.t());
            }
            antes = c.t();
        }
        for (Roteiro.Evento e : r.eventos()) {
            if (e.t() < 0 || e.t() >= r.duracao()) p.add("evento fora da cena no tick " + e.t());
            switch (e) {
                case Roteiro.Som s -> {
                    if (!s.som().matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) p.add("som sem namespace: " + s.som());
                    if (s.volume() <= 0 || s.volume() > 1) p.add("volume fora de 0-1: " + s.som());
                    if (s.tom() < 0.5f || s.tom() > 2f) p.add("tom fora de 0,5-2: " + s.som());
                }
                case Roteiro.Fade f -> {
                    if (f.ate() < 0 || f.ate() > 1 || f.de() > 1) p.add("fade fora de 0-1 no tick " + f.t());
                    if (f.duracao() < 0) p.add("fade com duração negativa no tick " + f.t());
                }
                case Roteiro.Texto x -> {
                    if (!x.chave().startsWith("odteste.")) p.add("texto sem chave odteste.: " + x.chave());
                    if (x.linha() != 0 && x.linha() != 1) p.add("linha do texto é 0 ou 1: " + x.chave());
                    if (x.duracao() <= 0 || x.t() + x.duracao() > r.duracao()) p.add("texto passa do fim: " + x.chave());
                }
                case Roteiro.Tremor tr -> {
                    if (tr.forca() <= 0 || tr.forca() > 2) p.add("tremor fora de 0-2 no tick " + tr.t());
                    if (tr.duracao() <= 0) p.add("tremor sem duração no tick " + tr.t());
                }
                case Roteiro.Fx fx -> {
                    if (!efeitos.contains(fx.efeito())) p.add("efeito desconhecido: " + fx.efeito());
                    if (fx.segundos() < 0 || fx.segundos() > 60) p.add("efeito fora de 0-60 s: " + fx.efeito());
                }
                case Roteiro.PararSons ps -> { }
            }
        }
        return p;
    }

    static double misturar(double a, double b, double f) {
        return a + (b - a) * f;
    }
}
