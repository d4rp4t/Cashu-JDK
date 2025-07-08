package com.project.nut21;

import com.project.nut04.PostMintQuoteRequest;

import java.math.BigInteger;

public class PostMintQuoteBolt11Request extends PostMintQuoteRequest {
    public PostMintQuoteBolt11Request(String unit, BigInteger amount, String description) {
        this.unit = unit;
        this.amount = amount;
        this.description = description;
    }
    public BigInteger amount;
    public String description;
}
