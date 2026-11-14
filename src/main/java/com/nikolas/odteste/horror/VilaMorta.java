package com.nikolas.odteste.horror;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.vilacalada.ClimaVilaCalada;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * A Vila Calada está morta (pedido de 14/11/2026):
 * <ul>
 * <li>nenhum animal, pássaro ou morcego nasce sozinho dentro do muro (nascimento natural e de chunk novo);</li>
 * <li>bicho que entra fica parado, olhando o campanário, enquanto há jogador na vila.</li>
 * </ul>
 * Animal de jogador (domado, com nome, na guia, montado) fica de fora. Apanhou: solta na hora.
 * Nada fica salvo no bicho: a velocidade usa modificador temporário ({@code odteste:animal_parado}) e o resto é
 * estado da IA que some ao descarregar. Sem lag: procura bichos uma vez por segundo, no máximo {@link #MAX} parados,
 * e por tick só vira a cabeça desses.
 */
final class VilaMorta {
    static final int MAX = 24;
    private static final double RAIO_PROCURA = 24;
    private static final ResourceLocation MODIFICADOR =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsHorror.MODIFICADOR_ANIMAL);
    private static final List<Mob> PARADOS = new ArrayList<>();

    private VilaMorta() {}

    static void iniciar() {
        NeoForge.EVENT_BUS.addListener(VilaMorta::aoNascer);
        NeoForge.EVENT_BUS.addListener(VilaMorta::aoTick);
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent e) -> soltarTodos());
    }

    private static boolean bicho(Mob m) {
        return m instanceof Animal || m instanceof AmbientCreature;
    }

    private static void aoNascer(FinalizeSpawnEvent e) {
        MobSpawnType tipo = e.getSpawnType();
        if (tipo != MobSpawnType.NATURAL && tipo != MobSpawnType.CHUNK_GENERATION) return;
        if (!bicho(e.getEntity())) return;
        ServerLevel nivel = e.getLevel().getLevel();
        if (ClimaVilaCalada.naVila(nivel, e.getX(), e.getY(), e.getZ())) e.setSpawnCancelled(true);
    }

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        Vec3 alvo = ClimaVilaCalada.campanario();
        if (alvo == null || !ClimaVilaCalada.alguemDentro()) {
            if (!PARADOS.isEmpty()) soltarTodos();
            return;
        }
        // Todo tick: só a cabeça dos parados (no máximo MAX).
        Iterator<Mob> it = PARADOS.iterator();
        while (it.hasNext()) {
            Mob m = it.next();
            if (!podeFicar(m)) {
                soltar(m);
                it.remove();
                continue;
            }
            travar(m); // o jogo pode religar as travas sozinho (montaria, barco): confere todo tick
            m.getNavigation().stop();
            m.getLookControl().setLookAt(alvo.x, alvo.y, alvo.z, 10f, 40f);
        }
        if (s.getTickCount() % 20 != 5) return;
        ServerLevel nivel = ClimaVilaCalada.nivelDaVila();
        if (nivel == null) return;
        for (ServerPlayer p : nivel.players()) {
            if (PARADOS.size() >= MAX) break;
            if (!ClimaVilaCalada.estaDentro(p)) continue;
            for (Mob m : nivel.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(RAIO_PROCURA),
                    m -> bicho(m) && !PARADOS.contains(m) && livre(m)
                            && ClimaVilaCalada.naVila(nivel, m.getX(), m.getY(), m.getZ()))) {
                if (PARADOS.size() >= MAX) break;
                parar(m, alvo);
                PARADOS.add(m);
            }
        }
    }

    /** Bicho sem dono, sem nome, no chão, sem guia, sem montaria e sem apanhar agora. */
    private static boolean livre(Mob m) {
        if (!m.isAlive() || m.isNoAi() || m.hasCustomName() || m.isLeashed() || m.isPassenger() || m.isVehicle()) return false;
        if (m instanceof TamableAnimal t && t.isTame()) return false;
        return m.onGround() && m.hurtTime == 0;
    }

    private static boolean podeFicar(Mob m) {
        return !m.isRemoved() && m.isAlive() && m.hurtTime == 0 && !m.isLeashed() && !m.isPassenger()
                && m.level() == ClimaVilaCalada.nivelDaVila()
                && ClimaVilaCalada.naVila((ServerLevel) m.level(), m.getX(), m.getY(), m.getZ());
    }

    private static void parar(Mob m, Vec3 alvo) {
        AttributeInstance vel = m.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null && !vel.hasModifier(MODIFICADOR)) {
            vel.addTransientModifier(new AttributeModifier(MODIFICADOR, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        travar(m);
        m.getNavigation().stop();
        float giro = (float) (Mth.atan2(alvo.z - m.getZ(), alvo.x - m.getX()) * Mth.RAD_TO_DEG) - 90f;
        m.setYRot(giro);
        m.setYBodyRot(giro);
        m.setYHeadRot(giro);
    }

    /** Sem andar, pular nem olhar em volta: só o campanário. Estado da IA, não vai para o disco. */
    private static void travar(Mob m) {
        m.goalSelector.disableControlFlag(Goal.Flag.MOVE);
        m.goalSelector.disableControlFlag(Goal.Flag.JUMP);
        m.goalSelector.disableControlFlag(Goal.Flag.LOOK);
    }

    private static void soltar(Mob m) {
        if (m.isRemoved()) return;
        AttributeInstance vel = m.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null) vel.removeModifier(MODIFICADOR);
        m.goalSelector.enableControlFlag(Goal.Flag.MOVE);
        m.goalSelector.enableControlFlag(Goal.Flag.JUMP);
        m.goalSelector.enableControlFlag(Goal.Flag.LOOK);
    }

    private static void soltarTodos() {
        for (Mob m : PARADOS) soltar(m);
        PARADOS.clear();
    }
}
