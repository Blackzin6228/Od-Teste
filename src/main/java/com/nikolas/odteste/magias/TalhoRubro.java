package com.nikolas.odteste.magias;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.rede.FaseMagia;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Talho Rubro (Sangue, Básico): corte em meia-lua à frente (4 blocos) + sangramento curto.
 * O efeito mora aqui; o Iron's (compat/irons) e a tecla de Afinidade ({@link Magias}) só chamam {@link #conjurar}.
 *
 * Servidor: calcula alvos e dano no golpe, e manda um pacote por fase. Nada é salvo e nenhum bloco muda:
 * as manchas no chão são só desenho no cliente (somem em 3 s).
 * Custo: uma lista curta de golpes esperando a preparação (no máximo {@link #MAX_ESPERANDO}), olhada uma vez
 * por tick só quando não está vazia; a busca de alvos é uma caixa de 4 blocos em volta de quem conjura.
 */
public final class TalhoRubro {
    public static final ResourceKey<DamageType> DANO = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(OdMod.MODID, IdsMagia.DANO_MAGIA_SANGUE));
    /** Trava contra excesso (ex.: muitos conjurando ao mesmo tempo): acima disso, o golpe novo não entra. */
    static final int MAX_ESPERANDO = 64;

    /** Como o dano chega ao alvo. Sem Iron's: dano vanilla do tipo odteste:magia_sangue. Com Iron's: o dele. */
    @FunctionalInterface
    public interface AplicarDano {
        /** Devolve true se o golpe pegou (aí entra o sangramento). */
        boolean aplicar(LivingEntity quem, LivingEntity alvo, float dano);
    }

    public static final AplicarDano DANO_VANILLA = (quem, alvo, dano) -> alvo.hurt(fonte(quem), dano);

    private record Esperando(LivingEntity quem, UUID id, long golpeEm, float dano, AplicarDano aplicar) {}

    private static final List<Esperando> ESPERANDO = new ArrayList<>();

    private TalhoRubro() {}

    public static DamageSource fonte(LivingEntity quem) {
        Holder<DamageType> tipo = quem.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DANO);
        return new DamageSource(tipo, quem, quem);
    }

    /**
     * Começa a magia: fase de preparação agora, golpe daqui a {@link RegrasTalhoRubro#PREPARACAO_TICKS}.
     * Se a mesma criatura já está preparando um Talho, este é ignorado (não empilha).
     *
     * @return true se começou
     */
    public static boolean conjurar(LivingEntity quem, float dano, AplicarDano aplicar) {
        if (!(quem.level() instanceof ServerLevel nivel) || !quem.isAlive()) return false;
        if (ESPERANDO.size() >= MAX_ESPERANDO) return false;
        for (Esperando e : ESPERANDO) if (e.id().equals(quem.getUUID())) return false;
        long agora = nivel.getServer().getTickCount();
        ESPERANDO.add(new Esperando(quem, quem.getUUID(), agora + RegrasTalhoRubro.PREPARACAO_TICKS, dano,
                aplicar == null ? DANO_VANILLA : aplicar));
        enviar(quem, FaseMagia.PREPARACAO, nivel.random.nextInt());
        return true;
    }

    /** Uma vez por tick do servidor. Lista vazia (o normal): sai na hora. */
    static void aoTick(MinecraftServer servidor) {
        if (ESPERANDO.isEmpty()) return;
        long agora = servidor.getTickCount();
        List<Esperando> prontos = null;
        for (Iterator<Esperando> it = ESPERANDO.iterator(); it.hasNext(); ) {
            Esperando e = it.next();
            // Morreu, saiu ou trocou de mundo no meio: a magia some sem golpe.
            if (e.quem().isRemoved() || !e.quem().isAlive()) {
                it.remove();
                continue;
            }
            // agora < golpeEm - preparação: o relógio voltou (servidor reiniciou o contador); solta já.
            if (agora >= e.golpeEm() || agora < e.golpeEm() - RegrasTalhoRubro.PREPARACAO_TICKS) {
                it.remove();
                if (prontos == null) prontos = new ArrayList<>(2);
                prontos.add(e);
            }
        }
        if (prontos != null) for (Esperando e : prontos) golpe(e);
    }

    static void limpar() {
        ESPERANDO.clear();
    }

    private static void golpe(Esperando e) {
        LivingEntity quem = e.quem();
        if (!(quem.level() instanceof ServerLevel nivel)) return;
        double giro = Math.toRadians(quem.getYRot());
        double frenteX = -Math.sin(giro);
        double frenteZ = Math.cos(giro);
        AABB caixa = quem.getBoundingBox().inflate(RegrasTalhoRubro.ALCANCE, RegrasTalhoRubro.ALTURA_MAX, RegrasTalhoRubro.ALCANCE);
        List<LivingEntity> alvos = nivel.getEntitiesOfClass(LivingEntity.class, caixa, alvo -> alvo != quem
                && alvo.isAlive() && !alvo.isSpectator() && !quem.isAlliedTo(alvo) && !ehDoDono(quem, alvo)
                && RegrasTalhoRubro.dentroDoArco(alvo.getX() - quem.getX(), alvo.getY() - quem.getY(),
                        alvo.getZ() - quem.getZ(), frenteX, frenteZ)
                && quem.hasLineOfSight(alvo));
        alvos.sort(Comparator.comparingDouble(quem::distanceToSqr));
        int n = Math.min(alvos.size(), RegrasTalhoRubro.MAX_ALVOS);
        for (int i = 0; i < n; i++) {
            LivingEntity alvo = alvos.get(i);
            if (e.aplicar().aplicar(quem, alvo, e.dano()) && alvo.isAlive()) {
                Sangramento.aplicar(alvo, RegrasTalhoRubro.SANGRAMENTO_NIVEL, RegrasTalhoRubro.SANGRAMENTO_TICKS, quem);
            }
        }
        enviar(quem, FaseMagia.GOLPE, nivel.random.nextInt());
    }

    /** O bicho de estimação de quem conjura não leva o corte. */
    private static boolean ehDoDono(LivingEntity quem, LivingEntity alvo) {
        return alvo instanceof OwnableEntity o && quem.getUUID().equals(o.getOwnerUUID());
    }

    private static void enviar(LivingEntity quem, int fase, int semente) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(quem, new FaseMagia(IdsMagia.TALHO_RUBRO, fase, quem.getId(),
                quem.getX(), quem.getY(), quem.getZ(), quem.getYRot(), semente));
    }
}
