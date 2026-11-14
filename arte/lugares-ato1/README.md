# Lugares do Ato 1: estruturas (prévia, ainda fora do jogo)

Gerado por `ferramentas/estruturas/gerar_lugares.py` (não edite à mão: rode o gerador).
Cinco estruturas **vanilla 1.21.1** feitas por código, na paleta morta da Vila Calada (madeira escura gasta, terra
escura, pedra suja; vermelho só de Sangue). **Nada está registrado no mod.** Entram só por comando de admin
(regra 11): a Dev liga depois, com a colocação em partes que não destrói construção de jogador.

## Como colocar (teste em mundo de criativo)
- Copie `<nome>/<nome>.nbt` para `<mundo>/generated/minecraft/structures/` e rode
  `/place template minecraft:<nome> X Y Z`, com **Y = (y do bloco de cima do terreno) - y_chao** (tabela abaixo).
  A origem é o **canto noroeste de baixo** (x leste, z sul).
- Create: copie para `schematics/` e use o Canhão de Esquemas com "substituir ar".
- Posição que a estrutura não define fica fora do arquivo: o terreno ali não é tocado. Só os cômodos gravam ar.
- Abaixo de `y_chao` só há porão e cripta (cavados no terreno).

## Marcadores (para a Dev)
Bloco de estrutura em modo **DATA**, com `metadata = "tipo:nome"`. O mod lê os marcadores do template e **não coloca**
o bloco de estrutura (fica ar). Tipos:

| Tipo | Onde fica | O que a Dev liga |
|---|---|---|
| `bau:` `atril:` `disco:` | **um bloco acima** do baú, atril ou jukebox (o alvo) | enche o baú, põe o livro, põe o disco |
| `spawn:` | no ponto (ar) | onde a criatura ou figura aparece |
| `rota:` | no ponto | caminho da perseguição, em ordem (`rota:01`, `rota:02`...) |
| `som:` | no ponto | origem de um som do mod (sempre som vanilla) |
| `evento:` | no ponto | gatilho ou centro de um evento (cadeado, ritual, alavancas, portas) |
| `mapa:` | na frente do mapa-mural | alvo = o bloco do mural atrás; o mod troca por `red_terracotta` (a marca) e devolve o original |

A lista completa com posição e alvo está em `<nome>/<nome>.marcadores.json`. A prévia `previas/marcadores.png`
mostra os pontos (amarelo baú/atril, vermelho spawn, verde rota, ciano som, magenta evento, branco mapa).

## Prévias
Feitas por `render_gl.py` (OpenGL sem tela): luz de bloco e de céu como no jogo, cores aproximadas, **não é print**.
Cada lugar tem `00_prancha.png` (resumo) e, em `previas/`: `dia_<lado>` (4 lados), `corte_<andar>_frente|fundo`,
`planta_<andar>` (de cima, norte para cima, grade de 5 em 5), `noite_longe`, `noite_longe_2`, `noite_perto`
(névoa carmesim, como o shader de Sangue vai fazer) e `marcadores`.


## Os cinco lugares

| Lugar | Arquivo | Tamanho (x, y, z) | y_chao | Marcadores |
|---|---|---|---|---|
| 1. Moinho Parado | `moinho_parado/moinho_parado.nbt` | 36 x 26 x 30 | 2 | 6 |
| 2. Casa do Açougueiro | `casa_acougueiro/casa_acougueiro.nbt` | 48 x 23 x 36 | 7 | 23 |
| 3. Casa do Cartógrafo | `casa_cartografo/casa_cartografo.nbt` | 26 x 30 x 22 | 0 | 19 |

## 1. Moinho Parado

Prólogo. O grupo chega pelo leste, cruza a ponte do rio seco e vê as pás paradas em X (uma quebrada no chão).
Os blocos de nota do último andar tocam o Morse do E1; o baú do moleiro (cadeado de letras) guarda o Diário p. 1
e a metade esquerda do Mapa. Sem spawn dentro do moinho e da casa (luz invisível fraca). Chão em `y_chao = 2`:
o leito do rio desce 2 blocos.

| metadata | posição | alvo | nota |
|---|---|---|---|
| `bau:moleiro_diario` | (9, 4, 26) | (9, 3, 26) | Ao pé da cama. Cadeado de letras (palavra do E1). Diário do Açougueiro p. 1 + metade esquerda do Mapa Rasgado; na tampa, a carvão, o endereço do Atlas. |
| `som:moinho_morse` | (18, 16, 12) | (18, 16, 12) | Os 5 blocos de nota do último andar (x 16-20, y 14, z 11) tocam o Morse do E1. A roda trava no meio; inteira só numa vigília de noite toda. |
| `som:roda_rangendo` | (19, 17, 14) | (19, 17, 14) | A roda de madeira do eixo das pás range sozinha. |
| `evento:cabecas_moleiro` | (7, 6, 23) | (7, 6, 23) | Prateleira de cabeças viradas para a parede (x 9, z 21-24): o mod deixa uma por jogador e tira o resto. |
| `evento:cheiro` | (18, 5, 13) | (18, 5, 13) | Centro do Cheiro (névoa avermelhada leve que cresce com o tempo). Correntes arrastando ao longe. |
| `evento:chegada` | (34, 4, 21) | (34, 4, 21) | Pé da ponte, lado leste: o grupo chega por aqui. |

Outros pontos (sem marcador, só no json):

- `nota` (16, 14, 11): Bloco de nota 1 de 5 (grave = traço).
- `nota` (17, 14, 11): Bloco de nota 2 de 5 (agudo = ponto).
- `nota` (18, 14, 11): Bloco de nota 3 de 5 (grave = traço).
- `nota` (19, 14, 11): Bloco de nota 4 de 5 (agudo = ponto).
- `nota` (20, 14, 11): Bloco de nota 5 de 5 (grave = traço).
- `estandarte` (5, 6, 2): Cruz de Ferro rasgada (provisória: o desenho final é do Modelador).

## 2. Casa do Açougueiro

O grande lugar do Ato 1. Entradas: **porta da frente** (sul, duas folhas: acorda o Magarefe), **porta de ferro** de
botão (leste, lado do curral: a entrada segura) e a **saída de serviço do porão** (alçapão no curral, escada de mão,
túnel até o porão: pega o Magarefe de frente). A rota da perseguição (`rota:01` a `rota:08`) sai do porão, sobe pelo
curral, contorna a casa e entra pela frente. Sem spawn em toda a estrutura (luz invisível fraca). Chão em
`y_chao = 7`: o porão ocupa y 0 a 6.

| metadata | posição | alvo | nota |
|---|---|---|---|
| `bau:porao_livro_trocas` | (28, 3, 12) | (28, 2, 12) | Barril aberto sobre a bancada. Livro de Trocas, metade direita do Mapa e Faca de Osso. Abre com a ordem certa das correntes. |
| `atril:diario_p2_p3` | (31, 9, 20) | (31, 8, 20) | Na ponta do balcão. Diário do Açougueiro, páginas 2 e 3 (ordem dos bichos; tabela dos Riscos de Corte). |
| `spawn:magarefe` | (21, 1, 12) | (21, 1, 12) | Lugar do Magarefe: porão de salga. Não morre no Ato 1. |
| `rota:01` | (30, 2, 12) | (30, 2, 12) | Perseguição: porão, túnel, poço do curral, volta pela frente. |
| `rota:02` | (38, 1, 12) | (38, 1, 12) |  |
| `rota:03` | (42, 2, 12) | (42, 2, 12) |  |
| `rota:04` | (43, 8, 14) | (43, 8, 14) |  |
| `rota:05` | (42, 8, 30) | (42, 8, 30) |  |
| `rota:06` | (34, 8, 30) | (34, 8, 30) |  |
| `rota:07` | (26, 8, 28) | (26, 8, 28) |  |
| `rota:08` | (25, 8, 23) | (25, 8, 23) |  |
| `evento:porta_frente` | (24, 10, 25) | (24, 10, 25) | Abrir a porta da frente acorda o Magarefe. |
| `evento:porta_ferro` | (36, 8, 12) | (36, 8, 12) | Porta de ferro de botão (lado do curral): a entrada segura (E1). |
| `evento:saida_servico` | (43, 8, 12) | (43, 8, 12) | Alçapão da saída de serviço do porão (curral). |
| `evento:alcapao_porao` | (25, 8, 14) | (25, 8, 14) | Alçapão grande (2 x 2) trancado: abre com a ordem certa das correntes. |
| `evento:corrente_cao` | (18, 10, 12) | (18, 10, 12) | Corrente 'Cão': placa em (18, 8, 13), alavanca em (18, 8, 12). Erro: balança. |
| `evento:corrente_vaca` | (22, 10, 12) | (22, 10, 12) | Corrente 'Vaca': placa em (22, 8, 13), alavanca em (22, 8, 12). Erro: balança. |
| `evento:corrente_lebre` | (28, 10, 12) | (28, 10, 12) | Corrente 'Lebre': placa em (28, 8, 13), alavanca em (28, 8, 12). Erro: balança. |
| `evento:corrente_javali` | (32, 10, 12) | (32, 10, 12) | Corrente 'Javali': placa em (32, 8, 13), alavanca em (32, 8, 12). Erro: balança. |
| `evento:pendurado` | (16, 9, 10) | (16, 9, 10) | Corrente vazia: o Pendurado aparece aqui (quem tem mais Exposição). |
| `evento:palheiro_palavra` | (24, 13, 18) | (24, 13, 18) | Viga do palheiro: a palavra riscada que abre o vídeo E7 (a Lore decide; não vai no repositório). |
| `som:moscas` | (26, 10, 12) | (26, 10, 12) | Moscas e correntes rangendo na casa de abate. |
| `evento:defumador` | (24, 9, 18) | (24, 9, 18) | Defumadores (x 20 e 29, z 17) soltando fumaça sem fogo. |

## 3. Casa do Cartógrafo

Casa isolada de papel e silêncio. Entra-se pela porta da frente (sem tranca) ou pela janela grande do salão. O mapa-mural
(parede norte do salão, x 4-12, y 1-4, z 8) tem **12 pontos possíveis** (`mapa:p01` a `mapa:p12`): o mod troca o bloco
alvo por `red_terracotta` (a marca) e devolve o original na manhã seguinte. `mapa:vila_calada` é a marca fixa.
A torre tem escada em caracol até o mirante (luneta, círculo de giz). Sem spawn dentro. Chão em `y_chao = 0`.

| metadata | posição | alvo | nota |
|---|---|---|---|
| `bau:cartografo_terco` | (13, 2, 16) | (13, 1, 16) | Baú do salão: abre com a senha do Atlas (a mesma do vídeo). Guarda o Terço da Moça (gancho do Ato 2). |
| `mapa:p01` | (4, 1, 10) | (4, 1, 9) | Ponto possível da marca (alvo = bloco do mural atrás). A cada manhã o mod sorteia onde ficam as marcas; a última tem o nome de {marcado}. |
| `mapa:p02` | (5, 3, 10) | (5, 3, 9) |  |
| `mapa:p03` | (6, 2, 10) | (6, 2, 9) |  |
| `mapa:p04` | (6, 4, 10) | (6, 4, 9) |  |
| `mapa:p05` | (7, 1, 10) | (7, 1, 9) |  |
| `mapa:p06` | (8, 3, 10) | (8, 3, 9) |  |
| `mapa:p07` | (9, 2, 10) | (9, 2, 9) |  |
| `mapa:p08` | (9, 4, 10) | (9, 4, 9) |  |
| `mapa:p09` | (11, 1, 10) | (11, 1, 9) |  |
| `mapa:p10` | (11, 3, 10) | (11, 3, 9) |  |
| `mapa:p11` | (12, 2, 10) | (12, 2, 9) |  |
| `mapa:p12` | (12, 4, 10) | (12, 4, 9) |  |
| `mapa:vila_calada` | (10, 3, 10) | (10, 3, 9) | Marca fixa (já é red_terracotta no arquivo): a Vila Calada. |
| `evento:circulo_giz` | (20, 20, 7) | (20, 20, 7) | Centro do círculo de giz do mirante: ritual em dupla ou mais, de noite; as cinzas formam a palavra. |
| `evento:luneta` | (19, 21, 4) | (19, 21, 4) | Luneta na janela norte (vire a janela para o lado da Fazenda ao colocar). Depois do Atlas, mostra a Torre das Correntes na neblina. |
| `evento:mesa_cartografia` | (7, 2, 13) | (7, 2, 13) | Mesa de cartografia com o mapa pela metade. |
| `evento:entrada` | (9, 3, 15) | (9, 3, 15) | Porta da frente (sem tranca) e janela grande quebrada do salão. |
| `evento:pendurado_escada` | (21, 9, 4) | (21, 9, 4) | Escada em caracol: o Pendurado aparece; quem fica sozinho aqui perde Lucidez devagar. |

Outros pontos (sem marcador, só no json):

- `escada` (19, 9, 4): Meio da escada em caracol: onde o Pendurado aparece.
