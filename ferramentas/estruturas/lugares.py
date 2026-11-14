"""Peças comuns dos cinco lugares do Ato 1 (Moinho, Açougueiro, Cartógrafo, Torre, Fazenda).

Mesma paleta morta da Vila Calada: madeira escura gasta, terra escura, pedra suja, vermelho só de Sangue.
Convenções (valem para os cinco):
- e.y_chao = camada do chão do mundo (o bloco de cima do terreno). Quem anda pisa em y_chao + 1.
  Abaixo dela só porão e cripta. Ao colocar: origem y = (y do bloco de cima do terreno) - y_chao.
- Marcadores (structure_block modo DATA, o mod lê e não coloca). metadata "tipo:nome":
  bau:/atril:/disco: ficam UM BLOCO ACIMA do alvo (baú, atril, jukebox); os outros ficam NO PONTO (alvo = ele mesmo).
"""
import nbt_io as N
from estrutura import Mistura, ruido
import modulos as m

LAJE = {"type": "bottom", "waterlogged": False}
LAJE_TOPO = {"type": "top", "waterlogged": False}
DEGRAU = {"half": "bottom", "shape": "straight", "waterlogged": False}
SEM_LADOS = dict(north=False, south=False, east=False, west=False, waterlogged=False)

CHAO_MORTO = Mistura(("coarse_dirt", 7), ("podzol", 5), ("rooted_dirt", 3), ("dirt", 3), ("mud", 1),
                     ("packed_mud", 1), ("gravel", 1))
TERRA_ESCURA = Mistura(("coarse_dirt", 5), ("podzol", 3), ("dirt", 2), ("rooted_dirt", 2))
PEDRA_SUJA = Mistura(("cobblestone", 6), ("mossy_cobblestone", 2), ("cobbled_deepslate", 2), ("andesite", 1))
TABUA_GASTA = Mistura(("spruce_planks", 9), ("dark_oak_planks", 5))
TABUA_ESCURA = Mistura(("dark_oak_planks", 9), ("spruce_planks", 3))
TIJOLO_SUJO = Mistura(("stone_bricks", 6), ("cracked_stone_bricks", 4), ("mossy_stone_bricks", 1))

ANTES_DO_ALVO = ("bau", "atril", "disco", "barril")


# ---------------------------------------------------------------------------
# Marcadores
# ---------------------------------------------------------------------------
def ponto(e, x, y, z, nome, nota=""):
    """Marcador no ponto (spawn, som, evento, rota, mapa). O bloco tem de estar vazio (ar)."""
    e.marcador(x, y, z, nome, nota, alvo="aqui")


def bau(e, x, y, z, facing, nome, nota="", bloco="chest"):
    """Baú (ou barril) vazio + marcador um bloco acima: o mod enche com o que a história pede."""
    if bloco == "chest":
        e.por(x, y, z, "chest", facing=facing, type="single", waterlogged=False)
    else:
        e.por(x, y, z, "barrel", facing=facing, open=False)
    e.marcador(x, y + 1, z, "bau:" + nome, nota)


def atril(e, x, y, z, facing, nome, nota=""):
    e.por(x, y, z, "lectern", facing=facing, has_book=False, powered=False)
    e.marcador(x, y + 1, z, "atril:" + nome, nota)


# ---------------------------------------------------------------------------
# Placa com texto e estandarte com desenho (dados de bloco)
# ---------------------------------------------------------------------------
def _linhas(linhas):
    linhas = list(linhas)[:4] + [""] * (4 - len(linhas))
    return N.Lista(N.STRING, ['"' + t.replace('"', '\\"') + '"' for t in linhas])


def placa(e, x, y, z, linhas, parede=None, rotation=0, verso=None):
    """Placa de abeto com texto (palavras, nunca número). parede = facing para placa de parede."""
    if parede:
        e.por(x, y, z, "spruce_wall_sign", facing=parede, waterlogged=False)
    else:
        e.por(x, y, z, "spruce_sign", rotation=rotation, waterlogged=False)
    lado = {"messages": _linhas(linhas), "color": "black", "has_glowing_text": N.Byte(0)}
    tras = {"messages": _linhas(verso or []), "color": "black", "has_glowing_text": N.Byte(0)}
    e.com_dados(x, y, z, {"id": "minecraft:sign", "front_text": lado, "back_text": tras, "is_waxed": N.Byte(1)})


def estandarte_cruz(e, x, y, z, facing=None, rotation=None):
    """Estandarte cinza com a Cruz de Ferro PROVISÓRIA (cruz preta e borda): o desenho final é do Modelador."""
    if facing:
        e.por(x, y, z, "light_gray_wall_banner", facing=facing)
    else:
        e.por(x, y, z, "light_gray_banner", rotation=rotation or 0)
    pad = N.Lista(N.COMPOUND, [
        {"pattern": "minecraft:straight_cross", "color": "black"},
        {"pattern": "minecraft:circle", "color": "light_gray"},
        {"pattern": "minecraft:triangles_bottom", "color": "gray"},
    ])
    e.com_dados(x, y, z, {"id": "minecraft:banner", "patterns": pad})


# ---------------------------------------------------------------------------
# Telhado de duas águas em qualquer eixo, normal (1 por 1) ou íngreme (2 por 1)
# ---------------------------------------------------------------------------
def telhado(e, a0, a1, b0, b1, y_base, eixo, degrau, preench, cume, inc=1, buracos=0.0, protegido=None):
    """Cumeeira ao longo de 'eixo' ("x" ou "z"). a0..a1 = largura (as águas caem para os dois lados),
    b0..b1 = comprimento ao longo da cumeeira. Devolve altura(a) = primeiro y do telhado na coluna a.
    """
    def P(a, y, b, bloco, **pr):
        if eixo == "z":
            e.por(a, y, b, bloco, **pr)
        else:
            e.por(b, y, a, bloco, **pr)

    def T(a, y, b):
        if eixo == "z":
            e.tirar(a, y, b)
        else:
            e.tirar(b, y, a)

    sobe_a, sobe_b = ("east", "west") if eixo == "z" else ("south", "north")
    for b in range(min(b0, b1), max(b0, b1) + 1):
        k = 0
        while a0 + k <= a1 - k:
            xa, xb = a0 + k, a1 - k
            y = y_base + inc * k
            if xa == xb:
                if inc == 2:
                    P(xa, y, b, preench)
                    P(xa, y + 1, b, cume)
                else:
                    P(xa, y, b, cume)
                    if k > 0:
                        P(xa, y - 1, b, preench)
            else:
                if inc == 2:
                    P(xa, y, b, preench)
                    P(xb, y, b, preench)
                    P(xa, y + 1, b, degrau, facing=sobe_a, **DEGRAU)
                    P(xb, y + 1, b, degrau, facing=sobe_b, **DEGRAU)
                    if xb == xa + 1:
                        P(xa, y + 2, b, cume)
                        P(xb, y + 2, b, cume)
                else:
                    P(xa, y, b, degrau, facing=sobe_a, **DEGRAU)
                    P(xb, y, b, degrau, facing=sobe_b, **DEGRAU)
                    if xa + 1 <= xb - 1:
                        P(xa + 1, y, b, preench)
                        P(xb - 1, y, b, preench)
            k += 1
    if buracos > 0:
        meio = (a1 - a0) // 2
        for b in range(min(b0, b1), max(b0, b1) + 1):
            if protegido and protegido(b):
                continue
            for k in range(1, meio - 1):
                for xa, d in ((a0 + k, 1), (a1 - k, -1)):
                    if e.chance(buracos):
                        y = y_base + inc * k
                        for dy in range(inc + (1 if inc == 2 else 0)):
                            T(xa, y + dy, b)
                        if inc == 1:
                            T(xa + d, y, b)

    def altura(a):
        return y_base + inc * min(a - a0, a1 - a)

    return altura


def empena(e, eixo, b, a0, a1, y_base, inc, material, de=0):
    """Parede triangular sob o telhado(...) no plano b. material: bloco, Mistura ou fn(a, y)."""
    for a in range(a0 + 1, a1):
        topo = y_base + inc * min(a - a0, a1 - a) - 1
        for y in range(y_base + de, topo + 1):
            bl = material(a, y) if callable(material) else material
            if bl is None:
                continue
            if eixo == "z":
                e.por(a, y, b, bl)
            else:
                e.por(b, y, a, bl)


# ---------------------------------------------------------------------------
# Chão, colar de terra, árvores mortas, mato seco
# ---------------------------------------------------------------------------
def chao_morto(e, x0, z0, x1, z1, y, borda_irregular=3, so_vazio=True, material=CHAO_MORTO):
    """Troca o topo do terreno por terra escura. A borda some aos poucos (não fica um quadrado)."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            d = min(x - x0, x1 - x, z - z0, z1 - z)
            if d < borda_irregular and ruido(x, 0, z, 3.0, e.semente + 91) > (d + 1) / (borda_irregular + 1):
                continue
            if so_vazio and not e.vazio(x, y, z):
                continue
            if (x, y, z) in e.blocos:
                continue
            e.por(x, y, z, material)


def colar(e, x0, z0, x1, z1, y, largura=2):
    """Colar de terra escura em volta do pé de uma construção (encaixa em terreno torto)."""
    for x in range(x0 - largura, x1 + largura + 1):
        for z in range(z0 - largura, z1 + largura + 1):
            if x0 <= x <= x1 and z0 <= z <= z1:
                continue
            if e.dentro(x, y, z) and (x, y, z) not in e.blocos:
                e.por(x, y, z, TERRA_ESCURA)


def mato_seco(e, x0, z0, x1, z1, y, chance=0.08):
    """Arbusto morto e capim ralo em cima do chão (nada de verde vivo: capim só de vez em quando)."""
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if not e.dentro(x, y + 1, z) or not e.vazio(x, y + 1, z) or (x, y + 1, z) in e.blocos:
                continue
            n = e.nome(x, y, z)
            if n not in ("minecraft:coarse_dirt", "minecraft:podzol", "minecraft:rooted_dirt", "minecraft:dirt"):
                continue
            if e.chance(chance):
                r = e.rng.random()
                e.por(x, y + 1, z, "dead_bush" if r < 0.7 else ("short_grass" if r < 0.85 else "brown_mushroom"))


def arvore_morta(e, x, y, z, altura, galhos=3, tronco="dark_oak_log", folhas=0):
    """Árvore seca: tronco torto, galhos curtos, quase sem folha."""
    cx, cz = x, z
    for i in range(altura):
        if not e.dentro(cx, y + i, cz):
            break
        e.por(cx, y + i, cz, tronco, axis="y")
        if i > 2 and e.chance(0.18):
            dx, dz = [(1, 0), (-1, 0), (0, 1), (0, -1)][int(e.rng.random() * 4)]
            if e.dentro(cx + dx, y + i, cz + dz):
                e.por(cx + dx, y + i, cz + dz, tronco, axis="x" if dx else "z")
                cx, cz = cx + dx, cz + dz
    topo = y + altura - 1
    for _ in range(galhos):
        dx, dz = [(1, 0), (-1, 0), (0, 1), (0, -1)][int(e.rng.random() * 4)]
        yy = topo - int(e.rng.random() * max(1, altura // 2))
        comp = 1 + int(e.rng.random() * 3)
        for k in range(1, comp + 1):
            px, pz = cx + dx * k, cz + dz * k
            py = yy + (k // 2)
            if e.dentro(px, py, pz) and e.vazio(px, py, pz):
                e.por(px, py, pz, tronco, axis="x" if dx else "z")
    for _ in range(folhas):
        px = cx + int(e.rng.random() * 5) - 2
        pz = cz + int(e.rng.random() * 5) - 2
        py = topo - 1 + int(e.rng.random() * 3)
        if e.dentro(px, py, pz) and e.vazio(px, py, pz):
            e.por(px, py, pz, "spruce_leaves", persistent=True, distance=7, waterlogged=False)


def espantalho(e, x, y, z, olha, cabeca="carved_pumpkin"):
    """Espantalho de bloco: poste, tronco de feno, braços de cerca, cabeça de abóbora entalhada."""
    e.por(x, y, z, "spruce_fence", **SEM_LADOS)
    e.por(x, y + 1, z, "hay_block", axis="y")
    e.por(x, y + 2, z, cabeca, facing=olha)
    if olha in ("north", "south"):
        e.por(x - 1, y + 1, z, "spruce_fence", **dict(SEM_LADOS, east=True))
        e.por(x + 1, y + 1, z, "spruce_fence", **dict(SEM_LADOS, west=True))
    else:
        e.por(x, y + 1, z - 1, "spruce_fence", **dict(SEM_LADOS, south=True))
        e.por(x, y + 1, z + 1, "spruce_fence", **dict(SEM_LADOS, north=True))


def cerca(e, pontos, y, material="spruce_fence", falha=0.0):
    """Cerca ligando cantos (x, z) em linhas retas; postes se ligam aos vizinhos."""
    pos = []
    for (xa, za), (xb, zb) in zip(pontos, pontos[1:]):
        passos = max(abs(xb - xa), abs(zb - za))
        for i in range(passos + 1):
            x = xa + (xb - xa) * i // max(passos, 1)
            z = za + (zb - za) * i // max(passos, 1)
            if (x, z) not in pos:
                pos.append((x, z))
    vivos = [p for p in pos if not e.chance(falha)]
    s = set(vivos)
    for x, z in vivos:
        e.por(x, y, z, material, north=(x, z - 1) in s, south=(x, z + 1) in s, east=(x + 1, z) in s,
              west=(x - 1, z) in s, waterlogged=False)
    return vivos


def sem_spawn(e, x0, y0, z0, x1, y1, z1, passo=5, nivel=3):
    """Luz invisível fraca (nível 3) em grade no chão dos cômodos: monstro comum não nasce e o escuro fica."""
    for x in range(x0, x1 + 1, 1):
        for z in range(z0, z1 + 1, 1):
            if (x - x0) % passo or (z - z0) % passo:
                continue
            for y in range(y0, y1 + 1):
                if (x, y, z) in e.blocos and e.nome(x, y, z) == "minecraft:air" and e.cheio(x, y - 1, z):
                    e.por(x, y, z, "light", level=nivel, waterlogged=False)
                    break


def poste_lanterna(e, x, y, z, altura=3, acesa=True):
    for dy in range(altura):
        e.por(x, y + dy, z, "spruce_fence", **SEM_LADOS)
    if acesa:
        e.por(x, y + altura, z, "lantern", hanging=False, waterlogged=False)


def viga(e, x0, y, z0, x1, z1, material="stripped_dark_oak_log"):
    eixo = "x" if x1 != x0 else "z"
    for x in range(min(x0, x1), max(x0, x1) + 1):
        for z in range(min(z0, z1), max(z0, z1) + 1):
            e.por(x, y, z, material, axis=eixo)
