package com.nikolas.odteste.estruturas;

import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

/**
 * Uma estrutura colocada por comando ou evento, com o backup para desfazer. Fica salva no mundo.
 * Estrutura grande tem {@link Partes} (o andamento da colocação em fatias); a pequena tem {@code null}.
 */
public record Colocacao(String id, String template, ResourceKey<Level> dimensao, BlockPos canto, Backup backup, Partes partes) {

    public Colocacao(String id, String template, ResourceKey<Level> dimensao, BlockPos canto, Backup backup) {
        this(id, template, dimensao, canto, backup, null);
    }

    /** Em que pé está uma colocação em partes. */
    public enum Fase {
        /** Pondo uma fatia por tick. */
        COLOCANDO,
        /** Uma fatia achou bloqueio: espera o admin ({@code continuar} ou {@code desfazer}). */
        PAUSADA,
        /** Tudo colocado. */
        PRONTA,
        /** Desfazendo um pedaço por tick; some da lista no fim. */
        DESFAZENDO;

        static Fase ler(String s) {
            for (Fase f : values()) if (f.name().equalsIgnoreCase(s)) return f;
            return PAUSADA; // valor estranho no arquivo: para e espera o admin, nunca sai colocando
        }
    }

    /**
     * Andamento da colocação em partes. Muda no lugar (o registro da colocação continua o mesmo).
     * {@code limite} é o de quando começou: as fatias dependem dele, então a config nova só vale para a próxima.
     */
    public static final class Partes {
        public final Rotation rotacao;
        public final int limite;
        public final int total;
        public int fatia;
        public Fase fase;
        public UUID admin;

        public Partes(Rotation rotacao, int limite, int total, int fatia, Fase fase, UUID admin) {
            this.rotacao = rotacao;
            this.limite = Math.max(1, limite);
            this.total = Math.max(0, total);
            this.fatia = Math.max(0, fatia);
            this.fase = fase;
            this.admin = admin;
        }

        public boolean ativa() {
            return fase == Fase.COLOCANDO || fase == Fase.DESFAZENDO;
        }

        CompoundTag salvar() {
            CompoundTag t = new CompoundTag();
            t.putString("rotacao", rotacao.name().toLowerCase(Locale.ROOT));
            t.putInt("limite", limite);
            t.putInt("total", total);
            t.putInt("fatia", fatia);
            t.putString("fase", fase.name().toLowerCase(Locale.ROOT));
            if (admin != null) t.putUUID("admin", admin);
            return t;
        }

        static Partes carregar(CompoundTag t) {
            Rotation r = Rotation.NONE;
            for (Rotation x : Rotation.values()) if (x.name().equalsIgnoreCase(t.getString("rotacao"))) r = x;
            return new Partes(r, t.getInt("limite"), t.getInt("total"), t.getInt("fatia"),
                    Fase.ler(t.getString("fase")), t.hasUUID("admin") ? t.getUUID("admin") : null);
        }
    }

    public CompoundTag salvar() {
        CompoundTag t = new CompoundTag();
        t.putString("id", id);
        t.putString("template", template);
        t.putString("dim", dimensao.location().toString());
        t.putLong("canto", canto.asLong());
        t.put("backup", backup.salvar());
        if (partes != null) t.put("partes", partes.salvar());
        return t;
    }

    public static Colocacao carregar(CompoundTag t) {
        ResourceLocation dim = ResourceLocation.tryParse(t.getString("dim"));
        return new Colocacao(t.getString("id"), t.getString("template"),
                ResourceKey.create(Registries.DIMENSION, dim == null ? Level.OVERWORLD.location() : dim),
                BlockPos.of(t.getLong("canto")), Backup.carregar(t.getCompound("backup")),
                t.contains("partes") ? Partes.carregar(t.getCompound("partes")) : null);
    }
}
