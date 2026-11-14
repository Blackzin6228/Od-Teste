# Biblioteca de referência (mods que fazem bem feito)

> **Regra deste documento (vale para todas as mesas):**
> 1. Daqui a gente **aprende a técnica** (como foi feito) e **escreve do nosso jeito**, com código e arte nossos.
> 2. **Copiar código ou arte** de outro mod só com **licença compatível** e **registrada antes** em `arte/LICENCAS.md`
>    (regra 10 do `CLAUDE.md`). Na dúvida, não copia.
> 3. **Nada de lore, nome, símbolo ou visual** de outra obra (regra 3 do `CLAUDE.md`). Pegamos o *como*, nunca o *o quê*.
> 4. "Assets" (modelos, texturas, sons) quase sempre são **todos os direitos reservados**, mesmo quando o código é livre.
>    Licença de código livre **não** libera a arte.

Pesquisa feita em 2026-11-08 pela mesa Modelador. Licenças conferidas no arquivo `LICENSE` do repositório oficial
(pela API do GitHub) e na página do Modrinth. Quando as duas fontes não batem ou não há arquivo, está marcado.

**No pack?** = o mod já está no `Conguitos-smp` (dá para abrir o jogo e estudar ao vivo, sem baixar nada de fora).

## Resumo

| Mod | Aprender com ele | Licença do código | Licença da arte | No pack? |
|---|---|---|---|---|
| Lodestone | partículas com curva, render types aditivos, tremor de tela, pós-processo | LGPL-3.0 (repo) | segue o repo (sem arte relevante) | sim (1.8.2) |
| Malum | brilho de "espírito", paleta escura com acento forte, rituais com VFX | LGPL-3.0-or-later (Modrinth) · **repo sem arquivo de licença** | **não confirmada** | sim (1.8.2) |
| GeckoLib | animação por osso, controladores, glowmask, armadura animada | MIT | — (biblioteca) | sim (4.9.3) |
| AAA Particles + Effekseer | efeitos grandes feitos num editor e tocados no jogo | MIT (os dois) | os efeitos de exemplo seguem o repo; os nossos serão próprios | sim (2.3.4) |
| Create (+ Flywheel) | máquina animada sem lag (instancing), peças separadas, Ponder | MIT (Create e Flywheel) | **Todos os direitos reservados** (`assets/`) | sim (6.0.10) |
| Mowzie's Mobs | chefes com aviso de ataque, peso, antecipação | licença própria, **todos os direitos reservados** + não-concorrência | todos os direitos reservados | sim (1.8.2) |
| L_Ender's Cataclysm | chefe + arena como uma peça só, fases, música | CC-BY-NC-ND-4.0 (Modrinth) | idem (sem derivados) | sim (3.33) |
| Alex's Caves | bioma coeso: paleta, névoa, som ambiente, criaturas | LGPL-3.0-only (Modrinth) · **repo sem arquivo de licença** | **não confirmada** | sim (2.0.10) |
| YUNG's Better Dungeons (e família) | estrutura que varia, se encaixa no terreno, "conta história" | LGPL-3.0 | segue o repo (blocos vanilla) | sim (5.1.4) |
| Repurposed Structures | variante por bioma, espaçamento configurável | LGPL-3.0 | segue o repo | não |
| When Dungeons Arise | escala e silhueta de estruturas gigantes | Todos os direitos reservados, **sem código aberto** | todos os direitos reservados | sim (2.1.68) |
| Iron's Spells 'n Spellbooks | qualidade de magia (projétil, rastro, som, animação do jogador) | **Todos os direitos reservados** (pode addon; não pode usar assets) | todos os direitos reservados | sim (3.16.3) |
| Ars Nouveau | criaturas GeckoLib com charme, partículas de magia leves | LGPL-3.0 | **Todos os direitos reservados** | não |
| Occultism | círculo de giz e ritual legível no chão | MIT | **não confirmada** (há `THIRD_PARTY_NOTICES.md`) | sim |
| Mekanism | interface de máquina clara, multibloco | MIT | **não confirmada** separadamente | sim |
| Iris + Sodium | não é referência visual: é o **teste de compatibilidade** dos nossos shaders | LGPL-3.0 (Iris) | — | sim |
| Deeper and Darker | dimensão escura com identidade | AGPL-3.0 (cuidado: copiar código "contamina") | não conferida | não |
| Veil | biblioteca de render moderna (pós-processo, luz dinâmica) | LGPL-3.0 | — | não |

Legenda das licenças, em uma frase cada:
- **MIT:** pode copiar e adaptar o código, mantendo o aviso de copyright. Ainda assim registramos em `arte/LICENCAS.md`.
- **LGPL-3.0:** pode **usar como dependência** sem problema; se copiar o código para dentro do nosso mod, aquela parte
  continua LGPL e precisa ficar aberta. Preferimos depender ou reescrever.
- **AGPL / GPL:** copiar obriga o mod inteiro a seguir a mesma licença. **Não copiar.**
- **CC-BY-NC-ND:** sem uso comercial e **sem obra derivada**. Só olhar.
- **Todos os direitos reservados:** só olhar e jogar. Nada sai de lá.

---

## Efeitos e partículas

### Lodestone (biblioteca do Malum)
- **Repositório:** `LodestarMC/Lodestone` (branch `1.21`). Licença: **LGPL-3.0**.
- **O que faz bem:** dá às partículas uma "vida" com começo, meio e fim. Nada aparece e some seco.
- **Técnica:**
  - Partícula com **curvas de valor** (escala, cor, transparência e giro mudam com *easing* ao longo da vida):
    nasce pequena, cresce rápido, some devagar.
  - **Render type aditivo** (a cor soma com o fundo): num lugar escuro vira brilho sem precisar de shader de bloom.
  - Cor em **dois tons** (início → fim), por exemplo vermelho vivo que termina vinho escuro.
  - **Tremor de tela** com intensidade e curva de queda, e **rastros** (*trails*) feitos de pontos que seguem um objeto.
  - **Pós-processo** (shader de tela inteira) ligado e desligado por evento.
- **Para nós:** é a base técnica ideal para Marca, Exposição e magia. Como já está no pack, pode entrar como
  **integração opcional** em `compat/lodestone/` (regra 4). Sem ele, cair para partículas vanilla mais simples.

### Malum
- **Repositório:** `SammySemicolon/Malum-Mod` (branch `1.21.1`). Modrinth diz **LGPL-3.0-or-later**, mas o
  repositório **não tem arquivo de licença**. Arte: **não confirmada**. Tratar como "só olhar".
- **O que faz bem:** o brilho dos "espíritos" é a marca do mod. Leitura instantânea: cada tipo tem cor própria.
- **Técnica:** fundo escuro e dessaturado + **um acento saturado que brilha** (aditivo). Itens com camada que brilha
  no escuro. Rituais com partículas que **convergem** para o centro (dá sensação de "puxar").
- **Para nós:** confirma a nossa regra "um acento de cor por peça". O brilho deve ser **raro** no nosso tom sério:
  brilho demais vira fantasia alegre. No horror, o acento aparece pouco e assusta mais.

### AAA Particles + Effekseer
- **Repositórios:** `ChloePrime/AAAParticles` (**MIT**) e `effekseer/Effekseer` (editor, **MIT**).
- **O que faz bem:** efeitos de "jogo grande" (explosões em camadas, anéis, distorção) que partícula vanilla não faz.
- **Técnica:** o efeito é montado no **editor Effekseer** (programa grátis) e salvo como `.efkefc`. O mod carrega o
  arquivo do resource pack e toca no cliente. É só cliente (o servidor não precisa).
- **Para nós:** `docs/animacoes.md` já cita AAA Particles para os momentos grandes (Entrega da Marca). Os efeitos
  `.efkefc` têm que ser **feitos por nós** no editor. Integração opcional em `compat/aaaparticles/`; sem o mod, usar
  a versão Lodestone/vanilla. **Custo:** efeito grande pesa; limitar quantos tocam ao mesmo tempo.

### Iron's Spells 'n Spellbooks
- **Repositório:** `iron431/irons-spells-n-spellbooks`. Licença: **todos os direitos reservados** (pode usar no
  modpack e fazer addon; **não** pode usar nenhum asset, nem pessoal).
- **O que faz bem:** magia que se lê de longe: **animação do jogador** ao conjurar, projétil com rastro, impacto com
  som próprio, cor por escola.
- **Técnica:** cada magia tem 4 tempos: **preparo** (pose + partícula na mão), **solta**, **viagem** (rastro),
  **impacto** (partícula + som + às vezes tremor). A cor da escola é constante em todos os tempos.
- **Para nós:** é a régua das magias (decisão de 07/11: nível Apex Arcana). Licença do Apex Arcana: **não confirmada**.

### Apex Arcana (efeitos de tela com shader)
- **Licença:** o `neoforge.mods.toml` do jar declara **MIT** (conferido em 14/11/2026 pela mesa Dev). Mesmo assim,
  nada foi copiado: aprendemos a técnica e escrevemos os nossos do zero.
- **O que aprendemos:** (1) copiar a tela (cor e profundidade) para uma textura e desenhar um quadro na tela inteira
  que lê dela; (2) voltar da profundidade para o ponto no mundo com a inversa da projeção e da vista, e tirar a
  normal pela variação entre pixels (assim o sangue fica só no chão de verdade, ou só na parede); (3) perguntar ao
  Iris por reflexão se há shader pack; (4) com pack, desenhar **depois** que o pack terminou a imagem, guardando as
  matrizes do quadro antes. Os shaders `fx_blood_floor`/`fx_blood_wall` deles mostraram a ideia de poça por
  profundidade.
- **Onde está no nosso:** `cliente/FxTela.java` (base), `compat/iris/PackIris.java`, `cliente/FxSangue.java`,
  `shaders/core/odfx_*.fsh`. Diferenças nossas: o Apex desliga a passada de tela com pack; nós desenhamos no começo
  da HUD por cima da imagem do pack, conferimos se a profundidade que o pack deixou presta e, se não, usamos a
  versão sem profundidade em vez de desligar.

### Hand of God (estudo, sem copiar)
- **Licença:** todos os direitos reservados. Só estudado (como encadeia efeitos de tela). Nada copiado.

---

## Modelos e animação de criaturas

### GeckoLib
- **Repositório:** `bernie-g/geckolib`. Licença: **MIT**. É dependência obrigatória nossa (regra 4).
- **O que faz bem:** leva o modelo e a animação do Blockbench direto para o jogo.
- **Técnica:**
  - **Animação por osso** com *keyframes* e curvas (o Blockbench exporta; o jogo toca).
  - **Controladores**: várias animações ao mesmo tempo (andar + respirar + virar a cabeça) com **transição suave**
    entre elas (`transitionLength`).
  - **Animações disparadas** (ataque, grito) pelo servidor, sem trocar o estado de andar.
  - **Glowmask**: uma segunda textura `_glowmask` só com as partes que brilham no escuro (olhos, veias).
  - **Armadura animada** e itens com modelo 3D.
- **Para nós:** formato de toda criatura e armadura. Sempre usar transição (nada de "estalo" entre animações).

### Mowzie's Mobs
- **Repositório:** `BobMowzie/MowziesMobs-Public`. Licença **própria**: todos os direitos reservados, com cláusula de
  **não-concorrência** (não pode oferecer conteúdo deles). **Só olhar e jogar.**
- **O que faz bem:** os chefes têm **peso**. Você sente o tamanho e sabe o que vem.
- **Técnica (princípios de animação):**
  - **Antecipação**: o golpe forte tem um "carregar" visível (0,5 a 1 s) antes. Isso é justo com o jogador.
  - **Continuação** (*follow-through*): depois do golpe, o corpo continua um pouco e volta.
  - **Tremor de tela e poeira** no impacto pesado, proporcional ao tamanho.
  - Ciclo de **ocioso** vivo (respira, olha em volta). Criatura parada nunca fica "congelada".
- **Para nós:** régua de animação de criatura e chefe.

### L_Ender's Cataclysm
- **Fonte:** `lender544/...` (link do Modrinth). Licença: **CC-BY-NC-ND-4.0** (sem uso comercial, sem derivados).
- **O que faz bem:** **chefe + arena** pensados juntos. A estrutura prepara o encontro (caminho, portão, sala).
- **Técnica:** chefe com **fases** (muda ataque e visual com a vida), **barra de chefe** e **música própria**,
  arena com espaço para desviar dos ataques.
- **Para nós:** modelo de encontro para as entidades e chefes por elemento.

### Alex's Caves
- **Fonte:** `AlexModGuy/AlexsCaves`. Modrinth diz **LGPL-3.0-only**; repositório **sem arquivo de licença**;
  arte **não confirmada**. Tratar como "só olhar". No Modrinth só aparece 1.20.1; no pack está a 2.0.10.
- **O que faz bem:** cada bioma é um "lugar" coeso: paleta própria, névoa, som ambiente, criaturas que combinam.
- **Técnica:** **paleta restrita por bioma** (3 a 5 cores), **névoa e cor do céu** próprias, **som ambiente em
  camadas** (fundo contínuo + sons raros), criaturas com **silhueta única** que se reconhece a 30 blocos.
- **Para nós:** régua das 4 dimensões de elemento.

### Ars Nouveau
- **Repositório:** `baileyholl/Ars-Nouveau`. Código **LGPL-3.0**; assets **todos os direitos reservados**.
- **O que faz bem:** criaturas GeckoLib pequenas com muita personalidade em poucas animações.
- **Para nós:** prova que **3 a 5 animações bem feitas** valem mais que 15 rasas. (O tom deles é fofo; o nosso não.)

---

## Máquinas

### Create (+ Flywheel)
- **Repositórios:** `Creators-of-Create/Create` (código **MIT**, pasta `assets/` **todos os direitos reservados**)
  e `Engine-Room/Flywheel` (**MIT**).
- **O que faz bem:** dezenas de máquinas girando **sem derrubar o FPS**, e você entende o que cada uma faz só olhando.
- **Técnica:**
  - **Peças separadas** (*partial models*): a parte que gira é um modelo à parte, desenhado pela placa de vídeo
    com **instancing** (Flywheel), sem refazer o bloco a cada tick.
  - **Material legível**: poucos materiais (pedra, latão, cobre) com paleta fixa; dá para reconhecer o "tier".
  - **Movimento mostra função**: o que gira, empurra ou corta se vê.
  - **Ponder**: cenas que ensinam a máquina dentro do jogo.
  - Som discreto e contínuo enquanto trabalha.
- **Para nós:** régua dos 3 aparelhos de `docs/tech-paranormal.md`. Parte animada em GeckoLib ou separada, nunca
  bloco inteiro recriado todo tick.

### Mekanism
- **Repositório:** `mekanism/Mekanism`. Código **MIT**; arte **não confirmada** separadamente.
- **O que faz bem:** interface de máquina clara (barras, entradas/saídas por cor) e multiblocos com prévia.
- **Para nós:** régua da interface das máquinas.

---

## Estruturas

### YUNG's (Better Dungeons, Mineshafts, Strongholds...) + YUNG's API
- **Repositórios:** `YUNG-GANG/YUNGs-Better-Dungeons` e `YUNG-GANG/YUNGs-API`. Licença: **LGPL-3.0**.
- **O que faz bem:** estrutura que parece **gasta e habitada** e nunca é igual duas vezes.
- **Técnica:**
  - **Jigsaw** (peças que se encaixam) com várias versões de cada sala.
  - **Processadores de bloco**: troca aleatória (tijolo → rachado → musgo, tocha → apagada) para envelhecer.
  - **Adaptação ao terreno**: a estrutura enterra a base ou cava o espaço, sem flutuar.
  - **Histórias no ambiente**: baú de alguém, cama quebrada, marca na parede. A sala "conta" o que aconteceu.
- **Para nós:** régua das estruturas (combina com "outras pessoas já atravessaram antes" da premissa).

### Repurposed Structures
- **Repositório:** `TelepathicGrunt/RepurposedStructures`. Licença: **LGPL-3.0**.
- **O que faz bem:** variante por bioma e **espaçamento configurável** (o dono do servidor ajusta a frequência).
- **Para nós:** toda estrutura que nasce sozinha deve ter frequência ajustável e não brigar com as outras do pack.

### When Dungeons Arise
- **Licença:** todos os direitos reservados, **sem código aberto**. Só jogar e olhar.
- **O que faz bem:** **silhueta** reconhecível de longe (torres, pontes, navios no céu).
- **Cuidado:** estruturas enormes custam geração de chunk. Para nós: grande só quando o evento pede, e por comando
  (`docs/sistemas.md`), não espalhado no mundo.

---

## Compatibilidade de render (não é referência visual)

### Iris + Sodium (no pack)
- **Iris:** `IrisShaders/Iris`, **LGPL-3.0**. Os jogadores podem usar shader pack.
- **Para nós:** todo efeito nosso (render type, shader de tela, glowmask) **tem que ser testado com Sodium + Iris
  ligados e com um shader pack ligado**. Muitos efeitos de mod quebram ou somem com shader.

### Veil
- **Repositório:** `FoundryMC/Veil`, **LGPL-3.0**. Não está no pack. Só referência de como fazer pós-processo e luz
  dinâmica de forma moderna. **Não** vira dependência sem decisão do Nikolas.

---

## Pendências desta pesquisa
- **Malum** e **Alex's Caves**: o Modrinth declara LGPL, mas o repositório não tem arquivo de licença. Arte não confirmada.
  Enquanto isso: só olhar, nada copiado.
- **Occultism** e **Mekanism**: licença da arte não conferida separadamente.
- **Apex Arcana** (referência de magia): o jar declara MIT (conferido em 14/11/2026); a arte não foi usada.
- Licenças mudam. Antes de copiar qualquer coisa, conferir de novo no repositório e registrar em `arte/LICENCAS.md`.
