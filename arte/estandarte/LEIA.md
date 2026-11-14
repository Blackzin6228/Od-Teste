# Estandarte da vila comum (item 2.4 do plano do Ato 1): prévia

**Só prévia.** Nada disto está no jogo. O Nikolas escolhe A ou B (ou pede mudança) antes de virar textura.

| Arquivo | Nome | O que mostra |
|---|---|---|
| `estandarte_opcao_a.svg` | Gota Coroada | Uma gota de sangue grossa dentro de uma coroa fechada de espinhos. A gota vaza e escorre pelo pano. Ponta de baixo rasgada. |
| `estandarte_opcao_b.svg` | Lâmina na Boca | Um cutelo enferrujado, ponta para baixo, dentro de um arco de dentes (boca aberta). Uma gota cai da ponta. Ponta em V. |

- **Elemento:** Sangue (fome, dor, carne, lâminas, espinhos e dentes, como em `docs/estilo-do-genero.md`).
- **Cores:** só os quatro tons do Sangue (`#3A0306` `#7A0A10` `#B3131B` `#E0313A`, de `docs/decisoes/2026-11-08-cores-dos-elementos.md`)
  sobre pano preto. A haste é marrom-escuro neutro, só para dar leitura.
- **Nada da obra:** os dois símbolos fogem da cruz de Sangue da obra (traço vertical com bifurcações) e do pentagrama.
  Na opção B o cutelo **não tem guarda** de propósito, para não formar uma cruz.

## Como ver
Abra o `.svg` no navegador (arrastar o arquivo para uma aba). Para PNG: no Inkscape, *Exportar PNG*,
ou `rsvg-convert -w 400 estandarte_opcao_a.svg -o estandarte_opcao_a.png`.
**Os PNG ainda não foram gerados:** esta mesa não tinha permissão para rodar programas nesta rodada.

## Como foi feito
Desenhado à mão em SVG (texto) pela mesa Modelador em 09/11/2026. Arte própria, sem imagem de terceiros.

## Onde entraria (depois do ok)
O estandarte do jogo tem uma face de 20x40 pixels. Duas saídas possíveis, a decidir com o Nikolas:
1. **Padrão de estandarte** (`banner_pattern` por data pack, textura 64x64 na pasta `textures/entity/banner/`):
   o jogador pode até aplicar no tear. Precisa de ID novo em `docs/ids.md`.
2. **Só textura da estrutura** (estandarte já posto na vila pela estrutura, sem item novo).
Nos dois casos o desenho é reduzido para pixel art no tamanho certo antes de integrar, com nova prévia.
