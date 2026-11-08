# Troca de voz no roleplay (estudo da mesa Integrador, 07/11/2026)

Mods do pack usados: **Simple Voice Chat 2.6.22** (`voicechat-neoforge-1.21.1-2.6.22`) e
**Simple Voice Voice Changer 1.2.5** (`sv-voice-changer-neoforge-1.2.5+1.21.8`). Também estão no pack:
**Walkie Talkie** (rádio) e **VC Admin**.

## 1. O jar do SV Voice Changer carrega no 1.21.1?
**Sim, pelo que o autor declara.** O arquivo que está no pack (`...+1.21.8.jar`) é a versão oficial do Modrinth
(projeto `LLsfORx9`, versão `P6uZmM9Q`), marcada para **1.21 até 1.21.8**, incluindo o **1.21.1**, só NeoForge.
O "1.21.8" no nome é só a última versão da faixa.
- O sha256 do jar no pack (`46197a48…`) é o mesmo registrado no `.pw.toml`, então o arquivo não foi trocado.
- **Não deu para abrir o jar** e ler o `neoforge.mods.toml` (as permissões desta mesa não liberam `unzip`). A prova final é
  o teste de servidor do Actions e abrir o jogo uma vez com o pack. Fica como item para o Revisor conferir.
- O mod é **só do cliente** (mexe no microfone de quem fala). No pack ele está com `side = "both"`. Sugestão para o
  Publicador: marcar `side = "client"`. Isso **não tira o mod do pack**, só não manda para o servidor, que não precisa dele.
- ⚠️ **Licença:** o mod usa **CC-BY-NC-SA 4.0** ("NC" = sem uso comercial). **Decisão do Nikolas (07/11/2026):** o servidor
  hoje é **playtest com amigos, sem fim comercial**, então o mod **fica**. **Rever antes de cobrar qualquer coisa** (pedir permissão
  ao autor, `imsawiq`, ou tirar do pack de quem paga). Como o nosso efeito usa só a API do Simple Voice Chat (seção 3), a troca de voz
  do nosso mod **continua funcionando** mesmo se o SV Voice Changer sair.

## 2. O que já funciona sozinho (sem o nosso mod)
- No menu do Simple Voice Chat aparece o **Voice Changer Studio**: cada jogador escolhe a própria voz.
- Predefinições: **Homem, Mulher, Titã, Criança, Demônio, Rádio**.
- Ajustes: tom (pitch), formante, equalizador, distorção, eco, tremolo, camada robótica, autotune e outros.
- Dá para salvar perfis e compartilhar com os amigos.
- O servidor não precisa de nada extra: o efeito é aplicado **no PC de quem fala**, antes de mandar o áudio.

Para o roleplay, isso já resolve o básico: cada personagem tem a sua voz salva como perfil.

## 3. O nosso mod pode trocar a voz sozinho? Sim, pela API do Simple Voice Chat
Não dependemos do SV Voice Changer para isso. O próprio Simple Voice Chat tem uma API de plugins (o pacote
`de.maxhenkel.voicechat.api`). Os eventos que servem para nós (conferidos no código do SVC, branch 1.21.1):

| Evento | Onde roda | O que deixa fazer | Uso no nosso mod |
|---|---|---|---|
| `ClientSoundEvent` | PC de quem fala | ler e **trocar o áudio cru** (`getRawAudio` / `setRawAudio`) antes de enviar | **Marca alta** ou **relíquia na mão** muda a voz (mais grave, eco, chiado) |
| `ClientReceiveSoundEvent` | PC de quem ouve | mudar o que o jogador **escuta** | Exposição alta: o jogador ouve os outros com distorção ou sussurros (alucinação só dele) |
| `VoiceDistanceEvent` | servidor | mudar **até onde a voz chega** (`setDistance`) | relíquia de Conhecimento: voz some; de Sangue: grito chega mais longe |
| `MicrophonePacketEvent` | servidor | trocar o áudio já codificado (`setOpusEncodedData`) | **não usar**: o servidor teria de decodificar e codificar a voz de todo mundo (pesa) |

### Como fazer (plano para quando virar código)
1. Pacote `compat/voicechat/`, só carrega se `Compat.Integracao.VOICECHAT.ligada()`.
2. Classe plugin com a anotação do SVC para NeoForge (`@ForgeVoicechatPlugin`, **conferir no jar 2.6.22**), registrando os eventos de cliente.
3. O cliente já sabe a Marca e o que está na mão (dados sincronizados pelo nosso mod). No `ClientSoundEvent`:
   se a regra bater, aplica um efeito **leve** no quadro de áudio (20 ms, 960 amostras): mudança de tom, eco curto, distorção suave.
   Custo de processador é pequeno e fica no PC de quem fala. O servidor não sente nada.
4. Efeitos por elemento (proposta): **Sangue** grave e rouco; **Morte** lenta, com eco longo; **Conhecimento** em camadas (sussurro junto);
   **Energia** com chiado de rádio e cortes.
5. Quem liga e desliga: seção 4 (jogador e admin, por comando, salvo no servidor).

### Cuidados
- **Ordem com o SV Voice Changer:** os dois mexem no mesmo áudio. A ordem dos plugins não é garantida, então o efeito nosso
  fica **por cima** do perfil do jogador. Por isso o nosso tem de ser leve.
- **Não dá para escolher o perfil do SV Voice Changer pelo nosso mod**: não achamos API pública dele (e não deu para abrir o jar).
  Se um dia quisermos, é contato com o autor.
- O efeito só muda a voz de quem **tem o nosso mod no cliente** (todo mundo do pack tem).
- Rádio do Walkie Talkie: conferir se o áudio dele passa pelo mesmo evento.

## 4. Quem liga e desliga (decisão do Nikolas, 07/11/2026)
A voz **liga sozinha** (Marca alta ou relíquia na mão), o **jogador pode desligar** e o **admin pode ligar ou desligar para qualquer um**.

### 4.1 Três camadas, a de cima manda
| Camada | Quem mexe | Valores | Padrão |
|---|---|---|---|
| 1. **Admin** | admin (narrador) | `forcar_ligada`, `forcar_desligada`, `livre` | `livre` |
| 2. **Jogador** | o próprio jogador | `ligada`, `desligada` | `ligada` |
| 3. **Automática** | o mod | liga se a regra bater (Marca alta / relíquia na mão), com o efeito do elemento | — |

Conta final: se o admin forçou, vale o admin. Se não, e o jogador desligou, fica desligada. Se não, vale a regra automática.
Com `forcar_ligada` sem regra batendo, usa o efeito do elemento da Marca (ou o que o admin escolher, 4.2).

### 4.2 Comandos (nomes provisórios)
**Jogador** (qualquer um):
| Comando | O que faz |
|---|---|
| `/voz` | mostra o estado: ligada/desligada, por quê (regra, você, admin) e qual efeito |
| `/voz desligar` | desliga a troca de voz para você |
| `/voz ligar` | volta ao normal (automática) |

Se o admin forçou, o jogador recebe: *"O narrador travou sua voz agora."* e o comando não muda nada.

**Admin** (nível de permissão 2, ou o nó `odteste.voz.admin` pela PermissionAPI do NeoForge, para quem usa um mod de permissões):
| Comando | O que faz |
|---|---|
| `/voz admin <alvos> ligar` | força ligada (aceita `@a`, `@p`, nome) |
| `/voz admin <alvos> desligar` | força desligada |
| `/voz admin <alvos> livre` | tira a trava; volta a valer o jogador e a regra |
| `/voz admin <alvos> efeito <sangue\|morte\|conhecimento\|energia\|marca>` | escolhe o efeito usado quando forçada (para cena de evento) |
| `/voz admin <alvo> ver` | mostra as três camadas do jogador |

O jogador vê uma mensagem curta quando o admin muda a voz dele (pode ficar silenciosa com uma opção do comando, para não estragar a cena).

### 4.3 Como funciona por dentro
- O estado fica num **dado salvo no jogador** (anexo de dados do NeoForge), que **passa pela morte** e pelo logout. ID provisório
  `odteste:voz`; entra em `docs/ids.md` quando virar código.
- O servidor manda um pacote pequeno ao cliente de quem fala sempre que o estado muda (login, comando, troca de Marca, relíquia na mão).
  O cliente só lê esse estado no `ClientSoundEvent`. Nada de conta pesada no servidor.
- O `VoiceDistanceEvent` (alcance, no servidor) segue a mesma conta: voz desligada = alcance normal.
- **Limite honesto:** o efeito é aplicado no PC de quem fala. Um cliente modificado conseguiria fugir dele. Para roleplay entre amigos isso basta;
  forçar no servidor (`MicrophonePacketEvent`) pesa demais e não vale.
- Tudo em `compat/voicechat/`. Os comandos `/voz` só aparecem se o Simple Voice Chat estiver no pack (`Compat.Integracao.VOICECHAT.ligada()`).

## 5. Precisa do Nikolas
- Se a alucinação de voz (Exposição alta, `ClientReceiveSoundEvent`) entra. Se entrar, proposta: o mesmo `/voz desligar` desliga as duas coisas,
  e o admin tem `/voz admin <alvos> sussurros ligar|desligar`.
- Se o jogador vê aviso quando o admin trava a voz dele (proposta: sim, menos em cena marcada como silenciosa).
- Licença NC do SV Voice Changer: **rever antes de cobrar** (decidido para o playtest).

## Fontes
- [Simple Voice Voice Changer no Modrinth](https://modrinth.com/project/LLsfORx9) · [no CurseForge](https://www.curseforge.com/minecraft/mc-mods/simple-voice-voice-changer)
- [API do Simple Voice Chat (eventos, branch 1.21.1)](https://github.com/henkelmax/simple-voice-chat/tree/1.21.1/api/src/main/java/de/maxhenkel/voicechat/api/events)
