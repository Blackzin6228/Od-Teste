"""3. Casa do Cartógrafo. Ficha: lore/fichas-estruturas.md, seção 3. Tamanho 26 x 22 x 30.

Decisões de 14/11: as marcas do mapa-mural mudam de lugar toda manhã; casa isolada (sem vila perto).
Sul = frente. Casa de dois andares (x 2-15, z 8-17) e a torre-mirante redonda colada no canto nordeste.
- Salão (térreo): o mapa-mural na parede norte (mosaico de terracota: papel, rio, mata, estrada), mesa de
  cartografia com o mapa pela metade, estantes, mapas pequenos emoldurados, baú com cadeado de letras (Terço da Moça).
  Na frente do mural, uma fileira de marcadores `mapa:` = os pontos onde a marca pode aparecer.
- Quarto (andar de cima): cama arrumada, vela apagada, tinteiro e penas.
- Escada em caracol de pedra (dentro da torre), janelas estreitas: onde o Pendurado aparece.
- Mirante (topo da torre): luneta na janela, círculo de giz pela metade no chão, uma lanterna acesa (de longe,
  é a única janela com luz).
"""
import math

from estrutura import Estrutura, Mistura, ruido
import modulos as m
import lugares as L

NOME = "casa_cartografo"
TITULO = "3. Casa do Cartógrafo"
TAM = (26, 30, 22)
SEMENTE = 1403
G = 0
TX, TZ, TR = 19.5, 6.5, 4.0  # torre: centro e raio
CORTES = [("salao", G, G + 4), ("quarto", G + 5, G + 9), ("escada", G + 10, G + 17), ("mirante", G + 18, G + 23)]
FRENTE_ISO = 0
FRENTE_GRAUS = 20
LONGE = {"dist": 52, "altura_olho": 2.0}
RESUMO = """
Casa isolada de papel e silêncio. Entra-se pela porta da frente (sem tranca) ou pela janela grande do salão. O mapa-mural
(parede norte do salão, x 4-12, y 1-4, z 8) tem **12 pontos possíveis** (`mapa:p01` a `mapa:p12`): o mod troca o bloco
alvo por `red_terracotta` (a marca) e devolve o original na manhã seguinte. `mapa:vila_calada` é a marca fixa.
A torre tem escada em caracol até o mirante (luneta, círculo de giz). Sem spawn dentro. Chão em `y_chao = 0`.
"""

PEDRA = Mistura(("stone_bricks", 5), ("cracked_stone_bricks", 3), ("cobblestone", 1), ("mossy_stone_bricks", 1))
TORRE = Mistura(("stone_bricks", 5), ("cracked_stone_bricks", 3), ("polished_andesite", 1), ("andesite", 1),
                ("mossy_stone_bricks", 1))
TABUA = L.TABUA_ESCURA
LOG_Y = ("stripped_dark_oak_log", {"axis": "y"})
PAPEL = Mistura(("white_terracotta", 6), ("light_gray_terracotta", 1))


def _na_torre(x, z, r=TR):
    return math.hypot(x + 0.5 - TX, z + 0.5 - TZ) <= r


def _borda_torre(x, z):
    return _na_torre(x, z) and not _na_torre(x, z, TR - 1.0)


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    e.y_chao = G
    casa(e)
    torre(e)
    exterior(e)
    marcadores(e)
    L.sem_spawn(e, 3, G + 1, 9, 14, G + 1, 16, passo=4)
    L.sem_spawn(e, 3, G + 6, 9, 14, G + 6, 16, passo=4)
    L.sem_spawn(e, 16, G + 1, 3, 23, G + 26, 10, passo=3)
    return e


# ---------------------------------------------------------------------------
def casa(e):
    X0, Z0, X1, Z1 = 2, 8, 15, 17
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, PEDRA if x in (X0, X1) or z in (Z0, Z1) else
                  Mistura(("spruce_planks", 5), ("dark_oak_planks", 4)))
    # térreo de pedra, andar de cima de madeira em balanço (1 bloco para o sul e para o oeste)
    m.paredes(e, X0, Z0, X1, Z1, G + 1, G + 5,
              lambda x, y, z, c: "stone_bricks" if c else (PEDRA if y < G + 5 else
                                                          ("stripped_dark_oak_log", {"axis": "x" if z in (Z0, Z1)
                                                                                     else "z"})))
    A0, B0, A1, B1 = X0 - 1, Z0, X1, Z1 + 1
    for x in range(A0, A1 + 1):
        for z in range(B0, B1 + 1):
            if x == A0 and z == B1:
                e.por(x, G + 5, z, "dark_oak_planks")
            elif x == A0 or z == B1:
                e.por(x, G + 5, z, "dark_oak_stairs", facing="east" if x == A0 else "north", half="top",
                      shape="straight", waterlogged=False)

    def parede2(x, y, z, canto):
        if canto or (z == B1 and x in (5, 9, 12)) or (x == A0 and z in (11, 14)):
            return LOG_Y
        if y == G + 9:
            return ("stripped_dark_oak_log", {"axis": "x" if z in (B0, B1) else "z"})
        return Mistura(("dark_oak_planks", 5), ("spruce_planks", 2))

    m.paredes(e, A0, B0, A1, B1, G + 6, G + 9, parede2)
    for x in range(A0 + 1, A1):
        for z in range(B0 + 1, B1):
            e.por(x, G + 5, z, TABUA)
    alt = L.telhado(e, B0 - 1, B1 + 1, A0 - 1, A1 + 1, G + 10, "x", "deepslate_tile_stairs", "dark_oak_planks",
                    ("deepslate_tile_slab", L.LAJE), inc=2, buracos=0.02)
    for x in (A0, A1):
        L.empena(e, "x", x, B0 - 1, B1 + 1, G + 10, 2, lambda a, y: LOG_Y if a == 13 else TABUA)
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 4, Z1 - 1)
    e.ar_onde_vazio(A0 + 1, G + 6, B0 + 1, A1 - 1, G + 21, B1 - 1, condicao=lambda x, y, z: y < alt(z) - 1)
    # porta, janela grande do salão (quebrada: entrada), janelas
    m.vao(e, 9, G + 1, Z1, 9, G + 2, Z1)
    m.porta(e, 9, G + 1, Z1, "north", hinge="left", aberta=False)
    for x in (4, 5, 6):
        m.janela(e, x, Z1, G + 2, G + 3, "x", quebrada=0.8)
    e.ar(5, G + 2, Z1)
    e.ar(5, G + 3, Z1)
    for x in (12, 13):
        m.janela(e, x, Z1, G + 2, G + 3, "x", quebrada=0.3)
    m.janela(e, X0, 12, G + 2, G + 3, "z", quebrada=0.3)
    m.janela(e, X0, 14, G + 2, G + 3, "z", quebrada=0.3)
    for x in (4, 8, 11):
        m.janela(e, x, B1, G + 7, G + 8, "x", quebrada=0.3)
    m.janela(e, A0, 13, G + 7, G + 8, "z", quebrada=0.3)
    # águas-furtadas (janela na água do telhado, ao sul) para a casa não ficar lisa
    for x in (6, 11):
        for y in (G + 10, G + 11):
            e.ar(x, y, B1 + 1)
        e.por(x, G + 10, B1, "glass_pane", east=True, west=True, north=False, south=False, waterlogged=False)
    # passagem para a torre (parede leste, norte)
    m.vao(e, X1, G + 1, 9, X1, G + 2, 9)

    # ---- salão ----
    mural(e)
    e.por(7, G + 1, 12, "cartography_table")
    e.por(8, G + 1, 12, "dark_oak_planks")
    e.por(8, G + 2, 12, "white_carpet")  # o mapa pela metade, aberto na mesa
    m.cadeira(e, 7, G + 1, 13, "north", "dark_oak_stairs")
    for z in (11, 12, 13, 14, 15):
        for y in (G + 1, G + 2, G + 3):
            if z == 13 and y == G + 3:
                continue
            e.por(X0 + 1, y, z, "bookshelf")
    # mapas pequenos emoldurados (parede leste): quadro de papel com moldura de tronco
    for z0 in (11, 14):
        for dz in (0, 1):
            for y in (G + 2, G + 3):
                e.por(X1 - 1, y, z0 + dz, PAPEL)
    L.bau(e, 13, G + 1, 12, "west", "cartografo_terco",
          "Baú do salão: abre com a senha do Atlas (a mesma do vídeo). Guarda o Terço da Moça (gancho do Ato 2).")
    e.por(13, G + 1, 14, "barrel", facing="up", open=False)
    e.por(13, G + 1, 15, "barrel", facing="west", open=False)
    m.lanterna_pendurada(e, 8, G + 4, 14, com_lanterna=False, comprimento_corrente=0)
    # papéis no chão (tapete branco) e um tinteiro derrubado
    for x, z in ((6, 14), (9, 15), (10, 13), (5, 11)):
        e.por(x, G + 1, z, "white_carpet")
    # escada para o andar de cima (encostada na parede sul, sobe para o oeste)
    for i, x in enumerate(range(14, 9, -1)):
        e.por(x, G + 1 + i, 16, "dark_oak_stairs", facing="west", **L.DEGRAU)
        for y in range(G + 1, G + 1 + i):
            e.por(x, y, 16, "dark_oak_planks")
    for x in range(10, 15):
        e.ar(x, G + 5, 16)

    # ---- quarto (andar de cima) ----
    m.cama(e, 4, G + 6, 10, "north", "light_gray")
    e.por(3, G + 6, 10, "barrel", facing="up", open=False)
    m.vela(e, 3, G + 7, 10, 1, acesa=False)
    m.mesa(e, 7, 9, 8, 9, G + 6, "dark_oak_trapdoor")
    m.cadeira(e, 7, G + 6, 10, "north", "dark_oak_stairs")
    e.por(8, G + 7, 9, "flower_pot")  # tinteiro
    m.vela(e, 7, G + 7, 9, 2, acesa=False)
    e.por(13, G + 6, 9, "bookshelf")
    e.por(12, G + 6, 9, "bookshelf")
    e.por(13, G + 6, 10, "chest", facing="west", type="single", waterlogged=False)
    for x, z in ((5, 13), (6, 13), (5, 14)):
        e.por(x, G + 6, z, "brown_carpet")
    m.teias(e, 3, G + 3, 9, 14, G + 4, 16, 10)
    m.teias(e, 2, G + 8, 9, 14, G + 13, 17, 12)


def mural(e):
    """Mosaico de terracota encostado por dentro da parede norte (z = 9), x 4-12, y 1-4: papel, rio, mata, estrada."""
    for x in range(4, 13):
        for y in range(G + 1, G + 5):
            n = ruido(x, y, 0, 2.2, SEMENTE + 5)
            rio = abs((y - G) - (2.5 + 1.2 * math.sin(x / 1.7))) < 0.55
            if rio:
                b = "light_gray_terracotta"
            elif n > 0.66:
                b = "brown_terracotta"
            elif n < 0.22:
                b = "terracotta"
            else:
                b = "white_terracotta"
            e.por(x, y, 9, b)
    for y in range(G + 1, G + 5):
        e.por(3, y, 9, "stripped_dark_oak_log", axis="y")
        e.por(13, y, 9, "stripped_dark_oak_log", axis="y")
    e.por(10, G + 3, 9, "red_terracotta")  # marca fixa: a Vila Calada


PONTOS_MAPA = [(4, 1), (5, 3), (6, 2), (6, 4), (7, 1), (8, 3), (9, 2), (9, 4), (11, 1), (11, 3), (12, 2), (12, 4)]


def torre(e):
    topo_parede = G + 21
    for x in range(14, 25):
        for z in range(1, 13):
            if not _na_torre(x, z):
                continue
            e.por(x, G, z, "stone_bricks" if _borda_torre(x, z) else Mistura(("smooth_stone", 2),
                                                                             ("polished_andesite", 1)))
            if _borda_torre(x, z):
                for y in range(G + 1, topo_parede + 1):
                    e.por(x, y, z, TORRE if y not in (G + 6, G + 18) else "chiseled_stone_bricks")
            else:
                e.ar_onde_vazio(x, G + 1, z, x, topo_parede + 4, z)
    # o lado que encosta na casa: abre a passagem
    m.vao(e, 16, G + 1, 9, 16, G + 2, 9)
    e.ar(15, G + 1, 9)
    e.ar(15, G + 2, 9)
    # escada em caracol (degraus colados na parede de dentro), sobe até o mirante
    anel = []
    for x in range(14, 25):
        for z in range(1, 13):
            if _na_torre(x, z, TR - 1.0) and not _na_torre(x, z, TR - 2.05):
                ang = math.atan2(z + 0.5 - TZ, x + 0.5 - TX)
                anel.append((ang, x, z))
    anel.sort()
    # começa no oeste (perto da passagem) e gira no sentido horário visto de cima
    inicio = min(range(len(anel)), key=lambda i: abs(anel[i][1] - 17) + abs(anel[i][2] - 9))
    ordem = anel[inicio:] + anel[:inicio]
    y = G + 1
    passos = []
    i = 0
    while y <= G + 17:
        _, x, z = ordem[i % len(ordem)]
        _, nx, nz = ordem[(i + 1) % len(ordem)]
        dx, dz = nx - x, nz - z
        f = "east" if dx > 0 else "west" if dx < 0 else "south" if dz > 0 else "north"
        e.por(x, y, z, "stone_brick_stairs", facing=f, **L.DEGRAU)
        if y > G + 1 and e.vazio(x, y - 1, z):
            e.por(x, y - 1, z, "stone_brick_slab", **L.LAJE_TOPO)
        passos.append((x, y, z))
        y += 1
        i += 1
    # miolo de pedra (pilar central) até o mirante
    for x in range(14, 25):
        for z in range(1, 13):
            if _na_torre(x, z, TR - 2.05):
                for yy in range(G + 1, G + 18):
                    e.por(x, yy, z, "polished_andesite" if yy % 5 else "chiseled_stone_bricks")
    # janelas estreitas na escada
    for ang, yy in ((0.3, G + 4), (2.0, G + 8), (3.6, G + 12), (5.0, G + 15), (1.0, G + 16)):
        x = int(TX + (TR - 0.5) * math.cos(ang))
        z = int(TZ + (TR - 0.5) * math.sin(ang))
        if _borda_torre(x, z):
            e.ar(x, yy, z)
            e.ar(x, yy + 1, z)
    # piso do mirante (deixa o furo da chegada da escada)
    furo = {(x, z) for x, yy, z in passos[-3:]}
    for x in range(14, 25):
        for z in range(1, 13):
            if _na_torre(x, z, TR - 1.0) and (x, z) not in furo:
                e.por(x, G + 18, z, Mistura(("spruce_planks", 3), ("dark_oak_planks", 2)))
    # mirante: janelas largas em volta (y 20-22), lanterna acesa, luneta, círculo de giz pela metade
    for ang in range(0, 360, 45):
        a = math.radians(ang + 22)
        x = int(TX + (TR - 0.5) * math.cos(a))
        z = int(TZ + (TR - 0.5) * math.sin(a))
        if _borda_torre(x, z):
            for yy in (G + 20, G + 21):
                if e.chance(0.3):
                    e.por(x, yy, z, "iron_bars", **L.SEM_LADOS)
                else:
                    e.ar(x, yy, z)
    for xx, zz in ((19, 3), (20, 3)):
        for yy in (G + 20, G + 21):
            e.ar(xx, yy, zz)
    for xx in (19, 20):  # janela grande do sul, de onde se vê a luz de longe
        for yy in (G + 19, G + 20, G + 21):
            e.ar(xx, yy, 10)
    # luneta (poste + tubo de corrente deitada) na janela norte: olha para a Fazenda
    e.por(19, G + 19, 4, "spruce_fence", **L.SEM_LADOS)
    e.por(19, G + 20, 4, "chain", axis="z", waterlogged=False)
    # círculo de giz pela metade (centro = cinzas)
    cx, cz = 20, 7
    for x in range(cx - 2, cx + 3):
        for z in range(cz - 2, cz + 3):
            d = math.hypot(x - cx, z - cz)
            if 1.5 <= d <= 2.3 and (x - cx) >= -0.5 and _na_torre(x, z, TR - 1.0) and (x, z) not in furo:
                e.por(x, G + 19, z, "white_carpet")
    e.por(cx, G + 19, cz, "gray_carpet")
    m.lanterna_pendurada(e, 19, G + 21, 9, comprimento_corrente=0)
    # telhado cônico de telha escura, com ponta de ferro torta
    for k in range(0, 7):
        r = TR + 1.1 - k * 0.78
        yy = topo_parede + 1 + k
        for x in range(14, 25):
            for z in range(0, 13):
                d = math.hypot(x + 0.5 - TX, z + 0.5 - TZ)
                if r - 1.0 < d <= r:
                    if k == 0:
                        e.por(x, yy, z, "deepslate_tile_slab", **L.LAJE)
                    else:
                        e.por(x, yy, z, "deepslate_tiles")
                elif d <= r - 1.0 and k == 0:
                    e.por(x, yy, z, "dark_oak_planks")
    e.por(19, topo_parede + 8, 6, "iron_bars", **L.SEM_LADOS)
    e.por(19, topo_parede + 7, 6, "deepslate_tiles")
    e.ar_onde_vazio(16, G + 19, 3, 23, topo_parede, 10, condicao=lambda x, y, z: _na_torre(x, z, TR - 1.0))
    m.teias(e, 15, G + 2, 3, 23, G + 17, 10, 10)
    e.pista(*passos[len(passos) // 2], "escada", "Meio da escada em caracol: onde o Pendurado aparece.")


def exterior(e):
    L.chao_morto(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, borda_irregular=3)
    # caminho de lajes até a porta
    for z in range(18, TAM[2]):
        for x in (8, 9, 10):
            if x == 9 or e.chance(0.4):
                e.por(x, G, z, Mistura(("dirt_path", 3), ("gravel", 1), ("cobblestone", 1)))
    # muro baixo de pedra em ruínas (frente)
    for x in list(range(0, 8)) + list(range(11, 26)):
        if e.chance(0.65) and e.dentro(x, G + 1, 20):
            e.por(x, G + 1, 20, Mistura(("cobblestone", 3), ("mossy_cobblestone", 1)))
            if e.chance(0.3):
                e.por(x, G + 2, 20, "cobblestone_slab", type="bottom", waterlogged=False)
    # um poste com um mapa velho pregado (placa sem texto) e árvores mortas
    L.placa(e, 12, G + 1, 21, [""], rotation=8)
    L.arvore_morta(e, 1, G + 1, 3, 9, galhos=4)
    L.arvore_morta(e, 24, G + 1, 17, 7, galhos=3)
    L.arvore_morta(e, 1, G + 1, 19, 6, galhos=3, tronco="spruce_log")
    # varal de papéis rasgados (corda de cerca, lã branca)
    for x in range(17, 23):
        e.por(x, G + 3, 15, "spruce_fence", east=x < 22, west=x > 17, north=False, south=False, waterlogged=False)
    for x in (17, 22):
        for y in (G + 1, G + 2):
            e.por(x, y, 15, "spruce_fence", **L.SEM_LADOS)
    L.mato_seco(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, chance=0.06)


def marcadores(e):
    for i, (x, dy) in enumerate(PONTOS_MAPA, 1):
        e.marcador(x, G + dy, 10, f"mapa:p{i:02d}",
                   "Ponto possível da marca (alvo = bloco do mural atrás). A cada manhã o mod sorteia onde ficam "
                   "as marcas; a última tem o nome de {marcado}." if i == 1 else "", alvo=[x, G + dy, 9])
    e.marcador(10, G + 3, 10, "mapa:vila_calada", "Marca fixa (já é red_terracotta no arquivo): a Vila Calada.",
               alvo=[10, G + 3, 9])
    L.ponto(e, 20, G + 20, 7, "evento:circulo_giz",
            "Centro do círculo de giz do mirante: ritual em dupla ou mais, de noite; as cinzas formam a palavra.")
    L.ponto(e, 19, G + 21, 4, "evento:luneta",
            "Luneta na janela norte (vire a janela para o lado da Fazenda ao colocar). Depois do Atlas, mostra a "
            "Torre das Correntes na neblina.")
    L.ponto(e, 7, G + 2, 13, "evento:mesa_cartografia", "Mesa de cartografia com o mapa pela metade.")
    L.ponto(e, 9, G + 3, 15, "evento:entrada", "Porta da frente (sem tranca) e janela grande quebrada do salão.")
    L.ponto(e, 21, G + 9, 4, "evento:pendurado_escada",
            "Escada em caracol: o Pendurado aparece; quem fica sozinho aqui perde Lucidez devagar.")
