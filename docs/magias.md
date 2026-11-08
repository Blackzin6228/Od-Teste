# Magias dos elementos (PROPOSTA da mesa Integrador, 07/11/2026; espera o Nikolas)

> Só documento. Nomes de magias são provisórios (a Lore dá o nome final). Nenhuma magia entra em `docs/ids.md` antes de aprovada.
> **Decisão do Nikolas (07/11/2026):** a qualidade de referência é o addon **Apex Arcana** do Iron's Spells.

## 1. O que é o "nível Apex Arcana" (pesquisa)
O Apex Arcana (MIT, 1.20.1 Forge e 1.21.1 NeoForge) **não está no pack** Conguitos-smp. Ele põe **2 magias de topo em cada escola** do Iron's
(18 no total). O que faz delas magias de topo:
- **Fases:** cada uma tem preparação (2 a 7 s), ápice e rastro. Não é "um projétil maior".
- **Escala:** área de 20 a 100 blocos, mexe na cena inteira (céu, luz, som, cor da tela).
- **Tempo de recarga longo:** 150 a 220 s.
- **Uma por grimório** (padrão do mod, muda por config/gamerule). Isso impede empilhar magia de topo.
- **Som que acompanha a fase** (ex.: nota sobe a cada etapa).
- **Efeito de tela com segurança:** se o shader dá erro, o efeito de tela se desliga sozinho e a magia continua.

**O que pegamos:** essas seis regras de qualidade. **O que não pegamos:** nenhuma magia nem forma dele. Lista do que **não** usar nas nossas
(para não parecer cópia): círculos empilhados que colapsam, portões soltando espadas, mover o sol, ave de fogo, tempestade em vórtice com aurora,
**parar o tempo**, céu rachando, dragão de raio, portão de luz, estrela na mão, **olho no céu**, caixa de gravidade com lanças, ilhas subindo,
corte de dimensão, árvore que cresce, flor de luz, **arena de muros com lanças**, **coração de sangue que drena o campo**.
Também não usamos nomes de rituais de Ordem Paranormal (regra 3 do `CLAUDE.md`).

Fontes: [ModDex — Apex Arcana](https://moddex.gg/mod/apex-arcana-irons-spells-n-spellbooks-addon) · [Modpack Index — Apex Arcana](https://www.modpackindex.com/mod/177047/apex-arcana-irons-spells-n-spellbooks-addon)

## 2. Base técnica (o que já tem no pack)
| Mod | Versão | Uso |
|---|---|---|
| Iron's Spells 'n Spellbooks | 3.16.3 | escola, pergaminho, grimório, mana, poder e resistência por escola |
| AAA Particles (Effekseer) | 2.3.4 | efeitos visuais de todas as magias (`.efkefc`) |
| Player Animation Lib | 2.0.4 | animação do jogador ao conjurar |

**Regra 4 (sem dependência dura):** nenhum dos três é obrigatório. Por isso:
- O **efeito** de cada magia (dano, área, regra) mora no nosso código (`magia/`). O Iron's só **chama** esse efeito por um adaptador em `compat/irons/`.
- **Sem Iron's:** as magias continuam pela tecla de **Afinidade** (ideia do Máscaras), pagando **só Lucidez** (o dobro do custo de Lucidez da tabela, sem mana).
- **Sem AAA Particles:** versão simples com partículas do vanilla (`compat/aaa_particles/` decide qual usar).
- **Sem Player Animation Lib:** a magia sai sem animação.
- As animações são feitas no Blockbench pela mesa Modelador (formato do Player Animation Lib). Os `.efkefc` saem do editor Effekseer.

## 3. Escolas: duas propostas
O Iron's **já tem** uma escola chamada **Sangue** (*Blood*) e uma de **Eldritch** (conhecimento proibido). A nossa precisa de nome que não confunda.

| | **A. Quatro escolas** (Sangue, Morte, Conhecimento, Energia) | **B. Uma escola com 4 ramos** (ex.: "Outro Lado") |
|---|---|---|
| Como fica no Iron's | 4 escolas novas, cada uma com cor, foco, poder e resistência próprios | 1 escola nova; o ramo é uma marca nossa em cada magia |
| Ciclo de vantagens (Sangue > Conhecimento > Energia > Morte > Sangue) | natural: a resistência de cada escola entra na conta | precisamos fazer à mão |
| Itens do Iron's (armaduras, anéis, orbes que dão poder por escola) | funcionam sozinhos, por escola | um só bônus para os 4 elementos (menos escolha) |
| Ligação com a Marca | direta: Marca de Sangue → poder de Sangue | feita por nós |
| Custo | 8 atributos novos (poder + resistência × 4), 4 focos | 2 atributos, 1 foco |
| Risco de confusão | "Sangue" (nosso) ao lado de "Sangue" (Iron's) | menor |

**Recomendação: A**, porque a Marca é por elemento e o ciclo de vantagens fica certo sem código extra. Para não confundir com a escola de Sangue do Iron's,
o nome na tela leva o lugar de onde vem (ex.: "Sangue do Limiar"; nome final com a Lore). Os IDs das escolas (ex.: `odteste:sangue`) ficam para sempre.

## 4. Custo e regras gerais
| Nível | Mana | Lucidez | Recarga | Outras regras |
|---|---|---|---|---|
| Básico | 15–25 | 0 | 1–3 s | — |
| Comum | 30–50 | 0 | 5–10 s | — |
| Avançado | 60–80 | 5–8 | 15–25 s | — |
| Mestre | 110–150 | 10–15 | 40–60 s | só do elemento da **própria Marca** |
| Topo | 400–500 | 25–35 | 180–220 s | só da própria Marca; **sobe a Exposição**; **uma por grimório**; dano em jogador pela metade |

- Magia do **elemento da sua Marca**: metade da Lucidez. Do elemento que **vence** o seu no ciclo: dobro de mana.
- **Sem lag:** o servidor só calcula alvos e dano; o visual é do cliente (um pacote por fase). Nenhuma magia troca blocos de verdade:
  o que "muda o chão" é visual e some sozinho. Limite de 1 magia de topo ativa por área de 64 blocos.

## 5. Como se aprende
| Nível | Como |
|---|---|
| Básico | **pergaminho** no loot das nossas estruturas e das criaturas do elemento |
| Comum | **pergaminho** achado por pista do Dossiê (página, investigação) |
| Avançado | pergaminho "cifrado" que só abre num **ritual** (círculo do Occultism com giz do elemento; sem Occultism, o nosso Círculo) |
| Mestre | **Transcender nível 2** no elemento da Marca: o pergaminho aparece no fim do ritual |
| Topo | **Transcender nível 4** + item do chefe da dimensão do elemento, em **noite de evento**. Vem num **Grimório Ligado**: só o dono usa, não troca, não sai em loot nem em vendedor |

## 6. As magias (5 por elemento)
Visual = efeito AAA Particles (versão simples em vanilla se o mod sair). Animação = Player Animation Lib.

### 6.1 Sangue (vermelho: fúria, fome, dor, ferrugem, espinhos, dentes)
| Nível | Nome | Efeito | Visual | Animação |
|---|---|---|---|---|
| Básico | **Talho Rubro** | corte em meia-lua à frente (4 blocos) + sangramento curto | rastro de gotas grossas que caem e mancham o chão por 3 s | golpe de mão aberta em diagonal |
| Comum | **Mordida de Ferrugem** | projétil de dentes de metal velho; gruda e tira armadura do alvo por 8 s | dentes enferrujados girando, faíscas laranja ao grudar | arremesso de baixo para cima |
| Avançado | **Pele de Espinhos** | 15 s: quem bate em você leva dano; cada golpe que você leva dá 1 Fúria (até 5, +dano) | espinhos de carne saindo dos ombros, vapor vermelho | braços cruzados, curva o corpo e abre de uma vez |
| Mestre | **Investida Bestial** | corre 12 blocos, agarra o primeiro alvo, arrasta e esmaga no fim | silhueta de besta translúcida sobre o jogador, marcas de garra no chão | postura baixa, salto para a frente |
| **Topo** | **Maré Coalhada** | (1) 3 s: o chão de 20 blocos vira poça grossa que sobe; (2) inimigos afundam até a cintura (lentidão forte); (3) a poça se ergue em fileiras de lâminas enferrujadas que se fecham no centro como uma mandíbula. Dano cresce com a vida que **você** já perdeu | tela com borda vermelha batendo como coração; som de batida + guitarra distorcida | joelho no chão, mão espalmada; levanta puxando as duas mãos e fecha os punhos |

### 6.2 Morte (preto: tempo, fim, cinza, ossos, espiral anti-horária, envelhecer)
| Nível | Nome | Efeito | Visual | Animação |
|---|---|---|---|---|
| Básico | **Toque de Cinza** | bola lenta de lodo frio: Murchar + lentidão | lodo preto que solta cinzas **caindo para cima** | empurra com a palma, devagar |
| Comum | **Passo Atrasado** | deixa um eco; 3 s depois você volta ao lugar do eco (só posição, não a vida) | silhueta cinza parada, ponteiros girando ao contrário | mão no peito, gira o pulso no sentido anti-horário |
| Avançado | **Envelhecer** | feixe curto: alvo perde 30% de ataque e velocidade por 10 s | pele do alvo acinzenta; plantas na linha murcham (só visual) | aponta dois dedos e puxa devagar |
| Mestre | **Sepultura Rasa** | chão abre em 6 blocos; ossos seguram os alvos 3 s com Murchar; quem morre ali vira cinza que cura você um pouco | mãos de osso e terra fria, poeira | bate o pé, abaixa a mão aberta como quem empurra para baixo |
| **Topo** | **Último Ponteiro** | (1) o céu perde a cor; um ponteiro de lodo de 25 blocos nasce no chão a partir de você; (2) ele varre a área no sentido anti-horário, 4 voltas; quem ele toca envelhece (perde vida máxima e armadura temporárias, acumula); (3) para no "fim": a área vira terra morta por 30 s, onde ninguém regenera vida | tela sem cor, som tocado ao contrário, tique-taque cada vez mais lento | braço esticado girando o corpo devagar com o ponteiro; no fim, abaixa o braço |

### 6.3 Conhecimento (dourado/branco: ordem, regra, memória, escrita, máscara, sussurro, corrente)
| Nível | Nome | Efeito | Visual | Animação |
|---|---|---|---|---|
| Básico | **Sinal Dourado** | escreve um sinal no ar que voa como projétil e explode em letras | escrita **nossa** (não os sigilos da obra) em dourado | traça o sinal no ar com o dedo e empurra |
| Comum | **Corrente da Regra** | correntes prendem o alvo 2 s; ele fica "Registrado": +15% de dano recebido por 8 s | correntes douradas saindo do chão | puxa o punho fechado para trás |
| Avançado | **Rosto Vazio** | 6 s: criaturas perdem você de vista; ao sair, quem estava te atacando esquece o alvo | máscara branca lisa aparece sobre o rosto e some | mão passa na frente do rosto |
| Mestre | **Lei Escrita** | círculo de 10 blocos com uma regra escolhida ao conjurar: "não conjurar", "não correr" ou "não curar". Inimigo que quebra a regra leva dano alto | círculo de texto dourado que gira; o texto acende sobre quem quebra | abre um livro invisível com as duas mãos |
| **Topo** | **Errata** | (1) páginas douradas giram em 25 blocos; (2) todo efeito bom, escudo e invocação dos inimigos é "apagado"; (3) o que foi apagado é **reescrito** nos aliados, e cada inimigo leva dano pelo número de coisas que perdeu | letras saindo dos corpos como texto arrancado da página; tela com bordas de escrita ilegível; coro e sussurros | ergue a mão, fecha como quem amassa papel e abre de novo sobre os aliados |

### 6.4 Energia (neon: caos, sorte, eletricidade, cabos, explosão, riso)
| Nível | Nome | Efeito | Visual | Animação |
|---|---|---|---|---|
| Básico | **Faísca Instável** | raio curto; dano sorteado entre 50% e 150% | cor muda a cada uso (roxo, ciano, rosa, verde) | estalo de dedos |
| Comum | **Curto-Circuito** | teleporte de 6 blocos num cone, direção sorteada; pequena explosão onde estava | rastro de estática, cabos que somem | ombros tremem, some num chiado |
| Avançado | **Estado Instável** | esfera que troca de estado a cada 1 s: sólida (quica), líquida (poça elétrica), gás (nuvem que cega). Conjurar de novo faz explodir | a esfera muda de forma e cor na hora | segura a esfera entre as mãos e arremessa |
| Mestre | **Roleta** | sorteia 1 de 6: corrente de raios, explosão, velocidade, cura, cegueira em área ou **um efeito ruim para você** | anéis neon girando sobre a cabeça como roleta | gira a mão no ar e bate palma |
| **Topo** | **Apagão** | (1) todas as luzes em 30 blocos piscam e apagam (só no cliente) e cabos neon brotam do chão; (2) os cabos ligam todos os inimigos numa rede, e o dano pula de um para outro, crescendo a cada pulo, em ordem sorteada; (3) cada ponto da rede explode numa cor diferente | aberração de cor e "glitch" na tela, chiado, risada distorcida no fundo | puxa os braços como quem arranca um cabo; no fim, abre os braços |

## 7. Precisa do Nikolas
- Escolher **A (4 escolas)** ou **B (1 escola com 4 ramos)**. Recomendação: A.
- Aprovar a regra **Mestre e Topo só do elemento da própria Marca**.
- Aprovar custo com **Lucidez** e a Exposição subindo no Topo.
- Nomes finais das escolas e das magias → mesa Lore.
- Prévia de 1 magia (sugestão: **Talho Rubro**, a mais simples) com efeito e animação antes de fazer as outras → Modelador + Dev.
