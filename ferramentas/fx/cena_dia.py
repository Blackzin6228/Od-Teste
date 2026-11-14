"""Cena sintética do mundo comum (overworld), para a prévia do efeito cor_horror (mesa Dev).

Campo de grama com flores, uma estrada de terra, duas casas de carvalho, árvores, e uma base de tech de jogador
(blocos de metal coloridos, máquinas com painel aceso, um tanque e canos). De dia (sol, céu azul, nuvens) ou de
noite (lua, céu escuro, tochas). Cor e profundidade da mesma projeção do jogo, para o .fsh do mod ler igual.
"""
import moderngl
import numpy as np

CENA_VS = """
#version 330
in vec2 pos;
out vec2 uv;
void main() { uv = pos * 0.5 + 0.5; gl_Position = vec4(pos, 0.0, 1.0); }
"""

CENA_FS = """
#version 330
in vec2 uv;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform mat4 Proj;
uniform mat4 View;
uniform vec3 SolDir;
uniform vec3 CamPos;
uniform float Dia;       // 1 = meio-dia, 0 = noite
layout(location = 0) out vec4 cor;
layout(location = 1) out float prof;

const float CHAO = 64.0;

float hash(vec2 p) { return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453); }
float hash3(vec3 p) { return fract(sin(dot(p, vec3(12.9898, 78.233, 37.719))) * 43758.5453); }
float ruido(vec2 p) {
    vec2 i = floor(p), f = fract(p); f = f*f*(3.0-2.0*f);
    return mix(mix(hash(i), hash(i+vec2(1,0)), f.x), mix(hash(i+vec2(0,1)), hash(i+vec2(1,1)), f.x), f.y);
}

struct Hit { float t; vec3 n; int mat; };

void caixa(vec3 ro, vec3 rd, vec3 lo, vec3 hi, int mat, inout Hit h) {
    vec3 inv = 1.0 / rd;
    vec3 t0 = (lo - ro) * inv, t1 = (hi - ro) * inv;
    vec3 a = min(t0, t1), b = max(t0, t1);
    float tn = max(max(a.x, a.y), a.z), tf = min(min(b.x, b.y), b.z);
    if (tn >= tf || tf <= 0.0 || tn <= 0.0 || tn >= h.t) return;
    vec3 n;
    if (tn == a.x) n = vec3(-sign(rd.x), 0, 0);
    else if (tn == a.y) n = vec3(0, -sign(rd.y), 0);
    else n = vec3(0, 0, -sign(rd.z));
    h.t = tn; h.n = n; h.mat = mat;
}

void casa(vec3 ro, vec3 rd, vec3 c, vec3 tam, inout Hit h) {
    vec3 lo = c - vec3(tam.x, 0.0, tam.z) * 0.5;
    vec3 hi = c + vec3(tam.x * 0.5, tam.y, tam.z * 0.5);
    caixa(ro, rd, lo, vec3(hi.x, lo.y + 1.0, hi.z), 3, h);              // base de pedregulho
    caixa(ro, rd, vec3(lo.x, lo.y + 1.0, lo.z), hi, 2, h);               // tábua de carvalho
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        caixa(ro, rd, vec3(lo.x - 0.5, hi.y + fi, lo.z - 0.5 + fi), vec3(hi.x + 0.5, hi.y + fi + 1.0, hi.z + 0.5 - fi), 4, h);
    }
    float zp = hi.z;
    caixa(ro, rd, vec3(c.x - 0.5, lo.y + 1.0, zp), vec3(c.x + 0.5, lo.y + 3.0, zp + 0.06), 5, h);  // porta
    caixa(ro, rd, vec3(c.x + 1.5, lo.y + 2.0, zp), vec3(c.x + 2.5, lo.y + 3.0, zp + 0.06), 7, h);  // janela
}

void arvore(vec3 ro, vec3 rd, vec3 c, float alt, inout Hit h) {
    caixa(ro, rd, c + vec3(-0.5, 0.0, -0.5), c + vec3(0.5, alt, 0.5), 8, h);
    caixa(ro, rd, c + vec3(-2.5, alt - 2.0, -2.5), c + vec3(2.5, alt, 2.5), 9, h);
    caixa(ro, rd, c + vec3(-1.5, alt, -1.5), c + vec3(1.5, alt + 1.0, 1.5), 9, h);
}

// Base de tech: blocos de metal coloridos (cada cor um mat), máquinas com painel aceso, tanque e canos.
void base(vec3 ro, vec3 rd, inout Hit h) {
    vec3 o = vec3(7.0, CHAO, -14.0);
    caixa(ro, rd, o + vec3(-1.0, 0.0, -1.0), o + vec3(13.0, 0.25, 7.0), 10, h);  // piso de ferro
    // fileira de máquinas (2 de altura), de cor em cor
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        caixa(ro, rd, o + vec3(fi * 2.0, 0.25, 0.0), o + vec3(fi * 2.0 + 1.0, 2.25, 1.0), 20 + i, h);
    }
    // painéis acesos nas máquinas (lado +z)
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        caixa(ro, rd, o + vec3(fi * 2.0 + 0.2, 1.1, 1.0), o + vec3(fi * 2.0 + 0.8, 1.6, 1.04), 30 + (i % 3), h);
    }
    // tanque alto e canos
    caixa(ro, rd, o + vec3(1.0, 0.25, 4.0), o + vec3(3.0, 4.25, 6.0), 11, h);
    caixa(ro, rd, o + vec3(3.0, 1.4, 4.8), o + vec3(9.0, 1.8, 5.2), 12, h);
    caixa(ro, rd, o + vec3(8.6, 0.25, 4.8), o + vec3(9.0, 1.8, 5.2), 12, h);
    // bloco de diamante, ouro, esmeralda e redstone empilhados (decoração de base)
    caixa(ro, rd, o + vec3(10.0, 0.25, 4.0), o + vec3(11.0, 1.25, 5.0), 13, h);
    caixa(ro, rd, o + vec3(11.0, 0.25, 4.0), o + vec3(12.0, 1.25, 5.0), 14, h);
    caixa(ro, rd, o + vec3(10.0, 1.25, 4.0), o + vec3(11.0, 2.25, 5.0), 15, h);
    caixa(ro, rd, o + vec3(11.0, 1.25, 4.0), o + vec3(12.0, 2.25, 5.0), 16, h);
}

vec3 albedo(int mat, vec3 p, vec3 n) {
    vec3 b = floor(p - n * 0.01 + 1e-3);
    float g = hash3(b) * 0.10;
    vec2 f = (abs(n.y) > 0.5) ? fract(p.xz) : (abs(n.x) > 0.5 ? fract(p.zy) : fract(p.xy));
    float borda = smoothstep(0.0, 0.07, min(min(f.x, 1.0 - f.x), min(f.y, 1.0 - f.y)));
    vec2 q = (abs(n.y) > 0.5) ? p.xz : (abs(n.x) > 0.5 ? p.zy : p.xy);
    float px = hash(floor(q * 16.0));
    if (mat == 1) {                       // grama (com flores) e estrada de terra
        if (abs(p.x - 1.0) < 1.6) return vec3(0.47, 0.34, 0.22) * (0.8 + 0.25 * px);
        vec3 gr = vec3(0.36, 0.60, 0.22) * (0.75 + 0.3 * px) * (0.9 + 0.2 * ruido(p.xz * 0.3));
        float fl = step(0.985, hash(floor(p.xz * 2.0)));
        vec3 fc = hash(floor(p.xz * 2.0) + 3.0) < 0.5 ? vec3(0.85, 0.15, 0.12) : vec3(0.95, 0.85, 0.20);
        return mix(gr, fc, fl);
    }
    if (mat == 2) {
        float tab = fract(p.y * 4.0);
        float risca = smoothstep(0.0, 0.08, tab) * smoothstep(1.0, 0.92, tab);
        return vec3(0.64, 0.50, 0.31) * (0.78 + 0.22 * risca + px * 0.12) * (0.9 + g);
    }
    if (mat == 3) return vec3(0.48) * (0.6 + 0.4 * px) * (0.85 + 0.15 * borda);
    if (mat == 4) return vec3(0.40, 0.27, 0.16) * (0.8 + g * 2.0) * (0.8 + 0.2 * borda);
    if (mat == 5) return vec3(0.45, 0.33, 0.20);
    if (mat == 7) return vec3(0.55, 0.70, 0.80);
    if (mat == 8) return vec3(0.36, 0.27, 0.16) * (0.8 + 0.3 * px);
    if (mat == 9) return vec3(0.20, 0.42, 0.12) * (0.6 + 0.6 * px);
    // metal: borda mais escura, rebite, leve brilho
    float metal = (0.80 + 0.12 * px) * (0.65 + 0.35 * borda);
    if (mat == 10) return vec3(0.66, 0.66, 0.68) * metal;
    if (mat == 11) return vec3(0.75, 0.77, 0.80) * metal;
    if (mat == 12) return vec3(0.72, 0.45, 0.25) * metal;  // cano de cobre
    if (mat == 13) return vec3(0.38, 0.85, 0.82) * metal;  // diamante
    if (mat == 14) return vec3(0.96, 0.82, 0.25) * metal;  // ouro
    if (mat == 15) return vec3(0.20, 0.80, 0.40) * metal;  // esmeralda
    if (mat == 16) return vec3(0.70, 0.08, 0.04) * metal;  // redstone
    if (mat >= 20 && mat < 26) {
        vec3 cs[6] = vec3[6](vec3(0.80, 0.20, 0.18), vec3(0.95, 0.60, 0.15), vec3(0.85, 0.80, 0.20),
                             vec3(0.25, 0.65, 0.30), vec3(0.22, 0.40, 0.85), vec3(0.55, 0.25, 0.75));
        return cs[mat - 20] * metal;
    }
    return vec3(0.5);
}

// Painéis acesos: emitem (não dependem da luz do céu).
vec3 emissao(int mat) {
    if (mat == 30) return vec3(0.25, 1.00, 0.45);
    if (mat == 31) return vec3(1.00, 0.35, 0.20);
    if (mat == 32) return vec3(0.30, 0.65, 1.00);
    return vec3(0.0);
}

vec3 ceu(vec3 rd) {
    float y = max(rd.y, 0.0);
    vec3 diaC = mix(vec3(0.62, 0.75, 0.95), vec3(0.36, 0.55, 0.92), smoothstep(0.0, 0.7, y));
    vec3 noiteC = mix(vec3(0.045, 0.055, 0.085), vec3(0.010, 0.014, 0.030), smoothstep(0.0, 0.6, y));
    vec3 c = mix(noiteC, diaC, Dia);
    // nuvens (blocos chatos como as do jogo)
    vec2 q = rd.xz / max(rd.y, 0.04) * 40.0 + CamPos.xz;
    float nv = step(0.62, ruido(floor(q / 12.0) * 0.55)) * smoothstep(0.02, 0.12, y);
    c = mix(c, mix(vec3(0.20, 0.21, 0.26), vec3(0.97), Dia), nv * 0.85);
    // sol (de dia) ou lua (de noite), quadrados
    vec3 l = normalize(SolDir);
    vec3 dx = normalize(cross(l, vec3(0, 1, 0.001)));
    vec3 dy = cross(dx, l);
    float dl = dot(rd, l);
    if (dl > 0.0) {
        vec2 m = vec2(dot(rd, dx), dot(rd, dy)) / dl;
        if (max(abs(m.x), abs(m.y)) < 0.05) c = mix(vec3(0.86, 0.88, 0.92), vec3(1.0, 0.97, 0.80), Dia);
        c += mix(vec3(0.15, 0.17, 0.22), vec3(0.6, 0.55, 0.35), Dia) * pow(max(dl, 0.0), 200.0);
    }
    if (Dia < 0.5) {
        vec2 s = rd.xz / max(rd.y, 0.08) * 60.0;
        c += vec3(0.7) * step(0.996, hash(floor(s))) * smoothstep(0.05, 0.3, y) * (1.0 - Dia * 2.0) * (1.0 - nv);
    }
    return c;
}

const vec3 TOCHA = vec3(9.0, CHAO + 2.0, -11.5);
const vec3 TOCHA2 = vec3(-6.0, CHAO + 2.5, -5.4);

void main() {
    vec4 v = InvProj * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
    vec3 rdv = normalize(v.xyz / v.w);
    vec3 rd = normalize((InvView * vec4(rdv, 0.0)).xyz);
    vec3 ro = CamPos;
    Hit h; h.t = 1e9; h.n = vec3(0); h.mat = 0;
    if (rd.y < 0.0) {
        float tc = (CHAO - ro.y) / rd.y;
        if (tc > 0.0 && tc < h.t) { h.t = tc; h.n = vec3(0, 1, 0); h.mat = 1; }
    }
    casa(ro, rd, vec3(-7.0, CHAO, -10.0), vec3(7.0, 4.0, 7.0), h);
    casa(ro, rd, vec3(-12.0, CHAO, -30.0), vec3(8.0, 5.0, 8.0), h);
    arvore(ro, rd, vec3(-17.0, CHAO, -14.0), 6.0, h);
    arvore(ro, rd, vec3(3.0, CHAO, -40.0), 7.0, h);
    arvore(ro, rd, vec3(26.0, CHAO, -30.0), 6.0, h);
    arvore(ro, rd, vec3(-30.0, CHAO, -48.0), 7.0, h);
    base(ro, rd, h);

    vec3 col;
    if (h.t > 1e8) {
        col = ceu(rd);
        prof = 1.0;
    } else {
        vec3 p = ro + rd * h.t;
        vec3 a = albedo(h.mat, p, h.n);
        vec3 sol = normalize(SolDir);
        float ds = max(dot(h.n, sol), 0.0);
        // sombra simples do sol: raio de volta até o sol bate em algo?
        Hit s; s.t = 1e9; s.n = vec3(0); s.mat = 0;
        vec3 so = p + h.n * 0.02;
        casa(so, sol, vec3(-7.0, CHAO, -10.0), vec3(7.0, 4.0, 7.0), s);
        casa(so, sol, vec3(-12.0, CHAO, -30.0), vec3(8.0, 5.0, 8.0), s);
        arvore(so, sol, vec3(-17.0, CHAO, -14.0), 6.0, s);
        base(so, sol, s);
        float somb = s.t < 1e8 ? 0.0 : 1.0;
        vec3 luzDia = vec3(1.0, 0.96, 0.88) * ds * somb * 0.85 + vec3(0.42, 0.48, 0.60) * (0.55 + 0.25 * h.n.y);
        vec3 luzNoite = vec3(0.10, 0.11, 0.15) * (0.5 + 0.5 * ds) + vec3(0.03);
        vec3 luz = mix(luzNoite, luzDia, Dia);
        // lado das faces (como o jogo: x mais escuro que z)
        luz *= abs(h.n.x) > 0.5 ? 0.8 : (abs(h.n.z) > 0.5 ? 0.9 : 1.0);
        for (int k = 0; k < 2; k++) {
            vec3 tp = k == 0 ? TOCHA : TOCHA2;
            float dl = length(p - tp);
            float q = clamp(1.0 - dl / 10.0, 0.0, 1.0);
            luz += vec3(1.0, 0.70, 0.38) * q * q * (1.2 - Dia);
        }
        col = a * luz + emissao(h.mat);
        float neb = smoothstep(40.0, 110.0, h.t);
        col = mix(col, mix(vec3(0.040, 0.045, 0.065), vec3(0.70, 0.80, 0.95), Dia), neb);
        vec4 clip = Proj * View * vec4(p - ro, 1.0);
        prof = clamp(clip.z / clip.w * 0.5 + 0.5, 0.0, 0.99998);
    }
    for (int k = 0; k < 2; k++) {
        vec3 tp = (k == 0 ? TOCHA : TOCHA2) - ro;
        float tl = dot(tp, rd);
        if (tl > 0.0 && tl < h.t) {
            float d = length(tp - rd * tl);
            col += vec3(1.0, 0.72, 0.36) * (smoothstep(0.10, 0.04, d) * 1.5 + exp(-d * 3.0) * 0.2 * (1.2 - Dia));
        }
    }
    cor = vec4(col, 1.0);
}
"""


class CenaDia:
    def __init__(self, ctx, larg, alt):
        self.ctx = ctx
        self.cor = ctx.texture((larg, alt), 4, dtype="f1")
        self.prof = ctx.texture((larg, alt), 1, dtype="f4")
        for t in (self.cor, self.prof):
            t.filter = (moderngl.NEAREST, moderngl.NEAREST)
            t.repeat_x = t.repeat_y = False
        self.fbo = ctx.framebuffer(color_attachments=[self.cor, self.prof])
        self.prog = ctx.program(vertex_shader=CENA_VS, fragment_shader=CENA_FS)
        quad = np.array([-1, -1, 1, -1, -1, 1, 1, 1], dtype="f4")
        self.vbo = ctx.buffer(quad.tobytes())
        self.vao = ctx.vertex_array(self.prog, [(self.vbo, "2f", "pos")])

    def desenhar(self, proj, view, sol_dir, cam_pos, dia):
        self.fbo.use()
        p = self.prog
        p["InvProj"].write(np.linalg.inv(proj).T.astype("f4").tobytes())
        p["InvView"].write(np.linalg.inv(view).T.astype("f4").tobytes())
        p["Proj"].write(proj.T.astype("f4").tobytes())
        p["View"].write(view.T.astype("f4").tobytes())
        p["SolDir"].value = tuple(float(x) for x in sol_dir)
        p["CamPos"].value = tuple(float(x) for x in cam_pos)
        p["Dia"].value = float(dia)
        self.vao.render(moderngl.TRIANGLE_STRIP)
