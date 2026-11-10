#!/usr/bin/env python3
"""Gera os PNG das prévias da Vila Calada (Faminto e atril).

O que faz:
  1. pinta as texturas (pixel a pixel, sem ruído aleatório) em arte/vila-calada/texturas/;
  2. coloca as texturas dentro dos .bbmodel (o Blockbench abre já com textura);
  3. renderiza cada modelo de frente, lado, costas e 3/4 em arte/vila-calada/previas/*_render.png.

Só usa a biblioteca padrão do Python (sem Pillow). Rodar da raiz do repositório:
    python3 arte/vila-calada/fonte/gerar_png.py
"""
import base64
import json
import math
import struct
import zlib
from pathlib import Path

PASTA = Path(__file__).resolve().parent.parent  # arte/vila-calada
TEXTURAS = PASTA / "texturas"
PREVIAS = PASTA / "previas"

VAZIO = (0, 0, 0, 0)


def cor(h):
    return (int(h[1:3], 16), int(h[3:5], 16), int(h[5:7], 16), 255)


# ---------------------------------------------------------------- PNG

def png_bytes(img):
    w, h = len(img[0]), len(img)
    bruto = bytearray()
    for linha in img:
        bruto.append(0)
        for px in linha:
            bruto += bytes(px)

    def bloco(tipo, dados):
        c = struct.pack(">I", len(dados)) + tipo + dados
        return c + struct.pack(">I", zlib.crc32(tipo + dados) & 0xFFFFFFFF)

    return (b"\x89PNG\r\n\x1a\n"
            + bloco(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + bloco(b"IDAT", zlib.compress(bytes(bruto), 9))
            + bloco(b"IEND", b""))


def imagem(w, h, fundo=VAZIO):
    return [[fundo for _ in range(w)] for _ in range(h)]


# ---------------------------------------------------------------- paletas

PELE = ["#b3a691", "#978a76", "#7f7364", "#5c5246", "#3d362f"]
MANTO = ["#4a3c33", "#3a2f29", "#2f2621", "#1e1815", "#17120f"]
BOCA = ["#9a2620", "#6e1010", "#380808"]  # o acento: vermelho só na boca
DENTE = "#cbc2a2"
UNHA = "#6b5a3a"
ORBITA = "#120e0c"

MADEIRA = ["#57432f", "#4d3b2e", "#3b2e25", "#2a211c", "#161210"]
PAPEL_ESQ, PAPEL_DIR, LOMBADA, LOMBADA_FUNDO = "#d0c197", "#c9b98f", "#b8a982", "#9e8e66"
TEXTO, TINTA, TINTA_MEIA = "#8a7853", "#3a2c20", "#6b5a40"
CAPA, CAPA_BORDA = "#3a2a22", "#2a1d18"
FITA, FITA_SOMBRA = "#8a1717", "#5c0e0e"


def tom_pele(x, y, w, h, escurecer=0):
    t = 1
    if x == 0 or y == 0:
        t = 0
    if x == w - 1 or y == h - 1:
        t = 2
    if w >= 3 and 0 < x < w - 1 and (2 * x + y) % 7 == 0:
        t = 2  # estrias da pele esticada
    return PELE[min(t + escurecer, 4)]


def tom_manto(x, y, w, h):
    t = 1
    if y == 0:
        t = 0
    if x == w - 1 or y == h - 1:
        t = 2
    if w >= 6 and x % 4 == 1:
        t = 2  # dobra do pano
    if y == h - 1 and x % 3 == 0:
        t = 4  # barra rasgada
    return MANTO[t]


# ---------------------------------------------------------------- Faminto

def faces_box_uv(el):
    """Retângulos da UV em caixa (igual ao Blockbench) a partir de uv_offset e tamanho."""
    u, v = el["uv_offset"]
    w = round(el["to"][0] - el["from"][0])
    h = round(el["to"][1] - el["from"][1])
    d = round(el["to"][2] - el["from"][2])
    return {
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + 2 * d + w, v + d, w, h),
        "up": (u + d, v, w, d),
        "down": (u + d + w, v, w, d),
    }


def pintar_faminto(nome, face, x, y, w, h):
    if nome == "cabeca":
        if face == "north":
            if x in (1, 2, 5, 6) and y in (2, 3):
                return ORBITA  # sem olhos: o Sangue come os olhos primeiro
            if y == 1 and 1 <= x <= 6:
                return PELE[0]
            if x in (0, 7) and y == 5:
                return PELE[3]
            if y == h - 1:
                return PELE[2]
        if face == "up" and (x + 2 * y) % 5 == 0:
            return PELE[2]
        return tom_pele(x, y, w, h)
    if nome == "nariz":
        if face == "north" and y == h - 1:
            return PELE[4]
        return tom_pele(x, y, w, h, 1)
    if nome == "dentes":
        if face in ("north", "south") and x == 2:
            return BOCA[2]  # dente que falta
        return DENTE
    if nome == "boca":
        if face == "north":
            return BOCA[1] if y == h - 1 else BOCA[2]
        if face == "down":
            return BOCA[1]
        return BOCA[2]
    if nome == "pescoco":
        if face == "north" and x in (0, 2):
            return PELE[0]  # tendões
        return tom_pele(x, y, w, h)
    if nome == "mandibula":
        if face == "up":  # o lado de dentro da boca
            if y == 0:
                return DENTE if x % 2 == 0 else BOCA[0]
            return BOCA[1]
        if face == "down":
            return tom_pele(x, y, w, h, 1)
        return tom_pele(x, y, w, h)
    if nome == "corpo":
        if face == "north":
            if 2 <= x <= 5 and 2 <= y <= 6:
                return PELE[3] if y % 2 == 0 else PELE[1]  # costelas no rasgo
            if 1 <= x <= 6 and 1 <= y <= 7:
                return MANTO[4]
        if face == "south":
            if 3 <= x <= 4 and 1 <= y <= 10:
                return PELE[0] if y % 2 == 1 else PELE[2]  # espinha
            if 2 <= x <= 5 and 1 <= y <= 10:
                return MANTO[4]
        if face == "up":
            return MANTO[0] if x > 0 else MANTO[1]
        return tom_manto(x, y, w, h)
    if nome.startswith("braco"):
        if face == "up":
            return tom_manto(x, y, w, h)
        if face == "down":
            return tom_pele(x, y, w, h)
        if y < 4 or (y == 4 and x % 2 == 0):
            return tom_manto(x, y, w, 4)  # manga rasgada
        if y == 8:
            return PELE[0]  # cotovelo ossudo
        return tom_pele(x, y, w, h)
    if nome.startswith("mao"):
        if face == "down" or y == h - 1:
            return UNHA
        if face in ("north", "south") and y >= 2 and x == 1:
            return PELE[4]  # vão entre os dedos
        return tom_pele(x, y, w, h)
    if nome.startswith("perna"):
        if face == "down":
            return PELE[3]
        if y < 3:
            return tom_manto(x, y, w, 3)
        if y == h - 1:
            return PELE[3]
        return tom_pele(x, y, w, h)
    return "#ff00ff"


def textura_faminto(modelo):
    img = imagem(64, 64)
    for el in modelo["elements"]:
        for face, (u, v, w, h) in faces_box_uv(el).items():
            for y in range(h):
                for x in range(w):
                    img[v + y][u + x] = cor(pintar_faminto(el["name"], face, x, y, w, h))
    return img


# ---------------------------------------------------------------- atril

def textura_madeira():
    img = imagem(16, 16)
    for y in range(16):
        for x in range(16):
            t = 1 + (y // 4) % 2
            if y % 4 == 3:
                t = 3  # fresta entre tábuas
            elif (x + 3 * (y // 4)) % 7 == 0:
                t = min(t + 1, 3)  # veio
            if y % 4 == 0 and x < 6:
                t = 0  # luz de cima à esquerda
            if x + y >= 25 or (x < 3 and y > 12):
                t = 4  # carvão
            img[y][x] = cor(MADEIRA[t])
    return img


def textura_livro():
    img = imagem(16, 16)
    sino_escuro = {(7, 1), (6, 2), (8, 2), (6, 3), (8, 3), (6, 4), (8, 4), (7, 5)}
    sino_meio = {(7, 3), (7, 4)}
    linhas_esq = {1: range(0, 4), 2: range(1, 4), 3: range(0, 4), 5: range(0, 3), 6: range(0, 4)}
    for y in range(8):  # páginas abertas, 10 x 8
        for x in range(10):
            c = PAPEL_ESQ if x < 4 else PAPEL_DIR
            if x == 4:
                c = LOMBADA_FUNDO
            if x in linhas_esq.get(y, ()):
                c = TEXTO
            if (x, y) in sino_escuro:
                c = TINTA
            if (x, y) in sino_meio:
                c = TINTA_MEIA
            if y == 7 and 6 <= x <= 8:
                c = TEXTO
            if x == 5:
                c = FITA if y >= 4 else LOMBADA
            img[y][x] = cor(c)
    for x in range(10):  # borda das páginas
        img[8][x] = cor("#d6c8a0" if x % 2 == 0 else LOMBADA)
    for x in range(8):
        img[9][x] = cor("#d6c8a0" if x % 2 == 0 else LOMBADA)
    for y in range(10, 16):  # capa
        for x in range(11):
            borda = y in (10, 15) or x in (0, 10)
            img[y][x] = cor(CAPA_BORDA if borda else CAPA)
    for y in range(6):  # fita
        img[y][12] = cor(FITA_SOMBRA if y == 5 else FITA)
    return img


# ---------------------------------------------------------------- render (raio por pixel, câmera ortográfica)

def mat_rot(rx, ry, rz):
    def rx_(a):
        c, s = math.cos(a), math.sin(a)
        return [[1, 0, 0], [0, c, -s], [0, s, c]]

    def ry_(a):
        c, s = math.cos(a), math.sin(a)
        return [[c, 0, s], [0, 1, 0], [-s, 0, c]]

    def rz_(a):
        c, s = math.cos(a), math.sin(a)
        return [[c, -s, 0], [s, c, 0], [0, 0, 1]]

    r = mat_mul(rz_(math.radians(rz)), mat_mul(ry_(math.radians(ry)), rx_(math.radians(rx))))
    return r


def mat_mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def mat_vec(m, v):
    return [sum(m[i][k] * v[k] for k in range(3)) for i in range(3)]


def transp(m):
    return [[m[j][i] for j in range(3)] for i in range(3)]


def afim(origem, rot):
    """p -> R (p - o) + o, guardado como (M, t)."""
    r = mat_rot(*rot)
    ro = mat_vec(r, origem)
    return r, [origem[i] - ro[i] for i in range(3)]


def compor(a, b):
    ma, ta = a
    mb, tb = b
    mt = mat_vec(ma, tb)
    return mat_mul(ma, mb), [mt[i] + ta[i] for i in range(3)]


IDENT = ([[1, 0, 0], [0, 1, 0], [0, 0, 1]], [0, 0, 0])


def cubos_no_mundo(modelo):
    por_uuid = {el["uuid"]: el for el in modelo["elements"]}
    saida = []

    def andar(filhos, tr):
        for f in filhos:
            if isinstance(f, str):
                el = por_uuid[f]
                t = tr
                if any(el.get("rotation", [0, 0, 0])):
                    t = compor(tr, afim(el["origin"], el["rotation"]))
                saida.append((el, t))
            else:
                andar(f.get("children", []), compor(tr, afim(f["origin"], f.get("rotation", [0, 0, 0]))))

    andar(modelo["outliner"], IDENT)
    return saida


def cruzar(p0, dl, lo, hi):
    tmin, tmax, eixo = -math.inf, math.inf, -1
    for k in range(3):
        if abs(dl[k]) < 1e-12:
            if p0[k] < lo[k] or p0[k] > hi[k]:
                return None
            continue
        t1 = (lo[k] - p0[k]) / dl[k]
        t2 = (hi[k] - p0[k]) / dl[k]
        if t1 > t2:
            t1, t2 = t2, t1
        if t1 > tmin:
            tmin, eixo = t1, k
        tmax = min(tmax, t2)
        if tmin > tmax:
            return None
    if eixo < 0 or tmax < 0:
        return None
    return tmin, eixo


NORMAL = {"east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0),
          "south": (0, 0, 1), "north": (0, 0, -1)}


def amostra(el, face, q, texturas, escala_uv):
    lo, hi = el["from"], el["to"]
    w, h, d = (hi[0] - lo[0]) or 1, (hi[1] - lo[1]) or 1, (hi[2] - lo[2]) or 1
    x, y, z = q
    fx, fy = {
        "north": ((hi[0] - x) / w, (hi[1] - y) / h),
        "south": ((x - lo[0]) / w, (hi[1] - y) / h),
        "east": ((hi[2] - z) / d, (hi[1] - y) / h),
        "west": ((z - lo[2]) / d, (hi[1] - y) / h),
        "up": ((x - lo[0]) / w, (z - lo[2]) / d),
        "down": ((x - lo[0]) / w, (hi[2] - z) / d),
    }[face]
    f = el["faces"].get(face)
    if not f or f.get("texture") is None:
        return None
    u1, v1, u2, v2 = f["uv"]
    tex = texturas[int(f["texture"])]
    u = (u1 + (u2 - u1) * fx) * escala_uv
    v = (v1 + (v2 - v1) * fy) * escala_uv
    umin, umax = sorted((u1 * escala_uv, u2 * escala_uv))
    vmin, vmax = sorted((v1 * escala_uv, v2 * escala_uv))
    ui = int(min(max(math.floor(u), umin), max(umin, umax - 1)))
    vi = int(min(max(math.floor(v), vmin), max(vmin, vmax - 1)))
    ui = min(max(ui, 0), len(tex[0]) - 1)
    vi = min(max(vi, 0), len(tex) - 1)
    return tex[vi][ui]


def normalizar(v):
    n = math.sqrt(sum(c * c for c in v))
    return [c / n for c in v]


def cruz(a, b):
    return [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]


def render(modelo, texturas, centro, tamanho, px_por_unidade, direcao, fundo):
    d = normalizar(direcao)
    direita = normalizar(cruz(d, [0, 1, 0]))
    cima = cruz(direita, d)
    lado = int(tamanho * px_por_unidade)
    img = imagem(lado, lado, cor(fundo))
    cubos = []
    for el, (m, t) in cubos_no_mundo(modelo):
        mt = transp(m)
        cubos.append((el, m, mt, t, mat_vec(mt, d)))
    escala_uv = 1.0
    for j in range(lado):
        for i in range(lado):
            a = (i + 0.5) / px_por_unidade - tamanho / 2
            b = tamanho / 2 - (j + 0.5) / px_por_unidade
            p = [centro[k] + direita[k] * a + cima[k] * b - d[k] * 100 for k in range(3)]
            melhor = None
            for el, m, mt, t, dl in cubos:
                p0 = mat_vec(mt, [p[k] - t[k] for k in range(3)])
                r = cruzar(p0, dl, el["from"], el["to"])
                if not r:
                    continue
                s, eixo = r
                if melhor and s >= melhor[0]:
                    continue
                face = (("west", "east"), ("down", "up"), ("north", "south"))[eixo][0 if dl[eixo] > 0 else 1]
                q = [p0[k] + dl[k] * s for k in range(3)]
                c = amostra(el, face, q, texturas, escala_uv)
                if c is None or c[3] == 0:
                    continue
                n = mat_vec(m, NORMAL[face])
                luz = 0.72 + 0.28 * n[1] - 0.08 * abs(n[0])
                melhor = (s, tuple(min(255, int(c[k] * luz)) for k in range(3)) + (255,))
            if melhor:
                img[j][i] = melhor[1]
    return img


def lado_a_lado(imgs, margem=8, fundo="#0f0e12"):
    h = max(len(i) for i in imgs) + 2 * margem
    w = sum(len(i[0]) for i in imgs) + margem * (len(imgs) + 1)
    tela = imagem(w, h, cor(fundo))
    x0 = margem
    for im in imgs:
        for y, linha in enumerate(im):
            tela[margem + y][x0:x0 + len(linha)] = linha
        x0 += len(im[0]) + margem
    return tela


VISTAS = {  # direção para onde a câmera olha
    "frente": [0, 0, 1],
    "lado": [-1, 0, 0],
    "costas": [0, 0, -1],
    "tres_quartos": [-1, -0.7, 1],
}


def embutir(caminho, modelo, pngs):
    for tex, dados in zip(modelo["textures"], pngs):
        tex["source"] = "data:image/png;base64," + base64.b64encode(dados).decode("ascii")
        tex["internal"] = True
    caminho.write_text(json.dumps(modelo, ensure_ascii=False, indent="\t") + "\n", encoding="utf-8")


def principal():
    TEXTURAS.mkdir(exist_ok=True)
    PREVIAS.mkdir(exist_ok=True)

    # Faminto
    arq = PASTA / "faminto.bbmodel"
    modelo = json.loads(arq.read_text(encoding="utf-8"))
    tex = textura_faminto(modelo)
    dados = png_bytes(tex)
    (TEXTURAS / "faminto.png").write_bytes(dados)
    vistas = [render(modelo, [tex], [0, 16, -4], 36, 10, VISTAS[v], "#17161c") for v in VISTAS]
    (PREVIAS / "faminto_render.png").write_bytes(png_bytes(lado_a_lado(vistas)))
    embutir(arq, modelo, [dados])

    # Atril
    arq = PASTA / "atril_sineiro.bbmodel"
    modelo = json.loads(arq.read_text(encoding="utf-8"))
    madeira, livro = textura_madeira(), textura_livro()
    pngs = [png_bytes(madeira), png_bytes(livro)]
    (TEXTURAS / "atril_madeira.png").write_bytes(pngs[0])
    (TEXTURAS / "atril_livro.png").write_bytes(pngs[1])
    vistas = [render(modelo, [madeira, livro], [8, 9, 8], 22, 14, VISTAS[v], "#17161c") for v in VISTAS]
    (PREVIAS / "atril_render.png").write_bytes(png_bytes(lado_a_lado(vistas)))
    embutir(arq, modelo, pngs)

    print("Pronto: texturas/, previas/*_render.png e .bbmodel com textura embutida.")


if __name__ == "__main__":
    principal()
