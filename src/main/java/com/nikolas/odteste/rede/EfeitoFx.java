package com.nikolas.odteste.rede;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servidor → só o jogador alvo: "toque o efeito de tela X" (comando {@code /odfx}, teste ou mestre).
 * Mandado só quando alguém digita o comando. Nada salvo.
 *
 * @param efeito   carmesim, sangue, marcado, coracao ou cor_horror (RegrasFxSangue.EFEITOS); outro nome é ignorado
 * @param segundos quanto tempo (0 = padrão)
 * @param gravar   salvar os quadros em screenshots/odteste/ no PC do jogador
 */
public record EfeitoFx(String efeito, int segundos, boolean gravar) implements CustomPacketPayload {

    public static final Type<EfeitoFx> TIPO =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.PACOTE_EFEITO_FX));

    public static final StreamCodec<ByteBuf, EfeitoFx> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), EfeitoFx::efeito,
            ByteBufCodecs.VAR_INT, EfeitoFx::segundos,
            ByteBufCodecs.BOOL, EfeitoFx::gravar,
            EfeitoFx::new);

    /** Quem trata no cliente (cliente/ troca ao carregar). No servidor não faz nada e nada de cliente carrega. */
    private static volatile Consumer<EfeitoFx> receptor = m -> {};

    public static void definirReceptor(Consumer<EfeitoFx> r) {
        receptor = r == null ? m -> {} : r;
    }

    @Override
    public Type<EfeitoFx> type() {
        return TIPO;
    }

    public static void receber(EfeitoFx m, IPayloadContext contexto) {
        contexto.enqueueWork(() -> receptor.accept(m));
    }
}
