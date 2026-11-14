package com.nikolas.odteste.historia.vilacalada;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Eventos do Dobre, postados no {@code NeoForge.EVENT_BUS} (só servidor). Ganchos para quem vem depois, sem
 * depender de ninguém: a cutscene do fim (sistema de cenas da mesa Dev) ouve {@link Feito}; o Faminto, quando
 * existir, pode ouvir {@link Toque} e {@link Erro} além da tag {@code odteste:ouve_o_dobre}.
 *
 * <pre>
 * NeoForge.EVENT_BUS.addListener((DobreEvento.Feito e) -> tocarCena(e.presentes(), "dobre", e.sino()));
 * </pre>
 */
public abstract class DobreEvento extends Event {
    private final String grupo;
    private final ServerLevel nivel;
    private final BlockPos sino;

    protected DobreEvento(String grupo, ServerLevel nivel, BlockPos sino) {
        this.grupo = grupo;
        this.nivel = nivel;
        this.sino = sino;
    }

    public String grupo() {
        return grupo;
    }

    public ServerLevel nivel() {
        return nivel;
    }

    /** O sino rachado (no mundo). */
    public BlockPos sino() {
        return sino;
    }

    /** Um toque que contou ({@code numero} de 1 a {@link RegrasDobre#TOQUES}). */
    public static final class Toque extends DobreEvento {
        private final int numero;
        private final ServerPlayer quem;

        public Toque(String grupo, ServerLevel nivel, BlockPos sino, int numero, ServerPlayer quem) {
            super(grupo, nivel, sino);
            this.numero = numero;
            this.quem = quem;
        }

        public int numero() {
            return numero;
        }

        public ServerPlayer quem() {
            return quem;
        }
    }

    /** Errou: {@code tipo} é o nome do {@link RegrasDobre.Toque} (RAPIDO, FORA_DE_HORA, A_MAIS) ou do {@link RegrasDobre.Fim}. */
    public static final class Erro extends DobreEvento {
        private final String tipo;

        public Erro(String grupo, ServerLevel nivel, BlockPos sino, String tipo) {
            super(grupo, nivel, sino);
            this.tipo = tipo;
        }

        public String tipo() {
            return tipo;
        }
    }

    /**
     * O dobre foi feito (último toque e o som morreu). Gancho da cutscene: a câmera sai do campanário e desce a rua
     * enquanto o eco anda {@link RegrasDobre#ECO_BLOCOS_POR_TICK} bloco por tick (cada criatura se ajoelha quando o
     * eco chega); termina na capela ({@link RegrasPrepararVila#VELA} + canto). Texto: {@code odteste.dobre.fim}.
     */
    public static final class Feito extends DobreEvento {
        private final BlockPos canto;
        private final List<ServerPlayer> presentes;

        public Feito(String grupo, ServerLevel nivel, BlockPos sino, BlockPos canto, List<ServerPlayer> presentes) {
            super(grupo, nivel, sino);
            this.canto = canto;
            this.presentes = List.copyOf(presentes);
        }

        /** Canto da estrutura (somar as posições de {@link RegrasPrepararVila} e {@link RegrasDobre}). */
        public BlockPos canto() {
            return canto;
        }

        /** Membros do grupo que estavam perto do sino (quem vê a cena). */
        public List<ServerPlayer> presentes() {
            return presentes;
        }

        private boolean cenaCuida;

        /** A cutscene chama isto: ela mostra o texto final, e o mod não mostra o texto de reserva. */
        public void cenaVaiMostrar() {
            cenaCuida = true;
        }

        public boolean cenaCuida() {
            return cenaCuida;
        }
    }
}
