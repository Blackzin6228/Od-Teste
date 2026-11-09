package com.nikolas.odteste.itens;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Exposicao;
import com.nikolas.odteste.sistemas.Fonte;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Base dos Artefatos Paranormais: objetos que o jogador encontra e usa, com poder e custo
 * (lore/artefatos-paranormais.md). Sem receita. A recarga fica salva no jogador (sair e voltar não zera);
 * a recarga do Minecraft (o item "apagado" na barra) é só o aviso visual.
 * Nada de número na tela: recusa e uso falam por frase curta, som e partícula.
 */
public abstract class Artefato extends Item {
    private final RegrasArtefato.Raridade raridade;
    private final Elemento elemento;

    protected Artefato(RegrasArtefato.Raridade raridade, Elemento elemento) {
        super(new Item.Properties().stacksTo(1).fireResistant()
                .rarity(raridade == RegrasArtefato.Raridade.RARA ? Rarity.EPIC
                        : raridade == RegrasArtefato.Raridade.INCOMUM ? Rarity.RARE : Rarity.UNCOMMON));
        this.raridade = raridade;
        this.elemento = elemento;
    }

    /**
     * O poder e o custo do artefato. Só servidor.
     * @param dobro true se foi usado de novo em menos de 5 min (Lucidez/Exposição em dobro)
     * @return false se o artefato recusou (nada é cobrado nem entra em recarga)
     */
    protected abstract boolean usar(ServerPlayer p, boolean dobro);

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player jogador, InteractionHand mao) {
        ItemStack item = jogador.getItemInHand(mao);
        if (!(jogador instanceof ServerPlayer p)) return InteractionResultHolder.consume(item);

        String id = BuiltInRegistries.ITEM.getKey(this).getPath();
        long agora = p.server.overworld().getGameTime();
        Map<String, Long> usos = DadosJogador.usosArtefato(p);
        Long ultimo = usos.get(id);
        RegrasArtefato.Bloqueio b = RegrasArtefato.bloqueio(agora, ultimo, RegrasArtefato.ultimoDeTodos(usos),
                raridade, DadosJogador.lucidez(p));
        if (b != RegrasArtefato.Bloqueio.LIVRE) {
            p.displayClientMessage(Component.translatable("odteste.artefato.recusa." + b.name().toLowerCase(java.util.Locale.ROOT))
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), true);
            return InteractionResultHolder.fail(item);
        }
        boolean dobro = RegrasArtefato.custo(1, agora, ultimo) > 1;
        if (!usar(p, dobro)) return InteractionResultHolder.fail(item);

        DadosJogador.definirUsosArtefato(p, RegrasArtefato.registrar(usos, id, agora));
        // Aviso visual: este some pela recarga própria; os outros, pela compartilhada (sem encurtar recarga maior).
        p.getCooldowns().addCooldown(this, raridade.recarga);
        for (Item outro : Itens.artefatos()) {
            if (outro != this && !p.getCooldowns().isOnCooldown(outro)) {
                p.getCooldowns().addCooldown(outro, RegrasArtefato.RECARGA_COMPARTILHADA);
            }
        }
        Exposicao.contato(p, Fonte.RELIQUIA_USAR, elemento);
        return InteractionResultHolder.consume(item);
    }

    @Override
    public void appendHoverText(ItemStack item, Item.TooltipContext contexto, List<Component> linhas, TooltipFlag flag) {
        String base = getDescriptionId();
        linhas.add(Component.translatable(base + ".objeto").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        linhas.add(Component.translatable(base + ".poder").withStyle(ChatFormatting.DARK_RED));
        linhas.add(Component.translatable(base + ".custo").withStyle(ChatFormatting.DARK_GRAY));
    }
}
