package com.cashujdk.nut12;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigInteger;

public class HexBigIntegerDeserializer extends JsonDeserializer<BigInteger> {
    @Override
    public BigInteger deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String hexValue = p.getValueAsString();
        if (hexValue == null || hexValue.isEmpty()) {
            return null;
        }
        // Remove "0x" prefix if present
        if (hexValue.startsWith("0x")) {
            hexValue = hexValue.substring(2);
        }
        return new BigInteger(hexValue, 16);
    }
}
