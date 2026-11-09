# Criaturas de Sangue no play test (Retalhado e Sanguessuga-Mãe)

- **Data:** 2026-11-08 · **Mesa:** Dev (PR `mesa-dev/sangue-parte2`)
- **Status:** proposta do Dev, já no código do play test. **Esperando o ok do Nikolas.**

## Onde e quando nascem (raro, só em condição paranormal)
Todas as condições juntas:
1. **Nunca nos 3 primeiros dias de jogo do mundo** (relógio do mundo; `/time set` não engana).
2. **Nunca a menos de 256 blocos do spawn do mundo.**
3. Só **perto (até 48 blocos) de um jogador "Tocado"** (Exposição 20+): o paranormal vem atrás de quem já o percebeu.
4. Onde um monstro comum também nasceria (escuro, dificuldade não pacífica), só no Overworld.
5. Peso baixo no bioma: Retalhado 4, Sanguessuga-Mãe 3 (um zumbi tem 95).

Spawner, ritual e comando ignoram as regras 1 a 3 (seguem só a de monstro comum).

## Poder
- **Retalhado** (3 blocos de altura, 60 de vida): **Investida de Ganchos** a cada 8 s (corre em linha reta, crava, puxa o
  alvo para perto e deixa sangrando). Golpe **pelas costas** dói 1,5x ("cego nas costas").
- **Sanguessuga-Mãe** (2,6 de largura, 40 de vida): **Chuva de Filhotes** a cada 6 s (larvas que grudam: sangramento e a
  mãe se cura). Na **luz forte** (12+), os filhotes morrem no ar. Os filhotes são só partícula (nenhuma entidade a mais).
- A fraqueza a Conhecimento fica para quando existir dano de Conhecimento.

## Com os sistemas
- Ver uma de perto (12 blocos, à vista): Exposição (`criatura_vista`) e Lucidez (`mob_sangue`), **uma vez por criatura**.
- Cada troca de golpes: Exposição (`criatura_luta`); matar: Exposição (`criatura_morta`). O freio de repetição vale.
- As duas estão na tag `odteste:paranormal`: o dano delas pesa mais na Lucidez.

## Drop
- **Sangue Coalhado** (`odteste:materia_sangue_bruta`): Retalhado 1 (até 2 com Pilhagem), Sanguessuga-Mãe 0 a 1 (até 2).
- Só cai se quem matou foi **jogador de verdade** (máquina com jogador falso não conta) e se a criatura **não veio de spawner**.

## Pendente
- Os nomes "Retalhado" e "Sanguessuga-Mãe" ainda estão com "(?)" em `lore/criaturas.md`. **O ID fica para sempre**
  (`odteste:retalhado`, `odteste:sanguessuga_mae`); o nome na tela pode mudar depois sem problema.
- Modelo e textura são **provisórios** (blocos simples, textura gerada em código). Vão para a mesa Modelador.
