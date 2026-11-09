# Ferramenta de estruturas (schematic)

Monta as estruturas grandes da história **por código**, em módulos, e exporta `.nbt` no formato de estrutura
vanilla do Minecraft 1.21.1 (DataVersion 3955). O Create lê esse arquivo na pasta `schematics/`.
Python 3 puro: **nenhuma dependência** (nem nbtlib, nem Pillow). NBT e PNG são escritos pela própria ferramenta.

## Como usar
```bash
bash ferramentas/estruturas/gerar.sh                 # gera todas
bash ferramentas/estruturas/gerar.sh capela_fazenda  # só uma
python3 -I ferramentas/estruturas/validar.py arte/estruturas/capela_fazenda.nbt   # só confere um arquivo
python3 -I ferramentas/estruturas/testar.py          # testes
```
Estrutura grande (vila): `bash ferramentas/gerar_vila.sh` gera o `.nbt`, roda os testes e faz as prévias por parte
(`previa_partes.py`, saída em `arte/previas/estruturas/<nome>/`: vista de cima inteira + frente/costas/lado/cima/corte de
cada parte listada em `PARTES` no modelo).

Vila Calada (100 x 100, destino do Ato 1): `bash ferramentas/gerar_vila_calada.sh` roda os testes, gera
`arte/vila-calada-100/vila_calada.nbt` + pistas e as prévias (vista de cima, dois ângulos e as partes de perto).
A camada y 0 dela **assenta em cima do terreno** (nada abaixo, nada cavado): serve para a colocação em partes do mod.

Saída: `arte/estruturas/<nome>.nbt`, `arte/estruturas/<nome>.pistas.json` e as prévias em `arte/previas/estruturas/`.
A mesma semente gera sempre o mesmo arquivo (o diff no git só muda quando a estrutura muda).

## Como colocar no mundo
- **Create (Schematicannon):** copie o `.nbt` para a pasta `schematics/` da instância do jogo (cliente). Na Mesa de
  Esquemas, carregue o arquivo no Esquema vazio, posicione com o Esquema e use o Canhão de Esquemas.
  No canhão, use a opção de substituir blocos para que o ar de dentro dos cômodos limpe terra e árvore.
- **Comando (admin):** copie para `<mundo>/generated/minecraft/structures/capela_fazenda.nbt` e rode
  `/place template minecraft:capela_fazenda ~ ~ ~` (a origem é o canto noroeste de baixo; o piso é a camada y 0).
- Posição que a estrutura não define fica **fora do arquivo**: o terreno ali não é tocado. Só o interior dos cômodos
  grava ar.

## Como é organizada
| Arquivo | O que faz |
|---|---|
| `estrutura.py` | grade de blocos, `Mistura` (variação de bloco com peso), ruído em manchas, exportação NBT |
| `modulos.py` | paredes, piso, telhado de duas águas, empena, janela, porta, cama, cadeira, mesa, banco, lanterna, corrente, vela, desgaste (envelhecer, buracos, teias, musgo, trepadeira), sangue (mancha, rastro) |
| `blocos.py` | lista de blocos permitidos, propriedades válidas de cada um, cor e formato para a prévia |
| `validar.py` | relê o `.nbt` e confere DataVersion, size, palette, blocks, entities, nomes e propriedades |
| `render.py` | prévia PNG: isométrica (4 lados e cortes por altura) e planta de cima por andar |
| `modelos/` | uma estrutura por arquivo (`construir()` e `CORTES`) |

Estrutura nova: crie `modelos/<nome>.py` com `NOME`, `TAM`, `CORTES` e `construir()`, e registre em `modelos/__init__.py`.

## Blocos permitidos
Só **vanilla 1.21.1** (o ATM10 tem todos; nenhum bloco de mod). Assim a estrutura abre em qualquer pack e não quebra
se um mod sair. A lista exata está em `blocos.py` (`BLOCOS`); o validador recusa qualquer outro.

Pedra: `cobblestone`, `mossy_cobblestone`, `stone_bricks`, `mossy_stone_bricks`, `cracked_stone_bricks`,
`chiseled_stone_bricks`, `stone_brick_stairs`, `stone_brick_slab`, `polished_andesite`, `calcite`, `diorite`, `bricks`,
`deepslate_tiles`, `deepslate_tile_stairs`, `deepslate_tile_slab`, `cracked_deepslate_tiles`, `gravel`.
Madeira: `spruce_planks`, `spruce_stairs`, `spruce_slab`, `spruce_fence`, `spruce_door`, `spruce_trapdoor`,
`spruce_log`, `stripped_spruce_log`, `dark_oak_planks`, `dark_oak_stairs`, `dark_oak_slab`.
Vidro, metal, luz: `glass_pane`, `iron_bars`, `chain`, `lantern`, `candle`, `red_candle`, `bell`.
Móveis: `barrel`, `chest`, `furnace`, `smoker`, `crafting_table`, `cauldron`, `lectern`, `bookshelf`, `campfire`,
`white_bed`, `light_gray_bed`, `ladder`, `flower_pot`, `brown_carpet`, `hay_block`, `composter`.
Natureza e desgaste: `cobweb`, `vine`, `moss_carpet`, `short_grass`, `fern`, `dead_bush`, `coarse_dirt`, `dirt_path`.
Sangue: `redstone_wire` (pó de redstone apagado, sem ligação).
Vila Calada: `light` (luz invisível, só contra monstro comum), `bone_block`, `stone_button`, `spruce_sign`,
`damaged_anvil`, `carved_pumpkin`, `white_carpet`, `red_bed`, `mud`, `red_stained_glass_pane`.
Para usar um bloco novo: acrescente em `BLOCOS` (e as propriedades em `_FAMILIAS`, se tiver).

## Limites conhecidos
- A prévia **não usa as texturas do jogo**: cores e padrões feitos por código. Mostra forma, volume, cor e
  distribuição de blocos; não é um print. Conferir no jogo antes de aprovar de vez.
- Baús, barris e atril saem **vazios** (sem NBT de bloco). Os lugares das pistas estão no `.pistas.json` para a Lore/Dev.
- Pó de redstone como sangue: se alguém colocar uma tocha de redstone perto, ele acende. Não conduz nada sozinho.
- Rotação de escada/porta na prévia é aproximada (porta aberta, alçapão).
