# Régua de qualidade (checklist antes de abrir PR)

Toda mesa passa a régua do tipo da peça **antes de abrir o PR** e cola o checklist marcado no corpo do PR.
Item que não deu para cumprir fica desmarcado **com o motivo** (ex.: "som provisório, falta a mesa X").
Referências de onde veio cada ideia: `docs/referencias.md`.

**Nível profissional** aqui quer dizer: o jogador entende a peça sem ler nada, ela tem vida (movimento, som, luz),
combina com o resto do mod e não derruba o servidor nem o FPS.

> Os números de desempenho abaixo são **proposta inicial** da mesa Modelador (2026-11-08), para ajustar no play test.
> A **resolução de textura** ainda está pendente em `docs/decisoes.md`: até decidir, usar 16x e perguntar.

## Para toda peça (vale sempre)
- [ ] Tom sério: passa medo e presença, nada cômico (decisão de 08/11).
- [ ] Nada de nome, símbolo, visual ou lore de outra obra. Nada copiado sem licença em `arte/LICENCAS.md`.
- [ ] Paleta do elemento + **um acento de cor** só.
- [ ] Prévia aprovada (render PNG **frente, lado, costas**; GIF ou vídeo curto se tem animação) no PR.
- [ ] Arquivo fonte `.bbmodel` em `arte/blockbench/` e versão do jogo em `src/main/resources/assets/odteste/`.
- [ ] ID novo anotado em `docs/ids.md`; texto em `pt_br.json` **e** `en_us.json`.
- [ ] Funciona **sem** os mods opcionais (Lodestone, AAA Particles etc.): cai para uma versão simples, sem crash.
- [ ] Testado com **Sodium + Iris** e com um shader pack ligado (os dois estão no pack).
- [ ] Acessibilidade: tremor de tela e flash respeitam a opção de "efeitos de tela" do cliente; nada pisca mais de 3 vezes por segundo.

## Criatura (GeckoLib)
**Silhueta e modelo**
- [ ] Silhueta reconhecível **só pela sombra** a 30 blocos (teste: render todo preto contra fundo claro).
- [ ] Proporção que não é "zumbi com outra cor": pelo menos um traço único (postura, membro a mais/a menos, cabeça).
- [ ] Hitbox do tamanho do corpo visível (nem maior, nem menor que ~10%).

**Animações mínimas**
- [ ] Ocioso (respira, olha em volta; nunca congelada).
- [ ] Andar (e correr, se corre). Pés não "deslizam" no chão.
- [ ] Ataque com **antecipação** visível (0,3 s para ataque comum, 0,5 a 1 s para golpe forte) e continuação depois.
- [ ] Tomar dano (reação curta, sem travar o ocioso) e morte própria (não só o tombo vanilla).
- [ ] Transição suave entre todas (nada de "estalo").
- [ ] Chefe: além disso, entrada, troca de fase e pelo menos 3 ataques diferentes.

**Textura, luz, som, partícula**
- [ ] Textura com sombra pintada e **variação** (nada de cor chapada; pelo menos 3 tons por material).
- [ ] Glowmask (`_glowmask`) só onde faz sentido (olhos, veias). Brilho raro: assusta mais.
- [ ] Sons próprios ou mistura própria: ambiente, passo, ataque, dano, morte. Volume e alcance testados.
- [ ] Partícula ligada ao elemento em momentos-chave (surgir, golpe forte, morte), não o tempo todo.

**Desempenho**
- [ ] Até ~60 cubos para criatura comum, ~150 para chefe.
- [ ] IA não faz busca pesada todo tick (procurar alvo a cada 10-20 ticks, não a cada 1).
- [ ] Limite de quantas nascem por área (spawn cap ou regra do evento).

## Item (modelo Java do Minecraft)
- [ ] Ícone que se lê no inventário em 16x16: forma clara, contorno escuro, um ponto de destaque.
- [ ] Modelo na mão conferido em 1ª e 3ª pessoa, na moldura e no chão (`display` ajustado).
- [ ] Variação de textura (luz de cima à esquerda, como o vanilla; sem ruído aleatório).
- [ ] Item especial (artefato, espada): animação ou textura animada (`.mcmeta`) **ou** partícula ao segurar, e
      som ao usar.
- [ ] Usar o item tem retorno: som + partícula + animação do braço (quando houver ação).
- [ ] Se for 3D (GeckoLib), até ~40 cubos.

## Armadura
- [ ] Prévia vestida em jogador com skin larga e fina (Steve e Alex), frente, lado, costas.
- [ ] Não atravessa a cabeça/capa e fica bem com a **Marca** no corpo (decisão: visual se mistura com a Marca).
- [ ] Peças leem como conjunto (mesma paleta e mesmo detalhe repetido).
- [ ] Animação (GeckoLib) discreta e lenta; nada que distraia em combate.
- [ ] Som de vestir próprio.

## Máquina (bloco com função)
- [ ] O movimento **mostra a função** (o que entra, o que processa, o que sai).
- [ ] Estados visíveis: desligada, trabalhando, sem energia/sem material (luz, cor ou movimento diferentes).
- [ ] Parte animada é peça separada (GeckoLib ou modelo à parte), **nunca** o bloco inteiro recriado a cada tick.
- [ ] Animação só roda perto do jogador (desliga longe, ex.: 32 blocos) e para quando a máquina para.
- [ ] Som contínuo baixo enquanto trabalha, que não cansa depois de 10 minutos ouvindo.
- [ ] Interface (se tiver) segue a régua de interface.
- [ ] Desempenho: lógica do servidor não roda todo tick sem necessidade; testado com 20 máquinas juntas.

## Estrutura
- [ ] Silhueta reconhecível de longe (algo vertical ou estranho no horizonte).
- [ ] Parece **gasta e habitada**: processadores trocam blocos (rachado, musgo, tocha apagada).
- [ ] **Variação**: pelo menos 2 versões das salas principais (jigsaw ou sorteio), nunca igual duas vezes.
- [ ] Encaixa no terreno: não flutua, não corta montanha ao meio.
- [ ] Conta uma história sem texto: objetos de quem passou antes (premissa), pistas no ambiente.
- [ ] Luz e som pensados: escuro de propósito, som ambiente próprio na área.
- [ ] Loot que combina com a história do lugar (nada de baú aleatório).
- [ ] Desempenho: tamanho limitado (proposta: até 64×64 de base para estrutura que nasce sozinha; maior só por
      evento/comando), frequência configurável, testada sem travar a geração de chunk.
- [ ] Não briga com as estruturas dos outros mods do pack (espaçamento e biomas conferidos).

## Efeito (partícula, shader, efeito de tela)
- [ ] Tem **começo, meio e fim**: cresce, vive, some devagar (curva, não liga/desliga seco).
- [ ] Cor do elemento, com dois tons no máximo; brilho aditivo só no acento.
- [ ] Lê de longe e não esconde a luta (o jogador continua vendo o inimigo).
- [ ] Som junto (efeito sem som parece quebrado).
- [ ] Versão simples quando Lodestone/AAA Particles não estão ligados.
- [ ] Respeita a configuração de partículas do jogador (Mínimo / Diminuído / Tudo).
- [ ] Desempenho (proposta): efeito comum até ~50 partículas vivas; efeito grande até ~300 e no máximo 2 grandes ao
      mesmo tempo por jogador; o servidor manda **um pacote** por efeito, não um por partícula.
- [ ] Shader de tela: dura pouco, tem saída suave, funciona com Iris ligado.

## Interface (tela, HUD, livro)
- [ ] Lê sem tutorial: o que é mais importante é o maior/mais claro.
- [ ] Combina com o mod (moldura e cores do elemento), não parece interface vanilla pintada.
- [ ] Texto em `pt_br.json` e `en_us.json`; cabe nos dois idiomas sem cortar.
- [ ] Conferida em escala de GUI 2, 3 e 4 e em janela pequena.
- [ ] HUD não cobre o meio da tela nem a barra de vida/fome; some quando o jogador aperta F1.
- [ ] Som de clique/abrir próprio e discreto.
- [ ] Desempenho: nada calculado por frame que poderia ser calculado uma vez.

---

Régua viva: quando uma peça aprovada mostrar um jeito melhor, a mesa propõe a mudança aqui no PR.
