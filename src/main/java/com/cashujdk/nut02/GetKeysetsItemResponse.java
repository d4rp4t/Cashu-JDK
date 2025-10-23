package com.cashujdk.nut02;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public class GetKeysetsItemResponse {

    public String unit;
    @JsonProperty(value = "id", required = true)
    public String keysetId;
    public Boolean active;
    @JsonProperty(value = "input_fee_ppk", required = true)
    public int inputFee;
    @JsonProperty("final_expiry")
    public Optional<Long> finalExpiry;

    public GetKeysetsItemResponse() {}

    public GetKeysetsItemResponse(String unit, String keysetId, Boolean active, int inputFee) {
        this(unit, keysetId, active, inputFee, Optional.empty());
    }

    public GetKeysetsItemResponse(String unit, String keysetId, Boolean active, int inputFee, Optional<Long> finalExpiry) {
        this.unit = unit;
        this.keysetId = keysetId;
        this.active = active;
        this.inputFee = inputFee;
        this.finalExpiry = finalExpiry;
    }
}