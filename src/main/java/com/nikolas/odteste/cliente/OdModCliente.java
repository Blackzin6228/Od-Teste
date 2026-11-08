package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.compat.playeranimator.AnimacaoJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.rede.Momento;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Entrada do lado do cliente. Com {@code dist = Dist.CLIENT}, o NeoForge nem carrega esta classe
 * num servidor dedicado: nada de cliente/ chega lá.
 */
@Mod(value = OdMod.MODID, dist = Dist.CLIENT)
public final class OdModCliente {
    private static final boolean ANIMADOR = Compat.Integracao.PLAYER_ANIMATOR.ligada();

    public OdModCliente(IEventBus barramento, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ConfigCliente.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        barramento.addListener((RegisterGuiLayersEvent e) -> e.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.CAMADA_TELA), Tela::desenhar));

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
            EfeitosLucidez.aoTick();
            if (ANIMADOR) AnimacaoJogador.aoTick();
        });
        NeoForge.EVENT_BUS.addListener(Tela::aoCalcularCamera);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> Tela.limpar());

        Momento.definirReceptor(MomentosCliente::receber);
        if (ANIMADOR) AnimacaoJogador.iniciar();
        OdMod.LOGGER.info("{}: efeitos do cliente ligados (Player Animator: {}).", OdMod.MODID, ANIMADOR);
    }
}
