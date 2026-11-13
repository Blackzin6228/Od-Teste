# Criaturas de Sangue: hitbox maior, textura B e animações novas (13/11/2026)

Aprovado pelo Nikolas para a 0.2 (Retalhado e Sanguessuga-Mãe, já no jogo desde o PR #63):

1. **Hitbox** igual à sugerida pelo Modelador (largura x altura): **Retalhado 1,9 x 3,1** (era 1,5 x 3,0) e
   **Sanguessuga-Mãe 2,8 x 2,1** (era 2,6 x 1,9).
   - Conferido: para andar, o Minecraft arredonda a largura para blocos inteiros. O Retalhado continua precisando de
     um vão de 2 de largura e 4 de altura (igual a antes). A Sanguessuga continua precisando de 3 de largura; a altura
     passa de 2 para 3 blocos livres. Na Vila Calada 150x150 (schematic) a área que ela alcança caiu cerca de 4%
     (só cantos cobertos de 2 de altura); as ruas e a praça seguem livres. Porta comum ela já não passava antes.
2. **Textura variante B** ligada: cada criatura escolhe A ou B pelo UUID (metade de cada), sempre a mesma, sem
   dado salvo novo. O brilho do Retalhado vale para as duas.
3. **Animações novas** ligadas pelos controles do GeckoLib, sem mudar IDs:
   - `ataque` (Retalhado) e `mordida` (Sanguessuga): no golpe corpo a corpo (a investida segue com a dela).
   - `dano`: quando apanha e continua viva.
   - `afiar` (Retalhado) e `chocar` (Sanguessuga), a **presença**: às vezes, junto do som ambiente, só quando está
     parada e sem alvo; para se ela achar alvo ou começar a andar.
   - `morrer`: o corpo fica no chão o tempo da animação (2,6 s e 2,8 s; o vanilla é 1 s) e não tomba de lado.
     O drop continua caindo na hora da morte.
