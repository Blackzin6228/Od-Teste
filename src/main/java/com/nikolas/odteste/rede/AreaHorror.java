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
 * Servidor → só o próprio jogador: em que área da história ele está ("" = nenhuma). Mandado só quando muda
 * (o servidor confere uma vez por segundo). A noite o cliente sabe sozinho. O ponto da área nunca vai para o cliente.
 */
public record AreaHorror(String area) implements CustomPacketPayload {

    public static final Type<AreaHorror> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsHorror.PACOTE_AREA));

    public static final StreamCodec<ByteBuf, AreaHorror> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), AreaHorror::area,
            AreaHorror::new);

    private static volatile Consumer<AreaHorror> receptor = m -> {};

    public static void definirReceptor(Consumer<AreaHorror> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<AreaHorror> type() {
        return TIPO;
    }

    public static void receber(AreaHorror m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
