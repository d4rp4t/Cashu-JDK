package com.project.nut00;

import com.project.cryptography.Cashu;
import org.bouncycastle.math.ec.ECPoint;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class StringSecret implements ISecret {
    private final String secret;

    public StringSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

    public byte[] getBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public ECPoint toCurve() {
        return Cashu.hashToCurve(secret.getBytes());
    }

    public static StringSecret random(){
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        StringBuilder hexString = new StringBuilder(64);
        for (byte b : randomBytes) {
            hexString.append(String.format("%02x", b));
        }
        return new StringSecret(hexString.toString());
    }

}