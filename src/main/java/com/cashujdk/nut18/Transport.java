package com.cashujdk.nut18;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class Transport {
    public Transport() {}

    @JsonProperty("t")
    public String type;

    @JsonProperty("a")
    public String target;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("g")
    public Optional<TransportTag[]> tags;
}
