package com.nikolas.odteste.historia.vilacalada;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import com.nikolas.odteste.historia.mestre.Mestre;
import com.nikolas.odteste.historia.mestre.RegrasMestre;
import com.nikolas.odteste.itens.Itens;
import com.nikolas.odteste.rede.PenduradoSino;
import com.nikolas.odteste.sistemas.Lucidez;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * O Dobre da Vila Calada (Proposta A, decisão {@code 2026-11-14-enigma-vila-calada-dobre}). Só servidor; regras em
 * {@link RegrasDobre}.
 *
 * <ul>
 * <li><b>Sino rachado:</b> o sino do campanário da Vila Calada (reconhecido pelo atril no lugar certo ao lado).
 * Bater nele com o Martelo do Ferreiro é um toque do dobre; com a mão ou outra coisa, o sino toca normal e depois
 * <b>sozinho uma segunda vez</b> (pág. 14).</li>
 * <li><b>Toque certo:</b> grave e demorado, fere quem bate, as criaturas de Sangue param e olham a torre.</li>
 * <li><b>Erro:</b> rápido demais ou fora da meia-noite = o sino <b>chama</b> (as criaturas sobem até a torre);
 * toque a mais ou conta errada = o sino toca sozinho mais uma vez e a Lucidez cai em quem ouviu. Rápido e conta
 * errada gastam a noite. Errar nunca apaga progresso.</li>
 * <li><b>Feito:</b> as criaturas se ajoelham quando o eco chega (somem como cinza na manhã seguinte), a vila se
 * cala para o grupo (marco {@code dobre}, lido pelo {@link ClimaVilaCalada}), o Moinho range ao longe.
 * Evento {@link DobreEvento.Feito} para a cutscene.</li>
 * <li><b>Empurrões:</b> cliques do peitoril mais altos (2 noites sem tentar), bigorna virada para o campanário
 * (tentou com outra coisa), corrente caída na cova do Sineiro (2 contagens erradas), Pendurado em cima do sino
 * (só o mestre).</li>
 * </ul>
 * Sem lag: o golpe só faz conta quando alguém bate no sino; o resto roda uma vez por segundo, com listas pequenas
 * (teto {@link RegrasDobre#MAX_CRIATURAS}); a busca do canto da vila roda no máximo uma vez por minuto.
 */
public final class Dobre {
    public static final TagKey<EntityType<?>> OUVE = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsVilaCalada.TAG_OUVE_DOBRE));
    static final ResourceLocation MODIFICADOR =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsVilaCalada.MODIFICADOR_DOBRE);

    /** Um dobre em andamento (só memória). */
    private record Corrente(RegrasDobre.Andamento a, ServerLevel nivel, BlockPos sino, BlockPos canto) {}

    private record Agendado(long quando, Runnable acao) {}

    private static final Map<String, Corrente> ANDAMENTO = new HashMap<>();
    private static final PriorityQueue<Agendado> AGENDA =
            new PriorityQueue<>((x, y) -> Long.compare(x.quando(), y.quando()));
    /** Paradas pelo toque (até o tick). */
    private static final Map<Mob, Long> PARADOS = new HashMap<>();
    /**
     * Ajoelhadas pelo dobre feito (até virar cinza), só as carregadas agora. O que vale é a marca salva na própria
     * criatura ({@link #TAG_AJOELHADO}): quem descarrega com o chunk ou com o servidor volta para cá ao carregar.
     */
    private static final Set<Mob> AJOELHADOS = new HashSet<>();
    /** Marca salva na criatura (dados persistentes do NeoForge): dia do dobre e o sino. Sem a marca = criatura normal. */
    static final String TAG_AJOELHADO = "odteste:ajoelhado";
    /** Último chamado de cada grupo (tick do servidor). */
    private static final Map<String, Long> CHAMADO = new HashMap<>();
    /** Próximos cliques do peitoril de cada grupo (tick do servidor). */
    private static final Map<String, Long> CLIQUES = new HashMap<>();
    /** Grupos com alguém dentro da vila nesta noite; e grupos que tentaram (grupo:dia). */
    private static final Set<String> PRESENTES_NOITE = new HashSet<>();
    private static final Set<String> TENTOU = new HashSet<>();
    /** Segunda badalada sozinha marcada (não empilha). */
    private static long ecoSozinhoAte = -1;

    private static BlockPos cantoCache;
    private static ServerLevel nivelCache;
    private static long proximaBusca;

    private Dobre() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(Dobre::aoUsarBloco);
        NeoForge.EVENT_BUS.addListener(Dobre::aoTick);
        NeoForge.EVENT_BUS.addListener(Dobre::aoEntrarNoMundo);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
            ANDAMENTO.clear();
            AGENDA.clear();
            PARADOS.clear();
            AJOELHADOS.clear();
            CHAMADO.clear();
            CLIQUES.clear();
            PRESENTES_NOITE.clear();
            TENTOU.clear();
            ecoSozinhoAte = -1;
            cantoCache = null;
            nivelCache = null;
            proximaBusca = 0;
        });
    }

    // ------------------------------------------------------------------ o sino rachado

    /** Canto da vila se este sino for o sino rachado (atril no lugar ao lado, perto da âncora). */
    static Optional<BlockPos> cantoDoSino(ServerLevel nivel, BlockPos sino) {
        GlobalPos ancora = DadosHistoria.de(nivel.getServer()).ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        if (ancora == null || ancora.dimension() != nivel.dimension()) return Optional.empty();
        int r = RegrasPrepararVila.RAIO_PROCURA_SINO;
        if (sino.distSqr(ancora.pos()) > (double) r * r) return Optional.empty();
        int[] k = RegrasPrepararVila.canto(sino.getX(), sino.getY(), sino.getZ(), RegrasPrepararVila.SINO);
        BlockPos canto = new BlockPos(k[0], k[1], k[2]);
        if (!nivel.getBlockState(PrepararVilaCalada.em(canto, RegrasPrepararVila.ATRIL)).is(Blocks.LECTERN)) {
            return Optional.empty();
        }
        cantoCache = canto;
        nivelCache = nivel;
        return Optional.of(canto);
    }

    /** O canto da vila sem ninguém bater no sino (cliques, empurrões, comandos). Busca no máximo uma vez por minuto. */
    static Optional<BlockPos> canto(MinecraftServer s, boolean forcar) {
        GlobalPos ancora = DadosHistoria.de(s).ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        ServerLevel nivel = ancora == null ? null : s.getLevel(ancora.dimension());
        if (nivel == null) return Optional.empty();
        if (cantoCache != null && nivelCache == nivel) {
            // Confere o atril só se o chunk já está carregado (não carrega chunk para isso).
            BlockPos atril = PrepararVilaCalada.em(cantoCache, RegrasPrepararVila.ATRIL);
            if (!nivel.isLoaded(atril) || nivel.getBlockState(atril).is(Blocks.LECTERN)) return Optional.of(cantoCache);
            esquecerCanto(); // o atril saiu do lugar guardado: procura de novo (com o mesmo limite de uma vez por minuto)
        }
        if (!forcar && s.getTickCount() < proximaBusca) return Optional.empty();
        proximaBusca = s.getTickCount() + 1_200;
        Optional<BlockPos> c = PrepararVilaCalada.acharCanto(nivel, ancora.pos());
        c.ifPresent(k -> {
            cantoCache = k;
            nivelCache = nivel;
        });
        return c;
    }

    /** Esquece o canto guardado (vila ancorada de novo, desancorada ou preparada). A próxima busca segue o limite. */
    public static void esquecerCanto() {
        cantoCache = null;
        nivelCache = null;
    }

    static ServerLevel nivel(MinecraftServer s) {
        GlobalPos ancora = DadosHistoria.de(s).ancoras.get(IdsVilaCalada.LUGAR_VILA_CALADA);
        return ancora == null ? null : s.getLevel(ancora.dimension());
    }

    /** Há um botão de pedra no espaço vazio do peitoril? */
    static boolean botaoSineiro(ServerLevel nivel, BlockPos canto) {
        BlockPos p = PrepararVilaCalada.em(canto, RegrasDobre.ESPACO_SINEIRO);
        return nivel.isLoaded(p) && RegrasDobre.botaoDePedra(id(nivel.getBlockState(p)));
    }

    private static String id(BlockState s) {
        return BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString();
    }

    private static void aoUsarBloco(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() != InteractionHand.MAIN_HAND) return;
        if (e.getLevel().isClientSide()) {
            // No cliente: com o martelo no sino rachado, o jogo não toca o sino comum por conta própria (o servidor
            // manda o som do dobre). Só olha blocos que o cliente já tem; nada de classe de cliente aqui.
            if (e.getEntity().getMainHandItem().is(Itens.MARTELO.get()) && e.getLevel().getBlockState(e.getPos()).is(Blocks.BELL)) {
                int[] k = RegrasPrepararVila.canto(e.getPos().getX(), e.getPos().getY(), e.getPos().getZ(), RegrasPrepararVila.SINO);
                BlockPos atril = new BlockPos(k[0], k[1], k[2]).offset(RegrasPrepararVila.ATRIL.x(), RegrasPrepararVila.ATRIL.y(),
                        RegrasPrepararVila.ATRIL.z());
                if (e.getLevel().getBlockState(atril).is(Blocks.LECTERN)) {
                    e.setCanceled(true);
                    e.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
            return;
        }
        if (!(e.getEntity() instanceof ServerPlayer p) || p.isSpectator()) return;
        if (!(e.getLevel() instanceof ServerLevel nivel) || !nivel.getBlockState(e.getPos()).is(Blocks.BELL)) return;
        if (!PistasVilaCalada.dados(nivel.getServer()).ligado) return;
        Optional<BlockPos> canto = cantoDoSino(nivel, e.getPos());
        if (canto.isEmpty()) return;
        if (p.getMainHandItem().is(Itens.MARTELO.get())) {
            e.setCanceled(true);
            e.setCancellationResult(InteractionResult.SUCCESS);
            martelo(nivel, p, e.getPos(), canto.get());
        } else {
            outraCoisa(nivel, p, e.getPos());
        }
    }

    /** Mão ou outra coisa: o sino toca (o jogo cuida) e, um pouco depois, toca sozinho de novo. */
    private static void outraCoisa(ServerLevel nivel, ServerPlayer p, BlockPos sino) {
        long agora = nivel.getServer().getTickCount();
        if (agora < ecoSozinhoAte) return;
        ecoSozinhoAte = agora + 60;
        agendar(nivel.getServer(), 30, () -> nivel.playSound(null, sino, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS,
                2.0f, RegrasVilaCalada.TOM_RACHA));
        long hora = nivel.getDayTime();
        Grupos.Grupo g = grupo(nivel.getServer(), p);
        if (g == null || g.tem(IdsVilaCalada.MARCO_DOBRE) || RegrasDobre.manha(hora)) return;
        DadosVilaCalada dv = PistasVilaCalada.dados(nivel.getServer());
        DadosVilaCalada.Dobre d = dv.dobre(g.nome());
        if (!d.outraCoisa) {
            d.outraCoisa = true;
            dv.setDirty();
        }
    }

    private static Grupos.Grupo grupo(MinecraftServer s, ServerPlayer p) {
        return DadosHistoria.de(s).grupos.grupoDe(p.getUUID()).orElse(null);
    }

    // ------------------------------------------------------------------ o martelo

    private static void martelo(ServerLevel nivel, ServerPlayer p, BlockPos sino, BlockPos canto) {
        MinecraftServer s = nivel.getServer();
        Grupos.Grupo g = grupo(s, p);
        if (g == null || g.tem(IdsVilaCalada.MARCO_DOBRE)) {
            surdo(nivel, sino); // sem grupo, ou a vila já está calada para eles: o sino não responde
            return;
        }
        DadosVilaCalada dv = PistasVilaCalada.dados(s);
        DadosVilaCalada.Dobre d = dv.dobre(g.nome());
        long agora = nivel.getGameTime();
        long hora = nivel.getDayTime();
        long dia = RegrasDobre.dia(hora);
        Corrente c = ANDAMENTO.computeIfAbsent(g.nome(), k -> new Corrente(new RegrasDobre.Andamento(), nivel, sino, canto));
        boolean botao = botaoSineiro(nivel, canto);
        RegrasDobre.Toque t = RegrasDobre.julgar(c.a(), agora, hora, p.getUUID(), botao, d.noiteGasta == dia);
        if (t.bate) TENTOU.add(g.nome() + ":" + dia);
        switch (t) {
            case NOITE_GASTA -> {
                surdo(nivel, sino);
                if (!c.a().comecou()) ANDAMENTO.remove(g.nome());
            }
            case MESMA_MAO -> {
                p.displayClientMessage(Component.translatable("odteste.dobre.mesma_mao").withStyle(ChatFormatting.DARK_RED), true);
                nivel.playSound(null, p.blockPosition(), SoundEvents.CHAIN_STEP, SoundSource.PLAYERS, 0.6f, 0.5f);
            }
            case PESADO -> {
                p.displayClientMessage(Component.translatable("odteste.dobre.pesado").withStyle(ChatFormatting.DARK_RED), true);
                nivel.playSound(null, p.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.3f, 0.5f);
                cliqueVazio(nivel, g, canto);
            }
            case CONTOU -> {
                RegrasDobre.aplicar(c.a(), t, agora, p.getUUID());
                badalar(nivel, sino, true);
                ferir(p);
                pararCriaturas(nivel, sino);
                if (c.a().toques == 1) {
                    Mestre.avisar(s, Component.translatable("odteste.dobre.mestre.comecou", g.nome()).withStyle(ChatFormatting.GRAY));
                }
                NeoForge.EVENT_BUS.post(new DobreEvento.Toque(g.nome(), nivel, sino, c.a().toques, p));
            }
            case RAPIDO, FORA_DE_HORA -> {
                badalar(nivel, sino, false);
                ferir(p);
                chamar(nivel, g.nome(), sino);
                if (RegrasDobre.gastaNoite(t)) gastar(dv, d, dia, g.nome());
                else if (!c.a().comecou()) ANDAMENTO.remove(g.nome());
                Mestre.avisar(s, Component.translatable("odteste.dobre.mestre.erro", g.nome(),
                        Component.translatable("odteste.dobre.erro." + t.name().toLowerCase(java.util.Locale.ROOT)))
                        .withStyle(ChatFormatting.GRAY));
                NeoForge.EVENT_BUS.post(new DobreEvento.Erro(g.nome(), nivel, sino, t.name()));
            }
            case A_MAIS -> {
                badalar(nivel, sino, true);
                ferir(p);
                contaErrada(nivel, g.nome(), dv, d, dia, sino, t.name());
            }
        }
    }

    /** Toque que não sai: baque surdo, sem eco. */
    private static void surdo(ServerLevel nivel, BlockPos sino) {
        nivel.playSound(null, sino, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.4f, 0.3f);
    }

    /** Toque do martelo. Lento = grave e demorado; senão, a badalada rachada que chama (três, rápidas). */
    private static void badalar(ServerLevel nivel, BlockPos sino, boolean lento) {
        if (lento) {
            nivel.playSound(null, sino, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 4.0f, RegrasVilaCalada.TOM_GRAVE);
            nivel.playSound(null, sino, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 3.0f, RegrasVilaCalada.TOM_GRAVE);
            nivel.sendParticles(ParticleTypes.ASH, sino.getX() + 0.5, sino.getY() + 0.5, sino.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.01);
        } else {
            for (int i = 0; i < 3; i++) {
                agendar(nivel.getServer(), i * 6L, () -> nivel.playSound(null, sino, SoundEvents.BELL_BLOCK,
                        SoundSource.BLOCKS, 4.0f, RegrasVilaCalada.TOM_RACHA));
            }
        }
    }

    /** Cada toque machuca a mão de quem bate (nunca mata). */
    private static void ferir(ServerPlayer p) {
        float dano = RegrasDobre.dano(p.getHealth());
        if (dano <= 0f || p.isCreative()) return;
        Holder<net.minecraft.world.damagesource.DamageType> tipo = p.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(Sangramento.DANO);
        p.invulnerableTime = 0; // um toque logo depois de outro dano ainda dói
        p.hurt(new DamageSource(tipo), dano);
        ((ServerLevel) p.level()).sendParticles(Sangramento.GOTA, p.getX(), p.getY() + 1.1, p.getZ(), 6, 0.2, 0.1, 0.2, 0);
    }

    private static void gastar(DadosVilaCalada dv, DadosVilaCalada.Dobre d, long dia, String grupo) {
        d.noiteGasta = dia;
        dv.setDirty();
        ANDAMENTO.remove(grupo);
    }

    /** Toque a mais ou conta errada: o sino toca sozinho mais uma vez; Lucidez cai em quem ouviu; a noite acaba. */
    private static void contaErrada(ServerLevel nivel, String grupo, DadosVilaCalada dv, DadosVilaCalada.Dobre d,
                                    long dia, BlockPos sino, String tipo) {
        d.errosConta++;
        gastar(dv, d, dia, grupo);
        agendar(nivel.getServer(), 50, () -> {
            nivel.playSound(null, sino, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 4.0f, RegrasVilaCalada.TOM_RACHA);
            nivel.playSound(null, sino, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 3.0f, 0.4f);
            for (ServerPlayer q : nivel.players()) {
                if (!q.isSpectator() && q.position().distanceToSqr(Vec3.atCenterOf(sino)) <= RegrasDobre.RAIO_OUVIR * RegrasDobre.RAIO_OUVIR) {
                    Lucidez.perder(q, RegrasDobre.LUCIDEZ_ERRO);
                }
            }
        });
        Mestre.avisar(nivel.getServer(), Component.translatable("odteste.dobre.mestre.erro", grupo,
                Component.translatable("odteste.dobre.erro." + tipo.toLowerCase(java.util.Locale.ROOT))).withStyle(ChatFormatting.GRAY));
        NeoForge.EVENT_BUS.post(new DobreEvento.Erro(grupo, nivel, sino, tipo));
    }

    // ------------------------------------------------------------------ criaturas

    private static List<Mob> criaturas(ServerLevel nivel, BlockPos sino) {
        double r = RegrasDobre.RAIO_CRIATURAS;
        AABB caixa = new AABB(sino).inflate(r, 32, r);
        List<Mob> l = nivel.getEntitiesOfClass(Mob.class, caixa, m -> m.isAlive() && m.getType().is(OUVE));
        l.sort((a, b) -> Double.compare(a.distanceToSqr(Vec3.atCenterOf(sino)), b.distanceToSqr(Vec3.atCenterOf(sino))));
        return l.size() > RegrasDobre.MAX_CRIATURAS ? new ArrayList<>(l.subList(0, RegrasDobre.MAX_CRIATURAS)) : l;
    }

    private static void travar(Mob m, Vec3 olhar) {
        AttributeInstance vel = m.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null && !vel.hasModifier(MODIFICADOR)) {
            vel.addTransientModifier(new AttributeModifier(MODIFICADOR, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        m.getNavigation().stop();
        m.setTarget(null);
        m.getLookControl().setLookAt(olhar.x, olhar.y, olhar.z);
    }

    private static void soltar(Mob m) {
        AttributeInstance vel = m.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null) vel.removeModifier(MODIFICADOR);
    }

    /** Toque certo: param e viram a cabeça para a torre. Eles querem ouvir. */
    private static void pararCriaturas(ServerLevel nivel, BlockPos sino) {
        long ate = nivel.getServer().getTickCount() + RegrasDobre.PARADA_TOQUE;
        for (Mob m : criaturas(nivel, sino)) {
            if (AJOELHADOS.contains(m)) continue;
            travar(m, Vec3.atCenterOf(sino));
            PARADOS.put(m, ate);
        }
    }

    /** O sino chamou: as criaturas sobem até a torre. Uma vez a cada {@link RegrasDobre#CHAMADO_ESPERA}. */
    private static void chamar(ServerLevel nivel, String grupo, BlockPos sino) {
        long agora = nivel.getServer().getTickCount();
        Long antes = CHAMADO.get(grupo);
        if (antes != null && agora - antes < RegrasDobre.CHAMADO_ESPERA) return;
        CHAMADO.put(grupo, agora);
        BlockPos base = chaoDaTorre(nivel, sino);
        for (Mob m : criaturas(nivel, sino)) {
            if (AJOELHADOS.contains(m)) continue;
            PARADOS.remove(m);
            soltar(m);
            m.getNavigation().moveTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 1.2);
        }
    }

    /** O chão ao pé do campanário (procura para baixo, só blocos carregados). */
    private static BlockPos chaoDaTorre(ServerLevel nivel, BlockPos sino) {
        BlockPos.MutableBlockPos p = sino.mutable();
        for (int i = 0; i < 24 && nivel.isLoaded(p); i++) {
            p.move(Direction.DOWN);
            if (!nivel.getBlockState(p).isAir() && nivel.getBlockState(p.above()).isAir() && i > 16) return p.above().immutable();
        }
        return sino.below(19);
    }

    // ------------------------------------------------------------------ fechar e o fim

    private static void fechar(String grupo, Corrente c) {
        ServerLevel nivel = c.nivel();
        MinecraftServer s = nivel.getServer();
        DadosHistoria dh = DadosHistoria.de(s);
        DadosVilaCalada dv = PistasVilaCalada.dados(s);
        DadosVilaCalada.Dobre d = dv.dobre(grupo);
        long dia = RegrasDobre.dia(nivel.getDayTime());
        boolean botao = botaoSineiro(nivel, c.canto());
        switch (RegrasDobre.fim(c.a(), botao)) {
            case FEITO -> feito(nivel, dh, dv, grupo, c.sino(), c.canto(), false);
            case FALTOU_SINEIRO -> {
                gastar(dv, d, dia, grupo);
                Optional<Grupos.Grupo> g = dh.grupos.grupo(grupo);
                g.ifPresent(x -> cliqueVazio(nivel, x, c.canto()));
                Mestre.avisar(s, Component.translatable("odteste.dobre.mestre.erro", grupo,
                        Component.translatable("odteste.dobre.erro.faltou_sineiro")).withStyle(ChatFormatting.GRAY));
                NeoForge.EVENT_BUS.post(new DobreEvento.Erro(grupo, nivel, c.sino(), RegrasDobre.Fim.FALTOU_SINEIRO.name()));
            }
            case CONTA_ERRADA -> contaErrada(nivel, grupo, dv, d, dia, c.sino(), RegrasDobre.Fim.CONTA_ERRADA.name());
        }
    }

    /**
     * O dobre foi feito (ou o mestre resolveu à força). Marca o grupo, ajoelha as criaturas quando o eco chega,
     * agenda o Moinho e avisa a cutscene. Se ninguém tocar a cena, mostra o texto final na tela de quem estava lá.
     */
    static void feito(ServerLevel nivel, DadosHistoria dh, DadosVilaCalada dv, String grupo, BlockPos sino, BlockPos canto,
                      boolean forcado) {
        MinecraftServer s = nivel.getServer();
        ANDAMENTO.remove(grupo);
        if (dh.grupos.marcar(grupo, IdsVilaCalada.MARCO_DOBRE)) dh.setDirty();
        DadosVilaCalada.Dobre d = dv.dobre(grupo);
        d.feitoDia = RegrasDobre.dia(nivel.getDayTime());
        d.noiteGasta = d.feitoDia;
        dv.setDirty();
        OdMod.LOGGER.info("Vila Calada: o grupo {} fez o dobre{}", grupo, forcado ? " (mestre)" : "");

        nivel.playSound(null, sino, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 5.0f, 0.35f);
        Vec3 centro = Vec3.atCenterOf(sino);
        for (Mob m : criaturas(nivel, sino)) {
            int quando = RegrasDobre.eco(Math.sqrt(m.distanceToSqr(centro)));
            agendar(s, quando, () -> ajoelhar(nivel, m, centro));
        }
        List<ServerPlayer> presentes = new ArrayList<>();
        Optional<Grupos.Grupo> g = dh.grupos.grupo(grupo);
        if (g.isPresent()) {
            for (UUID u : g.get().membros().keySet()) {
                ServerPlayer p = s.getPlayerList().getPlayer(u);
                if (p != null && p.level() == nivel && p.position().distanceToSqr(centro) <= RegrasDobre.RAIO_OUVIR * RegrasDobre.RAIO_OUVIR) {
                    presentes.add(p);
                }
            }
        }
        for (int depois : RegrasDobre.MOINHO_DEPOIS) agendar(s, depois, () -> moinho(nivel, grupo, sino));
        DobreEvento.Feito ev = new DobreEvento.Feito(grupo, nivel, sino, canto, presentes);
        NeoForge.EVENT_BUS.post(ev);
        if (!ev.cenaCuida()) {
            // Sem cutscene ainda: só o texto final, no escuro, para quem estava lá.
            agendar(s, 60, () -> {
                for (ServerPlayer p : presentes) {
                    if (p.isRemoved()) continue;
                    p.connection.send(new ClientboundSetTitlesAnimationPacket(40, 80, 40));
                    p.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
                    p.connection.send(new ClientboundSetSubtitleTextPacket(
                            Component.translatable("odteste.dobre.fim").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
                }
            });
        }
        Mestre.avisar(s, Component.translatable("odteste.dobre.mestre.feito", grupo).withStyle(ChatFormatting.GOLD));
    }

    /** Ajoelha quando o eco chega: para de vez, sem alvo, olhando a torre. Some como cinza na manhã seguinte. */
    private static void ajoelhar(ServerLevel nivel, Mob m, Vec3 sino) {
        if (!m.isAlive() || m.isRemoved()) return;
        PARADOS.remove(m);
        travar(m, sino);
        m.setXRot(40f); // cabeça baixa (a animação de ajoelhar é do Modelador)
        CompoundTag t = new CompoundTag();
        t.putLong("dia", RegrasDobre.dia(nivel.getDayTime()));
        t.putLong("sino", BlockPos.containing(sino).asLong());
        m.getPersistentData().put(TAG_AJOELHADO, t);
        AJOELHADOS.add(m);
        nivel.sendParticles(ParticleTypes.ASH, m.getX(), m.getY() + 0.5, m.getZ(), 10, 0.3, 0.3, 0.3, 0.01);
    }

    /** Criatura marcada que carregou de novo (chunk ou servidor reiniciado): volta a ficar ajoelhada. */
    private static void aoEntrarNoMundo(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Mob m)) return;
        if (!m.getPersistentData().contains(TAG_AJOELHADO)) return;
        BlockPos sino = BlockPos.of(m.getPersistentData().getCompound(TAG_AJOELHADO).getLong("sino"));
        travar(m, Vec3.atCenterOf(sino));
        m.setXRot(40f);
        AJOELHADOS.add(m);
    }

    /** Vira cinza na manhã depois do dobre (ou se já passou dela enquanto estava descarregada). */
    private static boolean horaDaCinza(Mob m, long horaDoMundo) {
        if (RegrasDobre.manha(horaDoMundo)) return true;
        long dia = m.getPersistentData().getCompound(TAG_AJOELHADO).getLong("dia");
        return RegrasDobre.dia(horaDoMundo) > dia;
    }

    /** A roda do Moinho Parado, longe: só para quem do grupo está dentro da vila. */
    private static void moinho(ServerLevel nivel, String grupo, BlockPos sino) {
        MinecraftServer s = nivel.getServer();
        DadosHistoria dh = DadosHistoria.de(s);
        Optional<Grupos.Grupo> g = dh.grupos.grupo(grupo);
        if (g.isEmpty()) return;
        GlobalPos moinho = dh.ancoras.get(IdsVilaCalada.LUGAR_MOINHO);
        for (UUID u : g.get().membros().keySet()) {
            ServerPlayer p = s.getPlayerList().getPlayer(u);
            if (p == null || p.level() != nivel || !ClimaVilaCalada.estaDentro(p)) continue;
            double dx = moinho != null && moinho.dimension() == nivel.dimension() ? moinho.pos().getX() - p.getX() : -1;
            double dz = moinho != null && moinho.dimension() == nivel.dimension() ? moinho.pos().getZ() - p.getZ() : -1;
            double[] dir = RegrasVilaCalada.direcao(dx, dz);
            Vec3 onde = p.getEyePosition().add(dir[0] * 24, 2, dir[1] * 24);
            long semente = nivel.getRandom().nextLong();
            p.connection.send(new ClientboundSoundPacket(som(SoundEvents.GRINDSTONE_USE), SoundSource.AMBIENT,
                    onde.x, onde.y, onde.z, 0.5f, 0.45f, semente));
            p.connection.send(new ClientboundSoundPacket(som(SoundEvents.WOODEN_DOOR_CLOSE), SoundSource.AMBIENT,
                    onde.x, onde.y, onde.z, 0.35f, 0.4f, semente));
        }
    }

    private static Holder<SoundEvent> som(SoundEvent e) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(e);
    }

    // ------------------------------------------------------------------ peitoril (cliques)

    /** O martelo não bateu porque falta o Sineiro: um clique só, no espaço vazio, para o mais exposto do grupo. */
    private static void cliqueVazio(ServerLevel nivel, Grupos.Grupo g, BlockPos canto) {
        ServerPlayer melhor = null;
        for (UUID u : g.membros().keySet()) {
            ServerPlayer p = nivel.getServer().getPlayerList().getPlayer(u);
            if (p == null || p.level() != nivel || p.isSpectator() || p.getTags().contains(ClimaVilaCalada.TAG_MESTRE)) continue;
            if (melhor == null || DadosJogador.exposicao(p) > DadosJogador.exposicao(melhor)) melhor = p;
        }
        if (melhor == null) return;
        BlockPos v = PrepararVilaCalada.em(canto, RegrasDobre.ESPACO_SINEIRO);
        ServerPlayer quem = melhor;
        agendar(nivel.getServer(), 30, () -> quem.connection.send(new ClientboundSoundPacket(som(SoundEvents.STONE_BUTTON_CLICK_ON),
                SoundSource.BLOCKS, v.getX() + 0.5, v.getY() + 0.2, v.getZ() + 0.5, 3.0f, 0.6f, nivel.getRandom().nextLong())));
    }

    /** Os botões do peitoril clicam, um por vez, e param (um por botão que está lá). */
    private static void cliques(ServerLevel nivel, Grupos.Grupo g, BlockPos canto, boolean alto) {
        BlockPos primeiro = PrepararVilaCalada.em(canto, RegrasDobre.ESPACO_SINEIRO);
        if (!nivel.isLoaded(primeiro)) return;
        List<BlockPos> botoes = new ArrayList<>();
        for (int[] k : RegrasDobre.peitoril()) {
            BlockPos b = canto.offset(k[0], k[1], k[2]);
            if (RegrasDobre.botaoDePedra(id(nivel.getBlockState(b)))) botoes.add(b);
        }
        if (botoes.isEmpty()) return;
        Vec3 meio = Vec3.atCenterOf(botoes.get(botoes.size() / 2));
        List<ServerPlayer> ouvem = new ArrayList<>();
        for (UUID u : g.membros().keySet()) {
            ServerPlayer p = nivel.getServer().getPlayerList().getPlayer(u);
            if (p == null || p.level() != nivel || !ClimaVilaCalada.estaDentro(p)) continue;
            if (alto || p.position().distanceToSqr(meio) <= RegrasDobre.CLIQUES_PERTO * RegrasDobre.CLIQUES_PERTO) {
                ouvem.add(p);
                for (ServerPlayer m : Mestre.ouvintesPerto(nivel, p.position(), RegrasMestre.RAIO_PERTO_DO_GRUPO, p)) {
                    if (!ouvem.contains(m)) ouvem.add(m);
                }
            }
        }
        if (ouvem.isEmpty()) return;
        float vol = alto ? RegrasDobre.CLIQUES_VOLUME_EMPURRAO : RegrasDobre.CLIQUES_VOLUME;
        for (int i = 0; i < botoes.size(); i++) {
            BlockPos b = botoes.get(i);
            agendar(nivel.getServer(), (long) i * RegrasDobre.CLIQUE_INTERVALO, () -> {
                long semente = nivel.getRandom().nextLong();
                for (ServerPlayer p : ouvem) {
                    if (p.isRemoved()) continue;
                    p.connection.send(new ClientboundSoundPacket(som(SoundEvents.STONE_BUTTON_CLICK_ON), SoundSource.BLOCKS,
                            b.getX() + 0.5, b.getY() + 0.2, b.getZ() + 0.5, vol, 0.7f, semente));
                }
            });
        }
    }

    // ------------------------------------------------------------------ empurrões no mundo

    /** A bigorna rachada vira para o campanário. Só se a bigorna ainda está lá. */
    static boolean virarBigorna(ServerLevel nivel, BlockPos canto) {
        BlockPos b = PrepararVilaCalada.em(canto, RegrasDobre.BIGORNA);
        if (!nivel.isLoaded(b)) return false;
        BlockState st = nivel.getBlockState(b);
        if (!st.is(BlockTags.ANVIL) || !st.hasProperty(AnvilBlock.FACING)) return false;
        BlockPos sino = PrepararVilaCalada.em(canto, RegrasPrepararVila.SINO);
        Direction para = Direction.getNearest((double) (sino.getX() - b.getX()), 0.0, (double) (sino.getZ() - b.getZ()));
        // A bigorna "aponta" ao longo do eixo perpendicular ao FACING: gira para o comprido apontar o campanário.
        nivel.setBlock(b, st.setValue(AnvilBlock.FACING, para.getClockWise()), Block.UPDATE_ALL);
        nivel.playSound(null, b, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.4f, 0.5f);
        return true;
    }

    /** Uma corrente do sino caída em cima da cova fechada (a lápide da Cruz de Ferro). Só no ar. */
    static boolean correnteNaCova(ServerLevel nivel, BlockPos canto) {
        BlockPos p = PrepararVilaCalada.em(canto, RegrasDobre.LAPIDE).above();
        if (!nivel.isLoaded(p) || !nivel.getBlockState(p).isAir()) return false;
        nivel.setBlock(p, Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.X), Block.UPDATE_ALL);
        return true;
    }

    /** Empurrão do mestre: o Pendurado aparece em cima do sino, uma vez, para quem do grupo está na vila. */
    static int penduradoNoSino(MinecraftServer s, Grupos.Grupo g, BlockPos sino) {
        int n = 0;
        ServerLevel nivel = nivel(s);
        for (UUID u : g.membros().keySet()) {
            ServerPlayer p = s.getPlayerList().getPlayer(u);
            if (p == null || p.level() != nivel || !ClimaVilaCalada.estaDentro(p)) continue;
            PacketDistributor.sendToPlayer(p, new PenduradoSino(sino.above(4)));
            n++;
            for (ServerPlayer m : Mestre.assistindo(p)) PacketDistributor.sendToPlayer(m, new PenduradoSino(sino.above(4)));
        }
        return n;
    }

    // ------------------------------------------------------------------ tick

    private static void agendar(MinecraftServer s, long depois, Runnable r) {
        AGENDA.add(new Agendado(s.getTickCount() + depois, r));
    }

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        long tick = s.getTickCount();
        while (!AGENDA.isEmpty() && AGENDA.peek().quando() <= tick) {
            try {
                AGENDA.poll().acao().run();
            } catch (RuntimeException erro) {
                OdMod.LOGGER.warn("Dobre: ação agendada falhou: {}", erro.toString());
            }
        }
        if (tick % 20 != 15) return; // uma vez por segundo, longe dos outros (pistas no 0, clima no 10)

        // Dobres em andamento: o som morreu?
        Iterator<Map.Entry<String, Corrente>> it = new LinkedHashMap<>(ANDAMENTO).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Corrente> en = it.next();
            Corrente c = en.getValue();
            if (!c.a().comecou()) {
                ANDAMENTO.remove(en.getKey());
                continue;
            }
            if (RegrasDobre.fechou(c.a(), c.nivel().getGameTime())) fechar(en.getKey(), c);
        }

        // Criaturas paradas pelo toque e ajoelhadas.
        PARADOS.entrySet().removeIf(p -> {
            Mob m = p.getKey();
            if (m.isRemoved() || !m.isAlive()) return true;
            if (tick >= p.getValue()) {
                soltar(m);
                return true;
            }
            return false;
        });
        ServerLevel nivel = nivel(s);
        // Saiu do mundo: morta/descartada (shouldDestroy) some de vez; só descarregada (chunk, servidor) guarda a marca
        // salva e volta pelo aoEntrarNoMundo. Nos dois casos este objeto não serve mais.
        AJOELHADOS.removeIf(m -> {
            Entity.RemovalReason motivo = m.getRemovalReason();
            return motivo != null || !m.isAlive();
        });
        if (!AJOELHADOS.isEmpty()) {
            for (Iterator<Mob> im = AJOELHADOS.iterator(); im.hasNext(); ) {
                Mob m = im.next();
                if (horaDaCinza(m, m.level().getDayTime())) {
                    ((ServerLevel) m.level()).sendParticles(ParticleTypes.ASH, m.getX(), m.getY() + 0.8, m.getZ(), 30, 0.4, 0.6, 0.4, 0.02);
                    ((ServerLevel) m.level()).sendParticles(ParticleTypes.SMOKE, m.getX(), m.getY() + 0.5, m.getZ(), 8, 0.3, 0.4, 0.3, 0.01);
                    m.discard(); // vira cinza (sem drop)
                    im.remove();
                } else {
                    m.getNavigation().stop();
                    m.setTarget(null);
                }
            }
        }

        if (nivel == null || !PistasVilaCalada.dados(s).ligado) return;
        DadosHistoria dh = DadosHistoria.de(s);
        DadosVilaCalada dv = PistasVilaCalada.dados(s);
        long hora = nivel.getDayTime();
        long dia = RegrasDobre.dia(hora);
        boolean noite = !nivel.dimensionType().hasFixedTime() && RegrasVilaCalada.noite(hora);
        boolean manha = RegrasDobre.manha(hora);
        boolean fimDaNoite = RegrasDobre.hora(hora) >= RegrasVilaCalada.NOITE_FIM;
        if (!noite && !manha && !fimDaNoite) return;
        Optional<BlockPos> canto = canto(s, false);

        for (Grupos.Grupo g : dh.grupos.todos()) {
            if (!g.tem(IdsVilaCalada.MARCO_CHEGOU) || g.tem(IdsVilaCalada.MARCO_DOBRE)) continue;
            String nome = g.nome();
            if (noite && alguemDentro(s, g)) {
                PRESENTES_NOITE.add(nome);
                DadosVilaCalada.Dobre d = dv.dobre.get(nome);
                Long prox = CLIQUES.get(nome);
                if (prox == null) {
                    CLIQUES.put(nome, tick + RegrasDobre.proximosCliques(nivel.getRandom().nextDouble()) / 3);
                } else if (tick >= prox && canto.isPresent()) {
                    CLIQUES.put(nome, tick + RegrasDobre.proximosCliques(nivel.getRandom().nextDouble()));
                    cliques(nivel, g, canto.get(), d != null && RegrasDobre.empurraoCliques(d.noitesSemTentar));
                }
            }
            if (fimDaNoite && PRESENTES_NOITE.remove(nome)) {
                DadosVilaCalada.Dobre d = dv.dobre(nome);
                if (d.ultimaNoiteContada != dia) {
                    d.ultimaNoiteContada = dia;
                    d.noitesSemTentar = TENTOU.remove(nome + ":" + dia) ? 0 : d.noitesSemTentar + 1;
                    dv.setDirty();
                }
            }
            if (manha && canto.isPresent()) {
                DadosVilaCalada.Dobre d = dv.dobre.get(nome);
                if (d == null) continue;
                if (d.outraCoisa && !d.empurroes.contains("bigorna") && virarBigorna(nivel, canto.get())) {
                    d.empurroes.add("bigorna");
                    dv.setDirty();
                }
                if (RegrasDobre.empurraoCorrente(d.errosConta) && !d.empurroes.contains("corrente")
                        && correnteNaCova(nivel, canto.get())) {
                    d.empurroes.add("corrente");
                    dv.setDirty();
                }
            }
        }
        if (!noite) CLIQUES.clear();
    }

    private static boolean alguemDentro(MinecraftServer s, Grupos.Grupo g) {
        for (UUID u : g.membros().keySet()) {
            ServerPlayer p = s.getPlayerList().getPlayer(u);
            if (p != null && ClimaVilaCalada.estaDentro(p)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ para o mestre

    /** O grupo está com a vila calada (dobre feito)? */
    public static boolean calada(Grupos.Grupo g) {
        return g != null && g.tem(IdsVilaCalada.MARCO_DOBRE);
    }

    /** Linha do {@code /odteste mestre status}: como está o dobre do grupo. Para o mestre, número aparece. */
    public static Component status(MinecraftServer s, Grupos.Grupo g) {
        if (g.tem(IdsVilaCalada.MARCO_DOBRE)) return Component.translatable("odteste.mestre.status.dobre.feito");
        DadosVilaCalada.Dobre d = PistasVilaCalada.dados(s).dobre.get(g.nome());
        ServerLevel nivel = nivel(s);
        Optional<BlockPos> canto = nivel == null ? Optional.empty() : canto(s, false);
        Component botao = canto.isEmpty() || !nivel.isLoaded(PrepararVilaCalada.em(canto.get(), RegrasDobre.ESPACO_SINEIRO))
                ? Component.literal("?")
                : Component.translatable(botaoSineiro(nivel, canto.get()) ? "odteste.evento.sim" : "odteste.evento.nao");
        Corrente c = ANDAMENTO.get(g.nome());
        int toques = c == null ? 0 : c.a().toques;
        boolean gasta = d != null && nivel != null && d.noiteGasta == RegrasDobre.dia(nivel.getDayTime());
        return Component.translatable("odteste.mestre.status.dobre", toques, RegrasDobre.TOQUES, botao,
                Component.translatable(gasta ? "odteste.evento.sim" : "odteste.evento.nao"),
                d == null ? 0 : d.errosConta, d == null ? 0 : d.noitesSemTentar,
                Component.translatable(d != null && d.outraCoisa ? "odteste.evento.sim" : "odteste.evento.nao"),
                d == null || d.empurroes.isEmpty() ? "-" : String.join(", ", d.empurroes));
    }

    /** Reabrir (mestre): tira o marco e zera o estado do grupo. A vila volta a ter fome para eles. */
    static void reabrir(MinecraftServer s, String grupo) {
        DadosHistoria dh = DadosHistoria.de(s);
        if (dh.grupos.desmarcar(grupo, IdsVilaCalada.MARCO_DOBRE)) dh.setDirty();
        DadosVilaCalada dv = PistasVilaCalada.dados(s);
        if (dv.dobre.remove(grupo) != null) dv.setDirty();
        ANDAMENTO.remove(grupo);
    }

    /** Empurrão à mão (mestre): "cliques", "bigorna" ou "corrente". */
    static boolean empurrar(MinecraftServer s, Grupos.Grupo g, String qual) {
        ServerLevel nivel = nivel(s);
        Optional<BlockPos> canto = canto(s, true);
        if (nivel == null || canto.isEmpty()) return false;
        return switch (qual) {
            case "cliques" -> {
                cliques(nivel, g, canto.get(), true);
                yield true;
            }
            case "bigorna" -> virarBigorna(nivel, canto.get());
            case "corrente" -> correnteNaCova(nivel, canto.get());
            default -> false;
        };
    }
}
