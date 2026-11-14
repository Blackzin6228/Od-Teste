package com.nikolas.odteste.cenas;

import com.nikolas.odteste.OdMod;
import com.nikolas.odteste.historia.mestre.ConfigMestre;
import com.nikolas.odteste.historia.mestre.Mestre;
import com.nikolas.odteste.rede.PararCena;
import com.nikolas.odteste.rede.TocarCena;
import com.nikolas.odteste.rede.TocarCinema;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * API das cenas para os eventos da história (e para o comando do mestre). Toca qualquer cena: as em JSON
 * ({@link Roteiros}) e as escritas em código ({@link CenasConhecidas}); se as duas tiverem o mesmo nome, vale a JSON.
 *
 * <pre>{@code
 * Cinema.tocar(jogador, "chegada_vila_calada");          // um jogador
 * Cinema.tocar(grupo, "chegada_vila_calada", null);      // vários (mestre ouvindo vê uma vez só)
 * Cinema.parar(jogador);
 * }</pre>
 *
 * Só manda para os alvos (e para o mestre em espectador com o ouvir ligado: quem está com a câmera presa num alvo,
 * quem está a até {@link #RAIO_MESTRE} blocos dele e quem digitou o comando). O alvo fica parado e protegido no
 * servidor enquanto a cena toca ({@link ProtecaoCena}); o mestre não.
 */
public final class Cinema {
    /** Mestre ouvindo até essa distância de um alvo também vê a cena. */
    public static final double RAIO_MESTRE = 48;
    /** Lugar mais longe do alvo do que isso não vale (o cliente do alvo não teria o terreno carregado). */
    public static final double LUGAR_MAXIMO = 96;

    public enum Resultado {
        /** Tocou como a cena pede. */
        TOCOU,
        /** Tocou, mas o lugar da cena não foi achado (ou está longe): tocou onde o alvo está. */
        SEM_LUGAR,
        /** Não existe cena com esse nome. */
        DESCONHECIDA,
        /** O alvo está em espectador ou morto: não tocou. */
        NAO_PODE
    }

    private static final Map<String, Function<ServerPlayer, Optional<RegrasRoteiro.Ancora>>> LUGARES = new HashMap<>();

    private Cinema() {}

    /** Quem sabe achar um lugar das cenas (ex.: a Vila Calada sabe onde está o sino rachado). */
    public static void definirLugar(String nome, Function<ServerPlayer, Optional<RegrasRoteiro.Ancora>> lugar) {
        LUGARES.put(nome, lugar);
    }

    public static boolean existe(String cena) {
        return Roteiros.de(cena).isPresent() || CenasConhecidas.de(cena).isPresent();
    }

    /** Nomes de todas as cenas (para sugestão do comando). */
    public static Set<String> ids() {
        Set<String> r = new TreeSet<>(Roteiros.ids());
        r.addAll(CenasConhecidas.todas().keySet());
        return r;
    }

    public static Resultado tocar(ServerPlayer alvo, String cena) {
        return tocar(alvo, cena, null, new HashSet<>());
    }

    /** Toca para vários; o mestre que assiste vê uma vez só. {@code quemMandou} pode ser nulo. */
    public static Map<Resultado, Integer> tocar(Collection<ServerPlayer> alvos, String cena, ServerPlayer quemMandou) {
        Map<Resultado, Integer> r = new HashMap<>();
        Set<UUID> jaViram = new HashSet<>();
        for (ServerPlayer p : alvos) jaViram.add(p.getUUID()); // alvo nunca recebe a cópia de espectador
        for (ServerPlayer p : alvos) r.merge(tocar(p, cena, quemMandou, jaViram), 1, Integer::sum);
        return r;
    }

    /** Para a cena deste jogador (as duas, a de código e a em JSON) e solta ele. */
    public static void parar(ServerPlayer p) {
        ProtecaoCena.soltar(p);
        PacketDistributor.sendToPlayer(p, PararCena.INSTANCIA);
    }

    private static Resultado tocar(ServerPlayer alvo, String cena, ServerPlayer quemMandou, Set<UUID> jaViram) {
        if (alvo.isSpectator() || alvo.isDeadOrDying()) return Resultado.NAO_PODE;
        Optional<Roteiros.Carregado> json = Roteiros.de(cena);
        if (json.isPresent()) return tocarJson(alvo, json.get(), quemMandou, jaViram);
        Optional<Cena> codigo = CenasConhecidas.de(cena);
        if (codigo.isEmpty()) return Resultado.DESCONHECIDA;
        Optional<Cenas.Ancora> a = Cenas.lugar(cena, alvo);
        Cenas.Ancora ancora = a.orElseGet(() -> new Cenas.Ancora(alvo.getX(), alvo.getY(), alvo.getZ(), alvo.getYRot()));
        Cenas.tocar(alvo, cena, ancora);
        for (ServerPlayer m : mestres(alvo, quemMandou, jaViram)) {
            PacketDistributor.sendToPlayer(m, new TocarCena(cena, ancora.x(), ancora.y(), ancora.z(), ancora.giro(), true));
        }
        return a.isPresent() ? Resultado.TOCOU : Resultado.SEM_LUGAR;
    }

    private static Resultado tocarJson(ServerPlayer alvo, Roteiros.Carregado c, ServerPlayer quemMandou,
            Set<UUID> jaViram) {
        Roteiro r = c.roteiro();
        Resultado res = Resultado.TOCOU;
        RegrasRoteiro.Ancora a = null;
        if (!IdsCena.LUGAR_JOGADOR.equals(r.ancora())) {
            var lugar = LUGARES.get(r.ancora());
            a = lugar == null ? null : lugar.apply(alvo).orElse(null);
            if (a != null && Math.hypot(a.x() - alvo.getX(), a.z() - alvo.getZ()) > LUGAR_MAXIMO) a = null;
            if (a == null) res = Resultado.SEM_LUGAR;
        }
        if (a == null) a = new RegrasRoteiro.Ancora(alvo.getX(), alvo.getY(), alvo.getZ(), alvo.getYRot());
        Vec3 olhos = alvo.getEyePosition();
        boolean pular = RegrasRoteiro.podePular(r.pular(), ConfigMestre.pularCenas(), false);
        TocarCinema m = new TocarCinema(r.id(), c.json(), a.x(), a.y(), a.z(), a.giro(), olhos.x, olhos.y, olhos.z,
                alvo.getYRot(), alvo.getXRot(), pular, false);
        Cenas.marcarVista(alvo, r.id());
        ProtecaoCena.prender(alvo, r.id(), RegrasRoteiro.ticksProtegido(r.duracao()));
        PacketDistributor.sendToPlayer(alvo, m);
        TocarCinema copia = new TocarCinema(m.cena(), m.json(), m.ax(), m.ay(), m.az(), m.giro(), m.ox(), m.oy(),
                m.oz(), m.oGiro(), m.oInclinacao(), true, true);
        for (ServerPlayer mestre : mestres(alvo, quemMandou, jaViram)) PacketDistributor.sendToPlayer(mestre, copia);
        OdMod.LOGGER.info("Cena {} para {}{}", r.id(), alvo.getGameProfile().getName(),
                res == Resultado.SEM_LUGAR ? " (sem o lugar " + r.ancora() + ": no jogador)" : "");
        return res;
    }

    /** Mestres ouvindo que ainda não viram: câmera presa no alvo, perto dele, ou quem digitou. */
    private static Set<ServerPlayer> mestres(ServerPlayer alvo, ServerPlayer quemMandou, Set<UUID> jaViram) {
        Set<ServerPlayer> r = new LinkedHashSet<>(Mestre.assistindo(alvo));
        r.addAll(Mestre.ouvintesPerto(alvo.serverLevel(), alvo.position(), RAIO_MESTRE, alvo));
        if (quemMandou != null && Mestre.ouvindo(quemMandou)) r.add(quemMandou);
        r.removeIf(m -> m == alvo || !jaViram.add(m.getUUID()));
        return r;
    }
}
