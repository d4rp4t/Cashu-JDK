package com.cashujdk.nut00;

import com.cashujdk.cryptography.Cashu;
import com.cashujdk.nut12.DLEQProof;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlindSignature {
    public long amount;

    @JsonProperty("id")
    public String keysetId;

    @JsonProperty("C_")
    public String c_;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public DLEQProof dleq;

    public BlindSignature() {}
    public BlindSignature(long amount, String keysetId, String c_, DLEQProof dleq) {
        this.amount = amount;
        this.keysetId = keysetId;
        this.c_ = c_;
        this.dleq = dleq;
    }
}
