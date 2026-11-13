# Graph Report - bal-materia  (2026-11-13)

## Corpus Check
- 291 files · ~333,916 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .nbt 7, (none) 6, .bbmodel 5)

## Summary
- 3308 nodes · 9697 edges · 138 communities (64 shown, 74 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 330 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f69985ef`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- net.minecraft.commands.CommandSourceStack
- .em
- assertequals
- Busca de Espaço
- Aparelhos.java
- Grupos de Jogadores
- PrimeiroSinal.java
- PrimeiroSinal
- Workflow de Compilação CI
- DedalDentado
- Degrau
- net.minecraft.server.level.ServerPlayer
- tom
- org.junit.jupiter.api.Test
- .contato
- EstadoGrupo
- Investida de Ganchos
- Sistema de Lucidez
- net.minecraft.world.level.block.state.BlockState
- .marcaElemento
- Efeito de Ferida
- list
- Renderização da Marca
- ClaimsFtbChunks.java
- Alucinações no Cliente
- OdMod.java
- RegrasTest
- RegrasHorror
- modulos.py
- net.minecraft.server.level.ServerLevel
- ComandosHistoria.java
- Leitura do Mundo Real
- Criaturas
- Código Morse
- Chuva de Filhotes
- Padrão de Veias
- Fonte
- RegrasCapelaTest
- .aoTick
- Comandos de Admin
- Integrações com Mods
- Limites de Valores
- net.minecraft.core.BlockPos
- CriaturaDeSangue
- Marca de Sangue
- net.minecraft.server.MinecraftServer
- vila_calada_150.py
- math
- CenaCliente.java
- ruido
- Tipos de Momento
- gerar_marca_v2.py
- Testes de Consistência
- net.minecraft.world.item.ItemStack
- Script Baixar Libs
- Script Teste de Servidor
- numpy
- RegrasClimaVila
- gerar_marca.py
- gerar_png.py
- RegrasSinal
- PistasVilaCalada
- Estrutura
- Caixa
- net.minecraft.resources.ResourceLocation
- testar.py
- vila_destruida.py
- render.py
- gerar_sons.py
- .apontarDeLonge
- .use
- .sortearDrop
- RegrasCena
- .de
- AnimacaoJogador.java
- nbt_io.py
- .aoSoltar
- Cenas
- gerar_criaturas_sangue.py
- CenaCliente
- RegrasVilaCalada
- .montar
- previa_vila_calada_150.py
- Cena
- LivrosLidos
- .pose
- AnalisadorSinalEntidade
- RegrasClimaVilaTest
- RegrasFonteVilaTest
- Imagem
- ConfigCliente
- .ler
- .de
- Partes
- blocos.py
- ClimaVilaCalada
- Abalo
- construida
- CamadaMarca
- SomVento.java
- Ronda
- EventoEnergia
- .apply
- mancha
- Modo
- ConfigEstruturas
- LeitorDeItem
- BlocosCapelaIds.java
- comparar_criaturas_sangue.sh
- criaturas_sangue.sh
- gerar.sh script
- previa.sh
- gerar_vila.sh
- gerar_vila_calada.sh
- gerar_vila_calada_150.sh
- marca_sangue.sh
- marca_sangue_v2.sh
- render_geckolib.sh script

## God Nodes (most connected - your core abstractions)
1. `Elemento` - 78 edges
2. `PrimeiroSinal` - 66 edges
3. `DadosJogador` - 56 edges
4. `tom()` - 53 edges
5. `ruido()` - 52 edges
6. `DadosHistoria` - 41 edges
7. `Estrutura` - 40 edges
8. `Backup` - 40 edges
9. `OdMod` - 39 edges
10. `ComandosHistoria` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Image` --uses--> `Imagem`  [INFERRED]
  arte/marca-sangue/gerar_marca.py → arte/marca-sangue/imagem_simples.py
- `main()` --calls--> `Molde`  [EXTRACTED]
  arte/marca-sangue/v2/gerar_marca_v2.py → arte/marca-sangue/v2/veias.py
- `textura_provisoria()` --calls--> `cor()`  [EXTRACTED]
  arte/previas/criaturas/fonte/comparacao.py → arte/previas/criaturas/fonte/montagem.py
- `gerar()` --calls--> `bbmodel()`  [EXTRACTED]
  arte/previas/criaturas/fonte/gerar_criaturas_sangue.py → arte/previas/criaturas/fonte/montagem.py
- `mat()` --calls--> `sombreado()`  [EXTRACTED]
  arte/previas/criaturas/fonte/sanguessuga_mae.py → arte/previas/criaturas/fonte/montagem.py

## Import Cycles
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`

## Communities (138 total, 74 thin omitted)

### Community 1 - ".em"
Cohesion: 0.14
Nodes (4): MomentosCliente, Envelope, Pulso, Tela

### Community 3 - "Busca de Espaço"
Cohesion: 0.11
Nodes (15): Chao, Espaco, Mundo, Tipo, AR, COM_DADOS, FORA_DO_MUNDO, LIQUIDO (+7 more)

### Community 4 - "Aparelhos.java"
Cohesion: 0.04
Nodes (9): Aparelhos, BlocosCapela, Freio, ConteudoCliente, OdModCliente, OccultismCompat, Sangramento, Itens (+1 more)

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.12
Nodes (3): Grupo, Grupos, GruposTest

### Community 6 - "PrimeiroSinal.java"
Cohesion: 0.04
Nodes (3): IdsCena, IdsHistoria, IdsVilaCalada

### Community 7 - "PrimeiroSinal"
Cohesion: 0.08
Nodes (6): DadosHistoria, Ouvinte, PrimeiroSinal, Resposta, Som, VilaMarcada

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.06
Nodes (39): outputs, runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs (+31 more)

### Community 10 - "Degrau"
Cohesion: 0.06
Nodes (29): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+21 more)

### Community 11 - "net.minecraft.server.level.ServerPlayer"
Cohesion: 0.04
Nodes (16): RitualChamadoDeSangue, DadosJogador, Elemento, CONHECIMENTO, ENERGIA, MORTE, NENHUM, SANGUE (+8 more)

### Community 12 - "tom"
Cohesion: 0.05
Nodes (86): _aba(), aba_pintor(), aba_pintor2(), anims(), antebraco(), avental(), bota(), calca() (+78 more)

### Community 13 - "org.junit.jupiter.api.Test"
Cohesion: 0.07
Nodes (5): DadosAnalisadorTest, LimitesTest, ElementoTest, RegrasElementoTest, RegrasConteudoTest

### Community 14 - ".contato"
Cohesion: 0.12
Nodes (10): Fonte, APARICAO, CRIATURA_LUTA, CRIATURA_MORTA, CRIATURA_VISTA, DIMENSAO, RELIQUIA_PEGAR, RELIQUIA_USAR (+2 more)

### Community 15 - "EstadoGrupo"
Cohesion: 0.06
Nodes (13): ConfigGatilhos, DadosGatilhos, EstadoGrupo, Gatilhos, Acao, AVISAR_ADMIN, NADA, SOLTAR_PRIMEIRO_SINAL (+5 more)

### Community 17 - "Sistema de Lucidez"
Cohesion: 0.17
Nodes (3): Lucidez, Estado, Vigia

### Community 18 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.07
Nodes (4): AnalisadorSinalBloco, PocaDeSangue, VelaDaCapela, VelaDaCapelaDados

### Community 20 - "Efeito de Ferida"
Cohesion: 0.10
Nodes (3): CuriosCompat, RegrasSangramento, Rubra

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.16
Nodes (4): Alucinacoes, Figura, Porta, Sons

### Community 25 - "OdMod.java"
Cohesion: 0.05
Nodes (10): Compat, EsconderNoEmi, EsconderNoJei, IdsDados, OdMod, MarcaVisivel, Momento, SincronizarJogador (+2 more)

### Community 26 - "RegrasTest"
Cohesion: 0.05
Nodes (6): RegrasExposicao, Resultado, Acumulo, RegrasLucidez, RegrasMarca, RegrasTest

### Community 27 - "RegrasHorror"
Cohesion: 0.10
Nodes (4): Agenda, RegrasHorror, Sorteio, RegrasHorrorTest

### Community 28 - "modulos.py"
Cohesion: 0.07
Nodes (39): capela(), parede(), casa(), parede(), construir(), desgaste(), exterior(), passagem() (+31 more)

### Community 35 - "Código Morse"
Cohesion: 0.09
Nodes (6): Morse, Sequencia, Toque, RegrasAto1, MorseTest, RegrasAto1Test

### Community 38 - "Padrão de Veias"
Cohesion: 0.23
Nodes (3): PadraoVeias, Regiao, PadraoVeiasTest

### Community 39 - "Fonte"
Cohesion: 0.67
Nodes (3): Fonte, NOTA, SINO

### Community 43 - "Integrações com Mods"
Cohesion: 0.13
Nodes (13): Integracao, BOTANIA, CREATE, CURIOS, EMI, FTB_CHUNKS, IRONS, JEI (+5 more)

### Community 45 - "net.minecraft.core.BlockPos"
Cohesion: 0.07
Nodes (12): FonteDeSinal, Fonte, FontesFixas, FonteVilaCalada, Resultado, CHEIO, NOME_INVALIDO, OK (+4 more)

### Community 48 - "net.minecraft.server.MinecraftServer"
Cohesion: 0.26
Nodes (4): Colocacao, Acesso, EmPartes, Memoria

### Community 49 - "vila_calada_150.py"
Cohesion: 0.09
Nodes (39): arvore_morta(), arvores_mortas(), caminho(), caminho_dos_enterros(), campos(), carroca(), cemiterio(), cerca() (+31 more)

### Community 50 - "math"
Cohesion: 0.06
Nodes (28): Animacao, _avaliar(), Camera, centro_para(), _chaves(), _ease(), escala_m(), faixa_titulo() (+20 more)

### Community 52 - "ruido"
Cohesion: 0.08
Nodes (35): Mistura, ruido(), paiol(), arvore(), arvores_queimadas(), campanario(), campos(), casa() (+27 more)

### Community 53 - "Tipos de Momento"
Cohesion: 0.29
Nodes (6): TipoMomento, DEGRAU, MARCA_APARECEU, MARCA_SUBIU, MARCA_TROCOU, SURTO

### Community 54 - "gerar_marca_v2.py"
Cohesion: 0.09
Nodes (30): _caixa(), corpo(), faces(), mapa(), render(), _rot(), skin_teste(), skins_teste() (+22 more)

### Community 75 - "numpy"
Cohesion: 0.09
Nodes (18): faixa(), topo(), amostrar(), _c(), coracao(), corpo_veia(), costura(), _desdobrar() (+10 more)

### Community 76 - "RegrasClimaVila"
Cohesion: 0.09
Nodes (3): ClimaVilaCliente, Pendurado, RegrasClimaVila

### Community 77 - "gerar_marca.py"
Cohesion: 0.09
Nodes (22): ampliar_molde(), caixa(), corpo(), crescer(), desenhar(), fluxo(), fonte(), Image (+14 more)

### Community 78 - "gerar_png.py"
Cohesion: 0.12
Nodes (26): afim(), amostra(), compor(), cor(), cruz(), cruzar(), cubos_no_mundo(), andar() (+18 more)

### Community 79 - "RegrasSinal"
Cohesion: 0.08
Nodes (16): Direcao, DIREITA, ESQUERDA, FRENTE, FRENTE_DIREITA, FRENTE_ESQUERDA, TRAS, TRAS_DIREITA (+8 more)

### Community 80 - "PistasVilaCalada"
Cohesion: 0.14
Nodes (3): DadosVilaCalada, Estado, PistasVilaCalada

### Community 81 - "Estrutura"
Cohesion: 0.11
Nodes (3): Estrutura, TesteNBT, TesteVila150

### Community 82 - "Caixa"
Cohesion: 0.19
Nodes (3): Caixa, Fatias, FatiasTest

### Community 83 - "net.minecraft.resources.ResourceLocation"
Cohesion: 0.21
Nodes (3): Resultado, Estruturas, Tentativa

### Community 84 - "testar.py"
Cohesion: 0.15
Nodes (11): gerar(), main(), _salvar(), main(), recorte(), salvar(), main(), salvar() (+3 more)

### Community 85 - "vila_destruida.py"
Cohesion: 0.13
Nodes (21): arvores_queimadas(), casa(), casa_do_livro(), casas(), chao(), chao_queimado(), cinzas(), conectar() (+13 more)

### Community 86 - "render.py"
Cohesion: 0.13
Nodes (15): hash01(), c(), _amostrador(), cor_px(), _clamp(), _face(), _girar(), _girar_dir() (+7 more)

### Community 87 - "gerar_sons.py"
Cohesion: 0.13
Nodes (10): arrasto(), Biquad, eco(), envelope_bordas(), gravar_wav(), main(), normalizar(), para_ogg() (+2 more)

### Community 89 - ".use"
Cohesion: 0.11
Nodes (10): Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM, INCOMUM (+2 more)

### Community 90 - ".sortearDrop"
Cohesion: 0.12
Nodes (3): Achados, RegrasAchados, RegrasAchadosTest

### Community 91 - "RegrasCena"
Cohesion: 0.14
Nodes (4): Legenda, Quadro, RegrasCena, RegrasCenaTest

### Community 94 - "nbt_io.py"
Cohesion: 0.14
Nodes (13): Byte, Double, _escrever(), Float, Int, _Leitor, ler(), Lista (+5 more)

### Community 95 - ".aoSoltar"
Cohesion: 0.13
Nodes (4): CansacoDoElemento, RegrasCansaco, Cansaco, RegrasMateriaTest

### Community 96 - "Cenas"
Cohesion: 0.16
Nodes (3): Ancora, Cenas, CenaEntradaVila

### Community 97 - "gerar_criaturas_sangue.py"
Cohesion: 0.16
Nodes (10): carregar(), colunas(), folha(), main(), textura_provisoria(), ampliar(), gerar(), main() (+2 more)

### Community 100 - ".montar"
Cohesion: 0.20
Nodes (3): Pedaco, RegrasLivros, RegrasLivrosTest

### Community 101 - "previa_vila_calada_150.py"
Cohesion: 0.16
Nodes (9): nome_completo(), _texto(), com_chao_do_mundo(), conferir_centro(), main(), recortar(), salvar_nbt(), salvar_png() (+1 more)

### Community 102 - "Cena"
Cohesion: 0.18
Nodes (5): Cena, Plano, Ponto, Som, CenasConhecidas

### Community 106 - "RegrasClimaVilaTest"
Cohesion: 0.14
Nodes (3): RegrasCenaVila, RegrasCenaVilaTest, RegrasClimaVilaTest

### Community 109 - "ConfigCliente"
Cohesion: 0.16
Nodes (5): ConfigCliente, Forca, DESLIGADO, LIGADO, SUAVE

### Community 110 - ".ler"
Cohesion: 0.29
Nodes (3): LeitorLivros, Pagina, Trecho

### Community 112 - "Partes"
Cohesion: 0.19
Nodes (7): Fase, COLOCANDO, CONFERINDO, DESFAZENDO, PAUSADA, PRONTA, Partes

### Community 113 - "blocos.py"
Cohesion: 0.31
Nodes (5): cheio(), curto(), formas(), material(), _verdade()

### Community 115 - "Abalo"
Cohesion: 0.22
Nodes (4): Abalo, APARICAO, BATALHA_RELIQUIA, MOB_SANGUE

### Community 120 - "EventoEnergia"
Cohesion: 0.25
Nodes (4): EventoEnergia, APAGAO, CASA_OCUPADA, FOME_DA_LINHA

### Community 122 - "mancha"
Cohesion: 0.40
Nodes (4): sangue(), mancha(), _pingo(), rastro()

### Community 123 - "Modo"
Cohesion: 0.33
Nodes (4): Modo, COMPLETO, SEM_BALANCO, SO_TEXTO

## Knowledge Gaps
- **107 isolated node(s):** `COMPLETO`, `SEM_BALANCO`, `SO_TEXTO`, `CONFERINDO`, `COLOCANDO` (+102 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 752 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **74 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lista` connect `nbt_io.py` to `list`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **What connects `COMPLETO`, `SEM_BALANCO`, `SO_TEXTO` to the rest of the system?**
  _107 weakly-connected nodes found - possible missing edges._
- **Should `.em` be split into smaller, more focused modules?**
  _Cohesion score 0.1380952380952381 - nodes in this community are weakly interconnected._
- **Why does `Estrutura` connect `Estrutura` to `previa_vila_calada_150.py`, `vila_calada_150.py`, `ruido`, `vila_destruida.py`, `testar.py`, `modulos.py`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Should `assertequals` be split into smaller, more focused modules?**
  _Cohesion score 0.10935143288084465 - nodes in this community are weakly interconnected._
- **Why does `Elemento` connect `net.minecraft.server.level.ServerPlayer` to `.em`, `assertequals`, `Limites de Valores`, `org.junit.jupiter.api.Test`, `.contato`, `.de`, `.marcaElemento`, `OdMod.java`, `RegrasTest`, `ComandosHistoria.java`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Should `Busca de Espaço` be split into smaller, more focused modules?**
  _Cohesion score 0.10714285714285714 - nodes in this community are weakly interconnected._