# Marca de Sangue v2 (mesa Modelador)

Refação da Marca de Sangue no corpo do jogador. A versão anterior (PR #25, arquivos em `arte/marca-sangue/`)
ficou fraca: veias em linha de grade, espessura igual, estágio 4 virando mancha e brilho em retângulo.
Esta versão é gerada por script (reproduzível) e **ainda não está ligada ao jogo**: depende do ok do Nikolas e
depois da mesa Dev (`CamadaMarca` hoje desenha o `PadraoVeias` em código).

## Como gerar

```bash
bash ferramentas/marca_sangue_v2.sh            # texturas + prévias + GIF (uns 10 s)
bash ferramentas/marca_sangue_v2.sh --so-texturas
bash ferramentas/marca_sangue_v2.sh --checar   # confere Pillow/numpy (só numpy é usado)
```

Só precisa de `python3` e `numpy`. PNG e GIF são escritos pelo próprio script (`imagem_np.py`).

## Arquivos

| Arquivo | O que é |
|---|---|
| `estagio_N.png` / `estagio_N_alex.png` | a Marca do estágio N (1 a 4), molde 64x64 da skin, camada de baixo. Steve (braço 4 px) e Alex (braço 3 px) |
| `estagio_N_brilho*.png` | **só o que brilha no escuro**: interior das rachaduras, carne viva da ferida e o coração. Para a camada emissiva (pulso = animação da Dev) |
| `gerar_marca_v2.py` | principal: gera texturas, prévias e GIF |
| `veias.py` | o desenho: lista de veias/rachaduras/gotas por estágio e os pincéis (campo de distância) |
| `corpo.py` | corpo do jogador com o UV do Minecraft, skins de teste e render por raio |
| `imagem_np.py` | PNG/GIF/colagem/texto sem Pillow |

Prévias em `arte/previas/marca-sangue-v2/` (antes x depois por estágio, zoom 16x, ferida 40x, folha geral,
guia do molde com as faces e `brilho_pulsando.gif`).

## Como foi feito (para retocar)

- Cada parte do corpo é uma **faixa contínua** do molde (lado direito | frente | lado esquerdo | costas), que dá a
  volta no corpo. As veias são curvas suaves (Catmull-Rom) desenhadas nessa faixa, então passam de uma face para
  a outra sem quebra. O ombro usa a face de cima do braço e do tronco como ponte.
- Cada veia tem raio no começo e no fim (tronco 2-3 px, galho 1-2 px, ramo 1 px com alfa parcial). O pixel recebe
  cor pela distância à curva: miolo escuro, borda mais viva, **lado da luz claro e lado oposto escuro** (relevo),
  e um halo arroxeado de alfa baixo em volta (pele inflamada).
- Estágios: 1 arranhão diagonal no antebraço direito com 2 veias curtas · 2 veia tronco sobe o braço, passa o
  ombro e desce ao peito e às costas; ferida abre e ganha crosta · 3 braço esquerdo, pescoço, **coração** (nó
  escuro no peito, de onde tudo puxa), **costura** na ferida, primeira gota · 4 pernas, **rachaduras** com borda
  escura e miolo vivo que brilha (braço, costas, barriga, estrela no coração com pontos arrebentados), gotas
  escorrendo, olho direito injetado.
- Paleta: tons de Sangue da decisão de 08/11 (`#3A0306 #7A0A10 #B3131B #E0313A`), acento quente `#FF7A5C` só
  onde brilha, roxo `#4A0C2E` com alfa baixo para a pele inflamada, fio da costura quase preto.
- Para ajustar à mão: `arte/previas/marca-sangue-v2/molde_guia_estagio_4_*.png` mostra as faces por cima do molde.

## Limites e pendências

- **Resolução 64x64** (igual à skin): costura é fio de 1 px e no antebraço de 3-4 px tudo fica apertado. Se o
  Nikolas quiser mais detalhe, dá para gerar a mesma arte em 128x128 (o modelo aceita), mas é decisão de
  resolução pendente em `docs/decisoes.md`.
- Olho injetado assume olho no lugar do Steve/Alex padrão (pixels 9-10, 12 do molde). Em skins diferentes, o
  tom vermelho cai em outro ponto do rosto: é opcional e pode ser desligado.
- Só a camada de baixo da skin. A camada de cima (jaqueta, chapéu) fica livre.
- A Dev liga: trocar `PadraoVeias` por estes PNGs e desenhar `*_brilho` com `RenderType.eyes`/emissivo pulsando.
