#!/usr/bin/env python3
"""Folha de comparação das criaturas de Sangue: PROVISÓRIO (o que está no main/jogo hoje) x 5.1 (branch antes da
revisão) x 5.2 (revisada), lado a lado, nos MESMOS ângulos das prévias (frente, lado direito, costas, 3/4) e na
mesma escala, mais a linha de silhueta.

Rodar da raiz: bash ferramentas/comparar_criaturas_sangue.sh
Entrada: src/main/resources/assets/odteste/geo/entity/<c>.geo.json (provisório; textura refeita em numpy igual à
gerada em código pelo jogo, cliente/ModeloCriatura.java), arte/previas/criaturas/comparacao/v5_1/<c>/ (cópia da 5.1)
e arte/previas/criaturas/<c>/ (5.2 atual).
Saída: arte/previas/criaturas/comparacao/<c>.png e comparacao/folha_sangue.png.
"""
import sys
from pathlib import Path

import numpy as np

sys.dont_write_bytecode = True
AQUI = Path(__file__).resolve().parent
RAIZ = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else AQUI.parents[3]
sys.path.insert(0, str(RAIZ / "ferramentas"))
sys.path.insert(0, str(AQUI))

import render_geckolib as rg  # noqa: E402
from montagem import cor  # noqa: E402

PREVIAS = RAIZ / "arte" / "previas" / "criaturas"
COMPARACAO = PREVIAS / "comparacao"
GEO_JOGO = RAIZ / "src" / "main" / "resources" / "assets" / "odteste" / "geo" / "entity"
ESCALA = 5.0
VISTAS = [("FRENTE", 0, 0), ("LADO DIREITO", 90, 0), ("COSTAS", 180, 0), ("3/4", 35, 22)]


def textura_provisoria(base, sombra, brilho, costura, semente, lado=256):
    """Porta de ModeloCriatura.gerar (Java) para numpy: a mesma textura que o jogo mostra hoje."""
    ys, xs = np.mgrid[0:lado, 0:lado].astype(np.uint32)

    def ruido(x, y):
        h = (x * np.uint32(374761393) + y * np.uint32(668265263) + np.uint32(semente) * np.uint32(982451653))
        h = ((h ^ (h >> np.uint32(13))) * np.uint32(1274126177))
        return (h ^ (h >> np.uint32(16))) & np.uint32(0x7FFFFFFF)

    with np.errstate(over="ignore"):
        h = ruido(xs >> 1, ys >> 1)
        img = np.empty((lado, lado, 4), np.uint8)
        img[:, :] = cor(base)
        img[h % 5 == 0] = cor(brilho)
        img[h % 7 == 0] = cor(sombra)
        veia = ((xs + (ys >> 2) * 3 + (ruido(xs >> 3, ys >> 3) & 3)) % 37) == 0
        img[veia] = cor(sombra)
        fy = ys % 24
        cost = (fy == 12) | ((fy >= 10) & (fy <= 14) & (xs % 6 == 0))
        img[cost] = cor(costura)
    return img


PROVISORIOS = {
    "retalhado": ("#6E1414", "#2A0000", "#9E2A22", "#1A0E0A", 11),
    "sanguessuga_mae": ("#4A1410", "#1E0504", "#8B3A2E", "#D8C8B0", 23),
}


def carregar(nome, pasta):
    geo = pasta / f"{nome}.geo.json"
    if not geo.exists():
        return None
    tex = rg.ler_png(pasta / f"{nome}.png")
    brilho = pasta / f"{nome}_glowmask.png"
    glow = rg.ler_png(brilho) if brilho.exists() else None
    return rg.Modelo(geo), tex, glow


def colunas(nome):
    saida = []
    if nome in PROVISORIOS and (GEO_JOGO / f"{nome}.geo.json").exists():
        saida.append(("PROVISORIO (JOGO HOJE)", rg.Modelo(GEO_JOGO / f"{nome}.geo.json"), textura_provisoria(*PROVISORIOS[nome]), None))
    v51 = carregar(nome, COMPARACAO / "v5_1" / nome)
    if v51:
        saida.append(("5.1 (ANTES DA REVISAO)", *v51))
    v52 = carregar(nome, PREVIAS / nome)
    if v52:
        saida.append(("5.2 (REVISADA)", *v52))
    return saida


def folha(nome):
    cols = colunas(nome)
    if not cols:
        return None
    tams = [rg.tamanho_para(m, ESCALA) for _, m, _, _ in cols]
    tam = (max(t[0] for t in tams), max(t[1] for t in tams))
    linhas = []
    for rot, az, el in VISTAS:
        cam = rg.Camera(az, el)
        imgs = [rg.renderizar(m, tex, cam, glow=glow, escala=ESCALA, tamanho=tam, centro=rg.centro_para(m), rotulo=f"{titulo}\n{rot}")
                for titulo, m, tex, glow in cols]
        linhas.append(rg.juntar_horizontal(imgs, espaco=4))
    # silhueta (frente), fundo claro: teste da régua
    sil = [rg.renderizar(m, tex, rg.Camera(0, 0), escala=ESCALA, tamanho=tam, centro=rg.centro_para(m), fundo=(214, 214, 210, 255),
                         silhueta=True, regua=False) for _, m, tex, _ in cols]
    for s, (titulo, _, _, _) in zip(sil, cols):
        rg.texto(s, (8, 8), f"{titulo}\nSILHUETA", (40, 40, 40, 255), 2)
    linhas.append(rg.juntar_horizontal(sil, espaco=4, fundo=(214, 214, 210, 255)))
    largura = linhas[0].shape[1]
    versoes = " X ".join(t.split(" ")[0] for t, _, _, _ in cols)
    img = rg.juntar_vertical([rg.faixa_titulo(f"{nome.upper()}: {versoes} (MESMA ESCALA)", largura)] + linhas)
    rg.salvar_png(COMPARACAO / f"{nome}.png", img)
    print("comparação:", COMPARACAO / f"{nome}.png", f"({len(cols)} colunas)")
    return img


def main():
    folhas = [f for f in (folha(n) for n in ("retalhado", "sanguessuga_mae", "magarefe")) if f is not None]
    if folhas:
        rg.salvar_png(COMPARACAO / "folha_sangue.png", rg.juntar_vertical(folhas, espaco=18))
        print("folha completa:", COMPARACAO / "folha_sangue.png")


if __name__ == "__main__":
    main()
