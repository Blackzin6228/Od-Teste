# Relíquias e espadas principais (PROPOSTA, versão 2)

> **Nada aqui é cânone até o Nikolas aprovar.** Esta versão junta a decisão nova do PR #11: **uma relíquia e uma espada por elemento**,
> e **a relíquia é o receptáculo da entidade**. Uma pessoa usou a relíquia e virou a entidade (ou foi tomada por ela).
> Quem usar a relíquia no jogo pode se tornar a entidade, com poder enorme e preço enorme.
> A entidade em si (aparência, dimensão, forma de chefe) está em `lore/entidades.md`.
>
> - **As relíquias NÃO são máscaras.** Cada uma é um objeto próprio: coração, livro, ampulheta, antena.
> - **Sem craft** (decisão do PR #9): relíquia e espada vêm por **chefe, evento ou ritual**. Oferenda de ritual não é receita, o ritual **entrega** a peça.
> - Tudo é original: os objetos, as pessoas e as histórias são nossos. Sem nomes, objetos ou textos de Ordem Paranormal (regra 3).
> - Cada nome tem **2 opções (A/B)**; o Nikolas escolhe, as duas ficam até lá. Nomes com (?): busquei na web e **não achei uso em outra obra**,
>   mas a busca foi rasa. Falta checagem final antes de virar item. Os personagens do passado têm nome e matrícula inventados, só para dar rosto.
> - Tamanhos em blocos (1 bloco = 16 px).
> - Ligação com a espinha (`lore/premissa.md`): cada relíquia é **um dos que vieram antes**, trancado num objeto.

---

## A regra comum: a Cessão

Vale para as quatro relíquias. Assim o Dev e o Integrador montam um sistema só.

**Quatro degraus de uso** (o jogador nunca vê número; sente pelo visual e pelo som):

| Degrau | O que é | O que acontece |
|---|---|---|
| 1. **Portar** | Relíquia equipada (Curios, ou na mão se não houver Curios). | Poder pequeno e custo pequeno. A Exposição sobe devagar. A entidade "percebe" o dono. |
| 2. **Ceder** | O jogador segura a tecla de afinidade e **entrega o corpo** à entidade. | **Forma Cedida**: transformação temporária (modelo próprio, ver `lore/entidades.md`). Poder grande. A Lucidez cai a cada segundo. |
| 3. **Ser tomado** | A Lucidez zera na Forma Cedida, ou o dono insiste demais. | A entidade **assume o controle** por cerca de 1 minuto (efeito diferente em cada elemento). Depois a relíquia **volta ao altar** da dimensão. |
| 4. **Ficar** | Final de longo prazo (liga com Transcender nível 4 e Exposição "Atravessado", `docs/sistemas.md`). | O personagem **vira a entidade**: sai do jogo como personagem jogável. Vira o próximo rosto do elemento (chefe de evento no futuro). Só com aviso claro e confirmação em duas etapas. |

**Quem pode usar:** só quem tem a **Marca do mesmo elemento** (estágio 2 ou mais) e Exposição "Fundo" ou acima.
O jogador **não escolhe** a Marca (decisão de 08/11), então o poder vem do que ele já viveu, não de uma escolha de menu.
Quem usar sem Marca do elemento é **rejeitado**: dano, Lucidez em queda livre e a relíquia o larga. Nunca vira a entidade.

**Preço comum:** Lucidez por segundo na forma, Exposição que não desce, um pedaço da Marca que fica fixo, e a **voz** muda (liga sozinha, `docs/voz.md`).

### O que impede abuso no servidor (valem para as quatro)
1. **Uma por servidor** (proposta). A relíquia é única. Se um jogador a carrega, ninguém mais a tem. Evita corrida e evento de farm.
   *Alternativa:* cada grupo tem a sua, mas a Forma Cedida fica mais fraca. O Nikolas decide (pergunta 4).
2. **Vinculada à alma:** não vai para baú, Ender Chest nem troca. Se o dono **morre na forma** ou desconecta, a relíquia volta ao altar do elemento (não cai no chão para ser roubada).
3. **Tempo limitado e recarga em tempo real:** Forma Cedida dura 60 a 90 s e só volta depois de uma recarga longa (proposta: 24 h reais). O dono pode escolher sair antes.
4. **Poder pleno só no lugar do elemento.** Na dimensão do elemento e na Clareira, tudo funciona. Fora delas, a forma é reduzida.
5. **PvP:** contra outro jogador, o dano da forma cai pela metade e a entidade **não toma** quem estiver em área protegida.
6. **Sempre existe saída:** o dono da **espada do mesmo elemento** pode "cortar" a Cessão de um aliado possuído (a espada nasceu do mesmo passado). Há também um ritual de renúncia (pergunta 2).
7. **Admin:** comandos para encerrar a Cessão, devolver a relíquia ao altar e ver o histórico. O narrador pode ligar/desligar a tomada para evento.
8. **Nada vendável.** Nenhuma relíquia, espada ou forma dá vantagem comprada (regra 9). Só cosmético.

---

## RELÍQUIAS (uma por elemento)

### SANGUE

- **Nomes:** A) **Coração de Ferrugem** (?)  B) **Cálice do Faminto** (?)
- **Que objeto é (não é máscara):** um **coração de metal enferrujado**, do tamanho de um punho. B é o mesmo conceito como uma taça de ferro sempre cheia de líquido vermelho.
- **Aparência (para modelar depois):** coração de **0,4 bloco**, preso por correntes curtas a uma **gaiola de espinhos**, que se abre aos poucos como costelas. Rachaduras soltam luz vermelha. Cheiro (som) de ferro molhado. **Animação:** bate devagar, acelera quando há inimigo ou ferido por perto. (B: a taça ferve em vez de bater.)
- **Materiais:** ferro escuro, carne cozida (textura), vidro vermelho emissivo.
- **Quem a usou e virou entidade:** **Irene Valadares**, cozinheira de dia, agente de noite, matrícula 0311. O Ventre não deixava a passagem fechar e o grupo dela estava do outro lado. Ela segurou o coração e disse *"fica comigo e deixa eles passarem"*. O coração bateu no peito dela. A passagem fechou **com ela dentro**, e ela nunca mais largou. Cuidar e prender viraram a mesma coisa. Hoje é **A Hospedeira** (`lore/entidades.md`).
- **Como obter (sem craft):** **chefe** do Ventre. O Coração Aberto (chefe de área, ver `lore/criaturas.md`) guarda o coração verdadeiro. Cai quando o grupo o derrota **três vezes** e depois faz um **ritual de giz com uma gota do próprio sangue** (custa vida, não item). O ritual **entrega**, não fabrica.
- **Ao portar (degrau 1):** golpes curam um pouco. O dano sobe quando a vida está baixa.
- **Ao ceder (degrau 2):** vira a **Forma Cedida da Hospedeira**: abrigo que reparte o dano do grupo, cura em área, portas de carne que bloqueiam corredor, golpes que se alimentam.
- **Custo:** vida máxima cai aos poucos enquanto está equipada. A fome nunca sacia. Na forma, o jogador **não consegue se afastar do grupo** (saudade): longe demais, a Lucidez cai mais rápido.
- **Se for tomado:** a Hospedeira **tranca o grupo**: salas de carne se fecham em volta dos aliados por cerca de 1 minuto, e o dono "acolhe" quem está dentro. Os aliados têm que **abrir um caminho** (ou usar a espada de Sangue para cortar a Cessão).

### CONHECIMENTO

- **Nomes:** A) **Livro-Sem-Fim** (?)  B) **Lente do Escriba** (?)
- **Que objeto é (não é máscara):** um **livro grosso**, de páginas que continuam sendo escritas. B é uma **lente grande de cabo dourado**, que mostra o texto por trás do texto.
- **Aparência:** livro de **0,5 bloco**, fechado por tiras de couro, capa de pergaminho com uma **moldura dourada** no meio (um olho sem pupila gravado). Páginas soltas em órbita. **Animação:** as páginas viram sozinhas. A capa se abre um dedo quando alguém mente por perto. (B: a lente gira devagar e o vidro "lê" o que está atrás.)
- **Materiais:** couro velho, ouro fosco, papel, tinta dourada.
- **Quem a usou e virou entidade:** **Teodoro Antunes**, escrivão do Gabinete, matrícula 0087. Copiou o primeiro dossiê do lugar. O livro **continuou escrevendo depois que ele parou**. Ele leu a última página, que falava dele, e entendeu cada palavra sem esquecer nenhuma. Não conseguiu mais fechar os olhos. Hoje é **O Leitor Sem Nome** (`lore/entidades.md`).
- **Como obter (sem craft):** **evento de investigação**. Reunir as **12 páginas** espalhadas pelo mundo e lê-las juntas num altar da Biblioteca Muda. O último salão libera o livro numa mesa de copista, junto da espada. O grupo decide quem leva qual.
- **Ao portar:** revela escrita escondida, pistas e coordenadas de estruturas. Cada dica "gasta uma página".
- **Ao ceder:** **Forma Cedida do Leitor**: marca inimigos com a fraqueza deles, reescreve o ambiente por poucos segundos (abre uma porta, apaga uma armadilha) e silencia a voz de quem estiver perto.
- **Custo:** a Lucidez cai ao usar. O dono **esquece**: nomes viram "o de capa azul", a bússola gira, o mapa pessoal perde pontos fora da Biblioteca, e o inventário se embaralha de leve.
- **Se for tomado:** o Leitor **lê em voz alta**, no chat de cena, as Páginas que o dono já coletou, em tom de sentença. O dono fica imóvel, de olhos dourados, por cerca de 1 minuto. A informação **vai para o grupo** (bom para investigação, ruim para quem queria segredo). Ao acabar, o mapa dele fica em branco por um dia.

### MORTE

- **Nomes:** A) **Ampulheta Quebrada** (?)  B) **Sino do Último Dia** (?)
- **Que objeto é (não é máscara):** uma **ampulheta rachada**. B é um **sino pequeno de ferro preto**, que bate sem balançar. (Atenção: o sino se parece com o sino da premissa. Se o Nikolas preferir que o sino da floresta seja único e especial, usar só a Ampulheta.)
- **Aparência:** ampulheta de **0,6 bloco**, vidro fosco, base de osso, armação de ferro preto, areia **cinza que sobe** em vez de cair. **Animação:** a areia sobe devagar. Ao ativar, um estalo e a areia cai de uma vez. (B: o badalo não toca o sino, o som chega depois.)
- **Materiais:** vidro fosco, osso, ferro preto, cinza.
- **Quem a usou e virou entidade:** **Aurélio Matos**, relojoeiro, matrícula 0156. Queria voltar **uma hora** no tempo, antes do toque do sino. Virou a ampulheta de cabeça para baixo. A areia subiu e ele **não conseguiu desvirar**. Parou o próprio relógio para não envelhecer e ficou sem começo e sem fim. Hoje é **A Hora Parada** (`lore/entidades.md`).
- **Como obter (sem craft):** **chefe** do mausoléu do Ermo de Cinzas (o chefe do mausoléu ainda precisa de nome, mesa de criaturas). Só cai se o jogador entrar **sem curar nenhuma vez** (desafio). Pegar a relíquia **faz o relógio parar** para todos na área.
- **Ao portar:** uma vez por dia, desfaz o último golpe fatal (volta alguns segundos).
- **Ao ceder:** **Forma Cedida da Hora Parada**: congela uma área por poucos segundos, envelhece os inimigos (lentos, armadura fraca) e dá o "último segundo" para sobreviver a um golpe fatal.
- **Custo:** cada uso envelhece o rosto e o cabelo (a Marca de Morte avança), cura mais devagar e a sombra do dono chega sempre atrasada.
- **Se for tomado:** o dono **vira estátua de cinza** por cerca de 1 minuto: não se move, não pode ser morto, mas o mundo continua ao redor. Os aliados têm que **proteger a estátua** até a areia descer. Quem sai da frente perde a relíquia, que volta ao altar.

### ENERGIA

- **Nomes:** A) **Pilha de Eco** (?)  B) **Antena Torta** (?)
- **Que objeto é (não é máscara):** uma **antena de cobre retorcida**. A) é a **bobina** que fica no coração da antena (a "pilha"); B) é a antena inteira, que se usa nas costas ou na mão. O modelo é o mesmo.
- **Aparência:** antena de **1 bloco de altura**, cobre retorcido, uma **bobina roxa** girando e esferas de luz ciano em órbita. **Animação:** a bobina vibra, faíscas sobem em espiral. Estática no som. Dá pequenos choques em quem toca sem luva.
- **Materiais:** cobre, vidro roxo, fio trançado.
- **Quem a usou e virou entidade:** **Bruna Tavares**, técnica de rádio, matrícula 0204. Subiu numa torre e **ligou a antena para pedir socorro**. Ninguém respondeu. Ela não conseguiu desligar, porque desligar era desistir. Foi virando o sinal. Hoje é **O Sinal Mais Alto** (`lore/entidades.md`).
- **Como obter (sem craft):** **ritual** num pico de tempestade da Rede. Oferenda de **Núcleo Trêmulo** + descarga de **raio** (evento climático). O raio **entrega** a antena.
- **Ao portar:** carrega com o dano que recebe. Solta descarga em cadeia que paralisa máquinas hostis e inimigos de Morte.
- **Ao ceder:** **Forma Cedida do Sinal**: pulo curto entre pontos ("mudar de canal"), descarga em cadeia grande, religa máquinas amigas e fala com o grupo a qualquer distância.
- **Custo:** o dono **ouve estática** o tempo todo. HUD e eletrônicos pessoais falham em horários aleatórios. Choque aleatório.
- **Se for tomado:** o Sinal **muda o dono de canal**: teletransportes aleatórios curtos dentro de um raio por cerca de 1 minuto, e o chat de cena do dono sai em estática picotada com coordenadas. As máquinas dos aliados perto piscam.

---

## ESPADAS (uma por elemento)

> **Sem craft, sem receita.** Cada espada **já existe** no mundo e é entregue por evento, chefe ou ritual.
> **Ligação com a história:** a espada é **o que sobrou do ofício da pessoa que virou entidade**, uma peça que ficou para trás quando ela foi tomada.
> **Função de segurança:** o dono da espada pode **cortar a Cessão** de um aliado possuído pela entidade do mesmo elemento (custa Lucidez; a espada "lembra" do corpo que a entidade tomou).
> **Evolui em 3 estágios**, por feitos, sem item fabricado: Adormecida, Desperta, Plena. A espada guarda o histórico. Roubar não pula estágio: ela lembra quem a despertou.
> A espada **não é** a relíquia: sozinha ela não transforma ninguém. Quem tem as duas do mesmo elemento ganha um bônus pequeno, sem poder quebrar o jogo.

### SANGUE
- **Nomes:** A) **Rubra** (?)  B) **Dentes-de-Mãe** (?)
- **História:** a **faca de cozinha** de Irene, que cresceu com o Ventre. A lâmina foi ganhando dentes. Por isso B: dentes de mãe, de quem cuida e também morde.
- **Aparência:** lâmina **larga e levemente curva**, **1,4 bloco**, com fileiras de dentes no fio. Cabo envolto em couro vermelho. Guarda em forma de **mandíbula**. Brilho vermelho pulsante nas rachaduras. **Animação:** a mandíbula estala ao acertar.
- **Poder:** golpes causam **sangramento** que se acumula. Quando enche, um golpe forte abre uma ferida em área.
- **Custo:** só fica forte com a vida do dono abaixo de 70%.
- **Como obter:** **chefe**. Cai do **Coração Aberto** na primeira vez que o jogador o vence **sem usar poção de cura**.
- **Evolui:** *Desperta* ao vencer um combate de evento de Sangue com a vida abaixo de 30%. *Plena* em ritual de giz no Ventre, com uma gota do próprio sangue (custa vida).
- **Ligação com a relíquia/entidade:** corta a Cessão de um aliado tomado pela Hospedeira (abre as paredes de carne). Com o Coração de Ferrugem no mesmo servidor, a espada **sente o coração bater** e tem sangramento mais forte contra quem estiver preso.

### CONHECIMENTO
- **Nomes:** A) **Sentença** (?)  B) **Pena de Ouro** (?)
- **História:** a **pena** com que Teodoro anotava o dossiê. Ficou tão afiada de tanto escrever que virou lâmina. B lembra a pena. A é o que ela escreve: a última frase.
- **Aparência:** lâmina **fina e reta**, **1,3 bloco**, com **linhas de escrita** que se acendem. Guarda em forma de **balança**. Dourado frio. **Animação:** as letras correm pelo fio antes do golpe.
- **Poder:** marca o inimigo com uma "sentença": o próximo golpe causa dano extra e ignora parte da defesa.
- **Custo:** gasta Lucidez a cada marca. O dono "esquece" onde guardou itens (o inventário embaralha de leve).
- **Como obter:** **evento**. Surge junto da relíquia no último salão da Biblioteca Muda, numa mesa de copista, depois do enigma. O grupo escolhe quem leva qual.
- **Evolui:** *Desperta* ao decifrar o dossiê completo de um chefe. *Plena* em ritual de leitura (ler em voz alta a "sentença" do último agente do elemento, comando de cena).
- **Ligação com a relíquia/entidade:** corta a Cessão de um aliado tomado pelo Leitor: a lâmina **lê a frase final em voz alta** e o Leitor "perde a linha". Junto com o Livro-Sem-Fim, as sentenças aparecem como notas dentro do livro (pista, não poder).

### MORTE
- **Nomes:** A) **Foice de Séculos** (?)  B) **Fio-Baixo** (?)
- **História:** o **ponteiro dos minutos** do relógio de Aurélio, que ele arrancou quando parou o tempo. Ficou comprido, curvo, afiado. B vem de "fio" (de corte) e de "baixar" (o tempo).
- **Aparência:** **foice-espada de 1,6 bloco**. Haste de osso escuro, lâmina cinza **curva em espiral anti-horária**, pontas lascadas. Azul-frio nas bordas. **Animação:** cinza cai da lâmina a cada passo. Ao golpear, tudo fica em câmera lenta por um instante.
- **Poder:** cada golpe "envelhece" o alvo (lento, armadura fraca). Em vida baixa, execução.
- **Custo:** o dono nunca sente fome plenamente. A regeneração natural para.
- **Como obter:** **ritual**. Num túmulo antigo do Ermo de Cinzas, acender a **vela de osso** e esperar um minuto sem se mexer. O túmulo se abre e entrega a espada.
- **Evolui:** *Desperta* ao derrotar o **Coveiro de Séculos** (chefe) ou o equivalente de evento. *Plena* em ritual no mausoléu do relógio que ainda corre, devolvendo à cova um objeto pessoal do dono.
- **Ligação com a relíquia/entidade:** corta a Cessão de um aliado tomado pela Hora Parada: a ponta toca a estátua de cinza e o tempo volta a correr para ela. Perto da Ampulheta, os ponteiros do relógio de pulso do jogador giram ao contrário.

### ENERGIA
- **Nomes:** A) **Fulgor** (?)  B) **Fio Solto** (?)
- **História:** o **cabo cortado** da antena em que Bruna se pendurou. O cabo ficou aceso, com a ponta solta, procurando onde encostar. B é o cabo. A é o brilho.
- **Aparência:** espada de **lâmina translúcida de 1,3 bloco**, como um **tubo de neon** com um fio de cobre em espiral. Punho de borracha preta com pinos de metal. Roxo/magenta com ciano nas pontas. **Animação:** a lâmina "falha" (pisca) e solta faíscas.
- **Poder:** a cada golpe, chance de descarga em cadeia. Com carga cheia, dá um salto curto (dash).
- **Custo:** de vez em quando o golpe falha (aleatoriedade) e a lâmina desliga por 2 segundos.
- **Como obter:** **evento climático**. A espada cai num ponto de raio durante a tempestade elétrica da Rede. Quem segura metal condutor na hora perde a chance.
- **Evolui:** *Desperta* ao sobreviver a uma tempestade inteira da Rede segurando a espada. *Plena* ao derrotar a **Sobrecarga** (chefe) com a espada.
- **Ligação com a relíquia/entidade:** corta a Cessão de um aliado tomado pelo Sinal: a ponta "aterra" a carga e o dono volta ao canal certo. Com a Antena Torta por perto, a espada **sintoniza**: a descarga em cadeia pula de um aliado para outro sem machucar.

---

## Hierarquia de poder
1. **Entidades** (uma por elemento), o que a relíquia guarda. Ver `lore/entidades.md`.
2. **Relíquias** (uma por elemento), o receptáculo.
3. **Espadas** (uma por elemento), que evoluem em 3 estágios.
4. Armaduras sem elemento fixo (o visual muda com a Marca).

## Pontos para o Nikolas decidir
1. Qual nome (A ou B) para cada uma das 8 peças (as duas opções ficam até lá).
2. **Renúncia:** dá para largar a relíquia por um ritual (e perder a Marca de um estágio), ou só a espada resolve? Proposta: os dois.
3. **Ficar (virar a entidade):** o personagem sai do jogo para sempre, ou volta como chefe de evento que o grupo enfrenta? Proposta: sai como personagem e vira o próximo chefe de evento (com aviso e confirmação em duas etapas).
4. **Uma por servidor ou uma por grupo?** Proposta: uma por servidor.
5. Recarga da Cessão: 24 h reais serve, ou prefere dias de jogo?
6. As relíquias entram como **item de Curios** (equipável) ou **item de mão**? A proposta usa Curios quando existir, com fallback para a mão.
7. Os 3 estágios das espadas (Adormecida, Desperta, Plena) servem? A função de "cortar a Cessão" no dono da espada serve?
8. O Morte B (Sino do Último Dia) fica ou sai para não competir com o sino da floresta?
9. ~~O Medo ganha relíquia/espada?~~ Resolvido (07/11): não.
10. ~~Espada/relíquia com craft?~~ Resolvido (PR #9): não.
