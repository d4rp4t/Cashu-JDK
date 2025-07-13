package com.cashujdk.nut23;

import com.cashujdk.nut04.PostMintQuoteResponse;

public class PostMintQuoteBolt11Response extends PostMintQuoteResponse {

    public long amount;
    public String state;
    public long expiry;

    public PostMintQuoteBolt11Response(String unit, String quote, String request, long amount, String state, long expiry) {
        super(quote, request, unit);
        this.amount = amount;
        this.state = state;
        this.expiry = expiry;
    }

}
