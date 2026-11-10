# Chiado de estática do Analisador só com 800 FE (2026-11-09)
**Decisão:** o Analisador de Sinal só faz o chiado de estática quando tem **pelo menos 800 FE guardados**,
como está no PR #44.
**Por quê:** o chiado mostra que o aparelho está ligado de verdade; sem energia ele fica mudo, e isso também diz ao jogador
que falta carga, sem número na tela.
**Substitui:** nada (confirma o comportamento do #44).
