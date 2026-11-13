# Graph Report - balanceamento-tech  (2026-11-13)

## Corpus Check
- 304 files · ~335,011 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .nbt 7, (none) 6, .bbmodel 5)

## Summary
- 3299 nodes · 9693 edges · 142 communities (69 shown, 73 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 332 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f69985ef`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ComandosHistoria.java
- Animações do Jogador
- list
- Busca de Espaço
- .iniciar
- Grupos de Jogadores
- net.minecraft.server.level.ServerLevel
- Workflow de Compilação CI
- OdMod.java
- Faixas de Lucidez
- ElementoJogador
- org.junit.jupiter.api.Test
- Fonte
- net.minecraft.server.level.ServerPlayer
- Investida de Ganchos
- .exposicao
- LivrosHistoria
- net.minecraft.world.level.block.state.BlockState
- Efeito de Ferida
- CriaturaDeSangue.java
- Renderização da Marca
- ClaimsFtbChunks.java
- Alucinações no Cliente
- OdMod
- RegrasTest
- RegrasHorror
- Tipos de Alucinação
- net.minecraft.core.BlockPos
- modulos.py
- Leitura do Mundo Real
- IA de Monstros
- Spawn da Criatura de Sangue
- Alucinacoes.java
- Código Morse
- Chuva de Filhotes
- Padrão de Veias
- Sinais
- RegrasCapelaTest
- net.minecraft.client.player.LocalPlayer
- net.minecraft.commands.CommandSourceStack
- Integrações com Mods
- .deId
- Esconder Receitas no JEI
- Criatura de Sangue
- Marca de Sangue
- net.minecraft.server.MinecraftServer
- Testes de Regras de Elemento
- vila_calada_150.py
- ruido
- PistasVilaCalada
- AnimacaoJogador.java
- Repetição de Eventos
- Testes de Consistência
- math
- Script Baixar Libs
- Script Teste de Servidor
- gerar_marca_v2.py
- previa_vila_calada_150.py
- veias.py
- RegrasClimaVila
- AnalisadorSinalEntidade.java
- EstadoGrupo
- gerar_png.py
- tom
- .sortearDrop
- net.minecraft.resources.ResourceLocation
- Estrutura
- vila_destruida.py
- render.py
- sombreado
- gerar_sons.py
- nbt_io.py
- ClimaVilaCliente
- Cenas
- com.mojang.brigadier.context.CommandContext
- CenaCliente
- RegrasCena
- montagem.py
- sanguessuga_mae.py
- DadosGatilhos
- .aoTick
- com.mojang.brigadier.builder.LiteralArgumentBuilder
- RegrasCenaTest
- RegrasAnalisadorTest
- Direcao
- gerar_marca.py
- RegrasArtefato
- testar.py
- Cena
- RegrasFonteVilaTest
- Imagem
- corpo.py
- .de
- Criatura
- Partes
- comparacao.py
- .faces
- canal
- blocos.py
- Modo
- RegrasCenaVilaTest
- desenhar
- SomVento.java
- .colocacoes
- main
- _Pincel
- mancha
- estrutura.py
- Ruido
- p
- Acao
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
3. `tom()` - 53 edges
4. `ruido()` - 52 edges
5. `DadosJogador` - 49 edges
6. `DadosHistoria` - 41 edges
7. `Estrutura` - 40 edges
8. `Backup` - 40 edges
9. `OdMod` - 39 edges
10. `ComandosHistoria` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Image` --uses--> `Imagem`  [INFERRED]
  arte/marca-sangue/gerar_marca.py → arte/marca-sangue/imagem_simples.py
- `mat()` --calls--> `sombreado()`  [EXTRACTED]
  arte/previas/criaturas/fonte/sanguessuga_mae.py → arte/previas/criaturas/fonte/montagem.py
- `TesteVila150` --uses--> `Estrutura`  [INFERRED]
  ferramentas/estruturas/testar.py → ferramentas/estruturas/estrutura.py
- `main()` --calls--> `skins_teste()`  [EXTRACTED]
  arte/marca-sangue/v2/gerar_marca_v2.py → arte/marca-sangue/v2/corpo.py
- `main()` --calls--> `Molde`  [EXTRACTED]
  arte/marca-sangue/v2/gerar_marca_v2.py → arte/marca-sangue/v2/veias.py

## Import Cycles
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`

## Communities (142 total, 73 thin omitted)

### Community 0 - "ComandosHistoria.java"
Cohesion: 0.09
Nodes (5): ComandosArtefato, IdsCena, DadosHistoria, IdsHistoria, IdsVilaCalada

### Community 1 - "Animações do Jogador"
Cohesion: 0.08
Nodes (9): ConfigCliente, Forca, DESLIGADO, LIGADO, SUAVE, MomentosCliente, Envelope, Pulso (+1 more)

### Community 3 - "Busca de Espaço"
Cohesion: 0.07
Nodes (18): Caixa, Chao, Espaco, Mundo, Tipo, AR, COM_DADOS, FORA_DO_MUNDO (+10 more)

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.12
Nodes (3): Grupo, Grupos, GruposTest

### Community 7 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.08
Nodes (3): PrimeiroSinal, Resposta, VilaMarcada

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.06
Nodes (39): outputs, runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs (+31 more)

### Community 9 - "OdMod.java"
Cohesion: 0.04
Nodes (9): Aparelhos, BlocosCapela, ConteudoCliente, Compat, EsconderNoEmi, OccultismCompat, Sangramento, Itens (+1 more)

### Community 10 - "Faixas de Lucidez"
Cohesion: 0.11
Nodes (14): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+6 more)

### Community 13 - "org.junit.jupiter.api.Test"
Cohesion: 0.09
Nodes (3): DadosAnalisadorTest, RegrasAto1Test, RegrasConteudoTest

### Community 14 - "Fonte"
Cohesion: 0.17
Nodes (9): Fonte, APARICAO, CRIATURA_LUTA, CRIATURA_MORTA, CRIATURA_VISTA, DIMENSAO, RELIQUIA_PEGAR, RELIQUIA_USAR (+1 more)

### Community 15 - "net.minecraft.server.level.ServerPlayer"
Cohesion: 0.05
Nodes (14): Freio, DadosJogador, Elemento, CONHECIMENTO, ENERGIA, MORTE, NENHUM, SANGUE (+6 more)

### Community 17 - ".exposicao"
Cohesion: 0.05
Nodes (17): Limites, Abalo, APARICAO, BATALHA_RELIQUIA, MOB_SANGUE, Lucidez, Momentos, Reacoes (+9 more)

### Community 18 - "LivrosHistoria"
Cohesion: 0.06
Nodes (10): LeitorLivros, Livro, Pagina, Trecho, LivrosHistoria, SimpleJsonResourceReloadListener, LivrosLidos, Pedaco (+2 more)

### Community 19 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.08
Nodes (4): AnalisadorSinalBloco, PocaDeSangue, VelaDaCapela, VelaDaCapelaDados

### Community 20 - "Efeito de Ferida"
Cohesion: 0.10
Nodes (3): CuriosCompat, RegrasSangramento, Rubra

### Community 21 - "CriaturaDeSangue.java"
Cohesion: 0.08
Nodes (3): RitualChamadoDeSangue, IdsDados, Exposicao

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.20
Nodes (3): Alucinacoes, Figura, Porta

### Community 25 - "OdMod"
Cohesion: 0.08
Nodes (7): OdModCliente, OdMod, ClimaVila, MarcaVisivel, Momento, SincronizarJogador, TocarCena

### Community 26 - "RegrasTest"
Cohesion: 0.06
Nodes (5): RegrasExposicao, Resultado, Acumulo, RegrasLucidez, RegrasTest

### Community 27 - "RegrasHorror"
Cohesion: 0.10
Nodes (4): Agenda, RegrasHorror, Sorteio, RegrasHorrorTest

### Community 28 - "Tipos de Alucinação"
Cohesion: 0.10
Nodes (15): Alucinacao, CISCO, CORACAO, ESCURECER, ESTALO, INCLINAR, MOB_FALSO, NOME (+7 more)

### Community 30 - "modulos.py"
Cohesion: 0.07
Nodes (39): capela(), parede(), casa(), parede(), construir(), desgaste(), exterior(), passagem() (+31 more)

### Community 35 - "Código Morse"
Cohesion: 0.11
Nodes (10): Morse, Sequencia, Toque, Ouvinte, Fonte, NOTA, SINO, RegrasAto1 (+2 more)

### Community 38 - "Padrão de Veias"
Cohesion: 0.21
Nodes (3): PadraoVeias, Regiao, PadraoVeiasTest

### Community 39 - "Sinais"
Cohesion: 0.06
Nodes (13): FonteDeSinal, Fonte, FontesFixas, FonteVilaCalada, LeitorDeItem, Resultado, CHEIO, NOME_INVALIDO (+5 more)

### Community 43 - "Integrações com Mods"
Cohesion: 0.13
Nodes (13): Integracao, BOTANIA, CREATE, CURIOS, EMI, FTB_CHUNKS, IRONS, JEI (+5 more)

### Community 48 - "net.minecraft.server.MinecraftServer"
Cohesion: 0.32
Nodes (3): Colocacao, EmPartes, Memoria

### Community 50 - "vila_calada_150.py"
Cohesion: 0.09
Nodes (39): arvore_morta(), arvores_mortas(), caminho(), caminho_dos_enterros(), campos(), carroca(), cemiterio(), cerca() (+31 more)

### Community 51 - "ruido"
Cohesion: 0.08
Nodes (35): Mistura, ruido(), paiol(), arvore(), arvores_queimadas(), campanario(), campos(), casa() (+27 more)

### Community 52 - "PistasVilaCalada"
Cohesion: 0.08
Nodes (5): DadosVilaCalada, Estado, PistasVilaCalada, RegrasVilaCalada, RegrasVilaCaladaTest

### Community 53 - "AnimacaoJogador.java"
Cohesion: 0.08
Nodes (3): AnimacaoJogador, BalanceamentoTechTest, Leitor

### Community 57 - "math"
Cohesion: 0.09
Nodes (25): _rot(), Animacao, _avaliar(), Camera, centro_para(), _chaves(), _ease(), escala_m() (+17 more)

### Community 75 - "gerar_marca_v2.py"
Cohesion: 0.12
Nodes (22): render(), carregar_v1(), folha_antes_depois(), folha_comparacao_hd(), folha_ferida(), folha_geral(), folha_zoom(), gif_brilho() (+14 more)

### Community 76 - "previa_vila_calada_150.py"
Cohesion: 0.12
Nodes (18): gerar(), main(), _salvar(), main(), recorte(), salvar(), com_chao_do_mundo(), conferir_centro() (+10 more)

### Community 77 - "veias.py"
Cohesion: 0.08
Nodes (16): amostrar(), _c(), coracao(), corpo_veia(), costura(), _desdobrar(), F(), ferida() (+8 more)

### Community 78 - "RegrasClimaVila"
Cohesion: 0.09
Nodes (4): ClimaVilaCalada, Enviado, RegrasClimaVila, RegrasClimaVilaTest

### Community 79 - "AnalisadorSinalEntidade.java"
Cohesion: 0.09
Nodes (8): AnalisadorSinalEntidade, BateriaAparelho, Intensidade, FORTE, FRACA, MEDIA, NENHUMA, RegrasSinal

### Community 80 - "EstadoGrupo"
Cohesion: 0.17
Nodes (5): EstadoGrupo, Grupo, Momento, RegrasGatilhos, RegrasGatilhosTest

### Community 81 - "gerar_png.py"
Cohesion: 0.12
Nodes (26): afim(), amostra(), compor(), cor(), cruz(), cruzar(), cubos_no_mundo(), andar() (+18 more)

### Community 82 - "tom"
Cohesion: 0.18
Nodes (27): cor(), costura_h(), patchwork(), tom(), bico_gancho(), cabeca_pintor(), camadas(), cavidade() (+19 more)

### Community 83 - ".sortearDrop"
Cohesion: 0.10
Nodes (4): Achados, RegrasAchados, Ronda, RegrasAchadosTest

### Community 84 - "net.minecraft.resources.ResourceLocation"
Cohesion: 0.22
Nodes (3): Resultado, Estruturas, Tentativa

### Community 86 - "vila_destruida.py"
Cohesion: 0.13
Nodes (21): arvores_queimadas(), casa(), casa_do_livro(), casas(), chao(), chao_queimado(), cinzas(), conectar() (+13 more)

### Community 87 - "render.py"
Cohesion: 0.13
Nodes (15): hash01(), c(), _amostrador(), cor_px(), _clamp(), _face(), _girar(), _girar_dir() (+7 more)

### Community 88 - "sombreado"
Cohesion: 0.21
Nodes (21): _aba(), aba_pintor(), aba_pintor2(), antebraco(), avental(), bota(), calca(), camisa() (+13 more)

### Community 89 - "gerar_sons.py"
Cohesion: 0.14
Nodes (10): arrasto(), Biquad, eco(), envelope_bordas(), gravar_wav(), main(), normalizar(), para_ogg() (+2 more)

### Community 90 - "nbt_io.py"
Cohesion: 0.14
Nodes (13): Byte, Double, _escrever(), Float, Int, _Leitor, ler(), Lista (+5 more)

### Community 92 - "Cenas"
Cohesion: 0.16
Nodes (3): Ancora, Cenas, CenaEntradaVila

### Community 95 - "RegrasCena"
Cohesion: 0.20
Nodes (4): Local, Pose, Quadro, RegrasCena

### Community 96 - "montagem.py"
Cohesion: 0.18
Nodes (12): ampliar(), gerar(), main(), arquivo_animacoes(), bbmodel(), grupo(), clamp(), costura_v() (+4 more)

### Community 97 - "sanguessuga_mae.py"
Cohesion: 0.19
Nodes (17): barriga(), cabeca_larva(), canela_pintor(), casco(), cauda_larva(), coxa_pintor(), cume(), dente() (+9 more)

### Community 103 - "Direcao"
Cohesion: 0.12
Nodes (10): Direcao, DIREITA, ESQUERDA, FRENTE, FRENTE_DIREITA, FRENTE_ESQUERDA, TRAS, TRAS_DIREITA (+2 more)

### Community 104 - "gerar_marca.py"
Cohesion: 0.22
Nodes (11): caixa(), corpo(), crescer(), mapa(), ponto(), regiao_de(), regioes(), render() (+3 more)

### Community 105 - "RegrasArtefato"
Cohesion: 0.14
Nodes (10): Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM, INCOMUM (+2 more)

### Community 106 - "testar.py"
Cohesion: 0.17
Nodes (3): construida(), TesteModelos, TesteVila150

### Community 107 - "Cena"
Cohesion: 0.19
Nodes (5): Cena, Plano, Ponto, Som, CenasConhecidas

### Community 110 - "corpo.py"
Cohesion: 0.19
Nodes (9): _caixa(), corpo(), faces(), faixa(), mapa(), skin_teste(), skins_teste(), topo() (+1 more)

### Community 113 - "Criatura"
Cohesion: 0.16
Nodes (3): Criatura, Cubo, Osso

### Community 114 - "Partes"
Cohesion: 0.19
Nodes (7): Fase, COLOCANDO, CONFERINDO, DESFAZENDO, PAUSADA, PRONTA, Partes

### Community 116 - "comparacao.py"
Cohesion: 0.33
Nodes (5): carregar(), colunas(), folha(), main(), textura_provisoria()

### Community 117 - ".faces"
Cohesion: 0.24
Nodes (4): Modelo, visitar(), _retangulos_box_uv(), trans()

### Community 118 - "canal"
Cohesion: 0.27
Nodes (7): anims(), animacao(), canal(), anims(), lubdub(), anims(), wiggle()

### Community 119 - "blocos.py"
Cohesion: 0.31
Nodes (5): cheio(), curto(), formas(), material(), _verdade()

### Community 120 - "Modo"
Cohesion: 0.22
Nodes (4): Modo, COMPLETO, SEM_BALANCO, SO_TEXTO

### Community 122 - "desenhar"
Cohesion: 0.25
Nodes (3): desenhar(), fluxo(), profundidade_ponta()

### Community 125 - "main"
Cohesion: 0.47
Nodes (5): ampliar_molde(), fonte(), Image, main(), skin_teste()

### Community 127 - "mancha"
Cohesion: 0.40
Nodes (4): sangue(), mancha(), _pingo(), rastro()

### Community 130 - "p"
Cohesion: 0.67
Nodes (4): eh_costura(), p(), regiao(), sementes()

### Community 131 - "Acao"
Cohesion: 0.50
Nodes (4): Acao, AVISAR_ADMIN, NADA, SOLTAR_PRIMEIRO_SINAL

## Knowledge Gaps
- **104 isolated node(s):** `COMPLETO`, `SEM_BALANCO`, `SO_TEXTO`, `CONFERINDO`, `COLOCANDO` (+99 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 740 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **73 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lista` connect `nbt_io.py` to `list`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **What connects `COMPLETO`, `SEM_BALANCO`, `SO_TEXTO` to the rest of the system?**
  _104 weakly-connected nodes found - possible missing edges._
- **Should `ComandosHistoria.java` be split into smaller, more focused modules?**
  _Cohesion score 0.09210526315789473 - nodes in this community are weakly interconnected._
- **Why does `Elemento` connect `net.minecraft.server.level.ServerPlayer` to `ComandosHistoria.java`, `Animações do Jogador`, `Alucinacoes.java`, `list`, `map`, `ElementoJogador`, `.deId`, `.exposicao`, `Testes de Regras de Elemento`, `CriaturaDeSangue.java`, `Repetição de Eventos`, `OdMod`, `RegrasTest`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Should `Animações do Jogador` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._
- **Why does `Estrutura` connect `Estrutura` to `estrutura.py`, `testar.py`, `previa_vila_calada_150.py`, `vila_calada_150.py`, `ruido`, `vila_destruida.py`, `modulos.py`?**
  _High betweenness centrality (0.039) - this node is a cross-community bridge._
- **Should `list` be split into smaller, more focused modules?**
  _Cohesion score 0.09562841530054644 - nodes in this community are weakly interconnected._