#!/usr/bin/env bash
# Gera a Vila Destruída: schematic .nbt validado, lista de pistas, testes da ferramenta e prévias PNG.
# Uso (na raiz do repositório): bash ferramentas/gerar_vila.sh [--sem-previa]
set -euo pipefail
cd "$(dirname "$0")/.."
python3 -I ferramentas/estruturas/gerar.py --sem-previa vila_destruida
python3 -I ferramentas/estruturas/testar.py
if [[ "${1:-}" != "--sem-previa" ]]; then
  python3 -I ferramentas/estruturas/previa_partes.py vila_destruida
fi
