package com.nikolas.odteste.historia.livros;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.LecternMenu;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Livros de história com páginas por condição. Não registra item: é um {@code minecraft:written_book} com
 * {@code custom_data {"odteste:livro": "<id>"}}. O texto vem de {@code data/odteste/odteste_livros/<id>.json}
 * (a Lore troca o texto sem mexer em código; um datapack também pode trocar).
 *
 * <p>O item guarda sempre a <b>versão neutra</b> (só o que todo mundo lê, sem nome de ninguém). Toda vez que alguém
 * abre (na mão ou no atril), o mod monta a versão de quem lê, com a Exposição e o grupo dele, e manda <b>só para a
 * tela dele</b>. Assim o mesmo livro, passado de mão em mão, mostra coisas diferentes e não vaza o que outro viu.
 * Só no servidor. Limite: um mod que abra o livro por outro caminho mostra a versão neutra.
 *
 * <p>Marcadores: {jogador} quem lê · {grupo} os nomes do grupo de quem lê · {marcado} (e {anfitriao}, nome antigo)
 * quem tem mais Exposição entre os do grupo que estão online.
 */
@EventBusSubscriber(modid = OdMod.MODID)
public final class LivrosHistoria {
    /** Chave do custom_data que marca o livro vanilla como livro de história. Salva no item: nunca muda. */
    public static final String CHAVE = OdMod.MODID + ":livro";
    /** Pasta dos arquivos de dados (data/<modid>/odteste_livros/). */
    public static final String PASTA = "odteste_livros";

    private static final Gson GSON = new Gson();
    private static volatile Map<String, Livro> livros = Map.of();

    private LivrosHistoria() {}

    /** Ids dos livros carregados (para sugestão do comando). */
    public static List<String> ids() {
        return new ArrayList<>(livros.keySet());
    }

    public static Optional<Livro> livro(String id) {
        return Optional.ofNullable(livros.get(id));
    }

    /** O id do livro de história deste item, ou vazio se não for um. */
    public static Optional<String> idDe(ItemStack item) {
        if (item.isEmpty() || !item.is(Items.WRITTEN_BOOK)) return Optional.empty();
        CustomData cd = item.get(DataComponents.CUSTOM_DATA);
        if (cd == null || !cd.contains(CHAVE)) return Optional.empty();
        String id = cd.copyTag().getString(CHAVE);
        return id.isEmpty() ? Optional.empty() : Optional.of(id);
    }

    /**
     * Valor dos marcadores na versão neutra (a que fica guardada no item). Não é o nome de ninguém: assim o livro
     * passado de mão em mão não carrega o nome nem o grupo de quem leu antes.
     */
    private static final String NINGUEM = "alguém";
    private static final Map<String, String> MARCADORES_NEUTROS =
            Map.of("jogador", NINGUEM, "grupo", NINGUEM, "marcado", NINGUEM, "anfitriao", NINGUEM);

    /** Um livro novo, com a versão neutra. Quem abre vê a versão dele (montada só para a tela). Vazio se o id não existe. */
    public static Optional<ItemStack> criar(String id, ServerPlayer leitor) {
        Livro l = livros.get(id);
        if (l == null) return Optional.empty();
        ItemStack item = new ItemStack(Items.WRITTEN_BOOK);
        CustomData.update(DataComponents.CUSTOM_DATA, item, t -> t.putString(CHAVE, id));
        montarNeutro(item, l);
        return Optional.of(item);
    }

    /** Remonta as páginas do item para este leitor. Não faz nada se o item não for um livro nosso conhecido. */
    public static boolean montar(ItemStack item, ServerPlayer leitor) {
        Optional<Livro> l = idDe(item).map(livros::get);
        if (l.isEmpty()) return false;
        montar(item, l.get(), leitor);
        return true;
    }

    private static void montar(ItemStack item, Livro l, ServerPlayer leitor) {
        escrever(item, l, DadosJogador.exposicao(leitor), marcadores(leitor));
    }

    /** A versão que fica guardada no item: só o que todo mundo lê (Exposição 0), sem nome de ninguém. */
    private static void montarNeutro(ItemStack item, Livro l) {
        escrever(item, l, 0, MARCADORES_NEUTROS);
    }

    private static void escrever(ItemStack item, Livro l, int exposicao, Map<String, String> marcadores) {
        List<Filterable<Component>> paginas = new ArrayList<>();
        for (List<RegrasLivros.Pedaco> pedacos : RegrasLivros.montar(l, exposicao, marcadores, LeitorLivros.MAX_PAGINA)) {
            MutableComponent pagina = Component.empty();
            for (int i = 0; i < pedacos.size(); i++) {
                RegrasLivros.Pedaco p = pedacos.get(i);
                String texto = (i == 0 ? "" : "\n\n") + p.texto();
                MutableComponent c = Component.literal(texto);
                if (p.escondido()) c.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);
                pagina.append(c);
            }
            paginas.add(Filterable.passThrough(pagina));
        }
        item.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(Filterable.passThrough(l.titulo()), l.autor(), l.geracao(), paginas, true));
    }

    private static Map<String, String> marcadores(ServerPlayer leitor) {
        String eu = leitor.getGameProfile().getName();
        Optional<Grupos.Grupo> g = DadosHistoria.de(leitor.server).grupos.grupoDe(leitor.getUUID());
        String grupo = g.map(x -> RegrasLivros.juntarNomes(x.nomes())).filter(s -> !s.isEmpty()).orElse(eu);
        // Exposição só de quem está online (a de quem saiu não está carregada). Grupo pequeno: laço curto.
        Map<String, Integer> expo = new LinkedHashMap<>();
        if (g.isPresent()) {
            for (UUID u : g.get().membros().keySet()) {
                ServerPlayer p = leitor.server.getPlayerList().getPlayer(u);
                if (p != null) expo.put(p.getGameProfile().getName(), DadosJogador.exposicao(p));
            }
        } else {
            expo.put(eu, DadosJogador.exposicao(leitor));
        }
        String marcado = RegrasLivros.marcado(expo, eu);
        Map<String, String> m = new HashMap<>();
        m.put("jogador", eu);
        m.put("grupo", grupo);
        m.put("marcado", marcado);
        m.put("anfitriao", marcado);
        return m;
    }

    /** Dispara o gancho de primeira leitura (uma vez por jogador e livro). */
    private static void marcarLido(ServerPlayer p, String id) {
        if (LivrosLidos.de(p.server).marcar(p.getUUID(), id)) {
            NeoForge.EVENT_BUS.post(new LivroAbertoPrimeiraVez(p, id));
        }
    }

    // ------------------------------------------------------------------ eventos

    /**
     * Na mão: o mod abre o livro no lugar do vanilla. Põe a versão de quem lê no item, manda para o cliente
     * dele, abre a tela e volta o item para a versão neutra. A tela do livro guarda as páginas que recebeu ao
     * abrir, então a volta (que chega no tick seguinte) não muda o que está na tela. Os outros jogadores não
     * recebem a versão de quem lê: o item já está neutro de novo antes da troca de equipamento ser enviada.
     */
    @SubscribeEvent
    public static void usarItem(PlayerInteractEvent.RightClickItem e) {
        // O clique dispara uma vez por mão. Conta a principal; a outra mão só se a principal não tem livro
        // (aí seria o livro da outra mão que abre). Assim nunca abre dois livros no mesmo clique.
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (e.getHand() != InteractionHand.MAIN_HAND && p.getMainHandItem().is(Items.WRITTEN_BOOK)) return;
        ItemStack item = e.getItemStack();
        Optional<String> id = idDe(item);
        Livro l = id.map(livros::get).orElse(null);
        if (l == null) return; // livro desconhecido: o vanilla abre o que estiver salvo
        montar(item, l, p);
        p.containerMenu.broadcastChanges(); // o cliente recebe a versão dele ANTES do pedido de abrir
        p.connection.send(new ClientboundOpenBookPacket(e.getHand()));
        p.awardStat(Stats.ITEM_USED.get(Items.WRITTEN_BOOK));
        montarNeutro(item, l);
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);
        marcarLido(p, id.get());
    }

    /** No atril: antes de abrir, o livro guardado no atril vira a versão neutra (quem tirar o livro leva essa). */
    @SubscribeEvent
    public static void usarBloco(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() != InteractionHand.MAIN_HAND || !(e.getEntity() instanceof ServerPlayer)) return;
        if (!(e.getLevel().getBlockEntity(e.getPos()) instanceof LecternBlockEntity atril) || !atril.hasBook()) return;
        ItemStack livro = atril.getBook();
        Livro l = idDe(livro).map(livros::get).orElse(null);
        if (l == null) return;
        montarNeutro(livro, l);
        atril.setChanged();
    }

    /**
     * Atril aberto: manda só para a tela de quem abriu uma cópia com a versão dele. O livro do atril continua
     * neutro no servidor, então dois jogadores lendo o mesmo atril veem cada um a sua versão. O servidor acha
     * que o cliente tem a neutra e não reenvia nada enquanto o livro não muda.
     */
    @SubscribeEvent
    public static void abrirAtril(PlayerContainerEvent.Open e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !(e.getContainer() instanceof LecternMenu menu)) return;
        ItemStack livro = menu.getBook();
        Optional<String> id = idDe(livro);
        Livro l = id.map(livros::get).orElse(null);
        if (l == null) return;
        ItemStack copia = livro.copy();
        montar(copia, l, p);
        p.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, copia));
        marcarLido(p, id.get());
    }

    @SubscribeEvent
    public static void recarregar(AddReloadListenerEvent e) {
        e.addListener(new SimpleJsonResourceReloadListener(GSON, PASTA) {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> arquivos, ResourceManager rm, ProfilerFiller perfil) {
                Map<String, Livro> novos = new TreeMap<>();
                arquivos.forEach((rl, json) -> {
                    // Só os do nosso modid: o id salvo no item é o nome do arquivo.
                    if (!OdMod.MODID.equals(rl.getNamespace())) return;
                    try {
                        novos.put(rl.getPath(), LeitorLivros.ler(rl.getPath(), json, OdMod.LOGGER::warn));
                    } catch (RuntimeException erro) {
                        OdMod.LOGGER.error("Livro de história {} com erro: {}", rl, erro.getMessage());
                    }
                });
                livros = Collections.unmodifiableMap(novos);
                OdMod.LOGGER.info("{} livro(s) de história carregado(s): {}", novos.size(), novos.keySet());
            }
        });
    }
}
