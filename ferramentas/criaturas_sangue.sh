#!/usr/bin/env bash
# Gera a PRÉVIA das criaturas de Sangue (Retalhado e Sanguessuga-Mãe): geo, textura, animações, .bbmodel e renders.
# Saída: arte/previas/criaturas/<criatura>/ e arte/blockbench/<criatura>.bbmodel. Não mexe em src/.
# Uso: bash ferramentas/criaturas_sangue.sh [retalhado|sanguessuga_mae|magarefe|todas]   (só python3 + numpy)
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PYTHONDONTWRITEBYTECODE=1 python3 -I "$RAIZ/arte/previas/criaturas/fonte/gerar_criaturas_sangue.py" "$RAIZ" "${1:-todas}"
