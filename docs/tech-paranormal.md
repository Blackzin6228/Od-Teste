# Tech paranormal (PROPOSTA da mesa Integrador, 2026-11-08; espera o Nikolas)

> Só documento. **Nenhum código, receita ou ID existe ainda.** Nomes de máquinas e materiais são provisórios
> (a mesa Lore dá o nome final) e só entram em `docs/ids.md` depois de aprovados.
> Itens de outros mods: nomes do Mekanism 10.7 e do Create 6 para 1.21.1. Onde está **(conferir)**, o ID exato ou o
> formato da receita precisa ser lido no jar do pack antes de virar JSON.

## 1. Ideia em uma frase
Quem joga tech no ATM10 (Mekanism, Create e afins) é **o técnico do Gabinete**: monta os aparelhos que **ouvem, avisam,
prendem e decifram** o paranormal. O grupo precisa dele na história, e a fábrica dele vira alvo do elemento Energia.
Tudo **sem grind**: cada máquina sai numa mesa de craft com peças que o jogador de tech já tem no degrau dele, e a
matéria paranormal passa por **1 ou 2 máquinas, nunca mais**.

> **Atenção:** o pedido "tech entra na lore sem virar grind" (2026-11-08) **ainda não está em `docs/decisoes.md`**.
> Este documento parte dele, mas quem registra a decisão é o Nikolas.

## 2. Regras deste ramo
1. **Cadeia curta.** No máximo **2 passos de máquina** entre o drop e o uso. Peças de outros mods sempre do degrau atual
   (D1 = começo, D2 = meio, D3 = fim do meio; ver `docs/progressao.md`).
2. **Energia FE, sem depender de mod.** FE é do próprio NeoForge (`IEnergyStorage`). As nossas máquinas aceitam
   qualquer gerador, cabo ou bateria do pack. Por isso **as máquinas funcionam mesmo sem Mekanism e sem Create**.
3. **Duas portas e uma reserva.** Toda receita tem versão **Mekanism** e versão **Create**, cada uma com
   `neoforge:conditions` + `neoforge:mod_loaded`. Sem nenhum dos dois, entra a **receita reserva**, mais cara, só com
   vanilla e o nosso mod.
4. **Nada de duplicar.** Toda máquina devolve **1 para 1** (ou menos). Nossas matérias **não entram** em tags `c:ores`,
   `c:dusts`, `c:raw_materials` nem `c:ingots`, para nenhuma cadeia de minério (5x do Mekanism, lavagem do Create,
   moedores de outros mods) multiplicar o que cai das criaturas.
5. **Relíquias, espadas e Artefatos Paranormais não têm craft.** Nada desta página cria, carrega ou melhora nenhum deles.
   As máquinas só **leem, avisam, prendem e decifram**.
6. **Ataque à base é reversível.** Nenhum evento quebra bloco, apaga item ou some com energia para sempre (seção 5).
7. **Custo de servidor baixo.** Checagem rara, só em chunk já carregado, com limite de blocos olhados (seção 5.4).

## 3. Matéria paranormal: do drop ao uso (1 ou 2 passos)

Vem de `docs/progressao.md` (seção 4): **Resíduo** (sobra genérica) e **Matéria de cada elemento** (nomes provisórios:
Sangue Coalhado, Lodo Frio, Tinta Dourada, Chama Líquida). Cai **só com jogador de verdade matando** (sem `FakePlayer`).

| Estado | O que é | Para que serve |
|---|---|---|
| **Matéria bruta** | o que cai da criatura | só oferenda de ritual (lore). **Não entra em nenhuma receita de máquina** |
| **Matéria estável** (passo 1) | bruta assentada numa máquina | máquinas D1 e D2 deste documento; entrada do Fragmento (D2) |
| **Matéria tratada** (passo 2) | estável com o tratamento do elemento | máquinas D3 (Câmara de Contenção); entrada da Essência (D3) |

### 3.1 Passo 1: estabilizar (igual para os 4 elementos)
| Porta | Máquina | Entrada → saída | Custo |
|---|---|---|---|
| Mekanism | **Câmara de Enriquecimento** (`mekanism:enriching`) | 1 bruta → 1 estável | o padrão da máquina (≈ 4.000 FE por item, 10 s) |
| Create | **Rodas de Moer** (`create:crushing`) | 1 bruta → 1 estável, **sem chance de sobra extra** | só rotação |
| Reserva (sem os dois) | **Estabilizador** (bloco nosso) | **2 brutas + 1 Resíduo** → 1 estável | 100 FE/t, 20 s (40.000 FE) |

### 3.2 Passo 2: tratar (um por elemento, só para D3)
O tratamento usa a máquina que combina com o elemento (tabela 5.1 de `docs/progressao.md`).

| Elemento | Porta Mekanism | Porta Create | Reserva (Estabilizador) |
|---|---|---|---|
| **Sangue** | Câmara de Reação Pressurizada: 1 estável + 100 mB de água + 50 mB de hidrogênio | Misturador **aquecido** (Queimador de Blaze): 1 estável + 250 mB de água | 2 estáveis + 1 Resíduo, 30 s |
| **Morte** | Câmara de Purificação: 1 estável + oxigênio | **Assombrar** (ventilador + fogo de alma) | idem |
| **Conhecimento** | Câmara de Injeção Química: 1 estável + cloreto de hidrogênio | **Prensa Mecânica** | idem |
| **Energia** | Infusor Metalúrgico: 1 estável + 20 de redstone | Energizador do Create New Age (2.000 FE) **(conferir modid)** | idem |

Sempre 1 → 1. Assim, **uma criatura morta = um item útil**, e o técnico da base é quem transforma o drop do grupo.

### 3.3 Exemplo de JSON (formato a conferir no jar)
```json
// data/odteste/recipe/compat/create/assombrar_lodo_frio.json
{
  "neoforge:conditions": [{ "type": "neoforge:mod_loaded", "modid": "create" }],
  "type": "create:haunting",
  "ingredients": [{ "item": "odteste:lodo_frio_estavel" }],
  "results": [{ "id": "odteste:lodo_frio_tratado", "count": 1 }]
}
```
```json
// data/odteste/recipe/compat/mekanism/estabilizar_chama_liquida.json
{
  "neoforge:conditions": [{ "type": "neoforge:mod_loaded", "modid": "mekanism" }],
  "type": "mekanism:enriching",
  "input": { "item": "odteste:chama_liquida_bruta", "count": 1 },
  "output": { "id": "odteste:chama_liquida_estavel", "count": 1 }
}
```

## 4. Máquinas do paranormal

Todas são **blocos nossos** que usam FE. Ficam **paradas** sem energia (não quebram, não somem). Nenhuma roda lógica
quando está desligada ou sem jogador perto (regra 7 do `docs/progressao.md`).

### 4.1 Receptor de Ondas Curtas (D1): "o rádio que capta o outro lado"
- **O que faz:** um dial de frequência (00.00 a 99.99). Em certas frequências e horários ele **pega transmissões**:
  vozes picotadas, números, matrículas, avisos. Texto na tela no estilo "picotado" da Rede (letras faltando) e som de estática.
- **Na história:** é o primeiro aparelho que o grupo precisa para ouvir **os que vieram antes**. A primeira frequência
  pode ser a matrícula de Bruna Tavares (02.04), a técnica que virou **O Dono da Casa** (`lore/entidades.md`).
- **Responder (perigo):** o Receptor tem um botão "transmitir". Responder ao chamado **sobe a Exposição** de quem aperta e
  aumenta a chance de evento na base (seção 5). Combina com a ideia do Dono da Casa: quem atende, entra. Ninguém é obrigado.
- **Para o narrador:** comando de admin `/sinal transmitir <frequência> <texto>` manda uma transmissão para todos os
  Receptores sintonizados. Serve para roleplay ao vivo.
- **Energia:** 20 FE/t ligado (400 FE/s). Bateria interna de 100.000 FE (≈ 4 min sem gerador).
- **Receita** (mesa de craft):

| Porta | Pede |
|---|---|
| Mekanism | 1 Circuito Básico + 4 lingotes de cobre + 1 bloco de notas + 1 para-raios + 1 vidro + **2 Matéria estável** (qualquer) |
| Create | 2 Tubos de Elétrons + 2 lingotes de latão + 1 bloco de notas + 1 para-raios + 1 vidro + **2 Matéria estável** |
| Reserva | 2 blocos de redstone + 4 blocos de cobre + 1 bloco de notas + 1 para-raios + **4 Matéria estável** |

### 4.2 Antena de Vigia (D2): "avisa quando tem aparição perto"
- **O que faz:** a cada **5 s** olha um raio de **48 blocos** procurando criaturas nossas (tag `odteste:paranormal`),
  aparições e eventos de Energia começando. Avisa por **som no Receptor ligado**, **mensagem curta para quem está a até
  64 blocos** e **sinal de redstone** (força = distância; dá para ligar alarme, luz, porta).
- **Aviso de evento:** dá **30 s de antecedência** antes de um evento de Energia (seção 5). É a defesa principal do técnico.
- **Mastros:** cada bloco de **Mastro** empilhado embaixo soma **+16 blocos** de raio, até **96**. Mastro = 4 barras de
  ferro + 1 lingote de cobre (sem matéria, barato de propósito).
- **Energia:** 40 FE/t + 10 FE/t por mastro (máx. 80 FE/t).
- **Receita:**

| Porta | Pede |
|---|---|
| Mekanism | 4 Ligas Infundidas + 1 Circuito Avançado + 1 para-raios + 2 barras de ferro + **2 Matéria estável de Energia** |
| Create | 4 lingotes de latão + 2 Mecanismos de Precisão + 1 para-raios + **2 Matéria estável de Energia** |
| Reserva | 4 blocos de cobre + 2 olhos do Ender + 1 para-raios + **4 Matéria estável de Energia** |

### 4.3 Câmara de Contenção (D3): "prende o que não pode andar solto"
- **O que faz:** bloco de 1x2 (tamanho de porta), com vidro na frente. Prende **uma** criatura nossa que esteja com
  **menos de 25% da vida** (empurrada para dentro ou puxada com a câmara aberta) ou uma **aparição de evento**.
- **Na história:** é como o grupo **guarda e interroga** o que capturou: a cada **5 min** contida, a criatura gera um
  **Registro de Contenção** (página para o Dossiê: fraqueza, origem, pista). **Máximo 3 por criatura**; depois ela
  **se apaga** sem drop. Também é onde a história pode exigir guardar o **fragmento** que a entidade deixa no evento.
- **Sem farm:** criatura contida **não dá drop**, não se reproduz, não pode ser morta por máquina, e só 1 câmara ocupada
  por chunk.
- **Se a energia acabar:** a porta abre e a criatura **sai viva** (vira um perigo normal na base). Nenhum bloco quebra.
- **Energia:** 500 FE/t **só ocupada** (10.000 FE/s; 15 min de contenção = 9 M FE). Bateria de 2 M FE (≈ 3 min de folga).
- **Receita:**

| Porta | Pede |
|---|---|
| Mekanism | 4 Ligas Reforçadas + 2 Circuitos de Elite + 4 Vidros Estruturais + 1 Carcaça de Aço + **4 Matéria tratada (uma de cada elemento)** |
| Create | 4 Chapas Resistentes + 2 Mecanismos de Precisão + 4 vidros + 2 lingotes de Allthemodium **(se o mod estiver)** + **4 Matéria tratada (uma de cada)** |
| Reserva | 4 blocos de obsidiana chorona + 4 blocos de ferro + 1 estrela do Nether + **8 Matéria tratada (duas de cada)** |

### 4.4 Toca-fitas e Decodificador (D1 e D2): "pistas que só tech decifra"
- **Toca-fitas (D1):** toca **Fitas Gravadas** limpas. Energia 10 FE/t enquanto toca.
  Receita: 1 toca-discos + 1 Circuito Básico **ou** 1 Tubo de Elétrons + **1 Matéria estável**.
- **Decodificador (D2):** encaixa no Toca-fitas (bloco do lado). Limpa fitas chiadas e lê sinais gravados.
  **200 FE/t por 15 s = 60.000 FE por fita** e **1 Matéria estável de Conhecimento**. Sai uma **Transcrição** (folha
  escrita que qualquer um lê e passa para o grupo). Com um **Processador Lógico do AE2** encaixado, fica 50% mais rápido
  (opcional, só se o AE2 estiver).
  Receita Mekanism: 1 Circuito Avançado + 1 Carcaça de Aço + **2 Matéria estável de Conhecimento** + 1 Toca-fitas.
  Receita Create: 1 Carcaça de Latão + 2 Mecanismos de Precisão + **2 Matéria estável de Conhecimento** + 1 Toca-fitas.
  Reserva: 4 blocos de lápis-lazúli + 2 livros + **4 Matéria estável de Conhecimento** + 1 Toca-fitas.

Detalhe das pistas na seção 6.

## 5. O paranormal ataca a base (eventos de Energia)

### 5.1 Quando acontece
- A cada **10 min** por jogador **online e não AFK**, o servidor sorteia. Jogador com **Exposição 0 nunca é alvo**
  (quem acabou de chegar fica em paz).
- Chance base **2%**, sobe com: estágio de Exposição (+1% por degrau), **Marca de Energia** (+3%), carregar Artefato de
  Energia (+2%), ter **respondido** no Receptor nas últimas 2 h (+3%), base com mais de **10 M FE** guardados (+2%). Teto **15%**.
  Na prática: um técnico bem exposto vê **1 evento a cada 1 ou 2 horas** de jogo; quem não mexe com o paranormal, quase nunca.
- Limites: **1 evento por base a cada 40 min**, **no máximo 6 por hora no servidor inteiro**, nunca dois ao mesmo tempo na mesma área.
- A Antena de Vigia avisa **30 s antes**. Sem antena, começa sem aviso.

### 5.2 Os três eventos (nomes provisórios)
| Evento | O que acontece | Duração | Como resolver | Se ignorar |
|---|---|---|---|---|
| **Fome da Linha** (dreno) | puxa até **25%** da energia de até **8 baterias/células** a até 32 blocos (teto **2 M FE** no evento). A energia fica **guardada no evento**, não some | até 5 min | achar e banir o **Fantasma de Sinal** que aparece na base | a energia **volta sozinha** em 10 min |
| **Apagão** (desliga) | segura a saída de energia das baterias da área e **desliga as máquinas nossas** (Receptor, Antena, Câmara). Sem bateria, a fábrica para. Luzes piscam (**só no cliente**, nenhum bloco muda) | 60 a 120 s | ligar o **Aterramento** (6.2) ou sintonizar o Receptor na frequência que a Antena mostra | volta tudo sozinho no fim do tempo |
| **Casa Ocupada** (assombra) | sons de máquina fora do ritmo, o Receptor diz o nome do jogador, vultos nas telas, **abalo pequeno de Lucidez** em quem está dentro. Aparece 1 **Desligado** (`lore/criaturas.md`) que **não quebra bloco** | 3 a 5 min | expulsar o Desligado | ele some no fim do tempo, sem drop |

Recompensa ao **resolver**: a energia volta na hora e cai **1 Matéria bruta de Energia** para cada jogador que ajudou (drop pessoal).
Ignorar não pune além do susto: **tudo volta como estava**.

> **Câmara de Contenção ocupada** fica de fora do Apagão (não solta criatura por evento). Só a falta de energia
> **de verdade** abre a porta. Assim o evento não vira armadilha injusta.

### 5.3 O que nunca acontece
- Nenhum bloco é quebrado, trocado ou movido. Nenhum item some de baú, máquina ou inventário.
- Nenhuma energia é perdida para sempre: o que o evento segura fica salvo (`SavedData` do mundo) e volta mesmo se o
  servidor reiniciar. Se o jogador quebrou a bateria no meio, o resto volta para outra bateria da área, se houver.
- Nada acontece em base de jogador **offline** nem em chunk **não carregado**.
- Config do servidor e comando de admin para **desligar** os eventos por jogador, por área ou no servidor todo.

### 5.4 Custo de servidor
- Sorteio: **1 vez a cada 10 min por jogador**, espalhado pelos ticks (pelo UUID), para não cair tudo no mesmo tick.
- Só quando sai evento: olha os **block entities** dos chunks **já carregados** a até 2 chunks do jogador (5x5), pega a
  capacidade de energia do NeoForge e **para em 64 blocos olhados**. Nenhum chunk é carregado por nós.
- Durante o evento: atualiza **1 vez por segundo**, só a lista de até 8 blocos escolhidos.
- Antena: **1 busca a cada 5 s** (caixa de até 96 blocos, só entidades com a tag `odteste:paranormal`).
- Nada roda sem jogador perto.

### 5.5 Defesas (sem grind)
- **Antena de Vigia:** aviso de 30 s.
- **Aterramento do Limiar** (D2): bloco que corta o dreno pela metade num raio de 16 blocos e encurta o Apagão em 50%.
  Um por área (não soma). Usa **Matéria de Conhecimento**, porque no ciclo **Conhecimento vence Energia**.
  Receita: 1 bloco de cobre + 1 Circuito Básico **ou** 1 Tubo de Elétrons + **4 Matéria estável de Conhecimento**.
- **Selo de Giz** (já proposto em `docs/progressao.md`): impede o Fantasma e o Desligado de nascerem dentro da área.

## 6. Pistas que só tech decifra
| Pista | Onde se acha | Como se lê | Custo |
|---|---|---|---|
| **Fita Gravada (limpa)** | estruturas, corpos dos que vieram antes, Dossiê | Toca-fitas | 10 FE/t enquanto toca |
| **Fita Gravada (chiada)** | idem, mais fundo nas dimensões | Toca-fitas + Decodificador → Transcrição | 60.000 FE + 1 Matéria estável de Conhecimento |
| **Sinal no ar** | Receptor sintonizado na frequência certa, na hora certa (a frequência vem de outra pista) | grava numa **Fita Virgem**, depois Decodificador → Transcrição | idem |
| **Sinal da Rede** | só com Antena de **96 blocos** (6 mastros) ligada ao Receptor | igual ao de cima | idem |

- **Fita Virgem:** 2 papéis + 1 lingote de ferro + 1 redstone → 2 fitas. Barata de propósito.
- **Fitas Gravadas não têm craft** (são achadas ou entregues pelo narrador). A Transcrição pode ser copiada como um livro.
- O mago lê o que o técnico decifra, e o técnico precisa da matéria que o grupo traz. Ninguém resolve sozinho.

## 7. Onde fica cada coisa no código (quando for aprovado)
- **Máquinas e eventos:** usam só NeoForge (energia FE e capacidades), então **não são compat**. Ficam nas pastas do
  mod (`blocos/`, `sistemas/`), feitas pela mesa Dev, e funcionam sem Mekanism e sem Create (regra 4).
- **Receitas dos dois mods:** só **dados**, em `data/odteste/recipe/compat/mekanism/` e `.../compat/create/`, cada uma
  com `neoforge:mod_loaded`. Reservas em `data/odteste/recipe/reserva/` com a condição `neoforge:not` dos dois.
- **Código de outro mod**, só se precisar:
  - `compat/create/`: (opcional, depois) Receptor que também roda por **rotação** do Create, sem FE.
  - `compat/ae2/`: encaixe do Processador Lógico no Decodificador. Pede `AE2("ae2")` novo em `Compat.Integracao`.
  - `compat/mekanism/`: nada previsto por enquanto. Preferimos dados.
- **Versões:** a compilação usa os jars do Conguitos-smp (Mekanism 10.7.19). Antes das receitas, conferir no ATM10 8.5
  as versões de Mekanism, Create, Create New Age e AE2 e os IDs marcados **(conferir)**.

## 8. Ligação com a lore
- `lore/roteiro.md` **ainda não existe**. Ganchos sugeridos para a mesa Lore usar quando escrever o roteiro:
  1. **Primeiro sinal:** o grupo monta o Receptor e ouve a matrícula 0204 em loop (Bruna Tavares, `lore/entidades.md`).
  2. **Primeira captura:** a história pede uma criatura contida para o Dossiê descobrir uma fraqueza.
  3. **A fita chiada:** uma pista importante de um dos que vieram antes só sai pelo Decodificador.
  4. **Entrada da Rede:** o "circuito" de `lore/dimensoes.md` pode ser o Receptor + Antena de 96 blocos ligados numa clareira.
- O **Rádio que Chia o Seu Nome** é Artefato (achado, sem craft) e **não é** o Receptor. Ver pergunta 5.

## 9. Resumo de custos
| Coisa | Degrau | Matéria | Energia |
|---|---|---|---|
| Estabilizar (passo 1) | D1 | 1 bruta → 1 estável | ≈ 4.000 FE (Mekanism) ou só rotação (Create) |
| Tratar (passo 2) | D3 | 1 estável → 1 tratada | padrão da máquina |
| Receptor | D1 | 2 estáveis | 20 FE/t |
| Toca-fitas | D1 | 1 estável | 10 FE/t |
| Antena de Vigia | D2 | 2 estáveis de Energia | 40 a 80 FE/t |
| Decodificador | D2 | 2 estáveis de Conhecimento (+1 por fita) | 60.000 FE por fita |
| Aterramento | D2 | 4 estáveis de Conhecimento | sem consumo |
| Câmara de Contenção | D3 | 4 tratadas (uma de cada) | 500 FE/t só ocupada |
| Kit completo (sem a Câmara) | D1–D2 | **≈ 11 Matérias** | cabe num gerador de começo |

## 10. Precisa do Nikolas
1. Registrar em `docs/decisoes.md` a decisão "tech entra na lore sem grind" (2026-11-08)?
2. **Matéria bruta só serve depois da máquina:** isso faz o jogador de magia depender do técnico. Pode, ou a magia ganha
   um caminho próprio (ex.: Altar do Malum também estabiliza)?
3. **Eventos na base:** a chance (1 evento a cada 1 ou 2 h para quem está exposto) está boa? E **Casa Ocupada** pode
   colocar o Desligado dentro da base (sem quebrar nada)?
4. **Responder no Receptor** sobe Exposição e chama evento. Gosta da tensão ou é castigo demais?
5. O Artefato **Rádio que Chia o Seu Nome** tem alguma ligação com o Receptor (ex.: colocado nele, pega frequências
   que ninguém mais pega)? Ou ficam separados?
6. A matéria deste documento (estável/tratada) entra **no lugar** da etapa tech do Fragmento e da Essência de
   `docs/progressao.md`, deixando aquelas receitas mais curtas? Sugestão: sim.
7. Nomes das máquinas e materiais → mesa Lore.
