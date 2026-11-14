#!/usr/bin/env python3
"""Prévia de efeito de tela fora do jogo (mesa Dev).

Carrega O MESMO .vsh/.fsh do mod (resolvendo #moj_import), monta uma cena sintética com cor e profundidade
coerentes com InvProj/InvView (céu de fim de tarde, chão, colunas e árvores, câmera olhando um pouco para cima),
roda o shader com os uniforms ao longo do tempo e salva quadros e um GIF.

Uso:
    python3 ferramentas/fx/previa_shader.py olho [--saida arte/previas/fx/olho] [--rodada r1]

Precisa de: moderngl (com EGL), numpy, Pillow. Sem janela: usa o contexto EGL (llvmpipe serve).
"""
import argparse
import math
import os
import re
import sys

import moderngl
import numpy as np
from PIL import Image, ImageDraw

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SHADERS = os.path.join(RAIZ, "src", "main", "resources", "assets", "odteste", "shaders")

LARG, ALT = 1280, 720
FOV_Y = 70.0
PERTO, LONGE = 0.05, 512.0


# ---------------------------------------------------------------- shaders do mod

def ler_shader(nome_rl, tipo):
    """odteste:nome -> arquivo em shaders/core, resolvendo #moj_import <odteste:x.glsl> (shaders/include)."""
    ns, nome = nome_rl.split(":")
    caminho = os.path.join(SHADERS, "core", nome + "." + tipo)
    with open(caminho, encoding="utf-8") as f:
        texto = f.read()

    def troca(m):
        ns2, arq = m.group(1).split(":")
        with open(os.path.join(SHADERS, "include", arq), encoding="utf-8") as g:
            return g.read()

    return re.sub(r"#moj_import\s*<([^>]+)>", troca, texto)


def ler_programa(efeito):
    import json
    with open(os.path.join(SHADERS, "core", efeito + ".json"), encoding="utf-8") as f:
        js = json.load(f)
    return ler_shader(js["vertex"], "vsh"), ler_shader(js["fragment"], "fsh")


# ---------------------------------------------------------------- matrizes (iguais às do jogo)

def perspectiva(fov_y, aspecto, perto, longe):
    f = 1.0 / math.tan(math.radians(fov_y) / 2)
    m = np.zeros((4, 4), dtype=np.float32)
    m[0, 0] = f / aspecto
    m[1, 1] = f
    m[2, 2] = (longe + perto) / (perto - longe)
    m[2, 3] = 2 * longe * perto / (perto - longe)
    m[3, 2] = -1
    return m


def vista(yaw_graus, pitch_graus):
    """Matriz de vista só de rotação (câmera na origem), como o ModelViewMat do nível."""
    y, p = math.radians(yaw_graus), math.radians(pitch_graus)
    frente = np.array([math.sin(y) * math.cos(p), math.sin(p), -math.cos(y) * math.cos(p)])
    direita = np.cross(frente, [0, 1, 0])
    direita /= np.linalg.norm(direita)
    cima = np.cross(direita, frente)
    m = np.eye(4, dtype=np.float32)
    m[0, :3] = direita
    m[1, :3] = cima
    m[2, :3] = -frente
    return m, frente


def dir_de(yaw_graus, elev_graus):
    y, p = math.radians(yaw_graus), math.radians(elev_graus)
    return np.array([math.sin(y) * math.cos(p), math.sin(p), -math.cos(y) * math.cos(p)], dtype=np.float32)


def normal(v):
    v = np.asarray(v, dtype=np.float32)
    return v / max(np.linalg.norm(v), 1e-9)


# ---------------------------------------------------------------- cena sintética

CENA_VS = """
#version 330
in vec2 pos;
out vec2 uv;
void main() { uv = pos * 0.5 + 0.5; gl_Position = vec4(pos, 0.0, 1.0); }
"""

# Traça raios contra chão, colunas e árvores; escreve cor e profundidade [0,1] da mesma projeção do jogo.
CENA_FS = """
#version 330
in vec2 uv;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform mat4 Proj;
uniform mat4 View;
layout(location = 0) out vec4 cor;
layout(location = 1) out float prof;

const float CHAO = -1.62;

float hash(vec2 p) { return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453); }
float ruido(vec2 p) {
    vec2 i = floor(p), f = fract(p); f = f*f*(3.0-2.0*f);
    return mix(mix(hash(i), hash(i+vec2(1,0)), f.x), mix(hash(i+vec2(0,1)), hash(i+vec2(1,1)), f.x), f.y);
}

vec2 caixa(vec3 ro, vec3 rd, vec3 lo, vec3 hi, out vec3 n) {
    vec3 inv = 1.0 / rd;
    vec3 t0 = (lo - ro) * inv, t1 = (hi - ro) * inv;
    vec3 a = min(t0, t1), b = max(t0, t1);
    float tn = max(max(a.x, a.y), a.z), tf = min(min(b.x, b.y), b.z);
    n = vec3(0.0);
    if (tn == a.x) n = vec3(-sign(rd.x), 0, 0);
    else if (tn == a.y) n = vec3(0, -sign(rd.y), 0);
    else n = vec3(0, 0, -sign(rd.z));
    return vec2(tn, tf);
}

float esfera(vec3 ro, vec3 rd, vec3 c, float r) {
    vec3 oc = ro - c;
    float b = dot(oc, rd), k = dot(oc, oc) - r*r, h = b*b - k;
    if (h < 0.0) return -1.0;
    return -b - sqrt(h);
}

vec3 ceu(vec3 rd) {
    float y = rd.y;
    vec3 horiz = vec3(0.95, 0.55, 0.32);
    vec3 meio = vec3(0.55, 0.42, 0.55);
    vec3 alto = vec3(0.16, 0.20, 0.38);
    vec3 c = mix(horiz, meio, smoothstep(0.0, 0.25, y));
    c = mix(c, alto, smoothstep(0.2, 0.8, y));
    vec3 sol = normalize(vec3(-0.75, 0.08, -0.65));
    float s = max(dot(rd, sol), 0.0);
    c += vec3(1.0, 0.7, 0.4) * (pow(s, 400.0) * 3.0 + pow(s, 12.0) * 0.35);
    // nuvens finas
    vec2 p = rd.xz / max(rd.y, 0.05) * 1.5;
    float n = smoothstep(0.55, 0.8, ruido(p * 1.3) * 0.6 + ruido(p * 3.1) * 0.4);
    c = mix(c, vec3(0.85, 0.55, 0.45), n * 0.25 * smoothstep(0.02, 0.2, y));
    return c;
}

void main() {
    vec4 v = InvProj * vec4(uv * 2.0 - 1.0, 1.0, 1.0);
    vec3 rdv = normalize(v.xyz / v.w);
    vec3 rd = normalize((InvView * vec4(rdv, 0.0)).xyz);
    vec3 ro = vec3(0.0);
    float t = 1e9; vec3 n = vec3(0); vec3 alb = vec3(0);
    if (rd.y < 0.0) {
        float tc = CHAO / rd.y;
        if (tc < t) {
            t = tc; n = vec3(0,1,0);
            vec3 p = rd * tc;
            vec2 b = floor(p.xz);
            float g = hash(b) * 0.15;
            alb = vec3(0.30 + g, 0.42 + g, 0.20);
        }
    }
    // colunas de pedra
    for (int i = 0; i < 6; i++) {
        float fi = float(i);
        vec3 c = vec3(-14.0 + fi * 6.0 + (hash(vec2(fi, 2.0)) - 0.5) * 3.0, CHAO, -18.0 - hash(vec2(fi, 9.0)) * 14.0);
        vec3 nn; vec2 h = caixa(ro, rd, c + vec3(-1, 0, -1), c + vec3(1, 4.0 + hash(vec2(fi, 4.0)) * 6.0, 1), nn);
        if (h.x < h.y && h.x > 0.0 && h.x < t) { t = h.x; n = nn; alb = vec3(0.45, 0.44, 0.42); }
    }
    // árvores: tronco e copa
    for (int i = 0; i < 7; i++) {
        float fi = float(i);
        vec3 c = vec3(-24.0 + fi * 8.0 + (hash(vec2(fi, 7.0)) - 0.5) * 4.0, CHAO, -34.0 - hash(vec2(fi, 3.0)) * 20.0);
        float alt = 6.0 + hash(vec2(fi, 1.0)) * 6.0;
        vec3 nn; vec2 h = caixa(ro, rd, c + vec3(-0.5, 0, -0.5), c + vec3(0.5, alt, 0.5), nn);
        if (h.x < h.y && h.x > 0.0 && h.x < t) { t = h.x; n = nn; alb = vec3(0.30, 0.22, 0.14); }
        vec3 cc = c + vec3(0, alt + 1.5, 0);
        float te = esfera(ro, rd, cc, 3.2);
        if (te > 0.0 && te < t) { t = te; n = normalize(rd * te - cc); alb = vec3(0.18, 0.32, 0.12); }
    }
    vec3 col;
    if (t > 1e8) {
        col = ceu(rd);
        prof = 1.0;
    } else {
        vec3 p = rd * t;
        vec3 sol = normalize(vec3(-0.75, 0.25, -0.65));
        float dif = max(dot(n, sol), 0.0);
        col = alb * (0.35 + 0.9 * dif * vec3(1.0, 0.75, 0.55)) + alb * 0.2 * vec3(0.5, 0.5, 0.7);
        float neb = 1.0 - exp(-t * 0.012);
        col = mix(col, vec3(0.75, 0.52, 0.42), neb * 0.6);
        vec4 clip = Proj * View * vec4(p, 1.0);
        prof = clamp(clip.z / clip.w * 0.5 + 0.5, 0.0, 0.99998);
    }
    cor = vec4(col, 1.0);
}
"""


class Cena:
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

    def desenhar(self, proj, view):
        self.fbo.use()
        p = self.prog
        p["InvProj"].write(np.linalg.inv(proj).T.astype("f4").tobytes())
        p["InvView"].write(np.linalg.inv(view).T.astype("f4").tobytes())
        p["Proj"].write(proj.T.astype("f4").tobytes())
        p["View"].write(view.T.astype("f4").tobytes())
        self.vao.render(moderngl.TRIANGLE_STRIP)


class Efeito:
    """Roda o shader do mod sobre a cena, do jeito que o jogo roda (quadro inteiro, Sampler0 cor, Sampler1 prof.)."""

    def __init__(self, ctx, nome, larg, alt):
        vs, fs = ler_programa(nome)
        self.ctx = ctx
        self.prog = ctx.program(vertex_shader=vs, fragment_shader=fs)
        self.saida = ctx.texture((larg, alt), 4, dtype="f1")
        self.fbo = ctx.framebuffer(color_attachments=[self.saida])
        # Mesmo quadro do jogo: Position em pixels, UV0 de 0 a 1; aqui as matrizes deixam em NDC.
        dados = np.array([
            -1, -1, 0, 0, 0,
            1, -1, 0, 1, 0,
            -1, 1, 0, 0, 1,
            1, 1, 0, 1, 1,
        ], dtype="f4")
        self.vbo = ctx.buffer(dados.tobytes())
        self.vao = ctx.vertex_array(self.prog, [(self.vbo, "3f 2f", "Position", "UV0")])
        ident = np.eye(4, dtype="f4").tobytes()
        self.uniform("ModelViewMat", ident)
        self.uniform("ProjMat", ident)
        self.larg, self.alt = larg, alt

    def uniform(self, nome, valor):
        if nome not in self.prog:
            return
        u = self.prog[nome]
        if isinstance(valor, (bytes, bytearray)):
            u.write(valor)
        elif isinstance(valor, (list, tuple, np.ndarray)):
            u.value = tuple(float(x) for x in valor)
        else:
            u.value = float(valor)

    def desenhar(self, cena, proj, view, uniforms):
        self.fbo.use()
        cena.cor.use(0)
        cena.prof.use(1)
        if "Sampler0" in self.prog:
            self.prog["Sampler0"].value = 0
        if "Sampler1" in self.prog:
            self.prog["Sampler1"].value = 1
        self.uniform("InvProj", np.linalg.inv(proj).T.astype("f4").tobytes())
        self.uniform("InvView", np.linalg.inv(view).T.astype("f4").tobytes())
        self.uniform("ScreenSize", (self.larg, self.alt))
        for k, v in uniforms.items():
            self.uniform(k, v)
        self.vao.render(moderngl.TRIANGLE_STRIP)
        img = Image.frombytes("RGBA", (self.larg, self.alt), self.fbo.read(components=4))
        return img.transpose(Image.FLIP_TOP_BOTTOM).convert("RGB")


# ---------------------------------------------------------------- linha do tempo do Olho (igual ao OlhoNoCeu.java)

ABRIR, FECHAR = 3.0, 2.0


def curvas_olho(t, aberto):
    """Fenda e Abertura (0..1) no tempo t, com o olho aberto por `aberto` segundos depois de abrir."""
    if t < ABRIR:
        return min(t / 1.2, 1.0), max(0.0, min((t - 1.0) / 2.0, 1.0))
    s = t - ABRIR - aberto
    if s < 0:
        return 1.0, 1.0
    return max(0.0, 1.0 - s / FECHAR), max(0.0, 1.0 - s / 1.2)


def rodar_olho(saida, rodada):
    ctx = moderngl.create_standalone_context(backend="egl", require=330)
    cena = Cena(ctx, LARG, ALT)
    fx = Efeito(ctx, "odfx_olho", LARG, ALT)
    proj = perspectiva(FOV_Y, LARG / ALT, PERTO, LONGE)
    view, frente = vista(0.0, 14.0)
    cena.desenhar(proj, view)

    olho_dir = dir_de(4.0, 36.0)            # alto no céu, um pouco à direita
    olho_pos = olho_dir * 300.0             # fixo no mundo (relativo à câmera de partida)
    aberto = 6.0
    total = ABRIR + aberto + FECHAR
    fps = 10
    quadros = []
    # Para onde ele olha: começa olhando para longe, acha o jogador (a câmera) em ~3,5 s,
    # no meio desvia para o lado (como se o jogador tivesse corrido) e volta.
    longe = np.array([-60.0, -40.0, -120.0], dtype=np.float32)
    lado = np.array([45.0, -1.6, -25.0], dtype=np.float32)
    jogador = np.array([0.0, 0.0, 0.0], dtype=np.float32)
    olhar_pt = longe.copy()
    dt = 1.0 / fps
    chave = {}
    n = int(total * fps) + 1
    for i in range(n):
        t = i * dt
        if t < 3.5:
            alvo = longe
        elif 6.0 <= t < 8.0:
            alvo = lado
        else:
            alvo = jogador
        taxa = 3.2 if t >= 3.5 and t < 4.2 else 1.8
        olhar_pt = olhar_pt + (alvo - olhar_pt) * (1.0 - math.exp(-dt * taxa))
        olhar = normal(olhar_pt - olho_pos)
        chao = olhar_pt.copy()
        chao[1] = -1.62
        fenda, abertura = curvas_olho(t, aberto)
        img = fx.desenhar(cena, proj, view, {
            "Tempo": t, "Fenda": fenda, "Abertura": abertura,
            "OlhoDir": tuple(olho_dir), "Olhar": tuple(olhar), "Alvo": tuple(chao),
        })
        quadros.append(img)
        for nome, tc in (("1_abrindo", 1.8), ("2_aberto_camera", 5.5), ("3_olhando_lado", 7.6), ("4_fechando", 10.0)):
            if abs(t - tc) < dt / 2:
                chave[nome] = img

    os.makedirs(saida, exist_ok=True)
    pref = (rodada + "_") if rodada else ""
    for nome, img in chave.items():
        img.save(os.path.join(saida, pref + nome + ".png"))
    # Cena sem efeito, para comparar.
    cena_img = fx.desenhar(cena, proj, view, {"Tempo": 0.0, "Fenda": 0.0, "Abertura": 0.0,
                                              "OlhoDir": tuple(olho_dir), "Olhar": tuple(-olho_dir),
                                              "Alvo": (0, -1.62, 0)})
    cena_img.save(os.path.join(saida, pref + "0_sem_efeito.png"))
    # Zoom no olho aberto.
    if "2_aberto_camera" in chave:
        z = chave["2_aberto_camera"]
        w, h = z.size
        z.crop((int(w * 0.30), 0, int(w * 0.70), int(h * 0.40))).resize((1280, 720), Image.LANCZOS).save(
            os.path.join(saida, pref + "5_zoom_olho.png"))
    pequenos = [q.resize((640, 360), Image.LANCZOS) for q in quadros]
    pequenos[0].save(os.path.join(saida, pref + "olho.gif"), save_all=True, append_images=pequenos[1:],
                     duration=int(1000 / fps), loop=0, optimize=True)
    print("ok:", len(quadros), "quadros ->", saida)


def folha(saida, rodadas, nome="comparacao_rodadas.png"):
    """Folha lado a lado: uma linha por rodada (aberto olhando para a câmera e zoom)."""
    linhas = []
    for r in rodadas:
        a = os.path.join(saida, r + "_2_aberto_camera.png")
        z = os.path.join(saida, r + "_5_zoom_olho.png")
        if not (os.path.exists(a) and os.path.exists(z)):
            continue
        linhas.append((r, Image.open(a).resize((640, 360)), Image.open(z).resize((640, 360))))
    if not linhas:
        return
    img = Image.new("RGB", (640 + 640 + 30, len(linhas) * 390 + 10), (12, 12, 12))
    d = ImageDraw.Draw(img)
    for i, (r, a, z) in enumerate(linhas):
        y = 10 + i * 390
        img.paste(a, (10, y + 20))
        img.paste(z, (660, y + 20))
        d.text((12, y + 2), "rodada " + r, fill=(245, 220, 122))
    img.save(os.path.join(saida, nome))
    print("folha:", os.path.join(saida, nome))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("efeito", choices=["olho", "folha"])
    ap.add_argument("--saida", default=os.path.join(RAIZ, "arte", "previas", "fx", "olho"))
    ap.add_argument("--rodada", default="")
    ap.add_argument("--rodadas", default="")
    a = ap.parse_args()
    if a.efeito == "olho":
        rodar_olho(a.saida, a.rodada)
    else:
        folha(a.saida, [r for r in a.rodadas.split(",") if r])


if __name__ == "__main__":
    sys.exit(main())
