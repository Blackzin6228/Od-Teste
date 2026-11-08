#!/usr/bin/env bash
# Baixa do modpack (Blackzin6228/Conguitos-smp) os jars dos mods que o mod usa para compilar.
# Assim ele compila sempre contra as mesmas versões que estão no servidor.
# A lista sai do build.gradle (libsDoPack), para não ter duas listas para manter.
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PACK="https://raw.githubusercontent.com/Blackzin6228/Conguitos-smp/main/jars"
DESTINO="$RAIZ/libs"
mkdir -p "$DESTINO"

mapfile -t JARS < <(sed -n "/def libsDoPack = \[/,/^\]/p" "$RAIZ/build.gradle" | grep -oP "'\K[^']+\.jar")

for nome in "${JARS[@]}"; do
  if [[ -s "$DESTINO/$nome" ]]; then
    echo "ok (já tinha): $nome"
    continue
  fi
  url="$PACK/$(python3 -c 'import sys,urllib.parse;print(urllib.parse.quote(sys.argv[1]))' "$nome")"
  echo "baixando: $nome"
  curl -fsSL --retry 3 -o "$DESTINO/$nome.tmp" "$url"
  # Confere se é um jar de verdade (zip), e não uma página de erro.
  if ! unzip -tq "$DESTINO/$nome.tmp" >/dev/null 2>&1; then
    echo "ERRO: $nome não é um jar válido" >&2
    rm -f "$DESTINO/$nome.tmp"
    exit 1
  fi
  mv "$DESTINO/$nome.tmp" "$DESTINO/$nome"
done

echo "Pronto: ${#JARS[@]} jars em $DESTINO"
