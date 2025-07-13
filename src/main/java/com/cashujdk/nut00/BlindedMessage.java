package com.cashujdk.nut00;


import com.cashujdk.cryptography.Cashu;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bouncycastle.math.ec.ECPoint;


public class BlindedMessage {
    public long amount;

    @JsonProperty("id")
    public String keysetId;

    @JsonProperty("B_")
    public String b_;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String witness;

    public BlindedMessage() {}

    public BlindedMessage(long amount, String keysetId, String b_, String witness) {
        this.amount = amount;
        this.b_ = b_;
        this.keysetId = keysetId;
        this.witness = witness;
    }
    public BlindedMessage(long amount, String keysetId, ECPoint b_, String witness) {
        this.amount = amount;
        this.keysetId = keysetId;
        setB_(b_);
        this.witness = witness;
    }
    //getter for B_ as ECPoint
    public ECPoint getB_() {
       return Cashu.hexToPoint(b_);
    }
    public void setB_(ECPoint b_) {
        this.b_ = Cashu.pointToHex(b_, true);
    }
}
