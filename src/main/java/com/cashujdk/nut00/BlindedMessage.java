package com.cashujdk.nut00;


import com.cashujdk.cryptography.Cashu;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class BlindedMessage {
    // there's no ULong in java, so we have to use BigInteger which is a lot slower
    public BigInteger amount;

    //keyset id
    @JsonProperty("id")
    public String keysetId;

    @JsonProperty("B_")
    public String b_;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String witness;

    public BlindedMessage() {}

    public BlindedMessage(BigInteger amount, String keysetId, String b_, String witness) {
        this.amount = amount;
        this.b_ = b_;
        this.keysetId = keysetId;
        this.witness = witness;
    }
    public BlindedMessage(BigInteger amount, String keysetId, ECPoint b_, String witness) {
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