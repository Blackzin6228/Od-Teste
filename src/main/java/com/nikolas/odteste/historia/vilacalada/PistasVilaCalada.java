package com.nikolas.odteste.historia.vilacalada;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.estruturas.Colocacao;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import com.nikolas.odteste.historia.IdsHistoria;
import com.nikolas.odteste.historia.VilaMarcada;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * Ato 1: as três pistas que levam o grupo da vila comum até a Vila Calada (lore/fichas-estruturas.md). Só servidor.
 *
 * <ol>
 * <li><b>Sino rachado ao longe:</b> de noite, de tempo em tempo, cada membro ouve um toque grave e desafinado vindo
 * sempre da direção da vila, mais alto quanto mais perto. Som só para ele (pacote direto): nada muda no mundo.</li>
 * <li><b>Aldeões virados:</b> de tempo em tempo (e quando o sino toca), os aldeões em volta de um membro param uns
 * segundos, todos de frente para a vila. O freio é um modificador temporário (o jogo não salva) e o olhar é uma
 * memória que o jogo não salva: nenhum aldeão fica preso.</li>
 * <li><b>Cama manchada:</b> perto da cama do aldeão que sumiu no Primeiro Sinal, gotas vermelhas no chão fazem um
 * rastro curto da cama na direção da vila. Partícula só para quem está perto; nenhum bloco muda.</li>
 * </ol>
 * Valem para grupos que já tiveram o Primeiro Sinal e ainda não chegaram. Quem chega (a {@link RegrasVilaCalada#RAIO_CHEGADA}
 * do ponto) dá o marco {@code vila_calada} ao grupo: as pistas de longe param; o sino segue tocando só na vila.
 *
 * O ponto vem da âncora {@code vila_calada}: gravada sozinha quando uma estrutura {@code vila_calada*} termina de
 * ser colocada (lendo as colocações salvas a cada 5 s; não mexe em estruturas/), ou à mão pelo admin.
 *
 * Sem lag: tudo roda uma vez por segundo, só para membros online de grupos (teto {@link RegrasVilaCalada#MAX_JOGADORES});
 * a busca de aldeões é uma caixa pequena em volta do jogador, só quando a olhada começa.
 */
public final class PistasVilaCalada {
    /** Só memória, por jogador. */
    private static final class Estado {
        long proximoToque = -1;
        long proximaOlhada = -1;
        long olhadaAte;
        final List<Villager> virados = new ArrayList<>();
    }

    private static final Map<UUID, Estado> ESTADOS = new HashMap<>();
    private static final Set<UUID> VIRADOS_IDS = new HashSet<>();
    static final ResourceLocation MODIFICADOR_VIRADO =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsVilaCalada.MODIFICADOR_ALDEAO_VIRADO);
    /** O Primeiro Sinal para os aldeões perto do sino olhando para ele: esses não são nossos. */
    private static final ResourceLocation MODIFICADOR_PARADO =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsHistoria.MODIFICADOR_ALDEAO_PARADO);
    private static final DustParticleOptions GOTA = new DustParticleOptions(new Vector3f(0.40f, 0.02f, 0.02f), 0.8f);

    private static DadosVilaCalada cache;

    private PistasVilaCalada() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(PistasVilaCalada::aoTick);
        NeoForge.EVENT_BUS.addListener(ComandosVilaCalada::aoRegistrarComandos);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> {
            Estado st = ESTADOS.remove(e.getEntity().getUUID());
            if (st != null) soltar(st);
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent e) -> soltarTodos());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
            ESTADOS.clear();
            VIRADOS_IDS.clear();
            cache = null;
        });
    }

    static DadosVilaCalada dados(MinecraftServer s) {
        if (cache == null) cache = DadosVilaCalada.de(s);
        return cache;
    }

    // ------------------------------------------------------------------ tick

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        int tick = s.getTickCount();
        if (tick % 20 != 0) return;
        DadosHistoria dh = DadosHistoria.de(s);
        DadosVilaCalada dv = dados(s);
        if (tick % 100 == 0) conferirColocacoes(s, dh, dv);
        GlobalPos ancora = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        ServerLevel nivel = ancora == null ? null : s.getLevel(ancora.dimension());
        if (!dv.ligado || nivel == null) {
            if (!ESTADOS.isEmpty()) soltarTodos();
            return;
        }
        long agora = nivel.getGameTime();
        Set<UUID> vistos = new HashSet<>();
        int n = 0;
        for (Grupos.Grupo g : dh.grupos.todos()) {
            if (!g.tem(IdsHistoria.MARCO_PRIMEIRO_SINAL)) continue;
            for (UUID u : g.membros().keySet()) {
                if (n >= RegrasVilaCalada.MAX_JOGADORES) break;
                ServerPlayer p = s.getPlayerList().getPlayer(u);
                if (p == null || p.isSpectator() || p.level() != nivel) continue;
                n++;
                vistos.add(u);
                Estado st = ESTADOS.computeIfAbsent(u, k -> new Estado());
                double dist = distanciaHorizontal(p.position(), ancora.pos());
                boolean chegou = g.tem(IdsVilaCalada.MARCO_CHEGOU);
                if (!chegou && dist <= RegrasVilaCalada.RAIO_CHEGADA && dh.grupos.marcar(g.nome(), IdsVilaCalada.MARCO_CHEGOU)) {
                    dh.setDirty();
                    chegou = true;
                    OdMod.LOGGER.info("Vila Calada: o grupo {} chegou ({})", g.nome(), p.getGameProfile().getName());
                }
                sino(nivel, p, st, ancora.pos(), dist, chegou, agora);
                if (chegou) {
                    soltar(st);
                } else {
                    aldeoes(nivel, p, st, ancora.pos(), agora);
                    cama(nivel, p, dh.vilas.get(g.nome()), ancora.pos());
                }
            }
        }
        // Quem saiu do grupo, da dimensão ou do servidor: os aldeões dele voltam ao normal.
        Iterator<Map.Entry<UUID, Estado>> it = ESTADOS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Estado> en = it.next();
            if (!vistos.contains(en.getKey())) {
                soltar(en.getValue());
                it.remove();
            }
        }
    }

    private static double distanciaHorizontal(Vec3 de, BlockPos para) {
        double dx = para.getX() + 0.5 - de.x;
        double dz = para.getZ() + 0.5 - de.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    // ------------------------------------------------------------------ âncora pela colocação da estrutura

    /**
     * Lê as colocações salvas: estrutura {@code vila_calada*} pronta e ainda não vista ancora a vila no centro dela
     * (altura do chão da estrutura). Se a estrutura que ancorou for desfeita e a âncora ainda for a dela, a âncora sai.
     */
    private static void conferirColocacoes(MinecraftServer s, DadosHistoria dh, DadosVilaCalada dv) {
        if (!dv.colocacao.isEmpty()) {
            Colocacao col = dh.colocacoes.get(dv.colocacao);
            boolean saiu = col == null || (col.partes() != null && col.partes().fase == Colocacao.Fase.DESFAZENDO);
            if (saiu) {
                GlobalPos a = dh.ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
                if (a != null && dv.ancoraAuto != null && a.pos().asLong() == dv.ancoraAuto) {
                    dh.ancoras.remove(IdsVilaCalada.LUGAR_VILA_CALADA);
                    dh.setDirty();
                    OdMod.LOGGER.info("Vila Calada: a estrutura {} foi desfeita; âncora tirada (pistas param)", dv.colocacao);
                }
                dv.colocacao = "";
                dv.ancoraAuto = null;
                dv.setDirty();
            }
        }
        for (Colocacao col : dh.colocacoes.values()) {
            if (!RegrasVilaCalada.templateDaVila(col.template()) || dv.vistas.contains(col.id())) continue;
            if (col.partes() != null && col.partes().fase != Colocacao.Fase.PRONTA) continue;
            ServerLevel nl = s.getLevel(col.dimensao());
            if (nl == null) continue;
            BlockPos centro = centro(nl, col);
            dv.vistas.add(col.id());
            dv.colocacao = col.id();
            dv.ancoraAuto = centro.asLong();
            dv.setDirty();
            dh.ancoras.put(IdsVilaCalada.LUGAR_VILA_CALADA, GlobalPos.of(col.dimensao(), centro));
            dh.setDirty();
            OdMod.LOGGER.info("Vila Calada: ancorada pela estrutura {} em {} ({})", col.id(), centro.toShortString(),
                    col.dimensao().location());
            Component msg = Component.translatable("odteste.vila_calada.ancorada_pela_estrutura", col.id(),
                    centro.getX(), centro.getY(), centro.getZ()).withStyle(ChatFormatting.GRAY);
            UUID admin = col.partes() == null ? null : col.partes().admin;
            ServerPlayer j = admin == null ? null : s.getPlayerList().getPlayer(admin);
            if (j != null) j.sendSystemMessage(msg);
            return; // uma por vez
        }
    }

    /** Centro da caixa da estrutura, na altura do canto. Template sumiu: o próprio canto. */
    private static BlockPos centro(ServerLevel nivel, Colocacao col) {
        ResourceLocation rl = ResourceLocation.tryParse(col.template());
        Optional<StructureTemplate> t = rl == null ? Optional.empty() : nivel.getStructureManager().get(rl);
        if (t.isEmpty()) return col.canto();
        Rotation r = col.partes() == null ? Rotation.NONE : col.partes().rotacao;
        BoundingBox b = t.get().getBoundingBox(new StructurePlaceSettings().setRotation(r), col.canto());
        return new BlockPos((b.minX() + b.maxX()) / 2, col.canto().getY(), (b.minZ() + b.maxZ()) / 2);
    }

    // ------------------------------------------------------------------ 1. sino rachado ao longe

    private static void sino(ServerLevel nivel, ServerPlayer p, Estado st, BlockPos ancora, double dist, boolean chegou, long agora) {
        if (nivel.dimensionType().hasFixedTime() || !RegrasVilaCalada.noite(nivel.getDayTime())) {
            st.proximoToque = -1; // a próxima noite começa do zero
            return;
        }
        if (!RegrasVilaCalada.ouve(dist, chegou)) return;
        if (st.proximoToque < 0) {
            st.proximoToque = agora + RegrasVilaCalada.primeiroToque(nivel.getRandom().nextDouble());
            return;
        }
        if (agora < st.proximoToque) return;
        st.proximoToque = agora + RegrasVilaCalada.proximoToque(nivel.getRandom().nextDouble());
        tocar(nivel, p, ancora);
        // Os aldeões acordados em volta viram para o som.
        if (!chegou && st.virados.isEmpty()) olhada(nivel, p, st, ancora, agora);
    }

    /** Um toque do sino rachado só para este jogador (também usado pelo comando de teste do admin). */
    static void tocar(ServerLevel nivel, ServerPlayer p, BlockPos ancora) {
        Vec3 olhos = p.getEyePosition();
        double dist = distanciaHorizontal(olhos, ancora);
        Vec3 onde;
        float volume;
        if (dist <= RegrasVilaCalada.RAIO_SOM_DIRETO) {
            onde = Vec3.atCenterOf(ancora).add(0, 8, 0); // o campanário fica no alto
            volume = RegrasVilaCalada.VOLUME_NA_VILA;
        } else {
            double[] d = RegrasVilaCalada.direcao(ancora.getX() + 0.5 - olhos.x, ancora.getZ() + 0.5 - olhos.z);
            onde = olhos.add(d[0] * RegrasVilaCalada.DISTANCIA_FONTE, 1, d[1] * RegrasVilaCalada.DISTANCIA_FONTE);
            volume = RegrasVilaCalada.volumeLonge(dist);
        }
        if (volume <= 0f) return;
        long semente = nivel.getRandom().nextLong();
        p.connection.send(new ClientboundSoundPacket(som(SoundEvents.BELL_BLOCK), SoundSource.AMBIENT,
                onde.x, onde.y, onde.z, volume, RegrasVilaCalada.TOM_GRAVE, semente));
        p.connection.send(new ClientboundSoundPacket(som(SoundEvents.BELL_BLOCK), SoundSource.AMBIENT,
                onde.x, onde.y, onde.z, volume * 0.6f, RegrasVilaCalada.TOM_RACHA, semente));
        p.connection.send(new ClientboundSoundPacket(som(SoundEvents.BELL_RESONATE), SoundSource.AMBIENT,
                onde.x, onde.y, onde.z, volume * 0.5f, RegrasVilaCalada.TOM_GRAVE, semente));
    }

    private static Holder<SoundEvent> som(SoundEvent s) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(s);
    }

    // ------------------------------------------------------------------ 2. aldeões virados para lá

    private static void aldeoes(ServerLevel nivel, ServerPlayer p, Estado st, BlockPos ancora, long agora) {
        if (!st.virados.isEmpty()) {
            if (agora >= st.olhadaAte) soltar(st);
            else manter(st, ancora);
        }
        if (st.proximaOlhada < 0) {
            // Primeira vez que o mod vê o jogador: a primeira olhada vem mais cedo.
            st.proximaOlhada = agora + RegrasVilaCalada.proximaOlhada(nivel.getRandom().nextDouble()) / 2;
        }
        if (st.virados.isEmpty() && agora >= st.proximaOlhada) olhada(nivel, p, st, ancora, agora);
    }

    private static void olhada(ServerLevel nivel, ServerPlayer p, Estado st, BlockPos ancora, long agora) {
        st.proximaOlhada = agora + RegrasVilaCalada.proximaOlhada(nivel.getRandom().nextDouble());
        List<Villager> perto = nivel.getEntitiesOfClass(Villager.class,
                p.getBoundingBox().inflate(RegrasVilaCalada.RAIO_ALDEOES), PistasVilaCalada::podeVirar);
        if (perto.isEmpty()) return;
        perto.sort(Comparator.comparingDouble(a -> a.distanceToSqr(p)));
        for (Villager a : perto) {
            if (st.virados.size() >= RegrasVilaCalada.MAX_ALDEOES_POR_JOGADOR) break;
            virar(a, ancora);
            st.virados.add(a);
            VIRADOS_IDS.add(a.getUUID());
        }
        st.olhadaAte = agora + RegrasVilaCalada.OLHADA_DURA;
    }

    /** Aldeão livre: acordado, sem troca aberta, sem dano agora, sem ser do Primeiro Sinal nem de outro jogador. */
    private static boolean podeVirar(Villager a) {
        return a.isAlive() && !a.isSleeping() && a.getTradingPlayer() == null && !a.isPassenger() && a.hurtTime == 0
                && !VIRADOS_IDS.contains(a.getUUID()) && !temModificador(a, MODIFICADOR_PARADO);
    }

    private static boolean temModificador(Villager a, ResourceLocation id) {
        AttributeInstance vel = a.getAttribute(Attributes.MOVEMENT_SPEED);
        return vel != null && vel.hasModifier(id);
    }

    private static void virar(Villager a, BlockPos ancora) {
        AttributeInstance vel = a.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null && !vel.hasModifier(MODIFICADOR_VIRADO)) {
            vel.addTransientModifier(new AttributeModifier(MODIFICADOR_VIRADO, -1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        a.getNavigation().stop();
        a.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        Vec3 alvo = alvo(a, ancora);
        float giro = (float) (Mth.atan2(alvo.z - a.getZ(), alvo.x - a.getX()) * Mth.RAD_TO_DEG) - 90f;
        a.setYRot(giro);
        a.setYHeadRot(giro);
        a.setYBodyRot(giro);
        olhar(a, alvo);
    }

    /** Um ponto longe, na altura dos olhos, na direção da vila. */
    private static Vec3 alvo(Villager a, BlockPos ancora) {
        double[] d = RegrasVilaCalada.direcao(ancora.getX() + 0.5 - a.getX(), ancora.getZ() + 0.5 - a.getZ());
        return new Vec3(a.getX() + d[0] * RegrasVilaCalada.OLHAR_LONGE, a.getEyeY(), a.getZ() + d[1] * RegrasVilaCalada.OLHAR_LONGE);
    }

    private static void olhar(Villager a, Vec3 alvo) {
        a.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(BlockPos.containing(alvo)));
        a.getLookControl().setLookAt(alvo.x, alvo.y, alvo.z);
    }

    /** Uma vez por segundo: segue olhando; quem dormiu, foi trocar, apanhou ou virou do Primeiro Sinal sai. */
    private static void manter(Estado st, BlockPos ancora) {
        Iterator<Villager> it = st.virados.iterator();
        while (it.hasNext()) {
            Villager a = it.next();
            if (a.isRemoved() || !a.isAlive() || a.isSleeping() || a.getTradingPlayer() != null || a.hurtTime > 0
                    || temModificador(a, MODIFICADOR_PARADO)) {
                soltar(a);
                it.remove();
                continue;
            }
            a.getNavigation().stop();
            olhar(a, alvo(a, ancora));
        }
    }

    private static void soltar(Villager a) {
        VIRADOS_IDS.remove(a.getUUID());
        if (a.isRemoved()) return;
        AttributeInstance vel = a.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null) vel.removeModifier(MODIFICADOR_VIRADO);
        a.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    private static void soltar(Estado st) {
        for (Villager a : st.virados) soltar(a);
        st.virados.clear();
    }

    private static void soltarTodos() {
        for (Estado st : ESTADOS.values()) soltar(st);
        ESTADOS.clear();
        VIRADOS_IDS.clear();
    }

    // ------------------------------------------------------------------ 3. cama manchada

    /**
     * A cama do aldeão que sumiu: um rastro curto de gotas no chão, da cama para o lado da vila. Só lê
     * {@link RegrasVilaCalada#GOTAS} blocos em volta, uma vez por segundo, com alguém do grupo perto.
     */
    private static void cama(ServerLevel nivel, ServerPlayer p, VilaMarcada v, BlockPos ancora) {
        if (v == null || v.cama == null || v.dimensao != nivel.dimension()) return;
        BlockPos cama = v.cama;
        if (p.position().distanceToSqr(Vec3.atCenterOf(cama)) > RegrasVilaCalada.RAIO_CAMA * RegrasVilaCalada.RAIO_CAMA) return;
        if (!nivel.hasChunk(cama.getX() >> 4, cama.getZ() >> 4)) return;
        if (!nivel.getBlockState(cama).is(BlockTags.BEDS)) return;
        double[] d = RegrasVilaCalada.direcao(ancora.getX() - cama.getX(), ancora.getZ() - cama.getZ());
        // Mancha no lençol, do lado de lá.
        nivel.sendParticles(p, GOTA, false, cama.getX() + 0.5 + d[0] * 0.3, cama.getY() + 0.6,
                cama.getZ() + 0.5 + d[1] * 0.3, 2, 0.15, 0.0, 0.15, 0);
        for (int i = 1; i <= RegrasVilaCalada.GOTAS; i++) {
            double x = cama.getX() + 0.5 + d[0] * (i + 0.5);
            double z = cama.getZ() + 0.5 + d[1] * (i + 0.5);
            BlockPos coluna = BlockPos.containing(x, cama.getY(), z);
            if (!nivel.hasChunk(coluna.getX() >> 4, coluna.getZ() >> 4)) return;
            BlockPos chao = chao(nivel, coluna);
            if (chao == null) return; // parede ou buraco: o rastro para ali
            nivel.sendParticles(p, GOTA, false, x, chao.getY() + 0.05, z, 1, 0.08, 0.0, 0.08, 0);
        }
    }

    /** Lugar sem colisão com chão firme embaixo, na altura da cama ou um bloco acima/abaixo. */
    private static BlockPos chao(ServerLevel nivel, BlockPos coluna) {
        for (int dy : new int[] {0, -1, 1}) {
            BlockPos b = coluna.offset(0, dy, 0);
            BlockState s = nivel.getBlockState(b);
            if (!s.getCollisionShape(nivel, b).isEmpty()) continue;
            BlockPos baixo = b.below();
            if (nivel.getBlockState(baixo).isFaceSturdy(nivel, baixo, Direction.UP)) return b;
        }
        return null;
    }

    // ------------------------------------------------------------------ para os comandos

    /** Quantos aldeões estão virados agora (para o {@code ver} do admin). */
    static int viradosAgora() {
        return VIRADOS_IDS.size();
    }

    /** Desligar: todos os aldeões voltam ao normal na hora. */
    static void aoDesligar() {
        soltarTodos();
    }
}
