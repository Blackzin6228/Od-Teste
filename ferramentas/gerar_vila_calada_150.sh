#!/usr/bin/env bash
# Gera a Vila Calada 150 x 150 (centro de 100 intacto + arredores): testes da ferramenta, .nbt validados (inteiro
# e em 4 faixas), pistas/marcadores e prévias PNG em arte/vila-calada-150/.
# Uso (na raiz do repositório): bash ferramentas/gerar_vila_calada_150.sh [--sem-previa|--so-geral|--so-partes] [--sem-teste]
set -euo pipefail
cd "$(dirname "$0")/.."
args=()
teste=1
for a in "$@"; do
  if [ "$a" = "--sem-teste" ]; then teste=0; else args+=("$a"); fi
done
if [ "$teste" = 1 ]; then
  python3 -I ferramentas/estruturas/testar.py
fi
python3 -I ferramentas/estruturas/previa_vila_calada_150.py "${args[@]}"
