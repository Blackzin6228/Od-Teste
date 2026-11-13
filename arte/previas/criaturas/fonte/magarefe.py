"""Magarefe, versão 5.2 (revisão crítica da 5.1). Casa do Açougueiro, Ato 1: alto, ombros largos, avental, cutelo,
cabeça num saco. Anda devagar e não morre no Ato 1 (lore/roteiro.md, docs/decisoes/2026-11-09-nomes-acougueiro-magarefe.md).
Ainda não existe no jogo: só prévia.

O que mudou da 5.1 (ver arte/previas/criaturas/comparacao/LEIA.md):
- Assimetria: o ombro do cutelo cai (o peso puxa), o tronco pende um pouco para esse lado, a cabeça no saco
  fica torta e caída; o saco tem duas pontas soltas amarradas em cima (lê na silhueta).
- Cutelo maior (lê a 30 blocos); dois ganchos de carne pendurados no cinto, atrás; abas do avental em osso
  próprio (balançam ao andar).
- Camisa e avental com mais contraste (dobras, desgaste); escorrido do sangue mais escuro e mais fundo.
- Animações: parado com a cabeça que pende e um estalo seco de vez em quando; andar arrastado com o braço do
  cutelo atrasado; golpe com passo à frente, 0,7 s de antecipação, descida de uma vez e o cutelo que fica
  embaixo antes de voltar; escutar com o saco subindo em sacudidas.
"""
from montagem import Criatura, animacao, canal, cor, hash01, sombreado, tom

SANGUE = ["#E0313A", "#B3131B", "#7A0A10", "#3A0306"]
PELE = ["#978679", "#7f6f64", "#685a51", "#514640", "#3a322d"]
ESTOPA = ["#817359", "#6c604a", "#584d3c", "#443b2f", "#302a22"]
COURO = ["#5a4333", "#49362a", "#38291f", "#281d16", "#19120e"]
PANO = ["#46403f", "#363031", "#282425", "#1c1919", "#121010"]
METAL = ["#9a9a93", "#767670", "#55554f", "#393936", "#232321"]
FERRUGEM = ["#6b4327", "#4e301c"]
OSSO = ["#e0d6ba", "#bfb294", "#978b6e", "#6b614a"]
FIO = "#15100f"
GUME = "#c9c9c0"


def mat(paleta, semente, **k):
    return lambda face, fx, fy, fw, fh: sombreado(paleta, face, fx, fy, fw, fh, semente, **k)


def montar():
    c = Criatura("magarefe", 128, 128)
    c.osso("raiz")
    # pernas longas, botas pesadas
    for lado, s, esp in (("dir", -1, False), ("esq", 1, True)):
        p = c.osso(f"perna_{lado}", "raiz", (3.5 * s, 26, 0))
        xo = -6.5 if s < 0 else 0.5
        p.cubo(f"coxa_{lado}", (xo, 13, -3), (6, 14, 6), calca, espelho=esp, partilha=None if not esp else "coxa_dir")
        k = c.osso(f"canela_{lado}", f"perna_{lado}", (3.5 * s, 13, 0))
        k.cubo(f"canela_{lado}", (xo + 0.5, 4, -2.5), (5, 10, 5), mat(PANO, 2, base=2), espelho=esp, partilha=None if not esp else "canela_dir")
        k.cubo(f"bota_{lado}", (xo, 0, -4.5), (6, 4, 8), bota, espelho=esp, partilha=None if not esp else "bota_dir")
    # tronco pende para o lado do cutelo (direito)
    tronco = c.osso("tronco", "raiz", (0, 26, 0), (-6, 0, 3))
    tronco.cubo("abdomen", (-6, 25, -3.5), (12, 11, 7), camisa)
    tronco.cubo("peito", (-8, 36, -4.5), (16, 12, 9), camisa)
    tronco.cubo("ombro_dir", (-14, 40.5, -4), (7, 6, 8), ombro)  # caído: o cutelo pesa
    tronco.cubo("ombro_esq", (7, 42.5, -4), (7, 6, 8), ombro, espelho=True, partilha="ombro_dir")
    tronco.cubo("cinto", (-6.5, 33, -4), (13, 3, 8), cinto, inflar=0.2)
    tronco.cubo("peitilho", (-5, 38, -5.2), (10, 10, 0.7), avental)
    tronco.cubo("avental", (-7, 14, -4.6), (14, 24, 0.7), avental)
    aba = c.osso("aba", "tronco", (0, 14, -4.3))
    aba.cubo("aba_dir", (-7, 5, -4.6), (7, 9.2, 0.7), aba_pintor)
    aba.cubo("aba_esq", (0.2, 7, -4.6), (6.8, 7.2, 0.7), aba_pintor2)
    sino = c.osso("sino", "tronco", (-7, 33, 0))
    sino.cubo("sino_alca", (-7.5, 31, -0.5), (1, 2, 1), mat(METAL, 3, grad=False))
    sino.cubo("sino_corpo", (-8.5, 27.5, -1.5), (3, 3.5, 3), sino_pintor)
    # ganchos de carne pendurados no cinto, atrás
    ganchos = c.osso("ganchos", "tronco", (3, 33, 4))
    for i, x in enumerate((1.5, 4.5)):
        ganchos.cubo(f"gancho_{i}", (x, 27 - i, 3.8), (1, 6 + i, 1), gancho, partilha=None if i == 0 else None)
        ganchos.cubo(f"bico_{i}", (x, 27 - i, 1.3), (1, 1, 3.5), gancho_bico, partilha=None if i == 0 else "bico_0")
    # pescoço e cabeça no saco, caída para a frente e torta
    pescoco = c.osso("pescoco", "tronco", (0, 48, -1))
    pescoco.cubo("pescoco", (-2, 46, -3), (4, 4, 4), mat(PELE, 3))
    cabeca = c.osso("cabeca", "pescoco", (0, 50, -1), (-32, 0, 12))
    cabeca.cubo("saco", (-4, 49, -5.5), (8, 10, 8), saco)
    cabeca.cubo("no_corda", (-1.5, 48, -6.5), (3, 2, 2), mat(COURO, 4, grad=False))
    cabeca.cubo("orelha_a", (-3.5, 58.5, -3.5), (2.2, 3.5, 2.2), orelha, rotacao=(0, 0, -22), pivo=(-2.4, 59, -2.4))
    cabeca.cubo("orelha_b", (1.0, 58.5, -4.2), (2.2, 2.6, 2.2), orelha, rotacao=(14, 0, 18), pivo=(2.1, 59, -3.1), partilha=None)
    # braços compridos; o direito mais caído
    for lado, s, esp, queda in (("dir", -1, False, 2), ("esq", 1, True, 0)):
        b = c.osso(f"braco_{lado}", "tronco", (11 * s, 46 - queda, 0), (4, 0, -4 * s))
        xo = -13.5 if s < 0 else 8.5
        b.cubo(f"braco_{lado}", (xo, 32 - queda, -2.5), (5, 14, 5), mat(PELE, 5), espelho=esp, partilha=None if not esp else "braco_dir")
        a = c.osso(f"antebraco_{lado}", f"braco_{lado}", (11 * s, 33 - queda, 0), (-5, 0, 0))
        a.cubo(f"antebraco_{lado}", (xo + 0.5, 19 - queda, -2), (4, 14, 4), antebraco, espelho=esp, partilha=None if not esp else "antebraco_dir")
        a.cubo(f"mao_{lado}", (xo, 13 - queda, -2.5), (5, 6, 5), mat(PELE, 6, base=1), espelho=esp, partilha=None if not esp else "mao_dir")
    ad = c.por_nome["antebraco_dir"]
    ad.cubo("cabo", (-12, 8, -1), (2, 7, 2), mat(COURO, 7, grad=False))
    ad.cubo("lamina", (-11.75, 0.5, -10.5), (1.5, 12, 11.5), lamina)
    ae = c.por_nome["antebraco_esq"]
    for i, x in enumerate((8.6, 10.3, 12.0)):
        ae.cubo(f"dedo_{i}", (x, 7.5 - (i % 2), -1), (1, 5.5, 1), mat(PELE, 8, base=2, grad=False), partilha=None if i == 0 else "dedo_0")
    return c


# ---- pintores

def camisa(face, fx, fy, fw, fh):
    if face in ("north", "south", "east", "west"):
        if fx % 5 == 2 and hash01(fx, fy, 10) > 0.45:
            return tom(PANO, 0)  # fio puído
        if (fx + fy // 2) % 7 == 0:
            return tom(PANO, 3)  # dobra funda
    return sombreado(PANO, face, fx, fy, fw, fh, 11, base=1)


def calca(face, fx, fy, fw, fh):
    if face in ("north", "east", "west") and fy % 6 == 3 and fx % 2 == 0:
        return tom(PANO, 3)
    return sombreado(PANO, face, fx, fy, fw, fh, 1, base=1)


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


def _escorrido(fx, fy, fw, fh, semente, fundo):
    r = hash01(fx, fy, semente)
    coluna = hash01(fx, 0, semente + 1)
    comprimento = int(coluna * fh * 0.6) + fundo
    escorrido = fy < comprimento and (coluna > 0.42 or fy < 2)
    if escorrido and r > 0.1:
        return tom(SANGUE, 3) if (r > 0.25 or fy > 2) else tom(SANGUE, 2)
    return None


def avental(face, fx, fy, fw, fh):
    if face != "north":
        return tom(COURO, 3) if face in ("east", "west", "up", "down") else sombreado(COURO, face, fx, fy, fw, fh, 14, base=2)
    s = _escorrido(fx, fy, fw, fh, 15, 7 if fh > 12 else 2)
    if s:
        return s
    if fx == 0 or fx == fw - 1:
        return tom(COURO, 4)
    if fx % 5 == 3:
        return tom(COURO, 2 if fy % 9 else 3)  # dobra
    if hash01(fx // 2, fy // 2, 16) < 0.12:
        return tom(COURO, 0)  # couro gasto, mais claro
    return sombreado(COURO, face, fx, fy, fw, fh, 17, base=0 if fy < 3 else 1, pintas=(0.05, 0.97))


def _aba(face, fx, fy, fw, fh, semente):
    if face != "north":
        return tom(COURO, 3)
    if fy == fh - 1 and fx % 3 != 1:
        return None  # barra rasgada (transparente)
    if fy == fh - 2 and fx % 5 == 0:
        return None
    s = _escorrido(fx, fy + 20, fw, fh + 20, 15, 7)
    if s and fy < 3:
        return s
    if fx == 0 or fx == fw - 1:
        return tom(COURO, 4)
    if fx % 5 == 3:
        return tom(COURO, 2)
    return sombreado(COURO, face, fx, fy, fw, fh, semente, base=1, pintas=(0.05, 0.97))


def aba_pintor(face, fx, fy, fw, fh):
    return _aba(face, fx, fy, fw, fh, 18)


def aba_pintor2(face, fx, fy, fw, fh):
    return _aba(face, fx, fy, fw, fh, 19)


def sino_pintor(face, fx, fy, fw, fh):
    if face == "down":
        return cor("#0e0a0a") if (fx, fy) == (1, 1) else tom(METAL, 4)
    if fy == fh - 1:
        return tom(METAL, 1)
    return tom(METAL, 3 if fx == fw - 1 else 2)


def gancho(face, fx, fy, fw, fh):
    r = hash01(fx, fy, 20)
    return tom(FERRUGEM, 0 if r < 0.15 else 1) if r < 0.35 else tom(METAL, 2 if r < 0.8 else 3)


def gancho_bico(face, fx, fy, fw, fh):
    if face == "north":
        return cor(GUME)
    return tom(SANGUE, 3) if hash01(fx, fy, 21) < 0.35 else gancho(face, fx, fy, fw, fh)


def saco(face, fx, fy, fw, fh):
    # estopa com trama; sem rosto. Na frente, duas manchas úmidas onde seriam os olhos e um vinco da boca.
    if face == "north":
        if fy in (4, 5) and fx in (2, 5):
            return tom(SANGUE, 3)
        if fy == 6 and fx in (2, 5):
            return tom(ESTOPA, 4)  # a mancha escorreu
        if fy == 7 and 2 <= fx <= 5:
            return tom(ESTOPA, 3)  # vinco onde a boca respira por baixo
    if face == "up" and (fx + fy) % 2 == 0:
        return tom(ESTOPA, 2)
    base = 1 if (fx + fy) % 2 == 0 else 2
    if fx % 4 == 0 or fy % 4 == 0:
        base += 1  # trama grossa
    if face in ("east", "west") and fy == 1:
        return tom(COURO, 2)  # a corda que amarra
    return sombreado(ESTOPA, face, fx, fy, fw, fh, 22, base=base, grad=False)


def orelha(face, fx, fy, fw, fh):
    if fy == fh - 1 and face != "up":
        return tom(COURO, 2)  # a corda
    return tom(ESTOPA, 1 if (fx + fy) % 2 else 2)


def antebraco(face, fx, fy, fw, fh):
    if face in ("north", "east", "west") and fy % 5 == 2 and fx % 3 == 0:
        return tom(PELE, 4)  # cortes antigos nos braços
    if fy >= fh - 2 and hash01(fx, fy, 23) > 0.55:
        return tom(SANGUE, 3)  # as mãos ficaram sujas
    return sombreado(PELE, face, fx, fy, fw, fh, 24, base=1)


def bota(face, fx, fy, fw, fh):
    if face in ("north", "east", "west", "south") and fy == 0:
        return tom(COURO, 1)
    if face == "north" and fy == fh - 1:
        return tom(METAL, 3)  # biqueira
    return sombreado(COURO, face, fx, fy, fw, fh, 25, base=2, grad=False)


def lamina(face, fx, fy, fw, fh):
    if face in ("north", "down"):
        return cor(GUME) if hash01(fx, fy, 26) > 0.25 else tom(SANGUE, 2)
    if face in ("south", "up"):
        return tom(METAL, 3)
    if fy <= 0:
        return tom(METAL, 4)
    r = hash01(fx, fy, 27)
    if (fy >= fh - 2 or fx <= 1) and r < 0.35:
        return tom(SANGUE, 3)
    if fy == fh - 1 or fx == 0:
        return tom(METAL, 0)
    if r < 0.05:
        return tom(FERRUGEM, 1)
    if (fx + fy) % 5 == 0:
        return tom(METAL, 1)
    return tom(METAL, 2 if fy < fh * 0.5 else 1)


# ---- animações (devagar: é uma presença, não um perseguidor rápido)

E = "easeInOutSine"
OUT = "easeOutQuad"
IN = "easeInQuad"


def anims():
    parado = animacao(5.0, True, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (2.5, (2, 0, 1), E), (5.0, (0, 0, 0), E))},
        # a cabeça pende de um lado para o outro e, uma vez, dá um estalo seco
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (1.5, (3, -10, -8), E), (3.4, (2, 12, 4), E), (3.55, (-4, 16, 6), OUT),
                                     (3.8, (1, 12, 4), E), (5.0, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (2.5, (-3, 0, -1), E), (5.0, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (2.5, (-3, 0, 1), E), (5.0, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (3.8, (0, 0, 0)), (4.0, (7, 0, 0), E), (4.15, (0, 0, 0), IN), (5.0, (0, 0, 0)))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (2.5, (2, 0, 3), E), (4.15, (0, 0, 0), E), (4.3, (6, 0, 2), OUT), (5.0, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (2.5, (3, 0, 2), E), (5.0, (0, 0, 0), E))},
        "aba": {"rotation": canal((0, (0, 0, 0)), (2.5, (-1.5, 0, 0), E), (5.0, (0, 0, 0), E))},
    })
    # andar arrastado (1,8 s): o pé esquerdo quase não sai do chão, o braço do cutelo chega atrasado, o tronco
    # afunda no apoio, abas e ganchos balançam depois.
    andar = animacao(1.8, True, {
        "perna_dir": {"rotation": canal((0, (24, 0, 0)), (0.9, (-18, 0, 0), E), (1.8, (24, 0, 0), E))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.9, (0, 0, 0)), (1.3, (-34, 0, 0), OUT), (1.8, (0, 0, 0), IN))},
        "perna_esq": {"rotation": canal((0, (-16, 0, 0)), (0.9, (18, 0, 0), E), (1.8, (-16, 0, 0), E))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.4, (-14, 0, 0), OUT), (0.9, (0, 0, 0), IN), (1.8, (0, 0, 0)))},
        "tronco": {"rotation": canal((0, (-2, 4, 2)), (0.9, (-2, -4, -1), E), (1.8, (-2, 4, 2), E)),
                   "position": canal((0, (0, -1.2, 0)), (0.12, (0, -1.4, 0), OUT), (0.5, (0, 0, 0), E), (0.9, (0, -0.8, 0), IN),
                                     (1.02, (0, -1.0, 0), OUT), (1.4, (0, 0, 0), E), (1.8, (0, -1.2, 0), IN))},
        "cabeca": {"rotation": canal((0, (2, -4, 0)), (0.15, (-2, -3, 0), OUT), (0.9, (2, 4, 0), E), (1.05, (-2, 3, 0), OUT), (1.8, (2, -4, 0), E))},
        "braco_dir": {"rotation": canal((0, (-6, 0, 0)), (0.2, (-9, 0, 0), E), (0.9, (8, 0, 0), E), (1.1, (11, 0, 0), E), (1.8, (-6, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.3, (-5, 0, 0), E), (1.2, (4, 0, 0), E), (1.8, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (14, 0, 0)), (0.9, (-16, 0, 0), E), (1.8, (14, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.3, (-10, 0, 0), E), (1.1, (-2, 0, 0), E), (1.8, (0, 0, 0), E))},
        "sino": {"rotation": canal((0, (10, 0, 0)), (0.2, (13, 0, 0), E), (0.9, (-10, 0, 0), E), (1.1, (-13, 0, 0), E), (1.8, (10, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (-8, 0, 0)), (0.25, (-11, 0, 2), E), (0.9, (8, 0, 0), E), (1.15, (11, 0, -2), E), (1.8, (-8, 0, 0), E))},
        "aba": {"rotation": canal((0, (6, 0, 0)), (0.3, (9, 0, 0), E), (0.9, (-4, 0, 0), E), (1.2, (-7, 0, 0), E), (1.8, (6, 0, 0), E))},
    })
    # golpe (1,5 s): dá um passo à frente e ergue o cutelo com as duas mãos por 0,7 s (antecipação), desce de uma
    # vez, o cutelo fica cravado embaixo um instante (continuação) e volta devagar.
    ataque = animacao(1.5, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.3, (0, 0, 0.8), E), (0.7, (0, 0.6, 0.8), E), (0.84, (0, -1.2, -2.6), "easeInCubic"),
                                   (1.05, (0, -1.4, -2.6), E), (1.5, (0, 0, 0), E))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (0.3, (-10, 0, 0), E), (0.84, (22, 0, 0), IN), (1.05, (20, 0, 0)), (1.5, (0, 0, 0), E))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (0.3, (6, 0, 0), E), (0.84, (-18, 0, 0), IN), (1.05, (-16, 0, 0)), (1.5, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.7, (-155, 0, -14), "easeOutCubic"), (0.84, (48, 0, 0), "easeInCubic"),
                                        (0.96, (64, 0, 2), OUT), (1.1, (60, 0, 2), E), (1.5, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.7, (-44, 0, 0), "easeOutCubic"), (0.84, (12, 0, 0), "easeInCubic"), (0.96, (18, 0, 0), OUT), (1.5, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.7, (-125, 0, 24), "easeOutCubic"), (0.84, (38, 0, 10), "easeInCubic"), (0.96, (50, 0, 10), OUT), (1.5, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.7, (-54, 0, 0), "easeOutCubic"), (0.84, (0, 0, 0), "easeInCubic"), (1.5, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.7, (14, -6, 0), "easeOutCubic"), (0.86, (-34, 4, 0), "easeInCubic"), (0.98, (-38, 4, 0), OUT),
                                     (1.1, (-30, 2, 0), E), (1.5, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.7, (28, 0, 0), "easeOutCubic"), (0.86, (-8, 0, 0), "easeInCubic"), (1.0, (6, 0, -6), OUT), (1.5, (0, 0, 0), E))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (0.7, (-16, 0, 0), E), (0.95, (38, 0, 0), "easeOutBack"), (1.2, (-10, 0, 0), E), (1.5, (0, 0, 0), E))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (0.7, (-14, 0, 0), E), (0.95, (30, 0, 0), "easeOutBack"), (1.5, (0, 0, 0), E))},
        "aba": {"rotation": canal((0, (0, 0, 0)), (0.7, (-8, 0, 0), E), (0.95, (26, 0, 0), "easeOutBack"), (1.5, (0, 0, 0), E))},
    })
    dano = animacao(0.4, False, {  # quase não sente
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.1, (4, 0, 3), OUT), (0.4, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.12, (16, 0, -6), OUT), (0.4, (0, 0, 0), E))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (0.15, (8, 0, 0), OUT), (0.4, (0, 0, 0), E))},
    })
    morrer = animacao(2.8, False, {  # reservada para depois do Ato 1: ajoelha, larga o cutelo, tomba de lado
        "raiz": {"position": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (1.2, (0, -12, 0), "easeInCubic"), (1.32, (0, -11, 0), OUT),
                                   (2.0, (0, -17, 0), IN), (2.1, (0, -16.4, 0), OUT), (2.8, (0, -17, 0), E))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (1.2, (70, 0, -6), IN), (2.8, (70, 0, -6)))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (1.2, (-125, 0, 0), IN), (2.8, (-125, 0, 0)))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (1.2, (66, 0, 6), IN), (2.8, (66, 0, 6)))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (1.2, (-120, 0, 0), IN), (2.8, (-120, 0, 0)))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.5, (-10, 0, 0), E), (1.2, (-18, 0, 0), E), (2.0, (-20, 0, -80), IN), (2.15, (-20, 0, -74), OUT), (2.8, (-20, 0, -76), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.5, (-12, 0, 0), E), (1.2, (-14, 0, 0), E), (2.1, (-6, 0, 18), OUT), (2.8, (-4, 0, 14), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.5, (10, 0, -10), E), (1.2, (35, 0, -20), E), (2.8, (40, 0, -30), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (1.2, (-40, 0, 0), E), (2.8, (-20, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (1.2, (30, 0, 20), E), (2.8, (80, 0, 30), E))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (1.2, (20, 0, 0), E), (1.5, (-25, 0, 0), E), (2.0, (40, 0, 0), E), (2.8, (0, 0, 0), "easeOutBounce"))},
        "ganchos": {"rotation": canal((0, (0, 0, 0)), (1.2, (-20, 0, 0), E), (2.0, (30, 0, 0), E), (2.8, (0, 0, 0), "easeOutBounce"))},
    })
    # presença: para, ergue o saco em sacudidas e "escuta" (vira a cabeça), depois limpa o cutelo no avental
    escutar = animacao(4.6, False, {
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.5, (14, 0, -4), E), (0.62, (12, 0, -4), OUT), (0.95, (28, 0, 0), E), (1.05, (26, 0, 0), OUT),
                                     (1.5, (30, -35, 0), E), (2.4, (30, 35, 0), E), (3.0, (24, 0, 0), E), (3.6, (-4, 0, 8), E), (4.6, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.95, (4, 0, 0), E), (3.0, (4, 0, 0)), (3.6, (-6, 8, 0), E), (4.1, (-6, -4, 0), E), (4.6, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (3.0, (0, 0, 0)), (3.6, (34, 0, -30), E), (3.9, (26, 0, -22), E), (4.15, (36, 0, -30), E), (4.6, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (3.0, (0, 0, 0)), (3.6, (-45, 0, 0), E), (3.9, (-30, 0, 0), E), (4.15, (-45, 0, 0), E), (4.6, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.8, (-4, 0, 8), E), (3.0, (-4, 0, 8)), (4.6, (0, 0, 0), E))},
        "sino": {"rotation": canal((0, (0, 0, 0)), (3.6, (0, 0, 0)), (3.8, (12, 0, 4), E), (4.1, (-8, 0, -3), E), (4.6, (0, 0, 0), E))},
        "aba": {"rotation": canal((0, (0, 0, 0)), (3.6, (0, 0, 0)), (3.9, (10, 0, 0), E), (4.15, (-4, 0, 0), E), (4.6, (0, 0, 0), E))},
    })
    return {"parado": parado, "andar": andar, "ataque": ataque, "dano": dano, "morrer": morrer, "escutar": escutar}
