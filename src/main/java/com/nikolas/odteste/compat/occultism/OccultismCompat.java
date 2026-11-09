package com.nikolas.odteste.compat.occultism;

import com.klikli_dev.occultism.common.ritual.RitualFactory;
import com.klikli_dev.occultism.registry.OccultismRituals;
import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Integração com o Occultism (rituais de giz). Só é carregada se o Occultism estiver no pack
 * ({@code Compat.Integracao.OCCULTISM.ligada()}). Sem ele, a classe nem é lida e a receita do ritual
 * (data/odteste/recipe/ritual/chamado_de_sangue.json, com condição mod_loaded) não existe.
 */
public final class OccultismCompat {
    private static final DeferredRegister<RitualFactory> RITUAIS =
            DeferredRegister.create(OccultismRituals.RITUAL_FACTORIES_KEY, OdMod.MODID);

    static {
        RITUAIS.register(IdsDados.RITUAL_CHAMADO, () -> new RitualFactory(RitualChamadoDeSangue::new));
    }

    private OccultismCompat() {}

    public static void iniciar(IEventBus barramento) {
        RITUAIS.register(barramento);
    }
}
