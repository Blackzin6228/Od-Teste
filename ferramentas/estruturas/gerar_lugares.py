"""Gera os cinco lugares do Ato 1: .nbt validado, marcadores, prévias e o README para a Dev.

Uso (na raiz do repositório):
    python3 -I ferramentas/estruturas/gerar_lugares.py                  # os cinco
    python3 -I ferramentas/estruturas/gerar_lugares.py moinho_parado    # só um
    python3 -I ferramentas/estruturas/gerar_lugares.py --sem-previa     # só .nbt, json e README

Saída em arte/lugares-ato1/:
    <nome>/<nome>.nbt               estrutura vanilla 1.21.1 (DataVersion 3955)
    <nome>/<nome>.marcadores.json   marcadores (structure_block DATA) e pistas, com posição e alvo
    <nome>/previas/*.png            4 lados de dia, cortes por andar, plantas por andar, noite com névoa carmesim,
                                    vista dos marcadores
    <nome>/00_prancha.png           resumo numa imagem só
    README.md                       como colocar e a tabela de marcadores de cada lugar (gerado)
Precisa de numpy, Pillow e moderngl (só para as prévias).
"""
import json
import os
import sys
import time

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.abspath(os.path.join(AQUI, "..", ".."))
sys.path.insert(0, AQUI)

import nbt_io  # noqa: E402
import validar  # noqa: E402
import importlib  # noqa: E402

NOMES = ["moinho_parado", "casa_acougueiro", "casa_cartografo", "torre_correntes", "fazenda_sino_mudo"]
LUGARES = []
for _n in NOMES:
    try:
        LUGARES.append(importlib.import_module("modelos." + _n))
    except ModuleNotFoundError as _erro:
        if _erro.name != "modelos." + _n:
            raise
SAIDA = os.path.join(RAIZ, "arte", "lugares-ato1")
LADOS = [("sudeste", 0), ("nordeste", 1), ("noroeste", 2), ("sudoeste", 3)]


def gerar(mod, previa=True):
    t0 = time.time()
    est = mod.construir()
    pasta = os.path.join(SAIDA, mod.NOME)
    os.makedirs(pasta, exist_ok=True)
    caminho = os.path.join(pasta, mod.NOME + ".nbt")
    nbt_io.salvar(caminho, est.para_nbt())
    erros, avisos, resumo = validar.validar(caminho, tam_esperado=est.tam)
    validar.imprimir(os.path.relpath(caminho, RAIZ), erros, avisos, resumo)
    if erros:
        return None
    info = {
        "estrutura": mod.NOME, "titulo": mod.TITULO, "tamanho": list(est.tam), "y_chao": est.y_chao,
        "origem": "canto noroeste de baixo (x leste, y cima, z sul)",
        "colocar": f"origem y = (y do bloco de cima do terreno) - {est.y_chao}",
        "marcadores": est.marcadores, "pistas": est.pistas,
    }
    with open(os.path.join(pasta, mod.NOME + ".marcadores.json"), "w", encoding="utf-8") as f:
        json.dump(info, f, ensure_ascii=False, indent=2)
        f.write("\n")
    if previa:
        previas(mod, est, pasta)
    print(f"   {mod.NOME}: {len(est.blocos)} blocos, {len(est.marcadores)} marcadores, {time.time() - t0:.1f}s")
    return est


def previas(mod, est, pasta):
    import render_gl as G
    pv = os.path.join(pasta, "previas")
    os.makedirs(pv, exist_ok=True)
    for f in os.listdir(pv):
        if f.endswith(".png"):
            os.remove(os.path.join(pv, f))
    imgs = {}
    for nome, a in LADOS:
        imgs[nome] = G.iso(est, a)
        imgs[nome].save(os.path.join(pv, f"dia_{nome}.png"))
    for corte, y0, y1 in mod.CORTES:
        chao = y1 > est.y_chao
        G.iso(est, mod.FRENTE_ISO, y_max=y1, chao=chao).save(os.path.join(pv, f"corte_{corte}_frente.png"))
        G.iso(est, (mod.FRENTE_ISO + 2) % 4, y_max=y1, chao=chao).save(os.path.join(pv, f"corte_{corte}_fundo.png"))
        G.planta(est, y0, y1).save(os.path.join(pv, f"planta_{corte}.png"))
    imgs["noite_longe"] = G.longe(est, mod.FRENTE_GRAUS, **getattr(mod, "LONGE", {}))
    imgs["noite_longe"].save(os.path.join(pv, "noite_longe.png"))
    G.longe(est, mod.FRENTE_GRAUS + 140, **getattr(mod, "LONGE", {})).save(os.path.join(pv, "noite_longe_2.png"))
    G.iso(est, mod.FRENTE_ISO, noite=True).save(os.path.join(pv, "noite_perto.png"))
    G.iso(est, mod.FRENTE_ISO, marcadores=True, chao=False,
          y_max=getattr(mod, "Y_MARCADORES", None)).save(os.path.join(pv, "marcadores.png"))
    prancha(mod, imgs, os.path.join(pasta, "00_prancha.png"))


def prancha(mod, imgs, caminho):
    from PIL import Image, ImageDraw
    W = 1600
    topo = imgs["noite_longe"].resize((W, int(W * imgs["noite_longe"].height / imgs["noite_longe"].width)))
    cel = W // 2
    lados = []
    for nome, _ in LADOS:
        im = imgs[nome]
        k = min(cel / im.width, 620 / im.height)
        lados.append((nome, im.resize((int(im.width * k), int(im.height * k)))))
    alt_l = max(i.height for _, i in lados)
    H = 60 + topo.height + 2 * (alt_l + 30)
    p = Image.new("RGB", (W, H), (22, 22, 26))
    d = ImageDraw.Draw(p)
    try:
        from PIL import ImageFont
        fonte = ImageFont.truetype("DejaVuSans.ttf", 22)
        fonte_p = ImageFont.truetype("DejaVuSans.ttf", 16)
    except OSError:
        fonte = fonte_p = None
    d.text((20, 18), f"{mod.TITULO}  ({mod.TAM[0]} x {mod.TAM[2]} x {mod.TAM[1]})  -  prévia, não é print do jogo",
           fill=(220, 210, 205), font=fonte)
    p.paste(topo, (0, 60))
    for i, (nome, im) in enumerate(lados):
        cx = (i % 2) * cel + (cel - im.width) // 2
        cy = 60 + topo.height + (i // 2) * (alt_l + 30) + 24
        p.paste(im, (cx, cy))
        d.text(((i % 2) * cel + 20, cy - 22), "de " + nome, fill=(200, 200, 200), font=fonte_p)
    p.save(caminho)


# ---------------------------------------------------------------------------
INTRO = """# Lugares do Ato 1: estruturas (prévia, ainda fora do jogo)

Gerado por `ferramentas/estruturas/gerar_lugares.py` (não edite à mão: rode o gerador).
Cinco estruturas **vanilla 1.21.1** feitas por código, na paleta morta da Vila Calada (madeira escura gasta, terra
escura, pedra suja; vermelho só de Sangue). **Nada está registrado no mod.** Entram só por comando de admin
(regra 11): a Dev liga depois, com a colocação em partes que não destrói construção de jogador.

## Como colocar (teste em mundo de criativo)
- Copie `<nome>/<nome>.nbt` para `<mundo>/generated/minecraft/structures/` e rode
  `/place template minecraft:<nome> X Y Z`, com **Y = (y do bloco de cima do terreno) - y_chao** (tabela abaixo).
  A origem é o **canto noroeste de baixo** (x leste, z sul).
- Create: copie para `schematics/` e use o Canhão de Esquemas com "substituir ar".
- Posição que a estrutura não define fica fora do arquivo: o terreno ali não é tocado. Só os cômodos gravam ar.
- Abaixo de `y_chao` só há porão e cripta (cavados no terreno).

## Marcadores (para a Dev)
Bloco de estrutura em modo **DATA**, com `metadata = "tipo:nome"`. O mod lê os marcadores do template e **não coloca**
o bloco de estrutura (fica ar). Tipos:

| Tipo | Onde fica | O que a Dev liga |
|---|---|---|
| `bau:` `atril:` `disco:` | **um bloco acima** do baú, atril ou jukebox (o alvo) | enche o baú, põe o livro, põe o disco |
| `spawn:` | no ponto (ar) | onde a criatura ou figura aparece |
| `rota:` | no ponto | caminho da perseguição, em ordem (`rota:01`, `rota:02`...) |
| `som:` | no ponto | origem de um som do mod (sempre som vanilla) |
| `evento:` | no ponto | gatilho ou centro de um evento (cadeado, ritual, alavancas, portas) |
| `mapa:` | na frente do mapa-mural | alvo = o bloco do mural atrás; o mod troca por `red_terracotta` (a marca) e devolve o original |

A lista completa com posição e alvo está em `<nome>/<nome>.marcadores.json`. A prévia `previas/marcadores.png`
mostra os pontos (amarelo baú/atril, vermelho spawn, verde rota, ciano som, magenta evento, branco mapa).

## Prévias
Feitas por `render_gl.py` (OpenGL sem tela): luz de bloco e de céu como no jogo, cores aproximadas, **não é print**.
Cada lugar tem `00_prancha.png` (resumo) e, em `previas/`: `dia_<lado>` (4 lados), `corte_<andar>_frente|fundo`,
`planta_<andar>` (de cima, norte para cima, grade de 5 em 5), `noite_longe`, `noite_longe_2`, `noite_perto`
(névoa carmesim, como o shader de Sangue vai fazer) e `marcadores`.
"""


def readme(ests):
    linhas = [INTRO, "\n## Os cinco lugares\n",
              "| Lugar | Arquivo | Tamanho (x, y, z) | y_chao | Marcadores |", "|---|---|---|---|---|"]
    for mod in LUGARES:
        est = ests.get(mod.NOME)
        if est is None:
            continue
        linhas.append(f"| {mod.TITULO} | `{mod.NOME}/{mod.NOME}.nbt` | {est.tam[0]} x {est.tam[1]} x {est.tam[2]} | "
                      f"{est.y_chao} | {len(est.marcadores)} |")
    for mod in LUGARES:
        est = ests.get(mod.NOME)
        if est is None:
            continue
        linhas += ["", f"## {mod.TITULO}", "", mod.RESUMO.strip(), "",
                   "| metadata | posição | alvo | nota |", "|---|---|---|---|"]
        for mk in est.marcadores:
            linhas.append(f"| `{mk['metadata']}` | {tuple(mk['pos'])} | {tuple(mk['alvo'])} | {mk['nota']} |")
        if est.pistas:
            linhas += ["", "Outros pontos (sem marcador, só no json):", ""]
            for p in est.pistas:
                linhas.append(f"- `{p['tipo']}` {tuple(p['pos'])}: {p['nota']}")
    with open(os.path.join(SAIDA, "README.md"), "w", encoding="utf-8") as f:
        f.write("\n".join(linhas) + "\n")


def main(args):
    previa = "--sem-previa" not in args
    nomes = [a for a in args if not a.startswith("--")]
    escolhidos = [m for m in LUGARES if not nomes or m.NOME in nomes]
    ests = {}
    ok = True
    for mod in escolhidos:
        est = gerar(mod, previa)
        ok = ok and est is not None
        if est is not None:
            ests[mod.NOME] = est
    # o README sempre lista os cinco (os não escolhidos são construídos sem prévia)
    for mod in LUGARES:
        if mod.NOME not in ests:
            ests[mod.NOME] = mod.construir()
    readme(ests)
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
