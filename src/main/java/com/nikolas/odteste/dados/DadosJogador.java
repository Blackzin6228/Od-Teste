package com.nikolas.odteste.dados;

import com.mojang.serialization.Codec;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.rede.SincronizarJogador;
import com.nikolas.odteste.registro.Registro;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Dados dos sistemas do jogador (Exposição, Lucidez, Marca, Transcender), salvos no próprio jogador.
 * Todos usam copyOnDeath: morrer não apaga nada.
 *
 * Regra: ninguém mexe nos attachments direto. Use os métodos daqui, que prendem o valor na faixa
 * (Limites) e avisam o cliente do dono.
 */
public final class DadosJogador {

    public static final Supplier<AttachmentType<Integer>> EXPOSICAO =
            inteiro(IdsDados.EXPOSICAO, 0, Limites::exposicao);
    /** A faixa da Lucidez depende da máxima; ela é presa na leitura e na escrita. */
    public static final Supplier<AttachmentType<Integer>> LUCIDEZ =
            inteiro(IdsDados.LUCIDEZ, Limites.LUCIDEZ_MAX_PADRAO, v -> Math.max(0, v));
    public static final Supplier<AttachmentType<Integer>> LUCIDEZ_MAX =
            inteiro(IdsDados.LUCIDEZ_MAX, Limites.LUCIDEZ_MAX_PADRAO, Limites::lucidezMax);
    public static final Supplier<AttachmentType<Elemento>> MARCA_ELEMENTO = Registro.DADOS.register(
            IdsDados.MARCA_ELEMENTO,
            () -> AttachmentType.builder(() -> Elemento.NENHUM)
                    .serialize(Codec.STRING.xmap(Elemento::deIdOuNenhum, Elemento::id))
                    .copyOnDeath()
                    .build());
    public static final Supplier<AttachmentType<Integer>> MARCA_ESTAGIO =
            inteiro(IdsDados.MARCA_ESTAGIO, 0, v -> Limites.prender(v, 0, Limites.ESTAGIO_MAX));
    public static final Supplier<AttachmentType<Integer>> TRANSCENDER_NIVEL =
            inteiro(IdsDados.TRANSCENDER_NIVEL, 0, Limites::transcender);

    private DadosJogador() {}

    /** Força o carregamento da classe, para os registros entrarem antes do evento de registro. */
    public static void iniciar() {}

    private static Supplier<AttachmentType<Integer>> inteiro(String id, int padrao, UnaryOperator<Integer> prender) {
        // O codec prende o valor ao ler do disco: um arquivo editado à mão não quebra o jogador.
        Codec<Integer> codec = Codec.INT.xmap(prender, v -> v);
        return Registro.DADOS.register(id, () -> AttachmentType.builder(() -> padrao)
                .serialize(codec)
                .copyOnDeath()
                .build());
    }

    // ---------- leitura ----------

    public static int exposicao(Player p) {
        return Limites.exposicao(p.getData(EXPOSICAO));
    }

    public static int lucidezMax(Player p) {
        return Limites.lucidezMax(p.getData(LUCIDEZ_MAX));
    }

    public static int lucidez(Player p) {
        return Limites.lucidez(p.getData(LUCIDEZ), lucidezMax(p));
    }

    public static Elemento marcaElemento(Player p) {
        return p.getData(MARCA_ELEMENTO);
    }

    public static int marcaEstagio(Player p) {
        return Limites.estagioMarca(marcaElemento(p), p.getData(MARCA_ESTAGIO));
    }

    public static int transcender(Player p) {
        return Limites.transcender(p.getData(TRANSCENDER_NIVEL));
    }

    // ---------- escrita (sempre presa na faixa e sincronizada) ----------

    public static void definirExposicao(Player p, int valor) {
        p.setData(EXPOSICAO, Limites.exposicao(valor));
        sincronizar(p);
    }

    public static void definirLucidez(Player p, int valor) {
        p.setData(LUCIDEZ, Limites.lucidez(valor, lucidezMax(p)));
        sincronizar(p);
    }

    /** Baixar a máxima também baixa a Lucidez atual, se ela passar do novo teto. */
    public static void definirLucidezMax(Player p, int valor) {
        int max = Limites.lucidezMax(valor);
        p.setData(LUCIDEZ_MAX, max);
        p.setData(LUCIDEZ, Limites.lucidez(p.getData(LUCIDEZ), max));
        sincronizar(p);
    }

    public static void definirMarca(Player p, Elemento elemento, int estagio) {
        Elemento e = elemento == null ? Elemento.NENHUM : elemento;
        p.setData(MARCA_ELEMENTO, e);
        p.setData(MARCA_ESTAGIO, Limites.estagioMarca(e, estagio));
        sincronizar(p);
    }

    public static void definirTranscender(Player p, int nivel) {
        p.setData(TRANSCENDER_NIVEL, Limites.transcender(nivel));
        sincronizar(p);
    }

    // ---------- rede ----------

    /** Manda os valores só para o próprio jogador (os outros não precisam saber). No cliente, não faz nada. */
    public static void sincronizar(Player p) {
        if (p instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new SincronizarJogador(
                    exposicao(p), lucidez(p), lucidezMax(p),
                    marcaElemento(p).id(), marcaEstagio(p), transcender(p)));
        }
    }

    /** Usado pelo pacote no lado do cliente: grava a cópia que o servidor mandou. */
    public static void aplicarCopia(Player p, SincronizarJogador d) {
        p.setData(EXPOSICAO, Limites.exposicao(d.exposicao()));
        p.setData(LUCIDEZ_MAX, Limites.lucidezMax(d.lucidezMax()));
        p.setData(LUCIDEZ, Limites.lucidez(d.lucidez(), d.lucidezMax()));
        Elemento e = Elemento.deIdOuNenhum(d.marcaElemento());
        p.setData(MARCA_ELEMENTO, e);
        p.setData(MARCA_ESTAGIO, Limites.estagioMarca(e, d.marcaEstagio()));
        p.setData(TRANSCENDER_NIVEL, Limites.transcender(d.transcender()));
    }
}
