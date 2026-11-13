# Graph Report - Od-Teste  (2026-11-13)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1445 nodes · 4093 edges · 75 communities (26 shown, 49 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 126 edges (avg confidence: 0.85)
- Token cost: 36,138 input · 974 output

## Graph Freshness
- Built from commit: `16e636e1`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Comandos de História
- Animações do Jogador
- Asserções de Teste
- Busca de Espaço
- Inicialização do Mod
- Grupos de Jogadores
- Ouvinte do Primeiro Sinal
- Workflow de Compilação CI
- Integração EMI e Registros
- Faixas de Lucidez
- Elementos Paranormais
- Registros e Atributos
- Testes de Limites
- Ritual Chamado de Sangue
- Artefatos Paranormais
- Investida de Ganchos
- Sistema de Lucidez
- Agenda de Horror
- Dados do Jogador
- Efeito de Ferida
- Eventos de Dano e Sono
- Renderização da Marca
- Claims FTB Chunks
- Alucinações no Cliente
- Pacote Marca Visível
- Regras de Exposição
- Sorteio de Horror
- Tipos de Alucinação
- Backup de Blocos
- Efeitos e Partículas
- Leitura do Mundo Real
- Spawn da Criatura de Sangue
- Código Morse
- Tooltips de Itens
- Chuva de Filhotes
- Padrão de Veias
- Regras do Ato 1
- Regras de Lucidez
- Efeitos de Lucidez
- Comandos de Admin
- Integrações com Mods
- Limites de Valores
- Esconder Receitas no JEI
- Criatura de Sangue
- Marca de Sangue
- Dados da História
- Testes de Regras de Elemento
- Regras da Marca
- Sons Posicionais
- Tipos de Momento
- Repetição de Eventos
- Testes de Consistência
- Script Baixar Libs
- Script Teste de Servidor

## God Nodes (most connected - your core abstractions)
1. `Elemento` - 78 edges
2. `DadosJogador` - 49 edges
3. `PrimeiroSinal` - 47 edges
4. `ComandosHistoria` - 34 edges
5. `RegrasTest` - 32 edges
6. `RegrasHorror` - 31 edges
7. `Degrau` - 29 edges
8. `FaixaLucidez` - 26 edges
9. `Alucinacao` - 26 edges
10. `Grupos` - 26 edges

## Surprising Connections (you probably didn't know these)
- `Tentativa` --references--> `Backup`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/estruturas/Estruturas.java → src/main/java/com/nikolas/odteste/estruturas/Backup.java
- `Tentativa` --references--> `Resultado`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/estruturas/Estruturas.java → src/main/java/com/nikolas/odteste/estruturas/Espaco.java
- `EfeitosLucidez` --references--> `Degrau`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/cliente/EfeitosLucidez.java → src/main/java/com/nikolas/odteste/sistemas/Degrau.java
- `EfeitosLucidez` --references--> `FaixaLucidez`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/cliente/EfeitosLucidez.java → src/main/java/com/nikolas/odteste/sistemas/FaixaLucidez.java
- `DadosJogador` --references--> `Elemento`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/dados/DadosJogador.java → src/main/java/com/nikolas/odteste/elementos/Elemento.java

## Import Cycles
- None detected.

## Communities (75 total, 49 thin omitted)

### Community 0 - "Comandos de História"
Cohesion: 0.06
Nodes (5): ComandosElemento, ComandosHistoria, Estruturas, Tentativa, IdsHistoria

### Community 1 - "Animações do Jogador"
Cohesion: 0.05
Nodes (10): ConfigCliente, Forca, DESLIGADO, LIGADO, SUAVE, MomentosCliente, Envelope, Pulso (+2 more)

### Community 2 - "Asserções de Teste"
Cohesion: 0.06
Nodes (11): RegrasSangramento, Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM (+3 more)

### Community 3 - "Busca de Espaço"
Cohesion: 0.11
Nodes (16): Caixa, Chao, Espaco, Mundo, Resultado, Tipo, AR, COM_DADOS (+8 more)

### Community 4 - "Inicialização do Mod"
Cohesion: 0.08
Nodes (5): ConteudoCliente, OdModCliente, Compat, OccultismCompat, OdMod

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.13
Nodes (3): Grupo, Grupos, GruposTest

### Community 7 - "Ouvinte do Primeiro Sinal"
Cohesion: 0.11
Nodes (4): Ouvinte, PrimeiroSinal, Resposta, Som

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.07
Nodes (33): runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs, compilar (+25 more)

### Community 9 - "Integração EMI e Registros"
Cohesion: 0.09
Nodes (4): EsconderNoEmi, Criaturas, Itens, Paranormal

### Community 10 - "Faixas de Lucidez"
Cohesion: 0.12
Nodes (14): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+6 more)

### Community 11 - "Elementos Paranormais"
Cohesion: 0.13
Nodes (8): Elemento, CONHECIMENTO, ENERGIA, MORTE, NENHUM, SANGUE, RegrasElemento, ElementoJogador

### Community 13 - "Testes de Limites"
Cohesion: 0.12
Nodes (3): LimitesTest, ElementoTest, RegrasConteudoTest

### Community 14 - "Ritual Chamado de Sangue"
Cohesion: 0.08
Nodes (14): RitualChamadoDeSangue, Abalo, APARICAO, BATALHA_RELIQUIA, MOB_SANGUE, Fonte, APARICAO, CRIATURA_LUTA (+6 more)

### Community 15 - "Artefatos Paranormais"
Cohesion: 0.12
Nodes (5): AgulhaDeCerzir, Artefato, SinoDeMatadouro, Momentos, Reacoes

### Community 17 - "Sistema de Lucidez"
Cohesion: 0.16
Nodes (3): Lucidez, Estado, Vigia

### Community 22 - "Renderização da Marca"
Cohesion: 0.15
Nodes (3): CamadaMarca, ModeloCriatura, Paleta

### Community 23 - "Claims FTB Chunks"
Cohesion: 0.14
Nodes (3): ClaimsFtbChunks, Colocacao, Protecao

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.21
Nodes (3): Alucinacoes, Figura, Porta

### Community 25 - "Pacote Marca Visível"
Cohesion: 0.17
Nodes (3): MarcaVisivel, Momento, SincronizarJogador

### Community 26 - "Regras de Exposição"
Cohesion: 0.10
Nodes (3): RegrasExposicao, Resultado, RegrasTest

### Community 28 - "Tipos de Alucinação"
Cohesion: 0.13
Nodes (15): Alucinacao, CISCO, CORACAO, ESCURECER, ESTALO, INCLINAR, MOB_FALSO, NOME (+7 more)

### Community 35 - "Código Morse"
Cohesion: 0.20
Nodes (4): Morse, Sequencia, Toque, MorseTest

### Community 38 - "Padrão de Veias"
Cohesion: 0.19
Nodes (3): PadraoVeias, Regiao, PadraoVeiasTest

### Community 39 - "Regras do Ato 1"
Cohesion: 0.14
Nodes (5): Fonte, NOTA, SINO, RegrasAto1, RegrasAto1Test

### Community 43 - "Integrações com Mods"
Cohesion: 0.13
Nodes (13): Integracao, BOTANIA, CREATE, CURIOS, EMI, FTB_CHUNKS, IRONS, JEI (+5 more)

### Community 53 - "Tipos de Momento"
Cohesion: 0.22
Nodes (6): TipoMomento, DEGRAU, MARCA_APARECEU, MARCA_SUBIU, MARCA_TROCOU, SURTO

## Knowledge Gaps
- **77 isolated node(s):** `DESLIGADO`, `LIGADO`, `SUAVE`, `A_BEIRA`, `ATRAVESSADO` (+72 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 345 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **49 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Elemento` connect `Elementos Paranormais` to `Comandos de História`, `Animações do Jogador`, `Spawn da Criatura de Sangue`, `Asserções de Teste`, `Tooltips de Itens`, `Comandos de Admin`, `Registros e Atributos`, `Limites de Valores`, `Ritual Chamado de Sangue`, `Artefatos Paranormais`, `Testes de Regras de Elemento`, `Regras da Marca`, `Dados do Jogador`, `Eventos de Dano e Sono`, `Renderização da Marca`, `Pacote Marca Visível`, `Efeitos e Partículas`?**
  _High betweenness centrality (0.091) - this node is a cross-community bridge._
- **What connects `DESLIGADO`, `LIGADO`, `SUAVE` to the rest of the system?**
  _77 weakly-connected nodes found - possible missing edges._
- **Should `Comandos de História` be split into smaller, more focused modules?**
  _Cohesion score 0.06473960821786909 - nodes in this community are weakly interconnected._
- **Why does `DadosJogador` connect `Dados do Jogador` to `Comandos de História`, `Inicialização do Mod`, `Tooltips de Itens`, `Blocos e Pacotes de Mundo`, `Comandos de Admin`, `Elementos Paranormais`, `Registros e Atributos`, `Limites de Valores`, `Artefatos Paranormais`, `Sistema de Lucidez`, `Movimento e Vulto`, `Eventos de Dano e Sono`, `Renderização da Marca`, `Pacote Marca Visível`, `Efeitos e Partículas`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **Should `Animações do Jogador` be split into smaller, more focused modules?**
  _Cohesion score 0.050616050616050616 - nodes in this community are weakly interconnected._
- **Why does `PrimeiroSinal` connect `Ouvinte do Primeiro Sinal` to `Comandos de História`, `Inicialização do Mod`, `Grupos de Jogadores`, `Blocos e Pacotes de Mundo`, `Dados da História`, `Itens da História`, `Backup de Blocos`, `Efeitos e Partículas`, `Leitura do Mundo Real`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **Should `Asserções de Teste` be split into smaller, more focused modules?**
  _Cohesion score 0.06116700201207243 - nodes in this community are weakly interconnected._