# Graph Report - od-cena  (2026-11-14)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 4401 nodes · 13069 edges · 183 communities (83 shown, 100 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 370 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `88d3db35`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- list
- EstadoGrupo
- previa_sangue.py
- Busca de Espaço
- PrimeiroSinal
- Grupos de Jogadores
- net.minecraft.world.level.Level
- Ouvinte do Primeiro Sinal
- Workflow de Compilação CI
- PrimeiroSinal.java
- Faixas de Lucidez
- resourcelocation
- vila_calada_150.py
- net.minecraft.resources.ResourceLocation
- net.minecraft.world.item.ItemStack
- Investida de Ganchos
- Sistema de Lucidez
- Agenda de Horror
- FxTela.java
- gerar_marca_v2.py
- PistasVilaCalada
- Renderização da Marca
- net.minecraft.server.level.ServerLevel
- Alucinações no Cliente
- OdMod.java
- RegrasRoteiroTest
- Cenas
- Tipos de Alucinação
- vila_calada_150_v2.py
- RegrasHorror
- Leitura do Mundo Real
- .aoTick
- RegrasCena
- RegrasAto1Test
- .aoTick
- Chuva de Filhotes
- Padrão de Veias
- net.minecraft.world.entity.LivingEntity
- Regras de Lucidez
- DadosJogador
- Comandos de Admin
- Integrações com Mods
- Limites de Valores
- gerar_png.py
- ClimaVilaCalada
- Marca de Sangue
- .lucidez
- Testes de Regras de Elemento
- net.minecraft.commands.CommandSourceStack
- Caixa
- Tipo
- Tipos de Momento
- Contencao
- Testes de Consistência
- Script Gradle Wrapper
- BalanceamentoTechTest
- Script Baixar Libs
- Script Teste de Servidor
- Build Gradle
- Settings Gradle
- Pacote Cliente
- Pacote Comandos
- Pacote Criaturas
- Pacote Dados
- Pacote Elementos
- Pacote Estruturas
- Pacote História
- Pacote Itens
- Pacote Magias
- Pacote Marcas
- Pacote Rede
- Pacote Rituais
- Pacote Sistemas
- gerar_sons.py
- net.minecraft.world.entity.player.Player
- RegrasLucidez
- RegrasFxSangue
- FxTela
- nbt_io.py
- Momento
- Alucinacao
- SanguessugaMae
- AnimacaoJogador.java
- net.minecraft.util.RandomSource
- MagiaTalhoRubro.java
- render3d.py
- .de
- .marcaQueAparece
- net.minecraft.world.phys.Vec3
- .tremor
- Direcao
- com.mojang.brigadier.context.CommandContext
- FalhaMinimapa
- sanguessuga_mae.py
- MomentosCliente.java
- SustoCliente
- Integracao
- Cena
- net.minecraft.client.player.LocalPlayer
- CamadaHorrorCliente
- RegrasAnalisadorTest
- EspacoTest
- RegrasArtefato
- FxCorHorror
- .marcaElemento
- RegrasMestre
- RegrasFonteVilaTest
- Imagem
- ClaimsFtbChunks.java
- MundoReal
- RegrasTalhoRubro
- Criatura
- .ler
- ProtecaoCena
- Partes
- Quadro
- Fonte
- comparacao.py
- LuzDaNoite.java
- Obra
- LivrosLidos
- .checar
- canal
- blocos.py
- RegrasCenaVilaTest
- Ritmo
- TipoMomento
- EsconderNoEmi.java
- SomVento.java
- ComandosFx
- .colocacoes
- EventoEnergia
- ConsistenciaTest
- FxCorHorror.java
- .legenda
- Momento
- PararCena
- TocarCena
- TocarCinema
- Repeticao
- FonteHorror
- .rodar
- RegrasPrepararVilaTest
- ComandosArtefato
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
4. `OdMod` - 62 edges
5. `DadosJogador` - 59 edges
6. `tom()` - 53 edges
7. `Estrutura` - 42 edges
8. `DadosHistoria` - 41 edges
9. `Backup` - 40 edges
10. `ComandosHistoria` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Image` --uses--> `Imagem`  [INFERRED]
  arte/marca-sangue/gerar_marca.py → arte/marca-sangue/imagem_simples.py
- `Obra` --uses--> `Mistura`  [INFERRED]
  ferramentas/estruturas/modelos/vila_calada_150_v2.py → ferramentas/estruturas/estrutura.py
- `mat()` --calls--> `sombreado()`  [EXTRACTED]
  arte/previas/criaturas/fonte/sanguessuga_mae.py → arte/previas/criaturas/fonte/montagem.py
- `CenaCliente` --references--> `Cena`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/cliente/CenaCliente.java → src/main/java/com/nikolas/odteste/cenas/Cena.java
- `RegrasCenaTest` --references--> `Cena`  [EXTRACTED]
  src/test/java/com/nikolas/odteste/cenas/RegrasCenaTest.java → src/main/java/com/nikolas/odteste/cenas/Cena.java

## Import Cycles
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150_v2.py -> ferramentas/estruturas/modelos/__init__.py`
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`
- 3-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150_v2.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`

## Communities (183 total, 100 thin omitted)

### Community 1 - "EstadoGrupo"
Cohesion: 0.05
Nodes (14): ConfigEstruturas, ConfigGatilhos, DadosGatilhos, EstadoGrupo, Gatilhos, Acao, AVISAR_ADMIN, NADA (+6 more)

### Community 2 - "previa_sangue.py"
Cohesion: 0.06
Nodes (26): ImageDraw, CenaDia, CenaVila, forca_visual(), lado_a_lado(), main(), Previa, batida() (+18 more)

### Community 3 - "Busca de Espaço"
Cohesion: 0.06
Nodes (5): RitualChamadoDeSangue, IdsDados, Sangramento, Exposicao, Lucidez

### Community 4 - "PrimeiroSinal"
Cohesion: 0.07
Nodes (6): DadosHistoria, Ouvinte, PrimeiroSinal, Resposta, Som, VilaMarcada

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.05
Nodes (7): RegrasExposicao, Resultado, RegrasAchadosTest, DadosAnalisadorTest, ElementoTest, RegrasConteudoTest, RegrasTest

### Community 6 - "net.minecraft.world.level.Level"
Cohesion: 0.06
Nodes (3): Criaturas, Paranormal, RegrasCombate

### Community 7 - "Ouvinte do Primeiro Sinal"
Cohesion: 0.05
Nodes (7): Aparelhos, BlocosCapela, Freio, IronsCompat, OccultismCompat, Itens, Registro

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.07
Nodes (35): hash01(), c(), gerar(), main(), _salvar(), main(), recorte(), salvar() (+27 more)

### Community 9 - "PrimeiroSinal.java"
Cohesion: 0.04
Nodes (4): BrilhoLodestone, IdsGatilhos, PecaDeTraje, IdsMagia

### Community 10 - "Faixas de Lucidez"
Cohesion: 0.07
Nodes (38): capela(), parede(), casa(), parede(), construir(), desgaste(), exterior(), passagem() (+30 more)

### Community 11 - "resourcelocation"
Cohesion: 0.09
Nodes (8): IdsCena, IdsHorror, AreaHorror, EfeitoFx, FaseMagia, MarcaVisivel, SincronizarJogador, Susto

### Community 12 - "vila_calada_150.py"
Cohesion: 0.09
Nodes (40): arvore_morta(), arvores_mortas(), caminho(), caminho_dos_enterros(), campos(), carroca(), cemiterio(), cerca() (+32 more)

### Community 14 - "net.minecraft.resources.ResourceLocation"
Cohesion: 0.06
Nodes (4): Leitor, CamadaMarca, ModeloCriatura, EsconderNoJei

### Community 15 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.08
Nodes (10): LeitorLivros, Livro, Pagina, Trecho, LivrosHistoria, SimpleJsonResourceReloadListener, Pedaco, RegrasLivros (+2 more)

### Community 16 - "Investida de Ganchos"
Cohesion: 0.06
Nodes (8): Estrutura, nome_completo(), _texto(), construida(), TesteModelos, TesteNBT, TesteVila150, TesteVila150V2

### Community 17 - "Sistema de Lucidez"
Cohesion: 0.07
Nodes (28): Animacao, _avaliar(), Camera, centro_para(), _chaves(), _ease(), escala_m(), faixa_titulo() (+20 more)

### Community 18 - "Agenda de Horror"
Cohesion: 0.08
Nodes (36): ruido(), arvore(), arvores_queimadas(), campanario(), campos(), capela(), casa(), casas() (+28 more)

### Community 19 - "FxTela.java"
Cohesion: 0.05
Nodes (4): ConteudoCliente, OdModCliente, Compat, ClimaVila

### Community 20 - "gerar_marca_v2.py"
Cohesion: 0.09
Nodes (30): _caixa(), corpo(), faces(), mapa(), render(), _rot(), skin_teste(), skins_teste() (+22 more)

### Community 21 - "PistasVilaCalada"
Cohesion: 0.09
Nodes (4): Estado, PistasVilaCalada, RegrasVilaCalada, RegrasVilaCaladaTest

### Community 22 - "Renderização da Marca"
Cohesion: 0.08
Nodes (4): ClimaVilaCliente, Pendurado, RegrasClimaVila, RegrasClimaVilaTest

### Community 23 - "net.minecraft.server.level.ServerLevel"
Cohesion: 0.08
Nodes (11): FonteDeSinal, Fonte, FontesFixas, FonteVilaCalada, Resultado, CHEIO, NOME_INVALIDO, OK (+3 more)

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.09
Nodes (5): VelaDaCapelaDados, Porta, Backup, Restauracao, SinaisNoMundo

### Community 25 - "OdMod.java"
Cohesion: 0.08
Nodes (4): CenaEntradaVila, DadosVilaCalada, OdMod, RegrasSinal

### Community 26 - "RegrasRoteiroTest"
Cohesion: 0.11
Nodes (7): Ancora, ChaveMundo, Olhos, Pose, RegrasRoteiro, Veu, RegrasRoteiroTest

### Community 27 - "Cenas"
Cohesion: 0.08
Nodes (10): Ancora, Cenas, Cinema, Resultado, DESCONHECIDA, NAO_PODE, SEM_LUGAR, TOCOU (+2 more)

### Community 28 - "Tipos de Alucinação"
Cohesion: 0.09
Nodes (18): faixa(), topo(), amostrar(), _c(), coracao(), corpo_veia(), costura(), _desdobrar() (+10 more)

### Community 29 - "vila_calada_150_v2.py"
Cohesion: 0.11
Nodes (30): aberto(), adro(), arvore_morta(), caixas_viradas(), carroca_virada(), casas(), chao(), chao_y() (+22 more)

### Community 30 - "RegrasHorror"
Cohesion: 0.10
Nodes (4): Agenda, RegrasHorror, Sorteio, RegrasHorrorTest

### Community 31 - "Leitura do Mundo Real"
Cohesion: 0.07
Nodes (8): Area, CamadaHorror, AgulhaDeCerzir, Artefato, DedalDentado, SinoDeMatadouro, Magias, MarcaSangue

### Community 32 - ".aoTick"
Cohesion: 0.10
Nodes (10): Diario, RegrasSusto, Sorteio, TipoSusto, LUZ, PASSOS, PORTA, SILENCIO (+2 more)

### Community 33 - "RegrasCena"
Cohesion: 0.08
Nodes (9): Local, Modo, COMPLETO, SEM_BALANCO, SO_TEXTO, Pose, Quadro, RegrasCena (+1 more)

### Community 35 - "RegrasAto1Test"
Cohesion: 0.08
Nodes (9): Morse, Sequencia, Toque, Fonte, NOTA, SINO, RegrasAto1, MorseTest (+1 more)

### Community 36 - ".aoTick"
Cohesion: 0.09
Nodes (14): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+6 more)

### Community 37 - "Chuva de Filhotes"
Cohesion: 0.06
Nodes (39): outputs, runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs (+31 more)

### Community 38 - "Padrão de Veias"
Cohesion: 0.09
Nodes (21): ampliar_molde(), caixa(), corpo(), crescer(), desenhar(), fluxo(), fonte(), Image (+13 more)

### Community 39 - "net.minecraft.world.entity.LivingEntity"
Cohesion: 0.09
Nodes (6): CuriosCompat, Rubra, AplicarDano, Esperando, TalhoRubro, Combate

### Community 40 - "Regras de Lucidez"
Cohesion: 0.16
Nodes (31): cor(), costura_h(), patchwork(), eh_costura(), p(), regiao(), sementes(), tom() (+23 more)

### Community 43 - "Integrações com Mods"
Cohesion: 0.13
Nodes (3): Grupo, Grupos, GruposTest

### Community 44 - "Limites de Valores"
Cohesion: 0.11
Nodes (24): Mistura, arvores_queimadas(), casa(), casa_do_livro(), casas(), chao(), chao_queimado(), cinzas() (+16 more)

### Community 45 - "gerar_png.py"
Cohesion: 0.12
Nodes (26): afim(), amostra(), compor(), cor(), cruz(), cruzar(), cubos_no_mundo(), andar() (+18 more)

### Community 46 - "ClimaVilaCalada"
Cohesion: 0.11
Nodes (3): ClimaVilaCalada, Enviado, VilaMorta

### Community 47 - "Marca de Sangue"
Cohesion: 0.09
Nodes (7): CansacoDoElemento, Destino, CONTA, TIRA, RegrasCansaco, Cansaco, RegrasMateriaTest

### Community 48 - ".lucidez"
Cohesion: 0.12
Nodes (8): Resultado, CONJUROU, DESCONHECIDA, OCUPADO, RECARGA, SEM_LUCIDEZ, Estado, Vigia

### Community 51 - "Caixa"
Cohesion: 0.20
Nodes (3): Caixa, Fatias, FatiasTest

### Community 52 - "Tipo"
Cohesion: 0.10
Nodes (14): Chao, Espaco, Mundo, Resultado, Tipo, AR, COM_DADOS, FORA_DO_MUNDO (+6 more)

### Community 58 - "Script Baixar Libs"
Cohesion: 0.18
Nodes (9): Fim, BLOQUEADO, DONO, JA, LONGE, POSTO, PrepararVilaCalada, Peca (+1 more)

### Community 60 - "Build Gradle"
Cohesion: 0.24
Nodes (4): Colocacao, Acesso, EmPartes, Memoria

### Community 62 - "Pacote Cliente"
Cohesion: 0.09
Nodes (6): RegrasCriaturas, Raridade, CHEFE, COMUM, RARA, RegrasCombateTest

### Community 65 - "Pacote Dados"
Cohesion: 0.11
Nodes (7): AnalisadorSinalEntidade, BateriaAparelho, Intensidade, FORTE, FRACA, MEDIA, NENHUMA

### Community 66 - "Pacote Elementos"
Cohesion: 0.10
Nodes (18): Chave, Curva, CATMULL, CORTE, LINEAR, Evento, Fade, Fx (+10 more)

### Community 67 - "Pacote Estruturas"
Cohesion: 0.10
Nodes (7): Elemento, CONHECIMENTO, ENERGIA, MORTE, NENHUM, SANGUE, RegrasElemento

### Community 68 - "Pacote História"
Cohesion: 0.13
Nodes (5): ComandosAdmin, Abalo, APARICAO, BATALHA_RELIQUIA, MOB_SANGUE

### Community 69 - "Pacote Itens"
Cohesion: 0.09
Nodes (12): Canto, AUTOMATICO, BAIXO_DIREITA, BAIXO_ESQUERDA, CIMA_DIREITA, CIMA_ESQUERDA, NENHUM, ConfigCliente (+4 more)

### Community 70 - "Pacote Magias"
Cohesion: 0.12
Nodes (3): Achados, RegrasAchados, Ronda

### Community 72 - "Pacote Rede"
Cohesion: 0.13
Nodes (4): FxGravar, Ativo, FxSangue, Uniforms

### Community 73 - "Pacote Rituais"
Cohesion: 0.15
Nodes (14): ampliar(), gerar(), main(), arquivo_animacoes(), bbmodel(), grupo(), clamp(), costura_v() (+6 more)

### Community 74 - "Pacote Sistemas"
Cohesion: 0.21
Nodes (21): _aba(), aba_pintor(), aba_pintor2(), antebraco(), avental(), bota(), calca(), camisa() (+13 more)

### Community 75 - "gerar_sons.py"
Cohesion: 0.14
Nodes (10): arrasto(), Biquad, eco(), envelope_bordas(), gravar_wav(), main(), normalizar(), para_ogg() (+2 more)

### Community 78 - "RegrasLucidez"
Cohesion: 0.10
Nodes (4): RegrasCapela, Acumulo, RegrasLucidez, RegrasCapelaTest

### Community 81 - "nbt_io.py"
Cohesion: 0.14
Nodes (13): Byte, Double, _escrever(), Float, Int, _Leitor, ler(), Lista (+5 more)

### Community 82 - "Momento"
Cohesion: 0.13
Nodes (5): Momento, AREA_DIA, MUNDO, NADA, NOITE

### Community 83 - "Alucinacao"
Cohesion: 0.12
Nodes (15): Alucinacao, CISCO, CORACAO, ESCURECER, ESTALO, INCLINAR, MOB_FALSO, NOME (+7 more)

### Community 88 - "render3d.py"
Cohesion: 0.14
Nodes (7): Atlas, _cantos(), Cena, _olhar(), _orto(), _perspectiva(), _uv()

### Community 90 - ".marcaQueAparece"
Cohesion: 0.13
Nodes (3): RegrasSangramento, RegrasMarca, RegrasElementoTest

### Community 91 - "net.minecraft.world.phys.Vec3"
Cohesion: 0.21
Nodes (3): Gota, MagiaCliente, Mancha

### Community 92 - ".tremor"
Cohesion: 0.15
Nodes (3): Envelope, Pulso, Tela

### Community 93 - "Direcao"
Cohesion: 0.11
Nodes (10): Direcao, DIREITA, ESQUERDA, FRENTE, FRENTE_DIREITA, FRENTE_ESQUERDA, TRAS, TRAS_DIREITA (+2 more)

### Community 95 - "FalhaMinimapa"
Cohesion: 0.14
Nodes (3): FonteHorror, FalhaMinimapa, Mapa

### Community 96 - "sanguessuga_mae.py"
Cohesion: 0.19
Nodes (17): barriga(), cabeca_larva(), canela_pintor(), casco(), cauda_larva(), coxa_pintor(), cume(), dente() (+9 more)

### Community 99 - "Integracao"
Cohesion: 0.10
Nodes (18): Integracao, BOTANIA, CREATE, CURIOS, EMI, FTB_CHUNKS, IRIS, IRONS (+10 more)

### Community 100 - "Cena"
Cohesion: 0.16
Nodes (5): Cena, Plano, Ponto, Som, CenasConhecidas

### Community 105 - "RegrasArtefato"
Cohesion: 0.13
Nodes (10): Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM, INCOMUM (+2 more)

### Community 112 - "ClaimsFtbChunks.java"
Cohesion: 0.19
Nodes (3): ClaimsFtbChunks, PackIris, Protecao

### Community 115 - "Criatura"
Cohesion: 0.16
Nodes (3): Criatura, Cubo, Osso

### Community 118 - "Partes"
Cohesion: 0.19
Nodes (7): Fase, COLOCANDO, CONFERINDO, DESFAZENDO, PAUSADA, PRONTA, Partes

### Community 120 - "Fonte"
Cohesion: 0.17
Nodes (9): Fonte, APARICAO, CRIATURA_LUTA, CRIATURA_MORTA, CRIATURA_VISTA, DIMENSAO, RELIQUIA_PEGAR, RELIQUIA_USAR (+1 more)

### Community 121 - "comparacao.py"
Cohesion: 0.33
Nodes (5): carregar(), colunas(), folha(), main(), textura_provisoria()

### Community 126 - "canal"
Cohesion: 0.27
Nodes (7): anims(), animacao(), canal(), anims(), lubdub(), anims(), wiggle()

### Community 127 - "blocos.py"
Cohesion: 0.31
Nodes (5): cheio(), curto(), formas(), material(), _verdade()

### Community 129 - "Ritmo"
Cohesion: 0.22
Nodes (5): Ritmo, ENTRA, LINEAR, SAI, SUAVE

### Community 130 - "TipoMomento"
Cohesion: 0.22
Nodes (6): TipoMomento, DEGRAU, MARCA_APARECEU, MARCA_SUBIU, MARCA_TROCOU, SURTO

### Community 135 - "EventoEnergia"
Cohesion: 0.25
Nodes (4): EventoEnergia, APAGAO, CASA_OCUPADA, FOME_DA_LINHA

## Knowledge Gaps
- **157 isolated node(s):** `AVISAR_ADMIN`, `NADA`, `SOLTAR_PRIMEIRO_SINAL`, `COMPARTILHADA`, `LIVRE` (+152 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 964 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **100 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Elemento` connect `Pacote Estruturas` to `list`, `MomentosCliente.java`, `Busca de Espaço`, `Pacote Marcas`, `DadosJogador`, `RegrasArtefato`, `resourcelocation`, `.marcaElemento`, `net.minecraft.world.entity.player.Player`, `assertequals`, `.marcaQueAparece`, `Script Teste de Servidor`, `Leitura do Mundo Real`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **What connects `AVISAR_ADMIN`, `NADA`, `SOLTAR_PRIMEIRO_SINAL` to the rest of the system?**
  _157 weakly-connected nodes found - possible missing edges._
- **Should `list` be split into smaller, more focused modules?**
  _Cohesion score 0.0824561403508772 - nodes in this community are weakly interconnected._
- **Why does `Lista` connect `nbt_io.py` to `list`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Should `EstadoGrupo` be split into smaller, more focused modules?**
  _Cohesion score 0.051685393258426963 - nodes in this community are weakly interconnected._
- **Why does `BalanceamentoTechTest` connect `BalanceamentoTechTest` to `assertequals`?**
  _High betweenness centrality (0.017) - this node is a cross-community bridge._
- **Should `previa_sangue.py` be split into smaller, more focused modules?**
  _Cohesion score 0.05516431924882629 - nodes in this community are weakly interconnected._