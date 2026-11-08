# Papel de cada mod do pack (PROPOSTA — esperando o Nikolas)

Ideia geral: o paranormal vaza para o mundo e **cada tradição tenta lidar com ele do seu jeito**.
Cada mod do pack vira uma dessas tradições. O jogador escolhe por onde entra, mas a progressão boa cruza duas ou mais.
Detalhe da progressão em `docs/progressao.md` (degraus D1 a D5) e da voz em `docs/voz.md`.

## Magia

| Mod | Tradição na lore (proposta) | Como integra no jogo | Papel na progressão |
|---|---|---|---|
| **Occultism** | Os ritualistas: desenham círculos de giz para chamar e prender | Rituais nossos usando o giz e os pentáculos do Occultism; símbolos novos nossos | Fecha o **Núcleo** (D4) com Afrit/Marid e sacrifício; molde da armadura A3; última etapa do item final |
| **Iron's Spells** | Os conjuradores: falam e gesticulam o paranormal | Uma escola de magia nova, com magias próprias e livro próprio | Essência Arcana no D1–D2; Orbes de Melhoria nas Têmperas; Bigorna Arcana; Fragmento de Conhecimento Antigo |
| **Malum** | Os que colhem almas | Almas de criaturas nossas viram espíritos do Malum; altar como etapa de progressão | **Altar Espiritual** faz a **Essência** (D3), o molde A2, a Lâmina L2 e as Têmperas; Soul Stained Steel e Hallowed Gold |
| **Vampirism** | Os que pagam com sangue | Marcas no corpo que custam sangue; caçadores com ferramentas contra criaturas nossas | Sangue nos componentes de Sangue; sangue puro de nível alto no item final |
| **Botânia** | A natureza que resiste (ou apodrece) | Flores corrompidas pelo paranormal; mana como "remédio" para áreas afetadas | **Infusão de Mana** faz o **Fragmento** (D2); runas no Núcleo; Manasteel, Terrasteel e **Espírito de Gaia** |
| **Forbidden Arcanus** | (a definir) | Forja de Hefesto | Molde da A4 e uma etapa do item final |
| **Mystical Agriculture (+ Agradditions, Extended Tier)** | — | só como ingrediente | Insanium e o tier mais alto no item final. **Nunca** semente de material nosso |

## Tecnologia

| Mod | Tradição na lore (proposta) | Como integra no jogo | Papel na progressão |
|---|---|---|---|
| **Mekanism (+ Evolved Mekanism)** | Os céticos: medem, contêm e processam | Máquinas para detectar, conter e transformar material paranormal | Ligas e circuitos em todos os degraus; radiação para Morte; **SPS e antimatéria** no D5 e no item final |
| **Create (+ New Age, Addition, Occult Engineering)** | Os céticos (mecânicos) | Contraptions para processar material paranormal | Latão e mecanismo no D1; **Assombrar** (Morte); **montagem em sequência** fecha a Essência e a L3; energizador (Energia) |
| **Allthemodium** | — | só como metal | Allthemodium (D3), Vibranium (D4), Unobtainium e ligas (D5) |
| **Draconic Evolution** | — | Fusão como mesa de fim de jogo | **Fusão** monta o **Coração** (D5), a L4 e a A5; Núcleo Caótico no item final |
| **AE2 (+ MEGA Cells, Advanced AE)** | Os céticos (dados) | Discos e processadores guardam o "registro" | Processadores no D2; peças de **Conhecimento**; célula MEGA no item final |
| **Modern Industrialization (+ extensões)** | Os céticos (indústria) | porta tech alternativa ao Mekanism | Aço e circuitos (analógico → quântico); circuito quântico no item final |
| **Oritech** | Os céticos (física) | Acelerador de partículas | Peças de **Energia** no D3–D4; item do acelerador no item final |
| **Industrial Foregoing** | — | só como ingrediente | **Gás Éter** (precisa do Wither) no D4 e no item final. Triturador de mobs **não** gera drop nosso |
| **Powah, Extreme Reactors** | — | energia | Orbe Energizante (Energia); reator na corrente de Morte (opcional) |

## Chefes (itens para Corações e item final)
Cataclysm, Twilight Forest, Mowzie's Mobs e chefes do Iron's. Mapa por elemento em `docs/progressao.md` (seção 5.2). **Conferir drops.**

## Voz
| Mod | Papel |
|---|---|
| **Simple Voice Chat 2.6.22** | API de plugins: o nosso mod muda a voz (Marca/relíquia), o que se ouve (Exposição) e o alcance. Pacote `compat/voicechat/` |
| **SV Voice Changer 1.2.5** | Cada jogador escolhe a voz do personagem. Só cliente. Licença NC: Nikolas avalia |

Regra técnica: cada linha acima vira um pacote `compat/<mod>/` (ou receitas com `neoforge:mod_loaded`) que só liga se o mod estiver no pack.
