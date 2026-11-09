package com.nikolas.odteste.aparelhos;

import net.neoforged.neoforge.energy.EnergyStorage;

/**
 * Bateria FE dos nossos aparelhos. Por fora só <b>recebe</b> (cabo, gerador ou bateria de qualquer mod);
 * ninguém tira energia dela. O gasto é por dentro, com {@link #gastar(int)}.
 */
public class BateriaAparelho extends EnergyStorage {
    private final Runnable aoMudar;

    public BateriaAparelho(int capacidade, int entradaMaxima, Runnable aoMudar) {
        super(capacidade, entradaMaxima, 0);
        this.aoMudar = aoMudar;
    }

    @Override
    public int receiveEnergy(int quanto, boolean simular) {
        int recebido = super.receiveEnergy(quanto, simular);
        if (recebido > 0 && !simular) aoMudar.run();
        return recebido;
    }

    /** Gasta por dentro; nunca fica negativo. */
    public void gastar(int quanto) {
        int antes = energy;
        energy = RegrasAnalisador.gastar(energy, quanto);
        if (energy != antes) aoMudar.run();
    }

    /** Ao carregar do disco: valor fora da faixa é corrigido, não crasha. */
    public void definir(int valor) {
        energy = Math.max(0, Math.min(capacity, valor));
    }
}
