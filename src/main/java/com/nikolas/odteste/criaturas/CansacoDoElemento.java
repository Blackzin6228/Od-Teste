package com.nikolas.odteste.criaturas;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.itens.Itens;
import java.util.Iterator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Cansaço do elemento (RegrasCansaco), só servidor: no drop de criatura nossa morta por jogador de verdade,
 * cada unidade de matéria bruta passa pelo sorteio do dia e o que cai soma no contador do jogador.
 * Artefatos e o resto do drop não são tocados. Sem lag: só roda na morte de criatura nossa.
 */
public final class CansacoDoElemento {
    private CansacoDoElemento() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(CansacoDoElemento::aoSoltar);
    }

    private static void aoSoltar(LivingDropsEvent e) {
        if (!(e.getEntity() instanceof CriaturaDeSangue criatura)) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer p) || p instanceof FakePlayer) return;

        long hoje = RegrasCansaco.dia(p.server.overworld().getGameTime());
        DadosJogador.Cansaco salvo = DadosJogador.cansaco(p);
        int antes = RegrasCansaco.brutasHoje(salvo.dia(), salvo.brutas(), hoje);
        int brutas = antes;
        boolean tinhaMateria = false;

        Iterator<ItemEntity> it = e.getDrops().iterator();
        while (it.hasNext()) {
            ItemEntity solto = it.next();
            ItemStack pilha = solto.getItem();
            if (!ehMateriaBruta(pilha)) continue;
            tinhaMateria = true;
            int ficam = 0;
            for (int i = 0; i < pilha.getCount(); i++) {
                if (RegrasCansaco.cai(brutas, criatura.getRandom().nextInt(4))) {
                    ficam++;
                    brutas = Math.min(RegrasCansaco.TETO_CONTADOR, brutas + 1);
                }
            }
            if (ficam == 0) {
                it.remove();
            } else if (ficam != pilha.getCount()) {
                ItemStack menor = pilha.copy();
                menor.setCount(ficam);
                solto.setItem(menor);
            }
        }
        if (!tinhaMateria) return;
        DadosJogador.definirCansaco(p, hoje, brutas);
        int aviso = RegrasCansaco.aviso(antes, brutas);
        if (aviso > 0) {
            p.displayClientMessage(Component.translatable("odteste.cansaco." + aviso)
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
        }
    }

    /** Matéria bruta de algum elemento (hoje só a de Sangue). */
    static boolean ehMateriaBruta(ItemStack pilha) {
        return pilha.is(Itens.MATERIA_SANGUE.get());
    }
}
