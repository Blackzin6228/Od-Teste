"""2. Casa do Açougueiro. Ficha: lore/fichas-estruturas.md, seção 2. Tamanho 48 x 36 x 16, mais 7 de porão.

Decisões de 14/11: entrada pelo porão mantida (rota de perseguição do Magarefe) e casa isolada (sem vila perto).
Sul = frente (porta larga de duas folhas). Dois corpos colados, de alturas diferentes:
- Casa de abate (x 14-35, z 8-16): pedra fria, pé-direito alto aberto até o telhado, quatro correntes com placa
  (Cão, Vaca, Lebre, Javali) e alavanca embaixo, ralos de ferro, o alçapão grande do porão no meio, a quinta corrente
  vazia (do Pendurado) e a viga com o recado. Porta de ferro de botão no lado do curral (leste).
- Loja (x 14-35, z 16-24): madeira, balcão com o atril do Diário p. 2 e 3, dois defumadores, ganchos vazios.
  Em cima, o palheiro, com a fresta para a casa de abate e o gancho pendurado sobre a porta da frente.
- Moradia (anexo oeste, x 4-13, z 13-24): cama, mesa, estante quebrada, lareira apagada.
- Porão de salga (embaixo da casa de abate, y 0-6): barris em fila, bancada de pedra lisa com o barril aberto
  (Livro de Trocas, metade direita do Mapa, Faca de Osso). Túnel de serviço até o curral, com escada de mão.
- Curral (leste): cocho cheio, nenhum bicho, sulcos na terra, portão entreaberto e o alçapão da saída de serviço.
"""
from estrutura import Estrutura, Mistura, ruido
import modulos as m
import lugares as L

NOME = "casa_acougueiro"
TITULO = "2. Casa do Açougueiro"
TAM = (48, 23, 36)
SEMENTE = 1402
G = 7
CORTES = [("porao", 0, G - 2), ("terreo", G, G + 4), ("palheiro", G + 5, G + 9)]
FRENTE_ISO = 0
FRENTE_GRAUS = 15
LONGE = {"dist": 56, "altura_olho": 2.0}
Y_MARCADORES = None
RESUMO = """
O grande lugar do Ato 1. Entradas: **porta da frente** (sul, duas folhas: acorda o Magarefe), **porta de ferro** de
botão (leste, lado do curral: a entrada segura) e a **saída de serviço do porão** (alçapão no curral, escada de mão,
túnel até o porão: pega o Magarefe de frente). A rota da perseguição (`rota:01` a `rota:08`) sai do porão, sobe pelo
curral, contorna a casa e entra pela frente. Sem spawn em toda a estrutura (luz invisível fraca). Chão em
`y_chao = 7`: o porão ocupa y 0 a 6.
"""

PEDRA = Mistura(("cobblestone", 5), ("cracked_stone_bricks", 3), ("stone_bricks", 2), ("cobbled_deepslate", 2),
                ("mossy_cobblestone", 1))
PEDRA_PORAO = Mistura(("stone_bricks", 4), ("cracked_stone_bricks", 3), ("mossy_stone_bricks", 2),
                      ("cobbled_deepslate", 2))
TABUA = L.TABUA_ESCURA
LOG_Y = ("stripped_dark_oak_log", {"axis": "y"})
PISO_ABATE = Mistura(("white_terracotta", 6), ("light_gray_terracotta", 3), ("smooth_stone", 3),
                     ("terracotta", 1))
ANIMAIS = [(18, "Cão"), (22, "Vaca"), (28, "Lebre"), (32, "Javali")]


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    e.y_chao = G
    porao(e)
    abate(e)
    loja(e)
    moradia(e)
    curral(e)
    exterior(e)
    sangue(e)
    marcadores(e)
    L.sem_spawn(e, 15, 1, 9, 34, G - 1, 15, passo=4)
    L.sem_spawn(e, 36, 1, 11, 42, 3, 13, passo=3)
    L.sem_spawn(e, 15, G + 1, 9, 34, G + 2, 23, passo=4)
    L.sem_spawn(e, 5, G + 1, 14, 12, G + 2, 23, passo=4)
    L.sem_spawn(e, 15, G + 6, 17, 34, G + 6, 23, passo=4)
    return e


# ---------------------------------------------------------------------------
def porao(e):
    X0, Z0, X1, Z1 = 14, 8, 35, 16
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, 0, z, PEDRA_PORAO if x in (X0, X1) or z in (Z0, Z1) else
                  Mistura(("smooth_stone", 3), ("stone_bricks", 3), ("cobblestone", 2), ("mud", 1)))
    m.paredes(e, X0, Z0, X1, Z1, 1, G - 1, PEDRA_PORAO)
    e.ar_onde_vazio(X0 + 1, 1, Z0 + 1, X1 - 1, G - 1, Z1 - 1)
    # abóbada baixa: degraus de cabeça para baixo nas bordas, pilares a cada 5
    for x in range(X0 + 1, X1):
        e.por(x, G - 1, Z0 + 1, "stone_brick_stairs", facing="north", half="top", shape="straight", waterlogged=False)
        e.por(x, G - 1, Z1 - 1, "stone_brick_stairs", facing="south", half="top", shape="straight", waterlogged=False)
    for x in (19, 24, 29):
        for z in (Z0 + 1, Z1 - 1):
            for y in range(1, G - 1):
                e.por(x, y, z, "stone_bricks")
    # barris em fila (salga), alguns abertos
    for x in range(16, 34, 2):
        if x in (24,):
            continue
        for z in (9, 15):
            if e.vazio(x, 1, z):
                e.por(x, 1, z, "barrel", facing="up", open=e.chance(0.25))
                if e.chance(0.4):
                    e.por(x, 2, z, "barrel", facing="up", open=False)
    # bancada de pedra lisa com o barril aberto
    for x in range(26, 32):
        e.por(x, 1, 12, "stone_bricks")
        e.por(x, 2, 12, "smooth_stone_slab", **L.LAJE)
    e.por(28, 2, 12, "barrel", facing="up", open=True)
    e.marcador(28, 3, 12, "bau:porao_livro_trocas",
               "Barril aberto sobre a bancada. Livro de Trocas, metade direita do Mapa e Faca de Osso. "
               "Abre com a ordem certa das correntes.")
    # ralos
    for x in (21, 31):
        e.por(x, 1, 13, "iron_trapdoor", facing="north", half="bottom", open=False, powered=False, waterlogged=False)
    e.por(17, 1, 12, "cauldron")
    # coluna e escada do alçapão grande (sobe para o meio da casa de abate)
    for y in range(1, G):
        e.por(24, y, 11, "stone_bricks")
        e.por(24, y, 12, "ladder", facing="south", waterlogged=False)
    # túnel de serviço até o curral (leste) e o poço da escada de mão
    for x in range(X1, 43):
        for z in (10, 11, 12, 13, 14):
            for y in range(0, 5):
                borda = z in (10, 14) or y in (0, 4)
                if x == X1 and not borda:
                    e.ar(x, y, z)
                    continue
                if borda:
                    e.por(x, y, z, PEDRA_PORAO if y else Mistura(("cobblestone", 2), ("mud", 1)))
                else:
                    e.ar(x, y, z)
    for y in range(1, 4):
        e.ar(X1, y, 12)
    for x in (37, 40):
        for z in (11, 13):
            e.por(x, 3, z, "spruce_fence", **L.SEM_LADOS)
        e.por(x, 3, 12, "stripped_spruce_log", axis="z")
    # poço: x 42-44, z 11-13, de y 1 até o chão
    for y in range(1, G + 1):
        for x in (42, 43, 44):
            for z in (11, 12, 13):
                if x == 43 and z == 12:
                    e.ar(x, y, z)
                else:
                    if (x, y, z) not in e.blocos or y >= 4:
                        e.por(x, y, z, PEDRA_PORAO if y < G else "cobblestone")
    for y in range(1, G):
        e.por(44, y, 12, "stone_bricks")
        e.por(43, y, 12, "ladder", facing="west", waterlogged=False)
    e.ar(42, 1, 12)
    e.ar(42, 2, 12)
    e.ar(42, 3, 12)
    e.por(43, G, 12, "spruce_trapdoor", facing="west", half="top", open=False, powered=False, waterlogged=False)
    e.por(36, 1, 12, "lantern", hanging=False, waterlogged=False)
    m.teias(e, 15, 4, 9, 34, G - 1, 15, 14)


def abate(e):
    X0, Z0, X1, Z1 = 14, 8, 35, 16
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, PEDRA if x in (X0, X1) or z in (Z0, Z1) else PISO_ABATE)

    def parede(x, y, z, canto):
        if canto or (z == Z0 and x in (19, 24, 30)) or (x in (X0, X1) and z == 12):
            return "stone_bricks" if y < G + 9 else LOG_Y
        return PEDRA

    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 9, parede)
    alt = L.telhado(e, Z0 - 1, Z1 + 1, X0 - 1, X1 + 1, G + 10, "x", "deepslate_tile_stairs", "dark_oak_planks",
                    ("deepslate_tile_slab", L.LAJE), inc=1, buracos=0.04)
    for x in (X0, X1):
        L.empena(e, "x", x, Z0 - 1, Z1 + 1, G + 10, 1, PEDRA)
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 15, Z1 - 1, condicao=lambda x, y, z: y < alt(z))
    # tesouras do telhado: tirantes ao longo de z a cada 5 e a viga mestra ao longo de x (as correntes saem dela)
    for x in (16, 20, 25, 30, 33):
        for z in range(Z0 + 1, Z1):
            e.por(x, G + 10, z, "stripped_dark_oak_log", axis="z")
    for x in range(X0 + 1, X1):
        e.por(x, G + 12, 12, "stripped_dark_oak_log", axis="x")
    # janelas altas e estreitas, gradeadas (frestas de pedra)
    for x in (17, 22, 27, 33):
        m.janela(e, x, Z0, G + 6, G + 8, "x", quebrada=0.2, grade=0.9)
    # porta de ferro (leste) com botão de fora
    m.vao(e, X1, G + 1, 12, X1, G + 2, 12)
    m.porta(e, X1, G + 1, 12, "west", hinge="left", aberta=False, material="iron_door")
    e.por(X1 + 1, G + 2, 11, "stone_button", face="wall", facing="east", powered=False)
    e.por(X1, G + 3, 12, "chiseled_stone_bricks")
    # quatro correntes com placa e alavanca
    for x, nome in ANIMAIS:
        m.corrente(e, x, 12, G + 4, G + 11)
        e.por(x, G + 1, 12, "lever", face="floor", facing="south", powered=False)
        L.placa(e, x, G + 1, 13, [nome], rotation=0)
    # a quinta corrente, vazia, comprida (o Pendurado)
    m.corrente(e, 16, 10, G + 3, G + 9)
    # alçapão grande do porão (2 x 2, trancado), no meio
    for x in (24, 25):
        for z in (12, 13):
            e.por(x, G, z, "iron_trapdoor", facing="north", half="top", open=False, powered=False, waterlogged=False)
    e.por(24, G, 12, "iron_trapdoor", facing="south", half="top", open=False, powered=False, waterlogged=False)
    # ralos e a canaleta ao longo das correntes
    for x in range(16, 34):
        if x in (24, 25):
            continue
        if x % 3 == 0:
            e.por(x, G, 11, "iron_trapdoor", facing="north", half="top", open=False, powered=False, waterlogged=False)
    # o recado riscado na viga (parede norte, debaixo da viga)
    L.placa(e, 25, G + 4, Z0 + 1, ["O Açougueiro", "ensina a ordem.", "Não adianta", "pedir."], parede="south")
    # bancadas, ganchos, mesa de corte
    for x in (31, 32, 33):
        e.por(x, G + 1, 9, "smooth_stone")
        if x == 32:
            e.por(x, G + 2, 9, "chain", axis="y", waterlogged=False)
    e.por(17, G + 1, 15, "smooth_stone")
    e.por(18, G + 1, 15, "smooth_stone_slab", **L.LAJE)
    e.por(33, G + 1, 15, "cauldron")
    e.por(15, G + 1, 9, "barrel", facing="up", open=False)
    e.por(15, G + 1, 10, "barrel", facing="east", open=False)
    e.por(15, G + 2, 9, "barrel", facing="up", open=False)
    for x in (21, 27):
        m.corrente(e, x, Z0 + 1, G + 6, G + 9)
    # uma lanterna baixa acesa (a única luz da casa de abate), corrente longa
    m.lanterna_pendurada(e, 29, G + 5, 14, comprimento_corrente=4)
    m.teias(e, 15, G + 7, 9, 34, G + 11, 15, 16)


def loja(e):
    X0, Z0, X1, Z1 = 14, 16, 35, 24
    for x in range(X0, X1 + 1):
        for z in range(Z0 + 1, Z1 + 1):
            e.por(x, G, z, PEDRA if x in (X0, X1) or z == Z1 else
                  Mistura(("spruce_planks", 5), ("dark_oak_planks", 4), ("coarse_dirt", 1)))

    def parede(x, y, z, canto):
        if z == Z0:
            return None  # parede de pedra da casa de abate
        if y == G + 1:
            return PEDRA
        if canto or (z == Z1 and x in (18, 21, 28, 31)) or (x in (X0, X1) and z == 20):
            return LOG_Y
        if y == G + 5:
            return ("stripped_dark_oak_log", {"axis": "x" if z == Z1 else "z"})
        return TABUA

    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 8, parede)
    alt = L.telhado(e, Z0 - 1, Z1 + 1, X0 - 1, X1 + 1, G + 9, "x", "dark_oak_stairs", "dark_oak_planks",
                    ("dark_oak_slab", L.LAJE), inc=1, buracos=0.05, protegido=lambda x: 23 <= x <= 26)
    for x in (X0, X1):
        L.empena(e, "x", x, Z0 - 1, Z1 + 1, G + 9, 1, TABUA)
    # oitão de frente sobre a porta (cumeeira ao longo de z), com a porta do palheiro e a viga do gancho
    alt_f = L.telhado(e, 21, 28, Z1 - 3, Z1 + 1, G + 8, "z", "dark_oak_stairs", "dark_oak_planks",
                      ("dark_oak_slab", L.LAJE), inc=1)
    L.empena(e, "z", Z1, 21, 28, G + 8, 1, lambda a, y: LOG_Y if a in (22, 27) else TABUA)
    m.vao(e, 24, G + 6, Z1, 25, G + 7, Z1)
    for z in (Z1 - 1, Z1, Z1 + 1, Z1 + 2):
        e.por(24, G + 9, z, "stripped_dark_oak_log", axis="z")
    m.corrente(e, 24, Z1 + 2, G + 4, G + 8)
    # chaminé alta do defumadouro (sai do telhado: a silhueta do lugar)
    for y in range(G + 1, 23):
        for x in (19, 20):
            for z in (17, 18):
                if y <= G + 2 and x == 20 and z == 18:
                    continue
                e.por(x, y, z, Mistura(("deepslate_bricks", 4), ("cracked_deepslate_bricks", 2), ("bricks", 1)))
    # palheiro (piso em G + 5)
    for x in range(X0 + 1, X1):
        for z in range(Z0 + 1, Z1):
            e.por(x, G + 5, z, TABUA)
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 4, Z1 - 1)
    e.ar_onde_vazio(X0 + 1, G + 6, Z0 + 1, X1 - 1, G + 13, Z1 - 1,
                    condicao=lambda x, y, z: y < alt(z) or (21 < x < 28 and y < alt_f(x)))
    # porta da frente: duas folhas de madeira escura, fechadas
    m.vao(e, 24, G + 1, Z1, 25, G + 2, Z1)
    m.porta(e, 24, G + 1, Z1, "north", hinge="left", aberta=False, material="dark_oak_door")
    m.porta(e, 25, G + 1, Z1, "north", hinge="right", aberta=False, material="dark_oak_door")
    for x in (23, 26):
        e.por(x, G + 3, Z1, LOG_Y)
    for x in (16, 20, 29, 33):
        m.janela(e, x, Z1, G + 2, G + 3, "x", quebrada=0.5, grade=0.3)
    m.janela(e, 19, Z1, G + 6, G + 7, "x", quebrada=0.6)
    m.janela(e, 31, Z1, G + 6, G + 6, "x", quebrada=0.6)
    # passagem para a moradia
    m.vao(e, X0, G + 1, 21, X0, G + 2, 21)
    # balcão
    for x in range(18, 31):
        if x in (24, 25):
            continue
        e.por(x, G + 1, 20, "dark_oak_planks")
        e.por(x, G + 2, 20, "spruce_slab", **L.LAJE)
    L.atril(e, 31, G + 1, 20, "south", "diario_p2_p3",
            "Na ponta do balcão. Diário do Açougueiro, páginas 2 e 3 (ordem dos bichos; tabela dos Riscos de Corte).")
    # defumadores e ganchos vazios
    for x in (21, 29):
        e.por(x, G + 1, 17, "smoker", facing="south", lit=False)
    e.por(20, G + 1, 23, "barrel", facing="up", open=False)
    m.vela(e, 20, G + 2, 23, 3, acesa=True, cor="red")  # vela vermelha na janela da frente (vista de longe)
    m.lanterna_pendurada(e, 25, G + 7, 23, comprimento_corrente=1)  # luz no palheiro, pela porta de carga
    for x in range(17, 33, 3):
        m.corrente(e, x, 18, G + 3, G + 4)
    for x, z in ((15, 17), (16, 17), (34, 17), (34, 18), (15, 23)):
        e.por(x, G + 1, z, "barrel", facing="up" if e.chance(0.5) else "north", open=False)
    e.por(34, G + 2, 17, "barrel", facing="up", open=False)
    # escada de mão ao palheiro
    for y in range(G + 1, G + 6):
        e.por(15, y, 21, "ladder", facing="east", waterlogged=False)
    # palheiro: feno, a fresta para a casa de abate
    for x, z, h in ((17, 18, 2), (18, 18, 1), (17, 19, 1), (32, 18, 2), (33, 19, 2), (31, 18, 1), (29, 22, 1)):
        for y in range(h):
            e.por(x, G + 6 + y, z, "hay_block", axis="y")
    for x in (24, 25):
        e.ar(x, G + 6, Z0)
        e.por(x, G + 7, Z0, "iron_bars", east=x == 24, west=x == 25, north=False, south=False, waterlogged=False)
    e.por(26, G + 6, Z0, "air")
    m.teias(e, 15, G + 3, 17, 34, G + 4, 23, 10)
    m.teias(e, 15, G + 7, 17, 34, G + 12, 23, 14)


def moradia(e):
    X0, Z0, X1, Z1 = 4, 13, 13, 24
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, PEDRA if x in (X0, X1) or z in (Z0, Z1) else Mistura(("spruce_planks", 4),
                                                                              ("dark_oak_planks", 3)))

    def parede(x, y, z, canto):
        if x == X1 and z >= 16:
            return None if y > G + 5 else (PEDRA if y == G + 1 else TABUA)
        if y == G + 1:
            return PEDRA
        if canto or (z in (Z0, Z1) and x == 8):
            return ("spruce_log", {"axis": "y"})
        return TABUA

    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 5, parede)
    alt = L.telhado(e, X0 - 1, X1 + 1, Z0 - 1, Z1 + 1, G + 6, "z", "dark_oak_stairs", "dark_oak_planks",
                    ("dark_oak_slab", L.LAJE), inc=1, buracos=0.06)
    for z in (Z0, Z1):
        L.empena(e, "z", z, X0 - 1, X1 + 1, G + 6, 1, TABUA)
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 11, Z1 - 1, condicao=lambda x, y, z: y < alt(x))
    # lareira apagada e chaminé (oeste)
    for z in (17, 18, 19):
        for y in range(G + 1, G + 5):
            e.por(X0, y, z, Mistura(("cobblestone", 3), ("bricks", 1)))
        for y in range(G, G + 12):
            if z == 18 or y < G + 7:
                e.por(X0 - 1, y, z, Mistura(("cobblestone", 3), ("mossy_cobblestone", 1)))
    e.por(X0 + 1, G + 1, 18, "campfire", facing="east", lit=False, signal_fire=False, waterlogged=False)
    for z in (17, 19):
        e.por(X0 + 1, G + 1, z, "cobblestone")
        e.por(X0 + 1, G + 2, z, "cobblestone")
    e.por(X0 + 1, G + 3, 17, "cobblestone")
    e.por(X0 + 1, G + 3, 18, "cobblestone")
    e.por(X0 + 1, G + 3, 19, "cobblestone")
    # cama, mesa, estante quebrada
    m.cama(e, 11, G + 1, 23, "north", "light_gray")
    e.por(11, G + 1, 21, "barrel", facing="up", open=False)
    m.mesa(e, 8, 19, 8, 20, G + 1, "dark_oak_trapdoor")
    m.cadeira(e, 7, G + 1, 19, "east", "dark_oak_stairs")
    e.por(9, G + 1, 20, "dark_oak_stairs", facing="north", half="top", shape="straight", waterlogged=False)
    m.vela(e, 8, G + 2, 19, 1, acesa=True, cor="red")
    for x in (6, 7, 9, 10):
        e.por(x, G + 1, Z0 + 1, "bookshelf" if x != 9 else "spruce_planks")
        if x in (6, 10):
            e.por(x, G + 2, Z0 + 1, "bookshelf")
    e.por(7, G + 1, Z0 + 2, "spruce_slab", **L.LAJE)  # tábua caída
    m.porta(e, 8, G + 1, Z1, "north", hinge="left", aberta=True)
    m.janela(e, 6, Z1, G + 2, G + 3, "x", quebrada=0.5)
    m.janela(e, 11, Z0, G + 2, G + 3, "x", quebrada=0.5)
    m.teias(e, 5, G + 3, 14, 12, G + 7, 23, 10)


def curral(e):
    pontos = [(37, 4), (47, 4), (47, 31), (37, 31), (37, 18)]
    L.cerca(e, pontos, G + 1, material="dark_oak_fence", falha=0.08)
    e.por(37, G + 1, 27, "dark_oak_fence_gate", facing="west", in_wall=False, open=True, powered=False)
    # terra batida com sulcos
    for x in range(38, 47):
        for z in range(5, 31):
            if (x, G, z) in e.blocos:
                continue
            n = ruido(x, 0, z, 2.0, SEMENTE)
            sulco = (x + z // 3) % 4 == 0
            e.por(x, G, z, "mud" if sulco and n > 0.4 else ("coarse_dirt" if n > 0.35 else "packed_mud"))
    # cocho cheio de trigo
    for z in range(20, 25):
        e.por(46, G + 1, z, "composter", level=8 if z != 22 else 6)
    for z in (19, 25):
        e.por(46, G + 1, z, "spruce_slab", **L.LAJE)
    e.por(39, G + 1, 6, "hay_block", axis="x")
    e.por(40, G + 1, 6, "hay_block", axis="y")


def exterior(e):
    L.chao_morto(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, borda_irregular=3)
    L.colar(e, 14, 8, 35, 24, G, 1)
    # caminho até a porta da frente e o caminho do curral
    for z in range(25, TAM[2]):
        for x in (24, 25):
            if e.nome(x, G, z) and "dirt" in e.nome(x, G, z) or e.nome(x, G, z) in ("minecraft:podzol",
                                                                                    "minecraft:mud"):
                e.por(x, G, z, "dirt_path" if e.chance(0.75) else "coarse_dirt")
    # pátio de lenha e toco de corte (oeste da porta)
    for x in (17, 18):
        for y in (G + 1, G + 2):
            e.por(x, y, 27, "spruce_log", axis="x")
    e.por(20, G + 1, 28, "stripped_spruce_log", axis="y")
    e.por(19, G + 1, 28, "dark_oak_slab", **L.LAJE)
    # ossos amontoados atrás da casa, montes de terra
    for x, z, h in ((20, 4, 1), (21, 4, 2), (22, 5, 1), (21, 5, 1), (28, 3, 1)):
        for y in range(h):
            e.por(x, G + 1 + y, z, "bone_block", axis="y" if y else "x")
    # árvores mortas
    L.arvore_morta(e, 6, G + 1, 4, 10, galhos=5)
    L.arvore_morta(e, 2, G + 1, 30, 8, galhos=4)
    L.arvore_morta(e, 31, G + 1, 31, 7, galhos=3, tronco="spruce_log")
    L.arvore_morta(e, 11, G + 1, 31, 6, galhos=3)
    # varal de carne no pátio da frente: dois postes, a trave e as correntes vazias
    for x in (14, 20):
        for y in range(G + 1, G + 6):
            e.por(x, y, 30, "dark_oak_log", axis="y")
    for x in range(14, 21):
        e.por(x, G + 6, 30, "dark_oak_log", axis="x")
    for x, baixo in ((15, G + 3), (16, G + 2), (17, G + 4), (18, G + 2), (19, G + 3)):
        m.corrente(e, x, 30, baixo, G + 5)
    m.mancha(e, 17, G + 1, 30, 1, 0.5)
    # poste de lanterna apagada na frente
    L.poste_lanterna(e, 22, G + 1, 27, 3, acesa=False)
    e.por(22, G + 4, 27, "chain", axis="y", waterlogged=False)
    L.mato_seco(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, chance=0.05)


def sangue(e):
    # casa de abate: debaixo das correntes, na canaleta, o arrasto até o alçapão
    for x, _ in ANIMAIS:
        m.mancha(e, x, G + 1, 11, 1, 0.45)
    m.rastro(e, [(29, 10), (26, 11), (25, 11)], G + 1, falha=0.35)
    m.mancha(e, 16, G + 1, 11, 1, 0.5)
    # porão: debaixo da bancada e no ralo
    m.mancha(e, 28, 1, 13, 1, 0.6)
    m.mancha(e, 31, 1, 14, 1, 0.5)
    # loja: um pingo atrás do balcão
    m.mancha(e, 22, G + 1, 19, 0, 1.0)


def marcadores(e):
    L.ponto(e, 21, 1, 12, "spawn:magarefe", "Lugar do Magarefe: porão de salga. Não morre no Ato 1.")
    rota = [(30, 1, 12), (38, 1, 12), (42, 2, 12), (43, G + 1, 14), (42, G + 1, 30),
            (34, G + 1, 30), (26, G + 1, 28), (25, G + 1, 23)]
    for i, (x, y, z) in enumerate(rota, 1):
        if not e.vazio(x, y, z):
            y += 1
        L.ponto(e, x, y, z, f"rota:{i:02d}", "Perseguição: porão, túnel, poço do curral, volta pela frente."
                if i == 1 else "")
    L.ponto(e, 24, G + 3, 25, "evento:porta_frente", "Abrir a porta da frente acorda o Magarefe.")
    L.ponto(e, 36, G + 1, 12, "evento:porta_ferro", "Porta de ferro de botão (lado do curral): a entrada segura (E1).")
    L.ponto(e, 43, G + 1, 12, "evento:saida_servico", "Alçapão da saída de serviço do porão (curral).")
    L.ponto(e, 25, G + 1, 14, "evento:alcapao_porao",
            "Alçapão grande (2 x 2) trancado: abre com a ordem certa das correntes.")
    for x, nome in ANIMAIS:
        L.ponto(e, x, G + 3, 12, "evento:corrente_" + nome.lower().replace("ã", "a"),
                f"Corrente '{nome}': placa em ({x}, {G + 1}, 13), alavanca em ({x}, {G + 1}, 12). Erro: balança.")
    L.ponto(e, 16, G + 2, 10, "evento:pendurado", "Corrente vazia: o Pendurado aparece aqui (quem tem mais Exposição).")
    L.ponto(e, 24, G + 6, 18, "evento:palheiro_palavra",
            "Viga do palheiro: a palavra riscada que abre o vídeo E7 (a Lore decide; não vai no repositório).")
    L.ponto(e, 26, G + 3, 12, "som:moscas", "Moscas e correntes rangendo na casa de abate.")
    L.ponto(e, 24, G + 2, 18, "evento:defumador",
            "Defumadores (x 20 e 29, z 17) soltando fumaça sem fogo.")
