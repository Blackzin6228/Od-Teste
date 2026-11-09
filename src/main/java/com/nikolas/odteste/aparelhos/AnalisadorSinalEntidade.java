package com.nikolas.odteste.aparelhos;

import com.nikolas.odteste.sinais.Sinal;
import com.nikolas.odteste.sinais.Sinais;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dados e lógica do Analisador de Sinal (só servidor).
 * - Guarda a energia (chave NBT {@code energia}, ver docs/ids.md).
 * - A cada 2 s, só com jogador a até 16 blocos: gasta 20 FE/t e procura sinal a até 8 blocos (luz do bloco).
 * - Ao ser usado: decifra o sinal em volta, ou o do item na mão, e mostra o texto e a segunda camada.
 */
public class AnalisadorSinalEntidade extends BlockEntity {
    private static final String CHAVE_ENERGIA = "energia";

    private final BateriaAparelho bateria =
            new BateriaAparelho(RegrasAnalisador.CAPACIDADE, RegrasAnalisador.ENTRADA_MAXIMA, this::setChanged);
    /** Só memória: espera entre leituras. */
    private long ultimaLeitura = Long.MIN_VALUE / 2;

    public AnalisadorSinalEntidade(BlockPos pos, BlockState estado) {
        super(Aparelhos.ANALISADOR_ENTIDADE.get(), pos, estado);
    }

    public BateriaAparelho bateria() {
        return bateria;
    }

    // ---------- tick (servidor) ----------

    public static void tickServidor(Level nivel, BlockPos pos, BlockState estado, AnalisadorSinalEntidade be) {
        if (!(nivel instanceof ServerLevel servidor)) return;
        if (!RegrasAnalisador.vezDeChecar(nivel.getGameTime(), pos.asLong())) return;
        if (!nivel.hasNearbyAlivePlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                RegrasAnalisador.RAIO_JOGADOR)) {
            return; // ninguém perto: não gasta, não procura
        }

        boolean ligado = RegrasAnalisador.ficaLigado(be.bateria.getEnergyStored());
        boolean sinal = false;
        if (ligado) {
            be.bateria.gastar(RegrasAnalisador.GASTO_CHECAGEM);
            sinal = !Sinais.perto(servidor, pos, RegrasAnalisador.RAIO_SINAL).isEmpty();
        }

        boolean eraSinal = estado.getValue(AnalisadorSinalBloco.SINAL);
        if (sinal && !eraSinal) {
            nivel.playSound(null, pos, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 0.6f, 0.5f);
        }
        if (estado.getValue(AnalisadorSinalBloco.LIGADO) != ligado || eraSinal != sinal) {
            nivel.setBlock(pos, estado.setValue(AnalisadorSinalBloco.LIGADO, ligado)
                    .setValue(AnalisadorSinalBloco.SINAL, sinal), 3);
        }
    }

    // ---------- leitura ----------

    /** Jogador usou o aparelho. {@code item} pode estar vazio: aí lê o que está em volta. */
    public void ler(ServerLevel nivel, Player jogador, ItemStack item) {
        long agora = nivel.getGameTime();
        if (!RegrasAnalisador.esperouLeitura(agora, ultimaLeitura)) return;
        ultimaLeitura = agora;

        if (!RegrasAnalisador.podeLer(bateria.getEnergyStored())) {
            jogador.displayClientMessage(Component.translatable("odteste.analisador.sem_energia")
                    .withStyle(ChatFormatting.GRAY), true);
            nivel.playSound(null, worldPosition, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4f, 0.6f);
            return;
        }

        Optional<Sinal> doItem = Sinais.doItem(item);
        List<Sinal> sinais = doItem.isPresent()
                ? List.of(doItem.get())
                : Sinais.perto(nivel, worldPosition, RegrasAnalisador.RAIO_SINAL);

        if (sinais.isEmpty()) {
            jogador.displayClientMessage(Component.translatable("odteste.analisador.estatica")
                    .withStyle(ChatFormatting.GRAY), true);
            nivel.playSound(null, worldPosition, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 0.5f, 0.5f);
            return;
        }

        bateria.gastar(RegrasAnalisador.CUSTO_LEITURA);
        nivel.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 0.7f, 0.5f);
        jogador.sendSystemMessage(Component.translatable("odteste.analisador.cabecalho")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        int mostrados = 0;
        for (Sinal s : sinais) {
            if (mostrados++ >= RegrasAnalisador.MAX_SINAIS) break;
            jogador.sendSystemMessage(Component.literal("  ").append(s.decifrado().copy()
                    .withStyle(ChatFormatting.WHITE)));
            s.camada().ifPresent(camada -> {
                jogador.sendSystemMessage(Component.literal("  ").append(
                        Component.translatable("odteste.analisador.camada")
                                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
                jogador.sendSystemMessage(Component.literal("  ").append(camada.copy()
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)));
            });
        }
    }

    // ---------- salvar ----------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registros) {
        super.saveAdditional(tag, registros);
        tag.putInt(CHAVE_ENERGIA, bateria.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registros) {
        super.loadAdditional(tag, registros);
        bateria.definir(tag.getInt(CHAVE_ENERGIA));
    }
}
