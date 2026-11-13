# Comparação das criaturas de Sangue: provisório x 5.1 x 5.2

Folhas lado a lado, **mesmos ângulos** das prévias (frente, lado direito, costas, 3/4) e **mesma escala**
(1 traço da régua = 1 bloco), mais uma linha de silhueta (teste da régua de qualidade: reconhecer só pela sombra).

| Arquivo | O que é |
|---|---|
| `retalhado.png`, `sanguessuga_mae.png` | provisório (o que está no `main` e no jogo hoje) x 5.1 (branch antes da revisão) x 5.2 (revisada) |
| `magarefe.png` | 5.1 x 5.2 (o Magarefe não tem provisório: ainda não existe no jogo) |
| `folha_sangue.png` | as três folhas empilhadas |
| `v5_1/<criatura>/` | cópia dos arquivos da 5.1 (geo, textura, glowmask, animações), para a folha continuar reproduzível |

## Como regenerar
Na raiz do repositório: `bash ferramentas/comparar_criaturas_sangue.sh` (só python3 + numpy). O script
`arte/previas/criaturas/fonte/comparacao.py` lê o provisório de `src/main/resources/assets/odteste/geo/entity/`
(a textura provisória é refeita em numpy, igual à que `cliente/ModeloCriatura.java` gera no jogo), a 5.1 de `v5_1/`
e a 5.2 de `arte/previas/criaturas/<criatura>/`, e renderiza com `ferramentas/render_geckolib.py`.
Para refazer a 5.2 antes: `bash ferramentas/criaturas_sangue.sh`.

## O que estava abaixo do nível na 5.1 e o que a 5.2 mudou

### Retalhado
| Eixo | 5.1 | 5.2 |
|---|---|---|
| Silhueta | simétrica, "golem quadrado": dois ombros iguais, cabeça no meio de um retângulo, ganchos finos que somem na sombra | ombro direito (do cutelo) maior e mais alto, esquerdo caído com pele solta pendurada; corcova maior com três vértebras saltadas; cabeça afundada e torta; perna esquerda virada para fora (manca); ganchos em "J" grossos com elos; cutelo maior e lascado |
| Proporção | ok (braços até o joelho), mas pernas e braços iguais dos dois lados | braço esquerdo mais comprido e fino (o dos ganchos), direito grosso (o do cutelo) |
| Textura | grade de cruzes "+" espalhada em tudo (parecia curativo); carne de um tom só, pouco contraste; chuvisco de pintas | pele em **retalhos de três pessoas** (pálida, roxa de hematoma, curtida), costura **só na fronteira** dos retalhos, sangue seco grudado na costura; sombra com oclusão embaixo de cada peça e manchas largas em vez de pintas |
| Horror | boca com barra vermelha e dentes alinhados lembrava sorriso de desenho | mandíbula caída e torta, boca sempre entreaberta e escura, três dentes que sobraram, olhos costurados em X, rasgo no lugar do nariz |
| Variação | nenhuma | textura **variante B** (`retalhado_b.png`: outros retalhos e tons) para o Dev sortear; a "versão pequena" da Clareira (lore) é escala no código |
| Andar | pernas quase paradas, braços simétricos, tronco flutuando | tronco **cai** no contato de cada pé e sobe devagar; canela dobra só no balanço; braços e ganchos chegam atrasados; cabeça e mandíbula sacodem no impacto |
| Ataque | braço sobe reto como alavanca, resto do corpo parado | 0,45 s de antecipação com o corpo inteiro (torce, recua, ergue), corte em 0,13 s com o tronco junto, passa do ponto e volta devagar |
| Morte | joelhos cedem juntos, cai reto | tranco, cambaleia, joelho **esquerdo** cede antes do direito, cai de cara com um quique, ganchos ainda balançam, coração acelera e para |
| Ocioso | respiração simples | batida dupla do coração (lub-dub), cabeça fareja em sacudidas, boca abre e fecha, ganchos pendulam |

### Sanguessuga-Mãe
| Eixo | 5.1 | 5.2 |
|---|---|---|
| Silhueta | barril em quatro palitos retos; a **boca (a característica principal) só aparecia por baixo** | corpo em três camadas de larguras diferentes, **empinado** (a frente sobe): lábio de carne saliente e dentes aparecem de frente e de 3/4; pernas com três segmentos e joelho de espinho acima do corpo; espinhos no rebordo |
| Textura | trama de "tijolos" regular; larvas vermelho-vivo chapado ("lego vermelho") | fiadas que serpenteiam com tiras sombreadas; larvas com corpo anelado, cabeça escura e uma linha molhada, meio enterradas na carne do topo; carne úmida onde grudam |
| Andar | pernas quase não saíam do chão (parecia flutuar) | a perna no ar sobe de verdade (coxa +18, canela −34) e desce rápido; corpo afunda quando o par assenta; larvas balançam atrasadas |
| Mordida | sobe e desce | 0,4 s de antecipação (ergue, pernas esticam, dentes abrem), desaba em 0,12 s, anéis de dentes fecham um depois do outro, segura, volta |
| Morte | tombava inteira de uma vez | pernas da frente cedem primeiro, cesta cai e quica, tomba de lado, **duas pernas ainda se mexem**, larvas fogem |
| Ocioso | respiração | respiração + uma perna que muda de lugar no meio do ciclo (nunca congelada) |
| Variação | nenhuma | textura variante B (`sanguessuga_mae_b.png`) |

### Magarefe
| Eixo | 5.1 | 5.2 |
|---|---|---|
| Silhueta | simétrico, braços retos, saco liso, cutelo pequeno para 3,8 blocos | ombro do cutelo caído, tronco pendendo para esse lado, cabeça torta e caída, **duas pontas do saco** amarradas em cima, cutelo maior, dois ganchos de carne no cinto (atrás), abas do avental com osso próprio |
| Textura | camisa chapada; avental uniforme | dobras fundas na camisa e na calça, couro gasto com partes mais claras, escorrido mais fundo, barra do avental rasgada (transparente) |
| Ocioso | pêndulo suave | a cabeça pende e, uma vez por ciclo, dá um **estalo seco**; o sino responde |
| Andar | passo simétrico | arrastado: pé esquerdo quase não sai do chão, braço do cutelo atrasado, tronco afunda no apoio, abas e ganchos balançam depois |
| Ataque | ergue e desce, parado no lugar | **passo à frente**, 0,7 s de antecipação, desce de uma vez, cutelo fica embaixo um instante e volta devagar |
| Variação | nenhuma | textura variante B (`magarefe_b.png`) |

### O que continua pendente (para o Nikolas / Dev)
- Nada foi integrado em `src/`: o jogo segue com o provisório até a aprovação. Caminhos do código não mudaram
  (`geo/entity/<id>.geo.json`, `animations/entity/<id>.animation.json`, `textures/entity/<id>.png`).
- Nomes de animação que o código já usa continuam iguais (`parado`, `andar`, `investida`, `lancar`); as novas
  (`ataque`, `mordida`, `dano`, `morrer`, `afiar`, `chocar`, `escutar`) esperam o Dev ligar.
- Texturas: Retalhado 160x160, Sanguessuga 160x128, Magarefe 128x128 (todas a 16 px por bloco; a folha cresceu
  porque o modelo tem mais peças). Resolução continua pendente em `docs/decisoes.md`.
- Hitbox sugerida (medida nas prévias): Retalhado 1,9 x 3,1 (hoje 1,5 x 3,0); Sanguessuga-Mãe 2,8 x 2,1 (hoje
  2,6 x 1,9; as pernas passam da hitbox de propósito); Magarefe 0,9 x 3,8.
