# Artefatos Paranormais de Sangue no play test

- **Data:** 2026-11-08 · **Mesa:** Dev (PR `mesa-dev/sangue-parte2`)
- **Status:** proposta do Dev, já no código do play test. **Esperando o ok do Nikolas.**

Os três vêm de `lore/artefatos-paranormais.md` (nomes fechados). **Sem receita**: só `/give` por enquanto
(as estruturas, o Ventre e o chefe ainda não existem).

| Artefato | Poder | Custo |
|---|---|---|
| Agulha de Cerzir Carne (comum) | Regeneração II por 10 s (≈ 4 corações) e para o sangramento | Exposição +10; +1 ponto de contato com o Sangue ("progresso da Marca") |
| Dedal Dentado (incomum) | Por 20 s, quem bate de perto leva de volta 30% do dano como sangramento | Lucidez −5; −1 coração máximo até dormir a noite toda |
| Sino de Matadouro (raro) | Jogadores a até 8 blocos (quem toca também) ganham Força I e Velocidade I por 15 s | 3 corações de quem toca; Exposição +25. Com 3 corações ou menos, não toca |

## Regras anti-abuso que entraram
- Recarga própria (comum 60 s, incomum 3 min, raro 10 min) e **recarga compartilhada de 10 s** entre os três.
- A recarga fica **salva no jogador** (`odteste:artefato_recarga`): sair e voltar não zera.
- Usar o mesmo de novo em menos de 5 min cobra **o dobro** de Lucidez ou Exposição.
- **Lucidez zero** bloqueia o uso.
- Não empilha: usar de novo reinicia o tempo do efeito.
- "Progresso da Marca": como a Marca só sobe de estágio por comando hoje, o progresso entra como **contato com o
  Sangue** (o mesmo número secreto que decide qual Marca aparece).
- A Exposição de custo dos artefatos **não passa pelo freio de repetição** (a recarga e o dobro já freiam). Ela também para em 99.

## O que ficou de fora (precisa do Nikolas ou de outra parte do jogo)
- Limite diário dos raros (2 por dia real) e se tem limite semanal (pergunta 3 do lore).
- 1 de cada por jogador, 3 ativos no máximo, 1 por elemento ativo.
- Vínculo ao primeiro dono (recarga de 24 h ao trocar de dono), sumir 10 min depois de cair no chão, regra de PvP.
- Curios (equipável) ou só na mão (pergunta 4 do lore). Hoje: só na mão.
- Exposição por **pegar** um artefato (`reliquia_pegar`): entra quando houver baú/estrutura.
