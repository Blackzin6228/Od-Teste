package com.nikolas.odteste.criaturas;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * "O Que Segue" (nome provisório): silhueta alta e preta que segue um jogador. Não anda: **salta** alguns blocos
 * quando ninguém olha, sempre pelo caminho (não atravessa parede, não quebra bloco). Olhada, congela; encarada,
 * some. Se chega perto sem ser vista, pega. Não pode ser ferida: golpe faz ela sumir.
 *
 * Nunca é salva no mundo (some ao descarregar o chunk ou reiniciar). Quem a chama e cuida da recarga é {@link Segue}.
 * Pensa a cada 10 ticks; o caminho só é calculado na hora de saltar.
 */
public final class OQueSegue extends PathfinderMob implements GeoEntity {
    private static final RawAnimation PARADO = RawAnimation.begin().thenLoop("animation.o_que_segue.parado");
    private static final int PENSA_A_CADA = 10;
    /** Sem caminho por tanto tempo (ticks), desiste e volta mais tarde. */
    private static final int ESPERA_MAX = 20 * 60 * 3;
    /** Longe demais do alvo (ele correu, voou, teleportou): some. */
    private static final double LONGE_DEMAIS = 80;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private UUID alvo;
    private int olhada;
    private int semCaminho;
    private long proximoSalto;

    public OQueSegue(EntityType<? extends PathfinderMob> tipo, Level level) {
        super(tipo, level);
        this.xpReward = 0;
        setSilent(true);
    }

    public static AttributeSupplier.Builder atributos() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 64)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    void definirAlvo(UUID jogador, long agora) {
        this.alvo = jogador;
        this.proximoSalto = agora + 40;
    }

    @Nullable
    public UUID alvo() {
        return alvo;
    }

    // ---------- cabeça ----------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (alvo == null || (tickCount + getId()) % PENSA_A_CADA != 0) return;
        if (!(level() instanceof ServerLevel nivel)) return;
        ServerPlayer p = nivel.getServer().getPlayerList().getPlayer(alvo);
        if (p == null || !p.isAlive() || p.isSpectator() || p.level() != nivel || nivel.dimension() != Level.OVERWORLD
                || distanceToSqr(p) > LONGE_DEMAIS * LONGE_DEMAIS) {
            sumir(p, RegrasSegue.Motivo.PERDEU_ALVO);
            return;
        }
        if (p.isSleeping()) {
            sumir(p, RegrasSegue.Motivo.DORMIU);
            return;
        }
        // Sempre virada para ele.
        Vec3 d = p.position().subtract(position());
        float giro = (float) (Math.atan2(d.z, d.x) * (180 / Math.PI)) - 90f;
        setYRot(giro);
        setYHeadRot(giro);
        setYBodyRot(giro);

        double distancia = Math.sqrt(distanceToSqr(p));
        double olhar = Segue.olhar(p, position(), getBbHeight());
        boolean olhando = olhar >= RegrasSegue.DOT_OLHAR_DIRETO;
        olhada = olhando ? olhada + PENSA_A_CADA : 0;
        if (RegrasSegue.sumirOlhada(olhada, olhando, distancia)) {
            Segue.foiVista(p);
            sumir(p, RegrasSegue.Motivo.VISTA);
            return;
        }
        // No campo de visão: congela.
        if (olhar >= RegrasSegue.DOT_CAMPO) return;
        if (distancia <= RegrasSegue.DISTANCIA_PEGAR) {
            pegar(p);
            return;
        }
        if (nivel.getGameTime() >= proximoSalto) saltar(nivel, p);
    }

    /** Avança alguns blocos pelo caminho até o jogador, para um ponto que ele não vê. Sem caminho, espera. */
    private void saltar(ServerLevel nivel, ServerPlayer p) {
        double fator = Segue.fatorMaquinas(p);
        long agora = nivel.getGameTime();
        Path caminho = getNavigation().createPath(p.blockPosition(), 1);
        if (caminho == null || !caminho.canReach() || caminho.getNodeCount() < 2) {
            semCaminho += 40;
            proximoSalto = agora + 40;
            if (semCaminho >= ESPERA_MAX) sumir(p, RegrasSegue.Motivo.SEM_CAMINHO);
            return;
        }
        semCaminho = 0;
        proximoSalto = agora + RegrasSegue.intervaloSalto(random.nextDouble(), fator);
        double passo = RegrasSegue.passo(random.nextDouble(), fator);
        // O nó mais distante dentro do passo; se ele estiver à vista, tenta os anteriores.
        int escolhido = -1;
        for (int i = 1; i < caminho.getNodeCount(); i++) {
            Node n = caminho.getNode(i);
            if (position().distanceTo(new Vec3(n.x + 0.5, n.y, n.z + 0.5)) > passo) break;
            escolhido = i;
        }
        for (int i = escolhido; i >= 1; i--) {
            Node n = caminho.getNode(i);
            Vec3 ponto = new Vec3(n.x + 0.5, n.y, n.z + 0.5);
            if (Segue.visivel(p, ponto, getBbHeight())) continue;
            teleportTo(ponto.x, ponto.y, ponto.z);
            getNavigation().stop();
            Segue.passos(p, ponto);
            if (Math.sqrt(distanceToSqr(p)) <= RegrasSegue.DISTANCIA_PEGAR) pegar(p);
            return;
        }
        // Todo ponto à frente está à vista: espera.
    }

    private void pegar(ServerPlayer p) {
        Segue.pegar(p, this);
        sumir(p, RegrasSegue.Motivo.PEGOU);
    }

    /** Some (sem morrer, sem drop) e avisa o {@link Segue} para marcar a recarga. */
    void sumir(@Nullable ServerPlayer p, RegrasSegue.Motivo motivo) {
        if (isRemoved()) return;
        if (alvo != null) Segue.sumiu(level(), alvo, p, motivo);
        discard();
    }

    // ---------- não pode ser ferida ----------

    @Override
    public boolean hurt(DamageSource fonte, float quanto) {
        if (level().isClientSide) return false;
        // Golpe de alguém (ou /kill): some. Fogo, queda, cacto e afins: nada.
        if (fonte.getEntity() != null || fonte.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ServerPlayer p = alvo == null ? null : ((ServerLevel) level()).getServer().getPlayerList().getPlayer(alvo);
            sumir(p, RegrasSegue.Motivo.ATACADA);
        }
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance efeito) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity outro) {
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distancia) {
        return false;
    }

    @Override
    public void checkDespawn() {
    }

    // ---------- GeckoLib ----------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controles) {
        controles.add(new AnimationController<>(this, "corpo", 0, estado -> estado.setAndContinue(PARADO)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
