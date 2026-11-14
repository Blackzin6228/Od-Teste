# O Que Segue e "a tech chama o paranormal" (2026-11-13) — PROPOSTA

**Situação:** PROPOSTA. Entrou jogável para o Nikolas testar; nome, visual e números mudam depois do teste.
Vem de `2026-11-13-identidade-do-servidor.md` (ser caçado por algo que não se vence; roda sem mestre; tech livre com custo).

**Decisão proposta:**
- **O Que Segue** (nome provisório, ID `odteste:o_que_segue` fica): silhueta humanoide alta (2,8 blocos), magra, preta,
  cabeça inclinada, olhos fundos sem brilho. Modelo e textura nossos, simples (a Modeladora pode refazer depois).
- Aparece sozinha **à noite** ou com **Exposição 60+** (degrau Fundo), só no Overworld, para quem está **sozinho**
  (nenhum jogador a 24 blocos), no máximo uma por jogador, a **30–45 blocos**, atrás dele e fora da vista (de preferência
  atrás de árvore ou parede). Chance base de 3% a cada ~10 s, mais com Exposição e máquinas.
- **Não anda: salta.** Quando ninguém olha, avança 3–8 blocos pelo caminho a cada 1,5–5 s. Passos tocam atrás do jogador
  (só ele ouve). Sem caminho (casa fechada), espera; não quebra bloco nem atravessa parede.
- **Olhada:** congela. **Encarada** por ~3 s, ou encarada a menos de 8 blocos: some e volta em 5 min.
- **Pega** se chega a 3 blocos sem ser vista: escuridão (Darkness), golpe paranormal de 14 (com a vida cheia nunca mata),
  som grave, sobe a Exposição. Some e volta em 15 min (30 se ele dormiu ou estava num lugar claro com outro jogador).
- Não pode ser ferida: golpe ou flecha fazem ela sumir. O mestre (tag `odteste_mestre`), criativo e espectador nunca
  são alvo sozinhos.
- **A tech chama o paranormal:** a cada ~30 s, conta blocos com energia (qualquer mod) num raio de 24. 16+ máquinas
  sobem um pouco a Exposição (fonte `maquinas`, com o freio de repetição); 8+ deixam ela mais frequente e mais rápida
  (máximo com 64+).
- Config em `serverconfig/odteste-segue-server.toml` (dá para desligar). Admin: `/odteste segue chamar|afastar <jogador>`.

**A confirmar no teste:** nome; se ela vem toda noite já para jogador novo; números (distância, recarga, dano, chance);
se a Exposição das máquinas conta como contato com Energia (hoje é sem elemento, para não mexer na Marca).
