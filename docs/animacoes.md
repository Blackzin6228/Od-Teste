# Animações dos sistemas do jogador

> **Status:** proposta da mesa Dev (08/11/2026). **No código (play test de Sangue, parte 1):** seções 1, 2 e 3
> (degraus, Lucidez e surto, Marca aparecer/trocar/subir), com tela, partícula e som, e animação de corpo
> **provisória** pelo Player Animator para Marca e surto, e a camada de veias da Marca de Sangue no corpo (provisória,
> desenhada em código, vista por todos). Ficou de fora, por ora: dessaturação/tela negativa (precisam
> de shader), névoa, granulado, posturas da Lucidez para os outros (cabeça olhando para trás, respiração),
> animação de corpo dos degraus, AAA Particles (usamos partículas vanilla), e as seções 4 e 5 (Vínculo e Transcender).
> A opção "Efeitos fortes de tela" existe (config do cliente e tela de configuração do mod) e vem **ligada**
> (pergunta 1 abaixo continua aberta).
> Decisão do Nikolas (08/11/2026): os sistemas reaproveitados do Máscaras ficam muito melhores e com muito mais animação.
> Tudo aqui é desenho nosso. Nada copiado de Ordem Paranormal ou de outra obra (animação, efeito, som, texto).
> As frases entre aspas são exemplos provisórios; a Lore pode trocar.

## Como ler cada animação
Cada momento tem quatro camadas:

| Camada | Ferramenta | Quem vê |
|---|---|---|
| **Corpo** | Player Animator (já no pack) | todos por perto, ou só o próprio jogador (indicado em cada caso) |
| **Tela** | overlay na HUD, tremor da câmera, névoa, vinheta, e shader de pós-processamento só onde está escrito | só o próprio jogador |
| **Partículas** | AAA Particles (já no pack) e, se ele não estiver, partículas vanilla parecidas | todos por perto, ou só o próprio jogador |
| **Som** | só sons vanilla, com pitch baixo | o próprio jogador, ou quem está perto |

## Regras técnicas (para quem for implementar)
1. **Sem dependência dura.** Player Animator e AAA Particles **não** são obrigatórios. O código deles fica em
   `compat/playeranimator/` e `compat/aaaparticles/`, atrás de `Compat.Integracao.X.ligada()`. Sem o mod, o momento
   acontece só com tela, partícula vanilla e som. Nada crasha.
2. **Tudo no cliente.** O servidor só manda **um pacote curto** quando algo muda ("subiu de degrau", "Marca apareceu").
   Animação, tela, partícula e som rodam no cliente. Nada roda em todo tick no servidor.
3. **Quem está perto** recebe o pacote só se estiver a até **32 blocos** (`PacketDistributor` por área), nunca o servidor inteiro.
4. **Pitch do som:** o Minecraft prende o pitch entre **0,5 e 2,0**. Pedir 0,4 toca em 0,5. Por isso a faixa usada
   aqui é **0,5–0,7**. Para soar mais grave que isso, tocar dois sons juntos (um grave e um eco baixo) em vez de baixar mais.
5. **Efeitos de tela com limite:** cada momento dura no máximo o tempo escrito. Tremor e flash obedecem a uma opção
   do cliente **"Efeitos fortes de tela: ligado / suave / desligado"** (acessibilidade: tontura e fotossensibilidade).
   No "suave", tremor pela metade e sem flash; no "desligado", só vinheta e som.
6. **Não travar o jogador sem aviso.** Animações que prendem o corpo (ajoelhar, cair) duram no máximo 3 segundos,
   são canceladas se o jogador tomar dano e **nunca** rodam em combate (dano recebido ou causado nos últimos 5 s):
   aí só toca a versão curta (tela + som).
7. **Lucidez** é aleatória, mas com relógio: o cliente sorteia um efeito a cada 20–60 s conforme a faixa, nunca dois ao mesmo tempo.
8. **Vínculo e Transcender** são cerimônias (feitas num rito): podem ser longas e mostrar o corpo inteiro para os outros.
9. Cada animação (arquivo do Player Animator) e cada efeito do AAA Particles ganha um ID `odteste:...` em `docs/ids.md`
   quando for criado. Os arquivos de animação são feitos pela mesa Modelador, com prévia para o Nikolas.

---

## 1. Exposição — subir de degrau
Sempre um momento curto, e sempre igual na estrutura: **pausa, golpe, eco**. Fica mais forte a cada degrau.
Fora de combate; em combate, só tela e som (regra 6).

| Degrau | Corpo (todos veem) | Tela (só você) | Partículas | Som |
|---|---|---|---|---|
| **Tocado** (20) | Cabeça vira de leve para um lado, como quem ouviu algo. 1 s. | Vinheta escura que entra e sai em 1 s. O som do mundo abafa por meio segundo. | Poucos pontos de poeira cinza parados no ar na frente do rosto (só você). | `ambient.cave` em pitch 0,6, baixo. |
| **Marcado** (40) | Ombros encolhem e a mão vai até o peito. 1,5 s. | Vinheta + tremor leve de 0,5 s. Barra de ação: "Alguma coisa olhou de volta." | Anel fino de poeira que sobe do chão em volta dos pés (todos veem). | `entity.warden.heartbeat` 0,6 duas vezes, com eco baixo para quem está perto. |
| **Fundo** (60) | Um joelho dobra e o jogador se apoia, depois levanta. 2 s. | Tela perde a cor (dessaturação por shader) por 2 s e volta devagar. Névoa se fecha e abre. | Fumaça escura rasteira que sai do jogador e se espalha 3 blocos. | `block.respawn_anchor.deplete` 0,5 + `ambient.soul_sand_valley.mood` 0,6. |
| **À beira** (80) | Corpo trava, braços abertos rígidos, cabeça para cima. 2,5 s. | Tremor médio, vinheta forte, por meio segundo a tela fica **negativa** (só no "ligado"). Um vulto aparece e some no canto. | Coluna de partículas escuras que sobe do jogador até 4 blocos; por um instante as partículas descem em vez de subir. | `entity.elder_guardian.curse` 0,5 baixo + `block.beacon.deactivate` 0,5. |
| **Atravessado** (100) | Só por evento. Jogador cai de joelhos, curva o corpo e levanta devagar com outra postura (cabeça inclinada). 3 s. | Tela escurece inteira por 1 s, volta com borda que pulsa no ritmo de um coração por 10 s. | Explosão lenta de partículas da cor da Marca (ou cinza, sem Marca) que fica pairando 5 s. | `block.end_portal.spawn` 0,5 baixo para todos até 32 blocos + `entity.warden.heartbeat` 0,5. |

## 2. Lucidez — cada faixa e o surto
Lucidez é **só sensação**: quase tudo é só para você. Os outros só notam no corpo (postura) e no surto.

| Faixa | Corpo (todos veem) | Tela (só você) | Partículas (só você) | Som (só você) |
|---|---|---|---|---|
| **Firme** (>70%) | Normal. | Nada. | Nada. | Nada. |
| **Inquieto** (40–70%) | Parado por 10 s, às vezes olha para trás sozinho (só cabeça, 1 s). | No escuro, a vinheta "respira" (abre e fecha devagar, a cada 6 s). | Um cisco de poeira passa rápido no canto da visão. | Sussurro: `ambient.cave` ou `ambient.soul_sand_valley.additions` 0,6, vindo de trás. |
| **Abalado** (15–40%) | Postura encolhida quando parado. Mão treme ao segurar arco (balanço leve do braço). | Mira treme de leve ao puxar o arco. Às vezes a tela inclina 1–2 graus por 1 s. | Partículas falsas: gotas, faíscas ou cinzas que caem só para você e não estão lá. | Passos falsos atrás (`block.gravel.step` / `block.wood.step` 0,7, 3 a 5 passos que param quando você vira). |
| **Quebrando** (1–15%) | Respiração pesada (peito sobe e desce, em loop curto). Cabeça vira sozinha para um vulto. | Vultos no canto que somem ao olhar. Escuridão curta (2 s) de vez em quando. Bordas da tela com granulado. | Uma silhueta de partículas escuras a 8–12 blocos, que se desfaz quando você chega a 5 blocos. | Sons de criatura falsos (`entity.enderman.stare` 0,5, `entity.zombie.ambient` 0,5) e o próprio coração (`entity.warden.heartbeat` 0,7, baixo). |
| **Surto** (0) | **Todos veem:** o jogador agarra a cabeça, cambaleia e cai de joelhos. 3 s (proposta "desmaio"; ver `[DECIDIR]` do surto em `docs/sistemas.md`). | Tela fica preta de fora para dentro em 1 s, 4 s de escuro com sons, depois abre devagar com dessaturação que some em 10 s. | Para os outros: poeira escura sai do jogador como um sopro. Para você: no escuro, pontos de luz da cor da sua Marca. | Só você: um zumbido (`block.beacon.ambient` 0,5) que cresce e corta seco. Quem está perto: `entity.player.hurt` 0,6 e um eco baixo. |

- **Voltar de faixa** (subir a Lucidez) também tem um momento pequeno: a tela clareia por meio segundo e toca
  `block.amethyst_block.resonate` 0,7 baixo. Ajuda o jogador a sentir que dormir ou ficar perto do grupo funcionou.
- Os efeitos da faixa só começam **5 s** depois de entrar nela, para não piscar quando a Lucidez oscila na borda.

## 3. Marca

### Aparecer (estágio 1)
Acontece sozinha (o jogador não escolhe; ver `docs/sistemas.md`). É o momento mais importante: precisa ser marcante
e deixar claro **qual** elemento foi, sem texto explicando.

- **Corpo (todos veem):** o jogador para, olha a própria mão (braço sobe até a frente do rosto), aperta o punho. 2,5 s.
- **Tela:** a borda da tela tinge da cor do elemento por 2 s. Barra de ação com uma frase curta do elemento.
- **Partículas:** as partículas do elemento (tabela abaixo) sobem do braço e se espalham pelo corpo, depois somem
  "para dentro" da pele. A skin ganha a camada do estágio 1 no fim da animação.
- **Som:** o som do elemento (tabela abaixo) + `block.respawn_anchor.charge` 0,6.

### Trocar sozinha de elemento (antes do Vínculo)
- **Corpo:** o jogador coça/aperta o braço, como incômodo. 1,5 s.
- **Tela:** as cores dos dois elementos piscam na borda, a antiga some.
- **Partículas:** as do elemento antigo caem do corpo como pó; as do novo sobem.
- **Som:** som do elemento antigo em pitch 0,5 baixo, depois o do novo.

### Subir de estágio (3 e 4; o 2 é o Vínculo, abaixo)
| Estágio | Corpo | Tela | Partículas | Som |
|---|---|---|---|---|
| **3 — Cicatriz** | Dobra o corpo para frente como quem sente dor, mão no braço marcado, depois endireita. 2 s. | Tremor curto, borda da cor do elemento pulsa 3 vezes. | Partículas do elemento correm pelo braço como se desenhassem a cicatriz. | Som do elemento + `entity.player.hurt_sweet_berry_bush` 0,5 baixo. |
| **4 — Entrega** | Braços abertos, cabeça para trás, o corpo levanta um pouco do chão (só visual) e desce. 3 s. | Tela da cor do elemento por 1 s, depois o mundo volta com um tom leve dessa cor por 30 s. | Explosão grande do elemento (AAA Particles) que fica girando em volta do jogador por 5 s. Todos até 32 blocos veem. | Som do elemento em pitch 0,5 + `block.end_portal_frame.fill` 0,5. |

### Cada elemento (assinatura visual e sonora)
Usado em todos os momentos da Marca, do Vínculo e do Transcender. Cores do `Elemento.java`.

| Elemento | Partículas | Tela | Corpo (detalhe na animação) | Som |
|---|---|---|---|---|
| **Sangue** (vermelho escuro) | Gotas pesadas que caem e mancham o chão por alguns segundos; névoa vermelha rente ao corpo. | Borda vermelha que pulsa como batimento. | Movimento brusco, punhos fechados, peito estufado. | `entity.warden.heartbeat` 0,6 + `block.honey_block.slide` 0,5 (som molhado). |
| **Conhecimento** (dourado claro) | Pequenos traços dourados que se juntam no ar como escrita e se apagam; pontos de luz em órbita lenta. | Por um instante, glifos dourados nossos atravessam a tela. | Movimento contido, mão aberta à frente, cabeça inclinada como quem lê. | `block.enchantment_table.use` 0,6 + `item.book.page_turn` 0,5. |
| **Morte** (preto/cinza) | Cinzas que caem **devagar demais**, como em câmera lenta; fumaça escura rasteira. | A tela perde a cor e o som do mundo fica mais lento por 1 s (efeito de "tempo arrastado"). | Movimento lento e pesado, ombros caídos, cabeça baixa. | `block.bell.resonate` 0,5 + `entity.wither.ambient` 0,5 bem baixo. |
| **Energia** (roxo forte) | Faíscas que pulam em direções aleatórias e somem; pequenos estalos de luz que mudam de lugar. | Tela "falha": por frações de segundo a imagem desloca (glitch), só no "ligado". | Movimento nervoso, tremido, dedos abrindo e fechando. | `entity.lightning_bolt.thunder` 0,5 muito baixo + `block.respawn_anchor.charge` 0,7. |

Os glifos e símbolos são **nossos** (desenhados pela mesa Modelador), nunca os sigilos da obra.

## 4. Vínculo (estágio 2 da Marca, feito num rito)
Cerimônia: o jogador aceita a Marca que já tem. Pode ser longa (5–8 s) e é vista por todos no rito.

- **Corpo (todos veem):** ajoelha no centro do rito, encosta a mão marcada no chão, a mão "puxa" algo do chão
  até o peito, levanta devagar. Se tomar dano no meio, a animação para e o rito falha sem gastar o componente.
- **Tela:** o mundo escurece fora do círculo do rito; a borda fica da cor do elemento e, no fim, um flash suave da cor.
  Barra de ação: uma frase curta do elemento ("O sangue lembra seu nome." como exemplo provisório).
- **Partículas:** as do elemento sobem do desenho do rito no chão em espiral **para cima e para dentro**, até o
  jogador, e entram no peito. No fim, um anel fica no chão por 10 s.
- **Som:** som do elemento + `block.beacon.activate` 0,5 + `block.conduit.activate` 0,5 baixo para quem está perto.
- Depois do Vínculo, a cada 2–3 minutos, uma partícula pequena do elemento aparece no braço por 1 s (só enfeite, no cliente).

## 5. Transcender (nível 1 a 4, num rito)
Cada nível é uma cerimônia e um preço. A animação precisa mostrar o **preço** (algo se perde), não só o poder.

| Nível | Corpo (todos veem) | Tela (só você) | Partículas | Som |
|---|---|---|---|---|
| **1** | De pé, braços ao lado, o corpo se curva para trás como puxado pelo peito. 3 s. | Tela da cor do elemento, depois um "rasgo" escuro de cima a baixo que fecha. A Lucidez máxima menor é sentida: a vinheta fica um pouco mais fechada para sempre (sutil). | Elemento sai do jogador para cima e volta mais escuro. | `block.end_portal.spawn` 0,6 baixo + som do elemento. |
| **2** | Como o 1, e no fim o jogador olha para os lados como quem ouviu chamarem. 4 s. | Rasgo maior; por 2 s os sons do mundo somem e só fica um zumbido. | Partículas do elemento ficam em volta por 10 s e caem como pó. | `ambient.basalt_deltas.mood` 0,5 + `block.beacon.deactivate` 0,6. |
| **3** | Ajoelha, mãos no chão, o chão em volta "afunda" (partículas), levanta com a habilidade nova. 5 s. | Tela negativa por meio segundo (só no "ligado"), tremor forte curto, depois a cor volta mais fria. | Círculo de partículas no chão que se abre para fora e some; criaturas de partícula surgem e somem na borda. | `entity.warden.emerge` 0,5 baixo + som do elemento. |
| **4** | Fim de jogo. Corpo sobe um pouco do chão, braços abertos, cabeça cai para o lado; desce com postura diferente para sempre (cabeça levemente inclinada quando parado). 6 s. | Tela inteira escura por 2 s com só o coração; volta com o tom do elemento por 60 s e uma marca fixa discreta no canto da HUD. | Coluna do elemento até o céu (até 32 blocos de altura), vista por todos até 64 blocos, por 5 s. | `entity.wither.spawn` 0,5 baixo para todos perto + `entity.warden.heartbeat` 0,5 para você. |

---

## Antes de implementar (perguntas para o Nikolas)
1. A opção "Efeitos fortes de tela" vem **ligada** ou **suave** por padrão?
2. Os outros jogadores veem a postura da Lucidez baixa (encolhido, respiração pesada)? Proposta: **sim**, é bom para
   roleplay, mas não mostra número.
3. A coluna do Transcender 4 pode ser vista por todos até 64 blocos, ou fica só para quem está no rito?
4. Prévia: a mesa Modelador faz as animações no Blockbench e mostra em vídeo/GIF antes de integrar.
