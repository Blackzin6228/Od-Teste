package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.horror.IdsHorror;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → só o próprio jogador: começa um susto grande ({@code tipo} = id de
 * {@link com.nikolas.odteste.horror.TipoSusto}). O cliente faz a tensão e o momento forte; ninguém mais vê ou ouve.
 */
public record Susto(String tipo) implements CustomPacketPayload {

    public static final Type<Susto> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsHorror.PACOTE_SUSTO));

    public static final StreamCodec<ByteBuf, Susto> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), Susto::tipo,
            Susto::new);

    private static volatile Consumer<Susto> receptor = m -> {};

    public static void definirReceptor(Consumer<Susto> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<Susto> type() {
        return TIPO;
    }

    public static void receber(Susto m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
