package com.cashujdk.nut00;

import com.cashujdk.serialization.CBORSerializer;
import com.cashujdk.serialization.CBORDeserializer;

import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Token {
    public String mint;
    public String unit;
    public String memo;
    public List<InnerToken> tokens;

    public Token() {
    }

    /**
     * Creates a Token from a list of Proofs.
     * All proofs must be from the same mint (have the same keysetId)
     *
     * @param proofs List of proofs to create the token from
     * @param unit The unit for the token (e.g., "sat" or "BTC")
     * @throws IllegalArgumentException if proofs are from different mints
     */
    public Token(List<Proof> proofs, String mintUrl) {
        if (proofs == null || proofs.isEmpty()) {
            throw new IllegalArgumentException("Proofs list cannot be null or empty");
        }
        
        String unit = proofs.get(0).keysetId;
        if (unit == null || unit.isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be null or empty");
        }

        // Group proofs by keysetId
        var groupedProofs = proofs.stream()
            .collect(Collectors.groupingBy(p -> p.keysetId));

        if (groupedProofs.size() > 1) {
            throw new IllegalArgumentException("All proofs must be from the same mint (same keysetId)");
        }

        String keysetId = proofs.get(0).keysetId;
        this.tokens = new ArrayList<>();
        this.tokens.add(new InnerToken(keysetId, proofs));
        this.unit = unit;
        this.mint = mintUrl;
    }

    public String encode() throws Exception {
        var serializer = new CBORSerializer();
        byte[] cbor = serializer.toCBOR(this);
        // We'll use standard Base64 to match the test vector format
        String base64 = Base64.getEncoder().encodeToString(cbor);
        return "cashuB" + base64;
    }

    public static Token decode(String encoded) throws Exception {
        if (!encoded.startsWith("cashuB")) throw new IllegalArgumentException("invalid token");
        String base64 = encoded.substring(6);
        
        byte[] cbor;
        try {
            // First try standard Base64 decoder
            cbor = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            try {
                // Fall back to URL-safe decoder
                cbor = Base64.getUrlDecoder().decode(base64);
            } catch (IllegalArgumentException e2) {
                throw new IllegalArgumentException("invalid base64 encoding");
            }
        }
        
        var deserializer = new CBORDeserializer();
        return deserializer.fromCBOR(cbor);
    }
}
