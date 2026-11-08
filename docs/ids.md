# IDs do mod

Tudo que o mod registra (itens, blocos, criaturas, efeitos, dados salvos no jogador e no mundo) entra aqui.
Enquanto o mod não foi lançado no servidor, pode mudar. **Depois do lançamento, um ID listado aqui nunca some**
sem uma conversão (remap) aprovada pelo Nikolas.

Modid: `odteste` (provisório) · Lançado no servidor: **não**

| ID | Tipo | Desde | Observação |
|---|---|---|---|
| `odteste:exposicao` | dado do jogador (inteiro 0–100) | 07/11/2026 | sobrevive à morte |
| `odteste:lucidez` | dado do jogador (inteiro 0–lucidez_max) | 07/11/2026 | sobrevive à morte |
| `odteste:lucidez_max` | dado do jogador (inteiro 10–100) | 07/11/2026 | sobrevive à morte; padrão 100 |
| `odteste:marca_elemento` | dado do jogador (texto: `nenhum`, `sangue`, `conhecimento`, `morte`, `energia`) | 07/11/2026 | sobrevive à morte; texto desconhecido vira `nenhum` |
| `odteste:marca_estagio` | dado do jogador (inteiro 0–4; 0 só sem elemento) | 07/11/2026 | sobrevive à morte |
| `odteste:transcender_nivel` | dado do jogador (inteiro 0–4) | 07/11/2026 | sobrevive à morte |
| `odteste:sincronizar_jogador` | pacote de rede (servidor → cliente) | 07/11/2026 | manda os 6 valores acima só para o próprio jogador |

Os textos de elemento (`nenhum`, `sangue`…) também são IDs: ficam salvos no jogador.
