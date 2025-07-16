package com.cashujdk.nut12;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigInteger;
import java.util.Optional;

public class DLEQProof {

    public BigInteger s;
    public BigInteger e;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public BigInteger r; 

    public DLEQProof() {}

    public DLEQProof(BigInteger s, BigInteger e, Optional<BigInteger> r) {
        this.s = s;
        this.e = e;
        this.r = (r.isPresent()) ? r.get() : null;
    }
}
