# Vila Calada v2 ("vila morta"), 14/11/2026

Prévia para o Nikolas aprovar. **Não está no mod ainda**: a v1 (`arte/vila-calada-150/`) continua sendo a do jogo.

## Imagens
- `comparar_praca.png`, `comparar_rua_principal.png`, `comparar_entrada_sul.png`, `comparar_adro_do_sino.png`:
  antes (v1) à esquerda, depois (v2) à direita; de dia em cima, de noite com a névoa carmesim embaixo.
- `01_iso_sudeste.png` e `00_vista_de_cima.png`: a vila inteira.
- As cores são feitas por código (não são as texturas do jogo). Conferir no jogo antes de aprovar de vez.

## Arquivos
- `vila_calada_150_v2.nbt`: a vila inteira (Create e `/place template`).
- `partes/`: o centro de 100 e as 4 faixas de arredores, cada uma cabe na colocação em partes do mod.
- `vila_calada_150_v2.partes.json` e `vila_calada_150_v2.pistas.json`: origem das peças, pistas e marcadores
  (mesmos lugares da v1, mais as duas pistas do Adro do Sino).

## Gerar de novo
`python3 -I ferramentas/estruturas/previa_vila_calada_150_v2.py` (as comparações 3D precisam de numpy, Pillow e moderngl).
