# Graph Report - bal-tech  (2026-11-13)

## Corpus Check
- 296 files · ~335,963 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 25 file(s) not represented in the graph (top: .nbt 7, (none) 6, .bbmodel 5)

## Summary
- 3351 nodes · 9846 edges · 157 communities (64 shown, 93 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 330 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f69985ef`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- net.minecraft.resources.ResourceLocation
- Tela
- Busca de Espaço
- CenaCliente.java
- Grupos de Jogadores
- Ouvinte do Primeiro Sinal
- Workflow de Compilação CI
- Achados.java
- Faixas de Lucidez
- net.minecraft.world.entity.player.Player
- Ronda.java
- org.junit.jupiter.api.Test
- Fonte
- net.minecraft.server.level.ServerPlayer
- Investida de Ganchos
- Sistema de Lucidez
- Agenda de Horror
- .sincronizar
- EstadoGrupo
- list
- Renderização da Marca
- ClaimsFtbChunks.java
- Alucinações no Cliente
- OdMod.java
- LivrosHistoria
- vila_calada_150.py
- Tipos de Alucinação
- net.minecraft.server.level.ServerLevel
- ComandosHistoria.java
- Leitura do Mundo Real
- CriaturaDeSangue.java
- RegrasConteudoTest.java
- net.minecraft.world.level.block.state.BlockState
- RegrasAto1Test
- .use
- Chuva de Filhotes
- Padrão de Veias
- net.minecraft.core.BlockPos
- Regras de Lucidez
- EfeitosLucidez
- Comandos de Admin
- Integrações com Mods
- Limites de Valores
- ruido
- CriaturaDeSangue
- Marca de Sangue
- net.minecraft.server.MinecraftServer
- Testes de Regras de Elemento
- modulos.py
- math
- Tipos de Momento
- Repetição de Eventos
- Testes de Consistência
- RegrasClimaVila
- Script Baixar Libs
- Script Teste de Servidor
- RegrasSinal
- gerar_marca_v2.py
- veias.py
- retalhado.py
- gerar_png.py
- vila_destruida.py
- Contencao
- Estrutura
- Combate.java
- PistasVilaCalada
- render.py
- net.minecraft.nbt.CompoundTag
- sombreado
- os
- gerar_sons.py
- nbt_io.py
- .sortearDrop
- com.mojang.brigadier.context.CommandContext
- tom
- CenaCliente
- RegrasVilaCalada
- RegrasAnalisadorTest
- montagem.py
- AnalisadorSinalEntidade
- gerar_marca.py
- previa_vila_calada_150.py
- capela_fazenda.py
- testar.py
- Cena
- Cenas
- net.minecraft.client.player.AbstractClientPlayer
- RegrasFonteVilaTest
- Imagem
- corpo.py
- .pose
- .de
- Criatura
- ConfigCliente
- DadosVilaCalada
- RegrasCapelaTest
- RegrasCenaTest
- ClimaVilaCalada
- AnimacaoJogador
- comparacao.py
- .faces
- RegrasCena
- canal
- blocos.py
- LivrosLidos
- RegrasCenaVilaTest
- DadosAnalisadorTest
- desenhar
- SomVento.java
- Ronda
- .colocacoes
- ComandosLivros
- .adicionar
- main
- _Pincel
- CamadaMarca
- Modo
- TochaNaoProtege
- ComandosArtefato
- Forca
- Ruido
- BlocosCapelaIds.java
- IdsGatilhos.java
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
8. `OdMod` - 40 edges
9. `Backup` - 40 edges
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

## Communities (157 total, 93 thin omitted)

### Community 0 - "net.minecraft.resources.ResourceLocation"
Cohesion: 0.14
Nodes (3): Resultado, Estruturas, Tentativa

### Community 1 - "Tela"
Cohesion: 0.15
Nodes (3): Envelope, Pulso, Tela

### Community 3 - "Busca de Espaço"
Cohesion: 0.07
Nodes (18): Caixa, Chao, Espaco, Mundo, Tipo, AR, COM_DADOS, FORA_DO_MUNDO (+10 more)

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.12
Nodes (3): Grupo, Grupos, GruposTest

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.06
Nodes (39): outputs, runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs (+31 more)

### Community 9 - "Achados.java"
Cohesion: 0.05
Nodes (9): Aparelhos, BlocosCapela, Freio, Compat, EsconderNoEmi, EsconderNoJei, OccultismCompat, Itens (+1 more)

### Community 10 - "Faixas de Lucidez"
Cohesion: 0.11
Nodes (15): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+7 more)

### Community 13 - "org.junit.jupiter.api.Test"
Cohesion: 0.06
Nodes (6): RegrasExposicao, Resultado, LimitesTest, ElementoTest, RegrasConteudoTest, RegrasTest

### Community 14 - "Fonte"
Cohesion: 0.17
Nodes (9): Fonte, APARICAO, CRIATURA_LUTA, CRIATURA_MORTA, CRIATURA_VISTA, DIMENSAO, RELIQUIA_PEGAR, RELIQUIA_USAR (+1 more)

### Community 15 - "net.minecraft.server.level.ServerPlayer"
Cohesion: 0.05
Nodes (18): RitualChamadoDeSangue, Elemento, CONHECIMENTO, ENERGIA, MORTE, NENHUM, SANGUE, RegrasElemento (+10 more)

### Community 18 - "Agenda de Horror"
Cohesion: 0.10
Nodes (3): Agenda, Sorteio, RegrasHorrorTest

### Community 20 - "EstadoGrupo"
Cohesion: 0.06
Nodes (13): ConfigGatilhos, DadosGatilhos, EstadoGrupo, Gatilhos, Acao, AVISAR_ADMIN, NADA, SOLTAR_PRIMEIRO_SINAL (+5 more)

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.20
Nodes (3): Alucinacoes, Figura, Porta

### Community 25 - "OdMod.java"
Cohesion: 0.07
Nodes (9): DadosJogador, IdsDados, OdMod, ClimaVila, MarcaVisivel, Momento, Rede, SincronizarJogador (+1 more)

### Community 26 - "LivrosHistoria"
Cohesion: 0.07
Nodes (9): LeitorLivros, Livro, Pagina, Trecho, LivrosHistoria, SimpleJsonResourceReloadListener, Pedaco, RegrasLivros (+1 more)

### Community 27 - "vila_calada_150.py"
Cohesion: 0.09
Nodes (40): arvore_morta(), arvores_mortas(), caminho(), caminho_dos_enterros(), campos(), carroca(), cemiterio(), cerca() (+32 more)

### Community 28 - "Tipos de Alucinação"
Cohesion: 0.11
Nodes (15): Alucinacao, CISCO, CORACAO, ESCURECER, ESTALO, INCLINAR, MOB_FALSO, NOME (+7 more)

### Community 32 - "CriaturaDeSangue.java"
Cohesion: 0.06
Nodes (4): CuriosCompat, Sangramento, Rubra, Paranormal

### Community 34 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.05
Nodes (4): AnalisadorSinalBloco, PocaDeSangue, VelaDaCapela, VelaDaCapelaDados

### Community 35 - "RegrasAto1Test"
Cohesion: 0.08
Nodes (11): Morse, Sequencia, Toque, Ouvinte, Fonte, NOTA, SINO, RegrasAto1 (+3 more)

### Community 36 - ".use"
Cohesion: 0.10
Nodes (10): Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM, INCOMUM (+2 more)

### Community 38 - "Padrão de Veias"
Cohesion: 0.23
Nodes (3): PadraoVeias, Regiao, PadraoVeiasTest

### Community 39 - "net.minecraft.core.BlockPos"
Cohesion: 0.07
Nodes (13): FonteDeSinal, Fonte, FontesFixas, FonteVilaCalada, LeitorDeItem, Resultado, CHEIO, NOME_INVALIDO (+5 more)

### Community 40 - "Regras de Lucidez"
Cohesion: 0.10
Nodes (3): Acumulo, RegrasLucidez, RegrasCombateTest

### Community 41 - "EfeitosLucidez"
Cohesion: 0.14
Nodes (3): EfeitosLucidez, Sequencia, Sons

### Community 43 - "Integrações com Mods"
Cohesion: 0.12
Nodes (14): Integracao, BOTANIA, CREATE, CURIOS, EMI, FTB_CHUNKS, IRONS, JEI (+6 more)

### Community 45 - "ruido"
Cohesion: 0.08
Nodes (35): ruido(), c(), arvore(), arvores_queimadas(), campanario(), campos(), casa(), casas() (+27 more)

### Community 48 - "net.minecraft.server.MinecraftServer"
Cohesion: 0.26
Nodes (4): Colocacao, Acesso, EmPartes, Memoria

### Community 50 - "modulos.py"
Cohesion: 0.09
Nodes (31): capela(), casa(), parede(), desgaste(), casa_do_coveiro(), capela(), desgaste(), igreja() (+23 more)

### Community 52 - "math"
Cohesion: 0.09
Nodes (25): _rot(), Animacao, _avaliar(), Camera, centro_para(), _chaves(), _ease(), escala_m() (+17 more)

### Community 53 - "Tipos de Momento"
Cohesion: 0.20
Nodes (7): Momentos, TipoMomento, DEGRAU, MARCA_APARECEU, MARCA_SUBIU, MARCA_TROCOU, SURTO

### Community 57 - "RegrasClimaVila"
Cohesion: 0.09
Nodes (4): ClimaVilaCliente, Pendurado, RegrasClimaVila, RegrasClimaVilaTest

### Community 75 - "RegrasSinal"
Cohesion: 0.07
Nodes (16): Direcao, DIREITA, ESQUERDA, FRENTE, FRENTE_DIREITA, FRENTE_ESQUERDA, TRAS, TRAS_DIREITA (+8 more)

### Community 76 - "gerar_marca_v2.py"
Cohesion: 0.12
Nodes (22): render(), carregar_v1(), folha_antes_depois(), folha_comparacao_hd(), folha_ferida(), folha_geral(), folha_zoom(), gif_brilho() (+14 more)

### Community 77 - "veias.py"
Cohesion: 0.08
Nodes (16): amostrar(), _c(), coracao(), corpo_veia(), costura(), _desdobrar(), F(), ferida() (+8 more)

### Community 78 - "retalhado.py"
Cohesion: 0.13
Nodes (30): cor(), costura_h(), patchwork(), eh_costura(), p(), regiao(), sementes(), bico_gancho() (+22 more)

### Community 79 - "gerar_png.py"
Cohesion: 0.12
Nodes (26): afim(), amostra(), compor(), cor(), cruz(), cruzar(), cubos_no_mundo(), andar() (+18 more)

### Community 80 - "vila_destruida.py"
Cohesion: 0.11
Nodes (22): Mistura, arvores_queimadas(), casa(), casa_do_livro(), casas(), chao(), chao_queimado(), cinzas() (+14 more)

### Community 83 - "Combate.java"
Cohesion: 0.10
Nodes (7): PecaDeTraje, Combate, Raridade, CHEFE, COMUM, RARA, RegrasCombate

### Community 85 - "render.py"
Cohesion: 0.14
Nodes (14): hash01(), _amostrador(), cor_px(), _clamp(), _face(), _girar(), _girar_dir(), _girar_props() (+6 more)

### Community 86 - "net.minecraft.nbt.CompoundTag"
Cohesion: 0.13
Nodes (8): Fase, COLOCANDO, CONFERINDO, DESFAZENDO, PAUSADA, PRONTA, Partes, VilaMarcada

### Community 87 - "sombreado"
Cohesion: 0.21
Nodes (21): _aba(), aba_pintor(), aba_pintor2(), antebraco(), avental(), bota(), calca(), camisa() (+13 more)

### Community 88 - "os"
Cohesion: 0.17
Nodes (11): gerar(), main(), _salvar(), main(), recorte(), salvar(), main(), salvar() (+3 more)

### Community 89 - "gerar_sons.py"
Cohesion: 0.14
Nodes (10): arrasto(), Biquad, eco(), envelope_bordas(), gravar_wav(), main(), normalizar(), para_ogg() (+2 more)

### Community 90 - "nbt_io.py"
Cohesion: 0.14
Nodes (13): Byte, Double, _escrever(), Float, Int, _Leitor, ler(), Lista (+5 more)

### Community 91 - ".sortearDrop"
Cohesion: 0.12
Nodes (3): Achados, RegrasAchados, RegrasAchadosTest

### Community 93 - "tom"
Cohesion: 0.24
Nodes (18): tom(), barriga(), cabeca_larva(), canela_pintor(), casco(), cauda_larva(), coxa_pintor(), cume() (+10 more)

### Community 97 - "montagem.py"
Cohesion: 0.19
Nodes (12): ampliar(), gerar(), main(), arquivo_animacoes(), bbmodel(), grupo(), clamp(), costura_v() (+4 more)

### Community 99 - "gerar_marca.py"
Cohesion: 0.22
Nodes (11): caixa(), corpo(), crescer(), mapa(), ponto(), regiao_de(), regioes(), render() (+3 more)

### Community 100 - "previa_vila_calada_150.py"
Cohesion: 0.16
Nodes (9): nome_completo(), _texto(), com_chao_do_mundo(), conferir_centro(), main(), recortar(), salvar_nbt(), salvar_png() (+1 more)

### Community 101 - "capela_fazenda.py"
Cohesion: 0.15
Nodes (11): parede(), construir(), exterior(), passagem(), pistas(), reboco(), sangue(), mancha() (+3 more)

### Community 102 - "testar.py"
Cohesion: 0.15
Nodes (3): construida(), TesteModelos, TesteVila150

### Community 103 - "Cena"
Cohesion: 0.18
Nodes (5): Cena, Plano, Ponto, Som, CenasConhecidas

### Community 108 - "corpo.py"
Cohesion: 0.19
Nodes (9): _caixa(), corpo(), faces(), faixa(), mapa(), skin_teste(), skins_teste(), topo() (+1 more)

### Community 111 - "Criatura"
Cohesion: 0.16
Nodes (3): Criatura, Cubo, Osso

### Community 119 - "comparacao.py"
Cohesion: 0.33
Nodes (5): carregar(), colunas(), folha(), main(), textura_provisoria()

### Community 121 - ".faces"
Cohesion: 0.24
Nodes (4): Modelo, visitar(), _retangulos_box_uv(), trans()

### Community 122 - "RegrasCena"
Cohesion: 0.33
Nodes (3): Legenda, Quadro, RegrasCena

### Community 123 - "canal"
Cohesion: 0.27
Nodes (7): anims(), animacao(), canal(), anims(), lubdub(), anims(), wiggle()

### Community 124 - "blocos.py"
Cohesion: 0.31
Nodes (5): cheio(), curto(), formas(), material(), _verdade()

### Community 128 - "desenhar"
Cohesion: 0.25
Nodes (3): desenhar(), fluxo(), profundidade_ponta()

### Community 134 - "main"
Cohesion: 0.47
Nodes (5): ampliar_molde(), fonte(), Image, main(), skin_teste()

### Community 138 - "Modo"
Cohesion: 0.33
Nodes (4): Modo, COMPLETO, SEM_BALANCO, SO_TEXTO

### Community 142 - "Forca"
Cohesion: 0.40
Nodes (4): Forca, DESLIGADO, LIGADO, SUAVE

## Knowledge Gaps
- **108 isolated node(s):** `COMPLETO`, `SEM_BALANCO`, `SO_TEXTO`, `IRONS`, `OCCULTISM` (+103 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 752 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **93 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lista` connect `nbt_io.py` to `list`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **What connects `COMPLETO`, `SEM_BALANCO`, `SO_TEXTO` to the rest of the system?**
  _108 weakly-connected nodes found - possible missing edges._
- **Should `net.minecraft.resources.ResourceLocation` be split into smaller, more focused modules?**
  _Cohesion score 0.1423076923076923 - nodes in this community are weakly interconnected._
- **Why does `Elemento` connect `net.minecraft.server.level.ServerPlayer` to `CriaturaDeSangue.java`, `assertequals`, `CenaCliente.java`, `net.minecraft.client.player.AbstractClientPlayer`, `net.minecraft.world.entity.player.Player`, `Limites de Valores`, `org.junit.jupiter.api.Test`, `CriaturaDeSangue`, `Testes de Regras de Elemento`, `.sincronizar`, `Tipos de Momento`, `list`, `OdMod.java`, `ComandosHistoria.java`?**
  _High betweenness centrality (0.040) - this node is a cross-community bridge._
- **Should `Tela` be split into smaller, more focused modules?**
  _Cohesion score 0.1471861471861472 - nodes in this community are weakly interconnected._
- **Why does `Estrutura` connect `Estrutura` to `previa_vila_calada_150.py`, `capela_fazenda.py`, `testar.py`, `ruido`, `vila_destruida.py`, `os`, `vila_calada_150.py`?**
  _High betweenness centrality (0.037) - this node is a cross-community bridge._
- **Should `assertequals` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._