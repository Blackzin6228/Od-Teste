# Ideias para a 0.3 (mesa Lore)

**Nada disto é para agora.** Esta lista só vale DEPOIS que o grupo jogar a 0.1 e a 0.2 e a gente ouvir o que eles acharam.
Cada item abaixo precisa do ok do Nikolas antes de virar tarefa.

## 1. Armas nossas (pedido do Nikolas, 12/11/2026)

**Gatilho:** só entra se, no teste, as criaturas parecerem **fortes demais** ou **sem saída**. Se o grupo estiver se virando, não entra.

Ideia:
- **Poucas armas**, com cara de Minecraft: um **revólver velho** e uma **espingarda de cano serrado**. Nada de arsenal.
- **Munição rara e cara.** Cada bala pesa na decisão.
- **O tiro faz barulho.** Atrai criaturas e aparições. Atirar é pedir companhia.
- **Arma comum só atrasa o paranormal.** Segura, empurra, ganha tempo. Quem **mata** é arma do elemento, artefato ou ritual.
- **Opcional: munição ritualizada**, feita com matéria paranormal. É a ponte com o ramo tech (`docs/tech-paranormal.md`) e com os rituais. Custa Exposição a quem fabrica.

Para decidir depois:
- Dano contra criatura paranormal: zero, reduzido, ou só "empurra"?
- O barulho conta como contato paranormal (Exposição)? Provável que não, mas atrai.
- Receita comum ou só por mérito (como os artefatos, `docs/decisoes/2026-11-12-artefatos-so-por-merito.md`)?
- Tom: arma velha, pesada, com história. Nada de arma "legal" que tire o medo.

Decisão registrada: `docs/decisoes/2026-11-12-armas-so-se-precisar.md`.

## 2. Já previstos no plano (0.2 / 0.3)

Não são ideias novas. Estão aqui só para a 0.3 não esquecer.

- **Kit do Mestre e Mundo Vivo** (`docs/kit-do-mestre.md`): ferramenta do admin para soltar eventos, aparições vistas por um jogador só, formas prontas de entidade. Tem perguntas abertas para o Nikolas no fim do documento.
- **Os cinco lugares do Ato 1** (`lore/fichas-estruturas.md`, `lore/enigmas-fora.md`, `lore/roteiro.md`):
  1. Clareira e a Carroça
  2. Matadouro do Nono Marco
  3. Estalagem do Décimo Segundo Marco
  4. Torre do Sineiro
  5. Fazenda do Sino Mudo (Casarão + Capela)
  Entram no mundo por schematic e comando, sem gerar terreno (regra 11 do `CLAUDE.md`). Ato 1 "bem Minecraft": `docs/decisoes/2026-11-09-ato1-bem-minecraft.md`.
- **Evento do Diabo**: clímax do Ato 1, na Fazenda do Sino Mudo, depois da Mesa do Pacto. O admin solta; o Sino de Matadouro aparece aí (`lore/artefatos-paranormais.md`).
- **Vila Calada** (150x150): `docs/decisoes/2026-11-12-vila-calada-150x150.md`.

## 3. Modelos de jogador com CPM (pedido do Nikolas, 12/11/2026)

Mod: **Customizable Player Models (CPM)**. Licença **MIT**, leve. Preferido ao **Yes Steve Model** (ARR, pesado).
Conferir a licença e a versão para 1.21.1/NeoForge na hora de entrar (regra 10 do `CLAUDE.md`: registrar em `arte/LICENCAS.md`).

Regras de estilo:
- Skins e modelos **no estilo do mundo**: agentes, roupa comum, marcas de uso (sujeira, remendo, desgaste).
- **Nada de anime ou fofo** que quebre o horror (regra de tom sério).
- A equipe pode **fazer o modelo de cada jogador** (com o jogador aprovando a prévia).
- Integração opcional: o CPM não vira dependência dura (regra 4). O mod abre sem ele.

**Antes de entrar no pack, a Dev adapta e testa com modelos do CPM:**
- a **Marca de Sangue** no corpo (veias por estágio, todos veem);
- as **alucinações** (vulto, criatura falsa, porta falsa);
- a **cena de câmera**.

Só entra depois dessa compatibilidade testada e da prévia aprovada. Pastas: código em `compat/cpm/` só se `Compat.Integracao.CPM.ligada()`.

Decisão registrada: `docs/decisoes/2026-11-12-modelos-de-jogador-cpm.md`.

## Ideia futura: melhorar a arma com ritual (13/11/2026)

Ideia do Nikolas, guardada para quando as armas personalizadas estiverem prontas (não é tarefa ainda).
Uma arma pode ser **melhorada** por um ritual (sobe um nível) ou **amaldiçoada** (sobe mais, com um preço ou efeito ruim),
com um teto de níveis. Inspiração só na mecânica, vista num mod de fã; nada de código, arte, nome ou números de lá.
O custo e os rituais são nossos, ligados aos elementos e à matéria paranormal (e ao balanceamento tech).
