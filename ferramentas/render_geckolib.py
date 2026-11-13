#!/usr/bin/env python3
"""Renderiza modelos GeckoLib (.geo.json + textura PNG + .animation.json) em PNG, sem Blockbench.

Só precisa de Python 3 e numpy (sem Pillow). Serve para a mesa Modelador gerar prévias (frente, lado,
costas, 3/4, silhueta e quadros de animação) antes de integrar.

Uso (pelo wrapper): bash ferramentas/render_geckolib.sh --geo X.geo.json --textura X.png --saida pasta/ \
    [--anim X.animation.json] [--brilho X_glowmask.png] [--escala 6] [--quadros 8] [--nome RETALHADO]

Convenções (iguais ao Blockbench/Bedrock): Y para cima, frente do modelo em -Z, rotações em graus na ordem
X, Y, Z (matriz Rz·Ry·Rx), pivô do osso em coordenadas do modelo. A "easing" de um quadro-chave vale para o
trecho que CHEGA nele (do quadro anterior até ele).
"""
import argparse
import json
import math
import struct
import zlib
from pathlib import Path

import numpy as np

# ------------------------------------------------------------------ PNG (ler e salvar, sem Pillow)


def salvar_png(caminho, img):
    """img: array (h, w, 4) uint8."""
    img = np.ascontiguousarray(img, dtype=np.uint8)
    h, w = img.shape[:2]
    linhas = np.concatenate([np.zeros((h, 1), np.uint8), img.reshape(h, w * 4)], axis=1)
    bruto = linhas.tobytes()

    def bloco(tipo, dados):
        return struct.pack(">I", len(dados)) + tipo + dados + struct.pack(">I", zlib.crc32(tipo + dados) & 0xFFFFFFFF)

    Path(caminho).parent.mkdir(parents=True, exist_ok=True)
    with open(caminho, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(bloco(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)))
        f.write(bloco(b"IDAT", zlib.compress(bruto, 9)))
        f.write(bloco(b"IEND", b""))


def ler_png(caminho):
    """Lê PNG 8 bits (RGB, RGBA, cinza, cinza+alfa ou paleta), sem entrelaçamento. Devolve (h, w, 4) uint8."""
    dados = Path(caminho).read_bytes()
    assert dados[:8] == b"\x89PNG\r\n\x1a\n", "não é PNG"
    pos = 8
    idat = b""
    paleta = None
    trns = None
    while pos < len(dados):
        tam = struct.unpack(">I", dados[pos:pos + 4])[0]
        tipo = dados[pos + 4:pos + 8]
        corpo = dados[pos + 8:pos + 8 + tam]
        pos += 12 + tam
        if tipo == b"IHDR":
            w, h, bits, cor, _, _, entrelace = struct.unpack(">IIBBBBB", corpo)
            assert bits == 8 and entrelace == 0, "só PNG 8 bits sem entrelaçamento"
        elif tipo == b"PLTE":
            paleta = np.frombuffer(corpo, np.uint8).reshape(-1, 3)
        elif tipo == b"tRNS":
            trns = np.frombuffer(corpo, np.uint8)
        elif tipo == b"IDAT":
            idat += corpo
        elif tipo == b"IEND":
            break
    canais = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[cor]
    bruto = np.frombuffer(zlib.decompress(idat), np.uint8)
    passo = w * canais
    bruto = bruto.reshape(h, passo + 1)
    saida = np.zeros((h, passo), np.uint8)
    anterior = np.zeros(passo, np.int32)
    for y in range(h):
        filtro = bruto[y, 0]
        linha = bruto[y, 1:].astype(np.int32)
        if filtro == 0:
            atual = linha
        elif filtro == 2:
            atual = (linha + anterior) & 255
        else:
            atual = np.zeros(passo, np.int32)
            for i in range(passo):
                a = atual[i - canais] if i >= canais else 0
                b = anterior[i]
                c = anterior[i - canais] if i >= canais else 0
                if filtro == 1:
                    p = a
                elif filtro == 3:
                    p = (a + b) >> 1
                else:
                    pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                    p = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                atual[i] = (linha[i] + p) & 255
        saida[y] = atual
        anterior = atual
    px = saida.reshape(h, w, canais)
    if cor == 3:
        rgb = paleta[px[:, :, 0]]
        alfa = np.full((h, w, 1), 255, np.uint8)
        if trns is not None:
            tabela = np.full(256, 255, np.uint8)
            tabela[:len(trns)] = trns
            alfa = tabela[px[:, :, 0]][:, :, None]
        return np.concatenate([rgb, alfa], axis=2)
    if canais == 1:
        return np.concatenate([px, px, px, np.full((h, w, 1), 255, np.uint8)], axis=2)
    if canais == 2:
        return np.concatenate([px[:, :, :1]] * 3 + [px[:, :, 1:2]], axis=2)
    if canais == 3:
        return np.concatenate([px, np.full((h, w, 1), 255, np.uint8)], axis=2)
    return px.copy()


# ------------------------------------------------------------------ fonte de pixel 5x7 (nossa)

def _g(*linhas):
    return linhas


FONTE = {
    " ": _g("     ", "     ", "     ", "     ", "     ", "     ", "     "),
    "?": _g(" ### ", "#   #", "    #", "   # ", "  #  ", "     ", "  #  "),
    "A": _g(" ### ", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"),
    "B": _g("#### ", "#   #", "#   #", "#### ", "#   #", "#   #", "#### "),
    "C": _g(" ### ", "#   #", "#    ", "#    ", "#    ", "#   #", " ### "),
    "D": _g("#### ", "#   #", "#   #", "#   #", "#   #", "#   #", "#### "),
    "E": _g("#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#####"),
    "F": _g("#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#    "),
    "G": _g(" ### ", "#   #", "#    ", "# ###", "#   #", "#   #", " ####"),
    "H": _g("#   #", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"),
    "I": _g(" ### ", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "),
    "J": _g("  ###", "   # ", "   # ", "   # ", "   # ", "#  # ", " ##  "),
    "K": _g("#   #", "#  # ", "# #  ", "##   ", "# #  ", "#  # ", "#   #"),
    "L": _g("#    ", "#    ", "#    ", "#    ", "#    ", "#    ", "#####"),
    "M": _g("#   #", "## ##", "# # #", "# # #", "#   #", "#   #", "#   #"),
    "N": _g("#   #", "##  #", "# # #", "#  ##", "#   #", "#   #", "#   #"),
    "O": _g(" ### ", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "),
    "P": _g("#### ", "#   #", "#   #", "#### ", "#    ", "#    ", "#    "),
    "Q": _g(" ### ", "#   #", "#   #", "#   #", "# # #", "#  # ", " ## #"),
    "R": _g("#### ", "#   #", "#   #", "#### ", "# #  ", "#  # ", "#   #"),
    "S": _g(" ####", "#    ", "#    ", " ### ", "    #", "    #", "#### "),
    "T": _g("#####", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", "  #  "),
    "U": _g("#   #", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "),
    "V": _g("#   #", "#   #", "#   #", "#   #", "#   #", " # # ", "  #  "),
    "W": _g("#   #", "#   #", "#   #", "# # #", "# # #", "## ##", "#   #"),
    "X": _g("#   #", "#   #", " # # ", "  #  ", " # # ", "#   #", "#   #"),
    "Y": _g("#   #", "#   #", " # # ", "  #  ", "  #  ", "  #  ", "  #  "),
    "Z": _g("#####", "    #", "   # ", "  #  ", " #   ", "#    ", "#####"),
    "0": _g(" ### ", "#   #", "#  ##", "# # #", "##  #", "#   #", " ### "),
    "1": _g("  #  ", " ##  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "),
    "2": _g(" ### ", "#   #", "    #", "   # ", "  #  ", " #   ", "#####"),
    "3": _g("#### ", "    #", "    #", " ### ", "    #", "    #", "#### "),
    "4": _g("   # ", "  ## ", " # # ", "#  # ", "#####", "   # ", "   # "),
    "5": _g("#####", "#    ", "#### ", "    #", "    #", "#   #", " ### "),
    "6": _g(" ### ", "#    ", "#    ", "#### ", "#   #", "#   #", " ### "),
    "7": _g("#####", "    #", "   # ", "  #  ", " #   ", " #   ", " #   "),
    "8": _g(" ### ", "#   #", "#   #", " ### ", "#   #", "#   #", " ### "),
    "9": _g(" ### ", "#   #", "#   #", " ####", "    #", "    #", " ### "),
    ":": _g("     ", "  #  ", "  #  ", "     ", "  #  ", "  #  ", "     "),
    ".": _g("     ", "     ", "     ", "     ", "     ", " ##  ", " ##  "),
    ",": _g("     ", "     ", "     ", "     ", " ##  ", "  #  ", " #   "),
    "-": _g("     ", "     ", "     ", " ### ", "     ", "     ", "     "),
    "+": _g("     ", "  #  ", "  #  ", "#####", "  #  ", "  #  ", "     "),
    "/": _g("    #", "    #", "   # ", "  #  ", " #   ", "#    ", "#    "),
    "(": _g("   # ", "  #  ", " #   ", " #   ", " #   ", "  #  ", "   # "),
    ")": _g(" #   ", "  #  ", "   # ", "   # ", "   # ", "  #  ", " #   "),
    "_": _g("     ", "     ", "     ", "     ", "     ", "     ", "#####"),
    "=": _g("     ", "     ", "#####", "     ", "#####", "     ", "     "),
    "%": _g("##  #", "## # ", "  #  ", "  #  ", " #   ", "#  ##", "#  ##"),
}


def texto(img, pos, txt, cor=(230, 230, 230, 255), escala=2):
    """Escreve texto (maiúsculas, dígitos e pontuação simples) com a fonte de pixel. Devolve a largura usada."""
    h, w = img.shape[:2]
    x0, y = pos
    cor = np.array(cor if len(cor) == 4 else tuple(cor) + (255,), np.uint8)
    maior = 0
    for linha in txt.split("\n"):
        x = x0
        for ch in linha.upper():
            g = FONTE.get(ch, FONTE["?"])
            for r, bits in enumerate(g):
                for c, b in enumerate(bits):
                    if b == "#":
                        px, py = x + c * escala, y + r * escala
                        if 0 <= px < w and 0 <= py < h:
                            img[py:py + escala, px:px + escala] = cor
            x += 6 * escala
        maior = max(maior, x - x0)
        y += 9 * escala
    return maior


# ------------------------------------------------------------------ matrizes


def rot_x(a):
    a = math.radians(a)
    c, s = math.cos(a), math.sin(a)
    m = np.eye(4)
    m[1, 1], m[1, 2], m[2, 1], m[2, 2] = c, -s, s, c
    return m


def rot_y(a):
    a = math.radians(a)
    c, s = math.cos(a), math.sin(a)
    m = np.eye(4)
    m[0, 0], m[0, 2], m[2, 0], m[2, 2] = c, s, -s, c
    return m


def rot_z(a):
    a = math.radians(a)
    c, s = math.cos(a), math.sin(a)
    m = np.eye(4)
    m[0, 0], m[0, 1], m[1, 0], m[1, 1] = c, -s, s, c
    return m


def trans(v):
    m = np.eye(4)
    m[:3, 3] = v
    return m


def escala_m(v):
    m = np.eye(4)
    m[0, 0], m[1, 1], m[2, 2] = v
    return m


def rot_xyz(r):
    """Rotação Bedrock/Blockbench: X, depois Y, depois Z (matriz Rz·Ry·Rx)."""
    return rot_z(r[2]) @ rot_y(r[1]) @ rot_x(r[0])


# ------------------------------------------------------------------ modelo (.geo.json)

FACES = ["north", "south", "east", "west", "up", "down"]


def _retangulos_box_uv(u, v, w, h, d, mirror):
    r = {
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + 2 * d + w, v + d, w, h),
        "up": (u + d, v, w, d),
        "down": (u + d + w, v, w, d),
    }
    if mirror:
        r["east"], r["west"] = r["west"], r["east"]
    return r


class Modelo:
    def __init__(self, geo):
        if isinstance(geo, (str, Path)):
            geo = json.loads(Path(geo).read_text(encoding="utf-8"))
        g = geo["minecraft:geometry"][0]
        d = g["description"]
        self.tex_w = d.get("texture_width", 64)
        self.tex_h = d.get("texture_height", 64)
        self.identificador = d.get("identifier", "geometry.modelo")
        self.ossos = {}
        self.ordem = []
        for b in g["bones"]:
            self.ossos[b["name"]] = b
        # ordem: pais antes dos filhos
        vistos = set()

        def visitar(nome):
            if nome in vistos or nome not in self.ossos:
                return
            pai = self.ossos[nome].get("parent")
            if pai:
                visitar(pai)
            vistos.add(nome)
            self.ordem.append(nome)

        for nome in self.ossos:
            visitar(nome)

    def matrizes(self, pose=None):
        """pose: {osso: {"rotation": [..], "position": [..], "scale": [..]}} vindo da animação."""
        pose = pose or {}
        mundo = {}
        for nome in self.ordem:
            b = self.ossos[nome]
            piv = np.array(b.get("pivot", [0, 0, 0]), float)
            rot = np.array(b.get("rotation", [0, 0, 0]), float)
            p = pose.get(nome, {})
            rot = rot + np.array(p.get("rotation", [0, 0, 0]), float)
            pos = np.array(p.get("position", [0, 0, 0]), float)
            esc = np.array(p.get("scale", [1, 1, 1]), float)
            local = trans(pos) @ trans(piv) @ rot_xyz(rot) @ escala_m(esc) @ trans(-piv)
            pai = b.get("parent")
            mundo[nome] = (mundo[pai] if pai in mundo else np.eye(4)) @ local
        return mundo

    def faces(self, pose=None, so_ossos=None):
        """Lista de faces no espaço do mundo: dict(v=(4,3), uv=(u0,v0,u1,v1), n=(3,), cubo=nome)."""
        mundo = self.matrizes(pose)
        saida = []
        for nome in self.ordem:
            if so_ossos and nome not in so_ossos:
                continue
            b = self.ossos[nome]
            M = mundo[nome]
            for c in b.get("cubes", []):
                o = np.array(c["origin"], float)
                s = np.array(c["size"], float)
                inf = float(c.get("inflate", 0))
                x0, y0, z0 = o - inf
                x1, y1, z1 = o + s + inf
                mirror = bool(c.get("mirror", b.get("mirror", False)))
                uv = c.get("uv", [0, 0])
                if isinstance(uv, dict):
                    rects = {}
                    for f in FACES:
                        if f in uv:
                            fu, fv = uv[f]["uv"]
                            fw, fh = uv[f].get("uv_size", [1, 1])
                            rects[f] = (fu, fv, fw, fh)
                else:
                    rects = _retangulos_box_uv(uv[0], uv[1], s[0], s[1], s[2], mirror)
                Mc = M
                if "rotation" in c:
                    pc = np.array(c.get("pivot", o + s / 2), float)
                    Mc = M @ trans(pc) @ rot_xyz(c["rotation"]) @ trans(-pc)
                cantos = {
                    "north": ([x1, y1, z0], [x0, y1, z0], [x0, y0, z0], [x1, y0, z0], [0, 0, -1]),
                    "south": ([x0, y1, z1], [x1, y1, z1], [x1, y0, z1], [x0, y0, z1], [0, 0, 1]),
                    "east": ([x1, y1, z1], [x1, y1, z0], [x1, y0, z0], [x1, y0, z1], [1, 0, 0]),
                    "west": ([x0, y1, z0], [x0, y1, z1], [x0, y0, z1], [x0, y0, z0], [-1, 0, 0]),
                    "up": ([x1, y1, z1], [x0, y1, z1], [x0, y1, z0], [x1, y1, z0], [0, 1, 0]),
                    "down": ([x1, y0, z0], [x0, y0, z0], [x0, y0, z1], [x1, y0, z1], [0, -1, 0]),
                }
                for f, (a, bq, cq, dq, n) in cantos.items():
                    if f not in rects:
                        continue
                    fu, fv, fw, fh = rects[f]
                    if fw == 0 or fh == 0:
                        continue
                    v = np.array([a, bq, cq, dq], float)
                    v = (Mc @ np.concatenate([v, np.ones((4, 1))], axis=1).T).T[:, :3]
                    nn = (Mc[:3, :3] @ np.array(n, float))
                    nn /= (np.linalg.norm(nn) + 1e-9)
                    u0, u1 = fu, fu + fw
                    if mirror:
                        u0, u1 = u1, u0
                    saida.append({"v": v, "uv": (u0, fv, u1, fv + fh), "n": nn, "cubo": c.get("name", nome), "osso": nome})
        return saida

    def caixa(self, poses=(None,)):
        """Caixa (min, max) do modelo ao longo das poses dadas."""
        mn = np.full(3, np.inf)
        mx = np.full(3, -np.inf)
        for p in poses:
            for f in self.faces(p):
                mn = np.minimum(mn, f["v"].min(axis=0))
                mx = np.maximum(mx, f["v"].max(axis=0))
        return mn, mx


# ------------------------------------------------------------------ animação (.animation.json)

def _ease(nome, s):
    s = min(max(s, 0.0), 1.0)
    if nome in (None, "linear"):
        return s
    if nome == "step":
        return 0.0 if s < 1 else 1.0
    if nome == "easeInSine":
        return 1 - math.cos(s * math.pi / 2)
    if nome == "easeOutSine":
        return math.sin(s * math.pi / 2)
    if nome == "easeInOutSine":
        return -(math.cos(math.pi * s) - 1) / 2
    if nome == "easeInQuad":
        return s * s
    if nome == "easeOutQuad":
        return 1 - (1 - s) ** 2
    if nome == "easeInOutQuad":
        return 2 * s * s if s < 0.5 else 1 - (-2 * s + 2) ** 2 / 2
    if nome == "easeInCubic":
        return s ** 3
    if nome == "easeOutCubic":
        return 1 - (1 - s) ** 3
    if nome == "easeInOutCubic":
        return 4 * s ** 3 if s < 0.5 else 1 - (-2 * s + 2) ** 3 / 2
    if nome == "easeInBack":
        c1, c3 = 1.70158, 2.70158
        return c3 * s ** 3 - c1 * s ** 2
    if nome == "easeOutBack":
        c1, c3 = 1.70158, 2.70158
        return 1 + c3 * (s - 1) ** 3 + c1 * (s - 1) ** 2
    if nome == "easeOutBounce":
        n1, d1 = 7.5625, 2.75
        if s < 1 / d1:
            return n1 * s * s
        if s < 2 / d1:
            s -= 1.5 / d1
            return n1 * s * s + 0.75
        if s < 2.5 / d1:
            s -= 2.25 / d1
            return n1 * s * s + 0.9375
        s -= 2.625 / d1
        return n1 * s * s + 0.984375
    if nome == "easeOutElastic":
        c4 = (2 * math.pi) / 3
        return 0 if s == 0 else 1 if s == 1 else 2 ** (-10 * s) * math.sin((s * 10 - 0.75) * c4) + 1
    return s


def _num(v):
    if isinstance(v, (int, float)):
        return float(v)
    try:
        return float(v)
    except (TypeError, ValueError):
        return 0.0  # Molang: não avaliado na prévia


def _vetor(v):
    if isinstance(v, (int, float, str)):
        n = _num(v)
        return [n, n, n]
    return [_num(x) for x in v[:3]] + [0.0] * (3 - len(v[:3]))


def _chaves(canal):
    """Converte um canal em lista [(t, vetor, easing, catmull)] ordenada."""
    if isinstance(canal, (list, int, float, str)):
        return [(0.0, _vetor(canal), None, False)]
    saida = []
    for t, val in canal.items():
        easing, catmull = None, False
        if isinstance(val, dict):
            if "vector" in val:
                vec = _vetor(val["vector"])
            elif "post" in val:
                vec = _vetor(val["post"])
            elif "pre" in val:
                vec = _vetor(val["pre"])
            else:
                vec = [0.0, 0.0, 0.0]
            easing = val.get("easing")
            catmull = val.get("lerp_mode") == "catmullrom"
        else:
            vec = _vetor(val)
        saida.append((float(t), vec, easing, catmull))
    saida.sort(key=lambda k: k[0])
    return saida


def _avaliar(chaves, t):
    if len(chaves) == 1 or t <= chaves[0][0]:
        return chaves[0][1]
    if t >= chaves[-1][0]:
        return chaves[-1][1]
    for i in range(1, len(chaves)):
        t0, v0, _, _ = chaves[i - 1]
        t1, v1, easing, catmull = chaves[i]
        if t0 <= t <= t1:
            s = (t - t0) / (t1 - t0) if t1 > t0 else 1.0
            if catmull:
                p0 = chaves[max(i - 2, 0)][1]
                p3 = chaves[min(i + 1, len(chaves) - 1)][1]
                return [
                    0.5 * ((2 * v0[k]) + (-p0[k] + v1[k]) * s + (2 * p0[k] - 5 * v0[k] + 4 * v1[k] - p3[k]) * s * s
                           + (-p0[k] + 3 * v0[k] - 3 * v1[k] + p3[k]) * s ** 3) for k in range(3)]
            e = _ease(easing, s)
            return [v0[k] + (v1[k] - v0[k]) * e for k in range(3)]
    return chaves[-1][1]


class Animacao:
    def __init__(self, dados, nome=None):
        if isinstance(dados, (str, Path)):
            dados = json.loads(Path(dados).read_text(encoding="utf-8"))
        self.todas = dados["animations"]
        self.nome = nome or next(iter(self.todas))
        a = self.todas[self.nome]
        self.loop = a.get("loop", False) in (True, "true", "loop")
        self.duracao = float(a.get("animation_length", 0) or self._ultimo_tempo(a))
        self.ossos = {o: {c: _chaves(canal) for c, canal in canais.items() if c in ("rotation", "position", "scale")}
                      for o, canais in a.get("bones", {}).items()}

    @staticmethod
    def _ultimo_tempo(a):
        maior = 0.0
        for canais in a.get("bones", {}).values():
            for canal in canais.values():
                if isinstance(canal, dict):
                    maior = max([maior] + [float(t) for t in canal])
        return maior or 1.0

    @classmethod
    def nomes(cls, caminho):
        return list(json.loads(Path(caminho).read_text(encoding="utf-8"))["animations"])

    def pose(self, t):
        if self.loop and self.duracao > 0:
            t = t % self.duracao
        saida = {}
        for osso, canais in self.ossos.items():
            saida[osso] = {c: _avaliar(ch, t) for c, ch in canais.items()}
        return saida


# ------------------------------------------------------------------ câmera e rasterização

class Camera:
    """Ortográfica. azimute 0 = olhando a frente do modelo (câmera em -Z); 90 = câmera em +X (lado direito)."""

    def __init__(self, azimute=0.0, elevacao=0.0):
        az, el = math.radians(azimute), math.radians(elevacao)
        d = np.array([math.sin(az) * math.cos(el), math.sin(el), -math.cos(az) * math.cos(el)])
        self.f = -d / np.linalg.norm(d)
        cima = np.array([0.0, 1.0, 0.0])
        r = np.cross(self.f, cima)
        if np.linalg.norm(r) < 1e-6:
            r = np.array([-1.0, 0.0, 0.0])
        self.r = r / np.linalg.norm(r)
        self.u = np.cross(self.r, self.f)
        # luz: de cima, um pouco da esquerda e da frente da câmera
        luz = -0.35 * self.r + 0.85 * self.u - 0.55 * self.f
        self.luz = luz / np.linalg.norm(luz)

    def projetar(self, pontos, centro, escala, w, h):
        p = np.asarray(pontos, float) - centro
        sx = p @ self.r * escala + w / 2
        sy = -(p @ self.u) * escala + h / 2
        sz = p @ self.f
        return sx, sy, sz


def _preencher_triangulo(img, zbuf, p, z, uvs, tex, glow, sombra, alvo_cor=None):
    """p: (3,2) tela; z: (3,) profundidade; uvs: (3,2) texels; tex: (h,w,4)."""
    H, W = zbuf.shape
    xmin = max(int(math.floor(p[:, 0].min())), 0)
    xmax = min(int(math.ceil(p[:, 0].max())), W - 1)
    ymin = max(int(math.floor(p[:, 1].min())), 0)
    ymax = min(int(math.ceil(p[:, 1].max())), H - 1)
    if xmin > xmax or ymin > ymax:
        return
    (x0, y0), (x1, y1), (x2, y2) = p
    area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
    if abs(area) < 1e-9:
        return
    xs = np.arange(xmin, xmax + 1) + 0.5
    ys = np.arange(ymin, ymax + 1) + 0.5
    X, Y = np.meshgrid(xs, ys)
    w0 = ((x1 - X) * (y2 - Y) - (x2 - X) * (y1 - Y)) / area
    w1 = ((x2 - X) * (y0 - Y) - (x0 - X) * (y2 - Y)) / area
    w2 = 1 - w0 - w1
    eps = -1e-4
    dentro = (w0 >= eps) & (w1 >= eps) & (w2 >= eps)
    if not dentro.any():
        return
    Z = w0 * z[0] + w1 * z[1] + w2 * z[2]
    regiao = zbuf[ymin:ymax + 1, xmin:xmax + 1]
    passa = dentro & (Z < regiao)
    if not passa.any():
        return
    U = w0 * uvs[0, 0] + w1 * uvs[1, 0] + w2 * uvs[2, 0]
    V = w0 * uvs[0, 1] + w1 * uvs[1, 1] + w2 * uvs[2, 1]
    th, tw = tex.shape[:2]
    ui = np.clip(np.floor(U).astype(int), 0, tw - 1)
    vi = np.clip(np.floor(V).astype(int), 0, th - 1)
    cor = tex[vi, ui]
    opaco = cor[:, :, 3] > 8
    passa &= opaco
    if not passa.any():
        return
    rgb = cor[:, :, :3].astype(float) * sombra
    if glow is not None:
        gl = glow[vi, ui]
        brilha = gl[:, :, 3] > 8
        rgb = np.where(brilha[:, :, None], np.minimum(gl[:, :, :3].astype(float) * 1.08, 255), rgb)
    if alvo_cor is not None:
        rgb = np.broadcast_to(np.array(alvo_cor, float), rgb.shape)
    regiao[passa] = Z[passa]
    sub = img[ymin:ymax + 1, xmin:xmax + 1]
    sub[passa, :3] = np.clip(rgb[passa], 0, 255).astype(np.uint8)
    sub[passa, 3] = 255


def renderizar(modelo, tex, cam, pose=None, glow=None, escala=6.0, tamanho=(320, 420), centro=None,
               fundo=(24, 24, 28, 255), silhueta=False, chao=True, regua=True, rotulo=None):
    w, h = tamanho
    img = np.zeros((h, w, 4), np.uint8)
    img[:, :] = fundo
    zbuf = np.full((h, w), np.inf)
    faces = modelo.faces(pose)
    if centro is None:
        mn, mx = modelo.caixa((pose,))
        centro = np.array([(mn[0] + mx[0]) / 2, (mn[1] + mx[1]) / 2, (mn[2] + mx[2]) / 2])
    # sombra no chão (elipse), só se o chão aparece de cima
    if chao and not silhueta:
        mn, mx = modelo.caixa((pose,))
        cx, cz = (mn[0] + mx[0]) / 2, (mn[2] + mx[2]) / 2
        r1, r2 = (mx[0] - mn[0]) / 2 * 0.9, (mx[2] - mn[2]) / 2 * 0.9
        c0 = np.array(cam.projetar([[cx, 0, cz]], centro, escala, w, h)[:2]).ravel()
        a = np.array(cam.projetar([[cx + r1, 0, cz]], centro, escala, w, h)[:2]).ravel() - c0
        b = np.array(cam.projetar([[cx, 0, cz + r2]], centro, escala, w, h)[:2]).ravel() - c0
        Mat = np.array([a, b]).T
        if abs(np.linalg.det(Mat)) > 1e-3:
            inv = np.linalg.inv(Mat)
            Y, X = np.mgrid[0:h, 0:w]
            P = np.stack([X + 0.5 - c0[0], Y + 0.5 - c0[1]], axis=-1)
            ab = P @ inv.T
            dentro = (ab[:, :, 0] ** 2 + ab[:, :, 1] ** 2) <= 1
            img[dentro, :3] = (img[dentro, :3].astype(float) * 0.55).astype(np.uint8)
        else:
            # vista de lado: só uma linha de chão
            y = int(round(c0[1]))
            if 0 <= y < h:
                img[y, :, :3] = (img[y, :, :3].astype(float) * 0.6).astype(np.uint8)
    for f in faces:
        sx, sy, sz = cam.projetar(f["v"], centro, escala, w, h)
        n = f["n"]
        if np.dot(n, cam.f) > 0:
            n = -n
        dif = max(0.0, float(np.dot(n, cam.luz)))
        sombra = 0.42 + 0.58 * dif
        if abs(n[1]) > 0.9 and n[1] > 0:
            sombra = min(1.0, sombra + 0.08)
        u0, v0, u1, v1 = f["uv"]
        uvq = np.array([[u0, v0], [u1, v0], [u1, v1], [u0, v1]], float)
        pts = np.stack([sx, sy], axis=1)
        for tri in ((0, 1, 2), (0, 2, 3)):
            _preencher_triangulo(img, zbuf, pts[list(tri)], sz[list(tri)], uvq[list(tri)], tex,
                                 None if silhueta else glow, sombra,
                                 alvo_cor=(0, 0, 0) if silhueta else None)
    if regua and not silhueta:
        mn, mx = modelo.caixa((None,))
        altura = mx[1]
        cx, cz = (mn[0] + mx[0]) / 2, (mn[2] + mx[2]) / 2
        n_blocos = int(math.ceil(altura / 16.0 + 0.01))
        xr = 10
        for k in range(0, n_blocos + 1):
            yk = cam.projetar([[cx, k * 16.0, cz]], centro, escala, w, h)[1]
            yk = int(round(float(yk[0])))
            if 0 <= yk < h:
                img[yk, xr:xr + 10, :3] = (150, 150, 155)
                if k > 0:
                    texto(img, (xr + 13, yk - 4), f"{k}", (150, 150, 155, 255), 1)
        y0 = int(round(float(cam.projetar([[cx, 0, cz]], centro, escala, w, h)[1][0])))
        y1 = int(round(float(cam.projetar([[cx, n_blocos * 16.0, cz]], centro, escala, w, h)[1][0])))
        ya, yb = sorted((y0, y1))
        img[max(ya, 0):min(yb, h - 1) + 1, xr + 9, :3] = (150, 150, 155)
    if rotulo:
        texto(img, (8, 8), rotulo, (235, 235, 235, 255), 2)
    return img


def juntar_horizontal(imagens, espaco=6, fundo=(24, 24, 28, 255)):
    h = max(i.shape[0] for i in imagens)
    w = sum(i.shape[1] for i in imagens) + espaco * (len(imagens) - 1)
    saida = np.zeros((h, w, 4), np.uint8)
    saida[:, :] = fundo
    x = 0
    for i in imagens:
        saida[:i.shape[0], x:x + i.shape[1]] = i
        x += i.shape[1] + espaco
    return saida


def juntar_vertical(imagens, espaco=6, fundo=(24, 24, 28, 255)):
    w = max(i.shape[1] for i in imagens)
    h = sum(i.shape[0] for i in imagens) + espaco * (len(imagens) - 1)
    saida = np.zeros((h, w, 4), np.uint8)
    saida[:, :] = fundo
    y = 0
    for i in imagens:
        saida[y:y + i.shape[0], :i.shape[1]] = i
        y += i.shape[0] + espaco
    return saida


def faixa_titulo(txt, largura, altura=30, fundo=(12, 12, 14, 255), cor=(224, 49, 58, 255)):
    img = np.zeros((altura, largura, 4), np.uint8)
    img[:, :] = fundo
    texto(img, (8, (altura - 14) // 2), txt, cor, 2)
    return img


VISTAS = [("FRENTE", 0, 0), ("LADO DIREITO", 90, 0), ("COSTAS", 180, 0), ("3/4", 35, 22)]


def tamanho_para(modelo, escala, poses=(None,), extra=1.0):
    mn, mx = modelo.caixa(poses)
    ext = max(mx[0] - mn[0], mx[2] - mn[2]) * 1.05 + 16
    alt = (mx[1] - min(mn[1], 0)) + 16
    lado = max(ext, alt * 0.6) * extra
    w = int(math.ceil(max(lado, 40) * escala)) + 40
    h = int(math.ceil(max(alt, 40) * escala * extra)) + 40
    return (w, h)


def centro_para(modelo, poses=(None,)):
    mn, mx = modelo.caixa(poses)
    return np.array([(mn[0] + mx[0]) / 2, (max(mx[1], 0) + min(mn[1], 0)) / 2, (mn[2] + mx[2]) / 2])


def gerar_previas(geo, textura, saida, anim=None, brilho=None, escala=6.0, quadros=8, nome=None, prefixo=""):
    """Gera: frente/lado/costas/3-4 (arquivos separados + folha), silhueta e um PNG por animação."""
    modelo = Modelo(geo)
    tex = ler_png(textura)
    glow = ler_png(brilho) if brilho else None
    saida = Path(saida)
    saida.mkdir(parents=True, exist_ok=True)
    nome = (nome or Path(geo).stem.replace(".geo", "")).upper()
    tam = tamanho_para(modelo, escala)
    centro = centro_para(modelo)
    vistas = []
    for rot, az, el in VISTAS:
        cam = Camera(az, el)
        img = renderizar(modelo, tex, cam, glow=glow, escala=escala, tamanho=tam, centro=centro, rotulo=rot)
        vistas.append(img)
        salvar_png(saida / f"{prefixo}{rot.lower().replace(' ', '_').replace('/', '_')}.png", img)
    folha = juntar_vertical([faixa_titulo(f"{nome}: VISTAS (1 TRACO DA REGUA = 1 BLOCO)", sum(v.shape[1] for v in vistas) + 18),
                             juntar_horizontal(vistas)])
    salvar_png(saida / f"{prefixo}vistas.png", folha)
    # silhueta: tudo preto em fundo claro (teste da régua: reconhecer só pela sombra)
    sil = []
    for rot, az, el in VISTAS[:3]:
        cam = Camera(az, el)
        sil.append(renderizar(modelo, tex, cam, escala=escala * 0.6, tamanho=(int(tam[0] * 0.6), int(tam[1] * 0.6)),
                              centro=centro, fundo=(214, 214, 210, 255), silhueta=True, regua=False))
    salvar_png(saida / f"{prefixo}silhueta.png",
               juntar_vertical([faixa_titulo(f"{nome}: SILHUETA", sum(s.shape[1] for s in sil) + 12, fundo=(60, 60, 62, 255)),
                                juntar_horizontal(sil, fundo=(214, 214, 210, 255))], fundo=(214, 214, 210, 255)))
    # animações
    if anim:
        for nome_anim in Animacao.nomes(anim):
            a = Animacao(anim, nome_anim)
            curto = nome_anim.split(".")[-1]
            tempos = [a.duracao * i / (quadros - 1) for i in range(quadros)] if a.duracao > 0 else [0.0]
            if a.loop and len(tempos) > 1:
                tempos = [a.duracao * i / quadros for i in range(quadros)]
            poses = [a.pose(t) for t in tempos]
            tam_a = tamanho_para(modelo, escala * 0.55, poses, extra=1.15)
            cen_a = centro_para(modelo)
            cam = Camera(35, 18)
            linha = []
            for t, p in zip(tempos, poses):
                img = renderizar(modelo, tex, cam, pose=p, glow=glow, escala=escala * 0.55, tamanho=tam_a, centro=cen_a,
                                 regua=False, rotulo=f"{t:.2f}S")
                linha.append(img)
            # segunda linha de frente, para ler a animação de outro ângulo
            cam2 = Camera(0, 0)
            linha2 = [renderizar(modelo, tex, cam2, pose=p, glow=glow, escala=escala * 0.55, tamanho=tam_a, centro=cen_a,
                                 regua=False) for p in poses]
            largura = sum(i.shape[1] for i in linha) + 6 * (len(linha) - 1)
            tipo = "LOOP" if a.loop else "UMA VEZ"
            folha = juntar_vertical([faixa_titulo(f"{nome}: {curto.upper()} ({a.duracao:.1f} S, {tipo})", largura),
                                     juntar_horizontal(linha), juntar_horizontal(linha2)])
            salvar_png(saida / f"{prefixo}anim_{curto}.png", folha)
    return modelo


def main():
    ap = argparse.ArgumentParser(description="Prévias PNG de modelo GeckoLib (sem Blockbench).")
    ap.add_argument("--geo", required=True)
    ap.add_argument("--textura", required=True)
    ap.add_argument("--saida", required=True)
    ap.add_argument("--anim")
    ap.add_argument("--brilho")
    ap.add_argument("--escala", type=float, default=6.0)
    ap.add_argument("--quadros", type=int, default=8)
    ap.add_argument("--nome")
    ap.add_argument("--prefixo", default="")
    args = ap.parse_args()
    gerar_previas(args.geo, args.textura, args.saida, args.anim, args.brilho, args.escala, args.quadros, args.nome, args.prefixo)
    print("prévias em", args.saida)


if __name__ == "__main__":
    main()
