"""Prévia 3D em perspectiva (altura de quem anda), de dia e de noite, com névoa.

Usa moderngl (EGL, sem janela) + numpy + Pillow. A ferramenta de estruturas continua sem dependência:
só esta prévia precisa delas. As texturas são as mesmas cores/padrões procedurais do render.py (não são as
texturas do jogo): mostram cor, volume e clima, não substituem o print do jogo.

Uso (de outro script):
    import render3d
    cena = render3d.Cena(estrutura)                      # monta a malha uma vez
    img = cena.foto(olho=(74, 3, 120), alvo=(74, 2, 80), clima="noite")   # PIL.Image
Climas: "dia" (nublado claro, névoa leve) e "noite" (noite escura, lua e névoa carmesim; decisão de 14/11/2026).
"""
import math

import numpy as np

import blocos as B
import render

TILE = 16
_CTX = None
VARIANTES = 4

# clima: céu (horizonte, alto), névoa (cor, densidade), sol (direção, cor), ambiente (cor)
CLIMAS = {
    "dia": {  # dia nublado (a ficha: "céu nublado e baixo"), sem névoa de horror
        "ceu_h": (0.70, 0.72, 0.74), "ceu_a": (0.52, 0.57, 0.64),
        "nevoa": (0.68, 0.70, 0.72), "densidade": 0.011,
        "sol": (0.45, 0.80, 0.38), "sol_cor": (0.92, 0.90, 0.86), "amb": (0.50, 0.52, 0.56),
        "luzes": 0.0,
    },
    "noite": {
        # noite bem escura com névoa carmesim (camada de horror, 14/11/2026): a lua avermelhada dá o contorno
        "ceu_h": (0.20, 0.025, 0.03), "ceu_a": (0.035, 0.008, 0.014),
        "nevoa": (0.22, 0.03, 0.035), "densidade": 0.030,
        "sol": (-0.22, 0.30, -0.93), "sol_cor": (0.62, 0.30, 0.30), "amb": (0.19, 0.12, 0.14),
        "luzes": 1.0,
    },
}

_VS = """
#version 330
uniform mat4 mvp;
uniform mat4 luz_mvp;
in vec3 pos; in vec3 nor; in vec2 tile; in vec2 uv; in float ao; in float emis;
out vec3 v_pos; out vec3 v_nor; out vec2 v_tile; out vec2 v_uv; out float v_ao; out float v_emis; out vec4 v_luz;
void main() {
    v_pos = pos; v_nor = nor; v_tile = tile; v_uv = uv; v_ao = ao; v_emis = emis;
    v_luz = luz_mvp * vec4(pos + nor * 0.04, 1.0);
    gl_Position = mvp * vec4(pos, 1.0);
}
"""

_FS = """
#version 330
uniform sampler2D atlas;
uniform sampler2DShadow sombra;
uniform vec2 atlas_px;
uniform vec3 olho, sol, sol_cor, amb, nevoa_cor;
uniform float densidade;
uniform int n_luzes;
uniform vec3 luz_pos[16];
uniform float luzes_forca;
uniform int passo;  // 0 opaco (descarta translúcido), 1 translúcido
in vec3 v_pos; in vec3 v_nor; in vec2 v_tile; in vec2 v_uv; in float v_ao; in float v_emis; in vec4 v_luz;
out vec4 cor;
void main() {
    vec2 t = (v_tile + clamp(fract(v_uv), 0.001, 0.999) * 16.0) / atlas_px;
    vec4 c = texture(atlas, t);
    if (c.a < 0.1) discard;
    if (passo == 0 && c.a < 0.95) discard;
    if (passo == 1 && c.a >= 0.95) discard;
    vec3 n = normalize(v_nor);
    float lado = n.y > 0.5 ? 1.0 : (n.y < -0.5 ? 0.5 : (abs(n.x) > 0.5 ? 0.8 : 0.66));
    vec3 p = v_luz.xyz / v_luz.w * 0.5 + 0.5;
    float s = 0.0;
    for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++)
        s += texture(sombra, vec3(p.xy + vec2(i, j) / 2048.0, p.z - 0.0015));
    s /= 9.0;
    if (p.x < 0.0 || p.x > 1.0 || p.y < 0.0 || p.y > 1.0) s = 1.0;
    float d = max(dot(n, normalize(sol)), 0.0);
    vec3 luz = amb * lado * v_ao + sol_cor * d * s * mix(0.6, 1.0, v_ao);
    for (int i = 0; i < n_luzes; i++) {
        float r = distance(v_pos, luz_pos[i]);
        luz += vec3(1.0, 0.62, 0.3) * luzes_forca * 1.8 / (1.0 + r * r * 0.35);
    }
    vec3 rgb = c.rgb * luz;
    if (v_emis > 0.5) rgb = c.rgb * 1.3;
    float dist = distance(v_pos, olho);
    float f = 1.0 - exp(-pow(dist * densidade, 1.6));
    rgb = mix(rgb, nevoa_cor, clamp(f, 0.0, 1.0));
    cor = vec4(rgb, passo == 1 ? c.a : 1.0);
}
"""

_VS_SOMBRA = """
#version 330
uniform mat4 luz_mvp;
in vec3 pos;
void main() { gl_Position = luz_mvp * vec4(pos, 1.0); }
"""
_FS_SOMBRA = """
#version 330
void main() {}
"""

_VS_CEU = """
#version 330
in vec2 p; out vec2 v_p;
void main() { v_p = p; gl_Position = vec4(p, 0.9999, 1.0); }
"""
_FS_CEU = """
#version 330
uniform mat4 inv; uniform vec3 ceu_h, ceu_a, sol, nevoa_cor; uniform float lua;
in vec2 v_p; out vec4 cor;
void main() {
    vec4 a = inv * vec4(v_p, 1.0, 1.0);
    vec4 b = inv * vec4(v_p, -1.0, 1.0);
    vec3 d = normalize(a.xyz / a.w - b.xyz / b.w);
    float h = clamp(d.y * 2.2, 0.0, 1.0);
    vec3 c = mix(ceu_h, ceu_a, h);
    if (lua > 0.5) {
        float m = dot(d, normalize(sol));
        c += vec3(0.70, 0.26, 0.24) * smoothstep(0.99955, 0.99975, m) * 0.9;
        c += vec3(0.30, 0.05, 0.05) * pow(max(m, 0.0), 40.0) * 0.5;
    }
    c = mix(nevoa_cor, c, smoothstep(-0.02, 0.18, d.y));
    cor = vec4(c, 1.0);
}
"""


def _perspectiva(fov, aspecto, perto, longe):
    f = 1.0 / math.tan(math.radians(fov) / 2)
    return np.array([[f / aspecto, 0, 0, 0], [0, f, 0, 0],
                     [0, 0, (longe + perto) / (perto - longe), 2 * longe * perto / (perto - longe)],
                     [0, 0, -1, 0]], dtype="f8")


def _orto(l, r, b, t, n, f):
    return np.array([[2 / (r - l), 0, 0, -(r + l) / (r - l)], [0, 2 / (t - b), 0, -(t + b) / (t - b)],
                     [0, 0, -2 / (f - n), -(f + n) / (f - n)], [0, 0, 0, 1]], dtype="f8")


def _olhar(olho, alvo, cima=(0, 1, 0)):
    olho, alvo, cima = (np.array(v, dtype="f8") for v in (olho, alvo, cima))
    f = alvo - olho
    f /= np.linalg.norm(f)
    s = np.cross(f, cima)
    s /= np.linalg.norm(s)
    u = np.cross(s, f)
    m = np.identity(4)
    m[0, :3], m[1, :3], m[2, :3] = s, u, -f
    m[:3, 3] = -m[:3, :3] @ olho
    return m


class Atlas:
    """Uma peça 16 x 16 por (material, face, variante), feita pelo render.textura."""

    def __init__(self):
        self.idx = {}
        self.pecas = []

    def peca(self, mat, face, var):
        chave = (mat, face, var)
        if chave not in self.idx:
            self.idx[chave] = len(self.pecas)
            self.pecas.append(chave)
        return self.idx[chave]

    def imagem(self):
        n = len(self.pecas)
        lado = int(math.ceil(math.sqrt(n)))
        px = np.zeros((lado * TILE, lado * TILE, 4), dtype="u1")
        for i, (mat, face, var) in enumerate(self.pecas):
            cor, padrao = B.material(mat)
            ox, oy = (i % lado) * TILE, (i // lado) * TILE
            for tv in range(TILE):
                for tu in range(TILE):
                    r = render.textura(padrao, cor, tu, tv, var * 37 + 5, face)
                    if r is None:
                        continue
                    a = 255 if r[3] >= 0.95 else int(r[3] * 255)
                    px[oy + tv, ox + tu] = (r[0], r[1], r[2], max(a, 30))
        return lado, px

    def origem(self, i, lado):
        return float((i % lado) * TILE), float((i // lado) * TILE)


# faces de uma caixa: normal, 4 cantos (em ordem), e como tirar (u, v) de cada canto
_FACES = [
    ((0, 1, 0), "topo"), ((0, -1, 0), "baixo"), ((1, 0, 0), "lado"), ((-1, 0, 0), "lado"),
    ((0, 0, 1), "lado"), ((0, 0, -1), "lado"),
]


def _cantos(caixa, n):
    x0, y0, z0, x1, y1, z1 = caixa
    if n == (0, 1, 0):
        return [(x0, y1, z0), (x0, y1, z1), (x1, y1, z1), (x1, y1, z0)]
    if n == (0, -1, 0):
        return [(x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)]
    if n == (1, 0, 0):
        return [(x1, y0, z0), (x1, y1, z0), (x1, y1, z1), (x1, y0, z1)]
    if n == (-1, 0, 0):
        return [(x0, y0, z0), (x0, y0, z1), (x0, y1, z1), (x0, y1, z0)]
    if n == (0, 0, 1):
        return [(x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)]
    return [(x0, y0, z0), (x0, y1, z0), (x1, y1, z0), (x1, y0, z0)]


def _uv(c, n):
    x, y, z = c
    if n[1] != 0:
        return x, z
    if n[0] != 0:
        return z, 1 - y
    return x, 1 - y


class Cena:
    def __init__(self, est, chao_do_mundo=True, margem=160):
        import moderngl
        self.est = est
        global _CTX
        if _CTX is None:  # um contexto só por processo (dois contextos misturam as texturas)
            _CTX = moderngl.create_standalone_context(backend="egl")
        self.ctx = _CTX
        self.atlas = Atlas()
        blocos = dict(est.blocos)
        if chao_do_mundo:
            colunas = {(x, z) for (x, y, z), b in blocos.items() if y == 0 and b[0] != "minecraft:air"}
            todas = {(x, z) for (x, y, z) in blocos}
            for x in range(est.tam[0]):
                for z in range(est.tam[2]):
                    if (x, z) not in todas:
                        blocos[(x, 0, z)] = ("minecraft:grass_block", (("snowy", "false"),))
            self._colunas = colunas
        self.blocos = blocos
        self.luzes = []
        dados = []
        self._malha(blocos, dados)
        self._terreno(dados, est.tam, margem)
        arr = np.array(dados, dtype="f4")
        lado, img = self.atlas.imagem()
        self.lado = lado
        # troca o índice da peça (coluna 6) pela origem no atlas
        idx = arr[:, 6].astype(int)
        arr[:, 6] = (idx % lado) * TILE
        arr = np.insert(arr, 7, (idx // lado) * TILE, axis=1)
        self.tex = self.ctx.texture((lado * TILE, lado * TILE), 4, img.tobytes())
        self.tex.filter = (moderngl.NEAREST, moderngl.NEAREST)
        self.prog = self.ctx.program(vertex_shader=_VS, fragment_shader=_FS)
        self.prog_s = self.ctx.program(vertex_shader=_VS_SOMBRA, fragment_shader=_FS_SOMBRA)
        self.prog_c = self.ctx.program(vertex_shader=_VS_CEU, fragment_shader=_FS_CEU)
        vbo = self.ctx.buffer(arr.astype("f4").tobytes())
        self.vao = self.ctx.vertex_array(self.prog, [(vbo, "3f 3f 2f 2f 1f 1f", "pos", "nor", "tile", "uv", "ao", "emis")])
        self.vao_s = self.ctx.vertex_array(self.prog_s, [(vbo, "3f 36x", "pos")])
        q = self.ctx.buffer(np.array([-1, -1, 1, -1, -1, 1, 1, 1], dtype="f4").tobytes())
        self.vao_c = self.ctx.vertex_array(self.prog_c, [(q, "2f", "p")])
        self.sombra = self.ctx.depth_texture((2048, 2048))
        self.sombra.compare_func = "<="
        self.sombra.filter = (moderngl.LINEAR, moderngl.LINEAR)
        self.fbo_s = self.ctx.framebuffer(depth_attachment=self.sombra)
        self.n_vert = len(arr)

    # ------------------------------------------------------------------
    def _cheio(self, x, y, z):
        b = self.blocos.get((x, y, z))
        return b is not None and B.cheio(b[0], dict(b[1]))

    def _malha(self, blocos, dados):
        cheios = {p for p, b in blocos.items() if B.cheio(b[0], dict(b[1]))}
        for (x, y, z), (nome, props) in blocos.items():
            p = dict(props)
            curto = B.curto(nome)
            if curto in ("candle", "red_candle", "lantern", "campfire") and p.get("lit", p.get("hanging")) == "true" \
                    and curto != "lantern":
                self.luzes.append((x + 0.5, y + 0.6, z + 0.5))
            if curto == "lantern":
                self.luzes.append((x + 0.5, y + 0.4, z + 0.5))
            var = (x * 7 + y * 13 + z * 3) % VARIANTES
            for caixa, mat in B.formas(nome, p):
                padrao = B.material(mat)[1]
                if padrao == "nada":
                    continue
                emis = 1.0 if padrao == "luz" else 0.0
                if padrao == "planta" or curto == "wheat":
                    self._cruz(dados, x, y, z, caixa, mat, var)
                    continue
                cheia = caixa == B.CHEIA and (x, y, z) in cheios
                for n, face in _FACES:
                    if cheia and (x + n[0], y + n[1], z + n[2]) in cheios:
                        continue
                    if not cheia and caixa[1] == 0 and n == (0, -1, 0) and (x, y - 1, z) in cheios:
                        continue
                    t = self.atlas.peca(mat, "lado" if face == "lado" else "topo", var)
                    cs = _cantos(caixa, n)
                    aos = [self._ao(x, y, z, n, c, cheios) if cheia or caixa[1] == 0 and n[1] == 1 else 1.0
                           for c in cs]
                    vs = []
                    for c, a in zip(cs, aos):
                        u, v = _uv(c, n)
                        vs.append((x + c[0], y + c[1], z + c[2], n[0], n[1], n[2], t, u, v, a, emis))
                    for k in (0, 1, 2, 0, 2, 3):
                        u, v = vs[k][7], vs[k][8]
                        dados.append(vs[k][:6] + (vs[k][6],) + (min(u, 0.9999), min(v, 0.9999), vs[k][9], vs[k][10]))

    def _ao(self, x, y, z, n, c, cheios):
        # canto do bloco (0/1) nos eixos que não são a normal
        ax = [i for i in range(3) if n[i] == 0]
        base = [x + n[0], y + n[1], z + n[2]]
        s = []
        for i in ax:
            d = [0, 0, 0]
            d[i] = 1 if c[i] > 0.5 else -1
            s.append(d)
        def tem(d):
            return (base[0] + d[0], base[1] + d[1], base[2] + d[2]) in cheios
        s1, s2 = tem(s[0]), tem(s[1])
        canto = tem([s[0][k] + s[1][k] for k in range(3)])
        nivel = 0 if (s1 and s2) else 3 - (s1 + s2 + canto)
        return (0.45, 0.62, 0.8, 1.0)[nivel]

    def _cruz(self, dados, x, y, z, caixa, mat, var):
        t = self.atlas.peca(mat, "lado", var)
        h = caixa[4]
        for (ax, az, bx, bz) in ((0.15, 0.15, 0.85, 0.85), (0.15, 0.85, 0.85, 0.15)):
            cs = [(ax, 0, az), (bx, 0, bz), (bx, h, bz), (ax, h, az)]
            uvs = [(0, 0.999), (0.999, 0.999), (0.999, 1 - h + 0.001), (0, 1 - h + 0.001)]
            for k in (0, 1, 2, 0, 2, 3):
                c, (u, v) = cs[k], uvs[k]
                dados.append((x + c[0], y + c[1], z + c[2], 0, 1, 0, t, u, v, 0.9, 0.0))

    def _terreno(self, dados, tam, margem):
        """O terreno do mundo em volta (grama), um bloco abaixo do chão da vila."""
        t = self.atlas.peca("grass_block", "topo", 0)
        x0, z0, x1, z1 = -margem, -margem, tam[0] + margem, tam[2] + margem
        passo = 10
        for xa in range(x0, x1, passo):
            for za in range(z0, z1, passo):
                xb, zb = xa + passo, za + passo
                if 0 <= xa and xb <= tam[0] and 0 <= za and zb <= tam[2]:
                    continue
                cs = [(xa, 0, za), (xa, 0, zb), (xb, 0, zb), (xb, 0, za)]
                for k in (0, 1, 2, 0, 2, 3):
                    c = cs[k]
                    dados.append((c[0], 0.0, c[2], 0, 1, 0, t, c[0] - xa, c[2] - za, 1.0, 0.0))
        # debaixo da vila (aparece nas covas e no poço)
        cs = [(0, 0, 0), (0, 0, tam[2]), (tam[0], 0, tam[2]), (tam[0], 0, 0)]
        td = self.atlas.peca("dirt", "topo", 0)
        for k in (0, 1, 2, 0, 2, 3):
            c = cs[k]
            dados.append((c[0], -0.01, c[2], 0, 1, 0, td, c[0], c[2], 0.8, 0.0))

    # ------------------------------------------------------------------
    def foto(self, olho, alvo, clima="dia", tam=(960, 540), fov=70.0):
        import moderngl
        from PIL import Image
        k = CLIMAS[clima]
        ctx = self.ctx
        w, h = tam
        sol = np.array(k["sol"], dtype="f8")
        sol /= np.linalg.norm(sol)
        meio = np.array([self.est.tam[0] / 2, 0, self.est.tam[2] / 2])
        r = max(self.est.tam[0], self.est.tam[2]) * 0.8
        luz_v = _olhar(meio + sol * 120, meio, (0, 0, 1) if abs(sol[1]) > 0.99 else (0, 1, 0))
        luz_p = _orto(-r, r, -r, r, 1, 260)
        luz_mvp = (luz_p @ luz_v).astype("f4")
        # sombra
        self.fbo_s.use()
        self.fbo_s.clear(depth=1.0)
        ctx.enable(moderngl.DEPTH_TEST)
        ctx.disable(moderngl.CULL_FACE)
        self.prog_s["luz_mvp"].write(luz_mvp.T.tobytes())
        self.vao_s.render(moderngl.TRIANGLES)
        # cena
        fbo = ctx.framebuffer(color_attachments=[ctx.texture((w, h), 4)], depth_attachment=ctx.depth_renderbuffer((w, h)))
        fbo.use()
        fbo.clear(0, 0, 0, 1, depth=1.0)
        proj = _perspectiva(fov, w / h, 0.1, 600)
        vista = _olhar(olho, alvo)
        mvp = proj @ vista
        inv = np.linalg.inv(mvp)
        ctx.disable(moderngl.DEPTH_TEST)
        pc = self.prog_c
        pc["inv"].write(inv.astype("f4").T.tobytes())
        for n in ("ceu_h", "ceu_a"):
            pc[n].value = k[n]
        pc["sol"].value = tuple(sol)
        pc["nevoa_cor"].value = k["nevoa"]
        pc["lua"].value = 1.0 if clima == "noite" else 0.0
        self.vao_c.render(moderngl.TRIANGLE_STRIP)
        ctx.enable(moderngl.DEPTH_TEST)
        p = self.prog
        p["mvp"].write(mvp.astype("f4").T.tobytes())
        p["luz_mvp"].write(luz_mvp.T.tobytes())
        p["atlas_px"].value = (float(self.lado * TILE), float(self.lado * TILE))
        p["olho"].value = tuple(float(v) for v in olho)
        p["sol"].value = tuple(sol)
        p["sol_cor"].value = k["sol_cor"]
        p["amb"].value = k["amb"]
        p["nevoa_cor"].value = k["nevoa"]
        p["densidade"].value = k["densidade"]
        p["luzes_forca"].value = k["luzes"]
        luzes = sorted(self.luzes, key=lambda q: (q[0] - olho[0]) ** 2 + (q[2] - olho[2]) ** 2)[:16]
        p["n_luzes"].value = len(luzes)
        if luzes:
            arr = np.zeros((16, 3), dtype="f4")
            arr[:len(luzes)] = luzes
            p["luz_pos"].write(arr.tobytes())
        self.tex.use(0)
        self.sombra.use(1)
        p["atlas"].value = 0
        p["sombra"].value = 1
        p["passo"].value = 0
        self.vao.render(moderngl.TRIANGLES)
        ctx.enable(moderngl.BLEND)
        ctx.depth_mask = False
        p["passo"].value = 1
        self.vao.render(moderngl.TRIANGLES)
        ctx.depth_mask = True
        ctx.disable(moderngl.BLEND)
        img = Image.frombytes("RGBA", (w, h), fbo.color_attachments[0].read()).transpose(Image.FLIP_TOP_BOTTOM)
        fbo.release()
        return img.convert("RGB")
