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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
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
 * <p>O livro é <b>montado de novo toda vez que alguém abre</b> (na mão ou no atril), com a Exposição e o grupo de
 * quem lê. Assim o mesmo livro, passado de mão em mão, mostra coisas diferentes. Só no servidor.
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

    /** Um livro novo, já montado para quem vai receber. Vazio se o id não existe. */
    public static Optional<ItemStack> criar(String id, ServerPlayer leitor) {
        Livro l = livros.get(id);
        if (l == null) return Optional.empty();
        ItemStack item = new ItemStack(Items.WRITTEN_BOOK);
        CustomData.update(DataComponents.CUSTOM_DATA, item, t -> t.putString(CHAVE, id));
        montar(item, l, leitor);
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
        Map<String, String> marcadores = marcadores(leitor);
        List<Filterable<Component>> paginas = new ArrayList<>();
        for (List<RegrasLivros.Pedaco> pedacos : RegrasLivros.montar(l, DadosJogador.exposicao(leitor))) {
            MutableComponent pagina = Component.empty();
            for (int i = 0; i < pedacos.size(); i++) {
                RegrasLivros.Pedaco p = pedacos.get(i);
                String texto = (i == 0 ? "" : "\n\n") + RegrasLivros.substituir(p.texto(), marcadores);
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

    /** Monta o livro e dispara o gancho de primeira leitura. */
    private static void abrir(ServerPlayer p, ItemStack item) {
        Optional<String> id = idDe(item);
        if (id.isEmpty() || !montar(item, p)) return;
        if (LivrosLidos.de(p.server).marcar(p.getUUID(), id.get())) {
            NeoForge.EVENT_BUS.post(new LivroAbertoPrimeiraVez(p, id.get()));
        }
    }

    // ------------------------------------------------------------------ eventos

    /** Na mão: o evento vem antes do vanilla abrir o livro, então quem lê já vê a versão dele. */
    @SubscribeEvent
    public static void usarItem(PlayerInteractEvent.RightClickItem e) {
        if (e.getEntity() instanceof ServerPlayer p) abrir(p, e.getItemStack());
    }

    /** No atril: monta o livro do atril para quem vai ler (quem lê por último define o que está lá). */
    @SubscribeEvent
    public static void usarBloco(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (!(e.getLevel().getBlockEntity(e.getPos()) instanceof LecternBlockEntity atril) || !atril.hasBook()) return;
        ItemStack livro = atril.getBook();
        if (idDe(livro).isEmpty()) return;
        abrir(p, livro);
        atril.setChanged();
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
                        novos.put(rl.getPath(), LeitorLivros.ler(rl.getPath(), json));
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
