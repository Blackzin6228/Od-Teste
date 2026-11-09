package com.nikolas.odteste.criaturas;

import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Abalo;
import com.nikolas.odteste.sistemas.Exposicao;
import com.nikolas.odteste.sistemas.Fonte;
import com.nikolas.odteste.sistemas.Lucidez;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * O que toda criatura de Sangue faz com os sistemas (só servidor):
 * - ser vista de perto: Exposição (criatura_vista) e Lucidez (mob_sangue), uma vez por jogador por criatura;
 * - cada troca de golpes: Exposição (criatura_luta); morrer pela mão de um jogador: Exposição (criatura_morta);
 * - matéria de Sangue só cai se quem matou foi um jogador de verdade (não FakePlayer de máquina) e se ela
 *   não veio de spawner.
 * Sem lag: a "vista" olha só o jogador mais perto, a cada 2 s, na própria criatura.
 */
public abstract class CriaturaDeSangue extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Jogadores que já sentiram esta criatura (só memória; poucos por criatura). */
    private final Set<UUID> sentiram = new HashSet<>();
    private boolean semMateria;

    protected CriaturaDeSangue(EntityType<? extends Monster> tipo, Level level) {
        super(tipo, level);
    }

    /** Peso da criatura nos sistemas (1 = normal). */
    protected abstract float intensidade();

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if ((tickCount + getId()) % 40 == 0) sentirJogador();
    }

    private void sentirJogador() {
        Player perto = level().getNearestPlayer(this, RegrasCriaturas.RAIO_VISTA);
        if (!(perto instanceof ServerPlayer p) || p instanceof FakePlayer || p.isCreative() || p.isSpectator()) return;
        if (sentiram.contains(p.getUUID()) || !hasLineOfSight(p)) return;
        sentiram.add(p.getUUID());
        Exposicao.contato(p, Fonte.CRIATURA_VISTA, Elemento.SANGUE, intensidade());
        Lucidez.abalar(p, Abalo.MOB_SANGUE, intensidade());
        // Só quem viu escuta: um coração grave, longe.
        p.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.6f, 0.5f);
    }

    @Override
    public boolean hurt(DamageSource fonte, float quanto) {
        boolean doeu = super.hurt(fonte, quanto);
        if (doeu && fonte.getEntity() instanceof ServerPlayer p && !(p instanceof FakePlayer)) {
            Exposicao.contato(p, Fonte.CRIATURA_LUTA, Elemento.SANGUE, intensidade());
        }
        return doeu;
    }

    @Override
    public boolean doHurtTarget(Entity alvo) {
        boolean acertou = super.doHurtTarget(alvo);
        if (acertou && alvo instanceof ServerPlayer p) Exposicao.contato(p, Fonte.CRIATURA_LUTA, Elemento.SANGUE, intensidade());
        return acertou;
    }

    @Override
    public void die(DamageSource fonte) {
        if (!level().isClientSide) {
            if (fonte.getEntity() instanceof FakePlayer) {
                semMateria = true;
            } else if (fonte.getEntity() instanceof ServerPlayer p) {
                Exposicao.contato(p, Fonte.CRIATURA_MORTA, Elemento.SANGUE, intensidade());
            }
        }
        super.die(fonte);
    }

    @Override
    protected boolean shouldDropLoot() {
        return super.shouldDropLoot() && !semMateria && !getTags().contains(IdsDados.ETIQUETA_SEM_MATERIA);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor nivel, DifficultyInstance dificuldade, MobSpawnType motivo,
                                        @Nullable SpawnGroupData dados) {
        // Regra anti-farm: nada cai de criatura de spawner.
        if (motivo == MobSpawnType.SPAWNER || motivo == MobSpawnType.TRIAL_SPAWNER) addTag(IdsDados.ETIQUETA_SEM_MATERIA);
        return super.finalizeSpawn(nivel, dificuldade, motivo, dados);
    }

    /** Sons sempre graves (pitch 0,5–0,7). */
    @Override
    public float getVoicePitch() {
        return 0.5f + random.nextFloat() * 0.2f;
    }
}
