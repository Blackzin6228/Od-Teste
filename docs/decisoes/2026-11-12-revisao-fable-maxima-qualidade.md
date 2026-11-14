# Decisão de 12/11/2026: todo trabalho visual passa por revisão da mesa Modelador no Fable 5.2

Decisão do Nikolas. Vale para toda a versão 0.2 em diante.

## A regra
Todo trabalho **visual** passa por uma **revisão da mesa Modelador, rodando no Fable 5.2**, antes de chegar ao Nikolas.

Conta como visual:
- modelos, texturas e animações;
- efeitos de tela, partículas e névoa;
- cenas de câmera;
- estruturas (aparência, construção, acabamento).

## O que a revisão faz
1. Confere o que foi feito.
2. Compara com `docs/regua-de-qualidade.md` e `docs/referencias.md`.
3. **Melhora** o que puder (efeitos, desenvolvimento das animações, acabamento) até a **máxima qualidade**. Não é só apontar defeito: a mesa corrige e refina.
4. Só depois a prévia vai ao Nikolas.

## O que não muda
- **Código de sistema** continua com a mesa **Revisor**. A revisão do Fable cobre só a parte visual.
- Visual continua sem ser decidido sozinho: prévia (imagem) ao Nikolas antes de integrar, como em `CLAUDE.md`.
- Texto e lore de `lore/` não entram nesta revisão, a não ser quando viram efeito, cena de câmera ou estrutura.
- Um PR com código e visual passa pelos dois: Revisor (código) e Modelador/Fable (visual).

## Quem cuida
- A mesa **CEO** manda a revisão do Fable para o trabalho visual antes de levar a prévia ao Nikolas.
- A mesa **Lore**, quando propõe cena, efeito ou estrutura, marca na proposta que o resultado visual passará por essa revisão.
- Sem IDs novos aqui.
