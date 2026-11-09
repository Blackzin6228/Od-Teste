package com.nikolas.odteste.sinais;

import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * Um sinal paranormal que o Analisador de Sinal consegue ler.
 *
 * @param id        nome curto do sinal (ex.: {@code sino_vila}); serve para log e para evitar repetição. Não é salvo.
 * @param decifrado o que o ouvido (ou o olho) já pegaria com esforço: o Analisador entrega direto.
 * @param camada    a segunda camada, que só o aparelho separa. Vazio quando o sinal não tem.
 */
public record Sinal(String id, Component decifrado, Optional<Component> camada) {

    public Sinal {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("sinal sem id");
        if (decifrado == null) throw new IllegalArgumentException("sinal sem texto decifrado: " + id);
        camada = camada == null ? Optional.empty() : camada;
    }

    /** Sinal com textos prontos. {@code camada} pode ser {@code null}. */
    public static Sinal de(String id, Component decifrado, Component camada) {
        return new Sinal(id, decifrado, Optional.ofNullable(camada));
    }

    /**
     * Sinal com os textos no lang: {@code odteste.sinal.<id>.decifrado} e {@code odteste.sinal.<id>.camada}.
     * Use {@code comCamada = false} se o sinal não tiver segunda camada.
     */
    public static Sinal doLang(String id, boolean comCamada) {
        String base = "odteste.sinal." + id;
        return de(id, Component.translatable(base + ".decifrado"),
                comCamada ? Component.translatable(base + ".camada") : null);
    }
}
