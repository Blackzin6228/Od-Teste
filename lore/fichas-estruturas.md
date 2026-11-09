# Fichas de estruturas do Ato 1 (mesa lore)

Status: **rascunho para o Nikolas aprovar.** Base: `lore/roteiro.md` (Ato 1 refeito) e `docs/decisoes/2026-11-08-producao-estruturas-e-enigmas-fora.md`.
Nenhum nome de jogador aqui. Nenhum número aparece para o jogador dentro das estruturas (placas são palavra ou desenho; ver cada ficha).
**09/11/2026:** os lugares 1 a 4 foram refeitos em "Minecraft puro" (`docs/decisoes/2026-11-08-roteiro-minecraft.md`): saíram Clareira e Carro, Frigorífico, Pousada e Casa da Repetidora.
Tamanhos revistos para trabalho de mesa (decisão N5): cada lugar cabe em **16 a 40 horas** de trabalho de mesa; o Nikolas pode retocar no criativo.

## Regras comuns de construção
- **Quem faz:** as mesas montam a schematic; o Nikolas retoca no criativo se quiser e coloca no mundo (N5). Prévia (print ou render) antes de integrar.
- **Formato:** schematic `.nbt`, uma por estrutura, com a origem no canto sudoeste do térreo (para colocar sempre igual).
- **Paleta vanilla.** A base usa só blocos do jogo puro, para a schematic abrir em qualquer lugar (regra de não depender de mod).
  Decoração de mods do pack pode entrar numa segunda versão `-decor`, nunca na base.
- **Cara de Minecraft:** tudo o que aparece é bloco ou item do jogo (ou nosso, no mesmo estilo). Nada de carro, máquina moderna, TV, fita ou crachá.
- **Altura do chão:** a schematic leva um "colar" de terra de 2 blocos em volta, para encaixar em terreno irregular. O admin aplaina ou usa Create Schematicannon com "substituir ar".
- **Luz:** pouca e com defeito. Lanterna apagada, vela solitária, fogueira de alma quase apagada. Nunca tocha pregada na parede (some o clima). Tem de haver luz suficiente para **não nascer monstro dentro**, exceto onde a ficha diz o contrário.
  Os monstros comuns do jogo não devem competir com nossos perigos: cada ficha marca "sem spawn" onde importa (blocos de luz invisíveis são aceitos).
- **Pistas posicionadas** são blocos de dados (baú, barril, atril, placa, item em moldura, estandarte). O conteúdo dos livros e itens de história vem do mod; a ficha diz só **onde** fica.
- **Sem caça-tesouro:** toda pista fica à vista ou atrás de um enigma, nunca escondida atrás de bloco falso.
- **Um desenho de percurso:** cada lugar tem um caminho principal claro e dois ou três cantos de sombra. Quem se perde deve se perder por medo, não por labirinto.
- **Perigo sem grind:** nada aqui exige combate repetido.
- **Valores de tamanho** são aproximados (largura x profundidade x altura, em blocos). O construtor tem liberdade de 10%.

## Mapa do Ato 1 (ordem de visita)
0. Vila Calada (destino; ficha no fim deste arquivo)
1. Moinho Parado (prólogo)
2. Casa do Açougueiro
3. Casa do Cartógrafo
4. Torre das Correntes
5. Fazenda do Sino Mudo (Casarão + Capela da Mesa)

Distância sugerida entre elas: o suficiente para uma caminhada de alguns minutos (algumas centenas de blocos), seguindo rios, trilhas e vales, nunca estrada.
O Nikolas escolhe os pontos; o mod registra a posição de cada um (ver roteiro). Uma vila de verdade do mundo pode ficar perto, como detalhe.

| Lugar | Tamanho | Trabalho de mesa (estimativa) |
|---|---|---|
| 1. Moinho Parado | 36 x 30 x 26 | 16 a 20 h |
| 2. Casa do Açougueiro | 48 x 36 x 16, mais 7 de porão | 30 a 40 h |
| 3. Casa do Cartógrafo | 26 x 22 x 30 | 20 a 26 h |
| 4. Torre das Correntes | 20 x 20 x 40, mais 8 de porão | 22 a 28 h |
| 5. Fazenda do Sino Mudo | 72 x 60 x 18, mais 6 de porão e cripta | 34 a 40 h |

---

## 1. Moinho Parado
- **Função:** o primeiro lugar. Dá o clima, toca o **Morse PORTA** nos blocos de nota (E1), guarda o baú com o Diário p. 1, a metade esquerda do Mapa Rasgado e o **endereço do Atlas** (site).
- **Tamanho:** 36 x 30 x 26. Pequeno de propósito: é o prólogo.
- **Partes:**
  - **O moinho:** torre de pedra e tábua, base 9 x 9, três andares por dentro, pás de lã cinza e cerca (quatro, uma quebrada). No térreo, sacos de trigo (fardos de feno) e um mó (pedra lisa).
    No último andar, a **fileira de blocos de nota** ligada à roda de madeira (redstone escondida). Uma janela redonda olha para o rio seco.
  - **Casa do moleiro:** casa de aldeão pequena, porta aberta, mesa posta para um, prato com pão mofado (pão em moldura), cama desfeita.
    Uma prateleira com **cabeças de esqueleto viradas para a parede**, uma por jogador do grupo (o mod coloca quantas forem).
    Ao pé da cama, o **baú com cadeado de letras** (cinco letras).
  - **Rio seco:** leito de cascalho e areia grossa, uma ponte de tábua, uma roda d'água parada sem água.
  - **Celeiro:** pequeno, telhado furado, um espantalho (armadura em suporte com abóbora entalhada) virado para o moinho.
- **Paleta:** pedregulho e pedregulho com musgo, tábua de abeto e carvalho escuro, troncos descascados, lã cinza, cascalho, areia grossa, feno, teias.
- **Clima:** noite, neblina baixa, rio sem água, as pás paradas e a roda de dentro rangendo. Os blocos de nota tocam, travam, voltam. Correntes arrastando ao longe.
- **Pistas:**
  - **Último andar:** os blocos de nota (Morse PORTA; ver `enigmas-fora.md` E1). A roda trava no meio quase sempre; inteira só numa vigília de noite toda.
  - **Baú do moleiro:** Diário do Açougueiro p. 1, metade esquerda do Mapa Rasgado. Na tampa, a carvão, o endereço do **Atlas do Cartógrafo**.
  - **Celeiro:** um estandarte rasgado com a **Cruz de Ferro** (nossa; reaparece no Mapa e na Torre).
- **Por onde se entra:** pela ponte, a pé. A porta do moinho está aberta.
- **Perigos:** nenhum físico. O **Cheiro** (neblina avermelhada leve) cresce com o tempo. O barulho de correntes começa a ser ouvido aqui. Sem spawn dentro do moinho e da casa.
- **Integração:** com o Create no pack, a versão `-decor` troca as pás por um rolamento de moinho; girar faz a sequência tocar inteira (atalho).

## 2. Casa do Açougueiro
- **Função:** o grande lugar do Ato 1. Entrega o **Livro de Trocas**, a metade direita do Mapa, a **Faca de Osso** e apresenta o **Magarefe**. Maior estrutura antes da Fazenda.
- **Tamanho:** 48 x 36 x 16, mais 7 de profundidade no porão de salga.
- **História do lugar:** o açougue da vila perdida. O aldeão açougueiro vendia carne aos vizinhos e anotava, num livro, o que cada um dava em troca.
  Nos últimos invernos, o que davam deixou de ser esmeralda.
- **Partes:**
  - **Curral** (cerca de carvalho escuro): cocho cheio de trigo, nenhum bicho, chão de terra batida com sulcos. Um portão de cerca entreaberto.
  - **Loja** (térreo, frente): balcão de tábua, dois **defumadores** (a bancada do açougueiro no jogo), ganchos de corrente na parede com nada pendurado, barris.
    A **porta da frente** é larga, de madeira, duas folhas.
  - **Casa de abate** (térreo, fundo): pé-direito alto (10), vigas de carvalho escuro, **quatro correntes** que descem do teto. Embaixo de cada uma, uma placa de madeira (**Cão, Vaca, Lebre, Javali**) e uma alavanca.
    Ralo no chão (alçapões de ferro). O **alçapão grande** do porão fica no meio, trancado.
  - **Palheiro** (sótão): feno, uma escada de mão, uma fresta no assoalho que olha para a casa de abate (é de onde o vídeo E7 foi "gravado").
  - **Moradia** do açougueiro (anexo): cama, mesa, estante quebrada, lareira apagada.
  - **Porão de salga:** pedra, barris em fila, bancada de pedra lisa, ralo, uma saída de serviço por escada de mão até o curral. É o lugar do Magarefe.
- **Paleta:** carvalho escuro e abeto, pedregulho, tijolo de pedra rachado, pedra lisa, barras de ferro, correntes, terracota branca suja (piso da casa de abate), barris, feno.
  Vermelho só em poças pequenas (terracota vermelha, lã vermelha), nunca em grande quantidade.
- **Clima:** frio de pedra, cheiro de ferro (partícula rala vermelha), moscas (som), defumador soltando fumaça sem fogo, correntes rangendo. Tudo o que é "carne" é sugerido (corrente, ralo, poça), não mostrado. Tom sério, nada gratuito.
- **Pistas:**
  - **Balcão da loja:** **Diário do Açougueiro, páginas 2 e 3** (ordem dos bichos; tabela parcial dos Riscos de Corte).
  - **Casa de abate:** as placas e as alavancas (enigma de ordem). Riscado na viga: *"O Açougueiro ensina a ordem. Não adianta pedir."*
  - **Porão de salga** (abre com a ordem certa): **Livro de Trocas**, **metade direita do Mapa**, **Faca de Osso**, num barril aberto sobre a bancada.
  - **Palheiro:** riscada na viga, a palavra que abre o vídeo E7 no Atlas (opcional).
- **Por onde se entra:** pela **porta da frente** (acorda o Magarefe) ou pela **porta de ferro** do lado do curral (de botão; quem ouviu o aviso do E1 ou juntou FERRO e PORTA sabe).
  Há uma terceira entrada pela escada de serviço do porão, que pega o Magarefe de frente. Ela existe para o grupo corajoso e para a perseguição.
- **Perigos:** o **Magarefe** (perseguição lenta, entra pela frente). Erro na ordem das correntes: a corrente balança, sobe a Exposição e cai Lucidez.
  O **Pendurado** (efeito visual só para quem tem a maior Exposição) aparece na corrente vazia. **Sem spawn** em toda a estrutura.

## 3. Casa do Cartógrafo
- **Função:** o lugar do "vídeo achado". Tem o **ritual de giz** que dá a palavra do vídeo, o **Último Olhar do Cartógrafo** (E4, senha do Atlas) e o baú com o **Terço da Moça**.
  Explica o mapa que apareceu na Cena 1B: o cartógrafo desenhou um lugar que não existia, e o mapa continua desenhando.
- **Tamanho:** 26 x 22 x 30 (casa de dois andares e uma torre-mirante).
- **Partes:**
  - **Salão** (térreo): paredes cobertas de **mapas em molduras**, a **mesa de cartografia** com um mapa pela metade, uma estante, um baú com cadeado de letras.
    No meio, o **mapa-mural** (vários mapas lado a lado, em molduras) com **marcas de estandarte com nome**: os moradores da Vila Calada. A última marca tem o nome de `{marcado}`.
  - **Quarto** (andar de cima): cama arrumada, uma vela apagada, tinteiro (frasco com tinta de lula) e penas.
  - **Escada em caracol** até o mirante: estreita, de pedra, com janelas pequenas. É onde o Pendurado aparece.
  - **Mirante:** sala redonda no alto, uma **luneta** apoiada na janela que dá para a Fazenda, e no chão um **círculo de giz pela metade**.
- **Paleta:** tijolo de pedra e pedra lisa, tábua de bétula e abeto, lã branca e marrom, estantes, molduras, vidro, cobre oxidado (telhado da torre).
- **Clima:** papel e silêncio. Vento na torre. As marcas do mapa-mural mudam de lugar entre uma visita e outra (o mod troca de manhã).
- **Pistas:**
  - **Mapa-mural:** marcas com nomes; a última com `{marcado}`, em cima de onde ele dormiu.
  - **Mirante:** o círculo de giz (ritual em dupla ou mais, de noite: as cinzas do centro formam a palavra do vídeo). A luneta: olhando por ela depois do Atlas, a **Torre das Correntes** aparece na neblina.
  - **Baú do salão:** abre com a senha do Atlas (a mesma do vídeo) e guarda o **Terço da Moça** (gancho do Ato 2).
- **Por onde se entra:** pela porta da frente (sem tranca) ou pela janela do salão.
- **Perigos:** sem monstro fixo. O **Pendurado** pode aparecer na escada (efeito de cliente). Quem fica sozinho na escada ou no círculo perde Lucidez devagar. Sem spawn dentro.

## 4. Torre das Correntes
- **Função:** a **origem do barulho de correntes**. Quem resolve o Atlas cala o sino e recebe o **rumo da Fazenda**. Também guarda o segredo do Ato 5 (o Disco Riscado).
- **Tamanho:** 20 x 20 x 40 (torre de 36 de altura no alto de um morro) mais 8 de profundidade (porão).
- **Partes:**
  - **Muro baixo e portão** de pedra, quebrado; um estandarte da **Cruz de Ferro** no portão.
  - **Térreo:** sala de guarda, mesa, armeiro vazio (suportes de armadura sem armadura), a entrada do porão.
  - **Escada:** sobe colada à parede de dentro, com **degraus faltando**. No fundo do vão, um monte de **fardos de feno** (amortece a queda, como no jogo).
  - **Sala do sino** (topo): o **sino de ferro** grande (feito de blocos de ferro e correntes), **sem badalo**, pendurado por **correntes** que balançam e arrastam na pedra sem vento.
    No chão, o **alçapão com cadeado de letras**. Logo abaixo, uma fileira de **blocos de nota**.
  - **Porão:** pedra úmida, duas salas, uma **jukebox** com o **Disco Riscado**, e o caderno do vigia.
- **Paleta:** tijolo de pedra rachado e com musgo, pedregulho, blocos de ferro, correntes, barras de ferro, carvalho escuro, cascalho.
- **Clima:** isolamento, vento, correntes arrastando. Aqui o som é o personagem. Quando o sino cala, o silêncio é a recompensa e o medo.
- **Pistas:**
  - **Alçapão do sino:** cadeado de letras (o jogador escreve uma **palavra**, nunca número). Resposta do Atlas. Se certa, as correntes travam, o sino cala e os blocos de nota tocam, em Morse, o rumo da Fazenda.
  - **Caderno do vigia** (porão): fala do "barulho", sem explicar. *"Não é o vento que mexe o sino. É alguém do outro lado puxando a corda."*
  - **Jukebox:** o Disco Riscado (a imagem escondida no som; ver `enigmas-fora.md` E6).
- **Por onde se entra:** pelo portão (aberto) ou escalando o muro (cai Lucidez e sobe Exposição).
- **Perigos:** altura. **Queda não mata de uma vez** (decisão N7): dói muito, dá susto e tira Lucidez; o feno no fundo do vão e o mod seguram o dano. O Magarefe não vem aqui. Quem desce ao porão sozinho perde Lucidez; em dupla, não.

## 5. Fazenda do Sino Mudo
- **Função:** o clímax do Ato 1. Aqui está a **Mesa do Pacto**, o evento do Diabo e a Pista do Ato 2. Aldeões de uma vila próxima (detalhe) aparecem parados na cerca, de longe, olhando para a capela.
- **Tamanho total:** 72 x 60 x 18 (antes 110 x 90; reduzido para caber no trabalho de mesa). Três blocos:
  - **Casarão:** 30 x 20 x 16 (dois andares e sótão) mais 6 de profundidade (porão).
  - **Capela da Mesa:** 20 x 14 x 14 mais 6 de profundidade (cripta).
  - **Terreiro:** curral, galpão, cemitério pequeno, poço, caminho de pedra, árvore grande no meio, o **sino do terreiro** num poste.
- **Andares e salas do Casarão:**
  - **Térreo:** varanda larga, sala de visitas, sala de jantar (mesa comprida, cadeiras tombadas: escadas de madeira viradas), cozinha com fornalha e defumador.
  - **Andar de cima:** quatro quartos (camas de cores diferentes, uma por "lugar na mesa"), escritório do fazendeiro (**Diário do Açougueiro, página 4**, o poema das velas).
  - **Sótão:** quarto fechado com o retrato virado; baú vazio com a marca de um terço que alguém levou (o terço está na Casa do Cartógrafo).
  - **Porão:** adega de barris e passagem para a cripta da capela.
- **Andares e salas da Capela:**
  - **Nave:** bancos, **a Mesa do Pacto no meio**, um lugar posto para cada jogador do grupo mais um a mais no fim. Cada lugar: prato com o **nome do jogador** (item do mod), uma **vela** de altura diferente e um **cálice** vazio.
  - **Altar:** estrutura de pedra que gira e revela o **Altar do Aceite** (cálice de pedra com *"Chame."*).
  - **Cripta:** pequeno nicho com túmulos e uma vela acesa há muito tempo. Guarda a **carta com o selo de cera** da Pista do Ato 2 (só aparece depois do evento).
- **Paleta:** calcita e diorito (caiado), tijolo de pedra, carvalho escuro, terracota branca com padrão (piso da nave), vitral escuro (vidro tingido de vermelho-escuro e azul-marinho), ferro, correntes, velas, tapete vermelho, lã.
- **Clima:** abandono religioso. Pó dourado em luz de vitral. Cheiro de cera e ferro. Sino pendurado, mudo.
- **Pistas:**
  - **Casarão, escritório:** Diário p. 4 (o poema), num atril.
  - **Casarão, sótão:** o **retrato virado** (pintura nossa): quando virado, mostra o rosto de um velho **sem olhos**; atrás, um recado a carvão.
  - **Mesa:** pratos com nomes; velas; cálices.
  - **Cripta:** a carta com o selo.
- **Por onde se entra:** pelo portão da fazenda (aberto) e pela capela, que só **se abre** quando o sino da Torre das Correntes foi calado. Antes disso, a capela está de portas fechadas e o sino do terreiro toca (ouve-se).
- **Perigos:** dentro da capela o **chat de texto fica bloqueado; a voz continua** (decisão N7). Velas fora de ordem apagam todas e tiram Lucidez do grupo. Sem monstro. **A Convidada** (figura no lugar a mais) é o susto. O evento do Diabo acontece aqui, solto pelo admin.

## Perguntas para o Nikolas
Respondidas em 09/11 e aplicadas: quem constrói (N5: as mesas, você retoca), queda sem morte (N7), chat bloqueado na capela (N7).
1. Os tamanhos novos (tabela do mapa) estão bons? Se pesar, dá para cortar o anexo da Casa do Açougueiro e o celeiro do Moinho sem perder nada de história.
2. A Casa do Açougueiro tem uma terceira entrada pelo porão: (a) mantém (recomendo: dá a perseguição); (b) tira para simplificar.
3. As marcas de estandarte do mapa-mural mudam de lugar a cada manhã: (a) sim, é o medo do lugar (recomendo); (b) ficam paradas, mais simples para o Dev.
4. Quer uma vila de verdade do mundo perto da Casa do Açougueiro e da Casa do Cartógrafo (aldeões parados ao longe), ou ficam isoladas?

---

# Vila Calada (Ato 1)

Nome provisório. Busca feita em 09/11/2026: nenhuma obra conhecida usa "Vila Calada". (Conferir de novo antes de fixar.)
Texto do livro: `lore/livro-do-sineiro.md`. Decisão: `docs/decisoes/2026-11-09-vila-destruida.md`.

## Em uma frase
A vila onde o sino foi tocado pela última vez: totalmente destruída, com a primeira pista de como voltar.

## Onde e como entra
- **Estrutura grande, bem produzida, colocada pelo admin** (nunca por geração de mundo). Fica em terreno livre, aberto, sem construção de jogador.
- **Distância:** longe o bastante para ser uma viagem (uma noite de caminhada), perto o bastante para não virar grind. Sugestão: entre 700 e 1200 blocos da vila comum marcada na Cena 1.
- O admin pode trocar o ponto. As três pistas (abaixo) sempre apontam para o ponto escolhido.
- Dá para chegar a pé, de dia ou de noite. A noite é mais bonita e mais perigosa.

## Tamanho e forma
- Cerca de 100 por 100 blocos, com um muro baixo de pedra e estacas, quebrado em vários pontos.
- **Entrada pelo sul** (um caminho de terra que sai do campo queimado). Uma **rua principal** vai do sul ao norte, até a capela.
  Uma **rua cruzada** corta de leste a oeste na altura do poço. Ruas de terra batida e cascalho, com sulcos de roda e poças escuras.
- **Praça central** com o **poço**. É o coração da vila e da história.
- **Cerca de 14 casas**, todas de aldeão, todas pequenas e baixas:
  - 7 **queimadas** (só as paredes de pedra baixa e as vigas pretas).
  - 5 **desabadas** (telhado no chão, dentro cheio de entulho).
  - 2 **inteiras por fora e erradas por dentro** (ferreiro e Compadre, abaixo).
- Em volta, **campos queimados** (plantação seca e preta, espantalhos caídos) e um **cemitério** atrás da capela com covas abertas e vazias.

## Os lugares (da entrada para o fundo)

| Lugar | O que o jogador vê | Pista escondida |
|---|---|---|
| **Campo queimado** (entrada) | Plantação preta, cercas partidas, um espantalho de cabeça para baixo | Pegadas de cinza em fila, todas indo para dentro da vila. Nenhuma sai |
| **Curral comprido** (oeste) | Cercado enorme, ossos de gado em fileiras certinhas, valas de terra avermelhada | Cheiro de ferro mais forte aqui. Estandartes da Cruz de Ferro rasgados |
| **Casa da Moça do Terço** (oeste, perto da praça) | Peitoril cheio de botões de pedra, um para cada nome, e **um espaço vazio** | A fileira de botões está em ordem do mais velho ao mais novo. O vazio é a ponta. Ganchos para o Ato 3 |
| **Praça e poço** (centro) | Poço seco, corda podre, o chão em volta liso, como se muita gente tivesse parado ali | Uma placa na beira: *"Aqui ele ouvia."* No fundo (descida curta), um nicho com um baú de mantimentos (pão, maçã) e a Cruz de Ferro riscada na pedra. Com Exposição alta, uma silhueta de chapéu mole sentada na beira, só por um instante |
| **Casa do ferreiro** (leste) | Forja apagada, bigorna rachada ao meio, ferramentas espalhadas | Falta o **martelo**. Ele está no campanário (ver o livro) |
| **Casa do Compadre** (sudeste, isolada) | A única com **telhado inteiro** e porta aberta. Mesa comprida posta para muita gente, pratos vazios | Uma **cama feita, de lençol branco**: a única limpa da vila. Para quem tem Exposição alta, o travesseiro está marcado com o nome de `{marcado}` |
| **Capela** (norte, no alto) | Pedra e tábua, sem teto, vitrais quebrados, bancos virados. Uma **mesa** com **cadeira a mais**, virada para a porta. **Uma vela acesa**, a única da vila | A vela é o ponto de descanso (devolve Lucidez; ver Perigos). No chão, sangue seco em arrastos até o campanário |
| **Campanário** (ao lado da capela) | Torre alta de pedra, meio desabada. O **sino rachado**, torto na viga | **Onde está o Livro do Sineiro**, num **atril** (lectern) ao pé do sino, com o martelo ao lado |
| **Cemitério** (atrás da capela) | Covas abertas, lápides sem nome, terra revirada **para fora** | Nenhum corpo. Em uma lápide, um risco de **Cruz de Ferro** |

### O lugar do livro (o objetivo)
- O **Livro do Sineiro** fica no **atril do campanário**, no alto, perto do sino.
- **Acesso:** uma escada de madeira queimada, com alguns degraus faltando. **Qualquer bloco resolve** (sem exigir item raro). Nada de grind.
- Quando alguém **abre o livro**, o sino rachado **toca uma vez**, fraco. É o aviso de que a vila "ouviu". Depois, silêncio.
- O livro é **um só**. Pode ser pego (cada jogador lê a cópia que o admin der, ou lê no atril). A decisão de cópia fica com o Dev.
- Fora do livro, o campanário tem **um vão quebrado** na parede: de lá, no horizonte, **dá para ver a direção da vila comum**. Pista visual de que uma coisa chama a outra.

## O que a vila entrega ao grupo
1. O **Livro do Sineiro** (história do mundo + primeira pista de como voltar: páginas 11 e 12).
2. A certeza de que o **sino é uma porta** e de que a vila comum marcada na Cena 1 ainda tem um sino vivo.
3. Os dois ganchos de personagem: **o Compadre** e **a Moça do Terço**.
4. Comida no poço e uma vela segura: um fôlego antes de seguir.

## Perigos (poucos, de propósito)
Criatura de **Sangue**: **o Faminto** (nome provisório; aldeão curvado, pele esticada, boca aberta demais, mãos longas). Mob simples,
poucas partículas (regras de `lore/propostas-elementos.md`).
- **De dia:** 3 ou 4 Famintos **parados dentro das casas**, de frente para a parede. Acordam com barulho (quebrar bloco, correr perto) ou luz forte colada. Dá para atravessar de lado, devagar.
- **De noite:** o sino rachado toca. Os Famintos **saem para as ruas** e andam devagar. **Nenhum é obrigatório de enfrentar.** Há rota pelas sombras até o campanário.
- **Poças que "sangram":** na praça e no curral, algumas poças vermelhas pulsam (partícula rala). Ficar parado nelas tira um pouco de **Lucidez**.
- **O Aldeão de Pé** (Aparição 2 do roteiro) pode aparecer na praça, **só para o jogador de maior Exposição**.
- **O Magarefe não aparece aqui.** Ele é da Casa do Açougueiro (Enigma 3). Aqui a ameaça é a vila inteira, devagar.
- **Refúgio:** dentro da capela, perto da **vela**, a Lucidez volta. Os Famintos não entram na capela com a vela acesa.
- **Sem grind de combate:** os Famintos não carregam nada que o grupo precise. Matar não destrava nada.
- Para o admin: `incursoes` e aparições podem ser desligadas por config; a vila funciona como cenário e livro mesmo assim.

## Clima (som, luz, cor)
- **Cor:** vermelho e preto de Sangue sobre cinza. Pedra, tábua carbonizada, terra avermelhada. Poucos pontos de cor: o vermelho dos estandartes e a vela.
- **Céu:** nublado e baixo. **Neblina avermelhada leve** rente ao chão, mais densa de noite. Cinza caindo devagar (partícula discreta).
- **Som:** quase silêncio. Vento, madeira estalando, um rangido de corrente. Sem música. De noite, o **sino rachado** toca grave, em intervalo longo.
  O silêncio entre os toques é a parte mais assustadora: não preenchê-lo.
- **Luz:** escura mesmo de dia (neblina). Tochas e fogueiras só onde o grupo as pôs. Nada acende sozinho, **exceto a vela da capela**.
- **Cheiro (som e partícula):** ferro. É a Aparição 1 (O Cheiro) do roteiro, mais forte aqui.

## As três pistas que levam o grupo até a vila
Regra das três pistas: cada pista sozinha já aponta o rumo, e as três juntas fecham a certeza.
Nenhuma é tutorial na tela. Todas usam o que o jogador já conhece de Minecraft.

1. **O sino ouvido de longe, à noite.** Um toque grave, rachado, diferente do sino da vila comum, vindo **sempre da mesma direção**
   (som posicional, mais alto quando se anda para lá). Começa na primeira noite do "Primeiro Sinal". Quem para e escuta percebe de onde vem.
2. **A vila comum aponta.** Na Cena 1, os aldeões não olham mais para o sino: **todos ficam parados, de frente para a mesma direção**,
   a da Vila Calada. Dentro de casa, a cama do aldeão sumido tem o lençol molhado de vermelho e a **cabeceira virada para lá**.
   Quem anda uns blocos nessa direção vê a neblina avermelhar.
3. **O mapa que aparece com o grupo.** Ao amanhecer da primeira noite, um **mapa de cartógrafo** surge no inventário de `{marcado}`, sem ninguém ter dado.
   Ele mostra **só uma rosa dos ventos e um sino rachado desenhado a tinta**; o terreno em volta só se desenha **conforme se anda** nessa direção.
   Nenhum texto. (Técnica: decisão do Dev.) Quem o desenhou fica claro na Casa do Cartógrafo.
- **Atalho tech (não é pista obrigatória):** o **Analisador de Sinal** capta o sino rachado como um sinal de baixa frequência e indica a direção.
- **Ajuda para o admin:** se o grupo passar muitas noites sem se mexer, o jogo avisa o admin ("{grupo} não seguiu o sino"), e o admin decide se solta um empurrão
  (o sino mais perto, um Faminto vagando até a vila comum).

## O que fica faltando
- Prévia do Modelador/Construtor antes de qualquer construção (decisão do Nikolas).
- Nome e desenho final do Faminto (Modelador, com ficha em `lore/criaturas.md`).
- Estandarte da Cruz de Ferro e atril/livro com textura nossa (Modelador).
- Sons: sino rachado grave, vento, arrasto (Sons).
