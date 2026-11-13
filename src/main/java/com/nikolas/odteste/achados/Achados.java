package com.nikolas.odteste.achados;

import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.itens.Itens;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Os três jeitos de achar um Artefato Paranormal (decisão 2026-11-12): baú marcado das nossas estruturas, drop raro
 * de criatura de Sangue e comando do admin ({@code ComandosArtefato}). Nunca em baú, loja ou receita comum.
 */
public final class Achados {
    private Achados() {}

    private static List<Item> artefatos() {
        return List.of(Itens.AGULHA.get(), Itens.DEDAL.get(), Itens.SINO.get());
    }

    /** Artefato no slot do Curios também conta como "já tem" (só se o Curios estiver no pack). */
    private static boolean equipadoNoCurios(ServerPlayer jogador, Item item) {
        return Compat.Integracao.CURIOS.ligada()
                && com.nikolas.odteste.compat.curios.CuriosCompat.equipado(jogador, item);
    }

    /**
     * Sorteia o drop raro de uma criatura de Sangue derrotada por um jogador de verdade. Chamado uma vez por
     * criatura (então no máximo 1 artefato por criatura). Só servidor.
     */
    public static void sortearDrop(Entity criatura, ServerPlayer jogador) {
        List<Item> todos = artefatos();
        boolean[] tem = new boolean[todos.size()];
        boolean algum = false;
        for (int i = 0; i < tem.length; i++) {
            Item item = todos.get(i);
            tem[i] = jogador.getInventory().contains(s -> s.is(item)) || equipadoNoCurios(jogador, item);
            algum |= tem[i];
        }
        var sorte = criatura.level().getRandom();
        if (!RegrasAchados.sorteou(RegrasAchados.chanceDrop(algum), sorte.nextDouble())) return;
        int escolhido = RegrasAchados.escolher(tem, sorte.nextInt(RegrasAchados.somaPesos(tem)));
        criatura.spawnAtLocation(new ItemStack(todos.get(escolhido)));
    }

    /**
     * Depois de colocar (um pedaço de) estrutura: nos baús/barris que o template marca com
     * {@code odteste_recompensa}, e que ficaram vazios, põe a tabela de loot nossa. Baú que já tem coisa (peça de
     * enigma) não é tocado. Nunca derruba a colocação: erro aqui só vai para o log.
     */
    public static void aplicarRecompensas(ServerLevel nivel, StructureTemplate template, BlockPos ponto,
                                          StructurePlaceSettings ajustes) {
        try {
            for (Block bloco : new Block[] {Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}) {
                for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(ponto, ajustes, bloco)) {
                    CompoundTag nbt = info.nbt();
                    if (nbt == null || !IdsDados.RECOMPENSA_ARTEFATO_SANGUE.equals(nbt.getString(IdsDados.RECOMPENSA_CHAVE))) {
                        continue;
                    }
                    if (!(nivel.getBlockEntity(info.pos()) instanceof RandomizableContainerBlockEntity bau) || !bau.isEmpty()) {
                        continue;
                    }
                    ResourceKey<LootTable> tabela = ResourceKey.create(Registries.LOOT_TABLE,
                            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.LOOT_RECOMPENSA_ARTEFATO_SANGUE));
                    bau.setLootTable(tabela, nivel.getRandom().nextLong());
                }
            }
        } catch (RuntimeException ex) {
            OdMod.LOGGER.error("Recompensa de enigma não foi aplicada em {}", ponto, ex);
        }
    }
}
