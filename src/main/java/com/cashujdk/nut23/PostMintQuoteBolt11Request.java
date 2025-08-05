package com.cashujdk.nut23;

import com.cashujdk.nut04.PostMintQuoteRequest;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

public class PostMintQuoteBolt11Request extends PostMintQuoteRequest {

    public long amount;
    public String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<String> pubkey; // not specified in constructor, can be added manually since its public

    public PostMintQuoteBolt11Request(String unit, long amount, String description) {
        super(unit);
        this.amount = amount;
        this.description = description;
    }

}
