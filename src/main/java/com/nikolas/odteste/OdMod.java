package com.nikolas.odteste;

import com.mojang.logging.LogUtils;
import com.nikolas.odteste.achados.ComandosArtefato;
import com.nikolas.odteste.achados.Ronda;
import com.nikolas.odteste.aparelhos.Aparelhos;
import com.nikolas.odteste.blocos.capela.BlocosCapela;
import com.nikolas.odteste.comandos.ComandosAdmin;
import com.nikolas.odteste.comandos.ComandosHistoria;
import com.nikolas.odteste.historia.PrimeiroSinal;
import com.nikolas.odteste.historia.vilacalada.CenaEntradaVila;
import com.nikolas.odteste.historia.vilacalada.ClimaVilaCalada;
import com.nikolas.odteste.historia.vilacalada.PistasVilaCalada;
import com.nikolas.odteste.cenas.Cenas;
import com.nikolas.odteste.compat.Compat;
import com.nikolas.odteste.criaturas.Criaturas;
import com.nikolas.odteste.dados.DadosJogador;
import com.nikolas.odteste.efeitos.Sangramento;
import com.nikolas.odteste.itens.Itens;
import com.nikolas.odteste.rede.Rede;
import com.nikolas.odteste.registro.Registro;
import com.nikolas.odteste.sistemas.Vigia;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Classe principal. Só liga os sistemas: cada sistema mora na sua pasta e se registra sozinho.
 * O nome do mod ainda é provisório (ver docs/decisoes.md).
 */
@Mod(OdMod.MODID)
public final class OdMod {
    /** Vem do gradle.properties. Depois do primeiro lançamento no servidor, nunca mais muda. */
    public static final String MODID = "odteste";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OdMod(IEventBus barramento, ModContainer container) {
        DadosJogador.iniciar();
        Cenas.iniciar();
        Sangramento.iniciar();
        Itens.iniciar();
        BlocosCapela.iniciar();
        Criaturas.iniciar(barramento);
        Aparelhos.iniciar(barramento);
        Registro.registrar(barramento);
        Rede.iniciar(barramento);
        ComandosAdmin.iniciar();
        ComandosHistoria.iniciar();
        Vigia.iniciar();
        PrimeiroSinal.iniciar();
        PistasVilaCalada.iniciar();
        CenaEntradaVila.iniciar();
        ClimaVilaCalada.iniciar();
        ComandosArtefato.iniciar();
        Ronda.iniciar();
        Compat.iniciar(barramento);
        LOGGER.info("{} carregado. Integrações ligadas: {}", MODID, Compat.ligadas());
    }
}
