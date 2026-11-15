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
from modelos import vila_calada as VILA  # noqa: E402
from modelos import vila_calada_150 as VILA150  # noqa: E402
from modelos import vila_calada_150_v2 as VILA150V2  # noqa: E402

_PRONTAS = {}


def construida(nome):
    """Cada modelo é construído uma vez só para os testes (a vila de 150 demora)."""
    if nome not in _PRONTAS:
        _PRONTAS[nome] = MODELOS[nome].construir()
    return _PRONTAS[nome]


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
        for nome in MODELOS:
            e = construida(nome)
            with tempfile.TemporaryDirectory() as d:
                p = os.path.join(d, nome + ".nbt")
                N.salvar(p, e.para_nbt())
                erros, _, resumo = validar.validar(p, e.tam)
            self.assertEqual(erros, [], nome)
            self.assertGreater(resumo["blocos"], 1000)

    def test_mesma_semente_mesmo_arquivo(self):
        for nome, mod in MODELOS.items():
            if nome in (VILA150.NOME, VILA150V2.NOME):
                continue  # é o centro (testado aqui) mais arredores com a mesma semente; demora demais duas vezes
            a = N.para_bytes(construida(nome).para_nbt())
            b = N.para_bytes(mod.construir().para_nbt())
            self.assertEqual(a, b, nome)

    def test_sangue_so_em_cima_de_bloco_cheio(self):
        for nome in MODELOS:
            e = construida(nome)
            for (x, y, z), (bloco, _) in e.blocos.items():
                if bloco == "minecraft:redstone_wire":
                    self.assertTrue(e.cheio(x, y - 1, z), (nome, x, y, z))


class TesteVila150(unittest.TestCase):
    def test_centro_de_100_fica_intacto(self):
        e = construida(VILA150.NOME)
        centro = construida(VILA.NOME)
        d = VILA150.BORDA
        for (x, y, z), b in centro.blocos.items():
            self.assertEqual(e.blocos.get((x + d, y, z + d)), b, (x + d, y, z + d))
        x0, z0, x1, z1 = VILA150.CENTRO
        dentro = sum(1 for (x, y, z) in e.blocos if x0 <= x <= x1 and z0 <= z <= z1)
        self.assertEqual(dentro, len(centro.blocos))
        self.assertFalse(any(x0 <= x <= x1 and z0 <= z <= z1 for (x, y, z) in e.dados))

    def test_area_travada_nao_aceita_bloco(self):
        e = Estrutura("t", (4, 2, 4))
        e.travado = (1, 1, 2, 2)
        e.por(0, 0, 0, "stone_bricks")
        with self.assertRaises(ValueError):
            e.por(1, 0, 1, "stone_bricks")
        with self.assertRaises(ValueError):
            e.tirar(2, 0, 2)

    def test_marcadores_vao_para_o_nbt(self):
        e = construida(VILA150.NOME)
        nomes = sorted(mk["metadata"] for mk in e.marcadores)
        self.assertEqual(nomes, ["bau_carroca", "bau_cova", "bau_espantalho", "bau_poco_fora", "disco_carroca"])
        with tempfile.TemporaryDirectory() as d:
            p = os.path.join(d, "v.nbt")
            N.salvar(p, e.para_nbt())
            _, raiz = N.ler(p)
        paleta = raiz["palette"]
        lidos = {}
        for b in raiz["blocks"]:
            if paleta[b["state"]]["Name"] == "minecraft:structure_block":
                self.assertEqual(paleta[b["state"]]["Properties"]["mode"], "data")
                self.assertEqual(b["nbt"]["mode"], "DATA")
                lidos[b["nbt"]["metadata"]] = tuple(int(c) for c in b["pos"])
        self.assertEqual(sorted(lidos), nomes)
        for mk in e.marcadores:
            x, y, z = mk["alvo"]
            self.assertEqual(lidos[mk["metadata"]], (x, y + 1, z))
            self.assertIn(e.nome(x, y, z), ("minecraft:chest", "minecraft:jukebox"))

    def test_faixas_cobrem_os_arredores_e_cabem_no_mod(self):
        x0, z0, x1, z1 = VILA150.CENTRO
        sx, _, sz = VILA150.TAM
        cobertura = set()
        for _, fx0, fz0, fx1, fz1 in VILA150.FAIXAS:
            self.assertLessEqual((fx1 - fx0 + 1) * VILA150.TAM[1] * (fz1 - fz0 + 1), 524_288)
            for x in range(fx0, fx1 + 1):
                for z in range(fz0, fz1 + 1):
                    self.assertNotIn((x, z), cobertura)
                    self.assertFalse(x0 <= x <= x1 and z0 <= z <= z1)
                    cobertura.add((x, z))
        self.assertEqual(len(cobertura), sx * sz - (x1 - x0 + 1) * (z1 - z0 + 1))

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


class TesteVila150V2(unittest.TestCase):
    """A v2 (vila morta, 14/11/2026) não pode mexer no que a história usa."""

    def test_capela_e_campanario_so_trocam_material(self):
        v1, v2 = construida(VILA150.NOME), construida(VILA150V2.NOME)
        for caixa in VILA150V2.PROTEGIDAS:
            x0, z0, x1, z1 = caixa
            for x in range(x0, x1 + 1):
                for z in range(z0, z1 + 1):
                    for y in range(VILA150.TAM[1]):
                        a, b = v1.blocos.get((x, y, z)), v2.blocos.get((x, y, z))
                        self.assertEqual(a is None, b is None, (x, y, z))
                        if a is None:
                            continue
                        fa = [c for c, _ in B.formas(a[0], dict(a[1]))]
                        fb = [c for c, _ in B.formas(b[0], dict(b[1]))]
                        self.assertEqual(fa, fb, ((x, y, z), a, b))

    def test_pistas_e_marcadores_no_mesmo_lugar(self):
        v1, v2 = construida(VILA150.NOME), construida(VILA150V2.NOME)
        self.assertEqual([p["pos"] for p in v1.pistas], [p["pos"] for p in v2.pistas[:len(v1.pistas)]])
        self.assertEqual(v1.marcadores, v2.marcadores)
        for mk in v2.marcadores:
            for pos in (mk["pos"], mk["alvo"]):
                self.assertEqual(v1.blocos[tuple(pos)], v2.blocos[tuple(pos)], pos)
            self.assertEqual(v1.dados[tuple(mk["pos"])], v2.dados[tuple(mk["pos"])])
        for p in v1.pistas:
            pos = tuple(p["pos"])
            self.assertEqual(v1.blocos.get(pos), v2.blocos.get(pos), (p["tipo"], pos))

    def test_vila_morta_sem_grama_nem_caminho_amarelo(self):
        e = construida(VILA150V2.NOME)
        cont = e.contagem()
        pistas = {tuple(p["pos"]) for p in e.pistas}
        proibidos = {"minecraft:" + n for n in ("grass_block", "dirt_path", "short_grass", "fern", "stripped_spruce_log")}
        for pos, (n, _) in e.blocos.items():
            if n in proibidos:
                self.assertIn(pos, pistas, (n, pos))  # só o bloco da própria pista fica como era
        self.assertLess(cont.get("minecraft:spruce_planks", 0), cont.get("minecraft:dark_oak_planks", 0))

    def test_a_unica_vela_acesa_continua_unica(self):
        e = construida(VILA150V2.NOME)
        acesas = [p for p, (n, pr) in e.blocos.items() if n.endswith("candle") and dict(pr).get("lit") == "true"]
        self.assertEqual(len(acesas), 1)


if __name__ == "__main__":
    unittest.main()
