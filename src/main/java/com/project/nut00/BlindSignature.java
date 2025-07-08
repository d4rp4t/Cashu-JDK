package com.project.nut00;

import com.google.gson.annotations.SerializedName;
import com.project.cryptography.Cashu;
import com.project.nut12.DLEQ;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class BlindSignature {
    public BigInteger amount;

    @SerializedName("id")
    public String keysetId;

    @SerializedName("C_")
    public String c_;

    public DLEQ dleq;

    public BlindSignature() {}
    public BlindSignature(BigInteger amount, String keysetId, String c_, DLEQ dleq) {
        this.amount = amount;
        this.keysetId = keysetId;
        this.c_ = c_;
        this.dleq = dleq;
    }
    public BlindSignature(BigInteger amount, String keysetId, ECPoint c_, DLEQ dleq) {
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
        this.c_ = this.c_ = Cashu.pointToHex(c_, true);
    }
}