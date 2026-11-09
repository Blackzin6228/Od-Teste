# IDs do mod

Tudo que o mod registra (itens, blocos, criaturas, efeitos, dados salvos no jogador e no mundo) entra aqui.
O mod já está no mundo definitivo do servidor: **um ID listado aqui nunca some** sem uma conversão (remap)
aprovada pelo Nikolas.

Modid: `odteste` (definitivo, decisão de 2026-11-08) · Lançado no servidor: **sim** (mundo ATM10)

| ID | Tipo | Desde | Observação |
|---|---|---|---|
| `odteste:exposicao` | dado do jogador (inteiro 0–100) | 07/11/2026 | sobrevive à morte |
| `odteste:lucidez` | dado do jogador (inteiro 0–lucidez_max) | 07/11/2026 | sobrevive à morte |
| `odteste:lucidez_max` | dado do jogador (inteiro 10–100) | 07/11/2026 | sobrevive à morte; padrão 100 |
| `odteste:marca_elemento` | dado do jogador (texto: `nenhum`, `sangue`, `conhecimento`, `morte`, `energia`) | 07/11/2026 | sobrevive à morte; texto desconhecido vira `nenhum` |
| `odteste:marca_estagio` | dado do jogador (inteiro 0–4; 0 só sem elemento) | 07/11/2026 | sobrevive à morte |
| `odteste:transcender_nivel` | dado do jogador (inteiro 0–4) | 07/11/2026 | sobrevive à morte |
| `odteste:sincronizar_jogador` | pacote de rede (servidor → cliente) | 07/11/2026 | manda os 6 valores acima só para o próprio jogador |
| `odteste:exposicao_resto` | dado do jogador (inteiro 0–99) | 08/11/2026 | centésimos de Exposição até completar 1 ponto; sobrevive à morte; não vai para o cliente |
| `odteste:contato_elemento` | dado do jogador (mapa id do elemento → inteiro ≥ 0) | 08/11/2026 | contato secreto com cada elemento (decide a Marca); sobrevive à morte; elemento desconhecido é ignorado; não vai para o cliente |
| `odteste:momento` | pacote de rede (servidor → clientes a até 32 blocos) | 08/11/2026 | "subiu de degrau / Marca apareceu, trocou, subiu / surto"; versão da rede subiu para 2 |
| `odteste:marca_sangue_forca` | modificador de atributo (dano de ataque) | 08/11/2026 | temporário (não fica salvo); só com Marca de Sangue e menos de metade da vida |
| `odteste:tela_paranormal` | camada da HUD (cliente) | 08/11/2026 | vinheta, borda colorida, escurecer |
| `odteste:animacao_sistemas` | camada de animação do Player Animator (cliente) | 08/11/2026 | só se o Player Animator estiver no pack |
| `odteste:marca_aparece` | animação do jogador (provisória, no código) | 08/11/2026 | Modelador troca pela definitiva |
| `odteste:marca_sobe` | animação do jogador (provisória, no código) | 08/11/2026 | usada também na troca de elemento |
| `odteste:surto` | animação do jogador (provisória, no código) | 08/11/2026 | |
| `odteste:marca_visivel` | pacote de rede (servidor → quem vê o jogador e ele mesmo) | 08/11/2026 | só elemento e estágio da Marca, para desenhar no corpo |
| `odteste:paranormal` | tag de tipo de dano (`data/odteste/tags/damage_type/`), tag de criatura (`.../entity_type/`) **e** tag de item (`.../item/`, desde 08/11/2026) | 08/11/2026 | dano paranormal pesa mais na Lucidez; item paranormal faz a mão tremer (Exposição Média+, parado); hoje vazias |
| `odteste:comida_quente` | tag de item (`data/odteste/tags/item/comida_quente.json`, `"replace": false`) | 08/11/2026 | comer devolve Lucidez; hoje os 4 ensopados, a batata assada e as 5 carnes cozidas vanilla; outros mods somam por datapack/compat |
| `odteste:marca_sangue_1` | textura gerada no cliente (veias, estágio 1) | 08/11/2026 | provisória, desenhada em código (`PadraoVeias`) |
| `odteste:marca_sangue_2` | textura gerada no cliente (estágio 2) | 08/11/2026 | idem |
| `odteste:marca_sangue_3` | textura gerada no cliente (estágio 3) | 08/11/2026 | idem |
| `odteste:marca_sangue_4` | textura gerada no cliente (estágio 4) | 08/11/2026 | idem |
| `odteste:exposicao_repeticao` | dado do jogador (mapa `fonte/elemento` → [início da janela em tempo do mundo, vezes]) | 08/11/2026 | freio de repetição da Exposição; salvo para relogar não zerar; só guarda janelas abertas (10 min); sobrevive à morte; não vai para o cliente |
| `efeitos_fortes` | chave de config do cliente (`config/odteste-client.toml`: `LIGADO`, `SUAVE`, `DESLIGADO`) | 08/11/2026 | fica no PC do jogador; renomear perde a escolha de quem usa suave/desligado. Desde 08/11 também desliga as alucinações que se veem (vulto, criatura, porta), a tela lavada, o balanço e a mão tremendo; SUAVE: porta só com som, vulto e criatura somem em 1 s, tela e corpo pela metade |
| `odteste:lucidez_lentidao` | modificador de atributo (velocidade de movimento) | 08/11/2026 | temporário (não fica salvo); só Lucidez Quebrando (e surto) **com luz 7 ou menos**: −8% (`docs/horror.md` 2.3); conferido na checagem de 2 s do Vigia, na troca de faixa, ao entrar e ao renascer |
| `odteste_historia` | dado do mundo (`SavedData`, arquivo `data/odteste_historia.dat` do overworld) | 2026-11-08 | grupos (membros e marcos), pontos ancorados, vilas marcadas (com backup dos blocos e o aldeão que sumiu), estruturas colocadas por comando e aldeões que ainda vão voltar ao normal. Chaves dentro do arquivo também não mudam: `grupos`, `ancoras`, `vilas`, `colocacoes`, `descongelar`, `proxima_colocacao` |
| `odteste:substituivel` | tag de bloco (`data/odteste/tags/block/substituivel.json`, `"replace": false`) | 2026-11-08 | o que a história pode ocupar além do ar: grama curta e alta, samambaia, arbusto morto, neve fina. Tudo o resto conta como possível construção de jogador |
| `odteste:livro_carniceiro_1` | peça de história (item vanilla `minecraft:written_book` com custom_data `{odteste_peca: "odteste:livro_carniceiro_1"}`) | 2026-11-08 | Livro do Carniceiro, página 1 (Enigma 1). Texto provisório |
| `odteste:mapa_rasgado_esquerda` | peça de história (item vanilla `minecraft:paper` com custom_data `{odteste_peca: "odteste:mapa_rasgado_esquerda"}`) | 2026-11-08 | metade esquerda do Mapa Rasgado (Enigma 1). Provisório: vira mapa de verdade no Enigma 2 |
| `odteste_peca` | chave do custom_data dos itens de história | 2026-11-08 | o mod reconhece as peças por ela; o valor é o ID da peça (linhas acima) |

História (Ato 1, parte A). Ficam salvos dentro de `odteste_historia`, então também não mudam:
- **Marcos do grupo:** `primeiro_sinal` (Cena 1 solta para o grupo), `enigma_sino` (FERRO escrito na placa, baú aberto).
- **Lugares que o admin ancora:** `vila_do_sino` (onde procurar a vila da Cena 1).
- **Nomes de grupo:** quem escolhe é o admin (`/odteste grupo criar <nome>`). Renomear um grupo perde o progresso dele.
- Os eventos do comando (`/odteste evento primeiro_sinal`) e os ids de colocação (`e1`, `e2`...) não são IDs de conteúdo:
  o evento não fica salvo pelo nome, e o id de colocação só serve para o `desfazer`.
- Aldeões parados olhando o sino ficam com `NoAI` e invulneráveis **só enquanto há jogador perto**; o estado antigo
  deles fica salvo para voltar igual (desfazer, chunk recarregado ou ninguém perto).

Alucinações (só no cliente, **nada registrado nem salvo**): o vulto e a criatura falsa usam os modelos vanilla
`minecraft:enderman` e `minecraft:zombie` como molde provisório, **desenhados por evento de render e nunca
adicionados ao mundo do cliente** (minimapa, radar e Jade não veem). A porta falsa muda só a cópia do bloco no
cliente, dentro da "previsão de bloco" do jogo, que guarda o estado real e devolve ele no fim. A lentidão sutil com
o vulto perto (Exposição Crítica, −5%) é só no movimento do cliente, sem atributo nem ID. Os nomes das alucinações
(`RegrasHorror.Alucinacao`) não são salvos e podem mudar.

Chaves de texto (não são IDs salvos), da Lore (`docs/horror.md` seção 4): `odteste.horror.exposicao.<faixa>.<1-3>`
(`comum`, `baixa`, `media`, `alta`, `critica`), `odteste.horror.lucidez.<faixa>.<1-3>` (`firme`, `alta`, `media`,
`baixa`, `surto`), mostradas por `/exposicao` e `/lucidez` sem nada depois (sorteio, nunca a mesma duas vezes
seguidas), e `odteste.horror.sussurro.nome`, `.nome.2`, `.voz` (nome sussurrado no chat, só no cliente).

Fontes de Exposição (usadas no comando `/exposicao contato` e no freio de repetição). Ficam salvas dentro de
`odteste:exposicao_repeticao` como `fonte/elemento` (ex.: `criatura_vista/sangue`), então também não mudam:
`criatura_vista`, `criatura_luta`, `criatura_morta`, `reliquia_pegar`, `reliquia_usar`, `ritual`, `dimensao`, `aparicao`.

Abalos de Lucidez (comando `/lucidez abalar`, **não** ficam salvos): `batalha_reliquia`, `aparicao`, `mob_sangue`.

Os textos de elemento (`nenhum`, `sangue`…) também são IDs: ficam salvos no jogador.
