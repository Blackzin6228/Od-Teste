"""Leitura e escrita de NBT (formato do Minecraft Java), sem dependências.

Só o necessário para arquivos de estrutura (.nbt): raiz Compound com nome vazio, gzip.
Tipos: inteiros Python viram TAG_Int; use Byte/Short/Long/Float/Double para os outros.
Listas precisam dizer o tipo do item: Lista(INT, [...]).
"""
import gzip
import struct

(END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY,
 STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY) = range(13)


class Byte(int):
    pass


class Short(int):
    pass


class Int(int):
    pass


class Long(int):
    pass


class Float(float):
    pass


class Double(float):
    pass


class Lista(list):
    """Lista NBT com tipo de item fixo (END para lista vazia sem tipo)."""

    def __init__(self, tipo, itens=()):
        super().__init__(itens)
        self.tipo = tipo


def tipo_de(v):
    if isinstance(v, bool):
        raise TypeError("bool não existe em NBT; use Byte(0/1)")
    if isinstance(v, Byte):
        return BYTE
    if isinstance(v, Short):
        return SHORT
    if isinstance(v, Long):
        return LONG
    if isinstance(v, int):
        return INT
    if isinstance(v, Double):
        return DOUBLE
    if isinstance(v, float):
        return FLOAT
    if isinstance(v, str):
        return STRING
    if isinstance(v, Lista):
        return LIST
    if isinstance(v, dict):
        return COMPOUND
    if isinstance(v, (bytes, bytearray)):
        return BYTE_ARRAY
    raise TypeError(f"tipo sem NBT: {type(v)!r}")


_FMT = {BYTE: ">b", SHORT: ">h", INT: ">i", LONG: ">q", FLOAT: ">f", DOUBLE: ">d"}


def _escrever(out, tipo, v):
    if tipo in _FMT:
        out.append(struct.pack(_FMT[tipo], v))
    elif tipo == STRING:
        b = v.encode("utf-8")
        if len(b) > 65535:
            raise ValueError("texto grande demais para NBT")
        out.append(struct.pack(">H", len(b)) + b)
    elif tipo == BYTE_ARRAY:
        out.append(struct.pack(">i", len(v)) + bytes(v))
    elif tipo == LIST:
        ti = v.tipo if len(v) else END
        out.append(struct.pack(">bi", ti, len(v)))
        for item in v:
            if tipo_de(item) != ti:
                raise TypeError(f"item de tipo {tipo_de(item)} numa lista do tipo {ti}")
            _escrever(out, ti, item)
    elif tipo == COMPOUND:
        for k, val in v.items():
            t = tipo_de(val)
            out.append(struct.pack(">b", t))
            _escrever(out, STRING, k)
            _escrever(out, t, val)
        out.append(b"\x00")
    elif tipo == INT_ARRAY:
        out.append(struct.pack(">i", len(v)) + b"".join(struct.pack(">i", i) for i in v))
    elif tipo == LONG_ARRAY:
        out.append(struct.pack(">i", len(v)) + b"".join(struct.pack(">q", i) for i in v))
    else:
        raise TypeError(f"tipo NBT desconhecido {tipo}")


def para_bytes(raiz, nome=""):
    out = [struct.pack(">b", COMPOUND)]
    _escrever(out, STRING, nome)
    _escrever(out, COMPOUND, raiz)
    return b"".join(out)


def salvar(caminho, raiz):
    # mtime=0: o mesmo conteúdo gera sempre o mesmo arquivo (diff limpo no git).
    dados = gzip.compress(para_bytes(raiz), compresslevel=9, mtime=0)
    with open(caminho, "wb") as f:
        f.write(dados)


class _Leitor:
    def __init__(self, b):
        self.b = b
        self.i = 0

    def num(self, fmt):
        v = struct.unpack_from(fmt, self.b, self.i)[0]
        self.i += struct.calcsize(fmt)
        return v

    def payload(self, t):
        if t == BYTE:
            return Byte(self.num(">b"))
        if t == SHORT:
            return Short(self.num(">h"))
        if t == INT:
            return Int(self.num(">i"))
        if t == LONG:
            return Long(self.num(">q"))
        if t == FLOAT:
            return Float(self.num(">f"))
        if t == DOUBLE:
            return Double(self.num(">d"))
        if t == BYTE_ARRAY:
            n = self.num(">i")
            v = bytes(self.b[self.i:self.i + n])
            self.i += n
            return v
        if t == STRING:
            n = self.num(">H")
            s = self.b[self.i:self.i + n].decode("utf-8")
            self.i += n
            return s
        if t == LIST:
            ti = self.num(">b")
            n = self.num(">i")
            return Lista(ti, [self.payload(ti) for _ in range(n)])
        if t == COMPOUND:
            d = {}
            while True:
                ti = self.num(">b")
                if ti == END:
                    return d
                k = self.payload(STRING)
                d[k] = self.payload(ti)
        if t == INT_ARRAY:
            n = self.num(">i")
            return [self.num(">i") for _ in range(n)]
        if t == LONG_ARRAY:
            n = self.num(">i")
            return [self.num(">q") for _ in range(n)]
        raise ValueError(f"tipo NBT desconhecido {t} na posição {self.i}")


def ler(caminho):
    """Devolve (nome_da_raiz, compound_raiz). Aceita arquivo com ou sem gzip."""
    with open(caminho, "rb") as f:
        bruto = f.read()
    b = gzip.decompress(bruto) if bruto[:2] == b"\x1f\x8b" else bruto
    leitor = _Leitor(b)
    t = leitor.num(">b")
    if t != COMPOUND:
        raise ValueError("a raiz do NBT não é um Compound")
    nome = leitor.payload(STRING)
    raiz = leitor.payload(COMPOUND)
    if leitor.i != len(b):
        raise ValueError(f"sobraram {len(b) - leitor.i} bytes depois da raiz")
    return nome, raiz
