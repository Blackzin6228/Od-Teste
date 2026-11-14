package com.nikolas.odteste.sinais;

import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.vilacalada.DadosVilaCalada;
import com.nikolas.odteste.historia.vilacalada.IdsVilaCalada;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * A Vila Calada como fonte de sinal fixa do Analisador. Só <b>lê</b> a âncora {@code vila_calada}
 * (em {@code odteste_historia}), o liga/desliga das pistas (em {@code odteste_vila_calada}) e as fontes salvas;
 * não grava nada. Âncora ausente, desligada, em outra dimensão, ou com uma fonte salva do sino perto dela
 * ({@link RegrasFonteVila#duplicada}): sem sinal. Se o admin muda a âncora, o sinal muda junto.
 *
 * O sinal é texto e a luz do aparelho: este arquivo não toca som, e o aparelho não faz chiado sozinho por ele
 * ({@link RegrasFonteVila#silencioso}).
 */
final class FonteVilaCalada {
    private FonteVilaCalada() {}

    /** A fonte da vila para quem está em {@code nivel}, se ela emite agora. */
    static Optional<FontesFixas.Fonte> fonte(ServerLevel nivel) {
        MinecraftServer servidor = nivel.getServer();
        GlobalPos ancora = DadosHistoria.de(servidor).ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        boolean temAncora = ancora != null;
        boolean mesmaDimensao = temAncora && ancora.dimension().equals(nivel.dimension());
        // Só abre o arquivo da vila se a âncora existe (mundo sem Ato 1 não lê nada a mais).
        if (!temAncora || !RegrasFonteVila.emite(DadosVilaCalada.de(servidor).ligado, true, mesmaDimensao)) {
            return Optional.empty();
        }
        BlockPos a = ancora.pos();
        for (FontesFixas.Fonte salva : FontesFixas.de(servidor).todas()) {
            BlockPos s = salva.pos();
            if (salva.dimensao().equals(ancora.dimension()) && RegrasFonteVila.duplicada(salva.tipo(),
                    s.getX() - a.getX(), s.getY() - a.getY(), s.getZ() - a.getZ())) {
                return Optional.empty(); // a salva já fala pela vila
            }
        }
        return Optional.of(new FontesFixas.Fonte(RegrasFonteVila.ID_FONTE, ancora.dimension(), a,
                RegrasFonteVila.TIPO, RegrasFonteVila.COM_CAMADA));
    }

    /** O sinal da vila: textos do tipo {@code vila_calada}, id {@link RegrasFonteVila#ID_SINAL}. */
    static Sinal sinal() {
        String base = "odteste.sinal." + RegrasFonteVila.TIPO;
        return Sinal.de(RegrasFonteVila.ID_SINAL, Component.translatable(base + ".decifrado"),
                RegrasFonteVila.COM_CAMADA ? Component.translatable(base + ".camada") : null);
    }

    /** Para {@link Sinais#perto}: o sinal da vila, se está a até {@code raio} blocos (decifrável). */
    static List<Sinal> perto(ServerLevel nivel, BlockPos centro, int raio) {
        return fonte(nivel).filter(f -> dentro(f, centro, raio)).map(f -> List.of(sinal())).orElse(List.of());
    }

    /** A vila, se está a até {@code alcance} blocos (o aparelho aponta a direção de longe). */
    static Optional<FontesFixas.Fonte> aoAlcance(ServerLevel nivel, BlockPos centro, int alcance) {
        return fonte(nivel).filter(f -> dentro(f, centro, alcance));
    }

    private static boolean dentro(FontesFixas.Fonte f, BlockPos centro, int raio) {
        BlockPos p = f.pos();
        return RegrasFonteVila.dentro(p.getX() - centro.getX(), p.getY() - centro.getY(), p.getZ() - centro.getZ(), raio);
    }
}
