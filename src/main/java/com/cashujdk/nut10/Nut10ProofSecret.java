package com.cashujdk.nut10;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class Nut10ProofSecret {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public byte[] toBytes() {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(this);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ProofSecret", e);
        }
    }

    public String toJsonString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ProofSecret", e);
        }
    }

    public Optional<String[]> findTag(String tagName) {
        if (tags == null) {
            return Optional.empty();
        }
        return Arrays.stream(tags)
                .filter(tag -> tag != null && tag.length > 0 && tagName.equals(tag[0]))
                .findFirst();
    }

    public Optional<String> getTagValue(String tagName) {
        return findTag(tagName)
                .filter(tag -> tag.length > 1)
                .map(tag -> tag[1]);
    }

    public boolean hasTag(String tagName) {
        return findTag(tagName).isPresent();
    }

    public boolean isValid() {
        return nonce != null && !nonce.isEmpty();
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
