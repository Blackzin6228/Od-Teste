#!/usr/bin/env python3
"""Prévia dos quatro efeitos de Sangue da Vila Calada, fora do jogo (mesa Dev).

Roda O MESMO .fsh do mod (odfx_carmesim, odfx_sangue, odfx_marcado, odfx_coracao) sobre a cena sintética da vila
à noite (cena_vila.py), com as mesmas curvas de tempo do FxSangue.java, e salva PNG por fase e um GIF.

Uso:
    python3 ferramentas/fx/previa_sangue.py <cena|carmesim|sangue|marcado|coracao|tudo|folha|parede|folha_parede>
        [--saida arte/previas/fx/sangue] [--rodada r1] [--sem-profundidade]

--sem-profundidade roda a versão que o jogo usa quando o shader pack não deixa ler a profundidade.
"""
import argparse
import math
import os
import sys

import moderngl
import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from previa_shader import Efeito, perspectiva, vista, dir_de, RAIZ  # noqa: E402
from cena_vila import CenaVila  # noqa: E402

LARG, ALT = 1280, 720
FOV_Y = 70.0
PERTO, LONGE = 0.05, 512.0
CAM = np.array([0.5, 64.0 + 1.62, 6.0], dtype=np.float32)
LUA = dir_de(14.0, 21.0)
FPS = 12


def contexto():
    return moderngl.create_standalone_context(backend="egl", require=330)


# --pack: imita a imagem pronta de um shader pack (curva de filme, brilho que vaza, céu de noite mais claro e azul,
# lua com halo grande). Serve para conferir a versão sem profundidade, que é a que roda com a maioria dos packs.
PACK = False


def jeito_de_pack(cena):
    from PIL import ImageFilter
    raw = np.frombuffer(cena.cor.read(), dtype=np.uint8).reshape(ALT, LARG, 4)[..., :3].astype(np.float32) / 255.0
    lin = raw ** 2.2
    lin = lin * 1.8 + np.array([0.004, 0.006, 0.014], dtype=np.float32)      # exposição + céu de noite azulado
    claro = np.clip(lin - 0.35, 0.0, None)
    im = Image.fromarray((np.clip(claro, 0, 1) * 255).astype(np.uint8))
    halo = np.asarray(im.filter(ImageFilter.GaussianBlur(28)), dtype=np.float32) / 255.0
    lin = lin + halo * 1.4
    a, b, c, d, e = 2.51, 0.03, 2.43, 0.59, 0.14                               # ACES
    t = np.clip((lin * (a * lin + b)) / (lin * (c * lin + d) + e), 0, 1)
    out = t ** (1 / 2.2)
    m = out.mean(axis=2, keepdims=True)
    out = np.clip(m + (out - m) * 1.25, 0, 1)                                  # mais saturado
    cena.cor.write((np.concatenate([out, np.ones((ALT, LARG, 1), np.float32)], axis=2) * 255).astype(np.uint8).tobytes())


class Previa:
    def __init__(self, yaw=-4.0, pitch=-8.0, pedra=False, ctx=None):
        self.ctx = ctx or contexto()
        self.cena = CenaVila(self.ctx, LARG, ALT)
        self.proj = perspectiva(FOV_Y, LARG / ALT, PERTO, LONGE)
        self.view, _ = vista(yaw, pitch)
        self.cena.desenhar(self.proj, self.view, LUA, CAM, pedra)
        if PACK:
            jeito_de_pack(self.cena)
        self.fx = {}
        self.desloca = np.zeros(3, dtype=np.float32)   # só no uniform Camera: coordenada grande como no jogo

    def efeito(self, nome):
        if nome not in self.fx:
            self.fx[nome] = Efeito(self.ctx, nome, LARG, ALT)
        return self.fx[nome]

    def sem_efeito(self):
        img = Image.frombytes("RGBA", (LARG, ALT), self.cena.cor.read())
        return img.transpose(Image.FLIP_TOP_BOTTOM).convert("RGB")

    def comuns(self, t, prof):
        return {
            "Tempo": t, "ComProfundidade": 1.0 if prof else 0.0,
            "Camera": tuple(float(x) % 4096.0 for x in (CAM + self.desloca)),
            "LuaDir": tuple(LUA),
        }

    def cadeia(self, lista, u):
        """Vários efeitos em sequência, como no jogo (cada um lê a saída do anterior)."""
        class Entrada:
            pass
        ent = Entrada()
        ent.cor, ent.prof = self.cena.cor, self.cena.prof
        img = None
        for nome, extra in lista:
            fx = self.efeito(nome)
            uu = dict(u)
            uu.update(extra)
            img = fx.desenhar(ent, self.proj, self.view, uu)
            nova = Entrada()
            nova.cor, nova.prof = fx.saida, self.cena.prof
            ent = nova
        return img

    def quadro(self, nome, u):
        return self.efeito(nome).desenhar(self.cena, self.proj, self.view, u)


# ---------------------------------------------------------------- curvas (iguais às do FxSangue.java)

def suave(x):
    x = min(max(x, 0.0), 1.0)
    return x * x * (3 - 2 * x)


def batida(t, intervalo):
    """Forma do batimento (0..1): 'tum' forte e 'tum' mais fraco 0,2 s depois. Igual a FxSangue.batida."""
    f = t % intervalo
    a = math.exp(-((f - 0.03) / 0.055) ** 2)
    b = 0.6 * math.exp(-((f - 0.23) / 0.06) ** 2)
    return min(1.0, a + b)


SANGUE_SOBE, SANGUE_VIVE, SANGUE_SECA, SANGUE_SOME = 6.0, 6.0, 5.0, 3.0


def curva_sangue(t):
    """Forca e Seca do sangue no chão/parede (FxSangue: sobe, vive, seca, some)."""
    total = SANGUE_SOBE + SANGUE_VIVE + SANGUE_SECA + SANGUE_SOME
    seca = suave((t - SANGUE_SOBE - SANGUE_VIVE) / SANGUE_SECA)
    forca = 1.0 - suave((t - total + SANGUE_SOME) / SANGUE_SOME)
    return forca, seca, total


# ---------------------------------------------------------------- cada efeito

def salvar(saida, pref, chave, quadros, nome_gif):
    os.makedirs(saida, exist_ok=True)
    for nome, img in chave.items():
        img.save(os.path.join(saida, pref + nome + ".png"))
    if quadros:
        pequenos = [q.resize((480, 270), Image.LANCZOS) for q in quadros[::2]]
        pequenos[0].save(os.path.join(saida, pref + nome_gif + ".gif"), save_all=True, append_images=pequenos[1:],
                         duration=int(2000 / FPS), loop=0, optimize=True)


def rodar(efeito, saida, pref, prof):
    pv = Previa()
    quadros, chave = [], {}
    sufixo = "" if prof else "_semprof"
    if efeito == "cena":
        pv.sem_efeito().save(os.path.join(saida, pref + "0_cena.png"))
        return
    if efeito == "carmesim":
        total = 6.0
        for i in range(int(total * FPS)):
            t = i / FPS
            u = pv.comuns(t, prof)
            u["Forca"] = suave(t / 3.0)
            img = pv.quadro("odfx_carmesim", u)
            quadros.append(img)
            if abs(t - 1.5) < 0.5 / FPS: chave["a1_carmesim_entrando" + sufixo] = img
            if abs(t - 5.0) < 0.5 / FPS: chave["a2_carmesim_cheio" + sufixo] = img
    elif efeito == "sangue":
        f, s, total = curva_sangue(0)
        origem = (-6.0, -1.62, -16.0)
        for i in range(int(total * FPS)):
            t = i / FPS
            forca, seca, _ = curva_sangue(t)
            u = pv.comuns(t, prof)
            u.update({"Forca": forca, "Idade": t, "Seca": seca, "Origem": origem, "Chao": -1.62, "Alcance": 12.0})
            img = pv.quadro("odfx_sangue", u)
            quadros.append(img)
            for nome, tc in (("b1_escorrendo", 2.5), ("b2_espalhado", 9.0), ("b3_secando", 14.5), ("b4_sumindo", 18.2)):
                if abs(t - tc) < 0.5 / FPS: chave[nome + sufixo] = img
    elif efeito == "marcado":
        total = 12.0
        for i in range(int(total * FPS)):
            t = i / FPS
            est = min(1.0, 0.25 + 0.75 * t / 9.0)
            u = pv.comuns(t, prof)
            u.update({"Forca": suave(t / 1.5), "Estagio": est, "Pulso": batida(t, 1.0), "Mexer": 1.0})
            img = pv.quadro("odfx_marcado", u)
            quadros.append(img)
            for nome, tc in (("c1_estagio1", 1.5), ("c2_estagio3", 6.0), ("c3_estagio4_pulso", 10.03)):
                if abs(t - tc) < 0.5 / FPS: chave[nome] = img
    elif efeito == "coracao":
        total = 8.0
        for i in range(int(total * FPS)):
            t = i / FPS
            perto = suave(t / 5.0)
            intervalo = 1.1 - 0.5 * perto
            u = pv.comuns(t, prof)
            u.update({"Forca": perto, "Batida": batida(t, intervalo), "Mexer": 1.0})
            img = pv.quadro("odfx_coracao", u)
            quadros.append(img)
        # quadro no pico de uma batida, com o Faminto colado
        u = pv.comuns(7.0, prof)
        u.update({"Forca": 1.0, "Batida": 1.0, "Mexer": 1.0})
        chave["d2_batida_pico"] = pv.quadro("odfx_coracao", u)
        u["Batida"] = 0.0
        chave["d1_entre_batidas"] = pv.quadro("odfx_coracao", u)
    elif efeito == "junto":
        # O que o jogador vê de verdade: carmesim de noite, a coisa chega (sangue + coração), Marca no estágio 3.
        total = 16.0
        origem = (-6.0, -1.62, -16.0)
        intervalo, ultima = 1.1, -10.0
        for i in range(int(total * FPS)):
            t = i / FPS
            perto = suave((t - 2.0) / 6.0)
            if t - ultima >= intervalo:
                ultima, intervalo = t, 1.15 - 0.6 * perto
            bat = batida(t - ultima, 99.0)
            ts = t - 1.0
            forca_s, seca, _ = curva_sangue(ts)
            u = pv.comuns(t, prof)
            u.update({"Forca": 1.0 if ts >= 0 else 0.0, "Idade": max(ts, 0.0), "Seca": seca, "Origem": origem,
                      "Chao": -1.62, "Alcance": 12.0, "Estagio": 0.75, "Pulso": bat, "Batida": bat, "Mexer": 1.0})
            img = pv.cadeia([("odfx_sangue", {"Forca": forca_s if ts >= 0 else 0.0}), ("odfx_carmesim", {"Forca": 1.0}),
                             ("odfx_marcado", {"Forca": 1.0}), ("odfx_coracao", {"Forca": perto})], u)
            quadros.append(img)
            for nome, tc in (("e1_chegando", 3.0), ("e2_perto", 9.0), ("e3_colado_batida", 13.0)):
                if abs(t - tc) < 0.5 / FPS: chave[nome + sufixo] = img
    salvar(saida, pref, chave, quadros, efeito + sufixo)
    print("ok:", efeito, len(quadros), "quadros")


NOMES_OLHO = ("iris_lua", "palpebras", "lua_que_sangra")


def olho(saida, pref, prof):
    """Olho vermelho na lua, nas 3 variantes: rua da vila (com e sem carmesim) e olhando para a lua (perto)."""
    ctx = contexto()
    sufixo = "" if prof else "_semprof"
    rua = Previa(ctx=ctx)
    lua = Previa(14.0, 21.0, ctx=ctx)
    lua.cena.desenhar(lua.proj, lua.view, LUA, CAM + np.array([0.0, 6.0, 4.0], dtype=np.float32), False)
    if PACK:
        jeito_de_pack(lua.cena)
    olhar_lado = tuple(float(x) for x in (dir_de(60.0, -10.0) * 0.8 - LUA * 0.2))
    encara = tuple(float(x) for x in -LUA)
    for vi, vnome in enumerate(NOMES_OLHO):
        quadros = []
        for i in range(int(8.0 * FPS)):
            t = i / FPS
            # vagueia, depois encara (a pupila desliza), pisca no fim
            m = min(max((t - 3.0) / 1.5, 0.0), 1.0)
            ol = tuple(float(a * (1 - m) + b * m) for a, b in zip(olhar_lado, encara))
            ab = 1.0 if t < 6.6 or t > 7.2 else abs(t - 6.9) / 0.3
            u = rua.comuns(t, prof)
            u.update({"Forca": min(1.0, t / 1.0), "Olhar": ol, "Abertura": ab, "Variante": float(vi)})
            quadros.append(rua.cadeia([("odfx_carmesim", {"Forca": 1.0}), ("odfx_olho_lua", {})], u))
        for nome, pv, cad, ol in (("rua_carmesim", rua, True, encara), ("rua_lua_de_sangue", rua, False, encara),
                                  ("perto_vagueia", lua, True, olhar_lado), ("perto_encara", lua, True, encara)):
            u = pv.comuns(5.0, prof)
            u.update({"Forca": 1.0, "Olhar": ol, "Abertura": 1.0, "Variante": float(vi)})
            lista = ([("odfx_carmesim", {"Forca": 1.0})] if cad else []) + [("odfx_olho_lua", {})]
            img = pv.cadeia(lista, u)
            if nome.startswith("perto"):
                img = img.crop((640 - 180, 360 - 120, 640 + 180, 360 + 120)).resize((960, 640), Image.LANCZOS)
            img.save(os.path.join(saida, pref + "f_" + vnome + "_" + nome + sufixo + ".png"))
        salvar(saida, pref, {}, quadros, "olho_" + vnome + sufixo)
    if not os.path.exists(os.path.join(saida, pref + "f_0_lua_perto_sem_efeito.png")):
        u = lua.comuns(5.0, prof)
        u.update({"Forca": 1.0})
        lua.cadeia([("odfx_carmesim", {"Forca": 1.0})], u).crop((460, 240, 820, 480)).resize((960, 640), Image.LANCZOS).save(
            os.path.join(saida, pref + "f_0_lua_perto_so_carmesim.png"))
        lua.sem_efeito().crop((460, 240, 820, 480)).resize((960, 640), Image.LANCZOS).save(
            os.path.join(saida, pref + "f_0_lua_perto_sem_efeito.png"))
    print("ok: olho")


VISTAS_PAREDE = (("media", -4.0, -6.0), ("longe", 32.0, -3.0))
TEMPOS_PAREDE = (("escorrendo", 4.0), ("espalhado", 10.0))


def parede(saida, pref, prof):
    """Sala de pedra iluminada (como o print do jogo de 14/11): parede a média (~10) e longa (~19) distância."""
    ctx = contexto()
    for vnome, yaw, pitch in VISTAS_PAREDE:
        pv = Previa(yaw, pitch, pedra=True, ctx=ctx)
        # o print do jogo foi em x 4002, z 4006: com número grande o ruído perde precisão (era isso o código de barras)
        pv.desloca = np.array([4002.0, 0.0, 4000.0], dtype=np.float32)
        if not os.path.exists(os.path.join(saida, "pedra_" + vnome + "_cena.png")):
            pv.sem_efeito().save(os.path.join(saida, "pedra_" + vnome + "_cena.png"))
        for tnome, t in TEMPOS_PAREDE:
            forca, seca, _ = curva_sangue(t)
            u = pv.comuns(t, prof)
            u.update({"Forca": forca, "Idade": t, "Seca": seca, "Origem": (-1.0, -1.62, -6.0), "Chao": -1.62,
                      "Alcance": 12.0})
            pv.quadro("odfx_sangue", u).save(os.path.join(saida, pref + "p_" + vnome + "_" + tnome + ".png"))
    print("ok: parede")


def folha_parede(saida, rodadas, nome="folha_parede.png"):
    """Uma linha por rodada (antes x depois): quadro inteiro de cada vista e um recorte 2x (sem filtro)."""
    linhas = []
    for r in rodadas:
        imgs = []
        for vnome, _, _ in VISTAS_PAREDE:
            c = os.path.join(saida, r + "_p_" + vnome + "_espalhado.png")
            if not os.path.exists(c):
                imgs += [None, None]
                continue
            im = Image.open(c)
            imgs.append(im.resize((640, 360), Image.LANCZOS))
            cx, cy = (640, 300) if vnome == "media" else (600, 330)
            imgs.append(im.crop((cx - 160, cy - 90, cx + 160, cy + 90)).resize((640, 360), Image.NEAREST))
        linhas.append((r, imgs))
    w = 10 + 4 * 650
    img = Image.new("RGB", (w, len(linhas) * 390 + 10), (10, 8, 8))
    d = ImageDraw.Draw(img)
    for i, (r, imgs) in enumerate(linhas):
        y = 10 + i * 390
        d.text((12, y + 2), r + "  (media | recorte 2x | longe | recorte 2x)", fill=(220, 60, 60))
        for j, im in enumerate(imgs):
            if im is not None:
                img.paste(im, (10 + j * 650, y + 22))
    img.save(os.path.join(saida, nome))
    print("folha:", os.path.join(saida, nome))


def folha(saida, rodadas, nome="folha_rodadas.png"):
    """Uma linha por rodada, com uma fase de cada efeito."""
    fases = ["a2_carmesim_cheio", "b2_espalhado", "c3_estagio4_pulso", "d2_batida_pico", "e2_perto"]
    linhas = []
    for r in rodadas:
        imgs = []
        for f in fases:
            c = os.path.join(saida, r + "_" + f + ".png")
            imgs.append(Image.open(c).resize((480, 270)) if os.path.exists(c) else None)
        linhas.append((r, imgs))
    w = 10 + len(fases) * 490
    img = Image.new("RGB", (w, len(linhas) * 300 + 10), (10, 8, 8))
    d = ImageDraw.Draw(img)
    for i, (r, imgs) in enumerate(linhas):
        y = 10 + i * 300
        d.text((12, y + 2), "rodada " + r, fill=(220, 60, 60))
        for j, im in enumerate(imgs):
            if im is not None:
                img.paste(im, (10 + j * 490, y + 20))
    img.save(os.path.join(saida, nome))
    print("folha:", os.path.join(saida, nome))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("efeito", choices=["cena", "carmesim", "sangue", "marcado", "coracao", "junto", "tudo", "folha",
                                       "parede", "folha_parede", "olho"])
    ap.add_argument("--saida", default=os.path.join(RAIZ, "arte", "previas", "fx", "sangue"))
    ap.add_argument("--rodada", default="")
    ap.add_argument("--rodadas", default="")
    ap.add_argument("--sem-profundidade", action="store_true")
    ap.add_argument("--pack", action="store_true", help="imita a imagem de um shader pack (use com --sem-profundidade)")
    a = ap.parse_args()
    global PACK
    PACK = a.pack
    os.makedirs(a.saida, exist_ok=True)
    pref = (a.rodada + "_") if a.rodada else ""
    if a.efeito == "folha":
        folha(a.saida, [r for r in a.rodadas.split(",") if r])
        return
    if a.efeito == "folha_parede":
        folha_parede(a.saida, [r for r in a.rodadas.split(",") if r])
        return
    if a.efeito == "olho":
        olho(a.saida, pref, not a.sem_profundidade)
        return
    if a.efeito == "parede":
        parede(a.saida, pref, not a.sem_profundidade)
        return
    lista = ["cena", "carmesim", "sangue", "marcado", "coracao", "junto"] if a.efeito == "tudo" else [a.efeito]
    for e in lista:
        rodar(e, a.saida, pref, not a.sem_profundidade)


if __name__ == "__main__":
    sys.exit(main())
