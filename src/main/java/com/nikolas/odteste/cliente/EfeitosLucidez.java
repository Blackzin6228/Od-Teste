package com.nikolas.odteste.cliente;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.sistemas.Degrau;
import com.nikolas.odteste.sistemas.FaixaLucidez;
import com.nikolas.odteste.sistemas.RegrasHorror;
import com.nikolas.odteste.sistemas.RegrasHorror.Alucinacao;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;

/**
 * O que a Lucidez e a Exposição fazem sentir (docs/animacoes.md, seção 2). Tudo só neste cliente: sons,
 * partículas, tela e alucinações que ninguém mais vê. Nenhum número na tela. O servidor não manda nada novo:
 * usa a cópia da Lucidez e da Exposição que já chega quando o valor muda.
 *
 * Regras:
 * - a faixa de Lucidez só vale 5 s depois de entrar nela (não pisca na borda);
 * - o "nível de horror" (RegrasHorror.nivel) junta Lucidez e Exposição e decide frequência e o que pode tocar;
 * - uma alucinação por vez, intervalo sorteado (nunca menos de 20 s), a mesma não repete antes de 2 min;
 * - com "efeitos fortes" DESLIGADO, só vinheta e som.
 */
final class EfeitosLucidez {
    /** 5 s na faixa antes de valer. */
    private static final int CONFIRMAR = 100;

    private static FaixaLucidez faixaVista = FaixaLucidez.FIRME;
    private static FaixaLucidez faixaConfirmada = FaixaLucidez.FIRME;
    private static int ticksNaFaixa;
    /** Nível de horror atual (0 a 4), lido pela Tela e pela mão. */
    static int nivel;

    private static final RegrasHorror.Agenda AGENDA = new RegrasHorror.Agenda();
    private static final RegrasHorror.Recentes RECENTES = new RegrasHorror.Recentes();

    /** Efeito sonoro em andamento (passos, coração, eco do sussurro). Só um por vez. */
    private static Sequencia ativa;
    private static int passoAtivo;

    /** Um efeito que dura vários ticks. Devolve false quando acabou. */
    private interface Sequencia {
        boolean tick(LocalPlayer p, int passo);
    }

    private EfeitosLucidez() {}

    static void limpar() {
        faixaVista = faixaConfirmada = FaixaLucidez.FIRME;
        ticksNaFaixa = 0;
        nivel = 0;
        ativa = null;
        AGENDA.zerar();
        RECENTES.zerar();
        Alucinacoes.limpar();
        Tela.limpar();
    }

    static void aoTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) {
            if (nivel != 0 || ativa != null || Alucinacoes.ocupado()) limpar();
            return;
        }
        if (mc.isPaused()) return;
        Tela.aoTick();
        long agora = Tela.tick;

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
        }
        Degrau degrau = Degrau.de(DadosJogador.exposicao(p));
        nivel = RegrasHorror.nivel(faixaConfirmada, degrau);

        boolean noEscuro = mc.level.getMaxLocalRawBrightness(BlockPos.containing(p.getEyePosition())) <= 4;
        float respira = (float) (0.5 + 0.5 * Math.sin(agora * (2 * Math.PI / 120.0))); // um ciclo a cada 6 s
        float vinheta = switch (faixaConfirmada) {
            case FIRME, SURTO -> 0f;
            case INQUIETO -> noEscuro ? 0.15f + 0.2f * respira : 0f;
            case ABALADO -> 0.25f + (noEscuro ? 0.15f * respira : 0f);
            case QUEBRANDO -> 0.45f + 0.15f * respira;
        };
        // Exposição alta também fecha as bordas, de leve (À beira em diante).
        if (degrau.peloMenos(Degrau.A_BEIRA)) vinheta = Math.max(vinheta, 0.2f + 0.1f * respira);
        Tela.vinhetaAlvo = vinheta;
        Tela.dessaturarAlvo = RegrasHorror.dessaturar(nivel);

        // Mira treme ao puxar o arco (Abalado para baixo).
        boolean arco = p.isUsingItem()
                && (p.getUseItem().getItem() instanceof BowItem || p.getUseItem().getItem() instanceof CrossbowItem);
        Tela.tremorMira = !arco ? 0f : switch (faixaConfirmada) {
            case ABALADO -> 0.25f;
            case QUEBRANDO -> 0.5f;
            default -> 0f;
        };

        Alucinacoes.aoTick(p, agora);
        if (ativa != null) {
            if (!ativa.tick(p, passoAtivo++)) ativa = null;
        }

        RandomSource r = p.getRandom();
        if (!AGENDA.deveTocar(agora, nivel, r::nextInt)) return;
        if (ativa != null || Alucinacoes.ocupado()) {
            AGENDA.adiar(agora);
            return;
        }
        boolean fortes = !ConfigCliente.soVinhetaESom();
        Optional<Alucinacao> escolhida = RegrasHorror.escolher(nivel, fortes, RECENTES, agora, r::nextInt);
        if (escolhida.isEmpty()) {
            AGENDA.adiar(agora);
            return;
        }
        Alucinacao a = escolhida.get();
        RECENTES.marcar(a, agora); // marca mesmo se falhar: não procura porta/chão de novo a cada 5 s
        if (!tocar(p, a, r, agora)) AGENDA.adiar(agora);
    }

    /** Toca uma alucinação. False se não deu (sem chão para o vulto, sem porta perto...). */
    private static boolean tocar(LocalPlayer p, Alucinacao a, RandomSource r, long agora) {
        switch (a) {
            case SUSSURRO -> comecar(sussurro(p, r));
            case CISCO -> cisco(p, r);
            case PASSOS -> comecar(passos(p, r));
            case CORACAO -> comecar(coracao(nivel));
            case PARTICULAS -> comecar(particulasFalsas(r));
            case INCLINAR -> Tela.inclinar(20 + r.nextInt(20), 1.5f + nivel * 0.4f);
            case PORTA -> {
                return Alucinacoes.porta(p, agora);
            }
            case SOM_CRIATURA -> criaturaFalsa(p, r);
            case ESCURECER -> Tela.escurecer(8, 24, 8, 0.85f);
            case VULTO -> {
                return Alucinacoes.vulto(p);
            }
            case MOB_FALSO -> {
                if (!Alucinacoes.mobFalso(p)) return false;
                Sons.em(p, SoundEvents.WARDEN_HEARTBEAT, 0.4f, 0.5f);
            }
        }
        return true;
    }

    /** Voltou para uma faixa melhor: a tela clareia por meio segundo e toca um som de cristal. */
    private static void melhorou(LocalPlayer p) {
        Tela.pulso(0xFFFFFF, 10, 1, 0.2f);
        Sons.em(p, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.5f, 0.7f);
    }

    private static void comecar(Sequencia s) {
        ativa = s;
        passoAtivo = 0;
    }

    private static float pitch(RandomSource r) {
        return RegrasHorror.pitch(r::nextInt);
    }

    // ---------- sons ----------

    /** Sussurro grave atrás de você, com eco: o mesmo som de novo, mais baixo e mais longe, meio segundo depois. */
    private static Sequencia sussurro(LocalPlayer p, RandomSource r) {
        boolean caverna = r.nextBoolean();
        float tom = pitch(r);
        return (jog, passo) -> {
            if (passo > 24) return false;
            if (passo == 0 || passo == 10 || passo == 22) {
                float vol = passo == 0 ? 0.55f : (passo == 10 ? 0.3f : 0.15f);
                Vec3 pos = Sons.atras(jog, 3 + passo * 0.25);
                if (caverna) Sons.em(pos, SoundEvents.AMBIENT_CAVE, vol, tom);
                else Sons.em(pos, SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, vol + 0.05f, tom);
            }
            return true;
        };
    }

    /** 3 a 5 passos atrás de você, que param quando você vira. */
    private static Sequencia passos(LocalPlayer p, RandomSource r) {
        int total = 3 + r.nextInt(3);
        float yawInicio = p.getYRot();
        boolean cascalho = r.nextBoolean();
        float tom = pitch(r);
        return (jog, passo) -> {
            if (Math.abs(Mth.wrapDegrees(jog.getYRot() - yawInicio)) > 60) return false;
            if (passo % 7 == 0) {
                int n = passo / 7;
                if (n >= total) return false;
                Vec3 pos = Sons.atras(jog, 4.0 - n * 0.4).add(0, -jog.getEyeHeight(), 0);
                Sons.em(pos, cascalho ? SoundEvents.GRAVEL_STEP : SoundEvents.WOOD_STEP, 0.45f, tom);
            }
            return true;
        };
    }

    /** O próprio coração: mais batidas e mais rápidas quanto pior o nível. */
    private static Sequencia coracao(int nivelAgora) {
        int batidas = 2 + Math.min(4, nivelAgora);
        int ritmo = nivelAgora >= 4 ? 12 : 16;
        return (jog, passo) -> {
            if (passo > batidas * ritmo) return false;
            if (passo % ritmo == 0) {
                Sons.em(jog, SoundEvents.WARDEN_HEARTBEAT, 0.6f, 0.6f);
                if (nivelAgora >= 3) Tela.vinheta(2, 2, 6, 0.25f); // a borda aperta junto
            }
            return true;
        };
    }

    private static void criaturaFalsa(LocalPlayer p, RandomSource r) {
        double ang = r.nextDouble() * Math.PI * 2;
        Vec3 pos = p.position().add(Math.cos(ang) * 6, 1, Math.sin(ang) * 6);
        if (r.nextBoolean()) Sons.em(pos, SoundEvents.ENDERMAN_STARE, 0.5f, 0.5f);
        else Sons.em(pos, SoundEvents.ZOMBIE_AMBIENT, 0.6f, pitch(r));
    }

    // ---------- visuais leves ----------

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
}
