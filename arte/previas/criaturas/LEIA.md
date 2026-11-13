# Criaturas de Sangue: prévia em qualidade final, versão 5.2 (Retalhado, Sanguessuga-Mãe e Magarefe)

Status: **só prévia, para o Nikolas aprovar.** Nada disto está no jogo: o jogo continua com o modelo provisório
em `src/main/resources/assets/odteste/`. Nenhum ID mudou (`odteste:retalhado`, `odteste:sanguessuga_mae`).
Base: `lore/criaturas.md`, `docs/decisoes/2026-11-08-cores-dos-elementos.md`, `docs/regua-de-qualidade.md`.

**Versão 5.2** = revisão crítica da 5.1 (silhueta, proporção, textura, variação, animações com peso, horror sério).
A lista do que estava abaixo do nível e o que mudou, com as folhas lado a lado provisório x 5.1 x 5.2, está em
`comparacao/LEIA.md` e `comparacao/<criatura>.png`.

## Como regenerar
Na raiz do repositório: `bash ferramentas/criaturas_sangue.sh [retalhado|sanguessuga_mae|magarefe|todas]`
(só python3 + numpy; sem Blockbench, sem Pillow). O script monta a geometria, pinta a textura pixel a pixel (variantes
A e B), escreve as animações e o `.bbmodel`, e renderiza as prévias. Depois, `bash ferramentas/comparar_criaturas_sangue.sh`
refaz as folhas de comparação. Para renderizar qualquer outro modelo GeckoLib:
`bash ferramentas/render_geckolib.sh --geo ... --textura ... --saida ...`.

Código-fonte em `fonte/`: `montagem.py` (cubos, ossos, UV, costura em retalhos, `.bbmodel`), um módulo por criatura
(`retalhado.py`, `sanguessuga_mae.py`, `magarefe.py`, cada um com `montar()` e `anims()`), `gerar_criaturas_sangue.py`
(orquestra) e `comparacao.py` (folhas).

## Arquivos por criatura (`<criatura>/`)
| Arquivo | O que é |
|---|---|
| `<criatura>.geo.json` | Modelo GeckoLib (Bedrock 1.12). Vai para `assets/odteste/geo/entity/` na integração. |
| `<criatura>.animation.json` | Animações GeckoLib. Mantém os nomes que o código já usa (`parado`, `andar`, `investida` / `lancar`) e acrescenta `ataque`/`mordida`, `dano`, `morrer` e a animação de presença. |
| `<criatura>.png` (+ `_glowmask.png`) | Textura variante A, densidade padrão do Minecraft (16 px por bloco). O glowmask só existe no Retalhado (miolo do coração). |
| `<criatura>_b.png` | Textura **variante B** (mesmo modelo, outros retalhos/tons), para o Dev sortear por criatura. |
| `textura_ampliada.png` | A textura A 4x maior, para conferir pixel a pixel. |
| `frente.png`, `lado_direito.png`, `costas.png`, `3_4.png`, `vistas.png` | Renders (1 traço da régua = 1 bloco). |
| `silhueta.png` | Teste da régua: tudo preto em fundo claro. |
| `variantes.png` | A x B, frente e 3/4. |
| `anim_<nome>.png` | 8 quadros de cada animação, em 3/4 (linha de cima) e de frente (linha de baixo). |

O arquivo fonte para o Blockbench fica em `arte/blockbench/<criatura>.bbmodel` (textura A embutida, animações incluídas).

## Retalhado (45 cubos, 15 ossos, ~3,1 blocos de altura; textura 160x160)
- Bruto curvado e **assimétrico**: ombro direito (do cutelo) maior e mais alto, esquerdo caído com um retalho de pele
  pendurado; corcova grande com três vértebras saltadas; cabeça afundada e torta entre os ombros; perna esquerda
  virada para fora (manca). Braço esquerdo mais comprido e fino, direito grosso.
- Pele em **retalhos de três pessoas** (pálida, roxa de hematoma, curtida), costura só na fronteira dos retalhos,
  sangue seco junto dela. O vermelho do Sangue aparece só no **coração exposto** (com brilho no miolo, entre duas
  costelas quebradas), na borda rasgada da cavidade, nos olhos costurados e no sangue seco do cutelo: **um acento**.
- **Cutelo** grande e lascado preso ao antebraço direito por tiras; **três ganchos em "J"** com elos de corrente na mão esquerda.
- Olhos costurados em X, rasgo no lugar do nariz, mandíbula caída e torta com três dentes.
- Animações: `parado` (lub-dub a ~45 bpm, fareja em sacudidas, boca abre e fecha), `andar` (tronco cai no contato
  do pé, braços e ganchos atrasados), `ataque` (0,45 s de antecipação com o corpo inteiro, corte, overshoot),
  `investida` (agacha, lança os ganchos, crava, puxa), `dano`, `morrer` (joelho esquerdo cede antes, cai de cara com
  quique, coração acelera e para), presença **`afiar`**.

## Sanguessuga-Mãe (56 cubos, 23 ossos, ~2,1 blocos de altura x 3,9 de largura com as pernas; textura 160x128)
- Corpo de **cesta de carne** em três camadas de larguras diferentes, **empinado**: a boca da barriga (lábio de carne
  saliente, garganta escura, dois anéis de 4 dentes) aparece de frente e de 3/4. Quatro **pernas de espinho** em três
  segmentos (coxa, canela, tarso) com joelho de espinho acima do corpo; espinhos nas laterais e no rebordo.
- **Seis larvas** (corpo anelado, cabeça escura, linha molhada) meio enterradas na carne do topo: o acento vermelho.
- Animações: `parado` (respira, larvas se mexem, dentes batem, uma perna muda de lugar), `andar` (pares em diagonal,
  a perna sobe de verdade, corpo afunda no apoio), `mordida` (ergue 0,4 s, desaba, anéis fecham um depois do outro),
  `lancar` (Chuva de Filhotes: empina, chicoteia, as larvas voam e somem, voltam a crescer), `dano`, `morrer` (pernas
  da frente cedem primeiro, cesta cai e quica, tomba de lado, duas pernas ainda se mexem, larvas fogem),
  presença **`chocar`**.

## Magarefe (37 cubos, 15 ossos, ~3,8 blocos de altura; ainda não existe no jogo, sem ID; textura 128x128)
- Gente esticada, não um bruto: alto e magro, ombro do cutelo caído, tronco pendendo para esse lado, **cabeça num saco
  de estopa** torta e caída com duas pontas amarradas em cima, sem rosto. Camisa escura com dobras, **avental de couro**
  gasto do peito à canela com o **sangue escorrido** (o acento) e abas rasgadas, braços nus com cortes antigos, botas
  pesadas. **Cutelo de cabo** na mão direita arrastando perto do chão; mão esquerda de dedos compridos;
  **Sino de Matadouro** no cinto; dois ganchos de carne pendurados atrás.
- Animações: `parado` (a cabeça pende e dá um estalo seco), `andar` (1,8 s, arrastado, braço do cutelo atrasado),
  `ataque` (passo à frente, 0,7 s de antecipação, desce de uma vez e segura embaixo), `dano` (quase não sente),
  `morrer` (guardada para depois do Ato 1), presença **`escutar`**.

## Para o Nikolas decidir
1. Aprovar o visual das três na 5.2 (ou dizer o que muda), antes de integrar. Folhas: `comparacao/`.
2. **Resolução**: densidade padrão (16 px por bloco). Se preferir 32x, o gerador troca com um número.
3. **Hitbox** sugerida ao Dev: Retalhado 1,9 x 3,1 (hoje 1,5 x 3,0); Sanguessuga-Mãe 2,8 x 2,1 (hoje 2,6 x 1,9);
   Magarefe 0,9 x 3,8.
4. Nomes das animações novas (`ataque`, `mordida`, `dano`, `morrer`, `afiar`, `chocar`, `escutar`) e as texturas
   variante B para o Dev ligar no código.
5. O Faminto fica para a próxima rodada (já tem prévia própria em `arte/vila-calada/`, esperando aprovação).
6. Magarefe: o ID (`odteste:magarefe`) só entra em `docs/ids.md` quando o Dev criar a criatura.
