# Rubra no play test de Sangue

- **Data:** 2026-11-08 · **Mesa:** Dev (PR `mesa-dev/sangue-parte2`)
- **Status:** proposta do Dev, já no código do play test. **Esperando o ok do Nikolas.**

## O que foi decidido para o play test
- A Rubra (`odteste:rubra`) **não tem receita**. Só sai por comando (`/give`) ou, no futuro, pelo evento do chefe.
- **Base:** espada de diamante (7 de dano, velocidade normal), **não quebra**, não queima na lava.
- **Poder** (do lore): com a vida do dono **abaixo de 70%**, cada golpe põe um acúmulo de **sangramento** (até 5).
  Com o acúmulo cheio, o golpe seguinte **abre a ferida**: 5 de dano em área (3 blocos) e sangramento em quem está perto.
- **Custo** (do lore): acima de 70% de vida, é só uma espada de diamante.
- A área da ferida **não pega outros jogadores nem bichos domados** (só o alvo do golpe, se for jogador). Evita acidente em grupo.
- Abrir a ferida conta como "usar relíquia" para a Exposição (fonte `reliquia_usar`, Sangue).

## O que ficou de fora (precisa do Nikolas)
- Os 3 estágios (Adormecida, Desperta, Plena): ainda em aberto em `lore/reliquias-e-espadas.md`.
- Uma espada por jogador ou uma por servidor.
- Drop pelo chefe Coração Aberto (o chefe ainda não existe).
