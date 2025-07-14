package com.cashujdk.serialization;

import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut10.Nut10Secret;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class SecretDeserializer extends StdDeserializer<ISecret> {

    public SecretDeserializer() {
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
