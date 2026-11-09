package com.nikolas.odteste.itens;

import com.nikolas.odteste.efeitos.RegrasSangramento;
import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Exposicao;
import com.nikolas.odteste.sistemas.Fonte;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;

/**
 * Rubra, a espada de Sangue (lore/reliquias-e-espadas.md). Sem receita: só comando ou evento.
 * - Com a vida do dono acima de 70%, é uma espada comum (de diamante, que não quebra).
 * - Abaixo de 70%, cada golpe põe um acúmulo de sangramento. Com o acúmulo cheio, o golpe abre a ferida:
 *   dano em área (3 blocos) e sangramento em quem estiver perto. Outros jogadores e bichos domados ficam de fora
 *   da área (só o alvo do golpe é atingido, se for jogador).
 * Estágios (Adormecida/Desperta/Plena) ainda não: esperam o ok do Nikolas.
 */
public final class Rubra extends SwordItem {
    public Rubra() {
        super(Tiers.DIAMOND, new Item.Properties()
                // Sem número na tela: os atributos valem, mas o tooltip não mostra dano nem velocidade.
                .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 3, -2.4f).withTooltip(false))
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
    }

    @Override
    public boolean hurtEnemy(ItemStack item, LivingEntity alvo, LivingEntity dono) {
        if (dono.level() instanceof ServerLevel nivel && RegrasSangramento.rubraForte(dono.getHealth(), dono.getMaxHealth())) {
            int atual = Sangramento.nivel(alvo);
            if (RegrasSangramento.abreFerida(atual)) {
                abrirFerida(nivel, alvo, dono);
            } else {
                Sangramento.aplicar(alvo, RegrasSangramento.proximoAcumulo(atual), RegrasSangramento.DURACAO_RUBRA, dono);
                nivel.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(), SoundEvents.EVOKER_FANGS_ATTACK,
                        SoundSource.PLAYERS, 0.35f, 0.7f);
            }
        }
        return super.hurtEnemy(item, alvo, dono);
    }

    private static void abrirFerida(ServerLevel nivel, LivingEntity alvo, LivingEntity dono) {
        alvo.removeEffect(Sangramento.EFEITO);
        double r = RegrasSangramento.FERIDA_RAIO;
        List<LivingEntity> perto = nivel.getEntitiesOfClass(LivingEntity.class, alvo.getBoundingBox().inflate(r),
                e -> e != dono && e.isAlive() && (e == alvo
                        || (!(e instanceof Player) && !(e instanceof TamableAnimal t && t.isTame()))));
        for (LivingEntity e : perto) {
            // O alvo acabou de levar o golpe e está no tempo de imunidade: sem zerar, a ferida não pegaria nele.
            if (e == alvo) e.invulnerableTime = 0;
            e.hurt(Sangramento.fonte(nivel, dono), RegrasSangramento.FERIDA_DANO);
            Sangramento.aplicar(e, 1, RegrasSangramento.DURACAO_RUBRA, dono);
        }
        nivel.sendParticles(Sangramento.GOTA, alvo.getX(), alvo.getY() + alvo.getBbHeight() * 0.5, alvo.getZ(),
                30, r * 0.4, 0.4, r * 0.4, 0.0);
        // A mandíbula estala.
        nivel.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(), SoundEvents.EVOKER_FANGS_ATTACK,
                SoundSource.PLAYERS, 1.0f, 0.5f);
        nivel.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(), SoundEvents.WARDEN_HEARTBEAT,
                SoundSource.PLAYERS, 0.8f, 0.5f);
        if (dono instanceof ServerPlayer p) {
            Exposicao.contato(p, Fonte.RELIQUIA_USAR, Elemento.SANGUE);
            p.displayClientMessage(Component.translatable("odteste.rubra.ferida")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack item, Item.TooltipContext contexto, List<Component> linhas, TooltipFlag flag) {
        linhas.add(Component.translatable("item.odteste.rubra.objeto").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        linhas.add(Component.translatable("item.odteste.rubra.poder").withStyle(ChatFormatting.DARK_RED));
        linhas.add(Component.translatable("item.odteste.rubra.custo").withStyle(ChatFormatting.DARK_GRAY));
    }
}
