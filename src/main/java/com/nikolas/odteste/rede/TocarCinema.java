package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.cenas.IdsCena;
import com.nikolas.odteste.cenas.LeitorRoteiro;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → o alvo da cena (e o mestre que assiste): "toque esta cena em JSON". Mandado uma vez, no começo.
 * O texto da cena vai inteiro (o datapack do mundo pode trocar a cena sem jar novo); o cliente lê de novo e toca.
 *
 * @param cena       id
 * @param json       a cena (docs/cenas.md)
 * @param ax         âncora no mundo (e {@code giro}, graus no jeito do Minecraft)
 * @param ox         olhos do alvo no começo (e para onde ele olha): o servidor segura ele parado ali
 * @param pular      pode pular segurando o pulo
 * @param espectador quem recebe só assiste (mestre): o servidor não segura ninguém, o cliente não avisa o fim
 */
public record TocarCinema(String cena, String json, double ax, double ay, double az, float giro, double ox, double oy,
        double oz, float oGiro, float oInclinacao, boolean pular, boolean espectador) implements CustomPacketPayload {

    public static final Type<TocarCinema> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsCena.PACOTE_CINEMA));

    private static final StreamCodec<ByteBuf, String> ID = ByteBufCodecs.stringUtf8(64);
    private static final StreamCodec<ByteBuf, String> TEXTO = ByteBufCodecs.stringUtf8(LeitorRoteiro.MAX_TEXTO);

    public static final StreamCodec<ByteBuf, TocarCinema> CODEC = StreamCodec.of(
            (b, m) -> {
                ID.encode(b, m.cena);
                TEXTO.encode(b, m.json);
                b.writeDouble(m.ax);
                b.writeDouble(m.ay);
                b.writeDouble(m.az);
                b.writeFloat(m.giro);
                b.writeDouble(m.ox);
                b.writeDouble(m.oy);
                b.writeDouble(m.oz);
                b.writeFloat(m.oGiro);
                b.writeFloat(m.oInclinacao);
                b.writeBoolean(m.pular);
                b.writeBoolean(m.espectador);
            },
            b -> new TocarCinema(ID.decode(b), TEXTO.decode(b), b.readDouble(), b.readDouble(), b.readDouble(),
                    b.readFloat(), b.readDouble(), b.readDouble(), b.readDouble(), b.readFloat(), b.readFloat(),
                    b.readBoolean(), b.readBoolean()));

    /** Quem toca no cliente (cliente/ troca ao carregar). No servidor fica o padrão (nada). */
    private static volatile Consumer<TocarCinema> receptor = m -> {};

    public static void definirReceptor(Consumer<TocarCinema> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<TocarCinema> type() {
        return TIPO;
    }

    public static void receber(TocarCinema m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
