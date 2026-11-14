package com.nikolas.odteste.criaturas;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.historia.vilacalada.ClimaVilaCalada;
import com.nikolas.odteste.sistemas.Exposicao;
import com.nikolas.odteste.sistemas.Fonte;
import com.nikolas.odteste.sistemas.MaquinasPerto;
import com.nikolas.odteste.sistemas.Paranormal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Quem chama o "O Que Segue" e cuida da recarga de cada jogador. Também conta as máquinas em volta
 * ("a tech chama o paranormal"). Tudo no servidor, só em memória: reiniciar zera as recargas (proposta para o teste).
 *
 * Leve: roda a cada 20 ticks, só nos jogadores online. Tentativa de aparecer a cada ~10 s por jogador;
 * contagem de máquinas a cada ~30 s por jogador (com cache por chunk em {@link MaquinasPerto}).
 */
public final class Segue {
    private static final int TICKS_TENTATIVA = 200;
    private static final int TICKS_MAQUINAS = 600;
    private static final int TENTATIVAS_LUGAR = 16;
    /** Lugar claro com outros jogadores: luz a partir disso. */
    private static final int LUZ_PROTEGE = 12;
    private static final double RAIO_PROTEGE = 16;

    private static final Map<UUID, Estado> ESTADOS = new HashMap<>();
    private static int relogio;

    private static final class Estado {
        @Nullable
        UUID entidade;
        long livreEm;
        long proximaTentativa;
        long proximaContagem;
        int maquinas;

        Estado(long agora, RandomSource sorte) {
            proximaTentativa = agora + TICKS_TENTATIVA + sorte.nextInt(TICKS_TENTATIVA);
            proximaContagem = agora + sorte.nextInt(TICKS_MAQUINAS);
        }
    }

    private Segue() {}

    public static void iniciar() {
        ConfigSegue.registrar();
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> aoTick(e.getServer()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p) tirarSemRecarga(p);
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
            ESTADOS.clear();
            MaquinasPerto.limpar();
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> registrar(e.getDispatcher()));
    }

    private static Estado estado(ServerPlayer p) {
        return ESTADOS.computeIfAbsent(p.getUUID(), k -> new Estado(p.server.overworld().getGameTime(), p.getRandom()));
    }

    // ---------- a cada segundo ----------

    private static void aoTick(MinecraftServer s) {
        if (++relogio % 20 != 0) return;
        ServerLevel mundo = s.overworld();
        long agora = mundo.getGameTime();
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            Estado e = estado(p);
            if (agora >= e.proximaContagem) {
                e.proximaContagem = agora + TICKS_MAQUINAS;
                contarMaquinas(p, e);
            }
            if (e.entidade != null) {
                Entity q = mundo.getEntity(e.entidade);
                if (q instanceof OQueSegue && q.isAlive()) continue;
                e.entidade = null;
            }
            if (agora < e.proximaTentativa) continue;
            e.proximaTentativa = agora + TICKS_TENTATIVA;
            if (!ConfigSegue.ligado() || agora < e.livreEm || !podeSerAlvo(p)) continue;
            double chance = RegrasSegue.chance(mundo.isNight(), DadosJogador.exposicao(p),
                    RegrasSegue.fatorMaquinas(e.maquinas), ConfigSegue.chance());
            if (chance > 0 && p.getRandom().nextDouble() < chance) chamar(p);
        }
    }

    private static void contarMaquinas(ServerPlayer p, Estado e) {
        if (!ConfigSegue.maquinasLigado() || p.isSpectator()) {
            e.maquinas = 0;
            return;
        }
        e.maquinas = MaquinasPerto.contar(p.serverLevel(), p.blockPosition(), ConfigSegue.raioMaquinas());
        float intensidade = RegrasSegue.intensidadeMaquinas(e.maquinas);
        if (intensidade > 0) Exposicao.contato(p, Fonte.MAQUINAS, Elemento.NENHUM, intensidade);
    }

    /** Alvo natural: no Overworld, sobrevivência, sem a tag do mestre, acordado e sozinho. */
    private static boolean podeSerAlvo(ServerPlayer p) {
        return p.isAlive() && !p.isSpectator() && !p.isCreative() && p.level().dimension() == Level.OVERWORLD
                && !p.getTags().contains(ClimaVilaCalada.TAG_MESTRE) && !p.isSleeping()
                && !temOutroPerto(p, ConfigSegue.raioSozinho());
    }

    private static boolean temOutroPerto(ServerPlayer p, double raio) {
        if (raio <= 0) return false;
        double r2 = raio * raio;
        for (ServerPlayer o : p.serverLevel().players()) {
            if (o != p && !o.isSpectator() && o.distanceToSqr(p) <= r2) return true;
        }
        return false;
    }

    /** Dormindo, ou num lugar claro com outro jogador perto: a espera dobra. */
    private static boolean protegido(ServerPlayer p) {
        return p.isSleeping() || (p.level().getMaxLocalRawBrightness(p.blockPosition()) >= LUZ_PROTEGE
                && temOutroPerto(p, RAIO_PROTEGE));
    }

    static double fatorMaquinas(ServerPlayer p) {
        Estado e = ESTADOS.get(p.getUUID());
        return e == null ? 0 : RegrasSegue.fatorMaquinas(e.maquinas);
    }

    // ---------- aparecer ----------

    /** Faz ela aparecer atrás do jogador (30–45 blocos, fora da vista). Só no Overworld. */
    public static boolean chamar(ServerPlayer p) {
        if (p.level().dimension() != Level.OVERWORLD) return false;
        ServerLevel n = p.serverLevel();
        Estado e = estado(p);
        tirarSemRecarga(p);
        Vec3 lugar = acharLugar(n, p);
        if (lugar == null) return false;
        OQueSegue q = Criaturas.O_QUE_SEGUE.get().create(n);
        if (q == null) return false;
        Vec3 d = p.position().subtract(lugar);
        q.moveTo(lugar.x, lugar.y, lugar.z, (float) (Math.atan2(d.z, d.x) * (180 / Math.PI)) - 90f, 0f);
        q.definirAlvo(p.getUUID(), n.getGameTime());
        if (!n.addFreshEntity(q)) return false;
        e.entidade = q.getUUID();
        return true;
    }

    @Nullable
    private static Vec3 acharLugar(ServerLevel n, ServerPlayer p) {
        RandomSource sorte = p.getRandom();
        int min = ConfigSegue.distanciaMin();
        int max = ConfigSegue.distanciaMax();
        float altura = Criaturas.O_QUE_SEGUE.get().getHeight();
        Vec3 reserva = null;
        for (int t = 0; t < TENTATIVAS_LUGAR; t++) {
            // Atrás do jogador, até 70 graus para cada lado.
            double ang = Math.toRadians(p.getYRot() + 180 + (sorte.nextDouble() * 2 - 1) * 70);
            double dist = min + sorte.nextDouble() * (max - min);
            double x = p.getX() - Math.sin(ang) * dist;
            double z = p.getZ() + Math.cos(ang) * dist;
            Vec3 chao = chao(n, x, z, p.getBlockY());
            if (chao == null) continue;
            if (olhar(p, chao, altura) >= RegrasSegue.DOT_CAMPO) continue;
            // Preferido: atrás de árvore, parede, morro (nem a cabeça aparece).
            if (!linhaLivre(p, chao.add(0, altura * 0.9, 0)) && !linhaLivre(p, chao.add(0, altura * 0.5, 0))) return chao;
            if (reserva == null) reserva = chao;
        }
        return reserva;
    }

    /** Chão firme perto da altura do jogador, com espaço para ela em pé e sem água. Nunca carrega chunk. */
    @Nullable
    private static Vec3 chao(ServerLevel n, double x, double z, int y0) {
        BlockPos coluna = BlockPos.containing(x, y0, z);
        if (!n.hasChunkAt(coluna)) return null;
        for (int dy = 6; dy >= -10; dy--) {
            BlockPos b = coluna.atY(y0 + dy);
            BlockPos baixo = b.below();
            if (!n.getBlockState(baixo).isFaceSturdy(n, baixo, Direction.UP) || !n.getFluidState(b).isEmpty()) continue;
            double cx = b.getX() + 0.5;
            double cz = b.getZ() + 0.5;
            if (n.noCollision(Criaturas.O_QUE_SEGUE.get().getSpawnAABB(cx, b.getY(), cz))) return new Vec3(cx, b.getY(), cz);
        }
        return null;
    }

    // ---------- olhar ----------

    /**
     * O quanto o jogador olha para um corpo em pé em {@code pe}: o maior "dot" (1 = bem no centro da tela) entre a cabeça
     * e o meio do corpo que ele enxerga de fato (sem bloco no meio; folha esconde). −2 se não vê nada.
     * Raio de visão só quando o ponto está no campo de visão.
     */
    static double olhar(ServerPlayer p, Vec3 pe, float altura) {
        Vec3 olho = p.getEyePosition();
        Vec3 frente = p.getViewVector(1f);
        double melhor = -2;
        for (double h : new double[] {altura * 0.9, altura * 0.5}) {
            Vec3 ponto = pe.add(0, h, 0);
            Vec3 para = ponto.subtract(olho);
            double len = para.length();
            if (len < 1e-3) return 1;
            double dot = frente.dot(para.scale(1 / len));
            if (dot < RegrasSegue.DOT_CAMPO || dot <= melhor) continue;
            if (linhaLivre(p, ponto)) melhor = dot;
        }
        return melhor;
    }

    static boolean visivel(ServerPlayer p, Vec3 pe, float altura) {
        return olhar(p, pe, altura) >= RegrasSegue.DOT_CAMPO;
    }

    private static boolean linhaLivre(ServerPlayer p, Vec3 ate) {
        return p.level().clip(new ClipContext(p.getEyePosition(), ate, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, p))
                .getType() == HitResult.Type.MISS;
    }

    // ---------- o que ela faz ----------

    /** Passos atrás do jogador, na direção dela. Só ele ouve. */
    static void passos(ServerPlayer p, Vec3 ela) {
        ServerLevel n = p.serverLevel();
        Vec3 dir = ela.subtract(p.position()).multiply(1, 0, 1);
        if (dir.lengthSqr() < 1e-4) dir = p.getViewVector(1f).reverse().multiply(1, 0, 1);
        if (dir.lengthSqr() < 1e-4) dir = new Vec3(0, 0, 1);
        Vec3 som = p.position().add(dir.normalize().scale(3));
        BlockPos baixo = BlockPos.containing(ela).below();
        BlockState bloco = n.getBlockState(baixo);
        SoundEvent passo = bloco.isAir() ? SoundEvents.STONE_STEP : bloco.getSoundType(n, baixo, null).getStepSound();
        RandomSource sorte = p.getRandom();
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(passo), SoundSource.HOSTILE,
                som.x, som.y, som.z, 0.6f, 0.5f + sorte.nextFloat() * 0.2f, sorte.nextLong()));
    }

    /** Encarada: o jogador viu algo que não devia (um pouco de Exposição). */
    static void foiVista(ServerPlayer p) {
        Exposicao.contato(p, Fonte.APARICAO, Elemento.NENHUM, 0.5f);
    }

    /** Pegou: escuridão, golpe forte (com a vida cheia não mata), som grave e Exposição. */
    static void pegar(ServerPlayer p, OQueSegue quem) {
        ServerLevel n = p.serverLevel();
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 10, 0, false, false), quem);
        float dano = RegrasSegue.dano(ConfigSegue.dano(), p.getHealth(), p.getMaxHealth());
        // Sem causador: o dano não muda com a dificuldade (com causador, no difícil passaria da vida cheia).
        if (dano > 0) p.hurt(Paranormal.fonte(n, null), dano);
        n.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1f, 0.5f);
        n.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.5f, 0.6f);
        Exposicao.contato(p, Fonte.APARICAO, Elemento.NENHUM, 1.5f);
    }

    /** Ela sumiu: marca quando pode voltar para esse jogador. */
    static void sumiu(Level nivel, UUID alvo, @Nullable ServerPlayer p, RegrasSegue.Motivo motivo) {
        MinecraftServer s = nivel.getServer();
        if (s == null) return;
        Estado e = ESTADOS.get(alvo);
        if (e == null) return;
        e.entidade = null;
        boolean protegido = p != null && protegido(p);
        e.livreEm = s.overworld().getGameTime()
                + RegrasSegue.recarga(motivo, ConfigSegue.recargaMin(), ConfigSegue.voltaMin(), protegido);
    }

    /** Tira a que está seguindo (se houver) sem mexer na recarga. */
    private static void tirarSemRecarga(ServerPlayer p) {
        Estado e = ESTADOS.get(p.getUUID());
        if (e == null || e.entidade == null) return;
        Entity q = p.server.overworld().getEntity(e.entidade);
        e.entidade = null;
        if (q != null) q.discard();
    }

    /** Afasta a que está seguindo (recarga normal). */
    public static boolean afastar(ServerPlayer p) {
        Estado e = ESTADOS.get(p.getUUID());
        if (e == null || e.entidade == null) return false;
        Entity q = p.server.overworld().getEntity(e.entidade);
        if (q instanceof OQueSegue segue && segue.isAlive()) {
            segue.sumir(p, RegrasSegue.Motivo.AFASTADA);
            return true;
        }
        e.entidade = null;
        return false;
    }

    // ---------- comando do admin ----------

    /** {@code /odteste segue chamar <jogador>} (ignora noite, recarga e a tag do mestre) e {@code /odteste segue afastar <jogador>}. */
    private static void registrar(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("odteste").requires(s -> s.hasPermission(2))
                .then(Commands.literal("segue").requires(s -> s.hasPermission(2))
                        .then(Commands.literal("chamar")
                                .then(Commands.argument("jogador", EntityArgument.player()).executes(Segue::cmdChamar)))
                        .then(Commands.literal("afastar")
                                .then(Commands.argument("jogador", EntityArgument.player()).executes(Segue::cmdAfastar)))));
    }

    private static int cmdChamar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "jogador");
        if (!chamar(p)) {
            c.getSource().sendFailure(Component.translatable("odteste.comando.segue.sem_lugar", p.getDisplayName()));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.segue.chamar", p.getDisplayName()), true);
        return 1;
    }

    private static int cmdAfastar(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "jogador");
        if (!afastar(p)) {
            c.getSource().sendFailure(Component.translatable("odteste.comando.segue.nenhuma", p.getDisplayName()));
            return 0;
        }
        c.getSource().sendSuccess(() -> Component.translatable("odteste.comando.segue.afastar", p.getDisplayName()), true);
        return 1;
    }
}
