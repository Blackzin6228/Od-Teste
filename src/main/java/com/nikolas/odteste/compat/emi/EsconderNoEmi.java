package com.nikolas.odteste.compat.emi;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.itens.Itens;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * Esconde no EMI os itens sem receita e as receitas do nosso mod (inclusive as que o EMI lê do JEI, como o ritual
 * do Occultism). Regra 6 de docs/progressao.md. O EMI só acha esta classe se ele estiver no pack. Só cliente.
 */
@EmiEntrypoint
public final class EsconderNoEmi implements EmiPlugin {
    @Override
    public void register(EmiRegistry registro) {
        if (!Compat.Integracao.EMI.ligada()) return;
        List<EmiStack> itens = Itens.escondidos().stream().map(EmiStack::of).toList();
        registro.removeEmiStacks(s -> itens.stream().anyMatch(i -> i.isEqual(s)));
        registro.removeRecipes(r -> {
            ResourceLocation id = r.getId();
            return id != null && (id.getNamespace().equals(OdMod.MODID) || id.getPath().contains(OdMod.MODID + "/"));
        });
    }
}
