# Guia de estilo visual (proposta da mesa Modelador)

> 🟦 **Esperando aprovação do Nikolas.** Nada aqui vira regra até entrar em `docs/decisoes.md`.
> Pendente principal: **resolução das texturas** (seção 3).

Base: `docs/estilo-do-genero.md` (cores dos elementos) e `docs/decisoes.md`. Tudo é feito para **dentro do Minecraft**:
modelo de cubos, textura em pixel, sem shader obrigatório, leve para um servidor com ~410 mods.

---

## 1. Paleta por elemento

Cada elemento tem **3 tons**: sombra, base e brilho. A coluna "acento" é a cor que pode aparecer em **pontos pequenos**
(olhos, runas, fio da lâmina), nunca como cor principal. Regra da mesa: **um elemento por peça** e, no máximo, um acento.

| Elemento | Sombra | Base | Brilho | Acento | Ideia |
|---|---|---|---|---|---|
| 🩸 Sangue | `#4A0A12` | `#8E1624` | `#D9393B` | `#FF8A6B` (carne viva, só em brilho úmido) | vermelho grosso, ferrugem, carne |
| 💀 Morte | `#0C0B0F` | `#25222B` | `#6F6979` | `#C9C2B0` (osso/cinza) | lodo preto, poeira, osso velho |
| 📜 Conhecimento | `#6E4A10` | `#C9961F` | `#FFE27A` | `#FFF6D8` (branco-papel) | dourado de sigilo, papel, máscara |
| ⚡ Energia | `#2B1060` | `#7B2FD6` | `#3FF2FF` | `#FF4FD8` rosa ou `#4DFF8A` verde (escolher **um** por peça) | neon, cabo, fantasma |
| 👁️ Medo (fundo) | `#8D939D` | `#D5D9E0` | `#FFFFFF` | — | névoa, vazio, chama translúcida |

**Neutros** (servem para qualquer elemento, são o "corpo" das peças):

| Material | Contorno | Sombra | Base | Brilho | Fio/reflexo |
|---|---|---|---|---|---|
| Metal | `#1C1A21` | `#3A3843` | `#676673` | `#A3A2B0` | `#E2E1EA` |
| Couro / madeira | — | `#2A1913` | `#4E2E21` | `#7A4B33` | — |
| Tecido escuro (roupa de agente) | — | `#1A1C24` | `#2F3340` | `#4C5263` | — |

Por que assim: no Minecraft a cor é a primeira coisa que o jogador lê à distância. Se cada elemento tem **sempre** os
mesmos 3 tons, o jogador reconhece "isso é de Morte" antes de ler o nome do item.

## 2. Regras de textura

1. **Só cores da paleta.** Cor nova precisa entrar aqui primeiro. Por peça: até ~5 tons por material.
2. **Sombra puxa para frio, brilho puxa para quente** (já embutido na paleta). Nada de escurecer só com preto.
3. **Luz vem de cima e da frente-esquerda** (igual à prévia). Topo mais claro, fundo mais escuro.
4. **Sem ruído e sem filtro** (nada de "noise" do Photoshop, gradiente suave ou foto). Cada pixel é colocado à mão.
5. **Contorno escuro só no metal e só onde separa peças.** Em modelo 3D não se contorna a silhueta inteira.
6. **Brilho de elemento = máscara de brilho.** Partes que brilham no escuro (runas, olhos, neon) vão numa textura
   separada `<nome>_glowmask.png` (GeckoLib desenha sem sombra) ou, em item comum, num cubo com luz máxima no JSON
   do modelo (a mesa Dev confirma o campo do NeoForge 1.21.1). Não pintar "brilho" com cor
   clara achando que vai brilhar: no escuro do Minecraft ela some.
7. **Símbolos são nossos.** Nenhum sigilo ou símbolo copiado da obra (regra 3 do `CLAUDE.md`). Desenhar o nosso no estilo do elemento.
8. **PNG RGBA, sem entrelaçamento.** Transparência só 0 ou 255 (meia-transparência só em vidro/névoa, e com `render_type` translúcido).
9. **Mesma densidade de pixel no mod inteiro** (seção 3): um cubo de 1 bloco tem sempre o mesmo número de pixels por lado.

## 3. Resolução: 16x, 32x ou 64x — o Nikolas decide

"16x" quer dizer: **1 bloco = 16 pixels de textura**. Um mob de 2 blocos de altura em 16x usa uma textura 64×64;
em 32x, 128×128; em 64x, 256×256. O que importa é a **densidade**, não o tamanho do arquivo.

| | 16x | 32x | 64x |
|---|---|---|---|
| Combina com o pack (~410 mods, quase todos 16x) | ✅ igual a tudo | 🟨 destaca, às vezes "de outro jogo" | ❌ destoa muito |
| Detalhe (rosto, runa, sigilo) | 🟨 pouco, exige pixel bem pensado | ✅ bom | ✅ muito |
| Peso (memória de vídeo, atlas de texturas) | ✅ mínimo | 🟨 4× | ❌ 16× (atlas do pack já é grande) |
| Tempo para fazer cada textura | ✅ rápido | 🟨 ~2–3× | ❌ ~6×+ |
| Retoque pelo Nikolas no Blockbench | ✅ fácil | 🟨 ok | ❌ difícil |
| Item na mão / no inventário | ✅ padrão | 🟨 ok (o jogo reduz) | ❌ vira borrão no inventário |

**Recomendação da mesa: 16x em tudo.** Para peças especiais (as 4 relíquias e as 4 espadas principais), detalhe vem de
**modelo 3D bem feito + máscara de brilho + animação**, não de mais pixels. Se o Nikolas quiser mais detalhe, a segunda
opção é **32x só para criaturas grandes e chefes** (que são vistos de longe e grandes na tela), mantendo itens em 16x.
64x não recomendo: pesa, destoa do pack e cada textura demora muito.

## 4. Proporções

Unidade = 1 pixel do Minecraft (16 = 1 bloco). Corpo do jogador: cabeça 8×8×8, tronco 8×12×4, braços 4×12×4
(magro: 3×12×4), pernas 4×12×4. Altura total: 32.

| Peça | Tamanho | Observação |
|---|---|---|
| Espada comum | 16–20 de comprimento | igual às do jogo (cabe num bloco na diagonal) |
| Espada principal (por elemento) | 20–26 | mais longa, mas cabo sempre 4–5; lâmina larga no máx. 4 |
| Espada grande (duas mãos) | até 32 | nunca mais que a altura do jogador |
| Relíquia na mão | 8–12 | segurável com uma mão |
| Relíquia no corpo (Curios: cinto, pescoço) | 3–6 | não pode tampar o rosto nem a skin inteira |
| Armadura | 0,5–1,0 de folga (inflate) sobre a skin | capacete até 10×10×10; ombreira no máx. 2 para fora do braço |
| Enfeite de armadura (chifre, capuz, espinho) | no máx. 4 para fora do corpo | mais que isso parece hitbox errada e atravessa parede |
| Criatura pequena | 8–16 de altura | |
| Criatura humanoide | 24–40 | hitbox do código = tamanho do modelo |
| Chefe | até 64 (4 blocos) | maior que isso não cabe em caverna e trava em árvore; combinar a arena com a mesa Dev |

**Armadura com a Marca** (decisão de 07/11): a armadura tem um modelo base neutro (tecido escuro + metal) e uma
**camada da Marca** separada (textura extra ou ossos que ligam/desligam). Assim um modelo serve para todos os elementos.

**Espadas:** no inventário, sprite 2D (16×16) desenhado à parte; na mão, o modelo 3D. Em 1.21.1 o NeoForge faz isso com
o carregador `neoforge:separate_transforms`, sem GeckoLib. Só espadas com animação própria usam GeckoLib.

## 5. Limites anti-lag

Medido por **modelo**. Estourou? Pedir aprovação no PR explicando o porquê.

| Tipo | Ossos | Cubos | Textura (16x) | Animação |
|---|---|---|---|---|
| Item / bloco (modelo Java) | — | ≤ 24 | 16×16 ou 32×32 | — |
| Item GeckoLib (relíquia animada) | ≤ 12 | ≤ 40 | ≤ 64×64 | idle em loop curto |
| Armadura GeckoLib (conjunto todo) | os 8 da seção 6 + ≤ 16 extras | ≤ 80 | ≤ 64×64 por peça | sem animação parada |
| Criatura comum | ≤ 30 | ≤ 80 | ≤ 64×64 | ≤ 6 animações |
| Chefe | ≤ 60 | ≤ 200 | ≤ 128×128 | ≤ 12 animações |

Outras regras:
- **Cubo só onde se vê.** Nada de cubo escondido dentro de outro.
- **Rotação em ângulos simples** (22,5°, 45°…) em modelos Java: é o que o Minecraft aceita. Em GeckoLib pode qualquer ângulo.
- **Sem malha (mesh) livre do Blockbench:** o GeckoLib aceita, mas pesa e quebra o estilo de cubos.
- **Partículas e brilho por código**, com limite (a mesa Dev define), nunca muitos por tick.
- **Mob muito repetido** (spawn natural) fica perto do mínimo; chefe pode gastar mais porque aparece um por vez.

## 6. Convenções GeckoLib

**Arquivos** (dentro de `src/main/resources/assets/<modid>/`):
`geo/<tipo>/<nome>.geo.json`, `animations/<tipo>/<nome>.animation.json`, `textures/<tipo>/<nome>.png`
(e `<nome>_glowmask.png` se brilhar). `<tipo>` = `entity`, `item`, `armor` ou `block`. Nome em `snake_case`, sem acento,
igual ao ID em `docs/ids.md`. Fonte editável sempre em `arte/blockbench/<nome>.bbmodel`.

**Ossos** (`snake_case`, português, sem acento). Pivô no ponto em que a peça gira de verdade (ombro, quadril, pescoço).

| Osso | Para quê |
|---|---|
| `raiz` | osso pai de tudo, pivô em 0,0,0 (no chão, no meio) |
| `corpo` | tronco; filho de `raiz` |
| `cabeca` | o código gira esse osso para o mob olhar para o jogador (nome fixo, a mesa Dev usa) |
| `braco_dir`, `braco_esq`, `perna_dir`, `perna_esq` | membros. "dir" = direita **do mob** |
| `brilho_*` | partes que brilham (fica fácil esconder/mostrar) |
| `marca_*` | partes da armadura que mudam com a Marca |
| `efeito_*` | ponto vazio de onde saem partículas |

**Armadura GeckoLib**: os ossos de encaixe têm nome **fixo do GeckoLib** e não podem mudar:
`armorHead`, `armorBody`, `armorRightArm`, `armorLeftArm`, `armorRightLeg`, `armorLeftLeg`, `armorRightBoot`, `armorLeftBoot`.
Enfeites ficam **dentro** desses ossos.

**Animações** — nome `animation.<nome>.<ação>`:

| Ação | Loop | Duração | Observação |
|---|---|---|---|
| `idle` | sim | 2–4 s | respiração; quase parado |
| `walk` | sim | casa com a velocidade do mob | sem "deslizar" o pé |
| `attack` | não | 0,4–0,8 s | o golpe acerta no meio da animação (a mesa Dev sincroniza o dano) |
| `especial` | não | ≤ 2 s | habilidade do elemento; avisar a mesa Dev do tempo exato |
| `morte` | não | ≤ 1,5 s | opcional; o jogo já tem a animação de morte vermelha |

Item/relíquia: `idle` (pairar, girar devagar, pulsar o brilho) e, se houver, `usar`.

## 7. Fluxo de uma peça

1. Esboço no Blockbench em `arte/blockbench/<nome>.bbmodel` (com a textura embutida).
2. Prévia: `bash ferramentas/previa/previa.sh arte/blockbench/<nome>.bbmodel --saida arte/previas/<nome>.png`.
3. PR com o PNG da prévia → **Nikolas aprova o visual**.
4. Só então: exportar para `src/main/resources/assets/`, registrar o ID em `docs/ids.md`, avisar a mesa Dev.
