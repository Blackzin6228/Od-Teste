#!/usr/bin/env bash
# Prévia de modelos. Só precisa de python3 (já vem no Ubuntu/WSL).
#
#   bash ferramentas/previa/previa.sh MODELO.bbmodel [--saida arte/previas/x.png]
#   bash ferramentas/previa/previa.sh MODELO.geo.json --textura T.png [--saida ...]
#   bash ferramentas/previa/previa.sh exemplos     # recria os modelos de exemplo e as prévias deles
set -euo pipefail
AQUI="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RAIZ="$(cd "$AQUI/../.." && pwd)"

if [[ "${1:-}" == "exemplos" ]]; then
  python3 -I "$AQUI/gerar_exemplos.py" "$RAIZ"
  python3 -I "$AQUI/previa.py" "$RAIZ/arte/blockbench/exemplo.bbmodel" --saida "$RAIZ/arte/previas/exemplo.png"
  python3 -I "$AQUI/previa.py" "$AQUI/teste/exemplo.geo.json" --textura "$RAIZ/arte/texturas/exemplo.png" \
    --saida "$AQUI/teste/exemplo_geo.png"
  python3 -I "$AQUI/previa.py" "$RAIZ/arte/blockbench/espada_exemplo.bbmodel" --saida "$RAIZ/arte/previas/espada_exemplo.png"
  exit 0
fi

exec python3 -I "$AQUI/previa.py" "$@"
