package com.cashujdk.nut21;

import java.math.BigInteger;

import com.cashujdk.nut04.PostMintQuoteResponse;

public class PostMintQuoteBolt11Response extends PostMintQuoteResponse {


    public BigInteger amount;
    public String state;
    public BigInteger expiry;

    public PostMintQuoteBolt11Response(String unit, String quote, String request, BigInteger amount, String state, BigInteger expiry) {
        this.unit = unit;
        this.quote = quote;
        this.request = request;
        this.amount = amount;
        this.state = state;
        this.expiry = expiry;
    }

}
