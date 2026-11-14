# Sons da Vila Calada (item 1.12 do plano do Ato 1): prévia

> **Ainda não executado.** O script foi escrito mas **não rodou**: nesta rodada a mesa Modelador não tinha permissão
> para rodar `python3`. Os `.ogg` ainda não existem. Quando o Nikolas liberar, basta rodar o comando abaixo
> e ouvir os quatro arquivos antes de aprovar.

**Só prévia.** Nada disto entra em `src/` nem em `sounds.json` antes do ok do Nikolas.
**O sino não está aqui:** o sino como som ambiente espera decisão do Nikolas.

## O que sai
| Arquivo | Som | Diferença entre as variações |
|---|---|---|
| `vento_1.ogg` | Vento entre casas queimadas, 12 s | Rajadas lentas, assobio mais agudo (~1150 Hz) |
| `vento_2.ogg` | Vento entre casas queimadas, 12 s | Rajadas mais curtas, assobio mais grave (~820 Hz) |
| `arrasto_1.ogg` | Algo pesado arrastado ao longe, 9 s | 3 puxões, peso médio |
| `arrasto_2.ogg` | Algo pesado arrastado ao longe, 9 s | 4 puxões, peso mais grave |

Todos **mono** (o Minecraft só dá direção e distância a sons mono), 44,1 kHz, com entrada e saída suaves.

## Como gerar
```
python3 -I arte/sons/gerar_sons.py
```
Precisa de um destes para gravar `.ogg`: o pacote `soundfile` (`pip install soundfile`), o `ffmpeg` ou o `oggenc`
(`sudo apt install vorbis-tools`). Sem nenhum, o script deixa `.wav` e avisa. A semente é fixa: o resultado é sempre igual.

## Como foram feitos
Sintetizados **do zero** por `gerar_sons.py` (mesa Modelador, 09/11/2026), só com ruído aleatório e filtros
(as fórmulas de filtro do *Audio EQ Cookbook*, de domínio público). Nenhuma gravação ou amostra de terceiros.
- **Vento:** ruído passando por um filtro que muda de tom com as rajadas, mais um grave constante. No auge de cada
  rajada aparece um assobio fino e trêmulo, como ar passando por fresta de parede queimada.
- **Arrasto:** em cada puxão o peso "prende e solta" (grãos ásperos de 8 a 22 por segundo) e termina num baque
  quando assenta no chão. Depois tira os agudos e põe um eco curto, para soar longe e entre casas.

## Onde entrariam (depois do ok)
- Pasta do jogo: `src/main/resources/assets/odteste/sounds/vila_calada/`.
- Eventos de som sugeridos (IDs novos, vão para `docs/ids.md` só quando aprovados):
  `odteste:vila_calada.vento` (sorteia entre as 2 variações) e `odteste:vila_calada.arrasto` (idem).
- Uso: o **vento** como ambiente perto da âncora `vila_calada`, a qualquer hora; o **arrasto** de vez em quando,
  só à noite, tocado longe do jogador (atrás de uma casa), para ele procurar e não achar nada.
  A parte de código (quando e onde tocar) é da mesa Dev.
