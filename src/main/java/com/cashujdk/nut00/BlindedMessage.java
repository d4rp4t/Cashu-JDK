package com.cashujdk.nut00;


import com.cashujdk.cryptography.Cashu;
import com.google.gson.annotations.SerializedName;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class BlindedMessage {
    // there's no ULong in java, so we have to use BigInteger which is a lot slower
    public BigInteger amount;

    //keyset id
    @SerializedName("id")
    public String keysetId;

    @SerializedName("B_")
    public String b_;


    public BlindedMessage() {}

    public BlindedMessage(BigInteger amount, String keysetId, String b_, String witness) {
        this.amount = amount;
        this.b_ = b_;
        this.keysetId = keysetId;
    }
    public BlindedMessage(BigInteger amount, String keysetId, ECPoint b_) {
        this.amount = amount;
        this.keysetId = keysetId;
        setB_(b_);
    }
    //getter for B_ as ECPoint
    public ECPoint getB_() {
       return Cashu.hexToPoint(b_);
    }
    public void setB_(ECPoint b_) {
        this.b_ = Cashu.pointToHex(b_, true);
    }
}