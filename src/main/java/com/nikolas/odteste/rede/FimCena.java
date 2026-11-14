package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.cenas.IdsCena;
import com.nikolas.odteste.cenas.ProtecaoCena;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Jogador → servidor: "a cena {@code cena} acabou aqui" (terminou ou pulou). O servidor solta o jogador (volta a
 * andar e a tomar dano). Só solta a cena com esse nome; mandar à toa não dá nada (sem cena, ignora).
 */
public record FimCena(String cena) implements CustomPacketPayload {

    public static final Type<FimCena> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsCena.PACOTE_FIM));

    public static final StreamCodec<ByteBuf, FimCena> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), FimCena::cena,
            FimCena::new);

    @Override
    public Type<FimCena> type() {
        return TIPO;
    }

    public static void receber(FimCena m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> {
            if (contexto.player() instanceof ServerPlayer p) ProtecaoCena.soltar(p, m.cena());
        });
    }
}
