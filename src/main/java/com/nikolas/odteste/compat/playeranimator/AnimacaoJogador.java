package com.nikolas.odteste.compat.playeranimator;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.IdsDados;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Animação do corpo com o Player Animator. **Só cliente** e só chamado de cliente/ atrás de
 * {@code Compat.Integracao.PLAYER_ANIMATOR.ligada()}: sem o mod, esta classe nunca carrega.
 *
 * As animações de hoje são **provisórias** e montadas no código (curtas, poucos ossos), para o play test.
 * A mesa Modelador faz as definitivas no Blockbench, com prévia para o Nikolas; aí elas passam a vir de
 * assets/odteste/player_animation/ e este arquivo só troca de onde lê.
 * Ângulos em radianos; tempo em ticks (20 = 1 s).
 */
public final class AnimacaoJogador {
    private static final ResourceLocation CAMADA =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.CAMADA_ANIMACAO);
    /** Prioridade alta: fica por cima das animações comuns, por pouco tempo. */
    private static final int PRIORIDADE = 1500;

    private static Map<String, KeyframeAnimation> animacoes = Map.of();
    /** Quem está animando agora (para cortar se tomar dano). Fraco: some sozinho quando a entidade some. */
    private static final Set<AbstractClientPlayer> ANIMANDO = Collections.newSetFromMap(new WeakHashMap<>());

    private AnimacaoJogador() {}

    /** Registra a camada e monta as animações. Chamado uma vez, ao abrir o cliente. */
    public static void iniciar() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CAMADA, PRIORIDADE, jogador -> new ModifierLayer<>());
        animacoes = Map.of(
                IdsDados.ANIM_MARCA_APARECE, marcaAparece(),
                IdsDados.ANIM_MARCA_SOBE, marcaSobe(),
                IdsDados.ANIM_SURTO, surto());
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> camada(AbstractClientPlayer p) {
        Object dado = PlayerAnimationAccess.getPlayerAssociatedData(p).get(CAMADA);
        return dado instanceof ModifierLayer<?> m ? (ModifierLayer<IAnimation>) m : null;
    }

    public static void tocar(AbstractClientPlayer p, String id) {
        KeyframeAnimation anim = animacoes.get(id);
        ModifierLayer<IAnimation> c = anim == null ? null : camada(p);
        if (c == null) return;
        c.setAnimation(new KeyframeAnimationPlayer(anim));
        ANIMANDO.add(p);
    }

    /** Uma vez por tick do cliente: a animação para se o jogador tomar dano (docs/animacoes.md, regra 6). */
    public static void aoTick() {
        if (ANIMANDO.isEmpty()) return;
        ANIMANDO.removeIf(p -> {
            ModifierLayer<IAnimation> c = camada(p);
            if (c == null || c.getAnimation() == null || !c.isActive()) return true;
            if (p.hurtTime > 0) {
                c.setAnimation(null);
                return true;
            }
            return false;
        });
    }

    // ---------- animações provisórias ----------

    private static KeyframeAnimation.AnimationBuilder novo(int duracao) {
        KeyframeAnimation.AnimationBuilder b = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_EMOTECRAFT);
        b.beginTick = 0;
        b.endTick = duracao;
        b.stopTick = duracao + 4;
        b.isLooped = false;
        return b;
    }

    private static void chave(KeyframeAnimation.StateCollection.State s, int tick, float valor) {
        s.addKeyFrame(tick, valor, Ease.INOUTSINE);
    }

    /** Olha a própria mão: braço direito sobe até a frente do rosto, cabeça desce para olhar, e volta. 2,5 s. */
    private static KeyframeAnimation marcaAparece() {
        KeyframeAnimation.AnimationBuilder b = novo(50);
        var braco = b.rightArm;
        chave(braco.pitch, 0, 0f);
        chave(braco.pitch, 12, -1.9f);
        chave(braco.pitch, 38, -1.9f);
        chave(braco.pitch, 50, 0f);
        chave(braco.yaw, 0, 0f);
        chave(braco.yaw, 12, -0.4f);
        chave(braco.yaw, 38, -0.4f);
        chave(braco.yaw, 50, 0f);
        chave(b.head.pitch, 0, 0f);
        chave(b.head.pitch, 12, 0.35f);
        chave(b.head.pitch, 38, 0.35f);
        chave(b.head.pitch, 50, 0f);
        return b.build();
    }

    /** Dor: o corpo dobra para frente, mão esquerda no braço direito, e endireita. 2 s. */
    private static KeyframeAnimation marcaSobe() {
        KeyframeAnimation.AnimationBuilder b = novo(40);
        chave(b.body.pitch, 0, 0f);
        chave(b.body.pitch, 8, 0.45f);
        chave(b.body.pitch, 30, 0.45f);
        chave(b.body.pitch, 40, 0f);
        chave(b.leftArm.pitch, 0, 0f);
        chave(b.leftArm.pitch, 8, -1.0f);
        chave(b.leftArm.pitch, 30, -1.0f);
        chave(b.leftArm.pitch, 40, 0f);
        chave(b.leftArm.yaw, 0, 0f);
        chave(b.leftArm.yaw, 8, 0.7f);
        chave(b.leftArm.yaw, 30, 0.7f);
        chave(b.leftArm.yaw, 40, 0f);
        return b.build();
    }

    /** Surto: agarra a cabeça com as duas mãos e se curva. 3 s. */
    private static KeyframeAnimation surto() {
        KeyframeAnimation.AnimationBuilder b = novo(60);
        for (var braco : new KeyframeAnimation.StateCollection[] {b.rightArm, b.leftArm}) {
            chave(braco.pitch, 0, 0f);
            chave(braco.pitch, 8, -2.6f);
            chave(braco.pitch, 50, -2.6f);
            chave(braco.pitch, 60, 0f);
        }
        chave(b.rightArm.yaw, 8, 0.5f);
        chave(b.leftArm.yaw, 8, -0.5f);
        chave(b.rightArm.yaw, 60, 0f);
        chave(b.leftArm.yaw, 60, 0f);
        chave(b.body.pitch, 0, 0f);
        chave(b.body.pitch, 10, 0.6f);
        chave(b.body.pitch, 50, 0.6f);
        chave(b.body.pitch, 60, 0f);
        chave(b.head.pitch, 0, 0f);
        chave(b.head.pitch, 10, 0.5f);
        chave(b.head.pitch, 50, 0.5f);
        chave(b.head.pitch, 60, 0f);
        return b.build();
    }
}
