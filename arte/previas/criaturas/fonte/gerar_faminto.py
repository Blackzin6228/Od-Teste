#!/usr/bin/env python3
"""PRÉVIA do Faminto (Vila Calada), conceitos A e B, em 32 px por bloco. Nada vai para src/.

Rodar da raiz: bash ferramentas/faminto.sh
Saída: previas-modelos/faminto/<a|b>/ (geo, animation, png, renders de dia e de noite, silhueta, animações),
       previas-modelos/faminto/*.png (folhas de comparação) e arte/blockbench/faminto_<a|b>.bbmodel.
"""
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
import faminto  # noqa: E402
import noite  # noqa: E402
from montagem import arquivo_animacoes, bbmodel, json_compacto  # noqa: E402

SAIDA = RAIZ / "previas-modelos" / "faminto"
BLOCKBENCH = RAIZ / "arte" / "blockbench"
FATOR = 2  # 2 px por unidade = 32 px por bloco
NOMES = {"a": "CONCEITO A: O QUE SOBROU", "b": "CONCEITO B: O ESFOMEADO"}


def pintar_hd(cria, f=FATOR):
    tex = np.zeros((cria.tex_h * f, cria.tex_w * f, 4), np.uint8)
    for c in cria.cubos():
        if c.partilha is not None:
            continue
        for face, (u, v, fw, fh) in c.retangulos().items():
            W, H = fw * f, fh * f
            for fy in range(H):
                for fx in range(W):
                    px = c.pintor(face, fx, fy, W, H)
                    if px is not None:
                        tex[v * f + fy, u * f + fx] = px
    return tex


def assentar(cria):
    """Põe os pés no chão (y = 0) na pose de descanso: os joelhos dobrados levantam o pé."""
    cria.empacotar_uv()
    m = rg.Modelo(cria.geo())
    pes = [f for f in m.faces() if f["osso"].startswith("canela")]
    dy = -min(float(f["v"][:, 1].min()) for f in pes)
    for o in cria.ossos:
        o.pivo[1] += dy
        for c in o.cubos:
            c.origem[1] += dy
            if c.pivo:
                c.pivo = [c.pivo[0], c.pivo[1] + dy, c.pivo[2]]
    return dy


def ampliar(img, k):
    return np.repeat(np.repeat(img, k, axis=0), k, axis=1)


def gerar(var):
    pasta = SAIDA / var
    pasta.mkdir(parents=True, exist_ok=True)
    cria = faminto.montar(var)
    print(f"  assentar: {assentar(cria):+.2f}")
    tex = pintar_hd(cria)
    anims = faminto.anims(cria)
    nome = cria.nome
    geo = cria.geo((2.5, 2.8, 1.2))
    (pasta / f"{nome}.geo.json").write_text(json_compacto(geo) + "\n", encoding="utf-8")
    (pasta / f"{nome}.animation.json").write_text(json_compacto(arquivo_animacoes(nome, anims)) + "\n", encoding="utf-8")
    rg.salvar_png(pasta / f"{nome}.png", tex)
    rg.salvar_png(pasta / "textura_ampliada.png", ampliar(tex, 3))
    bb = bbmodel(cria, anims, (pasta / f"{nome}.png").read_bytes(), f"{nome}.png", (2.5, 2.8))
    t = bb["textures"][0]
    t["width"], t["height"] = cria.tex_w * FATOR, cria.tex_h * FATOR
    t["relative_path"] = f"../../previas-modelos/faminto/{var}/{nome}.png"
    for a in bb["animations"]:
        if a["name"].endswith(".ajoelhar"):
            a["loop"] = "hold"
    BLOCKBENCH.mkdir(parents=True, exist_ok=True)
    (BLOCKBENCH / f"{nome}.bbmodel").write_text(json.dumps(bb), encoding="utf-8")
    n = len(cria.cubos())
    print(f"{nome}: {n} cubos, {len(cria.ossos)} ossos, UV {cria.tex_w}x{cria.tex_h} (PNG {tex.shape[1]}x{tex.shape[0]})")
    modelo = rg.gerar_previas(pasta / f"{nome}.geo.json", pasta / f"{nome}.png", pasta, pasta / f"{nome}.animation.json",
                              escala=7.0, quadros=8, nome=NOMES[var])
    mn, mx = modelo.caixa()
    print(f"  caixa: L {mx[0]-mn[0]:.1f} x A {mx[1]-mn[1]:.1f} x P {mx[2]-mn[2]:.1f} (16 = 1 bloco), base y={mn[1]:.1f}")
    noite.folha_noite(modelo, tex, pasta / "noite.png", f"{NOMES[var]}: NOITE NA VILA (LUZ DA LUA)", escala=7.0)
    return modelo, tex


def main():
    res = {v: gerar(v) for v in ("a", "b")}
    noite.comparacao_conceitos(res, NOMES, SAIDA / "conceitos_a_b.png")
    noite.escuro_30_blocos(res, NOMES, SAIDA / "no_escuro_de_longe.png")
    antes = RAIZ / "previas-modelos" / "faminto" / "antes_render.png"
    noite.antes_depois(antes if antes.exists() else None, res, NOMES, SAIDA / "antes_depois.png")
    noite.ajoelhados(res, NOMES, SAIDA / "fim_do_dobre.png")
    print("pronto:", SAIDA)


if __name__ == "__main__":
    main()
