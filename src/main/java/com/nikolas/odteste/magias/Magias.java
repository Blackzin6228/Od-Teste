package com.nikolas.odteste.magias;

import com.nikolas.odteste.dados.DadosJogador;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Entrada das magias próprias (só servidor). Dois caminhos chamam o mesmo efeito:
 * - com Iron's: o pergaminho/grimório dele (compat/irons), pagando mana;
 * - sem Iron's: {@link #conjurarPelaAfinidade}, pagando só Lucidez. É o gancho da **tecla de Afinidade**,
 *   que ainda não existe: quando ela entrar, o pacote da tecla chama este método (docs/progresso.md).
 * Nada daqui fica salvo: a recarga sem Iron's é só memória (sair e voltar só zera 2 s de espera).
 */
public final class Magias {
    public enum Resultado { CONJUROU, DESCONHECIDA, RECARGA, SEM_LUCIDEZ, OCUPADO }

    /** Último uso pela Afinidade (jogador → tick do servidor). Limpo quando ele sai. */
    private static final Map<UUID, Long> ULTIMO_USO = new HashMap<>();

    private Magias() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> TalhoRubro.aoTick(e.getServer()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> ULTIMO_USO.remove(e.getEntity().getUUID()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
            ULTIMO_USO.clear();
            TalhoRubro.limpar();
        });
        ComandosMagia.iniciar();
    }

    /** Teste do admin: solta a magia de graça (sem mana, Lucidez nem recarga), no nível 1. */
    public static Resultado testar(ServerPlayer p, String magia) {
        if (!IdsMagia.TALHO_RUBRO.equals(magia)) return Resultado.DESCONHECIDA;
        return TalhoRubro.conjurar(p, RegrasTalhoRubro.dano(1, 1f), TalhoRubro.DANO_VANILLA)
                ? Resultado.CONJUROU : Resultado.OCUPADO;
    }

    /**
     * Caminho sem Iron's (tecla de Afinidade): paga só Lucidez e respeita a recarga.
     * Também funciona com o Iron's no pack (o admin testa pelo comando), mas a tecla só vai existir sem ele.
     */
    public static Resultado conjurarPelaAfinidade(ServerPlayer p, String magia) {
        if (!IdsMagia.TALHO_RUBRO.equals(magia)) return Resultado.DESCONHECIDA;
        long agora = p.server.getTickCount();
        if (!RegrasTalhoRubro.recargaPronta(agora, ULTIMO_USO.getOrDefault(p.getUUID(), -1L))) return Resultado.RECARGA;
        int custo = p.isCreative() ? 0 : RegrasTalhoRubro.LUCIDEZ_SEM_IRONS;
        if (!RegrasTalhoRubro.podePagarLucidez(DadosJogador.lucidez(p), custo)) return Resultado.SEM_LUCIDEZ;
        if (!TalhoRubro.conjurar(p, RegrasTalhoRubro.dano(1, 1f), TalhoRubro.DANO_VANILLA)) return Resultado.OCUPADO;
        // Paga direto (não pela Lucidez.perder): a proteção pós-surto não pode deixar a magia sair de graça.
        if (custo > 0) DadosJogador.definirLucidez(p, DadosJogador.lucidez(p) - custo);
        ULTIMO_USO.put(p.getUUID(), agora);
        return Resultado.CONJUROU;
    }
}
