package com.nikolas.odteste.cenas;

import com.nikolas.odteste.rede.PararCena;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Durante uma cena, o servidor segura o jogador: parado no lugar onde a cena começou, sem tomar dano (menos o que
 * passa por cima de tudo: {@code /kill} e o vazio) e sem bater, usar ou quebrar nada.
 *
 * <p><b>Nada fica salvo:</b> a trava é só uma lista em memória, conferida nos eventos. Não mexe em atributo, modo de
 * jogo nem na flag de invulnerável. Por isso sair do servidor, cair o servidor ou dar erro nunca deixa ninguém preso:
 * a lista some junto. O jogador é solto quando o cliente avisa o fim ({@code odteste:cena_fim}), quando o tempo da
 * cena + 5 s passa, ao morrer, ao trocar de dimensão ou ao sair.
 *
 * <p>Sem lag: com a lista vazia, cada evento sai na primeira linha. No tick, só os presos (no máximo um por jogador).
 */
public final class ProtecaoCena {
    /** Andou mais que isso (blocos ao quadrado) desde o começo: volta ao lugar. */
    private static final double SOLTO = 0.05 * 0.05;

    private record Preso(String cena, Vec3 pos, ResourceKey<Level> dim, long ate) {}

    private static final Map<UUID, Preso> PRESOS = new HashMap<>();

    private ProtecaoCena() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(ProtecaoCena::aoTick);
        NeoForge.EVENT_BUS.addListener(ProtecaoCena::aoDano);
        NeoForge.EVENT_BUS.addListener((AttackEntityEvent e) -> barrar(e.getEntity(), e));
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock e) -> barrar(e.getEntity(), e));
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock e) -> barrar(e.getEntity(), e));
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem e) -> barrar(e.getEntity(), e));
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract e) -> barrar(e.getEntity(), e));
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteractSpecific e) -> barrar(e.getEntity(), e));
        NeoForge.EVENT_BUS.addListener((BlockEvent.BreakEvent e) -> barrar(e.getPlayer(), e));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> PRESOS.remove(e.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent e) -> pararSePreso(e.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent e) -> pararSePreso(e.getEntity()));
        NeoForge.EVENT_BUS.addListener((LivingDeathEvent e) -> {
            if (!PRESOS.isEmpty()) pararSePreso(e.getEntity());
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> PRESOS.clear());
    }

    /** Segura o jogador por até {@code ticks} (a cena nova troca a anterior). */
    public static void prender(ServerPlayer p, String cena, int ticks) {
        MinecraftServer s = p.getServer();
        if (s == null || p.isSpectator()) return;
        p.fallDistance = 0;
        PRESOS.put(p.getUUID(), new Preso(cena, p.position(), p.level().dimension(), s.getTickCount() + (long) ticks));
    }

    /** O cliente avisou que a cena {@code cena} acabou: solta (outra cena que já começou continua presa). */
    public static void soltar(ServerPlayer p, String cena) {
        Preso preso = PRESOS.get(p.getUUID());
        if (preso != null && preso.cena().equals(cena)) PRESOS.remove(p.getUUID());
    }

    /** Solta sem perguntar (comando parar). */
    public static void soltar(ServerPlayer p) {
        PRESOS.remove(p.getUUID());
    }

    public static boolean preso(Player p) {
        return !PRESOS.isEmpty() && PRESOS.containsKey(p.getUUID());
    }

    /** Quantos estão presos agora (painel/teste). */
    public static int quantos() {
        return PRESOS.size();
    }

    private static void pararSePreso(Object entidade) {
        if (entidade instanceof ServerPlayer p && PRESOS.remove(p.getUUID()) != null) {
            PacketDistributor.sendToPlayer(p, PararCena.INSTANCIA);
        }
    }

    private static void barrar(Player p, ICancellableEvent e) {
        if (p != null && preso(p)) e.setCanceled(true);
    }

    private static void aoDano(LivingIncomingDamageEvent e) {
        if (PRESOS.isEmpty() || !(e.getEntity() instanceof ServerPlayer p) || !PRESOS.containsKey(p.getUUID())) return;
        // /kill e o vazio passam (o admin sempre consegue tirar alguém de um lugar ruim).
        if (e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        e.setCanceled(true);
    }

    private static void aoTick(ServerTickEvent.Post e) {
        if (PRESOS.isEmpty()) return;
        MinecraftServer s = e.getServer();
        long agora = s.getTickCount();
        Iterator<Map.Entry<UUID, Preso>> it = PRESOS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Preso> en = it.next();
            Preso preso = en.getValue();
            ServerPlayer p = s.getPlayerList().getPlayer(en.getKey());
            if (p == null || agora > preso.ate() || p.isSpectator() || p.level().dimension() != preso.dim()) {
                it.remove();
                continue;
            }
            p.fallDistance = 0;
            if (p.isPassenger()) continue; // montado: o bicho manda; só a proteção vale
            if (p.position().distanceToSqr(preso.pos()) > SOLTO) {
                Vec3 v = preso.pos();
                p.connection.teleport(v.x, v.y, v.z, p.getYRot(), p.getXRot());
            }
            p.setDeltaMovement(Vec3.ZERO);
        }
    }
}
