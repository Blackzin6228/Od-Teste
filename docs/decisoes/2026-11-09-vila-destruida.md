# Decisão de 09/11/2026: o objetivo do Ato 1 é uma vila destruída

Pedido do Nikolas, depois do estudo de aventura. Vale para o Ato 1 do roteiro.

## O que o Nikolas decidiu
1. **Problema:** depois de atravessar a floresta o grupo está num mundo diferente e não sabe o que fazer. O caminho natural é o grind.
   A história precisa **puxar o grupo**, **sem tutorial na tela**.
2. **Objetivo do Ato 1:** chegar a uma **vila específica, nossa, totalmente destruída**. É uma **estrutura grande e bem produzida, colocada pelo admin**.
3. **Na vila está a primeira pista de COMO VOLTAR PARA A REALIDADE.**
4. **Na vila há um LIVRO** que conta a história do mundo onde eles estão: quem viveu ali, o que aconteceu, o Sangue, o que o Medo fez.
   Páginas curtas, tom sério, pistas escondidas para os enigmas, uma página em branco que só aparece com Exposição.
5. **Como o grupo descobre a vila:** regra das três pistas. A **abertura da vila comum** (já programada) vira o **primeiro gancho** até a vila destruída.
6. Tom sério, estética Minecraft, nenhum número na tela. Nada da obra Ordem Paranormal (regra 3).

## O que a mesa Lore propôs (aguarda o ok do Nikolas)
- **Nome provisório:** Vila Calada. (Busca em 09/11/2026 não achou obra com esse nome.)
- **O livro:** *Livro do Sineiro*, 14 páginas, texto completo em `lore/livro-do-sineiro.md`.
  A página 14 é a em branco (só aparece com Exposição). As páginas 11 e 12 entregam a primeira pista de como voltar: **o sino precisa tocar ao contrário**,
  e para isso **quatro portas pequenas precisam responder** (sangue, silêncio, memória, luz) e uma quinta "não é porta, é parede" (o Medo).
- **A ficha:** `lore/fichas-estruturas.md` (tamanho, ruas, casas, capela, poço, lugar do livro, pistas, perigos, clima).
- **As três pistas:** (1) o sino rachado ouvido de longe à noite; (2) os aldeões da vila comum parados olhando na direção da vila destruída e a cama
  manchada com a cabeceira virada para lá; (3) um mapa de cartógrafo que aparece no inventário de `{marcado}` e só se desenha conforme se anda.
  Atalho tech: o Analisador de Sinal capta o sino. Nenhuma pista é tutorial.
- **Ajustes no roteiro (`lore/roteiro.md`, Ato 1):** nova Cena 1B (a Vila Calada); a Cena 1 passa a apontar para ela;
  o Enigma 1 passa a ter uma ponte com a página 13 do livro; o fio da descoberta ganha um passo zero.

## Efeitos
- Os cinco enigmas continuam. A vila destruída vem **antes** deles e o livro **ensina a ler o sino** (dois tons, cinco em cinco) sem entregar a resposta.
- Quem pula o livro ainda resolve os enigmas pelo caminho antigo. Quem lê ganha só mais contexto: nada fica travado.
- Não muda nenhum ID ou item existente. Itens novos (Livro do Sineiro, mapa, Faminto, estandarte) entram em `docs/ids.md` quando o Dev os criar.
- Colocar a estrutura por comando de admin segue a regra 11 (sem apagar construção de jogador): plano do Dev em `docs/sistemas.md`.

## Pendências
- O Nikolas aprovar nome, texto do livro e ficha.
- Prévia do Modelador/Construtor antes de construir.
- O Dev escolher a técnica do mapa, do som distante e do comando de colocação.
