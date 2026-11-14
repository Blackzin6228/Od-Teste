package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.cenas.IdsCena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Servidor → jogador: para a cena que estiver tocando (as duas: a de código e a em JSON) e devolve tudo. */
public record PararCena() implements CustomPacketPayload {
    public static final PararCena INSTANCIA = new PararCena();

    public static final Type<PararCena> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsCena.PACOTE_PARAR));

    public static final StreamCodec<ByteBuf, PararCena> CODEC = StreamCodec.unit(INSTANCIA);

    private static volatile Runnable receptor = () -> {};

    public static void definirReceptor(Runnable r) {
        receptor = r == null ? () -> {} : r;
    }

    @Override
    public Type<PararCena> type() {
        return TIPO;
    }

    public static void receber(PararCena m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.run());
    }
}
