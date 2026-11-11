#!/usr/bin/env python3
"""PRÉVIA das criaturas de Sangue (Retalhado, Sanguessuga-Mãe e Magarefe), versão 5.2: monta geo, textura
(variantes A e B), animações e .bbmodel em código e renderiza as prévias. Nada vai para src/ (o jogo continua
com o modelo provisório até o Nikolas aprovar).

Rodar da raiz: bash ferramentas/criaturas_sangue.sh [retalhado|sanguessuga_mae|magarefe|todas]
Saída: arte/previas/criaturas/<criatura>/ (geo, animation, png, renders) e arte/blockbench/<criatura>.bbmodel.
A folha de comparação (provisório x 5.1 x 5.2) é feita por fonte/comparacao.py (ferramentas/comparar_criaturas_sangue.sh).

Cada criatura mora no próprio módulo: retalhado.py, sanguessuga_mae.py, magarefe.py (montar() + anims()).
Base: lore/criaturas.md, docs/decisoes/2026-11-08-cores-dos-elementos.md (vermelho do Sangue = único acento),
docs/regua-de-qualidade.md. Arte nossa, feita do zero.
"""
import importlib
import json
import sys
from pathlib import Path

import numpy as np

sys.dont_write_bytecode = True
AQUI = Path(__file__).resolve().parent
RAIZ = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else AQUI.parents[3]
sys.path.insert(0, str(RAIZ / "ferramentas"))
sys.path.insert(0, str(AQUI))

import render_geckolib as rg  # noqa: E402
import montagem  # noqa: E402
from montagem import arquivo_animacoes, bbmodel, json_compacto  # noqa: E402

PREVIAS = RAIZ / "arte" / "previas" / "criaturas"
BLOCKBENCH = RAIZ / "arte" / "blockbench"

# nome -> (módulo, caixa visível do Blockbench (largura, altura, deslocamento))
CRIATURAS = {
    "retalhado": ("retalhado", (3, 3.2, 1.5)),
    "sanguessuga_mae": ("sanguessuga_mae", (4, 2.5, 1)),
    "magarefe": ("magarefe", (3, 4, 1.8)),
}
SEMENTE_VARIANTE_B = 101


def ampliar(img, fator):
    return np.repeat(np.repeat(img, fator, axis=0), fator, axis=1)


def gerar(nome, modulo, caixa_visivel):
    pasta = PREVIAS / nome
    pasta.mkdir(parents=True, exist_ok=True)
    BLOCKBENCH.mkdir(parents=True, exist_ok=True)
    m = importlib.import_module(modulo)
    cria = m.montar()
    cria.empacotar_uv()
    montagem.DESLOCAMENTO_SEMENTE = 0
    tex, glow = cria.pintar()
    montagem.DESLOCAMENTO_SEMENTE = SEMENTE_VARIANTE_B
    tex_b, _ = cria.pintar()
    montagem.DESLOCAMENTO_SEMENTE = 0
    anims = m.anims()
    geo = cria.geo(caixa_visivel)
    (pasta / f"{nome}.geo.json").write_text(json_compacto(geo) + "\n", encoding="utf-8")
    (pasta / f"{nome}.animation.json").write_text(json_compacto(arquivo_animacoes(nome, anims)) + "\n", encoding="utf-8")
    rg.salvar_png(pasta / f"{nome}.png", tex)
    rg.salvar_png(pasta / f"{nome}_b.png", tex_b)
    if glow is not None:
        rg.salvar_png(pasta / f"{nome}_glowmask.png", glow)
    rg.salvar_png(pasta / "textura_ampliada.png", ampliar(tex, 4))
    png_bytes = (pasta / f"{nome}.png").read_bytes()
    (BLOCKBENCH / f"{nome}.bbmodel").write_text(json.dumps(bbmodel(cria, anims, png_bytes, f"{nome}.png", caixa_visivel[:2])),
                                                encoding="utf-8")
    n_cubos = len(cria.cubos())
    print(f"{nome}: {n_cubos} cubos, {len(cria.ossos)} ossos, textura {cria.tex_w}x{cria.tex_h}, {len(anims)} animações")
    if n_cubos > 60:
        print(f"  AVISO: régua pede até ~60 cubos para criatura comum ({n_cubos})")
    modelo = rg.gerar_previas(pasta / f"{nome}.geo.json", pasta / f"{nome}.png", pasta, pasta / f"{nome}.animation.json",
                              pasta / f"{nome}_glowmask.png" if glow is not None else None, escala=6.0, quadros=8, nome=nome)
    mn, mx = modelo.caixa()
    print(f"  caixa: largura {mx[0]-mn[0]:.1f} x altura {mx[1]-mn[1]:.1f} x fundo {mx[2]-mn[2]:.1f} (unidades; 16 = 1 bloco); "
          f"base em y={mn[1]:.1f}")
    # variantes A e B lado a lado (frente e 3/4)
    tam = rg.tamanho_para(modelo, 5.0)
    cen = rg.centro_para(modelo)
    linha = []
    for rot, img_tex in (("A", tex), ("B", tex_b)):
        for vista, az, el in (("FRENTE", 0, 0), ("3/4", 35, 22)):
            linha.append(rg.renderizar(modelo, img_tex, rg.Camera(az, el), glow=glow, escala=5.0, tamanho=tam, centro=cen,
                                       rotulo=f"VARIANTE {rot}: {vista}"))
    folha = rg.juntar_vertical([rg.faixa_titulo(f"{nome.upper()}: VARIANTES DE TEXTURA (MESMO MODELO)",
                                                sum(i.shape[1] for i in linha) + 18), rg.juntar_horizontal(linha)])
    rg.salvar_png(pasta / "variantes.png", folha)
    return modelo


def main():
    alvo = sys.argv[2] if len(sys.argv) > 2 else "todas"
    for nome, (modulo, caixa) in CRIATURAS.items():
        if alvo in (nome, "todas"):
            gerar(nome, modulo, caixa)
    print("pronto:", PREVIAS)


if __name__ == "__main__":
    main()
