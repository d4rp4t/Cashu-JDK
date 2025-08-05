package com.cashujdk.nut23;

import com.cashujdk.nut04.PostMintQuoteResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

public class PostMintQuoteBolt11Response extends PostMintQuoteResponse {

    public long amount;
    public String state;
    public long expiry;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Optional<String> pubkey;

    public PostMintQuoteBolt11Response(String unit, String quote, String request, long amount, String state, long expiry) {
        super(quote, request, unit);
        this.amount = amount;
        this.state = state;
        this.expiry = expiry;
    }


}
