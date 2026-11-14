"""Prévia em PNG com OpenGL (moderngl, sem janela): luz de bloco e de céu como no jogo, noite e névoa.

Mais rápida e mais legível que render.py (que segue valendo para a planta por andar). Usa as mesmas
"texturas" procedurais de render.textura (não usa textura do Minecraft: é prévia de forma, cor e luz).

- iso(est, angulo, ...): vista isométrica de um dos 4 cantos (0 = sudeste, 1 = nordeste, 2 = noroeste, 3 = sudoeste).
- longe(est, angulo, ...): câmera em perspectiva, do chão e de longe (como o jogador chega).
- noite=True: céu escuro, lua e névoa carmesim; só a luz dos blocos (lanterna, vela, fogueira) ilumina.

Luz: a mesma regra do jogo (nível 0 a 15, perde 1 por bloco, parede opaca segura), calculada numa grade.
Precisa de: numpy, Pillow, moderngl (contexto EGL, sem tela).
"""
import math

import numpy as np
from PIL import Image

import blocos as B
import render as R

_CTX = None
VARIANTES = 4

# luz que cada bloco emite (nível do jogo)
def _emite(nome, p):
    n = B.curto(nome)
    if n == "lantern":
        return 15
    if n.endswith("candle"):
        return 3 * int(p.get("candles", "1")) if p.get("lit") == "true" else 0
    if n == "campfire":
        return 15 if p.get("lit") == "true" else 0
    if n == "soul_campfire":
        return 10 if p.get("lit") == "true" else 0
    if n == "light":
        return int(p.get("level", "15"))
    if n == "redstone_torch":
        return 7
    return 0


def _ctx():
    global _CTX
    if _CTX is None:
        import moderngl
        _CTX = moderngl.create_standalone_context(backend="egl")
    return _CTX


# ---------------------------------------------------------------------------
# Atlas de texturas (array 2D: uma camada por material x face x variante)
# ---------------------------------------------------------------------------
class _Atlas:
    def __init__(self):
        self.indice = {}
        self.camadas = []
        self.emissivo = set()

    def camada(self, mat, face, hb):
        topo = 1 if face == "topo" else 0
        v = hb % VARIANTES
        chave = (mat, topo, v)
        if chave not in self.indice:
            cor, padrao = B.material(mat)
            img = np.zeros((16, 16, 4), np.uint8)
            for tv in range(16):
                for tu in range(16):
                    c = R.textura(padrao, cor, tu, tv, 1000003 * (v + 1) + 17, "topo" if topo else "sul")
                    if c is None:
                        continue
                    r, g, b, a, luz = c
                    img[tv, tu] = (r, g, b, int(255 * a))
            self.indice[chave] = len(self.camadas)
            self.camadas.append(img)
            if padrao == "luz":
                self.emissivo.add(mat)
        return self.indice[chave]


# ---------------------------------------------------------------------------
# Luz em grade (céu e bloco), com margem em volta da estrutura
# ---------------------------------------------------------------------------
M = 6  # margem (blocos) em volta para o céu entrar pelos lados


def _grades(blocos, tam, y_chao):
    sx, sy, sz = tam
    gx, gy, gz = sx + 2 * M, sy + M, sz + 2 * M
    opaco = np.zeros((gx, gy, gz), bool)
    fonte = np.zeros((gx, gy, gz), np.int16)
    for (x, y, z), (n, p) in blocos.items():
        if B.cheio(n, p) or B.curto(n).endswith(("_slab", "_stairs")):
            opaco[x + M, y, z + M] = True
        e = _emite(n, p)
        if e:
            fonte[x + M, y, z + M] = e
    # chão do mundo: abaixo de y_chao é terra (fora das colunas da estrutura)
    terra = np.zeros_like(opaco)
    terra[:, :y_chao + 1, :] = True
    ocupado = np.zeros((gx, gz), bool)
    for (x, y, z) in blocos:
        if y <= y_chao:
            ocupado[x + M, z + M] = True
    for y in range(y_chao + 1):
        terra[:, y, :] &= ~ocupado | opaco[:, y, :]
    bloqueia = opaco | terra
    livre = ~bloqueia

    # céu: de cima para baixo até achar bloco
    ceu = np.zeros((gx, gy, gz), np.int16)
    aberto = np.cumprod(livre[:, ::-1, :], axis=1)[:, ::-1, :].astype(bool)
    ceu[aberto] = 15
    ceu = _espalhar(ceu, livre)
    blk = _espalhar(fonte.copy(), livre | (fonte > 0))
    return ceu, blk, bloqueia


def _espalhar(L, livre):
    for _ in range(15):
        n = L.copy()
        for eixo in range(3):
            for d in (1, -1):
                s = np.roll(L, d, axis=eixo)
                # nada entra pela borda do outro lado
                idx = [slice(None)] * 3
                idx[eixo] = 0 if d == 1 else -1
                s[tuple(idx)] = 0
                n = np.maximum(n, s - 1)
        n[~livre] = 0
        n = np.maximum(n, 0)
        if np.array_equal(n, L):
            break
        L = np.maximum(L, n)
    return L


# ---------------------------------------------------------------------------
# Malha
# ---------------------------------------------------------------------------
# face: (nome, normal, origem relativa na caixa, eixo u, eixo v, sombra do jogo)
_FACES = {
    "topo": ((0, 1, 0), 1.00),
    "baixo": ((0, -1, 0), 0.50),
    "norte": ((0, 0, -1), 0.80),
    "sul": ((0, 0, 1), 0.80),
    "oeste": ((-1, 0, 0), 0.60),
    "leste": ((1, 0, 0), 0.60),
}


def _quad(face, c):
    """4 cantos (x, y, z) e (u, v) em texel 0..1 da face da caixa c = (x0,y0,z0,x1,y1,z1), ordem anti-horária."""
    x0, y0, z0, x1, y1, z1 = c
    if face == "topo":
        return [((x0, y1, z0), (x0, z0)), ((x0, y1, z1), (x0, z1)), ((x1, y1, z1), (x1, z1)), ((x1, y1, z0), (x1, z0))]
    if face == "baixo":
        return [((x0, y0, z0), (x0, z0)), ((x1, y0, z0), (x1, z0)), ((x1, y0, z1), (x1, z1)), ((x0, y0, z1), (x0, z1))]
    if face == "sul":
        return [((x0, y0, z1), (x0, 1 - y0)), ((x1, y0, z1), (x1, 1 - y0)), ((x1, y1, z1), (x1, 1 - y1)), ((x0, y1, z1), (x0, 1 - y1))]
    if face == "norte":
        return [((x1, y0, z0), (1 - x1, 1 - y0)), ((x0, y0, z0), (1 - x0, 1 - y0)), ((x0, y1, z0), (1 - x0, 1 - y1)), ((x1, y1, z0), (1 - x1, 1 - y1))]
    if face == "leste":
        return [((x1, y0, z1), (1 - z1, 1 - y0)), ((x1, y0, z0), (1 - z0, 1 - y0)), ((x1, y1, z0), (1 - z0, 1 - y1)), ((x1, y1, z1), (1 - z1, 1 - y1))]
    # oeste
    return [((x0, y0, z0), (z0, 1 - y0)), ((x0, y0, z1), (z1, 1 - y0)), ((x0, y1, z1), (z1, 1 - y1)), ((x0, y1, z0), (z0, 1 - y1))]


COR_MARCADOR = {"bau": (255, 200, 40), "atril": (255, 200, 40), "disco": (255, 200, 40), "barril": (255, 200, 40),
                "spawn": (255, 40, 40), "rota": (120, 255, 90), "som": (60, 230, 255), "evento": (255, 70, 230),
                "mapa": (250, 250, 250)}


def _cor_marcador(meta):
    return COR_MARCADOR.get(meta.split(":")[0], (90, 120, 255))


def _malha(est, y_max=None, x_corte=None, y_min=None, marcadores=False):
    blocos = {}
    for pos, (n, props) in est.blocos.items():
        if n == "minecraft:air":
            continue
        if y_max is not None and pos[1] > y_max:
            continue
        if x_corte is not None and pos[0] > x_corte:
            continue
        if y_min is not None and pos[1] < y_min:
            continue
        blocos[pos] = (n, dict(props))
    y_chao = getattr(est, "y_chao", 0)
    ceu, blk, bloqueia = _grades(blocos, est.tam, y_chao)
    gx, gy, gz = ceu.shape
    opacos = {pos for pos, (n, p) in blocos.items() if B.cheio(n, p)}
    atlas = _Atlas()
    verts = []  # pos3 uv2 layer shade sky blk emis

    def luz(x, y, z):
        i, j, k = x + M, y, z + M
        if 0 <= i < gx and 0 <= j < gy and 0 <= k < gz:
            if bloqueia[i, j, k]:
                return None
            return ceu[i, j, k], blk[i, j, k]
        if j >= gy:
            return 15, 0
        return None

    for (x, y, z), (n, p) in blocos.items():
        hb = int(R.hash01(x, y, z) * 1e9)
        formas = B.formas(n, p)
        cheia_bloco = (x, y, z) in opacos
        proprio = luz(x, y, z) if not cheia_bloco else None
        for caixa, mat in formas:
            emis = 1.0 if B.material(mat)[1] == "luz" else 0.0
            cheia = caixa == B.CHEIA and cheia_bloco
            for face, (nv, sombra) in _FACES.items():
                nx, ny, nz = nv
                if cheia and (x + nx, y + ny, z + nz) in opacos:
                    continue
                if not cheia:
                    # caixa parcial: some se a face fica colada num vizinho opaco
                    enc = {"topo": caixa[4] >= 1, "baixo": caixa[1] <= 0, "norte": caixa[2] <= 0,
                           "sul": caixa[5] >= 1, "oeste": caixa[0] <= 0, "leste": caixa[3] >= 1}[face]
                    if enc and (x + nx, y + ny, z + nz) in opacos:
                        continue
                camada = atlas.camada(mat, "topo" if face in ("topo", "baixo") else "lado", hb)
                cantos = _quad(face, caixa)
                frente = (x + nx, y + ny, z + nz)
                for (cx, cy, cz), (u, v) in cantos:
                    if cheia:
                        # canto do vértice no plano da frente: vizinhos laterais
                        ts = []
                        for eixo, val in ((0, cx), (1, cy), (2, cz)):
                            if nv[eixo] != 0:
                                continue
                            ts.append((eixo, 1 if val >= 1 else -1))
                        (e1, s1), (e2, s2) = ts
                        d1 = [0, 0, 0]
                        d1[e1] = s1
                        d2 = [0, 0, 0]
                        d2[e2] = s2
                        a = (frente[0] + d1[0], frente[1] + d1[1], frente[2] + d1[2])
                        b = (frente[0] + d2[0], frente[1] + d2[1], frente[2] + d2[2])
                        c = (frente[0] + d1[0] + d2[0], frente[1] + d1[1] + d2[1], frente[2] + d1[2] + d2[2])
                        oa, ob, oc = a in opacos, b in opacos, c in opacos
                        ao = 0 if (oa and ob) else 3 - (oa + ob + oc)
                        amostras = [luz(*frente)]
                        if not oa:
                            amostras.append(luz(*a))
                        if not ob:
                            amostras.append(luz(*b))
                        if not oc and not (oa and ob):
                            amostras.append(luz(*c))
                        amostras = [s for s in amostras if s is not None]
                        if amostras:
                            sk = sum(s[0] for s in amostras) / len(amostras)
                            bl = sum(s[1] for s in amostras) / len(amostras)
                        else:
                            sk, bl = 0, 0
                        f = sombra * (0.55 + 0.15 * ao)
                    else:
                        s = proprio or luz(*frente) or (0, 0)
                        sk, bl = s
                        f = sombra
                    verts.append((x + cx, y + cy, z + cz, u, v, camada, f, sk / 15.0, bl / 15.0, emis))
    if marcadores:
        for mk in est.marcadores:
            x, y, z = mk["pos"]
            if (x, y, z) not in blocos:
                continue
            cor = _cor_marcador(mk["metadata"])
            chave = "__m_%d_%d_%d" % cor
            B.EXTRA_PREVIA.setdefault(chave, (cor, "luz"))
            caixa = (.22, .22, .22, .78, .78, .78)
            for face in _FACES:
                camada = atlas.camada(chave, "lado", 0)
                for (cx, cy, cz), (u, v) in _quad(face, caixa):
                    verts.append((x + cx, y + cy, z + cz, u, v, camada, 1.0, 1.0, 1.0, 1.0))
    arr = np.array(verts, np.float32).reshape(-1, 4, 10)
    tri = arr[:, [0, 1, 2, 0, 2, 3], :].reshape(-1, 10)
    texturas = np.stack(atlas.camadas) if atlas.camadas else np.zeros((1, 16, 16, 4), np.uint8)
    return tri, texturas, blocos


# ---------------------------------------------------------------------------
# Shaders
# ---------------------------------------------------------------------------
_VS = """
#version 330
uniform mat4 mvp;
uniform mat4 vista;
in vec3 in_pos; in vec2 in_uv; in float in_layer; in float in_shade; in float in_sky; in float in_blk; in float in_emis;
out vec2 uv; flat out float layer; out float shade; out float sky; out float blk; out float emis; out vec3 mundo; out float prof;
void main(){
  gl_Position = mvp * vec4(in_pos, 1.0);
  uv = in_uv; layer = in_layer; shade = in_shade; sky = in_sky; blk = in_blk; emis = in_emis; mundo = in_pos;
  prof = -(vista * vec4(in_pos, 1.0)).z;
}
"""

_FS = """
#version 330
uniform sampler2DArray tex;
uniform int noite; uniform int passe; uniform float y_chao; uniform float prof0; uniform float dens; uniform float nevoa_baixa;
uniform vec3 cor_nevoa;
in vec2 uv; flat in float layer; in float shade; in float sky; in float blk; in float emis; in vec3 mundo; in float prof;
out vec4 cor;
float curva(float l){ return l / (4.0 - 3.0 * l); }
void main(){
  vec4 t = texture(tex, vec3(clamp(uv, 0.0, 0.9999), layer));
  if (t.a < 0.08) discard;
  if (passe == 0 && t.a < 0.95) discard;
  if (passe == 1 && t.a >= 0.95) discard;
  vec3 c = t.rgb;
  vec3 luz;
  if (emis > 0.5) { luz = vec3(1.25); }
  else if (noite == 1) {
    vec3 lua = vec3(0.40, 0.19, 0.21) * curva(sky);
    vec3 fogo = vec3(1.25, 0.82, 0.48) * curva(blk);
    luz = (vec3(0.035, 0.028, 0.035) + lua + fogo) * shade;
  } else {
    float l = max(curva(sky) * 1.0, curva(blk) * 0.95);
    luz = vec3(0.13 + 0.92 * l) * shade;
  }
  c *= luz;
  if (noite == 1) {
    float d = max(prof - prof0, 0.0);
    float f = 1.0 - exp(-dens * d);
    float h = clamp((mundo.y - y_chao) / 5.0, 0.0, 1.0);
    f = max(f, nevoa_baixa * (1.0 - h) * (1.0 - h));
    if (emis > 0.5) f *= 0.35;
    c = mix(c, cor_nevoa, clamp(f, 0.0, 0.92));
  }
  cor = vec4(c, passe == 1 ? t.a : 1.0);
}
"""

_VS_FUNDO = """
#version 330
in vec2 in_pos; out vec2 p;
void main(){ p = in_pos; gl_Position = vec4(in_pos, 0.9999, 1.0); }
"""

_FS_FUNDO = """
#version 330
uniform int noite; uniform vec3 cor_nevoa; uniform vec2 lua; uniform float aspecto;
in vec2 p; out vec4 cor;
float h(vec2 q){ return fract(sin(dot(q, vec2(12.9898,78.233))) * 43758.5453); }
void main(){
  if (noite == 0) { cor = vec4(30.0/255.0, 32.0/255.0, 36.0/255.0, 1.0); return; }
  float t = clamp(p.y * 0.5 + 0.5, 0.0, 1.0);
  vec3 alto = vec3(0.012, 0.004, 0.008);
  vec3 c = mix(cor_nevoa * 1.6, alto, pow(t, 0.42));
  vec2 q = vec2((p.x - lua.x) * aspecto, p.y - lua.y);
  float d = length(q);
  c += vec3(0.62, 0.13, 0.12) * smoothstep(0.055, 0.050, d);
  c += vec3(0.30, 0.04, 0.05) * exp(-d * 6.0) * 0.6;
  vec2 g = floor(p * vec2(aspecto, 1.0) * 220.0);
  if (h(g) > 0.9975 && t > 0.55) c += vec3(0.25, 0.18, 0.18) * (t - 0.5);
  cor = vec4(c, 1.0);
}
"""

_FS_CHAO = """
#version 330
uniform int noite; uniform float y_chao; uniform float prof0; uniform float dens; uniform float nevoa_baixa;
uniform vec3 cor_nevoa;
in vec3 mundo; in float prof; out vec4 cor;
float h(vec2 q){ return fract(sin(dot(q, vec2(12.9898,78.233))) * 43758.5453); }
void main(){
  vec2 b = floor(mundo.xz);
  float r = h(b);
  vec3 c = mix(vec3(0.25, 0.21, 0.17), vec3(0.22, 0.22, 0.17), step(0.55, h(floor(b / 3.0))));
  c *= 0.78 + 0.3 * r;
  if (noite == 1) {
    c *= vec3(0.30, 0.13, 0.15) + vec3(0.035);
    float d = max(prof - prof0, 0.0);
    float f = max(1.0 - exp(-dens * d), nevoa_baixa);
    c = mix(c, cor_nevoa, clamp(f, 0.0, 0.95));
  }
  cor = vec4(c, 1.0);
}
"""

_VS_CHAO = """
#version 330
uniform mat4 mvp; uniform mat4 vista;
in vec3 in_pos; out vec3 mundo; out float prof;
void main(){ gl_Position = mvp * vec4(in_pos, 1.0); mundo = in_pos; prof = -(vista * vec4(in_pos, 1.0)).z; }
"""


# ---------------------------------------------------------------------------
# Matrizes
# ---------------------------------------------------------------------------
def _olhar(olho, alvo, cima=(0, 1, 0)):
    olho, alvo, cima = (np.array(v, np.float64) for v in (olho, alvo, cima))
    f = alvo - olho
    f /= np.linalg.norm(f)
    s = np.cross(f, cima)
    s /= np.linalg.norm(s)
    u = np.cross(s, f)
    m = np.identity(4)
    m[0, :3], m[1, :3], m[2, :3] = s, u, -f
    m[:3, 3] = -m[:3, :3] @ olho
    return m


def _orto(l, r, b, t, n, f):
    m = np.identity(4)
    m[0, 0], m[1, 1], m[2, 2] = 2 / (r - l), 2 / (t - b), -2 / (f - n)
    m[0, 3], m[1, 3], m[2, 3] = -(r + l) / (r - l), -(t + b) / (t - b), -(f + n) / (f - n)
    return m


def _persp(fov, asp, n, f):
    k = 1 / math.tan(math.radians(fov) / 2)
    m = np.zeros((4, 4))
    m[0, 0], m[1, 1] = k / asp, k
    m[2, 2], m[2, 3] = (f + n) / (n - f), 2 * f * n / (n - f)
    m[3, 2] = -1
    return m


# ---------------------------------------------------------------------------
# Desenho
# ---------------------------------------------------------------------------
def _desenhar(est, vista, proj, w, h, noite, y_max=None, chao=True, nevoa=None, x_corte=None, ss=2,
              y_min=None, marcadores=False):
    ctx = _ctx()
    tri, texturas, blocos = _malha(est, y_max, x_corte, y_min, marcadores)
    W, H = w * ss, h * ss
    fbo = ctx.framebuffer(color_attachments=[ctx.texture((W, H), 4)], depth_attachment=ctx.depth_texture((W, H)))
    fbo.use()
    ctx.viewport = (0, 0, W, H)
    ctx.clear(0, 0, 0, 1)
    mvp = (proj @ vista).astype("f4")
    nev = dict(prof0=0.0, dens=0.0, nevoa_baixa=0.0, cor_nevoa=(0.20, 0.025, 0.035))
    if nevoa:
        nev.update(nevoa)
    y_chao = float(getattr(est, "y_chao", 0)) + 1.0

    def uni(prog, **kv):
        for k, v in kv.items():
            if k in prog:
                prog[k].value = v

    # fundo
    pf = ctx.program(vertex_shader=_VS_FUNDO, fragment_shader=_FS_FUNDO)
    uni(pf, noite=int(noite), cor_nevoa=nev["cor_nevoa"], lua=(0.55, 0.62), aspecto=W / H)
    q = ctx.buffer(np.array([-1, -1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1], "f4"))
    ctx.disable(ctx.DEPTH_TEST)
    ctx.vertex_array(pf, [(q, "2f", "in_pos")]).render()
    ctx.enable(ctx.DEPTH_TEST)

    comuns = dict(noite=int(noite), y_chao=y_chao, prof0=float(nev["prof0"]), dens=float(nev["dens"]),
                  nevoa_baixa=float(nev["nevoa_baixa"]), cor_nevoa=nev["cor_nevoa"])
    if chao:
        pc = ctx.program(vertex_shader=_VS_CHAO, fragment_shader=_FS_CHAO)
        pc["mvp"].write(mvp.T.copy().tobytes())
        pc["vista"].write(vista.astype("f4").T.copy().tobytes())
        uni(pc, **comuns)
        sx, _, sz = est.tam
        R_ = 400
        yc = y_chao - 0.02
        quads = [(-R_, -R_, sx + R_, 0), (-R_, sz, sx + R_, sz + R_), (-R_, 0, 0, sz), (sx, 0, sx + R_, sz)]
        yb = int(y_chao) - 1
        for x in range(sx):
            for z in range(sz):
                if (x, yb, z) not in blocos and est.blocos.get((x, yb, z), ("",))[0] != "minecraft:air":
                    quads.append((x, z, x + 1, z + 1))
        v = []
        for a, b, c, d in quads:
            v += [a, yc, b, c, yc, b, c, yc, d, a, yc, b, c, yc, d, a, yc, d]
        chao_v = np.array(v, "f4")
        ctx.vertex_array(pc, [(ctx.buffer(chao_v), "3f", "in_pos")]).render()

    if len(tri):
        tex = ctx.texture_array((16, 16, len(texturas)), 4, texturas.tobytes())
        import moderngl
        tex.filter = (moderngl.NEAREST, moderngl.NEAREST)
        tex.use(0)
        prog = ctx.program(vertex_shader=_VS, fragment_shader=_FS)
        prog["mvp"].write(mvp.T.copy().tobytes())
        prog["vista"].write(vista.astype("f4").T.copy().tobytes())
        uni(prog, tex=0, **comuns)
        vbo = ctx.buffer(tri.astype("f4").tobytes())
        vao = ctx.vertex_array(prog, [(vbo, "3f 2f 1f 1f 1f 1f 1f",
                                       "in_pos", "in_uv", "in_layer", "in_shade", "in_sky", "in_blk", "in_emis")])
        prog["passe"].value = 0
        vao.render()
        ctx.enable(ctx.BLEND)
        ctx.blend_func = ctx.SRC_ALPHA, ctx.ONE_MINUS_SRC_ALPHA
        ctx.depth_mask = False
        prog["passe"].value = 1
        vao.render()
        ctx.depth_mask = True
        ctx.disable(ctx.BLEND)
    dados = fbo.read(components=3)
    img = Image.frombytes("RGB", (W, H), dados).transpose(Image.FLIP_TOP_BOTTOM)
    if noite:
        prof = np.frombuffer(fbo.depth_attachment.read(), np.float32).reshape(H, W)[::-1]
        img = _halos(img, est, blocos, mvp, W, H, prof, proj)
    fbo.release()
    img = img.resize((w, h), Image.LANCZOS)
    if marcadores:
        img = _rotulos(img, est, mvp, w, h)
    return img


def _rotulos(img, est, mvp, w, h):
    """Marcadores por cima de tudo (como raio X), com o nome: para a Dev achar cada ponto."""
    from PIL import ImageDraw, ImageFont
    try:
        fonte = ImageFont.truetype("DejaVuSans.ttf", 11)
    except OSError:
        fonte = None
    d = ImageDraw.Draw(img, "RGBA")
    usados = []
    for mk in sorted(est.marcadores, key=lambda k: -k["pos"][1]):
        x, y, z = mk["pos"]
        v = mvp @ np.array([x + .5, y + .5, z + .5, 1.0])
        if v[3] <= 0:
            continue
        sx = (v[0] / v[3] * .5 + .5) * w
        sy = (1 - (v[1] / v[3] * .5 + .5)) * h
        cor = _cor_marcador(mk["metadata"])
        d.ellipse([sx - 5, sy - 5, sx + 5, sy + 5], fill=cor + (255,), outline=(0, 0, 0, 255))
        ty = sy - 7
        while any(abs(ty - uy) < 12 and abs(sx - ux) < 140 for ux, uy in usados):
            ty -= 12
        usados.append((sx, ty))
        if ty != sy - 7:
            d.line([(sx, sy), (sx + 8, ty + 6)], fill=cor + (200,))
        d.rectangle([sx + 8, ty, sx + 12 + 6.2 * len(mk["metadata"]), ty + 13], fill=(0, 0, 0, 170))
        d.text((sx + 10, ty), mk["metadata"], fill=cor + (255,), font=fonte)
    return img


def _linear(d, proj):
    """Profundidade do buffer (0..1) para distância da câmera."""
    zn = d * 2 - 1
    if proj[3, 3] == 1:  # ortográfica
        return (zn - proj[2, 3]) / proj[2, 2]
    return proj[2, 3] / (zn + proj[2, 2])


def _halos(img, est, blocos, mvp, W, H, prof, proj):
    """Brilho em volta das luzes que se veem (lanterna, vela acesa): só para ler a luz de longe."""
    a = np.asarray(img).astype(np.float32)
    yy, xx = np.mgrid[0:H, 0:W]
    for (x, y, z), (n, p) in blocos.items():
        e = _emite(n, p)
        if not e or B.curto(n) == "light":
            continue
        v = mvp @ np.array([x + .5, y + .45, z + .5, 1.0])
        if v[3] <= 0:
            continue
        sx = (v[0] / v[3] * .5 + .5) * W
        sy = (1 - (v[1] / v[3] * .5 + .5)) * H
        ix, iy = int(sx), int(sy)
        if not (0 <= ix < W and 0 <= iy < H):
            continue
        d_luz = abs(_linear(v[2] / v[3] * .5 + .5, proj))
        janela = prof[max(0, iy - 2):iy + 3, max(0, ix - 2):ix + 3]
        d_buf = abs(_linear(janela.max(), proj))
        if d_buf < d_luz - 0.9:
            continue  # tem parede na frente
        r = (2.5 + e * 0.55) * (W / 1000)
        x0, x1 = int(max(0, sx - 4 * r)), int(min(W, sx + 4 * r))
        y0, y1 = int(max(0, sy - 4 * r)), int(min(H, sy + 4 * r))
        if x0 >= x1 or y0 >= y1:
            continue
        d2 = (xx[y0:y1, x0:x1] - sx) ** 2 + (yy[y0:y1, x0:x1] - sy) ** 2
        g = np.exp(-d2 / (2 * r * r)) * (0.05 + 0.01 * e)
        cor = np.array([255, 140, 70], np.float32) if "red" not in n else np.array([255, 60, 50], np.float32)
        a[y0:y1, x0:x1] += g[..., None] * cor
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8))


def _limites(est, y_max):
    sx, sy, sz = est.tam
    ys = [p[1] for p in est.blocos if y_max is None or p[1] <= y_max] or [0]
    return (0, min(ys), 0), (sx, max(ys) + 1, sz)


def iso(est, angulo=0, y_max=None, px_bloco=14, noite=False, chao=True, x_corte=None, larg_max=1800,
        marcadores=False, y_min=None):
    """angulo: 0 = de sudeste, 1 = de nordeste, 2 = de noroeste, 3 = de sudoeste."""
    (x0, y0, z0), (x1, y1, z1) = _limites(est, y_max)
    cx, cy, cz = (x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2
    ang = math.radians(45 + 90 * angulo)
    # 0 = sudeste: câmera em +x +z
    dx, dz = {0: (1, 1), 1: (1, -1), 2: (-1, -1), 3: (-1, 1)}[angulo]
    elev = math.radians(32)
    dist = 400
    olho = (cx + dx * dist * math.cos(elev) / math.sqrt(2), cy + dist * math.sin(elev),
            cz + dz * dist * math.cos(elev) / math.sqrt(2))
    vista = _olhar(olho, (cx, cy, cz))
    cantos = np.array([[x, y, z, 1] for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)], float)
    v = (vista @ cantos.T).T
    l, r, b, t = v[:, 0].min() - 2, v[:, 0].max() + 2, v[:, 1].min() - 2, v[:, 1].max() + 2
    pxb = min(px_bloco, larg_max / (r - l))
    w, h = int((r - l) * pxb), int((t - b) * pxb)
    proj = _orto(l, r, b, t, 1, 2000)
    nevoa = None
    if noite:
        prof_c = dist
        nevoa = dict(prof0=prof_c - 10, dens=0.006, nevoa_baixa=0.42)
    return _desenhar(est, vista, proj, w, h, noite, y_max, chao, nevoa, x_corte, marcadores=marcadores, y_min=y_min)


def planta(est, y0, y1, px=16, marcadores=True):
    """Vista de cima só com os blocos entre y0 e y1 (norte para cima). Grade a cada bloco, linha forte a cada 5."""
    from PIL import ImageDraw
    sx, sy, sz = est.tam
    olho = (sx / 2, 1000, sz / 2)
    vista = _olhar(olho, (sx / 2, 0, sz / 2), cima=(0, 0, -1))
    proj = _orto(-sx / 2 - 1, sx / 2 + 1, -sz / 2 - 1, sz / 2 + 1, 1, 2000)
    w, h = int((sx + 2) * px), int((sz + 2) * px)
    img = _desenhar(est, vista, proj, w, h, False, y1, False, None, None, y_min=y0, marcadores=marcadores)
    d = ImageDraw.Draw(img, "RGBA")
    for i in range(sx + 1):
        X = int((i + 1) * px)
        d.line([(X, px), (X, h - px)], fill=(0, 0, 0, 110 if i % 5 == 0 else 40))
    for j in range(sz + 1):
        Y = int((j + 1) * px)
        d.line([(px, Y), (w - px, Y)], fill=(0, 0, 0, 110 if j % 5 == 0 else 40))
    d.polygon([(px * .5, 2), (px * .1, px * .9), (px * .9, px * .9)], fill=(230, 230, 230, 255))
    return img


def longe(est, angulo=0, dist=None, altura_olho=3.0, w=1600, h=900, noite=True, fov=42, alvo_y=None):
    """Câmera em perspectiva no chão, de longe. angulo em graus (0 = do sul, 90 = do leste...)."""
    sx, sy, sz = est.tam
    y_chao = getattr(est, "y_chao", 0) + 1
    topo = max(p[1] for p in est.blocos) + 1
    cx, cz = sx / 2, sz / 2
    dist = dist or max(sx, sz) * 1.6 + 20
    a = math.radians(angulo)
    olho = (cx + math.sin(a) * dist, y_chao + altura_olho, cz + math.cos(a) * dist)
    ay = alvo_y if alvo_y is not None else y_chao + (topo - y_chao) * 0.42
    vista = _olhar(olho, (cx, ay, cz))
    proj = _persp(fov, w / h, 0.5, 1500)
    nevoa = dict(prof0=dist * 0.3, dens=0.5 / dist, nevoa_baixa=0.28) if noite else None
    return _desenhar(est, vista, proj, w, h, noite, None, True, nevoa)
