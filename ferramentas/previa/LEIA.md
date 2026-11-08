# Prévia de modelos

Gera um PNG com 4 vistas (**frente, lado, costas, 3/4**) de um modelo do Blockbench, para mostrar ao Nikolas antes de
integrar. Só precisa de `python3` (já vem no Ubuntu/WSL); não instala nada.

```bash
# .bbmodel (textura embutida ou ao lado do arquivo)
bash ferramentas/previa/previa.sh arte/blockbench/espada_exemplo.bbmodel --saida arte/previas/espada_exemplo.png

# .geo.json do GeckoLib (precisa da textura)
bash ferramentas/previa/previa.sh caminho/modelo.geo.json --textura caminho/textura.png --saida arte/previas/x.png

# recria os modelos de exemplo e as prévias deles
bash ferramentas/previa/previa.sh exemplos
```

Opções: `--tamanho 320` (pixels de cada vista), `--fundo 34303b` (cor de fundo).

- A frente do modelo olha para o **norte** (igual ao Blockbench). "LADO" mostra o lado **direito do modelo** (leste).
- A régua no canto mede **1 bloco** (16 pixels do Minecraft).
- Lê cubos, grupos/ossos com rotação, `inflate`, UV por face e box UV. Malha livre (mesh) é ignorada com aviso.
- Não desenha animação: mostra a pose parada do arquivo.

**Teste:** `teste/exemplo.geo.json` é o mesmo cubo de `arte/blockbench/exemplo.bbmodel`. As duas prévias
(`arte/previas/exemplo.png` e `teste/exemplo_geo.png`) têm de sair iguais, com o "F" legível na frente.
