package com.cashujdk.nut05;

public class PostMeltQuoteResponse {
    public String quote;
    public long amount;
    public String unit;
    public String state;
    public long expiry;

    public PostMeltQuoteResponse(String quote, long amount, String unit, String state, long expiry) {
        this.quote = quote;
        this.amount = amount;
        this.unit = unit;
        this.state = state;
        this.expiry = expiry;
    }
}
