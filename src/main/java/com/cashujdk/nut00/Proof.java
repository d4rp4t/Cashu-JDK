package com.cashujdk.nut00;


import com.cashujdk.nut12.DLEQProof;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bouncycastle.math.ec.ECPoint;

public class Proof {
    public int amount;

    @JsonProperty("id")
    public String keysetId;

    @JsonProperty("s")
    public ISecret secret;
    @JsonProperty("C")
    public ECPoint c;

    @JsonProperty("d")
    public DLEQProof dleq;
    @JsonProperty("w")
    public String witness;
    //todo: verify

}