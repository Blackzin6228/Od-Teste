# Vila Calada: prévias do Faminto e do atril

Status: **só prévia, para o Nikolas aprovar (M1).** Nada disso está no jogo. Não tem ID novo nem arquivo em `src/`.
Base: `lore/fichas-estruturas.md` (Vila Calada, branch `mesa-lore/vila-destruida`) e `docs/estilo-do-genero.md`.

## Arquivos

| Arquivo | O que é |
|---|---|
| `previas/faminto_previa.svg` | Desenho do Faminto de frente, lado e costas, na escala do modelo. Abre no navegador ou no GitHub. |
| `previas/atril_previa.svg` | Desenho do atril de frente, lado e costas, mais o detalhe das páginas do livro (pixel a pixel). |
| `faminto.bbmodel` | Modelo do Faminto no formato GeckoLib (criatura animada). 13 cubos, ossos prontos para animar (cabeça, mandíbula, braços, pernas). Textura 64x64. |
| `atril_sineiro.bbmodel` | Modelo do atril no formato de bloco Java do Minecraft (1 bloco). Duas texturas 16x16: madeira e livro. |
| `fonte/gerar_png.py` | Script que pinta as texturas, coloca dentro dos `.bbmodel` e faz o render PNG (frente, lado, costas, 3/4). |

### Por que SVG e não PNG (ainda)
Nesta sessão não deu para rodar Python nem outro programa (o ambiente só liberou git). Então:
- as prévias para aprovar estão em **SVG** (desenho feito à mão com as mesmas medidas do modelo);
- as texturas PNG e o render PNG saem do script. Para gerar, na raiz do repositório:
  `python3 arte/vila-calada/fonte/gerar_png.py`
  Cria `texturas/faminto.png`, `texturas/atril_madeira.png`, `texturas/atril_livro.png`, `previas/faminto_render.png`,
  `previas/atril_render.png` e embute as texturas nos `.bbmodel`. O script ainda **não foi testado**; a próxima sessão
  do Modelador roda, confere e manda os PNG no PR.
- Até lá, o Blockbench abre os `.bbmodel`, mas **sem textura** (ele procura os arquivos em `texturas/`).

## O Faminto (Sangue)
- Aldeão curvado para a frente (corpo inclinado 30 graus), pele esticada cinza-amarelada, manto queimado e rasgado.
- **Sem olhos**: só as órbitas fundas (o Sangue come os olhos primeiro).
- **Boca aberta demais**: a mandíbula cai solta até o peito. É o único vermelho da peça (o acento).
- Braços longos, mãos de dedos compridos quase no chão. No rasgo do manto aparecem as costelas na frente e a espinha atrás.
- Altura 31 pixels (quase 2 blocos, como um aldeão). Poucas partículas, como pede a ficha.
- O nariz de aldeão ficou de propósito, murcho: é o que diz que ele "já foi gente da vila".
- Criado do zero, sem copiar criatura da obra (não é o Zumbi de Sangue).

## O atril do Sineiro
- Bloco de madeira carbonizada: base, poste, tampo inclinado 22,5 graus e uma borda para o livro não escorregar.
- Em cima, o **Livro do Sineiro** aberto. Na página direita, um **sino rachado** desenhado a tinta (símbolo nosso).
- **Fita marcadora vermelha** pendurada na frente: o acento da peça. Nada de Cruz de Sangue nem símbolo da obra.
- A frente (lado baixo do tampo) é o lado de quem lê.

## O que falta decidir (Nikolas)
1. **Aprovar o visual** do Faminto e do atril (ou dizer o que muda).
2. **Nome final** do Faminto (hoje é provisório, na ficha da lore).
3. **Resolução das texturas**: ainda pendente em `docs/decisoes.md`. As prévias usam **16x** (padrão do Minecraft), como manda `docs/regua-de-qualidade.md`.
4. **O atril é um bloco novo do mod** (com ID para sempre, por exemplo `odteste:atril_sineiro`) **ou só a textura do atril vanilla** (lectern)
   trocada na estrutura? Bloco novo permite o toque do sino ao abrir o livro sem mexer no vanilla; a decisão técnica é da mesa Dev.
5. O **martelo do ferreiro** ao lado do atril e o **estandarte da Cruz de Ferro** ficam para uma próxima prévia.

## Depois de aprovar (não feito aqui)
- Animações do Faminto: parado de frente para a parede, andar arrastado, acordar (a mandíbula balança).
- Rodar o script, revisar as texturas pixel a pixel, e só então a integração em `src/main/resources/assets/odteste/` (outra etapa, com ID em `docs/ids.md`).
