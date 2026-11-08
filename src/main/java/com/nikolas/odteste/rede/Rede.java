package com.nikolas.odteste.rede;

import com.nikolas.odteste.dados.DadosJogador;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Pacotes do mod e quando os dados do jogador são mandados para o cliente.
 * Só manda em momentos raros (entrar, renascer, trocar de dimensão, mudança de valor): nada por tick.
 */
public final class Rede {
    /** Versão do protocolo. Mudou o formato de algum pacote? Suba o número. */
    private static final String VERSAO = "1";

    private Rede() {}

    public static void iniciar(IEventBus barramentoDoMod) {
        barramentoDoMod.addListener(Rede::registrarPacotes);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> DadosJogador.sincronizar(e.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent e) -> DadosJogador.sincronizar(e.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent e) -> DadosJogador.sincronizar(e.getEntity()));
    }

    private static void registrarPacotes(RegisterPayloadHandlersEvent evento) {
        evento.registrar(VERSAO)
                .playToClient(SincronizarJogador.TIPO, SincronizarJogador.CODEC, SincronizarJogador::receber);
    }
}
