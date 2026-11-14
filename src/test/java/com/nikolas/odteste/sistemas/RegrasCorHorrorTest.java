package com.nikolas.odteste.sistemas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RegrasCorHorrorTest {
    @Test
    void valeNoOverworldDeDiaENaoNasOutrasDimensoes() {
        assertEquals(1f, RegrasCorHorror.diaPadrao(true));
        assertEquals(0f, RegrasCorHorror.diaPadrao(false));
        assertEquals(1f, RegrasCorHorror.forca(1f, 0f, 1f), 1e-6);
        assertEquals(0f, RegrasCorHorror.forca(0f, 0f, 1f), 1e-6);
    }

    @Test
    void noiteSoNoOverworld() {
        assertEquals(0f, RegrasCorHorror.noiteAlvo(true, 6_000L));
        assertEquals(1f, RegrasCorHorror.noiteAlvo(true, 18_000L));
        assertEquals(0f, RegrasCorHorror.noiteAlvo(false, 18_000L));
    }

    @Test
    void daLugarAoCarmesim() {
        assertEquals(0f, RegrasCorHorror.forca(1f, 1f, 1f), 1e-6);
        assertEquals(0.5f, RegrasCorHorror.forca(1f, 0.5f, 1f), 1e-6);
    }

    @Test
    void efeitosFortesDiminuiMasNaoDesliga() {
        float desligado = RegrasCorHorror.forca(1f, 0f, 0f);
        assertTrue(desligado > 0.4f && desligado < 1f, "DESLIGADO diminui sem sumir: " + desligado);
    }

    @Test
    void aproximaSemPassar() {
        assertEquals(0.01f, RegrasCorHorror.aproximar(0f, 1f, 0.01f), 1e-6);
        assertEquals(1f, RegrasCorHorror.aproximar(0.995f, 1f, 0.01f), 1e-6);
        assertEquals(0f, RegrasCorHorror.aproximar(0.005f, 0f, 0.01f), 1e-6);
    }
}
