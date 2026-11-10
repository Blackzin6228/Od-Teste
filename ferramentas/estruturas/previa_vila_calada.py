"""Vila Calada 100 x 100: gera o .nbt validado, a lista de pistas e as prévias PNG em arte/vila-calada-100/.

Uso (na raiz do repositório):
    python3 -I ferramentas/estruturas/previa_vila_calada.py              # .nbt + todas as prévias
    python3 -I ferramentas/estruturas/previa_vila_calada.py --sem-previa # só o .nbt e as pistas

Saída em arte/vila-calada-100/:
    vila_calada.nbt, vila_calada.pistas.json
    00_vista_de_cima.png, 01_iso_sudeste.png, 02_iso_noroeste.png   (a vila inteira)
    <parte>_sudeste.png, <parte>_noroeste.png                         (de perto, PARTES do modelo)
    capela_corte_interior.png                                         (capela sem as paredes de cima)

A prévia não usa as texturas do jogo (cores feitas por código): mostra forma, volume e distribuição.
"""
import json
import os
import sys
import time

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.abspath(os.path.join(AQUI, "..", ".."))
sys.path.insert(0, AQUI)

import nbt_io  # noqa: E402
import render  # noqa: E402
import validar  # noqa: E402
from modelos import vila_calada as V  # noqa: E402
from previa_partes import recorte  # noqa: E402

SAIDA = os.path.join(RAIZ, "arte", "vila-calada-100")
BLOCOS_POR_TICK = 8192  # padrão de estruturas.blocos_por_tick (PR #37)


def fatias(sx, sy, sz, limite=BLOCOS_POR_TICK):
    """Mesma conta de Fatias.java (PR #37): quantos ticks a colocação em partes leva."""
    area = sx * sz
    if area <= limite:
        passo_y = max(1, min(sy, limite // area))
        return -(-sy // passo_y)
    passo_x = max(1, limite // sz)
    return sy * -(-sx // passo_x)


def salvar(q, arquivo):
    caminho = os.path.join(SAIDA, arquivo)
    q.salvar_png(caminho)
    print(f"   prévia: {os.path.relpath(caminho, RAIZ)} ({q.w}x{q.h})")


def main(args):
    t0 = time.time()
    est = V.construir()
    os.makedirs(SAIDA, exist_ok=True)
    caminho = os.path.join(SAIDA, V.NOME + ".nbt")
    nbt_io.salvar(caminho, est.para_nbt())
    erros, avisos, resumo = validar.validar(caminho, tam_esperado=est.tam)
    validar.imprimir(os.path.relpath(caminho, RAIZ), erros, avisos, resumo)
    if erros:
        return 1
    with open(os.path.join(SAIDA, V.NOME + ".pistas.json"), "w", encoding="utf-8") as f:
        json.dump({"estrutura": V.NOME, "tamanho": list(est.tam),
                   "origem": "canto noroeste de baixo (x leste, y cima, z sul); a camada y 0 assenta em cima do terreno",
                   "pistas": est.pistas}, f, ensure_ascii=False, indent=2)
        f.write("\n")
    sx, sy, sz = est.tam
    print(f"   caixa {sx} x {sy} x {sz} = {sx * sy * sz} blocos; 1 arquivo; "
          f"colocação em partes (#37, {BLOCOS_POR_TICK}/tick): {fatias(sx, sy, sz)} fatias")

    if "--sem-previa" not in args:
        salvar(render.planta(est, 0, sy - 1, celula=10), "00_vista_de_cima.png")
        salvar(render.iso(est, giros=0, escala=9), "01_iso_sudeste.png")
        salvar(render.iso(est, giros=2, escala=9), "02_iso_noroeste.png")
        for nome, x0, z0, x1, z1 in V.PARTES:
            p = recorte(est, x0, z0, x1, z1)
            salvar(render.iso(p, giros=0, escala=14), f"{nome}_sudeste.png")
            salvar(render.iso(p, giros=2, escala=14), f"{nome}_noroeste.png")
        capela = recorte(est, 40, 17, 64, 35)
        salvar(render.iso(capela, giros=0, y_max=V.PLAT + 3, escala=16), "capela_corte_interior.png")
    print(f"   pronto em {time.time() - t0:.1f}s")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
