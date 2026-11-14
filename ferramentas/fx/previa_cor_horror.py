#!/usr/bin/env python3
"""Prévia do efeito cor_horror (overworld inteiro), fora do jogo (mesa Dev).

Roda O MESMO .fsh do mod (odfx_cor_horror) sobre a cena do mundo comum (cena_dia.py: campo, casas e uma base de
tech com blocos de metal coloridos), de dia e de noite, antes x depois, com e sem profundidade.

Uso:
    python3 ferramentas/fx/previa_cor_horror.py [--saida arte/previas/fx/cor_horror] [--rodada r1]
"""
import argparse
import os
import sys

import moderngl
import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from previa_shader import Efeito, perspectiva, vista, dir_de, RAIZ  # noqa: E402
from cena_dia import CenaDia  # noqa: E402

LARG, ALT = 1280, 720
FOV_Y = 70.0
PERTO, LONGE = 0.05, 512.0
CAM = np.array([1.0, 64.0 + 1.62, 6.0], dtype=np.float32)
CAM_BASE = np.array([9.0, 64.0 + 1.62, 0.5], dtype=np.float32)


def forca_visual(tremor):
    """Igual a RegrasFxSangue.forcaVisual: efeitos_fortes diminui, não desliga."""
    return 0.5 + 0.5 * max(0.0, min(1.0, tremor))


class Previa:
    def __init__(self):
        self.ctx = moderngl.create_standalone_context(backend="egl", require=330)
        self.cena = CenaDia(self.ctx, LARG, ALT)
        self.proj = perspectiva(FOV_Y, LARG / ALT, PERTO, LONGE)
        self.fx = Efeito(self.ctx, "odfx_cor_horror", LARG, ALT)

    def par(self, yaw, pitch, dia, u, cam=CAM):
        view, _ = vista(yaw, pitch)
        sol = dir_de(40.0, 55.0) if dia > 0.5 else dir_de(-30.0, 40.0)
        self.cena.desenhar(self.proj, view, sol, cam, dia)
        antes = Image.frombytes("RGBA", (LARG, ALT), self.cena.cor.read()).transpose(Image.FLIP_TOP_BOTTOM).convert("RGB")
        uu = {"Tempo": 3.0, "ComProfundidade": 1.0}
        uu.update(u)
        depois = self.fx.desenhar(self.cena, self.proj, view, uu)
        return antes, depois


def lado_a_lado(a, b, ta, tb):
    w, h = a.size
    s = Image.new("RGB", (w * 2 + 8, h + 34), (12, 12, 14))
    s.paste(a, (0, 34))
    s.paste(b, (w + 8, 34))
    d = ImageDraw.Draw(s)
    try:
        from PIL import ImageFont
        fonte = ImageFont.truetype("DejaVuSans.ttf", 18)
    except OSError:
        fonte = None
    d.text((10, 10), ta, fill=(230, 230, 230), font=fonte)
    d.text((w + 18, 10), tb, fill=(230, 230, 230), font=fonte)
    return s


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--saida", default=os.path.join(RAIZ, "arte", "previas", "fx", "cor_horror"))
    ap.add_argument("--rodada", default="r1")
    a = ap.parse_args()
    os.makedirs(a.saida, exist_ok=True)
    pv = Previa()
    r = a.rodada

    def salvar(nome, img):
        img.save(os.path.join(a.saida, f"{r}_{nome}.png"))

    dia = {"Forca": 1.0, "Noite": 0.0, "Area": 0.0}
    noite = {"Forca": 1.0, "Noite": 1.0, "Area": 0.0}
    # 1) dia: campo e casas, base de tech de perto, sem profundidade
    an, de = pv.par(-20.0, -4.0, 1.0, dia)
    salvar("dia_campo", lado_a_lado(an, de, "antes (dia)", "cor_horror (dia)"))
    an, de = pv.par(22.0, -16.0, 1.0, dia, CAM_BASE)
    salvar("dia_base", lado_a_lado(an, de, "antes (base de tech)", "cor_horror (base de tech)"))
    an, de2 = pv.par(22.0, -16.0, 1.0, dict(dia, ComProfundidade=0.0), CAM_BASE)
    salvar("dia_base_semprof", lado_a_lado(de, de2, "com profundidade", "sem profundidade (pack que não deixa)"))
    # 2) área da história de dia e efeitos_fortes DESLIGADO
    an, de = pv.par(-20.0, -4.0, 1.0, dict(dia, Area=1.0))
    _, de3 = pv.par(-20.0, -4.0, 1.0, dict(dia, Forca=forca_visual(0.0)))
    salvar("dia_area_e_suave", lado_a_lado(de, de3, "área da história (dia)", "efeitos_fortes DESLIGADO (metade)"))
    # 3) noite
    an, de = pv.par(-20.0, -4.0, 0.0, noite)
    salvar("noite_campo", lado_a_lado(an, de, "antes (noite)", "cor_horror (noite)"))
    an, de = pv.par(22.0, -16.0, 0.0, noite, CAM_BASE)
    salvar("noite_base", lado_a_lado(an, de, "antes (noite, base)", "cor_horror (noite, base)"))
    print("ok:", a.saida)


if __name__ == "__main__":
    main()
