# Dimensões (PROPOSTA)

> Proposta da mesa Lore. Nada aqui é cânone até o Nikolas aprovar. Base: `lore/canon.md`, `lore/elementos-no-jogo.md`.
> Decisão do Nikolas (07/11/2026): **uma dimensão para cada elemento**. O Medo é o princípio dos 4 e **não tem dimensão própria**.
> Nomes são genéricos em português. IDs técnicos só entram em `docs/ids.md` depois da aprovação.

## Visão geral
| Lugar | Elemento | Nome (proposta) | Ideia em uma frase |
|---|---|---|---|
| Chegada | nenhum | **A Clareira** | Floresta de névoa onde a travessia termina. Só passagem e ponto de partida. |
| 1 | Sangue | **O Ventre** | Um corpo gigante que já foi paisagem: carne, veias, poças de Seiva Rubra. |
| 2 | Conhecimento | **A Biblioteca Muda** | Corredores e salas de páginas sem fim, tinta dourada escrevendo sozinha. |
| 3 | Morte | **O Ermo de Cinzas** | Deserto frio de cinza e osso, onde o tempo anda para trás ou para. |
| 4 | Energia | **A Rede** | Céu de estática, ilhas de cabo e cristal, tudo zumbindo e piscando. |

**O Medo** aparece nas quatro como **clima**, nunca como bloco ou mob: a névoa se afasta de quem anda,
o silêncio vem de repente, alguma coisa "escuta". É a pista de que os quatro têm o mesmo começo.

---

## A Clareira (lugar da travessia)
Mantida só como **ponto de chegada**, porque a premissa pede (agentes entram numa floresta e atravessam).
Não é dimensão de elemento e não tem relíquia.
- **Clima:** frio úmido, névoa baixa que não mexe com o vento. Sem chuva.
- **Céu:** cinza-claro, sem sol nem lua visíveis. Um "sino" soa a cada tantos minutos.
- **Blocos:** terra, musgo, troncos **sem sombra**. Bloco `Névoa Muda` só aqui (ver elementos).
- **Perigos:** poucos. Criaturas fracas à noite, nada dos elementos ainda.
- **Coleta:** madeira, pedra, comida. Pistas: giz antigo, cadernos, crachás.
- **Portais:** daqui saem as 4 passagens, uma por elemento (ver "Como se chega"). Ficam em pontos distantes da mata, cada um com a cara do elemento.

---

## 1. O Ventre (Sangue)
- **Clima:** quente e úmido, ar pesado, cheiro de ferrugem. Pulsa: o chão "respira" a cada poucos segundos. Chuva rara de gotas vermelhas.
- **Céu:** vermelho-escuro `#2A0000` com um "sol" baixo, grande, que bate como coração.
- **Blocos:** Carne Viva (pulsa), Espinheiro de Osso, Poço de Seiva (fluido lento), terra encharcada, osso longo como pilar.
- **Perigos:** poças que prendem os pés, espinhos que sangram, chão que "contrai" e empurra. Quem está com a Marca de Sangue perde menos vida ali; os outros sangram mais devagar do que cicatrizam.
- **Criaturas (nossas):** *Rastejador de Veias* (rápido, em bando), *Pelanca* (lento, resistente), *Coração Aberto* (chefe de área: um núcleo pulsante que invoca).
- **Coleta:** Gota Grossa, Dente Rubro, Seiva Rubra (balde), Espinho de Osso.
- **Como se chega:** **ritual** de giz com uma gota do próprio sangue (custa vida, não item). Abre uma passagem de carne na Clareira.
- **Travessia (visual):** tela fecha em vermelho pelas bordas, som de batida de coração que acelera, e a imagem "pisca" como pálpebra. Acorda-se com uma respiração funda.
- **Ligação com a relíquia (lore):** a relíquia do Sangue fica no núcleo do Ventre, dentro da Hospedeira. É vista no santuário e no evento, **não é pega por ninguém** (`lore/reliquias-e-espadas.md`).
- **Ligação com a espada:** a **Espada do Sangue** cai do *Coração Aberto* (sem craft). Quanto mais ferida a pessoa, mais forte a lâmina, e ela cobra uma gota a cada golpe forte.

## 2. A Biblioteca Muda (Conhecimento)
- **Clima:** ar seco, parado, poeira dourada em suspensão. Sem vento. Sem chuva, às vezes "chove" papel queimado.
- **Céu:** teto de estantes distantes, luz âmbar `#D4A92A` vinda de lugar nenhum. Sem noite.
- **Blocos:** Página Fixa, Estante Muda (loot), pergaminho, madeira escura, piso de letras gravadas, vidro dourado.
- **Perigos:** salas que **mudam de lugar** quando ninguém olha, textos que "perguntam" e punem a resposta errada (puzzle), tinta que cega por segundos.
- **Criaturas (nossas):** *Copista* (silencioso, copia o jogador por perto), *Verbete* (nuvem de letras que fere), *Bibliotecário Cego* (chefe: só ataca quem faz barulho).
- **Coleta:** Tinta de Ouvir, Fio de Pergaminho, Grafia Viva, páginas de lore.
- **Como se chega:** **ritual de leitura**: montar uma frase com páginas achadas na Clareira (puzzle de investigação) e ler em voz alta (comando de cena). Abre uma porta de livro.
- **Travessia (visual):** letras douradas cobrem a tela e se reorganizam, som de papel e sussurro, texto some e a cena "se escreve" ao redor.
- **Ligação com a relíquia (lore):** a relíquia do Conhecimento (o livro) fica aberta no último salão, com o Leitor. Só se vê; não é pega por ninguém.
- **Ligação com a espada:** a **Espada do Conhecimento** surge numa mesa de copista depois do enigma do último salão (sem craft). Dá bônus a quem leu mais páginas (progressão de investigação, sem número na tela).

## 3. O Ermo de Cinzas (Morte)
- **Clima:** frio seco, cinza caindo devagar o tempo todo. Silêncio quase total. Sem chuva; neblina de pó.
- **Céu:** preto `#0B0B0D` com uma lua grande de osso `#E8E0D0` parada. Estrelas que se apagam uma a uma.
- **Blocos:** Cinza Assentada (cai se pisar), Osso Cristalino, Breu (fluido preto), pedra cinza, ruínas de relógios.
- **Perigos:** chão que afunda, Breu que "envelhece" itens (durabilidade cai), zonas onde o tempo trava (movimento lento). Sem fome nem sede aqui, só cansaço.
- **Criaturas (nossas):** *Cinzento* (anda devagar, atravessa fumaça), *Eco Atrasado* (cópia do jogador que chega segundos depois), *Coveiro de Séculos* (chefe: cava e some).
- **Coleta:** Pó de Séculos, Relógio Parado, Osso Cristalino, Breu Velho.
- **Como se chega:** **portal ritual** num lugar onde algo morreu de verdade (cova, ruína). Acende-se uma vela de osso e espera-se um minuto sem se mexer.
- **Travessia (visual):** cor some da tela até ficar cinza, sons tocam ao contrário, partículas de cinza sobem e depois caem. A sombra do jogador chega um pouco depois.
- **Ligação com a relíquia (lore):** a relíquia da Morte (a ampulheta) está num mausoléu sobre o único relógio que ainda corre, no centro da Hora Parada. Só se vê; não é pega por ninguém.
- **Ligação com a espada:** a **Espada da Morte** é entregue por um túmulo antigo, num ritual de vela de osso (sem craft). Quanto mais perto de um inimigo morrer, mais fundo ela corta; também "apaga" efeitos do alvo.

## 4. A Rede (Energia)
- **Clima:** ar carregado, cabelo arrepia, estática no ar. Tempestades elétricas curtas e frequentes. Sem chuva de água.
- **Céu:** roxo `#6A00FF` com faixas ciano `#00E5FF` e magenta `#FF2BD6`, tipo "chuvisco de TV". Raios sem trovão atrasado.
- **Blocos:** Cabo Vivo (conduz faíscas), Cristal de Estática, brasa fluida (efêmera), metal polido, ilhas flutuantes ligadas por cabos.
- **Perigos:** descargas em metal (cuidado com armadura de ferro), blocos que "falham" e somem por segundos, saltos entre ilhas com vão de vazio.
- **Criaturas (nossas):** *Faísca* (rápida, se divide), *Fantasma de Sinal* (imagem dupla, difícil de acertar), *Sobrecarga* (chefe: núcleo que liga e desliga a área).
- **Coleta:** Fio de Cobre Vivo, Núcleo Trêmulo, Cristal de Estática, Brasa Fluida.
- **Como se chega:** **comando de cena** + **circuito**: ligar um circuito feito de blocos do mundo (compat com Create/Mekanism, opcional) numa clareira e fechar com um raio. Sem os mods, funciona com bloco do nosso mod.
- **Travessia (visual):** tela com chuvisco e linhas horizontais, som de estática subindo, imagem dupla por um instante, e "carrega" como tela de vídeo.
- **Ligação com a relíquia (lore):** a relíquia da Energia (a antena) fica no coração da Rede, ligada a todos os cabos, na cabeça do Sinal. Só se vê; não é pega por ninguém.
- **Ligação com a espada:** a **Espada da Energia** cai num ponto de raio na tempestade elétrica da Rede (sem craft). Carrega com o tempo e descarrega em arco no golpe forte.

---

## Regras para as quatro
- **Ciclo de vantagens** (ver elementos) vale dentro de cada dimensão: criaturas de um elemento sofrem mais do elemento que o vence. Nenhum número na tela.
- **Volta:** sempre existe um jeito de voltar à Clareira (passagem de saída perto de onde se chegou). Não há dimensão sem saída.
- **Ordem:** não há ordem obrigatória. A dificuldade pode escalar pela espada ou pelas relíquias menores exigidas (decisão do Nikolas e da mesa Integrador).
- **Medo:** quem juntar as pistas das 4 descobre que o começo de todas é o mesmo. Isso é o mistério final, sem dimensão nem chefe do Medo.
- **Técnico (para a mesa Dev):** cada dimensão = uma `Dimension` própria do mod; travessia via comando/ritual com efeito visual de cliente. Nada disso precisa de mod externo; Create/Mekanism só enfeitam a Rede (regra 4).

## Perguntas para o Nikolas
1. Os nomes (Ventre, Biblioteca Muda, Ermo de Cinzas, Rede) servem? Quer outros?
2. A Clareira fica como lugar fixo de chegada, ou a chegada já é dentro de uma das 4?
3. As quatro ficam liberadas desde o início ou abrem por progressão?
4. ~~O chefe guarda a relíquia?~~ Resolvido: a relíquia principal é lore (fica na entidade), ninguém a pega.
