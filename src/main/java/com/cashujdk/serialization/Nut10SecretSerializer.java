package com.cashujdk.serialization;

import com.cashujdk.nut10.Nut10ProofSecret;
import com.cashujdk.nut10.Nut10Secret;
import com.cashujdk.nut11.P2PKProofSecret;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class Nut10SecretSerializer extends JsonSerializer<Nut10Secret> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void serialize(Nut10Secret value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {

        // Serialize as array: [key, proofSecret]
        gen.writeStartArray();
        gen.writeString(value.key);

        // Serialize proofSecret as string value
        String proofSecretJson = objectMapper.writeValueAsString(value.proofSecret);
        gen.writeString(proofSecretJson);  // ← As string!

        gen.writeEndArray();
    }
}
