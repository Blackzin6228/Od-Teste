package com.nikolas.odteste.criaturas;

import com.nikolas.odteste.achados.Achados;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.sistemas.Abalo;
import com.nikolas.odteste.sistemas.Exposicao;
import com.nikolas.odteste.sistemas.Fonte;
import com.nikolas.odteste.sistemas.Lucidez;
import com.nikolas.odteste.sistemas.Paranormal;
import com.nikolas.odteste.sistemas.RegrasCombate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * O que toda criatura de Sangue faz com os sistemas (só servidor):
 * - ser vista de perto: Exposição (criatura_vista) e Lucidez (mob_sangue), uma vez por jogador por criatura;
 * - cada troca de golpes: Exposição (criatura_luta); morrer pela mão de um jogador: Exposição (criatura_morta);
 * - matéria de Sangue só cai se quem matou foi um jogador de verdade (não FakePlayer de máquina) e se ela
 *   não veio de spawner.
 * Sem lag: a "vista" olha só o jogador mais perto, a cada 2 s, na própria criatura.
 *
 * Animações (GeckoLib, decisão de 13/11/2026), em quatro controles, do mais fraco ao mais forte (o de baixo manda
 * nos ossos que anima): {@code corpo} (parado/andar), {@code reacao} (dano), {@code acao} (golpe, especial e
 * presença) e {@code morte}. O servidor só dispara a animação no momento do evento (nada por tick).
 */
public abstract class CriaturaDeSangue extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Jogadores que já sentiram esta criatura. Salvo na criatura (sobrevive a reinício); poucos por criatura. */
    private final Set<UUID> sentiram = new LinkedHashSet<>();
    /** Teto da lista salva: mais que isso, os mais antigos saem (nunca cresce sem limite). */
    private static final int MAX_SENTIRAM = 32;
    private boolean semMateria;
    /** Até quando (tickCount) a animação de presença está tocando; só na memória, não é salvo. */
    private int presencaAte;

    static final String CONTROLE_CORPO = "corpo";
    static final String CONTROLE_REACAO = "reacao";
    static final String CONTROLE_ACAO = "acao";
    static final String CONTROLE_MORTE = "morte";
    static final String ANIM_DANO = "dano";

    protected CriaturaDeSangue(EntityType<? extends Monster> tipo, Level level) {
        super(tipo, level);
    }

    /** Peso da criatura nos sistemas (1 = normal). */
    protected abstract float intensidade();

    /** Quanto tempo o corpo fica no chão antes de sumir (ticks): o tamanho da animação {@code morrer}. */
    protected abstract int ticksMorrendo();

    /** Nome da animação de presença (disparável no controle {@code acao}) e quanto ela dura em ticks. */
    protected abstract String presenca();

    protected abstract int ticksPresenca();

    /** Os quatro controles na ordem certa. {@code acoes}: nome → animação disparável no controle {@code acao}. */
    protected final void registrarControles(AnimatableManager.ControllerRegistrar controles,
                                            RawAnimation parado, RawAnimation andar, RawAnimation dano,
                                            RawAnimation morrer, Map<String, RawAnimation> acoes) {
        controles.add(new AnimationController<>(this, CONTROLE_CORPO, 5, estado -> {
            if (isDeadOrDying()) return PlayState.STOP;
            return estado.setAndContinue(estado.isMoving() ? andar : parado);
        }));
        controles.add(new AnimationController<>(this, CONTROLE_REACAO, 1, estado -> PlayState.STOP)
                .triggerableAnim(ANIM_DANO, dano));
        AnimationController<CriaturaDeSangue> acao = new AnimationController<>(this, CONTROLE_ACAO, 2, estado -> PlayState.STOP);
        acoes.forEach(acao::triggerableAnim);
        controles.add(acao);
        // Por último: na morte ela manda em todos os ossos, mesmo se um golpe ainda estava tocando.
        controles.add(new AnimationController<>(this, CONTROLE_MORTE, 0,
                estado -> isDeadOrDying() ? estado.setAndContinue(morrer) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if ((tickCount + getId()) % 40 == 0) sentirJogador();
        // Começou a andar no meio da presença: corta (senão as pernas ficam paradas andando).
        if (presencaAte > tickCount && getNavigation().isInProgress()) pararPresenca();
    }

    /** Presença: às vezes, quando o som ambiente toca e ela está parada e sem alvo. Só no servidor. */
    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
        if (level().isClientSide || !isAlive() || getTarget() != null || getNavigation().isInProgress()) return;
        if (random.nextInt(3) != 0) return;
        presencaAte = tickCount + ticksPresenca();
        triggerAnim(CONTROLE_ACAO, presenca());
    }

    @Override
    public void setTarget(@Nullable LivingEntity alvo) {
        super.setTarget(alvo);
        if (alvo != null && presencaAte > tickCount && !level().isClientSide) pararPresenca();
    }

    private void pararPresenca() {
        presencaAte = 0;
        stopTriggeredAnim(CONTROLE_ACAO, presenca());
    }

    /** Igual ao vanilla, só que o corpo fica o tempo da animação {@code morrer} (vanilla: 1 s). */
    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime >= ticksMorrendo() && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(Entity.RemovalReason.KILLED);
        }
    }

    private void sentirJogador() {
        Player perto = level().getNearestPlayer(this, RegrasCriaturas.RAIO_VISTA);
        if (!(perto instanceof ServerPlayer p) || p instanceof FakePlayer || p.isCreative() || p.isSpectator()) return;
        if (sentiram.contains(p.getUUID()) || !hasLineOfSight(p)) return;
        if (sentiram.size() >= MAX_SENTIRAM) sentiram.remove(sentiram.iterator().next());
        sentiram.add(p.getUUID());
        Exposicao.contato(p, Fonte.CRIATURA_VISTA, Elemento.SANGUE, intensidade());
        Lucidez.abalar(p, Abalo.MOB_SANGUE, intensidade());
        // Só quem viu escuta: um coração grave, longe.
        p.playNotifySound(SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.6f, 0.5f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag lista = new ListTag();
        for (UUID id : sentiram) lista.add(NbtUtils.createUUID(id));
        tag.put(IdsDados.CHAVE_SENTIRAM, lista);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        sentiram.clear();
        ListTag lista = tag.getList(IdsDados.CHAVE_SENTIRAM, Tag.TAG_INT_ARRAY);
        for (int i = 0; i < lista.size() && sentiram.size() < MAX_SENTIRAM; i++) {
            try {
                sentiram.add(NbtUtils.loadUUID(lista.get(i)));
            } catch (IllegalArgumentException ignorado) {
                // Entrada estragada (arquivo editado à mão): ignora.
            }
        }
    }

    @Override
    public boolean hurt(DamageSource fonte, float quanto) {
        boolean doeu = super.hurt(fonte, quanto);
        if (doeu && isAlive() && !level().isClientSide) triggerAnim(CONTROLE_REACAO, ANIM_DANO);
        if (doeu && fonte.getEntity() instanceof ServerPlayer p && !(p instanceof FakePlayer)) {
            Exposicao.contato(p, Fonte.CRIATURA_LUTA, Elemento.SANGUE, intensidade());
        }
        return doeu;
    }

    /**
     * Golpe corpo a corpo igual ao do vanilla ({@code Mob.doHurtTarget}), só que com o tipo de dano
     * {@code odteste:paranormal} (passa pela armadura normal; a MekaSuit e o traje nosso tratam à parte).
     */
    @Override
    public boolean doHurtTarget(Entity alvo) {
        float dano = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        DamageSource fonte = Paranormal.fonte(level(), this);
        if (level() instanceof ServerLevel nivel) {
            dano = EnchantmentHelper.modifyDamage(nivel, getWeaponItem(), alvo, fonte, dano);
        }
        boolean acertou = alvo.hurt(fonte, dano);
        if (acertou) {
            float recuo = getKnockback(alvo, fonte);
            if (recuo > 0 && alvo instanceof LivingEntity vivo) {
                vivo.knockback(recuo * 0.5, Mth.sin(getYRot() * Mth.DEG_TO_RAD), -Mth.cos(getYRot() * Mth.DEG_TO_RAD));
                setDeltaMovement(getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            if (level() instanceof ServerLevel nivel) EnchantmentHelper.doPostAttackEffects(nivel, alvo, fonte);
            setLastHurtMob(alvo);
        }
        if (acertou && alvo instanceof ServerPlayer p) Exposicao.contato(p, Fonte.CRIATURA_LUTA, Elemento.SANGUE, intensidade());
        return acertou;
    }

    /** Quanto resiste a dano comum (RegrasCombate): comum 0%, rara 30%, chefe 60%. */
    public RegrasCombate.Raridade raridade() {
        return RegrasCombate.Raridade.COMUM;
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

    /**
     * Jogador que o loot usa no {@code killed_by_player}: o último que bateu, se o golpe ainda vale (5 s).
     * Pode ser um FakePlayer (máquina). O cansaço do elemento conta para ele, mesmo em morte indireta.
     */
    @Nullable
    public Player jogadorDoDrop() {
        return RegrasCansaco.jogadorDoDrop(lastHurtByPlayer, lastHurtByPlayerTime);
    }

    /** Drop raro de artefato (no máximo 1 por criatura; só jogador de verdade; só onde a matéria também cairia). */
    @Override
    protected void dropCustomDeathLoot(ServerLevel nivel, DamageSource fonte, boolean recente) {
        super.dropCustomDeathLoot(nivel, fonte, recente);
        if (fonte.getEntity() instanceof ServerPlayer p && !(p instanceof FakePlayer)) Achados.sortearDrop(this, p);
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
        // Nasceu sozinha: o jogador Tocado mais perto não atrai outra nesta noite.
        if (motivo == MobSpawnType.NATURAL || motivo == MobSpawnType.CHUNK_GENERATION) {
            Criaturas.gastarNoite(nivel.getLevel(), getX(), getY(), getZ());
        }
        return super.finalizeSpawn(nivel, dificuldade, motivo, dados);
    }

    /** Sons sempre graves (pitch 0,5–0,7). */
    @Override
    public float getVoicePitch() {
        return 0.5f + random.nextFloat() * 0.2f;
    }
}
