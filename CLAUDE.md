# Mod paranormal do CONGUITOS SMP (nome provisório: Od Teste) — regras para todo agente

Mod **novo, do zero**. Não é continuação do Máscaras Relíquia: o repositório `Blackzin6228/Mascaras` serve só como
referência técnica (como ele resolveu GeckoLib, Curios, Iron's, publicação no pack). Nada de lore, nome ou visual vem de lá.

Antes de trabalhar, leia: este arquivo, `docs/decisoes.md`, `docs/progresso.md` e o arquivo da sua mesa em `.claude/agents/`.

## Projeto
- NeoForge **1.21.1** (21.1.262), Java 21. Modid provisório **`odteste`** (vem do `gradle.properties`).
- Modpack: `Blackzin6228/Conguitos-smp` (packwiz, ~410 mods). O mod compila contra os jars **desse** pack.
- Dono: **Nikolas** (GitHub `Blackzin6228`). Meta: projeto grande, para um servidor futuro que gere renda.

## Regras que não mudam
1. **Português**, frases curtas, sem jargão quando falar com o Nikolas.
2. **Perguntar antes de decidir** visual, jogo, lore ou qualquer coisa difícil de desfazer. Mostrar prévia
   (imagem, print, render do Blockbench) antes de integrar. Decisão aprovada vai para `docs/decisoes.md`, com data.
3. **Inspiração só no gênero** (`docs/estilo-do-genero.md`). Nada copiado de Ordem Paranormal ou de outra obra:
   nomes próprios, símbolos, criaturas, personagens, textos, músicas. Nomes genéricos em português são ok
   ("Lâmina Rubra", "Selo de Giz"); termos que identificam outra obra, não.
4. **Integração sem dependência dura.** Só NeoForge e GeckoLib são obrigatórios. Todo código que toca outro mod
   fica em `compat/<mod>/` e só carrega se `Compat.Integracao.X.ligada()`. Se um mod sair do pack, o nosso abre igual.
5. **Nunca remover mods do pack.** **WATERMeDIA e o plugin do YouTube nunca vão para o servidor.**
6. **IDs são para sempre.** Todo item, bloco, criatura e dado salvo entra em `docs/ids.md`. Depois do primeiro
   lançamento no servidor, um ID só muda com conversão (remap) aprovada.
7. **Nada vai para o servidor sem backup e sem o Nikolas pedir.**
8. **Nunca colocar senha, token ou endereço do servidor em arquivo do repositório.** Segredos só em *Secrets*,
   e quem cola é o Nikolas.
9. **Venda só de cosmético.** Pelas regras da Mojang, nada que dê vantagem no jogo pode ser vendido. Se uma ideia
   depende de venda, marcar no PR para o Nikolas avaliar.
10. **Arte própria.** Modelos, texturas e símbolos feitos por nós (ou por IA a partir de referência aprovada).
    Nada baixado de terceiros sem licença registrada em `arte/LICENCAS.md`.

## Como as mesas trabalham juntas
- Cada mesa (agente) trabalha num **branch próprio** (`mesa-dev/...`, `mesa-modelador/...`) e abre **Pull Request** para o `main`.
- O PR só entra com o **Actions verde**: compila, roda os testes **e liga um servidor com o pack inteiro** (`ferramentas/teste_servidor.sh`).
  Se o servidor crashar, o PR não entra. Esse é o seguro contra travamento.
- A mesa **Revisor** revisa todo PR de código antes do Nikolas aprovar.
- Cada mesa mexe só nas pastas dela (tabela no `README.md`). Precisou mexer em outra? Avisa no PR.
- Ao terminar uma etapa, a mesa atualiza `docs/progresso.md`.

## Comandos
- `bash ferramentas/baixar_libs.sh` — baixa os jars do pack para `libs/` (precisa antes do primeiro build).
- `./gradlew build` — compila e testa.
- `bash ferramentas/teste_servidor.sh build/libs/<jar>` — liga servidor com o pack inteiro (precisa de ~12 GB de RAM).

## Commits
- Mensagens em português, curtas, dizendo o que mudou para quem joga.
