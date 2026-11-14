#!/usr/bin/env bash
# Gera a Vila Calada (100 x 100): testes da ferramenta, schematic .nbt validado, pistas e prévias PNG
# em arte/vila-calada-100/. Uso (na raiz do repositório): bash ferramentas/gerar_vila_calada.sh [--sem-previa]
set -euo pipefail
cd "$(dirname "$0")/.."
python3 -I ferramentas/estruturas/testar.py
python3 -I ferramentas/estruturas/previa_vila_calada.py "$@"
