#!/usr/bin/env python3
"""Gera o .json de cada efeito de tela (mesa Dev). Todo uniform usado no .fsh tem que estar listado,
senão o jogo não manda o valor. Rode depois de mudar uniforms: python3 ferramentas/fx/gerar_json.py"""
import json
import os

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
CORE = os.path.join(RAIZ, "src", "main", "resources", "assets", "odteste", "shaders", "core")

COMUNS = [("InvProj", "matrix4x4", 16), ("InvView", "matrix4x4", 16), ("ScreenSize", "float", 2),
          ("Tempo", "float", 1), ("Forca", "float", 1), ("ComProfundidade", "float", 1)]

EFEITOS = {
    "odfx_carmesim": [("LuaDir", "float", 3), ("Camera", "float", 3)],
    "odfx_sangue": [("LuaDir", "float", 3), ("Camera", "float", 3), ("Idade", "float", 1), ("Seca", "float", 1),
                    ("Origem", "float", 3), ("Chao", "float", 1), ("Alcance", "float", 1)],
    "odfx_marcado": [("Estagio", "float", 1), ("Pulso", "float", 1), ("Mexer", "float", 1)],
    "odfx_coracao": [("Batida", "float", 1), ("Mexer", "float", 1)],
}


def ident():
    return [1.0 if i % 5 == 0 else 0.0 for i in range(16)]


def gerar(nome, extras):
    unis = [{"name": "ModelViewMat", "type": "matrix4x4", "count": 16, "values": ident()},
            {"name": "ProjMat", "type": "matrix4x4", "count": 16, "values": ident()}]
    for n, t, c in COMUNS + extras:
        unis.append({"name": n, "type": t, "count": c, "values": ident() if c == 16 else [0.0] * c})
    js = {"vertex": "odteste:odfx_tela", "fragment": "odteste:" + nome, "attributes": ["Position", "UV0"],
          "samplers": [{"name": "Sampler0"}, {"name": "Sampler1"}], "uniforms": unis}
    with open(os.path.join(CORE, nome + ".json"), "w", encoding="utf-8") as f:
        json.dump(js, f, indent=2)
        f.write("\n")


if __name__ == "__main__":
    for k, v in EFEITOS.items():
        gerar(k, v)
    print("ok:", ", ".join(EFEITOS))
