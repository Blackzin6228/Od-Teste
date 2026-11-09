"""Testes rápidos da ferramenta. Uso: python3 -I ferramentas/estruturas/testar.py"""
import os
import sys
import tempfile
import unittest

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, AQUI)

import blocos as B  # noqa: E402
import nbt_io as N  # noqa: E402
import render  # noqa: E402
import validar  # noqa: E402
from estrutura import Estrutura  # noqa: E402
from modelos import MODELOS  # noqa: E402


class TesteNBT(unittest.TestCase):
    def test_ida_e_volta(self):
        raiz = {"a": N.Int(5), "b": "texto", "c": N.Lista(N.INT, [N.Int(1), N.Int(-2)]),
                "d": N.Lista(N.END), "e": {"f": N.Byte(1)}}
        with tempfile.TemporaryDirectory() as d:
            p = os.path.join(d, "x.nbt")
            N.salvar(p, raiz)
            nome, lido = N.ler(p)
        self.assertEqual(nome, "")
        self.assertEqual(lido, raiz)
        self.assertIsInstance(lido["a"], N.Int)
        self.assertEqual(lido["c"].tipo, N.INT)

    def test_validador_pega_bloco_proibido(self):
        e = Estrutura("t", (2, 2, 2))
        e.por(0, 0, 0, "stone_bricks")
        e.por(1, 0, 0, "oak_planks")  # fora da lista
        with tempfile.TemporaryDirectory() as d:
            p = os.path.join(d, "t.nbt")
            N.salvar(p, e.para_nbt())
            erros, _, _ = validar.validar(p, (2, 2, 2))
        self.assertTrue(any("oak_planks" in x for x in erros))

    def test_validador_pega_propriedade_errada(self):
        e = Estrutura("t", (1, 1, 1))
        e.por(0, 0, 0, "spruce_stairs", facing="up")
        with tempfile.TemporaryDirectory() as d:
            p = os.path.join(d, "t.nbt")
            N.salvar(p, e.para_nbt())
            erros, _, _ = validar.validar(p)
        self.assertTrue(erros)


class TesteModelos(unittest.TestCase):
    def test_todos_os_modelos_validam(self):
        for nome, mod in MODELOS.items():
            e = mod.construir()
            with tempfile.TemporaryDirectory() as d:
                p = os.path.join(d, nome + ".nbt")
                N.salvar(p, e.para_nbt())
                erros, _, resumo = validar.validar(p, e.tam)
            self.assertEqual(erros, [], nome)
            self.assertGreater(resumo["blocos"], 1000)

    def test_mesma_semente_mesmo_arquivo(self):
        for mod in MODELOS.values():
            a = N.para_bytes(mod.construir().para_nbt())
            b = N.para_bytes(mod.construir().para_nbt())
            self.assertEqual(a, b)

    def test_sangue_so_em_cima_de_bloco_cheio(self):
        for mod in MODELOS.values():
            e = mod.construir()
            for (x, y, z), (nome, _) in e.blocos.items():
                if nome == "minecraft:redstone_wire":
                    self.assertTrue(e.cheio(x, y - 1, z), (x, y, z))

    def test_render_pequeno(self):
        e = Estrutura("t", (2, 2, 2))
        e.por(0, 0, 0, "stone_bricks")
        e.por(1, 0, 0, "spruce_stairs", facing="east", half="bottom", shape="straight", waterlogged=False)
        e.por(0, 1, 0, "glass_pane", east=True, west=True, north=False, south=False, waterlogged=False)
        q = render.iso(e)
        self.assertTrue(any(q.px[i] != render.FUNDO[0] for i in range(0, len(q.px), 3)))
        render.planta(e, 0, 1)

    def test_tabela_tem_formas_para_todos(self):
        for n in B.BLOCOS:
            for _, mat in B.formas("minecraft:" + n, {}):
                B.material(mat)


if __name__ == "__main__":
    unittest.main()
