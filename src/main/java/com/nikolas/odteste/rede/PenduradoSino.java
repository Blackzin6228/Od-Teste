package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.vilacalada.IdsVilaCalada;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → um jogador: "o Pendurado aparece pendurado deste bloco" (empurrão do mestre no Dobre: em cima do sino
 * rachado, uma vez, para quem do grupo está na vila). Só desenho no cliente; nada salvo. Diferente do Pendurado do
 * clima, este não some quando se olha direto: some com o tempo ou quando se chega perto.
 *
 * @param teto o bloco de onde a corrente desce
 */
public record PenduradoSino(BlockPos teto) implements CustomPacketPayload {
    public static final Type<PenduradoSino> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsVilaCalada.PACOTE_PENDURADO_SINO));

    public static final StreamCodec<ByteBuf, PenduradoSino> CODEC =
            BlockPos.STREAM_CODEC.map(PenduradoSino::new, PenduradoSino::teto);

    private static volatile Consumer<PenduradoSino> receptor = m -> {};

    public static void definirReceptor(Consumer<PenduradoSino> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<PenduradoSino> type() {
        return TIPO;
    }

    public static void receber(PenduradoSino m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
