# Progresso

Cada mesa atualiza a sua parte ao terminar uma etapa. Legenda: ⬜ não começou · 🟨 em andamento · 🟦 esperando aprovação · ✅ pronto

## Base
🟦 Projeto NeoForge compilando contra os jars do pack, integrações opcionais, testes de consistência
🟦 Actions: compilar + ligar servidor com o pack inteiro
🟦 Regras, mesas e documentos

## Lore
🟦 Artefatos Paranormais revisados com a regra de tom sério (08/11/2026): 5 objetos trocados, 4 ajustados, 2 opções de nome cada, esperando a escolha do Nikolas (`lore/artefatos-paranormais.md`)
🟦 Horror de Exposição e Lucidez (08/11/2026): tabelas por faixa, frases dos comandos, acessibilidade e lang (`docs/horror.md`), esperando o ok do Nikolas
⬜ Propostas de elementos (2 ou 3) · ⬜ escolha do Nikolas · ⬜ papel de cada mod (revisar `docs/integracoes.md`) · ⬜ primeiro arco

## Dev
🟦 Dados do jogador (Exposição, Lucidez, Marca, Transcender) · 🟦 comandos de admin · 🟦 desenho dos sistemas (`docs/sistemas.md`, com [DECIDIR]) · 🟦 plano de animações (`docs/animacoes.md`) · 🟦 play test de Sangue, parte 1: Exposição só por contato paranormal (pontos de entrada + `/exposicao contato`), Lucidez cai com dano (paranormal pesa mais) e abalos, volta descansando, dormindo e com luz + grupo + comida quente (tag `odteste:comida_quente`), efeitos de tela e surto, Marca aparece sozinha, Marca de Sangue no corpo (veias por estágio, todos veem), animação provisória (Player Animator) · ⬜ play test de Sangue, parte 2 (conteúdo: criatura/relíquia de Sangue chamando `Exposicao.contato` e `Lucidez.abalar`) · ⬜ primeiro item de progressão · ⬜ primeiro ritual
🟦 Mundo definitivo (2026-11-08): modid `odteste` fixo, remap obrigatório, plano de estruturas por evento/comando (`docs/sistemas.md`) · ⬜ implementar estruturas por comando
🟦 Dados do jogador (Exposição, Lucidez, Marca, Transcender) · 🟦 comandos de admin · 🟦 desenho dos sistemas (`docs/sistemas.md`, com [DECIDIR]) · 🟦 plano de animações (`docs/animacoes.md`) · 🟦 play test de Sangue, parte 1: Exposição só por contato paranormal (pontos de entrada + `/exposicao contato`), Lucidez cai com dano (paranormal pesa mais) e abalos, volta descansando, dormindo e com luz + grupo + comida quente (tag `odteste:comida_quente`), efeitos de tela e surto, Marca aparece sozinha, Marca de Sangue no corpo (veias por estágio, todos veem), animação provisória (Player Animator) · 🟦 mais horror, base técnica (08/11/2026): nível de horror junta Lucidez e Exposição; alucinações só no cliente (passos, sussurro com eco, coração, vulto no canto que some ao olhar, criatura falsa que some ao chegar perto, porta/alçapão que parece abrir), tela lavada, visão que "respira", balanço da câmera, mão tremendo, lentidão leve na Lucidez baixa (servidor, só na troca de faixa), `/exposicao` e `/lucidez` sozinhos mostram símbolo + frase (texto provisório para a Lore, `docs/horror.md`) · 🟦 PR #15 ajustado ao `docs/horror.md` (08/11/2026): espera mínima por faixa (4 min a 75 s, sorteio até 4 min no pior caso), grupo perto corta pela metade, vulto a 12-16 blocos, criatura some a 3 blocos, eco 6/14 ticks, sem som de Enderman, mão e visão só nas situações da tabela, lentidão −8% só com pouca luz (servidor) e −5% com vulto perto (cliente), vulto e criatura desenhados sem entrar no mundo (minimapa não vê), porta falsa que sempre volta ao estado real, frases da Lore com 3 variações, nome sussurrado no chat, SUAVE como a tabela 1.4/2.4 · ⬜ play test de Sangue, parte 2 (conteúdo: criatura/relíquia de Sangue chamando `Exposicao.contato` e `Lucidez.abalar`) · ⬜ primeiro item de progressão · ⬜ primeiro ritual

## Modelador
⬜ Guia de estilo visual aprovado · ⬜ primeiro item (prévia) · ⬜ primeira armadura · ⬜ primeira criatura

## Integrador
🟦 Plano de progressão e crafts (`docs/progressao.md`, armaduras acima da MekaSuit) · 🟦 Troca de voz com controle do jogador e do admin (`docs/voz.md`) · 🟦 Magias por elemento (`docs/magias.md`) · ⬜ Escola nova no Iron's (1 magia de teste) · ⬜ ritual no Occultism · ⬜ ponte com Malum · 🟦 ramo tech (máquinas = misto, decidido 08/11): processamento nas máquinas do pack (mods conferidos), 3 aparelhos nossos com ficha para o Modelador, eventos de Energia e pistas por fita (`docs/tech-paranormal.md`; receitas JSON ainda não escritas) · 🟦 **Analisador de Sinal** (2026-11-08, primeiro aparelho nosso): bloco `odteste:analisador_sinal` com bateria FE (aceita cabo de qualquer mod), gasta 20 FE/t só com jogador perto, acende quando há sinal a até 8 blocos e, ao ser usado (vazio ou com item marcado), mostra o texto decifrado e a segunda camada; receitas Mekanism/Create (só com o mod) e reserva vanilla mais cara; interface `sinais/` para a Dev ligar o sino marcado e os livros; modelo **provisório** (caixa com texturas vanilla de referência) até o Modelador

## Revisor
⬜ Checklist de revisão em uso nos PRs

## Publicador
⬜ Botão "Publicar no pack" (modo teste) · ⬜ primeira publicação (só com o Nikolas pedindo)
