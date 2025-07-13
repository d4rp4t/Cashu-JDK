package com.cashujdk.nut00;


import com.cashujdk.nut12.DLEQProof;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.bouncycastle.math.ec.ECPoint;

public class Proof {
    public long amount;

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

    public Proof(long amount, String keysetId, ISecret secret, ECPoint c, String witness, DLEQProof dleq) {
        this.amount = amount;
        this.keysetId = keysetId;
        this.secret = secret;
        this.c = c;
        this.witness = witness;
        this.dleq = dleq;
    }
}
