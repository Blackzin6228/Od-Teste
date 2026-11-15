"""Vila Calada 150 x 150, versão 2 (vila morta): gera os .nbt validados, pistas/marcadores e as prévias
(antes x depois, de dia e de noite com a névoa carmesim) em arte/vila-calada-150-v2/.

Uso (na raiz do repositório):
    python3 -I ferramentas/estruturas/previa_vila_calada_150_v2.py              # tudo
    python3 -I ferramentas/estruturas/previa_vila_calada_150_v2.py --sem-previa # só .nbt e json
    python3 -I ferramentas/estruturas/previa_vila_calada_150_v2.py --so-3d      # .nbt + comparações 3D

As comparações 3D precisam de numpy, Pillow e moderngl (EGL). O resto é Python puro, como a ferramenta.

Saída em arte/vila-calada-150-v2/:
    vila_calada_150_v2.nbt                 a vila inteira (Create e prévia)
    partes/vila_calada_v2_centro.nbt       o centro de 100 (cabe no mod)
    partes/vila_calada_v2_arredores_<faixa>.nbt   norte, sul, oeste, leste
    vila_calada_150_v2.partes.json, vila_calada_150_v2.pistas.json
    comparar_<lugar>.png                   antes (v1) x depois (v2), dia em cima, noite embaixo
    01_iso_sudeste.png, 00_vista_de_cima.png
"""
import json
import os
import sys
import time

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.abspath(os.path.join(AQUI, "..", ".."))
sys.path.insert(0, AQUI)

import render  # noqa: E402
from modelos import vila_calada_150 as V1  # noqa: E402
from modelos import vila_calada_150_v2 as M  # noqa: E402
from previa_vila_calada_150 import com_chao_do_mundo, recortar, salvar_nbt  # noqa: E402

SAIDA = os.path.join(RAIZ, "arte", "vila-calada-150-v2")
PARTES_DIR = os.path.join(SAIDA, "partes")

# lugares das comparações: nome, título, olho, alvo (coordenadas da vila de 150; y 1 = em pé no chão)
LUGARES = [
    ("praca", "Praça e poço, olhando para a capela e o campanário", (66.5, 3.3, 89.5), (80.0, 6.0, 62.0)),
    ("rua_principal", "Rua principal, do portão para o norte", (75.0, 2.9, 109.0), (75.0, 5.0, 66.0)),
    ("entrada_sul", "Entrada sul: a estrada chegando no portão", (75.5, 3.2, 131.0), (75.0, 3.6, 100.0)),
    ("adro_do_sino", "Adro do Sino: chão largo e paredes altas (efeitos de Sangue)", (66.0, 3.6, 70.0), (85.0, 8.0, 57.0)),
]


def comparar(antes, depois, nome, titulo, olho, alvo):
    from PIL import Image, ImageDraw, ImageFont
    w, h = 960, 540
    fotos = [[antes.foto(olho, alvo, c, (w, h)), depois.foto(olho, alvo, c, (w, h))] for c in ("dia", "noite")]
    barra, margem = 46, 6
    img = Image.new("RGB", (2 * w + margem, 2 * h + margem + barra), (16, 14, 15))
    for i, linha in enumerate(fotos):
        for j, f in enumerate(linha):
            img.paste(f, (j * (w + margem), barra + i * (h + margem)))
    d = ImageDraw.Draw(img)
    try:
        fonte = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 22)
        menor = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 18)
    except OSError:
        fonte = menor = ImageFont.load_default()
    d.text((12, 11), titulo, fill=(236, 230, 226), font=fonte)
    for j, rot in enumerate(("ANTES (v1)", "DEPOIS (v2)")):
        for i, cl in enumerate(("dia", "noite, névoa carmesim")):
            d.text((j * (w + margem) + 12, barra + i * (h + margem) + 10), f"{rot} - {cl}", fill=(250, 246, 240),
                   font=menor, stroke_width=2, stroke_fill=(0, 0, 0))
    caminho = os.path.join(SAIDA, f"comparar_{nome}.png")
    img.save(caminho, optimize=True)
    print(f"   prévia: {os.path.relpath(caminho, RAIZ)}")


def main(args):
    t0 = time.time()
    est = M.construir()
    os.makedirs(PARTES_DIR, exist_ok=True)
    ok = salvar_nbt(est, os.path.join(SAIDA, M.NOME + ".nbt"))
    x0, z0, x1, z1 = M.CENTRO
    centro = recortar(est, x0, z0, x1, z1, "vila_calada_v2_centro")
    centro.tam = (x1 - x0 + 1, M.TAM[1], z1 - z0 + 1)
    arq = os.path.join(PARTES_DIR, centro.titulo + ".nbt")
    ok &= salvar_nbt(centro, arq)
    partes = [{"peca": "centro", "arquivo": os.path.relpath(arq, RAIZ), "origem": [x0, 0, z0],
               "tamanho": list(centro.tam), "marcadores": []}]
    for nome, fx0, fz0, fx1, fz1 in M.FAIXAS:
        p = recortar(est, fx0, fz0, fx1, fz1, f"vila_calada_v2_arredores_{nome}")
        arquivo = os.path.join(PARTES_DIR, p.titulo + ".nbt")
        ok &= salvar_nbt(p, arquivo)
        partes.append({"peca": nome, "arquivo": os.path.relpath(arquivo, RAIZ), "origem": [fx0, 0, fz0],
                       "tamanho": list(p.tam), "marcadores": [mk["metadata"] for mk in est.marcadores
                                                              if fx0 <= mk["pos"][0] <= fx1 and fz0 <= mk["pos"][2] <= fz1]})
    if not ok:
        return 1
    origem = "canto noroeste de baixo (x leste, y cima, z sul); a camada y 0 assenta em cima do terreno"
    with open(os.path.join(SAIDA, M.NOME + ".partes.json"), "w", encoding="utf-8") as f:
        json.dump({"estrutura": M.NOME, "tamanho": list(est.tam), "origem": origem,
                   "como_montar": "colocar cada peça com a origem dela somada ao canto da vila inteira; "
                                  "todas assentam na mesma altura (y 0 = chão)",
                   "partes": partes}, f, ensure_ascii=False, indent=2)
        f.write("\n")
    with open(os.path.join(SAIDA, M.NOME + ".pistas.json"), "w", encoding="utf-8") as f:
        json.dump({"estrutura": M.NOME, "tamanho": list(est.tam), "origem": origem,
                   "nota": "mesmas posições da v1 (vila_calada_150.pistas.json) mais as duas do Adro do Sino",
                   "marcadores": {"convencao": "bloco de estrutura em modo DATA, metadata = nome, UM bloco ACIMA "
                                               "do baú/jukebox (alvo). O mod ignora o bloco de estrutura ao colocar "
                                               "e enche o alvo.",
                                  "lista": est.marcadores},
                   "pistas": est.pistas}, f, ensure_ascii=False, indent=2)
        f.write("\n")

    if "--sem-previa" in args:
        return 0
    if "--so-3d" not in args:
        prev = com_chao_do_mundo(est)
        for arq, q in (("00_vista_de_cima.png", render.planta(prev, 0, est.tam[1] - 1, celula=8)),
                       ("01_iso_sudeste.png", render.iso(prev, giros=0, escala=7))):
            q.salvar_png(os.path.join(SAIDA, arq))
            print(f"   prévia: arte/vila-calada-150-v2/{arq}")
    import render3d
    antes = render3d.Cena(V1.construir())
    depois = render3d.Cena(est)
    so = [a.split("=", 1)[1] for a in args if a.startswith("--lugar=")]
    for nome, titulo, olho, alvo in LUGARES:
        if so and nome not in so:
            continue
        comparar(antes, depois, nome, titulo, olho, alvo)
    print(f"   pronto em {time.time() - t0:.1f}s")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
