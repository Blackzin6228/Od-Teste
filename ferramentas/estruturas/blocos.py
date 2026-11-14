"""Blocos que a ferramenta conhece: só blocos vanilla do Minecraft 1.21.1.

Todo bloco usado numa estrutura precisa estar em BLOCOS. O validador recusa o resto.
Isso garante que a estrutura abre em qualquer pack (ATM10 inclui o vanilla inteiro)
e evita erro de digitação no nome ou numa propriedade.

Cada entrada: nome -> (cor média RGB, padrão de textura da prévia).
As cores são aproximações feitas à mão para a prévia; não são texturas do jogo.
"""

BLOCOS = {
    # pedra e alvenaria
    "cobblestone": ((122, 122, 122), "pedra"),
    "mossy_cobblestone": ((104, 118, 88), "pedra_musgo"),
    "stone_bricks": ((122, 121, 122), "tijolo_pedra"),
    "mossy_stone_bricks": ((112, 120, 100), "tijolo_pedra_musgo"),
    "cracked_stone_bricks": ((116, 115, 116), "tijolo_pedra_rachado"),
    "chiseled_stone_bricks": ((120, 119, 120), "cinzelado"),
    "stone_brick_stairs": ((122, 121, 122), "tijolo_pedra"),
    "stone_brick_slab": ((122, 121, 122), "tijolo_pedra"),
    "polished_andesite": ((132, 135, 134), "liso"),
    "calcite": ((222, 223, 219), "reboco"),
    "diorite": ((189, 188, 189), "pedra"),
    "bricks": ((150, 97, 83), "tijolo"),
    "deepslate_tiles": ((54, 54, 56), "telha"),
    "deepslate_tile_stairs": ((54, 54, 56), "telha"),
    "deepslate_tile_slab": ((54, 54, 56), "telha"),
    "cracked_deepslate_tiles": ((50, 50, 51), "telha"),
    "gravel": ((131, 127, 126), "pedra"),
    # madeira
    "spruce_planks": ((114, 84, 48), "tabuas"),
    "spruce_stairs": ((114, 84, 48), "tabuas"),
    "spruce_slab": ((114, 84, 48), "tabuas"),
    "spruce_fence": ((114, 84, 48), "tabuas"),
    "spruce_door": ((106, 80, 48), "porta"),
    "spruce_trapdoor": ((104, 78, 46), "tabuas"),
    "spruce_log": ((58, 38, 18), "casca"),
    "stripped_spruce_log": ((115, 89, 52), "tronco_liso"),
    "dark_oak_planks": ((66, 43, 20), "tabuas"),
    "dark_oak_stairs": ((66, 43, 20), "tabuas"),
    "dark_oak_slab": ((66, 43, 20), "tabuas"),
    # vidro, metal, luz
    "glass_pane": ((196, 220, 224), "vidro"),
    "iron_bars": ((128, 131, 128), "grade"),
    "chain": ((58, 63, 74), "metal_escuro"),
    "lantern": ((72, 70, 74), "lanterna"),
    "candle": ((222, 204, 166), "la"),
    "red_candle": ((150, 36, 32), "la"),
    "bell": ((226, 186, 58), "metal"),
    # móveis e utilidades
    "barrel": ((112, 82, 46), "barril"),
    "chest": ((158, 112, 44), "bau"),
    "furnace": ((108, 108, 108), "pedra"),
    "smoker": ((92, 84, 76), "pedra"),
    "crafting_table": ((122, 92, 56), "tabuas"),
    "cauldron": ((66, 66, 72), "metal_escuro"),
    "lectern": ((170, 130, 80), "tabuas"),
    "bookshelf": ((118, 92, 58), "estante"),
    "campfire": ((84, 64, 44), "tronco_liso"),
    "white_bed": ((226, 226, 226), "la"),
    "light_gray_bed": ((148, 148, 142), "la"),
    "ladder": ((126, 96, 56), "escada"),
    "flower_pot": ((124, 68, 53), "liso"),
    "brown_carpet": ((112, 72, 42), "la"),
    "hay_block": ((166, 138, 34), "feno"),
    "composter": ((112, 82, 46), "tabuas"),
    # natureza e desgaste
    "cobweb": ((224, 224, 228), "teia"),
    "vine": ((58, 92, 30), "folha"),
    "moss_carpet": ((88, 108, 44), "musgo"),
    "short_grass": ((92, 128, 52), "planta"),
    "fern": ((80, 116, 52), "planta"),
    "dead_bush": ((106, 78, 40), "planta"),
    "coarse_dirt": ((118, 84, 58), "terra"),
    "dirt_path": ((146, 120, 66), "terra"),
    # vila destruída: chão, ruína, fogo e água
    "grass_block": ((96, 132, 58), "musgo"),
    "dirt": ((126, 90, 62), "terra"),
    "farmland": ((92, 62, 40), "terra"),
    "wheat": ((178, 150, 62), "planta"),
    "soul_soil": ((76, 58, 46), "terra"),
    "water": ((46, 78, 150), "agua"),
    "cobblestone_stairs": ((122, 122, 122), "pedra"),
    "cobblestone_slab": ((122, 122, 122), "pedra"),
    "cobblestone_wall": ((122, 122, 122), "pedra"),
    "mossy_cobblestone_wall": ((104, 118, 88), "pedra_musgo"),
    "coal_block": ((22, 22, 24), "pedra"),
    "blackstone": ((44, 38, 44), "pedra"),
    "smooth_basalt": ((72, 72, 78), "liso"),
    "dark_oak_log": ((60, 46, 30), "casca"),
    "dark_oak_fence": ((66, 43, 20), "tabuas"),
    "dark_oak_trapdoor": ((62, 40, 18), "tabuas"),
    "gray_carpet": ((62, 68, 71), "la"),
    "light_gray_carpet": ((132, 132, 126), "la"),
    "red_wall_banner": ((150, 30, 28), "la"),
    # sangue (o acento vermelho): pó de redstone sem energia, deitado no chão
    "redstone_wire": ((112, 8, 6), "sangue"),
    # Vila Calada (100 x 100)
    "light": ((0, 0, 0), "nada"),  # bloco de luz invisível (só para não nascer monstro)
    "bone_block": ((222, 218, 198), "liso"),
    "stone_button": ((122, 122, 122), "pedra"),
    "spruce_sign": ((114, 84, 48), "tabuas"),
    "damaged_anvil": ((70, 70, 74), "metal_escuro"),
    "carved_pumpkin": ((196, 116, 24), "liso"),
    "white_carpet": ((230, 230, 230), "la"),
    "red_bed": ((150, 36, 32), "la"),
    "mud": ((58, 52, 56), "terra"),
    "red_stained_glass_pane": ((150, 40, 36), "vidro"),
    # Vila Calada 150 x 150 (arredores)
    "structure_block": ((0, 0, 0), "nada"),  # marcador DATA (o mod não coloca; só lê a metadata)
    "jukebox": ((96, 64, 40), "barril"),
    "spruce_fence_gate": ((114, 84, 48), "tabuas"),
    "spruce_wall_sign": ((114, 84, 48), "tabuas"),
    "spruce_leaves": ((46, 70, 44), "musgo"),
    "andesite": ((136, 136, 136), "pedra"),
    "podzol": ((88, 58, 30), "terra"),
    "rooted_dirt": ((124, 88, 62), "terra"),
    "moss_block": ((88, 108, 44), "musgo"),
    "pumpkin": ((190, 112, 30), "liso"),
    "brown_mushroom": ((150, 120, 90), "planta"),
    "wither_rose": ((36, 34, 36), "planta"),
    "white_wool": ((230, 230, 230), "la"),
    # Lugares do Ato 1 (Moinho, Açougueiro, Cartógrafo, Torre, Fazenda)
    "note_block": ((92, 60, 42), "barril"),
    "iron_block": ((196, 196, 198), "metal"),
    "iron_door": ((186, 186, 188), "porta"),
    "iron_trapdoor": ((170, 170, 172), "grade"),
    "smooth_stone": ((158, 158, 158), "liso"),
    "smooth_stone_slab": ((158, 158, 158), "liso"),
    "white_terracotta": ((176, 150, 136), "liso"),
    "terracotta": ((150, 92, 66), "liso"),
    "light_gray_terracotta": ((132, 104, 96), "liso"),
    "gray_terracotta": ((58, 42, 36), "liso"),
    "brown_terracotta": ((77, 51, 36), "liso"),
    "red_terracotta": ((132, 56, 42), "liso"),
    "red_wool": ((132, 28, 26), "la"),
    "red_carpet": ((132, 28, 26), "la"),
    "gray_wool": ((62, 68, 71), "la"),
    "light_gray_wool": ((136, 136, 130), "la"),
    "black_wool": ((24, 24, 28), "la"),
    "lever": ((110, 100, 90), "pedra"),
    "cartography_table": ((90, 66, 46), "tabuas"),
    "skeleton_skull": ((200, 198, 190), "liso"),
    "skeleton_wall_skull": ((200, 198, 190), "liso"),
    "grindstone": ((140, 140, 140), "liso"),
    "soul_campfire": ((70, 60, 50), "tronco_liso"),
    "stone_brick_wall": ((122, 121, 122), "tijolo_pedra"),
    "cobbled_deepslate": ((78, 78, 82), "pedra"),
    "cobbled_deepslate_wall": ((78, 78, 82), "pedra"),
    "deepslate_bricks": ((72, 72, 74), "tijolo_pedra"),
    "cracked_deepslate_bricks": ((66, 66, 68), "tijolo_pedra_rachado"),
    "deepslate_brick_stairs": ((72, 72, 74), "tijolo_pedra"),
    "deepslate_brick_slab": ((72, 72, 74), "tijolo_pedra"),
    "polished_deepslate": ((72, 72, 74), "liso"),
    "stripped_dark_oak_log": ((72, 54, 36), "tronco_liso"),
    "dark_oak_door": ((66, 43, 20), "porta"),
    "dark_oak_fence_gate": ((66, 43, 20), "tabuas"),
    "packed_mud": ((142, 106, 80), "terra"),
    "light_gray_wall_banner": ((150, 150, 146), "la"),
    "light_gray_banner": ((150, 150, 146), "la"),
    "red_banner": ((132, 28, 26), "la"),
    "tripwire_hook": ((120, 110, 100), "metal"),
    "chiseled_bookshelf": ((110, 84, 52), "estante"),
    "black_stained_glass_pane": ((30, 28, 34), "vidro"),
    "gray_bed": ((70, 74, 78), "la"),
    "brown_bed": ((100, 66, 40), "la"),
    "polished_blackstone_bricks": ((50, 44, 52), "tijolo_pedra"),
    "air": ((0, 0, 0), "nada"),
}

# Materiais só da prévia (não são blocos).
EXTRA_PREVIA = {
    "__chama": ((255, 196, 96), "luz"),
    "__luz_lanterna": ((255, 206, 120), "luz"),
    "__chama_alma": ((110, 210, 220), "luz"),
}

PERMITIDOS = {"minecraft:" + n for n in BLOCOS}

_DIR4 = {"north", "south", "east", "west"}
_BOOL = {"true", "false"}

# Propriedades aceitas por família. Bloco fora daqui não pode ter propriedade.
_FAMILIAS = [
    (lambda n: n.endswith("_fence_gate"), {"facing": _DIR4, "in_wall": _BOOL, "open": _BOOL, "powered": _BOOL}),
    (lambda n: n.endswith("_wall_sign"), {"facing": _DIR4, "waterlogged": _BOOL}),
    (lambda n: n.endswith("_leaves"), {"persistent": _BOOL, "distance": {str(i) for i in range(1, 8)},
                                       "waterlogged": _BOOL}),
    (lambda n: n == "structure_block", {"mode": {"save", "load", "corner", "data"}}),
    (lambda n: n == "jukebox", {"has_record": _BOOL}),
    (lambda n: n == "note_block", {"instrument": {"harp", "bass", "basedrum", "snare", "hat", "bell", "flute",
                                                  "chime", "guitar", "xylophone", "iron_xylophone", "cow_bell",
                                                  "didgeridoo", "bit", "banjo", "pling"},
                                   "note": {str(i) for i in range(25)}, "powered": _BOOL}),
    (lambda n: n == "lever", {"face": {"floor", "wall", "ceiling"}, "facing": _DIR4, "powered": _BOOL}),
    (lambda n: n == "grindstone", {"face": {"floor", "wall", "ceiling"}, "facing": _DIR4}),
    (lambda n: n == "skeleton_skull", {"rotation": {str(i) for i in range(16)}, "powered": _BOOL}),
    (lambda n: n == "skeleton_wall_skull", {"facing": _DIR4, "powered": _BOOL}),
    (lambda n: n == "light_gray_banner" or n == "red_banner", {"rotation": {str(i) for i in range(16)}}),
    (lambda n: n == "light_gray_wall_banner", {"facing": _DIR4}),
    (lambda n: n == "tripwire_hook", {"facing": _DIR4, "attached": _BOOL, "powered": _BOOL}),
    (lambda n: n == "chiseled_bookshelf", {"facing": _DIR4, **{f"slot_{i}_occupied": _BOOL for i in range(6)}}),
    (lambda n: n.endswith("_stairs"), {"facing": _DIR4, "half": {"top", "bottom"},
                                       "shape": {"straight", "inner_left", "inner_right", "outer_left", "outer_right"},
                                       "waterlogged": _BOOL}),
    (lambda n: n.endswith("_slab"), {"type": {"top", "bottom", "double"}, "waterlogged": _BOOL}),
    (lambda n: n.endswith("_trapdoor"), {"facing": _DIR4, "half": {"top", "bottom"}, "open": _BOOL,
                                         "powered": _BOOL, "waterlogged": _BOOL}),
    (lambda n: n.endswith("_door"), {"facing": _DIR4, "half": {"upper", "lower"}, "hinge": {"left", "right"},
                                     "open": _BOOL, "powered": _BOOL}),
    (lambda n: n.endswith("_fence") or n.endswith("glass_pane") or n == "iron_bars",
     {"north": _BOOL, "south": _BOOL, "east": _BOOL, "west": _BOOL, "waterlogged": _BOOL}),
    (lambda n: n.endswith("_bed"), {"facing": _DIR4, "part": {"head", "foot"}, "occupied": _BOOL}),
    (lambda n: n.endswith("_log") or n in ("hay_block", "bone_block"), {"axis": {"x", "y", "z"}}),
    (lambda n: n == "cartography_table", {}),
    (lambda n: n == "light", {"level": {str(i) for i in range(16)}, "waterlogged": _BOOL}),
    (lambda n: n == "stone_button", {"face": {"floor", "wall", "ceiling"}, "facing": _DIR4, "powered": _BOOL}),
    (lambda n: n == "spruce_sign", {"rotation": {str(i) for i in range(16)}, "waterlogged": _BOOL}),
    (lambda n: n in ("damaged_anvil", "carved_pumpkin"), {"facing": _DIR4}),
    (lambda n: n == "chain", {"axis": {"x", "y", "z"}, "waterlogged": _BOOL}),
    (lambda n: n == "lantern", {"hanging": _BOOL, "waterlogged": _BOOL}),
    (lambda n: n.endswith("candle"), {"candles": {"1", "2", "3", "4"}, "lit": _BOOL, "waterlogged": _BOOL}),
    (lambda n: n == "bell", {"attachment": {"floor", "ceiling", "single_wall", "double_wall"},
                             "facing": _DIR4, "powered": _BOOL}),
    (lambda n: n == "ladder", {"facing": _DIR4, "waterlogged": _BOOL}),
    (lambda n: n == "vine", {"north": _BOOL, "south": _BOOL, "east": _BOOL, "west": _BOOL, "up": _BOOL}),
    (lambda n: n in ("campfire", "soul_campfire"), {"facing": _DIR4, "lit": _BOOL, "signal_fire": _BOOL, "waterlogged": _BOOL}),
    (lambda n: n in ("furnace", "smoker"), {"facing": _DIR4, "lit": _BOOL}),
    (lambda n: n == "chest", {"facing": _DIR4, "type": {"single", "left", "right"}, "waterlogged": _BOOL}),
    (lambda n: n == "barrel", {"facing": _DIR4 | {"up", "down"}, "open": _BOOL}),
    (lambda n: n == "lectern", {"facing": _DIR4, "has_book": _BOOL, "powered": _BOOL}),
    (lambda n: n == "redstone_wire", {"power": {str(i) for i in range(16)},
                                      "north": {"none", "side", "up"}, "south": {"none", "side", "up"},
                                      "east": {"none", "side", "up"}, "west": {"none", "side", "up"}}),
    (lambda n: n == "composter", {"level": {str(i) for i in range(9)}}),
    (lambda n: n.endswith("_wall") and n != "red_wall_banner",
     {"up": _BOOL, "north": {"none", "low", "tall"}, "south": {"none", "low", "tall"},
      "east": {"none", "low", "tall"}, "west": {"none", "low", "tall"}, "waterlogged": _BOOL}),
    (lambda n: n == "red_wall_banner", {"facing": _DIR4}),
    (lambda n: n == "grass_block", {"snowy": _BOOL}),
    (lambda n: n == "farmland", {"moisture": {str(i) for i in range(8)}}),
    (lambda n: n == "wheat", {"age": {str(i) for i in range(8)}}),
    (lambda n: n == "water", {"level": {str(i) for i in range(16)}}),
]


def props_validas(nome_curto):
    for teste, props in _FAMILIAS:
        if teste(nome_curto):
            return props
    return {}


def curto(nome):
    return nome.split(":", 1)[1] if ":" in nome else nome


# ---------------------------------------------------------------------------
# Formato de cada bloco (caixas em coordenadas 0..1 dentro do bloco).
# Usado pela prévia e para saber se um bloco é "cheio" (para sangue, musgo, sombra).
# ---------------------------------------------------------------------------
CHEIA = (0.0, 0.0, 0.0, 1.0, 1.0, 1.0)


def _verdade(p, k):
    return p.get(k) == "true"


def formas(nome, p):
    """Lista de (caixa, material) do bloco. p é um dict de propriedades (texto)."""
    n = curto(nome)
    if n in ("air", "light", "structure_block"):
        return []
    if n.endswith("_wall_sign"):
        return [({"north": (0, .25, .875, 1, .75, 1), "south": (0, .25, 0, 1, .75, .125),
                  "east": (0, .25, 0, .125, .75, 1), "west": (.875, .25, 0, 1, .75, 1)}[p.get("facing", "north")], n)]
    if n.endswith("_fence_gate"):
        if _verdade(p, "open"):
            return [((.375, 0, .375, .625, 1, .625), n)]
        if p.get("facing") in ("east", "west"):
            return [((.375, .375, 0, .625, 1, 1), n), ((.375, 0, 0, .625, 1, .125), n), ((.375, 0, .875, .625, 1, 1), n)]
        return [((0, .375, .375, 1, 1, .625), n), ((0, 0, .375, .125, 1, .625), n), ((.875, 0, .375, 1, 1, .625), n)]
    if n == "brown_mushroom":
        return [((.3125, 0, .3125, .6875, .375, .6875), n)]
    if n == "stone_button":
        return [((.3125, 0, .375, .6875, .125, .625), n)]
    if n == "spruce_sign":
        return [((.4375, 0, .4375, .5625, .5625, .5625), n), ((0, .5, .4375, 1, 1, .5625), n)]
    if n == "damaged_anvil":
        return [((.125, 0, .125, .875, .25, .875), n), ((.3125, .25, .25, .6875, .625, .75), n),
                ((0, .625, .1875, 1, 1, .8125), n)]
    if n.endswith("_slab"):
        t = p.get("type", "bottom")
        if t == "bottom":
            return [((0, 0, 0, 1, .5, 1), n)]
        if t == "top":
            return [((0, .5, 0, 1, 1, 1), n)]
        return [(CHEIA, n)]
    if n.endswith("_stairs"):
        topo = p.get("half", "bottom") == "top"
        base = (0, .5, 0, 1, 1, 1) if topo else (0, 0, 0, 1, .5, 1)
        y0, y1 = (0, .5) if topo else (.5, 1)
        degrau = {"north": (0, y0, 0, 1, y1, .5), "south": (0, y0, .5, 1, y1, 1),
                  "east": (.5, y0, 0, 1, y1, 1), "west": (0, y0, 0, .5, y1, 1)}[p.get("facing", "north")]
        return [(base, n), (degrau, n)]
    if n.endswith("_carpet"):
        return [((0, 0, 0, 1, 1 / 16, 1), n)]
    if n in ("dirt_path", "farmland"):
        # 15/16 de altura: o topo não é "cheio" (pó de redstone não fica em cima no jogo)
        return [((0, 0, 0, 1, .9375, 1), n)]
    if n == "wheat":
        return [((.125, 0, .125, .875, .25 + .09 * int(p.get("age", "7")), .875), n)]
    if n == "lever":
        return [((.3125, 0, .25, .6875, .1875, .75), "cobblestone"), ((.4375, .1875, .4375, .5625, .625, .5625), "spruce_planks")]
    if n == "tripwire_hook":
        return [({"north": (.375, .3, .875, .625, .7, 1), "south": (.375, .3, 0, .625, .7, .125),
                  "east": (0, .3, .375, .125, .7, .625), "west": (.875, .3, .375, 1, .7, .625)}[p.get("facing", "north")], n)]
    if n == "skeleton_skull":
        return [((.25, 0, .25, .75, .5, .75), n)]
    if n == "skeleton_wall_skull":
        return [({"north": (.25, .25, .5, .75, .75, 1), "south": (.25, .25, 0, .75, .75, .5),
                  "east": (0, .25, .25, .5, .75, .75), "west": (.5, .25, .25, 1, .75, .75)}[p.get("facing", "north")], n)]
    if n == "grindstone":
        return [((.125, .25, .25, .875, .9, .75), n), ((.2, 0, .4, .3, .3, .6), "dark_oak_planks"),
                ((.7, 0, .4, .8, .3, .6), "dark_oak_planks")]
    if n in ("light_gray_banner", "red_banner"):
        return [((.4375, 0, .4375, .5625, 1, .5625), "dark_oak_planks"), ((.0625, .9, .4375, .9375, 1, .5625), "dark_oak_planks"),
                ((.0625, .05, .45, .9375, .9, .55), n)]
    if n == "light_gray_wall_banner":
        return [({"north": (.0625, .0, .875, .9375, .875, 1), "south": (.0625, .0, 0, .9375, .875, .125),
                  "east": (0, .0, .0625, .125, .875, .9375), "west": (.875, .0, .0625, 1, .875, .9375)}
                 [p.get("facing", "south")], n)]
    if n == "red_wall_banner":
        return [({"north": (.0625, .0, .875, .9375, .875, 1), "south": (.0625, .0, 0, .9375, .875, .125),
                  "east": (0, .0, .0625, .125, .875, .9375), "west": (.875, .0, .0625, 1, .875, .9375)}
                 [p.get("facing", "south")], n)]
    if n.endswith("_wall"):
        caixas = [((.25, 0, .25, .75, 1, .75), n)] if _verdade(p, "up") else []
        braco = {"north": (.3125, 0, 0, .6875, .875, .5), "south": (.3125, 0, .5, .6875, .875, 1),
                 "east": (.5, 0, .3125, 1, .875, .6875), "west": (0, 0, .3125, .5, .875, .6875)}
        for d, cx in braco.items():
            if p.get(d, "none") != "none":
                caixas.append((cx, n))
        return caixas or [((.25, 0, .25, .75, 1, .75), n)]
    if n == "redstone_wire":
        return [((.1, 0, .1, .9, .04, .9), n)]
    if n.endswith("glass_pane") or n == "iron_bars":
        a, b = .4375, .5625
        caixas = []
        ew = _verdade(p, "east") or _verdade(p, "west")
        ns = _verdade(p, "north") or _verdade(p, "south")
        if ew:
            caixas.append(((0 if _verdade(p, "west") else a, 0, a, 1 if _verdade(p, "east") else b, 1, b), n))
        if ns:
            caixas.append(((a, 0, 0 if _verdade(p, "north") else a, b, 1, 1 if _verdade(p, "south") else b), n))
        return caixas or [((a, 0, a, b, 1, b), n)]
    if n.endswith("_fence"):
        caixas = [((.375, 0, .375, .625, 1, .625), n)]
        braco = {"north": (.4375, .375, 0, .5625, .9375, .375), "south": (.4375, .375, .625, .5625, .9375, 1),
                 "east": (.625, .375, .4375, 1, .9375, .5625), "west": (0, .375, .4375, .375, .9375, .5625)}
        for d, cx in braco.items():
            if _verdade(p, d):
                caixas.append((cx, n))
        return caixas
    if n.endswith("_trapdoor"):
        if _verdade(p, "open"):
            # aberto: encostado no lado oposto ao facing
            return [({"north": (0, 0, .8125, 1, 1, 1), "south": (0, 0, 0, 1, 1, .1875),
                      "east": (0, 0, 0, .1875, 1, 1), "west": (.8125, 0, 0, 1, 1, 1)}[p.get("facing", "north")], n)]
        if p.get("half") == "top":
            return [((0, .8125, 0, 1, 1, 1), n)]
        return [((0, 0, 0, 1, .1875, 1), n)]
    if n.endswith("_door"):
        f = p.get("facing", "north")
        if _verdade(p, "open"):
            # porta aberta fica de lado (aproximação)
            f = {"north": "east", "east": "south", "south": "west", "west": "north"}[f]
        return [({"north": (0, 0, .8125, 1, 1, 1), "south": (0, 0, 0, 1, 1, .1875),
                  "east": (0, 0, 0, .1875, 1, 1), "west": (.8125, 0, 0, 1, 1, 1)}[f], n)]
    if n.endswith("_bed"):
        return [((0, .1875, 0, 1, .5625, 1), n), ((.0, 0, .0, .1875, .1875, .1875), "dark_oak_planks"),
                ((.8125, 0, .8125, 1, .1875, 1), "dark_oak_planks")]
    if n == "lantern":
        y = .0625 if _verdade(p, "hanging") else 0
        return [((.3125, y, .3125, .6875, y + .4375, .6875), "lantern"),
                ((.375, y + .0625, .375, .625, y + .375, .625), "__luz_lanterna")]
    if n.endswith("candle"):
        c = int(p.get("candles", "1"))
        pos = [(.4375, .4375), (.25, .5), (.5625, .25), (.625, .625)][:c]
        caixas = []
        for ox, oz in pos:
            caixas.append(((ox, 0, oz, ox + .125, .375, oz + .125), n))
            if _verdade(p, "lit"):
                caixas.append(((ox + .03, .375, oz + .03, ox + .095, .5, oz + .095), "__chama"))
        return caixas
    if n == "chain":
        return [((.4375, 0, .4375, .5625, 1, .5625), n)]
    if n == "bell":
        return [((.25, .25, .25, .75, .8125, .75), n), ((.4375, .8125, .4375, .5625, 1, .5625), "chain")]
    if n == "lectern":
        return [((0, 0, 0, 1, .125, 1), n), ((.25, .125, .25, .75, .8125, .75), n), ((0, .8125, 0, 1, .9375, 1), n)]
    if n in ("campfire", "soul_campfire"):
        caixas = [((0, 0, 0, 1, .4375, 1), n)]
        if _verdade(p, "lit"):
            caixas.append(((.3, .4375, .3, .7, .75, .7), "__chama" if n == "campfire" else "__chama_alma"))
        return caixas
    if n == "flower_pot":
        return [((.3125, 0, .3125, .6875, .375, .6875), n)]
    if n == "chest":
        return [((.0625, 0, .0625, .9375, .875, .9375), n)]
    if n == "ladder":
        return [({"north": (0, 0, .875, 1, 1, 1), "south": (0, 0, 0, 1, 1, .125),
                  "east": (0, 0, 0, .125, 1, 1), "west": (.875, 0, 0, 1, 1, 1)}[p.get("facing", "north")], n)]
    if n == "vine":
        lados = {"north": (0, 0, 0, 1, 1, .0625), "south": (0, 0, .9375, 1, 1, 1),
                 "east": (.9375, 0, 0, 1, 1, 1), "west": (0, 0, 0, .0625, 1, 1)}
        caixas = [(cx, n) for d, cx in lados.items() if _verdade(p, d)]
        return caixas or [((0, .9375, 0, 1, 1, 1), n)]
    if n in ("short_grass", "fern", "dead_bush", "wither_rose"):
        return [((.125, 0, .125, .875, .8125, .875), n)]
    return [(CHEIA, n)]


TRANSPARENTES = {"vidro", "teia", "planta", "folha", "grade", "escada", "nada", "agua"}


def material(nome):
    n = curto(nome)
    return BLOCOS.get(n) or EXTRA_PREVIA[n]


def cheio(nome, p):
    """Bloco cúbico e opaco (esconde o vizinho e segura pó/tapete em cima)."""
    if nome is None:
        return False
    f = formas(nome, p)
    return len(f) == 1 and f[0][0] == CHEIA and material(f[0][1])[1] not in TRANSPARENTES
