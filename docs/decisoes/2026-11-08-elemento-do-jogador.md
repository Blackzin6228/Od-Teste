# Elemento do jogador (2026-11-08)
**Decisão:** cada jogador tem um elemento (Sangue, Conhecimento, Morte ou Energia) **definido pelo admin**, que fica
**escondido até a história revelar**.
- Comandos (só admin): `/odteste elemento <jogador> <sangue|conhecimento|morte|energia|nenhum>` e
  `/odteste elemento revelar <jogador>`. Sem elemento, `/odteste elemento <jogador>` mostra o que está salvo.
- **Marca no corpo:** só cresce no elemento do jogador. Sem elemento, tudo continua como antes (o contato decide).
- **Espada e artefatos do próprio elemento custam menos** (30% a menos de Lucidez, Exposição ou vida). Sem número na tela.
- **Antes de revelado, o jogador não vê o elemento em lugar nenhum.** `/exposicao` e `/lucidez` não citam.
- O sistema de eventos usa marcadores nos textos: `{escolhido_sangue}`, `{escolhido_conhecimento}`, `{escolhido_morte}`,
  `{escolhido_energia}`. Nenhum nome de jogador fica no código nem nos docs.

**Como ficou no código (Dev, proposta):**
- Antes de revelado, a Marca **não aparece** (ela é visível no corpo e entregaria o segredo). O contato continua
  contando escondido; ao revelar, se o jogador já é Tocado, a Marca aparece no elemento dele.
- A Marca de quem tem elemento nunca troca para outro. Uma Marca antiga de outro elemento volta para o elemento
  dele (se ainda no estágio 1 e já revelado).
- Rubra: o custo é a vida que precisa faltar. Dono de Sangue: acorda abaixo de 79% (em vez de 70%).
- Trocar o elemento de um jogador esconde de novo.
- O elemento nunca vai para o cliente (nem do dono), então nem um cliente modificado descobre.

**Por quê:** o narrador controla quem é de cada elemento, e a revelação vira um momento da história.

## Lado da história (Mesa Lore, 08/11/2026; falta o Nikolas conferir o texto)
- O elemento muda o **papel na história**: a pessoa é a **escolhida** daquele elemento. Os enigmas e a entidade daquele
  elemento falam com ela, e o Ato (ou trecho do Ato) daquele elemento depende dela. Os outros elementos continuam usáveis,
  só custam mais. Ninguém fica com item inútil.
- **Ato 1:** a revelação acontece na **Mesa do Pacto**. O prato e o cálice com o nome do jogador revelam o elemento dele.
  Só o de **Sangue** fica claro no Ato 1. Os outros aparecem como **presságio** (sinal que se entende só depois).
- **O admin, que também joga, NÃO tem elemento** (decisão do Nikolas, 08/11/2026). Ele é aquele que o paranormal **não consegue ler**.
  - Não recebe Marca e não é "escolhido" de nenhum elemento.
  - Na Mesa do Pacto o prato dele existe, mas o cálice não reage a ele.
  - O paranormal se confunde com ele (entidades) e os enigmas não falam com ele; em compensação, em algum momento ele vê o que os escolhidos não veem.
  - No texto é sempre o marcador `{ilegivel}`, nunca um nome (o repositório é público). Papel no roteiro: `lore/roteiro.md`.
  - `{ilegivel}` não tem plano B: se o admin não estiver jogando, o marcador fica vazio e o trecho do texto que o usa não aparece.
- Se ninguém do grupo tem o elemento, o texto cai num plano B (`{marcado}` ou o que o admin escolher).
  Se mais de um jogador tem o mesmo elemento, todos são "escolhidos" e o marcador vira lista.

### Pendente
- Quantos jogadores por elemento é o ideal (um grupo com dois de Sangue e nenhum de Morte trava o Ato de Morte?).
- Como o jogador "se descobre" se o admin mudar o elemento depois (troca vale? só antes da Mesa?).
