#!/usr/bin/env bash
# Gera a PRÉVIA do Faminto (conceitos A e B, 32 px por bloco): geo, textura, animações, .bbmodel e renders.
# Saída: previas-modelos/faminto/ e arte/blockbench/faminto_<a|b>.bbmodel. Não mexe em src/. Só python3 + numpy.
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PYTHONDONTWRITEBYTECODE=1 python3 -I "$RAIZ/arte/previas/criaturas/fonte/gerar_faminto.py" "$RAIZ"
