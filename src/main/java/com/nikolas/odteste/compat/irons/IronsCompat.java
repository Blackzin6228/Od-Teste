package com.nikolas.odteste.compat.irons;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.magias.IdsMagia;
import com.nikolas.odteste.magias.TalhoRubro;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Integração com o Iron's Spells 'n Spellbooks. Só é carregada se o Iron's estiver no pack
 * ({@code Compat.Integracao.IRONS.ligada()}); sem ele, nada daqui existe e as magias saem pela Afinidade.
 *
 * Registra a escola {@code odteste:sangue} (só ela por enquanto; Morte, Conhecimento e Energia depois),
 * os dois atributos dela (poder e resistência) e o adaptador do Talho Rubro. O efeito da magia mora em magias/.
 */
public final class IronsCompat {
    /** Vermelho de Sangue do nosso estilo, mais escuro que o da Blood do Iron's (para não confundir). */
    static final int COR_SANGUE = 0x8E1414;

    private static final DeferredRegister<Attribute> ATRIBUTOS = DeferredRegister.create(Registries.ATTRIBUTE, OdMod.MODID);
    private static final DeferredRegister<SchoolType> ESCOLAS =
            DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, OdMod.MODID);
    private static final DeferredRegister<AbstractSpell> MAGIAS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, OdMod.MODID);

    /** Mesmo formato dos atributos do Iron's: 1 = 100% (sem bônus), sincronizado com o cliente para o tooltip. */
    static final DeferredHolder<Attribute, Attribute> PODER_SANGUE = ATRIBUTOS.register(IdsMagia.PODER_SANGUE,
            () -> new RangedAttribute("attribute.odteste." + IdsMagia.PODER_SANGUE, 1.0, 0.0, 10.0).setSyncable(true));
    static final DeferredHolder<Attribute, Attribute> RESISTENCIA_SANGUE = ATRIBUTOS.register(IdsMagia.RESISTENCIA_SANGUE,
            () -> new RangedAttribute("attribute.odteste." + IdsMagia.RESISTENCIA_SANGUE, 1.0, -10.0, 10.0).setSyncable(true));

    static final ResourceLocation ID_ESCOLA = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsMagia.ESCOLA_SANGUE);

    static final DeferredHolder<SchoolType, SchoolType> ESCOLA_SANGUE = ESCOLAS.register(IdsMagia.ESCOLA_SANGUE,
            () -> new SchoolType(
                    ID_ESCOLA,
                    TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsMagia.FOCO_SANGUE)),
                    Component.translatable("school.odteste." + IdsMagia.ESCOLA_SANGUE).withStyle(Style.EMPTY.withColor(COR_SANGUE)),
                    PODER_SANGUE,
                    RESISTENCIA_SANGUE,
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WARDEN_HEARTBEAT),
                    TalhoRubro.DANO));

    static final DeferredHolder<AbstractSpell, AbstractSpell> TALHO_RUBRO =
            MAGIAS.register(IdsMagia.TALHO_RUBRO, MagiaTalhoRubro::new);

    private IronsCompat() {}

    public static void iniciar(IEventBus barramento) {
        ATRIBUTOS.register(barramento);
        ESCOLAS.register(barramento);
        MAGIAS.register(barramento);
        // Como o Iron's faz com os dele: todo ser vivo tem o atributo (magia de criatura também lê o poder).
        barramento.addListener((EntityAttributeModificationEvent e) -> e.getTypes().forEach(tipo -> {
            e.add(tipo, PODER_SANGUE);
            e.add(tipo, RESISTENCIA_SANGUE);
        }));
    }
}
