package com.nikolas.odteste.compat.jei;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.itens.Itens;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Esconde no JEI os itens sem receita e as receitas do nosso mod que mostram outro mod (o ritual do Occultism).
 * Regra 6 de docs/progressao.md: quem descobre é o jogador. O JEI só acha esta classe se ele estiver no pack.
 * Só cliente (o JEI só roda no cliente).
 */
@JeiPlugin
public final class EsconderNoJei implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.PLUGIN_JEI);

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jei) {
        if (!Compat.Integracao.JEI.ligada()) return;
        List<ItemStack> itens = Itens.escondidos().stream().map(ItemStack::new).toList();
        jei.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, itens);

        // Receitas nossas em qualquer categoria (ex.: rituais do Occultism), sem ler classe do Occultism.
        IRecipeManager receitas = jei.getRecipeManager();
        receitas.createRecipeCategoryLookup().includeHidden().get()
                .forEach(categoria -> esconderNossas(receitas, categoria.getRecipeType()));
    }

    private static <T> void esconderNossas(IRecipeManager receitas, RecipeType<T> tipo) {
        List<T> nossas = receitas.createRecipeLookup(tipo).get()
                .filter(r -> r instanceof RecipeHolder<?> h && h.id().getNamespace().equals(OdMod.MODID))
                .toList();
        if (!nossas.isEmpty()) receitas.hideRecipes(tipo, nossas);
    }
}
