# Graph Report - bal-materia  (2026-11-13)

## Corpus Check
- 291 files · ~334,477 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .nbt 7, (none) 6, .bbmodel 5)

## Summary
- 3320 nodes · 9736 edges · 144 communities (65 shown, 79 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 330 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1fe19b26`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- net.minecraft.commands.CommandSourceStack
- Tela
- Busca de Espaço
- Aparelhos.java
- Grupos de Jogadores
- PrimeiroSinal.java
- PrimeiroSinal
- Workflow de Compilação CI
- AnalisadorSinalBloco.java
- Degrau
- net.minecraft.server.level.ServerPlayer
- tom
- org.junit.jupiter.api.Test
- .contato
- EstadoGrupo
- Investida de Ganchos
- Sistema de Lucidez
- net.minecraft.world.level.Level
- Efeito de Ferida
- Renderização da Marca
- ClaimsFtbChunks.java
- Alucinações no Cliente
- OdMod
- RegrasTest
- RegrasHorror
- modulos.py
- net.minecraft.server.level.ServerLevel
- ComandosHistoria.java
- Leitura do Mundo Real
- SanguessugaMae.java
- Criaturas
- Código Morse
- Chuva de Filhotes
- Padrão de Veias
- Alucinacao
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
- sombreado
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
- AnimacaoJogador
- nbt_io.py
- .aoSoltar
- Cenas
- montagem.py
- CenaCliente
- RegrasVilaCalada
- hash01
- Cena
- LivrosLidos
- Local
- AnalisadorSinalEntidade
- net.minecraft.client.player.AbstractClientPlayer
- RegrasFonteVilaTest
- Imagem
- ConfigEstruturas
- RegrasElementoTest
- ElementoJogador
- Partes
- blocos.py
- ClimaVilaCalada
- Abalo
- Criatura
- CamadaMarca
- SomVento.java
- Ronda
- EventoEnergia
- EsconderNoJei.java
- capela_fazenda.py
- Modo
- .OdModCliente
- comparacao.py
- canal
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
- .deId
- DadosVilaCalada
- .estado
- Forca
- .adicionar
- IdsGatilhos.java

## God Nodes (most connected - your core abstractions)
1. `Elemento` - 78 edges
2. `PrimeiroSinal` - 66 edges
3. `DadosJogador` - 56 edges
4. `tom()` - 53 edges
5. `ruido()` - 52 edges
6. `DadosHistoria` - 41 edges
7. `Backup` - 40 edges
8. `Estrutura` - 40 edges
9. `OdMod` - 39 edges
10. `ComandosHistoria` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Image` --uses--> `Imagem`  [INFERRED]
  arte/marca-sangue/gerar_marca.py → arte/marca-sangue/imagem_simples.py
- `mat()` --calls--> `sombreado()`  [EXTRACTED]
  arte/previas/criaturas/fonte/sanguessuga_mae.py → arte/previas/criaturas/fonte/montagem.py
- `Retalhado` --inherits--> `CriaturaDeSangue`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/criaturas/Retalhado.java → src/main/java/com/nikolas/odteste/criaturas/CriaturaDeSangue.java
- `SanguessugaMae` --inherits--> `CriaturaDeSangue`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/criaturas/SanguessugaMae.java → src/main/java/com/nikolas/odteste/criaturas/CriaturaDeSangue.java
- `EfeitosLucidez` --references--> `Degrau`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/cliente/EfeitosLucidez.java → src/main/java/com/nikolas/odteste/sistemas/Degrau.java

## Import Cycles
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`

## Communities (144 total, 79 thin omitted)

### Community 1 - "Tela"
Cohesion: 0.15
Nodes (4): ConfigCliente, Envelope, Pulso, Tela

### Community 3 - "Busca de Espaço"
Cohesion: 0.07
Nodes (18): Caixa, Chao, Espaco, Mundo, Tipo, AR, COM_DADOS, FORA_DO_MUNDO (+10 more)

### Community 4 - "Aparelhos.java"
Cohesion: 0.05
Nodes (9): Aparelhos, BlocosCapela, Freio, Compat, EsconderNoEmi, OccultismCompat, Sangramento, Itens (+1 more)

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.12
Nodes (3): Grupo, Grupos, GruposTest

### Community 7 - "PrimeiroSinal"
Cohesion: 0.07
Nodes (5): DadosHistoria, Ouvinte, PrimeiroSinal, Resposta, VilaMarcada

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.06
Nodes (39): outputs, runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs (+31 more)

### Community 10 - "Degrau"
Cohesion: 0.09
Nodes (14): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+6 more)

### Community 11 - "net.minecraft.server.level.ServerPlayer"
Cohesion: 0.04
Nodes (18): RitualChamadoDeSangue, DadosJogador, Elemento, CONHECIMENTO, ENERGIA, MORTE, NENHUM, SANGUE (+10 more)

### Community 12 - "tom"
Cohesion: 0.16
Nodes (31): cor(), costura_h(), patchwork(), eh_costura(), p(), regiao(), sementes(), tom() (+23 more)

### Community 13 - "org.junit.jupiter.api.Test"
Cohesion: 0.08
Nodes (4): DadosAnalisadorTest, LimitesTest, RegrasAto1Test, RegrasConteudoTest

### Community 14 - ".contato"
Cohesion: 0.12
Nodes (10): Fonte, APARICAO, CRIATURA_LUTA, CRIATURA_MORTA, CRIATURA_VISTA, DIMENSAO, RELIQUIA_PEGAR, RELIQUIA_USAR (+2 more)

### Community 15 - "EstadoGrupo"
Cohesion: 0.06
Nodes (13): ConfigGatilhos, DadosGatilhos, EstadoGrupo, Gatilhos, Acao, AVISAR_ADMIN, NADA, SOLTAR_PRIMEIRO_SINAL (+5 more)

### Community 17 - "Sistema de Lucidez"
Cohesion: 0.17
Nodes (3): Lucidez, Estado, Vigia

### Community 20 - "Efeito de Ferida"
Cohesion: 0.11
Nodes (3): CuriosCompat, RegrasSangramento, Rubra

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.25
Nodes (3): Alucinacoes, Figura, Porta

### Community 25 - "OdMod"
Cohesion: 0.06
Nodes (9): IdsCena, OdModCliente, IdsDados, OdMod, ClimaVila, MarcaVisivel, Momento, SincronizarJogador (+1 more)

### Community 26 - "RegrasTest"
Cohesion: 0.06
Nodes (6): RegrasExposicao, Resultado, Acumulo, RegrasLucidez, RegrasMarca, RegrasTest

### Community 27 - "RegrasHorror"
Cohesion: 0.10
Nodes (4): Agenda, RegrasHorror, Sorteio, RegrasHorrorTest

### Community 28 - "modulos.py"
Cohesion: 0.09
Nodes (28): capela(), parede(), casa(), parede(), reboco(), casa_do_coveiro(), igreja(), parede() (+20 more)

### Community 29 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.11
Nodes (4): VelaDaCapelaDados, Backup, Restauracao, SinaisNoMundo

### Community 35 - "Código Morse"
Cohesion: 0.12
Nodes (9): Morse, Sequencia, Toque, Fonte, NOTA, SINO, RegrasAto1, Som (+1 more)

### Community 38 - "Padrão de Veias"
Cohesion: 0.21
Nodes (3): PadraoVeias, Regiao, PadraoVeiasTest

### Community 39 - "Alucinacao"
Cohesion: 0.11
Nodes (15): Alucinacao, CISCO, CORACAO, ESCURECER, ESTALO, INCLINAR, MOB_FALSO, NOME (+7 more)

### Community 41 - ".aoTick"
Cohesion: 0.17
Nodes (3): EfeitosLucidez, Sequencia, Sons

### Community 43 - "Integrações com Mods"
Cohesion: 0.13
Nodes (13): Integracao, BOTANIA, CREATE, CURIOS, EMI, FTB_CHUNKS, IRONS, JEI (+5 more)

### Community 45 - "net.minecraft.core.BlockPos"
Cohesion: 0.07
Nodes (12): FonteDeSinal, Fonte, FontesFixas, FonteVilaCalada, LeitorDeItem, Resultado, CHEIO, NOME_INVALIDO (+4 more)

### Community 48 - "net.minecraft.server.MinecraftServer"
Cohesion: 0.25
Nodes (4): Colocacao, Acesso, EmPartes, Memoria

### Community 49 - "vila_calada_150.py"
Cohesion: 0.09
Nodes (40): arvore_morta(), arvores_mortas(), caminho(), caminho_dos_enterros(), campos(), carroca(), cemiterio(), cerca() (+32 more)

### Community 50 - "math"
Cohesion: 0.07
Nodes (29): _rot(), Animacao, _avaliar(), Camera, centro_para(), _chaves(), _ease(), escala_m() (+21 more)

### Community 52 - "ruido"
Cohesion: 0.09
Nodes (34): ruido(), arvore(), arvores_queimadas(), campanario(), campos(), capela(), casa(), casas() (+26 more)

### Community 53 - "Tipos de Momento"
Cohesion: 0.22
Nodes (6): TipoMomento, DEGRAU, MARCA_APARECEU, MARCA_SUBIU, MARCA_TROCOU, SURTO

### Community 54 - "gerar_marca_v2.py"
Cohesion: 0.09
Nodes (29): _caixa(), corpo(), faces(), mapa(), render(), skin_teste(), skins_teste(), carregar_v1() (+21 more)

### Community 57 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.07
Nodes (9): LeitorLivros, Livro, Pagina, Trecho, LivrosHistoria, SimpleJsonResourceReloadListener, Pedaco, RegrasLivros (+1 more)

### Community 75 - "numpy"
Cohesion: 0.08
Nodes (18): faixa(), topo(), amostrar(), _c(), coracao(), corpo_veia(), costura(), _desdobrar() (+10 more)

### Community 76 - "RegrasClimaVila"
Cohesion: 0.09
Nodes (4): ClimaVilaCliente, Pendurado, RegrasClimaVila, RegrasClimaVilaTest

### Community 77 - "gerar_marca.py"
Cohesion: 0.09
Nodes (22): ampliar_molde(), caixa(), corpo(), crescer(), desenhar(), fluxo(), fonte(), Image (+14 more)

### Community 78 - "gerar_png.py"
Cohesion: 0.12
Nodes (26): afim(), amostra(), compor(), cor(), cruz(), cruzar(), cubos_no_mundo(), andar() (+18 more)

### Community 79 - "RegrasSinal"
Cohesion: 0.08
Nodes (15): Direcao, DIREITA, ESQUERDA, FRENTE, FRENTE_DIREITA, FRENTE_ESQUERDA, TRAS, TRAS_DIREITA (+7 more)

### Community 81 - "Estrutura"
Cohesion: 0.07
Nodes (7): Estrutura, nome_completo(), _texto(), construida(), TesteModelos, TesteNBT, TesteVila150

### Community 82 - "sombreado"
Cohesion: 0.21
Nodes (20): _aba(), aba_pintor(), aba_pintor2(), antebraco(), avental(), bota(), calca(), camisa() (+12 more)

### Community 83 - "net.minecraft.resources.ResourceLocation"
Cohesion: 0.20
Nodes (3): Resultado, Estruturas, Tentativa

### Community 84 - "testar.py"
Cohesion: 0.12
Nodes (18): gerar(), main(), _salvar(), main(), recorte(), salvar(), com_chao_do_mundo(), conferir_centro() (+10 more)

### Community 85 - "vila_destruida.py"
Cohesion: 0.12
Nodes (22): Mistura, arvores_queimadas(), casa(), casa_do_livro(), casas(), chao(), chao_queimado(), cinzas() (+14 more)

### Community 86 - "render.py"
Cohesion: 0.13
Nodes (15): hash01(), c(), _amostrador(), cor_px(), _clamp(), _face(), _girar(), _girar_dir() (+7 more)

### Community 87 - "gerar_sons.py"
Cohesion: 0.14
Nodes (10): arrasto(), Biquad, eco(), envelope_bordas(), gravar_wav(), main(), normalizar(), para_ogg() (+2 more)

### Community 89 - ".use"
Cohesion: 0.11
Nodes (10): Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM, INCOMUM (+2 more)

### Community 90 - ".sortearDrop"
Cohesion: 0.12
Nodes (3): Achados, RegrasAchados, RegrasAchadosTest

### Community 91 - "RegrasCena"
Cohesion: 0.13
Nodes (4): Legenda, Quadro, RegrasCena, RegrasCenaTest

### Community 94 - "nbt_io.py"
Cohesion: 0.14
Nodes (13): Byte, Double, _escrever(), Float, Int, _Leitor, ler(), Lista (+5 more)

### Community 95 - ".aoSoltar"
Cohesion: 0.09
Nodes (7): CansacoDoElemento, Destino, CONTA, TIRA, RegrasCansaco, Cansaco, RegrasMateriaTest

### Community 96 - "Cenas"
Cohesion: 0.17
Nodes (3): Ancora, Cenas, CenaEntradaVila

### Community 97 - "montagem.py"
Cohesion: 0.18
Nodes (12): ampliar(), gerar(), main(), arquivo_animacoes(), bbmodel(), grupo(), clamp(), costura_v() (+4 more)

### Community 100 - "hash01"
Cohesion: 0.19
Nodes (18): hash01(), barriga(), cabeca_larva(), canela_pintor(), casco(), cauda_larva(), coxa_pintor(), cume() (+10 more)

### Community 102 - "Cena"
Cohesion: 0.19
Nodes (5): Cena, Plano, Ponto, Som, CenasConhecidas

### Community 104 - "Local"
Cohesion: 0.16
Nodes (4): Local, Pose, RegrasCenaVila, RegrasCenaVilaTest

### Community 112 - "Partes"
Cohesion: 0.19
Nodes (7): Fase, COLOCANDO, CONFERINDO, DESFAZENDO, PAUSADA, PRONTA, Partes

### Community 113 - "blocos.py"
Cohesion: 0.31
Nodes (5): cheio(), curto(), formas(), material(), _verdade()

### Community 115 - "Abalo"
Cohesion: 0.29
Nodes (4): Abalo, APARICAO, BATALHA_RELIQUIA, MOB_SANGUE

### Community 116 - "Criatura"
Cohesion: 0.16
Nodes (3): Criatura, Cubo, Osso

### Community 120 - "EventoEnergia"
Cohesion: 0.25
Nodes (4): EventoEnergia, APAGAO, CASA_OCUPADA, FOME_DA_LINHA

### Community 122 - "capela_fazenda.py"
Cohesion: 0.15
Nodes (14): construir(), desgaste(), exterior(), passagem(), pistas(), sangue(), desgaste(), sangue() (+6 more)

### Community 123 - "Modo"
Cohesion: 0.20
Nodes (4): Modo, COMPLETO, SEM_BALANCO, SO_TEXTO

### Community 125 - "comparacao.py"
Cohesion: 0.33
Nodes (5): carregar(), colunas(), folha(), main(), textura_provisoria()

### Community 126 - "canal"
Cohesion: 0.27
Nodes (7): anims(), animacao(), canal(), anims(), lubdub(), anims(), wiggle()

### Community 141 - "Forca"
Cohesion: 0.40
Nodes (4): Forca, DESLIGADO, LIGADO, SUAVE

## Knowledge Gaps
- **109 isolated node(s):** `CONTA`, `TIRA`, `A_BEIRA`, `ATRAVESSADO`, `COMUM` (+104 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 755 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **79 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lista` connect `nbt_io.py` to `assertequals`?**
  _High betweenness centrality (0.048) - this node is a cross-community bridge._
- **What connects `CONTA`, `TIRA`, `A_BEIRA` to the rest of the system?**
  _109 weakly-connected nodes found - possible missing edges._
- **Should `Tela` be split into smaller, more focused modules?**
  _Cohesion score 0.14624505928853754 - nodes in this community are weakly interconnected._
- **Why does `PrimeiroSinal` connect `PrimeiroSinal` to `Aparelhos.java`, `Grupos de Jogadores`, `PrimeiroSinal.java`, `net.minecraft.server.level.ServerPlayer`, `PistasVilaCalada`, `net.minecraft.server.level.ServerLevel`, `ComandosHistoria.java`, `Leitura do Mundo Real`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **Should `assertequals` be split into smaller, more focused modules?**
  _Cohesion score 0.1223529411764706 - nodes in this community are weakly interconnected._
- **Why does `Alucinacao` connect `Alucinacao` to `assertequals`, `EfeitosLucidez.java`, `.aoTick`, `Degrau`, `RegrasHorror`?**
  _High betweenness centrality (0.032) - this node is a cross-community bridge._
- **Should `Busca de Espaço` be split into smaller, more focused modules?**
  _Cohesion score 0.0727036676403765 - nodes in this community are weakly interconnected._