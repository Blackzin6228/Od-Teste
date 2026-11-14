package com.nikolas.odteste.sinais;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Fonte de sinal pronta, por posição: o conteúdo marca um bloco (ex.: o sino da vila marcada) e o Analisador
 * perto passa a ouvir. Só servidor.
 *
 * <b>Fica só em memória</b> (não cria dado salvo nem ID novo). Quem marca é dono do estado: ao ligar o servidor
 * (ou ao carregar o evento), marque de novo a partir do seu próprio SavedData. Ao desligar, tudo some.
 */
public final class SinaisNoMundo {
    private static final Map<ResourceKey<Level>, Map<BlockPos, Sinal>> PONTOS = new HashMap<>();

    private SinaisNoMundo() {}

    public static void iniciar() {
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, e -> PONTOS.clear());
    }

    /** Faz o bloco em {@code pos} emitir {@code sinal}. Marcar de novo o mesmo bloco troca o sinal. */
    public static void marcar(ServerLevel nivel, BlockPos pos, Sinal sinal) {
        PONTOS.computeIfAbsent(nivel.dimension(), k -> new HashMap<>()).put(pos.immutable(), sinal);
    }

    /** Para de emitir (ex.: "quando o sino calar"). */
    public static void desmarcar(ServerLevel nivel, BlockPos pos) {
        Map<BlockPos, Sinal> daqui = PONTOS.get(nivel.dimension());
        if (daqui != null) daqui.remove(pos);
    }

    static List<Sinal> perto(ServerLevel nivel, BlockPos centro, int raio) {
        Map<BlockPos, Sinal> daqui = PONTOS.get(nivel.dimension());
        if (daqui == null || daqui.isEmpty()) return List.of();
        long raio2 = (long) raio * raio;
        List<Sinal> achados = new ArrayList<>();
        for (Map.Entry<BlockPos, Sinal> e : daqui.entrySet()) {
            if (e.getKey().distSqr(centro) <= raio2) achados.add(e.getValue());
        }
        return achados;
    }
}
