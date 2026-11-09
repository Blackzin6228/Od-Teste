"""Imagem RGBA minima em Python puro (sem Pillow): pixel, colar com transparencia, ampliar, poligono,
texto em fonte de pixel 5x7 (feita por nos) e salvar PNG. So o que o gerador da Marca usa."""
import struct
import zlib


class Imagem:
    def __init__(self, w, h, cor=(0, 0, 0, 0)):
        self.size = (w, h)
        self.w, self.h = w, h
        self.p = [tuple(cor)] * (w * h)

    # acesso estilo Pillow: px = img.load(); px[x, y]
    def load(self):
        return self

    def __getitem__(self, k):
        x, y = k
        return self.p[y * self.w + x]

    def __setitem__(self, k, cor):
        x, y = k
        self.p[y * self.w + x] = tuple(cor)

    def alpha_composite(self, outra, pos=(0, 0)):
        ox, oy = pos
        for y in range(outra.h):
            ty = y + oy
            if not 0 <= ty < self.h:
                continue
            for x in range(outra.w):
                tx = x + ox
                if not 0 <= tx < self.w:
                    continue
                c = outra.p[y * outra.w + x]
                a = c[3]
                if a == 0:
                    continue
                i = ty * self.w + tx
                if a == 255:
                    self.p[i] = c
                    continue
                d = self.p[i]
                af = a / 255
                da = d[3] / 255
                oa = af + da * (1 - af)
                if oa <= 0:
                    continue
                cor = tuple(int((c[j] * af + d[j] * da * (1 - af)) / oa + 0.5) for j in range(3))
                self.p[i] = cor + (int(oa * 255 + 0.5),)

    def resize(self, tam, _modo=None):
        nw, nh = tam
        n = Imagem(nw, nh)
        for y in range(nh):
            sy = y * self.h // nh
            for x in range(nw):
                n.p[y * nw + x] = self.p[sy * self.w + x * self.w // nw]
        return n

    def poligono(self, pts, cor):
        """Poligono convexo: pinta pixels cujo centro cai dentro."""
        xs = [q[0] for q in pts]
        ys = [q[1] for q in pts]
        x0, x1 = max(0, int(min(xs))), min(self.w - 1, int(max(xs)) + 1)
        y0, y1 = max(0, int(min(ys))), min(self.h - 1, int(max(ys)) + 1)
        n = len(pts)
        arestas = [(pts[i], pts[(i + 1) % n]) for i in range(n)]
        area = sum(a[0] * b[1] - b[0] * a[1] for a, b in arestas)
        if abs(area) < 1e-9:
            return
        sinal = 1 if area > 0 else -1
        cor = tuple(cor)
        for y in range(y0, y1 + 1):
            cy = y + 0.5
            for x in range(x0, x1 + 1):
                cx = x + 0.5
                dentro = True
                for a, b in arestas:
                    if sinal * ((b[0] - a[0]) * (cy - a[1]) - (b[1] - a[1]) * (cx - a[0])) < -1e-9:
                        dentro = False
                        break
                if dentro:
                    self.p[y * self.w + x] = cor

    def texto(self, pos, txt, cor, escala=2):
        x0, y = pos
        cor = tuple(cor) + ((255,) if len(cor) == 3 else ())
        for linha in txt.split("\n"):
            x = x0
            for ch in linha.upper():
                g = FONTE.get(ch, FONTE["?"])
                for r, bits in enumerate(g):
                    for c, b in enumerate(bits):
                        if b == "#":
                            for dy in range(escala):
                                for dx in range(escala):
                                    px, py = x + c * escala + dx, y + r * escala + dy
                                    if 0 <= px < self.w and 0 <= py < self.h:
                                        self.p[py * self.w + px] = cor
                x += 6 * escala
            y += 9 * escala

    def save(self, caminho):
        raw = bytearray()
        for y in range(self.h):
            raw.append(0)
            for c in self.p[y * self.w:(y + 1) * self.w]:
                raw.extend(bytes((c[0] & 255, c[1] & 255, c[2] & 255, c[3] & 255)))

        def bloco(tipo, dados):
            return struct.pack(">I", len(dados)) + tipo + dados + struct.pack(">I", zlib.crc32(tipo + dados) & 0xFFFFFFFF)

        with open(caminho, "wb") as f:
            f.write(b"\x89PNG\r\n\x1a\n")
            f.write(bloco(b"IHDR", struct.pack(">IIBBBBB", self.w, self.h, 8, 6, 0, 0, 0)))
            f.write(bloco(b"IDAT", zlib.compress(bytes(raw), 9)))
            f.write(bloco(b"IEND", b""))


def _g(*linhas):
    return linhas


FONTE = {
    " ": _g("     ", "     ", "     ", "     ", "     ", "     ", "     "),
    "?": _g(" ### ", "#   #", "    #", "   # ", "  #  ", "     ", "  #  "),
    "A": _g(" ### ", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"),
    "B": _g("#### ", "#   #", "#   #", "#### ", "#   #", "#   #", "#### "),
    "C": _g(" ### ", "#   #", "#    ", "#    ", "#    ", "#   #", " ### "),
    "D": _g("#### ", "#   #", "#   #", "#   #", "#   #", "#   #", "#### "),
    "E": _g("#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#####"),
    "F": _g("#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#    "),
    "G": _g(" ### ", "#   #", "#    ", "# ###", "#   #", "#   #", " ####"),
    "H": _g("#   #", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"),
    "I": _g(" ### ", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "),
    "J": _g("  ###", "   # ", "   # ", "   # ", "   # ", "#  # ", " ##  "),
    "K": _g("#   #", "#  # ", "# #  ", "##   ", "# #  ", "#  # ", "#   #"),
    "L": _g("#    ", "#    ", "#    ", "#    ", "#    ", "#    ", "#####"),
    "M": _g("#   #", "## ##", "# # #", "# # #", "#   #", "#   #", "#   #"),
    "N": _g("#   #", "##  #", "# # #", "#  ##", "#   #", "#   #", "#   #"),
    "O": _g(" ### ", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "),
    "P": _g("#### ", "#   #", "#   #", "#### ", "#    ", "#    ", "#    "),
    "Q": _g(" ### ", "#   #", "#   #", "#   #", "# # #", "#  # ", " ## #"),
    "R": _g("#### ", "#   #", "#   #", "#### ", "# #  ", "#  # ", "#   #"),
    "S": _g(" ####", "#    ", "#    ", " ### ", "    #", "    #", "#### "),
    "T": _g("#####", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", "  #  "),
    "U": _g("#   #", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "),
    "V": _g("#   #", "#   #", "#   #", "#   #", "#   #", " # # ", "  #  "),
    "W": _g("#   #", "#   #", "#   #", "# # #", "# # #", "## ##", "#   #"),
    "X": _g("#   #", "#   #", " # # ", "  #  ", " # # ", "#   #", "#   #"),
    "Y": _g("#   #", "#   #", " # # ", "  #  ", "  #  ", "  #  ", "  #  "),
    "Z": _g("#####", "    #", "   # ", "  #  ", " #   ", "#    ", "#####"),
    "0": _g(" ### ", "#   #", "#  ##", "# # #", "##  #", "#   #", " ### "),
    "1": _g("  #  ", " ##  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "),
    "2": _g(" ### ", "#   #", "    #", "   # ", "  #  ", " #   ", "#####"),
    "3": _g("#### ", "    #", "    #", " ### ", "    #", "    #", "#### "),
    "4": _g("   # ", "  ## ", " # # ", "#  # ", "#####", "   # ", "   # "),
    "5": _g("#####", "#    ", "#### ", "    #", "    #", "#   #", " ### "),
    "6": _g(" ### ", "#    ", "#    ", "#### ", "#   #", "#   #", " ### "),
    "7": _g("#####", "    #", "   # ", "  #  ", " #   ", " #   ", " #   "),
    "8": _g(" ### ", "#   #", "#   #", " ### ", "#   #", "#   #", " ### "),
    "9": _g(" ### ", "#   #", "#   #", " ####", "    #", "    #", " ### "),
    ":": _g("     ", "  #  ", "  #  ", "     ", "  #  ", "  #  ", "     "),
    ".": _g("     ", "     ", "     ", "     ", "     ", " ##  ", " ##  "),
    ",": _g("     ", "     ", "     ", "     ", " ##  ", "  #  ", " #   "),
    "-": _g("     ", "     ", "     ", " ### ", "     ", "     ", "     "),
    "+": _g("     ", "  #  ", "  #  ", "#####", "  #  ", "  #  ", "     "),
    "/": _g("    #", "    #", "   # ", "  #  ", " #   ", "#    ", "#    "),
    "(": _g("   # ", "  #  ", " #   ", " #   ", " #   ", "  #  ", "   # "),
    ")": _g(" #   ", "  #  ", "   # ", "   # ", "   # ", "  #  ", " #   "),
    "x": None,
}
FONTE["x"] = FONTE["X"]
