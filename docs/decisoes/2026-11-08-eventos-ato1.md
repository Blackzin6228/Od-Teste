# Eventos da história e Ato 1, parte A (2026-11-08)
**Status:** proposta técnica da mesa Dev, no PR do Ato 1 parte A. **Esperando o ok do Nikolas** (nada aqui é difícil
de desfazer: tudo sai com `/odteste evento desfazer` e os textos e números mudam num lugar só).

**Decisão (proposta):**
1. **Grupo é do admin.** O admin cria o grupo e põe os jogadores (`/odteste grupo criar <nome> <jogadores>`).
   O progresso da história é do **grupo**, salvo no mundo (`odteste_historia`). Quem entra depois chega com o que o
   grupo já descobriu. Um jogador fica em um grupo só. (Não usa o FTB Teams para não depender dele.)
2. **Uma vila por grupo.** O Primeiro Sinal marca o sino de vila mais perto do grupo (ou do ponto/âncora do admin),
   só em chunk carregado e sem claim. Dois grupos nunca pegam o mesmo sino.
3. **Nada da vila é destruído.** Só pomos bloco em ar ou planta (`odteste:substituivel`); a cama manchada é
   **partícula** (não troca a cama); as trocas viram frase **sem mexer** nas ofertas do aldeão; o aldeão que some é
   guardado inteiro e volta no desfazer.
4. **Aldeões parados só com jogador perto** (40 blocos). Parados, ficam invulneráveis (não podem fugir de zumbi por
   nossa causa). Longe de todos, voltam ao normal.
5. **O sino em Morse é por jogador.** Cada um ouve no seu ritmo (Exposição Média ou mais: mais devagar). O som vai só
   para quem está a até 96 blocos; longe, ele sai de um ponto perto do jogador na direção do sino, para ser ouvido de
   longe sem passar do volume 0,7. Os aldeões não "ouvem" o sino de verdade (o sino só balança na tela).
6. **Lucidez:** ouvir a sequência inteira a até 12 blocos tira 4 de Lucidez, no máximo uma vez a cada 5 minutos.
7. **Estrutura por comando:** `/odteste estrutura prever` antes de `colocar` (vale 5 min); se não cabe, **não coloca**:
   mostra um ponto sugerido até 32 blocos e o admin confere com um `prever` lá (correção de 2026-11-09, revisão do
   PR #22); no máximo 16.384 blocos; desfazer só devolve o original onde ainda está o bloco que pusemos.
8. **Claims do FTB Chunks:** se o mod estiver no pack, chunk com dono barra tudo. Se a leitura do claim falhar,
   conta como protegido (na dúvida, não coloca).

**Por quê:** o mundo é o definitivo (decisão de 2026-11-08): tudo que a história faz tem que ser reversível e nunca
pode estragar construção de jogador nem a vila.
