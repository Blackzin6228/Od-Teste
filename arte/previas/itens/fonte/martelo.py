#!/usr/bin/env python3
"""PRÉVIA do Martelo do Ferreiro (Vila Calada, proposta A "O Dobre"): duas variações, item do Minecraft (modelo
Java em elementos, 3D na mão) + ícone 32x32 só para o inventário. Nada vai para src/.

V1 "Martelo de Forja": martelo de bigorna de pena (uma face chata, uma cunha), cabo comprido de freixo escuro,
    pano enrolado manchado de sangue seco (o acento), e no cabo os entalhes de contagem em grupos de cinco
    (os mesmos riscos da parede do campanário: quem tocou o sino contava).
V2 "O que Rachou o Sino": a mesma ferramenta depois da última noite. Cabeça rachada com sangue seco na fenda,
    face de golpe amassada com lascas do bronze do sino, cabo amarrado com um pedaço da corrente do sino (o
    Sineiro se pendurou nelas) e uma tira de pano preto de luto. Entalhes de contagem também, mas o último grupo
    ficou incompleto (falta um: o próprio Sineiro).

Textura: 32x32 (2 px por unidade de modelo, 32 px por bloco). Ícone: 32x32 desenhado à mão em código.
Rodar da raiz: bash ferramentas/martelo.sh
"""
import json
import math
import sys
from pathlib import Path

import numpy as np

sys.dont_write_bytecode = True
RAIZ = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path(__file__).resolve().parents[4]
sys.path.insert(0, str(RAIZ / "ferramentas"))
sys.path.insert(0, str(RAIZ / "arte" / "previas" / "criaturas" / "fonte"))
import render_geckolib as rg  # noqa: E402
import noite  # noqa: E402

SAIDA = RAIZ / "previas-modelos" / "martelo"
BLOCKBENCH = RAIZ / "arte" / "blockbench"


def cor(h):
    return np.array([int(h[1:3], 16), int(h[3:5], 16), int(h[5:7], 16), 255], np.uint8)


def h01(x, y, s=0):
    h = (x * 374761393 + y * 668265263 + s * 982451653) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0x7FFFFFFF) / 0x80000000


FERRO = ["#a3a39c", "#7f7f79", "#61615c", "#474744", "#30302e", "#1d1d1c"]
FERRUGEM = ["#8a5330", "#6b3e22", "#4f2c18", "#36200f"]
MADEIRA = ["#6e5038", "#5a412d", "#463223", "#33241a", "#221810"]
PANO = ["#8c7b62", "#706250", "#56493c", "#3d342b"]          # linho sujo (V1)
LUTO = ["#38343a", "#2a272c", "#1d1b1f", "#121114"]          # pano preto de luto (V2)
SANGUE = ["#9a1c24", "#6e0f16", "#45070c", "#2a0408"]
BRONZE = ["#8e7444", "#6a5532"]


def T(p, i):
    return cor(p[max(0, min(len(p) - 1, i))])


# ------------------------------------------------------------------ pintores de região (px dentro da região)

def p_ferro_lado(var):
    def f(x, y, w, h):
        i = 2
        if y == 0:
            i = 0                        # quina de cima pega luz (desgaste brilhante)
        elif y == 1:
            i = 1
        elif y >= h - 1:
            i = 4
        elif y >= h - 2:
            i = 3
        if x == 0 or x == w - 1:
            i += 1
        if h01(x // 2, y // 2, 3 + var) < 0.18:
            i += 1                        # marca de martelada
        c = T(FERRO, i)
        # ferrugem: manchas largas perto das bordas e embaixo
        if (y >= h - 3 or x < 2) and h01(x // 2, y // 2, 7 + var) < 0.45:
            c = T(FERRUGEM, 1 + (h01(x, y, 8) < 0.4))
        if var == 2:
            # rachadura: do centro de cima até embaixo, em zigue-zague, com sangue seco dentro
            xc = int(w * 0.55 + math.sin(y * 1.3) * 1.2)
            if x == xc:
                c = T(SANGUE, 3)
            elif x == xc + 1 and y < h - 1:
                c = T(SANGUE, 2) if y % 2 else T(FERRO, 5)
        return c
    return f


def p_ferro_topo(var):
    def f(x, y, w, h):
        c = T(FERRO, 1 if 0 < y < h - 1 else 3)
        if h01(x // 2, y, 11 + var) < 0.2:
            c = T(FERRO, 2)
        if (x < 3 or x > w - 3) and h01(x, y, 12) < 0.4:
            c = T(FERRUGEM, 2)
        if var == 2 and x == int(w * 0.55):
            c = T(SANGUE, 3)
        return c
    return f


def p_face_golpe(var):
    def f(x, y, w, h):
        d = ((x - (w - 1) / 2) / (w / 2)) ** 2 + ((y - (h - 1) / 2) / (h / 2)) ** 2
        if d > 0.85:
            return T(FERRO, 4)           # borda amassada para fora
        c = T(FERRO, 0 if d < 0.25 else 1)
        if var == 2:
            if d < 0.35 and (x + y) % 3 == 0:
                c = T(FERRO, 3)          # amassado
            if h01(x, y, 21) < 0.25:
                c = T(BRONZE, h01(x, y, 22) < 0.5)  # lasca do bronze do sino
        return c
    return f


def p_cunha(var):
    def f(x, y, w, h):
        c = T(FERRO, 2 + (y >= h - 2))
        if x in (0, w - 1):
            c = T(FERRO, 4)
        if h01(x, y, 31 + var) < 0.35:
            c = T(FERRUGEM, 1)
        return c
    return f


def p_madeira(var, entalhes=False):
    def f(x, y, w, h):
        veio = (x + int(math.sin(y * 0.45 + x) * 1.3)) % 3 == 0
        i = 1 + veio + (x == w - 1)
        if y < 2:
            i = 3                        # encaixe na cabeça: sombra
        if h01(x, y // 3, 41 + var) < 0.08:
            i += 1
        c = T(MADEIRA, i)
        if x == 0 and not entalhes:
            c = T(MADEIRA, 0)            # brilho de mão na quina
        if entalhes and 3 <= y < h - 2:
            # entalhes de contagem: grupos de cinco riscos e um vão. V2: o último grupo só tem quatro (falta o Sineiro).
            g, r = divmod(y - 3, 12)
            risco = g < 3 and r in (0, 2, 4, 6, 8) and not (var == 2 and g == 2 and r == 8)
            if risco:
                c = T(MADEIRA, 4) if x > 0 else T(MADEIRA, 3)
                if g == 0 and h01(x, y, 44) < 0.4:
                    c = T(SANGUE, 3)  # sangue seco no primeiro grupo
            elif g < 3 and r in (1, 3, 5, 7, 9) and r - 1 in (0, 2, 4, 6, 8) and x == 1:
                c = T(MADEIRA, 0)     # borda clara do corte
        return c
    return f


def p_pano(var):
    pal = PANO if var == 1 else LUTO

    def f(x, y, w, h):
        faixa = (y + x // 2) % 4
        c = T(pal, 0 if faixa == 0 else (1 if faixa == 1 else 2))
        if faixa == 3:
            c = T(pal, 3)                # sulco entre voltas
        if var == 1 and h01(x // 2, y // 2, 51) < 0.24 and y > 2:
            c = T(SANGUE, 1 + (faixa >= 2) + (y > h // 2))   # mancha de sangue velho (o acento)
        if var == 2 and h01(x, y, 52) < 0.12:
            c = T(SANGUE, 3)
        return c
    return f


def p_tira(var):
    pal = PANO if var == 1 else LUTO

    def f(x, y, w, h):
        if y >= h - 1 and x % 2:
            return None                  # ponta desfiada
        c = T(pal, 1 + (x == w - 1))
        if var == 1 and y > h * 0.4 and h01(x, y, 61) < 0.5:
            c = T(SANGUE, 2)
        return c
    return f


def p_corrente(x, y, w, h):
    borda = x in (0, w - 1) or y in (0, h - 1)
    if not borda:
        return None                      # elo vazado
    return T(FERRO, 1 if y == 0 else (4 if y == h - 1 else 3))


def p_pomo(var):
    def f(x, y, w, h):
        c = T(FERRO, 2 if (x + y) % 3 else 3)
        if h01(x, y, 71 + var) < 0.4:
            c = T(FERRUGEM, 1)
        return c
    return f


# regiões da textura 32x32 (px): nome -> (x, y, w, h, pintor)
def regioes(var):
    r = {
        "cabo": (0, 0, 3, 26, p_madeira(var)),
        "cabo_entalhe": (3, 0, 3, 26, p_madeira(var, entalhes=True)),
        "pano": (6, 0, 4, 12, p_pano(var)),
        "tira": (6, 12, 2, 9, p_tira(var)),
        "cabeca_lado": (10, 0, 15, 7, p_ferro_lado(var)),
        "cabeca_topo": (10, 7, 15, 6, p_ferro_topo(var)),
        "face_golpe": (25, 0, 7, 8, p_face_golpe(var)),
        "cunha": (25, 8, 6, 6, p_cunha(var)),
        "pomo": (0, 26, 4, 4, p_pomo(var)),
        "corrente": (10, 14, 4, 4, p_corrente),
        "corrente_b": (14, 14, 4, 4, p_corrente),
    }
    return r


def pintar(var):
    tex = np.zeros((32, 32, 4), np.uint8)
    for nome, (x0, y0, w, h, f) in regioes(var).items():
        for y in range(h):
            for x in range(w):
                c = f(x, y, w, h)
                if c is not None:
                    tex[y0 + y, x0 + x] = c
    return tex


# ------------------------------------------------------------------ elementos (modelo Java, espaço 0..16)

def uvr(var, nome, frac=None):
    x, y, w, h, _ = regioes(var)[nome]
    u0, v0, u1, v1 = x / 2, y / 2, (x + w) / 2, (y + h) / 2
    if frac:  # recorte (fração da região)
        a, b, c, d = frac
        u0, u1 = u0 + (u1 - u0) * a, u0 + (u1 - u0) * c
        v0, v1 = v0 + (v1 - v0) * b, v0 + (v1 - v0) * d
    return [round(u0, 3), round(v0, 3), round(u1, 3), round(v1, 3)]


def el(nome, de, ate, faces, rot=None):
    e = {"name": nome, "from": list(de), "to": list(ate), "faces": faces}
    if rot:
        e["rotation"] = rot
    return e


def todas(uv, **troca):
    f = {k: {"uv": uv, "texture": "#0"} for k in ("north", "south", "east", "west", "up", "down")}
    for k, v in troca.items():
        if v is None:
            f.pop(k, None)
        else:
            f[k] = {"uv": v, "texture": "#0"}
    return f


def elementos(var):
    u = lambda n, fr=None: uvr(var, n, fr)  # noqa: E731
    E = []
    alto = 14.0 if var == 1 else 13.0     # V2: cabo mais curto (quebrou e foi reencaixado)
    E.append(el("cabo", (7.25, 0.5, 7.25), (8.75, alto, 8.75),
                todas(u("cabo"), north=u("cabo_entalhe"), up=None, down=u("pomo"))))
    E.append(el("pomo", (7.0, 0, 7.0), (9.0, 1.0, 9.0), todas(u("pomo"))))
    E.append(el("pano", (6.9, 2.5, 6.9), (9.1, 8.5, 9.1), todas(u("pano"), up=u("pomo"), down=u("pomo"))))
    E.append(el("tira", (8.4, 0.0, 6.8), (9.4, 4.5, 6.8), {"north": {"uv": u("tira"), "texture": "#0"},
                                                          "south": {"uv": u("tira"), "texture": "#0"}},
                {"angle": -22.5, "axis": "z", "origin": [8.9, 4.5, 6.8]}))
    yc = alto - 1.5
    if var == 1:
        # martelo de pena: face chata de um lado, cunha do outro
        E.append(el("cabeca", (4.25, yc, 6.5), (11.5, yc + 3.5, 9.5),
                    todas(u("cabeca_lado"), up=u("cabeca_topo"), down=u("cabeca_topo"),
                          east=u("face_golpe"), west=u("cunha"))))
        E.append(el("face_golpe", (11.5, yc - 0.25, 6.25), (12.5, yc + 3.75, 9.75),
                    todas(u("cabeca_lado", (0, 0, 0.15, 1)), east=u("face_golpe"), up=u("cabeca_topo", (0, 0, 0.15, 1)),
                          down=u("cabeca_topo", (0, 0, 0.15, 1)))))
        E.append(el("cunha", (2.5, yc + 0.75, 7.0), (4.25, yc + 2.75, 9.0),
                    todas(u("cunha"))))
        E.append(el("cunha_fio", (1.75, yc + 1.25, 7.25), (2.5, yc + 2.25, 8.75), todas(u("cunha"))))
    else:
        # marreta curta de duas faces, rachada; face de golpe larga amassada
        E.append(el("cabeca", (4.5, yc - 0.5, 6.25), (11.0, yc + 3.5, 9.75),
                    todas(u("cabeca_lado"), up=u("cabeca_topo"), down=u("cabeca_topo"),
                          east=u("face_golpe"), west=u("face_golpe"))))
        E.append(el("face_golpe", (11.0, yc - 0.75, 6.0), (12.0, yc + 3.75, 10.0),
                    todas(u("cabeca_lado", (0, 0, 0.15, 1)), east=u("face_golpe"), up=u("cabeca_topo", (0, 0, 0.15, 1)),
                          down=u("cabeca_topo", (0, 0, 0.15, 1)))))
        E.append(el("lasca", (3.5, yc + 0.25, 6.75), (4.5, yc + 3.0, 9.25), todas(u("face_golpe"))))
        # corrente do sino: três elos alternados pendurados do pomo
        E.append(el("elo_1", (7.5, -1.75, 8.0), (8.5, 0.25, 8.0), {"north": {"uv": u("corrente"), "texture": "#0"},
                                                                  "south": {"uv": u("corrente"), "texture": "#0"}}))
        E.append(el("elo_2", (8.0, -3.5, 7.5), (8.0, -1.5, 8.5), {"east": {"uv": u("corrente_b"), "texture": "#0"},
                                                                 "west": {"uv": u("corrente_b"), "texture": "#0"}}))
        E.append(el("elo_3", (7.5, -5.25, 8.0), (8.5, -3.25, 8.0), {"north": {"uv": u("corrente"), "texture": "#0"},
                                                                   "south": {"uv": u("corrente"), "texture": "#0"}},
                    {"angle": 22.5, "axis": "z", "origin": [8, -3.25, 8]}))
    E.append(el("cunha_ferro", (7.6, yc + 3.4, 7.4), (8.4, yc + 3.8, 8.6), todas(u("pomo"))))
    return E


DISPLAY = {  # conferir em jogo (régua: 1ª e 3ª pessoa, moldura, chão)
    "thirdperson_righthand": {"rotation": [0, 90, -10], "translation": [0, 3.5, 1.5], "scale": [0.9, 0.9, 0.9]},
    "thirdperson_lefthand": {"rotation": [0, -90, 10], "translation": [0, 3.5, 1.5], "scale": [0.9, 0.9, 0.9]},
    "firstperson_righthand": {"rotation": [0, 90, 20], "translation": [1.1, 2.8, 0.8], "scale": [0.7, 0.7, 0.7]},
    "firstperson_lefthand": {"rotation": [0, -90, -20], "translation": [1.1, 2.8, 0.8], "scale": [0.7, 0.7, 0.7]},
    "ground": {"translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [0.85, 0.85, 0.85]},
    "head": {"rotation": [0, 0, -30], "translation": [0, 8, 0]},
}


def modelos_json(var):
    nome = f"martelo_ferreiro_v{var}"
    modelo3d = {"credit": "odteste (arte própria)", "texture_size": [32, 32],
                "textures": {"0": f"odteste:item/{nome}", "particle": f"odteste:item/{nome}"},
                "elements": elementos(var), "display": DISPLAY}
    # na mão o 3D; no inventário o ícone desenhado (NeoForge: loader separate_transforms)
    item = {"loader": "neoforge:separate_transforms", "gui_light": "front",
            "base": {"parent": f"odteste:item/{nome}_3d"},
            "perspectives": {"gui": {"parent": "minecraft:item/generated",
                                     "textures": {"layer0": f"odteste:item/{nome}_icone"}}}}
    return modelo3d, item


# ------------------------------------------------------------------ ícone 32x32 (inventário)

def icone(var):
    img = np.zeros((32, 32, 4), np.uint8)
    contorno = cor("#120d0b")

    def px(x, y, c):
        if 0 <= x < 32 and 0 <= y < 32:
            img[y, x] = c

    # cabo: diagonal de baixo-esquerda (4,28) até (21,11), 3 px de largura
    L = 24 if var == 1 else 21
    for t in range(L):
        x, y = 4 + t * 0.72, 28 - t * 0.72
        for k in (-1, 0, 1):
            xi, yi = int(round(x + k * 0.7)), int(round(y + k * 0.7))
            cabo_c = T(MADEIRA, 0 if k == -1 else (1 if k == 0 else 3))
            if 6 <= t <= 12:
                pal = PANO if var == 1 else LUTO
                cabo_c = T(pal, (t + k) % 3)
                if var == 1 and h01(t, k, 3) < 0.45:
                    cabo_c = T(SANGUE, 1 + (k > 0))
            if 14 <= t <= 19 and k == -1 and t % 2 == 0:
                cabo_c = T(MADEIRA, 4)   # entalhes
            px(xi, yi, cabo_c)
    # cabeça perpendicular ao cabo, perto da ponta
    cx, cy = 4 + (L - 2) * 0.72, 28 - (L - 2) * 0.72
    ux, uy = 0.707, 0.707                 # ao longo da cabeça (perpendicular ao cabo)
    vx, vy = 0.707, -0.707                # ao longo do cabo
    comp = 7.5 if var == 1 else 6.5
    for a in np.arange(-comp, comp + 0.01, 0.35):
        larg = 2.6 if var == 1 else 3.2
        if var == 1 and a < -comp + 3:
            larg = 2.6 * (1 - (-comp + 3 - a) / 4.5)   # cunha afinando
        for b in np.arange(-larg, larg + 0.01, 0.35):
            x, y = cx + ux * a + vx * b, cy + uy * a + vy * b
            i = 1 if b > larg * 0.3 else (2 if b > -larg * 0.4 else 3)
            c = T(FERRO, i)
            if a > comp - 1.3:
                c = T(FERRO, 0)          # face de golpe gasta, brilha
            if h01(int(x), int(y), 9 + var) < 0.15 and abs(a) < comp - 1.5:
                c = T(FERRUGEM, 1)
            if var == 2 and abs(a - 1.0) < 0.4:
                c = T(SANGUE, 3)         # rachadura
            px(int(round(x)), int(round(y)), c)
    if var == 2:  # corrente pendurada do pomo
        for i, (x, y) in enumerate(((3, 29), (2, 30), (3, 31))):
            px(x, y, T(FERRO, 1 + i % 2))
    # contorno escuro (régua: lê em 16x16)
    a = img[:, :, 3] > 0
    viz = np.zeros_like(a)
    viz[1:, :] |= a[:-1, :]
    viz[:-1, :] |= a[1:, :]
    viz[:, 1:] |= a[:, :-1]
    viz[:, :-1] |= a[:, 1:]
    img[viz & ~a] = contorno
    return img


# ------------------------------------------------------------------ render (converte para o formato do renderizador)

def para_geo(elems):
    cubos = []
    for e in elems:
        de, ate = np.array(e["from"], float), np.array(e["to"], float)
        o = de - np.array([8, 0, 8])
        uv = {}
        for f, d in e["faces"].items():
            u0, v0, u1, v1 = d["uv"]
            uv[f] = {"uv": [u0, v0], "uv_size": [u1 - u0, v1 - v0]}
        c = {"origin": list(o), "size": list(ate - de), "uv": uv}
        if "rotation" in e:
            r = e["rotation"]
            eixo = {"x": 0, "y": 1, "z": 2}[r["axis"]]
            rot = [0, 0, 0]
            rot[eixo] = r["angle"]
            c["rotation"] = rot
            c["pivot"] = list(np.array(r["origin"], float) - np.array([8, 0, 8]))
        cubos.append(c)
    return {"minecraft:geometry": [{"description": {"identifier": "geometry.martelo", "texture_width": 16, "texture_height": 16},
                                    "bones": [{"name": "martelo", "pivot": [0, 0, 0], "cubes": cubos}]}]}


def bbmodel_java(var, elems, png):
    import base64
    import uuid
    els = []
    for e in elems:
        d = {"name": e["name"], "box_uv": False, "rescale": False, "from": e["from"], "to": e["to"], "autouv": 0,
             "color": 0, "origin": e.get("rotation", {}).get("origin", [8, 8, 8]),
             "faces": {f: {"uv": v["uv"], "texture": 0} for f, v in e["faces"].items()}, "type": "cube",
             "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, f"odteste/martelo/{var}/{e['name']}"))}
        if "rotation" in e:
            r = e["rotation"]
            rot = [0, 0, 0]
            rot[{"x": 0, "y": 1, "z": 2}[r["axis"]]] = r["angle"]
            d["rotation"] = rot
        els.append(d)
    nome = f"martelo_ferreiro_v{var}"
    return {"meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False}, "name": nome,
            "parent": "", "ambientocclusion": True, "front_gui_light": False, "visible_box": [1, 1, 0],
            "resolution": {"width": 32, "height": 32}, "elements": els, "outliner": [x["uuid"] for x in els],
            "textures": [{"path": "", "name": f"{nome}.png", "folder": "item", "namespace": "odteste", "id": "0",
                          "particle": True, "mode": "bitmap", "saved": True, "width": 32, "height": 32,
                          "uv_width": 16, "uv_height": 16, "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, f"odteste/martelo/{var}/tex")),
                          "source": "data:image/png;base64," + base64.b64encode(png).decode("ascii")}],
            "display": DISPLAY}


def ampliar(img, k, fundo=(24, 24, 28)):
    a = np.repeat(np.repeat(img, k, axis=0), k, axis=1).copy()
    vazio = a[:, :, 3] == 0
    a[vazio, :3] = fundo
    a[:, :, 3] = 255
    return a


def slot_inventario(ic, escala):
    """Ícone num slot de inventário vanilla (cinza com bevel), no tamanho de GUI 2x e 4x, como o jogador vê."""
    s = 18 * escala
    img = np.zeros((s, s, 4), np.uint8)
    img[:, :] = (139, 139, 139, 255)
    img[:escala, :] = (55, 55, 55, 255)
    img[:, :escala] = (55, 55, 55, 255)
    img[-escala:, :] = (255, 255, 255, 255)
    img[:, -escala:] = (255, 255, 255, 255)
    # o jogo desenha o ícone em 16x16 de GUI: ícone 32x32 cai em meio "pixel de GUI"
    g = np.repeat(np.repeat(ic, max(1, escala // 2), axis=0), max(1, escala // 2), axis=1)
    o = escala
    m = g[:, :, 3] > 0
    sub = img[o:o + g.shape[0], o:o + g.shape[1]]
    sub[m] = g[m]
    return img


def gerar(var):
    pasta = SAIDA / f"v{var}"
    pasta.mkdir(parents=True, exist_ok=True)
    tex = pintar(var)
    ic = icone(var)
    rg.salvar_png(pasta / f"martelo_ferreiro_v{var}.png", tex)
    rg.salvar_png(pasta / f"martelo_ferreiro_v{var}_icone.png", ic)
    m3d, item = modelos_json(var)
    (pasta / f"martelo_ferreiro_v{var}_3d.json").write_text(json.dumps(m3d, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    (pasta / f"martelo_ferreiro_v{var}.json").write_text(json.dumps(item, indent=1, ensure_ascii=False) + "\n", encoding="utf-8")
    BLOCKBENCH.mkdir(parents=True, exist_ok=True)
    (BLOCKBENCH / f"martelo_ferreiro_v{var}.bbmodel").write_text(
        json.dumps(bbmodel_java(var, m3d["elements"], (pasta / f"martelo_ferreiro_v{var}.png").read_bytes())), encoding="utf-8")
    modelo = rg.Modelo(para_geo(m3d["elements"]))
    esc = 22.0
    tam = (420, 520)
    cen = rg.centro_para(modelo)
    vistas = [rg.renderizar(modelo, tex, rg.Camera(az, el), escala=esc, tamanho=tam, centro=cen, regua=False, chao=False, rotulo=r)
              for r, az, el in (("FRENTE", 0, 0), ("LADO", 90, 0), ("COSTAS", 180, 0), ("3/4", 35, 20), ("DE CIMA 3/4", 210, 35))]
    vistas.append(noite.render_noite(modelo, tex, 35, 12, esc, tam, cen, rotulo="NOITE"))
    titulo = {1: "MARTELO DO FERREIRO V1: MARTELO DE FORJA", 2: "MARTELO DO FERREIRO V2: O QUE RACHOU O SINO"}[var]
    linha1 = rg.juntar_horizontal(vistas)
    extra = [ampliar(ic, 12), slot_inventario(ic, 2), slot_inventario(ic, 4), ampliar(tex, 10)]
    for e, r in zip(extra, ("ICONE 32X32 (12X)", "SLOT GUI 2", "SLOT GUI 4", "TEXTURA 3D 32X32 (10X)")):
        rg.texto(e, (4, 4), r, (235, 235, 235, 255), 1)
    linha2 = rg.juntar_horizontal(extra)
    folha = rg.juntar_vertical([rg.faixa_titulo(titulo, linha1.shape[1]), linha1, linha2])
    rg.salvar_png(pasta / "vistas.png", folha)
    print(f"martelo v{var}: {len(m3d['elements'])} elementos")
    return modelo, tex, ic


def main():
    res = {v: gerar(v) for v in (1, 2)}
    # folha lado a lado das duas variações (3/4 e ícone)
    imgs = []
    for v, (modelo, tex, ic) in res.items():
        cen = rg.centro_para(modelo)
        imgs.append(rg.renderizar(modelo, tex, rg.Camera(35, 20), escala=22, tamanho=(420, 520), centro=cen, regua=False,
                                  chao=False, rotulo=f"V{v}"))
        imgs.append(noite.render_noite(modelo, tex, 35, 12, 22, (420, 520), cen, rotulo=f"V{v} NOITE"))
        icg = np.zeros((520, 400, 4), np.uint8)
        icg[:, :] = (24, 24, 28, 255)
        a = ampliar(ic, 12)
        m = a[:, :, 3] > 0
        icg[60:60 + a.shape[0], 8:8 + a.shape[1]][m] = a[m]
        rg.texto(icg, (8, 8), f"V{v} ICONE", (235, 235, 235, 255), 2)
        imgs.append(icg)
    rg.salvar_png(SAIDA / "martelo_v1_v2.png", rg.juntar_vertical([rg.faixa_titulo("MARTELO DO FERREIRO: V1 X V2", 2600),
                                                                   rg.juntar_horizontal(imgs[:3]), rg.juntar_horizontal(imgs[3:])]))
    print("pronto:", SAIDA)


if __name__ == "__main__":
    main()
