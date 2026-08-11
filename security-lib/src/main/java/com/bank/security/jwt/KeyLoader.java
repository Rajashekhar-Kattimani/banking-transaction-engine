package com.bank.security.jwt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class KeyLoader {

    private static final String PRIVATE_KEY_PATH = "keys/private_key.pem";
    private static final String PUBLIC_KEY_PATH = "keys/public_key.pem";

    public PrivateKey loadPrivateKey() {

        try {
            String key = readPem(PRIVATE_KEY_PATH)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA")
                    .generatePrivate(spec);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to load private key",
                    ex);
        }
    }

    public PublicKey loadPublicKey() {

        try {

            String key = readPem(PUBLIC_KEY_PATH)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA")
                    .generatePublic(spec);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to load public key",
                    ex);
        }
    }

    private String readPem(String location) throws Exception {

        ClassPathResource resource =
                new ClassPathResource(location);

        try (InputStream input = resource.getInputStream()) {

            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8);
        }
    }

}