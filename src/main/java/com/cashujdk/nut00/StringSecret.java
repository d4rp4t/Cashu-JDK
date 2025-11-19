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
        return secret.getBytes();
    }

    public ECPoint hashToCurve() {
        return Cashu.messageToCurve(secret);
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
