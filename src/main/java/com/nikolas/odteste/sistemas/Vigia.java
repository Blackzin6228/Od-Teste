package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Os gatilhos automáticos da Lucidez e da Marca de Sangue (só servidor). Sem lag:
 * - uma checagem por jogador a cada 2 s, espalhada pelos ticks (cada jogador num tick diferente);
 * - cada checagem olha só em volta do próprio jogador (nunca o mundo inteiro);
 * - o resto são eventos (tomar dano, dormir, comer).
 *
 * A Exposição **não** sobe sozinha aqui: ela é só contato com o paranormal (Exposicao.contato).
 */
public final class Vigia {
    /** Distância para "perto do grupo" (recupera Lucidez). */
    private static final double GRUPO_RAIO = 8;
    /** Luz forte (bloco ou céu). */
    private static final int LUZ_FORTE = 12;

    /**
     * Comida quente que acalma (tag {@code odteste:comida_quente}). Hoje: ensopados, batata assada e carnes cozidas vanilla; comidas de outros
     * mods entram por datapack ou pelo compat, somando na tag ({@code "replace": false}), sem mexer aqui.
     */
    public static final TagKey<Item> COMIDA_QUENTE = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.TAG_COMIDA_QUENTE));

    /** Contadores de cada jogador. Só memória; some quando ele sai. */
    private static final class Estado {
        int grupoSeguido;
        int semDano;
        int parado;
        int restoDano;
        Vec3 ultimaPosicao;
    }

    private static final Map<UUID, Estado> ESTADOS = new HashMap<>();

    private Vigia() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(Vigia::aoTick);
        NeoForge.EVENT_BUS.addListener(Vigia::aoTomarDano);
        NeoForge.EVENT_BUS.addListener(Vigia::aoComer);
        NeoForge.EVENT_BUS.addListener(Vigia::aoAmanhecer);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> {
            ESTADOS.remove(e.getEntity().getUUID());
            Lucidez.esquecer(e.getEntity().getUUID());
        });
    }

    private static Estado estado(ServerPlayer p) {
        return ESTADOS.computeIfAbsent(p.getUUID(), u -> new Estado());
    }

    private static void aoTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        // Espalha: cada jogador checa num tick diferente, uma vez a cada 2 s.
        if ((p.tickCount + p.getId()) % RegrasLucidez.TICKS_POR_CHECAGEM != 0) return;
        if (!p.isAlive()) return;

        MarcaSangue.checar(p);
        if (p.isCreative() || p.isSpectator()) return;

        Estado s = estado(p);
        ServerLevel nivel = p.serverLevel();

        // 1) Descanso: parado e sem tomar dano há um minuto, a Lucidez volta devagar.
        s.semDano++;
        Vec3 aqui = p.position();
        boolean parou = s.ultimaPosicao != null && s.ultimaPosicao.distanceTo(aqui) < RegrasLucidez.DESCANSO_PARADO;
        s.parado = parou ? s.parado + 1 : 0;
        s.ultimaPosicao = aqui;
        if (RegrasLucidez.recuperaDescansando(s.semDano, s.parado)) Lucidez.recuperar(p, 1);

        // 2) Luz forte e outro jogador perto: o grupo acalma.
        int luz = nivel.getMaxLocalRawBrightness(BlockPos.containing(p.getEyePosition()));
        boolean grupo = luz >= LUZ_FORTE && nivel.getNearestPlayer(p.getX(), p.getY(), p.getZ(), GRUPO_RAIO,
                o -> o != p && !o.isSpectator()) != null;
        if (grupo) {
            if (++s.grupoSeguido % RegrasLucidez.GRUPO_INTERVALO == 0) Lucidez.recuperar(p, 1);
        } else {
            s.grupoSeguido = 0;
        }
    }

    /** Tomar dano abala: pouco se for comum, mais se for paranormal. Só servidor. */
    private static void aoTomarDano(LivingDamageEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.isCreative() || p.isSpectator()) return;
        Estado s = estado(p);
        s.semDano = 0;
        if (Lucidez.protegido(p)) return; // logo depois do surto, dano não abala
        int centesimos = RegrasLucidez.perdaPorDano(e.getNewDamage(), Paranormal.dano(e.getSource()));
        RegrasLucidez.Acumulo a = RegrasLucidez.acumular(s.restoDano, centesimos);
        s.restoDano = a.resto();
        Lucidez.perder(p, a.pontos());
    }

    /** Comer algo da tag {@link #COMIDA_QUENTE} devolve um pouco de Lucidez. */
    private static void aoComer(LivingEntityUseItemEvent.Finish e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        ItemStack item = e.getItem();
        if (item.is(COMIDA_QUENTE)) Lucidez.recuperar(p, RegrasLucidez.GANHO_COMIDA_QUENTE);
    }

    /** A noite passou dormindo: quem estava na cama recupera metade da Lucidez máxima. */
    private static void aoAmanhecer(SleepFinishedTimeEvent e) {
        if (!(e.getLevel() instanceof ServerLevel nivel)) return;
        for (ServerPlayer p : nivel.players()) {
            if (p.isSleeping()) Lucidez.recuperar(p, RegrasLucidez.ganhoAoDormir(DadosJogador.lucidezMax(p)));
        }
    }
}
