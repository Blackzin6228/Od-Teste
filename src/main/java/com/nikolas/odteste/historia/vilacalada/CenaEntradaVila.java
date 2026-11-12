package com.nikolas.odteste.historia.vilacalada;

import com.nikolas.odteste.cenas.Cenas;
import com.nikolas.odteste.cenas.IdsCena;
import com.nikolas.odteste.cenas.RegrasCena;
import com.nikolas.odteste.estruturas.Colocacao;
import com.nikolas.odteste.historia.DadosHistoria;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Cena de entrada da Vila Calada, lado do servidor: na primeira vez que cada jogador entra no muro, manda o pacote
 * da cena e marca "já viu" ({@code odteste:cenas_vistas}). Morrer, sair e voltar não repetem. O admin repete com
 * {@code /odteste cena tocar <jogador> entrada_vila_calada}.
 *
 * Vale para qualquer jogador (com ou sem grupo). Desliga junto com as pistas ({@code /odteste vila_calada desligar})
 * ou sem âncora.
 *
 * Sem lag: uma conta de distância por jogador por segundo (teto {@link RegrasCenaVila#MAX_JOGADORES}), em outro
 * tick que as pistas e o clima.
 */
public final class CenaEntradaVila {
    private CenaEntradaVila() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(CenaEntradaVila::aoTick);
        Cenas.definirLugar(IdsCena.ENTRADA_VILA_CALADA, CenaEntradaVila::ancora);
    }

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        if (s.getTickCount() % 20 != 15) return;
        DadosVilaCalada dv = PistasVilaCalada.dados(s);
        if (!dv.ligado) return;
        GlobalPos centro = DadosHistoria.de(s).ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        ServerLevel nivel = centro == null ? null : s.getLevel(centro.dimension());
        if (nivel == null) return;
        int n = 0;
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (n++ >= RegrasCenaVila.MAX_JOGADORES) break;
            if (p.level() != nivel || p.isSpectator() || p.isDeadOrDying()) continue;
            if (Cenas.viu(p, IdsCena.ENTRADA_VILA_CALADA)) continue;
            BlockPos c = centro.pos();
            if (!RegrasCenaVila.dentro(p.getX() - (c.getX() + 0.5), p.getY() - c.getY(), p.getZ() - (c.getZ() + 0.5))) {
                continue;
            }
            ancora(p).ifPresent(a -> Cenas.tocar(p, IdsCena.ENTRADA_VILA_CALADA, a));
        }
    }

    /**
     * O centro do portão, no chão, olhando para dentro da vila. Vazio se a vila não tem âncora ou o jogador está em
     * outra dimensão.
     */
    static Optional<Cenas.Ancora> ancora(ServerPlayer p) {
        MinecraftServer s = p.getServer();
        if (s == null) return Optional.empty();
        DadosHistoria dh = DadosHistoria.de(s);
        GlobalPos centro = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        if (centro == null || p.level().dimension() != centro.dimension()) return Optional.empty();
        ServerLevel nivel = (ServerLevel) p.level();
        BlockPos c = centro.pos();
        double cx = c.getX() + 0.5, cz = c.getZ() + 0.5;

        Direction sul = rotacao(dh, PistasVilaCalada.dados(s), centro).map(r -> r.rotate(Direction.SOUTH)).orElse(null);
        double[] d = RegrasCenaVila.direcaoPortao(sul == null ? null : sul.getStepX(), sul == null ? null : sul.getStepZ(),
                p.getX() - cx, p.getZ() - cz);
        double px = cx + d[0] * RegrasCenaVila.PORTAO_DO_CENTRO;
        double pz = cz + d[1] * RegrasCenaVila.PORTAO_DO_CENTRO;
        int bx = (int) Math.floor(px), bz = (int) Math.floor(pz);
        double y = c.getY();
        if (nivel.hasChunk(bx >> 4, bz >> 4)) y = nivel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        // Olha do portão para dentro (o contrário de centro → portão).
        float giro = RegrasCena.giroDe(-d[0], -d[1]);
        return Optional.of(new Cenas.Ancora(px, y, pz, giro));
    }

    /** Rotação da estrutura que ancorou a vila sozinha, se a âncora ainda for a dela. Ancorada à mão: vazio. */
    private static Optional<Rotation> rotacao(DadosHistoria dh, DadosVilaCalada dv, GlobalPos centro) {
        if (dv.colocacao.isEmpty() || dv.ancoraAuto == null || centro.pos().asLong() != dv.ancoraAuto
                || !centro.dimension().location().toString().equals(dv.ancoraAutoDim)) {
            return Optional.empty();
        }
        Colocacao col = dh.colocacoes.get(dv.colocacao);
        if (col == null) return Optional.empty();
        return Optional.of(col.partes() == null ? Rotation.NONE : col.partes().rotacao);
    }
}
