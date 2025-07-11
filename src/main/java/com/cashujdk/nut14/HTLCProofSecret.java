package com.cashujdk.nut14;

import com.cashujdk.nut11.P2PKProofSecret;
import com.cashujdk.nut11.P2PKWitness;
import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut00.Proof;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class HTLCProofSecret extends P2PKProofSecret {
    public static final String KEY = "HTLC";

    public HTLCProofSecret(String nonce, String data, String[][] tags ) {
        super(nonce, data, tags);
    }

    @JsonIgnore
    public HTLCBuilder getBuilder() {
        return HTLCBuilder.load(this);
    }

    public AllowedKeysResult getAllowedPubkeys() {
        HTLCBuilder builder = getBuilder();
        if (builder.getLockTime() != null && builder.getLockTime().getEpochSecond() < Instant.now().getEpochSecond()) {
            int requiredSignatures = Math.min(
                    builder.getRefundPubkeys() != null ? builder.getRefundPubkeys().size() : 0,
                    1
            );
            List<ECPoint> refundKeys = builder.getRefundPubkeys() != null ?
                    builder.getRefundPubkeys() : Collections.emptyList();
            return new AllowedKeysResult(refundKeys, requiredSignatures);
        }
        return new AllowedKeysResult(builder.getPubkeys(), builder.getSignatureThreshold());
    }

    public HTLCWitness generateWitness(Proof proof, List<ECPrivateKeyParameters> keys, String preimage) {
        return generateWitness(proof.secret.getBytes(), keys, preimage.getBytes(StandardCharsets.UTF_8));
    }

    public HTLCWitness generateWitness(BlindedMessage blindedMessage, List<ECPrivateKeyParameters> keys, String preimage) {
        ECPoint point = blindedMessage.getB_();
        byte[] msg = point.getEncoded(false);
        return generateWitness(msg, keys, preimage.getBytes(StandardCharsets.UTF_8));
    }

    public HTLCWitness generateWitness(byte[] msg, List<ECPrivateKeyParameters> keys, byte[] preimage) {
        byte[] hash = sha256Hash(msg);
        return generateWitnessFromHash(hash, keys, preimage);
    }
    public HTLCWitness generateWitnessFromHash(byte[] hash, List<ECPrivateKeyParameters> keys, byte[] preimage) {
        if (!verifyPreimage(new String(preimage, StandardCharsets.UTF_8))) {
            throw new IllegalStateException("Invalid preimage");
        }
        P2PKWitness p2pkWitness = super.generateWitnessHash(hash, keys);
        HTLCWitness witness = new HTLCWitness();
        witness.signatures = p2pkWitness.signatures;
        witness.preimage = new String(preimage, StandardCharsets.UTF_8);
        return witness;
    }

    public boolean verifyPreimage(String preimage) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(preimage.getBytes(StandardCharsets.UTF_8));

            return getBuilder().getHashLock().getEncoded(true).equals(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public boolean verifyWitness(String message, HTLCWitness witness) {
        byte[] hash = sha256Hash(message.getBytes(StandardCharsets.UTF_8));
        return verifyWitnessHash(hash, witness);
    }

    public boolean verifyWitness(ISecret secret, HTLCWitness witness) {
        return verifyWitness(secret.getBytes(), witness);
    }

    @Override
    public P2PKWitness generateWitness(Proof proof, List<ECPrivateKeyParameters> keys) {
        throw new UnsupportedOperationException("Use generateWitness(Proof proof, List<ECPrivateKeyParameters> keys, String preimage)");
    }

    @Override
    public P2PKWitness generateWitness(BlindedMessage message, List<ECPrivateKeyParameters> keys) {
        throw new UnsupportedOperationException("Use generateWitness(BlindedMessage message, List<ECPrivateKeyParameters> keys, String preimage)");
    }

    @Override
    public P2PKWitness generateWitness(byte[] msg, List<ECPrivateKeyParameters> keys) {
        throw new UnsupportedOperationException("Use generateWitness(byte[] msg, List<ECPrivateKeyParameters> keys, byte[] preimage)");
    }

    @Override
    public boolean verifyWitness(String message, P2PKWitness witness) {
        return super.verifyWitness(message, witness);
    }

    @Override
    public boolean verifyWitness(ISecret secret, P2PKWitness witness) {
        return super.verifyWitness(secret, witness);
    }

    @Override
    public boolean verifyWitness(byte[] message, P2PKWitness witness) {
        return super.verifyWitness(message, witness);
    }

    @Override
    public boolean verifyWitnessHash(byte[] hash, P2PKWitness witness) {
        if (!(witness instanceof HTLCWitness)) {
            return false;
        }
        return super.verifyWitnessHash(hash, witness);
    }

    private byte[] sha256Hash(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
