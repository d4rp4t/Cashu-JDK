package com.cashujdk.nut00;

import com.cashujdk.cryptography.Cashu;
import com.cashujdk.nut12.DLEQProof;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bouncycastle.math.ec.ECPoint;


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
    public BlindSignature(long amount, String keysetId, ECPoint c_, DLEQProof dleq) {
        this.amount = amount;
        this.keysetId = keysetId;
        setC_(c_);
        this.dleq = dleq;
    }

    //getter for C_ as ECPoint
    public ECPoint getC_() {
        return Cashu.hexToPoint(c_);
    }
    public void setC_(ECPoint c_) {
        this.c_ = Cashu.pointToHex(c_, true);
    }
}
