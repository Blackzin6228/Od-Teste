# Do estudo ao mod: o que cada mesa faz (Dev, Modelador, Integrador)

> Estudo da mesa Lore, 09/11/2026. Junta `aventura-rpg.md`, `genero-ordem-paranormal.md` e `audiovisual-horror.md`.
> **Nada aqui está aprovado.** Cada técnica vira tarefa só depois do "ok" do Nikolas. Respeita as regras do `CLAUDE.md`
> (integração só em `compat/<mod>/`, IDs em `docs/ids.md`, nada que dê vantagem vendido, arte própria).
> Já existem: `docs/sistemas.md` (Exposição, Lucidez, Marca) e `docs/horror.md` (efeitos de tela e som, com `efeitos_fortes`).
> Este arquivo só diz o que **falta** ou o que **muda de prioridade**.

## 1. Tabela técnica x mesa

| Técnica | Dev (código) | Modelador (arte/modelo/som) | Integrador (outros mods) |
|---|---|---|---|
| **Três pistas** | Cada enigma lê um registro de "pistas" que levam à mesma conclusão. Dado salvo por grupo: `pistas_vistas` | Pelo menos 3 objetos/placas/livros por conclusão (lista no roteiro) | Pistas extras que aproveitam mods do pack (livro do ferreiro, mapa de cartógrafo vanilla) |
| **Mapa de nós** | Estado por grupo: quais nós estão "abertos". Um nó abre quando o grupo tem 3 pistas para ele | Cada nó precisa de um visual identificável (vila marcada, galpão, capela) | Marcar vilas e ruínas de mods de estrutura (ver `integracoes.md`) sem alterar terreno |
| **Revelação em camadas** | Texto com camadas (comum, vermelho por Exposição, tech). Uma função única de "texto do documento" | Livros/papéis com versão normal + camada vermelha (textura) | Camada tech via Mekanism/Create (Analisador, Lâmpada) em `compat/` |
| **Ritmo (calmaria/tensão)** | Contador "tempo desde o último pico" por grupo, visível para o admin. Trava o próximo pico se for cedo | Sons e aparições de pico em lista curta e reconhecível | Respeitar mods de clima/música do pack (não brigar com eles) |
| **Relógios** | Relógio genérico (n fatias, nome, consequência, quem ouve). Mostra no mundo (velas/sino), não na tela | Modelo de suporte de velas (fatias), sino com badaladas | Nenhum no começo |
| **Ganchos por jogador** | Dossiê com 1 linha pessoal por jogador (ex.: "base", "primeira morte") lida de dados do mundo | Ícone/papel do Dossiê | Ganchos tech/magia em `compat/` por mod |
| **Cooperação** | Enigmas aceitam N jogadores (1 a 6); papéis dinâmicos; informação por jogador (`{parceiro}`) | Estruturas com assentos/botões em número variável (ou "assentos extras" sempre presentes) | Voice chat (Simple Voice Chat) para o jogo de sinais do enigma 5 |
| **Grupo travado** | Aviso ao admin "grupo parado há X min no enigma Y"; dica em camadas; `/dica` do admin | Nenhum | Nenhum |
| **Sessão zero** | Texto de boas-vindas único (1ª entrada), `/pausar_clima` para o jogador, config global `efeitos_fortes` (já existe) | Tela/livro de boas-vindas | Nenhum |
| **Temporadas** | Estado de ato/temporada por servidor; comando admin para avançar; recapitulação no Dossiê | Nenhum além do Dossiê | Nenhum |
| **Susto raro, pavor constante** | Fila de eventos com "orçamento de susto" (máx. 1 grande por X minutos por jogador) | Aparições em poucos modelos reusáveis | Nenhum |
| **Som e silêncio** | Camada de som do paranormal (cliente): corta ambiente, sons posicionais, eco, pitch baixo; cooldown | Arquivos de som novos para o que o vanilla não alcança (pitch < 0,5, vozes); licença em `arte/LICENCAS.md` | Cuidar com mods de som/música do pack |
| **Luz e emissivo** | Velas com luz baixa; mobs vistos só no cliente | Texturas emissivas do símbolo e do texto vermelho | Testar com os shaders do pack (se houver) |
| **Névoa** | Fog de cliente por proximidade/medidor (eventos de renderização do NeoForge) | Tom e cor da névoa por elemento | Compat com shaders e mods de clima |
| **Partículas** | Poucas, lentas, só perto do jogador, limite por tick | Textura de gota/pegada/giz | Nenhum |
| **Estrutura por evento** | Já planejado em `docs/sistemas.md`; ensinar o gerador a usar "ponto livre" e admin override | Estruturas pequenas e legíveis em 1 olhar (galpão, capela) | Nenhum worldgen em terreno gerado |

## 2. O que o **Dev** precisa criar (ordem sugerida)
1. **Registro de pistas e conclusões** (dado do grupo). Base de três pistas, nós e grupo travado.
2. **Orçamento de ritmo.** Um serviço central que decide se um evento de pico pode ocorrer agora (tempo desde o último, Lucidez, calmaria combinada). O admin pode ignorar.
3. **Relógio genérico.** Sem UI numérica. Cada relógio tem: id, fatias, consequência, desligável por config.
4. **Texto com camadas** (comum / Exposição / tech) em um só lugar, para livros, placas, mapas.
5. **Aviso ao admin** (já planejado: "Mesa resolvida") estendido para "grupo parado" e "pico pronto".
6. **Camada de som e fog do paranormal** (cliente), com limites de desempenho e o `efeitos_fortes` desligando o que for pesado.
7. **Comando de sessão zero / pausa de clima** por jogador.
8. **Recapitulação do Dossiê** (3 frases por ato, geradas de flags do mundo).

Regras: IDs novos em `docs/ids.md`; dado salvo versionado (o mundo já existe); nada de número na tela do jogador.

## 3. O que o **Modelador** precisa criar
- **Conjunto de "pistas redundantes"** por enigma (livro, placa, objeto, marca no chão). Ver tabela da seção 1 de `aventura-rpg.md`.
- **Suporte de velas / sino para relógios** (modelo que mostra fatias de forma legível sem número).
- **Texturas emissivas** da Cruz de Ferro e do texto vermelho; **cama manchada**; **cabeças** dos bichos.
- **Silhuetas parciais** do Aldeão de Pé e do Magarefe: pensar em "o que o jogador vê de canto" e "o que vê de costas" antes do modelo completo.
- **Paleta por elemento** (Sangue: vermelho velho, ferrugem; sem vermelho vivo).
- **Arte sonora:** lista de sons que o vanilla não faz (pitch muito baixo, sussurro curto, madeira arrastando). Com licença registrada.
- **Prévia primeiro**, sempre (regra 2 do `CLAUDE.md`). Para criaturas: três ângulos escuros, sem rosto definido.

## 4. O que o **Integrador** precisa fazer
- **Mapear o que o pack já tem para cada gancho:** vilas e ruínas de mods de estrutura, sino/cama/cartografia, mods de clima, mods de som, shaders. Dizer o que *atrapalha* (neblina, música ambiente forte, mods que trocam sons).
- **Compat de camada tech/magia** em `compat/<mod>/`, sempre opcional:
  - Mekanism/Create: Analisador, Lâmpada, Medidor (ver `docs/tech-paranormal.md`).
  - Occultism/Iron's: Círculo de giz e rastro (atalhos, não bloqueios).
  - Simple Voice Chat: sinais no enigma 5; voz do admin no evento do Diabo (`docs/voz.md`).
- **Testar no servidor com o pack inteiro** cada efeito de cliente (névoa, partículas, som) para ver se outro mod anula ou duplica.
- **Descobrir a política do pack sobre shaders** (afeta emissivo e névoa). Se o pack não tiver, o mod não pode depender disso.
- **Verificar custo:** quanto de desempenho cada efeito pesa em ~530 mods.

## 5. Critérios de aceite (para QA do Nikolas)
Cada enigma novo só entra se passar por esta lista:
1. Tem 3 pistas para a conclusão principal (aventura-rpg.md, seção 1).
2. Pode ser resolvido por 1 jogador e por 6.
3. Tem plano de grupo travado (dica em camadas e aviso ao admin).
4. Funciona com `efeitos_fortes = false` (sem perder a história).
5. Não exige tech, magia nem item raro.
6. Mostra o estado no mundo, sem número.
7. O servidor liga com o pack inteiro (Actions verde).

## 6. Dez mudanças concretas sugeridas para o roteiro do Ato 1
(Para o Nikolas aprovar uma a uma. Cada uma cita o estudo que a sustenta.)

1. **Redundância no enigma 1:** além do Morse do sino, uma segunda fonte para `FERRO` (um aldeão ferreiro com a palavra na troca, ou uma placa riscada numa casa).
   *Três pistas* (aventura-rpg, seção 1).
2. **Redundância no enigma 2:** o rumo do mapa também sai de **pegadas ou gotas** em linha no terreno da vila e de **um aldeão** que repete a direção. O ritual de giz continua como forma mais rica.
   *Três pistas / nós.*
3. **Dois caminhos para a Capela:** o Livro-Caixa e uma **névoa que sempre empurra** para o ponto da capela depois do sino calar.
   *Nós; ninguém fica sem saída.*
4. **Relógio "O Sino Cala" (6 fatias):** velas num suporte perto do sino da vila, uma por erro/noite sem avanço. Cheio, o Diabo vem sem convite (admin pode desligar).
   *Relógios de ameaça; sem número.*
5. **Régua de ritmo para o admin:** o aviso "{grupo} resolveu a Mesa" ganha irmãos: "grupo parado há X", "último pico há X". O admin vê quando a calmaria já foi longa.
   *Ritmo e grupo travado.*
6. **Calmaria planejada depois do Magarefe e do enigma 5:** nada novo por um tempo longo; só um detalhe pequeno (cama, sino fraco).
   *Calmaria x tensão.*
7. **Gancho pessoal por jogador no Dossiê:** uma linha secreta tirada do que o jogador fez (base, primeira morte, item mais usado).
   *Ganchos por personagem.*
8. **Papéis claros no enigma 5 e número flexível:** quantas cadeiras o grupo tiver, mais a da ponta; tarefas dividas (quem acende, quem lê o poema, quem vigia). Sem chat de texto, mas com sinais combinados no voice.
   *Cooperação.*
9. **Sessão zero dentro do jogo:** na primeira entrada, uma tela/livro curto com o tom ("lento, escuro, em grupo"), um comando de pausa de clima, aviso sobre o chat bloqueado da capela *antes* de ela existir.
   *Sessão zero; linhas e véus.*
10. **Mais pavor, menos susto:** trocar a aparição de pico por **ausência** em pelo menos uma cena (ex.: o Aldeão de Pé aparece como *espaço vazio* onde antes havia um aldeão, antes de mostrar silhueta) e somar **silêncio ambiental** (pássaros e grilos somem) como primeiro sinal do Magarefe e do Diabo.
    *Revelar pouco; som e silêncio.*

**Opcional (fora das 10):** recapitulação de 3 frases no Dossiê ao abrir o jogo, para entrada de novos jogadores (temporadas).

## 7. Riscos e perguntas
- **Pistas demais** podem tirar o mistério. Controlar com a regra "uma conclusão por vez".
- **Ritmo controlado pelo admin** exige admin atento. Se o Nikolas não estiver online, o mod precisa de um modo automático (opcional).
- **Efeitos de cliente + 530 mods:** o Integrador precisa testar antes de prometer.
- **Dependência de shader** para emissivo: não assumir.
- **Temporadas** pedem estado persistente por servidor; combinar com o Dev antes de gravar IDs.
- Perguntas ao Nikolas: aceita relógio visível (velas) mesmo que o grupo possa "ler" que existe tempo? Quer modo automático de ritmo?

## Fontes
Os estudos irmãos desta pasta e as fontes citadas neles:
- `docs/estudo/aventura-rpg.md`, `docs/estudo/genero-ordem-paranormal.md`, `docs/estudo/audiovisual-horror.md`
- Documentos internos: `lore/roteiro.md`, `docs/sistemas.md`, `docs/horror.md`, `docs/integracoes.md`, `docs/voz.md`, `docs/tech-paranormal.md`
- [The Alexandrian, Three Clue Rule](https://thealexandrian.net/?p=7985) e [Node-Based Scenario Design](https://thealexandrian.net/wordpress/8122/roleplaying-games/node-based-scenario-design-collectors-edition)
- [Game Developer, "Scary Games: Why Fear Should Be Easy in Games But It Isn't"](https://www.gamedeveloper.com/design/scary-games-why-fear-should-be-easy-in-games-but-it-isn-t-)
- [Fórum Blades in the Dark sobre relógios](https://bitd.gplusarchive.online/?p=176147) e [Roll20, Lines and Veils](https://bloghub.roll20.net/posts/guest-blog-an-introduction-to-lines-and-veils)
- Pontos técnicos do NeoForge/Minecraft (eventos de névoa e câmera, limites de pitch do `/playsound`) vêm de conhecimento geral e **precisam de confirmação do Dev** na documentação oficial.
