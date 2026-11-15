# Graph Report - od-dobre  (2026-11-14)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 4346 nodes · 12959 edges · 174 communities (80 shown, 94 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 364 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `88d3db35`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OdMod.java
- list
- OdMod
- Busca de Espaço
- net.minecraft.core.BlockPos
- Grupos de Jogadores
- Caixa
- Ouvinte do Primeiro Sinal
- Workflow de Compilação CI
- resourcelocation
- Faixas de Lucidez
- RegrasHorror
- LivrosHistoria
- assertequals
- Dobre.java
- modulos.py
- Investida de Ganchos
- Sistema de Lucidez
- Agenda de Horror
- math
- ruido
- gerar_marca_v2.py
- Renderização da Marca
- Sinais
- Alucinações no Cliente
- net.minecraft.world.level.Level
- os
- net.minecraft.world.item.ItemStack
- Tipos de Alucinação
- numpy
- gerar_marca.py
- Leitura do Mundo Real
- RegrasSinal
- net.minecraft.server.MinecraftServer
- with
- previa_shader.py
- .reiniciar
- Chuva de Filhotes
- Padrão de Veias
- gerar_png.py
- Regras de Lucidez
- Estrutura
- Comandos de Admin
- Integrações com Mods
- Limites de Valores
- previa_sangue.py
- net.minecraft.world.level.block.Rotation
- Marca de Sangue
- Grupos
- Testes de Regras de Elemento
- MagiaTalhoRubro.java
- RegrasHorrorTest
- Mestre
- Tipos de Momento
- Contencao
- Testes de Consistência
- Script Gradle Wrapper
- Paranormal
- Script Baixar Libs
- Script Teste de Servidor
- Build Gradle
- Settings Gradle
- Pacote Cliente
- Pacote Comandos
- Pacote Criaturas
- Pacote Elementos
- Pacote Estruturas
- Pacote História
- Pacote Itens
- Pacote Magias
- Pacote Marcas
- Pacote Rede
- Pacote Rituais
- Pacote Sistemas
- Cenas
- Retalhado
- RegrasVilaCalada
- TalhoRubro
- tom
- .registrar
- RegrasCena
- testar.py
- CenaCliente
- RegrasAnalisadorTest
- Integracao
- .aoTick
- montagem.py
- Cena
- net.minecraft.world.entity.Mob
- .tremor
- .contato
- AnalisadorSinalEntidade
- LivrosLidos
- RegrasArtefato
- ClimaVilaCalada
- Diario
- AnimacaoJogador
- net.neoforged.neoforge.common.ModConfigSpec
- net.minecraft.client.player.AbstractClientPlayer
- CamadaHorror
- RegrasFonteVilaTest
- Imagem
- Criatura
- FxSangue
- RegrasSusto
- RegrasCenaTest
- .passo
- comparacao.py
- Obra
- Quadro
- .forca
- TipoSusto
- canal
- blocos.py
- FxGravar
- .podeNascer
- RegrasCenaVilaTest
- RegrasSangue
- BrilhoLodestone.java
- Modo
- FalhaMinimapa
- DadosGatilhos
- RegrasPrepararVilaTest
- SomVento.java
- .aoAbrirLivro
- FxCorHorror
- ComandosFx
- EventoEnergia
- ModeloCriatura.java
- .colocacoes
- .adicionar
- TipoMomento
- ConsistenciaTest
- .estado
- FonteHorror
- PackIris
- .rodar
- BlocosCapelaIds.java
- MinimapaJourneyMap.java
- MinimapaXaero.java
- IdsMestre.java
- baixar_libs.sh
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
- teste_servidor.sh

## God Nodes (most connected - your core abstractions)
1. `PrimeiroSinal` - 66 edges
2. `Elemento` - 64 edges
3. `ruido()` - 63 edges
4. `DadosJogador` - 60 edges
5. `OdMod` - 58 edges
6. `tom()` - 53 edges
7. `Dobre` - 48 edges
8. `DadosHistoria` - 43 edges
9. `Estrutura` - 42 edges
10. `Backup` - 40 edges

## Surprising Connections (you probably didn't know these)
- `Image` --uses--> `Imagem`  [INFERRED]
  arte/marca-sangue/gerar_marca.py → arte/marca-sangue/imagem_simples.py
- `Obra` --uses--> `Mistura`  [INFERRED]
  ferramentas/estruturas/modelos/vila_calada_150_v2.py → ferramentas/estruturas/estrutura.py
- `Previa` --uses--> `Efeito`  [INFERRED]
  ferramentas/fx/previa_sangue.py → ferramentas/fx/previa_shader.py
- `TesteVila150` --uses--> `Estrutura`  [INFERRED]
  ferramentas/estruturas/testar.py → ferramentas/estruturas/estrutura.py
- `mat()` --calls--> `sombreado()`  [EXTRACTED]
  arte/previas/criaturas/fonte/sanguessuga_mae.py → arte/previas/criaturas/fonte/montagem.py

## Import Cycles
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150_v2.py -> ferramentas/estruturas/modelos/__init__.py`
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`
- 3-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150_v2.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`

## Communities (174 total, 94 thin omitted)

### Community 0 - "OdMod.java"
Cohesion: 0.03
Nodes (11): ComandosArtefato, Aparelhos, BlocosCapela, Freio, Compat, EsconderNoEmi, OccultismCompat, Criaturas (+3 more)

### Community 1 - "list"
Cohesion: 0.07
Nodes (3): IdsCena, IdsHistoria, IdsVilaCalada

### Community 2 - "OdMod"
Cohesion: 0.03
Nodes (5): CamadaMarca, IronsCompat, EsconderNoJei, IdsMagia, OdMod

### Community 3 - "Busca de Espaço"
Cohesion: 0.05
Nodes (13): Cansaco, DadosJogador, Limites, Elemento, CONHECIMENTO, ENERGIA, MORTE, NENHUM (+5 more)

### Community 4 - "net.minecraft.core.BlockPos"
Cohesion: 0.07
Nodes (9): Ronda, DadosVilaCalada, Agendado, Corrente, Dobre, DobreEvento, Erro, Feito (+1 more)

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.06
Nodes (6): AnalisadorSinalBloco, PocaDeSangue, RegrasCapela, VelaDaCapela, VelaDaCapelaDados, RegrasCapelaTest

### Community 6 - "Caixa"
Cohesion: 0.07
Nodes (18): Caixa, Chao, Espaco, Mundo, Tipo, AR, COM_DADOS, FORA_DO_MUNDO (+10 more)

### Community 7 - "Ouvinte do Primeiro Sinal"
Cohesion: 0.05
Nodes (14): Toque, AgulhaDeCerzir, Artefato, DedalDentado, SinoDeMatadouro, Abalo, APARICAO, BATALHA_RELIQUIA (+6 more)

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.07
Nodes (5): MundoReal, DadosHistoria, PrimeiroSinal, Resposta, VilaMarcada

### Community 9 - "resourcelocation"
Cohesion: 0.06
Nodes (11): IdsHorror, AreaHorror, ClimaVila, EfeitoFx, FaseMagia, MarcaVisivel, Momento, PenduradoSino (+3 more)

### Community 10 - "Faixas de Lucidez"
Cohesion: 0.05
Nodes (7): DadosAnalisadorTest, LimitesTest, ElementoTest, RegrasElementoTest, RegrasAto1Test, RegrasTalhoRubroTest, RegrasConteudoTest

### Community 11 - "RegrasHorror"
Cohesion: 0.06
Nodes (30): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+22 more)

### Community 12 - "LivrosHistoria"
Cohesion: 0.06
Nodes (9): LeitorLivros, Livro, Pagina, Trecho, LivrosHistoria, SimpleJsonResourceReloadListener, Pedaco, RegrasLivros (+1 more)

### Community 14 - "Dobre.java"
Cohesion: 0.06
Nodes (3): RitualChamadoDeSangue, Sangramento, Exposicao

### Community 15 - "modulos.py"
Cohesion: 0.07
Nodes (39): capela(), parede(), casa(), parede(), construir(), desgaste(), exterior(), passagem() (+31 more)

### Community 16 - "Investida de Ganchos"
Cohesion: 0.09
Nodes (40): arvore_morta(), arvores_mortas(), caminho(), caminho_dos_enterros(), campos(), carroca(), cemiterio(), cerca() (+32 more)

### Community 17 - "Sistema de Lucidez"
Cohesion: 0.05
Nodes (6): RegrasExposicao, Resultado, Acumulo, RegrasLucidez, RegrasMarca, RegrasTest

### Community 18 - "Agenda de Horror"
Cohesion: 0.08
Nodes (7): Momento, AREA_DIA, MUNDO, NADA, NOITE, RegrasCamadaHorror, RegrasCamadaHorrorTest

### Community 19 - "math"
Cohesion: 0.07
Nodes (28): Animacao, _avaliar(), Camera, centro_para(), _chaves(), _ease(), escala_m(), faixa_titulo() (+20 more)

### Community 20 - "ruido"
Cohesion: 0.08
Nodes (35): ruido(), c(), arvore(), arvores_queimadas(), campanario(), campos(), capela(), casa() (+27 more)

### Community 21 - "gerar_marca_v2.py"
Cohesion: 0.09
Nodes (30): _caixa(), corpo(), faces(), mapa(), render(), _rot(), skin_teste(), skins_teste() (+22 more)

### Community 23 - "Sinais"
Cohesion: 0.07
Nodes (12): FonteDeSinal, Fonte, FontesFixas, FonteVilaCalada, LeitorDeItem, Resultado, CHEIO, NOME_INVALIDO (+4 more)

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.08
Nodes (15): Andamento, Fim, CONTA_ERRADA, FALTOU_SINEIRO, FEITO, RegrasDobre, Toque, A_MAIS (+7 more)

### Community 25 - "net.minecraft.world.level.Level"
Cohesion: 0.08
Nodes (3): ClaimsFtbChunks, Protecao, Dobre

### Community 26 - "os"
Cohesion: 0.11
Nodes (21): gerar(), main(), _salvar(), main(), recorte(), salvar(), com_chao_do_mundo(), conferir_centro() (+13 more)

### Community 28 - "Tipos de Alucinação"
Cohesion: 0.11
Nodes (31): aberto(), adro(), arvore_morta(), caixas_viradas(), carroca_virada(), casas(), chao(), chao_y() (+23 more)

### Community 29 - "numpy"
Cohesion: 0.09
Nodes (18): faixa(), topo(), amostrar(), _c(), coracao(), corpo_veia(), costura(), _desdobrar() (+10 more)

### Community 30 - "gerar_marca.py"
Cohesion: 0.09
Nodes (23): ampliar_molde(), caixa(), corpo(), crescer(), desenhar(), fluxo(), fonte(), Image (+15 more)

### Community 31 - "Leitura do Mundo Real"
Cohesion: 0.09
Nodes (3): Gota, MagiaCliente, Mancha

### Community 32 - "RegrasSinal"
Cohesion: 0.07
Nodes (16): Direcao, DIREITA, ESQUERDA, FRENTE, FRENTE_DIREITA, FRENTE_ESQUERDA, TRAS, TRAS_DIREITA (+8 more)

### Community 33 - "net.minecraft.server.MinecraftServer"
Cohesion: 0.13
Nodes (11): Colocacao, Fase, COLOCANDO, CONFERINDO, DESFAZENDO, PAUSADA, PRONTA, Partes (+3 more)

### Community 34 - "with"
Cohesion: 0.06
Nodes (39): outputs, runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs (+31 more)

### Community 35 - "previa_shader.py"
Cohesion: 0.09
Nodes (13): CenaDia, forca_visual(), lado_a_lado(), main(), Previa, Cena, dir_de(), Efeito (+5 more)

### Community 36 - ".reiniciar"
Cohesion: 0.09
Nodes (10): Morse, Sequencia, Toque, Ouvinte, Fonte, NOTA, SINO, RegrasAto1 (+2 more)

### Community 37 - "Chuva de Filhotes"
Cohesion: 0.09
Nodes (3): CamadaHorrorCliente, FonteHorror, LuzDaNoite

### Community 38 - "Padrão de Veias"
Cohesion: 0.13
Nodes (30): cor(), costura_h(), patchwork(), eh_costura(), p(), regiao(), sementes(), bico_gancho() (+22 more)

### Community 39 - "gerar_png.py"
Cohesion: 0.12
Nodes (26): afim(), amostra(), compor(), cor(), cruz(), cruzar(), cubos_no_mundo(), andar() (+18 more)

### Community 40 - "Regras de Lucidez"
Cohesion: 0.11
Nodes (24): Mistura, arvores_queimadas(), casa(), casa_do_livro(), casas(), chao(), chao_queimado(), cinzas() (+16 more)

### Community 41 - "Estrutura"
Cohesion: 0.10
Nodes (4): Estrutura, nome_completo(), _texto(), TesteNBT

### Community 44 - "Limites de Valores"
Cohesion: 0.09
Nodes (3): CuriosCompat, RegrasSangramento, Rubra

### Community 45 - "previa_sangue.py"
Cohesion: 0.11
Nodes (12): CenaVila, batida(), contexto(), curva_sangue(), folha(), folha_parede(), main(), parede() (+4 more)

### Community 46 - "net.minecraft.world.level.block.Rotation"
Cohesion: 0.19
Nodes (3): Resultado, Estruturas, Tentativa

### Community 48 - "Grupos"
Cohesion: 0.16
Nodes (3): Grupo, Grupos, GruposTest

### Community 49 - "Testes de Regras de Elemento"
Cohesion: 0.19
Nodes (9): Fim, BLOQUEADO, DONO, JA, LONGE, POSTO, PrepararVilaCalada, Peca (+1 more)

### Community 51 - "RegrasHorrorTest"
Cohesion: 0.13
Nodes (3): Agenda, Sorteio, RegrasHorrorTest

### Community 52 - "Mestre"
Cohesion: 0.11
Nodes (3): Mestre, RegrasMestre, RegrasMestreTest

### Community 53 - "Tipos de Momento"
Cohesion: 0.10
Nodes (4): ClimaVilaCliente, ConteudoCliente, OdModCliente, Pendurado

### Community 55 - "Testes de Consistência"
Cohesion: 0.17
Nodes (4): EstadoGrupo, Grupo, Momento, RegrasGatilhosTest

### Community 57 - "Paranormal"
Cohesion: 0.10
Nodes (8): Combate, Paranormal, Raridade, CHEFE, COMUM, RARA, RegrasCombate, RegrasCombateTest

### Community 58 - "Script Baixar Libs"
Cohesion: 0.14
Nodes (3): Alucinacoes, Figura, Porta

### Community 60 - "Build Gradle"
Cohesion: 0.12
Nodes (3): RegrasFxSangue, RegrasCorHorror, RegrasFxSangueTest

### Community 63 - "Pacote Comandos"
Cohesion: 0.11
Nodes (6): CansacoDoElemento, Destino, CONTA, TIRA, RegrasCansaco, RegrasMateriaTest

### Community 64 - "Pacote Criaturas"
Cohesion: 0.09
Nodes (12): Canto, AUTOMATICO, BAIXO_DIREITA, BAIXO_ESQUERDA, CIMA_DIREITA, CIMA_ESQUERDA, NENHUM, ConfigCliente (+4 more)

### Community 66 - "Pacote Elementos"
Cohesion: 0.14
Nodes (14): hash01(), _amostrador(), cor_px(), _clamp(), _face(), _girar(), _girar_dir(), _girar_props() (+6 more)

### Community 68 - "Pacote História"
Cohesion: 0.21
Nodes (21): _aba(), aba_pintor(), aba_pintor2(), antebraco(), avental(), bota(), calca(), camisa() (+13 more)

### Community 69 - "Pacote Itens"
Cohesion: 0.14
Nodes (10): arrasto(), Biquad, eco(), envelope_bordas(), gravar_wav(), main(), normalizar(), para_ogg() (+2 more)

### Community 71 - "Pacote Marcas"
Cohesion: 0.14
Nodes (13): Byte, Double, _escrever(), Float, Int, _Leitor, ler(), Lista (+5 more)

### Community 73 - "Pacote Rituais"
Cohesion: 0.14
Nodes (7): Atlas, _cantos(), Cena, _olhar(), _orto(), _perspectiva(), _uv()

### Community 74 - "Pacote Sistemas"
Cohesion: 0.13
Nodes (3): Achados, RegrasAchados, RegrasAchadosTest

### Community 75 - "Cenas"
Cohesion: 0.17
Nodes (3): Ancora, Cenas, CenaEntradaVila

### Community 78 - "TalhoRubro"
Cohesion: 0.14
Nodes (10): Magias, Resultado, CONJUROU, DESCONHECIDA, OCUPADO, RECARGA, SEM_LUCIDEZ, AplicarDano (+2 more)

### Community 79 - "tom"
Cohesion: 0.24
Nodes (18): tom(), barriga(), cabeca_larva(), canela_pintor(), casco(), cauda_larva(), coxa_pintor(), cume() (+10 more)

### Community 81 - "RegrasCena"
Cohesion: 0.19
Nodes (4): Local, Pose, Quadro, RegrasCena

### Community 82 - "testar.py"
Cohesion: 0.14
Nodes (4): construida(), TesteModelos, TesteVila150, TesteVila150V2

### Community 85 - "Integracao"
Cohesion: 0.10
Nodes (18): Integracao, BOTANIA, CREATE, CURIOS, EMI, FTB_CHUNKS, IRIS, IRONS (+10 more)

### Community 88 - "montagem.py"
Cohesion: 0.19
Nodes (12): ampliar(), gerar(), main(), arquivo_animacoes(), bbmodel(), grupo(), clamp(), costura_v() (+4 more)

### Community 89 - "Cena"
Cohesion: 0.16
Nodes (6): Cena, Legenda, Plano, Ponto, Som, CenasConhecidas

### Community 91 - ".tremor"
Cohesion: 0.19
Nodes (3): Envelope, Pulso, Tela

### Community 92 - ".contato"
Cohesion: 0.12
Nodes (10): Fonte, APARICAO, CRIATURA_LUTA, CRIATURA_MORTA, CRIATURA_VISTA, DIMENSAO, RELIQUIA_PEGAR, RELIQUIA_USAR (+2 more)

### Community 95 - "RegrasArtefato"
Cohesion: 0.14
Nodes (10): Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM, INCOMUM (+2 more)

### Community 105 - "Criatura"
Cohesion: 0.16
Nodes (3): Criatura, Cubo, Osso

### Community 106 - "FxSangue"
Cohesion: 0.24
Nodes (3): Ativo, FxSangue, Uniforms

### Community 109 - ".passo"
Cohesion: 0.27
Nodes (5): Acao, AVISAR_ADMIN, NADA, SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos

### Community 110 - "comparacao.py"
Cohesion: 0.33
Nodes (5): carregar(), colunas(), folha(), main(), textura_provisoria()

### Community 114 - "TipoSusto"
Cohesion: 0.20
Nodes (6): TipoSusto, LUZ, PASSOS, PORTA, SILENCIO, SUSSURRO

### Community 115 - "canal"
Cohesion: 0.27
Nodes (7): anims(), animacao(), canal(), anims(), lubdub(), anims(), wiggle()

### Community 116 - "blocos.py"
Cohesion: 0.31
Nodes (5): cheio(), curto(), formas(), material(), _verdade()

### Community 122 - "Modo"
Cohesion: 0.22
Nodes (4): Modo, COMPLETO, SEM_BALANCO, SO_TEXTO

### Community 130 - "EventoEnergia"
Cohesion: 0.25
Nodes (4): EventoEnergia, APAGAO, CASA_OCUPADA, FOME_DA_LINHA

### Community 134 - "TipoMomento"
Cohesion: 0.29
Nodes (6): TipoMomento, DEGRAU, MARCA_APARECEU, MARCA_SUBIU, MARCA_TROCOU, SURTO

## Knowledge Gaps
- **152 isolated node(s):** `AVISAR_ADMIN`, `NADA`, `SOLTAR_PRIMEIRO_SINAL`, `A_BEIRA`, `ATRAVESSADO` (+147 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 964 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **94 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lista` connect `Pacote Marcas` to `list`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **What connects `AVISAR_ADMIN`, `NADA`, `SOLTAR_PRIMEIRO_SINAL` to the rest of the system?**
  _152 weakly-connected nodes found - possible missing edges._
- **Should `OdMod.java` be split into smaller, more focused modules?**
  _Cohesion score 0.03350189633375474 - nodes in this community are weakly interconnected._
- **Why does `Estrutura` connect `Estrutura` to `Regras de Lucidez`, `modulos.py`, `Investida de Ganchos`, `testar.py`, `ruido`, `os`, `Tipos de Alucinação`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **Should `list` be split into smaller, more focused modules?**
  _Cohesion score 0.06797706797706797 - nodes in this community are weakly interconnected._
- **Why does `CriaturaDeSangue` connect `Pacote Cliente` to `OdMod.java`, `list`, `ModeloCriatura.java`, `net.minecraft.world.entity.Entity`, `Retalhado`, `Dobre.java`, `SanguessugaMae.java`, `Script Gradle Wrapper`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Should `OdMod` be split into smaller, more focused modules?**
  _Cohesion score 0.029499626587005227 - nodes in this community are weakly interconnected._