package com.project.nut00;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.List;

//Inner token of v4 token. Has to belong to the same mint
public class InnerToken {
    @JsonProperty("i")
    public byte[] keysetId;
    @JsonProperty("p")
    public List<Proof> proofs;
}