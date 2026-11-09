package com.nikolas.odteste.criaturas;

import com.nikolas.odteste.efeitos.Sangramento;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Retalhado (lore/criaturas.md): bruto curvado, costurado, com um cutelo no antebraço e o coração exposto.
 * 3 blocos de altura. Ataque especial: **Investida de Ganchos** (corre em linha reta, crava os ganchos, arrasta o
 * alvo para perto e deixa sangrando). Cego nas costas: golpe por trás dói mais.
 * Modelo provisório (GeckoLib) até a mesa Modelador fazer o definitivo.
 */
public final class Retalhado extends CriaturaDeSangue {
    private static final RawAnimation PARADO = RawAnimation.begin().thenLoop("animation.retalhado.parado");
    private static final RawAnimation ANDAR = RawAnimation.begin().thenLoop("animation.retalhado.andar");
    private static final RawAnimation INVESTIDA = RawAnimation.begin().thenPlay("animation.retalhado.investida");

    public Retalhado(EntityType<? extends Monster> tipo, Level level) {
        super(tipo, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60)
                .add(Attributes.ATTACK_DAMAGE, 8)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5);
    }

    @Override
    protected float intensidade() {
        return 2f;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new InvestidaDeGanchos(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controles) {
        controles.add(new AnimationController<>(this, "corpo", 5,
                estado -> estado.setAndContinue(estado.isMoving() ? ANDAR : PARADO)));
        controles.add(new AnimationController<>(this, "acao", 2, estado -> PlayState.STOP)
                .triggerableAnim("investida", INVESTIDA));
    }

    /** Cego nas costas: golpe que vem de trás dói mais. */
    @Override
    public boolean hurt(DamageSource fonte, float quanto) {
        if (fonte.getEntity() != null) {
            Vec3 olhar = Vec3.directionFromRotation(0, getYRot());
            Vec3 paraAtacante = fonte.getEntity().position().subtract(position()).multiply(1, 0, 1);
            if (paraAtacante.lengthSqr() > 1e-4) {
                quanto *= RegrasCriaturas.multiplicadorCostas(olhar.dot(paraAtacante.normalize()));
            }
        }
        return super.hurt(fonte, quanto);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState bloco) {
        playSound(SoundEvents.RAVAGER_STEP, 0.5f, 0.6f);
    }

    /**
     * Investida de Ganchos: com o alvo a 5–14 blocos e à vista, corre em linha reta por até ~1,2 s.
     * Se encostar: golpe, sangramento e o alvo é puxado para perto. Depois, 8 s de espera.
     */
    static final class InvestidaDeGanchos extends Goal {
        private static final int ESPERA = 20 * 8;
        private static final int DURACAO = 25;
        private final Retalhado dono;
        private Vec3 direcao = Vec3.ZERO;
        private int ticks;
        private boolean acertou;
        private long proximaEm;

        InvestidaDeGanchos(Retalhado dono) {
            this.dono = dono;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity alvo = dono.getTarget();
            if (alvo == null || !alvo.isAlive() || !dono.onGround()) return false;
            if (dono.level().getGameTime() < proximaEm) return false;
            double d2 = dono.distanceToSqr(alvo);
            return d2 >= 25 && d2 <= 196 && dono.hasLineOfSight(alvo);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity alvo = dono.getTarget();
            return ticks < DURACAO && !acertou && alvo != null && alvo.isAlive() && !dono.horizontalCollision;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            LivingEntity alvo = dono.getTarget();
            ticks = 0;
            acertou = false;
            if (alvo == null) return;
            Vec3 d = alvo.position().subtract(dono.position()).multiply(1, 0, 1);
            direcao = d.lengthSqr() < 1e-4 ? Vec3.ZERO : d.normalize();
            dono.getNavigation().stop();
            dono.getLookControl().setLookAt(alvo, 30f, 30f);
            dono.level().playSound(null, dono.getX(), dono.getY(), dono.getZ(), SoundEvents.RAVAGER_ROAR,
                    SoundSource.HOSTILE, 1.2f, 0.5f);
            dono.triggerAnim("acao", "investida");
        }

        @Override
        public void tick() {
            ticks++;
            LivingEntity alvo = dono.getTarget();
            if (alvo == null) return;
            dono.setDeltaMovement(direcao.x * 0.75, dono.getDeltaMovement().y, direcao.z * 0.75);
            dono.setYRot((float) (Math.atan2(direcao.z, direcao.x) * (180 / Math.PI)) - 90f);
            if (dono.getBoundingBox().inflate(0.5).intersects(alvo.getBoundingBox())) {
                acertou = true;
                if (dono.doHurtTarget(alvo)) {
                    Sangramento.aplicar(alvo, 1, 100, dono);
                    Vec3 puxa = dono.position().subtract(alvo.position()).multiply(1, 0, 1);
                    if (puxa.lengthSqr() > 1e-4) {
                        puxa = puxa.normalize().scale(0.8);
                        alvo.setDeltaMovement(puxa.x, 0.25, puxa.z);
                        alvo.hurtMarked = true;
                    }
                    dono.level().playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(), SoundEvents.CHAIN_BREAK,
                            SoundSource.HOSTILE, 1.0f, 0.5f);
                }
            }
        }

        @Override
        public void stop() {
            proximaEm = dono.level().getGameTime() + ESPERA;
            dono.setDeltaMovement(dono.getDeltaMovement().multiply(0.2, 1, 0.2));
        }
    }
}
