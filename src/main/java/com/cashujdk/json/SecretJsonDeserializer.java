package com.cashujdk.json;

import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut10.Nut10ProofSecret;
import com.cashujdk.nut10.Nut10Secret;
import com.cashujdk.nut11.P2PKProofSecret;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class SecretJsonDeserializer extends StdDeserializer<ISecret> {

    public SecretJsonDeserializer() {
        super(ISecret.class);
    }

    @Override
    public ISecret deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        ObjectMapper mapper = (ObjectMapper) p.getCodec();

        // Case: Array -> Nut10Secret
        if (token == JsonToken.START_ARRAY && p.getParsingContext().inRoot()) {
            return mapper.readValue(p, Nut10Secret.class);
        }

        // Case: String
        if (token == JsonToken.VALUE_STRING) {
            String str = p.getText();
            if (str == null || str.isEmpty()) {
                throw new JsonParseException(p, "Secret was not nut10 or a (not empty) string");
            }

            try {
                return mapper.readValue(str, Nut10Secret.class);
            } catch (Exception e) {
                return new StringSecret(str);
            }
        }

        throw new JsonParseException(p, "Expected array or string");
    }
}

class Nut10SecretDeserializer extends StdDeserializer<Nut10Secret> {

    protected Nut10SecretDeserializer() {
        super(Nut10Secret.class);
    }

    @Override
    public Nut10Secret deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
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
        Nut10ProofSecret proofSecret;

        switch(key){
            case P2PKProofSecret.KEY:
                proofSecret = jsonParser.readValueAs(P2PKProofSecret.class);
                break;
            //TODO: ADD HTLC
            default:
                throw new JsonParseException(jsonParser, "Uknown spending conditoin proof");
        }
        jsonParser.nextToken();
        if(jsonParser.getCurrentToken() != JsonToken.END_ARRAY) {
            throw new JsonParseException(jsonParser, "Expected end array");
        }
        return new Nut10Secret(key, proofSecret);
    }
}
