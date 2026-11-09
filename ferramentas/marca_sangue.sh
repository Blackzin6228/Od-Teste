#!/usr/bin/env bash
# Gera a arte da Marca de Sangue (texturas por estágio) e as prévias.
# Uso: bash ferramentas/marca_sangue.sh   (só precisa de python3; não usa Pillow)
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PYTHONDONTWRITEBYTECODE=1 python3 "$RAIZ/arte/marca-sangue/gerar_marca.py" "$RAIZ"
