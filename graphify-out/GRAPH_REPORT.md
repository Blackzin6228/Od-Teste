# Graph Report - od-fx  (2026-11-14)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 4215 nodes · 12406 edges · 173 communities (87 shown, 86 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 358 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `88d3db35`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OdMod.java
- Caixa
- org.junit.jupiter.api.Test
- Busca de Espaço
- list
- Grupos de Jogadores
- net.minecraft.world.level.block.state.BlockState
- Ouvinte do Primeiro Sinal
- Workflow de Compilação CI
- LivrosHistoria
- Faixas de Lucidez
- AnalisadorSinalEntidade.java
- net.minecraft.resources.ResourceLocation
- vila_calada_150.py
- Achados.java
- modulos.py
- Investida de Ganchos
- Sistema de Lucidez
- Agenda de Horror
- DadosJogador
- ruido
- RegrasAto1Test
- Renderização da Marca
- RegrasClimaVila
- Alucinações no Cliente
- previa_shader.py
- vila_calada_150_v2.py
- gerar_marca_v2.py
- net.minecraft.core.BlockPos
- Elemento
- Leitura do Mundo Real
- net.minecraft.server.MinecraftServer
- with
- net.minecraft.server.level.ServerPlayer
- .podeNascer
- PistasVilaCalada
- Chuva de Filhotes
- Padrão de Veias
- Degrau
- Regras de Lucidez
- gerar_png.py
- Comandos de Admin
- Integrações com Mods
- Limites de Valores
- tom
- Estrutura
- Marca de Sangue
- com.mojang.brigadier.context.CommandContext
- Testes de Regras de Elemento
- MagiaTalhoRubro.java
- net.minecraft.sounds.SoundEvent
- Contencao
- Tipos de Momento
- EstadoGrupo
- Testes de Consistência
- Script Gradle Wrapper
- .aoSoltar
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
- Mestre
- nbt_io.py
- render3d.py
- .use
- Cenas
- RegrasCamadaHorror
- FxSangue
- testar.py
- net.minecraft.world.phys.Vec3
- SustoCliente
- Integracao
- montagem.py
- sanguessuga_mae.py
- RegrasFxSangue
- CenaCliente
- RegrasAnalisadorTest
- .aoTick
- net.minecraft.world.level.block.Rotation
- CamadaHorrorCliente
- AnalisadorSinalEntidade
- ElementoJogador
- .contato
- RegrasMestre
- Cena
- .de
- AnimacaoJogador
- .pose
- net.neoforged.neoforge.common.ModConfigSpec
- FxGravar.java
- RegrasFonteVilaTest
- Combate.java
- capela_fazenda.py
- .aoTick
- FalhaMinimapa
- .enviar
- Criatura
- net.minecraft.resources.ResourceKey
- RegrasCenaTest
- .passo
- comparacao.py
- LuzDaNoite.java
- Obra
- RegrasCena
- .forca
- .checar
- canal
- blocos.py
- DadosAnalisadorTest
- BrilhoLodestone.java
- DadosGatilhos
- RegrasCenaVilaTest
- Resultado
- SomVento.java
- .aoAbrirLivro
- .iniciar
- FxCorHorror
- ComandosFx
- .colocacoes
- ConsistenciaTest
- Modo
- FonteHorror
- PackIris
- .rodar
- p
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
4. `DadosJogador` - 59 edges
5. `OdMod` - 57 edges
6. `tom()` - 53 edges
7. `Estrutura` - 42 edges
8. `DadosHistoria` - 41 edges
9. `Backup` - 40 edges
10. `ComandosHistoria` - 38 edges

## Surprising Connections (you probably didn't know these)
- `Obra` --uses--> `Mistura`  [INFERRED]
  ferramentas/estruturas/modelos/vila_calada_150_v2.py → ferramentas/estruturas/estrutura.py
- `Previa` --uses--> `Efeito`  [INFERRED]
  ferramentas/fx/previa_sangue.py → ferramentas/fx/previa_shader.py
- `TesteVila150` --uses--> `Estrutura`  [INFERRED]
  ferramentas/estruturas/testar.py → ferramentas/estruturas/estrutura.py
- `mat()` --calls--> `sombreado()`  [EXTRACTED]
  arte/previas/criaturas/fonte/sanguessuga_mae.py → arte/previas/criaturas/fonte/montagem.py
- `Memoria` --references--> `Caixa`  [EXTRACTED]
  src/main/java/com/nikolas/odteste/estruturas/EmPartes.java → src/main/java/com/nikolas/odteste/estruturas/Espaco.java

## Import Cycles
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150_v2.py -> ferramentas/estruturas/modelos/__init__.py`
- 2-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`
- 3-file cycle: `ferramentas/estruturas/modelos/__init__.py -> ferramentas/estruturas/modelos/vila_calada_150_v2.py -> ferramentas/estruturas/modelos/vila_calada_150.py -> ferramentas/estruturas/modelos/__init__.py`

## Communities (173 total, 86 thin omitted)

### Community 0 - "OdMod.java"
Cohesion: 0.04
Nodes (5): ComandosArtefato, Compat, LuaDeSangue, OdMod, Rede

### Community 1 - "Caixa"
Cohesion: 0.06
Nodes (21): Caixa, Chao, Espaco, Mundo, Tipo, AR, COM_DADOS, FORA_DO_MUNDO (+13 more)

### Community 2 - "org.junit.jupiter.api.Test"
Cohesion: 0.04
Nodes (8): RegrasExposicao, Resultado, LimitesTest, ElementoTest, RegrasVilaCaladaTest, RegrasTalhoRubroTest, RegrasConteudoTest, RegrasTest

### Community 3 - "Busca de Espaço"
Cohesion: 0.07
Nodes (5): MundoReal, DadosHistoria, PrimeiroSinal, Resposta, VilaMarcada

### Community 4 - "list"
Cohesion: 0.06
Nodes (5): BlocosCapelaIds, IdsCena, MinimapaJourneyMap, MinimapaXaero, IdsMestre

### Community 5 - "Grupos de Jogadores"
Cohesion: 0.10
Nodes (3): IdsGatilhos, IdsHistoria, IdsVilaCalada

### Community 6 - "net.minecraft.world.level.block.state.BlockState"
Cohesion: 0.08
Nodes (4): AnalisadorSinalBloco, PocaDeSangue, VelaDaCapela, VelaDaCapelaDados

### Community 7 - "Ouvinte do Primeiro Sinal"
Cohesion: 0.07
Nodes (10): IdsHorror, AreaHorror, ClimaVila, EfeitoFx, FaseMagia, MarcaVisivel, Momento, SincronizarJogador (+2 more)

### Community 8 - "Workflow de Compilação CI"
Cohesion: 0.09
Nodes (5): RegrasSangramento, EventoEnergia, APAGAO, CASA_OCUPADA, FOME_DA_LINHA

### Community 9 - "LivrosHistoria"
Cohesion: 0.07
Nodes (9): LeitorLivros, Livro, Pagina, Trecho, LivrosHistoria, SimpleJsonResourceReloadListener, Pedaco, RegrasLivros (+1 more)

### Community 10 - "Faixas de Lucidez"
Cohesion: 0.06
Nodes (3): ConteudoCliente, Criaturas, Paranormal

### Community 11 - "AnalisadorSinalEntidade.java"
Cohesion: 0.06
Nodes (13): FonteDeSinal, Fonte, FontesFixas, FonteVilaCalada, LeitorDeItem, Resultado, CHEIO, NOME_INVALIDO (+5 more)

### Community 12 - "net.minecraft.resources.ResourceLocation"
Cohesion: 0.06
Nodes (4): CamadaMarca, ModeloCriatura, EsconderNoJei, IdsDados

### Community 13 - "vila_calada_150.py"
Cohesion: 0.09
Nodes (40): arvore_morta(), arvores_mortas(), caminho(), caminho_dos_enterros(), campos(), carroca(), cemiterio(), cerca() (+32 more)

### Community 14 - "Achados.java"
Cohesion: 0.07
Nodes (6): Aparelhos, BlocosCapela, Freio, EsconderNoEmi, Itens, Registro

### Community 15 - "modulos.py"
Cohesion: 0.08
Nodes (33): capela(), casa(), parede(), desgaste(), casa_do_coveiro(), capela(), compadre(), desgaste() (+25 more)

### Community 16 - "Investida de Ganchos"
Cohesion: 0.07
Nodes (27): Animacao, _avaliar(), Camera, centro_para(), _chaves(), _ease(), escala_m(), faixa_titulo() (+19 more)

### Community 17 - "Sistema de Lucidez"
Cohesion: 0.06
Nodes (22): ampliar_molde(), caixa(), corpo(), crescer(), desenhar(), fluxo(), fonte(), Image (+14 more)

### Community 18 - "Agenda de Horror"
Cohesion: 0.06
Nodes (25): _caixa(), corpo(), faces(), faixa(), mapa(), _rot(), skin_teste(), skins_teste() (+17 more)

### Community 19 - "DadosJogador"
Cohesion: 0.12
Nodes (3): Cansaco, DadosJogador, Limites

### Community 20 - "ruido"
Cohesion: 0.09
Nodes (33): ruido(), arvore(), arvores_queimadas(), campanario(), campos(), casa(), casas(), cemiterio() (+25 more)

### Community 21 - "RegrasAto1Test"
Cohesion: 0.07
Nodes (11): Morse, Sequencia, Toque, Ouvinte, Fonte, NOTA, SINO, RegrasAto1 (+3 more)

### Community 22 - "Renderização da Marca"
Cohesion: 0.11
Nodes (22): gerar(), main(), _salvar(), main(), recorte(), salvar(), com_chao_do_mundo(), conferir_centro() (+14 more)

### Community 23 - "RegrasClimaVila"
Cohesion: 0.09
Nodes (4): ClimaVilaCliente, Pendurado, RegrasClimaVila, RegrasClimaVilaTest

### Community 24 - "Alucinações no Cliente"
Cohesion: 0.09
Nodes (10): Diario, RegrasSusto, Sorteio, TipoSusto, LUZ, PASSOS, PORTA, SILENCIO (+2 more)

### Community 25 - "previa_shader.py"
Cohesion: 0.07
Nodes (15): CenaDia, folha(), fonte(), forca_visual(), lado_a_lado(), main(), Previa, Cena (+7 more)

### Community 26 - "vila_calada_150_v2.py"
Cohesion: 0.11
Nodes (30): aberto(), adro(), arvore_morta(), caixas_viradas(), carroca_virada(), casas(), chao(), chao_y() (+22 more)

### Community 27 - "gerar_marca_v2.py"
Cohesion: 0.11
Nodes (23): render(), carregar_v1(), folha_antes_depois(), folha_comparacao_hd(), folha_ferida(), folha_geral(), folha_zoom(), gif_brilho() (+15 more)

### Community 30 - "Elemento"
Cohesion: 0.10
Nodes (10): MomentosCliente, Envelope, Pulso, Tela, Elemento, CONHECIMENTO, ENERGIA, MORTE (+2 more)

### Community 31 - "Leitura do Mundo Real"
Cohesion: 0.07
Nodes (16): Direcao, DIREITA, ESQUERDA, FRENTE, FRENTE_DIREITA, FRENTE_ESQUERDA, TRAS, TRAS_DIREITA (+8 more)

### Community 32 - "net.minecraft.server.MinecraftServer"
Cohesion: 0.14
Nodes (11): Colocacao, Fase, COLOCANDO, CONFERINDO, DESFAZENDO, PAUSADA, PRONTA, Partes (+3 more)

### Community 33 - "with"
Cohesion: 0.06
Nodes (39): outputs, runs-on, steps, timeout-minutes, concurrency, cancel-in-progress, group, jobs (+31 more)

### Community 34 - "net.minecraft.server.level.ServerPlayer"
Cohesion: 0.09
Nodes (7): Area, CamadaHorror, AgulhaDeCerzir, Artefato, DedalDentado, SinoDeMatadouro, MarcaSangue

### Community 35 - ".podeNascer"
Cohesion: 0.07
Nodes (7): RegrasCriaturas, Raridade, CHEFE, COMUM, RARA, RegrasCombate, RegrasCombateTest

### Community 36 - "PistasVilaCalada"
Cohesion: 0.12
Nodes (3): Estado, PistasVilaCalada, RegrasVilaCalada

### Community 37 - "Chuva de Filhotes"
Cohesion: 0.09
Nodes (5): CuriosCompat, Rubra, AplicarDano, Esperando, TalhoRubro

### Community 38 - "Padrão de Veias"
Cohesion: 0.11
Nodes (4): Agenda, RegrasHorror, Sorteio, RegrasHorrorTest

### Community 39 - "Degrau"
Cohesion: 0.09
Nodes (14): Degrau, A_BEIRA, ATRAVESSADO, COMUM, FUNDO, MARCADO, TOCADO, FaixaLucidez (+6 more)

### Community 40 - "Regras de Lucidez"
Cohesion: 0.10
Nodes (16): ImageDraw, CenaVila, batida(), contexto(), curva_sangue(), folha(), folha_parede(), jeito_de_pack() (+8 more)

### Community 41 - "gerar_png.py"
Cohesion: 0.12
Nodes (26): afim(), amostra(), compor(), cor(), cruz(), cruzar(), cubos_no_mundo(), andar() (+18 more)

### Community 42 - "Comandos de Admin"
Cohesion: 0.11
Nodes (22): Mistura, arvores_queimadas(), casa(), casa_do_livro(), casas(), chao(), chao_queimado(), cinzas() (+14 more)

### Community 43 - "Integrações com Mods"
Cohesion: 0.12
Nodes (3): RitualChamadoDeSangue, Sangramento, Exposicao

### Community 44 - "Limites de Valores"
Cohesion: 0.11
Nodes (3): ClimaVilaCalada, Enviado, VilaMorta

### Community 45 - "tom"
Cohesion: 0.18
Nodes (27): cor(), costura_h(), patchwork(), tom(), bico_gancho(), cabeca_pintor(), camadas(), cavidade() (+19 more)

### Community 46 - "Estrutura"
Cohesion: 0.11
Nodes (4): Estrutura, nome_completo(), _texto(), TesteNBT

### Community 47 - "Marca de Sangue"
Cohesion: 0.10
Nodes (4): Achados, RegrasAchados, Ronda, RegrasAchadosTest

### Community 49 - "Testes de Regras de Elemento"
Cohesion: 0.16
Nodes (3): Grupo, Grupos, GruposTest

### Community 53 - "Tipos de Momento"
Cohesion: 0.11
Nodes (6): Momento, AREA_DIA, MUNDO, NADA, NOITE, RegrasCamadaHorrorTest

### Community 54 - "EstadoGrupo"
Cohesion: 0.17
Nodes (4): EstadoGrupo, Grupo, Momento, RegrasGatilhosTest

### Community 55 - "Testes de Consistência"
Cohesion: 0.12
Nodes (5): ComandosAdmin, Abalo, APARICAO, BATALHA_RELIQUIA, MOB_SANGUE

### Community 56 - "Script Gradle Wrapper"
Cohesion: 0.08
Nodes (12): Canto, AUTOMATICO, BAIXO_DIREITA, BAIXO_ESQUERDA, CIMA_DIREITA, CIMA_ESQUERDA, NENHUM, ConfigCliente (+4 more)

### Community 57 - ".aoSoltar"
Cohesion: 0.10
Nodes (6): CansacoDoElemento, Destino, CONTA, TIRA, RegrasCansaco, RegrasMateriaTest

### Community 60 - "Build Gradle"
Cohesion: 0.15
Nodes (8): DadosVilaCalada, Fim, BLOQUEADO, DONO, JA, LONGE, POSTO, PrepararVilaCalada

### Community 61 - "Settings Gradle"
Cohesion: 0.16
Nodes (3): Lucidez, Estado, Vigia

### Community 62 - "Pacote Cliente"
Cohesion: 0.13
Nodes (15): hash01(), c(), _amostrador(), cor_px(), _clamp(), _face(), _girar(), _girar_dir() (+7 more)

### Community 63 - "Pacote Comandos"
Cohesion: 0.13
Nodes (3): RegrasElemento, RegrasMarca, RegrasElementoTest

### Community 64 - "Pacote Criaturas"
Cohesion: 0.21
Nodes (21): _aba(), aba_pintor(), aba_pintor2(), antebraco(), avental(), bota(), calca(), camisa() (+13 more)

### Community 65 - "Pacote Dados"
Cohesion: 0.14
Nodes (10): arrasto(), Biquad, eco(), envelope_bordas(), gravar_wav(), main(), normalizar(), para_ogg() (+2 more)

### Community 66 - "Pacote Elementos"
Cohesion: 0.18
Nodes (3): FxTela, Passe, Quadro

### Community 67 - "Pacote Estruturas"
Cohesion: 0.10
Nodes (4): RegrasCapela, Acumulo, RegrasLucidez, RegrasCapelaTest

### Community 70 - "Pacote Magias"
Cohesion: 0.11
Nodes (15): Alucinacao, CISCO, CORACAO, ESCURECER, ESTALO, INCLINAR, MOB_FALSO, NOME (+7 more)

### Community 71 - "Pacote Marcas"
Cohesion: 0.11
Nodes (3): IronsCompat, OccultismCompat, IdsMagia

### Community 73 - "Pacote Rituais"
Cohesion: 0.21
Nodes (3): Alucinacoes, Figura, Porta

### Community 76 - "nbt_io.py"
Cohesion: 0.14
Nodes (13): Byte, Double, _escrever(), Float, Int, _Leitor, ler(), Lista (+5 more)

### Community 77 - "render3d.py"
Cohesion: 0.14
Nodes (7): Atlas, _cantos(), Cena, _olhar(), _orto(), _perspectiva(), _uv()

### Community 78 - ".use"
Cohesion: 0.12
Nodes (10): Bloqueio, COMPARTILHADA, LIVRE, RECARGA, SEM_LUCIDEZ, Raridade, COMUM, INCOMUM (+2 more)

### Community 79 - "Cenas"
Cohesion: 0.16
Nodes (3): Ancora, Cenas, CenaEntradaVila

### Community 81 - "FxSangue"
Cohesion: 0.16
Nodes (4): Ativo, FxSangue, FxTela.Passe, Uniforms

### Community 82 - "testar.py"
Cohesion: 0.14
Nodes (4): construida(), TesteModelos, TesteVila150, TesteVila150V2

### Community 83 - "net.minecraft.world.phys.Vec3"
Cohesion: 0.22
Nodes (3): Gota, MagiaCliente, Mancha

### Community 86 - "Integracao"
Cohesion: 0.10
Nodes (19): Integracao, BOTANIA, CREATE, CURIOS, EMI, ENHANCED_CELESTIALS, FTB_CHUNKS, IRIS (+11 more)

### Community 87 - "montagem.py"
Cohesion: 0.18
Nodes (12): ampliar(), gerar(), main(), arquivo_animacoes(), bbmodel(), grupo(), clamp(), costura_v() (+4 more)

### Community 88 - "sanguessuga_mae.py"
Cohesion: 0.19
Nodes (17): barriga(), cabeca_larva(), canela_pintor(), casco(), cauda_larva(), coxa_pintor(), cume(), dente() (+9 more)

### Community 95 - "net.minecraft.world.level.block.Rotation"
Cohesion: 0.27
Nodes (3): Resultado, Estruturas, Tentativa

### Community 99 - ".contato"
Cohesion: 0.12
Nodes (10): Fonte, APARICAO, CRIATURA_LUTA, CRIATURA_MORTA, CRIATURA_VISTA, DIMENSAO, RELIQUIA_PEGAR, RELIQUIA_USAR (+2 more)

### Community 101 - "Cena"
Cohesion: 0.18
Nodes (5): Cena, Plano, Ponto, Som, CenasConhecidas

### Community 109 - "capela_fazenda.py"
Cohesion: 0.18
Nodes (10): parede(), construir(), exterior(), passagem(), pistas(), reboco(), sangue(), mancha() (+2 more)

### Community 112 - ".enviar"
Cohesion: 0.14
Nodes (8): Momentos, Reacoes, TipoMomento, DEGRAU, MARCA_APARECEU, MARCA_SUBIU, MARCA_TROCOU, SURTO

### Community 113 - "Criatura"
Cohesion: 0.16
Nodes (3): Criatura, Cubo, Osso

### Community 116 - ".passo"
Cohesion: 0.27
Nodes (5): Acao, AVISAR_ADMIN, NADA, SOLTAR_PRIMEIRO_SINAL, RegrasGatilhos

### Community 117 - "comparacao.py"
Cohesion: 0.33
Nodes (5): carregar(), colunas(), folha(), main(), textura_provisoria()

### Community 120 - "RegrasCena"
Cohesion: 0.33
Nodes (3): Legenda, Quadro, RegrasCena

### Community 123 - "canal"
Cohesion: 0.27
Nodes (7): anims(), animacao(), canal(), anims(), lubdub(), anims(), wiggle()

### Community 124 - "blocos.py"
Cohesion: 0.31
Nodes (5): cheio(), curto(), formas(), material(), _verdade()

### Community 129 - "Resultado"
Cohesion: 0.25
Nodes (7): Magias, Resultado, CONJUROU, DESCONHECIDA, OCUPADO, RECARGA, SEM_LUCIDEZ

### Community 137 - "Modo"
Cohesion: 0.33
Nodes (4): Modo, COMPLETO, SEM_BALANCO, SO_TEXTO

### Community 142 - "p"
Cohesion: 0.67
Nodes (4): eh_costura(), p(), regiao(), sementes()

## Knowledge Gaps
- **143 isolated node(s):** `AR`, `COM_DADOS`, `FORA_DO_MUNDO`, `LIQUIDO`, `NAO_CARREGADO` (+138 more)
  These have ≤1 connection - possible missing edges. (Counts symbols only; 936 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **86 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Lista` connect `nbt_io.py` to `list`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **What connects `AR`, `COM_DADOS`, `FORA_DO_MUNDO` to the rest of the system?**
  _143 weakly-connected nodes found - possible missing edges._
- **Should `OdMod.java` be split into smaller, more focused modules?**
  _Cohesion score 0.03978561121418264 - nodes in this community are weakly interconnected._
- **Why does `RegrasHorror` connect `Padrão de Veias` to `OdMod.java`, `list`, `Grupos de Jogadores`, `Pacote Magias`, `Degrau`, `Pacote Rituais`, `Script Gradle Wrapper`, `Alucinacoes.java`?**
  _High betweenness centrality (0.016) - this node is a cross-community bridge._
- **Should `Caixa` be split into smaller, more focused modules?**
  _Cohesion score 0.05576707726763718 - nodes in this community are weakly interconnected._
- **Should `org.junit.jupiter.api.Test` be split into smaller, more focused modules?**
  _Cohesion score 0.04341736694677871 - nodes in this community are weakly interconnected._
- **Should `Busca de Espaço` be split into smaller, more focused modules?**
  _Cohesion score 0.06582427270055834 - nodes in this community are weakly interconnected._