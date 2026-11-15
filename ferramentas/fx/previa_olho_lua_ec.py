#!/usr/bin/env python3
"""Prévia do olho na lua com a Lua de Sangue do Enhanced Celestials: antes x depois (mesa Dev).

Roda o MESMO odfx_olho_lua.fsh do mod (o "antes" sai do git, numa revisão dada) sobre um céu sintético de Lua de
Sangue do EC: céu escuro puxado para vermelho, lua quadrada pequena e vermelha. A lua é posta onde o jogo a desenha;
o "antes" recebe o LuaDir com o desvio antigo de 0,05 em z (como o FxOlhoLua mandava) e a abertura 0,15 de quando
o olho não encara. Duas vistas: lua média (pitch 35) e lua alta (pitch 72, onde a lágrima virava risco de lado).
Linhas: com profundidade (sem shader pack) e sem profundidade (shader pack que não deixa ler).

Uso:
    python3 ferramentas/fx/previa_olho_lua_ec.py --antes b488cc6 --saida previas-fx/8_olho_lua_de_sangue_ec_antes_depois.png
"""
import argparse
import math
import os
import subprocess
import sys
import tempfile

import moderngl
import numpy as np
from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import previa_shader  # noqa: E402
from previa_shader import Efeito, perspectiva, vista, dir_de, RAIZ  # noqa: E402

LARG, ALT = 960, 540
FOV_Y = 70.0
MEIA_LUA = 0.03                      # lua quadrada pequena (a do EC na Lua de Sangue, como no print do Nikolas)
COR_LUA = np.array([0.78, 0.16, 0.10], dtype=np.float32)


class Cena:
    """Céu de Lua de Sangue do EC, com profundidade (céu = 1) e um galho escuro na frente de parte do céu."""

    def __init__(self, ctx, proj, view, lua):
        inv = np.linalg.inv(proj @ view)
        ys, xs = np.mgrid[0:ALT, 0:LARG].astype(np.float32)
        u = (xs + 0.5) / LARG * 2 - 1
        v = (ys + 0.5) / ALT * 2 - 1
        clip = np.stack([u, v, np.ones_like(u), np.ones_like(u)], axis=-1)
        w = clip @ inv.T
        rd = w[..., :3] / w[..., 3:4]
        rd /= np.linalg.norm(rd, axis=-1, keepdims=True)
        alto = np.clip(rd[..., 1], 0, 1)[..., None]
        cor = (np.array([0.10, 0.025, 0.025]) * (1 - alto) + np.array([0.035, 0.008, 0.010]) * alto).astype(np.float32)
        rng = np.random.default_rng(3)
        estrelas = rng.random((ALT, LARG)) > 0.9985
        cor[estrelas] = [0.55, 0.35, 0.35]
        # lua quadrada no plano da lua (eixos como o céu vanilla: lados alinhados ao mundo)
        l = lua / np.linalg.norm(lua)
        dx = np.cross(l, [0, 1, 0.001])
        dx /= np.linalg.norm(dx)
        dy = np.cross(dx, l)
        dl = rd @ l
        m = np.stack([rd @ dx, rd @ dy], axis=-1) / np.maximum(dl, 1e-4)[..., None]
        dentro = (dl > 0) & (np.max(np.abs(m), axis=-1) < MEIA_LUA)
        mancha = 0.8 + 0.2 * rng.random((ALT, LARG))
        cor[dentro] = COR_LUA * mancha[dentro][..., None]
        halo = np.clip(dl, 0, 1) ** 900
        cor += (halo * 0.12)[..., None] * np.array([0.9, 0.2, 0.15], dtype=np.float32)
        prof = np.ones((ALT, LARG), dtype=np.float32)
        # galho escuro (prova de que o que está na frente não é pintado)
        galho = (np.abs(ys - (0.18 * xs + 40)) < 5) & (xs < LARG * 0.35)
        cor[galho] = [0.02, 0.015, 0.012]
        prof[galho] = 0.97
        rgba = np.concatenate([np.clip(cor, 0, 1), np.ones((ALT, LARG, 1), np.float32)], axis=-1)
        self.cor = ctx.texture((LARG, ALT), 4, (rgba * 255).astype(np.uint8).tobytes(), dtype="f1")
        self.prof = ctx.texture((LARG, ALT), 1, prof.tobytes(), dtype="f4")


def lua_na_tela(proj, view, l):
    c = proj @ view @ np.array([l[0], l[1], l[2], 0.0])
    return (c[0] / c[3] * 0.5 + 0.5, c[1] / c[3] * 0.5 + 0.5, 1.0) if c[3] > 1e-4 else (0.5, 0.5, 0.0)


def shaders_da_revisao(rev):
    pasta = tempfile.mkdtemp(prefix="olho_antes_")
    base = "src/main/resources/assets/odteste/shaders"
    arq = subprocess.run(["git", "-C", RAIZ, "archive", rev, base], check=True, capture_output=True).stdout
    subprocess.run(["tar", "-x", "-C", pasta], input=arq, check=True)
    return os.path.join(pasta, base)


def quadro(ctx, pitch, prof, antes, pasta_antes):
    proj = perspectiva(FOV_Y, LARG / ALT, 0.05, 512.0)
    view, frente = vista(-20.0, pitch)
    lua = dir_de(-12.0, pitch + 4.0)                       # a lua de verdade, perto do meio da tela
    cena = Cena(ctx, proj, view, lua)
    original = previa_shader.SHADERS
    if antes:
        previa_shader.SHADERS = pasta_antes
    try:
        fx = Efeito(ctx, "odfx_olho_lua", LARG, ALT)
    finally:
        previa_shader.SHADERS = original
    if antes:
        # o FxOlhoLua antigo mandava a lua com +0,05 em z e abertura 0,15 sem encarar
        l = lua + np.array([0.0, 0.0, 0.05], dtype=np.float32)
        l = l / np.linalg.norm(l)
        abertura = 0.15
    else:
        l = lua
        abertura = 0.8
    olhar = dir_de(60.0, -10.0) * 0.8 - lua * 0.2           # vagueando (não encara)
    u = {"Tempo": 9.0, "ComProfundidade": 1.0 if prof else 0.0, "Forca": 1.0, "LuaDir": tuple(float(x) for x in l),
         "Olhar": tuple(float(x) for x in olhar), "Abertura": abertura, "Variante": 2.0,
         "LuaTela": lua_na_tela(proj, view, lua)}
    img = fx.desenhar(cena, proj, view, u)
    cx, cy = lua_na_tela(proj, view, lua)[:2]
    cx, cy = int(cx * LARG), int((1 - cy) * ALT)
    caixa = (cx - 200, cy - 125, cx + 200, cy + 125)
    return img.crop(caixa).resize((560, 350), Image.LANCZOS)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--antes", default="HEAD", help="revisão do git com o shader antigo")
    ap.add_argument("--saida", default=os.path.join(RAIZ, "previas-fx", "8_olho_lua_de_sangue_ec_antes_depois.png"))
    a = ap.parse_args()
    ctx = moderngl.create_standalone_context(backend="egl", require=330)
    pasta_antes = shaders_da_revisao(a.antes)
    colunas = (("ANTES - lua média", 35.0, True), ("DEPOIS - lua média", 35.0, False),
               ("ANTES - lua alta", 72.0, True), ("DEPOIS - lua alta", 72.0, False))
    linhas = (("com profundidade (sem shader pack)", True), ("sem profundidade (shader pack)", False))
    folha = Image.new("RGB", (10 + 4 * 570, 30 + 2 * 380), (12, 10, 12))
    d = ImageDraw.Draw(folha)
    try:
        fonte = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 14)
    except OSError:
        fonte = ImageFont.load_default()
    d.text((10, 6), "Olho na lua (desenho 2) na Lua de Sangue do Enhanced Celestials - lua vermelha e pequena, pupila vagueando",
           fill=(230, 210, 210), font=fonte)
    for i, (titulo_l, prof) in enumerate(linhas):
        for j, (titulo_c, pitch, antes) in enumerate(colunas):
            x, y = 10 + j * 570, 30 + i * 380
            d.text((x, y), titulo_c + " | " + titulo_l, fill=(220, 90, 80) if antes else (120, 220, 120), font=fonte)
            folha.paste(quadro(ctx, pitch, prof, antes, pasta_antes), (x, y + 18))
    os.makedirs(os.path.dirname(a.saida), exist_ok=True)
    folha.save(a.saida)
    print("ok:", a.saida)


if __name__ == "__main__":
    sys.exit(main())
