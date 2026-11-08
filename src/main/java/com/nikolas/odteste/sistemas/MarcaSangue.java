package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

/**
 * O que a Marca de Sangue faz no corpo (só servidor), chamado pelo Vigia a cada 2 s.
 * Números em RegrasSangue. O modificador de dano é temporário: não fica salvo, é refeito a cada checagem.
 */
public final class MarcaSangue {
    public static final ResourceLocation ID_MODIFICADOR =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.MODIFICADOR_SANGUE);
    /** Vermelho escuro do Sangue (0x8B0000), um pouco mais vivo para aparecer no escuro. */
    private static final DustParticleOptions GOTA = new DustParticleOptions(new Vector3f(0.6f, 0.02f, 0.02f), 0.8f);

    private MarcaSangue() {}

    static void checar(ServerPlayer p) {
        if (DadosJogador.marcaElemento(p) != Elemento.SANGUE) return;
        int estagio = DadosJogador.marcaEstagio(p);

        // Ganha: força quando ferido.
        boolean ferido = RegrasSangue.ferido(p.getHealth(), p.getMaxHealth());
        aplicarBonus(p, RegrasSangue.bonusDano(estagio, ferido));

        // Paga: fome mais rápida nos estágios altos.
        float exaustao = RegrasSangue.exaustaoExtra(estagio);
        if (exaustao > 0 && !p.isCreative()) p.causeFoodExhaustion(exaustao);

        // Visível: poucas gotas vermelhas no corpo, vistas por quem está perto. Invisível não solta nada.
        if (!p.isInvisible() && !p.isSpectator()) {
            int n = RegrasSangue.particulas(estagio);
            p.serverLevel().sendParticles(GOTA, p.getX(), p.getY() + 0.9, p.getZ(), n, 0.25, 0.45, 0.25, 0.0);
        }
    }

    private static void aplicarBonus(ServerPlayer p, double valor) {
        AttributeInstance dano = p.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dano == null) return;
        AttributeModifier atual = dano.getModifier(ID_MODIFICADOR);
        double tinha = atual == null ? 0 : atual.amount();
        if (tinha == valor) return;
        dano.removeModifier(ID_MODIFICADOR);
        if (valor > 0) {
            dano.addTransientModifier(new AttributeModifier(ID_MODIFICADOR, valor, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    /** Tira o efeito (Marca removida ou trocada). */
    static void limpar(ServerPlayer p) {
        AttributeInstance dano = p.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dano != null) dano.removeModifier(ID_MODIFICADOR);
    }
}
