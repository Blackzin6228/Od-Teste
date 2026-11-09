# Como se desenha uma aventura de investigação e horror (guia para o time)

> Estudo da mesa Lore, 09/11/2026. Serve de apoio ao `lore/roteiro.md`. Nada aqui é decisão: as mudanças sugeridas
> estão no fim de `docs/estudo/design-e-desenvolvimento.md`, para o Nikolas aprovar.
> Base: técnicas públicas de mestres de RPG de mesa (Call of Cthulhu, GUMSHOE, Blades in the Dark e blogs de design).

## 1. A regra das três pistas
**A ideia:** para cada conclusão que o grupo precisa tirar, existem **pelo menos três** pistas independentes que levam a ela.
Se o grupo perde uma, acha outra. Se perde duas, a terceira segura a história.

**Por que existe:** em investigação, o pior erro de projeto é a história parar porque ninguém abriu a gaveta certa.
Num mod, é pior ainda: o grupo pode estar em 4 pessoas, 2 online, e ninguém lembra de ler a placa.

**Versão inversa (Justin Alexander):** a regra vale também para as *pistas que levam a cenas*. Se o grupo tem **três pistas
quaisquer**, deve chegar a **pelo menos um** lugar novo. Assim não existe beco sem saída: cada cena entrega pistas para
duas ou três outras.

**Como aplicar ao Ato 1** (hoje a cadeia é linear, cada enigma entrega *uma* chave para o próximo):

| Conclusão que o grupo precisa tirar | Pistas hoje | Sugestão (3 pistas) |
|---|---|---|
| "Existe um lugar de verdade onde o mapa aponta" | 1 (a frase cifrada) | frase cifrada + **aldeão da vila repete o rumo** nas trocas + **pegadas/gotas** em linha na neve ou na grama |
| "A palavra do baú é FERRO" | 1 (Morse do sino) | Morse + **placa velha** com "FERRO" riscado em outra casa da vila + livro do ferreiro com a letra inicial marcada |
| "Quando o sino calar, vá à capela" | 1 (verso do Livro-Caixa) | verso do livro + sino que para + aldeões que **viram o rosto** na direção da capela |

Regra de bolso: **toda cena de enigma termina com o jogador segurando, no mínimo, duas coisas que apontam para o próximo lugar.**

## 2. Cenários feitos por nós ("node-based")
**A ideia:** em vez de uma fila (cena 1, depois 2, depois 3), a aventura é um **mapa de nós**: lugares, pessoas, eventos e objetos.
Cada nó guarda pistas que apontam para outros nós. O grupo escolhe a ordem.

**Tipos de nó úteis para nós:**
- **Lugar:** vila marcada, matadouro, capela.
- **Personagem:** aldeão que não vira o rosto, o Compadre, a Moça do Terço.
- **Objeto:** livro, mapa rasgado, faca.
- **Evento:** o sino calar, a vela apagar, a noite de neblina.
- **Atividade:** ritual de giz, trocar com aldeão.

**Exemplo (Ato 1 como mapa, em vez de fila):**
```
 [Sino da Vila] ---> [Mapa Rasgado] ---> [Matadouro] ---> [Livro-Caixa] ---> [Capela]
       \                  ^                   ^                 ^
        \--> [Aldeões] ---+-------------------+                 |
         \--> [Cama manchada] ---> [Cheiro/neblina] ------------+
```
O sino continua sendo a porta de entrada, mas o grupo pode chegar ao Matadouro **por dois caminhos** (mapa ou aldeões) e à Capela
**por dois caminhos** (livro ou neblina que sempre empurra para o mesmo ponto).

**Regra de ouro dos nós:** a *informação* é o que flui, não a *chave*. Um nó pode estar trancado (baú, placa), mas
a conclusão que ele entrega precisa existir em outro nó também.

**Quando manter fila:** gargalos de propósito (o Diabo só chega depois da Mesa). Fila é boa para **clímax**, nó é bom para **investigação**.

## 3. Revelações
- **Pista nunca é "dica": é fato com cheiro de mistério.** Boa pista responde uma pergunta e abre duas.
- **Revele de fora para dentro:** primeiro o *efeito* (sino toca), depois a *causa próxima* (cama manchada), depois o *porquê* (Livro-Caixa), por fim o *quem* (o Diabo).
- **Mostre antes de explicar.** O jogador deve ter visto o sintoma duas vezes antes de ler a explicação.
- **Uma revelação por sessão grande.** Revelação demais seguida vira recitar enciclopédia.
- **Revelação com preço:** saber algo muda o jogo (Exposição sobe, aldeões reagem). Isso já existe no roteiro; manter.
- **Pistas falsas só quando o jogador *sabe* que podem existir.** A Lucidez baixa já faz isso, e é justa porque o jogo ensina no enigma 2.
- **A grande revelação do Ato 1 ("vocês já passaram pela porta") não pode ser dita.** O grupo precisa montar, e o jogo só confirma
  em silêncio (as cabeças na mesa). Isso já é a maior força do roteiro.

## 4. Ritmo: calmaria e tensão
Horror bom respira. Sem calmaria, a tensão vira ruído.

**Ciclo sugerido (cada "onda" dura 20 a 40 minutos de jogo):**
1. **Calmaria** (explorar, conversar, comer, construir). Detalhe estranho pequeno.
2. **Sinal** (som, cheiro, aldeão olhando). Sem perigo.
3. **Investigação** (enigma, pista).
4. **Tensão** (escuro, aparição, perseguição curta).
5. **Alívio com consequência** (acertou, mas algo mudou).

**Regras:**
- Depois de um pico (Magarefe), uma calmaria **mais longa** que o normal.
- A **tensão sobe em degraus**, não em rampa: cada pico é um pouco maior que o anterior, nunca dois seguidos.
- **O pior momento tem que ter sido anunciado.** O Diabo tem prelúdio (sinos juntos, céu avermelhado). Surpresa total é só para sustos pequenos.
- **Quem controla o ritmo é o admin.** Isso já está no roteiro. Falta dar a ele uma **régua**: "quanto tempo desde o último pico?"
  (ver design-e-desenvolvimento).
- Em servidor, calmaria é **sagrada**: jogador que só quer construir não pode ser interrompido sem aviso.

## 5. Relógios de ameaça
**Do Blades in the Dark:** um relógio é um círculo dividido em 4, 6 ou 8 fatias. Cada vez que o grupo demora ou falha, uma fatia enche.
Quando enche, **algo acontece**. O relógio é **visível** para criar tensão, mesmo que o nome seja vago.

**Dois tipos para nós:**
- **Relógio de ameaça:** o que cresce enquanto o grupo hesita. Ex.: "O Sino Cala" (6 fatias): cada erro de enigma, cada noite sem resolver, cada vela apagada enche uma. Cheio, o Diabo aparece **sem convite**.
- **Relógio de progresso:** o que enche com o acerto. Ex.: "A Mesa" (4 fatias, uma por vela).

**Regras de uso:**
- **Nada de número na tela** (regra do roteiro). Mostre no mundo: **velas** num suporte (fatias), badaladas do sino, aldeões virando o rosto um a um.
- **Ameaça precisa ter consequência clara e anunciada.** O grupo deve saber *o que* acontece quando enche, nem que seja em forma de pergunta.
- **Preço, não castigo:** relógio cheio não é "perdeu", é "o jogo mudou" (o Diabo chega antes, o Magarefe desperta, a vila fica vazia).
- **Relógio pode ser desligado pelo admin** (servidor grande não pode ter timer para cada grupo).

## 6. Ganchos por personagem
No Minecraft **não existe ficha**, então o gancho nasce do que o jogador *faz* e *tem*:

| O que o jogador faz | Gancho que o roteiro pode dar |
|---|---|
| Joga com tech (Mekanism/Create) | Analisador, camadas extras, Resíduo |
| Joga com magia (Iron's, Occultism) | Círculo de giz, rastro mágico |
| Constrói muito | A capela "copia" uma construção dele; aldeões olham para a base dele |
| Explora / viaja | Mapa Rasgado aponta para onde ele já esteve |
| Luta | O Magarefe só perde interesse por outro caminho (não dá para só bater) |
| Gosta de lore / lê tudo | Texto vermelho, camadas escondidas |
| Fala no voice chat | Papel de "voz do grupo" no enigma 5 |

Dois pontos fortes que o roteiro já tem: `{marcado}` (maior Exposição recebe a oferta) e `{parceiro}` (sorteio).
**Sugestão:** um gancho **pessoal** pequeno por jogador (uma linha no Dossiê, que só ele lê), tirado de algo que ele fez no mundo
(a primeira morte, a base, a coisa que mais coletou). Dá a cada um um segredo para dividir ou esconder.

## 7. Cooperação
- **Papéis, não classes.** Em cada enigma, pelo menos **uma tarefa** que só um jogador consegue fazer *naquele momento*: quem ouve o sino, quem
  anota, quem digita na placa, quem aperta os botões.
- **Informação dividida.** Cada jogador recebe/vê um pedaço diferente (Exposição alta, Lucidez baixa, aparição só para um). Falar é o jogo.
- **Ninguém espera sozinho.** Se o enigma exige 3 pessoas, as outras 2 precisam ter algo a fazer enquanto isso (vigiar, anotar, segurar a luz).
- **Grupo de 1 a 6:** todo enigma tem versão solo (o mesmo mecanismo, mas o jogo ajuda mais) e versão cheia. Servidor grande: o enigma 5 não pode exigir "N cadeiras" fixas, o número vem do grupo que entrou na capela.
- **Evitar o "jogador alfa"** (um resolve tudo e os outros assistem): dividir a pista em partes que só aparecem para pessoas diferentes.

## 8. Quando o grupo trava
O grupo **vai** travar. Plano em camadas, do mais sutil ao mais direto:
1. **Repita a pista de outro jeito** (a terceira pista da regra das três).
2. **O mundo empurra:** a neblina, o sino ou os aldeões apontam o caminho. Nada de seta.
3. **Reduza o custo:** Exposição média torna o Morse mais lento (já existe no roteiro).
4. **O Dossiê relembra** (nunca dá a resposta, só o que já foi visto).
5. **Alívio ao admin:** aviso "grupo parado há X tempo no enigma Y" (a ferramenta de admin vira o "Mestre" que sopra).
6. **Última camada: a pergunta.** O admin ou um NPC pergunta, em voz baixa, algo que reorienta ("Por que o sino calou?").
Nunca: dar a resposta pronta no chat, ou teletransportar o grupo para a solução.

**Regras de projeto:** nenhuma chave de progresso depende de **um** teste de sorte, **nem de um item raro**, **nem de tech**. Tech e magia dão atalho.

## 9. Sessão zero
É a conversa antes do jogo, para alinhar expectativas. Num servidor, vira um **texto de entrada** e um **canal de avisos**.
- **Linhas e véus (lines and veils):** *linha* é o que **não aparece** no jogo (ex.: violência contra crianças); *véu* é o que acontece **fora de cena** (o aldeão sumiu, não vimos). Perguntar uma vez, por escrito, e anônimo se possível.
- **Carta X / pausar:** qualquer jogador pode pedir para suavizar ou pular. No mod: um comando de "pausar clima" para ele (some o som e a aparição só para ele por um tempo).
- **Expectativa de tom:** "É um jogo de medo lento, sem sustos de gritar, em grupo. Tem voz, escuro e pouco combate."
- **Combinar a regra do voice chat** (a Lucidez usa o grupo como âncora) e o **tamanho do grupo**.
- **Como entra o novo jogador no meio da campanha?** Um resumo de 5 linhas no Dossiê dele.
- **Para o Ato 1:** lembrar o grupo de que tem **chat de texto bloqueado** na capela, e porquê, **antes** de chegar lá.

## 10. Campanhas em temporadas
- **Temporada = arco fechado com final.** O Ato 1 + Ato 2 podem ser uma temporada; os cinco atos, uma campanha.
- **Cada temporada termina com um evento** que o servidor inteiro vê (o Diabo é o modelo). Isso vira "episódio final".
- **Ganchos para a próxima temporada** deixados abertos de propósito (o Compadre, a Moça do Terço, a pergunta sem resposta do Diabo).
- **O mundo guarda a consequência:** o que o grupo fez na temporada 1 fica no mapa (vila vazia, altar de cinzas) e aparece na 2.
- **Recapitulação curta no início:** o Dossiê escreve 3 frases do que o grupo fez, em tom de lenda.
- **Quem chegou depois** deve poder acompanhar sem ter jogado antes (resumo no Dossiê).
- **Rodízio de "estrelas":** cada temporada pode dar o holofote a outro elemento; o jogador que não gostou de uma volta na próxima.

## 11. Checklist para revisar qualquer enigma novo
1. Há **três** pistas para a conclusão principal?
2. Existe versão **sem aparelho** e versão **solo**?
3. O grupo **pode travar sem saída**? Qual é a válvula (item 8)?
4. Que **papel** cada jogador tem?
5. Que **preço** (Exposição ou Lucidez) o jogador paga e o que ganha?
6. Depois dele, vem **calmaria**?
7. O que o admin vê quando o grupo chega aqui?

## Fontes
- [The Alexandrian, "Three Clue Rule"](https://thealexandrian.net/?p=7985)
- [The Alexandrian, "Node-Based Scenario Design" (edição de colecionador)](https://thealexandrian.net/wordpress/8122/roleplaying-games/node-based-scenario-design-collectors-edition)
- [The Alexandrian, "Running Mysteries: The Two Types of Leads"](https://thealexandrian.net/wordpress/50095/roleplaying-games/running-mysteries-the-two-types-of-leads)
- [Fórum Blades in the Dark: como mostrar relógios de progresso](https://bitd.gplusarchive.online/?p=176147)
- [Roll20, "Introduction to Lines and Veils"](https://bloghub.roll20.net/posts/guest-blog-an-introduction-to-lines-and-veils)
- [2d10plus, "Session Zero, Safety Tools, Etc"](https://2d10plus.substack.com/p/session-zero-safety-tools-etc)
- [Dicas de mestre de Call of Cthulhu (fórum GiantITP)](https://forums.giantitp.com/archive/index.php/t-93654.html)
- Chaosium, *Call of Cthulhu Keeper Tips* (consulta recomendada; não lido na íntegra nesta pesquisa).
- Relógios de ameaça: John Harper, *Blades in the Dark* (livro; conferir o texto exato antes de citar).
- Observação: a busca por Call of Cthulhu retornou muitas páginas de baixa qualidade; só as fontes acima foram aproveitadas.
