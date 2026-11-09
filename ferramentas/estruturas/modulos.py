"""Peças reutilizáveis para montar estruturas. Todas recebem a Estrutura como primeiro argumento.

Direções: "north" = -z, "south" = +z, "east" = +x, "west" = -x.
Escadas (stairs): o lado alto fica para o lado do "facing". Banco virado para o sul = facing "north".
"""
from estrutura import Mistura, ruido

DIRECAO = {"north": (0, -1), "south": (0, 1), "east": (1, 0), "west": (-1, 0)}
OPOSTO = {"north": "south", "south": "north", "east": "west", "west": "east"}


def _intervalo(a, b):
    return range(min(a, b), max(a, b) + 1)


# ---------------------------------------------------------------------------
# Paredes, pisos, telhado
# ---------------------------------------------------------------------------
def perimetro(x0, z0, x1, z1):
    """Posições (x, z) da borda de um retângulo, sem repetir."""
    for x in _intervalo(x0, x1):
        for z in _intervalo(z0, z1):
            if x in (x0, x1) or z in (z0, z1):
                yield x, z


def paredes(est, x0, z0, x1, z1, y0, y1, material):
    """material: bloco, Mistura ou função (x, y, z, canto) -> bloco."""
    for x, z in perimetro(x0, z0, x1, z1):
        canto = x in (x0, x1) and z in (z0, z1)
        for y in _intervalo(y0, y1):
            b = material(x, y, z, canto) if callable(material) else material
            if b is not None:
                est.por(x, y, z, b)


def piso(est, x0, z0, x1, z1, y, material):
    for x in _intervalo(x0, x1):
        for z in _intervalo(z0, z1):
            b = material(x, y, z) if callable(material) else material
            if b is not None:
                est.por(x, y, z, b)


def telhado_duas_aguas(est, x0, x1, z0, z1, y_base, degrau, preench, cume, buracos=0.0, protegido=None):
    """Telhado de duas águas com a cumeeira ao longo de z (as águas caem para oeste e leste).

    Cada água: degrau (stairs) por fora e uma fileira de 'preench' por baixo (telhado grosso, sem fresta).
    Devolve uma função altura(x) = primeiro y do telhado naquela coluna (para limpar o ar por dentro).
    buracos: chance de faltar um pedaço (degrau + forro) — telhado de ruína.
    protegido(z): se devolver True, aquela fileira nunca ganha buraco.
    """
    largura = x1 - x0
    for z in _intervalo(z0, z1):
        k = 0
        while x0 + k <= x1 - k:
            y = y_base + k
            xa, xb = x0 + k, x1 - k
            if xa == xb:
                est.por(xa, y, z, cume)
                if k > 0 and est.dentro(xa, y - 1, z):
                    est.por(xa, y - 1, z, preench)
            else:
                est.por(xa, y, z, degrau, facing="east", half="bottom", shape="straight", waterlogged=False)
                est.por(xb, y, z, degrau, facing="west", half="bottom", shape="straight", waterlogged=False)
                if xa + 1 <= xb - 1:
                    est.por(xa + 1, y, z, preench)
                    est.por(xb - 1, y, z, preench)
            k += 1
    if buracos > 0:
        for z in _intervalo(z0, z1):
            if protegido and protegido(z):
                continue
            for k in range(1, largura // 2 - 1):
                for xa, dx in ((x0 + k, 1), (x1 - k, -1)):
                    if est.chance(buracos):
                        y = y_base + k
                        est.tirar(xa, y, z)
                        est.tirar(xa + dx, y, z)

    def altura(x):
        return y_base + min(x - x0, x1 - x)

    return altura


def oitao(est, z, x0, x1, y_base, material, de_k=1):
    """Parede triangular (empena) no plano z, por baixo de um telhado_duas_aguas(x0..x1, y_base)."""
    k = de_k
    while x0 + k + 1 <= x1 - k - 1:
        y = y_base + k
        for x in range(x0 + k + 1, x1 - k):
            b = material(x, y, z, False) if callable(material) else material
            est.por(x, y, z, b)
        k += 1


# ---------------------------------------------------------------------------
# Aberturas
# ---------------------------------------------------------------------------
def janela(est, x, z, y0, y1, ao_longo, quebrada=0.3, grade=0.0, vidro="glass_pane"):
    """Janela de vidro numa parede. ao_longo = "x" (parede norte/sul) ou "z" (parede leste/oeste)."""
    for y in _intervalo(y0, y1):
        if est.chance(quebrada):
            est.ar(x, y, z)
            continue
        bloco = "iron_bars" if est.chance(grade) else vidro
        if ao_longo == "x":
            est.por(x, y, z, bloco, east=True, west=True, north=False, south=False, waterlogged=False)
        else:
            est.por(x, y, z, bloco, north=True, south=True, east=False, west=False, waterlogged=False)


def porta(est, x, y, z, facing, hinge="left", aberta=False, material="spruce_door"):
    for meia, dy in (("lower", 0), ("upper", 1)):
        est.por(x, y + dy, z, material, facing=facing, half=meia, hinge=hinge, open=aberta, powered=False)


def vao(est, x0, y0, z0, x1, y1, z1):
    """Abertura sem porta (passagem)."""
    est.encher(x0, y0, z0, x1, y1, z1, "air")


# ---------------------------------------------------------------------------
# Mobília
# ---------------------------------------------------------------------------
def cama(est, x, y, z, facing, cor="white"):
    """(x, y, z) é o pé da cama; a cabeceira fica um bloco para o lado do facing."""
    dx, dz = DIRECAO[facing]
    est.por(x, y, z, f"{cor}_bed", facing=facing, part="foot", occupied=False)
    est.por(x + dx, y, z + dz, f"{cor}_bed", facing=facing, part="head", occupied=False)


def cadeira(est, x, y, z, olhando_para, material="spruce_stairs"):
    """Cadeira de degrau: quem senta olha para 'olhando_para' (o encosto fica do lado oposto)."""
    est.por(x, y, z, material, facing=OPOSTO[olhando_para], half="bottom", shape="straight", waterlogged=False)


def mesa(est, x0, z0, x1, z1, y, material="spruce_trapdoor"):
    """Tampo de alçapão fechado no alto do bloco (mesa baixa, padrão de construção)."""
    for x in _intervalo(x0, x1):
        for z in _intervalo(z0, z1):
            est.por(x, y, z, material, facing="north", half="top", open=False, powered=False, waterlogged=False)


def banco(est, x0, x1, y, z, olhando_para="south", falha=0.0, material="spruce_stairs"):
    """Banco de igreja ao longo de x."""
    for x in _intervalo(x0, x1):
        if est.chance(falha):
            continue
        cadeira(est, x, y, z, olhando_para, material)


def lanterna_pendurada(est, x, y, z, comprimento_corrente=0, com_lanterna=True):
    """Lanterna em (x, y, z) com corrente subindo 'comprimento_corrente' blocos acima dela."""
    if com_lanterna:
        est.por(x, y, z, "lantern", hanging=True, waterlogged=False)
    for dy in range(1, comprimento_corrente + 1):
        est.por(x, y + dy, z, "chain", axis="y", waterlogged=False)


def corrente(est, x, z, y0, y1):
    for y in _intervalo(y0, y1):
        est.por(x, y, z, "chain", axis="y", waterlogged=False)


def vela(est, x, y, z, quantas=1, acesa=False, cor=None):
    est.por(x, y, z, f"{cor}_candle" if cor else "candle", candles=quantas, lit=acesa, waterlogged=False)


# ---------------------------------------------------------------------------
# Desgaste, ruína e natureza
# ---------------------------------------------------------------------------
def envelhecer(est, x0, y0, z0, x1, y1, z1, trocas, intensidade=1.0, escala=2.5):
    """Troca blocos por versões gastas, em manchas (ruído) e não em pontinhos soltos.

    trocas: {"stone_bricks": Mistura(("cracked_stone_bricks", 2), ("mossy_stone_bricks", 1)), ...}
    A chance cresce perto do chão (umidade).
    """
    for x in _intervalo(x0, x1):
        for y in _intervalo(y0, y1):
            for z in _intervalo(z0, z1):
                b = est.blocos.get((x, y, z))
                if not b:
                    continue
                curto = b[0].split(":")[1]
                if curto not in trocas:
                    continue
                perto_do_chao = max(0.0, 1.0 - (y - y0) / 4.0) * 0.25
                if ruido(x, y, z, escala, est.semente) + perto_do_chao > 1.0 - 0.35 * intensidade:
                    # propriedades vêm só do bloco novo (as do antigo podem não valer para ele)
                    est.blocos[(x, y, z)] = est._resolver(trocas[curto], {})


def buracos(est, x0, y0, z0, x1, y1, z1, chance, so=None):
    """Tira blocos (vira ar) para fazer frestas e tábuas faltando. so: conjunto de nomes curtos."""
    for x in _intervalo(x0, x1):
        for y in _intervalo(y0, y1):
            for z in _intervalo(z0, z1):
                n = est.nome(x, y, z)
                if not n or n == "minecraft:air":
                    continue
                if so and n.split(":")[1] not in so:
                    continue
                if est.chance(chance):
                    est.ar(x, y, z)


def _encostado(est, x, y, z):
    return any(est.cheio(x + dx, y + dy, z + dz)
               for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, 0, 1), (0, 0, -1)))


def teias(est, x0, y0, z0, x1, y1, z1, quantidade):
    """Teias em cantos altos: só em lugar vazio encostado em parede ou teto."""
    candidatos = []
    for x in _intervalo(x0, x1):
        for y in _intervalo(y0, y1):
            for z in _intervalo(z0, z1):
                if est.vazio(x, y, z) and _encostado(est, x, y, z):
                    vizinhos = sum(est.cheio(x + dx, y + dy, z + dz)
                                   for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, 0, 1), (0, 0, -1)))
                    candidatos.append((vizinhos, est.rng.random(), (x, y, z)))
    candidatos.sort(reverse=True)  # cantos (mais vizinhos) primeiro
    for _, _, (x, y, z) in candidatos[:quantidade]:
        est.por(x, y, z, "cobweb")


def chao_com_musgo(est, x0, z0, x1, z1, y, chance, escala=3.0, material="moss_carpet"):
    """Tapete de musgo em manchas sobre piso cheio (y = altura do tapete)."""
    for x in _intervalo(x0, x1):
        for z in _intervalo(z0, z1):
            if est.vazio(x, y, z) and est.cheio(x, y - 1, z):
                if ruido(x, y, z, escala, est.semente + 7) > 1.0 - chance:
                    est.por(x, y, z, material)


def trepadeira(est, x, z, y_topo, lado_da_parede, comprimento):
    """Cipó descendo de y_topo, grudado na parede que fica no 'lado_da_parede' (ex.: "south")."""
    for y in range(y_topo, y_topo - comprimento, -1):
        if not est.dentro(x, y, z) or not est.vazio(x, y, z):
            break
        est.por(x, y, z, "vine", **{d: (d == lado_da_parede) for d in ("north", "south", "east", "west")}, up=False)


# ---------------------------------------------------------------------------
# Sangue (acento vermelho). Pó de redstone apagado, só em cima de bloco cheio.
# ---------------------------------------------------------------------------
def _pingo(est, x, y, z):
    if est.dentro(x, y, z) and est.vazio(x, y, z) and est.cheio(x, y - 1, z):
        est.por(x, y, z, "redstone_wire", power=0, north="none", south="none", east="none", west="none")
        return True
    return False


def mancha(est, cx, y, cz, raio, densidade=0.6):
    """Poça irregular: mais cheia no centro, falhando para a borda."""
    for x in range(cx - raio, cx + raio + 1):
        for z in range(cz - raio, cz + raio + 1):
            d = ((x - cx) ** 2 + (z - cz) ** 2) ** 0.5
            if d > raio + 0.3:
                continue
            p = densidade * (1.0 - 0.6 * d / max(raio, 1))
            if est.chance(p):
                _pingo(est, x, y, z)


def rastro(est, pontos, y, falha=0.25):
    """Rastro de arrasto ligando pontos (x, z) em linha, com falhas (pingos espaçados)."""
    for (xa, za), (xb, zb) in zip(pontos, pontos[1:]):
        passos = max(abs(xb - xa), abs(zb - za), 1)
        for i in range(passos + 1):
            x = round(xa + (xb - xa) * i / passos)
            z = round(za + (zb - za) * i / passos)
            if not est.chance(falha):
                _pingo(est, x, y, z)
