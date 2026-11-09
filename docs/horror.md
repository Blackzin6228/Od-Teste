# Horror de Exposição e Lucidez

> Pedido do Nikolas depois do play test (08/11/2026): **mais horror**, nos 4 eixos abaixo. Esta é uma **proposta da Lore**:
> nada aqui está aprovado até virar linha em `docs/decisoes.md`. Quem implementa é o Dev (`docs/animacoes.md` tem a base).
> Inspiração só no gênero (medo lento, presença, o "algo está errado"). Nada copiado de outra obra.

## Regras do tom (valem para tudo)
1. **Devagar.** O começo é só desconfiança ("foi o vento?"). O terror vem no fim, e só para quem foi fundo.
2. **Sem jumpscare.** Nenhum som alto de repente, nenhum rosto na tela, nada que faça rir. Todo som **entra aos poucos** (2 s de subida) e **nunca passa de volume 0,6**.
3. **Presença, não susto.** O medo é a sensação de que algo está perto, olhando. Quase sempre o efeito **some quando você olha** ou **não deixa prova**.
4. **Nada de verdade acontece.** Toda alucinação é **só no cliente do jogador**: não muda o mundo, não dá dano, não abre porta real, não aparece pros outros. Isso protege o servidor e deixa o teste honesto.
5. **Sem número.** O jogador nunca vê o valor (já é regra de `docs/sistemas.md`).
6. **Frequência baixa e com intervalo mínimo.** Cada alucinação tem espera mínima (coluna "espera") e **nunca duas ao mesmo tempo**. Mais horror vem de **mais variedade e mais silêncio entre elas**, não de mais barulho.
7. **Grupo ajuda.** Com outro jogador a menos de 8 blocos, a frequência cai pela metade (combina com "grupo por perto" da Lucidez).

## Faixas usadas
- **Exposição** (degraus de `docs/sistemas.md`): Comum 0-19 = nada. **Baixa** = Tocado (20-39) · **Média** = Marcado (40-59) · **Alta** = Fundo (60-79) · **Crítica** = À beira (80-99) e Atravessado (100).
- **Lucidez** (`FaixaLucidez`): Firme (acima de 70%) = nada. **Alta** = Inquieto (40-70%) · **Média** = Abalado (15-40%) · **Baixa** = Quebrando (1-15%) · **Surto** = 0.

## Sobre o som
- **Só sons vanilla**, tocados no cliente, com **pitch entre 0,4 e 0,7** (grave, arrastado).
- **Eco:** o vanilla não tem reverb, então o eco é feito tocando o mesmo som de novo 6 e 14 ticks depois, com volume 50% e 25% do anterior e pitch 0,05 mais baixo (ainda dentro de 0,4-0,7).
- Candidatos vanilla (o Dev confirma o ID no pack): `ambient.cave`, `ambient.soul_sand_valley.mood`, `entity.warden.heartbeat`, `entity.warden.nearby_close`, `entity.warden.listening`, `block.sculk_sensor.clicking`, `block.sculk_shrieker.shriek` (só em pitch 0,4 e volume baixo), `entity.enderman.stare` (**não**: é agudo e vira susto; evitar), `entity.elder_guardian.curse` (só Crítica, volume 0,3), `block.wooden_door.open/close`, `block.gravel.step`, `block.stone.step`, `block.grass.step`, `entity.ghast.ambient` (pitch 0,4), `block.bell.resonate` (pitch 0,4, Surto).

---

# 1. Exposição

Tema: **o paranormal te conhece.** Cada faixa é "algo percebe você" em um grau maior.

## 1.1 Alucinações (Exposição)

| Faixa | O que acontece | Espera mín. | Some se... |
|---|---|---|---|
| **Baixa** (Tocado) | Um som que só você ouve, longe: um passo na grama, um clique de pedra. Nada na tela. | 4 min | Sempre some sozinho. |
| **Média** (Marcado) | **Passos atrás de você** que acompanham o seu ritmo e **param quando você para**. Uma **porta abre sozinha** (só na sua tela, perto, sem som de mob) e volta a fechar. | 3 min | Você olha a porta ou se vira. |
| **Alta** (Fundo) | **Vulto no canto da visão** (silhueta escura e alta, parada a 12+ blocos, só no cliente) que **não existe quando você olha**. Um **bloco** perto (tocha, baú, bancada) **parece trocar de lugar ou de tipo** por 3 s. Seu **nome sussurrado** no chat só pra você, sem remetente. | 2 min | Você olha direto ou chega a 4 blocos. |
| **Crítica** (À beira / Atravessado) | **Mob falso** (um aldeão, um jogador sem nome) parado de costas, longe; **some quando você olha** e reaparece mais perto, em outro lugar. O **nome sussurrado** vem junto de uma segunda linha ("Já era hora."). Blocos mudam em grupo (um corredor parece mais comprido). | 90 s | Nunca persegue nem ataca. Só observa. |

## 1.2 Tela e som (Exposição)

| Faixa | Tela | Som |
|---|---|---|
| **Baixa** | Nenhuma. | Um sussurro grave a cada tantos minutos (`ambient.cave`, pitch 0,6, vol 0,3, eco). |
| **Média** | Vinheta bem leve nas bordas, só à noite ou em lugar fechado. | `block.sculk_sensor.clicking` distante, pitch 0,5. |
| **Alta** | Cor **some devagar** (dessaturação de 20%), vinheta que respira. Leve distorção só na borda. | Batimento lento (`entity.warden.heartbeat`, pitch 0,5, vol 0,35) quando um vulto está perto. Sussurros com eco. |
| **Crítica** | Dessaturação de 45%, borda ondulando, **ruído fino** como TV fora do ar (só nas bordas). Nada cobre o centro, para você poder jogar. | Batimento contínuo e lento, `entity.warden.nearby_close` pitch 0,4 em loop raro, sussurros com eco duplo. |

## 1.3 Corpo (Exposição)

| Faixa | Corpo |
|---|---|
| **Baixa** | Nada no corpo. Só a **primeira marca pálida** na pele (a Marca começa, ver `docs/sistemas.md` seção 3). |
| **Média** | **Mão tremendo** de leve quando você segura um item paranormal parado. |
| **Alta** | **Visão balançando** bem pouco ao parar (como respirar fundo). **Marcas na pele** (camada da Marca) mais visíveis. |
| **Crítica** | **Lentidão sutil** (−5% de velocidade, nunca mais que isso) quando um vulto está perto. Marcas na pele **pulsam** devagar, no ritmo do batimento. |

## 1.4 Acessibilidade (`efeitos_fortes`) para Exposição

`efeitos_fortes` tem LIGADO / SUAVE / DESLIGADO (`ConfigCliente.Forca`; o tooltip já diz: DESLIGADO deixa só vinheta e som).

| Efeito | LIGADO | SUAVE | DESLIGADO |
|---|---|---|---|
| Passos atrás | Completo | Completo, mais raros | Só o som (já é só som) |
| Porta que abre sozinha | Completo | Só o som da porta, sem mudar o bloco | Nada |
| Vulto no canto | Completo | Mais transparente, some em 1 s | Nada |
| Bloco que parece mudar | Completo | Troca por 1 s, só tipo parecido | Nada |
| Mob falso | Completo | Só a silhueta parada, não muda de lugar | Nada |
| Nome sussurrado no chat | Completo | Completo | **Mantém** (é texto, não é efeito de tela) |
| Dessaturação / distorção | Completo | Metade | Só a vinheta |
| Ruído fino nas bordas | Completo | Metade | Nada |
| Batimento / sussurros | Completo | Completo | Mantém (som) |
| Mão tremendo / visão balançando | Completo | Metade | Nada |
| Lentidão sutil | Completo | Completo | Mantém (não é efeito de tela e é só −5%) |
| Pulsar das marcas | Pulsa | Sem pulsar | Sem pulsar |

---

# 2. Lucidez

Tema: **a mente se solta.** Exposição é "algo te vê". Lucidez é "você não confia no que vê". Por isso a Lucidez mexe mais com **o que parece real**.

## 2.1 Alucinações (Lucidez)

| Faixa | O que acontece | Espera mín. | Some se... |
|---|---|---|---|
| **Alta** (Inquieto) | Sussurros raros. A tela "respira" no escuro. Um **passo** ou **estalo** logo atrás, uma vez. | 3 min | Sempre sozinho. |
| **Média** (Abalado) | **Passos atrás** mais claros. **Porta que abre** quando você vira as costas. **Bloco que parece mudar** (a porta que você fechou aparece aberta). Sombras se mexendo na borda (partículas só suas). | 2 min | Você olha direto. |
| **Baixa** (Quebrando) | **Mob falso** (zumbi, criatura de Sangue) que parece vir até você e **some a 3 blocos**. **Vulto** que **repete o seu movimento** com atraso. Sons de criatura que não existe. **Seu nome** sussurrado com a **sua própria voz** (texto no chat, em itálico). | 75 s | Some a 3 blocos ou ao olhar. Nunca causa dano. |
| **Surto** | Por 5 s o mundo "cede": tudo escurece, **vários sons falsos juntos**, o corredor parece fechar. É o único momento em que há mais de uma alucinação ao mesmo tempo. Termina com silêncio e o sino grave. (Combina com o desmaio proposto em `docs/sistemas.md` seção 2.) | só ao zerar | Termina sozinho. |

## 2.2 Tela e som (Lucidez)

| Faixa | Tela | Som |
|---|---|---|
| **Alta** | A vinheta respira, bem leve. | Sussurro raro (`ambient.cave` pitch 0,6, vol 0,3, eco). |
| **Média** | Cor some um pouco (15%). A imagem **treme de leve** ao mirar com arco. | Passos falsos (`block.gravel.step`, `block.stone.step`, pitch 0,6) com eco. |
| **Baixa** | Cor some mais (40%). Distorção nas bordas que **respira**. Escuridão curta a cada tanto. | Batimento (`entity.warden.heartbeat` pitch 0,5), sussurros em duas vozes, `entity.ghast.ambient` pitch 0,4 distante. |
| **Surto** | Escurece até quase preto (nunca preto total: o jogador vê um tiquinho do mundo), borda tremendo. | Silêncio de 1 s, depois o batimento sobe até 0,6 e some. `block.bell.resonate` pitch 0,4 no fim. |

## 2.3 Corpo (Lucidez)

| Faixa | Corpo |
|---|---|
| **Alta** | Nada. |
| **Média** | **Mão tremendo** ao mirar e ao usar item. |
| **Baixa** | **Visão balançando** de leve ao andar. **Lentidão leve** (−8%) quando as luzes estão fracas. |
| **Surto** | Cegueira + Lentidão forte por 5 s (já proposto), o jogador cai de joelhos na animação provisória. Depois, a Lucidez volta a 15%. |

(Os números de lentidão são sugestão; o Dev ajusta e depois me conta para eu cuidar do texto.)

## 2.4 Acessibilidade (`efeitos_fortes`) para Lucidez

| Efeito | LIGADO | SUAVE | DESLIGADO |
|---|---|---|---|
| Passos / sussurros falsos | Completo | Completo | Mantém (som) |
| Porta que abre sozinha | Completo | Só o som | Nada |
| Bloco que parece mudar | Completo | 1 s, tipo parecido | Nada |
| Mob falso | Completo | Silhueta parada, some em 1 s | Nada |
| Vulto que repete o movimento | Completo | Sem repetir, parado | Nada |
| Dessaturação / distorção | Completo | Metade | Só a vinheta |
| Mão tremendo / visão balançando | Completo | Metade | Nada |
| Mira treme (arco) | Completo | Metade | Nada |
| Lentidão leve | Completo | Completo | Mantém (pequena) |
| **Surto** | Escurece quase tudo + tremor | Escurece menos (pouco tremor); a Cegueira vira mais curta (3 s) | Vinheta pesada + som; **a cegueira e a lentidão continuam**, pois são regra de jogo e não efeito de tela (ver nota) |

> **Nota para o Dev e o Nikolas:** o Surto tem parte de regra (desmaio de 5 s). Acessibilidade só pode desligar o **efeito de tela**, não a regra, senão o jogo fica injusto para quem escolheu DESLIGADO. Sugestão: no DESLIGADO, trocar "tela preta" por vinheta pesada e manter a lentidão.

---

# 3. Os comandos `/exposicao` e `/lucidez` sem número

Hoje o comando mostra número (útil para admin). **Proposta:** o jogador, ao rodar o comando **sem argumento**, recebe **uma frase sombria da faixa dele**; o admin continua vendo número com `/exposicao ver <jogador>` (já existe). A frase é sorteada entre 3 variações, nunca igual duas vezes seguidas.

### 3.1 Exposição

| Faixa | Variação 1 | Variação 2 | Variação 3 |
|---|---|---|---|
| **Comum** | "Nada olhou para você. Ainda." | "O lugar está quieto demais." | "Você ainda é só mais um." |
| **Baixa** | "Você escutou alguma coisa. Ela escutou você." | "Algo anotou o seu nome." | "O silêncio já não é de graça." |
| **Média** | "Há passos que combinam com os seus." | "As portas lembram de você." | "Você já não anda sozinho." |
| **Alta** | "Tem alguém no canto dos seus olhos. Continua lá." | "Eles sabem o seu cheiro." | "O que olha de volta já sabe o seu nome." |
| **Crítica** | "Já não é você que está procurando." | "A porta está aberta. Do outro lado, esperam por você." | "Quase lá. Quase deles." |

### 3.2 Lucidez

| Faixa | Variação 1 | Variação 2 | Variação 3 |
|---|---|---|---|
| **Firme** | "Os pés no chão. Por enquanto." | "O que você vê é o que existe." | "Respire. Está tudo no lugar." |
| **Alta** | "Algo está fora do lugar. Você não achou ainda." | "Você conferiu a porta duas vezes." | "O silêncio tem textura." |
| **Média** | "Você não confia mais no que ouve." | "A porta que você fechou está aberta." | "Seus passos têm companhia." |
| **Baixa** | "A sua voz sussurra do outro lado do quarto." | "Você já não sabe quem te chama." | "O que é real se afasta de você." |
| **Surto** | "O mundo cedeu." | "Não há chão. Só o que você acha que há." | "Você se perdeu e não achou o caminho." |

Cuidados: as frases são **do personagem**, não da obra. Nenhuma cita número, "nível" ou "pontos".

### 3.3 Símbolos nossos (ideias, o Modelador faz a prévia)
Regras: nada de pentagrama, nada parecido com o símbolo do Medo ou da Cruz de Sangue do guia de estilo. Formas simples, que cabem em 16x16.
- **Exposição:** **"Fresta"** (nome provisório do símbolo): um círculo fechado com uma **fenda vertical aberta** no meio, como uma porta entreaberta ou um olho que não fecha. A fenda **aumenta** por faixa (quase fechada → aberta).
- **Lucidez:** **"Fio"**: uma linha vertical **dividida em três segmentos** com espaços entre eles. Em Firme, o fio é inteiro. A cada faixa, **mais um corte** e o segmento de cima desvia um pixel para o lado. No Surto, os três segmentos ficam soltos.
- Uso: ícone no Dossiê ao lado da frase; opcional como marca d'água fraca na tela de Surto.

Nota: "Fresta" também aparece como nome de proposta no commit `f7c6195`; se o Nikolas escolher aquele sistema com esse nome, trocamos o símbolo para "Frincha".

---

# 4. Textos de lang sugeridos

Chaves novas, para `pt_br.json` e `en_us.json` (o Dev cola). A numeração 1-3 é para o sorteio. Prefixo `odteste.horror.`.

## pt_br
```json
"odteste.horror.exposicao.comum.1": "Nada olhou para você. Ainda.",
"odteste.horror.exposicao.comum.2": "O lugar está quieto demais.",
"odteste.horror.exposicao.comum.3": "Você ainda é só mais um.",
"odteste.horror.exposicao.baixa.1": "Você escutou alguma coisa. Ela escutou você.",
"odteste.horror.exposicao.baixa.2": "Algo anotou o seu nome.",
"odteste.horror.exposicao.baixa.3": "O silêncio já não é de graça.",
"odteste.horror.exposicao.media.1": "Há passos que combinam com os seus.",
"odteste.horror.exposicao.media.2": "As portas lembram de você.",
"odteste.horror.exposicao.media.3": "Você já não anda sozinho.",
"odteste.horror.exposicao.alta.1": "Tem alguém no canto dos seus olhos. Continua lá.",
"odteste.horror.exposicao.alta.2": "Eles sabem o seu cheiro.",
"odteste.horror.exposicao.alta.3": "O que olha de volta já sabe o seu nome.",
"odteste.horror.exposicao.critica.1": "Já não é você que está procurando.",
"odteste.horror.exposicao.critica.2": "A porta está aberta. Do outro lado, esperam por você.",
"odteste.horror.exposicao.critica.3": "Quase lá. Quase deles.",
"odteste.horror.lucidez.firme.1": "Os pés no chão. Por enquanto.",
"odteste.horror.lucidez.firme.2": "O que você vê é o que existe.",
"odteste.horror.lucidez.firme.3": "Respire. Está tudo no lugar.",
"odteste.horror.lucidez.alta.1": "Algo está fora do lugar. Você não achou ainda.",
"odteste.horror.lucidez.alta.2": "Você conferiu a porta duas vezes.",
"odteste.horror.lucidez.alta.3": "O silêncio tem textura.",
"odteste.horror.lucidez.media.1": "Você não confia mais no que ouve.",
"odteste.horror.lucidez.media.2": "A porta que você fechou está aberta.",
"odteste.horror.lucidez.media.3": "Seus passos têm companhia.",
"odteste.horror.lucidez.baixa.1": "A sua voz sussurra do outro lado do quarto.",
"odteste.horror.lucidez.baixa.2": "Você já não sabe quem te chama.",
"odteste.horror.lucidez.baixa.3": "O que é real se afasta de você.",
"odteste.horror.lucidez.surto.1": "O mundo cedeu.",
"odteste.horror.lucidez.surto.2": "Não há chão. Só o que você acha que há.",
"odteste.horror.lucidez.surto.3": "Você se perdeu e não achou o caminho.",
"odteste.horror.sussurro.nome": "%s…",
"odteste.horror.sussurro.nome.2": "Já era hora, %s.",
"odteste.horror.sussurro.voz": "…%s, vem…",
"odteste.configuration.efeitos_fortes.tooltip": "Tremor, inclinação e alucinações visuais (vultos, portas, blocos). SUAVE corta pela metade; DESLIGADO deixa só vinheta, som e texto."
```

## en_us
```json
"odteste.horror.exposicao.comum.1": "Nothing has looked at you. Yet.",
"odteste.horror.exposicao.comum.2": "The place is too quiet.",
"odteste.horror.exposicao.comum.3": "You are still just one of many.",
"odteste.horror.exposicao.baixa.1": "You heard something. It heard you.",
"odteste.horror.exposicao.baixa.2": "Something wrote down your name.",
"odteste.horror.exposicao.baixa.3": "Silence is no longer free.",
"odteste.horror.exposicao.media.1": "There are footsteps that match yours.",
"odteste.horror.exposicao.media.2": "The doors remember you.",
"odteste.horror.exposicao.media.3": "You no longer walk alone.",
"odteste.horror.exposicao.alta.1": "Someone stands at the edge of your sight. They are still there.",
"odteste.horror.exposicao.alta.2": "They know your scent.",
"odteste.horror.exposicao.alta.3": "What looks back already knows your name.",
"odteste.horror.exposicao.critica.1": "You are no longer the one searching.",
"odteste.horror.exposicao.critica.2": "The door is open. On the other side, they are waiting.",
"odteste.horror.exposicao.critica.3": "Almost there. Almost theirs.",
"odteste.horror.lucidez.firme.1": "Feet on the ground. For now.",
"odteste.horror.lucidez.firme.2": "What you see is what exists.",
"odteste.horror.lucidez.firme.3": "Breathe. Everything is in its place.",
"odteste.horror.lucidez.alta.1": "Something is out of place. You haven't found it yet.",
"odteste.horror.lucidez.alta.2": "You checked the door twice.",
"odteste.horror.lucidez.alta.3": "The silence has a texture.",
"odteste.horror.lucidez.media.1": "You no longer trust what you hear.",
"odteste.horror.lucidez.media.2": "The door you closed is open.",
"odteste.horror.lucidez.media.3": "Your steps have company.",
"odteste.horror.lucidez.baixa.1": "Your own voice whispers from across the room.",
"odteste.horror.lucidez.baixa.2": "You no longer know who is calling you.",
"odteste.horror.lucidez.baixa.3": "What is real drifts away from you.",
"odteste.horror.lucidez.surto.1": "The world gave way.",
"odteste.horror.lucidez.surto.2": "There is no floor. Only what you think is there.",
"odteste.horror.lucidez.surto.3": "You are lost and cannot find the way.",
"odteste.horror.sussurro.nome": "%s…",
"odteste.horror.sussurro.nome.2": "About time, %s.",
"odteste.horror.sussurro.voz": "…%s, come…",
"odteste.configuration.efeitos_fortes.tooltip": "Camera shake, tilt and visual hallucinations (figures, doors, blocks). SUAVE halves it; DESLIGADO leaves only vignette, sound and text."
```

(As duas últimas chaves `...tooltip` já existem nos arquivos de lang: substituir o texto, não duplicar.)

---

# 5. Para o Dev (resumo técnico)
- Tudo no cliente (`cliente/`), sorteado por faixa, com espera mínima e **trava global**: só uma alucinação por vez (exceto Surto).
- O servidor continua mandando só a faixa (como hoje); a frase do comando é escolhida no servidor e enviada como chave de lang (`Component.translatable`), assim cada cliente a lê no seu idioma.
- Mob falso e vulto: entidade ou partícula **só do cliente**, nunca registrada no servidor, removida ao olhar (ângulo da câmera) ou depois do tempo.
- Bloco que muda: atualização falsa de bloco no cliente, sempre voltando ao real em 3 s; nunca em baú aberto, fornalha ligada ou lugar onde o jogador clica.
- Respeitar `efeitos_fortes` em cada linha das tabelas 1.4 e 2.4.
- Testes de Java puro: faixa → conjunto de alucinações permitidas, espera mínima, e DESLIGADO nunca dispara efeito de tela.

# 6. Perguntas para o Nikolas
1. As frases sombrias dos comandos estão no tom? Prefere mais curtas?
2. Tudo de Surto bem? Mantemos o desmaio de 5 s mesmo com `efeitos_fortes` desligado?
3. Mob falso **de costas** (Crítica) assusta no ponto certo, ou é "demais"?
4. Símbolos: aprova "Fresta" (Exposição) e "Fio" (Lucidez) como ideia para o Modelador?
