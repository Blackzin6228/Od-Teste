package com.nikolas.odteste.historia;

import com.nikolas.odteste.estruturas.Backup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * A vila marcada pela Cena 1 de um grupo, com tudo o que precisa para desfazer: blocos que pusemos,
 * o aldeão que sumiu (inteiro, como estava), os aldeões parados e os marcos que o evento deu.
 * Fica salva no mundo ({@link DadosHistoria}).
 */
public final class VilaMarcada {
    /** Bits de {@link #congelados}: como o aldeão estava antes de parar. */
    public static final int ERA_SEM_IA = 1;
    public static final int ERA_INVULNERAVEL = 2;

    public final String grupo;
    public final ResourceKey<Level> dimensao;
    public final BlockPos sino;
    public BlockPos nota;
    public BlockPos bau;
    public BlockPos placa;
    public BlockPos estandarte;
    public BlockPos cama;
    /** O aldeão que sumiu, salvo inteiro (trocas, profissão, casa) para voltar no desfazer. */
    public CompoundTag aldeao;
    public final Backup blocos;
    /** Aldeões parados olhando o sino → como estavam antes. */
    public final Map<UUID, Integer> congelados = new HashMap<>();
    /** O baú já abriu (FERRO escrito na placa). */
    public boolean resolvido;
    /** Marcos que este evento deu ao grupo (o desfazer tira só esses). */
    public final List<String> marcosDados = new ArrayList<>();

    public VilaMarcada(String grupo, ResourceKey<Level> dimensao, BlockPos sino, Backup blocos) {
        this.grupo = grupo;
        this.dimensao = dimensao;
        this.sino = sino;
        this.blocos = blocos;
    }

    private static void pos(CompoundTag t, String chave, BlockPos p) {
        if (p != null) t.putLong(chave, p.asLong());
    }

    private static BlockPos pos(CompoundTag t, String chave) {
        return t.contains(chave, Tag.TAG_LONG) ? BlockPos.of(t.getLong(chave)) : null;
    }

    public CompoundTag salvar() {
        CompoundTag t = new CompoundTag();
        t.putString("grupo", grupo);
        t.putString("dim", dimensao.location().toString());
        pos(t, "sino", sino);
        pos(t, "nota", nota);
        pos(t, "bau", bau);
        pos(t, "placa", placa);
        pos(t, "estandarte", estandarte);
        pos(t, "cama", cama);
        if (aldeao != null) t.put("aldeao", aldeao);
        t.put("blocos", blocos.salvar());
        CompoundTag cong = new CompoundTag();
        congelados.forEach((u, f) -> cong.putInt(u.toString(), f));
        t.put("congelados", cong);
        t.putBoolean("resolvido", resolvido);
        ListTag marcos = new ListTag();
        for (String m : marcosDados) marcos.add(StringTag.valueOf(m));
        t.put("marcos", marcos);
        return t;
    }

    public static VilaMarcada carregar(CompoundTag t) {
        ResourceLocation dim = ResourceLocation.tryParse(t.getString("dim"));
        BlockPos sino = pos(t, "sino");
        VilaMarcada v = new VilaMarcada(t.getString("grupo"),
                ResourceKey.create(Registries.DIMENSION, dim == null ? Level.OVERWORLD.location() : dim),
                sino == null ? BlockPos.ZERO : sino, Backup.carregar(t.getCompound("blocos")));
        v.nota = pos(t, "nota");
        v.bau = pos(t, "bau");
        v.placa = pos(t, "placa");
        v.estandarte = pos(t, "estandarte");
        v.cama = pos(t, "cama");
        if (t.contains("aldeao", Tag.TAG_COMPOUND)) v.aldeao = t.getCompound("aldeao");
        CompoundTag cong = t.getCompound("congelados");
        for (String k : cong.getAllKeys()) {
            try {
                v.congelados.put(UUID.fromString(k), cong.getInt(k));
            } catch (IllegalArgumentException ignorado) {
                // UUID estragado no arquivo: ignora em vez de crashar.
            }
        }
        v.resolvido = t.getBoolean("resolvido");
        ListTag marcos = t.getList("marcos", Tag.TAG_STRING);
        for (int i = 0; i < marcos.size(); i++) v.marcosDados.add(marcos.getString(i));
        return v;
    }
}
