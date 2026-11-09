# Plano do Ato 1 jogável

Mesa CEO, 09/11/2026. Base: `lore/roteiro.md` (Ato 1 com a Vila Calada, PR #34), `lore/fichas-estruturas.md`,
`docs/sistemas.md` e o estado do main e dos PRs abertos nesse dia.

**"Jogável" quer dizer:** um grupo sai da vila comum, acha a Vila Calada pelas três pistas, lê o Livro do Sineiro,
passa pelos cinco lugares, resolve os enigmas e chega ao Evento do Diabo, **sem o admin improvisar nada**.
O admin só coloca as estruturas e solta os eventos com comandos prontos.

As horas são de trabalho de mesa (agente + revisão). Não contam o tempo de espera pelo ok do Nikolas.

## Onde estamos (09/11)
- **Pronto no main:** abertura na vila comum (sino, aldeão some, Morse FERRO, baú), grupos, Marca de Sangue,
  comandos `estrutura prever|colocar|desfazer`.
- **Já mesclado depois:** correções da abertura (#31).
- **Em PR:** estrutura em partes (#37), livros por Exposição (#36), prévias Faminto e atril (#38), Analisador de Sinal (#21), protótipo da Vila Calada em `.nbt` (#32, empilhado sobre o #27),
  Lore da Vila Calada (#34), arte da Marca de Sangue (#25, ainda sem integrar).
- **Não existe ainda:** os outros cinco lugares (nenhum `.nbt`), as criaturas do Ato 1, os itens de história além de dois, os blocos de enigma
  e o Evento do Diabo.

## Fase 0: decisões do Nikolas (respondidas em 09/11)
| # | Decisão | Resposta | Arquivo |
|---|---|---|---|
| N1 | Vila Calada: nome, livro, ficha, ordem | **Aprovada** (vila comum → Vila Calada → cinco lugares) | `decisoes/2026-11-09-vila-calada.md` |
| N2 | Protótipo 72x64 ou refazer | **Refazer com ~100x100** pela ficha, prévia antes | `decisoes/2026-11-09-vila-calada.md` |
| N3 | FERRO repetido | **Segunda palavra muda** (Lore escolhe) | `decisoes/2026-11-09-segunda-palavra-morse.md` |
| N4 | Nomes | **"Diário do Açougueiro"** e **"Magarefe"** | `decisoes/2026-11-09-nomes-acougueiro-magarefe.md` |
| N5 | Quem constrói | **As mesas geram**; o Nikolas pode retocar no criativo | `decisoes/2026-11-09-quem-constroi-estruturas.md` |
| N6 | Enigmas fora do jogo | **Site novo na Vercel**; áudios e vídeos pelas mesas | `decisoes/2026-11-09-site-enigmas-fora.md` |
| N7 | Regras | Capela **só voz** (chat bloqueado), pacto **sem castigo**, queda **sem morte instantânea** (dano e susto) | `decisoes/2026-11-09-regras-capela-pacto-queda.md` |
| N8 | Prévias | O Nikolas aprova Faminto, atril (#38) e Marca (#25) quando chegarem | `decisoes/2026-11-09-previas-pendentes.md` |
| NOVO | Ato 1 "bem Minecraft" | **Sem carro, fita, TV, VHS.** A Lore refaz os cinco lugares; nada deles é programado antes | `decisoes/2026-11-09-ato1-bem-minecraft.md` |

**Efeito no plano:** Fases 1 e 2 seguem. **Fases 3, 4 e 5 estão congeladas**: os nomes de lugares e itens abaixo
(Carro, Fita K7, Frigorífico, Pousada, VHS, Repetidora) são do roteiro antigo e vão ser trocados quando o roteiro novo
(`mesa-lore/ato1-minecraft`) for aprovado. As horas continuam como estimativa.

## Fase 1: Vila Calada (o destino), cerca de 95 h
| # | Item | Tipo | Mesa | Horas |
|---|---|---|---|---|
| 1.1 | **Colocar estrutura em partes** (passa do limite de 16.384 blocos, sem travar, com backup e desfazer) | código | Dev | 8 |
| 1.2 | **Livro de história com páginas por condição** (pág. 14 só com Exposição, texto em arquivo de dados) | código | Dev 2 | 8 |
| 1.3 | **Fontes de sinal fixas** (o Analisador capta o sino de longe) | código | Integrador | 6 |
| 1.4 | Estrutura final da Vila Calada (`.nbt` no lugar certo: `data/odteste/structure/`) com o atril, os baús e a cama | estrutura | Modelador | 16 |
| 1.5 | Prévias do Faminto e do atril (**M1**) | modelo | Modelador | 5 |
| 1.6 | Faminto: modelo, textura e animação GeckoLib depois do ok | modelo | Modelador | 10 |
| 1.7 | Faminto: criatura (parado de dia, solto de noite, sem drop que vire grind) | código | Dev | 8 |
| 1.8 | Âncora `vila_calada` + as três pistas: sino rachado ao longe à noite, aldeões virados, cama manchada | código | Dev | 8 |
| 1.9 | Mapa de cartógrafo que se desenha conforme se anda | código | Dev 2 | 6 |
| 1.10 | Vela da capela (devolve Lucidez) e poças (tiram Lucidez) | código | Dev 2 | 4 |
| 1.11 | Abrir o livro toca o sino; aviso ao admin "{grupo} não seguiu o sino" | código | Dev | 3 |
| 1.12 | Sons próprios (sino rachado, vento, arrasto) + `sounds.json` | som | Modelador | 5 |
| 1.13 | Textos finais (lang pt_br/en_us) da vila e do livro | textos | Lore | 3 |
| 1.14 | Teste de ponta a ponta da Vila Calada (roteiro de QA + teste de servidor) | testes | Revisor | 5 |

## Fase 2: Abertura amarrada, cerca de 12 h
| # | Item | Tipo | Mesa | Horas |
|---|---|---|---|---|
| 2.1 | Mesclar #31 (correções da abertura) | revisão | Revisor + CEO | 2 |
| 2.2 | Evento solta sozinho na primeira noite de neblina (com e sem o livro lido) | código | Dev | 4 |
| 2.3 | Mochila com o nome bordado (lanterna que falha, rádio sem sinal, caderno) sem ID novo, se der | código | Dev 2 | 4 |
| 2.4 | Estandarte definitivo da vila | modelo | Modelador | 2 |

## Fase 3: Clareira e Frigorífico, cerca de 90 h
| # | Item | Tipo | Mesa | Horas |
|---|---|---|---|---|
| 3.1 | Clareira e o Carro (`.nbt`) + toca-fitas com Morse, caixa com palavra | estrutura + código | Modelador / Dev | 8 + 6 |
| 3.2 | Itens de história: Fita K7, Diário p.1 a 4, metades do Mapa, Livro-Caixa, Faca de Osso, crachá (usando o sistema 1.2) | código + textos | Dev 2 / Lore | 8 + 4 |
| 3.3 | Enigmas E1 e E2 fora do jogo (áudio da Fita, cifra do Mapa) | conteúdo | Lore + quem o Nikolas indicar | 6 |
| 3.4 | Frigorífico do Km 9 (`.nbt`, grande, em partes) | estrutura | Modelador | 20 |
| 3.5 | Blocos de enigma: alavancas dos ganchos (E3) | código | Dev | 8 |
| 3.6 | Magarefe e Pendurado: prévia, modelo, criatura | modelo + código | Modelador / Dev | 14 + 12 |
| 3.7 | Teste de ponta a ponta (Clareira → Frigorífico) | testes | Revisor | 4 |

## Fase 4: Pousada, Arquivo e Repetidora, cerca de 70 h
| # | Item | Tipo | Mesa | Horas |
|---|---|---|---|---|
| 4.1 | Pousada do Km 12 + Arquivo (`.nbt`) | estrutura | Modelador | 18 |
| 4.2 | E5 (vídeo VHS) e E6 (site secreto) | conteúdo | Lore + quem o Nikolas indicar | 10 |
| 4.3 | Cofre com palavra e Terço | código | Dev 2 | 5 |
| 4.4 | Casa da Repetidora (`.nbt`) | estrutura | Modelador | 12 |
| 4.5 | Console com palavra (cala o sino), telégrafo que dá a Fazenda (E7) | código | Dev | 8 |
| 4.6 | Ligações tech: a Repetidora e o Analisador; receitas | código | Integrador | 6 |
| 4.7 | Sons: rádio, VHS, telégrafo, metal arrastando no mundo todo | som + código | Modelador / Dev 2 | 4 + 3 |
| 4.8 | Teste de ponta a ponta | testes | Revisor | 4 |

## Fase 5: Fazenda do Sino Mudo e Evento do Diabo, cerca de 75 h
| # | Item | Tipo | Mesa | Horas |
|---|---|---|---|---|
| 5.1 | Fazenda: Casarão + Capela (`.nbt`) | estrutura | Modelador | 18 |
| 5.2 | Velas, cálices, revelação do elemento, Altar do Aceite (Mesa do Pacto) | código | Dev 2 | 10 |
| 5.3 | Chat bloqueado na capela (se N7 aprovar) | código | Dev | 3 |
| 5.4 | `/odteste evento diabo <fase>` + falas prontas + Sino do Pacto | código + textos | Dev / Lore | 12 + 4 |
| 5.5 | O Diabo e a Convidada: prévia, modelo, animação | modelo | Modelador | 16 |
| 5.6 | Integrar a arte nova da Marca de Sangue (#25) | modelo + código | Modelador / Dev | 3 + 2 |
| 5.7 | Pista do Ato 2 na cripta | estrutura + textos | Modelador / Lore | 3 + 2 |
| 5.8 | Ensaio do Ato 1 inteiro com o grupo (servidor de teste, nunca o definitivo) | testes | Revisor + Nikolas | 6 |

**Total aproximado: 340 h de mesa.** Com quatro mesas em paralelo, umas 90 h de relógio, mais o tempo das decisões.
O gargalo é a **Modelador** (estruturas grandes, cerca de 150 h). Se N5 trouxer um construtor humano, cai pela metade.

## Primeiras tarefas (lançadas em 09/11, em paralelo)
| Tarefa | Mesa | Branch | Arquivos dela (ninguém mais mexe) |
|---|---|---|---|
| 1.1 Estrutura em partes | Dev | `mesa-dev/estrutura-em-partes` (sobre o #31) | `estruturas/`, parte `estrutura` de `ComandosHistoria.java` |
| 1.2 Livros de história | Dev 2 | `mesa-dev/livros-historia` | `historia/livros/`, `comandos/ComandosLivros.java`, `data/odteste/odteste_livros/` |
| 1.3 Fontes de sinal fixas | Integrador | `mesa-integrador/analisador` (PR #21) | `sinais/`, `aparelhos/`, comando de sinal |
| 1.5 Prévias Faminto e atril | Modelador | `mesa-modelador/previas-vila-calada` | `arte/vila-calada/` |

Regras para não dar conflito:
- Ninguém dessas quatro mexe em `docs/progresso.md` nem em `docs/ids.md`: cada PR lista as chaves novas e a CEO junta tudo num commit só.
- Os textos novos de livro ficam em arquivo de dados, não no `lang`. Só o Integrador já tinha `lang` no PR #21.
- Ponto comum conhecido: a **linha de registro de comandos** (Dev 2 e Integrador acrescentam uma linha cada). Conflito de uma linha, a CEO resolve no merge.
- Ordem de merge: #31 → 1.1 → 1.3 (#21) → 1.2. Prévias não entram em código.

## Segunda rodada (lançada em 09/11, depois da Fase 0)
| Tarefa | Mesa | Branch | Arquivos dela (ninguém mais mexe) |
|---|---|---|---|
| Cinco lugares "bem Minecraft", nova palavra Morse, nomes N4 | Lore | `mesa-lore/ato1-minecraft` (sobre o #34) | `lore/roteiro.md`, `lore/fichas-estruturas.md`, `lore/enigmas-fora.md` |
| 1.4 Vila Calada ~100x100, **só prévia** | Modelador | `mesa-modelador/vila-calada-100` (sobre o #32) | `arte/vila-calada-100/` |
| 1.8 Âncora `vila_calada` + três pistas | Dev | `mesa-dev/vila-calada-pistas` (sobre o #37) | `historia/vilacalada/` |
| 1.10 Vela da capela e poças | Dev 2 | `mesa-dev/vela-pocas` | pacote novo dos dois blocos |
| Revisão de #36, #37 e #21 | Revisor | (só lê) | nenhum |

Regras de antes continuam: `docs/progresso.md` e `docs/ids.md` só a CEO, uma linha de registro em comum é resolvida no merge.
Esperando: o Integrador fica livre até a revisão do #21; o Faminto (1.6, 1.7) espera o ok da prévia do #38.

## Próximo passo
Aprovar o roteiro novo da Lore → reescrever as Fases 3 a 5 com os lugares novos. Ordem de merge: #37 → #21 → #36 → pistas e vela.
Lore antes de código, código antes de revisão.
