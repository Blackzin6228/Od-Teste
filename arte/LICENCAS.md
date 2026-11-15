# Licenças da arte

Toda arte que não foi feita do zero por nós precisa de uma linha aqui (de onde veio, licença, link).

| Arquivo | Origem | Licença |
|---|---|---|
| — | — | — |

Arte própria (não precisa de linha na tabela, fica anotada para saber de onde veio):
- Veias provisórias da Marca de Sangue (`odteste:marca_sangue_1` a `_4`): desenhadas **em código** pela mesa Dev
  (`src/main/java/com/nikolas/odteste/marcas/PadraoVeias.java`), sem imagem de terceiros. Serão trocadas pela textura da mesa Modelador.
- Estruturas (`arte/estruturas/*.nbt`) e prévias (`arte/previas/estruturas/*.png`): montadas **em código** pela mesa Modelador
  (`ferramentas/estruturas/`), só com blocos vanilla. As prévias usam cores procedurais, sem textura do Minecraft nem de terceiros.
- Sons da Vila Calada (`arte/sons/*.ogg`, vento e arrasto): sintetizados **do zero** pela mesa Modelador
  (`arte/sons/gerar_sons.py`), só ruído e filtros. Nenhuma gravação de terceiros.
- Estandarte da vila comum (`arte/estandarte/*.svg`): desenhado à mão em SVG pela mesa Modelador. Símbolos nossos.

## Skills de agente (não é arte do jogo)
- `.claude/skills/minecraft-*`: de Jahrome907/minecraft-agent-skills (GitHub), licença MIT (cópia em `.claude/skills/LICENCA-minecraft-agent-skills.txt`). Instalado em 15/11/2026 a pedido do Nikolas. Só instruções para as mesas; nada disso vai para o jar.
