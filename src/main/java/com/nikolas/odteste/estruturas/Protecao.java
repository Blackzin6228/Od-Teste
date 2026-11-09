package com.nikolas.odteste.estruturas;

import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.compat.ftbchunks.ClaimsFtbChunks;
import net.minecraft.server.level.ServerLevel;

/**
 * Proteção de outros mods (claims). Cada mod de claim entra aqui atrás do seu {@code ligada()}:
 * se o mod sair do pack, a checagem só some e o resto continua igual.
 */
public final class Protecao {
    private Protecao() {}

    /** O chunk tem dono (claim)? */
    public static boolean temDono(ServerLevel nivel, int chunkX, int chunkZ) {
        if (Compat.Integracao.FTB_CHUNKS.ligada()) {
            return ClaimsFtbChunks.temDono(nivel.dimension(), chunkX, chunkZ);
        }
        return false;
    }
}
