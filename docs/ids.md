# IDs do mod

Tudo que o mod registra (itens, blocos, criaturas, efeitos, dados salvos no jogador e no mundo) entra aqui.
Enquanto o mod não foi lançado no servidor, pode mudar. **Depois do lançamento, um ID listado aqui nunca some**
sem uma conversão (remap) aprovada pelo Nikolas.

Modid: `odteste` (fixo para sempre; o nome visível pode mudar) · Lançado no servidor: **sim, 08/11/2026 (mundo definitivo)**

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
| `odteste:paranormal` | tag de tipo de dano (`data/odteste/tags/damage_type/`) **e** tag de criatura (`.../entity_type/`) | 08/11/2026 | dano paranormal pesa mais na Lucidez; hoje vazias |
| `odteste:comida_quente` | tag de item (`data/odteste/tags/item/comida_quente.json`, `"replace": false`) | 08/11/2026 | comer devolve Lucidez; hoje os 4 ensopados, a batata assada e as 5 carnes cozidas vanilla; outros mods somam por datapack/compat |
| `odteste:marca_sangue_1` | textura gerada no cliente (veias, estágio 1) | 08/11/2026 | provisória, desenhada em código (`PadraoVeias`) |
| `odteste:marca_sangue_2` | textura gerada no cliente (estágio 2) | 08/11/2026 | idem |
| `odteste:marca_sangue_3` | textura gerada no cliente (estágio 3) | 08/11/2026 | idem |
| `odteste:marca_sangue_4` | textura gerada no cliente (estágio 4) | 08/11/2026 | idem |
| `odteste:exposicao_repeticao` | dado do jogador (mapa `fonte/elemento` → [início da janela em tempo do mundo, vezes]) | 08/11/2026 | freio de repetição da Exposição; salvo para relogar não zerar; só guarda janelas abertas (10 min); sobrevive à morte; não vai para o cliente |
| `efeitos_fortes` | chave de config do cliente (`config/odteste-client.toml`: `LIGADO`, `SUAVE`, `DESLIGADO`) | 08/11/2026 | fica no PC do jogador; renomear perde a escolha de quem usa suave/desligado |

Fontes de Exposição (usadas no comando `/exposicao contato` e no freio de repetição). Ficam salvas dentro de
`odteste:exposicao_repeticao` como `fonte/elemento` (ex.: `criatura_vista/sangue`), então também não mudam:
`criatura_vista`, `criatura_luta`, `criatura_morta`, `reliquia_pegar`, `reliquia_usar`, `ritual`, `dimensao`, `aparicao`.

Abalos de Lucidez (comando `/lucidez abalar`, **não** ficam salvos): `batalha_reliquia`, `aparicao`, `mob_sangue`.

Os textos de elemento (`nenhum`, `sangue`…) também são IDs: ficam salvos no jogador.
