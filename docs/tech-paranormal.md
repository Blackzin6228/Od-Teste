# Tech paranormal (mesa Integrador; base decidida em 2026-11-08, detalhes ainda são proposta)

> **Decidido (2026-11-08, `docs/decisoes.md`): MÁQUINAS = MISTO.**
> 1. **Processar** matéria paranormal usa **as máquinas que já existem no pack** (Mekanism, Create, Industrial
>    Foregoing etc.), só com **receitas novas** (dados em `compat/`). **Nenhuma máquina nossa** para processar.
> 2. **Só 3 (no máximo 4) aparelhos nossos**, porque fazem o que nenhuma máquina faz: **Rádio**, **Antena** e
>    **Câmara de Contenção** (o 4º só se a história pedir). Energia **FE**, ligam em cabo de qualquer mod.
>    Acabamento **nível Mekanism**: modelo detalhado, animação e **som vanilla** (fichas na seção 4).
>
> Ainda é proposta: números, receitas, eventos e nomes (provisórios; a mesa Lore dá o nome final).
> **Nenhum código, receita ou ID existe ainda.** IDs só entram em `docs/ids.md` depois de aprovados.

## 1. Ideia em uma frase
Quem joga tech no ATM10 é **o técnico do Gabinete**: usa a fábrica que já tem para tratar a matéria que o grupo traz e
monta os aparelhos que **ouvem, avisam e prendem** o paranormal. A fábrica dele vira alvo do elemento Energia.
Tudo **sem grind**: a matéria passa por **1 ou 2 máquinas, nunca mais**.

## 2. Regras deste ramo
1. **Cadeia curta.** No máximo **2 passos de máquina** entre o drop e o uso. Peças de outros mods sempre do degrau
   atual (D1 = começo, D2 = meio, D3 = fim do meio; ver `docs/progressao.md`).
2. **Várias portas, mesmo resultado.** Cada passo tem receita em **mais de um mod** do pack (tabela 3.1). Todas dão o
   **mesmo** resultado, com custo parecido: nenhuma porta é atalho. Cada receita leva `neoforge:conditions` +
   `neoforge:mod_loaded`.
3. **Reserva sem tech.** Se **nenhum** mod de máquina estiver carregado, entra uma receita **de mesa de craft**, mais
   cara (3 brutas → 1 estável). Condição: `neoforge:not` + `neoforge:or` dos modids da tabela 3.1. Não é máquina nossa.
4. **Nada de duplicar.** Toda receita devolve **1 para 1** (ou menos), **sem saída extra por chance**. Nossas matérias
   **não entram** em tags `c:ores`, `c:dusts`, `c:raw_materials`, `c:ingots` nem `c:gems`, para nenhuma cadeia de
   minério (5x do Mekanism, lavagem do Create, moedores, Occultism) multiplicar o que cai das criaturas.
5. **Relíquias, espadas e Artefatos Paranormais não têm craft.** Nada desta página cria, carrega ou melhora nenhum deles.
6. **Ataque à base é reversível.** Nenhum evento quebra bloco, apaga item ou some com energia para sempre (seção 5).
7. **Custo de servidor baixo.** Checagem rara, só em chunk já carregado, com limite de blocos olhados (seção 5.4).

## 3. Matéria paranormal nas máquinas do pack

Vem de `docs/progressao.md` (seção 4): **Resíduo** (sobra genérica) e **Matéria de cada elemento** (nomes provisórios:
Sangue Coalhado, Lodo Frio, Tinta Dourada, Chama Líquida). Cai **só com jogador de verdade matando** (sem `FakePlayer`,
então o Triturador de Mobs do Industrial Foregoing e afins não geram drop nosso).

| Estado | O que é | Para que serve |
|---|---|---|
| **Matéria bruta** | o que cai da criatura | oferenda de ritual (lore). Entra **só** no passo 1 |
| **Matéria estável** (passo 1) | bruta assentada numa máquina | Rádio e Antena; entrada do Fragmento (D2) |
| **Matéria tratada** (passo 2) | estável com o tratamento do elemento | Câmara de Contenção; entrada da Essência (D3) |

### 3.0 Mods de máquina no pack (conferido em 2026-11-08)
Fonte: `index.toml` do pack Conguitos ATM10 publicado (`conguitos-atm10-pack.vercel.app`). Os jars em `libs/` ainda
são do Conguitos-smp antigo (só Mekanism 10.7.19 e Create 6.0.10). **Não consegui abrir os jars** nesta sessão, então
**o tipo de receita e o formato do JSON de todos os mods abaixo ainda precisam ser lidos no jar do ATM10** antes de
virar arquivo.

| Mod | No pack? | Usamos para |
|---|---|---|
| Mekanism (+ Generators, More Machine) | **sim** (`mekanism`, `mekanism-generators`, `mekanism-more-machine`) | passo 1 e passo 2 |
| Create | **sim** (`create`) | passo 1 e passo 2 |
| Create Crafts & Additions | **sim** (`createaddition`) | passo 2 de Energia (carregar) |
| Industrial Foregoing (+ Souls) | **sim** (`industrial-foregoing`, `industrial-foregoing-souls`) | passo 2 de Conhecimento e Morte |
| Powah | **sim** (`powah-rearchitected`) | passo 2 de Energia (Orbe Energizante) |
| Immersive Engineering | **sim** (`immersive-engineering`) | passo 1 (Triturador) |
| Modern Industrialization | **sim** (`modern-industrialization`) | passo 1 (Macerador) |
| Oritech | **sim** (`oritech`) | passo 1 (Pulverizador) |
| Ender IO | **sim** (`ender-io`) | passo 1 (Moinho SAG), **sem** saída extra |
| Actually Additions, PneumaticCraft, Integrated Dynamics, Extreme Reactors, AE2 | sim | **nada por agora** (AE2 só no Rádio, opcional) |
| Create New Age | **não achei** no índice | saiu do plano (o Create Crafts & Additions cobre Energia) |
| Evolved Mekanism | **não achei** | nada |
| Thermal (Expansion/Foundation) | **não achei** | nada |
| Occultism (Moedor de espírito) | sim | **não usar**: ele multiplica moagem. Matéria fica fora das tags dele |

### 3.1 Passo 1: estabilizar (igual para os 4 elementos, 1 bruta → 1 estável)
| Mod | Máquina | Tipo de receita | Custo |
|---|---|---|---|
| Mekanism | Câmara de Enriquecimento | `mekanism:enriching` **(conferir)** | padrão da máquina |
| Create | Rodas de Moer | `create:crushing` **(conferir)**, só 1 saída, sem chance extra | rotação |
| Immersive Engineering | Triturador | `immersiveengineering:crusher` **(conferir)**, sem saída secundária | padrão |
| Modern Industrialization | Macerador | `modern_industrialization:macerator` **(conferir)** | padrão |
| Oritech | Pulverizador | `oritech:pulverizer` **(conferir)** | padrão |
| Ender IO | Moinho SAG | `enderio:sag_milling` **(conferir)**, `bonus: none`, sem bola de moagem que dobre | padrão |
| Reserva (nenhum dos acima) | mesa de craft | `minecraft:crafting_shapeless` | **3 brutas → 1 estável** |

### 3.2 Passo 2: tratar (um por elemento, só para D3; 1 estável → 1 tratada)
A máquina combina com o elemento (tabela 5.1 de `docs/progressao.md`). Pelo menos **duas portas** por elemento.

| Elemento | Mekanism | Create | Outra porta |
|---|---|---|---|
| **Sangue** | Câmara de Reação Pressurizada (`mekanism:reaction`): + água + hidrogênio | Misturador **aquecido** (`create:mixing`, `heat_requirement: heated`) + 250 mB de água | — |
| **Morte** | Câmara de Purificação (`mekanism:purifying`) + oxigênio | **Assombrar** (`create:haunting`, ventilador + fogo de alma) | Industrial Foregoing: Câmara de Dissolução + **Éter Líquido** pouco (conferir fluido) |
| **Conhecimento** | Câmara de Injeção Química (`mekanism:injecting`) + cloreto de hidrogênio | Prensa Mecânica (`create:pressing`) | Industrial Foregoing: Câmara de Dissolução + **Essência** (XP líquido), `industrialforegoing:dissolution_chamber` |
| **Energia** | Infusor Metalúrgico (`mekanism:metallurgic_infusing`) + 20 de redstone | Bobina Tesla do Create Crafts & Additions (`createaddition:charging`) | Powah: Orbe Energizante (`powah:energizing`) |

Todos os tipos acima: **(conferir no jar do ATM10)**. Se algum não existir, a porta cai e as outras ficam.
Uma criatura morta = um item útil, e o técnico é quem transforma o drop do grupo.

### 3.3 Onde ficam as receitas
- `src/main/resources/data/odteste/recipe/compat/<mod>/...json`, uma pasta por mod, cada arquivo com
  `"neoforge:conditions": [{ "type": "neoforge:mod_loaded", "modid": "<modid>" }]`.
- Reserva em `.../recipe/reserva/` com `neoforge:not` + `neoforge:or` dos modids do passo 1.
- **Só dados**: nenhum código Java para processar matéria. Modids exatos (`immersiveengineering`, `industrialforegoing`,
  `modern_industrialization`, `createaddition`, `powah`, `oritech`, `enderio`) **(conferir no `neoforge.mods.toml` de cada jar)**.

Exemplo (formato a conferir):
```json
// data/odteste/recipe/compat/create/assombrar_lodo_frio.json
{
  "neoforge:conditions": [{ "type": "neoforge:mod_loaded", "modid": "create" }],
  "type": "create:haunting",
  "ingredients": [{ "item": "odteste:lodo_frio_estavel" }],
  "results": [{ "id": "odteste:lodo_frio_tratado", "count": 1 }]
}
```

## 4. Os aparelhos nossos (3, talvez 4) e as fichas para o Modelador

Todos são **blocos nossos** com FE do próprio NeoForge (`IEnergyStorage`): aceitam cabo, gerador e bateria de qualquer
mod e funcionam sem Mekanism e sem Create. Sem energia, ficam **parados** (não quebram, não somem). Nenhum roda lógica
desligado ou sem jogador perto. Código fica fora de `compat/` (só usa NeoForge), feito pela mesa Dev.

**Padrão de acabamento para os três (nível Mekanism):**
- Modelo GeckoLib detalhado (peças em relevo, parafusos, grades, cabos, vidro com profundidade), textura 16x por
  padrão (resolução ainda pendente em `docs/decisoes.md`).
- Estados visuais: **desligado** (sem energia), **em espera** (com energia, sem fazer nada), **trabalhando**.
  Luzes/telas emissivas só ligadas. Face de entrada de energia marcada (conector em cobre).
- **Som só vanilla** (lista em cada ficha), tocado por nós com volume e tom ajustados. Nada de som de outra obra.
- Item no inventário mostra o modelo em miniatura (render do bloco), com a mesma pose do estado "desligado".
- Estilo: aparelho de órgão de investigação, gasto, remendado, cobre e metal pintado; detalhe do elemento Energia
  (amarelo/azul elétrico) só onde o paranormal "vaza". **Nada de símbolo ou sigilo copiado de outra obra.**

### 4.1 Ficha: Rádio de Ondas Curtas (D1) — "capta o outro lado e decifra fitas"
- **Função:** dial de frequência (00.00 a 99.99) que pega **transmissões** do outro lado em certas frequências e
  horários (texto "picotado" na tela + estática). Tem **gaveta de fita**: toca Fitas Gravadas limpas e **decifra
  fitas chiadas** (vira Transcrição). Grava sinal no ar numa Fita Virgem. Botão **transmitir** (responder ao
  chamado sobe a Exposição e a chance de evento na base; ninguém é obrigado). Junta o antigo Receptor, Toca-fitas e
  Decodificador num só bloco.
- **Tamanho:** **1 bloco** (rádio de bancada grande), ocupa de 1x1 com caixa até ~14x12x10 px. Vira para o jogador.
- **Partes que animam:** dial de sintonia (gira ao sintonizar); ponteiro na janela de frequência (corre de lado);
  2 mostradores de agulha (sinal e energia; a de sinal treme com estática); **carretéis da fita** (giram ao tocar,
  mais rápido ao decifrar); tampa da gaveta (abre/fecha); botão transmitir (afunda); antena telescópica de cima
  (estica quando há sinal forte); tela pequena com chiado (textura animada, emissiva).
- **Estados:**
  - desligado: tela escura, agulhas no zero, antena recolhida;
  - em espera: tela com chiado fraco, luz âmbar acesa, agulha de energia parada;
  - sintonizado/recebendo: agulha de sinal pula, antena estica, luz verde pisca no ritmo da voz;
  - decifrando: carretéis rápidos, luz vermelha "gravando";
  - transmitindo: todas as luzes pulsam em vermelho, um tremor leve no aparelho (só visual).
- **Sons vanilla:** `block.lever.click` (dial, tom alto), `ui.button.click` (botão), `block.note_block.hat` e
  `block.sculk_sensor.clicking` em tom baixo (estática), `block.beacon.ambient` baixo (zumbido ligado),
  `block.iron_trapdoor.open/close` (gaveta), `block.note_block.bit` (bip de sinal achado).
- **Energia:** 20 FE/t ligado; **200 FE/t por 15 s** ao decifrar (60.000 FE + 1 Matéria estável de Conhecimento por fita).
  Bateria interna de 100.000 FE.
- **Extra opcional:** Processador Lógico do AE2 encaixado deixa a decifração 50% mais rápida (`compat/ae2/`).
- **Comando do narrador:** `/sinal transmitir <frequência> <texto>` para todos os Rádios sintonizados (roleplay ao vivo).
- **Receita (mesa de craft):** Mekanism: 1 Circuito Básico + 4 lingotes de cobre + 1 bloco de notas + 1 para-raios +
  1 vidro + **2 Matéria estável**. Create: 2 Tubos de Elétrons + 2 lingotes de latão + o resto igual. Reserva:
  2 blocos de redstone + 4 blocos de cobre + 1 bloco de notas + 1 para-raios + **4 Matéria estável**.

### 4.2 Ficha: Antena de Vigia (D2) — "avisa quando tem aparição perto"
- **Função:** a cada **5 s** olha um raio de **48 blocos** procurando criaturas nossas (tag `odteste:paranormal`),
  aparições e eventos de Energia começando. Avisa por **som no Rádio ligado**, **mensagem curta** para quem está a até
  64 blocos e **sinal de redstone** (força = distância). Dá **30 s de antecedência** antes de evento na base.
- **Mastros:** cada **Mastro** empilhado embaixo soma **+16 blocos** de raio, até **96** (3 mastros). Mastro é bloco
  simples (sem lógica), 4 barras de ferro + 1 lingote de cobre. Com 3 mastros + Rádio ligado, pega o **Sinal da Rede**.
- **Aterramento (sem 4º aparelho):** um **Mastro Aterrado** (mastro + 4 Matéria estável de Conhecimento) embaixo da
  Antena corta o dreno pela metade e encurta o Apagão em 50% num raio de 16 blocos. Fica como peça da Antena para
  não virar mais uma máquina.
- **Tamanho:** cabeça da antena = **1x1x2** (2 blocos de altura: base com caixa de controle + haste com prato/grade).
  Mastro = 1x1 (haste com treliça). Hitbox da cabeça 1x2.
- **Partes que animam:** prato/grade de cima **gira devagar** em espera e **vira e trava** na direção da aparição
  achada; luz de topo (pisca vermelha em alerta); medidor de 5 barras na caixa de controle (sobe com a proximidade);
  pequenos isoladores com faísca (partícula) só em alerta de evento; tampa da caixa (abre ao clicar).
  Mastro: luz de posição pisca quando a Antena está ligada; o Mastro Aterrado tem cabo descendo até o chão.
- **Estados:**
  - desligado: prato parado e inclinado, sem luz;
  - em espera: prato gira 1 volta a cada ~8 s, luz verde fraca;
  - alerta (aparição): prato aponta e treme, luz âmbar, medidor sobe;
  - alerta de evento (30 s): luz vermelha piscando rápido, faíscas, som de alarme.
- **Sons vanilla:** `block.beacon.ambient` (giro), `block.piston.extend` baixo (prato trava),
  `block.bell.use` com tom baixo e `block.note_block.pling` (alerta), `block.respawn_anchor.charge` (alerta de evento),
  `block.copper_bulb.turn_on` (liga), `block.beacon.deactivate` (desliga).
- **Energia:** 40 FE/t + 10 FE/t por mastro (máx. 70 FE/t).
- **Receita:** Mekanism: 4 Ligas Infundidas + 1 Circuito Avançado + 1 para-raios + 2 barras de ferro +
  **2 Matéria estável de Energia**. Create: 4 lingotes de latão + 2 Mecanismos de Precisão + 1 para-raios +
  **2 Matéria estável de Energia**. Reserva: 4 blocos de cobre + 2 olhos do Ender + 1 para-raios + **4 Matéria estável de Energia**.

### 4.3 Ficha: Câmara de Contenção (D3) — "prende o que não pode andar solto"
- **Função:** prende **uma** criatura nossa com **menos de 25% da vida** (empurrada para dentro ou puxada com a
  câmara aberta) ou uma **aparição de evento**. A cada **5 min** contida gera um **Registro de Contenção** (página do
  Dossiê: fraqueza, origem, pista), **máximo 3 por criatura**; depois ela **se apaga** sem drop.
- **Sem farm:** criatura contida não dá drop, não se reproduz, não pode ser morta por máquina; 1 câmara ocupada por chunk.
- **Se a energia acabar de verdade:** a porta abre e a criatura **sai viva**. Evento de Apagão **não** abre a câmara.
- **Tamanho:** **1x1x2** (altura de porta), base e teto de metal grosso, frente de vidro reforçado.
  Hitbox 1x2. A criatura aparece **dentro**, encolhida (render da entidade em escala, a mesa Dev decide como).
- **Partes que animam:** porta de vidro (desce/sobe como guilhotina ou abre de lado: **Modelador propõe**); 4 travas
  nos cantos (giram e encaixam ao fechar); anéis/bobinas no teto e na base (giram devagar ocupada, emissivos);
  medidor de energia em coluna na lateral (desce conforme gasta); névoa/partícula dentro só ocupada; luz de status
  no topo; gaveta pequena que **empurra para fora** o Registro de Contenção quando sai um.
- **Estados:**
  - desligado: porta aberta, travas soltas, sem luz;
  - em espera (vazia, com energia): porta aberta, anéis parados, luz verde;
  - ocupada: porta fechada, travas encaixadas, anéis girando, névoa dentro, luz azul; o vidro trinca de leve
    (textura) quando a criatura bate;
  - energia baixa (menos de 10%): luz vermelha piscando, anéis falhando (giro com tranco), som de aviso;
  - abrindo à força (energia acabou): travas soltam de uma vez, porta sobe, fumaça.
- **Sons vanilla:** `block.iron_door.close` + `block.chain.place` (fechar e travar), `block.conduit.ambient`
  (ocupada), `block.conduit.activate` (prende), `block.glass.hit` (criatura bate no vidro),
  `block.respawn_anchor.deplete` (energia baixa), `block.iron_door.open` + `block.fire.extinguish` (abre à força),
  `item.book.page_turn` (sai Registro).
- **Energia:** 500 FE/t **só ocupada**. Bateria interna de 2 M FE (≈ 3 min de folga).
- **Receita:** Mekanism: 4 Ligas Reforçadas + 2 Circuitos de Elite + 4 Vidros Estruturais + 1 Carcaça de Aço +
  **4 Matéria tratada (uma de cada elemento)**. Create: 4 Chapas Resistentes **(conferir item)** + 2 Mecanismos de
  Precisão + 4 vidros + 2 lingotes de Allthemodium (mod confirmado no pack) + **4 Matéria tratada (uma de cada)**.
  Reserva: 4 obsidianas choronas + 4 blocos de ferro + 1 estrela do Nether + **8 Matéria tratada (duas de cada)**.

### 4.4 Vaga do 4º aparelho (só se a história pedir)
Fica **vazia**. Candidatos, para o Nikolas e a Lore decidirem depois do roteiro: um aparelho ligado à entrada da Rede
(`lore/dimensoes.md`) ou algo que o Dossiê precise e as máquinas do pack não façam. Regra: se uma máquina do pack faz,
não vira aparelho nosso.

## 5. O paranormal ataca a base (eventos de Energia)

### 5.1 Quando acontece
- A cada **10 min** por jogador **online e não AFK**, o servidor sorteia. Jogador com **Exposição 0 nunca é alvo**.
- Chance base **2%**, sobe com: estágio de Exposição (+1% por degrau), **Marca de Energia** (+3%), carregar Artefato de
  Energia (+2%), ter **respondido** no Rádio nas últimas 2 h (+3%), base com mais de **10 M FE** guardados (+2%). Teto **15%**.
  Na prática: um técnico bem exposto vê **1 evento a cada 1 ou 2 horas** de jogo; quem não mexe com o paranormal, quase nunca.
- Limites: **1 evento por base a cada 40 min**, **no máximo 6 por hora no servidor inteiro**, nunca dois juntos na mesma área.
- A Antena de Vigia avisa **30 s antes**. Sem antena, começa sem aviso.

### 5.2 Os três eventos (nomes provisórios)
| Evento | O que acontece | Duração | Como resolver | Se ignorar |
|---|---|---|---|---|
| **Fome da Linha** (dreno) | puxa até **25%** da energia de até **8 baterias/células** a até 32 blocos (teto **2 M FE**). A energia fica **guardada no evento**, não some | até 5 min | achar e banir o **Fantasma de Sinal** que aparece na base | a energia **volta sozinha** em 10 min |
| **Apagão** (desliga) | segura a saída das baterias da área e **desliga o Rádio e a Antena**. Luzes piscam (**só no cliente**) | 60 a 120 s | Mastro Aterrado (4.2) ou sintonizar o Rádio na frequência que a Antena mostra | volta tudo sozinho |
| **Casa Ocupada** (assombra) | sons de máquina fora do ritmo, o Rádio diz o nome do jogador, vultos, **abalo pequeno de Lucidez**. Aparece 1 **Desligado** (`lore/criaturas.md`) que **não quebra bloco** | 3 a 5 min | expulsar o Desligado | ele some no fim, sem drop |

Recompensa ao **resolver**: a energia volta na hora e cai **1 Matéria bruta de Energia** por jogador que ajudou.
Ignorar não pune além do susto. **Câmara de Contenção ocupada fica de fora do Apagão.**

### 5.3 O que nunca acontece
- Nenhum bloco é quebrado, trocado ou movido. Nenhum item some de baú, máquina ou inventário.
- Nenhuma energia some para sempre: o que o evento segura fica salvo (`SavedData`) e volta mesmo se o servidor reiniciar.
- Nada acontece em base de jogador **offline** nem em chunk **não carregado**.
- Config do servidor e comando de admin para **desligar** os eventos por jogador, por área ou no servidor todo.

### 5.4 Custo de servidor
- Sorteio: **1 vez a cada 10 min por jogador**, espalhado pelos ticks (pelo UUID).
- Só quando sai evento: olha os block entities dos chunks **já carregados** a até 2 chunks do jogador (5x5), pega a
  capacidade de energia do NeoForge e **para em 64 blocos olhados**. Nenhum chunk é carregado por nós.
- Durante o evento: atualiza **1 vez por segundo**, só a lista de até 8 blocos.
- Antena: **1 busca a cada 5 s** (caixa de até 96 blocos, só entidades com a tag `odteste:paranormal`).
- Nada roda sem jogador perto.

### 5.5 Defesas (sem grind)
- **Antena de Vigia:** aviso de 30 s. **Mastro Aterrado:** dreno pela metade e Apagão mais curto (Conhecimento vence Energia).
- **Selo de Giz** (`docs/progressao.md`): impede o Fantasma e o Desligado de nascerem dentro da área.

## 6. Pistas que só tech decifra
| Pista | Onde se acha | Como se lê | Custo |
|---|---|---|---|
| **Fita Gravada (limpa)** | estruturas, corpos dos que vieram antes, Dossiê | Rádio (gaveta de fita) | 20 FE/t |
| **Fita Gravada (chiada)** | idem, mais fundo nas dimensões | Rádio decifra → Transcrição | 60.000 FE + 1 Matéria estável de Conhecimento |
| **Sinal no ar** | Rádio na frequência e hora certas (a frequência vem de outra pista) | grava numa **Fita Virgem**, depois decifra | idem |
| **Sinal da Rede** | Rádio ligado a Antena com 3 mastros (96 blocos) | igual ao de cima | idem |

- **Fita Virgem:** 2 papéis + 1 lingote de ferro + 1 redstone → 2 fitas. Barata de propósito.
- **Fitas Gravadas não têm craft.** A Transcrição pode ser copiada como um livro.
- O mago lê o que o técnico decifra, e o técnico precisa da matéria que o grupo traz. Ninguém resolve sozinho.

## 7. Onde fica cada coisa no código (quando for aprovado)
- **Aparelhos e eventos:** só NeoForge (FE e capacidades), então **não são compat**. Ficam em `blocos/` e
  `sistemas/`, pela mesa Dev.
- **Receitas das máquinas do pack:** só **dados**, em `data/odteste/recipe/compat/<mod>/`, cada uma com
  `neoforge:mod_loaded`. Reserva em `data/odteste/recipe/reserva/`.
- **Código de outro mod**, só se precisar: `compat/ae2/` (Processador Lógico no Rádio; pede `AE2("ae2")` em
  `Compat.Integracao`). Mekanism, Create e os outros: só dados.
- **Versões:** antes de escrever os JSON, ler nos jars do ATM10 8.5 os tipos de receita e IDs marcados **(conferir)**.
  Se for preciso compilar contra algum (só o AE2), pôr o jar exato em `libsDoPack` do `build.gradle`.

## 8. Ligação com a lore
`lore/roteiro.md` está sendo escrito pela mesa Lore (ainda não está no `main`). Ganchos sugeridos:
1. **Primeiro sinal:** o grupo monta o Rádio e ouve a matrícula 0204 em loop (Bruna Tavares, `lore/entidades.md`).
2. **Primeira captura:** a história pede uma criatura contida para o Dossiê descobrir uma fraqueza.
3. **A fita chiada:** uma pista importante de um dos que vieram antes só sai decifrando no Rádio.
4. **Entrada da Rede:** o "circuito" de `lore/dimensoes.md` pode ser o Rádio + Antena de 96 blocos numa clareira.
- O **Rádio que Chia o Seu Nome** é Artefato (achado, sem craft) e **não é** o Rádio de Ondas Curtas.

## 9. Resumo de custos
| Coisa | Degrau | Matéria | Energia |
|---|---|---|---|
| Estabilizar (passo 1) | D1 | 1 bruta → 1 estável | padrão da máquina do pack |
| Tratar (passo 2) | D3 | 1 estável → 1 tratada | padrão da máquina do pack |
| Rádio | D1 | 2 estáveis (+1 de Conhecimento por fita decifrada) | 20 FE/t; 60.000 FE por fita |
| Antena de Vigia | D2 | 2 estáveis de Energia | 40 a 70 FE/t |
| Mastro Aterrado | D2 | 4 estáveis de Conhecimento | sem consumo |
| Câmara de Contenção | D3 | 4 tratadas (uma de cada) | 500 FE/t só ocupada |
| Kit completo (sem a Câmara) | D1–D2 | **≈ 8 Matérias** | cabe num gerador de começo |

## 10. Precisa do Nikolas
1. **Rádio juntou Receptor + Toca-fitas + Decodificador** num aparelho só, e o **Aterramento virou peça da Antena**
   (Mastro Aterrado), para ficar em 3 aparelhos. Pode?
2. **Matéria bruta só serve depois da máquina:** o jogador de magia depende do técnico. Pode, ou a magia ganha um
   caminho próprio (ex.: Altar do Malum também estabiliza)?
3. **Eventos na base:** a chance (1 evento a cada 1 ou 2 h para quem está exposto) está boa? E o Desligado dentro da base?
4. **Responder no Rádio** sobe Exposição e chama evento. Gosta da tensão ou é castigo demais?
5. O Artefato **Rádio que Chia o Seu Nome** tem ligação com o Rádio de Ondas Curtas, ou ficam separados?
6. A matéria estável/tratada entra **no lugar** da etapa tech do Fragmento e da Essência de `docs/progressao.md`? Sugestão: sim.
7. Nomes dos aparelhos e materiais → mesa Lore.
