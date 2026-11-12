# Estudo: balanceamento tech x paranormal (mesa Integrador, 12/11/2026, para a versão 0.2)

> **Só documento.** Nada aqui mexe no jogo. Pedido do Nikolas em 11/11/2026.
> Base: `docs/progressao.md` (degraus e armaduras), `docs/tech-paranormal.md` (matéria nas máquinas e eventos na base),
> `docs/horror.md` (Lucidez e Exposição) e o código atual (`criaturas/`, `itens/`).
>
> **Lista de mods:** pasta `pack/mods` do repositório privado `Blackzin6228/Conguitos-ATM10` (537 arquivos, lida em
> 12/11/2026). Só entram aqui mods que **estão** nessa pasta. Configs lidas em `pack/config/` e `pack/defaultconfigs/`
> do mesmo repositório. Onde o número não foi lido no código ou no config, está marcado **(a confirmar)**.

## 0. Resumo em 5 linhas
1. O que mais estraga o horror no ATM10 não é a armadura: é **Mega Torch** (bloqueia nossos spawns naturais em 64 blocos),
   **luz em massa** (sobe a Lucidez de graça) e **fazenda/captura de criatura** (vira grind).
2. Nossas criaturas já escapam de muita coisa (drop só com jogador de verdade; spawner não dá matéria). Faltam **tags de
   bloqueio** e trocar a regra de matéria para "**só ganha quem nasceu pelas nossas regras**".
3. No ATM10 a armadura de pontos (Allthemodium, Unobtainium) já chega no **teto de 80% de redução** do vanilla. A MekaSuit
   com energia passa disso (100%). Ser "mais forte que Meka" tem de vir de **como absorve** (Reserva, dano paranormal),
   não de mais pontos.
4. Eventos de Energia **nunca** podem mexer em reatores (Draconic e Mekanism explodem). Na 0.2, só nossos aparelhos e
   efeitos de tela; dreno de baterias de outros mods fica para depois, com lista permitida.
5. Dez decisões curtas para o Nikolas no fim (seção 5).

---

## 1. O que no ATM10 anula o horror

Como ler: cada linha diz **o que o mod faz contra o nosso mod**, as opções e a **recomendação** (em negrito).
Opções sempre do mesmo tipo: (a) **ignorar** a proteção, (b) **tornar caro** de manter, (c) **virar parte da história**.

### 1.1 Proteção de spawn

| Mod (no pack) | O que faz contra nós | Opções | Recomendação |
|---|---|---|---|
| **Torchmaster** (`torchmaster`) — Mega Torch | Bloqueia spawn **natural** de todo mob que não é "amigável" num **cubo de 64 blocos** para cada lado (config padrão `megaTorchRadius = 64`; o pack não muda). Nossas criaturas são `MobCategory.MONSTER` e nascem por `biome_modifier` (spawn `NATURAL`), então **a Mega Torch apaga as duas** (Retalhado e Sanguessuga-Mãe) na base inteira. **Não bloqueia** spawn `EVENT`, `TRIGGERED`, `COMMAND`, ovo, nem `MOB_SUMMONED` (código `TorchmasterEventHandler.isIntentionalSpawn`). | (a) nossas aparições na base nascem como `EVENT` (passa pela tocha); (b) a tocha funciona, mas "segura" o Medo: cada noite com tocha acesa sobe um contador da área e, cheio, vem um evento maior; (c) a Mega Torch vira "luz que o outro lado nota": funciona contra mob comum, e contra o nosso só até a Exposição do dono passar de Média | **(a) + (c).** Spawn natural continua respeitando a tocha (justo com quem constrói). Tudo que é **história** (eventos, aparições, perseguição) nasce como `EVENT` e ignora a tocha. A partir de Exposição Alta, a tocha deixa de proteger o próprio dono (nossa regra, sem mexer no Torchmaster). |
| **Torchmaster** — Dread Lamp e Feral Flare Lantern | Dread Lamp bloqueia **animais** (não nos afeta). Feral Flare põe luz invisível sozinha num raio de **16 blocos** até luz 10 (config padrão) | ver 1.5 | ver 1.5 |
| **In Control!** (`in-control`) | Regras de spawn do servidor. O pack só limita mobs vanilla em quantidade (`defaultconfigs/incontrol/spawn.json`, `mincount 15`). **Não nos atinge hoje.** | É ferramenta do admin, não ameaça | **Aliado.** Se precisar, o admin limita nossas criaturas por regra, sem código. |
| **Nature's Aura** (`natures-aura`) | Tem bloco que impede spawn de monstros numa área **(a confirmar no jar o nome e o raio)** | (a) passa por cima com `EVENT`; (b) gasta Aura | **(a)**, igual à Mega Torch. |
| **RFTools Utility** (`rftools-utility`) | Controlador Ambiental com módulo **Pacífico** (sem monstros na área) **(a confirmar no jar 1.21)** | (a) `EVENT` passa; (b) é caro em RF | **(a).** |
| **MFFS** (`mffs`) | Matriz de Interdição: **mata ou expulsa** mobs hostis que entram no campo de força **(a confirmar módulo e dano)** | (a) nossas criaturas com tag que ela não reconheça; (b) a matriz gasta muito mais energia contra o nosso; (c) vira alvo do evento "Fome da Linha" | **(b) + (c)**, sem tornar imune: contra criatura nossa, o campo **funciona**, mas a criatura é "anotada" e a próxima vez vem um evento na base. Precisa ler o jar para ver se dá para mexer só por tag. |
| **Enhanced Celestials** (`enhanced-celestials-2-*`) | Luas de sangue e eventos lunares | — | **Aliado**: noite de lua de sangue pode subir a chance das nossas aparições (só dados, se a API deixar) **(a confirmar)**. |

### 1.2 Visão noturna, voo e imunidade

| Mod (no pack) | O que faz contra nós | Opções | Recomendação |
|---|---|---|---|
| **Mekanism** — MekaSuit | Unidade de visão (visão noturna, 500 J/t), voo por Modulação Gravitacional, **reduz efeitos de poção ruins** (80.000 J por uso, config do pack), **absorve 100%** do dano que não ignora armadura (ver seção 2). | (a) nossos efeitos não são poção vanilla; (b) contra dano paranormal ela absorve menos; (c) a roupa "chia" perto do paranormal | **(a) + (b).** Cegueira, Escuridão e Lentidão que usamos no horror viram **efeitos nossos** sem cura (NeoForge `EffectCure` vazio) **(a confirmar se a MekaSuit respeita)**. Nosso tipo de dano entra no data map `mekanism:mekasuit_absorption` com valor menor (ex.: 0,5). Isso é dado, não código. |
| **Allthemodium** (`allthemodium`) | O capacete de Allthemodium/Vibranium/Unobtainium **tira o efeito Escuridão** todo tick (`Allthemodium_Helmet.inventoryTick`). As botas cancelam dano de queda. O conjunto anula fogo, lava, afogamento, bater na parede, parte da magia e do vazio (`ArmorEvents.java`). | (a) não usar Escuridão vanilla; (b) usar tipo de dano próprio | **(a) + (b).** Nunca depender de `minecraft:darkness` nem de `minecraft:magic`. O nosso `odteste:sangramento` já é dano próprio (passa pela armadura, tag `bypasses_armor`). |
| **Draconic Evolution** (`draconic-evolution`) | Escudo por módulos (Caótico: 100 pontos por módulo, 500 no grande), voo criativo (Dracônico e Caótico), **módulo Não-Morrer** (evita a morte, com recarga) — `DEModules.java`. | (a) nosso dano paranormal ignora parte do escudo; (b) o Não-Morrer não funciona contra o Surto; (c) nada | **(b).** O Surto e o dano de evento narrado não matam, então não há o que salvar. Contra criatura nossa, o escudo funciona normal. Não vale a briga de código com o Draconic. |
| **Just Dire Things** (`just-dire-things`) | Habilidades **todas ligadas** no pack (`justdirethings-server.toml`): voo, visão noturna, **invulnerabilidade**, **proteção contra morte**, **removedor de efeitos ruins**, **"sem IA"** (congela o mob) e **polimorfia** (troca o mob por outro). | (a) bloquear por tag as que mexem no mob; (b) aceitar as que mexem no jogador | **(a)** para "sem IA" e polimorfia (criatura nossa não pode virar porco nem estátua) **(a confirmar a tag no jar)**. **(b)** para voo, visão e invulnerabilidade curta: são caras e já têm recarga. |
| **Iron Jetpacks** (`iron-jetpacks`) | Voo até o Unobtainium (tier 8, 15 de armadura, `config/ironjetpacks/jetpacks/unobtainium.json`) | (a) ignorar; (c) nas nossas dimensões o jato falha | **(a)** no mundo normal; **(c)** só dentro das dimensões e eventos. Fugir voando de uma criatura é parte do jogo. |
| **Botânia** (Tiara de Flügel), **Ars Nouveau**, **Ad Astra** (traje), **Paraglider** | Voo ou planar | igual acima | **(a)** no mundo; **(c)** nas dimensões. |
| **Artifacts / Reliquified Artifacts**, **Relics**, **Enigmatic Legacy+**, **Reliquary** | Anéis e amuletos: visão noturna, imunidades, vida extra **(a confirmar a lista de itens no 1.21)** | (a) nossos efeitos próprios não são curados por eles | **(a).** |
| **Vampirism** (`vampirism`) | Jogador vampiro tem **visão noturna** de nível | (c) vampiro "vê demais": o horror dele é outro (sol, sangue) | **(c)**, combinar com a Lore: vampiro ganha Exposição mais rápido perto de Sangue. Não tirar a visão dele. |
| **Tombstone**, **PlayerRevive**, **Baubley Heart Canisters** | Túmulo com itens, reviver caído, mais corações | — | **Ignorar.** Morrer no nosso mod já não é o castigo principal (é a Lucidez). |
| **Iris + shaders**, **Sodium Extra** | Shader e opções de cliente; o Sodium Extra pode **desligar a neblina** **(a confirmar)**; o brilho (gamma) é do próprio jogador | (a) nossa neblina de evento é **partícula/overlay nosso**, não a neblina do vanilla | **(a).** O horror não pode depender de uma opção que o jogador desliga no menu. |

### 1.3 Fazendas, matadouros e captura de criatura

| Mod (no pack) | O que faz contra nós | Já estamos protegidos? | Recomendação |
|---|---|---|---|
| **Mob Grinding Utils** (`mob-grinding-utils`) | Espetos, Moedor (Mob Masher), ventilador, terra que faz nascer mob | **Sim para o drop:** matam com `FakePlayer` e o nosso código não dá matéria (`CriaturaDeSangue`, linhas 106 e 122) | Manter. Conferir se a "terra" faz nascer como `NATURAL` dentro de área escura **(a confirmar)**: se sim, ela é só mais um lugar de spawn (com os nossos limites). |
| **Industrial Foregoing** — Triturador de Mobs | Mata com `FakePlayer` (cooldown 2,5 s, config do pack) | **Sim** (mesmo motivo) | Manter. Pôr nossas criaturas na lista de bloqueio do triturador, se for tag **(a confirmar)**. |
| **Industrial Foregoing** — **Duplicador de Mobs** + Ferramenta de Prender Mob | **Copia** a criatura com Essência (`essenceNeeded = 12` × vida, `exactCopy = false`, config do pack) | **Não.** A cópia não nasce como `SPAWNER`, então não ganha a etiqueta "sem matéria". Um jogador de verdade matando cópias **ganharia matéria** | **Trocar a regra:** em vez de marcar quem **não** dá matéria, marcar quem **dá** (etiqueta positiva só em spawn `NATURAL` com as nossas regras e em `EVENT` nosso). Mais a tag `c:capturing_not_supported` (NeoForge) para a ferramenta não pegar a criatura **(a confirmar se o IF respeita)**. |
| **Just Dire Things** — Máquina do Paradoxo | **Clona** blocos e entidades (config do pack: `paradox_restricted_mobs = true`, tem tag de bloqueio) | **Não** | Pôr nossas criaturas na tag de bloqueio do Paradoxo **(a confirmar nome da tag)** + regra da etiqueta positiva. |
| **Hostile Neural Networks** (`hostile-neural-networks`) | Simula mob e gera drop sem mob nenhum, **só se existir um "modelo de dados"** para ele (o pack cria os seus por KubeJS) | **Sim**, enquanto ninguém criar modelo para as nossas | Não criar modelo. Teste na `ferramentas/`: falha se aparecer modelo `odteste:` no pack. |
| **Apotheosis** (spawners), **Ender IO** (Frasco de Alma, spawner com energia) **(a confirmar no 1.21)**, **Draconic** (spawner estabilizado) **(a confirmar no 1.21)** | Levar a criatura para um spawner | **Sim** para spawner (`SPAWNER` → sem matéria). **Não** para o Frasco de Alma (carrega a criatura viva) | `c:capturing_not_supported` nas nossas criaturas. Ovos já escondidos (só `/give`). |
| **Ars Nouveau** — Drygmy | Gera o loot de mobs próximos **sem matar** **(a confirmar se monta o contexto com jogador falso)** | **Talvez não:** nosso drop está na tabela de loot com `killed_by_player`, e esse teste aceita jogador falso | Mover a regra para uma **condição de loot nossa** (`odteste:jogador_de_verdade`) ou pôr na tag de bloqueio do Drygmy **(a confirmar a tag)**. |
| **SecurityCraft** (sentinela), **Immersive Engineering** (torretas), **PneumaticCraft** (torreta sentinela) | Defesa automática da base: mata a criatura antes do jogador ver | Drop: sim (não é jogador) | **(c)** É legítimo defender a base. Mas criatura **de evento** é "aparição": a torreta erra (o golpe passa) **(precisa de código; deixar para depois da 0.2)**. |
| **Mekanism** | Não tem matadouro nem fazenda de mob. O risco do Mekanism é **multiplicar minério** (5x) | Já coberto: nossas matérias fora de `c:ores`, `c:dusts` etc. (`docs/tech-paranormal.md`, regra 4) | Manter. |
| **Time in a Bottle**, **Just Dire Things** (Varinha do Tempo), **Accelerated Decay** | **Aceleram** blocos: o Registro de Contenção "a cada 5 min" e qualquer recarga em bloco | **Não** | Contar o tempo pelo **relógio do mundo** (`gameTime`), não por ticks do bloco, e pôr os nossos blocos na tag de "não acelerável" de cada mod **(a confirmar nomes das tags)**. |

### 1.4 Teleporte e chunk loaders

| Mod (no pack) | O que faz contra nós | Opções | Recomendação |
|---|---|---|---|
| **Waystones** | Teleporte entre pedras. Config do pack: **leva bichos na guia** (`transportLeashed = "ENABLED"`), só nega o Wither | (a) nossas criaturas na lista `entityDenyList` ou não aceitam guia | **Não aceitar guia** (código nosso, pequeno) e pedir ao Publicador para pôr nossas criaturas no `entityDenyList` do pack. |
| **FTB Essentials** (`/home`, `/back`, `/tpa`), **Tempad**, **Mekanism** (teleportador), **Draconic** (deslocador), **Just Dire Things** (pistola de portal, "voidshift") | Fugir de uma perseguição ou de uma dimensão nossa num clique | (a) liberar no mundo; (b) bloquear dentro das nossas dimensões e durante evento de perseguição | **(b)**: `EntityTeleportEvent` cancelado **só** dentro da dimensão nossa e durante evento marcado. No mundo normal, liberado. Avisar a frase "algo segura você aqui". |
| **Compact Machines** | Base dentro de uma "sala de bolso": evento de Energia não alcança | (a) aceitar; (c) a sala de bolso "não tem sinal" | **(a) + (c).** É um bom esconderijo e cabe na história: o Analisador e a Antena não funcionam lá dentro. |
| **FTB Chunks** (carregar chunk do time), **Mekanism** (Upgrade Âncora) | Chunk carregado sem ninguém perto | Nossa regra já é "nada acontece sem jogador perto" | **Manter a regra.** Nenhuma lógica nossa olha só "chunk carregado": sempre exige jogador online a até X blocos. |
| **Ender Storage**, **DimStorage**, **Entangled** | Mandar item de chefe ou matéria para outro lugar | — | **Ignorar.** Matéria pode circular entre jogadores; isso é a troca que a progressão quer. |

### 1.5 Iluminação em massa

| Mod (no pack) | O que faz contra nós | Opções | Recomendação |
|---|---|---|---|
| **Torchmaster** Feral Flare, **Mining Gadgets**, **Building Gadgets**, lâmpadas de **Additional Lights**, **Macaw's Lights**, **Simply Light**, **XyCraft**, **Luminax** | Base e caverna 100% iluminadas. Isso corta nosso spawn natural (`LUZ_FORTE = 12`) e **sobe a Lucidez** (luz é uma das formas de recuperar) | (a) aceitar no spawn; (b) luz recupera Lucidez só até um teto; (c) o evento apaga a luz **na tela** | **(b) + (c).** Luz forte **segura** a Lucidez (não deixa cair) mas **não sobe** acima de Inquieto (70%) sozinha: para subir, dormir, grupo e comida quente. No Apagão, a luz pisca **só no cliente** (já previsto). |
