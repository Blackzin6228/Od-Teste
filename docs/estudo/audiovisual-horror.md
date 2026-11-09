# Audiovisual do horror: luz, enquadramento, som, silêncio (guia para o time)

> Estudo da mesa Lore, 09/11/2026. Tom do mod: **sério, medo e presença, sem sustos baratos** (`lore/roteiro.md`).
> Tudo que depende de código/API do Minecraft está marcado **(validar com o Dev)**: citei de memória do que o NeoForge 1.21.1 oferece.

## 1. Medo, pavor e susto
Três coisas diferentes. O mod quer as duas primeiras.

| | O que é | Como se sente | Duração | Usar? |
|---|---|---|---|---|
| **Susto** (jump scare) | Informação nova e brusca (barulho alto, rosto na tela) | Choque, coração acelera, passa | Segundos | **Quase nunca.** Só como tempero, e raro |
| **Medo / tensão** | Há uma ameaça e eu sei (ou imagino) que ela vem | Antecipação | Minutos | Sim: perseguição, Magarefe |
| **Pavor / mal-estar** (dread) | Algo está *errado* e eu não consigo explicar | Desconforto que não sai | Horas, dias de jogo | **Sim: é o centro do mod** |

Ideia-chave de um artigo de design de jogos de terror: o medo é **controlar o fluxo de informação**. O jogador deve **saber que está faltando informação**
("o que está atrás da porta?") e você dá só o bastante para a tensão começar, mas nunca o bastante para ela terminar.
O susto, ao contrário, só entrega informação de uma vez e libera a tensão.
Corolário clássico: a bomba que o público *sabe* que está debaixo da mesa dá mais medo do que a explosão.

**Regra para nós:** o Ato 1 é quase todo pavor. Sustos só como **vírgula** depois de muita calmaria, e nunca dois seguidos.

## 2. Revelar pouco
- **O monstro mostrado tarde, e aos poucos:** sombra, depois silhueta, depois detalhe, por fim o rosto (ou nunca). Jogos de horror respeitados deixam a criatura de luz baixa por bastante tempo. Bastante luz cedo demais mata o medo.
- **Parcialmente visto é pior:** de canto de olho, atrás de vidro, de longe, de costas.
- **Ausência como sinal:** o aldeão sumido, a cadeira vazia, a vela apagada. Mostrar *o espaço* onde algo deveria estar.
- **Áreas escuras mostradas de propósito** avisam "há algo que você não sabe". Para nós: corredor sem tocha, porta aberta, alçapão.
- **O Diabo do roteiro já segue isso:** "só se vê o vermelho dos olhos de quem está perto". Manter. Quando a arte vier, **resistir** a mostrar o rosto.
- **Inconsistência controlada:** mostrar a mesma coisa duas vezes de forma levemente diferente (a cama manchada, depois outra com a mancha na outra ponta) perturba mais que uma ameaça explícita.

## 3. Luz
**O que se aprende do cinema e dos jogos:**
- Escuro **não é** só ausência de luz: é o lugar onde o público projeta o próprio medo.
- A luz do jogador é **recurso**: lanterna, tocha, vela. Quando está acesa, ela é também o que denuncia. Segurança e exposição ao mesmo tempo.
- **Contraste:** pouca luz recortando o escuro assusta mais que escuridão total (ver o estilo da obra: brilho do elemento na penumbra).
- **Cor por elemento:** vermelho para Sangue, dourado para Conhecimento etc. Uma luz de cor errada num lugar comum já é suficiente para dizer "algo mudou".
- **Luz que falha ou pisca** marca presença. Mas pouco: virou clichê se for em toda cena.

**No Minecraft:**
- **Nível de luz dos blocos** (0 a 15). Velas, lanternas e tochas têm níveis diferentes. Nosso bloco "Vela de Sebo" pode emitir luz baixa (ex.: 5-7) em vez de 14, para o jogador sempre sentir que "não é suficiente". **(validar com Modelador/Dev)**
- **Efeito Escuridão (Darkness)** do vanilla, o que pulsa a visão quando um Warden está por perto. Pode servir para o "mundo mente" e para Lucidez baixa.
- **Efeito Cegueira** curtíssima: pesado, usar com cuidado.
- **Brilho de bloco emissivo** (textura que acende no escuro) para cruz de ferro, símbolos, texto vermelho. A textura emissiva exige suporte do pack/shader; sem ele, usar bloco com luz mínima. **(validar com o Integrador: o pack tem shaders?)**
- **Névoa colorida e tom da tela:** ver seção 7.
- **Luz que "aprende" com o jogador:** a luz de uma vela perto de quem tem Lucidez baixa pode diminuir só no cliente dele **(validar com o Dev)**.

## 4. Enquadramento (câmera)
Em jogo em primeira pessoa, o "enquadramento" é o que o jogador é levado a olhar. Técnicas:
- **Guiar o olhar:** luz, som e movimento fazem o jogador virar a cabeça para onde você quer. Colocar o evento *fora* do centro e deixar a atenção cair nele.
- **Campo de visão apertado:** Minecraft dá visão em primeira pessoa, limitada. Em corredores de 2 blocos de largura, o jogador não tem para onde fugir.
- **Canto do olho:** coisas que só existem fora do centro da tela e somem ao olhar direto (o Aldeão de Pé). Efeito de cliente.
- **Altura e ângulo:** olhar para cima (algo no teto, sombra de cima) e para baixo (algo no poço) causam desconforto. Pé-direito baixo comprime.
- **Simetria quebrada:** sala simétrica com um único objeto fora do lugar. Rápido de ler, difícil de esquecer.
- **Controle de câmera (uso raro):** o mod pode mexer em *FOV* e *balanço da câmera* **(validar: eventos de câmera/FOV no cliente)**. Regra: nunca roubar o controle do jogador. Um micro-tremor ou uma leve inclinação só.
- **Escala:** a sombra enorme no horizonte do evento do Diabo é enquadramento por escala: nada se aproxima, mas o tamanho já diz tudo.

## 5. Som e silêncio
É onde o horror mais ganha. Estudos e entrevistas de equipes de jogos de terror concordam: **sem som o jogo perde boa parte do medo.**

**Princípios:**
1. **Camadas:** um som de fundo contínuo e baixo (vento, hum), sons médios (madeira, passos) e sons raros e fortes (sino).
2. **Silêncio como instrumento:** o silêncio **antes** é o que carrega o susto. Mas silêncio demais, por muito tempo, vira tédio. Reservar para picos.
3. **Som sem fonte:** um barulho que o jogador não consegue localizar ("de onde veio?") gera mais medo que o barulho forte.
4. **Direção errada:** colocar o som atrás ou longe do que seria lógico.
5. **Som que muda de repente:** o sino cala. O silêncio **é** o evento (o roteiro já faz isso no enigma 4).
6. **Som reconhecido, usado errado:** sons conhecidos (sino, aldeão, porta) em lugar, hora ou velocidade erradas. É o "familiar errado" em áudio.
7. **Silêncio de som ambiente:** retirar o ruído de fundo (pássaros, grilos) quando o paranormal se aproxima. O jogador sente ausência antes de entender.
8. **Música só em momentos escolhidos.** Preferência: **nenhuma música** na exploração; um piano esparso só para o Medo (identidade do gênero). Música em todo canto "avisa" que vem perigo e mata a surpresa.
9. **Sons vinculados ao medidor:** em jogos conhecidos de horror, o áudio reage à sanidade. Para nós: com Lucidez baixa, sons ficam abafados, cheios de eco, ou aparece um sussurro curto.

**O que dá com o som do Minecraft puro (sem arquivo novo):**
- `/playsound` aceita *volume* e *pitch*. O pitch vai de 0,5 a 2,0 **(validar: a engine limita a 0,5 a 2)**. Pitch **0,5** em sino, aldeão, porta ou nota dá um tom grave e arrastado que dura o dobro.
- **Sons vanilla úteis para pitch baixo:** sino de vila, aldeão (ambiente e "não"), porta de madeira, bloco de notas (grave), baú, morcego ao longe, vento de caverna, batida de coração do Warden. **(confirmar o nome exato do evento de som com o Dev antes de usar)**
- **Som posicional:** tocar o som numa posição *diferente* da do jogador (atrás, no teto, na parede) é grátis e funciona.
- **Volume mínimo:** sons a volume baixo (0,1 a 0,3) com pitch baixo parecem "dentro da cabeça". Bom para Lucidez baixa.
- **Atraso de eco:** tocar o mesmo som duas vezes com 0,4 s de diferença e volume menor na segunda parece um eco que não deveria existir.
- **Limite:** abaixo de 0,5 de pitch ou para vozes/sussurros, é preciso **arquivo de som novo** (`sounds.json` + .ogg). Isso entra com a mesa Dev e arte sonora própria (regra 10 do `CLAUDE.md`: licença registrada).

## 6. Partículas
- **Menos é mais.** Uma gota vermelha que cai de um ponto fixo diz "aqui". Cem partículas dizem "efeito".
- **Poucas e lentas:** velocidade baixa, tempo de vida longo, tamanho pequeno.
- **Sem contorno chamativo:** evitar brilho de "magia".
- **Direção:** partículas **que apontam** (pegadas de giz, gotas em fila) servem de pista e atmosfera.
- **Regra de desempenho:** o pack tem ~530 mods. Partículas só perto do jogador, só no cliente, com limite por tick **(validar com o Dev)**.
- **Cor do elemento:** vermelho-escuro para Sangue. Evitar o vermelho vivo, que lembra "dano". Sangue velho é marrom-avermelhado.

## 7. Neblina e atmosfera
- A névoa é **o sinal do paranormal perto** no gênero (ver `docs/estilo-do-genero.md`). Perfeita para o Minecraft.
- **Névoa vanilla:** aparece em clima, no Nether, em efeito de Cegueira/Escuridão. **Névoa por mod** pode ser mudada no cliente por eventos de renderização do NeoForge (distância e cor da névoa) **(validar com o Dev)**.
- **Usos:**
  - Névoa avermelhada leve perto da vila marcada (o Cheiro).
  - Névoa fecha o horizonte e encurta a visão ao se aproximar do galpão.
  - Névoa **some** de repente quando o paranormal "olha" para você (silêncio visual).
- **Cor e hora do dia:** o céu avermelhando (evento do Diabo) faz o servidor inteiro sentir. Cuidado com compatibilidade com shaders e com mods de clima do pack **(validar com o Integrador)**.
- **Tela/overlay de Lucidez:** vinheta escura ou leve dessaturação. Ainda sem número.

## 8. O que mapas e mods de horror de Minecraft ensinam
(O que consegui ver em resenhas e listas; não testei os mods.)
- **Clima de espreita:** o medo vem do sentimento de que *algo pode aparecer a qualquer momento*, não da aparição em si. Mapas e mods de neblina usam sons distantes distorcidos e amplificados para o jogador duvidar do ouvido.
- **Incerteza de quando:** entidades de lenda urbana do Minecraft (como a "entidade que às vezes mexe no mundo") funcionam porque o jogador nunca sabe *quando*; o aviso fica no fundo da cabeça. Para nós: o admin controla o ritmo, mas pequenos sinais aleatórios (sino que bate sozinho, porta que abre) mantêm o fundo.
- **Som reposicionado e camadas de áudio** mudam completamente o clima sem mudar a tela.
- **Silêncio antes do susto** é frequente em mapas que funcionam bem.
- **Dependência de configurações:** vários mapas pedem brilho baixo e volume alto. **Cuidado:** não obrigar. O mod deve funcionar no brilho do jogador e avisar na sessão zero ("jogue com fone, em lugar escuro, se quiser").
- **Erro comum a evitar:** monstro mostrado cedo e em plena luz; tela que dá susto por barulho alto; muita coisa ao mesmo tempo.

## 9. Resumo: tabela de técnica para o Minecraft
| Técnica | Como fazer em Minecraft | Custo |
|---|---|---|
| Som sem fonte | `playsound` em posição fora do campo de visão | Baixo |
| Som grave e arrastado | Pitch 0,5 em sino/aldeão/porta | Baixo |
| Silêncio ambiental | Cortar sons de ambiente perto do paranormal (cliente) | Médio |
| Luz fraca | Velas de luz baixa, escuridão no galpão | Baixo |
| Presença no canto do olho | Entidade visível só no cliente do jogador de maior Exposição | Médio |
| Névoa que avisa | Fog de cliente ligado ao medidor/lugar | Médio |
| Pegada/gota como pista | Partículas lentas apontando | Baixo |
| Lucidez baixa | Vinheta, sons abafados, texto que muda | Médio |
| Câmera | Micro-tremor ou FOV leve (raríssimo) | Médio |
| Voz do Diabo | Voice chat + voice changer do admin | Já existe no pack |

## Fontes
- [Game Developer, "Scary Games: Why Fear Should Be Easy in Games But It Isn't"](https://www.gamedeveloper.com/design/scary-games-why-fear-should-be-easy-in-games-but-it-isn-t-) (fluxo de informação, escuro como sinal, som sem ver a ameaça, limite do susto)
- [Alphr/Medium, "From Silent Hill to Alien: Isolation: what makes a horror game scary?"](https://medium.com/stories-from-alphr-com/from-silent-hill-to-alien-isolation-what-makes-a-horror-game-scary-4d4cf4d731ab) (luz, ambiente, falta de sustos baratos)
- [Epic Games, "Why horror games are so scary"](https://store.epicgames.com/news/why-horror-games-are-so-scary) (escala do desconforto ao terror)
- [Wayline, "Silence is scary: sound design in horror games"](https://www.wayline.io/blog/silence-is-scary-sound-design-horror-games) (opinião de blog; silêncio nos picos)
- [minecraft.how, melhores mapas de horror 2026](https://minecraft.how/blog/post/best-horror-maps-minecraft-2026) e [mais assustadores](https://minecraft.how/blog/post/scariest-minecraft-horror-maps-2026) (listas de mapas, uso de som e brilho baixo)
- [Minecraft: The Fog (resenha)](https://discourse.weareopen.coop/news/minecraft-the-fog-a-spine-1761728243772) (sons distorcidos e neblina)
- Referência não consultada ao vivo: Minecraft Wiki, comando `/playsound` e lista de eventos de som; documentação NeoForge (eventos de renderização de névoa/câmera). **O Dev deve confirmar antes de implementar.**
- Qualidade das fontes: várias são blogs de opinião; as listas de mapas são conteúdo de SEO. Usar como inspiração, não como regra técnica.
