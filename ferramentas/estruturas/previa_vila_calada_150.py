"""Vila Calada 150 x 150: centro de 100 intacto + arredores. Gera os .nbt validados, a lista de pistas e
marcadores e as prévias PNG em arte/vila-calada-150/.

Uso (na raiz do repositório):
    python3 -I ferramentas/estruturas/previa_vila_calada_150.py               # tudo
    python3 -I ferramentas/estruturas/previa_vila_calada_150.py --sem-previa  # só .nbt e json
    python3 -I ferramentas/estruturas/previa_vila_calada_150.py --so-geral    # .nbt + vista de cima e isométricas
    python3 -I ferramentas/estruturas/previa_vila_calada_150.py --so-partes   # .nbt + prévias de perto

Saída em arte/vila-calada-150/:
    vila_calada_150.nbt                     a vila inteira (150 x 27 x 150). Passa do limite de colocação em
                                            partes do mod (524.288 blocos): serve para o Create e para a prévia
    partes/vila_calada_arredores_<faixa>.nbt  norte, sul, oeste, leste: cada faixa cabe no mod. O centro é o
                                            arte/vila-calada-100/vila_calada.nbt, deslocado de (25, 0, 25)
    vila_calada_150.partes.json             as peças e a origem de cada uma dentro dos 150
    vila_calada_150.pistas.json             pistas (centro deslocado + arredores) e os marcadores de recompensa
    00_vista_de_cima.png, 01_iso_sudeste.png, 02_iso_noroeste.png   a vila inteira
    <parte>_sudeste.png, <parte>_noroeste.png                        arredores de perto (PARTES do modelo)

A prévia pinta de grama o chão do mundo onde o arquivo não grava nada (a emenda entre o centro e os arredores
e as bordas irregulares): no jogo, ali fica o terreno que já existe.
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
from estrutura import Estrutura  # noqa: E402
from modelos import vila_calada as V  # noqa: E402
from modelos import vila_calada_150 as M  # noqa: E402
from previa_vila_calada import BLOCOS_POR_TICK, fatias  # noqa: E402

SAIDA = os.path.join(RAIZ, "arte", "vila-calada-150")
PARTES_DIR = os.path.join(SAIDA, "partes")
LIMITE_MOD = 524_288  # Estruturas.VOLUME_MAXIMO_PARTES


def conferir_centro(est):
    """O centro dos 150 tem de ser, bloco a bloco, a vila de 100 (deslocada de BORDA)."""
    centro = V.construir()
    d = M.BORDA
    for (x, y, z), b in centro.blocos.items():
        if est.blocos.get((x + d, y, z + d)) != b:
            raise AssertionError(f"centro alterado em {(x + d, y, z + d)}")
    x0, z0, x1, z1 = M.CENTRO
    dentro = sum(1 for (x, y, z) in est.blocos if x0 <= x <= x1 and z0 <= z <= z1)
    if dentro != len(centro.blocos):
        raise AssertionError(f"o centro dos 150 tem {dentro} blocos; a vila de 100 tem {len(centro.blocos)}")
    return len(centro.blocos)


def recortar(est, x0, z0, x1, z1, titulo):
    """Pedaço (x0..x1, z0..z1) com os blocos, dados e marcadores; a altura é a do pedaço."""
    alto = max([y for (x, y, z) in est.blocos if x0 <= x <= x1 and z0 <= z <= z1] + [0]) + 1
    p = Estrutura(titulo, (x1 - x0 + 1, alto, z1 - z0 + 1), est.semente)
    for (x, y, z), b in est.blocos.items():
        if x0 <= x <= x1 and z0 <= z <= z1:
            p.blocos[(x - x0, y, z - z0)] = b
            if (x, y, z) in est.dados:
                p.dados[(x - x0, y, z - z0)] = est.dados[(x, y, z)]
    return p


def com_chao_do_mundo(est):
    """Cópia para a prévia: grama onde o arquivo não tem nada na coluna (o terreno do mundo)."""
    q = Estrutura(est.titulo + "_previa", est.tam, est.semente)
    q.blocos = dict(est.blocos)
    colunas = {(x, z) for (x, y, z) in est.blocos}
    grama = q._resolver("grass_block", {"snowy": False})
    for x in range(est.tam[0]):
        for z in range(est.tam[2]):
            if (x, z) not in colunas:
                q.blocos[(x, 0, z)] = grama
    return q


def salvar_nbt(est, caminho):
    nbt_io.salvar(caminho, est.para_nbt())
    erros, avisos, resumo = validar.validar(caminho, tam_esperado=est.tam)
    validar.imprimir(os.path.relpath(caminho, RAIZ), erros, avisos, resumo)
    sx, sy, sz = est.tam
    volume = sx * sy * sz
    cabe = "cabe no mod" if volume <= LIMITE_MOD else f"NÃO cabe no mod (limite {LIMITE_MOD})"
    print(f"   caixa {sx} x {sy} x {sz} = {volume} blocos; {cabe}; "
          f"colocação em partes ({BLOCOS_POR_TICK}/tick): {fatias(sx, sy, sz)} fatias")
    return not erros


def salvar_png(q, arquivo):
    caminho = os.path.join(SAIDA, arquivo)
    q.salvar_png(caminho)
    print(f"   prévia: {os.path.relpath(caminho, RAIZ)} ({q.w}x{q.h})")


def main(args):
    t0 = time.time()
    est = M.construir()
    n_centro = conferir_centro(est)
    print(f"   centro intacto: {n_centro} blocos iguais à vila de 100; arredores: {len(est.blocos) - n_centro} blocos")
    os.makedirs(PARTES_DIR, exist_ok=True)

    ok = salvar_nbt(est, os.path.join(SAIDA, M.NOME + ".nbt"))
    partes = [{"peca": "centro", "arquivo": "arte/vila-calada-100/vila_calada.nbt",
               "origem": [M.BORDA, 0, M.BORDA], "tamanho": list(V.TAM),
               "nota": "a vila de 100, sem mudar nada"}]
    for nome, x0, z0, x1, z1 in M.FAIXAS:
        p = recortar(est, x0, z0, x1, z1, f"vila_calada_arredores_{nome}")
        arquivo = os.path.join(PARTES_DIR, p.titulo + ".nbt")
        ok &= salvar_nbt(p, arquivo)
        partes.append({"peca": nome, "arquivo": os.path.relpath(arquivo, RAIZ), "origem": [x0, 0, z0],
                       "tamanho": list(p.tam), "marcadores": [mk["metadata"] for mk in est.marcadores
                                                              if x0 <= mk["pos"][0] <= x1 and z0 <= mk["pos"][2] <= z1]})
    if not ok:
        return 1

    with open(os.path.join(SAIDA, M.NOME + ".partes.json"), "w", encoding="utf-8") as f:
        json.dump({"estrutura": M.NOME, "tamanho": list(est.tam),
                   "origem": "canto noroeste de baixo (x leste, y cima, z sul); a camada y 0 assenta em cima do terreno",
                   "como_montar": "colocar cada peça com a origem dela somada ao canto da vila inteira; "
                                  "todas assentam na mesma altura (y 0 = chão)",
                   "partes": partes}, f, ensure_ascii=False, indent=2)
        f.write("\n")
    with open(os.path.join(SAIDA, M.NOME + ".pistas.json"), "w", encoding="utf-8") as f:
        json.dump({"estrutura": M.NOME, "tamanho": list(est.tam),
                   "origem": "canto noroeste de baixo (x leste, y cima, z sul); a camada y 0 assenta em cima do terreno",
                   "centro": {"origem": [M.BORDA, 0, M.BORDA], "nota": "as pistas do centro já vêm deslocadas"},
                   "marcadores": {"convencao": "bloco de estrutura em modo DATA, metadata = nome, UM bloco ACIMA "
                                               "do baú/jukebox (alvo). O mod ignora o bloco de estrutura ao colocar "
                                               "e enche o alvo.",
                                  "lista": est.marcadores},
                   "pistas": est.pistas}, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"   marcadores: {', '.join(mk['metadata'] for mk in est.marcadores)}")

    geral = "--sem-previa" not in args and "--so-partes" not in args
    perto = "--sem-previa" not in args and "--so-geral" not in args
    if geral or perto:
        prev = com_chao_do_mundo(est)
        sy = est.tam[1]
        if geral:
            salvar_png(render.planta(prev, 0, sy - 1, celula=8), "00_vista_de_cima.png")
            salvar_png(render.iso(prev, giros=0, escala=7), "01_iso_sudeste.png")
            salvar_png(render.iso(prev, giros=2, escala=7), "02_iso_noroeste.png")
        if perto:
            for nome, x0, z0, x1, z1 in M.PARTES:
                p = recortar(prev, x0, z0, x1, z1, nome)
                salvar_png(render.iso(p, giros=0, escala=14), f"{nome}_sudeste.png")
                salvar_png(render.iso(p, giros=2, escala=14), f"{nome}_noroeste.png")
    print(f"   pronto em {time.time() - t0:.1f}s")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
