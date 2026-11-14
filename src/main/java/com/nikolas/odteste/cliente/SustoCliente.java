package com.nikolas.odteste.cliente;

import com.nikolas.odteste.horror.TipoSusto;
import com.nikolas.odteste.rede.Susto;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Os sustos grandes (decisão de 14/11/2026: raros e fortes). O servidor decide quando ({@code horror/RegrasSusto}:
 * 1 ou 2 por noite, regulado pela Exposição) e o mestre pode disparar ({@code /odteste susto <tipo> <jogador>}).
 * Aqui só se toca: primeiro a tensão (silêncio súbito, um som pequeno), depois o momento forte.
 *
 * <ul>
 * <li>Só sons vanilla, grave (o jogo prende o tom em 0,5). Nada de rosto na tela, nada que faça rir.</li>
 * <li>Silêncio: durante o susto, sons de ambiente, chuva e bichos param (monstros continuam: o susto nunca esconde
 * um creeper).</li>
 * <li>{@code efeitos_fortes} diminui piscada, escuro e tremor; não tira o susto.</li>
 * <li>Um por vez. Entra por cima das alucinações (que somem) e nunca no meio de uma cena.</li>
 * </ul>
 */
final class SustoCliente {
    private static TipoSusto tipo;
    private static int t;
    private static boolean silencio;
    /** Nosso próprio som passando pelo filtro do silêncio. */
    private static boolean liberado;
    // estado de cada tipo
    private static float yawPassos;
    private static int virouEm = -1;
    private static int fimPassos;
    private static Vec3 porta;
    private static int lado = 1;

    private SustoCliente() {}

    static void iniciar() {
        Susto.definirReceptor(m -> comecar(TipoSusto.de(m.tipo())));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> aoTick());
    }

    static boolean tocando() {
        return tipo != null;
    }

    static void limpar() {
        tipo = null;
        silencio = false;
        liberado = false;
        CamadaHorrorCliente.apagar = 0f;
    }

    private static void comecar(TipoSusto novo) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (novo == null || p == null || mc.level == null || tipo != null || CenaCliente.tocando()) return;
        // O grande entra por cima: alucinação e Pendurado na tela somem.
        if (Alucinacoes.ocupado()) Alucinacoes.limpar();
        if (Pendurado.presente()) Pendurado.limpar();
        tipo = novo;
        t = 0;
        virouEm = -1;
        fimPassos = -1;
        porta = null;
        RandomSource r = p.getRandom();
        lado = r.nextBoolean() ? 1 : -1;
        yawPassos = p.getYRot();
        if (novo == TipoSusto.PORTA) porta = acharPorta(p, mc.level);
        // Silêncio súbito: o que estava tocando de ambiente para agora.
        silencio = true;
        mc.getSoundManager().stop(null, SoundSource.AMBIENT);
        mc.getSoundManager().stop(null, SoundSource.WEATHER);
        mc.getSoundManager().stop(null, SoundSource.NEUTRAL);
    }

    /** Filtro do silêncio (chamado pelo CamadaHorrorCliente em cada som que vai tocar). */
    static void filtrar(PlaySoundEvent e) {
        if (!silencio || liberado || e.getSound() == null) return;
        SoundSource s = e.getSound().getSource();
        if (s == SoundSource.AMBIENT || s == SoundSource.WEATHER || s == SoundSource.NEUTRAL) e.setSound(null);
    }

    /** Força dos efeitos de tela (1, 0,5 ou 0,25): diminui, nunca tira. */
    private static float forca() {
        return Math.max(0.25f, ConfigCliente.tremor());
    }

    /** Piscada e escuro: no mínimo 40% da força cheia. */
    private static float piscada(float k) {
        return k * (0.4f + 0.6f * forca());
    }

    private static void aoTick() {
        if (tipo == null) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || CenaCliente.tocando()) {
            limpar();
            return;
        }
        if (mc.isPaused()) return;
        boolean segue = switch (tipo) {
            case PASSOS -> passos(p);
            case PORTA -> porta(p);
            case LUZ -> luz(p);
            case SUSSURRO -> sussurro(p);
            case SILENCIO -> silencio(p);
        };
        t++;
        if (!segue) limpar();
    }

    // ---------- os cinco ----------

    /** Passos atrás que acompanham. Virou para ver: param. Um instante depois, algo pesado bem nas suas costas. */
    private static boolean passos(LocalPlayer p) {
        if (t == 30) Tela.vinheta(40, 200, 40, 0.25f);
        if (t >= 40 && virouEm < 0 && fimPassos < 0) {
            if (Math.abs(Mth.wrapDegrees(p.getYRot() - yawPassos)) > 100f) {
                virouEm = t; // virou: os passos param na hora
            } else if (t >= 40 + 240) {
                fimPassos = t; // nunca virou: os passos param sozinhos, colados nele
            } else if ((t - 40) % 9 == 0) {
                double perto = 3.0 - Math.min(1.6, (t - 40) / 150.0);
                som(Sons.atras(p, perto).add(0, -1.2, 0), (t / 9) % 2 == 0 ? SoundEvents.GRAVEL_STEP : SoundEvents.STONE_STEP,
                        0.45f, 0.55f);
                yawPassos = p.getYRot(); // acompanha o seu passo: conta a virada desde o último som
            }
        }
        int parou = virouEm >= 0 ? virouEm : fimPassos;
        if (parou < 0) return true;
        if (t == parou + 30) golpeNasCostas(p);
        if (t > parou + 30 && t <= parou + 30 + 48 && (t - parou - 30) % 16 == 0) coracao(p, 0.6f);
        return t < parou + 100;
    }

    /** Uma porta perto range devagar... e bate com força, várias vezes. Depois, um sussurro de um lado só. */
    private static boolean porta(LocalPlayer p) {
        Vec3 onde = porta != null ? porta : Sons.atras(p, 6);
        if (t == 40) som(onde, SoundEvents.WOODEN_DOOR_OPEN, 0.45f, 0.5f);
        if (t == 100) {
            som(onde, SoundEvents.WOODEN_DOOR_CLOSE, 1.0f, 0.5f);
            som(onde, SoundEvents.IRON_DOOR_CLOSE, 0.8f, 0.5f);
            Tela.tremer(6, 1.0f);
            Tela.escurecer(1, 2, 10, piscada(0.35f));
        }
        if (t == 112) som(onde, SoundEvents.WOODEN_DOOR_CLOSE, 0.9f, 0.5f);
        if (t == 120) som(onde, SoundEvents.WOODEN_DOOR_CLOSE, 0.8f, 0.55f);
        if (t == 126) som(onde, SoundEvents.WOODEN_DOOR_CLOSE, 0.7f, 0.5f);
        if (t == 190) som(aoLado(p, 1.0), SoundEvents.AMBIENT_CAVE, 0.55f, 0.5f);
        return t < 240;
    }

    /** A luz falha: tocha e vela parecem apagar (só na sua tela). No escuro, algo respira perto. */
    private static boolean luz(LocalPlayer p) {
        float k = forca();
        float a = 0f;
        if (t == 30) som(p.getEyePosition(), SoundEvents.FIRE_EXTINGUISH, 0.35f, 0.6f);
        if (t >= 30 && t < 70) {
            // Falha quatro vezes, como chama no vento (piscada diminui com efeitos_fortes).
            int fase = (t - 30) % 10;
            a = fase < 3 ? 0.35f + 0.35f * k : 0f;
        } else if (t >= 70 && t < 76) {
            a = (t - 70) / 6f;
        } else if (t >= 76 && t < 150) {
            a = 1f;
        } else if (t >= 150 && t < 190) {
            a = 1f - (t - 150) / 40f;
        }
        if (t >= 70) a *= 0.6f + 0.35f * k; // mínimo: ainda fica bem escuro, nunca preto total
        CamadaHorrorCliente.apagar = Math.max(0f, Math.min(0.95f, a));
        if (t == 70) som(p.getEyePosition(), SoundEvents.FIRE_EXTINGUISH, 0.6f, 0.5f);
        if (t == 95) som(aoLado(p, 1.4), SoundEvents.AMBIENT_CAVE, 0.6f, 0.5f);
        if (t >= 110 && t <= 142 && (t - 110) % 16 == 0) coracao(p, 0.65f);
        if (t == 125) Tela.vinheta(6, 40, 30, piscada(0.6f));
        return t < 200;
    }

    /** Um sussurro de um lado só, colado no ouvido. Depois, o seu nome no chat, só para você. */
    private static boolean sussurro(LocalPlayer p) {
        if (t == 40) som(aoLado(p, 1.0), SoundEvents.AMBIENT_CAVE, 0.6f, 0.5f);
        if (t == 47) som(aoLado(p, 1.2), SoundEvents.AMBIENT_CAVE, 0.3f, 0.5f);
        if (t == 100) som(aoLado(p, 0.8), SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 0.6f, 0.5f);
        if (t == 130) {
            p.displayClientMessage(Component.translatable("odteste.horror.sussurro.nome", p.getName())
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC), false);
            Tela.vinheta(4, 30, 40, piscada(0.55f));
            Tela.tremer(8, 0.6f);
        }
        if (t >= 134 && t <= 166 && (t - 134) % 16 == 0) coracao(p, 0.55f);
        return t < 200;
    }

    /** Tudo fica mudo de repente. Muito tempo depois, um sino grave. */
    private static boolean silencio(LocalPlayer p) {
        if (t == 0) Tela.vinheta(40, 120, 40, 0.35f);
        if (t == 160) {
            Vec3 longe = Sons.atras(p, 18).add(0, 6, 0);
            som(longe, SoundEvents.BELL_BLOCK, 0.6f, 0.5f);
            som(longe, SoundEvents.BELL_RESONATE, 0.8f, 0.5f);
            Tela.escurecer(2, 4, 20, piscada(0.45f));
        }
        return t < 230;
    }

    // ---------- pedaços ----------

    /** O momento forte dos passos: um som pesado logo atrás, a tela fecha e treme. */
    private static void golpeNasCostas(LocalPlayer p) {
        Vec3 atras = Sons.atras(p, 1.3);
        som(atras, SoundEvents.WARDEN_NEARBY_CLOSE, 0.9f, 0.5f);
        som(atras, SoundEvents.WOOD_BREAK, 0.6f, 0.5f);
        Tela.escurecer(2, 8, 30, piscada(0.75f));
        Tela.tremer(12, 1.5f);
    }

    private static void coracao(LocalPlayer p, float volume) {
        som(p.getEyePosition(), SoundEvents.WARDEN_HEARTBEAT, volume, 0.5f);
    }

    /** Colado num ouvido só (o lado é sorteado no começo do susto). */
    private static Vec3 aoLado(LocalPlayer p, double distancia) {
        Vec3 olhar = p.getLookAngle();
        Vec3 plano = new Vec3(olhar.x, 0, olhar.z);
        if (plano.lengthSqr() < 1e-4) plano = new Vec3(0, 0, 1);
        plano = plano.normalize();
        Vec3 lateral = new Vec3(-plano.z, 0, plano.x).scale(lado * distancia);
        return p.getEyePosition().add(lateral);
    }

    private static void som(Vec3 onde, SoundEvent s, float volume, float tom) {
        liberado = true;
        try {
            Sons.em(onde, s, volume, tom);
        } finally {
            liberado = false;
        }
    }

    private static void som(Vec3 onde, Holder<SoundEvent> s, float volume, float tom) {
        som(onde, s.value(), volume, tom);
    }

    /** Porta (metade de baixo) a até 10 blocos; procura uma vez, no começo do susto (~3 mil blocos, uma vez). */
    private static Vec3 acharPorta(LocalPlayer p, ClientLevel nivel) {
        BlockPos centro = p.blockPosition();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        Vec3 melhor = null;
        double melhorD = Double.MAX_VALUE;
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    m.set(centro.getX() + dx, centro.getY() + dy, centro.getZ() + dz);
                    BlockState s = nivel.getBlockState(m);
                    if (!(s.getBlock() instanceof DoorBlock) || s.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) continue;
                    double d = dx * dx + dy * dy + dz * dz;
                    if (d < 9 || d >= melhorD) continue; // colada no jogador não assusta: tem que ser "ali"
                    melhorD = d;
                    melhor = Vec3.atCenterOf(m);
                }
            }
        }
        return melhor;
    }
}
