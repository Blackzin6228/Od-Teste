# Textos finais da Vila Calada e do Livro do Sineiro (pt_br e en_us)

Mesa Lore, 09/11/2026. Tarefa 1.13 de `docs/plano-ato1.md`.
Status: **para o Nikolas escolher** onde há opções (A/B/C). O resto segue o que já foi aprovado
(`docs/decisoes/2026-11-09-vila-calada.md`, `docs/decisoes/2026-11-09-ato1-bem-minecraft.md`).

Regras usadas:
- Tom sério. Frases curtas. **Nenhum número como dígito para o jogador** (para o admin pode).
- Nada copiado de outra obra. "Vila Calada" e "Sineiro": busca feita hoje, sem uso conhecido em outra obra.
- No inglês, **"Vila Calada" fica como nome próprio** (como o `en_us.json` já faz). Também ficam "Compadre" (nome de pessoa).
- **Fora deste arquivo, de propósito:** o sino tocando como som ambiente à noite (esperando o Nikolas).
  Por isso a legenda do som do sino rachado e qualquer texto sobre o toque noturno não estão aqui.
- Chave marcada **NOVA** = proposta. O Dev confirma o nome e registra em `docs/ids.md` se for ID.

---

## 1. Livro do Sineiro

**Onde fica hoje:** o texto em pt_br está em `src/main/resources/data/odteste/odteste_livros/sineiro.json` (sem chave de lang).
O inglês abaixo serve para um de dois caminhos (escolha do Dev): um segundo arquivo de livro em inglês, ou chaves de lang.
Se for por lang, as chaves propostas são:

- **NOVA** `odteste.livro.sineiro.titulo`, `odteste.livro.sineiro.autor`
- **NOVA** `odteste.livro.sineiro.pagina.<n>` (n por extenso no arquivo não precisa: é chave, o jogador não vê)
- **NOVA** `odteste.livro.sineiro.pagina.<n>.escondido.<m>` (linhas que só aparecem com Exposição)

### Título e autor
| Chave | pt_br | en_us |
|---|---|---|
| `odteste.livro.sineiro.titulo` (NOVA) | Livro do Sineiro | The Bell-Ringer's Book |
| `odteste.livro.sineiro.autor` (NOVA) | o Sineiro | the Bell-Ringer |

### Correção no JSON (para a CEO passar ao Dev)
- **Página 6:** o `sineiro.json` diz "O **Carniceiro**". O texto aprovado e a decisão N4 dizem **Açougueiro**. Trocar.
- Fora do escopo, só aviso: a chave `odteste.ato1.livro_carniceiro.nome` ainda diz "Livro do Carniceiro". A N4 pede "Diário do Açougueiro".

### Páginas
Exposição "média" = degrau `marcado`; "alta" = degrau `fundo` (como no JSON).

**Página 1**
- pt_br: LIVRO DO SINEIRO\n\nSe alguém abrir isto, eu já não estou.\nEscrevo devagar. A mão treme e a tinta é pouca.\nMeu nome ficou no sino. Não procurem.
- en_us: THE BELL-RINGER'S BOOK\n\nIf anyone opens this, I am already gone.\nI write slowly. My hand shakes and the ink is low.\nMy name stayed in the bell. Do not look for it.

**Página 2**
- pt_br: Éramos poucos, mas éramos uma vila. Casas de tábua, uma capela de pedra, um poço no meio, um curral comprido. E o sino. Eu o tocava ao nascer do sol e ao cair da tarde, e todos sabiam em que hora do mundo estavam.
- en_us: We were few, but we were a village. Plank houses, a stone chapel, a well in the middle, a long pen. And the bell. I rang it at sunrise and at dusk, and everyone knew what hour of the world they were in.

**Página 3**
- pt_br: Veio um inverno que não acabava. O gelo fechou os campos. O gado começou a cair no curral, um por noite. Quem tinha pão dividia. Quem tinha pouco escondia. A fome passou a andar de casa em casa, e era ela quem batia à porta.
- en_us: A winter came that would not end. The ice shut the fields. The cattle began to fall in the pen, one each night. Those who had bread shared it. Those who had little hid it. Hunger walked from house to house, and it was hunger that knocked.
- Escondido (média) pt_br: Não era fome. Era chamado.
- Escondido (média) en_us: It was not hunger. It was a call.

**Página 4**
- pt_br: Foi o Compadre quem falou primeiro. Disse que na beira do poço, de noite, havia um homem de chapéu mole que ouvia e não pedia nada. "Ele só aceita", dizia. O povo quis acreditar. Eu também quis.
- en_us: It was the Compadre who spoke first. He said that at the edge of the well, at night, there was a man in a soft hat who listened and asked for nothing. "He only accepts," he said. The people wanted to believe. So did I.
- Escondido (média) pt_br: Ele já sabia o nome de todos.
- Escondido (média) en_us: He already knew every name.

**Página 5**
- pt_br: Na primeira noite pediram pão, e houve pão. Na segunda pediram carne, e o gado parou de cair. Ninguém viu o homem. Só uma mesa posta na capela, com uma cadeira a mais. Ninguém perguntou de quem era.
- en_us: On the first night they asked for bread, and there was bread. On the second they asked for meat, and the cattle stopped falling. No one saw the man. Only a table set in the chapel, with one chair too many. No one asked whose it was.

**Página 6**
- pt_br: Depois a carne não bastou. Comiam, e a fome ficava. O Açougueiro passou a trabalhar de madrugada. O cheiro de ferro subiu do curral para a rua, e da rua entrou nas camas. Chamavam isso de bênção.
- en_us: Then the meat was not enough. They ate, and the hunger stayed. The Butcher began to work before dawn. The smell of iron rose from the pen into the street, and from the street into the beds. They called it a blessing.
- Escondido (média) pt_br: Os dentes vieram depois da carne.
- Escondido (média) en_us: The teeth came after the meat.

**Página 7**
- pt_br: As camas começaram a amanhecer molhadas e vazias. A Moça do Terço pôs um botão de pedra no peitoril para cada nome. Quando os botões acabaram, ela parou de rezar e passou a contar os que restavam, em voz alta, à noite.
- en_us: The beds began to wake wet and empty. The Rosary Girl set a stone button on the sill for each name. When the buttons ran out, she stopped praying and began to count the ones who were left, out loud, at night.

**Página 8**
- pt_br: Entendi tarde: o homem do poço não era o pior. Ele só aceitava. O pior veio quando o povo teve medo. Não de um bicho. Do escuro entre as casas. E o escuro, de tanto ser temido, aprendeu a olhar de volta.
- en_us: I understood too late: the man at the well was not the worst. He only accepted. The worst came when the people grew afraid. Not of a beast. Of the dark between the houses. And the dark, feared for so long, learned to look back.

**Página 9**
- pt_br: Na última noite subi à torre e toquei sem parar, para quem estivesse longe. Pedi que viessem. O sino não é só bronze, descobri. É uma porta com dois lados, e eu a abri sem saber para onde dava.
- en_us: On the last night I climbed the tower and rang without stopping, for anyone far away. I begged them to come. The bell is not only bronze, I learned. It is a door with two sides, and I opened it without knowing where it led.

**Página 10**
- pt_br: O fogo começou no curral. Não vi quem acendeu. As casas caíram umas sobre as outras e ninguém correu, porque já não havia para onde. Só a capela ficou de pé, sem teto. A cadeira a mais ficou virada para a porta.
- en_us: The fire began in the pen. I did not see who lit it. The houses fell onto one another and no one ran, because there was nowhere left to run. Only the chapel stood, without a roof. The extra chair stayed turned toward the door.
- Escondido (média) pt_br: Uma cadeira para cada um que viria.
- Escondido (média) en_us: One chair for each one who would come.

**Página 11**
- pt_br: Rachei o sino com o martelo do ferreiro, para que não chamasse mais ninguém. Rachado, ele só toca de noite, e fraco. Mas o que entra por uma porta só sai por ela. O sino precisa tocar ao contrário.
- en_us: I cracked the bell with the smith's hammer, so it would call no one else. Cracked, it rings only at night, and faintly. But what comes in through a door can only leave through it. The bell must ring backward.
- Atenção: "ele só toca de noite" é texto já aprovado, mas depende da decisão pendente sobre o toque noturno.
  Se o Nikolas tirar o toque à noite, esta frase muda. Alternativa pronta (sem citar a noite):
  - pt_br: Rachado, ele quase não tem voz.
  - en_us: Cracked, it has almost no voice.

**Página 12**
- pt_br: O pai do meu pai dizia que o mundo tem quatro portas pequenas: a que sangra, a que cala, a que lembra e a que acende. E uma quinta que não é porta, é parede. Para o sino tocar ao contrário, as quatro precisam responder. Eu só consegui fechar uma: a que sangrava. E mal.
- en_us: My father's father said the world has four small doors: the one that bleeds, the one that hushes, the one that remembers and the one that kindles. And a fifth that is no door, but a wall. For the bell to ring backward, all four must answer. I closed only one: the one that bled. And poorly.
- Atenção: é a página mais longa (perto do limite de uma página do livro). Se não couber na tela, cortar
  "pequenas" / "small" primeiro. As palavras "sangra, cala, lembra, acende" não podem sair.

**Página 13**
- pt_br: Deixo o que sei do sino, pois só ele me ouviu. Fala em dois tons: o grave é demorado, o agudo é pressa. Eu contava de cinco em cinco, como meu pai. Outro sino, em outra vila, aprendeu a minha voz. Ouçam o que ele disser.
- en_us: I leave what I know of the bell, since only it heard me. It speaks in two tones: the low one lingers, the high one hurries. I counted by fives, like my father. Another bell, in another village, learned my voice. Listen to what it says.

**Página 14** (em branco sem Exposição)
- Texto visível pt_br / en_us: (vazio)
- Escondido (média) pt_br: Eu toquei o sino uma vez. O sino tocou duas. Na segunda, ninguém tocou.
- Escondido (média) en_us: I rang the bell once. The bell rang twice. The second time, no one rang it.
- Escondido (alta) pt_br: Estava chamando vocês. {marcado}: sua cama aqui já estava feita.
- Escondido (alta) en_us: It was calling you. {marcado}: your bed here was already made.

---

## 2. Textos da Vila Calada para o jogador

### Placa na beira do poço
Placa dentro da estrutura (`.nbt`). Se o Dev usar placa traduzível, a chave é:
| Chave | pt_br | en_us |
|---|---|---|
| `odteste.vila_calada.placa.poco` (NOVA) | Aqui ele ouvia. | Here he listened. |

### Abrir o livro no atril
A ficha pede: o sino toca uma vez, fraco, e depois silêncio.
- **A (recomendo):** nenhum texto. Só o som. O silêncio faz o trabalho.
- **B:** uma linha na barra de ação, uma vez por jogador.

| Chave | pt_br | en_us |
|---|---|---|
| `odteste.vila_calada.livro.ouviu` (NOVA, só se B) | Lá em cima, a vila ouviu. | Up above, the village heard. |

### Vela da capela (tarefa 1.10)
Nome do bloco (o ID é do Dev):
| Opção | pt_br | en_us |
|---|---|---|
| A (recomendo) | Vela da Capela | Chapel Candle |
| B | Vela que Não Treme | Unwavering Candle |

| Chave | pt_br | en_us |
|---|---|---|
| `block.odteste.<id_da_vela>` (NOVA) | (nome escolhido acima) | (nome escolhido acima) |
| `odteste.vila_calada.vela.dica` (NOVA, tooltip) | A única chama da vila. Ninguém a acendeu. | The only flame in the village. No one lit it. |
| `odteste.vila_calada.vela.lucidez` (NOVA, barra de ação ao recuperar) | A chama não treme. Você volta a respirar. | The flame does not waver. You breathe again. |

### Poças que sangram (tarefa 1.10)
Nome do bloco:
| Opção | pt_br | en_us |
|---|---|---|
| A (recomendo) | Poça que Sangra | Bleeding Puddle |
| B | Poça Escura | Dark Puddle |

| Chave | pt_br | en_us |
|---|---|---|
| `block.odteste.<id_da_poca>` (NOVA) | (nome escolhido acima) | (nome escolhido acima) |
| `odteste.vila_calada.poca.lucidez` (NOVA, barra de ação ao perder) | O chão está morno. Não é água. | The ground is warm. It is not water. |

### O Faminto (criatura, nome provisório)
| Chave | pt_br | en_us |
|---|---|---|
| `entity.odteste.faminto` (NOVA) | Faminto | The Starved |

Busca feita hoje: "Faminto" não é criatura de Ordem Paranormal. **Mas "Faminto" é o título em português de um filme de
terror de 2021** (o do wendigo). É palavra comum e o nosso é outro bicho, então o risco é baixo. Opções:
- **A:** manter "Faminto" / "The Starved".
- **B:** trocar para "Esfaimado" / "The Starveling" (mais raro, sem título de filme).

### Mapa que aparece com o grupo (pista 3, tarefa 1.9)
A ficha diz "nenhum texto". Mas o item precisa de algum nome na mão.
| Opção | pt_br | en_us |
|---|---|---|
| A (recomendo) | sem nome próprio: fica "Mapa" do jogo | (vanilla) |
| B | Mapa sem Dono | Ownerless Map |
| C | Mapa do Sino Rachado | Map of the Cracked Bell |

Chave, se B ou C: `odteste.vila_calada.mapa.nome` (NOVA).

### Analisador de Sinal captando o sino rachado (atalho tech, tarefa 1.3)
Segue o padrão das chaves `odteste.sinal.sino_vila.*` (maiúsculas, sem acento, como letra de código).
| Opção | `odteste.sinal.sino_rachado.decifrado` (NOVA) | `odteste.sinal.sino_rachado.camada` (NOVA) |
|---|---|---|
| A (recomendo) | VENHAM | A CAMA JA ESTA FEITA |
| B | VENHAM | NINGUEM TOCOU A SEGUNDA |

en_us: igual ao pt_br (é o sinal decifrado, como já acontece com FERRO e NAO ENTREM PELA PORTA DA FRENTE).

Direção (se o Integrador mostrar para onde o sinal puxa):
| Chave | pt_br | en_us |
|---|---|---|
| `odteste.analisador.direcao` (NOVA) | O ponteiro puxa para %s. | The needle pulls toward %s. |
| `odteste.direcao.norte` / `sul` / `leste` / `oeste` (NOVA) | o norte / o sul / o leste / o oeste | the north / the south / the east / the west |

### Legendas de som (tarefa 1.12)
Só os sons de clima. A legenda do sino rachado fica para depois da decisão do toque noturno.
| Chave (NOVA, nome do som é do Modelador) | pt_br | en_us |
|---|---|---|
| `subtitles.odteste.vila_calada.vento` | Vento entre tábuas queimadas | Wind through burnt planks |
| `subtitles.odteste.vila_calada.madeira` | Madeira estala | Wood creaks |
| `subtitles.odteste.vila_calada.corrente` | Corrente range | A chain grinds |
| `subtitles.odteste.vila_calada.arrasto` | Algo é arrastado | Something is dragged |

---

## 3. Aviso ao admin: "{grupo} não seguiu o sino" (provisório)
Só o admin vê. Aqui número pode aparecer.
**Chave NOVA:** `odteste.vila_calada.nao_seguiu` (`%1$s` = grupo, `%2$s` = noites desde o Primeiro Sinal).

| Opção | pt_br | en_us |
|---|---|---|
| A (recomendo) | %1$s não seguiu o sino. %2$s noite(s) desde o Primeiro Sinal sem chegar perto da Vila Calada. Se quiser, solte um empurrão. | %1$s did not follow the bell. %2$s night(s) since the First Signal without nearing Vila Calada. Push them if you want. |
| B | %1$s não seguiu o sino (%2$s noite(s) parado). | %1$s did not follow the bell (%2$s night(s) idle). |

Se o Dev quiser uma segunda linha com as opções de empurrão:
| Chave | pt_br | en_us |
|---|---|---|
| `odteste.vila_calada.nao_seguiu.dica` (NOVA) | Empurrões: um Faminto vagando até a vila comum, ou a mesma pista mais perto. | Pushes: a Starved wandering to the common village, or the same clue closer. |

---

## 4. Chaves que já existem (Vila Calada, admin): ficam como estão
Conferi `pt_br.json` e `en_us.json`. Estão claras, sérias o bastante para o admin e com os dois idiomas. Sem mudança:
`odteste.vila_calada.ver`, `.ligado`, `.desligado`, `.sem_ancora`, `.grupo.antes`, `.grupo.seguindo`, `.grupo.chegou`,
`.ligou`, `.desligou`, `.tocou`, `.marcou_chegada`, `.reabriu`, `.ancorada_pela_estrutura`.
Também sem mudança: `odteste.ato1.abertura` e `odteste.ato1.troca.2` (citam o sino, mas não o toque noturno).

## Para o Nikolas escolher
1. Abrir o livro: silêncio (A) ou uma linha (B)?
2. Nome da vela e da poça (A ou B).
3. "Faminto" fica ou vira "Esfaimado"?
4. Mapa: sem nome (A), "Mapa sem Dono" (B) ou "Mapa do Sino Rachado" (C)?
5. Sinal do sino rachado no Analisador: camada A ou B?
6. Aviso ao admin: longo (A) ou curto (B)?
