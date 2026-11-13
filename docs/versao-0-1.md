# Versão 0.1 — o que jogar, como começar, como testar

Mesa CEO, 10/11/2026. Decisão: `decisoes/2026-11-10-versao-0-1.md`.
**A 0.1 é** o `main` + #39 (vela e poças) + #44 (custo do Analisador) + #47 (Analisador capta a Vila Calada) + #48 (Primeiro
Sinal na neblina e aviso ao admin). Sem ideias novas até o grupo jogar.

Regra 7: **nada vai para o servidor sem backup e sem o Nikolas pedir.** Teste primeiro num servidor de teste com o pack.

## 1. O que tem para jogar

**A história (Ato 1, começo)**
- **Primeiro Sinal** numa vila comum: o sino toca sozinho, um aldeão some e a cama dele fica manchada, os outros aldeões
  ficam parados olhando o sino, e o grupo recebe o texto de abertura.
- **Enigma do sino:** o sino toca FERRO em Morse. Quem escreve FERRO na placa em branco abre o baú, que tem o Livro do
  Carniceiro e a metade esquerda do Mapa Rasgado. Com Exposição alta, o sino toca mais devagar. Quem ouve a sequência
  inteira de perto perde Lucidez.
- **Vila Calada:** três pistas levam até ela. Aldeões virados para ela, gotas saindo da cama manchada e o sino rachado
  tocando à noite. Chegar perto marca a chegada e as pistas param.
- **Livro do Sineiro:** cada leitor vê a própria versão. Quem tem Exposição alta lê trechos escondidos. Na primeira vez
  que alguém abre o livro, o sino toca uma vez (#48).
- **Capela (#39):** a Vela da Capela devolve Lucidez a quem está perto. A Poça de Sangue tira Lucidez de quem pisa nela.

**Sempre ligado**
- **Exposição:** só sobe por contato com o paranormal: ver ou lutar com criatura, usar artefato, fazer ritual.
  Tem cinco degraus, de Tocado até Atravessado.
- **Lucidez:** cai com dano, sustos e o Morse. Volta com descanso, luz junto do grupo, comida quente e dormindo.
  Em zero, o jogador surta.
- **Marca:** aparece a partir de Tocado. A Marca de Sangue deixa veias e gotas no corpo, que todos veem.
- **Alucinações:** só o jogador afetado vê. São passos, sussurros, vulto e porta falsa. O jogador escolhe a força em
  `config/odteste-client.toml`, com `efeitos_fortes` = LIGADO, SUAVE ou DESLIGADO.
- **Criaturas:** Retalhado e Sanguessuga-Mãe aparecem raramente. Só depois do 3º dia, longe do spawn e perto de quem
  está Tocado.
- **Analisador de Sinal:** tem receita (Mekanism, Create ou reserva vanilla) e funciona com energia FE.
  - Quando há sinal perto, decifra a mensagem.
  - Quando a fonte está longe, diz de que lado ela vem.
  - Pelo #47, ele também capta a Vila Calada.
- **Occultism (se estiver no pack):** o ritual Chamado de Sangue chama um Retalhado.

**Só por `/give` (sem receita):** Rubra, Agulha de Cerzir Carne, Dedal Dentado, Sino de Matadouro, Sangue Coalhado,
Vela da Capela, Poça de Sangue.

## 2. Como o admin começa a história (em ordem)
Todos os comandos pedem permissão de admin (nível 2). Nos exemplos, troque `grupo1`, os nomes e as coordenadas.

**Antes de ligar**
1. Faça backup do mundo.
2. **Vila Calada (o arquivo não vem dentro do mod):** copie `arte/vila-calada-100/vila_calada.nbt` para
   `<mundo>/generated/odteste/structures/vila_calada.nbt`. Para isso, a prévia 100x100 precisa do ok do Nikolas.
3. O Primeiro Sinal automático vem desligado (`serverconfig/odteste-gatilhos-server.toml`,
   `primeiro_sinal_automatico = false`). Deixe assim: na 0.1, o admin solta o evento à mão.

**No jogo**
1. Crie o grupo: `/odteste grupo criar grupo1 Fulano Ciclano`. O nome do grupo usa só minúsculas, números e `_`.
   Opcional: defina o elemento escondido de cada um com `/odteste elemento <jogador> <sangue|conhecimento|morte|energia>`.
2. Escolha uma vila comum com sino, longe de construções de jogador. Opcional: `/odteste evento ancorar vila_do_sino <x y z>`
   perto do sino.
3. Solte o evento: `/odteste evento primeiro_sinal grupo1 [x y z do sino]`.
4. Ligue o sino ao Analisador. Isso não acontece sozinho: `/odteste sinal fonte adicionar sino1 sino_vila <x y z do sino>`.
5. Coloque a Vila Calada longe da vila comum, em terreno sem construção:
   - `/odteste estrutura prever odteste:vila_calada <x y z> [0|90|180|270]` mostra o contorno.
   - Em até 5 minutos, `/odteste estrutura colocar odteste:vila_calada <x y z> [giro]`. A estrutura entra aos poucos.
   - O mod grava a âncora sozinho. Confira com `/odteste vila_calada ver`.
   - **Sem a estrutura** (se a prévia não for aprovada): marque um lugar à mão com `/odteste vila_calada ancorar <x y z>`.
     Sem âncora não há pistas, nem sinal no Analisador, nem o aviso "não seguiu o sino".
6. Ponha o livro no atril, porque a estrutura vem com o atril vazio. Use `/odteste livro dar sineiro <seu nome>` e coloque o
   livro no atril da vila.
7. Opcional: a capela da Vila Calada tem velas e poças. Coloque-as com
   `/setblock <x y z> odteste:vela_da_capela` e `/setblock <x y z> odteste:poca_de_sangue`.

**Durante o jogo**
- `/odteste evento ver grupo1` e `/odteste vila_calada ver` mostram onde o grupo está na história.
- O admin recebe sozinho os avisos "resolveu o Sino da Vila" e "não seguiu o sino", este depois de 5 noites sem avançar.
- Se o grupo travar:
  - `/odteste vila_calada tocar <jogadores>` toca o sino rachado na hora;
  - `/odteste vila_calada chegou grupo1` marca a chegada.
- Para desfazer:
  - `/odteste evento desfazer grupo1` desfaz o Primeiro Sinal;
  - `/odteste estrutura lista` e `/odteste estrutura desfazer <id>` tiram a estrutura;
  - `/odteste vila_calada desligar` desliga as pistas.
- Para ver e ajustar valores (só no teste):
  - `/exposicao ver|definir <jogador> <0-100>`, `/lucidez ver|definir <jogador> <0-100>`, `/marca ver <jogador>`;
  - `/odteste elemento <jogador> <elemento>` define o elemento escondido; `/odteste elemento revelar <jogador>` revela.

**Pegadinhas**
- O Primeiro Sinal só acha sino em chunk carregado e sem dono (FTB Chunks). `desfazer` e `retrancar` também precisam do
  chunk carregado: fique perto ao usar.
- O sino rachado só toca de noite. De dia, use `/odteste vila_calada tocar`.
- Em criativo, o Morse não tira Lucidez e a vela não devolve Lucidez: teste em sobrevivência.
- Aldeões com nome, dormindo ou trocando ficam de fora das pistas.
- Renomear um grupo perde o progresso dele: apague e crie de novo só no teste.
- Os avisos de enigma vão para todos que têm permissão de admin.

## 3. Roteiro de teste para o grupo
Faça no servidor de teste com o pack inteiro, com 2 a 4 jogadores e 1 admin. Anote **o que aconteceu, o horário e um print**
de cada item. Marque cada item como ✅, ❌ ou "estranho".

**A. Ligar**
- [ ] O servidor liga e todos entram sem crash.
- [ ] `/exposicao` e `/lucidez`, digitados por um jogador comum, mostram um símbolo e uma frase, sem número.

**B. Primeiro Sinal (vila comum)**
- [ ] O sino toca sozinho, um aldeão some e a cama dele fica manchada.
- [ ] Os aldeões ficam olhando o sino e as trocas viram frases.
- [ ] Todo o grupo recebe o texto de abertura, e quem está fora do grupo não recebe.
- [ ] Nenhum bloco de construção de jogador foi mexido.

**C. Enigma do sino**
- [ ] Dá para entender que o sino toca Morse. Anote quanto tempo o grupo levou para perceber.
- [ ] Escrever FERRO na placa abre o baú, que tem o livro e a metade do mapa. Escrever outra palavra não abre.
- [ ] Com `/exposicao definir <jogador> 45`, o sino toca mais devagar.
- [ ] O Analisador ao lado do sino mostra FERRO e a segunda camada.

**D. Analisador (#44, #47)**
- [ ] Sem energia, ele não liga.
- [ ] Com pouca energia (entre 5.000 e 20.000 FE), ele aponta o lado da fonte, mas não decifra.
- [ ] Com 20.000 FE ou mais, ele decifra.
- [ ] Perto da âncora da Vila Calada (`/odteste vila_calada ver` mostra onde fica), mostra "VENHAM / A CAMA JA ESTA FEITA". Não faz clique nem chiado.

**E. Caminho até a Vila Calada**
- [ ] À noite, o grupo ouve o sino rachado de longe e os aldeões estão virados para a vila.
- [ ] Sai um rastro de gotas da cama manchada.
- [ ] Ao chegar, as pistas param.
- [ ] Quem chega sem ter tido o Primeiro Sinal **não** recebe pistas.

**F. Livro do Sineiro e aviso (#48)**
- [ ] Na primeira vez que alguém abre o livro, o sino toca uma vez, fraco. Na segunda vez, não toca.
- [ ] Um jogador com Exposição 45 ou mais vê um trecho que outro jogador, com Exposição baixa, não vê.
- [ ] Fazer só com o admin: um grupo com o Primeiro Sinal que fica 5 noites sem chegar nem ler o livro faz o admin
      receber "não seguiu o sino". Um grupo **sem** Primeiro Sinal nunca gera esse aviso.

**G. Capela (#39)**
- [ ] Perto da vela, a Lucidez sobe aos poucos. Em cima da poça, ela cai.
- [ ] Nem a vela nem a poça quebram ou somem com explosão.

**H. Sistemas e desempenho**
- [ ] Levar dano baixa a Lucidez, e dormir e comer junto do grupo devolve.
- [ ] Com `/lucidez definir <jogador> 0`, o jogador surta e depois volta.
- [ ] As alucinações aparecem só para quem tem Exposição ou Lucidez baixa. O modo SUAVE realmente suaviza.
- [ ] Os FPS e o TPS ficam normais perto da Vila Calada e durante a colocação da estrutura. Anote se houve travada.

**I. Desfazer**
- [ ] `/odteste evento desfazer grupo1` limpa a vila comum.
- [ ] `/odteste estrutura desfazer <id>` devolve o terreno de antes.

## 4. Coisas provisórias (o testador vai notar; não é bug)
- O texto do Livro do Carniceiro e do Livro do Sineiro é provisório. O Mapa Rasgado é um papel com nome. O estandarte tem
  uma cruz provisória.
- Os modelos do Analisador e do Retalhado são provisórios, e o desenho da Marca é provisório.
- A aba "Od Teste" do criativo não aparece. É de propósito: os itens só vêm por `/give`.
- As mensagens de `/odteste livro` aparecem só em português.
- **Não tem na 0.1** (fica para a 0.2): os cinco lugares, o Evento do Diabo, o Kit do Mestre, o sino ambiente, o mapa que
  se desenha, o Faminto e os sons próprios.
