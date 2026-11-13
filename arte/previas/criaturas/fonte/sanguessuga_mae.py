"""Sanguessuga-Mãe, versão 5.2 (revisão crítica da 5.1). lore/criaturas.md: corpo de cesta redonda, quatro pernas
de espinho, boca circular com anéis de dentes na barriga, filhotes presos nas costas. 2 blocos de altura x 3 de largura.

O que mudou da 5.1 (ver arte/previas/criaturas/comparacao/LEIA.md):
- Deixou de parecer um barril: corpo em três camadas de larguras diferentes (barriga estreita, meio inchado, topo
  arredondado), INCLINADO para a frente, com um lábio de carne saliente embaixo: a boca e os dentes aparecem de
  frente e de 3/4, não só por baixo.
- Pernas com três segmentos (coxa grossa, canela, tarso fino), joelho de espinho acima do corpo, mais abertas.
- Larvas com forma de larva (corpo + cabeça escura), meio enterradas na carne do topo; vermelho escuro com uma
  linha molhada clara, sem "tijolos vermelhos".
- Trama da cesta orgânica (fiadas que serpenteiam, tiras com sombra), não parede de tijolos.
- Animações: passo que levanta a perna de verdade (coxa sobe, canela dobra), corpo que afunda no apoio;
  mordida com antecipação e dentes fechando; morte com pernas que cedem, cesta que tomba e espasmos.
"""
import math

from montagem import Criatura, animacao, canal, cor, hash01, sombreado, tom

SANGUE = ["#E0313A", "#B3131B", "#7A0A10", "#3A0306"]
CESTA = ["#7a5c60", "#664a4e", "#543c40", "#432f33", "#312226", "#1e1416"]
QUITINA = ["#55474d", "#42363b", "#32282c", "#231b1e", "#150f11"]
OSSO = ["#e0d6ba", "#bfb294", "#978b6e", "#6b614a"]
FIO = "#15100f"
GARGANTA = "#0e0507"
LARVA = ["#c4252e", "#8e1017", "#5e080d", "#330206"]

PERNAS = {"fd": (10, -8), "fe": (-10, -8), "td": (10, 8), "te": (-10, 8)}
COXA, CANELA, TARSO = 18.0, 27.0, 5.0
ROT_COXA, ROT_CANELA, ROT_TARSO = 44.0, -66.0, -14.0


def mat(paleta, semente, **k):
    return lambda face, fx, fy, fw, fh: sombreado(paleta, face, fx, fy, fw, fh, semente, **k)


def montar():
    c = Criatura("sanguessuga_mae", 160, 128)
    c.osso("raiz")
    corpo = c.osso("corpo", "raiz", (0, 14, 0), (12, 0, 0))  # empinada: a frente sobe e a boca da barriga aparece de frente
    # cesta: três camadas (dois quadrados a 45 graus por camada = silhueta redonda com pontas)
    corpo.cubo("barriga_a", (-8.5, 5, -8.5), (17, 5, 17), barriga)
    corpo.cubo("barriga_b", (-8.5, 5.2, -8.5), (17, 4.6, 17), None, rotacao=(0, 45, 0), pivo=(0, 7.5, 0), partilha="barriga_a")
    corpo.cubo("casco_a", (-13, 9, -13), (26, 11, 26), casco)
    corpo.cubo("casco_b", (-13, 9.2, -13), (26, 10.6, 26), None, rotacao=(0, 45, 0), pivo=(0, 14.5, 0), partilha="casco_a")
    corpo.cubo("topo_a", (-10, 19.5, -10), (20, 4.5, 20), topo)
    corpo.cubo("topo_b", (-10, 19.7, -10), (20, 4.1, 20), None, rotacao=(0, 45, 0), pivo=(0, 21.75, 0), partilha="topo_a")
    corpo.cubo("cume", (-6, 23.5, -6), (12, 2.5, 12), cume)
    # lábio: anel de carne viva saliente embaixo da barriga (a boca)
    corpo.cubo("labio_n", (-6.5, 2.4, -7.5), (13, 3, 2.2), labio)
    corpo.cubo("labio_s", (-6.5, 2.4, 5.3), (13, 3, 2.2), labio, partilha="labio_n")
    corpo.cubo("labio_o", (-7.5, 2.4, -7.5), (2.2, 3, 15), labio_lado)
    corpo.cubo("labio_e", (5.3, 2.4, -7.5), (2.2, 3, 15), labio_lado, partilha="labio_o")
    # espinhos: quatro grandes nas laterais, quatro menores no rebordo do topo
    for i, yaw in enumerate((0, 90, 180, 270)):
        corpo.cubo(f"espinho_{i}", (-1.2, 13, -22), (2.4, 2.4, 9.5), espinho, rotacao=(26, yaw, 0), pivo=(0, 14, -13),
                   partilha=None if i == 0 else "espinho_0")
        corpo.cubo(f"espinho_t{i}", (-0.9, 21, -16.5), (1.8, 1.8, 7), espinho, rotacao=(40, yaw + 45, 0), pivo=(0, 21.5, -10),
                   partilha=None if i == 0 else "espinho_t0")
    # dentes: dois anéis de 4 que fecham alternados, dentro do lábio
    for grupo in ("a", "b"):
        d = c.osso(f"dentes_{grupo}", "corpo", (0, 4, 0))
        for k in range(4):
            idx = k * 2 + (0 if grupo == "a" else 1)
            ang = math.radians(idx * 45 + 22.5)
            r = 4.6
            cx, cz = r * math.sin(ang), -r * math.cos(ang)
            yaw = math.degrees(math.atan2(cx, cz))
            d.cubo(f"dente_{idx}", (cx - 1, 1.0, cz - 1), (2, 3.4, 2), dente, rotacao=(28, yaw, 0),
                   pivo=(cx, 4.4, cz), partilha=None if idx == 0 else "dente_0")
    # pernas: coxa sobe para fora, joelho de espinho em cima, canela desce, tarso fino até o chão
    for nome, (px, pz) in PERNAS.items():
        yaw = math.degrees(math.atan2(px, pz))
        coxa = c.osso(f"perna_{nome}", "raiz", (px, 16, pz), (ROT_COXA, yaw, 0))
        coxa.cubo(f"coxa_{nome}", (px - 2, 15, pz - 2), (4, COXA + 1, 4), coxa_pintor, partilha=None if nome == "fd" else "coxa_fd")
        coxa.cubo(f"joelho_{nome}", (px - 1, 16 + COXA - 1, pz - 1), (2, 5, 2), espinho, rotacao=(-30, 0, 0),
                  pivo=(px, 16 + COXA, pz), partilha=None if nome == "fd" else "joelho_fd")
        canela = c.osso(f"canela_{nome}", f"perna_{nome}", (px, 16 + COXA, pz), (ROT_CANELA, 0, 0))
        canela.cubo(f"canela_{nome}", (px - 1.5, 16 + COXA - CANELA, pz - 1.5), (3, CANELA, 3), canela_pintor,
                    partilha=None if nome == "fd" else "canela_fd")
        tarso = c.osso(f"tarso_{nome}", f"canela_{nome}", (px, 16 + COXA - CANELA, pz), (ROT_TARSO, 0, 0))
        tarso.cubo(f"tarso_{nome}", (px - 0.8, 16 + COXA - CANELA - TARSO, pz - 0.8), (1.6, TARSO, 1.6), tarso_pintor,
                   partilha=None if nome == "fd" else "tarso_fd")
    # filhotes: larvas meio enterradas no topo (o acento vermelho)
    c.osso("filhotes", "corpo", (0, 24, 0))
    larvas = [(0, 0, 0, 20, True), (1, -6, 3, 110, False), (2, 6, -3, 250, False),
              (3, 3, 6.5, 320, False), (4, -3.5, -6, 60, False), (5, 7, 4.5, 190, False)]
    for i, cx, cz, yaw, grande in larvas:
        incl = -14 if abs(cx) + abs(cz) > 8 else -4
        o = c.osso(f"filhote_{i}", "filhotes", (cx, 23, cz), (incl, yaw, 0))
        lw, lh, ld = (5, 3.5, 8) if grande else (4, 3, 6.5)
        o.cubo(f"larva_{i}", (cx - lw / 2, 22.6, cz - ld / 2), (lw, lh, ld), larva, partilha=None if (grande or i == 1) else "larva_1")
        o.cubo(f"cabeca_larva_{i}", (cx - 1.5, 22.9, cz - ld / 2 - 2.2), (3, 2.4, 2.4), cabeca_larva,
               partilha=None if i == 0 else "cabeca_larva_0")
        if grande:
            o.cubo("cauda_larva_0", (cx - 1.5, 22.9, cz + ld / 2 - 0.2), (3, 2.2, 2.6), cauda_larva)
    return c


# ---- pintores

def trama(face, fx, fy, fw, fh, semente):
    """Cesta de carne trançada: fiadas de 3 px que serpenteiam (deslocam 1 px a cada fiada), tiras com sombra,
    postes verticais irregulares. Orgânico, não tijolo."""
    fiada = fy // 3
    desloc = (fiada * 2 + int(hash01(fiada, 0, semente) * 3)) % 7
    coluna = (fx + desloc) // 7
    if (fx + desloc) % 7 == 0:
        return cor(FIO)
    posicao = fy % 3
    sobre = (fiada + coluna) % 2 == 0
    i = 1 if posicao == 1 else (2 if posicao == 0 else 3)
    if not sobre:
        i += 1
    if (fx + desloc) % 7 == 1 and not sobre:
        i += 1
    if hash01(fx // 2, fy // 2, semente + 3) < 0.1:
        i -= 1  # brilho úmido
    return sombreado(CESTA, face, fx, fy, fw, fh, semente, base=i, grad=False, borda=False, manchas=0, pintas=(0.03, 0.98))


def casco(face, fx, fy, fw, fh):
    if face in ("up", "down"):
        return sombreado(CESTA, face, fx, fy, fw, fh, 30, base=2)
    return trama(face, fx, fy, fw, fh, 31)


def topo(face, fx, fy, fw, fh):
    if face == "up":
        d = max(abs(fx - fw / 2 + 0.5), abs(fy - fh / 2 + 0.5))
        anel = int(d) % 3
        for (mx, my) in ((10, 10), (4, 13), (16, 7), (13, 16), (6, 4), (17, 14)):  # carne úmida onde as larvas grudam
            if abs(fx - mx) + abs(fy - my) <= 2:
                return tom(SANGUE, 3) if abs(fx - mx) + abs(fy - my) == 2 else tom(SANGUE, 2)
        return sombreado(CESTA, face, fx, fy, fw, fh, 32, base=1 + anel, grad=False)
    if face == "down":
        return tom(CESTA, 4)
    return trama(face, fx, fy, fw, fh, 33)


def cume(face, fx, fy, fw, fh):
    if face == "up":
        return tom(SANGUE, 3) if hash01(fx, fy, 34) < 0.35 else tom(CESTA, 2 if (fx + fy) % 2 else 3)
    return trama(face, fx, fy, fw, fh, 35)


def barriga(face, fx, fy, fw, fh):
    if face == "down":
        dx, dy = fx - fw / 2 + 0.5, fy - fh / 2 + 0.5
        r = math.hypot(dx, dy)
        if r < 3.6:
            return cor(GARGANTA)
        if r < 5.6:
            return tom(SANGUE, 3) if hash01(fx, fy, 36) > 0.3 else cor(GARGANTA)
        if r < 7.2:
            return tom(SANGUE, 2) if hash01(fx, fy, 37) > 0.25 else tom(SANGUE, 1)  # gengiva
        return sombreado(CESTA, face, fx, fy, fw, fh, 38, base=3)
    if face == "up":
        return tom(CESTA, 4)
    return trama(face, fx, fy, fw, fh, 39)


def labio(face, fx, fy, fw, fh):
    if face == "down":
        return tom(SANGUE, 1) if hash01(fx, fy, 40) > 0.3 else tom(SANGUE, 2)
    if face == "south":  # lado de dentro da boca
        return tom(SANGUE, 2) if fy < fh - 1 else tom(SANGUE, 1)
    if face == "north":
        if fy == fh - 1:
            return tom(SANGUE, 1) if hash01(fx, fy, 41) > 0.4 else tom(SANGUE, 2)
        return tom(SANGUE, 2) if hash01(fx, fy, 42) > 0.3 else tom(SANGUE, 3)
    return tom(SANGUE, 2) if fy == fh - 1 else tom(SANGUE, 3)


def labio_lado(face, fx, fy, fw, fh):
    if face == "down":
        return tom(SANGUE, 1) if hash01(fx, fy, 43) > 0.3 else tom(SANGUE, 2)
    if face in ("east", "west"):
        return tom(SANGUE, 2) if hash01(fx, fy, 44) > 0.3 else tom(SANGUE, 3)
    return tom(SANGUE, 2) if fy == fh - 1 else tom(SANGUE, 3)


def espinho(face, fx, fy, fw, fh):
    if face == "north" or (face == "up" and fy == 0):
        return tom(QUITINA, 0)
    return sombreado(QUITINA, face, fx, fy, fw, fh, 45, base=1, grad=False, manchas=0)


def dente(face, fx, fy, fw, fh):
    if face in ("north", "south", "east", "west"):
        if fy == 0:
            return tom(SANGUE, 3)  # gengiva
        return tom(OSSO, 1 if fy == fh - 1 else 2) if hash01(fx, fy, 46) > 0.2 else tom(OSSO, 3)
    return tom(OSSO, 0) if face == "down" else tom(SANGUE, 3)


def coxa_pintor(face, fx, fy, fw, fh):
    if face in ("north", "east") and fx == 0:
        return tom(QUITINA, 0)  # aresta que pega luz
    if fy % 6 == 5:
        return tom(QUITINA, 4)  # juntas
    if fy % 6 == 0 and fx == fw - 1:
        return tom(QUITINA, 3)
    return sombreado(QUITINA, face, fx, fy, fw, fh, 47, base=1, grad=False, manchas=0.15)


def canela_pintor(face, fx, fy, fw, fh):
    if face in ("north", "east") and fx == 0:
        return tom(QUITINA, 0)
    if fy % 7 == 6:
        return tom(QUITINA, 4)
    return sombreado(QUITINA, face, fx, fy, fw, fh, 48, base=2 if fy > fh / 2 else 1, grad=False, manchas=0.15)


def tarso_pintor(face, fx, fy, fw, fh):
    if face == "down":
        return tom(QUITINA, 4)
    return tom(QUITINA, 3 if fy > fh / 2 else 2)


def larva(face, fx, fy, fw, fh):
    if face == "up":
        if fx in (fw // 2, (fw - 1) // 2) and fy % 2 == 1:
            return tom(LARVA, 0)  # linha molhada no dorso
        return tom(LARVA, 1) if fy % 2 == 0 else tom(LARVA, 2)
    if face == "down":
        return tom(LARVA, 3)
    if face in ("east", "west"):
        return tom(LARVA, 2) if fx % 2 == 0 else (tom(LARVA, 1) if fy == 0 else tom(LARVA, 3))  # anéis do corpo
    return tom(LARVA, 2) if fy < fh - 1 else tom(LARVA, 3)


def cabeca_larva(face, fx, fy, fw, fh):
    if face == "north" and fx == 1 and fy == 1:
        return cor(GARGANTA)  # ventosa
    return tom(LARVA, 3) if face != "up" else tom(LARVA, 2)


def cauda_larva(face, fx, fy, fw, fh):
    return tom(LARVA, 2) if face == "up" else tom(LARVA, 3)


# ---- animações (nomes parado/andar/lancar já usados por criaturas/SanguessugaMae.java)

E = "easeInOutSine"
OUT = "easeOutQuad"
IN = "easeInQuad"


def anims():
    def wiggle(i, amp=7, dur=4.0):
        f = 0.35 + 0.17 * i
        return canal((0, (0, 0, 0)), (dur * f * 0.5, (0, amp, 0), E), (dur * f, (0, -amp, 0), E), (dur, (0, 0, 0), E))

    parado_ossos = {
        "corpo": {"scale": canal((0, (1, 1, 1)), (2.0, (1.035, 0.965, 1.035), E), (4.0, (1, 1, 1), E)),
                  "rotation": canal((0, (0, 0, 0)), (1.3, (1, 2.5, 0), E), (2.9, (-1, -2.5, 0), E), (4.0, (0, 0, 0), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (1.0, (0, 0, 0)), (1.12, (0, -0.7, 0), OUT), (1.3, (0, 0, 0), E), (4.0, (0, 0, 0)))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (3.0, (0, 0, 0)), (3.12, (0, -0.7, 0), OUT), (3.3, (0, 0, 0), E), (4.0, (0, 0, 0)))},
        "perna_fd": {"rotation": canal((0, (0, 0, 0)), (2.0, (2, 0, 0), E), (4.0, (0, 0, 0), E))},
        "perna_te": {"rotation": canal((0, (0, 0, 0)), (2.0, (2, 0, 0), E), (4.0, (0, 0, 0), E))},
        "perna_fe": {"rotation": canal((0, (2, 0, 0)), (2.0, (0, 0, 0), E), (4.0, (2, 0, 0), E))},
        "perna_td": {"rotation": canal((0, (2, 0, 0)), (2.0, (0, 0, 0), E), (4.0, (2, 0, 0), E))},
        # uma perna muda de lugar no meio: a criatura nunca fica "congelada"
        "canela_fe": {"rotation": canal((0, (0, 0, 0)), (2.3, (0, 0, 0)), (2.5, (-22, 0, 0), OUT), (2.75, (0, 0, 0), IN), (4.0, (0, 0, 0)))},
    }
    for i in range(6):
        parado_ossos[f"filhote_{i}"] = {"rotation": wiggle(i, 8)}
    parado = animacao(4.0, True, parado_ossos)

    # andar: pares em diagonal (fd+te no ar entre 0 e 0,5; fe+td entre 0,5 e 1,0). A perna no ar sobe de verdade
    # (coxa +18, canela -34) e desce rápido (easeInQuad); o corpo afunda quando o par assenta.
    andar_ossos = {
        "corpo": {"position": canal((0, (0, -1.2, 0)), (0.1, (0, -1.5, 0), OUT), (0.3, (0, 0.4, 0), E), (0.5, (0, -1.2, 0), IN),
                                    (0.6, (0, -1.5, 0), OUT), (0.8, (0, 0.4, 0), E), (1.0, (0, -1.2, 0), IN)),
                  "rotation": canal((0, (1, 0, 3)), (0.5, (-1, 0, -3), E), (1.0, (1, 0, 3), E))},
        "filhotes": {"rotation": canal((0, (0, 0, 3)), (0.12, (0, 0, 4), OUT), (0.5, (0, 0, -3), E), (0.62, (0, 0, -4), OUT), (1.0, (0, 0, 3), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.1, (0, -0.4, 0), OUT), (0.4, (0, 0, 0), E), (1.0, (0, 0, 0)))},
    }
    for nome, (px, pz) in PERNAS.items():
        sgn = 1 if px > 0 else -1
        a = 16 * sgn
        if nome in ("fd", "te"):
            andar_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, -a, 0)), (0.25, (18, 0, 0), E), (0.5, (0, a, 0), IN), (1.0, (0, -a, 0), E))}
            andar_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.22, (-34, 0, 0), OUT), (0.5, (4, 0, 0), IN), (0.6, (0, 0, 0), E), (1.0, (0, 0, 0)))}
            andar_ossos[f"tarso_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.25, (-16, 0, 0), E), (0.5, (6, 0, 0), IN), (0.6, (0, 0, 0), E), (1.0, (0, 0, 0)))}
        else:
            andar_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, a, 0)), (0.5, (0, -a, 0), E), (0.75, (18, 0, 0), E), (1.0, (0, a, 0), IN))}
            andar_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (0.72, (-34, 0, 0), OUT), (1.0, (4, 0, 0), IN))}
            andar_ossos[f"tarso_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.5, (0, 0, 0)), (0.75, (-16, 0, 0), E), (1.0, (6, 0, 0), IN))}
    andar = animacao(1.0, True, andar_ossos)

    # mordida (1,0 s): ergue o corpo por 0,4 s (antecipação: as pernas esticam, os dentes abrem), desaba sobre o
    # alvo em 0,12 s, os anéis fecham um depois do outro, segura, volta.
    mordida_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.4, (0, 4, 0), "easeOutCubic"), (0.52, (0, -3.5, -1.5), "easeInCubic"),
                                   (0.62, (0, -2.6, -1.5), OUT), (0.75, (0, -3, -1.5), E), (1.0, (0, 0, 0), E))},
        "corpo": {"rotation": canal((0, (0, 0, 0)), (0.4, (16, 0, 0), "easeOutCubic"), (0.52, (-16, 0, 0), "easeInCubic"), (0.62, (-12, 0, 0), OUT), (1.0, (0, 0, 0), E)),
                  "scale": canal((0, (1, 1, 1)), (0.4, (0.97, 1.05, 0.97), E), (0.52, (1.06, 0.9, 1.06), IN), (0.7, (1, 1, 1), E), (1.0, (1, 1, 1)))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.4, (0, -1.4, 0), E), (0.56, (0, 0.7, 0), IN), (0.75, (0, 0.7, 0)), (1.0, (0, 0, 0), E)),
                     "scale": canal((0, (1, 1, 1)), (0.4, (1.3, 1, 1.3), E), (0.56, (0.7, 1.15, 0.7), IN), (0.75, (0.7, 1.15, 0.7)), (1.0, (1, 1, 1), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.4, (0, -1.4, 0), E), (0.62, (0, 0.7, 0), IN), (0.75, (0, 0.7, 0)), (1.0, (0, 0, 0), E)),
                     "scale": canal((0, (1, 1, 1)), (0.4, (1.3, 1, 1.3), E), (0.62, (0.7, 1.15, 0.7), IN), (0.75, (0.7, 1.15, 0.7)), (1.0, (1, 1, 1), E))},
        "filhotes": {"position": canal((0, (0, 0, 0)), (0.4, (0, 0.6, 0), E), (0.56, (0, -1, 0), "easeInCubic"), (0.68, (0, 0.3, 0), OUT), (1.0, (0, 0, 0), E))},
    }
    for nome in PERNAS:
        mordida_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.4, (-8, 0, 0), "easeOutCubic"), (0.52, (10, 0, 0), "easeInCubic"), (1.0, (0, 0, 0), E))}
        mordida_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.4, (18, 0, 0), "easeOutCubic"), (0.52, (-20, 0, 0), "easeInCubic"), (1.0, (0, 0, 0), E))}
    mordida = animacao(1.0, False, mordida_ossos)

    # Chuva de Filhotes (1,4 s): empina devagar (0,55 s; pernas da frente no ar, as larvas se agitam), chicoteia
    # para a frente, as larvas voam e somem, o corpo volta ao chão com um quique, as larvas crescem de novo.
    lancar_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.55, (0, 1.5, 1), E), (0.7, (0, -0.8, -1), IN), (0.82, (0, -1.4, -1), OUT),
                                   (0.95, (0, -0.9, -1), E), (1.15, (0, 0, 0), E))},
        "corpo": {"rotation": canal((0, (0, 0, 0)), (0.55, (26, 0, 0), "easeOutCubic"), (0.7, (-20, 0, 0), "easeInCubic"),
                                    (0.9, (-8, 0, 0), OUT), (1.4, (0, 0, 0), E))},
        "filhotes": {"position": canal((0, (0, 0, 0)), (0.55, (0, 1.4, 1.2), E), (0.72, (0, 8, -13), IN), (0.78, (0, 8, -16)), (1.15, (0, 0, 0)), (1.4, (0, 0, 0))),
                     "rotation": canal((0, (0, 0, 0)), (0.3, (0, 6, 0), E), (0.42, (0, -6, 0), E), (0.55, (0, 5, 0), E), (0.72, (-30, 0, 0), IN), (1.15, (0, 0, 0)), (1.4, (0, 0, 0))),
                     "scale": canal((0, (1, 1, 1)), (0.55, (1.12, 1.12, 1.12), E), (0.72, (1, 1, 1)), (0.78, (0, 0, 0)), (1.15, (0, 0, 0)), (1.4, (1, 1, 1), "easeOutBack"))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.55, (0, -0.9, 0), E), (0.95, (0, 0, 0), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.55, (0, -0.9, 0), E), (0.95, (0, 0, 0), E))},
        "perna_fd": {"rotation": canal((0, (0, 0, 0)), (0.55, (22, 0, 0), "easeOutCubic"), (0.8, (-6, 0, 0), IN), (1.15, (0, 0, 0), E))},
        "perna_fe": {"rotation": canal((0, (0, 0, 0)), (0.55, (22, 0, 0), "easeOutCubic"), (0.8, (-6, 0, 0), IN), (1.15, (0, 0, 0), E))},
        "canela_fd": {"rotation": canal((0, (0, 0, 0)), (0.55, (-40, 0, 0), "easeOutCubic"), (0.8, (10, 0, 0), IN), (1.15, (0, 0, 0), E))},
        "canela_fe": {"rotation": canal((0, (0, 0, 0)), (0.55, (-40, 0, 0), "easeOutCubic"), (0.8, (10, 0, 0), IN), (1.15, (0, 0, 0), E))},
        "canela_td": {"rotation": canal((0, (0, 0, 0)), (0.55, (14, 0, 0), E), (1.15, (0, 0, 0), E))},
        "canela_te": {"rotation": canal((0, (0, 0, 0)), (0.55, (14, 0, 0), E), (1.15, (0, 0, 0), E))},
    }
    lancar = animacao(1.4, False, lancar_ossos)

    dano = animacao(0.45, False, {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.1, (0, 0, 2), OUT), (0.45, (0, 0, 0), E))},
        "corpo": {"rotation": canal((0, (0, 0, 0)), (0.1, (10, 0, 0), OUT), (0.45, (0, 0, 0), E)),
                  "scale": canal((0, (1, 1, 1)), (0.1, (0.94, 1.08, 0.94), OUT), (0.45, (1, 1, 1), E))},
        "filhotes": {"rotation": canal((0, (0, 0, 0)), (0.12, (-14, 0, 0), OUT), (0.45, (0, 0, 0), E))},
        "canela_fd": {"rotation": canal((0, (0, 0, 0)), (0.1, (12, 0, 0), OUT), (0.45, (0, 0, 0), E))},
        "canela_fe": {"rotation": canal((0, (0, 0, 0)), (0.1, (12, 0, 0), OUT), (0.45, (0, 0, 0), E))},
    })

    # morte própria (2,8 s): espasmo para cima, as pernas abrem e cedem (as da frente primeiro), a cesta cai,
    # quica, tomba de lado com a boca aberta, duas pernas ainda se mexem, as larvas fogem.
    morrer_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.3, (0, 2, 0), OUT), (0.75, (0, -2, 0), IN), (1.2, (0, -6, 0), "easeInCubic"),
                                   (1.35, (0, -4.8, 0), OUT), (1.55, (0, -6, 0), E), (2.8, (0, -6, 0)))},
        "corpo": {"rotation": canal((0, (0, 0, 0)), (0.3, (14, 0, 0), OUT), (0.75, (4, 0, 4), E), (1.2, (-6, 0, 8), "easeInCubic"),
                                    (1.7, (-8, 0, 30), E), (2.1, (-8, 0, 34), E), (2.8, (-8, 0, 34))),
                  "scale": canal((0, (1, 1, 1)), (1.2, (1, 1, 1)), (1.35, (1.08, 0.86, 1.08), OUT), (2.8, (1.06, 0.88, 1.06), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.3, (0, -1.2, 0), E), (1.2, (0, -1.6, 0)), (1.7, (0, -1, 0), E), (2.8, (0, -1, 0))),
                     "scale": canal((0, (1, 1, 1)), (0.3, (1.35, 1, 1.35), E), (2.8, (1.4, 1, 1.4), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.3, (0, -1.2, 0), E), (1.2, (0, -1.6, 0)), (1.7, (0, -1, 0), E), (2.8, (0, -1, 0))),
                     "scale": canal((0, (1, 1, 1)), (0.3, (1.35, 1, 1.35), E), (2.8, (1.4, 1, 1.4), E))},
    }
    for nome, (px, pz) in PERNAS.items():
        frente = pz < 0
        t_cede = 0.75 if frente else 1.0
        sgn = 1 if px > 0 else -1
        morrer_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.3, (-8, 0, 0), E), (t_cede, (6, 0, 0), E),
                                                            (1.2, (36, 10 * sgn, 0), "easeInCubic"), (1.35, (32, 10 * sgn, 0), OUT), (2.8, (38, 12 * sgn, 0), E))}
        morrer_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.3, (8, 0, 0), E), (t_cede, (-10, 0, 0), E),
                                                             (1.2, (28, 0, 0), "easeInCubic"), (2.8, (34, 0, 0), E))}
    # duas pernas ainda se mexem depois de cair
    morrer_ossos["canela_te"]["rotation"].update(canal((2.0, (34, 0, 0)), (2.15, (18, 0, 0), OUT), (2.35, (34, 0, 0), IN), (2.55, (24, 0, 0), OUT), (2.8, (34, 0, 0), E)))
    morrer_ossos["tarso_fd"] = {"rotation": canal((0, (0, 0, 0)), (1.2, (0, 0, 0)), (1.9, (0, 0, 0)), (2.05, (-30, 0, 0), OUT), (2.3, (0, 0, 0), IN), (2.6, (-18, 0, 0), OUT), (2.8, (0, 0, 0), E))}
    import math as _m
    for i in range(6):
        ang = _m.radians(60 * i + 15)
        dx, dz = 8 * _m.sin(ang), 8 * _m.cos(ang)
        t0 = 1.3 + 0.08 * i
        morrer_ossos[f"filhote_{i}"] = {
            "position": canal((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.45, (dx, -3, dz), IN), (2.8, (dx, -3, dz))),
            "scale": canal((0, (1, 1, 1)), (t0 + 0.3, (1, 1, 1)), (t0 + 0.5, (0, 0, 0), IN), (2.8, (0, 0, 0))),
            "rotation": canal((0, (0, 0, 0)), (t0, (0, 0, 0)), (t0 + 0.45, (0, 90, 0), E), (2.8, (0, 90, 0))),
        }
    morrer = animacao(2.8, False, morrer_ossos)

    # presença: "chocar". Abaixa a cesta até o chão, três pulsos pesados, as larvas rodam e uma desce até a boca
    pulso = lambda t: [(t, (1, 1, 1)), (t + 0.35, (1.09, 0.9, 1.09), OUT), (t + 0.7, (1, 1, 1), E)]
    sobe = lambda t: [(t, (0, 0, 0)), (t + 0.35, (0, 0.9, 0), OUT), (t + 0.7, (0, 0, 0), E)]
    chocar_ossos = {
        "raiz": {"position": canal((0, (0, 0, 0)), (0.8, (0, -3.5, 0), E), (3.5, (0, -3.5, 0)), (4.4, (0, 0, 0), E))},
        "corpo": {"scale": canal(*([(0, (1, 1, 1))] + pulso(1.0) + pulso(1.9) + pulso(2.8) + [(4.4, (1, 1, 1))])),
                  "rotation": canal((0, (0, 0, 0)), (0.8, (6, 0, 0), E), (3.5, (6, 0, 0)), (4.4, (0, 0, 0), E))},
        "filhotes": {"rotation": canal((0, (0, 0, 0)), (1.0, (0, 0, 0)), (3.4, (0, 180, 0), E), (4.4, (0, 180, 0))),
                     "position": canal(*([(0, (0, 0, 0))] + sobe(1.0) + sobe(1.9) + sobe(2.8) + [(4.4, (0, 0, 0))]))},
        "filhote_3": {"position": canal((0, (0, 0, 0)), (1.2, (0, 0, 0)), (2.0, (5, -1, 3), E), (2.6, (6, -9, 3.5), IN), (3.1, (6, -9, 3.5)), (3.6, (0, 0, 0), E))},
        "dentes_a": {"position": canal((0, (0, 0, 0)), (0.8, (0, -0.7, 0), E), (2.6, (0, -0.7, 0)), (2.8, (0, 0.6, 0), IN), (3.1, (0, 0.6, 0)), (3.5, (0, 0, 0), E))},
        "dentes_b": {"position": canal((0, (0, 0, 0)), (0.8, (0, -0.7, 0), E), (2.6, (0, -0.7, 0)), (2.85, (0, 0.6, 0), IN), (3.1, (0, 0.6, 0)), (3.5, (0, 0, 0), E))},
    }
    for nome in PERNAS:
        chocar_ossos[f"perna_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.8, (12, 0, 0), E), (3.5, (12, 0, 0)), (4.4, (0, 0, 0), E))}
        chocar_ossos[f"canela_{nome}"] = {"rotation": canal((0, (0, 0, 0)), (0.8, (-16, 0, 0), E), (3.5, (-16, 0, 0)), (4.4, (0, 0, 0), E))}
    chocar = animacao(4.4, False, chocar_ossos)
    return {"parado": parado, "andar": andar, "mordida": mordida, "lancar": lancar, "dano": dano, "morrer": morrer, "chocar": chocar}
