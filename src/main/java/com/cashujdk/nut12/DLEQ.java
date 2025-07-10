package com.cashujdk.nut12;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

public class DLEQ {
    public DLEQ(BigInteger s, BigInteger e) {
        this.s = s;
        this.e = e;
    }

    @JsonProperty("e")
    public BigInteger e;
    @JsonProperty("s")
    public BigInteger s;
}
