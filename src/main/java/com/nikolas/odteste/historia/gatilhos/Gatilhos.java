package com.nikolas.odteste.historia.gatilhos;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.DadosHistoria;
import com.nikolas.odteste.historia.Grupos;
import com.nikolas.odteste.historia.IdsHistoria;
import com.nikolas.odteste.historia.PrimeiroSinal;
import com.nikolas.odteste.historia.livros.LivroAbertoPrimeiraVez;
import com.nikolas.odteste.historia.vilacalada.IdsVilaCalada;
import com.nikolas.odteste.historia.vilacalada.RegrasVilaCalada;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Gatilhos do Ato 1 (regras em {@link RegrasGatilhos}). Só servidor.
 * <ul>
 * <li>Primeiro Sinal sozinho na primeira noite de neblina, para grupo que ainda não o teve.</li>
 * <li>Livro do Sineiro aberto pela primeira vez: o sino rachado toca uma vez, fraco, ali (só um toque).</li>
 * <li>Grupo que não segue o sino até a Vila Calada: aviso só para os admins.</li>
 * </ul>
 * Sem lag: olha os grupos a cada 5 s (grupos são poucos, laço curto); no máximo uma tentativa de soltar o evento
 * por olhada (a busca de vila do Primeiro Sinal é a única parte com peso).
 */
public final class Gatilhos {
    private static DadosGatilhos cache;

    private Gatilhos() {}

    /** Chamado no construtor do mod (pelo {@code PrimeiroSinal.iniciar}). */
    public static void iniciar() {
        ConfigGatilhos.registrar();
        NeoForge.EVENT_BUS.addListener(Gatilhos::aoTick);
        NeoForge.EVENT_BUS.addListener(Gatilhos::aoAbrirLivro);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> cache = null);
    }

    static DadosGatilhos dados(MinecraftServer s) {
        if (cache == null) cache = DadosGatilhos.de(s);
        return cache;
    }

    // ------------------------------------------------------------------ olhada nos grupos

    private static void aoTick(ServerTickEvent.Post e) {
        MinecraftServer s = e.getServer();
        if (s.getTickCount() % RegrasGatilhos.INTERVALO_TICKS != 0) return;
        DadosHistoria dh = DadosHistoria.de(s);
        DadosGatilhos dg = dados(s);
        List<Grupos.Grupo> grupos = new ArrayList<>(dh.grupos.todos());
        dg.manterSo(grupos.stream().map(Grupos.Grupo::nome).toList());
        if (grupos.isEmpty()) return;

        ServerLevel mundo = s.overworld();
        RegrasGatilhos.Momento m = new RegrasGatilhos.Momento(mundo.getDayTime(), mundo.isRaining(),
                ConfigGatilhos.precisaChuva(), ConfigGatilhos.automatico(),
                ConfigGatilhos.avisoLigado(), ConfigGatilhos.noitesAviso());
        boolean admin = adminOnline(s);
        boolean temVila = dh.ancoras.containsKey(IdsVilaCalada.LUGAR_VILA_CALADA);
        boolean tentou = false;

        for (Grupos.Grupo g : grupos) {
            EstadoGrupo st = dg.estado(g.nome());
            boolean online = algumOnline(s, g);
            boolean teve = g.tem(IdsHistoria.MARCO_PRIMEIRO_SINAL) || dh.vilas.containsKey(g.nome());
            RegrasGatilhos.Grupo agora = new RegrasGatilhos.Grupo(teve, g.tem(IdsVilaCalada.MARCO_CHEGOU), temVila,
                    online, !tentou, admin);
            switch (RegrasGatilhos.passo(st, m, agora)) {
                case SOLTAR_PRIMEIRO_SINAL -> {
                    tentou = true;
                    soltar(s, g, st);
                }
                case AVISAR_ADMIN -> {
                    paraAdmins(s, Component.translatable("odteste.gatilhos.admin.nao_seguiu_sino", g.nome())
                            .withStyle(ChatFormatting.GOLD));
                    st.avisou();
                    OdMod.LOGGER.info("Gatilhos: o grupo {} não seguiu o sino ({} noites)", g.nome(), st.noites);
                }
                case NADA -> { }
            }
            if (st.limparSujo()) dg.setDirty();
        }
    }

    private static void soltar(MinecraftServer s, Grupos.Grupo g, EstadoGrupo st) {
        PrimeiroSinal.Resposta r = PrimeiroSinal.comecar(s, g.nome(), null, null);
        if (r.ok()) {
            st.soltou();
            OdMod.LOGGER.info("Gatilhos: Primeiro Sinal soltou sozinho para o grupo {} (noite de neblina)", g.nome());
            paraAdmins(s, Component.translatable("odteste.gatilhos.admin.primeiro_sinal_soltou", g.nome())
                    .withStyle(ChatFormatting.GRAY));
        } else {
            // Sem vila perto, sem espaço...: tenta de novo na próxima noite de neblina.
            OdMod.LOGGER.info("Gatilhos: Primeiro Sinal não soltou para o grupo {}: {}", g.nome(), r.mensagem().getString());
            paraAdmins(s, Component.translatable("odteste.gatilhos.admin.primeiro_sinal_falhou", g.nome(), r.mensagem())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static boolean algumOnline(MinecraftServer s, Grupos.Grupo g) {
        for (UUID u : g.membros().keySet()) {
            ServerPlayer p = s.getPlayerList().getPlayer(u);
            if (p != null && !p.isSpectator()) return true;
        }
        return false;
    }

    private static boolean adminOnline(MinecraftServer s) {
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (p.hasPermissions(2)) return true;
        }
        return false;
    }

    private static void paraAdmins(MinecraftServer s, Component msg) {
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            if (p.hasPermissions(2)) p.sendSystemMessage(msg);
        }
    }

    // ------------------------------------------------------------------ Livro do Sineiro

    /**
     * Primeira vez que o jogador abre o Livro do Sineiro: um toque só do sino rachado (o mesmo som das pistas),
     * fraco, um pouco acima dele (o atril fica ao pé do sino). Quem está perto também ouve. O grupo dele conta
     * como "seguiu o sino".
     */
    private static void aoAbrirLivro(LivroAbertoPrimeiraVez e) {
        if (!IdsGatilhos.LIVRO_SINEIRO.equals(e.livro())) return;
        ServerPlayer p = e.jogador();
        ServerLevel nivel = p.serverLevel();
        double x = p.getX();
        double y = p.getEyeY() + 2;
        double z = p.getZ();
        nivel.playSound(null, x, y, z, SoundEvents.BELL_BLOCK, SoundSource.AMBIENT, 0.6f, RegrasVilaCalada.TOM_GRAVE);
        nivel.playSound(null, x, y, z, SoundEvents.BELL_BLOCK, SoundSource.AMBIENT, 0.36f, RegrasVilaCalada.TOM_RACHA);
        nivel.playSound(null, x, y, z, SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 0.3f, RegrasVilaCalada.TOM_GRAVE);

        DadosHistoria dh = DadosHistoria.de(p.server);
        dh.grupos.grupoDe(p.getUUID()).ifPresent(g -> {
            DadosGatilhos dg = dados(p.server);
            EstadoGrupo st = dg.estado(g.nome());
            st.leuLivro();
            if (st.limparSujo()) dg.setDirty();
        });
    }
}
