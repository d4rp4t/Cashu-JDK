package com.cashujdk.nut06;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class GetInfoResponse {
    public String name;

    public String pubkey;

    public String version;

    public String description;

    @JsonProperty("description_long")
    public String descriptionLong;

    @JsonProperty("contact")
     List<ContactInfo> contact;

    public String motd;

    @JsonProperty("icon_url")
    public String iconUrl;

    public String[] urls;

    @JsonProperty("time")
    public OffsetDateTime time;

    @JsonProperty("tos_url")
    public String tosUrl;

    @JsonProperty("nuts")
    public Map<String, Object> nuts;
}
