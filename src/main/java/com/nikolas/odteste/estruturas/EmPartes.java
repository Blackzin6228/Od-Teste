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
import net.minecraft.network.chat.MutableComponent;
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
 *
 * <p>Erro no meio de uma fatia: o que já entrou fica no backup ({@link Estruturas#colocarParte}) e a colocação
 * pausa. No {@code continuar}, a fatia confere o espaço de novo, mas bloco nosso (no backup e ainda igual) não
 * conta como bloqueio: ela é refeita por cima, sem travar para sempre.
 *
 * <p><b>Líquido solto no template não serve para estrutura grande:</b> a água de uma fatia escorre para a
 * próxima antes de ela entrar, e a próxima pausa (água que não é nossa conta como bloqueio). Use bloco alagado
 * fechado (que não vaza) ou ponha o líquido depois, à mão.
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
    public static Component andamento(Colocacao col) {
        Colocacao.Partes p = col.partes();
        if (p == null) return Component.empty();
        return switch (p.fase) {
            case PRONTA -> Component.empty();
            case DESFAZENDO -> Component.translatable("odteste.estrutura.partes.andamento.desfazendo", col.backup().tamanho());
            default -> Component.translatable("odteste.estrutura.partes.andamento",
                    Component.translatable("odteste.estrutura.partes.fase." + p.fase.name().toLowerCase(Locale.ROOT)),
                    p.fatia, p.total);
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
            if (alvo.partes().fase != Colocacao.Fase.DESFAZENDO) pausar(s, alvo, "odteste.estrutura.partes.motivo.erro");
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
            pausar(s, col, "odteste.estrutura.partes.motivo.template");
            return false;
        }
        m.caixa = Estruturas.caixa(m.template, col.canto(), p.rotacao);
        if (Fatias.total(m.caixa, p.limite) != p.total) {
            // O arquivo da estrutura mudou de tamanho depois que começou: as fatias não batem mais.
            m.caixa = null;
            pausar(s, col, "odteste.estrutura.partes.motivo.tamanho");
            return false;
        }
        m.ultimoDecimo = p.total == 0 ? 0 : p.fatia * 10 / p.total;
        return true;
    }

    private static void passoConferir(MinecraftServer s, Colocacao col, Memoria m, long agora) {
        Colocacao.Partes p = col.partes();
        ServerLevel nivel = s.getLevel(col.dimensao());
        if (nivel == null) {
            pausar(s, col, "odteste.estrutura.partes.motivo.dimensao");
            return;
        }
        if (!preparar(s, col, m, nivel)) return;
        if (p.fatia >= p.total) {
            p.fase = Colocacao.Fase.COLOCANDO;
            p.fatia = 0;
            m.ultimoDecimo = 0;
            acesso.sujo(s);
            avisar(s, p, Component.translatable("odteste.estrutura.partes.conferido", col.id(), p.total)
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
            avisar(s, p, Component.translatable("odteste.estrutura.partes.nao_cabe", col.id(), col.template(),
                            r.x(), r.y(), r.z(), motivo(r))
                    .withStyle(ChatFormatting.RED));
            return;
        }
        p.fatia++;
        m.avisouEspera = false;
        acesso.sujo(s);
        progresso(s, col, m, "odteste.estrutura.partes.progresso.conferindo");
    }

    private static void passoColocar(MinecraftServer s, Colocacao col, Memoria m, long agora) {
        Colocacao.Partes p = col.partes();
        ServerLevel nivel = s.getLevel(col.dimensao());
        if (nivel == null) {
            pausar(s, col, "odteste.estrutura.partes.motivo.dimensao");
            return;
        }
        if (!preparar(s, col, m, nivel)) return;
        if (p.fatia >= p.total) {
            terminarColocacao(s, col);
            return;
        }
        Espaco.Caixa f = Fatias.fatia(m.caixa, p.limite, p.fatia);
        Espaco.Resultado r = Espaco.checar(f, semOsNossos(nivel, col.backup()));
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
        progresso(s, col, m, "odteste.estrutura.partes.progresso.colocando");
    }

    /**
     * O mundo para a checagem de uma fatia na hora de colocar: bloco que está no backup e ainda é o que nós
     * pusemos conta como ar (a fatia parou no meio por erro e vai ser refeita por cima). Bloco de jogador,
     * claim e chunk descarregado continuam barrando. O mapa do backup só é montado se aparecer bloqueio.
     */
    private static Espaco.Mundo semOsNossos(ServerLevel nivel, Backup b) {
        MundoReal real = new MundoReal(nivel);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        return (x, y, z) -> {
            Espaco.Tipo t = real.tipo(x, y, z);
            if (t != Espaco.Tipo.OCUPADO && t != Espaco.Tipo.COM_DADOS && t != Espaco.Tipo.LIQUIDO) return t;
            if (b.tamanho() == 0) return t;
            cursor.set(x, y, z);
            return b.ehNosso(cursor, nivel.getBlockState(cursor)) ? Espaco.Tipo.AR : t;
        };
    }

    private static void progresso(MinecraftServer s, Colocacao col, Memoria m, String chave) {
        Colocacao.Partes p = col.partes();
        int decimo = p.total == 0 ? 10 : p.fatia * 10 / p.total;
        if (decimo <= m.ultimoDecimo || decimo >= 10) return;
        m.ultimoDecimo = decimo;
        avisar(s, p, Component.translatable(chave, col.id(), decimo * 10, p.fatia, p.total)
                .withStyle(ChatFormatting.GRAY));
    }

    private static void terminarColocacao(MinecraftServer s, Colocacao col) {
        col.partes().fase = Colocacao.Fase.PRONTA;
        MEMORIA.remove(col.id());
        acesso.sujo(s);
        avisar(s, col.partes(), Component.translatable("odteste.estrutura.partes.pronta", col.id(), col.template(),
                        col.backup().tamanho(), col.id())
                .withStyle(ChatFormatting.GREEN));
    }

    private static void passoDesfazer(MinecraftServer s, Colocacao col, Memoria m, long agora) {
        Colocacao.Partes p = col.partes();
        ServerLevel nivel = s.getLevel(col.dimensao());
        if (nivel == null) {
            m.esperaAte = agora + 20 * 60;
            if (!m.avisouEspera) {
                m.avisouEspera = true;
                avisar(s, p, Component.translatable("odteste.estrutura.partes.sem_dimensao", col.id())
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
                avisar(s, p, Component.translatable("odteste.estrutura.partes.desfazendo", col.id(), decimo * 10)
                        .withStyle(ChatFormatting.GRAY));
            }
            return;
        }
        // Tudo de volta: agora os vizinhos se ajustam, também aos pedaços (a lista fica salva). Cada posição
        // avisa até 12 blocos em volta, então o pedaço é uma fração do limite.
        if (p.vizinhosFeitos < p.vizinhos.size()) {
            int ate = Math.min(p.vizinhos.size(), p.vizinhosFeitos + p.vizinhosPorTick());
            for (int i = p.vizinhosFeitos; i < ate; i++) Backup.atualizarVizinhos(nivel, BlockPos.of(p.vizinhos.getLong(i)));
            p.vizinhosFeitos = ate;
            acesso.sujo(s);
            return;
        }
        acesso.colocacoes(s).remove(col.id());
        acesso.sujo(s);
        MEMORIA.remove(col.id());
        MutableComponent fim = Component.translatable("odteste.estrutura.partes.desfeita", col.id(), m.mantidos);
        if (m.soltos > 0 && m.ondeSoltou != null) {
            fim.append(Component.translatable("odteste.estrutura.partes.soltos", m.soltos, m.ondeSoltou.toShortString()));
        }
        avisar(s, p, fim.withStyle(ChatFormatting.GREEN));
    }

    private static void esperarChunk(MinecraftServer s, Colocacao col, Memoria m, long agora, int x, int y, int z) {
        m.esperaAte = agora + ESPERA_CHUNK;
        if (m.avisouEspera) return;
        m.avisouEspera = true;
        avisar(s, col.partes(), Component.translatable("odteste.estrutura.partes.esperando", col.id(), x, y, z)
                .withStyle(ChatFormatting.YELLOW));
    }

    private static Component motivo(Espaco.Resultado r) {
        return Component.translatable("odteste.espaco." + r.motivo().name().toLowerCase(Locale.ROOT));
    }

    private static void pausar(MinecraftServer s, Colocacao col, Espaco.Resultado r) {
        pausar(s, col, Component.translatable("odteste.estrutura.partes.bloqueio", r.x(), r.y(), r.z(), motivo(r)));
    }

    /** @param chaveMotivo chave de tradução do motivo ({@code odteste.estrutura.partes.motivo.*}) */
    private static void pausar(MinecraftServer s, Colocacao col, String chaveMotivo) {
        pausar(s, col, Component.translatable(chaveMotivo));
    }

    private static void pausar(MinecraftServer s, Colocacao col, Component motivo) {
        Colocacao.Partes p = col.partes();
        p.fase = Colocacao.Fase.PAUSADA;
        MEMORIA.remove(col.id());
        acesso.sujo(s);
        avisar(s, p, Component.translatable("odteste.estrutura.partes.pausada", col.id(), p.fatia + 1, p.total, motivo,
                        col.id(), col.id())
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
