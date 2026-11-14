"""Grade de blocos de uma estrutura e exportação para .nbt (formato de estrutura vanilla).

Coordenadas: x = leste, y = cima, z = sul. A origem (0,0,0) é o canto noroeste de baixo.
Posição que nunca recebeu bloco fica FORA do arquivo: ao colar, o que já está no mundo
ali continua. Para limpar terreno (por exemplo dentro da casa), use ar() / ar_onde_vazio().
"""
import random

import blocos as B
import nbt_io as N

DATA_VERSION_1_21_1 = 3955


def nome_completo(n):
    return n if ":" in n else "minecraft:" + n


def _texto(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    return str(v)


def hash01(*valores):
    """Número pseudoaleatório estável em [0, 1) a partir de inteiros."""
    x = 2166136261
    for v in valores:
        x = ((x ^ (int(v) & 0xFFFFFFFF)) * 16777619) & 0xFFFFFFFF
    x ^= x >> 13
    x = (x * 1274126177) & 0xFFFFFFFF
    x ^= x >> 16
    return x / 4294967296.0


def ruido(x, y, z, escala=3.0, semente=0):
    """Ruído suave (value noise trilinear) em [0, 1). Faz manchas em vez de pontinhos soltos."""
    fx, fy, fz = x / escala, y / escala, z / escala
    ix, iy, iz = int(fx // 1), int(fy // 1), int(fz // 1)
    tx, ty, tz = fx - ix, fy - iy, fz - iz
    tx, ty, tz = (t * t * (3 - 2 * t) for t in (tx, ty, tz))

    def c(a, b, d):
        return hash01(semente, ix + a, iy + b, iz + d)

    def lerp(a, b, t):
        return a + (b - a) * t

    x00 = lerp(c(0, 0, 0), c(1, 0, 0), tx)
    x10 = lerp(c(0, 1, 0), c(1, 1, 0), tx)
    x01 = lerp(c(0, 0, 1), c(1, 0, 1), tx)
    x11 = lerp(c(0, 1, 1), c(1, 1, 1), tx)
    return lerp(lerp(x00, x10, ty), lerp(x01, x11, ty), tz)


class Mistura:
    """Escolha sorteada com peso: Mistura(("calcite", 8), ("diorite", 1), (("spruce_log", {"axis": "y"}), 1))."""

    def __init__(self, *opcoes):
        if not opcoes:
            raise ValueError("Mistura vazia")
        self.opcoes = opcoes
        self.total = float(sum(p for _, p in opcoes))

    def escolher(self, rng):
        r = rng.random() * self.total
        for bloco, peso in self.opcoes:
            r -= peso
            if r < 0:
                return bloco
        return self.opcoes[-1][0]


class Estrutura:
    def __init__(self, nome, tamanho, semente=1):
        # "titulo" e não "nome": um atributo self.nome esconderia o método nome(x, y, z)
        self.titulo = nome
        self.tam = tuple(tamanho)
        self.blocos = {}  # (x,y,z) -> (nome completo, tupla ordenada de (prop, valor))
        self.semente = semente
        self.rng = random.Random(semente)
        self.pistas = []  # lugares para a Lore/Dev colocarem livro, recado, evento
        self.dados = {}  # (x,y,z) -> NBT do bloco (block entity), gravado no campo "nbt" da estrutura
        self.marcadores = []  # blocos de estrutura em modo DATA: onde o mod põe recompensa
        self.travado = None  # caixa (x0, z0, x1, z1) que não pode mais mudar (ex.: o centro pronto da vila)
        self.y_chao = 0  # camada do piso térreo (abaixo dela: porão/cripta). A prévia põe o chão do mundo aqui

    # ---- leitura ---------------------------------------------------------
    def dentro(self, x, y, z):
        sx, sy, sz = self.tam
        return 0 <= x < sx and 0 <= y < sy and 0 <= z < sz

    def nome(self, x, y, z):
        b = self.blocos.get((x, y, z))
        return b[0] if b else None

    def props(self, x, y, z):
        b = self.blocos.get((x, y, z))
        return dict(b[1]) if b else {}

    def vazio(self, x, y, z):
        n = self.nome(x, y, z)
        return n is None or n == "minecraft:air"

    def cheio(self, x, y, z):
        n = self.nome(x, y, z)
        return n is not None and B.cheio(n, self.props(x, y, z))

    def chance(self, p):
        return self.rng.random() < p

    # ---- escrita ---------------------------------------------------------
    def _resolver(self, bloco, props):
        if isinstance(bloco, Mistura):
            bloco = bloco.escolher(self.rng)
        if callable(bloco):
            raise TypeError("use por_fn para blocos calculados por posição")
        if isinstance(bloco, tuple):
            nome, p0 = bloco
            p = dict(p0)
            p.update(props)
        else:
            nome, p = bloco, dict(props)
        return nome_completo(nome), tuple(sorted((k, _texto(v)) for k, v in p.items()))

    def _conferir(self, x, y, z):
        if not self.dentro(x, y, z):
            raise ValueError(f"{self.titulo}: posição fora da estrutura {(x, y, z)} (tamanho {self.tam})")
        if self.travado:
            x0, z0, x1, z1 = self.travado
            if x0 <= x <= x1 and z0 <= z <= z1:
                raise ValueError(f"{self.titulo}: {(x, y, z)} está na área travada {self.travado}")

    def por(self, x, y, z, bloco, **props):
        self._conferir(x, y, z)
        self.blocos[(x, y, z)] = self._resolver(bloco, props)
        self.dados.pop((x, y, z), None)

    def ar(self, x, y, z):
        self.por(x, y, z, "air")

    def tirar(self, x, y, z):
        """Some do arquivo (o terreno do mundo fica)."""
        self._conferir(x, y, z)
        self.blocos.pop((x, y, z), None)
        self.dados.pop((x, y, z), None)

    def com_dados(self, x, y, z, nbt):
        """Guarda o NBT do bloco que já está em (x, y, z) (block entity: baú, placa, bloco de estrutura)."""
        if (x, y, z) not in self.blocos:
            raise ValueError(f"{self.titulo}: dados sem bloco em {(x, y, z)}")
        self.dados[(x, y, z)] = nbt

    def marcador(self, x, y, z, nome, nota="", alvo="abaixo"):
        """Bloco de estrutura em modo DATA com metadata = nome: o mod acha o ponto e põe a recompensa.

        Convenção: o marcador fica UM BLOCO ACIMA do baú (ou da jukebox) de verdade. O baú já existe no
        arquivo (vazio); o mod lê os marcadores do template e enche o bloco de baixo. Ao colocar, o mod
        ignora o bloco de estrutura em si (ele não aparece no mundo).
        """
        self.por(x, y, z, "structure_block", mode="data")
        self.com_dados(x, y, z, {
            "id": "minecraft:structure_block", "mode": "DATA", "metadata": nome, "name": "", "author": "odteste",
            "posX": N.Int(0), "posY": N.Int(0), "posZ": N.Int(0),
            "sizeX": N.Int(0), "sizeY": N.Int(0), "sizeZ": N.Int(0),
            "rotation": "NONE", "mirror": "NONE", "ignoreEntities": N.Byte(1), "powered": N.Byte(0),
            "showair": N.Byte(0), "showboundingbox": N.Byte(0), "integrity": N.Float(1.0), "seed": N.Long(0),
        })
        if alvo == "abaixo":
            alvo = [x, y - 1, z]
        elif alvo == "aqui":
            alvo = [x, y, z]
        self.marcadores.append({"pos": [x, y, z], "metadata": nome, "alvo": list(alvo), "nota": nota})

    def encher(self, x0, y0, z0, x1, y1, z1, bloco, so_vazio=False, **props):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    if so_vazio and not self.vazio(x, y, z):
                        continue
                    self.por(x, y, z, bloco, **props)

    def ar_onde_vazio(self, x0, y0, z0, x1, y1, z1, condicao=None):
        """Marca como ar o que ficou sem bloco (limpa terra/árvore do mundo ao colar)."""
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    if (x, y, z) in self.blocos:
                        continue
                    if condicao and not condicao(x, y, z):
                        continue
                    self.ar(x, y, z)

    def pista(self, x, y, z, tipo, nota):
        self.pistas.append({"pos": [x, y, z], "tipo": tipo, "nota": nota})

    # ---- saída -----------------------------------------------------------
    def contagem(self):
        cont = {}
        for nome, _ in self.blocos.values():
            cont[nome] = cont.get(nome, 0) + 1
        return dict(sorted(cont.items(), key=lambda kv: -kv[1]))

    def para_nbt(self):
        paleta, indice = [], {}
        lista = N.Lista(N.COMPOUND)
        for pos in sorted(self.blocos, key=lambda p: (p[1], p[2], p[0])):
            nome, props = self.blocos[pos]
            chave = (nome, props)
            if chave not in indice:
                indice[chave] = len(paleta)
                estado = {"Name": nome}
                if props:
                    estado["Properties"] = {k: v for k, v in props}
                paleta.append(estado)
            item = {"pos": N.Lista(N.INT, [N.Int(c) for c in pos]), "state": N.Int(indice[chave])}
            if pos in self.dados:
                item["nbt"] = self.dados[pos]
            lista.append(item)
        return {
            "size": N.Lista(N.INT, [N.Int(c) for c in self.tam]),
            "entities": N.Lista(N.END),
            "blocks": lista,
            "palette": N.Lista(N.COMPOUND, paleta),
            "DataVersion": N.Int(DATA_VERSION_1_21_1),
        }
