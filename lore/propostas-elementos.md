# Propostas de sistema de elementos (mesa lore)

Status: **proposta, esperando o Nikolas escolher** (ver "Pendentes" em `docs/decisoes.md`).

## Aviso sobre os nomes
- Conferi todos os nomes contra a lista de termos de Ordem Paranormal em `docs/estilo-do-genero.md`
  (Sangue, Morte, Conhecimento, Energia, Medo, Tinta, Sinais, Intenção, Transmissão, Membrana, Sigilos, Lodo etc.). Nenhum bate.
  Troquei "Tinta" e "Rabisco" por outros nomes porque "Tinta" é elemento híbrido de lá.
- **A pesquisa na web foi negada nesta sessão.** Então NÃO verifiquei marcas fora desse universo (outros jogos, mods, livros).
  Antes de fechar o nome, alguém com acesso à web deve pesquisar cada nome escolhido.
- Todos os nomes são palavras comuns do português. Nenhum símbolo, criatura ou texto vem de outra obra.

## Regras técnicas que valem para todas as propostas (para não dar lag)
- Partícula: no máximo 1 a 3 por tick por fonte, vida curta. Nada de centenas de partículas por mob.
- Marca no corpo: **camada extra da skin** (textura 64x64 sobreposta, via renderer layer) + 1 partícula leve + 1 overlay discreto na tela. Dado salvo como attachment do jogador (1 número + 1 nível por elemento).
- Mobs: modelos GeckoLib com poucos ossos (até ~12), sem sombra dinâmica extra, IA simples (goals do vanilla).
- Líquidos: usar o sistema de FluidType do NeoForge, sem ticks extras além do vanilla.
- Cada elemento tem 1 cor dominante e 1 cor de brilho. Cor lida bem em textura 16x16 e no escuro porque o brilho usa tom claro.

---

# PROPOSTA A — A FRESTA (ciclo de quatro)

**Ideia.** O mundo tem rachaduras invisíveis, as fretas, por onde vaza algo de "fora". O que vaza não é mau: é *matéria sem dono*, e ela se agarra ao que as pessoas sentem e lembram. Vaza mais onde há abandono (minas velhas, vilas vazias, construções sem uso).

**Como se relacionam.** Quatro elementos em **ciclo**: cada um "gasta" o seguinte.
`Eco → Ferrugem → Maré → Vão → Eco`
- Quem se marca com um elemento **resiste** ao que ele gasta e **sofre** do que o gasta (ex.: marca de Eco resiste a Ferrugem, sofre com Vão).
- **Mistura:** dois vizinhos no ciclo juntos fazem um material híbrido raro (ex.: Eco + Ferrugem = "Sino Oxidado"). Opostos (Eco × Maré, Ferrugem × Vão) **não** se misturam: o ritual falha e causa dano.

## Elementos da A

### 1. Eco — memória e repetição
- **Tema:** voz que volta, lembrança, repetir o que já foi dito. **Cor:** `#7FA8C9` (azul-aço), brilho `#D6ECFF`.
- **Mod que combina:** Iron's Spells (magias de som/repetição) e Occultism (espíritos que repetem).
- **Material:** *Cristal de Eco*, minério que aparece em cavernas profundas e **dentro de minas abandonadas** (estrutura da mina). Faz um som de sino ao ser quebrado.
- **Partícula:** pequenos anéis azuis que se expandem e somem (tipo "onda"), 1 por tick. Reconhece de longe pelo anel, no escuro pelo brilho claro.
- **Marca no corpo:** linhas finas azuis nas têmporas e mãos (camada da skin) + anel pequeno de partícula a cada passo. **Dá:** a *primeira* magia lançada a cada 10 s é repetida sem custo de mana. **Custo:** sons do jogo chegam atrasados (eco de 1 s no áudio de passos) e −2 corações máximos (1 coração por nível, até 2).
- **Mob: Repetidor.** 2 blocos de altura, silhueta magra, cabeça alongada sem rosto, braços compridos que arrastam. Ataca *copiando* o último golpe: bate duas vezes seguidas (segundo golpe com atraso de 1 s). Anda devagar.
- **Ritual (giz):** *Círculo do Retorno*. Três círculos concêntricos de giz azul, item pessoal no centro. Recompensa: devolve ao jogador o último item perdido na morte (1 vez por dia). Falha: chama um Repetidor.

### 2. Ferrugem — tempo e desgaste
- **Tema:** o que envelhece, quebra e cai. **Cor:** `#B5532A` (laranja-queimado), brilho `#FFB27A`.
- **Mod:** Create/Mekanism (metal, engrenagem), Malum (almas de coisas velhas).
- **Material:** *Ferro Cansado*, bloco que cresce em cima de blocos de ferro em estruturas antigas (vila abandonada, trilhos de mina). Vira lingote com fundição. Não tem líquido: fica só no bloco, para não pesar o servidor.
- **Partícula:** lascas laranja que caem devagar (gravidade), 1 por 2 ticks. Fácil de ver porque cai.
- **Marca no corpo:** manchas laranja nos ombros e cotovelos + rangido ao andar. **Dá:** armadura ganha +1 de dureza por nível e ferramentas gastam 25% menos durabilidade. **Custo:** a comida enche 20% menos (o corpo "enferruja"); cura natural mais lenta.
- **Mob: Carcaça.** 1,8 bloco, forma de armadura velha corcunda, quase quadrada, dois olhos laranja. Ataca com corpo a corpo lento e forte; ao morrer **desmonta** em itens de ferro. Imune a fogo, fraco a água (tomar dano na chuva).
- **Ritual (giz):** *Círculo do Desgaste*. Giz laranja ao redor de uma ferramenta quebrada. Recompensa: restaura 50% da durabilidade, mas deixa o item com −10% de dano máximo.

### 3. Maré — fome e profundidade
- **Tema:** água escura, afogar, puxar para baixo. **Cor:** `#1F6F78` (verde-azulado), brilho `#6FE0D0`.
- **Mod:** Botânia (flores de água), Vampirism (fome), Iron's (magias de água).
- **Material:** *Água Funda*, líquido novo. Aparece em **poças dentro de cavernas inundadas e no fundo de oceano frio**. Fabrica-se com balde de água + Cristal de Maré. Não afoga mais que água normal, mas puxa jogadores e itens levemente para baixo.
- **Partícula:** bolhas turquesa que sobem, 1 por tick. Visível no escuro pelo brilho claro.
- **Marca no corpo:** pele com veios verde-água nos pulsos e pescoço + bolhas pequenas. **Dá:** respirar debaixo d'água, +velocidade de nado. **Custo:** fora d'água a fome cai mais rápido e a pele "seca" (dano pequeno ao ficar muito tempo no deserto).
- **Mob: Afogado Fundo.** 1,9 bloco, silhueta de pessoa curvada, cabelo comprido pingando, boca aberta. Ataca puxando o jogador (aplica lentidão forte) e depois morde. Só aparece perto de água.
- **Ritual (giz):** *Círculo da Maré*. Giz verde-água em volta de um balde com água, no chão molhado. Recompensa: transforma um bloco de terra em terra fértil perpétua. Falha: enche a área de água por 10 s.

### 4. Vão — vazio e luz apagada
- **Tema:** o espaço entre as coisas, escuro, silêncio. **Cor:** `#5B3A8C` (roxo-fundo), brilho `#C29BFF`.
- **Mod:** Iron's (magias de sombra), Occultism (espíritos), Malum.
- **Material:** *Pedra Vazia*, bloco que aparece **no teto de cavernas muito profundas e dentro das fretas** (estrutura-fenda de 3 a 5 blocos, rara). Absorve a luz ao redor (nível de luz 0 num raio de 2).
- **Partícula:** pontos roxos que são *puxados* para dentro do bloco, 1 por tick.
- **Marca no corpo:** olhos totalmente roxos na skin e fumaça roxa nas costas. **Dá:** visão no escuro e mobs passivos te ignoram. **Custo:** não pode dormir em cama (fica insone) e fica visível a mobs hostis a mais distância.
- **Mob: Sombra Rasa.** 2,2 blocos, silhueta quase só uma mancha achatada, sem detalhes, com duas fendas roxas. Ataca vindo do teto, cai sobre o jogador. Some se a tocha estiver perto.
- **Ritual (giz):** *Círculo do Vão*. Giz roxo, quatro velas apagadas nos cantos. Recompensa: abre um "bolso" vazio (baú de 9 espaços que só abre dentro do círculo).

## Progressão da A
- **Começo:** o jogador acha um *Diário de Campo* numa mina velha, quebra Cristal de Eco, faz o primeiro círculo de giz (Eco). Sem marca ainda.
- **Meio:** escolhe 1 elemento e recebe a primeira marca. Ganha acesso a Ferro Cansado / Água Funda / Pedra Vazia. Mistura de vizinhos desbloqueia itens híbridos. Precisa lidar com o elemento que "gasta" a marca dele.
- **Fim:** fecha uma **fresta grande** (estrutura-boss no subsolo), exigindo as 4 marcas em itens temporários. Recompensa: *Selo da Fresta*, item que dá uma marca nível 3 sem custo extra.

---

# PROPOSTA B — O ACERVO (pares de oposição)

**Ideia.** Todo registro (livro, carta, foto, cartaz) guarda uma pequena parte de quem o fez. Em arquivos velhos esse acúmulo vira *pressão*: o que foi escrito e esquecido começa a agir sozinho. O paranormal vaza em bibliotecas, cartórios e porões.

**Como se relacionam.** Quatro elementos em **dois pares opostos**: `Carvão × Mofo` (escrever × esquecer) e `Lacre × Poeira` (fixar × dissipar). Opostos se anulam: ficar perto de um em excesso apaga a marca do outro. **Mistura** é entre elementos *não* opostos: Carvão + Lacre = "Documento Oficial", Mofo + Poeira = "Pó Antigo", etc. (4 híbridos possíveis).

## Elementos da B

### 1. Carvão — escrita e traço
- **Tema:** o que fica marcado, desenho, anotação. **Cor:** `#4A5078` (azul-ardósia), brilho `#8EA0FF`.
- **Mod:** Occultism (giz, símbolos), Iron's (magias de selo).
- **Material:** *Veio de Carvão Antigo*, minério em **porões de biblioteca e salas de arquivo** (estrutura "Acervo Esquecido"). Vira o *Giz Grafite*, usado nos rituais.
- **Partícula:** riscos finos azuis que aparecem e somem como traços de caneta, 1 por 2 ticks.
- **Marca no corpo:** linhas de "anotação" nos antebraços (como rabiscos à mão) + traço azul que o jogador deixa ao correr. **Dá:** símbolos no chão duram 5x mais e magias de selo ficam 20% mais fortes. **Custo:** o jogador não consegue apagar o que faz: blocos postos ficam "marcados" e mobs seguem o rastro azul.
- **Mob: Escrivão.** 1,7 bloco, silhueta de pessoa magra com cabeça de livro fechado e mão enorme com pena. Ataca de longe lançando riscos azuis em linha reta (projétil lento, 3 de dano).
- **Ritual (giz):** *Margem Anotada*. Grafite em quadrado com uma página de livro no meio. Recompensa: o livro vira *Livro de Notas* que guarda as coordenadas dos últimos 5 lugares visitados.

### 2. Lacre — cera e fixação
- **Tema:** selar, trancar, prender no lugar. **Cor:** `#C4572B` (vermelho-cera), brilho `#FFC27A`.
- **Mod:** Occultism (conter espíritos), Malum (prender almas), Create (prensa).
- **Material:** *Cera de Lacre*, sai de **colmeias dentro de bibliotecas** ou se fabrica derretendo vela. Faz o bloco *Lacre*, que impede abrir baús e portas sem a chave certa.
- **Partícula:** gotas lentas alaranjadas que pingam, 1 por 3 ticks.
- **Marca no corpo:** pingo de cera na palma, cordão vermelho no pulso. **Dá:** o jogador pode *selar* um mob por 5 s (imobiliza); +proteção contra empurrões. **Custo:** itens ficam presos na mão se segurar muito tempo sem usar (não troca de slot por 1 s depois de usar o selo).
- **Mob: Guardião de Cera.** 2,4 blocos, silhueta larga, parece uma estátua derretida, sem rosto, dedos fundidos. Ataca esmagando o chão (área pequena) e deixa poças de cera que prendem os pés.
- **Ritual (giz):** *Selo Simples*. Círculo de giz laranja em volta de um baú. Recompensa: o baú só abre para quem desenhou. Falha: cola o jogador no lugar por 3 s.

### 3. Mofo — esquecimento e decomposição lenta
- **Tema:** o que apodrece em silêncio, esquecer, umidade. **Cor:** `#6E8B3D` (verde-musgo), brilho `#C8EE7A`.
- **Mod:** Botânia (flores doentes), Vampirism (doença).
- **Material:** *Fungo de Arquivo*, planta que cresce em paredes de pedra úmida em **cavernas e porões**. Dá *Esporo*, usado em poções.
- **Partícula:** esporos verdes que flutuam para cima, 1 por 2 ticks.
- **Marca no corpo:** manchas verdes nos braços e pescoço (textura de mofo) + esporos saindo da roupa. **Dá:** imunidade a veneno e fome, e as plantas ao redor crescem mais rápido. **Custo:** o jogador **esquece** o mapa: marcadores do mapa/minimap dele ficam desatualizados e itens de comida estragam mais rápido no inventário.
- **Mob: Bolor.** 1,5 bloco, silhueta redonda e baixa, parece uma pilha de livros mofados com braços finos. Ataca soltando uma nuvem de esporo (veneno em área, 3 s).
- **Ritual (giz):** *Círculo do Esquecimento*. Giz verde em volta de uma flor. Recompensa: a flor vira semente de uma planta rara de Botânia.

### 4. Poeira — tempo passado e dissipação
- **Tema:** o que já foi, desfazer, ficar leve. **Cor:** `#D9D2BE` (osso), brilho `#FFFFFF`.
- **Mod:** Iron's (magias de vento), Mekanism (ar), Create (ventiladores).
- **Material:** *Pó de Arquivo*, junta-se em **teias e prateleiras abandonadas** e some se o jogador passar a vela perto. Fabrica-se passando farinha pelo ventilador do Create.
- **Partícula:** pontos brancos muito lentos que flutuam sem direção, 1 por 2 ticks. No escuro parece uma poeira de luz.
- **Marca no corpo:** cabelo e roupas ficam esbranquiçados + pó caindo dos ombros. **Dá:** queda lenta e +velocidade de movimento. **Custo:** itens soltos no chão somem mais rápido perto dele e a vida máxima cai 1 coração por nível.
- **Mob: Sopro.** 1,2 bloco, silhueta sem forma, tipo um redemoinho com dois pontinhos. Ataca atravessando o jogador (dano baixo) e roubando 1 item do hotbar por 10 s (devolve se morrer).
- **Ritual (giz):** *Círculo da Poeira*. Giz branco com uma vela acesa. Recompensa: teleporte curto (até 8 blocos) uma vez. Falha: nuvem que cega por 5 s.

## Progressão da B
- **Começo:** encontra o *Acervo Esquecido* (estrutura de biblioteca em ruínas), pega a primeira pista (um livro com anotações em símbolos novos), faz Giz Grafite.
- **Meio:** escolhe um par. Cada marca *nega* a do par oposto, então quem escolhe Carvão não pode ter Mofo. Misturas por vizinhos geram "documentos" que abrem portas lacradas em outras estruturas.
- **Fim:** o **Arquivo Central** (estrutura grande no subsolo, com 4 salas, uma por elemento), onde um mob-chefe (o *Curador*) vence-se com os 2 pares equilibrados. Recompensa: *Livro em Branco* que o jogador usa para escrever um feitiço personalizado de Iron's.

---

# PROPOSTA C — A DÍVIDA (moedas e troca)

**Ideia.** O paranormal é um *empréstimo*: algo do outro lado oferece poder, mas sempre cobra de volta. Vaza porque muita gente aceitou a oferta e não pagou, e o que ficou devendo se acumula como "juros" no mundo.

**Como se relacionam.** Não há ciclo nem oposição. Os quatro elementos são **moedas**: cada marca *converte* um elemento em outro (câmbio). O jogador ganha poder quando "toma emprestado" de um e **paga** com outro. **Mistura** = *juros*: usar dois ao mesmo tempo rende mais, mas gera dívida que cresce com o tempo (número visível numa barra). Dívida zero = sem efeito; dívida alta = o mundo cobra com um mob.

## Elementos da C

### 1. Febre — calor e ímpeto
- **Tema:** pressa, calor do corpo, vontade. **Cor:** `#E0431F` (vermelho-laranja), brilho `#FF9A66`.
- **Mod:** Vampirism (corpo, sede), Malum (alma que arde), Mekanism (calor).
- **Material:** *Brasa Viva*, minério em **desertos e na camada de lava**. Vira líquido *Calda de Febre* fervendo (não é lava, não queima bloco, mas causa dano de calor).
- **Partícula:** faíscas vermelhas subindo rápido, 1 por tick. Muito visível, brilho forte.
- **Marca no corpo:** pele avermelhada nas bochechas e mãos + vapor leve saindo. **Dá:** +velocidade de ataque e +dano por nível. **Custo (dívida):** cada ataque soma 1 ponto de dívida; passando de 20, o jogador toma dano de calor.
- **Mob: Cobrador de Febre.** 2 blocos, silhueta alta e magra com casaco comprido e pescoço torto. Ataca correndo em linha reta e dando soco duplo. Deixa rastro de calor no chão.
- **Ritual (giz):** *Penhor de Febre*. Giz vermelho com um item de ouro no centro. Recompensa: "empresta" +4 corações por 5 min; depois cobra −2 corações por 10 min.

### 2. Sono — descanso emprestado
- **Tema:** cansaço, sonho, descanso. **Cor:** `#4B3FA0` (azul-violeta), brilho `#A59BFF`.
- **Mod:** Iron's (magias de sonho), Botânia (flores calmantes), Occultism.
- **Material:** *Pétala de Sono*, flor que cresce em **florestas à noite** e em **camas abandonadas** (estrutura casa velha). Vira *Névoa de Sono* em frasco.
- **Partícula:** pequenos "z" estilizados em bolinhas violeta que sobem devagar, 1 por 3 ticks.
- **Marca no corpo:** olheiras fundas e fios violeta nos cabelos. **Dá:** pode "pular" a noite (dorme em qualquer lugar e cura tudo), vida regenera mais rápido. **Custo (dívida):** dívida em forma de *Bocejos*: telas fecham a visão por 1 s a cada minuto e cresce com a dívida.
- **Mob: Sonâmbulo.** 1,8 bloco, silhueta de pessoa de pijama andando de braços para frente, olhos fechados. Ataca só se o jogador estiver acordado e perto; ignora quem estiver agachado. Dá lentidão.
- **Ritual (giz):** *Penhor do Sono*. Giz violeta em volta de uma cama. Recompensa: a cama cura totalmente e passa a noite, mas o jogador fica 10 min com lentidão.

### 3. Fôlego — ar emprestado
- **Tema:** respirar, voz, vento no peito. **Cor:** `#8FE3D6` (verde-água claro), brilho `#E6FFFB`.
- **Mod:** Mekanism (gás), Create (pressão), Iron's (magias de vento).
- **Material:** *Gás Fôlego*, coletado de **cavernas com ar parado** e de **máquinas** (Mekanism, tanques). Em jogo: gás visível como névoa clara, enche um tanque.
- **Partícula:** nuvenzinhas claras que sobem e se dispersam, 1 por 2 ticks.
- **Marca no corpo:** respiração visível (nuvem de vapor mesmo no calor) e linhas claras nos lábios. **Dá:** pulos duplos e respirar mais tempo debaixo d'água. **Custo (dívida):** a barra de ar do jogador se esvazia mais rápido em lugares fechados.
- **Mob: Sufocante.** 1,6 bloco, silhueta de saco inflado com pernas finas, boca grande sempre aberta. Ataca "sugando" o ar (esvazia a barra de ar do jogador em 3 s) em um cone à frente.
- **Ritual (giz):** *Penhor do Fôlego*. Giz claro em torno de um tanque ou frasco. Recompensa: enche o frasco com ar que dá +respiração por 2 min.

### 4. Sal — luto e preservação
- **Tema:** conservar, guardar, não deixar apodrecer, luto. **Cor:** `#E8E4D8` (branco-sal), brilho `#FFFFFF`.
- **Mod:** Malum (almas guardadas), Occultism (círculos de proteção), Botânia.
- **Material:** *Cristal de Sal*, minério em **salinas** (estrutura nova: lago seco com cristais) e em **desertos**. Vira o *Círculo de Sal*, item que protege uma área de mobs (como ritual).
- **Partícula:** grãos brancos que caem rápido e quicam, 1 por 2 ticks.
- **Marca no corpo:** mãos e rosto com textura de cristal branco, olhos lacrimejando (partícula). **Dá:** itens de comida não estragam e o jogador recebe menos dano de mobs mortos-vivos. **Custo (dívida):** não pode usar itens de cura vindos de outros (poções) enquanto marcado.
- **Mob: Viúva de Sal.** 2,1 blocos, silhueta de mulher alta de véu longo, toda branca, mãos cruzadas. Ataca chorando (grito em área que reduz velocidade) e depois bate com mão fechada. Fraca a água (dissolve).
- **Ritual (giz):** *Penhor do Sal*. Círculo de sal puro. Recompensa: marca um ponto de volta, onde o jogador reaparece uma vez, mas gasta uma "dívida".

## Progressão da C
- **Começo:** recebe uma **Promissória** (item) de um NPC-fantasma ou em uma casa velha, faz o primeiro penhor simples. Dívida visível em uma barrinha.
- **Meio:** aprende a *converter* (Febre ↔ Sono ↔ Fôlego ↔ Sal) em um **Balcão** (bloco). Misturas geram juros. Precisa gastar a dívida (rituais ou mobs) para não ser cobrado.
- **Fim:** enfrenta o **Credor** (mob-chefe, silhueta alta de terno com cartola e livro-caixa, ataca cobrando um elemento por vez) com todas as 4 moedas equilibradas. Recompensa: *Quitação*, item que apaga toda a dívida uma vez e libera uma marca máxima sem custo.

---

# Comparação rápida

| | A — Fresta | B — Acervo | C — Dívida |
|---|---|---|---|
| Relação | Ciclo | Pares opostos | Câmbio (troca) |
| Clima | Cósmico, abandono | Investigação, bibliotecas | Pacto, custo |
| Combina mais com | Iron's, Malum | Occultism, Botânia | Vampirism, Mekanism/Create |
| Risco técnico | Baixo | Baixo (estruturas novas) | Médio (barra de dívida + sistema de conversão) |
| Cenário | Minas, cavernas | Bibliotecas, porões | Casas velhas, salinas, desertos |

## Perguntas para o Nikolas
1. Qual proposta (ou mistura) segue?
2. Quatro elementos está bom ou prefere mais/menos?
3. Podemos pesquisar os nomes finais na web antes de fechar (a pesquisa foi negada nesta sessão)?
