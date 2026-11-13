package com.nikolas.odteste.cliente;

import com.nikolas.odteste.criaturas.Criaturas;
import com.nikolas.odteste.criaturas.Retalhado;
import com.nikolas.odteste.criaturas.SanguessugaMae;
import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.itens.Itens;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Cliente do conteúdo de Sangue: desenho das criaturas, cor provisória dos itens e o sangramento sem ícone
 * nem tempo na tela (regra: nenhum número; quem sangra vê as gotas).
 */
final class ConteudoCliente {
    /** Cores provisórias (os itens usam texturas vanilla tingidas até o Modelador fazer as nossas). */
    private static final int COR_RUBRA = 0xFFB02020;
    private static final int COR_AGULHA = 0xFF9E1010;
    private static final int COR_DEDAL = 0xFF5A3A3A;
    private static final int COR_SINO = 0xFF9A6A3A;
    private static final int COR_MATERIA = 0xFF6A0A0A;

    private ConteudoCliente() {}

    static void iniciar(IEventBus barramento) {
        barramento.addListener((EntityRenderersEvent.RegisterRenderers e) -> {
            // Modelos 5.2 (aprovados em 13/11/2026): geo, animação e textura em assets/odteste/{geo,animations,textures}/entity/.
            // O Retalhado tem glowmask (retalhado_glowmask.png): só o que está pintado nela brilha no escuro.
            // Textura A ou B pelo UUID (ModeloCriatura). A morte é a animação "morrer": sem o tombo de lado do vanilla.
            e.registerEntityRenderer(Criaturas.RETALHADO.get(), ctx -> {
                GeoEntityRenderer<Retalhado> r = new GeoEntityRenderer<>(ctx, new ModeloCriatura<Retalhado>("retalhado")) {
                    @Override
                    protected float getDeathMaxRotation(Retalhado criatura) {
                        return 0f;
                    }
                };
                r.addRenderLayer(new AutoGlowingGeoLayer<>(r));
                return r;
            });
            e.registerEntityRenderer(Criaturas.SANGUESSUGA.get(), ctx -> new GeoEntityRenderer<SanguessugaMae>(ctx,
                    new ModeloCriatura<SanguessugaMae>("sanguessuga_mae")) {
                @Override
                protected float getDeathMaxRotation(SanguessugaMae criatura) {
                    return 0f;
                }
            });
        });

        barramento.addListener((RegisterColorHandlersEvent.Item e) -> {
            e.register((item, camada) -> camada == 0 ? COR_RUBRA : -1, Itens.RUBRA.get());
            e.register((item, camada) -> camada == 0 ? COR_AGULHA : -1, Itens.AGULHA.get());
            e.register((item, camada) -> camada == 0 ? COR_DEDAL : -1, Itens.DEDAL.get());
            e.register((item, camada) -> camada == 0 ? COR_SINO : -1, Itens.SINO.get());
            e.register((item, camada) -> camada == 0 ? COR_MATERIA : -1, Itens.MATERIA_SANGUE.get());
        });

        barramento.addListener((RegisterClientExtensionsEvent e) -> e.registerMobEffect(new IClientMobEffectExtensions() {
            @Override
            public boolean isVisibleInInventory(MobEffectInstance instancia) {
                return false;
            }

            @Override
            public boolean isVisibleInGui(MobEffectInstance instancia) {
                return false;
            }
        }, Sangramento.EFEITO.get()));
    }
}
