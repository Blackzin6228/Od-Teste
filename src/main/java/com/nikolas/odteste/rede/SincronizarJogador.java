package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → cliente do próprio jogador: a cópia atual dos valores dele.
 * O cliente usa isso só para escolher efeitos (som, partícula, textura da armadura), nunca para mostrar número.
 */
public record SincronizarJogador(int exposicao, int lucidez, int lucidezMax,
                                 String marcaElemento, int marcaEstagio, int transcender)
        implements CustomPacketPayload {

    public static final Type<SincronizarJogador> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.PACOTE_SINCRONIZAR));

    public static final StreamCodec<ByteBuf, SincronizarJogador> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SincronizarJogador::exposicao,
            ByteBufCodecs.VAR_INT, SincronizarJogador::lucidez,
            ByteBufCodecs.VAR_INT, SincronizarJogador::lucidezMax,
            ByteBufCodecs.stringUtf8(32), SincronizarJogador::marcaElemento,
            ByteBufCodecs.VAR_INT, SincronizarJogador::marcaEstagio,
            ByteBufCodecs.VAR_INT, SincronizarJogador::transcender,
            SincronizarJogador::new);

    @Override
    public Type<SincronizarJogador> type() {
        return TIPO;
    }

    /** Roda no cliente. context.player() é o jogador local; não precisa de classe só de cliente. */
    public static void receber(SincronizarJogador dados, IPayloadContext contexto) {
        contexto.enqueueWork(() -> DadosJogador.aplicarCopia(contexto.player(), dados));
    }
}
