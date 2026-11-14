package com.nikolas.odteste.sistemas;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * "A tech chama o paranormal": conta blocos com energia (qualquer mod que use a capacidade de energia do NeoForge)
 * perto de um ponto. Sem depender de mod nenhum.
 *
 * Leve: conta chunks inteiros que já estão carregados (nunca carrega chunk), olha no máximo
 * {@link #BLOCOS_POR_CHUNK} blocos com dados por chunk, para em {@link #TETO_CHUNK} máquinas por chunk e guarda a conta
 * de cada chunk por 1 minuto (jogadores perto usam a mesma conta). Só no servidor.
 */
public final class MaquinasPerto {
    public static final int BLOCOS_POR_CHUNK = 512;
    public static final int TETO_CHUNK = 64;
    public static final int TETO_TOTAL = 128;
    private static final long VALIDADE = 20L * 60;
    private static final int LIMPAR_ACIMA = 4096;
    /** Dimensão → chunk → [contagem, vale até]. */
    private static final Map<ResourceKey<Level>, Map<Long, long[]>> CACHE = new HashMap<>();

    private MaquinasPerto() {}

    public static int contar(ServerLevel nivel, BlockPos centro, int raio) {
        long agora = nivel.getGameTime();
        Map<Long, long[]> cache = CACHE.computeIfAbsent(nivel.dimension(), k -> new HashMap<>());
        if (cache.size() > LIMPAR_ACIMA) cache.values().removeIf(c -> agora >= c[1]);
        int total = 0;
        int cx0 = (centro.getX() - raio) >> 4;
        int cx1 = (centro.getX() + raio) >> 4;
        int cz0 = (centro.getZ() - raio) >> 4;
        int cz1 = (centro.getZ() + raio) >> 4;
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                long chave = ChunkPos.asLong(cx, cz);
                long[] c = cache.get(chave);
                if (c == null || agora >= c[1] || agora < c[1] - VALIDADE) {
                    LevelChunk chunk = nivel.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;
                    c = new long[] {contarChunk(nivel, chunk), agora + VALIDADE};
                    cache.put(chave, c);
                }
                total += (int) c[0];
                if (total >= TETO_TOTAL) return TETO_TOTAL;
            }
        }
        return total;
    }

    private static int contarChunk(ServerLevel nivel, LevelChunk chunk) {
        int n = 0;
        int vistos = 0;
        try {
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (++vistos > BLOCOS_POR_CHUNK) break;
                if (be.isRemoved()) continue;
                if (nivel.getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(), be.getBlockState(), be, null) != null
                        && ++n >= TETO_CHUNK) {
                    break;
                }
            }
        } catch (RuntimeException outroMod) {
            // Um mod com a capacidade quebrada não pode derrubar a contagem: fica o que deu para contar.
        }
        return n;
    }

    /** Ao desligar o servidor (mundo novo no mesmo jogo não herda a conta). */
    public static void limpar() {
        CACHE.clear();
    }
}
