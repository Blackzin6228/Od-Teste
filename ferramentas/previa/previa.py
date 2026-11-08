#!/usr/bin/env python3
"""Prévia de modelos Blockbench sem instalar nada.

Lê um .bbmodel (Blockbench) ou .geo.json (GeckoLib/Bedrock) + textura PNG
e gera um PNG com 4 vistas: FRENTE, LADO, COSTAS e 3/4.

Só usa a biblioteca padrão do Python 3 (zlib, struct, json, base64).

Uso:
  python3 previa.py MODELO [--textura T.png] [--saida S.png] [--tamanho 320] [--fundo 34303b]

Convenções (as mesmas do Blockbench):
  - A frente do modelo olha para o NORTE (-Z).
  - Y para cima. Unidade = 1 pixel do Minecraft (16 unidades = 1 bloco).
"""
import argparse
import base64
import json
import math
import os
import struct
import sys
import zlib

# ---------------------------------------------------------------- PNG (ler)

def ler_png(dados):
    """Decodifica PNG (8 bits, ou paleta 1/2/4/8 bits, sem entrelaçamento). Retorna (w, h, pixels RGBA)."""
    if dados[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("não é PNG")
    pos, idat, paleta, trns = 8, b"", None, None
    w = h = prof = tipo = entrel = 0
    while pos < len(dados):
        tam, = struct.unpack(">I", dados[pos:pos + 4])
        nome = dados[pos + 4:pos + 8]
        corpo = dados[pos + 8:pos + 8 + tam]
        pos += 12 + tam
        if nome == b"IHDR":
            w, h, prof, tipo, _, _, entrel = struct.unpack(">IIBBBBB", corpo)
        elif nome == b"PLTE":
            paleta = [tuple(corpo[i:i + 3]) for i in range(0, len(corpo), 3)]
        elif nome == b"tRNS":
            trns = corpo
        elif nome == b"IDAT":
            idat += corpo
        elif nome == b"IEND":
            break
    if entrel:
        raise ValueError("PNG entrelaçado não suportado (salve sem 'interlace')")
    canais = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[tipo]
    if prof != 8 and tipo not in (0, 3):
        raise ValueError("só PNG de 8 bits por canal")
    bits_px = canais * prof
    bpp = max(1, bits_px // 8)
    linha_bytes = (w * bits_px + 7) // 8
    cru = zlib.decompress(idat)
    linhas, ant, p = [], bytearray(linha_bytes), 0
    for _ in range(h):
        filtro = cru[p]
        lin = bytearray(cru[p + 1:p + 1 + linha_bytes])
        p += 1 + linha_bytes
        for i in range(linha_bytes):
            a = lin[i - bpp] if i >= bpp else 0
            b = ant[i]
            c = ant[i - bpp] if i >= bpp else 0
            if filtro == 1:
                lin[i] = (lin[i] + a) & 255
            elif filtro == 2:
                lin[i] = (lin[i] + b) & 255
            elif filtro == 3:
                lin[i] = (lin[i] + ((a + b) >> 1)) & 255
            elif filtro == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                pr = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                lin[i] = (lin[i] + pr) & 255
        linhas.append(lin)
        ant = lin
    px = []
    for lin in linhas:
        if prof < 8:
            vals = []
            for byte in lin:
                for s in range(8 - prof, -1, -prof):
                    vals.append((byte >> s) & ((1 << prof) - 1))
            vals = vals[:w]
        else:
            vals = None
        for x in range(w):
            if tipo == 6:
                px.append(tuple(lin[x * 4:x * 4 + 4]))
            elif tipo == 2:
                r, g, b = lin[x * 3:x * 3 + 3]
                a = 0 if trns and len(trns) >= 6 and (r, g, b) == struct.unpack(">HHH", trns[:6]) else 255
                px.append((r, g, b, a))
            elif tipo == 3:
                i = vals[x] if vals is not None else lin[x]
                a = trns[i] if trns and i < len(trns) else 255
                px.append(paleta[i] + (a,))
            elif tipo == 0:
                v = vals[x] if vals is not None else lin[x]
                v = v * 255 // ((1 << prof) - 1)
                px.append((v, v, v, 255))
            elif tipo == 4:
                px.append((lin[x * 2], lin[x * 2], lin[x * 2], lin[x * 2 + 1]))
    return w, h, px


# ---------------------------------------------------------------- PNG (gravar)

def gravar_png(caminho, w, h, px):
    """Grava PNG RGBA 8 bits. px = lista de (r, g, b, a) linha a linha."""
    cru = bytearray()
    for y in range(h):
        cru.append(0)
        for x in range(w):
            cru.extend(px[y * w + x])

    def bloco(nome, corpo):
        c = nome + corpo
        return struct.pack(">I", len(corpo)) + c + struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + bloco(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += bloco(b"IDAT", zlib.compress(bytes(cru), 9)) + bloco(b"IEND", b"")
    pasta = os.path.dirname(caminho)
    if pasta:
        os.makedirs(pasta, exist_ok=True)
    with open(caminho, "wb") as f:
        f.write(png)


# ---------------------------------------------------------------- matemática

def mat_rot(rx, ry, rz):
    """Rotação em graus na ordem do Blockbench (Euler 'ZYX': aplica X, depois Y, depois Z)."""
    ax, ay, az = (math.radians(v) for v in (rx, ry, rz))
    cx, sx, cy, sy, cz, sz = math.cos(ax), math.sin(ax), math.cos(ay), math.sin(ay), math.cos(az), math.sin(az)
    X = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Y = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Z = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    return mul(Z, mul(Y, X))


def mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def aplica(m, v):
    return [m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
            m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
            m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]]


def gira_em_torno(p, pivo, rot):
    if not rot or not any(rot):
        return p
    d = aplica(mat_rot(*rot), [p[0] - pivo[0], p[1] - pivo[1], p[2] - pivo[2]])
    return [d[0] + pivo[0], d[1] + pivo[1], d[2] + pivo[2]]


# ---------------------------------------------------------------- faces de um cubo

def cantos_face(nome, a, b):
    """4 cantos na ordem da UV: topo-esq, topo-dir, baixo-dir, baixo-esq (igual ao Minecraft)."""
    x1, y1, z1 = a
    x2, y2, z2 = b
    return {
        "north": [(x2, y2, z1), (x1, y2, z1), (x1, y1, z1), (x2, y1, z1)],
        "south": [(x1, y2, z2), (x2, y2, z2), (x2, y1, z2), (x1, y1, z2)],
        "east":  [(x2, y2, z2), (x2, y2, z1), (x2, y1, z1), (x2, y1, z2)],
        "west":  [(x1, y2, z1), (x1, y2, z2), (x1, y1, z2), (x1, y1, z1)],
        "up":    [(x1, y2, z1), (x2, y2, z1), (x2, y2, z2), (x1, y2, z2)],
        "down":  [(x1, y1, z2), (x2, y1, z2), (x2, y1, z1), (x1, y1, z1)],
    }[nome]


def box_uv(u, v, tam, espelho=False):
    """UV automática (box UV) igual ao Blockbench."""
    sx, sy, sz = (math.floor(t + 1e-6) for t in tam)
    lista = {
        "east": ((0, sz), (sz, sy)),
        "west": ((sz + sx, sz), (sz, sy)),
        "up": ((sz + sx, sz), (-sx, -sz)),
        "down": ((sz + 2 * sx, 0), (-sx, sz)),
        "south": ((2 * sz + sx, sz), (sx, sy)),
        "north": ((sz, sz), (sx, sy)),
    }
    faces = {}
    for nome, ((fu, fv), (lu, lv)) in lista.items():
        uv = [u + fu, v + fv, u + fu + lu, v + fv + lv]
        if espelho:
            uv[0], uv[2] = uv[2], uv[0]
        faces[nome] = uv
    if espelho:
        faces["east"], faces["west"] = faces["west"], faces["east"]
    return faces


# Cada cubo vira um dicionário:
# {"de": [x,y,z], "ate": [x,y,z], "transf": [(pivo, rot), ...] (de dentro para fora),
#  "faces": {nome: {"uv": [u1,v1,u2,v2], "tex": indice, "rot": 0/90/180/270}}}

# ---------------------------------------------------------------- .bbmodel

def carregar_bbmodel(caminho):
    with open(caminho, encoding="utf-8") as f:
        m = json.load(f)
    res = m.get("resolution", {"width": 16, "height": 16})
    texs, ids = [], {}
    for i, t in enumerate(m.get("textures", [])):
        img = None
        src = t.get("source", "")
        if src.startswith("data:image/png;base64,"):
            img = ler_png(base64.b64decode(src.split(",", 1)[1]))
        else:
            for chave in ("relative_path", "path"):
                p = t.get(chave)
                if p:
                    p = p if os.path.isabs(p) else os.path.join(os.path.dirname(caminho), p)
                    if os.path.exists(p):
                        with open(p, "rb") as f:
                            img = ler_png(f.read())
                        break
        uvw = t.get("uv_width", res["width"])
        uvh = t.get("uv_height", res["height"])
        texs.append({"img": img, "uv_tam": (uvw, uvh), "nome": t.get("name", "?")})
        ids[t.get("uuid")] = i
        ids[str(t.get("id", i))] = i

    elementos = {e["uuid"]: e for e in m.get("elements", [])}
    grupos = {g["uuid"]: g for g in m.get("groups", [])}
    cubos, avisos = [], []

    def tex_indice(v):
        if v is None or v is False:
            return None
        if isinstance(v, int):
            return v
        return ids.get(str(v))

    def visita(no, cadeia):
        if isinstance(no, str):
            e = elementos.get(no)
            if not e:
                return
            if e.get("type", "cube") != "cube":
                avisos.append(f"elemento '{e.get('name')}' do tipo {e.get('type')} ignorado")
                return
            if e.get("visibility") is False:
                return
            inf = e.get("inflate", 0)
            de = [v - inf for v in e["from"]]
            ate = [v + inf for v in e["to"]]
            rot = e.get("rotation", [0, 0, 0])
            if isinstance(rot, dict):  # formato antigo de bloco Java
                rot = [rot["angle"] if rot["axis"] == ax else 0 for ax in "xyz"]
            transf = [(e.get("origin", [0, 0, 0]), rot)] + cadeia
            faces_auto = None
            if e.get("box_uv") or (m.get("meta", {}).get("box_uv") and "box_uv" not in e):
                off = e.get("uv_offset", [0, 0])
                tam = [b - a for a, b in zip(e["from"], e["to"])]
                faces_auto = box_uv(off[0], off[1], tam, e.get("mirror_uv", False))
            faces = {}
            for nome, f in e.get("faces", {}).items():
                ti = tex_indice(f.get("texture"))
                if ti is None:
                    continue
                uv = f.get("uv") or (faces_auto or {}).get(nome)
                if uv:
                    faces[nome] = {"uv": uv, "tex": ti, "rot": f.get("rotation", 0)}
            cubos.append({"de": de, "ate": ate, "transf": transf, "faces": faces})
            return
        # grupo (formato novo: só uuid + children; formato antigo: tudo junto)
        g = dict(grupos.get(no.get("uuid"), {}))
        g.update({k: v for k, v in no.items() if k != "children"})
        if g.get("visibility") is False:
            return
        nova = [(g.get("origin", [0, 0, 0]), g.get("rotation", [0, 0, 0]))] + cadeia
        for filho in no.get("children", []):
            visita(filho, nova)

    outliner = m.get("outliner")
    if outliner is None:
        outliner = list(elementos)
    for no in outliner:
        visita(no, [])
    return cubos, texs, avisos


# ---------------------------------------------------------------- .geo.json

def carregar_geo(caminho, textura):
    with open(caminho, encoding="utf-8") as f:
        m = json.load(f)
    geo = (m.get("minecraft:geometry") or [None])[0]
    if geo is None:
        raise ValueError("arquivo sem 'minecraft:geometry'")
    desc = geo.get("description", {})
    tw, th = desc.get("texture_width", 16), desc.get("texture_height", 16)
    texs = [{"img": textura, "uv_tam": (tw, th), "nome": "textura"}]
    ossos = {o["name"]: o for o in geo.get("bones", [])}

    def conv_pivo(p):
        return [-p[0], p[1], p[2]]

    def conv_rot(r):
        return [-r[0], -r[1], r[2]]

    def cadeia_de(nome):
        cadeia = []
        while nome and nome in ossos:
            o = ossos[nome]
            cadeia.append((conv_pivo(o.get("pivot", [0, 0, 0])), conv_rot(o.get("rotation", [0, 0, 0]))))
            nome = o.get("parent")
        return cadeia

    cubos = []
    for o in geo.get("bones", []):
        cadeia = cadeia_de(o["name"])
        for c in o.get("cubes", []):
            org, tam = c["origin"], c["size"]
            inf = c.get("inflate", 0)
            de = [-(org[0] + tam[0]), org[1], org[2]]
            ate = [de[0] + tam[0], de[1] + tam[1], de[2] + tam[2]]
            de = [v - inf for v in de]
            ate = [v + inf for v in ate]
            transf = [(conv_pivo(c.get("pivot", [0, 0, 0])), conv_rot(c.get("rotation", [0, 0, 0])))] + cadeia
            uv = c.get("uv", [0, 0])
            faces = {}
            if isinstance(uv, dict):
                for nome, f in uv.items():
                    u, v = f["uv"]
                    lu, lv = f.get("uv_size", [0, 0])
                    if nome in ("up", "down"):  # o Bedrock guarda topo/fundo invertidos
                        q = [u + lu, v + lv, u, v]
                    else:
                        q = [u, v, u + lu, v + lv]
                    faces[nome] = {"uv": q, "tex": 0, "rot": f.get("uv_rotation", 0)}
            else:
                for nome, q in box_uv(uv[0], uv[1], tam, c.get("mirror", o.get("mirror", False))).items():
                    faces[nome] = {"uv": q, "tex": 0, "rot": 0}
            cubos.append({"de": de, "ate": ate, "transf": transf, "faces": faces})
    return cubos, texs, []


# ---------------------------------------------------------------- render

LUZ = (0.45, 0.8, -0.4)  # de cima, um pouco da frente e da esquerda de quem olha
_n = math.sqrt(sum(v * v for v in LUZ))
LUZ = tuple(v / _n for v in LUZ)


def para_mundo(p, transf):
    for pivo, rot in transf:
        p = gira_em_torno(p, pivo, rot)
    return p


def vista(yaw, pitch):
    """Matriz que leva o mundo para a câmera. A câmera olha para +Z; tela x = -x da câmera."""
    a, b = math.radians(yaw), math.radians(pitch)
    Ry = [[math.cos(a), 0, math.sin(a)], [0, 1, 0], [-math.sin(a), 0, math.cos(a)]]
    Rx = [[1, 0, 0], [0, math.cos(b), -math.sin(b)], [0, math.sin(b), math.cos(b)]]
    return mul(Rx, Ry)


VISTAS = [("FRENTE", 0, 0), ("LADO", 90, 0), ("COSTAS", 180, 0), ("3/4", 35, -25)]


def montar_celulas(cubos, texs):
    """Quebra cada face em células de 1 texel: [(4 cantos no mundo, normal, cor)]."""
    celulas, faltando = [], set()
    for c in cubos:
        for nome, f in c["faces"].items():
            t = texs[f["tex"]] if f["tex"] < len(texs) else None
            if not t or not t["img"]:
                faltando.add(t["nome"] if t else str(f["tex"]))
                continue
            tw, th, tpx = t["img"]
            ex, ey = tw / t["uv_tam"][0], th / t["uv_tam"][1]
            u1, v1, u2, v2 = f["uv"]
            u1, u2, v1, v2 = u1 * ex, u2 * ex, v1 * ey, v2 * ey
            cant = [para_mundo(list(p), c["transf"]) for p in cantos_face(nome, c["de"], c["ate"])]
            e1 = [cant[1][i] - cant[0][i] for i in range(3)]
            e2 = [cant[3][i] - cant[0][i] for i in range(3)]
            n = [e2[1] * e1[2] - e2[2] * e1[1], e2[2] * e1[0] - e2[0] * e1[2], e2[0] * e1[1] - e2[1] * e1[0]]
            tn = math.sqrt(sum(v * v for v in n))
            if tn < 1e-9:
                continue
            n = [v / tn for v in n]
            nu = max(1, round(abs(u2 - u1)))
            nv = max(1, round(abs(v2 - v1)))
            rot = int(f.get("rot", 0)) % 360
            for j in range(nv):
                for i in range(nu):
                    s0, s1, t0, t1 = i / nu, (i + 1) / nu, j / nv, (j + 1) / nv
                    # rotação da UV na face
                    sc, tc = (s0 + s1) / 2, (t0 + t1) / 2
                    if rot == 90:
                        sc, tc = tc, 1 - sc
                    elif rot == 180:
                        sc, tc = 1 - sc, 1 - tc
                    elif rot == 270:
                        sc, tc = 1 - tc, sc
                    tu = u1 + sc * (u2 - u1)
                    tv = v1 + tc * (v2 - v1)
                    px = min(tw - 1, max(0, int(math.floor(tu))))
                    py = min(th - 1, max(0, int(math.floor(tv))))
                    cor = tpx[py * tw + px]
                    if cor[3] < 128:
                        continue

                    def ponto(s, tt):
                        return [cant[0][k] + s * e1[k] + tt * e2[k] for k in range(3)]

                    celulas.append(([ponto(s0, t0), ponto(s1, t0), ponto(s1, t1), ponto(s0, t1)], n, cor))
    return celulas, faltando


def render_vista(celulas, mat, tam, escala, centro, fundo):
    zbuf = [math.inf] * (tam * tam)
    img = [fundo] * (tam * tam)
    cx, cy = centro
    for cant, n, cor in celulas:
        nc = aplica(mat, n)
        if nc[2] > 1e-6:  # face de costas para a câmera
            continue
        luz = 0.55 + 0.45 * max(0.0, sum(n[k] * LUZ[k] for k in range(3)))
        rgb = tuple(min(255, int(cor[k] * luz)) for k in range(3)) + (255,)
        pts = []
        for p in cant:
            q = aplica(mat, p)
            pts.append(((-q[0] - cx) * escala + tam / 2, (cy - q[1]) * escala + tam / 2, q[2]))
        for tri in ((pts[0], pts[1], pts[2]), (pts[0], pts[2], pts[3])):
            (x0, y0, z0), (x1, y1, z1), (x2, y2, z2) = tri
            area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
            if abs(area) < 1e-9:
                continue
            xa, xb = max(0, int(math.floor(min(x0, x1, x2)))), min(tam - 1, int(math.ceil(max(x0, x1, x2))))
            ya, yb = max(0, int(math.floor(min(y0, y1, y2)))), min(tam - 1, int(math.ceil(max(y0, y1, y2))))
            for py in range(ya, yb + 1):
                fy = py + 0.5
                for px in range(xa, xb + 1):
                    fx = px + 0.5
                    w0 = ((x1 - fx) * (y2 - fy) - (x2 - fx) * (y1 - fy)) / area
                    w1 = ((x2 - fx) * (y0 - fy) - (x0 - fx) * (y2 - fy)) / area
                    w2 = 1 - w0 - w1
                    if w0 < -1e-6 or w1 < -1e-6 or w2 < -1e-6:
                        continue
                    z = w0 * z0 + w1 * z1 + w2 * z2
                    k = py * tam + px
                    if z < zbuf[k] - 1e-4:
                        zbuf[k] = z
                        img[k] = rgb
    return img


# ---------------------------------------------------------------- letras (3x5)

FONTE = {
    "F": "111100110100100", "R": "110101110101101", "E": "111100110100111", "N": "110101101101101",
    "T": "111010010010010", "L": "100100100100111", "A": "010101111101101", "D": "110101101101110",
    "O": "010101101101010", "C": "011100100100011", "S": "011100010001110", "3": "110001010001110",
    "/": "001001010100100", "4": "101101111001001", "B": "110101110101110", " ": "000000000000000",
    "1": "010110010010111", "6": "011100110101010", "X": "101101010101101", "U": "101101101101111",
}


def escreve(img, w, texto, x, y, cor, esc=2):
    for ch in texto.upper():
        g = FONTE.get(ch, FONTE[" "])
        for i, b in enumerate(g):
            if b == "1":
                for dy in range(esc):
                    for dx in range(esc):
                        px, py = x + (i % 3) * esc + dx, y + (i // 3) * esc + dy
                        if 0 <= px < w and 0 <= py < len(img) // w:
                            img[py * w + px] = cor
        x += 4 * esc


# ---------------------------------------------------------------- principal

def gerar(caminho, textura=None, saida=None, tam=320, fundo=(0x34, 0x30, 0x3B, 255)):
    tex_img = None
    if textura:
        with open(textura, "rb") as f:
            tex_img = ler_png(f.read())
    if caminho.endswith(".bbmodel"):
        cubos, texs, avisos = carregar_bbmodel(caminho)
        if tex_img:  # --textura troca todas as texturas do modelo
            for t in texs:
                t["img"] = tex_img
            if not texs:
                texs.append({"img": tex_img, "uv_tam": (tex_img[0], tex_img[1]), "nome": "textura"})
    elif caminho.endswith(".json"):
        if not tex_img:
            raise SystemExit("para .geo.json, informe --textura")
        cubos, texs, avisos = carregar_geo(caminho, tex_img)
    else:
        raise SystemExit("formato não reconhecido (use .bbmodel ou .geo.json)")

    celulas, faltando = montar_celulas(cubos, texs)
    for a in avisos:
        print("aviso:", a, file=sys.stderr)
    if faltando:
        print("aviso: textura não encontrada:", ", ".join(sorted(faltando)), file=sys.stderr)
    if not celulas:
        raise SystemExit("nada para desenhar (modelo vazio ou sem textura)")

    mats = [vista(yaw, pitch) for _, yaw, pitch in VISTAS]
    # mesma escala nas vistas, para dar para comparar tamanhos
    extensao, centros = 1e-6, []
    for mat in mats:
        xs, ys = [], []
        for cant, _, _ in celulas:
            for p in cant:
                q = aplica(mat, p)
                xs.append(-q[0])
                ys.append(q[1])
        extensao = max(extensao, max(xs) - min(xs), max(ys) - min(ys))
        centros.append(((max(xs) + min(xs)) / 2, (max(ys) + min(ys)) / 2))
    escala = tam * 0.8 / extensao

    topo = 22
    W, H = tam * len(VISTAS), tam + topo
    img = [fundo] * (W * H)
    claro = tuple(min(255, c + 40) for c in fundo[:3]) + (255,)
    for i, mat in enumerate(mats):
        v = render_vista(celulas, mat, tam, escala, centros[i], fundo)
        for y in range(tam):
            img[(y + topo) * W + i * tam:(y + topo) * W + (i + 1) * tam] = v[y * tam:(y + 1) * tam]
        escreve(img, W, VISTAS[i][0], i * tam + 8, 6, (0xE8, 0xE4, 0xF0, 255))
        if i:
            for y in range(H):
                img[y * W + i * tam] = claro
    # régua de 1 bloco (16 unidades) no canto
    r = int(round(16 * escala))
    if r < tam - 16:
        for x in range(8, 8 + r):
            img[(H - 8) * W + x] = claro
        for y in range(H - 12, H - 4):
            img[y * W + 8] = claro
            img[y * W + 8 + r - 1] = claro
        escreve(img, W, "1 B", 8 + r + 6, H - 14, claro)

    if not saida:
        base = os.path.basename(caminho).replace(".bbmodel", "").replace(".geo.json", "").replace(".json", "")
        saida = os.path.join(os.path.dirname(caminho), base + "_previa.png")
    gravar_png(saida, W, H, img)
    print(f"ok: {saida} ({len(cubos)} cubos, {len(celulas)} texels desenhados)")
    return saida


def main():
    ap = argparse.ArgumentParser(description="Gera PNG de prévia (frente, lado, costas, 3/4) de .bbmodel ou .geo.json")
    ap.add_argument("modelo")
    ap.add_argument("--textura", help="PNG da textura (obrigatório para .geo.json)")
    ap.add_argument("--saida", help="PNG de saída (padrão: <modelo>_previa.png)")
    ap.add_argument("--tamanho", type=int, default=320, help="lado de cada vista em pixels")
    ap.add_argument("--fundo", default="34303b", help="cor de fundo em hex")
    a = ap.parse_args()
    fundo = tuple(int(a.fundo[i:i + 2], 16) for i in (0, 2, 4)) + (255,)
    gerar(a.modelo, a.textura, a.saida, a.tamanho, fundo)


if __name__ == "__main__":
    main()
