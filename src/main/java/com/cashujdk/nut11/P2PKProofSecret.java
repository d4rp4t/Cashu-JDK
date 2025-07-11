package com.cashujdk.nut11;

import com.cashujdk.cryptography.ECC;
import com.cashujdk.nut00.BlindedMessage;
import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut00.Proof;
import com.cashujdk.nut10.Nut10ProofSecret;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

public class P2PKProofSecret extends Nut10ProofSecret {
    public static final String KEY = "P2PK";

    @JsonIgnore
    public P2PkBuilder getBuilder() {
        return P2PkBuilder.load(this);
    }

    public AllowedKeysResult getAllowedPubkeys() {
        P2PkBuilder builder = getBuilder();

        if (builder.getLockTime() != null &&
                builder.getLockTime().getEpochSecond() < Instant.now().getEpochSecond()) {

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

    public P2PKWitness generateWitness(Proof proof, List<ECPrivateKeyParameters> keys) {
        return generateWitness(proof.secret.getBytes(), keys);
    }

    public P2PKWitness generateWitness(BlindedMessage message, List<ECPrivateKeyParameters> keys) {
        ECPoint point = message.getB_();
        byte[] msg = point.getEncoded(false);
        return generateWitness(msg, keys);
    }

    public P2PKWitness generateWitness(byte[] msg, List<ECPrivateKeyParameters> keys) {
        byte[] hash = sha256Hash(msg);
        return generateWitnessHash(hash, keys);
    }

    public P2PKWitness generateWitnessHash(byte[] hash, List<ECPrivateKeyParameters> keys) {
        AllowedKeysResult allowedResult = getAllowedPubkeys();
        List<ECPoint> allowedKeys = allowedResult.keys;
        int requiredSignatures = allowedResult.requiredSignatures;

        List<String> signatures = new ArrayList<>();
        List<ECPrivateKeyParameters> availableKeys = new ArrayList<>(keys);
        int keysRequiredLeft = requiredSignatures;

        P2PkBuilder builder = getBuilder();
        ECDomainParameters domainParams = ECC.DOMAIN;

        while (keysRequiredLeft > 0 && !availableKeys.isEmpty()) {
            ECPrivateKeyParameters key = availableKeys.remove(0);

            ECPoint publicPoint = ECC.DOMAIN.getG().multiply(key.getD()).normalize();

            boolean isAllowed = allowedKeys.stream()
                    .anyMatch(allowedKey -> allowedKey.normalize().equals(publicPoint.normalize()));

            if (isAllowed) {
                try {
                    ECDSASigner signer = new ECDSASigner();
                    signer.init(true, key);
                    BigInteger[] signature = signer.generateSignature(hash);

                    String sigHex = signatureToHex(signature);
                    signatures.add(sigHex);
                    keysRequiredLeft--;
                } catch (Exception e) {
                    throw new RuntimeException("Signing failed", e);
                }
            }
        }

        if (keysRequiredLeft > 0) {
            throw new IllegalStateException("Not enough valid keys to sign");
        }

        P2PKWitness witness = new P2PKWitness();
        witness.signatures = signatures.stream().toList();
        return witness;
    }

    // Weryfikacja
    public boolean verifyWitness(String message, P2PKWitness witness) {
        byte[] hash = sha256Hash(message.getBytes(StandardCharsets.UTF_8));
        return verifyWitnessHash(hash, witness);
    }

    public boolean verifyWitness(ISecret secret, P2PKWitness witness) {
        return verifyWitness(secret.getBytes(), witness);
    }

    public boolean verifyWitness(byte[] message, P2PKWitness witness) {
        byte[] hash = sha256Hash(message);
        return verifyWitnessHash(hash, witness);
    }

    public boolean verifyWitnessHash(byte[] hash, P2PKWitness witness) {
        try {
            AllowedKeysResult allowedResult = getAllowedPubkeys();
            List<ECPoint> allowedKeys = allowedResult.keys;
            int requiredSignatures = allowedResult.requiredSignatures;

            if ((long) witness.signatures.size() < requiredSignatures) {
                return false;
            }

            P2PkBuilder builder = getBuilder();
            ECDomainParameters domainParams = ECC.DOMAIN;

            int validSignatures = 0;

            for (String sigHex : witness.signatures) {
                BigInteger[] signature = hexToSignature(sigHex);
                if (signature == null) continue;

                for (ECPoint allowedKey : allowedKeys) {
                    if (verifySignature(hash, signature, allowedKey, domainParams)) {
                        validSignatures++;
                        break;
                    }
                }
            }

            return validSignatures >= requiredSignatures;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifySignature(byte[] hash, BigInteger[] signature, ECPoint publicKey, ECDomainParameters domainParams) {
        try {
            P2PkBuilder builder = getBuilder();
            ECPublicKeyParameters pubKeyParams = new ECPublicKeyParameters(publicKey, ECC.DOMAIN);
            ECDSASigner verifier = new ECDSASigner();
            verifier.init(false, pubKeyParams);
            return verifier.verifySignature(hash, signature[0], signature[1]);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] sha256Hash(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String signatureToHex(BigInteger[] signature) {
        byte[] r = removeLeadingZeros(signature[0].toByteArray());
        byte[] s = removeLeadingZeros(signature[1].toByteArray());
        return Hex.toHexString(r) + Hex.toHexString(s);
    }

    private BigInteger[] hexToSignature(String hex) {
        try {
            if (hex.length() % 2 != 0) return null;
            int halfLength = hex.length() / 2;
            String rHex = hex.substring(0, halfLength);
            String sHex = hex.substring(halfLength);
            BigInteger r = new BigInteger(rHex, 16);
            BigInteger s = new BigInteger(sHex, 16);
            return new BigInteger[]{r, s};
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] removeLeadingZeros(byte[] bytes) {
        int start = 0;
        while (start < bytes.length && bytes[start] == 0) {
            start++;
        }
        return Arrays.copyOfRange(bytes, start, bytes.length);
    }

    public static class AllowedKeysResult {
        public final List<ECPoint> keys;
        public final int requiredSignatures;

        public AllowedKeysResult(List<ECPoint> keys, int requiredSignatures) {
            this.keys = keys != null ? keys : Collections.emptyList();
            this.requiredSignatures = requiredSignatures;
        }
    }
}
