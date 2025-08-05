package com.cashujdk.nut06.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class Nut19Setting {
    public Optional<Long> ttl;
    @JsonProperty("cached_endpoints")
    public Endpoint[] cachedEndpoints;
}
