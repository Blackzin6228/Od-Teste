package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → quem está vendo o jogador (e ele mesmo): qual Marca ele tem e em que estágio.
 * Só elemento e estágio (a Marca é visível no corpo); Lucidez e Exposição **não** vão para os outros.
 * Mandado quando a Marca muda e quando alguém começa a ver o jogador. Nada por tick.
 */
public record MarcaVisivel(int jogador, String elemento, int estagio) implements CustomPacketPayload {

    public static final Type<MarcaVisivel> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.PACOTE_MARCA_VISIVEL));

    public static final StreamCodec<ByteBuf, MarcaVisivel> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MarcaVisivel::jogador,
            ByteBufCodecs.stringUtf8(32), MarcaVisivel::elemento,
            ByteBufCodecs.VAR_INT, MarcaVisivel::estagio,
            MarcaVisivel::new);

    @Override
    public Type<MarcaVisivel> type() {
        return TIPO;
    }

    public static MarcaVisivel de(Player p) {
        return new MarcaVisivel(p.getId(), DadosJogador.marcaElemento(p).id(), DadosJogador.marcaEstagio(p));
    }

    /** Para o próprio jogador e todos que estão vendo ele. */
    public static void espalhar(ServerPlayer p) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p, de(p));
    }

    /** Roda no cliente: grava a Marca na entidade daquele jogador (só para desenhar). */
    public static void receber(MarcaVisivel m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> {
            Entity e = contexto.player().level().getEntity(m.jogador());
            if (e instanceof Player outro) {
                DadosJogador.aplicarMarcaVisivel(outro, Elemento.deIdOuNenhum(m.elemento()), m.estagio());
            }
        });
    }
}
