package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.cenas.IdsCena;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → só o próprio jogador: "toque a cena X com esta âncora". Mandado uma vez, quando a cena começa.
 * O cliente faz o resto (câmera, preto, legenda, sons) e devolve tudo sozinho no fim.
 *
 * @param cena id da cena ({@code cenas/CenasConhecidas}); id que o cliente não conhece é ignorado
 * @param x    âncora no mundo
 * @param giro para onde a âncora "olha" (graus, jeito do Minecraft: 0 = sul)
 */
public record TocarCena(String cena, double x, double y, double z, float giro) implements CustomPacketPayload {

    public static final Type<TocarCena> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsCena.PACOTE_TOCAR));

    public static final StreamCodec<ByteBuf, TocarCena> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), TocarCena::cena,
            ByteBufCodecs.DOUBLE, TocarCena::x,
            ByteBufCodecs.DOUBLE, TocarCena::y,
            ByteBufCodecs.DOUBLE, TocarCena::z,
            ByteBufCodecs.FLOAT, TocarCena::giro,
            TocarCena::new);

    /** Quem toca no cliente. O código de cliente troca isto ao carregar; no servidor fica o padrão (nada). */
    private static volatile Consumer<TocarCena> receptor = m -> {};

    public static void definirReceptor(Consumer<TocarCena> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<TocarCena> type() {
        return TIPO;
    }

    public static void receber(TocarCena m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
