package com.project.nut12;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

public class DLEQProof extends DLEQ {

    public DLEQProof(BigInteger s, BigInteger e, BigInteger r) {
        super(s, e);
        this.r = r;
    }

    @JsonProperty("r")
    public BigInteger r;
}