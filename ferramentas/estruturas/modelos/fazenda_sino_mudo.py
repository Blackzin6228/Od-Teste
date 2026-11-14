"""5. Fazenda do Sino Mudo (clímax do Ato 1). Ficha: lore/fichas-estruturas.md, seção 5. Tamanho 72 x 60 x 18,
mais 6 de porão e cripta.

Aproveita o estilo e o código da capela_fazenda.py (o reboco de cal gasta, a espadana com o sino, os bancos, as
velas vermelhas do altar), agora no tamanho da ficha. Sul = portão da fazenda.
- Casarão (x 4-33, z 33-49): varanda larga, sala de visitas, sala de jantar (mesa comprida, cadeiras tombadas),
  cozinha; em cima, quatro quartos de cores diferentes e o escritório (Diário p. 4); sótão com o quarto fechado
  (retrato virado, baú vazio com a marca do terço); porão (adega) com a passagem para a cripta.
- Capela da Mesa (x 45-60, z 8-27): nave com a Mesa do Pacto no meio (um lugar por jogador e um a mais), altar
  que gira, vitrais escuros com velas vermelhas acesas (de longe, a capela brilha vermelho), portas fechadas.
  Cripta (embaixo do altar): túmulos e a vela acesa há muito tempo.
- Terreiro: árvore grande seca no meio, o sino do terreiro num poste, poço, galpão, curral, cemitério pequeno,
  caminho de pedra, cerca em volta com o portão aberto.
"""
from estrutura import Estrutura, Mistura, ruido
import modulos as m
import lugares as L
from modelos.capela_fazenda import reboco as reboco_capela

NOME = "fazenda_sino_mudo"
TITULO = "5. Fazenda do Sino Mudo"
TAM = (72, 24, 60)
SEMENTE = 1405
G = 6
CORTES = [("porao_e_cripta", 0, G - 2), ("terreo", G, G + 4), ("andar_de_cima", G + 5, G + 9),
          ("sotao", G + 10, G + 15)]
FRENTE_ISO = 0
FRENTE_GRAUS = 25
LONGE = {"dist": 95, "altura_olho": 2.5}
RESUMO = """
Clímax do Ato 1. Entra-se pelo portão da fazenda (sul, aberto). A capela fica **de portas fechadas** até o sino da
Torre das Correntes ser calado (`evento:portas_capela`); antes disso o sino do terreiro toca. Dentro da capela o chat
de texto fica bloqueado (`evento:area_capela`). A Mesa do Pacto tem 12 lugares (`lugar:01` a `lugar:12`, o mod usa
um por jogador) e o lugar a mais na cabeceira (`spawn:convidada`). Aldeões de uma vila próxima aparecem parados na
cerca (`spawn:aldeao_cerca_*`). Sem monstro. O Evento do Diabo fica para depois (decisão de 14/11). Chão em
`y_chao = 6`: porão e cripta ocupam y 0 a 5.
"""

PEDRA = Mistura(("cobblestone", 5), ("cobbled_deepslate", 2), ("mossy_cobblestone", 1), ("stone_bricks", 2))
TABUA = L.TABUA_ESCURA
LOG_Y = ("stripped_dark_oak_log", {"axis": "y"})
BASE_CAPELA = Mistura(("stone_bricks", 6), ("cracked_stone_bricks", 3), ("mossy_stone_bricks", 1))
PISO_NAVE = Mistura(("polished_andesite", 6), ("white_terracotta", 4), ("stone_bricks", 3),
                    ("cracked_stone_bricks", 2), ("light_gray_terracotta", 2))
PEDRA_CRIPTA = Mistura(("stone_bricks", 3), ("cracked_stone_bricks", 3), ("mossy_stone_bricks", 1),
                       ("cobbled_deepslate", 2), ("polished_blackstone_bricks", 1))
CX0, CX1, CZ0, CZ1 = 45, 60, 8, 27   # capela


def reboco(x, y, z):
    """O reboco da capela_fazenda, mais sujo: escorridos de umidade (andesito) que descem do beiral."""
    escorre = ruido(x, 0, z, 1.3, SEMENTE + 11) > 0.62 and ruido(x, y, z, 4.0, SEMENTE + 12) > 0.3
    if escorre:
        return "andesite" if y % 3 else "diorite"
    if ruido(x, y, z, 2.0, SEMENTE + 13) < 0.25:
        return "cracked_stone_bricks"
    return reboco_capela(x, y, z)
HX0, HX1, HZ0, HZ1 = 4, 33, 33, 45   # casarão (corpo); varanda até z 49


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    e.y_chao = G
    porao_e_tunel(e)
    cripta(e)
    casarao(e)
    capela(e)
    terreiro(e)
    marcadores(e)
    L.sem_spawn(e, HX0 + 1, G + 1, HZ0 + 1, HX1 - 1, G + 1, HZ1 - 1, passo=5)
    L.sem_spawn(e, HX0 + 1, G + 6, HZ0 + 1, HX1 - 1, G + 6, HZ1 - 1, passo=5)
    L.sem_spawn(e, HX0 + 2, G + 11, HZ0 + 1, HX1 - 2, G + 11, HZ1 - 1, passo=5)
    L.sem_spawn(e, CX0 + 1, G + 1, CZ0 + 1, CX1 - 1, G + 1, CZ1 - 1, passo=5)
    L.sem_spawn(e, 5, 1, 34, 59, 1, 44, passo=4)
    L.sem_spawn(e, 47, 1, 9, 58, 1, 16, passo=4)
    return e


# ---------------------------------------------------------------------------
def porao_e_tunel(e):
    X0, Z0, X1, Z1 = 6, 35, 20, 43
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, 0, z, PEDRA if x in (X0, X1) or z in (Z0, Z1) else Mistura(("cobblestone", 2), ("mud", 1),
                                                                             ("stone_bricks", 2)))
    m.paredes(e, X0, Z0, X1, Z1, 1, G - 1, PEDRA)
    e.ar_onde_vazio(X0 + 1, 1, Z0 + 1, X1 - 1, G - 1, Z1 - 1)
    for x in range(X0 + 1, X1, 2):
        for z in (Z0 + 1, Z1 - 1):
            e.por(x, 1, z, "barrel", facing="north" if z == Z0 + 1 else "south", open=False)
            if e.chance(0.6):
                e.por(x, 2, z, "barrel", facing="north" if z == Z0 + 1 else "south", open=False)
    m.lanterna_pendurada(e, 13, G - 2, 39, comprimento_corrente=1, com_lanterna=False)
    # túnel até a cripta: para o leste em z 38-40, depois para o norte em x 50-52
    def tunel(x, z, eixo):
        for y in range(0, 5):
            for d in (-1, 0, 1):
                px, pz = (x, z + d) if eixo == "x" else (x + d, z)
                borda = d != 0 or y in (0, 4)
                if not e.dentro(px, y, pz):
                    continue
                if (px, y, pz) in e.blocos and e.nome(px, y, pz) == "minecraft:air":
                    continue
                if borda:
                    e.por(px, y, pz, PEDRA_CRIPTA)
                else:
                    e.ar(px, y, pz)
        for y in (1, 2, 3):
            if eixo == "x":
                e.ar(x, y, z)
    for x in range(X1, 52):
        tunel(x, 39, "x")
        for y in (1, 2, 3):
            e.ar(x, y, 39)
    for z in range(16, 41):
        tunel(51, z, "z")
        for y in (1, 2, 3):
            e.ar(51, y, z)
    for x in (30, 40):
        m.lanterna_pendurada(e, x, 3, 39, com_lanterna=False)
    m.teias(e, 21, 2, 38, 50, 3, 40, 10)
    m.teias(e, 7, 3, 36, 19, G - 1, 42, 10)


def cripta(e):
    X0, Z0, X1, Z1 = 47, 9, 58, 16
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, 0, z, PEDRA_CRIPTA)
    m.paredes(e, X0, Z0, X1, Z1, 1, G - 1, PEDRA_CRIPTA)
    e.ar_onde_vazio(X0 + 1, 1, Z0 + 1, X1 - 1, G - 1, Z1 - 1)
    for y in (1, 2, 3):
        e.ar(51, y, Z1)
    # túmulos (caixas de andesito polido com tampa de laje) e nichos na parede
    for x in (49, 53, 56):
        for z in (11, 12):
            e.por(x, 1, z, "polished_andesite")
            e.por(x, 2, z, "smooth_stone_slab", **L.LAJE)
    # o nicho da vela acesa há muito tempo e da carta com selo (parede norte)
    e.ar(52, 2, Z0)
    e.ar(52, 3, Z0)
    e.por(52, 1, Z0, "stone_bricks")
    m.vela(e, 52, 2, Z0, 1, acesa=True)
    L.bau(e, 53, 1, Z0 + 1, "south", "carta_selo",
          "Carta com o selo de cera (Pista do Ato 2). Só aparece depois do Evento do Diabo.")
    # escada estreita para a nave (atrás do altar)
    for i in range(5):
        e.por(57, 1 + i, 15 - i, "stone_brick_stairs", facing="north", **L.DEGRAU)
        for y in range(1, 1 + i):
            e.por(57, y, 15 - i, "stone_bricks")
        for y in range(2 + i, G + 1):
            if y <= G - 1:
                e.ar(57, y, 15 - i)
    for z in (10, 11):
        e.ar(57, G, z)
    m.teias(e, 48, 3, 10, 57, G - 1, 15, 10)
    m.mancha(e, 52, 1, 13, 1, 0.4)


def casarao(e):
    X0, Z0, X1, Z1 = HX0, HZ0, HX1, HZ1
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, PEDRA if x in (X0, X1) or z in (Z0, Z1) else
                  Mistura(("spruce_planks", 5), ("dark_oak_planks", 5)))
    # térreo: base de pedra e reboco gasto; em cima, madeira escura
    def parede(x, y, z, canto):
        if canto or (z in (Z0, Z1) and x in (9, 14, 19, 24, 29)) or (x in (X0, X1) and z == 39):
            return LOG_Y
        if y == G + 1:
            return PEDRA
        if y == G + 5:
            return ("stripped_dark_oak_log", {"axis": "x" if z in (Z0, Z1) else "z"})
        if y < G + 5:
            return reboco(x, y, z) if ruido(x, y, z, 3, 7) > 0.35 else "diorite"
        return Mistura(("dark_oak_planks", 5), ("spruce_planks", 2))

    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 9, parede)
    for x in range(X0 + 1, X1):
        for z in range(Z0 + 1, Z1):
            e.por(x, G + 5, z, TABUA)
            e.por(x, G + 10, z, TABUA)
    alt = L.telhado(e, Z0 - 1, Z1 + 1, X0 - 1, X1 + 1, G + 10, "x", "deepslate_tile_stairs", "dark_oak_planks",
                    ("deepslate_tile_slab", L.LAJE), inc=1, buracos=0.03)
    for x in (X0, X1):
        L.empena(e, "x", x, Z0 - 1, Z1 + 1, G + 10, 1, Mistura(("dark_oak_planks", 5), ("spruce_planks", 2)))
    # duas trapeiras na frente (sul) e chaminés
    for xd in (11, 26):
        for x in range(xd - 1, xd + 2):
            for y in range(G + 11, G + 13):
                e.por(x, y, Z1 + 1, "dark_oak_planks" if x != xd else "black_stained_glass_pane",
                      **({} if x != xd else dict(east=True, west=True, north=False, south=False, waterlogged=False)))
            e.por(x, G + 13, Z1 + 1, "deepslate_tile_slab", **L.LAJE)
        for z in range(Z1 - 1, Z1 + 1):
            for y in range(G + 11, G + 13):
                e.ar(xd, y, z)
    for x, z in ((X0 + 2, 39), (X1 - 2, 37)):
        for y in range(G + 1, G + 18):
            e.por(x, y, z, Mistura(("bricks", 2), ("cobblestone", 2), ("cobbled_deepslate", 1)))
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 4, Z1 - 1)
    e.ar_onde_vazio(X0 + 1, G + 6, Z0 + 1, X1 - 1, G + 9, Z1 - 1)
    e.ar_onde_vazio(X0 + 1, G + 11, Z0 + 1, X1 - 1, G + 16, Z1 - 1, condicao=lambda x, y, z: y < alt(z))
    # varanda larga (z 46-49)
    for x in range(X0, X1 + 1):
        for z in range(Z1 + 1, Z1 + 5):
            e.por(x, G, z, Mistura(("spruce_planks", 4), ("dark_oak_planks", 3)))
            if z < Z1 + 4 or x % 2:
                e.por(x, G + 5, z, "dark_oak_slab", **L.LAJE)
        if x % 5 == 4:
            for y in range(G + 1, G + 5):
                e.por(x, y, Z1 + 4, "dark_oak_fence", **L.SEM_LADOS)
    for x in range(X0, X1 + 1):
        if x % 5 != 4 and e.chance(0.75):
            e.por(x, G + 1, Z1 + 4, "dark_oak_fence", east=True, west=True, north=False, south=False,
                  waterlogged=False)
    for x in range(16, 20):
        e.ar(x, G + 1, Z1 + 4)
        e.por(x, G, Z1 + 5, "dark_oak_stairs", facing="north", **L.DEGRAU)
    m.cadeira(e, 8, G + 1, Z1 + 2, "south", "dark_oak_stairs")
    e.por(9, G + 1, Z1 + 2, "dark_oak_stairs", facing="west", half="top", shape="straight", waterlogged=False)
    # portas e janelas
    m.porta(e, 17, G + 1, Z1, "north", hinge="left", aberta=True)
    m.porta(e, 18, G + 1, Z1, "north", hinge="right", aberta=False)
    for x in (6, 11, 23, 27, 31):
        m.janela(e, x, Z1, G + 2, G + 3, "x", quebrada=0.4)
        m.janela(e, x, Z1, G + 7, G + 8, "x", quebrada=0.4)
    for x in (7, 16, 25, 30):
        m.janela(e, x, Z0, G + 2, G + 3, "x", quebrada=0.4, grade=0.3)
        m.janela(e, x, Z0, G + 7, G + 8, "x", quebrada=0.4)
    for z in (36, 42):
        for x in (X0, X1):
            m.janela(e, x, z, G + 2, G + 3, "z", quebrada=0.4)
            m.janela(e, x, z, G + 7, G + 8, "z", quebrada=0.4)
    # paredes internas do térreo (x 14 e 24) com vãos
    for x in (14, 24):
        for z in range(Z0 + 1, Z1):
            for y in range(G + 1, G + 5):
                e.por(x, y, z, Mistura(("dark_oak_planks", 3), ("spruce_planks", 1)))
        m.vao(e, x, G + 1, 40, x, G + 2, 41)
    # sala de visitas (oeste): poltronas, estante, tapete, lareira
    for z in (37, 38, 39, 40, 41):
        e.por(X0 + 1, G + 1, z, "bricks" if z != 39 else "campfire", **({} if z != 39 else dict(
            facing="east", lit=False, signal_fire=False, waterlogged=False)))
    for x in range(7, 12):
        for z in range(37, 42):
            if (x + z) % 5:
                e.por(x, G + 1, z, "brown_carpet")
    m.cadeira(e, 8, G + 1, 36, "south", "dark_oak_stairs")
    m.cadeira(e, 11, G + 1, 36, "south", "dark_oak_stairs")
    for x in (6, 7, 8, 9):
        e.por(x, G + 1, Z1 - 1, "bookshelf")
        if x != 8:
            e.por(x, G + 2, Z1 - 1, "bookshelf")
    # sala de jantar (meio): mesa comprida, cadeiras tombadas
    m.mesa(e, 16, 38, 22, 39, G + 1, "dark_oak_trapdoor")
    for x in range(16, 23):
        for z, olha in ((37, "south"), (40, "north")):
            if e.chance(0.5):
                m.cadeira(e, x, G + 1, z, olha, "dark_oak_stairs")
            else:
                e.por(x, G + 1, z, "dark_oak_stairs", facing=olha, half="top", shape="straight", waterlogged=False)
    for x in (17, 20):
        m.vela(e, x, G + 2, 38, 2, acesa=False)
    e.por(19, G + 2, 39, "flower_pot")
    m.lanterna_pendurada(e, 19, G + 4, 38, com_lanterna=False, comprimento_corrente=0)
    # cozinha (leste): fornalha, defumador, barris, caldeirão; escada para o porão
    for z, b in ((35, "furnace"), (36, "smoker"), (37, "crafting_table"), (38, "cauldron")):
        e.por(X1 - 1, G + 1, z, b, **({"facing": "west", "lit": False} if b in ("furnace", "smoker") else {}))
    for x in (26, 27):
        e.por(x, G + 1, Z1 - 1, "barrel", facing="up", open=False)
    m.mesa(e, 28, 40, 29, 41, G + 1, "spruce_trapdoor")
    # escada do porão (desce na sala de visitas, encostada na parede norte)
    for i in range(G):
        x = 12 - i
        if x < 7:
            break
        e.por(x, G - i, 34, "stone_brick_stairs", facing="east", **L.DEGRAU)
        for y in range(G - i + 1, G + 1):
            e.ar(x, y, 34)
    # escada para o andar de cima (encostada na parede norte, na sala de jantar)
    for i in range(5):
        x = 16 + i
        e.por(x, G + 1 + i, 34, "dark_oak_stairs", facing="east", **L.DEGRAU)
        for y in range(G + 1, G + 1 + i):
            e.por(x, y, 34, "dark_oak_planks")
    for x in range(16, 21):
        e.ar(x, G + 5, 34)

    # ---- andar de cima: corredor em z 39-40, quartos ao norte e ao sul, escritório no leste ----
    for x in range(X0 + 1, X1):
        for y in range(G + 6, G + 10):
            if x not in (16, 17, 18, 19, 20):
                e.por(x, y, 38, TABUA)
            e.por(x, y, 41, TABUA)
    for x in (12, 22):
        for z in list(range(Z0 + 1, 38)) + list(range(42, Z1)):
            for y in range(G + 6, G + 10):
                e.por(x, y, z, TABUA)
    for x in (8, 17, 27):
        m.vao(e, x, G + 6, 41, x, G + 7, 41)
    for x in (8, 27):
        m.vao(e, x, G + 6, 38, x, G + 7, 38)
    # quatro quartos, camas de cores diferentes
    for (x, z, f, cor) in ((6, 43, "south", "white"), (16, 43, "south", "red"), (25, 43, "south", "light_gray"),
                           (6, 36, "north", "gray")):
        m.cama(e, x, G + 6, z, f, cor)
        e.por(x + 1, G + 6, z, "barrel", facing="up", open=False)
        m.vela(e, x + 1, G + 7, z, 1, acesa=False)
    # escritório (nordeste): atril com o Diário p. 4, estantes, mesa
    L.atril(e, 27, G + 6, 35, "south", "diario_p4", "Escritório do fazendeiro: Diário do Açougueiro p. 4 (o poema).")
    for x in (24, 25, 29, 30, 31):
        e.por(x, G + 6, Z0 + 1, "bookshelf")
    m.mesa(e, 29, 36, 30, 36, G + 6, "dark_oak_trapdoor")
    m.cadeira(e, 29, G + 6, 37, "north", "dark_oak_stairs")
    # escada de mão para o sótão (corredor, leste)
    for y in range(G + 6, G + 11):
        e.por(X1 - 1, y, 40, "ladder", facing="west", waterlogged=False)

    # ---- sótão: o quarto fechado (retrato virado, baú vazio com a marca do terço) ----
    for z in range(36, 43):
        for y in range(G + 11, G + 14):
            e.por(20, y, z, TABUA)
            e.por(28, y, z, TABUA)
    for x in range(20, 29):
        for y in range(G + 11, G + 14):
            e.por(x, y, 36, TABUA)
            e.por(x, y, 42, TABUA)
    m.porta(e, 24, G + 11, 42, "south", hinge="left", aberta=False)
    for x in range(21, 28):
        for z in range(37, 42):
            e.ar(x, G + 11, z)
            e.ar(x, G + 12, z)
            e.ar(x, G + 13, z)
    bau_sotao = (26, G + 11, 37)
    L.bau(e, *bau_sotao, "south", "sotao_terco_vazio",
          "Baú vazio com a marca de um terço que alguém levou (o terço está na Casa do Cartógrafo).")
    m.cama(e, 22, G + 11, 39, "north", "light_gray")
    for x, z in ((8, 37), (9, 41), (31, 39), (15, 43)):
        e.por(x, G + 11, z, "barrel", facing="up", open=False)
    m.teias(e, X0 + 1, G + 3, Z0 + 1, X1 - 1, G + 4, Z1 - 1, 22)
    m.teias(e, X0 + 1, G + 8, Z0 + 1, X1 - 1, G + 9, Z1 - 1, 22)
    m.teias(e, X0 + 2, G + 12, Z0 + 1, X1 - 2, G + 16, Z1 - 1, 24)
    m.mancha(e, 18, G + 6, 43, 1, 0.5)


def capela(e):
    X0, X1, Z0, Z1 = CX0 + 1, CX1 - 1, CZ0, CZ1   # paredes em x 46 e 59
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            borda = x in (X0, X1) or z in (Z0, Z1)
            if (x, G, z) in e.blocos and e.nome(x, G, z) == "minecraft:air":
                continue
            e.por(x, G, z, BASE_CAPELA if borda else PISO_NAVE)

    def parede(x, y, z, canto):
        pilastra = canto or (x in (X0, X1) and z in (12, 17, 22))
        if y <= G + 1:
            return BASE_CAPELA
        if pilastra:
            return "stone_bricks"
        if y == G + 7 and x in (X0, X1):
            return "stone_bricks"
        return reboco(x, y, z)

    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 7, parede)
    # contrafortes
    for z in (12, 17, 22):
        for x, dx in ((X0, -1), (X1, 1)):
            for y in range(G + 1, G + 5):
                e.por(x + dx, y, z, BASE_CAPELA)
            e.por(x + dx, G + 5, z, "stone_brick_stairs", facing="east" if dx < 0 else "west", **L.DEGRAU)
    alt = L.telhado(e, X0 - 1, X1 + 1, Z0 - 1, Z1 + 1, G + 8, "z", "deepslate_tile_stairs", "dark_oak_planks",
                    ("deepslate_tile_slab", L.LAJE), inc=1, buracos=0.04, protegido=lambda z: z >= Z1 - 1)
    for z in (Z0, Z1):
        L.empena(e, "z", z, X0 - 1, X1 + 1, G + 8, 1, lambda a, y: reboco(a, y, z))
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 14, Z1 - 1, condicao=lambda x, y, z: y < alt(x))
    # fachada sul: espadana com o sino mudo (do capela_fazenda), porta dupla fechada, óculo
    for x in range(49, 57):
        topo = G + 17 if x in (51, 52, 53, 54) else G + 15
        for y in range(G + 8, topo + 1):
            if e.vazio(x, y, Z1) or y > alt(x):
                e.por(x, y, Z1, "stone_bricks" if x in (49, 56) else reboco(x, y, Z1))
    for x in (52, 53):
        e.ar(x, G + 14, Z1)
        e.ar(x, G + 15, Z1)
    e.por(52, G + 15, Z1, "bell", attachment="ceiling", facing="south", powered=False)
    e.por(52, G + 16, Z1, "chiseled_stone_bricks")
    e.por(53, G + 16, Z1, "chiseled_stone_bricks")
    for x in (49, 56):
        e.por(x, G + 16, Z1, "stone_brick_slab", **L.LAJE)
    for x in (50, 55):
        e.por(x, G + 16, Z1, "stone_brick_slab", **L.LAJE)
    m.vao(e, 52, G + 1, Z1, 53, G + 3, Z1)
    m.porta(e, 52, G + 1, Z1, "north", hinge="left", aberta=False, material="dark_oak_door")
    m.porta(e, 53, G + 1, Z1, "north", hinge="right", aberta=False, material="dark_oak_door")
    e.por(52, G + 4, Z1, "chiseled_stone_bricks")
    e.por(53, G + 4, Z1, "chiseled_stone_bricks")
    for x in (52, 53):
        for y in (G + 6, G + 7):
            e.por(x, y, Z1, "red_stained_glass_pane", east=True, west=True, north=False, south=False,
                  waterlogged=False)
    for x in range(50, 56):
        e.por(x, G, Z1 + 1, "stone_brick_stairs", facing="north", **L.DEGRAU)
    # oferendas: velas vermelhas acesas no chão diante das portas fechadas (de longe, a luz da capela)
    for x, q in ((50, 2), (51, 4), (54, 3), (55, 1), (52, 1)):
        e.por(x, G, Z1 + 2, "cobblestone")
        m.vela(e, x, G + 1, Z1 + 2, q, acesa=True, cor="red")
    # vitrais escuros laterais (vermelho-escuro e preto)
    for z in (10, 14, 19, 24):
        for x in (X0, X1):
            for y in range(G + 3, G + 7):
                vidro = "red_stained_glass_pane" if (y + z) % 3 == 0 else "black_stained_glass_pane"
                e.por(x, y, z, vidro, north=True, south=True, east=False, west=False, waterlogged=False)
    # nave: a Mesa do Pacto no meio (ao longo de z), cadeiras dos dois lados, lugar a mais na cabeceira (sul)
    for z in range(14, 22):
        for x in (52, 53):
            e.por(x, G + 1, z, "dark_oak_planks")
    lugares = []
    for i, z in enumerate(range(15, 21)):
        for x, olha in ((51, "east"), (54, "west")):
            m.cadeira(e, x, G + 1, z, olha, "dark_oak_stairs")
            mesa_x = 52 if x == 51 else 53
            m.vela(e, mesa_x, G + 2, z, 1 + (i + (x == 54)) % 4, acesa=False, cor="red" if (i + x) % 5 == 0 else None)
            lugares.append((mesa_x, z))
    for x in (52, 53):
        e.por(x, G + 2, 14, "flower_pot")
    m.cadeira(e, 52, G + 1, 22, "north", "dark_oak_stairs")  # o lugar a mais
    e.por(53, G + 1, 22, "dark_oak_stairs", facing="south", half="top", shape="straight", waterlogged=False)
    # bancos empurrados para as paredes
    for z in range(11, 26, 2):
        for x in (47, 48):
            if e.chance(0.8):
                m.cadeira(e, x, G + 1, z, "east" if x == 47 else "east", "spruce_stairs")
        for x in (57, 58):
            if e.chance(0.8):
                m.cadeira(e, x, G + 1, z, "west", "spruce_stairs")
    # altar ao norte (z 9-10) que gira: pedra cinzelada, velas vermelhas acesas (a luz da capela)
    for x in range(49, 57):
        e.por(x, G + 1, 11, "stone_brick_stairs", facing="north", **L.DEGRAU)
        for z in (9, 10):
            e.por(x, G + 1, z, PISO_NAVE)
    for x in (51, 52, 53, 54):
        e.por(x, G + 2, 9, "chiseled_stone_bricks" if x in (52, 53) else "stone_bricks")
    m.vela(e, 51, G + 3, 9, 3, acesa=True, cor="red")
    m.vela(e, 54, G + 3, 9, 4, acesa=True, cor="red")
    m.vela(e, 49, G + 2, 10, 2, acesa=True, cor="red")
    m.vela(e, 56, G + 2, 10, 3, acesa=True, cor="red")
    e.por(52, G + 3, 9, "flower_pot")  # o cálice de pedra (lugar do Altar do Aceite)
    for x in (50, 55):
        m.corrente(e, x, 9, G + 5, alt(x) - 1)
    # velas vermelhas acesas debaixo dos vitrais: de longe, a capela brilha vermelho
    for z in (10, 14, 19, 24):
        for x in (X0 + 1, X1 - 1):
            if e.vazio(x, G + 1, z):
                m.vela(e, x, G + 1, z, 2 + (z % 3), acesa=True, cor="red")
    # o furo da escada da cripta (atrás do altar, canto leste)
    for z in (10, 11):
        e.ar(57, G, z)
    m.lanterna_pendurada(e, 52, G + 7, 18, com_lanterna=False, comprimento_corrente=alt(52) - G - 8)
    m.teias(e, X0 + 1, G + 6, Z0 + 1, X1 - 1, G + 12, Z1 - 1, 20)
    # sangue: pingos na mesa e o arrasto do lugar a mais até o altar
    m.rastro(e, [(52, 23), (51, 22), (50, 16), (50, 12)], G + 1, falha=0.45)
    for z in (15, 18):
        m.mancha(e, 52, G + 2, z, 0, 0.6)
    e.pista(52, G + 3, 9, "altar", "Cálice de pedra do Altar do Aceite ('Chame.'). O altar gira (evento).")
    return lugares


def terreiro(e):
    # chão morto em tudo, terra batida no meio e caminho de pedra
    L.chao_morto(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, borda_irregular=3)
    for x in range(36, 46):
        for z in range(14, 58):
            if (x, G, z) in e.blocos and "dirt" in e.nome(x, G, z) or e.nome(x, G, z) in ("minecraft:podzol",):
                if ruido(x, 0, z, 2.5, 3) > 0.45:
                    e.por(x, G, z, "packed_mud" if e.chance(0.5) else "coarse_dirt")
    caminho = [(41, 59), (41, 50), (40, 46), (34, 47)]
    caminho2 = [(41, 50), (46, 40), (52, 30), (52, 28)]
    for pts in (caminho, caminho2):
        for (xa, za), (xb, zb) in zip(pts, pts[1:]):
            n = max(abs(xb - xa), abs(zb - za), 1)
            for i in range(n + 1):
                x = round(xa + (xb - xa) * i / n)
                z = round(za + (zb - za) * i / n)
                for dx in (0, 1):
                    if e.dentro(x + dx, G, z) and e.chance(0.8):
                        e.por(x + dx, G, z, Mistura(("cobblestone", 3), ("gravel", 2), ("andesite", 1),
                                                    ("mossy_cobblestone", 1)))
    # a árvore grande seca no meio (tronco 2 x 2)
    tx, tz = 38, 28
    for y in range(G + 1, G + 12):
        for dx in (0, 1):
            for dz in (0, 1):
                if y < G + 10 or (dx, dz) == (0, 0):
                    e.por(tx + dx, y, tz + dz, "dark_oak_log", axis="y")
    for dx, dz, comp, y0 in ((1, 0, 6, G + 8), (-1, 0, 5, G + 10), (0, 1, 4, G + 9), (0, -1, 6, G + 7),
                             (1, 1, 4, G + 11), (-1, -1, 3, G + 6)):
        x, z, y = tx, tz, y0
        for k in range(1, comp + 1):
            x += dx
            z += dz
            if k % 2 == 0:
                y += 1
            if e.dentro(x, y, z) and e.vazio(x, y, z):
                e.por(x, y, z, "dark_oak_log", axis="x" if dx else "z")
    # corda com um laço vazio num galho (corrente)
    m.corrente(e, 43, 28, G + 5, G + 7)
    # o sino do terreiro num poste (trave de madeira)
    for y in range(G + 1, G + 7):
        e.por(30, y, 26, "dark_oak_log", axis="y")
    for x in (30, 31, 32):
        e.por(x, G + 7, 26, "dark_oak_log", axis="x")
    e.por(32, G + 6, 26, "bell", attachment="ceiling", facing="east", powered=False)
    e.por(29, G + 1, 26, "dark_oak_fence", **L.SEM_LADOS)
    # poço
    for x in range(42, 45):
        for z in range(38, 41):
            if x == 43 and z == 39:
                e.ar(x, G, z)
                for y in range(G - 3, G):
                    e.ar(x, y, z)
                continue
            e.por(x, G + 1, z, Mistura(("cobblestone", 2), ("mossy_cobblestone", 1)))
    for x, z in ((42, 38), (44, 40)):
        for y in (G + 2, G + 3):
            e.por(x, y, z, "dark_oak_fence", **L.SEM_LADOS)
    for x in range(42, 45):
        e.por(x, G + 4, 39, "dark_oak_slab", **L.LAJE) if x != 43 else e.por(x, G + 4, 39, "dark_oak_planks")
    m.corrente(e, 43, 39, G + 1, G + 3)
    # galpão aberto (x 20-34, z 6-14)
    for x in (20, 26, 32):
        for z in (6, 13):
            for y in range(G + 1, G + 5):
                e.por(x, y, z, "dark_oak_log", axis="y")
    for x in range(19, 34):
        for z in range(5, 15):
            y = G + 5 + (0 if z >= 10 else 1)
            if e.chance(0.88):
                e.por(x, y, z, "spruce_slab", **L.LAJE)
    for x in range(20, 33):
        for y in range(G + 1, G + 5):
            if e.chance(0.85):
                e.por(x, y, 6, Mistura(("dark_oak_planks", 3), ("spruce_planks", 1)))
    for x, z, h in ((22, 8, 2), (23, 8, 1), (22, 9, 1), (30, 9, 1)):
        for y in range(h):
            e.por(x, G + 1 + y, z, "hay_block", axis="y")
    e.por(27, G + 1, 10, "damaged_anvil", facing="east")
    e.por(25, G + 1, 12, "grindstone", face="floor", facing="north")
    # curral (oeste) vazio
    L.cerca(e, [(2, 4), (17, 4), (17, 24), (2, 24), (2, 4)], G + 1, material="dark_oak_fence", falha=0.1)
    e.por(10, G + 1, 24, "dark_oak_fence_gate", facing="south", in_wall=False, open=True, powered=False)
    for z in range(8, 12):
        e.por(4, G + 1, z, "composter", level=0)
    # cemitério pequeno (leste, x 62-70, z 32-46): covas e cruzes de muro de pedra
    L.cerca(e, [(62, 32), (70, 32), (70, 46), (62, 46), (62, 32)], G + 1, material="dark_oak_fence", falha=0.15)
    e.ar(62, G + 1, 39)
    for gx in (64, 67):
        for gz in (34, 37, 40, 43):
            e.por(gx, G + 1, gz, "stone_brick_wall", up=True, north="none", south="none", east="none",
                  west="none", waterlogged=False)
            if e.chance(0.7):
                e.por(gx, G + 2, gz, "stone_brick_wall", up=True, north="none", south="none", east="none",
                      west="none", waterlogged=False)
            for dz in (1, 2):
                e.por(gx, G, gz + dz, Mistura(("coarse_dirt", 2), ("rooted_dirt", 2), ("mud", 1)))
            if e.chance(0.4):
                e.por(gx, G + 1, gz + 1, "dead_bush")
    # cerca da fazenda em volta, portão aberto ao sul
    L.cerca(e, [(39, 58), (1, 58), (1, 1), (70, 1), (70, 58), (44, 58)], G + 1, material="spruce_fence", falha=0.18)
    for x in (39, 44):
        for y in range(G + 1, G + 4):
            e.por(x, y, 58, "dark_oak_log", axis="y")
    e.por(40, G + 1, 58, "spruce_fence_gate", facing="south", in_wall=False, open=True, powered=False)
    # árvores mortas nos cantos
    for x, z, h in ((5, 54, 8), (66, 5, 9), (66, 54, 7), (8, 28, 6), (58, 50, 6)):
        L.arvore_morta(e, x, G + 1, z, h, galhos=4)
    L.mato_seco(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, chance=0.05)


def marcadores(e):
    lugares = [(52, z) for z in range(15, 21)] + [(53, z) for z in range(15, 21)]
    for i, (x, z) in enumerate(sorted(lugares, key=lambda p: (p[1], p[0])), 1):
        e.marcador(x, G + 3, z, f"lugar:{i:02d}",
                   "Lugar na Mesa do Pacto: prato com o nome do jogador (item do mod), vela de altura diferente e "
                   "cálice vazio. O mod usa um por jogador." if i == 1 else "", alvo=[x, G + 2, z])
    L.ponto(e, 52, G + 2, 23, "spawn:convidada", "O lugar a mais, na cabeceira: a Convidada (susto do Ato 1).")
    L.ponto(e, 52, G + 5, 18, "evento:area_capela",
            "Centro da capela: dentro, o chat de texto fica bloqueado (a voz continua). Velas fora de ordem apagam "
            "todas e tiram Lucidez do grupo.")
    L.ponto(e, 53, G + 2, 28, "evento:portas_capela",
            "Portas fechadas até o sino da Torre das Correntes ser calado. Antes disso, o sino do terreiro toca.")
    L.ponto(e, 53, G + 4, 10, "evento:altar_aceite",
            "Altar que gira e revela o Altar do Aceite (cálice de pedra com 'Chame.').")
    L.ponto(e, 31, G + 3, 26, "som:sino_terreiro", "Sino do terreiro, no poste: toca sozinho até a capela abrir.")
    L.ponto(e, 52, G + 14, 26, "som:sino_mudo", "Sino da espadana da capela: mudo.")
    L.ponto(e, 25, G + 12, 40, "evento:retrato_virado",
            "Quarto fechado do sótão: o retrato virado (pintura nossa). Virado, mostra um velho sem olhos; atrás, "
            "um recado a carvão.")
    for i, (x, z) in enumerate(((70, 30), (70, 20), (66, 58), (20, 58)), 1):
        y = G + 2
        if not e.vazio(x, y, z):
            y += 1
        L.ponto(e, x, y, z, f"spawn:aldeao_cerca_{i}",
                "Aldeão de uma vila próxima, parado na cerca, olhando para a capela." if i == 1 else "")
    L.ponto(e, 41, G + 2, 56, "evento:chegada", "Portão da fazenda (aberto).")
