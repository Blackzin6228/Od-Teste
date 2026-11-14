package com.nikolas.odteste.compat.iris;

import com.nikolas.odteste.OdMod;
import java.lang.reflect.Method;

/**
 * Pergunta ao Iris se há um shader pack ligado. **Só cliente**, chamado de cliente/ atrás de
 * {@code Compat.Integracao.IRIS.ligada()}. Tudo por reflexão (API pública {@code IrisApi}): o nosso mod não
 * depende do Iris para compilar nem para abrir. Se a API mudar ou falhar, responde "sem pack" e avisa uma vez.
 *
 * Técnica aprendida no Apex Arcana (MIT, docs/referencias.md); código nosso.
 */
public final class PackIris {
    private static final String[] API = {"net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi"};
    /** Perguntar ao Iris custa pouco, mas não precisa ser todo quadro. */
    private static final long VALIDADE_NS = 500_000_000L;

    private static boolean procurou;
    private static Object api;
    private static Method emUso;
    private static boolean ultimo;
    private static long perguntouEm;
    private static boolean perguntou;

    private PackIris() {}

    /** Há shader pack ligado agora? */
    public static boolean packLigado() {
        long agora = System.nanoTime();
        if (perguntou && agora - perguntouEm < VALIDADE_NS) return ultimo;
        perguntou = true;
        perguntouEm = agora;
        ultimo = perguntar();
        return ultimo;
    }

    private static boolean perguntar() {
        if (!procurou) procurar();
        if (emUso == null) return false;
        try {
            return Boolean.TRUE.equals(emUso.invoke(api));
        } catch (Throwable falhou) {
            emUso = null;
            api = null;
            OdMod.LOGGER.warn("{}: não deu para perguntar ao Iris se há shader pack; seguindo como se não houvesse.",
                    OdMod.MODID, falhou);
            return false;
        }
    }

    private static void procurar() {
        procurou = true;
        for (String nome : API) {
            try {
                Class<?> tipo = Class.forName(nome, false, PackIris.class.getClassLoader());
                Object instancia = tipo.getMethod("getInstance").invoke(null);
                if (instancia == null) continue;
                api = instancia;
                emUso = tipo.getMethod("isShaderPackInUse");
                OdMod.LOGGER.info("{}: Iris achado ({}); com shader pack, os efeitos de tela vão depois do pack.",
                        OdMod.MODID, nome);
                return;
            } catch (Throwable ignorado) {
                // tenta o próximo nome
            }
        }
        OdMod.LOGGER.warn("{}: Iris está no pack, mas a API dele não respondeu; efeitos de tela no caminho comum.",
                OdMod.MODID);
    }
}
