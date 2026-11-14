"""Gera as estruturas da história: .nbt validado + prévias PNG + lista de pistas.

Uso (na raiz do repositório):
    python3 ferramentas/estruturas/gerar.py              # todas
    python3 ferramentas/estruturas/gerar.py capela_fazenda
    python3 ferramentas/estruturas/gerar.py --sem-previa capela_fazenda

Saída:
    arte/estruturas/<nome>.nbt            (vai para a pasta schematics/ do Create ou para /place template)
    arte/estruturas/<nome>.pistas.json    (onde a Lore/Dev colocam livros, recados, eventos)
    arte/previas/estruturas/<nome>_*.png  (prévias para o Nikolas aprovar)
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
from modelos import MODELOS  # noqa: E402

VISTAS = [
    # (sufixo, giros) — ver render.iso
    ("iso_frente_noroeste", 2),
    ("iso_lado_nordeste", 1),
    ("iso_costas_sudeste", 0),
    ("iso_lado_sudoeste", 3),
]


def gerar(nome, previa=True):
    mod = MODELOS[nome]
    t0 = time.time()
    est = mod.construir()
    pasta_nbt = os.path.join(RAIZ, "arte", "estruturas")
    pasta_png = os.path.join(RAIZ, "arte", "previas", "estruturas")
    os.makedirs(pasta_nbt, exist_ok=True)
    os.makedirs(pasta_png, exist_ok=True)

    caminho = os.path.join(pasta_nbt, nome + ".nbt")
    nbt_io.salvar(caminho, est.para_nbt())
    erros, avisos, resumo = validar.validar(caminho, tam_esperado=est.tam)
    validar.imprimir(os.path.relpath(caminho, RAIZ), erros, avisos, resumo)
    if erros:
        return False

    with open(os.path.join(pasta_nbt, nome + ".pistas.json"), "w", encoding="utf-8") as f:
        json.dump({"estrutura": nome, "tamanho": list(est.tam),
                   "origem": "canto noroeste de baixo (x leste, y cima, z sul)",
                   "pistas": est.pistas}, f, ensure_ascii=False, indent=2)
        f.write("\n")

    if previa:
        for sufixo, giros in VISTAS:
            _salvar(render.iso(est, giros=giros), pasta_png, f"{nome}_{sufixo}.png")
        for corte, y0, y1 in mod.CORTES:
            _salvar(render.planta(est, y0, y1), pasta_png, f"{nome}_planta_{corte}.png")
            _salvar(render.iso(est, giros=0, y_max=y1), pasta_png, f"{nome}_corte_{corte}_sudeste.png")
            _salvar(render.iso(est, giros=2, y_max=y1), pasta_png, f"{nome}_corte_{corte}_noroeste.png")
    print(f"   pronto em {time.time() - t0:.1f}s")
    return True


def _salvar(quadro, pasta, arquivo):
    caminho = os.path.join(pasta, arquivo)
    quadro.salvar_png(caminho)
    print(f"   prévia: {os.path.relpath(caminho, RAIZ)} ({quadro.w}x{quadro.h})")


def main(args):
    previa = "--sem-previa" not in args
    # sem nome: todas, menos as que têm gerador próprio (GERAR_COM_TODAS = False, ex.: vila_calada_150)
    nomes = ([a for a in args if not a.startswith("--")]
             or [n for n, mod in MODELOS.items() if getattr(mod, "GERAR_COM_TODAS", True)])
    desconhecidos = [n for n in nomes if n not in MODELOS]
    if desconhecidos:
        print(f"estrutura desconhecida: {', '.join(desconhecidos)}. Existem: {', '.join(MODELOS)}")
        return 2
    ok = all([gerar(n, previa) for n in nomes])
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
