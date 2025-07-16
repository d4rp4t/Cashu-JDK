package com.cashujdk.nut00;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import com.cashujdk.cryptography.Cashu;

import java.security.SecureRandom;

public class StringSecret implements ISecret {

    private String secret;

    public String getSecret() {
        return this.secret;
    }

    public byte[] getBytes() {
        try {
            // First try decoding as hex
            return Hex.decode(secret);
        } catch (Exception e) {
            // If not hex, treat as regular string (for backwards compatibility)
            return secret.getBytes();
        }
    }

    public ECPoint toCurve() {
        return Cashu.hashToCurve(getBytes());
    }

    public static StringSecret random() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return new StringSecret(Hex.toHexString(randomBytes));
    }

    public StringSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String toString() {
        return getSecret();
    }
}
