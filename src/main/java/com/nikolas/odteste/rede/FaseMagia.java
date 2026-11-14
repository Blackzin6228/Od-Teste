package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.magias.IdsMagia;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.Utf8String;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → quem vê quem conjura (e ele mesmo): "a magia entrou nesta fase". Um pacote por fase, nunca por tick.
 * O cliente desenha tudo (partícula, som, mancha, tela, animação); o servidor só calcula alvo e dano.
 *
 * @param magia    id da magia (ex.: talho_rubro). Magia que o cliente não conhece é ignorada.
 * @param fase     0 = preparação, 1 = golpe (o rastro sai do golpe, no próprio cliente)
 * @param entidade id de quem conjura (no mundo do cliente)
 * @param x        posição de quem conjura no momento da fase (pés)
 * @param y        idem
 * @param z        idem
 * @param giro     para onde olha (graus, como o yRot do jogo)
 * @param semente  sorteio do desenho (todo mundo vê as mesmas gotas)
 */
public record FaseMagia(String magia, int fase, int entidade, double x, double y, double z, float giro, int semente)
        implements CustomPacketPayload {
    public static final int PREPARACAO = 0;
    public static final int GOLPE = 1;

    public static final Type<FaseMagia> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsMagia.PACOTE_FASE));

    /** Oito campos (o composite do vanilla vai até seis): escrito à mão, na mesma ordem nos dois lados. */
    public static final StreamCodec<ByteBuf, FaseMagia> CODEC = StreamCodec.of(
            (buf, f) -> {
                Utf8String.write(buf, f.magia(), 32);
                VarInt.write(buf, f.fase());
                VarInt.write(buf, f.entidade());
                buf.writeDouble(f.x());
                buf.writeDouble(f.y());
                buf.writeDouble(f.z());
                buf.writeFloat(f.giro());
                buf.writeInt(f.semente());
            },
            buf -> new FaseMagia(Utf8String.read(buf, 32), VarInt.read(buf), VarInt.read(buf),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readInt()));

    /** Quem trata no cliente (cliente/ troca ao carregar). No servidor fica sem fazer nada. */
    private static volatile Consumer<FaseMagia> receptor = f -> {};

    public static void definirReceptor(Consumer<FaseMagia> r) {
        receptor = r == null ? f -> {} : r;
    }

    @Override
    public Type<FaseMagia> type() {
        return TIPO;
    }

    public static void receber(FaseMagia f, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(f));
    }
}
