package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.dados.DadosJogador;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Os gatilhos automáticos (só servidor). Sem lag:
 * - uma checagem por jogador a cada 2 s, espalhada pelos ticks (cada jogador num tick diferente);
 * - cada checagem olha só em volta do próprio jogador (caixa pequena, nunca o mundo inteiro);
 * - o resto são eventos raros (morrer, dormir, comer).
 */
public final class Vigia {
    /** Distância para "longe de outros jogadores" no escuro. */
    private static final double SOZINHO_RAIO = 24;
    /** Distância para "perto do grupo" (recupera Lucidez). */
    private static final double GRUPO_RAIO = 8;
    /** Distância do mob hostil à noite. */
    private static final double MOB_RAIO = 8;
    /** Luz forte (bloco ou céu). */
    private static final int LUZ_FORTE = 12;

    /** Contadores de cada jogador. Só memória; some quando ele sai. */
    private static final class Estado {
        int escuroSeguido;
        int grupoSeguido;
        int desdeUltimoMob = RegrasLucidez.MOB_INTERVALO;
    }

    private static final Map<UUID, Estado> ESTADOS = new HashMap<>();

    private Vigia() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(Vigia::aoTick);
        NeoForge.EVENT_BUS.addListener(Vigia::aoMorrer);
        NeoForge.EVENT_BUS.addListener(Vigia::aoComer);
        NeoForge.EVENT_BUS.addListener(Vigia::aoAmanhecer);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> {
            ESTADOS.remove(e.getEntity().getUUID());
            Exposicao.esquecer(e.getEntity().getUUID());
        });
    }

    private static void aoTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        // Espalha: cada jogador checa num tick diferente, uma vez a cada 2 s.
        if ((p.tickCount + p.getId()) % RegrasLucidez.TICKS_POR_CHECAGEM != 0) return;
        if (!p.isAlive()) return;

        MarcaSangue.checar(p);
        if (p.isCreative() || p.isSpectator()) return;

        Estado s = ESTADOS.computeIfAbsent(p.getUUID(), u -> new Estado());
        ServerLevel nivel = p.serverLevel();
        BlockPos olhos = BlockPos.containing(p.getEyePosition());
        int luz = nivel.getMaxLocalRawBrightness(olhos);

        // 1) Escuro total, longe de todo mundo: a Lucidez cai devagar e a Exposição sobe um pouco.
        boolean sozinho = nivel.getNearestPlayer(p.getX(), p.getY(), p.getZ(), SOZINHO_RAIO,
                o -> o != p && !o.isSpectator()) == null;
        if (luz == 0 && sozinho) {
            s.escuroSeguido++;
            Lucidez.perder(p, RegrasLucidez.perdaNoEscuro(s.escuroSeguido));
            if (RegrasLucidez.exposicaoNoEscuro(s.escuroSeguido)) Exposicao.ganhar(p, Fonte.ESCURO);
        } else {
            s.escuroSeguido = 0;
        }

        // 2) Luz forte e outro jogador perto: o grupo acalma.
        boolean grupo = luz >= LUZ_FORTE && nivel.getNearestPlayer(p.getX(), p.getY(), p.getZ(), GRUPO_RAIO,
                o -> o != p && !o.isSpectator()) != null;
        if (grupo) {
            if (++s.grupoSeguido % RegrasLucidez.GRUPO_INTERVALO == 0) Lucidez.recuperar(p, 1);
        } else {
            s.grupoSeguido = 0;
        }

        // 3) Mob hostil perto, à noite.
        s.desdeUltimoMob++;
        boolean noite = !nivel.dimensionType().hasFixedTime() && !nivel.isDay();
        if (s.desdeUltimoMob >= RegrasLucidez.MOB_INTERVALO && noite
                && !nivel.getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(MOB_RAIO), Monster::isAlive).isEmpty()) {
            s.desdeUltimoMob = 0;
            Exposicao.ganhar(p, Fonte.MOB_NOTURNO);
        }
    }

    private static void aoMorrer(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Exposicao.ganhar(p, Fonte.MORRER);
    }

    private static void aoComer(LivingEntityUseItemEvent.Finish e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        ItemStack item = e.getItem();
        if (item.is(Items.MUSHROOM_STEW) || item.is(Items.RABBIT_STEW) || item.is(Items.BEETROOT_SOUP)
                || item.is(Items.SUSPICIOUS_STEW)) {
            Lucidez.recuperar(p, RegrasLucidez.GANHO_COMIDA_QUENTE);
        }
    }

    /** A noite passou dormindo: quem estava na cama recupera metade da Lucidez máxima. */
    private static void aoAmanhecer(SleepFinishedTimeEvent e) {
        if (!(e.getLevel() instanceof ServerLevel nivel)) return;
        for (ServerPlayer p : nivel.players()) {
            if (p.isSleeping()) {
                Lucidez.recuperar(p, RegrasLucidez.ganhoAoDormir(DadosJogador.lucidezMax(p)));
            }
        }
    }
}
