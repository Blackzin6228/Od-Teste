package com.nikolas.odteste.cliente;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.sistemas.Degrau;
import com.nikolas.odteste.sistemas.FaixaLucidez;
import com.nikolas.odteste.sistemas.Paranormal;
import com.nikolas.odteste.sistemas.RegrasHorror;
import com.nikolas.odteste.sistemas.RegrasHorror.Alucinacao;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

/**
 * O que a Lucidez e a Exposição fazem sentir (docs/horror.md). Tudo só neste cliente: sons, partículas, tela,
 * texto e alucinações que ninguém mais vê. Nenhum número na tela. O servidor não manda nada novo:
 * usa a cópia da Lucidez e da Exposição que já chega quando o valor muda.
 *
 * Regras:
 * - a faixa de Lucidez só vale 5 s depois de entrar nela (não pisca na borda);
 * - frequência pela espera mínima da pior faixa (RegrasHorror.esperaMinima), pela metade com outro jogador perto;
 * - uma alucinação por vez, a mesma não repete antes de 2 min;
 * - SUAVE: porta só com som, vulto e criatura somem em 1 s, tela e corpo pela metade;
 * - DESLIGADO: desde 14/11/2026 vale como "Mínimo" (força 0,25, igual ao SUAVE nas alucinações; nada desliga).
 */
final class EfeitosLucidez {
    /** 5 s na faixa antes de valer. */
    private static final int CONFIRMAR = 100;

    private static FaixaLucidez faixaVista = FaixaLucidez.FIRME;
    private static FaixaLucidez faixaConfirmada = FaixaLucidez.FIRME;
    private static int ticksNaFaixa;
    private static Degrau degrau = Degrau.COMUM;
    /** Intensidade atual (0 a 4), lida pela Tela (visão que respira). */
    static int nivel;
    /** Balanço da visão (graus) e tremor da mão (blocos), já suavizados; a Tela multiplica pela força do efeito. */
    static float balanco;
    static float mao;

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
        degrau = Degrau.COMUM;
        ticksNaFaixa = 0;
        nivel = 0;
        balanco = mao = 0;
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
        degrau = Degrau.de(DadosJogador.exposicao(p));
        nivel = RegrasHorror.nivel(faixaConfirmada, degrau);

        boolean noEscuro = mc.level.getMaxLocalRawBrightness(BlockPos.containing(p.getEyePosition())) <= 4;
        float respira = (float) (0.5 + 0.5 * Math.sin(agora * (2 * Math.PI / 120.0))); // um ciclo a cada 6 s
        float vinheta = switch (faixaConfirmada) {
            case FIRME, SURTO -> 0f;
            case INQUIETO -> noEscuro ? 0.15f + 0.2f * respira : 0f;
            case ABALADO -> 0.25f + (noEscuro ? 0.15f * respira : 0f);
            case QUEBRANDO -> 0.45f + 0.15f * respira;
        };
        // Exposição: Média = vinheta bem leve só no escuro; Alta = vinheta que respira; Crítica = mais forte.
        int fe = RegrasHorror.faixa(degrau);
        if (fe == 2 && noEscuro) vinheta = Math.max(vinheta, 0.12f);
        if (fe == 3) vinheta = Math.max(vinheta, 0.12f + 0.1f * respira);
        if (fe >= 4) vinheta = Math.max(vinheta, 0.2f + 0.1f * respira);
        Tela.vinhetaAlvo = vinheta;
        Tela.dessaturarAlvo = RegrasHorror.dessaturar(faixaConfirmada, degrau);

        // Corpo: só nas situações do horror.md (parado, andando, mirando, segurando item paranormal).
        boolean parado = Math.abs(p.getX() - p.xo) + Math.abs(p.getZ() - p.zo) < 0.01;
        boolean usando = p.isUsingItem();
        boolean itemParanormal = p.getMainHandItem().is(Paranormal.ITEM) || p.getOffhandItem().is(Paranormal.ITEM);
        float maoAlvo = RegrasHorror.tremorMao(faixaConfirmada, degrau, itemParanormal, parado, usando);
        float balancoAlvo = RegrasHorror.balanco(faixaConfirmada, degrau, parado);
        mao += (maoAlvo - mao) * 0.1f;          // entra e sai em ~1 s
        balanco += (balancoAlvo - balanco) * 0.03f; // entra e sai em ~2 s

        // A imagem treme ao mirar com arco ou besta (Lucidez Média para baixo).
        boolean arco = usando
                && (p.getUseItem().getItem() instanceof BowItem || p.getUseItem().getItem() instanceof CrossbowItem);
        Tela.tremorMira = !arco ? 0f : switch (faixaConfirmada) {
            case ABALADO -> 0.25f;
            case QUEBRANDO -> 0.5f;
            default -> 0f;
        };

        // Cena tocando: nada de alucinação no meio (a que estava na tela some); depois da cena, espera um pouco.
        if (CenaCliente.tocando() || SustoCliente.tocando()) { // susto grande também: um por vez
            if (Alucinacoes.ocupado()) Alucinacoes.limpar();
            ativa = null;
            AGENDA.adiar(agora);
            return;
        }
        // Trocou para DESLIGADO com algo na tela: some na hora.
        if (ConfigCliente.soVinhetaESom() && Alucinacoes.ocupado()) Alucinacoes.limpar();
        Alucinacoes.aoTick(p, agora);
        if (ativa != null) {
            if (!ativa.tick(p, passoAtivo++)) ativa = null;
        }

        RandomSource r = p.getRandom();
        int espera = RegrasHorror.esperaMinima(faixaConfirmada, degrau);
        if (!AGENDA.deveTocar(agora, espera, () -> grupoPerto(p), r::nextInt)) return;
        if (ativa != null || Alucinacoes.ocupado()) {
            AGENDA.adiar(agora);
            return;
        }
        boolean fortes = !ConfigCliente.soVinhetaESom();
        Optional<Alucinacao> escolhida = RegrasHorror.escolher(faixaConfirmada, degrau, fortes, RECENTES, agora,
                r::nextInt);
        if (escolhida.isEmpty()) {
            AGENDA.adiar(agora);
            return;
        }
        Alucinacao a = escolhida.get();
        RECENTES.marcar(a, agora); // marca mesmo se falhar: não procura porta/chão de novo a cada 5 s
        if (!tocar(p, a, r, agora)) AGENDA.adiar(agora);
    }

    /** Outro jogador a menos de 8 blocos? Só roda na hora de tocar (uma vez por alucinação), nunca por tick. */
    private static boolean grupoPerto(LocalPlayer p) {
        for (AbstractClientPlayer o : p.clientLevel.players()) {
            if (o != p && !o.isSpectator() && o.distanceToSqr(p) < RegrasHorror.GRUPO_RAIO_SQ) return true;
        }
        return false;
    }

    /** Lentidão sutil (−5%) com o vulto perto, na Exposição Crítica. Só no movimento deste cliente; vale até no DESLIGADO. */
    static void aoMover(MovementInputUpdateEvent e) {
        if (!Alucinacoes.vultoPresente() || RegrasHorror.faixa(degrau) < 4) return;
        float k = 1f - RegrasHorror.LENTIDAO_VULTO;
        e.getInput().forwardImpulse *= k;
        e.getInput().leftImpulse *= k;
    }

    /** Toca uma alucinação. False se não deu (sem chão para o vulto, sem porta perto...). */
    private static boolean tocar(LocalPlayer p, Alucinacao a, RandomSource r, long agora) {
        boolean suave = ConfigCliente.suave();
        boolean lucidezBaixa = RegrasHorror.faixa(faixaConfirmada) >= 3;
        switch (a) {
            case SUSSURRO -> comecar(sussurro(r));
            case ESTALO -> estalo(p, r);
            case CISCO -> cisco(p, r);
            case PASSOS -> comecar(passos(p, r));
            case CORACAO -> comecar(coracao(nivel));
            case PARTICULAS -> comecar(particulasFalsas(r));
            case INCLINAR -> Tela.inclinar(20 + r.nextInt(20), 1.5f + nivel * 0.4f);
            case PORTA -> {
                return Alucinacoes.porta(p, agora, suave);
            }
            case NOME -> nome(p, lucidezBaixa);
            case SOM_CRIATURA -> criaturaFalsa(p, r);
            case ESCURECER -> Tela.escurecer(8, 24, 8, 0.85f);
            case VULTO -> {
                if (!Alucinacoes.vulto(p, suave)) return false;
                if (RegrasHorror.faixa(degrau) >= 3) Sons.em(p, SoundEvents.WARDEN_HEARTBEAT, 0.35f, 0.5f);
            }
            case MOB_FALSO -> {
                // Lucidez Baixa: vem até você. Só Exposição Crítica: de costas, longe.
                if (!Alucinacoes.mobFalso(p, lucidezBaixa, suave)) return false;
                Sons.em(p, SoundEvents.WARDEN_HEARTBEAT, 0.35f, 0.5f);
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

    /** Toca {@code som} com eco: de novo 6 e 14 ticks depois, a 50% e 25% do volume, 0,05 mais grave cada vez. */
    private static boolean eco(LocalPlayer jog, int passo, SoundEvent som, float volume, float tom, double longe) {
        int[] t = RegrasHorror.ECO_TICKS;
        for (int i = 0; i < t.length; i++) {
            if (passo == t[i]) {
                Vec3 pos = Sons.atras(jog, longe + i * 1.5);
                Sons.em(pos, som, RegrasHorror.ecoVolume(volume, i), RegrasHorror.ecoPitch(tom, i));
            }
        }
        return passo < t[t.length - 1];
    }

    /** Sussurro grave atrás de você, com eco. Exposição Alta/Crítica e Lucidez Baixa: eco mais longo. */
    private static Sequencia sussurro(RandomSource r) {
        boolean caverna = r.nextBoolean();
        float tom = RegrasHorror.pitchComEco(r::nextInt);
        SoundEvent som = caverna ? SoundEvents.AMBIENT_CAVE.value() : SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.value();
        return (jog, passo) -> eco(jog, passo, som, 0.3f, tom, 3);
    }

    /** Um passo na grama ou um clique de pedra, uma vez: longe (Exposição) ou logo atrás (Lucidez). */
    private static void estalo(LocalPlayer p, RandomSource r) {
        boolean perto = RegrasHorror.faixa(faixaConfirmada) >= 1;
        Vec3 pos;
        if (perto) {
            pos = Sons.atras(p, 2).add(0, -p.getEyeHeight(), 0);
        } else {
            double ang = r.nextDouble() * Math.PI * 2;
            pos = p.position().add(Math.cos(ang) * 12, 0, Math.sin(ang) * 12);
        }
        SoundEvent som = r.nextBoolean() ? SoundEvents.GRASS_STEP : SoundEvents.STONE_STEP;
        Sons.em(pos, som, perto ? 0.45f : 0.35f, pitch(r));
    }

    /** 3 a 5 passos atrás de você, que acompanham o seu ritmo e param quando você para ou vira. */
    private static Sequencia passos(LocalPlayer p, RandomSource r) {
        int total = 3 + r.nextInt(3);
        float yawInicio = p.getYRot();
        boolean cascalho = r.nextBoolean();
        float tom = pitch(r);
        int[] dados = {0}; // quantos já tocaram
        return (jog, passo) -> {
            if (Math.abs(Mth.wrapDegrees(jog.getYRot() - yawInicio)) > 60) return false;
            if (passo > 20 * 20) return false; // ficou parado 20 s: desiste
            boolean andando = Math.abs(jog.getX() - jog.xo) + Math.abs(jog.getZ() - jog.zo) > 0.05;
            if (andando && passo % 7 == 0) {
                if (dados[0] >= total) return false;
                Vec3 pos = Sons.atras(jog, 4.0 - dados[0] * 0.4).add(0, -jog.getEyeHeight(), 0);
                Sons.em(pos, cascalho ? SoundEvents.GRAVEL_STEP : SoundEvents.STONE_STEP, 0.45f, tom);
                dados[0]++;
            }
            return true;
        };
    }

    /** O batimento lento: mais batidas quanto pior. */
    private static Sequencia coracao(int nivelAgora) {
        int batidas = 2 + Math.min(4, nivelAgora);
        int ritmo = nivelAgora >= 4 ? 14 : 18;
        return (jog, passo) -> {
            if (passo > batidas * ritmo) return false;
            if (passo % ritmo == 0) {
                Sons.em(jog, SoundEvents.WARDEN_HEARTBEAT, 0.35f, 0.5f);
                if (nivelAgora >= 3) Tela.vinheta(2, 2, 6, 0.25f); // a borda aperta junto
            }
            return true;
        };
    }

    /** Som de criatura que não existe: o Warden de perto ou um Ghast distante, os dois graves. */
    private static void criaturaFalsa(LocalPlayer p, RandomSource r) {
        double ang = r.nextDouble() * Math.PI * 2;
        if (r.nextBoolean()) {
            Vec3 pos = p.position().add(Math.cos(ang) * 6, 1, Math.sin(ang) * 6);
            Sons.em(pos, SoundEvents.WARDEN_NEARBY_CLOSE, 0.4f, 0.5f);
        } else {
            Vec3 pos = p.position().add(Math.cos(ang) * 14, 3, Math.sin(ang) * 14);
            Sons.em(pos, SoundEvents.GHAST_AMBIENT, 0.3f, 0.5f);
        }
    }

    /** Seu nome sussurrado no chat, sem remetente. Lucidez Baixa: com a sua própria voz. Crítica: segunda linha. */
    private static void nome(LocalPlayer p, boolean propriaVoz) {
        Minecraft mc = Minecraft.getInstance();
        Component quem = p.getName();
        String chave = propriaVoz ? "odteste.horror.sussurro.voz" : "odteste.horror.sussurro.nome";
        mc.gui.getChat().addMessage(Component.translatable(chave, quem)
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        if (!propriaVoz && RegrasHorror.faixa(degrau) >= 4) {
            mc.gui.getChat().addMessage(Component.translatable("odteste.horror.sussurro.nome.2", quem)
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    // ---------- visuais leves ----------

    /** Um cisco de sombra que passa rápido no canto da visão. */
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
