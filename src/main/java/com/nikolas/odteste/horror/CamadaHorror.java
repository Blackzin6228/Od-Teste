package com.nikolas.odteste.horror;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.historia.vilacalada.ClimaVilaCalada;
import com.nikolas.odteste.rede.AreaHorror;
import com.nikolas.odteste.rede.Susto;
import com.nikolas.odteste.sistemas.Degrau;
import com.nikolas.odteste.sistemas.RegrasHorror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Camada de horror, lado do servidor. Uma vez por segundo, para cada jogador (teto {@link #MAX_JOGADORES}):
 * <ul>
 * <li>diz ao cliente em que área da história ele está (só quando muda);</li>
 * <li>a cada 30 s, tenta um susto grande ({@link RegrasSusto}): só de noite, nunca para o mestre (tag
 * {@code odteste_mestre}), criativo ou espectador, nem para quem outro sistema já está assustando.</li>
 * </ul>
 * Comando do mestre: {@code /odteste susto <tipo> <jogador>} (dispara na hora, de dia também).
 * Nada salvo no mundo: reiniciar zera as contas da noite.
 *
 * <b>Área nova da história:</b> {@link #registrarArea}("id", jogador -> está dentro?). A conta tem que ser leve
 * (roda uma vez por segundo por jogador).
 * <b>Outro sistema que assusta</b> (ex.: o "O Que Segue"): {@link #ocupadoSe}(jogador -> está com ele?): nenhum
 * susto grande sozinho enquanto isso.
 */
public final class CamadaHorror {
    static final int MAX_JOGADORES = 128;

    private record Area(String id, Predicate<ServerPlayer> dentro) {}

    private static final List<Area> AREAS = new ArrayList<>();
    private static final List<Predicate<ServerPlayer>> OCUPADO = new ArrayList<>();
    private static final Map<UUID, String> ENVIADO = new HashMap<>();
    private static final Map<UUID, RegrasSusto.Diario> DIARIOS = new HashMap<>();
    private static final Map<UUID, TipoSusto> ULTIMO = new HashMap<>();

    private static final DynamicCommandExceptionType TIPO_INVALIDO =
            new DynamicCommandExceptionType(t -> Component.translatable("odteste.comando.susto.invalido", t));

    private CamadaHorror() {}

    public static void iniciar() {
        registrarArea(IdsHorror.AREA_VILA_CALADA, ClimaVilaCalada::estaDentro);
        NeoForge.EVENT_BUS.addListener(CamadaHorror::aoTick);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> esquecer(e.getEntity().getUUID()));
        // Entrou de novo: o cliente começa sem área; manda de novo.
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> ENVIADO.remove(e.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
            ENVIADO.clear();
            DIARIOS.clear();
            ULTIMO.clear();
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
        VilaMorta.iniciar();
    }

    /** Registra uma área da história (a primeira que bate vale). */
    public static synchronized void registrarArea(String id, Predicate<ServerPlayer> dentro) {
        AREAS.removeIf(a -> a.id().equals(id));
        AREAS.add(new Area(id, dentro));
    }

    /** Outro sistema avisa que está assustando o jogador: sem susto grande sozinho enquanto valer. */
    public static synchronized void ocupadoSe(Predicate<ServerPlayer> ocupado) {
        OCUPADO.add(ocupado);
    }

    private static void esquecer(UUID u) {
        ENVIADO.remove(u);
        DIARIOS.remove(u);
        ULTIMO.remove(u);
    }

    /** Em que área o jogador está ("" = nenhuma). */
    public static String areaDe(ServerPlayer p) {
        for (Area a : AREAS) {
            if (a.dentro().test(p)) return a.id();
        }
        return "";
    }

    static RegrasCamadaHorror.Momento momento(ServerPlayer p, boolean emArea) {
        Level nivel = p.level();
        int ceu = nivel.getBrightness(LightLayer.SKY, BlockPos.containing(p.getEyePosition()));
        return RegrasCamadaHorror.momento(nivel.dimension() == Level.OVERWORLD, nivel.getDayTime(), ceu, emArea);
    }

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        int tick = s.getTickCount();
        if (tick % 20 != 15) return; // depois das pistas (0) e do clima da vila (10): não pesa no mesmo tick
        int n = 0;
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (n++ >= MAX_JOGADORES) break;
            String area = p.isSpectator() ? "" : areaDe(p);
            if (!area.equals(ENVIADO.get(p.getUUID()))) {
                ENVIADO.put(p.getUUID(), area);
                PacketDistributor.sendToPlayer(p, new AreaHorror(area));
            }
            RegrasCamadaHorror.Momento m = momento(p, !area.isEmpty());
            RegrasSusto.Diario d = DIARIOS.computeIfAbsent(p.getUUID(), u -> new RegrasSusto.Diario());
            boolean pode = RegrasCamadaHorror.podeSusto(m) && alvoValido(p);
            long agora = s.overworld().getGameTime();
            d.camada(pode, agora);
            if (!pode || !RegrasSusto.vezDeTentar(tick / 20, p.getUUID().hashCode())) continue;
            int faixa = RegrasHorror.faixa(Degrau.de(DadosJogador.exposicao(p)));
            long noite = RegrasSusto.noite(p.level().getDayTime());
            if (!RegrasSusto.tentar(d, noite, agora, faixa, grupoPerto(p), p.getRandom().nextDouble())) continue;
            TipoSusto t = RegrasSusto.escolher(faixa, ULTIMO.get(p.getUUID()), p.getRandom()::nextInt);
            enviar(p, t, d, noite, agora);
        }
    }

    /** Sozinho, só para quem joga de verdade: nada para o mestre, criativo, espectador ou quem já está ocupado. */
    private static boolean alvoValido(ServerPlayer p) {
        if (p.isSpectator() || p.isCreative() || p.getTags().contains(ClimaVilaCalada.TAG_MESTRE)) return false;
        for (Predicate<ServerPlayer> o : OCUPADO) {
            if (o.test(p)) return false;
        }
        return true;
    }

    /** Outro jogador a menos de 8 blocos (só na hora da tentativa, a cada 30 s). */
    private static boolean grupoPerto(ServerPlayer p) {
        for (var o : p.level().players()) {
            if (o != p && !o.isSpectator() && o.distanceToSqr(p) < RegrasHorror.GRUPO_RAIO_SQ) return true;
        }
        return false;
    }

    private static void enviar(ServerPlayer p, TipoSusto t, RegrasSusto.Diario d, long noite, long agora) {
        d.marcar(noite, agora);
        ULTIMO.put(p.getUUID(), t);
        PacketDistributor.sendToPlayer(p, new Susto(t.id));
    }

    // ---------- comando do mestre ----------

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("susto").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("tipo", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(TipoSusto.ids(), b))
                                .then(Commands.argument("jogador", EntityArgument.player())
                                        .executes(CamadaHorror::comando)))));
    }

    private static int comando(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        String id = StringArgumentType.getString(c, "tipo");
        TipoSusto t = TipoSusto.de(id);
        if (t == null) throw TIPO_INVALIDO.create(id);
        ServerPlayer p = EntityArgument.getPlayer(c, "jogador");
        RegrasSusto.Diario d = DIARIOS.computeIfAbsent(p.getUUID(), u -> new RegrasSusto.Diario());
        enviar(p, t, d, RegrasSusto.noite(p.level().getDayTime()), p.server.overworld().getGameTime());
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.susto", t.id, p.getDisplayName()), true);
        return 1;
    }
}
