package com.cashujdk.nut00;


import com.cashujdk.nut12.DLEQProof;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class Proof {
    public BigInteger amount;

    @JsonProperty("id")
    public String keysetId;

    @JsonProperty("secret")
    public ISecret secret;

    @JsonProperty("C")
    public ECPoint c;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String witness;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public DLEQProof dleq;
}