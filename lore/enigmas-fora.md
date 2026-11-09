# Enigmas que saem do jogo (Ato 1) (mesa lore)

Status: **rascunho para o Nikolas aprovar.** Base: `lore/roteiro.md` e `docs/decisoes/2026-11-08-producao-estruturas-e-enigmas-fora.md`.

## Aviso importante: o repositório é público
Este arquivo descreve o **método** de cada enigma. As **respostas reais** (senhas, palavras de retorno, textos cifrados finais) são geradas por temporada
e guardadas **fora do repositório** (pasta privada do Nikolas, nunca no GitHub). As palavras abaixo, marcadas **(exemplo)**, são de teste.
Quem joga e lê o repositório não pode achar a solução aqui.

## Regras comuns
- **Entrada sempre achada no jogo.** O site, o áudio e o vídeo só aparecem para quem leu a pista dentro de uma estrutura.
- **O que volta ao jogo é sempre palavra**, digitada num bloco do mod (cadeado de letras, console, cofre). Nenhum número na tela.
- **Plano B dentro do jogo:** cada enigma tem uma **Dica em livro** que o admin pode liberar com um comando (o Dev cria). Se o site cair ou o grupo travar, a história não para. A dica custa um pouco de Lucidez do grupo, para valer a pena tentar sozinho.
- **Sem jogo dentro do jogo para quem não tem tempo:** tudo é resolvível com papel, caneta e programas grátis (Audacity, qualquer visualizador de imagem, um leitor de PDF).
- **Dificuldade em três níveis** para cada um: fácil (sem ferramentas), médio (uma ferramenta comum), difícil (ferramenta que o grupo precisa descobrir). O grupo só precisa passar o médio.
- **Nenhum material de terceiros.** Áudios, imagens e vídeos são produzidos por nós. Cada arquivo é registrado em `arte/LICENCAS.md` com a origem (ver "Produção").

## O site secreto: Arquivo do Km 9
- **O que é:** uma página que parece **intranet de empresa de fachada** (a do Frigorífico). Visual de computador antigo: fundo escuro, letra de máquina, nada de efeito moderno.
  Tom: o do jogo (sério). Nenhum logotipo de outra obra.
- **Como funciona (técnico, proposta para o Dev):** página estática (hospedagem grátis). O conteúdo de cada página fica **cifrado no próprio arquivo**; a senha digitada no navegador é a chave.
  Assim ninguém lê as páginas olhando o código da página, nem no GitHub. A senha de verdade fica fora do repositório.
- **Entrada achada no jogo:** o **crachá** no escritório do gerente do Frigorífico traz o endereço e o usuário (`gerente`). A senha **não** está no crachá.
- **Senha:** sai do **vídeo da Pousada** (E4 abaixo).
- **Páginas (camadas):**
  1. **Início:** aviso de manutenção e uma lista de arquivos com nomes de pasta. Três pastas abrem; uma está "trancada" e só abre depois das outras.
  2. **Memorandos:** o documento "vazado" (E5a).
  3. **Fotos:** a foto com texto escondido (E5b).
  4. **Áudio:** o áudio da ligação (E5c).
  5. **Resposta:** um campo onde o jogador digita as três pistas; se certas, o site devolve a **palavra de retorno** do Arquivo.
- **O que volta ao jogo:** a palavra de retorno, digitada no **console da Casa da Repetidora**.
- **Se o site cair:** o admin libera a Dica em livro (as páginas em texto, no jogo).
- **Pendente:** quem hospeda e o endereço. Nenhum endereço de servidor do jogo aparece no site. O site não coleta nada de ninguém (sem cadastro, sem rastreio).

---

## Enigmas, na ordem em que o grupo os encontra

### E1. A Fita da Clareira (áudio com Morse e camada escondida)
- **O que é:** o áudio da Fita K7 do carro. Batidas longas e curtas com um sino; por baixo, voz chiada ilegível; no fim, um zumbido agudo.
- **Como se acha a entrada no jogo:** o toca-fitas no painel do carro, na Clareira. O áudio toca no jogo, e o mod permite **exportar o arquivo de áudio** (o jogador pega o item "Fita K7" e usa o botão de cópia, ou o admin entrega o arquivo `.ogg`/`.mp3` por fora).
- **Solução:**
  - Camada 1 (médio): Morse. Longa é traço, curta é ponto. Resultado **(exemplo)**: `FERRO`.
  - Camada 2 (difícil): abrindo o áudio como **espectrograma** (Audacity, "Espectrograma"), o zumbido agudo desenha **letras**: *NAO ABRAM A PORTA DA FRENTE*. O aviso protege o grupo no Frigorífico.
  - Camada 3 (opcional): a voz chiada, tocada ao contrário, diz uma frase curta que repete o título do Diário.
- **O que volta ao jogo:** a palavra (cinco letras) no **cadeado da caixa de ferramentas** do carro. Abre o Diário p. 1 e a metade esquerda do Mapa.
- **Plano B:** a Dica em livro dá a tradução do Morse. O espectrograma não tem plano B (é bônus).

### E2. O Mapa Rasgado (imagem com cifra)
- **O que é:** a metade esquerda de um mapa desenhado à mão, com uma frase em letras trocadas na margem. O jogador tira print ou copia a frase.
- **Como se acha:** dentro da caixa de ferramentas (E1).
- **Solução:** cifra de César (deslocamento igual ao tamanho da palavra E1, que o Diário p. 1 sugere: "cinco passos de cada vez"). A frase decifrada diz o **rumo e a distância** do Frigorífico, em palavras.
  A distância é a **real**, calculada com a posição que o admin registrou. Por isso a frase é gerada, não fixa.
- **O que volta ao jogo:** a localização do Frigorífico, que o grupo vai achar andando.
- **Camada extra (difícil):** o verso do print, com os níveis de contraste alterados (Photos, GIMP, qualquer editor), mostra a **grade** do mesmo caminho em coordenadas reais.
- **Plano B:** a Dica em livro dá a regra da cifra.

### E3. O Livro-Caixa (documento cifrado com símbolos)
- **O que é:** um livro de contas com nomes em **Riscos de Corte** (alfabeto nosso, substituição simples) e uma frase longa na última página.
- **Como se acha:** câmara fria do Frigorífico (depois do enigma dos ganchos).
- **Solução:** tabela parcial no Diário p. 3 (as 10 primeiras letras); o resto por **análise de frequência**. Texto claro: *O DIABO NAO ROUBA. ELE ACEITA. VENHAM A MESA QUANDO O SINO CALAR.*
  No verso, em Morse, a localização da **Pousada** e a frase "quem dorme no lilás viu tudo".
- **Fonte:** nós criamos uma fonte (`.ttf`) dos Riscos de Corte e a deixamos para download no Arquivo (depois de aberto). Quem digita o livro no computador acelera.
- **O que volta ao jogo:** a localização da Pousada.
- **Plano B:** a Dica em livro entrega a tabela completa.

### E4. A Fita VHS da Pousada (vídeo de câmera de segurança)
- **O que é:** um vídeo de **cerca de um minuto**, em preto e branco com granulado, de uma câmera de corredor. Duas visões: o corredor da Pousada e a porta do quarto lilás.
- **Como se acha a entrada no jogo:** a **fita VHS** na TV da recepção da Pousada. Colocar a fita no videocassete mostra uma tela de chiado e um **código de acesso para ver o vídeo** (uma palavra curta). Ao digitar no site, o vídeo abre. (Se o grupo tiver um telefone, pode filmar a TV.)
  Alternativa sem site: a TV do jogo exibe os primeiros quinze segundos, só com som; o vídeo completo é por fora.
- **O que mostra (roteiro):**
  1. Um homem de casaco entra no corredor com uma mala, para na porta lilás.
  2. Ele olha para a câmera (tempo mais longo que o normal) e fala uma frase sem som (leitura labial: o jogador precisa ler os lábios).
  3. No quadro de chaves atrás dele, uma etiqueta com uma palavra escrita a lápis. Esta é a **senha do Arquivo (exemplo: `CARNEIRO`)**.
  4. Depois de ele entrar, a câmera pisca e, por **um quadro**, a figura de alguém pendurado no teto do corredor. Pausa quadro a quadro mostra. Não adianta para a senha, adianta para o clima.
- **Solução:** ler a palavra no quadro de chaves (médio: pausar o vídeo e dar zoom) e, se quiser, ler os lábios (difícil). A leitura labial diz *"o sino não toca; ele chama"* e reforça o aviso do Ato 5.
- **O que volta ao jogo:** a senha abre o site (Arquivo do Km 9).
- **Plano B:** a Dica em livro fala a palavra do quadro.

### E5. O Arquivo do Km 9 (site) — três achados
A senha de E4 abre as pastas. O grupo se divide: cada pasta é um achado. Os três respondem a uma pergunta só: **"onde começa a ordem do barulho?"**

#### E5a. Memorando vazado (documento com tarja)
- **O que é:** um PDF de memorando do gerente, com trechos cobertos por **tarjas pretas**.
- **Solução (fácil a médio):** o texto por baixo das tarjas **ainda está no arquivo**; selecionar e copiar (ou abrir em outro leitor) revela. Diz o nome do dono do terreno (um NPC nosso) e a frase *"A repetidora é o sino. Quem cala o sino, chama o resto."*
- **O que volta:** a palavra **"repetidora"** (usada na pergunta da página Resposta).
- **Plano B:** livro de dica.

#### E5b. A foto do Açougueiro (imagem com texto escondido)
- **O que é:** uma foto antiga do Açougueiro (NPC nosso) na frente do Frigorífico, em tom sépia.
- **Solução (médio):** abrir no editor e **subir o brilho/contraste das sombras** (ou "Níveis"). No escuro da janela do fundo aparece um **recado escrito no vidro**, de dentro para fora: *"a ordem é cão, vaca, lebre, javali"* e, embaixo, uma segunda linha em letras menores: um **trecho da frequência** do telégrafo da Repetidora, escrito em palavras (a pista para a Casa da Repetidora).
- **Difícil:** o arquivo também tem um comentário nos metadados (EXIF) com uma frase de reserva. Quem não olha, não perde.
- **O que volta:** a palavra **"telegrafo"**.
- **Plano B:** livro de dica.

#### E5c. O áudio da ligação (áudio ao contrário)
- **O que é:** uma gravação de telefone, de cerca de trinta segundos, vinda do sótão da Pousada. Parece uma conversa em sussurro, ininteligível.
- **Solução (médio):** tocar o áudio **de trás para frente** (Audacity: Efeito > Inverter). A voz diz, devagar: *"quando o sino calar, o que dorme acorda. Digam a palavra do dono."* e a **palavra do dono (exemplo: `PRESEPADA`)**.
- **O que volta:** a palavra do dono, que vale como terceira pista.
- **Camada difícil:** o fim do áudio, no espectrograma, mostra o **desenho de uma porta com quatro fechaduras** (semente do Ato 5; sem recompensa de avanço).
- **Plano B:** livro de dica.

#### Página final: Resposta
- **O que é:** um campo com três espaços. O grupo digita as três palavras das pastas (ordem livre).
- **O que volta ao jogo:** o site devolve a **palavra de retorno (exemplo: `SOBRESSALTO`)**, que o grupo digita no console da Repetidora.
- **Erro:** o site responde só *"Arquivo corrompido. Tente de novo."* Não fala qual está errada.

### E6. A Gravação da Repetidora (espectrograma do barulho)
- **O que é:** o áudio do metal arrastando, que o mod toca de 2 em 2 minutos, disponível como item "Fita da Repetidora" achada no bunker.
- **Como se acha a entrada no jogo:** a fita sobre a bancada do bunker.
- **Solução:** espectrograma. O barulho esconde, em frequência alta, um **desenho** (uma porta fechada com quatro fechaduras e uma fenda). Não vale avanço. Vale **sinal**: quem desenhar, descobre a pista de que as quatro relíquias do Ato 5 abrem uma porta.
- **O que volta ao jogo:** nada. Quem mostra o desenho ao admin ganha, por comando dele, uma **relíquia de estética** (cosmético, não vantagem) ou um texto único.
- **Plano B:** não precisa (opcional).

### E7. A Câmera do Frigorífico (vídeo opcional)
- **O que é:** um vídeo de **quarenta e cinco segundos** com **quatro câmeras** na mesma tela (grade de segurança): pátio, loja, sala dos ganchos e subsolo.
- **Como se acha:** a fita VHS na sala de segurança do mezanino do Frigorífico (precisa do videocassete da própria sala, que o grupo pode ligar).
- **O que mostra:**
  1. O Açougueiro puxa as alavancas dos ganchos em certa ordem (o som de cada alavanca é de **altura diferente**; a ordem aparece em quem presta atenção ao som, não só à imagem).
  2. Na câmera do pátio, um vulto entra pela **porta lateral** sem acordar nada.
  3. Na do subsolo, uma figura de avental olha para a câmera e **vira de lado, sem pressa**.
- **Solução:** serve de **segunda via** para a ordem dos ganchos (quem perdeu o Diário p. 2) e para a porta lateral.
- **O que volta ao jogo:** a ordem dos ganchos e a porta lateral.
- **Plano B:** a ordem já está no Diário p. 2.

### E8. A Mesa do Pacto (dentro do jogo)
Continua só no jogo (sinal sem fala, velas em ordem). Não tem parte fora. O poema do Diário p. 4 é o único texto que o grupo pode anotar.

---

## Produção (quem faz o quê)
| Item | O que é | Quem faz (proposta) | Formato |
|---|---|---|---|
| Áudio da Fita K7 (E1) | Morse + voz + zumbido escondido | Lore escreve o roteiro; áudio gerado/gravado por nós | `.ogg` no mod, `.mp3` para o site |
| Mapa / Livro-Caixa (E2, E3) | Texto cifrado gerado no jogo | Dev + Lore (cifras) | texto no mod |
| Fonte Riscos de Corte | Alfabeto nosso | Modelador | `.ttf` |
| Vídeo VHS da Pousada (E4) | 1 minuto, preto e branco | Gravado dentro do set da Pousada, com filtro de câmera de segurança | `.mp4` |
| Vídeo da Câmera do Frigorífico (E7) | 45 s, 4 câmeras | Gravado dentro do set do Frigorífico | `.mp4` |
| Site Arquivo do Km 9 (E5) | Página estática cifrada | Novo: precisa de uma mesa ou de ajuda do Dev | HTML estático |
| Memorando (E5a) | PDF com tarja | Lore escreve; diagramação por quem tiver | `.pdf` |
| Foto do Açougueiro (E5b) | Imagem com texto nas sombras | Modelador / IA a partir de referência aprovada | `.png` |
| Áudio da ligação (E5c) | 30 s, invertido | Gravado por nós | `.wav` |

- **Os vídeos são gravados nas estruturas construídas.** Isso evita ter de inventar cenário e mostra o trabalho de construção. Como ficam fora do servidor, não dependem de nenhum mod de vídeo (o WATERMeDIA e o plugin do YouTube continuam fora do servidor).
- **Vozes:** voz humana de pessoa que topar, ou voz sintetizada sem licença complicada. Se for voz de gente real, cada uma precisa de autorização por escrito, registrada em `arte/LICENCAS.md`.
- **Sem marca de outra obra:** nenhuma música, efeito sonoro ou imagem copiada.

## Perguntas para o Nikolas
1. O site aberto por senha achada no jogo é o formato certo? Gosta do visual de intranet antiga?
2. Quem hospeda e quem produz os arquivos e os vídeos? Quer uma mesa nova ("Produção") para isso?
3. A Dica em livro custa um pouco de Lucidez do grupo. Prefere que custe, ou que seja de graça?
4. Vozes: prefere que alguém grave ou voz sintetizada? Se alguém real, quem?
5. O espectrograma do barulho (E6) dá um bônus cosmético. Aprova, ou prefere que não dê nada?
