package com.nikolas.odteste.comandos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nikolas.odteste.estruturas.Backup;
import com.nikolas.odteste.estruturas.Colocacao;
import com.nikolas.odteste.estruturas.Espaco;
import com.nikolas.odteste.estruturas.Estruturas;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import com.nikolas.odteste.historia.IdsHistoria;
import com.nikolas.odteste.historia.PrimeiroSinal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Comandos da história, todos só de admin (permissão 2):
 *
 * /odteste grupo criar|adicionar|tirar|apagar|ver
 * /odteste evento primeiro_sinal <grupo> [ponto] · desfazer <grupo> · ver <grupo> · ancorar <lugar> <ponto> · desancorar <lugar>
 * /odteste estrutura prever|colocar <template> <ponto> [rotação] · desfazer <id> · lista
 * /odteste elemento <jogador> [elemento] · revelar <jogador> (ComandosElemento)
 *
 * Para o admin, número aparece (coordenadas, contagens). Para o jogador, nunca.
 */
public final class ComandosHistoria {
    private static final String GRUPO = "grupo";
    private static final String JOGADORES = "jogadores";
    private static final String PONTO = "ponto";
    private static final String LUGAR = "lugar";
    private static final String TEMPLATE = "template";
    private static final String ROTACAO = "rotacao";
    private static final List<String> ROTACOES = List.of("0", "90", "180", "270");
    /** prever antes de colocar: guarda a última prévia de cada admin por 5 min (só memória). */
    private static final long PREVIA_VALE_MS = 5 * 60 * 1000L;
    private static final Map<String, String> PREVIAS = new HashMap<>();
    private static final Map<String, Long> PREVIAS_QUANDO = new HashMap<>();

    private ComandosHistoria() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
    }

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(grupo())
                .then(evento())
                .then(estrutura())
                .then(ComandosElemento.elemento()));
    }

    private static DadosHistoria dados(CommandContext<CommandSourceStack> c) {
        return DadosHistoria.de(c.getSource().getServer());
    }

    private static int responder(CommandContext<CommandSourceStack> c, boolean ok, Component msg) {
        if (ok) {
            c.getSource().sendSuccess(() -> msg, true);
            return 1;
        }
        c.getSource().sendFailure(msg);
        return 0;
    }

    private static int erro(CommandContext<CommandSourceStack> c, String chave, Object... args) {
        return responder(c, false, Component.translatable(chave, args));
    }

    private static int ok(CommandContext<CommandSourceStack> c, String chave, Object... args) {
        return responder(c, true, Component.translatable(chave, args));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> argGrupo() {
        return Commands.argument(GRUPO, StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                        DadosHistoria.de(c.getSource().getServer()).grupos.todos().stream().map(Grupos.Grupo::nome), b));
    }

    // ------------------------------------------------------------------ grupo

    private static LiteralArgumentBuilder<CommandSourceStack> grupo() {
        return Commands.literal("grupo")
                .then(Commands.literal("criar").then(argGrupo()
                        .executes(c -> criar(c, false))
                        .then(Commands.argument(JOGADORES, EntityArgument.players()).executes(c -> criar(c, true)))))
                .then(Commands.literal("adicionar").then(argGrupo()
                        .then(Commands.argument(JOGADORES, EntityArgument.players()).executes(c -> adicionar(c)))))
                .then(Commands.literal("tirar").then(argGrupo()
                        .then(Commands.argument(JOGADORES, EntityArgument.players()).executes(ComandosHistoria::tirar))))
                .then(Commands.literal("apagar").then(argGrupo().executes(ComandosHistoria::apagar)))
                .then(Commands.literal("ver")
                        .executes(ComandosHistoria::listarGrupos)
                        .then(argGrupo().executes(ComandosHistoria::verGrupo)));
    }

    private static int criar(CommandContext<CommandSourceStack> c, boolean comJogadores) throws CommandSyntaxException {
        String nome = StringArgumentType.getString(c, GRUPO);
        DadosHistoria d = dados(c);
        if (Grupos.normalizar(nome).isEmpty()) return erro(c, "odteste.grupo.erro.nome", nome);
        if (!d.grupos.criar(nome)) return erro(c, "odteste.grupo.erro.existe", nome);
        d.setDirty();
        ok(c, "odteste.grupo.criado", Grupos.normalizar(nome).get());
        return comJogadores ? adicionar(c) : 1;
    }

    private static int adicionar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String nome = StringArgumentType.getString(c, GRUPO);
        DadosHistoria d = dados(c);
        if (d.grupos.grupo(nome).isEmpty()) return erro(c, "odteste.evento.erro.sem_grupo", nome);
        Collection<ServerPlayer> js = EntityArgument.getPlayers(c, JOGADORES);
        for (ServerPlayer p : js) {
            Optional<String> antigo = d.grupos.adicionar(nome, p.getUUID(), p.getGameProfile().getName());
            antigo.ifPresent(a -> ok(c, "odteste.grupo.trocou", p.getDisplayName(), a));
        }
        d.setDirty();
        return ok(c, "odteste.grupo.adicionados", js.size(), nome) > 0 ? js.size() : 0;
    }

    private static int tirar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String nome = StringArgumentType.getString(c, GRUPO);
        DadosHistoria d = dados(c);
        int n = 0;
        for (ServerPlayer p : EntityArgument.getPlayers(c, JOGADORES)) {
            if (d.grupos.tirar(nome, p.getUUID())) n++;
        }
        d.setDirty();
        return ok(c, "odteste.grupo.tirados", n, nome) > 0 ? n : 0;
    }

    private static int apagar(CommandContext<CommandSourceStack> c) {
        String nome = StringArgumentType.getString(c, GRUPO);
        DadosHistoria d = dados(c);
        Optional<String> n = Grupos.normalizar(nome);
        if (n.isPresent() && d.vilas.containsKey(n.get())) return erro(c, "odteste.grupo.erro.tem_evento", n.get());
        if (!d.grupos.apagar(nome)) return erro(c, "odteste.evento.erro.sem_grupo", nome);
        d.setDirty();
        return ok(c, "odteste.grupo.apagado", nome);
    }

    private static int listarGrupos(CommandContext<CommandSourceStack> c) {
        DadosHistoria d = dados(c);
        if (d.grupos.todos().isEmpty()) return ok(c, "odteste.grupo.nenhum");
        for (Grupos.Grupo g : d.grupos.todos()) mostrarGrupo(c, g, d);
        return d.grupos.todos().size();
    }

    private static int verGrupo(CommandContext<CommandSourceStack> c) {
        DadosHistoria d = dados(c);
        String nome = StringArgumentType.getString(c, GRUPO);
        Optional<Grupos.Grupo> g = d.grupos.grupo(nome);
        if (g.isEmpty()) return erro(c, "odteste.evento.erro.sem_grupo", nome);
        mostrarGrupo(c, g.get(), d);
        return 1;
    }

    private static void mostrarGrupo(CommandContext<CommandSourceStack> c, Grupos.Grupo g, DadosHistoria d) {
        String membros = g.nomes().isEmpty() ? "-" : String.join(", ", g.nomes());
        String marcos = g.marcos().isEmpty() ? "-" : String.join(", ", g.marcos());
        String evento = d.vilas.containsKey(g.nome()) ? IdsHistoria.EVENTO_PRIMEIRO_SINAL
                + (d.vilas.get(g.nome()).resolvido ? " (" + IdsHistoria.MARCO_ENIGMA_SINO + ")" : "") : "-";
        c.getSource().sendSuccess(() -> Component.translatable("odteste.grupo.ver", g.nome(), membros, marcos, evento), false);
    }

    // ------------------------------------------------------------------ evento

    private static LiteralArgumentBuilder<CommandSourceStack> evento() {
        return Commands.literal("evento")
                .then(Commands.literal(IdsHistoria.EVENTO_PRIMEIRO_SINAL).then(argGrupo()
                        .executes(c -> primeiroSinal(c, false))
                        .then(Commands.argument(PONTO, BlockPosArgument.blockPos()).executes(c -> primeiroSinal(c, true)))))
                .then(Commands.literal("desfazer").then(argGrupo().executes(c -> {
                    PrimeiroSinal.Resposta r = PrimeiroSinal.desfazer(c.getSource().getServer(), StringArgumentType.getString(c, GRUPO));
                    return responder(c, r.ok(), r.mensagem());
                })))
                .then(Commands.literal("ver").then(argGrupo().executes(ComandosHistoria::verGrupo)))
                .then(Commands.literal("ancorar").then(argLugar()
                        .then(Commands.argument(PONTO, BlockPosArgument.blockPos()).executes(ComandosHistoria::ancorar))))
                .then(Commands.literal("desancorar").then(argLugar().executes(ComandosHistoria::desancorar)));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> argLugar() {
        return Commands.argument(LUGAR, StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(IdsHistoria.LUGARES, b));
    }

    private static int primeiroSinal(CommandContext<CommandSourceStack> c, boolean comPonto) {
        String g = StringArgumentType.getString(c, GRUPO);
        ServerLevel nivel = comPonto ? c.getSource().getLevel() : null;
        BlockPos ponto = comPonto ? BlockPosArgument.getBlockPos(c, PONTO) : null;
        PrimeiroSinal.Resposta r = PrimeiroSinal.comecar(c.getSource().getServer(), g, nivel, ponto);
        return responder(c, r.ok(), r.mensagem());
    }

    private static int ancorar(CommandContext<CommandSourceStack> c) {
        String lugar = StringArgumentType.getString(c, LUGAR).toLowerCase(Locale.ROOT);
        if (!IdsHistoria.LUGARES.contains(lugar)) return erro(c, "odteste.evento.erro.lugar", lugar);
        BlockPos p = BlockPosArgument.getBlockPos(c, PONTO);
        DadosHistoria d = dados(c);
        d.ancoras.put(lugar, GlobalPos.of(c.getSource().getLevel().dimension(), p));
        d.setDirty();
        return ok(c, "odteste.evento.ancorado", lugar, p.getX(), p.getY(), p.getZ());
    }

    private static int desancorar(CommandContext<CommandSourceStack> c) {
        String lugar = StringArgumentType.getString(c, LUGAR).toLowerCase(Locale.ROOT);
        DadosHistoria d = dados(c);
        if (d.ancoras.remove(lugar) == null) return erro(c, "odteste.evento.erro.lugar", lugar);
        d.setDirty();
        return ok(c, "odteste.evento.desancorado", lugar);
    }

    // ------------------------------------------------------------------ estrutura

    private static LiteralArgumentBuilder<CommandSourceStack> estrutura() {
        return Commands.literal("estrutura")
                .then(Commands.literal("prever").then(argTemplate().then(Commands.argument(PONTO, BlockPosArgument.blockPos())
                        .executes(c -> prever(c, Rotation.NONE))
                        .then(argRotacao().executes(c -> prever(c, rotacao(c)))))))
                .then(Commands.literal("colocar").then(argTemplate().then(Commands.argument(PONTO, BlockPosArgument.blockPos())
                        .executes(c -> colocar(c, Rotation.NONE))
                        .then(argRotacao().executes(c -> colocar(c, rotacao(c)))))))
                .then(Commands.literal("desfazer").then(Commands.argument("id", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                DadosHistoria.de(c.getSource().getServer()).colocacoes.keySet(), b))
                        .executes(ComandosHistoria::desfazerEstrutura)))
                .then(Commands.literal("lista").executes(ComandosHistoria::listarEstruturas));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> argTemplate() {
        return Commands.argument(TEMPLATE, ResourceLocationArgument.id())
                .suggests((c, b) -> SharedSuggestionProvider.suggestResource(
                        c.getSource().getLevel().getStructureManager().listTemplates(), b));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> argRotacao() {
        return Commands.argument(ROTACAO, StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(ROTACOES, b));
    }

    private static Rotation rotacao(CommandContext<CommandSourceStack> c) {
        return switch (StringArgumentType.getString(c, ROTACAO)) {
            case "90" -> Rotation.CLOCKWISE_90;
            case "180" -> Rotation.CLOCKWISE_180;
            case "270" -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static String chavePrevia(ResourceLocation id, BlockPos p, Rotation r) {
        return id + "|" + p.asLong() + "|" + r.name();
    }

    private static int prever(CommandContext<CommandSourceStack> c, Rotation r) {
        ServerLevel nivel = c.getSource().getLevel();
        ResourceLocation id = ResourceLocationArgument.getId(c, TEMPLATE);
        BlockPos p = BlockPosArgument.getBlockPos(c, PONTO);
        Optional<StructureTemplate> t = Estruturas.template(nivel, id);
        if (t.isEmpty()) return erro(c, "odteste.estrutura.erro.template", id.toString());
        Espaco.Caixa caixa = Estruturas.caixa(t.get(), p, r);
        Espaco.Resultado res = Estruturas.checar(nivel, t.get(), p, r);
        PREVIAS.put(c.getSource().getTextName(), chavePrevia(id, p, r));
        PREVIAS_QUANDO.put(c.getSource().getTextName(), System.currentTimeMillis());
        if (c.getSource().getEntity() instanceof ServerPlayer admin) contorno(nivel, admin, caixa, res);
        if (res.cabe()) return ok(c, "odteste.estrutura.prever.cabe", id.toString(), caixa.volume());
        return erro(c, "odteste.estrutura.prever.nao_cabe", id.toString(), res.x(), res.y(), res.z(), motivo(res.motivo()));
    }

    private static Component motivo(Espaco.Tipo t) {
        return Component.translatable("odteste.espaco." + t.name().toLowerCase(Locale.ROOT));
    }

    /** Contorno da caixa em partículas, só para o admin que pediu. O bloqueio aparece em chama. */
    private static void contorno(ServerLevel nivel, ServerPlayer admin, Espaco.Caixa c, Espaco.Resultado res) {
        int enviadas = 0;
        for (int x = c.x0(); x <= c.x1() + 1 && enviadas < 400; x++) {
            for (int y : new int[] {c.y0(), c.y1() + 1}) {
                for (int z : new int[] {c.z0(), c.z1() + 1}) {
                    nivel.sendParticles(admin, ParticleTypes.END_ROD, true, x, y, z, 1, 0, 0, 0, 0);
                    enviadas++;
                }
            }
        }
        for (int z = c.z0(); z <= c.z1() + 1 && enviadas < 800; z++) {
            for (int y : new int[] {c.y0(), c.y1() + 1}) {
                for (int x : new int[] {c.x0(), c.x1() + 1}) {
                    nivel.sendParticles(admin, ParticleTypes.END_ROD, true, x, y, z, 1, 0, 0, 0, 0);
                    enviadas++;
                }
            }
        }
        for (int y = c.y0(); y <= c.y1() + 1 && enviadas < 1000; y++) {
            for (int x : new int[] {c.x0(), c.x1() + 1}) {
                for (int z : new int[] {c.z0(), c.z1() + 1}) {
                    nivel.sendParticles(admin, ParticleTypes.END_ROD, true, x, y, z, 1, 0, 0, 0, 0);
                    enviadas++;
                }
            }
        }
        if (!res.cabe()) {
            nivel.sendParticles(admin, ParticleTypes.FLAME, true, res.x() + 0.5, res.y() + 0.5, res.z() + 0.5, 8, 0.2, 0.2, 0.2, 0);
        }
    }

    private static int colocar(CommandContext<CommandSourceStack> c, Rotation r) {
        ServerLevel nivel = c.getSource().getLevel();
        ResourceLocation id = ResourceLocationArgument.getId(c, TEMPLATE);
        BlockPos p = BlockPosArgument.getBlockPos(c, PONTO);
        String quem = c.getSource().getTextName();
        Long quando = PREVIAS_QUANDO.get(quem);
        if (!chavePrevia(id, p, r).equals(PREVIAS.get(quem)) || quando == null
                || System.currentTimeMillis() - quando > PREVIA_VALE_MS) {
            return erro(c, "odteste.estrutura.erro.prever_antes");
        }
        Optional<StructureTemplate> t = Estruturas.template(nivel, id);
        if (t.isEmpty()) return erro(c, "odteste.estrutura.erro.template", id.toString());
        Estruturas.Tentativa tent = Estruturas.colocar(nivel, t.get(), p, r, true);
        if (!tent.ok()) {
            if ("grande".equals(tent.erro())) return erro(c, "odteste.estrutura.erro.grande", Estruturas.VOLUME_MAXIMO);
            Espaco.Resultado res = tent.checagem();
            return erro(c, "odteste.estrutura.erro.sem_lugar", id.toString(), res.x(), res.y(), res.z(), motivo(res.motivo()));
        }
        PREVIAS.remove(quem);
        DadosHistoria d = dados(c);
        String idColocacao = d.novoIdColocacao();
        d.colocacoes.put(idColocacao, new Colocacao(idColocacao, id.toString(), nivel.dimension(), tent.ponto(), tent.backup().get()));
        d.setDirty();
        BlockPos o = tent.ponto();
        return ok(c, tent.mudouDeLugar() ? "odteste.estrutura.colocada_outro_lugar" : "odteste.estrutura.colocada",
                id.toString(), o.getX(), o.getY(), o.getZ(), idColocacao);
    }

    private static int desfazerEstrutura(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "id");
        DadosHistoria d = dados(c);
        Colocacao col = d.colocacoes.get(id);
        if (col == null) return erro(c, "odteste.estrutura.erro.id", id);
        ServerLevel nivel = c.getSource().getServer().getLevel(col.dimensao());
        if (nivel == null) return erro(c, "odteste.evento.erro.dimensao");
        Backup b = col.backup();
        List<BlockPos> faltando = new ArrayList<>();
        for (BlockPos p : b.posicoes()) {
            if (!nivel.hasChunk(p.getX() >> 4, p.getZ() >> 4)) {
                faltando.add(p);
                break;
            }
        }
        if (!faltando.isEmpty()) {
            BlockPos p = faltando.get(0);
            return erro(c, "odteste.evento.erro.descarregado", p.getX(), p.getY(), p.getZ());
        }
        Backup.Restauracao rest = b.restaurar(nivel, PrimeiroSinal::ehPeca);
        d.colocacoes.remove(id);
        d.setDirty();
        return responder(c, true, PrimeiroSinal.comItensSoltos(
                Component.translatable("odteste.estrutura.desfeita", id, rest.mantidos()), rest.itensSoltos(), rest.ondeSoltou()));
    }

    private static int listarEstruturas(CommandContext<CommandSourceStack> c) {
        DadosHistoria d = dados(c);
        if (d.colocacoes.isEmpty()) return ok(c, "odteste.estrutura.nenhuma");
        for (Colocacao col : d.colocacoes.values()) {
            c.getSource().sendSuccess(() -> Component.literal(col.id() + ": " + col.template() + " @ "
                    + col.canto().toShortString() + " (" + col.dimensao().location() + ")").withStyle(ChatFormatting.GRAY), false);
        }
        return d.colocacoes.size();
    }
}
