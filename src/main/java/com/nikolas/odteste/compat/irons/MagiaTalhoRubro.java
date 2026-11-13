package com.nikolas.odteste.compat.irons;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.magias.IdsMagia;
import com.nikolas.odteste.magias.RegrasTalhoRubro;
import com.nikolas.odteste.magias.TalhoRubro;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Adaptador do Talho Rubro para o Iron's: ele cuida de mana, recarga, pergaminho e grimório;
 * o efeito (alvos, dano, fases, visual) é o mesmo da Afinidade ({@link TalhoRubro}).
 * Sem som nem animação do Iron's: as fases tocam os nossos (cliente/MagiaCliente) e o gancho do Player Animator.
 */
final class MagiaTalhoRubro extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsMagia.TALHO_RUBRO);

    private final DefaultConfig config = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(IronsCompat.ID_ESCOLA)
            .setMaxLevel(RegrasTalhoRubro.NIVEL_MAX)
            .setCooldownSeconds(RegrasTalhoRubro.RECARGA_SEGUNDOS)
            // Básico se aprende por pergaminho no loot das NOSSAS estruturas e criaturas (docs/magias.md seção 5):
            // nada de forja de pergaminho nem do loot aleatório do Iron's.
            .setAllowCrafting(false)
            .setAllowLooting(false)
            .build();

    MagiaTalhoRubro() {
        this.manaCostPerLevel = RegrasTalhoRubro.MANA_POR_NIVEL;
        this.baseSpellPower = (int) RegrasTalhoRubro.DANO_BASE;
        this.spellPowerPerLevel = (int) RegrasTalhoRubro.DANO_POR_NIVEL;
        this.castTime = 0;
        this.baseManaCost = RegrasTalhoRubro.MANA_BASE;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return ID;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return config;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int nivel, LivingEntity quem) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(dano(nivel, quem), 1)));
    }

    private float dano(int nivel, LivingEntity quem) {
        // getSpellPower já soma o nível e multiplica pelo poder geral e pelo da escola odteste:sangue.
        return quem == null ? RegrasTalhoRubro.dano(nivel, 1f) : getSpellPower(nivel, quem);
    }

    @Override
    public void onCast(Level mundo, int nivel, LivingEntity quem, CastSource fonte, MagicData dados) {
        float dano = dano(nivel, quem);
        TalhoRubro.conjurar(quem, dano, (q, alvo, d) -> DamageSources.applyDamage(alvo, d, getDamageSource(q)));
        super.onCast(mundo, nivel, quem, fonte, dados);
    }
}
