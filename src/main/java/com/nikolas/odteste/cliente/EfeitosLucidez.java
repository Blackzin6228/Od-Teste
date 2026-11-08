package com.nikolas.odteste.cliente;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.sistemas.FaixaLucidez;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * O que a Lucidez faz sentir, por faixa (docs/animacoes.md, seção 2). Tudo só neste cliente: sons, partículas
 * e tela que ninguém mais vê. Nenhum número na tela.
 *
 * Regras: os efeitos de uma faixa só começam 5 s depois de entrar nela (não pisca na borda);
 * um efeito sorteado a cada 20–60 s; nunca dois ao mesmo tempo.
 */
final class EfeitosLucidez {
    /** 5 s na faixa antes de valer. */
    private static final int CONFIRMAR = 100;
    private static final DustParticleOptions SOMBRA = new DustParticleOptions(new Vector3f(0.03f, 0.03f, 0.03f), 1.6f);

    private static FaixaLucidez faixaVista = FaixaLucidez.FIRME;
    private static FaixaLucidez faixaConfirmada = FaixaLucidez.FIRME;
    private static int ticksNaFaixa;
    private static int proximoEfeito = 400;
    /** Efeito em andamento (passos, vulto...). Só um por vez. */
    private static Sequencia ativa;

    /** Um efeito que dura vários ticks. Devolve false quando acabou. */
    private interface Sequencia {
        boolean tick(LocalPlayer p, int passo);
    }

    private static int passoAtivo;

    private EfeitosLucidez() {}

    static void aoTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) {
            faixaVista = faixaConfirmada = FaixaLucidez.FIRME;
            ativa = null;
            Tela.limpar();
            return;
        }
        if (mc.isPaused()) return;
        Tela.aoTick();

        FaixaLucidez f = FaixaLucidez.de(DadosJogador.lucidez(p), DadosJogador.lucidezMax(p));
        if (f == FaixaLucidez.SURTO) f = FaixaLucidez.QUEBRANDO; // o surto tem o momento próprio
        if (f != faixaVista) {
            faixaVista = f;
            ticksNaFaixa = 0;
        } else if (ticksNaFaixa < CONFIRMAR) {
            ticksNaFaixa++;
        }
        if (ticksNaFaixa == CONFIRMAR && f != faixaConfirmada) {
            if (f.gravidade() < faixaConfirmada.gravidade()) melhorou(p);
            faixaConfirmada = f;
            proximoEfeito = 200 + p.getRandom().nextInt(400);
        }

        boolean noEscuro = mc.level.getMaxLocalRawBrightness(BlockPos.containing(p.getEyePosition())) <= 4;
        float respira = (float) (0.5 + 0.5 * Math.sin(Tela.tick * (2 * Math.PI / 120.0))); // um ciclo a cada 6 s
        Tela.vinhetaAlvo = switch (faixaConfirmada) {
            case FIRME, SURTO -> 0f;
            case INQUIETO -> noEscuro ? 0.15f + 0.2f * respira : 0f;
            case ABALADO -> 0.25f + (noEscuro ? 0.15f * respira : 0f);
            case QUEBRANDO -> 0.45f + 0.15f * respira;
        };

        // Mira treme ao puxar o arco (Abalado para baixo).
        boolean arco = p.isUsingItem()
                && (p.getUseItem().getItem() instanceof BowItem || p.getUseItem().getItem() instanceof CrossbowItem);
        Tela.tremorMira = !arco ? 0f : switch (faixaConfirmada) {
            case ABALADO -> 0.25f;
            case QUEBRANDO -> 0.5f;
            default -> 0f;
        };

        if (ativa != null) {
            if (!ativa.tick(p, passoAtivo++)) ativa = null;
            return;
        }
        if (faixaConfirmada.gravidade() < FaixaLucidez.INQUIETO.gravidade()) return;
        if (--proximoEfeito > 0) return;
        proximoEfeito = 400 + p.getRandom().nextInt(800); // 20 a 60 s
        sortear(p, faixaConfirmada);
    }

    /** Voltou para uma faixa melhor: a tela clareia por meio segundo e toca um som de cristal. */
    private static void melhorou(LocalPlayer p) {
        Tela.pulso(0xFFFFFF, 10, 1, 0.2f);
        Sons.em(p, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.5f, 0.7f);
    }

    private static void sortear(LocalPlayer p, FaixaLucidez f) {
        RandomSource r = p.getRandom();
        // 70% um efeito da própria faixa, 30% de uma faixa mais leve.
        FaixaLucidez de = f;
        if (f != FaixaLucidez.INQUIETO && r.nextFloat() < 0.3f) {
            de = FaixaLucidez.values()[1 + r.nextInt(f.gravidade() - 1)];
        }
        switch (de) {
            case INQUIETO -> {
                if (r.nextBoolean()) sussurro(p, r);
                else cisco(p, r);
            }
            case ABALADO -> {
                switch (r.nextInt(3)) {
                    case 0 -> comecar(passos(p, r));
                    case 1 -> comecar(particulasFalsas(r));
                    default -> Tela.inclinar(20, 1.5f);
                }
            }
            default -> {
                switch (r.nextInt(4)) {
                    case 0 -> comecar(vulto(p, r));
                    case 1 -> Tela.escurecer(8, 24, 8, 0.85f);
                    case 2 -> criaturaFalsa(p, r);
                    default -> comecar(coracao());
                }
            }
        }
    }

    private static void comecar(Sequencia s) {
        ativa = s;
        passoAtivo = 0;
    }

    // ---------- Inquieto ----------

    private static void sussurro(LocalPlayer p, RandomSource r) {
        Vec3 pos = Sons.atras(p, 3);
        if (r.nextBoolean()) Sons.em(pos, SoundEvents.AMBIENT_CAVE, 0.5f, 0.6f);
        else Sons.em(pos, SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.6f, 0.6f);
    }

    /** Um cisco de poeira que passa rápido no canto da visão. */
    private static void cisco(LocalPlayer p, RandomSource r) {
        Vec3 olhar = p.getLookAngle();
        Vec3 lado = new Vec3(-olhar.z, 0, olhar.x).normalize().scale(r.nextBoolean() ? 1 : -1);
        Vec3 pos = p.getEyePosition().add(olhar.scale(1.6)).add(lado.scale(1.1));
        Vec3 vel = lado.scale(-0.25);
        for (int i = 0; i < 3; i++) {
            p.level().addParticle(ParticleTypes.SMOKE, pos.x, pos.y + i * 0.05, pos.z, vel.x, 0.0, vel.z);
        }
    }

    // ---------- Abalado ----------

    /** 3 a 5 passos atrás de você, que param quando você vira. */
    private static Sequencia passos(LocalPlayer p, RandomSource r) {
        int total = 3 + r.nextInt(3);
        float yawInicio = p.getYRot();
        boolean cascalho = r.nextBoolean();
        return (jog, passo) -> {
            if (Math.abs(Mth.wrapDegrees(jog.getYRot() - yawInicio)) > 60) return false;
            if (passo % 7 == 0) {
                int n = passo / 7;
                if (n >= total) return false;
                Vec3 pos = Sons.atras(jog, 4.0 - n * 0.4).add(0, -jog.getEyeHeight(), 0);
                Sons.em(pos, cascalho ? SoundEvents.GRAVEL_STEP : SoundEvents.WOOD_STEP, 0.45f, 0.7f);
            }
            return true;
        };
    }

    /** Gotas, faíscas ou cinzas que caem só para você, por 2 s. */
    private static Sequencia particulasFalsas(RandomSource r) {
        ParticleOptions tipo = switch (r.nextInt(3)) {
            case 0 -> ParticleTypes.FALLING_DRIPSTONE_WATER;
            case 1 -> ParticleTypes.ELECTRIC_SPARK;
            default -> ParticleTypes.ASH;
        };
        return (jog, passo) -> {
            if (passo > 40) return false;
            if (passo % 3 == 0) {
                double x = jog.getX() + (jog.getRandom().nextDouble() - 0.5) * 5;
                double z = jog.getZ() + (jog.getRandom().nextDouble() - 0.5) * 5;
                jog.level().addParticle(tipo, x, jog.getY() + 2.5, z, 0, -0.05, 0);
            }
            return true;
        };
    }

    // ---------- Quebrando ----------

    /** Silhueta escura a 8–12 blocos, que se desfaz se você chega a 5 blocos. Dura 3 s. */
    private static Sequencia vulto(LocalPlayer p, RandomSource r) {
        double ang = r.nextDouble() * Math.PI * 2;
        double dist = 8 + r.nextDouble() * 4;
        Vec3 base = new Vec3(p.getX() + Math.cos(ang) * dist, p.getY(), p.getZ() + Math.sin(ang) * dist);
        return (jog, passo) -> {
            if (passo > 60 || jog.position().distanceToSqr(base) < 25) return false;
            if (passo % 2 == 0) {
                RandomSource rr = jog.getRandom();
                for (int i = 0; i < 5; i++) {
                    double y = rr.nextDouble() * 1.9;
                    double largura = y > 1.5 ? 0.18 : (y > 0.8 ? 0.3 : 0.15); // cabeça, ombros, pernas
                    jog.level().addParticle(SOMBRA, base.x + (rr.nextDouble() - 0.5) * 2 * largura,
                            base.y + y, base.z + (rr.nextDouble() - 0.5) * 2 * largura, 0, 0, 0);
                }
            }
            return true;
        };
    }

    private static void criaturaFalsa(LocalPlayer p, RandomSource r) {
        double ang = r.nextDouble() * Math.PI * 2;
        Vec3 pos = p.position().add(Math.cos(ang) * 6, 1, Math.sin(ang) * 6);
        if (r.nextBoolean()) Sons.em(pos, SoundEvents.ENDERMAN_STARE, 0.5f, 0.5f);
        else Sons.em(pos, SoundEvents.ZOMBIE_AMBIENT, 0.6f, 0.5f);
    }

    /** O próprio coração, três batidas. */
    private static Sequencia coracao() {
        return (jog, passo) -> {
            if (passo > 40) return false;
            if (passo % 18 == 0) Sons.em(jog, SoundEvents.WARDEN_HEARTBEAT, 0.6f, 0.7f);
            return true;
        };
    }
}
