"""4. Torre das Correntes. Ficha: lore/fichas-estruturas.md, seção 4. Tamanho 20 x 20 x 40, mais 8 de porão.

A origem do barulho de correntes. Torre quadrada de cantos cortados (x 5-14, z 5-14) no alto do morro, dentro de um
muro baixo quebrado com portão ao sul (estandarte da Cruz de Ferro).
- Térreo: sala de guarda (mesa, armeiro vazio), o monte de feno no fundo do vão, o alçapão do porão.
- Vão aberto até o patamar, com a escada colada na parede de dentro e degraus faltando (queda não mata: feno).
- Patamar (y 34): a fileira de blocos de nota e, no teto, o alçapão com cadeado de letras.
- Sala do sino (y 38-44): o sino de ferro grande, sem badalo, pendurado por correntes; correntes soltas arrastam
  no chão e escorrem pelos arcos, descendo pela fachada.
- Porão (y 0-7): duas salas de pedra úmida, a jukebox do Disco Riscado e o caderno do vigia.
"""
from estrutura import Estrutura, Mistura
import modulos as m
import lugares as L

NOME = "torre_correntes"
TITULO = "4. Torre das Correntes"
TAM = (20, 48, 20)
SEMENTE = 1404
G = 8
X0, Z0, X1, Z1 = 5, 5, 14, 14
PATAMAR = G + 23     # piso do patamar (as notas ficam em cima dele)
PISO_SINO = G + 26   # piso da sala do sino (o alçapão)
CORTES = [("porao", 0, G - 2), ("terreo", G, G + 4), ("meio_da_escada", G + 5, G + 16),
          ("patamar", G + 17, PISO_SINO - 1), ("sala_do_sino", PISO_SINO, PISO_SINO + 4)]
FRENTE_ISO = 0
FRENTE_GRAUS = 10
LONGE = {"dist": 70, "altura_olho": 2.0}
RESUMO = """
Isolamento, vento e correntes: aqui o som é o personagem. Entra-se pelo portão (aberto) ou escalando o muro (tira
Lucidez). O vão da escada é aberto do térreo ao patamar: quem cai bate no feno (o mod segura o dano, decisão N7).
O alçapão do teto do patamar tem cadeado de letras (resposta do Atlas): certo, as correntes travam, o sino cala e os
blocos de nota tocam em Morse o rumo da Fazenda. Porão: jukebox (Disco Riscado) e caderno do vigia; quem desce
sozinho perde Lucidez. Chão em `y_chao = 8`: o porão ocupa y 0 a 7.
"""

PEDRA = Mistura(("stone_bricks", 4), ("cracked_stone_bricks", 4), ("mossy_stone_bricks", 1), ("cobblestone", 1),
                ("cobbled_deepslate", 2), ("andesite", 1))
PEDRA_UMIDA = Mistura(("mossy_stone_bricks", 3), ("cracked_stone_bricks", 3), ("mossy_cobblestone", 2),
                      ("cobbled_deepslate", 2))
CHAO_MORRO = Mistura(("coarse_dirt", 4), ("gravel", 3), ("andesite", 2), ("cobblestone", 1), ("podzol", 2),
                     ("rooted_dirt", 1))


def _dentro(x, z):
    if not (X0 <= x <= X1 and Z0 <= z <= Z1):
        return False
    return not (x in (X0, X1) and z in (Z0, Z1))


def _borda(x, z):
    return _dentro(x, z) and any(not _dentro(x + dx, z + dz) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))


def construir(semente=SEMENTE):
    e = Estrutura(NOME, TAM, semente)
    e.y_chao = G
    porao(e)
    torre(e)
    escada(e)
    sala_do_sino(e)
    muro(e)
    marcadores(e)
    L.sem_spawn(e, 6, 1, 6, 13, 1, 13, passo=3)
    L.sem_spawn(e, 6, G + 1, 6, 13, G + 1, 13, passo=3)
    L.sem_spawn(e, 6, PATAMAR + 1, 6, 13, PATAMAR + 1, 13, passo=3)
    L.sem_spawn(e, 6, PISO_SINO + 1, 6, 13, PISO_SINO + 1, 13, passo=3)
    return e


# ---------------------------------------------------------------------------
def porao(e):
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, 0, z, PEDRA_UMIDA if x in (X0, X1) or z in (Z0, Z1) else
                  Mistura(("mud", 2), ("cobblestone", 2), ("mossy_cobblestone", 2), ("gravel", 1)))
    m.paredes(e, X0, Z0, X1, Z1, 1, G - 1, PEDRA_UMIDA)
    e.ar_onde_vazio(X0 + 1, 1, Z0 + 1, X1 - 1, G - 1, Z1 - 1)
    # parede do meio (duas salas), com passagem
    for z in range(Z0 + 1, Z1):
        for y in range(1, G):
            e.por(10, y, z, PEDRA_UMIDA)
    m.vao(e, 10, 1, 9, 10, 2, 9)
    # sala oeste: a jukebox e um banco
    e.por(6, 1, 12, "jukebox", has_record=False)
    e.marcador(6, 2, 12, "disco:disco_riscado", "Jukebox com o Disco Riscado (E6: a imagem escondida no som).")
    m.cadeira(e, 7, 1, 12, "west", "spruce_stairs")
    e.por(8, 1, 6, "barrel", facing="up", open=False)
    m.vela(e, 6, 1, 7, 1, acesa=False)
    # sala leste: o caderno do vigia, cama de palha, a escada de mão do alçapão
    L.atril(e, 12, 1, 12, "north", "caderno_vigia",
            "Caderno do vigia: fala do barulho, sem explicar.")
    e.por(13, 1, 7, "hay_block", axis="x")
    e.por(12, 1, 7, "hay_block", axis="x")
    e.por(13, 1, 8, "brown_carpet")
    m.lanterna_pendurada(e, 11, G - 2, 12, com_lanterna=True, comprimento_corrente=1)
    for y in range(1, G + 1):
        e.por(13, y, 11, "ladder", facing="west", waterlogged=False)
    e.por(13, G, 11, "spruce_trapdoor", facing="west", half="top", open=False, powered=False, waterlogged=False)
    m.teias(e, 6, 4, 6, 13, G - 1, 13, 14)
    m.mancha(e, 7, 1, 10, 0, 1.0)


def torre(e):
    topo = PISO_SINO + 7
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if not _dentro(x, z):
                continue
            if _borda(x, z):
                e.por(x, G, z, "stone_bricks")
                for y in range(G + 1, topo + 1):
                    cinta = y in (G + 1, G + 12, G + 24, PISO_SINO)
                    e.por(x, y, z, "chiseled_stone_bricks" if cinta and (x + z) % 3 == 0 else
                          ("polished_andesite" if cinta else PEDRA))
            else:
                if (x, G, z) not in e.blocos:
                    e.por(x, G, z, Mistura(("cobblestone", 3), ("gravel", 1), ("stone_bricks", 2)))
                e.ar_onde_vazio(x, G + 1, z, x, PATAMAR - 1, z)
    # contrafortes nos cantos (base mais larga)
    for cx, cz, dx, dz in ((X0, Z0 + 1, -1, 0), (X0 + 1, Z0, 0, -1), (X1, Z0 + 1, 1, 0), (X1 - 1, Z0, 0, -1),
                           (X0, Z1 - 1, -1, 0), (X0 + 1, Z1, 0, 1), (X1, Z1 - 1, 1, 0), (X1 - 1, Z1, 0, 1)):
        for y in range(G + 1, G + 7):
            e.por(cx + dx, y, cz + dz, PEDRA)
        e.por(cx + dx, G + 7, cz + dz, "stone_brick_stairs", facing={(-1, 0): "east", (1, 0): "west",
                                                                       (0, -1): "south", (0, 1): "north"}[(dx, dz)],
              **L.DEGRAU)
    # cintas de pedra salientes (ressalto de degrau invertido em volta)
    for yc in (G + 12, G + 22):
        for x in range(X0, X1 + 1):
            for z in range(Z0, Z1 + 1):
                if not _borda(x, z):
                    continue
                for dx, dz, f in ((-1, 0, "west"), (1, 0, "east"), (0, -1, "north"), (0, 1, "south")):
                    if not _dentro(x + dx, z + dz) and e.vazio(x + dx, yc, z + dz):
                        e.por(x + dx, yc, z + dz, "stone_brick_stairs", facing={"west": "east", "east": "west",
                                                                                "north": "south",
                                                                                "south": "north"}[f],
                              half="top", shape="straight", waterlogged=False)
    # porta (sul) em arco, aberta
    m.vao(e, 9, G + 1, Z1, 10, G + 3, Z1)
    e.por(9, G + 3, Z1, "stone_brick_stairs", facing="east", half="top", shape="straight", waterlogged=False)
    e.por(10, G + 3, Z1, "stone_brick_stairs", facing="west", half="top", shape="straight", waterlogged=False)
    m.porta(e, 9, G + 1, Z1, "north", hinge="left", aberta=True, material="dark_oak_door")
    # frestas (seteiras) ao longo da subida
    for x, y, z in ((X0, G + 6, 9), (X1, G + 10, 10), (9, G + 14, Z0), (X0, G + 18, 10), (10, G + 21, Z1),
                    (X1, G + 23, 8), (8, G + 9, Z1), (11, G + 17, Z0)):
        e.ar(x, y, z)
        e.ar(x, y + 1, z)
    # térreo: mesa de guarda, armeiro vazio, feno no fundo do vão
    for x in range(8, 12):
        for z in range(8, 12):
            h = 2 if 9 <= x <= 10 and 9 <= z <= 10 else 1
            for y in range(h):
                e.por(x, G + 1 + y, z, "hay_block", axis="y" if y else ("x" if (x + z) % 2 else "z"))
    m.mesa(e, 12, 12, 12, 12, G + 1, "dark_oak_trapdoor")
    for z in (7, 8, 9):
        e.por(X1 - 1, G + 1, z, "dark_oak_fence", **L.SEM_LADOS)
        e.por(X1 - 1, G + 2, z, "spruce_trapdoor", facing="west", half="bottom", open=True, powered=False,
              waterlogged=False)
    m.lanterna_pendurada(e, 7, G + 3, 12, com_lanterna=True, comprimento_corrente=0)
    e.por(7, G + 4, 12, "dark_oak_slab", **L.LAJE_TOPO)
    # patamar (piso do topo do vão)
    for x in range(X0 + 1, X1):
        for z in range(Z0 + 1, Z1):
            if _dentro(x, z) and not _borda(x, z):
                e.por(x, PATAMAR, z, Mistura(("spruce_planks", 3), ("dark_oak_planks", 2)))
                e.ar_onde_vazio(x, PATAMAR + 1, z, x, PISO_SINO - 1, z)
    # a fileira de blocos de nota (parede norte do patamar)
    for i, x in enumerate(range(7, 13)):
        e.por(x, PATAMAR + 1, Z0 + 1, "note_block", instrument="bass", note=2 if i % 2 == 0 else 19, powered=False)
        e.pista(x, PATAMAR + 1, Z0 + 1, "nota", f"Bloco de nota {i + 1} de 6: tocam o rumo da Fazenda em Morse.")
    # teto do patamar = piso da sala do sino, com o alçapão de cadeado e a escada de mão
    for x in range(X0 + 1, X1):
        for z in range(Z0 + 1, Z1):
            if _dentro(x, z) and not _borda(x, z):
                e.por(x, PISO_SINO, z, Mistura(("stone_bricks", 3), ("cracked_stone_bricks", 2), ("cobblestone", 1)))
    e.por(6, PISO_SINO, 9, "iron_trapdoor", facing="east", half="top", open=False, powered=False, waterlogged=False)
    for y in range(PATAMAR + 1, PISO_SINO):
        e.por(6, y, 9, "ladder", facing="east", waterlogged=False)
    m.teias(e, 6, G + 20, 6, 13, PATAMAR - 1, 13, 10)
    m.teias(e, 6, PATAMAR + 2, 6, 13, PISO_SINO - 1, 13, 6)


def escada(e):
    """Degraus colados na parede de dentro, do térreo ao patamar, alguns faltando."""
    anel = []
    for z in range(Z0 + 1, Z1):          # oeste, descendo para o sul
        anel.append((X0 + 1, z))
    for x in range(X0 + 2, X1):           # sul, para o leste
        anel.append((x, Z1 - 1))
    for z in range(Z1 - 2, Z0, -1):       # leste, para o norte
        anel.append((X1 - 1, z))
    for x in range(X1 - 2, X0 + 1, -1):   # norte, para o oeste
        anel.append((x, Z0 + 1))
    anel = [p for p in anel if _dentro(*p)]
    # começa perto da porta (sul), sobe no sentido anti-horário visto de cima
    inicio = anel.index((9, Z1 - 1))
    ordem = anel[inicio:] + anel[:inicio]
    y, i = G + 1, 0
    faltando = {7, 8, 15, 21, 30, 38, 39}
    while y < PATAMAR:
        x, z = ordem[i % len(ordem)]
        nx, nz = ordem[(i + 1) % len(ordem)]
        f = "east" if nx > x else "west" if nx < x else "south" if nz > z else "north"
        if i not in faltando:
            e.por(x, y, z, "stone_brick_stairs", facing=f, **L.DEGRAU)
            if y > G + 1:
                e.por(x, y - 1, z, "stone_brick_slab", **L.LAJE_TOPO)
        else:
            e.ar(x, y, z)
            e.ar(x, y - 1, z)
            if e.chance(0.5):
                e.por(x, y - 1, z, "cobweb")
        y += 1
        i += 1
    # o último degrau chega no patamar: abre o piso ali
    x, z = ordem[(i - 1) % len(ordem)]
    e.ar(x, PATAMAR, z)


def sala_do_sino(e):
    S = PISO_SINO
    # arcos grandes nos quatro lados (y S+1 a S+5), com parapeito baixo
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            if not _borda(x, z):
                continue
            no_meio = (7 <= x <= 12 and z in (Z0, Z1)) or (7 <= z <= 12 and x in (X0, X1))
            if no_meio:
                for y in range(S + 2, S + 6):
                    e.ar(x, y, z)
                e.por(x, S + 1, z, "stone_brick_slab", **L.LAJE)
                if x in (7, 12) and z in (Z0, Z1) or z in (7, 12) and x in (X0, X1):
                    f = {Z0: "south", Z1: "north"}.get(z) if z in (Z0, Z1) else ("east" if x == X0 else "west")
                    e.por(x, S + 5, z, "stone_brick_stairs", facing=f, half="top", shape="straight",
                          waterlogged=False)
    e.ar_onde_vazio(X0 + 1, S + 1, Z0 + 1, X1 - 1, S + 7, Z1 - 1, condicao=lambda x, y, z: _dentro(x, z))
    # cimalha e telhado em pirâmide íngreme, com a ponta de ferro
    cim = S + 8
    for x in range(X0 - 1, X1 + 2):
        for z in range(Z0 - 1, Z1 + 2):
            if x in (X0 - 1, X1 + 1) or z in (Z0 - 1, Z1 + 1):
                if not ((x in (X0 - 1, X1 + 1)) and (z in (Z0 - 1, Z1 + 1))):
                    e.por(x, cim - 1, z, "stone_brick_slab", **L.LAJE_TOPO)
    for k in range(0, 6):
        a0, a1 = X0 + k, X1 - k
        b0, b1 = Z0 + k, Z1 - k
        if a0 > a1:
            break
        y = cim + k
        if y >= TAM[1]:
            break
        for x in range(a0, a1 + 1):
            for z in range(b0, b1 + 1):
                if x in (a0, a1) or z in (b0, b1):
                    f = "east" if x == a0 else "west" if x == a1 else "south" if z == b0 else "north"
                    if a1 - a0 > 1:
                        e.por(x, y, z, "deepslate_tile_stairs", facing=f, **L.DEGRAU)
                    else:
                        e.por(x, y, z, "deepslate_tiles")
                elif k == 0:
                    e.por(x, y, z, "dark_oak_planks")
    # vigas e o sino de ferro (sem badalo)
    for x in range(X0 + 1, X1):
        e.por(x, S + 7, 9, "dark_oak_log", axis="x")
        e.por(x, S + 7, 10, "dark_oak_log", axis="x")
    for x, z in ((9, 9), (10, 10), (9, 10), (10, 9)):
        e.por(x, S + 6, z, "chain", axis="y", waterlogged=False)
    for x in (9, 10):
        for z in (9, 10):
            e.por(x, S + 5, z, "iron_block")
    for x in range(8, 12):
        for z in range(8, 12):
            if x in (8, 11) or z in (8, 11):
                if not (x in (8, 11) and z in (8, 11)):
                    e.por(x, S + 4, z, "iron_block")
                e.por(x, S + 3, z, "iron_block")
            else:
                e.ar(x, S + 4, z)
                e.ar(x, S + 3, z)
    for x in range(7, 13):
        for z in range(7, 13):
            if (x in (7, 12) or z in (7, 12)) and not (x in (7, 12) and z in (7, 12)):
                e.por(x, S + 2, z, "iron_trapdoor", facing="north", half="top", open=False, powered=False,
                      waterlogged=False)
    # correntes soltas que arrastam no chão, até os arcos, e escorrem pela fachada
    for x in (7, 8, 10, 11):
        e.por(x, S + 1, 12, "chain", axis="x", waterlogged=False)
    for z in (11, 13):
        e.por(10, S + 1, z, "chain", axis="z", waterlogged=False)
    for x, z, comp in ((10, Z1 + 1, 16), (8, Z0 - 1, 22), (X0 - 1, 11, 12), (X1 + 1, 8, 19), (12, Z1 + 1, 9),
                       (7, Z1 + 1, 25), (X0 - 1, 7, 26), (X1 + 1, 12, 24), (11, Z0 - 1, 14)):
        if not e.dentro(x, S, z):
            continue
        e.por(x, S + 1, z, "chain", axis="y", waterlogged=False)
        for y in range(S, S - comp, -1):
            if y <= G or not e.vazio(x, y, z):
                break
            e.por(x, y, z, "chain", axis="y", waterlogged=False)
    # correntes grossas dos cantos da cimalha até o chão, presas em blocos de ferro (a torre amarrada)
    for x, z in ((X0 - 1, Z0), (X1 + 1, Z1), (X0, Z1 + 1), (X1, Z0 - 1)):
        for y in range(G + 2, S + 7):
            if e.vazio(x, y, z):
                e.por(x, y, z, "chain", axis="y", waterlogged=False)
        e.por(x, G + 1, z, "iron_block")
    m.teias(e, 6, S + 4, 6, 13, S + 6, 13, 8)


def muro(e):
    # chão do morro (pedra e cascalho)
    L.chao_morto(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, borda_irregular=2, material=CHAO_MORRO)
    for x in range(1, 19):
        for z in range(1, 19):
            if not (x in (1, 18) or z in (1, 18)):
                continue
            if 8 <= x <= 11 and z == 18:
                continue
            h = 2 if (x * 7 + z * 3) % 5 else 3
            if e.chance(0.18):
                h = 0 if e.chance(0.5) else 1
            for y in range(G + 1, G + 1 + h):
                e.por(x, y, z, Mistura(("cobblestone", 3), ("cobbled_deepslate", 2), ("mossy_cobblestone", 1),
                                       ("cracked_stone_bricks", 1)))
            if h and e.chance(0.4):
                e.por(x, G + 1 + h, z, "cobblestone_slab", type="bottom", waterlogged=False)
    # portão: dois pilares, folhas abertas, estandarte da Cruz de Ferro
    for x in (8, 11):
        for y in range(G + 1, G + 5):
            e.por(x, y, 18, "stone_bricks" if y < G + 4 else "chiseled_stone_bricks")
    e.por(9, G + 1, 18, "dark_oak_fence_gate", facing="south", in_wall=False, open=True, powered=False)
    L.estandarte_cruz(e, 8, G + 3, 19, facing="south")
    e.pista(8, G + 3, 19, "estandarte", "Cruz de Ferro no pilar do portão (provisória: desenho final do Modelador).")
    for z in range(19, 20):
        for x in (9, 10):
            e.por(x, G, z, "gravel")
    # caminho de pedra até a porta
    for z in range(15, 18):
        for x in (9, 10):
            e.por(x, G, z, Mistura(("cobblestone", 2), ("gravel", 2), ("andesite", 1)))
    L.arvore_morta(e, 2, G + 1, 3, 8, galhos=4)
    L.arvore_morta(e, 16, G + 1, 16, 6, galhos=3)
    L.mato_seco(e, 0, 0, TAM[0] - 1, TAM[2] - 1, G, chance=0.05)


def marcadores(e):
    L.ponto(e, 9, PISO_SINO + 2, 9, "som:sino_correntes",
            "O sino de ferro sem badalo: correntes balançam e arrastam na pedra sem vento. Cala com a palavra de "
            "retorno.")
    L.ponto(e, 7, PISO_SINO - 1, 9, "evento:cadeado_alcapao",
            "Alçapão do teto do patamar (x 6, z 9): cadeado de letras, resposta do Atlas. Certo: correntes travam, "
            "o sino cala e as notas tocam o rumo da Fazenda.")
    L.ponto(e, 9, PATAMAR + 2, Z0 + 2, "som:notas_rumo", "Os 6 blocos de nota do patamar (x 7-12, z 6).")
    L.ponto(e, 9, G + 3, 9, "evento:queda", "Fundo do vão: o feno amortece. Queda não mata (dano segurado pelo mod).")
    L.ponto(e, 9, 2, 9, "evento:porao_sozinho", "Porão: quem desce sozinho perde Lucidez; em dupla, não.")
    L.ponto(e, 9, G + 2, 19, "evento:portao", "Portão aberto (sul). Escalar o muro tira Lucidez e sobe Exposição.")
