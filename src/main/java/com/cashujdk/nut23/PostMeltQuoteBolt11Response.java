package com.cashujdk.nut23;

import com.cashujdk.nut00.BlindSignature;
import com.cashujdk.nut05.PostMeltQuoteResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PostMeltQuoteBolt11Response extends PostMeltQuoteResponse {

    public String request;

    @JsonProperty("fee_reserve")
    public long feeReserve;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("payment_preimage")
    public String paymentPreimage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<BlindSignature> change;

    PostMeltQuoteBolt11Response(
            String request,
            String quote,
            long amount,
            String unit,
            long feeReserve,
            String state,
            long expiry,
            String paymentPreimage,
            List<BlindSignature> change
    ){
        super(quote, amount, unit, state, expiry);
        this.feeReserve = feeReserve;
        this.paymentPreimage = paymentPreimage;
        this.change = change;
    }
}
