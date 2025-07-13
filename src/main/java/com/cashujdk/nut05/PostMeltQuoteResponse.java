package com.cashujdk.nut05;

import java.math.BigInteger;

public class PostMeltQuoteResponse {
    public String quote;
    public BigInteger amount;
    public String unit;
    public String state;
    public BigInteger expiry;

    public PostMeltQuoteResponse(String quote, BigInteger amount, String unit, String state, BigInteger expiry) {
        this.quote = quote;
        this.amount = amount;
        this.unit = unit;
        this.state = state;
        this.expiry = expiry;
    }
}
