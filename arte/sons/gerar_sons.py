#!/usr/bin/env python3
"""Gera as prévias de som da Vila Calada (item 1.12 do plano do Ato 1).

Tudo é sintetizado do zero (ruído + filtros), sem amostra de terceiros.
Saída: arte/sons/vento_1.ogg, vento_2.ogg, arrasto_1.ogg, arrasto_2.ogg (mono, 44,1 kHz).

Só usa a biblioteca padrão do Python para gerar o som. Para gravar .ogg tenta, nesta ordem:
  1. o pacote `soundfile` (pip install soundfile);
  2. o programa `ffmpeg`;
  3. o programa `oggenc` (pacote vorbis-tools).
Se nenhum existir, deixa só o .wav e avisa.

Uso:  python3 -I arte/sons/gerar_sons.py
A semente é fixa: rodar de novo dá exatamente o mesmo som.
"""
import math
import os
import random
import shutil
import struct
import subprocess
import sys
import wave

TAXA = 44100
PASTA = os.path.dirname(os.path.abspath(__file__))


# ---------------------------------------------------------------- filtros
class Biquad:
    """Filtro de segunda ordem (fórmulas do 'Audio EQ Cookbook', de domínio público)."""

    def __init__(self, tipo, freq, q):
        self.tipo, self.q = tipo, q
        self.x1 = self.x2 = self.y1 = self.y2 = 0.0
        self.ajustar(freq)

    def ajustar(self, freq):
        w = 2 * math.pi * max(20.0, min(freq, TAXA * 0.45)) / TAXA
        cw, alfa = math.cos(w), math.sin(w) / (2 * self.q)
        if self.tipo == "passa_baixa":
            b0, b1, b2 = (1 - cw) / 2, 1 - cw, (1 - cw) / 2
        elif self.tipo == "passa_alta":
            b0, b1, b2 = (1 + cw) / 2, -(1 + cw), (1 + cw) / 2
        else:  # passa_banda, ganho de pico 1
            b0, b1, b2 = alfa, 0.0, -alfa
        a0, a1, a2 = 1 + alfa, -2 * cw, 1 - alfa
        self.b0, self.b1, self.b2 = b0 / a0, b1 / a0, b2 / a0
        self.a1, self.a2 = a1 / a0, a2 / a0

    def __call__(self, x):
        y = self.b0 * x + self.b1 * self.x1 + self.b2 * self.x2 - self.a1 * self.y1 - self.a2 * self.y2
        self.x2, self.x1, self.y2, self.y1 = self.x1, x, self.y1, y
        return y


class RuidoLento:
    """Valor aleatório que muda devagar (interpolação suave entre pontos sorteados)."""

    def __init__(self, rnd, pontos_por_segundo):
        self.rnd = rnd
        self.passo = pontos_por_segundo / TAXA
        self.fase = 0.0
        self.a, self.b = rnd.random(), rnd.random()

    def __call__(self):
        self.fase += self.passo
        if self.fase >= 1.0:
            self.fase -= 1.0
            self.a, self.b = self.b, self.rnd.random()
        t = self.fase * self.fase * (3 - 2 * self.fase)
        return self.a + (self.b - self.a) * t


def envelope_bordas(sinal, entrada_s, saida_s):
    n = len(sinal)
    ne, ns = int(entrada_s * TAXA), int(saida_s * TAXA)
    for i in range(min(ne, n)):
        sinal[i] *= math.sin(0.5 * math.pi * i / ne) ** 2
    for i in range(min(ns, n)):
        sinal[n - 1 - i] *= math.sin(0.5 * math.pi * i / ns) ** 2
    return sinal


def eco(sinal, atraso_s, retorno, mistura):
    d = int(atraso_s * TAXA)
    saida = list(sinal)
    for i in range(d, len(saida)):
        saida[i] += saida[i - d] * retorno
    return [s * (1 - mistura) + w * mistura for s, w in zip(sinal, saida)]


def normalizar(sinal, pico):
    m = max(1e-9, max(abs(s) for s in sinal))
    return [s * pico / m for s in sinal]


# ---------------------------------------------------------------- vento
def vento(semente, segundos, assobio_hz, rajadas_por_s):
    """Vento entre casas queimadas: sopro grave, rajadas lentas e um assobio fino nas frestas."""
    rnd = random.Random(semente)
    n = int(segundos * TAXA)
    corpo = Biquad("passa_banda", 400, 0.7)
    grave = Biquad("passa_baixa", 220, 0.7)
    fresta = Biquad("passa_banda", assobio_hz, 18)
    tira_ronco = Biquad("passa_alta", 60, 0.7)
    rajada = RuidoLento(rnd, rajadas_por_s)
    tom = RuidoLento(rnd, rajadas_por_s * 0.7)
    tremor = RuidoLento(rnd, 6.0)
    saida = []
    for i in range(n):
        if i % 64 == 0:
            r = rajada()
            corpo.ajustar(250 + 650 * r)
            fresta.ajustar(assobio_hz * (0.85 + 0.3 * tom()))
        forca = 0.25 + 0.75 * r ** 1.6
        x = rnd.uniform(-1, 1)
        s = corpo(x) * forca * 1.6 + grave(x) * (0.5 + 0.5 * forca)
        # o assobio só aparece no auge da rajada, e treme
        s += fresta(x) * 5.0 * max(0.0, forca - 0.55) * (0.6 + 0.4 * tremor())
        saida.append(tira_ronco(s))
    saida = envelope_bordas(saida, 1.5, 2.0)
    return normalizar(saida, 0.6)


# ---------------------------------------------------------------- arrasto
def arrasto(semente, segundos, puxoes, peso_hz):
    """Algo pesado arrastado ao longe: puxões ásperos (prende e solta) na terra e na madeira, com eco."""
    rnd = random.Random(semente)
    n = int(segundos * TAXA)
    cru = [0.0] * n
    aspero = Biquad("passa_banda", 500, 1.2)
    corpo = Biquad("passa_baixa", peso_hz, 0.9)
    # cada puxão: 0,8 a 1,8 s de arrasto, depois silêncio (alguém respirando e puxando de novo)
    t = 0.4
    for _ in range(puxoes):
        dur = rnd.uniform(0.8, 1.8)
        ini, fim = int(t * TAXA), min(n, int((t + dur) * TAXA))
        proximo_grao = ini
        for i in range(ini, fim):
            p = (i - ini) / max(1, fim - ini)
            forca = math.sin(math.pi * p) ** 0.7
            if i >= proximo_grao:  # prende e solta: grãos irregulares de 8 a 22 por segundo
                proximo_grao = i + int(TAXA / rnd.uniform(8, 22))
                ganho_grao = rnd.uniform(0.5, 1.0)
                inicio_grao = i
            g = math.exp(-(i - inicio_grao) / (0.012 * TAXA))
            x = rnd.uniform(-1, 1)
            cru[i] = aspero(x) * forca * (0.35 + 0.65 * g * ganho_grao) + corpo(x) * forca * 2.5
        # baque no fim do puxão: o peso assenta no chão
        for k in range(int(0.25 * TAXA)):
            i = fim + k
            if i < n:
                cru[i] += math.sin(2 * math.pi * peso_hz * 0.5 * k / TAXA) * math.exp(-k / (0.06 * TAXA)) * 0.8
        t += dur + rnd.uniform(0.6, 1.4)
    # longe: tira os agudos e põe eco das casas
    longe = Biquad("passa_baixa", 1100, 0.7)
    saida = [longe(s) for s in cru]
    saida = eco(saida, 0.23, 0.35, 0.35)
    saida = envelope_bordas(saida, 0.3, 1.0)
    return normalizar(saida, 0.7)


# ---------------------------------------------------------------- gravação
def gravar_wav(caminho, sinal):
    with wave.open(caminho, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(TAXA)
        w.writeframes(b"".join(struct.pack("<h", int(max(-1.0, min(1.0, s)) * 32767)) for s in sinal))


def para_ogg(wav, ogg):
    try:
        import soundfile  # noqa: PLC0415

        dados, taxa = soundfile.read(wav)
        soundfile.write(ogg, dados, taxa, format="OGG", subtype="VORBIS")
        return "soundfile"
    except Exception:  # noqa: BLE001
        pass
    if shutil.which("ffmpeg"):
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-ac", "1", "-c:a", "libvorbis", "-q:a", "4", ogg], check=True)
        return "ffmpeg"
    if shutil.which("oggenc"):
        subprocess.run(["oggenc", "-Q", "-q", "4", "-o", ogg, wav], check=True)
        return "oggenc"
    return None


SONS = {
    "vento_1": lambda: vento(semente=1101, segundos=12, assobio_hz=1150, rajadas_por_s=0.35),
    "vento_2": lambda: vento(semente=1102, segundos=12, assobio_hz=820, rajadas_por_s=0.6),
    "arrasto_1": lambda: arrasto(semente=2201, segundos=9, puxoes=3, peso_hz=90),
    "arrasto_2": lambda: arrasto(semente=2202, segundos=9, puxoes=4, peso_hz=65),
}


def main():
    faltou_ogg = False
    for nome, gerar in SONS.items():
        wav = os.path.join(PASTA, nome + ".wav")
        ogg = os.path.join(PASTA, nome + ".ogg")
        gravar_wav(wav, gerar())
        como = para_ogg(wav, ogg)
        if como:
            os.remove(wav)
            print(f"{nome}.ogg gerado ({como})")
        else:
            faltou_ogg = True
            print(f"{nome}.wav gerado (sem codificador .ogg)")
    if faltou_ogg:
        print("Instale soundfile, ffmpeg ou vorbis-tools para sair em .ogg.", file=sys.stderr)


if __name__ == "__main__":
    main()
