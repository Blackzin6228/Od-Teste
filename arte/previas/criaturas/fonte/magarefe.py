"""Magarefe (Casa do Açougueiro, Ato 1): alto, ombros largos, avental, cutelo, cabeça baixa. Anda devagar e não morre
no Ato 1 (lore/roteiro.md, docs/decisoes/2026-11-09-nomes-acougueiro-magarefe.md). Ainda não existe no jogo: só prévia.
Diferente do Retalhado de propósito: é gente esticada, não um bruto costurado. Cabeça num saco de estopa amarrado,
sem rosto; avental de couro até a canela com o sangue escorrido (o acento); cutelo de cabo na mão, arrastando;
Sino de Matadouro pendurado no cinto."""
import math

from montagem import Criatura, animacao, canal, cor, costura_v, hash01, sombreado, tom

SANGUE = ["#E0313A", "#B3131B", "#7A0A10", "#3A0306"]
PELE = ["#8c7c72", "#76675e", "#60534c", "#4a403a", "#342d29"]
ESTOPA = ["#746753", "#625745", "#4f4638", "#3c352b", "#2a251e"]
COURO = ["#4e3a2c", "#3f2f24", "#31241c", "#241a14", "#17110d"]
PANO = ["#3a3435", "#2e292a", "#232020", "#181616"]
METAL = ["#8a8a84", "#6a6a64", "#4c4c47", "#333330", "#1f1f1d"]
OSSO = ["#d6cbae", "#b5a98c", "#8f8468", "#665d47"]
FIO = "#15100f"
GUME = "#b4b4ac"


def mat(paleta, semente, **k):
    return lambda face, fx, fy, fw, fh: sombreado(paleta, face, fx, fy, fw, fh, semente, **k)


def montar():
    c = Criatura("magarefe", 128, 128)
    c.osso("raiz")
    # pernas longas, botas pesadas
    for lado, s, esp in (("dir", -1, False), ("esq", 1, True)):
        p = c.osso(f"perna_{lado}", "raiz", (3.5 * s, 26, 0))
        xo = -6.5 if s < 0 else 0.5
        p.cubo(f"coxa_{lado}", (xo, 13, -3), (6, 14, 6), mat(PANO, 1), espelho=esp, partilha=None if not esp else "coxa_dir")
        k = c.osso(f"canela_{lado}", f"perna_{lado}", (3.5 * s, 13, 0))
        k.cubo(f"canela_{lado}", (xo + 0.5, 4, -2.5), (5, 10, 5), mat(PANO, 2, base=2), espelho=esp, partilha=None if not esp else "canela_dir")
        k.cubo(f"bota_{lado}", (xo, 0, -4.5), (6, 4, 8), bota, espelho=esp, partilha=None if not esp else "bota_dir")
    tronco = c.osso("tronco", "raiz", (0, 26, 0), (-6, 0, 0))
    tronco.cubo("abdomen", (-6, 25, -3.5), (12, 11, 7), camisa)
    tronco.cubo("peito", (-8, 36, -4.5), (16, 12, 9), camisa)
    tronco.cubo("ombro_dir", (-14, 42, -4), (7, 6, 8), ombro)
    tronco.cubo("ombro_esq", (7, 42, -4), (7, 6, 8), ombro, espelho=True, partilha="ombro_dir")
    tronco.cubo("cinto", (-6.5, 33, -4), (13, 3, 8), cinto, inflar=0.2)
    tronco.cubo("peitilho", (-5, 38, -5.2), (10, 10, 0.7), avental_pintor)
    tronco.cubo("avental", (-7, 8, -4.6), (14, 30, 0.7), avental_pintor)
    sino = c.osso("sino", "tronco", (-7, 33, 0))
    sino.cubo("sino_alca", (-7.5, 31, -0.5), (1, 2, 1), mat(METAL, 3, grad=False))
    sino.cubo("sino_corpo", (-8.5, 27.5, -1.5), (3, 3.5, 3), sino_pintor)
    # pescoço e cabeça no saco, caída para a frente
    pescoco = c.osso("pescoco", "tronco", (0, 48, -1))
    pescoco.cubo("pescoco", (-2, 46, -3), (4, 4, 4), mat(PELE, 3))
    cabeca = c.osso("cabeca", "pescoco", (0, 50, -1), (-30, 0, 0))
    cabeca.cubo("saco", (-4, 49, -5.5), (8, 10, 8), saco_pintor)
    cabeca.cubo("no_corda", (-1.5, 48, -6.5), (3, 2, 2), mat(COURO, 4, grad=False))
    # braços compridos
    for lado, s, esp in (("dir", -1, False), ("esq", 1, True)):
        b = c.osso(f"braco_{lado}", "tronco", (11 * s, 46, 0), (4, 0, -4 * s))
        xo = -13.5 if s < 0 else 8.5
        b.cubo(f"braco_{lado}", (xo, 32, -2.5), (5, 14, 5), mat(PELE, 5), espelho=esp, partilha=None if not esp else "braco_dir")
        a = c.osso(f"antebraco_{lado}", f"braco_{lado}", (11 * s, 33, 0), (-5, 0, 0))
        a.cubo(f"antebraco_{lado}", (xo + 0.5, 19, -2), (4, 14, 4), antebraco, espelho=esp, partilha=None if not esp else "antebraco_dir")
        a.cubo(f"mao_{lado}", (xo, 13, -2.5), (5, 6, 5), mat(PELE, 6, base=1), espelho=esp, partilha=None if not esp else "mao_dir")
    ad = c.por_nome["antebraco_dir"]
    ad.cubo("cabo", (-12, 9, -1), (2, 7, 2), mat(COURO, 7, grad=False))
    ad.cubo("lamina", (-11.75, -1, -8), (1.5, 11, 9), lamina_pintor)
    ae = c.por_nome["antebraco_esq"]
    for i, x in enumerate((8.6, 10.3, 12.0)):
        ae.cubo(f"dedo_{i}", (x, 8, -1), (1, 5.5, 1), mat(PELE, 8, base=2, grad=False), partilha=None if i == 0 else "dedo_0")
    return c


# ---- pintores

def camisa(face, fx, fy, fw, fh):
    if face in ("north", "south", "east", "west") and fx % 4 == 2 and hash01(fx, fy, 10) > 0.5:
        return tom(PANO, 0)  # fio puído
    return sombreado(PANO, face, fx, fy, fw, fh, 11, base=1)


def ombro(face, fx, fy, fw, fh):
    if face == "up" and fy == 1:
        return tom(COURO, 1)  # tira do avental passando pelo ombro
    return sombreado(PANO, face, fx, fy, fw, fh, 12, base=1)


def cinto(face, fx, fy, fw, fh):
    if face in ("north", "south", "east", "west"):
        if fy == 1 and fx % 4 == 1:
            return tom(METAL, 2)  # rebites
        if face == "north" and 5 <= fx <= 7 and fy >= 1:
            return tom(METAL, 1) if fy == 1 else tom(METAL, 3)  # fivela
        if face == "east" and fx in (2, 3):
            return tom(OSSO, 1) if fy == 0 else tom(OSSO, 2)  # cabo de osso de uma faca
    return sombreado(COURO, face, fx, fy, fw, fh, 13, base=1, grad=False)


def avental_pintor(face, fx, fy, fw, fh):
    if face != "north":
        return tom(COURO, 3) if face in ("east", "west", "up", "down") else sombreado(COURO, face, fx, fy, fw, fh, 14, base=2)
    # couro gasto com dobras verticais, borda rasgada embaixo e sangue escorrido do peitilho para baixo
    r = hash01(fx, fy, 15)
    coluna = hash01(fx, 0, 16)
    comprimento = int(coluna * fh * 0.55) + (6 if fh > 12 else 2)
    escorrido = fy < comprimento and (coluna > 0.45 or fy < 2)
    if escorrido and r > 0.12:
        return tom(SANGUE, 3) if (r > 0.3 or fy > 2) else tom(SANGUE, 2)
    if fy == fh - 1 and fx % 3 != 1 and fh > 12:
        return None  # barra rasgada (transparente)
    if fx == 0 or fx == fw - 1:
        return tom(COURO, 4)
    if fx % 5 == 3:
        return tom(COURO, 2)  # dobra
    return sombreado(COURO, face, fx, fy, fw, fh, 17, base=0 if fy < 3 else 1, pintas=(0.06, 0.96))


def sino_pintor(face, fx, fy, fw, fh):
    if face == "down":
        return cor("#0e0a0a") if (fx, fy) == (1, 1) else tom(METAL, 4)
    if fy == fh - 1:
        return tom(METAL, 1)
    return tom(METAL, 3 if fx == fw - 1 else 2)


def saco_pintor(face, fx, fy, fw, fh):
    # estopa com trama; sem rosto. Na frente, duas manchas úmidas onde seriam os olhos (acento escuro).
    if face == "north" and fy in (4, 5) and fx in (2, 5):
        return tom(SANGUE, 3)
    if face == "north" and fy == 7 and 2 <= fx <= 5:
        return tom(ESTOPA, 3)  # vinco onde a boca respira por baixo
    if face == "up" and (fx + fy) % 2 == 0:
        return tom(ESTOPA, 2)
    base = 1 if (fx + fy) % 2 == 0 else 2
    if fx % 4 == 0 or fy % 4 == 0:
        base += 1  # trama grossa
    return sombreado(ESTOPA, face, fx, fy, fw, fh, 18, base=base, grad=False)


def antebraco(face, fx, fy, fw, fh):
    if face in ("north", "east", "west") and fy % 5 == 2 and fx % 3 == 0:
        return tom(PELE, 4)  # cortes antigos nos braços
    if fy >= fh - 2 and hash01(fx, fy, 19) > 0.6:
        return tom(SANGUE, 3)  # as mãos ficaram sujas
    return sombreado(PELE, face, fx, fy, fw, fh, 20, base=1)


def bota(face, fx, fy, fw, fh):
    if face in ("north", "east", "west", "south") and fy == 0:
        return tom(COURO, 1)
    if face == "north" and fy == fh - 1:
        return tom(METAL, 3)  # biqueira
    return sombreado(COURO, face, fx, fy, fw, fh, 21, base=2, grad=False)


def lamina_pintor(face, fx, fy, fw, fh):
    if face in ("north", "down"):
        return cor(GUME) if hash01(fx, fy, 22) > 0.2 else tom(SANGUE, 2)
    if face in ("south", "up"):
        return tom(METAL, 3)
    if fy <= 0:
        return tom(METAL, 4)
    r = hash01(fx, fy, 23)
    if (fy >= fh - 2 or fx <= 1) and r < 0.35:
        return tom(SANGUE, 3)
    if fy == fh - 1 or fx == 0:
        return tom(METAL, 0)
    if (fx + fy) % 5 == 0:
        return tom(METAL, 1)
    return tom(METAL, 2 if fy < fh * 0.5 else 1)


# ---- animações (devagar: é uma presença, não um perseguidor rápido)

def anims():
    E = "easeInOutSine"
    parado = animacao(5.0, True, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (2.5, (2, 0, 0), E), (5.0, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (1.5, (3, -10, 2), E), (3.5, (2, 12, -2), E), (5.0, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (2.5, (-3, 0, -1), E), (5.0, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (2.5, (-3, 0, 1), E), (5.0, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (3.8, (0, 0, 0)), (4.0, (6, 0, 0), E), (4.2, (0, 0, 0), E), (5.0, (0, 0, 0)))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (2.5, (2, 0, 3), E), (5.0, (0, 0, 0), E))},
    })
    andar = animacao(1.8, True, {
        "perna_dir": {"rotation": canal((0, (22, 0, 0)), (0.9, (-18, 0, 0), E), (1.8, (22, 0, 0), E))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.9, (0, 0, 0)), (1.3, (-30, 0, 0), E), (1.8, (0, 0, 0), E))},
        "perna_esq": {"rotation": canal((0, (-18, 0, 0)), (0.9, (22, 0, 0), E), (1.8, (-18, 0, 0), E))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.4, (-30, 0, 0), E), (0.9, (0, 0, 0), E), (1.8, (0, 0, 0)))},
        "tronco": {"rotation": canal((0, (-2, 3, 1.5)), (0.9, (-2, -3, -1.5), E), (1.8, (-2, 3, 1.5), E)),
                   "position": canal((0, (0, 0, 0)), (0.45, (0, -1, 0), E), (0.9, (0, 0, 0), E), (1.35, (0, -1, 0), E), (1.8, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (2, -3, 0)), (0.9, (2, 3, 0), E), (1.8, (2, -3, 0), E))},
        "braco_dir": {"rotation": canal((0, (-8, 0, 0)), (0.9, (10, 0, 0), E), (1.8, (-8, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (12, 0, 0)), (0.9, (-14, 0, 0), E), (1.8, (12, 0, 0), E))},
        "sino": {"rotation": canal((0, (10, 0, 0)), (0.9, (-10, 0, 0), E), (1.8, (10, 0, 0), E))},
    })
    # golpe: ergue o cutelo com as duas mãos, devagar (0,6 s de antecipação), e desce de uma vez
    ataque = animacao(1.4, False, {
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (-150, 0, -15), "easeOutCubic"), (0.75, (50, 0, 0), "easeInQuad"), (0.9, (62, 0, 2), "easeOutQuad"), (1.4, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (-40, 0, 0), "easeOutCubic"), (0.75, (10, 0, 0), "easeInQuad"), (1.4, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (-120, 0, 25), "easeOutCubic"), (0.75, (40, 0, 10), "easeInQuad"), (1.4, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (-50, 0, 0), "easeOutCubic"), (0.75, (0, 0, 0), "easeInQuad"), (1.4, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.6, (12, 0, 0), "easeOutCubic"), (0.78, (-30, 0, 0), "easeInQuad"), (1.0, (-26, 0, 0), E), (1.4, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.6, (26, 0, 0), "easeOutCubic"), (0.78, (-6, 0, 0), "easeInQuad"), (1.4, (0, 0, 0), E))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (0.6, (-15, 0, 0), E), (0.85, (35, 0, 0), "easeOutBack"), (1.4, (0, 0, 0), E))},
    })
    dano = animacao(0.4, False, {  # quase não sente
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.1, (4, 0, 2), "easeOutQuad"), (0.4, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.15, (14, 0, 0), "easeOutQuad"), (0.4, (0, 0, 0), E))},
    })
    morrer = animacao(2.6, False, {  # reservada para depois do Ato 1: ajoelha, larga o cutelo, tomba de lado
        "raiz": {"position": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (1.2, (0, -12, 0), "easeInCubic"), (2.0, (0, -17, 0), E), (2.6, (0, -17, 0)))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (1.2, (70, 0, -6), "easeInQuad"), (2.6, (70, 0, -6)))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (1.2, (-125, 0, 0), "easeInQuad"), (2.6, (-125, 0, 0)))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (1.2, (66, 0, 6), "easeInQuad"), (2.6, (66, 0, 6)))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (1.2, (-120, 0, 0), "easeInQuad"), (2.6, (-120, 0, 0)))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.5, (-10, 0, 0), E), (1.2, (-18, 0, 0), E), (2.0, (-20, 0, -80), "easeInQuad"), (2.15, (-20, 0, -74), "easeOutQuad"), (2.6, (-20, 0, -76), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.5, (-12, 0, 0), E), (1.2, (-14, 0, 0), E), (2.6, (-6, 0, 10), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.5, (10, 0, -10), E), (1.2, (35, 0, -20), E), (2.6, (40, 0, -30), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (1.2, (-40, 0, 0), E), (2.6, (-20, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (1.2, (30, 0, 20), E), (2.6, (80, 0, 30), E))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (1.2, (20, 0, 0), E), (1.5, (-25, 0, 0), E), (2.0, (40, 0, 0), E), (2.6, (0, 0, 0), "easeOutBounce"))},
    })
    # presença: para, ergue o saco devagar e "escuta" (vira a cabeça), depois limpa o cutelo no avental
    escutar = animacao(4.6, False, {
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.8, (28, 0, 0), E), (1.5, (30, -35, 0), E), (2.4, (30, 35, 0), E), (3.0, (24, 0, 0), E), (3.6, (-4, 0, 0), E), (4.6, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.8, (4, 0, 0), E), (3.0, (4, 0, 0)), (3.6, (-6, 8, 0), E), (4.1, (-6, -4, 0), E), (4.6, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (3.0, (0, 0, 0)), (3.6, (34, 0, -30), E), (3.9, (26, 0, -22), E), (4.15, (36, 0, -30), E), (4.6, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (3.0, (0, 0, 0)), (3.6, (-45, 0, 0), E), (3.9, (-30, 0, 0), E), (4.15, (-45, 0, 0), E), (4.6, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.8, (-4, 0, 8), E), (3.0, (-4, 0, 8)), (4.6, (0, 0, 0), E))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (3.6, (0, 0, 0)), (3.8, (12, 0, 4), E), (4.1, (-8, 0, -3), E), (4.6, (0, 0, 0), E))},
    })
    return {"parado": parado, "andar": andar, "ataque": ataque, "dano": dano, "morrer": morrer, "escutar": escutar}
