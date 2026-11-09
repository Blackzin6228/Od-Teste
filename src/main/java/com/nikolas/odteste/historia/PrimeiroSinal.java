package com.nikolas.odteste.historia;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.estruturas.Backup;
import com.nikolas.odteste.estruturas.Espaco;
import com.nikolas.odteste.estruturas.MundoReal;
import com.nikolas.odteste.sistemas.Degrau;
import com.nikolas.odteste.sistemas.Lucidez;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * Ato 1, Cena 1 "Primeiro Sinal" e Enigma 1 "O Sino da Vila" (lore/roteiro.md). Só servidor.
 *
 * O admin solta o evento para um grupo. O mod escolhe uma vila que já existe (o sino mais perto do grupo, ou do
 * ponto do admin) e a marca sem quebrar nada: o sino toca FERRO em Morse e o bloco de notas responde; um aldeão
 * some e a cama dele solta partículas vermelhas; os outros param olhando o sino e as trocas viram frases; um
 * estandarte aparece numa parede; ao lado do sino, um baú trancado com uma placa em branco. Escrever FERRO na placa
 * abre o baú.
 *
 * Sem lag: o trabalho pesado (procurar jogador, aldeão, ler a placa) roda uma vez por segundo e só perto da vila
 * marcada; a cada tick só se toca o próximo som de quem está ouvindo (uma conta por ouvinte).
 * Os sons vão só para quem está perto (pacote direto), então cada um ouve no seu ritmo e nada muda no mundo.
 */
public final class PrimeiroSinal {
    /** Quem está ouvindo o sino agora: onde está na sequência. Só memória. */
    private static final class Ouvinte {
        long inicio;
        List<RegrasAto1.Som> sons;
        int duracao;
        int proximo;
        boolean pertoTodo;
        boolean avaliado;
    }

    private static final Map<String, Map<UUID, Ouvinte>> OUVINTES = new HashMap<>();
    private static final Map<UUID, Long> ULTIMA_PERDA = new HashMap<>();
    private static final Map<String, String> ULTIMA_PLACA = new HashMap<>();
    private static final Map<UUID, Integer> ULTIMA_FRASE = new HashMap<>();
    /**
     * Aldeões parados olhando o sino, por grupo. Só memória: o freio é um modificador temporário de velocidade
     * (o jogo não salva) e o dano é barrado por evento. Nada disso vai para o disco, então chunk descarregado,
     * salvamento automático, crash ou o mod sair do pack nunca deixam aldeão travado.
     */
    private static final Map<String, List<Villager>> PARADOS = new HashMap<>();
    private static final Set<UUID> PARADOS_IDS = new HashSet<>();
    static final ResourceLocation MODIFICADOR_PARADO =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsHistoria.MODIFICADOR_ALDEAO_PARADO);
    private static DadosHistoria cache;

    private PrimeiroSinal() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoTick);
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoInteragirComAldeao);
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoClicarBloco);
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoQuebrar);
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoExplodir);
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoEntrarNoMundo);
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoSairDoMundo);
        NeoForge.EVENT_BUS.addListener(PrimeiroSinal::aoLevarDano);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
            if (e.getEntity() instanceof ServerPlayer p) {
                DadosHistoria d = dados(p.server);
                d.grupos.grupoDe(p.getUUID()).ifPresent(g -> {
                    if (!p.getGameProfile().getName().equals(g.membros().get(p.getUUID()))) {
                        d.grupos.atualizarNome(p.getUUID(), p.getGameProfile().getName());
                        d.setDirty();
                    }
                });
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> ULTIMA_FRASE.remove(e.getEntity().getUUID()));
        // Ao desligar, os aldeões parados voltam ao normal (o freio já não é salvo; isto é capricho) e os que a
        // versão antiga deixou com NoAI também voltam antes de salvar.
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent e) -> {
            for (String g : new ArrayList<>(PARADOS.keySet())) liberar(g);
            DadosHistoria d = dados(e.getServer());
            for (VilaMarcada v : d.vilas.values()) {
                ServerLevel nivel = e.getServer().getLevel(v.dimensao);
                if (nivel != null && !v.congelados.isEmpty()) devolverAntigos(nivel, v, d);
            }
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
            OUVINTES.clear();
            ULTIMA_PERDA.clear();
            ULTIMA_PLACA.clear();
            ULTIMA_FRASE.clear();
            PARADOS.clear();
            PARADOS_IDS.clear();
            cache = null;
        });
    }

    static DadosHistoria dados(MinecraftServer s) {
        if (cache == null) cache = DadosHistoria.de(s);
        return cache;
    }

    // ------------------------------------------------------------------ começar

    /** Resultado para o admin: mensagem e se deu certo. */
    public record Resposta(boolean ok, Component mensagem) {
        static Resposta erro(String chave, Object... args) {
            return new Resposta(false, Component.translatable(chave, args));
        }
    }

    /**
     * Solta o Primeiro Sinal para o grupo.
     * @param nivelPonto e {@code ponto}: onde procurar a vila (admin). Null: ponto ancorado, ou perto do grupo.
     */
    public static Resposta comecar(MinecraftServer s, String nomeGrupo, ServerLevel nivelPonto, BlockPos ponto) {
        DadosHistoria d = dados(s);
        Optional<Grupos.Grupo> og = d.grupos.grupo(nomeGrupo);
        if (og.isEmpty()) return Resposta.erro("odteste.evento.erro.sem_grupo", nomeGrupo);
        Grupos.Grupo g = og.get();
        if (d.vilas.containsKey(g.nome())) return Resposta.erro("odteste.evento.erro.ja_marcada", g.nome());

        // De onde procurar a vila.
        ServerLevel nivel;
        BlockPos origem;
        int raio;
        if (nivelPonto != null && ponto != null) {
            nivel = nivelPonto;
            origem = ponto;
            raio = RegrasAto1.RAIO_BUSCA_VILA_PONTO;
        } else if (d.ancoras.containsKey(IdsHistoria.LUGAR_VILA_DO_SINO)) {
            GlobalPos a = d.ancoras.get(IdsHistoria.LUGAR_VILA_DO_SINO);
            nivel = s.getLevel(a.dimension());
            origem = a.pos();
            raio = RegrasAto1.RAIO_BUSCA_VILA_PONTO;
            if (nivel == null) return Resposta.erro("odteste.evento.erro.dimensao");
        } else {
            ServerPlayer membro = membrosOnline(s, g).stream().findFirst().orElse(null);
            if (membro == null) return Resposta.erro("odteste.evento.erro.ninguem_online", g.nome());
            nivel = membro.serverLevel();
            origem = membro.blockPosition();
            raio = RegrasAto1.RAIO_BUSCA_VILA;
        }

        MundoReal mundo = new MundoReal(nivel);
        Optional<BlockPos> achouSino = acharSino(nivel, mundo, origem, raio, d);
        if (achouSino.isEmpty()) return Resposta.erro("odteste.evento.erro.sem_vila", origem.getX(), origem.getY(), origem.getZ());
        BlockPos sino = achouSino.get();

        Backup blocos = new Backup();
        VilaMarcada v = new VilaMarcada(g.nome(), nivel.dimension(), sino, blocos);

        // Bloco de notas ao lado do sino, baú trancado e placa em branco: sem eles não há enigma, então tudo ou nada.
        Optional<BlockPos> nota = acharLugarNoChao(mundo, sino, 1, 3, -1, 1, List.of());
        if (nota.isEmpty()) return Resposta.erro("odteste.evento.erro.sem_espaco", sino.getX(), sino.getY(), sino.getZ());
        Optional<BlockPos[]> bauEPlaca = acharBauEPlaca(mundo, sino, nota.get());
        if (bauEPlaca.isEmpty()) return Resposta.erro("odteste.evento.erro.sem_espaco", sino.getX(), sino.getY(), sino.getZ());

        colocar(nivel, blocos, nota.get(), Blocks.NOTE_BLOCK.defaultBlockState());
        v.nota = nota.get();
        BlockPos bau = bauEPlaca.get()[0];
        BlockPos placa = bauEPlaca.get()[1];
        Direction frente = direcaoPara(bau, sino);
        colocar(nivel, blocos, bau, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, frente));
        colocar(nivel, blocos, placa, Blocks.OAK_SIGN.defaultBlockState()
                .setValue(StandingSignBlock.ROTATION, RotationSegment.convertToSegment(frente)));
        v.bau = bau;
        v.placa = placa;

        // Estandarte numa parede, de preferência dentro de uma casa (opcional: sem parede livre, segue sem ele).
        Optional<BlockPos[]> parede = acharParede(mundo, sino);
        if (parede.isPresent()) {
            BlockPos[] pf = parede.get();
            Direction f = Direction.from3DDataValue(pf[1].getX());
            BlockState st = Blocks.BLACK_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, f);
            colocar(nivel, blocos, pf[0], st);
            pintarEstandarte(nivel, pf[0]);
            v.estandarte = pf[0];
        }

        // Um aldeão some. A cama dele fica (e mancha).
        boolean sumiu = sumirComAldeao(nivel, mundo, v);

        if (d.grupos.marcar(g.nome(), IdsHistoria.MARCO_PRIMEIRO_SINAL)) v.marcosDados.add(IdsHistoria.MARCO_PRIMEIRO_SINAL);
        d.vilas.put(g.nome(), v);
        d.setDirty();

        // Texto de abertura, uma vez, para quem do grupo está online.
        Component abertura = Component.translatable("odteste.ato1.abertura", nomes(g))
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
        for (ServerPlayer p : membrosOnline(s, g)) p.sendSystemMessage(abertura);

        OdMod.LOGGER.info("Primeiro Sinal: grupo {} na vila do sino {} ({})", g.nome(), sino.toShortString(), nivel.dimension().location());
        return new Resposta(true, Component.translatable("odteste.evento.primeiro_sinal.ok", g.nome(),
                sino.getX(), sino.getY(), sino.getZ(),
                Component.translatable(parede.isPresent() ? "odteste.evento.sim" : "odteste.evento.nao"),
                Component.translatable(sumiu ? "odteste.evento.sim" : "odteste.evento.nao")));
    }

    static List<ServerPlayer> membrosOnline(MinecraftServer s, Grupos.Grupo g) {
        List<ServerPlayer> l = new ArrayList<>();
        for (UUID u : g.membros().keySet()) {
            ServerPlayer p = s.getPlayerList().getPlayer(u);
            if (p != null) l.add(p);
        }
        return l;
    }

    /** "{grupo}" dos textos: os nomes do grupo em lista. O "e" vem da tradução de quem lê. */
    static Component nomes(Grupos.Grupo g) {
        List<String> n = g.nomes();
        if (n.size() <= 1) return Component.literal(n.isEmpty() ? "" : n.get(0));
        return Component.translatable("odteste.texto.lista",
                String.join(", ", n.subList(0, n.size() - 1)), n.get(n.size() - 1));
    }

    /** Sino de vila (ponto de encontro dos aldeões) mais perto, só em chunk carregado, sem dono e livre. */
    private static Optional<BlockPos> acharSino(ServerLevel nivel, MundoReal mundo, BlockPos origem, int raio, DadosHistoria d) {
        PoiManager poi = nivel.getPoiManager();
        int c = (raio >> 4) + 1;
        ChunkPos centro = new ChunkPos(origem);
        List<BlockPos> achados = new ArrayList<>();
        for (int dx = -c; dx <= c; dx++) {
            for (int dz = -c; dz <= c; dz++) {
                int cx = centro.x + dx;
                int cz = centro.z + dz;
                // Só chunks já carregados: nunca lê do disco nem gera terreno.
                if (!nivel.hasChunk(cx, cz)) continue;
                poi.getInChunk(h -> h.is(PoiTypes.MEETING), new ChunkPos(cx, cz), PoiManager.Occupancy.ANY)
                        .map(PoiRecord::getPos).forEach(achados::add);
            }
        }
        return achados.stream()
                .filter(p -> p.distSqr(origem) <= (double) raio * raio)
                .filter(p -> nivel.getBlockState(p).is(Blocks.BELL))
                .filter(p -> !mundo.temDono(p.getX(), p.getZ()))
                .filter(p -> d.vilas.values().stream().noneMatch(v -> v.sino.equals(p) && v.dimensao == nivel.dimension()))
                .min(Comparator.comparingDouble(p -> p.distSqr(origem)));
    }

    /** Um lugar livre com chão firme, a uma distância (horizontal) de {@code min} a {@code max} do centro. */
    private static Optional<BlockPos> acharLugarNoChao(MundoReal mundo, BlockPos centro, int min, int max,
                                                       int dyMin, int dyMax, List<BlockPos> evitar) {
        BlockPos melhor = null;
        double melhorD = Double.MAX_VALUE;
        for (int dx = -max; dx <= max; dx++) {
            for (int dz = -max; dz <= max; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist < min) continue;
                for (int dy = dyMin; dy <= dyMax; dy++) {
                    BlockPos p = centro.offset(dx, dy, dz);
                    if (evitar.contains(p) || !livre(mundo, p) || !mundo.firme(p.below())) continue;
                    double dd = p.distSqr(centro);
                    if (dd < melhorD) {
                        melhorD = dd;
                        melhor = p;
                    }
                }
            }
        }
        return Optional.ofNullable(melhor);
    }

    /** Baú no chão perto do sino e a placa logo à frente dele (do lado do sino). */
    private static Optional<BlockPos[]> acharBauEPlaca(MundoReal mundo, BlockPos sino, BlockPos nota) {
        List<BlockPos[]> opcoes = new ArrayList<>();
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                int dist = Math.max(Math.abs(dx), Math.abs(dz));
                if (dist < 3) continue;
                for (int dy = -2; dy <= 1; dy++) {
                    BlockPos bau = sino.offset(dx, dy, dz);
                    if (bau.equals(nota) || !livre(mundo, bau) || !mundo.firme(bau.below())) continue;
                    // Baú com tampa livre (bloco de cima vazio) e a placa na frente dele.
                    if (!livre(mundo, bau.above())) continue;
                    BlockPos placa = bau.relative(direcaoPara(bau, sino));
                    if (placa.equals(nota) || !livre(mundo, placa) || !mundo.firme(placa.below())) continue;
                    opcoes.add(new BlockPos[] {bau, placa});
                }
            }
        }
        return opcoes.stream().min(Comparator.comparingDouble(o -> o[0].distSqr(sino)));
    }

    /**
     * Parede para o estandarte: ar com ar embaixo (o pano pendura), parede firme atrás e, se der, sem céu em cima
     * (dentro de casa). Devolve {pos, (facing,0,0)}.
     */
    private static Optional<BlockPos[]> acharParede(MundoReal mundo, BlockPos sino) {
        ServerLevel nivel = mundo.nivel();
        int r = RegrasAto1.RAIO_ESTANDARTE;
        BlockPos melhor = null;
        Direction melhorF = null;
        double melhorNota = Double.MAX_VALUE;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz < 25) continue;
                for (int dy = -2; dy <= 5; dy++) {
                    BlockPos p = sino.offset(dx, dy, dz);
                    if (mundo.tipo(p) != Espaco.Tipo.AR || mundo.tipo(p.below()) != Espaco.Tipo.AR) continue;
                    for (Direction f : Direction.Plane.HORIZONTAL) {
                        BlockPos apoio = p.relative(f.getOpposite());
                        if (!mundo.carregado(apoio.getX(), apoio.getZ())) continue;
                        Espaco.Tipo ta = mundo.tipo(apoio);
                        if (ta == Espaco.Tipo.PROTEGIDO || ta == Espaco.Tipo.NAO_CARREGADO) continue;
                        if (!nivel.getBlockState(apoio).isFaceSturdy(nivel, apoio, f)) continue;
                        // Dentro de casa vale mais; perto do sino desempata.
                        double nota = p.distSqr(sino) + (nivel.canSeeSky(p) ? 10_000 : 0);
                        if (nota < melhorNota) {
                            melhorNota = nota;
                            melhor = p;
                            melhorF = f;
                        }
                        break;
                    }
                }
            }
        }
        if (melhor == null) return Optional.empty();
        return Optional.of(new BlockPos[] {melhor, new BlockPos(melhorF.get3DDataValue(), 0, 0)});
    }

    private static boolean livre(MundoReal mundo, BlockPos p) {
        return mundo.tipo(p).livre;
    }

    static Direction direcaoPara(BlockPos de, BlockPos para) {
        int dx = para.getX() - de.getX();
        int dz = para.getZ() - de.getZ();
        if (Math.abs(dx) >= Math.abs(dz)) return dx >= 0 ? Direction.EAST : Direction.WEST;
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void colocar(ServerLevel nivel, Backup b, BlockPos p, BlockState st) {
        BlockState antes = nivel.getBlockState(p);
        nivel.setBlock(p, st, Block.UPDATE_ALL);
        b.anotar(p, antes, nivel.getBlockState(p));
    }

    /** Símbolo provisório (cruz vermelha com centro preto) até o Modelador desenhar a Cruz de Ferro. */
    private static void pintarEstandarte(ServerLevel nivel, BlockPos p) {
        if (!(nivel.getBlockEntity(p) instanceof BannerBlockEntity be)) return;
        var padroes = nivel.registryAccess().registryOrThrow(Registries.BANNER_PATTERN);
        BannerPatternLayers camadas = new BannerPatternLayers.Builder()
                .add(padroes.getHolderOrThrow(BannerPatterns.STRAIGHT_CROSS), DyeColor.RED)
                .add(padroes.getHolderOrThrow(BannerPatterns.CIRCLE_MIDDLE), DyeColor.BLACK)
                .build();
        ItemStack molde = new ItemStack(Items.BLACK_BANNER);
        molde.set(DataComponents.BANNER_PATTERNS, camadas);
        be.applyComponentsFromItemStack(molde);
        be.setChanged();
        BlockState st = nivel.getBlockState(p);
        nivel.sendBlockUpdated(p, st, st, Block.UPDATE_ALL);
    }

    /** Escolhe um aldeão com cama, guarda ele inteiro e tira do mundo. A cama fica. */
    private static boolean sumirComAldeao(ServerLevel nivel, MundoReal mundo, VilaMarcada v) {
        List<Villager> aldeoes = nivel.getEntitiesOfClass(Villager.class,
                new AABB(v.sino).inflate(RegrasAto1.RAIO_ALDEOES),
                a -> a.isAlive() && !a.isBaby() && !a.isPassenger() && !a.isVehicle()
                        && !mundo.temDono(a.getBlockX(), a.getBlockZ()));
        List<Villager> comCama = new ArrayList<>();
        for (Villager a : aldeoes) {
            Optional<GlobalPos> casa = a.getBrain().getMemory(MemoryModuleType.HOME);
            if (casa.isPresent() && casa.get().dimension() == nivel.dimension()
                    && mundo.carregado(casa.get().pos().getX(), casa.get().pos().getZ())
                    && nivel.getBlockState(casa.get().pos()).is(net.minecraft.tags.BlockTags.BEDS)) {
                comCama.add(a);
            }
        }
        if (comCama.isEmpty()) return false;
        Villager escolhido = comCama.get(nivel.getRandom().nextInt(comCama.size()));
        CompoundTag salvo = new CompoundTag();
        if (!escolhido.saveAsPassenger(salvo)) return false;
        v.aldeao = salvo;
        v.cama = escolhido.getBrain().getMemory(MemoryModuleType.HOME).map(GlobalPos::pos).orElse(null);
        escolhido.discard();
        return true;
    }

    // ------------------------------------------------------------------ desfazer

    /** Desfaz tudo: blocos, aldeão, aldeões parados, trocas (voltam sozinhas) e os marcos que o evento deu. */
    public static Resposta desfazer(MinecraftServer s, String nomeGrupo) {
        DadosHistoria d = dados(s);
        Optional<String> nome = Grupos.normalizar(nomeGrupo);
        VilaMarcada v = nome.map(d.vilas::get).orElse(null);
        if (v == null) return Resposta.erro("odteste.evento.erro.nada_para_desfazer", nomeGrupo);
        ServerLevel nivel = s.getLevel(v.dimensao);
        if (nivel == null) return Resposta.erro("odteste.evento.erro.dimensao");
        // Tudo precisa estar carregado: assim o desfazer nunca carrega chunk nem deixa coisa pela metade.
        List<BlockPos> conferir = new ArrayList<>(v.blocos.posicoes());
        conferir.add(v.sino);
        if (v.cama != null) conferir.add(v.cama);
        BlockPos ondeAldeao = v.aldeao == null ? null : posicaoSalva(v.aldeao);
        if (ondeAldeao != null) conferir.add(ondeAldeao);
        for (BlockPos p : conferir) {
            if (!nivel.hasChunk(p.getX() >> 4, p.getZ() >> 4)) {
                return Resposta.erro("odteste.evento.erro.descarregado", p.getX(), p.getY(), p.getZ());
            }
        }

        // O aldeão volta primeiro. Se não voltar, nada muda: a vila e o aldeão salvo ficam para tentar de novo.
        boolean voltou = false;
        if (v.aldeao != null) {
            Entity e = EntityType.loadEntityRecursive(v.aldeao.copy(), nivel, x -> x);
            if (e != null) {
                if (nivel.getEntity(e.getUUID()) != null) e.setUUID(UUID.randomUUID());
                voltou = nivel.addFreshEntity(e);
            }
            if (!voltou) {
                OdMod.LOGGER.warn("Primeiro Sinal: o aldeão do grupo {} não voltou no desfazer; a vila fica marcada", v.grupo);
                BlockPos p = ondeAldeao == null ? v.sino : ondeAldeao;
                return Resposta.erro("odteste.evento.erro.aldeao_nao_voltou", v.grupo, p.getX(), p.getY(), p.getZ());
            }
        }

        int mantidos = v.blocos.restaurar(nivel);
        liberar(v.grupo);
        // Aldeões que a versão antiga deixou com NoAI salvo: voltam agora ou quando o chunk carregar.
        for (Map.Entry<UUID, Integer> e : v.congelados.entrySet()) {
            if (nivel.getEntity(e.getKey()) instanceof Villager a) {
                devolver(a, e.getValue());
            } else {
                d.descongelarDepois.put(e.getKey(), e.getValue());
            }
        }
        for (String m : v.marcosDados) d.grupos.desmarcar(v.grupo, m);
        d.vilas.remove(v.grupo);
        OUVINTES.remove(v.grupo);
        ULTIMA_PLACA.remove(v.grupo);
        d.setDirty();
        return new Resposta(true, Component.translatable("odteste.evento.desfazer.ok", v.grupo, mantidos,
                Component.translatable(v.aldeao == null ? "odteste.evento.nao" : voltou ? "odteste.evento.sim" : "odteste.evento.falhou")));
    }

    /** Onde o aldeão estava quando sumiu (chave "Pos" do jogo), ou null se o arquivo não tem. */
    private static BlockPos posicaoSalva(CompoundTag aldeao) {
        net.minecraft.nbt.ListTag pos = aldeao.getList("Pos", net.minecraft.nbt.Tag.TAG_DOUBLE);
        if (pos.size() != 3) return null;
        return BlockPos.containing(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
    }

    private static void devolver(Villager a, int como) {
        a.setNoAi((como & VilaMarcada.ERA_SEM_IA) != 0);
        a.setInvulnerable((como & VilaMarcada.ERA_INVULNERAVEL) != 0);
    }

    // ------------------------------------------------------------------ tick

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        DadosHistoria d = dados(s);
        if (d.vilas.isEmpty()) return;
        boolean segundo = s.getTickCount() % 20 == 0;
        for (VilaMarcada v : d.vilas.values()) {
            ServerLevel nivel = s.getLevel(v.dimensao);
            if (nivel == null) continue;
            if (!nivel.hasChunk(v.sino.getX() >> 4, v.sino.getZ() >> 4)) {
                // Vila longe de todo mundo: ninguém ouve, nada roda.
                OUVINTES.remove(v.grupo);
                liberar(v.grupo);
                continue;
            }
            if (segundo) porSegundo(s, nivel, v, d);
            tocar(nivel, v);
        }
    }

    private static void porSegundo(MinecraftServer s, ServerLevel nivel, VilaMarcada v, DadosHistoria d) {
        Vec3 centro = Vec3.atCenterOf(v.sino);
        long agora = nivel.getGameTime();
        Map<UUID, Ouvinte> ouvintes = OUVINTES.computeIfAbsent(v.grupo, k -> new HashMap<>());
        boolean alguemPertoDosAldeoes = false;
        boolean alguemPertoDaCama = false;
        boolean alguemPertoDaPlaca = false;
        List<UUID> presentes = new ArrayList<>();
        for (ServerPlayer p : nivel.players()) {
            if (p.isSpectator()) continue;
            double d2 = p.position().distanceToSqr(centro);
            if (d2 > RegrasAto1.RAIO_OUVIR * RegrasAto1.RAIO_OUVIR) continue;
            presentes.add(p.getUUID());
            ouvintes.computeIfAbsent(p.getUUID(), u -> novoOuvinte(p, agora));
            if (d2 <= RegrasAto1.RAIO_ALDEOES * RegrasAto1.RAIO_ALDEOES) alguemPertoDosAldeoes = true;
            if (v.cama != null && p.position().distanceToSqr(Vec3.atCenterOf(v.cama)) <= RegrasAto1.RAIO_CAMA * RegrasAto1.RAIO_CAMA) {
                alguemPertoDaCama = true;
            }
            if (v.placa != null && p.position().distanceToSqr(Vec3.atCenterOf(v.placa)) <= RegrasAto1.RAIO_PLACA * RegrasAto1.RAIO_PLACA) {
                alguemPertoDaPlaca = true;
            }
        }
        ouvintes.keySet().retainAll(presentes);

        if (alguemPertoDosAldeoes) {
            congelar(nivel, v, d);
        } else {
            liberar(v.grupo);
            if (!v.congelados.isEmpty()) devolverAntigos(nivel, v, d);
        }

        if (alguemPertoDaCama && nivel.hasChunk(v.cama.getX() >> 4, v.cama.getZ() >> 4)) manchar(nivel, v.cama);
        if (alguemPertoDaPlaca && !v.resolvido) lerPlaca(s, nivel, v, d);
    }

    private static Ouvinte novoOuvinte(ServerPlayer p, long agora) {
        Ouvinte o = new Ouvinte();
        reiniciar(o, p, agora);
        return o;
    }

    private static void reiniciar(Ouvinte o, ServerPlayer p, long agora) {
        // Exposição Média (Marcado) para cima: o sino bate mais devagar só para quem já foi tocado.
        int unidade = RegrasAto1.unidade(Degrau.de(DadosJogador.exposicao(p)).peloMenos(Degrau.MARCADO));
        o.inicio = agora;
        o.sons = RegrasAto1.sons(RegrasAto1.PALAVRA, unidade);
        o.duracao = RegrasAto1.duracaoComPausa(RegrasAto1.PALAVRA, unidade);
        o.proximo = 0;
        o.pertoTodo = true;
        o.avaliado = false;
    }

    private static void tocar(ServerLevel nivel, VilaMarcada v) {
        Map<UUID, Ouvinte> ouvintes = OUVINTES.get(v.grupo);
        if (ouvintes == null || ouvintes.isEmpty()) return;
        long agora = nivel.getGameTime();
        Iterator<Map.Entry<UUID, Ouvinte>> it = ouvintes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Ouvinte> e = it.next();
            if (!(nivel.getPlayerByUUID(e.getKey()) instanceof ServerPlayer p)) {
                it.remove();
                continue;
            }
            Ouvinte o = e.getValue();
            long t = agora - o.inicio;
            if (t < 0) {
                reiniciar(o, p, agora); // relógio voltou (mundo trocado): começa de novo
                continue;
            }
            while (o.proximo < o.sons.size() && o.sons.get(o.proximo).tick() <= t) {
                RegrasAto1.Som som = o.sons.get(o.proximo++);
                if (som.fonte() == RegrasAto1.Fonte.SINO
                        && p.position().distanceToSqr(Vec3.atCenterOf(v.sino)) > RegrasAto1.RAIO_PERTO * RegrasAto1.RAIO_PERTO) {
                    o.pertoTodo = false;
                }
                emitir(nivel, v, p, som);
            }
            if (!o.avaliado && o.proximo >= o.sons.size()) {
                o.avaliado = true;
                // Ouviu a sequência inteira de perto: o som entra na cabeça. Contar de longe não tira nada.
                long ultima = ULTIMA_PERDA.getOrDefault(p.getUUID(), -1L);
                if (!p.isCreative() && RegrasAto1.perdeLucidez(o.pertoTodo, agora, ultima)) {
                    Lucidez.perder(p, RegrasAto1.PERDA_LUCIDEZ_SEQUENCIA);
                    ULTIMA_PERDA.put(p.getUUID(), agora);
                }
            }
            if (t >= o.duracao) reiniciar(o, p, agora);
        }
    }

    private static Holder<SoundEvent> som(SoundEvent s) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(s);
    }

    /** Um som só para este jogador. Longe, sai de um ponto perto dele na direção do sino (ouve-se de longe sem aumentar o volume). */
    private static void emitir(ServerLevel nivel, VilaMarcada v, ServerPlayer p, RegrasAto1.Som som) {
        boolean doSino = som.fonte() == RegrasAto1.Fonte.SINO || v.nota == null;
        Vec3 fonte = Vec3.atCenterOf(doSino ? v.sino : v.nota);
        Vec3 olhos = p.getEyePosition();
        double dist = olhos.distanceTo(fonte);
        Vec3 onde;
        float volume;
        if (dist <= RegrasAto1.RAIO_SOM_DIRETO) {
            onde = fonte;
            volume = RegrasAto1.VOLUME_PERTO;
        } else {
            onde = olhos.add(fonte.subtract(olhos).normalize().scale(RegrasAto1.RAIO_SOM_DIRETO - 4));
            volume = RegrasAto1.VOLUME_LONGE;
        }
        float tom = som.traco() ? RegrasAto1.TOM_TRACO : RegrasAto1.TOM_PONTO;
        long semente = nivel.getRandom().nextLong();
        if (som.fonte() == RegrasAto1.Fonte.SINO) {
            p.connection.send(new ClientboundSoundPacket(som(SoundEvents.BELL_BLOCK), SoundSource.BLOCKS,
                    onde.x, onde.y, onde.z, volume, tom, semente));
            if (som.traco()) {
                p.connection.send(new ClientboundSoundPacket(som(SoundEvents.BELL_RESONATE), SoundSource.BLOCKS, onde.x, onde.y, onde.z,
                        RegrasAto1.VOLUME_LONGE, RegrasAto1.TOM_TRACO, semente));
            }
            // O sino balança na tela de quem está perto (só no cliente: os aldeões não "ouvem" o sino de verdade).
            BlockState st = nivel.getBlockState(v.sino);
            if (dist < 48 && st.getBlock() instanceof BellBlock) {
                p.connection.send(new ClientboundBlockEventPacket(v.sino, st.getBlock(), 1,
                        st.getValue(BellBlock.FACING).get3DDataValue()));
            }
        } else {
            // Bloco de notas responde: grave para traço, aguda para ponto.
            Holder<SoundEvent> nota = som.traco() ? SoundEvents.NOTE_BLOCK_BASS : SoundEvents.NOTE_BLOCK_HARP;
            p.connection.send(new ClientboundSoundPacket(nota, SoundSource.RECORDS, onde.x, onde.y, onde.z, volume, tom, semente));
            if (dist < 32 && v.nota != null) {
                nivel.sendParticles(p, ParticleTypes.NOTE, false, v.nota.getX() + 0.5, v.nota.getY() + 1.2,
                        v.nota.getZ() + 0.5, 0, som.traco() ? 0.0 : 0.6, 0, 0, 1);
            }
        }
    }

    private static final DustParticleOptions SANGUE = new DustParticleOptions(new Vector3f(0.42f, 0.02f, 0.02f), 1.0f);

    /** A cama do aldeão que sumiu: poucas gotas vermelhas no lençol (provisório até a textura de cama manchada). */
    private static void manchar(ServerLevel nivel, BlockPos cama) {
        if (!nivel.getBlockState(cama).is(net.minecraft.tags.BlockTags.BEDS)) return;
        nivel.sendParticles(SANGUE, cama.getX() + 0.5, cama.getY() + 0.6, cama.getZ() + 0.5, 3, 0.3, 0.02, 0.3, 0);
    }

    /**
     * Aldeões perto do sino param e olham para ele (até {@link RegrasAto1#MAX_ALDEOES}). O freio é um modificador
     * temporário de velocidade e o olhar é uma memória que o jogo não salva: nada fica no disco.
     */
    private static void congelar(ServerLevel nivel, VilaMarcada v, DadosHistoria d) {
        List<Villager> parados = PARADOS.computeIfAbsent(v.grupo, k -> new ArrayList<>());
        Iterator<Villager> it = parados.iterator();
        while (it.hasNext()) {
            Villager a = it.next();
            if (a.isRemoved() || !a.isAlive()) {
                PARADOS_IDS.remove(a.getUUID());
                it.remove();
            }
        }
        List<Villager> aldeoes = nivel.getEntitiesOfClass(Villager.class,
                new AABB(v.sino).inflate(RegrasAto1.RAIO_ALDEOES), Villager::isAlive);
        for (Villager a : aldeoes) {
            // Parado pela versão antiga (NoAI salvo): volta ao normal antes.
            Integer antigo = v.congelados.remove(a.getUUID());
            if (antigo != null) {
                devolver(a, antigo);
                d.setDirty();
            }
            if (PARADOS_IDS.contains(a.getUUID()) || parados.size() >= RegrasAto1.MAX_ALDEOES) continue;
            parar(a, v.sino);
            parados.add(a);
            PARADOS_IDS.add(a.getUUID());
        }
        Vec3 alvo = Vec3.atCenterOf(v.sino);
        for (Villager a : parados) {
            a.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(v.sino));
            a.getLookControl().setLookAt(alvo.x, alvo.y, alvo.z);
        }
    }

    private static void parar(Villager a, BlockPos sino) {
        AttributeInstance vel = a.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null && !vel.hasModifier(MODIFICADOR_PARADO)) {
            vel.addTransientModifier(new AttributeModifier(MODIFICADOR_PARADO, -1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        a.getNavigation().stop();
        a.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        olharPara(a, Vec3.atCenterOf(sino));
    }

    private static void soltar(Villager a) {
        AttributeInstance vel = a.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel != null) vel.removeModifier(MODIFICADOR_PARADO);
        a.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
    }

    /** Solta todos os aldeões parados do grupo (ninguém perto, chunk descarregado, desfazer). */
    private static void liberar(String grupo) {
        List<Villager> l = PARADOS.remove(grupo);
        if (l == null) return;
        for (Villager a : l) {
            PARADOS_IDS.remove(a.getUUID());
            if (!a.isRemoved()) soltar(a);
        }
    }

    /** Aldeões que a versão antiga (até o PR #22) deixou com NoAI/invulnerável salvo: voltam como eram. */
    private static void devolverAntigos(ServerLevel nivel, VilaMarcada v, DadosHistoria d) {
        Iterator<Map.Entry<UUID, Integer>> it = v.congelados.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            if (nivel.getEntity(e.getKey()) instanceof Villager a) {
                devolver(a, e.getValue());
                it.remove();
                d.setDirty();
            }
            // Não carregado: fica na lista e volta ao normal quando carregar (aoEntrarNoMundo).
        }
    }

    private static void olharPara(Villager a, Vec3 alvo) {
        double dx = alvo.x - a.getX();
        double dz = alvo.z - a.getZ();
        double dy = alvo.y - a.getEyeY();
        float giro = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90f;
        float inclinacao = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);
        a.setYRot(giro);
        a.setYHeadRot(giro);
        a.setYBodyRot(giro);
        a.setXRot(Mth.clamp(inclinacao, -40f, 40f));
    }

    private static void lerPlaca(MinecraftServer s, ServerLevel nivel, VilaMarcada v, DadosHistoria d) {
        if (!(nivel.getBlockEntity(v.placa) instanceof SignBlockEntity placa)) return;
        List<String> frente = new ArrayList<>(4);
        List<String> verso = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            frente.add(placa.getFrontText().getMessage(i, false).getString());
            verso.add(placa.getBackText().getMessage(i, false).getString());
        }
        String f = RegrasAto1.juntar(frente);
        String b = RegrasAto1.juntar(verso);
        String escrito = f.isEmpty() && b.isEmpty() ? "" : f + "|" + b;
        if (RegrasAto1.placaCerta(frente, verso, RegrasAto1.PALAVRA)) {
            resolver(s, nivel, v, d);
        } else if (!escrito.isEmpty() && !escrito.equals(ULTIMA_PLACA.get(v.grupo))) {
            // Palavra errada: o baú só range, trancado.
            nivel.playSound(null, v.bau, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.6f, 0.6f);
        }
        ULTIMA_PLACA.put(v.grupo, escrito);
    }

    private static void resolver(MinecraftServer s, ServerLevel nivel, VilaMarcada v, DadosHistoria d) {
        v.resolvido = true;
        if (d.grupos.marcar(v.grupo, IdsHistoria.MARCO_ENIGMA_SINO)) v.marcosDados.add(IdsHistoria.MARCO_ENIGMA_SINO);
        if (nivel.getBlockEntity(v.bau) instanceof ChestBlockEntity bau) {
            bau.setItem(11, livroDoCarniceiro());
            bau.setItem(15, metadeEsquerdaDoMapa());
            bau.setChanged();
        }
        nivel.playSound(null, v.bau, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.6f, 0.5f);
        d.setDirty();
        // O jogo avisa o admin (o ritmo dos eventos grandes é dele).
        Component aviso = Component.translatable("odteste.ato1.admin.enigma_sino",
                        d.grupos.grupo(v.grupo).map(PrimeiroSinal::nomes).orElse(Component.literal(v.grupo)))
                .withStyle(ChatFormatting.GOLD);
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (p.hasPermissions(2)) p.sendSystemMessage(aviso);
        }
        OdMod.LOGGER.info("Enigma do sino resolvido pelo grupo {}", v.grupo);
    }

    private static void peca(ItemStack item, String id) {
        CustomData.update(DataComponents.CUSTOM_DATA, item,
                t -> t.putString(IdsHistoria.CHAVE_PECA, OdMod.MODID + ":" + id));
    }

    /** Livro do Carniceiro, página 1 (texto provisório: a Lore escreve o definitivo). */
    static ItemStack livroDoCarniceiro() {
        ItemStack livro = new ItemStack(Items.WRITTEN_BOOK);
        List<Filterable<Component>> paginas = List.of(
                Filterable.passThrough(Component.translatable("odteste.ato1.livro_carniceiro.1.pagina1")),
                Filterable.passThrough(Component.translatable("odteste.ato1.livro_carniceiro.1.pagina2")));
        livro.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(Filterable.passThrough("Livro do Carniceiro"), "?", 0, paginas, true));
        livro.set(DataComponents.CUSTOM_NAME, Component.translatable("odteste.ato1.livro_carniceiro.nome")
                .withStyle(st -> st.withItalic(false)));
        peca(livro, IdsDados.PECA_LIVRO_CARNICEIRO_1);
        return livro;
    }

    /** Metade esquerda do Mapa Rasgado (provisório: papel com nome; o mapa de verdade vem no Enigma 2). */
    static ItemStack metadeEsquerdaDoMapa() {
        ItemStack mapa = new ItemStack(Items.PAPER);
        mapa.set(DataComponents.CUSTOM_NAME, Component.translatable("odteste.ato1.mapa_esquerda.nome")
                .withStyle(st -> st.withItalic(false)));
        mapa.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("odteste.ato1.mapa_esquerda.descricao")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
        peca(mapa, IdsDados.PECA_MAPA_ESQUERDA);
        return mapa;
    }

    // ------------------------------------------------------------------ eventos do jogo

    private static VilaMarcada vilaDoBloco(Level nivel, BlockPos p) {
        if (cache == null) return null;
        for (VilaMarcada v : cache.vilas.values()) {
            if (v.dimensao != nivel.dimension()) continue;
            if (p.equals(v.nota) || p.equals(v.bau) || p.equals(v.placa) || p.equals(v.estandarte)) return v;
        }
        return null;
    }

    /** Aldeão parado não troca: a troca vira uma frase curta. */
    private static void aoInteragirComAldeao(PlayerInteractEvent.EntityInteract e) {
        if (e.getLevel().isClientSide() || !(e.getTarget() instanceof Villager a)) return;
        if (!PARADOS_IDS.contains(a.getUUID())) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (e.getHand() != InteractionHand.MAIN_HAND || !(e.getEntity() instanceof ServerPlayer p)) return;
        int f = RegrasAto1.fraseTroca(ULTIMA_FRASE.getOrDefault(p.getUUID(), 0), p.getRandom().nextInt(1000));
        ULTIMA_FRASE.put(p.getUUID(), f);
        p.displayClientMessage(Component.translatable("odteste.ato1.troca." + f)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }

    /** Baú trancado até a placa dizer a palavra. */
    private static void aoClicarBloco(PlayerInteractEvent.RightClickBlock e) {
        if (e.getLevel().isClientSide()) return;
        VilaMarcada v = vilaDoBloco(e.getLevel(), e.getPos());
        if (v == null || v.resolvido || !e.getPos().equals(v.bau)) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (e.getHand() != InteractionHand.MAIN_HAND || !(e.getEntity() instanceof ServerPlayer p)) return;
        p.connection.send(new ClientboundSoundPacket(som(SoundEvents.CHEST_LOCKED),
                SoundSource.BLOCKS, v.bau.getX() + 0.5, v.bau.getY() + 0.5, v.bau.getZ() + 0.5, 0.6f, 0.6f,
                p.getRandom().nextLong()));
        p.displayClientMessage(Component.translatable("odteste.ato1.bau.trancado")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }

    /** Os blocos que pusemos na vila não quebram (o admin tira com o desfazer). */
    private static void aoQuebrar(BlockEvent.BreakEvent e) {
        if (!(e.getLevel() instanceof Level nivel) || nivel.isClientSide()) return;
        if (vilaDoBloco(nivel, e.getPos()) != null) e.setCanceled(true);
    }

    private static void aoExplodir(ExplosionEvent.Detonate e) {
        if (e.getLevel().isClientSide() || cache == null || cache.vilas.isEmpty()) return;
        e.getAffectedBlocks().removeIf(p -> vilaDoBloco(e.getLevel(), p) != null);
    }

    /**
     * Aldeão que a versão antiga deixou parado (NoAI salvo no disco) volta ao normal quando carrega.
     * Usa {@link #dados}, não o cache: quando o servidor liga, os chunks do spawn carregam antes do primeiro tick.
     */
    private static void aoEntrarNoMundo(EntityJoinLevelEvent e) {
        if (!(e.getEntity() instanceof Villager a) || e.getLevel().isClientSide()) return;
        MinecraftServer s = e.getLevel().getServer();
        if (s == null) return;
        DadosHistoria d = dados(s);
        Integer como = d.descongelarDepois.remove(a.getUUID());
        if (como != null) {
            devolver(a, como);
            d.setDirty();
            return;
        }
        for (VilaMarcada v : d.vilas.values()) {
            Integer c = v.congelados.remove(a.getUUID());
            if (c != null) {
                // Se tiver jogador perto, o próximo segundo para de novo (agora sem nada salvo).
                devolver(a, c);
                d.setDirty();
                return;
            }
        }
    }

    /** Aldeão parado que sai do mundo (chunk descarregou, morreu, trocou de dimensão) sai da lista e do freio. */
    private static void aoSairDoMundo(EntityLeaveLevelEvent e) {
        if (!(e.getEntity() instanceof Villager a) || e.getLevel().isClientSide()) return;
        if (!PARADOS_IDS.remove(a.getUUID())) return;
        soltar(a);
        for (List<Villager> l : PARADOS.values()) l.removeIf(x -> x == a);
    }

    /** Parado não foge: sem isso, a vila perderia aldeões para os zumbis por nossa causa. /kill ainda funciona. */
    private static void aoLevarDano(LivingIncomingDamageEvent e) {
        if (!(e.getEntity() instanceof Villager a) || !PARADOS_IDS.contains(a.getUUID())) return;
        if (e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        e.setCanceled(true);
    }
}
