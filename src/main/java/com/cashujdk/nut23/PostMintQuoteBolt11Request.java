package com.cashujdk.nut23;

import com.cashujdk.nut04.PostMintQuoteRequest;

public class PostMintQuoteBolt11Request extends PostMintQuoteRequest {

    public long amount;
    public String description;

    public PostMintQuoteBolt11Request(String unit, long amount, String description) {
        super(unit);
        this.amount = amount;
        this.description = description;
    }

}
