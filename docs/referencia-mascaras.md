> Histórico do mod antigo **Máscaras Relíquia**, copiado do repositório `Blackzin6228/Mascaras`.
> São ideias do próprio Nikolas: Exposição, Lucidez, Marca, Transcender, Vínculo, Relíquia Viva, trajes que mudam com a Marca.
> Use como **ponto de partida** para os sistemas do mod novo (reaproveitar o que funcionou, melhorar o resto). Não é para copiar código.

# CONGUITOS SMP — tudo o que fizemos neste chat

Registro do trabalho no **mod Máscaras Relíquia**, no **Banco Central**, no **modpack CONGUITOS SMP** e no **launcher**,
mais o jeito que o Nikolas gosta de **texturas, skins e lore**. Serve para retomar o projeto em outro chat sem perder nada.

- **Minecraft** 1.21.1 · **NeoForge** 21.1.262
- **Servidor**: Lura (endereço no painel; não colocar em arquivo público), SFTP pelo FileZilla
- **Pack**: GitHub `Blackzin6228/Conguitos-smp` (formato packwiz, instalado pelo Prism)
- **Instância de desenvolvimento**: CurseForge "Conguetos (2)"

---

## 1. Versões atuais

| O quê | Versão | Onde |
|---|---|---|
| Máscaras Relíquia | **1.34.0** | pack do GitHub; servidor precisa receber pelo `PARA_O_SERVIDOR.zip` |
| Banco Central | **2.1.0** | pack do GitHub; servidor idem |
| Modpack (packwiz) | **8.3.0** | `https://raw.githubusercontent.com/Blackzin6228/Conguitos-smp/main/pack.toml` |
| Código-fonte das Máscaras | `mascaras-fonte-1.33.0-original.zip` (a 1.34 está no projeto local) | Downloads |
| Código-fonte do Banco | `bancocentral-v2.1.0-fonte.zip` | Downloads |

**Atenção:** o projeto em `Downloads\mascaras` que outro chat montou é **decompilado** (variáveis `var1`, `var2`…) e não compila.
O código de verdade é o original, que ficou comigo e nos zips `-original`.

---

## 2. Máscaras Relíquia — o que mudou

### 1.29.1 — Lascas mais fortes
Lascas e Caco subiram de dano (dano em área e no dash da Lasca do Ocaso também).

### 1.30.0 — Transcender
- Quem tem **Vínculo** pode transcender no Círculo: **agachado, componente do elemento na mão, outra mão vazia**.
- Até **4 níveis**. Cada nível pede Exposição (60 + 10 por nível) e XP. Em troca, a Lucidez máxima cai 15 por nível.
- **Ganha**: Despertar mais forte, ritos mais fortes, mais chance de o rito não gastar o componente.
- **Paga**: Lucidez máxima menor. A partir do nível 2, coisas estranhas à noite e criaturas que aparecem.
- Comandos: `/transcender ver <jogador>`, `/transcender definir <jogador> <0-4>`.
- Página "Transcender" no Dossiê.

### 1.32.0 — Abertura do Arco 1 (veio do roteiro no Claude Doc)
- **Armas mais fracas no começo, que crescem com o dono.** Katana 40 → **25**, Lasca 28 → **16**. **+2 de dano por estágio de Marca**, **×1,2 com Vínculo do mesmo elemento**, e o Despertar multiplica por cima.
- **Exposição só sobe.** Não cai com o tempo nem com rito. Subir de estágio faz barulho para quem está a até 96 blocos.
- **99 → 100 só na Noite da Costura** ou por comando de admin.
- **Páginas por datapack** (`data/mascaras/paginas/<n>.json`, com `texto`, `capitulo` e `destinatario`). As **Páginas 21 a 32** já vêm no mod. Uma Página endereçada só pode ser lida pelo destinatário; os outros veem glifos. A 22 é do **Kayoshin** e a 23 do **Barapay** (falta confirmar o nick do Barapay).
- **Comandos de cena** (OP):
  - `/costura esfriar`: A Lâmina Esfriou + Página 21.
  - `/costura entregar <jogadores> <n>`
  - `/costura chave <jogador>`
  - `/costura caderno <jogador> <vigiado>`
- **Tecla H** (Afinidade): conjura a magia do Vínculo sem grimório, pagando com Lucidez. Grimório do próprio elemento gasta metade da mana; do elemento que sufoca o seu, o dobro.
- **Itens do Arco**:
  - **Contrato de Kayoshin**, **Chave da Garantia**, **Caderno do Prefeito**: itens de história; só o narrador dá.
  - **Vela de Sebo**: segura a Lucidez de quem está perto.
  - **Sino Rachado**: paralisa os Cinzentos.
  - **Frasco de Estática**: captura um Estalo e desliga máquinas.

### 1.33.0 — O Avesso entra nos outros mods
- **Ritos no pentáculo do Occultism**: os 10 ritos também podem ser feitos no Occultism e saem **50% mais fortes**. O Círculo e o Giz continuam funcionando.
- **Mekanism × Máscaras**:
  - **Cinza Irradiada**: Cinzento morto em chão irradiado. Radiação alta também chama Cinzentos.
  - **Sangue Destilado**: Carne + frasco no Combinador.
  - **Disco Escrito**: célula 1k vazia do AE2 usada na Escrita, ou drop raro do Escriba. Mostra coordenadas incompletas de uma Raiz.
  - **Condensador do Avesso**: 1 milhão de FE vira um bloco de Plasma, às vezes junto com um Estalo.
  - Os ritos avançados no pentáculo pedem esses itens, e o de Energia pede Polônio.
- **Trajes** passaram a pedir uma **Liga Atômica** do Mekanism.
- **Itens novos**:
  - **Olho de Vidro do Escriba**: mostra o Vínculo de quem você encara.
  - **Lanterna do Limiar**: estica um fio de luz até a Raiz mais próxima.
  - **Bateria de Alma**: quando esvazia, bebe a Lucidez de quem está perto.
  - **Ampulheta da Garantia**: volta você 3 segundos no tempo, uma vez por dia.
  - **Selo da Prefeitura**: só do narrador.
- **Componentes de segunda linha**: Dente do Retalhado, Pena do Escriba, Cinza Acesa e Caco do Estalo. Caem às vezes das criaturas e sempre de **19 chefes** (Cataclysm, Twilight Forest, Mowzie's).
- **KubeJS** (`zz_mascaras_integracao.js`): a receita de topo de Draconic, Powah, Extreme Reactors, Enigmatic, Malum, Botania e Industrial Foregoing passa a pedir um pedaço do Avesso.
- **Fonte do Avesso**: glifos próprios. A Escrita lida sem as Lentes, e as Páginas que não são suas, aparecem nela.
- Uma revisão independente achou 8 problemas, todos corrigidos. O mais grave: o Condensador travaria o servidor depois de encher.

### 1.34.0 — Relíquia Viva e interface escura
- **Relíquia Viva** (no lugar do "Atravessar"): quem termina a Travessia em 100% **não perde o personagem, vira a relíquia**, com um ✠ antes do nome.
  - **Ganha**: toda relíquia desperta na mão dele, de qualquer elemento e mais forte; +12 de vida e +25% de força para sempre.
  - **Paga**: a máscara do rosto não sai mais; a Lucidez não volta sozinha (só por rito, derrotando O Voltado ou pelo narrador); criaturas aparecem perto dele.
  - Comando: `/reliquia_viva <jogador> [true|false]`.
- **Interface escura**: inventário, baús, fornalhas, mesas, botões e abas do Minecraft em preto e grafite. O **vermelho escuro** é o único destaque, nos botões em foco. Os títulos das telas ficam cor de pergaminho. Telas de outros mods com textura própria (Mekanism, AE2…) não mudam.

---

## 3. Banco Central 2.1.0 — revisão e correções

- **Duplicação de dinheiro pela carteira (o mais grave):** a carteira aberta podia gravar uma cópia velha por cima do que o caixa ou um comando tinha mexido. Dava para repetir com cliques rápidos no chat ou com um Deployer do Create. Agora a tela fecha sem gravar quando algo muda por fora, e máquinas não usam carteira nem caixa.
- **Tesouro atômico:** salário, imposto, multa, pagamento, PIX com taxa, emitir, queimar e imprimir mexem no saldo e no Tesouro na mesma transação. Um erro de leitura não zera mais o Tesouro.
- **Valores:** `10,00` = R$ 10 (antes virava R$ 1.000). `1,50` e `1.5` dão erro. `1,5k`, `2mil` e `3m` funcionam.
- Cédula falsa dentro de carteira não vira dinheiro no caixa.
- **O que o Nikolas pediu:** saiu o `/banco ranking` e o botão Ranking. O autocompletar com nomes de contas só aparece para admin.
- Extrato no horário de Brasília; comandos não dão erro com o banco fechado.
- **Painel web**: limite de tentativas de senha, senha só pelo cabeçalho, sem acumular threads.
- **Segurança:** a senha do painel e a do editor web do Resourceful Config estavam no pack público do GitHub. Foram retiradas. **Falta trocar a senha no servidor:** apagar o `senhaPainel` em `config/bancocentral.json` e reiniciar.

---

## 4. Modpack, servidor e problemas resolvidos

### Launcher e instalação
- **Pack no GitHub em formato packwiz.** Os 406 jars ficam no próprio repositório. Os 42 mods só de cliente são marcados e pulados no servidor.
- **Instância do Prism**: `https://raw.githubusercontent.com/Blackzin6228/Conguitos-smp/main/launcher/CONGUITOS-SMP.zip`
- **`INSTALAR-CONGUITOS.bat`**: instala o Prism portátil, cria a instância, ajusta a memória pela RAM do PC e põe um atalho que entra direto no servidor. Usa só `curl` e `tar` do Windows: a primeira versão em PowerShell deu "Acesso negado".
- **Correções**: aspas no comando de pré-inicialização (o Prism juntava o caminho do Java com `-jar`); tirados os argumentos de memória do Java que conflitavam com launchers que já usam ZGC.
- **Atualização automática**: quem abre pelo Prism baixa só o que mudou. Mundos, opções e contas nunca são mexidos.
- **Servidor sozinho pelo GitHub** (pack 8.4.0): `ATUALIZACAO_AUTOMATICA_SERVIDOR.zip`. A pasta `conguitos/` tem o `conguitos-atualizador.jar`, que entra como `-javaagent` no `user_jvm_args.txt` e no `unix_args.txt` e roda o packwiz antes do servidor ligar. Se der erro, volta a pasta mods como estava. Na primeira vez guarda backup e mantém os mods só do servidor; o WATERMeDIA nunca volta. Para desligar: `nao_atualizar.txt`. Fonte em `launcher/servidor/fonte`.
- **Máscaras 1.34.1**: Frasco com Caco do Estalo (não come mais o Condensador); Passagem pelo Occultism só com Olho do Ender e obsidiana chorona; Morte avançado com membrana de phantom no lugar do leite; giz aceita qualquer carvão; Marco aceita qualquer vela. KubeJS: as 4 trocas do Avesso que não pegavam foram para as receitas novas, e o JEI esconde só as receitas do Máscaras.
- **Contas**: o Prism e o servidor pedem conta Microsoft original. Não fiz nada para SKLauncher ou ely.by.

### Problemas resolvidos ao longo do chat
| Problema | Solução |
|---|---|
| KubeJS: `JEIEvents is not defined` | KubeJS 7 usa `RecipeViewerEvents` (`mascaras_jei.js` reescrito) |
| Servidor caía com WATERMeDIA | WATERMeDIA e o plugin do YouTube **nunca** no servidor; WaterVision nos dois |
| Link do YouTube não toca na TV | Plugin quebrado; usar Twitch, Kick ou link direto (.mp4, .m3u8) |
| Botão "+" do AE2 não aparecia | O AE2 19.2.18 só integra com EMI/REI, então foi instalado o EMI |
| Comandos sem resposta | `gamerule sendCommandFeedback true`; havia jar 1.29 e 1.30 juntos no servidor |
| Vínculo do Kayoshin apagado por engano | `exposicao Kayoshin 50`, depois `execute as Kayoshin run vinculo sangue` |
| "Multiple garbage collectors selected" | Deixar só um coletor (preset do launcher **ou** argumentos da instância) |
| "Espaço insuficiente no disco" | Limpar `%temp%`, a Lixeira e os instaladores velhos da pasta Downloads |
| Radiação que não sai | `/mek radiation heal`, `/mek radiation removeAll`, ou `/effect clear <jogador> alexscaves:irradiated` |

### Para colocar a versão atual no servidor
1. Fazer backup da pasta `world`.
2. Apagar `mascaras-1.30.0.jar` e `bancocentral-2.0.0.jar` da pasta `mods`.
3. Enviar o conteúdo do `PARA_O_SERVIDOR.zip` (pastas `mods` e `kubejs`).
4. Antes de ligar, conferir se alguém está em 100% de Exposição (`/exposicao <jogador>`). Quem estiver vira Relíquia Viva ao entrar.

---

## 5. O jeito que o Nikolas gosta

### Regras que não mudam
- **Sempre em português.**
- **Inspiração só no gênero** de horror paranormal: nada copiado de obra existente (nomes, símbolos, personagens, textos).
- **Nenhum número na tela** para o jogador. Distância, chance e quantidade aparecem em palavras ("perto", "longe", "o Avesso cobra").
- **Só sons do Minecraft vanilla**, com pitch baixo (0,4 a 0,7) e eco.
- **Relíquia não tem receita**: máscaras, katanas e relíquias só por evento ou comando. Trajes, só com receita difícil.
- **Estrutura nunca dá item difícil de fabricar**: nada de peça de traje, selo ou acessório em loot.
- **As receitas do mod ficam escondidas no JEI/EMI.** Descobre-se pelo Dossiê, pelas Páginas e pela Escrita.
- **Nunca remover mods do pack**, nem enfraquecer o FTB Ultimine, o DimStorage ou o RFTools Builder.
- **Os dados dos jogadores não podem se perder**: toda versão nova só acrescenta.
- **Teste no servidor com backup** antes de virar regra do roleplay.

### Tom da lore
- **Mistério e horror macabro** em vez de sistemas novos. O momento favorito do servidor: o Diabo conversando e a Magistrada surgindo para fazer pacto com outro jogador.
- **Frases curtas**, em segunda pessoa, que **sugerem e não explicam**. O preço vem sempre junto com o poder.
- **"Comportamento, não estatística"**: cada item faz uma coisa que se nota, cobra um preço e conta uma história.
- Ideias recusadas: o evento "As Sete Luas" (estilo Hexatombe).
- Ideias aprovadas para depois: Patente do agente, Quadro de investigação, Morrendo + Eco, Perseguição, Interlúdio em grupo, Ritual ampliado.
- **Exemplos do tom certo** (textos que estão no jogo):
  - *"A lâmina esfriou na sua mão. Não foi castigo. Foi recibo."*
  - *"Não queima óleo. Queima o que você ainda tem de lúcido."*
  - *"Uma vez por dia, devolve três segundos. A Garantia sempre cobra o que empresta."*
  - *"Costurado na gengiva com linha preta. Ainda morde."*
  - *"O Avesso não te levou. Ele mora em você agora."*
  - *"Engole energia sem parar. Quando se sacia, cospe Plasma, e nem sempre só Plasma."*

### Paleta por elemento (bíblia visual)
| Elemento | Cores | Traço | Clima |
|---|---|---|---|
| **Sangue** | vermelho `#8B0000`, preto, detalhe `#FF1A1A` | vitral e veias, linhas grossas pretas | saturado, névoa densa |
| **Morte** | preto, osso `#E8E0D0`, cinza | gravura, hachura, grão | sem cor, vinheta, cinza caindo |
| **Conhecimento** | pergaminho `#D9C7A0`, tinta `#2B1D0E`, dourado fosco | papel rasgado, manuscrito, selos | sépia, luz de vela |
| **Energia** | roxo `#6A00FF`, magenta `#FF2BD6`, ciano | neon, estática | aberração cromática, relâmpago |

**Regras gerais de visual:** estética **punk e macabra**; interface em preto com **um único acento**; nenhum símbolo copiado das referências (elas servem só de clima).

### Texturas de item
- **Pixel art limpa**, desenhada em 16×16 e salva em **32×32** como as outras do mod.
- **Contorno preto de 1 pixel**, luz vindo de **cima à esquerda**, poucas cores por item.
- Forma reconhecível de longe: frasco, sino, dente, pena, lanterna, ampulheta.
- Blocos em 16×16 com a mesma paleta (por exemplo, o Condensador é metal escuro com núcleo roxo; o Selo é um lacre vermelho com dourado).

### Skins (o que funcionou e o que não)
- **Não gostou**: skins cheias de detalhe, com sangue escorrendo e ruído ("ficou muito feio").
- **Gostou**: a **skin do Vínculo com o Conhecimento**. É a skin original do Nikolas **inteira e limpa**, com **poucos detalhes certeiros**: olhos amarelos, tatuagens ocre e um olho dourado nas costas.
- Seguindo esse padrão:
  - **Pacto (Sangue)**: olhos de brasa, pontas das orelhas vermelhas, uma rachadura fina, gravata carmim com alfinete dourado, pesponto vermelho, o Sinal do Pacto nas costas e um fio vermelho no pulso.
  - **Morte**: sem máscara, rosto pálido, mecha cinza, gravata preta, relógio de bolso parado, o sinal do Ocaso nas costas e um fio preto no pulso.
- Pipeline do roteiro: conceito → skin → retoque no Blockbench/NovaSkin → **Marca por estágio** como camada separada.

---

## 6. O que falta

- [ ] Colocar Máscaras 1.34 e Banco 2.1 no servidor (`PARA_O_SERVIDOR.zip`).
- [ ] Trocar a senha do painel do Banco Central.
- [ ] Ligar o `atualizar.sh` na inicialização da Lura (mandar print da aba de inicialização).
- [ ] Confirmar o nick de Minecraft do Barapay (Página 23).
- [ ] Testar no jogo: `/costura esfriar`, `/costura entregar`, tecla H, um rito no pentáculo, o Condensador, a interface escura.
- [ ] Opcional: corrigir a Página 21 duplicada quando há dois portadores de katana perto um do outro.
- [ ] **Degraus da Exposição**: o que cada estágio ganha e perde para sempre (por exemplo, Lucidez máxima 60 em "À beira").
- [ ] **Dicas no jogo** das receitas do pentáculo (Escritas ou Páginas).
- [ ] Manto de Segunda Pele e Cédula Marcada (liga o Banco Central ao Avesso).
- [ ] **Elenco de mobs com nome**, previsto para a 1.40: O Enterrado, A Garantia, A Cobradora, O Relojoeiro, Os Três Escrivães, A Que Não Para, O Voltado de Barapay, O Último Agente.
- [ ] **Trilogia do Medo**, prevista para a 1.40 e a 1.50: O Sem Boca, a Máscara do Medo, O Atrás, o Quinto Avesso, o Vínculo do Medo e a Costura Final.


---

## 6. Estilo v2 (sessão de 04/11/2026) — EM ANDAMENTO, ainda não publicado

### O que o Nikolas decidiu
- Tudo em **64x** (4 texels por unidade), estética de **horror urbano no gênero de Ordem Paranormal**: agentes com roupa comum (jaqueta, moletom, colete social, cachecol, coturno, tênis) e UMA cor de destaque; criaturas grotescas e explícitas. **Só o gênero**: nada de criatura, personagem, símbolo ou nome da série.
- **Trajes (6)**: Agente (neutro, jaqueta + moletom + jeans + coturno + lanterna), Carmim (Sangue, lutador, faixas vermelhas, braços nus), Arquivista (Conhecimento, colete social, gravata dourada, óculos, bolsa), Luto (Morte, sobretudo até o tornozelo, cachecol, chapéu), Estático (Energia, punk, jaqueta com patches neon, correntes, tênis grande), Remendado (os 4, capa de retalhos, capuz). Sem arma presa no traje. Muito volume, abas que balançam.
- **Todo traje muda com a Marca de quem veste** (qualquer elemento, estágios 1-4, e a Marca do Fim). Só a Marca de quem veste aparece. Só visual (sem bônus). 18 texturas por traje.
- **Criaturas (bichos impossíveis, grandes)**: Retalhado (bruto de carne costurada, cutelo, coração aceso; especial: investida + ganchos + sangramento), Escriba Oco (livro aberto que flutua, olhos, braços de pena; especial: quem olha perde Lucidez), Cinzento (lodo e engrenagens em 4 pernas de pau, relógio com rosto atrás do vidro; especial: volta o jogador 3 s no tempo), Estalo (núcleo neon com anéis de fragmentos; especial: pulo para trás do alvo com descarga), Voltado (chefe costurado dos 4; muda de elemento a cada 1/4 de vida). Comportamentos antigos mantidos.
- **Máscaras**: objetos do dia a dia — hóquei (Sangue), solda (Conhecimento), gás (Morte), porcelana (Energia); versões fracas quebradas. Não mudam com a Marca.
- **Katanas**: armas vivas (Presa Rubra de músculo e dentes, Oráculo de papel e olhos, Foice de ferro e engrenagens, Fratura de cacos e estática).
- **Artefatos**: vivos, como as katanas. **Ícones**: 64x.
- **Efeitos**: AAA Particles (Effekseer) em TUDO. Os .efkefc saem do editor Effekseer, que roda no PC do Nikolas (o chat deve estar ligado ao PC pelo app Claude desktop). CLI: `Effekseer.exe -cui -in efeito.efkproj -o efeito.efkefc`. API do mod: `AAALevel.addParticle(level, ParticleEmitterInfo.create(level, id).position(...))`, arquivos em `assets/mascaras/effeks/*.efkefc`.
- **Animação do jogador** nos especiais das katanas e rituais: Player Animator (dev.kosmx.playerAnim, já no pack).
- **Impacto forte**: tremor de tela e flash em todo golpe pesado.
- Ordem: primeiro **subir o nível do que já existe** (silhueta em camadas, peças afinando, animação com easing/antecipação/impacto, som e partícula no quadro certo — referência: Cataclysm e Mowzie's Mobs), depois artefatos e ícones, depois o resource pack (interface vanilla + telas do mod + telas de outros mods tingidas; itens/armas vanilla; zumbi, esqueleto, aranha, creeper).
- Entregar no servidor **tudo junto no final** (sugestão pendente: 3 levas testadas).

### O que já está feito (no código-fonte, NÃO publicado)
- `tools/v2/motor.py`: cubos com rotação, pintura por material (limpa, 3-4 tons, contorno), atlas, .geo.json GeckoLib, .bbmodel, prévia renderizada, exportador de item Java (`java_item`, `java_item_diagonal` para katanas), `repintar` (variantes com o mesmo UV).
- `tools/v2/trajes_v2.py` + `rodar_trajes.py`; `criatura_sangue.py` + `criaturas_v2.py` + `rodar_criaturas.py`; `mascaras_v2.py` + `rodar_mascaras.py`; `katanas_v2.py` + `rodar_katanas.py`.
- Java: `TrajeModelo` escolhe a textura pela Marca de quem veste (MarcaPayload.CLIENTE); `AtaquesAvesso` (investida/cravar/sangramento, olhar, voltarNoTempo, pulo); `CriaturaAvesso` com `tentarEspecial`/`intervaloEspecial` e animação "especial"; hitboxes maiores; Voltado com fases.
- Validador do plugin Blockbench headless (`bun run headless/call.ts --root <pasta> bbmodel_validate '{"file":"x.bbmodel"}'`, clone de github.com/jasonjgardner/blockbench-mcp-plugin).
- Compila como `mascaras-1.35.0` (`build-deps/compilar.sh 1.35.0`). Versão em gradle.properties continua 1.34.1 até publicar.

### Também feito nesta sessão (publicado)
- Revisão das receitas → Máscaras **1.34.1** e KubeJS corrigido (pack **8.4.0** no GitHub).
- Servidor se atualiza sozinho: `conguitos-servidor.jar` na pasta principal + painel da Lura com "Arquivo de inicialização .jar" = `conguitos-servidor.jar` (pasta `conguitos/` com o atualizador e o packwiz). Fonte em `launcher/servidor/` do repositório do pack.

### ⚠️ Avaliação do Nikolas no fim da sessão (04/11/2026)
- **As criaturas e os itens v2 (máscaras, katanas) ficaram FEIOS.** Não continuar nesse caminho (modelo gerado por código "no escuro").
- Próximo chat: modelar de forma **iterativa e visível** — de preferência no Blockbench do PC dele via plugin MCP (jasonjgardner/blockbench-mcp-plugin), com print a cada passo e correção ao vivo; pedir **uma referência visual por criatura/item** antes de modelar; perguntar o que exatamente não agradou.
- O código Java dos ataques novos (AtaquesAvesso, fases do Voltado), a troca de textura dos trajes pela Marca e o atualizador continuam valendo; o que foi reprovado é o visual.
- Nada do v2 foi publicado: no servidor e no GitHub continuam os modelos antigos.
- **Fluxo combinado para modelagem (próximo chat):** (1) o Nikolas gera a arte conceitual em IA de imagem; (2) passa a arte em IA de 3D com estilo voxel (Meshy, Tripo3D, VoxAI, SupaVoxel) e manda o .vox/.glb/.obj; (3) eu converto voxels → cubos do Minecraft (greedy merge para ficar leve), separo em ossos, animo (easing, antecipação, impacto), faço glowmask, efeitos AAA e integro/compilo. Testar primeiro com UMA criatura (a de Sangue) antes de seguir.

### Balanço do Nikolas (05/11/2026, 00h)
- **Gostou e quer manter:** os comandos, os efeitos, as Marcas, as katanas, as roupas (trajes) e as máscaras ("ficaram perfeitas", mas dá para melhorar).
- **Não gostou:** dos **mobs** (criaturas) e dos **símbolos** (sigilos/círculos desenhados).
- **Plano de trabalho:** dividir em vários chats, um tema por chat, e possivelmente usar o projeto dele **Escritório dos Agentes** (equipe QA, Dev, Revisor, Documentador) para tocar as frentes em paralelo.
- Frentes sugeridas: (1) infraestrutura — fonte do mod num repositório GitHub **privado** + compilação automática; (2) mobs refeitos com referência + IA de 3D; (3) símbolos novos com referência; (4) efeitos AAA + animação do jogador (chat ligado ao PC); (5) resource pack; (6) lore/RP.
- **Esclarecimento:** as katanas que ele gostou são **as que estão no servidor** (versão antiga, não as "vivas" do v2). Base para melhorar: partir das katanas do servidor. **Máscaras: ficam as do servidor** (1.34.1, as demoníacas). As v2 de hóquei/solda/gás/porcelana e as katanas "vivas" foram **removidas do código** (voltaram para as do servidor; dá para recuperar no histórico do git, commit 24bdd63).
- Quer melhorar muito ainda: **efeitos, partículas, modelagens, itens**. Melhorar em cima do que já agradou, com referência, em vez de trocar tudo.

---

## 7. Sessão de 05/11/2026 (tarde e noite) — pack 8.5.0 a 8.6.2

- **8.5.0 — KubeJS limpo.** Saíram todas as receitas alteradas de outros mods (`mascaras_mystical.js`, `mascaras_novos_mods.js`, `mascaras_tech_nova.js`, `zz_mascaras_integracao.js`). Ficaram:
  - `mascaras_expert.js`: só os 4 acessórios e as receitas dos 6 trajes;
  - `mascaras_dificuldade.js`: bloqueios (itens criativos, `botania:cre/`, `infinity_nuke`, máscaras e katanas sem receita), a cadeia da Liga Paranormal, a cadeia da Linha do Avesso (Cristal do Vazio, Fragmento, Linha) e as receitas da MekaSuit;
  - `nomes_ocultos.js`, `itens_no_chao.js` e as falas do Diabo.
- **8.5.1** — `w2w2` marcado para cliente e servidor (dava erro de canal ao entrar). Lição: o `gerar_pack.py` só reescreve o lado dos mods com `--jars`.
- **8.6.0** — Building Gadgets 2 (21.1.1) e Elevator Mod (1.11.4). **8.6.1** — Productive Bees 13.13.5 (a 13.14.0 não era para 1.21.1).
- **8.6.2** — **Meka-Gun bloqueada** (derrubava o servidor): `kubejs/server_scripts/bloqueios.js` tira a receita e cancela o uso com a mensagem "A Meka-Gun emperrou. Ela está desligada neste servidor."
- **Atualização automática do servidor funcionando** (log `logs/conguitos-atualizador.log` mostra sucesso desde 05/11 15h32). Para pegar uma versão nova do pack: reiniciar o servidor.

## 8. Decisão: o mod vai ser refeito do zero (05/11/2026, noite)

- Repositório novo: **`Blackzin6228/Mascaras`**. Serve para o código novo **e** para organizar os chats (pasta `chats/`, regras no `CLAUDE.md`).
- O modpack continua o mesmo (`Blackzin6228/Conguitos-smp`) e o servidor continua se atualizando sozinho por ele.
- O mod antigo vira **referência** (o zip `mascaras-fonte-v2-em-andamento.zip`): copiar ideias e trechos que funcionam, não a estrutura inteira.
- **Fica do antigo (aprovado):** comandos, efeitos, Marcas, as katanas do servidor (1.34.1), os trajes (e a ideia v2 de mudar com a Marca), as máscaras do servidor, Exposição, Lucidez, Vínculo, Transcender, Relíquia Viva, Páginas, Dossiê, Escrita.
- **Refazer:** criaturas (com referência + IA de 3D) e símbolos (propostas em 2D primeiro).
- **IDs:** a lista do que está no mundo está em `docs/ids-legado.md`. Manter o modid `mascaras` e os IDs evita perder itens e dados dos jogadores.
