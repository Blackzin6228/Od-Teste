package com.nikolas.odteste.historia.livros;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Quem já abriu cada livro (para o gancho {@link LivroAbertoPrimeiraVez} disparar uma vez só).
 * Salvo no mundo, no arquivo {@code data/odteste_livros_lidos.dat}: { "<uuid>": ["sineiro", ...] }.
 */
public final class LivrosLidos extends SavedData {
    public static final String ARQUIVO = "odteste_livros_lidos";
    private static final SavedData.Factory<LivrosLidos> FABRICA =
            new SavedData.Factory<>(LivrosLidos::new, LivrosLidos::carregar, null);

    private final Map<UUID, Set<String>> lidos = new HashMap<>();

    public static LivrosLidos de(MinecraftServer servidor) {
        return servidor.overworld().getDataStorage().computeIfAbsent(FABRICA, ARQUIVO);
    }

    /** @return true se é a primeira vez deste jogador com este livro (e já marca como lido) */
    public boolean marcar(UUID jogador, String livro) {
        boolean novo = lidos.computeIfAbsent(jogador, u -> new HashSet<>()).add(livro);
        if (novo) setDirty();
        return novo;
    }

    /** Para o admin repetir o gancho num teste. */
    public boolean esquecer(UUID jogador, String livro) {
        Set<String> s = lidos.get(jogador);
        boolean tirou = s != null && s.remove(livro);
        if (tirou) setDirty();
        return tirou;
    }

    private static LivrosLidos carregar(CompoundTag t, HolderLookup.Provider registros) {
        LivrosLidos d = new LivrosLidos();
        for (String k : t.getAllKeys()) {
            UUID u;
            try {
                u = UUID.fromString(k);
            } catch (IllegalArgumentException estragado) {
                continue;
            }
            ListTag l = t.getList(k, Tag.TAG_STRING);
            Set<String> s = new HashSet<>();
            for (int i = 0; i < l.size(); i++) s.add(l.getString(i));
            d.lidos.put(u, s);
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag t, HolderLookup.Provider registros) {
        lidos.forEach((u, s) -> {
            if (s.isEmpty()) return;
            ListTag l = new ListTag();
            for (String id : s) l.add(StringTag.valueOf(id));
            t.put(u.toString(), l);
        });
        return t;
    }
}
