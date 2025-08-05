package com.cashujdk.nut06.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MintBlindAuthSetting {

    public MintBlindAuthSetting() {}

    @JsonProperty("bat_max_mint")
    public int batMaxMint;

    @JsonProperty("protected_endpoints")
    public Endpoint[] protectedEndpoints;

}
