package com.nikolas.odteste.sinais;

import com.nikolas.odteste.OdMod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * Ponto de encontro entre quem emite sinal (conteúdo da história, mesa Dev) e quem lê (Analisador de Sinal).
 *
 * Dois jeitos de emitir:
 * <ul>
 *   <li><b>Ponto no mundo:</b> {@link SinaisNoMundo#marcar} (pronto, guarda em memória) ou uma
 *       {@link FonteDeSinal} própria (quando o conteúdo já guarda os pontos no seu SavedData).</li>
 *   <li><b>Fonte fixa salva no mundo:</b> {@link #registrarFonteFixa} (sobrevive a reinício; o Analisador
 *       aponta a direção de longe e decifra de perto).</li>
 *   <li><b>Item:</b> um {@link LeitorDeItem} que reconhece o item.</li>
 * </ul>
 * Erro numa fonte ou num leitor vira log, nunca crash.
 */
public final class Sinais {
    private static final List<FonteDeSinal> FONTES = new CopyOnWriteArrayList<>();
    private static final List<LeitorDeItem> LEITORES = new CopyOnWriteArrayList<>();

    static {
        FONTES.add(SinaisNoMundo::perto);
        FONTES.add((nivel, centro, raio) -> FontesFixas.de(nivel.getServer()).perto(nivel, centro, raio));
        FONTES.add(FonteVilaCalada::perto);
    }

    private Sinais() {}

    /** Resposta de {@link #registrarFonteFixa}. */
    public enum Resultado { OK, NOME_INVALIDO, TIPO_DESCONHECIDO, CHEIO }

    /**
     * Avisa que existe um tipo de sinal novo (com textos {@code odteste.sinal.<tipo>.decifrado} / {@code .camada}
     * no lang). Sem isso, fonte fixa desse tipo é recusada. {@code false} se o nome é inválido ou já existia.
     */
    public static boolean registrarTipo(String tipo) {
        return RegrasSinal.acrescentarTipo(tipo);
    }

    // ---------- fontes fixas (salvas no mundo, captadas de longe) ----------

    /**
     * Faz {@code pos} emitir um sinal que fica salvo no mundo e que o Analisador capta de longe (só a direção) e
     * decifra de perto. {@code id}: nome único da fonte (ex.: {@code sino_vila_calada}); registrar o mesmo id de
     * novo troca posição e tipo. {@code tipo}: textos {@code odteste.sinal.<tipo>.decifrado} e {@code .camada}.
     * Id e tipo: letras minúsculas, números e _, até 32. Só servidor.
     */
    public static Resultado registrarFonteFixa(ServerLevel nivel, String id, BlockPos pos, String tipo) {
        return registrarFonteFixa(nivel, id, pos, tipo, true);
    }

    /** Igual, escolhendo se o sinal tem segunda camada. */
    public static Resultado registrarFonteFixa(ServerLevel nivel, String id, BlockPos pos, String tipo,
                                               boolean comCamada) {
        return FontesFixas.de(nivel.getServer()).colocar(
                new FontesFixas.Fonte(id, nivel.dimension(), pos.immutable(), tipo, comCamada));
    }

    /** Para de emitir. {@code false} se não existia. */
    public static boolean removerFonteFixa(MinecraftServer servidor, String id) {
        return FontesFixas.de(servidor).tirar(id);
    }

    /** Todas as fontes fixas (só leitura). */
    public static Collection<FontesFixas.Fonte> fontesFixas(MinecraftServer servidor) {
        return FontesFixas.de(servidor).todas();
    }

    /**
     * A fonte fixa mais perto a até {@code alcance} blocos, na mesma dimensão. Conta as salvas e a Vila Calada
     * (que vem da âncora da história, ver {@link FonteVilaCalada}).
     */
    public static Optional<FontesFixas.Fonte> fonteFixaMaisProxima(ServerLevel nivel, BlockPos centro, int alcance) {
        Optional<FontesFixas.Fonte> salva;
        try {
            salva = FontesFixas.de(nivel.getServer()).maisProxima(nivel, centro, alcance);
        } catch (RuntimeException erro) {
            OdMod.LOGGER.error("Fontes fixas falharam em {}; ignoradas", centro, erro);
            salva = Optional.empty();
        }
        Optional<FontesFixas.Fonte> vila;
        try {
            vila = FonteVilaCalada.aoAlcance(nivel, centro, alcance);
        } catch (RuntimeException erro) {
            OdMod.LOGGER.error("Sinal da Vila Calada falhou em {}; ignorado", centro, erro);
            vila = Optional.empty();
        }
        if (vila.isEmpty()) return salva;
        double d2Salva = salva.map(f -> f.pos().distSqr(centro)).orElse(Double.MAX_VALUE);
        return RegrasFonteVila.vilaGanha(salva.isPresent(), d2Salva, vila.get().pos().distSqr(centro)) ? vila : salva;
    }

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
