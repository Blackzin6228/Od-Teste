# Enigmas que saem do jogo (Ato 1) (mesa lore)

Status: **rascunho para o Nikolas aprovar.** Base: `lore/roteiro.md` e `docs/decisoes/2026-11-08-producao-estruturas-e-enigmas-fora.md`.
**09/11/2026:** refeito em "Minecraft puro" (`docs/decisoes/2026-11-08-roteiro-minecraft.md`) e com a decisão N6 do Nikolas: os enigmas de fora vão num **site novo (Vercel)**,
com áudios e vídeos **feitos pelas mesas**. Saíram fita K7, fita VHS, câmera de segurança, TV, crachá e "intranet de empresa".
Tudo o que se vê e se ouve fora do jogo tem cara de Minecraft: blocos de nota, livros, mapas, desenho a carvão, gravação em primeira pessoa dentro do jogo.

## Aviso importante: o repositório é público
Este arquivo descreve o **método** de cada enigma. As **respostas reais** (senhas, palavras de retorno, textos cifrados finais) são geradas por temporada
e guardadas **fora do repositório** (pasta privada do Nikolas, nunca no GitHub). As palavras abaixo, marcadas **(exemplo)**, são de teste.
Quem joga e lê o repositório não pode achar a solução aqui.
Exceção: **FERRO** (abertura) e **PORTA** (Moinho) são palavras de história, decididas pelo Nikolas (N3), e podem ficar no texto. Se quiser segredo, troque por temporada mantendo **cinco letras** (o César do E2 depende disso).

## Regras comuns
- **Entrada sempre achada no jogo.** O endereço do site está no baú do Moinho Parado; cada página só abre com uma **palavra** achada dentro de um lugar.
- **O que volta ao jogo é sempre palavra**, escrita num bloco do mod (cadeado de letras). Nenhum número na tela, nem nos vídeos (HUD escondido; sem coordenadas, contagem de itens ou nível).
- **Plano B dentro do jogo:** cada enigma tem uma **Dica em livro** que o admin pode liberar com um comando (o Dev cria). Se o site cair ou o grupo travar, a história não para. A dica custa um pouco de Lucidez do grupo, para valer a pena tentar sozinho.
- **Sem jogo dentro do jogo para quem não tem tempo:** tudo é resolvível com papel, caneta e programas grátis (Audacity, qualquer visualizador de imagem, o próprio navegador).
- **Dificuldade em três níveis** para cada um: fácil (sem ferramentas), médio (uma ferramenta comum), difícil (ferramenta que o grupo precisa descobrir). O grupo só precisa passar o médio.
- **Nenhum material de terceiros.** Áudios, imagens e vídeos são produzidos pelas mesas. Cada arquivo é registrado em `arte/LICENCAS.md` com a origem (ver "Produção").
- **Gravações nunca no servidor.** Vídeos e áudios são feitos num mundo de teste com as schematics colocadas. Nenhum mod de vídeo vai para o servidor (WATERMeDIA e o plugin do YouTube continuam fora).

## O site: Atlas do Cartógrafo
Nome provisório (busca de 09/11/2026: nenhuma obra conhecida usa "Atlas do Cartógrafo"; conferir de novo antes de fixar).
- **O que é:** o atlas que o cartógrafo da vila perdida deixou "do outro lado". Visual de **livro e mapa do Minecraft**: papel envelhecido, tinta, letra de pixel, bordas de mapa.
  Nada de efeito moderno, nada de cara de empresa. Tom sério. Nenhum logotipo de outra obra.
- **Como funciona (técnico, proposta para o Dev):** página estática na **Vercel** (N6). O conteúdo de cada página fica **cifrado no próprio arquivo**; a palavra digitada no navegador é a chave.
  Assim ninguém lê as páginas olhando o código, nem no GitHub. As palavras de verdade ficam fora do repositório.
- **Entrada achada no jogo:** o endereço escrito a carvão na tampa do baú do moleiro (Moinho Parado).
- **Páginas (cada uma pede a sua palavra):**
  | Página | Abre com | O que tem |
  |---|---|---|
  | **Capa** | nada (aberta) | Uma rosa dos ventos, um sino rachado e um campo de palavra. Sem explicação |
  | **A Gravação do Moinho** | FERRO (a placa da abertura) | E1, a gravação limpa dos blocos de nota |
  | **O Olho do Palheiro** | palavra riscada na viga do palheiro (Casa do Açougueiro) | E7, vídeo opcional |
  | **O Último Olhar** | palavra das cinzas do círculo de giz (Casa do Cartógrafo) | E4, o vídeo em primeira pessoa |
  | **O Atlas fechado** | senha do vídeo E4 | E5: três achados, a página Resposta e a fonte dos Riscos de Corte |
  | **O Disco Riscado** | palavra de retorno (depois de usada na Torre) | E6, o áudio das correntes |
- **Erro:** o site responde só *"A página está em branco."* Não diz o que errou.
- **Se o site cair:** o admin libera a Dica em livro (as páginas em texto, no jogo).
- **Pendente:** domínio. Nenhum endereço de servidor do jogo aparece no site. O site não coleta nada de ninguém (sem cadastro, sem rastreio).

---

## Enigmas, na ordem em que o grupo os encontra

### E1. A Gravação do Moinho (blocos de nota em Morse, com camada escondida)
- **O que é:** a sequência que os blocos de nota do Moinho Parado tocam. Dois tons: **grave e longo** é traço, **agudo e curto** é ponto. No fundo, o ranger da roda.
- **Como se acha a entrada no jogo:** os blocos de nota no último andar do moinho. Dentro do jogo a roda trava no meio quase sempre (inteira só numa vigília de noite toda, ou girando o moinho com Create).
  A gravação limpa está no Atlas, página aberta com **FERRO**.
- **Solução:**
  - Camada 1 (médio): Morse. Resultado: **PORTA** (decisão N3).
  - Camada 2 (difícil): no **espectrograma** da gravação do site (Audacity, "Espectrograma"), um zumbido agudo por baixo das notas desenha **letras**: *NAO ENTREM PELA PORTA DA FRENTE*. O aviso protege o grupo na Casa do Açougueiro.
  - Fácil (sem ferramenta): quem junta FERRO e PORTA pensa em "porta de ferro"; a Casa do Açougueiro tem uma.
- **Por que PORTA:** o Livro do Sineiro diz que o sino é uma porta; o Ato 5 é abrir uma porta com quatro fechaduras; e, junto com FERRO, aponta a entrada segura. Cinco letras, como FERRO: o César do E2 segue com deslocamento cinco.
- **O que volta ao jogo:** a palavra no **cadeado do baú do moleiro**. Abre o Diário p. 1, a metade esquerda do Mapa e mostra o endereço do Atlas.
- **Plano B:** a Dica em livro dá a tradução do Morse. O espectrograma não tem plano B (é bônus).

### E2. O Mapa Rasgado (mapa de cartógrafo com cifra)
- **O que é:** a metade esquerda de um mapa de cartógrafo, rasgado, com uma frase em letras trocadas escrita na margem (texto do item). O jogador copia a frase.
- **Como se acha:** no baú do moleiro (E1).
- **Solução:** cifra de César, deslocamento igual ao tamanho da palavra E1 (cinco), que o Diário p. 1 sugere: "cinco passos de cada vez". A frase decifrada diz o **rumo e a distância** da Casa do Açougueiro, **em palavras** ("para onde o sol se põe, quatro centenas de passos").
  A distância é a **real**, calculada com a posição que o admin registrou. Por isso a frase é gerada, não fixa.
- **O que volta ao jogo:** o rumo da Casa do Açougueiro, que o grupo acha andando. Juntando com a metade direita (Casa do Açougueiro), o mapa inteiro mostra o caminho até a Casa do Cartógrafo.
- **Plano B:** a Dica em livro dá a regra da cifra.

### E3. O Livro de Trocas (livro cifrado com símbolos)
- **O que é:** o livro em que o Açougueiro anotava o que cada aldeão deu em troca de carne, escrito em **Riscos de Corte** (alfabeto nosso, substituição simples), com uma frase longa na última página.
  As primeiras trocas são esmeralda e trigo. As últimas, não.
- **Como se acha:** no porão de salga da Casa do Açougueiro (depois das quatro correntes).
- **Solução:** tabela parcial no Diário p. 3 (as dez primeiras letras); o resto por **análise de frequência**. Texto claro: *O DIABO NAO ROUBA. ELE ACEITA. VENHAM A MESA QUANDO O SINO CALAR.*
  Os dois nomes que mais se repetem nas trocas são o Compadre e a Moça do Terço. No verso, em Morse escrito a tinta, o **rumo da Casa do Cartógrafo** e a frase "quem desenha o caminho viu tudo".
- **Fonte:** criamos uma fonte (`.ttf`) dos Riscos de Corte, para download no Atlas fechado (depois de aberto). Quem digita o livro no computador acelera.
- **O que volta ao jogo:** o rumo da Casa do Cartógrafo.
- **Plano B:** a Dica em livro entrega a tabela completa.

### E4. O Último Olhar do Cartógrafo (vídeo em primeira pessoa dentro do jogo)
- **O que é:** um vídeo de **cerca de um minuto**, gravado **dentro do Minecraft, em primeira pessoa**, como se fossem os olhos do cartógrafo. HUD escondido. Legendas de som no estilo das do jogo, colocadas na edição.
- **Como se acha a entrada no jogo:** o **círculo de giz** do mirante da Casa do Cartógrafo. Completo e com duas pessoas ou mais dentro, de noite, as cinzas do centro formam uma **palavra**. Ela abre a página do vídeo no Atlas.
- **O que mostra (roteiro):**
  1. Escuro. Uma vela na mão. A escada em caracol, subindo devagar. Legenda: *"Passos"*.
  2. No mirante, ele olha pela **luneta**: neblina, e dentro dela, longe, uma torre com um sino que balança. Legenda: *"Correntes arrastam"*.
  3. Desce ao salão e **escreve numa placa** ao lado da mesa de cartografia. A palavra fica legível por um instante: é a **senha do Atlas (exemplo: `CARNEIRO`)**. Depois ele quebra a placa.
  4. Uma legenda que não corresponde a nenhum som aparece e some: *"Alguém sussurra: o sino não toca; ele chama"*. Reforça o aviso do Ato 5.
  5. Ele sobe de novo. Por **um quadro**, alguém pendurado no teto da escada. Pausa quadro a quadro mostra. Não adianta para a senha, adianta para o clima.
  6. Ele para de frente para o mapa-mural. As marcas de estandarte se apagam uma a uma. Corte seco.
- **Solução:** pausar no passo 3 e ler a placa (médio). A legenda falsa (passo 4) é o difícil: quem só ouve não a vê; quem só vê não sabe que não tem som.
- **O que volta ao jogo:** a senha abre o Atlas fechado e o **baú do salão** (Terço da Moça).
- **Plano B:** a Dica em livro fala a palavra da placa.

### E5. O Atlas fechado (site) — três achados
A senha de E4 abre a parte fechada do Atlas. O grupo se divide: cada página é um achado. Os três respondem a uma pergunta só: **"de onde vem o barulho?"**

#### E5a. A Carta do Clérigo (página manchada de tinta)
- **O que é:** uma página de livro do jogo, escrita pelo clérigo da vila perdida, com trechos cobertos por **manchas de tinta preta** (tinta de lula).
- **Solução (fácil a médio):** o texto por baixo das manchas **ainda está na página** (é texto de verdade, coberto por imagem). Selecionar e copiar, ou ler o código da página, revela.
  Diz *"A torre é o sino. Quem cala o sino, chama o resto."* e descreve o morro onde fica a Torre das Correntes (que a luneta confirma).
- **O que volta:** a palavra **"torre"** (usada na página Resposta).
- **Plano B:** livro de dica.

#### E5b. O desenho a carvão do Açougueiro (imagem com texto escondido)
- **O que é:** um desenho a carvão, em papel, do Açougueiro de pé na porta da casa de abate, as correntes atrás dele.
- **Solução (médio):** abrir no editor e **subir o brilho/contraste das sombras** (ou "Níveis"). No escuro atrás das correntes aparece um recado: *"a ordem é cão, vaca, lebre, javali"* e, embaixo, em letras menores, a palavra **"corrente"**.
- **Difícil:** o arquivo tem um comentário nos metadados com uma frase de reserva. Quem não olha, não perde.
- **O que volta:** a palavra **"corrente"**.
- **Plano B:** livro de dica.

#### E5c. O Eco do Poço (áudio ao contrário)
- **O que é:** uma gravação de cerca de trinta segundos, "ouvida no fundo do poço da Vila Calada". Parece sussurro e água, ininteligível.
- **Solução (médio):** tocar **de trás para frente** (Audacity: Efeito > Inverter). A voz é a do Compadre e diz, devagar: *"quando o sino calar, o que dorme acorda. Digam a palavra do dono."* e a **palavra do dono (exemplo: `RELENTO`)**.
- **O que volta:** a palavra do dono, que vale como terceira pista.
- **Camada difícil:** o fim do áudio, no espectrograma, mostra o **desenho de uma porta com quatro fechaduras** (semente do Ato 5; sem recompensa de avanço).
- **Plano B:** livro de dica.

#### Página final: Resposta
- **O que é:** uma página de livro com três linhas em branco. O grupo escreve as três palavras dos achados (ordem livre).
- **O que volta ao jogo:** o site devolve a **palavra de retorno (exemplo: `SOBRESSALTO`)**, que o grupo escreve no **cadeado do alçapão** da Torre das Correntes.
- **Erro:** *"A página está em branco."* Não diz qual está errada.

### E6. O Disco Riscado (espectrograma do barulho)
- **O que é:** o som das correntes arrastando, que o mod toca de tempos em tempos no mundo todo, gravado num disco nosso.
- **Como se acha a entrada no jogo:** na **jukebox** do porão da Torre das Correntes. Toca no jogo; o arquivo limpo está no Atlas, aberto com a palavra de retorno.
- **Solução:** espectrograma. O barulho esconde, em frequência alta, um **desenho** (uma porta fechada com quatro fechaduras e uma fenda). Não vale avanço. Vale **sinal**: quem desenhar descobre que as quatro relíquias do Ato 5 abrem uma porta.
- **O que volta ao jogo:** nada. Quem mostra o desenho ao admin ganha, por comando dele, uma **relíquia de estética** (cosmético, não vantagem) ou um texto único.
- **Plano B:** não precisa (opcional).

### E7. O Olho do Palheiro (vídeo opcional)
- **O que é:** um vídeo de **cerca de quarenta e cinco segundos**, gravado dentro do jogo, em primeira pessoa, **de alguém escondido no palheiro** da Casa do Açougueiro, olhando pela fresta do assoalho.
- **Como se acha:** a palavra riscada na viga do palheiro abre a página no Atlas.
- **O que mostra:**
  1. Lá embaixo, o aldeão açougueiro puxa as alavancas das correntes em certa ordem. Cada corrente faz soar um **bloco de nota de altura diferente**; a ordem aparece para quem presta atenção ao som, não só à imagem.
  2. Pela janela do palheiro, no curral, um vulto entra pela **porta de ferro** sem acordar nada.
  3. No fim, uma figura de avental, embaixo, **levanta a cabeça para a fresta** e vira de lado, sem pressa. Corte.
- **Solução:** serve de **segunda via** para a ordem das correntes (quem perdeu o Diário p. 2) e para a porta de ferro.
- **O que volta ao jogo:** a ordem das correntes e a entrada segura.
- **Plano B:** a ordem já está no Diário p. 2.

### E8. A Mesa do Pacto (dentro do jogo)
Continua só no jogo (sinal sem fala, velas em ordem; chat de texto bloqueado na capela, voz livre). Não tem parte fora. O poema do Diário p. 4 é o único texto que o grupo pode anotar.

---

## O que mudou em 09/11 (antigo → novo)
| Antes | Agora |
|---|---|
| E1 Fita K7 do carro (toca-fitas) | E1 Gravação do Moinho (blocos de nota); palavra FERRO → **PORTA** |
| E2 Mapa Rasgado na caixa de ferramentas | E2 Mapa Rasgado (mapa de cartógrafo) no baú do moleiro; sem a camada de contraste |
| E3 Livro-Caixa do Frigorífico | E3 **Livro de Trocas** do Açougueiro |
| E4 Fita VHS da Pousada (câmera de segurança) | E4 **Último Olhar do Cartógrafo** (primeira pessoa no jogo); leitura labial → legenda falsa |
| E5 Arquivo do Km 9 (intranet): memorando, foto, ligação | E5 **Atlas do Cartógrafo** (site): Carta do Clérigo, desenho a carvão, Eco do Poço |
| E6 Fita da Repetidora (bunker) | E6 **Disco Riscado** (jukebox da Torre) |
| E7 Câmera do Frigorífico (quatro câmeras) | E7 **Olho do Palheiro** (primeira pessoa) |
| Crachá com o endereço do site | Endereço a carvão na tampa do baú do moleiro |

## Produção (quem faz o quê)
| Item | O que é | Quem faz (proposta) | Formato |
|---|---|---|---|
| Gravação do Moinho (E1) | Blocos de nota em Morse + zumbido escondido | Lore escreve a sequência; mesa de Sons grava no mundo de teste e acrescenta a camada | `.ogg` no mod, `.mp3` no site |
| Mapa / Livro de Trocas (E2, E3) | Texto cifrado gerado no jogo | Dev + Lore (cifras) | texto no mod |
| Fonte Riscos de Corte | Alfabeto nosso | Modelador | `.ttf` |
| Último Olhar (E4) | 1 minuto, primeira pessoa | Gravado pelas mesas na Casa do Cartógrafo, em mundo de teste; legendas na edição | `.mp4` |
| Olho do Palheiro (E7) | 45 s, primeira pessoa | Gravado pelas mesas na Casa do Açougueiro, em mundo de teste | `.mp4` |
| Site Atlas do Cartógrafo (E5) | Página estática cifrada, Vercel | Dev (ou mesa nova de Produção) | HTML estático |
| Carta do Clérigo (E5a) | Página de livro com manchas | Lore escreve; Modelador faz a página | HTML + imagem |
| Desenho a carvão (E5b) | Imagem com texto nas sombras | Modelador / IA a partir de referência aprovada | `.png` |
| Eco do Poço (E5c) | 30 s, invertido | Sons (voz humana autorizada ou sintetizada) | `.ogg` |
| Disco Riscado (E6) | Som das correntes + desenho escondido | Sons | `.ogg` |

- **Os vídeos são gravados nas estruturas construídas**, num mundo de teste. Isso evita inventar cenário e mostra o trabalho de construção.
- **Vozes:** voz humana de pessoa que topar, ou voz sintetizada sem licença complicada. Se for voz de gente real, cada uma precisa de autorização por escrito, registrada em `arte/LICENCAS.md`.
- **Sem marca de outra obra:** nenhuma música, efeito sonoro ou imagem copiada.

## Perguntas para o Nikolas
Respondidas em 09/11 e aplicadas: site novo na Vercel com áudio e vídeo feitos pelas mesas (N6).
1. Nome e visual do site: **Atlas do Cartógrafo** com cara de livro e mapa do jogo? Alternativas: "O Atlas", "Mapoteca". Recomendo o primeiro.
2. PORTA fica fixa no texto (como FERRO), ou vira palavra secreta por temporada (sempre cinco letras)? Recomendo fixa: é palavra de história, não senha.
3. A Dica em livro custa um pouco de Lucidez do grupo. Prefere que custe, ou que seja de graça?
4. Vozes (Eco do Poço): prefere que alguém grave ou voz sintetizada? Se alguém real, quem?
5. O Disco Riscado (E6) dá um bônus cosmético. Aprova, ou prefere que não dê nada?
