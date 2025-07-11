package com.cashujdk.nut10;

import com.cashujdk.cryptography.Cashu;
import com.cashujdk.nut00.ISecret;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.UTF8;

import java.nio.charset.StandardCharsets;

public class Nut10Secret implements ISecret {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String key;
    public Nut10ProofSecret proofSecret;

    @JsonIgnore
    private String originalString;

    public Nut10Secret(String key, Nut10ProofSecret proofSecret) {
        this.key = key;
        this.proofSecret = proofSecret;
    }

    public Nut10Secret(String originalString) {
        this.originalString = originalString;
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
