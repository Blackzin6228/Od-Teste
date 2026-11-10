"""Prévias de estruturas grandes: vista de cima inteira + prévias por parte (PARTES do modelo).

Uso (na raiz do repositório):
    python3 -I ferramentas/estruturas/previa_partes.py vila_destruida
    bash ferramentas/estruturas/previa.sh vila_destruida

Saída em arte/previas/estruturas/<nome>/:
    00_vista_de_cima.png          planta da vila inteira, norte para cima
    01_iso_sudeste.png / 02_iso_noroeste.png   a vila inteira de longe (escala menor)
    <parte>_frente_sul.png / _costas_norte.png / _lado_oeste.png / _de_cima.png
"""
import os
import sys
import time

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.abspath(os.path.join(AQUI, "..", ".."))
sys.path.insert(0, AQUI)

import render  # noqa: E402
from estrutura import Estrutura  # noqa: E402
from modelos import MODELOS  # noqa: E402


def recorte(est, x0, z0, x1, z1, y_min=0):
    """Pedaço da estrutura (caixa x0..x1, z0..z1), só com os blocos de y >= y_min."""
    sx, sy, _ = est.tam
    novo = Estrutura(f"{est.titulo}_parte", (x1 - x0 + 1, sy - y_min, z1 - z0 + 1), est.semente)
    for (x, y, z), b in est.blocos.items():
        if x0 <= x <= x1 and z0 <= z <= z1 and y >= y_min:
            novo.blocos[(x - x0, y - y_min, z - z0)] = b
    return novo


def salvar(q, pasta, arquivo):
    caminho = os.path.join(pasta, arquivo)
    q.salvar_png(caminho)
    print(f"   prévia: {os.path.relpath(caminho, RAIZ)} ({q.w}x{q.h})")


def main(args):
    nomes = [a for a in args if not a.startswith("--")]
    if not nomes:
        print("uso: previa_partes.py <estrutura>")
        return 2
    for nome in nomes:
        mod = MODELOS[nome]
        t0 = time.time()
        est = mod.construir()
        g = getattr(mod, "G", 0)
        pasta = os.path.join(RAIZ, "arte", "previas", "estruturas", nome)
        os.makedirs(pasta, exist_ok=True)
        sx, sy, sz = est.tam
        acima = recorte(est, 0, 0, sx - 1, sz - 1, y_min=g)
        salvar(render.planta(acima, 0, sy - g - 1, celula=12), pasta, "00_vista_de_cima.png")
        salvar(render.iso(acima, giros=0, escala=10), pasta, "01_iso_sudeste.png")
        salvar(render.iso(acima, giros=2, escala=10), pasta, "02_iso_noroeste.png")
        for parte, x0, z0, x1, z1 in getattr(mod, "PARTES", []):
            p = recorte(est, x0, z0, x1, z1, y_min=0)
            # giros (render.iso): 0 = do sudeste, 2 = do noroeste, 3 = do sudoeste
            salvar(render.iso(p, giros=0, escala=14), pasta, f"{parte}_frente_sul.png")
            salvar(render.iso(p, giros=2, escala=14), pasta, f"{parte}_costas_norte.png")
            salvar(render.iso(p, giros=3, escala=14), pasta, f"{parte}_lado_oeste.png")
            salvar(render.planta(p, 0, sy - 1, celula=14), pasta, f"{parte}_de_cima.png")
            # corte na altura do chão + 3 (vê o interior sem telhado)
            salvar(render.iso(p, giros=3, y_max=g + 3, escala=14), pasta, f"{parte}_corte_interior.png")
        print(f"   pronto em {time.time() - t0:.1f}s")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
