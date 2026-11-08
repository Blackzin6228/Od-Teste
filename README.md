# Mod paranormal do CONGUITOS SMP

> Nome provisório: **Od Teste** (modid `odteste`). O nome final entra antes do primeiro lançamento.

Mod novo, feito do zero, de horror paranormal brasileiro para o **CONGUITOS SMP** (NeoForge 1.21.1).
Ele amarra os mods de magia e de tecnologia do pack numa lore só: rituais de giz (Occultism), magia (Iron's Spells),
almas (Malum), sangue (Vampirism), natureza (Botânia) e um ramo tech (Mekanism e Create) para quem não joga magia.

## Quem faz o quê (mesas do Escritório dos Agentes)

Cada mesa é um subagente em `.claude/agents/`. No Escritório, crie um agente com o provedor **Claude Code**,
`watch_path` apontando para a pasta deste repositório no seu PC e `subagent` com o nome da mesa.

| Mesa | Faz | Pastas | Entrega |
|---|---|---|---|
| `lore` | história, elementos, nomes, textos do jogo | `lore/`, `docs/decisoes.md` (propostas) | documento para aprovar |
| `dev` | código Java: sistemas, itens, blocos, criaturas | `src/main/java`, `src/main/resources/data` | PR que compila |
| `modelador` | modelos Blockbench, texturas, animações | `arte/`, `src/main/resources/assets` | prévia → PR |
| `integrador` | magias novas no Iron's, rituais no Occultism, receitas com Mekanism/Create/Malum/Botânia | `src/main/java/.../compat`, `data/` | PR + tabela em `docs/integracoes.md` |
| `revisor` | revisa todo PR, procura crash, confere regras | só comenta | aprovação ou lista de problemas |
| `publicador` | leva o jar para o pack, versão, backup | `.github/`, `ferramentas/` | PR no Conguitos-smp |

## Do código ao servidor

```
mesa faz mudança num branch → Pull Request
→ Actions: compila + testes + LIGA SERVIDOR COM OS ~410 MODS
→ mesa revisor revisa → Nikolas aprova → merge no main
→ publicador leva o jar para o Conguitos-smp (só quando o Nikolas pedir, com backup)
```

## Pastas
- `CLAUDE.md` — regras de todo agente.
- `docs/estilo-do-genero.md` — o gênero que inspira (só inspiração).
- `docs/decisoes.md` — decisões aprovadas e pendentes.
- `docs/progresso.md` — quadro de andamento por mesa.
- `docs/integracoes.md` — o papel de cada mod do pack na lore (proposta).
- `docs/ids.md` — IDs que não podem sumir depois de lançados.
- `lore/` — história, elementos, criaturas, textos.
- `arte/` — referências, arquivos `.bbmodel`, licenças.
- `ferramentas/` — baixar os jars do pack e o teste de servidor.
