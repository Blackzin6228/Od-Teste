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

---

## 2. Nossas armas e armaduras x MekaSuit e o topo do ATM10

### 2.1 Uma conta que muda tudo: o teto de 80%
No vanilla 1.21, a armadura reduz o dano assim: `redução = limite(armadura − dano ÷ (2 + resistência ÷ 4), armadura ÷ 5, 20) ÷ 25`.
O resultado nunca passa de **20 ÷ 25 = 80%**, por mais pontos que a peça tenha. O pack tem o **AttributeFix**
(`attributefix`), que **tira o teto de 30 pontos** de armadura **(a confirmar o novo teto no config padrão)**, mas a
conta continua parando em 80%. Então, no ATM10:
- Allthemodium, Vibranium e Unobtainium **já ficam em 80%** em quase todo golpe. Mais pontos só ajudam em golpe enorme.
- A MekaSuit **com energia** passa disso: absorve **100%** antes da conta da armadura.
- A tabela 6.3 de `docs/progressao.md` (A5 = 30 de armadura / 16 de resistência) fica **abaixo do Unobtainium em pontos**.
  Não é problema: o que importa é a **absorção** e o **dano paranormal** (2.4).

### 2.2 Números de referência (lidos no código ou no config do pack)

**Armaduras (conjunto inteiro)**

| Conjunto | Armadura | Resistência | Recuo | Absorção extra | Fonte |
|---|---|---|---|---|---|
| Netherita (vanilla) | 20 | 12 | 0,4 | — | vanilla |
| **MekaSuit** sem energia | 20 (3/8/6/3) | 12 (3 por peça) | **(a confirmar)** | — | `pack/config/Mekanism/startup.toml` |
| **MekaSuit** com energia | igual | igual | igual | **100%** do dano que não ignora armadura; queda 100% (botas); magia 100% só com a Unidade de Purificação | `pack/config/Mekanism/gear.toml` |
| Custo da MekaSuit (pack) | — | — | — | **250.000 J por ponto de dano** (padrão 100.000); **40 MJ por peça** sem melhorias (padrão 16 MJ). Conjunto ≈ 160 MJ ≈ **640 pontos de dano** sem melhorias **(a confirmar como divide entre as peças; melhorias crescem ao quadrado)** | `gear.toml` + `kubejs/.../Mekanism/Rebalance/rebalance.md` |
| Allthemodium | 24 (4/9/7/4) | 20 (5 por peça) | 2,0 (teto 1,0) | anula queda, fogo, lava, afogamento, parede; tira Escuridão | `ArmorRegistries.java`, `ArmorEvents.java` |
| Vibranium | 32 (6/11/9/6) | 36 (9 por peça) | 3,2 | igual | idem |
| Unobtainium | 40 (8/13/11/8) | 60 (15 por peça) | 4,0 | igual | idem |
| Draconic Caótico | **(a confirmar)** | **(a confirmar)** | — | escudo por módulo: 100 pontos (pequeno) ou 500 (grande, 2x2); Não-Morrer Caótico até 3 instalados; voo criativo | `DEModules.java` |
| Jetpack de Unobtainium | +15 (peito) | 0 | 0 | voo | `pack/config/ironjetpacks/jetpacks/unobtainium.json` |

Exemplo de um golpe de **50** (chefe forte): netherita reduz **40%**, Unobtainium **80%**, MekaSuit com energia **100%**
(gasta 12,5 MJ). Golpe de **10**: netherita 72%, Unobtainium 80%, MekaSuit 100%.

**Armas (dano por golpe, sem encantamento)**

| Arma | Dano | Fonte |
|---|---|---|
| Rubra (nossa) | **7** (base de diamante) + ferida de 5 em área quando o acúmulo enche | `docs/decisoes/2026-11-08-sangue-rubra.md` |
| Espada de netherita | 8 | vanilla |
| Espada Dracônica / Caótica (base) | 12,25 / **17,5** (multiplicador do nível × 7) | `EquipCfg.java` |
| Cajado Caótico (base) | 22,5 (2,5 × 9) | `EquipCfg.java` |
| Módulos de dano do Draconic | +2 / +4 / +8 / +16 por módulo **(a confirmar como somam)** | `DEModules.java` |
| **Meka-Tool** (pack) | **24** (padrão 4), 8.000 J por golpe | `rebalance.md` |
| Espadas de Allthemodium / Vibranium / Unobtainium | **(a confirmar)** | — |

Dano nosso que **já passa** por MekaSuit e Allthemodium: `odteste:sangramento` (tag `minecraft:bypasses_armor`).

### 2.3 O que "mais forte que a Meka" quer dizer (proposta)
A armadura nossa ganha da MekaSuit em **três coisas** e empata no resto:
1. **Não depende de FE.** A Reserva enche sozinha (já decidido em `docs/progressao.md` 6.2).
2. **Segura dano paranormal.** A MekaSuit não segura o nosso (passa armadura). A nossa segura.
3. **Protege a mente.** Exposição e Lucidez (nenhum mod do pack faz isso).

Ela **não** deve ganhar em: absorver dano que ignora armadura **de outros mods** (senão os chefes do Cataclysm, Twilight
e Iron's ficam fáceis) e utilitário (voo, ímã, visão, que ficam nos Selos).

### 2.4 Faixa proposta para A4 e A5 (troca a tabela 6.3 de `docs/progressao.md` se o Nikolas aprovar)

| | MekaSuit (pack, sem melhorias) | **A4 Traje Transcendido** | **A5 Traje da Travessia** |
|---|---|---|---|
| Armadura / resistência | 20 / 12 | 24 / 20 (igual Allthemodium) | 30 / 20 |
| Escudo | 640 pontos (energia) | **Reserva 400 pontos** | **Reserva 800 pontos** |
| Recarga | carregador, 200.000 J/t | 4 pontos/s depois de **10 s sem levar dano** | 8 pontos/s depois de 6 s |
| Dano comum com escudo | 100% | 100% | 100% |
| Dano que ignora armadura (outros mods) | 0% | **15%** (era 35%) | **25%** (era 60%) |
| **Dano paranormal** (tag `odteste:paranormal`) | 0% **(depende do data map)** | **50%** | **70%** |
| Mente | nada | Exposição −50%, perda de Lucidez −40% | −70% / −60% |
| PvP | 100% | metade | metade |
| Nunca protege | `/kill`, vazio | `/kill`, vazio, evento do admin, Surto | igual |

**Condições paranormais (o que impede de ser só "mais forte"):**
- **Lucidez manda na recarga.** Firme 100% · Inquieto 75% · Abalado 40% · Quebrando 10% · **Surto 0%** (Reserva não enche).
  Quem vive no limite fica sem escudo justo quando mais precisa. Isso é o horror pagando a força.
- **Transcender mínimo** (2 para A4, 3 para A5), como já está proposto. Sem ele, a peça vale A2.
- **Gasto da Reserva pelos Selos** (voo, visão): quem usa utilitário tem menos escudo.

**Custo:** fica como em `docs/progressao.md` 6.5 (A5 consome a peça da MekaSuit no máximo). Em tempo de jogo, a A5
deve chegar **depois** da MekaSuit com melhorias e **perto** do Draconic Caótico **(estimar no playtest)**.

### 2.5 Armas (proposta)
Relíquias e espadas não têm receita (decisão de 07/11). Elas crescem com a Marca. Faixa sugerida:
- **Contra mob comum e jogador:** do **diamante (7)** no começo até no máximo **20** com a Marca no topo. Fica entre a
  espada Caótica (17,5) e a Meka-Tool (24), **sem passar** da Meka-Tool. A força da nossa arma não é número.
- **Contra criatura nossa:** **dano paranormal** (tipo de dano nosso com a tag `odteste:paranormal`), que vale **×2**.
- **Criaturas nossas resistem a dano comum:** comuns 0%, raras 30%, chefes 60% **(proposta)**. Assim a Meka-Tool mata o
  Retalhado fácil, mas a Sanguessuga e os chefes pedem arma paranormal, magia do elemento ou grupo. O técnico depende
  do mago, como `docs/progressao.md` quer.
- Tooltip continua **sem número** (decisão da Rubra).

---

## 3. Matéria paranormal nas máquinas: rendimento e limites

### 3.1 Como está hoje (código no `main`)
- Retalhado e Sanguessuga-Mãe: **0 a 1** Matéria bruta de Sangue (`loot_table/entities/*.json`), **+0 a 1 por nível de
  Pilhagem**, com **teto de 2** extras (`limit: 2`). Teto por morte: **3**. O teto segura a Pilhagem alta do Apotheosis.
- Só com jogador de verdade (`killed_by_player` + checagem de `FakePlayer` no código).
- Spawn: depois do 3º dia, longe do spawn (256), jogador com Exposição ≥ 20, **1 Sanguessuga por jogador Tocado por
  noite** e **1 por área de 48 blocos** (`RegrasCriaturas.java`).
- Máquinas (proposta de `docs/tech-paranormal.md`): **1 para 1**, no máximo 2 passos, sem chance extra.

### 3.2 Rendimento proposto por fonte

| Fonte | Matéria bruta | Por quê |
|---|---|---|
| Criatura comum (Retalhado) | 0 a 1 (média 0,5), Pilhagem até +2 | comum, aparece em grupo |
| Criatura rara (Sanguessuga-Mãe) | **1 garantida** + 0 a 1 (Pilhagem até +2) | rara e perigosa: tem de valer a caçada |
| Aparição de evento resolvido | 1 por jogador que ajudou, **1 vez por evento** | já em `docs/tech-paranormal.md` 5.2 |
| Chefe nosso | 4 a 6 + item único | uma vez por grupo por evento |
| Ritual (Occultism/nosso) | **nunca cria matéria**: só converte (ex.: 3 brutas → 1 Fragmento) | ritual não pode virar fonte |
| Máquina do pack | **1 → 1** (ou menos), sem saída extra por chance, sem bônus de melhoria | regra 4 de `docs/tech-paranormal.md` |

**Meta de ritmo:** um jogador ativo caçando à noite junta **6 a 10 brutas por hora real**. O kit tech sem a Câmara
(≈ 8 matérias) sai em **uma boa noite em grupo**. A Câmara pede uma tratada **de cada elemento**, então precisa de
troca entre jogadores de elementos diferentes (é de propósito).

### 3.3 Limite contra grind: "cansaço do elemento" (proposta)
Mesmo com os limites de spawn, um grupo pode acampar numa área escura. Para isso não virar fazenda manual:
- Cada jogador tem um contador **por dia de jogo** (20 min reais). Depois de **8 brutas** no dia, a chance de drop cai à
  **metade**. Depois de **16**, a **um quarto**. Zera ao amanhecer.
- Sem número na tela. O jogador só nota que "o outro lado ficou quieto" (frase da Lore).
- Dado salvo no jogador (vai para `docs/ids.md` se aprovado).

### 3.4 Lista de bloqueios (tudo **dado**, sem código pesado)

| Bloqueio | Contra | Como | Situação |
|---|---|---|---|
| Matéria só de quem **nasceu pelas nossas regras** (etiqueta positiva) | Duplicador do IF, Paradoxo do JDT, ovo, qualquer clone | trocar `sem_materia` por `pode_dar_materia` no spawn `NATURAL`/`EVENT` nosso | **novo**, mesa Dev |
| Condição de loot `odteste:jogador_de_verdade` | Drygmy (Ars Nouveau) e outros que montam loot com jogador falso | condição de loot nossa no lugar de `killed_by_player` | **novo**, mesa Dev |
| `c:capturing_not_supported` (tag NeoForge) | Ferramenta de Prender do IF, Frasco de Alma do Ender IO e outros que respeitam a tag | tag de entidade | **novo**, só dado **(a confirmar quais mods respeitam)** |
| `c:teleporting_not_supported` | levar criatura por portal/teleporte | tag de entidade | **novo**, só dado |
| Tags de bloqueio de cada mod (triturador do IF, Paradoxo e polimorfia do JDT, Drygmy, aceleradores) | idem | arquivo em `data/<mod>/tags/...` com `"required": false` nas entradas; se o mod sair do pack, o arquivo é só ignorado | **novo**, nomes **(a confirmar nos jars)** |
| Fora de `c:ores`, `c:dusts`, `c:raw_materials`, `c:ingots`, `c:gems` | multiplicação de minério (Mekanism 5x, Create, moedores, Occultism) | não pôr nas tags; o Almost Unified (`almostunified`) também não pega o que não tem tag | já decidido |
| Sem semente, abelha, árvore ou modelo de dados | Mystical Agriculture, Productive Bees, Productive Trees, Hostile Neural Networks | não criar; teste automático falha se aparecer | **novo** teste |
| Receita nunca devolve mais que entra | qualquer máquina | teste automático lendo os JSON de `data/odteste/recipe/` | **novo** teste |
| Tempo pelo relógio do mundo | Time in a Bottle, Varinha do Tempo | `gameTime` nos blocos nossos | **novo**, mesa Dev |
