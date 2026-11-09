package com.nikolas.odteste.compat;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

/**
 * Liga as integrações com os mods do pack. Regra de ouro: nenhuma classe de outro mod é tocada
 * fora de um pacote compat/<mod>, e esse pacote só é carregado quando o mod existe.
 * Assim, se um mod sair do pack, o nosso continua abrindo sem crash.
 */
public final class Compat {
    public enum Integracao {
        IRONS("irons_spellbooks"),
        OCCULTISM("occultism"),
        MALUM("malum"),
        VAMPIRISM("vampirism"),
        BOTANIA("botania"),
        MEKANISM("mekanism"),
        CREATE("create"),
        CURIOS("curios"),
        /** Animação do corpo do jogador. Só cliente: usado em cliente/ via compat/playeranimator/. */
        PLAYER_ANIMATOR("playeranimator");

        public final String modid;

        Integracao(String modid) {
            this.modid = modid;
        }

        public boolean ligada() {
            return ModList.get().isLoaded(modid);
        }
    }

    private Compat() {}

    public static void iniciar(IEventBus barramento) {
        // Cada integração entra aqui quando tiver conteúdo, sempre atrás do if (a classe compat só é lida se o mod existir).
        if (Integracao.OCCULTISM.ligada()) com.nikolas.odteste.compat.occultism.OccultismCompat.iniciar(barramento);
    }

    public static List<String> ligadas() {
        List<String> lista = new ArrayList<>();
        for (Integracao i : Integracao.values()) {
            if (i.ligada()) lista.add(i.modid);
        }
        return lista;
    }
}
