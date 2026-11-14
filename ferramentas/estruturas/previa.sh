#!/usr/bin/env bash
# Prévias de estrutura grande (vista de cima + por partes). Uso: bash ferramentas/estruturas/previa.sh <nome>
set -euo pipefail
cd "$(dirname "$0")/../.."
exec python3 -I ferramentas/estruturas/previa_partes.py "$@"
