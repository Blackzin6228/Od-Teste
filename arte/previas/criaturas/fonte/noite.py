"""Renders de prévia "de noite": luz da lua fria vinda de cima e de lado, ambiente baixo, céu e chão escuros,
neblina avermelhada rente ao chão (a da Vila Calada) e vinheta. Também as folhas de comparação do Faminto.
Só python3 + numpy (usa ferramentas/render_geckolib.py)."""
import numpy as np

import render_geckolib as rg

CEU_ALTO = np.array([8, 9, 16], float)
CEU_BAIXO = np.array([22, 22, 32], float)
NEBLINA = np.array([70, 40, 44], float)


def fundo_noite(w, h, y_chao):
    img = np.zeros((h, w, 4), np.uint8)
    t = np.linspace(0, 1, h)[:, None]
    ceu = CEU_ALTO * (1 - t) + CEU_BAIXO * t
    img[:, :, :3] = ceu[:, None, :].astype(np.uint8).repeat(w, axis=1) if ceu.ndim == 3 else np.repeat(ceu[:, None, :], w, axis=1)
    img[:, :, 3] = 255
    if 0 < y_chao < h:
        chao = img[y_chao:, :, :3].astype(float)
        k = np.linspace(0.9, 0.55, h - y_chao)[:, None, None]
        img[y_chao:, :, :3] = (chao * k + np.array([14, 13, 14]) * (1 - k)).astype(np.uint8)
        img[y_chao, :, :3] = (34, 30, 34)
    return img


def camera_noite(az, el):
    cam = rg.Camera(az, el)
    luz = -0.6 * cam.r + 0.7 * cam.u - 0.15 * cam.f
    cam.luz = luz / np.linalg.norm(luz)
    cam.ambiente = 0.24
    return cam


def gradar(rgb):
    """Visão de noite: tira saturação, puxa para o azul; o vermelho do acento sobrevive um pouco."""
    rgb = rgb.astype(float)
    lum = rgb @ np.array([0.3, 0.55, 0.15])
    vermelho = np.clip((rgb[..., 0] - np.maximum(rgb[..., 1], rgb[..., 2]) * 1.4) / 80.0, 0, 1)
    frio = np.stack([lum * 0.72, lum * 0.82, lum * 1.12], axis=-1)
    quente = rgb * np.array([0.8, 0.55, 0.6])
    return frio * (1 - vermelho[..., None] * 0.75) + quente * (vermelho[..., None] * 0.75)


def render_noite(modelo, tex, az, el, escala, tamanho, centro, pose=None, rotulo=None):
    w, h = tamanho
    cam = camera_noite(az, el)
    fig = rg.renderizar(modelo, tex, cam, pose=pose, escala=escala, tamanho=tamanho, centro=centro,
                        fundo=(0, 0, 0, 0), chao=False, regua=False)
    y_chao = int(round(float(cam.projetar([[centro[0], 0, centro[2]]], centro, escala, w, h)[1][0])))
    img = fundo_noite(w, h, y_chao if el < 5 else h + 1)
    m = fig[:, :, 3] > 0
    img[m, :3] = np.clip(gradar(fig[m, :3]), 0, 255).astype(np.uint8)
    # sombra de contato e neblina rente ao chão (até ~0,4 bloco)
    if el < 5:
        alt = int(escala * 7)
        for y in range(max(0, y_chao - alt), min(h, y_chao + 6)):
            a = 0.42 * (1 - abs(y - y_chao) / alt) ** 1.6
            onda = 0.5 + 0.5 * np.sin(np.arange(w) * 0.045 + y * 0.3)
            aa = a * (0.7 + 0.3 * onda)
            img[y, :, :3] = (img[y, :, :3] * (1 - aa[:, None]) + NEBLINA * aa[:, None]).astype(np.uint8)
    # vinheta
    Y, X = np.mgrid[0:h, 0:w]
    d = ((X - w / 2) / (w * 0.62)) ** 2 + ((Y - h / 2) / (h * 0.62)) ** 2
    v = np.clip(1 - 0.55 * d, 0.35, 1)[:, :, None]
    img[:, :, :3] = (img[:, :, :3] * v).astype(np.uint8)
    if rotulo:
        rg.texto(img, (8, 8), rotulo, (190, 196, 214, 255), 2)
    return img


def folha_noite(modelo, tex, caminho, titulo, escala=7.0):
    tam = rg.tamanho_para(modelo, escala)
    cen = rg.centro_para(modelo)
    vistas = [render_noite(modelo, tex, az, el, escala, tam, cen, rotulo=r) for r, az, el in rg.VISTAS]
    folha = rg.juntar_vertical([rg.faixa_titulo(titulo, sum(v.shape[1] for v in vistas) + 18), rg.juntar_horizontal(vistas)])
    rg.salvar_png(caminho, folha)


def _pose_final(modelo_dir, nome_anim):
    a = rg.Animacao(modelo_dir, nome_anim)
    return a, a.pose(a.duracao)


def comparacao_conceitos(res, nomes, caminho):
    linhas = []
    for var, (modelo, tex) in res.items():
        esc = 6.0
        tam = rg.tamanho_para(modelo, esc)
        cen = rg.centro_para(modelo)
        imgs = [rg.renderizar(modelo, tex, rg.Camera(az, el), escala=esc, tamanho=tam, centro=cen, rotulo=r)
                for r, az, el in (("FRENTE", 0, 0), ("3/4", 35, 18), ("LADO", 90, 0), ("COSTAS", 180, 0))]
        imgs.append(render_noite(modelo, tex, 35, 0, esc, tam, cen, rotulo="NOITE"))
        imgs.append(rg.renderizar(modelo, tex, rg.Camera(90, 0), escala=esc, tamanho=tam, centro=cen, fundo=(214, 214, 210, 255),
                                  silhueta=True, regua=False, rotulo=None))
        linha = rg.juntar_horizontal(imgs)
        linhas.append(rg.faixa_titulo(nomes[var], linha.shape[1]))
        linhas.append(linha)
    rg.salvar_png(caminho, rg.juntar_vertical(linhas))


def escuro_30_blocos(res, nomes, caminho):
    """Tamanho real na tela a ~30 blocos (tela 1080p, FOV 70: um bloco ~25 px de altura) e ampliado 4x para ler.
    Linha 1: silhueta preta contra o céu da noite. Linha 2: com a luz da lua e a neblina."""
    escala_real = 25.0 / 16.0
    quadros = []
    for var, (modelo, tex) in res.items():
        cen = rg.centro_para(modelo)
        tam = (60, 70)
        sil = rg.renderizar(modelo, tex, rg.Camera(90, 0), escala=escala_real, tamanho=tam, centro=cen,
                            fundo=(0, 0, 0, 0), chao=False, regua=False, silhueta=True)
        ceu = fundo_noite(tam[0], tam[1], tam[1] + 1)
        ceu[:, :, :3] = np.clip(ceu[:, :, :3].astype(int) + 26, 0, 255).astype(np.uint8)  # céu um pouco mais claro que a figura
        m = sil[:, :, 3] > 0
        ceu[m, :3] = (4, 4, 6)
        luz = render_noite(modelo, tex, 35, 0, escala_real, tam, cen)
        quadros.append((var, ceu, luz))
    linhas = []
    for idx, txt in ((1, "SILHUETA CONTRA O CEU"), (2, "LUZ DA LUA E NEBLINA")):
        imgs = []
        for var, ceu, luz in quadros:
            im = ampliar(ceu if idx == 1 else luz, 4)
            rg.texto(im, (6, 6), f"{var.upper()}", (200, 200, 210, 255), 2)
            imgs.append(im)
        linha = rg.juntar_horizontal(imgs)
        linhas.append(rg.faixa_titulo(f"A 30 BLOCOS (AMPLIADO 4X): {txt}", max(linha.shape[1], 480)))
        linhas.append(linha)
    rg.salvar_png(caminho, rg.juntar_vertical(linhas))


def ampliar(img, k):
    return np.repeat(np.repeat(img, k, axis=0), k, axis=1)


def antes_depois(antes_png, res, nomes, caminho):
    """Mesma escala do render antigo (~9,5 px por unidade), mesmas quatro vistas."""
    partes = []
    largura = 1480
    if antes_png is not None:
        a = rg.ler_png(antes_png)
        rg.texto(a, (8, 8), "ANTES: 13 CUBOS, 16 PX POR BLOCO, SEM ANIMACAO", (235, 235, 235, 255), 2)
        largura = a.shape[1]
        partes.append(a)
    for var, (modelo, tex) in res.items():
        esc = 9.5
        tam = (360, 400)
        cen = rg.centro_para(modelo)
        imgs = [rg.renderizar(modelo, tex, rg.Camera(az, el), escala=esc, tamanho=tam, centro=cen, regua=False,
                              rotulo=(f"DEPOIS {var.upper()}" if az == 0 else None))
                for az, el in ((0, 0), (90, 0), (180, 0), (35, 15))]
        partes.append(rg.juntar_horizontal(imgs))
    partes.insert(0, rg.faixa_titulo("FAMINTO: ANTES X DEPOIS (A E B: 32 PX POR BLOCO, 9 ANIMACOES)", largura))
    rg.salvar_png(caminho, rg.juntar_vertical(partes))


def ajoelhados(res, nomes, caminho):
    linhas = []
    for var, (modelo, tex) in res.items():
        arq = caminho.parent / var / f"faminto_{var}.animation.json"
        esc = 6.0
        aj = rg.Animacao(arq, f"animation.faminto_{var}.ajoelhar")
        cz = rg.Animacao(arq, f"animation.faminto_{var}.cinza")
        es = rg.Animacao(arq, f"animation.faminto_{var}.escutar")
        poses = [("ESCUTA O TOQUE", es.pose(1.2)), ("CEDE", aj.pose(1.3)), ("DE JOELHOS", aj.pose(aj.duracao)),
                 ("VIRA CINZA", cz.pose(1.8))]
        tam = rg.tamanho_para(modelo, esc)
        cen = rg.centro_para(modelo)
        imgs = []
        for r, p in poses:
            imgs.append(render_noite(modelo, tex, 60, 0, esc, tam, cen, pose=p, rotulo=r))
        imgs.append(rg.renderizar(modelo, tex, rg.Camera(35, 15), pose=aj.pose(aj.duracao), escala=esc, tamanho=tam, centro=cen,
                                  rotulo="DE JOELHOS 3/4"))
        linha = rg.juntar_horizontal(imgs)
        linhas.append(rg.faixa_titulo(f"{nomes[var]}: FIM DO DOBRE", linha.shape[1]))
        linhas.append(linha)
    rg.salvar_png(caminho, rg.juntar_vertical(linhas))
