package com.cashujdk.nut00;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//Inner token of v4 token. Has to belong to the same mint
public class InnerToken {
    public byte[] keysetId;
    public List<Proof> proofs;
}