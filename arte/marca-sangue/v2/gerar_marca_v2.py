"""Marca de Sangue v2 (mesa Modelador): texturas por estagio + camada de brilho + previas antes x depois + GIF.

Roda com:  bash ferramentas/marca_sangue_v2.sh      (python3 + numpy; sem Pillow)
Saida:
- arte/marca-sangue/v2/estagio_N.png (Steve) e estagio_N_alex.png (Alex): a Marca, para desenhar por cima da skin
- arte/marca-sangue/v2/estagio_N_brilho*.png: SO o que brilha no escuro (interior das rachaduras, ferida, coracao)
- arte/previas/marca-sangue-v2/: folhas antes x depois por estagio, zoom do estagio 4, folha geral e GIF do brilho
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from imagem_np import ampliar, colar, largura_texto, ler_png, nova, salvar_gif, salvar_png, texto, xadrez  # noqa: E402
from corpo import faces, render, skins_teste  # noqa: E402
from veias import Molde  # noqa: E402

RAIZ = sys.argv[1] if len(sys.argv) > 1 else os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
DIR_V1 = os.path.join(RAIZ, "arte", "marca-sangue")
DIR_V2 = os.path.join(DIR_V1, "v2")
DIR_PREVIA = os.path.join(RAIZ, "arte", "previas", "marca-sangue-v2")
FUNDO = (0x16, 0x16, 0x1A, 255)
CINZA = (175, 175, 175)
BRANCO = (235, 235, 235)
VAZIO = np.zeros((64, 64, 4), np.uint8)
COMBOS = [("clara", False), ("escura", False), ("clara", True), ("escura", True)]


def nome_combo(tom, alex):
    return f"{'Alex 3px' if alex else 'Steve 4px'}\npele {tom}"


def carregar_v1(s, alex):
    cam = os.path.join(DIR_V1, f"estagio_{s}{'_alex' if alex else ''}.png")
    return ler_png(cam) if os.path.exists(cam) else VAZIO


def grade_faces(img, fator, alex):
    """Linhas finas nas bordas das faces do molde (camada de baixo), para retocar sem se perder."""
    cor = np.array((120, 120, 135, 255), np.uint8)
    for f in faces(alex):
        umin, vmin, umax, vmax = f["uv"]
        x0, x1 = int(min(umin, umax)) * fator, int(max(umin, umax)) * fator
        y0, y1 = int(min(vmin, vmax)) * fator, int(max(vmin, vmax)) * fator
        img[y0, x0:x1] = cor
        img[y1 - 1, x0:x1] = cor
        img[y0:y1, x0] = cor
        img[y0:y1, x1 - 1] = cor
    return img


def pulso_coracao(t):
    """Batida dupla (tum-tum) e descanso. t em 0..1 (um ciclo)."""
    def batida(u):
        return math.exp(-((u / 0.09) ** 2)) if u >= 0 else 0.0
    p = batida(t) + 0.75 * batida(t - 0.2)
    return 0.22 + 0.78 * min(1.0, p)


def folha_antes_depois(s, skins, v1, v2, bri):
    esc = 8
    cw, ch = 20 * esc, 34 * esc
    gap = 10
    M = 150
    xs_antes = [M, M + cw + gap]
    x_dep = xs_antes[-1] + cw + 50
    xs_dep = [x_dep, x_dep + cw + gap, x_dep + 2 * (cw + gap)]
    x_molde = xs_dep[-1] + cw + 40
    W = x_molde + 64 * 4 + 24
    y0 = 96
    H = y0 + len(COMBOS) * (ch + gap) + 10
    folha = nova(W, H, FUNDO)
    texto(folha, 16, 12, f"Marca de Sangue - estagio {s}: antes (PR 25) x depois (v2)", BRANCO, 3)
    texto(folha, xs_antes[0], 50, "ANTES (PR 25)", (255, 170, 150), 2)
    texto(folha, xs_dep[0], 50, "DEPOIS (v2)", (150, 230, 160), 2)
    for x, nome in zip(xs_antes, ("frente", "costas")):
        texto(folha, x + 6, 72, nome, CINZA, 2)
    for x, nome in zip(xs_dep, ("frente", "3/4 lado dir.", "costas")):
        texto(folha, x + 6, 72, nome, CINZA, 2)
    for i, (tom, alex) in enumerate(COMBOS):
        y = y0 + i * (ch + gap)
        texto(folha, 14, y + ch // 2 - 16, nome_combo(tom, alex), (210, 210, 210), 2)
        sk = skins[(tom, alex)]
        for x, yaw in zip(xs_antes, (0, 180)):
            colar(folha, render(alex, sk, v1[(s, alex)], yaw, esc=esc), x, y)
        for x, yaw in zip(xs_dep, (0, -35, 180)):
            colar(folha, render(alex, sk, v2[(s, alex)], yaw, esc=esc), x, y)
        if i in (0, 2):
            mold = xadrez(v2[(s, alex)], 4)
            colar(folha, mold, x_molde, y)
            texto(folha, x_molde, y + 64 * 4 + 6, f"molde 64x64 ({'Alex' if alex else 'Steve'})", CINZA, 2)
        if i in (1, 3):
            mold = xadrez(bri[(s, alex)], 4, escuro=(0x0A, 0x0A, 0x0E, 255), claro=(0x12, 0x12, 0x16, 255))
            colar(folha, mold, x_molde, y)
            texto(folha, x_molde, y + 64 * 4 + 6, f"so o brilho ({'Alex' if alex else 'Steve'})", CINZA, 2)
    salvar_png(os.path.join(DIR_PREVIA, f"antes_depois_estagio_{s}.png"), folha)


def folha_zoom(s, skins, v2, bri):
    """Estagio em 16x: tronco + bracos de frente, de costas, 3/4 e a ferida de perto."""
    esc = 16
    jan = (-9, -1, 5, 13)
    vistas = [("frente (16x)", 0, jan, esc), ("costas (16x)", 180, jan, esc), ("3/4 lado dir. (16x)", -35, jan, esc),
              ("ferida (24x)", 0, (-9, 4, -3, 13), 24), ("no escuro, brilho (16x)", 0, jan, esc)]
    combos = [("clara", False), ("escura", False), ("clara", True)]
    cw = [int((j[2] - j[0]) * e) for _, _, j, e in vistas]
    chs = [int((j[3] - j[1]) * e) for _, _, j, e in vistas]
    ch = max(chs)
    gap = 12
    M = 150
    xs = [M]
    for w in cw[:-1]:
        xs.append(xs[-1] + w + gap)
    W = xs[-1] + cw[-1] + 20
    y0 = 80
    H = y0 + len(combos) * (ch + gap)
    folha = nova(W, H, FUNDO)
    texto(folha, 16, 12, f"Marca de Sangue v2 - estagio {s} de perto", BRANCO, 3)
    for x, (nome, _, _, _) in zip(xs, vistas):
        texto(folha, x, 54, nome, CINZA, 2)
    for i, (tom, alex) in enumerate(combos):
        y = y0 + i * (ch + gap)
        texto(folha, 14, y + ch // 2 - 16, nome_combo(tom, alex), (210, 210, 210), 2)
        for x, (nome, yaw, j, e) in zip(xs, vistas):
            escuro = nome.startswith("no escuro")
            img = render(alex, skins[(tom, alex)], v2[(s, alex)], yaw, esc=e, janela=j, escuro=escuro,
                         brilho=bri[(s, alex)] if escuro else None, pulso=0.9)
            colar(folha, img, x, y)
    salvar_png(os.path.join(DIR_PREVIA, f"zoom_estagio_{s}.png"), folha)


def folha_ferida(skins, v2):
    """A ferida de origem em 40x, estagios 1 a 4 (para conferir talho, crosta, costura e gotas)."""
    esc = 40
    jan = (-9, 4, -3, 13)
    cw, ch = int(6 * esc), int(9 * esc)
    combos = [("clara", False), ("escura", False), ("clara", True)]
    M = 150
    W = M + (cw + 16) * 4
    H = 70 + (ch + 16) * len(combos)
    folha = nova(W, H, FUNDO)
    texto(folha, 16, 12, "Ferida de origem (antebraco direito) - estagios 1 a 4, 40x", BRANCO, 3)
    for s in range(1, 5):
        texto(folha, M + (s - 1) * (cw + 16), 48, f"estagio {s}", CINZA, 2)
    for i, (tom, alex) in enumerate(combos):
        y = 70 + i * (ch + 16)
        texto(folha, 14, y + ch // 2 - 16, nome_combo(tom, alex), (210, 210, 210), 2)
        for s in range(1, 5):
            colar(folha, render(alex, skins[(tom, alex)], v2[(s, alex)], 0, esc=esc, janela=jan), M + (s - 1) * (cw + 16), y)
    salvar_png(os.path.join(DIR_PREVIA, "ferida_de_perto.png"), folha)


def folha_comparacao_hd(skins, v2, hd):
    """Mesma arte em 64x64 (igual a skin) e em 128x128 (dobro de detalhe), lado a lado, para o Nikolas escolher."""
    esc = 8
    jan16 = (-9, -1, 5, 13)
    jan_fer = (-9, 4, -3, 13)
    linhas = [(2, "clara", False), (3, "clara", False), (4, "clara", False), (4, "escura", False), (4, "clara", True)]
    cols = [("64x64\ncorpo", v2, None, esc), ("128x128\ncorpo", hd, None, esc),
            ("64x64\nde perto 16x", v2, jan16, 16), ("128x128\nde perto 16x", hd, jan16, 16),
            ("64x64\nferida 40x", v2, jan_fer, 40), ("128x128\nferida 40x", hd, jan_fer, 40)]
    larg = [20 * esc, 20 * esc, 14 * 16, 14 * 16, 6 * 40, 6 * 40]
    M = 150
    gap = 14
    xs = [M]
    for w in larg[:-1]:
        xs.append(xs[-1] + w + gap)
    W = xs[-1] + larg[-1] + 20
    ch = 34 * esc + gap
    y0 = 100
    H = y0 + ch * len(linhas)
    folha = nova(W, H, FUNDO)
    texto(folha, 16, 12, "Marca de Sangue v2 - mesma arte em 64x64 e em 128x128 (decisao de resolucao pendente)", BRANCO, 3)
    for x, (nome, _, _, _) in zip(xs, cols):
        texto(folha, x, 50, nome, CINZA, 2)
    for i, (s, tom, alex) in enumerate(linhas):
        y = y0 + i * ch
        texto(folha, 14, y + ch // 2 - 24, f"estagio {s}\n" + nome_combo(tom, alex), (210, 210, 210), 2)
        for x, (nome, fonte, jan, e) in zip(xs, cols):
            if jan is None:
                img = render(alex, skins[(tom, alex)], fonte[(s, alex)], 0, esc=e)
            else:
                img = render(alex, skins[(tom, alex)], fonte[(s, alex)], 0, esc=e, janela=jan)
            colar(folha, img, x, y)
    salvar_png(os.path.join(DIR_PREVIA, "comparacao_64_vs_128.png"), folha)


def folha_geral(skins, v2, bri):
    esc = 6
    cw, ch = 20 * esc, 34 * esc
    cols = [(tom, alex, yaw) for alex in (False, True) for tom in ("clara", "escura") for yaw in (0, 180)]
    M = 200
    W = M + (cw + 24) * len(cols)
    H = 96 + (ch + 6) * 6
    folha = nova(W, H, FUNDO)
    texto(folha, 16, 12, "Marca de Sangue v2 - todos os estagios (previa, nao integrada)", BRANCO, 3)
    for j, (tom, alex, yaw) in enumerate(cols):
        texto(folha, M + j * (cw + 24) + 4, 46, f"{'Alex' if alex else 'Steve'}\n{tom}\n{'frente' if yaw == 0 else 'costas'}", CINZA, 2)
    descr = {0: "0: sem marca", 1: "1: arranhao no\n   antebraco", 2: "2: sobe ao peito\n   e as costas",
             3: "3: braco esq.,\n   pescoco,\n   coracao,\n   costura", 4: "4: pernas,\n   rachaduras,\n   gotas, olho"}
    for s in range(5):
        y = 96 + s * (ch + 6)
        texto(folha, 12, y + ch // 2 - 20, descr[s], (210, 210, 210), 2)
        for j, (tom, alex, yaw) in enumerate(cols):
            colar(folha, render(alex, skins[(tom, alex)], v2[(s, alex)], yaw, esc=esc), M + j * (cw + 24), y)
    y = 96 + 5 * (ch + 6)
    texto(folha, 12, y + ch // 2 - 20, "4 no escuro\n(so o brilho)", (210, 210, 210), 2)
    for j, (tom, alex, yaw) in enumerate(cols):
        colar(folha, render(alex, skins[(tom, alex)], v2[(4, alex)], yaw, esc=esc, escuro=True, brilho=bri[(4, alex)], pulso=0.9),
              M + j * (cw + 24), y)
    salvar_png(os.path.join(DIR_PREVIA, "folha_estagios_v2.png"), folha)


def gif_brilho(skins, v2, bri):
    esc = 8
    cw, ch = 20 * esc, 34 * esc
    vistas = [("clara", False, 0), ("clara", False, 180), ("escura", True, 0), ("escura", True, -35)]
    M = 16
    W = M + (cw + 8) * len(vistas) + 8
    H = 70 + ch + 30
    n = 28
    quadros = []
    for k in range(n):
        p = pulso_coracao(k / n)
        q = nova(W, H, (0x08, 0x08, 0x0C, 255))
        texto(q, 12, 10, "estagio 4 no escuro: so rachaduras, ferida e coracao brilham", (200, 200, 200), 2)
        texto(q, 12, 36, "pulso: animacao que a mesa Dev liga depois. brilho: estagio_4_brilho.png", (120, 120, 130), 2)
        for j, (tom, alex, yaw) in enumerate(vistas):
            img = render(alex, skins[(tom, alex)], v2[(4, alex)], yaw, esc=esc, escuro=True, brilho=bri[(4, alex)], pulso=p)
            colar(q, img, M + j * (cw + 8), 70)
        # barra do pulso
        bx, by = M, 70 + ch + 10
        q[by:by + 6, bx:bx + int((W - 2 * M) * 0.25)] = (60, 20, 24, 255)
        q[by:by + 6, bx:bx + int((W - 2 * M) * 0.25 * p)] = (0xE0, 0x31, 0x3A, 255)
        quadros.append(q[..., :3].copy())
    salvar_gif(os.path.join(DIR_PREVIA, "brilho_pulsando.gif"), quadros, atraso_cs=5)


def main():
    os.makedirs(DIR_V2, exist_ok=True)
    os.makedirs(DIR_PREVIA, exist_ok=True)
    so_texturas = "--so-texturas" in sys.argv
    skins = skins_teste()
    v2, bri, v1, hd, hd_bri = {}, {}, {}, {}, {}
    os.makedirs(os.path.join(DIR_V2, "hd"), exist_ok=True)
    for alex in (False, True):
        suf = "_alex" if alex else ""
        m = Molde(alex)
        m_hd = Molde(alex, F=2)
        v2[(0, alex)] = VAZIO
        for s in range(1, 5):
            tex, b = m.gerar(s)
            v2[(s, alex)] = tex
            bri[(s, alex)] = b
            salvar_png(os.path.join(DIR_V2, f"estagio_{s}{suf}.png"), tex)
            salvar_png(os.path.join(DIR_V2, f"estagio_{s}_brilho{suf}.png"), b)
            tex, b = m_hd.gerar(s)
            hd[(s, alex)] = tex
            hd_bri[(s, alex)] = b
            salvar_png(os.path.join(DIR_V2, "hd", f"estagio_{s}{suf}.png"), tex)
            salvar_png(os.path.join(DIR_V2, "hd", f"estagio_{s}_brilho{suf}.png"), b)
            v1[(s, alex)] = carregar_v1(s, alex)
        guia = grade_faces(xadrez(v2[(4, alex)], 8), 8, alex)
        salvar_png(os.path.join(DIR_PREVIA, f"molde_guia_estagio_4_{'alex' if alex else 'steve'}.png"), guia)
    print("texturas ok:", DIR_V2)
    if so_texturas:
        return
    folha_comparacao_hd(skins, v2, hd)
    for s in range(1, 5):
        folha_antes_depois(s, skins, v1, v2, bri)
        print("previa estagio", s)
    for s in range(1, 5):
        folha_zoom(s, skins, v2, bri)
    folha_ferida(skins, v2)
    folha_geral(skins, v2, bri)
    gif_brilho(skins, v2, bri)
    print("previas ok:", DIR_PREVIA)


if __name__ == "__main__":
    main()
