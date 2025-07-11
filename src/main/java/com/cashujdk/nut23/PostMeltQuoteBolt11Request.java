package com.cashujdk.nut23;

import com.cashujdk.nut05.PostMeltQuoteRequest;
import com.cashujdk.nut05.PostMeltRequest;

public class PostMeltQuoteBolt11Request extends PostMeltQuoteRequest {
    //todo: change type
    public String options;

    PostMeltQuoteBolt11Request(String request, String unit, String options) {
        this.request = request;
        this.unit = unit;
        this.options = options;
    }
}

//  "options": { // Optional
//    "amountless": {
//      "amount_msat": <int>