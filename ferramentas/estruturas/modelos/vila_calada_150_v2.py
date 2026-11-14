"""VILA CALADA 150 x 150, versão 2 (pedido do Nikolas em 14/11/2026, depois de jogar a v1).

O que ele achou da v1: verde demais, caminho amarelo, madeira clara, "parece vila comum". A v2 parte da v1
(mesmas casas, mesmas ruas, mesmo arquivo de base) e MATA a vila:
- chão: a grama vira podzol, terra grossa, terra com raiz e lama (manchas, não pontinhos). Mato some.
- ruas: o caminho amarelo vira cascalho, terra grossa, lama e tufo (terra batida escura).
- madeira: abeto claro vira carvalho escuro; um pouco de abeto gasto; mangue (avermelhado) cada vez mais
  perto do campanário. O tronco descascado claro vira tronco escuro.
- casas: telhados com furos (pedaços caídos dentro), janelas tapadas com tábua (alçapão) ou só o buraco,
  portas arrombadas (a folha caída no chão de dentro), mais teias. A casa do Compadre fica como está
  (a única inteira e limpa: o contraste é a pista).
- muro mais alto (3 a 5) com menos brechas; entulho, pedaços de parede em pé, árvores mortas, carroças e
  caixas viradas, ossos e crânios, velas apagadas: menos espaço aberto.
- vermelho só como acento (terracota vermelha, pó de redstone apagado, vela vermelha apagada, mangue),
  cada vez mais denso perto do campanário.
- ADRO DO SINO (lugar para os efeitos de Sangue de 14/11): chão largo de pedra na frente da capela e do
  campanário (em cima do morro e embaixo), com o muro de arrimo do morro e duas paredes altas nas pontas:
  paredes altas para o sangue escorrer, chão largo e liso para ele se espalhar.
- entrada sul: pilares do portão mais altos, viga caída, crânios em estacas na estrada.

NÃO MUDA (teste confere): posição das pistas, os marcadores de recompensa (e os baús/jukebox embaixo),
a capela e o campanário com o atril. Dentro da capela e do campanário só há TROCA DE MATERIAL (mesma forma
de bloco, mesma posição). Os blocos das pistas não mudam nem de material.
"""
import math
import random

import blocos as B
from estrutura import Estrutura, Mistura, ruido
import modulos as m
from modelos import vila_calada as V
from modelos import vila_calada_150 as V1

NOME = "vila_calada_150_v2"
TAM = V1.TAM
BORDA = V1.BORDA
CENTRO = V1.CENTRO
SEMENTE = 1504
G = 0
GERAR_COM_TODAS = False
CORTES = V1.CORTES
FAIXAS = V1.FAIXAS
PARTES = V1.PARTES

B_ = BORDA
# caixas (x0, z0, x1, z1) globais
CAPELA = (41 + B_, 18 + B_, 57 + B_, 34 + B_)
CAMPANARIO = (58 + B_, 29 + B_, 63 + B_, 34 + B_)
PROTEGIDAS = (CAPELA, CAMPANARIO)
COMPADRE = (75 + B_, 71 + B_, 87 + B_, 82 + B_)
FERREIRO = (69 + B_, 43 + B_, 80 + B_, 52 + B_)
PRACA = (V.PRACA[0] + B_, V.PRACA[1] + B_, V.PRACA[2] + B_, V.PRACA[3] + B_)
SINO = (60.5 + B_, 31.5 + B_)  # meio do campanário (o vermelho cresce para cá)
MURO = (4 + B_, 3 + B_, 95 + B_, 88 + B_)  # x0, z0, x1, z1
PLAT = V.PLAT

LAJE, DEGRAU, SOLTO, MURETA = V.LAJE, V.DEGRAU, V.SOLTO, V.MURETA
TAMPA = {"half": "bottom", "open": False, "powered": False, "waterlogged": False}

# chão morto (por manchas): o índice sai do ruído, a mistura dá a variação de bloco a bloco
CHAO_MORTO = [
    Mistura(("coarse_dirt", 4), ("podzol", 3), ("gravel", 1)),
    Mistura(("podzol", 6), ("mud", 1)),
    Mistura(("rooted_dirt", 2), ("podzol", 4), ("coarse_dirt", 2)),
    Mistura(("mud", 5), ("podzol", 2), ("soul_soil", 1)),
    Mistura(("podzol", 4), ("coarse_dirt", 2), ("soul_soil", 2)),
]
RUA_MORTA = Mistura(("gravel", 3), ("tuff", 3), ("coarse_dirt", 3), ("mud", 3), ("cobbled_deepslate", 1))
PEDRA_MURO = Mistura(("cobblestone", 5), ("mossy_cobblestone", 3), ("cobbled_deepslate", 2), ("cracked_stone_bricks", 2),
                     ("tuff", 1))
PEDRA_ALTA = Mistura(("stone_bricks", 4), ("cracked_stone_bricks", 4), ("cobbled_deepslate", 2), ("mossy_stone_bricks", 2),
                     ("cobblestone", 2))
PISO_ADRO = Mistura(("polished_andesite", 4), ("stone_bricks", 4), ("cracked_stone_bricks", 3), ("tuff", 1),
                    ("andesite", 1))
ENTULHO = Mistura(("cobblestone", 4), ("cobbled_deepslate", 2), ("cracked_stone_bricks", 2), ("dark_oak_planks", 2),
                  ("mossy_cobblestone", 2), ("coal_block", 1), ("gravel", 1))
ENTULHO_TOPO = Mistura((("cobblestone_slab", LAJE), 4), (("dark_oak_slab", LAJE), 3),
                       (("deepslate_tile_slab", LAJE), 2), (("stone_brick_slab", LAJE), 1),
                       (("cobblestone_stairs", dict(DEGRAU, facing="north")), 1),
                       (("dark_oak_stairs", dict(DEGRAU, facing="west")), 1),
                       (("dark_oak_trapdoor", TAMPA), 2))

SANGUE_SECO = Mistura(("red_nether_bricks", 3), ("red_terracotta", 1))  # sangue seco: vinho escuro, pouco tijolo claro
TERRA = {"minecraft:grass_block", "minecraft:dirt", "minecraft:coarse_dirt", "minecraft:podzol", "minecraft:moss_block",
         "minecraft:rooted_dirt", "minecraft:mud", "minecraft:soul_soil", "minecraft:gravel", "minecraft:dirt_path",
         "minecraft:tuff", "minecraft:farmland"}
PLANTAS = {"minecraft:short_grass", "minecraft:fern", "minecraft:dead_bush", "minecraft:brown_mushroom",
           "minecraft:wither_rose", "minecraft:light", "minecraft:wheat"}


# ---------------------------------------------------------------------------
def em_caixa(x, z, c, folga=0):
    return c[0] - folga <= x <= c[2] + folga and c[1] - folga <= z <= c[3] + folga


def protegido(x, z):
    return any(em_caixa(x, z, c) for c in PROTEGIDAS)


def vermelho(x, z):
    """0 longe, 1 colado no campanário."""
    d = math.hypot(x - SINO[0], z - SINO[1])
    return max(0.0, min(1.0, 1.0 - (d - 6) / 72.0))


class Obra:
    """Ajudante: guarda o que não pode ser tocado (pistas, marcadores, capela e campanário)."""

    def __init__(self, e):
        self.e = e
        self.fixos = set()
        for p in e.pistas:
            self.fixos.add(tuple(p["pos"]))
        for mk in e.marcadores:
            self.fixos.add(tuple(mk["pos"]))
            self.fixos.add(tuple(mk["alvo"]))
        self.perto_de_pista = set()
        for (x, y, z) in self.fixos:
            for dx in (-1, 0, 1):
                for dz in (-1, 0, 1):
                    self.perto_de_pista.add((x + dx, z + dz))

    def pode(self, x, y, z):
        """Pode pôr/tirar bloco aqui (mudar a forma)."""
        return (self.e.dentro(x, y, z) and not protegido(x, z) and (x, y, z) not in self.fixos
                and (x, z) not in self.perto_de_pista)

    def trocar(self, x, y, z, bloco):
        """Troca de material: mesma posição, mesmas propriedades. Vale também na capela e no campanário."""
        if (x, y, z) in self.fixos or (x, y, z) not in self.e.blocos:
            return
        _, props = self.e.blocos[(x, y, z)]
        if isinstance(bloco, Mistura):
            bloco = bloco.escolher(self.e.rng)
        validas = B.props_validas(bloco.split(":")[-1])
        self.e.por(x, y, z, bloco, **{k: v for k, v in props if k in validas})

    def por(self, x, y, z, bloco, **p):
        if self.pode(x, y, z):
            self.e.por(x, y, z, bloco, **p)
            return True
        return False

    def ar(self, x, y, z):
        if self.pode(x, y, z):
            self.e.ar(x, y, z)

    def livre(self, x, y, z):
        """Vazio (ar, nada, mato ou luz invisível) e com bloco cheio embaixo (terra lavrada vira terra grossa)."""
        n = self.e.nome(x, y, z)
        if not (self.pode(x, y, z) and (n is None or n == "minecraft:air" or n in PLANTAS)):
            return False
        if self.e.nome(x, y - 1, z) in ("minecraft:farmland", "minecraft:dirt_path") and self.pode(x, y - 1, z):
            self.e.por(x, y - 1, z, "coarse_dirt")
        return self.e.cheio(x, y - 1, z)


def chao_y(e, x, z):
    """Topo do chão em (x, z) (0 no plano, 1-2 no morro e na lombada do cemitério)."""
    if em_caixa(x, z, CENTRO):
        return V.chao_y(x - B_, z - B_)
    return V1.chao_y(x, z)


# ---------------------------------------------------------------------------
def construir(semente=SEMENTE):
    base = V1.construir()
    e = Estrutura(NOME, TAM, semente)
    e.blocos = dict(base.blocos)
    e.dados = dict(base.dados)
    e.pistas = [dict(p) for p in base.pistas]
    e.marcadores = [dict(mk) for mk in base.marcadores]
    e.rng = random.Random(semente)
    o = Obra(e)
    ruas = mapa_de_ruas(base)

    chao(o, ruas)
    madeira(o)
    plantas(o)
    adro(o)
    muro(o)
    portao(o)
    casas(o)
    ocupar(o, ruas)
    entrada(o)
    escurecer(o, ruas)
    lajes_do_poco(o)
    sem_musgo(o)
    enterrar_ossos(o)
    sangue(o)
    luzes(o)
    V.conectar(e, filtro=lambda x, y, z: not protegido(x, z))
    pistas(e)
    return e


def mapa_de_ruas(base):
    ruas = set()
    for (x, y, z), (n, _) in base.blocos.items():
        if n in ("minecraft:dirt_path",) and y <= 2:
            ruas.add((x, z))
    for x in range(CENTRO[0], CENTRO[2] + 1):
        for z in range(CENTRO[1], CENTRO[3] + 1):
            if V.em_rua(x - B_, z - B_):
                ruas.add((x, z))
    for x in range(TAM[0]):
        for z in range(CENTRO[3] + 1, TAM[2]):
            if V1.em_estrada(x, z) or V1.em_estrada(x - 1, z) or V1.em_estrada(x + 1, z):
                ruas.add((x, z))  # a estrada sul e uma folga de cada lado
    return ruas


# ---------------------------------------------------------------------------
# Chão e plantas
# ---------------------------------------------------------------------------
def chao(o, ruas):
    e = o.e
    for (x, y, z), (n, _) in list(e.blocos.items()):
        if n == "minecraft:grass_block" or (n == "minecraft:moss_block" and e.chance(0.85)):
            i = int(ruido(x, 0, z, 6.0, SEMENTE + 1) * 4.99)
            o.trocar(x, y, z, CHAO_MORTO[i])
        elif n == "minecraft:mossy_cobblestone" and y <= 2 and e.nome(x, y + 1, z) in (None, "minecraft:air") \
                and e.chance(0.6):
            o.trocar(x, y, z, Mistura(("cobbled_deepslate", 2), ("cobblestone", 2), ("tuff", 1)))  # chão: menos verde
        elif n == "minecraft:dirt_path":
            if (x, z) in ruas or e.chance(0.8):
                o.trocar(x, y, z, "mud" if ruido(x, 8, z, 2.2, SEMENTE + 2) > 0.74 else RUA_MORTA)
            else:
                o.trocar(x, y, z, RUA_MORTA)
    # emendas sem chão dentro da vila (o mundo mostraria grama verde): cobre com chão morto
    for x in range(2, TAM[0] - 2):
        for z in range(2, TAM[2] - 2):
            if any((x, y, z) in e.blocos for y in range(0, 4)):
                continue
            i = int(ruido(x, 0, z, 6.0, SEMENTE + 1) * 4.99)
            o.por(x, 0, z, CHAO_MORTO[i])


def madeira(o):
    """Abeto claro -> carvalho escuro (e um pouco de abeto gasto); mangue perto do campanário."""
    e = o.e
    troca = {
        "spruce_stairs": "dark_oak_stairs", "spruce_slab": "dark_oak_slab", "spruce_fence": "dark_oak_fence",
        "spruce_trapdoor": "dark_oak_trapdoor", "spruce_door": "dark_oak_door",
        "spruce_fence_gate": "dark_oak_fence_gate", "stripped_spruce_log": "dark_oak_log",
    }
    for (x, y, z), (n, _) in list(e.blocos.items()):
        c = n.split(":")[1]
        if em_caixa(x, z, COMPADRE) and c != "stripped_spruce_log":
            continue  # a casa limpa demais continua igual (só o tronco claro escurece)
        if c == "spruce_planks":
            v = vermelho(x, z)
            r = e.rng.random()
            if r < 0.04 + 0.30 * v * v:
                o.trocar(x, y, z, "mangrove_planks")
            elif r < 0.80:
                o.trocar(x, y, z, "dark_oak_planks")
        elif c in troca:
            if c in ("spruce_stairs", "spruce_fence") and e.chance(0.12):
                continue  # um pouco de abeto velho
            o.trocar(x, y, z, troca[c])


def plantas(o):
    e = o.e
    for (x, y, z), (n, p) in list(e.blocos.items()):
        if n in ("minecraft:short_grass", "minecraft:fern"):
            if n == "minecraft:short_grass" and e.chance(0.10):
                o.trocar(x, y, z, "dead_bush")
            elif (x, y, z) not in o.fixos and not protegido(x, z):
                e.ar(x, y, z)  # tirar mato pode até perto de pista
        elif n == "minecraft:wheat":
            r = e.rng.random()
            if r < 0.70:
                o.ar(x, y, z)
            elif r < 0.82:
                if e.nome(x, y - 1, z) == "minecraft:farmland":
                    o.trocar(x, y - 1, z, "coarse_dirt")
                o.por(x, y, z, "dead_bush")
            else:
                o.por(x, y, z, "wheat", age=min(1, int(dict(p).get("age", 0))))
        elif n == "minecraft:dead_bush" and e.chance(0.5):
            o.ar(x, y, z)
        elif n == "minecraft:pumpkin" and e.chance(0.8):
            o.por(x, y, z, "dead_bush")
        elif n == "minecraft:hay_block" and not em_caixa(x, z, COMPADRE):
            # feno amarelo demais: o que não é corpo de espantalho apodrece em caixa virada
            if e.nome(x, y + 1, z) == "minecraft:carved_pumpkin" or e.nome(x, y - 1, z) == "minecraft:carved_pumpkin":
                continue
            if e.chance(0.6):
                o.por(x, y, z, "barrel", facing=e.rng.choice(("east", "west", "north", "south")), open=e.chance(0.4))
        elif n == "minecraft:spruce_leaves":
            # a beira da floresta também morreu pela metade: copa rala, galhos à mostra
            if ruido(x, y, z, 3.0, SEMENTE + 9) > 0.48 or e.chance(0.25):
                o.ar(x, y, z)


# ---------------------------------------------------------------------------
# Adro do Sino: chão largo e paredes altas na frente da capela e do campanário (efeitos de Sangue)
# ---------------------------------------------------------------------------
def adro(o):
    e = o.e
    # em cima do morro, colado na fachada da capela e na parede sul do campanário (21 de altura)
    for x in range(35 + B_, 66 + B_):
        for z in range(35 + B_, 38 + B_):
            if e.nome(x, PLAT, z) in TERRA:
                o.por(x, PLAT, z, PISO_ADRO)
            for y in range(PLAT + 1, PLAT + 4):
                if e.nome(x, y, z) in PLANTAS - {"minecraft:light"}:
                    o.ar(x, y, z)
    # embaixo: o adro baixo, de pedra lisa, entre o morro e a praça
    for x in range(36 + B_, 64 + B_):
        for z in range(38 + B_, 46 + B_):
            y = chao_y(e, x, z)
            if (x, y, z) in e.blocos and e.nome(x, y, z) in TERRA | {"minecraft:cobblestone"}:
                if 47 + B_ <= x <= 52 + B_ and z in (38 + B_, 39 + B_):
                    continue  # escada do morro
                if ruido(x, 1, z, 2.5, SEMENTE + 30) > 0.86 and abs(z - (42 + B_)) > 2:
                    continue  # uma falha aqui e ali
                o.por(x, y, z, PISO_ADRO)
                for yy in range(y + 1, y + 4):
                    if e.nome(x, yy, z) in PLANTAS - {"minecraft:light"}:
                        o.ar(x, yy, z)
    # muro de arrimo do morro (face sul, z 38 local): 2 do morro + parapeito de 1 a 3 = parede alta e lisa
    for x in range(35 + B_, 66 + B_):
        if 46 + B_ <= x <= 53 + B_:
            continue  # escada
        z = 38 + B_
        if e.nome(x, 2, z - 1) is None:
            continue
        n = ruido(x, 4, z, 2.0, SEMENTE + 31)
        topo = PLAT + 1 + int(n * 3.2)
        for y in range(0, topo + 1):
            o.por(x, y, z, PEDRA_ALTA)
    # duas paredes altas e quebradas nas pontas do adro baixo (5 a 9 de altura)
    for x0, rumo in ((35 + B_, 1), (64 + B_, -1)):
        for z in range(39 + B_, 46 + B_):
            if (x0, z) in o.perto_de_pista:
                continue
            n = ruido(x0, 5, z, 1.7, SEMENTE + 32)
            topo = 5 + int(n * 4.5) - (1 if z > 43 + B_ else 0)
            if 41 + B_ <= z <= 42 + B_ and x0 > 50 + B_:
                continue  # passagem da rua leste
            for y in range(1, topo + 1):
                o.por(x0, y, z, PEDRA_ALTA)
            if e.chance(0.5) and e.vazio(x0 + rumo, topo, z):
                o.por(x0 + rumo, topo, z, "cobweb")
        # contraforte e entulho do que caiu
        entulho(o, x0 + rumo * 2, 45 + B_, x0 + rumo * 4, 47 + B_, 2)


# ---------------------------------------------------------------------------
# Muro mais alto, portão
# ---------------------------------------------------------------------------
def muro(o):
    e = o.e
    X0, Z0, X1, Z1 = MURO
    for x, z in m.perimetro(X0, Z0, X1, Z1):
        lx, lz = x - B_, z - B_
        if lz == 88 and 46 <= lx <= 53:
            continue
        if lx in (4, 95) and 53 <= lz <= 58:
            continue
        n = ruido(x, 5, z, 2.0, SEMENTE + 40)
        base = chao_y(e, x, z)
        if n > 0.86:
            # brecha de verdade (rota pelas sombras): só entulho baixo
            if e.chance(0.5):
                o.por(x, base + 1, z, ENTULHO_TOPO)
            continue
        topo = base + 3 + int(ruido(x, 6, z, 3.0, SEMENTE + 41) * 2.6)
        for y in range(base + 1, topo + 1):
            nome = e.nome(x, y, z)
            if nome in (None, "minecraft:air", "minecraft:light") or (nome and "fence" in nome) or nome in PLANTAS:
                o.por(x, y, z, PEDRA_MURO)
        if e.chance(0.35):
            o.por(x, topo + 1, z, "dark_oak_fence", **SOLTO)  # estaca por cima
        elif e.chance(0.3):
            o.por(x, topo + 1, z, ENTULHO_TOPO)
    # trepadeira seca e teia na face de dentro, aqui e ali
    for x, z in m.perimetro(X0 + 1, Z0 + 1, X1 - 1, Z1 - 1):
        if e.chance(0.04) and o.livre(x, 1, z):
            o.por(x, 1, z, "bone_block", axis=e.rng.choice("xz"))
        elif e.chance(0.05) and o.pode(x, 3, z) and e.vazio(x, 3, z):
            o.por(x, 3, z, "cobweb")


def portao(o):
    """Entrada sul: pilares mais altos, a viga do portão caída, corrente pendurada."""
    e = o.e
    z = MURO[3]
    for lx in (45, 54):
        x = lx + B_
        for y in range(1, 7):
            o.por(x, y, z, "chiseled_stone_bricks" if y == 6 else PEDRA_ALTA)
        o.por(x, 7, z, "cobblestone_slab", **LAJE)
    # viga caída, apoiada torta no pilar oeste e no chão
    o.por(45 + B_ + 1, 6, z, "dark_oak_log", axis="x")
    o.por(45 + B_ + 2, 5, z, "dark_oak_log", axis="x")
    o.por(45 + B_ + 3, 4, z, "dark_oak_stairs", facing="west", **DEGRAU)
    for y in (4, 5):
        o.por(45 + B_ + 1, y, z, "chain", axis="y", waterlogged=False)
    for x in range(51 + B_, 55 + B_):
        if o.livre(x, 1, z + 1):
            o.por(x, 1, z + 1, "dark_oak_log", axis="x")
    # muro dos lados do portão: alto (5)
    for x in list(range(38 + B_, 45 + B_)) + list(range(55 + B_, 62 + B_)):
        for y in range(1, 6):
            nome = e.nome(x, y, z)
            if nome in (None, "minecraft:air", "minecraft:light") or (nome and "fence" in nome):
                o.por(x, y, z, PEDRA_MURO)


# ---------------------------------------------------------------------------
# Casas: telhado furado, janela tapada, porta arrombada, teia
# ---------------------------------------------------------------------------
CASAS = [  # caixas locais das casas do centro (x0, z0, x1, z1)
    (8, 33, 15, 39), (19, 33, 26, 39), (22, 44, 29, 51), (9, 60, 16, 67), (10, 72, 17, 79), (68, 10, 75, 17),
    (70, 27, 77, 34), (10, 44, 17, 51), (28, 59, 35, 67), (24, 73, 31, 80), (80, 12, 87, 19), (82, 29, 89, 36),
    (70, 44, 79, 51), (76, 72, 86, 81),
]


def casas(o):
    e = o.e
    for (x, y, z), (n, p) in sorted(e.blocos.items()):
        if protegido(x, z) or em_caixa(x, z, COMPADRE):
            continue
        c = n.split(":")[1]
        # telhado furado (do 5 para cima, o que é telha/tábua)
        if y >= 5 and c in ("dark_oak_stairs", "dark_oak_slab", "dark_oak_planks", "spruce_planks", "mangrove_planks",
                            "spruce_stairs", "spruce_slab"):
            limite = 0.80 if em_caixa(x, z, FERREIRO) else 0.62
            if ruido(x, y, z, 2.2, SEMENTE + 50) > limite:
                o.ar(x, y, z)
                # o pedaço caiu dentro
                yy = y - 1
                while yy > 1 and e.vazio(x, yy, z):
                    yy -= 1
                if e.cheio(x, yy, z) and o.livre(x, yy + 1, z) and e.chance(0.5):
                    if e.chance(0.5):
                        o.por(x, yy + 1, z, "dark_oak_slab", **LAJE)
                    else:
                        o.por(x, yy + 1, z, "dark_oak_stairs", **dict(DEGRAU, facing=e.rng.choice(("north", "east"))))
        # janelas tapadas com tábua
        elif c in ("glass_pane", "iron_bars") and 1 <= y <= 4:
            if em_caixa(x, z, FERREIRO) and e.chance(0.5):
                continue
            ao_longo_x = e.cheio(x - 1, y, z) or e.cheio(x + 1, y, z)
            r = e.rng.random()
            if r < 0.55:
                o.por(x, y, z, "dark_oak_trapdoor", facing=e.rng.choice(("north", "south")) if ao_longo_x
                      else e.rng.choice(("east", "west")), half="bottom", open=True, powered=False, waterlogged=False)
            elif r < 0.75:
                o.por(x, y, z, "dark_oak_planks")
            elif r < 0.92:
                o.ar(x, y, z)
        # portas arrombadas (menos a do ferreiro)
        elif c in ("dark_oak_door", "spruce_door") and dict(p).get("half") == "lower":
            if em_caixa(x, z, FERREIRO):
                continue
            o.ar(x, y, z)
            o.ar(x, y + 1, z)
            f = dict(p).get("facing", "north")
            dx, dz = m.DIRECAO[f]
            for k in (1, -1):
                if o.livre(x - dx * k, y, z - dz * k):
                    o.por(x - dx * k, y, z - dz * k, "dark_oak_trapdoor", facing=f, **TAMPA)
                    break
            if e.vazio(x, y + 2, z):
                o.por(x, y + 2, z, "cobweb")
    # teias e velas apagadas dentro das casas
    for (x0, z0, x1, z1) in CASAS:
        gx0, gz0, gx1, gz1 = x0 + B_, z0 + B_, x1 + B_, z1 + B_
        if em_caixa(gx0, gz0, COMPADRE):
            continue
        teias(o, gx0, 2, gz0, gx1, 5, gz1, 9)
        for _ in range(2):
            x, z = e.rng.randint(gx0 + 1, gx1 - 1), e.rng.randint(gz0 + 1, gz1 - 1)
            if o.livre(x, 1, z):
                m.vela(e, x, 1, z, e.rng.randint(1, 3), acesa=False)
        # teia nos cantos de fora, entre a parede e o chão
        for x, z in ((gx0 - 1, gz0 - 1), (gx1 + 1, gz0 - 1), (gx0 - 1, gz1 + 1), (gx1 + 1, gz1 + 1)):
            if o.livre(x, 1, z) and e.chance(0.5):
                o.por(x, 1, z, "cobweb")


def teias(o, x0, y0, z0, x1, y1, z1, quantidade):
    e = o.e
    cand = []
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            for z in range(z0, z1 + 1):
                if not o.pode(x, y, z) or not e.vazio(x, y, z):
                    continue
                viz = sum(e.cheio(x + dx, y + dy, z + dz)
                          for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, 0, 1), (0, 0, -1)))
                if viz >= 2:
                    cand.append((viz, e.rng.random(), (x, y, z)))
    cand.sort(reverse=True)
    for _, _, (x, y, z) in cand[:quantidade]:
        e.por(x, y, z, "cobweb")


# ---------------------------------------------------------------------------
# Menos espaço aberto: entulho, pedaços de parede, árvores mortas, carroças e caixas viradas, ossos, velas
# ---------------------------------------------------------------------------
def entulho(o, x0, z0, x1, z1, altura):
    e = o.e
    cx, cz = (x0 + x1) / 2, (z0 + z1) / 2
    rx, rz = max(1, (x1 - x0) / 2 + 0.5), max(1, (z1 - z0) / 2 + 0.5)
    for x in range(min(x0, x1), max(x0, x1) + 1):
        for z in range(min(z0, z1), max(z0, z1) + 1):
            d = math.sqrt(((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2)
            h = int((1.15 - d) * altura * (0.5 + ruido(x, 2, z, 1.7, SEMENTE + 60)) + 0.4)
            y = chao_y(e, x, z) + 1
            k = 0
            while k < h and o.livre(x, y, z):
                e.por(x, y, z, ENTULHO)
                y += 1
                k += 1
            if o.livre(x, y, z) and e.chance(0.55):
                e.por(x, y, z, ENTULHO_TOPO)


def aberto(o, ruas, x, z, r):
    """Quadrado de lado 2r+1 em volta de (x, z) sem nada em pé, sem rua e sem praça."""
    e = o.e
    for dx in range(-r, r + 1):
        for dz in range(-r, r + 1):
            px, pz = x + dx, z + dz
            if (px, pz) in ruas or em_caixa(px, pz, PRACA, 0) or not o.pode(px, 1, pz):
                return False
            y = chao_y(e, px, pz)
            if e.vazio(px, y, pz):
                return False
            for yy in range(y + 1, y + 4):
                if not (e.vazio(px, yy, pz) or e.nome(px, yy, pz) in PLANTAS | {"minecraft:wheat"}):
                    return False
    return True


def ocupar(o, ruas):
    e = o.e
    feitos = []
    pontos = []
    for x in range(4, TAM[0] - 4, 4):
        for z in range(4, TAM[2] - 4, 4):
            jx = x + int(ruido(x, 0, z, 1.0, SEMENTE + 70) * 4) - 2
            jz = z + int(ruido(x, 1, z, 1.0, SEMENTE + 71) * 4) - 2
            pontos.append((jx, jz))
    for x, z in pontos:
        dentro = em_caixa(x, z, MURO, -1)
        if not aberto(o, ruas, x, z, 1) or any(abs(x - fx) < 4 and abs(z - fz) < 4 for fx, fz in feitos):
            continue
        if not dentro and e.rng.random() < 0.68:
            continue  # fora do muro, os campos ficam mais vazios (mas não lisos)
        v = vermelho(x, z)
        r = e.rng.random()
        if not dentro:
            r = 0.52 + r * 0.48  # fora: árvore morta, carroça, caixas e ossos (sem entulho de casa)
        if dentro and r < 0.30:
            entulho(o, x - 2, z - 1, x + 2, z + 2, 3)
        elif dentro and r < 0.52:
            parede_em_pe(o, x, z)
        elif r < 0.68:
            arvore_morta(o, x, z)
        elif r < 0.78:
            carroca_virada(o, x, z)
        elif r < 0.88:
            caixas_viradas(o, x, z)
        else:
            ossos(o, x, z, 2 + int(v * 4))
        feitos.append((x, z))
    # nas beiras das ruas: caixas, ossos e velas, mais perto do sino mais coisa
    for (x, z) in sorted(ruas):
        if not o.pode(x, 1, z) or em_caixa(x, z, PRACA):
            continue
        v = vermelho(x, z)
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            px, pz = x + dx, z + dz
            if (px, pz) in ruas:
                continue
            y = chao_y(e, px, pz) + 1
            if not o.livre(px, y, pz):
                continue
            r = e.rng.random()
            if r < 0.025:
                o.por(px, y, pz, "barrel", facing=e.rng.choice(("east", "west", "north", "south")), open=e.chance(0.5))
            elif r < 0.035 + 0.02 * v:
                o.por(px, y, pz, "bone_block", axis=e.rng.choice("xz"))
            elif r < 0.042 + 0.03 * v:
                m.vela(e, px, y, pz, e.rng.randint(1, 3), acesa=False, cor="red" if e.chance(v) else None)
            elif r < 0.047 + 0.01 * v:
                o.por(px, y, pz, "skeleton_skull", rotation=e.rng.randint(0, 15), powered=False)
            elif r < 0.065:
                o.por(px, y, pz, ENTULHO if e.chance(0.5) else ENTULHO_TOPO)
            elif r < 0.075:
                o.por(px, y, pz, "cobweb")
    # praça: chão largo (para o sangue se espalhar) com as coisas só nas beiras
    px0, pz0, px1, pz1 = PRACA
    carroca_virada(o, px0 + 2, pz1 - 3)
    caixas_viradas(o, px1 - 2, pz0 + 2)
    ossos(o, px0 + 3, pz0 + 3, 4)
    ossos(o, px1 - 4, pz1 - 2, 3)
    for x, z in ((px0 + 6, pz0 + 1), (px1 - 1, pz0 + 9), (px0 + 1, pz0 + 12), (px1 - 7, pz1 - 1)):
        if o.livre(x, 1, z):
            m.vela(e, x, 1, z, 3, acesa=False)
    # velas apagadas em volta do poço (alguém tentou)
    for x, z in ((44 + B_, 54 + B_), (54 + B_, 56 + B_), (48 + B_, 50 + B_), (51 + B_, 60 + B_)):
        if o.livre(x, 1, z):
            m.vela(e, x, 1, z, e.rng.randint(2, 4), acesa=False, cor="red" if e.chance(0.4) else None)


def parede_em_pe(o, x, z):
    """Pedaço de parede de casa que sobrou em pé (3 a 6), em L, com a viga queimada."""
    e = o.e
    ao_longo_x = e.chance(0.5)
    comp = e.rng.randint(3, 5)
    for k in range(comp):
        px, pz = (x - 1 + k, z) if ao_longo_x else (x, z - 1 + k)
        topo = 2 + int(ruido(px, 3, pz, 1.5, SEMENTE + 72) * 4.5)
        y0 = chao_y(e, px, pz) + 1
        for y in range(y0, y0 + topo):
            if not o.livre(px, y, pz) and not e.vazio(px, y, pz):
                break
            o.por(px, y, pz, V.PEDRA_QUEIMADA if y == y0 else (V.TABUA_QUEIMADA if e.chance(0.4) else PEDRA_MURO))
    px, pz = (x - 1, z + 1) if ao_longo_x else (x + 1, z - 1)
    if o.livre(px, 1, pz):
        o.por(px, 1, pz, "dark_oak_log", axis="z" if ao_longo_x else "x")
    teias(o, x - 2, 1, z - 2, x + 2, 3, z + 2, 2)


def arvore_morta(o, x, z):
    e = o.e
    base = chao_y(e, x, z)
    alt = e.rng.randint(4, 8)
    if not o.pode(x, base, z):
        return
    for y in range(base + 1, base + 1 + alt):
        if not o.por(x, y, z, "dark_oak_log", axis="y"):
            return
    for _ in range(e.rng.randint(1, 3)):
        gx, gz = e.rng.choice(((1, 0), (-1, 0), (0, 1), (0, -1)))
        gy = base + e.rng.randint(alt // 2 + 1, alt)
        for i in (1, 2):
            px, pz = x + gx * i, z + gz * i
            if o.pode(px, gy + i - 1, pz) and e.vazio(px, gy + i - 1, pz):
                e.por(px, gy + i - 1, pz, "dark_oak_log", axis="x" if gx else "z")
        # corrente pendurada na ponta do galho (o que ela segurava já caiu)
        px, pz, py = x + gx * 2, z + gz * 2, gy
        if e.chance(0.45) and e.nome(px, py + 1, pz) == "minecraft:dark_oak_log":
            for k in range(e.rng.randint(1, 3)):
                if not (o.pode(px, py - k, pz) and e.vazio(px, py - k, pz)) or py - k <= base + 1:
                    break
                e.por(px, py - k, pz, "chain", axis="y", waterlogged=False)
    if e.chance(0.4) and o.livre(x + 1, base + 1, z):
        o.por(x + 1, base + 1, z, "dark_oak_log", axis="x")


def carroca_virada(o, x, z):
    """Carroça de lado: o fundo em pé (alçapões abertos), uma roda no chão, a outra no ar, a carga esparramada."""
    e = o.e
    y = chao_y(e, x, z) + 1
    for k in range(3):
        o.por(x, y + 1, z + k, "dark_oak_trapdoor", facing="east", half="bottom", open=True, powered=False,
              waterlogged=False)
        if o.livre(x, y, z + k):
            o.por(x, y, z + k, "dark_oak_slab", **LAJE)
    if o.livre(x + 1, y, z):
        o.por(x + 1, y, z, "dark_oak_trapdoor", facing="north", **TAMPA)
    if o.livre(x - 1, y, z + 2):
        o.por(x - 1, y, z + 2, "dark_oak_fence", **SOLTO)
        o.por(x - 1, y + 1, z + 2, "dark_oak_trapdoor", facing="west", half="bottom", open=True, powered=False,
              waterlogged=False)
    for dx, dz in ((2, 1), (2, 2), (3, 0)):
        if not o.livre(x + dx, y, z + dz):
            continue
        r = e.rng.random()
        if r < 0.4:
            o.por(x + dx, y, z + dz, "barrel", facing="east", open=True)
        elif r < 0.6:
            o.por(x + dx, y, z + dz, "bone_block", axis="x")
        elif r < 0.8:
            o.por(x + dx, y, z + dz, "dark_oak_slab", **LAJE)
        else:
            o.por(x + dx, y, z + dz, "cobweb")


def caixas_viradas(o, x, z):
    e = o.e
    for dx, dz in ((0, 0), (1, 0), (0, 1), (-1, 1), (1, 2)):
        px, pz = x + dx, z + dz
        y = chao_y(e, px, pz) + 1
        if not o.livre(px, y, pz) or e.chance(0.25):
            continue
        if e.chance(0.6):
            o.por(px, y, pz, "barrel", facing=e.rng.choice(("east", "west", "north", "south", "down")), open=e.chance(0.5))
            if e.chance(0.3) and o.livre(px, y + 1, pz):
                o.por(px, y + 1, pz, "barrel", facing=e.rng.choice(("east", "south")), open=False)
        else:
            o.por(px, y, pz, "dark_oak_trapdoor", facing=e.rng.choice(("north", "east")), **TAMPA)


def ossos(o, x, z, n):
    """Resto: osso afundado no chão (só o topo aparece), crânios e pano."""
    e = o.e
    for _ in range(n):
        px, pz = x + e.rng.randint(-2, 2), z + e.rng.randint(-2, 2)
        y = chao_y(e, px, pz) + 1
        if not o.livre(px, y, pz):
            continue
        r = e.rng.random()
        if r < 0.45 and e.nome(px, y - 1, pz) in TERRA | {"minecraft:cobbled_deepslate", "minecraft:tuff"}:
            o.por(px, y - 1, pz, "bone_block", axis=e.rng.choice("xz"))
        elif r < 0.85:
            o.por(px, y, pz, "skeleton_skull", rotation=e.rng.randint(0, 15), powered=False)
        else:
            o.por(px, y, pz, "white_carpet")  # pano


# ---------------------------------------------------------------------------
# Entrada sul (arredores): crânios em estacas na estrada, cercas mais quebradas
# ---------------------------------------------------------------------------
def entrada(o):
    e = o.e
    for z in range(CENTRO[3] + 3, TAM[2] - 2, 6):
        c = V1.eixo_estrada(z)
        for x in (c - 2, c + 5):
            if not o.livre(x, 1, z) or e.chance(0.3):
                continue
            alt = e.rng.randint(2, 3)
            for y in range(1, 1 + alt):
                o.por(x, y, z, "dark_oak_fence", **SOLTO)
            o.por(x, 1 + alt, z, "skeleton_skull", rotation=8, powered=False)  # olhando para quem chega (sul)
    # cercas: mais quebradas
    for (x, y, z), (n, _) in list(e.blocos.items()):
        if n in ("minecraft:dark_oak_fence", "minecraft:spruce_fence") and not em_caixa(x, z, CENTRO) and y <= 2:
            if e.nome(x, y + 1, z) == "minecraft:skeleton_skull" or e.nome(x, y - 1, z) == "minecraft:dark_oak_fence":
                continue
            if e.chance(0.18):
                o.ar(x, y, z)
            elif e.chance(0.08):
                o.por(x, y, z, "dark_oak_trapdoor", facing=e.rng.choice(("north", "east")), **TAMPA)


# ---------------------------------------------------------------------------
# Sangue: acento vermelho, cada vez mais perto do campanário
# ---------------------------------------------------------------------------
def sangue(o):
    e = o.e
    # chão: manchas secas (terracota vermelha) e pó apagado, em manchas de ruído
    for (x, y, z), (n, _) in sorted(e.blocos.items()):
        if y > 3 or n not in TERRA | {"minecraft:polished_andesite", "minecraft:stone_bricks",
                                       "minecraft:cracked_stone_bricks", "minecraft:cobblestone",
                                       "minecraft:andesite", "minecraft:mossy_cobblestone"}:
            continue
        if not e.vazio(x, y + 1, z) and e.nome(x, y + 1, z) not in PLANTAS:
            continue
        v = vermelho(x, z)
        if v <= 0.05:
            continue
        mancha = ruido(x, 9, z, 3.0, SEMENTE + 80)
        limiar = 1.0 - 0.42 * v ** 2.2
        if mancha > limiar:
            if mancha > limiar + 0.07 and e.cheio(x, y, z) and n != "minecraft:farmland":
                o.trocar(x, y, z, SANGUE_SECO)
            if e.vazio(x, y + 1, z) or e.nome(x, y + 1, z) in PLANTAS:
                if e.cheio(x, y, z) and e.chance(0.45) and o.pode(x, y + 1, z):
                    e.por(x, y + 1, z, "redstone_wire", power=0, north="none", south="none", east="none", west="none")
    # paredes perto do sino: o que escorreu e secou (pedra -> terracota vermelha, só embaixo)
    for (x, y, z), (n, _) in sorted(e.blocos.items()):
        if not (1 <= y <= 9) or n not in ("minecraft:cobblestone", "minecraft:stone_bricks", "minecraft:cracked_stone_bricks",
                                          "minecraft:cobbled_deepslate", "minecraft:mossy_cobblestone"):
            continue
        v = vermelho(x, z)
        if v < 0.55:
            continue
        if ruido(x, y * 0.5, z, 1.4, SEMENTE + 81) > 1.0 - 0.18 * v ** 3 and e.chance(0.7):
            o.trocar(x, y, z, SANGUE_SECO)
    # o adro: a poça grande na frente do campanário e o arrasto que desce a escada do morro
    for cx, cz, r, d in ((60 + B_, 36 + B_, 2, 0.5), (49 + B_, 41 + B_, 2, 0.4), (57 + B_, 43 + B_, 1, 0.5)):
        for x in range(cx - r, cx + r + 1):
            for z in range(cz - r, cz + r + 1):
                yy = chao_y(e, x, z)
                if e.cheio(x, yy, z) and e.chance(d) and o.pode(x, yy + 1, z) and e.vazio(x, yy + 1, z):
                    e.por(x, yy + 1, z, "redstone_wire", power=0, north="none", south="none", east="none", west="none")
                if e.chance(0.15):
                    o.trocar(x, yy, z, SANGUE_SECO)
    # velas vermelhas apagadas no pé do campanário
    for x, z in ((58 + B_, 36 + B_), (63 + B_, 35 + B_), (61 + B_, 37 + B_), (56 + B_, 35 + B_)):
        if o.livre(x, PLAT + 1, z):
            m.vela(e, x, PLAT + 1, z, e.rng.randint(1, 3), acesa=False, cor="red")
    # capela e campanário: só troca de material (chão manchado perto do arrasto que já existe)
    for (x, y, z), (n, _) in sorted(e.blocos.items()):
        if not protegido(x, z) or y != PLAT:
            continue
        if e.nome(x, y + 1, z) == "minecraft:redstone_wire" and e.chance(0.5):
            o.trocar(x, y, z, SANGUE_SECO)


# ---------------------------------------------------------------------------
# Rodada de 14/11 (dia limpo demais): pedra do chão escura e suja, sem musgo verde, ossos como resto
# ---------------------------------------------------------------------------
PEDRA_SUJA = [
    Mistura(("cobbled_deepslate", 5), ("tuff", 2), ("blackstone", 1)),
    Mistura(("tuff", 3), ("cobbled_deepslate", 2), ("cracked_deepslate_tiles", 2)),
    Mistura(("mud", 4), ("coarse_dirt", 2), ("cobbled_deepslate", 2)),
    Mistura(("blackstone", 2), ("cracked_deepslate_tiles", 4), ("deepslate_tiles", 1), ("tuff", 1)),
    Mistura(("cobbled_deepslate", 3), ("cracked_stone_bricks", 1), ("gravel", 1), ("mud", 1)),
]
PEDRA_CLARA = {"minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:stone_bricks",
               "minecraft:polished_andesite", "minecraft:andesite", "minecraft:mossy_stone_bricks",
               "minecraft:cracked_stone_bricks", "minecraft:gravel", "minecraft:diorite"}
CAMINHO_CLARO = Mistura(("cobblestone", 3), ("gravel", 3), ("tuff", 2), ("cracked_stone_bricks", 1))


def escurecer(o, ruas):
    """Chão de pedra clara (praça, adro, calçadas): pedra escura, tufo, lama e terra em manchas. A rua principal
    continua um pouco mais clara para o caminho ainda se ler."""
    e = o.e
    for (x, y, z), (n, _) in sorted(e.blocos.items()):
        if n not in PEDRA_CLARA or y > PLAT or not e.cheio(x, y, z):
            continue
        acima = e.nome(x, y + 1, z)
        if not (acima is None or acima == "minecraft:air" or acima in PLANTAS or acima.endswith("carpet")
                or acima.endswith("candle") or acima == "minecraft:redstone_wire"):
            continue
        if y != chao_y(e, x, z) and not (em_caixa(x, z, PRACA) and y == 1):
            continue  # só o chão (e o pódio do poço)
        principal = 48 + B_ <= x <= 51 + B_ and z >= 63  # a rua principal
        n_r = ruido(x, 7, z, 6.0, SEMENTE + 90) * 0.8 + ruido(x, 8, z, 2.0, SEMENTE + 91) * 0.2
        if principal and (x, z) in ruas:
            if n_r < 0.55:
                o.trocar(x, y, z, CAMINHO_CLARO)
            continue
        o.trocar(x, y, z, PEDRA_SUJA[min(4, int(n_r * 5))])


def lajes_do_poco(o):
    e = o.e
    for (x, y, z), (n, _) in list(e.blocos.items()):
        if n == "minecraft:stone_brick_slab" and em_caixa(x, z, PRACA):
            o.trocar(x, y, z, Mistura(("deepslate_tile_slab", 3), ("cobblestone_slab", 2), ("stone_brick_slab", 1)))
        elif n in ("minecraft:stone_bricks", "minecraft:cracked_stone_bricks") and em_caixa(x, z, PRACA) and y >= 1:
            if e.chance(0.5):
                o.trocar(x, y, z, Mistura(("cracked_stone_bricks", 2), ("cobbled_deepslate", 2), ("tuff", 1)))


def sem_musgo(o):
    """Nada de verde vivo: musgo vira pedra/terra morta (mesma forma onde for preciso)."""
    e = o.e
    troca = {
        "minecraft:mossy_cobblestone": Mistura(("cobblestone", 3), ("cobbled_deepslate", 2), ("tuff", 1)),
        "minecraft:mossy_stone_bricks": Mistura(("cracked_stone_bricks", 3), ("stone_bricks", 1)),
        "minecraft:mossy_cobblestone_wall": "cobblestone_wall",
        "minecraft:moss_block": Mistura(("podzol", 2), ("rooted_dirt", 1)),
        "minecraft:moss_carpet": Mistura(("gray_carpet", 2), ("brown_carpet", 1)),  # pó e folha morta
    }
    for (x, y, z), (n, _) in list(e.blocos.items()):
        if n in troca:
            o.trocar(x, y, z, troca[n])
        elif n == "minecraft:vine" and (x, y, z) not in o.fixos and not protegido(x, z):
            e.ar(x, y, z)


def enterrar_ossos(o):
    """Osso solto em cima do chão parece bloco branco: afunda no chão (só o topo aparece), junta em pilha
    meio enterrada e ganha crânio por perto."""
    e = o.e
    for (x, y, z), (n, p) in sorted(e.blocos.items()):
        if n != "minecraft:bone_block" or y < 1 or not o.pode(x, y, z) or not o.pode(x, y - 1, z):
            continue
        if e.nome(x, y - 1, z) not in TERRA and not (e.nome(x, y - 1, z) or "").endswith("_dirt"):
            continue  # pilha ou osso em cima de outra coisa: fica
        if e.nome(x, y + 1, z) == "minecraft:bone_block":
            continue
        e.ar(x, y, z)
        e.por(x, y - 1, z, "bone_block", **dict(p))
        r = e.rng.random()
        if r < 0.25 and o.livre(x, y, z):
            e.por(x, y, z, "skeleton_skull", rotation=e.rng.randint(0, 15), powered=False)
        elif r < 0.45:
            # pilha meio enterrada: mais um osso ao lado, afundado, e uma laje de terra por cima de parte
            dx, dz = e.rng.choice(((1, 0), (-1, 0), (0, 1), (0, -1)))
            if o.pode(x + dx, y - 1, z + dz) and e.nome(x + dx, y - 1, z + dz) in TERRA and o.livre(x + dx, y, z + dz):
                e.por(x + dx, y - 1, z + dz, "bone_block", axis=e.rng.choice("xz"))
                if e.chance(0.5):
                    e.por(x + dx, y, z + dz, "dark_oak_trapdoor", facing="north", **TAMPA)


def luzes(o):
    """Repõe a luz invisível (contra monstro comum) onde o entulho ou a parede ocupou o lugar."""
    e = o.e
    for lx in range(6, 94):
        for lz in range(5, 88):
            if lx % 4 != 2 or lz % 4 != 1:
                continue
            x, z = lx + B_, lz + B_
            if not o.pode(x, 1, z):
                continue
            y0 = chao_y(e, x, z) + 1
            if any(e.nome(x, y, z) == "minecraft:light" for y in range(y0, y0 + 8)):
                continue
            for y in range(y0, y0 + 8):
                if e.vazio(x, y, z) and e.cheio(x, y - 1, z) and o.pode(x, y, z):
                    e.por(x, y, z, "light", level=5, waterlogged=False)
                    break


def pistas(e):
    e.pista(60 + B_, PLAT + 1, 36 + B_, "efeito_sangue",
            "ADRO DO SINO (v2): chão largo de pedra colado na parede sul do campanário (21 de altura) e na fachada da "
            "capela. Melhor lugar para o sangue escorrer pela parede e se espalhar no chão (shader de 14/11).")
    e.pista(49 + B_, 1, 42 + B_, "efeito_sangue",
            "Adro baixo (v2): chão de pedra entre o morro e a praça, com o muro de arrimo (4 a 6) e duas paredes altas "
            "(5 a 9) nas pontas. Segundo lugar para o sangue escorrer e se espalhar.")
