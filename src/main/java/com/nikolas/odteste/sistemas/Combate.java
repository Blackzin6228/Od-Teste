package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.criaturas.CriaturaDeSangue;
import com.nikolas.odteste.itens.PecaDeTraje;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Balanceamento de combate (RegrasCombate), num lugar só, no começo do dano (antes da armadura):
 * 1) golpe de arma paranormal nunca passa de 20;
 * 2) criatura nossa: dano paranormal (tag, criatura nossa ou golpe de arma paranormal) x2; magia (tag
 *    {@code odteste:magia}) entra inteira; comum perde a resistência;
 * 3) jogador com traje A4/A5 completo segura parte do dano paranormal e do dano comum.
 * Nunca mexe em {@code /kill}, vazio e afins (tag {@code minecraft:bypasses_invulnerability}).
 * Sem lag: é só um evento de dano, com contas e duas checagens de tag.
 */
public final class Combate {
    private static final EquipmentSlot[] TRAJE = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private Combate() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(Combate::aoReceberDano);
    }

    private static void aoReceberDano(LivingIncomingDamageEvent e) {
        LivingEntity alvo = e.getEntity();
        if (alvo.level().isClientSide) return;
        DamageSource fonte = e.getSource();
        if (fonte.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;

        float dano = e.getAmount();
        boolean arma = Paranormal.golpeDeArma(fonte);
        if (arma) dano = RegrasCombate.golpeDeArma(dano);

        if (alvo instanceof CriaturaDeSangue criatura) {
            dano = RegrasCombate.contraCriatura(dano, arma || Paranormal.dano(fonte), fonte.is(Paranormal.MAGIA),
                    criatura.raridade());
        } else if (alvo instanceof Player p) {
            int grau = grauDoTraje(p);
            if (grau >= 4) {
                dano = RegrasCombate.depoisDoTraje(dano, RegrasCombate.absorcaoTraje(grau, Paranormal.dano(fonte)));
            }
        }
        if (dano != e.getAmount()) e.setAmount(dano);
    }

    /** Degrau do traje completo: o menor das 4 peças; 0 se faltar uma ou não for traje nosso. */
    public static int grauDoTraje(LivingEntity quem) {
        int grau = Integer.MAX_VALUE;
        for (EquipmentSlot slot : TRAJE) {
            ItemStack peca = quem.getItemBySlot(slot);
            if (!(peca.getItem() instanceof PecaDeTraje t)) return 0;
            grau = Math.min(grau, t.grau());
        }
        return grau;
    }
}
