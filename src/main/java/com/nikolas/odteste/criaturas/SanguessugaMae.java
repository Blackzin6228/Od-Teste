package com.nikolas.odteste.criaturas;

import com.nikolas.odteste.efeitos.Sangramento;
import java.util.EnumSet;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import software.bernie.geckolib.animation.RawAnimation;

/**
 * Sanguessuga-Mãe (lore/criaturas.md): corpo de cesta redonda, quatro pernas de espinho, boca de anéis de dentes,
 * filhotes presos nas costas. Ataque especial: **Chuva de Filhotes** (lança larvas que grudam e drenam vida:
 * sangramento em quem pegam e a mãe se cura). Fraqueza: os filhotes morrem em luz forte.
 * Os filhotes são só partícula e efeito (nenhuma entidade a mais: sem custo de servidor).
 */
public final class SanguessugaMae extends CriaturaDeSangue {
    private static final RawAnimation PARADO = RawAnimation.begin().thenLoop("animation.sanguessuga_mae.parado");
    private static final RawAnimation ANDAR = RawAnimation.begin().thenLoop("animation.sanguessuga_mae.andar");
    private static final RawAnimation LANCAR = RawAnimation.begin().thenPlay("animation.sanguessuga_mae.lancar");
    private static final RawAnimation MORDIDA = RawAnimation.begin().thenPlay("animation.sanguessuga_mae.mordida");
    private static final RawAnimation CHOCAR = RawAnimation.begin().thenPlay("animation.sanguessuga_mae.chocar");
    private static final RawAnimation DANO = RawAnimation.begin().thenPlay("animation.sanguessuga_mae.dano");
    private static final RawAnimation MORRER = RawAnimation.begin().thenPlayAndHold("animation.sanguessuga_mae.morrer");
    /** Durações das animações (sanguessuga_mae.animation.json): morrer 2,8 s, chocar 4,4 s. */
    private static final int TICKS_MORRER = 56;
    private static final int TICKS_CHOCAR = 88;

    public SanguessugaMae(EntityType<? extends Monster> tipo, Level level) {
        super(tipo, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder atributos() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    protected float intensidade() {
        return 1.5f;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ChuvaDeFilhotes(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected int ticksMorrendo() {
        return TICKS_MORRER;
    }

    @Override
    protected String presenca() {
        return "chocar";
    }

    @Override
    protected int ticksPresenca() {
        return TICKS_CHOCAR;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controles) {
        registrarControles(controles, PARADO, ANDAR, DANO, MORRER,
                Map.of("lancar", LANCAR, "mordida", MORDIDA, "chocar", CHOCAR));
    }

    /** Golpe comum (corpo a corpo): a mordida. A Chuva de Filhotes não passa por aqui (dano direto). */
    @Override
    public boolean doHurtTarget(Entity alvo) {
        if (!level().isClientSide) triggerAnim(CONTROLE_ACAO, "mordida");
        return super.doHurtTarget(alvo);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource fonte) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState bloco) {
        playSound(SoundEvents.SPIDER_STEP, 0.4f, 0.5f);
    }

    /**
     * Chuva de Filhotes: com o alvo a 3–10 blocos e à vista, a mãe se arma por meio segundo e lança as larvas.
     * Na luz forte (12+) as larvas morrem no ar. Depois, 6 s de espera.
     */
    static final class ChuvaDeFilhotes extends Goal {
        private static final int ESPERA = 20 * 6;
        private static final int ARMAR = 10;
        private final SanguessugaMae dono;
        private int ticks;
        private long proximaEm;

        ChuvaDeFilhotes(SanguessugaMae dono) {
            this.dono = dono;
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity alvo = dono.getTarget();
            if (alvo == null || !alvo.isAlive()) return false;
            if (dono.level().getGameTime() < proximaEm) return false;
            double d2 = dono.distanceToSqr(alvo);
            return d2 >= 9 && d2 <= 100 && dono.hasLineOfSight(alvo);
        }

        @Override
        public boolean canContinueToUse() {
            return ticks <= ARMAR && dono.getTarget() != null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            ticks = 0;
            dono.triggerAnim(CONTROLE_ACAO, "lancar");
            dono.playSound(SoundEvents.SLIME_JUMP, 0.8f, 0.5f);
        }

        @Override
        public void tick() {
            LivingEntity alvo = dono.getTarget();
            if (alvo == null) return;
            dono.getLookControl().setLookAt(alvo, 30f, 30f);
            if (++ticks == ARMAR) lancar(alvo);
        }

        private void lancar(LivingEntity alvo) {
            if (!(dono.level() instanceof ServerLevel nivel)) return;
            // O arco das larvas: 8 pontos de partícula entre a mãe e o alvo (barato).
            Vec3 de = dono.position().add(0, dono.getBbHeight() * 0.8, 0);
            Vec3 ate = alvo.position().add(0, alvo.getBbHeight() * 0.5, 0);
            for (int i = 1; i <= 8; i++) {
                double t = i / 8.0;
                Vec3 p = de.lerp(ate, t).add(0, Math.sin(t * Math.PI) * 1.5, 0);
                nivel.sendParticles(Sangramento.GOTA, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.0);
            }
            BlockPos ondeCai = BlockPos.containing(alvo.getEyePosition());
            if (nivel.getMaxLocalRawBrightness(ondeCai) >= RegrasCriaturas.LUZ_FORTE) {
                // Fraqueza: na luz forte os filhotes morrem antes de grudar.
                nivel.sendParticles(ParticleTypes.SMOKE, ate.x, ate.y, ate.z, 10, 0.3, 0.3, 0.3, 0.01);
                nivel.playSound(null, ate.x, ate.y, ate.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.6f, 0.5f);
                return;
            }
            Sangramento.aplicar(alvo, 0, 120, dono);
            if (alvo.hurt(Sangramento.fonte(nivel, dono), 2f)) dono.heal(3f);
            nivel.playSound(null, ate.x, ate.y, ate.z, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.HOSTILE, 0.8f, 0.5f);
        }

        @Override
        public void stop() {
            proximaEm = dono.level().getGameTime() + ESPERA;
        }
    }
}
