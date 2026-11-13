#!/usr/bin/env bash
# Prévias PNG de um modelo GeckoLib (frente, lado, costas, 3/4, silhueta e quadros de animação), sem Blockbench.
# Uso: bash ferramentas/render_geckolib.sh --geo X.geo.json --textura X.png --saida pasta/ [--anim X.animation.json]
#      [--brilho X_glowmask.png] [--escala 6] [--quadros 8] [--nome NOME]
# Só precisa de python3 + numpy.
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PYTHONDONTWRITEBYTECODE=1 python3 -I "$RAIZ/ferramentas/render_geckolib.py" "$@"
