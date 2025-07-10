package com.cashujdk.nut10;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Nut10ProofSecret {

    @JsonProperty("nonce")
    private String nonce;

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String data;

    @JsonProperty("tags")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String[][] tags;

    public Nut10ProofSecret() {
    }

    public Nut10ProofSecret(String nonce, String data, String[][] tags) {
        this.nonce = nonce;
        this.data = data;
        this.tags = tags;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String[][] getTags() {
        return tags;
    }

    public void setTags(String[][] tags) {
        this.tags = tags;
    }
}