#!/usr/bin/env bash
# Gera a arte da Marca de Sangue v2 (texturas por estágio, brilho, prévias e GIF).
# Uso: bash ferramentas/marca_sangue_v2.sh                 -> gera tudo (uns 10 s)
#      bash ferramentas/marca_sangue_v2.sh --so-texturas   -> só os PNG de arte/marca-sangue/v2
#      bash ferramentas/marca_sangue_v2.sh --checar        -> só confere se numpy (e Pillow) estão instalados
# Precisa de python3 com numpy (pip install numpy). Pillow não é usado.
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
DIR="$RAIZ/arte/marca-sangue/v2"
if [[ "${1:-}" == "--checar" ]]; then
  exec python3 -I -B "$DIR/checar_ambiente.py"
fi
# -I: ignora o ambiente e a pasta atual (seguranca); -B: nao grava __pycache__
exec python3 -I -B "$DIR/gerar_marca_v2.py" "$RAIZ" "$@"
