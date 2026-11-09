"""Protótipo: capela de fazenda abandonada com a casa do sitiante ao lado. Clima de Sangue.

Tamanho 25 x 15 x 20 (x, y, z). Norte = z 0 (frente da capela, com a escada e o sino).
- Casa (oeste, x 1-11, z 6-18): térreo (sala com lareira, cozinha, sala de jantar, escada),
  andar de cima (dois quartos e corredor) e sótão. Varanda na frente (norte).
- Capela (leste, x 13-23, z 1-18): nave com bancos, altar com velas vermelhas, campanário de
  parede (espadana) com sino na fachada. Ligada à casa por uma passagem coberta (z 13).
- História no cenário, sem texto: a cama do quarto com mancha e rastro até a janela arrancada;
  na capela, poça diante do altar e um arrasto pelo corredor até a porta.
O acento de cor é um só: o vermelho do Sangue (pó de redstone apagado e velas vermelhas).
"""
from estrutura import Estrutura, Mistura, ruido
import modulos as m

NOME = "capela_fazenda"
TAM = (25, 15, 20)
SEMENTE = 1108

# (nome do corte, y mínimo, y máximo) para as plantas e cortes da prévia
CORTES = [("terreo", 0, 3), ("superior", 5, 8), ("sotao", 9, 12)]

PEDRA_BASE = Mistura(("cobblestone", 6), ("mossy_cobblestone", 3))
TABUA = Mistura(("spruce_planks", 16), ("dark_oak_planks", 2))
PISO_CASA = Mistura(("spruce_planks", 14), ("dark_oak_planks", 2), ("coarse_dirt", 1))
PISO_SUP = Mistura(("spruce_planks", 12), ("dark_oak_planks", 3))
PISO_CAPELA = Mistura(("polished_andesite", 8), ("stone_bricks", 5), ("cracked_stone_bricks", 2),
                      ("mossy_stone_bricks", 1))
BASE_CAPELA = Mistura(("stone_bricks", 6), ("mossy_stone_bricks", 3), ("cracked_stone_bricks", 2))
PISO_ALTAR = Mistura(("polished_andesite", 5), ("stone_bricks", 2), ("cracked_stone_bricks", 1))
LOG_Y = ("stripped_spruce_log", {"axis": "y"})
DEGRAU = {"half": "bottom", "shape": "straight", "waterlogged": False}
LAJE = {"type": "bottom", "waterlogged": False}


def reboco(x, y, z):
    """Cal gasta: manchas onde o tijolo aparece e onde a cal escureceu."""
    n = ruido(x, y, z, 2.2, SEMENTE + 3)
    if n < 0.30:
        return "bricks"
    if n < 0.37:
        return "diorite"
    return "calcite"


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    alt_casa = casa(e)
    alt_capela = capela(e)
    passagem(e)
    exterior(e)
    desgaste(e)
    sangue(e)
    pistas(e)
    # limpa o terreno/árvores que estiverem dentro dos cômodos ao colar
    e.ar_onde_vazio(2, 1, 7, 10, 4, 17)
    e.ar_onde_vazio(2, 6, 7, 10, 8, 17)
    e.ar_onde_vazio(3, 10, 7, 9, 13, 17, condicao=lambda x, y, z: y < alt_casa(x) - 1)
    e.ar_onde_vazio(2, 1, 3, 6, 3, 5)
    e.ar_onde_vazio(14, 1, 2, 22, 12, 17, condicao=lambda x, y, z: y < alt_capela(x) - 1)
    e.ar_onde_vazio(12, 1, 13, 12, 2, 13)
    return e


# ---------------------------------------------------------------------------
def casa(e):
    X0, X1, Z0, Z1 = 1, 11, 6, 18
    # telhado primeiro: as paredes, por cima, acertam o beiral
    alt = m.telhado_duas_aguas(e, 0, 12, 5, 19, 8, "dark_oak_stairs", "dark_oak_planks",
                               ("dark_oak_slab", LAJE), buracos=0.025)

    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            borda = x in (X0, X1) or z in (Z0, Z1)
            e.por(x, 0, z, PEDRA_BASE if borda else PISO_CASA)

    def parede(x, y, z, canto):
        if canto:
            return LOG_Y
        if y == 5:  # viga do andar
            return ("stripped_spruce_log", {"axis": "x" if z in (Z0, Z1) else "z"})
        if y == 1:
            return PEDRA_BASE
        if (z in (Z0, Z1) and x == 6) or (x in (X0, X1) and z == 12):
            return LOG_Y
        return TABUA

    m.paredes(e, X0, Z0, X1, Z1, 1, 8, parede)
    for z in (Z0, Z1):
        m.oitao(e, z, 0, 12, 8, lambda x, y, zz, c: LOG_Y if x == 6 else TABUA)
    m.janela(e, 5, Z0, 11, 11, "x", quebrada=0.5)
    m.janela(e, 7, Z1, 11, 11, "x", quebrada=0.5)

    # pisos
    m.piso(e, 2, 7, 10, 17, 5, PISO_SUP)
    m.piso(e, 3, 7, 9, 17, 9, TABUA)  # sótão (x 2 e 10 já são o forro do telhado)

    # portas e janelas (térreo y 2-3, andar de cima y 6-7)
    m.porta(e, 4, 1, Z0, "south", hinge="left", aberta=True)
    for x in (8, 9):
        m.janela(e, x, Z0, 2, 3, "x", 0.35)
    for x in (3, 9):
        m.janela(e, x, Z0, 6, 7, "x", 0.3)
        m.janela(e, x, Z1, 2, 3, "x", 0.3)
    m.janela(e, 8, Z1, 6, 7, "x", 0.3)
    for z in (8, 15):
        m.janela(e, X0, z, 2, 3, "z", 0.3)
    m.janela(e, X0, 15, 6, 7, "z", 0.3)
    e.ar(X0, 6, 10)  # janela do quarto arrancada: o rastro termina aqui
    e.ar(X0, 7, 10)
    m.janela(e, X1, 8, 2, 3, "z", 0.3)
    for z in (9, 15):
        m.janela(e, X1, z, 6, 7, "z", 0.3)
    m.porta(e, X1, 1, 13, "east", hinge="right", aberta=True)  # para a passagem da capela

    # varanda
    m.piso(e, 2, 3, 6, 5, 0, "spruce_planks")
    for y in (1, 2, 3):
        e.por(2, y, 3, "spruce_fence", north=False, south=False, east=False, west=False, waterlogged=False)
    e.por(6, 1, 3, "spruce_fence", north=False, south=False, east=False, west=False, waterlogged=False)  # poste quebrado
    m.piso(e, 2, 3, 6, 5, 4, ("dark_oak_slab", LAJE))
    e.tirar(6, 4, 3)
    e.tirar(5, 4, 3)

    # lareira e chaminé (parede oeste)
    for z in (11, 12, 13):
        for y in range(1, 5):
            e.por(X0, y, z, "bricks")
        for y in range(0, 8):
            e.por(0, y, z, "bricks")
        e.por(2, 3, z, "bricks")
    for y in range(8, 12):
        e.por(0, y, 12, "bricks")
    for z in (11, 13):
        e.por(2, 1, z, "bricks")
        e.por(2, 2, z, "bricks")
    e.por(2, 1, 12, "campfire", facing="east", lit=False, signal_fire=False, waterlogged=False)

    # térreo: sala
    for x in (2, 3):
        for y in (1, 2):
            e.por(x, y, 7, "bookshelf")
    m.cadeira(e, 4, 1, 11, "west")
    m.cadeira(e, 4, 1, 13, "west")
    for z in (11, 12, 13):
        e.por(3, 1, z, "brown_carpet")

    # térreo: cozinha (com parede interna em z 12 e vão em x 8)
    e.por(10, 1, 7, "barrel", facing="up", open=False)
    e.por(10, 1, 8, "furnace", facing="west", lit=False)
    e.por(10, 1, 9, "smoker", facing="west", lit=False)
    e.por(10, 1, 10, "crafting_table")
    e.por(10, 1, 11, "cauldron")
    e.por(7, 1, 7, "barrel", facing="up", open=False)
    e.por(8, 1, 7, "barrel", facing="south", open=False)
    m.lanterna_pendurada(e, 8, 4, 9)
    for x in range(7, 11):
        for y in range(1, 5):
            e.por(x, y, 12, TABUA)
    m.vao(e, 8, 1, 12, 8, 2, 12)

    # térreo: sala de jantar
    m.mesa(e, 7, 14, 8, 15, 1)
    m.cadeira(e, 6, 1, 14, "east")
    m.cadeira(e, 6, 1, 15, "east")
    m.cadeira(e, 9, 1, 14, "west")
    e.por(9, 1, 16, "spruce_stairs", facing="south", **DEGRAU)  # cadeira derrubada
    m.vela(e, 7, 2, 14, 2, acesa=False)
    e.por(8, 2, 15, "flower_pot")

    # escada para o andar de cima (sobe para o leste, encostada na parede sul)
    for x in range(2, 6):
        e.ar(x, 5, 17)  # vão no piso
    for i, x in enumerate(range(2, 7)):
        e.por(x, 1 + i, 17, "spruce_stairs", facing="east", **DEGRAU)
        if i >= 1:
            e.encher(x, 1, 17, x, i, 17, "spruce_planks")

    # andar de cima
    for z in range(7, 15):
        for y in (6, 7, 8):
            e.por(6, y, z, TABUA)  # parede entre os quartos
    for x in range(2, 6):
        e.por(x, 6, 16, "spruce_fence", east=x < 5, west=x > 2, north=False, south=False, waterlogged=False)
    # quarto oeste: a cama
    m.cama(e, 3, 6, 9, "north", "white")
    e.por(2, 6, 12, "barrel", facing="east", open=False)
    e.por(2, 6, 13, "barrel", facing="east", open=False)
    e.por(5, 6, 13, "chest", facing="west", type="single", waterlogged=False)
    # quarto leste
    m.cama(e, 9, 6, 9, "north", "light_gray")
    m.mesa(e, 10, 12, 10, 12, 6)
    m.cadeira(e, 9, 6, 12, "east")
    e.por(10, 6, 10, "chest", facing="west", type="single", waterlogged=False)
    e.por(10, 6, 14, "bookshelf")
    e.por(10, 7, 14, "bookshelf")
    for y in range(6, 10):
        e.por(7, y, 13, "ladder", facing="east", waterlogged=False)  # sobe para o sótão
    # corredor
    e.por(10, 6, 17, "barrel", facing="up", open=False)
    m.lanterna_pendurada(e, 8, 8, 16)

    # sótão
    e.por(5, 10, 8, "chest", facing="east", type="single", waterlogged=False)
    e.por(7, 10, 9, "barrel", facing="up", open=False)
    e.por(6, 10, 15, "barrel", facing="north", open=False)
    return alt


# ---------------------------------------------------------------------------
def capela(e):
    X0, X1, Z0, Z1 = 13, 23, 1, 18
    alt = m.telhado_duas_aguas(e, 12, 24, 1, 19, 7, "deepslate_tile_stairs", "dark_oak_planks",
                               ("deepslate_tile_slab", LAJE), buracos=0.05, protegido=lambda z: z <= 2)

    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            borda = x in (X0, X1) or z in (Z0, Z1)
            e.por(x, 0, z, BASE_CAPELA if borda else PISO_CAPELA)

    def parede(x, y, z, canto):
        pilastra = canto or (x in (X0, X1) and z in (6, 10, 14))
        if y <= 1:
            return BASE_CAPELA
        if pilastra:
            return "stone_bricks"
        if y == 6 and x in (X0, X1):
            return "stone_bricks"  # cimalha das paredes laterais
        return reboco(x, y, z)

    m.paredes(e, X0, Z0, X1, Z1, 0, 6, parede)
    for z in (Z0, Z1):
        m.oitao(e, z, 12, 24, 7, lambda x, y, zz, c: reboco(x, y, zz), de_k=0)

    # fachada: espadana (campanário de parede) com o sino
    for x in range(16, 21):
        topo = 13 if x in (17, 18, 19) else 12
        for y in range(7, topo + 1):
            e.por(x, y, Z0, "stone_bricks" if x in (16, 20) else reboco(x, y, Z0))
    e.ar(18, 11, Z0)
    e.por(18, 12, Z0, "bell", attachment="ceiling", facing="north", powered=False)
    e.por(18, 13, Z0, "chiseled_stone_bricks")
    e.por(18, 11, Z0 + 1, "stone_bricks")  # fundo do vão do sino
    for x in (16, 20):
        e.por(x, 13, Z0, "stone_brick_slab", **LAJE)
    for x in (17, 19):
        e.por(x, 14, Z0, "stone_brick_slab", **LAJE)
    e.por(18, 14, Z0, "stone_bricks")

    # porta da frente: vão de 3, verga de pedra, uma folha ficou (aberta), a outra caiu no chão
    m.vao(e, 17, 1, Z0, 19, 3, Z0)
    e.por(17, 4, Z0, "stone_bricks")
    e.por(18, 4, Z0, "chiseled_stone_bricks")
    e.por(19, 4, Z0, "stone_bricks")
    m.porta(e, 17, 1, Z0, "south", hinge="left", aberta=True)
    for z in (2, 3):
        e.por(19, 1, z, "spruce_trapdoor", facing="north", half="bottom", open=False, powered=False,
              waterlogged=False)
    for y in (5, 6):
        m.janela(e, 18, Z0, y, y, "x", quebrada=0.0)  # óculo da fachada
    for y in (4, 5):
        m.janela(e, 18, Z1, y, y, "x", quebrada=0.5)  # janela atrás do altar

    # janelas laterais altas
    for x in (X0, X1):
        for z in (4, 8, 12):
            m.janela(e, x, z, 2, 4, "z", quebrada=0.4, grade=0.15)
        m.janela(e, x, 16, 3, 4, "z", quebrada=0.4, grade=0.15)
    m.vao(e, X0, 1, 13, X0, 2, 13)  # passagem para a casa

    # bancos (olhando para o altar, ao sul); alguns faltam, um foi derrubado
    for z in (4, 6, 8, 10, 12):
        m.banco(e, 14, 17, 1, z, "south", falha=0.08)
        m.banco(e, 19, 22, 1, z, "south", falha=0.08)
    e.ar(16, 1, 8)
    e.ar(17, 1, 8)
    e.ar(20, 1, 10)
    e.por(20, 1, 11, "spruce_stairs", facing="east", **DEGRAU)

    # presbitério e altar
    for x in range(16, 21):
        e.por(x, 1, 14, "stone_brick_stairs", facing="south", **DEGRAU)
    m.piso(e, 14, 15, 22, 17, 1, PISO_ALTAR)
    e.por(17, 2, 16, "stone_bricks")
    e.por(18, 2, 16, "chiseled_stone_bricks")
    e.por(19, 2, 16, "stone_bricks")
    m.vela(e, 17, 3, 16, 3, acesa=True, cor="red")
    m.vela(e, 19, 3, 16, 2, acesa=True, cor="red")
    m.vela(e, 21, 2, 17, 2, acesa=False)
    m.vela(e, 15, 2, 17, 1, acesa=False)
    e.por(15, 2, 16, "lectern", facing="east", has_book=False, powered=False)
    # correntes vazias penduradas atrás do altar (presas no forro)
    for x in (16, 20):
        m.corrente(e, x, 17, 5, alt(x) - 2)

    # luz: uma lanterna ainda pendurada, a outra caiu
    m.lanterna_pendurada(e, 15, 5, 8, comprimento_corrente=alt(15) - 2 - 5)
    m.corrente(e, 21, 8, 7, alt(21) - 2)
    e.por(21, 1, 9, "lantern", hanging=False, waterlogged=False)
    return alt


# ---------------------------------------------------------------------------
def passagem(e):
    e.por(12, 0, 13, PEDRA_BASE)
    for z in (12, 14):
        e.por(12, 0, z, PEDRA_BASE)
        for y in (1, 2):
            e.por(12, y, z, TABUA)
    for z in (12, 13, 14):
        e.por(12, 3, z, "spruce_planks")


def exterior(e):
    for x in range(16, 21):
        e.por(x, 0, 0, "stone_brick_stairs", facing="south", **DEGRAU)
    for x, z in ((4, 0), (4, 1), (4, 2), (3, 0), (5, 1), (5, 0)):
        e.por(x, 0, z, "dirt_path")
    for x, z in ((6, 1), (7, 0), (2, 1), (3, 2)):
        e.por(x, 0, z, "coarse_dirt")
    e.por(0, 0, 19, "hay_block", axis="x")
    e.por(1, 0, 19, "hay_block", axis="x")
    e.por(0, 1, 19, "hay_block", axis="z")
    e.por(12, 0, 17, "composter", level=3)
    # trepadeiras na capela (fachada e lado da passagem) e na parede oeste da casa
    for z in (2, 3, 4, 5):
        if e.chance(0.75):
            m.trepadeira(e, 12, z, 6, "east", 2 + int(e.rng.random() * 4))
    for x in (14, 15, 21, 22):
        if e.chance(0.7):
            m.trepadeira(e, x, 0, 6, "south", 2 + int(e.rng.random() * 4))
    for z in (15, 16, 17):
        m.trepadeira(e, 0, z, 7, "east", 2 + int(e.rng.random() * 5))


def desgaste(e):
    m.envelhecer(e, 13, 0, 1, 23, 14, 18,
                 {"stone_bricks": Mistura(("cracked_stone_bricks", 2), ("mossy_stone_bricks", 2)),
                  "calcite": Mistura(("diorite", 1), ("bricks", 2))}, intensidade=0.6)
    m.envelhecer(e, 0, 0, 3, 12, 14, 19,
                 {"spruce_planks": "dark_oak_planks", "cobblestone": "mossy_cobblestone"}, intensidade=0.5)
    # tábuas faltando nas paredes da casa
    for x, z in m.perimetro(1, 6, 11, 18):
        for y in range(2, 9):
            if e.nome(x, y, z) in ("minecraft:spruce_planks", "minecraft:dark_oak_planks") and e.chance(0.04):
                e.ar(x, y, z)
    # frestas no piso de cima (só onde não tem móvel em cima)
    for x in range(2, 11):
        for z in range(7, 16):
            if e.vazio(x, 6, z) and e.vazio(x, 4, z) and e.chance(0.03):
                e.ar(x, 5, z)
    # mato no chão de terra da casa, musgo na nave
    for x in range(2, 11):
        for z in range(7, 18):
            if e.nome(x, 0, z) == "minecraft:coarse_dirt" and e.vazio(x, 1, z) and e.chance(0.6):
                e.por(x, 1, z, "short_grass" if e.chance(0.6) else "dead_bush")
    m.chao_com_musgo(e, 14, 2, 22, 6, 1, 0.35)
    m.chao_com_musgo(e, 14, 7, 22, 13, 1, 0.12)
    # teias nos cantos altos
    m.teias(e, 2, 3, 7, 10, 4, 17, 10)
    m.teias(e, 2, 7, 7, 10, 8, 17, 12)
    m.teias(e, 3, 10, 7, 9, 12, 17, 14)
    m.teias(e, 14, 4, 2, 22, 8, 17, 18)


def sangue(e):
    # capela: em cima do altar, a poça diante dele e o arrasto pelo corredor até a porta
    m.mancha(e, 18, 3, 16, 0, 1.0)
    m.mancha(e, 18, 2, 15, 2, 0.75)
    m.rastro(e, [(18, 13), (18, 9), (18, 5), (18, 2), (18, 1)], 1, falha=0.3)
    # casa: ao lado da cama, e o rastro até a janela arrancada
    m.mancha(e, 4, 6, 9, 1, 0.7)
    m.rastro(e, [(4, 10), (3, 10), (2, 10), (1, 10)], 6, falha=0.15)


def pistas(e):
    e.pista(15, 2, 16, "atril", "Atril vazio ao lado do altar: lugar do livro principal (Lore decide o texto).")
    e.pista(18, 12, 1, "sino", "Sino da espadana: pode tocar sozinho num evento (roteiro: sino que toca sozinho).")
    e.pista(3, 6, 8, "cama", "Cama do quarto oeste com mancha e rastro até a janela arrancada (x 1, z 10).")
    e.pista(5, 6, 13, "bau", "Baú do quarto oeste: recado ou foto.")
    e.pista(10, 6, 10, "bau", "Baú do quarto leste: diário.")
    e.pista(5, 10, 8, "bau", "Baú do sótão: peça de enigma.")
    e.pista(18, 3, 16, "altar", "Altar: velas vermelhas acesas (única luz quente da capela).")
