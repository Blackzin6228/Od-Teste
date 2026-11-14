package com.nikolas.odteste.historia.vilacalada;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.blocos.capela.BlocosCapela;
import com.nikolas.odteste.estruturas.MundoReal;
import com.nikolas.odteste.historia.gatilhos.IdsGatilhos;
import com.nikolas.odteste.historia.livros.LivrosHistoria;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import com.nikolas.odteste.itens.Itens;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code /odteste vila_calada preparar [canto]}: põe na Vila Calada v2 colocada à mão o que o template não guarda
 * (posições em {@link RegrasPrepararVila}): o Livro do Sineiro no atril, a comida no baú do poço, a placa do poço,
 * a vela da capela, as poças de sangue, o Martelo do Ferreiro (moldura no campanário) e o peitoril da Moça do Terço.
 *
 * Nunca mexe em bloco de jogador: só ocupa ar ou o bloco que o próprio template pôs ali (vela comum, pó de
 * redstone), fora de claim, em chunk carregado. Idempotente: o que já está lá fica; o baú do poço enche uma vez só
 * (fica salvo em {@code preparados}); o livro volta ao atril se alguém levou (cada um pode ter a sua cópia).
 */
final class PrepararVilaCalada {
    private PrepararVilaCalada() {}

    /**
     * Acha o canto pelo sino rachado: sino (ponto de encontro indexado pelo jogo) perto da âncora com o atril no
     * lugar certo ao lado. Só chunk carregado. Vazio se não achou (aí o mestre passa o canto).
     */
    static Optional<BlockPos> acharCanto(ServerLevel nivel, BlockPos ancora) {
        PoiManager poi = nivel.getPoiManager();
        int r = RegrasPrepararVila.RAIO_PROCURA_SINO;
        int c = (r >> 4) + 1;
        ChunkPos centro = new ChunkPos(ancora);
        List<BlockPos> sinos = new ArrayList<>();
        for (int dx = -c; dx <= c; dx++) {
            for (int dz = -c; dz <= c; dz++) {
                int cx = centro.x + dx;
                int cz = centro.z + dz;
                if (!nivel.hasChunk(cx, cz)) continue;
                poi.getInChunk(h -> h.is(PoiTypes.MEETING), new ChunkPos(cx, cz), PoiManager.Occupancy.ANY)
                        .map(PoiRecord::getPos).forEach(sinos::add);
            }
        }
        return sinos.stream()
                .filter(p -> p.distSqr(ancora) <= (double) r * r)
                .map(p -> {
                    int[] k = RegrasPrepararVila.canto(p.getX(), p.getY(), p.getZ(), RegrasPrepararVila.SINO);
                    return new BlockPos(k[0], k[1], k[2]);
                })
                .filter(k -> nivel.getBlockState(em(k, RegrasPrepararVila.ATRIL)).is(Blocks.LECTERN))
                .min(Comparator.comparingDouble(k -> k.distSqr(ancora)));
    }

    static BlockPos em(BlockPos canto, RegrasPrepararVila.Peca p) {
        return canto.offset(p.x(), p.y(), p.z());
    }

    /** Prepara tudo. Devolve uma linha por peça, para o mestre. */
    static List<Component> preparar(ServerLevel nivel, BlockPos canto, DadosVilaCalada dv) {
        MundoReal mundo = new MundoReal(nivel);
        List<Component> r = new ArrayList<>();
        r.add(livro(nivel, mundo, canto));
        r.add(bauPoco(nivel, mundo, canto, dv));
        r.add(placaPoco(nivel, mundo, canto));
        r.add(trocar(nivel, mundo, canto, RegrasPrepararVila.VELA, BlocosCapela.VELA.get().defaultBlockState(), true));
        for (RegrasPrepararVila.Peca p : RegrasPrepararVila.POCAS) {
            r.add(trocar(nivel, mundo, canto, p, BlocosCapela.POCA.get().defaultBlockState(), false));
        }
        r.add(martelo(nivel, mundo, canto));
        r.add(peitoril(nivel, mundo, canto));
        OdMod.LOGGER.info("Vila Calada: preparada a partir do canto {}", canto.toShortString());
        return r;
    }

    private enum Fim { POSTO, JA, BLOQUEADO, LONGE, DONO }

    private static Component linha(RegrasPrepararVila.Peca p, BlockPos onde, Fim f) {
        ChatFormatting cor = switch (f) {
            case POSTO -> ChatFormatting.GREEN;
            case JA -> ChatFormatting.GRAY;
            default -> ChatFormatting.RED;
        };
        return Component.translatable("odteste.vila_calada.preparar." + f.name().toLowerCase(java.util.Locale.ROOT),
                Component.translatable("odteste.vila_calada.peca." + p.nome()), onde.getX(), onde.getY(), onde.getZ())
                .withStyle(cor);
    }

    /** Chunk carregado e sem dono? Senão, o fim que impede. */
    private static Fim pode(MundoReal mundo, BlockPos p) {
        if (!mundo.carregado(p.getX(), p.getZ())) return Fim.LONGE;
        if (mundo.temDono(p.getX(), p.getZ())) return Fim.DONO;
        return null;
    }

    private static String id(BlockState s) {
        return BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString();
    }

    private static Component livro(ServerLevel nivel, MundoReal mundo, BlockPos canto) {
        RegrasPrepararVila.Peca peca = RegrasPrepararVila.ATRIL;
        BlockPos p = em(canto, peca);
        Fim f = pode(mundo, p);
        if (f != null) return linha(peca, p, f);
        BlockState st = nivel.getBlockState(p);
        if (!st.is(Blocks.LECTERN)) {
            if (!RegrasPrepararVila.ar(id(st))) return linha(peca, p, Fim.BLOQUEADO);
            st = Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.WEST);
            nivel.setBlock(p, st, Block.UPDATE_ALL);
        }
        if (!(nivel.getBlockEntity(p) instanceof LecternBlockEntity atril)) return linha(peca, p, Fim.BLOQUEADO);
        if (atril.hasBook()) {
            // Livro nosso já lá: nada a fazer. Outro livro (de jogador): não mexe.
            return linha(peca, p, LivrosHistoria.idDe(atril.getBook()).filter(IdsGatilhos.LIVRO_SINEIRO::equals).isPresent()
                    ? Fim.JA : Fim.BLOQUEADO);
        }
        Optional<ItemStack> livro = LivrosHistoria.criar(IdsGatilhos.LIVRO_SINEIRO, null);
        if (livro.isEmpty()) return linha(peca, p, Fim.BLOQUEADO);
        atril.setBook(livro.get());
        atril.setChanged();
        nivel.setBlock(p, st.setValue(LecternBlock.HAS_BOOK, true), Block.UPDATE_ALL);
        return linha(peca, p, Fim.POSTO);
    }

    private static Component bauPoco(ServerLevel nivel, MundoReal mundo, BlockPos canto, DadosVilaCalada dv) {
        RegrasPrepararVila.Peca peca = RegrasPrepararVila.BAU_POCO;
        BlockPos p = em(canto, peca);
        String chave = RegrasPrepararVila.chave(canto.asLong(), peca);
        if (dv.preparados.contains(chave)) return linha(peca, p, Fim.JA);
        Fim f = pode(mundo, p);
        if (f != null) return linha(peca, p, f);
        if (!(nivel.getBlockEntity(p) instanceof ChestBlockEntity bau)) return linha(peca, p, Fim.BLOQUEADO);
        dv.preparados.add(chave);
        dv.setDirty();
        if (!bau.isEmpty()) return linha(peca, p, Fim.JA);
        bau.setItem(RegrasPrepararVila.ESPACO_PAO, new ItemStack(Items.BREAD, RegrasPrepararVila.PAES));
        bau.setItem(RegrasPrepararVila.ESPACO_MACA, new ItemStack(Items.APPLE, RegrasPrepararVila.MACAS));
        bau.setChanged();
        return linha(peca, p, Fim.POSTO);
    }

    private static Component placaPoco(ServerLevel nivel, MundoReal mundo, BlockPos canto) {
        RegrasPrepararVila.Peca peca = RegrasPrepararVila.PLACA_POCO;
        BlockPos p = em(canto, peca);
        Fim f = pode(mundo, p);
        if (f != null) return linha(peca, p, f);
        if (!(nivel.getBlockEntity(p) instanceof SignBlockEntity placa)) return linha(peca, p, Fim.BLOQUEADO);
        for (int i = 0; i < 4; i++) {
            if (!placa.getFrontText().getMessage(i, false).getString().isBlank()) return linha(peca, p, Fim.JA);
        }
        SignText t = new SignText().setMessage(1, Component.translatable("odteste.vila_calada.placa_poco"));
        placa.setText(t, true);
        placa.setWaxed(true); // ninguém reescreve
        placa.setChanged();
        BlockState st = nivel.getBlockState(p);
        nivel.sendBlockUpdated(p, st, st, Block.UPDATE_ALL);
        return linha(peca, p, Fim.POSTO);
    }

    /**
     * Martelo do Ferreiro numa moldura deitada no chão do campanário, ao lado do atril (o template não guarda item).
     * Moldura nossa vazia (alguém levou o martelo): põe outro. Moldura com outra coisa: não mexe.
     */
    private static Component martelo(ServerLevel nivel, MundoReal mundo, BlockPos canto) {
        RegrasPrepararVila.Peca peca = RegrasDobre.MARTELO;
        BlockPos p = em(canto, peca);
        Fim f = pode(mundo, p);
        if (f != null) return linha(peca, p, f);
        ItemStack martelo = new ItemStack(Itens.MARTELO.get());
        for (ItemFrame q : nivel.getEntitiesOfClass(ItemFrame.class, new AABB(p))) {
            if (q.getItem().is(Itens.MARTELO.get())) return linha(peca, p, Fim.JA);
            if (q.getItem().isEmpty()) {
                q.setItem(martelo, true);
                return linha(peca, p, Fim.POSTO);
            }
            return linha(peca, p, Fim.BLOQUEADO);
        }
        if (!RegrasPrepararVila.ar(id(nivel.getBlockState(p)))) return linha(peca, p, Fim.BLOQUEADO);
        ItemFrame q = new ItemFrame(nivel, p, Direction.UP);
        if (!q.survives()) return linha(peca, p, Fim.BLOQUEADO);
        q.setItem(martelo, false);
        nivel.addFreshEntity(q);
        return linha(peca, p, Fim.POSTO);
    }

    /**
     * Peitoril da Moça do Terço: a tábua e os seis botões de pedra (o template já traz; só repõe o que falta, no ar).
     * O espaço do Sineiro (a sétima posição) nunca é preenchido aqui: é o grupo que põe.
     */
    private static Component peitoril(ServerLevel nivel, MundoReal mundo, BlockPos canto) {
        RegrasPrepararVila.Peca peca = new RegrasPrepararVila.Peca("peitoril", RegrasDobre.PEITORIL_X,
                RegrasDobre.PEITORIL_Y, RegrasDobre.PEITORIL_Z0);
        BlockPos inicio = em(canto, peca);
        Fim f = pode(mundo, inicio);
        if (f != null) return linha(peca, inicio, f);
        int[][] pos = RegrasDobre.peitoril();
        boolean posAlgo = false;
        boolean bloqueou = false;
        for (int i = 0; i < pos.length; i++) {
            BlockPos b = canto.offset(pos[i][0], pos[i][1], pos[i][2]);
            BlockPos tabua = b.below();
            if (RegrasPrepararVila.ar(id(nivel.getBlockState(tabua)))) {
                nivel.setBlock(tabua, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z), Block.UPDATE_ALL);
                posAlgo = true;
            }
            if (i >= RegrasDobre.BOTOES_NO_PEITORIL) continue; // o do Sineiro fica vazio
            String agora = id(nivel.getBlockState(b));
            if (RegrasDobre.botaoDePedra(agora)) continue;
            if (!RegrasPrepararVila.ar(agora)) {
                bloqueou = true;
                continue;
            }
            nivel.setBlock(b, Blocks.STONE_BUTTON.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.ButtonBlock.FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR)
                    .setValue(net.minecraft.world.level.block.ButtonBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
            posAlgo = true;
        }
        return linha(peca, inicio, bloqueou ? Fim.BLOQUEADO : posAlgo ? Fim.POSTO : Fim.JA);
    }

    /** Vela ou poça: entra só no ar ou no bloco que o template pôs ali. */
    private static Component trocar(ServerLevel nivel, MundoReal mundo, BlockPos canto, RegrasPrepararVila.Peca peca,
                                    BlockState nosso, boolean vela) {
        BlockPos p = em(canto, peca);
        Fim f = pode(mundo, p);
        if (f != null) return linha(peca, p, f);
        BlockState agora = nivel.getBlockState(p);
        if (agora.is(nosso.getBlock())) return linha(peca, p, Fim.JA);
        String id = id(agora);
        boolean pode = vela ? RegrasPrepararVila.velaPodeEntrar(id) : RegrasPrepararVila.pocaPodeEntrar(id);
        if (!pode) return linha(peca, p, Fim.BLOQUEADO);
        nivel.setBlock(p, nosso, Block.UPDATE_ALL);
        return linha(peca, p, Fim.POSTO);
    }
}
