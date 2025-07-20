package com.cashujdk.nut18;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class PaymentRequest {
    public PaymentRequest() {}
    @JsonProperty("i")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<Integer> id;
    @JsonProperty("a")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<Long> amount;
    @JsonProperty("u")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<String> unit;
    @JsonProperty("s")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<Boolean> singleUse;
    @JsonProperty("m")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<String[]> mints;
    @JsonProperty("d")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<String> description;
    @JsonProperty("t")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<Transport> transport;
    @JsonProperty("nut10")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<Nut10Option> nut10Option;
}
