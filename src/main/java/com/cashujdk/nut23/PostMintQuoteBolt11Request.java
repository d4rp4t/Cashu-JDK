package com.cashujdk.nut23;

import java.math.BigInteger;

import com.cashujdk.nut04.PostMintQuoteRequest;

public class PostMintQuoteBolt11Request extends PostMintQuoteRequest {

    public BigInteger amount;
    public String description;

    public PostMintQuoteBolt11Request(String unit, BigInteger amount, String description) {
        this.unit = unit;
        this.amount = amount;
        this.description = description;
    }

}
