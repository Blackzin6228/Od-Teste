"""1. Moinho Parado (prólogo do Ato 1). Ficha: lore/fichas-estruturas.md, seção 1. Tamanho 36 x 30 x 26.

Leste = de onde o grupo chega (pela ponte do rio seco). O moinho fica na beira do rio, com a roda d'água parada
no leito sem água e as quatro pás em X viradas para quem chega (uma quebrada, caída no chão).
- Moinho (x 14-22, z 9-17): três andares. Térreo com a mó e sacos de trigo; meio com barris; último andar
  (mais estreito) com a roda de madeira no eixo das pás e a fileira de blocos de nota (o Morse do E1).
- Casa do moleiro (x 2-10, z 20-27): mesa posta para um, cama desfeita, prateleira de cabeças viradas para a
  parede, baú ao pé da cama (Diário p. 1 e metade esquerda do Mapa).
- Celeiro (x 1-9, z 1-9): telhado furado, estandarte da Cruz de Ferro; o espantalho no campo morto olha para o moinho.
- Rio seco (x 23-30): leito de cascalho e lama, ponte de tábua com uma tábua faltando.
"""
import math

from estrutura import Estrutura, Mistura, ruido
import modulos as m
import lugares as L

NOME = "moinho_parado"
TITULO = "1. Moinho Parado"
TAM = (36, 26, 30)
SEMENTE = 1401
G = 2  # chão do mundo
CORTES = [("rio_e_terreo", 0, G + 4), ("meio", G + 5, G + 10), ("ultimo_andar", G + 11, G + 16)]
FRENTE_ISO = 0      # vista de sudeste
FRENTE_GRAUS = 70   # câmera de leste-sudeste, olhando o moinho de frente para as pás
LONGE = {"dist": 62, "altura_olho": 2.5}
RESUMO = """
Prólogo. O grupo chega pelo leste, cruza a ponte do rio seco e vê as pás paradas em X (uma quebrada no chão).
Os blocos de nota do último andar tocam o Morse do E1; o baú do moleiro (cadeado de letras) guarda o Diário p. 1
e a metade esquerda do Mapa. Sem spawn dentro do moinho e da casa (luz invisível fraca). Chão em `y_chao = 2`:
o leito do rio desce 2 blocos.
"""

PEDRA = Mistura(("cobblestone", 6), ("mossy_cobblestone", 2), ("cobbled_deepslate", 2), ("stone_bricks", 1))
TABUA = L.TABUA_ESCURA
LOG_Y = ("stripped_dark_oak_log", {"axis": "y"})
CX, CZ = 18, 13  # eixo do moinho
LEITO = Mistura(("mud", 5), ("gravel", 4), ("coarse_dirt", 2), ("packed_mud", 2), ("cobblestone", 1))


def rio_centro(z):
    return 26.5 + 1.3 * math.sin(z / 4.5)


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    e.y_chao = G
    rio(e)
    moinho(e)
    pas(e)
    roda_dagua(e)
    casa(e)
    celeiro(e)
    exterior(e)
    marcadores(e)
    L.sem_spawn(e, 15, G + 1, 10, 21, G + 15, 16, passo=3)
    L.sem_spawn(e, 3, G + 1, 21, 9, G + 3, 26, passo=3)
    return e


# ---------------------------------------------------------------------------
def rio(e):
    for z in range(TAM[2]):
        c = rio_centro(z)
        for x in range(21, 33):
            d = abs(x + 0.5 - c)
            if d <= 2.6:
                e.por(x, 0, z, LEITO)
                e.ar(x, 1, z)
                e.ar(x, 2, z)
                if e.chance(0.05):
                    e.por(x, 1, z, "dead_bush")
                elif e.chance(0.03):
                    e.por(x, 1, z, "bone_block", axis="x" if e.chance(0.5) else "z")
            elif d <= 3.7:
                e.por(x, 1, z, Mistura(("rooted_dirt", 3), ("dirt", 2), ("mud", 1)))
                e.ar(x, 2, z)
                e.por(x, 0, z, "dirt")
    # pedras grandes no leito
    for x, z in ((26, 3), (27, 4), (25, 26), (28, 27)):
        e.por(x, 1, z, "cobblestone")


def _oct(x, z, x0, z0, x1, z1):
    """Dentro do octógono (quadrado com os cantos cortados)."""
    if not (x0 <= x <= x1 and z0 <= z <= z1):
        return False
    return not ((x in (x0, x1)) and (z in (z0, z1)))


def _borda_oct(x0, z0, x1, z1):
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not _oct(x, z, x0, z0, x1, z1):
                continue
            if any(not _oct(x + dx, z + dz, x0, z0, x1, z1) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                yield x, z


def moinho(e):
    X0, Z0, X1, Z1 = 14, 9, 22, 17
    # fundação e piso do térreo
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if _oct(x, z, X0, Z0, X1, Z1):
                e.por(x, G, z, PEDRA if (x, z) in set(_borda_oct(X0, Z0, X1, Z1)) else
                      Mistura(("spruce_planks", 3), ("cobblestone", 2), ("coarse_dirt", 1)))
                e.por(x, G - 1, z, "cobblestone")
    borda = list(_borda_oct(X0, Z0, X1, Z1))
    # térreo (pedra) e andar do meio (pedra embaixo, madeira em cima)
    for x, z in borda:
        canto = (x in (X0 + 1, X1 - 1) and z in (Z0, Z1)) or (z in (Z0 + 1, Z1 - 1) and x in (X0, X1))
        for y in range(G + 1, G + 11):
            if y <= G + 6:
                b = PEDRA
            elif canto:
                b = LOG_Y
            elif y == G + 10:
                b = ("stripped_dark_oak_log", {"axis": "x" if z in (Z0, Z1) else "z"})
            else:
                b = TABUA
            e.por(x, y, z, b)
    # pisos
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if _oct(x, z, X0, Z0, X1, Z1) and (x, z) not in borda:
                e.por(x, G + 6, z, TABUA)
                e.ar_onde_vazio(x, G + 1, z, x, G + 5, z)
                e.ar_onde_vazio(x, G + 7, z, x, G + 10, z)
    # último andar: mais estreito (7 x 7), em balanço sobre uma cinta de laje
    A0, B0, A1, B1 = 15, 10, 21, 16
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if _oct(x, z, X0, Z0, X1, Z1) and not _oct(x, z, A0, B0, A1, B1):
                e.por(x, G + 11, z, "dark_oak_slab", **L.LAJE)
    borda3 = list(_borda_oct(A0, B0, A1, B1))
    for x in range(A0, A1 + 1):
        for z in range(B0, B1 + 1):
            if _oct(x, z, A0, B0, A1, B1):
                e.por(x, G + 11, z, TABUA)
    for x, z in borda3:
        canto = (x in (A0 + 1, A1 - 1) and z in (B0, B1)) or (z in (B0 + 1, B1 - 1) and x in (A0, A1))
        for y in range(G + 12, G + 15):
            e.por(x, y, z, LOG_Y if canto else ("spruce_planks" if e.chance(0.3) else "dark_oak_planks"))
    for x in range(A0, A1 + 1):
        for z in range(B0, B1 + 1):
            if _oct(x, z, A0, B0, A1, B1) and (x, z) not in borda3:
                e.ar_onde_vazio(x, G + 12, z, x, G + 14, z)
    # capuz: pirâmide íngreme de telha escura, torta para o leste no topo
    for k in range(0, 5):
        a0, b0, a1, b1 = A0 - 1 + k, B0 - 1 + k, A1 + 1 - k, B1 + 1 - k
        y = G + 15 + 2 * k
        for x in range(a0, a1 + 1):
            for z in range(b0, b1 + 1):
                borda_k = x in (a0, a1) or z in (b0, b1)
                if not borda_k:
                    continue
                if x == a0:
                    f = "east"
                elif x == a1:
                    f = "west"
                elif z == b0:
                    f = "south"
                else:
                    f = "north"
                if a1 - a0 <= 1:
                    e.por(x, y, z, "deepslate_tiles")
                    continue
                e.por(x, y, z, "deepslate_tiles")
                e.por(x, y + 1, z, "deepslate_tile_stairs", facing=f, **L.DEGRAU)
        if a1 - a0 <= 1:
            break
    topo = G + 15 + 2 * 4
    e.por(CX, topo - 1, CZ, "deepslate_tiles")
    e.por(CX + 1, topo, CZ, "iron_bars", **L.SEM_LADOS)  # ponta torta
    e.ar_onde_vazio(A0 + 1, G + 15, B0 + 1, A1 - 1, G + 18, B1 - 1)
    # buracos no capuz
    for x, y, z in ((16, G + 18, 11), (20, G + 18, 15), (15, G + 16, 14)):
        e.tirar(x, y, z)
        e.tirar(x, y - 1, z)

    # porta (sul, aberta) e janelas
    m.vao(e, CX, G + 1, Z1, CX, G + 2, Z1)
    m.porta(e, CX, G + 1, Z1, "north", hinge="left", aberta=True)
    e.por(CX, G + 3, Z1, "stripped_dark_oak_log", axis="x")
    for x, z, ao in ((X0, 12, "z"), (X1, 14, "z"), (16, Z0, "x")):
        m.janela(e, x, z, G + 3, G + 4, ao, quebrada=0.5, grade=0.3)
    for x, z, ao in ((X0, 14, "z"), (20, Z1, "x"), (X1, 11, "z")):
        m.janela(e, x, z, G + 8, G + 9, ao, quebrada=0.6)
    # janela redonda do último andar, olhando o rio (sul)
    for dx, dy in ((0, 1), (0, 2), (0, 3), (-1, 2), (1, 2)):
        m.janela(e, CX + dx, B1, G + 12 + dy - 1, G + 12 + dy - 1, "x", quebrada=0.2)
    m.janela(e, A0, 13, G + 13, G + 14, "z", quebrada=0.7)

    # interior: térreo
    for y in range(G + 1, G + 16):
        if e.vazio(CX, y, CZ) or e.nome(CX, y, CZ) in ("minecraft:dark_oak_planks", "minecraft:spruce_planks"):
            e.por(CX, y, CZ, "stripped_dark_oak_log", axis="y")  # eixo vertical
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if dx or dz:
                e.por(CX + dx, G + 1, CZ + dz, "smooth_stone_slab", **L.LAJE)  # a mó
    e.por(CX, G + 1, CZ, "smooth_stone")
    for x, z in ((15, 11), (15, 12), (16, 10), (15, 15)):
        e.por(x, G + 1, z, "hay_block", axis="y")
    e.por(15, G + 2, 11, "hay_block", axis="x")
    e.por(21, G + 1, 15, "barrel", facing="up", open=False)
    e.por(21, G + 1, 16 - 1, "barrel", facing="west", open=False)
    e.por(20, G + 1, 10, "grindstone", face="floor", facing="north")
    # escada de mão (parede norte), furos nos pisos
    for y in range(G + 1, G + 12):  # passa pelos furos dos pisos
        e.por(17, y, 10, "ladder", facing="south", waterlogged=False)
    # andar do meio: barris, sacos, lanterna caída
    for x, z in ((15, 14), (15, 15), (21, 12), (20, 16)):
        e.por(x, G + 7, z, "barrel", facing="up", open=False)
    e.por(16, G + 7, 16, "hay_block", axis="z")
    e.por(21, G + 7, 14, "lantern", hanging=False, waterlogged=False)
    m.vela(e, 15, G + 8, 14, 1, acesa=False)
    # último andar: eixo das pás, roda de madeira, blocos de nota
    for x in range(CX, 23):
        e.por(x, G + 13, CZ, "stripped_dark_oak_log", axis="x")
    for dy in (-2, -1, 0, 1, 2):
        for dz in (-2, -1, 0, 1, 2):
            if abs(dy) == 2 and abs(dz) == 2:
                continue
            if max(abs(dy), abs(dz)) == 2 or (dy == 0) != (dz == 0) and max(abs(dy), abs(dz)) == 1:
                y, z = G + 13 + dy, CZ + dz
                if G + 12 <= y <= G + 15 and e.vazio(20, y, z):
                    if abs(dy) == 2 or abs(dz) == 2:
                        e.por(20, y, z, "dark_oak_fence", **L.SEM_LADOS)
                    else:
                        e.por(20, y, z, "stripped_spruce_log", axis="x")
    notas = []
    for i, x in enumerate(range(16, 21)):
        grave = i % 2 == 0
        e.por(x, G + 12, 11, "note_block", instrument="bass", note=3 if grave else 20, powered=False)
        notas.append([x, G + 12, 11])
        e.pista(x, G + 12, 11, "nota", f"Bloco de nota {i + 1} de 5 ({'grave = traço' if grave else 'agudo = ponto'}).")
    e.por(16, G + 12, 15, "barrel", facing="up", open=False)
    m.vela(e, CX, G + 12, 15, 1, acesa=True, cor="red")  # luz onde não devia ter ninguém (vista de longe)
    e.por(20, G + 12, 15, "cobweb")
    # sangue: pouco, só um rastro seco subindo a escada
    m.rastro(e, [(17, 12), (17, 11)], G + 7, falha=0.2)
    # teias
    m.teias(e, 15, G + 3, 10, 21, G + 5, 16, 8)
    m.teias(e, 15, G + 8, 10, 21, G + 10, 16, 8)
    m.teias(e, 16, G + 14, 11, 20, G + 15, 15, 6)


def pas(e):
    """Quatro pás em X no plano x = 24, viradas para o leste. A de baixo-sul quebrou e caiu."""
    hy, hz, px = G + 13, CZ, 24
    e.por(23, hy, hz, "stripped_dark_oak_log", axis="x")
    e.por(px, hy, hz, "dark_oak_planks")
    for sy, sz, comp in ((1, 1, 9), (1, -1, 9), (-1, -1, 8), (-1, 1, 3)):
        for i in range(1, comp + 1):
            y, z = hy + sy * i, hz + sz * i
            if not e.dentro(px, y, z):
                break
            e.por(px, y, z, "spruce_log", axis="y")
            if i >= 3:
                # vela de lã do lado de dentro do giro, com rasgos
                for k in (1, 2):
                    for (a, b) in ((y, z - sz * k), (y - sy * k, z)):
                        if e.dentro(px, a, b) and e.vazio(px, a, b) and not e.chance(0.12):
                            r = e.rng.random()
                            e.por(px, a, b, "gray_wool" if r < 0.6 else ("black_wool" if r < 0.85 else "light_gray_wool"))
                if i % 3 == 0:
                    e.por(px + 1, y, z, "dark_oak_fence", **L.SEM_LADOS)
    # a pá quebrada, no chão perto da roda
    for x, z in ((26, 4), (27, 5), (28, 6), (29, 7), (30, 8)):
        e.por(x, 1 if 24 <= x <= 29 else G + 1, z, "stripped_spruce_log", axis="x")
    for x, z in ((27, 4), (28, 5), (29, 6)):
        if e.vazio(x, 2, z):
            if e.cheio(x, 1, z):
                e.por(x, 2, z, "light_gray_carpet")


def roda_dagua(e):
    """Roda d'água parada, metade dentro do leito seco (plano x = 23 e 24)."""
    cy, cz, r = G + 1, CZ, 4
    for x in (23, 24):
        for y in range(0, cy + r + 1):
            for z in range(cz - r, cz + r + 1):
                d = math.hypot(y - cy, z - cz)
                if r - 0.6 <= d <= r + 0.4:
                    e.por(x, y, z, "dark_oak_planks" if x == 23 else "spruce_planks")
                elif d < r - 0.6 and (y == cy or z == cz):
                    e.por(x, y, z, "stripped_dark_oak_log", axis="z" if y == cy else "y")
                elif d < r - 0.6:
                    e.ar(x, y, z)
    for ang in range(0, 360, 45):
        y = round(cy + (r + 1) * math.sin(math.radians(ang)))
        z = round(cz + (r + 1) * math.cos(math.radians(ang)))
        if 0 <= y < TAM[1] and e.dentro(24, y, z):
            e.por(24, y, z, "spruce_slab", **L.LAJE)


def casa(e):
    X0, Z0, X1, Z1 = 2, 20, 10, 27
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, PEDRA if x in (X0, X1) or z in (Z0, Z1) else
                  Mistura(("spruce_planks", 5), ("dark_oak_planks", 3), ("coarse_dirt", 1)))

    def parede(x, y, z, canto):
        if y == G + 1:
            return PEDRA
        if canto or (z in (Z0, Z1) and x == 6):
            return LOG_Y
        if y == G + 4:
            return ("stripped_dark_oak_log", {"axis": "x" if z in (Z0, Z1) else "z"})
        return TABUA

    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 4, parede)
    alt = L.telhado(e, Z0 - 1, Z1 + 1, X0 - 1, X1 + 1, G + 5, "x", "dark_oak_stairs", "dark_oak_planks",
                    ("dark_oak_slab", L.LAJE), inc=2, buracos=0.03)
    for x in (X0, X1):
        L.empena(e, "x", x, Z0 - 1, Z1 + 1, G + 5, 2, lambda a, y: LOG_Y if a == 23 + 1 else TABUA)
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 4, Z1 - 1)
    e.ar_onde_vazio(X0 + 1, G + 5, Z0 + 1, X1 - 1, G + 9, Z1 - 1, condicao=lambda x, y, z: y < alt(z) - 1)
    # porta aberta (norte), janelas pequenas
    m.porta(e, 6, G + 1, Z0, "south", hinge="right", aberta=True)
    m.janela(e, 4, Z0, G + 2, G + 3, "x", quebrada=0.4)
    m.janela(e, 8, Z0, G + 2, G + 2, "x", quebrada=0.6)
    m.janela(e, X1, 24, G + 2, G + 3, "z", quebrada=0.5, grade=0.5)
    m.janela(e, 6, Z1, G + 6, G + 7, "x", quebrada=0.5)
    # chaminé torta (oeste)
    for y in range(G + 1, G + 12):
        e.por(X0 - 1, y, 23, Mistura(("cobblestone", 3), ("mossy_cobblestone", 1)))
        if y < G + 9:
            e.por(X0 - 1, y, 24, Mistura(("cobblestone", 3), ("mossy_cobblestone", 1)))
    e.por(X0, G + 1, 23, "campfire", facing="east", lit=False, signal_fire=False, waterlogged=False)
    # interior: mesa posta para um
    m.mesa(e, 4, 22, 4, 23, G + 1, "dark_oak_trapdoor")
    e.por(4, G + 2, 22, "white_carpet")
    e.por(4, G + 2, 23, "white_carpet")
    m.vela(e, 4, G + 3, 23, 1, acesa=False)
    e.por(4, G + 3, 22, "flower_pot")
    m.cadeira(e, 5, G + 1, 22, "west", "dark_oak_stairs")
    e.por(3, G + 1, 24, "dark_oak_stairs", facing="north", half="top", shape="straight", waterlogged=False)
    # cama desfeita (canto sudeste), cobertor caído no chão
    m.cama(e, 9, G + 1, 25, "north", "light_gray")
    e.por(8, G + 1, 26, "light_gray_carpet")
    e.por(8, G + 1, 25, "light_gray_carpet")
    L.bau(e, 9, G + 1, 26, "west", "moleiro_diario",
          "Ao pé da cama. Cadeado de letras (palavra do E1). Diário do Açougueiro p. 1 + metade esquerda do Mapa "
          "Rasgado; na tampa, a carvão, o endereço do Atlas.")
    # prateleira com cabeças viradas para a parede (leste)
    for z in range(21, 25):
        e.por(9, G + 2, z, "dark_oak_slab", **L.LAJE_TOPO)
    for z in range(21, 25):
        e.por(9, G + 3, z, "skeleton_skull", rotation=12, powered=False)
    e.por(3, G + 1, 26, "barrel", facing="up", open=False)
    e.por(3, G + 1, 21, "crafting_table")
    m.teias(e, 3, G + 3, 21, 9, G + 4, 26, 6)
    m.lanterna_pendurada(e, 6, G + 4, 24, com_lanterna=False, comprimento_corrente=1)
    m.vela(e, 7, G + 1, 21, 2, acesa=True, cor="red")  # a única luz: vela vermelha no chão


def celeiro(e):
    X0, Z0, X1, Z1 = 1, 1, 9, 9
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, Mistura(("coarse_dirt", 3), ("dirt", 2), ("spruce_planks", 1)))

    def parede(x, y, z, canto):
        if canto or (x in (X0, X1) and z == 5):
            return ("spruce_log", {"axis": "y"})
        if y == G + 1:
            return PEDRA
        return Mistura(("dark_oak_planks", 5), ("spruce_planks", 2))

    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 5, parede)
    alt = L.telhado(e, X0 - 1, X1 + 1, Z0 - 1, Z1 + 1, G + 6, "z", "spruce_stairs", "spruce_planks",
                    ("spruce_slab", L.LAJE), inc=2, buracos=0.16, protegido=lambda z: z in (0, 10))
    for z in (Z0, Z1):
        L.empena(e, "z", z, X0 - 1, X1 + 1, G + 6, 2, Mistura(("dark_oak_planks", 4), ("spruce_planks", 1)))
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 13, Z1 - 1, condicao=lambda x, y, z: y < alt(x) - 1)
    # portão grande (sul) escancarado, uma folha caída
    m.vao(e, 4, G + 1, Z1, 6, G + 3, Z1)
    for x in (4, 5):  # a folha caída
        e.por(x, G + 1, Z1 + 1, "spruce_trapdoor", facing="north", half="bottom", open=False, powered=False,
              waterlogged=False)
    # feno, carroça quebrada, estandarte
    for x, z, h in ((2, 2, 2), (3, 2, 1), (2, 3, 1), (8, 2, 2), (8, 3, 1), (7, 2, 1)):
        for y in range(h):
            e.por(x, G + 1 + y, z, "hay_block", axis="y")
    L.estandarte_cruz(e, 5, G + 4, Z0 + 1, facing="south")
    e.pista(5, G + 4, Z0 + 1, "estandarte", "Cruz de Ferro rasgada (provisória: o desenho final é do Modelador).")
    e.por(3, G + 1, 7, "damaged_anvil", facing="east")
    e.por(8, G + 1, 7, "composter", level=2)
    m.teias(e, 2, G + 4, 2, 8, G + 7, 8, 8)


def exterior(e):
    # chão morto em quase tudo (menos rio e prédios, que já têm chão)
    L.chao_morto(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, borda_irregular=3)
    # campo morto e espantalho virado para o moinho
    for x in range(2, 12):
        for z in range(11, 18):
            if (x + z) % 2 == 0 or z % 2 == 0:
                e.por(x, G, z, "farmland", moisture=0)
                if e.chance(0.35):
                    e.por(x, G + 1, z, "dead_bush")
                elif e.chance(0.2):
                    e.por(x, G + 1, z, "wheat", age=7)
    L.espantalho(e, 8, G + 1, 14, "east")
    # caminho: da ponte ao moinho e à casa
    caminho = [(35, 21), (30, 21), (23, 20), (19, 19), (18, 18)]
    ramo = [(19, 19), (12, 19), (6, 19)]
    for pts in (caminho, ramo):
        for (xa, za), (xb, zb) in zip(pts, pts[1:]):
            n = max(abs(xb - xa), abs(zb - za))
            for i in range(n + 1):
                x = round(xa + (xb - xa) * i / n)
                z = round(za + (zb - za) * i / n)
                for dz in (0, 1):
                    if e.dentro(x, G, z + dz) and e.nome(x, G, z + dz) not in (None,) and \
                            e.nome(x, G, z + dz).split(":")[1] in ("coarse_dirt", "podzol", "dirt", "rooted_dirt",
                                                                    "mud", "packed_mud", "gravel"):
                        e.por(x, G, z + dz, "dirt_path" if e.chance(0.7) else "coarse_dirt")
    # ponte de tábua sobre o rio (z 20-22), uma tábua faltando
    for x in range(22, 32):
        for z in (20, 21, 22):
            if (x, z) == (27, 21):
                continue
            e.por(x, G, z, "spruce_planks" if e.chance(0.7) else "dark_oak_planks")
        if x % 3 == 0:
            for z in (20, 22):
                for y in range(0, G):
                    e.por(x, y, z, "spruce_log", axis="y")
    L.cerca(e, [(22, 20), (31, 20)], G + 1, falha=0.3)
    L.cerca(e, [(22, 22), (31, 22)], G + 1, falha=0.35)
    e.ar(27, G + 1, 20)
    e.ar(28, G + 1, 22)
    # árvores mortas
    L.arvore_morta(e, 33, G + 1, 4, 9, galhos=4)
    L.arvore_morta(e, 32, G + 1, 27, 7, galhos=3)
    L.arvore_morta(e, 12, G + 1, 25, 6, galhos=3, tronco="spruce_log")
    L.arvore_morta(e, 1, G + 1, 15, 8, galhos=4)
    # cerca velha na margem
    L.cerca(e, [(21, 1), (21, 7)], G + 1, falha=0.4)
    L.cerca(e, [(21, 24), (21, 29)], G + 1, falha=0.4)
    # pilha de lenha e um balde de pedra (cocho seco)
    for z in (3, 4):
        for y in (G + 1, G + 2):
            e.por(12, y, z, "spruce_log", axis="x")
    e.por(13, G + 1, 19, "cauldron")
    L.mato_seco(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, chance=0.06)


def marcadores(e):
    L.ponto(e, 18, G + 14, 12, "som:moinho_morse",
            "Os 5 blocos de nota do último andar (x 16-20, y 14, z 11) tocam o Morse do E1. A roda trava no meio; "
            "inteira só numa vigília de noite toda.")
    L.ponto(e, 19, G + 15, 14, "som:roda_rangendo", "A roda de madeira do eixo das pás range sozinha.")
    L.ponto(e, 7, G + 4, 23, "evento:cabecas_moleiro",
            "Prateleira de cabeças viradas para a parede (x 9, z 21-24): o mod deixa uma por jogador e tira o resto.")
    L.ponto(e, 18, G + 3, 13, "evento:cheiro",
            "Centro do Cheiro (névoa avermelhada leve que cresce com o tempo). Correntes arrastando ao longe.")
    L.ponto(e, 34, G + 2, 21, "evento:chegada", "Pé da ponte, lado leste: o grupo chega por aqui.")
