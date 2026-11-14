package com.nikolas.odteste.cliente;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.criaturas.CriaturaDeSangue;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.historia.vilacalada.RegrasFxSangue;
import com.nikolas.odteste.rede.EfeitoFx;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;

/**
 * Os quatro efeitos de tela do Sangue, só na Vila Calada (e, antes deles, o {@link FxCorHorror} do mundo inteiro) (decisão de 14/11/2026, camada de horror):
 * <ol>
 * <li><b>carmesim</b>: à noite, céu, lua e névoa puxam para vermelho escuro; cor lavada fora do vermelho.</li>
 * <li><b>sangue</b>: quando uma criatura de Sangue chega perto (ou o mestre manda), sangue escorre das paredes e
 * se espalha pelo chão de verdade; depois seca e some. No máximo um a cada 3 min.</li>
 * <li><b>marcado</b>: quem tem a Marca de Sangue vê veias e pulso nas bordas, crescendo com o estágio.</li>
 * <li><b>coracao</b>: com o Faminto (ou outra criatura de Sangue) perto, a tela bate e escurece junto com o
 * som de batida (vanilla, tom baixo).</li>
 * </ol>
 * {@code efeitos_fortes} diminui (metade no DESLIGADO), não desliga; a distorção e o zoom seguem o tremor.
 * Sem lag: a procura por criaturas é numa caixa de 20 blocos, a cada meio segundo, só dentro da vila.
 * O comando {@code /odfx} (teste e mestre) chega por {@link EfeitoFx} e vale fora da vila também.
 */
final class FxSangue {
    private static final ResourceLocation FAMINTO = ResourceLocation.fromNamespaceAndPath(OdMod.MODID, "faminto");

    /** Relógio dos efeitos (ticks, só anda sem pausa). */
    private static long relogio;

    /** Teste pelo comando: efeito -> tick em que acaba. */
    private static final Map<String, Long> TESTE = new HashMap<>();

    // ---- carmesim
    private static float carmesim;
    // ---- sangue no chão
    private static long sangueInicio = -1;
    private static Vec3 sangueOrigem = Vec3.ZERO;
    private static double sangueChao;
    private static long sangueProximoPermitido;
    private static boolean criaturaPertoAntes;
    private static long proximaGota;
    // ---- marcado
    private static float marcado;
    private static float estagio;
    private static boolean marcadoSom;
    // ---- coração
    private static float perto;
    private static float pertoAlvo;
    private static long batidaInicio = -1000;
    private static float batidaIntervalo = RegrasFxSangue.BATIDA_LONGE;
    private static long testeCoracaoInicio = -1;

    private FxSangue() {}

    static void iniciar(IEventBus barramentoDoMod) {
        // Ordem: o que está no mundo (sangue) antes do ar (névoa carmesim), e o que é do olho (veias, batida) por último.
        // O tom do mundo (cor_horror) vem primeiro: o sangue e as veias por cima ficam vermelhos de verdade.
        FxTela.iniciar(barramentoDoMod, List.of(
                passe(FxCorHorror.SHADER, FxCorHorror::ativo, FxCorHorror::uniforms),
                passe("odfx_sangue", () -> sangueInicio >= 0, (sh, q) -> {
                    float t = segundosDesde(sangueInicio, q.parcial());
                    sh.safeGetUniform("Forca").set(RegrasFxSangue.sangueForca(t) * forca());
                    sh.safeGetUniform("Idade").set(t);
                    sh.safeGetUniform("Seca").set(RegrasFxSangue.sangueSeca(t));
                    Vec3 o = sangueOrigem.subtract(q.camera());
                    sh.safeGetUniform("Origem").set((float) o.x, (float) o.y, (float) o.z);
                    sh.safeGetUniform("Chao").set((float) (sangueChao - q.camera().y));
                    sh.safeGetUniform("Alcance").set(RegrasFxSangue.SANGUE_ALCANCE);
                    lua(sh, q);
                    camera(sh, q);
                }),
                passe("odfx_carmesim", () -> carmesim > 0.002f, (sh, q) -> {
                    sh.safeGetUniform("Forca").set(carmesim * forca());
                    lua(sh, q);
                    camera(sh, q);
                }),
                passe("odfx_marcado", () -> marcado * estagio > 0.002f, (sh, q) -> {
                    sh.safeGetUniform("Forca").set(marcado * forca());
                    sh.safeGetUniform("Estagio").set(estagio);
                    sh.safeGetUniform("Pulso").set(pulsoAgora(q.parcial()));
                    sh.safeGetUniform("Mexer").set(ConfigCliente.tremor());
                }),
                passe("odfx_coracao", () -> perto > 0.002f, (sh, q) -> {
                    sh.safeGetUniform("Forca").set(perto * forca());
                    sh.safeGetUniform("Batida").set(batidaAgora(q.parcial()));
                    sh.safeGetUniform("Mexer").set(ConfigCliente.tremor());
                })));
        EfeitoFx.definirReceptor(FxSangue::receber);
    }

    // ---------------------------------------------------------------- uniforms comuns

    private interface Uniforms {
        void por(ShaderInstance sh, FxTela.Quadro q);
    }

    private interface Ativo {
        boolean agora();
    }

    private static FxTela.Passe passe(String shader, Ativo ativo, Uniforms u) {
        return new FxTela.Passe() {
            @Override
            public String shader() {
                return shader;
            }

            @Override
            public boolean ativo() {
                return ativo.agora();
            }

            @Override
            public void uniforms(ShaderInstance sh, FxTela.Quadro q) {
                sh.safeGetUniform("Tempo").set(segundosDesde(0, q.parcial()) % 3600f);
                u.por(sh, q);
            }
        };
    }

    /** Diminui com efeitos_fortes, nunca some (metade no DESLIGADO). */
    private static float forca() {
        return RegrasFxSangue.forcaVisual(ConfigCliente.tremor());
    }

    private static float segundosDesde(long inicio, float parcial) {
        return (relogio - inicio + parcial) / 20f;
    }

    private static void lua(ShaderInstance sh, FxTela.Quadro q) {
        ClientLevel nivel = Minecraft.getInstance().level;
        float a = nivel == null ? 0f : nivel.getSunAngle(q.parcial());
        // O sol gira no plano x-y (nasce a leste, +x); a lua fica do lado oposto.
        sh.safeGetUniform("LuaDir").set(Mth.sin(a), -Mth.cos(a), 0.05f);
    }

    private static void camera(ShaderInstance sh, FxTela.Quadro q) {
        Vec3 c = q.camera();
        sh.safeGetUniform("Camera").set((float) (c.x % 4096.0), (float) (c.y % 4096.0), (float) (c.z % 4096.0));
    }

    // ---------------------------------------------------------------- cada tick

    static void aoTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ClientLevel nivel = mc.level;
        if (p == null || nivel == null) {
            if (relogio != 0) limpar();
            return;
        }
        if (mc.isPaused()) return;
        relogio++;
        FxCorHorror.aoTick();
        TESTE.entrySet().removeIf(t -> relogio >= t.getValue());
        float vila = ClimaVilaCliente.fatorVila();
        boolean naVila = vila > 0.3f;

        // 1) carmesim: noite × dentro da vila (ou teste)
        float alvoC = vila * ClimaVilaCliente.noiteVila();
        if (testando(RegrasFxSangue.CARMESIM)) alvoC = 1f;
        carmesim = aproximar(carmesim, alvoC, 1f / 60f);

        // procura: criatura de Sangue mais perto (só na vila, a cada meio segundo)
        if (relogio % 10 == 0) {
            Entity criatura = naVila ? maisPerto(nivel, p) : null;
            double d = criatura == null ? 999 : criatura.distanceTo(p);
            pertoAlvo = RegrasFxSangue.perto(d);
            boolean pertoAgora = d <= RegrasFxSangue.SANGUE_DISTANCIA;
            if (pertoAgora && !criaturaPertoAntes && relogio >= sangueProximoPermitido && sangueInicio < 0) {
                comecarSangue(p, criatura.position());
            }
            criaturaPertoAntes = pertoAgora;
        }

        // 2) sangue no chão: acaba sozinho; gotas enquanto escorre
        if (sangueInicio >= 0) {
            float t = (relogio - sangueInicio) / 20f;
            if (t > RegrasFxSangue.sangueTotal()) {
                sangueInicio = -1;
            } else if (t < RegrasFxSangue.SANGUE_SOBE + RegrasFxSangue.SANGUE_VIVE && relogio >= proximaGota) {
                RandomSource r = p.getRandom();
                proximaGota = relogio + 8 + r.nextInt(18);
                Vec3 ponto = p.position().add((r.nextDouble() - 0.5) * 8, 1.5 + r.nextDouble() * 2,
                        (r.nextDouble() - 0.5) * 8);
                Sons.em(ponto, r.nextBoolean() ? SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA
                        : SoundEvents.POINTED_DRIPSTONE_DRIP_WATER, 0.5f, 0.5f + r.nextFloat() * 0.15f);
            }
        }

        // 3) marcado: Marca de Sangue, dentro da vila (ou teste)
        boolean temMarca = DadosJogador.marcaElemento(p) == Elemento.SANGUE;
        float est = temMarca ? RegrasFxSangue.estagio(DadosJogador.marcaEstagio(p)) : 0f;
        float alvoM = temMarca ? vila : 0f;
        if (testando(RegrasFxSangue.MARCADO)) {
            alvoM = 1f;
            if (est <= 0f) est = 1f; // teste sem Marca: mostra o estágio 4
        }
        estagio = aproximar(estagio, est, 1f / 100f);
        marcado = aproximar(marcado, alvoM, 1f / 60f);
        if (marcado > 0.5f && !marcadoSom) {
            Sons.em(p, SoundEvents.WARDEN_HEARTBEAT, 0.25f, 0.5f);
            marcadoSom = true;
        } else if (marcado < 0.1f) {
            marcadoSom = false;
        }

        // 4) coração: perto da criatura (ou do mestre/teste), batida com som
        float alvoP = pertoAlvo;
        if (testeCoracaoInicio >= 0) {
            float t = (relogio - testeCoracaoInicio) / 20f;
            Long fim = TESTE.get(RegrasFxSangue.CORACAO);
            if (fim == null) testeCoracaoInicio = -1;
            else alvoP = Math.max(alvoP, RegrasFxSangue.suave(t / 4f));
        }
        perto = aproximar(perto, alvoP, 1f / 40f);
        if (perto > 0.02f) {
            float desde = (relogio - batidaInicio) / 20f;
            if (desde >= batidaIntervalo) {
                batidaInicio = relogio;
                batidaIntervalo = RegrasFxSangue.intervalo(perto);
                Sons.em(p, SoundEvents.WARDEN_HEARTBEAT, 0.25f + 0.55f * perto, 0.5f);
            }
        }

        FxGravar.aoTick(relogio);
    }

    /** Quanto o carmesim está ligado agora (o cor_horror sai na mesma medida). */
    static float carmesimAgora() {
        return carmesim;
    }

    static boolean testando(String efeito) {
        return TESTE.containsKey(efeito);
    }

    private static float aproximar(float atual, float alvo, float passo) {
        if (atual < alvo) return Math.min(alvo, atual + passo);
        return Math.max(alvo, atual - passo);
    }

    /** Criatura de Sangue (ou o Faminto, quando existir) mais perto, numa caixa de 20 blocos. */
    private static Entity maisPerto(ClientLevel nivel, LocalPlayer p) {
        AABB caixa = p.getBoundingBox().inflate(RegrasFxSangue.SANGUE_DISTANCIA + 2);
        List<Monster> lista = nivel.getEntitiesOfClass(Monster.class, caixa,
                m -> m.isAlive() && (m instanceof CriaturaDeSangue
                        || FAMINTO.equals(BuiltInRegistries.ENTITY_TYPE.getKey(m.getType()))));
        Entity melhor = null;
        double menor = Double.MAX_VALUE;
        for (Monster m : lista) {
            double d = m.distanceToSqr(p);
            if (d < menor) {
                menor = d;
                melhor = m;
            }
        }
        return melhor;
    }

    private static void comecarSangue(LocalPlayer p, Vec3 de) {
        sangueInicio = relogio;
        sangueOrigem = new Vec3(de.x, p.getY(), de.z);
        sangueChao = p.getY();
        sangueProximoPermitido = relogio + RegrasFxSangue.SANGUE_ESPERA_TICKS;
        proximaGota = relogio;
        Sons.em(de, SoundEvents.HONEY_BLOCK_SLIDE, 0.6f, 0.5f);
    }

    /** Sem origem (mestre/teste): a coisa vem de trás, a uns 8 blocos. */
    private static Vec3 atras(LocalPlayer p) {
        return Sons.atras(p, 8);
    }

    // ---------------------------------------------------------------- batidas

    private static float batidaAgora(float parcial) {
        return RegrasFxSangue.batida((relogio - batidaInicio + parcial) / 20f);
    }

    /** O Marcado pulsa no ritmo do coração quando ele está acelerado; senão, no ritmo próprio. */
    private static float pulsoAgora(float parcial) {
        if (perto > 0.05f) return batidaAgora(parcial);
        float t = (relogio + parcial) / 20f;
        return RegrasFxSangue.batida(t % RegrasFxSangue.BATIDA_MARCADO);
    }

    // ---------------------------------------------------------------- comando /odfx

    static void receber(EfeitoFx m) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || !RegrasFxSangue.conhecido(m.efeito())) return;
        int ticks = RegrasFxSangue.segundosTeste(m.segundos()) * 20;
        String e = m.efeito();
        switch (e) {
            case RegrasFxSangue.SANGUE -> {
                sangueInicio = -1;
                comecarSangue(p, atras(p));
                ticks = Math.max(ticks, (int) (RegrasFxSangue.sangueTotal() * 20));
            }
            case RegrasFxSangue.CORACAO -> testeCoracaoInicio = relogio;
            default -> { }
        }
        TESTE.put(e, relogio + ticks);
        if (m.gravar()) {
            FxGravar.comecar(e, relogio, Math.min(ticks, RegrasFxSangue.GRAVAR_MAXIMO));
        }
        if (FxTela.quebrado("odfx_" + e)) {
            p.displayClientMessage(Component.translatable("odteste.fx.quebrado", e), false);
        } else {
            p.displayClientMessage(Component.translatable("odteste.fx.tocando", e, FxTela.caminho()), true);
        }
    }

    static void limpar() {
        relogio = 0;
        TESTE.clear();
        carmesim = marcado = estagio = perto = pertoAlvo = 0f;
        sangueInicio = testeCoracaoInicio = -1;
        sangueProximoPermitido = 0;
        criaturaPertoAntes = marcadoSom = false;
        batidaInicio = -1000;
        FxCorHorror.limpar();
        FxGravar.parar();
        FxTela.limpar();
    }
}
