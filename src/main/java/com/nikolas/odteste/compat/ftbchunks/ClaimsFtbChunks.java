package com.nikolas.odteste.compat.ftbchunks;

import com.nikolas.odteste.OdMod;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Pergunta ao FTB Chunks se um chunk tem dono. Só é chamada atrás de {@code Compat.Integracao.FTB_CHUNKS.ligada()}.
 *
 * O jar do FTB Chunks não está na lista de compilação (ele não está no pack antigo que o build usa), então a
 * conversa é por reflexão, só com a API pública dele (1.21.1: {@code FTBChunksAPI.api().getManager().getChunk(ChunkDimPos)}).
 * Se a API mudar e a reflexão falhar, a resposta é "protegido" (na dúvida, não coloca) e o log avisa uma vez.
 */
public final class ClaimsFtbChunks {
    private static boolean pronto;
    private static boolean quebrado;
    private static Method api;
    private static Method managerCarregado;
    private static Method manager;
    private static Method chunk;
    private static Constructor<?> chunkDimPos;

    private ClaimsFtbChunks() {}

    private static synchronized boolean preparar() {
        if (pronto) return true;
        if (quebrado) return false;
        try {
            Class<?> apiClasse = Class.forName("dev.ftb.mods.ftbchunks.api.FTBChunksAPI");
            Class<?> apiInterface = Class.forName("dev.ftb.mods.ftbchunks.api.FTBChunksAPI$API");
            Class<?> managerClasse = Class.forName("dev.ftb.mods.ftbchunks.api.ClaimedChunkManager");
            Class<?> posClasse = Class.forName("dev.ftb.mods.ftblibrary.math.ChunkDimPos");
            api = apiClasse.getMethod("api");
            managerCarregado = apiInterface.getMethod("isManagerLoaded");
            manager = apiInterface.getMethod("getManager");
            chunk = managerClasse.getMethod("getChunk", posClasse);
            chunkDimPos = posClasse.getConstructor(ResourceKey.class, int.class, int.class);
            pronto = true;
            return true;
        } catch (ReflectiveOperationException | LinkageError e) {
            quebrado = true;
            OdMod.LOGGER.warn("FTB Chunks: não consegui ler os claims ({}). Por segurança, nada da história "
                    + "será colocado enquanto isso não for corrigido.", e.toString());
            return false;
        }
    }

    /**
     * @return true se o chunk tem dono, ou se não deu para conferir (na dúvida, protegido)
     */
    public static boolean temDono(ResourceKey<Level> dimensao, int chunkX, int chunkZ) {
        if (!preparar()) return true;
        try {
            Object a = api.invoke(null);
            if (!(Boolean) managerCarregado.invoke(a)) return true;
            Object m = manager.invoke(a);
            Object pos = chunkDimPos.newInstance(dimensao, chunkX, chunkZ);
            return chunk.invoke(m, pos) != null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            OdMod.LOGGER.debug("FTB Chunks: falha ao conferir claim", e);
            return true;
        }
    }
}
