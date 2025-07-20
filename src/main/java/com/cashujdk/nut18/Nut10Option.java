package com.cashujdk.nut18;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

public class Nut10Option {
    public Nut10Option() {}
    public String kind;
    public String data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<TransportTag[]> tags;
}
