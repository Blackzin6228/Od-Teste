#!/usr/bin/env python3
"""Gera os modelos de EXEMPLO usados para testar o pipeline de prévia.

Nada aqui é peça oficial do mod. Gera:
  arte/texturas/exemplo.png            + arte/blockbench/exemplo.bbmodel        (cubo de orientação)
  ferramentas/previa/teste/exemplo.geo.json                                     (o mesmo cubo em GeckoLib)
  arte/texturas/espada_exemplo.png     + arte/blockbench/espada_exemplo.bbmodel (espada de teste, 16x)

Uso: python3 gerar_exemplos.py [pasta_do_repositorio]
"""
import base64
import json
import os
import sys
import uuid

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from previa import FONTE, gravar_png  # noqa: E402

RAIZ = os.path.abspath(sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..", ".."))


def hexa(h):
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


# Paleta do arte/guia-de-estilo.md (mantenha as duas iguais)
P = {
    ".": (0, 0, 0, 0),
    # neutros: metal
    "m0": hexa("1C1A21"), "m1": hexa("3A3843"), "m2": hexa("676673"), "m3": hexa("A3A2B0"), "m4": hexa("E2E1EA"),
    # neutros: couro
    "c0": hexa("2A1913"), "c1": hexa("4E2E21"), "c2": hexa("7A4B33"),
    # Sangue
    "s0": hexa("4A0A12"), "s1": hexa("8E1624"), "s2": hexa("D9393B"),
    # Morte
    "k0": hexa("0C0B0F"), "k1": hexa("25222B"), "k2": hexa("6F6979"),
    # Conhecimento
    "g0": hexa("6E4A10"), "g1": hexa("C9961F"), "g2": hexa("FFE27A"),
    # Energia
    "e0": hexa("2B1060"), "e1": hexa("7B2FD6"), "e2": hexa("3FF2FF"),
    # Medo
    "w0": hexa("8D939D"), "w1": hexa("D5D9E0"), "w2": hexa("FFFFFF"),
}


class Textura:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.px = [P["."]] * (w * h)

    def pinta(self, x, y, cor):
        self.px[y * self.w + x] = P[cor] if isinstance(cor, str) else cor

    def retangulo(self, x, y, w, h, cor):
        for j in range(h):
            for i in range(w):
                self.pinta(x + i, y + j, cor)

    def linhas(self, x, y, linhas):
        """linhas = lista de strings com códigos separados por espaço."""
        for j, lin in enumerate(linhas):
            for i, c in enumerate(lin.split()):
                self.pinta(x + i, y + j, c)

    def letra(self, ch, x, y, cor):
        for i, b in enumerate(FONTE[ch]):
            if b == "1":
                self.pinta(x + i % 3, y + i // 3, cor)

    def png_bytes(self, caminho):
        gravar_png(caminho, self.w, self.h, self.px)
        with open(caminho, "rb") as f:
            return f.read()


def uid(nome):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "odteste/" + nome))


def cubo(nome, de, ate, faces):
    """faces: {face: [u1,v1,u2,v2]} — as que faltarem ficam sem textura."""
    return {
        "name": nome, "box_uv": False, "rescale": False, "locked": False, "render_order": "default",
        "allow_mirror_modeling": True, "from": de, "to": ate, "autouv": 0, "color": 0,
        "origin": [(a + b) / 2 for a, b in zip(de, ate)], "rotation": [0, 0, 0],
        "faces": {f: ({"uv": uv, "texture": 0} if uv else {"uv": [0, 0, 0, 0], "texture": None})
                  for f, uv in ((f, faces.get(f)) for f in ("north", "east", "south", "west", "up", "down"))},
        "type": "cube", "uuid": uid(nome),
    }


def todas(nsul, lados, cimabaixo, baixo=None):
    """Atalho: mesma UV para frente/costas, para os dois lados e para cima/baixo."""
    return {"north": nsul, "south": nsul, "east": lados, "west": lados, "up": cimabaixo, "down": baixo or cimabaixo}


def bbmodel(nome, elementos, tex_nome, tex_bytes, tw, th, display=None):
    return {
        "meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
        "name": nome, "parent": "", "ambientocclusion": True, "front_gui_light": False,
        "visible_box": [1, 1, 0], "variable_placeholders": "", "variable_placeholder_buttons": [],
        "unhandled_root_fields": {}, "resolution": {"width": tw, "height": th},
        "elements": elementos,
        "outliner": [e["uuid"] for e in elementos],
        "textures": [{
            "path": "", "name": tex_nome, "folder": "", "namespace": "", "id": "0", "group": "",
            "width": tw, "height": th, "uv_width": tw, "uv_height": th, "particle": True,
            "use_as_default": False, "layers_enabled": False, "sync_to_project": "", "render_mode": "default",
            "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop", "frame_order": "",
            "frame_interpolate": False, "visible": True, "internal": True, "saved": True,
            "uuid": uid(nome + "/tex"), "relative_path": "../texturas/" + tex_nome,
            "source": "data:image/png;base64," + base64.b64encode(tex_bytes).decode(),
        }],
        "display": display or {},
    }


def salva_json(caminho, dados):
    os.makedirs(os.path.dirname(caminho), exist_ok=True)
    with open(caminho, "w", encoding="utf-8") as f:
        json.dump(dados, f, ensure_ascii=False, indent=1)
        f.write("\n")
    print("ok:", os.path.relpath(caminho, RAIZ))


# ------------------------------------------------------------------ cubo de orientação

def exemplo():
    t = Textura(32, 32)
    # cada face do cubo (8x8) tem uma cor de elemento e uma letra do ponto de vista do modelo:
    # F frente (norte), T trás (sul), D direita (leste), E esquerda (oeste), C cima, B baixo
    blocos = [("F", 0, 0, "s1", "s2"), ("T", 8, 0, "g1", "g2"), ("D", 16, 0, "e1", "e2"),
              ("E", 24, 0, "k1", "k2"), ("C", 0, 8, "w1", "w0"), ("B", 8, 8, "m1", "m3")]
    for letra, x, y, fundo, frente in blocos:
        t.retangulo(x, y, 8, 8, fundo)
        t.letra(letra, x + 3, y + 2, frente)
        t.pinta(x, y, frente)  # canto superior esquerdo marcado: mostra se a UV girou
    t.retangulo(16, 8, 16, 2, "m2")   # lados da base
    t.retangulo(0, 16, 12, 12, "m1")  # cima/baixo da base
    for i in range(12):
        t.pinta(i, 16, "m2")
        t.pinta(0, 16 + i, "m2")
    png = t.png_bytes(os.path.join(RAIZ, "arte/texturas/exemplo.png"))
    print("ok: arte/texturas/exemplo.png")

    els = [
        cubo("base", [2, 0, 2], [14, 2, 14], todas([16, 8, 28, 10], [16, 8, 28, 10], [0, 16, 12, 28])),
        cubo("cubo", [4, 2, 4], [12, 10, 12], {"north": [0, 0, 8, 8], "south": [8, 0, 16, 8],
                                                "east": [16, 0, 24, 8], "west": [24, 0, 32, 8],
                                                "up": [0, 8, 8, 16], "down": [8, 8, 16, 16]}),
    ]
    salva_json(os.path.join(RAIZ, "arte/blockbench/exemplo.bbmodel"),
               bbmodel("exemplo", els, "exemplo.png", png, 32, 32))

    # o mesmo cubo em .geo.json (GeckoLib), para testar o outro leitor.
    # No Bedrock, x é espelhado: origem_x = -(de_x + tamanho_x) e topo/fundo têm UV invertida.
    def face(u1, v1, u2, v2, nome):
        if nome in ("up", "down"):
            return {"uv": [u2, v2], "uv_size": [u1 - u2, v1 - v2]}
        return {"uv": [u1, v1], "uv_size": [u2 - u1, v2 - v1]}

    def geo_cubo(de, ate, faces):
        tam = [b - a for a, b in zip(de, ate)]
        return {"origin": [-(de[0] + tam[0]) + 8, de[1], de[2] - 8], "size": tam,
                "uv": {n: face(*uv, n) for n, uv in faces.items()}}

    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {"identifier": "geometry.exemplo", "texture_width": 32, "texture_height": 32,
                            "visible_bounds_width": 2, "visible_bounds_height": 2, "visible_bounds_offset": [0, 0.5, 0]},
            "bones": [{"name": "raiz", "pivot": [0, 0, 0], "cubes": [
                geo_cubo([2, 0, 2], [14, 2, 14], todas([16, 8, 28, 10], [16, 8, 28, 10], [0, 16, 12, 28])),
                geo_cubo([4, 2, 4], [12, 10, 12], {"north": [0, 0, 8, 8], "south": [8, 0, 16, 8],
                                                   "east": [16, 0, 24, 8], "west": [24, 0, 32, 8],
                                                   "up": [0, 8, 8, 16], "down": [8, 8, 16, 16]}),
            ]}],
        }],
    }
    salva_json(os.path.join(RAIZ, "ferramentas/previa/teste/exemplo.geo.json"), geo)


# ------------------------------------------------------------------ espada de exemplo (NÃO oficial)

def espada():
    t = Textura(16, 16)
    # lâmina, frente/costas (3x10): sombra | base com veio de sangue | fio claro
    t.linhas(0, 0, [
        "m1 m3 m4",
        "m1 m2 m4",
        "m1 m2 m4",
        "m1 m2 m3",
        "m1 s0 m3",
        "m1 s1 m3",
        "m1 s1 m3",
        "m1 s2 m3",
        "s0 s1 m3",
        "s0 s0 m3",
    ])
    t.linhas(3, 0, ["m4"] * 3 + ["m3"] * 7)        # lado do fio (leste)
    t.linhas(4, 0, ["m2"] * 3 + ["m1"] * 7)        # lado das costas (oeste)
    t.linhas(0, 10, ["m3 m3 m3"])                   # topo/fundo da lâmina
    t.linhas(5, 0, ["m2 m4", "m3 m3"])              # ponta 1: frente / topo
    t.linhas(7, 0, ["m3"])                          # ponta: lados
    t.linhas(8, 0, ["m4", "m4"])                    # ponta 2: frente / topo
    t.linhas(10, 0, ["c2", "c0", "c2", "c0"])       # cabo enrolado
    t.linhas(11, 0, ["c1"])                         # cabo: topo/fundo
    t.linhas(12, 0, ["m3 m2", "m2 m1", "m2 m2", "m1 m1"])  # pomo: lados / topo
    t.linhas(14, 0, ["s2 s1", "s2 s0"])             # pedra: frente, lado / topo, fundo
    t.linhas(0, 12, [
        "m1 m2 m3 m2 m2 m3 m2 m1 m1 m2 m2 m1 m2 m3",  # guarda frente | guarda lado | ponta da guarda
        "m2 m3 m3 m3 m3 m3 m3 m2 .  .  .  .  .  m0",  # guarda topo
        "m1 m2 m2 m2 m2 m2 m2 m1",
        "m0 m1 m1 m1 m1 m1 m1 m0",                    # guarda fundo
    ])
    png = t.png_bytes(os.path.join(RAIZ, "arte/texturas/espada_exemplo.png"))
    print("ok: arte/texturas/espada_exemplo.png")

    els = [
        cubo("lamina", [6.5, 6, 7.5], [9.5, 16, 8.5],
             {"north": [0, 0, 3, 10], "south": [0, 0, 3, 10], "east": [3, 0, 4, 10], "west": [4, 0, 5, 10],
              "up": [0, 10, 3, 11], "down": [0, 10, 3, 11]}),
        cubo("ponta_1", [7, 16, 7.5], [9, 17, 8.5], todas([5, 0, 7, 1], [7, 0, 8, 1], [5, 1, 7, 2])),
        cubo("ponta_2", [7.5, 17, 7.5], [8.5, 18, 8.5], todas([8, 0, 9, 1], [7, 0, 8, 1], [8, 1, 9, 2])),
        cubo("guarda", [4, 5, 7], [12, 6, 9], todas([0, 12, 8, 13], [8, 12, 10, 13], [0, 13, 8, 15], [0, 14, 8, 16])),
        cubo("guarda_esq", [4, 4, 7], [5, 5, 9], todas([12, 12, 13, 13], [10, 12, 12, 13], [13, 12, 14, 14])),
        cubo("guarda_dir", [11, 4, 7], [12, 5, 9], todas([12, 12, 13, 13], [10, 12, 12, 13], [13, 12, 14, 14])),
        cubo("pedra", [7.5, 5, 6.5], [8.5, 6, 9.5], todas([14, 0, 15, 1], [15, 0, 16, 1], [14, 1, 15, 2], [15, 1, 16, 2])),
        cubo("cabo", [7.5, 1, 7.5], [8.5, 5, 8.5], todas([10, 0, 11, 4], [10, 0, 11, 4], [11, 0, 12, 1])),
        cubo("pomo", [7, -1, 7], [9, 1, 9], todas([12, 0, 14, 2], [12, 0, 14, 2], [12, 2, 14, 4])),
    ]
    # posições na mão: ponto de partida, ajustar no Blockbench (aba Display)
    display = {
        "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
        "thirdperson_lefthand": {"rotation": [0, -90, 0], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
        "firstperson_righthand": {"rotation": [0, 90, 20], "translation": [1.1, 3.2, 1.1], "scale": [0.68, 0.68, 0.68]},
        "firstperson_lefthand": {"rotation": [0, -90, -20], "translation": [1.1, 3.2, 1.1], "scale": [0.68, 0.68, 0.68]},
        "ground": {"translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
        "gui": {"rotation": [0, 0, -45], "translation": [0.5, 0.5, 0], "scale": [0.8, 0.8, 0.8]},
        "fixed": {"rotation": [0, 0, -45], "scale": [0.9, 0.9, 0.9]},
    }
    salva_json(os.path.join(RAIZ, "arte/blockbench/espada_exemplo.bbmodel"),
               bbmodel("espada_exemplo", els, "espada_exemplo.png", png, 16, 16, display))


if __name__ == "__main__":
    exemplo()
    espada()
