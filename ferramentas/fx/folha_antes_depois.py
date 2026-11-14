#!/usr/bin/env python3
"""Folhas de comparação antes x depois dos efeitos de Sangue (mesa Dev). Lê previas-fx/{antes,depois,pack,olho}."""
import os
import sys
from PIL import Image, ImageDraw

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
P = os.path.join(RAIZ, "previas-fx")
W, H = 640, 360


def fonte():
    from PIL import ImageFont
    for f in ("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "/usr/share/fonts/dejavu/DejaVuSans.ttf"):
        if os.path.exists(f):
            return ImageFont.truetype(f, 15)
    return None


def folha(nome, colunas, linhas):
    """linhas: [(rotulo, [arquivo por coluna])]"""
    img = Image.new("RGB", (10 + len(colunas) * (W + 10), 34 + len(linhas) * (H + 30)), (10, 8, 8))
    d = ImageDraw.Draw(img)
    fo = fonte()
    for j, c in enumerate(colunas):
        d.text((10 + j * (W + 10), 10), c, fill=(230, 200, 200), font=fo)
    for i, (rot, arqs) in enumerate(linhas):
        y = 34 + i * (H + 30)
        d.text((10, y), rot, fill=(220, 60, 60), font=fo)
        for j, a in enumerate(arqs):
            c = os.path.join(P, a)
            if os.path.exists(c):
                img.paste(Image.open(c).convert("RGB").resize((W, H), Image.LANCZOS), (10 + j * (W + 10), y + 16))
    img.save(os.path.join(P, nome))
    print(os.path.join(P, nome))


def ad(fases):
    return [("ANTES", ["antes/antes_" + f + ".png" for f in fases]), ("DEPOIS", ["depois/depois_" + f + ".png" for f in fases])]


folha("1_lua_e_nevoa_carmesim.png", ["vila à noite, efeito cheio", "entrando", "sem profundidade (shader pack)"],
      ad(["a2_carmesim_cheio", "a1_carmesim_entrando", "a2_carmesim_cheio_semprof"]))
folha("2_sangue_parede_e_chao.png", ["sala de pedra (parede + chão)", "escorrendo", "vila, espalhado", "secando"],
      ad(["p_media_espalhado", "p_media_escorrendo", "b2_espalhado", "b3_secando"]))
folha("2b_sangue_sem_profundidade.png", ["escorrendo", "espalhado", "longe"],
      ad(["b1_escorrendo_semprof", "b2_espalhado_semprof", "p_longe_espalhado"]))
folha("3_visao_do_marcado.png", ["estágio 1", "estágio 3", "estágio 4 na batida"],
      ad(["c1_estagio1", "c2_estagio3", "c3_estagio4_pulso"]))
folha("4_batimento.png", ["entre batidas", "no pico da batida"], ad(["d1_entre_batidas", "d2_batida_pico"]))
folha("5_tudo_junto.png", ["chegando", "perto", "colado, na batida"], ad(["e1_chegando", "e2_perto", "e3_colado_batida"]))
fp = ["a2_carmesim_cheio_semprof", "b2_espalhado_semprof", "c3_estagio4_pulso", "d2_batida_pico"]
folha("6_com_shader_pack_simulado.png", ["carmesim", "sangue", "marcado", "batimento"],
      [("ANTES (pack, sem profundidade)", ["pack/antes_pack_" + f + ".png" for f in fp]),
       ("DEPOIS (pack, sem profundidade)", ["pack/depois_pack_" + f + ".png" for f in fp])])
vs = [("0 ÍRIS-LUA", "iris_lua"), ("1 PÁLPEBRAS", "palpebras"), ("2 A LUA QUE SANGRA", "lua_que_sangra")]
folha("7_olho_na_lua_variacoes.png", ["vila à noite (com carmesim)", "Lua de Sangue fora da vila", "perto: vagueia", "perto: encara você"],
      [(r, ["olho/depois_f_%s_%s.png" % (n, s) for s in ("rua_carmesim", "rua_lua_de_sangue", "perto_vagueia", "perto_encara")]) for r, n in vs])
folha("7b_olho_na_lua_com_shader_pack.png", ["vila à noite", "Lua de Sangue", "perto: encara você"],
      [(r, ["pack/depois_pack_f_%s_%s_semprof.png" % (n, s) for s in ("rua_carmesim", "rua_lua_de_sangue", "perto_encara")]) for r, n in vs])
