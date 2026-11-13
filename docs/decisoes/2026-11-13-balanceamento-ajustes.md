# Balanceamento tech: ajustes depois da revisão (13/11/2026)

Respostas do Nikolas às dúvidas da revisão dos PRs #65 e #67 (completam `2026-11-13-balanceamento-tech.md`):

1. **Traje A4/A5:** segura **15%/25% do dano comum** (todo dano que não é paranormal), como está no texto da decisão.
   Não é só o dano que ignora armadura. Contra dano paranormal continua 50%/70%.
2. **Magia passa por cima da resistência das criaturas.** Dano de magia de outros mods (Iron's Spells, Ars Nouveau e afins)
   entra inteiro nas criaturas nossas: a resistência 0/30/60% não vale para ele (e ele não ganha o ×2 do paranormal).
   A magia é reconhecida pelo tipo de dano, na tag `odteste:magia`, só com entradas opcionais: se o mod sair do pack,
   nada quebra. A tag `neoforge:is_magic` fica de fora de propósito, porque traz espinhos, veneno, wither e poção de dano.
3. **MekaSuit:** absorve **0,5 do dano paranormal** (data map `mekanism:mekasuit_absorption`, no PR do Integrador).

Valores continuam ponto de partida: ajuste no playtest.
