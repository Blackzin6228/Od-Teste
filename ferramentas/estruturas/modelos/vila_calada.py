"""VILA CALADA (Ato 1, elemento Sangue): a vila destruída, destino do Ato 1. Prévia da mesa Modelador.

Base: a ficha "Vila Calada" em lore/fichas-estruturas.md (branch mesa-lore/vila-destruida) e o
lore/livro-do-sineiro.md. Substitui o protótipo 72 x 64 (vila_destruida.py) quando o Nikolas aprovar.

Tamanho 100 x 27 x 100 (x, y, z). Norte = z 0. Origem = canto noroeste de baixo (padrão do Minecraft).

ASSENTA NO CHÃO: a camada y 0 é o chão da vila e fica EM CIMA do terreno do mundo (nada é gravado
abaixo dela; nada é cavado). Assim a colocação do mod (PR #37) só precisa de ar e grama alta dentro
da caixa, e chão firme embaixo. Covas e o fundo do poço são "buracos" na camada y 0: o fundo é o
próprio terreno do mundo.

Partes (da entrada para o fundo):
- Campo queimado (sul, z 89-99): roça preta, cercas partidas, espantalho de cabeça para baixo,
  pegadas de cinza na estrada, todas para dentro.
- Muro baixo de pedra com estacas (x 4-95, z 3-88), quebrado em vários pontos. Portão ao sul.
- Rua principal (x 48-51) sul-norte até a capela; rua cruzada (z 54-57) leste-oeste no poço.
- Praça (x 38-61, z 46-65) com o poço seco num pódio de pedra: nicho com baú e a cruz riscada.
- 14 casas de aldeão: 7 queimadas, 5 desabadas, 2 inteiras por fora e erradas por dentro
  (ferreiro a leste, Compadre isolado a sudeste). Casa da Moça do Terço (botões no peitoril).
- Curral comprido (noroeste): ossos em fileiras certinhas, valas de lama, estandartes vermelhos.
- Morro da capela (norte, 2 blocos acima): capela sem teto (mesa com cadeira a mais, a única vela
  acesa), campanário meio desabado (escada queimada com degraus faltando, atril, sino rachado) e o
  cemitério atrás, com covas abertas e terra jogada para fora.
Acento de cor único: o vermelho do Sangue (pó de redstone apagado, cama vermelha, estandarte,
vitral vermelho). Nenhum símbolo desenhado: a Cruz de Ferro fica marcada nas pistas (desenho pendente).
"""
import math

from estrutura import Estrutura, Mistura, ruido
import modulos as m

NOME = "vila_calada"
TAM = (100, 27, 100)
SEMENTE = 911
G = 0          # chão da vila
PLAT = 2       # topo do morro da capela (2 blocos acima do chão)
LUZ_INVISIVEL = True  # grade de blocos de luz (nível 5) para monstro comum não nascer dentro do muro

CORTES = [("chao", G, G + 5)]

# partes para as prévias de perto: (nome, x0, z0, x1, z1)
PARTES = [
    ("capela_campanario", 33, 3, 68, 41),
    ("poco_praca", 37, 45, 62, 66),
    ("casa_compadre", 73, 69, 89, 84),
    ("casa_ferreiro", 67, 41, 82, 54),
    ("casa_moca_terco", 25, 56, 40, 70),
    ("casa_cama_manchada", 79, 26, 92, 39),
    ("curral", 5, 5, 34, 32),
    ("entrada_sul", 30, 82, 70, 99),
]

LAJE = {"type": "bottom", "waterlogged": False}
DEGRAU = {"half": "bottom", "shape": "straight", "waterlogged": False}
SOLTO = {"north": False, "south": False, "east": False, "west": False, "waterlogged": False}
MURETA = {"up": True, "north": "none", "south": "none", "east": "none", "west": "none", "waterlogged": False}

PEDRA_BASE = Mistura(("cobblestone", 6), ("mossy_cobblestone", 2))
PEDRA_QUEIMADA = Mistura(("cobblestone", 5), ("blackstone", 2), ("smooth_basalt", 1), ("mossy_cobblestone", 1))
PEDRA_PAREDE = Mistura(("cobblestone", 5), ("stone_bricks", 3), ("cracked_stone_bricks", 2), ("mossy_cobblestone", 2))
PEDRA_MURO = Mistura(("cobblestone", 6), ("mossy_cobblestone", 4), ("gravel", 1))
TABUA = Mistura(("spruce_planks", 14), ("dark_oak_planks", 3))
TABUA_QUEIMADA = Mistura(("dark_oak_planks", 8), ("coal_block", 3), ("blackstone", 1), ("spruce_planks", 1))
PISO = Mistura(("spruce_planks", 12), ("dark_oak_planks", 3), ("coarse_dirt", 1))
PISO_QUEIMADO = Mistura(("dark_oak_planks", 6), ("coal_block", 2), ("soul_soil", 2), ("spruce_planks", 1))
TRONCO = ("stripped_spruce_log", {"axis": "y"})
TRONCO_QUEIMADO = Mistura((("dark_oak_log", {"axis": "y"}), 5), ("coal_block", 1))
CINZA = Mistura(("gray_carpet", 3), ("light_gray_carpet", 2))
ENTULHO = Mistura(("cobblestone", 5), ("dark_oak_planks", 2), ("coal_block", 1), ("cracked_stone_bricks", 2),
                  ("mossy_cobblestone", 2), ("deepslate_tiles", 1))
ENTULHO_TOPO = Mistura((("cobblestone_slab", LAJE), 4), (("dark_oak_slab", LAJE), 2),
                       (("deepslate_tile_slab", LAJE), 2), (("stone_brick_slab", LAJE), 1),
                       (("cobblestone_stairs", dict(DEGRAU, facing="north")), 1),
                       (("cobblestone_stairs", dict(DEGRAU, facing="east")), 1),
                       (("deepslate_tile_stairs", dict(DEGRAU, facing="west")), 1))
CALCAMENTO = Mistura(("cobblestone", 6), ("stone_bricks", 3), ("mossy_cobblestone", 3), ("cracked_stone_bricks", 2),
                     ("mossy_stone_bricks", 1), ("grass_block", 1))
LISO = Mistura(("polished_andesite", 6), ("stone_bricks", 2), ("cracked_stone_bricks", 1))
RUA = Mistura(("dirt_path", 12), ("gravel", 2), ("coarse_dirt", 2), ("cobblestone", 1))
SULCO = Mistura(("coarse_dirt", 3), ("gravel", 2), ("dirt", 1))
QUEIMADO_CHAO = Mistura(("soul_soil", 3), ("coarse_dirt", 3), ("dirt", 1), ("blackstone", 1))
POCO_PEDRA = Mistura(("stone_bricks", 3), ("mossy_stone_bricks", 2), ("cracked_stone_bricks", 2), ("cobblestone", 1))
MURETA_MIX = Mistura(("cobblestone_wall", 3), ("mossy_cobblestone_wall", 2))

# ruas: retângulos (x0, z0, x1, z1)
RUAS = [
    (48, 40, 51, 99),   # rua principal (sul -> norte, até a escada do morro)
    (48, 35, 51, 37),   # adro da capela (em cima do morro)
    (0, 54, 99, 57),    # rua cruzada (leste-oeste, passa no poço)
    # oeste
    (11, 40, 12, 43), (11, 42, 21, 43), (20, 40, 21, 53), (16, 30, 17, 43),
    (13, 52, 14, 53), (25, 52, 26, 53), (12, 58, 13, 59), (18, 58, 19, 69),
    (13, 68, 28, 69), (13, 70, 14, 71), (27, 70, 28, 72), (36, 62, 37, 64),
    # leste
    (66, 41, 85, 42), (66, 41, 67, 53), (78, 18, 79, 42), (71, 18, 79, 19),
    (78, 20, 84, 21), (73, 35, 74, 40), (78, 32, 81, 33), (74, 52, 75, 53),
    (81, 58, 82, 71),
]
PRACA = (38, 46, 61, 65)
MORRO = (35, 65, 6, 37)  # platô do morro: x0, x1, z0, z1
ZONAS_QUEIMADAS = []     # (x0, z0, x1, z1, força), preenchida pelas casas e árvores


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    ZONAS_QUEIMADAS.clear()
    chao(e)
    ruas(e)
    escada_do_morro(e)
    praca(e)
    muro(e)
    capela(e)
    campanario(e)
    cemiterio(e)
    poco(e)
    casas(e)
    curral(e)
    campos(e)
    arvores_queimadas(e)
    detalhes(e)
    chao_queimado(e)
    desgaste(e)
    sangue(e)
    luzes(e)
    vegetacao(e)
    conectar(e)
    pistas(e)
    return e


# ---------------------------------------------------------------------------
# Chão (camada que assenta em cima do terreno) e morro
# ---------------------------------------------------------------------------
def chao_y(x, z):
    """Altura do topo do chão da vila em (x, z): 0 no plano, 1 na encosta, 2 no morro."""
    if 47 <= x <= 52 and z in (38, 39):  # escada do adro
        return 1 if z == 38 else 0
    px0, px1, pz0, pz1 = MORRO
    d = max(max(px0 - x, 0, x - px1), max(pz0 - z, 0, z - pz1))
    if d == 0:
        return PLAT
    if d <= 1 + int(ruido(x, 9, z, 3.0, SEMENTE + 40) * 2):
        return 1
    return 0


def chao(e):
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            borda = min(x, z, TAM[0] - 1 - x, TAM[2] - 1 - z)
            # borda irregular: o chão da vila "morre" no terreno do mundo
            if borda < 2 and ruido(x, 3, z, 2.0, SEMENTE + 41) < (0.55 if borda == 0 else 0.3):
                continue
            topo = chao_y(x, z)
            for y in range(topo):
                e.por(x, y, z, "dirt")
            n = ruido(x, 0, z, 5.0, SEMENTE + 1)
            if n < 0.22:
                e.por(x, topo, z, "coarse_dirt")
            elif n < 0.3:
                e.por(x, topo, z, "dirt")
            else:
                e.por(x, topo, z, "grass_block", snowy=False)


def em_rua(x, z):
    for x0, z0, x1, z1 in RUAS:
        borda_ruido = ruido(x, 1, z, 2.0, SEMENTE + 2)
        folga = 1 if borda_ruido > 0.72 else (-1 if borda_ruido < 0.2 else 0)
        if x0 - folga <= x <= x1 + folga and z0 - folga <= z <= z1 + folga:
            return True
    return False


def ruas(e):
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            if not em_rua(x, z):
                continue
            topo = chao_y(x, z)
            if 48 <= x <= 51 and z >= 66 and x in (48, 51):
                b = SULCO  # sulcos de roda na rua principal
            elif ruido(x, 8, z, 2.5, SEMENTE + 42) > 0.8:
                b = "mud"  # poça escura
            else:
                b = RUA
            e.por(x, topo, z, b)
            e.ar_onde_vazio(x, topo + 1, z, x, topo + 3, z)


def escada_do_morro(e):
    for x in range(47, 53):
        e.por(x, 0, 38, "stone_bricks")
        e.por(x, 1, 38, "stone_bricks")
        e.por(x, 2, 38, "stone_brick_stairs", facing="north", **DEGRAU)
        e.por(x, 0, 39, "stone_bricks")
        e.por(x, 1, 39, "stone_brick_stairs", facing="north", **DEGRAU)
        e.ar_onde_vazio(x, 3, 38, x, 5, 38)
        e.ar_onde_vazio(x, 2, 39, x, 4, 39)


def praca(e):
    x0, z0, x1, z1 = PRACA
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            canto = (x in (x0, x1)) + (z in (z0, z1))
            if canto and e.chance(0.45):
                continue  # borda irregular: a grama come o calçamento
            liso = 43 <= x <= 55 and 49 <= z <= 61  # "o chão em volta liso, como se muita gente tivesse parado ali"
            e.por(x, G, z, LISO if liso else CALCAMENTO)
            e.ar_onde_vazio(x, G + 1, z, x, G + 3, z)


def muro(e):
    X0, X1, Z0, Z1 = 4, 95, 3, 88
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        if z == Z1 and 46 <= x <= 53:
            continue  # portão sul
        if x in (X0, X1) and 53 <= z <= 58:
            continue  # saídas da rua cruzada
        n = ruido(x, 5, z, 2.0, SEMENTE + 43)
        if n > 0.74:
            continue  # muro caído
        base = chao_y(x, z)
        e.por(x, base + 1, z, PEDRA_MURO)
        if n < 0.45:
            e.por(x, base + 2, z, PEDRA_MURO)
        elif (x + z) % 4 == 0:
            e.por(x, base + 2, z, "spruce_fence", **SOLTO)  # estaca
            if n < 0.62:
                e.por(x, base + 3, z, "spruce_fence", **SOLTO)
    # pilares do portão (o portão caiu na estrada)
    for x in (45, 54):
        for y in range(1, 4):
            e.por(x, y, Z1, "stone_bricks" if y < 3 else "cracked_stone_bricks")
    e.por(49, 1, 90, "dark_oak_trapdoor", facing="south", half="bottom", open=False, powered=False,
          waterlogged=False)
    e.por(50, 1, 91, "dark_oak_trapdoor", facing="east", half="bottom", open=False, powered=False,
          waterlogged=False)


# ---------------------------------------------------------------------------
# Capela e campanário (no morro, chão em y = PLAT)
# ---------------------------------------------------------------------------
def capela(e):
    X0, X1, Z0, Z1 = 41, 57, 18, 34
    Y = PLAT
    BASE = Mistura(("cobblestone", 4), ("stone_bricks", 4), ("mossy_cobblestone", 2), ("cracked_stone_bricks", 2))
    PISO_C = Mistura(("stone_bricks", 5), ("polished_andesite", 3), ("cracked_stone_bricks", 2),
                     ("mossy_stone_bricks", 1))
    TABUA_C = Mistura(("spruce_planks", 6), ("dark_oak_planks", 3), ("coal_block", 1))
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            borda = x in (X0, X1) or z in (Z0, Z1)
            e.por(x, Y, z, BASE if borda else PISO_C)
    # paredes de pedra embaixo e tábua em cima, comidas por cima (sem teto)
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        canto = x in (X0, X1) and z in (Z0, Z1)
        pilastra = (x in (X0, X1) and z in (22, 26, 30)) or (z in (Z0, Z1) and x in (45, 53))
        n = ruido(x, 0, z, 2.5, SEMENTE + 50)
        h = Y + 8 if (canto or pilastra) else Y + 7 - int(n * 3.5)
        if x in (X0, X1) and z in (22, 28):
            h = max(h, Y + 8)  # onde as vigas ainda se apoiam
        if z == Z1 and 47 <= x <= 51:
            h = max(h, Y + 9 if x == 49 else Y + 6)  # fachada: ponta no meio
        for y in range(Y + 1, h + 1):
            pedra = y <= Y + 2 or canto or pilastra
            e.por(x, y, z, BASE if pedra else TABUA_C)
    # restos das vigas do telhado (uma inteira de um lado, a outra quebrada)
    for z in (22, 28):
        for x in range(X0 + 1, X0 + 6):
            e.por(x, Y + 8, z, "dark_oak_log", axis="x")
        if z == 22:
            for x in range(X1 - 5, X1):
                e.por(x, Y + 8, z, "dark_oak_log", axis="x")
    # porta: vão de 3, folha caída para fora
    m.vao(e, 48, Y + 1, Z1, 50, Y + 3, Z1)
    e.por(49, Y + 4, Z1, "chiseled_stone_bricks")
    e.por(47, Y + 1, Z1 + 2, "spruce_trapdoor", facing="south", half="bottom", open=False, powered=False,
          waterlogged=False)
    # vitrais quebrados: quase todo o vidro caiu; sobra um caco vermelho aqui e ali
    def vitral(x, z):
        for y in range(Y + 3, Y + 6):
            if e.vazio(x, y, z):
                continue
            r = e.rng.random()
            if r < 0.68:
                e.ar(x, y, z)
            elif r < 0.86:
                e.por(x, y, z, "glass_pane", **SOLTO)
            else:
                e.por(x, y, z, "red_stained_glass_pane", **SOLTO)
    for z in (20, 24, 28, 32):
        vitral(X0, z)
        vitral(X1, z)
    for x in (45, 49, 53):
        vitral(x, Z0)
    # porta para o campanário (parede leste)
    m.vao(e, X1, Y + 1, 31, X1, Y + 2, 31)
    e.ar_onde_vazio(X0 + 1, Y + 1, Z0 + 1, X1 - 1, Y + 9, Z1 - 1)

    # a mesa, com uma cadeira a mais virada para a porta, e a única vela acesa da vila
    m.mesa(e, 45, 21, 53, 21, Y + 1)
    for x in range(45, 54):
        m.cadeira(e, x, Y + 1, 20, "south")
    m.cadeira(e, 49, Y + 1, 22, "south")  # a cadeira a mais
    m.vela(e, 49, Y + 2, 21, 1, acesa=True)  # A VELA (refúgio)
    m.vela(e, 46, Y + 2, 21, 2, acesa=False)
    m.vela(e, 52, Y + 2, 21, 1, acesa=False)
    # bancos virados (degrau em qualquer direção, alguns de ponta-cabeça)
    lados = ("north", "south", "east", "west")
    for z in range(24, 33, 2):
        for x in list(range(42, 48)) + list(range(51, 57)):
            if e.chance(0.18):
                continue
            f = lados[int(e.rng.random() * 4)]
            meia = "top" if e.chance(0.35) else "bottom"
            e.por(x, Y + 1, z, "spruce_stairs", facing=f, half=meia, shape="straight", waterlogged=False)
    m.chao_com_musgo(e, X0 + 1, Z0 + 1, X1 - 1, Z1 - 1, Y + 1, 0.22)
    m.teias(e, X0 + 1, Y + 3, Z0 + 1, X1 - 1, Y + 6, Z1 - 1, 10)


def campanario(e):
    X0, X1, Z0, Z1 = 58, 63, 29, 34
    Y = PLAT
    BASE_T = Mistura(("stone_bricks", 6), ("mossy_stone_bricks", 2), ("cracked_stone_bricks", 3), ("cobblestone", 1))
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, Y, z, "stone_bricks")
    # paredes: o lado norte desabou (para pela metade); o resto vai até o alto
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        canto = x in (X0, X1) and z in (Z0, Z1)
        n = ruido(x, 0, z, 1.4, SEMENTE + 60)
        if z == Z0 or (x == X0 and z == Z0 + 1):
            topo = Y + 10 + int(n * 4)
        else:
            topo = Y + 21 - int(n * 2)
        for y in range(Y + 1, topo + 1):
            if canto:
                b = "chiseled_stone_bricks" if y % 5 == 0 else "stone_bricks"
            else:
                b = BASE_T
            e.por(x, y, z, b)
    # escada de madeira queimada em caracol, com degraus faltando ("qualquer bloco resolve")
    anel = [(59, 33), (60, 33), (61, 33), (62, 33), (62, 32), (62, 31), (62, 30),
            (61, 30), (60, 30), (59, 30), (59, 31), (59, 32)]
    rumo = {(1, 0): "east", (-1, 0): "west", (0, 1): "south", (0, -1): "north"}
    faltando = (4, 9, 11)
    for i in range(14):
        if i in faltando:
            continue
        cx, cz = anel[i % 12]
        nx, nz = anel[(i + 1) % 12]
        mat = "dark_oak_stairs" if e.chance(0.75) else "spruce_stairs"
        e.por(cx, Y + 1 + i, cz, mat, facing=rumo[(nx - cx, nz - cz)], **DEGRAU)
    # andar do sino (y = PLAT + 15): tábuas furadas
    protegidos = {(60, 32), (61, 32), (62, 31), (61, 31), (59, 30)}
    for x in range(59, 63):
        for z in range(30, 34):
            if (x, z) in (anel[0], anel[1]):
                continue  # passagem da escada
            if (x, z) not in protegidos and e.chance(0.15):
                continue
            e.por(x, Y + 15, z, TABUA_QUEIMADA)
    e.ar_onde_vazio(X0 + 1, Y + 1, Z0 + 1, X1 - 1, Y + 22, Z1 - 1)
    # o sino rachado, torto numa viga quebrada; o atril ao pé dele
    for x in range(59, 62):
        e.por(x, Y + 18, 32, "dark_oak_log", axis="x")
    e.por(60, Y + 17, 32, "bell", attachment="ceiling", facing="east", powered=False)
    e.por(62, Y + 16, 31, "lectern", facing="west", has_book=False, powered=False)  # LIVRO DO SINEIRO
    # vão quebrado (sul): de lá se vê o horizonte (girar a vila ao colocar para apontar a vila comum)
    m.vao(e, 60, Y + 16, Z1, 61, Y + 18, Z1)
    # seteiras
    m.vao(e, 60, Y + 5, Z1, 60, Y + 6, Z1)
    m.vao(e, X1, Y + 9, 31, X1, Y + 10, 31)
    # porta para a capela
    m.vao(e, X0, Y + 1, 31, X0, Y + 2, 31)
    # entulho do lado que caiu (norte), em cima do morro
    entulho(e, 58, 24, 65, 28, 3, SEMENTE + 61, base=Y)
    for z in range(29, 35):
        if e.chance(0.6):
            m.trepadeira(e, X1 + 1, z, Y + 12, "west", 3 + int(e.rng.random() * 6))


def cemiterio(e):
    """Atrás da capela: covas abertas e vazias, lápides sem nome, terra jogada PARA FORA."""
    Y = PLAT
    for x, z in m.perimetro(36, 6, 64, 16):
        if z == 16 and 46 <= x <= 52:
            continue  # entrada
        if ruido(x, 6, z, 1.6, SEMENTE + 70) > 0.7:
            continue
        e.por(x, Y + 1, z, "spruce_fence", **SOLTO)
    lapide = Mistura((("cobblestone_wall", MURETA), 3), (("mossy_cobblestone_wall", MURETA), 2),
                     (("stone_brick_stairs", dict(DEGRAU, facing="north")), 1))
    for linha in (9, 13):
        for gx in range(39, 61, 3):
            for z in (linha, linha + 1):
                e.ar(gx, Y, z)       # cova de 2 de fundo (o fundo é a terra do morro)
                e.ar(gx, Y - 1, z)
                e.ar_onde_vazio(gx, Y + 1, z, gx, Y + 2, z)
            if gx == 51 and linha == 9:
                e.por(gx, Y + 1, linha - 1, "chiseled_stone_bricks")  # lápide com o risco da Cruz de Ferro
            else:
                e.por(gx, Y + 1, linha - 1, lapide)
            e.por(gx + 1, Y + 1, linha, "coarse_dirt")
            if e.chance(0.7):
                e.por(gx + 1, Y + 1, linha + 1, "dirt")
            if e.chance(0.4):
                e.por(gx - 1, Y + 1, linha + 1, "gravel")


# ---------------------------------------------------------------------------
# Poço (no meio da praça, num pódio de pedra; a descida é curta)
# ---------------------------------------------------------------------------
def poco(e):
    cx, cz = 49, 55
    for x in range(45, 54):
        for z in range(51, 60):
            anel = max(abs(x - cx), abs(z - cz))
            if anel == 4:
                e.por(x, 1, z, "stone_brick_slab", **LAJE)  # degrau
            elif anel in (2, 3):
                e.por(x, 1, z, POCO_PEDRA)
            else:  # o cano do poço, seco: o fundo é o terreno do mundo
                for y in range(0, 4):
                    e.ar(x, y, z)
            if anel == 3 and (x, z) not in ((49, 52), (49, 58)):
                e.por(x, 2, z, MURETA_MIX, **MURETA)
            elif anel in (2, 3, 4):
                e.ar_onde_vazio(x, 2, z, x, 3, z)
    # nicho no fundo: baú de mantimentos e a Cruz de Ferro riscada na pedra
    e.por(49, 0, 53, "chest", facing="south", type="single", waterlogged=False)
    e.por(49, 1, 53, "chiseled_stone_bricks")
    for y in (0, 1):
        e.por(50, y, 56, "ladder", facing="north", waterlogged=False)
    # postes, viga, metade do telhadinho e a corda podre (corrente curta)
    for x in (46, 52):
        for y in (3, 4):
            e.por(x, y, cz, "spruce_fence", **SOLTO)
    for x in range(46, 53):
        e.por(x, 5, cz, "spruce_log", axis="x")
    for x in range(46, 50):
        e.por(x, 5, cz - 1, "spruce_stairs", facing="south", **DEGRAU)
        e.por(x, 5, cz + 1, "spruce_stairs", facing="north", **DEGRAU)
        e.por(x, 6, cz, "spruce_slab", **LAJE)
    e.por(55, 1, 57, "spruce_stairs", facing="east", **DEGRAU)  # pedaço do telhado no chão
    for y in (3, 4):
        e.por(49, y, cz, "chain", axis="y", waterlogged=False)
    # a placa da beira: "Aqui ele ouvia." (texto vem do mod/admin)
    e.por(47, 3, 58, "spruce_sign", rotation=0, waterlogged=False)


# ---------------------------------------------------------------------------
# Casas de aldeão
# ---------------------------------------------------------------------------
def entulho(e, x0, z0, x1, z1, altura, semente, base=G):
    """Monte de entulho: mais alto no meio, irregular, com laje/degrau por cima."""
    cx, cz = (x0 + x1) / 2, (z0 + z1) / 2
    rx, rz = max(1, (x1 - x0) / 2 + 0.5), max(1, (z1 - z0) / 2 + 0.5)
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not (0 <= x < TAM[0] and 0 <= z < TAM[2]):
                continue
            d = math.sqrt(((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2)
            h = int((1.15 - d) * altura * (0.5 + ruido(x, 2, z, 1.7, semente)) + 0.4)
            y = base + 1
            while y < base + 1 + h and e.vazio(x, y, z):
                e.por(x, y, z, "gravel" if y == base + 1 and e.chance(0.2) else ENTULHO)
                y += 1
            if e.vazio(x, y, z) and e.cheio(x, y - 1, z) and e.chance(0.55):
                e.por(x, y, z, ENTULHO_TOPO)


def casa(e, x0, z0, x1, z1, porta, estado, alt=4, chamine=None, pedra=False, sem=0, caido="east"):
    """estado: "queimada" (paredes comidas, só caibros), "desabada" (meio telhado no chão, do lado
    'caido'), "inteira" (telhado e paredes inteiros, porta aberta). porta: lado da porta."""
    s = SEMENTE + sem * 31
    topo = G + alt
    queimada = estado == "queimada"
    tabua = TABUA_QUEIMADA if queimada else TABUA
    tronco = TRONCO_QUEIMADO if queimada else TRONCO
    if pedra:
        tabua = PEDRA_PAREDE
        tronco = "stone_bricks"
    if queimada:
        ZONAS_QUEIMADAS.append((x0, z0, x1, z1, 1.0))
    meio_x = (x0 + x1) // 2
    caido_leste = caido == "east"
    com_telhado = estado in ("desabada", "inteira")

    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            borda = x in (x0, x1) or z in (z0, z1)
            e.por(x, G, z, (PEDRA_QUEIMADA if queimada else PEDRA_BASE) if borda
                  else (PISO_QUEIMADO if queimada else PISO))

    altura = None
    if com_telhado:
        altura = m.telhado_duas_aguas(e, x0 - 1, x1 + 1, z0 - 1, z1 + 1, topo + 1, "dark_oak_stairs",
                                      "spruce_planks", ("dark_oak_slab", LAJE),
                                      buracos=0.06 if estado == "desabada" else 0.0)
        for z in (z0, z1):
            m.oitao(e, z, x0 - 1, x1 + 1, topo + 1, lambda x, y, zz, c: tabua)
        if estado == "desabada":
            for x in range(x0 - 1, x1 + 2):
                lado = x >= meio_x if caido_leste else x <= meio_x
                if not lado:
                    continue
                for z in range(z0 - 1, z1 + 2):
                    for y in range(topo + 1, topo + 2 + (x1 - x0) // 2 + 1):
                        if ruido(x, y, z, 1.8, s) < 0.85:
                            e.tirar(x, y, z)
    for x, z in m.perimetro(x0, z0, x1, z1):
        canto = x in (x0, x1) and z in (z0, z1)
        n = ruido(x, 0, z, 2.2, s)
        if queimada:
            h = topo if canto else topo - int(n * 3.4)
        elif estado == "desabada":
            h = topo
            longe = (x - meio_x) if caido_leste else (meio_x - x)
            if longe > 0:
                h = max(G + 1, topo - int(longe * 1.1 + n * 2))
        else:
            h = topo
        for y in range(G + 1, h + 1):
            if y == G + 1:
                b = PEDRA_QUEIMADA if queimada else PEDRA_BASE
            elif canto:
                b = tronco
            elif not pedra and com_telhado and y == topo:
                b = ("spruce_log", {"axis": "x" if z in (z0, z1) else "z"})
            else:
                b = tabua
            e.por(x, y, z, b)

    if queimada:
        # só alguns caibros carbonizados ficaram, e só onde a parede ainda segura
        for z in range(z0, z1 + 1, 3):
            if e.chance(0.3) or e.vazio(x0, topo, z) or e.vazio(x1, topo, z):
                continue
            ate = int(e.rng.random() * ((x1 - x0) // 2 + 1)) + 1
            for k in range(ate):
                if x0 + k <= x1 - k:
                    e.por(x0 + k, topo + 1 + k, z, "dark_oak_stairs", facing="east", **DEGRAU)
                if e.chance(0.6) and x0 + k < x1 - k:
                    e.por(x1 - k, topo + 1 + k, z, "dark_oak_stairs", facing="west", **DEGRAU)

    dx, dz = m.DIRECAO[porta]
    if porta in ("north", "south"):
        px, pz = (x0 + x1) // 2, (z0 if porta == "north" else z1)
    else:
        px, pz = (x0 if porta == "west" else x1), (z0 + z1) // 2
    e.ar(px, G + 1, pz)
    e.ar(px, G + 2, pz)
    if com_telhado:
        m.porta(e, px, G + 1, pz, porta, hinge="left", aberta=True)
    else:
        fx, fz = px + dx, pz + dz  # a porta caiu para fora
        if 0 <= fx < TAM[0] and 0 <= fz < TAM[2]:
            e.por(fx, G + 1, fz, "dark_oak_trapdoor", facing=porta, half="bottom", open=False, powered=False,
                  waterlogged=False)
    for x, z in m.perimetro(x0, z0, x1, z1):
        if x in (x0, x1) and z in (z0, z1):
            continue
        ao_longo = "x" if z in (z0, z1) else "z"
        pos = x if ao_longo == "x" else z
        if (pos - (x0 if ao_longo == "x" else z0)) % 3 == 2 and (x, z) != (px, pz):
            for y in (G + 2, G + 3):
                if e.vazio(x, y, z):
                    continue
                if queimada or (estado == "desabada" and e.chance(0.5)) or (estado == "inteira" and e.chance(0.08)):
                    e.ar(x, y, z)
                else:
                    e.por(x, y, z, "glass_pane" if (estado == "inteira" or e.chance(0.6)) else "iron_bars", **SOLTO)

    if chamine:
        cx, cz = chamine
        for y in range(G + 1, topo + 4):
            e.por(cx, y, cz, "bricks" if e.chance(0.85) else "cracked_stone_bricks")
        e.por(cx, topo + 4, cz, "bricks")
        ix = cx + (1 if cx == x0 else -1 if cx == x1 else 0)
        iz = cz + (1 if cz == z0 else -1 if cz == z1 else 0)
        e.por(ix, G + 1, iz, "campfire", facing="north", lit=False, signal_fire=False, waterlogged=False)

    e.ar_onde_vazio(x0 + 1, G + 1, z0 + 1, x1 - 1, topo + (3 if altura else 2), z1 - 1,
                    condicao=(lambda x, y, z: y < altura(x) - 1) if altura else None)

    if estado == "desabada":
        if caido_leste:
            entulho(e, meio_x, z0 + 1, x1 + 2, z1 - 1, 3, s)
        else:
            entulho(e, x0 - 2, z0 + 1, meio_x, z1 - 1, 3, s)
    return {"x0": x0, "z0": z0, "x1": x1, "z1": z1, "topo": topo, "estado": estado, "porta": (px, pz)}


def mobiliar(e, c, itens):
    """itens: lista de (dx, dz, tipo, extra) relativos ao canto noroeste de dentro (x0+1, z0+1)."""
    ix, iz = c["x0"] + 1, c["z0"] + 1
    for dx, dz, tipo, extra in itens:
        x, z, y = ix + dx, iz + dz, G + 1
        if not e.vazio(x, y, z):
            continue
        if tipo == "cama":
            fx, fz = m.DIRECAO[extra.get("facing", "north")]
            if not e.vazio(x + fx, y, z + fz):
                continue
            m.cama(e, x, y, z, extra.get("facing", "north"), extra.get("cor", "white"))
        elif tipo == "cadeira":
            m.cadeira(e, x, y, z, extra.get("olha", "north"))
        elif tipo == "barrel":
            e.por(x, y, z, "barrel", facing=extra.get("facing", "up"), open=False)
        elif tipo == "chest":
            e.por(x, y, z, "chest", facing=extra.get("facing", "south"), type="single", waterlogged=False)
        elif tipo in ("furnace", "smoker"):
            e.por(x, y, z, tipo, facing=extra.get("facing", "south"), lit=False)
        elif tipo == "brasa":
            e.por(x, y, z, "campfire", facing="north", lit=False, signal_fire=False, waterlogged=False)
        else:
            e.por(x, y, z, tipo)


def cinzas(e, c, chance):
    for x in range(c["x0"] + 1, c["x1"]):
        for z in range(c["z0"] + 1, c["z1"]):
            if e.vazio(x, G + 1, z) and e.cheio(x, G, z) and ruido(x, 5, z, 2.0, SEMENTE + 13) > 1 - chance:
                e.por(x, G + 1, z, CINZA)


def casas(e):
    # --- 7 queimadas -------------------------------------------------------
    for (x0, z0, x1, z1, porta, chamine, sem, itens) in (
            (8, 33, 15, 39, "south", None, 1,
             [(0, 0, "brasa", {}), (5, 0, "barrel", {}), (2, 2, "coal_block", {}),
              (0, 3, "cama", {"facing": "north", "cor": "light_gray"})]),
            (19, 33, 26, 39, "south", (26, 36), 2,
             [(0, 0, "furnace", {}), (3, 1, "cauldron", {}), (5, 4, "barrel", {}), (1, 3, "brasa", {})]),
            (22, 44, 29, 51, "south", None, 3,
             [(1, 1, "brasa", {}), (4, 2, "coal_block", {}), (5, 5, "barrel", {"facing": "north"})]),
            (9, 60, 16, 67, "north", (16, 63), 4,
             [(0, 5, "brasa", {}), (1, 5, "barrel", {}), (3, 2, "coal_block", {}),
              (0, 0, "cama", {"facing": "south", "cor": "light_gray"})]),
            (10, 72, 17, 79, "north", (10, 75), 5,
             [(4, 4, "brasa", {}), (5, 0, "barrel", {}), (2, 5, "cauldron", {})]),
            (68, 10, 75, 17, "south", None, 6,
             [(0, 0, "brasa", {}), (5, 1, "coal_block", {}), (2, 4, "barrel", {})]),
            (70, 27, 77, 34, "south", (77, 30), 7,
             [(1, 1, "brasa", {}), (0, 5, "furnace", {"facing": "north"}), (4, 3, "coal_block", {})])):
        c = casa(e, x0, z0, x1, z1, porta, "queimada", alt=4, chamine=chamine, sem=sem)
        mobiliar(e, c, itens)
        cinzas(e, c, 0.55)

    # --- 5 desabadas -------------------------------------------------------
    # oeste, ao norte da rua cruzada (o lado oeste caiu: a mobília ficou no leste)
    h = casa(e, 10, 44, 17, 51, "south", "desabada", alt=4, chamine=(10, 47), sem=8, caido="west")
    mobiliar(e, h, [(5, 0, "cama", {"facing": "south"}), (5, 3, "chest", {"facing": "west"}),
                    (4, 0, "barrel", {}), (5, 5, "crafting_table", {})])
    m.teias(e, 11, G + 2, 45, 16, G + 4, 50, 6)

    # Casa da Moça do Terço: peitoril com um botão de pedra para cada nome e UM espaço vazio na ponta
    h = casa(e, 28, 59, 35, 67, "east", "desabada", alt=4, sem=9, caido="east")
    for z in range(60, 67):
        e.por(29, G + 1, z, "stripped_spruce_log", axis="z")
    for z in range(60, 66):  # do mais velho (norte) ao mais novo; z 66 fica vazio
        e.por(29, G + 2, z, "stone_button", face="floor", facing="east", powered=False)
    e.ar(29, G + 2, 66)
    mobiliar(e, h, [(1, 1, "cama", {"facing": "north"}), (1, 6, "chest", {"facing": "east"}),
                    (2, 6, "barrel", {})])
    m.teias(e, 29, G + 3, 60, 33, G + 4, 66, 5)

    h = casa(e, 24, 73, 31, 80, "north", "desabada", alt=4, chamine=(24, 77), sem=10, caido="west")
    mobiliar(e, h, [(5, 1, "cama", {"facing": "south"}), (5, 5, "chest", {"facing": "west"}),
                    (4, 5, "barrel", {}), (5, 3, "smoker", {"facing": "west"})])
    m.teias(e, 25, G + 2, 74, 30, G + 4, 79, 6)

    h = casa(e, 80, 12, 87, 19, "south", "desabada", alt=4, chamine=(87, 15), sem=11, caido="east")
    mobiliar(e, h, [(0, 0, "cama", {"facing": "south"}), (0, 3, "bookshelf", {}), (0, 4, "chest", {"facing": "east"}),
                    (1, 0, "barrel", {})])
    m.teias(e, 81, G + 2, 13, 86, G + 4, 18, 6)

    # a casa da cama manchada: lençol vermelho, mancha e arrasto até a porta
    h = casa(e, 82, 29, 89, 36, "west", "desabada", alt=4, sem=12, caido="east")
    mobiliar(e, h, [(1, 1, "cama", {"facing": "north", "cor": "red"}), (0, 5, "chest", {"facing": "east"}),
                    (0, 4, "barrel", {})])
    m.teias(e, 83, G + 2, 30, 88, G + 4, 35, 6)

    # --- 2 inteiras por fora, erradas por dentro ---------------------------
    ferreiro(e)
    compadre(e)


def ferreiro(e):
    """Casa do ferreiro (leste): pedra, telhado inteiro. Forja apagada, bigorna rachada, falta o martelo."""
    casa(e, 70, 44, 79, 51, "south", "inteira", alt=4, chamine=(70, 46), pedra=True, sem=13)
    for z in (47, 48):
        e.por(71, G + 1, z, "furnace", facing="east", lit=False)
        e.por(71, G + 2, z, "bricks")
    e.por(71, G + 1, 49, "cauldron")
    e.por(74, G + 1, 47, "damaged_anvil", facing="east")
    e.por(78, G + 1, 45, "chest", facing="west", type="single", waterlogged=False)
    e.por(78, G + 1, 50, "barrel", facing="up", open=False)
    e.por(77, G + 1, 50, "barrel", facing="north", open=False)
    e.por(76, G + 1, 45, "crafting_table")
    # viga com correntes (onde se pendurava o que se forjava)
    for x in range(72, 78):
        e.por(x, G + 4, 48, "dark_oak_log", axis="x")
    for x, comp in ((73, 2), (75, 1), (76, 2)):
        for y in range(G + 4 - comp, G + 4):
            e.por(x, y, 48, "chain", axis="y", waterlogged=False)
    m.teias(e, 71, G + 3, 45, 78, G + 4, 50, 5)


def compadre(e):
    """Casa do Compadre (sudeste, isolada): a única com telhado inteiro e porta aberta. Mesa posta para
    muita gente, pratos vazios, e a única cama limpa da vila. Limpa demais: sem teia, sem pó."""
    casa(e, 76, 72, 86, 81, "north", "inteira", alt=4, sem=14)
    m.mesa(e, 78, 76, 84, 77, G + 1)
    for x in range(78, 85):
        m.cadeira(e, x, G + 1, 75, "south")
        m.cadeira(e, x, G + 1, 78, "north")
        for z in (76, 77):
            e.por(x, G + 2, z, "white_carpet")  # pratos
    m.cadeira(e, 77, G + 1, 76, "east")
    m.cama(e, 85, G + 1, 80, "north", "white")  # pé em z 80, cabeceira em z 79
    e.por(77, G + 1, 80, "chest", facing="east", type="single", waterlogged=False)
    e.por(85, G + 1, 73, "crafting_table")
    e.por(77, G + 1, 73, "barrel", facing="up", open=False)
    e.por(85, G + 1, 76, "flower_pot")


# ---------------------------------------------------------------------------
# Curral, campos, árvores, detalhes
# ---------------------------------------------------------------------------
def curral(e):
    """Curral comprido (oeste): cercado enorme, ossos em fileiras certinhas, valas de terra avermelhada."""
    X0, X1, Z0, Z1 = 8, 31, 8, 29
    for x in range(X0 + 1, X1):
        for z in range(Z0 + 1, Z1):
            e.por(x, G, z, Mistura(("coarse_dirt", 4), ("dirt", 2), ("grass_block", 1)))
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        if z == Z1 and x in (16, 17):
            continue  # porteira aberta
        if ruido(x, 3, z, 1.5, SEMENTE + 15) > 0.78:
            continue  # tábuas arrancadas
        e.por(x, G + 1, z, "spruce_fence", **SOLTO)
    for z in (12, 18, 24):  # valas
        for x in range(X0 + 2, X1 - 1):
            e.por(x, G, z, "mud")
    for z in (10, 15, 21, 27):  # ossos de gado, certinhos demais
        for x in range(X0 + 3, X1 - 1, 3):
            e.por(x, G + 1, z, "bone_block", axis="x")
    # estandartes vermelhos rasgados em mourões (o símbolo da Cruz de Ferro entra depois)
    for x, z, lado, bx in ((X0, 14, "west", X0 - 1), (X0, 22, "west", X0 - 1), (X1, 11, "east", X1 + 1)):
        for y in range(G + 1, G + 4):
            e.por(x, y, z, "spruce_log", axis="y")
        e.por(bx, G + 2, z, "red_wall_banner", facing=lado)
    e.por(28, G + 1, 26, "hay_block", axis="x")
    e.por(29, G + 1, 26, "hay_block", axis="z")
    e.por(10, G + 1, 27, "cauldron")
    e.por(30, G + 1, 9, "composter", level=1)


def plantacao(e, x0, z0, x1, z1):
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if em_rua(x, z) or e.nome(x, G, z) is None or (x, G + 1, z) in e.blocos:
                continue
            n = ruido(x, 4, z, 3.0, SEMENTE + 16)
            if z % 4 == 0:
                e.por(x, G, z, "coarse_dirt")
            elif n < 0.5:
                e.por(x, G, z, QUEIMADO_CHAO)
                if e.chance(0.12):
                    e.por(x, G + 1, z, "dead_bush")
            else:
                e.por(x, G, z, "farmland", moisture=0)
                if e.chance(0.45):
                    e.por(x, G + 1, z, "wheat", age=int(e.rng.random() * 3))


def campos(e):
    # campo queimado da entrada (fora do muro)
    plantacao(e, 0, 90, 99, 99)
    for x in range(0, 100):
        if 46 <= x <= 53 or e.nome(x, G, 90) is None:
            continue
        if ruido(x, 6, 90, 1.5, SEMENTE + 17) < 0.6 and e.vazio(x, G + 1, 90):
            e.por(x, G + 1, 90, "spruce_fence", **SOLTO)
    # roça queimada dentro do muro (sudeste)
    plantacao(e, 56, 70, 72, 86)
    # espantalho de cabeça para baixo (cabeça no chão, pernas para o céu)
    e.por(40, G + 1, 94, "carved_pumpkin", facing="south")
    e.por(40, G + 2, 94, "hay_block", axis="y")
    e.por(39, G + 2, 94, "spruce_fence", **SOLTO)
    e.por(41, G + 2, 94, "spruce_fence", **SOLTO)
    e.por(40, G + 3, 94, "spruce_fence", **SOLTO)
    e.por(40, G + 4, 94, "spruce_fence", **SOLTO)
    # espantalho caído na roça
    e.por(63, G + 1, 78, "carved_pumpkin", facing="west")
    e.por(64, G + 1, 78, "hay_block", axis="x")
    e.por(65, G + 1, 78, "spruce_fence", **SOLTO)


def arvore(e, x, z, alt, galhos):
    base = chao_y(x, z)
    e.por(x, base, z, "coarse_dirt")
    for y in range(base + 1, base + 1 + alt):
        e.por(x, y, z, "dark_oak_log", axis="y")
    for gx, gz, gy in galhos:
        for i in (1, 2):
            px, pz = x + gx * i, z + gz * i
            if 0 <= px < TAM[0] and 0 <= pz < TAM[2]:
                e.por(px, base + gy + i - 1, pz, "dark_oak_log", axis="x" if gx else "z")
    ZONAS_QUEIMADAS.append((x - 1, z - 1, x + 1, z + 1, 0.6))


def arvores_queimadas(e):
    for (x, z, alt, galhos) in ((38, 71, 6, ((1, 0, 4), (-1, 0, 5))),
                                 (44, 83, 5, ((0, 1, 3),)),
                                 (91, 46, 6, ((-1, 0, 4), (0, 1, 5))),
                                 (60, 42, 5, ((1, 0, 4),)),
                                 (2, 48, 5, ((1, 0, 3),)),
                                 (97, 22, 6, ((-1, 0, 4),)),
                                 (63, 11, 7, ((-1, 0, 5), (0, 1, 4))),
                                 (34, 50, 6, ((0, -1, 4), (1, 0, 5))),
                                 (20, 95, 4, ((1, 0, 3),))):
        arvore(e, x, z, alt, galhos)


def detalhes(e):
    # carroça tombada e barris largados
    for x in range(38, 41):
        e.por(x, G + 1, 77, "spruce_slab", **LAJE)
    e.por(37, G + 1, 77, "spruce_trapdoor", facing="east", half="bottom", open=True, powered=False,
          waterlogged=False)
    e.por(41, G + 1, 78, "barrel", facing="east", open=False)
    e.por(58, G + 1, 48, "hay_block", axis="z")
    e.por(40, G + 1, 63, "barrel", facing="north", open=False)
    e.por(87, G + 1, 41, "barrel", facing="up", open=False)
    # pegadas de cinza na estrada: todas para dentro. Nenhuma sai.
    for z in range(72, 99, 2):
        x = 49 if (z // 2) % 2 == 0 else 50
        if e.vazio(x, G + 1, z) and e.nome(x, G, z) is not None:
            e.por(x, G + 1, z, "light_gray_carpet")


def chao_queimado(e):
    """Chão chamuscado em volta das casas que queimaram."""
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            y = chao_y(x, z)
            if e.nome(x, y, z) not in ("minecraft:grass_block", "minecraft:dirt", "minecraft:coarse_dirt"):
                continue
            for x0, z0, x1, z1, forca in ZONAS_QUEIMADAS:
                dx = max(x0 - x, 0, x - x1)
                dz = max(z0 - z, 0, z - z1)
                d = math.hypot(dx, dz)
                if d <= 4 and ruido(x, 7, z, 2.0, SEMENTE + 19) * forca > d / 5.5:
                    e.por(x, y, z, QUEIMADO_CHAO)
                    break


def desgaste(e):
    m.envelhecer(e, 33, PLAT, 3, 68, PLAT + 24, 41,
                 {"stone_bricks": Mistura(("cracked_stone_bricks", 2), ("mossy_stone_bricks", 2))},
                 intensidade=0.6)
    for z in range(19, 34, 2):
        if e.chance(0.5):
            m.trepadeira(e, 40, z, PLAT + 6, "east", 2 + int(e.rng.random() * 4))


def sangue(e):
    Y = PLAT
    # capela: da mesa até o campanário, e lá em cima ao pé do sino
    m.mancha(e, 49, Y + 1, 23, 1, 0.6)
    m.rastro(e, [(49, 23), (52, 26), (55, 29), (56, 31), (57, 31), (59, 31), (60, 32)], Y + 1, falha=0.3)
    m.mancha(e, 61, Y + 16, 32, 0, 1.0)
    # praça: as poças que "sangram" (o mod põe a partícula)
    m.mancha(e, 43, G + 1, 49, 1, 0.7)
    m.mancha(e, 56, G + 1, 62, 1, 0.7)
    m.mancha(e, 41, G + 1, 60, 0, 1.0)
    # curral: o vermelho nas valas
    for z in (12, 18, 24):
        m.rastro(e, [(10, z), (29, z)], G + 1, falha=0.55)
    # cama manchada e arrasto até a porta
    m.mancha(e, 84, G + 1, 32, 1, 0.8)
    m.rastro(e, [(84, 32), (81, 32)], G + 1, falha=0.2)


def luzes(e):
    """Blocos de luz invisíveis (nível 5) em grade, dentro do muro: monstro comum não nasce na vila."""
    if not LUZ_INVISIVEL:
        return
    for x in range(6, 94):
        for z in range(5, 88):
            if x % 4 != 2 or z % 4 != 1:
                continue
            y = chao_y(x, z) + 1
            if e.vazio(x, y, z) and not e.vazio(x, y - 1, z):
                e.por(x, y, z, "light", level=5, waterlogged=False)
    for x, y, z in ((60, PLAT + 6, 31), (61, PLAT + 12, 32), (59, PLAT + 16, 30)):
        if e.vazio(x, y, z):
            e.por(x, y, z, "light", level=5, waterlogged=False)


def vegetacao(e):
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            y = chao_y(x, z)
            n = e.nome(x, y, z)
            if (x, y + 1, z) in e.blocos:
                continue
            if n == "minecraft:grass_block":
                r = e.rng.random()
                if r < 0.16:
                    e.por(x, y + 1, z, "short_grass")
                elif r < 0.19:
                    e.por(x, y + 1, z, "fern")
                elif r < 0.21:
                    e.por(x, y + 1, z, "dead_bush")
            elif n in ("minecraft:coarse_dirt", "minecraft:soul_soil") and e.chance(0.05):
                e.por(x, y + 1, z, "dead_bush")


# ---------------------------------------------------------------------------
# Ligações de cerca, mureta, vidro e grade (como o jogo faria ao colocar)
# ---------------------------------------------------------------------------
LADOS = {"north": (0, -1), "south": (0, 1), "east": (1, 0), "west": (-1, 0)}


def _vidro(c):
    return c.endswith("glass_pane") or c == "iron_bars"


def conectar(e):
    for (x, y, z), (nome, props) in list(e.blocos.items()):
        curto = nome.split(":")[1]
        p = dict(props)
        if curto.endswith("_fence") or _vidro(curto):
            for d, (dx, dz) in LADOS.items():
                v = e.nome(x + dx, y, z + dz)
                if v is None:
                    p[d] = "false"
                    continue
                vc = v.split(":")[1]
                if curto.endswith("_fence"):
                    liga = vc.endswith("_fence") or e.cheio(x + dx, y, z + dz)
                else:
                    liga = _vidro(vc) or e.cheio(x + dx, y, z + dz)
                p[d] = "true" if liga else "false"
            e.blocos[(x, y, z)] = (nome, tuple(sorted(p.items())))
        elif curto.endswith("_wall") and curto != "red_wall_banner":
            ligados = []
            for d, (dx, dz) in LADOS.items():
                v = e.nome(x + dx, y, z + dz)
                liga = v is not None and ((v.split(":")[1].endswith("_wall") and v != "minecraft:red_wall_banner")
                                          or e.cheio(x + dx, y, z + dz))
                p[d] = "low" if liga else "none"
                if liga:
                    ligados.append(d)
            reto = sorted(ligados) in (["north", "south"], ["east", "west"])
            p["up"] = "false" if reto and e.vazio(x, y + 1, z) else "true"
            e.blocos[(x, y, z)] = (nome, tuple(sorted(p.items())))


# ---------------------------------------------------------------------------
# Pistas (onde a Lore/Dev põem livro, item, texto e evento)
# ---------------------------------------------------------------------------
def pistas(e):
    Y = PLAT
    e.pista(62, Y + 16, 31, "atril", "LIVRO DO SINEIRO: atril no alto do campanário, ao pé do sino. Abrir = o sino toca uma vez, fraco.")
    e.pista(61, Y + 16, 31, "martelo", "Martelo do ferreiro ao lado do atril (item do mod ou moldura; o schematic não guarda item).")
    e.pista(60, Y + 17, 32, "sino", "Sino rachado, torto na viga quebrada. Toca de noite (som do mod).")
    e.pista(60, Y + 17, 34, "vao", "Vão quebrado do campanário (sul do arquivo): girar a vila ao colocar para ele apontar a vila comum.")
    e.pista(49, Y + 2, 21, "vela", "A ÚNICA VELA ACESA da vila, na mesa da capela: refúgio (Lucidez volta; Famintos não entram).")
    e.pista(49, Y + 1, 22, "cadeira", "A cadeira a mais, virada para a porta.")
    e.pista(49, 0, 53, "bau", "Nicho no fundo do poço (descida pela escada): baú de mantimentos (pão, maçã).")
    e.pista(49, 1, 53, "cruz", "Cruz de Ferro riscada na pedra, em cima do nicho (bloco cinzelado; desenho nosso pendente).")
    e.pista(47, 3, 58, "placa", "Placa na beira do poço: \"Aqui ele ouvia.\" (texto posto pelo mod/admin).")
    e.pista(29, G + 2, 60, "botoes", "Casa da Moça do Terço: botões de pedra no peitoril (z 60 a 65, do mais velho ao mais novo). O vazio da ponta é z 66.")
    e.pista(74, G + 1, 47, "bigorna", "Casa do ferreiro: bigorna rachada, forja apagada. Falta o martelo (está no campanário).")
    e.pista(85, G + 1, 79, "cama", "Casa do Compadre: a única cama limpa, de lençol branco. Travesseiro marcado com {marcado} para Exposição alta.")
    e.pista(81, G + 1, 76, "mesa", "Casa do Compadre: mesa comprida posta para muita gente, pratos vazios.")
    e.pista(84, G + 1, 30, "cama", "Cama manchada (lençol vermelho), mancha e arrasto até a porta oeste.")
    e.pista(51, Y + 1, 8, "lapide", "Cemitério: a lápide com o risco da Cruz de Ferro (bloco cinzelado). Covas abertas, nenhum corpo.")
    e.pista(7, G + 2, 14, "estandarte", "Curral: estandartes vermelhos lisos (trocar pelo da Cruz de Ferro quando desenhado). Cheiro de ferro mais forte aqui.")
    e.pista(43, G + 1, 49, "poca", "Praça: poça que pulsa (partícula rala; parado nela perde Lucidez). Outras: (56, 1, 62) e (41, 1, 60).")
    e.pista(20, G + 1, 18, "poca", "Curral: valas com vermelho (poças que pulsam).")
    e.pista(40, G + 1, 94, "espantalho", "Campo queimado da entrada: espantalho de cabeça para baixo. Pegadas de cinza na estrada, todas para dentro.")
    e.pista(14, G + 1, 48, "faminto", "Lugar de Faminto parado, de frente para a parede (dia).")
    e.pista(84, G + 1, 16, "faminto", "Lugar de Faminto parado, de frente para a parede (dia).")
    e.pista(28, G + 1, 77, "faminto", "Lugar de Faminto parado, de frente para a parede (dia).")
    e.pista(73, G + 1, 30, "faminto", "Lugar de Faminto parado (casa queimada).")
    for p in e.pistas:
        x, y, z = p["pos"]
        if p["tipo"] not in ("martelo", "vao", "poca", "faminto", "cadeira") and e.vazio(x, y, z):
            print(f"   AVISO: pista sem bloco em {p['pos']} ({p['tipo']})")
