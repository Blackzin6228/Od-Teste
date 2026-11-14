"""Cena sintética da Vila Calada à noite, para as prévias de efeito (mesa Dev).

Casas de tábua com base de pedra e telhado escuro, rua de terra e cascalho, muro de pedra no fundo, uma lanterna
acesa numa porta, lua no céu e névoa do jogo. Tudo em blocos de 1 m (cara de Minecraft), com cor e profundidade
da mesma projeção do jogo, para o .fsh do mod ler igual lê no jogo.
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
uniform vec3 LuaDir;
uniform vec3 CamPos;     // posição da câmera no mundo
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

// Casa: base de pedra (1 bloco), parede de tábua, telhado em degraus (escada) ao longo de x.
void casa(vec3 ro, vec3 rd, vec3 c, vec3 tam, inout Hit h) {
    vec3 lo = c - vec3(tam.x, 0.0, tam.z) * 0.5;
    vec3 hi = c + vec3(tam.x * 0.5, tam.y, tam.z * 0.5);
    caixa(ro, rd, lo, vec3(hi.x, lo.y + 1.0, hi.z), 3, h);              // base
    caixa(ro, rd, vec3(lo.x, lo.y + 1.0, lo.z), hi, 2, h);               // parede
    for (int i = 0; i < 3; i++) {                                         // telhado
        float fi = float(i);
        float enc = fi * 1.0;
        caixa(ro, rd, vec3(lo.x - 0.5, hi.y + fi, lo.z - 0.5 + enc), vec3(hi.x + 0.5, hi.y + fi + 1.0, hi.z + 0.5 - enc), 4, h);
    }
    // porta escura e janela
    float xp = c.x < 0.0 ? hi.x : lo.x - 0.06;
    caixa(ro, rd, vec3(xp, lo.y + 1.0, c.z + 0.5), vec3(xp + 0.06, lo.y + 3.0, c.z + 1.5), 5, h);
    caixa(ro, rd, vec3(xp, lo.y + 2.0, c.z - 2.0), vec3(xp + 0.06, lo.y + 3.0, c.z - 1.0), 7, h);
}

vec3 albedo(int mat, vec3 p, vec3 n) {
    vec3 b = floor(p + n * 0.01 * -1.0 + 1e-3);
    float g = hash3(b) * 0.12;
    vec2 f = (abs(n.y) > 0.5) ? fract(p.xz) : (abs(n.x) > 0.5 ? fract(p.zy) : fract(p.xy));
    float borda = smoothstep(0.0, 0.06, min(min(f.x, 1.0 - f.x), min(f.y, 1.0 - f.y)));
    if (mat == 1) {           // rua: terra, cascalho e grama morta
        float k = hash(floor(p.xz * 1.0));
        vec3 terra = vec3(0.34, 0.25, 0.17);
        vec3 casc = vec3(0.40, 0.39, 0.37);
        vec3 grama = vec3(0.30, 0.33, 0.20);
        vec3 c = k < 0.45 ? terra : (k < 0.8 ? casc : grama);
        float px = hash(floor(p.xz * 8.0)) * 0.2;
        return c * (0.85 + px) * (0.9 + 0.1 * borda);
    }
    if (mat == 2) {           // tábua (abeto escuro)
        float tab = fract(p.y * 4.0);
        float risca = smoothstep(0.0, 0.08, tab) * smoothstep(1.0, 0.92, tab);
        float px = hash(floor(vec2((abs(n.x) > 0.5 ? p.z : p.x) * 8.0, p.y * 16.0))) * 0.15;
        return vec3(0.27, 0.19, 0.12) * (0.75 + 0.25 * risca + px) * (0.85 + g);
    }
    if (mat == 3 || mat == 6) { // pedregulho
        vec2 q = (abs(n.y) > 0.5) ? p.xz : (abs(n.x) > 0.5 ? p.zy : p.xy);
        float px = hash(floor(q * 8.0));
        float m = smoothstep(0.35, 0.7, ruido(q * 5.0));
        return vec3(0.38, 0.38, 0.38) * (0.55 + 0.45 * px) * (0.7 + 0.3 * m) * (0.85 + 0.15 * borda);
    }
    if (mat == 4) return vec3(0.17, 0.13, 0.11) * (0.8 + g * 2.0) * (0.8 + 0.2 * borda); // telhado
    if (mat == 5) return vec3(0.12, 0.08, 0.05);                                          // porta
    if (mat == 7) return vec3(0.02, 0.02, 0.025);                                         // janela escura
    return vec3(0.5);
}

vec3 ceu(vec3 rd) {
    float y = max(rd.y, 0.0);
    vec3 c = mix(vec3(0.050, 0.060, 0.090), vec3(0.012, 0.016, 0.032), smoothstep(0.0, 0.6, y));
    // estrelas
    vec2 q = rd.xz / max(rd.y, 0.08) * 60.0;
    float s = step(0.996, hash(floor(q))) * smoothstep(0.05, 0.3, y);
    c += vec3(0.8) * s * 0.6;
    // lua quadrada (como a do jogo)
    vec3 l = normalize(LuaDir);
    vec3 dx = normalize(cross(l, vec3(0, 1, 0.001)));
    vec3 dy = cross(dx, l);
    float dl = dot(rd, l);
    if (dl > 0.0) {
        vec2 m = vec2(dot(rd, dx), dot(rd, dy)) / dl;
        if (max(abs(m.x), abs(m.y)) < 0.036) {
            float cr = 0.82 + 0.18 * hash(floor(m * 140.0));
            c = vec3(0.86, 0.88, 0.92) * cr;
        }
        c += vec3(0.20, 0.22, 0.28) * pow(max(dl, 0.0), 300.0);
    }
    return c;
}

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
    // casas dos dois lados da rua (a rua corre em -z)
    casa(ro, rd, vec3(-9.0, CHAO, -8.0), vec3(6.0, 5.0, 7.0), h);
    casa(ro, rd, vec3(-9.5, CHAO, -21.0), vec3(7.0, 4.0, 8.0), h);
    casa(ro, rd, vec3(9.0, CHAO, -11.0), vec3(6.0, 6.0, 8.0), h);
    casa(ro, rd, vec3(10.0, CHAO, -25.0), vec3(7.0, 5.0, 7.0), h);
    // muro no fundo
    caixa(ro, rd, vec3(-40.0, CHAO, -38.0), vec3(40.0, CHAO + 4.0, -37.0), 6, h);
    // poço na rua
    caixa(ro, rd, vec3(-1.0, CHAO, -16.0), vec3(1.0, CHAO + 1.0, -14.0), 3, h);

    vec3 col;
    if (h.t > 1e8) {
        col = ceu(rd);
        prof = 1.0;
    } else {
        vec3 p = ro + rd * h.t;
        vec3 a = albedo(h.mat, p, h.n);
        // luz do jogo: céu de noite (fraca, fria), lanterna numa porta (quente), ambiente
        float lua = max(dot(h.n, normalize(LuaDir)), 0.0);
        vec3 luz = vec3(0.10, 0.11, 0.15) * (0.45 + 0.55 * lua) + vec3(0.035);
        vec3 lant = vec3(-5.6, CHAO + 2.6, -6.0);
        float dl = length(p - lant);
        float l2 = max(dot(h.n, normalize(lant - p)), 0.0) * 0.6 + 0.4;
        luz += vec3(1.0, 0.62, 0.30) * l2 * clamp(1.0 - dl / 11.0, 0.0, 1.0) * clamp(1.0 - dl / 11.0, 0.0, 1.0) * 1.3;
        // oclusão leve no pé das paredes
        if (h.n.y > 0.5) luz *= 0.85 + 0.15 * smoothstep(0.0, 0.0, 0.0);
        col = a * luz;
        // névoa do jogo (escura, de noite)
        float neb = smoothstep(18.0, 70.0, h.t);
        col = mix(col, vec3(0.040, 0.045, 0.065), neb);
        vec4 clip = Proj * View * vec4(p - ro, 1.0);
        prof = clamp(clip.z / clip.w * 0.5 + 0.5, 0.0, 0.99998);
    }
    // lanterna (ponto de luz visível, se nada estiver na frente)
    vec3 lant = vec3(-5.6, CHAO + 2.6, -6.0) - ro;
    float tl = dot(lant, rd);
    if (tl > 0.0 && tl < h.t) {
        float d = length(lant - rd * tl);
        col += vec3(1.0, 0.7, 0.35) * (smoothstep(0.12, 0.05, d) * 1.5 + exp(-d * 3.0) * 0.25);
    }
    cor = vec4(col, 1.0);
}
"""


class CenaVila:
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

    def desenhar(self, proj, view, lua_dir, cam_pos):
        self.fbo.use()
        p = self.prog
        p["InvProj"].write(np.linalg.inv(proj).T.astype("f4").tobytes())
        p["InvView"].write(np.linalg.inv(view).T.astype("f4").tobytes())
        p["Proj"].write(proj.T.astype("f4").tobytes())
        p["View"].write(view.T.astype("f4").tobytes())
        p["LuaDir"].value = tuple(float(x) for x in lua_dir)
        p["CamPos"].value = tuple(float(x) for x in cam_pos)
        self.vao.render(moderngl.TRIANGLE_STRIP)
