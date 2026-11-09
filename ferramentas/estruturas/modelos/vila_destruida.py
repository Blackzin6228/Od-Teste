"""Protótipo: a VILA DESTRUÍDA (Ato 1, elemento Sangue). Proposta da mesa Modelador para aprovação.

Base: lore/roteiro.md (Ato 1, "A Vila que Toca") e docs/estilo-do-genero.md (Sangue). A ficha
lore/fichas-estruturas.md ainda não existe no repositório: as partes seguem o pedido do Nikolas
(casas queimadas e desabadas, ruas, igreja, poço, lugar do livro) e o roteiro.

Tamanho 72 x 30 x 64 (x, y, z). Norte = z 0. O chão da vila é a camada y = 4 (G); abaixo dela só
o poço e o porão da Casa do Livro. Fora das construções, só a camada do chão é gravada: o resto do
terreno do mundo fica como está.

Partes:
- Igreja (norte, x 26-44, z 1-21): nave com bancos, altar, estandarte vermelho, torre com o sino.
  O meio do telhado caiu (entulho na nave) e a parede leste rachou e desabou.
- Praça (x 27-44, z 23-40): calçamento velho, o sino da vila num pórtico de madeira, árvore queimada.
- Poço (x 38-42, z 33-37): o arrasto de sangue que sai do altar termina na borda dele.
- Casa do Livro (x 46-56, z 38-47): queimada por inteiro, menos um círculo perfeito de assoalho
  intacto em volta de um atril, com velas vermelhas acesas. Porão com baú.
- Casas (10): queimadas (paredes comidas, só caibros), desabadas (meio telhado no chão) e
  só alicerce (com a chaminé em pé). A ferraria é de pedra, com correntes no fundo.
- Ruas: rua principal (leste-oeste), rua sul, caminhos para as casas. Curral e roça queimada ao sul.
O acento de cor é um só: o vermelho do Sangue (pó de redstone apagado, velas vermelhas, estandarte).
Nenhum símbolo desenhado: o estandarte é liso, esperando o símbolo nosso (Cruz de Ferro, a desenhar).
"""
import math

from estrutura import Estrutura, Mistura, ruido
import modulos as m

NOME = "vila_destruida"
TAM = (72, 30, 64)
SEMENTE = 1109
G = 4  # altura do chão da vila

CORTES = [("chao", G, G + 4)]

# partes para as prévias por pedaço: (nome, x0, z0, x1, z1)
PARTES = [
    ("igreja", 24, 0, 46, 22),
    ("praca_poco", 26, 22, 45, 41),
    ("poco", 36, 31, 44, 39),
    ("lugar_do_livro", 44, 34, 58, 49),
    ("casas_oeste", 0, 2, 23, 28),
    ("casas_nordeste", 45, 2, 71, 27),
    ("casas_sudoeste", 0, 35, 31, 63),
    ("ferraria_e_roca", 38, 35, 71, 63),
    ("rua_principal", 0, 26, 71, 37),
]

LAJE = {"type": "bottom", "waterlogged": False}
DEGRAU = {"half": "bottom", "shape": "straight", "waterlogged": False}

PEDRA_BASE = Mistura(("cobblestone", 6), ("mossy_cobblestone", 2))
PEDRA_QUEIMADA = Mistura(("cobblestone", 5), ("blackstone", 2), ("smooth_basalt", 1), ("mossy_cobblestone", 1))
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
RUA = Mistura(("dirt_path", 12), ("gravel", 2), ("coarse_dirt", 2), ("cobblestone", 1))
GRAMA = Mistura(("grass_block", 10), ("coarse_dirt", 1), ("dirt", 1))
QUEIMADO_CHAO = Mistura(("soul_soil", 3), ("coarse_dirt", 3), ("dirt", 1), ("blackstone", 1))

# ruas: retângulos (x0, z0, x1, z1)
RUAS = [
    (0, 30, 71, 33),    # rua principal (leste-oeste)
    (34, 40, 37, 63),   # rua sul
    (35, 21, 39, 23),   # adro da igreja
    (9, 14, 11, 29),    # caminho das casas do oeste
    (54, 13, 55, 29),   # caminho da casa grande do nordeste
    (48, 34, 53, 37),   # caminho da Casa do Livro
    (62, 34, 64, 36),   # caminho da ferraria
    (7, 34, 9, 36),     # caminho da casa sudoeste
    (17, 34, 19, 36),   # caminho da casa do alicerce
    (15, 47, 33, 48),   # caminho do curral e da casa do fundo
    (38, 49, 45, 50),   # caminho da roça
]
PRACA = (27, 23, 44, 40)
ZONAS_QUEIMADAS = []  # preenchida pelas casas: (x0, z0, x1, z1, força)


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    ZONAS_QUEIMADAS.clear()
    chao(e)
    ruas(e)
    igreja(e)
    praca(e)
    poco(e)
    casas(e)
    casa_do_livro(e)
    ferraria(e)
    curral_e_roca(e)
    arvores_queimadas(e)
    detalhes_de_rua(e)
    chao_queimado(e)
    desgaste(e)
    sangue(e)
    vegetacao(e)
    conectar(e)
    pistas(e)
    return e


# ---------------------------------------------------------------------------
# Chão e ruas
# ---------------------------------------------------------------------------
def chao(e):
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            n = ruido(x, 0, z, 5.0, SEMENTE + 1)
            if n < 0.22:
                e.por(x, G, z, "coarse_dirt")
            elif n < 0.3:
                e.por(x, G, z, "dirt")
            else:
                e.por(x, G, z, "grass_block", snowy=False)


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
            if em_rua(x, z):
                e.por(x, G, z, RUA)
                e.ar_onde_vazio(x, G + 1, z, x, G + 3, z)


def praca(e):
    x0, z0, x1, z1 = PRACA
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            canto = (x in (x0, x1)) + (z in (z0, z1))
            if canto and e.chance(0.45):
                continue  # borda irregular: a grama come o calçamento
            e.por(x, G, z, CALCAMENTO)
            e.ar_onde_vazio(x, G + 1, z, x, G + 3, z)
    # o sino da vila, num pórtico de madeira (toca sozinho no evento)
    for x in (29, 33):
        for y in range(G + 1, G + 5):
            e.por(x, y, 26, TRONCO)
        e.por(x, G, 26, "stone_bricks")
    for x in range(29, 34):
        e.por(x, G + 5, 26, "spruce_log", axis="x")
    for x in (29, 33):
        e.por(x, G + 6, 26, "spruce_slab", **LAJE)
    for x in (30, 32):
        e.por(x, G + 6, 26, "spruce_stairs", facing="east" if x == 30 else "west", **DEGRAU)
    e.por(31, G + 6, 26, "spruce_planks")
    e.por(31, G + 4, 26, "bell", attachment="ceiling", facing="north", powered=False)
    for x in range(30, 33):
        e.por(x, G, 26, "stone_bricks")
        e.por(x, G, 25, "stone_brick_slab", type="double", waterlogged=False)
        e.por(x, G, 27, "stone_bricks")
    # banco de pedra virado para o sino
    m.banco(e, 29, 31, G + 1, 29, "north", material="stone_brick_stairs")
    # lanterna caída no chão da praça
    e.por(42, G + 1, 25, "lantern", hanging=False, waterlogged=False)


# ---------------------------------------------------------------------------
# Igreja
# ---------------------------------------------------------------------------
def igreja(e):
    X0, X1, Z0, Z1 = 31, 43, 2, 20
    TOPO = G + 7
    BASE = Mistura(("stone_bricks", 6), ("mossy_stone_bricks", 3), ("cracked_stone_bricks", 2), ("cobblestone", 1))
    PISO_I = Mistura(("polished_andesite", 6), ("stone_bricks", 4), ("cracked_stone_bricks", 2),
                     ("mossy_stone_bricks", 1))

    def reboco(x, y, z):
        n = ruido(x, y, z, 2.2, SEMENTE + 3)
        if n < 0.30:
            return "stone_bricks"
        if n < 0.38:
            return "diorite"
        return "calcite"

    # telhado de telha escura (cumeeira norte-sul)
    alt = m.telhado_duas_aguas(e, X0 - 1, X1 + 1, Z0 - 1, Z1 + 1, TOPO + 1, "deepslate_tile_stairs",
                               "dark_oak_planks", ("deepslate_tile_slab", LAJE), buracos=0.04,
                               protegido=lambda z: z >= Z1)
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            borda = x in (X0, X1) or z in (Z0, Z1)
            e.por(x, G, z, BASE if borda else PISO_I)

    def parede(x, y, z, canto):
        pilastra = canto or (x in (X0, X1) and z in (6, 10, 14, 18))
        if y <= G + 1:
            return BASE
        if pilastra or y == TOPO:
            return BASE
        return reboco(x, y, z)

    m.paredes(e, X0, Z0, X1, Z1, G + 1, TOPO, parede)
    for z in (Z0, Z1):
        m.oitao(e, z, X0 - 1, X1 + 1, TOPO + 1, lambda x, y, zz, c: reboco(x, y, zz), de_k=0)
    # cruzeiro de pedra no alto da fachada (sem símbolo: só o bloco cinzelado)
    e.por(37, alt(37) + 1, Z1, "chiseled_stone_bricks")

    # porta: arco de 3, folha arrancada caída na escadaria
    m.vao(e, 36, G + 1, Z1, 38, G + 4, Z1)
    for x in (36, 38):
        e.por(x, G + 4, Z1, "stone_brick_stairs", facing="south", half="top", shape="straight", waterlogged=False)
    e.por(37, G + 5, Z1, "chiseled_stone_bricks")
    for x in range(35, 40):
        e.por(x, G, Z1 + 1, "stone_brick_stairs", facing="north", **DEGRAU)
    e.por(39, G + 1, 22, "dark_oak_trapdoor", facing="north", half="bottom", open=False, powered=False,
          waterlogged=False)
    e.por(39, G + 1, 23, "dark_oak_trapdoor", facing="north", half="bottom", open=False, powered=False,
          waterlogged=False)
    # óculo da fachada (vidro quebrado)
    for x, y in ((36, G + 9), (37, G + 9), (38, G + 9), (37, G + 10), (37, G + 8)):
        m.janela(e, x, Z1, y, y, "x", quebrada=0.5)

    # janelas laterais altas, estreitas
    for x in (X0, X1):
        for z in (4, 8, 12, 16):
            m.janela(e, x, z, G + 3, G + 5, "z", quebrada=0.55, grade=0.2)
    for x in (35, 39):
        m.janela(e, x, Z0, G + 3, G + 5, "x", quebrada=0.4)

    # presbitério: degraus, altar de pedra, estandarte vermelho atrás
    for x in range(X0 + 1, X1):
        for z in (3, 4, 5):
            e.por(x, G + 1, z, "stone_bricks" if z < 5 else "stone_brick_slab", **({} if z < 5 else LAJE))
        e.por(x, G + 1, 6, "stone_brick_stairs", facing="north", **DEGRAU)
    for x in (36, 37, 38):
        e.por(x, G + 2, 3, "chiseled_stone_bricks" if x == 37 else "polished_andesite")
    m.vela(e, 36, G + 3, 3, 3, acesa=True, cor="red")
    m.vela(e, 38, G + 3, 3, 2, acesa=False, cor="red")
    e.por(37, G + 5, 3, "red_wall_banner", facing="south")  # no jogo ocupa 2 de altura, pendurado
    m.vela(e, 33, G + 2, 4, 1, acesa=False)
    m.vela(e, 41, G + 2, 4, 2, acesa=False)
    e.por(41, G + 2, 3, "lectern", facing="south", has_book=False, powered=False)

    # bancos (olhando para o altar, ao norte)
    for z in range(8, 19, 2):
        m.banco(e, X0 + 1, 35, G + 1, z, "north", falha=0.12)
        m.banco(e, 39, X1 - 1, G + 1, z, "north", falha=0.12)
    e.por(34, G + 1, 17, "spruce_stairs", facing="west", **DEGRAU)  # banco derrubado

    # correntes do lustre que caiu
    m.corrente(e, 37, 18, G + 8, alt(37) - 2)
    e.por(38, G + 1, 18, "lantern", hanging=False, waterlogged=False)

    # torre do sino (canto sudoeste, colada na nave)
    torre(e)

    # O meio do telhado caiu: z 9-15 sem telha, com bordas irregulares
    for z in range(8, 17):
        for x in range(X0 - 1, X1 + 2):
            for y in range(TOPO + 1, alt(37) + 2):
                d = abs(z - 12)
                if d <= 3 or ruido(x, y, z, 1.6, SEMENTE + 9) > 0.55:
                    e.tirar(x, y, z)
    # um caibro ficou atravessado no buraco
    for x in range(X0 + 1, X1):
        y = TOPO + 1 + min(x - X0, X1 - x) // 2
        e.por(x, y, 12, "dark_oak_log", axis="x")
    # parede leste rachada e desabada (z 9-14)
    for z in range(9, 15):
        h = G + 1 + int(ruido(X1, 0, z, 1.5, SEMENTE + 4) * 3)
        for y in range(h + 1, TOPO + 1):
            e.tirar(X1, y, z)
    for x in range(X0 + 1, X1):
        for z in range(10, 15):
            e.tirar(x, G + 1, z)  # os bancos de baixo do entulho
    entulho(e, X0 + 1, 9, X1 - 1, 15, 3, SEMENTE + 5)
    entulho(e, X1, 8, X1 + 3, 16, 2, SEMENTE + 6)

    # limpar o interior da nave (ar onde o terreno do mundo estiver)
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, alt(37), Z1 - 1,
                    condicao=lambda x, y, z: y < alt(x) - 1)
    return alt


def torre(e):
    X0, X1, Z0, Z1 = 26, 30, 15, 20
    H = G + 20
    BASE = Mistura(("stone_bricks", 6), ("mossy_stone_bricks", 2), ("cracked_stone_bricks", 3))
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, "stone_bricks")

    def parede(x, y, z, canto):
        if canto:
            return "stone_bricks" if y % 4 else "chiseled_stone_bricks"
        return BASE

    # topo quebrado: cada coluna termina numa altura diferente
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        canto = x in (X0, X1) and z in (Z0, Z1)
        # sempre acima da viga do sino (G+18): o topo quebra, mas nada fica solto no ar
        topo = H + 2 - int(ruido(x, 0, z, 1.4, SEMENTE + 11) * 4)
        for y in range(G + 1, topo + 1):
            e.por(x, y, z, parede(x, y, z, canto))
    # porta para a nave e para fora
    m.vao(e, X1, G + 1, 18, X1, G + 2, 18)
    m.vao(e, X1 + 1, G + 1, 18, X1 + 1, G + 2, 18)
    m.vao(e, 28, G + 1, Z1, 28, G + 3, Z1)
    # andares de madeira (o de cima furado), escada de mão
    for y in (G + 7, G + 13):
        m.piso(e, X0 + 1, Z0 + 1, X1 - 1, Z1 - 1, y, TABUA)
    e.ar(28, G + 13, 17)
    e.ar(29, G + 13, 18)
    for y in range(G + 1, G + 14):  # passa pelos furos dos dois andares
        e.por(27, y, 19, "ladder", facing="north", waterlogged=False)
    # seteiras e vãos do campanário
    for y in (G + 4, G + 9):
        for x, z, eixo in ((28, Z0, "x"), (X0, 17, "z"), (X1, 17, "z")):
            m.janela(e, x, z, y, y + 1, eixo, quebrada=1.0)
    for x, z in ((28, Z0), (28, Z1)):
        m.vao(e, x - 1, G + 15, z, x + 1, G + 17, z)
    for x in (X0, X1):
        m.vao(e, x, G + 15, 17, x, G + 17, 18)
    # o sino, preso numa viga
    for x in range(X0 + 1, X1):
        e.por(x, G + 18, 17, "dark_oak_log", axis="x")
    e.por(28, G + 17, 17, "bell", attachment="ceiling", facing="north", powered=False)
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, H, Z1 - 1)
    entulho(e, X0 - 2, Z0 - 2, X0, Z1 - 1, 2, SEMENTE + 12)


# ---------------------------------------------------------------------------
# Poço
# ---------------------------------------------------------------------------
def poco(e):
    X0, X1, Z0, Z1 = 38, 42, 33, 37
    cx, cz = 40, 35
    # cano do poço (abaixo do chão): pedra, água funda
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            borda = x in (X0, X1) or z in (Z0, Z1)
            dentro = abs(x - cx) <= 1 and abs(z - cz) <= 1
            e.por(x, 0, z, "cobblestone")
            for y in range(1, G):
                e.por(x, y, z, ("water", {"level": 0}) if dentro else PEDRA_BASE)
            e.por(x, G, z, "air" if dentro else Mistura(("stone_bricks", 3), ("mossy_stone_bricks", 2),
                                                          ("cracked_stone_bricks", 1)))
    # baú no fundo (pista), dentro da água
    e.por(cx, 1, cz, "chest", facing="north", type="single", waterlogged=True)
    # beirada de muro, com uma falha
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if abs(x - cx) <= 1 and abs(z - cz) <= 1:
                continue
            if abs(x - cx) == 2 and abs(z - cz) == 2:
                continue  # cantos abertos
            if (x, z) == (42, 36):
                continue  # pedra da beirada caiu
            e.por(x, G + 1, z, Mistura(("cobblestone_wall", 3), ("mossy_cobblestone_wall", 2)),
                  up=True, north="none", south="none", east="none", west="none", waterlogged=False)
    e.por(43, G + 1, 37, "cobblestone_slab", **LAJE)  # a pedra caída
    # postes, viga, telhadinho (metade caiu)
    for x in (X0, X1):
        for y in range(G + 2, G + 5):
            e.por(x, y, cz, "spruce_fence", north=False, south=False, east=False, west=False, waterlogged=False)
    for x in range(X0, X1 + 1):
        e.por(x, G + 5, cz, "spruce_log", axis="x")
    for x in range(X0, X1 + 1):
        if x >= 41 and x != 42:
            continue
        e.por(x, G + 5, cz - 1, "spruce_stairs", facing="south", **DEGRAU)
        e.por(x, G + 5, cz + 1, "spruce_stairs", facing="north", **DEGRAU)
        e.por(x, G + 6, cz, "spruce_slab", **LAJE)
    e.por(41, G + 1, 38, "spruce_stairs", facing="east", **DEGRAU)  # pedaço do telhado no chão
    # corrente descendo até a água
    for y in range(G, G + 5):
        e.por(cx, y, cz, "chain", axis="y", waterlogged=False)
    m.vela(e, 38, G + 2, 34, 1, acesa=False, cor="red")
    m.vela(e, 39, G + 2, 37, 1, acesa=True, cor="red")


# ---------------------------------------------------------------------------
# Casas
# ---------------------------------------------------------------------------
def entulho(e, x0, z0, x1, z1, altura, semente):
    """Monte de entulho: mais alto no meio, irregular, com laje/degrau por cima."""
    cx, cz = (x0 + x1) / 2, (z0 + z1) / 2
    rx, rz = max(1, (x1 - x0) / 2 + 0.5), max(1, (z1 - z0) / 2 + 0.5)
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not (0 <= x < TAM[0] and 0 <= z < TAM[2]):
                continue
            d = math.sqrt(((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2)
            h = int((1.15 - d) * altura * (0.5 + ruido(x, 2, z, 1.7, semente)) + 0.4)
            y = G + 1
            while y < G + 1 + h and e.vazio(x, y, z):
                e.por(x, y, z, "gravel" if y == G + 1 and e.chance(0.2) else ENTULHO)
                y += 1
            if e.vazio(x, y, z) and e.cheio(x, y - 1, z) and e.chance(0.55):
                e.por(x, y, z, ENTULHO_TOPO)


def casa(e, x0, z0, x1, z1, porta, estado, alt=4, chamine=None, pedra=False, sem=0):
    """estado: "queimada" (paredes comidas, só caibros), "desabada" (meio telhado no chão),
    "alicerce" (só a base, a chaminé em pé). porta: lado da porta (north/south/east/west)."""
    s = SEMENTE + sem * 31
    topo = G + alt
    queimada = estado in ("queimada", "alicerce")
    tabua = TABUA_QUEIMADA if queimada else TABUA
    tronco = TRONCO_QUEIMADO if queimada else TRONCO
    if pedra:
        tabua = Mistura(("cobblestone", 5), ("stone_bricks", 3), ("cracked_stone_bricks", 2), ("mossy_cobblestone", 2))
        tronco = "stone_bricks"
    if queimada:
        ZONAS_QUEIMADAS.append((x0, z0, x1, z1, 1.0 if estado == "queimada" else 0.7))
    meio_x = (x0 + x1) // 2
    caido_leste = estado == "desabada" and sem % 2 == 0  # lado que desabou

    # piso e alicerce
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            borda = x in (x0, x1) or z in (z0, z1)
            e.por(x, G, z, (PEDRA_QUEIMADA if queimada else PEDRA_BASE) if borda
                  else (PISO_QUEIMADO if queimada else PISO))

    # telhado (antes das paredes, como na capela)
    altura = None
    if estado == "desabada":
        altura = m.telhado_duas_aguas(e, x0 - 1, x1 + 1, z0 - 1, z1 + 1, topo + 1, "dark_oak_stairs",
                                      "spruce_planks", ("dark_oak_slab", LAJE), buracos=0.06)
        for z in (z0, z1):
            m.oitao(e, z, x0 - 1, x1 + 1, topo + 1, lambda x, y, zz, c: tabua)
        # metade do telhado caiu para dentro
        for x in range(x0 - 1, x1 + 2):
            lado = x >= meio_x if caido_leste else x <= meio_x
            if not lado:
                continue
            for z in range(z0 - 1, z1 + 2):
                for y in range(topo + 1, topo + 2 + (x1 - x0) // 2 + 1):
                    if ruido(x, y, z, 1.8, s) < 0.85:
                        e.tirar(x, y, z)
    # paredes: cada coluna com sua altura
    for x, z in m.perimetro(x0, z0, x1, z1):
        canto = x in (x0, x1) and z in (z0, z1)
        n = ruido(x, 0, z, 2.2, s)
        if estado == "queimada":
            h = topo if canto else topo - int(n * 3.4)
        elif estado == "alicerce":
            h = G + 1 + int(n * 2.4) + (1 if canto else 0)
        else:
            h = topo
            longe = (x - meio_x) if caido_leste else (meio_x - x)
            if longe > 0:
                h = max(G + 1, topo - int(longe * 1.1 + n * 2))
        for y in range(G + 1, h + 1):
            if y == G + 1:
                b = PEDRA_QUEIMADA if queimada else PEDRA_BASE
            elif canto or (not pedra and y == topo and estado == "desabada"):
                b = tronco if canto else ("spruce_log", {"axis": "x" if z in (z0, z1) else "z"})
            else:
                b = tabua
            e.por(x, y, z, b)

    if estado == "queimada":
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

    # porta e janelas
    dx, dz = {"north": (0, -1), "south": (0, 1), "east": (1, 0), "west": (-1, 0)}[porta]
    if porta in ("north", "south"):
        px, pz = (x0 + x1) // 2, (z0 if porta == "north" else z1)
    else:
        px, pz = (x0 if porta == "west" else x1), (z0 + z1) // 2
    e.ar(px, G + 1, pz)
    e.ar(px, G + 2, pz)
    if estado == "desabada" and not pedra:
        m.porta(e, px, G + 1, pz, porta, hinge="left", aberta=True)
    else:
        # a porta caiu para fora
        fx, fz = px + dx, pz + dz
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
                if not e.vazio(x, y, z):
                    if queimada or e.chance(0.5):
                        e.ar(x, y, z)
                    else:
                        e.por(x, y, z, "glass_pane" if e.chance(0.6) else "iron_bars", north=False, south=False,
                              east=False, west=False, waterlogged=False)

    # chaminé de tijolo (fica em pé mesmo quando a casa queimou)
    if chamine:
        cx, cz = chamine
        for y in range(G + 1, topo + 4):
            e.por(cx, y, cz, "bricks" if e.chance(0.85) else "cracked_stone_bricks")
        e.por(cx, topo + 4, cz, "bricks")
        ix = cx + (1 if cx == x0 else -1 if cx == x1 else 0)
        iz = cz + (1 if cz == z0 else -1 if cz == z1 else 0)
        e.por(ix, G + 1, iz, "campfire", facing="north", lit=False, signal_fire=False, waterlogged=False)

    # limpar o interior
    e.ar_onde_vazio(x0 + 1, G + 1, z0 + 1, x1 - 1, topo + (3 if altura else 2), z1 - 1,
                    condicao=(lambda x, y, z: y < altura(x) - 1) if altura else None)

    # entulho do lado que caiu
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
            m.cama(e, x, y, z, extra.get("facing", "north"), extra.get("cor", "white"))
        elif tipo == "mesa":
            m.mesa(e, x, z, x + extra.get("w", 1), z + extra.get("d", 0), y)
        elif tipo == "cadeira":
            m.cadeira(e, x, y, z, extra.get("olha", "north"))
        elif tipo in ("barrel",):
            e.por(x, y, z, "barrel", facing=extra.get("facing", "up"), open=False)
        elif tipo == "chest":
            e.por(x, y, z, "chest", facing=extra.get("facing", "south"), type="single", waterlogged=False)
        elif tipo in ("furnace", "smoker"):
            e.por(x, y, z, tipo, facing=extra.get("facing", "south"), lit=False)
        elif tipo == "brasa":
            e.por(x, y, z, "campfire", facing="north", lit=False, signal_fire=False, waterlogged=False)
        elif tipo == "cinza":
            e.por(x, y, z, CINZA)
        else:
            e.por(x, y, z, tipo)


def cinzas(e, c, chance):
    for x in range(c["x0"] + 1, c["x1"]):
        for z in range(c["z0"] + 1, c["z1"]):
            if e.vazio(x, G + 1, z) and e.cheio(x, G, z) and ruido(x, 5, z, 2.0, SEMENTE + 13) > 1 - chance:
                e.por(x, G + 1, z, CINZA)


def casas(e):
    # oeste
    # (o lado leste desta casa desabou: a mobília ficou no lado oeste)
    h1 = casa(e, 2, 3, 13, 12, "south", "desabada", alt=5, chamine=(2, 7), sem=2)
    mobiliar(e, h1, [(0, 0, "cama", {"facing": "south"}), (2, 0, "cama", {"facing": "south", "cor": "light_gray"}),
                     (2, 3, "mesa", {"w": 0, "d": 1}), (1, 4, "cadeira", {"olha": "east"}),
                     (0, 6, "chest", {"facing": "east"}), (1, 7, "crafting_table", {}),
                     (3, 0, "barrel", {}), (3, 7, "barrel", {"facing": "north"})])
    m.teias(e, 3, G + 3, 4, 12, G + 5, 11, 10)

    h3 = casa(e, 1, 16, 7, 26, "east", "desabada", alt=4, sem=3)
    # a casa do aldeão que sumiu: cama manchada e rastro até a porta (o lado oeste desabou)
    mobiliar(e, h3, [(3, 1, "cama", {"facing": "north"}), (4, 7, "chest", {"facing": "west"}),
                     (4, 6, "barrel", {}), (2, 8, "furnace", {"facing": "north"}), (3, 6, "mesa", {"w": 0, "d": 0}),
                     (3, 7, "cadeira", {"olha": "north"})])
    m.teias(e, 2, G + 2, 17, 6, G + 4, 25, 6)

    h2 = casa(e, 13, 16, 21, 26, "south", "queimada", alt=4, chamine=(21, 21), sem=1)
    mobiliar(e, h2, [(0, 0, "brasa", {}), (2, 4, "brasa", {}), (6, 1, "barrel", {}), (0, 8, "furnace", {}),
                     (4, 6, "coal_block", {})])
    cinzas(e, h2, 0.55)

    # nordeste
    h4 = casa(e, 49, 2, 60, 11, "south", "desabada", alt=5, chamine=(60, 6), sem=4)
    mobiliar(e, h4, [(0, 0, "cama", {"facing": "south"}), (0, 3, "bookshelf", {}), (0, 4, "bookshelf", {}),
                     (2, 3, "mesa", {"w": 1, "d": 0}), (2, 4, "cadeira", {"olha": "north"}),
                     (3, 4, "cadeira", {"olha": "north"}), (3, 0, "chest", {"facing": "south"}),
                     (1, 7, "barrel", {}), (2, 7, "barrel", {"facing": "north"}), (3, 7, "smoker", {"facing": "north"})])
    m.teias(e, 50, G + 3, 3, 59, G + 5, 10, 12)

    h5 = casa(e, 58, 16, 68, 25, "south", "queimada", alt=4, sem=5)
    mobiliar(e, h5, [(1, 1, "brasa", {}), (7, 2, "coal_block", {}), (3, 6, "barrel", {}), (8, 6, "brasa", {}),
                     (5, 3, "cauldron", {})])
    cinzas(e, h5, 0.5)

    h6 = casa(e, 46, 15, 52, 24, "south", "alicerce", alt=4, chamine=(46, 19), sem=6)
    cinzas(e, h6, 0.6)

    # sudoeste
    h7 = casa(e, 3, 37, 13, 46, "north", "queimada", alt=5, chamine=(13, 42), sem=7)
    mobiliar(e, h7, [(0, 7, "brasa", {}), (1, 7, "barrel", {}), (4, 4, "coal_block", {}), (7, 1, "brasa", {}),
                     (0, 0, "cama", {"facing": "south", "cor": "light_gray"})])
    cinzas(e, h7, 0.55)

    h8 = casa(e, 15, 37, 23, 44, "north", "alicerce", alt=4, chamine=(23, 40), sem=8)
    cinzas(e, h8, 0.6)

    h9 = casa(e, 5, 50, 14, 58, "east", "desabada", alt=4, chamine=(5, 54), sem=9)
    mobiliar(e, h9, [(5, 0, "cama", {"facing": "south"}), (6, 0, "barrel", {}), (7, 0, "barrel", {}),
                     (5, 4, "mesa", {"w": 1, "d": 0}), (5, 5, "cadeira", {"olha": "north"}),
                     (7, 6, "chest", {"facing": "west"})])
    m.teias(e, 6, G + 2, 51, 13, G + 4, 57, 8)

    h10 = casa(e, 39, 52, 44, 58, "west", "queimada", alt=4, sem=10)
    cinzas(e, h10, 0.6)
    mobiliar(e, h10, [(1, 1, "brasa", {}), (3, 4, "barrel", {})])


def casa_do_livro(e):
    """Queimada por inteiro, menos um círculo perfeito de assoalho em volta do atril."""
    c = casa(e, 46, 38, 56, 47, "north", "queimada", alt=5, chamine=(56, 43), sem=11)
    cx, cz, r = 51, 42, 3.3
    for x in range(47, 56):
        for z in range(39, 47):
            d = math.hypot(x - cx, z - cz)
            if d <= r:
                e.por(x, G, z, "spruce_planks")  # o fogo não passou daqui
                e.ar(x, G + 1, z)
            elif d <= r + 1.2:
                e.por(x, G, z, "coal_block" if e.chance(0.5) else "dark_oak_planks")  # borda bem marcada
    cinzas(e, c, 0.7)
    for x in range(47, 56):
        for z in range(39, 47):
            if math.hypot(x - cx, z - cz) <= r:
                e.ar(x, G + 1, z)
    # o atril no meio, virado para a porta
    e.por(cx, G + 1, cz, "lectern", facing="north", has_book=False, powered=False)
    # anel de velas vermelhas acesas
    for i in range(8):
        a = i * math.pi / 4
        x, z = cx + round(3 * math.cos(a)), cz + round(3 * math.sin(a))
        m.vela(e, x, G + 1, z, 1 + i % 3, acesa=True, cor="red")
    # porão: alçapão no canto, escada, baú
    for x in range(48, 57):
        for z in range(41, 48):
            borda = x in (48, 56) or z in (41, 47)
            e.por(x, 0, z, "cobblestone")
            for y in range(1, G):
                if borda:
                    e.por(x, y, z, PEDRA_BASE)
                else:
                    e.ar(x, y, z)
    e.por(55, G, 46, "spruce_trapdoor", facing="north", half="top", open=True, powered=False, waterlogged=False)
    for y in range(1, G):
        e.por(55, y, 46, "ladder", facing="north", waterlogged=False)
    e.por(49, 1, 42, "chest", facing="south", type="single", waterlogged=False)
    e.por(50, 1, 42, "barrel", facing="up", open=False)
    e.por(49, 1, 43, "barrel", facing="east", open=False)
    m.vela(e, 52, 1, 42, 1, acesa=False, cor="red")
    e.por(52, 3, 44, "lantern", hanging=True, waterlogged=False)


def ferraria(e):
    c = casa(e, 59, 37, 69, 46, "north", "desabada", alt=5, pedra=True, sem=14)  # o lado leste caiu
    # forja: fornalhas e caldeirão na parede oeste
    for z in (39, 40):
        e.por(60, G + 1, z, "furnace", facing="east", lit=False)
    e.por(60, G + 1, 41, "cauldron")
    e.por(60, G + 2, 39, "bricks")
    e.por(60, G + 2, 40, "bricks")
    e.por(60, G + 1, 42, "chest", facing="east", type="single", waterlogged=False)
    e.por(61, G + 1, 45, "barrel", facing="up", open=False)
    # no fundo: viga com correntes (onde se pendurava carne)
    for x in range(60, 64):
        e.por(x, G + 4, 44, "dark_oak_log", axis="x")
    for x, comp in ((61, 2), (62, 1), (63, 2)):
        for y in range(G + 4 - comp, G + 4):
            e.por(x, y, 44, "chain", axis="y", waterlogged=False)
    e.por(62, G + 2, 44, "iron_bars", north=False, south=False, east=False, west=False, waterlogged=False)
    m.teias(e, 60, G + 3, 38, 64, G + 4, 45, 6)
    return c


def curral_e_roca(e):
    # curral: cerca quebrada, feno, sem bicho nenhum
    X0, X1, Z0, Z1 = 18, 30, 51, 61
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        if (x, z) in ((24, Z0), (25, Z0)):
            continue  # porteira aberta
        if ruido(x, 3, z, 1.5, SEMENTE + 15) > 0.78:
            continue  # tábuas arrancadas
        e.por(x, G + 1, z, "spruce_fence", north=False, south=False, east=False, west=False, waterlogged=False)
    for x in range(X0 + 1, X1):
        for z in range(Z0 + 1, Z1):
            e.por(x, G, z, Mistura(("coarse_dirt", 4), ("dirt", 2), ("grass_block", 1)))
    for x, z in ((20, 59), (21, 59), (20, 58), (28, 53)):
        e.por(x, G + 1, z, "hay_block", axis="x")
    e.por(29, G + 1, 60, "composter", level=2)
    e.por(19, G + 1, 52, "cauldron")
    # roça queimada: trigo morto, terra arada, mancha de queimado
    for x in range(46, 69):
        for z in range(52, 62):
            if 39 <= x <= 45 and z <= 59:
                continue
            n = ruido(x, 4, z, 3.0, SEMENTE + 16)
            if z % 4 == 0:
                e.por(x, G, z, "coarse_dirt")
                continue
            if n < 0.45:
                e.por(x, G, z, QUEIMADO_CHAO)
                if e.chance(0.12):
                    e.por(x, G + 1, z, "dead_bush")
            else:
                e.por(x, G, z, "farmland", moisture=0)
                if e.chance(0.7):
                    e.por(x, G + 1, z, "wheat", age=int(e.rng.random() * 8))
    for x in range(46, 69):
        if ruido(x, 6, 63, 1.5, SEMENTE + 17) < 0.7:
            e.por(x, G + 1, 63, "spruce_fence", north=False, south=False, east=False, west=False, waterlogged=False)


def arvores_queimadas(e):
    for (x, z, alt, galhos) in ((30, 37, 6, ((1, 0, 4), (-1, 0, 5), (0, 1, 3))),
                                 (23, 6, 7, ((1, 0, 5), (0, -1, 4))),
                                 (66, 29, 5, ((-1, 0, 4),)),
                                 (45, 27, 6, ((0, 1, 4), (1, 0, 5))),
                                 (2, 62, 5, ((1, 0, 3),)),
                                 (70, 12, 6, ((-1, 0, 4), (0, 1, 5)))):
        e.por(x, G, z, "coarse_dirt")
        for y in range(G + 1, G + 1 + alt):
            e.por(x, y, z, "dark_oak_log", axis="y")
        for gx, gz, gy in galhos:
            for i in (1, 2):
                px, pz = x + gx * i, z + gz * i
                if 0 <= px < TAM[0] and 0 <= pz < TAM[2]:
                    e.por(px, G + gy + i - 1, pz, "dark_oak_log", axis="x" if gx else "z")
        ZONAS_QUEIMADAS.append((x - 1, z - 1, x + 1, z + 1, 0.6))


def detalhes_de_rua(e):
    # postes de lanterna ao longo da rua principal (alguns caídos)
    for i, x in enumerate((4, 16, 26, 47, 58, 69)):
        z = 29 if i % 2 == 0 else 34
        if e.nome(x, G + 1, z) not in (None, "minecraft:air"):
            continue
        quebrado = i in (1, 4)
        altura = 1 if quebrado else 3
        for y in range(G + 1, G + 1 + altura):
            e.por(x, y, z, "spruce_fence", north=False, south=False, east=False, west=False, waterlogged=False)
        if quebrado:
            e.por(x + 1, G + 1, z, "lantern", hanging=False, waterlogged=False)
            e.por(x - 1, G + 1, z, "spruce_fence", north=False, south=False, east=False, west=False,
                  waterlogged=False)
        else:
            e.por(x, G + 4, z, "lantern", hanging=False, waterlogged=False)
    # carroça tombada e barris largados
    for x in range(22, 25):
        e.por(x, G + 1, 35, "spruce_slab", **LAJE)
    e.por(21, G + 1, 35, "spruce_trapdoor", facing="east", half="bottom", open=True, powered=False,
          waterlogged=False)
    e.por(25, G + 1, 36, "barrel", facing="east", open=False)
    e.por(64, G + 1, 29, "barrel", facing="north", open=False)
    e.por(45, G + 1, 29, "hay_block", axis="z")
    e.por(46, G + 1, 34, "barrel", facing="up", open=False)
    # cercas quebradas na entrada leste e oeste
    for z in (28, 35):
        for x in (0, 1, 70, 71):
            if e.chance(0.6):
                e.por(x, G + 1, z, "spruce_fence", north=False, south=False, east=False, west=False,
                      waterlogged=False)


def chao_queimado(e):
    """Chão chamuscado em volta das casas que queimaram."""
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            n = e.nome(x, G, z)
            if n not in ("minecraft:grass_block", "minecraft:dirt", "minecraft:coarse_dirt"):
                continue
            for x0, z0, x1, z1, forca in ZONAS_QUEIMADAS:
                dx = max(x0 - x, 0, x - x1)
                dz = max(z0 - z, 0, z - z1)
                d = math.hypot(dx, dz)
                if d <= 4 and ruido(x, 7, z, 2.0, SEMENTE + 19) * forca > d / 5.5:
                    e.por(x, G, z, QUEIMADO_CHAO)
                    break


def desgaste(e):
    m.envelhecer(e, 24, G, 0, 46, G + 26, 22,
                 {"stone_bricks": Mistura(("cracked_stone_bricks", 2), ("mossy_stone_bricks", 2)),
                  "calcite": Mistura(("diorite", 1), ("stone_bricks", 1), ("mossy_stone_bricks", 1))},
                 intensidade=0.7)
    m.chao_com_musgo(e, 32, 7, 42, 19, G + 1, 0.25)
    m.teias(e, 32, G + 5, 3, 42, G + 7, 19, 18)
    m.teias(e, 27, G + 8, 16, 29, G + 18, 19, 10)
    # trepadeiras na igreja e na torre
    for z in range(3, 20, 2):
        if z < 15 and e.chance(0.6):
            m.trepadeira(e, 30, z, G + 7, "east", 2 + int(e.rng.random() * 5))
        if e.chance(0.5):
            m.trepadeira(e, 44, z, G + 7, "west", 2 + int(e.rng.random() * 5))
    for x in (26, 27, 29, 30):
        if e.chance(0.7):
            m.trepadeira(e, x, 21, G + 12, "north", 3 + int(e.rng.random() * 7))


def sangue(e):
    # 1) altar: poça no altar e no degrau, arrasto pelo corredor até a porta, pela praça até o poço
    m.mancha(e, 37, G + 3, 3, 0, 1.0)
    m.mancha(e, 37, G + 2, 5, 2, 0.8)
    m.rastro(e, [(37, 7), (37, 16), (37, 19), (37, 20)], G + 1, falha=0.25)
    m.rastro(e, [(37, 21), (37, 24), (38, 28), (39, 31), (40, 32)], G + 1, falha=0.3)
    m.mancha(e, 40, G + 1, 32, 1, 0.8)
    m.mancha(e, 43, G + 1, 35, 1, 0.6)
    # 2) do poço até a Casa do Livro (pingos, não arrasto)
    m.rastro(e, [(43, 37), (46, 37), (49, 37), (51, 38), (51, 39)], G + 1, falha=0.5)
    m.rastro(e, [(51, 39)], G + 1, falha=0.0)
    # 3) casa do aldeão que sumiu: em volta da cama e até a porta
    m.mancha(e, 5, G + 1, 19, 1, 0.8)
    m.rastro(e, [(5, 20), (6, 21), (7, 21), (8, 21)], G + 1, falha=0.2)
    # 4) curral vazio: mancha grande e arrasto saindo pela porteira
    m.mancha(e, 24, G + 1, 56, 2, 0.6)
    m.rastro(e, [(24, 56), (24, 53), (24, 51), (25, 49)], G + 1, falha=0.35)
    # 5) ferraria: embaixo das correntes
    m.mancha(e, 62, G + 1, 44, 1, 0.8)
    m.mancha(e, 61, G + 1, 43, 0, 1.0)
    # 6) porão da Casa do Livro
    m.mancha(e, 51, 1, 44, 1, 0.7)
    # 7) pingos soltos na rua principal (só onde não é caminho de terra)
    for x in range(2, 70, 5):
        if e.chance(0.4):
            m.mancha(e, x, G + 1, 31 + int(e.rng.random() * 3), 1, 0.35)


def vegetacao(e):
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            n = e.nome(x, G, z)
            if not e.vazio(x, G + 1, z) or (x, G + 1, z) in e.blocos:
                continue
            if n == "minecraft:grass_block":
                r = e.rng.random()
                if r < 0.16:
                    e.por(x, G + 1, z, "short_grass")
                elif r < 0.19:
                    e.por(x, G + 1, z, "fern")
                elif r < 0.21:
                    e.por(x, G + 1, z, "dead_bush")
            elif n in ("minecraft:coarse_dirt", "minecraft:soul_soil") and e.chance(0.05):
                e.por(x, G + 1, z, "dead_bush")


# ---------------------------------------------------------------------------
# Ligações de cerca, muro, vidro e grade (como o jogo faria ao colocar)
# ---------------------------------------------------------------------------
LADOS = {"north": (0, -1), "south": (0, 1), "east": (1, 0), "west": (-1, 0)}


def conectar(e):
    for (x, y, z), (nome, props) in list(e.blocos.items()):
        curto = nome.split(":")[1]
        p = dict(props)
        if curto.endswith("_fence") or curto in ("glass_pane", "iron_bars"):
            for d, (dx, dz) in LADOS.items():
                v = e.nome(x + dx, y, z + dz)
                if v is None:
                    p[d] = "false"
                    continue
                vc = v.split(":")[1]
                if curto.endswith("_fence"):
                    liga = vc.endswith("_fence") or e.cheio(x + dx, y, z + dz)
                else:
                    liga = vc in ("glass_pane", "iron_bars") or e.cheio(x + dx, y, z + dz)
                p[d] = "true" if liga else "false"
            e.blocos[(x, y, z)] = (nome, tuple(sorted(p.items())))
        elif curto.endswith("_wall") and curto != "red_wall_banner":
            ligados = []
            for d, (dx, dz) in LADOS.items():
                v = e.nome(x + dx, y, z + dz)
                liga = v is not None and (v.split(":")[1].endswith("_wall") and v != "minecraft:red_wall_banner"
                                          or e.cheio(x + dx, y, z + dz))
                p[d] = "low" if liga else "none"
                if liga:
                    ligados.append(d)
            reto = sorted(ligados) in (["north", "south"], ["east", "west"])
            p["up"] = "false" if reto and e.vazio(x, y + 1, z) else "true"
            e.blocos[(x, y, z)] = (nome, tuple(sorted(p.items())))


def pistas(e):
    e.pista(51, G + 1, 42, "atril", "LUGAR DO LIVRO: atril no meio do círculo que não queimou. Livro principal (Lore/Dev).")
    e.pista(49, 1, 42, "bau", "Porão da Casa do Livro (alçapão em x 55, z 46): baú para página/recado.")
    e.pista(40, 1, 35, "bau", "Fundo do poço, debaixo d'água: baú (pista para quem desce pela corrente).")
    e.pista(31, G + 4, 26, "sino", "Sino da vila no pórtico da praça: toca sozinho (Ato 1, cena 1 e enigma 1).")
    e.pista(28, G + 17, 17, "sino", "Sino da torre da igreja: pode tocar ao contrário (Ato 5).")
    e.pista(37, G + 5, 3, "estandarte", "Estandarte vermelho liso atrás do altar: trocar pelo símbolo nosso (Cruz de Ferro) quando desenhado.")
    e.pista(37, G + 2, 3, "altar", "Altar com poça; o arrasto vai pelo corredor, pela praça, até o poço.")
    e.pista(41, G + 2, 3, "atril", "Atril do presbitério (vazio): segundo lugar possível para um livro.")
    e.pista(5, G + 1, 18, "cama", "Casa do aldeão que sumiu: cama com mancha e rastro até a porta.")
    e.pista(60, G + 1, 42, "bau", "Ferraria: baú ao lado da forja (o baú trancado do enigma 1, se a Lore quiser).")
    e.pista(62, G + 2, 44, "correntes", "Fundo da ferraria: viga com correntes e mancha embaixo.")
    e.pista(24, G + 1, 56, "curral", "Curral vazio: mancha e arrasto saindo pela porteira.")
    e.pista(3, G + 1, 10, "bau", "Casa grande do noroeste: baú no quarto (diário).")
    e.pista(53, G + 1, 3, "bau", "Casa grande do nordeste: baú (recado).")
    e.pista(13, G + 1, 57, "bau", "Casa desabada do sudoeste: baú.")
    # confere que cada pista aponta para um bloco que existe
    for p in e.pistas:
        x, y, z = p["pos"]
        if p["tipo"] != "curral" and e.vazio(x, y, z):
            print(f"   AVISO: pista sem bloco em {p['pos']} ({p['tipo']})")
