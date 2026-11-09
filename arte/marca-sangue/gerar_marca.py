"""Arte da Marca de Sangue (mesa Modelador) -- texturas por estagio + previas.

Feito do zero por nos (nada de terceiros). Roda com:  bash ferramentas/marca_sangue.sh

Como funciona:
- Monta o corpo do jogador (Steve 4px e Alex 3px) em 3D com o MESMO mapeamento de UV do Minecraft
  (ModelPart.Cube), entao cada pixel do molde 64x64 sabe onde fica no corpo e quem sao os vizinhos,
  inclusive dobrando a quina de uma face para outra e passando do braco para o tronco pelo ombro.
- As veias nascem de UM ponto (a ferida no antebraco direito, onde o Sangue entrou) e crescem como uma
  arvore pelo corpo (caminho mais curto com "terreno" de ruido suave = veia torta, organica).
- Estagios iguais aos do codigo (PadraoVeias): 1 braco direito; 2 + peito e costas; 3 + braco esquerdo e
  pescoco; 4 + pernas. Cada estagio contem tudo do anterior (so cresce).
- Saida: arte/marca-sangue/estagio_N.png (Steve) e estagio_N_alex.png (Alex), estagio_4_brilho*.png
  (so o que brilha, para uma futura camada que brilha no escuro), skins de teste e previas.
"""
import heapq
import math
import os
import random
import sys

from imagem_simples import Imagem  # Python puro, sem Pillow


class Image:  # nomes curtos usados abaixo
    NEAREST = 0

    @staticmethod
    def new(_modo, tam, cor=(0, 0, 0, 0)):
        return Imagem(tam[0], tam[1], cor)


class ImageDraw:
    @staticmethod
    def Draw(img):
        return _Pincel(img)


class _Pincel:
    def __init__(self, img):
        self.img = img

    def polygon(self, pts, fill):
        self.img.poligono(pts, fill)

    def text(self, pos, txt, font=2, fill=(255, 255, 255)):
        self.img.texto((int(pos[0]), int(pos[1])), txt, fill, font)

RAIZ = sys.argv[1] if len(sys.argv) > 1 else os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
DIR_ARTE = os.path.join(RAIZ, "arte", "marca-sangue")
DIR_SKINS = os.path.join(DIR_ARTE, "skins-teste")
DIR_PREVIA = os.path.join(RAIZ, "arte", "previas", "marca-sangue")

# ---------------------------------------------------------------- paleta (arte/guia-de-estilo.md, Sangue)
SOMBRA = (0x4A, 0x0A, 0x12)   # veia grossa, borda de rachadura
BASE = (0x8E, 0x16, 0x24)     # veia comum
BRILHO = (0xD9, 0x39, 0x3B)   # borda viva da veia (le em skin escura), rachadura
ACENTO = (0xFF, 0x8A, 0x6B)   # carne viva / brilho umido: SO na ferida e no fundo das rachaduras
LINHA = (0x2A, 0x19, 0x13)    # linha da costura (neutro couro escuro)


# ---------------------------------------------------------------- corpo 3D com UV do Minecraft
def caixa(nome, x0, y0, z0, w, h, d, u, v):
    """Faces de um cubo do jeito do ModelPart.Cube (y para baixo, frente = -z)."""
    x1, y1, z1 = x0 + w, y0 + h, z0 + d
    P = [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0),
         (x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)]
    u1, u2, u3, u4, u5 = u + d, u + d + w, u + d + w + w, u + d + w + d, u + d + w + d + w
    v0, v1, v2 = v, v + d, v + d + h
    faces = [
        ("cima", (5, 4, 0, 1), (u1, v0, u2, v1), (0, -1, 0)),
        ("baixo", (2, 3, 7, 6), (u2, v1, u3, v0), (0, 1, 0)),
        ("dir", (0, 4, 7, 3), (u, v1, u1, v2), (-1, 0, 0)),
        ("frente", (1, 0, 3, 2), (u1, v1, u2, v2), (0, 0, -1)),
        ("esq", (5, 1, 2, 6), (u2, v1, u4, v2), (1, 0, 0)),
        ("costas", (4, 5, 6, 7), (u4, v1, u5, v2), (0, 0, 1)),
    ]
    out = []
    for fnome, idx, (umin, vmin, umax, vmax), normal in faces:
        A, B, C = P[idx[1]], P[idx[0]], P[idx[2]]  # A=(umin,vmin) B=(umax,vmin) C=(umin,vmax)
        out.append(dict(parte=nome, face=fnome, A=A, B=B, C=C, uv=(umin, vmin, umax, vmax), normal=normal))
    return out


def corpo(alex):
    bw = 3 if alex else 4
    dy = 0.5 if alex else 0.0
    partes = [
        caixa("cabeca", -4, -8, -4, 8, 8, 8, 0, 0),
        caixa("tronco", -4, 0, -2, 8, 12, 4, 16, 16),
        caixa("braco_dir", -4 - bw, dy, -2, bw, 12, 4, 40, 16),
        caixa("braco_esq", 4, dy, -2, bw, 12, 4, 32, 48),
        caixa("perna_dir", -4, 12, -2, 4, 12, 4, 0, 16),
        caixa("perna_esq", 0, 12, -2, 4, 12, 4, 16, 48),
    ]
    return [f for p in partes for f in p]


def ponto(face, s, t):
    A, B, C = face["A"], face["B"], face["C"]
    return tuple(A[i] + s * (B[i] - A[i]) + t * (C[i] - A[i]) for i in range(3))


def texels(face):
    umin, vmin, umax, vmax = face["uv"]
    for tu in range(int(min(umin, umax)), int(max(umin, umax))):
        for tv in range(int(min(vmin, vmax)), int(max(vmin, vmax))):
            yield tu, tv


def st(face, tu, tv):
    umin, vmin, umax, vmax = face["uv"]
    return (tu - umin) / (umax - umin), (tv - vmin) / (vmax - vmin)


def mapa(alex):
    """texel -> (face, centro 3D) para o corpo todo (so a camada de baixo)."""
    m = {}
    for f in corpo(alex):
        du = 1 / (f["uv"][2] - f["uv"][0])
        dv = 1 / (f["uv"][3] - f["uv"][1])
        for tu, tv in texels(f):
            s, t = st(f, tu, tv)
            m[(tu, tv)] = (f, ponto(f, s + du / 2, t + dv / 2))
    return m


JUNTAS = {frozenset(("braco_dir", "tronco")): lambda y: y <= 1.6,      # ombro
          frozenset(("braco_esq", "tronco")): lambda y: y <= 1.6,
          frozenset(("cabeca", "tronco")): lambda y: -0.6 <= y <= 0.6,  # pescoco
          frozenset(("perna_dir", "tronco")): lambda y: 11.4 <= y <= 12.6,  # quadril
          frozenset(("perna_esq", "tronco")): lambda y: 11.4 <= y <= 12.6}


def vizinhos(m):
    chaves = list(m)
    viz = {k: [] for k in chaves}
    for i, a in enumerate(chaves):
        fa, pa = m[a]
        for b in chaves[i + 1:]:
            fb, pb = m[b]
            d = math.dist(pa, pb)
            if d > 1.2:
                continue
            if fa["parte"] == fb["parte"]:
                if d < 0.01 or d > 1.01:
                    continue
            else:
                regra = JUNTAS.get(frozenset((fa["parte"], fb["parte"])))
                if regra is None or d < 0.3 or not (regra(pa[1]) and regra(pb[1])):
                    continue
            viz[a].append((b, d))
            viz[b].append((a, d))
    return viz


# ---------------------------------------------------------------- regioes por estagio (iguais ao codigo)
def regioes(alex):
    bw = 3 if alex else 4
    larg = 2 * bw + 8
    return [
        ("braco_dir", 1, [(40, 16, 40 + larg, 32)]),
        ("tronco", 2, [(16, 16, 40, 32)]),
        ("braco_esq", 3, [(32, 48, 32 + larg, 64)]),
        ("pescoco", 3, [(0, 12, 32, 16), (16, 0, 24, 8)]),  # lados de baixo da cabeca + face de baixo (ponte)
        ("perna_dir", 4, [(0, 16, 16, 32)]),
        ("perna_esq", 4, [(16, 48, 32, 64)]),
    ]


def regiao_de(alex, tu, tv):
    for nome, desde, rets in regioes(alex):
        for x0, y0, x1, y1 in rets:
            if x0 <= tu < x1 and y0 <= tv < y1:
                return nome, desde
    return None, 99


# ---------------------------------------------------------------- ruido suave 3D (veia torta)
class Ruido:
    def __init__(self, semente):
        r = random.Random(semente)
        self.g = {}
        self.r = r

    def _v(self, i, j, k):
        if (i, j, k) not in self.g:
            self.g[(i, j, k)] = random.Random(hash((i, j, k, 7919))).random()
        return self.g[(i, j, k)]

    def __call__(self, x, y, z, esc=3.0):
        x, y, z = x / esc, y / esc, z / esc
        i, j, k = math.floor(x), math.floor(y), math.floor(z)
        fx, fy, fz = x - i, y - j, z - k
        sm = lambda t: t * t * (3 - 2 * t)
        fx, fy, fz = sm(fx), sm(fy), sm(fz)
        tot = 0.0
        for di in (0, 1):
            for dj in (0, 1):
                for dk in (0, 1):
                    w = (fx if di else 1 - fx) * (fy if dj else 1 - fy) * (fz if dk else 1 - fz)
                    tot += w * self._v(i + di, j + dj, k + dk)
        return tot


# ---------------------------------------------------------------- crescimento das veias
ORIGEM = (45, 28)   # frente do antebraco direito (Steve e Alex: face da frente comeca em u=44)

# quantas pontas novas por regiao: (estagio em que a regiao abre, pontas no estagio em que abre, pontas depois)
PONTAS = {"braco_dir": (7, 2), "tronco": (9, 3), "braco_esq": (5, 2), "pescoco": (4, 1),
          "perna_dir": (4, 0), "perna_esq": (4, 0)}


def crescer(alex):
    m = mapa(alex)
    viz = vizinhos(m)
    ruido = Ruido(11)
    custo = {}
    for k, (f, p) in m.items():
        n = ruido(*p)
        custo[k] = 1.0 + 4.0 * n * n
    rnd = random.Random(2026)
    pai = {ORIGEM: None}
    ordem_estagio = {ORIGEM: 1}
    estagios = []
    for s in range(1, 5):
        permitido = {k for k in m if regiao_de(alex, *k)[1] <= s}
        # Dijkstra a partir da arvore inteira
        dist = {k: 0.0 for k in pai}
        prev = {}
        fila = [(0.0, k) for k in pai]
        heapq.heapify(fila)
        while fila:
            d, a = heapq.heappop(fila)
            if d > dist.get(a, 1e9):
                continue
            for b, dd in viz[a]:
                if b not in permitido:
                    continue
                nd = d + dd * (custo[a] + custo[b]) / 2
                if nd < dist.get(b, 1e9):
                    dist[b] = nd
                    prev[b] = a
                    heapq.heappush(fila, (nd, b))
        # escolhe pontas: longe da arvore, em faces que aparecem (nao em cima/baixo)
        for nome, desde, _ in regioes(alex):
            if desde > s:
                continue
            n = PONTAS[nome][0] if desde == s else PONTAS[nome][1]
            cand = [k for k in permitido if regiao_de(alex, *k)[0] == nome and k not in pai
                    and m[k][0]["face"] not in ("cima", "baixo") and k in dist]
            if nome == "pescoco":
                cand = [k for k in cand if m[k][0]["face"] != "baixo"]
            cand.sort(key=lambda k: (-dist[k], k))
            # metade mais longe, sorteada: veias de comprimentos variados (nao so listras ate a ponta)
            cand = cand[: max(len(cand) * 2 // 5, n * 3, 1)]
            rnd.shuffle(cand)
            escolhidas = []
            for k in cand:
                if len(escolhidas) >= n:
                    break
                if all(math.dist(m[k][1], m[e][1]) > 3.0 for e in escolhidas):
                    escolhidas.append(k)
            for k in escolhidas:
                c = k
                caminho = []
                while c not in pai:
                    caminho.append(c)
                    c = prev[c]
                prox = c
                for c in reversed(caminho):
                    pai[c] = prox
                    ordem_estagio[c] = s
                    prox = c
        estagios.append(dict(pai))
    return m, viz, estagios


def fluxo(pai):
    """Quantas pontas passam por cada pixel (veia tronco = muito fluxo = grossa)."""
    filhos = {}
    for k, p in pai.items():
        if p is not None:
            filhos.setdefault(p, []).append(k)
    f = {}

    def conta(k):
        pilha = [(k, False)]
        while pilha:
            n, feito = pilha.pop()
            if feito:
                fs = filhos.get(n, [])
                f[n] = 1 if not fs else sum(f[c] for c in fs)
            else:
                pilha.append((n, True))
                for c in filhos.get(n, []):
                    pilha.append((c, False))
    conta(ORIGEM)
    return f, filhos


def profundidade_ponta(pai, filhos):
    """Distancia (em pixels) ate a ponta mais longe abaixo: pontas finas somem aos poucos."""
    alt = {}
    pilha = [(ORIGEM, False)]
    while pilha:
        n, feito = pilha.pop()
        if feito:
            fs = filhos.get(n, [])
            alt[n] = 0 if not fs else 1 + max(alt[c] for c in fs)
        else:
            pilha.append((n, True))
            for c in filhos.get(n, []):
                pilha.append((c, False))
    return alt


# ---------------------------------------------------------------- pintura no molde 64x64
def desenhar(alex, m, viz, pai, s):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    px = img.load()
    brilho = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    pb = brilho.load()
    f, filhos = fluxo(pai)
    alt = profundidade_ponta(pai, filhos)

    def por(k, cor, a, so_vazio=False):
        x, y = k
        if so_vazio and px[x, y][3] > 0:
            return
        px[x, y] = cor + (a,)

    def vizinho_mesma_face(k, du, dv):
        q = (k[0] + du, k[1] + dv)
        if q in m and m[q][0] is m[k][0]:
            return q
        return None

    grossura_tronco = {1: 99, 2: 99, 3: 6, 4: 5}[s]
    # 1) veias
    for k in pai:
        fl = f.get(k, 1)
        if alt.get(k, 0) == 0:
            por(k, BASE, 150)                      # ponta: some aos poucos
        elif alt.get(k, 0) == 1:
            por(k, BASE, 200)
        elif fl >= 3:
            por(k, SOMBRA, 245)                    # tronco
        else:
            por(k, BASE, 235)
    # 2) borda viva (de cima/esquerda, onde bate a luz): faz a veia ler em skin escura
    for k in list(pai):
        fl = f.get(k, 1)
        if fl >= 2 and alt.get(k, 0) >= 2:
            for du, dv in ((-1, 0), (0, -1)):
                q = vizinho_mesma_face(k, du, dv)
                if q and q not in pai:
                    por(q, BRILHO, 120 if fl < 4 else 165, so_vazio=True)
                    break
    # 3) tronco engrossa com o estagio
    for k in list(pai):
        if f.get(k, 1) >= grossura_tronco and k != ORIGEM:
            for du, dv in ((1, 0), (0, 1)):
                q = vizinho_mesma_face(k, du, dv)
                if q and q not in pai:
                    por(q, SOMBRA, 200)
                    break
    # 4) pele avermelhada em volta da ferida (inchaco)
    ox, oy = ORIGEM
    raio = {1: 1, 2: 2, 3: 2, 4: 3}[s]
    for k, (face, p) in m.items():
        if face is not m[ORIGEM][0]:
            continue
        d = abs(k[0] - ox) + abs(k[1] - oy)
        if 0 < d <= raio + 1 and px[k][3] == 0:
            por(k, BRILHO, 70 if d <= raio else 40)

    # 5) ferida / cicatriz / costura no ponto de entrada (frente do antebraco direito, u 44..47/48)
    fx0 = 44
    if s == 1:
        por((ox, oy), SOMBRA, 255)
        por((ox, oy - 1), ACENTO, 230)            # brilho umido
        por((ox, oy + 1), SOMBRA, 230)
    else:
        # corte vertical que cresce: 3 px no 2, 5 px no 3 e 4
        meio = 2 if s == 2 else 3
        for dy in range(-meio + 1, meio):
            por((ox, oy + dy), SOMBRA, 255)
        por((ox, oy - 1 if s == 2 else oy), ACENTO, 235)  # carne viva no meio do corte
        if s >= 3:
            # costura: pontos de linha cruzando o corte, alternando os lados
            for i, dy in enumerate(range(-meio + 1, meio, 2)):
                lado = -1 if i % 2 == 0 else 1
                por((ox + lado, oy + dy), LINHA, 255)
                por((ox - lado, oy + dy + 1), LINHA, 210)
            por((ox + 2, oy - meio), BRILHO, 150)  # repuxado nas pontas do corte
            por((ox - 1, oy + meio), BRILHO, 150)

    # 6) estagio 4: rachaduras com brilho fraco nos troncos + pele repuxada + costura no ombro
    if s == 4:
        tronco = [k for k in pai if f.get(k, 1) >= 4 and k != ORIGEM and m[k][0]["face"] in ("frente", "costas")
                  and m[k][0]["parte"] in ("tronco", "braco_dir", "braco_esq")]
        rnd = random.Random(44)
        tronco.sort()
        rnd.shuffle(tronco)
        rach = set()
        for k in tronco:
            if len(rach) >= 26:
                break
            # rachadura curta seguindo a veia (3 a 4 px)
            c = k
            seg = []
            for _ in range(4):
                if c is None or c == ORIGEM or c in rach:
                    break
                seg.append(c)
                c = pai.get(c)
            if len(seg) >= 3 and all(math.dist(m[q][1], m[r][1]) > 1.5 for q in seg for r in rach):
                rach.update(seg)
        for i, k in enumerate(sorted(rach)):
            quente = (hash(k) % 3 == 0)
            por(k, ACENTO if quente else BRILHO, 255)
            pb[k] = (ACENTO if quente else BRILHO) + (255,)
            # bordas escuras dos dois lados (fenda funda)
            for du, dv in ((-1, 0), (1, 0)):
                q = vizinho_mesma_face(k, du, dv)
                if q and q not in rach:
                    por(q, SOMBRA, 255)
            # pele repuxada: risco fraco na diagonal, a cada 2 px
            if k[1] % 2 == 0:
                for du, dv in ((-2, -1), (2, 1)):
                    q = vizinho_mesma_face(k, du, dv)
                    if q and px[q][3] == 0:
                        por(q, SOMBRA, 90)
        # costura no ombro direito (onde o braco "foi preso" ao corpo): linha na frente do tronco
        x_omb = 17
        for y in range(20, 25):
            por((x_omb, y), SOMBRA, 230)
            if y % 2 == 0:
                por((x_omb - 1 if (x_omb - 1) >= 20 else x_omb + 1, y), LINHA, 255)
        for y in range(20, 25, 2):
            por((x_omb + 1, y), LINHA, 255)
        # e nas costas, do mesmo lado do corpo (direita do jogador = lado direito de quem olha as costas)
        x_omb_c = 38
        for y in range(20, 25):
            por((x_omb_c, y), SOMBRA, 230)
        for y in range(20, 25, 2):
            por((x_omb_c - 1, y), LINHA, 255)
    return img, brilho


# ---------------------------------------------------------------- skins de teste (feitas aqui)
PELE_CLARA = ((0xF0, 0xC8, 0xA8), (0xDC, 0xAE, 0x8C))
PELE_ESCURA = ((0x6A, 0x43, 0x2C), (0x55, 0x34, 0x22))


def skin_teste(alex, pele, roupa, cabelo):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    m = mapa(alex)
    px = img.load()
    for k, (face, p) in m.items():
        x, y, z = p
        parte = face["parte"]
        cor = pele[0] if (k[0] + k[1]) % 5 else pele[1]
        if parte == "cabeca":
            if y < -6 or face["face"] == "cima" or (face["face"] == "costas" and y < -1.5) \
                    or (face["face"] in ("dir", "esq") and y < -3.5 and z > -2):
                cor = cabelo
            if face["face"] == "frente" and -4.0 < y < -3.0 and (x in (-2.5, -1.5) or x in (1.5, 2.5)):
                cor = (0xF4, 0xF4, 0xF4) if x in (-2.5, 2.5) else (0x2D, 0x4A, 0x80)
            if face["face"] == "frente" and -1.5 < y < -0.5 and -1.5 < x < 1.5:
                cor = (pele[1][0] - 30, pele[1][1] - 30, pele[1][2] - 30)
        elif parte == "tronco":
            if roupa["tronco"]:
                cor = roupa["cor"]
                if roupa["regata"] and y < 3 and abs(x) > 2:
                    cor = pele[0]
                if roupa["regata"] and y < 1.5 and abs(x) < 1.6 and face["face"] == "frente":
                    cor = pele[0]
        elif parte.startswith("braco"):
            if roupa["manga"] and y < 4.5:
                cor = roupa["cor"]
            if y > 11.5 or (face["face"] == "baixo"):
                cor = pele[1]
        elif parte.startswith("perna"):
            cor = roupa["calca"] if y < 21 else (0x2E, 0x2A, 0x28)
        px[k] = cor + (255,)
    return img


# ---------------------------------------------------------------- render (ortografico, pixel a pixel)
def render(alex, skin, marca, yaw, escala=10, escuro=False, brilho=None):
    faces = corpo(alex)
    a = math.radians(yaw)
    ca, sa = math.cos(a), math.sin(a)
    luz = (0.35, 0.55, 0.75)  # em relacao a camera: vem de frente, de cima e um pouco da esquerda
    nl = math.sqrt(sum(c * c for c in luz))
    luz = tuple(c / nl for c in luz)

    def rot(p):
        x, y, z = p
        return (x * ca + z * sa, y, -x * sa + z * ca)

    quads = []
    sk, mk = skin.load(), marca.load()
    bk = brilho.load() if brilho else None
    for f in faces:
        n = rot(f["normal"])
        if n[2] >= -1e-6:
            continue
        lam = max(0.0, -(n[0] * luz[0] + n[1] * luz[1] + n[2] * luz[2]))
        sombra = 0.55 + 0.45 * lam
        if escuro:
            sombra *= 0.22
        du = 1 / (f["uv"][2] - f["uv"][0])
        dv = 1 / (f["uv"][3] - f["uv"][1])
        for tu, tv in texels(f):
            s, t = st(f, tu, tv)
            cantos = [rot(ponto(f, s + a1 * du, t + b1 * dv)) for a1, b1 in ((0, 0), (1, 0), (1, 1), (0, 1))]
            c = sk[tu, tv]
            if c[3] == 0:
                continue
            r, g, b = c[:3]
            mc = mk[tu, tv]
            if mc[3]:
                al = mc[3] / 255
                r, g, b = (r * (1 - al) + mc[0] * al, g * (1 - al) + mc[1] * al, b * (1 - al) + mc[2] * al)
            r, g, b = r * sombra, g * sombra, b * sombra
            if bk and bk[tu, tv][3]:
                gc = bk[tu, tv]
                r, g, b = max(r, gc[0] * 0.8), max(g, gc[1] * 0.8), max(b, gc[2] * 0.8)
            prof = sum(q[2] for q in cantos) / 4
            quads.append((prof, cantos, (int(r), int(g), int(b))))
    quads.sort(key=lambda q: -q[0])
    W, H = 20 * escala, 34 * escala
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    dr = ImageDraw.Draw(img)
    for _, cantos, cor in quads:
        pts = [(W / 2 + q[0] * escala, escala * 9 + q[1] * escala) for q in cantos]
        dr.polygon(pts, fill=cor + (255,))
    return img


def fonte(tam):
    return 3 if tam >= 24 else 2  # escala da fonte de pixel


def ampliar_molde(img, fator=8):
    fundo = Image.new("RGBA", img.size, (0x30, 0x30, 0x36, 255))
    # xadrez para ver transparencia
    p = fundo.load()
    for y in range(64):
        for x in range(64):
            if (x // 4 + y // 4) % 2:
                p[x, y] = (0x3A, 0x3A, 0x42, 255)
    fundo.alpha_composite(img)
    return fundo.resize((64 * fator, 64 * fator), Image.NEAREST)


# ---------------------------------------------------------------- principal
def main():
    os.makedirs(DIR_SKINS, exist_ok=True)
    os.makedirs(DIR_PREVIA, exist_ok=True)

    clara = dict(tronco=True, regata=True, manga=False, cor=(0xE6, 0xE6, 0xE0), calca=(0x3B, 0x4A, 0x6B))
    escura = dict(tronco=True, regata=False, manga=True, cor=(0x26, 0x26, 0x2C), calca=(0x4A, 0x40, 0x36))
    skins = {}
    for alex in (False, True):
        mod = "alex" if alex else "steve"
        skins[("clara", alex)] = skin_teste(alex, PELE_CLARA, clara, (0x6B, 0x45, 0x26))
        skins[("escura", alex)] = skin_teste(alex, PELE_ESCURA, escura, (0x1A, 0x14, 0x12))
        for tom in ("clara", "escura"):
            skins[(tom, alex)].save(os.path.join(DIR_SKINS, f"{tom}_{mod}.png"))

    marcas = {}
    brilhos = {}
    vazio = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for alex in (False, True):
        m, viz, estagios = crescer(alex)
        marcas[(0, alex)] = vazio
        for s in range(1, 5):
            img, br = desenhar(alex, m, viz, estagios[s - 1], s)
            marcas[(s, alex)] = img
            brilhos[(s, alex)] = br
            suf = "_alex" if alex else ""
            img.save(os.path.join(DIR_ARTE, f"estagio_{s}{suf}.png"))
            if s == 4:
                br.save(os.path.join(DIR_ARTE, f"estagio_4_brilho{suf}.png"))

    f_tit, f_txt = fonte(28), fonte(18)
    vistas = [("frente", 0), ("3/4", 35), ("costas", 180)]
    combos = [("clara", False), ("escura", False), ("clara", True), ("escura", True)]
    esc = 10
    cel_w, cel_h = 20 * esc, 34 * esc

    # prévia por estágio: linhas = skin/modelo, colunas = vistas
    for s in range(1, 5):
        W = 170 + cel_w * len(vistas) + 64 * 4 + 40
        H = 60 + cel_h * len(combos)
        folha = Image.new("RGBA", (W, H), (0x18, 0x18, 0x1C, 255))
        dr = ImageDraw.Draw(folha)
        dr.text((16, 14), f"Marca de Sangue - estagio {s}  (previa, nao integrada)", font=f_tit, fill=(230, 230, 230))
        for i, (tom, alex) in enumerate(combos):
            y = 60 + i * cel_h
            dr.text((16, y + cel_h // 2 - 20), f"{'Alex 3px' if alex else 'Steve 4px'}\npele {tom}", font=f_txt,
                    fill=(200, 200, 200))
            for j, (nome, yaw) in enumerate(vistas):
                r = render(alex, skins[(tom, alex)], marcas[(s, alex)], yaw, esc)
                folha.alpha_composite(r, (170 + j * cel_w, y))
                if i == 0:
                    dr.text((170 + j * cel_w + 8, 44), nome, font=f_txt, fill=(160, 160, 160))
            if i < 2:
                mold = ampliar_molde(marcas[(s, i == 1)], 4)
                folha.alpha_composite(mold, (170 + cel_w * len(vistas) + 20, 70 + i * (cel_h * 2)))
                dr.text((170 + cel_w * len(vistas) + 20, 70 + i * cel_h * 2 + 262),
                        f"molde 64x64 ({'Alex' if i else 'Steve'})", font=f_txt, fill=(160, 160, 160))
        folha.save(os.path.join(DIR_PREVIA, f"estagio_{s}.png"))

    # folha única: linhas = estágios 0..4, colunas = combinações (frente e costas)
    cols = [(tom, alex, yaw) for alex in (False, True) for tom in ("clara", "escura") for yaw in (0, 180)]
    esc2 = 8
    cw, ch = 20 * esc2, 34 * esc2
    M = 210  # margem dos rotulos
    W = M + cw * len(cols)
    H = 90 + ch * 5 + ch + 20
    folha = Image.new("RGBA", (W, H), (0x18, 0x18, 0x1C, 255))
    dr = ImageDraw.Draw(folha)
    dr.text((16, 12), "Marca de Sangue - todos os estagios (previa, nao integrada)", font=f_tit, fill=(235, 235, 235))
    for j, (tom, alex, yaw) in enumerate(cols):
        dr.text((M + j * cw + 6, 56), f"{'Alex' if alex else 'Steve'} {tom}\n{'frente' if yaw == 0 else 'costas'}",
                font=f_txt, fill=(170, 170, 170))
    descr = {0: "0: sem marca", 1: "1: braco dir.", 2: "2: + peito\n    e costas", 3: "3: + braco esq.\n    pescoco\n    costura",
             4: "4: + pernas\n    rachaduras"}
    for s in range(5):
        y = 100 + s * ch
        dr.text((12, y + ch // 2 - 20), descr[s], font=f_txt, fill=(210, 210, 210))
        for j, (tom, alex, yaw) in enumerate(cols):
            folha.alpha_composite(render(alex, skins[(tom, alex)], marcas[(s, alex)], yaw, esc2), (M + j * cw, y))
    # linha extra: estágio 4 no escuro, com a camada de brilho (como ficaria se a mesa Dev ligar o brilho)
    y = 100 + 5 * ch
    dr.text((12, y + ch // 2 - 20), "4 no escuro\n(com brilho)", font=f_txt, fill=(210, 210, 210))
    for j, (tom, alex, yaw) in enumerate(cols):
        folha.alpha_composite(render(alex, skins[(tom, alex)], marcas[(4, alex)], yaw, esc2, escuro=True,
                                     brilho=brilhos[(4, alex)]), (M + j * cw, y))
    folha.save(os.path.join(DIR_PREVIA, "folha_estagios.png"))
    print("ok:", DIR_ARTE, DIR_PREVIA)


if __name__ == "__main__":
    main()
