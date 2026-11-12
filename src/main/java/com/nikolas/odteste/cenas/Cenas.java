package com.nikolas.odteste.cenas;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.nikolas.odteste.OdMod;
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
 * Cenas, lado do servidor: só guarda quem já viu cada cena e manda o pacote. Nada no mundo muda, o jogador não é
 * movido nem fica invulnerável: a câmera é só do cliente.
 *
 * Comandos de admin (permissão 2): {@code /odteste cena tocar <jogadores> <cena>} (toca de novo, mesmo para quem já
 * viu) e {@code /odteste cena esquecer <jogadores> <cena>} (volta a "não viu": a cena dispara de novo no gatilho).
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

    /** Manda tocar a cena neste jogador e marca como vista (pular também conta como visto). */
    public static void tocar(ServerPlayer p, String cena, Ancora a) {
        marcarVista(p, cena);
        PacketDistributor.sendToPlayer(p, new TocarCena(cena, a.x(), a.y(), a.z(), a.giro()));
        OdMod.LOGGER.info("Cena {} para {}", cena, p.getGameProfile().getName());
    }

    // ------------------------------------------------------------------ comandos

    private static void registrarComandos(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("cena").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("tocar").then(Commands.argument("jogadores", EntityArgument.players())
                                .then(argCena().executes(c -> tocarComando(c)))))
                        .then(Commands.literal("esquecer").then(Commands.argument("jogadores", EntityArgument.players())
                                .then(argCena().executes(c -> esquecerComando(c)))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> argCena() {
        return Commands.argument("cena", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(IdsCena.CENAS, b));
    }

    private static int tocarComando(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String cena = StringArgumentType.getString(c, "cena");
        if (CenasConhecidas.de(cena).isEmpty()) {
            c.getSource().sendFailure(Component.translatable("odteste.cena.cmd.desconhecida", cena));
            return 0;
        }
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(c, "jogadores");
        int semLugar = 0;
        for (ServerPlayer p : jogadores) {
            Function<ServerPlayer, Optional<Ancora>> lugar = LUGARES.get(cena);
            Optional<Ancora> a = lugar == null ? Optional.empty() : lugar.apply(p);
            if (a.isEmpty()) {
                // Sem lugar (ex.: vila sem âncora ou em outra dimensão): toca onde o jogador está, olhando para frente.
                semLugar++;
                a = Optional.of(new Ancora(p.getX(), p.getY(), p.getZ(), p.getYRot()));
            }
            tocar(p, cena, a.get());
        }
        int n = jogadores.size();
        int sl = semLugar;
        c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.tocou", cena, n), true);
        if (sl > 0) c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.sem_lugar", sl), false);
        return n;
    }

    private static int esquecerComando(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String cena = StringArgumentType.getString(c, "cena");
        if (CenasConhecidas.de(cena).isEmpty()) {
            c.getSource().sendFailure(Component.translatable("odteste.cena.cmd.desconhecida", cena));
            return 0;
        }
        Collection<ServerPlayer> jogadores = EntityArgument.getPlayers(c, "jogadores");
        for (ServerPlayer p : jogadores) esquecer(p, cena);
        int n = jogadores.size();
        c.getSource().sendSuccess(() -> Component.translatable("odteste.cena.cmd.esqueceu", cena, n), true);
        return n;
    }
}
