package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import java.util.function.Consumer;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → clientes a até 32 blocos: "aconteceu um momento com este jogador".
 * Pacote curto, mandado só quando algo muda (nunca por tick). O cliente toca tela/som/partícula/animação.
 *
 * @param tipo           número do TipoMomento
 * @param jogador        id da entidade do jogador (no mundo do cliente)
 * @param elemento       id do elemento (Marca nova), ou "nenhum"
 * @param elementoAntigo id do elemento anterior (só em MARCA_TROCOU), ou "nenhum"
 * @param valor          degrau ou estágio, conforme o tipo
 * @param emCombate      tomou ou deu dano nos últimos 5 s: o cliente pula a animação que prende o corpo
 */
public record Momento(int tipo, int jogador, String elemento, String elementoAntigo, int valor, boolean emCombate)
        implements CustomPacketPayload {

    public static final Type<Momento> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.PACOTE_MOMENTO));

    public static final StreamCodec<ByteBuf, Momento> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Momento::tipo,
            ByteBufCodecs.VAR_INT, Momento::jogador,
            ByteBufCodecs.stringUtf8(32), Momento::elemento,
            ByteBufCodecs.stringUtf8(32), Momento::elementoAntigo,
            ByteBufCodecs.VAR_INT, Momento::valor,
            ByteBufCodecs.BOOL, Momento::emCombate,
            Momento::new);

    /**
     * Quem trata o momento no cliente. O código de cliente (cliente/) troca isto ao carregar.
     * No servidor fica o padrão (não faz nada), e nenhuma classe de cliente é carregada.
     */
    private static volatile Consumer<Momento> receptor = m -> {};

    public static void definirReceptor(Consumer<Momento> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<Momento> type() {
        return TIPO;
    }

    public static void receber(Momento m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
