#!/usr/bin/env python3
"""PRÉVIA das criaturas de Sangue (Retalhado e Sanguessuga-Mãe) em qualidade final: monta geo, textura, animações
e .bbmodel em código e renderiza as prévias. Nada vai para src/ (o jogo continua com o modelo provisório).

Rodar da raiz: bash ferramentas/criaturas_sangue.sh [retalhado|sanguessuga_mae|todas]
Saída: arte/previas/criaturas/<criatura>/ (geo, animation, png, renders) e arte/blockbench/<criatura>.bbmodel.

Base: lore/criaturas.md, docs/decisoes/2026-11-08-cores-dos-elementos.md (vermelho do Sangue = único acento),
docs/regua-de-qualidade.md. Arte nossa, feita do zero.
"""
import json
import math
import sys
from pathlib import Path

import numpy as np

sys.dont_write_bytecode = True
AQUI = Path(__file__).resolve().parent
RAIZ = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else AQUI.parents[3]
sys.path.insert(0, str(RAIZ / "ferramentas"))
sys.path.insert(0, str(AQUI))

import render_geckolib as rg  # noqa: E402
from montagem import (Criatura, animacao, arquivo_animacoes, bbmodel, canal, cor, costura_h, costura_v,  # noqa: E402
                      hash01, json_compacto, sombreado, tom)

PREVIAS = RAIZ / "arte" / "previas" / "criaturas"
BLOCKBENCH = RAIZ / "arte" / "blockbench"
GEO_JOGO = RAIZ / "src" / "main" / "resources" / "assets" / "odteste" / "geo" / "entity"

# Cores do Sangue (decisão 2026-11-08): o ÚNICO acento. Todo o resto é neutro escuro.
SANGUE = ["#E0313A", "#B3131B", "#7A0A10", "#3A0306"]
# Materiais neutros (sem vermelho): carne morta, metal, couro, osso, quitina, cesta.
CARNE = ["#74605c", "#604c49", "#4e3c3a", "#3c2d2c", "#2a1f1f"]
METAL = ["#8a8a84", "#6a6a64", "#4c4c47", "#333330", "#1f1f1d"]
FERRUGEM = "#5a3a26"
GUME = "#b4b4ac"
COURO = ["#55402f", "#43322a", "#33261e", "#231a14"]
OSSO = ["#d6cbae", "#b5a98c", "#8f8468", "#665d47"]
QUITINA = ["#4a3f44", "#3a3034", "#2c2428", "#1e181b", "#120e10"]
CESTA = ["#6a5054", "#5a4246", "#4a363a", "#3a2a2d", "#281d1f", "#160f11"]
FIO = "#15100f"
CAVIDADE = "#160a0c"


def mat(paleta, semente, **k):
    return lambda face, fx, fy, fw, fh: sombreado(paleta, face, fx, fy, fw, fh, semente, **k)


def camadas(*pintores):
    """Aplica os pintores em ordem; o primeiro que devolver cor vence (decalque antes da base)."""
    def p(face, fx, fy, fw, fh):
        for f in pintores:
            c = f(face, fx, fy, fw, fh)
            if c is not None:
                return c
        return None
    return p


def so_faces(faces, pintor):
    return lambda face, fx, fy, fw, fh: pintor(face, fx, fy, fw, fh) if face in faces else None


def retalhos(paleta, semente, px=7, py=6, faces=("north", "south", "east", "west", "up")):
    """Pele em retalhos: grade de costuras com cada retalho num tom um pouco diferente (é o Retalhado)."""
    def p(face, fx, fy, fw, fh):
        if face not in faces:
            return sombreado(paleta, face, fx, fy, fw, fh, semente)
        c = costura_h(fx, fy, py, fw, passo=4) if fh > py + 1 else None
        if c is None and fw > px + 1:
            c = costura_v(fx, fy, px, fh, passo=4)
        if c is not None:
            return c
        desloc = ((fx > px) + (fy > py) + semente) % 2
        return sombreado(paleta, face, fx, fy, fw, fh, semente + 3, base=1 + desloc, pintas=(0.08, 0.95))
    return p


# ====================================================================== RETALHADO


def montar_retalhado():
    c = Criatura("retalhado", 128, 128)
    c.osso("raiz")

    # ---- pernas (joelho dobrado: coxa +10, canela -10; o pé fica no chão)
    for lado, s, esp in (("dir", -1, False), ("esq", 1, True)):
        perna = c.osso(f"perna_{lado}", "raiz", (5 * s, 21, 0), (10, 0, 0))
        xo = -8.5 if s < 0 else 1.5
        perna.cubo(f"coxa_{lado}", (xo, 10, -4), (7, 12, 8), retalhos(CARNE, 1, px=3, py=5), espelho=esp,
                   partilha=None if not esp else "coxa_dir")
        canela = c.osso(f"canela_{lado}", f"perna_{lado}", (5 * s, 11, 0), (-10, 0, 0))
        xo2 = -8 if s < 0 else 2
        canela.cubo(f"canela_{lado}", (xo2, 2, -3.5), (6, 9, 7), camadas(
            so_faces(("north",), lambda f, x, y, w, h: costura_v(x, y, 2, h)), mat(CARNE, 2)),
            espelho=esp, partilha=None if not esp else "canela_dir")
        canela.cubo(f"pe_{lado}", (xo, 0, -7.5), (7, 3, 10), pe_retalhado, espelho=esp,
                    partilha=None if not esp else "pe_dir")

    # ---- tronco inclinado para a frente
    tronco = c.osso("tronco", "raiz", (0, 21, 0), (-8, 0, 0))
    tronco.cubo("pelve", (-8, 19, -4.5), (16, 8, 9), pelve_retalhado)
    tronco.cubo("peito", (-11, 26, -6), (22, 14, 12), peito_retalhado)
    tronco.cubo("corcova", (-9, 38, -4), (18, 8, 12), corcova_retalhado)
    tronco.cubo("ombro_dir", (-16, 33, -5.5), (7, 9, 11), ombro_retalhado)
    tronco.cubo("ombro_esq", (9, 33, -5.5), (7, 9, 11), ombro_retalhado, espelho=True, partilha="ombro_dir")

    ferida = c.osso("ferida", "tronco", (0, 33, -6))
    ferida.cubo("cavidade", (-5, 29, -6.6), (10, 9, 1), cavidade_retalhado)
    coracao = c.osso("coracao", "ferida", (0, 33.5, -7))
    coracao.cubo("coracao", (-2.5, 30.5, -9.2), (5, 6, 3), coracao_pintor, brilho=coracao_brilho)

    cabeca = c.osso("cabeca", "tronco", (0, 39, -5), (-12, 0, 0))
    cabeca.cubo("cabeca", (-4.5, 36, -13.5), (9, 8, 9), cabeca_retalhado)
    cabeca.cubo("mandibula", (-3.5, 33.5, -13), (7, 3, 7), mandibula_retalhado)

    # ---- braço direito: cutelo preso ao antebraço
    bd = c.osso("braco_dir", "tronco", (-13.5, 40, -1), (12, 0, -5))
    bd.cubo("braco_dir", (-17, 27, -4.5), (7, 13, 7), retalhos(CARNE, 4, px=3, py=6))
    ad = c.osso("antebraco_dir", "braco_dir", (-13.5, 28, -1), (-6, 0, 0))
    ad.cubo("antebraco_dir", (-16.5, 13, -4.5), (6, 15, 7), camadas(
        so_faces(("north", "east", "west"), lambda f, x, y, w, h: costura_h(x, y, 8, w)), mat(CARNE, 5)))
    ad.cubo("mao_dir", (-17, 8, -5), (7, 5, 8), mao_retalhado)
    ad.cubo("lamina", (-19.4, 3, -17), (1.5, 17, 14), lamina_pintor)
    ad.cubo("tira_a", (-19.4, 23, -4.7), (3, 2, 7.4), mat(COURO, 6, grad=False))
    ad.cubo("tira_b", (-19.4, 16, -4.7), (3, 2, 7.4), mat(COURO, 7, grad=False), partilha="tira_a")

    # ---- braço esquerdo: ganchos em correntes
    be = c.osso("braco_esq", "tronco", (13.5, 40, -1), (12, 0, 5))
    be.cubo("braco_esq", (10, 27, -4.5), (7, 13, 7), None, espelho=True, partilha="braco_dir")
    ae = c.osso("antebraco_esq", "braco_esq", (13.5, 28, -1), (-6, 0, 0))
    ae.cubo("antebraco_esq", (10.5, 13, -4.5), (6, 15, 7), None, espelho=True, partilha="antebraco_dir")
    ae.cubo("mao_esq", (10, 8, -5), (7, 5, 8), None, espelho=True, partilha="mao_dir")
    g = c.osso("ganchos", "antebraco_esq", (13.5, 8, -1))
    for i, x in enumerate((10.8, 13.0, 15.2)):
        comp = (5, 6, 4)[i]
        g.cubo(f"elo_{i}", (x, 8 - 3, -1.5), (1, 3, 1), mat(METAL, 20 + i, base=2, grad=False),
               partilha=None if i == 0 else "elo_0")
        g.cubo(f"haste_{i}", (x, 5 - comp, -1.5), (1, comp, 1), gancho_pintor,
               partilha=None if i == 0 else None)
        g.cubo(f"ponta_{i}", (x, 5 - comp, -5.5), (1, 1, 4), ponta_gancho_pintor,
               partilha=None if i == 0 else "ponta_0")
    return c


# ---- pintores do Retalhado

def pe_retalhado(face, fx, fy, fw, fh):
    if face == "north":  # dedos: três unhas de osso embaixo
        if fy == fh - 1 and fx in (1, 3, 5):
            return tom(OSSO, 1)
        if fx in (2, 4):
            return tom(CARNE, 4)
    if face == "up" and fy > fh - 4 and fx in (2, 4):
        return tom(CARNE, 4)
    return sombreado(CARNE, face, fx, fy, fw, fh, 3, base=1)


def pelve_retalhado(face, fx, fy, fw, fh):
    if face in ("north", "south", "east", "west") and fy <= 1:  # cinta de couro
        return tom(COURO, 1 if fy == 0 else 2) if not (fy == 1 and fx % 5 == 2) else tom(METAL, 2)
    if face == "south":
        c = costura_v(fx, fy, fw // 2, fh)
        if c:
            return c
    return sombreado(CARNE, face, fx, fy, fw, fh, 8, base=1)


def peito_retalhado(face, fx, fy, fw, fh):
    if face == "north":
        # a cavidade cobre o centro (fx 6..15, fy 3..11); costuras saem dela para os lados
        if 6 <= fx <= 15 and 3 <= fy <= 11:
            return cor(CAVIDADE)
        c = costura_h(fx, fy, 7, fw) if (fx < 6 or fx > 15) else None
        if c is None:
            c = costura_v(fx, fy, 3, fh) or costura_v(fx, fy, 18, fh)
        if c:
            return c
        if (fx + fy) % 9 == 0:  # veia funda
            return tom(CARNE, 3)
        return sombreado(CARNE, face, fx, fy, fw, fh, 9, base=0 if fy < 3 else 1)
    if face == "south":
        return retalhos(CARNE, 10, px=7, py=6)(face, fx, fy, fw, fh)
    return retalhos(CARNE, 11, px=5, py=7)(face, fx, fy, fw, fh)


def corcova_retalhado(face, fx, fy, fw, fh):
    if face == "up":
        if fx in (fw // 2 - 1, fw // 2) and fy % 3 != 2:  # vértebras saltadas
            return tom(OSSO, 1 if fy % 3 == 0 else 2)
        c = costura_v(fx, fy, 3, fh) or costura_v(fx, fy, fw - 4, fh)
        if c:
            return c
    if face == "south":
        c = costura_h(fx, fy, 3, fw)
        if c:
            return c
    return sombreado(CARNE, face, fx, fy, fw, fh, 12, base=1)


def ombro_retalhado(face, fx, fy, fw, fh):
    if face == "up" and (fx + fy) % 4 == 0 and 1 <= fx < fw - 1:
        return tom(METAL, 2)  # pregos de ferro segurando a pele
    c = costura_h(fx, fy, 4, fw) if face in ("north", "east", "west", "south") else None
    if c:
        return c
    return sombreado(CARNE, face, fx, fy, fw, fh, 13, base=0)


def cavidade_retalhado(face, fx, fy, fw, fh):
    if face != "north":
        return tom(SANGUE, 3)
    if fx in (0, fw - 1) or fy in (0, fh - 1):
        return tom(SANGUE, 2) if (fx + fy) % 2 == 0 else tom(SANGUE, 3)  # carne rasgada
    if fy in (1, 4, 7) and (fx <= 2 or fx >= fw - 3):
        return tom(OSSO, 1 if fx in (1, fw - 2) else 2)  # costelas quebradas
    return cor(CAVIDADE) if hash01(fx, fy, 14) > 0.2 else tom(SANGUE, 3)


def coracao_pintor(face, fx, fy, fw, fh):
    if face == "north":
        if (fx, fy) in ((2, 1), (1, 2), (2, 2)):
            return tom(SANGUE, 0)
        if fy == 0 and 1 <= fx <= 3:
            return tom(SANGUE, 3)  # vasos
        if fx == fw - 1 or fy == fh - 1:
            return tom(SANGUE, 2)
        return tom(SANGUE, 1)
    if face == "up":
        return tom(SANGUE, 3) if fx % 2 == 0 else tom(SANGUE, 2)
    return tom(SANGUE, 2) if fy < fh - 1 else tom(SANGUE, 3)


def coracao_brilho(face, fx, fy, fw, fh):
    # só o miolo brilha (brilho raro assusta mais)
    if face == "north" and 1 <= fx <= 3 and 1 <= fy <= 3:
        return tom(SANGUE, 0 if (fx, fy) in ((2, 1), (1, 2), (2, 2)) else 1)
    return None


def cabeca_retalhado(face, fx, fy, fw, fh):
    if face == "north":
        # olhos costurados: duas fendas de sangue com fio cruzado (o Sangue come os olhos primeiro)
        if fy == 2 and (1 <= fx <= 3 or 5 <= fx <= 7):
            return tom(SANGUE, 3) if fx % 2 == 0 else cor(FIO)
        if fy in (1, 3) and fx in (2, 6):
            return cor(FIO)
        if fy == 0:
            return tom(CARNE, 3)
        if fy >= 6 and 2 <= fx <= 6:  # beiço superior, escuro
            return tom(CARNE, 3)
    if face in ("east", "west"):
        c = costura_h(fx, fy, 2, fw)
        if c:
            return c
    if face == "up":
        c = costura_v(fx, fy, fw // 2, fh)
        if c:
            return c
    return sombreado(CARNE, face, fx, fy, fw, fh, 15, base=1)


def mandibula_retalhado(face, fx, fy, fw, fh):
    if face == "north":
        if fy == 0:
            return tom(OSSO, 1) if fx % 2 == 1 else tom(SANGUE, 3)  # dentes
        if fy == 1:
            return tom(SANGUE, 3)
    if face == "up":
        return tom(SANGUE, 3) if hash01(fx, fy, 16) > 0.3 else tom(SANGUE, 2)
    return sombreado(CARNE, face, fx, fy, fw, fh, 16, base=2)


def mao_retalhado(face, fx, fy, fw, fh):
    if face == "north" and fy <= 1 and fx % 2 == 0:
        return tom(CARNE, 3)  # nós dos dedos
    if face == "down" and fy == fh - 1 and fx % 2 == 1:
        return tom(OSSO, 2)
    return sombreado(CARNE, face, fx, fy, fw, fh, 17, base=1)


def lamina_pintor(face, fx, fy, fw, fh):
    if face in ("north", "down"):      # gume (frente e embaixo)
        return cor(GUME) if hash01(fx, fy, 18) > 0.25 else tom(SANGUE, 2)
    if face in ("south", "up"):        # dorso grosso
        return tom(METAL, 3)
    # faces largas (east/west): dorso escuro em cima, chapa com riscos de afiação, gume claro e sangue seco
    if fy <= 1:
        return tom(METAL, 4 if fy == 0 else 3)
    if (fx, fy) in ((10, 4), (11, 4), (10, 5), (11, 5)):
        return tom(METAL, 4)  # furo do cutelo
    r = hash01(fx, fy, 19)
    perto_gume = fy >= fh - 2 or fx <= 1
    if perto_gume and r < 0.4:
        return tom(SANGUE, 3) if r > 0.06 else tom(SANGUE, 2)
    if fy == fh - 1 or fx == 0:
        return tom(METAL, 0)
    if r < 0.04:
        return cor(FERRUGEM)
    if (fx + fy) % 6 == 0:
        return tom(METAL, 1)  # risco de afiação
    return tom(METAL, 2 if fy < fh * 0.55 else 1)


def gancho_pintor(face, fx, fy, fw, fh):
    r = hash01(fx, fy, 21)
    if r < 0.3:
        return cor(FERRUGEM)
    return tom(METAL, 2 if r < 0.8 else 3)


def ponta_gancho_pintor(face, fx, fy, fw, fh):
    if face == "north":
        return cor(GUME)
    if face in ("up", "down") and fy == 0:
        return cor(GUME)
    return gancho_pintor(face, fx, fy, fw, fh)


# ---- animações do Retalhado (nomes parado/andar/investida já usados pelo código do jogo)

def anims_retalhado():
    E = "easeInOutSine"
    batida = lambda t: [(t, (1, 1, 1)), (t + 0.1, (1.3, 1.3, 1.3), "easeOutQuad"), (t + 0.28, (1, 1, 1), E)]
    parado = animacao(4.0, True, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (2.0, (2.5, 0, 0), E), (4.0, (0, 0, 0), E)),
                   "position": canal((0, (0, 0, 0)), (2.0, (0, -0.5, 0), E), (4.0, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (1.2, (3, -14, 0), E), (2.6, (-2, 12, 0), E), (4.0, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (2.0, (-3, 0, -2), E), (4.0, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (2.0, (-3, 0, 2), E), (4.0, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (3, 0, 2)), (1.3, (-3, 0, -2), E), (2.6, (3, 0, 1), E), (4.0, (3, 0, 2), E))},
        "coracao": {"scale": canal(*(batida(0.0) + batida(1.35) + batida(2.7) + [(4.0, (1, 1, 1))]))},
    })
    andar = animacao(1.1, True, {
        "perna_dir": {"rotation": canal((0, (28, 0, 0)), (0.55, (-22, 0, 0), E), (1.1, (28, 0, 0), E))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.55, (0, 0, 0)), (0.8, (-40, 0, 0), E), (1.1, (0, 0, 0), E))},
        "perna_esq": {"rotation": canal((0, (-22, 0, 0)), (0.55, (28, 0, 0), E), (1.1, (-22, 0, 0), E))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.25, (-40, 0, 0), E), (0.55, (0, 0, 0), E), (1.1, (0, 0, 0)))},
        "tronco": {"rotation": canal((0, (-2, 4, 2)), (0.55, (-2, -4, -2), E), (1.1, (-2, 4, 2), E)),
                   "position": canal((0, (0, 0, 0)), (0.27, (0, -1.2, 0), E), (0.55, (0, 0, 0), E), (0.82, (0, -1.2, 0), E), (1.1, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (3, -4, 0)), (0.27, (-2, 0, 0), E), (0.55, (3, 4, 0), E), (0.82, (-2, 0, 0), E), (1.1, (3, -4, 0), E))},
        "braco_dir": {"rotation": canal((0, (-18, 0, 0)), (0.55, (18, 0, 0), E), (1.1, (-18, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (18, 0, 0)), (0.55, (-18, 0, 0), E), (1.1, (18, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.3, (-8, 0, 0), E), (0.55, (0, 0, 0), E), (1.1, (0, 0, 0)))},
        "ganchos": {"rotation": canal((0, (14, 0, 0)), (0.15, (16, 0, 3), E), (0.7, (-14, 0, -3), E), (1.1, (14, 0, 0), E))},
    })
    # golpe de cutelo: antecipação 0,4 s (braço vai para cima e para trás), corte, continuação, volta
    ataque = animacao(1.0, False, {
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-125, 0, -20), "easeOutCubic"), (0.55, (65, 0, 5), "easeInQuad"),
                                        (0.7, (78, 0, 8), "easeOutQuad"), (1.0, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-25, 0, 0), "easeOutCubic"), (0.55, (12, 0, 0), "easeInQuad"), (1.0, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.4, (30, 0, 10), "easeOutCubic"), (0.6, (-25, 0, 5), "easeInQuad"), (1.0, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.4, (8, 18, 0), "easeOutCubic"), (0.6, (-22, -14, 0), "easeInQuad"), (1.0, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.4, (14, -12, 0), "easeOutCubic"), (0.6, (-8, 8, 0), "easeInQuad"), (1.0, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.45, (-40, 0, 0), E), (0.75, (45, 0, 0), E), (1.0, (0, 0, 0), E))},
    })
    # Investida de Ganchos: puxa o braço, lança os ganchos à frente, crava, puxa o alvo
    investida = animacao(1.4, False, {
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.5, (-70, 0, 15), "easeOutCubic"), (0.7, (95, 0, 0), "easeInQuad"),
                                        (1.0, (90, 0, 0), E), (1.25, (-30, 0, 10), "easeOutCubic"), (1.4, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.5, (-60, 0, 0), "easeOutCubic"), (0.7, (10, 0, 0), "easeInQuad"),
                                            (1.25, (-20, 0, 0), E), (1.4, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.5, (-35, 0, 0), E), (0.78, (75, 0, 0), "easeOutBack"),
                                      (1.0, (70, 0, 0), E), (1.25, (-20, 0, 0), E), (1.4, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.5, (35, 0, -10), "easeOutCubic"), (0.7, (-20, 0, -5), "easeInQuad"), (1.4, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.5, (8, -16, 0), "easeOutCubic"), (0.7, (-20, 10, 0), "easeInQuad"),
                                     (1.0, (-18, 8, 0), E), (1.25, (10, -6, 0), "easeOutCubic"), (1.4, (0, 0, 0), E)),
                   "position": canal((0, (0, 0, 0)), (0.5, (0, 0, 1.5), E), (0.7, (0, 0, -3), "easeInQuad"), (1.0, (0, 0, -3)), (1.25, (0, 0, 2), E), (1.4, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.5, (10, 10, 0), E), (0.7, (-6, -6, 0), E), (1.4, (0, 0, 0), E))},
    })
    dano = animacao(0.45, False, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.1, (12, 0, -4), "easeOutQuad"), (0.45, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.1, (22, 0, 0), "easeOutQuad"), (0.45, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.1, (-15, 0, -25), "easeOutQuad"), (0.45, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.1, (-15, 0, 25), "easeOutQuad"), (0.45, (0, 0, 0), E))},
        "coracao": {"scale": canal((0, (1, 1, 1)), (0.08, (1.45, 1.45, 1.45), "easeOutQuad"), (0.3, (1, 1, 1), E))},
    })
    # morte própria: tranco, joelhos cedem, cai de cara sobre o cutelo; o coração bate rápido e para
    morrer = animacao(2.2, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.4, (0, 0, 0)), (1.0, (0, -11, 0), "easeInCubic"), (1.6, (0, -15, 0), E), (2.2, (0, -15, 0)))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-6, 0, 0), E), (1.0, (62, 0, -8), "easeInQuad"), (1.6, (70, 0, -10), E))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (1.0, (-115, 0, 0), "easeInQuad"), (1.6, (-120, 0, 0), E))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (0.4, (-6, 0, 0), E), (1.0, (58, 0, 8), "easeInQuad"), (1.6, (66, 0, 10), E))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (1.0, (-110, 0, 0), "easeInQuad"), (1.6, (-118, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.4, (14, 0, 3), "easeOutQuad"), (1.0, (-10, 0, 3), E), (1.6, (-78, 0, 6), "easeInQuad"), (1.75, (-72, 0, 6), "easeOutQuad"), (2.2, (-74, 0, 6), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.4, (28, 0, 0), "easeOutQuad"), (1.0, (10, 0, 0), E), (1.6, (35, 0, 0), E), (1.9, (-20, 0, 12), "easeInQuad"), (2.2, (-22, 0, 12)))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-20, 0, -35), "easeOutQuad"), (1.0, (10, 0, -15), E), (1.6, (75, 0, -10), E), (2.2, (80, 0, -10), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.4, (-20, 0, 35), "easeOutQuad"), (1.0, (10, 0, 15), E), (1.6, (85, 0, 10), E), (2.2, (90, 0, 12), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (1.6, (-30, 0, 0), E), (2.2, (-10, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.5, (30, 0, 0), E), (1.6, (-60, 0, 0), E), (1.9, (-40, 0, 0), "easeOutBounce"), (2.2, (-45, 0, 0)))},
        "coracao": {"scale": canal((0, (1, 1, 1)), (0.1, (1.35, 1.35, 1.35), "easeOutQuad"), (0.3, (1, 1, 1), E), (0.45, (1.35, 1.35, 1.35), "easeOutQuad"),
                                   (0.6, (1, 1, 1), E), (0.75, (1.3, 1.3, 1.3), "easeOutQuad"), (0.9, (1, 1, 1), E), (1.6, (1, 1, 1)), (2.1, (0.7, 0.7, 0.7), "easeInCubic"))},
    })
    # presença: afia o cutelo nos próprios ganchos (duas passadas), depois levanta a cabeça cega e fareja
    afiar = animacao(3.6, False, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.6, (-20, 0, 0), E), (0.9, (-20, -6, 0), E), (1.2, (-20, 4, 0), E), (1.5, (-20, -5, 0), E),
                                     (2.1, (-20, 4, 0), E), (2.7, (4, 0, 0), E), (3.6, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (58, 0, -12), E), (2.6, (58, 0, -12)), (3.3, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (-35, 0, 0), E), (2.6, (-35, 0, 0)), (3.3, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (38, 0, 24), E), (1.2, (72, 0, 18), E), (1.5, (36, 0, 24), E), (2.1, (74, 0, 18), E),
                                        (2.6, (40, 0, 22), E), (3.3, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (-22, 0, 0), E), (1.2, (12, 0, 0), E), (1.5, (-22, 0, 0), E), (2.1, (12, 0, 0), E), (3.3, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.6, (-8, 0, 0), E), (0.9, (-10, 0, 0)), (1.2, (24, 0, 6), "easeOutBack"), (1.5, (-8, 0, -4), E),
                                      (2.1, (26, 0, 6), "easeOutBack"), (2.6, (0, 0, 0), E), (3.6, (0, 0, 0)))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.6, (-10, 0, 0), E), (2.6, (-6, 0, 0), E), (2.9, (18, -22, 0), E), (3.2, (22, 20, 0), E), (3.6, (0, 0, 0), E))},
        "coracao": {"scale": canal((0, (1, 1, 1)), (2.7, (1, 1, 1)), (2.8, (1.3, 1.3, 1.3), "easeOutQuad"), (3.0, (1, 1, 1), E))},
    })
    return {"parado": parado, "andar": andar, "ataque": ataque, "investida": investida, "dano": dano, "morrer": morrer, "afiar": afiar}


# ====================================================================== SANGUESSUGA-MÃE

PERNAS = {"fd": (9, -9), "fe": (-9, -9), "td": (9, 9), "te": (-9, 9)}


def montar_sanguessuga():
    c = Criatura("sanguessuga_mae", 128, 128)
    c.osso("raiz")
    corpo = c.osso("corpo", "raiz", (0, 14, 0))
    # cesta redonda: dois quadrados a 45 graus em cada camada (o rotacionado é 0,2 menor para não brigar nas faces)
    corpo.cubo("casco_a", (-12, 8, -12), (24, 12, 24), casco_pintor)
    corpo.cubo("casco_b", (-12, 8.2, -12), (24, 11.6, 24), None, rotacao=(0, 45, 0), pivo=(0, 14, 0), partilha="casco_a")
    corpo.cubo("topo_a", (-9, 20, -9), (18, 4, 18), topo_pintor)
    corpo.cubo("topo_b", (-9, 20, -9), (18, 3.8, 18), None, rotacao=(0, 45, 0), pivo=(0, 22, 0), partilha="topo_a")
    corpo.cubo("barriga_a", (-9, 4, -9), (18, 4, 18), barriga_pintor)
    corpo.cubo("barriga_b", (-9, 4.2, -9), (18, 3.8, 18), None, rotacao=(0, 45, 0), pivo=(0, 6, 0), partilha="barriga_a")
    for i, yaw in enumerate((0, 90, 180, 270)):
        corpo.cubo(f"espinho_{i}", (-1, 13, -20.5), (2, 2, 8.5), espinho_pintor, rotacao=(28, yaw, 0), pivo=(0, 14, -12),
                   partilha=None if i == 0 else "espinho_0")
    # boca circular na barriga: dois anéis de 4 dentes que se fecham alternados
    for grupo in ("a", "b"):
        d = c.osso(f"dentes_{grupo}", "corpo", (0, 4, 0))
        for k in range(4):
            idx = k * 2 + (0 if grupo == "a" else 1)
            ang = math.radians(idx * 45 + 22.5)
            r = 6.3
            cx, cz = r * math.sin(ang), -r * math.cos(ang)
            yaw = math.degrees(math.atan2(cx, cz))
            d.cubo(f"dente_{idx}", (cx - 1, 1.2, cz - 1), (2, 3, 2), dente_pintor, rotacao=(25, yaw, 0),
                   pivo=(cx, 4.2, cz), partilha=None if idx == 0 else "dente_0")
    # pernas de espinho: coxa sobe para fora (50 graus), canela desce (20 graus para fora) até o chão
    for nome, (px, pz) in PERNAS.items():
        yaw = math.degrees(math.atan2(px, pz))
        coxa = c.osso(f"perna_{nome}", "raiz", (px, 18, pz), (40, yaw, 0))
        coxa.cubo(f"coxa_{nome}", (px - 1.5, 17, pz - 1.5), (3, 19, 3), coxa_pintor, partilha=None if nome == "fd" else "coxa_fd")
        canela = c.osso(f"canela_{nome}", f"perna_{nome}", (px, 36, pz), (-60, 0, 0))  # 40 - 60 = -20: pé para fora
        canela.cubo(f"canela_{nome}", (px - 1, 8, pz - 1), (2, 28, 2), canela_pintor, partilha=None if nome == "fd" else "canela_fd")
        canela.cubo(f"ponta_{nome}", (px - 0.5, 2, pz - 0.5), (1, 6, 1), ponta_pintor, partilha=None if nome == "fd" else "ponta_fd")
    # filhotes presos nas costas (larvas cheias de sangue: o acento)
    c.osso("filhotes", "corpo", (0, 24, 0))
    larvas = [(0, 0, 0, 20, (5, 3, 8)), (1, -5.5, 3, 115, (4, 3, 7)), (2, 5.5, -3, 250, (4, 3, 7)),
              (3, 2.5, 6, 320, (4, 3, 7)), (4, -3, -6, 60, (4, 3, 7)), (5, 6.5, 4.5, 190, (4, 3, 7))]
    for i, cx, cz, yaw, (lw, lh, ld) in larvas:
        incl = -16 if abs(cx) + abs(cz) > 7 else 0
        o = c.osso(f"filhote_{i}", "filhotes", (cx, 24, cz), (incl, yaw, 0))
        grande = (lw, lh, ld) == (5, 3, 8)
        o.cubo(f"larva_{i}", (cx - lw / 2, 24, cz - ld / 2), (lw, lh, ld), larva_pintor,
               partilha=None if (grande or i == 1) else "larva_1")
        o.cubo(f"cabeca_larva_{i}", (cx - 1.5, 24.4, cz - ld / 2 - 2), (3, 2, 2), cabeca_larva_pintor,
               partilha=None if i == 0 else "cabeca_larva_0")
    return c


# ---- pintores da Sanguessuga-Mãe

def trama(face, fx, fy, fw, fh, semente):
    """Cesta de carne trançada: tiras horizontais de 3 px com postes verticais a cada 6 px."""
    if fx % 6 == 0:
        return cor(FIO)
    faixa = fy // 3
    posicao = fy % 3
    sobre = (faixa + fx // 6) % 2 == 0
    i = 1 if posicao == 0 else (2 if posicao == 1 else 4)
    if not sobre:
        i += 1
    if fx % 6 == 1 and not sobre:
        i += 1
    return sombreado(CESTA, face, fx, fy, fw, fh, semente, base=i, grad=False, borda=False, pintas=(0.06, 0.97))


def casco_pintor(face, fx, fy, fw, fh):
    if face in ("up", "down"):
        return sombreado(CESTA, face, fx, fy, fw, fh, 30, base=2)
    return trama(face, fx, fy, fw, fh, 31)


def topo_pintor(face, fx, fy, fw, fh):
    if face == "up":
        d = max(abs(fx - fw / 2 + 0.5), abs(fy - fh / 2 + 0.5))
        anel = int(d) % 3
        # manchas úmidas onde as larvas grudam (acento escuro, pouco)
        for (mx, my) in ((9, 9), (4, 11), (14, 7), (11, 15), (6, 3)):
            if abs(fx - mx) + abs(fy - my) <= 1:
                return tom(SANGUE, 3)
        return sombreado(CESTA, face, fx, fy, fw, fh, 32, base=1 + anel, grad=False)
    if face == "down":
        return tom(CESTA, 4)
    return trama(face, fx, fy, fw, fh, 33)


def barriga_pintor(face, fx, fy, fw, fh):
    if face == "down":
        dx, dy = fx - fw / 2 + 0.5, fy - fh / 2 + 0.5
        r = math.hypot(dx, dy)
        if r < 3.2:
            return cor("#0e0507")
        if r < 5.2:
            return tom(SANGUE, 3) if hash01(fx, fy, 34) > 0.3 else cor("#0e0507")
        if r < 6.8:
            return tom(SANGUE, 2) if hash01(fx, fy, 35) > 0.25 else tom(SANGUE, 1)  # lábio: anel de carne viva
        if r < 8.0:
            return tom(CESTA, 4)
        return sombreado(CESTA, face, fx, fy, fw, fh, 36, base=3)
    if face == "up":
        return tom(CESTA, 4)
    return trama(face, fx, fy, fw, fh, 37)


def espinho_pintor(face, fx, fy, fw, fh):
    if face == "north":
        return tom(QUITINA, 0)
    if face == "up" and fy == 0:
        return tom(QUITINA, 0)
    return sombreado(QUITINA, face, fx, fy, fw, fh, 38, base=1, grad=False)


def dente_pintor(face, fx, fy, fw, fh):
    if face in ("north", "south", "east", "west"):
        if fy == 0:
            return tom(SANGUE, 3)  # gengiva
        return tom(OSSO, 1 if fy == fh - 1 else 2) if hash01(fx, fy, 39) > 0.2 else tom(OSSO, 3)
    return tom(OSSO, 0) if face == "down" else tom(SANGUE, 3)


def coxa_pintor(face, fx, fy, fw, fh):
    if face in ("north", "east") and fx == 0:
        return tom(QUITINA, 0)  # aresta que pega luz
    if fy % 6 == 5:
        return tom(QUITINA, 4)  # juntas
    return sombreado(QUITINA, face, fx, fy, fw, fh, 40, base=1, grad=False)


def canela_pintor(face, fx, fy, fw, fh):
    if face in ("north", "east") and fx == 0:
        return tom(QUITINA, 0)
    if fy % 8 == 7:
        return tom(QUITINA, 4)
    return sombreado(QUITINA, face, fx, fy, fw, fh, 41, base=2 if fy > fh / 2 else 1, grad=False)


def ponta_pintor(face, fx, fy, fw, fh):
    if face == "down":
        return tom(QUITINA, 4)
    return tom(QUITINA, 3 if fy > fh / 2 else 2)


def larva_pintor(face, fx, fy, fw, fh):
    if face == "up":
        if fx in (fw // 2, (fw - 1) // 2) and fy % 2 == 1:
            return tom(SANGUE, 0)  # brilho molhado no dorso
        return tom(SANGUE, 1) if fy % 2 == 0 else tom(SANGUE, 2)
    if face == "down":
        return tom(SANGUE, 3)
    if face in ("east", "west"):
        return tom(SANGUE, 2) if fx % 2 == 0 else (tom(SANGUE, 1) if fy == 0 else tom(SANGUE, 3))
    return tom(SANGUE, 2) if fy < fh - 1 else tom(SANGUE, 3)


def cabeca_larva_pintor(face, fx, fy, fw, fh):
    if face == "north" and fx == 1 and fy == 1:
        return cor("#0e0507")  # ventosa
    return tom(SANGUE, 3) if face != "up" else tom(SANGUE, 2)


# ---- animações da Sanguessuga-Mãe (nomes parado/andar/lancar já usados pelo código do jogo)

def anims_sanguessuga():
    E = "easeInOutSine"

    def wiggle(i, amp=6, dur=4.0):
        f = 0.35 + 0.17 * i
        return canal((0, (0, 0, 0)), (dur * f * 0.5, (0, amp, 0), E), (dur * f, (0, -amp, 0), E), (dur, (0, 0, 0), E))

    parado_ossos = {
        "corpo": {"scale": canal((0, (1, 1, 1)), (2.0, (1.03, 0.97, 1.03), E), (4.0, (1, 1, 1), E)),
                  "rotation": canal((0, (0, 0, 0)), (1.3, (0, 2, 0), E), (2.9, (0, -2, 0), E), (4.0, (0, 0, 0), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (1.0, (0, 0, 0)), (1.15, (0, -0.5, 0), E), (1.3, (0, 0, 0), E), (4.0, (0, 0, 0)))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (3.0, (0, 0, 0)), (3.15, (0, -0.5, 0), E), (3.3, (0, 0, 0), E), (4.0, (0, 0, 0)))},
        "perna_fd": {"rotation": canal((0, (0, 0, 0)), (2.0, (1.5, 0, 0), E), (4.0, (0, 0, 0), E))},
        "perna_te": {"rotation": canal((0, (0, 0, 0)), (2.0, (1.5, 0, 0), E), (4.0, (0, 0, 0), E))},
        "perna_fe": {"rotation": canal((0, (1.5, 0, 0)), (2.0, (0, 0, 0), E), (4.0, (1.5, 0, 0), E))},
        "perna_td": {"rotation": canal((0, (1.5, 0, 0)), (2.0, (0, 0, 0), E), (4.0, (1.5, 0, 0), E))},
    }
    for i in range(6):
        parado_ossos[f"filhote_{i}"] = {"rotation": wiggle(i, 7)}
    parado = animacao(4.0, True, parado_ossos)

    # andar: pares em diagonal (fd+te, fe+td). "Para frente" em Y é + nas pernas da direita e - nas da esquerda.
    andar_ossos = {
        "corpo": {"position": canal((0, (0, 0, 0)), (0.25, (0, -0.6, 0), E), (0.5, (0, 0, 0), E), (0.75, (0, -0.6, 0), E), (1.0, (0, 0, 0), E)),
                  "rotation": canal((0, (0, 0, 2)), (0.5, (0, 0, -2), E), (1.0, (0, 0, 2), E))},
        "filhotes": {"rotation": canal((0, (0, 0, 2)), (0.5, (0, 0, -2), E), (1.0, (0, 0, 2), E))},
    }
    for nome, (px, pz) in PERNAS.items():
        sgn = 1 if px > 0 else -1
        a = 13 * sgn
        if nome in ("fd", "te"):   # balanço no ar entre 0 e 0,5
            andar_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, -a, 0)), (0.5, (0, a, 0), E), (1.0, (0, -a, 0), E))}
            andar_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.25, (-28, 0, 0), E), (0.5, (0, 0, 0), E), (1.0, (0, 0, 0)))}
        else:
            andar_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, a, 0)), (0.5, (0, -a, 0), E), (1.0, (0, a, 0), E))}
            andar_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (0.75, (-28, 0, 0), E), (1.0, (0, 0, 0), E))}
    andar = animacao(1.0, True, andar_ossos)

    # mordida: ergue o corpo (antecipação 0,35 s), desaba sobre o alvo, anéis de dentes fecham
    mordida_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.35, (0, 3, 0), "easeOutCubic"), (0.5, (0, -3.5, 0), "easeInCubic"), (0.65, (0, -3, 0), E), (0.9, (0, 0, 0), E))},
        "corpo": {"rotation": canal((0, (0, 0, 0)), (0.35, (12, 0, 0), "easeOutCubic"), (0.5, (-14, 0, 0), "easeInCubic"), (0.9, (0, 0, 0), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.35, (0, -1.2, 0), E), (0.55, (0, 0.6, 0), "easeInQuad"), (0.7, (0, 0.6, 0)), (0.9, (0, 0, 0), E)),
                     "scale": canal((0, (1, 1, 1)), (0.35, (1.25, 1, 1.25), E), (0.55, (0.75, 1.1, 0.75), "easeInQuad"), (0.7, (0.75, 1.1, 0.75)), (0.9, (1, 1, 1), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.35, (0, -1.2, 0), E), (0.6, (0, 0.6, 0), "easeInQuad"), (0.7, (0, 0.6, 0)), (0.9, (0, 0, 0), E)),
                     "scale": canal((0, (1, 1, 1)), (0.35, (1.25, 1, 1.25), E), (0.6, (0.75, 1.1, 0.75), "easeInQuad"), (0.7, (0.75, 1.1, 0.75)), (0.9, (1, 1, 1), E))},
        "filhotes": {"position": canal((0, (0, 0, 0)), (0.35, (0, 0.5, 0), E), (0.55, (0, -0.8, 0), "easeInCubic"), (0.9, (0, 0, 0), E))},
    }
    for nome in PERNAS:
        mordida_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.35, (16, 0, 0), "easeOutCubic"), (0.5, (-18, 0, 0), "easeInCubic"), (0.9, (0, 0, 0), E))}
    mordida = animacao(0.9, False, mordida_ossos)

    # Chuva de Filhotes: empina (frente sobe, pernas da frente no ar), chicoteia para a frente, as larvas voam e somem
    lancar_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.5, (0, 1, 0), E), (0.65, (0, -0.5, 0), "easeInQuad"), (0.8, (0, -1, 0), E), (1.05, (0, 0, 0), E))},
        "corpo": {"rotation": canal((0, (0, 0, 0)), (0.5, (22, 0, 0), "easeOutCubic"), (0.65, (-18, 0, 0), "easeInQuad"), (0.85, (-6, 0, 0), E), (1.3, (0, 0, 0), E))},
        "filhotes": {"position": canal((0, (0, 0, 0)), (0.5, (0, 1.2, 1), E), (0.66, (0, 7, -11), "easeInQuad"), (0.72, (0, 7, -14)), (1.1, (0, 0, 0)), (1.3, (0, 0, 0))),
                     "scale": canal((0, (1, 1, 1)), (0.5, (1.1, 1.1, 1.1), E), (0.66, (1, 1, 1)), (0.72, (0, 0, 0)), (1.1, (0, 0, 0)), (1.3, (1, 1, 1), "easeOutBack"))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.5, (0, -0.8, 0), E), (0.9, (0, 0, 0), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.5, (0, -0.8, 0), E), (0.9, (0, 0, 0), E))},
        "canela_fd": {"rotation": canal((0, (0, 0, 0)), (0.5, (-45, 0, 0), "easeOutCubic"), (0.75, (10, 0, 0), "easeInQuad"), (1.05, (0, 0, 0), E))},
        "canela_fe": {"rotation": canal((0, (0, 0, 0)), (0.5, (-45, 0, 0), "easeOutCubic"), (0.75, (10, 0, 0), "easeInQuad"), (1.05, (0, 0, 0), E))},
        "perna_fd": {"rotation": canal((0, (0, 0, 0)), (0.5, (-12, 0, 0), "easeOutCubic"), (0.75, (4, 0, 0), "easeInQuad"), (1.05, (0, 0, 0), E))},
        "perna_fe": {"rotation": canal((0, (0, 0, 0)), (0.5, (-12, 0, 0), "easeOutCubic"), (0.75, (4, 0, 0), "easeInQuad"), (1.05, (0, 0, 0), E))},
        "canela_td": {"rotation": canal((0, (0, 0, 0)), (0.5, (12, 0, 0), E), (1.05, (0, 0, 0), E))},
        "canela_te": {"rotation": canal((0, (0, 0, 0)), (0.5, (12, 0, 0), E), (1.05, (0, 0, 0), E))},
    }
    lancar = animacao(1.3, False, lancar_ossos)

    dano_ossos = {
        "corpo": {"position": canal((0, (0, 0, 0)), (0.1, (0, 0, 1.8), "easeOutQuad"), (0.4, (0, 0, 0), E)),
                  "rotation": canal((0, (0, 0, 0)), (0.1, (9, 0, 0), "easeOutQuad"), (0.4, (0, 0, 0), E)),
                  "scale": canal((0, (1, 1, 1)), (0.1, (0.95, 1.05, 0.95), "easeOutQuad"), (0.4, (1, 1, 1), E))},
        "filhotes": {"rotation": canal((0, (0, 0, 0)), (0.1, (-10, 0, 0), "easeOutQuad"), (0.4, (0, 0, 0), E))},
    }
    dano = animacao(0.4, False, dano_ossos)

    # morte própria: empina, as pernas abrem e cedem, a cesta desaba e tomba, a boca fica aberta, as larvas fogem
    morrer_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.3, (0, 1, 0), E), (1.1, (0, -3.5, 0), "easeInCubic"), (1.3, (0, -2.8, 0), "easeOutQuad"), (1.5, (0, -3.5, 0), E), (2.4, (0, -3.5, 0)))},
        "corpo": {"rotation": canal((0, (0, 0, 0)), (0.3, (12, 0, 0), E), (1.1, (-4, 0, 6), "easeInCubic"), (1.6, (-6, 0, 24), E), (2.4, (-6, 0, 26), E)),
                  "scale": canal((0, (1, 1, 1)), (1.1, (1, 1, 1)), (2.4, (1.06, 0.84, 1.06), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.3, (0, -1, 0), E), (1.1, (0, -1.4, 0)), (1.6, (0, -0.8, 0), E), (2.4, (0, -0.8, 0))),
                     "scale": canal((0, (1, 1, 1)), (0.3, (1.3, 1, 1.3), E), (2.4, (1.35, 1, 1.35), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.3, (0, -1, 0), E), (1.1, (0, -1.4, 0)), (1.6, (0, -0.8, 0), E), (2.4, (0, -0.8, 0))),
                     "scale": canal((0, (1, 1, 1)), (0.3, (1.3, 1, 1.3), E), (2.4, (1.35, 1, 1.35), E))},
    }
    for nome, (px, pz) in PERNAS.items():
        morrer_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.3, (-6, 0, 0), E), (1.1, (32, 0, 0), "easeInCubic"), (1.3, (28, 0, 0), "easeOutQuad"), (2.4, (34, 0, 0), E))}
        morrer_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.3, (6, 0, 0), E), (1.1, (26, 0, 0), "easeInCubic"), (2.4, (30, 0, 0), E))}
    for i in range(6):
        ang = math.radians(60 * i + 15)
        dx, dz = 7 * math.sin(ang), 7 * math.cos(ang)
        t0 = 1.15 + 0.08 * i
        morrer_ossos[f"filhote_{i}"] = {
            "position": canal((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.45, (dx, -3, dz), "easeInQuad"), (2.4, (dx, -3, dz))),
            "scale": canal((0, (1, 1, 1)), (t0 + 0.3, (1, 1, 1)), (t0 + 0.5, (0, 0, 0), "easeInQuad"), (2.4, (0, 0, 0))),
            "rotation": canal((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.45, (0, 90, 0), E), (2.4, (0, 90, 0))),
        }
    morrer = animacao(2.4, False, morrer_ossos)

    # presença: "chocar". Abaixa a cesta até o chão, três pulsos pesados, as larvas rodam e uma desce até a boca
    pulso = lambda t: [(t, (1, 1, 1)), (t + 0.35, (1.09, 0.9, 1.09), "easeOutQuad"), (t + 0.7, (1, 1, 1), E)]
    sobe = lambda t: [(t, (0, 0, 0)), (t + 0.35, (0, 0.9, 0), "easeOutQuad"), (t + 0.7, (0, 0, 0), E)]
    chocar_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.8, (0, -3, 0), E), (3.5, (0, -3, 0)), (4.4, (0, 0, 0), E))},
        "corpo": {"scale": canal(*([(0, (1, 1, 1))] + pulso(1.0) + pulso(1.9) + pulso(2.8) + [(4.4, (1, 1, 1))])),
                  "rotation": canal((0, (0, 0, 0)), (0.8, (-5, 0, 0), E), (3.5, (-5, 0, 0)), (4.4, (0, 0, 0), E))},
        "filhotes": {"rotation": canal((0, (0, 0, 0)), (1.0, (0, 0, 0)), (3.4, (0, 180, 0), E), (4.4, (0, 180, 0))),
                     "position": canal(*([(0, (0, 0, 0))] + sobe(1.0) + sobe(1.9) + sobe(2.8) + [(4.4, (0, 0, 0))]))},
        "filhote_3": {"position": canal((0, (0, 0, 0)), (1.2, (0, 0, 0)), (2.0, (5, -1, 3), E), (2.6, (6, -9, 3.5), "easeInQuad"), (3.1, (6, -9, 3.5)), (3.6, (0, 0, 0), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.8, (0, -0.6, 0), E), (2.6, (0, -0.6, 0)), (2.8, (0, 0.5, 0), "easeInQuad"), (3.1, (0, 0.5, 0)), (3.5, (0, 0, 0), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.8, (0, -0.6, 0), E), (2.6, (0, -0.6, 0)), (2.85, (0, 0.5, 0), "easeInQuad"), (3.1, (0, 0.5, 0)), (3.5, (0, 0, 0), E))},
    }
    for nome in PERNAS:
        chocar_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.8, (10, 0, 0), E), (3.5, (10, 0, 0)), (4.4, (0, 0, 0), E))}
        chocar_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.8, (-14, 0, 0), E), (3.5, (-14, 0, 0)), (4.4, (0, 0, 0), E))}
    chocar = animacao(4.4, False, chocar_ossos)
    return {"parado": parado, "andar": andar, "mordida": mordida, "lancar": lancar, "dano": dano, "morrer": morrer, "chocar": chocar}


# ====================================================================== provisório (igual ao gerado em código no jogo)

def textura_provisoria(base, sombra, brilho, costura, semente, lado=256):
    """Porta de ModeloCriatura.gerar (Java) para numpy: mesma textura que o jogo mostra hoje."""
    ys, xs = np.mgrid[0:lado, 0:lado].astype(np.uint32)

    def ruido(x, y):
        h = (x * np.uint32(374761393) + y * np.uint32(668265263) + np.uint32(semente) * np.uint32(982451653))
        h = ((h ^ (h >> np.uint32(13))) * np.uint32(1274126177))
        return (h ^ (h >> np.uint32(16))) & np.uint32(0x7FFFFFFF)

    with np.errstate(over="ignore"):
        h = ruido(xs >> 1, ys >> 1)
        img = np.empty((lado, lado, 4), np.uint8)
        img[:, :] = cor(base)
        img[h % 5 == 0] = cor(brilho)
        img[h % 7 == 0] = cor(sombra)
        veia = ((xs + (ys >> 2) * 3 + (ruido(xs >> 3, ys >> 3) & 3)) % 37) == 0
        img[veia] = cor(sombra)
        fy = ys % 24
        cost = (fy == 12) | ((fy >= 10) & (fy <= 14) & (xs % 6 == 0))
        img[cost] = cor(costura)
    return img


PROVISORIOS = {
    "retalhado": ("#6E1414", "#2A0000", "#9E2A22", "#1A0E0A", 11),
    "sanguessuga_mae": ("#4A1410", "#1E0504", "#8B3A2E", "#D8C8B0", 23),
}


# ====================================================================== geração

def ampliar(img, fator):
    return np.repeat(np.repeat(img, fator, axis=0), fator, axis=1)


def gerar(nome, montar, anims_fn, caixa_visivel):
    pasta = PREVIAS / nome
    pasta.mkdir(parents=True, exist_ok=True)
    BLOCKBENCH.mkdir(parents=True, exist_ok=True)
    cria = montar()
    cria.empacotar_uv()
    tex, glow = cria.pintar()
    anims = anims_fn()
    geo = cria.geo(caixa_visivel)
    (pasta / f"{nome}.geo.json").write_text(json_compacto(geo) + "\n", encoding="utf-8")
    (pasta / f"{nome}.animation.json").write_text(json_compacto(arquivo_animacoes(nome, anims)) + "\n", encoding="utf-8")
    rg.salvar_png(pasta / f"{nome}.png", tex)
    if glow is not None:
        rg.salvar_png(pasta / f"{nome}_glowmask.png", glow)
    rg.salvar_png(pasta / "textura_ampliada.png", ampliar(tex, 4))
    png_bytes = (pasta / f"{nome}.png").read_bytes()
    (BLOCKBENCH / f"{nome}.bbmodel").write_text(json.dumps(bbmodel(cria, anims, png_bytes, f"{nome}.png", caixa_visivel[:2])),
                                                encoding="utf-8")
    n_cubos = len(cria.cubos())
    print(f"{nome}: {n_cubos} cubos, {len(cria.ossos)} ossos, textura {cria.tex_w}x{cria.tex_h}, {len(anims)} animações")
    modelo = rg.gerar_previas(pasta / f"{nome}.geo.json", pasta / f"{nome}.png", pasta, pasta / f"{nome}.animation.json",
                              pasta / f"{nome}_glowmask.png" if glow is not None else None, escala=6.0, quadros=8, nome=nome)
    mn, mx = modelo.caixa()
    print(f"  caixa: largura {mx[0]-mn[0]:.1f} x altura {mx[1]-mn[1]:.1f} x fundo {mx[2]-mn[2]:.1f} (unidades; 16 = 1 bloco)")
    comparar(nome, modelo, tex, glow, pasta)
    return modelo


def comparar(nome, novo, tex, glow, pasta):
    """Folha ANTES (provisório que está no jogo) x DEPOIS (proposta), mesma escala."""
    geo_velho = GEO_JOGO / f"{nome}.geo.json"
    if not geo_velho.exists():
        return
    velho = rg.Modelo(geo_velho)
    tex_velha = textura_provisoria(*PROVISORIOS[nome])
    escala = 5.0
    tam_n = rg.tamanho_para(novo, escala)
    tam_v = rg.tamanho_para(velho, escala)
    tam = (max(tam_n[0], tam_v[0]), max(tam_n[1], tam_v[1]))
    imgs = []
    for rot, az, el in (("FRENTE", 0, 0), ("3/4", 35, 22), ("LADO", 90, 0)):
        a = rg.renderizar(velho, tex_velha, rg.Camera(az, el), escala=escala, tamanho=tam, centro=rg.centro_para(velho), rotulo=f"ANTES: {rot}")
        b = rg.renderizar(novo, tex, rg.Camera(az, el), glow=glow, escala=escala, tamanho=tam, centro=rg.centro_para(novo), rotulo=f"DEPOIS: {rot}")
        imgs.append(rg.juntar_horizontal([a, b], espaco=4))
    largura = imgs[0].shape[1]
    folha = rg.juntar_vertical([rg.faixa_titulo(f"{nome.upper()}: ANTES (PROVISORIO NO JOGO) X DEPOIS (PROPOSTA)", largura)] + imgs)
    rg.salvar_png(pasta / "comparacao.png", folha)


def main():
    alvo = sys.argv[2] if len(sys.argv) > 2 else "todas"
    feitos = {}
    if alvo in ("retalhado", "todas"):
        feitos["retalhado"] = gerar("retalhado", montar_retalhado, anims_retalhado, (3, 3.2, 1.5))
    if alvo in ("sanguessuga_mae", "todas"):
        feitos["sanguessuga_mae"] = gerar("sanguessuga_mae", montar_sanguessuga, anims_sanguessuga, (4, 2.5, 1))
    if len(feitos) == 2:
        folhas = [rg.ler_png(PREVIAS / n / "comparacao.png") for n in feitos]
        rg.salvar_png(PREVIAS / "comparacao_sangue.png", rg.juntar_vertical(folhas, espaco=16))
    print("pronto:", PREVIAS)


if __name__ == "__main__":
    main()
