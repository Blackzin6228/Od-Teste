package com.nikolas.odteste.cenas;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.mestre.ConfigMestre;
import com.nikolas.odteste.rede.TocarCena;
import com.nikolas.odteste.registro.Registro;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Cenas, lado do servidor: guarda quem já viu cada cena e manda o pacote. Nada no mundo muda; a câmera é só do
 * cliente. Enquanto a cena toca, o jogador fica parado e protegido ({@link ProtecaoCena}, só em memória).
 *
 * Comandos de admin (permissão 2), desde 14/11/2026 com a cena primeiro:
 * {@code /odteste cena tocar <cena> <alvos>} (toca de novo, mesmo para quem já viu; cena em JSON ou de código),
 * {@code /odteste cena parar <alvos>}, {@code /odteste cena esquecer <cena> <alvos>} (volta a "não viu": a cena
 * dispara de novo no gatilho) e {@code /odteste cena lista}. A API para os eventos é {@link Cinema}.
 */
public final class Cenas {
    /** Cenas que o jogador já viu (ids). Sobrevive à morte. Não vai para o cliente. */
    public static final Supplier<AttachmentType<List<String>>> VISTAS = Registro.DADOS.register(
            IdsCena.DADO_VISTAS,
            () -> AttachmentType.<List<String>>builder(() -> List.of())
                    .serialize(Codec.STRING.listOf().xmap(Cenas::limpar, l -> l))
                    .copyOnDeath()
                    .build());

    /** Onde fica a âncora de cada cena para um jogador (vazio = a cena não tem lugar agora). */
    public record Ancora(double x, double y, double z, float giro) {}

    private static final Map<String, Function<ServerPlayer, Optional<Ancora>>> LUGARES = new HashMap<>();

    private Cenas() {}

    /** Força o carregamento da classe antes do registro (o attachment entra no DeferredRegister). */
    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrarComandos(e.getDispatcher()));
        ProtecaoCena.iniciar();
        Roteiros.iniciar();
    }

    /** Quem sabe onde a cena acontece (ex.: a Vila Calada sabe onde fica o portão dela). */
    public static void definirLugar(String cena, Function<ServerPlayer, Optional<Ancora>> lugar) {
        LUGARES.put(cena, lugar);
    }

    private static List<String> limpar(List<String> lido) {
        return RegrasCena.limparVistas(lido);
    }

    public static boolean viu(Player p, String cena) {
        return p.getData(VISTAS).contains(cena);
    }

    public static void marcarVista(Player p, String cena) {
        if (viu(p, cena)) return;
        List<String> l = new ArrayList<>(p.getData(VISTAS));
        l.add(cena);
        p.setData(VISTAS, limpar(l));
    }

    public static void esquecer(Player p, String cena) {
        if (!viu(p, cena)) return;
        List<String> l = new ArrayList<>(p.getData(VISTAS));
        l.remove(cena);
        p.setData(VISTAS, List.copyOf(l));
    }

    /** Onde a cena (de código) acontece para este jogador, se quem sabe o lugar achou. */
    public static Optional<Ancora> lugar(String cena, ServerPlayer p) {
        Function<ServerPlayer, Optional<Ancora>> lugar = LUGARES.get(cena);
        return lugar == null ? Optional.empty() : lugar.apply(p);
    }

    /**
     * Manda tocar a cena (de código) neste jogador e marca como vista (pular também conta como visto). Desde
     * 14/11/2026 o servidor segura o jogador parado e protegido enquanto ela toca ({@link ProtecaoCena}).
     */
    public static void tocar(ServerPlayer p, String cena, Ancora a) {
        marcarVista(p, cena);
        CenasConhecidas.de(cena).ifPresent(c ->
                ProtecaoCena.prender(p, cena, RegrasRoteiro.ticksProtegido(c.fim() + c.saida())));
        PacketDistributor.sendToPlayer(p, new TocarCena(cena, a.x(), a.y(), a.z(), a.giro(),
                RegrasRoteiro.podePular(true, ConfigMestre.pularCenas(), false)));
        OdMod.LOGGER.info("Cena {} para {}", cena, p.getGameProfile().getName());
    }

    // ------------------------------------------------------------------ comandos

    private static void registrarComandos(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("cena").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("tocar").then(argCena()
                                .then(Commands.argument("alvos", EntityArgument.players()).executes(c -> tocarComando(c)))))
                        .then(Commands.literal("parar").then(Commands.argument("alvos", EntityArgument.players())
                                .executes(c -> pararComando(c))))
                        .then(Commands.literal("esquecer").then(argCena()
                                .then(Commands.argument("alvos", EntityArgument.players()).executes(c -> esquecerComando(c)))))
                        .then(Commands.literal("lista").executes(c -> listaComando(c)))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> argCena() {
        return Commands.argument("cena", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Cinema.ids(), b));
    }

    private static int tocarComando(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String cena = StringArgumentType.getString(c, "cena");
        if (!Cinema.existe(cena)) {
            c.getSource().sendFailure(Component.translatable("odteste.cena.cmd.desconhecida", cena));
            return 0;
        }
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(c, "alvos");
        ServerPlayer quem = c.getSource().getEntity() instanceof ServerPlayer sp ? sp : null;
        Map<Cinema.Resultado, Integer> r = Cinema.tocar(jogadores, cena, quem);
        int tocou = r.getOrDefault(Cinema.Resultado.TOCOU, 0) + r.getOrDefault(Cinema.Resultado.SEM_LUGAR, 0);
        int semLugar = r.getOrDefault(Cinema.Resultado.SEM_LUGAR, 0);
        int naoPode = r.getOrDefault(Cinema.Resultado.NAO_PODE, 0);
        c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.tocou", cena, tocou), false);
        if (semLugar > 0) {
            c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.sem_lugar", semLugar), false);
        }
        if (naoPode > 0) {
            c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.nao_pode", naoPode), false);
        }
        return tocou;
    }

    private static int pararComando(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(c, "alvos");
        for (ServerPlayer p : jogadores) Cinema.parar(p);
        int n = jogadores.size();
        c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.parou", n), false);
        return n;
    }

    private static int esquecerComando(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String cena = StringArgumentType.getString(c, "cena");
        if (!Cinema.existe(cena)) {
            c.getSource().sendFailure(Component.translatable("odteste.cena.cmd.desconhecida", cena));
            return 0;
        }
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(c, "alvos");
        for (ServerPlayer p : jogadores) esquecer(p, cena);
        int n = jogadores.size();
        c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.esqueceu", cena, n), false);
        return n;
    }

    private static int listaComando(CommandContext<CommandSourceStack> c) {
        String lista = String.join(", ", Cinema.ids());
        c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.lista", lista), false);
        return Cinema.ids().size();
    }
}
