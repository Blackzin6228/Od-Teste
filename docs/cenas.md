# Cenas de câmera (cutscenes)

Duas formas de cena convivem:
- **De código** (`cenas/CenasConhecidas`): a entrada da Vila Calada (`entrada_vila_calada`), que dispara sozinha.
- **Em JSON** (desde 14/11/2026): `src/main/resources/data/odteste/cenas/<id>.json`. Um datapack do mundo pode trocar
  ou somar cenas sem jar novo (`/reload` relê). Exemplo: `chegada_vila_calada.json`.

## Como tocar
- Mestre: `/odteste cena tocar <cena> <alvos>`, `/odteste cena parar <alvos>`, `/odteste cena lista`,
  `/odteste cena esquecer <cena> <alvos>`.
- Código (eventos da história): `Cinema.tocar(jogador, "id")`, `Cinema.tocar(lista, "id", null)`, `Cinema.parar(jogador)`.
- Só os alvos recebem. Mestre em espectador com `/odteste mestre ouvir on` vê junto se estiver com a câmera presa num
  alvo, a até 48 blocos dele, ou se foi ele quem digitou.

## O que acontece
- Antes: a visão escurece em 0,5 s. Barras de cinema entram em 1 s. HUD, mão e mira somem.
- O jogador fica **parado e protegido no servidor** (sem dano, sem bater/usar/quebrar). Só em memória: sair, morrer,
  trocar de dimensão ou o servidor cair solta na hora. `/kill` e o vazio ainda passam.
- Pular: segurar o pulo 1 s, só se a cena (`"pular"`) e a config `mestre.pular_cenas` deixarem.
- "Efeitos fortes de tela": diminui o tremor (não desliga a cena).

## Formato
```json
{
  "duracao": 400,              // ticks de câmera (20 = 1 s; até 2 min)
  "ancora": "sino_rachado",    // "jogador" (padrão) ou um lugar: sino_rachado
  "pular": true,
  "barras": 0.11,              // altura de cada barra (0 a 0,25; 0 = sem)
  "saida": "direta",           // "preto" (padrão: escurece e volta) ou "direta" (volta direto aos olhos)
  "mostrar_jogador": true,     // o boneco do próprio jogador aparece (para a câmera olhar para ele)
  "chaves": [ ... ],
  "eventos": [ ... ]
}
```

### Chaves (pontos de câmera)
| Campo | O que é |
|---|---|
| `t` | tick (a primeira é 0; em ordem) |
| `em` | referência: `ancora`, `jogador` (olhos do alvo, eixos da âncora), `entre` (entre os olhos e a âncora, com `entre` 0 a 1), `olhos` (olhos do alvo e para onde ele olha) |
| `pos` | `[x, y, z]`: x direita, y cima, z frente |
| `olhar` | `[x, y, z]` (na referência `olhar_em`, padrão a mesma) ou `"jogador"` (os olhos do alvo). Sem `olhar`: `giro` e `inclinacao` em graus (em `olhos`, somados aos do alvo) |
| `fov` | campo de visão (sem = o do jogador) |
| `roll` | câmera deitada para o lado (graus) |
| `curva` | até a próxima: `catmull` (padrão, curva suave pelos pontos), `linear`, `corte` (fica e pula) |
| `ritmo` | até a próxima: `linear` (padrão), `suave`, `entra`, `sai` |

Para terminar "nos olhos": última chave `{"em": "olhos", "pos": [0,0,0], "giro": 0, "inclinacao": 0}` e `"saida": "direta"`.

### Eventos
| `tipo` | Campos |
|---|---|
| `som` | `som` (id, ex. `minecraft:block.bell.use`), `volume` 0-1, `tom` 0,5-2 |
| `fade` | `cor` (`preto`, `vermelho`, `#RRGGBB`; sem = a do fade anterior), `de` (opcional), `ate` 0-1, `duracao` |
| `texto` | `chave` (tradução `odteste.`, nos dois idiomas), `linha` 0 (grande) ou 1, `duracao` |
| `tremor` | `forca` (até 2), `duracao` |
| `fx` | `efeito` (`sangue`, `coracao`, `carmesim`, `marcado`, `cor_horror`), `segundos` |
| `parar_sons` | — |

Cena com erro não carrega (aviso no log do servidor). O teste `RegrasRoteiroTest` confere as cenas do mod.

## Técnica (por que é estável com Sodium/Iris)
Sem entidade de câmera e sem mixin. O giro entra no `ViewportEvent.ComputeCameraAngles` (no meio da montagem da
câmera); a posição entra no `ViewportEvent.ComputeFov`, que o jogo chama logo depois da montagem e antes de desenhar
o mundo. Daí em diante tudo (Sodium, Iris, culling, névoa, som) lê a câmera já no lugar. O terreno precisa estar
carregado no cliente: por isso o lugar da cena vale só até 96 blocos do alvo.
