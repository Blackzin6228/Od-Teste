#!/usr/bin/env bash
# Folha de comparação das criaturas de Sangue: provisório (jogo hoje) x 5.1 x 5.2, mesmos ângulos e escala.
# Saída: arte/previas/criaturas/comparacao/<criatura>.png e folha_sangue.png. Não mexe em src/.
# Uso: bash ferramentas/comparar_criaturas_sangue.sh   (só python3 + numpy)
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PYTHONDONTWRITEBYTECODE=1 python3 -I "$RAIZ/arte/previas/criaturas/fonte/comparacao.py" "$RAIZ"
