package com.nikolas.odteste.estruturas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Uma estrutura colocada por comando ou evento, com o backup para desfazer. Fica salva no mundo. */
public record Colocacao(String id, String template, ResourceKey<Level> dimensao, BlockPos canto, Backup backup) {

    public CompoundTag salvar() {
        CompoundTag t = new CompoundTag();
        t.putString("id", id);
        t.putString("template", template);
        t.putString("dim", dimensao.location().toString());
        t.putLong("canto", canto.asLong());
        t.put("backup", backup.salvar());
        return t;
    }

    public static Colocacao carregar(CompoundTag t) {
        ResourceLocation dim = ResourceLocation.tryParse(t.getString("dim"));
        return new Colocacao(t.getString("id"), t.getString("template"),
                ResourceKey.create(Registries.DIMENSION, dim == null ? Level.OVERWORLD.location() : dim),
                BlockPos.of(t.getLong("canto")), Backup.carregar(t.getCompound("backup")));
    }
}
