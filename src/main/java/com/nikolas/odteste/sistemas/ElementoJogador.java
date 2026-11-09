package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.elementos.RegrasElemento;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Elemento do jogador: o admin define, a história revela. Só servidor.
 *
 * API para o sistema de eventos (Ato 1 em diante):
 * - {@link #de(Player)} e {@link #revelado(Player)} para ler;
 * - {@link #escolhidos(Collection, Elemento)} para achar quem é de um elemento num grupo;
 * - {@link #marcadores(Collection)} para os textos: {escolhido_sangue}, {escolhido_conhecimento}...
 *
 * Só funciona com jogador online (o dado fica no próprio jogador). Antes de revelado, nada aqui manda o
 * elemento para o cliente: quem decide mostrar um nome num texto é o evento.
 */
public final class ElementoJogador {
    private ElementoJogador() {}

    public static Elemento de(Player p) {
        return DadosJogador.elemento(p);
    }

    public static boolean revelado(Player p) {
        return DadosJogador.elementoRevelado(p);
    }

    /** O jogador é deste elemento? (nenhum nunca é) */
    public static boolean eDo(Player p, Elemento e) {
        return RegrasElemento.afim(de(p), e);
    }

    /**
     * Admin define o elemento. Trocar de elemento (ou tirar) esconde de novo: a revelação era do elemento antigo.
     * Não avisa o jogador.
     */
    public static void definir(ServerPlayer p, Elemento e) {
        Elemento novo = e == null ? Elemento.NENHUM : e;
        boolean manter = novo == de(p) && revelado(p);
        DadosJogador.gravarElemento(p, novo, manter);
        Reacoes.conferirMarca(p);
    }

    /**
     * A história revela o elemento ao jogador: frase curta e som só para ele. Depois disso a Marca pode aparecer
     * (se ele já for Tocado).
     * @return false se ele não tem elemento
     */
    public static boolean revelar(ServerPlayer p) {
        Elemento e = de(p);
        if (e == Elemento.NENHUM) return false;
        boolean jaEra = revelado(p);
        DadosJogador.gravarElemento(p, e, true);
        if (!jaEra) {
            p.sendSystemMessage(Component.translatable("odteste.elemento.revelado",
                            Component.translatable(e.chaveTraducao()).withStyle(s -> s.withColor(e.cor()).withItalic(false)))
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            p.playNotifySound(SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 0.8f, 0.5f);
        }
        Reacoes.conferirMarca(p);
        return true;
    }

    /** Quem é deste elemento, na ordem da lista (revelado ou não: o evento decide). */
    public static List<ServerPlayer> escolhidos(Collection<ServerPlayer> jogadores, Elemento e) {
        List<ServerPlayer> l = new ArrayList<>();
        if (e == null || e == Elemento.NENHUM) return l;
        for (ServerPlayer p : jogadores) {
            if (eDo(p, e)) l.add(p);
        }
        return l;
    }

    /** Nomes em lista ("A", "A e B", "A, B e C"; o "e" vem da tradução de quem lê). Vazio se ninguém. */
    public static Component nomes(List<ServerPlayer> jogadores) {
        if (jogadores.isEmpty()) return Component.empty();
        if (jogadores.size() == 1) return jogadores.get(0).getDisplayName();
        StringBuilder inicio = new StringBuilder();
        for (int i = 0; i < jogadores.size() - 1; i++) {
            if (i > 0) inicio.append(", ");
            inicio.append(jogadores.get(i).getGameProfile().getName());
        }
        return Component.translatable("odteste.texto.lista", inicio.toString(),
                jogadores.get(jogadores.size() - 1).getGameProfile().getName());
    }

    /**
     * Marcadores para os textos dos eventos: "escolhido_sangue" → nome(s) de quem é de Sangue no grupo, e assim
     * para os 4 elementos. Elemento sem ninguém → texto vazio. Ex.: Component.translatable(chave, m.get("escolhido_sangue")).
     */
    public static Map<String, Component> marcadores(Collection<ServerPlayer> jogadores) {
        Map<String, Component> m = new LinkedHashMap<>();
        for (Elemento e : Elemento.values()) {
            if (e == Elemento.NENHUM) continue;
            m.put(RegrasElemento.marcador(e), nomes(escolhidos(jogadores, e)));
        }
        return m;
    }

    /** Custo inteiro (Lucidez, Exposição) já com o desconto do próprio elemento. */
    public static int custo(Player p, int base, Elemento doObjeto) {
        return RegrasElemento.custo(base, de(p), doObjeto);
    }

    /** Custo em vida já com o desconto do próprio elemento. */
    public static float custoVida(Player p, float base, Elemento doObjeto) {
        return RegrasElemento.custoVida(base, de(p), doObjeto);
    }
}
