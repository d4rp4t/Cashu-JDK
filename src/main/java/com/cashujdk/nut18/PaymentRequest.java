package com.cashujdk.nut18;

import com.cashujdk.serialization.CBORDeserializer;
import com.cashujdk.serialization.CBORSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Base64;
import java.util.Optional;

public class PaymentRequest {
    public PaymentRequest() {
        this.id = Optional.empty();
        this.amount = Optional.empty();
        this.unit = Optional.empty();
        this.singleUse = Optional.empty();
        this.mints = Optional.empty();
        this.description = Optional.empty();
        this.transport = Optional.empty();
        this.nut10Option = Optional.empty();
    }
    @JsonProperty("i")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<String> id;
    @JsonProperty("a")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<Long> amount;
    @JsonProperty("u")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<String> unit;
    @JsonProperty("s")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<Boolean> singleUse;
    @JsonProperty("m")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<String[]> mints;
    @JsonProperty("d")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<String> description;
    @JsonProperty("t")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<Transport[]> transport;
    @JsonProperty("nut10")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Optional<Nut10Option> nut10Option;

    public String encode() throws Exception {
        var serializer = new CBORSerializer();
        byte[] cbor = serializer.toCBOR(this);
        // Use URL-safe Base64 encoding without padding
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(cbor);
        return "creqA" + base64;
    }

    public static PaymentRequest decode(String encoded){
        if (!encoded.startsWith("creqA")) {
            throw new IllegalArgumentException("invalid token");
        }
        String base64 = encoded.substring(5);

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
        return deserializer.prFromCBOR(cbor);
    }
}
