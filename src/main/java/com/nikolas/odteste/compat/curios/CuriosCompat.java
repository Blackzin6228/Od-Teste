package com.nikolas.odteste.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Curios: o que o jogador tem equipado nos slots de acessório. Só é chamada atrás de
 * {@code Compat.Integracao.CURIOS.ligada()} (esta classe nem é lida sem o Curios no pack).
 */
public final class CuriosCompat {
    private CuriosCompat() {}

    /** O item está em algum slot do Curios desta criatura/jogador? */
    public static boolean equipado(LivingEntity quem, Item item) {
        return CuriosApi.getCuriosInventory(quem).map(h -> h.isEquipped(item)).orElse(false);
    }
}
