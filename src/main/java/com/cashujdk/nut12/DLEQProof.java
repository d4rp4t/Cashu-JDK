package com.cashujdk.nut12;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigInteger;
import java.util.Optional;

public class DLEQProof {

    @JsonDeserialize(using = HexBigIntegerDeserializer.class)
    public BigInteger s;

    @JsonDeserialize(using = HexBigIntegerDeserializer.class)
    public BigInteger e;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = HexBigIntegerDeserializer.class)
    public BigInteger r; 

    public DLEQProof() {}

    public DLEQProof(BigInteger s, BigInteger e, Optional<BigInteger> r) {
        this.s = s;
        this.e = e;
        this.r = (r.isPresent()) ? r.get() : null;
    }
}
