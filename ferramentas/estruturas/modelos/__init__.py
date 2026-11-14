"""Cada estrutura da história é um módulo aqui com construir() -> Estrutura e CORTES."""
from modelos import capela_fazenda, vila_calada, vila_calada_150, vila_calada_150_v2, vila_destruida

MODELOS = {
    capela_fazenda.NOME: capela_fazenda,
    vila_destruida.NOME: vila_destruida,
    vila_calada.NOME: vila_calada,
    vila_calada_150.NOME: vila_calada_150,
    vila_calada_150_v2.NOME: vila_calada_150_v2,
}
