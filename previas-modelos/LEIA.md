# Prévias: Martelo do Ferreiro e Faminto (mesa Modelador, 14/11/2026)

Status: **só prévia, para o Nikolas escolher.** Nada disto está no jogo nem em `src/`. Sem ID novo.
Base: proposta A do enigma "O Dobre" (`lore/enigma-vila-calada-v2.md`, branch `mesa-lore/horror-rpg`) e a ficha da Vila Calada.
Textura em **32 px por bloco** (a resolução ainda está pendente em `docs/decisoes.md`; dá para voltar a 16 com um número).

## O que olhar primeiro
| Arquivo | O que é |
|---|---|
| `faminto/antes_depois.png` | Faminto antigo (13 cubos) x conceitos A e B, mesma escala |
| `faminto/conceitos_a_b.png` | A e B: frente, 3/4, lado, costas, noite e silhueta |
| `faminto/no_escuro_de_longe.png` | Como cada um aparece a ~30 blocos, de noite (ampliado 4x) |
| `faminto/fim_do_dobre.png` | Escuta o toque, cede, de joelhos, vira cinza |
| `faminto/<a\|b>/anim_*.png` | 9 animações: parado, andar, correr, ataque, dano, morrer, escutar, ajoelhar, cinza |
| `faminto/<a\|b>/noite.png` | Vistas com luz da lua e neblina vermelha |
| `martelo/martelo_v1_v2.png` | As duas variações do martelo, de dia, de noite e o ícone |
| `martelo/v<n>/vistas.png` | Cada martelo: 5 ângulos, noite, ícone, slot do inventário e textura |

## Faminto
- **A, "O Que Sobrou"** (40 cubos): ainda é gente da vila. Cabeça alta de aldeão, nariz murcho, órbitas vazias,
  manto queimado em tiras, corcunda com vértebras, mandíbula solta até o peito com a pele das bochechas rasgada.
- **B, "O Esfomeado"** (50 cubos): o Sangue comeu o resto. Costelas abertas sobre a cavidade vermelha, espinha
  rompendo as costas em espinhos (lê na silhueta), trapo do manto virou capuz, pernas na ponta dos pés, braços que arrastam.
- Vermelho só na boca, nas costelas abertas (B) e no sangue seco: um acento.
- Fonte: `arte/blockbench/faminto_a.bbmodel` e `faminto_b.bbmodel`. Regerar: `bash ferramentas/faminto.sh`.

## Martelo do Ferreiro
- **V1, "Martelo de Forja"**: martelo de pena (face chata + cunha), pano de linho enrolado com sangue velho,
  entalhes de contagem em grupos de cinco no cabo (os mesmos riscos da parede do campanário).
- **V2, "O que Rachou o Sino"**: marreta rachada com sangue seco na fenda, face amassada com lascas de bronze do sino,
  pedaço da corrente do sino no pomo, pano preto de luto. O último grupo de entalhes tem só quatro: falta o Sineiro.
- Na mão: modelo 3D (elementos Java). No inventário: ícone desenhado 32x32 (`neoforge:separate_transforms`).
- Fonte: `arte/blockbench/martelo_ferreiro_v<n>.bbmodel`. Regerar: `bash ferramentas/martelo.sh`.

## Falta (depois da escolha)
- Ajustar `display` do martelo dentro do jogo (1ª e 3ª pessoa, moldura, chão).
- Sons, partículas de cinza e o código (mesa Dev); IDs em `docs/ids.md`.
