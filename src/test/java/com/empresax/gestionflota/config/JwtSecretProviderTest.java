package com.empresax.gestionflota.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JwtSecretProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void generaYReutilizaUnSecretoLocal() {
        Path secretFile = tempDir.resolve("jwt-secret");
        SecretKey firstKey = new JwtSecretProvider(secretFile).load("");
        SecretKey keyAfterRestart = new JwtSecretProvider(secretFile).load(null);

        assertTrue(firstKey.getEncoded().length >= 32);
        assertArrayEquals(firstKey.getEncoded(), keyAfterRestart.getEncoded());
        assertTrue(Files.exists(secretFile));
    }

    @Test
    void priorizaElSecretoConfiguradoSinEscribirloEnDisco() {
        Path secretFile = tempDir.resolve("jwt-secret");
        String configuredSecret = "12345678901234567890123456789012";

        SecretKey key = new JwtSecretProvider(secretFile).load(configuredSecret);

        assertEquals(configuredSecret, new String(key.getEncoded(), java.nio.charset.StandardCharsets.UTF_8));
        assertFalse(Files.exists(secretFile));
    }

    @Test
    void rechazaUnSecretoConfiguradoDemasiadoCorto() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtSecretProvider(tempDir.resolve("jwt-secret")).load("corto"));
    }
}
