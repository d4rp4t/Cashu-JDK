package com.cashujdk.nut04;

public class PostMintQuoteResponse {
    public String quote;
    public String request;
    public String unit;

    //additional method specific fields
    public PostMintQuoteResponse(String quote, String request, String unit) {
        this.quote = quote;
        this.request = request;
        this.unit = unit;
    }
}
