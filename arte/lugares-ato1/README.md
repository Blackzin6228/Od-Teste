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
