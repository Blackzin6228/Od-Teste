"""Corpo do jogador (Steve 4px / Alex 3px) com o MESMO mapeamento de UV do Minecraft (ModelPart.Cube),
skins de teste feitas aqui e um render ortografico por raio (numpy). Tudo nosso, sem Pillow.

Eixos iguais aos do jogo: x cresce para a ESQUERDA do jogador, y cresce para BAIXO, frente = -z.
Quem olha o jogador de frente ve o +x a sua direita (o braco direito do jogador fica a esquerda de quem olha).
"""
import math

import numpy as np


# ---------------------------------------------------------------- caixas e faces
def _caixa(nome, x0, y0, z0, w, h, d, u, v):
    x1, y1, z1 = x0 + w, y0 + h, z0 + d
    P = [(x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0),
         (x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1)]
    u1, u2, u3, u4, u5 = u + d, u + d + w, u + d + w + w, u + d + w + d, u + d + w + d + w
    v0, v1, v2 = v, v + d, v + d + h
    faces = [("cima", (5, 4, 0, 1), (u1, v0, u2, v1), (0, -1, 0)),
             ("baixo", (2, 3, 7, 6), (u2, v1, u3, v0), (0, 1, 0)),
             ("dir", (0, 4, 7, 3), (u, v1, u1, v2), (-1, 0, 0)),
             ("frente", (1, 0, 3, 2), (u1, v1, u2, v2), (0, 0, -1)),
             ("esq", (5, 1, 2, 6), (u2, v1, u4, v2), (1, 0, 0)),
             ("costas", (4, 5, 6, 7), (u4, v1, u5, v2), (0, 0, 1))]
    out = []
    for fnome, idx, uv, normal in faces:
        A, B, C = P[idx[1]], P[idx[0]], P[idx[2]]  # A=(umin,vmin) B=(umax,vmin) C=(umin,vmax)
        out.append(dict(parte=nome, face=fnome, A=np.array(A, float), B=np.array(B, float),
                        C=np.array(C, float), uv=uv, normal=np.array(normal, float)))
    return dict(nome=nome, lo=np.array((x0, y0, z0), float), hi=np.array((x1, y1, z1), float), faces=out)


def corpo(alex):
    bw = 3 if alex else 4
    dy = 0.5 if alex else 0.0
    return [
        _caixa("cabeca", -4, -8, -4, 8, 8, 8, 0, 0),
        _caixa("tronco", -4, 0, -2, 8, 12, 4, 16, 16),
        _caixa("braco_dir", -4 - bw, dy, -2, bw, 12, 4, 40, 16),
        _caixa("braco_esq", 4, dy, -2, bw, 12, 4, 32, 48),
        _caixa("perna_dir", -4, 12, -2, 4, 12, 4, 0, 16),
        _caixa("perna_esq", 0, 12, -2, 4, 12, 4, 16, 48),
    ]


def faces(alex):
    return [f for c in corpo(alex) for f in c["faces"]]


# ---------------------------------------------------------------- faixas (tiras) do molde 64x64
# Cada parte tem uma "faixa" continua no molde: dir | frente | esq | costas, que da a volta no corpo
# (a borda direita da faixa emenda na esquerda). Desenhar nela = curva continua pelo corpo.
def faixa(parte, alex):
    """(u0, v0, largura, altura, {face: (x_inicio, largura_face)})"""
    w = 3 if (alex and parte.startswith("braco")) else (8 if parte in ("tronco", "cabeca") else 4)
    d = 8 if parte == "cabeca" else 4
    h = 8 if parte == "cabeca" else 12
    origem = {"cabeca": (0, 8), "tronco": (16, 20), "braco_dir": (40, 20), "braco_esq": (32, 52),
              "perna_dir": (0, 20), "perna_esq": (16, 52)}[parte]
    fx = {"dir": (0, d), "frente": (d, w), "esq": (d + w, d), "costas": (2 * d + w, w)}
    return origem[0], origem[1], 2 * d + 2 * w, h, fx


def topo(parte, alex):
    """Face de cima (ombro): (u0, v0, largura, altura). Borda da frente fica em y = altura."""
    w = 3 if (alex and parte.startswith("braco")) else (8 if parte == "tronco" else 4)
    origem = {"tronco": (20, 16), "braco_dir": (44, 16), "braco_esq": (36, 48)}[parte]
    return origem[0], origem[1], w, 4


# ---------------------------------------------------------------- texel -> ponto 3D (para as skins de teste)
def mapa(alex):
    m = {}
    for f in faces(alex):
        umin, vmin, umax, vmax = f["uv"]
        for tu in range(int(min(umin, umax)), int(max(umin, umax))):
            for tv in range(int(min(vmin, vmax)), int(max(vmin, vmax))):
                s = (tu + 0.5 - umin) / (umax - umin)
                t = (tv + 0.5 - vmin) / (vmax - vmin)
                p = f["A"] + s * (f["B"] - f["A"]) + t * (f["C"] - f["A"])
                m[(tu, tv)] = (f, tuple(p))
    return m


# ---------------------------------------------------------------- skins de teste (feitas aqui)
PELE_CLARA = ((0xF0, 0xC8, 0xA8), (0xDC, 0xAE, 0x8C))
PELE_ESCURA = ((0x6A, 0x43, 0x2C), (0x55, 0x34, 0x22))


def skin_teste(alex, pele, roupa, cabelo):
    img = np.zeros((64, 64, 4), np.uint8)
    for (tu, tv), (face, p) in mapa(alex).items():
        x, y, z = p
        parte, fn = face["parte"], face["face"]
        cor = pele[0] if (tu + tv) % 5 else pele[1]
        if parte == "cabeca":
            if y < -6 or fn == "cima" or (fn == "costas" and y < -1.5) or (fn in ("dir", "esq") and y < -3.5 and z > -2):
                cor = cabelo
            if fn == "frente" and -4.0 < y < -3.0 and (x in (-2.5, -1.5) or x in (1.5, 2.5)):
                cor = (0xF4, 0xF4, 0xF4) if x in (-2.5, 2.5) else (0x2D, 0x4A, 0x80)
            if fn == "frente" and -1.5 < y < -0.5 and -1.5 < x < 1.5:
                cor = (pele[1][0] - 30, pele[1][1] - 30, pele[1][2] - 30)
        elif parte == "tronco":
            if roupa["tronco"]:
                cor = roupa["cor"]
                if roupa["regata"] and y < 3 and abs(x) > 2:
                    cor = pele[0]
                if roupa["regata"] and y < 1.5 and abs(x) < 1.6 and fn == "frente":
                    cor = pele[0]
        elif parte.startswith("braco"):
            if roupa["manga"] and y < 4.5:
                cor = roupa["cor"]
            if y > 11.5 or fn == "baixo":
                cor = pele[1]
        elif parte.startswith("perna"):
            cor = roupa["calca"] if y < 21 else (0x2E, 0x2A, 0x28)
        img[tv, tu] = (*cor, 255)
    return img


def skins_teste():
    clara = dict(tronco=True, regata=True, manga=False, cor=(0xE6, 0xE6, 0xE0), calca=(0x3B, 0x4A, 0x6B))
    escura = dict(tronco=True, regata=False, manga=True, cor=(0x26, 0x26, 0x2C), calca=(0x4A, 0x40, 0x36))
    out = {}
    for alex in (False, True):
        out[("clara", alex)] = skin_teste(alex, PELE_CLARA, clara, (0x6B, 0x45, 0x26))
        out[("escura", alex)] = skin_teste(alex, PELE_ESCURA, escura, (0x1A, 0x14, 0x12))
    return out


# ---------------------------------------------------------------- render por raio (ortografico)
def _rot(yaw, pitch):
    a, b = math.radians(yaw), math.radians(pitch)
    ry = np.array([[math.cos(a), 0, math.sin(a)], [0, 1, 0], [-math.sin(a), 0, math.cos(a)]])
    rx = np.array([[1, 0, 0], [0, math.cos(b), -math.sin(b)], [0, math.sin(b), math.cos(b)]])
    return rx @ ry  # modelo -> camera


_EIXO = {"dir": 0, "esq": 0, "cima": 1, "baixo": 1, "frente": 2, "costas": 2}
_NEG = {"dir", "cima", "frente"}


def render(alex, skin, marca=None, yaw=0.0, pitch=0.0, esc=8, janela=(-10, -9, 10, 25), escuro=False,
           brilho=None, pulso=1.0):
    """Desenha o jogador. skin/marca/brilho: (64,64,4) uint8. janela em unidades do modelo (1 = 1 texel).
    yaw 0 = de frente, 180 = de costas, -35 = tres quartos mostrando o lado direito do jogador.
    escuro = noite sem luz (a camada de brilho continua acesa, multiplicada por `pulso`)."""
    x0, y0, x1, y1 = janela
    W, H = int(round((x1 - x0) * esc)), int(round((y1 - y0) * esc))
    sx = x0 + (np.arange(W) + 0.5) / esc
    sy = y0 + (np.arange(H) + 0.5) / esc
    SX, SY = np.meshgrid(sx, sy)
    N = W * H
    R = _rot(yaw, pitch)
    o = np.stack([SX.ravel(), SY.ravel(), np.full(N, -100.0)], 1) @ R  # = R^T aplicado a cada raio
    d = R.T @ np.array([0.0, 0.0, 1.0])
    d_seguro = np.where(np.abs(d) < 1e-9, 1e-9, d)
    luz = np.array([0.35, 0.55, 0.75])
    luz /= np.linalg.norm(luz)

    melhor_t = np.full(N, np.inf)
    rgb = np.zeros((N, 3))
    alfa = np.zeros(N)
    skin_f = skin.astype(float) / 255
    marca_f = None if marca is None else marca.astype(float) / 255
    brilho_f = None if brilho is None else brilho.astype(float) / 255

    for cx in corpo(alex):
        t1 = (cx["lo"] - o) / d_seguro
        t2 = (cx["hi"] - o) / d_seguro
        tmin, tmax = np.minimum(t1, t2), np.maximum(t1, t2)
        t_in, t_out = tmin.max(1), tmax.min(1)
        bate = (t_in <= t_out) & (t_out > 0) & (t_in < melhor_t)
        if not bate.any():
            continue
        eixo = tmin.argmax(1)
        p = o + t_in[:, None] * d
        for f in cx["faces"]:
            ax = _EIXO[f["face"]]
            if (d[ax] > 0) != (f["face"] in _NEG):
                continue
            m = bate & (eixo == ax)
            if not m.any():
                continue
            A, B, C = f["A"], f["B"], f["C"]
            AB, AC = B - A, C - A
            q = p[m] - A
            s = (q @ AB) / (AB @ AB)
            t = (q @ AC) / (AC @ AC)
            umin, vmin, umax, vmax = f["uv"]
            uf, vf = umin + s * (umax - umin), vmin + t * (vmax - vmin)
            u = np.clip(np.floor(uf).astype(int), min(umin, umax), max(umin, umax) - 1)
            v = np.clip(np.floor(vf).astype(int), min(vmin, vmax), max(vmin, vmax) - 1)
            # a marca pode ter mais resolucao que a skin (128x128 = fator 2), mesmo molde
            fonte_f = marca if marca is not None else brilho
            F = (fonte_f.shape[0] // 64) if fonte_f is not None else 1
            uF = np.clip(np.floor(uf * F).astype(int), min(umin, umax) * F, max(umin, umax) * F - 1)
            vF = np.clip(np.floor(vf * F).astype(int), min(vmin, vmax) * F, max(vmin, vmax) * F - 1)
            cor = skin_f[v, u]
            n_cam = R @ f["normal"]
            lam = max(0.0, -float(n_cam @ luz))
            sombra = 0.55 + 0.45 * lam
            if escuro:
                sombra *= 0.2
            c = cor[:, :3].copy()
            if marca_f is not None:
                mc = marca_f[vF, uF]
                a = mc[:, 3:4]
                c = c * (1 - a) + mc[:, :3] * a
            c = c * sombra
            if brilho_f is not None:
                g = brilho_f[vF, uF]
                ga = g[:, 3:4] * pulso
                c = c * (1 - ga) + g[:, :3] * ga
            idx = np.nonzero(m)[0]
            rgb[idx] = c
            alfa[idx] = cor[:, 3]
            melhor_t[idx] = t_in[idx]
    out = np.zeros((H, W, 4), np.uint8)
    out[..., :3] = np.clip(np.round(rgb * 255), 0, 255).reshape(H, W, 3)
    out[..., 3] = np.clip(np.round(alfa * 255), 0, 255).reshape(H, W)
    return out
