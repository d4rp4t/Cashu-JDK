package com.cashujdk.nut02;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetKeysetsItemResponse {

    public String unit;
    @JsonProperty("id") public String keysetId;
    public Boolean active;
    @JsonProperty("input_fee_ppk") public int inputFee;

    public GetKeysetsItemResponse(String unit, String keysetId, Boolean active, int inputFee) {
        this.unit = unit;
        this.keysetId = keysetId;
        this.active = active;
        this.inputFee = inputFee;
    }
}
