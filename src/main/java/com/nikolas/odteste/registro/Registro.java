package com.nikolas.odteste.registro;

import com.nikolas.odteste.OdMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Todas as listas do mod. Cada ID registrado aqui entra em docs/ids.md:
 * depois que um ID vai para o servidor, ele não pode sumir (os itens do mundo dependem dele).
 */
public final class Registro {
    public static final DeferredRegister.Items ITENS = DeferredRegister.createItems(OdMod.MODID);
    public static final DeferredRegister.Blocks BLOCOS = DeferredRegister.createBlocks(OdMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCOS_COM_DADOS =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OdMod.MODID);
    public static final DeferredRegister<EntityType<?>> CRIATURAS =
            DeferredRegister.create(Registries.ENTITY_TYPE, OdMod.MODID);
    public static final DeferredRegister<CreativeModeTab> ABAS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OdMod.MODID);
    /** Dados presos ao jogador e ao mundo (marcas, elemento, progresso). */
    public static final DeferredRegister<AttachmentType<?>> DADOS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, OdMod.MODID);

    private Registro() {}

    public static void registrar(IEventBus barramento) {
        ITENS.register(barramento);
        BLOCOS.register(barramento);
        BLOCOS_COM_DADOS.register(barramento);
        CRIATURAS.register(barramento);
        ABAS.register(barramento);
        DADOS.register(barramento);
    }
}
