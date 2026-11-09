package com.nikolas.odteste.estruturas;

import com.nikolas.odteste.OdMod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
 * Regras que continuam valendo:
 * <ul>
 * <li>Cada fatia confere o espaço de novo logo antes de pôr (um jogador pode ter construído ali depois do
 * {@code prever}). Bloqueio pausa e avisa o admin: nada de jogador é apagado.</li>
 * <li>Chunk não carregado: espera (avisa uma vez), não carrega chunk.</li>
 * <li>Tudo entra no mesmo {@link Backup}; {@code desfazer} funciona a qualquer momento, inclusive no meio.</li>
 * <li>O andamento ({@link Colocacao.Partes}) fica salvo junto da colocação: se o servidor reiniciar, continua
 * da fatia salva. Se a fatia achar bloco que não esperava (o mundo salvou antes do andamento), pausa.</li>
 * </ul>
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

    /** Só memória (some ao reiniciar): template já lido, avisos e o que falta atualizar no fim do desfazer. */
    private static final class Memoria {
        StructureTemplate template;
        Espaco.Caixa caixa;
        final List<BlockPos> trocados = new ArrayList<>();
        int vizinhosFeitos;
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
     * Começa a colocar (quem chama já conferiu o espaço da caixa inteira e põe a colocação nos dados).
     * @param admin quem recebe os avisos no chat (null = só no log)
     */
    public static Colocacao comecar(ServerLevel nivel, StructureTemplate t, String id, ResourceLocation template,
                                    BlockPos ponto, Rotation rotacao, UUID admin) {
        Espaco.Caixa c = Estruturas.caixa(t, ponto, rotacao);
        int limite = ConfigEstruturas.blocosPorTick();
        Colocacao.Partes p = new Colocacao.Partes(rotacao, limite, Fatias.total(c, limite), 0, Colocacao.Fase.COLOCANDO, admin);
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

    /** Pausada volta a andar (a próxima fatia confere o espaço de novo). */
    public static boolean continuar(Colocacao col, UUID admin) {
        Colocacao.Partes p = col.partes();
        if (p == null || p.fase != Colocacao.Fase.PAUSADA) return false;
        p.fase = Colocacao.Fase.COLOCANDO;
        if (admin != null) p.admin = admin;
        MEMORIA.remove(col.id());
        talvez = true;
        return true;
    }

    /** Começa a desfazer em partes (pode ser no meio da colocação). */
    public static boolean desfazer(Colocacao col, UUID admin) {
        Colocacao.Partes p = col.partes();
        if (p == null || p.fase == Colocacao.Fase.DESFAZENDO) return false;
        p.fase = Colocacao.Fase.DESFAZENDO;
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
            if (alvo.partes().fase == Colocacao.Fase.DESFAZENDO) passoDesfazer(s, alvo, m, agora);
            else passoColocar(s, alvo, m, agora);
        } catch (RuntimeException ex) {
            // Nunca derrubar o servidor nem repetir o erro todo tick: para e espera o admin.
            OdMod.LOGGER.error("Estrutura {} parou com erro", alvo.id(), ex);
            if (alvo.partes().fase == Colocacao.Fase.COLOCANDO) pausar(s, alvo, "erro interno (ver o log)");
            else m.esperaAte = agora + 20 * 60;
        }
    }

    private static void passoColocar(MinecraftServer s, Colocacao col, Memoria m, long agora) {
        Colocacao.Partes p = col.partes();
        ServerLevel nivel = s.getLevel(col.dimensao());
        if (nivel == null) {
            pausar(s, col, "a dimensão não existe");
            return;
        }
        if (m.template == null) {
            ResourceLocation rl = ResourceLocation.tryParse(col.template());
            m.template = rl == null ? null : Estruturas.template(nivel, rl).orElse(null);
            if (m.template == null) {
                pausar(s, col, "o template sumiu");
                return;
            }
            m.caixa = Estruturas.caixa(m.template, col.canto(), p.rotacao);
            m.ultimoDecimo = p.total == 0 ? 0 : p.fatia * 10 / p.total;
        }
        if (Fatias.total(m.caixa, p.limite) != p.total) {
            // O arquivo da estrutura mudou de tamanho depois que começou: as fatias não batem mais.
            pausar(s, col, "o template mudou de tamanho");
            return;
        }
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
        Estruturas.colocarParte(nivel, m.template, col.canto(), p.rotacao, f, col.backup(), true);
        p.fatia++;
        m.avisouEspera = false;
        acesso.sujo(s);
        if (p.fatia >= p.total) {
            terminarColocacao(s, col);
            return;
        }
        int decimo = p.fatia * 10 / p.total;
        if (decimo > m.ultimoDecimo) {
            m.ultimoDecimo = decimo;
            // TODO lang
            avisar(s, p, Component.literal("Estrutura " + col.id() + ": " + decimo * 10 + "% (" + p.fatia + "/" + p.total + ")")
                    .withStyle(ChatFormatting.GRAY));
        }
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
            Backup.Restauracao r = b.restaurarParte(nivel, acesso::ehPeca, p.limite, m.trocados);
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
        // Tudo de volta: agora os vizinhos se ajustam, também aos pedaços.
        if (m.vizinhosFeitos < m.trocados.size()) {
            int ate = Math.min(m.trocados.size(), m.vizinhosFeitos + p.limite);
            for (int i = m.vizinhosFeitos; i < ate; i++) Backup.atualizarVizinhos(nivel, m.trocados.get(i));
            m.vizinhosFeitos = ate;
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

    private static void pausar(MinecraftServer s, Colocacao col, Espaco.Resultado r) {
        // TODO lang
        pausar(s, col, Component.literal("bloqueio em " + r.x() + " " + r.y() + " " + r.z() + " (")
                .append(Component.translatable("odteste.espaco." + r.motivo().name().toLowerCase(Locale.ROOT)))
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

    /** Avisa o admin que começou (se estiver online) e sempre deixa no log. */
    private static void avisar(MinecraftServer s, Colocacao.Partes p, Component msg) {
        OdMod.LOGGER.info("[estrutura] {}", msg.getString());
        if (p.admin == null) return;
        ServerPlayer j = s.getPlayerList().getPlayer(p.admin);
        if (j != null) j.sendSystemMessage(msg);
    }
}
