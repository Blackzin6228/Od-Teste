package com.nikolas.odteste.sinais;

import com.nikolas.odteste.OdMod;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * Ponto de encontro entre quem emite sinal (conteúdo da história, mesa Dev) e quem lê (Analisador de Sinal).
 *
 * Dois jeitos de emitir:
 * <ul>
 *   <li><b>Ponto no mundo:</b> {@link SinaisNoMundo#marcar} (pronto, guarda em memória) ou uma
 *       {@link FonteDeSinal} própria (quando o conteúdo já guarda os pontos no seu SavedData).</li>
 *   <li><b>Item:</b> um {@link LeitorDeItem} que reconhece o item.</li>
 * </ul>
 * Erro numa fonte ou num leitor vira log, nunca crash.
 */
public final class Sinais {
    private static final List<FonteDeSinal> FONTES = new CopyOnWriteArrayList<>();
    private static final List<LeitorDeItem> LEITORES = new CopyOnWriteArrayList<>();

    static {
        FONTES.add(SinaisNoMundo::perto);
    }

    private Sinais() {}

    public static void registrarFonte(FonteDeSinal fonte) {
        if (fonte != null) FONTES.add(fonte);
    }

    public static void registrarLeitor(LeitorDeItem leitor) {
        if (leitor != null) LEITORES.add(leitor);
    }

    /** Todos os sinais a até {@code raio} blocos (só servidor). */
    public static List<Sinal> perto(ServerLevel nivel, BlockPos centro, int raio) {
        List<Sinal> achados = new ArrayList<>();
        for (FonteDeSinal fonte : FONTES) {
            try {
                List<Sinal> desta = fonte.perto(nivel, centro, raio);
                if (desta != null) achados.addAll(desta);
            } catch (RuntimeException erro) {
                OdMod.LOGGER.error("Fonte de sinal falhou em {}; ignorada nesta leitura", centro, erro);
            }
        }
        return achados;
    }

    /** O sinal guardado no item, se algum leitor o reconhece (só servidor). */
    public static Optional<Sinal> doItem(ItemStack item) {
        if (item == null || item.isEmpty()) return Optional.empty();
        for (LeitorDeItem leitor : LEITORES) {
            try {
                Optional<Sinal> sinal = leitor.ler(item);
                if (sinal != null && sinal.isPresent()) return sinal;
            } catch (RuntimeException erro) {
                OdMod.LOGGER.error("Leitor de sinal falhou com {}; ignorado", item, erro);
            }
        }
        return Optional.empty();
    }
}
