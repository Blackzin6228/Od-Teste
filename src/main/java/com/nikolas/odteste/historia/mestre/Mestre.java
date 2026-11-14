package com.nikolas.odteste.historia.mestre;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import com.nikolas.odteste.historia.IdsHistoria;
import com.nikolas.odteste.historia.VilaMarcada;
import com.nikolas.odteste.historia.gatilhos.IdsGatilhos;
import com.nikolas.odteste.historia.livros.LivrosLidos;
import com.nikolas.odteste.historia.vilacalada.DadosVilaCalada;
import com.nikolas.odteste.historia.vilacalada.IdsVilaCalada;
import com.nikolas.odteste.historia.vilacalada.PistasVilaCalada;
import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;
import com.nikolas.odteste.horror.CamadaHorror;
import com.nikolas.odteste.sistemas.Degrau;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * O mestre (regras em {@link RegrasMestre}). Só servidor.
 * <ul>
 * <li>Avisos automáticos da história ({@link #avisar}) vão só para os mestres, não para todo op.</li>
 * <li>"Mestre ouvindo": em espectador, o mestre recebe os sons e pistas que o grupo recebe perto dele
 * ({@link #ouvindo}, {@link #ouvintesPerto}). Nunca conta como presente para regra nenhuma.</li>
 * <li>Comandos: {@code /odteste mestre ouvir on|off} e {@code /odteste mestre status <grupo>}.</li>
 * </ul>
 * Sem lag: tudo é laço curto sobre os jogadores de um nível (poucos), chamado só quando um som/pista já vai sair.
 */
public final class Mestre {
    /** Escolha de cada mestre (ligar/desligar o ouvir). Só memória: reiniciar volta ao padrão da config. */
    private static final Map<UUID, Boolean> ESCOLHA = new HashMap<>();

    private Mestre() {}

    /** Chamado no construtor do mod (pelo {@code PrimeiroSinal.iniciar}). */
    public static void iniciar() {
        ConfigMestre.registrar();
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> ESCOLHA.clear());
    }

    public static boolean ehMestre(ServerPlayer p) {
        return p != null && RegrasMestre.ehMestre(p.hasPermissions(2), p.getGameProfile().getName(), ConfigMestre.mestres());
    }

    /** Mestre em espectador com o ouvir ligado. */
    public static boolean ouvindo(ServerPlayer p) {
        return p != null && p.isSpectator() && RegrasMestre.ouvindo(ehMestre(p), true, ESCOLHA.get(p.getUUID()),
                ConfigMestre.ouvirPadrao());
    }

    /** Mestres ouvindo neste nível a até {@code raio} do ponto, ou com a câmera presa em {@code alvo}. */
    public static List<ServerPlayer> ouvintesPerto(ServerLevel nivel, Vec3 ponto, double raio, ServerPlayer alvo) {
        List<ServerPlayer> r = new ArrayList<>();
        for (ServerPlayer p : nivel.players()) {
            if (!p.isSpectator() || !ouvindo(p)) continue;
            if ((alvo != null && p.getCamera() == alvo) || p.position().distanceToSqr(ponto) <= raio * raio) r.add(p);
        }
        return r;
    }

    /** Mestres ouvindo (de qualquer nível) com a câmera presa neste jogador. */
    public static List<ServerPlayer> assistindo(ServerPlayer alvo) {
        List<ServerPlayer> r = new ArrayList<>();
        for (ServerPlayer p : alvo.server.getPlayerList().getPlayers()) {
            if (p != alvo && p.isSpectator() && p.getCamera() == alvo && ouvindo(p)) r.add(p);
        }
        return r;
    }

    /** Aviso da história só para os mestres online. */
    public static void avisar(MinecraftServer s, Component msg) {
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (ehMestre(p)) p.sendSystemMessage(msg);
        }
    }

    public static boolean algumOnline(MinecraftServer s) {
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (ehMestre(p)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ comandos

    /** Console, bloco de comando ou mestre. Op que não está na lista não usa. */
    private static boolean podeUsar(CommandSourceStack s) {
        if (!s.hasPermission(2)) return false;
        return !(s.getEntity() instanceof ServerPlayer p) || ehMestre(p);
    }

    static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("mestre").requires(Mestre::podeUsar)
                        .then(Commands.literal("ouvir")
                                .then(Commands.literal("on").executes(c -> ouvir(c, true)))
                                .then(Commands.literal("off").executes(c -> ouvir(c, false))))
                        .then(Commands.literal("status").then(Commands.argument("grupo", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        DadosHistoria.de(c.getSource().getServer()).grupos.todos().stream()
                                                .map(Grupos.Grupo::nome), b))
                                .executes(Mestre::status)))));
    }

    private static int ouvir(CommandContext<CommandSourceStack> c, boolean ligar) {
        if (!(c.getSource().getEntity() instanceof ServerPlayer p)) {
            c.getSource().sendFailure(Component.translatable("odteste.mestre.ouvir.nao_jogador"));
            return 0;
        }
        ESCOLHA.put(p.getUUID(), ligar);
        c.getSource().sendSuccess(() -> Component.translatable(ligar ? "odteste.mestre.ouvir.on" : "odteste.mestre.ouvir.off"), false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        MinecraftServer s = c.getSource().getServer();
        String nome = StringArgumentType.getString(c, "grupo");
        DadosHistoria dh = DadosHistoria.de(s);
        Optional<Grupos.Grupo> og = dh.grupos.grupo(nome);
        if (og.isEmpty()) {
            c.getSource().sendFailure(Component.translatable("odteste.evento.erro.sem_grupo", nome));
            return 0;
        }
        Grupos.Grupo g = og.get();
        DadosVilaCalada dv = DadosVilaCalada.de(s);
        VilaMarcada v = dh.vilas.get(g.nome());
        GlobalPos calada = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        long hora = s.overworld().getDayTime();
        boolean noite = RegrasVilaCalada.noite(hora);
        boolean chegou = g.tem(IdsVilaCalada.MARCO_CHEGOU);
        List<Component> linhas = new ArrayList<>();

        linhas.add(Component.translatable("odteste.mestre.status.titulo", g.nome()).withStyle(ChatFormatting.GOLD));
        linhas.add(Component.translatable("odteste.mestre.status.marcos",
                simNao(g.tem(IdsHistoria.MARCO_PRIMEIRO_SINAL)), simNao(g.tem(IdsHistoria.MARCO_ENIGMA_SINO)), simNao(chegou)));
        linhas.add(Component.translatable(noite ? "odteste.mestre.status.hora.noite" : "odteste.mestre.status.hora.dia",
                RegrasMestre.minutos(RegrasMestre.ateMudar(hora))));
        linhas.add(sino(s, g, dv, calada, noite, chegou));
        if (chegou) linhas.add(com.nikolas.odteste.historia.vilacalada.Dobre.status(s, g));
        linhas.add(Component.translatable("odteste.mestre.status.lugares",
                v == null ? Component.translatable("odteste.mestre.status.sem_lugar") : Component.literal(xyz(v.sino)),
                calada == null ? Component.translatable("odteste.mestre.status.sem_lugar") : Component.literal(xyz(calada.pos()))));

        LivrosLidos lidos = LivrosLidos.de(s);
        for (Map.Entry<UUID, String> m : g.membros().entrySet()) {
            ServerPlayer p = s.getPlayerList().getPlayer(m.getKey());
            if (p == null) {
                linhas.add(Component.translatable("odteste.mestre.status.fora", m.getValue()).withStyle(ChatFormatting.DARK_GRAY));
                continue;
            }
            int expo = DadosJogador.exposicao(p);
            linhas.add(Component.translatable("odteste.mestre.status.membro", m.getValue(),
                    longe(p, v == null ? null : GlobalPos.of(v.dimensao, v.sino)), longe(p, calada),
                    expo, Component.translatable("odteste.mestre.degrau." + Degrau.de(expo).name().toLowerCase(Locale.ROOT)),
                    DadosJogador.lucidez(p), DadosJogador.lucidezMax(p), CamadaHorror.sustosNaNoite(p),
                    simNao(lidos.leu(p.getUUID(), IdsGatilhos.LIVRO_SINEIRO)))
                    .withStyle(p.isSpectator() ? ChatFormatting.DARK_GRAY : ChatFormatting.GRAY));
        }
        for (Component l : linhas) c.getSource().sendSuccess(() -> l, false);
        return 1;
    }

    private static Component sino(MinecraftServer s, Grupos.Grupo g, DadosVilaCalada dv, GlobalPos calada,
                                  boolean noite, boolean chegou) {
        String chave;
        Object[] args = {};
        if (calada == null) chave = "odteste.mestre.status.sino.sem_ancora";
        else if (!dv.ligado) chave = "odteste.mestre.status.sino.desligado";
        else if (!g.tem(IdsHistoria.MARCO_PRIMEIRO_SINAL)) chave = "odteste.mestre.status.sino.sem_sinal";
        else if (chegou) chave = "odteste.mestre.status.sino.chegou";
        else if (!noite) chave = "odteste.mestre.status.sino.dia";
        else {
            Long proximo = PistasVilaCalada.proximoToque(g.nome());
            ServerLevel nivel = s.getLevel(calada.dimension());
            if (proximo == null || nivel == null) {
                chave = "odteste.mestre.status.sino.esperando";
            } else {
                long falta = proximo - nivel.getGameTime();
                if (falta <= 20 * 20) {
                    chave = "odteste.mestre.status.sino.logo";
                } else {
                    chave = "odteste.mestre.status.sino.em";
                    args = new Object[] {RegrasMestre.minutos(falta)};
                }
            }
        }
        return Component.translatable(chave, args);
    }

    private static Component simNao(boolean b) {
        return Component.translatable(b ? "odteste.evento.sim" : "odteste.evento.nao");
    }

    private static String xyz(BlockPos p) {
        return p.getX() + " " + p.getY() + " " + p.getZ();
    }

    private static MutableComponent longe(ServerPlayer p, GlobalPos alvo) {
        if (alvo == null) return Component.literal("-");
        if (p.level().dimension() != alvo.dimension()) return Component.translatable("odteste.mestre.status.outra_dimensao");
        long d = RegrasMestre.distancia(alvo.pos().getX() + 0.5 - p.getX(), alvo.pos().getZ() + 0.5 - p.getZ());
        return Component.translatable("odteste.mestre.status.blocos", RegrasMestre.milhar(d));
    }
}
