package com.nikolas.odteste.cliente;

import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.compat.playeranimator.AnimacaoJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import com.nikolas.odteste.rede.Momento;
import com.nikolas.odteste.sistemas.Degrau;
import com.nikolas.odteste.sistemas.TipoMomento;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

/**
 * Toca os momentos que o servidor avisou (docs/animacoes.md): subir de degrau, Marca aparecer/trocar/subir, surto.
 * Quem é o dono do momento sente tudo (tela + som + partícula + corpo); quem está perto vê o corpo e as partículas
 * e ouve um eco. Partículas com número fixo e pequeno: nada cresce sem limite.
 */
final class MomentosCliente {
    private MomentosCliente() {}

    static void receber(Momento m) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        TipoMomento tipo = TipoMomento.deNumero(m.tipo());
        Entity ent = mc.level.getEntity(m.jogador());
        if (tipo == null || !(ent instanceof AbstractClientPlayer alvo)) return;
        boolean eu = alvo == mc.player;
        Elemento elemento = Elemento.deIdOuNenhum(m.elemento());

        switch (tipo) {
            case DEGRAU -> degrau(alvo, eu, m.valor(), elemento);
            case MARCA_APARECEU -> {
                marca(alvo, eu, elemento, 50);
                animar(alvo, IdsDados.ANIM_MARCA_APARECE, m.emCombate());
                if (eu) barra(mc, "odteste.momento.marca." + elemento.id());
            }
            case MARCA_TROCOU -> {
                Elemento antigo = Elemento.deIdOuNenhum(m.elementoAntigo());
                if (eu) Tela.pulso(cor(antigo), 20, 1, 0.6f);
                particulas(alvo, antigo, 12, -0.04);
                assinatura(alvo, antigo, eu ? 0.6f : 0.25f, 0.5f);
                marca(alvo, eu, elemento, 30);
                animar(alvo, IdsDados.ANIM_MARCA_SOBE, m.emCombate());
            }
            case MARCA_SUBIU -> {
                marca(alvo, eu, elemento, 40);
                if (eu && m.valor() >= 3) Tela.tremer(10, 0.8f);
                if (m.valor() >= 3) Sons.em(alvo, SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, eu ? 0.6f : 0.3f, 0.5f);
                if (m.valor() >= 4) Sons.em(alvo, SoundEvents.END_PORTAL_FRAME_FILL, eu ? 0.7f : 0.3f, 0.5f);
                animar(alvo, IdsDados.ANIM_MARCA_SOBE, m.emCombate());
            }
            case SURTO -> surto(alvo, eu, elemento, m.emCombate());
        }
    }

    // ---------- Exposição: pausa, golpe, eco ----------

    private static void degrau(AbstractClientPlayer alvo, boolean eu, int valor, Elemento marca) {
        Degrau[] todos = Degrau.values();
        Degrau d = valor >= 0 && valor < todos.length ? todos[valor] : Degrau.COMUM;
        RandomSource r = alvo.getRandom();
        switch (d) {
            case TOCADO -> {
                if (!eu) return; // só você
                Tela.vinheta(10, 0, 10, 0.6f);
                Sons.em(alvo, SoundEvents.AMBIENT_CAVE, 0.5f, 0.6f);
                var olhar = alvo.getLookAngle();
                for (int i = 0; i < 6; i++) {
                    alvo.level().addParticle(ParticleTypes.WHITE_ASH,
                            alvo.getX() + olhar.x * 1.2 + (r.nextDouble() - 0.5) * 0.8,
                            alvo.getEyeY() + (r.nextDouble() - 0.5) * 0.6,
                            alvo.getZ() + olhar.z * 1.2 + (r.nextDouble() - 0.5) * 0.8, 0, 0, 0);
                }
            }
            case MARCADO -> {
                if (eu) {
                    Tela.vinheta(8, 4, 12, 0.7f);
                    Tela.tremer(10, 0.6f);
                    barra(Minecraft.getInstance(), "odteste.momento.degrau.marcado");
                }
                Sons.em(alvo, SoundEvents.WARDEN_HEARTBEAT, eu ? 0.8f : 0.3f, 0.6f);
                anel(alvo, ParticleTypes.ASH, 24);
            }
            case FUNDO -> {
                if (eu) Tela.vinheta(10, 30, 20, 0.8f);
                Sons.em(alvo, SoundEvents.RESPAWN_ANCHOR_DEPLETE, eu ? 0.8f : 0.3f, 0.5f);
                if (eu) Sons.em(alvo, SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 0.7f, 0.6f);
                anel(alvo, ParticleTypes.LARGE_SMOKE, 20);
            }
            case A_BEIRA -> {
                if (eu) {
                    Tela.vinheta(6, 10, 20, 0.95f);
                    Tela.tremer(10, 1.2f);
                }
                Sons.em(alvo, SoundEvents.ELDER_GUARDIAN_CURSE, eu ? 0.6f : 0.25f, 0.5f);
                Sons.em(alvo, SoundEvents.BEACON_DEACTIVATE, eu ? 0.7f : 0.3f, 0.5f);
                for (int i = 0; i < 24; i++) {
                    alvo.level().addParticle(ParticleTypes.SQUID_INK, alvo.getX() + (r.nextDouble() - 0.5) * 0.6,
                            alvo.getY() + i * 0.17, alvo.getZ() + (r.nextDouble() - 0.5) * 0.6, 0, 0.02, 0);
                }
            }
            case ATRAVESSADO -> {
                if (eu) {
                    Tela.escurecer(5, 20, 20, 1f);
                    Tela.pulso(0x8B0000, 200, 10, 0.5f); // borda pulsando como um coração por 10 s
                }
                Sons.em(alvo, SoundEvents.END_PORTAL_SPAWN, eu ? 0.7f : 0.4f, 0.5f);
                if (eu) Sons.em(alvo, SoundEvents.WARDEN_HEARTBEAT, 0.8f, 0.5f);
                particulas(alvo, marca, 40, 0.02);
            }
            default -> {}
        }
    }

    // ---------- Marca ----------

    /** Borda da cor, partículas do elemento subindo pelo corpo, som do elemento. */
    private static void marca(AbstractClientPlayer alvo, boolean eu, Elemento e, int particulas) {
        if (eu) Tela.pulso(cor(e), 40, e == Elemento.SANGUE ? 3 : 1, 0.8f);
        particulas(alvo, e, particulas, 0.03);
        assinatura(alvo, e, eu ? 0.8f : 0.3f, 0.6f);
        Sons.em(alvo, SoundEvents.RESPAWN_ANCHOR_CHARGE, eu ? 0.6f : 0.25f, 0.6f);
    }

    /** O som de cada elemento (tabela "Cada elemento" do docs/animacoes.md). */
    private static void assinatura(AbstractClientPlayer alvo, Elemento e, float volume, float pitch) {
        switch (e) {
            case SANGUE -> {
                Sons.em(alvo, SoundEvents.WARDEN_HEARTBEAT, volume, pitch);
                Sons.em(alvo, SoundEvents.HONEY_BLOCK_SLIDE, volume * 0.8f, 0.5f);
            }
            case CONHECIMENTO -> {
                Sons.em(alvo, SoundEvents.ENCHANTMENT_TABLE_USE, volume, pitch);
                Sons.em(alvo, SoundEvents.BOOK_PAGE_TURN, volume * 0.8f, 0.5f);
            }
            case MORTE -> {
                Sons.em(alvo, SoundEvents.BELL_RESONATE, volume, 0.5f);
                Sons.em(alvo, SoundEvents.WITHER_AMBIENT, volume * 0.3f, 0.5f);
            }
            case ENERGIA -> {
                Sons.em(alvo, SoundEvents.LIGHTNING_BOLT_THUNDER, volume * 0.15f, 0.5f);
                Sons.em(alvo, SoundEvents.RESPAWN_ANCHOR_CHARGE, volume * 0.7f, 0.7f);
            }
            default -> {}
        }
    }

    // ---------- Surto ----------

    private static void surto(AbstractClientPlayer alvo, boolean eu, Elemento marca, boolean combate) {
        RandomSource r = alvo.getRandom();
        if (eu) {
            // Preto de fora para dentro em 1 s, 4 s de escuro, abre devagar em 5 s.
            Tela.vinheta(20, 80, 40, 1f);
            Tela.escurecer(20, 80, 100, 0.95f);
            Sons.em(alvo, SoundEvents.BEACON_AMBIENT, 1f, 0.5f);
            Sons.em(alvo, SoundEvents.WARDEN_HEARTBEAT, 0.8f, 0.5f);
            if (marca != Elemento.NENHUM) {
                // No escuro, pontos de luz da cor da sua Marca.
                DustParticleOptions ponto = new DustParticleOptions(cor3(marca), 0.6f);
                var olhar = alvo.getLookAngle();
                for (int i = 0; i < 10; i++) {
                    alvo.level().addParticle(ponto, alvo.getX() + olhar.x * 2 + (r.nextDouble() - 0.5) * 2,
                            alvo.getEyeY() + (r.nextDouble() - 0.5) * 1.5,
                            alvo.getZ() + olhar.z * 2 + (r.nextDouble() - 0.5) * 2, 0, 0, 0);
                }
            }
        } else {
            Sons.em(alvo, SoundEvents.PLAYER_HURT, 0.6f, 0.6f);
            for (int i = 0; i < 16; i++) {
                alvo.level().addParticle(ParticleTypes.SMOKE, alvo.getX(), alvo.getY() + 1.2, alvo.getZ(),
                        (r.nextDouble() - 0.5) * 0.15, 0.02, (r.nextDouble() - 0.5) * 0.15);
            }
        }
        animar(alvo, IdsDados.ANIM_SURTO, combate);
    }

    // ---------- pedaços ----------

    private static void barra(Minecraft mc, String chave) {
        mc.gui.setOverlayMessage(Component.translatable(chave), false);
    }

    /** Corpo (Player Animator). Sem o mod, ou em combate, o momento fica só com tela, partícula e som. */
    private static void animar(AbstractClientPlayer alvo, String id, boolean emCombate) {
        if (emCombate || !Compat.Integracao.PLAYER_ANIMATOR.ligada()) return;
        AnimacaoJogador.tocar(alvo, id);
    }

    private static int cor(Elemento e) {
        // A Morte é quase preta: na borda, um cinza para aparecer.
        return e == Elemento.MORTE ? 0x404040 : e.cor();
    }

    private static Vector3f cor3(Elemento e) {
        int c = cor(e);
        return new Vector3f(((c >> 16) & 0xFF) / 255f, ((c >> 8) & 0xFF) / 255f, (c & 0xFF) / 255f);
    }

    /** Partículas do elemento em volta do corpo, subindo (vy > 0) ou caindo como pó (vy < 0). */
    private static void particulas(AbstractClientPlayer alvo, Elemento e, int n, double vy) {
        RandomSource r = alvo.getRandom();
        DustParticleOptions p = new DustParticleOptions(cor3(e), 0.9f);
        for (int i = 0; i < Math.min(n, 60); i++) {
            alvo.level().addParticle(p, alvo.getX() + (r.nextDouble() - 0.5) * 0.8,
                    alvo.getY() + r.nextDouble() * 1.8, alvo.getZ() + (r.nextDouble() - 0.5) * 0.8, 0, vy, 0);
        }
    }

    /** Anel fino em volta dos pés. */
    private static void anel(AbstractClientPlayer alvo, net.minecraft.core.particles.SimpleParticleType tipo, int n) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            alvo.level().addParticle(tipo, alvo.getX() + Math.cos(a) * 1.2, alvo.getY() + 0.1,
                    alvo.getZ() + Math.sin(a) * 1.2, 0, 0.03, 0);
        }
    }
}
