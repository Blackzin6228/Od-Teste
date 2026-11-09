"""Relê um .nbt de estrutura e confere se o Minecraft 1.21.1 (e o Create) vão aceitar.

Uso: python3 ferramentas/estruturas/validar.py arte/estruturas/<nome>.nbt [...]
Saída 0 = tudo certo; 1 = erro.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import blocos as B  # noqa: E402
import nbt_io as N  # noqa: E402

DATA_VERSION = 3955  # Minecraft 1.21.1


def validar(caminho, tam_esperado=None):
    erros, avisos = [], []
    try:
        nome_raiz, r = N.ler(caminho)
    except Exception as ex:  # arquivo corrompido
        return [f"não consegui ler o NBT: {ex}"], [], {}

    if nome_raiz != "":
        avisos.append(f"raiz com nome {nome_raiz!r} (o vanilla usa nome vazio)")

    dv = r.get("DataVersion")
    if not isinstance(dv, N.Int) or dv != DATA_VERSION:
        erros.append(f"DataVersion = {dv!r}, esperado Int {DATA_VERSION} (1.21.1)")

    size = r.get("size")
    if not (isinstance(size, N.Lista) and size.tipo == N.INT and len(size) == 3 and all(s > 0 for s in size)):
        erros.append(f"size inválido: {size!r}")
        size = None
    else:
        size = tuple(int(s) for s in size)
        if tam_esperado and size != tuple(tam_esperado):
            erros.append(f"size {size} diferente do esperado {tuple(tam_esperado)}")
        if max(size) > 48:
            avisos.append("passa de 48 blocos num eixo: o Bloco de Estrutura não carrega; /place template e Create carregam")

    paleta = r.get("palette")
    if not (isinstance(paleta, N.Lista) and paleta.tipo == N.COMPOUND and paleta):
        erros.append("palette ausente, vazia ou de tipo errado")
        paleta = []
    vistos = set()
    for i, est in enumerate(paleta):
        nome = est.get("Name")
        if not isinstance(nome, str):
            erros.append(f"palette[{i}] sem Name")
            continue
        if nome not in B.PERMITIDOS:
            erros.append(f"palette[{i}]: bloco fora da lista permitida: {nome}")
            continue
        props = est.get("Properties", {})
        if not isinstance(props, dict):
            erros.append(f"palette[{i}] {nome}: Properties não é Compound")
            continue
        validas = B.props_validas(B.curto(nome))
        for k, v in props.items():
            if not isinstance(v, str):
                erros.append(f"palette[{i}] {nome}: propriedade {k} não é texto")
            elif k not in validas:
                erros.append(f"palette[{i}] {nome}: propriedade desconhecida {k}")
            elif v not in validas[k]:
                erros.append(f"palette[{i}] {nome}: {k}={v} não existe")
        chave = (nome, tuple(sorted(props.items())))
        if chave in vistos:
            avisos.append(f"palette[{i}] repetida: {nome} {dict(props)}")
        vistos.add(chave)

    lista = r.get("blocks")
    contagem, usados, posicoes = {}, set(), set()
    if not (isinstance(lista, N.Lista) and lista.tipo in (N.COMPOUND, N.END)):
        erros.append("blocks ausente ou de tipo errado")
        lista = []
    for j, b in enumerate(lista):
        s, pos = b.get("state"), b.get("pos")
        if not isinstance(s, N.Int) or not 0 <= s < len(paleta):
            erros.append(f"blocks[{j}]: state {s!r} fora da paleta")
            continue
        if not (isinstance(pos, N.Lista) and pos.tipo == N.INT and len(pos) == 3):
            erros.append(f"blocks[{j}]: pos inválido {pos!r}")
            continue
        p = tuple(int(c) for c in pos)
        if size and not all(0 <= p[k] < size[k] for k in range(3)):
            erros.append(f"blocks[{j}]: pos {p} fora do size {size}")
        if p in posicoes:
            erros.append(f"blocks[{j}]: posição repetida {p}")
        posicoes.add(p)
        usados.add(int(s))
        nome = paleta[s].get("Name", "?")
        contagem[nome] = contagem.get(nome, 0) + 1
        if "nbt" in b and not isinstance(b["nbt"], dict):
            erros.append(f"blocks[{j}]: nbt não é Compound")

    nao_usados = len(paleta) - len(usados)
    if nao_usados:
        avisos.append(f"{nao_usados} entradas da paleta sem uso")

    ent = r.get("entities")
    if not isinstance(ent, N.Lista):
        erros.append("entities ausente (o vanilla espera uma lista, mesmo vazia)")

    resumo = {"size": size, "paleta": len(paleta), "blocos": len(lista),
              "contagem": dict(sorted(contagem.items(), key=lambda kv: -kv[1]))}
    return erros, avisos, resumo


def imprimir(caminho, erros, avisos, resumo):
    print(f"== {caminho}")
    if resumo:
        print(f"   size {resumo['size']} · paleta {resumo['paleta']} estados · {resumo['blocos']} blocos gravados")
        for nome, n in resumo["contagem"].items():
            print(f"     {n:6d}  {nome}")
    for a in avisos:
        print(f"   aviso: {a}")
    for e in erros:
        print(f"   ERRO: {e}")
    print("   OK" if not erros else f"   FALHOU ({len(erros)} erros)")


def main(args):
    falhou = False
    for caminho in args:
        erros, avisos, resumo = validar(caminho)
        imprimir(caminho, erros, avisos, resumo)
        falhou |= bool(erros)
    return 1 if falhou else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
