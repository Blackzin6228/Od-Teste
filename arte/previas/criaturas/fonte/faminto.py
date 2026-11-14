"""Faminto (Vila Calada, Sangue): dois conceitos para o Nikolas escolher, mesmo esqueleto e mesmas animações.

Base: lore/fichas-estruturas.md (aldeão curvado, pele esticada, boca aberta demais, mãos longas; sem olhos, o Sangue
come os olhos primeiro), proposta A do enigma "O Dobre" (lore/enigma-vila-calada-v2.md, branch mesa-lore/horror-rpg):
os Famintos param e viram a cabeça para a torre a cada toque, sobem a rua se o toque for rápido e, no último toque,
se ajoelham virados para a torre e viram cinza.

Conceito A, "O Que Sobrou": ainda lê como aldeão (nariz murcho, cabeça alta, manto queimado em tiras), corcunda,
mandíbula solta pendurada até o peito. O medo vem de reconhecer gente da vila.
Conceito B, "O Esfomeado": o Sangue comeu o resto. Pele e osso, costelas abertas sobre a cavidade vermelha, espinha
rompendo as costas em espinhos, trapo do manto virou capuz, braços que arrastam os nós dos dedos, pernas dobradas
na ponta dos pés. O medo vem da forma.

Textura em 32 px por bloco (2 px por unidade do modelo): o .geo.json declara a UV em unidades (128x128) e a PNG tem
o dobro (256x256). O GeckoLib normaliza a UV pela largura declarada, então a PNG maior só dá mais detalhe.
Arte nossa, do zero (nada da obra: não é o Zumbi de Sangue nem outra criatura de lá).
"""
import math

from montagem import Criatura, animacao, canal, cor, hash01, sombreado, tom

# ------------------------------------------------------------------ paletas (Sangue: vermelho é o único acento)
PELE = ["#aea288", "#938973", "#7a715f", "#615a4c", "#49443a", "#302d28"]          # cinza-amarelada, esticada
PELE_B = ["#a29a8c", "#878074", "#6e685e", "#57524a", "#403c37", "#2a2725"]        # mais fria, mais morta (B)
ROXO = ["#7a6466", "#5f4c50", "#46373b", "#2f2427"]                                  # fundo de órbita, hematoma
MANTO = ["#4e4034", "#3d3229", "#2e2620", "#211b16", "#151110", "#0d0a09"]          # lã queimada
CARVAO = ["#191312", "#0e0a09"]
BRASA = "#4a2a18"
SANGUE = ["#b42a33", "#82141c", "#580a11", "#34050a", "#1c0207"]
OSSO = ["#ddd1b4", "#bdb092", "#958a70", "#6a614e", "#463f33"]
UNHA = ["#4d3e2e", "#33291f", "#1f1913"]


def _escuro(c, k=0.72):
    return (int(c[0] * k), int(c[1] * k), int(c[2] * k), 255)


def _mistura(a, b, t):
    return tuple(int(a[i] * (1 - t) + b[i] * t) for i in range(3)) + (255,)


# ------------------------------------------------------------------ pintores (coordenadas da PNG HD)

def pele(paleta, semente, costelas=False, veias=True, escorrido=0.0, cilindro=True, base=1, hematoma=0.0):
    """Pele esticada: luz de cima, lados escuros (dá volume de membro fino), veias roxas finas, hematomas largos,
    costelas marcadas (luz em cima do osso, sulco embaixo) e sangue escorrido vindo de cima."""
    def p(face, fx, fy, fw, fh):
        c = sombreado(paleta, face, fx, fy, fw, fh, semente, base=base, pintas=(0.03, 0.985), manchas=0.07)
        lat = face in ("north", "south", "east", "west")
        if lat and fh >= 12 and fy < 2:
            c = _escuro(c, 0.7)  # sombra de encaixe
        if lat and cilindro and fw >= 4:
            d = min(fx, fw - 1 - fx) / fw
            if d < 0.13:
                c = _escuro(c, 0.8)
            elif d > 0.38 and fy < fh * 0.5 and face == "north":
                c = _mistura(c, cor(paleta[0]), 0.25)
        if lat and hematoma and hash01(fx // 5, fy // 4, semente + 77) < hematoma:
            c = _mistura(c, cor(ROXO[1]), 0.35)
        if lat and costelas and face in ("north", "east", "west") and fh >= 8:
            meio = fw / 2
            curva = int(abs(fx - meio) * 0.35)
            k = (fy - curva) % 5
            if fy < fh * 0.8:
                if k == 0:
                    c = _mistura(c, cor(OSSO[1]), 0.35)
                elif k in (2, 3):
                    c = _escuro(c, 0.66)
            if face == "north" and abs(fx - meio) < 1 and fy < fh * 0.75:
                c = _escuro(c, 0.6)  # esterno afundado
        if lat and veias and fh >= 10:
            for k in range(2):
                x0 = int(hash01(k, 3, semente) * fw)
                xv = x0 + int(round(math.sin(fy * 0.35 + k * 2 + semente) * 1.5))
                if fx == xv and hash01(k, fy // 6, semente + 9) < 0.7:
                    c = _mistura(c, cor(ROXO[2]), 0.55)
        if lat and escorrido and fw >= 3:
            if hash01(fx // 2, 0, semente + 41) < escorrido:
                L = int(fh * (0.25 + 0.6 * hash01(fx // 2, 1, semente + 41)))
                if fy < L:
                    i = 2 if fy < L - 2 else 3
                    c = cor(SANGUE[i]) if fx % 2 == 0 or fy < L * 0.5 else _mistura(c, cor(SANGUE[3]), 0.5)
        return c
    return p


def mat(paleta, semente, **k):
    return lambda face, fx, fy, fw, fh: sombreado(paleta, face, fx, fy, fw, fh, semente, **k)


def manto(semente, franja=True, queimado=0.35):
    """Lã queimada: trama (linhas finas), dobras verticais, borda de baixo comida pelo fogo em dentes irregulares
    (transparente), carvão perto da borda e um fio de brasa velha (marrom, sem brilho)."""
    def p(face, fx, fy, fw, fh):
        lat = face in ("north", "south", "east", "west")
        if lat and franja and fh >= 6:
            fundo = fh - 1 - int(hash01(fx // 2, 5, semente) * fh * queimado)
            if fy > fundo:
                return None
            if fy >= fundo - 1:
                return cor(CARVAO[1])
            if fy >= fundo - 3:
                return cor(CARVAO[0]) if hash01(fx, fy, semente) < 0.6 else cor(BRASA)
        if lat and hash01(fx // 3, fy // 3, semente + 3) < 0.05:
            return None if fh > 6 and 2 < fy < fh - 3 and fw > 6 else cor(CARVAO[0])  # furo de traça / queimadura
        c = sombreado(MANTO, face, fx, fy, fw, fh, semente, base=1, pintas=(0.05, 0.97), manchas=0.2)
        if lat:
            if (fx + int(hash01(fx // 4, 0, semente) * 3)) % 6 == 0:
                c = _escuro(c, 0.75)  # dobra
            if fy % 2 == 0 and fx % 4 == 1:
                c = _mistura(c, cor(MANTO[0]), 0.2)  # trama
        return c
    return p


def cabeca_a(face, fx, fy, fw, fh):
    """Cabeça do A: testa alta, órbitas fundas e vazias (sem olho), maçã do rosto saltada, pele rachada no topo."""
    c = sombreado(PELE, face, fx, fy, fw, fh, 21, base=1, pintas=(0.04, 0.975), manchas=0.12)
    if face == "north":
        # órbitas: duas covas fundas, roxas na borda, pretas no fundo
        for cx in (fw * 0.27, fw * 0.73):
            dx, dy = (fx - cx) / (fw * 0.15), (fy - fh * 0.55) / (fh * 0.13)
            d = dx * dx + dy * dy
            if d < 0.45:
                return cor("#070505")
            if d < 1.0:
                return cor(ROXO[2])
            if d < 1.5:
                c = _mistura(c, cor(ROXO[0]), 0.5)
            if 0.0 < fy - fh * 0.68 < 2 and abs(fx - cx) < fw * 0.18:
                c = _escuro(c, 0.65)  # olheira escorrida
        if fy > fh * 0.78 and (abs(fx - fw * 0.2) < 1.2 or abs(fx - fw * 0.8) < 1.2):
            c = _escuro(c, 0.7)  # bochecha chupada
        if fy >= fh - 2:
            c = _escuro(c, 0.6)
    if face == "up" and hash01(fx // 2, fy // 2, 23) < 0.12:
        c = cor(ROXO[3])  # rachado
    if face in ("east", "west") and fy > fh * 0.6 and fx > fw * 0.3:
        c = _escuro(c, 0.82)
    if face == "south" and fy < fh * 0.4 and hash01(fx // 3, fy // 3, 24) < 0.15:
        c = cor(ROXO[2])  # tufos de cabelo que sobraram
    return c


def cranio_b(face, fx, fy, fw, fh):
    """Cabeça do B (dentro do capuz): crânio com pele colada, órbitas grandes, nariz comido (dois furos)."""
    c = sombreado(PELE_B, face, fx, fy, fw, fh, 31, base=2, pintas=(0.04, 0.975), manchas=0.12)
    if face == "north":
        for cx in (fw * 0.28, fw * 0.72):
            dx, dy = (fx - cx) / (fw * 0.19), (fy - fh * 0.42) / (fh * 0.17)
            d = dx * dx + dy * dy
            if d < 0.6:
                return cor("#060404")
            if d < 1.15:
                return cor(ROXO[3])
            if d < 1.6:
                c = _escuro(c, 0.75)
        if abs(fx - fw / 2) < 1.6 and fh * 0.62 < fy < fh * 0.78 and fx != fw // 2:
            return cor("#0a0606")  # furos do nariz
        if fy > fh * 0.82:
            c = _escuro(c, 0.65)
    return c


def boca(face, fx, fy, fw, fh):
    """Garganta: o acento. Vermelho molhado na borda, escuro no fundo."""
    if face == "down":
        # céu da boca, visto de baixo quando a mandíbula cai: sulcos escuros, um brilho molhado
        if fy % 3 == 0:
            return tom(SANGUE, 4)
        return tom(SANGUE, 1) if hash01(fx, fy, 51) < 0.08 else tom(SANGUE, 2 + (fx + fy) % 2)
    return tom(SANGUE, 4 if fy > fh // 2 else 3)


def dentes(face, fx, fy, fw, fh):
    if face in ("north", "south"):
        if fx % 3 == 2 or (fx // 3) % 4 == 3:
            return tom(SANGUE, 4)  # vão de dente que caiu
        return tom(OSSO, 1 if fy == 0 else 2) if fx % 3 else tom(OSSO, 3)
    return tom(OSSO, 3)


def mandibula(paleta, semente):
    base = pele(paleta, semente, veias=False, cilindro=False)

    def p(face, fx, fy, fw, fh):
        if face == "up":
            # dentro da boca de baixo: língua seca no meio, gengiva escura, um brilho molhado
            meio = abs(fx - fw / 2) < fw * 0.28
            if meio and fy > 2:
                return tom(SANGUE, 1) if hash01(fx, fy, 61) < 0.1 else tom(SANGUE, 2)
            return tom(SANGUE, 3 + (fy < 2))
        if face == "north" and fy < 2:
            return tom(SANGUE, 2)  # lábio rasgado
        if face in ("north", "east", "west") and fy >= 2:
            c = base(face, fx, fy, fw, fh)
            if hash01(fx, 0, semente + 3) < 0.4 and fy < 2 + int(fh * hash01(fx, 1, semente + 3)):
                return tom(SANGUE, 2 if fy % 3 else 3)  # baba de sangue escorrendo do queixo
            return c
        return base(face, fx, fy, fw, fh)
    return p


def dedos(paleta, semente):
    """Dedo comprido: pele escura, nó marcado, unha preta comprida na ponta."""
    def p(face, fx, fy, fw, fh):
        if face in ("north", "south", "east", "west"):
            if fy >= fh - 3:
                return tom(UNHA, 1 if fy < fh - 1 else 2)
            if fy == fh - 4:
                return tom(UNHA, 0)
            if fy % 4 == 3:
                return cor(paleta[4])  # nó do dedo
        return sombreado(paleta, face, fx, fy, fw, fh, semente, base=2, grad=False, manchas=0.0, pintas=(0.0, 1.0))
    return p


def pes(paleta, semente):
    """Pé descalço, magro: dedos separados por sulco na frente, unhas escuras, sola suja de terra e cinza."""
    def p(face, fx, fy, fw, fh):
        if face == "down":
            return cor(CARVAO[0]) if hash01(fx, fy, semente) < 0.5 else tom(paleta, 5)
        if face == "north":
            if fx % 3 == 2:
                return tom(paleta, 5)
            if fy >= fh - 1:
                return tom(UNHA, 2)
            if fy >= fh - 2:
                return tom(UNHA, 1)
        if face == "up" and fy < 3 and fx % 3 == 2:
            return tom(paleta, 4)
        c = sombreado(paleta, face, fx, fy, fw, fh, semente, base=2, manchas=0.05, pintas=(0.0, 1.0))
        if face in ("east", "west", "south") and fy >= fh - 1:
            return cor(CARVAO[0])
        return c
    return p


def bochecha(semente, paleta=PELE):
    """Pele das bochechas rasgada e esticada entre a cabeça e a mandíbula caída: tiras verticais com vãos."""
    def p(face, fx, fy, fw, fh):
        if face in ("east", "west"):
            col = fx // 2
            if hash01(col, 0, semente) < 0.35 and fh * 0.25 < fy < fh * 0.85:
                return None  # rasgo
            if hash01(col, 1, semente) < 0.3:
                return tom(SANGUE, 3)
            return tom(paleta, 3 + (fy % 4 == 0))
        return tom(paleta, 4)
    return p


def osso_pintor(semente):
    def p(face, fx, fy, fw, fh):
        c = sombreado(OSSO, face, fx, fy, fw, fh, semente, base=1, manchas=0.1)
        if face != "up" and fy >= fh - 2:
            return tom(SANGUE, 3)  # raiz do espinho, onde rasgou a pele
        if fy < 2 and face != "down":
            c = tom(OSSO, 0)
        return c
    return p


def cavidade(face, fx, fy, fw, fh):
    """Dentro do peito do B: carne viva escura, um ponto molhado mais claro."""
    if face == "north":
        d = ((fx - fw * 0.5) / fw) ** 2 + ((fy - fh * 0.45) / fh) ** 2
        if d < 0.02:
            return tom(SANGUE, 1)
        return tom(SANGUE, 2 if d < 0.08 else (3 if d < 0.18 else 4))
    return tom(SANGUE, 4)


def costela(face, fx, fy, fw, fh):
    c = sombreado(OSSO, face, fx, fy, fw, fh, 71 + fw, base=2, grad=False, manchas=0.0)
    if face == "north" and (fx < 2 or fx >= fw - 2):
        return cor(PELE_B[3])  # onde a costela entra na pele
    if face == "down":
        return tom(OSSO, 3)
    return c


# ------------------------------------------------------------------ montagem


def _membro(c, lado, s, pai, ombro, comp_braco, comp_ante, larg, pal, sem, dedos_comp, rot_ante=0.0):
    """Braço comprido: braço, antebraço e mão em ossos próprios; três dedos longos com unha."""
    xo = ombro[0] - larg / 2
    esp = s > 0
    b = c.osso(f"braco_{lado}", pai, ombro)
    y0 = ombro[1] - comp_braco
    b.cubo(f"braco_{lado}", (xo, y0, -larg / 2), (larg, comp_braco, larg), pele(pal, sem + 1, hematoma=0.12),
           espelho=esp, partilha=None if not esp else "braco_dir")
    a = c.osso(f"antebraco_{lado}", f"braco_{lado}", (ombro[0], y0 + 0.5, 0), (rot_ante, 0, 0) if rot_ante else None)
    y1 = y0 - comp_ante
    la = larg - 0.4
    a.cubo(f"antebraco_{lado}", (ombro[0] - la / 2, y1, -la / 2), (la, comp_ante + 0.5, la),
           pele(pal, sem + 2, escorrido=0.12, hematoma=0.08), espelho=esp, partilha=None if not esp else "antebraco_dir")
    m = c.osso(f"mao_{lado}", f"antebraco_{lado}", (ombro[0], y1 + 0.3, 0))
    m.cubo(f"palma_{lado}", (ombro[0] - 1.25, y1 - 2.6, -1.2), (2.5, 2.8, 2.2), pele(pal, sem + 3, veias=False),
           espelho=esp, partilha=None if not esp else "palma_dir")
    for i, dx in enumerate((-0.95, 0.0, 0.95)):
        comp = dedos_comp - (0.8 if i == (2 if s < 0 else 0) else 0)
        m.cubo(f"dedo_{lado}_{i}", (ombro[0] + dx - 0.35, y1 - 2.6 - comp + 0.2, -1.05 + (0.2 if i == 1 else 0)),
               (0.7, comp, 0.7), dedos(pal, sem + 4), espelho=esp,
               partilha=None if (not esp) else f"dedo_dir_{2 - i}")
    return b


def _perna(c, lado, s, quadril, coxa, canela, larg, pal, sem, rot_coxa, rot_canela, pe_comp=4.0):
    esp = s > 0
    p = c.osso(f"perna_{lado}", "raiz", quadril, (rot_coxa, 0, 2 * s))
    yk = quadril[1] - coxa
    p.cubo(f"coxa_{lado}", (quadril[0] - larg / 2, yk, -larg / 2), (larg, coxa + 0.5, larg), pele(pal, sem),
           espelho=esp, partilha=None if not esp else "coxa_dir")
    k = c.osso(f"canela_{lado}", f"perna_{lado}", (quadril[0], yk + 0.4, 0), (rot_canela, 0, 0))
    lc = larg - 0.3
    k.cubo(f"canela_{lado}", (quadril[0] - lc / 2, yk - canela, -lc / 2), (lc, canela + 0.6, lc),
           pele(pal, sem + 1, escorrido=0.1), espelho=esp, partilha=None if not esp else "canela_dir")
    k.cubo(f"pe_{lado}", (quadril[0] - 1.25, yk - canela - 1.2, -pe_comp + 0.8), (2.5, 1.4, pe_comp),
           pes(pal, sem + 2), espelho=esp, partilha=None if not esp else "pe_dir")
    return yk


def montar_a():
    c = Criatura("faminto_a", 128, 128)
    c.osso("raiz")
    # pernas finas, joelho dobrado para a frente (cansado, pesado na frente)
    for lado, s in (("dir", -1), ("esq", 1)):
        _perna(c, lado, s, (2.0 * s, 14, 0.5), 6.6, 6.3, 2.6, PELE, 3, 30, -40)
    t = c.osso("tronco", "raiz", (0, 13.5, 0.5), (-34, 0, 3))
    t.cubo("cintura", (-2.8, 13, -1.6), (5.6, 6.5, 3.8), pele(PELE, 11, hematoma=0.15))
    t.cubo("peito", (-4.2, 19, -2.4), (8.4, 8, 5.2), pele(PELE, 12, costelas=True, escorrido=0.18))
    t.cubo("corcunda", (-3.6, 21, 1.2), (7.2, 6.5, 3.4), pele(PELE, 13, hematoma=0.25))
    for i, (y, z) in enumerate(((25.5, 4.2), (23.0, 4.3), (20.5, 3.9), (18.0, 2.9))):
        t.cubo(f"vertebra_{i}", (-0.7, y, z), (1.4, 1.4, 1.0), osso_pintor(14 + i), partilha=None if i == 0 else "vertebra_0")
    # manto queimado: gola por cima dos ombros, capa curta nas costas e duas tiras na frente (osso próprio, balança)
    tr = c.osso("trapos", "tronco", (0, 26, 0.5))
    tr.cubo("gola", (-4.8, 25.4, -2.9), (9.6, 2.4, 6.4), manto(5, franja=False))
    tr.cubo("capa", (-4.6, 10.5, 3.3), (9.2, 15.5, 0.8), manto(6, queimado=0.45))
    tr.cubo("tira_a", (-3.6, 9.0, -2.8), (3.0, 10.5, 0.6), manto(7, queimado=0.5))
    tr.cubo("tira_b", (0.6, 11.0, -2.8), (2.8, 8.5, 0.6), manto(8, queimado=0.5))
    # pescoço fino esticado para a frente, cabeça alta de aldeão
    pe = c.osso("pescoco", "tronco", (0, 26.5, -1.2), (18, 0, 0))
    pe.cubo("pescoco", (-1.2, 25.8, -2.6), (2.4, 4.2, 2.4), pele(PELE, 15, veias=True))
    cb = c.osso("cabeca", "pescoco", (0, 29.6, -1.4), (20, 0, 9))
    cb.cubo("cranio", (-3.5, 29.4, -5.2), (7, 9, 7), cabeca_a)
    cb.cubo("nariz", (-0.9, 29.6, -6.6), (1.8, 3.6, 1.5), pele(PELE, 16, veias=False, cilindro=False), rotacao=(8, 0, 6),
            pivo=(0, 33.0, -5.2))
    cb.cubo("bochecha_dir", (-3.0, 25.0, -5.0), (0.3, 4.5, 3.6), bochecha(19))
    cb.cubo("bochecha_esq", (2.7, 25.0, -5.0), (0.3, 4.5, 3.6), bochecha(20), espelho=True, partilha="bochecha_dir")
    cb.cubo("garganta", (-2.7, 27.8, -4.8), (5.4, 1.8, 5.0), boca)
    cb.cubo("dentes_cima", (-2.6, 28.6, -5.15), (5.2, 0.9, 0.5), dentes)
    mb = c.osso("mandibula", "cabeca", (0, 29.0, -1.0), (-52, 0, -6))
    mb.cubo("queixo", (-2.9, 27.0, -5.6), (5.8, 1.6, 4.8), mandibula(PELE, 17))
    mb.cubo("dentes_baixo", (-2.5, 28.5, -5.35), (5.0, 0.9, 0.5), dentes)
    mb.cubo("ramo_dir", (-3.0, 27.0, -1.6), (0.8, 2.2, 1.2), pele(PELE, 18, veias=False, cilindro=False))
    mb.cubo("ramo_esq", (2.2, 27.0, -1.6), (0.8, 2.2, 1.2), pele(PELE, 18, veias=False, cilindro=False), espelho=True,
            partilha="ramo_dir")
    for lado, s in (("dir", -1), ("esq", 1)):
        b = _membro(c, lado, s, "tronco", (5.0 * s, 26.0, -0.2), 9.5, 9.0, 2.2, PELE, 30 + (s > 0) * 10, 4.0,
                    rot_ante=14 if s < 0 else 8)
        b.rotacao = [54 if s < 0 else 46, 0, -6 * s]
    return c


def montar_b():
    c = Criatura("faminto_b", 128, 128)
    c.osso("raiz")
    # pernas de pau, na ponta dos pés: joelho bem à frente, canela para trás
    for lado, s in (("dir", -1), ("esq", 1)):
        _perna(c, lado, s, (1.9 * s, 16.5, 0.5), 8.4, 8.2, 2.2, PELE_B, 103, 40, -70, pe_comp=3.4)
    t = c.osso("tronco", "raiz", (0, 15.5, 0.5), (-44, 0, -4))
    t.cubo("cintura", (-2.2, 15, -1.3), (4.4, 6.5, 3.0), pele(PELE_B, 111, hematoma=0.2))
    t.cubo("costas", (-4.0, 21, -0.3), (8.0, 9.5, 3.4), pele(PELE_B, 112, costelas=True, hematoma=0.15))
    t.cubo("cavidade", (-2.9, 21.5, -1.9), (5.8, 8.2, 1.8), cavidade)
    # costelas abertas: cada uma em duas metades que contornam o peito e não se encontram no meio (o esterno sumiu)
    for i, y in enumerate((22.4, 24.6, 26.8, 28.9)):
        meia = 3.6 if i < 3 else 3.2
        for lado, s in (("d", -1), ("e", 1)):
            x0 = 0.7 if s > 0 else -0.7 - meia
            t.cubo(f"costela_{i}{lado}", (x0, y - (i % 2) * 0.3 * s, -2.35), (meia, 0.7, 0.8), costela,
                   rotacao=(0, -24 * s, -8 * s), pivo=(s * 4.0, y, -0.8), espelho=s > 0,
                   partilha=None if s < 0 else f"costela_{i}d")
    t.cubo("ombros", (-4.6, 29.2, -1.8), (9.2, 2.0, 4.6), pele(PELE_B, 113, veias=False))
    # espinha rompendo as costas: espinhos de osso apontando para trás e para cima (traço único na silhueta)
    for i, (y, h) in enumerate(((29.0, 3.6), (26.4, 4.4), (23.6, 4.0), (20.8, 3.2), (18.2, 2.4))):
        t.cubo(f"espinho_{i}", (-0.6, y, 2.6), (1.2, h, 1.2), osso_pintor(120 + i), rotacao=(48 - i * 4, 0, 0),
               pivo=(0, y, 3.1))
    tr = c.osso("trapos", "tronco", (0, 16, 0.5))
    tr.cubo("tanga_f", (-2.6, 8.5, -1.7), (5.2, 7.0, 0.6), manto(105, queimado=0.55))
    tr.cubo("tanga_t", (-2.4, 10.0, 1.5), (4.8, 5.5, 0.6), manto(106, queimado=0.55))
    pe = c.osso("pescoco", "tronco", (0, 30.5, -0.8), (30, 0, 0))
    pe.cubo("pescoco", (-1.0, 29.8, -2.0), (2.0, 4.0, 2.0), pele(PELE_B, 115, veias=True))
    cb = c.osso("cabeca", "pescoco", (0, 33.4, -1.0), (26, 0, -16))
    cb.cubo("cranio", (-2.8, 32.4, -5.0), (5.6, 7.0, 6.0), cranio_b)
    cb.cubo("garganta", (-2.3, 31.0, -4.4), (4.6, 1.6, 4.2), boca)
    cb.cubo("dentes_cima", (-2.2, 31.8, -4.75), (4.4, 0.8, 0.5), dentes)
    cb.cubo("bochecha_dir", (-2.5, 28.4, -4.6), (0.3, 4.0, 3.4), bochecha(119, PELE_B))
    cb.cubo("bochecha_esq", (2.2, 28.4, -4.6), (0.3, 4.0, 3.4), bochecha(120, PELE_B), espelho=True, partilha="bochecha_dir")
    # capuz: o trapo do manto enrolado na cabeça, com a ponta caída para trás
    cb.cubo("capuz", (-3.6, 33.6, -4.6), (7.2, 6.8, 7.6), manto(107, queimado=0.25), inflar=0.15)
    cb.cubo("capuz_costas", (-3.4, 28.0, 2.2), (6.8, 8.0, 1.0), manto(108, queimado=0.5))
    cb.cubo("capuz_ponta", (-1.4, 37.6, 0.5), (2.8, 2.0, 4.6), manto(109, franja=False), rotacao=(-30, 0, 0),
            pivo=(0, 38.0, 1.0))
    mb = c.osso("mandibula", "cabeca", (0, 32.0, -1.0), (-60, 0, 10))
    mb.cubo("queixo", (-2.5, 29.4, -5.2), (5.0, 1.6, 4.6), mandibula(PELE_B, 117))
    mb.cubo("dentes_baixo", (-2.1, 30.8, -4.95), (4.2, 0.8, 0.5), dentes)
    mb.cubo("ramo_dir", (-2.6, 29.4, -1.0), (0.7, 2.6, 1.0), pele(PELE_B, 118, veias=False, cilindro=False))
    mb.cubo("ramo_esq", (1.9, 29.4, -1.0), (0.7, 2.6, 1.0), pele(PELE_B, 118, veias=False, cilindro=False), espelho=True,
            partilha="ramo_dir")
    for lado, s in (("dir", -1), ("esq", 1)):
        b = _membro(c, lado, s, "tronco", (5.0 * s, 29.6, 0.2), 10.0, 9.5, 1.9, PELE_B, 130 + (s > 0) * 10, 4.2,
                    rot_ante=8 if s < 0 else 4)
        b.rotacao = [60 if s < 0 else 54, 0, -4 * s]
    return c


VARIANTES = {"a": montar_a, "b": montar_b}


def montar(variante="a"):
    return VARIANTES[variante]()


# ------------------------------------------------------------------ animações (mesmos nomes nos dois conceitos)
E = "easeInOutSine"
OUT = "easeOutCubic"
IN = "easeInCubic"


def _pernas(c):
    p = c.por_nome["perna_dir"]
    k = c.por_nome["canela_dir"]
    return p.pivo[1], k.pivo[1], (p.rotacao or [0])[0], (k.rotacao or [0])[0]


def anims(c):
    """c: a criatura montada (as poses de ajoelhar dependem do tamanho da perna de cada conceito)."""
    yq, yk, rc, rk = _pernas(c)
    rt = c.por_nome["tronco"].rotacao[0]
    queda = -(yk - 1.6)       # o quadril desce até o joelho tocar o chão
    # pose de joelhos (valores somados à pose de descanso): coxa em pé, canela deitada para trás, tronco curvado,
    # cabeça baixa, braços largados, mandíbula aberta
    J = {
        "raiz": {"position": (0, queda, 0)},
        "perna_dir": {"rotation": (4 - rc, 0, 0)}, "perna_esq": {"rotation": (2 - rc, 0, 0)},
        "canela_dir": {"rotation": (-92 - rk + 0, 0, 0)}, "canela_esq": {"rotation": (-90 - rk, 0, 0)},
        "tronco": {"rotation": (16, 0, 0)},
        "cabeca": {"rotation": (10, 0, 0)},
        "mandibula": {"rotation": (-14, 0, 0)},
        "braco_dir": {"rotation": (-34, 0, 8)}, "braco_esq": {"rotation": (-30, 0, -8)},
        "antebraco_dir": {"rotation": (-44, 0, 0)}, "antebraco_esq": {"rotation": (-50, 0, 0)},
        "mao_dir": {"rotation": (-30, 0, 0)}, "mao_esq": {"rotation": (-34, 0, 0)},
    }

    def jv(o, canal_n="rotation"):
        return J.get(o, {}).get(canal_n, (0, 0, 0))

    parado = animacao(4.0, True, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (1.6, (2.5, 0, 1), E), (4.0, (0, 0, 0), E)),
                   "position": canal((0, (0, 0, 0)), (1.6, (0, 0.35, 0), E), (4.0, (0, 0, 0), E))},
        # a cabeça vira devagar, para, e dá um tranco curto (escutou algo)
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (1.4, (2, -14, -4), E), (2.6, (2, -14, -4)), (2.7, (-6, 8, 6), OUT),
                                     (3.2, (-3, 6, 4), E), (4.0, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (1.0, (6, 0, 3), E), (2.0, (-4, 0, -2), E), (2.7, (-4, 0, -2)),
                                        (2.78, (10, 0, 0), OUT), (2.95, (-6, 0, 0), E), (4.0, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (2.0, (-3, 0, 1.5), E), (4.0, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (2.2, (-2.5, 0, -1.5), E), (4.0, (0, 0, 0), E))},
        "mao_dir": {"rotation": canal((0, (0, 0, 0)), (3.0, (0, 0, 0)), (3.1, (14, 0, 0), OUT), (3.5, (0, 0, 0), E), (4.0, (0, 0, 0)))},
        "mao_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (10, 0, 0), E), (1.2, (0, 0, 0), E), (4.0, (0, 0, 0)))},
        "trapos": {"rotation": canal((0, (0, 0, 0)), (2.0, (2, 0, 1), E), (4.0, (0, 0, 0), E))},
    })
    # andar arrastado (1,7 s): o pé direito sai do chão, o esquerdo arrasta; tronco cai no apoio; braços e mandíbula
    # chegam atrasados (peso morto)
    andar = animacao(1.7, True, {
        "perna_dir": {"rotation": canal((0, (20, 0, 0)), (0.85, (-16, 0, 0), E), (1.7, (20, 0, 0), E))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.85, (0, 0, 0)), (1.25, (-30, 0, 0), OUT), (1.7, (0, 0, 0), IN))},
        "perna_esq": {"rotation": canal((0, (-14, 0, 0)), (0.85, (14, 0, 0), E), (1.7, (-14, 0, 0), E))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.35, (-8, 0, 0), OUT), (0.85, (0, 0, 0), IN), (1.7, (0, 0, 0)))},
        "tronco": {"rotation": canal((0, (-2, 5, 3)), (0.85, (-2, -5, -2), E), (1.7, (-2, 5, 3), E)),
                   "position": canal((0, (0, -0.9, 0)), (0.12, (0, -1.1, 0), OUT), (0.5, (0, 0, 0), E), (0.85, (0, -0.6, 0), IN),
                                     (0.97, (0, -0.8, 0), OUT), (1.35, (0, 0, 0), E), (1.7, (0, -0.9, 0), IN))},
        "cabeca": {"rotation": canal((0, (3, -5, 0)), (0.18, (-2, -4, 0), OUT), (0.85, (3, 5, 0), E), (1.03, (-2, 4, 0), OUT),
                                     (1.7, (3, -5, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.2, (-8, 0, 0), OUT), (0.6, (4, 0, 0), E), (1.05, (-8, 0, 0), OUT),
                                        (1.45, (4, 0, 0), E), (1.7, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (-10, 0, 0)), (0.3, (-13, 0, 0), E), (0.95, (10, 0, 0), E), (1.15, (12, 0, 0), E),
                                        (1.7, (-10, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-8, 0, 0), E), (1.25, (6, 0, 0), E), (1.7, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (10, 0, 0)), (0.85, (-12, 0, 0), E), (1.7, (10, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.4, (-10, 0, 0), E), (1.2, (-2, 0, 0), E), (1.7, (0, 0, 0), E))},
        "trapos": {"rotation": canal((0, (6, 0, 0)), (0.3, (9, 0, 2), E), (0.85, (-3, 0, 0), E), (1.15, (-6, 0, -2), E),
                                     (1.7, (6, 0, 0), E))},
    })
    # correr (0,8 s): quando o sino toca rápido e chama, eles sobem a rua. Mais deitado, braços para trás.
    correr = animacao(0.8, True, {
        "perna_dir": {"rotation": canal((0, (34, 0, 0)), (0.4, (-30, 0, 0), E), (0.8, (34, 0, 0), E))},
        "canela_dir": {"rotation": canal((0, (-10, 0, 0)), (0.4, (0, 0, 0)), (0.6, (-55, 0, 0), OUT), (0.8, (-10, 0, 0), IN))},
        "perna_esq": {"rotation": canal((0, (-30, 0, 0)), (0.4, (34, 0, 0), E), (0.8, (-30, 0, 0), E))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.2, (-55, 0, 0), OUT), (0.4, (-10, 0, 0), IN), (0.8, (0, 0, 0)))},
        "tronco": {"rotation": canal((0, (-16, 4, 0)), (0.4, (-16, -4, 0), E), (0.8, (-16, 4, 0), E)),
                   "position": canal((0, (0, -1.2, -1)), (0.2, (0, 0.4, -1), OUT), (0.4, (0, -1.2, -1), IN),
                                     (0.6, (0, 0.4, -1), OUT), (0.8, (0, -1.2, -1), IN))},
        "cabeca": {"rotation": canal((0, (-14, 0, 0)), (0.2, (-10, 0, 0), OUT), (0.4, (-14, 0, 0)), (0.6, (-10, 0, 0), OUT),
                                     (0.8, (-14, 0, 0)))},
        "mandibula": {"rotation": canal((0, (-14, 0, 0)), (0.2, (-4, 0, 0), OUT), (0.4, (-14, 0, 0)), (0.6, (-4, 0, 0), OUT),
                                        (0.8, (-14, 0, 0)))},
        "braco_dir": {"rotation": canal((0, (-40, 0, 6)), (0.4, (-20, 0, 6), E), (0.8, (-40, 0, 6), E))},
        "braco_esq": {"rotation": canal((0, (-20, 0, -6)), (0.4, (-40, 0, -6), E), (0.8, (-20, 0, -6), E))},
        "antebraco_dir": {"rotation": canal((0, (-20, 0, 0)), (0.8, (-20, 0, 0)))},
        "antebraco_esq": {"rotation": canal((0, (-20, 0, 0)), (0.8, (-20, 0, 0)))},
        "trapos": {"rotation": canal((0, (22, 0, 0)), (0.2, (28, 0, 3), E), (0.4, (22, 0, 0), E), (0.6, (28, 0, -3), E),
                                     (0.8, (22, 0, 0), E))},
    })
    # ataque (1,1 s): 0,4 s de antecipação (encolhe, braços para trás e para cima, mandíbula escancara), bote com as
    # duas mãos em gancho e a mandíbula fechando de uma vez, continuação baixa e volta
    ataque = animacao(1.1, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.4, (0, -0.6, 1.2), OUT), (0.52, (0, -0.2, -3.0), IN), (0.75, (0, -0.4, -3.0), E),
                                   (1.1, (0, 0, 0), E))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.4, (14, 0, -3), OUT), (0.52, (-22, 0, 2), IN), (0.62, (-26, 0, 2), OUT),
                                     (1.1, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.4, (-12, 0, 0), OUT), (0.52, (12, 0, 0), IN), (0.7, (8, 0, -4), OUT),
                                     (1.1, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.4, (-24, 0, 0), OUT), (0.5, (-26, 0, 0)), (0.56, (42, 0, 0), IN),
                                        (0.62, (34, 0, 0), OUT), (1.1, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-96, 0, 18), OUT), (0.53, (36, 0, -10), IN), (0.7, (42, 0, -12), OUT),
                                        (1.1, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.42, (-90, 0, -18), OUT), (0.55, (32, 0, 10), IN), (0.72, (38, 0, 12), OUT),
                                        (1.1, (0, 0, 0), E))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-38, 0, 0), OUT), (0.53, (-6, 0, 0), IN), (1.1, (0, 0, 0), E))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (0.42, (-38, 0, 0), OUT), (0.55, (-6, 0, 0), IN), (1.1, (0, 0, 0), E))},
        "mao_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-20, 0, 0), OUT), (0.53, (40, 0, 0), IN), (1.1, (0, 0, 0), E))},
        "mao_esq": {"rotation": canal((0, (0, 0, 0)), (0.42, (-20, 0, 0), OUT), (0.55, (40, 0, 0), IN), (1.1, (0, 0, 0), E))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (0.4, (-8, 0, 0), OUT), (0.52, (18, 0, 0), IN), (1.1, (0, 0, 0), E))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (0.4, (6, 0, 0), OUT), (0.52, (-14, 0, 0), IN), (1.1, (0, 0, 0), E))},
        "trapos": {"rotation": canal((0, (0, 0, 0)), (0.4, (-10, 0, 0), OUT), (0.6, (22, 0, 0), "easeOutBack"), (1.1, (0, 0, 0), E))},
    })
    dano = animacao(0.45, False, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.08, (12, 0, 5), OUT), (0.45, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.1, (-18, 10, -8), OUT), (0.45, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.1, (-16, 0, 0), OUT), (0.45, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.1, (-14, 0, 10), OUT), (0.45, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.1, (-14, 0, -10), OUT), (0.45, (0, 0, 0), E))},
    })
    # morrer (2,4 s): os joelhos cedem, cai de cara, quica, a mão ainda se fecha uma vez
    morrer = animacao(2.4, False, {
        "raiz": {"rotation": canal((0, (0, 0, 0)), (0.6, (0, 0, 0)), (1.25, (-78, 0, 6), IN), (1.38, (-72, 0, 6), OUT),
                                   (1.6, (-80, 0, 6), IN), (2.4, (-80, 0, 6))),
                 "position": canal((0, (0, 0, 0)), (0.6, (0, queda * 0.55, 0), E), (1.25, (0, -1.5, -2), IN), (2.4, (0, -1.5, -2)))},
        "perna_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (40, 0, 0), E), (1.25, (16, 0, 0), E), (2.4, (16, 0, 0)))},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (-70, 0, 0), E), (1.25, (-20, 0, 0), E), (2.4, (-24, 0, 0)))},
        "perna_esq": {"rotation": canal((0, (0, 0, 0)), (0.5, (30, 0, 0), E), (1.25, (8, 0, 0), E), (2.4, (8, 0, 0)))},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.5, (-60, 0, 0), E), (1.25, (-12, 0, 0), E), (2.4, (-12, 0, 0)))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.6, (-8, 0, 4), E), (1.25, (18, 0, 0), IN), (2.4, (18, 0, 0)))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.6, (-20, 0, 0), E), (1.3, (10, 22, 10), IN), (2.4, (10, 22, 10)))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.6, (-18, 0, 0), E), (1.35, (8, 0, 0), OUT), (2.4, (8, 0, 0)))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (-30, 0, 20), E), (1.25, (-50, 0, 40), IN), (2.4, (-50, 0, 40)))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.6, (-24, 0, -18), E), (1.25, (-30, 0, -50), IN), (2.4, (-30, 0, -50)))},
        "mao_dir": {"rotation": canal((0, (0, 0, 0)), (1.8, (0, 0, 0)), (1.95, (40, 0, 0), OUT), (2.4, (30, 0, 0), E))},
    })
    # escutar (2,2 s): a cada toque do sino a criatura para, se endireita, vira a cabeça e abre a boca (O Dobre)
    escutar = animacao(2.2, False, {
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.35, (12, 14, 0), OUT), (1.7, (12, 14, 0)), (2.2, (0, 0, 0), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.2, (0, 0, 0)), (0.45, (-22, 30, 12), OUT), (1.7, (-24, 32, 14), E),
                                     (2.2, (0, 0, 0), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (1.4, (-18, 0, 0), E), (1.7, (-18, 0, 0)), (2.2, (0, 0, 0), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (0.35, (-8, 0, 0), OUT), (1.7, (-8, 0, 0)), (2.2, (0, 0, 0), E))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (0.35, (-8, 0, 0), OUT), (1.7, (-8, 0, 0)), (2.2, (0, 0, 0), E))},
        "mao_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (0, 0, 0)), (0.7, (18, 0, 0), OUT), (1.0, (0, 0, 0), E))},
    })
    # ajoelhar (3,2 s, fica na pose final): no último toque do Dobre. Endireita, olha a torre, o joelho direito cede,
    # depois o esquerdo; o corpo dobra devagar e a cabeça baixa.
    def k3(osso, canal_n="rotation", meio=None, atraso=0.0):
        alvo = jv(osso, canal_n)
        m = meio if meio is not None else tuple(v * 0.35 for v in alvo)
        return canal((0, (0, 0, 0)), (0.6 + atraso, m, E), (1.9 + atraso, alvo, IN), (2.05 + atraso, tuple(v * 1.04 for v in alvo), OUT),
                     (3.2, alvo, E))
    ajoelhar = animacao(3.2, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.6, (0, 0.4, 0), E), (1.3, (0, queda * 0.45, 0), IN), (1.9, (0, queda, 0), IN),
                                   (2.05, (0, queda + 0.5, 0), OUT), (3.2, (0, queda, 0), E))},
        "perna_dir": {"rotation": k3("perna_dir")},
        "canela_dir": {"rotation": canal((0, (0, 0, 0)), (0.6, (0, 0, 0)), (1.3, jv("canela_dir"), IN), (3.2, jv("canela_dir")))},
        "perna_esq": {"rotation": k3("perna_esq", atraso=0.0)},
        "canela_esq": {"rotation": canal((0, (0, 0, 0)), (0.9, (-20, 0, 0), E), (1.9, jv("canela_esq"), IN), (3.2, jv("canela_esq")))},
        "tronco": {"rotation": canal((0, (0, 0, 0)), (0.6, (16, 0, 0), E), (1.9, (4, 0, 0), E), (3.2, jv("tronco"), E))},
        "cabeca": {"rotation": canal((0, (0, 0, 0)), (0.6, (-26, 0, 0), E), (2.0, (-20, 0, 0), E), (3.2, jv("cabeca"), E))},
        "mandibula": {"rotation": canal((0, (0, 0, 0)), (0.6, (-10, 0, 0), E), (3.2, jv("mandibula"), E))},
        "braco_dir": {"rotation": canal((0, (0, 0, 0)), (1.9, (-6, 0, 0), E), (2.4, jv("braco_dir"), E), (3.2, jv("braco_dir")))},
        "braco_esq": {"rotation": canal((0, (0, 0, 0)), (1.9, (-6, 0, 0), E), (2.5, jv("braco_esq"), E), (3.2, jv("braco_esq")))},
        "antebraco_dir": {"rotation": canal((0, (0, 0, 0)), (2.4, jv("antebraco_dir"), E), (3.2, jv("antebraco_dir")))},
        "antebraco_esq": {"rotation": canal((0, (0, 0, 0)), (2.5, jv("antebraco_esq"), E), (3.2, jv("antebraco_esq")))},
        "mao_dir": {"rotation": canal((0, (0, 0, 0)), (2.4, jv("mao_dir"), E), (3.2, jv("mao_dir")))},
        "mao_esq": {"rotation": canal((0, (0, 0, 0)), (2.5, jv("mao_esq"), E), (3.2, jv("mao_esq")))},
        "trapos": {"rotation": canal((0, (0, 0, 0)), (1.95, (0, 0, 0)), (2.1, (12, 0, 0), OUT), (3.2, (4, 0, 0), E))},
    })
    ajoelhar["loop"] = "hold_on_last_frame"

    # cinza (2,6 s): parte da pose de joelhos. A cabeça cai, o corpo afunda e encolhe; a mesa Dev solta as
    # partículas de cinza e remove a criatura no fim.
    def cz(osso, extra, canal_n="rotation"):
        a = jv(osso, canal_n)
        return canal((0, a), (1.2, tuple(a[i] + extra[i] * 0.4 for i in range(3)), E), (2.6, tuple(a[i] + extra[i] for i in range(3)), IN))
    cinza = animacao(2.6, False, {
        "raiz": {"position": canal((0, (0, queda, 0)), (1.2, (0, queda - 0.5, 0), E), (2.6, (0, queda - 3.0, 0), IN)),
                 "scale": canal((0, (1, 1, 1)), (1.4, (0.97, 0.94, 0.97), E), (2.6, (0.7, 0.3, 0.7), IN))},
        "perna_dir": {"rotation": cz("perna_dir", (0, 0, 0))}, "perna_esq": {"rotation": cz("perna_esq", (0, 0, 0))},
        "canela_dir": {"rotation": cz("canela_dir", (0, 0, 0))}, "canela_esq": {"rotation": cz("canela_esq", (0, 0, 0))},
        "tronco": {"rotation": cz("tronco", (-22, 0, 4))},
        "cabeca": {"rotation": cz("cabeca", (26, 0, -10))},
        "mandibula": {"rotation": cz("mandibula", (-14, 0, 0))},
        "braco_dir": {"rotation": cz("braco_dir", (10, 0, 8))}, "braco_esq": {"rotation": cz("braco_esq", (10, 0, -8))},
        "antebraco_dir": {"rotation": cz("antebraco_dir", (0, 0, 0))}, "antebraco_esq": {"rotation": cz("antebraco_esq", (0, 0, 0))},
        "mao_dir": {"rotation": cz("mao_dir", (0, 0, 0))}, "mao_esq": {"rotation": cz("mao_esq", (0, 0, 0))},
    })
    return {"parado": parado, "andar": andar, "correr": correr, "ataque": ataque, "dano": dano, "morrer": morrer,
            "escutar": escutar, "ajoelhar": ajoelhar, "cinza": cinza}
