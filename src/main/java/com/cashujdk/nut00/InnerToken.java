package com.cashujdk.nut00;


import java.util.List;

//Inner token of v4 token. Has to belong to the same mint
public class InnerToken {
    public String keysetId;
    public List<Proof> proofs;

    public InnerToken(String keysetId, List<Proof> proofs) {
        this.keysetId = keysetId;
        this.proofs = proofs;
    }

    public InnerToken() {
    }
}
