package com.project.nut06;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class GetInfoResponse {
    private String name;

    private String pubkey;

    private String version;

    private String description;

    @JsonProperty("description_long")
    private String descriptionLong;

    @JsonProperty("contact")
    private List<ContactInfo> contact;

    private String motd;

    @JsonProperty("icon_url")
    private String iconUrl;

    private String[] urls;

    @JsonProperty("time")
    private OffsetDateTime time;

    @JsonProperty("tos_url")
    private String tosUrl;

    @JsonProperty("nuts")
    private Map<String, Object> nuts;
}

//todo: add certain nuts settings here