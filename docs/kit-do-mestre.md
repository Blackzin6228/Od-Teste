# Kit do Mestre e Mundo Vivo (mesa lore)

Status: **PROPOSTA.** Nada aqui está decidido até o Nikolas aprovar. Perguntas abertas no fim.
Base: `docs/decisoes.md` (08/11/2026), `lore/roteiro.md`, `docs/integracoes.md`.
Sem IDs novos neste documento. Nomes de itens, cenas e telas são provisórios.

## O problema (palavras do Nikolas sobre o mod antigo)
- Poucos efeitos.
- Grind chato: só juntar recurso. O mod não desenvolvia.
- O pessoal não tinha no que focar.
- Quando o Nikolas entrava como entidade para assustar e subir Exposição, era tudo manual e arcaico.

A resposta tem quatro partes: o **Kit do Mestre** (ferramenta), o **Mundo Vivo** (o horror não depende dele),
o **Dossiê com foco** (o jogador sempre sabe o que fazer) e o **efeito mínimo** (nada acontece seco).

---

## 1. KIT DO MESTRE

Ideia: o admin vira uma aparição de verdade, por uma **tela**, não por comando. Comando fica só como atalho para quem quiser.

Na história, quem faz isso é o `{ilegivel}`. O paranormal não o lê, então ele pode vestir qualquer forma sem que o paranormal reclame.
Isso casa com o papel dele no roteiro (decisão de 08/11) e dá a justificativa dentro do jogo.

### 1.1 Como se abre
- Item de mão ou tecla. Só o admin tem. Nome provisório: **Véu**.
- Ao usar, abre o **Painel do Mestre**. Fechar o painel não desfaz a forma: o mestre continua em cena e joga normal.
- Sem número em nenhum lugar, nem no painel do mestre. Intensidade é palavra: *leve, firme, pesada*.

### 1.2 Aba "Forma"
O mestre escolhe o que ele é agora.
- **Entidade do elemento** (a que o Ato do elemento tiver aprovado: Diabo, Magarefe, Convidada, e as de Conhecimento, Morte e Energia quando existirem).
  Troca o modelo e a voz. Cada forma já vem com seus gestos (andar, olhar, parar) prontos.
- **Vulto.** Silhueta sem rosto. Só aparece de lado, some ao olhar direto. Barato, dá para usar o tempo todo.
- **Só voz.** Invisível, sem corpo. O som vem de um ponto do mundo que ele escolhe (atrás do jogador, dentro da parede, do poço).
- **Invisível total.** Para observar. Os jogadores não veem nem ouvem nada dele.
- Para cada forma, três ajustes em palavras: **distância** (perto, média, longe), **visto por** (só um jogador, o grupo, todos), **duração** (um instante, um tempo, até eu desfazer).
- "Visto por um jogador só" é o mais importante: é o que faz o Pendurado do Ato 1 e a alucinação de Lucidez baixa parecerem reais. O mestre escolhe quem vê.
- Voz: usa o Simple Voice Chat com o voice changer do pack. Cada forma tem um **tom** pronto (o Diabo calmo e grave, a Convidada baixa). Um botão "falar como a forma".

### 1.3 Aba "Poderes" (efeito completo, 1 clique)
Cada poder já vem com imagem, som e reação (ver seção 5). O mestre só escolhe o alvo e a intensidade.

| Poder | O que o jogador vive |
|---|---|
| **Apagar luzes** | Tochas, lanternas e lâmpadas perto vão apagando uma a uma, na direção do jogador. Silêncio antes da última |
| **Sussurrar o nome** | O nome de `{jogador}` dito baixo, perto do ouvido, de uma direção que não tem ninguém |
| **Passos** | Passos andando em volta, no ritmo do jogador, e parando quando ele para |
| **Bater a porta** | A porta mais próxima fecha sozinha, com batida seca. Variação: bate de dentro, como se pedissem para abrir |
| **Piscar na borda da visão** | Um vulto aparece no canto da tela por um instante e some. Só o alvo vê |
| **Aparecer e sumir** | A forma surge a distância, parada, olhando. Quando o jogador pisca, ou olha para outro lado, ela já não está |
| **Aproximar** | A forma está um pouco mais perto a cada vez que o jogador olha de novo |
| **Peso no ar** | Neblina baixa, tremor leve, som grave. Sem criatura |
| **Falar** | Fala pronta (do `lore/roteiro.md`) ou livre, com voz da forma |
| **Subir Exposição de quem viu** | Quem estava olhando "vê mais" (um degrau de Exposição). Escolha: *leve, firme*. Nunca mostra número |
| **Descer Lucidez de quem viu** | A cabeça de quem viu cede um pouco. Mesma lógica de palavra |
| **Devolver Lucidez** | Gesto de misericórdia: a luz volta, o barulho para. O mestre também pode ajudar |

Regras do painel, para o mestre não quebrar a história:
- **Exposição só sobe, como no roteiro.** O painel não tem botão de descer Exposição.
- **Poder pesado pede confirmação** (por exemplo, subir Exposição de um grupo inteiro).
- **Respeita a regra de ouro do roteiro:** Exposição abre a porta, Lucidez paga o pedágio. O painel mostra, em palavra, quem está "perto da beira" (Lucidez baixa), para o mestre não afundar quem já está no fundo sem querer.
- **O mestre não afeta quem não quiser.** Opção por jogador no menu do servidor: "não quero ser alvo de susto". Para um servidor futuro com público, isso é obrigatório.
- Todo poder respeita o Ato aberto: a Convidada não aparece antes do enigma 5 do Ato 1, por exemplo.

### 1.4 Aba "Cenas" (1 clique, várias coisas juntas)
Uma cena é uma sequência pronta de poderes, com tempo. Cada uma já usa som, imagem e reação.

| Cena | O que acontece |
|---|---|
| **A vila para** | Todo mundo num raio da vila fica parado, sem som de mob. O vento some. Um sino toca longe. Depois tudo volta, mas ninguém se lembra de ter parado (Lucidez cai um pouco) |
| **O sino toca** | Sino no horizonte, em direção que o mestre escolhe. Cada toque some uma luz ao longe. O barulho de metal da floresta cala de uma vez |
| **Visita do Diabo** | Prelúdio, chegada, conversa e oferta do roteiro, na ordem, com pausas. O mestre só escolhe a hora e fala nas pausas |
| **O lugar a mais** | Na casa ou base do grupo, uma cadeira a mais aparece posta, com um prato com o nome de `{parceiro}`. Some quando alguém chama |
| **A voz do amigo** | O jogador ouve a voz de um amigo do grupo chamando por ele, de fora da base. Só ele ouve. O amigo estava dormindo |
| **A fila da noite** | Silhuetas paradas, uma ao lado da outra, na borda da luz da fogueira. Só quem tem Exposição alta vê |
| **O pendurado** | O Pendurado do Ato 1 aparece em qualquer lugar escuro que o mestre apontar |

- O mestre pode **montar cena própria** arrastando poderes numa linha do tempo, e salvar com nome (para reaproveitar).
- Cenas por elemento: cada Ato novo entrega ao menos 2 cenas prontas do seu elemento.
- Toda cena tem **"gancho de história"**: no fim, deixa uma pista (um bilhete, uma marca na parede) que o jogador pode achar. Susto sem pista é só susto.

### 1.5 Desfazer e segurança
- **Botão Desfazer:** corta a cena, devolve o céu, a luz e o som, tira a forma do mestre. Não devolve Exposição (ela só sobe), mas devolve a Lucidez perdida **naquela cena**, aos poucos.
- **Botão de pânico:** igual ao Desfazer, mais um aviso em texto calmo a todos: *"Passou."* Para quando um jogador real se assustar de verdade.
- **Registro (log) privado do mestre:** o que foi feito, em quem, quando. Para o mestre lembrar o que o grupo já viu e para evitar repetir.
- **Tudo reversível:** o painel nunca quebra bloco, mata jogador ou mexe em inventário. Só afeta som, luz, visual, neblina, Exposição e Lucidez. Isso protege o mundo definitivo do servidor.

### 1.6 Formas do mestre (requisito: troca de tudo de uma vez)
Status: **REQUISITO proposto**, falta o ok do Nikolas. Detalha a aba "Forma" (1.2). Sem código, só o que precisa existir.

**A dor que isto resolve.** No mod antigo, para cada aparição o Nikolas rodava `/fakename` e `/skin` na mão, toda vez.
No Kit isso some. Escolheu a forma no menu, o resto é do mod.

**Regra principal.** Ao escolher uma FORMA no menu do Kit, o mod troca tudo junto, num único clique:
1. **Corpo.** O jogador do mestre é desenhado com o modelo da entidade (GeckoLib). Não é skin de jogador.
2. **Nome.** O nome é trocado ou escondido, em todo lugar onde aparece: em cima da cabeça, na lista TAB e no chat.
3. **Som e partícula.** Passos, voz, respiração e partículas passam a ser os daquela entidade.
4. **Sair da forma.** Ao sair, tudo volta como estava: modelo, nome, TAB, chat, sons e partículas. Nada fica para trás.

**Nenhum comando manual.** `/fakename` e `/skin` não são necessários para nada disso. O mestre nunca digita para entrar ou sair de uma forma.

**Formas prontas (lista inicial):**
| Forma | O que é |
|---|---|
| As 4 entidades | As entidades dos elementos, com os nomes como estão neste documento e no `lore/roteiro.md`. Esta seção não cria nome novo (ver pergunta 12) |
| **Vulto** | A silhueta sem rosto da aba "Forma" (1.2) |
| **Aldeão "errado"** | Parece um aldeão comum, mas algo está fora do lugar. O "errado" fica a cargo da Lore e do Modelador, em proposta separada |
| **Jogador "duplicado"** | Cópia de um jogador que está online agora. O mestre escolhe o alvo numa lista de quem está presente |
| **Criaturas do mod** | Cada criatura nova do mod vira uma forma, sem trabalho extra do mestre |

**Regras de comportamento:**
- Uma forma por vez. Escolher outra troca direto, sem passar pelo estado normal.
- Cada forma já vem completa: modelo, nome, som e partícula. Forma sem um desses não entra no menu.
- O nome mostrado é o da forma (ou nenhum). O mestre pode ver na tela dele em qual forma está.
- O que a forma não fizer parte (inventário, itens, vida) não muda. A forma é visual e sonora (ver 1.5, "tudo reversível").
- O jogador "duplicado" só existe enquanto o alvo está online. Se o alvo sair, o que acontece está em aberto (pergunta 15).
- Respeita as regras de 1.3: o Ato aberto, o "não quero ser alvo de susto" e o pedido de confirmação para o que for pesado.
- Se AAA Particles ou outro mod de efeito sair do pack, a forma usa as partículas do próprio jogo, sem crash (regra 4 do `CLAUDE.md`).

**Critérios de aceite (o que o QA confere):**
1. Escolher cada forma pelo menu troca modelo, nome, som e partícula, sem digitar nenhum comando.
2. Outro jogador, em outro computador, vê o mestre com o modelo da entidade (não com skin de jogador).
3. O nome da forma (ou a ausência de nome) aparece igual em cima da cabeça, no TAB e no chat. O nome real do mestre não aparece em nenhum dos três.
4. Ao sair da forma, modelo, nome, TAB, chat, sons e partículas voltam ao estado de antes, conferido nos três lugares do nome.
5. Entrar e sair da forma 10 vezes seguidas não deixa resto (nome preso, modelo errado, partícula solta, som preso).
6. Trocar de uma forma direto para outra não deixa nada da forma anterior.
7. Com 3 ou mais jogadores online, todos veem a mesma forma ao mesmo tempo, e quem entra depois também a vê.
8. O Desfazer e o botão de pânico (1.5) tiram a forma e restauram tudo.
9. Jogador "duplicado": a lista mostra só quem está online; o nome e o TAB seguem a regra da pergunta 14.
10. Sem o mod de partículas do pack, a forma entra, sai e não derruba o servidor.
11. Inventário, vida e itens do mestre ficam iguais antes, durante e depois da forma.
12. Subir o servidor com o pack inteiro e usar uma forma não gera erro no log.

---

## 2. O MUNDO SE MEXE SOZINHO

Para o horror não depender do Nikolas estar online. O mundo é um "mestre automático" em modo raro.

### 2.1 O Diretor (proposta de nome)
Um sistema que, quando o mestre está **offline**, solta eventos pequenos e raros. Ele nunca solta os **grandes** eventos de entidade (decisão de 08/11: esses são do admin).
Ele só faz o que o painel faz nas abas Poderes e uma ou duas Cenas pequenas.

- **Raridade:** em média um evento pequeno por sessão de cada jogador, com folga grande. Não é spam.
- **Respiro:** depois de um evento, silêncio longo. O jogador precisa esquecer para o próximo assustar.
- **Contexto:** só acontece quando faz sentido: noite, jogador sozinho, longe de luz, perto de lugar marcado, com Exposição acima de um piso.
- **Nunca em combate, nunca depois de morrer, nunca logo ao entrar no servidor.**
- **Aviso ao mestre:** quando ele volta, vê o que o Diretor fez ("A Convidada piscou para `{anfitriao}` ontem à noite"), para encaixar na história.
- Interruptor no arquivo de regras do servidor (`mundo_vivo=false`, nome a ser definido pelo Dev).

### 2.2 Exemplos de eventos automáticos (todos curtos)
- Uma luz da base apaga sozinha e acende de novo.
- Passos atrás do jogador que está sozinho na mina.
- O nome do jogador dito baixo uma vez.
- Uma porta que estava aberta amanhece fechada.
- O vulto na borda da visão, uma única vez, sem repetir na mesma noite.
- O Dossiê ganha uma linha que o jogador não escreveu (ver seção 3).
- Um animal da base olha fixo para um canto vazio.
- O barulho de metal da floresta toca mais perto que o normal e volta ao normal.

### 2.3 Eventos de mundo ligados à história
- **O mundo responde ao que o grupo fez.** Se resolveram um enigma, o Diretor solta um sinal pequeno no dia seguinte (algo que mostra que foram notados). Exemplo: depois da Mesa do Pacto, a vela de cera some das casas do grupo.
- **Se o grupo ficar parado muito tempo** sem avançar o mistério, o Diretor dá um **empurrão**: um bilhete novo, uma pista nova num lugar que eles já visitam. Nunca uma resposta, só um novo ângulo.
- **Cada elemento tem seu "clima"** (Sangue: cheiro de ferro e vermelho no céu; Conhecimento: palavras ao contrário em placas; Morte: relógios parando; Energia: estática e cabelo arrepiado). O Diretor usa o clima do elemento do Ato aberto.
- O `{ilegivel}`, quando está online, é **ignorado** pelo Diretor na maioria das vezes (ele é a página em branco). Raramente algo olha para ele e não acha nada (frase única do roteiro).

---

## 3. SEMPRE TER NO QUE FOCAR

### 3.1 O Dossiê como bússola
O Dossiê é o livro do jogador. Hoje guarda pistas (decisão do roteiro). Proposta: ele vira **o lugar onde o jogador sabe o que fazer**, sem número e sem marcador.

Três páginas fixas, todas em palavras, na voz de quem escreve à mão:

1. **Mistérios abertos.**
   Uma lista de perguntas, não de tarefas. Exemplos: *"Quem acendeu a vela do outro lado do mapa?"*, *"O que a fita dizia por baixo da voz?"*, *"Por que o lugar a mais estava sujo?"*
   Uma pergunta resolvida sai da lista e vira entrada na página de Achados.
2. **Pistas achadas.**
   Cada pista é um recorte: o que foi achado, onde foi achado (em palavras: "no carro da clareira"), e **com quem**. A pista cifrada fica copiada no Dossiê (nunca a resposta, regra do roteiro).
   Pistas ligadas a um mistério aparecem **ao lado** dele, com um fio desenhado.
3. **Próximo passo.**
   Uma frase curta, escrita como lembrete do próprio jogador, que muda conforme o que o grupo fez. Exemplos:
   - *"A fita tem mais por baixo. Precisamos de ouvido melhor, ou de uma máquina."*
   - *"Falta alguém ler o mapa em voz alta. Eu não vou ser."*
   - *"A capela só abre com todo mundo. Ninguém fala."*
   O próximo passo é **sugestão**, nunca ordem, e nunca mostra a resposta. Quando tem dois caminhos (cabeça ou aparelho), mostra os dois.

Regras do Dossiê:
- **Sem número, sem barra de progresso, sem marcador no mapa, sem seta na tela.** Só texto e desenho.
- **Com Lucidez baixa, o Dossiê mente** (como o resto do mundo): uma linha pode trocar. O grupo confirma junto. Casa com a regra do roteiro.
- **Com Exposição média ou alta, aparece uma linha em vermelho** que o jogador não escreveu (a letra é diferente). É um jeito de a história falar com ele.
- **Cada jogador tem o seu**, mas existe uma página **do grupo** (mistérios que todos conhecem). Pistas achadas por um aparecem para os outros quando ele as mostra (item, voz, chat).
- **Ninguém perde progresso sem querer:** morrer não apaga o Dossiê (ele volta com o jogador).
- Dossiê perdido pode ser pedido de novo ao admin sem apagar o que já foi achado (o conteúdo fica salvo no jogador, não no item).

### 3.2 Cada elemento é uma temporada
Cada elemento vira uma **temporada** com começo, meio e fim. O jogador sempre sabe qual é a temporada aberta.

| Parte | O que o jogador tem |
|---|---|
| **Pergunta da temporada** | Uma pergunta grande, escrita no Dossiê no começo. Ex. (Sangue): *"O que o Diabo quer guardar?"* |
| **Cadeia de enigmas** | 4 a 6 enigmas, cada um com versão de cabeça e de aparelho (como no Ato 1) |
| **Presença** | Uma entidade e um escolhido (decisão de 08/11), com aparições escalonadas |
| **Evento de fim** | Um evento ao vivo, solto pelo mestre ou pelo Diretor em versão pequena |
| **Recompensa de história** | Relíquia, espada, uma cena e uma pista que abre a próxima temporada |
| **Fechamento** | O Dossiê "fecha" a temporada com uma página escrita pelo próprio jogo, sem número |

- **Temporada aberta no servidor:** o mestre sabe qual elemento está "em temporada" e pode adiantar, segurar ou esperar o grupo.
- **Entre temporadas:** período de calma curto, com um mistério solto e um evento do Diretor, para respirar.
- **Quem entrou tarde pega a temporada atual** e tem uma página de "o que aconteceu antes" no Dossiê, escrita como boato (sem spoiler total).
- **Ordem das temporadas** vem do Ato 3 do roteiro e depende do Nikolas.

### 3.3 O que o grupo sempre faz (não depende do mestre)
- Resolver o próximo enigma da cadeia (tem de cabeça e tem de aparelho).
- Explorar um lugar que se manifestou (decisão de 08/11: o lugar vem até eles).
- Descobrir o elemento de cada um (a revelação na Mesa).
- Montar o que foi achado: cruzar pistas, descobrir o que liga um mistério ao outro.
- Cuidar da Lucidez do grupo (quem está sozinho? quem está no fundo?).

---

## 4. CONTRA O GRIND

### 4.1 O que NÃO faremos
- **Pedir quantidade de recurso para avançar.** Nada de "traga 500 de X". Recurso nunca é a chave de um enigma.
- **Matar mob em quantidade para ter progresso.** O Magarefe, por exemplo, perde o interesse e vai embora (roteiro). Não existe "derrote 50".
- **Farmar tempo.** Nada de "fique 2 horas num lugar", nem espera em relógio para liberar.
- **Rolar sorte para o item de história.** Item de história nunca tem chance de cair. Ele está num lugar, ou resolve-se um enigma e ele vem.
- **Repetir o que já foi feito.** Nenhuma saída pede a mesma ação duas vezes.
- **Diário (missão do dia).** Nada de missão que reseta e vira tarefa.
- **Barra de progresso, contagem, porcentagem.** Decisão do Nikolas: nenhum número visível.
- **Bloquear por tech ou por magia.** Tech e magia só dão atalho e camada extra (roteiro).
- **Castigo por não ter jogado.** Quem entrou tarde pega a história de onde está.

### 4.2 Como cada recompensa vem
| Recompensa | Vem de |
|---|---|
| Pista nova | Resolver o enigma anterior (cabeça ou aparelho) |
| Item de história (Faca de Osso, Sino do Pacto) | Estar no lugar certo, na hora certa, com o enigma resolvido |
| Marca no corpo | Sobreviver a um evento e aceitar (ou recusar sem castigo, ver Ato 1) |
| Relíquia e espada | Fechar a temporada do elemento |
| Camada tech (máquinas) | Entender o que o enigma pedia e ter a receita (receita simples, sem farm longo) |
| Camada de magia | Entender o símbolo e usar o ritual |
| Mais Exposição | Ver, ler, aceitar, estar lá (nunca "farmar") |
| Mais Lucidez de volta | Descansar com luz, ficar perto de amigo, Vela de Sebo (roteiro) |
| Cosmético (se existir) | Escolha de jogador, nunca vantagem. Ver regra 9 do `CLAUDE.md` |

- **Tech e magia dão profundidade, não requisito.** Quem joga tech tem mais camadas (o Analisador entrega a frase escondida). Quem joga com a cabeça acha pelo método (frequência, César, Morse).
- **Recurso existe, mas serve ao mundo, não à história:** o Resíduo Rubro e as Incursões (roteiro) são **risco escolhido**, não degrau. Quem não guarda, não sofre.
- **Teste de grind (para o QA, ligado ao interesse do Nikolas):** toda proposta nova de enigma ou recompensa passa por 3 perguntas antes de entrar.
  1. Dá para resolver sem repetir uma ação mais de uma vez?
  2. Dá para resolver sem juntar recurso em quantidade?
  3. O jogador aprende ou descobre algo ao fazer isso?
  Se alguma resposta for "não", volta para a Lore.

---

## 5. CHECKLIST DE "EFEITO MÍNIMO"

Regra: **nada paranormal acontece seco.** Toda ação precisa de **imagem + som + reação** antes de ir para o jogo.
O Revisor usa isto como checklist de PR para qualquer poder, cena, aparição ou evento.

### 5.1 Os três itens
| Item | Pergunta | Ferramenta provável |
|---|---|---|
| **Imagem** | O jogador **vê** algo mudar? | Partícula (AAA Particles), neblina, mudança de luz, modelo (GeckoLib), tela |
| **Som** | O jogador **ouve** algo mudar? | Som próprio ou do jogo, com direção; silêncio também conta |
| **Reação** | O corpo do jogador **sente**? | Animação do jogador (Player Animator: tropeço, mão no ouvido, olhar para trás), tremor de câmera leve, batida, mudança de Lucidez, fala |

### 5.2 Regras de uso
- **Os três juntos.** Se faltar um, o efeito não entra.
- **Antes, durante e depois.** Um aviso pequeno antes (som, luz), o acontecimento, e uma marca depois (cheiro, poça, marca na parede). A marca depois é o que o jogador conta para o grupo.
- **Direção é tudo.** Som sem direção é ruído. O som sempre tem de onde vir.
- **Silêncio é efeito.** Cortar o som ambiente antes de um susto conta como som.
- **Pouco efeito, bem feito.** Poucas partículas por efeito (regra técnica de `lore/propostas-elementos.md`), para o servidor aguentar com vários jogadores.
- **Cliente e servidor:** o que só o alvo vê (vulto na borda, nome sussurrado) é desenhado só no cliente dele. É barato e é o que permite "só um jogador vê".
- **Acessibilidade:** opção por jogador para reduzir tremor, flash e susto sonoro alto. Obrigatório para servidor futuro.
- **Sem dependência dura:** AAA Particles e Player Animator são mods do pack. Se saírem, o efeito cai para partícula e animação do próprio jogo, sem crash (regra 4 do `CLAUDE.md`). O código fica em `compat/<mod>/`.

### 5.3 Exemplo preenchido: "Sussurrar o nome"
- **Imagem:** neblina baixa de leve ao redor do jogador, uma partícula fina vermelha perto do ouvido (se Exposição alta).
- **Som:** o nome dito baixo, com direção que muda, por trás.
- **Reação:** o jogador vira a cabeça sozinho (animação curta), a Lucidez cai um tico, uma linha nova aparece no Dossiê.
- **Marca depois:** nada. Nem a neblina fica. É o efeito.

### 5.4 Como o QA usa isso (ligado ao interesse do Nikolas)
Cada poder, cena e aparição ganha uma linha numa tabela de teste: *imagem ok, som ok, reação ok, desfaz ok, funciona só para o alvo, funciona em servidor com vários jogadores*. Isso é um bom primeiro caso de teste de verdade do mod.

---

## 6. Como isso encaixa no que já foi decidido
- **Protagonistas são os jogadores:** o painel usa os marcadores `{jogador}`, `{parceiro}`, `{anfitriao}`, nunca nome fixo.
- **A história chega por pistas, eventos ao vivo e enigmas:** o Kit é o jeito do admin soltar o evento ao vivo sem comando. O Diretor só cobre o pequeno.
- **Ritmo:** grandes eventos continuam nas mãos do admin. O Diretor não os dispara.
- **Pouco grind:** a seção 4 transforma a decisão em lista de verificação.
- **Mundo definitivo (ATM10):** o Kit e o Diretor não mexem em bloco nem em terreno. Só som, luz, neblina, modelo, Exposição e Lucidez.
- **Admin sem elemento:** o Kit é o jeito do `{ilegivel}` participar como "a página em branco que veste qualquer rosto".
- **Tech e magia:** o Analisador de Sinal e o Gravador de Evento ganham uso novo: gravar e ler o que o mestre fez (exemplo: a fala do Diabo guardada).

## 7. Ordem sugerida de construção (para o Dev e o Integrador estimarem, sem prazo)
1. Painel do Mestre com Forma "Só voz" e "Vulto" e 4 poderes (apagar luzes, passos, sussurrar, bater porta). Já dá para soltar sustos reais.
2. Subir Exposição / descer Lucidez de quem viu + Desfazer + pânico.
3. Dossiê com as três páginas (mistérios, pistas, próximo passo).
4. Cenas prontas (a vila para, o sino toca, visita do Diabo).
5. O Diretor (eventos automáticos raros) com interruptor.
6. Formas com modelo (entidades do elemento), quando o Modelador entregar.
7. Cenas próprias com linha do tempo.

## Perguntas abertas para o Nikolas
1. O nome **Véu** para o item do mestre serve? Alternativas: *Lanterna Cega*, *Máscara Branca* (cuidado: Máscaras remete ao mod antigo), *Página Branca*.
2. Quer que o mestre **veja os números** no painel dele (só ele, para controlar), ou prefere palavras também para ele? Proposta: só palavras.
3. O **Diretor** (mundo vivo) começa ligado ou desligado? Proposta: desligado, o Nikolas liga quando quiser.
4. Quanto o Diretor pode fazer quando o mestre está offline? Proposta: só poderes pequenos e uma ou duas cenas pequenas, nunca evento de entidade.
5. "Visto por um jogador só": tudo bem o mestre assustar **um** jogador sem o grupo ver? Proposta: sim, é o que faz a Lucidez baixa funcionar.
6. O botão de "não quero ser alvo de susto" por jogador: obrigatório desde o começo (proposta) ou só para servidor com público?
7. O Dossiê "mente" com Lucidez baixa: quanto, e em quais páginas? Proposta: só no "próximo passo" e em uma pista por vez, nunca apaga nada.
8. Cada elemento como temporada com pergunta grande: gosta? Quer que as temporadas sejam em ordem fixa ou o grupo escolhe?
9. Cenas nomeadas aqui (A vila para, O sino toca, A fila da noite, O lugar a mais, A voz do amigo): alguma tira do tom? Quer propor outras?
10. A **voz do amigo** (cena que imita a voz de um jogador real) é forte. Aceitável com consentimento, ou fora?
11. O Integrador precisa confirmar se AAA Particles e Player Animator estão no pack atual e se têm API estável para o compat. Quer que eu peça isso agora?

### Perguntas sobre as Formas (1.6)
12. Quais são "as 4 entidades" das formas prontas? O documento só nomeia Diabo, Magarefe e Convidada (e o Pendurado, do Ato 1). Quer que o quarto seja o Pendurado, ou outra? As de Conhecimento, Morte e Energia entram depois?
13. Se o mestre **desconectar** em forma, o que acontece quando ele volta? Entra já sem forma, ou volta na forma em que estava?
14. Se o mestre **morrer** em forma, o que aparece? A morte mostra a forma (a entidade "cai") ou o corpo normal? E ele renasce em forma ou sem?
15. Se o alvo do jogador "duplicado" **desconectar ou trocar de nome**, a cópia some, continua, ou o Kit avisa o mestre?
16. O jogador "duplicado" mostra a **skin real do alvo**? Ou uma versão levemente errada de propósito (o susto)? Aqui o modelo é de jogador, então vale conferir com a regra de "modelo da entidade".
17. Quem pode ver o **nome real** do mestre em forma? Só o próprio mestre? Outros mestres e admins? Algum registro (log) para o servidor?
18. O nome do duplicado aparece **igual** ao do alvo no TAB e no chat (dois jogadores com o mesmo nome) ou só no corpo? Duplicar o nome no chat pode enganar de verdade.
19. Em forma, o mestre **fala no chat** com o nome da forma, com nome nenhum, ou o chat fica bloqueado?
20. O mestre em forma pode **sofrer dano e atacar** normalmente, ou a forma é só de cena (sem luta)?
21. Os jogadores com "não quero ser alvo de susto" veem o mestre em forma mesmo assim, ou veem o mestre normal?
