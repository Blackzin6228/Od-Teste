"""Textura PROVISÓRIA do Martelo do Ferreiro (16x16), desenhada em código pela mesa Dev (14/11/2026).
O Modelador troca o PNG (ou o modelo) no mesmo caminho. Uso: python3 ferramentas/itens/martelo_provisorio.py"""
from PIL import Image

T = (0, 0, 0, 0)
MADEIRA = (74, 48, 30, 255)
MADEIRA_CLARA = (98, 66, 40, 255)
FERRO = (92, 92, 98, 255)
FERRO_CLARO = (136, 136, 142, 255)
FERRO_ESCURO = (52, 52, 58, 255)
FERRUGEM = (110, 30, 22, 255)

img = Image.new("RGBA", (16, 16), T)
px = img.load()
# Cabo na diagonal (de baixo à esquerda até o meio).
for i in range(2, 11):
    x, y = i, 15 - i
    px[x, y] = MADEIRA
    if i % 3 == 0:
        px[x + 1, y] = MADEIRA_CLARA
# Cabeça de ferro, atravessada no alto à direita.
cabeca = [(9, 2), (10, 2), (11, 3), (12, 4), (13, 5), (8, 3), (9, 3), (10, 3), (11, 4), (12, 5), (13, 6),
          (7, 4), (8, 4), (9, 4), (10, 4), (11, 5), (12, 6), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (11, 6)]
for c in cabeca:
    px[c] = FERRO
for c in [(9, 2), (10, 2), (8, 3), (7, 4), (6, 5)]:
    px[c] = FERRO_CLARO
for c in [(13, 6), (12, 6), (11, 6), (10, 5)]:
    px[c] = FERRO_ESCURO
# Racha e sangue seco na face de bater.
px[11, 4] = FERRO_ESCURO
px[12, 5] = FERRUGEM
px[13, 5] = FERRUGEM
img.save("src/main/resources/assets/odteste/textures/item/martelo_do_ferreiro.png")
