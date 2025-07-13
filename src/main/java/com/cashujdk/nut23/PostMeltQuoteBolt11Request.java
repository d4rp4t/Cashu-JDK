package com.cashujdk.nut23;

import com.cashujdk.nut05.PostMeltQuoteRequest;

public class PostMeltQuoteBolt11Request extends PostMeltQuoteRequest {
    //todo: change type
    public String options;

    PostMeltQuoteBolt11Request(String request, String unit, String options) {
        super(request, unit);
        this.options = options;
    }
}

//  "options": { // Optional
//    "amountless": {
//      "amount_msat": <int>