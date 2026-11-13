# Mod paranormal do CONGUITOS SMP (nome provisório: Od Teste) — regras para todo agente

Mod **novo, do zero**. Não é continuação do Máscaras Relíquia: o repositório `Blackzin6228/Mascaras` serve só como
referência técnica (como ele resolveu GeckoLib, Curios, Iron's, publicação no pack). Nada de lore, nome ou visual vem de lá.

Antes de trabalhar, leia: este arquivo, `docs/decisoes.md` + a pasta `docs/decisoes/`, `docs/progresso.md` e o arquivo da sua mesa em `.claude/agents/`.
Para lore e história: leia **`lore/mapa.md` primeiro** e abra só o arquivo de detalhe que precisar (economia de token).

## Projeto
- NeoForge **1.21.1** (21.1.262), Java 21. Modid **`odteste`**, definitivo (vem do `gradle.properties`). O nome que o jogador vê pode mudar.
- O mod **já está no mundo definitivo** do servidor (ATM10). Tudo que muda precisa funcionar num mundo que já existe.
- Modpack: **Conguitos ATM10** (repositório privado `Blackzin6228/Conguitos-ATM10`, publicado em `conguitos-atm10.nikolascampos1915.workers.dev`). ATM10 8.5 + extras, ~530 mods. O teste de servidor usa **esse** pack. (A compilação ainda usa os jars do Conguitos-smp antigo: mesmas versões das APIs.)
- Dono: **Nikolas** (GitHub `Blackzin6228`). Meta: projeto grande, para um servidor futuro que gere renda.

## Regras que não mudam
1. **Português**, frases curtas, sem jargão quando falar com o Nikolas.
2. **Perguntar antes de decidir** visual, jogo, lore ou qualquer coisa difícil de desfazer. Mostrar prévia
   (imagem, print, render do Blockbench) antes de integrar. Decisão aprovada vira **um arquivo novo** em `docs/decisoes/` (`AAAA-MM-DD-assunto.md`, uma decisão por arquivo). Não acrescente linhas no fim de `docs/decisoes.md` (fica como histórico até 08/11/2026): vários PRs mexendo no mesmo fim de arquivo dão conflito.
3. **Elementos e gênero de Ordem Paranormal: pode** (decisão do Nikolas, 07/11/2026). Os quatro elementos
   **Sangue, Morte, Conhecimento e Energia** (e o Medo como força de fundo), com os temas, cores e matérias descritos em
   `docs/estilo-do-genero.md`, são a base da lore. **Não pode:** copiar a arte (símbolos/sigilos desenhados iguais,
   o símbolo do pentagrama, logos), personagens, criaturas com nome e visual da obra, ou textos da obra. Símbolos,
   criaturas e personagens são **nossos**, desenhados no estilo de cada elemento.
4. **Integração sem dependência dura.** Só NeoForge e GeckoLib são obrigatórios. Todo código que toca outro mod
   fica em `compat/<mod>/` e só carrega se `Compat.Integracao.X.ligada()`. Se um mod sair do pack, o nosso abre igual.
5. **Nunca remover mods do pack.** **WATERMeDIA e o plugin do YouTube nunca vão para o servidor.**
6. **IDs são para sempre.** Todo item, bloco, criatura, dado salvo e config entra em `docs/ids.md`. O servidor já
   está no ar: um ID só muda com conversão (remap) aprovada. O modid `odteste` nunca muda.
7. **Nada vai para o servidor sem backup e sem o Nikolas pedir.**
8. **Nunca colocar senha, token ou endereço do servidor em arquivo do repositório.** Segredos só em *Secrets*,
   e quem cola é o Nikolas.
9. **Venda só de cosmético.** Pelas regras da Mojang, nada que dê vantagem no jogo pode ser vendido. Se uma ideia
   depende de venda, marcar no PR para o Nikolas avaliar.
10. **Arte própria.** Modelos, texturas e símbolos feitos por nós (ou por IA a partir de referência aprovada).
    Nada baixado de terceiros sem licença registrada em `arte/LICENCAS.md`.
11. **Estruturas sem geração de mundo.** O terreno já existe. Estrutura da história entra por evento ou comando
    de admin, sem destruir construção de jogador (plano em `docs/sistemas.md`).

## Como as mesas trabalham juntas
- Cada mesa (agente) trabalha num **branch próprio** (`mesa-dev/...`, `mesa-modelador/...`) e abre **Pull Request** para o `main`.
- O PR só entra com o **Actions verde**: compila, roda os testes **e liga um servidor com o pack inteiro** (`ferramentas/teste_servidor.sh`).
  Se o servidor crashar, o PR não entra. Esse é o seguro contra travamento.
- A mesa **Revisor** revisa todo PR de código antes do Nikolas aprovar.
- Cada mesa mexe só nas pastas dela (tabela no `README.md`). Precisou mexer em outra? Avisa no PR.
- Ao terminar uma etapa, a mesa atualiza `docs/progresso.md`.

## Mesa CEO
Quando o pedido **não** manda usar um subagente (ele vem direto do Nikolas para a mesa CEO), você é a **mesa CEO**:
siga `docs/ceo.md`. Você coordena as outras mesas pela ferramenta Agent e responde ao Nikolas no fim.

## Comandos
- `bash ferramentas/baixar_libs.sh` — baixa os jars do pack para `libs/` (precisa antes do primeiro build).
- `./gradlew build` — compila e testa.
- `bash ferramentas/teste_servidor.sh build/libs/<jar>` — liga servidor com o pack inteiro (precisa de ~12 GB de RAM).

## Commits
- Mensagens em português, curtas, dizendo o que mudou para quem joga.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- When graphify-out/graph.json exists, run `graphify query "<question>"` before your first grep, search or source-file read for a codebase question, and put this rule in every subagent prompt that explores code. Read raw files only for lines the graph or an earlier read already located. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
- Depois de mudar código Java, rode `graphify . --code-only` e `graphify cluster-only .` antes do commit (sem chave de API; os docs não entram no grafo).
- A committed graphify-out/graph.json is rewritten whole on every build, so a large diff to it in a pull request is expected. Do not flag it as churn or suggest skipping the graph rebuild or its hooks.
