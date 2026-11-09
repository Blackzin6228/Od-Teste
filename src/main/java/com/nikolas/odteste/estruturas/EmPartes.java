package com.nikolas.odteste.estruturas;

import com.nikolas.odteste.OdMod;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Colocação e desfazer de estrutura grande em partes: uma fatia ({@link Fatias}) por tick, no máximo
 * {@code blocos_por_tick} blocos ({@link ConfigEstruturas}). Só uma estrutura anda por tick.
 *
 * Fases ({@link Colocacao.Fase}):
 * <ol>
 * <li><b>conferindo</b>: confere o espaço da caixa inteira, fatia por fatia, sem pôr nada. Bloqueio cancela
 * (nada foi colocado, a colocação some). Assim o servidor nunca lê a caixa inteira num tick só.</li>
 * <li><b>colocando</b>: cada fatia confere o próprio espaço de novo logo antes de pôr (um jogador pode ter
 * construído ali no meio). Bloqueio pausa e avisa o admin: nada de jogador é apagado.</li>
 * <li><b>pronta</b>, ou <b>pausada</b> (espera {@code continuar} ou {@code desfazer}).</li>
 * <li><b>desfazendo</b>: um pedaço do backup por tick; no fim os vizinhos se ajustam, também aos pedaços.</li>
 * </ol>
 * Chunk não carregado: espera (avisa uma vez), não carrega chunk. O andamento fica salvo junto da colocação:
 * se o servidor reiniciar, continua de onde parou.
 */
public final class EmPartes {
    /** O que a colocação em partes precisa de fora do pacote (os dados salvos ficam na história). */
    public interface Acesso {
        Map<String, Colocacao> colocacoes(MinecraftServer servidor);

        void sujo(MinecraftServer servidor);

        /** Peça de história num baú nosso: some no desfazer (o resto cai no chão). */
        boolean ehPeca(ItemStack it);
    }

    /** Ticks de espera quando falta chunk carregado. */
    private static final int ESPERA_CHUNK = 40;

    /** Só memória (some ao reiniciar e é refeita): template separado por camada, avisos e contagens. */
    private static final class Memoria {
        Espaco.Caixa caixa;
        StructureTemplate template;
        /** Blocos de cada camada; a camada sai daqui quando é carregada (a memória vai liberando). */
        CompoundTag[] camadas;
        int camadaAtual = -1;
        StructureTemplate templateCamada;
        int inicialDesfazer = -1;
        int mantidos;
        int soltos;
        BlockPos ondeSoltou;
        long esperaAte;
        boolean avisouEspera;
        int ultimoDecimo;
    }

    private static Acesso acesso;
    /** Pode haver trabalho ativo? Falso = o tick nem olha os dados. */
    private static boolean talvez;
    private static final Map<String, Memoria> MEMORIA = new HashMap<>();

    private EmPartes() {}

    public static void iniciar(Acesso a) {
        acesso = a;
        NeoForge.EVENT_BUS.addListener(EmPartes::aoTick);
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> {
            MEMORIA.clear();
            talvez = true; // confere no primeiro tick se ficou algo pela metade
        });
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
            MEMORIA.clear();
            talvez = false;
        });
    }

    // ------------------------------------------------------------------ comandos

    /**
     * Começa pela conferência do espaço (quem chama já conferiu o chão e põe a colocação nos dados).
     * @param admin quem recebe os avisos no chat (null = só no log)
     */
    public static Colocacao comecar(ServerLevel nivel, StructureTemplate t, String id, ResourceLocation template,
                                    BlockPos ponto, Rotation rotacao, UUID admin) {
        Espaco.Caixa c = Estruturas.caixa(t, ponto, rotacao);
        int limite = ConfigEstruturas.blocosPorTick();
        Colocacao.Partes p = new Colocacao.Partes(rotacao, limite, Fatias.total(c, limite), 0, Colocacao.Fase.CONFERINDO, admin);
        Memoria m = new Memoria();
        m.template = t;
        m.caixa = c;
        MEMORIA.put(id, m);
        talvez = true;
        return new Colocacao(id, template.toString(), nivel.dimension(), ponto, new Backup(), p);
    }

    /** Quantas fatias uma estrutura teria com a config de agora (para o {@code prever}). */
    public static int fatiasPrevistas(Espaco.Caixa c) {
        return Fatias.total(c, ConfigEstruturas.blocosPorTick());
    }

    /** Pausada volta a andar. Se nada foi posto ainda, confere tudo de novo antes. */
    public static boolean continuar(Colocacao col, UUID admin) {
        Colocacao.Partes p = col.partes();
        if (p == null || p.fase != Colocacao.Fase.PAUSADA) return false;
        if (col.backup().tamanho() == 0) {
            p.fase = Colocacao.Fase.CONFERINDO;
            p.fatia = 0;
        } else {
            p.fase = Colocacao.Fase.COLOCANDO; // a próxima fatia confere o próprio espaço antes de pôr
        }
        if (admin != null) p.admin = admin;
        MEMORIA.remove(col.id());
        talvez = true;
        return true;
    }

    /** Começa a desfazer em partes (pode ser no meio da conferência ou da colocação). */
    public static boolean desfazer(Colocacao col, UUID admin) {
        Colocacao.Partes p = col.partes();
        if (p == null || p.fase == Colocacao.Fase.DESFAZENDO) return false;
        p.fase = Colocacao.Fase.DESFAZENDO;
        p.vizinhos.clear();
        p.vizinhosFeitos = 0;
        if (admin != null) p.admin = admin;
        MEMORIA.remove(col.id());
        talvez = true;
        return true;
    }

    /** Texto curto do andamento para a {@code lista}. */
    public static String andamento(Colocacao col) {
        Colocacao.Partes p = col.partes();
        if (p == null) return "";
        return switch (p.fase) {
            case PRONTA -> "";
            case DESFAZENDO -> " [desfazendo, faltam " + col.backup().tamanho() + " blocos]";
            default -> " [" + p.fase.name().toLowerCase(Locale.ROOT) + " " + p.fatia + "/" + p.total + "]";
        };
    }

    // ------------------------------------------------------------------ tick

    private static void aoTick(ServerTickEvent.Post e) {
        if (!talvez || acesso == null) return;
        MinecraftServer s = e.getServer();
        long agora = s.getTickCount();
        Colocacao alvo = null;
        boolean algumaAtiva = false;
        for (Colocacao col : acesso.colocacoes(s).values()) {
            if (col.partes() == null || !col.partes().ativa()) continue;
            algumaAtiva = true;
            Memoria m = MEMORIA.get(col.id());
            if (m != null && m.esperaAte > agora) continue;
            alvo = col;
            break;
        }
        if (!algumaAtiva) {
            talvez = false;
            return;
        }
        if (alvo == null) return;
        Memoria m = MEMORIA.computeIfAbsent(alvo.id(), k -> new Memoria());
        try {
            switch (alvo.partes().fase) {
                case CONFERINDO -> passoConferir(s, alvo, m, agora);
                case COLOCANDO -> passoColocar(s, alvo, m, agora);
                case DESFAZENDO -> passoDesfazer(s, alvo, m, agora);
                default -> { }
            }
        } catch (RuntimeException ex) {
            // Nunca derrubar o servidor nem repetir o erro todo tick: para e espera o admin.
            OdMod.LOGGER.error("Estrutura {} parou com erro", alvo.id(), ex);
            if (alvo.partes().fase != Colocacao.Fase.DESFAZENDO) pausar(s, alvo, "erro interno (ver o log)");
            else m.esperaAte = agora + 20 * 60;
        }
    }

    /** Lê o template e a caixa (uma vez por colocação, ou de novo depois de reiniciar). Falso = parou. */
    private static boolean preparar(MinecraftServer s, Colocacao col, Memoria m, ServerLevel nivel) {
        if (m.caixa != null) return true;
        Colocacao.Partes p = col.partes();
        ResourceLocation rl = ResourceLocation.tryParse(col.template());
        m.template = rl == null ? null : Estruturas.template(nivel, rl).orElse(null);
        if (m.template == null) {
            pausar(s, col, "o template sumiu");
            return false;
        }
        m.caixa = Estruturas.caixa(m.template, col.canto(), p.rotacao);
        if (Fatias.total(m.caixa, p.limite) != p.total) {
            // O arquivo da estrutura mudou de tamanho depois que começou: as fatias não batem mais.
            m.caixa = null;
            pausar(s, col, "o template mudou de tamanho");
            return false;
        }
        m.ultimoDecimo = p.total == 0 ? 0 : p.fatia * 10 / p.total;
        return true;
    }

    private static void passoConferir(MinecraftServer s, Colocacao col, Memoria m, long agora) {
        Colocacao.Partes p = col.partes();
        ServerLevel nivel = s.getLevel(col.dimensao());
        if (nivel == null) {
            pausar(s, col, "a dimensão não existe");
            return;
        }
        if (!preparar(s, col, m, nivel)) return;
        if (p.fatia >= p.total) {
            p.fase = Colocacao.Fase.COLOCANDO;
            p.fatia = 0;
            m.ultimoDecimo = 0;
            acesso.sujo(s);
            // TODO lang
            avisar(s, p, Component.literal("Estrutura " + col.id() + ": espaço conferido, colocando em " + p.total + " partes.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        Espaco.Resultado r = Espaco.checar(Fatias.fatia(m.caixa, p.limite, p.fatia), new MundoReal(nivel));
        if (!r.cabe()) {
            if (r.motivo() == Espaco.Tipo.NAO_CARREGADO) {
                esperarChunk(s, col, m, agora, r.x(), r.y(), r.z());
                return;
            }
            // Nada foi posto: só cancela.
            acesso.colocacoes(s).remove(col.id());
            MEMORIA.remove(col.id());
            acesso.sujo(s);
            // TODO lang
            avisar(s, p, Component.literal("Estrutura " + col.id() + " (" + col.template() + ") não cabe: bloqueio em "
                            + r.x() + " " + r.y() + " " + r.z() + " (")
                    .append(motivo(r))
                    .append("). Nada foi colocado; escolha outro ponto e use prever de novo.")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        p.fatia++;
        m.avisouEspera = false;
        acesso.sujo(s);
        progresso(s, col, m, "Conferindo");
    }

    private static void passoColocar(MinecraftServer s, Colocacao col, Memoria m, long agora) {
        Colocacao.Partes p = col.partes();
        ServerLevel nivel = s.getLevel(col.dimensao());
        if (nivel == null) {
            pausar(s, col, "a dimensão não existe");
            return;
        }
        if (!preparar(s, col, m, nivel)) return;
        if (p.fatia >= p.total) {
            terminarColocacao(s, col);
            return;
        }
        Espaco.Caixa f = Fatias.fatia(m.caixa, p.limite, p.fatia);
        Espaco.Resultado r = Espaco.checar(f, new MundoReal(nivel));
        if (!r.cabe()) {
            if (r.motivo() == Espaco.Tipo.NAO_CARREGADO) {
                esperarChunk(s, col, m, agora, r.x(), r.y(), r.z());
                return;
            }
            pausar(s, col, r);
            return;
        }
        // O template é separado por camada uma vez só; cada tick carrega só a camada da vez.
        if (m.camadas == null) {
            m.camadas = Estruturas.porCamada(m.template, col.canto(), p.rotacao, m.caixa, p.limite);
            m.template = null;
        }
        int k = Fatias.camadaDaFatia(m.caixa, p.limite, p.fatia);
        if (k != m.camadaAtual) {
            m.templateCamada = Estruturas.carregarCamada(m.camadas[k]);
            m.camadas[k] = null;
            m.camadaAtual = k;
        }
        Estruturas.colocarParte(nivel, m.templateCamada, col.canto(), p.rotacao, f, col.backup(), true);
        p.fatia++;
        m.avisouEspera = false;
        acesso.sujo(s);
        if (p.fatia >= p.total) {
            terminarColocacao(s, col);
            return;
        }
        progresso(s, col, m, "Colocando");
    }

    private static void progresso(MinecraftServer s, Colocacao col, Memoria m, String fazendo) {
        Colocacao.Partes p = col.partes();
        int decimo = p.total == 0 ? 10 : p.fatia * 10 / p.total;
        if (decimo <= m.ultimoDecimo || decimo >= 10) return;
        m.ultimoDecimo = decimo;
        // TODO lang
        avisar(s, p, Component.literal(fazendo + " " + col.id() + ": " + decimo * 10 + "% (" + p.fatia + "/" + p.total + ")")
                .withStyle(ChatFormatting.GRAY));
    }

    private static void terminarColocacao(MinecraftServer s, Colocacao col) {
        col.partes().fase = Colocacao.Fase.PRONTA;
        MEMORIA.remove(col.id());
        acesso.sujo(s);
        // TODO lang
        avisar(s, col.partes(), Component.literal("Estrutura " + col.id() + " (" + col.template() + ") pronta: "
                + col.backup().tamanho() + " blocos. Para tirar: /odteste estrutura desfazer " + col.id())
                .withStyle(ChatFormatting.GREEN));
    }

    private static void passoDesfazer(MinecraftServer s, Colocacao col, Memoria m, long agora) {
        Colocacao.Partes p = col.partes();
        ServerLevel nivel = s.getLevel(col.dimensao());
        if (nivel == null) {
            m.esperaAte = agora + 20 * 60;
            if (!m.avisouEspera) {
                m.avisouEspera = true;
                // TODO lang
                avisar(s, p, Component.literal("Estrutura " + col.id() + ": a dimensão não existe, desfazer parado.")
                        .withStyle(ChatFormatting.RED));
            }
            return;
        }
        Backup b = col.backup();
        if (m.inicialDesfazer < 0) m.inicialDesfazer = Math.max(1, b.tamanho());
        if (b.tamanho() > 0) {
            BlockPos fora = b.descarregadaNoFim(nivel, p.limite);
            if (fora != null) {
                esperarChunk(s, col, m, agora, fora.getX(), fora.getY(), fora.getZ());
                return;
            }
            Backup.Restauracao r = b.restaurarParte(nivel, acesso::ehPeca, p.limite, p.vizinhos);
            m.mantidos += r.mantidos();
            m.soltos += r.itensSoltos();
            if (m.ondeSoltou == null) m.ondeSoltou = r.ondeSoltou();
            m.avisouEspera = false;
            acesso.sujo(s);
            int decimo = (int) ((m.inicialDesfazer - b.tamanho()) * 10L / m.inicialDesfazer);
            if (decimo > m.ultimoDecimo && b.tamanho() > 0) {
                m.ultimoDecimo = decimo;
                // TODO lang
                avisar(s, p, Component.literal("Desfazendo " + col.id() + ": " + decimo * 10 + "%").withStyle(ChatFormatting.GRAY));
            }
            return;
        }
        // Tudo de volta: agora os vizinhos se ajustam, também aos pedaços (a lista fica salva).
        if (p.vizinhosFeitos < p.vizinhos.size()) {
            int ate = Math.min(p.vizinhos.size(), p.vizinhosFeitos + p.limite);
            for (int i = p.vizinhosFeitos; i < ate; i++) Backup.atualizarVizinhos(nivel, BlockPos.of(p.vizinhos.getLong(i)));
            p.vizinhosFeitos = ate;
            acesso.sujo(s);
            return;
        }
        acesso.colocacoes(s).remove(col.id());
        acesso.sujo(s);
        MEMORIA.remove(col.id());
        // TODO lang
        String fim = "Estrutura " + col.id() + " desfeita. Blocos mantidos (jogador mexeu): " + m.mantidos + ".";
        if (m.soltos > 0 && m.ondeSoltou != null) {
            fim += " Itens de jogador soltos no chão: " + m.soltos + " (perto de " + m.ondeSoltou.toShortString() + ").";
        }
        avisar(s, p, Component.literal(fim).withStyle(ChatFormatting.GREEN));
    }

    private static void esperarChunk(MinecraftServer s, Colocacao col, Memoria m, long agora, int x, int y, int z) {
        m.esperaAte = agora + ESPERA_CHUNK;
        if (m.avisouEspera) return;
        m.avisouEspera = true;
        // TODO lang
        avisar(s, col.partes(), Component.literal("Estrutura " + col.id() + ": esperando o chunk em " + x + " " + y + " " + z
                + " carregar (fique perto). Continua sozinha.").withStyle(ChatFormatting.YELLOW));
    }

    private static Component motivo(Espaco.Resultado r) {
        return Component.translatable("odteste.espaco." + r.motivo().name().toLowerCase(Locale.ROOT));
    }

    private static void pausar(MinecraftServer s, Colocacao col, Espaco.Resultado r) {
        // TODO lang
        pausar(s, col, Component.literal("bloqueio em " + r.x() + " " + r.y() + " " + r.z() + " (")
                .append(motivo(r))
                .append(")"));
    }

    private static void pausar(MinecraftServer s, Colocacao col, String motivo) {
        pausar(s, col, Component.literal(motivo));
    }

    private static void pausar(MinecraftServer s, Colocacao col, Component motivo) {
        Colocacao.Partes p = col.partes();
        p.fase = Colocacao.Fase.PAUSADA;
        MEMORIA.remove(col.id());
        acesso.sujo(s);
        // TODO lang
        avisar(s, p, Component.literal("Estrutura " + col.id() + " pausada na parte " + (p.fatia + 1) + "/" + p.total + ": ")
                .append(motivo)
                .append(". Use /odteste estrutura continuar " + col.id() + " ou desfazer " + col.id() + ".")
                .withStyle(ChatFormatting.RED));
    }

    /** Avisa o admin da colocação (se estiver online) e sempre deixa no log. */
    private static void avisar(MinecraftServer s, Colocacao.Partes p, Component msg) {
        OdMod.LOGGER.info("[estrutura] {}", msg.getString());
        if (p.admin == null) return;
        ServerPlayer j = s.getPlayerList().getPlayer(p.admin);
        if (j != null) j.sendSystemMessage(msg);
    }
}
