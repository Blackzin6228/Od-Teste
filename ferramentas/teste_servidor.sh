#!/usr/bin/env bash
# Teste de fumaça: liga um servidor com o pack INTEIRO (Conguitos ATM10) + o jar novo do mod.
# Passa só se o servidor chegar no "Done" sem crash e o mod disser que carregou.
# Uso: bash ferramentas/teste_servidor.sh <caminho-do-jar> [pasta-de-trabalho]
set -euo pipefail

JAR="$(realpath "$1")"
PASTA="$(realpath -m "${2:-servidor-teste}")"
NEO="${NEO_VERSION:-21.1.262}"
PACK_URL="${PACK_URL:-https://conguitos-atm10-pack.vercel.app/pack.toml}"
BOOT_URL="https://github.com/packwiz/packwiz-installer-bootstrap/releases/latest/download/packwiz-installer-bootstrap.jar"
PACK_REPO="${PACK_REPO:-https://raw.githubusercontent.com/Blackzin6228/Conguitos-smp/main}"
LIMITE_SEG="${LIMITE_SEG:-1500}"    # 25 minutos para ligar (ATM10 é grande)
MEMORIA="${MEMORIA:-10G}"
# mods só de cliente que vão soltos no pack ATM10 (o servidor não usa)
SO_CLIENTE="moreoverlays-1.24.2-mc1.21.1-neoforge.jar notenoughanimations-neoforge-1.12.7-mc1.21.1.jar"
MODID="${MODID:-$(grep '^mod_id=' gradle.properties | cut -d= -f2)}"

mkdir -p "$PASTA"
cd "$PASTA"

# 1) NeoForge do servidor (mesma versão do Lura)
if [[ ! -f run.sh ]]; then
  echo "== instalando NeoForge $NEO"
  curl -fsSL --retry 3 -o neoforge-installer.jar \
    "https://maven.neoforged.net/releases/net/neoforged/neoforge/$NEO/neoforge-$NEO-installer.jar"
  java -jar neoforge-installer.jar --installServer > instalador.log 2>&1 || { tail -50 instalador.log; exit 1; }
fi

# 2) Mods do pack, do jeito que o servidor de verdade baixa (packwiz, lado servidor)
echo "== baixando o pack ATM10 (lado servidor)"
curl -fsSL --retry 3 -o packwiz-installer-bootstrap.jar "$BOOT_URL"
baixou=0
for tentativa in 1 2 3; do
  if java -jar packwiz-installer-bootstrap.jar -g -s server "$PACK_URL" > packwiz.log 2>&1; then baixou=1; break; fi
  echo "packwiz falhou (tentativa $tentativa):"; grep -m5 -i "error\|failed\|excluded" packwiz.log || tail -5 packwiz.log
  sleep 10
done
[[ $baixou == 1 ]] || { echo "Não deu para baixar o pack."; tail -30 packwiz.log; exit 1; }
for j in $SO_CLIENTE; do rm -f "mods/$j"; done

# 3) Troca a versão do nosso mod (se o pack já tiver uma) pela nova
rm -f "mods/$MODID-"*.jar
cp "$JAR" mods/
echo "== mods instalados: $(ls mods/*.jar | wc -l)"

# 4) Liga o servidor
echo "eula=true" > eula.txt
cat > server.properties <<PROPS
online-mode=false
level-name=mundo-teste
level-seed=conguitos
spawn-protection=0
max-tick-time=-1
PROPS
echo "-Xms4G -Xmx$MEMORIA" > user_jvm_args.txt
rm -rf logs crash-reports

mkfifo entrada || true
tail -f entrada | ./run.sh nogui > console.log 2>&1 &
PID=$!
exec 3>entrada

resultado="tempo"
for ((s = 0; s < LIMITE_SEG; s += 5)); do
  sleep 5
  if grep -q 'Done (' console.log; then resultado="ok"; break; fi
  if ls crash-reports/*.txt >/dev/null 2>&1; then resultado="crash"; break; fi
  if ! kill -0 "$PID" 2>/dev/null; then resultado="morreu"; break; fi
done

# Deixa rodar 30 s de jogo (pega erro de tick logo após ligar) e desliga
if [[ "$resultado" == "ok" ]]; then
  sleep 30
  ls crash-reports/*.txt >/dev/null 2>&1 && resultado="crash"
  echo "stop" >&3
  for _ in $(seq 1 60); do kill -0 "$PID" 2>/dev/null || break; sleep 2; done
fi
kill "$PID" 2>/dev/null || true
pkill -f 'tail -f entrada' 2>/dev/null || true

echo
echo "================ RESULTADO: $resultado ================"
if [[ "$resultado" != "ok" ]]; then
  echo "--- fim do console ---"; tail -80 console.log
  ls crash-reports/*.txt >/dev/null 2>&1 && { echo "--- crash report ---"; head -80 crash-reports/*.txt; }
  exit 1
fi
if ! grep -q "$MODID carregado" console.log logs/latest.log 2>/dev/null; then
  echo "O servidor ligou, mas o mod $MODID não avisou que carregou."; exit 1
fi
grep -h "$MODID carregado" logs/latest.log | tail -1
echo "Servidor ligou com o pack inteiro + $MODID, sem crash."
