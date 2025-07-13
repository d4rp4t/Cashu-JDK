package com.cashujdk.nut05;

public abstract class PostMeltQuoteRequest {
    public String request;
    public String unit;

    public PostMeltQuoteRequest(String request, String unit) {
        this.request = request;
        this.unit = unit;
    }
}
