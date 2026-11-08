package com.nikolas.odteste.sistemas;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Ponto de entrada da Lucidez (só servidor). O conteúdo chama {@link #abalar} (batalha com relíquia, aparição,
 * mob de Sangue) ou {@link #perder}/{@link #recuperar} direto. O dano já entra sozinho pelo Vigia.
 * Chegar a 0 dispara o surto, venha de onde vier (até de comando).
 */
public final class Lucidez {
    private Lucidez() {}

    /** Um abalo do paranormal. Intensidade 1 = normal (0 a 5). */
    public static void abalar(ServerPlayer p, Abalo abalo, float intensidade) {
        if (p == null || abalo == null) return;
        perder(p, abalo.perda(intensidade));
    }

    public static void perder(ServerPlayer p, int quanto) {
        if (quanto <= 0) return;
        DadosJogador.definirLucidez(p, DadosJogador.lucidez(p) - quanto);
    }

    public static void recuperar(ServerPlayer p, int quanto) {
        if (quanto <= 0) return;
        int atual = DadosJogador.lucidez(p);
        if (atual >= DadosJogador.lucidezMax(p)) return;
        DadosJogador.definirLucidez(p, atual + quanto);
    }

    /**
     * Surto (proposta do docs/sistemas.md): desmaio de 5 s (Cegueira + Lentidão forte) e a Lucidez volta para 15%.
     * Quem está perto vê o corpo e ouve. Não dá Exposição: Exposição é só contato com o paranormal.
     */
    static void surto(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, RegrasLucidez.SURTO_TICKS, 0, false, false, true));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, RegrasLucidez.SURTO_TICKS, 3, false, false, true));
        Momentos.enviar(p, TipoMomento.SURTO, DadosJogador.marcaElemento(p), Elemento.NENHUM, 0);
        DadosJogador.definirLucidez(p, RegrasLucidez.aposSurto(DadosJogador.lucidezMax(p)));
    }
}
