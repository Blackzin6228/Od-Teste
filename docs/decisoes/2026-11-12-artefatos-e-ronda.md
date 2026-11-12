# Artefatos achados só de três jeitos, e criaturas de Sangue rondando a Vila Calada

- **Data:** 2026-11-12 · **Decisão do Nikolas** · **Mesa:** Dev (PR `mesa-dev/artefatos-e-ronda`)

## Artefatos Paranormais só vêm de
1. **Baú das nossas estruturas**, como recompensa de enigma. A tabela de loot é nossa
   (`odteste:chests/recompensa_artefato_sangue`) e o mod a aplica ao colocar a estrutura, só nos baús que o template
   marca com `odteste_recompensa: artefato_sangue` e que estão vazios. Nunca entra em tabela do vanilla ou do ATM10.
2. **Drop raro de criatura de Sangue derrotada**: um sorteio por criatura (no máximo 1 artefato), 3%; quem já tem
   algum no inventário tem 1/4 dessa chance e o sorteio prefere o que falta. Pesos: Agulha 6, Dedal 3, Sino 1.
   Só jogador de verdade (não máquina), nunca de spawner.
3. **Evento do admin**: `/odteste artefato dar <jogador> <artefato>`.

Nada de artefato em baú comum, loja, villager ou receita. Já ficam fora do criativo e escondidos no JEI/EMI; um teste
confere que nenhum arquivo de `data/` cita um artefato fora da tabela de recompensa.

## Ronda na Vila Calada
De noite (depois do 3º dia), até 128 blocos da âncora `vila_calada`, a cada 30 s o servidor olha até 4 jogadores e
cada um tem 6% de fazer nascer uma criatura de Sangue a 24-40 blocos dele (30% Sanguessuga-Mãe). Limites: no máximo
2 vivas em volta da âncora e 1 por chunk; fora de Mushroom Fields e biomas sem monstro; mesmas regras de luz e
dificuldade de monstro comum. Sem âncora, nada acontece. O nascimento natural de antes não mudou.

## Pendente
O `vila_calada.nbt` ainda não tem baú marcado: o gerador (`ferramentas/estruturas`, mesa Modelador) precisa pôr a
chave `odteste_recompensa` no baú do enigma que o Nikolas escolher.
