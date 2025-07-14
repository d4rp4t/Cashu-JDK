package com.cashujdk.serialization;

import com.cashujdk.nut00.ISecret;
import com.cashujdk.nut00.StringSecret;
import com.cashujdk.nut10.Nut10Secret;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class SecretSerializer extends StdSerializer<ISecret> {
    SecretSerializer() {
        super(ISecret.class);
    }

    @Override
    public void serialize(ISecret secret, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(secret instanceof StringSecret stringSecret) {
            jsonGenerator.writeString(stringSecret.getSecret());
            return;
        }
        if(secret instanceof Nut10Secret nut10Secret) {
            jsonGenerator.writeStartArray();
            jsonGenerator.writeString(nut10Secret.key);
            jsonGenerator.writeObject(nut10Secret.proofSecret);
            jsonGenerator.writeEndArray();
            return;
        }
        throw new JsonParseException("Uknown spending condition secret type");
    }
}
