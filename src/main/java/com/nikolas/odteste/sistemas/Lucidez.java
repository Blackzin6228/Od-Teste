package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.dados.IdsDados;
import com.nikolas.odteste.elementos.Elemento;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Ponto de entrada da Lucidez (só servidor). O conteúdo chama {@link #abalar} (batalha com relíquia, aparição,
 * mob de Sangue) ou {@link #perder}/{@link #recuperar} direto. O dano já entra sozinho pelo Vigia.
 * Chegar a 0 dispara o surto, venha de onde vier (até de comando).
 */
public final class Lucidez {
    /**
     * Até quando (tempo do mundo, em ticks) cada jogador está protegido depois do surto. Só memória:
     * dura 20 s, então sair e voltar no meio só tira a proteção (não dá vantagem). Limpo quando ele sai.
     */
    private static final Map<UUID, Long> PROTEGIDO_ATE = new HashMap<>();
    public static final ResourceLocation ID_LENTIDAO =
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsDados.MODIFICADOR_LENTIDAO);

    private Lucidez() {}

    /** Um abalo do paranormal. Intensidade 1 = normal (0 a 5). */
    public static void abalar(ServerPlayer p, Abalo abalo, float intensidade) {
        if (p == null || abalo == null) return;
        perder(p, abalo.perda(intensidade));
    }

    /** Tira Lucidez (dano, abalo). Logo depois de um surto, não tira nada. Comando de admin não passa por aqui. */
    public static void perder(ServerPlayer p, int quanto) {
        if (quanto <= 0 || protegido(p)) return;
        DadosJogador.definirLucidez(p, DadosJogador.lucidez(p) - quanto);
    }

    public static void recuperar(ServerPlayer p, int quanto) {
        if (quanto <= 0) return;
        int atual = DadosJogador.lucidez(p);
        if (atual >= DadosJogador.lucidezMax(p)) return;
        DadosJogador.definirLucidez(p, atual + quanto);
    }

    /** Está nos 20 s de proteção depois do surto? */
    public static boolean protegido(ServerPlayer p) {
        Long ate = PROTEGIDO_ATE.get(p.getUUID());
        if (ate == null) return false;
        long agora = p.server.overworld().getGameTime();
        // agora < ate - duração: o relógio voltou (mundo trocado); não confia na marca.
        if (agora >= ate || agora < ate - RegrasLucidez.PROTECAO_SURTO_TICKS) {
            PROTEGIDO_ATE.remove(p.getUUID());
            return false;
        }
        return true;
    }

    /**
     * Surto (proposta do docs/sistemas.md): desmaio de 5 s (Cegueira + Lentidão forte) e a Lucidez volta para 15%.
     * Depois, 20 s em que nada tira Lucidez (sem surto em cadeia).
     * Quem está perto vê o corpo e ouve. Não dá Exposição: Exposição é só contato com o paranormal.
     */
    static void surto(ServerPlayer p) {
        PROTEGIDO_ATE.put(p.getUUID(), p.server.overworld().getGameTime() + RegrasLucidez.PROTECAO_SURTO_TICKS);
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, RegrasLucidez.SURTO_TICKS, 0, false, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, RegrasLucidez.SURTO_TICKS, 3, false, false, true));
        Momentos.enviar(p, TipoMomento.SURTO, DadosJogador.marcaElemento(p), Elemento.NENHUM, 0);
        DadosJogador.definirLucidez(p, RegrasLucidez.aposSurto(DadosJogador.lucidezMax(p)));
    }

    /**
     * Lentidão leve conforme a faixa (RegrasLucidez.lentidao). Chamada só quando a faixa muda, ao entrar e ao
     * renascer: nunca por tick. Modificador temporário (não salva; se o mod sair, não sobra nada no jogador).
     */
    public static void atualizarLentidao(ServerPlayer p) {
        AttributeInstance vel = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (vel == null) return;
        double valor = RegrasLucidez.lentidao(FaixaLucidez.de(DadosJogador.lucidez(p), DadosJogador.lucidezMax(p)));
        AttributeModifier atual = vel.getModifier(ID_LENTIDAO);
        if (atual != null && atual.amount() == valor) return;
        vel.removeModifier(ID_LENTIDAO);
        if (valor != 0) {
            vel.addTransientModifier(new AttributeModifier(ID_LENTIDAO, valor,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    static void esquecer(UUID jogador) {
        PROTEGIDO_ATE.remove(jogador);
    }
}
