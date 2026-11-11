"""Retalhado, versão 5.2 (revisão crítica da 5.1). lore/criaturas.md: bruto curvado, ombros largos, braços até o
joelho, costuras grossas, cutelo preso ao antebraço, coração exposto; cego nas costas.

O que mudou da 5.1 (ver arte/previas/criaturas/comparacao/LEIA.md):
- Silhueta assimétrica: ombro direito (do cutelo) maior e mais alto, ombro esquerdo caído, corcova maior com três
  vértebras saltadas, cabeça afundada e torta entre os ombros, perna esquerda virada para fora (manca).
- Cutelo maior e lascado; três ganchos em "J" com elos de corrente, mais grossos (leem a 30 blocos).
- Pele em retalhos de três pessoas diferentes (pálida, roxa de hematoma, curtida), costura só na fronteira dos
  retalhos, sangue seco junto da costura. Acabou a grade de cruzes.
- Animações com peso: tronco cai no contato do pé, braços atrasados, antecipação e continuação no golpe,
  morte em dois tempos (um joelho cede antes do outro), coração com batida dupla que para.
"""
from montagem import Criatura, animacao, canal, cor, costura_h, hash01, patchwork, sombreado, tom

SANGUE = ["#E0313A", "#B3131B", "#7A0A10", "#3A0306"]
# Três "pessoas" na mesma pele (tons neutros; o vermelho fica só no acento).
PALIDA = ["#a89d93", "#8f847a", "#776c64", "#5f5650", "#48413c", "#302b28"]
ROXA = ["#86727c", "#705e68", "#5b4b54", "#463a41", "#332a2f", "#211b1e"]
CURTIDA = ["#95785f", "#7c6350", "#644f40", "#4d3c31", "#372b23", "#231b16"]
PELES = [PALIDA, ROXA, CURTIDA]
METAL = ["#9a9a93", "#767670", "#55554f", "#393936", "#232321"]
FERRUGEM = ["#6b4327", "#4e301c"]
GUME = "#c9c9c0"
COURO = ["#5a4432", "#46342a", "#35281f", "#241a14"]
OSSO = ["#e0d6ba", "#bfb294", "#978b6e", "#6b614a"]
FIO = "#15100f"
CAVIDADE = "#140709"


def mat(paleta, semente, **k):
    return lambda face, fx, fy, fw, fh: sombreado(paleta, face, fx, fy, fw, fh, semente, **k)


def camadas(*pintores):
    def p(face, fx, fy, fw, fh):
        for f in pintores:
            c = f(face, fx, fy, fw, fh)
            if c is not None:
                return c
        return None
    return p


def so_faces(faces, pintor):
    return lambda face, fx, fy, fw, fh: pintor(face, fx, fy, fw, fh) if face in faces else None


# ====================================================================== geometria

def montar():
    c = Criatura("retalhado", 160, 160)  # mesma densidade (16 px por bloco); a folha cresceu com os ganchos e o cutelo
    c.osso("raiz")

    # ---- pernas: joelho dobrado; a esquerda vira para fora (manca). O pé fica no chão nas duas.
    for lado, s, esp, yaw in (("dir", -1, False, 0), ("esq", 1, True, 14)):
        perna = c.osso(f"perna_{lado}", "raiz", (5.5 * s, 22, 0), (9, yaw * s, 0))
        xo = -9.5 if s < 0 else 1.5
        perna.cubo(f"coxa_{lado}", (xo, 10, -4.5), (8, 13, 9), patchwork(PELES, 1 if s < 0 else 2), espelho=esp,
                   partilha=None if not esp else "coxa_dir")
        canela = c.osso(f"canela_{lado}", f"perna_{lado}", (5.5 * s, 11, 0), (-9, 0, 0))
        xo2 = -9 if s < 0 else 2
        canela.cubo(f"canela_{lado}", (xo2, 2, -3.5), (7, 10, 7), camadas(
            so_faces(("north", "east", "west"), lambda f, x, y, w, h: costura_h(x, y, 3, w, passo=4)), mat(ROXA, 3)),
            espelho=esp, partilha=None if not esp else "canela_dir")
        canela.cubo(f"pe_{lado}", (xo - 0.5, 0, -8.5), (9, 3, 11), pe, espelho=esp, partilha=None if not esp else "pe_dir")

    # ---- tronco: pelve, peito, corcova grande, ombros desiguais, vértebras saltadas
    tronco = c.osso("tronco", "raiz", (0, 22, 0), (-14, 0, 0))
    tronco.cubo("pelve", (-8, 20, -4.5), (16, 7, 9), pelve)
    tronco.cubo("peito", (-11, 26, -6), (22, 14, 12), peito)
    tronco.cubo("corcova", (-9.5, 37, -3), (19, 9, 14), corcova)
    tronco.cubo("ombro_dir", (-19, 33, -6.5), (9, 12, 13), ombro)
    tronco.cubo("ombro_esq", (10, 31, -5.5), (7, 9, 11), ombro_esq)
    tronco.cubo("retalho_solto", (16.9, 24, -3), (0.5, 8, 6), retalho_solto)  # pele solta pendurada do ombro caído
    for i, (y, z, h) in enumerate(((45.5, -1, 3.2), (45.8, 3.5, 2.6), (45.6, 8, 2.0))):
        tronco.cubo(f"vertebra_{i}", (-1.1, y, z), (2.2, h, 2.2), vertebra)

    ferida = c.osso("ferida", "tronco", (0, 33, -6))
    ferida.cubo("cavidade", (-5, 29, -6.6), (10, 9, 1), cavidade)
    ferida.cubo("costela_dir", (-6.2, 29.5, -7.2), (1.2, 8, 1.2), mat(OSSO, 4, grad=False, manchas=0))
    ferida.cubo("costela_esq", (5, 29.5, -7.2), (1.2, 8, 1.2), mat(OSSO, 5, grad=False, manchas=0), partilha="costela_dir")
    coracao = c.osso("coracao", "ferida", (0, 33.5, -7))
    coracao.cubo("coracao", (-2.5, 30.5, -9.6), (5, 6, 3), coracao_pintor, brilho=coracao_brilho)

    # ---- cabeça afundada entre os ombros, torta; mandíbula caída
    cabeca = c.osso("cabeca", "tronco", (0, 38.5, -6), (-8, 0, 7))
    cabeca.cubo("cabeca", (-4.5, 35, -15.5), (9, 8, 9.5), cabeca_pintor)
    mandibula = c.osso("mandibula", "cabeca", (0, 35.5, -7), (9, 0, -3))  # caída e torta: boca sempre entreaberta
    mandibula.cubo("mandibula", (-3.5, 31.8, -15), (7, 3.5, 8), mandibula_pintor)

    # ---- braço direito (grosso): cutelo preso ao antebraço por tiras
    bd = c.osso("braco_dir", "tronco", (-15, 41, -1), (6, 0, -6))
    bd.cubo("braco_dir", (-19, 27, -5), (8, 14, 8), patchwork(PELES, 6))
    ad = c.osso("antebraco_dir", "braco_dir", (-15, 28, -1), (-6, 0, 0))
    ad.cubo("antebraco_dir", (-18.5, 12, -4.5), (7, 16, 7), camadas(
        so_faces(("north", "east", "west"), lambda f, x, y, w, h: costura_h(x, y, 9, w, passo=4)), mat(PALIDA, 7)))
    ad.cubo("mao_dir", (-19, 7, -5), (8, 5, 8), mao)
    ad.cubo("lamina", (-21.6, 2.5, -20), (2, 18, 16), lamina)
    ad.cubo("tira_a", (-21.6, 23, -4.7), (3.2, 2, 7.4), mat(COURO, 8, grad=False))
    ad.cubo("tira_b", (-21.6, 16, -4.7), (3.2, 2, 7.4), mat(COURO, 9, grad=False), partilha="tira_a")

    # ---- braço esquerdo (comprido, mais fino): ganchos em correntes
    be = c.osso("braco_esq", "tronco", (13.5, 39.5, -1), (12, 0, 7))
    be.cubo("braco_esq", (10, 28, -4.5), (7, 12, 7), patchwork(PELES, 10), espelho=True)
    ae = c.osso("antebraco_esq", "braco_esq", (13.5, 28.5, -1), (-6, 0, 0))
    ae.cubo("antebraco_esq", (10.5, 14, -4.5), (6, 15, 7), camadas(
        so_faces(("north", "east", "west"), lambda f, x, y, w, h: costura_h(x, y, 10, w, passo=4)), mat(CURTIDA, 11)))
    ae.cubo("mao_esq", (10, 9, -5), (7, 5, 8), mao, espelho=True)
    g = c.osso("ganchos", "antebraco_esq", (13.5, 9, -1))
    for i, (x, comp) in enumerate(((10.6, 3.5), (13.0, 4.5), (15.4, 2.8))):
        g.cubo(f"elo_{i}a", (x, 6.8, -1.6), (1.2, 2.2, 1.2), elo, partilha=None if i == 0 else "elo_0a")
        g.cubo(f"elo_{i}b", (x + 0.1, 4.8, -1.5), (1.0, 2.2, 1.0), elo, partilha=None if i == 0 else "elo_0b")
        g.cubo(f"haste_{i}", (x, 5.0 - comp, -1.6), (1.3, comp, 1.3), gancho, partilha=None if i == 0 else None)
        g.cubo(f"ponta_{i}", (x, 5.0 - comp, -6.5), (1.3, 1.3, 5), ponta_gancho, partilha=None if i == 0 else "ponta_0")
        g.cubo(f"bico_{i}", (x, 5.0 - comp, -6.5), (1.3, 3.2, 1.3), bico_gancho, partilha=None if i == 0 else "bico_0")
    return c


# ====================================================================== pintores

def pe(face, fx, fy, fw, fh):
    if face == "north":  # dedos: quatro unhas de osso embaixo
        if fy == fh - 1 and fx in (1, 3, 5, 7):
            return tom(OSSO, 1)
        if fx in (2, 4, 6):
            return tom(ROXA, 4)
    if face == "up" and fy > fh - 4 and fx in (2, 4, 6):
        return tom(ROXA, 4)
    return sombreado(PALIDA, face, fx, fy, fw, fh, 3, base=2)


def pelve(face, fx, fy, fw, fh):
    if face in ("north", "south", "east", "west") and fy <= 1:  # cinta de couro com rebites
        return tom(COURO, 1 if fy == 0 else 2) if not (fy == 1 and fx % 5 == 2) else tom(METAL, 2)
    return patchwork(PELES, 12)(face, fx, fy, fw, fh)


def peito(face, fx, fy, fw, fh):
    if face == "north":
        # a cavidade cobre o centro (fx 6..15, fy 3..11); a pele em volta é rasgada, não costurada
        if 6 <= fx <= 15 and 3 <= fy <= 11:
            return cor(CAVIDADE)
        if (5 <= fx <= 16 and 2 <= fy <= 12):
            return tom(SANGUE, 3) if hash01(fx, fy, 13) > 0.35 else tom(ROXA, 4)  # borda rasgada
        if (fx + fy) % 11 == 0 and fy > 2:  # veia funda
            return tom(ROXA, 4)
    return patchwork(PELES, 14)(face, fx, fy, fw, fh)


def corcova(face, fx, fy, fw, fh):
    if face == "up":
        if fx in (fw // 2 - 1, fw // 2):
            return tom(ROXA, 4) if fy % 3 != 2 else tom(SANGUE, 3)  # sulco da coluna, onde as vértebras saem
    return patchwork(PELES, 15)(face, fx, fy, fw, fh)


def ombro(face, fx, fy, fw, fh):
    if face == "up" and (fx + fy) % 4 == 0 and 1 <= fx < fw - 1:
        return tom(METAL, 2)  # pregos de ferro segurando a pele
    return patchwork(PELES, 16)(face, fx, fy, fw, fh)


def ombro_esq(face, fx, fy, fw, fh):
    if face == "up" and (fx + fy) % 5 == 0:
        return tom(METAL, 2)
    return patchwork(PELES, 17)(face, fx, fy, fw, fh)


def retalho_solto(face, fx, fy, fw, fh):
    if face in ("east", "west"):
        if fy == fh - 1 and fx % 2 == 0:
            return None  # barra rasgada
        if fy == 0:
            return tom(SANGUE, 3)
        return tom(PALIDA, 3 if (fx + fy) % 3 else 4)
    return tom(PALIDA, 4)


def vertebra(face, fx, fy, fw, fh):
    if face == "up":
        return tom(OSSO, 0)
    return tom(OSSO, 1 if fy < fh - 1 else 2) if hash01(fx, fy, 18) > 0.2 else tom(OSSO, 2)


def cavidade(face, fx, fy, fw, fh):
    if face != "north":
        return tom(SANGUE, 3)
    if fx in (0, fw - 1) or fy in (0, fh - 1):
        return tom(SANGUE, 2) if (fx + fy) % 2 == 0 else tom(SANGUE, 3)  # carne viva rasgada
    if fy in (2, 5) and (fx <= 1 or fx >= fw - 2):
        return tom(OSSO, 2)  # pontas de costela quebrada
    return cor(CAVIDADE) if hash01(fx, fy, 19) > 0.22 else tom(SANGUE, 3)


def coracao_pintor(face, fx, fy, fw, fh):
    if face == "north":
        if (fx, fy) in ((2, 1), (1, 2), (2, 2), (2, 3)):
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
    if face == "north" and 1 <= fx <= 3 and 1 <= fy <= 3:
        return tom(SANGUE, 0 if (fx, fy) in ((2, 1), (1, 2), (2, 2)) else 1)
    return None


def cabeca_pintor(face, fx, fy, fw, fh):
    if face == "north":
        # olhos costurados em X (o Sangue come os olhos primeiro): fenda escura, fio cruzado por cima; sem nariz,
        # só um rasgo; o beiço de cima some na sombra da boca aberta
        if fy == 2 and (1 <= fx <= 3 or 5 <= fx <= 7):
            return tom(SANGUE, 3) if fx in (2, 6) else cor(CAVIDADE)
        if (fy, fx) in ((1, 1), (3, 3), (1, 3), (3, 1), (1, 5), (3, 7), (1, 7), (3, 5)):
            return cor(FIO)
        if fy in (4, 5) and fx == 4:
            return tom(SANGUE, 3) if fy == 4 else cor(CAVIDADE)
        if fy == 0:
            return tom(ROXA, 3)
        if fy == fh - 1 and 1 <= fx <= 7:  # sombra da boca aberta embaixo
            return cor(CAVIDADE)
        if fy == fh - 2 and 2 <= fx <= 6:
            return tom(ROXA, 4)
        return sombreado(PALIDA, face, fx, fy, fw, fh, 20, base=1, manchas=0.1)
    if face == "up":
        c = costura_h(fx, fy, fh // 2, fw, passo=4) if fx > 1 else None
        if c:
            return c
    return patchwork([PALIDA, ROXA], 21, maximo=2)(face, fx, fy, fw, fh)


def mandibula_pintor(face, fx, fy, fw, fh):
    if face == "north":
        if fy == 0:
            return tom(OSSO, 1 if fx != 5 else 2) if fx in (0, 2, 5) else cor(CAVIDADE)  # dentes que sobraram
        if fy == 1:
            return cor(CAVIDADE) if fx % 3 else tom(SANGUE, 3)
    if face == "up":
        return cor(CAVIDADE) if hash01(fx, fy, 22) > 0.25 else tom(SANGUE, 3)
    return sombreado(ROXA, face, fx, fy, fw, fh, 22, base=2)


def mao(face, fx, fy, fw, fh):
    if face == "north" and fy <= 1 and fx % 2 == 0:
        return tom(ROXA, 4)  # nós dos dedos
    if face == "down" and fy == fh - 1 and fx % 2 == 1:
        return tom(OSSO, 2)
    return sombreado(CURTIDA, face, fx, fy, fw, fh, 23, base=1)


LASCAS = (2, 3, 7, 11, 12)  # colunas (contadas da frente) onde o gume está lascado


def lamina(face, fx, fy, fw, fh):
    if face == "north":      # gume da frente
        return cor(GUME) if hash01(fx, fy, 24) > 0.3 else tom(SANGUE, 3)
    if face == "down":       # gume de baixo, lascado (fy = profundidade a partir da frente)
        if fy in LASCAS:
            return None
        return cor(GUME) if hash01(fx, fy, 25) > 0.3 else tom(SANGUE, 2)
    if face in ("south", "up"):
        return tom(METAL, 3)
    # faces largas: dorso escuro em cima, chapa com riscos, gume claro embaixo, ferrugem e sangue seco
    prof = fx if face == "west" else fw - 1 - fx
    if fy == fh - 1 and prof in LASCAS:
        return None
    if fy <= 1:
        return tom(METAL, 4 if fy == 0 else 3)
    if (prof, fy) in ((11, 4), (12, 4), (11, 5), (12, 5)):
        return tom(METAL, 4)  # furo do cutelo
    r = hash01(prof, fy, 26)
    perto_gume = fy >= fh - 3 or prof <= 1
    if perto_gume and r < 0.45:
        return tom(SANGUE, 3) if r > 0.08 else tom(SANGUE, 2)
    if fy == fh - 1 or prof == 0:
        return tom(METAL, 0)
    if r < 0.06:
        return tom(FERRUGEM, 0 if r < 0.03 else 1)
    if (prof + fy) % 6 == 0:
        return tom(METAL, 1)  # risco de afiação
    return tom(METAL, 2 if fy < fh * 0.55 else 1)


def elo(face, fx, fy, fw, fh):
    return tom(METAL, 3 if fy in (0, fh - 1) else 2) if hash01(fx, fy, 27) > 0.3 else tom(FERRUGEM, 1)


def gancho(face, fx, fy, fw, fh):
    r = hash01(fx, fy, 28)
    if r < 0.3:
        return tom(FERRUGEM, 0 if r < 0.15 else 1)
    return tom(METAL, 2 if r < 0.8 else 3)


def ponta_gancho(face, fx, fy, fw, fh):
    if face == "down":
        return cor(GUME)
    return gancho(face, fx, fy, fw, fh)


def bico_gancho(face, fx, fy, fw, fh):
    if face == "up" or fy == 0:
        return cor(GUME)
    return tom(SANGUE, 3) if hash01(fx, fy, 29) < 0.3 else gancho(face, fx, fy, fw, fh)


# ====================================================================== animações
# Nomes parado/andar/investida já são usados por criaturas/Retalhado.java. Os outros ficam para o Dev ligar.

E = "easeInOutSine"
OUT = "easeOutQuad"
IN = "easeInQuad"


def lubdub(t):
    """Batida dupla do coração (lub-dub) começando em t."""
    return [(t, (1, 1, 1)), (t + 0.08, (1.32, 1.32, 1.32), OUT), (t + 0.17, (1.06, 1.06, 1.06), E),
            (t + 0.26, (1.2, 1.2, 1.2), OUT), (t + 0.46, (1, 1, 1), E)]


def anims():
    parado = animacao(4.4, True, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (2.2, (2.5, 0, 0.6), E), (4.4, (0, 0, 0), E)),
                   "position": canal((0, (0, 0, 0)), (2.2, (0, -0.7, 0), E), (4.4, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.9, (4, -16, 0), E), (1.1, (1, -21, 0), OUT), (1.4, (3, -18, 0), E),
                                     (2.5, (-3, 14, 0), E), (3.1, (6, 10, 0), E), (4.4, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (1.0, (6, 0, 0), E), (1.5, (2, 0, 0), E), (3.0, (7, 0, 0), E), (4.4, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (2.2, (-4, 0, -3), E), (4.4, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (2.2, (-3, 0, 3), E), (4.4, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (2.4, (-5, 0, 0), E), (4.4, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (4, 0, 3)), (1.4, (-4, 0, -3), E), (2.8, (4, 0, 2), E), (4.4, (4, 0, 3), E))},
        "coracao": {"scale": canal(*(lubdub(0.0) + lubdub(1.35) + lubdub(2.7) + [(4.0, (1, 1, 1)), (4.4, (1, 1, 1))]))},
    })

    # andar: 1,3 s por ciclo. O tronco CAI no contato de cada pé (easeOutQuad) e sobe devagar; os braços e os
    # ganchos chegam atrasados (sobreposição); a canela dobra só na fase de balanço.
    andar = animacao(1.3, True, {
        "perna_dir": {"rotation": canal((0, (32, 0, 0)), (0.65, (-26, 0, 0), E), (1.3, (32, 0, 0), E))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.65, (-4, 0, 0), E), (0.95, (-58, 0, 0), OUT), (1.3, (0, 0, 0), E))},
        "perna_esq": {"rotation": canal((0, (-26, 0, 0)), (0.65, (32, 0, 0), E), (1.3, (-26, 0, 0), E))},
        "canela_esq": {"rotation": canal((0, (-4, 0, 0)), (0.3, (-58, 0, 0), OUT), (0.65, (0, 0, 0), E), (1.3, (-4, 0, 0), E))},
        "tronco": {"rotation": canal((0, (-3, 6, 3)), (0.32, (0, 0, 0), E), (0.65, (-3, -6, -3), E), (0.97, (0, 0, 0), E), (1.3, (-3, 6, 3), E)),
                   "position": canal((0, (0, -1.8, 0)), (0.12, (0, -2.1, 0), OUT), (0.38, (0, 0.3, 0), E), (0.65, (0, -1.8, 0), IN),
                                     (0.77, (0, -2.1, 0), OUT), (1.03, (0, 0.3, 0), E), (1.3, (0, -1.8, 0), IN))},
        "cabeca": {"rotation": canal((0, (5, -5, -2)), (0.2, (-3, -2, 0), OUT), (0.65, (5, 5, 2), E), (0.85, (-3, 2, 0), OUT), (1.3, (5, -5, -2), E))},
        "mandibula": {"rotation": canal((0, (2, 0, 0)), (0.1, (7, 0, 0), OUT), (0.4, (2, 0, 0), E), (0.75, (7, 0, 0), OUT), (1.3, (2, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (-22, 0, -2)), (0.65, (24, 0, 2), E), (1.3, (-22, 0, -2), E))},
        "antebraco_dir": {"rotation": canal((0, (-4, 0, 0)), (0.18, (-12, 0, 0), E), (0.75, (6, 0, 0), E), (1.3, (-4, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (24, 0, 3)), (0.65, (-22, 0, -3), E), (1.3, (24, 0, 3), E))},
        "antebraco_esq": {"rotation": canal((0, (4, 0, 0)), (0.25, (-14, 0, 0), E), (0.8, (-2, 0, 0), E), (1.3, (4, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (18, 0, 0)), (0.2, (24, 0, 4), E), (0.85, (-20, 0, -4), E), (1.3, (18, 0, 0), E))},
        "coracao": {"scale": canal(*(lubdub(0.0) + [(0.65, (1, 1, 1))] + lubdub(0.66) + [(1.3, (1, 1, 1))]))},
    })

    # golpe de cutelo (1,1 s): 0,45 s de antecipação (o corpo inteiro arma: torce, recua, ergue), corte em 0,13 s
    # com o tronco indo junto, passa do ponto (overshoot) e volta devagar.
    ataque = animacao(1.1, False, {
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.45, (-135, 0, -28), "easeOutCubic"), (0.58, (72, 0, 6), "easeInCubic"),
                                        (0.7, (84, 0, 10), OUT), (1.1, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.45, (-32, 0, 0), "easeOutCubic"), (0.58, (14, 0, 0), "easeInCubic"),
                                            (0.7, (18, 0, 0), OUT), (1.1, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.45, (32, 0, 14), "easeOutCubic"), (0.6, (-28, 0, 6), "easeInCubic"), (1.1, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.45, (8, 24, -4), "easeOutCubic"), (0.58, (-26, -16, 2), "easeInCubic"),
                                     (0.7, (-30, -19, 2), OUT), (1.1, (0, 0, 0), E)),
                   "position": canal((0, (0, 0, 0)), (0.45, (0, 0.6, 1.4), "easeOutCubic"), (0.58, (0, -1.5, -2.5), "easeInCubic"),
                                     (0.7, (0, -1.8, -2.8), OUT), (1.1, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.45, (16, -16, 0), "easeOutCubic"), (0.6, (-8, 10, 0), "easeInCubic"), (1.1, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.45, (10, 0, 0), E), (0.6, (2, 0, 0), IN), (0.7, (12, 0, 0), OUT), (1.1, (0, 0, 0), E))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (0.45, (-8, 0, 0), E), (0.6, (10, 0, 0), IN), (1.1, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.5, (-44, 0, 0), E), (0.8, (48, 0, 0), E), (1.1, (0, 0, 0), E))},
    })

    # Investida de Ganchos (1,4 s): agacha e recua o braço (0,5 s), lança (os ganchos passam do ponto), crava e
    # segura com o corpo para trás, puxa.
    investida = animacao(1.4, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.5, (0, -2.2, 1.2), "easeOutCubic"), (0.68, (0, 0, -3.2), IN), (1.0, (0, 0, -3.2)),
                                   (1.25, (0, -1, 2), "easeOutCubic"), (1.4, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.5, (-78, 0, 18), "easeOutCubic"), (0.68, (102, 0, 0), "easeInCubic"),
                                        (1.0, (92, 0, 0), E), (1.25, (-36, 0, 12), "easeOutCubic"), (1.4, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.5, (-70, 0, 0), "easeOutCubic"), (0.68, (8, 0, 0), "easeInCubic"),
                                            (1.25, (-24, 0, 0), E), (1.4, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.5, (-40, 0, 0), E), (0.78, (82, 0, 0), "easeOutBack"),
                                      (1.0, (72, 0, 0), E), (1.25, (-22, 0, 0), E), (1.4, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.5, (38, 0, -12), "easeOutCubic"), (0.68, (-24, 0, -6), IN), (1.4, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.5, (12, -20, 0), "easeOutCubic"), (0.68, (-26, 12, 0), "easeInCubic"),
                                     (1.0, (-22, 10, 0), E), (1.25, (16, -8, 0), "easeOutCubic"), (1.4, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.5, (12, 12, 0), E), (0.68, (-8, -8, 0), E), (1.4, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.5, (4, 0, 0), E), (0.68, (16, 0, 0), OUT), (1.0, (14, 0, 0)), (1.4, (0, 0, 0), E))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (0.5, (14, 0, 0), E), (0.68, (-10, 0, 0), IN), (1.4, (0, 0, 0), E))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (0.5, (-10, 0, 0), E), (0.68, (14, 0, 0), IN), (1.4, (0, 0, 0), E))},
    })

    dano = animacao(0.5, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.1, (0, 0, 1.6), OUT), (0.5, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.1, (14, 0, -5), OUT), (0.5, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.08, (24, 8, 0), OUT), (0.5, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.1, (14, 0, 0), OUT), (0.5, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.1, (-18, 0, -28), OUT), (0.5, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.1, (-18, 0, 28), OUT), (0.5, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.2, (30, 0, 0), OUT), (0.5, (0, 0, 0), E))},
        "coracao": {"scale": canal((0, (1, 1, 1)), (0.08, (1.5, 1.5, 1.5), OUT), (0.35, (1, 1, 1), E))},
    })

    # morte própria (2,6 s): tranco, cambaleia, o joelho ESQUERDO cede primeiro, depois o direito, cai de cara
    # sobre o cutelo com um quique, os ganchos ainda balançam, o coração acelera e para.
    morrer = animacao(2.6, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.35, (0, 0, 0)), (0.9, (0, -5, 1.2), IN), (1.5, (0, -11, 0.5), "easeInCubic"),
                                   (1.85, (0, -15.5, -1), IN), (1.97, (0, -14, -1), OUT), (2.15, (0, -15.5, -1), E), (2.6, (0, -15.5, -1)))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (0.35, (-6, 0, 0), E), (0.9, (56, 0, 12), IN), (1.5, (66, 0, 12), E), (2.6, (68, 0, 12)))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.35, (0, 0, 0)), (0.9, (-108, 0, 0), IN), (1.5, (-118, 0, 0), E), (2.6, (-118, 0, 0)))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (0.9, (-10, 0, 0), E), (1.5, (62, 0, -10), IN), (1.85, (70, 0, -10), E), (2.6, (70, 0, -10)))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.9, (0, 0, 0)), (1.5, (-116, 0, 0), IN), (1.85, (-122, 0, 0), E), (2.6, (-122, 0, 0)))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.35, (16, 0, 4), OUT), (0.9, (-4, 6, 9), E), (1.5, (-14, 4, 6), E),
                                     (1.85, (-82, 0, 6), IN), (1.97, (-74, 0, 6), OUT), (2.15, (-78, 0, 6), E), (2.6, (-78, 0, 6)))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.35, (30, 0, 0), OUT), (0.9, (12, -10, 0), E), (1.5, (38, 0, 0), E),
                                     (1.85, (-6, 0, 10), IN), (1.97, (-24, 0, 14), OUT), (2.3, (-20, 0, 14), E), (2.6, (-20, 0, 14)))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.35, (18, 0, 0), OUT), (1.5, (10, 0, 0), E), (1.97, (22, 0, 0), OUT), (2.6, (22, 0, 0)))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.35, (-22, 0, -38), OUT), (0.9, (12, 0, -16), E), (1.5, (30, 0, -14), E),
                                        (1.85, (78, 0, -12), E), (2.6, (82, 0, -12), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.35, (-22, 0, 38), OUT), (0.9, (-8, 0, 26), E), (1.5, (40, 0, 14), E),
                                        (1.85, (86, 0, 12), E), (2.6, (92, 0, 14), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.9, (-20, 0, 0), E), (1.85, (-34, 0, 0), E), (2.6, (-10, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.5, (32, 0, 0), E), (1.85, (-64, 0, 0), E), (2.05, (-38, 0, 0), OUT),
                                      (2.25, (-52, 0, 0), E), (2.6, (-46, 0, 0), E))},
        "coracao": {"scale": canal((0, (1, 1, 1)), (0.1, (1.4, 1.4, 1.4), OUT), (0.26, (1, 1, 1), E), (0.4, (1.4, 1.4, 1.4), OUT),
                                   (0.56, (1, 1, 1), E), (0.7, (1.35, 1.35, 1.35), OUT), (0.86, (1, 1, 1), E), (1.3, (1.25, 1.25, 1.25), OUT),
                                   (1.55, (1, 1, 1), E), (2.05, (1.15, 1.15, 1.15), OUT), (2.3, (1, 1, 1), E), (2.6, (0.62, 0.62, 0.62), "easeInCubic"))},
    })

    # presença: afia o cutelo nos próprios ganchos (duas passadas), levanta a cabeça cega, fareja em sacudidas
    afiar = animacao(3.6, False, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.6, (-20, 0, 0), E), (0.9, (-20, -6, 0), E), (1.2, (-20, 4, 0), E), (1.5, (-20, -5, 0), E),
                                     (2.1, (-20, 4, 0), E), (2.7, (4, 0, 0), E), (3.6, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (60, 0, -14), E), (2.6, (60, 0, -14)), (3.3, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (-38, 0, 0), E), (2.6, (-38, 0, 0)), (3.3, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (40, 0, 26), E), (1.2, (76, 0, 20), E), (1.5, (38, 0, 26), E), (2.1, (78, 0, 20), E),
                                        (2.6, (42, 0, 24), E), (3.3, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (-24, 0, 0), E), (1.2, (14, 0, 0), E), (1.5, (-24, 0, 0), E), (2.1, (14, 0, 0), E), (3.3, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.6, (-8, 0, 0), E), (0.9, (-10, 0, 0)), (1.2, (26, 0, 6), "easeOutBack"), (1.5, (-8, 0, -4), E),
                                      (2.1, (28, 0, 6), "easeOutBack"), (2.6, (0, 0, 0), E), (3.6, (0, 0, 0)))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.6, (-10, 0, 0), E), (2.6, (-6, 0, 0), E), (2.85, (20, -24, 0), E), (2.95, (16, -26, 0), OUT),
                                     (3.05, (20, -22, 0), E), (3.25, (24, 22, 0), E), (3.6, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (2.6, (0, 0, 0)), (2.9, (12, 0, 0), E), (3.3, (4, 0, 0), E), (3.6, (0, 0, 0), E))},
        "coracao": {"scale": canal((0, (1, 1, 1)), (2.7, (1, 1, 1)), (2.8, (1.3, 1.3, 1.3), OUT), (3.0, (1, 1, 1), E))},
    })
    return {"parado": parado, "andar": andar, "ataque": ataque, "investida": investida, "dano": dano, "morrer": morrer, "afiar": afiar}
