# Progressão e crafts (PROPOSTA da mesa Integrador, 07/11/2026; espera o Nikolas)

> **Revisão 07/11/2026 (decisões do PR #9):** espadas e relíquias saíram do craft; armaduras refeitas para ficarem
> acima da MekaSuit. Magias ficam em `docs/magias.md`.

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
3. **Relíquias, entidades e espadas principais não têm receita** (decisão do Nikolas, 07/11/2026). Só evento, chefe ou
   ritual narrado. **Nenhum material** deste documento cria ou melhora espada ou relíquia. Os materiais servem para
   **armaduras, componentes e itens de suporte**.
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

> **Decisão do Nikolas (07/11/2026):** a armadura é **muito forte**, boa contra o paranormal **e** contra o resto,
> provavelmente **mais forte que a MekaSuit**. Esta seção foi refeita para isso.

### 6.1 Contra o que estamos medindo: a MekaSuit do pack
Lido no `config/Mekanism/gear.toml` do Conguitos-smp (Mekanism 10.7.19, valores padrão):
- **Com energia e o conjunto inteiro**, a MekaSuit absorve **100% do dano que não ignora armadura**. Cada ponto de dano custa **100.000 J**.
  Cada peça guarda **16 MJ** sem melhorias (cresce muito com melhorias). Queda: 100% (botas). Magia: 100%, mas **só com a Unidade de Purificação** no capacete.
- **Sem energia**, cada peça vira uma peça de **netherita**.
- **Pontos fracos:** (1) **dano que ignora armadura** passa inteiro (vazio, `/kill`, fome, e o "dano verdadeiro" de muitos mods e chefes);
  (2) **depende de carregar**: luta longa contra chefe esvazia; (3) **nenhuma proteção paranormal** (Exposição, Lucidez, Marca).

É nesses três pontos que a nossa ganha. Nos utilitários (voo, jato, visão noturna, ímã) ela **não tenta** ganhar: isso fica para os encaixes (6.4).

### 6.2 Como a nossa funciona
- **Reserva** (nome provisório): um escudo próprio da armadura. **Não usa FE**: enche sozinho com o tempo e mais rápido quando o dono tem
  Lucidez alta. Enquanto tem Reserva, a armadura absorve o dano **como a MekaSuit**, mas também **uma parte do dano que ignora armadura**.
- **Nunca protege de:** `/kill`, cair no vazio e o dano narrado pelo admin (eventos). Assim ninguém fica imortal e o narrador continua mandando.
- **Sem Reserva**, a peça não vira netherita: fica com os números de base do nível (A4 e A5 já acima da netherita).
- **Contra o paranormal:** segura a Exposição, reduz a perda de Lucidez e protege de efeitos das nossas criaturas e dimensões.
- **Ela escolhe o dono:** cada nível pede **nível de Transcender** (ver tabela). Quem veste sem o nível usa a peça como A2. Isso segura o poder
  e liga a armadura ao sistema do mod.
- **PvP:** contra outros jogadores a absorção pela Reserva cai pela metade (o PvP não vira empate eterno). Número para ajustar no playtest.
- Cada nível **melhora a mesma peça** na Mesa de Ferraria com um molde (como netherita). Ninguém perde a peça nem os dados dela.

### 6.3 Níveis
Números para começar a balancear (conjunto inteiro). Teto do jogo: armadura 30, resistência 20.

| Nível | Nome provisório | Armadura / Resistência | Reserva | Absorção com Reserva | Dano que ignora armadura | Paranormal | Transcender | Comparação |
|---|---|---|---|---|---|---|---|---|
| A1 | Roupa de Campo | 10 / 0 | — | — | 0% | Exposição −10% | 0 | couro reforçado |
| A2 | Traje Reforçado | 18 / 2 | pequena | 50% | 0% | Exposição −20%, Lucidez −10% de perda | 0 | perto do ferro/diamante |
| A3 | Traje Marcado | 22 / 6 | média | 80% | 15% | −35% / −25% | 1 | acima da netherita |
| A4 | Traje Transcendido | 26 / 12 | grande | **100%** | 35% | −50% / −40%, imune ao efeito de 1 criatura por elemento | 2 | **igual ou acima da MekaSuit cheia** |
| A5 | Traje da Travessia | 30 / 16 | enorme, enche rápido | **100%**, inclui magia sem módulo | **60%** | −70% / −60%, imune aos efeitos das dimensões | 3 | **acima da MekaSuit** (abaixo do Caótico só em utilitário) |

Resistência a recuo: A3 0,1 · A4 0,3 · A5 0,5.

### 6.4 Encaixes (para não perder para a MekaSuit em utilitário)
A partir do A3, cada peça tem **1 a 2 encaixes** para **Selos de Traje** (itens de suporte, seção 7): por exemplo voo curto, visão no escuro,
passo silencioso, pulmão (água). Cada Selo gasta **Reserva** ao usar, não FE. Assim quem quer voo forte ainda pensa em trocar por um Selo de defesa.

### 6.5 Custo (precisa estar à altura: D4 e D5)
Valores **por peça** (vezes 4 no conjunto). Itens de outros mods com receita reserva se o mod sair (regra 5 da seção 2).

| Nível | Pede (por peça) | Onde |
|---|---|---|
| A1 | couro + lã + Resíduo + latão do Create | mesa de craft |
| A2 | A1 + Liga Reforçada (Mekanism) **ou** aço do MI + Manasteel + 1 Fragmento de qualquer elemento | Mesa de Ferraria + molde (o molde sai do Altar Espiritual do Malum) |
| A3 | A2 + 4 placas de Allthemodium + 2 Terrasteel + Soul Stained Steel + 2 Essências de elementos diferentes | molde feito em ritual do Occultism (giz dourado) |
| A4 | A3 + 4 blocos de Vibranium + 16 Ligas Atômicas + 8 circuitos Supremos + 32 pelotas de Polônio + 2 Espíritos de Gaia + Núcleo Desperto (Draconic) + circuito digital (MI) + **2 Núcleos** | **Fusão do Draconic** (injetores Despertos), depois o molde é selado na **Forja de Hefesto** (Forbidden Arcanus) |
| A5 | A4 + **a peça igual da MekaSuit** com as melhorias no máximo + 2 blocos de liga Unobtainium-Vibranium + Núcleo de Energia Caótico + 16 pelotas de **antimatéria** + 4 Corações (um de cada elemento) + item do chefe da dimensão do dono | Fusão do Draconic (injetores Caóticos) + **ritual nosso**, com o dono dentro do círculo |

- **Por que A5 come a MekaSuit:** fica claro no jogo que ela é "a MekaSuit levada para o paranormal". Também puxa quem joga magia a depender
  de quem joga tech. Se o Mekanism sair do pack, a receita reserva troca a MekaSuit por **mais 2 Corações**.
- O conjunto A5 inteiro custa, em ordem de grandeza, **mais que o Âmago** sem os chefes. Isso é de propósito: é o objetivo de fim de jogo de cada jogador.

### 6.6 Visual: muda com a Marca de quem veste
A armadura não tem elemento. O modelo é um só, e o que muda é cor, detalhe e brilho. **Precisa de prévia do Modelador e aprovação do Nikolas.**

| Marca de quem veste | Ideia de visual (proposta) |
|---|---|
| Sem Marca | cinza de campo, tecido e placas lisas, sem brilho |
| Sangue | placas vermelho-ferrugem, bordas com espinhos curtos, costuras que parecem pulsar |
| Morte | preto fosco, poeira e cinza que caem das placas, linhas em espiral anti-horária gastas |
| Conhecimento | branco e dourado, faixas com escrita própria (nossa, não sigilos da obra), fecho de corrente |
| Energia | placas escuras com linhas neon que trocam de cor, faíscas nas juntas |

- **Nível do Transcender** aumenta o detalhe (A3 mostra a cor, A4 o detalhe, A5 o brilho e partículas leves).
- Troca **na hora** quando a Marca muda. O visual vem dos dados já sincronizados; não é um item novo, então não gasta ID.
- **Regra 9:** a armadura dá vantagem, então **não é vendida**. Só **variações de visual** (cosmético) poderiam um dia ser vendidas.

## 7. Espadas, relíquias e o que os materiais fazem
- **Espadas principais, relíquias e entidades:** **sem receita e sem material de melhoria**. Chegam por evento, chefe ou ritual narrado e
  crescem com a **Marca** do dono e com eventos. Nada desta página entra no caminho delas.
- **Para onde vão os materiais de progressão** (Fragmento, Essência, Núcleo, Coração):
  1. **Armaduras** (seção 6).
  2. **Componentes** para outros mods (ex.: Núcleo de Energia como peça de máquina, catalisador de ritual do Occultism).
  3. **Itens de suporte** (nomes provisórios, a Lore escolhe):

| Item | O que faz | Degrau | Pede (exemplo) |
|---|---|---|---|
| Lanterna de Luz Negra | mostra pistas e escrita escondida | D1 | latão (Create) + Essência Arcana (Iron's) + Resíduo |
| Vela de Vigília | segura a Lucidez de quem está perto | D2 | cera + Manasteel (Botânia) + Fragmento |
| Selo de Giz | área pequena onde nossas criaturas não nascem | D2 | giz branco (Occultism) + Liga Infundida (Mekanism) |
| Kit de Campo | devolve Lucidez, cura Exposição recente (não a permanente) | D3 | Essência + sangue (Vampirism) + Terrasteel |
| Selos de Traje | encaixes da armadura (voo curto, visão, silêncio, água) | D3 a D5 | Essência/Núcleo + peça tech (circuito de Elite, Polônio, antimatéria) |
| Âncora de Retorno | volta para o Círculo de base depois de uma dimensão | D4 | Núcleo + Pérola do Ender + componente do AE2 (conferir) |
| Foco do Elemento | foco das magias novas (ver `docs/magias.md`) | D2 a D4 | Fragmento/Essência + orbe ou item da escola do Iron's |

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

O Âmago **não é relíquia nem espada** (essas não têm receita). Se a Lore decidir que ele vira relíquia, ele sai desta página.
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
- **Altar Espiritual do Malum:** Essência (D3), molde da A2.
- **Infusão da Botânia:** Fragmento (D2) no poço de mana; runas no Núcleo (D4); Terrasteel e Gaia em vários níveis.
- **Mekanism:** ligas e circuitos em todos os degraus; radiação para Morte; SPS e antimatéria para Energia, para a A5 e para o item final;
  a **MekaSuit** entra na receita da A5.
- **Create:** latão e mecanismo no começo; Assombrar para Morte; montagem em sequência para fechar a Essência.
- **Draconic:** Fusão no D4 e D5 (A4, A5, Coração); Núcleo Caótico na A5 e no item final.
- **AE2, MI, Oritech, IF, Mystical Agriculture, Forbidden Arcanus:** pedaços do D4 e D5 (ver tabela de `docs/integracoes.md`).

## 11. Precisa do Nikolas
- Aprovar os **5 degraus** e se cada receita deve pedir **magia e tech juntos** (ou aceitar só um dos dois com custo maior).
- Nomes dos materiais (Resíduo, Matérias, Fragmento/Essência/Núcleo/Coração) → mesa Lore.
- O que o **item final** faz e se ele é ligado ao **Medo** (pendente em `docs/decisoes.md`).
- Armadura: aprovar os **números da tabela 6.3**, a regra do **Transcender mínimo**, o corte no **PvP** e se a **A5 consome a MekaSuit**.
- Visual da armadura por Marca (6.6): espera prévia do Modelador.
- Lista de itens de suporte (seção 7).
- Mapa de chefe por elemento.
