package com.project.nut10;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cryptography.Cashu;
import com.project.nut00.ISecret;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.UTF8;

import java.nio.charset.StandardCharsets;

public class Nut10Secret implements ISecret {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String key;
    private Nut10ProofSecret proofSecret;

    @JsonIgnore
    private final String originalString;

    public Nut10Secret(String key, Nut10ProofSecret proofSecret) {
        this.key = key;
        this.proofSecret = proofSecret;
        this.originalString = null;
    }

    public Nut10Secret(String originalString) {
        this.originalString = originalString;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Nut10ProofSecret getProofSecret() {
        return proofSecret;
    }

    public void setProofSecret(Nut10ProofSecret proofSecret) {
        this.proofSecret = proofSecret;
    }

    public byte[] getBytes() {
        if (originalString != null) {
            return originalString.getBytes(StandardCharsets.UTF_8);
        }
        try {
            return objectMapper.writeValueAsBytes(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Nut10Secret", e);
        }
    }

    public ECPoint toCurve() {
        return Cashu.hashToCurve(getBytes());
    }
}
