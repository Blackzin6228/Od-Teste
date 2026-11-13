package com.nikolas.odteste.historia.vilacalada;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import com.nikolas.odteste.rede.ClimaVila;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Clima da Vila Calada, lado do servidor: uma vez por segundo, diz a cada jogador se ele está dentro do muro e se é
 * ele quem vê o Pendurado (o de maior Exposição do grupo, entre quem está dentro da vila). Manda só quando muda. Tudo o que se vê e
 * ouve é feito no cliente ({@code cliente/ClimaVilaCliente}). Nada salvo: some ao reiniciar e volta no segundo seguinte.
 *
 * Vale para quem está lá, de qualquer grupo (ou sem grupo). Desliga junto com as pistas
 * ({@code /odteste vila_calada desligar}) ou sem âncora.
 *
 * Sem lag: uma conta de distância por jogador por segundo (teto {@link RegrasClimaVila#MAX_JOGADORES}).
 */
public final class ClimaVilaCalada {
    private record Enviado(boolean dentro, boolean pendurado) {}

    private static final Enviado NADA = new Enviado(false, false);
    private static final Map<UUID, Enviado> ENVIADO = new HashMap<>();
    /** Tag vanilla do mestre: nunca vê o Pendurado. Quem testa sem a tag vê normal. */
    public static final String TAG_MESTRE = "odteste_mestre";

    private ClimaVilaCalada() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(ClimaVilaCalada::aoTick);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> ENVIADO.remove(e.getEntity().getUUID()));
        // Entrou de novo: o cliente começa sem clima; manda de novo se ainda estiver dentro.
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> ENVIADO.remove(e.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> ENVIADO.clear());
    }

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        if (s.getTickCount() % 20 != 10) return; // meio segundo depois das pistas: não pesa no mesmo tick
        DadosHistoria dh = DadosHistoria.de(s);
        DadosVilaCalada dv = PistasVilaCalada.dados(s);
        GlobalPos ancora = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        ServerLevel nivel = ancora == null || !dv.ligado ? null : s.getLevel(ancora.dimension());
        Set<UUID> dentroDaVila = new HashSet<>();
        int n = 0;
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (n++ >= RegrasClimaVila.MAX_JOGADORES) break;
            if (nivel != null && p.level() == nivel && !p.isSpectator() && dentro(p, ancora.pos())) {
                dentroDaVila.add(p.getUUID());
            }
        }
        Set<UUID> veemPendurado = dentroDaVila.isEmpty() ? Set.of() : maisExpostos(s, dh, dentroDaVila);

        n = 0;
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (n++ >= RegrasClimaVila.MAX_JOGADORES) break;
            boolean dentro = dentroDaVila.contains(p.getUUID());
            Enviado agora = dentro ? new Enviado(true, veemPendurado.contains(p.getUUID())) : NADA;
            Enviado antes = ENVIADO.getOrDefault(p.getUUID(), NADA);
            if (agora.equals(antes)) continue;
            if (agora.equals(NADA)) ENVIADO.remove(p.getUUID());
            else ENVIADO.put(p.getUUID(), agora);
            PacketDistributor.sendToPlayer(p, new ClimaVila(agora.dentro(), agora.pendurado()));
        }
    }

    private static boolean dentro(ServerPlayer p, BlockPos ancora) {
        return RegrasClimaVila.dentro(p.getX() - (ancora.getX() + 0.5), p.getY() - ancora.getY(),
                p.getZ() - (ancora.getZ() + 0.5));
    }

    /**
     * O membro de maior Exposição de cada grupo, só entre quem está dentro da vila (decisão de 13/11/2026).
     * Quem tem a tag {@link #TAG_MESTRE} (o mestre, {@code /tag <nome> add odteste_mestre}) nunca é escolhido.
     */
    private static Set<UUID> maisExpostos(MinecraftServer s, DadosHistoria dh, Set<UUID> dentroDaVila) {
        Set<UUID> r = new HashSet<>();
        for (Grupos.Grupo g : dh.grupos.todos()) {
            Map<UUID, Integer> exp = new HashMap<>();
            for (UUID u : g.membros().keySet()) {
                if (!dentroDaVila.contains(u)) continue;
                ServerPlayer p = s.getPlayerList().getPlayer(u);
                if (p != null && !p.getTags().contains(TAG_MESTRE)) exp.put(u, DadosJogador.exposicao(p));
            }
            UUID melhor = RegrasClimaVila.maisExposto(exp);
            if (melhor != null) r.add(melhor);
        }
        return r;
    }
}
