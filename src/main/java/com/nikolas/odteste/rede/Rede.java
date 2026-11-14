package com.nikolas.odteste.rede;

import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.elementos.Elemento;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Pacotes do mod e quando os dados do jogador são mandados para o cliente.
 * Só manda em momentos raros (entrar, renascer, trocar de dimensão, mudança de valor, alguém começar a ver
 * o jogador): nada por tick.
 */
public final class Rede {
    /** Versão do protocolo. Mudou o formato de algum pacote? Suba o número. */
    private static final String VERSAO = "5";

    private Rede() {}

    public static void iniciar(IEventBus barramentoDoMod) {
        barramentoDoMod.addListener(Rede::registrarPacotes);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> DadosJogador.sincronizar(e.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent e) -> DadosJogador.sincronizar(e.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent e) -> DadosJogador.sincronizar(e.getEntity()));
        // Alguém começou a ver um jogador marcado: manda só a Marca dele (para desenhar no corpo).
        NeoForge.EVENT_BUS.addListener((PlayerEvent.StartTracking e) -> {
            if (e.getTarget() instanceof Player alvo && e.getEntity() instanceof ServerPlayer quemVe
                    && DadosJogador.marcaElemento(alvo) != Elemento.NENHUM) {
                PacketDistributor.sendToPlayer(quemVe, MarcaVisivel.de(alvo));
            }
        });
    }

    private static void registrarPacotes(RegisterPayloadHandlersEvent evento) {
        evento.registrar(VERSAO)
                .playToClient(SincronizarJogador.TIPO, SincronizarJogador.CODEC, SincronizarJogador::receber)
                .playToClient(Momento.TIPO, Momento.CODEC, Momento::receber)
                .playToClient(MarcaVisivel.TIPO, MarcaVisivel.CODEC, MarcaVisivel::receber)
                .playToClient(TocarCena.TIPO, TocarCena.CODEC, TocarCena::receber)
                .playToClient(ClimaVila.TIPO, ClimaVila.CODEC, ClimaVila::receber)
                .playToClient(FaseMagia.TIPO, FaseMagia.CODEC, FaseMagia::receber)
                .playToClient(AreaHorror.TIPO, AreaHorror.CODEC, AreaHorror::receber)
                .playToClient(Susto.TIPO, Susto.CODEC, Susto::receber);
    }
}
