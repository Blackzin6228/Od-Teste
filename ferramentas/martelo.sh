#!/usr/bin/env bash
# Gera a PRÉVIA do Martelo do Ferreiro (V1 e V2): texturas 32x32, ícone, modelos JSON, .bbmodel e renders.
# Saída: previas-modelos/martelo/ e arte/blockbench/martelo_ferreiro_v<n>.bbmodel. Não mexe em src/. Só python3 + numpy.
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PYTHONDONTWRITEBYTECODE=1 python3 -I "$RAIZ/arte/previas/itens/fonte/martelo.py" "$RAIZ"
