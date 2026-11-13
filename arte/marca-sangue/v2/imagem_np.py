"""Imagem RGBA com numpy, sem Pillow: ler/salvar PNG, salvar GIF animado (LZW proprio), colar com
transparencia, ampliar e escrever texto com a fonte de pixel da v1 (arte nossa).

Por que sem Pillow: a maquina do projeto nao tem o Pillow instalado e o gerador precisa rodar em qualquer
lugar com `python3` + numpy. Tudo aqui e biblioteca padrao + numpy."""
import os
import struct
import sys
import zlib

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from imagem_simples import FONTE  # noqa: E402  (fonte 5x7 feita por nos, na v1)


# ---------------------------------------------------------------- PNG
def salvar_png(caminho, arr):
    """arr: uint8 (H, W, 4) ou (H, W, 3)."""
    arr = np.ascontiguousarray(arr, dtype=np.uint8)
    h, w, c = arr.shape
    tipo_cor = 6 if c == 4 else 2
    linhas = np.concatenate([np.zeros((h, 1), np.uint8), arr.reshape(h, w * c)], axis=1)

    def bloco(tipo, dados):
        return struct.pack(">I", len(dados)) + tipo + dados + struct.pack(">I", zlib.crc32(tipo + dados) & 0xFFFFFFFF)

    with open(caminho, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(bloco(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, tipo_cor, 0, 0, 0)))
        f.write(bloco(b"IDAT", zlib.compress(linhas.tobytes(), 9)))
        f.write(bloco(b"IEND", b""))


def ler_png(caminho):
    """Le PNG de 8 bits (cinza, cinza+alfa, RGB, RGBA ou paleta), sem entrelacamento. Devolve uint8 (H, W, 4)."""
    with open(caminho, "rb") as f:
        dados = f.read()
    if dados[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"nao e PNG: {caminho}")
    pos, idat, plte, trns = 8, [], None, None
    w = h = prof = tipo = inter = None
    while pos < len(dados):
        n = struct.unpack(">I", dados[pos:pos + 4])[0]
        tag, corpo = dados[pos + 4:pos + 8], dados[pos + 8:pos + 8 + n]
        pos += 12 + n
        if tag == b"IHDR":
            w, h, prof, tipo, _, _, inter = struct.unpack(">IIBBBBB", corpo)
        elif tag == b"IDAT":
            idat.append(corpo)
        elif tag == b"PLTE":
            plte = np.frombuffer(corpo, np.uint8).reshape(-1, 3)
        elif tag == b"tRNS":
            trns = np.frombuffer(corpo, np.uint8)
        elif tag == b"IEND":
            break
    if prof != 8 or inter != 0:
        raise ValueError(f"PNG com profundidade {prof}/entrelacado {inter} nao suportado: {caminho}")
    canais = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[tipo]
    raw = zlib.decompress(b"".join(idat))
    passo = w * canais
    saida = np.zeros((h, passo), np.uint8)
    anterior = np.zeros(passo, np.uint8)
    for y in range(h):
        base = y * (passo + 1)
        filtro = raw[base]
        linha = np.frombuffer(raw, np.uint8, passo, base + 1).copy()
        if filtro == 1:
            for i in range(canais, passo):
                linha[i] = (int(linha[i]) + int(linha[i - canais])) & 255
        elif filtro == 2:
            linha = (linha.astype(np.uint16) + anterior).astype(np.uint8)
        elif filtro == 3:
            for i in range(passo):
                esq = int(linha[i - canais]) if i >= canais else 0
                linha[i] = (int(linha[i]) + ((esq + int(anterior[i])) >> 1)) & 255
        elif filtro == 4:
            for i in range(passo):
                a = int(linha[i - canais]) if i >= canais else 0
                b = int(anterior[i])
                c = int(anterior[i - canais]) if i >= canais else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pred = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                linha[i] = (int(linha[i]) + pred) & 255
        saida[y] = linha
        anterior = linha
    px = saida.reshape(h, w, canais)
    if tipo == 6:
        return px.copy()
    rgba = np.zeros((h, w, 4), np.uint8)
    rgba[..., 3] = 255
    if tipo == 2:
        rgba[..., :3] = px
    elif tipo == 0:
        rgba[..., :3] = px
    elif tipo == 4:
        rgba[..., :3] = px[..., :1]
        rgba[..., 3] = px[..., 1]
    elif tipo == 3:
        rgba[..., :3] = plte[px[..., 0]]
        if trns is not None:
            alfa = np.full(len(plte), 255, np.uint8)
            alfa[:len(trns)] = trns
            rgba[..., 3] = alfa[px[..., 0]]
    return rgba


# ---------------------------------------------------------------- GIF animado (paleta global, LZW)
def _quantizar(quadros):
    """Reduz todos os quadros (H, W, 3) a uma paleta de ate 256 cores. Devolve (paleta uint8 (256,3), indices)."""
    todos = np.concatenate([q.reshape(-1, 3) for q in quadros]).astype(np.int64)
    chave = (todos[:, 0] >> 3) << 10 | (todos[:, 1] >> 3) << 5 | (todos[:, 2] >> 3)
    unicos, inverso, contagem = np.unique(chave, return_inverse=True, return_counts=True)
    # cor media de cada chave (fica mais fiel que o centro do cubo)
    soma = np.zeros((len(unicos), 3), np.int64)
    np.add.at(soma, inverso, todos)
    media = soma / contagem[:, None]
    if len(unicos) <= 256:
        paleta = media
        mapa = np.arange(len(unicos))
    else:
        ordem = np.argsort(-contagem)
        escolhidas = ordem[:256]
        paleta = media[escolhidas]
        # as demais vao para a cor mais proxima da paleta
        d = ((media[:, None, :] - paleta[None, :, :]) ** 2).sum(axis=2)
        mapa = np.argmin(d, axis=1)
    pal = np.zeros((256, 3), np.uint8)
    pal[:len(paleta)] = np.clip(np.round(paleta), 0, 255)
    indices = []
    ini = 0
    for q in quadros:
        n = q.shape[0] * q.shape[1]
        indices.append(mapa[inverso[ini:ini + n]].astype(np.uint16).reshape(q.shape[:2]))
        ini += n
    return pal, indices


def _lzw(indices, min_code=8):
    """Codifica indices (lista/array 1D de ints < 256) no LZW do GIF. Devolve bytes (sem sub-blocos)."""
    limpar, fim = 1 << min_code, (1 << min_code) + 1
    saida = bytearray()
    acumulador = 0
    nbits = 0

    def emitir(codigo, tamanho):
        nonlocal acumulador, nbits
        acumulador |= codigo << nbits
        nbits += tamanho
        while nbits >= 8:
            saida.append(acumulador & 255)
            acumulador >>= 8
            nbits -= 8

    tamanho = min_code + 1
    dic = {}
    proximo = fim + 1
    emitir(limpar, tamanho)
    it = iter(indices)
    prefixo = int(next(it))
    for k in it:
        k = int(k)
        chave = (prefixo, k)
        c = dic.get(chave)
        if c is not None:
            prefixo = c
            continue
        emitir(prefixo, tamanho)
        if proximo >= (1 << tamanho) and tamanho < 12:
            tamanho += 1
        if proximo < 4096:
            dic[chave] = proximo
            proximo += 1
        else:
            emitir(limpar, tamanho)
            dic.clear()
            proximo = fim + 1
            tamanho = min_code + 1
        prefixo = k
    emitir(prefixo, tamanho)
    if proximo >= (1 << tamanho) and tamanho < 12:
        tamanho += 1
    emitir(fim, tamanho)
    if nbits:
        saida.append(acumulador & 255)
    return bytes(saida)


def salvar_gif(caminho, quadros, atraso_cs=8, repetir=0):
    """quadros: lista de uint8 (H, W, 3) do mesmo tamanho. atraso_cs em centesimos de segundo."""
    h, w = quadros[0].shape[:2]
    pal, indices = _quantizar(quadros)
    with open(caminho, "wb") as f:
        f.write(b"GIF89a")
        f.write(struct.pack("<HHBBB", w, h, 0xF7, 0, 0))
        f.write(pal.tobytes())
        f.write(b"\x21\xFF\x0BNETSCAPE2.0\x03\x01" + struct.pack("<H", repetir) + b"\x00")
        for idx in indices:
            f.write(b"\x21\xF9\x04\x00" + struct.pack("<H", atraso_cs) + b"\x00\x00")
            f.write(b"\x2C" + struct.pack("<HHHHB", 0, 0, w, h, 0))
            f.write(bytes([8]))
            dados = _lzw(idx.reshape(-1).tolist())
            for i in range(0, len(dados), 255):
                parte = dados[i:i + 255]
                f.write(bytes([len(parte)]) + parte)
            f.write(b"\x00")
        f.write(b"\x3B")


# ---------------------------------------------------------------- composicao
def nova(w, h, cor=(0, 0, 0, 0)):
    img = np.zeros((h, w, 4), np.uint8)
    img[...] = cor
    return img


def colar(destino, origem, x, y):
    """Cola `origem` (RGBA) em `destino` (RGBA) na posicao (x, y), com transparencia (over)."""
    H, W = destino.shape[:2]
    h, w = origem.shape[:2]
    x0, y0 = max(0, x), max(0, y)
    x1, y1 = min(W, x + w), min(H, y + h)
    if x1 <= x0 or y1 <= y0:
        return
    src = origem[y0 - y:y1 - y, x0 - x:x1 - x].astype(np.float32) / 255
    dst = destino[y0:y1, x0:x1].astype(np.float32) / 255
    sa = src[..., 3:4]
    da = dst[..., 3:4]
    oa = sa + da * (1 - sa)
    rgb = np.where(oa > 0, (src[..., :3] * sa + dst[..., :3] * da * (1 - sa)) / np.maximum(oa, 1e-6), 0)
    destino[y0:y1, x0:x1, :3] = np.clip(np.round(rgb * 255), 0, 255)
    destino[y0:y1, x0:x1, 3] = np.clip(np.round(oa[..., 0] * 255), 0, 255)


def ampliar(img, fator):
    return np.repeat(np.repeat(img, fator, axis=0), fator, axis=1)


def xadrez(img, fator, escuro=(0x2C, 0x2C, 0x32, 255), claro=(0x3A, 0x3A, 0x42, 255), bloco=4):
    """Textura ampliada sobre um xadrez (para ver transparencia)."""
    h, w = img.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    fundo = np.where((((xx // bloco) + (yy // bloco)) % 2 == 0)[..., None], np.array(escuro, np.uint8), np.array(claro, np.uint8))
    fundo = fundo.astype(np.uint8)
    colar(fundo, img, 0, 0)
    return ampliar(fundo, fator)


def texto(destino, x, y, txt, cor=(230, 230, 230), escala=2):
    cor = tuple(cor) + ((255,) if len(cor) == 3 else ())
    H, W = destino.shape[:2]
    y0 = y
    for linha in txt.split("\n"):
        cx = x
        for ch in linha.upper():
            g = FONTE.get(ch, FONTE["?"])
            for r, bits in enumerate(g):
                for c, b in enumerate(bits):
                    if b == "#":
                        px, py = cx + c * escala, y0 + r * escala
                        destino[max(0, py):min(H, py + escala), max(0, px):min(W, px + escala)] = cor
            cx += 6 * escala
        y0 += 9 * escala


def largura_texto(txt, escala=2):
    return max(len(l) for l in txt.split("\n")) * 6 * escala
