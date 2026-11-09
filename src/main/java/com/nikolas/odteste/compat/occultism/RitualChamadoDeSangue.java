package com.nikolas.odteste.compat.occultism;

import com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity;
import com.klikli_dev.occultism.common.ritual.SummonRitual;
import com.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Abalo;
import com.nikolas.odteste.sistemas.Exposicao;
import com.nikolas.odteste.sistemas.Fonte;
import com.nikolas.odteste.sistemas.Lucidez;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Chamado de Sangue: ritual de giz do Occultism que chama um Retalhado (hostil) para o círculo.
 * A invocação é a do Occultism ({@link SummonRitual}); o que é nosso é o contato com o paranormal:
 * quem fez o ritual ganha Exposição (fonte "ritual", Sangue) e a Lucidez balança.
 */
public final class RitualChamadoDeSangue extends SummonRitual {
    public RitualChamadoDeSangue(RitualRecipe receita) {
        super(receita, false);
    }

    @Override
    public void finish(Level level, BlockPos tigela, GoldenSacrificialBowlBlockEntity bloco,
                       @Nullable ServerPlayer jogador, ItemStack ativacao) {
        super.finish(level, tigela, bloco, jogador, ativacao);
        level.playSound(null, tigela, SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0f, 0.5f);
        level.playSound(null, tigela, SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 0.8f, 0.5f);
        if (jogador != null) {
            Exposicao.contato(jogador, Fonte.RITUAL, Elemento.SANGUE, 1f);
            Lucidez.abalar(jogador, Abalo.APARICAO, 0.5f);
        }
    }
}
