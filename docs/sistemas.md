# Sistemas do jogador

Desenho de **Exposição, Lucidez, Marca, Vínculo e Transcender**. Ponto de partida: o que o Máscaras fazia
(`docs/referencia-mascaras.md`), com o que funcionou mantido e o resto melhorado.
As animações, efeitos de tela, partículas e sons de cada sistema estão em `docs/animacoes.md`.

> **Status:** proposta da mesa Dev (07/11/2026, revisada em 08/11/2026). O que está em **[DECIDIR]** precisa do Nikolas.
> Já está no código: os valores salvos, a faixa de cada um, a sincronização com o cliente e os comandos de admin.
> **Play test de Sangue, parte 1 (08/11/2026):** ganho automático de Exposição, Lucidez com efeito, surto,
> Marca que aparece sozinha e a Marca de Sangue visível entraram no código, usando as **propostas** deste
> documento onde ainda há [DECIDIR]. Detalhe e números na seção "No código (play test de Sangue)" no fim.
>
> **Nome do mundo paranormal:** enquanto a Lore não propõe um nome nosso, este documento diz só "o paranormal".

## Regras que valem para todos
1. **Nenhum número na tela.** O jogador sente pelos efeitos: som, partícula, névoa, texto curto em palavras,
   o visual da armadura e da skin. Números só para admin (comandos).
2. **Tudo dentro do Minecraft.** Efeitos com o que o jogo e o pack já têm: sons vanilla com pitch baixo (0,5–0,7; o jogo não toca abaixo de 0,5),
   partículas, animação do corpo (Player Animator), efeitos de poção curtos (Escuridão, Náusea, Lentidão),
   névoa do cliente, mensagens na barra de ação.
3. **Sem lag.** Nada roda a cada tick. Checagens a cada 1–2 segundos por jogador, só no próprio jogador.
   Efeitos visuais (sussurro, vulto, partícula falsa) rodam **no cliente**: o servidor não paga nada.
4. **O preço vem junto com o poder.** Todo degrau que dá algo também tira algo.
5. **Os dados não se perdem.** Morrer não zera nada. Dado salvo errado (arquivo velho) é corrigido ao carregar, não crasha.
6. **Comportamento, não estatística.** Cada degrau precisa de uma coisa que se nota no jogo e que dá roleplay.

---

## 1. Exposição (0 a 100)
Quanto o paranormal já tocou você. É a "experiência" paranormal: **só sobe** (como no Máscaras 1.32).

**Sobe com** (cada fonte sobe pouco, e repetir a mesma coisa rende menos):
- ver uma criatura paranormal pela primeira vez; matar uma;
- fazer um rito; usar uma relíquia; ler uma Página;
- passar tempo numa dimensão nova (quando existir) ou num lugar marcado pelo paranormal.

**Não desce.** Nem com o tempo, nem com morte. **[DECIDIR]** Existe algum jeito raro de baixar
(um rito caro, um evento do narrador)? Proposta: só pelo narrador.

**Degraus** (o jogador nunca vê o número, só o nome do degrau no Dossiê, em palavras):

| Faixa | Degrau | O que muda no jogo |
|---|---|---|
| 0–19 | **Comum** | Nada. Itens paranormais mostram descrição em glifos (não entende). |
| 20–39 | **Tocado** | Começa a entender as descrições dos itens. Às vezes ouve um som que os outros não ouvem. A primeira Marca pode aparecer. |
| 40–59 | **Marcado** | Criaturas paranormais percebem você de mais longe. Pode fazer Vínculo. Ritos básicos funcionam. |
| 60–79 | **Fundo** | Ritos avançados. À noite, a névoa chega mais perto de você. Pode Transcender. |
| 80–99 | **À beira** | Relíquias respondem melhor na sua mão. O paranormal "chama": vultos no canto da visão. |
| 100 | **Atravessado** | Só por evento do narrador ou comando (como a Noite da Costura). Fim de jogo: ver Transcender nível 4. |

- Subir de degrau é **um momento**: som grave para você e um eco baixo para quem está perto (até 32 blocos),
  e uma frase curta na barra de ação ("Alguma coisa olhou de volta.").
- **[DECIDIR]** No Máscaras, 99 → 100 só no evento. Manter igual?
- **[DECIDIR]** Cada degrau tira Lucidez máxima para sempre (ex.: −5 por degrau a partir de Marcado)?
  Proposta: **não**. Quem cobra Lucidez máxima é só o Transcender, para o preço ficar claro e ser escolha.

## 2. Lucidez (0 até a Lucidez máxima)
O quanto você ainda está são. Cai rápido e volta devagar. É o "medo" do personagem.

**Cai com:**
- ver criaturas paranormais de perto; ficar muito tempo no escuro total longe de outros jogadores;
- pagar um rito ou a magia do elemento (tecla de afinidade, como a tecla H do Máscaras);
- eventos do narrador.

**Volta com:**
- dormir; ficar perto de luz forte e de outros jogadores (o grupo acalma — bom para roleplay);
- comida quente (ensopados), itens próprios (ex.: uma vela que segura a Lucidez de quem está perto).

**O que se sente** (em fração da máxima, para funcionar igual para quem transcendeu):

| Faixa | Estado | Efeito (tudo no cliente, nada de número) |
|---|---|---|
| acima de 70% | **Firme** | Nada. |
| 40–70% | **Inquieto** | Sussurros raros (sons vanilla graves). A tela "respira" de leve no escuro. |
| 15–40% | **Abalado** | Passos atrás de você, partículas que só você vê, a mira treme de leve ao mirar com arco. |
| 1–15% | **Quebrando** | Vultos falsos no canto da visão (somem ao olhar), Escuridão curta de vez em quando, sons de criatura falsos. |
| 0 | **Surto** | **[DECIDIR]** Proposta: desmaio de 5 segundos (Cegueira + Lentidão forte), a Lucidez volta para 15% e a Exposição sobe um pouco. Alternativa: uma criatura de verdade aparece perto. |

- **[DECIDIR]** A Lucidez também cai devagar com o tempo quando a Exposição é alta? Proposta: só a partir de "À beira".
- O cliente recebe a Lucidez e a máxima só para escolher o efeito. **Os outros jogadores não recebem** (ninguém "lê" o outro sem um item, como o Olho de Vidro do Máscaras).

## 3. Marca (elemento + estágio 1 a 4)
O elemento que marcou o seu corpo: **Sangue, Conhecimento, Morte ou Energia** (ou nenhum).
É **visível**: a skin ganha uma camada por estágio (pouco detalhe certeiro, como a skin do Vínculo com o
Conhecimento que o Nikolas aprovou) e a **armadura muda com a Marca de quem veste** (decisão de 07/11/2026).

**O jogador não escolhe a Marca** (decisão do Nikolas, 08/11/2026). Não existe menu, item, rito ou comando de
jogador para escolher o elemento. A Marca aparece sozinha pelo que acontece com ele:

- O jogo conta, em segredo, o **contato com cada elemento**: Exposição ganha com relíquias, criaturas, ritos,
  Páginas e lugares de um elemento. Cada fonte soma no elemento dela.
- Quando a Exposição chega em **"Tocado"**, o elemento com mais contato deixa a Marca de **estágio 1**.
  Empate: espera o próximo contato desempatar (a Marca não aparece no sorteio).
- Até o Vínculo, se outro elemento passar bem à frente (proposta: o dobro do contato do atual), a Marca **troca
  sozinha** para ele, com aviso (ver `docs/animacoes.md`).
- O jogador influencia só **pelo que faz** (com o que anda, o que caça, onde vai), nunca por uma escolha direta.
- O comando `/marca definir` continua existindo, mas é **só para admin e evento do narrador** (permissão 2).

**Como sobe de estágio:** com Exposição + feitos do próprio elemento. Cada estágio tem um mínimo de Exposição:

| Estágio | Nome (provisório) | Pede | Ganha | Paga |
|---|---|---|---|---|
| 1 | **Sinal** | Tocado | Armadura e skin com um detalhe do elemento. Itens do elemento "reconhecem" você (texto novo). | Criaturas do elemento que vence o seu notam você mais. |
| 2 | **Vínculo** | Marcado + rito | Ver seção 4. | O elemento fica fixo (não troca mais sozinho). |
| 3 | **Cicatriz** | Fundo | Armas do elemento crescem mais na sua mão (como o +dano por estágio do Máscaras). Magia do elemento mais barata. | Fraqueza clara contra o elemento que vence o seu. |
| 4 | **Entrega** | À beira | Relíquia do seu elemento desperta na sua mão. Visual completo. | Comportamento do elemento (ver abaixo). |

**O que cada elemento cobra no estágio 4** (ideias, **[DECIDIR]**):
- **Sangue:** fome que não para (a barra de fome cai mais rápido); mais força quando ferido.
- **Conhecimento:** lê o que outros não leem; esquece (às vezes uma Página "some" do inventário para o Dossiê).
- **Morte:** o tempo passa diferente — cura devagar, mas resiste mais; animais se afastam.
- **Energia:** sorte e azar fortes (efeitos aleatórios curtos); máquinas perto falham às vezes.

Ciclo: **Sangue vence Conhecimento, que vence Energia, que vence Morte, que vence Sangue** (já no código).

## 4. Vínculo
No Máscaras, o Vínculo era um pacto separado com o elemento. **Proposta:** o Vínculo passa a ser o
**estágio 2 da Marca** — o momento em que você **aceita**, num rito, a Marca que já tem. Fica um sistema só,
mais fácil de entender e sem dado extra salvo. **[DECIDIR]** Aceita juntar Vínculo e Marca assim?

- O rito **não deixa escolher** o elemento: ele só fixa o que já marcou você. Sem Marca, o rito não funciona.
- **Ganha:** a magia do elemento sem grimório (tecla de afinidade, pagando Lucidez); armas do mesmo elemento
  ×1,2 (como no Máscaras); grimório do seu elemento gasta menos mana, o do elemento que vence o seu gasta mais
  (integração com Iron's, em `compat/`).
- **Paga:** o elemento fica fixo. Trocar só por evento do narrador.
- Antes do Vínculo, a Marca pode **trocar sozinha** de elemento se outro elemento dominar o seu contato.

## 5. Transcender (nível 0 a 4)
Ir mais fundo de propósito. É **escolha** do jogador, num rito, e o preço é **para sempre**.
(A escolha é **ir mais fundo** ou não; o elemento continua sendo o da Marca, que o jogador não escolhe.)

**Pede:** Vínculo (Marca estágio 2+), Exposição mínima (proposta: 60 + 10 por nível, como no Máscaras),
um componente do elemento e XP.

| Nível | Ganha | Paga |
|---|---|---|
| 1 | Ritos e magia do elemento mais fortes; mais chance de o rito não gastar o componente. | Lucidez máxima −15. |
| 2 | O anterior, mais forte. | Lucidez máxima −15. À noite, coisas estranhas perto de você (sons, portas que abrem). |
| 3 | Habilidade única do elemento **[DECIDIR qual]**. | Lucidez máxima −15. Criaturas aparecem perto de você às vezes. |
| 4 | **Fim de jogo** (como a Relíquia Viva do Máscaras): vira parte do paranormal, com um sinal no nome. | Lucidez máxima −15. A Lucidez não volta sozinha. |

- Com os números propostos, nível 4 deixa a Lucidez máxima em 40. O código tem um piso de 10 (nunca zera).
- **[DECIDIR]** O nível 4 é a mesma coisa que Exposição 100 ("Atravessado"), ou são caminhos separados?
- **[DECIDIR]** Transcender pode ser desfeito? Proposta: **não**, só pelo narrador.
- Hoje o comando `/transcender definir` **não** mexe na Lucidez máxima (o admin ajusta com `/lucidez maxima`).
  Quando o rito existir, ele cobra as duas coisas juntas.

---

## O que já está no código
| O quê | Onde |
|---|---|
| 4 elementos + nenhum, com cor e ciclo de vantagem | `elementos/Elemento.java` |
| Faixas de cada valor | `dados/Limites.java` (testado em `LimitesTest`) |
| Dados salvos no jogador, que sobrevivem à morte | `dados/DadosJogador.java` (IDs em `docs/ids.md`) |
| Cópia para o cliente do próprio jogador (ao entrar, renascer, trocar de dimensão e quando muda) | `rede/` |
| Comandos de admin (permissão 2) | `comandos/ComandosAdmin.java` |

Não há nenhum caminho para o jogador escolher a Marca: ela vem do contato (abaixo) ou do comando de admin.

## No código (play test de Sangue, parte 1 — 08/11/2026)
Tudo em `sistemas/` (servidor) e `cliente/` (efeitos). As contas estão em classes `Regras*`, testadas em `RegrasTest`.
Os números são **proposta para o play test**: mudam num lugar só.

**Exposição sobe sozinha** (`Vigia`, checagem por jogador a cada 2 s, espalhada pelos ticks):
| Gatilho | Quanto (1ª vez) |
|---|---|
| 60 s seguidos no escuro total (luz 0), sem outro jogador a 24 blocos | 0,5 |
| Morrer | 2 |
| Mob hostil a até 8 blocos, à noite (no máximo a cada 30 s) | 0,25 |
| Surto | 3 |
- Ganho em centésimos; o resto fica salvo (`odteste:exposicao_resto`) até completar 1 ponto.
- Repetir a mesma fonte em 10 minutos rende menos (1ª inteira, 2ª metade, 3ª um terço...).
- O ganho automático para em 99: 100 só por evento/comando.
- **Ponto de entrada** para o conteúdo: `Exposicao.ganhar(jogador, Fonte.CRIATURA_VISTA, Elemento.SANGUE)`.
  Com elemento, soma no contato (`odteste:contato_elemento`). Fontes prontas: `criatura_vista`, `criatura_morta`,
  `reliquia`, `rito`, `pagina`, `lugar`. Para testar sem conteúdo: `/exposicao fonte <jogador> <fonte> [elemento]`.

**Lucidez** (`Lucidez.perder/recuperar` são o ponto de entrada):
- Cai: escuro total sozinho, depois de 30 s, 1 a cada 6 s (de 100 a 0 em uns 10 minutos).
- Volta: luz forte (12+) com outro jogador a até 8 blocos, +1 a cada 10 s; ensopado/sopa +8; dormir a noite toda +metade da máxima.
- Efeitos por faixa no cliente (tabela da seção 2 e `docs/animacoes.md`): vinheta que respira, sussurros e passos
  falsos, partículas falsas, mira tremendo no arco, tela inclinada, vultos, escuridão curta, sons de criatura e coração.
- **Surto** (proposta "desmaio"): Cegueira + Lentidão IV por 5 s, a Lucidez volta para 15% da máxima e a Exposição sobe 3.
  Quem está perto vê o corpo (animação) e ouve. Vale até para `/lucidez definir <jogador> 0`.
- Ainda **não**: a Lucidez cair sozinha com Exposição alta ([DECIDIR] 4) e a queda por ver criatura (não há criatura).

**Marca:**
- Aparece sozinha (estágio 1) quando o jogador fica Tocado e um elemento lidera o contato (empate espera).
- Troca sozinha só no estágio 1, se outro elemento tiver o dobro do contato **e** pelo menos 3 pontos de contato
  (protege a Marca dada pelo narrador, que começa sem contato).
- Estágios 2 a 4 só por comando por enquanto (o rito do Vínculo ainda não existe).
- **Marca de Sangue** (`RegrasSangue`, proposta a partir do estágio 4 "mais força quando ferido; fome que não para"):

| Estágio | Ganha (com menos de metade da vida) | Paga | Visível |
|---|---|---|---|
| 1 | +0,5 de dano | — | 1 partícula vermelha a cada 2 s |
| 2 | +1 de dano | — | 2 |
| 3 | +1,5 de dano | fome um pouco mais rápida | 3 |
| 4 | +2,5 de dano | fome bem mais rápida (1 ponto a cada ~40 s) | 4 |

As partículas são vistas por todos por perto (o servidor manda poucas, a cada 2 s). Os outros elementos já têm
o momento (cor, som, partícula), mas ainda **não** têm efeito no corpo.

**Comandos (só admin, permissão 2):**
- `/exposicao ver <jogador>` · `/exposicao definir <jogador> <0-100>` · `/exposicao fonte <jogador> <fonte> [elemento]`
  (simula uma fonte pelo mesmo caminho do conteúdo; com elemento, soma contato e pode fazer a Marca aparecer)
- `/lucidez ver <jogador>` · `/lucidez definir <jogador> <0-100>` (presa na máxima) · `/lucidez maxima <jogador> <10-100>`
- `/marca ver <jogador>` · `/marca definir <jogador> <elemento> <1-4>` · `/marca definir <jogador> nenhum` (tira a Marca).
  Com elemento, **o estágio é obrigatório**: sem ele, o comando dá erro e não salva nada. Estágio fora de 1–4 é barrado.
- `/transcender ver <jogador>` · `/transcender definir <jogador> <0-4>`

## Resumo do que precisa do Nikolas
1. Exposição: existe jeito de baixar? 99 → 100 só por evento?
2. Exposição tira Lucidez máxima por degrau, ou só o Transcender cobra?
3. O que acontece com Lucidez 0 (desmaio ou criatura).
4. A Lucidez cai sozinha com Exposição alta?
5. O que cada elemento cobra no estágio 4.
6. Juntar Vínculo e Marca (Vínculo = estágio 2)?
7. Habilidade do Transcender nível 3; nível 4 é o mesmo que "Atravessado"?; dá para desfazer?
