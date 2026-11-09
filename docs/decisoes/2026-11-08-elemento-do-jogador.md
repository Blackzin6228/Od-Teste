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
