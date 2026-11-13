"""Pintura da Marca de Sangue v2: veias organicas por campo de distancia (curvas suaves, afinando),
com relevo (luz/sombra), pele inflamada em volta, ferida de origem com costura, coracao, rachaduras
que brilham e gotas. Tudo em numpy, sobre as "faixas" do molde 64x64 (ver corpo.py).

Por que campo de distancia: cada pixel recebe a distancia ate a curva mais proxima e o raio da veia
naquele ponto. Isso da borda suave (alfa parcial), espessura que varia (3 -> 2 -> 1 px) e um vetor
normal por pixel, usado para o lado iluminado / sombreado. Nada de passo em angulo reto.
"""
import numpy as np

from corpo import faixa, topo

# ---------------------------------------------------------------- paleta (docs/decisoes/2026-11-08-cores-dos-elementos.md)
ESCURO = (0x3A, 0x03, 0x06)   # fundo de veia grossa, borda de rachadura
VEIA = (0x7A, 0x0A, 0x10)     # miolo da veia
VIVO = (0xB3, 0x13, 0x1B)     # veia fina, sangue na pele
CLARO = (0xE0, 0x31, 0x3A)    # lado iluminado, interior de rachadura
QUENTE = (0xFF, 0x7A, 0x5C)   # acento: carne viva no fundo da rachadura, ferida e coracao (so onde brilha)
ROXO = (0x4A, 0x0C, 0x2E)     # pele inflamada em volta (sempre com alfa baixo)
LINHA = (0x0E, 0x0A, 0x0A)    # fio da costura (neutro, quase preto: le contra a pele e contra o vermelho)

LUZ = np.array([-0.6, -0.8])
LUZ = LUZ / np.linalg.norm(LUZ)


def _c(cor):
    return np.asarray(cor, float) / 255


def mix(a, b, t):
    a, b = np.asarray(a, float), np.asarray(b, float)
    t = np.asarray(t, float)[..., None]
    return a * (1 - t) + b * t


# ---------------------------------------------------------------- curvas
def _desdobrar(pts, W):
    """Em faixa que da a volta, escolhe para cada ponto a copia (x +- W) mais perto do anterior."""
    out = [list(pts[0])]
    for p in pts[1:]:
        x = p[0]
        while x - out[-1][0] > W / 2:
            x -= W
        while x - out[-1][0] < -W / 2:
            x += W
        out.append([x, p[1]])
    return np.array(out, float)


def amostrar(pts, passo=0.08):
    """Curva suave (Catmull-Rom) pelos pontos. Devolve (pontos (M,2), s em 0..1 por comprimento)."""
    pts = np.asarray(pts, float)
    if len(pts) == 1:
        return pts, np.zeros(1)
    if len(pts) == 2:
        n = max(2, int(np.linalg.norm(pts[1] - pts[0]) / passo))
        t = np.linspace(0, 1, n)[:, None]
        P = pts[0] + (pts[1] - pts[0]) * t
    else:
        E = np.vstack([pts[0] * 2 - pts[1], pts, pts[-1] * 2 - pts[-2]])
        partes = []
        for i in range(1, len(E) - 2):
            p0, p1, p2, p3 = E[i - 1], E[i], E[i + 1], E[i + 2]
            n = max(2, int(np.linalg.norm(p2 - p1) / passo))
            t = np.linspace(0, 1, n, endpoint=False)[:, None]
            partes.append(0.5 * (2 * p1 + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t ** 2
                                 + (-p0 + 3 * p1 - 3 * p2 + p3) * t ** 3))
        partes.append(pts[-1:])
        P = np.vstack(partes)
    seg = np.linalg.norm(np.diff(P, axis=0), axis=1)
    s = np.concatenate([[0], np.cumsum(seg)])
    L = s[-1] if s[-1] > 0 else 1.0
    return P, s / L


# ---------------------------------------------------------------- tela (uma faixa do molde)
class Tela:
    def __init__(self, W, H, volta):
        self.W, self.H, self.volta = W, H, volta
        self.rgb = np.zeros((H, W, 3))
        self.a = np.zeros((H, W))
        self.grgb = np.zeros((H, W, 3))
        self.ga = np.zeros((H, W))
        yy, xx = np.mgrid[0:H, 0:W]
        self.cx, self.cy = xx + 0.5, yy + 0.5

    def over(self, cor, alfa, brilho=False):
        cor = np.asarray(cor, float) / 255  # cores sempre em 0..255 (paleta ou mix da paleta)
        if cor.ndim == 1:
            cor = np.broadcast_to(cor, (self.H, self.W, 3))
        alfa = np.clip(np.asarray(alfa, float), 0, 1)
        drgb, da = (self.grgb, self.ga) if brilho else (self.rgb, self.a)
        oa = alfa + da * (1 - alfa)
        num = cor * alfa[..., None] + drgb * (da * (1 - alfa))[..., None]
        novo = np.where(oa[..., None] > 1e-9, num / np.maximum(oa, 1e-9)[..., None], 0)
        drgb[...] = novo
        da[...] = oa

    def campo(self, pts, r0, r1, comp=1.0, perfil=None, ondula=0.0, semente=0):
        """Distancia de cada pixel a curva. Devolve (sdf, raio no ponto mais perto, normal (H,W,2), s)."""
        pts = np.asarray(pts, float)
        if self.volta:
            pts = _desdobrar(pts, self.W)
        P, s = amostrar(pts)
        if comp < 1.0:
            manter = s <= comp
            P, s = P[manter], s[manter] / max(comp, 1e-6)
        r = r0 + (r1 - r0) * s
        if perfil is not None:
            r = r * perfil(s)
        if ondula:
            L = max(1.0, float(np.sum(np.linalg.norm(np.diff(P, axis=0), axis=1))) if len(P) > 1 else 1.0)
            r = r * (1 + ondula * np.sin(s * L * 7.3 + semente * 1.7) * np.cos(s * L * 2.9 + semente))
        dx = self.cx[..., None] - P[:, 0]
        if self.volta:
            dx = (dx + self.W / 2) % self.W - self.W / 2
        dy = self.cy[..., None] - P[:, 1]
        dist = np.hypot(dx, dy)
        sd = dist - r
        i = sd.argmin(-1)
        sdf = np.take_along_axis(sd, i[..., None], -1)[..., 0]
        di = np.take_along_axis(dist, i[..., None], -1)[..., 0]
        nx = np.take_along_axis(dx, i[..., None], -1)[..., 0] / np.maximum(di, 1e-6)
        ny = np.take_along_axis(dy, i[..., None], -1)[..., 0] / np.maximum(di, 1e-6)
        return sdf, r[i], np.stack([nx, ny], -1), s[i]

    def para_uv(self, tex, brilho, u0, v0):
        tex[v0:v0 + self.H, u0:u0 + self.W, :3] = np.round(self.rgb * 255)
        tex[v0:v0 + self.H, u0:u0 + self.W, 3] = np.round(self.a * 255)
        brilho[v0:v0 + self.H, u0:u0 + self.W, :3] = np.round(self.grgb * 255)
        brilho[v0:v0 + self.H, u0:u0 + self.W, 3] = np.round(self.ga * 255)


# ---------------------------------------------------------------- pinceis
def halo_veia(tela, pts, r0, r1, comp=1.0, forca=1.0, semente=0):
    """Alfa da pele inflamada (arroxeada) em volta da veia. Mais larga onde a veia e grossa.
    Devolve o alfa (quem chama junta todos por maximo, para nao empilhar e virar mancha)."""
    sdf, r, n, s = tela.campo(pts, r0, r1, comp, semente=semente)
    alc = 0.7 + 1.0 * r
    return np.clip(1 - sdf / alc, 0, 1) ** 1.6 * (sdf >= 0) * 0.17 * np.clip(r / 0.7, 0.25, 1.0) * forca


def corpo_veia(tela, pts, r0, r1, comp=1.0, forca=1.0, semente=0):
    """A veia em si, com relevo: miolo escuro, borda mais viva, lado da luz claro, lado oposto escuro."""
    sdf, r, n, s = tela.campo(pts, r0, r1, comp, ondula=0.08, semente=semente)
    lit = n @ LUZ
    grossa = np.clip(r / 0.7, 0, 1)
    prof = np.clip(-sdf / np.maximum(r, 0.3), 0, 1)
    # relevo fora da veia, do lado da luz (pele esticada por cima)
    fora = (sdf >= 0) & (sdf < 0.7)
    rim = 0.14 * np.clip(lit, 0, 1) * np.clip(1 - sdf / 0.7, 0, 1) * grossa * fora
    tela.over(CLARO, rim * forca)
    base = mix(VIVO, VEIA, prof * grossa)
    base = mix(base, CLARO, 0.55 * np.clip(lit, 0, 1) * (1 - prof))
    base = mix(base, ESCURO, 0.5 * np.clip(-lit, 0, 1) * (1 - 0.5 * prof))
    cob = np.clip(0.5 - sdf, 0, 1)
    opac = np.where(r >= 0.5, 0.93, 0.5 + 0.86 * r)
    tela.over(base, cob * opac * forca)


def rachadura(tela, pts, r_in=0.42, borda=0.4, brilho=1.0, semente=0):
    """Pele que rachou por cima da veia: borda escura, interior vermelho vivo (que brilha)."""
    def perfil(s):
        return np.clip(np.minimum(s, 1 - s) * 5, 0.12, 1)
    sdf, r, n, s = tela.campo(pts, r_in, r_in, perfil=perfil, ondula=0.28, semente=semente)
    b = np.clip(borda + 0.5 - sdf, 0, 1) * 0.96
    tela.over(ESCURO, b)
    centro = np.clip(-sdf / np.maximum(r, 0.2), 0, 1) * np.sqrt(np.clip(1 - np.abs(2 * s - 1), 0, 1))
    cor = mix(CLARO, QUENTE, centro)
    dentro = np.clip(0.5 - sdf, 0, 1)
    tela.over(cor, dentro)
    tela.over(cor, np.clip((dentro - 0.35) / 0.65, 0, 1) * brilho, brilho=True)  # so o miolo (1 px) brilha


def gota(tela, p, comp, forca=1.0):
    """Sangue escorrendo: rastro fino e uma gota com brilho na ponta de baixo."""
    x, y = p
    sdf, r, n, s = tela.campo([(x, y), (x + 0.12, y + comp * 0.55), (x, y + comp - 0.45)], 0.34, 0.2)
    tela.over(VIVO, np.clip(0.5 - sdf, 0, 1) * 0.72 * forca)
    sdf, r, n, s = tela.campo([(x, y + comp - 0.35), (x, y + comp)], 0.5, 0.56)
    lit = n @ LUZ
    cor = mix(VIVO, CLARO, 0.6 * np.clip(lit, 0, 1))
    cor = mix(cor, ESCURO, 0.3 * np.clip(-lit, 0, 1))
    tela.over(cor, np.clip(0.5 - sdf, 0, 1) * 0.95 * forca)


def costura(tela, pts, passo=1.7, inicio=0.6, fio=2.2, rasgada=False):
    """Pontos de linha cruzando um corte. rasgada = so metade dos pontos, so de um lado (arrebentou)."""
    pts = np.asarray(pts, float)
    if tela.volta:
        pts = _desdobrar(pts, tela.W)
    P, s = amostrar(pts)
    L = float(np.sum(np.linalg.norm(np.diff(P, axis=0), axis=1)))
    for k, a in enumerate(np.arange(inicio, L - 0.3, passo)):
        i = min(int(np.searchsorted(s * L, a)), len(P) - 2)
        i = max(i, 1)
        p = P[i]
        t = P[i + 1] - P[i - 1]
        t = t / max(np.linalg.norm(t), 1e-6)
        perp = np.array([-t[1], t[0]])
        lado = 1 if k % 2 == 0 else -1
        if rasgada:
            if k % 2 == 1:
                continue
            seg = [p + perp * lado * 0.3, p + perp * lado * fio]
        else:
            seg = [p - perp * (fio - 0.2 * lado), p + perp * (fio + 0.2 * lado)]
        sdf, r, n, s2 = tela.campo(seg, 0.33, 0.33)
        tela.over(LINHA, np.clip(0.5 - sdf, 0, 1) * 0.96)


def ferida(tela, pts, estagio):
    """Onde o Sangue entrou: talho diagonal no antebraco. Cresce, abre e e costurado com o estagio."""
    cfg = {1: dict(halo=(1.3, 0.20), fora=0.50, dentro=0.0, brilho=0.0, crosta=0.0),
           2: dict(halo=(1.6, 0.26), fora=0.60, dentro=0.26, brilho=0.45, crosta=0.30),
           3: dict(halo=(1.8, 0.28), fora=0.70, dentro=0.30, brilho=0.70, crosta=0.30),
           4: dict(halo=(1.9, 0.30), fora=0.72, dentro=0.36, brilho=1.00, crosta=0.30)}[estagio]
    d0, r, n, s = tela.campo(pts, 0.0, 0.0)
    hr, ha = cfg["halo"]
    tela.over(ROXO, np.clip(1 - d0 / hr, 0, 1) ** 1.4 * ha)
    if cfg["crosta"]:
        anel = np.clip(1 - (d0 - cfg["fora"]) / 0.9, 0, 1) * (d0 >= cfg["fora"] - 0.2) * cfg["crosta"]
        tela.over(mix(ESCURO, LINHA, 0.5), anel)
    lit = n @ LUZ
    borda = mix(ESCURO, VEIA, 0.35 * np.clip(lit, 0, 1))
    tela.over(borda, np.clip(0.5 - (d0 - cfg["fora"]), 0, 1) * 0.97)
    if cfg["dentro"] > 0:
        perfil = np.sqrt(np.clip(1 - np.abs(2 * s - 1), 0, 1))
        di = d0 - cfg["dentro"] * np.clip(perfil + 0.25, 0, 1)
        centro = np.clip(-di / 0.5, 0, 1) * perfil
        cor = mix(CLARO, QUENTE, centro)
        cob = np.clip(0.5 - di, 0, 1)
        tela.over(cor, cob)
        tela.over(cor, np.clip((cob - 0.35) / 0.65, 0, 1) * cfg["brilho"], brilho=True)  # so o miolo brilha
    else:
        # estagio 1: so um pixel umido no meio do arranhao
        pm = np.asarray(pts, float).mean(0)
        dc, _, _, _ = tela.campo([pm, pm + 0.01], 0.3, 0.3)
        tela.over(VIVO, np.clip(0.5 - dc, 0, 1) * 0.9)


def coracao(tela, c, estagio):
    """O no da Marca no peito, de onde as veias puxam. Brilha; no 4 racha em estrela."""
    raio = {3: 0.8, 4: 1.0}[estagio]
    d, r, n, s = tela.campo([c, (c[0] + 0.01, c[1])], 0.0, 0.0)
    tela.over(ROXO, np.clip(1 - (d - raio) / 2.0, 0, 1) ** 1.5 * (d >= raio) * 0.3)
    lit = n @ LUZ
    cor = mix(VEIA, ESCURO, np.clip(1 - d / raio, 0, 1))
    cor = mix(cor, CLARO, 0.4 * np.clip(lit, 0, 1) * (d > raio - 0.9))
    tela.over(cor, np.clip(raio + 0.5 - d, 0, 1) * 0.95)
    forca = 0.7 if estagio == 3 else 1.0
    tela.over(QUENTE, np.clip(0.85 - d, 0, 1) * forca)
    tela.over(QUENTE, np.clip(1.0 - d, 0, 1) * (0.6 if estagio == 3 else 1.0), brilho=True)


# ---------------------------------------------------------------- o desenho (Steve; Alex usa fracoes da largura)
# Ponto em uma face: (face, fracao da largura da face 0..1, y em px desde o topo da faixa).
# Faixas: dir | frente | esq | costas. Braco direito: dir = lado de fora, esq = lado do corpo.
# Braco esquerdo: dir = lado do corpo, esq = lado de fora. Costas: fracao 0 = lado esquerdo do jogador.
def F(face, fx, fy):
    return (face, fx, fy)


FERIDA = [F("frente", 0.28, 6.2), F("frente", 0.5, 7.6), F("frente", 0.72, 9.1)]
FERIDA_CURTA = [F("frente", 0.38, 6.9), F("frente", 0.62, 8.4)]
CORACAO = F("frente", 0.56, 3.4)

# veia: parte, pontos, (r0, r1), desde (estagio em que nasce), comp {estagio: fracao do comprimento}
VEIAS = [
    # --- braco direito: tudo nasce na ferida
    dict(parte="braco_dir", pts=[F("frente", 0.5, 7.4), F("frente", 0.3, 5.6), F("frente", 0.42, 3.6), F("frente", 0.62, 1.8), F("frente", 0.8, 0.0)],
         r=(0.8, 0.6), desde=1, comp={1: 0.42, 2: 1.0}),
    dict(parte="braco_dir", pts=[F("frente", 0.5, 8.0), F("frente", 0.62, 9.8), F("frente", 0.5, 11.4), F("frente", 0.42, 12.0)],
         r=(0.7, 0.3), desde=1, comp={1: 0.55, 2: 1.0}),
    dict(parte="braco_dir", pts=[F("frente", 0.35, 5.0), F("dir", 0.75, 4.0), F("dir", 0.45, 2.2), F("dir", 0.6, 0.4)],
         r=(0.6, 0.3), desde=2),
    dict(parte="braco_dir", pts=[F("dir", 0.55, 3.0), F("dir", 0.3, 5.5), F("dir", 0.55, 8.0), F("dir", 0.4, 10.0)],
         r=(0.55, 0.25), desde=2),
    dict(parte="braco_dir", pts=[F("costas", 0.45, 0.0), F("costas", 0.62, 2.4), F("costas", 0.4, 5.0), F("costas", 0.55, 7.8), F("costas", 0.45, 10.5)],
         r=(0.8, 0.3), desde=2, comp={2: 0.7}),
    dict(parte="braco_dir", pts=[F("costas", 0.5, 4.2), F("costas", 0.8, 5.8), F("dir", 0.2, 7.0)],
         r=(0.45, 0.2), desde=3),
    dict(parte="braco_dir", pts=[F("frente", 0.6, 7.0), F("esq", 0.3, 6.0), F("esq", 0.6, 4.5)],
         r=(0.5, 0.2), desde=2),
    dict(parte="braco_dir", pts=[F("frente", 0.35, 4.6), F("frente", 0.12, 3.4)], r=(0.35, 0.15), desde=3),
    dict(parte="braco_dir", pts=[F("frente", 0.6, 9.6), F("esq", 0.25, 10.6)], r=(0.35, 0.15), desde=3),
    dict(parte="braco_dir", pts=[F("frente", 0.3, 5.6), F("frente", 0.15, 7.2), F("dir", 0.9, 8.6)], r=(0.35, 0.12), desde=1, comp={1: 0.5, 2: 1.0}),
    dict(parte="braco_dir_topo", pts=[("topo", 0.8, 4.0), ("topo", 0.82, 1.6)], r=(0.75, 0.65), desde=2),
    # --- tronco
    dict(parte="tronco_topo", pts=[("topo", 0.06, 0.0), ("topo", 0.055, 4.0)], r=(0.8, 0.8), desde=2),
    dict(parte="tronco", pts=[F("frente", 0.04, 0.0), F("frente", 0.15, 1.4), F("frente", 0.36, 2.5), F("frente", 0.56, 3.4)],
         r=(1.0, 0.85), desde=2),
    dict(parte="tronco", pts=[F("frente", 0.56, 3.4), F("frente", 0.47, 5.6), F("frente", 0.54, 7.6), F("frente", 0.44, 10.0), F("frente", 0.46, 11.2)],
         r=(0.95, 0.45), desde=2, comp={2: 0.6, 3: 0.85}),
    dict(parte="tronco", pts=[F("frente", 0.56, 3.4), F("frente", 0.72, 2.4), F("frente", 0.9, 1.6), F("esq", 0.2, 1.2), F("esq", 0.6, 1.6), F("esq", 0.95, 0.5)],
         r=(0.65, 0.4), desde=2, comp={2: 0.5}),
    dict(parte="tronco", pts=[F("frente", 0.18, 1.8), F("frente", 0.09, 4.0), F("frente", 0.04, 6.8), F("dir", 0.85, 8.4), F("dir", 0.65, 10.6)],
         r=(0.6, 0.25), desde=2, comp={2: 0.6}),
    dict(parte="tronco", pts=[F("frente", 0.5, 6.8), F("frente", 0.68, 8.0), F("frente", 0.8, 9.8)], r=(0.45, 0.2), desde=3),
    dict(parte="tronco", pts=[F("frente", 0.33, 5.0), F("frente", 0.17, 6.3)], r=(0.4, 0.15), desde=3),
    dict(parte="tronco", pts=[F("frente", 0.46, 10.2), F("frente", 0.3, 11.4), F("frente", 0.28, 12.0)], r=(0.5, 0.4), desde=4),
    dict(parte="tronco", pts=[F("frente", 0.46, 10.2), F("frente", 0.66, 11.4), F("frente", 0.7, 12.0)], r=(0.5, 0.4), desde=4),
    dict(parte="tronco", pts=[F("costas", 0.95, 0.0), F("costas", 0.82, 1.6), F("costas", 0.66, 3.4), F("costas", 0.56, 5.6), F("costas", 0.6, 8.0), F("costas", 0.52, 10.5)],
         r=(1.0, 0.4), desde=2, comp={2: 0.65}),
    dict(parte="tronco", pts=[F("costas", 0.78, 2.0), F("costas", 0.92, 4.0), F("costas", 0.96, 6.6)], r=(0.5, 0.2), desde=2),
    dict(parte="tronco", pts=[F("costas", 0.62, 4.0), F("costas", 0.42, 4.3), F("costas", 0.24, 3.2), F("costas", 0.1, 1.8), F("costas", 0.03, 0.4)],
         r=(0.6, 0.3), desde=3),
    dict(parte="tronco", pts=[F("costas", 0.58, 7.2), F("costas", 0.36, 8.6), F("costas", 0.2, 10.4)], r=(0.45, 0.2), desde=3),
    dict(parte="tronco", pts=[F("costas", 0.64, 6.0), F("costas", 0.8, 7.6)], r=(0.35, 0.15), desde=4),
    dict(parte="tronco", pts=[F("costas", 0.54, 9.6), F("costas", 0.7, 11.2), F("costas", 0.72, 12.0)], r=(0.45, 0.35), desde=4),
    dict(parte="tronco", pts=[F("costas", 0.54, 9.6), F("costas", 0.38, 11.2), F("costas", 0.34, 12.0)], r=(0.45, 0.35), desde=4),
    dict(parte="tronco", pts=[F("dir", 0.5, 0.0), F("dir", 0.35, 2.6), F("dir", 0.55, 5.4)], r=(0.55, 0.2), desde=3),
    dict(parte="tronco_topo", pts=[("topo", 0.95, 1.8), ("topo", 0.94, 4.0)], r=(0.6, 0.6), desde=3),
    # --- pescoco (faixa da cabeca, 8 de altura; dir = lado direito do jogador)
    dict(parte="cabeca", pts=[F("dir", 0.68, 8.0), F("dir", 0.6, 6.6), F("dir", 0.7, 5.2)], r=(0.6, 0.25), desde=3),
    dict(parte="cabeca", pts=[F("frente", 0.2, 8.0), F("frente", 0.26, 6.9)], r=(0.45, 0.2), desde=3),
    dict(parte="cabeca", pts=[F("costas", 0.88, 8.0), F("costas", 0.82, 6.6), F("costas", 0.7, 5.6)], r=(0.5, 0.2), desde=3),
    dict(parte="cabeca", pts=[F("dir", 0.3, 8.0), F("dir", 0.38, 6.8)], r=(0.4, 0.15), desde=4),
    # olho direito injetado (estagio 4): veia que desce do olho pela bochecha e outra para a tempora
    dict(parte="cabeca", pts=[F("frente", 0.2, 5.0), F("frente", 0.12, 6.0), F("frente", 0.08, 7.4)], r=(0.3, 0.12), desde=4, olho=True),
    dict(parte="cabeca", pts=[F("frente", 0.14, 4.4), F("frente", 0.04, 3.6)], r=(0.25, 0.1), desde=4, olho=True),
    # --- braco esquerdo (dir = lado do corpo, esq = lado de fora)
    dict(parte="braco_esq_topo", pts=[("topo", 0.15, 1.8), ("topo", 0.2, 4.0)], r=(0.6, 0.6), desde=3),
    dict(parte="braco_esq", pts=[F("frente", 0.35, 0.0), F("frente", 0.55, 2.2), F("frente", 0.38, 4.6), F("frente", 0.5, 7.0), F("frente", 0.42, 9.5)],
         r=(0.75, 0.3), desde=3, comp={3: 0.7}),
    dict(parte="braco_esq", pts=[F("frente", 0.7, 0.4), F("esq", 0.3, 1.6), F("esq", 0.55, 4.0), F("esq", 0.35, 6.5), F("esq", 0.5, 9.0)],
         r=(0.6, 0.25), desde=3, comp={3: 0.6}),
    dict(parte="braco_esq", pts=[F("costas", 0.5, 0.0), F("costas", 0.4, 2.5), F("costas", 0.58, 5.0), F("costas", 0.45, 7.5)],
         r=(0.6, 0.25), desde=3, comp={3: 0.7}),
    dict(parte="braco_esq", pts=[F("frente", 0.45, 3.6), F("frente", 0.15, 4.8)], r=(0.35, 0.15), desde=4),
    dict(parte="braco_esq", pts=[F("esq", 0.45, 3.0), F("esq", 0.8, 4.4)], r=(0.3, 0.12), desde=4),
    # --- pernas (so no 4): continuam as veias que descem da barriga e das costas
    dict(parte="perna_dir", pts=[F("frente", 0.56, 0.0), F("frente", 0.45, 2.6), F("frente", 0.62, 5.6), F("frente", 0.48, 8.6), F("frente", 0.55, 11.0)], r=(0.8, 0.3), desde=4),
    dict(parte="perna_dir", pts=[F("frente", 0.5, 4.0), F("dir", 0.75, 5.6), F("dir", 0.5, 8.0)], r=(0.4, 0.15), desde=4),
    dict(parte="perna_dir", pts=[F("costas", 0.44, 0.0), F("costas", 0.6, 3.0), F("costas", 0.4, 6.4), F("costas", 0.55, 9.2)], r=(0.7, 0.25), desde=4),
    dict(parte="perna_dir", pts=[F("dir", 0.5, 0.0), F("dir", 0.65, 2.8), F("dir", 0.45, 5.0)], r=(0.5, 0.2), desde=4),
    dict(parte="perna_esq", pts=[F("frente", 0.44, 0.0), F("frente", 0.55, 2.8), F("frente", 0.38, 5.8), F("frente", 0.5, 8.8)], r=(0.7, 0.25), desde=4),
    dict(parte="perna_esq", pts=[F("costas", 0.68, 0.0), F("costas", 0.5, 3.2), F("costas", 0.62, 6.0)], r=(0.6, 0.2), desde=4),
    dict(parte="perna_esq", pts=[F("esq", 0.5, 0.0), F("esq", 0.35, 2.6), F("esq", 0.55, 5.5)], r=(0.5, 0.2), desde=4),
    dict(parte="perna_esq", pts=[F("frente", 0.5, 4.6), F("esq", 0.2, 6.0)], r=(0.35, 0.12), desde=4),
]

# rachaduras: parte, pontos, desde; gota = comprimento da gota que escorre da ponta de baixo
RACHADURAS = [
    dict(parte="braco_dir", pts=[F("dir", 0.5, 4.8), F("dir", 0.38, 3.2), F("dir", 0.55, 1.6)], desde=4),
    dict(parte="braco_dir", pts=[F("costas", 0.5, 3.0), F("costas", 0.42, 4.8)], desde=4),
    dict(parte="tronco", pts=[F("costas", 0.64, 3.8), F("costas", 0.57, 5.4), F("costas", 0.6, 7.0)], desde=4, gota=2.0),
    dict(parte="tronco", pts=[F("frente", 0.52, 8.0), F("frente", 0.46, 9.6)], desde=4, gota=1.8),
    dict(parte="braco_esq", pts=[F("esq", 0.5, 3.2), F("esq", 0.4, 5.0)], desde=4, gota=1.6),
    # estrela do coracao
    dict(parte="tronco", pts=[F("frente", 0.56, 3.4), F("frente", 0.46, 4.6), F("frente", 0.5, 5.8)], desde=4, coracao=True, costura=True, gota=1.5),
    dict(parte="tronco", pts=[F("frente", 0.56, 3.4), F("frente", 0.72, 2.6), F("frente", 0.86, 2.1)], desde=4, coracao=True),
]

GOTAS_FERIDA = {3: [(0.72, 9.3, 1.4)], 4: [(0.72, 9.3, 2.2), (0.36, 7.6, 1.3)]}  # (fx, fy, comprimento) na frente do braco

ESCALA_R = {1: 0.55, 2: 0.74, 3: 0.84, 4: 0.92}   # multiplica os raios acima (peito tem so 8 px de largura)
FORCA_HALO = {1: 0.5, 2: 0.8, 3: 1.0, 4: 1.1}
OLHO_DIR = ((9, 12), (10, 12))  # branco e pupila do olho direito no molde (skins no padrao Steve)


# ---------------------------------------------------------------- montagem
class Molde:
    def __init__(self, alex):
        self.alex = alex
        self.telas = {}
        self.origem = {}
        for parte in ("cabeca", "tronco", "braco_dir", "braco_esq", "perna_dir", "perna_esq"):
            u0, v0, W, H, _ = faixa(parte, alex)
            self.telas[parte] = Tela(W, H, True)
            self.origem[parte] = (u0, v0)
        for parte in ("tronco", "braco_dir", "braco_esq"):
            u0, v0, W, H = topo(parte, alex)
            self.telas[parte + "_topo"] = Tela(W, H, False)
            self.origem[parte + "_topo"] = (u0, v0)

    def xy(self, parte, p):
        face, fx, fy = p
        if parte.endswith("_topo"):
            return (fx * self.telas[parte].W, fy)
        _, _, _, _, fxs = faixa(parte, self.alex)
        x0, w = fxs[face]
        return (x0 + fx * w, fy)

    def pts(self, parte, lista):
        return [self.xy(parte, p) for p in lista]

    def gerar(self, estagio):
        tex = np.zeros((64, 64, 4), np.uint8)
        bri = np.zeros((64, 64, 4), np.uint8)
        s = estagio
        ativas = [v for v in VEIAS if v["desde"] <= s]

        def raio(v):
            # r**1.35: tronco continua tronco, galho e ramo encolhem mais (hierarquia legivel em 8 px)
            r0, r1 = v["r"]
            k = ESCALA_R[s] * (0.82 if v["desde"] == s and s > 1 else 1.0)
            return r0 ** 1.35 * k, r1 ** 1.35 * k

        def comp(v):
            c = v.get("comp", {})
            if s in c:
                return c[s]
            if v["desde"] == s and c:
                return min(c.values())
            return 1.0

        # 1) pele inflamada de todas as veias (juntas por maximo), 2) corpos (finas primeiro, grossas por cima)
        ordem = sorted(ativas, key=lambda v: v["r"][0])
        halos = {}
        for i, v in enumerate(ordem):
            t = self.telas[v["parte"]]
            r0, r1 = raio(v)
            h = halo_veia(t, self.pts(v["parte"], v["pts"]), r0, r1, comp(v), FORCA_HALO[s], semente=i)
            halos[v["parte"]] = np.maximum(halos.get(v["parte"], 0), h)
        for parte, h in halos.items():
            self.telas[parte].over(ROXO, h)
        for i, v in enumerate(ordem):
            t = self.telas[v["parte"]]
            r0, r1 = raio(v)
            corpo_veia(t, self.pts(v["parte"], v["pts"]), r0, r1, comp(v), semente=i)
        # 3) ferida de origem
        tb = self.telas["braco_dir"]
        ferida(tb, self.pts("braco_dir", FERIDA if s >= 2 else FERIDA_CURTA), s)
        # 4) coracao e rachaduras
        tt = self.telas["tronco"]
        if s >= 3:
            coracao(tt, self.xy("tronco", CORACAO), s)
        for i, r in enumerate(RACHADURAS):
            if r["desde"] > s:
                continue
            t = self.telas[r["parte"]]
            rachadura(t, self.pts(r["parte"], r["pts"]), semente=i)
        # 5) costuras: ferida (3+) e os pontos arrebentados em volta do coracao (4)
        if s >= 3:
            costura(tb, self.pts("braco_dir", FERIDA), rasgada=(s == 4))
        if s == 4:
            for r in RACHADURAS:
                if r.get("costura"):
                    costura(self.telas[r["parte"]], self.pts(r["parte"], r["pts"]), passo=1.3, inicio=0.9, fio=1.2, rasgada=True)
        # 6) gotas
        for fx, fy, c in GOTAS_FERIDA.get(s, []):
            gota(tb, self.xy("braco_dir", F("frente", fx, fy)), c)
        for r in RACHADURAS:
            if r["desde"] <= s and r.get("gota"):
                p = self.xy(r["parte"], r["pts"][-1])
                gota(self.telas[r["parte"]], (p[0], p[1] + 0.3), r["gota"], forca=0.9)
        # 7) molde
        for parte, t in self.telas.items():
            u0, v0 = self.origem[parte]
            t.para_uv(tex, bri, u0, v0)
        # 8) olho direito injetado (skins no padrao Steve/Alex: branco em (9,12), pupila em (10,12))
        if s >= 3:
            a = {3: (0.35, 0.0), 4: (0.6, 0.25)}[s]
            for (u, v), alfa in zip(OLHO_DIR, a):
                if alfa > 0:
                    _tingir(tex, u, v, VIVO, alfa)
        tex[tex[..., 3] < 4] = 0
        bri[bri[..., 3] < 4] = 0
        return tex, bri


def _tingir(tex, u, v, cor, alfa):
    atual = tex[v, u].astype(float) / 255
    oa = alfa + atual[3] * (1 - alfa)
    rgb = (np.asarray(cor) / 255 * alfa + atual[:3] * atual[3] * (1 - alfa)) / max(oa, 1e-6)
    tex[v, u, :3] = np.round(rgb * 255)
    tex[v, u, 3] = round(oa * 255)
