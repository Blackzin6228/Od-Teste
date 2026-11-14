"""VILA CALADA 150 x 150 (decisão de 12/11/2026): o centro de 100 x 100 INTACTO mais uma coroa de 25 blocos
de arredores, densa e com cara de Minecraft.

O centro é o vila_calada.py (mesma semente, mesmo arquivo): ele entra deslocado de (25, 0, 25) e fica TRAVADO.
Nenhuma função daqui consegue escrever dentro dele (a Estrutura levanta erro). O teste confere bloco a bloco.

Tamanho 150 x 27 x 150 (x, y, z). Norte = z 0. Origem = canto noroeste de baixo. A camada y 0 ASSENTA EM CIMA
do terreno, como no centro (nada abaixo, nada cavado). Covas fora do muro ficam numa lombada de 1 bloco: o
buraco atravessa a lombada e o chão da vila; o fundo é o terreno do mundo.

Arredores (faixa de 25 em volta):
- SUL: a estrada de chegada (continua a rua principal), cerca caída dos dois lados, a carroça tombada no meio da
  estrada (de lado, uma roda no ar, carga espalhada, jukebox em cima da carga) e os bois mortos na frente.
  Campo queimado dos dois lados: queimado colado ao muro, vivo mais longe (o fogo veio da vila). Espantalho em pé
  no campo oeste, olhando para quem chega, com um baú enterrado aos pés.
- NORTE: o cemitério grande, fora do muro, numa lombada: grade de ferro com pilares, portão a leste com corrente,
  cerca de 60 covas (abertas com a terra jogada para fora, fechadas com o monte, ou velhas e afundadas), a casa
  do coveiro sem teto, duas árvores mortas, ossário no canto. Uma cova aberta guarda um baú no lugar do corpo.
- LESTE: o caminho dos enterros (do portão do cemitério até a saída leste da rua cruzada), o cruzeiro de ferro na
  bifurcação, o poço fora do muro (seco, o balde pendurado na corrente, um baú no fundo), o pasto com a cerca
  caída e os ossos do gado espalhados (morreu correndo).
- OESTE: a trilha para a floresta, saindo pela rua cruzada e serpenteando para o noroeste, com as marcas de
  arrasto (dois sulcos, gotas, coisas largadas) vindo DA floresta PARA a vila. Placa quebrada na bifurcação,
  fogueira apagada de quem esperou. No canto noroeste, a beira da floresta: pinheiros, troncos caídos, podzol.

Marcadores de recompensa (bloco de estrutura em modo DATA, UM bloco ACIMA do baú/jukebox de verdade):
    bau_carroca, disco_carroca, bau_cova, bau_poco_fora, bau_espantalho
Acento de cor: só o vermelho do Sangue (pó de redstone apagado). Sem luz invisível fora do muro (decisão de
12/11: os arredores ficam mais escuros e mais quietos; criaturas rondam perto do muro).
"""
import math

from estrutura import Estrutura, Mistura, ruido
import modulos as m
from modelos import vila_calada as V

NOME = "vila_calada_150"
BORDA = 25
TAM = (V.TAM[0] + 2 * BORDA, V.TAM[1], V.TAM[2] + 2 * BORDA)  # 150 x 27 x 150
CENTRO = (BORDA, BORDA, BORDA + V.TAM[0] - 1, BORDA + V.TAM[2] - 1)  # x0, z0, x1, z1 = 25, 25, 124, 124
SEMENTE = 1503
G = 0
GERAR_COM_TODAS = False  # gerar.sh sem nome não renderiza esta (use gerar_vila_calada_150.sh)

CORTES = [("chao", G, G + 5)]

# faixas dos arredores para o mod (o arquivo inteiro passa do limite de colocação em partes): nome, x0, z0, x1, z1
FAIXAS = [
    ("norte", 0, 0, 149, 24),
    ("sul", 0, 125, 149, 149),
    ("oeste", 0, 25, 24, 124),
    ("leste", 125, 25, 149, 124),
]

# prévias de perto dos arredores: (nome, x0, z0, x1, z1)
PARTES = [
    ("estrada_carroca", 58, 118, 96, 149),
    ("campo_espantalho", 25, 120, 62, 149),
    ("cemiterio", 44, 0, 112, 30),
    ("poco_fora", 118, 62, 149, 100),
    ("trilha_floresta", 0, 0, 44, 90),
    ("pasto_leste", 118, 88, 149, 128),
]

LAJE, DEGRAU, SOLTO, MURETA = V.LAJE, V.DEGRAU, V.SOLTO, V.MURETA
TAMPA = {"half": "bottom", "open": False, "powered": False, "waterlogged": False}  # tábua caída no chão
PEDRA_SOLTA = Mistura(("cobblestone", 5), ("mossy_cobblestone", 3), ("andesite", 2))
LAPIDE = Mistura((("cobblestone_wall", MURETA), 3), (("mossy_cobblestone_wall", MURETA), 2),
                 (("stone_brick_stairs", dict(DEGRAU, facing="north")), 1),
                 (("spruce_sign", {"rotation": 0, "waterlogged": False}), 2))
MONTE = Mistura(("coarse_dirt", 5), ("dirt", 2), ("gravel", 1))
CHAO_FLORESTA = Mistura(("podzol", 5), ("grass_block", 3), ("moss_block", 1), ("coarse_dirt", 1))
CAMINHO = Mistura(("dirt_path", 7), ("gravel", 3), ("coarse_dirt", 2))
ARRASTO = Mistura(("coarse_dirt", 3), ("mud", 1), ("dirt_path", 1))

ESTRADA_X = 74  # continua a rua principal do centro (x 48-51 local -> 73-76)
RUA_CRUZADA = (79, 82)  # z da rua cruzada do centro (54-57 local)
CARROCA_Z = 137
POCO = (136, 80)
ESPANTALHO = (44, 139)
COVA_DO_BAU = (95, 16)


def fora(x, z):
    """Posição nos arredores (fora do centro travado)."""
    x0, z0, x1, z1 = CENTRO
    return not (x0 <= x <= x1 and z0 <= z <= z1)


def construir(semente=SEMENTE):
    centro = V.construir()
    e = Estrutura(NOME, TAM, semente)
    for (x, y, z), b in centro.blocos.items():
        e.blocos[(x + BORDA, y, z + BORDA)] = b
    for p in centro.pistas:
        x, y, z = p["pos"]
        e.pistas.append({"pos": [x + BORDA, y, z + BORDA], "tipo": p["tipo"], "nota": p["nota"]})
    e.travado = CENTRO

    chao(e)
    estrada(e)
    cercas_da_estrada(e)
    carroca(e)
    campos(e)
    espantalho(e)
    cemiterio(e)
    casa_do_coveiro(e)
    caminho_dos_enterros(e)
    cruzeiro(e)
    poco_fora(e)
    pasto(e)
    trilha(e)
    floresta(e)
    extras(e)
    pedras(e)
    arvores_mortas(e)
    sangue(e)
    vegetacao(e)
    V.conectar(e, filtro=lambda x, y, z: fora(x, z))
    marcadores(e)
    pistas(e)
    return e


# ---------------------------------------------------------------------------
# Chão dos arredores
# ---------------------------------------------------------------------------
def lomba(x, z):
    """Lombada do cemitério (1 bloco acima), de borda irregular."""
    d = ((x - 78) / 27.0) ** 2 + ((z - 12) / 10.5) ** 2
    return 1 if d < 0.78 + 0.3 * ruido(x, 11, z, 4.0, SEMENTE + 5) else 0


def chao_y(x, z):
    if 48 <= x <= 108 and z <= 24:
        return lomba(x, z)
    return 0


def zona_floresta(x, z):
    return (x < 52 and z < 26) or (x < 25 and z < 72)


def chao(e):
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            if not fora(x, z):
                continue
            borda = min(x, z, TAM[0] - 1 - x, TAM[2] - 1 - z)
            if borda < 2 and ruido(x, 3, z, 2.0, SEMENTE + 41) < (0.55 if borda == 0 else 0.3):
                continue  # o chão da vila "morre" no terreno do mundo
            topo = chao_y(x, z)
            for y in range(topo):
                e.por(x, y, z, "dirt")
            n = ruido(x, 0, z, 5.0, SEMENTE + 1)
            if zona_floresta(x, z) and ruido(x, 2, z, 4.0, SEMENTE + 3) > 0.42:
                e.por(x, topo, z, CHAO_FLORESTA)
            elif n < 0.24:
                e.por(x, topo, z, "coarse_dirt")
            elif n < 0.32:
                e.por(x, topo, z, "dirt")
            else:
                e.por(x, topo, z, "grass_block", snowy=False)


def chao_natural(e, x, z):
    return e.nome(x, chao_y(x, z), z) in ("minecraft:grass_block", "minecraft:dirt", "minecraft:coarse_dirt",
                                         "minecraft:podzol", "minecraft:moss_block")


def livre(e, x, z, y=None):
    """Lugar dos arredores com chão e nada em cima (para pôr um detalhe)."""
    if not (e.dentro(x, 0, z) and fora(x, z)):
        return False
    y = chao_y(x, z) + 1 if y is None else y
    return e.nome(x, y - 1, z) is not None and e.vazio(x, y, z) and (x, y, z) not in e.blocos


# ---------------------------------------------------------------------------
# SUL: estrada de chegada, cerca caída, carroça tombada, campos, espantalho
# ---------------------------------------------------------------------------
def eixo_estrada(z):
    """x do lado esquerdo da estrada em z (4 de largura). Serpenteia um pouco longe do portão."""
    if z <= 130:
        return ESTRADA_X - 1
    return ESTRADA_X - 1 + int(round((ruido(0, 0, z, 9.0, SEMENTE + 7) - 0.5) * 3))


def em_estrada(x, z):
    if z < CENTRO[3] + 1:
        return False
    c = eixo_estrada(z)
    return c - 1 <= x <= c + 4


def estrada(e):
    for z in range(CENTRO[3] + 1, TAM[2]):
        c = eixo_estrada(z)
        for x in range(c - 1, c + 5):
            if e.nome(x, G, z) is None:
                continue
            if x in (c - 1, c + 4):
                if ruido(x, 1, z, 2.0, SEMENTE + 2) < 0.45:
                    continue  # borda irregular
                b = V.SULCO
            elif x in (c, c + 3):
                b = V.SULCO if e.chance(0.7) else V.RUA  # sulcos de roda
            elif ruido(x, 8, z, 2.5, SEMENTE + 42) > 0.8:
                b = "mud"
            else:
                b = V.RUA
            e.por(x, G, z, b)
            e.ar_onde_vazio(x, G + 1, z, x, G + 3, z)
    # pegadas de cinza: continuam as do centro (z ímpar), todas para dentro
    for z in range(CENTRO[3] + 1, TAM[2], 2):
        c = eixo_estrada(z)
        x = c + 1 if (z // 2) % 2 == 0 else c + 2
        if livre(e, x, z) and e.nome(x, G, z) is not None:
            e.por(x, G + 1, z, "light_gray_carpet")


def cerca(e, pontos, falha=0.28, caida=0.18, semente=0, altura2=0.0):
    """Cerca de estacas ligando pontos (x, z) em linha: falhas, tábuas caídas no chão, alguma estaca alta."""
    for (xa, za), (xb, zb) in zip(pontos, pontos[1:]):
        passos = max(abs(xb - xa), abs(zb - za), 1)
        for i in range(passos + 1):
            x = round(xa + (xb - xa) * i / passos)
            z = round(za + (zb - za) * i / passos)
            if not livre(e, x, z) or em_estrada(x, z):
                continue
            n = ruido(x, 3, z, 1.6, SEMENTE + 15 + semente)
            y = chao_y(x, z) + 1
            if n > 1 - falha:
                if e.chance(caida):
                    e.por(x, y, z, "spruce_trapdoor", facing="north", **TAMPA)
                continue
            e.por(x, y, z, "spruce_fence", **SOLTO)
            if altura2 and e.chance(altura2):
                e.por(x, y + 1, z, "spruce_fence", **SOLTO)


def cercas_da_estrada(e):
    c0, c1 = ESTRADA_X - 5, ESTRADA_X + 6  # x 69 e 80
    z0, z1 = CENTRO[3] + 1, TAM[2] - 2
    cerca(e, [(c0, z0), (c0, z1)], falha=0.3, caida=0.3, semente=1)
    cerca(e, [(c1, z0), (c1, z1)], falha=0.3, caida=0.3, semente=2)
    # porteira aberta para o campo oeste e um trecho inteiro derrubado (tábuas no chão) a leste
    for z in (131, 132):
        e.por(c0, G + 1, z, "spruce_fence_gate", facing="east", in_wall=False, open=True, powered=False)
    for z in range(140, 145):
        if livre(e, c1, z):
            e.por(c1, G + 1, z, "spruce_trapdoor", facing="west", **TAMPA)
        if livre(e, c1 + 1, z) and e.chance(0.5):
            e.por(c1 + 1, G + 1, z, "spruce_trapdoor", facing="east", **TAMPA)
    # roda solta no campo, perto do portão
    roda(e, 64, G + 1, 128, "bottom")


def roda(e, cx, y, cz, meia):
    """Roda de carroça deitada (3 x 3 de alçapões em volta do cubo)."""
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            x, z = cx + dx, cz + dz
            if not (e.dentro(x, y, z) and fora(x, z)):
                continue
            if dx == 0 and dz == 0:
                e.por(x, y, z, "dark_oak_fence", **SOLTO)
            else:
                e.por(x, y, z, "dark_oak_trapdoor", facing="north", half=meia, open=False, powered=False,
                      waterlogged=False)


def carroca(e):
    """Carroça tombada de lado no meio da estrada, de frente para a vila. Caçamba em pé (parede de tábuas),
    tábuas da lateral no chão, eixo em pé com a roda de baixo no chão e a de cima no ar, carga espalhada
    para o lado em que caiu (oeste). Bois mortos na frente, ainda nos varais."""
    z0 = CARROCA_Z
    cx = eixo_estrada(z0 + 1) + 2  # meio da estrada
    bx = cx - 1  # a caçamba (parede)
    for z in range(z0, z0 + 4):
        e.por(bx, G + 1, z, "spruce_planks")
        e.por(bx, G + 2, z, "spruce_planks")
        e.por(bx, G + 3, z, "stripped_spruce_log", axis="z")
        e.por(bx - 1, G + 1, z, "spruce_trapdoor", facing="west", **TAMPA)  # lateral que soltou
    # eixo em pé e as duas rodas
    zm = z0 + 2
    for y in range(G + 1, G + 4):
        e.por(cx + 1, y, zm, "spruce_fence", **SOLTO)
    roda(e, cx + 1, G + 1, zm, "bottom")
    roda(e, cx + 1, G + 3, zm, "top")  # a roda no ar
    # varais (um no chão, outro no alto) e os bois
    for z in (z0 - 2, z0 - 1):
        e.por(bx, G + 1, z, "dark_oak_fence", **SOLTO)
        e.por(bx, G + 3, z, "dark_oak_fence", **SOLTO)
    for z in (z0 - 5, z0 - 4):
        e.por(bx, G + 1, z, "bone_block", axis="z")
        e.por(bx + 1, G + 1, z, "bone_block", axis="z")
    e.por(bx, G, z0 - 3, "coarse_dirt")
    # a carga, espalhada para oeste; a jukebox em cima do feno; o baú de viagem
    cz = bx - 2
    e.por(cz, G + 1, z0, "hay_block", axis="x")
    e.por(cz, G + 2, z0, "jukebox", has_record=False)
    e.por(cz, G + 1, z0 + 1, "white_wool")
    e.por(cz - 1, G + 1, z0 + 2, "white_wool")
    e.por(cz, G + 1, z0 + 2, "barrel", facing="west", open=True)
    e.por(cz - 1, G + 1, z0, "barrel", facing="up", open=False)
    e.por(cz, G + 1, z0 + 3, "chest", facing="west", type="single", waterlogged=False)
    e.por(cz - 2, G + 1, z0 + 1, "pumpkin")
    e.por(cz - 2, G + 1, z0 + 3, "spruce_trapdoor", facing="south", **TAMPA)
    e.por(cz - 1, G + 1, z0 + 4, "hay_block", axis="z")
    for x in range(cz - 2, cx + 3):
        for z in range(z0 - 5, z0 + 5):
            if fora(x, z):
                e.ar_onde_vazio(x, G + 1, z, x, G + 4, z)


def queima(x, z):
    """1 colado ao muro, 0 longe: o fogo veio da vila."""
    x0, z0, x1, z1 = CENTRO
    d = math.hypot(max(x0 - x, 0, x - x1), max(z0 - z, 0, z - z1))
    return max(0.0, min(1.0, 1.25 - d / 20.0))


def roca(e, x0, z0, x1, z1, linhas="x"):
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not livre(e, x, z) or em_estrada(x, z) or not chao_natural(e, x, z):
                continue
            q = queima(x, z)
            n = ruido(x, 4, z, 3.0, SEMENTE + 16)
            if (z if linhas == "x" else x) % 4 == 0:
                e.por(x, G, z, "coarse_dirt")
                continue
            if n < 0.2 + 0.55 * q:
                e.por(x, G, z, V.QUEIMADO_CHAO)
                if e.chance(0.12):
                    e.por(x, G + 1, z, "dead_bush")
            else:
                e.por(x, G, z, "farmland", moisture=0)
                r = e.rng.random()
                if r < 0.55:
                    idade = int((1 - q) * 7 + (e.rng.random() - 0.5) * 2.5)
                    e.por(x, G + 1, z, "wheat", age=max(0, min(7, idade)))
                elif r < 0.585 and q < 0.6:
                    e.por(x, G + 1, z, "pumpkin")


def campos(e):
    zs = CENTRO[3] + 2  # 126
    roca(e, 2, zs, ESTRADA_X - 6, TAM[2] - 2)        # campo oeste da estrada (até o canto sudoeste)
    roca(e, ESTRADA_X + 7, zs, TAM[0] - 3, TAM[2] - 2)  # campo leste (até o canto sudeste)
    roca(e, 2, 96, 22, CENTRO[3], linhas="z")        # roça menor na faixa oeste
    # cercas partidas em volta dos campos
    cerca(e, [(3, zs - 1), (ESTRADA_X - 6, zs - 1)], falha=0.4, caida=0.2, semente=3)
    cerca(e, [(ESTRADA_X + 7, zs - 1), (TAM[0] - 3, zs - 1)], falha=0.4, caida=0.2, semente=4)
    cerca(e, [(23, 95), (23, CENTRO[3] - 2)], falha=0.35, caida=0.2, semente=5)
    cerca(e, [(2, 95), (22, 95)], falha=0.35, caida=0.2, semente=6)


def espantalho(e):
    """Em pé, no campo oeste, de costas para a vila: olha para quem chega. Um baú enterrado aos pés."""
    x, z = ESPANTALHO
    for dx in range(-2, 3):
        for dz in range(-1, 3):
            px, pz = x + dx, z + dz
            if e.nome(px, G + 1, pz) in ("minecraft:wheat", "minecraft:dead_bush", "minecraft:pumpkin",
                                         "minecraft:short_grass", "minecraft:fern"):
                e.tirar(px, G + 1, pz)
            e.por(px, G, pz, V.QUEIMADO_CHAO)
    e.por(x, G + 1, z, "spruce_fence", **SOLTO)
    e.por(x, G + 2, z, "spruce_fence", **SOLTO)
    e.por(x, G + 3, z, "hay_block", axis="y")
    e.por(x - 1, G + 3, z, "spruce_fence", **SOLTO)
    e.por(x + 1, G + 3, z, "spruce_fence", **SOLTO)
    e.por(x, G + 4, z, "carved_pumpkin", facing="south")
    e.por(x, G, z + 1, "chest", facing="south", type="single", waterlogged=False)  # BAU_ESPANTALHO (enterrado)
    e.por(x - 1, G + 1, z + 1, "dead_bush")


# ---------------------------------------------------------------------------
# NORTE: cemitério grande, casa do coveiro
# ---------------------------------------------------------------------------
CEM = (53, 3, 103, 22)  # x0, z0, x1, z1 da grade
PORTAO_Z = (9, 10, 11)


def cemiterio(e):
    X0, Z0, X1, Z1 = CEM
    # grade de ferro com pilares de pedra; trechos caídos
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        if x == X1 and z in PORTAO_Z:
            continue
        y = chao_y(x, z) + 1
        pilar = (x - X0) % 5 == 0 and (z in (Z0, Z1)) or (z - Z0) % 5 == 0 and (x in (X0, X1))
        n = ruido(x, 6, z, 1.6, SEMENTE + 70)
        if pilar or (x in (X0, X1) and z in (Z0, Z1)):
            e.por(x, y, z, "cobblestone_wall", **MURETA)
            if n < 0.5:
                e.por(x, y + 1, z, "mossy_cobblestone_wall" if e.chance(0.4) else "cobblestone_wall", **MURETA)
        elif n > 0.74:
            if e.chance(0.3):
                e.por(x, y, z, "iron_bars", **SOLTO)  # pedaço caído
        else:
            e.por(x, y, z, "iron_bars", **SOLTO)
            if n < 0.3:
                e.por(x, y + 1, z, "iron_bars", **SOLTO)
    # portão leste: dois pilares altos e a corrente por cima; as folhas de grade caíram no chão
    for z in (PORTAO_Z[0] - 1, PORTAO_Z[-1] + 1):
        y0 = chao_y(X1, z) + 1
        for y in range(y0, y0 + 4):
            e.por(X1, y, z, "chiseled_stone_bricks" if y == y0 + 3 else "stone_bricks")
        m.trepadeira(e, X1 + 1, z, y0 + 2, "west", 2)
    yc = chao_y(X1, PORTAO_Z[1]) + 4
    for z in PORTAO_Z:
        e.por(X1, yc, z, "chain", axis="z", waterlogged=False)
    e.por(X1 + 1, chao_y(X1 + 1, 10) + 1, 10, "iron_bars", **SOLTO)
    e.por(X1 + 2, chao_y(X1 + 2, 11) + 1, 11, "iron_bars", **SOLTO)
    # alameda do portão até o fundo (z 8-9)
    for x in range(X0 + 1, X1):
        for z in (8, 9):
            if ruido(x, 1, z, 2.5, SEMENTE + 71) > 0.78:
                continue
            e.por(x, chao_y(x, z), z, CAMINHO)
    # covas: colunas de 3 em 3; fileiras (cova em z e z+1, lápide em z-1, monte ao lado)
    for linha in (6, 11, 16, 20):
        for gx in range(X0 + 3, X1 - 2, 3):
            if (gx, linha) == COVA_DO_BAU:
                cova(e, gx, linha, "bau")
                continue
            r = ruido(gx, 9, linha, 1.0, SEMENTE + 72)
            cova(e, gx, linha, "aberta" if r < 0.42 else ("fechada" if r < 0.86 else "velha"))
    # ossário no canto noroeste e lápide "diferente" (cinzelada) no meio
    y = chao_y(X0 + 2, Z0 + 1) + 1
    e.por(X0 + 2, y, Z0 + 1, "bone_block", axis="y")
    e.por(X0 + 2, y + 1, Z0 + 1, "bone_block", axis="y")
    e.por(X0 + 3, y, Z0 + 1, "bone_block", axis="x")
    e.por(X0 + 1, y, Z0 + 2, "bone_block", axis="z")
    arvore_morta(e, 67, 13, 7, ((1, 0, 4), (0, -1, 5), (-1, 0, 3)))
    arvore_morta(e, 94, 4, 6, ((-1, 0, 4), (0, 1, 3)))


def cova(e, gx, z, estado):
    topo = chao_y(gx, z)
    y = topo + 1
    if estado in ("aberta", "bau"):
        for zz in (z, z + 1):
            for yy in range(0, topo + 1):
                e.ar(gx, yy, zz)
            e.ar_onde_vazio(gx, topo + 1, zz, gx, topo + 2, zz)
        if estado == "bau":
            e.por(gx, 0, z, "chest", facing="south", type="single", waterlogged=False)  # BAU_COVA
        # terra jogada para fora
        e.por(gx + 1, y, z, MONTE)
        if e.chance(0.7):
            e.por(gx + 1, y, z + 1, MONTE)
        if e.chance(0.45):
            e.por(gx - 1, y, z + 1, "gravel")
        if e.chance(0.3):
            e.por(gx + 1, y + 1, z, "coarse_dirt")
    elif estado == "fechada":
        e.por(gx, y, z, MONTE)
        e.por(gx, y, z + 1, MONTE)
        if e.chance(0.22):
            e.por(gx, y + 1, z + 1, "wither_rose")
    else:  # velha: só a lápide e o chão afundado
        e.por(gx, topo, z, "coarse_dirt")
        e.por(gx, topo, z + 1, "coarse_dirt")
    if estado == "velha" and e.chance(0.35):
        return  # lápide perdida
    e.por(gx, chao_y(gx, z - 1) + 1, z - 1, "chiseled_stone_bricks" if estado == "bau" else LAPIDE)


def casa_do_coveiro(e):
    """Cabana de pedra do coveiro, a oeste do cemitério. Sem teto (só duas vigas), porta para o cemitério."""
    X0, Z0, X1, Z1 = 44, 5, 49, 10
    for x in range(X0, X1 + 1):
        for z in range(Z0, Z1 + 1):
            e.por(x, G, z, V.PEDRA_BASE if (x in (X0, X1) or z in (Z0, Z1)) else V.PISO)
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        n = ruido(x, 0, z, 2.0, SEMENTE + 73)
        h = G + 3 if (x in (X0, X1) and z in (Z0, Z1)) else G + 3 - int(n * 2.2)
        for y in range(G + 1, h + 1):
            e.por(x, y, z, V.PEDRA_PAREDE)
    m.vao(e, X1, G + 1, 7, X1, G + 2, 7)
    e.por(X1 + 1, G + 1, 8, "spruce_trapdoor", facing="east", **TAMPA)  # a porta, no chão
    m.janela(e, X0, 8, G + 2, G + 2, "z", quebrada=0.5, grade=1.0)
    for x in range(X0, X1 + 1):
        e.por(x, G + 4, 6, "dark_oak_log", axis="x")
        if x < X0 + 3:
            e.por(x, G + 4, 9, "dark_oak_log", axis="x")
    e.ar_onde_vazio(X0 + 1, G + 1, Z0 + 1, X1 - 1, G + 5, Z1 - 1)
    m.cama(e, X0 + 1, G + 1, Z1 - 1, "north", "light_gray")
    e.por(X1 - 1, G + 1, Z0 + 1, "barrel", facing="up", open=False)
    e.por(X1 - 2, G + 1, Z0 + 1, "cauldron")
    e.por(X0 + 1, G + 1, Z0 + 1, "composter", level=3)
    m.mesa(e, X1 - 1, 8, X1 - 1, 9, G + 1)
    m.teias(e, X0 + 1, G + 2, Z0 + 1, X1 - 1, G + 3, Z1 - 1, 4)
    m.chao_com_musgo(e, X0 + 1, Z0 + 1, X1 - 1, Z1 - 1, G + 1, 0.3)
    # pás e enxadas não existem como bloco: a pilha de terra e as carriolas (barris) contam a história
    e.por(X0 - 1, G + 1, 7, MONTE)
    e.por(X0 - 2, G + 1, 8, MONTE)
    e.por(X0 - 1, G + 1, 9, "barrel", facing="west", open=True)


# ---------------------------------------------------------------------------
# LESTE: caminho dos enterros, cruzeiro, poço fora do muro, pasto
# ---------------------------------------------------------------------------
def caminho(e, pontos, largura=2, falha=0.18, semente=0):
    """Caminho de terra ligando pontos (x, z); a grama come pedaços."""
    for (xa, za), (xb, zb) in zip(pontos, pontos[1:]):
        passos = max(abs(xb - xa), abs(zb - za), 1)
        ao_longo_x = abs(xb - xa) >= abs(zb - za)
        for i in range(passos + 1):
            x = round(xa + (xb - xa) * i / passos)
            z = round(za + (zb - za) * i / passos)
            for k in range(largura):
                px, pz = (x, z + k) if ao_longo_x else (x + k, z)
                if not (e.dentro(px, 0, pz) and fora(px, pz)) or not chao_natural(e, px, pz):
                    continue
                if ruido(px, 1, pz, 2.0, SEMENTE + 80 + semente) > 1 - falha:
                    continue
                y = chao_y(px, pz)
                e.por(px, y, pz, CAMINHO)
                e.ar_onde_vazio(px, y + 1, pz, px, y + 2, pz)


def caminho_dos_enterros(e):
    X1 = CEM[2]
    caminho(e, [(X1 + 1, 9), (128, 9), (128, RUA_CRUZADA[0] - 1)], largura=2, semente=1)
    # a rua cruzada do centro continua até a praça do poço
    for z in range(RUA_CRUZADA[0], RUA_CRUZADA[1] + 1):
        for x in range(CENTRO[2] + 1, POCO[0] - 4):
            if e.nome(x, G, z) is None:
                continue
            e.por(x, G, z, "mud" if ruido(x, 8, z, 2.5, SEMENTE + 42) > 0.8 else V.RUA)
            e.ar_onde_vazio(x, G + 1, z, x, G + 3, z)
    # saída oeste da rua cruzada (até a trilha)
    for z in range(RUA_CRUZADA[0], RUA_CRUZADA[1] + 1):
        for x in range(CENTRO[0] - 3, CENTRO[0]):
            if e.nome(x, G, z) is not None:
                e.por(x, G, z, V.RUA)


def cruzeiro(e):
    """Cruz de barras de ferro na bifurcação do caminho dos enterros (a Cruz de Ferro da vila, desenho pendente)."""
    x, z = 131, 76
    e.por(x, G + 1, z, "stone_bricks")
    for y in range(G + 2, G + 5):
        e.por(x, y, z, "iron_bars", **SOLTO)
    e.por(x - 1, G + 3, z, "iron_bars", **SOLTO)
    e.por(x + 1, G + 3, z, "iron_bars", **SOLTO)
    e.por(x, G, z - 1, "gravel")
    e.por(x - 1, G, z, "gravel")
    e.por(x + 1, G, z, "gravel")


def poco_fora(e):
    """Poço seco fora do muro, numa pracinha de pedra. O balde (caldeirão) pendurado na corrente, o telhadinho
    pela metade. No fundo, um baú."""
    cx, cz = POCO
    for x in range(cx - 4, cx + 5):
        for z in range(cz - 4, cz + 5):
            anel = max(abs(x - cx), abs(z - cz))
            if anel == 4:
                if e.chance(0.4) or not chao_natural(e, x, z):
                    continue
                e.por(x, G, z, V.CALCAMENTO)
            elif anel == 3:
                e.por(x, G, z, V.CALCAMENTO)
            elif anel == 2:
                e.por(x, G + 1, z, V.POCO_PEDRA)
                if (x, z) not in ((cx, cz - 2), (cx, cz + 2)):
                    e.por(x, G + 2, z, V.MURETA_MIX, **MURETA)
            else:
                e.ar(x, G, z)  # o fundo é o terreno do mundo
            e.ar_onde_vazio(x, G + 1, z, x, G + 3, z)
    e.por(cx, G, cz, "chest", facing="north", type="single", waterlogged=False)  # BAU_POCO_FORA
    for x in (cx - 3, cx + 3):
        for y in range(G + 1, G + 5):
            e.por(x, y, cz, "spruce_fence", **SOLTO)
    for x in range(cx - 3, cx + 4):
        e.por(x, G + 5, cz, "spruce_log", axis="x")
    for x in range(cx - 3, cx + 1):
        e.por(x, G + 5, cz - 1, "spruce_stairs", facing="south", **DEGRAU)
        e.por(x, G + 5, cz + 1, "spruce_stairs", facing="north", **DEGRAU)
        e.por(x, G + 6, cz, "spruce_slab", **LAJE)
    for y in (G + 3, G + 4):
        e.por(cx, y, cz, "chain", axis="y", waterlogged=False)
    e.por(cx, G + 2, cz, "cauldron")  # o balde
    e.por(cx + 4, G + 1, cz + 3, "spruce_stairs", facing="east", **DEGRAU)  # pedaço do telhado que caiu longe
    e.por(cx - 4, G + 1, cz - 3, "cauldron")  # cocho
    e.por(cx - 4, G + 1, cz - 2, "hay_block", axis="x")
    e.por(cx + 4, G + 1, cz - 4, "spruce_fence", **SOLTO)  # mourão de amarrar
    e.por(cx + 4, G + 2, cz - 4, "chain", axis="y", waterlogged=False)


def pasto(e):
    """Pasto com a cerca caída e o gado morto espalhado (morreu correndo; nada de fileira)."""
    X0, Z0, X1, Z1 = 127, 93, 147, 122
    for x in range(X0 + 1, X1):
        for z in range(Z0 + 1, Z1):
            if not chao_natural(e, x, z):
                continue
            n = ruido(x, 4, z, 3.5, SEMENTE + 90)
            if n < 0.3:
                e.por(x, G, z, "coarse_dirt")
            elif n > 0.82:
                e.por(x, G, z, "mud")
    cerca(e, [(X0, Z0), (X1, Z0), (X1, Z1), (X0, Z1), (X0, Z0)], falha=0.34, caida=0.3, semente=7, altura2=0.0)
    for z in (104, 105):
        e.por(X0, G + 1, z, "spruce_fence_gate", facing="east", in_wall=False, open=True, powered=False)
    paiol(e, 138, 112)
    charco(e, 133, 100, 3, 2)
    for x, z, eixo in ((131, 110, "x"), (138, 115, "z"), (134, 97, "x"), (143, 103, "z"), (145, 118, "x"),
                       (136, 108, "z")):
        if livre(e, x, z):
            e.por(x, G + 1, z, "bone_block", axis=eixo)
    e.por(140, G + 1, 100, "hay_block", axis="x")
    e.por(141, G + 1, 100, "hay_block", axis="z")
    e.por(140, G + 2, 100, "hay_block", axis="y")
    e.por(129, G + 1, 95, "composter", level=0)
    e.por(146, G + 1, 120, "cauldron")
    e.por(133, G + 1, 119, "barrel", facing="east", open=False)
    # a cerca do pasto virada para a vila: trecho inteiro derrubado
    for z in range(108, 114):
        if livre(e, X0, z):
            e.por(X0, G + 1, z, "spruce_trapdoor", facing="west", **TAMPA)


def paiol(e, x0, z0):
    """Paiol aberto (4 estacas, telhado de palha e tábua furado) com feno e um barril embaixo."""
    for x, z in ((x0, z0), (x0 + 4, z0), (x0, z0 + 3), (x0 + 4, z0 + 3)):
        for y in range(G + 1, G + 4):
            e.por(x, y, z, "spruce_fence", **SOLTO)
    telha = Mistura((("spruce_slab", LAJE), 2), (("hay_block", {"axis": "y"}), 3))
    for x in range(x0, x0 + 5):
        for z in range(z0, z0 + 4):
            if ruido(x, 5, z, 1.5, SEMENTE + 97) > 0.72:
                continue
            e.por(x, G + 4, z, telha)
    e.ar_onde_vazio(x0, G + 1, z0, x0 + 4, G + 3, z0 + 3)
    e.por(x0 + 1, G + 1, z0 + 1, "hay_block", axis="x")
    e.por(x0 + 2, G + 1, z0 + 1, "hay_block", axis="z")
    e.por(x0 + 1, G + 2, z0 + 1, "hay_block", axis="y")
    e.por(x0 + 3, G + 1, z0 + 2, "barrel", facing="up", open=False)
    e.por(x0 + 1, G + 1, z0 + 2, "spruce_trapdoor", facing="south", **TAMPA)


def charco(e, cx, cz, rx, rz):
    """Charco que secou: lama rachada, mato morto, um osso."""
    for x in range(cx - rx, cx + rx + 1):
        for z in range(cz - rz, cz + rz + 1):
            d = ((x - cx) / (rx + 0.5)) ** 2 + ((z - cz) / (rz + 0.5)) ** 2
            if d > 1 or not (e.dentro(x, 0, z) and fora(x, z)) or not chao_natural(e, x, z):
                continue
            e.por(x, G, z, "mud" if d < 0.55 else Mistura(("mud", 2), ("coarse_dirt", 2), ("dirt", 1)))
            if (x, G + 1, z) in e.blocos:
                e.tirar(x, G + 1, z)
            if d > 0.5 and e.chance(0.2):
                e.por(x, G + 1, z, "dead_bush")
    if livre(e, cx + 1, cz):
        e.por(cx + 1, G + 1, cz, "bone_block", axis="x")


def extras(e):
    """Coisas de beira de vila, para a coroa não ficar lisa."""
    # marco de pedra da estrada (a entrada das terras da vila): dois pilares, a trave caída, a placa do marco
    c = eixo_estrada(146)
    for x in (c - 3, c + 6):
        for y in range(G + 1, G + 4):
            e.por(x, y, 146, "cobblestone" if y < G + 3 else "mossy_cobblestone")
    e.por(c + 6, G + 3, 145, "spruce_wall_sign", facing="north", waterlogged=False)  # PLACA DO MARCO
    e.por(c - 2, G + 1, 147, "spruce_log", axis="x")
    e.por(c - 1, G + 1, 147, "spruce_log", axis="x")
    # pilha de lenha ao lado do poço
    for x in range(POCO[0] + 5, POCO[0] + 8):
        for z in (POCO[1] + 4, POCO[1] + 5):
            if livre(e, x, z):
                e.por(x, G + 1, z, "spruce_log", axis="x")
                if x != POCO[0] + 7 and e.chance(0.7):
                    e.por(x, G + 2, z, "spruce_log", axis="x")
    # mureta baixa ao lado do caminho dos enterros, perto do cemitério
    for x in range(CEM[2] + 2, 126):
        if ruido(x, 3, 11, 1.8, SEMENTE + 98) < 0.7 and livre(e, x, 11):
            e.por(x, chao_y(x, 11) + 1, 11, "cobblestone_wall" if e.chance(0.7) else "mossy_cobblestone_wall", **MURETA)
    # curral velho a leste: só restos de cerca, feno podre e ossos
    cerca(e, [(131, 42), (141, 42), (141, 52), (131, 52), (131, 42)], falha=0.55, caida=0.4, semente=9)
    for x, z, eixo in ((134, 46, "x"), (138, 49, "z")):
        if livre(e, x, z):
            e.por(x, G + 1, z, "bone_block", axis=eixo)
    if livre(e, 136, 44):
        e.por(136, G + 1, 44, "hay_block", axis="y")
    # tocos e lenha cortada na beira da floresta (alguém cortava lenha aqui)
    for x, z in ((26, 12), (29, 18), (24, 22), (35, 20), (44, 16), (31, 5), (47, 20)):
        if livre(e, x, z):
            e.por(x, G + 1, z, "spruce_log", axis="y")
    for x in (27, 28, 29):
        if livre(e, x, 15):
            e.por(x, G + 1, 15, "spruce_log", axis="x")
    if livre(e, 28, 15, G + 2):
        e.por(28, G + 2, 15, "spruce_log", axis="x")


# ---------------------------------------------------------------------------
# OESTE: trilha para a floresta com marcas de arrasto; a beira da floresta
# ---------------------------------------------------------------------------
TRILHA = [(24, 80), (18, 77), (13, 70), (9, 61), (6, 50), (5, 40), (5, 31), (3, 23), (2, 16)]


def trilha(e):
    for i, ((xa, za), (xb, zb)) in enumerate(zip(TRILHA, TRILHA[1:])):
        passos = max(abs(xb - xa), abs(zb - za), 1)
        ao_longo_x = abs(xb - xa) >= abs(zb - za)
        larga = i < 3
        for k in range(passos + 1):
            x = round(xa + (xb - xa) * k / passos)
            z = round(za + (zb - za) * k / passos)
            # centro pisado e os dois sulcos do arrasto, um de cada lado
            celulas = [(x, z, "dirt_path")]
            for lado in (-1, 1):
                px, pz = (x, z + lado) if ao_longo_x else (x + lado, z)
                celulas.append((px, pz, V.SULCO if larga else ARRASTO))
            for px, pz, b in celulas:
                if not (e.dentro(px, 0, pz) and fora(px, pz)) or not chao_natural(e, px, pz):
                    continue
                if b != "dirt_path" and ruido(px, 1, pz, 1.5, SEMENTE + 81) > 0.8:
                    continue
                e.por(px, chao_y(px, pz), pz, b)
                e.ar_onde_vazio(px, chao_y(px, pz) + 1, pz, px, chao_y(px, pz) + 2, pz)
    # coisas largadas ao longo do arrasto
    e.por(22, G + 1, 78, "spruce_fence", **SOLTO)
    e.por(22, G + 2, 78, "spruce_fence", **SOLTO)
    e.por(21, G + 2, 78, "spruce_wall_sign", facing="west", waterlogged=False)  # PLACA_TRILHA (quebrada)
    e.por(21, G + 1, 76, "spruce_trapdoor", facing="north", **TAMPA)  # a outra metade da placa, no chão
    if livre(e, 16, 74):
        e.por(16, G + 1, 74, "barrel", facing="west", open=True)
    if livre(e, 11, 65):
        e.por(11, G + 1, 65, "spruce_trapdoor", facing="east", **TAMPA)
    if livre(e, 8, 56):
        e.por(8, G + 1, 56, "white_carpet")  # pano rasgado
    if livre(e, 7, 47):
        e.por(7, G + 1, 47, "spruce_fence", **SOLTO)  # estaca quebrada
    if livre(e, 3, 36):
        e.por(3, G + 1, 36, "light_gray_carpet")
    # fogueira apagada de quem esperou, com dois troncos para sentar
    e.por(12, G + 1, 60, "campfire", facing="north", lit=False, signal_fire=False, waterlogged=False)
    e.por(13, G + 1, 61, "spruce_log", axis="x")
    e.por(14, G + 1, 61, "spruce_log", axis="x")
    e.por(11, G + 1, 59, "spruce_log", axis="z")
    e.por(13, G + 1, 59, "barrel", facing="up", open=False)
    # pegadas de cinza também na trilha (para dentro), nos dois primeiros trechos
    passo = 0
    for (xa, za), (xb, zb) in zip(TRILHA[:3], TRILHA[1:4]):
        passos = max(abs(xb - xa), abs(zb - za), 1)
        for k in range(passos):
            passo += 1
            if passo % 2:
                continue
            x = round(xa + (xb - xa) * k / passos)
            z = round(za + (zb - za) * k / passos)
            if livre(e, x, z) and e.nome(x, G, z) == "minecraft:dirt_path":
                e.por(x, G + 1, z, "light_gray_carpet")


def pinheiro(e, x, z, alt):
    base = chao_y(x, z)
    e.por(x, base, z, "podzol")
    for y in range(base + 1, base + 1 + alt):
        e.por(x, y, z, "spruce_log", axis="y")
    camadas = ((alt + 1, 0), (alt, 1), (alt - 1, 2), (alt - 2, 1), (alt - 3, 2), (alt - 4, 1), (alt - 5, 2))
    for dy, r in camadas:
        if dy < 3:
            continue
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                if (dx == 0 and dz == 0 and dy <= alt) or (r == 2 and abs(dx) == 2 and abs(dz) == 2):
                    continue
                px, py, pz = x + dx, base + dy, z + dz
                if e.dentro(px, py, pz) and fora(px, pz) and (px, py, pz) not in e.blocos:
                    e.por(px, py, pz, "spruce_leaves", persistent=True, distance=7, waterlogged=False)


def floresta(e):
    for x, z, alt in ((4, 5, 9), (11, 2, 8), (17, 7, 10), (7, 13, 7), (14, 18, 9), (23, 3, 8), (30, 8, 9),
                      (37, 3, 7), (41, 13, 8), (2, 28, 8), (13, 38, 9), (12, 52, 8), (19, 30, 7), (21, 44, 9),
                      (8, 22, 6), (3, 44, 7)):
        if livre(e, x, z):
            pinheiro(e, x, z, alt)
    # troncos caídos e cogumelos
    for x, z in ((19, 12), (20, 12), (21, 12)):
        if livre(e, x, z):
            e.por(x, G + 1, z, "spruce_log", axis="x")
    for x, z in ((33, 13), (33, 14), (33, 15), (33, 16)):
        if livre(e, x, z):
            e.por(x, G + 1, z, "spruce_log", axis="z")
    for x, z in ((9, 9), (9, 10)):
        if livre(e, x, z):
            e.por(x, G + 1, z, "dark_oak_log", axis="z")


# ---------------------------------------------------------------------------
# Pedras, árvores mortas, sangue, vegetação
# ---------------------------------------------------------------------------
def pedra(e, cx, cz, rx, rz, alt):
    for x in range(cx - rx, cx + rx + 1):
        for z in range(cz - rz, cz + rz + 1):
            d = math.sqrt(((x - cx) / (rx + 0.5)) ** 2 + ((z - cz) / (rz + 0.5)) ** 2)
            if d > 1 or not livre(e, x, z):
                continue
            h = int((1.1 - d) * alt * (0.6 + ruido(x, 2, z, 1.5, SEMENTE + 95)) + 0.5)
            y0 = chao_y(x, z) + 1
            for y in range(y0, y0 + h):
                e.por(x, y, z, PEDRA_SOLTA)
            if h and e.chance(0.3):
                e.por(x, y0 + h, z, "cobblestone_slab", **LAJE)


def pedras(e):
    pedra(e, 136, 8, 3, 2, 3)
    pedra(e, 143, 15, 2, 2, 2)
    pedra(e, 128, 3, 2, 1, 2)
    pedra(e, 146, 44, 2, 2, 2)
    pedra(e, 8, 134, 2, 2, 2)
    pedra(e, 3, 100, 1, 2, 2)
    pedra(e, 40, 20, 2, 1, 2)


def arvore_morta(e, x, z, alt, galhos):
    base = chao_y(x, z)
    e.por(x, base, z, "coarse_dirt")
    for y in range(base + 1, base + 1 + alt):
        e.por(x, y, z, "dark_oak_log", axis="y")
    for gx, gz, gy in galhos:
        for i in (1, 2):
            px, pz = x + gx * i, z + gz * i
            if e.dentro(px, 0, pz) and fora(px, pz):
                e.por(px, base + gy + i - 1, pz, "dark_oak_log", axis="x" if gx else "z")


def arvores_mortas(e):
    for x, z, alt, galhos in ((30, 146, 5, ((1, 0, 3),)),
                              (101, 147, 6, ((-1, 0, 4), (0, -1, 5))),
                              (129, 133, 5, ((0, 1, 3),)),
                              (146, 62, 6, ((-1, 0, 4),)),
                              (140, 32, 5, ((1, 0, 3), (0, 1, 4))),
                              (20, 102, 6, ((0, -1, 4),)),
                              (10, 116, 5, ((1, 0, 3),)),
                              (60, 4, 6, ((1, 0, 4),)),
                              (118, 15, 5, ((0, 1, 3),)),
                              (4, 88, 6, ((1, 0, 4),))):
        if livre(e, x, z):
            arvore_morta(e, x, z, alt, galhos)


def sangue(e):
    # carroça: onde o carroceiro caiu, e o arrasto para dentro da vila
    cx = eixo_estrada(CARROCA_Z + 1) + 2
    m.mancha(e, cx - 4, G + 1, CARROCA_Z - 1, 1, 0.7)
    m.rastro(e, [(cx - 3, CARROCA_Z - 2), (cx, CARROCA_Z - 6), (cx + 1, CENTRO[3] + 1)], G + 1, falha=0.5)
    # trilha: as gotas do arrasto, mais juntas perto da vila
    m.rastro(e, TRILHA[:4], G + 1, falha=0.5)
    m.rastro(e, TRILHA[3:], G + 1, falha=0.78)
    # cemitério: o monte ao lado da cova do baú; poço: a beira
    gx, gz = COVA_DO_BAU
    m.mancha(e, gx + 1, chao_y(gx + 1, gz) + 2, gz, 0, 1.0)
    m.mancha(e, POCO[0] + 3, G + 1, POCO[1] + 3, 1, 0.6)


def vegetacao(e):
    for x in range(TAM[0]):
        for z in range(TAM[2]):
            if not fora(x, z):
                continue
            y = chao_y(x, z)
            n = e.nome(x, y, z)
            if n is None or (x, y + 1, z) in e.blocos:
                continue
            r = e.rng.random()
            floresta = zona_floresta(x, z)
            if n == "minecraft:grass_block":
                if r < (0.22 if floresta else 0.14):
                    e.por(x, y + 1, z, "short_grass")
                elif r < (0.34 if floresta else 0.17):
                    e.por(x, y + 1, z, "fern")
                elif r < (0.35 if floresta else 0.19):
                    e.por(x, y + 1, z, "dead_bush")
            elif n in ("minecraft:podzol", "minecraft:moss_block"):
                if r < 0.07:
                    e.por(x, y + 1, z, "brown_mushroom")
                elif r < 0.2:
                    e.por(x, y + 1, z, "fern")
            elif n in ("minecraft:coarse_dirt", "minecraft:soul_soil") and r < 0.05:
                e.por(x, y + 1, z, "dead_bush")


# ---------------------------------------------------------------------------
# Marcadores (onde o mod põe recompensa) e pistas
# ---------------------------------------------------------------------------
def marcadores(e):
    cx = eixo_estrada(CARROCA_Z + 1) + 2
    cz = cx - 3
    e.marcador(cz, G + 2, CARROCA_Z + 3, "bau_carroca", "Baú de viagem na carga da carroça tombada (estrada sul).")
    e.marcador(cz, G + 3, CARROCA_Z, "disco_carroca", "Jukebox em cima do feno da carroça (o mod põe o disco).")
    gx, gz = COVA_DO_BAU
    e.marcador(gx, 1, gz, "bau_cova", "Baú no fundo de uma cova aberta do cemitério grande (no lugar do corpo).")
    e.marcador(POCO[0], G + 1, POCO[1], "bau_poco_fora", "Baú no fundo do poço seco fora do muro (leste).")
    ex, ez = ESPANTALHO
    e.marcador(ex, G + 1, ez + 1, "bau_espantalho", "Baú enterrado aos pés do espantalho do campo (sudoeste).")
    for mk in e.marcadores:
        x, y, z = mk["alvo"]
        if e.nome(x, y, z) not in ("minecraft:chest", "minecraft:jukebox"):
            raise ValueError(f"marcador {mk['metadata']} sem baú/jukebox embaixo em {mk['alvo']}: {e.nome(x, y, z)}")


def pistas(e):
    cx = eixo_estrada(CARROCA_Z + 1) + 2
    e.pista(cx - 1, G + 1, CARROCA_Z, "carroca", "Estrada sul: carroça tombada de lado, uma roda no ar, bois mortos nos varais. Mesma cena-tipo do Ato 1 (jukebox e disco): se levar objeto de enigma, a Lore descreve antes.")
    e.pista(cx - 4, G + 1, CARROCA_Z - 1, "poca", "Onde o carroceiro caiu: mancha e arrasto PARA DENTRO da vila (ninguém saiu).")
    ex, ez = ESPANTALHO
    e.pista(ex, G + 4, ez, "espantalho", "Campo oeste da estrada: espantalho em pé, de costas para a vila, olhando quem chega. Os de dentro estão caídos ou de cabeça para baixo.")
    e.pista(CEM[2], chao_y(CEM[2], PORTAO_Z[1]) + 4, PORTAO_Z[1], "portao","Cemitério grande (norte, fora do muro): portão de ferro a leste, corrente por cima, folhas da grade caídas.")
    gx, gz = COVA_DO_BAU
    e.pista(gx, G + 1, gz - 1, "lapide", "A lápide cinzelada (risco da Cruz de Ferro, desenho pendente) da cova que tem o baú no lugar do corpo.")
    e.pista(45, G + 1, 8, "cama", "Casa do coveiro sem teto: cama cinza, carriola (barril) e montes de terra na porta.")
    e.pista(131, G + 3, 76, "cruz", "Cruzeiro de barras de ferro na bifurcação do caminho dos enterros (placeholder da Cruz de Ferro).")
    e.pista(POCO[0], G + 2, POCO[1], "balde", "Poço fora do muro: seco, balde (caldeirão) pendurado na corrente, telhadinho pela metade.")
    e.pista(134, G + 1, 110, "ossos", "Pasto leste: cerca caída, gado morto espalhado (morreu correndo), um trecho da cerca derrubado para o lado da vila.")
    e.pista(21, G + 2, 78, "placa", "Bifurcação da trilha: placa quebrada (metade no chão). Texto pelo mod/admin.")
    e.pista(eixo_estrada(146) + 6, G + 3, 145, "placa", "Marco de pedra na entrada da estrada (dois pilares, trave caída): a placa com o nome da vila. Texto pelo mod/admin.")
    e.pista(139, G + 2, 113, "paiol","Pasto leste: paiol aberto com feno. Lugar de Faminto parado à sombra (dia).")
    e.pista(12, G + 1, 60, "fogueira", "Trilha: fogueira apagada e troncos de sentar. Alguém esperou aqui, olhando para a vila.")
    e.pista(9, G + 1, 61, "arrasto", "Trilha para a floresta: dois sulcos e gotas, da floresta PARA a vila. Sai pelo canto noroeste (girar a vila ao colocar para apontar a floresta do Ato 1).")
    for p in e.pistas:
        x, y, z = p["pos"]
        if fora(x, z) and p["tipo"] not in ("poca", "arrasto", "fogueira") and e.vazio(x, y, z):
            print(f"   AVISO: pista sem bloco em {p['pos']} ({p['tipo']})")
