package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.vilacalada.IdsVilaCalada;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → só o próprio jogador: "você está dentro do muro da Vila Calada" e "você é quem vê o Pendurado".
 * Mandado só quando muda (o servidor confere uma vez por segundo). O cliente faz a névoa, a cinza, os sons e o
 * Pendurado. O ponto da vila nunca vai para o cliente (um cliente modificado não acha a vila por aqui).
 *
 * @param dentro    dentro do muro da vila
 * @param pendurado é o jogador de maior Exposição do grupo (e está dentro)
 */
public record ClimaVila(boolean dentro, boolean pendurado) implements CustomPacketPayload {

    public static final Type<ClimaVila> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsVilaCalada.PACOTE_CLIMA));

    public static final StreamCodec<ByteBuf, ClimaVila> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClimaVila::dentro,
            ByteBufCodecs.BOOL, ClimaVila::pendurado,
            ClimaVila::new);

    /** Quem trata no cliente (cliente/ troca ao carregar). No servidor não faz nada e nada de cliente carrega. */
    private static volatile Consumer<ClimaVila> receptor = m -> {};

    public static void definirReceptor(Consumer<ClimaVila> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<ClimaVila> type() {
        return TIPO;
    }

    public static void receber(ClimaVila m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
