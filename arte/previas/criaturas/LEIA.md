# Criaturas de Sangue: prévia em qualidade final (Retalhado e Sanguessuga-Mãe)

Status: **só prévia, para o Nikolas aprovar.** Nada disto está no jogo: o jogo continua com o modelo provisório
em `src/main/resources/assets/odteste/`. Nenhum ID mudou (`odteste:retalhado`, `odteste:sanguessuga_mae`).
Base: `lore/criaturas.md`, `docs/decisoes/2026-11-08-cores-dos-elementos.md`, `docs/regua-de-qualidade.md`.

## Como regenerar
Na raiz do repositório: `bash ferramentas/criaturas_sangue.sh` (só python3 + numpy; sem Blockbench, sem Pillow).
O script monta a geometria, pinta a textura pixel a pixel, escreve as animações e o `.bbmodel`, e renderiza as prévias.
Para renderizar qualquer outro modelo GeckoLib: `bash ferramentas/render_geckolib.sh --geo ... --textura ... --saida ...`.

## Arquivos por criatura (`<criatura>/`)
| Arquivo | O que é |
|---|---|
| `<criatura>.geo.json` | Modelo GeckoLib (Bedrock 1.12). Vai para `assets/odteste/geo/entity/` na integração. |
| `<criatura>.animation.json` | Animações GeckoLib. Mantém os nomes que o código já usa (`parado`, `andar`, `investida` / `lancar`) e acrescenta `ataque`/`mordida`, `dano`, `morrer` e a animação de presença. |
| `<criatura>.png` (+ `_glowmask.png`) | Textura 128x128 na densidade padrão do Minecraft (16 px por bloco). O glowmask só existe no Retalhado (miolo do coração). |
| `textura_ampliada.png` | A textura 4x maior, para conferir pixel a pixel. |
| `frente.png`, `lado_direito.png`, `costas.png`, `3_4.png`, `vistas.png` | Renders (1 traço da régua = 1 bloco). |
| `silhueta.png` | Teste da régua: tudo preto em fundo claro. |
| `anim_<nome>.png` | 8 quadros de cada animação, em 3/4 (linha de cima) e de frente (linha de baixo). |
| `comparacao.png` | ANTES (provisório que está no jogo) x DEPOIS (proposta), mesma escala. |

`comparacao_sangue.png` junta as duas folhas. O arquivo fonte para o Blockbench fica em `arte/blockbench/<criatura>.bbmodel`
(textura embutida, animações incluídas). `fonte/` tem o código que gera tudo.

## Retalhado (33 cubos, 14 ossos, 3 blocos de altura)
- Bruto curvado: corcova acima da cabeça, cabeça baixa entre os ombros, braços até o joelho, joelhos dobrados.
- Pele morta em **retalhos** costurados (tons neutros cinza-carne). O vermelho do Sangue aparece só no **coração exposto**
  (com brilho no miolo), nas fendas das costuras, nos olhos costurados e no sangue seco do cutelo: **um acento**.
- **Cutelo** preso ao antebraço direito por tiras de couro; **três ganchos** em correntes na mão esquerda (para a Investida).
- Olhos costurados e sem pupila: "cego nas costas" e o Sangue come os olhos primeiro. Mandíbula caída com dentes.
- Animações: `parado` (respira, olha em volta, coração bate a 45 bpm), `andar` (passo pesado, ganchos balançam),
  `ataque` (cutelo: 0,4 s de antecipação, corte, continuação), `investida` (Investida de Ganchos: puxa, lança, crava, arrasta),
  `dano`, `morrer` (joelhos cedem, cai de cara sobre o cutelo, o coração acelera e para), e a presença **`afiar`**:
  afia o cutelo nos próprios ganchos, levanta a cabeça cega e fareja.

## Sanguessuga-Mãe (42 cubos, 19 ossos, ~2 blocos de altura x 3 de largura)
- **Cesta** de carne trançada (dois quadrados a 45 graus por camada = silhueta redonda com pontas), quatro **pernas de espinho**
  com os joelhos acima do corpo, quatro espinhos nas laterais.
- **Boca circular na barriga**: anel de carne viva, garganta escura e dois anéis de 4 dentes que fecham alternados.
- **Seis filhotes** (larvas cheias de sangue) grudados nas costas: são o acento vermelho da peça.
- Animações: `parado` (cesta respira, larvas se mexem, dentes batem), `andar` (pares em diagonal), `mordida` (ergue e desaba,
  dentes fecham), `lancar` (Chuva de Filhotes: empina, chicoteia, as larvas voam e somem, voltam a crescer),
  `dano`, `morrer` (pernas abrem e cedem, a cesta tomba, as larvas fogem), e a presença **`chocar`**: abaixa até o chão,
  três pulsos pesados, as larvas rodam e uma desce até a boca.

## Para o Nikolas decidir
1. Aprovar o visual das duas (ou dizer o que muda), antes de integrar.
2. **Resolução**: ficou na densidade padrão (16 px por bloco), em folhas de 128x128. Se preferir 32x, o gerador troca com um número.
3. **Hitbox**: o Retalhado tem ombros de ~2,1 blocos (hitbox atual 1,5) e a Sanguessuga ~3,2 blocos com as pernas
   (hitbox 2,6 x 1,9). Sugestão para o Dev: 1,9 x 3,0 e 3,0 x 2,0.
4. Nomes das animações novas (`ataque`, `mordida`, `dano`, `morrer`, `afiar`, `chocar`) para o Dev ligar no código.
5. Magarefe e Faminto ficam para a próxima rodada (o Faminto já tem prévia em `arte/vila-calada/`).
