"""Prévia em PNG de uma estrutura, sem dependências (nem Pillow).

- iso(): vista isométrica de fora (ou cortada numa altura, para ver o interior).
- planta(): vista de cima de um andar (corte horizontal), norte para cima.

As cores e "texturas" são procedurais (feitas por código a partir de BLOCOS em blocos.py).
Não usa textura do Minecraft: é uma prévia de forma, volume e cor, não um print do jogo.
"""
import math
import struct
import zlib

import blocos as B
from estrutura import hash01

FUNDO = (30, 32, 36)
SOMBRA_FACE = {"topo": 1.0, "leste": 0.80, "sul": 0.63}
LIVROS = [(112, 34, 30), (44, 62, 98), (54, 82, 44), (124, 100, 52), (74, 52, 82), (40, 40, 44)]


# ---------------------------------------------------------------------------
# Quadro (imagem RGB em memória) e PNG
# ---------------------------------------------------------------------------
class Quadro:
    def __init__(self, w, h, fundo=FUNDO):
        self.w, self.h = w, h
        self.px = bytearray(bytes(fundo) * (w * h))

    def pintar(self, x, y, r, g, b, a=1.0):
        i = (y * self.w + x) * 3
        if a >= 0.999:
            self.px[i], self.px[i + 1], self.px[i + 2] = r, g, b
        else:
            p = self.px
            p[i] = int(p[i] + (r - p[i]) * a)
            p[i + 1] = int(p[i + 1] + (g - p[i + 1]) * a)
            p[i + 2] = int(p[i + 2] + (b - p[i + 2]) * a)

    def retangulo(self, x0, y0, x1, y1, cor):
        for y in range(max(0, y0), min(self.h, y1)):
            for x in range(max(0, x0), min(self.w, x1)):
                self.pintar(x, y, *cor)

    def salvar_png(self, caminho):
        linha = self.w * 3
        bruto = b"".join(b"\x00" + bytes(self.px[y * linha:(y + 1) * linha]) for y in range(self.h))

        def bloco(tipo, dados):
            return (struct.pack(">I", len(dados)) + tipo + dados
                    + struct.pack(">I", zlib.crc32(tipo + dados) & 0xFFFFFFFF))

        png = (b"\x89PNG\r\n\x1a\n"
               + bloco(b"IHDR", struct.pack(">IIBBBBB", self.w, self.h, 8, 2, 0, 0, 0))
               + bloco(b"IDAT", zlib.compress(bruto, 9))
               + bloco(b"IEND", b""))
        with open(caminho, "wb") as f:
            f.write(png)


# ---------------------------------------------------------------------------
# "Textura" procedural: devolve (r, g, b, alfa) ou None (pixel vazado)
# ---------------------------------------------------------------------------
def _clamp(v):
    return 0 if v < 0 else 255 if v > 255 else int(v)


def textura(padrao, cor, tu, tv, hb, face):
    r, g, b = cor

    def rnd(a, c=0):
        return hash01(hb, a, c, 91)

    f, alfa, sobre = 1.0, 1.0, None
    if padrao == "nada":
        return None
    if padrao in ("pedra", "pedra_musgo", "terra"):
        f = 0.80 + rnd(tu // 2, tv // 2) * 0.32 + (rnd(tu, tv) - 0.5) * 0.08
        if padrao == "pedra_musgo" and rnd(tu // 3 + 40, tv // 3) > 0.6:
            sobre = (78, 104, 52)
    elif padrao.startswith("tijolo_pedra"):
        linha = tv // 8
        junta = 15 if linha % 2 == 0 else 7
        if tv % 8 == 7 or tu == junta:
            f = 0.68
        else:
            f = 0.88 + rnd(linha * 2 + (tu > junta), 3) * 0.14 + (rnd(tu, tv) - 0.5) * 0.06
        if padrao.endswith("musgo") and rnd(tu // 3 + 7, tv // 3) > 0.55:
            sobre = (82, 106, 58)
        if padrao.endswith("rachado") and (tu == (tv * 3 + hb) % 16 or tu == (16 - tv * 2) % 16):
            f = 0.55
    elif padrao == "cinzelado":
        borda = tu in (0, 1, 14, 15) or tv in (0, 1, 14, 15)
        centro = 5 <= tu <= 10 and 5 <= tv <= 10
        f = 0.72 if borda else (0.8 if centro and (tu in (5, 10) or tv in (5, 10)) else 0.95)
    elif padrao == "tijolo":
        linha = tv // 4
        junta = (tu + (4 if linha % 2 else 0)) % 8 == 7
        if tv % 4 == 3 or junta:
            sobre, f = (168, 160, 152), 1.0
        else:
            f = 0.85 + rnd(linha, (tu + (4 if linha % 2 else 0)) // 8) * 0.25
    elif padrao == "reboco":
        f = 0.93 + rnd(tu // 3, tv // 3) * 0.08 + (rnd(tu, tv) - 0.5) * 0.04
        if rnd(tu // 4 + 13, tv // 4) > 0.86:
            f *= 0.86  # mancha de umidade
    elif padrao == "tabuas" or padrao == "porta":
        if padrao == "porta" and face != "topo":
            # tábuas em pé, com moldura
            if tu in (0, 15) or tv in (0, 15) or tv == 7:
                f = 0.72
            else:
                f = 0.86 + rnd(tu // 4, 5) * 0.16 + (rnd(tu, tv) - 0.5) * 0.05
        else:
            linha = tv // 4
            emenda = (hb * 7 + linha * 5) % 16
            if tv % 4 == 3 or tu == emenda:
                f = 0.70
            else:
                f = 0.84 + rnd(linha, tu > emenda) * 0.2 + (rnd(tu, tv) - 0.5) * 0.06
    elif padrao == "tronco_liso":
        if face == "topo":
            d = max(abs(tu - 7.5), abs(tv - 7.5))
            f = 0.82 + (0.12 if int(d) % 3 == 0 else 0.0)
        else:
            f = 0.86 + rnd(tu, 1) * 0.16
    elif padrao == "casca":
        f = 0.72 + rnd(tu, tv // 3) * 0.4
    elif padrao == "telha":
        linha = tv // 4
        if tv % 4 == 3 or (tu + (4 if linha % 2 else 0)) % 8 == 7:
            f = 0.62
        else:
            f = 0.86 + rnd(linha, (tu + (4 if linha % 2 else 0)) // 8) * 0.22
    elif padrao == "vidro":
        if tu in (0, 15) or tv in (0, 15):
            f, alfa = 0.82, 0.85
        else:
            f, alfa = (1.08 if (tu - tv) % 9 == 0 else 1.0), 0.32
    elif padrao == "grade":
        if tu % 4 not in (1, 2) and tv not in (0, 15):
            return None
        f = 0.8 + rnd(tu, tv) * 0.25
    elif padrao == "teia":
        if not ((tu + tv) % 5 == 0 or (tu - tv) % 5 == 0 or tu == 8 or tv == 8):
            return None
        if rnd(tu, tv) < 0.3:
            return None
        alfa = 0.8
    elif padrao in ("planta", "folha"):
        if rnd(tu, tv) < (0.5 if padrao == "planta" else 0.35):
            return None
        f = 0.75 + rnd(tu + 3, tv) * 0.4
    elif padrao == "musgo":
        f = 0.78 + rnd(tu // 2, tv // 2) * 0.35
    elif padrao == "la":
        f = 0.92 + (rnd(tu, tv) - 0.5) * 0.1
    elif padrao == "sangue":
        if rnd(tu // 2, tv // 2) < 0.28:
            return None
        f = 0.7 + rnd(tu, tv) * 0.55
    elif padrao == "barril":
        if face == "topo":
            f = 0.72 if (abs(tu - 7.5) > 6 or abs(tv - 7.5) > 6) else 0.9
        elif tv in (2, 3, 12, 13):
            sobre = (62, 60, 62)
        else:
            f = 0.72 if tu % 4 == 0 else 0.86 + rnd(tu // 4, 2) * 0.14
    elif padrao == "bau":
        if tu in (0, 15) or tv in (0, 15) or tv == 5:
            f = 0.62
        elif face != "topo" and 6 <= tu <= 9 and 4 <= tv <= 7:
            sobre = (190, 190, 196)
        else:
            f = 0.86 + rnd(tv // 4, 0) * 0.14
    elif padrao == "estante":
        if face == "topo" or tv in (0, 7, 8, 15):
            sobre = (118, 92, 58)
            f = 0.85
        else:
            livro = (tu + hb) // 2 + (tv // 8) * 11
            sobre = LIVROS[int(rnd(livro, 9) * len(LIVROS))]
            f = 0.75 + rnd(livro, 1) * 0.3
    elif padrao == "escada":
        if not (tu in (2, 3, 12, 13) or tv % 4 == 1):
            return None
        f = 0.9
    elif padrao == "feno":
        f = 0.75 if tv % 5 == 0 or (face != "topo" and tv in (3, 12)) else 0.92 + rnd(tu, tv) * 0.12
    elif padrao == "metal":
        f = 0.82 + rnd(tu // 2, tv // 2) * 0.3
    elif padrao == "metal_escuro":
        f = 0.8 + rnd(tu // 2, tv // 2) * 0.35
    elif padrao == "lanterna":
        f = 0.75 if tu % 5 == 0 or tv % 5 == 0 else 0.95
    elif padrao == "liso":
        f = 0.94 + (rnd(tu, tv) - 0.5) * 0.08
    elif padrao == "agua":
        f, alfa = 0.85 + rnd(tu // 3, tv // 2) * 0.25, 0.78
    elif padrao == "luz":
        return (r, g, b, 1.0, True)
    if sobre:
        r, g, b = sobre
    return (_clamp(r * f), _clamp(g * f), _clamp(b * f), alfa, False)


# ---------------------------------------------------------------------------
# Rotação (para ver os quatro lados)
# ---------------------------------------------------------------------------
_DIRS = ["north", "east", "south", "west"]


def _girar_dir(d):
    return _DIRS[(_DIRS.index(d) + 1) % 4] if d in _DIRS else d


def _girar_props(p):
    q = {k: v for k, v in p.items() if k not in _DIRS}
    for d in _DIRS:
        if d in p:
            q[_girar_dir(d)] = p[d]
    if "facing" in p:
        q["facing"] = _girar_dir(p["facing"])
    if p.get("axis") in ("x", "z"):
        q["axis"] = "z" if p["axis"] == "x" else "x"
    return q


def _girar(blocos, tam):
    """90 graus no sentido horário visto de cima: o que era norte vira leste."""
    sx, sy, sz = tam
    novo = {(sz - 1 - z, y, x): (n, _girar_props(p), o) for (x, y, z), (n, p, o) in blocos.items()}
    return novo, (sz, sy, sx)


def _preparar(est, giros, y_max=None):
    blocos = {}
    for pos, (nome, props) in est.blocos.items():
        if nome == "minecraft:air":
            continue
        if y_max is not None and pos[1] > y_max:
            continue
        blocos[pos] = (nome, dict(props), pos)
    tam = est.tam
    for _ in range(giros % 4):
        blocos, tam = _girar(blocos, tam)
    return blocos, tam


# ---------------------------------------------------------------------------
# Vista isométrica
# ---------------------------------------------------------------------------
def _face(q, p0, a, b, cor_px):
    det = a[0] * b[1] - a[1] * b[0]
    if abs(det) < 1e-6:
        return
    xs = (p0[0], p0[0] + a[0], p0[0] + b[0], p0[0] + a[0] + b[0])
    ys = (p0[1], p0[1] + a[1], p0[1] + b[1], p0[1] + a[1] + b[1])
    x0, x1 = max(0, int(math.floor(min(xs)))), min(q.w - 1, int(math.ceil(max(xs))))
    y0, y1 = max(0, int(math.floor(min(ys)))), min(q.h - 1, int(math.ceil(max(ys))))
    inv = 1.0 / det
    ax, ay, bx, by = a[0], a[1], b[0], b[1]
    for py in range(y0, y1 + 1):
        dy = py + 0.5 - p0[1]
        for px in range(x0, x1 + 1):
            dx = px + 0.5 - p0[0]
            s = (dx * by - dy * bx) * inv
            if s < 0 or s >= 1:
                continue
            t = (ax * dy - ay * dx) * inv
            if t < 0 or t >= 1:
                continue
            c = cor_px(s, t)
            if c:
                q.pintar(px, py, c[0], c[1], c[2], c[3])


def iso(est, giros=0, y_max=None, escala=16):
    """giros: 0 = vista do sudeste (vê sul e leste), 1 = vê norte e leste, 2 = vê norte e oeste, 3 = vê oeste e sul."""
    blocos, (sx, sy, sz) = _preparar(est, giros, y_max)
    k, h, s = escala * math.sqrt(3) / 2, escala / 2, escala
    margem = 24
    w = int((sx + sz) * k) + 2 * margem
    alt = int((sx + sz) * h + sy * s) + 2 * margem
    q = Quadro(w, alt)
    ox, oy = sz * k + margem, sy * s + margem

    def proj(x, y, z):
        return (ox + (x - z) * k, oy + (x + z) * h - y * s)

    opacos = {pos for pos, (n, p, _) in blocos.items() if B.cheio(n, p)}
    itens = []
    for pos, (n, p, orig) in blocos.items():
        hb = int(hash01(*orig) * 1e9)
        for caixa, mat in B.formas(n, p):
            cx = pos[0] + (caixa[0] + caixa[3]) / 2
            cy = pos[1] + (caixa[1] + caixa[4]) / 2
            cz = pos[2] + (caixa[2] + caixa[5]) / 2
            itens.append((cx + cy + cz, cy, pos, caixa, mat, hb))
    itens.sort(key=lambda i: (i[0], i[1]))

    for _, _, (x, y, z), cx, mat, hb in itens:
        cor, padrao = B.material(mat)
        cheia = cx == B.CHEIA and (x, y, z) in opacos
        x0, y0, z0, x1, y1, z1 = cx
        faces = []
        if not (cheia and (x, y + 1, z) in opacos):
            faces.append(("topo", (x + x0, y + y1, z + z0), (x1 - x0, 0, 0), (0, 0, z1 - z0)))
        if not (cheia and (x + 1, y, z) in opacos):
            faces.append(("leste", (x + x1, y + y0, z + z0), (0, 0, z1 - z0), (0, y1 - y0, 0)))
        if not (cheia and (x, y, z + 1) in opacos):
            faces.append(("sul", (x + x0, y + y0, z + z1), (x1 - x0, 0, 0), (0, y1 - y0, 0)))
        for face, P, A, Bv in faces:
            p0 = proj(*P)
            pa = proj(P[0] + A[0], P[1] + A[1], P[2] + A[2])
            pb = proj(P[0] + Bv[0], P[1] + Bv[1], P[2] + Bv[2])
            a = (pa[0] - p0[0], pa[1] - p0[1])
            b = (pb[0] - p0[0], pb[1] - p0[1])
            sombra = SOMBRA_FACE[face]
            # luz ambiente: mais escuro perto do chão, como dentro de uma casa sem luz
            amb = 0.78 + 0.22 * min(1.0, (y + 1) / 6.0)
            _face(q, p0, a, b, _amostrador(face, cx, cor, padrao, hb, sombra * amb))
    return q


def _amostrador(face, cx, cor, padrao, hb, sombra):
    x0, y0, z0, x1, y1, z1 = cx
    cheia = cx == B.CHEIA

    def cor_px(s, t):
        if face == "topo":
            u, v = x0 + s * (x1 - x0), z0 + t * (z1 - z0)
        elif face == "leste":
            u, v = z0 + s * (z1 - z0), 1 - (y0 + t * (y1 - y0))
        else:
            u, v = x0 + s * (x1 - x0), 1 - (y0 + t * (y1 - y0))
        tu, tv = min(15, int(u * 16)), min(15, int(v * 16))
        c = textura(padrao, cor, tu, tv, hb, face)
        if c is None:
            return None
        r, g, b, alfa, luz = c
        if luz:
            return (r, g, b, 1.0)
        f = sombra
        if cheia and (s < 0.04 or t < 0.04 or s > 0.96 or t > 0.96):
            f *= 0.86  # contorno do bloco
        return (_clamp(r * f), _clamp(g * f), _clamp(b * f), alfa)

    return cor_px


# ---------------------------------------------------------------------------
# Planta (corte horizontal visto de cima)
# ---------------------------------------------------------------------------
def planta(est, y_min, y_max, celula=16):
    """Vista de cima só com os blocos entre y_min e y_max. Norte para cima, leste à direita.

    Mais claro = mais alto (parede); mais escuro = mais baixo (piso). Linhas a cada 5 blocos.
    """
    sx, sy, sz = est.tam
    margem = 16
    q = Quadro(sx * celula + 2 * margem, sz * celula + 2 * margem)
    blocos = {pos: (n, dict(p)) for pos, (n, p) in est.blocos.items() if n != "minecraft:air"}
    faixa = max(1, y_max - y_min)
    for x in range(sx):
        for z in range(sz):
            camadas = []
            for y in range(y_max, y_min - 1, -1):
                b = blocos.get((x, y, z))
                if not b:
                    continue
                camadas.append((y, b))
                if B.cheio(*b):
                    break
            px0, pz0 = margem + x * celula, margem + z * celula
            if not camadas:
                q.retangulo(px0, pz0, px0 + celula, pz0 + celula, (22, 23, 26))
                continue
            hb_base = int(hash01(x, z, 5) * 1e9)
            for y, (n, p) in reversed(camadas):
                altura = 0.5 + 0.5 * (y - y_min) / faixa
                for cx, mat in B.formas(n, p):
                    cor, padrao = B.material(mat)
                    ax0, ax1 = int(px0 + cx[0] * celula), int(math.ceil(px0 + cx[3] * celula))
                    az0, az1 = int(pz0 + cx[2] * celula), int(math.ceil(pz0 + cx[5] * celula))
                    for py in range(az0, max(az1, az0 + 1)):
                        for pxx in range(ax0, max(ax1, ax0 + 1)):
                            u = (pxx - px0 + 0.5) / celula
                            v = (py - pz0 + 0.5) / celula
                            c = textura(padrao, cor, min(15, int(u * 16)), min(15, int(v * 16)),
                                        hb_base + y, "topo")
                            if c is None:
                                continue
                            r, g, b, alfa, luz = c
                            f = 1.0 if luz else altura
                            q.pintar(pxx, py, _clamp(r * f), _clamp(g * f), _clamp(b * f), alfa)
    # grade: linha fina em cada bloco, mais forte a cada 5
    for i in range(sx + 1):
        x = margem + i * celula
        for y in range(margem, margem + sz * celula):
            if 0 <= x < q.w:
                q.pintar(x, y, 0, 0, 0, 0.35 if i % 5 == 0 else 0.12)
    for j in range(sz + 1):
        y = margem + j * celula
        for x in range(margem, margem + sx * celula):
            if 0 <= y < q.h:
                q.pintar(x, y, 0, 0, 0, 0.35 if j % 5 == 0 else 0.12)
    # seta do norte (triângulo no canto de cima à esquerda)
    for d in range(8):
        for x in range(4 + 8 - d, 4 + 8 + d + 1):
            q.pintar(x, 2 + d, 220, 220, 220)
    return q
