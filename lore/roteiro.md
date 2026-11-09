# Roteiro (mesa lore)

Status: **rascunho para o Nikolas aprovar.** O Ato 1 foi **refeito** em 08/11/2026 na nova direção (produção de qualidade: estruturas grandes e enigmas que saem do jogo).
Os outros atos são esqueleto. Base: `lore/canon.md`, `docs/decisoes.md`, `docs/decisoes/2026-11-08-producao-estruturas-e-enigmas-fora.md`.
Detalhes do Ato 1 em: `lore/fichas-estruturas.md` (os lugares, incluindo a **Vila Calada**), `lore/enigmas-fora.md` (o que se resolve fora do jogo) e `lore/livro-do-sineiro.md` (o texto do livro).
**Atualização de 09/11/2026:** a **Vila Calada** (vila destruída) é o **destino do Ato 1**; lá o grupo acha o **Livro do Sineiro** e a primeira pista de como voltar à realidade.
A vila comum da abertura é o primeiro gancho. Decisão: `docs/decisoes/2026-11-09-vila-calada-destino-ato1.md`.

## Regras do roteiro
- **Os protagonistas são quem está jogando.** Nenhum texto usa nome fixo de amigo. O mod troca marcadores na hora de mostrar:
  `{jogador}` (quem lê), `{grupo}` (todos do grupo, em lista), `{parceiro}` (outro do grupo, sorteado), `{anfitriao}` (quem está
  com a maior Exposição). Serve igual para o grupo de amigos e para um servidor grande.
  (Nos textos da Vila Calada, `{marcado}` é só outro nome de `{anfitriao}`.)
  Marcadores de **elemento** (ver `docs/decisoes/2026-11-08-elemento-do-jogador.md`): `{escolhido_sangue}`, `{escolhido_conhecimento}`,
  `{escolhido_morte}`, `{escolhido_energia}`. Sem escolhido no grupo, cai em `{anfitriao}` (ou no que o admin escolher). **Nunca nome de jogador no repositório.**
- **O admin que joga não tem elemento.** No texto é `{ilegivel}`. Ver "O Ilegível".
- **Cada jogador (menos o `{ilegivel}`) tem um elemento e não sabe qual.** Descobre jogando. A Marca é só a do elemento dele; os enigmas e a entidade
  do elemento falam com o escolhido; espada e artefatos do próprio elemento custam menos para ele (sem deixar os outros inúteis).
- **A história chega por quatro caminhos:** (1) **lugares grandes** com pistas posicionadas, (2) **eventos ao vivo** soltos pelo admin,
  (3) **enigmas dentro do jogo** e (4) **enigmas fora do jogo** (site secreto, áudio, imagem, vídeo, documento) que voltam ao jogo como **palavra**.
- **Ritmo:** enigma resolvido libera o próximo **sozinho**. Aparição grande de entidade só quando o **admin** solta.
  O jogo avisa o admin quando o grupo chega perto ("{grupo} resolveu a Mesa: evento do Diabo pronto").
- **Pouco grind.** Nada de juntar recurso para avançar. Tech e magia dão **atalhos e camadas extras**, nunca bloqueio.
- **Todo enigma de fora tem plano B dentro do jogo** (Dica em livro liberada pelo admin). Site fora do ar não trava a história.
- **Nenhum número na tela** (regra do Nikolas). Tudo em palavra, som e imagem. O que volta ao jogo é palavra.
- **Texto do jogo:** frases curtas, segunda pessoa, sugere e não explica. O preço vem junto com o poder.
- **Estética Minecraft, tom sério.** Cenário liminar brasileiro: estrada de terra, carro parado, frigorífico de beira de estrada, pousada, torre de rádio, fazenda.
- Texto escondido em vermelho existe em documentos do jogo. Só aparece com Exposição média/alta ou com luz negra (tech).

## Colocação dos lugares (substitui a "regra de manifestação", decisão de 08/11)
O mundo do servidor (ATM10, pré-gerado e já explorado) **não será refeito**. Agora:
- Cada lugar do Ato 1 é uma **schematic** (`.nbt`) que o **Nikolas coloca no mundo**, no ponto que fizer sentido (Create Schematicannon ou comando).
- O Dev entrega um comando para **registrar a posição** de cada lugar. As pistas que dizem "rumo e distância" (Mapa, Livro-Caixa) usam a posição registrada.
  Assim a pista é verdadeira em qualquer mundo.
- A "manifestação por evento" (o lugar nascer ao ler a pista) fica como **opção** futura. Não é obrigatória.
- Lugares dos Atos 2 em diante ficam em dimensões novas (não existem no mundo gerado) e também podem ser schematics.
- **Vilas** continuam, como **detalhe**: uma vila próxima de cada lugar, com NPCs que contam a lenda, placas, rádio. Nunca o centro de um enigma.

## O Ilegível (o admin que joga)
O admin que joga não tem elemento. O paranormal lê todo mundo; dele, não lê nada. No texto é `{ilegivel}`.
- **As entidades se confundem com ele.** Magarefe demora a notar, o Pendurado nunca aparece para ele, mas ele **não é protegido**: o que o acha, acha de verdade.
  Texto, uma vez: *"Alguma coisa olhou para você e não achou nada."*
- **Os enigmas dentro do jogo não falam com ele.** Cálice, vela e segunda leitura não reagem. Ele resolve junto, sem o sinal dos escolhidos.
  Os **enigmas de fora** são iguais para todos (site, áudio, vídeo): ele é um dos que mais se beneficia, porque não depende do sinal do jogo.
- **Ele vê o que fica entre os elementos** (a silhueta no lugar a mais antes da hora, a segunda porta que só aparece de lado). Um momento por Ato, no máximo, solto pelo admin.
  Só serve se ele **contar para o grupo**. Lucidez baixa faz o mundo mentir, então o que ele conta precisa de confirmação.
- Sem Marca, sem espada barata. Armas dos elementos funcionam no custo cheio.
- Se o admin não estiver jogando, nada disso aparece.

## Os dois medidores
- **Exposição** (só sobe). Mede quanto do paranormal já entrou em você. Sobe ao ler coisa maldita, entrar em lugar marcado, ver aparição, aceitar pacto.
  **Mais Exposição = vê mais e é visto mais.**
- **Lucidez** (cai e volta). É a cabeça inteira. Cai com susto, errar enigma, ficar sozinho no escuro. Volta perto de luz, perto de um amigo (voice chat conta) e com a Vela de Sebo.
  Quando acaba, o mundo **mente**: pista falsa e aparição falsa só para ele. A saída é confirmar com o grupo.
- Regra de ouro: **Exposição abre a porta, Lucidez paga o pedágio.**

## Os cinco atos (visão geral)

| Ato | Nome provisório | O que acontece | Estado |
|---|---|---|---|
| 1 | A Floresta | Cinco lugares, enigmas dentro e fora do jogo, o Sangue e o Diabo | **refeito abaixo** |
| 2 | O Outro Lado | O paranormal é um lugar. Primeiras dimensões novas | esqueleto |
| 3 | As Mesas | Uma entidade por elemento | esqueleto |
| 4 | O Fundo | O Medo (pendente de decisão do Nikolas) | esqueleto |
| 5 | O Caminho de Volta | Reunir as 4 relíquias e achar a saída pelo barulho de metal | esqueleto |

---

# ATO 1 — A FLORESTA

Elemento em foco: **Sangue** (vermelho, fome, dor, bestialidade). Entidade: **o Diabo**.

## O percurso em uma olhada
**Objetivo do Ato 1: chegar à Vila Calada** (destino) e ler o Livro do Sineiro. O jogador não recebe tutorial: a história puxa.
A abertura na vila comum é o gancho; três pistas apontam o rumo; a Vila Calada entrega o livro; o livro dá o "porquê" dos cinco lugares seguintes.

```
Vila comum (abertura: sino sozinho, aldeão some, vila parada, Morse FERRO na placa)
        |  três pistas: sino ao longe, aldeões virados, mapa de cartógrafo
        v
VILA CALADA (destino)  --Livro do Sineiro--> primeira pista de como voltar (págs. 11 e 12)
        |  pág. 13: ensina a ler o sino (dois tons, cinco em cinco) e liga ao Enigma 1
        v
Clareira e o Carro  --Fita (Morse + espectrograma)-->  FERRO
        |  Mapa Rasgado (César)
        v
Frigorífico do Km 9  --ganchos, Livro-Caixa, Faca de Osso, crachá (endereço do Arquivo)-->
        |  Livro-Caixa diz: "Pousada do Km 12"
        v
Pousada do Km 12  --vídeo da câmera de segurança--> senha do Arquivo
        |
        v
Arquivo do Km 9 (site secreto)  --memorando, foto, áudio ao contrário--> palavra de retorno
        |
        v
Casa da Repetidora  --console: silencia o sino--> localização da Fazenda
        |
        v
Fazenda do Sino Mudo (Casarão + Capela)  --Mesa do Pacto--> Evento do Diabo --> Pista do Ato 2
```

**O fio da descoberta (o que leva a quê):**
0. **A vila destruída.** A vila comum chama (sino, aldeões, cama, mapa). Na Vila Calada, o livro diz o que aconteceu, que há uma porta aberta e que o sino ensina a ouvir.
   O sino tocando uma vez ao abrir o livro é o aviso de que algo, longe, respondeu.
1. **A Clareira.** A Fita dá a palavra; a caixa dá o Diário e a metade do Mapa.
2. **O Frigorífico.** O Mapa leva lá; os ganchos dão o Livro-Caixa, a outra metade e a Faca de Osso.
3. **A Pousada e o Arquivo.** O Livro-Caixa leva à Pousada; o vídeo abre o Arquivo; o Arquivo dá a palavra de retorno.
4. **A Repetidora.** A palavra cala o sino de metal; o telégrafo dá a Fazenda.
5. **A Fazenda.** A Mesa do Pacto chama o Diabo.
O passo zero dá o "porquê". Os passos de 1 a 5 funcionam sozinhos, para quem chega sem ele.

Nenhuma etapa pede recurso. Os enigmas de fora têm três níveis de dificuldade; o grupo só precisa passar o do meio.
Quem pula o livro ainda resolve tudo pelo caminho dos cinco lugares: o livro dá contexto e a pista de volta, não trava nada.

## Cena 0 — A vila comum (abertura, primeiro gancho)
Já programada (`docs/decisoes/2026-11-09-vila-como-abertura.md`). Serve para ensinar o paranormal sem tutorial.
- O sino da vila comum toca sozinho. Um aldeão some. Os outros ficam parados. Na placa, o Morse **FERRO**.
- Primeira Exposição e primeira queda pequena de Lucidez. O jogador aprende que o mundo "responde".
- **Gancho:** a partir da primeira noite o grupo começa a receber as **três pistas** (abaixo). Elas apontam para a Vila Calada, não para a Clareira.

## Cena 1B — A Vila Calada (o destino do Ato 1)
Ficha: `fichas-estruturas.md`, "Vila Calada". Livro: `livro-do-sineiro.md`.
Estrutura grande, colocada pelo admin (comando de admin, sem apagar construção de jogador). A posição registrada é a que as três pistas usam.

**As três pistas (nenhuma é tutorial; cada uma sozinha aponta, as três fecham a certeza):**
1. **O sino ao longe, à noite.** Toque grave e rachado, sempre da mesma direção.
2. **Os aldeões e a cama.** Todos parados de frente para a mesma direção; a cama do aldeão sumido, molhada de vermelho, com a cabeceira virada para lá.
3. **O mapa de cartógrafo.** Aparece no inventário de `{anfitriao}` ao amanhecer; só uma rosa dos ventos e um sino rachado, e o terreno se desenha conforme se anda.
- Atalho tech: o Analisador de Sinal capta o sino. Se o grupo não se mexer por muitas noites, o jogo avisa o admin ("{grupo} não seguiu o sino").

**O que se faz lá (sem recurso e sem combate obrigatório):**
- Atravessar a vila (campo queimado, praça e poço, capela). Famintos parados de dia, soltos de noite. Rota pelas sombras até o campanário. A vela da capela devolve Lucidez.
- Subir ao campanário (qualquer bloco serve). O **Livro do Sineiro** está no atril. Abrir o livro faz o sino tocar uma vez.
- No vão quebrado do campanário, avista-se a direção da vila comum: uma coisa chama a outra.

**O que o livro entrega:**
- **Pág. 11 e 12 (para todos): a primeira pista de como voltar.** O sino precisa tocar ao contrário. Para isso, quatro portas pequenas precisam responder
  (a que sangra, a que cala, a que lembra, a que acende) e há uma quinta que é parede. O Sineiro só fechou uma, a que sangrava, e mal.
  Isto é o fio dos Atos: o Ato 1 é a porta que sangra (o Diabo); Atos 3 e 4 são as outras; o Ato 5 é o sino ao contrário.
- **Pág. 13:** ensina a ler o sino de graça (dois tons, cinco em cinco). Ponte para o Enigma 1 (Morse) e o 2 (César, deslocamento 5), só para quem leu.
- **Pág. 14:** em branco; com Exposição mostra que o grupo foi chamado ("sua cama aqui já estava feita").
- Ganchos de personagem: o Compadre e a Moça do Terço (casa dela com os botões de pedra), a mesma do Terço no cofre da Pousada.
- Para `{ilegivel}`: a vela da capela acende normalmente; as páginas em vermelho não aparecem para ele (sem Exposição que o leia). Ele pode ver, uma vez, o Aldeão de Pé no poço, de lado.

**Depois do livro:** o grupo sabe que há o que investigar e por quê. A Fita da Clareira e os cinco lugares seguintes são o caminho para fechar a porta que sangra.

## Cena 1 — A Clareira (prólogo curto)
**Vem da Cena 1B.** Na noite depois do livro, o sino rachado da Vila Calada não toca. Quem fica atento ouve, longe, um rádio de carro chiando no lugar dele.
O grupo **já está do outro lado** desde que entrou na floresta. Não há tutorial em tela.
- **Gatilho:** o admin solta o evento "Primeiro Sinal" (ou o mod solta sozinho na primeira noite de neblina depois que alguém do grupo abriu o Livro do Sineiro; sem o livro, na primeira noite de neblina depois de ativado). O grupo é guiado à Clareira (a schematic já está colocada).
- Cada jogador recebe a mochila com o **próprio nome** bordado: lanterna que falha, rádio sem sinal, caderno em branco.
- Texto de abertura (uma vez, letras pequenas):
  > *{grupo}. Vocês ouviram o barulho e entraram. Vocês nunca saíram. Só esqueceram.*
- Som: o barulho de metal arrastando, de 2 em 2 minutos, **começa a ser ouvido no mundo todo** (mais tarde é a chave do Ato 5; no Ato 1 vem da Repetidora).
- **Enigma 1: a Fita da Clareira** (fora do jogo, ver `enigmas-fora.md` E1). O toca-fitas do carro toca Morse; a palavra abre a caixa de ferramentas.
  Dentro: Diário p. 1 e a metade esquerda do **Mapa Rasgado** (E2, César).
  **Ponte com a Vila Calada:** quem leu a pág. 13 do Livro do Sineiro já sabe ouvir os dois tons e contar de cinco em cinco; o Morse e o César ficam mais fáceis. Quem não leu resolve igual.
- A Clareira vem depois da Vila Calada no caminho normal, mas não é obrigatória nessa ordem (o grupo pode achar o carro antes).
- Lucidez e Exposição já mexeram na abertura e na Vila Calada. A grande queda continua no Frigorífico.

## Cena 2 — O Frigorífico do Km 9 (a primeira grande construção)
Ficha: `fichas-estruturas.md`, estrutura 2. O Mapa aponta o lugar (rumo e distância reais).
- **Chegada:** o Frigorífico é grande demais para ser olhado de uma vez. O pátio, os caminhões velhos, o portão de ferro e a porta lateral.
  Quem ouviu o aviso escondido da fita entra pela lateral; quem não, entra pela frente e **acorda o Magarefe**.
- **Enigma 3 (dentro do jogo): os quatro ganchos.** Ordem das alavancas: **Cão, Vaca, Lebre, Javali.** O Diário p. 2 (no balcão da loja) diz a ordem em verso:
  > *Primeiro o que obedece. Depois o que dá leite. Depois o que foge. Por último, o que morde.*
  Vídeo opcional (E7): a Câmera do Frigorífico mostra a ordem pelo **som** das alavancas. Segunda via.
- Acertando, abre a câmara fria: **Livro-Caixa**, **metade direita do Mapa**, **Faca de Osso**.
- **Enigma 4 (fora do jogo): o Livro-Caixa** (E3, símbolos nossos, análise de frequência). Texto claro: *O DIABO NAO ROUBA. ELE ACEITA. VENHAM A MESA QUANDO O SINO CALAR.*
  No verso, em Morse, o caminho para a **Pousada do Km 12**.
- **No escritório do gerente:** o crachá com o endereço do Arquivo (site secreto) e o usuário. Falta a senha.
- **Exposição:** média faz as plaquinhas se mexerem; alta mostra a ordem inteira, mas o Magarefe sente de longe.
- **Lucidez:** erro nos ganchos tira de quem está lá dentro; acertar devolve um pouco.
- **Tech:** o **Medidor de Resíduo** apita mais forte perto do gancho certo.

## Cena 3 — A Pousada do Km 12 e o Arquivo (o primeiro enigma que sai do jogo)
Ficha: estrutura 3. O Livro-Caixa diz o caminho.
- **Dentro da Pousada:** livro de hóspedes (o último nome é `{anfitriao}`, tinta fresca), a TV da recepção com a **fita VHS** da câmera de segurança.
- **Enigma 5 (fora do jogo): o vídeo da câmera de segurança** (E4). Cerca de um minuto, em preto e branco: o homem de casaco no corredor, o quadro de chaves com a palavra a lápis.
  A palavra é a **senha do Arquivo do Km 9**. Pausar e dar zoom resolve. Ler os lábios é bônus.
- **Cofre do quarto lilás** abre com a mesma palavra e guarda o **Terço da Moça** (gancho do Ato 2).
- **Enigma 6 (fora do jogo): o Arquivo do Km 9** (site secreto, E5). Com a senha, três pastas para o grupo dividir:
  - **Memorando vazado** (PDF com tarja que ainda é texto): diz que *"a repetidora é o sino"*.
  - **Foto do Açougueiro** (imagem com texto nas sombras): diz *"telegrafo"*.
  - **Áudio da ligação** (ao contrário): diz *"a palavra do dono"*.
  As três palavras entram na página **Resposta**. O site devolve a **palavra de retorno**.
- **Exposição:** vídeo e áudio entram na cabeça. A Exposição sobe um pouco ao ver o vídeo inteiro. Quem pula a parte do "quadro único" (o vulto pendurado) não perde nada, mas perde clima.
- **Lucidez:** ficar sozinho no corredor da Pousada tira devagar.
- **Plano B:** Dica em livro, liberada pelo admin.

## Cena 4 — A Casa da Repetidora (o barulho tem origem)
Ficha: estrutura 4. A localização vem do Arquivo (uma pista na pasta do memorando) e do telégrafo.
- Torre de rádio, casa de máquinas, bunker. O sino de metal sem badalo balança sozinho no topo. É daqui que vem o barulho.
- **Enigma 7 (volta ao jogo): o console.** O jogador digita a **palavra de retorno** do Arquivo. O sino **cala**. O telégrafo imprime, em Morse, a localização da Fazenda.
  Ao silenciar, o barulho de metal **para em todo o mundo** por um tempo (Prelúdio do Diabo).
- **Bônus (E6): a Fita da Repetidora.** O barulho, visto em espectrograma, desenha uma porta com quatro fechaduras. Não dá avanço; dá sinal para o Ato 5.
- **Perigos:** altura, bunker no escuro. Sem Magarefe aqui.

## Cena 5 — A Fazenda do Sino Mudo e a Mesa do Pacto (cooperativo)
Ficha: estrutura 5. Só abre depois do console da Repetidora (a capela está de portas fechadas até então).
- **Casarão:** o poema do Diário p. 4 (escritório), o retrato virado, o quarto fechado. A ordem das velas fica aqui.
- **Capela da Mesa:** no meio, uma **mesa posta** com um lugar para cada jogador do grupo mais um. Prato com **nome**, **vela** de altura diferente, **cálice** vazio. O lugar a mais, sujo de sangue seco.
- **Como se resolve (dentro do jogo):** cada jogador se senta no lugar com seu nome e acende a vela, **sem falar em texto** (o chat de texto bloqueia; a voz continua: jogo de sinais).
  Poema do Diário p. 4:
  > *Quem gastou mais, acende primeiro. Quem chegou agora, por último.*
  Da vela **mais baixa para a mais alta**. Fora de ordem apaga todas e tira Lucidez do grupo. A **Faca de Osso** corta a vela difícil.
- **Revelação do elemento (decisão de 08/11):**
  - **{escolhido_sangue}:** o cálice se enche de líquido escuro e quente, cheiro de ferro, a vela dele é a única que não treme. Texto único: *"Esse lugar é seu desde antes. Beba ou não. Já está na sua boca."*
  - **Presságios dos outros três** (sem nome, cada um só percebe o seu):
    - **Conhecimento:** a chama inclina na direção de um livro que não está ali; o prato mostra uma palavra ao contrário por um instante.
    - **Morte:** a vela fica fria (cera suja de pó), o cálice mostra a cara sem rosto, um relógio para.
    - **Energia:** a vela estala, o cabelo arrepia, zumbido no metal do cálice, o rádio pega estática.
  - **{ilegivel}:** o cálice fica seco, a vela acende sem tremer, nada acontece. *"Não tem nome aqui para você."* Ele vê **uma vez** a silhueta do lugar a mais virar a cabeça e hesitar.
  - O jogador de presságio não sabe o que viu. O Ato do elemento dele explica depois.
  - O admin vê no aviso "Mesa resolvida" quais elementos acordaram, sem mostrar ao grupo.
- **Altar do Aceite:** o altar gira e mostra um cálice de pedra com *"Chame."* Quando todos acendem, o jogo avisa o admin: **Evento do Diabo pronto**. Até soltar, o altar fica quieto:
  *"Ele já ouviu. Ele vem quando quiser."*
- **Exposição:** acender sobe a de todos. **Lucidez:** cai um pouco no fim; sair da capela devolve.
- **Tech:** o **Gravador de Evento** na mesa captura o evento do Diabo.

## Aparições do Ato 1 (escalonadas)
Poucas partículas e mobs simples (ver `lore/propostas-elementos.md`). Cada aparição só é *permitida* quando o respectivo lugar está aberto.

| # | Nome | Onde / quando | O que é | Quem vê |
|---|---|---|---|---|
| 1 | **O Cheiro** | Da Clareira em diante | Cheiro de ferro (som de gota, partícula vermelha rala, neblina avermelhada leve) | Todos |
| 2 | **O Pendurado** | Frigorífico (erro nos ganchos), corredor da Pousada (Exposição alta) | Silhueta pendurada vista pelo canto do olho; some se se olha direto | **Só o jogador com maior Exposição** (efeito de cliente) |
| 3 | **O Magarefe** | Frigorífico, entrando pela frente ou subsolo | Criatura de Sangue: alto, ombros largos, avental, cutelo, cabeça baixa. Anda devagar, **não morre** no Ato 1. Perde o interesse quando o grupo sai com a Faca de Osso | Todos |
| 4 | **A Convidada** | Capela, quando todas as velas apagam por erro | Figura sentada no lugar a mais por 3 segundos. Anuncia o Diabo | Todos na capela |

- Para `{ilegivel}`: o Pendurado nunca aparece; o Magarefe o confunde; a Convidada vira a cabeça para ele e não o reconhece.
- Versão tech: a **Câmara de Contenção** prende o Magarefe e coleta Resíduo (opcional).

## Evento ao vivo — O Diabo (admin solta)
Nossa versão: **o Diabo não rouba, não obriga e não grita. Ele aceita.** Fala calmo, usa os nomes de quem está ali e só oferece o que a pessoa já queria.
Visual: silhueta alta de terno largo e sujo, chapéu de abas moles, sem rosto definido; só se vê o vermelho dos olhos de quem está perto.
Base: folclore popular brasileiro do pacto na encruzilhada (domínio público). Sem relação com símbolo, texto ou desenho de outra obra.
O evento acontece **na Capela da Fazenda do Sino Mudo**.

**Fases (`/evento diabo <fase>`, nome provisório):**
1. **Prelúdio.** O servidor sente: céu avermelha, sinos no horizonte, o barulho de metal já parou (Cena 4). Mensagem global: *Alguém acendeu uma vela do outro lado do mapa.*
2. **Chegada.** Na capela, a silhueta senta no lugar vazio. Longe, uma sombra enorme no horizonte, na direção da Fazenda, por 10 segundos. Som de cadeira arrastando.
3. **Conversa.** O admin fala pelo voice changer e pelo chat com as falas prontas.
4. **Oferta e resolução** para `{escolhido_sangue}` (ou `{anfitriao}`):
   - **Aceita:** Exposição dele sobe um degrau; nasce o primeiro sinal da **Marca de Sangue** (só para quem tem Sangue; sem escolhido, fica pendente). Ganha o **Sino do Pacto**. O Diabo some e o altar vira cinzas. Preço: os amigos dele ficam com um olho no pescoço dele (ele agora é visto sempre).
   - **Recusa:** o Diabo sorri, diz que o convite fica e volta no Ato 3. Exposição do grupo sobe pouco. Ninguém é punido; o pacto fica pendente.

**Falas prontas:**
- *{grupo}. Sentem. A mesa é de vocês.*
- *{anfitriao}. Seu nome estava aqui antes de você chegar. Não é magia. É memória.*
- *Eu não pego nada. Vocês me dão. Eu só guardo.*
- *{parceiro} está com fome. Você sabe. Eu também sei.*
- *Todo pacto começa assim: com alguém dizendo que não quer.*
- *Vocês vieram por um barulho. Eu sou o intervalo entre dois barulhos.*
- *Vão. Voltam depois. Vocês sempre voltam. A estrada é redonda.*
- Só para `{ilegivel}`: *{ilegivel}. Eu não consigo ler você. Isso me incomoda. Fique.*
- Escondida (Gravador + Analisador): *"Ele não cobra o que você dá. Cobra o que você guarda."*

**Ao final:** o grupo recebe a **Pista do Ato 2**: carta com selo de cera queimada na borda, endereçada a `{grupo}`, marcada com o símbolo de **Conhecimento** (um olho de tinta, a definir com o Modelador). Aponta para uma biblioteca. A carta fica na **cripta da capela**, que se abre depois do evento.

## Ganchos para tech e magia (tudo opcional)
- **Mekanism/Create (nossas máquinas):** Analisador de Sinal (lê fita, tinta e áudio em camadas), Lâmpada de Luz Negra, Medidor de Resíduo, Câmara de Contenção, Gravador de Evento.
  Atalhos de história; nada trava sem eles. Matéria: **Resíduo Rubro**, que atrai Incursões na base (desligáveis, avisadas antes pelo Medidor).
- **Occultism:** Selo de Giz protege a porta lateral do Frigorífico do Magarefe.
- **Iron's Spells:** a Faca de Osso pode ser "lida" por uma magia de rastro que mostra, em fumaça, o caminho do Frigorífico até a Fazenda.
- **Vampirism:** a Moça do Terço (NPC, Ato 2) é ligada a esse mod.
- Cada gancho é caminho **alternativo**, nunca obrigatório.

## O que o Ato 1 entrega ao jogador
- A **Vila Calada** (destino), o **Livro do Sineiro** e a primeira pista de como voltar à realidade: o sino precisa tocar ao contrário; quatro portas pequenas e uma parede.
- Cinco lugares explorados, quatro peças de história (Fita, Mapa, Livro-Caixa, Faca de Osso) e, se aceitou, o Sino do Pacto.
- A experiência de resolver enigmas **fora do jogo** (site, vídeo, áudio).
- Exposição subindo de forma legível; Lucidez sendo testada.
- A Marca de Sangue **pode** nascer aqui.
- O Magarefe e o Diabo apresentados. A Pista do Ato 2.

---

# ATO 2 — O OUTRO LADO (esqueleto)
- O grupo recebe a carta de Conhecimento e acha a **Biblioteca Submersa** (nome provisório), uma **estrutura grande** numa **dimensão nova**.
  Mesma direção: andares, salas, pistas posicionadas, mais enigmas fora (um segundo site? um documento "vazado" de verdade?).
- O presságio da Mesa ganha explicação. A porta da Biblioteca só abre inteira com `{escolhido_conhecimento}`; `{escolhido_morte}` e `{escolhido_energia}` recebem mais um presságio curto.
- `{ilegivel}` no Ato 2: a porta não responde a ele, mas é o único que vê, de lado, o livro que "falta" na estante.
- Descoberta: o paranormal é um **lugar** e tem regras. O barulho era uma porta que se fechou.
- Pendente: nome do mundo paranormal (usar só "o paranormal" até a Lore propor um nome nosso).

# ATO 3 — AS MESAS (esqueleto)
- Uma entidade por elemento, em ordem decidida pelo Nikolas: **Diabo (Sangue)**, e as de **Conhecimento, Morte, Energia**.
- Cada uma tem seu "Ato 1" reduzido: **uma estrutura grande**, 3 a 4 enigmas (um deles fora do jogo), aparições e um evento ao vivo.
- Cada entidade fala com o seu escolhido; sem escolhido, o admin decide o plano B.
- `{ilegivel}`: nenhuma entidade tem o que dizer a ele; em cada Mesa, um momento em que vê o ponto cego daquele elemento.
- Cada entidade devolve uma **relíquia** e uma **espada**. A pergunta do Diabo volta aqui.

# ATO 4 — O FUNDO (esqueleto)
- O Medo. Pendente de decisão: só força de fundo ou também relíquia/marca rara de fim de jogo.

# ATO 5 — O CAMINHO DE VOLTA (esqueleto)
- Fio do Livro do Sineiro: "o sino tocando ao contrário" (págs. 11 e 12 do livro) é a mesma coisa que o desenho da porta com quatro fechaduras. As quatro portas pequenas respondem uma por Ato; a quinta, a parede, é o Medo.
- O barulho de metal volta como mapa: o desenho da porta com quatro fechaduras (E6, bônus do Ato 1) vira a chave. As 4 relíquias abrem a porta.
- Decisão em aberto: o grupo volta, fica ou escolhe.

---

## Perguntas para o Nikolas
1. A nova direção ficou como você queria: cinco lugares grandes e três tipos de enigma fora do jogo? Algo a mais ou a menos?
2. Tamanho e quantidade das estruturas (ver perguntas em `fichas-estruturas.md`): reduzimos? Quem constrói?
3. Site, vídeos e áudio (ver perguntas em `enigmas-fora.md`): quem produz, onde hospeda?
4. Aceitar ou recusar o pacto do Diabo: gosta de **sem castigo** nas duas saídas?
5. O Magarefe serve de nome? (pesquisa superficial, sem ligação com outra obra; alternativas: "O Açougueiro", "Carniceiro".) Checagem extra por quem conhece a obra.
6. O Diabo como nome: você já decidiu manter. A versão aqui é nossa. Confirme.
7. Chat de texto bloqueado na capela: aceita?
8. O Ilegível: "ver o que fica entre os elementos" é o bônus certo? Alternativas: (a) ver só o que o **Medo** esconde (Ato 4); (b) ouvir "de perto" o barulho de metal (Ato 5).
9. As máquinas tech nomeadas: quer que o Integrador veja a viabilidade antes de aprovar?
10. Vila Calada: aprova o nome (provisório), o texto do livro e a ordem (vila comum, Vila Calada, depois os cinco lugares)? O Sineiro fechou "a que sangrava, e mal": gostou como justificativa do Diabo ainda solto?
11. O Morse FERRO da placa da vila comum e a Fita da Clareira repetem a mesma palavra. Quer manter (reforço) ou mudar uma delas?
