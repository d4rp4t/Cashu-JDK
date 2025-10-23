package com.cashujdk.nut01;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeysetItemResponse {
    @JsonProperty(required = true)
    public String id;
    @JsonProperty(required = true)
    public String unit;
    @JsonProperty("final_expiry")
    public Optional<Long> finalExpiry = Optional.empty();
    @JsonProperty(required = true)
    public Map<BigInteger, String> keys;

    public KeysetItemResponse() {}

    public KeysetItemResponse(String id, String unit, Map<BigInteger, String> keys, Optional<Long> finalExpiry) {
        this.id = id;
        this.unit = unit;
        this.keys = keys;
        this.finalExpiry = finalExpiry;
    }

    public KeysetItemResponse(String id, String unit, Map<BigInteger, String> keys) {
        this(id, unit, keys, Optional.empty());
    }

    
}
