package com.nikolas.odteste.dados;

import com.mojang.serialization.Codec;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.rede.MarcaVisivel;
import com.nikolas.odteste.rede.SincronizarJogador;
import com.nikolas.odteste.registro.Registro;
import com.nikolas.odteste.sistemas.Reacoes;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    /** Centésimos de Exposição guardados até completar um ponto. Não vai para o cliente. */
    public static final Supplier<AttachmentType<Integer>> EXPOSICAO_RESTO =
            inteiro(IdsDados.EXPOSICAO_RESTO, 0, v -> Limites.prender(v, 0, 99));
    /**
     * Contato secreto com cada elemento (id do elemento → centésimos). Não vai para o cliente.
     * Elemento desconhecido no arquivo é ignorado ao ler; valor negativo vira 0.
     */
    public static final Supplier<AttachmentType<Map<String, Integer>>> CONTATO_ELEMENTO = Registro.DADOS.register(
            IdsDados.CONTATO_ELEMENTO,
            () -> AttachmentType.<Map<String, Integer>>builder(() -> Map.of())
                    .serialize(Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(DadosJogador::limparContato, m -> m))
                    .copyOnDeath()
                    .build());
    /**
     * Freio de repetição da Exposição: fonte → [início da janela (tempo do mundo), vezes]. Não vai para o cliente.
     * Só guarda janelas abertas (10 min), então fica pequeno.
     */
    public static final Supplier<AttachmentType<Map<String, List<Long>>>> EXPOSICAO_REPETICAO = Registro.DADOS.register(
            IdsDados.EXPOSICAO_REPETICAO,
            () -> AttachmentType.<Map<String, List<Long>>>builder(() -> Map.of())
                    .serialize(Codec.unboundedMap(Codec.STRING, Codec.LONG.listOf()))
                    .copyOnDeath()
                    .build());

    /**
     * Último uso de cada Artefato Paranormal: id do item → tempo do mundo. Não vai para o cliente.
     * Salvo para a recarga não zerar ao sair e voltar. Só guarda usos recentes (ver RegrasArtefato.limpar).
     */
    public static final Supplier<AttachmentType<Map<String, Long>>> ARTEFATO_RECARGA = Registro.DADOS.register(
            IdsDados.ARTEFATO_RECARGA,
            () -> AttachmentType.<Map<String, Long>>builder(() -> Map.of())
                    .serialize(Codec.unboundedMap(Codec.STRING, Codec.LONG))
                    .copyOnDeath()
                    .build());

    /**
     * Noite (dia do relógio) em que a última criatura de Sangue nasceu sozinha perto do jogador. Não vai para o
     * cliente. Salvo para sair e voltar não liberar outra na mesma noite.
     */
    public static final Supplier<AttachmentType<Long>> CRIATURA_NOITE = Registro.DADOS.register(
            IdsDados.CRIATURA_NOITE,
            () -> AttachmentType.builder(() -> -1L)
                    .serialize(Codec.LONG)
                    .copyOnDeath()
                    .build());

    private DadosJogador() {}

    public static long criaturaNoite(Player p) {
        return p.getData(CRIATURA_NOITE);
    }

    public static void definirCriaturaNoite(Player p, long noite) {
        p.setData(CRIATURA_NOITE, noite);
    }

    /** Usos recentes de artefatos (id do item → tempo do mundo). */
    public static Map<String, Long> usosArtefato(Player p) {
        return p.getData(ARTEFATO_RECARGA);
    }

    public static void definirUsosArtefato(Player p, Map<String, Long> usos) {
        p.setData(ARTEFATO_RECARGA, Map.copyOf(usos));
    }

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

    public static int exposicaoResto(Player p) {
        return Limites.prender(p.getData(EXPOSICAO_RESTO), 0, 99);
    }

    /** Contato com cada elemento (sem NENHUM). */
    public static Map<Elemento, Integer> contato(Player p) {
        Map<Elemento, Integer> m = new EnumMap<>(Elemento.class);
        p.getData(CONTATO_ELEMENTO).forEach((id, v) -> Elemento.deId(id)
                .filter(e -> e != Elemento.NENHUM)
                .ifPresent(e -> m.put(e, Math.max(0, v))));
        return m;
    }

    /** Tira elementos desconhecidos e valores negativos (arquivo velho ou editado à mão). */
    static Map<String, Integer> limparContato(Map<String, Integer> lido) {
        Map<String, Integer> m = new HashMap<>();
        lido.forEach((id, v) -> Elemento.deId(id)
                .filter(e -> e != Elemento.NENHUM)
                .ifPresent(e -> m.put(e.id(), Math.max(0, v == null ? 0 : v))));
        return Map.copyOf(m);
    }

    // ---------- escrita (sempre presa na faixa e sincronizada) ----------

    public static void definirExposicao(Player p, int valor) {
        int antes = exposicao(p);
        p.setData(EXPOSICAO, Limites.exposicao(valor));
        sincronizar(p);
        if (p instanceof ServerPlayer sp) Reacoes.exposicaoMudou(sp, antes, exposicao(p));
    }

    /** Usado pelo ganho automático: grava a Exposição e o resto juntos. */
    public static void definirExposicaoComResto(Player p, int valor, int resto) {
        p.setData(EXPOSICAO_RESTO, Limites.prender(resto, 0, 99));
        if (Limites.exposicao(valor) != exposicao(p)) definirExposicao(p, valor);
    }

    /** Soma contato com um elemento (nunca desce, para em 1 milhão para não estourar). */
    public static void somarContato(Player p, Elemento e, int centesimos) {
        if (e == null || e == Elemento.NENHUM || centesimos <= 0) return;
        Map<String, Integer> m = new HashMap<>(p.getData(CONTATO_ELEMENTO));
        m.merge(e.id(), centesimos, (a, b) -> (int) Math.min(1_000_000L, (long) a + b));
        p.setData(CONTATO_ELEMENTO, Map.copyOf(m));
    }

    public static void definirLucidez(Player p, int valor) {
        int antes = lucidez(p);
        p.setData(LUCIDEZ, Limites.lucidez(valor, lucidezMax(p)));
        sincronizar(p);
        if (p instanceof ServerPlayer sp) Reacoes.lucidezMudou(sp, antes, lucidez(p));
    }

    /** Baixar a máxima também baixa a Lucidez atual, se ela passar do novo teto. */
    public static void definirLucidezMax(Player p, int valor) {
        int max = Limites.lucidezMax(valor);
        p.setData(LUCIDEZ_MAX, max);
        p.setData(LUCIDEZ, Limites.lucidez(p.getData(LUCIDEZ), max));
        sincronizar(p);
        if (p instanceof ServerPlayer sp) com.nikolas.odteste.sistemas.Lucidez.atualizarLentidao(sp);
    }

    public static void definirMarca(Player p, Elemento elemento, int estagio) {
        Elemento antes = marcaElemento(p);
        int estagioAntes = marcaEstagio(p);
        Elemento e = elemento == null ? Elemento.NENHUM : elemento;
        p.setData(MARCA_ELEMENTO, e);
        p.setData(MARCA_ESTAGIO, Limites.estagioMarca(e, estagio));
        sincronizar(p);
        if (p instanceof ServerPlayer sp) {
            MarcaVisivel.espalhar(sp);
            Reacoes.marcaMudou(sp, antes, estagioAntes, e, marcaEstagio(p));
        }
    }

    /** Só no cliente: grava a Marca de outro jogador (vinda de MarcaVisivel), para desenhar no corpo. */
    public static void aplicarMarcaVisivel(Player p, Elemento elemento, int estagio) {
        Elemento e = elemento == null ? Elemento.NENHUM : elemento;
        p.setData(MARCA_ELEMENTO, e);
        p.setData(MARCA_ESTAGIO, Limites.estagioMarca(e, estagio));
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
