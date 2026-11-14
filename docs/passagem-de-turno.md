# Passagem de turno da mesa CEO

Leia isto ao abrir um chat novo. Atualize no fim de cada sessão grande.

## Estado em 14/11/2026 (noite)
- **No servidor (pack Conguitos-ATM10):** Od-Teste commit `88d3db3` (branch `mesa-dev/evento-impecavel`, PR #80). Inclui: camada de horror (dia leve, noite 0.55), shaders de Sangue, Vila Calada v2 (canto 3928 63 3954), minimapa com chiado, tocha clareando ~8 blocos, modo mestre (`/odteste mestre ouvir`, `/odteste mestre status <grupo>`), `/odteste vila_calada preparar`, comandos sem vazar no chat.
  - Voltar: commit anterior `3d87a00` em `extras/mod-novo.txt`.
- **Mods de jogador adicionados no pack:** LambDynamicLights (tocha na mão). Também: Sound Physics Remastered, AmbientSounds, Entity Culling.
- **Grupo `conguitos`** (m3r1d4, kayoshin, giomeioassim, pkzim): marcos `primeiro_sinal`, `enigma_sino`. Próximo: achar a Vila Calada.
- **Mestre:** Nikolas (Blackzin6228). Falta pôr `mestres = "Blackzin6228"` em `serverconfig/odteste-mestre-server.toml`.

## Em andamento
- Mesa Dev `mesa-dev/fx-melhores`: FX de Sangue melhores, compat com Iris, olho vermelho na lua (Lua de Sangue do Enhanced Celestials ou noite na Vila Calada). Prévias em `previas-fx/`. Nada entra sem o Nikolas ver.
- Lore PR #81: `lore/metodo-horror-rpg.md` e `lore/propostas-ato1-ato2.md` (propostas, não canon).
- Plano novo: `docs/decisoes/2026-11-14-plano-qualidade.md`.

## Decidido em 14/11 (noite)
- Enigma da Vila Calada: Proposta A "O Dobre" (`docs/decisoes/2026-11-14-enigma-vila-calada-dobre.md`). Mesa Dev implementa em `mesa-dev/dobre`; Modelador faz Martelo do Ferreiro e Faminto (prévias).
- Pack: Sound Physics Remastered, AmbientSounds, Entity Culling publicados.

- Cutscenes: PR #84 (`mesa-dev/cutscenes`, 72767e5, Actions verde, não testado no jogo). Cena `chegada_vila_calada` só por comando. Rede versão 7. Comando agora é `tocar <cena> <alvos>`.
- Modelos: branch `mesa-modelador/martelo-faminto` (3b54fde), prévias Faminto A/B e Martelo V1/V2 em `previas-modelos/`. Aguardando escolha + resolução 16/32.

- O Dobre: PR #83 (`mesa-dev/dobre`, fea9d9b, Actions verde, não testado no jogo). Item `odteste:martelo_do_ferreiro` (textura provisória). Comandos de teste em `docs/kit-do-mestre.md`/PR. **Atenção:** #83 e #84 sobem a rede para versão 7 e mexem em `Rede.java`: ao juntar, resolver o conflito e subir para 8.
- Próximo passo combinado: juntar #80 + #83 + #84 + FX numa versão de teste única, Nikolas testa no PC (vídeo/prints) antes de ir ao servidor.

## Pendente do Nikolas
- Olho na lua: significado A/B/C (Lore sugere B+C) e nome ("Lua Injetada" / "A Lua que Olha").
- Baús da v2, mapa da pista 3, livro levado (recomendado: só baú da cova; mapa comum com X; volta pelo `preparar`).
- PRs #74–#77, #79, #80 ainda não juntados no `main` (o servidor usa o branch).
