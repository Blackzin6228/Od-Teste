# Cena de entrada da Vila Calada (mesa lore)

Status: **rascunho para o Nikolas aprovar.** Nada aqui está decidido além do que diz a decisão de 12/11/2026 (cena dentro do jogo, câmera do próprio mod, sem mod de vídeo; WATERMeDIA nunca no servidor).
Aviso: não consegui abrir `docs/decisoes/2026-11-12-cena-entrada-vila-calada.md` (ele está só no branch `mesa-lore/decisoes-0-2`). Escrevi pelo pedido da mesa CEO. Se o arquivo disser outra coisa, ele manda.
Base: `docs/decisoes/2026-11-12-vila-calada-150x150.md`, `...-vila-calada-clima-prioridade.md`, `lore/roteiro.md` (o Pendurado).

## Resumo
- **Duração:** 18 segundos. Sem música. Só som do jogo, grave e lento.
- **Quando:** só na **primeira vez** que cada jogador chega perto da Vila Calada. Depois, nunca mais (salvo comando de admin).
- **Pulável:** sim, a qualquer momento.
- **Texto na tela:** *Vila Calada.* e uma frase curta (2 opções abaixo).
- **Tom:** sério. Nada se mexe sem motivo. Nada de susto de grito.

## Medidas usadas
- **Âncora:** o centro do portão do muro, no chão. É o ponto (0, 0, 0) da vila. O mod já precisa dela para o clima (névoa dentro do muro).
- **Eixos (visto de quem chega pela estrada):** `z` positivo = para dentro da vila; `x` positivo = à direita; `y` = altura em blocos acima do chão da âncora.
- A **estrada de chegada** vem do lado `z` negativo. A **carroça tombada** fica à esquerda dela (`x` negativo). O **campo queimado** fica à direita (`x` positivo).
- Posições abaixo são para a vila de 150x150 (centro de 100x100, coroa de 25). Se a construção mudar, só mudam os números.
- **Proposta (precisa de OK):** a **torre do sino da vila** fica no fundo, à direita: base em (+30, 0, +40), topo em y = 22. O sino (o sino ambiente) fica numa abertura no alto, e é lá que o Pendurado aparece. Se a torre ficar noutro lugar, mantém-se a ideia e trocam-se os números.

## Roteiro plano a plano

| # | Tempo | Duração | Posição da câmera | Altura | Olha para | Movimento | Transição de saída |
|---|-------|---------|-------------------|--------|-----------|-----------|--------------------|
| 1 | 0,0 - 3,0 s | 3,0 s | (0, y, -30), em cima da estrada | 1,7 | O portão (+z), a vila pequena ao fundo | Fica parada; avança só 3 blocos, bem devagar | Corte seco |
| 2 | 3,0 - 7,0 s | 4,0 s | De (-4, y, -22) até (-2, y, -8), ao lado da carroça tombada | 1,2 | Primeiro a carroça (esquerda), depois o portão | Rasteira, lenta, balança de leve como passo | Corte seco |
| 3 | 7,0 - 10,0 s | 3,0 s | De (0, y, -4) até (0, y, +8), atravessando o portão | 2,0 | A praça, de frente (+z) | Avança em linha reta; ao passar o muro a névoa (cinza e vermelha) fica visível | Corte seco |
| 4 | 10,0 - 14,0 s | 4,0 s | De (-25, y, -12) para (-10, y, +4), por cima do muro | 14 | Começa na vila toda; vira devagar para a direita até a torre | Sobe um pouco e gira 40 graus; sem tremor | Corte seco |
| 5 | 14,0 - 16,0 s | 2,0 s | (+16, y, +24), de baixo, perto da torre | 6 | O topo da torre (+30, 22, +40), a abertura do sino | Parada. Zoom lento (campo de visão cai uns 10) | Fade para preto em 0,4 s |
| 6 | 16,0 - 18,0 s | 2,0 s | (preto) | - | - | Texto na tela (ver abaixo) | Fade do preto para o jogador em 0,8 s |

Notas dos planos:
- Os planos 1 a 3 são "a chegada": o que o jogador veria andando, só que mais lento e sem pressa. O 4 mostra o tamanho. O 5 é o quadro. O 6 fecha.
- Depois do plano 3 não há mais ninguém na tela. A vila está parada. Nenhuma criatura visível, nem de longe.
- Horário: o mod pode **forçar a luz da cena** em fim de tarde (cena igual para todos, qualquer hora real do jogo). **Proposta, precisa de OK**: assim a vila sempre parece igual. Só muda a luz para este jogador (efeito de cliente), o mundo não mexe.

## Sons de cada plano (todos vanilla, pitch entre 0,5 e 0,7)
Sem música. Entre os sons, silêncio de verdade. Volumes baixos; o silêncio é o recurso.

| Plano | Sons | Pitch | Observação |
|-------|------|-------|-----------|
| 1 | `ambient.cave` (uma vez, no começo) | 0,6 | Só ar pesado. Nada mais. |
| 2 | `block.wooden_trapdoor.open` (porta que bate no vento, 1 vez, longe); `block.chain.hit` (1 vez, bem baixo) | 0,6 / 0,5 | A madeira da carroça "responde" ao vento. |
| 3 | `block.bell.use` (um toque só, longe, sem origem) | 0,5 | É o sino ambiente. Vem **ao cruzar o muro**. |
| 4 | `ambient.soul_sand_valley.mood` | 0,6 | Longo, quase inaudível. Segura o plano. |
| 5 | `block.chain.hit` (duas batidas lentas) e, na segunda, `entity.warden.heartbeat` (1 batida) | 0,5 / 0,5 | O som do Pendurado. O coração só toca se a opção B de abaixo for aprovada (ver "O quadro"). |
| 6 | Tudo corta. Silêncio total por 1 s. Depois `block.bell.resonate` (volume mínimo) no último segundo do texto | 0,5 | O silêncio entre os dois sons é o susto. |

Ao voltar ao jogo, o som volta ao normal do mundo (o clima da vila toma conta: vento, madeira, passos que não são de ninguém).

## O texto que aparece
Dentro do plano 6, sobre preto. Fonte do jogo, letra branca desbotada (nada de vermelho, nada de tremer). Aparece aos poucos (0,5 s), fica 1 s, some.

1. **Linha 1 (certa):** `Vila Calada.`
2. **Linha 2 (escolher uma; entra 0,6 s depois da primeira):**
   - A: `O sino ainda toca.` (liga ao sino ambiente; avisa sem explicar)
   - B: `Ninguém avisou que vinha.` (a vila "sabe" que o jogador chegou)
   - C: `Quem ficou, ficou calado.` (explica o nome da vila sem entregar o segredo)

Eu recomendo **A**: é a mais curta, serve de dica do sino e não promete nada que o jogo não cumpra.

## O quadro do Pendurado (plano 5)
Imagem: de baixo para cima, a torre cinza contra um céu de fim de tarde. Na abertura do sino, uma **silhueta escura, comprida, de cabeça baixa, pendurada por correntes** que descem do vão. Não tem rosto, nem cor, nem detalhe de roupa. Balança a menos de um bloco, sem vento. O sino ao lado fica parado. Ele **não olha para a câmera**.
Dura **1,5 s** (do segundo 14,3 ao 15,8). Depois do corte para o preto, não aparece mais na cena.

O roteiro diz que o Pendurado só aparece para quem tem a **maior Exposição** (efeito de cliente). Como a cena é feita para todo mundo, duas propostas (o Nikolas escolhe):
- **A (recomendada):** todos veem o Pendurado **só nesta cena**, uma única vez. Fora dela, a regra do roteiro continua igual (só a maior Exposição). Assim o jogador novo aprende que ele existe, e o jogo depois "cobra" o mistério. Sem batida de coração.
- **B:** só aparece se o jogador já tem Exposição acima de zero. Quem chega sem nada vê a torre vazia e o sino parado (o quadro vira "nada"). Mais discreto, mas quem mais precisa do aviso nunca recebe. Com batida de coração no plano 5.
- **C:** a torre fica vazia para todos nesta cena, e o Pendurado só aparece no jogo, de relance. Mais seguro para o clima, mas a cena perde o quadro que pedia.

## Como volta ao jogador
- A cena acontece **por jogador**. Nenhum outro jogador vê.
- O jogador **não é movido**. O mod só troca a câmera de lugar (cliente) e põe o corpo dele parado. Quando termina, a câmera volta para o corpo, no mesmo ponto e com a mesma direção do olhar de antes.
- Fim: fade do preto para o jogo em 0,8 s. Nenhuma mensagem no chat. Nada além do texto da cena.
- **Proteção (proposta, precisa de OK):** durante a cena o jogador fica **invulnerável**. A cena **só começa** se não houver monstro a menos de 20 blocos e se ele não estiver em combate; se houver, espera até o jogador ficar tranquilo.
- **Em grupo:** cada jogador tem a sua cena na sua primeira vez, sem esperar os outros. Quem já viu entra direto.

## Quando dispara
- **Gatilho (proposta):** o jogador entra pela **primeira vez** na área da coroa da vila, na altura da estrada (25 blocos antes do portão). Não dispara dentro do muro nem pelo lado de trás (a trilha da floresta): se ele chegar por outro lado, a cena dispara ao cruzar o portão, com os planos 1 e 2 cortados e o resto igual (cerca de 11 s).
- O mod guarda um **sinal por jogador** (já visto / não visto). Isso é dado salvo: **precisa entrar em `docs/ids.md`** antes da implementação. Não decidi o nome do dado.
- Comando de admin para **rever** a cena (para teste e para jogador novo no mundo).
- Se um jogador morrer e voltar, a cena **não** se repete.

## Como pular
- Mostra, bem fraco, no canto de baixo: `Segure [Espaço] para pular`. (Proposta de tecla; precisa de OK do Dev.)
- **Segurando por 1 s** a cena corta direto para o fade-out e volta ao jogador. O sinal de "já visto" **conta** como visto: pular não repete a cena.
- Qualquer outro botão ou ESC **não** pula, para evitar sem querer. ESC abre o menu e a cena pausa no cliente (efeito do jogo).
- Quem pula perde só o quadro do Pendurado e o texto. Nada do enigma. A vila inteira continua igual.

## O que a cena NÃO faz
- Não dá item, nem pista, nem avanço. É só clima.
- Não mostra o Magarefe, nem nenhuma criatura de Sangue, nem a Convidada. A vila "ainda não sabe" do jogador.
- Não usa vídeo, mod de vídeo, nem arquivo de som novo. Só câmera do mod e sons do jogo.
- Não usa símbolo, criatura, nome ou texto de outra obra. Os nomes (Vila Calada, o Pendurado) são nossos.

## Perguntas para o Nikolas
1. Duração de 18 s e 6 planos: está bom, ou prefere mais curta (12 s)?
2. Segunda frase do texto: A, B ou C?
3. O Pendurado na cena: A, B ou C?
4. A torre do sino da vila fica no fundo à direita. Aprova a ideia de uma torre alta com o sino no alto?
5. Luz de fim de tarde forçada só na cena: aprova?
6. Jogador invulnerável durante a cena e cena adiada se tem monstro por perto: aprova?
7. Pular com Espaço segurado por 1 s: ok?

## Para o Dev (resumo)
- Câmera do mod no cliente, com pontos e tempos desta tabela. Sem mod de vídeo.
- Sinal "já visto" por jogador (dado novo: pedir registro em `docs/ids.md`).
- Gatilho por área, comando de admin para rever.
- Sons: só vanilla da tabela. Se um nome de som não existir na 1.21.1, trocar por um da mesma família e avisar a Lore.
