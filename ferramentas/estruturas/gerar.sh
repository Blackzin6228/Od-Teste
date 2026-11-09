#!/usr/bin/env bash
# Gera as estruturas (.nbt validado + prévias PNG). Uso: bash ferramentas/estruturas/gerar.sh [nome...]
set -euo pipefail
cd "$(dirname "$0")/../.."
exec python3 -I ferramentas/estruturas/gerar.py "$@"
