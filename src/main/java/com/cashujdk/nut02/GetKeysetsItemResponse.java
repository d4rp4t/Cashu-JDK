package com.cashujdk.nut02;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetKeysetsItemResponse {

    public String unit;
    @JsonProperty("id") public String keysetId;
    public Boolean active;
    @JsonProperty("input_fee_ppk") public int inputFee;
}
