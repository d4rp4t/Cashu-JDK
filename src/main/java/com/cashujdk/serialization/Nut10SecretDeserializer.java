package com.cashujdk.serialization;

import com.cashujdk.nut10.Nut10Secret;
import com.cashujdk.nut10.Nut10ProofSecret;
import com.cashujdk.nut11.P2PKProofSecret;
import com.cashujdk.nut14.HTLCProofSecret;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Nut10SecretDeserializer extends JsonDeserializer<Nut10Secret> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Nut10Secret deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = p.getCodec().readTree(p);

        if (node.isTextual()) {
            return new Nut10Secret(node.asText());
        }

        if (!node.isArray()) {
            throw new IOException("Expected array [key, proofSecret]");
        }

        if (node.size() != 2) {
            throw new IOException("Expected array of size 2: [key, proofSecret]");
        }

        String key = node.get(0).asText();
        JsonNode proofSecretNode = node.get(1);

        if (proofSecretNode.isTextual()) {
            String proofSecretJson = proofSecretNode.asText();
            proofSecretNode = OBJECT_MAPPER.readTree(proofSecretJson);
        }

        Nut10ProofSecret proofSecret;
        switch (key) {
            case "P2PK":
                proofSecret = p.getCodec().treeToValue(proofSecretNode, P2PKProofSecret.class);
                break;
            case "HTLC":
                proofSecret = p.getCodec().treeToValue(proofSecretNode, HTLCProofSecret.class);
                break;
            default:
                throw new IOException("Unknown secret type: " + key);
        }

        return new Nut10Secret(key, proofSecret);
    }
}
