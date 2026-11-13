"""Infraestrutura para montar criaturas GeckoLib em código (sem Blockbench): cubos e ossos, empacotador de UV
em caixa, folha de textura pintada pixel a pixel, escrita do .geo.json, do .animation.json e do .bbmodel.
Só Python 3 + numpy. Arte nossa; nada copiado de terceiros."""
import base64
import json
import math
import uuid

import numpy as np

# ------------------------------------------------------------------ cores e ruído determinístico


def cor(h):
    return (int(h[1:3], 16), int(h[3:5], 16), int(h[5:7], 16), 255)


# Deslocamento global de semente: 0 = variante A da textura; outro valor = variante B (mesmo modelo, outros retalhos).
DESLOCAMENTO_SEMENTE = 0


def hash01(x, y, s=0):
    """Número fixo em [0,1) para (x, y, semente). Não é aleatório: o mesmo pixel dá sempre o mesmo tom."""
    s = s + DESLOCAMENTO_SEMENTE
    h = (x * 374761393 + y * 668265263 + s * 982451653) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    h = (h ^ (h >> 16)) & 0x7FFFFFFF
    return h / 0x80000000


def clamp(i, a, b):
    return max(a, min(b, i))


def tom(paleta, i):
    return cor(paleta[clamp(i, 0, len(paleta) - 1)])


def sombreado(paleta, face, fx, fy, fw, fh, semente=0, borda=True, grad=True, base=1, pintas=(0.07, 0.96),
              manchas=0.22, oclusao=True):
    """Tom base com luz de cima (topo mais claro), borda direita/baixo mais escura, manchas largas (3x3 px) e
    poucas pintas soltas. Oclusão: as 2 linhas de baixo das faces altas escurecem (a peça "assenta" no que está
    embaixo). Garante pelo menos 3 tons por material (régua de qualidade), sem virar chuvisco."""
    i = base
    if grad and fh >= 4:
        i += 0 if fy < fh * 0.3 else (0 if fy < fh * 0.65 else 1)
        if fy < max(1, fh * 0.15):
            i -= 1
    if oclusao and fh >= 7 and fy >= fh - 2 and face not in ("up", "down"):
        i += 1
    if face == "up":
        i -= 1
    elif face == "down":
        i += 1
    if manchas and hash01(fx // 3, fy // 3, semente * 31 + FACE_INDICE[face]) < manchas:
        i += 1
    r = hash01(fx, fy, semente * 7 + FACE_INDICE[face])
    if r < pintas[0]:
        i += 1
    elif r > pintas[1]:
        i -= 1
    if borda and fw >= 3 and fh >= 3:
        if fx == fw - 1 or fy == fh - 1:
            i += 1
    return tom(paleta, i)


CORTE = "#1f1012"
SANGUE_SECO = "#3A0306"


def patchwork(familias, semente, maximo=3, seco=0.14, fio="#15100f"):
    """Pele em retalhos IRREGULARES: cada face é dividida em 2 ou 3 regiões (vizinho mais perto de sementes fixas);
    cada região vem de outra "pessoa" (outra família de tons); a costura só existe na fronteira entre regiões
    (corte escuro + fio cruzando a cada 3 px) e o sangue seco fica junto da costura. Nada de grade de cruzes."""
    cache = {}

    def sementes(face, fw, fh):
        chave = (face, fw, fh)
        if chave not in cache:
            k = max(2, min(maximo, (fw * fh) // 36 + 1))
            base = semente * 13 + FACE_INDICE[face]
            cache[chave] = [(hash01(i, 1, base) * (fw - 1), hash01(i, 2, base) * (fh - 1), hash01(i, 3, base) * 2.0)
                            for i in range(k)]
        return cache[chave]

    def regiao(face, fx, fy, fw, fh):
        melhor, qual = 1e9, 0
        for i, (sx, sy, peso) in enumerate(sementes(face, fw, fh)):
            d = abs(fx - sx) + abs(fy - sy) * 1.2 + peso
            if d < melhor:
                melhor, qual = d, i
        return qual

    def eh_costura(face, fx, fy, fw, fh):
        r = regiao(face, fx, fy, fw, fh)
        if fx + 1 < fw and regiao(face, fx + 1, fy, fw, fh) != r:
            return True
        return fy + 1 < fh and regiao(face, fx, fy + 1, fw, fh) != r

    def p(face, fx, fy, fw, fh):
        if fw < 4 or fh < 4:
            return sombreado(familias[semente % len(familias)], face, fx, fy, fw, fh, semente)
        r = regiao(face, fx, fy, fw, fh)
        fam = familias[(r + semente) % len(familias)]
        if eh_costura(face, fx, fy, fw, fh):
            return cor(CORTE) if (fx + fy) % 3 != 1 else cor(fio)
        # fio cruzando a costura: só de um lado (direita/baixo), a cada 3 px; sangue seco raro, grudado nela
        perto = any(0 <= fx + dx < fw and 0 <= fy + dy < fh and eh_costura(face, fx + dx, fy + dy, fw, fh)
                    for dx, dy in ((-1, 0), (0, -1)))
        if perto:
            if (fx + fy) % 3 == 1:
                return cor(fio)
            if hash01(fx, fy, semente * 5 + 2) < seco:
                return cor(SANGUE_SECO)
        return sombreado(fam, face, fx, fy, fw, fh, semente + r * 3, base=1)
    return p


FACE_INDICE = {"north": 0, "south": 1, "east": 2, "west": 3, "up": 4, "down": 5}
FACES = ["north", "south", "east", "west", "up", "down"]


def costura_h(fx, fy, linha, fw, fio="#15100f", corte="#3A0306", passo=3):
    """Costura grossa horizontal na linha `linha`: corte escuro no meio, fio cruzando a cada `passo` pixels."""
    if fy == linha:
        return cor(corte) if fx % passo != 1 else cor(fio)
    if fy in (linha - 1, linha + 1) and fx % passo == 1:
        return cor(fio)
    return None


def costura_v(fx, fy, coluna, fh, fio="#15100f", corte="#3A0306", passo=3):
    if fx == coluna:
        return cor(corte) if fy % passo != 1 else cor(fio)
    if fx in (coluna - 1, coluna + 1) and fy % passo == 1:
        return cor(fio)
    return None


# ------------------------------------------------------------------ cubos e ossos


class Cubo:
    def __init__(self, nome, origem, tamanho, pintor, inflar=0.0, espelho=False, partilha=None,
                 rotacao=None, pivo=None, brilho=None):
        self.nome = nome
        self.origem = [float(v) for v in origem]
        self.tamanho = [float(v) for v in tamanho]
        self.pintor = pintor          # f(face, fx, fy, fw, fh) -> RGBA
        self.brilho = brilho          # f(face, fx, fy, fw, fh) -> RGBA ou None (glowmask)
        self.inflar = float(inflar)
        self.espelho = espelho
        self.partilha = partilha      # nome de outro cubo com a mesma UV (mesmo tamanho)
        self.rotacao = rotacao
        self.pivo = pivo
        self.uv = None

    @property
    def caixa_uv(self):
        w, h, d = (int(math.ceil(v - 1e-6)) for v in self.tamanho)
        return (2 * w + 2 * d, h + d)

    def retangulos(self):
        u, v = self.uv
        w, h, d = (int(math.ceil(x - 1e-6)) for x in self.tamanho)
        return {
            "east": (u, v + d, d, h),
            "north": (u + d, v + d, w, h),
            "west": (u + d + w, v + d, d, h),
            "south": (u + 2 * d + w, v + d, w, h),
            "up": (u + d, v, w, d),
            "down": (u + d + w, v, w, d),
        }


class Osso:
    def __init__(self, nome, pai=None, pivo=(0, 0, 0), rotacao=None):
        self.nome = nome
        self.pai = pai
        self.pivo = [float(v) for v in pivo]
        self.rotacao = rotacao
        self.cubos = []

    def cubo(self, *a, **k):
        c = Cubo(*a, **k)
        self.cubos.append(c)
        return c


class Criatura:
    def __init__(self, nome, largura_tex=128, altura_tex=128):
        self.nome = nome
        self.ossos = []
        self.por_nome = {}
        self.tex_w = largura_tex
        self.tex_h = altura_tex

    def osso(self, nome, pai=None, pivo=(0, 0, 0), rotacao=None):
        o = Osso(nome, pai, pivo, rotacao)
        self.ossos.append(o)
        self.por_nome[nome] = o
        return o

    def cubos(self):
        return [c for o in self.ossos for c in o.cubos]

    # --- UV em caixa: empacotador de prateleiras
    def empacotar_uv(self, margem=1):
        todos = {c.nome: c for c in self.cubos()}
        unicos = [c for c in self.cubos() if c.partilha is None]
        unicos.sort(key=lambda c: (-c.caixa_uv[1], -c.caixa_uv[0]))
        x = y = 0
        alt_linha = 0
        for c in unicos:
            w, h = c.caixa_uv
            if x + w > self.tex_w:
                x = 0
                y += alt_linha + margem
                alt_linha = 0
            if y + h > self.tex_h:
                raise ValueError(f"textura {self.tex_w}x{self.tex_h} pequena demais para {self.nome} (em {c.nome})")
            c.uv = (x, y)
            x += w + margem
            alt_linha = max(alt_linha, h)
        for c in self.cubos():
            if c.partilha is not None:
                alvo = todos[c.partilha]
                assert alvo.caixa_uv == c.caixa_uv, f"{c.nome} partilha UV com {c.partilha} mas o tamanho difere"
                c.uv = alvo.uv
        return y + alt_linha

    # --- textura
    def pintar(self):
        tex = np.zeros((self.tex_h, self.tex_w, 4), np.uint8)
        glow = np.zeros((self.tex_h, self.tex_w, 4), np.uint8)
        tem_glow = False
        for c in self.cubos():
            if c.partilha is not None:
                continue
            for face, (u, v, fw, fh) in c.retangulos().items():
                for fy in range(fh):
                    for fx in range(fw):
                        px = c.pintor(face, fx, fy, fw, fh)
                        if px is not None:
                            tex[v + fy, u + fx] = px
                        if c.brilho is not None:
                            g = c.brilho(face, fx, fy, fw, fh)
                            if g is not None:
                                glow[v + fy, u + fx] = g
                                tem_glow = True
        return tex, (glow if tem_glow else None)

    # --- .geo.json (Bedrock 1.12, lido pelo GeckoLib)
    def geo(self, caixa_visivel=(3, 3, 1.5)):
        ossos = []
        for o in self.ossos:
            b = {"name": o.nome, "pivot": o.pivo}
            if o.pai:
                b["parent"] = o.pai
            if o.rotacao:
                b["rotation"] = list(o.rotacao)
            if o.cubos:
                cubos = []
                for c in o.cubos:
                    d = {"origin": c.origem, "size": c.tamanho, "uv": list(c.uv)}
                    if c.inflar:
                        d["inflate"] = c.inflar
                    if c.espelho:
                        d["mirror"] = True
                    if c.rotacao:
                        d["rotation"] = list(c.rotacao)
                        d["pivot"] = list(c.pivo) if c.pivo else [c.origem[i] + c.tamanho[i] / 2 for i in range(3)]
                    cubos.append(d)
                b["cubes"] = cubos
            ossos.append(b)
        return {
            "format_version": "1.12.0",
            "minecraft:geometry": [{
                "description": {
                    "identifier": f"geometry.{self.nome}",
                    "texture_width": self.tex_w,
                    "texture_height": self.tex_h,
                    "visible_bounds_width": caixa_visivel[0],
                    "visible_bounds_height": caixa_visivel[1],
                    "visible_bounds_offset": [0, caixa_visivel[2], 0],
                },
                "bones": ossos,
            }],
        }


# ------------------------------------------------------------------ animação (GeckoLib / Bedrock 1.8)


def canal(*chaves):
    """canal((t, (x,y,z)), (t, (x,y,z), 'easeOutCubic'), ...). A easing vale para o trecho que chega no quadro."""
    d = {}
    for ch in chaves:
        t, v = ch[0], ch[1]
        chave = f"{float(t):g}"
        if "." not in chave:
            chave += ".0"
        valor = [round(float(x), 4) for x in v]
        d[chave] = valor if len(ch) == 2 else {"vector": valor, "easing": ch[2]}
    return d


def animacao(duracao, loop, ossos):
    """ossos: {osso: {"rotation": canal(...), "position": ..., "scale": ...}}"""
    return {"loop": bool(loop), "animation_length": float(duracao), "bones": ossos}


def arquivo_animacoes(criatura, anims):
    return {"format_version": "1.8.0",
            "animations": {f"animation.{criatura}.{nome}": a for nome, a in anims.items()}}


# ------------------------------------------------------------------ .bbmodel (Blockbench 4.x, formato GeckoLib)


def _uuid(*partes):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "odteste/" + "/".join(str(p) for p in partes)))


def _faces_bb(c):
    r = c.retangulos()
    if c.espelho:
        r["east"], r["west"] = r["west"], r["east"]
    faces = {}
    for f, (u, v, w, h) in r.items():
        uv = [u, v, u + w, v + h]
        if f == "down":
            uv = [u, v + h, u + w, v]
        if c.espelho:
            uv = [uv[2], uv[1], uv[0], uv[3]]
        faces[f] = {"uv": uv, "texture": 0}
    return faces


def _kf(canal_dict, nome_canal, osso):
    saida = []
    for t, val in canal_dict.items():
        if isinstance(val, dict):
            vec, easing = val["vector"], val.get("easing", "linear")
        else:
            vec, easing = val, "linear"
        kf = {"channel": nome_canal, "data_points": [{"x": f"{vec[0]:g}", "y": f"{vec[1]:g}", "z": f"{vec[2]:g}"}],
              "uuid": _uuid("kf", osso, nome_canal, t), "time": float(t), "color": -1,
              "interpolation": "linear", "bezier_linked": True,
              "bezier_left_time": [-0.1, -0.1, -0.1], "bezier_left_value": [0, 0, 0],
              "bezier_right_time": [0.1, 0.1, 0.1], "bezier_right_value": [0, 0, 0]}
        if easing and easing != "linear":
            kf["easing"] = easing
        saida.append(kf)
    return saida


def bbmodel(criatura, anims, png_textura, nome_textura, caixa_visivel=(3, 3)):
    elementos = []
    por_osso = {}
    for o in criatura.ossos:
        ids = []
        for c in o.cubos:
            uid = _uuid("cubo", criatura.nome, o.nome, c.nome)
            ids.append(uid)
            e = {"name": c.nome, "box_uv": True, "rescale": False, "locked": False,
                 "from": c.origem, "to": [c.origem[i] + c.tamanho[i] for i in range(3)],
                 "autouv": 0, "color": FACE_INDICE.get("north", 0),
                 "origin": list(c.pivo) if c.pivo else [c.origem[i] + c.tamanho[i] / 2 for i in range(3)],
                 "uv_offset": list(c.uv), "faces": _faces_bb(c), "type": "cube", "uuid": uid}
            if c.rotacao:
                e["rotation"] = list(c.rotacao)
            if c.inflar:
                e["inflate"] = c.inflar
            if c.espelho:
                e["mirror_uv"] = True
            elementos.append(e)
        por_osso[o.nome] = ids

    def grupo(o):
        filhos = list(por_osso[o.nome])
        for f in criatura.ossos:
            if f.pai == o.nome:
                filhos.append(grupo(f))
        return {"name": o.nome, "origin": o.pivo, "rotation": list(o.rotacao or [0, 0, 0]), "color": 0,
                "uuid": _uuid("osso", criatura.nome, o.nome), "export": True, "mirror_uv": False, "isOpen": True,
                "locked": False, "visibility": True, "autouv": 0, "children": filhos}

    raizes = [grupo(o) for o in criatura.ossos if not o.pai]
    animacoes = []
    for nome, a in anims.items():
        animadores = {}
        for osso, canais in a["bones"].items():
            kfs = []
            for nome_canal in ("rotation", "position", "scale"):
                if nome_canal in canais:
                    kfs += _kf(canais[nome_canal], nome_canal, osso)
            animadores[_uuid("osso", criatura.nome, osso)] = {"name": osso, "type": "bone", "keyframes": kfs}
        animacoes.append({"uuid": _uuid("anim", criatura.nome, nome), "name": f"animation.{criatura.nome}.{nome}",
                          "loop": "loop" if a["loop"] else "once", "override": False, "length": a["animation_length"],
                          "snapping": 24, "selected": False, "saved": True, "path": "", "anim_time_update": "",
                          "blend_weight": "", "start_delay": "", "loop_delay": "", "animators": animadores})
    return {
        "meta": {"format_version": "4.10", "model_format": "animated_entity_model", "box_uv": True},
        "name": criatura.nome,
        "model_identifier": criatura.nome,
        "visible_box": [caixa_visivel[0], caixa_visivel[1], 0],
        "variable_placeholders": "",
        "variable_placeholder_buttons": [],
        "timeline_setups": [],
        "unhandled_root_fields": {},
        "resolution": {"width": criatura.tex_w, "height": criatura.tex_h},
        "elements": elementos,
        "outliner": raizes,
        "textures": [{
            "path": "", "name": nome_textura, "folder": "entity", "namespace": "odteste", "id": "0",
            "particle": False, "render_mode": "default", "render_sides": "auto", "frame_time": 1,
            "frame_order_type": "loop", "frame_order": "", "frame_interpolate": False, "visible": True,
            "mode": "bitmap", "saved": True, "uuid": _uuid("tex", criatura.nome),
            "relative_path": f"../previas/criaturas/{criatura.nome}/{nome_textura}",
            "source": "data:image/png;base64," + base64.b64encode(png_textura).decode("ascii"),
            "width": criatura.tex_w, "height": criatura.tex_h, "uv_width": criatura.tex_w, "uv_height": criatura.tex_h,
        }],
        "animations": animacoes,
    }


def json_compacto(obj):
    return json.dumps(obj, ensure_ascii=False, indent=1)
