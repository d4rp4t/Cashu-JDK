package com.cashujdk.serialization;

import com.cashujdk.nut10.Nut10ProofSecret;
import com.cashujdk.nut10.Nut10Secret;
import com.cashujdk.nut11.P2PKProofSecret;
import com.cashujdk.nut14.HTLCProofSecret;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class Nut10SecretDeserializer extends StdDeserializer<Nut10Secret> {

    protected Nut10SecretDeserializer() {
        super(Nut10Secret.class);
    }

    @Override
    public Nut10Secret deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        // [
        if(jsonParser.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        if(jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
            return null;
        }
        jsonParser.nextToken();

        //"P2PK"
        String key = jsonParser.getValueAsString();
        if(key == null || key.isEmpty()) {
            return null;
        }
        Nut10ProofSecret proofSecret = switch (key) {
            case P2PKProofSecret.KEY -> jsonParser.readValueAs(P2PKProofSecret.class);
            case HTLCProofSecret.KEY -> jsonParser.readValueAs(HTLCProofSecret.class);
            default -> throw new JsonParseException(jsonParser, "Uknown spending conditoin proof");
        };

        jsonParser.nextToken();
        if(jsonParser.getCurrentToken() != JsonToken.END_ARRAY) {
            throw new JsonParseException(jsonParser, "Expected end array");
        }
        return new Nut10Secret(key, proofSecret);
    }
}
