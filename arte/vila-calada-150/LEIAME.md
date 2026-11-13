# Vila Calada 150 x 150 (prévia para aprovação)

Decisão de 12/11/2026 (`docs/decisoes/2026-11-12-vila-calada-150x150.md`): o centro de 100 x 100 continua igual e
ganha uma coroa de 25 blocos de arredores. **Nada daqui está no mod (`src/`) ainda**: espera o ok do Nikolas.

Gerado por `bash ferramentas/gerar_vila_calada_150.sh` (modelo em `ferramentas/estruturas/modelos/vila_calada_150.py`).
O centro é o `arte/vila-calada-100/vila_calada.nbt`, **sem mudar um bloco**: o modelo trava a área do centro e o
teste `TesteVila150.test_centro_de_100_fica_intacto` confere bloco a bloco.

## Arquivos
| Arquivo | O que é |
|---|---|
| `vila_calada_150.nbt` | A vila inteira, 150 x 27 x 150. **Passa do limite do mod** (`Estruturas.VOLUME_MAXIMO_PARTES` = 524.288; a caixa tem 607.500). Serve para o Create (Schematicannon) e para a prévia. |
| `partes/vila_calada_arredores_norte.nbt` (150 x 12 x 25) | Cemitério grande, casa do coveiro, beira da floresta, pedras. Origem (0, 0, 0) dentro dos 150. |
| `partes/vila_calada_arredores_sul.nbt` (150 x 6 x 25) | Estrada de chegada, carroça tombada, cerca caída, campos queimados, espantalho. Origem (0, 0, 125). |
| `partes/vila_calada_arredores_oeste.nbt` (25 x 11 x 100) | Trilha para a floresta com marcas de arrasto, fogueira, placa, roça pequena, pinheiros. Origem (0, 0, 25). |
| `partes/vila_calada_arredores_leste.nbt` (25 x 7 x 100) | Caminho dos enterros, cruzeiro de ferro, poço fora do muro, pasto com gado morto. Origem (125, 0, 25). |
| `vila_calada_150.partes.json` | As cinco peças (centro + 4 faixas) e a origem de cada uma. |
| `vila_calada_150.pistas.json` | Pistas do centro (já deslocadas de 25, 0, 25), pistas dos arredores e os **marcadores de recompensa**. |
| `00_vista_de_cima.png`, `01_iso_sudeste.png`, `02_iso_noroeste.png` | A vila inteira. |
| `<parte>_sudeste.png`, `<parte>_noroeste.png` | Arredores de perto: `estrada_carroca`, `campo_espantalho`, `cemiterio`, `poco_fora`, `trilha_floresta`, `pasto_leste`. |

Todas as peças assentam na **mesma altura** (y 0 = chão, em cima do terreno; nada é cavado). Para montar com o
mod: colocar o centro no canto + (25, 0, 25) e cada faixa no canto + origem dela. Se o Dev preferir subir o
limite do mod para 607.500 (ex.: 160 x 32 x 160 = 819.200), o arquivo inteiro serve direto.

## Marcadores de recompensa (para o mod saber onde pôr o baú de enigma)
Convenção: **bloco de estrutura em modo DATA**, `metadata` = nome, **um bloco acima** do baú (ou da jukebox) de
verdade. O baú já está no arquivo, vazio. O mod já ignora blocos de estrutura ao colocar
(`BlockIgnoreProcessor.STRUCTURE_BLOCK` em `Estruturas.ajustes`), então eles não aparecem no mundo: o Dev lê os
marcadores do template (`StructureTemplate.filterBlocks(..., Blocks.STRUCTURE_BLOCK)`, campo `metadata`) e enche o
bloco de baixo. Posições (x, y, z) dentro dos 150; o alvo é y − 1.

| metadata | marcador | alvo (baú/jukebox) | onde |
|---|---|---|---|
| `bau_carroca` | (71, 2, 140) | baú (71, 1, 140) | carga espalhada da carroça tombada, estrada sul |
| `disco_carroca` | (71, 3, 137) | jukebox (71, 2, 137) | em cima do feno da carroça (o mod põe o disco) |
| `bau_cova` | (95, 1, 16) | baú (95, 0, 16) | fundo de uma cova aberta do cemitério grande, no lugar do corpo; lápide cinzelada |
| `bau_poco_fora` | (136, 1, 80) | baú (136, 0, 80) | fundo do poço seco fora do muro, leste |
| `bau_espantalho` | (44, 1, 140) | baú (44, 0, 140) | enterrado aos pés do espantalho do campo, sudoeste |

(As posições exatas saem do `vila_calada_150.pistas.json`; a tabela é cópia dele.) Com o Create, os cinco blocos de
estrutura seriam impressos no mundo: tirar à mão ou usar o mod.

## O que cada lado conta (resumo; detalhe no cabeçalho do modelo)
- **Sul:** a estrada continua a rua principal, com os sulcos de roda e as pegadas de cinza (todas para dentro). Na
  entrada das terras, o marco de pedra (dois pilares, trave caída, placa). A carroça está de lado no meio da
  estrada, uma roda no ar, bois mortos nos varais, carga para o lado em que caiu, jukebox em cima do feno. Cerca
  caída dos dois lados, uma roda solta no campo. Campos queimados colados ao muro e vivos mais longe (o fogo veio da
  vila). O espantalho em pé olha para quem chega.
- **Norte:** cemitério grande numa lombada, grade de ferro, portão a leste com corrente, ~60 covas (abertas com a
  terra para fora, fechadas com o monte, velhas afundadas), casa do coveiro sem teto, ossário, duas árvores mortas.
- **Leste:** caminho dos enterros do portão do cemitério até a rua cruzada (mureta baixa ao lado), cruzeiro de barras
  de ferro na bifurcação (placeholder da Cruz de Ferro), restos de um curral velho, poço seco com o balde pendurado e
  a pilha de lenha, pasto com a cerca caída, o gado morto espalhado, o paiol aberto e o charco que secou.
- **Oeste:** trilha serpenteando para a floresta (sai pelo canto noroeste), dois sulcos de arrasto e gotas vindo da
  floresta para a vila, coisas largadas, fogueira apagada de quem esperou, placa quebrada. Pinheiros, tocos, lenha
  cortada e podzol na beira da floresta.

Acento de cor: só o vermelho do Sangue (pó de redstone apagado). Sem bloco de luz fora do muro (os arredores ficam
mais escuros e mais quietos; criaturas rondam perto do muro). Só blocos vanilla.

## Limites
- A prévia não usa as texturas do jogo (cores por código). Onde o arquivo não grava nada (bordas irregulares e a
  emenda centro/arredores), a prévia pinta grama: no jogo fica o terreno que já existe.
- Baús, barris e jukebox saem vazios. O texto da placa da trilha e o disco vêm do mod/Lore.
- A carroça é a mesma cena-tipo do Ato 1 (jukebox e disco): se levar objeto de enigma, a Lore descreve antes.
