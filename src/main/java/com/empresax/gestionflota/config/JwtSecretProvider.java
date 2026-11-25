package com.empresax.gestionflota.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.EnumSet;

public class JwtSecretProvider {

    private final Path secretFile;

    public JwtSecretProvider(Path secretFile) {
        this.secretFile = secretFile;
    }

    public SecretKey load(String configuredSecret) {
        String secret = configuredSecret == null || configuredSecret.isBlank()
                ? loadOrCreateSecret()
                : configuredSecret;
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes.");
        }
        return new SecretKeySpec(key, "HmacSHA256");
    }

    private String loadOrCreateSecret() {
        try {
            Files.createDirectories(secretFile.getParent());
            if (Files.exists(secretFile, LinkOption.NOFOLLOW_LINKS)) {
                return readSecret();
            }

            byte[] randomKey = new byte[32];
            new SecureRandom().nextBytes(randomKey);
            String generatedSecret = Base64.getEncoder().encodeToString(randomKey);
            try {
                Files.writeString(secretFile, generatedSecret, StandardCharsets.US_ASCII,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                restrictPermissions();
                return generatedSecret;
            } catch (FileAlreadyExistsException e) {
                return readSecret();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar o guardar el secreto JWT local.", e);
        }
    }

    private String readSecret() throws IOException {
        if (Files.isSymbolicLink(secretFile) || !Files.isRegularFile(secretFile, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalStateException("El archivo local del secreto JWT no es válido.");
        }
        restrictPermissions();
        String secret = Files.readString(secretFile, StandardCharsets.US_ASCII).trim();
        if (secret.isEmpty()) {
            throw new IllegalStateException("El archivo local del secreto JWT está vacío.");
        }
        return secret;
    }

    private void restrictPermissions() throws IOException {
        if (Files.getFileAttributeView(secretFile, PosixFileAttributeView.class) != null) {
            Files.setPosixFilePermissions(secretFile,
                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }
    }
}
