# Progressão e crafts (PROPOSTA da mesa Integrador, 07/11/2026; espera o Nikolas)

> Só documento. Nenhuma receita existe ainda. Nomes dos **nossos** itens são provisórios (a mesa Lore dá o nome final)
> e só entram em `docs/ids.md` depois de aprovados. Números de quantidade são ponto de partida para balancear.
> Itens de outros mods foram tirados da lista `mods/` do pack `Blackzin6228/Conguitos-smp` (07/11/2026). Onde está
> escrito **(conferir)**, o nome exato do item ou o tipo de receita precisa ser lido no jar antes de virar receita.

## 1. Ideia em uma frase
Cada degrau pede **uma parte de magia e uma parte de tecnologia**. Quem só joga tech precisa de quem joga magia, e o
contrário também. No roleplay, isso vira troca, encomenda e favor entre os agentes.

## 2. Regras da progressão
1. **Cada receita puxa 2 mods ou mais**, com pelo menos **um de magia e um de tecnologia**.
2. **Duas portas na tecnologia** quando der: o mesmo passo aceita a máquina do Mekanism **ou** a do Modern Industrialization
   (ou do Create). Assim ninguém fica preso a um mod só.
3. **Relíquias e espadas principais não têm receita.** Só evento, chefe ou ritual narrado. O que tem receita são os
   **materiais para usar e melhorar** essas peças.
4. **Nada de atalho por máquina** (ver seção 9): nossos materiais não viram semente, não se duplicam e não saem de fazenda automática.
5. **Receita reserva:** toda receita que usa outro mod tem a condição `neoforge:mod_loaded`. Se o mod sair do pack, entra uma
   receita reserva, **mais cara**, feita só com o nosso mod e vanilla. O jogo nunca fica sem caminho.
6. **Receitas escondidas no JEI/EMI** (preferência do Nikolas desde o Máscaras). Quem descobre é o jogador, pelo Dossiê e por pistas.
7. **Sem lag:** usamos as máquinas e altares que já existem nos outros mods. Os nossos blocos de craft só trabalham quando
   alguém usa (nada rodando sozinho o tempo todo).

## 3. Os 5 degraus (como o ATM10 divide o jogo)

| Degrau | Nome provisório | Nível ATM10 equivalente | Magia usada | Tecnologia usada |
|---|---|---|---|---|
| D1 | **Recém-chegado** | começo (ferro, ouro, diamante) | Iron's (Essência Arcana), Occultism (giz branco, Otherstone) | Create (latão, mecanismo de precisão) |
| D2 | **Exposto** | meio (aço, ligas básicas) | Botânia (Manasteel, infusão de mana), Malum (Altar Espiritual, Soul Stained Steel), Vampirism (sangue) | Mekanism (Ligas Infundida/Reforçada, circuitos), MI (aço, circuito analógico/eletrônico), AE2 (processadores) |
| D3 | **Marcado** | fim do meio (Allthemodium, Terrasteel) | Botânia (Terrasteel, runas), Malum (Hallowed Gold, ritos), Occultism (Djinni/Afrit), Iron's (Orbes de Melhoria) | Allthemodium, Mekanism (Liga Atômica, circuito de Elite), Create (montagem em sequência), Oritech |
| D4 | **Transcendido** | fim de jogo (Vibranium, Draconium Desperto, Gaia) | Botânia (Espírito de Gaia), Occultism (Marid), Malum (metal maligno, conferir), Forbidden Arcanus (Forja de Hefesto) | Vibranium, Draconic (Núcleo Wyvern/Desperto), Mekanism (circuito Supremo, Polônio), MI (circuito digital), IF (gás Éter) |
| D5 | **Atravessado** | ATM Star | tudo acima + chefes | Unobtainium e ligas ATM, Draconic Caótico, **antimatéria** (SPS do Mekanism), AE2 MEGA, MI quântico, acelerador de partículas do Oritech |

## 4. Material base do nosso mod
- **Resíduo** (nome provisório): o que sobra quando uma criatura nossa morre. Cai **só com jogador de verdade matando** (sem `FakePlayer`).
- **Matéria de cada elemento** (nomes provisórios, a Lore escolhe): Sangue Coalhado (Sangue), Lodo Frio (Morte),
  Tinta Dourada (Conhecimento), Chama Líquida (Energia). Cai das criaturas do elemento e de lugares da dimensão.
- Esses dois são a "moeda" do mod. Tudo daqui para baixo come Resíduo e Matéria.

## 5. Componentes elementais (um por elemento, 4 níveis cada)

Cada elemento tem uma corrente: **Fragmento (D2) → Essência (D3) → Núcleo (D4) → Coração (D5)**.
Os nomes são provisórios. A tabela mostra **onde** cada nível é feito.

### 5.1 Que mod combina com cada elemento
| Elemento | Magia | Tecnologia | Por quê |
|---|---|---|---|
| **Sangue** | Vampirism (sangue, sangue puro), Malum (espíritos Infernal + Perverso), Botânia (Runas da Ira e da Gula), Iron's (Orbe de Sangue) | Mekanism (Câmara de Reação Pressurizada), Create (misturador aquecido), Ice and Fire (sangue de dragão, conferir) | Fome, carne, dor, metal enferrujado |
| **Morte** | Occultism (rituais com sacrifício, Marid), Malum (Sobrenatural + Terroso), Botânia (Runas do Inverno e da Preguiça) | Create (**Assombrar** com ventilador e fogo de alma), Mekanism (radiação, Centrífuga Isotópica), Extreme Reactors | Tempo, cinza, osso, decadência |
| **Conhecimento** | Iron's (Fragmento de Conhecimento Antigo, escola Eldritch), Occultism (giz dourado, Djinni), Botânia (Runas da Mana e do Orgulho) | **AE2** (gravador/Inscriber, processadores, células), MI (circuitos), Create (montagem em sequência) | Registro, memória, ordem, dados |
| **Energia** | Malum (Aéreo + Arcano), Iron's (Orbe de Raio), Botânia (Runas do Ar e da Luxúria) | Mekanism (SPS, Polônio, antimatéria), **Oritech** (acelerador de partículas), Powah (Orbe Energizante), Create New Age (energizador) | Caos, neon, eletricidade |

### 5.2 Corrente de cada nível
| Nível | Como se faz (exemplo para todos os elementos) |
|---|---|
| **Fragmento** (D2) | Matéria + Essência Arcana (Iron's) + Manasteel (Botânia) → **Infusão de Mana** num poço de mana. Depois, uma etapa tech: **Mekanism** (Infusor Metalúrgico com Liga Infundida) **ou** **MI** (montador com circuito analógico). |
| **Essência** (D3) | Fragmento + 8 Resíduos + 2 espíritos do Malum do elemento → **Altar Espiritual do Malum**. O resultado passa por **Create** (montagem em sequência com mecanismo de precisão e lingote de Allthemodium) para fechar. |
| **Núcleo** (D4) | 4 Essências + Terrasteel + runas do elemento (**Altar Rúnico da Botânia**) + Vibranium + circuito de Elite do Mekanism. Fechado num **ritual do Occultism** com o espírito forte do elemento (Afrit para Sangue e Energia, Marid para Morte e Conhecimento) **e um sacrifício** de criatura nossa. |
| **Coração** (D5) | 4 Núcleos + Espírito de Gaia (Botânia) + Núcleo Desperto (Draconic) + **antimatéria** (Mekanism) + item de chefe ligado ao elemento. Montado na **Fusão do Draconic** (injetores Caóticos). |

Item de chefe por elemento (exemplo): Sangue → Ignis (Cataclysm); Morte → Maledictus (Cataclysm) ou Lich (Twilight Forest);
Conhecimento → Harbinger (Cataclysm); Energia → Ender Guardian (Cataclysm) ou Ferrous Wroughtnaut (Mowzie's). **(conferir drops)**

## 6. Armaduras (sem elemento fixo; o visual muda com a Marca de quem veste)

A armadura **não** tenta ganhar da MekaSuit nem da armadura Caótica do Draconic. O papel dela é outro:
**proteger contra o paranormal** (segura a Exposição, devolve Lucidez, abre espaço para Marca). Assim quem quer as duas coisas
precisa escolher, e o nosso mod não quebra o equilíbrio do pack.

Cada nível **melhora a mesma peça** na Mesa de Ferraria com um molde (como netherita), então o jogador não perde a armadura nem os dados dela.

| Nível | Nome provisório | Pede | Onde |
|---|---|---|---|
| A1 | Roupa de Campo | couro + lã + Resíduo + latão do Create | mesa de craft |
| A2 | Traje Reforçado | A1 + Liga Reforçada (Mekanism) **ou** aço do MI + Manasteel + 1 Fragmento de qualquer elemento | Mesa de Ferraria + molde (o molde sai do Altar Espiritual do Malum) |
| A3 | Traje Marcado | A2 + placas de Allthemodium + Terrasteel + Soul Stained Steel + 2 Essências de elementos diferentes | molde feito em ritual do Occultism (giz dourado) |
| A4 | Traje Transcendido | A3 + Vibranium + Liga Atômica + Espírito de Gaia + Núcleo Wyvern (Draconic) + 2 Núcleos | molde forjado na Forja de Hefesto (Forbidden Arcanus) |
| A5 | Traje da Travessia | A4 + liga Unobtainium-Vibranium + Núcleo Caótico (Draconic) + 4 Corações (um de cada) | Fusão do Draconic + selado no nosso ritual |

## 7. Espadas
- **Espadas principais** (uma por elemento): **sem receita**. Chegam por evento, chefe ou ritual. Crescem com a Marca do dono.
- **Materiais para melhorar as principais** (têm receita):
  - **Têmpera do elemento** (D3): Essência + Lingote de Allthemodium + Orbe de Melhoria do Iron's da escola parecida. Sobe um degrau da espada.
  - **Têmpera maior** (D4 e D5): Núcleo/Coração + Vibranium/Unobtainium + Hallowed Gold do Malum. Feita na **Bigorna Arcana** do Iron's ou no Altar Espiritual.
- **Lâminas de agente** (espadas comuns, com receita, para quem não tem uma principal):

| Nível | Pede | Onde |
|---|---|---|
| L1 | ferro + Resíduo + latão | mesa de craft |
| L2 | Soul Stained Steel (Malum) + Liga Infundida (Mekanism) + Fragmento | Altar Espiritual |
| L3 | Allthemodium + Terrasteel + Essência | Infusão de Mana e depois montagem em sequência do Create |
| L4 | Vibranium + Draconium Desperto + Núcleo | Fusão do Draconic (injetores Wyvern) |

## 8. Item final: "Âmago da Travessia" (nome provisório, nível ATM Star)

Para fazer, o servidor inteiro precisa estar no fim de jogo. Ideia de receita (em etapas):
1. **4 Corações** (um por elemento).
2. **Bloco de cada liga ATM** (Unobtainium-Allthemodium, Unobtainium-Vibranium, Vibranium-Allthemodium).
3. **Núcleo de Energia Caótico** do Draconic (que já pede Fragmentos de Caos e um reator).
4. **64 Pelotas de Antimatéria** do Mekanism (SPS; centenas de horas de máquina ou muita energia).
5. **Célula MEGA** do AE2 (conferir) e **circuito quântico** do MI.
6. **Item do acelerador de partículas** do Oritech (conferir) e **gás Éter** do Industrial Foregoing (precisa do Wither).
7. **Essência de Insanium** (Mystical Agradditions) e o maior tier do Mystical Extended Tier (conferir).
8. **9 Espíritos de Gaia**, **sangue puro** do Vampirism de nível alto e um item de **cada chefe principal** (Cataclysm, Twilight Forest, Mowzie's, Iron's).
9. Montagem: **Fusão do Draconic** com injetores Caóticos → depois **Forja de Hefesto** (Forbidden Arcanus) → por último o **nosso ritual**,
   no círculo de giz do Occultism, **só numa noite de evento** e com os 4 elementos representados por jogadores diferentes.

O que ele faz é decisão do Nikolas e da Lore (ex.: abre a passagem de volta, ou faz parte do Medo). Fica marcado como pendente.

## 9. Proteção contra atalhos (para nada ficar fácil por causa de outro mod)
| Risco | Mod | O que fazer |
|---|---|---|
| Semente que gera nosso material | Mystical Agriculture | não criar semente; tag de bloqueio, se o mod tiver |
| Fazenda de criatura com máquina | Industrial Foregoing (Triturador), Mob Grinding Utils, Apotheosis (geradores), Hostile Neural Networks | drops só com jogador de verdade; nossas criaturas fora da lista de geradores e sem modelo de dados |
| Duplicar item | Create (Deployer), AE2, Productive Bees | nada de abelha nossa; receitas sem saída maior que a entrada |
| Teletransporte de item de chefe | — | itens de chefe ficam ligados ao jogador que matou (conferir viabilidade) |
| Receita escondida mas copiável | EMI/JEI | esconder as nossas; a pista vem pelo Dossiê |

## 10. Onde entra cada sistema (resumo)
- **Rituais do Occultism:** fechar o Núcleo (D4), molde da A3, última etapa do item final. Feitos por **dados** (`data/`), com giz e sacrifício.
- **Altar Espiritual do Malum:** Essência (D3), molde da A2, Lâmina L2, Têmpera.
- **Infusão da Botânia:** Fragmento (D2) no poço de mana; runas no Núcleo (D4); Terrasteel e Gaia em vários níveis.
- **Mekanism:** ligas e circuitos em todos os degraus; radiação para Morte; SPS e antimatéria para Energia e para o item final.
- **Create:** latão e mecanismo no começo; Assombrar para Morte; montagem em sequência para fechar a Essência e a L3.
- **Draconic:** Fusão no D4 e D5; Núcleo Caótico no item final.
- **AE2, MI, Oritech, IF, Mystical Agriculture, Forbidden Arcanus:** pedaços do D4 e D5 (ver tabela de `docs/integracoes.md`).

## 11. Precisa do Nikolas
- Aprovar os **5 degraus** e se cada receita deve pedir **magia e tech juntos** (ou aceitar só um dos dois com custo maior).
- Nomes dos materiais (Resíduo, Matérias, Fragmento/Essência/Núcleo/Coração) → mesa Lore.
- O que o **item final** faz e se ele é ligado ao **Medo** (pendente em `docs/decisoes.md`).
- Se a armadura fica "contra o paranormal" (sem ganhar da MekaSuit) ou se deve ser a melhor do pack.
- Mapa de chefe por elemento.
