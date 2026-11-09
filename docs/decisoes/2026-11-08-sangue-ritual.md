# Ritual de giz de Sangue: Chamado de Sangue

- **Data:** 2026-11-08 · **Mesa:** Dev (PR `mesa-dev/sangue-parte2`)
- **Status:** proposta do Dev, já no código do play test. **Esperando o ok do Nikolas** (o nome e o que o ritual faz).

## O que é
Um ritual do **Occultism** que **chama um Retalhado** (hostil) para dentro do círculo. Serve para o grupo
procurar a luta de propósito, em vez de esperar a criatura rara aparecer.

- **Pentáculo:** o de invocar Foliot (`occultism:summon_foliot`, só giz branco: dá para fazer cedo).
- **Ativação:** 1 Sangue Coalhado na tigela dourada.
- **Ingredientes nas tigelas:** 1 Sangue Coalhado, 1 osso, 1 carne podre, 1 barra de ferro.
- **Sacrifício:** uma vaca dentro do círculo durante o ritual. **Duração:** 30 s.
- **No fim:** o Retalhado aparece no círculo; quem fez o ritual ganha Exposição (fonte `ritual`, Sangue) e perde um pouco de Lucidez.

## Sem farm
O ritual custa 2 Sangue Coalhado e o Retalhado solta no máximo 2 (com Pilhagem). Não dá lucro de matéria.

## Sem o Occultism
O código fica em `compat/occultism/` e só carrega se o Occultism estiver no pack. A receita tem a condição
`neoforge:mod_loaded`: sem o Occultism, o ritual simplesmente não existe e nada quebra.
